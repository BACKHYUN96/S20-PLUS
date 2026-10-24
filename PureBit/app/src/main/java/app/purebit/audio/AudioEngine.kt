package app.purebit.audio

import android.content.Context
import android.annotation.SuppressLint
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioTrack
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import app.purebit.core.*
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.AtomicLong

data class AudioState(
    val filename: String = "파일을 선택하세요", val message: String = "Ready", val pcm: PreparedPcm? = null,
    val sourceUri: String = "", val sourceGeneration: Int = 0,
    val hardwareVolumeRaw: Int? = null, val hardwareVolumeMin: Int = -15360, val hardwareVolumeMax: Int = 0, val hardwareVolumeStep: Int = 256,
    val playing: Boolean = false, val loading: Boolean = false, val positionMs: Long = 0, val preparing: Boolean = false,
    val client: PcmFormat? = null, val routedName: String = "UNKNOWN / inactive", val routed: Boolean = false,
    val outputDetails: String = "No active AudioTrack; physical DAC format UNKNOWN",
    val inventory: String = "", val mixers: String = "", val usb: String = "",
    val bufferVerification: String = "APP OUTPUT BUFFER NOT VERIFIED", val compatible: Boolean = false,
    val usbDirectMode: Boolean = false, val usbCompletedFrames: Long = 0, val usbProbe: String = "Not probed",
    val usbHardwareMusic: Boolean = false, val volumeProbe: String = "Not tested",
    val comparisonRate: Int = 0, val comparisonReports: String = "48/96 kHz comparison: Not tested",
    val abRepeat: AbRepeat = AbRepeat(), val abCycles: Long = 0
) {
    fun canSeek(currentGeneration:Int)=pcm!=null&&!loading&&sourceGeneration==currentGeneration
    val status get() = if (usbDirectMode && playing && usbCompletedFrames > 0) PathStatus.DIRECT_OUTPUT
        else if (usbDirectMode) PathStatus.UNKNOWN
        else pathStatus(pcm?.format, client, routed && playing, compatible)
}
data class SourceCompletion(val generation: Int, val playbackIntent: Int, val pcmPath: String)

/** AudioTrack and all state mutations are serialized on one worker. No main-thread audio writes. */
@SuppressLint("UsableSpace") // Refuse on low space; do not reclaim other apps' caches for a test player.
class AudioEngine(private val context: Context) : AutoCloseable {
    @Volatile var state = AudioState(); private set
    /** Called only at natural EOF, never for pause, seek, detach or failure. Runs on the audio worker. */
    @Volatile var onEnded: ((SourceCompletion) -> Unit)? = null
    @Volatile var onSourceControlChanged:(()->Unit)?=null
    private val worker = Executors.newSingleThreadScheduledExecutor()
    private val decoderWorker = Executors.newSingleThreadExecutor()
    private val generation = AtomicInteger()
    private val playbackIntent = AtomicInteger()
    private val playWanted = AtomicBoolean()
    private val nativeIntent = AtomicLong(NativeUsb.invalidatePreparation())
    private data class PendingStart(val source: Int, val intent: Int)
    private val pendingStart = AtomicReference<PendingStart?>()
    private var activeIntent = 0
    private data class SeekRequest(val positionMs: Long, val generation: Int, val intent: Int, val resume: Boolean, val pauseMessage:String?=null)
    private val pendingSeek = AtomicReference<SeekRequest?>()
    private val seekScheduled = AtomicBoolean()
    private val backend = AndroidOutputBackend(context)
    private val directBackend = UsbDirectBackend(context)
    val usbInspector = UsbInspector(context)
    private val manager = context.getSystemService(AudioManager::class.java)
    companion object {
        private val cacheLock = Any()
        private val activeCachePaths = mutableSetOf<String>()
    }
    private val cache = synchronized(cacheLock) {
        val root = File(context.cacheDir, "pcm-session").apply { mkdirs() }
        // Avoid deleting another foreground instance's buffers during Activity recreation.
        root.listFiles()?.filter { it.absolutePath !in activeCachePaths }?.forEach { it.deleteRecursively() }
        File(root, java.util.UUID.randomUUID().toString()).apply {
            check(mkdirs()) { "Cannot create private PCM cache" }
            activeCachePaths += absolutePath
        }
    }
    private var session: AndroidOutputBackend.Session? = null
    private var userHardwareVolume: Int? = null
    private var usbSession: UsbDirectBackend.Session? = null
    private var reader: RandomAccessFile? = null
    private var verifier = BufferVerifier()
    private var payload = ByteArray(0); private var reference = ByteArray(0); private var offset = 0
    private var startFrame = 0L; private var acceptedFrames = 0L; private var endFrame = 0L
    private var lastRoute: Int? = null; private var lastPublish = 0L; private var lastLog = 0L
    private val comparisonResults = linkedMapOf<Int, String>()
    private var bitPerfectPolicyArmed = false
    private var focusHeld = false
    private val focus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(backend.attributes).setWillPauseWhenDucked(true)
        .setOnAudioFocusChangeListener({ change ->
            if (change != AudioManager.AUDIOFOCUS_GAIN) {
                onSourceControlChanged?.invoke()
                pauseRequested("Audio focus lost ($change); DSP ducking disabled")
            }
        }, Handler(Looper.getMainLooper())).build()
    private val devicesCallback = object : android.media.AudioDeviceCallback() {
        override fun onAudioDevicesAdded(added: Array<out android.media.AudioDeviceInfo>) { devicesChanged() }
        override fun onAudioDevicesRemoved(removed: Array<out android.media.AudioDeviceInfo>) { devicesChanged() }
    }
    init {
        manager.registerAudioDeviceCallback(devicesCallback, Handler(Looper.getMainLooper()))
        AudioLog.event("runtime", "manufacturer=${Build.MANUFACTURER} model=${Build.MODEL} Android=${Build.VERSION.RELEASE} SDK=${Build.VERSION.SDK_INT} build=${Build.DISPLAY}")
        worker.scheduleWithFixedDelay({ guard { tick() } }, 0, 10, TimeUnit.MILLISECONDS)
        refreshDevices()
    }
    private fun command(action: () -> Unit) { if (!worker.isShutdown) runCatching { worker.execute { guard(action) } } }
    private fun guard(action: () -> Unit) {
        try { action() } catch (e: Throwable) {
            if (state.sourceGeneration == generation.get()) playWanted.set(false)
            AudioLog.error("engine_error", e); runCatching { pause("Error: ${e.message}") }
            state = state.copy(playing = false, loading = false, preparing = false, message = "Error: ${e.message}")
        }
    }
    fun load(uri: Uri, filename: String, comparisonRate: Int = 0, autoPlay: Boolean = false, resumePositionMs:Long = 0) {
        onSourceControlChanged?.invoke()
        val token = generation.incrementAndGet()
        val intent = invalidatePlaybackIntent()
        playWanted.set(autoPlay)
        pendingStart.set(if (autoPlay) PendingStart(token, intent) else null)
        pendingSeek.set(null)
        command {
            pause("Preparing PCM; no playback during verification")
            state.pcm?.let { directBackend.releaseSource(it); it.file.delete() }
            verifier = BufferVerifier()
            state = state.copy(comparisonRate = comparisonRate, filename = filename, sourceUri = uri.toString(), sourceGeneration = token,
                pcm = null, client = null, positionMs = 0, loading = true, abRepeat = AbRepeat(), abCycles = 0,
                message = "PCM 분석 및 무결성 검사 중…", bufferVerification = "APP OUTPUT BUFFER NOT VERIFIED", compatible = false)
        }
        decoderWorker.execute {
            val input = File(cache, "$token.source"); val output = File(cache, "$token.pcm")
            fun cancelled() { check(generation.get() == token && !Thread.currentThread().isInterrupted) { "Preparation cancelled" } }
            try {
                context.contentResolver.openInputStream(uri).use { src ->
                    requireNotNull(src) { "Cannot open selected file" }
                    input.outputStream().buffered().use { dst ->
                        val buf = ByteArray(65536); var copied = 0L
                        while (true) {
                            cancelled(); val n = src.read(buf); if (n < 0) break
                            copied += n
                            require(copied <= 1024L * 1024 * 1024 && cache.usableSpace > n + 32L * 1024 * 1024) { "Source cache limit (1 GiB) / storage exhausted" }
                            dst.write(buf, 0, n)
                        }
                    }
                }
                val prepared = PcmDecoder.decode(input, output, ::cancelled)
                cancelled()
                command {
                    if (generation.get() != token) { output.delete(); return@command }
                    state = state.copy(pcm = prepared, loading = false, positionMs=app.purebit.session.ResumeRules.position(resumePositionMs,prepared.durationMs), message = "Ready — 기기 볼륨을 낮춘 후 PLAY", compatible = backend.compatible(prepared.format))
                    if(resumePositionMs>0)AudioLog.event("resume_position","requested=$resumePositionMs actual=${state.positionMs}; applied after source verification, before output")
                    AudioLog.event("source_format", "file=$filename codec=${prepared.codec} ${prepared.format} bytes=${prepared.bytes}")
                    AudioLog.event("decoder_output", "${prepared.decoder}: ${prepared.format}; SHA-256=${prepared.sha256}")
                    AudioLog.event("processing", "resampler=OFF; EQ/ReplayGain/normalization/crossfade/dither/loudness/software volume/spatial effects=OFF; Android/Samsung DSP UNKNOWN")
                    AudioLog.event("verification_source", prepared.sourceVerification)
                    val request = pendingStart.get()
                    if (request?.source == token && request.intent == playbackIntent.get() && pendingStart.compareAndSet(request, null)) play()
                }
            } catch (e: Throwable) {
                output.delete()
                if (generation.get() == token) command { if (generation.get() == token) throw e }
            } finally { input.delete() }
        }
    }
    private fun invalidatePlaybackIntent(): Int {
        val intent = playbackIntent.incrementAndGet()
        pendingStart.set(null)
        pendingSeek.set(null)
        playWanted.set(false)
        val nativeToken = NativeUsb.invalidatePreparation()
        nativeIntent.updateAndGet { maxOf(it, nativeToken) }
        return intent
    }
    fun toggle() {
        // Intent changes immediately; the published playing/preparing snapshot may lag behind taps.
        val stop = playWanted.get()
        if(stop){pauseRequested("Paused");return}
        if(resumePendingSeek())return
        val intent = invalidatePlaybackIntent()
        playWanted.set(!stop)
        AudioLog.event("transport_request", "action=TOGGLE intent=$intent wanted=${!stop}")
        command {
            if (intent != playbackIntent.get()) return@command
            if (state.loading) pendingStart.set(PendingStart(generation.get(), intent))
            else play()
        }
    }
    fun playPlayback() {
        // Repeated PLAY must preserve an already-requested decode, seek or USB preparation too.
        if (playWanted.get()) { AudioLog.event("transport_request", "action=PLAY already requested; pending work retained");return }
        if(resumePendingSeek())return
        val intent = invalidatePlaybackIntent()
        playWanted.set(true)
        AudioLog.event("transport_request", "action=PLAY intent=$intent wanted=true")
        command {
            if (intent != playbackIntent.get()) return@command
            if (state.loading) pendingStart.set(PendingStart(generation.get(), intent))
            else play()
        }
    }
    fun chooseUsbDirect(hardwareMusic: Boolean = false) {
        onSourceControlChanged?.invoke()
        invalidatePlaybackIntent()
        command {
        pause("USB Direct selected; experimental test mode")
        bitPerfectPolicyArmed = false
        state = state.copy(usbDirectMode = true, usbHardwareMusic = hardwareMusic, usbCompletedFrames = 0, client = null,
            outputDetails = "USB Direct selected, NOT STARTED; no native PCM sent", message = if(hardwareMusic) "USB 음악 모드 — 48/96kHz 원본 선택 후 PLAY; 하드웨어 볼륨 검증 실패 시 차단" else "USB Direct 실험 모드 — 안전 테스트 음원 준비 후 PLAY")
    }
    }
    fun chooseAndroidOutput() {
        onSourceControlChanged?.invoke()
        invalidatePlaybackIntent()
        command {
        pause("Android output selected")
        userHardwareVolume=null
        state = state.copy(usbDirectMode = false, usbHardwareMusic = false, comparisonRate = 0, usbCompletedFrames = 0, client = null,
            outputDetails = "Android output selected, inactive", message = "Android 모드 — PLAY 전 볼륨 확인")
    }
    }
    fun prepareUsbTestTone() {
        onSourceControlChanged?.invoke()
        decoderWorker.execute {
            try {
                val tone = File(context.cacheDir, "PureBit-USB-test-minus60dB-24-48.wav")
                UsbTestTone.write(tone)
                load(Uri.fromFile(tone), tone.name)
            } catch (e: Throwable) { command { throw e } }
        }
    }
    fun prepareUsbComparison(rate: Int) {
        require(rate == 48000 || rate == 96000)
        chooseUsbDirect(hardwareMusic = true)
        decoderWorker.execute {
            try {
                val tone = File(context.cacheDir, "PureBit-comparison-24-${rate}-minus12dB.wav")
                UsbTestTone.write(tone, rate, comparison = true)
                load(Uri.fromFile(tone), tone.name, comparisonRate = rate)
            } catch (e: Throwable) { command { throw e } }
        }
    }
    fun testHardwareVolume() {
        onSourceControlChanged?.invoke()
        invalidatePlaybackIntent()
        command {
        pause("Hardware volume test: no PCM transmitted")
        val report=runCatching { directBackend.volumeTest() }.getOrElse { "Hardware volume test failed: ${it.message}; NO PCM transmitted" }
        state=state.copy(volumeProbe="Captured: ${java.time.Instant.now()}\n$report",message="볼륨 검증 완료 — 진단 결과 확인; 자동 재생 없음")
        AudioLog.event("usb_hardware_volume_test",state.volumeProbe)
    }
    }
    fun readUsbSettings() = command {
        if (usbSession != null) {
            val info = NativeUsb.details(usbSession!!.handle)
            state = state.copy(usbProbe = "APP-OWNED USB SESSION\n$info")
            AudioLog.event("usb_read_current", info)
        } else {
            decoderWorker.execute {
                val report = runCatching { usbInspector.readCurrentSettings() }.getOrElse { "USB read-only query failed: ${it.message}" }
                command {
                    state = state.copy(usbProbe = "Captured: ${java.time.Instant.now()}\n$report")
                    AudioLog.event("usb_read_current", report)
                }
            }
        }
    }
    fun onUsbPhysicalDetach() {
        onSourceControlChanged?.invoke()
        pauseRequested("USB detached; output stopped; no automatic fallback")
        command {
        userHardwareVolume=null
        // Keep the user's chosen output mode. Reconnection still needs permission and explicit PLAY.
        state = state.copy(usbCompletedFrames = 0)
        }
    }
    fun onAudioBecomingNoisy() {
        // Native USB can temporarily detach Android's ALSA route while claiming the interface.
        if(state.usbDirectMode)return
        onSourceControlChanged?.invoke()
        pauseRequested("Headphone route became noisy; stopped; explicit PLAY required")
        AudioLog.event("audio_becoming_noisy","Android stop requested; explicit PLAY required")
    }
    fun adjustHardwareVolume(direction: Int) = command {
        val active=usbSession
        if(active!=null && state.usbHardwareMusic && state.playing && (direction==1 || direction==-1)) {
            NativeUsb.volumeStep(active.handle,direction)
            AudioLog.event("user_hardware_volume_request", "direction=$direction; hardware SET/readback; no PCM scaling")
        }
    }
    fun pausePlayback() {
        pauseRequested("Paused")
    }
    fun serviceUnavailable(error:Throwable){
        invalidatePlaybackIntent()
        command {throw IllegalStateException("재생 서비스를 시작하지 못했습니다. 앱으로 돌아와 곡을 다시 선택하세요.",error)}
    }
    private fun currentPendingSeek():SeekRequest?=pendingSeek.get()?.takeIf {
        it.generation==generation.get()&&it.intent==playbackIntent.get()
    }
    private fun resumePendingSeek():Boolean {
        val request=currentPendingSeek()?:return false
        if(!state.canSeek(request.generation))return false
        AudioLog.event("seek_resume","targetMs=${request.positionMs}; committed target retained on explicit PLAY")
        requestSeek(request.positionMs,true)
        return true
    }
    private fun pauseRequested(message:String){
        // A committed seek survives a pause/interruption; only its automatic resume is revoked.
        val seek=currentPendingSeek()
        val intent=invalidatePlaybackIntent()
        if(seek!=null)pendingSeek.set(seek.copy(intent=intent,resume=false,pauseMessage=message))
        AudioLog.event("transport_request", "action=PAUSE intent=$intent wanted=false")
        command {if(intent==playbackIntent.get())pause(message)}
        if(seek!=null)scheduleSeek()
    }
    fun hasCurrentSource(uri:String,snapshot:AudioState=state):Boolean = snapshot.sourceUri==uri&&snapshot.canSeek(generation.get())
    fun beginScrub(): ScrubToken? {
        val s = state
        if (!s.canSeek(generation.get()) || s.preparing) return null
        // A preview does not own transport: no intent invalidation, native fence, pause or seek.
        val token = ScrubToken(s.sourceGeneration, playbackIntent.get(), playWanted.get())
        AudioLog.event("scrub_begin", "source=${token.generation}; resume=${token.resume}; current stream unchanged; preview target NOT submitted")
        return token
    }
    fun finishScrub(token: ScrubToken, ms: Long): Boolean {
        if (!token.isCurrent(generation.get(), playbackIntent.get())) return false
        requestSeek(ms, token.resume)
        AudioLog.event("scrub_commit", "targetMs=$ms; resume=${token.resume}; one seek on release")
        return true
    }
    fun cancelScrub(token: ScrubToken) {
        if (!token.isCurrent(generation.get(), playbackIntent.get())) return
        AudioLog.event("scrub_cancel", "preview discarded; no target committed; current playback unchanged")
    }
    fun setRepeatA(ms: Long) = changeRepeat("A") { p -> state.abRepeat.withA(ms, p.durationMs) }
    fun setRepeatB(ms: Long) = changeRepeat("B") { p -> state.abRepeat.withB(ms, p.durationMs) }
    fun clearRepeat() = changeRepeat("clear") { AbRepeat() }
    private fun changeRepeat(label: String, update: (PreparedPcm) -> AbRepeat) {
        val source = generation.get()
        command {
            val p = state.pcm ?: return@command
            if (source != generation.get() || state.sourceGeneration != source || state.loading || state.comparisonRate != 0) return@command
            val next = try { update(p) } catch (e: IllegalArgumentException) {
                state = state.copy(message = "A–B: ${e.message}"); return@command
            }
            val bounded = state.abRepeat.active || next.active
            val resume = playWanted.get()
            val intent = if (bounded) invalidatePlaybackIntent() else playbackIntent.get()
            if (bounded) pause("A–B range changed; discard queued PCM")
            if (intent != playbackIntent.get() || source != generation.get()) return@command
            state = state.copy(abRepeat = next, abCycles = 0, positionMs = if (label == "B" && next.active) next.aMs!! else next.constrain(state.positionMs, p.durationMs),
                message = if (next.active) "A–B: 구간 반복 켜짐" else if (next.aMs != null) "A–B: B 끝점을 지정하세요" else "A–B: 구간 반복 해제")
            AudioLog.event("ab_repeat", "action=$label; A=${next.aMs}; B=${next.bMs}; active=${next.active}; end exclusive; PCM/DSP unchanged")
            if (bounded && resume) { playWanted.set(true); play() }
        }
    }
    /** Latest target wins: rapid slider requests never enqueue one USB startup per movement. */
    fun seek(ms: Long) = requestSeek(ms, playWanted.get())
    private fun requestSeek(ms: Long, resume: Boolean) {
        if(!state.canSeek(generation.get())) {
            AudioLog.event("seek_ignored", "source not prepared/current; pending decode/play request retained")
            return
        }
        val intent = invalidatePlaybackIntent() // A posted EOF must not advance the queue after a new seek request.
        playWanted.set(resume)
        pendingSeek.set(SeekRequest(ms, generation.get(), intent, resume))
        scheduleSeek()
    }
    private fun scheduleSeek() {
        if (worker.isShutdown || !seekScheduled.compareAndSet(false, true)) return
        runCatching { worker.schedule({
            val request = pendingSeek.getAndSet(null)
            guard {
                if (request != null && request.generation == generation.get() && request.intent == playbackIntent.get()) {
                    val p = state.pcm
                    if (p != null) {
                        pause(request.pauseMessage?:"Seek; new verification segment")
                        verifier = BufferVerifier()
                        state = state.copy(positionMs = state.abRepeat.constrain(request.positionMs, p.durationMs),
                            bufferVerification = "APP OUTPUT BUFFER NOT VERIFIED: seek reset")
                        // Closing USB may block: controls arriving meanwhile supersede this resume.
                        if (request.resume && request.intent==playbackIntent.get() && request.generation==generation.get()) play()
                    }
                }
            }
            seekScheduled.set(false)
            if (pendingSeek.get() != null) scheduleSeek()
        }, 120, TimeUnit.MILLISECONDS) }.onFailure { seekScheduled.set(false) }
    }
    fun armBitPerfectPolicy(enabled: Boolean) {
        onSourceControlChanged?.invoke()
        invalidatePlaybackIntent()
        command {
        check(!state.usbDirectMode) { "Android mixer policies do not apply to USB Direct" }
        pause("Mixer policy changed; press PLAY")
        bitPerfectPolicyArmed = enabled
        state = state.copy(message = if (enabled) "BIT_PERFECT policy enabled for this device session; hardware volume required" else "Ordinary mixer policy enabled")
    }
    }
    fun refreshDevices() = command {
        state = state.copy(inventory = backend.deviceInventory(), mixers = backend.mixerInventory(), usb = usbInspector.inspect(),
            compatible = state.pcm?.let { backend.compatible(it.format) } ?: false)
        AudioLog.event("devices", state.inventory)
        AudioLog.event("mixer_capabilities", state.mixers)
    }
    fun captureDiagnostics(callback: (AudioState, String, String) -> Unit, onFailure: (Throwable) -> Unit) = command {
        try {
        // Refresh the live client snapshot before opening any save/share Activity.
        tick(forcePublish = true)
        val s = state
        if (s.usbDirectMode) {
            val summary = "PC 없는 USB Direct 자체진단\n" +
                "Active: ${s.playing}; 성공한 커널 ISO 완료 frame: ${s.usbCompletedFrames}\n" +
                "${s.status.label}: 앱 소유 USB 전송 범위이며 DAC 샘플 검증은 아님\n" +
                "${s.pcm?.sourceVerification ?: "SOURCE PCM NOT VERIFIED"}\n${s.bufferVerification}\n" +
                "${s.message}\nDEVICE PATH NOT VERIFIED / BIT-PERFECT NOT VERIFIED\n" +
                "USB clock/alternate 조회 성공은 디바이스 샘플 무변형의 증거가 아닙니다. 실패 시 Android fallback 없음."
            AudioLog.event("self_diagnostics", summary); callback(s, summary, AudioLog.snapshot()); return@command
        }
        val route = session?.track?.routedDevice
        val f = s.pcm?.format
        val rates = route?.sampleRates?.toList() ?: emptyList()
        val exactProfile = if (route != null && f != null) runCatching {
            route.audioProfiles.any { it.format == encoding(f.bits) && f.rate in it.sampleRates &&
                it.channelMasks.any { mask -> Integer.bitCount(mask) == f.channels } }
        }.getOrNull() else null
        val bitPerfect = if (Build.VERSION.SDK_INT >= 34 && route != null && isUsb(route) && f != null) runCatching {
            manager.getSupportedMixerAttributes(route).any {
                it.mixerBehavior == android.media.AudioMixerAttributes.MIXER_BEHAVIOR_BIT_PERFECT &&
                    it.format.pcm() == f
            }
        }.getOrNull() else null
        val evidence = DiagnosticEvidence(f, s.client, s.playing, s.playing && route != null,
            route?.let { isUsb(it) } ?: false, rates, exactProfile, bitPerfect,
            s.pcm?.sourceVerification?.startsWith("SOURCE PCM VERIFIED") == true,
            if (s.pcm != null) verifier.bytes else 0, verifier.equal, session?.track?.underrunCount)
        val summary = SelfDiagnostics.summarize(evidence)
        AudioLog.event("self_diagnostics", summary)
        callback(s, summary, AudioLog.snapshot())
        } catch (error: Throwable) {
            onFailure(error)
            throw error // Preserve the normal engine error/stop behavior.
        }
    }
    private fun devicesChanged() = command {
        // Taking the streaming interface from ALSA can remove its AudioDeviceInfo. This is
        // expected for native USB; physical detach is handled separately and by native errno.
        if (usbSession != null) { AudioLog.event("android_devices_changed", "USB native session owns IF2; no Android reroute/fallback"); return@command }
        if (state.playing || pendingStart.get() != null) {
            invalidatePlaybackIntent()
            pause("Audio devices changed; stopped to prevent unintended routing. Press PLAY after checking volume.")
        }
        bitPerfectPolicyArmed = false
        refreshDevices()
    }
    private fun play() {
        val p = state.pcm ?: return
        if (state.loading) return
        if (!playWanted.get()) return
        // A superseded TOGGLE pause may have been skipped. Never overwrite a live session/reader.
        if(session!=null||usbSession!=null)pause("Replace output for the latest playback request")
        activeIntent = playbackIntent.get()
        val nativeToken = nativeIntent.get()
        state = state.copy(positionMs = state.abRepeat.constrain(if (state.positionMs >= p.durationMs) 0 else state.positionMs, p.durationMs))
        endFrame = state.abRepeat.endFrame(p.format.rate, p.frames)
        val granted = manager.requestAudioFocus(focus) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        check(granted) { "Audio focus denied; app must be in foreground" }; focusHeld = true
        if (state.usbDirectMode) {
            verifier = BufferVerifier()
            startFrame = state.positionMs * p.format.rate / 1000
            state = state.copy(preparing = true, usbCompletedFrames = 0, message = "USB 출력 준비 중…",
                outputDetails = "USB Direct preflight in progress; kernel transfer NOT VERIFIED")
            if (state.comparisonRate != 0) {
                check(state.usbHardwareMusic && p.format.rate == state.comparisonRate && p.durationMs == 5000L) { "Comparison requires hardware-gated 5-second source" }
            }
            val direct = runCatching { directBackend.open(p, startFrame, state.usbHardwareMusic, nativeToken, if(state.comparisonRate==0)userHardwareVolume ?: Int.MIN_VALUE else Int.MIN_VALUE, endFrame) }.getOrElse {
                if (activeIntent != playbackIntent.get() || state.sourceGeneration != generation.get()) {
                    pause("USB 시작 취소 — 재생 요청이 변경됨")
                    return
                }
                if (state.comparisonRate != 0) recordComparison("FAILED before an active USB session: ${it.message}; kernel transfer NOT VERIFIED", "Error")
                throw it
            }
            if (activeIntent != playbackIntent.get() || state.sourceGeneration != generation.get()) {
                val report = directBackend.close(direct)
                state = state.copy(outputDetails = report, bufferVerification = report)
                pause("USB 시작 취소 — 재생 요청이 변경됨")
                return
            }
            usbSession = direct
            runCatching { directBackend.begin(direct) }.getOrElse {
                if (activeIntent != playbackIntent.get() || state.sourceGeneration != generation.get()) {
                    pause("USB 시작 취소 — 재생 요청이 변경됨")
                    return
                }
                throw it
            }
            if (activeIntent != playbackIntent.get() || state.sourceGeneration != generation.get()) {
                pause("USB 재생 취소 — 요청이 변경됨")
                return
            }
            state = state.copy(playing = true, preparing = false, routed = true, routedName = direct.deviceName, client = p.format,
                message = "USB Direct test running — kernel packets / device samples separate",
                outputDetails = NativeUsb.details(direct.handle), bufferVerification = "APP USB OUTPUT BUFFER NOT VERIFIED: no successful completed packets yet")
            AudioLog.event("usb_direct_open", state.outputDetails)
            return
        }
        val s = backend.open(p.format, bitPerfectPolicyArmed)
        if (activeIntent != playbackIntent.get() || state.sourceGeneration != generation.get()) {
            backend.close(s)
            pause("재생 시작 취소 — 요청이 변경됨")
            return
        }
        session = s
        reader = RandomAccessFile(p.file, "r")
        startFrame = state.positionMs * p.format.rate / 1000
        reader!!.seek(startFrame * p.format.frameBytes)
        AudioLog.event("playback_range", "frames=[$startFrame,$endFrame); A–B=${state.abRepeat.active}; no PCM conversion")
        verifier = BufferVerifier(); acceptedFrames = 0; payload = ByteArray(0); offset = 0; lastRoute = null
        s.track.play()
        state = state.copy(playing = true, message = "Playing", client = s.track.format.pcm(), routed = false,
            bufferVerification = verifier.report())
        AudioLog.event("volume", "AudioTrack unity gain (default, no software scaling); STREAM_MUSIC current=${manager.getStreamVolume(AudioManager.STREAM_MUSIC)}/${manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)}; system volume unchanged; hardware volume unknown")
    }
    private fun playedFrames() = session?.track?.playbackHeadPosition?.toLong()?.and(0xffffffffL)
        ?: usbSession?.let { NativeUsb.poll(it.handle)[1] } ?: 0
    private fun pause(message: String) {
        val p = state.pcm
        val current = if ((session != null || usbSession != null) && p != null) (startFrame + playedFrames()) * 1000 / p.format.rate else state.positionMs
        val toClose = usbSession; usbSession = null
        toClose?.let {
            val last=NativeUsb.poll(it.handle)
            if(last.size>=10 && last[9]>0 && state.comparisonRate==0)userHardwareVolume=last[5].toInt()
            val info = directBackend.close(it)
            state = state.copy(outputDetails = info, bufferVerification = info)
            if (state.comparisonRate != 0) recordComparison(info, message)
        }
        session?.let { backend.close(it) }; session = null
        reader?.close(); reader = null
        if (focusHeld) { manager.abandonAudioFocusRequest(focus); focusHeld = false }
        if (verifier.bytes > 0) AudioLog.event("verification_buffer", verifier.report())
        state = state.copy(playing = false, preparing = false, routed = false, positionMs = current.coerceAtMost(p?.durationMs ?: 0),
            bufferVerification = if (verifier.bytes > 0) verifier.report() else state.bufferVerification,
            message = message, outputDetails = state.outputDetails + if (state.playing) "\nINACTIVE: last session report" else "")
    }
    private fun recordComparison(info: String, reason: String) {
        comparisonResults[state.comparisonRate] = "Captured: ${java.time.Instant.now()}\nSource: ${state.filename}; generated 440 Hz, peak -12 dBFS; first/last 1s silence\nStop reason: $reason\n$info"
        state = state.copy(comparisonReports = listOf(48000, 96000).joinToString("\n\n") { rate ->
            "$rate Hz TEST\n${comparisonResults[rate] ?: "Not tested"}"
        })
        AudioLog.event("usb_comparison_result", state.comparisonReports)
    }
    private fun tick(forcePublish: Boolean = false) {
        if (usbSession != null) { tickUsb(forcePublish); return }
        val s = session ?: return
        val p = state.pcm ?: return
        if (!state.playing) return
        val route = s.track.routedDevice
        if (route != null) {
            if ((s.preferred != null && route.id != s.preferred.id) || (lastRoute != null && lastRoute != route.id)) {
                bitPerfectPolicyArmed = false
                invalidatePlaybackIntent()
                pause("Route changed/rejected; playback stopped. Check device and volume before PLAY.")
                AudioLog.event("route_changed", deviceDescription(route)); return
            }
            if (lastRoute != route.id) AudioLog.event("selected_audio_device", deviceDescription(route))
            lastRoute = route.id
        }
        val endByte = endFrame * p.format.frameBytes
        if (offset >= payload.size && reader!!.filePointer < endByte) {
            val n = minOf(endByte - reader!!.filePointer, (8192 * p.format.frameBytes).toLong()).toInt()
            payload = ByteArray(n); reader!!.readFully(payload); reference = payload.copyOf(); offset = 0
        }
        if (offset < payload.size) {
            val n = s.track.write(payload, offset, payload.size - offset, AudioTrack.WRITE_NON_BLOCKING)
            check(n >= 0) { "AudioTrack.write error $n; exact-format fallback unavailable" }
            if (n > 0) {
                check(n % p.format.frameBytes == 0) { "AudioTrack returned partial PCM frame" }
                verifier.accept(reference, payload, offset, n)
                check(verifier.equal) { "PCM buffer comparison failed" }
                offset += n; acceptedFrames += n / p.format.frameBytes
            }
        }
        val now = System.nanoTime()
        if (forcePublish || now - lastPublish >= 250_000_000L) {
            lastPublish = now
            val details = "API: ${backend.name}\nRequested: ${p.format}\nAudioTrack reported CLIENT: ${s.track.format.pcm()}\n" +
                "Encoding: ${encodingName(s.track.audioFormat)}\nPerformance mode: ${s.track.performanceMode} (NONE=0)\n" +
                "Actual DAC/HAL sample rate, encoding, depth: UNKNOWN\nDirect output active: UNKNOWN\nOffloaded playback: ${s.track.isOffloadedPlayback}\n" +
                "${s.directProbe}\n${backend.currentPolicy(s)}\nAudio device: ${deviceDescription(route)}\nUnderruns: ${s.track.underrunCount}"
            state = state.copy(positionMs = ((startFrame + playedFrames()) * 1000 / p.format.rate).coerceAtMost(p.durationMs),
                routed = route != null, routedName = deviceDescription(route), outputDetails = details, bufferVerification = verifier.report())
            if (now - lastLog > 5_000_000_000L) {
                lastLog = now; AudioLog.event("output_live", details)
                AudioLog.event("verification_buffer", state.bufferVerification)
                AudioLog.event("verification_path", "${state.status.label}; DEVICE PATH NOT VERIFIED; BIT-PERFECT NOT VERIFIED")
            }
        }
        if (reader!!.filePointer >= endByte && offset >= payload.size && playedFrames() >= acceptedFrames) {
            if (restartRepeat()) return
            pause("Finished"); state = state.copy(positionMs = p.durationMs)
            if (activeIntent == playbackIntent.get()) playWanted.set(false)
            onEnded?.invoke(SourceCompletion(state.sourceGeneration, activeIntent, p.file.absolutePath))
        }
    }
    private fun tickUsb(forcePublish: Boolean) {
        val s = usbSession ?: return
        val p = state.pcm ?: return
        val counters = NativeUsb.poll(s.handle)
        val info = if (forcePublish || System.nanoTime() - lastPublish >= 250_000_000L || counters[0] == 0L) NativeUsb.details(s.handle) else null
        if (info != null) {
            lastPublish = System.nanoTime()
            if(counters.size>=10 && counters[9]>0 && state.comparisonRate==0)userHardwareVolume=counters[5].toInt()
            state = state.copy(hardwareVolumeRaw=if(state.usbHardwareMusic && counters.size>=10 && counters[8]>0)counters[5].toInt() else null,
                hardwareVolumeMin=if(counters.size>=10)counters[6].toInt() else -15360, hardwareVolumeMax=if(counters.size>=10)counters[7].toInt() else 0,
                hardwareVolumeStep=if(counters.size>=10 && counters[8]>0)((256+counters[8].toInt()-1)/counters[8].toInt())*counters[8].toInt() else 256,
                usbCompletedFrames = counters[1],
                positionMs = ((startFrame + counters[1])*1000/p.format.rate).coerceAtMost(p.durationMs),
                outputDetails = info, bufferVerification = info)
            if (forcePublish || System.nanoTime() - lastLog > 1_000_000_000L) {
                lastLog = System.nanoTime(); AudioLog.event("usb_direct_live", info)
            }
        }
        if (counters[3] > 0 || counters[4] != 1L) error("USB native transfer failed; ${info ?: NativeUsb.details(s.handle)}")
        if (counters[0] == 0L) {
            val cancelled = activeIntent != playbackIntent.get() || state.sourceGeneration != generation.get()
            if (!cancelled) {
                check(counters[1] == endFrame - startFrame && counters[2] == counters[1]) { "USB stopped before the selected source range completed; no loop/queue advance" }
                if (restartRepeat()) return
            }
            pause(if (cancelled) "USB Direct stopped: playback request changed" else "USB Direct test finished; Android route restoration must be checked")
            if (!cancelled) {
                playWanted.set(false)
                state = state.copy(positionMs = p.durationMs)
                if (state.comparisonRate == 0 && state.usbHardwareMusic)
                    onEnded?.invoke(SourceCompletion(state.sourceGeneration, activeIntent, p.file.absolutePath))
            }
        }
    }
    private fun restartRepeat(): Boolean {
        val loop = state.abRepeat
        if (!loop.active || activeIntent != playbackIntent.get() || state.sourceGeneration != generation.get() || !playWanted.get()) return false
        val intent = activeIntent
        pause("A–B boundary — prepare next pass")
        if (intent != playbackIntent.get() || !playWanted.get() || state.sourceGeneration != generation.get()) return true
        state = state.copy(positionMs = loop.aMs!!, abCycles = state.abCycles + 1)
        AudioLog.event("ab_repeat_cycle", "cycle=${state.abCycles}; A=${loop.aMs}; B=${loop.bMs}; fresh buffer verification per pass; gapless NOT promised")
        play()
        return true
    }
    override fun close() {
        generation.incrementAndGet(); invalidatePlaybackIntent(); manager.unregisterAudioDeviceCallback(devicesCallback)
        decoderWorker.shutdownNow()
        command {
            pause("Closed")
            state.pcm?.let { directBackend.releaseSource(it) }
            synchronized(cacheLock) { cache.deleteRecursively(); activeCachePaths -= cache.absolutePath }
        }
        worker.shutdown()
    }
    fun isCurrentCompletion(completion: SourceCompletion): Boolean {
        val s = state
        return generation.get() == completion.generation && playbackIntent.get() == completion.playbackIntent &&
            s.sourceGeneration == completion.generation && s.pcm?.file?.absolutePath == completion.pcmPath &&
            !s.playing && !s.loading && s.positionMs >= (s.pcm?.durationMs ?: Long.MAX_VALUE)
    }
}
