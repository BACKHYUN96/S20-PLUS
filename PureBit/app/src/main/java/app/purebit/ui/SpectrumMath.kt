package app.purebit.ui

import app.purebit.core.PcmFormat
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/** Display-only analysis. Input bytes are read, never changed or fed back into playback. */
class SpectrumMath(val size: Int = 4096, val bandCount: Int = 64) {
    init {
        require(size >= 64 && size and (size - 1) == 0) { "FFT size must be a power of two" }
        require(bandCount > 0)
    }

    private val real = DoubleArray(size)
    private val imaginary = DoubleArray(size)
    private val window = DoubleArray(size) { 0.5 - 0.5 * cos(2 * PI * it / (size - 1)) }
    private val magnitude = DoubleArray(size / 2 + 1)

    data class Result(val bands: FloatArray, val rms: Float, val peak: Float)

    /** Signed little-endian 16/24/32-bit PCM; stereo is averaged only for this display. */
    fun analyze(pcm: ByteArray, bytes: Int, format: PcmFormat): Result {
        require(format.bits == 16 || format.bits == 24 || format.bits == 32)
        require(format.channels in 1..2 && format.rate > 0)
        require(bytes in 0..pcm.size && bytes % format.frameBytes == 0)
        val frames = min(size, bytes / format.frameBytes)
        var sum = 0.0
        var peak = 0.0
        for (frame in 0 until size) {
            var mono = 0.0
            if (frame < frames) {
                for (channel in 0 until format.channels) {
                    val sample = sample(pcm, frame * format.frameBytes + channel * (format.bits / 8), format.bits)
                    mono += sample
                    peak = max(peak, kotlin.math.abs(sample))
                }
                mono /= format.channels
            }
            sum += mono * mono
            real[frame] = mono * window[frame]
            imaginary[frame] = 0.0
        }
        fft()
        // Hann coherent gain ~0.5. Magnitude is relative to the source PCM, not DAC output.
        for (bin in magnitude.indices) magnitude[bin] = sqrt(real[bin] * real[bin] + imaginary[bin] * imaginary[bin]) * 4 / size
        val low = 24.0
        val high = min(20000.0, format.rate / 2.0)
        val span = ln(high / low)
        val bands = FloatArray(bandCount)
        for (band in bands.indices) {
            val start = low * exp(span * band / bandCount)
            val end = low * exp(span * (band + 1) / bandCount)
            val first = max(1, kotlin.math.ceil(start * size / format.rate).toInt())
            val last = min(size / 2, floor(end * size / format.rate).toInt())
            var level = 0.0
            if (first <= last) {
                for (bin in first..last) level = max(level, magnitude[bin])
            } else {
                val exact = sqrt(start * end) * size / format.rate
                val left = floor(exact).toInt().coerceIn(0, size / 2)
                val right = (left + 1).coerceAtMost(size / 2)
                level = magnitude[left] * (1 - (exact - left)) + magnitude[right] * (exact - left)
            }
            // Display floor -84 dBFS; never changes audio gain or file data.
            val db = 20 * log10(max(1e-10, level))
            bands[band] = ((db + 84) / 84).toFloat().coerceIn(0f, 1f)
        }
        return Result(bands, sqrt(sum / size).toFloat(), peak.toFloat())
    }

    private fun fft() {
        var j = 0
        for (i in 1 until size) {
            var bit = size shr 1
            while (j and bit != 0) { j = j xor bit; bit = bit shr 1 }
            j = j xor bit
            if (i < j) {
                val r = real[i]; real[i] = real[j]; real[j] = r
                val im = imaginary[i]; imaginary[i] = imaginary[j]; imaginary[j] = im
            }
        }
        var len = 2
        while (len <= size) {
            val angle = -2 * PI / len
            val cosAngle = cos(angle)
            val sinAngle = sin(angle)
            var start = 0
            while (start < size) {
                var wr = 1.0; var wi = 0.0
                for (offset in 0 until len / 2) {
                    val a = start + offset; val b = a + len / 2
                    val tr = real[b] * wr - imaginary[b] * wi
                    val ti = real[b] * wi + imaginary[b] * wr
                    real[b] = real[a] - tr; imaginary[b] = imaginary[a] - ti
                    real[a] += tr; imaginary[a] += ti
                    val next = wr * cosAngle - wi * sinAngle
                    wi = wr * sinAngle + wi * cosAngle; wr = next
                }
                start += len
            }
            len = len shl 1
        }
    }

    companion object {
        fun sample(bytes: ByteArray, offset: Int, bits: Int): Double = when (bits) {
            16 -> ((bytes[offset].toInt() and 255) or (bytes[offset + 1].toInt() shl 8)).toShort().toDouble() / 32768.0
            24 -> ((bytes[offset].toInt() and 255) or ((bytes[offset + 1].toInt() and 255) shl 8) or
                (bytes[offset + 2].toInt() shl 16)).toDouble() / 8388608.0
            32 -> ((bytes[offset].toInt() and 255) or ((bytes[offset + 1].toInt() and 255) shl 8) or
                ((bytes[offset + 2].toInt() and 255) shl 16) or (bytes[offset + 3].toInt() shl 24)).toDouble() / 2147483648.0
            else -> error("Unsupported PCM depth")
        }
    }
}
