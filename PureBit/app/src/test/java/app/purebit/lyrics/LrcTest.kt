package app.purebit.lyrics

import org.junit.Assert.*
import org.junit.Test

class LrcTest {
    private fun parse(text:String)=Lrc.parse(text.toByteArray())
    @Test fun timestampsRepeatedTranslationsAndBlankCuesFollowPlayheadIncludingSeekBack(){
        val t=parse("\uFEFF[ar:Artist]\n[00:05.50][00:10.500]꽃잎\n[00:05.500]Petals\n[00:07.0]\n[01:00]끝")
        assertEquals(listOf(5500L,7000L,10500L,60000L),t.lines.map {it.timeMs})
        assertEquals("꽃잎\nPetals",t.lines[0].text);assertEquals("",t.lines[1].text)
        assertEquals(-1,t.activeIndex(5499));assertEquals(0,t.activeIndex(5500));assertEquals(1,t.activeIndex(7000))
        assertEquals(3,t.activeIndex(999999));assertEquals(0,t.activeIndex(6000))
    }
    @Test fun timestampsInsideTextRemainLiteral(){assertEquals("text [01:00] annotation",parse("[00:01]text [01:00] annotation").lines.single().text)}
    @Test fun signedOffsetAdvancesOrDelaysOnlyTheLyricTimeline(){
        assertEquals(4000L,parse("[offset:+1000]\n[00:05]a").lines.single().timeMs)
        assertEquals(6000L,parse("[00:05]a\n[offset:-1000]").lines.single().timeMs)
        assertEquals(0L,parse("[offset:1000]\n[00:00.5]a").lines.single().timeMs)
    }
    @Test fun fractionsAndUnsortedInputAreDeterministic(){
        val t=parse("[00:10.001]later\n[00:01.1]one\n[00:02.12]two")
        assertEquals(listOf(1100L,2120L,10001L),t.lines.map {it.timeMs})
    }
    @Test fun malformedAndOversizedDocumentsDoNotReplaceAValidAssociation(){
        for(text in listOf("plain text","[00:99]bad","[offset:9999999]\n[00:00]x","x".repeat(4097))){assertTrue(runCatching {parse(text)}.isFailure)}
        assertTrue(runCatching {Lrc.parse(ByteArray(Lrc.MAX_BYTES+1))}.isFailure)
        assertTrue(runCatching {Lrc.parse(byteArrayOf(0xc3.toByte(),0x28))}.isFailure)
        assertTrue(runCatching {parse((0..Lrc.MAX_CUES).joinToString("\n"){"[00:01]a"})}.isFailure)
    }
}
