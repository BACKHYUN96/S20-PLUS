package app.purebit.session

import org.junit.Assert.*
import org.junit.Test

class ResumeRulesTest {
    @Test fun positionNeverSeeksBeyondRevalidatedSourceAndEofRestarts(){
        assertEquals(12345L,ResumeRules.position(12345,60000))
        assertEquals(59999L,ResumeRules.position(59999,60000))
        for(value in listOf(-1L,60000L,Long.MAX_VALUE))assertEquals(0L,ResumeRules.position(value,60000))
        assertEquals(0L,ResumeRules.position(12345,0))
    }
    @Test fun diagnosticOrPreparingSourcesCannotOverwriteMusicBookmark(){
        val uri="content://music/1"
        assertTrue(ResumeRules.canCheckpoint(uri,uri,0,false,true))
        assertFalse(ResumeRules.canCheckpoint(uri,uri,96000,false,true))
        assertFalse(ResumeRules.canCheckpoint(uri,uri,0,true,true))
        assertFalse(ResumeRules.canCheckpoint(uri,uri,0,false,false))
        assertFalse(ResumeRules.canCheckpoint(uri,"generated:test",0,false,true))
        assertFalse(ResumeRules.canCheckpoint(null,uri,0,false,true))
        assertFalse(ResumeRules.canCheckpoint("content://music/2",uri,0,false,true))
    }
}
