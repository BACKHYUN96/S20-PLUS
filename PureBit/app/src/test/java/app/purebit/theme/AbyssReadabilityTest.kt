package app.purebit.theme

import app.purebit.theme.abyss.AbyssReadability
import org.junit.Assert.*
import org.junit.Test

class AbyssReadabilityTest {
    @Test fun uncoveredSceneRetainsFullOpacityAndVisibleTextGetsAttenuated() {
        val regions=AbyssReadability()
        assertEquals(1f,regions.visibility(0f,0f,10f,10f,20f),0f)
        regions.add(100f,100f,300f,150f)
        assertEquals(.18f,regions.visibility(120f,110f,160f,170f,20f),.0001f)
        assertEquals(1f,regions.visibility(0f,0f,10f,10f,20f),0f)
    }
    @Test fun fadingStartsBeforeTentaclesTouchTheProtectedAreaAndIsContinuous() {
        val regions=AbyssReadability();regions.add(100f,100f,300f,150f)
        var previous=1f
        for(bottom in 70..100) {
            val value=regions.visibility(120f,bottom-40f,170f,bottom.toFloat(),30f)
            assertTrue(value<=previous+.00001f)
            assertTrue(value in .18f..1f)
            previous=value
        }
        assertTrue(kotlin.math.abs(regions.visibility(120f,50f,170f,99.99f,30f)-regions.visibility(120f,50f,170f,100f,30f))<.001f)
    }
    @Test fun scrollingOrChangingPagesCanReplaceRegionsWithoutAccumulatingOldOnes() {
        val regions=AbyssReadability();regions.add(10f,10f,60f,60f)
        regions.clear();regions.add(200f,200f,260f,260f)
        assertEquals(1f,regions.visibility(20f,20f,40f,40f,20f),0f)
        assertEquals(.18f,regions.visibility(210f,210f,240f,240f,20f),.0001f)
    }
}
