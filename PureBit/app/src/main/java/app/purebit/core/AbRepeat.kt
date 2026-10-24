package app.purebit.core

/** Positions are source milliseconds; the output range is [A, B), with no PCM processing. */
data class AbRepeat(val aMs: Long? = null, val bMs: Long? = null) {
    val active get() = aMs != null && bMs != null
    fun withA(ms: Long, durationMs: Long): AbRepeat {
        require(ms >= 0 && ms <= durationMs - MIN_INTERVAL_MS) { "A 뒤에 최소 1초가 필요합니다" }
        return AbRepeat(ms)
    }
    fun withB(ms: Long, durationMs: Long): AbRepeat {
        val a = requireNotNull(aMs) { "먼저 A 시작점을 지정하세요" }
        require(ms <= durationMs && ms - a >= MIN_INTERVAL_MS) { "B는 A보다 최소 1초 뒤여야 합니다" }
        return copy(bMs = ms)
    }
    fun constrain(ms: Long, durationMs: Long): Long {
        val clamped = ms.coerceIn(0, durationMs)
        return if (active && (clamped < aMs!! || clamped >= bMs!!)) aMs!! else clamped
    }
    fun endFrame(rate: Int, totalFrames: Long): Long = if (active) (bMs!! * rate / 1000).coerceAtMost(totalFrames) else totalFrames
    companion object { const val MIN_INTERVAL_MS = 1000L }
}

/** Preview leaves transport alone; release may seek only its original source/transport intent. */
data class ScrubToken(val generation: Int, val intent: Int, val resume: Boolean) {
    fun isCurrent(sourceGeneration: Int, playbackIntent: Int) = generation == sourceGeneration && intent == playbackIntent
}
