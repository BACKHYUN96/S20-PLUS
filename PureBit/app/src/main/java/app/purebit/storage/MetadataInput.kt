package app.purebit.storage

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction

/** Enforce limits on the opened stream, including AtomicFile's recovered backup. */
object MetadataInput {
    fun read(input:InputStream,maxBytes:Int):ByteArray {
        require(maxBytes in 0 until Int.MAX_VALUE)
        val output=ByteArrayOutputStream(minOf(maxBytes,8192))
        val buffer=ByteArray(8192)
        var count=0
        while(true){
            val n=input.read(buffer,0,minOf(buffer.size,maxBytes-count+1))
            if(n<0)break
            if(n==0){
                val b=input.read();if(b<0)break
                require(count<maxBytes){"메타데이터 파일 크기 한도를 초과했습니다"}
                output.write(b);count++
            }else{
                require(n<=maxBytes-count){"메타데이터 파일 크기 한도를 초과했습니다"}
                output.write(buffer,0,n);count+=n
            }
        }
        return output.toByteArray()
    }
    fun utf8(bytes:ByteArray):String=Charsets.UTF_8.newDecoder()
        .onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT)
        .decode(ByteBuffer.wrap(bytes)).toString()
}

object AtomicMetadataNames {
    /** A .new file alone is an uncommitted write, not a saved document. Preserve it on disk. */
    fun committed(names:List<String>,suffix:String):List<String> = names.map {it.removeSuffix(".bak")}
        .filter {it.endsWith(suffix)}.distinct().sorted()
}
