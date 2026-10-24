package app.purebit.core

import org.jflac.FLACDecoder
import org.jflac.PCMProcessor
import org.jflac.FrameListener
import org.jflac.frame.Frame
import org.jflac.metadata.Metadata
import org.jflac.metadata.StreamInfo
import org.jflac.util.ByteData
import java.io.File
import java.io.RandomAccessFile
import java.security.MessageDigest

data class PcmFormat(val rate: Int, val bits: Int, val channels: Int) {
    val frameBytes get() = channels * (bits / 8)
    override fun toString() = "$bits-bit / ${rate / 1000.0} kHz / $channels ch"
    fun validate() {
        require(rate in 8000..384000) { "Unsupported sample rate: $rate" }
        require(bits in listOf(16, 24, 32)) { "Unsupported integer PCM depth: $bits" }
        require(channels in 1..2) { "v0.1 supports mono/stereo only; no downmix" }
    }
}

data class PreparedPcm(
    val file: File, val codec: String, val format: PcmFormat, val bytes: Long,
    val sha256: String, val sourceVerification: String, val decoder: String
) {
    val frames get() = bytes / format.frameBytes
    val durationMs get() = frames * 1000 / format.rate
}

fun ByteArray.hex() = joinToString("") { "%02x".format(it.toInt() and 255) }

/** No Android dependency: the decoder's exact bytes can be tested on the JVM. */
@Suppress("UsableSpace") // Conservative JVM-compatible check; never request reclamation of other apps' cache.
object PcmDecoder {
    const val MAX_PCM_BYTES = 2L * 1024 * 1024 * 1024
    fun decode(input: File, output: File, checkCancelled: () -> Unit = {}): PreparedPcm {
        RandomAccessFile(input, "r").use { r ->
            val magic = ByteArray(4); r.readFully(magic)
            return when (String(magic, Charsets.US_ASCII)) {
                "RIFF" -> wav(r, output, checkCancelled)
                "fLaC" -> flac(input, r, output, checkCancelled)
                else -> error("Only native FLAC and RIFF integer PCM WAV are supported")
            }
        }
    }

    private fun wav(r: RandomAccessFile, out: File, cancelled: () -> Unit): PreparedPcm {
        val riffEnd = u32(r) + 8
        require(riffEnd <= r.length() && riffEnd >= 12) { "Truncated RIFF" }
        require(ascii(r, 4) == "WAVE") { "Not WAVE" }
        var format: PcmFormat? = null
        var dataOffset = -1L; var size = 0L
        while (r.filePointer + 8 <= riffEnd) {
            val id = ascii(r, 4); val len = u32(r); val start = r.filePointer
            require(len <= riffEnd - start) { "Truncated $id chunk" }
            if (id == "fmt ") {
                require(len >= 16) { "Short fmt chunk" }
                val tag = u16(r); val channels = u16(r); val rate = u32(r).toInt()
                val byteRate = u32(r); val align = u16(r); val bits = u16(r)
                if (tag == 0xfffe) {
                    require(len >= 40 && u16(r) >= 22) { "Short extensible WAV" }
                    val validBits = u16(r); val mask = u32(r)
                    val guid = ByteArray(16); r.readFully(guid)
                    require(guid.contentEquals(byteArrayOf(1,0,0,0,0,0,16,0,-128,0,0,-86,0,56,-101,113))) {
                        "Only integer PCM WAV supported; float/compressed WAV rejected"
                    }
                    require(validBits == bits) { "Valid depth $validBits differs from container $bits; conversion disabled" }
                    require(mask == 0L || (channels == 1 && mask == 4L) || (channels == 2 && mask == 3L)) {
                        "Unsupported channel layout; remapping disabled"
                    }
                } else require(tag == 1) { "Only integer PCM WAV supported (tag=$tag)" }
                format = PcmFormat(rate, bits, channels).also { it.validate() }
                require(align == format.frameBytes && byteRate == rate.toLong() * align) { "Invalid WAV block alignment/byte rate" }
            }
            if (id == "data") {
                require(dataOffset < 0) { "Multiple data chunks unsupported" }
                dataOffset = start; size = len
            }
            r.seek(start + len + (len and 1))
        }
        val f = requireNotNull(format) { "Missing WAV fmt" }
        require(dataOffset >= 0 && size > 0 && size % f.frameBytes == 0L) { "Missing/misaligned WAV data" }
        require(size <= MAX_PCM_BYTES && size + 32L * 1024 * 1024 < out.parentFile!!.usableSpace) { "Insufficient PCM cache space" }
        r.seek(dataOffset)
        val sourceHash = MessageDigest.getInstance("SHA-256")
        out.outputStream().buffered().use { dst ->
            val buf = ByteArray(65536); var left = size
            while (left > 0) {
                cancelled(); val n = minOf(left, buf.size.toLong()).toInt()
                r.readFully(buf, 0, n); sourceHash.update(buf, 0, n); dst.write(buf, 0, n); left -= n
            }
        }
        val sha = sourceHash.digest().hex()
        require(hashFile(out, "SHA-256", cancelled) == sha) { "WAV cache differs from source data" }
        return PreparedPcm(out, "WAV", f, size, sha,
            "SOURCE PCM VERIFIED: WAV data chunk = PCM cache (SHA-256); source authenticity unknown", "PureBit RIFF PCM passthrough")
    }

    private fun flac(input: File, r: RandomAccessFile, out: File, cancelled: () -> Unit): PreparedPcm {
        val header = r.readUnsignedByte(); val len = (r.readUnsignedByte() shl 16) or (r.readUnsignedByte() shl 8) or r.readUnsignedByte()
        require(header and 127 == 0 && len == 34) { "Missing FLAC STREAMINFO" }
        val info = ByteArray(34); r.readFully(info)
        var packed = 0L
        for (i in 10..17) packed = (packed shl 8) or (info[i].toLong() and 255)
        val f = PcmFormat((packed ushr 44).toInt(), ((packed ushr 36) and 31).toInt() + 1, ((packed ushr 41) and 7).toInt() + 1)
        f.validate()
        require(f.bits in listOf(16, 24)) { "jFLAC v0.1 supports only 16/24-bit FLAC; 32-bit rejected" }
        val samples = packed and 0xfffffffffL
        require(samples > 0) { "FLAC with unknown total samples unsupported in v0.1" }
        val expectedBytes = samples * f.frameBytes
        require(expectedBytes <= MAX_PCM_BYTES && expectedBytes + 32L * 1024 * 1024 < out.parentFile!!.usableSpace) { "Insufficient PCM cache space" }
        val expectedMd5 = info.copyOfRange(18, 34)
        val md5 = MessageDigest.getInstance("MD5"); val sha = MessageDigest.getInstance("SHA-256")
        var size = 0L
        input.inputStream().buffered().use { src ->
            out.outputStream().buffered().use { dst ->
                val decoder = FLACDecoder(src)
                decoder.addFrameListener(object : FrameListener {
                    override fun processMetadata(metadata: Metadata) {}
                    override fun processFrame(frame: Frame) {
                        require(frame.header.sampleRate == f.rate && frame.header.bitsPerSample == f.bits && frame.header.channels == f.channels) {
                            "FLAC frame format differs from STREAMINFO; conversion disabled"
                        }
                    }
                    override fun processError(message: String) { error("FLAC frame error: $message; refusing synthesized silence") }
                })
                decoder.addPCMProcessor(object : PCMProcessor {
                    override fun processStreamInfo(si: StreamInfo) {
                        require(PcmFormat(si.sampleRate, si.bitsPerSample, si.channels) == f) { "FLAC metadata disagreement" }
                    }
                    override fun processPCM(pcm: ByteData) {
                        cancelled()
                        require(pcm.len > 0 && size + pcm.len <= expectedBytes) { "Invalid decoded PCM length" }
                        md5.update(pcm.data, 0, pcm.len); sha.update(pcm.data, 0, pcm.len)
                        dst.write(pcm.data, 0, pcm.len); size += pcm.len
                    }
                })
                decoder.decode()
                require(decoder.badFrames == 0) { "FLAC decode errors: ${decoder.badFrames}" }
            }
        }
        require(size == expectedBytes) { "FLAC truncated: $size / $expectedBytes bytes" }
        val actualMd5 = md5.digest()
        val hasMd5 = expectedMd5.any { it.toInt() != 0 }
        require(!hasMd5 || actualMd5.contentEquals(expectedMd5)) { "FLAC STREAMINFO MD5 mismatch; refusing playback" }
        val hash = sha.digest().hex()
        require(hashFile(out, "SHA-256", cancelled) == hash) { "Decoded PCM/cache mismatch" }
        return PreparedPcm(out, "FLAC", f, size, hash,
            if (hasMd5) "SOURCE PCM VERIFIED: FLAC STREAMINFO MD5 matched; decoded/cache SHA-256 matched"
            else "SOURCE PCM NOT VERIFIED: STREAMINFO MD5 absent; decoded/cache SHA-256 matched only",
            "jFLAC 1.5.2 integer decoder")
    }

    fun hashFile(file: File, algorithm: String, cancelled: () -> Unit = {}): String {
        val hash = MessageDigest.getInstance(algorithm)
        file.inputStream().buffered().use { src ->
            val buf = ByteArray(65536)
            while (true) { cancelled(); val n = src.read(buf); if (n < 0) break; hash.update(buf, 0, n) }
        }
        return hash.digest().hex()
    }
    private fun ascii(r: RandomAccessFile, len: Int) = ByteArray(len).also { r.readFully(it) }.toString(Charsets.US_ASCII)
    private fun u16(r: RandomAccessFile) = r.readUnsignedByte() or (r.readUnsignedByte() shl 8)
    private fun u32(r: RandomAccessFile) = u16(r).toLong() or (u16(r).toLong() shl 16)
}
