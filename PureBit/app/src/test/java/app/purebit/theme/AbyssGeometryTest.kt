package app.purebit.theme

import app.purebit.theme.abyss.AbyssGeometry
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.hypot

class AbyssGeometryTest {
    @Test fun onlyVisibleCircularAlbumAreaRespondsToTouch() {
        val portal=AbyssGeometry.portal(400f,360f)
        val album=AbyssGeometry.album(portal)
        assertTrue(AbyssGeometry.albumHit(album.x,album.y,portal))
        assertFalse(AbyssGeometry.albumHit(album.x-album.radius*.8f,album.y-album.radius*.5f,portal))
        assertFalse(AbyssGeometry.albumHit(album.x+album.radius,album.y+album.radius,portal))
        assertFalse(AbyssGeometry.albumHit(portal.x,portal.y,portal))
    }
    @Test fun satelliteOverlapsButDoesNotCoverWindowCenter() {
        for((w,h) in listOf(360f to 260f,412f to 390f,900f to 380f)) {
            val portal=AbyssGeometry.portal(w,h,w>h*2)
            val album=AbyssGeometry.album(portal)
            val distance=hypot(album.x-portal.x,album.y-portal.y)
            assertTrue(distance<portal.radius+album.radius)
            assertTrue(distance>portal.radius)
            assertTrue(distance-album.radius>portal.radius*.7f)
            assertTrue(album.x+album.radius<=w)
            assertTrue(album.y+album.radius<=h)
        }
    }
    @Test fun morphHasCircularAndRoundedRectangleEndpoints() {
        assertEquals(100f,AbyssGeometry.corner(200f,0f),.001f)
        assertEquals(9f,AbyssGeometry.corner(200f,1f),.001f)
        val corners=(0..100).map {AbyssGeometry.corner(200f,it/100f)}
        assertTrue(corners.zipWithNext().all {(a,b)->b<=a})
        assertEquals(0f,AbyssGeometry.smooth(-1f),0f)
        assertEquals(1f,AbyssGeometry.smooth(2f),0f)
    }
    @Test fun abyssPresetAndLongIntroRoundTrip() {
        val theme=BuiltInThemes.abyss.copy(id="abyss-custom")
        assertEquals(theme,ThemeCodec.decode(ThemeCodec.encode(theme)))
        assertEquals(11000L,IntroTimeline.duration(theme,IntroMode.FULL))
        assertEquals(1800L,IntroTimeline.duration(theme,IntroMode.SHORT))
        assertEquals(0L,IntroTimeline.duration(theme,IntroMode.OFF))
    }
}
