package app.purebit.theme

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.*
import app.purebit.ui.*

enum class PlayerAction { PLAY, PREVIOUS, NEXT, SHUFFLE, REPEAT, LOOP_A, LOOP_B, LOOP_CLEAR, VOLUME_DOWN, VOLUME_UP, PICKER, FULLSCREEN, LYRICS, FAVORITE }

/** Theme-owned view construction. Only semantic commands cross into the player host. */
class ThemePlayerLayout(
    internal val context: Context,
    internal val theme: ThemeSpec,
    internal val screenDp: Float,
    internal val petalScale: Float,
    internal val reaction: Boolean,
    internal val createSeek: () -> WaveformSeekBar,
    internal val makeButton: (String, Boolean, () -> Unit) -> Button,
    internal val gestures: (View) -> Unit,
    internal val actions: (PlayerAction) -> Unit
) {
    internal val isGarden get() = theme.renderer == ThemeRenderer.HWAHYUN
    internal val cyan get() = theme.accent
    internal val muted get() = theme.muted
    lateinit var main: LinearLayout
        internal set
    lateinit var deck: DeckView
        internal set
    lateinit var title: TextView
        internal set
    lateinit var artist: TextView
        internal set
    lateinit var source: TextView
        internal set
    lateinit var route: TextView
        internal set
    lateinit var message: TextView
        internal set
    lateinit var volumeText: TextView
        internal set
    lateinit var volumeDown: Button
        internal set
    lateinit var volumeUp: Button
        internal set
    lateinit var time: TextView
        internal set
    lateinit var play: Button
        internal set
    lateinit var seek: WaveformSeekBar
        internal set
    lateinit var shuffleButton: Button
        internal set
    lateinit var repeatButton: Button
        internal set
    lateinit var loopA: Button
        internal set
    lateinit var loopB: Button
        internal set
    lateinit var loopClear: Button
        internal set
    lateinit var loopStatus: TextView
        internal set
    var loopToggle: Button? = null
        internal set
    var favoriteButton:Button?=null
        internal set
    internal val blocks=linkedMapOf<PlayerBlock,List<View>>()
    init {
        ThemePacks.forRenderer(theme.renderer).build(this)
        applyLayout()
    }
    internal fun dp(n:Int)=(n*context.resources.displayMetrics.density).toInt()
    internal fun row()=LinearLayout(context).apply {orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
    internal fun column(padding:Int=20)=LinearLayout(context).apply {orientation=LinearLayout.VERTICAL;setPadding(dp(padding),dp(8),dp(padding),dp(12))}
    internal fun text(value:String,size:Int,color:Int)=TextView(context).apply {if(isGarden)typeface=Typeface.SERIF;text=value;textSize=size.toFloat();setTextColor(color);setPadding(0,dp(7),0,dp(7))}
    internal fun button(label:String,primary:Boolean=false,action:()->Unit)=makeButton(label,primary,action)

    internal fun applyLayout() {
        val spec=theme.layout.validated()
        main.removeAllViews()
        spec.blocks.forEach {block -> blocks.getValue(block).forEach {main.addView(it)}}
        main.setPadding(dp(spec.horizontalPadding),main.paddingTop,dp(spec.horizontalPadding),main.paddingBottom)
        if(spec.artworkHeight>0)deck.layoutParams=deck.layoutParams.apply {height=dp(spec.artworkHeight)}
        title.textSize=spec.titleSize.toFloat()
        play.layoutParams=play.layoutParams.apply {width=dp(spec.playSize)}
    }
}
