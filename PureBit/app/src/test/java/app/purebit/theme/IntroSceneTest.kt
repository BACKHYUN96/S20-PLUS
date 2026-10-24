package app.purebit.theme

import org.junit.Assert.*
import org.junit.Test

class IntroSceneTest {
    @Test fun missingOrFuturePreferenceFallsBackAndBuiltInScenesStayBounded(){
        assertEquals(IntroScene.THEME,IntroScene.decode("future"));assertEquals(IntroScene.THEME,IntroScene.decode(null))
        for(scene in IntroScene.entries){
            assertEquals(scene,IntroScene.decode(scene.name))
            assertTrue(IntroTimeline.duration(BuiltInThemes.hwahyun,IntroMode.FULL,scene) in 8000L..10000L)
            assertEquals(1800L,IntroTimeline.duration(BuiltInThemes.hwahyun,IntroMode.SHORT,scene))
            assertEquals(0L,IntroTimeline.duration(BuiltInThemes.hwahyun,IntroMode.OFF,scene))
            assertEquals(3200L,IntroTimeline.duration(BuiltInThemes.original,IntroMode.FULL,scene))
        }
    }
}
