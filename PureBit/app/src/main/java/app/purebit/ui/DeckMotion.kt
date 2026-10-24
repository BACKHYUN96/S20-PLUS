package app.purebit.ui

import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.max

/** Smooth source-energy response, not a beat/clock/DAC verification algorithm. */
class BassPulse {
    var level = 0f; private set
    private var baseline = 0f
    private var onset = 0f
    fun update(source: Float, dt: Float, enabled: Boolean): Float {
        if (!enabled) { level = 0f; baseline = 0f; onset = 0f; return 0f }
        val t = dt.coerceIn(0f, .1f); val energy = source.coerceIn(0f, 1f)
        baseline += (energy - baseline) * (1 - exp(-2.5f * t))
        onset = max((energy - baseline - .035f).coerceAtLeast(0f), onset * exp(-5f * t))
        val target = (energy * .2f + onset * 1.7f).coerceIn(0f, 1f)
        val speed = if (target > level) 16f else 4f
        level += (target - level) * (1 - exp(-speed * t))
        return level
    }
}

/** Uniform-distance three-segment interpolation with no per-frame allocations. */
object CircuitFlow {
    fun point(fraction: Float, x0: Float, y0: Float, x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, out: FloatArray) {
        val a = hypot(x1 - x0, y1 - y0); val b = hypot(x2 - x1, y2 - y1); val c = hypot(x3 - x2, y3 - y2)
        var distance = fraction.coerceIn(0f, 1f) * (a + b + c)
        if (a > 0 && distance <= a) { val t = distance / a; out[0] = x0 + (x1 - x0) * t; out[1] = y0 + (y1 - y0) * t; return }
        distance -= a
        if (b > 0 && distance <= b) { val t = distance / b; out[0] = x1 + (x2 - x1) * t; out[1] = y1 + (y2 - y1) * t; return }
        distance -= b
        val t = if (c > 0) (distance / c).coerceIn(0f, 1f) else 1f
        out[0] = x2 + (x3 - x2) * t; out[1] = y2 + (y3 - y2) * t
    }
}
