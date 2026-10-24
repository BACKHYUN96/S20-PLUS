package app.purebit.ui

import android.content.Context
import android.graphics.*
import app.purebit.theme.BuiltInThemes
import app.purebit.theme.ThemeSpec
import kotlin.math.*

/** Shared moon/strings composition. The embedded player leaves its full-window garden visible. */
class HwahyunScene(context:Context) {
    private val d=context.resources.displayMetrics.density
    private val garden=GardenPainter(context)
    private val moon=GardenArt.moon(context)
    private val water by lazy {MoonWater(context)}
    private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val glowMatrix=Matrix()
    private var moonColor=BuiltInThemes.hwahyun.accent
    private var oldColor=moonColor
    private var tintAt=-10f
    private fun glow(color:Int)=RadialGradient(0f,0f,1f,
        intArrayOf(alpha(color,0),alpha(color,0),alpha(color,72),alpha(color,192),alpha(color,88),alpha(color,0)),
        floatArrayOf(0f,.80f,.88f,.93f,.96f,1f),Shader.TileMode.CLAMP)
    private var albumGlow=glow(moonColor)
    private var oldGlow=albumGlow
    fun setMoonColor(color:Int){
        if(color==moonColor)return
        oldColor=moonColor;oldGlow=albumGlow;moonColor=color;albumGlow=glow(color);tintAt=GardenClock.seconds()
    }
    private val path=Path();private val clip=Path();private val rect=RectF();private val src=Rect()
    private var lastArt:Bitmap?=null;private var previous:Bitmap?=null;private var artChanged=0f
    private var flowerAt=-10f
    private val flower=Path().apply {moveTo(0f,0f);cubicTo(-1f,-.25f,-.65f,-1.1f,0f,-.8f);cubicTo(.65f,-1.1f,1f,-.25f,0f,0f);close()}
    fun draw(c:Canvas,w:Float,h:Float,theme:ThemeSpec=BuiltInThemes.hwahyun,art:Bitmap?=null,
        seconds:Float=0f,levels:FloatArray?=null,pulse:Float=0f,flow:Float=0f,motion:Boolean=true,
        petalsScale:Float=1f,reveal:Float=1f,moonReveal:Float=1f,labels:Boolean=true,embedded:Boolean=false,presence:Float=1f){
        if(w<=0||h<=0)return
        val time=if(motion)GardenClock.seconds()else 0f
        if(!embedded){garden.background(c,w,h,time,theme);garden.petals(c,w,h,time,theme,petalsScale,false,presence)}
        drawStrings(c,w,h,theme,seconds,levels,flow,reveal,motion,presence)
        if(art!==lastArt){previous=lastArt;lastArt=art;artChanged=GardenClock.seconds();if(art!=null)flowerAt=artChanged}
        val fade=if(motion)((GardenClock.seconds()-artChanged)/1.15f).coerceIn(0f,1f)else 1f
        drawMoon(c,w*.5f,h*.45f,min(w*.42f,h*.41f),theme,art,moonReveal,pulse,previous,fade,if(motion)((GardenClock.seconds()-tintAt)/.9f).coerceIn(0f,1f)else 1f)
        if(motion && petalsScale>0f)drawFlowerSweep(c,w,h,theme,(GardenClock.seconds()-flowerAt)/1.45f)
        if(fade>=1f)previous=null
        if(!embedded)garden.petals(c,w,h,time,theme,petalsScale,true,presence)
    }
    fun drawLandscape(c:Canvas,w:Float,h:Float,t:ThemeSpec,art:Bitmap?,seconds:Float,levels:FloatArray?,pulse:Float,flow:Float,motion:Boolean,petalsScale:Float,presence:Float){
        val time=if(motion)GardenClock.seconds()else 0f
        garden.background(c,w,h,time,t);garden.petals(c,w,h,time,t,petalsScale,false,presence)
        if(art!==lastArt){previous=lastArt;lastArt=art;artChanged=GardenClock.seconds();if(art!=null)flowerAt=artChanged}
        val fade=if(motion)((GardenClock.seconds()-artChanged)/1.15f).coerceIn(0f,1f)else 1f
        val x=w*.25f;val y=h*.35f;val r=min(w*.205f,h*.29f)
        drawMoon(c,x,y,r,t,art,1f,pulse,previous,fade,if(motion)((GardenClock.seconds()-tintAt)/.9f).coerceIn(0f,1f)else 1f)
        water.draw(c,w,h,x,y,r,art,if(motion)seconds else 0f,pulse,t)
        c.save();c.translate(w*.49f,h*.14f);c.clipRect(0f,0f,w*.48f,h*.57f)
        drawStrings(c,w*.48f,h*.55f,t,seconds,levels,flow,motion=motion,presence=presence);c.restore()
        if(motion&&petalsScale>0f)drawFlowerSweep(c,w,h,t,(GardenClock.seconds()-flowerAt)/1.45f)
        if(fade>=1)previous=null
        garden.petals(c,w,h,time,t,petalsScale,true,presence)
    }
    fun drawLyricBackdrop(c:Canvas,w:Float,h:Float,t:ThemeSpec,art:Bitmap?,seconds:Float,pulse:Float,motion:Boolean,petalsScale:Float,presence:Float){
        val time=if(motion)GardenClock.seconds()else 0f
        garden.background(c,w,h,time,t,quiet=true)
        garden.petals(c,w,h,time,t,petalsScale*.65f,false,presence)
        if(art!==lastArt){previous=lastArt;lastArt=art;artChanged=GardenClock.seconds()}
        val fade=if(motion)((GardenClock.seconds()-artChanged)/1.15f).coerceIn(0f,1f)else 1f
        val wide=w>h
        val x=if(wide)w*.23f else w*.5f
        val y=if(wide)h*.44f else h*.22f
        val r=if(wide)min(w*.18f,h*.29f)else min(w*.23f,h*.17f)
        drawMoon(c,x,y,r,t,art,1f,pulse,previous,fade)
        if(wide)water.draw(c,w,h,x,y,r,art,if(motion)seconds else 0f,pulse,t)
        // Fixed translucent veil preserves text contrast without flashing with source energy.
        fill(alpha(t.background,82));c.drawRect(if(wide)w*.43f else 0f,if(wide)0f else h*.38f,w,h,p)
        if(fade>=1f)previous=null
        garden.petals(c,w,h,time,t,petalsScale*.45f,true,presence)
    }
    private fun drawFlowerSweep(c:Canvas,w:Float,h:Float,t:ThemeSpec,progress:Float){
        if(progress<=0f||progress>=1f)return
        val envelope=sin(progress*PI.toFloat())
        for(i in 0 until 24){
            val seed=(i*.618034f)%1f
            val x=w*(-.22f+progress*1.44f)+(seed-.5f)*w*.42f
            val y=h*(.16f+seed*.55f)+sin(progress*PI.toFloat()*2+i)*h*.065f
            val size=(5f+i%5*1.7f)*d
            fill(alpha(if(i%3==0)t.text else t.accent,(envelope*(90+i%4*22)).toInt()))
            c.save();c.translate(x,y);c.rotate(i*47f+progress*160f);c.scale(size,size);c.drawPath(flower,p);c.restore()
        }
    }
    fun drawStrings(c:Canvas,w:Float,h:Float,t:ThemeSpec,seconds:Float,levels:FloatArray?=null,flow:Float=0f,reveal:Float=1f,motion:Boolean=true,presence:Float=1f){
        for(i in 0 until 12){
            val appear=((reveal*14-i)/2).coerceIn(0f,1f);if(appear==0f)continue
            val energy=(levels?.getOrNull(i*5)?:0f).coerceIn(0f,1f)*presence
            val wave=if(motion)sin(seconds*(2.3f+i*.13f)+i)*energy*7*d else 0f
            val x0=-w*.04f;val x3=w*1.04f;val y0=h*(.77f+i*.013f);val y3=h*(.83f+i*.011f)
            val y1=h*(.62f+i*.021f)+wave;val y2=h*(1.02f-i*.005f)-wave
            c.save();c.clipRect(0f,0f,w*appear,h)
            path.reset();path.moveTo(x0,y0);path.cubicTo(w*.3f,y1,w*.72f,y2,x3,y3)
            stroke(alpha(t.accent,((12+energy*38)*appear).toInt()),4*d);c.drawPath(path,p)
            stroke(alpha(t.text,((42+energy*105)*appear).toInt()),(.55f+energy*.35f)*d);c.drawPath(path,p)
            if(flow>.002f&&motion){val q=(seconds*.08f+i*.081f)%1f;val u=1-q
                val x=u*u*u*x0+3*u*u*q*w*.3f+3*u*q*q*w*.72f+q*q*q*x3
                val y=u*u*u*y0+3*u*u*q*y1+3*u*q*q*y2+q*q*q*y3
                fill(alpha(moonColor,(50+flow*140).toInt()));c.drawCircle(x,y,(2f+flow)*d,p)
                fill(alpha(t.text,(50+flow*155).toInt()));c.drawCircle(x,y,1.1f*d,p)
                for(j in 1..3){val tail=(q-j*.013f).coerceAtLeast(0f);val v=1-tail
                    val tx=v*v*v*x0+3*v*v*tail*w*.3f+3*v*tail*tail*w*.72f+tail*tail*tail*x3
                    val ty=v*v*v*y0+3*v*v*tail*y1+3*v*tail*tail*y2+tail*tail*tail*y3
                    fill(alpha(moonColor,(flow*(90-j*20)).toInt()));c.drawCircle(tx,ty,(1.5f-j*.2f)*d,p)}}
            c.restore()
        }
    }
    fun drawMoon(c:Canvas,x:Float,y:Float,radius:Float,t:ThemeSpec,art:Bitmap?=null,reveal:Float=1f,pulse:Float=0f,oldArt:Bitmap?=null,fade:Float=1f,tintMix:Float=1f){
        if(reveal<=0f)return
        val r=radius*(.94f+.06f*reveal)
        for(i in 3 downTo 1){stroke(alpha(t.accent,((5+pulse*12)*reveal).toInt()),i*5*d);c.drawCircle(x,y,r+(i+pulse*6)*d,p)}
        rect.set(x-r*1.17f,y-r*1.17f,x+r*1.17f,y+r*1.17f)
        fill(Color.WHITE);p.alpha=(255*reveal).toInt();c.drawBitmap(moon,null,rect,p)
        stroke(alpha(blend(oldColor,moonColor,tintMix),(20*reveal).toInt()),5*d)
        c.drawCircle(x,y,r*1.01f,p)
        // Visual-only glow from the already smoothed source bass envelope. No audio gain/DSP.
        if((art!=null&&!art.isRecycled)||(oldArt!=null&&!oldArt.isRecycled)){
            val bass=(pulse*3f).coerceIn(0f,1f)
            val edge=r*.88f+bass*2*d
            val haloRadius=edge/.93f
            glowMatrix.setScale(haloRadius,haloRadius);glowMatrix.postTranslate(x,y)
            val glowAlpha=((.48f+bass*.48f)*255*reveal).toInt()
            if(tintMix<1f){oldGlow.setLocalMatrix(glowMatrix);fill(Color.WHITE);p.shader=oldGlow;p.alpha=(glowAlpha*(1-tintMix)).toInt();c.drawCircle(x,y,haloRadius,p)}
            albumGlow.setLocalMatrix(glowMatrix)
            fill(Color.WHITE);p.shader=albumGlow;p.alpha=(glowAlpha*tintMix).toInt()
            c.drawCircle(x,y,haloRadius,p)
            stroke(alpha(blend(oldColor,moonColor,tintMix),((66f+bass*105f)*reveal).toInt()),(1.7f+bass*.9f)*d)
            c.drawCircle(x,y,edge,p)
        }
        // Let the moon texture show gently through even dark album covers.
        if(oldArt!=null&&!oldArt.isRecycled&&fade<1f)album(c,oldArt,x,y,r*.86f,(218*reveal*(1-fade)).toInt())
        if(art!=null&&!art.isRecycled)album(c,art,x,y,r*.86f,(218*reveal*fade).toInt())
        stroke(alpha(t.accent,(125*reveal).toInt()),.65f*d);c.drawCircle(x,y,r*.88f,p)
    }
    private fun album(c:Canvas,bitmap:Bitmap,x:Float,y:Float,r:Float,a:Int){
        c.save();clip.reset();clip.addCircle(x,y,r,Path.Direction.CW);c.clipPath(clip)
        val side=min(bitmap.width,bitmap.height);src.set((bitmap.width-side)/2,(bitmap.height-side)/2,(bitmap.width+side)/2,(bitmap.height+side)/2)
        rect.set(x-r,y-r,x+r,y+r);fill(Color.WHITE);p.alpha=a;c.drawBitmap(bitmap,src,rect,p);c.restore()
    }
    private fun blend(a:Int,b:Int,f:Float):Int {
        fun ch(shift:Int)=((a ushr shift and 255)*(1-f)+(b ushr shift and 255)*f).toInt().coerceIn(0,255)
        return (0xff shl 24) or (ch(16) shl 16) or (ch(8) shl 8) or ch(0)
    }
    private fun fill(color:Int){p.shader=null;p.style=Paint.Style.FILL;p.color=color}
    private fun stroke(color:Int,width:Float){fill(color);p.style=Paint.Style.STROKE;p.strokeWidth=width;p.strokeCap=Paint.Cap.ROUND}
    private fun alpha(color:Int,a:Int)=(color and 0xffffff) or (a.coerceIn(0,255) shl 24)
}
