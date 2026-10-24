package app.purebit.theme

import org.junit.Assert.*
import org.junit.Test

class PlayerLayoutSpecTest {
    private fun custom()=BuiltInThemes.hwahyun.copy(id="layout-test",name="배치 테스트")
    @Test fun legacyFilesKeepTheirOriginalLayout() {
        for(base in BuiltInThemes.all) {
            val old="schema=1\nid=legacy-${base.id}\nname=Legacy\nrenderer=${base.renderer.name.lowercase()}\n"
            assertEquals(base.layout,ThemeCodec.decode(old).layout)
        }
    }
    @Test fun customOrderAndDimensionsSurviveSaveReload() {
        val theme=custom().copy(layout=custom().layout.copy(blocks=PlayerBlock.entries.reversed(),artworkHeight=240,horizontalPadding=12,titleSize=28,playSize=96))
        assertEquals(theme,ThemeCodec.decode(ThemeCodec.encode(theme)))
    }
    @Test fun controlsCannotBeDuplicatedOrDropped() {
        val base=custom()
        for(blocks in listOf(base.layout.blocks.drop(1),base.layout.blocks+PlayerBlock.TRANSPORT,base.layout.blocks.map {if(it==PlayerBlock.SEEK)PlayerBlock.TIME else it})) {
            assertThrows(IllegalArgumentException::class.java) {ThemeCodec.decode(ThemeCodec.encode(base.copy(layout=base.layout.copy(blocks=blocks))))}
        }
    }
    @Test fun boundedSizesAndUnsupportedBlocksAreRejected() {
        val base=custom()
        for(layout in listOf(base.layout.copy(artworkHeight=-1),base.layout.copy(artworkHeight=999999),base.layout.copy(playSize=20),base.layout.copy(horizontalPadding=100),base.layout.copy(titleSize=1))) {
            assertThrows(IllegalArgumentException::class.java) {ThemeCodec.decode(ThemeCodec.encode(base.copy(layout=layout)))}
        }
        assertThrows(IllegalArgumentException::class.java) {ThemeCodec.decode(ThemeCodec.encode(base).replace("artwork,","execute,"))}
    }
    @Test fun oldSchemaCannotSmuggleLayoutKeys() {
        assertThrows(IllegalArgumentException::class.java) {ThemeCodec.decode(ThemeCodec.encode(custom()).replace("schema=2","schema=1"))}
    }
    @Test fun suppliedThemePackIsUsableAndRoundTrips() {
        val input=requireNotNull(javaClass.getResourceAsStream("/themes/hwahyun-studio.bitwire-theme"))
        val theme=input.use {ThemeCodec.decode(it.readBytes().toString(Charsets.UTF_8))}
        assertEquals(PlayerBlock.METADATA,theme.layout.blocks.first())
        assertEquals(260,theme.layout.artworkHeight)
        assertEquals(theme,ThemeCodec.decode(ThemeCodec.encode(theme)))
    }
}
