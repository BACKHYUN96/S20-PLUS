package app.purebit.theme.abyss

import android.content.Context
import android.graphics.*
import android.widget.Button
import app.purebit.theme.ThemeSpec
import app.purebit.theme.abyss.AbyssControlGeometry.Form

/** Native clicks, semantics and focus with completely theme-owned optical surfaces and symbols. */
class AbyssButton(context:Context):Button(context) {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private val icons=AbyssIconPainter()
    private val face=Typeface.create("sans-serif-light",Typeface.NORMAL)
    private var form=Form.PANEL
    private var accent=0xffa2f6ff.toInt()
    private var pointGlow:Shader?=null
    private var label=""
    fun configure(theme:ThemeSpec,primary:Boolean) {
        accent=theme.accent
        form=AbyssControlGeometry.form(label,primary)
        background=AbyssControlSurface(context,theme,form)
    }
    override fun onTextChanged(text:CharSequence?,start:Int,before:Int,count:Int) {
        super.onTextChanged(text,start,before,count)
        label=text?.toString().orEmpty()
    }
    override fun onSizeChanged(w:Int,h:Int,oldw:Int,oldh:Int) {
        super.onSizeChanged(w,h,oldw,oldh)
        val d=resources.displayMetrics.density
        if(w>0&&h>0)pointGlow=RadialGradient(w*.5f,h-4*d,7*d,intArrayOf(accent,accent and 0x00ffffff),null,Shader.TileMode.CLAMP)
    }
    override fun onDraw(c:Canvas) {
        val d=resources.displayMetrics.density
        val color=if(isEnabled)currentTextColor else (currentTextColor and 0xffffff)or 0x65000000
        val saved=c.save()
        if(isPressed)c.scale(.965f,.965f,width*.5f,height*.5f)
        if(form==Form.NAV&&resources.configuration.fontScale<=1.35f) {
            if(isSelected){p.shader=pointGlow;p.style=Paint.Style.FILL;p.alpha=160;c.drawCircle(width*.5f,height-4*d,7*d,p);p.shader=null}
            icons.draw(c,label,width*.5f,height*.29f,11.5f*d,color,d)
            p.style=Paint.Style.FILL;p.color=color;p.typeface=face;p.textAlign=Paint.Align.CENTER;p.textSize=textSize*.88f
            val tracking=1.2f*d
            val total=p.measureText(label)+(label.length-1).coerceAtLeast(0)*tracking
            var xx=(width-total)/2
            p.textAlign=Paint.Align.LEFT
            for(i in label.indices){c.drawText(label,i,i+1,xx,height*.77f,p);xx+=p.measureText(label,i,i+1)+tracking}
            if(isSelected){p.color=Color.WHITE;p.alpha=240;c.drawCircle(width*.5f,height-4*d,1.8f*d,p)}
        } else if(label in SYMBOLS) {
            val r=when {
                form==Form.ORB -> minOf(width,height)*.155f
                label=="◀◀"||label=="▶▶" -> 9.5f*d
                form==Form.RING -> 9*d
                label=="⚙" -> 10.5f*d
                else -> 11.5f*d
            }
            icons.draw(c,label,width*.5f,height*.5f,r,color,d)
        } else super.onDraw(c)
        c.restoreToCount(saved)
    }
    companion object {
        private val RING=setOf("−","＋")
        private val ICON=setOf("◀◀","▶▶","⇄","↻","↻¹","⚙","⛶")
        private val SYMBOLS=ICON+RING+setOf("▶","Ⅱ","■")
    }
}
