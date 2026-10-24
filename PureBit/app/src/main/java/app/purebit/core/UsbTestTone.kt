package app.purebit.core

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

object UsbTestTone {
    /** Generates test source PCM; never scales decoded music or changes system volume. */
    fun write(file: File, rate: Int = 48000, comparison: Boolean = false) {
        require(rate == 48000 || rate == 96000)
        // Comparison source is generated at -12 dBFS; playback requires the hardware gate.
        val peak = if (comparison) 2107122.0 else 8388.0
        val frames=rate*5; val dataSize=frames*6
        val header=ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray());putInt(36+dataSize);put("WAVEfmt ".toByteArray());putInt(16)
            putShort(1);putShort(2);putInt(rate);putInt(rate*6);putShort(6);putShort(24)
            put("data".toByteArray());putInt(dataSize)
        }.array()
        file.outputStream().buffered().use { out ->
            out.write(header)
            for(i in 0 until frames) {
                val value=if(i<rate || i>=rate*4) 0 else (peak*sin(2*PI*440*i/rate)).roundToInt()
                repeat(2) { out.write(value and 255);out.write((value shr 8) and 255);out.write((value shr 16) and 255) }
            }
        }
    }
}
