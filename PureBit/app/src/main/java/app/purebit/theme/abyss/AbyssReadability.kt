package app.purebit.theme.abyss

import kotlin.math.max
import kotlin.math.sqrt

/** A small borrowed set of actual UI rectangles. No pixel capture, layers or per-frame objects. */
class AbyssReadability {
    private val regions=FloatArray(16*4)
    private var size=0
    fun clear(){size=0}
    fun add(left:Float,top:Float,right:Float,bottom:Float) {
        if(size>=16||right<=left||bottom<=top)return
        val at=size++*4;regions[at]=left;regions[at+1]=top;regions[at+2]=right;regions[at+3]=bottom
    }
    /** Opacity fades before touching text and restores smoothly after passing it. */
    fun visibility(left:Float,top:Float,right:Float,bottom:Float,softness:Float):Float {
        var nearest=Float.POSITIVE_INFINITY
        for(i in 0 until size) {
            val at=i*4
            val dx=max(0f,max(regions[at]-right,left-regions[at+2]))
            val dy=max(0f,max(regions[at+1]-bottom,top-regions[at+3]))
            nearest=minOf(nearest,dx*dx+dy*dy)
        }
        if(nearest==Float.POSITIVE_INFINITY)return 1f
        val q=(sqrt(nearest)/softness.coerceAtLeast(1f)).coerceIn(0f,1f)
        return .18f+.82f*q*q*(3-2*q)
    }
}
