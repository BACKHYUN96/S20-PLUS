package app.purebit.ui

import kotlin.math.*

/** Decorative color selection only. Samples never enter the audio pipeline. */
object AlbumMoonColor {
    fun select(pixels:IntArray,base:Int):Int {
        val weights=DoubleArray(12);val red=DoubleArray(12);val green=DoubleArray(12);val blue=DoubleArray(12)
        for(pixel in pixels){
            val a=(pixel ushr 24)/255.0;if(a<.5)continue
            val r=(pixel ushr 16 and 255)/255.0;val g=(pixel ushr 8 and 255)/255.0;val b=(pixel and 255)/255.0
            val hi=max(r,max(g,b));val lo=min(r,min(g,b));val chroma=hi-lo
            // Black/white/gray covers keep the theme's pink moonlight.
            if(hi<.12||chroma<.12||chroma/hi<.22)continue
            val hue=when(hi){r->((g-b)/chroma+6)%6;g->(b-r)/chroma+2;else->(r-g)/chroma+4}
            val bin=(hue*2).toInt().coerceIn(0,11);val weight=chroma*a
            weights[bin]+=weight;red[bin]+=r*weight;green[bin]+=g*weight;blue[bin]+=b*weight
        }
        val bin=weights.indices.maxByOrNull {weights[it]}?:return base
        val total=weights[bin];if(total==0.0)return base
        fun channel(shift:Int,value:Double)=(((base ushr shift and 255)*.72+value/total*255*.28).roundToInt()).coerceIn(0,255)
        return (0xff shl 24) or (channel(16,red[bin]) shl 16) or (channel(8,green[bin]) shl 8) or channel(0,blue[bin])
    }
}

/** Bounded visual envelope; no playback commands or software audio volume. */
class GardenPresence {
    var value=0f;private set
    fun update(playing:Boolean,dt:Float,motion:Boolean=true):Float {
        if(!motion){value=0f;return value}
        val target=if(playing)1f else 0f
        value+=(target-value)*(1-exp(-dt.coerceIn(0f,.1f)*if(playing)3.5f else 2f))
        return value
    }
}
