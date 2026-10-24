package app.purebit.core

import org.junit.Assert.*
import org.junit.Test

class UsbDescriptorsTest {
    private fun bytes(vararg n: Int) = n.map { it.toByte() }.toByteArray()
    @Test fun uac1DiscreteRatesAndIsoOutputAreParsed() {
        val raw = bytes(9,4,0,0,0,1,1,0,0) + bytes(12,0x24,2,1,1,1,0,2,3,0,0,0) +
            bytes(9,4,1,1,1,1,2,0,0) + bytes(7,0x24,1,1,0,1,0) +
            bytes(14,0x24,2,1,2,3,24,2,0x44,0xac,0,0,0x77,1) + bytes(7,5,1,1,0,2,1)
        val f = UsbDescriptors.parse(raw).formats.single()
        assertEquals(24,f.bits); assertEquals(2,f.channels); assertTrue(f.pcm)
        assertEquals("44100, 96000 Hz",f.rates); assertEquals(1,f.isoOutEndpoint)
        assertEquals(3L,f.channelConfig)
    }
    @Test fun uac2RatesAreUnknownUntilClockRangeIsQueried() {
        val raw = bytes(9,4,1,1,1,1,2,0x20,0) + bytes(16,0x24,1,1,0,1,1,0,0,0,2,3,0,0,0,0) +
            bytes(6,0x24,2,1,4,24) + bytes(7,5,2,1,0,2,1)
        val f = UsbDescriptors.parse(raw).formats.single()
        assertEquals(24,f.bits); assertEquals(4,f.subslotBytes); assertEquals(3L,f.channelConfig)
        assertTrue(f.rates.contains("UNKNOWN")); assertTrue(f.pcm)
    }
    @Test fun malformedDescriptorsAreBoundedAndInputEndpointsIgnored() {
        assertFalse(UsbDescriptors.parse(bytes(0,0)).warnings.isEmpty())
        assertFalse(UsbDescriptors.parse(bytes(10,4,1)).warnings.isEmpty())
        val raw = bytes(9,4,1,1,1,1,2,0,0) + bytes(7,5,0x81,1,0,2,1)
        assertTrue(UsbDescriptors.parse(raw).formats.isEmpty())
    }
}
