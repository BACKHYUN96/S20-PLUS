package app.purebit.theme

import app.purebit.theme.abyss.AbyssGeometry
import app.purebit.theme.abyss.AbyssLayout
import org.junit.Assert.*
import org.junit.Test

class AbyssCompositionTest {
    @Test fun defaultPhoneFitsTwoLineTitleAndAllControlsWithoutClippingCircularWindow(){
        for(screen in listOf(360f,384f,412f))for(viewport in listOf(640f,700f,780f))for(controls in listOf(300f,340f)){
            val content=screen-32f
            val height=AbyssLayout.heroHeight(viewport,controls,content).toFloat()
            val portal=AbyssGeometry.portal(content,height)
            assertTrue("screen=$screen viewport=$viewport controls=$controls",height+controls<=viewport)
            assertTrue(portal.radius*2/screen in .64f.. .85f)
            assertTrue(portal.x-portal.radius*1.035f>=0f)
            assertTrue(portal.x+portal.radius*1.035f<=content)
            assertTrue(portal.y-portal.radius*1.035f>=0f)
        }
    }
    @Test fun expandedLoopOrLargeFontUsesScrollInsteadOfShrinkingWindow(){
        val width=380f
        val normal=AbyssLayout.heroHeight(700f,370f,width)
        val expanded=AbyssLayout.heroHeight(700f,570f,width,expanded=true)
        val largeFont=AbyssLayout.heroHeight(700f,570f,width,fontScale=1.3f)
        assertTrue(expanded>normal)
        assertEquals(expanded,largeFont)
        assertTrue(expanded+570>700)
        assertTrue(expanded>=width*.97f)
    }
    @Test fun previouslyOverflowingS26BudgetUsesAvailableHeightAndShortScreensStillScroll(){
        val height=AbyssLayout.heroHeight(640f,320f,380f)
        assertTrue(height+320<=640)
        assertTrue(380f*.98f+320>640) // Previous width-led minimum hid the tools row.
        val short=AbyssLayout.heroHeight(420f,340f,380f)
        assertTrue(short>=240)
        assertTrue(short+340>420)
    }
    @Test fun enlargedWindowAndSatelliteKeepVisibleAlbumHitAreaInsideHero(){
        for(width in listOf(328f,380f,448f)){
            val height=AbyssLayout.heroHeight(700f,370f,width).toFloat()
            val portal=AbyssGeometry.portal(width,height)
            val album=AbyssGeometry.album(portal)
            assertTrue(album.x+album.radius<=width)
            assertTrue(album.y+album.radius<=height)
            assertTrue(AbyssGeometry.albumHit(album.x,album.y,portal))
            assertFalse(AbyssGeometry.albumHit(portal.x,portal.y,portal))
        }
    }
    @Test fun seekTimeCombinationHonorsCustomBlockOrder(){
        assertTrue(AbyssLayout.combineSeekTime(BuiltInThemes.abyss.layout.blocks))
        assertFalse(AbyssLayout.combineSeekTime(PlayerBlock.entries.reversed()))
        assertFalse(AbyssLayout.combineSeekTime(listOf(PlayerBlock.TIME,PlayerBlock.SEEK)))
        assertFalse(AbyssLayout.combineSeekTime(emptyList()))
    }
}
