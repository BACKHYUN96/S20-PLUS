package app.purebit.audio

import android.util.Log
import app.purebit.core.RecentLog

object AudioLog {
    const val TAG = "PureBitAudio"
    private val recent = RecentLog()
    fun snapshot() = recent.snapshot()
    fun event(kind: String, detail: String) {
        // Logcat truncates long lines: preserve full USB descriptor/hash diagnostics in chunks.
        detail.chunked(2800).forEachIndexed { index, text ->
            recent.append("${System.currentTimeMillis()} I/$TAG $kind[$index] $text")
            Log.i(TAG, "$kind[$index] $text")
        }
    }
    fun error(kind: String, error: Throwable) {
        recent.append("${System.currentTimeMillis()} E/$TAG $kind: ${error.stackTraceToString()}")
        Log.e(TAG, "$kind: ${error.message}", error)
    }
}
