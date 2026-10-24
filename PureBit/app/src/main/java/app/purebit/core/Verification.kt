package app.purebit.core

import java.security.MessageDigest

enum class PathStatus(val label: String) {
    UNKNOWN("UNKNOWN"), COMPATIBLE("COMPATIBLE"), FORMAT_MATCH("FORMAT MATCH ✓"),
    DIRECT_OUTPUT("DIRECT OUTPUT"), VERIFIED_BIT_PERFECT("VERIFIED BIT-PERFECT ✓")
}

/** v0.1 deliberately has no promotion to DEVICE PATH VERIFIED. */
fun pathStatus(source: PcmFormat?, client: PcmFormat?, routed: Boolean, compatible: Boolean): PathStatus = when {
    source == null -> PathStatus.UNKNOWN
    routed && client == source -> PathStatus.FORMAT_MATCH
    compatible -> PathStatus.COMPATIBLE
    else -> PathStatus.UNKNOWN
}

/** A segment starts at play/rebuild/seek; only bytes accepted by AudioTrack are included. */
class BufferVerifier {
    private val reference = MessageDigest.getInstance("SHA-256")
    private val submitted = MessageDigest.getInstance("SHA-256")
    var bytes = 0L; private set
    var equal = true; private set
    fun accept(source: ByteArray, output: ByteArray, offset: Int, count: Int) {
        require(count >= 0 && offset >= 0 && offset + count <= source.size && offset + count <= output.size)
        for (i in offset until offset + count) if (source[i] != output[i]) equal = false
        reference.update(source, offset, count); submitted.update(output, offset, count); bytes += count
    }
    fun report(): String {
        if (bytes == 0L) return "APP OUTPUT BUFFER NOT VERIFIED: no accepted PCM bytes"
        val a = (reference.clone() as MessageDigest).digest().hex()
        val b = (submitted.clone() as MessageDigest).digest().hex()
        return "APP OUTPUT BUFFER ${if (equal && a == b) "VERIFIED" else "MISMATCH"}: $bytes accepted bytes in current segment\nReference SHA-256: $a\nSubmitted SHA-256: $b\nScope ends at AudioTrack.write(); no DAC receipt proof"
    }
}
