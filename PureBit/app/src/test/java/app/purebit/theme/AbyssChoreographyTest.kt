package app.purebit.theme

import app.purebit.theme.abyss.AbyssGeometry as G
import org.junit.Assert.*
import org.junit.Test

class AbyssChoreographyTest {
    @Test fun landingEndsAtActualDeckCoordinates() {
        val destination=G.portal(380f,390f)
        val landed=G.landing(412f,900f,16f,94f,380f,390f,1f)
        assertEquals(destination.x+16,landed.x,.001f)
        assertEquals(destination.y+94,landed.y,.001f)
        assertEquals(destination.radius,landed.radius,.001f)
        assertEquals(landed,G.landing(412f,900f,16f,94f,380f,390f,2f))
    }
    @Test fun travelSettlesBeforeUncroppingCompletes() {
        assertEquals(0f,G.albumUncrop(.1f),0f)
        assertTrue(G.albumTravel(.4f)>G.albumUncrop(.4f))
        assertEquals(1f,G.albumTravel(.9f),0f)
        assertTrue(G.albumUncrop(.9f)<1f)
        for(curve in listOf<(Float)->Float>(G::albumTravel,G::albumUncrop)){
            val values=(0..100).map {curve(it/100f)}
            assertEquals(0f,values.first(),0f);assertEquals(1f,values.last(),0f)
            assertTrue(values.zipWithNext().all {(a,b)->b>=a})
        }
    }
    @Test fun bubblesFadeAtBothEndpoints() {
        assertEquals(0f,G.bubbleEnvelope(0f),0f)
        assertEquals(0f,G.bubbleEnvelope(1f),0f)
        assertEquals(1f,G.bubbleEnvelope(.5f),0f)
    }
}
