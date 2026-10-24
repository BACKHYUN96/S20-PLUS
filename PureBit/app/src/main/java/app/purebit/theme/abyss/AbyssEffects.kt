package app.purebit.theme.abyss

/** Independent UI intensity settings. These values cannot alter audio processing. */
data class AbyssEffects(val bubbles:Float=1f,val jellyfish:Float=1f,val light:Float=1f,val waves:Float=1f) {
    companion object {
        val keys=listOf("bubbles","jellyfish","light","waves")
        fun strength(level:Int)=floatArrayOf(0f,.55f,1f,1.5f)[level.coerceIn(0,3)]
        fun fromLevels(levels:List<Int>)=AbyssEffects(strength(levels.getOrElse(0){2}),strength(levels.getOrElse(1){2}),strength(levels.getOrElse(2){2}),strength(levels.getOrElse(3){2}))
        val calm=listOf(1,1,1,1)
        val luminous=listOf(3,2,3,3)
    }
}
