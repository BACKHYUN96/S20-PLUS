package app.purebit.core

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs

class UsbTestToneTest {
    private fun tone(rate: Int, comparison: Boolean): ByteArray {
        val f = File.createTempFile("purebit-tone", ".wav")
        return try { UsbTestTone.write(f, rate, comparison); f.readBytes() } finally { f.delete() }
    }
    private fun sample(b: ByteArray, frame: Int, channel: Int = 0): Int {
        val i = 44 + frame * 6 + channel * 3
        val value = (b[i].toInt() and 255) or ((b[i+1].toInt() and 255) shl 8) or ((b[i+2].toInt() and 255) shl 16)
        return (value shl 8) shr 8
    }
    @Test fun comparisonHasExactFormatSilenceAndMatchingWaveform() {
        val low = tone(48000, true); val high = tone(96000, true)
        for ((rate, b) in listOf(48000 to low, 96000 to high)) {
            assertEquals(44 + rate * 5 * 6, b.size)
            val h = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN)
            assertEquals(rate, h.getInt(24)); assertEquals(24, h.getShort(34).toInt())
            var peak = 0
            for (frame in 0 until rate*5) {
                val v = sample(b, frame)
                assertEquals(v, sample(b, frame, 1))
                if (frame < rate || frame >= rate*4) assertEquals(0, v)
                peak = maxOf(peak, abs(v))
            }
            assertTrue(peak in 2107000..2107122)
        }
        for (frame in 0 until 48000*5) assertEquals(sample(low, frame), sample(high, frame*2))
    }
    @Test fun legacyToneRetainsMinus60PeakGate() {
        val b = tone(48000, false)
        for (frame in 0 until 48000*5) assertTrue(abs(sample(b, frame)) <= 8388)
    }
}
