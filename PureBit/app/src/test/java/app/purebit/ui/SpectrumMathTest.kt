package app.purebit.ui

import app.purebit.core.PcmFormat
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.roundToLong
import kotlin.math.sin

class SpectrumMathTest {
    @Test fun signedIntegerDepthsDecodeWithoutChangingTheirInput() {
        val cases = listOf(
            16 to byteArrayOf(0, -128, -1, 127),
            24 to byteArrayOf(0, 0, -128, -1, -1, 127),
            32 to byteArrayOf(0, 0, 0, -128, -1, -1, -1, 127)
        )
        for ((bits, bytes) in cases) {
            val saved = bytes.clone()
            assertEquals(-1.0, SpectrumMath.sample(bytes, 0, bits), 0.0)
            assertTrue(SpectrumMath.sample(bytes, bits / 8, bits) > 0.9999)
            SpectrumMath().analyze(bytes, bytes.size, PcmFormat(48000, bits, 1))
            assertArrayEquals(saved, bytes)
        }
    }

    @Test fun sineFrequencyMapsToCorrectLogarithmicBandAcrossRatesAndDepths() {
        for (rate in listOf(48000, 96000)) for (bits in listOf(16, 24, 32)) {
            val data = sine(rate, bits, channels = 2, hz = 1500.0)
            val original = data.clone()
            val result = SpectrumMath().analyze(data, data.size, PcmFormat(rate, bits, 2))
            val peakBand = result.bands.indices.maxByOrNull { result.bands[it] }!!
            val expected = (ln(1500.0 / 24) / ln(20000.0 / 24) * 64).toInt()
            assertTrue("$rate/$bits peak=$peakBand expected=$expected", abs(peakBand - expected) <= 1)
            assertTrue(result.rms in 0.34f..0.37f)
            assertTrue(result.peak in 0.49f..0.51f)
            assertArrayEquals(original, data)
        }
    }

    @Test fun silenceAndPartialWindowsNeverManufactureLevels() {
        val math = SpectrumMath()
        val data = ByteArray(128)
        val result = math.analyze(data, data.size, PcmFormat(48000, 16, 2))
        assertTrue(result.bands.all { it == 0f })
        assertEquals(0f, result.rms, 0f)
        assertEquals(0f, result.peak, 0f)
    }

    @Test fun analysisMakesStereoAverageExplicitRatherThanAnAcousticMeasurement() {
        val frames = 4096
        val data = ByteArray(frames * 4)
        for (i in 0 until frames) {
            val v = (sin(2 * PI * 1000 * i / 48000) * 16384).toInt()
            data[i * 4] = v.toByte(); data[i * 4 + 1] = (v shr 8).toByte()
            data[i * 4 + 2] = (-v).toByte(); data[i * 4 + 3] = (-v shr 8).toByte()
        }
        val result = SpectrumMath().analyze(data, data.size, PcmFormat(48000, 16, 2))
        assertTrue(result.bands.all { it == 0f })
        assertTrue(result.peak > 0.49f) // Separate channel peak still exposes non-silent source samples.
    }

    private fun sine(rate: Int, bits: Int, channels: Int, hz: Double): ByteArray {
        val bytes = ByteArray(4096 * channels * bits / 8)
        val scale = when (bits) { 16 -> 32768.0; 24 -> 8388608.0; else -> 2147483648.0 }
        for (frame in 0 until 4096) {
            val sample = (sin(2 * PI * hz * frame / rate) * scale * 0.5).roundToLong()
            for (channel in 0 until channels) for (b in 0 until bits / 8) {
                bytes[(frame * channels + channel) * (bits / 8) + b] = (sample shr (b * 8)).toByte()
            }
        }
        return bytes
    }
}
