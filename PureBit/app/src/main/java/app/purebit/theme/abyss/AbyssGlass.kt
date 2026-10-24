package app.purebit.theme.abyss

import android.graphics.*
import android.graphics.drawable.Drawable
import app.purebit.theme.ThemeSpec
import kotlin.math.min

/** Lightweight glass chrome. Native Buttons retain focus, click and accessibility behavior. */
class AbyssGlass(private val theme:ThemeSpec,private val d:Float,private val circular:Boolean=false,private val ghost:Boolean=false,private val outline:Boolean=false):Drawable() {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG);private val rect=RectF();private var pressed=false
    private var opacity=255
    private var glass:Shader?=null
    private var sphere:Shader?=null
    private fun rebuild(){val b=bounds;glass=LinearGradient(b.left.toFloat(),b.top.toFloat(),b.right.toFloat(),b.bottom.toFloat(),intArrayOf(if(pressed)0xb0427588.toInt()else 0x88406576.toInt(),0x30051a2a,0x880c2738.toInt()),null,Shader.TileMode.CLAMP)
        if(b.width()>0&&b.height()>0)sphere=RadialGradient(b.exactCenterX()-b.width()*.19f,b.exactCenterY()-b.height()*.25f,min(b.width(),b.height())*.80f,intArrayOf(0xa592e6f5.toInt(),0x50336880,0x90041725.toInt(),0xa05ab9cb.toInt()),floatArrayOf(0f,.22f,.72f,1f),Shader.TileMode.CLAMP)
    }
    override fun onBoundsChange(bounds:Rect){super.onBoundsChange(bounds);rebuild()}
    override fun onStateChange(state:IntArray):Boolean {val value=android.R.attr.state_pressed in state||android.R.attr.state_focused in state;if(value==pressed)return false;pressed=value;rebuild();invalidateSelf();return true}
    override fun isStateful()=true
    override fun draw(c:Canvas) {
        if(ghost&&!pressed)return
        rect.set(bounds);rect.inset(d,d)
        if(rect.width()<=0||rect.height()<=0)return
        val orb=AbyssGlassGeometry.isOrb(rect.width(),rect.height(),circular)
        val r=if(circular)min(rect.width(),rect.height())*.5f else 14*d
        p.style=Paint.Style.FILL;p.alpha=opacity
        p.shader=if(orb&&!outline)sphere else glass
        if(!outline)c.drawRoundRect(rect,r,r,p);p.shader=null
        p.color=theme.accent;p.alpha=(if(pressed)240 else if(orb&&!outline)205 else 100)*opacity/255;p.style=Paint.Style.STROKE;p.strokeWidth=d;c.drawRoundRect(rect,r,r,p)
        p.color=Color.WHITE;p.alpha=50*opacity/255;p.strokeWidth=.6f*d
        if(orb&&!outline){
            p.alpha=150*opacity/255;p.strokeWidth=1.2f*d;val inset=3*d;rect.inset(inset,inset);c.drawArc(rect,205f,85f,false,p)
            p.alpha=65*opacity/255;p.strokeWidth=2.2f*d;rect.inset(2*d,2*d);c.drawArc(rect,35f,48f,false,p)
            p.style=Paint.Style.FILL;p.alpha=95*opacity/255
            c.drawOval(rect.left+rect.width()*.17f,rect.top+rect.height()*.11f,rect.left+rect.width()*.35f,rect.top+rect.height()*.19f,p)
        }
        else {p.alpha=28*opacity/255;c.drawLine(rect.left+r,rect.top+d,rect.right-r,rect.top+d,p)}
    }
    override fun setAlpha(alpha:Int){opacity=alpha.coerceIn(0,255);invalidateSelf()}
    override fun setColorFilter(filter:ColorFilter?){p.colorFilter=filter;invalidateSelf()}
    @Deprecated("Drawable opacity is unused") override fun getOpacity()=PixelFormat.TRANSLUCENT
}
