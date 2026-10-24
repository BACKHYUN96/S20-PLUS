package app.purebit.ui

import app.purebit.core.PreparedPcm
import java.io.RandomAccessFile
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/** A separate read-only file handle and worker; never runs on the audio writer or UI thread. */
class SpectrumAnalyzer : AutoCloseable {
    private val executor = Executors.newSingleThreadExecutor { task ->
        Thread(task, "BitwireSpectrum").apply { isDaemon = true; priority = Thread.MIN_PRIORITY }
    }
    private val busy = AtomicBoolean()
    private val generation = AtomicLong()
    private val math = SpectrumMath()
    private val bytes = ByteArray(math.size * 8)
    private var source: PreparedPcm? = null
    private var reader: RandomAccessFile? = null
    @Volatile private var closed = false
    @Volatile var result = SpectrumMath.Result(FloatArray(64), 0f, 0f)
        private set

    fun request(pcm: PreparedPcm, positionMs: Long) {
        if (closed || !busy.compareAndSet(false, true)) return
        val epoch = generation.get()
        try {
            executor.execute {
                try {
                    if (closed || epoch != generation.get()) return@execute
                    if (source !== pcm) {
                        reader?.close()
                        reader = RandomAccessFile(pcm.file, "r")
                        source = pcm
                    }
                    val frame = (positionMs.coerceAtLeast(0) * pcm.format.rate / 1000)
                        .coerceAtMost(pcm.frames)
                    val start = (frame - math.size / 2).coerceAtLeast(0)
                    val count = minOf(math.size.toLong(), pcm.frames - start).toInt() * pcm.format.frameBytes
                    val file = requireNotNull(reader)
                    file.seek(start * pcm.format.frameBytes)
                    file.readFully(bytes, 0, count)
                    val next = math.analyze(bytes, count, pcm.format)
                    if (!closed && epoch == generation.get()) result = next
                } catch (_: Exception) {
                    // A replaced/deleted PCM cache may disappear; visualisation failure is never playback failure.
                    if (epoch == generation.get()) result = SpectrumMath.Result(FloatArray(64), 0f, 0f)
                } finally { busy.set(false) }
            }
        } catch (_: java.util.concurrent.RejectedExecutionException) { busy.set(false) }
    }

    fun suspend() {
        generation.incrementAndGet()
        result = SpectrumMath.Result(FloatArray(64), 0f, 0f)
        if (!closed) try {
            executor.execute { reader?.close(); reader = null; source = null }
        } catch (_: java.util.concurrent.RejectedExecutionException) { /* Already closed. */ }
    }

    override fun close() {
        if (closed) return
        closed = true
        generation.incrementAndGet()
        // Queue after an in-flight read; don't close its handle from another thread.
        executor.execute { reader?.close(); reader = null; source = null }
        executor.shutdown()
    }
}
