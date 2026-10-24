package app.purebit.theme.abyss

import android.graphics.*
import app.purebit.theme.ThemeSpec

/** Reference line + white optical cursor, independent of PCM analysis and seek input. */
class AbyssSeekPainter(private val density: Float) {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private val matrix=Matrix()
    private var glow:Shader?=null
    private var accent=0
    fun draw(c:Canvas,left:Float,right:Float,height:Float,fraction:Float,theme:ThemeSpec,a:Float?,b:Float?) {
        if(accent!=theme.accent || glow==null) {
            accent=theme.accent
            glow=RadialGradient(0f,0f,9*density,intArrayOf((accent and 0xffffff) or 0x99000000.toInt(),accent and 0xffffff),null,Shader.TileMode.CLAMP)
        }
        val y=height*.38f;val span=(right-left).coerceAtLeast(0f);val x=left+span*fraction.coerceIn(0f,1f)
        p.shader=null;p.style=Paint.Style.STROKE;p.strokeCap=Paint.Cap.ROUND;p.strokeWidth=.65f*density;p.color=0x708daebe
        c.drawLine(left,y,right,y,p)
        if(a!=null&&b!=null) {
            p.strokeWidth=4*density;p.color=(accent and 0xffffff) or 0x45000000
            c.drawLine(left+span*a.coerceIn(0f,1f),y,left+span*b.coerceIn(0f,1f),y,p)
        }
        p.strokeWidth=.9f*density;p.color=accent;c.drawLine(left,y,x,y,p)
        p.style=Paint.Style.FILL
        matrix.setTranslate(x,y);glow?.setLocalMatrix(matrix);p.shader=glow;p.alpha=255;c.drawCircle(x,y,9*density,p);p.shader=null
        p.color=accent;c.drawCircle(x,y,3.4f*density,p)
        p.color=0xffefffff.toInt();c.drawCircle(x,y,2.7f*density,p)
    }
}
