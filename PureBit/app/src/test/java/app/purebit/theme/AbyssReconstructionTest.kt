package app.purebit.theme

import app.purebit.theme.abyss.AbyssLayout
import app.purebit.theme.abyss.JellyMesh
import org.junit.Assert.*
import org.junit.Test

class AbyssReconstructionTest {
    @Test fun viewportFittingLeavesSpaceForControlsAndStillAllowsShortScreenScroll(){
        assertEquals(420,AbyssLayout.heroHeight(760f,340f))
        assertEquals(240,AbyssLayout.heroHeight(480f,400f))
        assertEquals(520,AbyssLayout.heroHeight(1200f,300f))
    }
    @Test fun jellyMeshIsDeterministicAndFiniteAcrossAnimation(){
        val a=FloatArray(JellyMesh.FLOATS);val b=FloatArray(JellyMesh.FLOATS)
        JellyMesh.fill(10f,20f,100f,200f,1f,a);JellyMesh.fill(10f,20f,100f,200f,1f,b)
        assertArrayEquals(a,b,0f)
        for(t in listOf(0f,1f,50f,10000f)){
            JellyMesh.fill(10f,20f,100f,200f,t,a)
            for(i in a.indices step 2){assertTrue(a[i].isFinite()&&a[i] in -5f..125f);assertTrue(a[i+1].isFinite()&&a[i+1] in 10f..230f)}
        }
    }
    @Test fun tentaclesDeformDifferentlyFromBellRatherThanRigidScaling(){
        val a=FloatArray(JellyMesh.FLOATS);val b=FloatArray(JellyMesh.FLOATS)
        JellyMesh.fill(0f,0f,100f,200f,0f,a);JellyMesh.fill(0f,0f,100f,200f,1f,b)
        val bell=(3*(JellyMesh.COLUMNS+1)+JellyMesh.COLUMNS/2)*2
        val tail=(15*(JellyMesh.COLUMNS+1)+JellyMesh.COLUMNS/2)*2
        assertTrue(kotlin.math.abs((b[tail]-a[tail])-(b[bell]-a[bell]))>.5f)
    }
    @Test fun strandVariantsDoNotFoldMeshOrChangeTheBellPropulsionPhase(){
        val a=FloatArray(JellyMesh.FLOATS);val b=FloatArray(JellyMesh.FLOATS)
        for(variant in 0..4)for(step in 0..80) {
            val time=step*.1f
            JellyMesh.fill(0f,0f,100f,200f,time,a,variant)
            JellyMesh.fill(0f,0f,100f,200f,time,b,0)
            assertEquals(b[0],a[0],.0001f)
            for(row in 0..JellyMesh.ROWS)for(col in 0 until JellyMesh.COLUMNS) {
                val at=(row*(JellyMesh.COLUMNS+1)+col)*2
                assertTrue(a[at+2]>a[at])
            }
        }
        JellyMesh.fill(0f,0f,100f,200f,1f,a,0)
        JellyMesh.fill(0f,0f,100f,200f,1f,b,1)
        assertTrue(kotlin.math.abs(a[a.size-2]-b[b.size-2])>.1f)
    }
}
