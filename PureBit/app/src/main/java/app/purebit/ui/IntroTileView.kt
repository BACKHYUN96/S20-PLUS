package app.purebit.ui

import android.content.Context
import android.graphics.*
import android.view.View
import app.purebit.theme.*
import kotlin.math.*

/** Static thumbnail; the preview button runs the actual bundled opening. */
class IntroTileView(context:Context,private val theme:ThemeSpec,private val scene:IntroScene):View(context){
    constructor(context:Context):this(context,BuiltInThemes.hwahyun,IntroScene.THEME)
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private val path=Path()
    private var background:Shader?=null
    init {importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO}
    override fun onSizeChanged(w:Int,h:Int,ow:Int,oh:Int){background=LinearGradient(0f,0f,w.toFloat(),h.toFloat(),intArrayOf(theme.surface,theme.background),null,Shader.TileMode.CLAMP)}
    override fun onDraw(c:Canvas){
        val w=width.toFloat();val h=height.toFloat();p.shader=background;p.style=Paint.Style.FILL;c.drawRect(0f,0f,w,h,p);p.shader=null
        val cx=w*.53f;val cy=h*.44f;val r=h*.28f
        for(i in 4 downTo 1){p.color=(theme.accent and 0xffffff) or ((12+i*5) shl 24);c.drawCircle(cx,cy,r+i*3,p)}
        p.color=theme.text;c.drawCircle(cx,cy,r,p)
        p.color=theme.surface;p.alpha=110;c.drawCircle(cx+r*.30f,cy-r*.2f,r*.88f,p);p.alpha=255
        val count=if(scene==IntroScene.PETAL_RAIN)38 else 9
        for(i in 0 until count){val x=((i*.618034f)%1f)*w;val y=((i*.381966f)%1f)*h
            p.color=theme.accent;p.alpha=100+i%4*30;c.save();c.translate(x,y);c.rotate(i*37f);c.drawOval(-4f,-7f,4f,7f,p);c.restore()}
        p.alpha=150;p.style=Paint.Style.STROKE;p.strokeWidth=resources.displayMetrics.density
        for(i in 0 until 7){path.reset();path.moveTo(0f,h*(.72f+i*.02f));path.cubicTo(w*.3f,h*.52f,w*.7f,h*.98f,w,h*(.74f+i*.02f));c.drawPath(path,p)
            if(scene==IntroScene.STRING_BLOOM){p.style=Paint.Style.FILL;for(j in 0 until 5){val a=j*PI*2/5;c.drawCircle(w*(.18f+i*.10f)+cos(a).toFloat()*6,h*.70f+sin(a).toFloat()*6,3f,p)};p.style=Paint.Style.STROKE}}
        p.alpha=255;p.style=Paint.Style.FILL
    }
}
