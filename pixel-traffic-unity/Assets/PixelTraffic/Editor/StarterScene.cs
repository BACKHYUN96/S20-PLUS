using System;
using System.Collections.Generic;
using System.IO;
using UnityEditor;
using UnityEditor.Build;
using UnityEditor.SceneManagement;
using UnityEngine;
using UnityEngine.Rendering;
using UnityEngine.Rendering.Universal;
using UnityEngine.SceneManagement;

namespace PixelTraffic.UnityPrototype.Editor
{
    // Generated geometry stays in its own scene; existing scenes are never rebuilt in place.
    public static class StarterScene
    {
        internal const string Generated = "Assets/PixelTraffic/Generated/VehicleDetail080";
        private const string PipelinePath = Generated + "/MobileURP.asset";
        private static readonly Dictionary<string, Material> materials = new Dictionary<string, Material>();

        [MenuItem("Pixel Traffic/1. Prepare First Scene")]
        public static void Prepare()
        {
            RequireEditor();
            if (!EditorSceneManager.SaveCurrentModifiedScenesIfUserWantsTo()) return;
            if (File.Exists(StarterConfig.ScenePath))
            {
                EditorSceneManager.OpenScene(StarterConfig.ScenePath);
                Debug.Log("Existing traffic scene opened; geometry and user edits preserved.");
                return;
            }
            CreateScene();
        }

        public static void PrepareBatch()
        {
            RequireEditor();
            if (!File.Exists(StarterConfig.ScenePath)) CreateScene();
            Validate();
        }

        public static void RequireEditor()
        {
            if (Application.unityVersion != StarterConfig.EditorVersion)
                throw new InvalidOperationException("Open this project with Unity " + StarterConfig.EditorVersion +
                    ". Current Editor: " + Application.unityVersion);
        }

        private static void CreateScene()
        {
            Directory.CreateDirectory(Generated);
            Directory.CreateDirectory(Path.GetDirectoryName(StarterConfig.ScenePath));
            AssetDatabase.Refresh();
            materials.Clear();
            ConfigurePipeline();
            ConfigurePlayer();
            Scene scene = EditorSceneManager.NewScene(NewSceneSetup.EmptyScene, NewSceneMode.Single);
            RenderSettings.ambientMode = AmbientMode.Trilight;
            RenderSettings.ambientSkyColor = new Color(.68f, .75f, .83f);
            RenderSettings.ambientEquatorColor = new Color(.43f, .46f, .49f);
            RenderSettings.ambientGroundColor = new Color(.25f, .24f, .22f);
            RenderSettings.fog = true;
            RenderSettings.fogMode = FogMode.Linear;
            RenderSettings.fogColor = new Color(.72f, .78f, .81f);
            RenderSettings.fogStartDistance = 130;
            RenderSettings.fogEndDistance = 360;

            var camera = new GameObject("Portrait Camera").AddComponent<Camera>();
            camera.tag = "MainCamera";
            camera.transform.position = new Vector3(0, 26, -32);
            camera.transform.LookAt(new Vector3(0, 0, 28));
            camera.fieldOfView = 50;
            camera.nearClipPlane = .3f;
            camera.farClipPlane = 500;
            camera.clearFlags = CameraClearFlags.SolidColor;
            camera.backgroundColor = new Color(.65f, .77f, .86f);
            camera.allowHDR = false;
            camera.gameObject.AddComponent<UniversalAdditionalCameraData>();

            var sun = new GameObject("Afternoon Sun").AddComponent<Light>();
            sun.type = LightType.Directional;
            sun.color = new Color(1, .88f, .70f);
            sun.intensity = 1.35f;
            sun.shadows = LightShadows.Soft;
            sun.shadowStrength = .82f;
            sun.transform.rotation = Quaternion.Euler(43, -38, 0);
            RenderSettings.sun = sun;

            Transform road = new GameObject("Road and Sidewalks").transform;
            Transform scenery = new GameObject("Scenery").transform;
            CityEnvironment.Create(road, scenery);

            TrafficFleet.Create();
            StreetScene.Create();
            ClimateScene.Create();
            EditorSceneManager.SaveScene(scene, StarterConfig.ScenePath);
            EditorBuildSettings.scenes = new[] { new EditorBuildSettingsScene(StarterConfig.ScenePath, true) };
            AssetDatabase.SaveAssets();
            Debug.Log("Traffic scene saved. Set Game view to 9:20, then press Play. Time/weather settings are available in the live wallpaper launcher.");
        }

        private static void ConfigurePipeline()
        {
            var pipeline = AssetDatabase.LoadAssetAtPath<UniversalRenderPipelineAsset>(PipelinePath);
            if (pipeline == null)
            {
                const string rendererPath = Generated + "/MobileRenderer.asset";
                var renderer = AssetDatabase.LoadAssetAtPath<UniversalRendererData>(rendererPath);
                if (renderer == null)
                {
                    renderer = ScriptableObject.CreateInstance<UniversalRendererData>();
                    AssetDatabase.CreateAsset(renderer, rendererPath);
                }
                pipeline = UniversalRenderPipelineAsset.Create(renderer);
                pipeline.renderScale = 1;
                pipeline.msaaSampleCount = 2;
                pipeline.maxAdditionalLightsCount = 4;
                pipeline.supportsHDR = false;
                pipeline.shadowDistance = 70;
                pipeline.shadowCascadeCount = 2;
                pipeline.mainLightShadowmapResolution = 2048;
                pipeline.useSRPBatcher = true;
                AssetDatabase.CreateAsset(pipeline, PipelinePath);
            }
            // URP 17.3 exposes these flags as getters; set its serialized asset fields.
            var settings = new SerializedObject(pipeline);
            settings.FindProperty("m_AdditionalLightsRenderingMode").intValue = (int)LightRenderingMode.PerPixel;
            settings.FindProperty("m_MainLightShadowsSupported").boolValue = true;
            settings.FindProperty("m_SoftShadowsSupported").boolValue = true;
            settings.FindProperty("m_SoftShadowQuality").intValue = (int)SoftShadowQuality.Low;
            settings.ApplyModifiedPropertiesWithoutUndo();
            pipeline.shadowDepthBias = .45f;
            pipeline.shadowNormalBias = .35f;
            EditorUtility.SetDirty(pipeline);
            GraphicsSettings.defaultRenderPipeline = pipeline;
            int previousQuality = QualitySettings.GetQualityLevel();
            for (int i = 0; i < QualitySettings.names.Length; i++)
            {
                QualitySettings.SetQualityLevel(i, false);
                QualitySettings.renderPipeline = pipeline;
            }
            QualitySettings.SetQualityLevel(previousQuality, false);
        }

        public static void ConfigurePlayer()
        {
            PlayerSettings.companyName = "BACKHYUN96";
            PlayerSettings.productName = "Pixel Traffic Unity";
            PlayerSettings.bundleVersion = StarterConfig.VersionName;
            PlayerSettings.defaultInterfaceOrientation = UIOrientation.Portrait;
            PlayerSettings.colorSpace = ColorSpace.Linear;
            PlayerSettings.runInBackground = false;
            PlayerSettings.SetApplicationIdentifier(NamedBuildTarget.Android, StarterConfig.ExperimentAppId);
            PlayerSettings.Android.bundleVersionCode = StarterConfig.VersionCode;
            PlayerSettings.Android.minSdkVersion = AndroidSdkVersions.AndroidApiLevel29;
            PlayerSettings.Android.targetSdkVersion = AndroidSdkVersions.AndroidApiLevelAuto;
            PlayerSettings.Android.targetArchitectures = AndroidArchitecture.ARM64;
            PlayerSettings.SetScriptingBackend(NamedBuildTarget.Android, ScriptingImplementation.IL2CPP);
            // Retain the Activity Java backend; the generated manifest hosts it in a WallpaperService.
            PlayerSettings.Android.applicationEntry = AndroidApplicationEntry.Activity;
        }

        internal static Material Surface(string name, Color color, float metallic = 0, float smoothness = .25f)
        {
            if (materials.TryGetValue(name, out Material existing)) return existing;
            string path = Generated + "/" + name.Replace(" ", "") + ".mat";
            Material material = AssetDatabase.LoadAssetAtPath<Material>(path);
            if (material == null)
            {
                Shader shader = Shader.Find("Universal Render Pipeline/Lit");
                if (shader == null) throw new InvalidOperationException("URP Lit shader missing; wait for Package Manager import.");
                material = new Material(shader) { name = name };
                material.enableInstancing = true;
                material.SetColor("_BaseColor", color);
                material.SetFloat("_Metallic", metallic);
                material.SetFloat("_Smoothness", smoothness);
                AssetDatabase.CreateAsset(material, path);
            }
            materials.Add(name, material);
            return material;
        }

        internal static GameObject Part(string name, Transform parent, PrimitiveType type, Vector3 position, Vector3 scale, Material material)
        {
            GameObject part = GameObject.CreatePrimitive(type);
            part.name = name;
            part.transform.SetParent(parent, false);
            part.transform.localPosition = position;
            part.transform.localScale = scale;
            part.GetComponent<Renderer>().sharedMaterial = material;
            var collider = part.GetComponent<Collider>();
            if (collider != null) UnityEngine.Object.DestroyImmediate(collider);
            return part;
        }

        internal static GameObject Box(string name, Transform parent, Vector3 position, Vector3 scale, Material material)
            => Part(name, parent, PrimitiveType.Cube, position, scale, material);

        [MenuItem("Pixel Traffic/2. Validate First Scene")]
        public static void Validate()
        {
            RequireEditor();
            if (EditorApplication.isPlaying) throw new InvalidOperationException("Stop Play mode before scene validation.");
            if (!File.Exists(StarterConfig.ScenePath)) throw new InvalidOperationException("Run Prepare First Scene first.");
            if (!Application.isBatchMode && !EditorSceneManager.SaveCurrentModifiedScenesIfUserWantsTo()) return;
            EditorSceneManager.OpenScene(StarterConfig.ScenePath);
            var pipeline = GraphicsSettings.defaultRenderPipeline as UniversalRenderPipelineAsset;
            Require(pipeline != null, "URP asset not assigned.");
            Require(pipeline.scriptableRenderer != null, "URP renderer not available.");
            Require(PlayerSettings.GetApplicationIdentifier(NamedBuildTarget.Android) == StarterConfig.ExperimentAppId, "Experimental app ID changed.");
            TrafficFleet.Report traffic = TrafficFleet.Validate();
            VehicleDetailChecks.Report vehicleDetail = VehicleDetailChecks.Validate();
            StreetChecks.Report street = StreetChecks.Validate();
            AndroidWallpaperBuild.ValidateTemplates();
            Require(Camera.main != null && Camera.main.farClipPlane > 120, "Portrait camera missing.");
            CityEnvironment.Report environment = CityEnvironment.Validate(Camera.main, pipeline);
            ClimateChecks.Report climate = ClimateChecks.Validate();
            Require(PlayerSettings.bundleVersion == StarterConfig.VersionName &&
                PlayerSettings.Android.bundleVersionCode == StarterConfig.VersionCode, "APK version differs from source configuration.");
            Directory.CreateDirectory("Reports");
            File.WriteAllText("Reports/scene-validation.json", JsonUtility.ToJson(new ValidationReport {
                editor = Application.unityVersion, applicationId = StarterConfig.ExperimentAppId,
                utc = DateTime.UtcNow.ToString("O"), traffic = traffic, street = street,
                version = StarterConfig.VersionName, environment = environment, climate = climate, vehicleDetail = vehicleDetail,
                result = "PASS: actual city and two-way fleet geometry, material, lane bounds, motion, wrap, wheels and lifecycle; not an Android/device test"
            }, true));
            Debug.Log("PASS: city and two-way traffic geometry and motion. Reports/scene-validation.json");
            // Checks create transient runtime materials/meshes. Build from the saved scene,
            // so the next player initializes from original asset references and stored preferences.
            EditorSceneManager.OpenScene(StarterConfig.ScenePath);
            Require(UnityEngine.Object.FindFirstObjectByType<CityClimate>().TimeBlend == null, "Validation state leaked into the build scene.");
        }

        private static void Require(bool condition, string message)
        {
            if (!condition) throw new InvalidOperationException(message);
        }

        [Serializable]
        private sealed class ValidationReport
        {
            public string editor, applicationId, utc, result, version;
            public CityEnvironment.Report environment;
            public ClimateChecks.Report climate;
            public TrafficFleet.Report traffic;
            public VehicleDetailChecks.Report vehicleDetail;
            public StreetChecks.Report street;
        }
    }
}
