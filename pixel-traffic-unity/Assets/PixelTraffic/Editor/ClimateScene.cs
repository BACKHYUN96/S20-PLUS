using System;
using System.Collections.Generic;
using UnityEditor;
using UnityEngine;
using UnityEngine.Rendering;

namespace PixelTraffic.UnityPrototype.Editor
{
    internal static class ClimateScene
    {
        internal static void Create()
        {
            var root = new GameObject("Climate and Lighting");
            var controller = root.AddComponent<CityClimate>(); var effects = root.AddComponent<ClimateEffects>();
            var materials = new Material[6]; string[] names = { "Asphalt", "Paving", "Window Glass", "Lamp Lens", "Headlamp", "Tail Lamp" };
            for (int i = 0; i < names.Length; i++)
            {
                materials[i] = AssetDatabase.LoadAssetAtPath<Material>(StarterScene.Generated + "/" + names[i].Replace(" ", "") + ".mat");
                if (materials[i] == null) throw new InvalidOperationException("Climate material asset missing: " + names[i]);
            }
            var lamps = new List<Transform>(); var trees = new List<Transform>();
            foreach (var renderer in UnityEngine.Object.FindObjectsByType<Renderer>(FindObjectsSortMode.None))
            {
                if (renderer.name == "Lamp Lens") lamps.Add(renderer.transform);
                if (renderer.name == "Layered Canopy") { trees.Add(renderer.transform); GameObjectUtility.SetStaticEditorFlags(renderer.gameObject, 0); }
            }
            for (int i = 2; i < materials.Length; i++) { materials[i].EnableKeyword("_EMISSION"); materials[i].SetColor("_EmissionColor", Color.black); EditorUtility.SetDirty(materials[i]); }
            lamps.Sort((a, b) => Vector3.Distance(a.position, Camera.main.transform.position).CompareTo(Vector3.Distance(b.position, Camera.main.transform.position)));
            Light[] lights = new Light[4]; var pools = new Renderer[lamps.Count];
            Material glow = Transparent("Lamp Pool", RadialTexture(), true);
            for (int i = 0; i < lamps.Count; i++)
            {
                var pool = Quad("Warm Road Pool", root.transform, glow, false);
                pool.transform.position = new Vector3(lamps[i].position.x, .035f, lamps[i].position.z); pool.transform.rotation = Quaternion.Euler(90, 0, 0); pool.transform.localScale = new Vector3(5, 9, 1); pools[i] = pool;
                if (i < lights.Length)
                {
                    lights[i] = new GameObject("Local Street Light").AddComponent<Light>(); lights[i].transform.SetParent(lamps[i], false); lights[i].transform.localRotation = Quaternion.Euler(90, 0, 0);
                    lights[i].type = LightType.Spot; lights[i].range = 12; lights[i].spotAngle = 105; lights[i].color = new Color(1, .73f, .35f); lights[i].intensity = 0; lights[i].shadows = LightShadows.None;
                }
            }
            Material particles = Transparent("Weather Particles", null, true);
            MeshFilter rain = ParticlePool("Rain Pool", root.transform, particles, 160), snow = ParticlePool("Snow Pool", root.transform, particles, 96);
            Renderer paper = Quad("Windblown Newspaper", root.transform, Transparent("Newspaper", PaperTexture(), false), true); paper.transform.localScale = new Vector3(.65f, .46f, 1); paper.enabled = false;
            effects.Configure(rain, snow, paper, pools); controller.Configure(materials, lights, trees.ToArray(), effects);
        }
        private static Material Transparent(string name, Texture2D texture, bool emission)
        {
            var mat = StarterScene.Surface(name, Color.white);
            mat.SetFloat("_Surface", 1); mat.SetFloat("_Blend", 0); mat.SetFloat("_SrcBlend", (float)BlendMode.SrcAlpha); mat.SetFloat("_DstBlend", (float)BlendMode.OneMinusSrcAlpha); mat.SetFloat("_ZWrite", 0); mat.SetFloat("_Cull", 0);
            mat.EnableKeyword("_SURFACE_TYPE_TRANSPARENT"); mat.renderQueue = (int)RenderQueue.Transparent;
            if (texture != null) mat.SetTexture("_BaseMap", texture);
            if (emission) { mat.EnableKeyword("_EMISSION"); mat.SetColor("_EmissionColor", Color.black); }
            EditorUtility.SetDirty(mat); return mat;
        }
        private static Renderer Quad(string name, Transform root, Material mat, bool billboard)
        {
            var obj = GameObject.CreatePrimitive(PrimitiveType.Quad); obj.name = name; obj.transform.SetParent(root, false); UnityEngine.Object.DestroyImmediate(obj.GetComponent<Collider>());
            var renderer = obj.GetComponent<Renderer>(); renderer.sharedMaterial = mat; renderer.shadowCastingMode = ShadowCastingMode.Off; renderer.receiveShadows = false; return renderer;
        }
        private static MeshFilter ParticlePool(string name, Transform root, Material mat, int count)
        {
            var obj = new GameObject(name, typeof(MeshFilter), typeof(MeshRenderer)); obj.transform.SetParent(root, false);
            var mesh = new Mesh { name = name, vertices = new Vector3[count * 4], bounds = new Bounds(new Vector3(0, 8, 55), new Vector3(30, 20, 185)) };
            var normals = new Vector3[count * 4]; var uv = new Vector2[count * 4];
            for (int i = 0; i < normals.Length; i++) { normals[i] = Vector3.back; uv[i] = new Vector2(i % 4 == 1 || i % 4 == 2 ? 1 : 0, i % 4 >= 2 ? 1 : 0); }
            mesh.normals = normals; mesh.uv = uv;
            var triangles = new int[count * 6];
            for (int i = 0; i < count; i++) { int k = i * 4, n = i * 6; triangles[n] = k; triangles[n+1] = k+1; triangles[n+2] = k+2; triangles[n+3] = k; triangles[n+4] = k+2; triangles[n+5] = k+3; }
            mesh.triangles = triangles; mesh.bounds = new Bounds(new Vector3(0, 8, 55), new Vector3(30, 20, 185));
            AssetDatabase.CreateAsset(mesh, StarterScene.Generated + "/" + name.Replace(" ", "") + ".asset");
            var filter = obj.GetComponent<MeshFilter>(); filter.sharedMesh = mesh; var renderer = obj.GetComponent<MeshRenderer>(); renderer.sharedMaterial = mat; renderer.shadowCastingMode = ShadowCastingMode.Off; renderer.receiveShadows = false; renderer.enabled = false; return filter;
        }
        private static Texture2D RadialTexture() => Texture("LampFalloff", true);
        private static Texture2D PaperTexture() => Texture("Newsprint", false);
        private static Texture2D Texture(string name, bool radial)
        {
            var texture = new Texture2D(64, 64, TextureFormat.RGBA32, true) { name = name, wrapMode = TextureWrapMode.Clamp, filterMode = FilterMode.Bilinear };
            var pixels = new Color[4096];
            for (int y = 0; y < 64; y++) for (int x = 0; x < 64; x++)
            {
                float radius = new Vector2((x - 31.5f) / 32, (y - 31.5f) / 32).magnitude;
                float ink = x > 5 && x < 59 && y > 8 && y < 56 && (y % 5 == 0 || y > 48) && x % 29 < 24 ? .30f : .92f;
                pixels[y * 64 + x] = radial ? new Color(1, 1, 1, Mathf.Pow(Mathf.Clamp01(1 - radius), 2)) : new Color(ink, ink, ink * .96f, 1);
            }
            texture.SetPixels(pixels); texture.Apply(true, false); AssetDatabase.CreateAsset(texture, StarterScene.Generated + "/" + name + ".asset"); return texture;
        }
    }
}
