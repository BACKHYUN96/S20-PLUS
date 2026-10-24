package app.purebit

import app.purebit.QueueNavigation.Previous
import org.junit.Assert.*
import org.junit.Test

class QueueNavigationTest {
    @Test fun previousAfterNextDoesNotSeekUsingOutgoingTracksPosition() {
        // B has just been selected while the worker still reports A at 90 seconds.
        assertEquals(Previous.Load(0),QueueNavigation.previous(1,3,90_000,null,false))
        // A second NEXT has selected C; PREVIOUS must select B, not seek outgoing A.
        assertEquals(Previous.Load(1),QueueNavigation.previous(2,3,90_000,null,false))
    }
    @Test fun preparedCurrentTrackRestartsOnlyAfterThreeSeconds() {
        assertEquals(Previous.SeekStart,QueueNavigation.previous(1,3,3001,null,true))
        assertEquals(Previous.Load(0),QueueNavigation.previous(1,3,3000,null,true))
        assertEquals(Previous.Load(0),QueueNavigation.previous(1,3,0,null,true))
    }
    @Test fun coldBookmarkRestartsSelectedTrackWithoutSeekingAnUnloadedSource() {
        assertEquals(Previous.Load(1),QueueNavigation.previous(1,3,90_000,30_000,false))
        assertEquals(Previous.Load(0),QueueNavigation.previous(1,3,90_000,3000,false))
    }
    @Test fun firstTrackWrapsAndSingleTrackReloadsWhenNotPrepared() {
        assertEquals(Previous.Load(2),QueueNavigation.previous(0,3,0,null,true))
        assertEquals(Previous.Load(0),QueueNavigation.previous(0,1,90_000,null,false))
        assertEquals(Previous.SeekStart,QueueNavigation.previous(0,1,90_000,null,true))
    }
    @Test fun emptyOrUnselectedQueueDoesNothing() {
        assertEquals(Previous.None,QueueNavigation.previous(-1,0,90_000,null,false))
        assertEquals(Previous.None,QueueNavigation.previous(-1,3,90_000,null,true))
        assertEquals(Previous.None,QueueNavigation.previous(3,3,90_000,null,true))
    }
}
