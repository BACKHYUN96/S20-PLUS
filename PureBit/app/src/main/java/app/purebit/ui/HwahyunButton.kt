package app.purebit.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import app.purebit.theme.ThemeSpec
import app.purebit.theme.BuiltInThemes
import kotlin.math.*

/** Native Button semantics with theme-specific vector controls and floral seal feedback. */
class HwahyunButton(context:Context,private val theme:ThemeSpec):Button(context) {
    constructor(context:Context):this(context,BuiltInThemes.hwahyun)
    enum class Form { TEXT, ICON, SEAL, NAV }
    var form=Form.TEXT
    var navIndex=0
    private val d=resources.displayMetrics.density
    private val p=Paint(Paint.ANTI_ALIAS_FLAG);private val path=Path();private val r=RectF()
    private var glow=0f;private var animator:ValueAnimator?=null
    init{background=null;stateListAnimator=null;typeface=Typeface.SERIF;isAllCaps=false;minHeight=(44*d).toInt();minimumHeight=minHeight;setTextColor(theme.text)}
    override fun performClick():Boolean{
        if(ValueAnimator.areAnimatorsEnabled()&&context.getSystemService(AccessibilityManager::class.java)?.isTouchExplorationEnabled!=true){
            animator?.cancel();animator=ValueAnimator.ofFloat(1f,0f).apply {duration=420;addUpdateListener{glow=it.animatedValue as Float;invalidate()};start()}
        }
        return super.performClick()
    }
    override fun onDetachedFromWindow(){animator?.cancel();animator=null;super.onDetachedFromWindow()}
    override fun onDraw(c:Canvas){
        val w=width.toFloat();val h=height.toFloat();val x=w/2;val y=if(form==Form.NAV)h*.37f else h/2
        val enabled=if(isEnabled)1f else .32f
        p.alpha=255;p.shader=null;p.strokeCap=Paint.Cap.ROUND
        if(form==Form.TEXT){
            p.style=Paint.Style.FILL;p.color=0x35180e1b;c.drawRect(0f,0f,w,h,p)
            stroke(alpha(theme.accent,(55*enabled+glow*140).toInt()),.6f*d)
            c.drawLine(5*d,h-1*d,w-5*d,h-1*d,p)
            flower(c,9*d,h/2,2.5f*d,55)
            super.onDraw(c);return
        }
        val size=if(form==Form.SEAL)min(w,h)*.36f else if(form==Form.NAV)10*d else 11*d
        if(form==Form.SEAL){
            p.style=Paint.Style.FILL;p.color=alpha(theme.accent,(18+glow*28).toInt());c.drawCircle(x,y,size,p)
            for(i in 3 downTo 1){stroke(alpha(theme.accent,((8+glow*18)*enabled).toInt()),i*3*d);c.drawCircle(x,y,size+i*d,p)}
            stroke(alpha(theme.accent,(205*enabled).toInt()),.8f*d);c.drawCircle(x,y,size,p);c.drawCircle(x,y,size-4*d,p)
            for(i in 0..11){val a=i*PI/6;flower(c,x+cos(a).toFloat()*(size-8*d),y+sin(a).toFloat()*(size-8*d),2.3f*d,(160*enabled).toInt())}
        }
        if(form==Form.NAV){
            if(isSelected){p.style=Paint.Style.FILL;p.color=alpha(theme.accent,30);c.drawCircle(x,y,19*d,p)}
            when(navIndex){0->flower(c,x,y,6*d,if(isSelected)245 else 150);1->icon(c,"music",x,y,size,enabled);2->icon(c,"queue",x,y,size,enabled);else->flower(c,x,y,4*d,if(isSelected)245 else 150)}
            p.style=Paint.Style.FILL;p.color=if(isSelected)theme.text else theme.muted;p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.SERIF;p.textSize=textSize
            c.drawText(text.toString(),x,h*.83f,p)
            if(isSelected){stroke(alpha(theme.accent,180),d);c.drawLine(x-12*d,h-2*d,x+12*d,h-2*d,p)}
        }else icon(c,text.toString(),x,y,if(form==Form.SEAL)size*.40f else size,enabled)
        if(glow>.01f){stroke(alpha(theme.accent,(glow*140).toInt()),d);c.drawCircle(x,y,size+glow*6*d,p)}
    }
    private fun icon(c:Canvas,label:String,x:Float,y:Float,s:Float,enabled:Float){
        val color=alpha(currentTextColor,(255*enabled).toInt());stroke(color,1.4f*d)
        when(label){
            "▶"->{p.style=Paint.Style.FILL;path.reset();path.moveTo(x-s*.55f,y-s);path.lineTo(x+s*.8f,y);path.lineTo(x-s*.55f,y+s);path.close();c.drawPath(path,p)}
            "Ⅱ"->{p.style=Paint.Style.FILL;c.drawRoundRect(x-s*.65f,y-s,x-s*.22f,y+s,d,d,p);c.drawRoundRect(x+s*.22f,y-s,x+s*.65f,y+s,d,d,p)}
            "■"->{p.style=Paint.Style.FILL;c.drawRect(x-s*.6f,y-s*.6f,x+s*.6f,y+s*.6f,p)}
            "◀◀","▶▶"->{val direction=if(label=="◀◀")-1 else 1;p.style=Paint.Style.FILL;path.reset();path.moveTo(x+direction*s*.6f,y);path.lineTo(x-direction*s*.5f,y-s);path.lineTo(x-direction*s*.5f,y+s);path.close();c.drawPath(path,p);stroke(color,1.8f*d);c.drawLine(x+direction*s*.9f,y-s,x+direction*s*.9f,y+s,p)}
            "−","-","＋","+"->{c.drawLine(x-s*.65f,y,x+s*.65f,y,p);if(label=="＋"||label=="+")c.drawLine(x,y-s*.65f,x,y+s*.65f,p)}
            "⇄"->{path.reset();path.moveTo(x-s,y-s*.55f);path.cubicTo(x-s*.1f,y-s*.9f,x+s*.1f,y+s*.9f,x+s,y+s*.55f);c.drawPath(path,p);path.reset();path.moveTo(x-s,y+s*.55f);path.cubicTo(x-s*.1f,y+s*.9f,x+s*.1f,y-s*.9f,x+s,y-s*.55f);c.drawPath(path,p);c.drawLine(x+s*.6f,y-s*.95f,x+s,y-s*.55f,p)}
            "↻","↻¹"->{r.set(x-s,y-s*.65f,x+s,y+s*.65f);c.drawArc(r,25f,290f,false,p);c.drawLine(x+s*.8f,y-s*.65f,x+s*.5f,y-s*.1f,p);if(label.endsWith("¹")){p.style=Paint.Style.FILL;p.textSize=10*d;p.textAlign=Paint.Align.CENTER;c.drawText("1",x,y+3*d,p)}}
            "music"->{path.reset();path.moveTo(x-s*.3f,y+s*.5f);path.lineTo(x-s*.3f,y-s*.7f);path.lineTo(x+s*.6f,y-s);path.lineTo(x+s*.6f,y+s*.2f);c.drawPath(path,p);r.set(x-s*.8f,y+s*.25f,x-s*.25f,y+s*.7f);c.drawOval(r,p)}
            "queue"->{for(i in -1..1){c.drawLine(x-s*.4f,y+i*s*.6f,x+s,y+i*s*.6f,p);c.drawCircle(x-s*.85f,y+i*s*.6f,.6f*d,p)}}
            "⚙"->flower(c,x,y,s*.48f,(220*enabled).toInt())
            "⛶"->{for(i in 0..3){c.save();c.rotate(i*90f,x,y);path.reset();path.moveTo(x-s,y-s*.3f);path.lineTo(x-s,y-s);path.lineTo(x-s*.3f,y-s);c.drawPath(path,p);c.restore()}}
            else->{p.style=Paint.Style.FILL;p.typeface=Typeface.SERIF;p.textAlign=Paint.Align.CENTER;p.textSize=14*d;c.drawText(label,x,y+5*d,p)}
        }
    }
    private fun flower(c:Canvas,x:Float,y:Float,size:Float,opacity:Int){
        stroke(alpha(theme.accent,opacity),.65f*d)
        for(i in 0..4){c.save();c.rotate(i*72f,x,y);r.set(x-size*.48f,y-size*1.7f,x+size*.48f,y);c.drawOval(r,p);c.restore()}
        p.style=Paint.Style.FILL;p.color=alpha(theme.text,opacity);c.drawCircle(x,y,max(.5f*d,size*.18f),p)
    }
    private fun stroke(color:Int,width:Float){p.shader=null;p.color=color;p.style=Paint.Style.STROKE;p.strokeWidth=width}
    private fun alpha(color:Int,a:Int)=(color and 0xffffff) or (a.coerceIn(0,255) shl 24)
}

/** Ink panel edges remain translucent so the garden continues behind every page. */
class HwahyunPanel(private val theme:ThemeSpec,private val density:Float):Drawable(){
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    override fun draw(c:Canvas){val b=bounds;p.style=Paint.Style.FILL;p.color=0xb818101c.toInt();c.drawRect(b,p)
        p.style=Paint.Style.STROKE;p.strokeWidth=.6f*density;p.color=(theme.accent and 0xffffff) or 0x55000000
        val margin=10*density;c.drawLine(b.left+margin,b.top+1f,b.right-margin,b.top+1f,p);c.drawLine(b.left+margin,b.bottom-1f,b.right-margin,b.bottom-1f,p)
        c.drawCircle(b.left+4*density,b.top+1f,1.3f*density,p);c.drawCircle(b.right-4*density,b.bottom-1f,1.3f*density,p)}
    override fun setAlpha(alpha:Int){p.alpha=alpha}
    override fun setColorFilter(colorFilter:ColorFilter?){p.colorFilter=colorFilter}
    @Deprecated("Required by Drawable") override fun getOpacity()=PixelFormat.TRANSLUCENT
}

class BlossomThumb(private val theme:ThemeSpec,private val density:Float):Drawable(){
    private val p=Paint(Paint.ANTI_ALIAS_FLAG);private val r=RectF()
    override fun getIntrinsicWidth()=(30*density).toInt()
    override fun getIntrinsicHeight()=(30*density).toInt()
    override fun isStateful()=true
    override fun onStateChange(state:IntArray):Boolean {invalidateSelf();return true}
    override fun draw(c:Canvas){val x=bounds.exactCenterX();val y=bounds.exactCenterY();val s=5*density
        val checked=state.contains(android.R.attr.state_checked)
        p.style=Paint.Style.FILL;p.color=if(checked)theme.accent else theme.muted
        for(i in 0..4){c.save();c.rotate(i*72f,x,y);r.set(x-s*.55f,y-s*1.7f,x+s*.55f,y);c.drawOval(r,p);c.restore()}
        p.color=theme.text;c.drawCircle(x,y,1.3f*density,p)
    }
    override fun setAlpha(alpha:Int){p.alpha=alpha}
    override fun setColorFilter(colorFilter:ColorFilter?){p.colorFilter=colorFilter}
    @Deprecated("Required by Drawable") override fun getOpacity()=PixelFormat.TRANSLUCENT
}

class GardenQueueRail(private val theme:ThemeSpec,private val selected:Boolean,private val density:Float):Drawable(){
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    override fun draw(c:Canvas){val x=bounds.exactCenterX();val y=bounds.exactCenterY()
        p.style=Paint.Style.STROKE;p.strokeWidth=.6f*density;p.color=(theme.accent and 0xffffff) or 0x65000000
        c.drawLine(x,bounds.top.toFloat(),x,bounds.bottom.toFloat(),p)
        p.style=Paint.Style.FILL;p.color=0xf0180f1c.toInt();c.drawCircle(x,y,13*density,p)
        p.style=Paint.Style.STROKE;p.color=(theme.accent and 0xffffff) or (if(selected)0xdd000000.toInt()else 0x55000000)
        c.drawCircle(x,y,13*density,p)
        if(selected){p.color=(theme.accent and 0xffffff) or 0x33000000;p.strokeWidth=5*density;c.drawCircle(x,y,14*density,p)}
    }
    override fun setAlpha(alpha:Int){p.alpha=alpha}
    override fun setColorFilter(colorFilter:ColorFilter?){p.colorFilter=colorFilter}
    @Deprecated("Required by Drawable") override fun getOpacity()=PixelFormat.TRANSLUCENT
}
