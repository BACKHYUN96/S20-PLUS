package app.purebit.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.View
import android.widget.*
import app.purebit.theme.*

/** Shared transport strip without an analyzer, decoder, or private playback instance. */
class MiniPlayerView(context:Context,private val theme:ThemeSpec,open:()->Unit,toggle:()->Unit,next:()->Unit):LinearLayout(context){
    constructor(context:Context):this(context,BuiltInThemes.original,{},{},{})
    private val d=resources.displayMetrics.density
    private val cover=ImageView(context)
    private val title=TextView(context)
    private val artist=TextView(context)
    private val play=control("▶",toggle)
    private val nextButton=control("▶▶",next)
    private val progress=ProgressBar(context,null,android.R.attr.progressBarStyleHorizontal)
    private var art:Bitmap?=null;private var artSet=false
    init {
        orientation=VERTICAL
        background=if(theme.renderer==ThemeRenderer.ABYSS)app.purebit.theme.abyss.AbyssGlass(theme,d)else if(theme.renderer==ThemeRenderer.HWAHYUN)HwahyunPanel(theme,d)else GradientDrawable().apply {setColor(theme.surface);cornerRadius=12*d}
        val line=LinearLayout(context).apply {gravity=Gravity.CENTER_VERTICAL;setPadding(dp(10),dp(3),dp(6),dp(2))}
        cover.scaleType=ImageView.ScaleType.CENTER_CROP;cover.contentDescription="현재 곡 · 재생 화면 열기";cover.setOnClickListener {open()}
        line.addView(cover,LayoutParams(dp(46),dp(46)))
        val words=LinearLayout(context).apply {orientation=VERTICAL;setPadding(dp(10),0,dp(6),0);gravity=Gravity.CENTER_VERTICAL;isFocusable=true;setOnClickListener {open()}}
        listOf(title,artist).forEach {it.maxLines=1;it.ellipsize=android.text.TextUtils.TruncateAt.END;it.typeface=if(theme.renderer==ThemeRenderer.HWAHYUN)Typeface.SERIF else Typeface.DEFAULT}
        title.textSize=13f;title.setTextColor(theme.text);artist.textSize=10f;artist.setTextColor(theme.muted)
        words.addView(title);words.addView(artist);words.minimumHeight=dp(54);line.addView(words,LayoutParams(0,-2,1f))
        line.addView(play,LayoutParams(dp(48),dp(48)));line.addView(nextButton,LayoutParams(dp(48),dp(48)))
        line.minimumHeight=dp(58);addView(line,LayoutParams(-1,-2));progress.max=1000;progress.progressTintList=ColorStateList.valueOf(theme.accent);progress.progressBackgroundTintList=ColorStateList.valueOf(theme.surface)
        progress.importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO;addView(progress,LayoutParams(-1,dp(3)))
    }
    private fun control(label:String,action:()->Unit):Button = (if(theme.renderer==ThemeRenderer.HWAHYUN)HwahyunButton(context,theme).apply {form=HwahyunButton.Form.ICON} else Button(context)).apply {text=label;textSize=18f;setTextColor(theme.accent);setPadding(0,0,0,0);setOnClickListener {action()}}
    fun update(name:String,by:String,playing:Boolean,busy:Boolean,prepared:Boolean,position:Long,duration:Long,bitmap:Bitmap?){
        title.text=name;artist.text=if(busy)"음원 준비 중…" else by
        play.text=if(busy)"■" else if(playing)"Ⅱ" else "▶";play.contentDescription=if(busy)"재생 준비 취소" else if(playing)"일시정지" else "재생";play.isEnabled=prepared||busy
        nextButton.contentDescription="다음 곡"
        progress.progress=if(duration>0)(position.coerceIn(0,duration)*1000/duration).toInt()else 0
        if(!artSet||art!==bitmap){art=bitmap;artSet=true
            if(bitmap!=null&&!bitmap.isRecycled)cover.setImageBitmap(bitmap)else if(theme.renderer==ThemeRenderer.HWAHYUN)cover.setImageBitmap(GardenArt.moon(context))else cover.setImageResource(app.purebit.R.drawable.ic_purebit)}
    }
    private fun dp(n:Int)=(n*d).toInt()
}
