package app.purebit.theme.packs

import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.*
import app.purebit.theme.*
import app.purebit.ui.*

object OriginalPack : ThemePack {
    override fun build(host: ThemePlayerLayout) = with(host) {
        main=column();
        val top=row()
        route=text("ANDROID OUTPUT",11,cyan).apply { letterSpacing=.12f }
        top.addView(route,LinearLayout.LayoutParams(0,dp(38),1f))
        top.addView(button("⛶",false) { actions(PlayerAction.FULLSCREEN) },LinearLayout.LayoutParams(dp(44),dp(38)));main.addView(top)
        deck=DeckView(context).apply { setTheme(theme,petalScale,reaction) };main.addView(deck,LinearLayout.LayoutParams(-1,dp(((screenDp).toInt()-620).coerceIn(190,290))))
        title=text("당신의 음악을 연결하세요",22,theme.text).apply { maxLines=2; ellipsize=android.text.TextUtils.TruncateAt.END; gravity=Gravity.CENTER; typeface=Typeface.create("sans-serif-medium",Typeface.NORMAL) }
        artist=text("폴더를 추가하거나 FLAC / WAV 파일을 선택하세요",13,muted).apply { gravity=Gravity.CENTER;maxLines=2;ellipsize=android.text.TextUtils.TruncateAt.END }
        source=text("LOCAL AUDIO · READY",10,cyan).apply { gravity=Gravity.CENTER;letterSpacing=.13f }
        main.addView(title);main.addView(artist);main.addView(source)
        seek=createSeek()
        main.addView(seek,LinearLayout.LayoutParams(-1,dp(56)))
        time=text("0:00                                      0:00",12,muted).apply { typeface=Typeface.MONOSPACE;gravity=Gravity.CENTER }
        main.addView(time)
        val loopRow=row()
        loopA=button("A 시작",false){actions(PlayerAction.LOOP_A)}
        loopB=button("B 끝",false){actions(PlayerAction.LOOP_B)}
        loopClear=button("해제",false){actions(PlayerAction.LOOP_CLEAR)}
        listOf(loopA,loopB,loopClear).forEach { loopRow.addView(it,LinearLayout.LayoutParams(0,dp(44),1f)) };main.addView(loopRow)
        loopStatus=text("A–B 구간 반복 · A 시작 → B 끝",10,muted).apply { gravity=Gravity.CENTER };main.addView(loopStatus)
        val controls=row().apply { gravity=Gravity.CENTER;setPadding(0,dp(4),0,dp(6)) }
        shuffleButton=button("⇄",false){actions(PlayerAction.SHUFFLE)};controls.addView(shuffleButton,LinearLayout.LayoutParams(0,dp(50),1f))
        controls.addView(button("◀◀",false){actions(PlayerAction.PREVIOUS)},LinearLayout.LayoutParams(0,dp(50),1f))
        play=button("▶",true){actions(PlayerAction.PLAY)}.apply { textSize=24f };controls.addView(play,LinearLayout.LayoutParams(dp(78),dp(66)))
        controls.addView(button("▶▶",false){actions(PlayerAction.NEXT)},LinearLayout.LayoutParams(0,dp(50),1f))
        repeatButton=button("↻",false){actions(PlayerAction.REPEAT)};controls.addView(repeatButton,LinearLayout.LayoutParams(0,dp(50),1f))
        main.addView(controls)
        val volumeRow=row()
        volumeDown=button("−",false){actions(PlayerAction.VOLUME_DOWN)};volumeUp=button("＋",false){actions(PlayerAction.VOLUME_UP)}
        volumeText=text("음량",12,muted).apply { gravity=Gravity.CENTER }
        volumeRow.addView(volumeDown,LinearLayout.LayoutParams(dp(48),dp(42)));volumeRow.addView(volumeText,LinearLayout.LayoutParams(0,dp(42),1f));volumeRow.addView(volumeUp,LinearLayout.LayoutParams(dp(48),dp(42)));main.addView(volumeRow)
        message=text("",12,muted).apply { gravity=Gravity.CENTER;maxLines=3 };main.addView(message)
        val picker=button("＋ 음악 파일 선택",false){actions(PlayerAction.PICKER)};main.addView(picker)
        blocks.putAll(mapOf(PlayerBlock.ROUTE to listOf(top),PlayerBlock.ARTWORK to listOf(deck),PlayerBlock.METADATA to listOf(title,artist,source),PlayerBlock.SEEK to listOf(seek),PlayerBlock.TIME to listOf(time),PlayerBlock.LOOP to listOf(loopRow,loopStatus),PlayerBlock.TRANSPORT to listOf(controls),PlayerBlock.VOLUME to listOf(volumeRow),PlayerBlock.MESSAGE to listOf(message),PlayerBlock.PICKER to listOf(picker)))
    }
}
