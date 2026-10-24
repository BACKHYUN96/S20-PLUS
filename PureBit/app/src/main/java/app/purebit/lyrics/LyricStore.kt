package app.purebit.lyrics

import android.content.Context
import app.purebit.audio.AudioLog
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.AtomicFile
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.Executors
import java.io.FileNotFoundException
import app.purebit.storage.MetadataInput
import app.purebit.storage.StoreLifetime

/** Copies user-selected LRC into private storage. Original files and audio are never modified. */
class LyricStore(context:Context):AutoCloseable {
    private val app=context.applicationContext
    private val directory=File(app.filesDir,"lyrics-v1")
    private val main=Handler(Looper.getMainLooper())
    private val worker=Executors.newSingleThreadExecutor {r->Thread(r,"BitwireLyrics").apply {priority=Thread.NORM_PRIORITY-1}}
    companion object {private val filesLock=Any()}
    private val lifetime=StoreLifetime(filesLock)
    private fun file(source:String):AtomicFile {
        val hash=MessageDigest.getInstance("SHA-256").digest(source.toByteArray()).joinToString(""){"%02x".format(it.toInt() and 255)}
        return AtomicFile(File(directory,"$hash.lrc"))
    }
    private fun read(input:java.io.InputStream):ByteArray {
        return MetadataInput.read(input,Lrc.MAX_BYTES)
    }
    private fun <T> submit(callback:(Result<T>)->Unit,task:()->T){
        lifetime.offer {worker.execute {val result=runCatching {check(!lifetime.closed);task()};main.post {if(!lifetime.closed)callback(result)}}}
    }
    fun load(source:String,callback:(Result<LyricContent?>)->Unit)=submit(callback){
        val atomic=file(source)
        val local=lifetime.access {
            try {atomic.openRead().use {LyricContent(timeline=Lrc.parse(read(it)),origin="연결 LRC")}}
            catch(error:FileNotFoundException){
                if(atomic.baseFile.exists()||File(atomic.baseFile.path+".bak").exists())throw error
                null
            }
        }
        val result=local?:app.contentResolver.openInputStream(Uri.parse(source))?.use {EmbeddedLyrics.read(it)}
        AudioLog.event("lyrics_read","origin=${result?.origin?:"none"} timed=${result?.timeline!=null} cues=${result?.timeline?.lines?.size?:0} plain_chars=${result?.plain?.length?:0}; tags_only=true audio_unchanged=true")
        result
    }
    fun import(source:String,uri:Uri,callback:(Result<LyricTimeline>)->Unit)=submit(callback){
        require(source.startsWith("content://")){"먼저 음악 파일을 선택하세요"}
        val bytes=app.contentResolver.openInputStream(uri)?.use(::read)?:error("LRC 파일을 열 수 없습니다")
        val parsed=Lrc.parse(bytes)
        lifetime.access {
            directory.mkdirs();val atomic=file(source);val output=atomic.startWrite()
            try {output.write(bytes);atomic.finishWrite(output)}catch(e:Throwable){atomic.failWrite(output);throw e}
        }
        parsed
    }
    fun remove(source:String,callback:(Result<Unit>)->Unit)=submit(callback){lifetime.access {file(source).delete()}}
    override fun close(){lifetime.close {worker.shutdownNow()}}
}
