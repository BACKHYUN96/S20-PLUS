package app.purebit.library

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.AtomicFile
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException
import java.util.ArrayDeque
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import app.purebit.storage.MetadataInput

/** Offline SAF library; no broad storage permission, media scan, network, or audio-thread work. */
class LibraryStore(context: Context) : AutoCloseable {
    private val appContext = context.applicationContext
    private val resolver = appContext.contentResolver
    private val main = Handler(Looper.getMainLooper())
    private val worker = Executors.newSingleThreadExecutor { job -> Thread(job, "BitwireLibrary").apply { priority = Thread.NORM_PRIORITY - 1 } }
    private val closed = AtomicBoolean(false)
    private val file = AtomicFile(File(appContext.filesDir, "bitwire-library.json"))
    private val shared = synchronized(CATALOGUE_LOCK) {
        catalogues.getOrPut(file.baseFile.absolutePath) { SharedCatalogue() }
    }
    private val treeUris get() = shared.treeUris
    private var snapshot: List<LibraryTrack>
        get() = shared.snapshot
        set(value) { shared.snapshot = value }
    @Volatile var lastScanReport = LibraryScanReport()
        private set

    init {
        // Startup reads only the small private catalogue. Document/tag access stays on the worker.
        synchronized(CATALOGUE_LOCK) {
            if (!shared.loaded) { load(); shared.loaded = true }
        }
    }

    fun tracks(): List<LibraryTrack> = snapshot.toList()
    fun trees(): List<Uri> = synchronized(treeUris) { treeUris.map(Uri::parse) }

    /** Callback runs on the main thread. May be used again to rescan an existing folder grant. */
    fun scanTree(uri: Uri, callback: (Result<List<LibraryTrack>>) -> Unit) = submit(callback) {
        require(uri.scheme == "content" && DocumentsContract.isTreeUri(uri)) { "음악 폴더 접근 권한이 필요합니다." }
        val warnings = ArrayList<String>()
        persistReadGrant(uri, warnings)
        val tree = uri.toString()
        val result = try {
            collectTree(uri, warnings)
        } catch (error: Exception) {
            // Closing a screen must not turn a cancelled scan into a revoked-grant catalogue.
            ensureOpen()
            commit {
                snapshot = snapshot.map { if (it.treeUriString == tree) it.copy(available = false) else it }
                lastScanReport = LibraryScanReport(deniedDirectories = 1, warnings = warnings + (error.message ?: "폴더 접근 실패"))
                runCatching { save() }
            }
            throw IllegalStateException("폴더를 읽을 수 없습니다. 폴더를 다시 선택해 권한을 허용해주세요.", error)
        }
        ensureOpen()
        // Keep missing items visible but unavailable, so stale grants do not silently lose a queue.
        commit {
            snapshot = LibraryRules.reconcileScan(snapshot,result.first,tree,result.second.truncated)
            synchronized(treeUris) { treeUris.add(tree) }
            lastScanReport = result.second
            save()
            Log.i("PureBitAudio", "library_scan uri=$uri ${lastScanReport.summary}; bounded=true tags_only=true")
            snapshot.toList()
        }
    }

    /** Adds one ACTION_OPEN_DOCUMENT result; metadata failures do not prevent decoder validation. */
    fun addDocument(uri: Uri, callback: (Result<LibraryTrack>) -> Unit) = submit(callback) {
        require(uri.scheme == "content") { "문서 선택기로 FLAC/WAV를 선택해주세요." }
        val warnings = ArrayList<String>()
        persistReadGrant(uri, warnings)
        val name = documentName(uri)
        require(LibraryRules.isSupported(name, resolver.getType(uri))) { "FLAC 또는 WAV 파일만 추가할 수 있습니다." }
        // Opening briefly distinguishes revoked/deleted documents from tag-reader incompatibility.
        resolver.openFileDescriptor(uri, "r")?.use { } ?: error("파일을 열 수 없습니다.")
        val prior = snapshot.firstOrNull { it.uriString == uri.toString() }
        val track = readTags(uri, name, prior?.treeUriString,prior?.folderUriString,prior?.folderPath.orEmpty()).first
        ensureOpen()
        commit {
            snapshot = LibraryRules.merge(snapshot, listOf(track))
            lastScanReport = LibraryScanReport(importedTracks = 1, warnings = warnings)
            save()
            track
        }
    }

    /** Artwork only, never source-format verification. Callback bitmap is owned by the caller. */
    fun artwork(track: LibraryTrack, maxEdgePx: Int = 512, isCurrent: () -> Boolean = {true}, callback: (Bitmap?) -> Unit) {
        enqueue {
            if(!isCurrent())return@enqueue
            val bitmap = runCatching {
                check(!closed.get())
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(appContext, track.uri)
                    val bytes = retriever.embeddedPicture ?: return@runCatching null
                    if (bytes.size > MAX_ARTWORK_BYTES) return@runCatching null
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null
                    val edge = maxEdgePx.coerceIn(64, 1024)
                    var sample = 1
                    while (bounds.outWidth / sample > edge * 2 || bounds.outHeight / sample > edge * 2) sample *= 2
                    val options = BitmapFactory.Options().apply { inSampleSize = sample }
                    val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) ?: return@runCatching null
                    val scale = minOf(1f, edge.toFloat() / maxOf(decoded.width, decoded.height))
                    if (scale >= 1f) decoded else Bitmap.createScaledBitmap(decoded, maxOf(1, (decoded.width * scale).toInt()), maxOf(1, (decoded.height * scale).toInt()), true).also { decoded.recycle() }
                } finally { retriever.release() }
            }.getOrNull()
            main.post { if (!closed.get() && isCurrent()) callback(bitmap) else bitmap?.recycle() }
        }
    }

    private fun collectTree(treeUri: Uri, warnings: MutableList<String>): Pair<List<LibraryTrack>, LibraryScanReport> {
        data class Folder(val id:String,val depth:Int,val path:String)
        val queue = ArrayDeque<Folder>()
        val rootId=DocumentsContract.getTreeDocumentId(treeUri)
        val rootDocument=DocumentsContract.buildDocumentUriUsingTree(treeUri,rootId)
        val rootName=runCatching {resolver.query(rootDocument,arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),null,null,null)?.use {if(it.moveToFirst())it.getString(0)else null}}.getOrNull()?.takeIf {it.isNotBlank()}?:"선택한 폴더"
        queue.add(Folder(rootId,0,rootName.take(512)))
        val seen = HashSet<String>()
        val tracks = ArrayList<LibraryTrack>()
        var visited = 0
        var skipped = 0
        var denied = 0
        var failedTags = 0
        var truncated = false
        while (queue.isNotEmpty()) {
            check(!closed.get() && !Thread.currentThread().isInterrupted) { "라이브러리 탐색이 취소됐습니다." }
            if (visited >= MAX_DOCUMENTS || tracks.size >= MAX_TRACKS) { truncated = true; break }
            val (parentId, depth, parentPath) = queue.removeFirst()
            if (!seen.add(parentId)) continue
            if (depth > MAX_DEPTH) { truncated = true; continue }
            val children = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentId)
            try {
                val projection = arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE)
                val cursor = resolver.query(children, projection, null, null, null) ?: error("폴더 목록 응답 없음")
                cursor.use {
                    val idColumn = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                    val nameColumn = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    val mimeColumn = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
                    while (it.moveToNext()) {
                        check(!closed.get() && !Thread.currentThread().isInterrupted) { "라이브러리 탐색이 취소됐습니다." }
                        if (visited >= MAX_DOCUMENTS || tracks.size >= MAX_TRACKS) { truncated = true; break }
                        visited++
                        val id = it.getString(idColumn) ?: continue
                        val name = it.getString(nameColumn).orEmpty().ifBlank { "Untitled" }
                        val mime = it.getString(mimeColumn)
                        if (mime == DocumentsContract.Document.MIME_TYPE_DIR) { queue.add(Folder(id,depth+1,"$parentPath / $name".take(512))); continue }
                        if (!LibraryRules.isSupported(name, mime)) { skipped++; continue }
                        val document = DocumentsContract.buildDocumentUriUsingTree(treeUri, id)
                        try {
                            val (track, tagsFailed) = readTags(document,name,treeUri.toString(),DocumentsContract.buildDocumentUriUsingTree(treeUri,parentId).toString(),parentPath)
                            tracks.add(track)
                            if (tagsFailed) failedTags++
                        } catch (error: Exception) {
                            denied++
                            if (warnings.size < MAX_WARNINGS) warnings.add("$name: ${error.message ?: "읽기 실패"}")
                        }
                    }
                }
            } catch (error: Exception) {
                // The root must be readable; partial subfolder denial remains a visible scan warning.
                if (depth == 0) throw error
                denied++
                if (warnings.size < MAX_WARNINGS) warnings.add("하위 폴더 접근 실패: ${error.message}")
            }
        }
        return tracks to LibraryScanReport(visited, tracks.size, skipped, denied, failedTags, truncated, warnings.toList())
    }

    private fun readTags(uri: Uri, name: String, tree: String?,folder:String?=null,folderPath:String=""): Pair<LibraryTrack, Boolean> {
        val boundedName = name.take(1024)
        val base = LibraryTrack(LibraryRules.stableId(uri.toString()), uri.toString(), boundedName, LibraryRules.titleFromName(boundedName).take(512), treeUriString = tree,folderUriString=folder,folderPath=folderPath.take(512))
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(appContext, uri)
            fun text(key: Int): String = retriever.extractMetadata(key)?.trim()?.take(512).orEmpty()
            base.copy(
                title = text(MediaMetadataRetriever.METADATA_KEY_TITLE).ifBlank { base.title },
                artist = text(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                album = text(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                albumArtist = text(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST),
                trackNumber = text(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER).substringBefore('/').toIntOrNull()?.coerceIn(0,9999)?:0,
                discNumber = text(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER).substringBefore('/').toIntOrNull()?.coerceIn(0,999)?:0,
                durationMs = text(MediaMetadataRetriever.METADATA_KEY_DURATION).toLongOrNull()?.coerceIn(0, MAX_DURATION_MS) ?: 0,
            ) to false
        } catch (error: SecurityException) {
            throw error
        } catch (error: FileNotFoundException) {
            throw error
        } catch (_: Exception) {
            // A tag-parser failure is not proof of an invalid FLAC/WAV; the actual decoder decides.
            base to true
        } finally { retriever.release() }
    }

    private fun documentName(uri: Uri): String {
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
            if (it.moveToFirst()) return it.getString(0).orEmpty().ifBlank { "Untitled" }
        }
        return uri.lastPathSegment?.substringAfterLast('/') ?: "Untitled"
    }

    private fun persistReadGrant(uri: Uri, warnings: MutableList<String>) {
        try { resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        catch (_: SecurityException) { warnings.add("이 제공자는 영구 읽기 권한을 허용하지 않았습니다. 앱 재시작 후 다시 선택해야 할 수 있습니다.") }
    }

    private fun load() {
        try {
            // openRead must run even if only the AtomicFile .bak exists. Bound the recovered stream.
            val bytes=file.openRead().use {MetadataInput.read(it,MAX_CATALOGUE_BYTES.toInt())}
            val root = JSONObject(MetadataInput.utf8(bytes))
            val tracks = root.optJSONArray("tracks") ?: JSONArray()
            val restored = ArrayList<LibraryTrack>()
            val grants = resolver.persistedUriPermissions.filter { it.isReadPermission }.map { it.uri.toString() }.toSet()
            for (i in 0 until minOf(tracks.length(), MAX_TRACKS)) {
                val item = tracks.optJSONObject(i) ?: continue
                val uri = item.optString("uri")
                if (!uri.startsWith("content://")) continue
                val name = item.optString("displayName", "Untitled").take(1024)
                val tree = item.optString("tree").takeIf { it.startsWith("content://") }
                val granted = uri in grants || (tree != null && tree in grants)
                restored.add(LibraryTrack(LibraryRules.stableId(uri), uri, name, item.optString("title", LibraryRules.titleFromName(name)).take(512), item.optString("artist").take(512), item.optString("album").take(512), item.optLong("durationMs").coerceIn(0, MAX_DURATION_MS), item.optBoolean("available", true) && granted, tree, item.optString("albumArtist").take(512),item.optInt("trackNumber").coerceIn(0,9999),item.optInt("discNumber").coerceIn(0,999),item.optString("folder").takeIf {it.startsWith("content://")},item.optString("folderPath").take(512)))
            }
            snapshot = LibraryRules.merge(emptyList(), restored)
            val trees = root.optJSONArray("trees") ?: JSONArray()
            synchronized(treeUris) { for (i in 0 until minOf(trees.length(), MAX_TREES)) trees.optString(i).takeIf { it.startsWith("content://") }?.let(treeUris::add) }
        } catch (error: FileNotFoundException) {
            // A fresh install has no catalogue; an existing but unreadable file is a real failure.
            if(file.baseFile.exists()||File(file.baseFile.path+".bak").exists())
                Log.w("PureBitAudio", "library_catalogue_read_failed ${error.javaClass.simpleName}")
        } catch (error: Exception) {
            Log.w("PureBitAudio", "library_catalogue_read_failed ${error.javaClass.simpleName}")
        }
    }

    private fun save() {
        check(Thread.holdsLock(CATALOGUE_LOCK)) { "Catalogue writes must be serialized" }
        ensureOpen()
        // Do not silently persist only a prefix and lose remaining entries on the next launch.
        require(snapshot.size<=MAX_TRACKS){"라이브러리는 최대 10,000곡입니다. 기존 목록은 유지했습니다."}
        require(treeUris.size<=MAX_TREES){"음악 폴더는 최대 64개입니다. 기존 목록은 유지했습니다."}
        val items = JSONArray()
        snapshot.take(MAX_TRACKS).forEach { track ->
            items.put(JSONObject().put("uri", track.uriString).put("displayName", track.displayName).put("title", track.title).put("artist", track.artist).put("album", track.album).put("albumArtist",track.albumArtist).put("trackNumber",track.trackNumber).put("discNumber",track.discNumber).put("folder",track.folderUriString?:JSONObject.NULL).put("folderPath",track.folderPath).put("durationMs", track.durationMs).put("available", track.available).put("tree", track.treeUriString ?: JSONObject.NULL))
        }
        val root = JSONObject().put("version", 1).put("tracks", items).put("trees", JSONArray(synchronized(treeUris) { treeUris.take(MAX_TREES) }))
        val encoded=root.toString().toByteArray(Charsets.UTF_8)
        require(encoded.size<=MAX_CATALOGUE_BYTES){"라이브러리 메타데이터 용량 한도(16 MiB)에 도달했습니다"}
        val output = file.startWrite()
        try {
            output.write(encoded)
            file.finishWrite(output)
        } catch (error: Exception) { file.failWrite(output); throw error }
    }

    private fun <T> submit(callback: (Result<T>) -> Unit, work: () -> T) {
        enqueue {
            val result = runCatching { ensureOpen(); work() }
            main.post { if (!closed.get()) callback(result) }
        }
    }

    /** A blocking provider may ignore interrupts; this guard must precede every catalogue commit. */
    private fun ensureOpen() {
        check(!closed.get() && !Thread.currentThread().isInterrupted) { "라이브러리 작업이 취소됐습니다." }
    }

    private fun <T> commit(work: () -> T): T = synchronized(CATALOGUE_LOCK) {
        ensureOpen()
        val before=snapshot;val beforeTrees=synchronized(treeUris){treeUris.toList()};val beforeReport=lastScanReport
        try {work()}catch(error:Throwable){
            // AtomicFile keeps the previous disk catalogue; keep the shared view consistent too.
            snapshot=before;synchronized(treeUris){treeUris.clear();treeUris.addAll(beforeTrees)};lastScanReport=beforeReport
            throw error
        }
    }

    private fun enqueue(work: () -> Unit) {
        synchronized(CATALOGUE_LOCK) { if (!closed.get()) worker.execute { work() } }
    }

    override fun close() = synchronized(CATALOGUE_LOCK) {
        // An already-committing atomic write finishes before close returns. No later write can start.
        if (closed.compareAndSet(false, true)) worker.shutdownNow()
        Unit
    }

    private class SharedCatalogue {
        @Volatile var snapshot: List<LibraryTrack> = emptyList()
        val treeUris = linkedSetOf<String>()
        var loaded = false
    }

    private companion object {
        private val CATALOGUE_LOCK = Any()
        private val catalogues = HashMap<String, SharedCatalogue>()
        const val MAX_DOCUMENTS = 50_000
        const val MAX_TRACKS = 10_000
        const val MAX_DEPTH = 32
        const val MAX_TREES = 64
        const val MAX_WARNINGS = 20
        const val MAX_ARTWORK_BYTES = 8 * 1024 * 1024
        const val MAX_CATALOGUE_BYTES = 16 * 1024 * 1024L
        const val MAX_DURATION_MS = 7 * 24 * 60 * 60 * 1000L
    }
}
