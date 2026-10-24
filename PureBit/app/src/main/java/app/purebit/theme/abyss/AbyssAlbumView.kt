package app.purebit.theme.abyss

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.View
import android.view.WindowInsets
import android.view.accessibility.AccessibilityManager
import android.view.animation.LinearInterpolator
import app.purebit.theme.ThemeSpec
import kotlin.math.*

/** One continuous source bitmap and clipping geometry; never starts or pauses playback. */
@android.annotation.SuppressLint("ViewConstructor") // Runtime-only overlay requires the originating album geometry.
class AbyssAlbumView(context:Context,private val theme:ThemeSpec,private val origin:RectF,private val occluder:RectF,
    private var art:Bitmap,private val motion:Boolean,private val dismissed:()->Unit):View(context),AutoCloseable {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val path=Path();private val frame=RectF();private val src=Rect();private val target=RectF()
    private val scene=AbyssScene(context)
    fun configureAbyss(value:AbyssEffects){scene.configure(value)}
    private val glare=LinearGradient(0f,0f,1f,1f,intArrayOf(0x40ffffff,0x00ffffff,0x10ffffff),null,Shader.TileMode.CLAMP)
    private val glareMatrix=Matrix()
    private val erase=PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    private val d=resources.displayMetrics.density
    private val captionSize=android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_SP,13f,resources.displayMetrics)
    private var progress=0f;private var animator:ValueAnimator?=null;private var closing=false;private var closed=false
    private var bubbleAt=0L
    init {
        isClickable=true;isFocusable=true;importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_YES
        contentDescription="원본 앨범아트 · 누르거나 뒤로 가기로 닫기"
        setOnClickListener {dismiss()}
        setOnApplyWindowInsetsListener {_,insets->invalidate();insets}
    }
    fun start(){accessibilityPaneTitle="원본 앨범아트";requestFocus();animateTo(1f)}
    fun setArtwork(bitmap:Bitmap){art=bitmap;invalidate()}
    private fun animateTo(to:Float) {
        animator?.cancel()
        bubbleAt=android.os.SystemClock.uptimeMillis()
        val enabled=motion&&ValueAnimator.areAnimatorsEnabled()&&context.getSystemService(AccessibilityManager::class.java)?.isTouchExplorationEnabled!=true
        if(!enabled){progress=to;invalidate();if(to==0f)finish();return}
        animator=ValueAnimator.ofFloat(progress,to).apply {
            duration=(900*abs(to-progress)).toLong().coerceAtLeast(100)
            interpolator=LinearInterpolator()
            addUpdateListener {progress=it.animatedValue as Float;invalidate();if(progress<=0f&&closing)finish()}
            start()
        }
    }
    fun dismiss(){if(closing||closed)return;closing=true;animateTo(0f)}
    private fun finish(){if(closed)return;close();dismissed()}
    override fun close(){if(closed)return;closed=true;animator?.removeAllUpdateListeners();animator?.cancel();animator=null}
    override fun onDetachedFromWindow(){close();super.onDetachedFromWindow()}
    override fun onDraw(c:Canvas) {
        if(art.isRecycled)return
        val q=AbyssGeometry.albumTravel(progress)
        val uncrop=AbyssGeometry.albumUncrop(progress)
        p.shader=null;p.style=Paint.Style.FILL;p.color=0xff010a13.toInt();p.alpha=(q*220).toInt();c.drawRect(0f,0f,width.toFloat(),height.toFloat(),p)
        val bars=rootWindowInsets?.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
        val left=(bars?.left?:0)+24*d;val right=width-(bars?.right?:0)-24*d
        val top=(bars?.top?:0)+48*d;val bottom=height-(bars?.bottom?:0)-72*d
        val scale=min((right-left)/art.width,(bottom-top)/art.height)
        val tw=art.width*scale;val th=art.height*scale
        target.set((left+right-tw)/2,(top+bottom-th)/2,(left+right+tw)/2,(top+bottom+th)/2)
        fun mix(a:Float,b:Float)=a+(b-a)*q
        frame.set(mix(origin.left,target.left),mix(origin.top,target.top),mix(origin.right,target.right),mix(origin.bottom,target.bottom))
        frame.offset(sin(q*PI).toFloat()*10*d,-sin(q*PI).toFloat()*20*d)
        val radius=AbyssGeometry.corner(min(frame.width(),frame.height()),uncrop)
        val layer=if(q<.3f)c.saveLayer(frame,null)else -1
        p.color=theme.accent;p.alpha=(35*q).toInt();p.style=Paint.Style.STROKE;p.strokeWidth=10*d;c.drawRoundRect(frame,radius,radius,p)
        c.save();path.reset();path.addRoundRect(frame,radius,radius,Path.Direction.CW);c.clipPath(path)
        val side=min(art.width,art.height).toFloat()
        fun cropMix(a:Float,b:Float)=a+(b-a)*uncrop
        src.set(cropMix((art.width-side)/2,0f).toInt(),cropMix((art.height-side)/2,0f).toInt(),cropMix((art.width+side)/2,art.width.toFloat()).toInt(),cropMix((art.height+side)/2,art.height.toFloat()).toInt())
        p.style=Paint.Style.FILL;p.alpha=255;p.color=Color.WHITE;c.drawBitmap(art,src,frame,p)
        glareMatrix.setScale(frame.width(),frame.height());glareMatrix.postTranslate(frame.left,frame.top);glare.setLocalMatrix(glareMatrix)
        p.shader=glare;c.drawRoundRect(frame,radius,radius,p);p.shader=null;c.restore()
        p.color=theme.accent;p.alpha=220;p.style=Paint.Style.STROKE;p.strokeWidth=1.2f*d;c.drawRoundRect(frame,radius,radius,p)
        if(layer>=0){
            p.style=Paint.Style.FILL;p.color=Color.BLACK;p.alpha=((1-q/.3f)*255).toInt().coerceIn(0,255);p.xfermode=erase
            c.drawOval(occluder,p);p.xfermode=null;c.restoreToCount(layer)
        }
        if(motion&&progress>0f&&progress<1f)for(i in 0..23) {
            val a=i*2*PI/24;val cx=cos(a).toFloat();val sy=sin(a).toFloat()
            val edge=1f+(1f/max(abs(cx),abs(sy))-1f)*uncrop
            val elapsed=(android.os.SystemClock.uptimeMillis()-bubbleAt)/1000f
            val x=frame.centerX()+cx*(frame.width()*.52f*edge+elapsed*10*d)
            val y=frame.centerY()+sy*frame.height()*.52f*edge-elapsed*(25+i%5*13)*d
            scene.bubble(c,x,y,(2+i%5)*d,theme.accent,(AbyssGeometry.bubbleEnvelope(progress)*160).toInt())
        }
        p.style=Paint.Style.FILL;p.color=theme.text;p.alpha=(q*220).toInt();p.textSize=captionSize;p.textAlign=Paint.Align.CENTER
        c.drawText("원본 앨범아트 · 터치하여 돌아가기",width*.5f,height-(bars?.bottom?:0)-30*d,p)
    }
}
