using System;
using System.IO;
using UnityEditor;
using UnityEditor.SceneManagement;
using UnityEngine;
using UnityEngine.Rendering;
using UnityEngine.Rendering.Universal;

namespace PixelTraffic.UnityPrototype.Editor
{
    public static class CityPreview
    {
        private static double started;
        private static int warmupFrames;

        // A real URP camera render on the runner's GPU, not an AI-generated mockup.
        // This is an Editor preview; it does not assert Android device performance.
        public static void CaptureBatch()
        {
            try
            {
                StarterScene.RequireEditor();
                if (SystemInfo.graphicsDeviceType == GraphicsDeviceType.Null)
                    throw new InvalidOperationException("Preview needs a graphics device; do not use -nographics.");
                EditorSceneManager.OpenScene(StarterConfig.ScenePath);
                var street = UnityEngine.Object.FindFirstObjectByType<StreetSimulation>();
                if (street != null) { street.ResetModel(); street.Advance(30); street.ApplyViews(); }
                UnityEngine.Object.FindFirstObjectByType<CityClimate>().Initialize();
                started = EditorApplication.timeSinceStartup;
                warmupFrames = 0;
                EditorApplication.update += CaptureWhenReady;
                EditorApplication.QueuePlayerLoopUpdate();
            }
            catch (Exception exception) { Fail(exception); }
        }

        private static void CaptureWhenReady()
        {
            double elapsed = EditorApplication.timeSinceStartup - started;
            if (elapsed > 90) { Fail(new TimeoutException("Preview shader warmup timed out.")); return; }
            if (elapsed < 3 || ShaderUtil.anythingCompiling)
            {
                EditorApplication.QueuePlayerLoopUpdate();
                return;
            }
            if (warmupFrames < 3)
            {
                try
                {
                    Camera warmCamera = Camera.main;
                    var warmTarget = RenderTexture.GetTemporary(540, 1200, 24, RenderTextureFormat.ARGB32);
                    try
                    {
                        warmCamera.aspect = .45f;
                        RenderPipeline.SubmitRenderRequest(warmCamera, new UniversalRenderPipeline.SingleCameraRequest { destination = warmTarget });
                    }
                    finally { RenderTexture.ReleaseTemporary(warmTarget); }
                    warmupFrames++;
                    EditorApplication.QueuePlayerLoopUpdate();
                    return;
                }
                catch (Exception exception) { Fail(exception); return; }
            }
            EditorApplication.update -= CaptureWhenReady;
            RenderTexture target = null;
            Texture2D image = null;
            RenderTexture previous = RenderTexture.active;
            try
            {
                const int width = 540, height = 1200;
                Camera camera = Camera.main;
                if (camera == null) throw new InvalidOperationException("City camera missing.");
                camera.aspect = (float)width / height;
                target = new RenderTexture(width, height, 24, RenderTextureFormat.ARGB32) { antiAliasing = 2 };
                target.Create();
                var request = new UniversalRenderPipeline.SingleCameraRequest { destination = target };
                RenderPipeline.SubmitRenderRequest(camera, request);
                RenderTexture.active = target;
                image = new Texture2D(width, height, TextureFormat.RGB24, false);
                image.ReadPixels(new Rect(0, 0, width, height), 0, 0);
                image.Apply();
                Color32[] pixels = image.GetPixels32();
                int nonBlack = 0, pink = 0;
                foreach (Color32 pixel in pixels)
                {
                    if (pixel.r + pixel.g + pixel.b > 30) nonBlack++;
                    if (pixel.r > 220 && pixel.b > 220 && pixel.g < 50) pink++;
                }
                if (nonBlack < pixels.Length / 2 || pink > pixels.Length / 100)
                    throw new InvalidOperationException("Preview is black or has missing-material pink pixels.");
                Directory.CreateDirectory("Reports");
                string output = "Reports/city-preview-" + StarterConfig.VersionName + ".png";
                File.WriteAllBytes(output, image.EncodeToPNG());
                string[] names = { "Asphalt", "Road White", "Road Yellow", "Vehicle Blue", "Leaves 0", "Leaves 1", "Leaves 2" };
                var colors = new string[names.Length];
                for (int i = 0; i < names.Length; i++)
                {
                    Material material = AssetDatabase.LoadAssetAtPath<Material>(StarterScene.Generated + "/" + names[i].Replace(" ", "") + ".mat");
                    colors[i] = names[i] + ": " + material.GetColor("_BaseColor").ToString("F3");
                }
                var pipeline = (UniversalRenderPipelineAsset)GraphicsSettings.currentRenderPipeline;
                bool oldBatcher = pipeline.useSRPBatcher;
                bool oldGraphicsBatcher = GraphicsSettings.useScriptableRenderPipelineBatching;
                try
                {
                    pipeline.useSRPBatcher = false;
                    GraphicsSettings.useScriptableRenderPipelineBatching = false;
                    RenderPipeline.SubmitRenderRequest(camera, request);
                    RenderTexture.active = target;
                    image.ReadPixels(new Rect(0, 0, width, height), 0, 0); image.Apply();
                    File.WriteAllBytes("Reports/city-preview-" + StarterConfig.VersionName + "-batcher-diagnostic.png", image.EncodeToPNG());
                }
                finally
                {
                    pipeline.useSRPBatcher = oldBatcher;
                    GraphicsSettings.useScriptableRenderPipelineBatching = oldGraphicsBatcher;
                }
                var climate = UnityEngine.Object.FindFirstObjectByType<CityClimate>();
                var previews = new System.Collections.Generic.List<string>();
                int[] themes = { 1, 2, 0, 0, 0, 2 }; int[] weather = { 0, 0, 2, 3, 4, 5 };
                string[] labels = { "sunset", "night", "rain", "snow", "fog", "storm-night" };
                for (int i = 0; i < labels.Length; i++)
                {
                    climate.Preview(themes[i], weather[i]);
                    var street=UnityEngine.Object.FindFirstObjectByType<StreetSimulation>();
                    for (int n = 0; n < 50; n++) {climate.Advance(.1, true);street.Advance(.1);}street.ApplyViews();
                    previews.Add(CaptureState(camera, request, target, image, labels[i]));
                }
                climate.Preview(0, 0); climate.Advance(.01, true);
                var transitionStreet=UnityEngine.Object.FindFirstObjectByType<StreetSimulation>();transitionStreet.Advance(0);transitionStreet.ApplyViews();
                climate.Select(2, 0);
                previews.Add(CaptureState(camera, request, target, image, "transition-0s"));
                for(int second=1;second<=4;second++)
                {
                    for (int n = 0; n < 10; n++) climate.Advance(.1, true);
                    previews.Add(CaptureState(camera, request, target, image, "transition-"+second+"s"));
                }
                climate.Preview(0,5);
                var settledStreet=UnityEngine.Object.FindFirstObjectByType<StreetSimulation>();
                for(int n=0;n<2000;n++){climate.Advance(.1,true);settledStreet.Advance(.1);}settledStreet.ApplyViews();
                previews.Add(CaptureState(camera, request, target, image, "storm-settled"));
                climate.Preview(0, 0);
                File.WriteAllText("Reports/preview-result.json", JsonUtility.ToJson(new Report {
                    result = "PASS", version = StarterConfig.VersionName, editor = Application.unityVersion,
                    graphicsApi = SystemInfo.graphicsDeviceType.ToString(), width = width, height = height,
                    image = output, source = "Unity Editor URP camera; not a phone screenshot or FPS test"
                    , materialColors = colors, climateImages = previews.ToArray()
                }, true));
                Debug.Log("PASS: real city camera preview saved: " + output);
            }
            catch (Exception exception) { Fail(exception); return; }
            finally
            {
                RenderTexture.active = previous;
                if (target != null) { target.Release(); UnityEngine.Object.DestroyImmediate(target); }
                if (image != null) UnityEngine.Object.DestroyImmediate(image);
            }
            EditorApplication.Exit(0);
        }

        private static string CaptureState(Camera camera, UniversalRenderPipeline.SingleCameraRequest request, RenderTexture target, Texture2D image, string label)
        {
            RenderPipeline.SubmitRenderRequest(camera, request); RenderTexture.active = target;
            image.ReadPixels(new Rect(0, 0, image.width, image.height), 0, 0); image.Apply();
            int visible = 0, pink = 0; foreach (Color32 pixel in image.GetPixels32())
            { if (pixel.r + pixel.g + pixel.b > 15) visible++; if (pixel.r > 220 && pixel.b > 220 && pixel.g < 50) pink++; }
            if (visible < image.width * image.height / 3 || pink > image.width * image.height / 100) throw new InvalidOperationException("Climate preview is black or pink: " + label);
            string path = "Reports/city-preview-" + StarterConfig.VersionName + "-" + label + ".png"; File.WriteAllBytes(path, image.EncodeToPNG()); return path;
        }

        private static void Fail(Exception exception)
        {
            EditorApplication.update -= CaptureWhenReady;
            Directory.CreateDirectory("Reports");
            File.WriteAllText("Reports/preview-result.json", JsonUtility.ToJson(new Report {
                result = "FAILED", version = StarterConfig.VersionName, editor = Application.unityVersion,
                source = "Editor preview failed; APK verification is separate"
            }, true));
            Debug.LogException(exception);
            EditorApplication.Exit(1);
        }

        [Serializable] private sealed class Report
        {
            public string result, version, editor, graphicsApi, image, source;
            public int width, height;
            public string[] materialColors, climateImages;
        }
    }
}
