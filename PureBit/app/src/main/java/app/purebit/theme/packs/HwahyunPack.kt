package app.purebit.theme.packs

import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.*
import app.purebit.theme.*
import app.purebit.ui.*

object HwahyunPack : ThemePack {
    override fun build(host: ThemePlayerLayout) = with(host) {
        main=column(0).apply {setPadding(dp(24),0,dp(24),dp(10))};
        route=text("",9,muted)
        deck=DeckView(context).apply {gardenEmbedded=true;setTheme(theme,petalScale,reaction);setOnClickListener{actions(PlayerAction.FULLSCREEN)};contentDescription="달빛 앨범아트 · 두 번 누르거나 클릭하여 몰입 감상";importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_YES;isFocusable=true}
        gestures(deck)
        val screenDp=host.screenDp
        main.addView(deck,LinearLayout.LayoutParams(-1,dp(GardenMotion.heroHeight(screenDp).toInt())))
        title=text("달빛에 머무는 음악",23,theme.text).apply {gravity=Gravity.CENTER;maxLines=2;ellipsize=android.text.TextUtils.TruncateAt.END;typeface=Typeface.SERIF;setPadding(0,dp(4),0,dp(2))}
        artist=text("",13,theme.text).apply {gravity=Gravity.CENTER;maxLines=1;ellipsize=android.text.TextUtils.TruncateAt.END;letterSpacing=.08f;setPadding(0,dp(4),0,dp(3))}
        source=text("LOCAL AUDIO",9,cyan).apply {gravity=Gravity.CENTER;letterSpacing=.13f;setPadding(0,dp(5),0,dp(10))}
        main.addView(title);main.addView(artist);main.addView(source)
        seek=createSeek();main.addView(seek,LinearLayout.LayoutParams(-1,dp(48)))
        time=GardenTimeView(context,theme).apply {textSize=11f;setTextColor(muted);gravity=Gravity.CENTER;setPadding(dp(12),0,dp(12),0)}
        main.addView(time,LinearLayout.LayoutParams(-1,dp(24)))
        val controls=row().apply {gravity=Gravity.CENTER;setPadding(0,dp(2),0,dp(2))}
        shuffleButton=button("⇄",false){actions(PlayerAction.SHUFFLE)};controls.addView(shuffleButton,LinearLayout.LayoutParams(0,dp(56),1f))
        controls.addView(button("◀◀",false){actions(PlayerAction.PREVIOUS)},LinearLayout.LayoutParams(0,dp(56),1f))
        play=button("▶",true){actions(PlayerAction.PLAY)}
        controls.addView(play,LinearLayout.LayoutParams(dp(90),dp(90)))
        controls.addView(button("▶▶",false){actions(PlayerAction.NEXT)},LinearLayout.LayoutParams(0,dp(56),1f))
        repeatButton=button("↻",false){actions(PlayerAction.REPEAT)};controls.addView(repeatButton,LinearLayout.LayoutParams(0,dp(56),1f));main.addView(controls)
        val volume=row();volumeDown=button("−",false){actions(PlayerAction.VOLUME_DOWN)};volumeUp=button("＋",false){actions(PlayerAction.VOLUME_UP)}
        volumeText=text("",11,theme.text).apply {gravity=Gravity.CENTER;typeface=Typeface.SERIF}
        volume.addView(volumeDown,LinearLayout.LayoutParams(dp(48),dp(42)));volume.addView(volumeText,LinearLayout.LayoutParams(0,dp(42),1f));volume.addView(volumeUp,LinearLayout.LayoutParams(dp(48),dp(42)));main.addView(volume)
        val loopPanel=column(0).apply {visibility=View.GONE;background=HwahyunPanel(theme,context.resources.displayMetrics.density)}
        val tools=row();loopToggle=button("A–B 구간",false){loopPanel.visibility=if(loopPanel.visibility==View.GONE)View.VISIBLE else View.GONE};tools.addView(loopToggle,LinearLayout.LayoutParams(0,dp(44),1f))
        tools.addView(button("음악 선택 ＋",false){actions(PlayerAction.PICKER)},LinearLayout.LayoutParams(0,dp(44),1f));main.addView(tools)
        val loops=row();loopA=button("A 시작",false){actions(PlayerAction.LOOP_A)};loopB=button("B 끝",false){actions(PlayerAction.LOOP_B)};loopClear=button("해제",false){actions(PlayerAction.LOOP_CLEAR)}
        listOf(loopA,loopB,loopClear).forEach {loops.addView(it,LinearLayout.LayoutParams(0,dp(44),1f))};loopPanel.addView(loops)
        loopStatus=text("",10,muted).apply {gravity=Gravity.CENTER};loopPanel.addView(loopStatus);main.addView(loopPanel)
        message=text("",11,muted).apply {gravity=Gravity.CENTER;maxLines=2};main.addView(message)
        blocks.putAll(mapOf(PlayerBlock.ROUTE to emptyList(),PlayerBlock.ARTWORK to listOf(deck),PlayerBlock.METADATA to listOf(title,artist,source),PlayerBlock.SEEK to listOf(seek),PlayerBlock.TIME to listOf(time),PlayerBlock.TRANSPORT to listOf(controls),PlayerBlock.VOLUME to listOf(volume),PlayerBlock.LOOP to listOf(tools,loopPanel),PlayerBlock.MESSAGE to listOf(message),PlayerBlock.PICKER to emptyList()))
    }}
