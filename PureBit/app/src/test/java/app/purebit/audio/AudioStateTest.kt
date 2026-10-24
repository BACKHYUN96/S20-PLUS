package app.purebit.audio

import app.purebit.core.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class AudioStateTest {
    @Test fun seekRejectsOutgoingPcmBeforeQueuedSourceLoadIsPublished() {
        val p=PreparedPcm(File("not-opened"),"WAV",PcmFormat(48000,24,2),288000,"test","test","test")
        val outgoing=AudioState(pcm=p,sourceGeneration=7,positionMs=90_000,playing=true)
        assertTrue(outgoing.canSeek(7))
        assertFalse(outgoing.canSeek(8)) // New load already owns generation 8; state still belongs to 7.
        assertFalse(outgoing.copy(loading=true).canSeek(7))
    }
    @Test fun seekNeedsDecodedPcmAndAcceptsAPausedCurrentSource() {
        assertFalse(AudioState(sourceGeneration=8,loading=true).canSeek(8))
        assertFalse(AudioState(sourceGeneration=8).canSeek(8))
        val p=PreparedPcm(File("not-opened"),"WAV",PcmFormat(48000,24,2),288000,"test","test","test")
        assertTrue(AudioState(sourceGeneration=8,pcm=p,playing=false).canSeek(8))
    }
    @Test fun directOutputRequiresActiveNativeSessionAndCompletedFrames() {
        val before = AudioState(usbDirectMode=true,playing=true,usbCompletedFrames=0)
        assertEquals(PathStatus.UNKNOWN,before.status)
        val completed=before.copy(usbCompletedFrames=48)
        assertEquals(PathStatus.DIRECT_OUTPUT,completed.status)
        assertEquals(PathStatus.UNKNOWN,completed.copy(playing=false).status)
        assertNotEquals(PathStatus.VERIFIED_BIT_PERFECT,completed.status)
    }
    @Test fun androidFormatMatchNeverUsesNativeCompletionCounters() {
        val format=PcmFormat(48000,24,2)
        val p=PreparedPcm(File("not-opened"),"WAV",format,288000,"test","test","test")
        val state=AudioState(pcm=p,client=format,playing=true,routed=true,usbCompletedFrames=48000)
        assertEquals(PathStatus.FORMAT_MATCH,state.status)
        assertNotEquals(PathStatus.DIRECT_OUTPUT,state.status)
        assertNotEquals(PathStatus.VERIFIED_BIT_PERFECT,state.status)
    }
}
