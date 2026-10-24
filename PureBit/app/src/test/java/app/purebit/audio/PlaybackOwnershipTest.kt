package app.purebit.audio

import org.junit.Assert.*
import org.junit.Test

class PlaybackOwnershipTest {
    @Test fun retiringServiceCannotRunTheNextSourceBeforeItsReplacementIsReady() {
        val flow=PlaybackOwnership();val old=Any();val next=Any();val events=mutableListOf<String>()
        flow.started(old);flow.stopping(old)
        flow.submit {events+="new source"}
        assertTrue(flow.hasPending);assertTrue(events.isEmpty())
        assertTrue(flow.stopped(old));assertTrue(events.isEmpty())
        flow.started(next)
        assertEquals(listOf("new source"),events);assertFalse(flow.hasPending)
    }
    @Test fun lateDestroyOfAnOldOwnerDoesNotRetireTheNewOwner() {
        val flow=PlaybackOwnership();val old=Any();val replacement=Any()
        flow.started(old);flow.stopping(old);flow.started(replacement)
        assertFalse(flow.stopped(old));flow.stopping(old)
        assertTrue(flow.ready)
        var ran=false;assertNull(flow.submit {ran=true});assertTrue(ran)
        assertTrue(flow.stopped(replacement));assertFalse(flow.ready)
    }
    @Test fun latestDeferredSelectionWinsAndAnOldCancellationCannotDeleteIt() {
        val flow=PlaybackOwnership();val events=mutableListOf<String>()
        val first=flow.submit {events+="old"}!!
        flow.submit {events+="latest"};flow.cancel(first)
        flow.started(Any());assertEquals(listOf("latest"),events)
    }
    @Test fun rejectedOrReplacedSourceRequestsDoNotStartOnALaterService() {
        val flow=PlaybackOwnership();var count=0
        val denied=flow.submit {count++}!!;flow.cancel(denied)
        assertFalse(flow.hasPending);flow.started(Any());assertEquals(0,count)
        val retiring=Any();flow.started(retiring);flow.stopping(retiring)
        flow.submit {count++};flow.cancelPending() // File selection without auto-play replaces it.
        flow.started(Any());assertEquals(0,count)
    }
    @Test fun drainingStopsIfAWorkItemRetiresItsOwner() {
        val flow=PlaybackOwnership();val token=Any();var count=0
        flow.submit {count++;flow.stopping(token);flow.submit {count++}}
        flow.started(token)
        assertEquals(1,count);assertFalse(flow.ready);assertTrue(flow.hasPending)
        flow.started(Any());assertEquals(2,count);assertFalse(flow.hasPending)
    }
}
