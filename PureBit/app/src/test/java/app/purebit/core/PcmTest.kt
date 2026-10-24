package app.purebit.core

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files

class PcmTest {
    private fun <T> temporary(block: (File) -> T): T {
        val dir = Files.createTempDirectory("purebit-test").toFile()
        try { return block(dir) } finally { dir.deleteRecursively() }
    }
    private fun wav(bits: Int, channels: Int, pcm: ByteArray, tag: Short = 1): ByteArray {
        val pad = pcm.size and 1
        return ByteBuffer.allocate(44 + pcm.size + pad).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray()); putInt(36 + pcm.size + pad); put("WAVEfmt ".toByteArray()); putInt(16)
            putShort(tag); putShort(channels.toShort()); putInt(96000); putInt(96000 * channels * bits / 8)
            putShort((channels * bits / 8).toShort()); putShort(bits.toShort()); put("data".toByteArray()); putInt(pcm.size); put(pcm)
        }.array()
    }
    @Test fun wavIntegerSamplesRemainIdenticalForAllSupportedDepths() = temporary<Unit> { dir ->
        for (bits in listOf(16,24,32)) for (channels in 1..2) {
            val bytes = ByteArray(257 * channels * bits / 8) { ((it * 67 + 131) and 255).toByte() }
            val src = File(dir, "sample.wav").apply { writeBytes(wav(bits, channels, bytes)) }
            val p = PcmDecoder.decode(src, File(dir, "out.pcm"))
            assertEquals(PcmFormat(96000,bits,channels), p.format)
            assertArrayEquals(bytes, p.file.readBytes()); assertTrue(p.sourceVerification.startsWith("SOURCE PCM VERIFIED"))
        }
    }
    @Test fun floatAndTruncatedWavAreRejected() = temporary<Unit> { dir ->
        val src = File(dir, "bad.wav"); val dst = File(dir,"pcm")
        src.writeBytes(wav(32,2,ByteArray(80),3))
        assertThrows(IllegalArgumentException::class.java) { PcmDecoder.decode(src,dst) }
        src.writeBytes(wav(24,2,ByteArray(60)).dropLast(1).toByteArray())
        assertThrows(IllegalArgumentException::class.java) { PcmDecoder.decode(src,dst) }
    }
    @Test fun misalignedAndUnsupportedWavAreRejected() = temporary<Unit> { dir ->
        val src = File(dir,"bad.wav"); val dst = File(dir,"pcm")
        src.writeBytes(wav(24,2,ByteArray(61)))
        assertThrows(IllegalArgumentException::class.java) { PcmDecoder.decode(src,dst) }
        src.writeBytes(wav(16,6,ByteArray(120)))
        assertThrows(IllegalArgumentException::class.java) { PcmDecoder.decode(src,dst) }
    }
    private fun resource(name: String) = requireNotNull(javaClass.getResourceAsStream("/fixtures/$name")).use { it.readBytes() }
    @Test fun flac16And24MatchIndependentReferencePcmAndMd5() = temporary<Unit> { dir ->
        for (bits in listOf(16,24)) {
            val src = File(dir,"test.flac").apply { writeBytes(resource("int$bits.flac")) }
            val p = PcmDecoder.decode(src,File(dir,"pcm"))
            assertEquals(PcmFormat(96000,bits,2),p.format)
            assertArrayEquals(resource("int$bits.pcm"),p.file.readBytes())
            assertTrue(p.sourceVerification.contains("MD5 matched"))
        }
    }
    @Test fun corruptedFlacMd5AndMissingAudioAreRejected() = temporary<Unit> { dir ->
        val original = resource("int24.flac"); val src = File(dir,"test.flac"); val dst = File(dir,"pcm")
        val changed = original.copyOf(); changed[26] = (changed[26].toInt() xor 1).toByte(); src.writeBytes(changed)
        assertThrows(IllegalArgumentException::class.java) { PcmDecoder.decode(src,dst) }
        src.writeBytes(original.copyOf(42))
        assertThrows(java.io.IOException::class.java) { PcmDecoder.decode(src,dst) }
    }
    @Test fun absentFlacMd5DoesNotClaimSourceVerification() = temporary<Unit> { dir ->
        val data = resource("int16.flac"); for (i in 26..41) data[i] = 0
        val p = PcmDecoder.decode(File(dir,"test.flac").apply { writeBytes(data) },File(dir,"pcm"))
        assertTrue(p.sourceVerification.startsWith("SOURCE PCM NOT VERIFIED"))
    }
    @Test fun flacCrcCorruptionWithNoMd5IsRejected() = temporary<Unit> { dir ->
        val data = resource("int24.flac"); for (i in 26..41) data[i] = 0
        data[data.lastIndex] = (data.last().toInt() xor 1).toByte()
        val src = File(dir,"test.flac").apply { writeBytes(data) }
        assertThrows(IllegalStateException::class.java) { PcmDecoder.decode(src,File(dir,"pcm")) }
    }
}
