package app.purebit.theme

import app.purebit.theme.packs.HwahyunPack
import app.purebit.theme.packs.OriginalPack
import app.purebit.theme.packs.AbyssPack

/** Bundled, trusted visual modules. External files contain data, never executable code. */
interface ThemePack {
    fun build(host: ThemePlayerLayout)
}

object ThemePacks {
    private val packs = mapOf(
        ThemeRenderer.ORIGINAL to OriginalPack,
        ThemeRenderer.HWAHYUN to HwahyunPack,
        ThemeRenderer.ABYSS to AbyssPack
    )
    fun forRenderer(renderer: ThemeRenderer): ThemePack = requireNotNull(packs[renderer])
}
