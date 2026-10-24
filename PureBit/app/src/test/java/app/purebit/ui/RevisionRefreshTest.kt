package app.purebit.ui

import org.junit.Assert.*
import org.junit.Test

class RevisionRefreshTest {
    @Test fun mutationDuringReadIsNotMistakenForDisplayedData() {
        val refresh = RevisionRefresh()
        assertTrue(refresh.needsRefresh(0))
        val read = refresh.begin(0)
        assertFalse(refresh.needsRefresh(1)) // Do not pile up reads while one is running.
        assertTrue(refresh.complete(read))
        assertTrue(refresh.needsRefresh(1)) // A save finished after the read began.
        assertTrue(refresh.complete(refresh.begin(1)))
        assertFalse(refresh.needsRefresh(1))
    }
    @Test fun lateReadCannotCompleteANewerRead() {
        val refresh = RevisionRefresh()
        val old = refresh.begin(3)
        val current = refresh.begin(4)
        assertFalse(refresh.complete(old))
        assertFalse(refresh.needsRefresh(5))
        assertTrue(refresh.complete(current))
        assertTrue(refresh.needsRefresh(5))
    }
    @Test fun leavingScreenCancelsDeliveryWithoutAcknowledgingData() {
        val refresh = RevisionRefresh()
        val old = refresh.begin(0)
        refresh.cancel()
        assertFalse(refresh.complete(old))
        assertTrue(refresh.needsRefresh(0))
        val resumed = refresh.begin(1)
        assertFalse(refresh.complete(old))
        assertTrue(refresh.complete(resumed))
        assertFalse(refresh.needsRefresh(1))
    }
}
