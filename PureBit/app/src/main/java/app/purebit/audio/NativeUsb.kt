package app.purebit.audio

object NativeUsb {
    init { System.loadLibrary("purebit_usb") }
    external fun probe(fd: Int): String
    external fun volumeTest(fd: Int): String
    external fun start(fd: Int, pcmPath: String, expectedSha: String, rate: Int, bits: Int, channels: Int,
        interfaceId: Int, alternate: Int, endpoint: Int, maxPacket: Int, startFrame: Long, hardwareMusic: Boolean, intentToken: Long, requestedVolume: Int, endFrameExclusive: Long): Long
    /** prepare returns muted with no PCM thread; begin is a separate explicit, cancellation-fenced step. */
    external fun begin(handle: Long)
    external fun invalidatePreparation(): Long
    external fun volumeStep(handle: Long, direction: Int)
    external fun poll(handle: Long): LongArray
    external fun details(handle: Long): String
    external fun close(handle: Long): String
    /** Evicts a prepared source after playback stopped, before its private cache is deleted. */
    external fun releaseSource(pcmPath: String)
}
