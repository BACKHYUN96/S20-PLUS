package app.purebit.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.Choreographer
import android.view.View
import android.view.ViewTreeObserver
import android.view.accessibility.AccessibilityManager
import app.purebit.theme.ThemeSpec
import app.purebit.theme.BuiltInThemes
import kotlin.math.*

/** Decorative full-window layer. It cannot consume touches or issue audio commands. */
class GardenAtmosphereView(context:Context,private val theme:ThemeSpec,private val foreground:Boolean):View(context),AutoCloseable {
    constructor(context:Context):this(context,BuiltInThemes.hwahyun,false)
    private val painter by lazy {GardenPainter(context)}
    private val abyss by lazy {app.purebit.theme.abyss.AbyssScene(context)}
    private val abyssReadability=app.purebit.theme.abyss.AbyssReadability()
    private val protectedViews=ArrayList<View>()
    private val localOrigin=IntArray(2)
    private val protectedOrigin=IntArray(2)
    private val visibleBounds=Rect()
    private var regionsDirty=true
    private val regionLayout=ViewTreeObserver.OnGlobalLayoutListener {regionsDirty=true}
    private val regionScroll=ViewTreeObserver.OnScrollChangedListener {regionsDirty=true}
    fun protectAbyssViews(views:List<View>) {
        protectedViews.clear();protectedViews.addAll(views);regionsDirty=true;invalidate()
    }
    private fun updateAbyssRegions() {
        if(!regionsDirty)return
        regionsDirty=false;abyssReadability.clear();getLocationInWindow(localOrigin)
        for(view in protectedViews)if(view.isShown&&view.getLocalVisibleRect(visibleBounds)) {
            view.getLocationInWindow(protectedOrigin)
            val dx=(protectedOrigin[0]-localOrigin[0]).toFloat();val dy=(protectedOrigin[1]-localOrigin[1]).toFloat()
            abyssReadability.add(dx+visibleBounds.left,dy+visibleBounds.top,dx+visibleBounds.right,dy+visibleBounds.bottom)
        }
    }
    fun configureAbyss(value:app.purebit.theme.abyss.AbyssEffects){if(theme.renderer==app.purebit.theme.ThemeRenderer.ABYSS)abyss.configure(value);invalidate()}
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private val presence=GardenPresence()
    private var playing=false
    private var lastFrame=0L
    fun setPlaying(value:Boolean){playing=value}
    private var active=false;private var posted=false;private var closed=false;private var next=0L
    private var motion=true;private var densityScale=1f;private var quiet=false
    private var sweepAt=-10f
    private val petal=Path().apply {moveTo(0f,0f);cubicTo(-1f,-.25f,-.65f,-1.1f,0f,-.8f);cubicTo(.65f,-1.1f,1f,-.25f,0f,0f);close()}
    private var burstX=0f;private var burstY=0f;private var burstAt=-100f
    private val frame=object:Choreographer.FrameCallback{override fun doFrame(now:Long){
        posted=false;if(!active||closed||!isAttachedToWindow)return
        if(now+500_000L>=next){presence.update(playing,if(lastFrame==0L)1f/60 else (now-lastFrame)/1e9f,motion);lastFrame=now;next=if(next==0L||now-next>50_000_000L)now+16_666_667L else next+16_666_667L;invalidate()};schedule()
    }}
    init{isClickable=false;isFocusable=false;importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO}
    fun configure(scale:Float,quiet:Boolean){densityScale=scale;this.quiet=quiet;invalidate()}
    fun setActive(value:Boolean){
        motion=ValueAnimator.areAnimatorsEnabled()&&context.getSystemService(AccessibilityManager::class.java)?.isTouchExplorationEnabled!=true
        active=value&&!closed;next=0;lastFrame=0
        if(active&&motion)schedule()else{Choreographer.getInstance().removeFrameCallback(frame);posted=false};invalidate()
    }
    fun burst(x:Float,y:Float){if(!motion)return;burstX=x;burstY=y;burstAt=GardenClock.seconds();invalidate()}
    fun trackChanged(){if(active&&motion&&densityScale>0f){sweepAt=GardenClock.seconds();invalidate()}}
    private fun schedule(){if(active&&motion&&!posted&&isAttachedToWindow){posted=true;Choreographer.getInstance().postFrameCallback(frame)}}
    override fun onAttachedToWindow(){super.onAttachedToWindow();regionsDirty=true;if(foreground&&theme.renderer==app.purebit.theme.ThemeRenderer.ABYSS){viewTreeObserver.addOnGlobalLayoutListener(regionLayout);viewTreeObserver.addOnScrollChangedListener(regionScroll)};schedule()}
    override fun onDetachedFromWindow(){Choreographer.getInstance().removeFrameCallback(frame);posted=false;if(viewTreeObserver.isAlive){viewTreeObserver.removeOnGlobalLayoutListener(regionLayout);viewTreeObserver.removeOnScrollChangedListener(regionScroll)};super.onDetachedFromWindow()}
    override fun onDraw(c:Canvas){
        val time=if(motion)GardenClock.seconds()else 0f
        if(theme.renderer==app.purebit.theme.ThemeRenderer.ABYSS){
            val w=width.toFloat();val h=height.toFloat()
            if(!foreground)abyss.background(c,w,h,time,quiet)
            if(foreground)updateAbyssRegions()
            abyss.particles(c,w,h,time,theme,foreground,densityScale,if(foreground)abyssReadability else null)
            val elapsed=time-burstAt
            if(foreground&&elapsed in 0f..1.1f)for(i in 0..7){val a=i*PI/4;val d=resources.displayMetrics.density
                abyss.bubble(c,burstX+cos(a).toFloat()*elapsed*25*d,burstY-elapsed*(35+i*6)*d,(2+i%3)*d,theme.accent,((1-elapsed/1.1f)*130).toInt())}
            return
        }
        if(!foreground)painter.background(c,width.toFloat(),height.toFloat(),time,theme,quiet)
        painter.petals(c,width.toFloat(),height.toFloat(),time,theme,densityScale,foreground,if(motion)presence.value else 1f)
        val sweep=(time-sweepAt)/1.65f
        if(motion&&densityScale>0f&&sweep in 0f..1f){
            val envelope=sin(sweep*PI.toFloat())
            val d=resources.displayMetrics.density
            paint.style=Paint.Style.FILL
            for(i in 0 until 28){
                val seed=(i*.618034f)%1f
                val x=width*(-.25f+sweep*1.5f)+(seed-.5f)*width*.36f
                val y=height*(.12f+seed*.72f)+sin(sweep*PI.toFloat()*2+i)*height*.04f
                val size=(4f+i%5*1.4f)*d
                paint.color=((if(i%3==0)theme.text else theme.accent) and 0xffffff) or ((envelope*(85+i%4*18)).toInt() shl 24)
                c.save();c.translate(x,y);c.rotate(i*47f+sweep*180);c.scale(size,size);c.drawPath(petal,paint);c.restore()
            }
        }
        val elapsed=time-burstAt
        if(foreground&&elapsed in 0f..1.1f){
            paint.style=Paint.Style.STROKE;paint.strokeWidth=resources.displayMetrics.density
            paint.color=(theme.accent and 0xffffff) or (((1-elapsed/1.1f)*100).toInt() shl 24)
            c.drawCircle(burstX,burstY,(10+elapsed*50)*resources.displayMetrics.density,paint)
            paint.style=Paint.Style.FILL
            for(i in 0..5){val a=i*PI/3+elapsed;val r=elapsed*36*resources.displayMetrics.density
                c.drawOval((burstX+cos(a)*r).toFloat(),(burstY+sin(a)*r).toFloat(),(burstX+cos(a)*r+3*resources.displayMetrics.density).toFloat(),(burstY+sin(a)*r+6*resources.displayMetrics.density).toFloat(),paint)}
        }
    }
    override fun close(){closed=true;protectedViews.clear();abyssReadability.clear();setActive(false)}
}
