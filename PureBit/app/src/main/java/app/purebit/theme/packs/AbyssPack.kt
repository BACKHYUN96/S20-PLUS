package app.purebit.theme.packs

import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.*
import app.purebit.theme.*
import app.purebit.theme.abyss.*
import app.purebit.ui.*

object AbyssPack:ThemePack {
    override fun build(host:ThemePlayerLayout)=with(host) {
        AbyssLayout.lastReport=if(theme.layout.artworkHeight>0)"Custom fixed artworkHeight=${theme.layout.artworkHeight}dp; automatic fit disabled" else "ABYSS player awaiting layout measurement"
        main=column(0).apply {setPadding(dp(24),0,dp(24),dp(4))}
        route=text("ABYSS · LOCAL AUDIO",9,muted).apply {gravity=Gravity.CENTER;letterSpacing=.12f}
        deck=DeckView(context).apply {
            gardenEmbedded=true;setTheme(theme,petalScale,reaction)
            contentDescription="어비스 · 원형 앨범아트를 눌러 원본 보기";importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_YES;isFocusable=true
        }
        val hero=FrameLayout(context)
        hero.addView(deck,FrameLayout.LayoutParams(-1,-1))
        hero.addView(button("⛶",false){actions(PlayerAction.FULLSCREEN)},FrameLayout.LayoutParams(dp(48),dp(48),Gravity.TOP or Gravity.END))
        main.addView(hero,LinearLayout.LayoutParams(-1,dp(if(theme.layout.artworkHeight>0)theme.layout.artworkHeight else (screenDp-480).toInt().coerceIn(240,520))))
        title=text("깊은 바다에 음악을",23,theme.text).apply {gravity=Gravity.CENTER;maxLines=2;includeFontPadding=false;ellipsize=android.text.TextUtils.TruncateAt.END;letterSpacing=.06f;typeface=Typeface.create("sans-serif-light",Typeface.NORMAL)}
        artist=text("FLAC / WAV를 선택하세요",13,muted).apply {gravity=Gravity.CENTER;maxLines=2;includeFontPadding=false;ellipsize=android.text.TextUtils.TruncateAt.END}
        source=text("LOCAL AUDIO · READY",9,cyan).apply {gravity=Gravity.CENTER;includeFontPadding=false;letterSpacing=.13f}
        title.setPadding(0,dp(2),0,dp(1));artist.setPadding(0,dp(1),0,dp(1));source.setPadding(0,dp(1),0,dp(1))
        listOf(title,artist,source).forEach {it.setShadowLayer(2*context.resources.displayMetrics.density,0f,context.resources.displayMetrics.density,0x90000a12.toInt())}
        main.addView(title);main.addView(artist);main.addView(source)
        seek=createSeek().apply {
            waveformEnabled=false
            contentDescription="재생 위치 · 드래그 중 음악 유지 · 놓으면 이동"
            setPadding(dp(24),0,dp(24),0)
        }
        val compact=context.resources.configuration.fontScale<=1.15f&&app.purebit.theme.abyss.AbyssLayout.combineSeekTime(theme.layout.blocks)
        time=app.purebit.theme.abyss.AbyssTimeView(context).apply {setText(app.purebit.R.string.abyss_initial_time);textSize=11f;setTextColor(muted)}.apply {gravity=Gravity.CENTER;typeface=Typeface.create("sans-serif-light",Typeface.NORMAL);includeFontPadding=false;minHeight=if(compact)0 else dp(20);setPadding(dp(24),0,dp(24),0);isClickable=false;importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_YES}
        val seekModule:View=if(compact)FrameLayout(context).apply {
            addView(seek,FrameLayout.LayoutParams(-1,dp(48)))
            addView(time,FrameLayout.LayoutParams(-1,dp(14),Gravity.BOTTOM))
            layoutParams=LinearLayout.LayoutParams(-1,dp(48))
        }else seek.apply {layoutParams=LinearLayout.LayoutParams(-1,dp(48))}
        main.addView(seekModule);if(!compact)main.addView(time,LinearLayout.LayoutParams(-1,-2))
        val controls=row().apply {gravity=Gravity.CENTER;setPadding(0,dp(3),0,dp(3))}
        shuffleButton=button("⇄",false){actions(PlayerAction.SHUFFLE)}
        repeatButton=button("↻",false){actions(PlayerAction.REPEAT)}
        play=button("▶",true){actions(PlayerAction.PLAY)}.apply {textSize=26f}
        controls.addView(shuffleButton,LinearLayout.LayoutParams(0,dp(52),1f));controls.addView(button("◀◀",false){actions(PlayerAction.PREVIOUS)},LinearLayout.LayoutParams(0,dp(52),1f))
        controls.addView(play,LinearLayout.LayoutParams(dp(theme.layout.playSize),dp(theme.layout.playSize)))
        controls.addView(button("▶▶",false){actions(PlayerAction.NEXT)},LinearLayout.LayoutParams(0,dp(52),1f));controls.addView(repeatButton,LinearLayout.LayoutParams(0,dp(52),1f));main.addView(controls)
        val volume=AbyssControlRow(context,.64f,232f);volumeDown=button("−",false){actions(PlayerAction.VOLUME_DOWN)};volumeUp=button("＋",false){actions(PlayerAction.VOLUME_UP)}
        volumeText=text("",10,muted).apply {gravity=Gravity.CENTER;letterSpacing=.12f;maxLines=2;includeFontPadding=false}
        volume.addView(volumeDown,LinearLayout.LayoutParams(dp(48),dp(48)));volume.addView(volumeText,LinearLayout.LayoutParams(0,dp(48),1f));volume.addView(volumeUp,LinearLayout.LayoutParams(dp(48),dp(48)));main.addView(volume)
        val loopPanel=column(4).apply {visibility=View.GONE;background=AbyssGlass(theme,context.resources.displayMetrics.density)}
        val tools=AbyssControlRow(context,.52f,192f).apply {tag="abyss-tools"};loopToggle=button("A–B",false){loopPanel.visibility=if(loopPanel.visibility==View.GONE)View.VISIBLE else View.GONE}
        loopToggle?.apply {textSize=10f;letterSpacing=.18f;minHeight=dp(48);minimumHeight=dp(48);setPadding(0,0,0,0);contentDescription="A–B 구간 반복 설정"}
        tools.addView(loopToggle,LinearLayout.LayoutParams(0,dp(48),1f));main.addView(tools)
        tools.addView(button("LYRICS",false){actions(PlayerAction.LYRICS)}.apply {textSize=10f;letterSpacing=.18f;contentDescription="가사 열기";setPadding(0,0,0,0)},LinearLayout.LayoutParams(0,dp(48),1f))
        favoriteButton=null // Library track menu retains favorite actions.
        val loops=row();loopA=button("A 시작",false){actions(PlayerAction.LOOP_A)};loopB=button("B 끝",false){actions(PlayerAction.LOOP_B)};loopClear=button("해제",false){actions(PlayerAction.LOOP_CLEAR)}
        listOf(loopA,loopB,loopClear).forEach {loops.addView(it,LinearLayout.LayoutParams(0,dp(48),1f))};loopPanel.addView(loops)
        loopStatus=text("",10,muted).apply {gravity=Gravity.CENTER};loopPanel.addView(loopStatus);main.addView(loopPanel)
        message=text("",11,muted).apply {gravity=Gravity.CENTER;maxLines=3};main.addView(message)
        blocks.putAll(mapOf(PlayerBlock.ROUTE to emptyList(),PlayerBlock.ARTWORK to listOf(hero),PlayerBlock.METADATA to listOf(title,artist,source),PlayerBlock.SEEK to listOf(seekModule),PlayerBlock.TIME to if(compact)emptyList()else listOf(time),PlayerBlock.TRANSPORT to listOf(controls),PlayerBlock.VOLUME to listOf(volume),PlayerBlock.LOOP to listOf(tools,loopPanel),PlayerBlock.MESSAGE to listOf(message),PlayerBlock.PICKER to emptyList()))
        if(theme.layout.artworkHeight==0)main.viewTreeObserver.addOnGlobalLayoutListener {
            val viewport=(main.parent as? ScrollView)?.height?:0
            if(viewport>0){
                var fixed=main.paddingTop+main.paddingBottom
                for(i in 0 until main.childCount){val child=main.getChildAt(i);if(child!==hero&&child.visibility!=View.GONE){fixed+=child.measuredHeight;val margins=child.layoutParams as? android.view.ViewGroup.MarginLayoutParams;if(margins!=null)fixed+=margins.topMargin+margins.bottomMargin}}
                val density=context.resources.displayMetrics.density
                val contentWidth=(main.width-main.paddingLeft-main.paddingRight)/density
                val fontScale=context.resources.configuration.fontScale
                val expanded=loopPanel.visibility==View.VISIBLE
                val height=dp(AbyssLayout.heroHeight(viewport/density,fixed/density,contentWidth,fontScale,expanded))
                if(hero.layoutParams.height!=height)hero.layoutParams=hero.layoutParams.apply {this.height=height}
                AbyssLayout.lastReport="viewport=${(viewport/density).toInt()}dp controls=${(fixed/density).toInt()}dp hero=${(height/density).toInt()}dp width=${contentWidth.toInt()}dp fontScale=$fontScale expanded=$expanded overflow=${((height+fixed-viewport).coerceAtLeast(0)/density).toInt()}dp; measured layout, not a visual pass"
            }
        }
    }
}
