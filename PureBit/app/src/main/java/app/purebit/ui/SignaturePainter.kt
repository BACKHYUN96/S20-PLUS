package app.purebit.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import app.purebit.theme.ThemeSpec
import kotlin.math.min

/** Offline licensed handwriting fonts, revealed like ink. No claimed reconstruction of pen strokes. */
class SignaturePainter(context: Context) {
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private val korean=fonts(context).first
    private val latin=fonts(context).second
    companion object {
        private var cached: Pair<Typeface,Typeface>?=null
        @Synchronized private fun fonts(context: Context): Pair<Typeface,Typeface> = cached ?: Pair(
            Typeface.createFromAsset(context.applicationContext.assets,"fonts/NanumBrushScript-Regular.ttf"),
            Typeface.createFromAsset(context.applicationContext.assets,"fonts/Allura-Regular.ttf")
        ).also { cached=it }
    }
    fun draw(canvas: Canvas, cx: Float, baseline: Float, available: Float, size: Float, progress: Float, theme: ThemeSpec, opacity:Float=1f) {
        if(progress<=0f)return
        paint.typeface=korean;paint.textSize=size
        val first=paint.measureText("김백현 ")
        paint.typeface=latin;paint.textSize=size*1.05f
        val second=paint.measureText("& Codex")
        val scale=min(1f,available/(first+second))
        canvas.save();canvas.translate(cx-(first+second)*scale/2,baseline);canvas.scale(scale,scale)
        canvas.clipRect(-size,-size*1.5f,(first+second)*progress.coerceAtMost(1f)+1,size*.7f)
        paint.color=theme.text;paint.alpha=(235*opacity.coerceIn(0f,1f)).toInt();paint.textAlign=Paint.Align.LEFT
        paint.setShadowLayer(size*.12f,0f,0f,theme.accent)
        paint.typeface=korean;paint.textSize=size;canvas.drawText("김백현 ",0f,0f,paint)
        paint.typeface=latin;paint.textSize=size*1.05f;canvas.drawText("& Codex",first,0f,paint)
        paint.clearShadowLayer();canvas.restore()
    }
}
