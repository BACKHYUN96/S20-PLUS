package app.purebit.core

data class UsbPcmAlternate(
    val interfaceId: Int, val alternate: Int, val protocol: Int,
    val channels: Int?, val channelConfig: Long?, val subslotBytes: Int?, val bits: Int?,
    val rates: String, val pcm: Boolean, val isoOutEndpoint: Int?,
    val discreteRates: List<Int> = emptyList(), val endpointAttributes: Int? = null,
    val maxPacketBytes: Int? = null, val interval: Int? = null, val frequencyControl: Boolean = false
) {
    override fun toString() = "IF $interfaceId alt $alternate / protocol 0x${protocol.toString(16)}: " +
        "${channels ?: "?"} ch (config ${channelConfig?.let { "0x${it.toString(16)}" } ?: "unknown"}), " +
        "${bits ?: "?"}-bit in ${subslotBytes ?: "?"} bytes, $rates, PCM=$pcm, " +
        "iso OUT=${isoOutEndpoint?.let { "0x${it.toString(16)}" } ?: "none"}, attributes=$endpointAttributes, maxPacket=$maxPacketBytes, interval=$interval, freqControl=$frequencyControl"
}
data class UsbDescriptorReport(val versions: List<String>, val formats: List<UsbPcmAlternate>, val warnings: List<String>)

/** Read-only UAC1/UAC2 Type I parsing. Never treat UAC2 bit depth as a rate list. */
object UsbDescriptors {
    fun parse(raw: ByteArray): UsbDescriptorReport {
        val versions = mutableListOf<String>(); val formats = mutableListOf<UsbPcmAlternate>(); val warnings = mutableListOf<String>()
        var current: UsbPcmAlternate? = null
        var klass = -1; var subclass = -1; var protocol = -1
        val uac1ChannelConfig = mutableMapOf<Int, Long>()
        fun flush() { current?.let { if (it.alternate > 0 && it.isoOutEndpoint != null) formats += it }; current = null }
        var pos = 0
        while (pos < raw.size) {
            val len = raw[pos].toInt() and 255
            if (len < 2 || pos + len > raw.size) { warnings += "Malformed/truncated descriptor at $pos"; break }
            fun u(i: Int) = raw[pos + i].toInt() and 255
            fun le(i: Int, n: Int): Long { var v = 0L; for (j in 0 until n) v = v or (u(i+j).toLong() shl (8*j)); return v }
            val type = u(1)
            when {
                type == 2 -> { flush(); uac1ChannelConfig.clear() }
                type == 4 && len >= 9 -> {
                    flush(); klass = u(5); subclass = u(6); protocol = u(7)
                    if (klass == 1 && subclass == 2) current = UsbPcmAlternate(u(2), u(3), protocol, null, null, null, null,
                        if (protocol == 0x20) "rates UNKNOWN: UAC2 clock GET_RANGE not queried" else "rates UNKNOWN", false, null)
                }
                type == 0x24 && klass == 1 && subclass == 1 && len >= 5 && u(2) == 1 -> {
                    val version = le(3,2).toInt(); versions += "UAC bcdADC=0x${version.toString(16)} (IF protocol=0x${protocol.toString(16)})"
                }
                type == 0x24 && klass == 1 && subclass == 1 && protocol == 0 && len >= 12 && u(2) == 2 -> {
                    uac1ChannelConfig[u(3)] = le(8,2)
                }
                type == 0x24 && klass == 1 && subclass == 2 && current != null -> {
                    if (protocol == 0 && len >= 7 && u(2) == 1) current = current!!.copy(pcm = le(5,2) == 1L,
                        channelConfig = uac1ChannelConfig[u(3)])
                    if (protocol == 0x20 && len >= 16 && u(2) == 1) current = current!!.copy(
                        pcm = u(5) == 1 && le(6,4) and 1L != 0L, channels = u(10), channelConfig = le(11,4))
                    if (protocol == 0 && len >= 8 && u(2) == 2 && u(3) == 1) {
                        val count = u(7)
                        val rates = when {
                            count == 0 && len >= 14 -> "${le(8,3)}..${le(11,3)} Hz (continuous descriptor range)"
                            count > 0 && len >= 8 + count * 3 -> (0 until count).joinToString(", ") { "${le(8 + it*3,3)}" } + " Hz"
                            else -> { warnings += "Truncated UAC1 rate table"; "rates UNKNOWN (malformed)" }
                        }
                        val discrete = if (count > 0 && len >= 8 + count * 3) (0 until count).map { le(8+it*3,3).toInt() } else emptyList()
                        current = current!!.copy(channels = u(4), subslotBytes = u(5), bits = u(6), rates = rates, discreteRates = discrete)
                    }
                    if (protocol == 0x20 && len >= 6 && u(2) == 2 && u(3) == 1) current = current!!.copy(subslotBytes = u(4), bits = u(5))
                    if (protocol !in listOf(0, 0x20)) warnings += "UAC protocol 0x${protocol.toString(16)} not interpreted"
                }
                type == 5 && len >= 7 && current != null && u(2) and 0x80 == 0 && u(3) and 3 == 1 -> {
                    current = current!!.copy(isoOutEndpoint = u(2), endpointAttributes = u(3), maxPacketBytes = le(4,2).toInt(), interval = u(6))
                }
                type == 0x25 && len >= 4 && current?.isoOutEndpoint != null && protocol == 0 -> current = current!!.copy(frequencyControl = u(3) and 1 != 0)
            }
            pos += len
        }
        flush()
        return UsbDescriptorReport(versions.distinct(), formats, warnings.distinct())
    }
}
