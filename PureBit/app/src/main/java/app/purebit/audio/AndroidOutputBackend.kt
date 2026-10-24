package app.purebit.audio

import android.content.Context
import android.media.*
import android.os.Build
import app.purebit.core.PcmFormat

fun isUsb(d: AudioDeviceInfo) = d.type in listOf(AudioDeviceInfo.TYPE_USB_DEVICE, AudioDeviceInfo.TYPE_USB_HEADSET, AudioDeviceInfo.TYPE_USB_ACCESSORY)
fun encoding(bits: Int) = when (bits) {
    16 -> AudioFormat.ENCODING_PCM_16BIT
    24 -> AudioFormat.ENCODING_PCM_24BIT_PACKED
    32 -> AudioFormat.ENCODING_PCM_32BIT
    else -> error("Unsupported PCM depth")
}
fun encodingName(value: Int) = when (value) {
    AudioFormat.ENCODING_PCM_16BIT -> "PCM signed 16 LE"
    AudioFormat.ENCODING_PCM_24BIT_PACKED -> "PCM signed 24 packed LE"
    AudioFormat.ENCODING_PCM_32BIT -> "PCM signed 32 LE"
    AudioFormat.ENCODING_PCM_FLOAT -> "PCM float 32"
    else -> "encoding $value"
}
fun AudioFormat.pcm() = PcmFormat(sampleRate, when (encoding) {
    AudioFormat.ENCODING_PCM_16BIT -> 16
    AudioFormat.ENCODING_PCM_24BIT_PACKED -> 24
    AudioFormat.ENCODING_PCM_32BIT -> 32
    else -> 0
}, channelCount)
fun deviceDescription(d: AudioDeviceInfo?) = d?.let { "${it.productName} (id=${it.id}, type=${it.type}, address=${it.address})" } ?: "UNKNOWN / not routed"

class AndroidOutputBackend(context: Context) : OutputBackend {
    private val manager = context.getSystemService(AudioManager::class.java)
    override val name = "AudioTrack MODE_STREAM"
    override fun availability(format: PcmFormat) = "Exact client PCM only; hardware support must be probed at runtime"
    val attributes: AudioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
    fun devices() = manager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).toList()
    fun preferredUsb() = devices().firstOrNull { isUsb(it) }
    fun deviceInventory() = devices().joinToString("\n\n") { d ->
        deviceDescription(d) + "\nReported rates=${d.sampleRates.joinToString()} (empty may mean arbitrary/unknown)\n" +
            "Reported encodings=${d.encodings.joinToString { encodingName(it) }} / channels=${d.channelCounts.joinToString()}\n" +
            "Profiles=${d.audioProfiles.joinToString { "${encodingName(it.format)} rates=${it.sampleRates.joinToString()} masks=${it.channelMasks.joinToString()}" }}"
    }
    fun mixerInventory(): String = if (Build.VERSION.SDK_INT >= 34) devices().filter { isUsb(it) }.joinToString("\n") { d ->
        runCatching { "${d.productName}: ${manager.getSupportedMixerAttributes(d).joinToString { "${it.format.pcm()} behavior=${it.mixerBehavior}" }.ifEmpty { "none exposed by HAL" }}" }
            .getOrElse { "Mixer probe failed: ${it.message}" }
    }.ifEmpty { "No USB output" } else "Mixer attributes unavailable before API 34"
    fun compatible(f: PcmFormat): Boolean = devices().any { d ->
        d.audioProfiles.any { p -> p.format == encoding(f.bits) && f.rate in p.sampleRates &&
            p.channelMasks.any { Integer.bitCount(it) == f.channels } }
    }
    data class Session(val track: AudioTrack, val preferred: AudioDeviceInfo?, val mixerOwned: Boolean,
        val mixerPolicy: String, val directProbe: String)
    fun open(f: PcmFormat, allowBitPerfectPolicy: Boolean): Session {
        val requested = AudioFormat.Builder().setSampleRate(f.rate).setEncoding(encoding(f.bits))
            .setChannelMask(if (f.channels == 1) AudioFormat.CHANNEL_OUT_MONO else AudioFormat.CHANNEL_OUT_STEREO).build()
        val device = preferredUsb()
        var owned = false
        var policy = "No preferred mixer policy; HAL/DSP/mixing unknown"
        if (device != null && Build.VERSION.SDK_INT >= 34) {
            policy = runCatching {
                val matches = manager.getSupportedMixerAttributes(device).filter {
                    it.format.sampleRate == f.rate && it.format.encoding == requested.encoding && it.format.channelMask == requested.channelMask
                }
                val chosen = matches.firstOrNull { allowBitPerfectPolicy && it.mixerBehavior == AudioMixerAttributes.MIXER_BEHAVIOR_BIT_PERFECT }
                    ?: matches.firstOrNull { it.mixerBehavior == AudioMixerAttributes.MIXER_BEHAVIOR_DEFAULT }
                if (chosen == null) "No exact mixer attributes exposed; FALLBACK ordinary AudioTrack, PCM unchanged"
                else {
                    owned = manager.setPreferredMixerAttributes(attributes, device, chosen)
                    if (owned) "${if (chosen.mixerBehavior == 1) "BIT_PERFECT" else "DEFAULT"} preferred policy ACCEPTED; not hardware-path proof"
                    else "Mixer preference REJECTED; FALLBACK ordinary AudioTrack, PCM unchanged"
                }
            }.getOrElse { "Mixer probe/set failed: ${it.message}; FALLBACK ordinary AudioTrack, PCM unchanged" }
        }
        val probe = if (Build.VERSION.SDK_INT >= 33) runCatching {
            val flags = AudioManager.getDirectPlaybackSupport(requested, attributes)
            "getDirectPlaybackSupport flags=0x${flags.toString(16)}; capability only, not active direct output"
        }.getOrElse { "Direct support query failed: ${it.message}" } else "Direct support query needs API 33"
        AudioLog.event("requested_output", "$f API=$name preferred=${deviceDescription(device)} policy=$policy $probe")
        if (policy.contains("FALLBACK")) AudioLog.event("fallback", policy)
        try {
            val minimum = AudioTrack.getMinBufferSize(f.rate, requested.channelMask, requested.encoding)
            require(minimum > 0) { "Exact AudioTrack format rejected: $f (getMinBufferSize=$minimum); conversion disabled" }
            val bytes = maxOf(minimum, f.rate / 5 * f.frameBytes)
            val track = AudioTrack.Builder().setAudioAttributes(attributes).setAudioFormat(requested)
                .setTransferMode(AudioTrack.MODE_STREAM).setBufferSizeInBytes((bytes + f.frameBytes - 1) / f.frameBytes * f.frameBytes)
                .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_NONE).build()
            try {
                check(track.state == AudioTrack.STATE_INITIALIZED) { "AudioTrack initialization failed" }
                if (device != null) check(track.setPreferredDevice(device)) { "USB preferred device rejected; no silent speaker fallback" }
                check(track.format.pcm() == f) { "Reported AudioTrack client format differs from source; playback refused" }
                AudioLog.event("reported_output", "client=${track.format.pcm()} encoding=${encodingName(track.audioFormat)} performance=${track.performanceMode}; DAC rate UNKNOWN")
                AudioLog.event("direct_output", "Active AudioFlinger direct/offload/exclusive path UNKNOWN; offload=${track.isOffloadedPlayback}; $policy")
                return Session(track, device, owned, policy, probe)
            } catch (e: Throwable) { track.release(); throw e }
        } catch (e: Throwable) {
            if (owned && device != null && Build.VERSION.SDK_INT >= 34) manager.clearPreferredMixerAttributes(attributes, device)
            throw e
        }
    }
    fun currentPolicy(s: Session): String = if (Build.VERSION.SDK_INT >= 34 && s.preferred != null && s.mixerOwned) runCatching {
        val policy = manager.getPreferredMixerAttributes(attributes, s.preferred)
        "${s.mixerPolicy}\nCurrent preference=${policy?.let { "${it.format.pcm()} behavior=${it.mixerBehavior}" } ?: "none (changed/cleared)"}"
    }.getOrElse { "Policy read failed: ${it.message}" } else s.mixerPolicy
    fun close(s: Session) {
        runCatching { s.track.pause(); s.track.flush() }
        s.track.release()
        if (Build.VERSION.SDK_INT >= 34 && s.mixerOwned && s.preferred != null) {
            AudioLog.event("mixer_clear", runCatching { "success=${manager.clearPreferredMixerAttributes(attributes, s.preferred)}" }
                .getOrElse { "failed=${it.message}" })
        }
    }
}
