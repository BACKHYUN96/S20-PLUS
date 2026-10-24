package app.purebit.theme.abyss

/** A primary action is not necessarily a circular button. */
object AbyssGlassGeometry {
    fun isOrb(width:Float,height:Float,wantsRound:Boolean)=wantsRound&&width>0&&height>0&&width/height in .8f..1.25f
}
