package app.purebit.theme

import app.purebit.theme.abyss.CoverFade
import org.junit.Assert.*
import org.junit.Test

class CoverFadeTest {
    @Test fun fadeReleasesOldCoverAtCompletion() {
        val f=CoverFade<Any>();val a=Any();val b=Any()
        f.submit(a,0f,false);f.submit(b,1f,true)
        assertSame(a,f.previous);assertSame(b,f.current);assertEquals(0f,f.progress(1f),0f)
        assertEquals(1f,f.progress(2f),0f);assertNull(f.previous)
    }
    @Test fun rapidChangesRetainDominantCoverAndLatestTarget() {
        val f=CoverFade<Any>();val a=Any();val b=Any();val c=Any()
        f.submit(a,0f,false);f.submit(b,1f,true);f.submit(c,1.1f,true)
        assertSame(a,f.previous);assertSame(c,f.current)
        f.progress(3f);assertNull(f.previous);assertSame(c,f.current)
    }
    @Test fun noArtAndReducedMotionNeverRetainOldCover() {
        val f=CoverFade<Any>();f.submit(Any(),0f,false);f.submit(null,1f,true)
        assertNotNull(f.previous);assertNull(f.current)
        f.settle();assertNull(f.previous);assertEquals(1f,f.progress(1f),0f)
    }
    @Test fun sameBitmapDoesNotRestartTransition() {
        val f=CoverFade<Any>();val a=Any();f.submit(a,1f,true)
        val before=f.progress(1.4f);f.submit(a,1.4f,true)
        assertEquals(before,f.progress(1.4f),0f)
    }
}
