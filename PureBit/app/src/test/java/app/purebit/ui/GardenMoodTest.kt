package app.purebit.ui

import org.junit.Assert.*
import org.junit.Test

class GardenMoodTest {
    private val pink=0xfff1afc5.toInt()
    @Test fun monochromeAndTransparentCoversPreservePink(){
        assertEquals(pink,AlbumMoonColor.select(intArrayOf(0xff000000.toInt(),0xffffffff.toInt(),0xff888888.toInt(),0x00ff0000),pink))
        assertEquals(pink,AlbumMoonColor.select(intArrayOf(),pink))
    }
    @Test fun DominantColorIsSubtleAndIgnoresGrayBorders(){
        val result=AlbumMoonColor.select(IntArray(100){if(it<80)0xff111111.toInt() else 0xff0000ff.toInt()},pink)
        assertTrue((result and 255)>(pink and 255))
        for(shift in listOf(0,8,16))assertTrue(abs((result ushr shift and 255)-(pink ushr shift and 255))<=72)
        assertEquals(result,AlbumMoonColor.select(IntArray(20){0xff0000ff.toInt()},pink))
    }
    @Test fun playbackEnvelopeEasesWithoutOvershootAndHonorsReducedMotion(){
        val mood=GardenPresence();var last=0f
        repeat(120){val next=mood.update(true,1f/60);assertTrue(next>=last&&next<=1);last=next}
        assertTrue(last>.99f)
        repeat(120){val next=mood.update(false,1f/60);assertTrue(next<=last&&next>=0);last=next}
        assertTrue(last<.03f)
        assertEquals(0f,mood.update(true,1f,false),0f)
        assertEquals(0f,mood.update(true,-1f),0f)
    }
    private fun abs(value:Int)=kotlin.math.abs(value)
}
