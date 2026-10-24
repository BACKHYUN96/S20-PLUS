package app.purebit.core

import org.junit.Assert.*
import org.junit.Test

class VerificationTest {
    @Test fun onlyAcceptedBytesAreComparedAndHashed() {
        val reference = byteArrayOf(1,2,3,4,5,6); val out = reference.copyOf(); out[5] = 0
        val v = BufferVerifier(); v.accept(reference,out,0,3); v.accept(reference,out,3,2)
        assertEquals(5L,v.bytes); assertTrue(v.equal); assertTrue(v.report().contains("BUFFER VERIFIED"))
        v.accept(reference,out,5,1); assertFalse(v.equal); assertTrue(v.report().contains("MISMATCH"))
        assertTrue(BufferVerifier().report().contains("NOT VERIFIED"))
    }
    @Test fun rateMatchAloneCannotPromoteStatus() {
        val source = PcmFormat(96000,24,2)
        assertEquals(PathStatus.UNKNOWN,pathStatus(source,PcmFormat(96000,16,2),true,false))
        assertEquals(PathStatus.UNKNOWN,pathStatus(source,PcmFormat(96000,24,1),true,false))
        assertEquals(PathStatus.COMPATIBLE,pathStatus(source,source,false,true))
        assertEquals(PathStatus.FORMAT_MATCH,pathStatus(source,source,true,true))
        for (routed in listOf(false,true)) for (compatible in listOf(false,true)) {
            assertNotEquals(PathStatus.VERIFIED_BIT_PERFECT,pathStatus(source,source,routed,compatible))
            assertNotEquals(PathStatus.DIRECT_OUTPUT,pathStatus(source,source,routed,compatible))
        }
    }
}
