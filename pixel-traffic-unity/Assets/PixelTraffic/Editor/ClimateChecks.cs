using System;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.Rendering.Universal;

namespace PixelTraffic.UnityPrototype.Editor
{
    internal static class ClimateChecks
    {
        internal static Report Validate()
        {
            int[] rates = { 15, 30, 60, 120 }; int blendCases = 0;
            foreach (int count in new[] { 3, 6 }) foreach (int fps in rates)
                for (int from = 0; from < count; from++) for (int to = 0; to < count; to++)
                {
                    var blend = new SceneBlend(count, from); blend.Select(to);
                    for (int frame = 1; frame <= fps * 4; frame++)
                    {
                        blend.Advance(1.0 / fps); double t = frame / (fps * 4.0), eased = t * t * (3 - 2 * t);
                        for (int i = 0; i < count; i++)
                            Need(Math.Abs(blend.Weights[i] - ((i == from ? 1 - eased : 0) + (i == to ? eased : 0))) < 1e-9, "Native smoothstep oracle differs.");
                    }
                    blendCases++;
                }
            var interrupted = new SceneBlend(6); interrupted.Select(5);
            for (int i = 0; i < 20; i++) interrupted.Advance(.1);
            Need(Math.Abs(interrupted.Weights[5] - .5) < 1e-9, "Two-second midpoint differs.");
            var current = (double[])interrupted.Weights.Clone(); interrupted.Select(3);
            Need(Equal(current, interrupted.Weights), "Retarget changes the current frame.");
            interrupted.Advance(.1); var step = (double[])interrupted.Weights.Clone(); interrupted.Select(3);
            Need(Equal(step, interrupted.Weights), "Repeated selection restarts the blend.");
            interrupted.Advance(double.NaN); interrupted.Advance(double.PositiveInfinity); interrupted.Advance(-1);
            Need(Equal(step, interrupted.Weights), "Invalid elapsed time changes the scene.");
            interrupted.Advance(100); double q = .2 / 4, smooth = q * q * (3 - 2 * q);
            Need(Math.Abs(interrupted.Weights[5] - .5 * (1 - smooth)) < 1e-9, "Delayed frame must advance at most 0.1 seconds.");
            for (int i = 0; i < 40; i++) interrupted.Advance(.1);
            Need(interrupted.Weights[3] == 1, "Retarget fails to finish.");

            CityClimate climate = UnityEngine.Object.FindFirstObjectByType<CityClimate>(); Need(climate != null, "Climate controller missing."); climate.Initialize();
            var pipeline = UnityEngine.Rendering.GraphicsSettings.defaultRenderPipeline as UniversalRenderPipelineAsset;
            Need(pipeline.additionalLightsRenderingMode == LightRenderingMode.PerPixel && pipeline.maxAdditionalLightsCount == 4 && climate.AdditionalLights == 4, "Mobile light limit differs.");
            var fixedObjects = new List<Transform>(); var fixedPositions = new List<Vector3>(); var fixedRotations = new List<Quaternion>(); var trees = new List<Transform>();
            foreach (var transform in UnityEngine.Object.FindObjectsByType<Transform>(FindObjectsSortMode.None))
            {
                if (transform.name == "Layered Canopy") trees.Add(transform);
                if (transform.GetComponent<PrototypeDrive>() != null || transform.name.StartsWith("Walker ", StringComparison.Ordinal) || transform.name == "Tree Bed" || transform.name == "Signal Pole")
                { fixedObjects.Add(transform); fixedPositions.Add(transform.position); fixedRotations.Add(transform.rotation); }
            }
            Need(trees.Count == 32, "Wind canopy count differs."); int combos = 0;
            for (int theme = 0; theme < 3; theme++) for (int weather = 0; weather < 6; weather++)
            {
                climate.Preview(theme, weather); climate.Advance(.1, true);
                Need(climate.TimeBlend.Weights[theme] == 1 && climate.WeatherBlend.Weights[weather] == 1, "Saved selection does not snap on initial load.");
                Need(RenderSettings.sun.intensity > 0 && RenderSettings.fogEndDistance > RenderSettings.fogStartDistance && RenderSettings.fogEndDistance < Camera.main.farClipPlane, "Invalid lighting/fog endpoint.");
                double expectedWet = weather == 1 ? .68 : weather == 2 || weather == 5 ? 1 : weather == 4 ? .35 : 0;
                Need(Math.Abs(climate.Wetness - expectedWet) < .0001, "Native wetness mapping differs."); combos++;
            }
            climate.Preview(0, 0); climate.Select(2, 5);
            for (int i = 0; i < 20; i++) climate.Advance(.1, true);
            double halfway = climate.TimeBlend.Weights[2]; float clock = climate.ActiveSeconds; int launches = climate.Effects.PaperLaunches;
            for (int i = 0; i < 100; i++) climate.Advance(.1, false);
            Need(climate.ActiveSeconds == clock && climate.TimeBlend.Weights[2] == halfway && climate.Effects.PaperLaunches == launches, "Hidden scene advances climate or effects.");
            var before = (double[])climate.TimeBlend.Weights.Clone(); climate.Select(1, 3);
            Need(Equal(before, climate.TimeBlend.Weights), "Scene retarget jumps.");
            for (int i = 0; i < 40; i++) climate.Advance(.1, true);
            Need(climate.TimeBlend.Weights[1] == 1 && climate.WeatherBlend.Weights[3] == 1, "Scene transition incomplete after four active seconds.");
            climate.Preview(2, 5); Quaternion treeBefore = trees[0].localRotation;
            for (int i = 0; i < 300; i++) climate.Advance(.1, true);
            Need(Quaternion.Angle(treeBefore, trees[0].localRotation) > .01f, "Storm does not move actual canopy.");
            Need(climate.Effects.PaperLaunches - launches >= 5 && climate.Effects.LastPaperInterval >= 3 && climate.Effects.LastPaperInterval <= 5, "Storm paper cadence invalid.");
            for (int i = 0; i < fixedObjects.Count; i++) Need(fixedObjects[i].position == fixedPositions[i] && fixedObjects[i].rotation == fixedRotations[i], "Climate moves traffic, pedestrians or obstacles.");
            Need(climate.Effects.RainCapacity == 160 && climate.Effects.SnowCapacity == 96, "Precipitation pool is unbounded.");
            var report = new Report { result = "PASS: native four-second smoothstep oracle, retarget, pause, 18 scene endpoints, wind and fixed-capacity effects; device test pending", durationSeconds = 4, frameRates = rates, blendCases = blendCases, combinations = combos, realtimeStreetLights = climate.AdditionalLights, rainCapacity = 160, snowCapacity = 96, paperLaunches = climate.Effects.PaperLaunches, lastPaperInterval = climate.Effects.LastPaperInterval };
            climate.Preview(0, 0); climate.Advance(.01, true); return report;
        }
        private static bool Equal(double[] a, double[] b) { for (int i = 0; i < a.Length; i++) if (a[i] != b[i]) return false; return true; }
        private static void Need(bool condition, string reason) { if (!condition) throw new InvalidOperationException(reason); }
        [Serializable] public sealed class Report
        {
            public string result; public int durationSeconds, blendCases, combinations, realtimeStreetLights, rainCapacity, snowCapacity, paperLaunches;
            public int[] frameRates; public float lastPaperInterval;
        }
    }
}
