package app.purebit.theme

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
import app.purebit.ui.*
import app.purebit.theme.abyss.AbyssGlass

/** Shared theme chrome for player, library, queue, settings and diagnostics. */
class ThemeWidgets(private val context: Context, private val theme: ThemeSpec, private val bloom: (View) -> Unit) {
    private val isGarden get()=theme.renderer==ThemeRenderer.HWAHYUN
    private val cyan get()=theme.accent
    private val ink get()=theme.background
    private val muted get()=theme.muted
    private fun dp(n:Int)=(n*context.resources.displayMetrics.density).toInt()
    fun text(value:String,size:Int,color:Int=muted)=TextView(context).apply { if(isGarden)typeface=Typeface.SERIF;text=value;textSize=size.toFloat();setTextColor(color);setPadding(0,dp(7),0,dp(7)) }
    fun row()=LinearLayout(context).apply { orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL }
    fun column(padding:Int=20)=LinearLayout(context).apply { orientation=LinearLayout.VERTICAL;setPadding(dp(padding),dp(8),dp(padding),dp(12)) }
    fun rounded(color:Int,radius:Int,stroke:Int=Color.TRANSPARENT)=GradientDrawable().apply { setColor(color);cornerRadius=dp(radius).toFloat();setStroke(dp(1),stroke) }
    fun themeEdge():Int { val c=theme.secondary;return Color.rgb((Color.red(theme.surface)*.8f+Color.red(c)*.2f).toInt(),(Color.green(theme.surface)*.8f+Color.green(c)*.2f).toInt(),(Color.blue(theme.surface)*.8f+Color.blue(c)*.2f).toInt()) }
    fun card()=column(16).apply { background=if(theme.renderer==ThemeRenderer.ABYSS)AbyssGlass(theme,context.resources.displayMetrics.density)else if(isGarden)HwahyunPanel(theme,context.resources.displayMetrics.density) else rounded(theme.surface,16,themeEdge());layoutParams=LinearLayout.LayoutParams(-1,-2).apply { bottomMargin=dp(12) } }
    fun themeSwitch():Switch=Switch(context).apply {
        if(isGarden){
            thumbDrawable=BlossomThumb(theme,context.resources.displayMetrics.density)
            trackDrawable=GradientDrawable().apply {setColor((theme.accent and 0xffffff) or 0x45000000);cornerRadius=dp(6).toFloat();setSize(dp(42),dp(10))}
            switchMinWidth=dp(54);minimumHeight=dp(48);setPadding(dp(4),dp(6),dp(4),dp(6));typeface=Typeface.SERIF
        }
    }
    fun button(label:String,primary:Boolean=false,action:()->Unit):Button{
        if(theme.renderer==ThemeRenderer.ABYSS)return app.purebit.theme.abyss.AbyssButton(context).apply {
            text=label;textSize=13f;isAllCaps=false;setTextColor(theme.text);letterSpacing=.06f
            configure(theme,primary)
            stateListAnimator=null;minWidth=0;minimumWidth=0;minHeight=dp(44);minimumHeight=dp(44);setPadding(dp(8),dp(4),dp(8),dp(4))
            layoutParams=LinearLayout.LayoutParams(-1,dp(48)).apply {topMargin=dp(4);bottomMargin=dp(4)}
            contentDescription=controlDescription(label);setOnClickListener {action()}
        }
        if(isGarden)return HwahyunButton(context,theme).apply {
            text=label;textSize=13f;setPadding(dp(14),0,dp(14),0);setTextColor(if(primary)cyan else theme.text)
            form=if(label=="▶"&&primary)HwahyunButton.Form.SEAL else if(label in listOf("▶","Ⅱ","■","⇄","↻","◀◀","▶▶","−","＋","⚙","⛶"))HwahyunButton.Form.ICON else HwahyunButton.Form.TEXT
            layoutParams=LinearLayout.LayoutParams(-1,dp(48)).apply {topMargin=dp(3);bottomMargin=dp(3)}
            contentDescription=controlDescription(label);setOnClickListener {bloom(this);action()}
        }
        return Button(context).apply { text=label;textSize=13f;isAllCaps=false;setTextColor(if(primary)ink else cyan);typeface=Typeface.create("sans-serif-medium",Typeface.NORMAL);background=rounded(if(primary)cyan else theme.surface,14,if(primary)cyan else themeEdge());stateListAnimator=null;minHeight=dp(44);minimumHeight=dp(44);setPadding(dp(10),dp(8),dp(10),dp(8));layoutParams=LinearLayout.LayoutParams(-1,dp(48)).apply { topMargin=dp(4);bottomMargin=dp(4) };contentDescription=when(label){"▶"->"재생";"Ⅱ"->"일시정지";"◀◀"->"이전 곡";"▶▶"->"다음 곡";"⇄"->"셔플";"↻"->"반복";"⛶"->"전체 화면 시각화";"⚙"->"설정";else->label};setOnClickListener { action() } }
    }
    fun controlDescription(label:String)=when(label){"▶"->"재생";"Ⅱ"->"일시정지";"◀◀"->"이전 곡";"▶▶"->"다음 곡";"⇄"->"셔플";"↻"->"반복";"⛶"->"전체 화면 시각화";"⚙"->"설정";"−"->"음량 줄이기";"＋"->"음량 높이기";else->label}}
