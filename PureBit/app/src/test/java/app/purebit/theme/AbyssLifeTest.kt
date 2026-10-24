package app.purebit.theme

import app.purebit.theme.abyss.AbyssLife
import org.junit.Assert.*
import org.junit.Test

class AbyssLifeTest {
    @Test fun powerStrokeIsShortAndRecoveryLong(){
        assertEquals(0f,AbyssLife.contraction(0f),.0001f)
        assertEquals(1f,AbyssLife.contraction(3.8f*.26f),.0001f)
        assertEquals(0f,AbyssLife.contraction(3.8f),.0001f)
        val power=AbyssLife.swimDistance(3.8f*.26f)-AbyssLife.swimDistance(0f)
        val recovery=AbyssLife.swimDistance(3.8f)-AbyssLife.swimDistance(3.8f*.26f)
        assertTrue(power/(3.8f*.26f)>recovery/(3.8f*.74f)*3)
    }
    @Test fun propulsionIsContinuousAndNeverReversesAtCycleBoundary(){
        var previous=AbyssLife.swimDistance(-1f)
        for(i in -99..10000){val now=AbyssLife.swimDistance(i*.01f);assertTrue(now>=previous-.00001f);previous=now}
        assertTrue(AbyssLife.swimDistance(3.8001f)-AbyssLife.swimDistance(3.7999f)<.001f)
    }
    @Test fun livingObjectsAreDeterministicAndBoundedOverLongPlayback(){
        val a=FloatArray(6);val b=FloatArray(6)
        for(t in listOf(0f,1f,40f,3600f,86400f)){
            repeat(96){i->
                AbyssLife.bubble(i,t,.65f,a);AbyssLife.bubble(i,t,.65f,b);assertArrayEquals(a,b,0f)
                assertTrue(a.all {it.isFinite()});assertTrue(a[0] in .01f.. .99f);assertTrue(a[1] in -.17f..1.15f)
                assertTrue(a[2]>0f);assertTrue(a[3] in 0f..190f);assertTrue(a[5] in .92f..1f)
                if(i%3==2)assertTrue(a[0]<.1f||a[0]>.9f)
            }
            repeat(5){i->AbyssLife.jelly(i,t,a);assertTrue(a.all {it.isFinite()});assertTrue(a[0]<.2f||a[0]>.8f);assertTrue(a[3] in 0f..1f)}
        }
    }
    @Test fun largerBubblesRiseFasterAndDeformMore(){
        val a=FloatArray(6);val b=FloatArray(6)
        val velocities=(0..2).map {i->AbyssLife.bubble(i,1f,1f,a);AbyssLife.bubble(i,1.1f,1f,b);(a[1]-b[1])/.1f}
        assertTrue(velocities[2]>velocities[1]&&velocities[1]>velocities[0])
        AbyssLife.bubble(0,1f,1f,a);AbyssLife.bubble(2,1f,1f,b)
        assertTrue(b[2]>a[2]);assertTrue(b[5]<a[5])
    }
    @Test fun bubbleWrapOccursOnlyWhileInvisible(){
        val a=FloatArray(6);val b=FloatArray(6)
        repeat(24){i->
            AbyssLife.bubble(i,0f,1f,a)
            for(step in 1..4000){AbyssLife.bubble(i,step*.05f,1f,b)
                if(b[1]>a[1]+.5f){assertTrue(a[3]<2);assertTrue(b[3]<2)}
                b.copyInto(a)
            }
        }
    }
}
