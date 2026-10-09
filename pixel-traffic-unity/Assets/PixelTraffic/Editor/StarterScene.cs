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
        private const string Generated = "Assets/PixelTraffic/Generated";
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
            RenderSettings.ambientSkyColor = new Color(.60f, .74f, .90f);
            RenderSettings.ambientEquatorColor = new Color(.39f, .43f, .49f);
            RenderSettings.ambientGroundColor = new Color(.20f, .21f, .23f);
            RenderSettings.fog = true;
            RenderSettings.fogMode = FogMode.Linear;
            RenderSettings.fogColor = new Color(.59f, .75f, .88f);
            RenderSettings.fogStartDistance = 100;
            RenderSettings.fogEndDistance = 190;

            var camera = new GameObject("Portrait Camera").AddComponent<Camera>();
            camera.tag = "MainCamera";
            camera.transform.position = new Vector3(0, 28, -28);
            camera.transform.LookAt(new Vector3(0, 0, 22));
            camera.fieldOfView = 44;
            camera.nearClipPlane = .3f;
            camera.farClipPlane = 220;
            camera.clearFlags = CameraClearFlags.SolidColor;
            camera.backgroundColor = new Color(.38f, .66f, .88f);
            camera.allowHDR = false;
            camera.gameObject.AddComponent<UniversalAdditionalCameraData>();

            var sun = new GameObject("Afternoon Sun").AddComponent<Light>();
            sun.type = LightType.Directional;
            sun.color = new Color(1, .91f, .77f);
            sun.intensity = 1.6f;
            sun.shadows = LightShadows.Soft;
            sun.transform.rotation = Quaternion.Euler(48, -35, 0);
            RenderSettings.sun = sun;

            Transform road = new GameObject("Road and Sidewalks").transform;
            Material asphalt = Surface("Asphalt", new Color(.16f, .18f, .22f));
            AddAsphaltTexture(asphalt);
            Box("Road", road, new Vector3(0, -.12f, 50), new Vector3(StarterConfig.RoadWidth, .24f, 140), asphalt);
            Material paving = Surface("Paving", new Color(.72f, .65f, .53f));
            Material curb = Surface("Curb", new Color(.79f, .79f, .73f));
            foreach (float side in new[] { -1f, 1f })
            {
                Box("Sidewalk", road, new Vector3(side * 8.4f, .08f, 50), new Vector3(4, .16f, 140), paving);
                Box("Curb", road, new Vector3(side * 6.52f, .13f, 50), new Vector3(.24f, .26f, 140), curb);
            }
            Material yellow = Surface("Road Yellow", new Color(.94f, .66f, .06f));
            Material white = Surface("Road White", new Color(.89f, .89f, .83f));
            foreach (float x in new[] { -.14f, .14f })
                Box("Centre Line", road, new Vector3(x, .012f, 50), new Vector3(.12f, .018f, 140), yellow);
            foreach (float x in new[] { -3.2f, 3.2f })
                for (float z = -15; z < 116; z += 9)
                    Box("Lane Dash", road, new Vector3(x, .012f, z), new Vector3(.14f, .018f, 3.4f), white);
            // Stripes run along the boulevard; the full crossing runs across the four lanes.
            for (int i = 0; i < 12; i++)
                Box("Zebra Stripe", road, new Vector3(-5.9f + i * 1.07f, .022f, 10), new Vector3(.64f, .018f, 4), white);

            Transform scenery = new GameObject("Scenery").transform;
            for (int i = 0; i < 8; i++)
                foreach (float side in new[] { -1f, 1f })
                    Tree(scenery, side * 8.5f, 4 + i * 14, i);
            for (int i = 0; i < 6; i++)
                foreach (float side in new[] { -1f, 1f })
                    Building(scenery, side * 14, 4 + i * 20, i);

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
                pipeline.shadowDistance = 55;
                pipeline.shadowCascadeCount = 2;
                pipeline.mainLightShadowmapResolution = 1024;
                pipeline.useSRPBatcher = true;
                AssetDatabase.CreateAsset(pipeline, PipelinePath);
            }
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
            PlayerSettings.bundleVersion = "0.1.0";
            PlayerSettings.defaultInterfaceOrientation = UIOrientation.Portrait;
            PlayerSettings.colorSpace = ColorSpace.Linear;
            PlayerSettings.runInBackground = false;
            PlayerSettings.SetApplicationIdentifier(NamedBuildTarget.Android, StarterConfig.ExperimentAppId);
            PlayerSettings.Android.bundleVersionCode = 1;
            PlayerSettings.Android.minSdkVersion = AndroidSdkVersions.AndroidApiLevel29;
            PlayerSettings.Android.targetSdkVersion = AndroidSdkVersions.AndroidApiLevelAuto;
            PlayerSettings.Android.targetArchitectures = AndroidArchitecture.ARM64;
            PlayerSettings.SetScriptingBackend(NamedBuildTarget.Android, ScriptingImplementation.IL2CPP);
            // Compatibility probe for a future native Service host uses the Activity entry point.
            PlayerSettings.Android.applicationEntry = AndroidApplicationEntry.Activity;
        }

        private static Material Surface(string name, Color color, float metallic = 0, float smoothness = .25f)
        {
            if (materials.TryGetValue(name, out Material existing)) return existing;
            string path = Generated + "/" + name.Replace(" ", "") + ".mat";
            Material material = AssetDatabase.LoadAssetAtPath<Material>(path);
            if (material == null)
            {
                Shader shader = Shader.Find("Universal Render Pipeline/Lit");
                if (shader == null) throw new InvalidOperationException("URP Lit shader missing; wait for Package Manager import.");
                material = new Material(shader) { name = name };
                material.SetColor("_BaseColor", color);
                material.SetFloat("_Metallic", metallic);
                material.SetFloat("_Smoothness", smoothness);
                AssetDatabase.CreateAsset(material, path);
            }
            materials.Add(name, material);
            return material;
        }

        private static void AddAsphaltTexture(Material material)
        {
            const string path = Generated + "/AsphaltPixels.asset";
            var texture = AssetDatabase.LoadAssetAtPath<Texture2D>(path);
            if (texture == null)
            {
                texture = new Texture2D(32, 32, TextureFormat.RGB24, true) { name = "Asphalt Pixels", filterMode = FilterMode.Point, wrapMode = TextureWrapMode.Repeat };
                var random = new System.Random(48);
                var pixels = new Color[32 * 32];
                for (int i = 0; i < pixels.Length; i++)
                {
                    float shade = .72f + (float)random.NextDouble() * .28f;
                    pixels[i] = new Color(shade, shade, shade);
                }
                texture.SetPixels(pixels);
                texture.Apply(true, false);
                AssetDatabase.CreateAsset(texture, path);
            }
            material.SetTexture("_BaseMap", texture);
            material.SetTextureScale("_BaseMap", new Vector2(4, 35));
            EditorUtility.SetDirty(material);
        }

        private static GameObject Part(string name, Transform parent, PrimitiveType type, Vector3 position, Vector3 scale, Material material)
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

        private static GameObject Box(string name, Transform parent, Vector3 position, Vector3 scale, Material material)
            => Part(name, parent, PrimitiveType.Cube, position, scale, material);

        private static void Tree(Transform parent, float x, float z, int index)
        {
            Transform root = new GameObject("Tree").transform;
            root.SetParent(parent, false);
            root.localPosition = new Vector3(x, .16f, z);
            Part("Trunk", root, PrimitiveType.Cylinder, new Vector3(0, 1.75f, 0), new Vector3(.16f, 1.75f, .16f), Surface("Bark", new Color(.30f, .20f, .12f)));
            for (int i = 0; i < 3; i++)
                Part("Canopy", root, PrimitiveType.Sphere, new Vector3((i - 1) * .65f, 3.6f + (i % 2) * .6f, 0), new Vector3(2.5f, 2.6f, 2.3f),
                    Surface(index % 2 == 0 ? "Leaf Light" : "Leaf Dark", index % 2 == 0 ? new Color(.23f, .48f, .12f) : new Color(.12f, .33f, .10f)));
        }

        private static void Building(Transform parent, float x, float z, int index)
        {
            float height = 10 + index % 3 * 4;
            Transform root = new GameObject("Building").transform;
            root.SetParent(parent, false);
            root.localPosition = new Vector3(x, .16f, z);
            Box("Facade", root, new Vector3(0, height / 2, 0), new Vector3(6.5f, height, 12), Surface("Facade " + index % 3, new Color(.63f + .06f * (index % 3), .66f, .64f)));
            Box("Roof", root, new Vector3(0, height + .12f, 0), new Vector3(6.8f, .24f, 12.3f), Surface("Roof", new Color(.33f, .36f, .39f)));
            float streetSide = x > 0 ? -3.27f : 3.27f;
            for (int floor = 0; floor < 3; floor++)
                for (int column = 0; column < 3; column++)
                    Box("Window", root, new Vector3(streetSide, 2.3f + floor * 2.8f, -3.4f + column * 3.4f), new Vector3(.045f, 1.55f, 1.7f), Surface("Window", new Color(.14f, .29f, .36f), .2f, .65f));
        }

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
            CheckMotion();
            Directory.CreateDirectory("Reports");
            File.WriteAllText("Reports/scene-validation.json", JsonUtility.ToJson(new ValidationReport {
                editor = Application.unityVersion, applicationId = StarterConfig.ExperimentAppId,
                utc = DateTime.UtcNow.ToString("O"), vehicleWidth = bounds.size.x, vehicleHeight = bounds.size.y,
                vehicleLength = bounds.size.z, wheelPivots = car.Wheels.Length,
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
            public string editor, applicationId, utc, result;
            public float vehicleWidth, vehicleHeight, vehicleLength;
            public int wheelPivots;
        }
    }
}
