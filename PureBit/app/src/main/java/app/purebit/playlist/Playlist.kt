package app.purebit.playlist

import app.purebit.QueueOrder
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.text.Normalizer
import java.util.Base64
import java.util.Locale

data class SavedPlaylist(val id:String,val name:String,val revision:Long,val modifiedAt:Long,val trackIds:List<String>)
object PlaylistRules {
    const val MAX_TRACKS=10000
    private val trackIdPattern=Regex("[0-9a-f]{64}")
    fun validTrackId(id:String)=trackIdPattern.matches(id)
    fun normalizedName(name:String)=Normalizer.normalize(name.trim(),Normalizer.Form.NFC).lowercase(Locale.ROOT)
    fun name(value:String):String {
        val result=value.trim();require(result.length in 1..80&&result.none {it.isISOControl()||it=='\u2028'||it=='\u2029'}){"목록 이름은 줄바꿈 없이 1~80자입니다"};return result
    }
    fun append(ids:List<String>,added:List<String>):List<String>{
        val next=(ids+added).distinct();require(next.size<=MAX_TRACKS){"목록은 최대 10,000곡입니다"};require(next.all {validTrackId(it)}){"유효하지 않은 곡 정보입니다"};return next
    }
    fun move(ids:List<String>,from:String,to:String)=QueueOrder.move(ids,from,to)
    fun requireRevision(actual:Long,expected:Long){require(actual==expected){"목록이 변경되었습니다. 갱신한 목록에서 다시 시도하세요"}}
}
/** Bounded, non-executable UTF-8 format. Names are Base64; track references are stable IDs. */
object PlaylistCodec {
    const val MAX_BYTES=700000
    fun encode(p:SavedPlaylist):String {
        validate(p)
        val body=(listOf("BITWIRE-PLAYLIST-1",p.id,Base64.getEncoder().encodeToString(p.name.toByteArray(Charsets.UTF_8)),p.revision.toString(),p.modifiedAt.toString(),p.trackIds.size.toString())+p.trackIds).joinToString("\n",postfix="\n")
        val text=body+"SHA256 ${hash(body)}\n"
        require(text.toByteArray(Charsets.UTF_8).size<=MAX_BYTES){"목록 파일 크기 한도를 초과했습니다"};return text
    }
    fun decode(bytes:ByteArray):SavedPlaylist {
        require(bytes.size<=MAX_BYTES){"목록 파일 크기 한도를 초과했습니다"}
        val text=utf8(bytes);require(text.endsWith("\n")&&!text.contains('\r')){"잘리거나 지원하지 않는 목록 파일입니다"}
        val lines=text.dropLast(1).split('\n')
        require(lines.size in 7..7+PlaylistRules.MAX_TRACKS&&lines[0]=="BITWIRE-PLAYLIST-1"){"지원하지 않는 재생목록 형식입니다"}
        val body=lines.dropLast(1).joinToString("\n",postfix="\n")
        require(lines.last()=="SHA256 ${hash(body)}"){"목록 파일 무결성 검사에 실패했습니다"}
        val count=lines[5].toInt();require(count in 0..PlaylistRules.MAX_TRACKS&&lines.size==count+7){"목록 곡 수 오류"}
        return SavedPlaylist(lines[1],utf8(Base64.getDecoder().decode(lines[2])),lines[3].toLong(),lines[4].toLong(),lines.drop(6).take(count)).also(::validate)
    }
    private fun hash(text:String)=java.security.MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8)).joinToString(""){"%02x".format(it.toInt() and 255)}
    private fun utf8(bytes:ByteArray)=Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString()
    private fun validate(p:SavedPlaylist){
        require(p.id.matches(Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))){"목록 ID 오류"}
        require(PlaylistRules.name(p.name)==p.name&&p.revision>=0&&p.modifiedAt>=0){"목록 정보 오류"}
        require(p.trackIds.size<=PlaylistRules.MAX_TRACKS&&p.trackIds.distinct().size==p.trackIds.size){"목록 곡 수 오류"}
        require(p.trackIds.all {PlaylistRules.validTrackId(it)}){"목록 곡 ID 오류"}
    }
}
