package app.purebit.ui

import app.purebit.core.PcmFormat
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/** Whole-track signed peaks and channel-independent power. Never changes playback PCM. */
data class WaveformData(val minimum: FloatArray, val maximum: FloatArray, val frames: Long,
    val rms: FloatArray, val sampleCounts: LongArray) {
    /** A display column may span unequal bucket sizes; weight by actual channel sample count. */
    fun columnRms(from: Int, to: Int): Float {
        var sum = 0.0; var count = 0L
        for (i in from until to) { sum += rms[i].toDouble() * rms[i] * sampleCounts[i]; count += sampleCounts[i] }
        return if (count > 0) sqrt(sum / count).toFloat() else 0f
    }
}

/** Fixed visual transfer only: no per-song auto gain, invented variation, or audio processing. */
object WaveformDisplay {
    fun body(rms: Float): Float = if (rms.isFinite()) .9f * rms.coerceIn(0f, 1f).pow(1.25f) else 0f
}
class WaveformAccumulator(private val format: PcmFormat, private val totalFrames: Long, buckets: Int = 768) {
    init { format.validate(); require(totalFrames > 0); require(buckets in 1..4096); require(totalFrames <= Long.MAX_VALUE / buckets) }
    private val minimum = FloatArray(buckets) { Float.POSITIVE_INFINITY }
    private val maximum = FloatArray(buckets) { Float.NEGATIVE_INFINITY }
    private val sumSquares = DoubleArray(buckets)
    private val sampleCounts = LongArray(buckets)
    private var frames = 0L
    fun append(bytes: ByteArray, length: Int) {
        require(length in 0..bytes.size && length % format.frameBytes == 0)
        val count = length / format.frameBytes
        require(count <= totalFrames - frames) { "Waveform source longer than declared" }
        for (frame in 0 until count) {
            val bucket = ((frames + frame) * minimum.size / totalFrames).toInt().coerceAtMost(minimum.lastIndex)
            for (channel in 0 until format.channels) {
                val value = SpectrumMath.sample(bytes, frame * format.frameBytes + channel * (format.bits / 8), format.bits).toFloat()
                minimum[bucket] = min(minimum[bucket], value)
                maximum[bucket] = max(maximum[bucket], value)
                sumSquares[bucket] += value.toDouble() * value
                sampleCounts[bucket]++
            }
        }
        frames += count
    }
    fun finish(): WaveformData {
        check(frames == totalFrames) { "Waveform source incomplete" }
        for(i in minimum.indices)if(!minimum[i].isFinite()){minimum[i]=0f;maximum[i]=0f}
        val rms = FloatArray(minimum.size) { i -> if (sampleCounts[i] > 0) sqrt(sumSquares[i] / sampleCounts[i]).toFloat() else 0f }
        return WaveformData(minimum, maximum, frames, rms, sampleCounts)
    }
}
