package app.purebit.theme.abyss

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.widget.TextView

/** Retains the complete accessible label; ordinary elapsed/total time uses opposite edges. */
class AbyssTimeView(context: Context): TextView(context) {
    private var elapsed: String?=null
    private var duration: String?=null
    override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
        super.onTextChanged(text,start,before,count)
        val parts=text?.toString()?.split('/')
        val valid=parts?.size==2 && parts.all { part -> part.trim().isNotEmpty() && part.trim().all { it in '0'..'9' || it==':' } }
        elapsed=if(valid)parts!![0].trim() else null
        duration=if(valid)parts!![1].trim() else null
    }
    override fun onDraw(canvas: Canvas) {
        val left=elapsed;val right=duration
        if(left==null || right==null) {
            super.onDraw(canvas);return
        }
        val previous=paint.textAlign
        paint.color=currentTextColor
        val baseline=(height-paint.fontMetrics.bottom-paint.fontMetrics.top)/2f
        paint.textAlign=Paint.Align.LEFT
        canvas.drawText(left,paddingLeft.toFloat(),baseline,paint)
        paint.textAlign=Paint.Align.RIGHT
        canvas.drawText(right,(width-paddingRight).toFloat(),baseline,paint)
        paint.textAlign=previous
    }
}
