package app.purebit.theme

import app.purebit.theme.abyss.AbyssEffects as F
import org.junit.Assert.*
import org.junit.Test

class AbyssEffectsTest {
    @Test fun defaultsPreserveExistingStrength(){assertEquals(F(),F.fromLevels(emptyList()))}
    @Test fun allEffectsCanBeDisabledIndependently(){
        val f=F.fromLevels(listOf(0,1,2,3))
        assertEquals(0f,f.bubbles,0f);assertEquals(.55f,f.jellyfish,0f);assertEquals(1f,f.light,0f);assertEquals(1.5f,f.waves,0f)
    }
    @Test fun corruptLevelsAreBounded(){assertEquals(0f,F.strength(-999),0f);assertEquals(1.5f,F.strength(999),0f)}
    @Test fun presetsCoverEveryEffectAndCalmIsLower(){
        assertEquals(F.keys.size,F.calm.size);assertEquals(F.keys.size,F.luminous.size)
        assertTrue(F.calm.zip(F.luminous).all {(a,b)->a<b})
    }
}
