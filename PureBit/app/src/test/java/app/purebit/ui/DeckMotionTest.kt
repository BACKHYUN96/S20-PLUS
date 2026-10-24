package app.purebit.ui

import org.junit.Assert.*
import org.junit.Test

class DeckMotionTest {
    @Test fun silenceDoesNotInventBassAndStrongAttackDecaysSmoothly() {
        val pulse=BassPulse()
        repeat(60){assertEquals(0f,pulse.update(0f,1/60f,true),0f)}
        val attack=pulse.update(1f,1/60f,true)
        assertTrue(attack>0f && attack<1f)
        val decay=pulse.update(0f,1/60f,true)
        assertTrue(decay>0f)
        repeat(300){pulse.update(0f,1/60f,true)}
        assertTrue(pulse.level<.001f)
        pulse.update(1f,1/60f,true)
        assertEquals(0f,pulse.update(1f,1/60f,false),0f)
    }
    @Test fun pulseIsBoundedAcrossDifferentFrameRatesAndClampsLongFrames() {
        for(fps in listOf(30,60,90,120)) {
            val pulse=BassPulse()
            repeat(fps*3){val v=pulse.update(if(it<fps)1f else .3f,1f/fps,true);assertTrue(v.isFinite()&&v in 0f..1f)}
            assertTrue(pulse.update(1f,30f,true) in 0f..1f)
        }
    }
    @Test fun circuitTraversalUsesDistanceAndHandlesZeroSegments() {
        val point=FloatArray(2)
        fun at(f:Float)=CircuitFlow.point(f,0f,0f,3f,0f,3f,4f,6f,4f,point)
        at(0f);assertArrayEquals(floatArrayOf(0f,0f),point,0f)
        at(.5f);assertArrayEquals(floatArrayOf(3f,2f),point,.00001f)
        at(1f);assertArrayEquals(floatArrayOf(6f,4f),point,0f)
        at(-1f);assertArrayEquals(floatArrayOf(0f,0f),point,0f)
        CircuitFlow.point(.5f,2f,3f,2f,3f,2f,3f,2f,3f,point)
        assertArrayEquals(floatArrayOf(2f,3f),point,0f)
    }
}
