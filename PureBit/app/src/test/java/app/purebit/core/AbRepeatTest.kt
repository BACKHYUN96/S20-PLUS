package app.purebit.core

import org.junit.Assert.*
import org.junit.Test

class AbRepeatTest {
    @Test fun marksSourcePositionsAndRequiresOrderedMinimumInterval() {
        val a = AbRepeat().withA(1500, 10000)
        assertFalse(a.active)
        assertEquals(AbRepeat(1500, 4500), a.withB(4500, 10000))
        for (invalid in listOf(-1L, 1500L, 2499L, 10001L)) {
            assertThrows(IllegalArgumentException::class.java) { a.withB(invalid, 10000) }
        }
        assertThrows(IllegalArgumentException::class.java) { AbRepeat().withB(5000, 10000) }
        assertThrows(IllegalArgumentException::class.java) { AbRepeat().withA(9001, 10000) }
        assertThrows(IllegalArgumentException::class.java) { AbRepeat().withA(0, 999) }
        assertTrue(AbRepeat().withA(0, 1000).withB(1000, 1000).active)
    }
    @Test fun newStartDisarmsPreviousEndAndCanBeRearmed() {
        val old = AbRepeat().withA(1000, 10000).withB(8000, 10000)
        val next = old.withA(3000, 10000)
        assertFalse(next.active); assertNull(next.bMs)
        assertTrue(next.withB(9000, 10000).active)
        assertFalse(AbRepeat().active)
    }
    @Test fun onlyActiveRangeConstrainsSeekAndEndIsExclusive() {
        val loop = AbRepeat().withA(1000, 10000).withB(8000, 10000)
        assertEquals(1000, loop.constrain(-10, 10000))
        assertEquals(1000, loop.constrain(0, 10000))
        assertEquals(1000, loop.constrain(1000, 10000))
        assertEquals(7999, loop.constrain(7999, 10000))
        assertEquals(1000, loop.constrain(8000, 10000))
        assertEquals(1000, loop.constrain(10000, 10000))
        assertEquals(0, AbRepeat(1000).constrain(-1, 10000))
        assertEquals(10000, AbRepeat().constrain(20000, 10000))
    }
    @Test fun exclusiveFrameBoundsPreserveRateAndAllUnselectedTailFrames() {
        for (rate in listOf(44100, 48000, 96000)) {
            val total = rate * 10L + 17
            val loop = AbRepeat().withA(1001, 10000).withB(2001, 10000)
            val startFrame = loop.aMs!! * rate / 1000
            assertEquals(rate.toLong(), loop.endFrame(rate, total) - startFrame)
            assertEquals(total, AbRepeat().endFrame(rate, total))
            assertEquals(total, AbRepeat(0, 20000).endFrame(rate, total))
        }
    }
    @Test fun releaseCannotResumeAfterSourceSwitchPauseFocusLossOrAnotherGesture() {
        val playing = ScrubToken(generation = 3, intent = 7, resume = true)
        assertTrue(playing.isCurrent(3, 7))
        assertFalse(playing.isCurrent(4, 7))
        assertFalse(playing.isCurrent(3, 8))
        assertTrue(playing.resume)
        val paused = ScrubToken(3, 9, false)
        assertTrue(paused.isCurrent(3, 9))
        assertFalse(paused.resume)
    }
}
