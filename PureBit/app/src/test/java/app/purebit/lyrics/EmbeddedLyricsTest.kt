package app.purebit.lyrics

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import org.junit.Assert.*
import org.junit.Test

class EmbeddedLyricsTest {
    private fun bytes(s:String)=s.toByteArray(Charsets.UTF_8)
    private fun number(n:Int,le:Boolean=false)=ByteArray(4){i->(n ushr ((if(le)i else 3-i)*8)).toByte()}
    private fun sync(n:Int)=ByteArray(4){i->(n ushr ((3-i)*7) and 127).toByte()}
    private fun vorbis(vararg fields:String):ByteArray {
        val out=ByteArrayOutputStream();out.write(number(0,true));out.write(number(fields.size,true))
        fields.forEach {val b=bytes(it);out.write(number(b.size,true));out.write(b)}
        val body=out.toByteArray();return bytes("fLaC")+byteArrayOf(0x84.toByte(),(body.size ushr 16).toByte(),(body.size ushr 8).toByte(),body.size.toByte())+body
    }
    private fun frame(key:String,payload:ByteArray,version:Int=3)=bytes(key)+(if(version==4)sync(payload.size)else number(payload.size))+byteArrayOf(0,0)+payload
    private fun tag(body:ByteArray,version:Int=3)=bytes("ID3")+byteArrayOf(version.toByte(),0,0)+sync(body.size)+body
    private fun chunk(id:String,body:ByteArray)=bytes(id)+number(body.size,true)+body+if(body.size%2==1)byteArrayOf(0)else byteArrayOf()
    private fun wav(vararg chunks:ByteArray):ByteArray {val body=bytes("WAVE")+chunks.fold(byteArrayOf()){a,b->a+b};return bytes("RIFF")+number(body.size,true)+body}
    private fun read(data:ByteArray)=EmbeddedLyrics.read(ByteArrayInputStream(data))
    @Test fun flacReadsPlainUnicodeAndCaseInsensitiveTags(){
        val result=read(vorbis("ARTIST=Other","lyrics=첫 구절\n二行目"))!!
        assertNull(result.timeline);assertEquals("첫 구절\n二行目",result.plain);assertTrue(result.origin.contains("FLAC"))
    }
    @Test fun syncedEmbeddedCommentsWinAndUseActualTimestamps(){
        val result=read(vorbis("UNSYNCEDLYRICS=plain","SYNCEDLYRICS=[00:02]첫 줄\n[00:04]둘째"))!!
        assertEquals(2000L,result.timeline!!.lines.first().timeMs);assertEquals(1,result.timeline.activeIndex(4000));assertEquals("",result.plain)
    }
    @Test fun wavId3UsltReadsUtf8AndUtf16WithoutTouchingData(){
        val utf8=byteArrayOf(3)+bytes("kor")+bytes("description")+byteArrayOf(0)+bytes("내장 가사\n둘째 줄")
        val result=read(wav(chunk("data",ByteArray(31)),chunk("id3 ",tag(frame("USLT",utf8,4),4))))!!
        assertEquals("내장 가사\n둘째 줄",result.plain)
        val utf16=byteArrayOf(1)+bytes("eng")+"설명".toByteArray(Charsets.UTF_16)+byteArrayOf(0,0)+"달빛\nLyrics".toByteArray(Charsets.UTF_16)
        assertEquals("달빛\nLyrics",read(wav(chunk("ID3 ",tag(frame("USLT",utf16)))))!!.plain)
    }
    @Test fun syltAcceptsMillisecondsAndNeverGuessesMpegFrameTiming(){
        val prefix=byteArrayOf(3)+bytes("eng")+byteArrayOf(2,1,0)
        val lines=bytes("first")+byteArrayOf(0)+number(1200)+bytes("second")+byteArrayOf(0)+number(2500)
        val result=read(wav(chunk("id3 ",tag(frame("SYLT",prefix+lines)))))!!
        assertEquals(listOf(1200L,2500L),result.timeline!!.lines.map {it.timeMs})
        assertNull(read(wav(chunk("id3 ",tag(frame("SYLT",byteArrayOf(3)+bytes("eng")+byteArrayOf(1,1,0)+lines))))))
    }
    @Test fun wavInfoAndId3PrefixedFlacAreSupported(){
        assertEquals("INFO lyrics",read(wav(chunk("LIST",bytes("INFO")+chunk("ILYR",bytes("INFO lyrics\u0000")))))!!.plain)
        val uslt=byteArrayOf(0)+bytes("eng")+byteArrayOf(0)+bytes("fallback")
        assertEquals("inside",read(tag(frame("USLT",uslt))+vorbis("LYRICS=[00:01]inside"))!!.timeline!!.lines.single().text)
    }
    @Test fun missingLyricsAndMalformedLengthsAreNotInvented(){
        assertNull(read(vorbis("TITLE=No lyrics")))
        assertNull(read(wav(chunk("data",ByteArray(8)))))
        assertTrue(runCatching {read(bytes("fLaC")+byteArrayOf(0x84.toByte(),127,127,127))}.isFailure)
        assertTrue(runCatching {read(vorbis("LYRICS=ok").dropLast(2).toByteArray())}.isFailure)
        assertNull(read(wav(chunk("id3 ",tag(frame("USLT",byteArrayOf(3)+bytes("eng")+bytes("no terminator")))))))
    }
    @Test fun id3v23UnsynchronizationPreservesLatin1Lyrics(){
        val payload=byteArrayOf(0)+bytes("eng")+byteArrayOf(0,0xff.toByte(),0xe0.toByte())
        val body=frame("USLT",payload);val encoded=ByteArrayOutputStream()
        for(value in body){encoded.write(value.toInt() and 255);if(value.toInt() and 255==255)encoded.write(0)}
        val physical=encoded.toByteArray()
        val tagged=bytes("ID3")+byteArrayOf(3,0,0x80.toByte())+sync(physical.size)+physical
        assertEquals("ÿà",read(wav(chunk("id3 ",tagged)))!!.plain)
    }
    @Test fun largeWavDataUsesFastFileSkipAndNonseekableSourcesStopAtTheLimit(){
        val dataSize=32*1024*1024
        val uslt=byteArrayOf(3)+bytes("eng")+byteArrayOf(0)+bytes("after PCM")
        val tail=chunk("id3 ",tag(frame("USLT",uslt)))
        val header=bytes("RIFF")+number(4+8+dataSize+tail.size,true)+bytes("WAVEdata")+number(dataSize,true)
        val file=java.io.File.createTempFile("bitwire-lyrics-", ".wav")
        try{
            java.io.RandomAccessFile(file,"rw").use {it.write(header);it.seek((header.size+dataSize).toLong());it.write(tail)}
            assertEquals("after PCM",java.io.FileInputStream(file).use {EmbeddedLyrics.read(it)}!!.plain)
            assertTrue(runCatching {read(header)}.exceptionOrNull()?.message.orEmpty().contains("빠른 탐색"))
        }finally {file.delete()}
    }

}
