package app.purebit

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.*
import app.purebit.audio.*
import app.purebit.startup.*
import app.purebit.core.ScrubToken
import app.purebit.library.*
import app.purebit.playlist.*
import app.purebit.ui.*
import app.purebit.theme.*
import app.purebit.lyrics.*
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.RectF
import java.io.File

@SuppressLint("SetTextI18n")
class MainActivity : Activity() {
    private lateinit var engine: AudioEngine
    private lateinit var library: LibraryStore
    private lateinit var favorites:FavoritesStore
    private lateinit var playlistStore:PlaylistStore
    private var playlistBusy=false
    private var playlistDialog:AlertDialog?=null
    private var playlistDialogRequest=0L
    private val playlistRefresh=RevisionRefresh()
    private var queueSaveButton:Button?=null
    private var queueTrimButton:Button?=null
    private var librarySearch:EditText?=null
    private var librarySearchProgrammatic=false
    private var playlistParentFilter=""
    private var favoritesOnly=false
    private var nightTickAt=0L
    private var favoriteButton:Button?=null
    private lateinit var root: FrameLayout
    private lateinit var pages: FrameLayout
    private lateinit var main: LinearLayout
    private lateinit var libraryPage: LinearLayout
    private lateinit var queuePage: LinearLayout
    private lateinit var settings: LinearLayout
    private lateinit var diagnostic: LinearLayout
    private lateinit var themesPage: LinearLayout
    private lateinit var themeStore:ThemeStore
    private var theme=BuiltInThemes.original
    private val isGarden get()=theme.renderer==ThemeRenderer.HWAHYUN
    private val isAbyss get()=theme.renderer==ThemeRenderer.ABYSS
    private val hasAtmosphere get()=isGarden||isAbyss
    private var abyssAlbum:app.purebit.theme.abyss.AbyssAlbumView?=null
    private val abyssChrome=mutableListOf<View>()
    private var abyssChromeHidden=false
    private var abyssExit:View?=null
    private var abyssFullscreenSeek:WaveformSeekBar?=null
    private var abyssFullscreenLoop:Button?=null
    private var abyssSeekToken:ScrubToken?=null
    private var abyssSeekDuration=0L
    private val hideAbyssChrome=Runnable {
        if(isAbyss&&fullOverlay!=null&&lyricOverlay==null&&abyssAlbum==null&&lifecycleVisible&&motionEnabled()&&root.isInTouchMode&&prefs.getBoolean("abyss_auto_hide",true)&&abyssSeekToken==null){
            abyssChromeHidden=true
            abyssChrome.forEach {v->v.animate().cancel();v.animate().alpha(0f).setDuration(650).withEndAction {if(abyssChromeHidden)v.visibility=View.INVISIBLE}.start()}
        }
    }
    private fun revealAbyssChrome(schedule:Boolean=true){
        handler.removeCallbacks(hideAbyssChrome);abyssChromeHidden=false
        abyssChrome.forEach {it.animate().cancel();it.visibility=View.VISIBLE;it.alpha=1f}
        if(schedule&&isAbyss&&fullOverlay!=null&&lifecycleVisible)handler.postDelayed(hideAbyssChrome,5000)
    }
    private fun abyssEffects()=app.purebit.theme.abyss.AbyssEffects.fromLevels(app.purebit.theme.abyss.AbyssEffects.keys.map {prefs.getInt("abyss_$it",2)})
    private fun wakeAbyssChrome(event:android.view.MotionEvent):Boolean {
        val hidden=abyssChromeHidden
        val button=abyssExit;var exitHit=false
        if(button!=null){val xy=IntArray(2);button.getLocationOnScreen(xy);exitHit=event.rawX>=xy[0]&&event.rawX<=xy[0]+button.width&&event.rawY>=xy[1]&&event.rawY<=xy[1]+button.height}
        revealAbyssChrome(false);return hidden&&!exitHit
    }
    private fun cancelAbyssSeek(){abyssSeekToken?.let {engine.cancelScrub(it)};abyssSeekToken=null}
    private var gardenBack:GardenAtmosphereView?=null
    private var gardenFront:GardenAtmosphereView?=null
    @Volatile private var libraryEpoch=0
    private var loopToggle:Button?=null
    private lateinit var deck: DeckView
    private var immersive: DeckView? = null
    private var fullOverlay: FrameLayout? = null
    private lateinit var mini:MiniPlayerView
    private var currentArtwork:Bitmap?=null
    private lateinit var lyricStore:LyricStore
    private var lyricTimeline:LyricTimeline?=null
    private var lyricPlain=""
    private var lyricSource=""
    private var lyricGeneration=0
    private var lyricStatus="UTF-8 LRC 파일을 연결하세요"
    private var pendingLyricSource:String?=null
    private var lyricOverlay:FrameLayout?=null
    private var lyricSheet:LyricSheetView?=null
    private var lyricTitle:TextView?=null
    private var lyricInfo:TextView?=null
    private var lyricPlay:Button?=null
    private var lyricGarden:GardenAtmosphereView?=null
    private var lyricDeck:DeckView?=null
    private var lyricFocused=false
    private var lyricChromeHidden=false
    private val lyricChrome=mutableListOf<View>()
    private val hideLyricChrome=Runnable {
        if(lyricOverlay!=null&&lyricFocused&&lifecycleVisible&&motionEnabled()&&(!isAbyss||(root.isInTouchMode&&prefs.getBoolean("abyss_auto_hide",true)))){
            lyricChromeHidden=true
            lyricChrome.forEach {v->v.animate().cancel();v.animate().alpha(0f).setDuration(650).withEndAction {if(lyricChromeHidden)v.visibility=View.INVISIBLE}.start()}
        }
    }
    private fun revealLyricChrome(){
        if(lyricOverlay==null)return
        lyricChromeHidden=false
        lyricChrome.forEach {v->v.animate().cancel();v.visibility=View.VISIBLE;v.alpha=1f}
        handler.removeCallbacks(hideLyricChrome)
        if(lyricFocused&&lifecycleVisible&&motionEnabled()&&(!isAbyss||prefs.getBoolean("abyss_auto_hide",true)))handler.postDelayed(hideLyricChrome,4500)
    }
    private var restoreLyricSheet=false
    private var restoreFullscreen=false
    private var fullscreenAutomatic=false
    private var landscapeDismissed=false
    private var landscapeTitle:TextView?=null
    private var landscapeArtist:TextView?=null
    private var landscapeSource:TextView?=null
    private var landscapeTime:TextView?=null
    private var landscapeStatus:TextView?=null
    private var landscapeLyric:TextView?=null
    private var landscapePlay:Button?=null
    private val isLandscape get()=resources.configuration.orientation==Configuration.ORIENTATION_LANDSCAPE
    private lateinit var title: TextView
    private lateinit var artist: TextView
    private lateinit var source: TextView
    private lateinit var route: TextView
    private lateinit var message: TextView
    private lateinit var volumeText: TextView
    private lateinit var volumeDown: Button
    private lateinit var volumeUp: Button
    private lateinit var time: TextView
    private lateinit var play: Button
    private lateinit var seek: WaveformSeekBar
    private lateinit var details: TextView
    private lateinit var selfCheck: Button
    private lateinit var libraryRows: LinearLayout
    private lateinit var queueRows: LinearLayout
    private lateinit var libraryStatus: TextView
    private lateinit var modeValue: TextView
    private lateinit var shuffleButton: Button
    private lateinit var repeatButton: Button
    private lateinit var loopA: Button
    private lateinit var loopB: Button
    private lateinit var loopClear: Button
    private lateinit var loopStatus: TextView
    private val navButtons = mutableListOf<Button>()
    private var page = 0
    private var dragging = false
    private var scrubToken: ScrubToken? = null
    private var scrubDurationMs = 0L
    private var scrubPreviewMs = 0L
    private var libraryFilter = ""
    private var libraryOffset=0
    private var libraryScrollY=0
    private var libraryScrollPending=false
    private var parentLibraryOffset=0
    private var parentLibraryScroll=0
    private var libraryMode=LibraryMode.TRACKS
    private var librarySort=TrackSort.TITLE
    private var libraryDescending=false
    private var librarySortButton:Button?=null
    private var librarySortDirection:Button?=null
    private val libraryTabs=mutableMapOf<String,Button>()
    private val searchLibrary=Runnable {if(!isDestroyed&&!isFinishing&&lifecycleVisible&&page==1&&::libraryRows.isInitialized)populateLibrary(false)}
    private var queueOffset=0
    private var recentOnly=false
    private var albumView=false
    private var openedAlbum:String?=null
    private var queueDragging=false
    private var shownQueueRevision=-1L
    private var lastFilename = ""
    private var artToken = 0
    private var reportFile: File? = null
    private var capturingReport = false
    private var startup: StartupView? = null
    private var themePreview:AlertDialog?=null
    private var modernBack: android.window.OnBackInvokedCallback? = null
    private val prefs by lazy { getSharedPreferences("bitwire_ui", MODE_PRIVATE) }
    private val handler = Handler(Looper.getMainLooper())
    private val cyan get()=theme.accent
    private val violet get()=theme.secondary
    private val ink get()=theme.background
    private val muted get()=theme.muted
    companion object { private var introShown = false }
    private val poll = object : Runnable { override fun run() { render();PlaybackQueue.checkpoint();updateStartupReady(); handler.postDelayed(this, 200) } }
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == UsbManager.ACTION_USB_DEVICE_DETACHED) engine.onUsbPhysicalDetach()
            engine.refreshDevices()
        }
    }
    private var recoveryRedirected=false
    private var recoverySafe=false
    private var startupToken=0L
    private var startupStableSince=0L
    private var startupReady=false
    private fun updateStartupReady(){
        if(startupReady||!lifecycleVisible)return
        if(startup!=null){startupStableSince=0;return}
        val now=SystemClock.uptimeMillis()
        if(startupStableSince==0L)startupStableSince=now
        if(now-startupStableSince>=2000){StartupSafety.ready(this,startupToken);startupReady=true;AppFaults.stage=if(recoverySafe)"running · safe ORIGINAL" else "running"}
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val destination=StartupSafety.destination(this)
        if(destination==StartupPolicy.Destination.RECOVERY&&!intent.getBooleanExtra("recovery_safe",false)){
            recoveryRedirected=true;startActivity(Intent(this,RecoveryActivity::class.java).putExtra("show_recovery",true));finish();return
        }
        recoverySafe=intent.getBooleanExtra("recovery_safe",false)||destination!=StartupPolicy.Destination.PLAYER
        startupToken=StartupSafety.begin(this,recoverySafe)
        GardenNight.state.mode=runCatching {NightMode.valueOf(prefs.getString("garden_night_mode","AUTO")!!)}.getOrDefault(NightMode.AUTO)
        themeStore=ThemeStore(applicationContext);theme=if(recoverySafe)BuiltInThemes.original else themeStore.selected()
        if(isGarden)setTheme(R.style.HwahyunTheme)
        AppFaults.stage="MainActivity setup"
        engine = PlaybackRuntime.engine(applicationContext)
        PlaybackQueue.bind(applicationContext)
        library = LibraryStore(applicationContext)
        favorites=FavoritesStore(applicationContext)
        playlistStore=PlaylistStore(applicationContext)
        if(!recoverySafe)PlaybackQueue.restoreSelection(library.tracks())
        lyricStore=LyricStore(applicationContext)
        pendingLyricSource=savedInstanceState?.getString("pendingLyricSource")
        restoreLyricSheet=!recoverySafe&&savedInstanceState?.getBoolean("lyricSheet",false)==true
        restoreFullscreen=!recoverySafe&&savedInstanceState?.getBoolean("manualFullscreen",false)==true
        landscapeDismissed=if(savedInstanceState!=null&&savedInstanceState.getBoolean("wasLandscape",false)==isLandscape)savedInstanceState.getBoolean("landscapeDismissed",false)else false
        favoritesOnly=savedInstanceState?.getBoolean("favoritesOnly",false)==true;recentOnly=savedInstanceState?.getBoolean("recentOnly",false)==true
        albumView=!favoritesOnly&&!recoverySafe&&prefs.getBoolean("album_view",isGarden)
        openedAlbum=savedInstanceState?.getString("openedAlbum")
        libraryMode=runCatching {LibraryMode.valueOf(savedInstanceState?.getString("libraryMode")?:prefs.getString("library_mode",if(albumView)"ALBUMS" else "TRACKS")!!)}.getOrDefault(LibraryMode.TRACKS)
        if(favoritesOnly||recentOnly)libraryMode=LibraryMode.TRACKS
        librarySort=runCatching {TrackSort.valueOf(prefs.getString("library_sort","TITLE")!!)}.getOrDefault(TrackSort.TITLE)
        libraryDescending=prefs.getBoolean("library_descending",false)
        libraryFilter=savedInstanceState?.getString("libraryFilter").orEmpty().take(256)
        playlistParentFilter=savedInstanceState?.getString("playlistParentFilter").orEmpty().take(256)
        libraryOffset=savedInstanceState?.getInt("libraryOffset",0)?.coerceAtLeast(0)?:0
        libraryScrollY=savedInstanceState?.getInt("libraryScrollY",0)?.coerceAtLeast(0)?:0
        parentLibraryOffset=savedInstanceState?.getInt("parentLibraryOffset",0)?.coerceAtLeast(0)?:0
        parentLibraryScroll=savedInstanceState?.getInt("parentLibraryScroll",0)?.coerceAtLeast(0)?:0
        reportFile = savedInstanceState?.getString("reportName")?.let { name ->
            File(filesDir, "diagnostic-reports/$name").takeIf { it.isFile && name.matches(Regex("PureBit-[0-9a-f-]{36}\\.txt")) }
        }
        root = FrameLayout(this).apply { setBackgroundColor(ink) }
        if(hasAtmosphere){gardenBack=GardenAtmosphereView(this,theme,false);root.addView(gardenBack,FrameLayout.LayoutParams(-1,-1))}
        val shell = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        shell.setOnApplyWindowInsetsListener { v, insets ->
            val bars = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout() or WindowInsets.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom); insets
        }
        val header = row().apply { setPadding(dp(22), dp(if(isGarden)6 else 12), dp(18), dp(if(isGarden)2 else 4));if(isGarden)background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(0x8c120a15.toInt(),0x00120a15)) }
        if(isAbyss){header.background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(0xb0020d19.toInt(),0x10020d19));header.setPadding(dp(32),dp(6),dp(24),dp(2))}
        header.addView(text("BITWIRE", if(isAbyss)14 else if(isGarden)17 else 24, theme.text).apply { letterSpacing=if(isAbyss).45f else .16f; typeface=Typeface.create(if(isGarden)"serif" else "sans-serif-medium",Typeface.NORMAL) }, LinearLayout.LayoutParams(0, dp(if(isGarden)42 else 48), 1f))
        header.addView(text(if(isAbyss)"ABYSS" else if(isGarden)"花絃" else "●  LOCAL", if(isGarden)18 else 10, cyan).apply { gravity=Gravity.CENTER; letterSpacing=.1f }, LinearLayout.LayoutParams(dp(74), dp(48)))
        header.addView(button("⚙", false) { showPage(3) }, LinearLayout.LayoutParams(dp(44), dp(44)))
        shell.addView(header)
        pages = FrameLayout(this)
        shell.addView(pages, LinearLayout.LayoutParams(-1, 0, 1f))
        AppFaults.stage="create player";createPlayer();if(recoverySafe)main.addView(text("안전 모드 · ORIGINAL\n시작 연출과 시각 효과를 임시로 껐습니다. 설정에서 기존 테마로 돌아갈 수 있습니다.",13,cyan));createPlayerTools(); AppFaults.stage="create library";createLibrary();AppFaults.stage="create queue";createQueue();AppFaults.stage="create settings";createSettings();AppFaults.stage="create diagnostics";createDiagnostics();AppFaults.stage="create themes";createThemes()
        mini=MiniPlayerView(this,theme,{showPage(0)},{toggleMusic()},{PlaybackQueue.next()}).apply {visibility=View.GONE}
        shell.addView(mini,LinearLayout.LayoutParams(-1,-2).apply {leftMargin=dp(12);rightMargin=dp(12)})
        val navigation = row().apply { setPadding(dp(12), dp(if(isGarden)6 else 8), dp(12), dp(if(isGarden)6 else 8)); background=if(isGarden)android.graphics.drawable.GradientDrawable(android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(0x00150c18,0xe5150c18.toInt())) else rounded(theme.surface, 0) }
        if(isAbyss)navigation.background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(0x00020d19,0xe5020d19.toInt()))
        (if(isAbyss)listOf("PLAYER","LIBRARY","QUEUE","SETTINGS")else if(isGarden)listOf("재생","음악","대기열","설정")else listOf("DECK", "LIBRARY", "QUEUE", "설정")).forEachIndexed { i, label ->
            val b=button(label, false) { showPage(i) }.apply { textSize=11f; letterSpacing=.08f }
            if(b is HwahyunButton){b.form=HwahyunButton.Form.NAV;b.navIndex=i}
            navButtons+=b; navigation.addView(b,LinearLayout.LayoutParams(0,dp(if(isAbyss)56 else if(isGarden)60 else 48),1f))
        }
        shell.addView(navigation); root.addView(shell);if(hasAtmosphere){gardenFront=GardenAtmosphereView(this,theme,true).apply {
            if(isAbyss)protectAbyssViews(listOfNotNull(header,navigation,title,artist,source,time,play.parent as? View,volumeText.parent as? View,loopToggle?.parent as? View,message))
        };root.addView(gardenFront,FrameLayout.LayoutParams(-1,-1))}; setContentView(root); root.post {if(!isDestroyed&&!isFinishing)configureEdgeToEdge()}; root.requestApplyInsets(); showPage(if(recoverySafe)0 else savedInstanceState?.getInt("page",0)?.coerceIn(0,5)?:0)
        val system=IntentFilter().apply { addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);addAction(UsbManager.ACTION_USB_DEVICE_DETACHED) }
        if(Build.VERSION.SDK_INT>=33)registerModernBack()
        val permission=IntentFilter(UsbInspector.PERMISSION)
        if(Build.VERSION.SDK_INT>=33) { registerReceiver(usbReceiver,permission,RECEIVER_NOT_EXPORTED);registerReceiver(usbReceiver,system,RECEIVER_NOT_EXPORTED) }
        else { registerLegacyPermissionReceiver(permission);registerReceiver(usbReceiver,system) }
        if(!recoverySafe && !introShown && savedInstanceState==null && themeStore.introMode()!=IntroMode.OFF && !engine.state.playing && !engine.state.preparing && !engine.state.loading) {
            introShown=true
            showIntro(theme,themeStore.introMode(),true)
        } else { introShown=true; maybeRequestNotification() }
    }
    @Suppress("DEPRECATION") // Only legacy runtimes need explicit decor fitting/colors.
    private fun configureEdgeToEdge(){
        if(recoverySafe)return
        if(!prefs.getBoolean("system_bars_enabled",false))return
        AppFaults.stage="deferred optional window appearance"
        val result=runCatching {
            if(Build.VERSION.SDK_INT<35){
                window.setDecorFitsSystemWindows(false)
                val params=android.view.WindowManager.LayoutParams().apply {copyFrom(window.attributes);layoutInDisplayCutoutMode=android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS}
                window.attributes=params
                window.statusBarColor=Color.TRANSPARENT;window.navigationBarColor=Color.TRANSPARENT
            }
            // Target35+ already uses platform-enforced edge-to-edge; avoid early decor creation.
            window.isStatusBarContrastEnforced=false;window.isNavigationBarContrastEnforced=false
            window.insetsController?.setSystemBarsAppearance(0,android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS)
        }
        result.exceptionOrNull()?.let {error->
            prefs.edit().putBoolean("system_bars_enabled",false).apply()
            AppFaults.record(applicationContext,"Optional system-bar settings disabled after failure",error)
            AudioLog.event("window_appearance_fallback","${error.javaClass.simpleName}: ${error.message}; appearance disabled; playback unchanged")
        }
        AppFaults.stage="running"
    }
    /** Decoration fills the window; only interactive chrome reserves bars/camera space. */
    private fun insetChrome(chrome:FrameLayout){
        chrome.setOnApplyWindowInsetsListener {v,insets->
            val safe=insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
            v.setPadding(safe.left,safe.top,safe.right,safe.bottom)
            chrome.post {if(chrome.isAttachedToWindow)sizeLandscapePanel(chrome)};insets
        }
        chrome.addOnLayoutChangeListener {_,l,_,r,_,oldL,_,oldR,_->if(r-l!=oldR-oldL)sizeLandscapePanel(chrome)}
    }
    private fun sizeLandscapePanel(chrome:FrameLayout){
        val panel=chrome.findViewWithTag<View>("landscape-panel")?:return
        if(chrome.width<=0)return
        val params=panel.layoutParams?:return
        val available=chrome.width-chrome.paddingLeft-chrome.paddingRight
        val desired=(available*.46f).toInt().coerceAtLeast(1)
        if(params.width!=desired){params.width=desired;panel.layoutParams=params}
    }
    private fun createSeek():WaveformSeekBar=WaveformSeekBar(this).apply {
            setTheme(theme)
            max=10000;progressTintList=android.content.res.ColorStateList.valueOf(cyan);thumbTintList=android.content.res.ColorStateList.valueOf(cyan)
            setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(b:SeekBar?,p:Int,user:Boolean){
                    if (!user) return
                    if (touchGestureActive && !dragging) return
                    if (dragging) {
                        scrubPreviewMs=scrubDurationMs*p/max
                        time.text=scrubTime(engine.state.positionMs)
                    } else if (scrubToken==null) engine.seek((engine.state.pcm?.durationMs?:0)*p/max)
                }
                override fun onStartTrackingTouch(b:SeekBar?){
                    scrubToken=engine.beginScrub();dragging=scrubToken!=null
                    scrubDurationMs=engine.state.pcm?.durationMs?:0;scrubPreviewMs=scrubDurationMs*progress/max
                }
                override fun onStopTrackingTouch(b:SeekBar?){
                    val token=scrubToken;scrubToken=null;dragging=false
                    if(token!=null) {
                        if(touchCancelled)engine.cancelScrub(token)
                        else if(engine.finishScrub(token,scrubDurationMs*progress/max) && token.resume)PlaybackRuntime.ensureService(this@MainActivity)
                    }
                }
            })
        }
    private fun createPlayerTools(){
        if(isAbyss)return
        val tools=row()
        favoriteButton=button("☆ 즐겨찾기",false){PlaybackQueue.current?.let {toggleFavorite(it)}}
        tools.addView(favoriteButton,LinearLayout.LayoutParams(0,dp(48),1f))
        tools.addView(button("가사 서첩",false){showLyricSheet()},LinearLayout.LayoutParams(0,dp(48),1f));main.addView(tools)
    }
    private fun toggleFavorite(track:LibraryTrack){
        runCatching {favorites.toggle(track.id)}.fold({added->Toast.makeText(this,if(added)"즐겨찾기에 담았습니다" else "즐겨찾기에서 해제했습니다",Toast.LENGTH_SHORT).show();if(page==1)populateLibrary();render()},{e->Toast.makeText(this,e.message,Toast.LENGTH_LONG).show()})
    }
    private fun createPlayer() {
        val views=ThemePlayerLayout(this,theme,
            windowManager.currentWindowMetrics.bounds.height()/resources.displayMetrics.density,
            petalsScale(),prefs.getBoolean("theme_reaction",true),::createSeek,::button,
            {view -> installGardenGestures(view){showFullscreen()}},
            {action -> when(action) {
                PlayerAction.PLAY -> toggleMusic()
                PlayerAction.PREVIOUS -> PlaybackQueue.previous()
                PlayerAction.NEXT -> PlaybackQueue.next()
                PlayerAction.SHUFFLE -> {PlaybackQueue.shuffle=!PlaybackQueue.shuffle;render()}
                PlayerAction.REPEAT -> {PlaybackQueue.repeat=PlaybackQueue.Repeat.entries[(PlaybackQueue.repeat.ordinal+1)%3];render()}
                PlayerAction.LOOP_A -> engine.setRepeatA(engine.state.positionMs)
                PlayerAction.LOOP_B -> engine.setRepeatB(engine.state.positionMs)
                PlayerAction.LOOP_CLEAR -> engine.clearRepeat()
                PlayerAction.VOLUME_DOWN -> adjustVolume(-1)
                PlayerAction.VOLUME_UP -> adjustVolume(1)
                PlayerAction.PICKER -> pickFile()
                PlayerAction.FULLSCREEN -> showFullscreen()
                PlayerAction.LYRICS -> showLyricSheet()
                PlayerAction.FAVORITE -> PlaybackQueue.current?.let {toggleFavorite(it)}
            }}
        )
        main=views.main
        deck=views.deck
        title=views.title
        artist=views.artist
        source=views.source
        route=views.route
        message=views.message
        volumeText=views.volumeText
        volumeDown=views.volumeDown
        volumeUp=views.volumeUp
        time=views.time
        play=views.play
        seek=views.seek
        shuffleButton=views.shuffleButton
        repeatButton=views.repeatButton
        loopA=views.loopA
        loopB=views.loopB
        loopClear=views.loopClear
        loopStatus=views.loopStatus
        loopToggle=views.loopToggle
        favoriteButton=views.favoriteButton
        if(isAbyss)deck.onAbyssAlbumClick={bounds->showAbyssAlbum(deck,bounds)}
        attachPage(main)
    }
    private fun showAbyssAlbum(from:DeckView,bounds:RectF) {
        if(!isAbyss||abyssAlbum!=null||!lifecycleVisible||isFinishing||isDestroyed)return
        val bitmap=currentArtwork?.takeUnless {it.isRecycled}?:return
        val origin=RectF(bounds);val location=IntArray(2);val rootLocation=IntArray(2)
        from.getLocationInWindow(location);root.getLocationInWindow(rootLocation)
        origin.offset((location[0]-rootLocation[0]).toFloat(),(location[1]-rootLocation[1]).toFloat())
        val portal=app.purebit.theme.abyss.AbyssGeometry.portal(from.width.toFloat(),from.height.toFloat(),from.gardenLandscape)
        val occluder=RectF(portal.x-portal.radius,portal.y-portal.radius,portal.x+portal.radius,portal.y+portal.radius)
        occluder.offset((location[0]-rootLocation[0]).toFloat(),(location[1]-rootLocation[1]).toFloat())
        val overlay=app.purebit.theme.abyss.AbyssAlbumView(this,theme,origin,occluder,bitmap,motionEnabled()) {closeAbyssAlbum()}
        overlay.configureAbyss(abyssEffects())
        abyssAlbum=overlay;from.hideAbyssAlbum=true;from.invalidate()
        root.addView(overlay,FrameLayout.LayoutParams(-1,-1));overlay.requestApplyInsets();overlay.start()
    }
    private fun closeAbyssAlbum() {
        val overlay=abyssAlbum?:return;abyssAlbum=null;overlay.close();root.removeView(overlay)
        if(::deck.isInitialized){deck.hideAbyssAlbum=false;deck.invalidate()}
        immersive?.let {it.hideAbyssAlbum=false;it.invalidate()}
    }
    private fun adjustVolume(direction:Int) {
        if(engine.state.usbDirectMode)engine.adjustHardwareVolume(direction)
        else getSystemService(android.media.AudioManager::class.java).adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC,if(direction>0)android.media.AudioManager.ADJUST_RAISE else android.media.AudioManager.ADJUST_LOWER,android.media.AudioManager.FLAG_SHOW_UI)
    }
    override fun onKeyDown(keyCode:Int,event:android.view.KeyEvent?):Boolean {
        if(recoveryRedirected)return super.onKeyDown(keyCode,event)
        if(engine.state.usbDirectMode && (keyCode==android.view.KeyEvent.KEYCODE_VOLUME_UP || keyCode==android.view.KeyEvent.KEYCODE_VOLUME_DOWN)) {
            if(engine.state.playing)adjustVolume(if(keyCode==android.view.KeyEvent.KEYCODE_VOLUME_UP)1 else -1)
            else if(event?.repeatCount==0)Toast.makeText(this,"USB 음량은 재생 중 조절할 수 있습니다",Toast.LENGTH_SHORT).show()
            return true
        }
        return super.onKeyDown(keyCode,event)
    }
    private fun createLibrary() {
        libraryPage=column();attachPage(libraryPage)
        libraryPage.addView(text(if(isGarden)"M U S I C  C O L L E C T I O N" else "YOUR COLLECTION",11,cyan).apply { letterSpacing=.14f });libraryPage.addView(text(if(isGarden)"봄의 화첩" else "라이브러리",28,theme.text))
        val buttons=row();buttons.addView(button("＋ 폴더",true){startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION),3)},LinearLayout.LayoutParams(0,dp(48),1f));buttons.addView(button("＋ 파일",false){pickFile()},LinearLayout.LayoutParams(0,dp(48),1f));libraryPage.addView(buttons)
        val search=EditText(this).apply {filters=arrayOf(android.text.InputFilter.LengthFilter(256));setText(libraryFilter); hint="곡 · 아티스트 · 앨범 · 폴더 검색";textSize=14f;setSingleLine();setTextColor(theme.text);setHintTextColor(muted);setPadding(dp(16),0,dp(16),0);background=if(isGarden)HwahyunPanel(theme,resources.displayMetrics.density)else rounded(theme.surface,12)
            addTextChangedListener(object:TextWatcher {override fun beforeTextChanged(s:CharSequence?,start:Int,count:Int,after:Int){};override fun onTextChanged(s:CharSequence?,start:Int,before:Int,count:Int){if(librarySearchProgrammatic)return;libraryFilter=s.toString();libraryOffset=0;libraryScrollY=0;handler.removeCallbacks(searchLibrary);handler.postDelayed(searchLibrary,250)};override fun afterTextChanged(e:Editable?){} }) }
        librarySearch=search;libraryPage.addView(search,LinearLayout.LayoutParams(-1,dp(48)).apply { topMargin=dp(16) })
        val tabs=listOf("TRACKS" to "전체 곡","ALBUMS" to "앨범","ARTISTS" to "아티스트","FOLDERS" to "폴더","PLAYLISTS" to "재생목록","FAVORITES" to "★ 즐겨찾기","RECENT" to "최근 재생")
        tabs.chunked(4).forEach {group->val line=row();group.forEach {(key,label)->
            val tab=button(label,false){chooseLibraryMode(when(key){"FAVORITES","RECENT"->LibraryMode.TRACKS;else->LibraryMode.valueOf(key)},key=="FAVORITES",key=="RECENT")}
            libraryTabs[key]=tab;line.addView(tab,LinearLayout.LayoutParams(0,dp(48),1f))
        };libraryPage.addView(line)}
        val ordering=row()
        librarySortButton=button("곡 정렬",false){AlertDialog.Builder(this).setTitle("곡 목록 정렬").setSingleChoiceItems(TrackSort.entries.map {it.label}.toTypedArray(),librarySort.ordinal){dialog,index->
            librarySort=TrackSort.entries[index];prefs.edit().putString("library_sort",librarySort.name).apply();libraryOffset=0;libraryScrollY=0;dialog.dismiss();populateLibrary(false)
        }.setNegativeButton("닫기",null).show()}
        librarySortDirection=button("↑ 오름차순",false){libraryDescending=!libraryDescending;prefs.edit().putBoolean("library_descending",libraryDescending).apply();libraryOffset=0;libraryScrollY=0;populateLibrary(false)}
        ordering.addView(librarySortButton,LinearLayout.LayoutParams(0,dp(48),1.3f));ordering.addView(librarySortDirection,LinearLayout.LayoutParams(0,dp(48),1f));libraryPage.addView(ordering)
        libraryPage.addView(button("검색어 지우기",false){search.setText("")})
        libraryPage.addView(text("곡을 길게 누르면 ‘다음에 재생’을 선택할 수 있습니다",10,muted))
        libraryStatus=text("",12,muted);libraryPage.addView(libraryStatus)
        libraryRows=column(0);libraryPage.addView(libraryRows)
        libraryPage.addView(button("폴더 다시 읽기",false){rescanFolders()})
    }
    private fun createQueue() {
        queuePage=column();attachPage(queuePage);queuePage.addView(text(if(isGarden)"T H E  N E X T  V E R S E" else "UP NEXT",11,violet).apply { letterSpacing=.14f });queuePage.addView(text(if(isGarden)"이어지는 선율" else "재생 대기열",28,theme.text));queuePage.addView(text("길게 잡아 이동 · 옆으로 밀어 제거\n현재 선택 곡은 유지 · 버튼으로도 편집 가능",12,muted))
        val tools=row()
        queueSaveButton=button("이 대기열 저장",false){newPlaylist(PlaybackQueue.tracks.map {it.id})}
        tools.addView(queueSaveButton,LinearLayout.LayoutParams(0,dp(48),1f))
        tools.addView(button("재생목록 열기",false){chooseLibraryMode(LibraryMode.PLAYLISTS);showPage(1)},LinearLayout.LayoutParams(0,dp(48),1f));queuePage.addView(tools)
        queueTrimButton=button("현재 곡만 남기기",false){
            val expected=PlaybackQueue.revision
            playlistDialog=AlertDialog.Builder(this).setTitle("대기열 정리").setMessage("현재 선택 곡을 유지하고 나머지 대기열 항목을 제거할까요?").setPositiveButton("현재 곡만 유지"){_,_->
                if(expected==PlaybackQueue.revision){PlaybackQueue.keepCurrent();populateQueue()}else Toast.makeText(this,"대기열이 변경되었습니다. 다시 확인해 주세요.",Toast.LENGTH_SHORT).show()
            }.setNegativeButton("취소",null).show()
        };queuePage.addView(queueTrimButton)
        queueRows=column(0);queuePage.addView(queueRows)
    }
    private fun createSettings() {
        settings=column();attachPage(settings);settings.addView(text(if(isGarden)"G A R D E N  S T U D I O" else "CONTROL ROOM",11,cyan).apply { letterSpacing=.14f });settings.addView(text(if(isGarden)"정원의 설정" else "설정",28,theme.text))
        val recovery=card();recovery.addView(text(if(recoverySafe)"안전 모드로 실행 중" else "시작 오류 복구",18,theme.text))
        recovery.addView(text("시작 도중 충돌이 확인되면 기본 테마로 실행합니다. 안전 모드도 실패하면 복구 화면을 엽니다.",12,muted))
        recovery.addView(button("복구 화면 · 오류 기록",false){startActivity(Intent(this,RecoveryActivity::class.java).putExtra("show_recovery",true))})
        if(recoverySafe)recovery.addView(button("저장된 테마로 다시 시도",false){StartupSafety.retrySelectedTheme(this);intent.putExtra("recovery_safe",false);recreate()})
        settings.addView(recovery)
        val themes=card();themes.addView(text("테마 및 시작 연출",18,theme.text));themes.addView(text(theme.name,13,cyan));themes.addView(button("테마 선택 · 파일 가져오기 →",false){showPage(5)});settings.addView(themes)
        val output=card();output.addView(text("오디오 출력",18,theme.text));modeValue=text("",12,muted);output.addView(modeValue)
        output.addView(button("USB Direct 음악 모드",true){showHardwareMusicOptions(false)})
        output.addView(button("Android 출력 모드",false){engine.chooseAndroidOutput()})
        output.addView(button("USB 접근 허용",false){engine.usbInspector.requestPermissions();engine.refreshDevices()})
        output.addView(text("USB Direct는 현재 확인된 삼성 USB-C 이어폰과 48/96 kHz 원본을 대상으로 합니다. 원본 PCM 변환과 자동 fallback은 없습니다.",12,muted));settings.addView(output)
        val visuals=card();visuals.addView(text("빛과 움직임",18,theme.text))
        visuals.addView(themeSwitch().apply { text=if(isGarden)"스펙트럼 막대 · ORIGINAL" else "음악 스펙트럼 시각화";setTextColor(theme.text);isChecked=prefs.getBoolean("visualizer",true);setOnCheckedChangeListener { _,v->prefs.edit().putBoolean("visualizer",v).apply();updateVisualActive() } })
        listOf("bass_pulse" to "베이스 펄스", "circuit_flow" to "회로 신호 흐름", "waveform" to "파형 탐색 바").forEach { (key,label) ->
            visuals.addView(themeSwitch().apply { text=if(isGarden)when(key){"bass_pulse"->"달빛 맥동";"circuit_flow"->"현의 빛 흐름";else->"원본 파형 · ORIGINAL"}else label;setTextColor(theme.text);isChecked=prefs.getBoolean(key,true);setOnCheckedChangeListener { _,v->prefs.edit().putBoolean(key,v).apply();updateVisualActive() } })
        }
        if(hasAtmosphere)visuals.addView(themeSwitch().apply {text="가로에서 감상 화면 자동 열기";setTextColor(theme.text);isChecked=prefs.getBoolean("landscape_auto",true);setOnCheckedChangeListener {_,v->prefs.edit().putBoolean("landscape_auto",v).apply()}})
        if(isGarden){
            lateinit var nightChoice:Button
            nightChoice=button("정원의 밤: ${GardenNight.state.mode.label}",false){
                AlertDialog.Builder(this).setTitle("정원의 밤").setSingleChoiceItems(NightMode.entries.map {it.label}.toTypedArray(),GardenNight.state.mode.ordinal){dialog,index->
                    GardenNight.state.mode=NightMode.entries[index];prefs.edit().putString("garden_night_mode",GardenNight.state.mode.name).apply();nightChoice.text="정원의 밤: ${GardenNight.state.mode.label}";dialog.dismiss()
                    Toast.makeText(this,GardenNight.state.mode.label,Toast.LENGTH_SHORT).show()
                }.setNegativeButton("닫기",null).show()
            };visuals.addView(nightChoice)
            visuals.addView(button("깊은 밤 미리보기 · 10초",false){GardenNight.state.preview();Toast.makeText(this,"재생 화면에서 깊은 밤을 미리 볼 수 있습니다",Toast.LENGTH_SHORT).show();showPage(0)})
            visuals.addView(text("자동: 화면을 보며 음악을 듣는 동안 12분에 걸쳐 변화합니다. 고정 분위기를 고를 수도 있습니다.",12,muted))
        }
        if(isGarden)visuals.addView(themeSwitch().apply {text="곡 전환 · 꽃잎 바람";setTextColor(theme.text);isChecked=prefs.getBoolean("track_transition",true);setOnCheckedChangeListener {_,v->prefs.edit().putBoolean("track_transition",v).apply()}})
        if(isGarden)visuals.addView(themeSwitch().apply {text="앨범 색을 달빛에 반영";setTextColor(theme.text);isChecked=prefs.getBoolean("album_moon_color",true);setOnCheckedChangeListener {_,v->prefs.edit().putBoolean("album_moon_color",v).apply();updateVisualActive()}})
        visuals.addView(themeSwitch().apply {text="시스템 바 투명 효과";setTextColor(theme.text);isChecked=prefs.getBoolean("system_bars_enabled",false);setOnCheckedChangeListener {_,v->prefs.edit().putBoolean("system_bars_enabled",v).apply();if(v)root.post {configureEdgeToEdge()}else recreate()}})
        visuals.addView(text("${theme.name} · 60 FPS 목표\n시각화는 원본 PCM 분석이며 실제 DAC 출력 측정은 아닙니다.",12,muted));settings.addView(visuals)
        if(isAbyss)createAbyssSettings()
        val internal=card();internal.addView(text("앱 내부 데이터",18,theme.text));internal.addView(text("출력 정보, 검증 범위, USB 진단 및 로그",12,muted));internal.addView(button("앱 내부 데이터 열기 →",false){showPage(4)});settings.addView(internal)
        settings.addView(text("BITWIRE  ${BuildConfig.VERSION_NAME}\nPureBit playback engine\nBIT-PERFECT NOT VERIFIED",11,muted))
    }
    private fun createAbyssSettings(){
        val panel=card();panel.addView(text("심해 분위기",20,theme.text))
        val values=arrayOf("끄기","은은하게","보통","풍성하게")
        val labels=listOf("기포·부유물","해파리 애니메이션","수중 굴절·빛줄기","저음 파동")
        val buttons=mutableListOf<Button>()
        fun refresh(){app.purebit.theme.abyss.AbyssEffects.keys.forEachIndexed {i,key->buttons.getOrNull(i)?.text="${labels[i]}: ${values[prefs.getInt("abyss_$key",2).coerceIn(0,3)]}"}}
        fun preset(levels:List<Int>){val edit=prefs.edit();app.purebit.theme.abyss.AbyssEffects.keys.forEachIndexed {i,key->edit.putInt("abyss_$key",levels[i])};edit.apply();refresh();updateVisualActive()}
        val presets=row();presets.addView(button("고요한 심해",false){preset(app.purebit.theme.abyss.AbyssEffects.calm)},LinearLayout.LayoutParams(0,-2,1f));presets.addView(button("발광 심해",false){preset(app.purebit.theme.abyss.AbyssEffects.luminous)},LinearLayout.LayoutParams(0,-2,1f));panel.addView(presets)
        app.purebit.theme.abyss.AbyssEffects.keys.forEachIndexed {i,key->
            val b=button("",false){AlertDialog.Builder(this).setTitle(labels[i]).setSingleChoiceItems(values,prefs.getInt("abyss_$key",2).coerceIn(0,3)){dialog,index->prefs.edit().putInt("abyss_$key",index).apply();dialog.dismiss();refresh();updateVisualActive()}.setNegativeButton("취소",null).show()};b.layoutParams=b.layoutParams.apply {height=-2};b.minimumHeight=dp(52);buttons+=b;panel.addView(b)
        }
        refresh()
        panel.addView(button("기본 강도로 복원",false){preset(listOf(2,2,2,2))})
        panel.addView(themeSwitch().apply {text="몰입 화면 조작부 자동 숨김 · 5초";setTextColor(theme.text);isChecked=prefs.getBoolean("abyss_auto_hide",true);setOnCheckedChangeListener {_,value->prefs.edit().putBoolean("abyss_auto_hide",value).apply();revealAbyssChrome()}})
        panel.addView(text("숨겨진 조작부는 첫 터치로 복원됩니다. 돌아가기 버튼은 유지합니다. 배경의 빛과 바위는 남지만 해파리는 별도 모션 레이어로 끌 수 있습니다.",12,muted))
        panel.addView(button("수중 렌더러 상태",false){AlertDialog.Builder(this).setTitle("어비스 · 시각 렌더러").setMessage(app.purebit.theme.abyss.AbyssWater.status+"\n\nAndroid 13 이상에서 AGSL을 시도하며 지원하지 않거나 실패하면 Canvas로 표시합니다. 실제 유체 시뮬레이션과 DAC 검증을 의미하지 않습니다.").setPositiveButton("확인",null).show()})
        settings.addView(panel)
    }
    private fun createDiagnostics() {
        diagnostic=column();attachPage(diagnostic)
        diagnostic.addView(button("← 설정",false){showPage(3)});diagnostic.addView(text("앱 내부 데이터",26,theme.text))
        diagnostic.addView(button("USB INSPECT / PERMISSION",false){engine.usbInspector.requestPermissions();engine.refreshDevices()})
        diagnostic.addView(button("48 kHz 비교 테스트 준비 · 5초",false){showComparison(48000)})
        diagnostic.addView(button("96 kHz 비교 테스트 준비 · 5초",false){showComparison(96000)})
        diagnostic.addView(button("USB Direct 저레벨 테스트 준비",false){showUsbDirectOptions()})
        diagnostic.addView(button("하드웨어 볼륨 검증 · PCM 전송 없음",false){showHardwareMusicOptions(true)})
        diagnostic.addView(button("USB 현재 설정 읽기 · READ ONLY",false){engine.readUsbSettings()})
        diagnostic.addView(button("실행 오류 기록 공유",false){shareStartupFaults()})
        diagnostic.addView(button("출력 정보 새로고침",false){engine.refreshDevices()})
        diagnostic.addView(button("Android mixer 정책",false){showPolicyOptions()})
        selfCheck=button("자체진단 실행",true){captureReport { report->AlertDialog.Builder(this).setTitle("자체진단 결과").setMessage(report.readText().substringBefore("\n=== FULL DIAGNOSTICS ===")).setPositiveButton("공유"){_,_->shareReport(report)}.setNeutralButton("저장"){_,_->saveReport(report)}.setNegativeButton("닫기",null).show() }}
        diagnostic.addView(selfCheck);diagnostic.addView(button("진단 + 내부 로그 공유",false){captureReport(::shareReport)});diagnostic.addView(button("진단 파일 저장",false){captureReport(::saveReport)})
        details=text("",12,muted).apply { typeface=Typeface.MONOSPACE;setTextIsSelectable(true) };diagnostic.addView(details)
    }
    private fun createThemes(){themesPage=column();attachPage(themesPage);populateThemes()}
    private fun petalsScale()=floatArrayOf(.5f,1f,1.7f)[prefs.getInt("petal_density",1).coerceIn(0,2)]
    private fun populateThemes(){
        themesPage.removeAllViews();themesPage.addView(button("ORIGINAL 기본 테마로 돌아가기",false){applyTheme(BuiltInThemes.original)});themesPage.addView(button("← 설정",false){showPage(3)})
        themesPage.addView(text("테마 및 시작 연출",26,theme.text));themesPage.addView(text("현재 테마 · ${theme.name}",12,cyan))
        themeStore.all().forEach { spec ->
            val tile=card();tile.addView(text((if(theme.id==spec.id)"● " else "")+spec.name,18,theme.text))
            tile.addView(text(if(spec.renderer==ThemeRenderer.ABYSS)"심해 전망창 · 기포 · 유리 앨범" else if(spec.renderer==ThemeRenderer.HWAHYUN)"달빛 · 열두 현 · 흩날리는 벚꽃" else "네온 회로 · 회전하는 DJ 데크",12,muted))
            val swatches=row();listOf(spec.accent,spec.secondary,spec.text).forEach { color -> swatches.addView(View(this).apply { background=rounded(color,8) },LinearLayout.LayoutParams(dp(38),dp(8)).apply { rightMargin=dp(8) }) };tile.addView(swatches)
            tile.addView(button("미리보기 · 적용",false){previewTheme(spec)});themesPage.addView(tile)
        }
        themesPage.addView(button("＋ 테마 파일 가져오기",false){startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {addCategory(Intent.CATEGORY_OPENABLE);type="*/*"},4)})
        themesPage.addView(text(".bitwire-theme · 오프라인 테마 팩\n색상·벚꽃·시작 연출과 재생 화면 배치·크기를 바꿀 수 있습니다. ORIGINAL/화현/ABYSS 장면을 지원합니다.",12,muted))
        val intro=card();intro.addView(text("시작 연출",18,theme.text))
        val modes=arrayOf("전체 · 테마 기본 길이","짧게 · 1.8초","끄기")
        intro.addView(button("시작 연출: ${modes[themeStore.introMode().ordinal]}",false){
            AlertDialog.Builder(this).setTitle("시작 연출").setSingleChoiceItems(modes,themeStore.introMode().ordinal){dialog,which ->themeStore.setIntroMode(IntroMode.entries[which]);dialog.dismiss();populateThemes()}.setNegativeButton("취소",null).show()
        })
        intro.addView(button("현재 오프닝 다시 보기",false){showIntro(theme,IntroMode.FULL)})
        if(isGarden)IntroScene.entries.forEach {scene->
            val tile=card();tile.addView(text((if(themeStore.introScene(theme)==scene)"● " else "")+scene.label,18,theme.text))
            tile.addView(IntroTileView(this,theme,scene),LinearLayout.LayoutParams(-1,dp(124)))
            tile.addView(text(scene.description,12,muted))
            val actions=row();actions.addView(button("미리보기",false){showIntro(theme,IntroMode.FULL,sceneStyle=scene)},LinearLayout.LayoutParams(0,dp(48),1f))
            actions.addView(button(if(themeStore.introScene(theme)==scene)"선택됨" else "선택",false){themeStore.setIntroScene(scene);populateThemes()},LinearLayout.LayoutParams(0,dp(48),1f));tile.addView(actions);intro.addView(tile)
        }
        intro.addView(text("전체 연출 약 ${IntroTimeline.duration(theme,IntroMode.FULL,themeStore.introScene(theme))/1000f}초 · 무음 · 터치로 건너뛰기\n자동 연출은 새 실행에서만 표시하며 재생 중 복귀 때는 생략합니다.",12,muted));themesPage.addView(intro)
        val petals=card();petals.addView(text(if(isAbyss)"기포와 빛" else "벚꽃과 현",18,theme.text))
        val densityNames=arrayOf("적게","보통","풍성하게")
        petals.addView(button("${if(isAbyss)"부유물 양" else "꽃잎 양"}: ${densityNames[prefs.getInt("petal_density",1).coerceIn(0,2)]}",false){
            AlertDialog.Builder(this).setTitle(if(isAbyss)"기포·부유물 양" else "꽃잎 양").setSingleChoiceItems(densityNames,prefs.getInt("petal_density",1).coerceIn(0,2)){dialog,i->prefs.edit().putInt("petal_density",i).apply();dialog.dismiss();updateVisualActive();populateThemes()}.show()
        })
        petals.addView(themeSwitch().apply { text=if(isAbyss)"어비스 음악 반응" else "화현 음악 반응";setTextColor(theme.text);isChecked=prefs.getBoolean("theme_reaction",true);setOnCheckedChangeListener {_,value->prefs.edit().putBoolean("theme_reaction",value).apply();updateVisualActive()} })
        petals.addView(text("음악 반응과 입자 효과는 화면에만 적용됩니다.",12,muted));themesPage.addView(petals)
        themesPage.addView(button("기본 ORIGINAL 테마로 복원",false){applyTheme(BuiltInThemes.original)})
    }
    private fun previewTheme(spec:ThemeSpec){
        if(recoverySafe){Toast.makeText(this,"설정에서 저장된 테마로 다시 시도한 뒤 미리보기를 열어 주세요",Toast.LENGTH_LONG).show();return}
        themePreview?.dismiss()
        val previewWidgets=ThemeWidgets(this,spec,{})
        val previewViews=ThemePlayerLayout(this,spec,
            windowManager.currentWindowMetrics.bounds.height()/resources.displayMetrics.density,
            petalsScale(),prefs.getBoolean("theme_reaction",true),
            {WaveformSeekBar(this).apply {setTheme(spec);isEnabled=false}},
            {label,primary,action -> previewWidgets.button(label,primary,action)}, {}, {})
        val preview=previewViews.deck.apply {setEffects(true,true,true);setArtwork(currentArtwork);gardenEmbedded=false}
        preview.configureAbyss(abyssEffects())
        val content=previewViews.main.apply {setBackgroundColor(spec.background)}
        val previewTitle=previewViews.title;val previewArtist=previewViews.artist
        previewViews.message.text="배치 미리보기 · 재생 조작은 실제 곡에 반영되지 않습니다"
        val frame=ScrollView(this).apply {addView(content)}
        val dialog=AlertDialog.Builder(this).setTitle(spec.name).setView(frame).setPositiveButton("테마 적용"){_,_->applyTheme(spec)}
            .setNeutralButton("오프닝 보기"){_,_->showIntro(spec,IntroMode.FULL)}.setNegativeButton("닫기",null).create()
        var cover=currentArtwork
        val refresh=object:Runnable{override fun run(){if(!dialog.isShowing)return;val state=engine.state
            preview.update(state.pcm,state.positionMs,state.playing)
            if(cover!==currentArtwork){cover=currentArtwork;preview.setArtwork(cover)}
            previewTitle.text=PlaybackQueue.current?.title?:"당신의 음악을 연결하세요"
            previewArtist.text=PlaybackQueue.current?.artist.orEmpty().ifBlank {"FLAC / WAV · LOCAL AUDIO"}
            previewViews.time.text="${clock(state.positionMs)} / ${clock(state.pcm?.durationMs?:0)}"
            handler.postDelayed(this,200)
        }}
        dialog.setOnShowListener {preview.setActive(true);handler.post(refresh)}
        dialog.setOnDismissListener {handler.removeCallbacks(refresh);previewViews.seek.setActive(false);preview.close();if(themePreview===dialog)themePreview=null};themePreview=dialog;dialog.show()
    }
    private fun applyTheme(spec:ThemeSpec){if(recoverySafe){Toast.makeText(this,"안전 모드에서는 테마 적용을 보류합니다. 설정에서 다시 시도해 주세요.",Toast.LENGTH_LONG).show();return};runCatching {themeStore.select(spec)}.fold({recreate()},{error->Toast.makeText(this,"테마 적용 실패: ${error.message}",Toast.LENGTH_LONG).show()})}
    private fun showIntro(spec:ThemeSpec,mode:IntroMode,notify:Boolean=false,sceneStyle:IntroScene=themeStore.introScene(spec)){
        if(recoverySafe){Toast.makeText(this,"안전 모드에서는 시작 연출을 생략합니다",Toast.LENGTH_SHORT).show();return}
        if(!startupReady)AppFaults.stage="startup animation"
        if(startup!=null || isFinishing || isDestroyed)return
        val target=RectF();lateinit var view:StartupView
        view=StartupView(this,spec,mode,target,sceneStyle){
            if(startup===view){root.removeView(view);startup=null;updateVisualActive();root.post {maybeAutoLandscape()};if(notify&&lifecycleVisible&&!isFinishing&&!isDestroyed)maybeRequestNotification()}
        }
        startup=view;root.addView(view,FrameLayout.LayoutParams(-1,-1));updateVisualActive()
        view.configureAbyss(abyssEffects())
        root.post {
            if(startup!==view)return@post
            if(!lifecycleVisible){view.cancel();return@post}
            val origin=IntArray(2);val location=IntArray(2);root.getLocationInWindow(origin);deck.getLocationInWindow(location)
            target.set((location[0]-origin[0]).toFloat(),(location[1]-origin[1]).toFloat(),(location[0]-origin[0]+deck.width).toFloat(),(location[1]-origin[1]+deck.height).toFloat())
            if(page!=0 || target.width()<=0 || target.height()<=0)target.set(0f,0f,root.width.toFloat(),root.height.toFloat())
            view.start()
        }
    }
    private fun attachPage(content:LinearLayout) { pages.addView((if(::queuePage.isInitialized&&content===queuePage)QueueScrollView(this) else ScrollView(this)).apply { isFillViewport=true;addView(content) }) }
    private fun showPage(value:Int) {
        if(value!=1&&::root.isInitialized){
            currentFocus?.clearFocus()
            root.windowInsetsController?.hide(WindowInsets.Type.ime())
        }
        playlistDialogRequest++
        if(value!=0)cancelScrub()
        if(page==1&&value!=1)rememberLibraryScroll()
        handler.removeCallbacks(searchLibrary)
        page=value;for(i in 0 until pages.childCount){val v=pages.getChildAt(i);v.animate().cancel();v.alpha=1f;v.translationY=0f;v.visibility=if(i==value)View.VISIBLE else View.GONE}
        navButtons.forEachIndexed { i,b->b.setTextColor(if(i==value || value>=4&&i==3)cyan else muted);b.isSelected=i==value||value>=4&&i==3 }
        if(hasAtmosphere&&lifecycleVisible&&motionEnabled()){
            val incoming=pages.getChildAt(value);incoming.alpha=0f;incoming.translationY=dp(8).toFloat();incoming.animate().alpha(1f).translationY(0f).setDuration(360).start()
        }
        if(value==1)populateLibrary(false);if(value==2)populateQueue();if(value==5)populateThemes();updateVisualActive();render();if(value==0)root.post {maybeAutoLandscape()}
    }
    private fun updateVisualActive() {
        if(!::deck.isInitialized)return
        if(isAbyss){val fx=abyssEffects();deck.configureAbyss(fx);immersive?.configureAbyss(fx);lyricDeck?.configureAbyss(fx);gardenBack?.configureAbyss(fx);gardenFront?.configureAbyss(fx);lyricGarden?.configureAbyss(fx)}
        deck.animationsSuppressed=recoverySafe
        val spectrum=!recoverySafe&&prefs.getBoolean("visualizer",true);val bass=!recoverySafe&&prefs.getBoolean("bass_pulse",true);val flow=!recoverySafe&&prefs.getBoolean("circuit_flow",true)
        deck.setAlbumColorEnabled(prefs.getBoolean("album_moon_color",true));immersive?.setAlbumColorEnabled(prefs.getBoolean("album_moon_color",true))
        deck.setTheme(theme,petalsScale(),prefs.getBoolean("theme_reaction",true));deck.setEffects(spectrum,bass,flow);deck.setActive(page==0 && fullOverlay==null && lifecycleVisible && startup==null && lyricOverlay==null)
        immersive?.setTheme(theme,petalsScale(),prefs.getBoolean("theme_reaction",true));immersive?.setEffects(spectrum,bass,flow);immersive?.setActive(lifecycleVisible && startup==null && lyricOverlay==null)
        if(::seek.isInitialized){seek.waveformEnabled=!recoverySafe&&!hasAtmosphere&&prefs.getBoolean("waveform",true);seek.setActive(page==0 && fullOverlay==null && lifecycleVisible && startup==null && lyricOverlay==null)}
        gardenBack?.configure(petalsScale(),page!=0);gardenFront?.configure(petalsScale(),page!=0)
        gardenBack?.setActive(lifecycleVisible&&startup==null&&lyricOverlay==null);gardenFront?.setActive(lifecycleVisible&&startup==null&&lyricOverlay==null)
        lyricGarden?.setActive(lifecycleVisible&&startup==null)
        lyricSheet?.setActive(lifecycleVisible&&startup==null)
        lyricDeck?.let {it.setAlbumColorEnabled(prefs.getBoolean("album_moon_color",true));it.setTheme(theme,petalsScale(),prefs.getBoolean("theme_reaction",true));it.setEffects(spectrum,bass,flow);it.setActive(lifecycleVisible&&startup==null)}
    }

    private fun toggleMusic(){
        if(PlaybackQueue.resumeIfPending())return
        val s=engine.state
        if(s.loading||s.preparing)PlaybackRuntime.pause(this)
        else {if(!s.playing)PlaybackQueue.markCurrent();PlaybackRuntime.toggle(this)}
    }
    private fun refreshLyricSource(source:String){
        if(lyricSource==source)return
        lyricSource=source;lyricTimeline=null;lyricPlain="";val generation=++lyricGeneration
        lyricStatus=if(source.startsWith("content://"))"가사 불러오는 중…" else "음악 파일을 먼저 선택하세요"
        if(!source.startsWith("content://"))return
        lyricStore.load(source){result->if(!isDestroyed&&generation==lyricGeneration&&engine.state.sourceUri==source){
            val content=result.getOrNull();lyricTimeline=content?.timeline;lyricPlain=content?.plain.orEmpty()
            lyricStatus=if(result.isFailure)"내장 가사 확인 실패 · 외부 LRC 연결 가능" else content?.status?:"지원하는 내장 가사 미검출 · LRC 연결 가능"
            if(result.isFailure)AudioLog.event("lyrics_read_failed",result.exceptionOrNull()?.message?:"unknown")
        }}
    }
    private fun pickLyrics(){
        val source=engine.state.sourceUri
        if(!source.startsWith("content://")){Toast.makeText(this,"음악 파일을 먼저 선택하세요",Toast.LENGTH_SHORT).show();return}
        pendingLyricSource=source
        startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {addCategory(Intent.CATEGORY_OPENABLE);type="*/*"},6)
    }
    private fun showLyricSheet(focus:Boolean=prefs.getBoolean("lyric_focus",false)&&!recoverySafe){
        if(lyricOverlay!=null||!lifecycleVisible||isDestroyed)return
        lyricFocused=focus;lyricChromeHidden=false
        val overlay=object:FrameLayout(this){
            private var waking=false
            override fun onInterceptTouchEvent(event:android.view.MotionEvent):Boolean {
                if(event.actionMasked==android.view.MotionEvent.ACTION_DOWN){waking=lyricFocused&&lyricChromeHidden;revealLyricChrome()}
                return waking||super.onInterceptTouchEvent(event)
            }
            override fun onTouchEvent(event:android.view.MotionEvent):Boolean {
                if(waking){if(event.actionMasked==android.view.MotionEvent.ACTION_UP){performClick();waking=false}else if(event.actionMasked==android.view.MotionEvent.ACTION_CANCEL)waking=false;return true}
                return super.onTouchEvent(event)
            }
            override fun performClick():Boolean {super.performClick();revealLyricChrome();return true}
        }.apply {setBackgroundColor(ink);isClickable=true};lyricOverlay=overlay
        if(hasAtmosphere&&focus){
            lyricDeck=DeckView(this).apply {lyricBackdrop=true;gardenLandscape=isAbyss&&isLandscape;setTheme(theme,petalsScale(),prefs.getBoolean("theme_reaction",true));setArtwork(currentArtwork)}
            overlay.addView(lyricDeck,FrameLayout.LayoutParams(-1,-1))
        }else if(hasAtmosphere){lyricGarden=GardenAtmosphereView(this,theme,false);overlay.addView(lyricGarden,FrameLayout.LayoutParams(-1,-1))}
        val shell=LinearLayout(this).apply {orientation=LinearLayout.VERTICAL;setPadding(dp(20),0,dp(20),dp(12))}
        shell.setOnApplyWindowInsetsListener {v,insets->val bars=insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout());v.setPadding(bars.left+dp(20),bars.top,bars.right+dp(20),bars.bottom+dp(12));insets}
        val heading=row();heading.addView(button("← 돌아가기",false){closeLyricSheet()},LinearLayout.LayoutParams(0,dp(48),1f))
        heading.addView(button(if(focus)"일반 서첩" else "집중 감상",false){val next=!lyricFocused;prefs.edit().putBoolean("lyric_focus",next).apply();closeLyricSheet(false);showLyricSheet(next)},LinearLayout.LayoutParams(0,dp(48),1f))
        shell.addView(heading);lyricChrome+=heading
        lyricTitle=text("",if(focus)14 else 18,theme.text).apply {gravity=Gravity.CENTER;maxLines=2};shell.addView(lyricTitle)
        lyricTitle?.let {lyricChrome+=it}
        val body=LinearLayout(this).apply {orientation=if(focus&&isLandscape)LinearLayout.HORIZONTAL else LinearLayout.VERTICAL}
        lyricSheet=LyricSheetView(this,theme).apply {setFocusMode(focus,isLandscape)}
        if(focus)body.addView(View(this),if(isLandscape)LinearLayout.LayoutParams(0,-1,.42f)else LinearLayout.LayoutParams(-1,0,.26f))
        body.addView(lyricSheet,if(focus&&isLandscape)LinearLayout.LayoutParams(0,-1,.58f)else LinearLayout.LayoutParams(-1,0,if(focus).74f else 1f))
        shell.addView(body,LinearLayout.LayoutParams(-1,0,1f))
        lyricInfo=text("",10,muted).apply {gravity=Gravity.CENTER;maxLines=2};if(!focus){shell.addView(lyricInfo);lyricInfo?.let {lyricChrome+=it}}
        val actions=row();actions.addView(button("LRC 연결",false){pickLyrics()},LinearLayout.LayoutParams(0,dp(48),1f))
        actions.addView(button("외부 LRC 해제",false){val source=engine.state.sourceUri;val generation=++lyricGeneration
            lyricStore.remove(source){result->if(!isDestroyed&&generation==lyricGeneration&&engine.state.sourceUri==source){
                if(result.isSuccess){lyricSource="";refreshLyricSource(source)}else lyricStatus="가사 연결 해제 실패"
            }}
        },LinearLayout.LayoutParams(0,dp(48),1f));if(!focus){shell.addView(actions);lyricChrome+=actions}
        val controls=row();controls.addView(button("◀◀",false){PlaybackQueue.previous()},LinearLayout.LayoutParams(0,dp(52),1f))
        lyricPlay=button("▶",false){toggleMusic()};controls.addView(lyricPlay,LinearLayout.LayoutParams(0,dp(52),1f));controls.addView(button("▶▶",false){PlaybackQueue.next()},LinearLayout.LayoutParams(0,dp(52),1f));shell.addView(controls);lyricChrome+=controls
        overlay.addView(shell,FrameLayout.LayoutParams(-1,-1));root.addView(overlay,FrameLayout.LayoutParams(-1,-1));shell.requestApplyInsets();updateVisualActive();render();revealLyricChrome()
        if(motionEnabled()){overlay.alpha=0f;overlay.animate().alpha(1f).setDuration(450).start()}
    }
    private fun closeLyricSheet(returnToScene:Boolean=true){
        handler.removeCallbacks(hideLyricChrome);lyricChrome.forEach {it.animate().cancel()};lyricChrome.clear();lyricChromeHidden=false;lyricFocused=false
        lyricDeck?.close();lyricDeck=null
        lyricGarden?.close();lyricGarden=null;lyricSheet=null;lyricTitle=null;lyricInfo=null;lyricPlay=null
        lyricOverlay?.let {it.animate().cancel();root.removeView(it)};lyricOverlay=null
        updateVisualActive();if(returnToScene)root.post {maybeAutoLandscape()}
    }
    private fun addAbyssFullscreenTools(panel:LinearLayout){
        abyssFullscreenSeek=WaveformSeekBar(this).apply {
            setTheme(theme);waveformEnabled=false
            max=10000;minimumHeight=dp(48);progressTintList=android.content.res.ColorStateList.valueOf(cyan);thumbTintList=android.content.res.ColorStateList.valueOf(cyan)
            contentDescription="몰입 화면 재생 위치"
            setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(bar:SeekBar?,position:Int,user:Boolean){
                    if(!user)return
                    if(touchGestureActive){if(abyssSeekToken!=null)landscapeTime?.text="${clock(abyssSeekDuration*position/10000)}로 이동"}
                    else if(abyssSeekToken==null)engine.seek((engine.state.pcm?.durationMs?:0)*position/10000)
                }
                override fun onStartTrackingTouch(bar:SeekBar?){handler.removeCallbacks(hideAbyssChrome);abyssSeekDuration=engine.state.pcm?.durationMs?:0;abyssSeekToken=engine.beginScrub()}
                override fun onStopTrackingTouch(bar:SeekBar?){val token=abyssSeekToken;abyssSeekToken=null;if(token!=null){if(touchCancelled)engine.cancelScrub(token)else if(engine.finishScrub(token,abyssSeekDuration*(bar?.progress?:0)/10000)&&token.resume)PlaybackRuntime.ensureService(this@MainActivity)};revealAbyssChrome()}
            })
        };panel.addView(abyssFullscreenSeek,LinearLayout.LayoutParams(-1,dp(48)))
        val tools=row()
        abyssFullscreenLoop=button("A–B 구간",false){
            val generation=engine.state.sourceGeneration
            AlertDialog.Builder(this).setTitle("A–B 구간 반복").setItems(arrayOf("현재 위치를 A 시작으로","현재 위치를 B 끝으로","구간 반복 해제")){_,which->
                if(engine.state.sourceGeneration==generation){when(which){0->engine.setRepeatA(engine.state.positionMs);1->engine.setRepeatB(engine.state.positionMs);else->engine.clearRepeat()}}
                revealAbyssChrome()
            }.setNegativeButton("닫기",null).show()
        }
        tools.addView(abyssFullscreenLoop,LinearLayout.LayoutParams(0,dp(52),1f));tools.addView(button("가사",false){showLyricSheet()},LinearLayout.LayoutParams(0,dp(52),1f));panel.addView(tools)
    }
    private fun createLandscapePanel(overlay:FrameLayout){
        val panel=column(12).apply {background=if(isAbyss)app.purebit.theme.abyss.AbyssGlass(theme,resources.displayMetrics.density)else GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(0x24100c18,0x70100c18))}
        landscapeTitle=text("",23,theme.text).apply {maxLines=2;ellipsize=android.text.TextUtils.TruncateAt.END};landscapeArtist=text("",12,theme.text).apply {maxLines=1;ellipsize=android.text.TextUtils.TruncateAt.END}
        landscapeSource=text("",9,cyan);landscapeTime=text("",11,muted)
        landscapeLyric=text("",14,theme.text).apply {maxLines=2;ellipsize=android.text.TextUtils.TruncateAt.END}
        listOf(landscapeTitle,landscapeArtist,landscapeSource,landscapeTime,landscapeLyric).forEach {panel.addView(it)}
        val controls=row();controls.addView(button("◀◀",false){PlaybackQueue.previous()},LinearLayout.LayoutParams(0,dp(52),1f))
        landscapePlay=button("▶",true){toggleMusic()};controls.addView(landscapePlay,LinearLayout.LayoutParams(0,dp(56),1f));controls.addView(button("▶▶",false){PlaybackQueue.next()},LinearLayout.LayoutParams(0,dp(52),1f));panel.addView(controls)
        if(!isAbyss)panel.addView(button("가사 서첩",false){showLyricSheet()});landscapeStatus=text("",10,muted).apply {maxLines=2};panel.addView(landscapeStatus)
        val scroll=ScrollView(this).apply {isFillViewport=true;tag="landscape-panel";addView(panel)}
        if(isAbyss){addAbyssFullscreenTools(panel);abyssChrome+=scroll}
        overlay.addView(scroll,FrameLayout.LayoutParams((windowManager.currentWindowMetrics.bounds.width()*.46f).toInt(),-1,Gravity.END).apply {rightMargin=dp(12);topMargin=dp(8);bottomMargin=dp(12)})
    }
    private var fullscreenHint:View?=null
    private val hideFullscreenChrome=Runnable { if(isGarden&&motionEnabled()){fullscreenHint?.animate()?.alpha(0f)?.setDuration(800)?.start()} }
    private var lifecycleVisible=false
    /** Native click remains available to accessibility; double tap is a visual shortcut. */
    @SuppressLint("ClickableViewAccessibility") // Confirmed single taps call performClick; scrolling stays with the parent.
    private fun installGardenGestures(view:View,doubleTap:()->Unit){
        val gestures=android.view.GestureDetector(this,object:android.view.GestureDetector.SimpleOnGestureListener(){
            override fun onDown(e:android.view.MotionEvent)=true
            override fun onSingleTapConfirmed(e:android.view.MotionEvent):Boolean {if(view.isAttachedToWindow)view.performClick();return true}
            override fun onDoubleTap(e:android.view.MotionEvent):Boolean {if(view.isAttachedToWindow)doubleTap();return true}
        })
        view.setOnTouchListener {_,event->gestures.onTouchEvent(event);true}
    }
    private fun maybeAutoLandscape(){
        if(hasAtmosphere&&isLandscape&&page==0&&!landscapeDismissed&&prefs.getBoolean("landscape_auto",true)&&startup==null&&lyricOverlay==null&&lifecycleVisible)showFullscreen(true)
    }
    private fun showFullscreen(automatic:Boolean=false) {
        if(fullOverlay!=null||isDestroyed||isFinishing||!lifecycleVisible)return
        fullscreenAutomatic=automatic
        val wide=hasAtmosphere&&isLandscape
        val overlay=(if(isAbyss)app.purebit.theme.abyss.AbyssImmersiveFrame(this).apply {wake={event->wakeAbyssChrome(event)};idle={revealAbyssChrome()}}else FrameLayout(this)).apply { setBackgroundColor(ink) };val view=DeckView(this).apply { immersive=true;gardenLandscape=wide }
        immersive=view;fullOverlay=overlay;overlay.addView(view,FrameLayout.LayoutParams(-1,-1))
        if(isAbyss){view.onAbyssAlbumClick={bounds->showAbyssAlbum(view,bounds)};view.importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_YES;view.isFocusable=true;view.contentDescription="어비스 전망창 · 앨범아트 원본 보기"}
        val chrome=FrameLayout(this);insetChrome(chrome);overlay.addView(chrome,FrameLayout.LayoutParams(-1,-1))
        val exit=button(if(isGarden)"← 돌아가기" else "← DECK",false){closeFullscreen()}
        if(isAbyss){abyssExit=exit;exit.text="← 돌아가기"}
        chrome.addView(exit,FrameLayout.LayoutParams(dp(116),dp(52),Gravity.TOP or Gravity.START).apply { topMargin=dp(if(isGarden)12 else 48);leftMargin=dp(20) })
        if(wide)createLandscapePanel(chrome)
        if(isAbyss&&!wide){
            val panel=column(12).apply {background=app.purebit.theme.abyss.AbyssGlass(theme,resources.displayMetrics.density)}
            val controls=row();controls.addView(button("◀◀",false){PlaybackQueue.previous()},LinearLayout.LayoutParams(0,dp(52),1f));landscapePlay=button("▶",true){toggleMusic()};controls.addView(landscapePlay,LinearLayout.LayoutParams(0,dp(56),1f));controls.addView(button("▶▶",false){PlaybackQueue.next()},LinearLayout.LayoutParams(0,dp(52),1f));panel.addView(controls)
            addAbyssFullscreenTools(panel)
            chrome.addView(panel,FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM).apply {leftMargin=dp(16);rightMargin=dp(16);bottomMargin=dp(16)});abyssChrome+=panel
        }
        if(isGarden&&!wide){
            val hint=text("두 번 누르면 돌아갑니다\n한 번 누르면 안내 표시",13,theme.text).apply {gravity=Gravity.CENTER;setPadding(dp(16),dp(12),dp(16),dp(12));background=HwahyunPanel(theme,resources.displayMetrics.density)}
            fullscreenHint=hint
            chrome.addView(hint,FrameLayout.LayoutParams(-1,dp(76),Gravity.BOTTOM).apply {leftMargin=dp(28);rightMargin=dp(28);bottomMargin=dp(48)})
            view.contentDescription="달빛 몰입 감상 · 두 번 누르거나 뒤로 가기로 종료"
            view.importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_YES;view.isFocusable=true
            view.setOnClickListener {hint.animate().cancel();hint.alpha=1f;handler.removeCallbacks(hideFullscreenChrome);handler.postDelayed(hideFullscreenChrome,3500)}
            installGardenGestures(view){closeFullscreen()}
            // Keep an explicit exit button visible even after the instruction fades.
            handler.postDelayed(hideFullscreenChrome,4500)
            if(motionEnabled()){overlay.alpha=0f;overlay.animate().alpha(1f).setDuration(650).start()}
        }
        if(wide&&isGarden){view.contentDescription="가로 달빛 감상 · 두 번 누르거나 돌아가기 버튼으로 종료";view.setOnClickListener {};installGardenGestures(view){closeFullscreen()}}
        root.addView(overlay,FrameLayout.LayoutParams(-1,-1));chrome.requestApplyInsets();updateVisualActive();render()
        if(isAbyss)revealAbyssChrome()
        val token=artToken
        PlaybackQueue.current?.let { t->library.artwork(t,512){b->if(immersive===view&&token==artToken)view.setArtwork(b)} }
    }
    private fun closeFullscreen(user:Boolean=true) {
        handler.removeCallbacks(hideAbyssChrome);abyssChrome.forEach {it.animate().cancel()};abyssChrome.clear();abyssChromeHidden=false
        abyssExit=null
        abyssSeekToken?.let {engine.cancelScrub(it)};abyssSeekToken=null;abyssFullscreenSeek?.close();abyssFullscreenSeek=null;abyssFullscreenLoop=null
        if(user&&isLandscape)landscapeDismissed=true
        landscapeTitle=null;landscapeArtist=null;landscapeSource=null;landscapeTime=null;landscapeStatus=null;landscapeLyric=null;landscapePlay=null
        handler.removeCallbacks(hideFullscreenChrome);fullscreenHint?.animate()?.cancel();fullscreenHint=null
        immersive?.close();immersive=null;fullOverlay?.let { it.animate().cancel();root.removeView(it) };fullOverlay=null;updateVisualActive()
    }
    private fun rememberLibraryScroll(){if(page==1&&!libraryScrollPending&&!handler.hasCallbacks(searchLibrary)&&::libraryPage.isInitialized)libraryScrollY=(libraryPage.parent as? ScrollView)?.scrollY?:libraryScrollY}
    private fun restoreLibraryScroll(epoch:Int){
        if(page!=1)return
        libraryScrollPending=true
        val scroll=libraryPage.parent as? ScrollView?:return
        val target=libraryScrollY
        scroll.viewTreeObserver.addOnPreDrawListener(object:android.view.ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw():Boolean {
                if(scroll.viewTreeObserver.isAlive)scroll.viewTreeObserver.removeOnPreDrawListener(this)
                if(!isDestroyed&&epoch==libraryEpoch&&page==1){scroll.scrollTo(0,target);libraryScrollPending=false}
                return true
            }
        })
    }
    private fun setLibraryQuery(value:String){
        handler.removeCallbacks(searchLibrary);libraryFilter=value.take(256)
        librarySearchProgrammatic=true
        try{librarySearch?.setText(libraryFilter)}finally{librarySearchProgrammatic=false}
    }
    private fun chooseLibraryMode(mode:LibraryMode,favorite:Boolean=false,recent:Boolean=false){
        handler.removeCallbacks(searchLibrary)
        if(mode==LibraryMode.PLAYLISTS||libraryMode==LibraryMode.PLAYLISTS)setLibraryQuery("")
        libraryMode=mode;albumView=mode==LibraryMode.ALBUMS;favoritesOnly=favorite;recentOnly=recent
        openedAlbum=null;libraryOffset=0;libraryScrollY=0;parentLibraryOffset=0;parentLibraryScroll=0
        prefs.edit().putString("library_mode",mode.name).putBoolean("album_view",albumView).apply()
        if(page==1)populateLibrary(false)
    }
    private fun openLibraryGroup(key:String){
        rememberLibraryScroll();parentLibraryOffset=libraryOffset;parentLibraryScroll=libraryScrollY
        if(libraryMode==LibraryMode.PLAYLISTS){playlistParentFilter=libraryFilter;setLibraryQuery("")}
        openedAlbum=key;libraryOffset=0;libraryScrollY=0;populateLibrary(false)
    }
    private fun closeLibraryGroup(){if(libraryMode==LibraryMode.PLAYLISTS)setLibraryQuery(playlistParentFilter);openedAlbum=null;libraryOffset=parentLibraryOffset;libraryScrollY=parentLibraryScroll;populateLibrary(false)}
    private fun libraryPaging(total:Int,size:Int){
        if(total==0)return
        if(total>size){val paging=row()
            paging.addView(button("이전",false){libraryOffset=(libraryOffset-size).coerceAtLeast(0);libraryScrollY=0;populateLibrary(false)}.apply {isEnabled=libraryOffset>0},LinearLayout.LayoutParams(0,dp(48),1f))
            paging.addView(button("다음",false){libraryOffset+=size;libraryScrollY=0;populateLibrary(false)}.apply {isEnabled=libraryOffset+size<total},LinearLayout.LayoutParams(0,dp(48),1f));libraryRows.addView(paging)
        }
        libraryRows.addView(text("${libraryOffset+1}–${minOf(libraryOffset+size,total)} / $total",11,muted).apply {gravity=Gravity.CENTER})
    }
    private fun populateLibrary(preserveScroll:Boolean=true) {
        if(!::libraryRows.isInitialized)return
        if(preserveScroll&&page==1)rememberLibraryScroll()
        val epoch=++libraryEpoch
        librarySearch?.hint=if(libraryMode==LibraryMode.PLAYLISTS&&openedAlbum==null)"재생목록 이름 검색" else "곡 · 아티스트 · 앨범 · 폴더 검색"
        libraryRows.animate().cancel();libraryRows.alpha=1f;libraryRows.rotationX=0f;libraryRows.scaleY=1f;libraryRows.translationY=0f;libraryRows.removeAllViews()
        librarySortButton?.apply {text=if(recentOnly)"최근 재생순" else "곡 정렬 · ${librarySort.label}";isEnabled=!recentOnly&&libraryMode !in listOf(LibraryMode.ALBUMS,LibraryMode.PLAYLISTS)}
        librarySortDirection?.apply {text=if(libraryDescending)"↓ 내림차순" else "↑ 오름차순";isEnabled=!recentOnly&&libraryMode !in listOf(LibraryMode.ALBUMS,LibraryMode.PLAYLISTS)}
        val active=if(favoritesOnly)"FAVORITES" else if(recentOnly)"RECENT" else libraryMode.name
        libraryTabs.forEach {(key,view)->view.isSelected=key==active;view.setTextColor(if(key==active)cyan else muted)}
        val all=library.tracks();val byId=all.associateBy {it.id}
        val scope=if(recentOnly)PlaybackQueue.recentIds(this).mapNotNull {byId[it]}else if(favoritesOnly){val ids=favorites.ids();all.filter {it.id in ids}}else all
        when(libraryMode){
            LibraryMode.TRACKS->{
                val matched=LibraryBrowser.search(scope,libraryFilter)
                val found=if(recentOnly)matched else LibraryBrowser.sorted(matched,librarySort,libraryDescending)
                libraryStatus.text="${if(favoritesOnly)"★ 즐겨찾기" else if(recentOnly)"최근 재생" else "전체 곡"} · ${found.size}곡 / ${scope.size}곡"
                renderLibraryTracks(found,isGarden)
            }
            LibraryMode.ALBUMS->renderLibraryGroups(AlbumShelf.group(scope).map {LibraryGroup(it.key,it.title,it.artist,it.tracks)},true)
            LibraryMode.ARTISTS->renderLibraryGroups(LibraryBrowser.artists(scope),false)
            LibraryMode.FOLDERS->renderLibraryGroups(LibraryBrowser.folders(scope),false)
            LibraryMode.PLAYLISTS->{readPlaylists(epoch);return}
        }
        restoreLibraryScroll(epoch)
    }
    private fun groupMatches(group:LibraryGroup):List<LibraryTrack>{
        val matches=LibraryBrowser.search(group.tracks,libraryFilter)
        return if(matches.isEmpty()&&libraryFilter.isNotBlank()&&group.title.contains(libraryFilter.trim(),true))group.tracks else matches
    }
    private fun renderLibraryGroups(groups:List<LibraryGroup>,albums:Boolean){
        val opened=groups.find {it.key==openedAlbum}
        if(openedAlbum!=null){
            libraryRows.addView(button("← ${libraryMode.label} 목록",false){closeLibraryGroup()})
            if(opened==null){libraryStatus.text="목록 정보가 변경되었습니다";libraryRows.addView(text("목록으로 돌아가 다시 선택하세요.",14,muted));return}
            val matches=groupMatches(opened)
            val found=if(albums)matches else LibraryBrowser.sorted(matches,librarySort,libraryDescending)
            libraryStatus.text="${libraryMode.label} · ${found.size}곡 / ${opened.tracks.size}곡"
            libraryRows.addView(text(opened.title,24,theme.text).apply {maxLines=3;ellipsize=android.text.TextUtils.TruncateAt.END})
            libraryRows.addView(text(opened.detail+if(albums)" · 디스크·트랙 순서" else "",12,muted))
            val actions=row();actions.addView(button("목록 재생",true){found.firstOrNull {it.available}?.let {PlaybackQueue.select(it,found,true);showPage(0)}}.apply {isEnabled=found.any {it.available}},LinearLayout.LayoutParams(0,dp(48),1f))
            actions.addView(button("다음에 추가",false){found.filter {it.available}.asReversed().forEach {PlaybackQueue.enqueueNext(it)};Toast.makeText(this,"사용 가능한 곡을 다음 재생에 추가했습니다",Toast.LENGTH_SHORT).show()}.apply {isEnabled=found.any {it.available}},LinearLayout.LayoutParams(0,dp(48),1f));libraryRows.addView(actions)
            renderLibraryTracks(found,false);return
        }
        val display=groups.filter {groupMatches(it).isNotEmpty()}
        libraryStatus.text="${libraryMode.label} · ${display.size}개 · 이름순"
        val size=if(albums)20 else 60;libraryOffset=LibraryBrowser.pageOffset(libraryOffset,display.size,size)
        if(display.isEmpty())libraryRows.addView(text("검색 결과가 없습니다. 검색어를 지우거나 음악 폴더를 추가하세요.",14,muted))
        if(albums)display.drop(libraryOffset).take(size).chunked(2).forEach {pair->val line=row().apply {gravity=Gravity.TOP}
            pair.forEach {group->
                val tile=column(8).apply {background=if(isGarden)HwahyunPanel(theme,resources.displayMetrics.density)else rounded(theme.surface,14);isFocusable=true}
                val cover=libraryCover();tile.addView(cover,LinearLayout.LayoutParams(-1,dp(136)))
                tile.addView(text(group.title,15,theme.text).apply {maxLines=2;minHeight=dp(48)});tile.addView(text("${group.detail} · ${group.tracks.size}곡",11,muted).apply {maxLines=2})
                tile.setOnClickListener {openLibraryGroup(group.key)}
                line.addView(tile,LinearLayout.LayoutParams(0,-2,1f).apply {setMargins(dp(4),dp(4),dp(4),dp(8))})
                group.tracks.firstOrNull {it.available}?.let {loadLibraryCover(it,cover)}
            };if(pair.size==1)line.addView(View(this),LinearLayout.LayoutParams(0,1,1f));libraryRows.addView(line)
        }else display.drop(libraryOffset).take(size).forEach {group->
            val tile=card().apply {isFocusable=true;setOnClickListener {openLibraryGroup(group.key)}}
            tile.addView(text(group.title,18,theme.text).apply {maxLines=3;ellipsize=android.text.TextUtils.TruncateAt.END})
            tile.addView(text("${group.tracks.size}곡 · ${group.detail}",12,muted));libraryRows.addView(tile)
        }
        libraryPaging(display.size,size)
    }
    private fun libraryCover()=ImageView(this).apply {
        scaleType=ImageView.ScaleType.CENTER_CROP;importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
        if(isGarden)setImageBitmap(GardenArt.moon(this@MainActivity))else {setImageResource(R.drawable.ic_purebit);background=rounded(theme.surface,8)}
    }
    private fun loadLibraryCover(track:LibraryTrack,view:ImageView){
        val epoch=libraryEpoch
        library.artwork(track,256,isCurrent={epoch==libraryEpoch}){bitmap->if(epoch==libraryEpoch&&!isDestroyed){if(bitmap!=null)view.setImageBitmap(bitmap)}else bitmap?.recycle()}
    }
    private fun renderLibraryTracks(found:List<LibraryTrack>,grid:Boolean){
        val size=if(grid)20 else 80;libraryOffset=LibraryBrowser.pageOffset(libraryOffset,found.size,size)
        if(found.isEmpty()){libraryRows.addView(text(if(libraryFilter.isNotBlank())"검색 결과가 없습니다." else if(favoritesOnly)"별에 담은 선율이 없습니다. 재생 화면의 별이나 곡 길게 누르기로 추가하세요."else "표시할 곡이 없습니다. 폴더나 FLAC/WAV 파일을 추가하세요.",15,theme.text));return}
        fun connect(view:View,track:LibraryTrack){
            view.isFocusable=true;view.alpha=if(track.available)1f else .5f
            view.setOnLongClickListener {showTrackOptions(track,found);true}
            view.setOnClickListener {if(track.available){PlaybackQueue.select(track,found,true);showPage(0)}else Toast.makeText(this,"파일 권한과 연결을 확인하세요",Toast.LENGTH_SHORT).show()}
        }
        if(grid)found.drop(libraryOffset).take(size).chunked(2).forEach {pair->val line=row().apply {gravity=Gravity.TOP}
            pair.forEach {track->
                val tile=column(8).apply {background=HwahyunPanel(theme,resources.displayMetrics.density)};connect(tile,track)
                val cover=libraryCover();tile.addView(cover,LinearLayout.LayoutParams(-1,dp(138)));loadLibraryCover(track,cover)
                tile.addView(text((if(favorites.contains(track.id))"★ " else "")+track.title.ifBlank {track.displayName},14,theme.text).apply {maxLines=2;minHeight=dp(48)})
                tile.addView(text(track.artist.ifBlank {"알 수 없는 아티스트"},11,muted).apply {maxLines=1})
                line.addView(tile,LinearLayout.LayoutParams(0,-2,1f).apply {setMargins(dp(4),dp(4),dp(4),dp(8))})
            };if(pair.size==1)line.addView(View(this),LinearLayout.LayoutParams(0,1,1f));libraryRows.addView(line)
        }else found.drop(libraryOffset).take(size).forEach {track->
            val tile=card();connect(tile,track)
            tile.addView(text((if(favorites.contains(track.id))"★ " else "")+track.title.ifBlank {track.displayName},16,theme.text).apply {maxLines=2})
            tile.addView(text("${track.artist.ifBlank {"알 수 없는 아티스트"}} · ${clock(track.durationMs)}${if(!track.available)" · 접근 불가" else ""}",12,muted));libraryRows.addView(tile)
        }
        libraryPaging(found.size,size)
    }
    private fun showTrackOptions(track:LibraryTrack,candidates:List<LibraryTrack> = library.tracks()){
        AlertDialog.Builder(this).setTitle(track.title).setItems(arrayOf("지금 재생","다음에 재생",if(favorites.contains(track.id))"★ 즐겨찾기 해제" else "☆ 즐겨찾기 추가","재생목록에 추가")){_,which->
            if(which==3){addToPlaylist(track);return@setItems}
            if(which==2){toggleFavorite(track);return@setItems}
            if(!track.available){Toast.makeText(this,"파일 권한을 확인하세요",Toast.LENGTH_SHORT).show();return@setItems}
            if(which==0){PlaybackQueue.select(track,candidates,true);showPage(0)}else if(track.id==PlaybackQueue.current?.id){Toast.makeText(this,"현재 선택된 곡입니다",Toast.LENGTH_SHORT).show()}else{PlaybackQueue.enqueueNext(track);Toast.makeText(this,"다음 재생에 추가했습니다",Toast.LENGTH_SHORT).show()}
        }.setNegativeButton("닫기",null).show()
    }
    private fun playlistError(error:Throwable):String = if(error is java.io.IOException)"목록 파일을 읽거나 저장하지 못했습니다. 저장 공간을 확인하고 다시 시도하세요."else error.message?:"재생목록 작업을 완료하지 못했습니다"
    private fun <T> playlistWrite(message:String,complete:((Result<T>)->Unit)?=null,work:((Result<T>)->Unit)->Unit){
        if(playlistBusy){complete?.invoke(Result.failure(IllegalStateException("다른 목록을 저장 중입니다. 잠시 후 다시 시도하세요")));return}
        playlistBusy=true
        if(page==1&&libraryMode==LibraryMode.PLAYLISTS)libraryStatus.text="목록 저장 중…"
        work {result->
            playlistBusy=false
            // The write is process-owned; a destroyed Activity must not receive UI completion.
            if(isDestroyed||isFinishing){result.exceptionOrNull()?.let {AudioLog.error("playlist_store",it)};return@work}
            val groupBefore=openedAlbum
            complete?.invoke(result)
            result.fold({if(lifecycleVisible)Toast.makeText(this,message,Toast.LENGTH_SHORT).show()},{error->AudioLog.error("playlist_store",error);if(lifecycleVisible)Toast.makeText(this,playlistError(error),Toast.LENGTH_LONG).show()})
            if(lifecycleVisible&&page==1&&libraryMode==LibraryMode.PLAYLISTS)populateLibrary(groupBefore==openedAlbum)
        }
    }
    private fun dismissPlaylistDialog(){playlistDialogRequest++;playlistDialog?.dismiss();playlistDialog=null}
    private fun newPlaylist(ids:List<String> = emptyList()){
        dismissPlaylistDialog()
        playlistDialog=PlaylistDialogs.name(this,if(ids.isEmpty())"새 재생목록" else "대기열·선택 곡 저장",""){name,done->
            playlistWrite<SavedPlaylist>("재생목록을 저장했습니다",{result->if(result.isSuccess&&page==1&&libraryMode==LibraryMode.PLAYLISTS&&openedAlbum==null)setLibraryQuery("");done(result.map {Unit})}){callback->playlistStore.create(name,ids,callback)}
        }
    }
    private fun addToPlaylist(track:LibraryTrack){
        dismissPlaylistDialog()
        val request=playlistDialogRequest;val epoch=libraryEpoch
        playlistStore.list {result->if(isDestroyed||isFinishing||!lifecycleVisible||request!=playlistDialogRequest||epoch!=libraryEpoch)return@list
            result.fold({listing->
                val choices=arrayOf("＋ 새 재생목록")+listing.playlists.map {"${it.name} · ${it.trackIds.size}곡"}
                playlistDialog?.dismiss()
                playlistDialog=AlertDialog.Builder(this).setTitle("${track.title} 추가").setItems(choices){_,index->
                    if(index==0)newPlaylist(listOf(track.id))else playlistWrite<SavedPlaylist>("목록에 곡을 담았습니다"){callback->playlistStore.append(listing.playlists[index-1].id,listOf(track.id),callback)}
                }.setNegativeButton("취소",null).show()
            },{Toast.makeText(this,playlistError(it),Toast.LENGTH_LONG).show()})
        }
    }
    private fun readPlaylists(epoch:Int){
        libraryStatus.text="재생목록 읽는 중…"
        val request=playlistRefresh.begin(playlistStore.revision)
        playlistStore.list {result->if(!playlistRefresh.complete(request)||isDestroyed||isFinishing||!lifecycleVisible||page!=1||libraryMode!=LibraryMode.PLAYLISTS||epoch!=libraryEpoch)return@list
            result.fold({listing->renderPlaylists(listing);restoreLibraryScroll(epoch)},{error->libraryStatus.text="목록을 읽지 못했습니다";libraryRows.addView(text(playlistError(error),14,muted))})
        }
    }
    private fun renderPlaylists(listing:PlaylistListing){
        libraryRows.removeAllViews()
        if(listing.unreadable>0)libraryRows.addView(text("읽을 수 없는 목록 ${listing.unreadable}개 · 파일은 보존했습니다",12,muted))
        val selected=listing.playlists.find {it.id==openedAlbum}
        if(openedAlbum!=null){
            libraryRows.addView(button("← 재생목록",false){closeLibraryGroup()})
            if(selected==null){libraryStatus.text="목록을 찾을 수 없습니다";return}
            renderPlaylist(selected);return
        }
        val words=libraryFilter.trim().lowercase(java.util.Locale.ROOT).split(Regex("\\s+")).filter {it.isNotBlank()}
        val matches=listing.playlists.filter {p->words.all {p.name.lowercase(java.util.Locale.ROOT).contains(it)}}
        libraryStatus.text="재생목록 ${matches.size}개 · 이름순"
        libraryRows.addView(button("＋ 새 재생목록",true){newPlaylist()})
        if(matches.isEmpty())libraryRows.addView(text("대기열을 저장하거나 새 재생목록을 만들어 곡을 담아보세요.",15,theme.text))
        libraryOffset=LibraryBrowser.pageOffset(libraryOffset,matches.size,20)
        matches.drop(libraryOffset).take(20).forEach {p->val tile=card().apply {isFocusable=true;setOnClickListener {openLibraryGroup(p.id)}}
            tile.addView(text(p.name,20,theme.text).apply {maxLines=2});tile.addView(text("${p.trackIds.size}곡 · 눌러서 보기·편집",12,muted));libraryRows.addView(tile)
        };libraryPaging(matches.size,20)
    }
    private fun renderPlaylist(saved:SavedPlaylist){
        val catalogue=library.tracks().associateBy {it.id}
        val playable=saved.trackIds.mapNotNull {catalogue[it]?.takeIf {t->t.available}}
        libraryStatus.text="${saved.trackIds.size}곡 · 사용 가능 ${playable.size}곡"
        libraryRows.addView(text(saved.name,24,theme.text).apply {maxLines=2})
        val management=row()
        management.addView(button("이름 변경",false){
            dismissPlaylistDialog()
            playlistDialog=PlaylistDialogs.name(this,"재생목록 이름",saved.name){name,done->playlistWrite<SavedPlaylist>("이름을 변경했습니다",{if(it.isSuccess&&openedAlbum==saved.id)playlistParentFilter="";done(it.map {Unit})}){callback->playlistStore.rename(saved.id,saved.revision,name,callback)}}
        },LinearLayout.LayoutParams(0,dp(48),1f))
        management.addView(button("목록 삭제",false){
            dismissPlaylistDialog()
            playlistDialog=AlertDialog.Builder(this).setTitle("${saved.name} 삭제").setMessage("이 재생목록을 삭제할까요? 음악 파일과 현재 재생 대기열은 유지됩니다.").setPositiveButton("목록 삭제"){_,_->
                playlistWrite<Unit>("목록을 삭제했습니다",{if(it.isSuccess&&openedAlbum==saved.id&&libraryMode==LibraryMode.PLAYLISTS){setLibraryQuery(playlistParentFilter);openedAlbum=null;libraryOffset=parentLibraryOffset;libraryScrollY=parentLibraryScroll}}){callback->playlistStore.delete(saved.id,saved.revision,callback)}
            }.setNegativeButton("취소",null).show()
        },LinearLayout.LayoutParams(0,dp(48),1f));libraryRows.addView(management)
        val actions=row()
        actions.addView(button("목록 재생",true){playable.firstOrNull()?.let {PlaybackQueue.select(it,playable,true);showPage(0)}}.apply {isEnabled=playable.isNotEmpty()},LinearLayout.LayoutParams(0,dp(48),1f))
        actions.addView(button("대기열에 담기",false){val count=PlaybackQueue.append(playable);render();Toast.makeText(this,"대기열 끝에 ${count}곡을 담았습니다",Toast.LENGTH_SHORT).show()}.apply {isEnabled=playable.isNotEmpty()},LinearLayout.LayoutParams(0,dp(48),1f));libraryRows.addView(actions)
        libraryRows.addView(button("＋ 곡 추가",false){
            dismissPlaylistDialog()
            playlistDialog=PlaylistDialogs.tracks(this,theme,library.tracks(),saved.trackIds.toSet()){ids->playlistWrite<SavedPlaylist>("선택한 곡을 담았습니다"){callback->playlistStore.append(saved.id,ids,callback)}}
        })
        libraryRows.addView(text("편집은 자동 저장 · 현재 대기열은 별도로 유지\n검색어를 지우면 순서를 바꿀 수 있습니다",11,muted))
        val matching=LibraryBrowser.search(saved.trackIds.mapNotNull {catalogue[it]},libraryFilter).map {it.id}.toSet()
        val visible=saved.trackIds.filter {libraryFilter.isBlank()||it in matching}
        if(visible.isEmpty())libraryRows.addView(text(if(saved.trackIds.isEmpty())"아직 곡이 없습니다. 곡 추가를 눌러 담아보세요."else "검색 결과가 없습니다.",14,muted))
        libraryOffset=LibraryBrowser.pageOffset(libraryOffset,visible.size,60)
        val indices=saved.trackIds.withIndex().associate {it.value to it.index}
        visible.drop(libraryOffset).take(60).forEach {id->
            val track=catalogue[id];val index=indices.getValue(id)
            val panel=card();val words=text("${index+1}. ${track?.title?:"라이브러리에 없는 곡"}\n${if(track?.available==true)track.artist.ifBlank {"알 수 없는 아티스트"}else "파일·폴더 접근 권한을 확인하세요"}",14,if(track?.available==true)theme.text else muted).apply {maxLines=3;isFocusable=true;setOnClickListener {if(track?.available==true){PlaybackQueue.select(track,playable,true);showPage(0)}}}
            panel.addView(words)
            val edits=row()
            edits.addView(button("↑",false){playlistWrite<SavedPlaylist>("순서를 저장했습니다"){callback->playlistStore.move(saved.id,saved.revision,id,saved.trackIds[index-1],callback)}}.apply {isEnabled=index>0&&libraryFilter.isBlank()&&!playlistBusy;contentDescription="위로 이동"},LinearLayout.LayoutParams(0,dp(44),1f))
            edits.addView(button("↓",false){playlistWrite<SavedPlaylist>("순서를 저장했습니다"){callback->playlistStore.move(saved.id,saved.revision,id,saved.trackIds[index+1],callback)}}.apply {isEnabled=index<saved.trackIds.lastIndex&&libraryFilter.isBlank()&&!playlistBusy;contentDescription="아래로 이동"},LinearLayout.LayoutParams(0,dp(44),1f))
            edits.addView(button("목록에서 제거",false){playlistWrite<SavedPlaylist>("목록에서 제거했습니다"){callback->playlistStore.remove(saved.id,saved.revision,id,callback)}},LinearLayout.LayoutParams(0,dp(44),2f));panel.addView(edits);libraryRows.addView(panel)
        };libraryPaging(visible.size,60)
    }
    private fun populateQueue(){
        if(!::queueRows.isInitialized||queueDragging)return
        shownQueueRevision=PlaybackQueue.revision;queueRows.removeAllViews();val tracks=PlaybackQueue.tracks
        queueSaveButton?.isEnabled=tracks.isNotEmpty()
        queueTrimButton?.isEnabled=tracks.size>1&&PlaybackQueue.current!=null
        if(tracks.isEmpty())queueRows.addView(text("곡을 길게 눌러 다음에 재생을 추가하세요.",14,muted))
        queueOffset=queueOffset.coerceAtMost((tracks.size-1).coerceAtLeast(0)/80*80)
        tracks.drop(queueOffset).take(80).forEachIndexed {relative,t->
            val i=relative+queueOffset;val selected=t.id==PlaybackQueue.current?.id
            val cell=SwipeQueueRow(this,!selected){val removed=PlaybackQueue.remove(t.id);if(removed)populateQueue();removed}
            val line=row().apply {background=if(isGarden)HwahyunPanel(theme,resources.displayMetrics.density)else rounded(theme.surface,14)}
            val handle=button("≡",false){}.apply {contentDescription="${t.title} · 길게 눌러 순서 이동"}
            val begin=View.OnLongClickListener {
                val token=QueueDragToken(t.id,PlaybackQueue.revision)
                val ok=line.startDragAndDrop(android.content.ClipData.newPlainText("BITWIRE queue",t.id),View.DragShadowBuilder(line),token,0)
                queueDragging=ok;ok
            }
            handle.setOnLongClickListener(begin);line.addView(handle,LinearLayout.LayoutParams(dp(38),dp(64)))
            val words=column(6).apply {isFocusable=true;contentDescription="${t.title}, ${t.artist}${if(selected)", 현재 선택 곡" else ""}";setOnClickListener {PlaybackQueue.playIndex(PlaybackQueue.tracks.indexOfFirst {it.id==t.id});showPage(0)};setOnLongClickListener(begin)}
            words.addView(text("${i+1}. ${t.title}",15,if(selected)cyan else theme.text).apply {maxLines=2});words.addView(text(t.artist.ifBlank {"알 수 없는 아티스트"},10,muted).apply {maxLines=1})
            line.addView(words,LinearLayout.LayoutParams(0,-2,1f))
            line.addView(button("↑",false){val at=PlaybackQueue.tracks.indexOfFirst {it.id==t.id};PlaybackQueue.move(at,at-1);populateQueue()}.apply {contentDescription="${t.title} 위로 이동";isEnabled=i>0},LinearLayout.LayoutParams(dp(40),dp(48)))
            line.addView(button("↓",false){val at=PlaybackQueue.tracks.indexOfFirst {it.id==t.id};PlaybackQueue.move(at,at+1);populateQueue()}.apply {contentDescription="${t.title} 아래로 이동";isEnabled=i<tracks.lastIndex},LinearLayout.LayoutParams(dp(40),dp(48)))
            line.addView(button("×",false){PlaybackQueue.remove(t.id);populateQueue()}.apply {isEnabled=!selected;contentDescription=if(selected)"현재 선택 곡 유지" else "${t.title} 대기열에서 제거"},LinearLayout.LayoutParams(dp(40),dp(48)))
            cell.addView(line,FrameLayout.LayoutParams(-1,-2));queueRows.addView(cell,LinearLayout.LayoutParams(-1,-2).apply {bottomMargin=dp(6)})
            cell.setOnDragListener {v,event->val token=event.localState as? QueueDragToken
                if(token==null)false else when(event.action){
                    android.view.DragEvent.ACTION_DRAG_STARTED->true
                    android.view.DragEvent.ACTION_DRAG_ENTERED->{v.alpha=.65f;true}
                    android.view.DragEvent.ACTION_DRAG_EXITED->{v.alpha=1f;true}
                    android.view.DragEvent.ACTION_DROP->{PlaybackQueue.moveBeforeDrop(token.id,t.id,token.revision);true}
                    android.view.DragEvent.ACTION_DRAG_ENDED->{v.alpha=1f;queueDragging=false;handler.post {if(!isDestroyed&&shownQueueRevision!=PlaybackQueue.revision)populateQueue()};true}
                    else->true
                }
            }
        }
        if(tracks.size>80){val paging=row();paging.addView(button("이전 80곡",false){queueOffset=(queueOffset-80).coerceAtLeast(0);populateQueue()},LinearLayout.LayoutParams(0,dp(48),1f));paging.addView(button("다음 80곡",false){if(queueOffset+80<tracks.size)queueOffset+=80;populateQueue()},LinearLayout.LayoutParams(0,dp(48),1f));queueRows.addView(paging)}
    }
    private fun rescanFolders() {
        val trees=library.trees();if(trees.isEmpty()){Toast.makeText(this,"먼저 폴더를 추가하세요",Toast.LENGTH_SHORT).show();return}
        libraryStatus.text="폴더 읽는 중…";trees.forEach { library.scanTree(it){result->if(!isDestroyed){populateLibrary();result.exceptionOrNull()?.let { e->Toast.makeText(this,e.message,Toast.LENGTH_LONG).show() }} } }
    }
    private fun pickFile() { startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply { addCategory(Intent.CATEGORY_OPENABLE);type="*/*";putExtra(Intent.EXTRA_MIME_TYPES,arrayOf("audio/*","application/octet-stream"));addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION) },1) }
    private fun maybeRequestNotification() { if(!lifecycleVisible || isFinishing || isDestroyed)return; if(Build.VERSION.SDK_INT>=33 && !prefs.getBoolean("notification_asked",false)){prefs.edit().putBoolean("notification_asked",true).apply();if(checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),9)} }
    private fun render() {
        if(!::title.isInitialized)return
        if(lifecycleVisible&&page==1&&libraryMode==LibraryMode.PLAYLISTS&&!playlistBusy&&!handler.hasCallbacks(searchLibrary)&&playlistRefresh.needsRefresh(playlistStore.revision))populateLibrary()
        if(page==2&&shownQueueRevision!=PlaybackQueue.revision&&!queueDragging)populateQueue()
        val s=engine.state;val p=s.pcm
        val nightNow=SystemClock.uptimeMillis()
        if(lifecycleVisible){GardenNight.state.advance(if(nightTickAt==0L)0f else (nightNow-nightTickAt)/1000f,isGarden&&s.playing&&startup==null,motionEnabled());nightTickAt=nightNow}
        val dormant=PlaybackQueue.pendingPositionMs?.takeIf {s.sourceUri.isBlank()&&!s.loading}
        val metadata=PlaybackQueue.current?.takeIf {it.uriString==s.sourceUri||dormant!=null}
        val displayPosition=dormant?:s.positionMs;val displayDuration=p?.durationMs?:if(dormant!=null)metadata?.durationMs?:0 else 0
        favoriteButton?.apply {isEnabled=metadata!=null;val selected=metadata!=null&&favorites.contains(metadata.id);text=if(isAbyss){if(selected)"★" else "☆"}else if(selected)"★ 즐겨찾기" else "☆ 즐겨찾기";contentDescription=if(selected)"즐겨찾기 해제" else "즐겨찾기 추가"}
        if(dragging && (scrubToken?.generation!=s.sourceGeneration || s.loading))cancelScrub()
        title.text=metadata?.title ?: if(p==null&&!s.loading)"당신의 음악을 연결하세요" else s.filename.substringBeforeLast('.')
        artist.text=metadata?.artist?.ifBlank { "알 수 없는 아티스트" } ?: if(p==null){if(hasAtmosphere)"FLAC / WAV를 선택하세요" else "폴더를 추가하거나 FLAC / WAV 파일을 선택하세요"} else "로컬 오디오"
        source.text=p?.let { "${it.codec} · ${it.format.bits}-BIT · ${it.format.rate/1000.0} kHz" } ?: if(s.loading)"PREPARING SOURCE" else if(dormant!=null)if(PlaybackQueue.pendingRestored)"LAST SESSION · TAP PLAY"else "QUEUE READY · TAP PLAY" else "LOCAL AUDIO · READY"
        route.text=if(s.usbDirectMode)"● USB DIRECT${if(!s.playing)" · 대기" else ""}" else "● ANDROID OUTPUT"
        route.setTextColor(if(s.usbDirectMode)cyan else violet)
        modeValue.text=if(s.usbDirectMode)"선택됨: USB Direct · 하드웨어 볼륨 검증" else "선택됨: Android 출력"
        message.text=when { dormant!=null->if(PlaybackQueue.pendingRestored)"${clock(dormant)}부터 이어듣기 · 재생을 누르세요"else "대기열 준비됨 · 재생을 누르세요";s.sourceUri.isBlank()&&PlaybackQueue.restoreNotice.isNotBlank()->PlaybackQueue.restoreNotice;dragging->if(s.playing)"음악은 계속 재생 중 · 놓으면 선택 위치로 이동" else "탐색 미리보기 · 놓으면 선택 위치로 이동";s.loading->"음원 준비 중…";s.preparing->"출력 준비 중…";s.message.startsWith("A–B:")->s.message;s.message.startsWith("Error:")->s.message;s.message.startsWith("USB detached")->"USB 연결 해제 · 재연결 후 재생하세요";s.message.startsWith("Audio focus lost")->"다른 오디오 요청으로 일시정지됨";s.message.startsWith("Headphone route became noisy")->"이어폰 경로 변경으로 정지됨 · 연결 확인 후 재생하세요";else->if(s.playing)"${if(s.usbDirectMode)"USB DIRECT" else "ANDROID"}  ·  ${s.routedName.substringBefore(" (id=")}" else if(p==null)if(isAbyss)"음악과 함께 깊은 바다로" else "원음을 잇는 디지털 데크" else "재생 준비됨" }
        if(hasAtmosphere){
            if(dormant==null&&PlaybackQueue.restoreNotice.isBlank()&&!dragging&&!s.loading&&!s.preparing&&!s.message.startsWith("Error:")&&!s.message.startsWith("USB detached")&&!s.message.startsWith("Audio focus lost")&&!s.message.startsWith("Headphone route became noisy")&&!s.message.startsWith("A–B:"))message.text=""
            message.visibility=if(message.text.isBlank())View.GONE else View.VISIBLE
        }
        volumeText.text=if(s.usbDirectMode)"USB DAC · ${s.hardwareVolumeRaw?.let { "%.1f dB".format(it/256.0) } ?: "재생 중 조절"}" else "휴대폰 미디어 음량"
        volumeUp.isEnabled=!s.usbDirectMode || (s.playing && s.usbHardwareMusic)
        volumeDown.isEnabled=volumeUp.isEnabled
        play.contentDescription=if(s.preparing||s.loading)"재생 시작 취소" else if(s.playing)"일시정지" else "재생"
        play.text=if(s.preparing || s.loading)"■" else if(s.playing)"Ⅱ" else "▶";play.isEnabled=p!=null||s.loading||s.preparing||dormant!=null
        seek.isEnabled=p!=null&&!s.loading&&!s.preparing
        time.text=if(dragging)scrubTime(s.positionMs) else if(isAbyss)"${clock(displayPosition)}  /  ${clock(displayDuration)}" else "${clock(displayPosition)}     ───────────     ${clock(displayDuration)}"
        if(!dragging)seek.progress=if(displayDuration>0)(displayPosition*seek.max/displayDuration).toInt().coerceIn(0,seek.max) else 0
        val loop=s.abRepeat
        loopToggle?.apply {
            text=if(isAbyss)"A–B" else if(loop.active)"A–B · 반복 중" else "A–B 구간"
            if(isAbyss){setTextColor(if(loop.active)cyan else theme.text);isSelected=loop.active;contentDescription=if(loop.active)"A–B 구간 반복 중 · 설정 열기" else "A–B 구간 반복 설정"}
        }
        loopA.text=loop.aMs?.let { "A ${preciseClock(it)}" }?:"A 시작";loopB.text=loop.bMs?.let { "B ${preciseClock(it)}" }?:"B 끝"
        loopA.isEnabled=p!=null&&!s.loading&&!s.preparing&&!dragging&&s.comparisonRate==0
        loopB.isEnabled=loopA.isEnabled&&loop.aMs!=null;loopClear.isEnabled=loopA.isEnabled&&loop.aMs!=null
        loopA.setTextColor(if(loop.aMs!=null)cyan else muted);loopB.setTextColor(if(loop.active)violet else muted)
        loopStatus.text=if(loop.active)"A–B ON · ${s.abCycles}회 · 구간 밖 탐색은 A로 이동" else if(loop.aMs!=null)"B 끝점을 지정하세요 · 최소 1초 구간" else "A–B 구간 반복 · A 시작 → B 끝"
        seek.setLoop(loop.aMs,loop.bMs,p?.durationMs?:0)
        shuffleButton.setTextColor(if(PlaybackQueue.shuffle)cyan else muted);repeatButton.text=if(PlaybackQueue.repeat==PlaybackQueue.Repeat.ONE)"↻¹" else "↻";repeatButton.setTextColor(if(PlaybackQueue.repeat!=PlaybackQueue.Repeat.OFF)cyan else muted)
        seek.setSource(p)
        gardenBack?.setPlaying(s.playing);gardenFront?.setPlaying(s.playing)
        deck.update(p,s.positionMs,s.playing);immersive?.update(p,s.positionMs,s.playing)
        val artworkKey=metadata?.uriString?:s.sourceUri
        if(artworkKey!=lastFilename){
            closeAbyssAlbum()
            lastFilename=artworkKey;val token=++artToken
            if(isGarden&&motionEnabled()&&prefs.getBoolean("track_transition",true)){gardenFront?.trackChanged();lyricGarden?.trackChanged()}
            // Retain the old cover until the new async request completes, including no-art results.
            fun acceptArt(bitmap:Bitmap?){if(token==artToken&&!isDestroyed){currentArtwork=bitmap;deck.setArtwork(bitmap);immersive?.setArtwork(bitmap);lyricDeck?.setArtwork(bitmap);if(bitmap!=null)abyssAlbum?.setArtwork(bitmap)else closeAbyssAlbum()}}
            if(metadata!=null)library.artwork(metadata,512){acceptArt(it)}else acceptArt(null)
        }
        refreshLyricSource(s.sourceUri)
        mini.visibility=if(page!=0&&(p!=null||s.loading||s.preparing||dormant!=null))View.VISIBLE else View.GONE
        mini.update(title.text.toString(),artist.text.toString(),s.playing,s.loading||s.preparing,p!=null||dormant!=null,displayPosition,displayDuration,currentArtwork)
        lyricTitle?.text=title.text;lyricInfo?.text="$lyricStatus · ${clock(s.positionMs)}";lyricSheet?.show(lyricTimeline,s.positionMs,lyricPlain)
        lyricPlay?.apply {text=play.text;contentDescription=play.contentDescription;isEnabled=play.isEnabled}
        lyricDeck?.update(p,s.positionMs,s.playing);lyricGarden?.setPlaying(s.playing)
        landscapeTitle?.text=title.text;landscapeArtist?.text=artist.text;landscapeSource?.text=source.text;if(abyssSeekToken==null)landscapeTime?.text="${clock(displayPosition)} / ${clock(displayDuration)}"
        landscapeStatus?.text=if(s.message.startsWith("Error:")||s.loading||s.preparing)message.text else "${if(s.usbDirectMode)"USB DIRECT" else "ANDROID"} · ${s.routedName.substringBefore(" (id=")}"
        landscapePlay?.apply {text=play.text;contentDescription=play.contentDescription;isEnabled=play.isEnabled}
        if(abyssSeekToken?.generation?.let {it!=s.sourceGeneration}==true||s.loading)cancelAbyssSeek()
        abyssFullscreenSeek?.apply {isEnabled=seek.isEnabled;if(abyssSeekToken==null)progress=if(displayDuration>0)(displayPosition*10000/displayDuration).toInt().coerceIn(0,10000)else 0}
        abyssFullscreenLoop?.apply {isEnabled=loopA.isEnabled;text=if(s.abRepeat.active)"A–B · 반복 중" else "A–B 구간"}
        val cue=lyricTimeline?.activeIndex(s.positionMs)?:-1
        landscapeLyric?.text=lyricTimeline?.lines?.getOrNull(cue)?.text?:if(lyricPlain.isNotBlank())"내장 가사 · 서첩에서 전체 보기" else ""
        if(page==4)details.text=diagnosticText(s)
    }
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun registerLegacyPermissionReceiver(filter: IntentFilter) {
        // Lint does not recognize the version guard and custom signature protection here.
        // This overload is used only on 31/32; NOT_EXPORTED is implemented on 33+ above.
        check(Build.VERSION.SDK_INT < 33)
        registerReceiver(usbReceiver, filter, "app.purebit.permission.USB_INTERNAL", null)
    }
    private fun showPolicyOptions() {
        AlertDialog.Builder(this).setTitle("USB mixer 정책")
            .setMessage("기본은 DEFAULT mixer입니다. 지원 기기에서는 BIT_PERFECT 정책을 요청할 수 있지만, 실제 경로 검증 결과는 아닙니다.\n\n이 정책은 Android 볼륨 조절을 생략할 수 있어 현재의 낮은 시스템 볼륨도 보호가 되지 않을 수 있습니다. DAC 하드웨어 볼륨을 낮추고 이어폰을 귀에서 뺀 뒤 테스트하세요. 하드웨어 볼륨이 없으면 기본 모드를 유지하세요.\n\n정책은 장치 변경 시 해제되며, 앱은 시스템 볼륨을 변경하지 않습니다.")
            .setPositiveButton("하드웨어 볼륨 확인 · 요청 허용") { _, _ -> engine.armBitPerfectPolicy(true) }
            .setNeutralButton("DEFAULT 사용") { _, _ -> engine.armBitPerfectPolicy(false) }
            .setNegativeButton("취소", null).show()
    }
    private fun showUsbDirectOptions() {
        AlertDialog.Builder(this).setTitle("USB Direct 실험 — Samsung 04e8:a05b")
            .setMessage("휴대폰 볼륨은 USB 직접 전송에 적용되지 않을 수 있습니다. 이어폰을 귀에서 뺀 상태로 테스트하세요.\n\n5초짜리 24-bit/48 kHz, -60 dBFS 테스트 WAV를 준비합니다. 첫/끝 1초는 무음입니다. 30초 이하·최대 진폭 -60 dBFS 이하의 PCM만 허용하며 일반 음악을 변환/감쇠해서 재생하지 않습니다.\n\nPLAY를 누르면 Android USB 오디오 드라이버에서 출력 interface를 인계받고 clock/alternate를 설정하는 실험을 합니다. 실패 시 자동 fallback은 없습니다. 종료 후 Android 출력이 복구되지 않으면 이어폰을 뺐다 다시 연결하세요. 다른 앱의 USB 소리/알림이 잠시 나오지 않을 수 있습니다.\n\n성공한 USB 패킷 전송도 DAC 샘플 무변형 검증은 아닙니다.")
            .setPositiveButton("이어폰 분리 확인 · 테스트 준비") { _, _ ->
                engine.chooseUsbDirect(); engine.prepareUsbTestTone()
            }.setNegativeButton("취소", null).show()
    }
    private fun showComparison(rate: Int) {
        AlertDialog.Builder(this).setTitle("${rate / 1000} kHz USB 비교 테스트")
            .setMessage("이어폰을 귀에서 빼고 USB 권한을 허용한 상태로 진행하세요.\n\n동일한 440 Hz / 24-bit stereo 테스트음을 5초 준비합니다. 첫/끝 1초는 무음입니다. 생성 음원은 -12 dBFS이며, 매 PLAY마다 하드웨어 볼륨을 -50 dB 이하로 설정·재조회하고 음소거 해제를 확인합니다. 이미 더 낮은 볼륨은 올리지 않습니다. 실제 소리 크기는 검증되지 않았습니다.\n\n준비 후 메인 화면의 PLAY를 누르세요. 자동 재생하지 않습니다. 끝나면 다른 sample rate도 준비하여 PLAY하세요. 두 결과는 이번 앱 세션의 진단 보고서에 함께 남습니다. 하드웨어 볼륨이 두 시험에서 같아야 같은 조건의 비교입니다. 실패 시 음악 전송을 차단합니다.\n\n기존 선택 파일을 테스트 WAV로 바꿉니다. 음악으로 돌아갈 때는 파일을 다시 선택하세요. 볼륨을 올리지 마세요. BIT-PERFECT NOT VERIFIED.")
            .setPositiveButton("이어폰 분리 확인 · 준비") { _, _ ->
                engine.prepareUsbComparison(rate)
                showPage(0)
            }.setNegativeButton("취소", null).show()
    }
    private fun showHardwareMusicOptions(testOnly: Boolean) {
        AlertDialog.Builder(this).setTitle(if(testOnly) "하드웨어 볼륨 설정·재조회 시험" else "USB Direct 일반 음악 — 실기기 볼륨 미검증")
            .setMessage("이어폰을 귀에서 뺀 상태로 첫 시험을 하세요. 휴대폰 볼륨은 USB 직접 재생에 적용되지 않을 수 있습니다.\n\n삼성 이어폰의 AudioControl IF0을 인계받아 FU5 master mute/current/min/max/res를 읽고, mute를 확인한 뒤 -50 dB 이하로 볼륨을 설정·재조회합니다. 원래 더 낮은 볼륨이면 올리지 않습니다. 실제 음향 감쇠량은 독립 검증되지 않았습니다.\n\n${if(testOnly) "이 버튼은 PCM을 보내지 않고 결과를 진단 파일에 남깁니다." else "현재 선택된 48/96 kHz stereo FLAC/WAV를 유지합니다. 모드 선택 후 PLAY를 눌러야 시작하며, 매번 하드웨어 볼륨 검증이 실패하면 음악 전송을 차단합니다. 소프트웨어 감쇠/리샘플링은 없습니다."}\n\n종료할 때 기존의 더 큰 하드웨어 볼륨을 자동 복원하지 않습니다. Android 음악이 너무 작거나 복구되지 않으면 재생을 멈추고 이어폰을 재연결하세요. 첫 시험에서 볼륨을 올리지 마세요. BIT-PERFECT NOT VERIFIED는 유지됩니다.")
            .setPositiveButton("이어폰 분리 확인 · 진행") { _, _ ->
                if(testOnly) engine.testHardwareVolume() else engine.chooseUsbDirect(hardwareMusic=true); render()
            }.setNegativeButton("취소", null).show()
    }
    private fun diagnosticText(s: AudioState = engine.state): String {
        val p = s.pcm
        return "BITWIRE / PUREBIT ENGINE DIAGNOSTICS v${BuildConfig.VERSION_NAME}\n${Build.MANUFACTURER} ${Build.MODEL}\nAndroid ${Build.VERSION.RELEASE}, API ${Build.VERSION.SDK_INT}\nBuild ${Build.DISPLAY}\n\n" +
            "SOURCE\nFile: ${s.filename}\nCodec: ${p?.codec ?: "UNKNOWN"}\nSample rate: ${p?.format?.rate ?: "UNKNOWN"} Hz\nBit depth: ${p?.format?.bits ?: "UNKNOWN"}\nChannels: ${p?.format?.channels ?: "UNKNOWN"}\n\n" +
            "LYRICS\nStatus: ${if(lyricSource==s.sourceUri)lyricStatus else "UNKNOWN · source changed"}\nTimed cues: ${lyricTimeline?.lines?.size?:0}\nPlain text chars: ${lyricPlain.length}\nTag read only; lyric body omitted; no audio timing correction\n\n" +
            "VISUAL LAYOUT\n${if(isAbyss)app.purebit.theme.abyss.AbyssLayout.lastReport else "Not ABYSS"}\nNot a screenshot, performance measurement or visual verification\n\n" +
            "TRANSPORT\nA–B: ${s.abRepeat}; cycles=${s.abCycles}; [A, B) source frame range\nUSB boundary may have reinitialization gap; not gapless/device sample proof\nTouch seek: keep current stream during preview; one commit on release; cancellation leaves transport unchanged\n\n" +
            "DECODER\n${p?.decoder ?: "UNKNOWN"}\nPCM: ${p?.format ?: "UNKNOWN"}\nApp playback DSP: OFF\nVisual source PCM FFT/waveform: separate read-only analysis, not DAC capture\nWaveform: RMS body with fixed display curve + faint signed peaks; audio bytes unchanged\nApp resampling: OFF\nApp software volume: OFF (unity bytes)\nSamsung/Android DSP/resampling/spatial/volume: UNKNOWN\n\n" +
            "${if (s.usbDirectMode) "USB DIRECT OUTPUT" else "ANDROID OUTPUT"}\nActive: ${s.playing}\nHardware-gated music: ${s.usbHardwareMusic}\nEngine: ${s.message}\n${s.outputDetails}\n\nHARDWARE VOLUME — LAST TEST\n${s.volumeProbe}\n\n48/96 kHz COMPARISON — THIS APP SESSION\n${s.comparisonReports}\n\n" +
            "ANDROID DEVICE CAPABILITIES\n${s.inventory}\n\nSUPPORTED MIXER ATTRIBUTES\n${s.mixers}\n\n" +
            "USB DEVICE\n${s.usb}\n\nUSB CURRENT SETTINGS — LAST PROBE\n${s.usbProbe}\n\nPATH\n${p?.codec ?: "UNKNOWN"}\n↓\nPCM ${p?.format ?: "UNKNOWN"}\n↓\n${p?.decoder ?: "UNKNOWN decoder"}\n↓\n${if (s.usbDirectMode) "Native usbfs ISO OUT (AudioTrack not used)" else "AudioTrack (Android system path)"}\n↓\n${s.routedName}\n↓\nDAC samples NOT VERIFIED\nUSB Direct: experimental Samsung-only mode\n\n" +
            "VERIFICATION\n${p?.sourceVerification ?: "SOURCE PCM NOT VERIFIED"}\nWhole PCM cache SHA-256: ${p?.sha256 ?: "none"}\n${s.bufferVerification}\nDEVICE PATH NOT VERIFIED\n\n${s.status.label} (${if (s.usbDirectMode) "app-owned USB / kernel completion scope" else "app/client scope"})\nBIT-PERFECT NOT VERIFIED\n\n" +
            "Direct/offload capability and accepted mixer policy are not proof of actual HAL/DAC bytes. No external capture evidence is available.\n"
    }
    private fun shareStartupFaults(){
        val app=applicationContext
        Thread {
            val result=runCatching {val directory=File(app.filesDir,"diagnostic-reports").apply {mkdirs()};File(directory,"PureBit-${java.util.UUID.randomUUID()}.txt").apply {writeText(AppFaults.report(app))}}
            handler.post {if(!isDestroyed&&!isFinishing&&lifecycleVisible)result.fold({shareReport(it)},{Toast.makeText(this,"실행 오류 기록 생성 실패",Toast.LENGTH_SHORT).show()})}
        }.start()
    }
    private fun captureReport(onReady: (File) -> Unit) {
        if (capturingReport) return
        val requestedPage=page
        capturingReport = true
        selfCheck.isEnabled = false
        engine.captureDiagnostics({ state, summary, logs ->
            val captured = java.time.Instant.now().toString()
            val text = "BITWIRE SELF-DIAGNOSTICS v${BuildConfig.VERSION_NAME}\nCaptured: $captured\nFile: ${state.filename}\n\n$summary\n" +
                "=== FULL DIAGNOSTICS ===\n${diagnosticText(state)}\n=== PUREBIT INTERNAL LOG ===\n$logs"
            // Save outside the audio worker; file picker/share cannot change this captured report.
            Thread {
                val result = runCatching {
                    val root = File(filesDir, "diagnostic-reports").apply { mkdirs() }
                    val report = File(root, "PureBit-${java.util.UUID.randomUUID()}.txt").apply { writeText(text+"\n"+AppFaults.report(applicationContext)) }
                    root.listFiles()?.sortedByDescending { it.lastModified() }?.drop(8)?.forEach { it.delete() }
                    report
                }
                handler.post {
                    if (isFinishing || isDestroyed) return@post
                    selfCheck.isEnabled = true
                    capturingReport = false
                    result.fold({ reportFile = it;if(lifecycleVisible&&page==requestedPage)onReady(it) }, {
                        Toast.makeText(this, "진단 저장 실패: ${it.message}", Toast.LENGTH_LONG).show()
                    })
                }
            }.start()
        }, { error -> handler.post {
            if (!isFinishing && !isDestroyed) {
                selfCheck.isEnabled = true; capturingReport = false
                Toast.makeText(this, "자체진단 실패: ${error.message}", Toast.LENGTH_LONG).show()
            }
        } })
    }
    private fun saveReport(report: File) {
        reportFile = report
        runCatching {startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE); type = "text/plain"; putExtra(Intent.EXTRA_TITLE, "BITWIRE-diagnostics.txt")
        }, 2)}.onFailure {AudioLog.error("diagnostic_save_ui",it);Toast.makeText(this,"파일 저장 화면을 열지 못했습니다",Toast.LENGTH_LONG).show()}
    }
    private fun shareReport(report: File) {
        val uri = android.net.Uri.Builder().scheme("content").authority("app.purebit.diagnostics").appendPath(report.name).build()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"; putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri("PureBit diagnostics", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching {startActivity(Intent.createChooser(intent, "진단 파일 공유"))}.onFailure {
            AudioLog.error("diagnostic_share_ui",it);Toast.makeText(this,"공유 화면을 열지 못했습니다",Toast.LENGTH_LONG).show()
        }
    }

    @Deprecated("Framework result API retained for the existing lightweight app")
    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?) {
        super.onActivityResult(requestCode,resultCode,data);if(resultCode!=RESULT_OK){if(requestCode==6)pendingLyricSource=null;return};val uri=data?.data?:return
        when(requestCode) {
            6->{val target=pendingLyricSource?:return;pendingLyricSource=null;val generation=++lyricGeneration
                lyricStore.import(target,uri){result->if(!isDestroyed)result.fold({timeline->
                    if(engine.state.sourceUri==target){lyricSource=target;lyricTimeline=timeline;lyricPlain="";lyricStatus="LRC · ${timeline.lines.size}구간";render()}
                    Toast.makeText(this,if(engine.state.sourceUri==target)"가사를 연결했습니다" else "파일을 선택할 때의 곡에 가사를 연결했습니다",Toast.LENGTH_LONG).show()
                },{error->if(generation==lyricGeneration)lyricStatus="LRC 연결 실패";Toast.makeText(this,"가사 연결 실패: ${error.message}",Toast.LENGTH_LONG).show()})}}
            4->{
                val app=applicationContext
                Thread {
                    val result=runCatching { app.contentResolver.openInputStream(uri).use { input -> ThemeStore(app).importTheme(requireNotNull(input)) } }
                    handler.post {if(!isDestroyed&&!isFinishing)result.fold({spec->showPage(5);if(lifecycleVisible)previewTheme(spec)},{error->Toast.makeText(this,"테마 가져오기 실패: ${error.message}",Toast.LENGTH_LONG).show()})}
                }.start()
            }
            1->library.addDocument(uri){r->if(!isDestroyed)r.fold({track->PlaybackQueue.select(track,library.tracks(),false);showPage(0)},{e->Toast.makeText(this,"파일 추가 실패: ${e.message}",Toast.LENGTH_LONG).show()})}
            3->{showPage(1);libraryStatus.text="폴더 읽는 중…";library.scanTree(uri){r->if(!isDestroyed){populateLibrary();r.fold({_ ->Toast.makeText(this,library.lastScanReport.summary,Toast.LENGTH_LONG).show()},{e->Toast.makeText(this,"폴더 추가 실패: ${e.message}",Toast.LENGTH_LONG).show()})}}}
            2->{val captured=reportFile;if(captured==null||!captured.isFile){Toast.makeText(this,"진단을 다시 실행하세요",Toast.LENGTH_SHORT).show();return};Thread { val r=runCatching { contentResolver.openOutputStream(uri,"wt").use { o->captured.inputStream().use { it.copyTo(requireNotNull(o)) } } };handler.post { if(!isDestroyed)Toast.makeText(this,r.fold({"진단 저장 완료"},{"저장 실패: ${it.message}"}),Toast.LENGTH_LONG).show() } }.start()}
        }
    }
    override fun onSaveInstanceState(out:Bundle) {if(recoveryRedirected){super.onSaveInstanceState(out);return}; rememberLibraryScroll();out.putString("libraryMode",libraryMode.name);out.putString("libraryFilter",libraryFilter);out.putString("playlistParentFilter",playlistParentFilter);out.putInt("libraryOffset",libraryOffset);out.putInt("libraryScrollY",libraryScrollY);out.putInt("parentLibraryOffset",parentLibraryOffset);out.putInt("parentLibraryScroll",parentLibraryScroll);out.putBoolean("favoritesOnly",favoritesOnly);out.putBoolean("recentOnly",recentOnly);out.putInt("page",page);out.putBoolean("lyricSheet",lyricOverlay!=null);out.putBoolean("manualFullscreen",fullOverlay!=null&&!fullscreenAutomatic);out.putBoolean("wasLandscape",isLandscape);out.putBoolean("landscapeDismissed",landscapeDismissed);pendingLyricSource?.let {out.putString("pendingLyricSource",it)};openedAlbum?.let {out.putString("openedAlbum",it)};reportFile?.let { out.putString("reportName",it.name) };super.onSaveInstanceState(out) }
    override fun onNewIntent(intent:Intent){
        super.onNewIntent(intent)
        if(recoveryRedirected)return
        setIntent(intent)
        if(intent.hasExtra("recovery_safe")&&intent.getBooleanExtra("recovery_safe",false)!=recoverySafe)recreate()
    }
    override fun onStart(){super.onStart();if(recoveryRedirected)return;lifecycleVisible=true;if(page==1)populateLibrary(false);updateVisualActive();handler.post(poll);revealAbyssChrome();root.post {
        if(!lifecycleVisible||isDestroyed)return@post
        if(restoreFullscreen){restoreFullscreen=false;showFullscreen()}
        if(restoreLyricSheet){restoreLyricSheet=false;showLyricSheet()}else maybeAutoLandscape();if(lyricOverlay!=null)revealLyricChrome()
    }}
    override fun onResume(){super.onResume();if(recoveryRedirected)return;engine.refreshDevices();if(startup==null)maybeRequestNotification()}
    private fun cancelScrub(){scrubToken?.let { engine.cancelScrub(it) };scrubToken=null;dragging=false}
    override fun onStop(){if(recoveryRedirected){super.onStop();return};if(page==1)rememberLibraryScroll();handler.removeCallbacks(searchLibrary);playlistDialogRequest++;playlistRefresh.cancel();libraryEpoch++;if(!startupReady)startupStableSince=0;nightTickAt=0;PlaybackQueue.checkpoint(true);handler.removeCallbacks(hideLyricChrome);revealAbyssChrome(false);cancelAbyssSeek();queueDragging=false;themePreview?.dismiss();closeAbyssAlbum();cancelScrub();lifecycleVisible=false;startup?.cancel();updateVisualActive();handler.removeCallbacks(poll);super.onStop()}
    override fun onDestroy(){if(recoveryRedirected){super.onDestroy();return};dismissPlaylistDialog();closeAbyssAlbum();playlistRefresh.cancel();handler.removeCallbacksAndMessages(null);gardenBack?.close();gardenFront?.close();lifecycleVisible=false;startup?.cancel();closeLyricSheet(false);closeFullscreen(false);seek.close();deck.close();library.close();lyricStore.close();unregisterReceiver(usbReceiver);if(Build.VERSION.SDK_INT>=33)modernBack?.let { onBackInvokedDispatcher.unregisterOnBackInvokedCallback(it) };super.onDestroy()}
    private fun registerModernBack(){if(Build.VERSION.SDK_INT<33)return;modernBack=android.window.OnBackInvokedCallback { handleBack() };onBackInvokedDispatcher.registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,modernBack!!)}
    private fun handleBack(){if(recoveryRedirected){finish();return};when {abyssAlbum!=null->abyssAlbum?.dismiss();startup!=null->startup?.cancel();lyricOverlay!=null->closeLyricSheet();fullOverlay!=null->closeFullscreen();page==1&&openedAlbum!=null->closeLibraryGroup();page>=4->showPage(3);page!=0->showPage(0);else->finish()}}
    @SuppressLint("GestureBackNavigation") // API33+ uses the registered platform callback; this is API31/32 compatibility.
    @Deprecated("API31/32 only; modern back is registered separately")
    override fun onBackPressed(){handleBack()}
    private fun dp(n:Int)=(n*resources.displayMetrics.density).toInt()
    private fun clock(ms:Long)="%d:%02d".format(ms/60000,ms/1000%60)
    private fun preciseClock(ms:Long)="${clock(ms)}.${ms/100%10}"
    private fun scrubTime(currentMs:Long)="${preciseClock(scrubPreviewMs)}로 이동 · 현재 ${clock(currentMs)}"
    private val widgets by lazy {ThemeWidgets(this,theme,::bloom)}
    private fun text(value:String,size:Int,color:Int=muted)=widgets.text(value,size,color)
    private fun row()=widgets.row()
    private fun column(padding:Int=20)=widgets.column(padding)
    private fun rounded(color:Int,radius:Int,stroke:Int=Color.TRANSPARENT)=widgets.rounded(color,radius,stroke)
    private fun themeEdge()=widgets.themeEdge()
    private fun card()=widgets.card()
    private fun themeSwitch()=widgets.themeSwitch()
    private fun motionEnabled()=!recoverySafe&&android.animation.ValueAnimator.areAnimatorsEnabled()&&getSystemService(android.view.accessibility.AccessibilityManager::class.java)?.isTouchExplorationEnabled!=true
    private fun bloom(view:View){val location=IntArray(2);val origin=IntArray(2);view.getLocationInWindow(location);root.getLocationInWindow(origin);gardenFront?.burst((location[0]-origin[0]+view.width/2).toFloat(),(location[1]-origin[1]+view.height/2).toFloat())}
    private fun button(label:String,primary:Boolean=false,action:()->Unit)=widgets.button(label,primary,action)

}
