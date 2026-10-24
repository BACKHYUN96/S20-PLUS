package app.purebit.theme.abyss

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.Log
import app.purebit.theme.ThemeSpec
import app.purebit.theme.abyss.AbyssControlGeometry.Form

/** Reference-specific chrome. The play orb has its own material, never a stretched panel. */
class AbyssControlSurface(context: Context, private val theme: ThemeSpec, private val form: Form): Drawable() {
    companion object {
        private var loaded=false
        private var texture: Bitmap?=null
        @Synchronized private fun orb(context: Context): Bitmap? {
            if(!loaded) {
                loaded=true
                texture=runCatching {
                    context.applicationContext.assets.open("abyss/play-orb.png").use {
                        BitmapFactory.decodeStream(it,null,BitmapFactory.Options().apply {inSampleSize=2})
                    }
                }.onFailure {Log.w("BitwireTheme","ABYSS play material unavailable; vector fallback",it)}.getOrNull()
            }
            return texture
        }
    }
    private val d=context.resources.displayMetrics.density
    private val bitmap=if(form==Form.ORB)orb(context)else null
    private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val rect=RectF()
    private val imageRect=RectF()
    private var halo: Shader?=null
    private var body: Shader?=null
    private var rim: Shader?=null
    private var opticalVeil: Shader?=null
    private var pressed=false
    private var focused=false
    private var enabled=true
    private var opacity=255
    override fun onBoundsChange(b: Rect) {
        val s=AbyssControlGeometry.surface(b.width().toFloat(),b.height().toFloat(),d,form)
        rect.set(b.left+s.left,b.top+s.top,b.left+s.right,b.top+s.bottom)
        if(rect.width()<=0 || rect.height()<=0)return
        val radius=minOf(rect.width(),rect.height())/2
        halo=RadialGradient(rect.centerX(),rect.centerY(),radius*1.13f,intArrayOf(0x004ad9ef,0x004ad9ef,0x344ad9ef,0x004ad9ef),floatArrayOf(0f,.75f,.88f,1f),Shader.TileMode.CLAMP)
        body=RadialGradient(rect.centerX()-radius*.42f,rect.centerY()-radius*.5f,radius*1.9f,intArrayOf(0xb06ab9cc.toInt(),0xc008253d.toInt(),0xe0011329.toInt(),0xc0449eb6.toInt()),floatArrayOf(0f,.3f,.7f,1f),Shader.TileMode.CLAMP)
        opticalVeil=RadialGradient(rect.centerX(),rect.centerY(),radius,intArrayOf(0x08051a2d,0x00051a2d,0x18051a2d),floatArrayOf(0f,.65f,1f),Shader.TileMode.CLAMP)
        rim=LinearGradient(rect.left,rect.top,rect.right,rect.bottom,intArrayOf(0xffe4ffff.toInt(),theme.accent,0x7065a2ba,0xffd9faff.toInt()),floatArrayOf(0f,.32f,.67f,1f),Shader.TileMode.CLAMP)
        // Asset's optical edge is at 88% of its canvas, so it aligns with the vector fallback.
        imageRect.set(rect.centerX()-radius/.88f,rect.centerY()-radius/.88f,rect.centerX()+radius/.88f,rect.centerY()+radius/.88f)
    }
    override fun isStateful()=true
    override fun onStateChange(state: IntArray): Boolean {
        val down=android.R.attr.state_pressed in state
        val focus=android.R.attr.state_focused in state
        val available=android.R.attr.state_enabled in state
        if(down==pressed && focus==focused && available==enabled)return false
        pressed=down;focused=focus;enabled=available;invalidateSelf();return true
    }
    override fun draw(c: Canvas) {
        if(rect.isEmpty)return
        val alpha=opacity*(if(enabled)1f else .42f)
        val cx=rect.centerX();val cy=rect.centerY();val r=minOf(rect.width(),rect.height())/2
        c.save()
        if(pressed)c.scale(.965f,.965f,cx,cy)
        p.shader=null;p.style=Paint.Style.FILL
        when(form) {
            Form.ORB -> {
                p.shader=halo;p.alpha=alpha.toInt();c.drawCircle(cx,cy,r*1.13f,p);p.shader=null
                if(bitmap!=null) {
                    p.alpha=(alpha*.96f).toInt();c.drawBitmap(bitmap,null,imageRect,p)
                    p.shader=opticalVeil;p.alpha=alpha.toInt();c.drawCircle(cx,cy,r,p);p.shader=null
                }
                else {
                    p.shader=body;p.alpha=alpha.toInt();c.drawCircle(cx,cy,r,p);p.shader=null
                    p.style=Paint.Style.STROKE;p.shader=rim;p.strokeWidth=1.4f*d;c.drawCircle(cx,cy,r,p);p.shader=null
                    p.color=Color.WHITE;p.alpha=(alpha*.75f).toInt();p.strokeWidth=d
                    c.drawArc(rect,203f,92f,false,p)
                }
                // Fine inner refraction edge remains crisp even at low texture sampling sizes.
                p.style=Paint.Style.STROKE;p.shader=rim;p.alpha=(alpha*.45f).toInt();p.strokeWidth=.55f*d;c.drawCircle(cx,cy,r*.93f,p);p.shader=null
            }
            Form.RING,Form.CAPSULE -> {
                p.color=0xff031b2b.toInt();p.alpha=(alpha*.28f).toInt();c.drawRoundRect(rect,r,r,p)
                p.style=Paint.Style.STROKE;p.shader=rim;p.alpha=(alpha*.65f).toInt();p.strokeWidth=.65f*d;c.drawRoundRect(rect,r,r,p);p.shader=null
            }
            Form.PANEL -> {
                p.color=0xff051b2b.toInt();p.alpha=(alpha*.74f).toInt();c.drawRoundRect(rect,12*d,12*d,p)
                p.style=Paint.Style.STROKE;p.color=theme.accent;p.alpha=(alpha*.28f).toInt();p.strokeWidth=.6f*d;c.drawRoundRect(rect,12*d,12*d,p)
            }
            else -> Unit
        }
        if(pressed || focused) {
            p.style=Paint.Style.STROKE;p.shader=null;p.color=if(focused)Color.WHITE else theme.accent
            p.alpha=(alpha*.85f).toInt();p.strokeWidth=if(focused)1.5f*d else d
            if(form==Form.ORB)c.drawCircle(cx,cy,r,p)
            else c.drawRoundRect(rect,if(form==Form.RING||form==Form.CAPSULE)r else 8*d,if(form==Form.RING||form==Form.CAPSULE)r else 8*d,p)
        }
        p.shader=null;c.restore()
    }
    override fun setAlpha(alpha: Int) {opacity=alpha.coerceIn(0,255);invalidateSelf()}
    override fun setColorFilter(filter: ColorFilter?) {p.colorFilter=filter;invalidateSelf()}
    @Deprecated("Drawable opacity is unused") override fun getOpacity()=PixelFormat.TRANSLUCENT
}
