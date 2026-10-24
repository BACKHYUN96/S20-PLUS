package app.purebit.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.widget.SeekBar
import app.purebit.core.PreparedPcm
import app.purebit.theme.*
import java.io.RandomAccessFile
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.max

/** Standard accessible SeekBar interaction, drawn using an independent read-only PCM overview. */
class WaveformSeekBar(context: Context) : SeekBar(context), AutoCloseable {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val silkThumb=Path().apply {moveTo(0f,1f);cubicTo(-1.1f,.1f,-.8f,-1f,0f,-.6f);cubicTo(.8f,-1f,1f,.1f,0f,1f);close()}
    private val density = resources.displayMetrics.density
    private val abyssPainter=app.purebit.theme.abyss.AbyssSeekPainter(density)
    private val worker = Executors.newSingleThreadExecutor { task ->
        Thread(task, "BitwireWaveform").apply { isDaemon = true; priority = Thread.MIN_PRIORITY }
    }
    private val handler = Handler(Looper.getMainLooper())
    private val epoch = AtomicLong()
    private var source: PreparedPcm? = null
    private var waveform: WaveformData? = null
    private var active = false
    private var loading = false
    private var failed = false
    private var theme=BuiltInThemes.original
    fun setTheme(value:ThemeSpec){theme=value;invalidate()}
    private fun tint(c:Int,a:Int)=(c and 0xffffff) or (a.coerceIn(0,255) shl 24)
    @Volatile private var closed = false
    /** SeekBar also calls onStopTrackingTouch on ACTION_CANCEL. Never commit that gesture. */
    var touchCancelled = false
        private set
    var touchGestureActive = false
        private set
    private var loopA: Float? = null
    private var loopB: Float? = null
    var waveformEnabled = true
        set(value) {
            if (field == value) return
            field = value
            if (!value) cancelRead() else startIfNeeded()
            invalidate()
        }
    init {
        contentDescription = "재생 위치 · 원본 PCM 에너지 파형 · 놓으면 이동"
        layoutDirection = LAYOUT_DIRECTION_LTR
        setPadding((12 * density).toInt(), 0, (12 * density).toInt(), 0)
    }
    fun setSource(value: PreparedPcm?) {
        if (source === value) return
        cancelRead(); source = value; waveform = null; failed = false
        startIfNeeded(); invalidate()
    }
    fun setLoop(a: Long?, b: Long?, durationMs: Long) {
        val nextA = if (durationMs > 0) a?.toFloat()?.div(durationMs) else null
        val nextB = if (durationMs > 0) b?.toFloat()?.div(durationMs) else null
        if (nextA == loopA && nextB == loopB) return
        loopA = nextA; loopB = nextB; invalidate()
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) { touchCancelled = false; touchGestureActive = true }
        if (event.actionMasked == MotionEvent.ACTION_CANCEL) touchCancelled = true
        if (event.actionMasked == MotionEvent.ACTION_DOWN && isEnabled) parent?.requestDisallowInterceptTouchEvent(true)
        val handled = super.onTouchEvent(event)
        if (handled && event.actionMasked == MotionEvent.ACTION_UP && !touchCancelled) performClick()
        if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
            touchGestureActive = false; parent?.requestDisallowInterceptTouchEvent(false)
        }
        return handled
    }
    override fun performClick(): Boolean = super.performClick()
    fun setActive(value: Boolean) {
        if (active == value || closed) return
        active = value
        if (!value) cancelRead() else startIfNeeded()
    }
    private fun cancelRead() { epoch.incrementAndGet(); loading = false }
    private fun startIfNeeded() {
        val pcm = source ?: return
        if (closed || !active || !waveformEnabled || loading || failed || waveform != null) return
        loading = true
        val token = epoch.get()
        try {
            worker.execute {
                if (closed || token != epoch.get()) return@execute
                val result = runCatching {
                    val accumulator = WaveformAccumulator(pcm.format, pcm.frames)
                    RandomAccessFile(pcm.file, "r").use { file ->
                        check(file.length() == pcm.bytes) { "Source length changed" }
                        val bytes = ByteArray(pcm.format.frameBytes * 8192)
                        var remaining = pcm.bytes
                        while (remaining > 0) {
                            check(!closed && token == epoch.get() && !Thread.currentThread().isInterrupted) { "Waveform cancelled" }
                            val n = minOf(remaining, bytes.size.toLong()).toInt()
                            file.readFully(bytes, 0, n)
                            accumulator.append(bytes, n)
                            remaining -= n
                        }
                    }
                    accumulator.finish()
                }
                handler.post {
                    if (!closed && token == epoch.get() && source === pcm) {
                        loading = false; waveform = result.getOrNull(); failed = result.isFailure
                        invalidate()
                    }
                }
            }
        } catch (_: java.util.concurrent.RejectedExecutionException) { loading = false }
    }
    override fun onDraw(canvas: Canvas) {
        if(theme.renderer==ThemeRenderer.ABYSS) {
            val left=paddingLeft.toFloat();val span=(width-paddingLeft-paddingRight).toFloat().coerceAtLeast(1f)
            abyssPainter.draw(canvas,left,left+span,height.toFloat(),if(max>0)progress.toFloat()/max else 0f,theme,loopA,loopB)
            drawMarker(canvas,loopA,"A",theme.accent,left,span);drawMarker(canvas,loopB,"B",theme.secondary,left,span)
            return
        }
        if(theme.renderer==ThemeRenderer.HWAHYUN){drawSilk(canvas);return}
        val left = paddingLeft.toFloat(); val right = (width - paddingRight).toFloat()
        val span = (right - left).coerceAtLeast(1f); val center = height * .52f
        val cursor = left + span * if (max > 0) progress.toFloat() / max else 0f
        val data = if (waveformEnabled) waveform else null
        if (loopA != null && loopB != null) {
            paint.style = Paint.Style.FILL; paint.color = tint(theme.secondary,28)
            canvas.drawRect(left + span * loopA!!, height * .12f, left + span * loopB!!, height * .88f, paint)
        }
        paint.style = Paint.Style.STROKE; paint.strokeCap = Paint.Cap.ROUND; paint.strokeWidth = density
        paint.color = tint(theme.secondary,65); canvas.drawLine(left, center, right, center, paint)
        if (data != null) {
            val columns = minOf(data.minimum.size, (span / (2.5f * density)).toInt().coerceAtLeast(1))
            val step = span / columns; val amplitude = height * .36f
            for (column in 0 until columns) {
                val from = column * data.minimum.size / columns
                val to = ((column + 1) * data.minimum.size / columns).coerceAtLeast(from + 1)
                var low = Float.POSITIVE_INFINITY; var high = Float.NEGATIVE_INFINITY
                for (i in from until to) { low = minOf(low, data.minimum[i]); high = max(high, data.maximum[i]) }
                val x = left + (column + .5f) * step
                // Peaks remain visible as a thin, faint layer instead of filling the whole bar.
                paint.strokeWidth = .65f * density
                paint.color = if (x <= cursor) tint(theme.accent,38) else tint(theme.secondary,32)
                var top = center - high * amplitude; var bottom = center - low * amplitude
                if(bottom-top<density){val middle=(top+bottom)*.5f;top=middle-.5f*density;bottom=middle+.5f*density}
                canvas.drawLine(x, top, x, bottom, paint)
                val energy = WaveformDisplay.body(data.columnRms(from, to))
                val radius = max(.45f * density, energy * amplitude)
                paint.strokeWidth = max(density, step * .55f)
                paint.color = if (x <= cursor) theme.accent else tint(theme.secondary,140)
                canvas.drawLine(x, center - radius, x, center + radius, paint)
            }
        } else {
            paint.color = theme.accent; paint.strokeWidth = (if(theme.renderer==ThemeRenderer.ABYSS)1f else 2f) * density
            canvas.drawLine(left, center, cursor, center, paint)
        }
        paint.strokeWidth = density; paint.color = tint(theme.accent,180)
        if(theme.renderer!=ThemeRenderer.ABYSS)canvas.drawLine(cursor, height * .1f, cursor, height * .88f, paint)
        paint.style = Paint.Style.FILL; paint.color = tint(theme.accent,35)
        canvas.drawCircle(cursor, center, (if(theme.renderer==ThemeRenderer.ABYSS)6f else 8f) * density, paint)
        paint.color = theme.accent; canvas.drawCircle(cursor, center, (if(theme.renderer==ThemeRenderer.ABYSS)2.5f else 3f) * density, paint)
        drawMarker(canvas, loopA, "A", theme.accent, left, span)
        drawMarker(canvas, loopB, "B", theme.secondary, left, span)
        if (loading && waveformEnabled) {
            paint.textSize = 8 * density; paint.typeface = android.graphics.Typeface.MONOSPACE
            paint.color = theme.muted; paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("SOURCE WAVEFORM…", right, height * .18f, paint)
            paint.textAlign = Paint.Align.LEFT
        }
    }
    private fun drawSilk(c:Canvas){
        val left=paddingLeft.toFloat();val right=(width-paddingRight).toFloat();val span=(right-left).coerceAtLeast(1f);val y=height*.5f
        val cursor=left+span*if(max>0)progress.toFloat()/max else 0f
        paint.style=Paint.Style.STROKE;paint.strokeCap=Paint.Cap.ROUND;paint.color=tint(theme.text,95);paint.strokeWidth=.6f*density
        c.drawLine(left,y,right,y,paint)
        paint.color=tint(theme.accent,24);paint.strokeWidth=6*density;c.drawLine(left,y,cursor,y,paint)
        paint.color=tint(theme.accent,210);paint.strokeWidth=density;c.drawLine(left,y,cursor,y,paint)
        if(loopA!=null&&loopB!=null){paint.color=tint(theme.secondary,150);paint.strokeWidth=2*density;c.drawLine(left+span*loopA!!,y,left+span*loopB!!,y,paint)}
        paint.style=Paint.Style.FILL;paint.color=tint(theme.accent,30);c.drawCircle(cursor,y,10*density,paint)
        c.save();c.translate(cursor,y);c.rotate(36f);c.scale(5*density,7*density);paint.color=theme.accent;c.drawPath(silkThumb,paint)
        paint.style=Paint.Style.STROKE;paint.color=tint(theme.text,200);paint.strokeWidth=.13f;c.drawPath(silkThumb,paint);c.restore()
        drawMarker(c,loopA,"A",theme.accent,left,span);drawMarker(c,loopB,"B",theme.secondary,left,span)
    }
    private fun drawMarker(canvas: Canvas, fraction: Float?, label: String, color: Int, left: Float, span: Float) {
        if (fraction == null) return
        val x = left + span * fraction.coerceIn(0f, 1f)
        paint.style = Paint.Style.STROKE; paint.strokeWidth = 1.5f * density; paint.color = color
        canvas.drawLine(x, height * .25f, x, height * .92f, paint)
        paint.style = Paint.Style.FILL; paint.textSize = 10 * density; paint.typeface = android.graphics.Typeface.MONOSPACE
        paint.textAlign = Paint.Align.CENTER; canvas.drawText(label, x, height * .21f, paint); paint.textAlign = Paint.Align.LEFT
    }
    override fun onDetachedFromWindow() { setActive(false); super.onDetachedFromWindow() }
    override fun close() {
        if (closed) return
        closed = true; cancelRead(); worker.shutdownNow(); handler.removeCallbacksAndMessages(null)
        waveform = null; source = null
    }
}
