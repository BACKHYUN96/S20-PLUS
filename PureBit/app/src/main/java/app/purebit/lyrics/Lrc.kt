package app.purebit.lyrics

import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction

data class LyricLine(val timeMs:Long,val text:String)
data class LyricTimeline(val lines:List<LyricLine>){
    /** Last cue at or before the actual playhead, including deliberate blank cues. */
    fun activeIndex(positionMs:Long):Int {
        var lo=0;var hi=lines.size
        while(lo<hi){val mid=(lo+hi) ushr 1;if(lines[mid].timeMs<=positionMs)lo=mid+1 else hi=mid}
        return lo-1
    }
}

object Lrc {
    const val MAX_BYTES=512*1024
    const val MAX_CUES=4096
    private val timestamp=Regex("\\[(\\d{1,6}):([0-5]\\d)(?:[.:](\\d{1,3}))?]")
    private val offset=Regex("(?im)^\\s*\\[offset:([+-]?\\d{1,7})]\\s*$")
    fun parse(bytes:ByteArray):LyricTimeline {
        require(bytes.size<=MAX_BYTES){"LRC 파일은 512 KiB 이하여야 합니다"}
        val text=Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes)).toString().removePrefix("\uFEFF")
        val adjustment=offset.findAll(text).lastOrNull()?.groupValues?.get(1)?.toLongOrNull()?:0L
        require(adjustment in -600_000L..600_000L){"LRC offset은 ±10분 범위여야 합니다"}
        val cues=ArrayList<LyricLine>()
        text.lineSequence().forEach {line->
            require(line.length<=4096){"LRC 한 줄이 너무 깁니다"}
            val value=line.trimStart();val matches=ArrayList<MatchResult>();var cursor=0
            while(cursor<value.length){val match=timestamp.find(value,cursor)?:break;if(match.range.first!=cursor)break
                matches+=match;cursor=match.range.last+1;while(cursor<value.length&&value[cursor].isWhitespace())cursor++}
            if(matches.isNotEmpty()){
                val content=value.substring(cursor).trim().take(512)
                for(match in matches){
                    require(cues.size<MAX_CUES){"LRC는 최대 4096개 구간을 지원합니다"}
                    val fraction=match.groupValues[3].padEnd(3,'0').ifBlank {"000"}.toLong()
                    val time=match.groupValues[1].toLong()*60_000+match.groupValues[2].toLong()*1000+fraction
                    // Positive offset advances lyrics; audio timestamps/seek positions never change.
                    cues+=LyricLine((time-adjustment).coerceAtLeast(0),content)
                }
            }
        }
        require(cues.isNotEmpty()){"시간 정보가 있는 UTF-8 LRC 파일을 선택하세요"}
        val merged=cues.groupBy {it.timeMs}.toSortedMap().map {(time,items)->LyricLine(time,items.map {it.text}.distinct().joinToString("\n").take(1024))}
        return LyricTimeline(merged)
    }
}
