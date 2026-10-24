package app.purebit.startup

import org.junit.Assert.*
import org.junit.Test

class StartupPolicyTest {
    @Test fun normalLaunchAndSafeRecoveryHaveSeparateDestinations(){
        assertEquals(StartupPolicy.Destination.PLAYER,StartupPolicy.destination(false,false))
        assertEquals(StartupPolicy.Destination.SAFE_PLAYER,StartupPolicy.destination(true,false))
        assertEquals(StartupPolicy.Destination.RECOVERY,StartupPolicy.destination(true,true))
        assertEquals(StartupPolicy.Destination.RECOVERY,StartupPolicy.destination(false,true))
    }
    @Test fun taskRemovalAndMemoryKillDoNotEnableSafeMode(){
        assertFalse(StartupPolicy.confirmedStartupCrash(100,42,101,42,false))
    }
    @Test fun oldCrashAndAnotherProcessCannotBeAssignedToThisLaunch(){
        assertFalse(StartupPolicy.confirmedStartupCrash(100,42,99,42,true))
        assertFalse(StartupPolicy.confirmedStartupCrash(100,42,101,43,true))
        assertFalse(StartupPolicy.confirmedStartupCrash(0,42,101,42,true))
        assertFalse(StartupPolicy.confirmedStartupCrash(100,0,101,0,true))
        assertTrue(StartupPolicy.confirmedStartupCrash(100,42,101,42,true))
    }
}
