package app.purebit.ui

/** Monotonic foreground listening time, independent of music bytes, wall clock and track changes. */
enum class NightMode(val label:String) { AUTO("서서히 깊어지는 밤 · 12분"), WARM("고정 · 따뜻한 달밤"), DUSK("고정 · 남보라 달밤"), DEEP("고정 · 깊은 밤") }
class NightProgress {
    var mode=NightMode.AUTO
    var listenedSeconds=0f;private set
    private var previewRemaining=0f
    val strength:Float get() {
        if(previewRemaining>0f)return 1f
        return when(mode){
            NightMode.WARM->0f;NightMode.DUSK->.55f;NightMode.DEEP->1f
            NightMode.AUTO->{val x=(listenedSeconds/720f).coerceIn(0f,1f);x*x*(3-2*x)}
        }
    }
    fun advance(seconds:Float,listening:Boolean,motion:Boolean){
        val dt=if(seconds.isFinite())seconds.coerceIn(0f,1f)else 0f
        previewRemaining=(previewRemaining-dt).coerceAtLeast(0f)
        if(mode==NightMode.AUTO&&listening&&motion)listenedSeconds=(listenedSeconds+dt).coerceAtMost(720f)
    }
    fun preview(){previewRemaining=10f}
}
object GardenNight {val state=NightProgress()}
