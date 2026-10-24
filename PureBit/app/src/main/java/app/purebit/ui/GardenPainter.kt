package app.purebit.ui

import android.content.Context
import android.graphics.*
import app.purebit.theme.ThemeSpec
import kotlin.math.*

/** Background and foreground are independently layered around real accessible widgets. */
class GardenPainter(context:Context) {
    private val d=context.resources.displayMetrics.density
    private val art=GardenArt.background(context)
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val rect=RectF();private val point=FloatArray(5)
    private val petal=Path().apply {moveTo(0f,1f);cubicTo(-1.1f,.3f,-1f,-.9f,-.1f,-.76f);lineTo(0f,-.48f);cubicTo(.8f,-1.1f,1.05f,.2f,0f,1f);close()}
    private val blur=makeSoftPetal()
    private var scrim:Shader?=null;private var lastH=0f
    private var nightTint:Shader?=null
    private val mist=RadialGradient(0f,0f,1f,intArrayOf(0x66d9b4e7,0x00d9b4e7),null,Shader.TileMode.CLAMP)
    private val mistMatrix=Matrix()
    fun background(c:Canvas,w:Float,h:Float,seconds:Float,theme:ThemeSpec,quiet:Boolean=false,camera:Float=1f){
        if(w<=0||h<=0)return
        val zoom=max(w/art.width,h/art.height)*(camera+sin(seconds*.055f)*.006f)
        val dw=art.width*zoom;val dh=art.height*zoom
        val dx=(w-dw)/2+sin(seconds*.045f)*w*.008f
        rect.set(dx,(h-dh)/2,dx+dw,(h+dh)/2)
        paint.shader=null;paint.style=Paint.Style.FILL;paint.color=Color.WHITE;paint.alpha=255;c.drawBitmap(art,null,rect,paint)
        if(lastH!=h){lastH=h;scrim=LinearGradient(0f,0f,0f,h,intArrayOf(0x40120a15,0x14120a15,0xb8120a15.toInt(),0xed120a15.toInt(),0xf2120a15.toInt()),floatArrayOf(0f,.27f,.42f,.63f,1f),Shader.TileMode.CLAMP)}
        paint.shader=scrim;c.drawRect(0f,0f,w,h,paint);paint.shader=null
        drawNight(c,w,h,seconds)
        if(quiet){paint.color=0xb4100a16.toInt();c.drawRect(0f,0f,w,h,paint)}
        // A low-opacity tint lets imported palettes influence the bundled scene.
        paint.color=alpha(theme.background,18);c.drawRect(0f,0f,w,h,paint)
    }
    private fun drawNight(c:Canvas,w:Float,h:Float,seconds:Float){
        val level=GardenNight.state.strength
        if(level<=.001f)return
        if(nightTint==null||nightHeight!=h){nightHeight=h;nightTint=LinearGradient(0f,0f,0f,h,intArrayOf(0xd40c103e.toInt(),0x7e281746,0x30170928),floatArrayOf(0f,.48f,1f),Shader.TileMode.CLAMP)}
        paint.style=Paint.Style.FILL;paint.color=Color.WHITE;paint.alpha=(255*level).toInt();paint.shader=nightTint;c.drawRect(0f,0f,w,h,paint);paint.shader=null
        for(i in 0 until 54){
            val x=((i*.618034f+.13f)%1f)*w;val y=((i*.381966f+.07f)%1f)*h*.51f
            val twinkle=.72f+.28f*sin(seconds*.32f+i*1.7f)
            paint.color=alpha(0xffe4deff.toInt(),((level-.12f).coerceAtLeast(0f)*twinkle*(75+i%4*24)).toInt())
            c.drawCircle(x,y,(.45f+i%3*.27f)*d,paint)
        }
        paint.color=Color.WHITE;paint.alpha=(level*100).toInt();paint.shader=mist
        for(i in 0..2){
            val x=w*(.12f+i*.42f)+sin(seconds*.035f+i)*w*.08f;val y=h*(.72f+i*.075f)
            mistMatrix.setScale(w*.60f,h*.085f);mistMatrix.postTranslate(x,y);mist.setLocalMatrix(mistMatrix)
            c.drawOval(x-w*.60f,y-h*.085f,x+w*.60f,y+h*.085f,paint)
        }
        paint.shader=null;paint.alpha=255
    }
    private var nightHeight=0f
    fun petals(c:Canvas,w:Float,h:Float,seconds:Float,theme:ThemeSpec,scale:Float,foreground:Boolean,presence:Float=1f){
        for(i in 0 until GardenMotion.count(theme.petals,scale)){
            val near=i%9==0
            if(foreground!=near)continue
            GardenMotion.petal(i,seconds,theme.petalSpeed,point)
            val x=point[0]*w;val y=point[1]*h;val radius=point[2]*d
            val legibility=(if(!near&&y>h*.54f&&x>w*.13f&&x<w*.87f).32f else 1f)*(.55f+.45f*presence.coerceIn(0f,1f))
            c.save();c.translate(x,y);c.rotate(point[3])
            paint.style=Paint.Style.FILL;paint.shader=null;paint.color=theme.accent;paint.alpha=(255*point[4]*legibility).toInt()
            if(near){rect.set(-radius,-radius,radius,radius);c.drawBitmap(blur,null,rect,paint)}
            else{
                c.scale(radius*(.65f+.3f*abs(cos(seconds*.8f+i))),radius)
                c.drawPath(petal,paint)
                paint.color=alpha(theme.text,(100*point[4]*legibility).toInt());paint.style=Paint.Style.STROKE;paint.strokeWidth=.05f
                c.drawLine(0f,-.5f,0f,.7f,paint)
            }
            c.restore()
        }
        paint.alpha=255;paint.style=Paint.Style.FILL
    }
    private fun makeSoftPetal():Bitmap {
        val bitmap=Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888);val canvas=Canvas(bitmap)
        val p=Paint(Paint.ANTI_ALIAS_FLAG).apply {color=0xffffc1d1.toInt();maskFilter=BlurMaskFilter(7f,BlurMaskFilter.Blur.NORMAL)}
        val shape=Path(petal);shape.transform(Matrix().apply {setScale(43f,43f)});canvas.translate(64f,64f);canvas.drawPath(shape,p);return bitmap
    }
    private fun alpha(c:Int,a:Int)=(c and 0xffffff) or (a.coerceIn(0,255) shl 24)
}
