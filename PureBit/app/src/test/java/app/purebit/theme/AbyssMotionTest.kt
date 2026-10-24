package app.purebit.theme

import app.purebit.theme.abyss.AbyssDepth
import app.purebit.theme.abyss.AbyssRipples
import org.junit.Assert.*
import org.junit.Test

class AbyssMotionTest {
    @Test fun constantLoudnessEmitsOneWaveThenSettles() {
        val state=AbyssRipples()
        repeat(600){state.update(.8f,1f/60,true)}
        assertEquals(1,state.strengths.count {it>0f})
        assertTrue(state.ages.all {it>1.65f})
        assertTrue(state.glow<.001f)
    }
    @Test fun separateBassAttacksEmitSeparateWaves() {
        val state=AbyssRipples()
        repeat(3){
            repeat(35){state.update(0f,1f/60,true)}
            state.update(.6f,1f/60,true)
        }
        assertEquals(3,state.strengths.count {it>0f})
        assertTrue(state.glow>0f)
    }
    @Test fun disabledOrNewSourceResetHasNoOldWaves() {
        val state=AbyssRipples();state.update(.9f,.02f,true);state.update(0f,.02f,false)
        assertEquals(0f,state.glow,0f);assertTrue(state.strengths.all {it==0f})
        state.update(Float.NaN,.02f,true);assertEquals(0f,state.glow,0f)
        state.update(.9f,.02f,true);state.reset();assertTrue(state.ages.all {it>1.65f})
    }
    @Test fun particlePlanesAreDeterministicAndNearIsLargerAndFaster() {
        val a=FloatArray(4);val b=FloatArray(4)
        AbyssDepth.sample(2,1f,1f,a);AbyssDepth.sample(2,1f,1f,b);assertArrayEquals(a,b,0f)
        val sizes=(0..2).map {AbyssDepth.sample(it,0f,1f,a);a[2]}
        assertTrue(sizes[2]>sizes[1]&&sizes[1]>sizes[0])
        val movement=(0..2).map {AbyssDepth.sample(it,0f,1f,a);AbyssDepth.sample(it,1f,1f,b);a[1]-b[1]}
        assertTrue(movement[2]>movement[1]&&movement[1]>movement[0])
        repeat(96){i->AbyssDepth.sample(i,10000f,.65f,a);assertTrue(a.all {it.isFinite()});assertTrue(a[1] in -.16f..1.16f)}
    }
}
