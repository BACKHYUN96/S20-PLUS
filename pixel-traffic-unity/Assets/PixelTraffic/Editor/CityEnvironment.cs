using System;
using System.Collections.Generic;
using UnityEditor;
using UnityEngine;
using UnityEngine.Rendering.Universal;

namespace PixelTraffic.UnityPrototype.Editor
{
    // Editor-built, deterministic city geometry. No per-frame scenery generation.
    internal static class CityEnvironment
    {
        private const float RoadStart = -65, RoadEnd = 355;
        private static Material Mat(string name, Color color, float metallic = 0, float smoothness = .18f)
            => StarterScene.Surface(name, color, metallic, smoothness);
        private static GameObject Box(string name, Transform parent, Vector3 position, Vector3 scale, Material material)
            => StarterScene.Box(name, parent, position, scale, material);
        private static GameObject Cylinder(string name, Transform parent, Vector3 position, Vector3 scale, Material material)
            => StarterScene.Part(name, parent, PrimitiveType.Cylinder, position, scale, material);

        public static void Create(Transform road, Transform scenery)
        {
            float length = RoadEnd - RoadStart, centre = (RoadStart + RoadEnd) * .5f;
            Box("City Ground", scenery, new Vector3(0, -.3f, 170), new Vector3(420, .24f, 520), Mat("City Ground", new Color(.49f, .48f, .43f)));
            Material asphalt = Mat("Asphalt", new Color(.22f, .235f, .25f), 0, .12f);
            Texture(asphalt, "Asphalt", 0, new Vector2(StarterConfig.RoadWidth / 3, length / 3));
            Box("Road", road, new Vector3(0, -.12f, centre), new Vector3(StarterConfig.RoadWidth, .24f, length), asphalt);
            Material paving = Mat("Paving", new Color(.78f, .70f, .56f));
            Texture(paving, "Pavers", 1, new Vector2(2, length / 4));
            Material curb = Mat("Curb", new Color(.73f, .73f, .67f));
            foreach (float side in new[] { -1f, 1f })
            {
                Box("Sidewalk", road, new Vector3(side * 8.4f, .08f, centre), new Vector3(4, .16f, length), paving);
                Box("Curb", road, new Vector3(side * 6.52f, .13f, centre), new Vector3(.24f, .26f, length), curb);
                Box("Building Apron", road, new Vector3(side * 11, -.02f, centre), new Vector3(1.8f, .16f, length), Mat("Apron", new Color(.60f, .58f, .51f)));
            }
            Material yellow = Mat("Road Yellow", new Color(.96f, .66f, .08f));
            Material white = Mat("Road White", new Color(.88f, .88f, .82f));
            foreach (float x in new[] { -.14f, .14f })
                Box("Centre Line", road, new Vector3(x, .012f, centre), new Vector3(.12f, .018f, length), yellow);
            foreach (float x in new[] { -3.2f, 3.2f })
                for (float z = RoadStart + 3; z < RoadEnd - 3; z += 9)
                    Box("Lane Dash", road, new Vector3(x, .012f, z), new Vector3(.14f, .018f, 3.4f), white);
            for (int i = 0; i < 12; i++)
                Box("Zebra Stripe", road, new Vector3(-5.9f + i * 1.07f, .022f, 10), new Vector3(.64f, .018f, 4), white);
            for (int i = 0; i < 4; i++)
                Cylinder("Manhole", road, new Vector3(i % 2 == 0 ? -1.6f : 4.8f, .005f, 24 + i * 34), new Vector3(.7f, .004f, .7f), Mat("Iron", new Color(.17f, .18f, .18f), .15f));

            Mesh canopy = CanopyMesh();
            for (int i = 0; i < 16; i++)
                foreach (float side in new[] { -1f, 1f }) Tree(scenery, canopy, side * 8.6f, -24 + i * 13, i);
            for (int i = 0; i < 12; i++)
                foreach (float side in new[] { -1f, 1f }) Building(scenery, side * 14.1f, -22 + i * 17, i);
            for (int i = 0; i < 10; i++)
                foreach (float side in new[] { -1f, 1f }) StreetLamp(scenery, side * 7.1f, -15 + i * 20);
            Skyline(scenery);
            foreach (Transform root in new[] { road, scenery })
                foreach (Renderer renderer in root.GetComponentsInChildren<Renderer>())
                    GameObjectUtility.SetStaticEditorFlags(renderer.gameObject, StaticEditorFlags.BatchingStatic);
        }

        private static void Texture(Material material, string name, int style, Vector2 scale)
        {
            string path = StarterScene.Generated + "/" + name + ".asset";
            Texture2D texture = AssetDatabase.LoadAssetAtPath<Texture2D>(path);
            if (texture == null)
            {
                const int size = 128;
                texture = new Texture2D(size, size, TextureFormat.RGB24, true) {
                    name = name, filterMode = FilterMode.Trilinear, wrapMode = TextureWrapMode.Repeat, anisoLevel = 2
                };
                var pixels = new Color[size * size];
                for (int y = 0; y < size; y++)
                    for (int x = 0; x < size; x++)
                    {
                        float grain = ((x * 73 + y * 179 + x * y * 13) % 101) / 100f;
                        float broad = Mathf.PerlinNoise(x * .055f + 12, y * .055f + 29);
                        float shade = .83f + broad * .13f + grain * .04f;
                        if (style == 1)
                            shade = x % 32 < 2 || y % 32 < 2 ? .63f : .87f + ((x / 32 + y / 32 * 3) % 5) * .023f;
                        else if (style == 2)
                            shade = y % 32 < 2 || (x + (y / 32 % 2) * 16) % 32 < 1 ? .70f : .87f + broad * .11f;
                        pixels[y * size + x] = new Color(shade, shade, shade);
                    }
                texture.SetPixels(pixels);
                texture.Apply(true, false);
                AssetDatabase.CreateAsset(texture, path);
            }
            material.SetTexture("_BaseMap", texture);
            material.SetTextureScale("_BaseMap", scale);
            EditorUtility.SetDirty(material);
        }

        private static void Tree(Transform parent, Mesh canopy, float x, float z, int index)
        {
            Transform root = new GameObject("City Tree").transform;
            root.SetParent(parent, false);
            root.localPosition = new Vector3(x, .16f, z);
            root.localRotation = Quaternion.Euler(0, index * 47, 0);
            Material bark = Mat("Bark", new Color(.33f, .23f, .13f));
            Cylinder("Trunk", root, new Vector3(0, 2, 0), new Vector3(.26f, 2, .26f), bark);
            for (int i = 0; i < 3; i++)
            {
                Vector3 direction = Quaternion.Euler(0, i * 120, 0) * new Vector3(.9f, 1.1f, 0);
                var branch = Cylinder("Branch", root, new Vector3(0, 3, 0) + direction * .5f, new Vector3(.13f, direction.magnitude * .5f, .13f), bark);
                branch.transform.localRotation = Quaternion.FromToRotation(Vector3.up, direction);
            }
            GameObject leaves = new GameObject("Layered Canopy", typeof(MeshFilter), typeof(MeshRenderer));
            leaves.transform.SetParent(root, false);
            leaves.transform.localScale = Vector3.one * (.87f + index % 3 * .065f);
            leaves.GetComponent<MeshFilter>().sharedMesh = canopy;
            Color[] greens = { new Color(.30f, .48f, .095f), new Color(.38f, .53f, .12f), new Color(.23f, .40f, .08f) };
            leaves.GetComponent<MeshRenderer>().sharedMaterial = Mat("Leaves " + index % 3, greens[index % 3]);
            Material planter = Mat("Planter Stone", new Color(.57f, .54f, .44f));
            Box("Tree Bed", root, new Vector3(0, .1f, 0), new Vector3(2.2f, .2f, 2.2f), planter);
            Box("Soil", root, new Vector3(0, .205f, 0), new Vector3(1.94f, .015f, 1.94f), Mat("Soil", new Color(.23f, .20f, .12f)));
        }

        private static Mesh CanopyMesh()
        {
            string path = StarterScene.Generated + "/LayeredCanopy.asset";
            Mesh existing = AssetDatabase.LoadAssetAtPath<Mesh>(path);
            if (existing != null) return existing;
            float t = (1 + Mathf.Sqrt(5)) / 2;
            var unit = new List<Vector3> {
                new Vector3(-1,t,0),new Vector3(1,t,0),new Vector3(-1,-t,0),new Vector3(1,-t,0),
                new Vector3(0,-1,t),new Vector3(0,1,t),new Vector3(0,-1,-t),new Vector3(0,1,-t),
                new Vector3(t,0,-1),new Vector3(t,0,1),new Vector3(-t,0,-1),new Vector3(-t,0,1)
            };
            for (int i = 0; i < unit.Count; i++) unit[i] = unit[i].normalized;
            int[] faces = {0,11,5,0,5,1,0,1,7,0,7,10,0,10,11,1,5,9,5,11,4,11,10,2,10,7,6,7,1,8,3,9,4,3,4,2,3,2,6,3,6,8,3,8,9,4,9,5,2,4,11,6,2,10,8,6,7,9,8,1};
            var midpoints = new Dictionary<long, int>();
            int Mid(int a, int b)
            {
                long key = ((long)Math.Min(a, b) << 32) | (uint)Math.Max(a, b);
                if (!midpoints.TryGetValue(key, out int value)) { value = unit.Count; unit.Add((unit[a] + unit[b]).normalized); midpoints.Add(key, value); }
                return value;
            }
            var refined = new List<int>();
            for (int i = 0; i < faces.Length; i += 3)
            {
                int a = faces[i], b = faces[i + 1], c = faces[i + 2], ab = Mid(a, b), bc = Mid(b, c), ca = Mid(c, a);
                refined.AddRange(new[] {a,ab,ca,b,bc,ab,c,ca,bc,ab,bc,ca});
            }
            Vector3[] clusters = {new Vector3(0,5.5f,0),new Vector3(-1.3f,4.6f,.2f),new Vector3(1.3f,4.6f,-.2f),new Vector3(0,4.5f,1.2f),new Vector3(0,4.5f,-1.2f),new Vector3(-.7f,5.5f,.8f),new Vector3(.8f,5.6f,-.7f),new Vector3(0,4.3f,0)};
            var vertices = new List<Vector3>(); var triangles = new List<int>();
            for (int clump = 0; clump < clusters.Length; clump++)
            {
                int offset = vertices.Count;
                foreach (Vector3 v in unit) vertices.Add(clusters[clump] + Vector3.Scale(v, new Vector3(1.5f, 1.35f, 1.45f)));
                foreach (int index in refined) triangles.Add(offset + index);
            }
            var mesh = new Mesh { name = "Layered Tree Canopy" };
            mesh.SetVertices(vertices); mesh.SetTriangles(triangles, 0); mesh.RecalculateNormals(); mesh.RecalculateBounds();
            AssetDatabase.CreateAsset(mesh, path);
            return mesh;
        }

        private static void Building(Transform parent, float x, float z, int index)
        {
            int floors = 4 + index % 3; float height = floors * 2.8f + 1.2f;
            Transform root = new GameObject("City Building").transform;
            root.SetParent(parent, false); root.localPosition = new Vector3(x, .16f, z);
            Color[] colors = {new Color(.77f,.72f,.62f),new Color(.69f,.68f,.63f),new Color(.79f,.74f,.65f)};
            Material facade = Mat("Facade " + index % 3, colors[index % 3]);
            Texture(facade, "Masonry", 2, new Vector2(5, floors * 2));
            Box("Facade", root, new Vector3(0, height / 2, 0), new Vector3(6.5f, height, 14), facade);
            float streetSide = x > 0 ? -3.28f : 3.28f;
            Material stone = Mat("Architectural Trim", new Color(.76f,.75f,.68f));
            Material frame = Mat("Window Frame", new Color(.27f,.31f,.31f), .1f);
            Material glass = Mat("Window Glass", new Color(.14f,.27f,.33f), .25f, .52f);
            for (int floor = 1; floor < floors; floor++)
            {
                Box("Floor Cornice", root, new Vector3(streetSide, floor * 2.8f + .1f, 0), new Vector3(.2f,.12f,14.1f), stone);
                for (int column = 0; column < 5; column++)
                {
                    Vector3 position = new Vector3(streetSide, 1.6f + floor * 2.8f, -5.2f + column * 2.6f);
                    Box("Window Frame", root, position, new Vector3(.09f,1.8f,1.8f), frame);
                    Box("Window Glass", root, position + new Vector3(x > 0 ? -.055f : .055f,0,0), new Vector3(.035f,1.6f,1.6f), glass);
                }
            }
            Box("Shop Windows", root, new Vector3(streetSide,1.5f,0), new Vector3(.10f,2.4f,11.8f), glass);
            Color[] awnings = {new Color(.20f,.39f,.23f),new Color(.73f,.36f,.13f),new Color(.28f,.38f,.45f)};
            Box("Shop Awning", root, new Vector3(streetSide + (x > 0 ? -.55f : .55f),2.8f,0), new Vector3(1.2f,.2f,12), Mat("Awning " + index % 3, awnings[index % 3]));
            Box("Roof", root, new Vector3(0,height + .1f,0), new Vector3(6.8f,.2f,14.3f), Mat("Roof",new Color(.39f,.40f,.38f)));
            foreach (float side in new[] {-1f,1f})
                Box("Roof Parapet", root, new Vector3(side * 3.2f,height + .4f,0), new Vector3(.2f,.7f,14), stone);
            Box("Roof Equipment", root, new Vector3(1,height + .65f,1.6f), new Vector3(1.5f,1.1f,2.3f), Mat("Roof Equipment",new Color(.45f,.46f,.42f)));
            if (index % 2 == 0)
                Cylinder("Water Tank",root,new Vector3(-1,height + 1.1f,-3),new Vector3(1.4f,1.0f,1.4f),Mat("Tank",new Color(.46f,.49f,.48f),.35f));
        }

        private static void StreetLamp(Transform parent, float x, float z)
        {
            Transform root = new GameObject("Street Lamp").transform;
            root.SetParent(parent,false); root.localPosition = new Vector3(x,.16f,z);
            Material metal = Mat("Street Metal",new Color(.19f,.24f,.23f),.35f,.32f);
            Cylinder("Base",root,new Vector3(0,.15f,0),new Vector3(.35f,.15f,.35f),metal);
            Cylinder("Pole",root,new Vector3(0,2.15f,0),new Vector3(.11f,2.15f,.11f),metal);
            float inward = x > 0 ? -1 : 1;
            Box("Lamp Arm",root,new Vector3(inward * .48f,4.3f,0),new Vector3(1.1f,.11f,.11f),metal);
            Box("Lamp Hood",root,new Vector3(inward * .95f,4.2f,0),new Vector3(.48f,.16f,.32f),metal);
            Box("Lamp Lens",root,new Vector3(inward * .95f,4.105f,0),new Vector3(.36f,.025f,.25f),Mat("Lamp Lens",new Color(.94f,.88f,.68f)));
        }

        private static void Skyline(Transform parent)
        {
            Transform root = new GameObject("Distant Skyline").transform; root.SetParent(parent,false);
            Material[] tones = {Mat("Skyline Near",new Color(.45f,.52f,.54f)),Mat("Skyline Far",new Color(.53f,.60f,.62f))};
            for (int i = 0; i < 26; i++)
            {
                float x = -128 + i * 10.1f, height = 13 + (i * 17 % 27), z = 236 + i % 3 * 18;
                if (Mathf.Abs(x) < 9) x += 14;
                Box("Skyline Tower",root,new Vector3(x,height / 2,z),new Vector3(6 + i % 4,height,8 + i % 3 * 2),tones[i % 2]);
                Box("Tower Roof",root,new Vector3(x,height + .4f,z),new Vector3(3,.8f,4),tones[i % 2]);
            }
            // Three overlapping terrain ridges behind the buildings give atmospheric depth.
            for (int layer = 0; layer < 3; layer++)
            {
                var mesh = new Mesh {name = "Distant Ridge " + layer};
                var vertices = new List<Vector3>(); var triangles = new List<int>();
                for (int i = 0; i <= 24; i++)
                {
                    float x = -220 + i * 18.4f;
                    float height = 15 + layer * 5 + Mathf.Sin(i * .44f + layer * 1.9f) * 10 + Mathf.Sin(i * .91f) * 3;
                    vertices.Add(new Vector3(x,-2,316 + layer * 24)); vertices.Add(new Vector3(x,height,316 + layer * 24));
                    if (i < 24) {int a=i*2;triangles.AddRange(new[]{a,a+1,a+2,a+2,a+1,a+3});}
                }
                mesh.SetVertices(vertices); mesh.SetTriangles(triangles,0); mesh.RecalculateNormals(); mesh.RecalculateBounds();
                AssetDatabase.CreateAsset(mesh,StarterScene.Generated + "/Ridge" + layer + ".asset");
                GameObject ridge = new GameObject("Distant Ridge",typeof(MeshFilter),typeof(MeshRenderer)); ridge.transform.SetParent(root,false);
                ridge.GetComponent<MeshFilter>().sharedMesh=mesh;
                ridge.GetComponent<MeshRenderer>().sharedMaterial=Mat("Ridge " + layer,new Color(.43f + layer*.035f,.55f + layer*.025f,.58f + layer*.03f));
                ridge.GetComponent<MeshRenderer>().shadowCastingMode=UnityEngine.Rendering.ShadowCastingMode.Off;
            }
        }

        public static Report Validate(Camera camera, UniversalRenderPipelineAsset pipeline)
        {
            void Need(bool condition,string message) {if(!condition)throw new InvalidOperationException(message);}
            Renderer road = GameObject.Find("Road").GetComponent<Renderer>();
            Renderer ground = GameObject.Find("City Ground").GetComponent<Renderer>();
            Need(road.bounds.min.z < -60 && road.bounds.max.z > 350,"Boulevard still ends inside the city view.");
            int covered = 0; float oldAspect = camera.aspect;
            try
            {
                foreach (float aspect in new[] {9f/20,9f/16})
                {
                    camera.aspect = aspect;
                    foreach(float x in new[]{.02f,.5f,.98f}) foreach(float y in new[]{.02f,.5f,.86f})
                    {
                        Ray ray=camera.ViewportPointToRay(new Vector3(x,y,0));
                        Need(new Plane(Vector3.up,Vector3.zero).Raycast(ray,out float distance),"Visible street ray misses city ground.");
                        Vector3 point=ray.GetPoint(distance);Bounds b=ground.bounds;
                        Need(point.x>=b.min.x&&point.x<=b.max.x&&point.z>=b.min.z&&point.z<=b.max.z,"Camera exposes empty ground at a portrait edge.");
                        covered++;
                    }
                }
            } finally {camera.aspect=oldAspect;}
            Need(pipeline.supportsMainLightShadows&&pipeline.supportsSoftShadows&&pipeline.shadowDistance>=65,"City/contact shadows are disabled.");
            Need(RenderSettings.sun!=null&&RenderSettings.sun.color.r>RenderSettings.sun.color.b,"Warm afternoon sun missing.");
            Need(RenderSettings.fog&&RenderSettings.fogEndDistance<camera.farClipPlane,"Distant haze outside camera coverage.");
            var renderers=UnityEngine.Object.FindObjectsByType<MeshRenderer>(FindObjectsSortMode.None);
            var materials=new HashSet<Material>();int triangles=0,trees=0,buildings=0,lamps=0,towers=0;
            foreach(var filter in UnityEngine.Object.FindObjectsByType<MeshFilter>(FindObjectsSortMode.None)) triangles+=filter.sharedMesh.triangles.Length/3;
            foreach(var transform in UnityEngine.Object.FindObjectsByType<Transform>(FindObjectsSortMode.None))
            {
                if(transform.name=="City Tree")trees++;if(transform.name=="City Building")buildings++;if(transform.name=="Street Lamp")lamps++;if(transform.name=="Skyline Tower")towers++;
            }
            foreach(var renderer in renderers) {Need(renderer.sharedMaterial!=null&&renderer.sharedMaterial.shader.name=="Universal Render Pipeline/Lit","City material is missing or pink.");materials.Add(renderer.sharedMaterial);}
            Need(trees==32&&buildings==24&&lamps==20&&towers==26,"City scenery is incomplete.");
            Need(triangles<120000&&renderers.Length<2400&&materials.Count<48,"City geometry/material budget exceeded.");
            foreach(string name in new[]{"Asphalt","Pavers","Masonry"})
            {
                var texture=AssetDatabase.LoadAssetAtPath<Texture2D>(StarterScene.Generated+"/"+name+".asset");
                Need(texture!=null&&texture.mipmapCount>1&&texture.filterMode==FilterMode.Trilinear,"Surface loses mipmap filtering at distance.");
            }
            return new Report {roadLength=road.bounds.size.z,coveredPortraitRays=covered,trees=trees,buildings=buildings,streetLamps=lamps,skylineTowers=towers,triangles=triangles,renderers=renderers.Length,materials=materials.Count,softShadows=pipeline.supportsSoftShadows};
        }

        [Serializable] public sealed class Report
        {
            public float roadLength;
            public int coveredPortraitRays,trees,buildings,streetLamps,skylineTowers,triangles,renderers,materials;
            public bool softShadows;
        }
    }
}
