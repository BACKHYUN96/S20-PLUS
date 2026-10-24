package app.purebit.playlist

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AtomicFile
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong
import app.purebit.storage.AtomicMetadataNames
import app.purebit.storage.MetadataInput

data class PlaylistListing(val playlists:List<SavedPlaylist>,val unreadable:Int)
/** All local file reads/writes share one worker; an explicit save survives Activity recreation. */
class PlaylistStore(context:Context){
    private val directory=File(context.applicationContext.filesDir,"playlists-v1")
    private val main=Handler(Looper.getMainLooper())
    val revision:Long get()=changes.get()
    companion object {
        private val changes=AtomicLong()
        private val worker=Executors.newSingleThreadExecutor {job->Thread(job,"BitwirePlaylists").apply {priority=Thread.NORM_PRIORITY-1}}
        private const val MAX_LISTS=64
        private const val MAX_TOTAL_TRACKS=100000
        private val validId=Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
    }
    private fun file(id:String):AtomicFile {require(validId.matches(id));return AtomicFile(File(directory,"$id.bwlist"))}
    private fun ids()=AtomicMetadataNames.committed(directory.listFiles().orEmpty().filter {it.isFile}.map {it.name},".bwlist")
        .filter {validId.matches(it.removeSuffix(".bwlist"))}.map {it.removeSuffix(".bwlist")}
    private fun read(id:String):SavedPlaylist {
        val bytes=file(id).openRead().use {MetadataInput.read(it,PlaylistCodec.MAX_BYTES)}
        return PlaylistCodec.decode(bytes).also {require(it.id==id){"목록 파일 ID 불일치"}}
    }
    private fun listing():PlaylistListing {
        var unreadable=0;val values=ids().take(MAX_LISTS).mapNotNull {id->runCatching {read(id)}.getOrElse {unreadable++;null}}
        return PlaylistListing(values.sortedWith(compareBy<SavedPlaylist>{PlaylistRules.normalizedName(it.name)}.thenBy {it.id}),unreadable)
    }
    private fun save(value:SavedPlaylist){
        val existing=listing().playlists
        require(existing.none {it.id!=value.id&&PlaylistRules.normalizedName(it.name)==PlaylistRules.normalizedName(value.name)}){"같은 이름의 재생목록이 있습니다"}
        require(existing.filter {it.id!=value.id}.sumOf {it.trackIds.size}+value.trackIds.size<=MAX_TOTAL_TRACKS){"저장된 목록의 총 곡 수 한도(100,000)에 도달했습니다"}
        val bytes=PlaylistCodec.encode(value).toByteArray(Charsets.UTF_8)
        check(directory.isDirectory||directory.mkdirs()){"목록 저장 폴더를 만들 수 없습니다"}
        val atomic=file(value.id);val stream=atomic.startWrite()
        try{stream.write(bytes);atomic.finishWrite(stream)}catch(error:Throwable){atomic.failWrite(stream);throw error}
    }
    private fun <T> job(callback:(Result<T>)->Unit,changesData:Boolean=false,work:()->T){worker.execute {
        val result=runCatching(work)
        if(changesData&&result.isSuccess)changes.incrementAndGet()
        main.post {callback(result)}
    }}
    fun list(callback:(Result<PlaylistListing>)->Unit)=job(callback){listing()}
    fun create(name:String,tracks:List<String>,callback:(Result<SavedPlaylist>)->Unit)=job(callback,true){
        require(ids().size<MAX_LISTS){"재생목록은 최대 64개입니다"}
        SavedPlaylist(UUID.randomUUID().toString(),PlaylistRules.name(name),0,System.currentTimeMillis().coerceAtLeast(0),PlaylistRules.append(emptyList(),tracks)).also(::save)
    }
    private fun update(id:String,revision:Long?,callback:(Result<SavedPlaylist>)->Unit,edit:(SavedPlaylist)->SavedPlaylist)=job(callback,true){
        val old=read(id);if(revision!=null)PlaylistRules.requireRevision(old.revision,revision)
        require(old.revision<Long.MAX_VALUE){"목록 버전 한도에 도달했습니다"}
        edit(old).copy(revision=old.revision+1,modifiedAt=System.currentTimeMillis().coerceAtLeast(0)).also(::save)
    }
    fun append(id:String,tracks:List<String>,callback:(Result<SavedPlaylist>)->Unit)=update(id,null,callback){it.copy(trackIds=PlaylistRules.append(it.trackIds,tracks))}
    fun rename(id:String,revision:Long,name:String,callback:(Result<SavedPlaylist>)->Unit)=update(id,revision,callback){it.copy(name=PlaylistRules.name(name))}
    fun move(id:String,revision:Long,track:String,target:String,callback:(Result<SavedPlaylist>)->Unit)=update(id,revision,callback){it.copy(trackIds=PlaylistRules.move(it.trackIds,track,target))}
    fun remove(id:String,revision:Long,track:String,callback:(Result<SavedPlaylist>)->Unit)=update(id,revision,callback){it.copy(trackIds=it.trackIds.filter {value->value!=track})}
    fun delete(id:String,revision:Long,callback:(Result<Unit>)->Unit)=job(callback,true){val old=read(id);PlaylistRules.requireRevision(old.revision,revision);file(id).delete();check(id !in ids()){"목록을 삭제할 수 없습니다"}}
}
