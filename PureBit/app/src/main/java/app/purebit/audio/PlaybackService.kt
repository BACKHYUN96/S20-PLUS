package app.purebit.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.hardware.usb.UsbManager
import android.media.MediaMetadata
import android.media.AudioManager
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.Build
import app.purebit.MainActivity
import app.purebit.PlaybackQueue
import app.purebit.library.LibraryStore
import app.purebit.library.LibraryTrack
import android.graphics.Bitmap

/** Foreground ownership precedes audio focus and supports background/screen-off local playback. */
class PlaybackService : Service() {
    companion object {
        const val ACTION_TOGGLE = "app.purebit.playback.TOGGLE"
        const val ACTION_PLAY = "app.purebit.playback.PLAY"
        const val ACTION_PAUSE = "app.purebit.playback.PAUSE"
        const val ACTION_STOP = "app.purebit.playback.STOP"
        const val ACTION_NEXT = "app.purebit.playback.NEXT"
        const val ACTION_PREVIOUS = "app.purebit.playback.PREVIOUS"
        private const val CHANNEL = "bitwire-playback"
        private const val NOTIFICATION = 41
    }
    private lateinit var engine: AudioEngine
    private lateinit var library:LibraryStore
    private var artwork:Bitmap?=null
    private var artSource=""
    @Volatile private var artGeneration=0L
    @Volatile private var destroyed=false
    private var lastMetadataKey=""
    private lateinit var session: MediaSession
    private lateinit var wakeLock: PowerManager.WakeLock
    private val handler = Handler(Looper.getMainLooper())
    private var remoteVolume: android.media.VolumeProvider? = null
    private var volumeRoute = ""
    private var idleSince = 0L
    private var lastNotificationKey = ""
    private var wakeAcquired = 0L
    private val ownerToken=Any()
    private var latestStartId=0
    private val detach = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == UsbManager.ACTION_USB_DEVICE_DETACHED) engine.onUsbPhysicalDetach()
            if(intent?.action==AudioManager.ACTION_AUDIO_BECOMING_NOISY)engine.onAudioBecomingNoisy()
        }
    }
    private val poll = object : Runnable {
        override fun run() {
            if(destroyed)return
            updateState()
            PlaybackQueue.checkpoint()
            handler.postDelayed(this, 500)
        }
    }
    override fun onCreate() {
        super.onCreate()
        engine = PlaybackRuntime.engine(this)
        PlaybackQueue.bind(this)
        library=LibraryStore(this)
        session = MediaSession(this, "BITWIRE").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() { mediaCommand(ACTION_PLAY) }
                override fun onPause() { mediaCommand(ACTION_PAUSE) }
                override fun onStop() { mediaCommand(ACTION_STOP) }
                override fun onSeekTo(pos: Long) { if(PlaybackRuntime.ownsService(ownerToken))engine.seek(pos) }
                override fun onSkipToNext() { mediaCommand(ACTION_NEXT) }
                override fun onSkipToPrevious() { mediaCommand(ACTION_PREVIOUS) }
            }, handler)
            setSessionActivity(openApp())
            isActive = true
        }
        wakeLock = getSystemService(PowerManager::class.java).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BITWIRE:local-playback").apply {
            setReferenceCounted(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL, "음악 재생", NotificationManager.IMPORTANCE_LOW).apply {
                setSound(null, null); enableVibration(false); setShowBadge(false)
            }
        )
        val detachFilter = IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED).apply {addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)}
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(detach, detachFilter, RECEIVER_NOT_EXPORTED)
        else registerReceiver(detach, detachFilter)
        // Required immediately for every startForegroundService, including the initial decode.
        startForeground(NOTIFICATION, notification(engine.state), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        PlaybackRuntime.serviceStarted(ownerToken)
        AudioLog.event("playback_service", "foreground mediaPlayback active; no DSP/volume change; bounded wake lock during decode/output preparation/playback")
        handler.post(poll)
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        latestStartId=startId
        idleSince = 0
        handleAction(intent?.action)
        return START_NOT_STICKY // Process death never silently restarts at an uncertain hardware volume.
    }
    private fun mediaCommand(action:String){
        if(PlaybackRuntime.ownsService(ownerToken)){idleSince=0;handleAction(action)}
        else PlaybackRuntime.action(this,action)
    }
    private fun handleAction(action:String?){
        when (action) {
            ACTION_TOGGLE -> if(!PlaybackQueue.resumeIfPending())engine.toggle()
            ACTION_PLAY -> if(!PlaybackQueue.resumeIfPending())engine.playPlayback()
            ACTION_PAUSE -> engine.pausePlayback()
            ACTION_NEXT -> PlaybackRuntime.next()
            ACTION_PREVIOUS -> PlaybackRuntime.previous()
            ACTION_STOP -> stopPlayback()
        }
    }
    private fun updateState() {
        if(destroyed||!PlaybackRuntime.ownsService(ownerToken))return
        val state = engine.state
        val now = android.os.SystemClock.elapsedRealtime()
        if (state.playing || state.loading || state.preparing) {
            if (!wakeLock.isHeld || now - wakeAcquired >= 9 * 60 * 1000) {
                wakeLock.acquire(10 * 60 * 1000L); wakeAcquired = now
            }
        } else if (wakeLock.isHeld) wakeLock.release()
        if(state.usbDirectMode && state.usbHardwareMusic && state.hardwareVolumeRaw!=null) {
            val step=state.hardwareVolumeStep.coerceAtLeast(1)
            val count=((state.hardwareVolumeMax-state.hardwareVolumeMin)/step).coerceAtLeast(1)
            val current=((state.hardwareVolumeRaw-state.hardwareVolumeMin)/step).coerceIn(0,count)
            val key="USB:$count:$step"
            if(volumeRoute!=key) {
                remoteVolume=object:android.media.VolumeProvider(android.media.VolumeProvider.VOLUME_CONTROL_RELATIVE,count,current) {
                    override fun onAdjustVolume(direction:Int) {
                        if(destroyed||!PlaybackRuntime.ownsService(ownerToken))return
                        if(direction==android.media.AudioManager.ADJUST_RAISE)engine.adjustHardwareVolume(1) else if(direction==android.media.AudioManager.ADJUST_LOWER)engine.adjustHardwareVolume(-1) else if(direction==android.media.AudioManager.ADJUST_MUTE || direction==android.media.AudioManager.ADJUST_TOGGLE_MUTE)engine.pausePlayback()
                    }
                }
                session.setPlaybackToRemote(remoteVolume!!);volumeRoute=key
            }
            remoteVolume?.setCurrentVolume(current)
        } else if(state.usbDirectMode && volumeRoute!="USB_UNAVAILABLE") {
            // Do not retain a previous Android/remote volume route before hardware readback is valid.
            remoteVolume=object:android.media.VolumeProvider(android.media.VolumeProvider.VOLUME_CONTROL_FIXED,1,1){}
            session.setPlaybackToRemote(remoteVolume!!);volumeRoute="USB_UNAVAILABLE"
        } else if(!state.usbDirectMode && volumeRoute!="ANDROID") {
            session.setPlaybackToLocal(android.media.AudioAttributes.Builder().setUsage(android.media.AudioAttributes.USAGE_MEDIA).setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC).build())
            remoteVolume=null;volumeRoute="ANDROID"
        }
        val track=track(state)
        if(artSource!=state.sourceUri){
            artSource=state.sourceUri;artwork=null;val generation=++artGeneration
            track?.let {t->library.artwork(t,256,isCurrent={!destroyed&&generation==artGeneration}){bitmap->
                if(!destroyed&&generation==artGeneration&&engine.state.sourceUri==t.uriString){
                    artwork=bitmap;lastMetadataKey="";lastNotificationKey="";updateState()
                }else bitmap?.recycle()
            }}
        }
        val metadataKey="${state.sourceUri}|${track?.title}|${track?.artist}|${track?.album}|${state.pcm?.durationMs}|$artGeneration"
        if(metadataKey!=lastMetadataKey){
            lastMetadataKey=metadataKey
            val builder=MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_MEDIA_ID,state.sourceUri)
                .putString(MediaMetadata.METADATA_KEY_TITLE,track?.title?.ifBlank {state.filename}?:state.filename)
                .putString(MediaMetadata.METADATA_KEY_ARTIST,track?.artist?.ifBlank {"알 수 없는 아티스트"}?:"BITWIRE")
                .putString(MediaMetadata.METADATA_KEY_ALBUM,track?.album.orEmpty())
                .putString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST,track?.albumArtist.orEmpty())
                .putLong(MediaMetadata.METADATA_KEY_DURATION,state.pcm?.durationMs?:track?.durationMs?:0)
            artwork?.let {builder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART,it).putBitmap(MediaMetadata.METADATA_KEY_ART,it).putBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON,it)}
            session.setMetadata(builder.build())
        }
        val actions = PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_PLAY_PAUSE or
            PlaybackState.ACTION_STOP or PlaybackState.ACTION_SEEK_TO or PlaybackState.ACTION_SKIP_TO_NEXT or PlaybackState.ACTION_SKIP_TO_PREVIOUS
        val phase = when {
            state.loading || state.preparing -> PlaybackState.STATE_BUFFERING
            state.playing -> PlaybackState.STATE_PLAYING
            state.message.startsWith("Error:") -> PlaybackState.STATE_ERROR
            else -> PlaybackState.STATE_PAUSED
        }
        session.setPlaybackState(PlaybackState.Builder().setActions(actions)
            .setState(phase, state.positionMs, if (phase==PlaybackState.STATE_PLAYING) 1f else 0f).build())
        val key = "$metadataKey|${state.playing}|${state.loading}|${state.preparing}|${state.usbDirectMode}|${state.message.startsWith("Error:")}"
        if (key != lastNotificationKey) {
            lastNotificationKey = key
            getSystemService(NotificationManager::class.java).notify(NOTIFICATION, notification(state))
        }
        if (state.playing || state.loading || state.preparing) idleSince = 0
        else {
            if (idleSince == 0L) idleSince = now
            val pausedGrace=if(state.pcm!=null&&!state.message.startsWith("Error:")&&!state.message.startsWith("USB detached"))300_000L else 30_000L
            if (now - idleSince > pausedGrace) {
                finishService()
            }
        }
    }
    private fun track(state:AudioState):LibraryTrack? = PlaybackQueue.current?.takeIf {it.uriString==state.sourceUri}
        ?:library.tracks().firstOrNull {it.uriString==state.sourceUri}
    private fun openApp()=PendingIntent.getActivity(this,0,Intent(this,MainActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    private fun notification(state: AudioState): Notification {
        val open=openApp();val track=track(state)
        val preparing=state.loading||state.preparing
        val canPause=state.playing||preparing
        fun command(action: String, id: Int) = PendingIntent.getService(this, id,
            Intent(this, PlaybackService::class.java).setAction(action), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val text = when {
            state.loading -> "음원 준비 중"
            state.preparing -> "USB 출력 준비 중"
            state.message.startsWith("Error:") -> "재생 중단 · 앱에서 확인"
            state.playing -> if (state.usbDirectMode) "USB DIRECT" else "ANDROID OUTPUT"
            else -> "일시정지"
        }
        return Notification.Builder(this, CHANNEL)
            .setSmallIcon(app.purebit.R.drawable.ic_purebit).setContentTitle(track?.title?.ifBlank {state.filename}?:state.filename)
            .setLargeIcon(artwork).setSubText(text)
            .setContentText(track?.artist?.ifBlank {text}?:text).setContentIntent(open).setOnlyAlertOnce(true)
            .setVisibility(Notification.VISIBILITY_PUBLIC).setCategory(Notification.CATEGORY_TRANSPORT)
            .setOngoing(state.playing || state.loading || state.preparing)
            .addAction(Notification.Action.Builder(android.R.drawable.ic_media_previous, "이전", command(ACTION_PREVIOUS, 1)).build())
            .addAction(Notification.Action.Builder(if (canPause) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if(preparing)"준비 취소" else if(canPause)"일시정지" else "재생", command(if(canPause)ACTION_PAUSE else ACTION_PLAY, 2)).build())
            .addAction(Notification.Action.Builder(android.R.drawable.ic_media_next, "다음", command(ACTION_NEXT, 3)).build())
            .addAction(Notification.Action.Builder(android.R.drawable.ic_menu_close_clear_cancel, "중지", command(ACTION_STOP, 4)).build())
            .setStyle(Notification.MediaStyle().setMediaSession(session.sessionToken).setShowActionsInCompactView(0, 1, 2)).build()
    }
    private fun stopPlayback() {
        PlaybackQueue.checkpoint(true)
        engine.pausePlayback()
        finishService()
    }
    private fun finishService(){
        // A newer start may already be registered in system_server before its callback arrives here.
        if(stopSelfResult(latestStartId)){
            PlaybackRuntime.serviceStopping(ownerToken)
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
    }
    override fun onDestroy() {
        PlaybackQueue.checkpoint(true)
        destroyed=true;artGeneration++;library.close();artwork=null
        val owned=PlaybackRuntime.serviceStopped(ownerToken)
        handler.removeCallbacksAndMessages(null)
        unregisterReceiver(detach)
        session.isActive = false; session.release()
        if (wakeLock.isHeld) wakeLock.release()
        // Preserve the prepared source for the UI; no orphan playback without foreground ownership.
        if(owned)engine.pausePlayback()
        AudioLog.event("playback_service", "foreground service stopped; owned=$owned; engine/cache retained; only the current owner pauses output")
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
