package app.purebit.core

/** First device-specific transport requires fixed full-speed synchronous packets. */
object UsbDirectPlan {
    fun select(vendor: Int, product: Int, format: PcmFormat, report: UsbDescriptorReport): UsbPcmAlternate {
        require(vendor == 0x04e8 && product == 0xa05b) { "USB Direct prototype supports Samsung 04e8:a05b only" }
        require(format.channels == 2 && format.rate in listOf(48000,96000)) { "USB Direct prototype requires stereo 48/96 kHz; resampling disabled" }
        require(report.warnings.isEmpty()) { "Malformed/unsupported USB descriptor: ${report.warnings}" }
        val selected = report.formats.firstOrNull {
            it.protocol == 0 && it.pcm && it.channels == format.channels && it.bits == format.bits &&
                it.subslotBytes == format.bits / 8 && format.rate in it.discreteRates && it.channelConfig == 3L &&
                it.endpointAttributes?.let { a -> (a and 3) == 1 && ((a shr 2) and 3) == 3 && ((a shr 4) and 3) == 0 } == true &&
                it.interval == 1 && it.frequencyControl
        } ?: error("No exact UAC1 synchronous OUT alternate; adaptive/asynchronous/feedback/UAC2 transport not implemented")
        require(format.rate / 1000 * format.frameBytes <= requireNotNull(selected.maxPacketBytes)) { "Endpoint packet too small" }
        return selected
    }
}
