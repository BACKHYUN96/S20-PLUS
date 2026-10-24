package app.purebit.ui

import android.content.Context
import android.graphics.*
import app.purebit.theme.ThemeSpec
import kotlin.math.*

/** Mirrored visual artwork in clipped water bands; never reads an output device or changes PCM. */
class MoonWater(context:Context){
    private val moon=GardenArt.moon(context)
    private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val rect=RectF();private val src=Rect();private val clip=Path();private val line=Path()
    private val matrix=Matrix()
    private val shade=LinearGradient(0f,0f,0f,1f,intArrayOf(0x121e1120,0xdd100c18.toInt()),null,Shader.TileMode.CLAMP)
    fun draw(c:Canvas,w:Float,h:Float,x:Float,y:Float,r:Float,art:Bitmap?,seconds:Float,bass:Float,theme:ThemeSpec){
        val water=h*.66f;val depth=h-water
        p.style=Paint.Style.FILL;p.color=Color.WHITE;p.alpha=255
        matrix.setScale(1f,depth);matrix.postTranslate(0f,water);shade.setLocalMatrix(matrix);p.shader=shade;c.drawRect(0f,water,w,h,p);p.shader=null
        val energy=(bass*3).coerceIn(0f,1f)
        for(i in 0 until 24){
            val q=i/24f;val y0=water+depth*q;val y1=water+depth*(i+1)/24f
            val shift=sin(seconds*1.25f+i*.52f)*(1.2f+energy*3)*contextDensity
            c.save();c.clipRect(0f,y0,w,y1+.5f);c.translate(shift,water*2);c.scale(1f,-1f)
            p.alpha=(65*(1-q).pow(1.4f)).toInt();rect.set(x-r*1.17f,y-r*1.17f,x+r*1.17f,y+r*1.17f);c.drawBitmap(moon,null,rect,p)
            if(art!=null&&!art.isRecycled){c.save();clip.reset();clip.addCircle(x,y,r*.86f,Path.Direction.CW);c.clipPath(clip)
                val side=min(art.width,art.height);src.set((art.width-side)/2,(art.height-side)/2,(art.width+side)/2,(art.height+side)/2)
                rect.set(x-r*.86f,y-r*.86f,x+r*.86f,y+r*.86f);p.alpha=(p.alpha*.85f).toInt();c.drawBitmap(art,src,rect,p);c.restore()}
            c.restore()
        }
        p.style=Paint.Style.STROKE;p.strokeWidth=.6f*contextDensity
        for(i in 0 until 9){val yy=water+depth*(.08f+i*.105f);val spread=w*(.10f+i*.014f)
            p.color=(theme.accent and 0xffffff) or ((13+i%3*5+energy*14).toInt() shl 24)
            line.reset();for(j in 0..24){val xx=x-spread+j*spread*2/24
                val dy=sin(j*.6f+seconds*(.5f+i*.05f)+i)*(1+energy*2)*contextDensity
                if(j==0)line.moveTo(xx,yy+dy)else line.lineTo(xx,yy+dy)};c.drawPath(line,p)}
        p.alpha=255;p.style=Paint.Style.FILL
    }
    private val contextDensity=context.resources.displayMetrics.density
}
