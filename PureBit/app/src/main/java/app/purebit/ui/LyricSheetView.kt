package app.purebit.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ScrollView
import app.purebit.lyrics.LyricTimeline
import app.purebit.theme.*

/** Native readable text with an ink-like fade; no audio processing or word-timing claims. */
class LyricSheetView(context:Context,private val theme:ThemeSpec):LinearLayout(context){
    constructor(context:Context):this(context,BuiltInThemes.hwahyun)
    companion object {private var script:Typeface?=null
        @Synchronized private fun script(context:Context)=script?:Typeface.createFromAsset(context.assets,"fonts/NanumBrushScript-Regular.ttf").also {script=it}}
    private val above=line(15,theme.muted)
    private val current=line(if(theme.renderer==ThemeRenderer.HWAHYUN)31 else 24,theme.text)
    private val below=line(15,theme.muted)
    private val plainText=line(20,theme.text).apply {maxLines=Int.MAX_VALUE;ellipsize=null;setTextIsSelectable(true);gravity=Gravity.TOP or Gravity.CENTER_HORIZONTAL}
    private val plainScroll=ScrollView(context).apply {isFillViewport=true;visibility=View.GONE;addView(plainText,LayoutParams(-1,-2))}
    private var lastPlain=""
    private var shown:LyricTimeline?=null;private var index=Int.MIN_VALUE
    private var animationsActive=false
    private var timedScroll:ScrollView?=null
    fun setActive(value:Boolean){
        animationsActive=value
        if(!value){current.animate().cancel();current.alpha=1f;current.translationY=0f}
    }
    init {orientation=VERTICAL;gravity=Gravity.CENTER;setPadding(20,12,20,12)
        if(theme.renderer==ThemeRenderer.ABYSS){background=app.purebit.theme.abyss.AbyssGlass(theme,resources.displayMetrics.density);setPadding((20*resources.displayMetrics.density).toInt(),(12*resources.displayMetrics.density).toInt(),(20*resources.displayMetrics.density).toInt(),(12*resources.displayMetrics.density).toInt());listOf(above,current,below,plainText).forEach {it.typeface=Typeface.create("sans-serif-light",Typeface.NORMAL)}}
        current.maxLines=4;above.maxLines=2;below.maxLines=2
        if(theme.renderer==ThemeRenderer.HWAHYUN)current.typeface=script(context.applicationContext)
        if(theme.renderer==ThemeRenderer.ABYSS){
            val lines=LinearLayout(context).apply {orientation=VERTICAL;gravity=Gravity.CENTER;addView(above,LayoutParams(-1,-2));addView(current,LayoutParams(-1,-2));addView(below,LayoutParams(-1,-2))}
            timedScroll=ScrollView(context).apply {isFillViewport=true;addView(lines,LayoutParams(-1,-2))};addView(timedScroll,LayoutParams(-1,-1))
        }else {addView(above,LayoutParams(-1,-2));addView(current,LayoutParams(-1,-2));addView(below,LayoutParams(-1,-2))}
        addView(plainScroll,LayoutParams(-1,-1))}
    fun setFocusMode(focus:Boolean,landscape:Boolean){
        current.textSize=if(focus){if(landscape)30f else 38f}else if(theme.renderer==ThemeRenderer.HWAHYUN)31f else 24f
        above.textSize=if(focus)16f else 15f;below.textSize=above.textSize
        current.maxLines=if(focus){if(landscape)3 else 6}else 4
        above.maxLines=if(focus&&landscape)1 else 2;below.maxLines=above.maxLines
        plainText.textSize=if(focus)24f else 20f
    }
    private fun line(size:Int,color:Int)=TextView(context).apply {textSize=size.toFloat();setTextColor(color);gravity=Gravity.CENTER;typeface=Typeface.SERIF;ellipsize=android.text.TextUtils.TruncateAt.END;setPadding(0,16,0,16);importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_YES}
    fun show(timeline:LyricTimeline?,position:Long,plain:String=""){
        val active=timeline?.activeIndex(position)?:-1
        if(shown===timeline&&index==active&&lastPlain==plain)return
        shown=timeline;index=active
        val untimed=timeline==null&&plain.isNotBlank()
        plainScroll.visibility=if(untimed)View.VISIBLE else View.GONE
        timedScroll?.visibility=if(untimed)View.GONE else View.VISIBLE
        listOf(above,current,below).forEach {it.visibility=if(untimed)View.GONE else View.VISIBLE}
        if(lastPlain!=plain){plainText.text=plain;plainScroll.scrollTo(0,0)}
        lastPlain=plain
        if(untimed){current.animate().cancel();return}
        above.text=timeline?.lines?.getOrNull(active-1)?.text.orEmpty()
        current.text=if(timeline==null)"당신의 음악에\n가사를 연결하세요" else if(active<0)"음악을 따라 피어날 문장" else timeline.lines[active].text.ifBlank {"♪"}
        below.text=timeline?.lines?.getOrNull(active+1)?.text.orEmpty()
        current.animate().cancel()
        if(animationsActive&&ValueAnimator.areAnimatorsEnabled()&&context.getSystemService(AccessibilityManager::class.java)?.isTouchExplorationEnabled!=true){current.alpha=.22f;current.translationY=8*resources.displayMetrics.density;current.animate().alpha(1f).translationY(0f).setDuration(650).start()}
        else {current.alpha=1f;current.translationY=0f}
    }
    override fun onDetachedFromWindow(){setActive(false);super.onDetachedFromWindow()}
}
