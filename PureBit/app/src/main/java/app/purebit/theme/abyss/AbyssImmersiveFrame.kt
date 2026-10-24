package app.purebit.theme.abyss

import android.content.Context
import android.view.MotionEvent
import android.widget.FrameLayout

/** First touch restores hidden controls and is consumed; active controls receive normal gestures. */
class AbyssImmersiveFrame(context:Context):FrameLayout(context) {
    init {isClickable=true}
    var wake:(MotionEvent)->Boolean={false}
    var idle:()->Unit={}
    private var waking=false
    override fun dispatchTouchEvent(event:MotionEvent):Boolean {
        if(event.actionMasked==MotionEvent.ACTION_DOWN)waking=wake(event)
        val consumed=if(waking){if(event.actionMasked==MotionEvent.ACTION_UP)performClick();true}else super.dispatchTouchEvent(event)
        if(event.actionMasked==MotionEvent.ACTION_UP||event.actionMasked==MotionEvent.ACTION_CANCEL){waking=false;idle()}
        return consumed
    }
    override fun performClick():Boolean {super.performClick();return true}
}
