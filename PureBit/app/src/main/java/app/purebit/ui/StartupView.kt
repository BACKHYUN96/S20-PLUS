package app.purebit.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.view.animation.LinearInterpolator
import app.purebit.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/** Silent branding only: no simulated hardware test, success indicator or audio side effects. */
class StartupView(context: Context, private val theme:ThemeSpec, private val mode:IntroMode,
    private val landing:RectF?=null, private val sceneStyle:IntroScene=IntroScene.THEME, private val onFinished: () -> Unit) : View(context) {
    constructor(context: Context) : this(context, BuiltInThemes.original, IntroMode.FULL, null, IntroScene.THEME, {})
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val rect = RectF()
    private val density = resources.displayMetrics.density
    private var animator: ValueAnimator? = null
    private var progress = 0f
    private var finished = false
    private var requested = false
    private val cyan = theme.accent
    private val violet = theme.secondary
    private val scene by lazy {HwahyunScene(context)}
    private val abyss by lazy {app.purebit.theme.abyss.AbyssScene(context)}
    fun configureAbyss(value:app.purebit.theme.abyss.AbyssEffects){if(theme.renderer==ThemeRenderer.ABYSS)abyss.configure(value)}
    private val garden by lazy {GardenPainter(context)}
    private val petalPath=Path().apply {moveTo(0f,1f);cubicTo(-1f,.2f,-1f,-1f,0f,-.7f);cubicTo(1f,-1f,1f,.2f,0f,1f);close()}
    private val signature=SignaturePainter(context)

    init {
        contentDescription = "${theme.name} · 김백현 & Codex · 터치하여 건너뛰기"
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        isClickable = true
        setOnClickListener { cancel() }
    }

    fun start() {
        if (requested || finished) return
        requested = true
        post {
            if (finished) return@post
            val accessibility = context.getSystemService(AccessibilityManager::class.java)
            if (mode==IntroMode.OFF || !ValueAnimator.areAnimatorsEnabled() || accessibility?.isTouchExplorationEnabled == true) {
                finish()
                return@post
            }
            animator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = IntroTimeline.duration(theme,mode,sceneStyle)
                interpolator = LinearInterpolator()
                addUpdateListener {
                    progress = it.animatedValue as Float
                    alpha = 1f - stage(0.88f, 1f)
                    invalidate()
                }
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) { finish() }
                })
                start()
            }
        }
    }

    fun cancel() {
        animator?.cancel()
        finish()
    }

    private fun finish() {
        if (finished) return
        finished = true
        animator = null
        onFinished()
    }

    override fun onDetachedFromWindow() {
        animator?.removeAllListeners()
        animator?.cancel()
        animator = null
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        if(theme.renderer==ThemeRenderer.ABYSS){
            val seconds=GardenClock.seconds()
            abyss.background(canvas,w,h,seconds,false,stage(0f,.85f))
            abyss.particles(canvas,w,h,seconds,theme,false,1.4f)
            canvas.save();paint.color=theme.background;paint.alpha=((1-stage(0f,.16f))*255).toInt();canvas.drawRect(0f,0f,w,h,paint);canvas.restore()
            val branding=1-stage(.68f,.83f)
            paint.alpha=(stage(.35f,.56f)*branding*255).toInt();paint.color=theme.text;paint.typeface=android.graphics.Typeface.create("sans-serif-light",android.graphics.Typeface.NORMAL);paint.textSize=42*density;paint.textAlign=Paint.Align.CENTER
            canvas.drawText("A B Y S S",w*.5f,h*.57f,paint)
            paint.textSize=11*density;paint.color=theme.accent;paint.alpha=(stage(.46f,.66f)*branding*230).toInt();canvas.drawText("B I T W I R E  ·  심 연",w*.5f,h*.62f,paint)
            signature.draw(canvas,w*.5f,h*.77f,w*.82f,30*density,stage(.52f,.69f),theme,branding)
            val reveal=stage(.70f,.85f)
            if(reveal>0f){
                val destination=landing?.takeIf {it.width()>0&&it.height()>0}
                val circle=app.purebit.theme.abyss.AbyssGeometry.landing(w,h,destination?.left?:0f,destination?.top?:0f,destination?.width()?:w,destination?.height()?:h,stage(.78f,.97f))
                canvas.saveLayerAlpha(0f,0f,w,h,(255*reveal).toInt())
                abyss.hero(canvas,w,h,seconds,theme,null,0f,true,hideAlbum=true,portalOverride=circle)
                canvas.restore()
            }
            abyss.particles(canvas,w,h,seconds,theme,true,1.4f)
            return
        }
        if(theme.renderer==ThemeRenderer.HWAHYUN){drawHwahyun(canvas,w,h);return}
        canvas.drawColor(Color.rgb(7, 10, 15))
        val cx = w / 2; val cy = h * 0.43f
        val radius = min(w * 0.24f, 100 * density)
        val circuit = stage(0.1f, 0.5f)
        val ring = stage(0.29f, 0.63f)
        val logo = stage(0.57f, 0.79f)

        paint.style = Paint.Style.STROKE; paint.strokeWidth = density
        for (i in 0..5) {
            val angle = i * PI / 3 - PI / 2
            val x = cx + cos(angle).toFloat() * radius
            val y = cy + sin(angle).toFloat() * radius
            val reach = radius + circuit * w * 0.35f
            val x2 = cx + cos(angle).toFloat() * reach
            val y2 = cy + sin(angle).toFloat() * reach
            paint.color = Color.argb((circuit * 115).toInt(), 51, 239, 213)
            path.reset(); path.moveTo(x, y)
            path.lineTo(x2, y2)
            path.lineTo(x2 + (if (i % 2 == 0) 1 else -1) * radius * circuit * 0.35f, y2)
            canvas.drawPath(path, paint)
            paint.style = Paint.Style.FILL
            paint.color = Color.argb((circuit * 200).toInt(), 51, 239, 213)
            canvas.drawCircle(x2, y2, 1.8f * density, paint)
            paint.style = Paint.Style.STROKE
        }
        rect.set(cx - radius, cy - radius, cx + radius, cy + radius)
        paint.color = Color.argb((ring * 35).toInt(), 51, 239, 213); paint.strokeWidth = 10 * density
        canvas.drawArc(rect, -90f, 360 * ring, false, paint)
        paint.color = cyan; paint.strokeWidth = 2 * density
        canvas.drawArc(rect, -90f, 360 * ring, false, paint)
        rect.inset(8 * density, 8 * density)
        paint.color = Color.argb((ring * 170).toInt(), 171, 118, 255); paint.strokeWidth = density
        canvas.drawArc(rect, 90f - 180 * ring, -260 * ring, false, paint)

        val dotAngle = (-90 + 360 * ring) * PI / 180
        paint.style = Paint.Style.FILL; paint.color = cyan
        canvas.drawCircle(cx + cos(dotAngle).toFloat() * radius * ring,
            cy + sin(dotAngle).toFloat() * radius * ring, (2 + (1 - ring) * 2) * density, paint)

        if (logo > 0) {
            canvas.save()
            canvas.translate(0f, (1 - logo) * 12 * density)
            paint.style = Paint.Style.STROKE; paint.strokeCap = Paint.Cap.ROUND
            paint.strokeWidth = 3 * density; paint.color = Color.argb((logo * 255).toInt(), 51, 239, 213)
            val s = radius * 0.45f
            path.reset(); path.moveTo(cx - s, cy - s * 0.55f); path.lineTo(cx - s, cy + s * 0.55f)
            path.lineTo(cx, cy + s * 0.55f); path.lineTo(cx, cy - s * 0.55f)
            path.lineTo(cx + s, cy - s * 0.55f); path.lineTo(cx + s, cy + s * 0.55f)
            canvas.drawPath(path, paint)
            paint.strokeCap = Paint.Cap.BUTT
            paint.style = Paint.Style.FILL
            paint.typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 32 * density
            paint.color = Color.argb((logo * 255).toInt(), 229, 243, 245)
            canvas.drawText("B I T W I R E", cx, cy + radius + 54 * density, paint)
            paint.textSize = 8 * density
            paint.typeface = android.graphics.Typeface.create("monospace", android.graphics.Typeface.NORMAL)
            paint.color = Color.argb((logo * 180).toInt(), 110, 142, 154)
            canvas.drawText("MUSIC / IN MOTION", cx, cy + radius + 74 * density, paint)
            canvas.restore()
        }
        paint.color = Color.rgb(38, 64, 71); paint.style = Paint.Style.FILL
        canvas.drawRect(cx - 36 * density, h * 0.83f, cx + 36 * density, h * 0.83f + density, paint)
        paint.color = violet
        canvas.drawRect(cx - 36 * density, h * 0.83f, cx - 36 * density + 72 * density * progress,
            h * 0.83f + density, paint)
        signature.draw(canvas,cx,h*.73f,w*.82f,30*density,stage(.57f,.85f),theme)
        hint(canvas,w,h)
    }

    private fun drawHwahyun(canvas:Canvas,w:Float,h:Float){
        val seconds=GardenClock.seconds();val exit=stage(.87f,1f);val target=landing
        garden.background(canvas,w,h,seconds,theme,camera=1.045f-.045f*stage(.05f,.87f))
        val rain=sceneStyle==IntroScene.PETAL_RAIN;val strings=sceneStyle==IntroScene.STRING_BLOOM
        garden.petals(canvas,w,h,seconds,theme,if(rain)2f else 1.3f,false)
        val targetX=target?.centerX()?:w*.5f
        val targetY=if(target!=null)target.top+target.height()*.45f else h*.27f
        val endR=if(target!=null)min(target.width()*.42f,target.height()*.41f) else w*.30f
        val initialR=min(w*.31f,h*.20f)
        scene.drawStrings(canvas,w,h*.70f,theme,seconds,reveal=if(strings)stage(.04f,.48f)else stage(.14f,.45f))
        if(strings)drawStringBloom(canvas,w,h)
        scene.drawMoon(canvas,w*.5f+(targetX-w*.5f)*exit,h*.27f+(targetY-h*.27f)*exit,initialR+(endR-initialR)*exit,theme,reveal=if(strings)stage(.35f,.60f)else stage(.20f,.48f))
        paint.style=Paint.Style.FILL;paint.textAlign=Paint.Align.CENTER;paint.typeface=android.graphics.Typeface.SERIF
        paint.color=theme.text;paint.alpha=(stage(.43f,.60f)*245).toInt();paint.textSize=56*density
        canvas.drawText("花 絃",w*.5f,h*.71f,paint)
        paint.textSize=9*density;paint.color=theme.accent;paint.alpha=(stage(.50f,.65f)*220).toInt()
        canvas.drawText("H W A H Y U N",w*.5f,h*.76f,paint)
        paint.textSize=8*density;canvas.drawText("B I T W I R E",w*.5f,h*.79f,paint)
        signature.draw(canvas,w*.5f,h*.875f,w*.82f,33*density,stage(.64f,.85f),theme)
        garden.petals(canvas,w,h,seconds,theme,if(rain)2f else 1.3f,true)
        if(rain)drawPetalCurtain(canvas,w,h)
        paint.color=theme.background;paint.alpha=((1-stage(0f,.14f))*255).toInt();canvas.drawRect(0f,0f,w,h,paint)
        hint(canvas,w,h)
    }
    private fun drawPetalCurtain(c:Canvas,w:Float,h:Float){
        val opening=stage(.02f,.62f);val veil=1-stage(.30f,.66f)
        if(veil<=0f)return
        for(i in 0 until 38){
            val seed=(i*.618034f)%1f
            val x=w*(seed+sin(progress*5+i)*.12f)
            val y=h*((i*.381966f)%1f-.25f+opening*.95f)
            val size=(16+i%7*6)*density
            paint.style=Paint.Style.FILL;paint.shader=null;paint.color=if(i%3==0)theme.text else theme.accent;paint.alpha=(veil*(75+i%4*32)).toInt()
            c.save();c.translate(x,y);c.rotate(i*47f+opening*120f);c.scale(size,size);c.drawPath(petalPath,paint);c.restore()
        }
        paint.alpha=255
    }
    private fun drawStringBloom(c:Canvas,w:Float,h:Float){
        val appear=stage(.08f,.40f)*(1-stage(.66f,.86f))
        for(i in 0 until 9){
            val reveal=stage(.08f+i*.025f,.24f+i*.025f)
            val x=w*(.12f+i*.095f);val y=h*(.48f+sin(i*.8f)*.08f)
            paint.style=Paint.Style.STROKE;paint.strokeWidth=density;paint.color=theme.accent;paint.alpha=(appear*150).toInt()
            path.reset();path.moveTo(x,h*.85f);path.quadTo(x-w*.10f,y+h*.08f,x,y);c.drawPath(path,paint)
            for(j in 0 until 5){paint.style=Paint.Style.FILL;paint.alpha=(appear*reveal*180).toInt()
                c.save();c.translate(x,y);c.rotate(j*72f+progress*18f);c.scale((5+reveal*7)*density,(5+reveal*7)*density);c.drawPath(petalPath,paint);c.restore()}
        }
        paint.alpha=255
    }
    private fun hint(canvas:Canvas,w:Float,h:Float){
        paint.style=Paint.Style.FILL;paint.textAlign=Paint.Align.CENTER
        paint.typeface=android.graphics.Typeface.create("sans-serif",android.graphics.Typeface.NORMAL);paint.textSize=10*density
        paint.color=theme.muted;paint.alpha=140
        val bottom=rootWindowInsets?.getInsets(android.view.WindowInsets.Type.systemBars() or android.view.WindowInsets.Type.displayCutout())?.bottom?:0
        canvas.drawText("터치하여 건너뛰기",w*.5f,min(h*.965f,h-bottom-12*density),paint)
        paint.textAlign=Paint.Align.LEFT;paint.alpha=255
    }

    private fun stage(from: Float, until: Float): Float {
        val t = max(0f, min(1f, (progress - from) / (until - from)))
        return t * t * (3 - 2 * t)
    }
}
