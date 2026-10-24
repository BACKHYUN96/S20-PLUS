package app.purebit.theme.abyss

/** Holds at most two borrowed covers. Never owns or recycles library bitmaps. */
class CoverFade<T> {
    var current:T?=null;private set
    var previous:T?=null;private set
    private var started=-10f
    private var changing=false
    fun submit(value:T?,time:Float,animate:Boolean) {
        if(value===current){if(!animate)settle();return}
        val dominant=if(changing&&progress(time)<.5f)previous else current
        previous=if(animate)dominant else null
        current=value;started=time;changing=animate
    }
    fun progress(time:Float):Float {
        if(!changing)return 1f
        val q=((time-started)/.75f).coerceIn(0f,1f)
        if(q>=1f)settle()
        return AbyssGeometry.smooth(q)
    }
    fun settle(){previous=null;changing=false}
}
