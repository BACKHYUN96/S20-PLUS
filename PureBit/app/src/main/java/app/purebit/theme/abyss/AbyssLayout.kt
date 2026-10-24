package app.purebit.theme.abyss

import app.purebit.theme.PlayerBlock

/** Fit default controls first; expanded tools, large fonts and genuinely short screens scroll. */
object AbyssLayout {
    @Volatile var lastReport="ABYSS player layout not measured in this process"
        internal set
    fun heroHeight(viewport:Float,controls:Float,contentWidth:Float=0f,fontScale:Float=1f,expanded:Boolean=false):Int {
        val preserveScene=expanded||fontScale>1.15f
        val ratio=if(preserveScene).98f else .78f
        val scenicMinimum=(contentWidth*ratio).coerceIn(240f,520f)
        return (viewport-controls).coerceIn(scenicMinimum,520f).toInt()
    }
    fun combineSeekTime(blocks:List<PlayerBlock>):Boolean {
        val seek=blocks.indexOf(PlayerBlock.SEEK)
        return seek>=0&&blocks.indexOf(PlayerBlock.TIME)==seek+1
    }
}
