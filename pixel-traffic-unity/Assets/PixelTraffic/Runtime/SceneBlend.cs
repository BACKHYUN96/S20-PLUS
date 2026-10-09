using System;

namespace PixelTraffic.UnityPrototype
{
    // Port of native ThemeBlend: four active seconds, smoothstep, retarget from current weights.
    public sealed class SceneBlend
    {
        public readonly double[] Weights;
        private readonly double[] start;
        private double elapsed = 4;
        public int Target { get; private set; }
        public SceneBlend(int count, int initial = 0) { Weights = new double[count]; start = new double[count]; Snap(initial); }
        public void Snap(int value)
        {
            Target = Math.Max(0, Math.Min(Weights.Length - 1, value)); elapsed = 4;
            for (int i = 0; i < Weights.Length; i++) Weights[i] = start[i] = i == Target ? 1 : 0;
        }
        public void Select(int value)
        {
            int next = Math.Max(0, Math.Min(Weights.Length - 1, value)); if (next == Target) return;
            Array.Copy(Weights, start, Weights.Length); Target = next; elapsed = 0;
        }
        public void Advance(double seconds)
        {
            if (double.IsNaN(seconds) || double.IsInfinity(seconds) || seconds <= 0 || elapsed >= 4) return;
            elapsed = Math.Min(4, elapsed + Math.Min(.1, seconds)); if (elapsed > 4 - 1e-9) elapsed = 4;
            double t = elapsed / 4; t = t * t * (3 - 2 * t);
            for (int i = 0; i < Weights.Length; i++) Weights[i] = start[i] * (1 - t) + (i == Target ? t : 0);
        }
    }
}
