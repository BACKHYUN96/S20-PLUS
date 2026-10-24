package app.purebit.theme

import app.purebit.theme.abyss.AbyssControlGeometry as G
import org.junit.Assert.*
import org.junit.Test

class AbyssControlGeometryTest {
    @Test fun onlyTransportPrimaryUsesOrbWhileOutputSettingsKeepWidePanel() {
        for(label in listOf("▶","Ⅱ","■"))assertEquals(G.Form.ORB,G.form(label,true))
        for(label in listOf("USB Direct 음악 모드","＋ 폴더","자체진단 실행","목록 재생"))assertEquals(G.Form.PANEL,G.form(label,true))
        assertEquals(G.Form.ICON,G.form("↻¹",false))
        assertEquals(G.Form.CAPSULE,G.form("A–B",false))
    }
    @Test fun playOrbStaysCircularInsideLandscapeAndLyricWeightedButtons() {
        for((w,h) in listOf(88f to 88f,180f to 56f,72f to 100f)) {
            val s=G.surface(w,h,1f,G.Form.ORB)
            assertEquals(s.width,s.height,.001f)
            assertTrue(s.left>=0 && s.top>=0 && s.right<=w && s.bottom<=h)
            assertEquals(w/2,(s.left+s.right)/2,.001f)
        }
    }
    @Test fun auxiliaryChromeDoesNotConsumeTheFullNativeTouchTarget() {
        for(density in listOf(1f,2.625f,3.5f)) {
            val ring=G.surface(48*density,48*density,density,G.Form.RING)
            assertEquals(28*density,ring.width,.001f)
            assertEquals(ring.width,ring.height,.001f)
            val capsule=G.surface(100*density,48*density,density,G.Form.CAPSULE)
            assertEquals(24*density,capsule.height,.001f)
            assertTrue(capsule.width>capsule.height)
        }
    }
    @Test fun narrowGroupsRetainTargetSpaceAndScaleDownPaddingForLargeText() {
        for(width in listOf(240f,288f,328f,380f,448f)) {
            val inset=G.groupInset(width,.52f,192f)
            val accessible=G.groupInset(width,.52f,192f*1.35f)
            assertTrue(inset>=0 && accessible>=0)
            assertTrue(width-2*inset>=minOf(width,192f))
            assertTrue(accessible<=inset)
        }
    }
    @Test fun zeroSizedOrTinySurfacesNeverInvert() {
        for(form in G.Form.entries)for(side in listOf(0f,1f,3f)) {
            val s=G.surface(side,side,3f,form)
            assertTrue(s.width>=0 && s.height>=0)
        }
    }
}
