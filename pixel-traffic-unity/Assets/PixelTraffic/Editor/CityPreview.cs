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
                started = EditorApplication.timeSinceStartup;
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
                File.WriteAllText("Reports/preview-result.json", JsonUtility.ToJson(new Report {
                    result = "PASS", version = StarterConfig.VersionName, editor = Application.unityVersion,
                    graphicsApi = SystemInfo.graphicsDeviceType.ToString(), width = width, height = height,
                    image = output, source = "Unity Editor URP camera; not a phone screenshot or FPS test"
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
        }
    }
}
