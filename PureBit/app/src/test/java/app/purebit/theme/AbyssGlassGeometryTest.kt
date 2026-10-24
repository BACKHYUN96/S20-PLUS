package app.purebit.theme

import app.purebit.theme.abyss.AbyssGlassGeometry as G
import org.junit.Assert.*
import org.junit.Test

class AbyssGlassGeometryTest {
    @Test fun widePrimaryActionsNeverReceiveOrbReflection(){assertFalse(G.isOrb(360f,48f,true));assertFalse(G.isOrb(600f,52f,true))}
    @Test fun playAndVolumeControlsRetainOrbReflection(){assertTrue(G.isOrb(90f,86f,true));assertTrue(G.isOrb(48f,48f,true))}
    @Test fun panelsAndEmptyBoundsAreNotOrbs(){assertFalse(G.isOrb(100f,100f,false));assertFalse(G.isOrb(0f,0f,true));assertFalse(G.isOrb(48f,0f,true))}
}
