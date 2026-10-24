package app.purebit.lyrics

import java.io.EOFException
import java.io.FileInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.util.Locale

/** Text metadata only. No source writes, decoding, sample conversion, or guessed lyric timing. */
data class LyricContent(val timeline:LyricTimeline?=null,val plain:String="",val origin:String){
    val status:String get()=if(timeline!=null)"$origin · ${timeline.lines.size}구간" else "$origin · 시간 정보 없음"
}

object EmbeddedLyrics {
    private const val MAX_TAG=4*1024*1024
    private const val MAX_TEXT=65536
    private val keys=setOf("LYRICS","UNSYNCEDLYRICS","UNSYNCED LYRICS","SYNCEDLYRICS","LYRICS_SYNCED","LYRICS-SYNCED")
    private fun u32(b:ByteArray,p:Int,le:Boolean=false):Long {
        require(p>=0&&p+4<=b.size){"가사 태그 길이 오류"}
        var value=0L;for(i in 0..3)value=(value shl 8) or (b[p+if(le)3-i else i].toLong() and 255);return value
    }
    private fun sync(b:ByteArray,p:Int):Int {require(p+4<=b.size);var n=0;for(i in 0..3){val v=b[p+i].toInt() and 255;require(v<128){"ID3 크기 오류"};n=(n shl 7) or v};return n}
    private fun id(b:ByteArray,p:Int,n:Int)=String(b,p,n,Charsets.ISO_8859_1)
    private fun utf8(b:ByteArray)=Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(b)).toString()
    private fun content(text:String,origin:String):LyricContent? {
        val clean=text.trim('\u0000','\uFEFF',' ','\r','\n').replace("\r\n","\n").replace('\r','\n')
        if(clean.isBlank())return null
        val timed=runCatching {Lrc.parse(clean.toByteArray(Charsets.UTF_8))}.getOrNull()
        return if(timed!=null)LyricContent(timeline=timed,origin=origin)else LyricContent(plain=clean.take(MAX_TEXT),origin=origin)
    }
    private fun best(a:LyricContent?,b:LyricContent?)=if(b==null)a else if(a?.timeline!=null&&b.timeline==null)a else if(a==null||b.timeline!=null)b else a

    fun read(input:InputStream):LyricContent? {
        val reader=Reader(input);var head=reader.bytes(4);var result:LyricContent?=null
        if(id(head,0,3)=="ID3"){
            val header=head+reader.bytes(6);val size=sync(header,6);require(size<=MAX_TAG){"ID3 가사 검색 범위 초과"}
            result=id3(header+reader.bytes(size),"내장 ID3")
            if(header[3].toInt()==4&&(header[5].toInt() and 16)!=0)reader.skip(10)
            head=reader.bytes(4)
        }
        if(id(head,0,4)=="fLaC"){
            repeat(128){
                val h=reader.bytes(4);val type=h[0].toInt() and 127
                val size=((h[1].toInt() and 255) shl 16) or ((h[2].toInt() and 255) shl 8) or (h[3].toInt() and 255)
                if(type==4){require(size<=MAX_TAG){"FLAC 코멘트가 너무 큽니다"};result=best(result,vorbis(reader.bytes(size)))}else reader.skip(size.toLong())
                if((h[0].toInt() and 128)!=0)return result
            }
            error("FLAC 메타데이터 블록 한도 초과")
        }
        if(id(head,0,4)=="RIFF"){
            val header=reader.bytes(8);var remaining=u32(header,0,true)-4
            if(id(header,4,4)!="WAVE")return result
            repeat(1024){
                if(remaining<8)return result
                val h=reader.bytes(8);val size=u32(h,4,true);val padded=size+(size and 1);require(padded<=remaining-8){"WAV 태그 범위 오류"}
                val key=id(h,0,4)
                if(key.equals("id3 ",true)||key=="ID3\u0000"){
                    require(size<=MAX_TAG){"WAV ID3 가사 검색 범위 초과"};result=best(result,id3(reader.bytes(size.toInt()),"내장 WAV ID3"))
                }else if(key=="LIST"&&size<=MAX_TAG)result=best(result,info(reader.bytes(size.toInt())))
                else reader.skip(size)
                if(size and 1L!=0L)reader.skip(1)
                remaining-=8+padded
            }
            error("WAV 메타데이터 청크 한도 초과")
        }
        return result
    }
    private fun vorbis(b:ByteArray):LyricContent? {
        var p=0
        fun number():Int {val n=u32(b,p,true);p+=4;require(n<=b.size){"FLAC 코멘트 길이 오류"};return n.toInt()}
        val vendor=number();require(p+vendor<=b.size);p+=vendor
        val count=number();require(count<=65536){"FLAC 코멘트 수 한도 초과"};var result:LyricContent?=null
        repeat(count){val n=number();require(p+n<=b.size);val field=b.copyOfRange(p,p+n);p+=n
            val eq=field.indexOf('='.code.toByte());if(eq>0){val key=id(field,0,eq).uppercase(Locale.ROOT)
                if(key in keys){val text=runCatching {utf8(field.copyOfRange(eq+1,field.size))}.getOrNull()
                    if(text!=null)result=best(result,content(text,"내장 FLAC · $key"))}}}
        return result
    }
    private fun info(b:ByteArray):LyricContent? {
        if(b.size<4||id(b,0,4)!="INFO")return null
        var p=4
        while(p+8<=b.size){val key=id(b,p,4);val size=u32(b,p+4,true);p+=8;require(size<=b.size-p)
            if(key=="ILYR")return runCatching {content(utf8(b.copyOfRange(p,p+size.toInt())),"내장 WAV INFO · ILYR")}.getOrNull()
            p+=size.toInt()+(size.toInt() and 1)}
        return null
    }
    private fun unsync(bytes:ByteArray):ByteArray {val out=ByteArray(bytes.size);var n=0;var p=0
        while(p<bytes.size){val value=bytes[p++];out[n++]=value;if((value.toInt() and 255)==255&&p<bytes.size&&bytes[p].toInt()==0)p++};return out.copyOf(n)}
    private fun id3(raw:ByteArray,origin:String):LyricContent? {
        if(raw.size<10||id(raw,0,3)!="ID3")return null
        val version=raw[3].toInt();if(version!=3&&version!=4)return null
        val length=sync(raw,6);require(length<=MAX_TAG&&length<=raw.size-10)
        var body=raw.copyOfRange(10,10+length);val flags=raw[5].toInt() and 255
        if(version==3&&flags and 128!=0)body=unsync(body)
        var p=0
        if(flags and 64!=0){require(body.size>=4);val ext=if(version==4)sync(body,0)else u32(body,0).toInt()+4;require(ext in 4..body.size);p=ext}
        var result:LyricContent?=null;var frames=0
        while(p+10<=body.size&&frames++<4096){
            val key=id(body,p,4);if(body[p].toInt()==0)break
            val size=if(version==4)sync(body,p+4).toLong()else u32(body,p+4)
            val format=body[p+9].toInt() and 255;p+=10;require(size<=body.size-p){"ID3 프레임 범위 오류"}
            val end=p+size.toInt()
            if(key=="USLT"||key=="SYLT"){
                val unsupported=if(version==3)format and 192!=0 else format and 12!=0
                if(!unsupported){var data=body.copyOfRange(p,end)
                    if(version==4&&(format and 2!=0||flags and 128!=0))data=unsync(data)
                    var prefix=0;if(version==3&&format and 32!=0||version==4&&format and 64!=0)prefix++
                    if(version==4&&format and 1!=0)prefix+=4
                    if(prefix<data.size)data=data.copyOfRange(prefix,data.size)else data=byteArrayOf()
                    val parsed=runCatching {if(key=="USLT")uslt(data,"$origin · USLT")else sylt(data,"$origin · SYLT")}.getOrNull()
                    result=best(result,parsed)
                }
            }
            p=end
        }
        return result
    }
    private class Text(private val b:ByteArray,val encoding:Int,var p:Int){
        private var charset:Charset=when(encoding){0->Charsets.ISO_8859_1;1->Charsets.UTF_16;2->Charsets.UTF_16BE;3->Charsets.UTF_8;else->error("지원하지 않는 ID3 문자 인코딩")}
        private val wide=encoding==1||encoding==2
        fun decode(start:Int,end:Int):String {
            if(encoding==1&&end-start>=2){val a=b[start].toInt() and 255;val c=b[start+1].toInt() and 255
                if(a==255&&c==254)charset=Charsets.UTF_16LE else if(a==254&&c==255)charset=Charsets.UTF_16BE}
            return charset.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(b,start,end-start)).toString().removePrefix("\uFEFF")
        }
        fun terminated():String {
            val start=p;val step=if(wide)2 else 1
            while(p+step<=b.size){if(b[p].toInt()==0&&(!wide||b[p+1].toInt()==0)){val end=p;p+=step;return decode(start,end)};p+=step}
            error("ID3 가사 구분자 누락")
        }
        fun rest()=decode(p,b.size)
    }
    private fun uslt(b:ByteArray,origin:String):LyricContent? {
        require(b.size>=5);val t=Text(b,b[0].toInt() and 255,4);t.terminated();return content(t.rest(),origin)
    }
    private fun sylt(b:ByteArray,origin:String):LyricContent? {
        require(b.size>=7);if(b[4].toInt()!=2||b[5].toInt()!=1)return null // MPEG-frame timing is never guessed.
        val t=Text(b,b[0].toInt() and 255,6);t.terminated();val lines=ArrayList<LyricLine>()
        while(t.p<b.size){require(lines.size<Lrc.MAX_CUES);val text=t.terminated();val time=u32(b,t.p);t.p+=4;lines+=LyricLine(time,text.take(512))}
        if(lines.isEmpty())return null
        return LyricContent(timeline=LyricTimeline(lines.groupBy {it.timeMs}.toSortedMap().map {(time,items)->LyricLine(time,items.joinToString("\n"){it.text}.take(1024))}),origin=origin)
    }
    private class Reader(private val input:InputStream){
        private var read=0L
        fun bytes(n:Int):ByteArray {require(n>=0&&read+n<=16*1024*1024){"가사 메타데이터 검색 범위 초과"};read+=n
            val result=ByteArray(n);var p=0;while(p<n){val count=input.read(result,p,n-p);if(count<0)throw EOFException("음원 태그가 잘렸습니다");if(count==0){val v=input.read();if(v<0)throw EOFException();result[p++]=v.toByte()}else p+=count};return result}
        fun skip(n:Long){
            require(n>=0);var remaining=n
            if(input is FileInputStream){while(remaining>0){val skipped=runCatching {input.skip(remaining)}.getOrDefault(0);if(skipped<=0)break;remaining-=skipped}}
            require(remaining<=16*1024*1024){"이 문서 제공자는 큰 WAV 태그의 빠른 탐색을 지원하지 않습니다"}
            while(remaining>0){val size=minOf(8192L,remaining).toInt();bytes(size);remaining-=size}
        }
    }
}
