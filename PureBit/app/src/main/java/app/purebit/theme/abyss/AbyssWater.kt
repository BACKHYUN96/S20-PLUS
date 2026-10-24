package app.purebit.theme.abyss

import android.graphics.*
import android.os.Build
import android.util.Log

/** Small optical animation of scenic pixels only; native controls and album are never sampled. */
class AbyssWater(private val bitmap:Bitmap) {
    companion object {
        @Volatile var status="Not initialized";private set
    }
    private val fallback=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private var backend:Water33?=if(Build.VERSION.SDK_INT>=33)runCatching {Water33(bitmap).also {status="AGSL ready; awaiting hardware draw"}}.onFailure {
        status="Canvas fallback: ${it.javaClass.simpleName}";Log.w("BitwireTheme","ABYSS water shader unavailable; keeping Canvas",it)
    }.getOrNull()else null
    init {if(Build.VERSION.SDK_INT<33)status="Canvas fallback: API < 33"}
    fun draw(c:Canvas,src:Rect?,dst:RectF,time:Float,alpha:Int,strength:Float) {
        if(dst.width()<=0||dst.height()<=0)return
        if(Build.VERSION.SDK_INT>=33&&c.isHardwareAccelerated&&backend!=null){
            val ok=runCatching {backend!!.draw(c,src,dst,time,alpha,strength)}.onFailure {
                backend=null;status="Canvas fallback: ${it.javaClass.simpleName}";Log.w("BitwireTheme","ABYSS water draw fallback",it)
            }.isSuccess
            if(ok){status="AGSL scenic water active (visual only)";return}
        }
        if(backend!=null&&!c.isHardwareAccelerated)status="Canvas fallback: software canvas"
        fallback.alpha=alpha.coerceIn(0,255);c.drawBitmap(bitmap,src,dst,fallback)
    }
    private class Water33(val bitmap:Bitmap) {
        private val image=BitmapShader(bitmap,Shader.TileMode.CLAMP,Shader.TileMode.CLAMP).apply {
            if(Build.VERSION.SDK_INT>=33)setFilterMode(BitmapShader.FILTER_MODE_LINEAR)
        }
        private val matrix=Matrix()
        private val shader=if(Build.VERSION.SDK_INT>=33)RuntimeShader("""
            uniform shader scenery;
            uniform float2 resolution;
            uniform float clock;
            uniform float strength;
            half4 main(float2 p) {
                float2 uv=p/resolution;
                float a=sin(uv.y*17.0+uv.x*5.0-clock*0.32);
                float b=sin(uv.x*23.0-uv.y*8.0+clock*0.21);
                float surface=1.0-smoothstep(0.02,0.22,uv.y);
                float2 flow=float2(a+0.3*b,b-0.25*a)*resolution.x*0.00065*strength;
                half4 base=scenery.eval(p+flow);
                // Restrict moving illumination to surface and textured side walls.
                // The open-water center must never become a full-screen luminous net.
                half3 across=scenery.eval(p+flow+float2(2.5,0.0)).rgb;
                half3 below=scenery.eval(p+flow+float2(0.0,2.5)).rgb;
                float textureEdge=clamp(float(length(across-base.rgb)+length(below-base.rgb))*6.0,0.0,1.0);
                float walls=smoothstep(0.12,0.38,abs(uv.x-0.5))*(1.0-smoothstep(0.55,0.95,uv.y));
                float shimmer=0.55+0.45*sin(uv.x*31.0+uv.y*13.0+a*0.8+clock*0.18)*sin(uv.x*14.0-uv.y*21.0+b*0.6-clock*0.13);
                float mask=max(surface*0.7,walls*textureEdge);
                half3 tint=half3(0.04,0.18,0.23)*half(shimmer*mask*0.045*strength);
                return half4(base.rgb+tint*base.a,base.a);
            }
        """.trimIndent())else null
        private val paint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {this.shader=this@Water33.shader}
        fun draw(c:Canvas,src:Rect?,dst:RectF,time:Float,alpha:Int,strength:Float) {
            if(Build.VERSION.SDK_INT<33)return
            val runtime=shader?:return
            val sw=src?.width()?.toFloat()?:bitmap.width.toFloat()
            val sh=src?.height()?.toFloat()?:bitmap.height.toFloat()
            val sx=dst.width()/sw;val sy=dst.height()/sh
            matrix.setScale(sx,sy);matrix.postTranslate(-(src?.left?:0)*sx,-(src?.top?:0)*sy);image.setLocalMatrix(matrix)
            // RuntimeShader captures the child's current native instance, including its matrix.
            runtime.setInputShader("scenery",image)
            runtime.setFloatUniform("resolution",dst.width(),dst.height())
            runtime.setFloatUniform("clock",time);runtime.setFloatUniform("strength",strength.coerceIn(0f,1.5f))
            paint.alpha=alpha.coerceIn(0,255)
            val save=c.save()
            try {c.translate(dst.left,dst.top);c.drawRect(0f,0f,dst.width(),dst.height(),paint)}finally {c.restoreToCount(save)}
        }
    }
}
