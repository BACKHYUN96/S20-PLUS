package app.purebit.ui

import android.content.Context
import android.view.DragEvent
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import android.widget.ScrollView
import kotlin.math.abs

/** App-local drag identity; a queue mutation invalidates an in-flight drop. */
data class QueueDragToken(val id:String,val revision:Long)

class QueueScrollView(context:Context):ScrollView(context){
    private var edge=0;private var dragging=false;private var posted=false
    private val scroll=object:Runnable {override fun run(){posted=false;if(!dragging||edge==0||!isAttachedToWindow)return
        scrollBy(0,edge);posted=true;postOnAnimation(this)}}
    override fun dispatchDragEvent(event:DragEvent):Boolean {
        if(event.localState is QueueDragToken)when(event.action){
            DragEvent.ACTION_DRAG_STARTED->dragging=true
            DragEvent.ACTION_DRAG_LOCATION->{val margin=64*resources.displayMetrics.density
                edge=when{event.y<margin->-12;event.y>height-margin->12;else->0}
                if(!posted&&edge!=0){posted=true;postOnAnimation(scroll)}}
            DragEvent.ACTION_DRAG_ENDED->{dragging=false;edge=0;removeCallbacks(scroll);posted=false}
        }
        return super.dispatchDragEvent(event)
    }
    override fun onDetachedFromWindow(){dragging=false;removeCallbacks(scroll);posted=false;super.onDetachedFromWindow()}
}

/** Intercept only deliberate horizontal movement; vertical scrolling and child clicks stay native. */
class SwipeQueueRow(context:Context,private val removable:Boolean,private val remove:()->Boolean):FrameLayout(context){
    constructor(context:Context):this(context,false,{false})
    private val slop=ViewConfiguration.get(context).scaledTouchSlop
    private var x=0f;private var y=0f;private var swiping=false
    private fun reset(){translationX=0f;alpha=1f;swiping=false;parent?.requestDisallowInterceptTouchEvent(false)}
    override fun onInterceptTouchEvent(event:MotionEvent):Boolean {
        if(!removable)return false
        when(event.actionMasked){
            MotionEvent.ACTION_DOWN->{animate().cancel();reset();x=event.rawX;y=event.rawY}
            MotionEvent.ACTION_MOVE->if(event.pointerCount==1&&abs(event.rawX-x)>slop&&abs(event.rawX-x)>abs(event.rawY-y)*1.5f){swiping=true;parent?.requestDisallowInterceptTouchEvent(true);return true}
            MotionEvent.ACTION_CANCEL,MotionEvent.ACTION_POINTER_DOWN->reset()
        }
        return swiping
    }
    @android.annotation.SuppressLint("ClickableViewAccessibility") // Only intercepted horizontal drags arrive here; child buttons own clicks and explicit remove actions.
    override fun onTouchEvent(event:MotionEvent):Boolean {
        if(!swiping)return super.onTouchEvent(event)
        when(event.actionMasked){
            MotionEvent.ACTION_MOVE->{translationX=event.rawX-x;alpha=(1-abs(translationX)/width.coerceAtLeast(1)*.5f).coerceIn(.5f,1f)}
            MotionEvent.ACTION_UP->{val commit=abs(translationX)>maxOf(width*.32f,64*resources.displayMetrics.density);reset();if(commit)remove()}
            MotionEvent.ACTION_CANCEL,MotionEvent.ACTION_POINTER_DOWN->reset()
        }
        return true
    }
    override fun performClick():Boolean=super.performClick()
}
