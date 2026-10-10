using System;

namespace PixelTraffic.UnityPrototype
{
    // Port of native ThemeBlend: four active seconds, smoothstep, retarget from current weights.
    public sealed class SceneBlend
    {
        public readonly double[] Weights;
        private readonly double[] start;
        private double elapsed = 4;
        private double duration = 4;
        private int leg;
        private bool pendingNight;
        private readonly bool sunsetOnDayToNight;
        public int Target { get; private set; }
        public SceneBlend(int count, int initial = 0, bool viaSunset = false)
        { Weights = new double[count]; start = new double[count]; sunsetOnDayToNight = viaSunset && count == 3; Snap(initial); }
        public void Snap(int value)
        {
            Target = Math.Max(0, Math.Min(Weights.Length - 1, value)); leg = Target; duration = elapsed = 4; pendingNight = false;
            for (int i = 0; i < Weights.Length; i++) Weights[i] = start[i] = i == Target ? 1 : 0;
        }
        public void Select(int value)
        {
            int next = Math.Max(0, Math.Min(Weights.Length - 1, value)); if (next == Target) return;
            Array.Copy(Weights, start, Weights.Length); Target = next; elapsed = 0;
            pendingNight = sunsetOnDayToNight && next == 2 && Weights[0] > Weights[2] && Weights[0] > .001;
            leg = pendingNight ? 1 : next; duration = pendingNight ? 2 : 4;
        }
        public void Advance(double seconds)
        {
            if (double.IsNaN(seconds) || double.IsInfinity(seconds) || seconds <= 0 || elapsed >= duration) return;
            double remaining = Math.Min(.1, seconds);
            while (remaining > 1e-12 && elapsed < duration)
            {
                double step = Math.Min(remaining, duration - elapsed); elapsed += step; remaining -= step;
                if (elapsed > duration - 1e-9) elapsed = duration;
                double t = elapsed / duration; t = t * t * (3 - 2 * t);
                for (int i = 0; i < Weights.Length; i++) Weights[i] = start[i] * (1 - t) + (i == leg ? t : 0);
                if (elapsed >= duration && pendingNight)
                { pendingNight = false; leg = Target; Array.Copy(Weights, start, Weights.Length); elapsed = 0; duration = 2; }
            }
        }
    }
}
