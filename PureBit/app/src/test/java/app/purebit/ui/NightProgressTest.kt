package app.purebit.ui

import org.junit.Assert.*
import org.junit.Test

class NightProgressTest {
    @Test fun automaticNightIsMonotonicAndBoundedAfterTwelveMinutes(){
        val night=NightProgress();var last=0f
        repeat(900){night.advance(1f,true,true);assertTrue(night.strength>=last&&night.strength<=1f);last=night.strength}
        assertEquals(1f,night.strength,0f);assertEquals(720f,night.listenedSeconds,0f)
    }
    @Test fun pausedHiddenOrReducedMotionTimeDoesNotAdvanceNight(){
        val night=NightProgress()
        repeat(50){night.advance(1f,false,true);night.advance(1f,true,false)}
        night.advance(-10f,true,true);night.advance(Float.NaN,true,true)
        assertEquals(0f,night.listenedSeconds,0f)
        night.advance(10000f,true,true);assertEquals(1f,night.listenedSeconds,0f)
    }
    @Test fun fixedModesAndPreviewLeaveListeningProgressIntact(){
        val night=NightProgress();repeat(360){night.advance(1f,true,true)}
        assertEquals(.5f,night.strength,.0001f)
        night.mode=NightMode.WARM;night.preview();assertEquals(1f,night.strength,0f)
        repeat(10){night.advance(1f,false,true)};assertEquals(0f,night.strength,0f)
        night.mode=NightMode.DUSK;assertEquals(.55f,night.strength,0f)
        night.mode=NightMode.DEEP;night.advance(1f,true,true);assertEquals(1f,night.strength,0f)
        night.mode=NightMode.AUTO;assertEquals(.5f,night.strength,.0001f)
    }
}
