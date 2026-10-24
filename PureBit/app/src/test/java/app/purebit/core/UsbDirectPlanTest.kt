package app.purebit.core

import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Files

class UsbDirectPlanTest {
    private val alt = UsbPcmAlternate(2,2,0,2,3L,3,24,"48000, 96000 Hz",true,3,
        listOf(48000,96000),0x0d,576,1,true)
    private fun report(a: UsbPcmAlternate = alt) = UsbDescriptorReport(listOf("UAC1"),listOf(a),emptyList())
    @Test fun exactSamsungSynchronousFormatIsSelected() {
        assertEquals(alt,UsbDirectPlan.select(0x04e8,0xa05b,PcmFormat(48000,24,2),report()))
        assertEquals(alt,UsbDirectPlan.select(0x04e8,0xa05b,PcmFormat(96000,24,2),report()))
    }
    @Test fun unsupportedRateDepthDeviceAndTransportAreRejected() {
        assertThrows(IllegalArgumentException::class.java) { UsbDirectPlan.select(0x04e8,0xa05b,PcmFormat(44100,24,2),report()) }
        assertThrows(IllegalArgumentException::class.java) { UsbDirectPlan.select(1,2,PcmFormat(48000,24,2),report()) }
        for(a in listOf(alt.copy(endpointAttributes=5),alt.copy(endpointAttributes=9),alt.copy(protocol=0x20),
            alt.copy(frequencyControl=false),alt.copy(interval=2),alt.copy(subslotBytes=4),alt.copy(channelConfig=0L))) {
            assertThrows(IllegalStateException::class.java) { UsbDirectPlan.select(0x04e8,0xa05b,PcmFormat(48000,24,2),report(a)) }
        }
        assertThrows(IllegalArgumentException::class.java) { UsbDirectPlan.select(0x04e8,0xa05b,PcmFormat(96000,24,2),report(alt.copy(maxPacketBytes=288))) }
    }
    @Test fun actualSamsungDescriptorFieldsAreRetained() {
        fun bytes(vararg n: Int)=n.map { it.toByte() }.toByteArray()
        val raw=bytes(9,4,0,0,0,1,1,0,0)+bytes(12,0x24,2,4,1,1,0,2,3,0,0,0)+
            bytes(9,4,2,2,1,1,2,0,0)+bytes(7,0x24,1,4,0,1,0)+
            bytes(14,0x24,2,1,2,3,24,2,0x80,0xbb,0,0,0x77,1)+bytes(9,5,3,0x0d,0x40,2,1,0,0)+bytes(7,0x25,1,1,0,0,0)
        val parsed=UsbDescriptors.parse(raw)
        val selected=UsbDirectPlan.select(0x04e8,0xa05b,PcmFormat(48000,24,2),parsed)
        assertEquals(576,selected.maxPacketBytes);assertEquals(listOf(48000,96000),selected.discreteRates)
        assertTrue(selected.frequencyControl)
    }
    @Test fun generatedToneHasExactFormatDurationAndLowPeakWithoutPlaybackScaling() {
        val dir=Files.createTempDirectory("usb-tone").toFile()
        try {
            val wav=java.io.File(dir,"tone.wav");UsbTestTone.write(wav)
            val p=PcmDecoder.decode(wav,java.io.File(dir,"tone.pcm"))
            assertEquals(PcmFormat(48000,24,2),p.format);assertEquals(5000L,p.durationMs)
            val data=p.file.readBytes();var peak=0
            for(i in data.indices step 3) {
                var sample=(data[i].toInt() and 255) or ((data[i+1].toInt() and 255) shl 8) or ((data[i+2].toInt() and 255) shl 16)
                if(sample and 0x800000 != 0)sample=sample or -0x1000000
                peak=maxOf(peak,kotlin.math.abs(sample))
            }
            assertTrue(peak in 8300..8388)
            assertTrue(data.take(48000*6).all { it.toInt()==0 });assertTrue(data.takeLast(48000*6).all { it.toInt()==0 })
        } finally {dir.deleteRecursively()}
    }
}
