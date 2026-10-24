package app.purebit.library

import android.net.Uri
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Locale

/** Library tags are descriptive only. AudioEngine independently validates the source PCM format. */
data class LibraryTrack(
    val id: String,
    val uriString: String,
    val displayName: String,
    val title: String,
    val artist: String = "",
    val album: String = "",
    val durationMs: Long = 0,
    val available: Boolean = true,
    val treeUriString: String? = null,
    val albumArtist: String = "",
    val trackNumber: Int = 0,
    val discNumber: Int = 0,
    val folderUriString:String?=null,
    val folderPath:String="",
) {
    val uri: Uri get() = Uri.parse(uriString)
}

data class LibraryScanReport(
    val visitedDocuments: Int = 0,
    val importedTracks: Int = 0,
    val skippedDocuments: Int = 0,
    val deniedDirectories: Int = 0,
    val metadataFailures: Int = 0,
    val truncated: Boolean = false,
    val warnings: List<String> = emptyList(),
) {
    val summary: String get() = buildString {
        append("${importedTracks}곡 · 제외 ${skippedDocuments} · 접근 실패 ${deniedDirectories}")
        if (metadataFailures > 0) append(" · 태그 읽기 실패 $metadataFailures")
        if (truncated) append(" · 탐색 한도 도달")
    }
}

internal object LibraryRules {
    private val acceptedMimeTypes = setOf("audio/flac", "audio/x-flac", "audio/wav", "audio/x-wav", "audio/wave", "audio/vnd.wave")

    fun isSupported(name: String, mimeType: String?): Boolean {
        val extension = name.substringAfterLast('.', "").lowercase(Locale.ROOT)
        if (extension == "flac" || extension == "wav") return true
        // A MIME hint is allowed for extensionless documents. The decoder still validates contents.
        return !name.contains('.') && mimeType?.lowercase(Locale.ROOT) in acceptedMimeTypes
    }

    fun stableId(uri: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(uri.toByteArray(StandardCharsets.UTF_8))
        val alphabet = "0123456789abcdef"
        val chars = CharArray(bytes.size * 2)
        bytes.forEachIndexed { index, byte ->
            val unsigned = byte.toInt() and 0xff
            chars[index * 2] = alphabet[unsigned ushr 4]
            chars[index * 2 + 1] = alphabet[unsigned and 15]
        }
        return String(chars)
    }

    fun titleFromName(name: String): String = name.substringBeforeLast('.', name).ifBlank { name }

    fun sorted(tracks: Collection<LibraryTrack>): List<LibraryTrack> = tracks.sortedWith(
        compareBy<LibraryTrack> { it.title.lowercase(Locale.ROOT) }
            .thenBy { it.artist.lowercase(Locale.ROOT) }
            .thenBy { it.displayName.lowercase(Locale.ROOT) }
            .thenBy { it.id }
    )

    fun merge(existing: List<LibraryTrack>, additions: List<LibraryTrack>): List<LibraryTrack> {
        val byUri = LinkedHashMap<String, LibraryTrack>()
        existing.forEach { byUri[it.uriString] = it }
        additions.forEach { byUri[it.uriString] = it }
        return sorted(byUri.values)
    }
    fun reconcileScan(existing:List<LibraryTrack>,found:List<LibraryTrack>,tree:String,truncated:Boolean):List<LibraryTrack> {
        // A bounded scan has not visited every document; unseen is not evidence of missing access.
        val prior=if(truncated)existing else existing.map {if(it.treeUriString==tree)it.copy(available=false)else it}
        return merge(prior,found)
    }
}
