package app.purebit.ui

import org.junit.Assert.*
import org.junit.Test

class GardenMotionTest {
    @Test fun depthLayersAreFiniteBoundedAndForegroundLeavesTheCenterReadable(){
        val point=FloatArray(5)
        for(seconds in listOf(0f,.016f,8f,120f,7200f))for(speed in listOf(.2f,.8f,2f))for(i in 0 until 96){
            GardenMotion.petal(i,seconds,speed,point)
            assertTrue(point.all {it.isFinite()})
            assertTrue(point[0] in -.1f..1.1f);assertTrue(point[1] in -.1f..1.1f)
            assertTrue(point[2] in 3f..39f);assertTrue(point[4] in 0f..0.6f)
            if(i%9==0)assertTrue(point[0]<.22f||point[0]>.78f)
        }
    }
    @Test fun densityIsCappedAndMotionIsDeterministicAcrossIndependentLayers(){
        assertEquals(0,GardenMotion.count(0,1f));assertEquals(74,GardenMotion.count(44,1.7f));assertEquals(96,GardenMotion.count(96,1.7f))
        val a=FloatArray(5);val b=FloatArray(5)
        GardenMotion.petal(12,7.5f,.8f,a);GardenMotion.petal(12,7.5f,.8f,b);assertArrayEquals(a,b,0f)
        GardenMotion.petal(12,8f,.8f,b);assertTrue(a[1]!=b[1])
        assertTrue(GardenMotion.heroHeight(720f)<720f*.4f)
        assertEquals(340f,GardenMotion.heroHeight(1200f),0f)
    }
}
