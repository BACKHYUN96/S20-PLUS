package app.purebit.audio

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import app.purebit.core.UsbDescriptors
import app.purebit.core.hex

/** Descriptor access never claims an audio interface or interrupts the system USB driver. */
class UsbInspector(private val context: Context) {
    private val manager = context.getSystemService(UsbManager::class.java)
    companion object { const val PERMISSION = "app.purebit.USB_PERMISSION" }
    private fun audioDevices() = manager.deviceList.values.filter { d ->
        (0 until d.interfaceCount).any { d.getInterface(it).interfaceClass == 1 }
    }.sortedBy { it.deviceName }
    fun requestPermissions() {
        audioDevices().filterNot { manager.hasPermission(it) }.forEach {
            manager.requestPermission(it, PendingIntent.getBroadcast(context, it.deviceId,
                Intent(PERMISSION).setPackage(context.packageName), PendingIntent.FLAG_IMMUTABLE))
        }
    }
    fun inspect(): String {
        val devices = audioDevices()
        if (devices.isEmpty()) return "No USB Audio Class device exposed by UsbManager\nAn analog USB-C adapter may appear only in AudioManager."
        return devices.joinToString("\n\n") { describe(it) }
    }
    private fun describe(d: UsbDevice): String {
        val title = "${d.manufacturerName ?: "Unknown manufacturer"} / ${d.productName ?: "Unknown product"}\n" +
            "VID/PID: %04x:%04x\nUSB device: %s".format(d.vendorId, d.productId, d.deviceName)
        if (!manager.hasPermission(d)) return "$title\nDescriptor permission required (USB INSPECT button)\nUSB Direct: experimental transport; not selected\nSelected USB alternate: UNKNOWN"
        return runCatching {
            val connection = manager.openDevice(d) ?: error("openDevice returned null")
            val raw = try { connection.rawDescriptors } finally { connection.close() }
            val report = UsbDescriptors.parse(raw)
            AudioLog.event("usb_descriptor", "$title raw=${raw.hex()} parsed=$report")
            "$title\n${report.versions.joinToString()}\n" +
                "Descriptor formats (not current clock):\n${report.formats.joinToString("\n").ifEmpty { "No supported Type I iso OUT alternate parsed" }}\n" +
                "${report.warnings.joinToString("\n")}\n" +
                "Descriptor-only view does not query active alternate/clock; see current-setting probe or native session below.\n" +
                "UAC1 rates are descriptor claims. UAC2 clock query: TODO.\nUSB Direct: Samsung-only experimental native transport; selected mode is shown separately"
        }.getOrElse { "$title\nDescriptor read failed: ${it.message}" }
    }
    fun readCurrentSettings(): String {
        val d = audioDevices().firstOrNull { it.vendorId == 0x04e8 && it.productId == 0xa05b }
            ?: error("Samsung 04e8:a05b not connected; current-setting query is device-specific")
        check(manager.hasPermission(d)) { "USB permission required; use USB INSPECT / PERMISSION first" }
        val c = manager.openDevice(d) ?: error("USB openDevice failed")
        return try {
            val formats = UsbDescriptors.parse(c.rawDescriptors).formats
            val b = ByteArray(3)
            val altRc = c.controlTransfer(0x81,10,0,2,b,1,150)
            val alt = if (altRc == 1) b[0].toInt() and 255 else null
            val selected = formats.firstOrNull { it.interfaceId == 2 && it.alternate == alt }
            val rateRc = c.controlTransfer(0xa2,0x81,0x0100,3,b,3,150)
            val rate = if (rateRc == 3) (b[0].toInt() and 255) or ((b[1].toInt() and 255) shl 8) or ((b[2].toInt() and 255) shl 16) else null
            val volumeRc = c.controlTransfer(0xa1,0x81,0x0200,0x0500,b,2,150)
            val volume = if (volumeRc == 2) ((b[0].toInt() and 255) or ((b[1].toInt() and 255) shl 8)).toShort().toInt()/256.0 else null
            "READ-ONLY SAMSUNG USB SETTINGS (no claim/detach/SET)\n" +
                "GET_INTERFACE IF2: rc=$altRc alternate=${alt ?: "UNKNOWN"}\n" +
                "Alternate descriptor: ${selected ?: "UNKNOWN / idle alternate"}\n" +
                "UAC1 endpoint 0x03 GET_CUR: rc=$rateRc configured frequency=${rate ?: "UNKNOWN"} Hz\n" +
                "UAC1 FU5 master volume GET_CUR: rc=$volumeRc configured dB=${volume ?: "UNKNOWN"} (no changes)\n" +
                "Read denied/unsupported/idle is UNKNOWN. USB configuration is not a PCM receipt proof.\n" + NativeUsb.probe(c.fileDescriptor)
        } finally { c.close() }
    }
}
