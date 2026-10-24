package app.purebit.audio

import android.content.Context
import android.hardware.usb.UsbManager
import app.purebit.core.*

/** Permission supplies the fd; native owns a duplicate and never opens USB /dev nodes itself. */
class UsbDirectBackend(context: Context) : OutputBackend {
    private val manager = context.getSystemService(UsbManager::class.java)
    override val name = "USB Direct / native usbfs"
    override fun availability(format: PcmFormat) = "Samsung UAC1 FULL-speed synchronous stereo 48/96 kHz; hardware-gated music or <=30s/-60dBFS test mode"
    data class Session(val handle: Long, val deviceId: Int, val deviceName: String, val format: PcmFormat)
    private fun device() = manager.deviceList.values.firstOrNull { it.vendorId == 0x04e8 && it.productId == 0xa05b }
        ?: error("Samsung USB-C Earphones 04e8:a05b not connected")
    fun probe(): String {
        val d = device()
        check(manager.hasPermission(d)) { "USB INSPECT / PERMISSION을 먼저 허용하세요. 허용 후 다시 눌러주세요." }
        val connection = manager.openDevice(d) ?: error("USB openDevice failed")
        return try { NativeUsb.probe(connection.fileDescriptor) } finally { connection.close() }
    }
    fun volumeTest(): String {
        val d=device();check(manager.hasPermission(d)) { "USB permission required" }
        val connection=manager.openDevice(d) ?: error("USB openDevice failed")
        return try { NativeUsb.volumeTest(connection.fileDescriptor) } finally { connection.close() }
    }
    fun open(pcm: PreparedPcm, startFrame: Long, hardwareMusic: Boolean, intentToken: Long, requestedVolume: Int = Int.MIN_VALUE, endFrameExclusive: Long = pcm.frames): Session {
        require(hardwareMusic || pcm.durationMs <= 30000) { "Low-level test mode accepts <=30 s only; music requires hardware volume handshake" }
        val d = device()
        check(manager.hasPermission(d)) { "USB permission missing; no automatic fallback" }
        val connection = manager.openDevice(d) ?: error("USB openDevice failed")
        return try {
            val selected = UsbDirectPlan.select(d.vendorId, d.productId, pcm.format, UsbDescriptors.parse(connection.rawDescriptors))
            AudioLog.event("usb_direct_plan", "${pcm.format}; $selected; hardwareMusic=$hardwareMusic; ${if(hardwareMusic) "hardware mute/volume handshake required" else "-60dBFS peak gate; no volume SET"}")
            val handle = NativeUsb.start(connection.fileDescriptor, pcm.file.absolutePath, pcm.sha256, pcm.format.rate, pcm.format.bits,
                pcm.format.channels, selected.interfaceId, selected.alternate, requireNotNull(selected.isoOutEndpoint),
                requireNotNull(selected.maxPacketBytes), startFrame, hardwareMusic, intentToken, requestedVolume, endFrameExclusive)
            check(handle != 0L) { "Native session returned no handle" }
            Session(handle, d.deviceId, "${d.productName} (${d.vendorId.toString(16)}:${d.productId.toString(16)})", pcm.format)
        } finally { connection.close() }
    }
    fun begin(session: Session) { NativeUsb.begin(session.handle) }
    fun close(session: Session): String {
        val report = NativeUsb.close(session.handle)
        AudioLog.event("usb_direct_close", report)
        return report
    }
    fun releaseSource(pcm: PreparedPcm) { NativeUsb.releaseSource(pcm.file.absolutePath) }
}
