package app.purebit.ui

import app.purebit.core.PcmFormat
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.sqrt

class WaveformTest {
    private fun encode(samples: IntArray, bits: Int): ByteArray = ByteArray(samples.size*(bits/8)).also { bytes ->
        samples.forEachIndexed { i,value -> repeat(bits/8) { b -> bytes[i*(bits/8)+b]=(value shr (b*8)).toByte() } }
    }
    @Test fun oppositePhaseStereoKeepsBothChannelPeaksWithoutChangingInput() {
        for(bits in listOf(16,24,32)) {
            val half=1 shl (bits-2)
            val bytes=encode(IntArray(12) { if(it%2==0)half else -half },bits)
            val original=bytes.copyOf()
            val acc=WaveformAccumulator(PcmFormat(96000,bits,2),6,3)
            acc.append(bytes,bytes.size);val data=acc.finish()
            assertEquals(6L,data.frames)
            data.minimum.forEach { assertEquals(-.5f,it,.00001f) }
            data.maximum.forEach { assertEquals(.5f,it,.00001f) }
            data.rms.forEach { assertEquals(.5f,it,.00001f) }
            data.sampleCounts.forEach { assertEquals(4L,it) }
            assertArrayEquals(original,bytes)
        }
    }
    @Test fun globalBucketsAndChunkedInputPreserveTransientPlacement() {
        val values=intArrayOf(0,32767,-32768,0,16000,-16000,0,0)
        val bytes=encode(values,16)
        val all=WaveformAccumulator(PcmFormat(44100,16,1),8,4).apply { append(bytes,bytes.size) }.finish()
        val parts=WaveformAccumulator(PcmFormat(44100,16,1),8,4)
        parts.append(bytes.copyOfRange(0,6),6);parts.append(bytes.copyOfRange(6,bytes.size),bytes.size-6)
        val chunked=parts.finish()
        assertArrayEquals(all.minimum,chunked.minimum,0f);assertArrayEquals(all.maximum,chunked.maximum,0f)
        assertArrayEquals(all.rms,chunked.rms,0f);assertArrayEquals(all.sampleCounts,chunked.sampleCounts)
        assertEquals(32767/32768f,all.maximum[0],.00001f)
        assertEquals(-1f,all.minimum[1],0f);assertEquals(0f,all.maximum[3],0f)
    }
    @Test fun signedDcOffsetsAreNotInventedAsZeroCrossings() {
        val positive=WaveformAccumulator(PcmFormat(48000,16,1),2,1)
        val bytes=encode(intArrayOf(16384,8192),16);positive.append(bytes,bytes.size)
        val data=positive.finish()
        assertEquals(.25f,data.minimum[0],0f);assertEquals(.5f,data.maximum[0],0f)
    }
    @Test fun silenceAndShortTracksHaveFiniteEmptyBuckets() {
        val acc=WaveformAccumulator(PcmFormat(48000,24,1),2,8)
        acc.append(ByteArray(6),6);val data=acc.finish()
        assertTrue(data.minimum.all { it==0f });assertTrue(data.maximum.all { it==0f })
        assertTrue(data.rms.all { it==0f });assertEquals(2L,data.sampleCounts.sum())
    }
    @Test fun equallyHighPeaksNoLongerMakeDifferentEnergySectionsLookFull() {
        val values=IntArray(400) { i ->
            if (i%100==0) 32767 else when(i/100) { 0->1000;1->7000;2->16000;else->28000 }
        }
        val bytes=encode(values,16);val original=bytes.copyOf()
        val data=WaveformAccumulator(PcmFormat(48000,16,1),400,4).apply { append(bytes,bytes.size) }.finish()
        assertTrue(data.maximum.all { it>.999f }) // The previous peak-only body would look flat/full.
        val body=data.rms.map { WaveformDisplay.body(it) }
        assertTrue(body.zipWithNext().all { (quiet,loud)->loud>quiet+.06f })
        assertTrue(body.all { it<.9f });assertArrayEquals(original,bytes)
    }
    @Test fun mergedColumnsUseActualSampleCountsAndEmptyBucketsDoNotDiluteEnergy() {
        val bytes=encode(intArrayOf(0,0,0,16384,16384),16)
        val data=WaveformAccumulator(PcmFormat(44100,16,1),5,2).apply { append(bytes,bytes.size) }.finish()
        assertArrayEquals(longArrayOf(3,2),data.sampleCounts)
        assertEquals(sqrt(.25*2/5).toFloat(),data.columnRms(0,2),.00001f)
        val short=encode(intArrayOf(16384,16384),16)
        val sparse=WaveformAccumulator(PcmFormat(44100,16,1),2,8).apply { append(short,short.size) }.finish()
        assertEquals(.5f,sparse.columnRms(0,8),0f)
        assertEquals(0f,sparse.columnRms(1,4),0f)
    }
    @Test fun displayCurveIsFixedMonotonicAndLeavesHeadroomAtFullScale() {
        val levels=listOf(0f,.01f,.05f,.2f,.5f,1f).map { WaveformDisplay.body(it) }
        assertEquals(0f,levels.first(),0f);assertEquals(.9f,levels.last(),0f)
        assertTrue(levels.zipWithNext().all { (a,b)->b>a })
        assertEquals(levels.last(),WaveformDisplay.body(100f),0f)
        assertEquals(0f,WaveformDisplay.body(-1f),0f)
        assertEquals(0f,WaveformDisplay.body(Float.NaN),0f)
        assertEquals(0f,WaveformDisplay.body(Float.POSITIVE_INFINITY),0f)
    }
    @Test fun rejectsPartialFramesOverflowAndIncompleteAnalysis() {
        val acc=WaveformAccumulator(PcmFormat(48000,24,2),2,2)
        assertThrows(IllegalArgumentException::class.java){acc.append(ByteArray(7),7)}
        acc.append(ByteArray(6),6)
        assertThrows(IllegalStateException::class.java){acc.finish()}
        assertThrows(IllegalArgumentException::class.java){acc.append(ByteArray(12),12)}
    }
}
