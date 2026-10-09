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
        internal const string Generated = "Assets/PixelTraffic/Generated/City020";
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
                Debug.Log("Existing FirstRoad opened; geometry and user edits preserved.");
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

            CreateVehicle();
            EditorSceneManager.SaveScene(scene, StarterConfig.ScenePath);
            EditorBuildSettings.scenes = new[] { new EditorBuildSettingsScene(StarterConfig.ScenePath, true) };
            AssetDatabase.SaveAssets();
            Debug.Log("FirstRoad saved. Set Game view to 9:16, then press Play. This is a starter scene, not the final city or live wallpaper.");
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
                pipeline.supportsHDR = false;
                pipeline.shadowDistance = 70;
                pipeline.shadowCascadeCount = 2;
                pipeline.mainLightShadowmapResolution = 2048;
                pipeline.useSRPBatcher = true;
                AssetDatabase.CreateAsset(pipeline, PipelinePath);
            }
            // URP 17.3 exposes these flags as getters; set its serialized asset fields.
            var settings = new SerializedObject(pipeline);
            settings.FindProperty("m_MainLightShadowsSupported").boolValue = true;
            settings.FindProperty("m_SoftShadowsSupported").boolValue = true;
            settings.ApplyModifiedPropertiesWithoutUndo();
            pipeline.shadowDepthBias = .45f;
            pipeline.shadowNormalBias = .35f;
            pipeline.softShadowQuality = SoftShadowQuality.Low;
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
            PlayerSettings.productName = "Pixel Traffic Unity Prototype";
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
            // Compatibility probe for a future native Service host uses the Activity entry point.
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

        private static void CreateVehicle()
        {
            Transform root = new GameObject("Prototype Vehicle").transform;
            root.position = new Vector3(StarterConfig.LaneCenter, 0, 24);
            root.rotation = Quaternion.Euler(0, 180, 0);
            Material paint = Surface("Vehicle Blue", new Color(.07f, .24f, .60f), .45f, .55f);
            Material glass = Surface("Vehicle Glass", new Color(.06f, .14f, .21f), .3f, .8f);
            Material trim = Surface("Vehicle Trim", new Color(.045f, .05f, .06f));
            Material chrome = Surface("Vehicle Metal", new Color(.63f, .67f, .70f), .7f, .6f);
            Box("Lower Body", root, new Vector3(0, .55f, 0), new Vector3(1.80f, .40f, 4.20f), paint);
            Box("Hood", root, new Vector3(0, .83f, 1.22f), new Vector3(1.72f, .24f, 1.43f), paint);
            Box("Trunk", root, new Vector3(0, .83f, -1.62f), new Vector3(1.72f, .24f, .75f), paint);
            Box("Cabin", root, new Vector3(0, 1.14f, -.32f), new Vector3(1.48f, .60f, 1.70f), glass);
            Box("Roof", root, new Vector3(0, 1.49f, -.35f), new Vector3(1.51f, .12f, 1.40f), paint);
            Box("Front Bumper", root, new Vector3(0, .46f, 2.11f), new Vector3(1.68f, .18f, .08f), trim);
            Box("Grille", root, new Vector3(0, .71f, 2.12f), new Vector3(.70f, .20f, .055f), trim);
            Box("Rear Bumper", root, new Vector3(0, .46f, -2.11f), new Vector3(1.68f, .18f, .08f), trim);
            var wheels = new List<Transform>();
            foreach (float side in new[] { -1f, 1f })
            {
                Box("Mirror", root, new Vector3(side * .97f, 1.08f, .52f), new Vector3(.19f, .13f, .30f), paint);
                Box("Headlamp", root, new Vector3(side * .61f, .80f, 2.12f), new Vector3(.34f, .13f, .06f), Surface("Headlamp", new Color(.97f, .94f, .79f)));
                Box("Tail Lamp", root, new Vector3(side * .63f, .80f, -2.12f), new Vector3(.34f, .13f, .06f), Surface("Tail Lamp", new Color(.66f, .04f, .025f)));
                Box("B Pillar", root, new Vector3(side * .75f, 1.19f, -.35f), new Vector3(.06f, .55f, .10f), trim);
                foreach (float z in new[] { -1.32f, 1.32f })
                {
                    Transform wheel = new GameObject("Wheel").transform;
                    wheel.SetParent(root, false);
                    wheel.localPosition = new Vector3(side * .87f, .32f, z);
                    var tyre = Part("Tyre", wheel, PrimitiveType.Cylinder, Vector3.zero, new Vector3(.64f, .105f, .64f), trim);
                    tyre.transform.localRotation = Quaternion.Euler(0, 0, 90);
                    var rim = Part("Rim", wheel, PrimitiveType.Cylinder, new Vector3(side * .11f, 0, 0), new Vector3(.40f, .015f, .40f), chrome);
                    rim.transform.localRotation = Quaternion.Euler(0, 0, 90);
                    wheels.Add(wheel);
                }
            }
            root.gameObject.AddComponent<PrototypeDrive>().Wheels = wheels.ToArray();
        }

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
            var vehicles = UnityEngine.Object.FindObjectsByType<PrototypeDrive>(FindObjectsSortMode.None);
            Require(vehicles.Length == 1, "Exactly one moving vehicle is required for this probe.");
            PrototypeDrive car = vehicles[0];
            Require(car.Wheels.Length == 4, "Four wheel pivots missing.");
            Require(Vector3.Dot(car.transform.forward, Vector3.back) > .999f, "Vehicle axis is not straight along its lane.");
            Require(Mathf.Abs(car.transform.position.x - StarterConfig.LaneCenter) < .001f, "Vehicle lane changed.");
            var renderers = car.GetComponentsInChildren<Renderer>();
            Require(renderers.Length > 0, "Vehicle has no geometry.");
            Bounds bounds = renderers[0].bounds;
            foreach (Renderer renderer in renderers)
            {
                bounds.Encapsulate(renderer.bounds);
                Require(renderer.sharedMaterial != null && renderer.sharedMaterial.shader.name == "Universal Render Pipeline/Lit", "Vehicle material missing or outside URP.");
            }
            float left = StarterConfig.LaneCenter - StarterConfig.LaneWidth / 2;
            float right = StarterConfig.LaneCenter + StarterConfig.LaneWidth / 2;
            Require(bounds.min.x > left && bounds.max.x < right, "Whole vehicle extends outside its lane.");
            Require(bounds.min.y >= -.01f && bounds.size.y > 1, "Vehicle ground/height incorrect.");
            Require(Camera.main != null && Camera.main.farClipPlane > 120, "Portrait camera missing.");
            CityEnvironment.Report environment = CityEnvironment.Validate(Camera.main, pipeline);
            Require(PlayerSettings.bundleVersion == StarterConfig.VersionName &&
                PlayerSettings.Android.bundleVersionCode == StarterConfig.VersionCode, "APK version differs from source configuration.");
            CheckMotion();
            Directory.CreateDirectory("Reports");
            File.WriteAllText("Reports/scene-validation.json", JsonUtility.ToJson(new ValidationReport {
                editor = Application.unityVersion, applicationId = StarterConfig.ExperimentAppId,
                utc = DateTime.UtcNow.ToString("O"), vehicleWidth = bounds.size.x, vehicleHeight = bounds.size.y,
                vehicleLength = bounds.size.z, wheelPivots = car.Wheels.Length,
                version = StarterConfig.VersionName, environment = environment,
                result = "PASS: real scene geometry, material, lane bounds and straight motion; not an Android/device test"
            }, true));
            Debug.Log("PASS: first scene geometry and motion. Reports/scene-validation.json");
        }

        private static void CheckMotion()
        {
            Require(PrototypeDrive.Advance(20, 0) == 20, "Paused time moves vehicle.");
            Require(Mathf.Abs(PrototypeDrive.Advance(20, 1) - 14) < .0001f, "Speed not six metres per second.");
            Require(Mathf.Abs(PrototypeDrive.Advance(StarterConfig.RouteStart, 1) - 84) < .0001f, "Wrap loses overshoot.");
            Require(Mathf.Abs(PrototypeDrive.Advance(20, 34) - 20) < .0001f, "Multiple loops lose phase.");
            foreach (int fps in new[] { 15, 30, 60, 120 })
            {
                float position = 24;
                for (int i = 0; i < fps * 20; i++) position = PrototypeDrive.Advance(position, 1f / fps);
                Require(Mathf.Abs(position - PrototypeDrive.Advance(24, 20)) < .02f, "Frame rate changes route phase.");
            }
            bool rejected = false;
            try { PrototypeDrive.Advance(24, float.NaN); } catch (ArgumentOutOfRangeException) { rejected = true; }
            Require(rejected, "Invalid time accepted.");
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
            public float vehicleWidth, vehicleHeight, vehicleLength;
            public int wheelPivots;
        }
    }
}
