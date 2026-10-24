package app.purebit.ui

import android.animation.ValueAnimator
import android.content.Context
import android.view.accessibility.AccessibilityManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.os.SystemClock
import android.view.Choreographer
import android.view.View
import app.purebit.core.PreparedPcm
import app.purebit.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/** 60 FPS target deck. The spectrum reads a copy of SOURCE PCM, not microphone/DAC samples. */
class DeckView(context: Context) : View(context), AutoCloseable {
    private val ink = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bounds = RectF()
    private val artSource = android.graphics.Rect()
    private val path = Path()
    private val analyzer = SpectrumAnalyzer()
    private val levels = FloatArray(64)
    private val peaks = FloatArray(64)
    private var theme=BuiltInThemes.original
    private val cyan get()=theme.accent
    private val violet get()=theme.secondary
    private val muted get()=theme.muted
    private val hwahyun by lazy {HwahyunScene(context)}
    private val abyss by lazy {app.purebit.theme.abyss.AbyssScene(context)}
    private var abyssEffects=app.purebit.theme.abyss.AbyssEffects()
    fun configureAbyss(value:app.purebit.theme.abyss.AbyssEffects){abyssEffects=value;if(theme.renderer==ThemeRenderer.ABYSS)abyss.configure(value);if(value.waves==0f)abyssRipples.reset();invalidate()}
    var hideAbyssAlbum=false
    var onAbyssAlbumClick:((RectF)->Unit)?=null
    fun abyssAlbumBounds():RectF {
        val circle=app.purebit.theme.abyss.AbyssGeometry.album(app.purebit.theme.abyss.AbyssGeometry.portal(width.toFloat(),height.toFloat(),gardenLandscape))
        return RectF(circle.x-circle.radius,circle.y-circle.radius,circle.x+circle.radius,circle.y+circle.radius)
    }
    override fun performClick():Boolean {
        if(theme.renderer==ThemeRenderer.ABYSS&&artwork!=null){onAbyssAlbumClick?.invoke(abyssAlbumBounds());super.performClick();return true}
        return super.performClick()
    }
    private var abyssTouch=false
    private var abyssDownX=0f;private var abyssDownY=0f
    private fun abyssAlbumHit(x:Float,y:Float)=app.purebit.theme.abyss.AbyssGeometry.albumHit(x,y,app.purebit.theme.abyss.AbyssGeometry.portal(width.toFloat(),height.toFloat(),gardenLandscape))
    override fun onTouchEvent(event:android.view.MotionEvent):Boolean {
        if(theme.renderer!=ThemeRenderer.ABYSS||artwork==null)return super.onTouchEvent(event)
        when(event.actionMasked){
            android.view.MotionEvent.ACTION_DOWN->{abyssTouch=abyssAlbumHit(event.x,event.y);abyssDownX=event.x;abyssDownY=event.y;return abyssTouch}
            android.view.MotionEvent.ACTION_MOVE->{if(kotlin.math.abs(event.x-abyssDownX)+kotlin.math.abs(event.y-abyssDownY)>android.view.ViewConfiguration.get(context).scaledTouchSlop)abyssTouch=false;return true}
            android.view.MotionEvent.ACTION_UP->{if(abyssTouch&&abyssAlbumHit(event.x,event.y))performClick();abyssTouch=false;return true}
            android.view.MotionEvent.ACTION_CANCEL->{abyssTouch=false;return true}
        };return true
    }
    var gardenEmbedded=false
    var gardenLandscape=false
    var lyricBackdrop=false
    private var sceneSeconds=0f
    private var petalsScale=1f
    private var themeReaction=true
    private val gardenPresence=GardenPresence()
    private var albumColorEnabled=true
    private val density = resources.displayMetrics.density
    private var pcm: PreparedPcm? = null
    private var sourcePosition = 0L
    private var positionUpdated = 0L
    private var playing = false
    private var active = false
    private var closed = false
    private var framePosted = false
    private var lastFrame = 0L
    private var nextFrame = 0L
    private var lastAnalysis = 0L
    private var rotation = 0f
    private val pulse = BassPulse()
    private val abyssRipples=app.purebit.theme.abyss.AbyssRipples()
    private var pulseAmount = 0f
    private var flowAmount = 0f
    private var flowPhase = 0f
    private val tracePoint = FloatArray(2)
    private val geometryValues = FloatArray(3)
    private var spectrumEnabled = true
    private var bassPulseEnabled = true
    private var circuitFlowEnabled = true
    var animationsSuppressed = false
    private var motionAllowed = true
    private var highLabel = "20k"
    private var artwork: Bitmap? = null
    private var barsShader: Shader? = null
    private var platterShader: Shader? = null
    private var ringShader: Shader? = null
    private var ambientShader: Shader? = null
    var immersive: Boolean = false
        set(value) { field = value; rebuildShaders(); invalidate() }

    private val frames = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            framePosted = false
            if (!active || closed || !isAttachedToWindow) return
            // A phase accumulator yields ~60 FPS on 60/90/120 Hz panels instead of
            // accidentally dropping to 45 FPS on a 90 Hz display.
            if (lastFrame == 0L || frameTimeNanos + 500_000L >= nextFrame) {
                val dt = if (lastFrame == 0L) 1f / 60 else ((frameTimeNanos - lastFrame) / 1e9f).coerceAtMost(0.1f)
                lastFrame = frameTimeNanos
                nextFrame = if (nextFrame == 0L || frameTimeNanos - nextFrame > 50_000_000L)
                    frameTimeNanos + 16_666_667L else nextFrame + 16_666_667L
                val now = SystemClock.uptimeMillis()
                gardenPresence.update(playing,dt,motionAllowed)
                if(motionAllowed)sceneSeconds+=dt
                val source = pcm
                if (playing && source != null && (spectrumEnabled || (motionAllowed && (bassPulseEnabled || circuitFlowEnabled || (theme.renderer==ThemeRenderer.HWAHYUN && themeReaction)))) && now - lastAnalysis >= 40) {
                    analyzer.request(source, (sourcePosition + now - positionUpdated).coerceAtMost(source.durationMs))
                    lastAnalysis = now
                }
                val result = analyzer.result
                var low = 0f
                for (i in levels.indices) {
                    val target = if (playing) result.bands[i] else 0f
                    val speed = if (target > levels[i]) 20f else 6f
                    levels[i] += (target - levels[i]) * (1 - exp(-speed * dt))
                    peaks[i] = max(levels[i], peaks[i] - dt * 0.24f)
                    if (i < 16) low += levels[i]
                }
                pulseAmount = pulse.update(if (playing) low / 16 else 0f, dt, bassPulseEnabled && motionAllowed)
                abyssRipples.update(low/16,dt,theme.renderer==ThemeRenderer.ABYSS&&playing&&bassPulseEnabled&&themeReaction&&motionAllowed&&abyssEffects.waves>0f)
                val flowTarget = if (playing && circuitFlowEnabled && motionAllowed) (result.rms * 3f).coerceIn(0f, 1f) else 0f
                flowAmount += (flowTarget - flowAmount) * (1 - exp(-6f * dt))
                if (playing && motionAllowed) {
                    rotation = (rotation + dt * 20f) % 360
                    if (circuitFlowEnabled && flowAmount > .001f) flowPhase = (flowPhase + dt * (.12f + flowAmount * .32f)) % 1f
                }
                invalidate()
            }
            scheduleFrame()
        }
    }

    init {
        contentDescription = "BITWIRE deck and 64-band source PCM spectrum. This does not measure the DAC output."
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        isFocusable = false
    }

    fun update(pcm: PreparedPcm?, positionMs: Long, playing: Boolean) {
        if (this.pcm !== pcm) {
            analyzer.suspend()
            levels.fill(0f); peaks.fill(0f); lastAnalysis = 0L
            pulse.update(0f, 0f, false); pulseAmount=0f; flowAmount=0f
            abyssRipples.reset()
            highLabel="${min(20000, (pcm?.format?.rate ?: 48000)/2)/1000}k"
        }
        this.pcm = pcm
        sourcePosition = positionMs.coerceAtLeast(0)
        positionUpdated = SystemClock.uptimeMillis()
        this.playing = playing
        if (!active) invalidate()
    }

    fun setEffects(spectrum: Boolean, bassPulse: Boolean, circuitFlow: Boolean) {
        spectrumEnabled=spectrum; bassPulseEnabled=bassPulse; circuitFlowEnabled=circuitFlow
        motionAllowed=!animationsSuppressed && ValueAnimator.areAnimatorsEnabled() && context.getSystemService(AccessibilityManager::class.java)?.isTouchExplorationEnabled != true
        if(!bassPulse || !motionAllowed){pulse.update(0f,0f,false);pulseAmount=0f;abyssRipples.reset()}
        if(!motionAllowed&&theme.renderer==ThemeRenderer.ABYSS)abyss.settleArtwork()
        if(!circuitFlow || !motionAllowed)flowAmount=0f
        if(!spectrum && (!motionAllowed || (!bassPulse && !circuitFlow && !(theme.renderer==ThemeRenderer.HWAHYUN && themeReaction))))analyzer.suspend()
        invalidate()
    }

    fun setArtwork(bitmap: Bitmap?) { if(theme.renderer==ThemeRenderer.ABYSS)abyss.setArtwork(bitmap,GardenClock.seconds(),active&&motionAllowed);artwork = bitmap;refreshMoonColor();invalidate() }
    fun setAlbumColorEnabled(value:Boolean){if(albumColorEnabled!=value){albumColorEnabled=value;refreshMoonColor()}}
    private fun refreshMoonColor(){
        if(theme.renderer!=ThemeRenderer.HWAHYUN)return
        val bitmap=artwork
        val color=if(albumColorEnabled&&bitmap!=null&&!bitmap.isRecycled){
            // Bounded 12x12 sampling, only on artwork/settings changes, never in a frame callback.
            val samples=IntArray(144){i->bitmap.getPixel(((i%12+.5f)*bitmap.width/12).toInt().coerceAtMost(bitmap.width-1),((i/12+.5f)*bitmap.height/12).toInt().coerceAtMost(bitmap.height-1))}
            AlbumMoonColor.select(samples,theme.accent)
        }else theme.accent
        hwahyun.setMoonColor(color)
    }
    fun setTheme(value:ThemeSpec,petalsScale:Float=1f,reaction:Boolean=true){
        val enteringAbyss=theme.renderer!=value.renderer&&value.renderer==ThemeRenderer.ABYSS
        theme=value;this.petalsScale=petalsScale;themeReaction=reaction
        if(!reaction)abyssRipples.reset()
        if(enteringAbyss){abyss.configure(abyssEffects);abyss.setArtwork(artwork,GardenClock.seconds(),false)}
        refreshMoonColor();rebuildShaders();invalidate()
    }

    fun setActive(value: Boolean) {
        if (closed || active == value) return
        active = value
        lastFrame = 0L
        nextFrame = 0L
        if (value) scheduleFrame() else {
            abyssRipples.reset()
            if(theme.renderer==ThemeRenderer.ABYSS)abyss.settleArtwork()
            Choreographer.getInstance().removeFrameCallback(frames)
            framePosted = false
            analyzer.suspend()
        }
    }

    private fun scheduleFrame() {
        if (active && !framePosted && isAttachedToWindow) {
            framePosted = true
            Choreographer.getInstance().postFrameCallback(frames)
        }
    }

    override fun onAttachedToWindow() { super.onAttachedToWindow(); scheduleFrame() }
    override fun onDetachedFromWindow() {
        Choreographer.getInstance().removeFrameCallback(frames)
        framePosted = false
        analyzer.suspend()
        super.onDetachedFromWindow()
    }

    override fun close() {
        if (closed) return
        setActive(false)
        closed = true
        analyzer.close()
        artwork = null
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) { rebuildShaders() }

    private fun geometry(): FloatArray {
        val w = width.toFloat(); val h = height.toFloat()
        val radius = min(w * if (immersive) 0.35f else 0.29f, h * if (immersive) 0.29f else 0.31f)
        geometryValues[0]=w/2;geometryValues[1]=h * if(immersive) .40f else .36f;geometryValues[2]=radius
        return geometryValues
    }

    private fun rebuildShaders() {
        if (width == 0 || height == 0) return
        val g = geometry(); val cx = g[0]; val cy = g[1]; val r = g[2]
        platterShader = RadialGradient(cx - r * 0.25f, cy - r * 0.4f, r * 1.5f,
            intArrayOf(Color.rgb(39, 47, 53), Color.rgb(17, 22, 27), Color.rgb(5, 9, 13)),
            floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP)
        ringShader = SweepGradient(cx, cy, intArrayOf(cyan, Color.rgb(28, 72, 80), violet, Color.rgb(44, 34, 63), cyan), null)
        barsShader = LinearGradient(0f, height * 0.92f, 0f, height * 0.67f,
            intArrayOf(cyan, Color.rgb(61, 164, 215), violet), null, Shader.TileMode.CLAMP)
        ambientShader = RadialGradient(cx, cy, max(width, height) * 0.65f,
            intArrayOf(Color.rgb(13, 30, 35), Color.rgb(8, 13, 20), Color.rgb(7, 10, 15)), null, Shader.TileMode.CLAMP)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat(); val h = height.toFloat()
        if (w < 1 || h < 1) return
        if(theme.renderer==ThemeRenderer.ABYSS){
            abyss.hero(canvas,w,h,if(motionAllowed)GardenClock.seconds() else 0f,theme,artwork,0f,gardenEmbedded,gardenLandscape,hideAbyssAlbum||lyricBackdrop,ripples=abyssRipples)
            return
        }
        if(theme.renderer==ThemeRenderer.HWAHYUN){
            if(lyricBackdrop){
                hwahyun.drawLyricBackdrop(canvas,w,h,theme,artwork,sceneSeconds,if(themeReaction)pulseAmount else 0f,motionAllowed,petalsScale,if(motionAllowed)gardenPresence.value else 1f)
                return
            }
            if(gardenLandscape){
                hwahyun.drawLandscape(canvas,w,h,theme,artwork,sceneSeconds,if(themeReaction&&motionAllowed)levels else null,
                    if(themeReaction)pulseAmount else 0f,if(themeReaction)flowAmount else 0f,motionAllowed,petalsScale,if(motionAllowed)gardenPresence.value else 1f)
                return
            }
            hwahyun.draw(canvas,w,h,theme,artwork,sceneSeconds,if(themeReaction&&motionAllowed)levels else null,
                if(themeReaction)pulseAmount else 0f,if(themeReaction)flowAmount else 0f,motionAllowed,petalsScale,embedded=gardenEmbedded,presence=if(motionAllowed)gardenPresence.value else 1f)
            return
        }
        val g = geometry(); val cx = g[0]; val cy = g[1]; val r = g[2]
        fill(Color.rgb(7, 10, 15)); ink.shader = ambientShader
        canvas.drawRect(0f, 0f, w, h, ink); ink.shader = null

        val edge = 18 * density
        drawLabel(canvas, "DECK / 01", edge, 19 * density, 8.5f, muted)
        drawLabel(canvas, "SOURCE PCM", w - edge, 19 * density, 8.5f, cyan, Paint.Align.RIGHT)
        circuits(canvas, cx, cy, r, edge)

        // Source low-frequency energy expands the halo; no hardware/verification animation.
        for (i in 7 downTo 1) {
            stroke(colorAlpha(cyan,6 + (pulseAmount * 30).toInt()), i * density)
            canvas.drawCircle(cx, cy, r + (7 + i) * density + pulseAmount * 15 * density, ink)
        }
        if(pulseAmount>.005f){
            stroke(colorAlpha(violet,(pulseAmount*70).toInt()),2*density)
            canvas.drawCircle(cx,cy,r+19*density+pulseAmount*22*density,ink)
        }
        fill(Color.rgb(10, 15, 20)); canvas.drawCircle(cx, cy, r + 7 * density, ink)
        stroke(Color.rgb(46, 61, 71), density); canvas.drawCircle(cx, cy, r + 6 * density, ink)
        stroke(cyan, 2 * density); ink.shader = ringShader
        canvas.drawCircle(cx, cy, r + 2 * density, ink); ink.shader = null

        fill(Color.BLACK); ink.shader = platterShader; canvas.drawCircle(cx, cy, r, ink); ink.shader = null
        for (i in 0 until 28) {
            val rad = r * (0.49f + i * 0.016f)
            stroke(if (i % 4 == 0) Color.argb(120, 53, 63, 74) else Color.argb(90, 30, 41, 48), 0.5f * density)
            canvas.drawCircle(cx, cy, rad, ink)
        }
        bounds.set(cx - r * 0.94f, cy - r * 0.94f, cx + r * 0.94f, cy + r * 0.94f)
        stroke(Color.argb(60, 177, 206, 211), 6 * density)
        canvas.drawArc(bounds, 210f, 48f, false, ink)
        stroke(Color.argb(25, 199, 172, 255), 10 * density)
        canvas.drawArc(bounds, 25f, 42f, false, ink)

        // Instrument-like micro ticks with one moving cue marker.
        for (i in 0 until 60) {
            val angle = i * PI / 30 - PI / 2
            val outer = r + 10 * density
            val inner = outer - if (i % 5 == 0) 4 * density else 2 * density
            stroke(if (i % 5 == 0) Color.rgb(91, 112, 122) else Color.rgb(43, 58, 68), 0.7f * density)
            canvas.drawLine(cx + cos(angle).toFloat() * inner, cy + sin(angle).toFloat() * inner,
                cx + cos(angle).toFloat() * outer, cy + sin(angle).toFloat() * outer, ink)
        }
        val cue = (rotation - 90) * PI / 180
        fill(cyan)
        canvas.drawCircle(cx + cos(cue).toFloat() * (r + 2 * density),
            cy + sin(cue).toFloat() * (r + 2 * density), 2.3f * density, ink)

        val labelRadius = r * 0.43f
        fill(Color.rgb(18, 28, 34)); canvas.drawCircle(cx, cy, labelRadius + density, ink)
        val art = artwork
        if (art != null && !art.isRecycled) {
            canvas.save()
            path.reset(); path.addCircle(cx, cy, labelRadius, Path.Direction.CW); canvas.clipPath(path)
            canvas.rotate(rotation, cx, cy)
            val side = min(art.width, art.height)
            artSource.set((art.width - side) / 2, (art.height - side) / 2,
                (art.width + side) / 2, (art.height + side) / 2)
            bounds.set(cx - labelRadius, cy - labelRadius, cx + labelRadius, cy + labelRadius)
            fill(Color.WHITE); canvas.drawBitmap(art, artSource, bounds, ink)
            canvas.restore()
            fill(Color.argb(35, 4, 9, 15)); canvas.drawCircle(cx, cy, labelRadius, ink)
        } else {
            stroke(Color.rgb(30, 69, 74), density)
            canvas.drawCircle(cx, cy, labelRadius - 4 * density, ink)
            drawLogo(canvas, cx, cy - 7 * density, r * 0.28f)
            drawLabel(canvas, "BITWIRE", cx, cy + 17 * density, 8f, Color.rgb(199, 222, 224), Paint.Align.CENTER)
            drawLabel(canvas, "PCM ENGINE", cx, cy + 27 * density, 5.5f, muted, Paint.Align.CENTER)
        }
        fill(Color.rgb(9, 13, 17)); canvas.drawCircle(cx, cy, 3 * density, ink)
        stroke(Color.argb(180, 127, 154, 162), density); canvas.drawCircle(cx, cy, 3 * density, ink)

        // Tonearm-shaped circuitry is a visual motif, not an audio routing claim.
        val ax = cx + r * 1.23f; val ay = cy - r * 0.68f
        stroke(Color.rgb(60, 75, 89), 4 * density)
        path.reset(); path.moveTo(ax, ay); path.lineTo(ax - r * 0.1f, cy + r * 0.16f)
        path.lineTo(cx + r * 0.73f, cy + r * 0.48f); canvas.drawPath(path, ink)
        stroke(Color.argb(170, 106, 146, 158), density); canvas.drawPath(path, ink)
        fill(Color.rgb(19, 31, 41)); canvas.drawCircle(ax, ay, 7 * density, ink)
        stroke(violet, density); canvas.drawCircle(ax, ay, 4 * density, ink)
        canvas.save(); canvas.rotate(-39f, cx + r * 0.73f, cy + r * 0.48f)
        bounds.set(cx + r * 0.73f - 3 * density, cy + r * 0.48f - 7 * density,
            cx + r * 0.73f + 3 * density, cy + r * 0.48f + 7 * density)
        fill(Color.rgb(80, 105, 115)); canvas.drawRoundRect(bounds, density, density, ink); canvas.restore()

        spectrum(canvas, edge, w - edge, h)
    }

    private fun spectrum(canvas: Canvas, left: Float, right: Float, h: Float) {
        val bottom = h - 19 * density
        val top = max(h * if (immersive) 0.73f else 0.73f, bottom - 73 * density)
        val step = (right - left) / 64
        drawLabel(canvas, "SOURCE SPECTRUM / 64", left, top - 9 * density, 6.5f, muted)
        drawLabel(canvas, if (!spectrumEnabled) "OFF" else if (playing) "LIVE" else "STANDBY", right, top - 9 * density, 6.5f,
            if (playing) cyan else muted, Paint.Align.RIGHT)
        if(!spectrumEnabled){
            stroke(Color.rgb(29,48,59),density);canvas.drawLine(left,bottom,right,bottom,ink)
            return
        }
        for (i in levels.indices) {
            val x = left + i * step
            val bar = (step * 0.68f).coerceAtLeast(0.6f)
            stroke(Color.argb(50, 69, 88, 103), 0.5f * density)
            canvas.drawLine(x, top, x, bottom, ink)
            fill(cyan); ink.shader = barsShader
            bounds.set(x, bottom - max(density, levels[i] * (bottom - top)), x + bar, bottom)
            canvas.drawRoundRect(bounds, bar / 3, bar / 3, ink); ink.shader = null
            if (peaks[i] > 0.035f) {
                stroke(Color.argb(190, 210, 242, 242), density)
                val peakY = bottom - peaks[i] * (bottom - top) - 2 * density
                canvas.drawLine(x, peakY, x + bar, peakY, ink)
            }
        }
        val y = h - 6 * density
        drawLabel(canvas, "24 Hz", left, y, 5.5f, muted)
        drawLabel(canvas, "100", left + (right - left) * 0.215f, y, 5.5f, muted, Paint.Align.CENTER)
        drawLabel(canvas, "1k", left + (right - left) * 0.55f, y, 5.5f, muted, Paint.Align.CENTER)
        drawLabel(canvas, highLabel, right, y, 5.5f, muted, Paint.Align.RIGHT)
    }

    private fun circuits(canvas: Canvas, cx: Float, cy: Float, r: Float, edge: Float) {
        for (sideIndex in 0..1) {
            val side=if(sideIndex==0)-1 else 1
            val end = if (side < 0) edge else width - edge
            for (i in 0..2) {
                val y = cy - r * 0.54f + i * 9 * density
                val mid = cx + side * (r + 22 * density + i * 3 * density)
                stroke(Color.argb(120, 27, 62, 71), 0.7f * density)
                path.reset(); path.moveTo(end, y + 12 * density); path.lineTo(mid, y + 12 * density)
                path.lineTo(mid - side * 12 * density, y); path.lineTo(cx + side * (r + 10 * density), y)
                canvas.drawPath(path, ink)
                fill(if (i == 0 && playing) cyan else Color.rgb(44, 81, 89))
                canvas.drawCircle(end, y + 12 * density, 1.4f * density, ink)
                if(circuitFlowEnabled && motionAllowed && flowAmount>.003f) {
                    for(tail in 5 downTo 0){
                        val fraction=((flowPhase+i*.23f+sideIndex*.17f-tail*.023f)%1f+1f)%1f
                        CircuitFlow.point(fraction,cx+side*(r+10*density),y,mid-side*12*density,y,mid,y+12*density,end,y+12*density,tracePoint)
                        val alpha=(flowAmount*(170-tail*23)).toInt().coerceIn(0,210)
                        fill(if(sideIndex==0)colorAlpha(cyan,alpha) else colorAlpha(violet,alpha))
                        canvas.drawCircle(tracePoint[0],tracePoint[1],(if(tail==0)2.2f else 1.2f)*density,ink)
                    }
                }
            }
        }
        drawLabel(canvas, "01", edge, cy + r * 0.7f, 6f, muted)
        drawLabel(canvas, "∞", width - edge, cy + r * 0.7f, 9f, muted, Paint.Align.RIGHT)
    }

    private fun drawLogo(canvas: Canvas, x: Float, y: Float, size: Float) {
        stroke(cyan, 2 * density)
        ink.strokeCap = Paint.Cap.ROUND
        path.reset(); path.moveTo(x - size * 0.55f, y - size * 0.35f)
        path.lineTo(x - size * 0.55f, y + size * 0.35f); path.lineTo(x, y + size * 0.35f)
        path.lineTo(x, y - size * 0.35f); path.lineTo(x + size * 0.55f, y - size * 0.35f)
        path.lineTo(x + size * 0.55f, y + size * 0.35f); canvas.drawPath(path, ink)
        ink.strokeCap = Paint.Cap.BUTT
    }

    private fun drawLabel(canvas: Canvas, text: String, x: Float, y: Float, sp: Float, color: Int,
                          align: Paint.Align = Paint.Align.LEFT) {
        fill(color); ink.textAlign = align; ink.textSize = sp * density
        ink.typeface = android.graphics.Typeface.MONOSPACE
        canvas.drawText(text, x, y, ink)
        ink.textAlign = Paint.Align.LEFT
    }
    private fun colorAlpha(color:Int,alpha:Int)=(color and 0xffffff) or (alpha.coerceIn(0,255) shl 24)
    private fun fill(color: Int) { ink.shader = null; ink.style = Paint.Style.FILL; ink.color = color; ink.strokeWidth = density }
    private fun stroke(color: Int, width: Float) { ink.shader = null; ink.style = Paint.Style.STROKE; ink.color = color; ink.strokeWidth = width }
}
