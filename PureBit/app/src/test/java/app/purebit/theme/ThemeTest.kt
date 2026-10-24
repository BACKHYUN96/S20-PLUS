package app.purebit.theme

import org.junit.Assert.*
import org.junit.Test

class ThemeTest {
    private fun custom()=BuiltInThemes.hwahyun.copy(id="moonlit",name="달빛 화현")
    @Test fun builtinsAreAlwaysDistinctAndBothRenderersRoundTrip(){
        assertEquals(3,BuiltInThemes.all.size)
        BuiltInThemes.all.forEach { assertEquals(it,ThemeCodec.decode(ThemeCodec.encode(it),false)) }
        assertEquals(custom(),ThemeCodec.decode(ThemeCodec.encode(custom())))
        assertEquals(ThemeRenderer.ORIGINAL,BuiltInThemes.original.renderer)
    }
    @Test fun propertiesSpecialCharactersAndKoreanNamesRoundTrip(){
        val special=custom().copy(name="화현 = 달빛 : \\ 꽃")
        assertEquals(special,ThemeCodec.decode(ThemeCodec.encode(special)))
    }
    @Test fun importedFilesCannotOverwriteBuiltinsOrSelectUnknownEngines(){
        BuiltInThemes.all.forEach { assertThrows(IllegalArgumentException::class.java){ThemeCodec.decode(ThemeCodec.encode(it))} }
        val good=ThemeCodec.encode(custom())
        assertThrows(IllegalStateException::class.java){ThemeCodec.decode(good.replace("renderer=hwahyun","renderer=downloaded-code"))}
        assertThrows(IllegalArgumentException::class.java){ThemeCodec.decode(good+"\nscript=payload\n")}
        assertThrows(IllegalArgumentException::class.java){ThemeCodec.decode(good.replace("schema=2","schema=999"))}
    }
    @Test fun boundedNamesPathsPayloadsAndParticleValuesAreRejected(){
        val good=ThemeCodec.encode(custom())
        for(t in listOf(custom().copy(id="../../original"),custom().copy(name=""),custom().copy(name="x\nrenderer=original"),
            custom().copy(petals=97),custom().copy(petalSpeed=Float.NaN),custom().copy(petalSpeed=Float.POSITIVE_INFINITY),custom().copy(petalSpeed=0f),custom().copy(introMillis=60000))) {
            assertThrows(IllegalArgumentException::class.java){ThemeCodec.decode(ThemeCodec.encode(t))}
        }
        assertThrows(IllegalArgumentException::class.java){ThemeCodec.decode(good+"#".repeat(ThemeCodec.MAX_BYTES))}
    }
    @Test fun darkThemeContrastAndOpaqueColorsAreEnforced(){
        for(t in listOf(custom().copy(text=0xff201927.toInt()),custom().copy(background=-1),custom().copy(accent=0xff201927.toInt()))){
            assertThrows(IllegalArgumentException::class.java){ThemeCodec.decode(ThemeCodec.encode(t))}
        }
        assertEquals(0xfff1afc5.toInt(),ThemeCodec.parseColor("#F1AfC5"))
        for(bad in listOf("#00ffffff","red","#FFF","../image"))assertThrows(IllegalArgumentException::class.java){ThemeCodec.parseColor(bad)}
    }
    @Test fun introTimingHasLongFullShortOffAndSmoothOrderedStages(){
        assertEquals(8000L,IntroTimeline.duration(BuiltInThemes.hwahyun,IntroMode.FULL))
        assertEquals(1800L,IntroTimeline.duration(BuiltInThemes.hwahyun,IntroMode.SHORT))
        assertEquals(0L,IntroTimeline.duration(BuiltInThemes.original,IntroMode.OFF))
        val values=(0..100).map { IntroTimeline.stage(it/100f,.66f,.86f) }
        assertEquals(0f,values.first(),0f);assertEquals(1f,values.last(),0f)
        assertTrue(values.zipWithNext().all { (a,b)->b>=a })
    }
    @Test fun downloadableMoonlitExampleImportsWithoutAppUpdate(){
        val text=requireNotNull(javaClass.getResourceAsStream("/themes/hwahyun-moonlit.bitwire-theme")).use {it.readBytes().toString(Charsets.UTF_8)}
        val theme=ThemeCodec.decode(text)
        assertEquals("hwahyun-moonlit",theme.id);assertEquals("화현 · 월백",theme.name)
        assertEquals(9000L,theme.introMillis);assertEquals(60,theme.petals)
        assertEquals(theme,ThemeCodec.decode(ThemeCodec.encode(theme)))
    }
}
