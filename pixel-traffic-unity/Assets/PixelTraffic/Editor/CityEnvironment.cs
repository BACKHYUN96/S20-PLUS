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
            {
            var obj=new GameObject(name,typeof(MeshFilter),typeof(MeshRenderer));obj.transform.SetParent(parent,false);obj.transform.localPosition=position;obj.transform.localScale=scale;
            obj.GetComponent<MeshFilter>().sharedMesh=DistantScene.StreetCylinder();obj.GetComponent<MeshRenderer>().sharedMaterial=material;return obj;
        }

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
            float crossingStart = StarterConfig.CrossingZ - 2, crossingEnd = StarterConfig.CrossingZ + 2;
            void Marking(string name, float x, float start, float end, float width, Material material)
            {
                // Keep road markings outside the zebra crossing, including gaps between its stripes.
                void Segment(float a, float b)
                {
                    if (b > a) Box(name, road, new Vector3(x, .012f, (a + b) * .5f), new Vector3(width, .018f, b - a), material);
                }
                if (start < crossingStart) Segment(start, Mathf.Min(end, crossingStart));
                if (end > crossingEnd) Segment(Mathf.Max(start, crossingEnd), end);
            }
            foreach (float x in new[] { -.14f, .14f })
                Marking("Centre Line", x, RoadStart, RoadEnd, .12f, yellow);
            foreach (float x in new[] { -3.2f, 3.2f })
                for (float z = RoadStart + 3; z < RoadEnd - 3; z += 9)
                    Marking("Lane Dash", x, z - 1.7f, z + 1.7f, .14f, white);
            for (int i = 0; i < 12; i++)
                Box("Zebra Stripe", road, new Vector3(-5.9f + i * 1.07f, .022f, StarterConfig.CrossingZ), new Vector3(.64f, .018f, 4), white);
            for (int i = 0; i < 4; i++)
                Cylinder("Manhole", road, new Vector3(i % 2 == 0 ? -1.6f : 4.8f, .005f, 24 + i * 34), new Vector3(.7f, .004f, .7f), Mat("Iron", new Color(.17f, .18f, .18f), .15f));

            for (int i = 0; i < 16; i++)
                foreach (float side in new[] { -1f, 1f }) Tree(scenery, FoliageScene.Canopy(i%3), side * 8.6f, -24 + i * 13, i);
            for (int i = 0; i < 12; i++)
                foreach (float side in new[] { -1f, 1f }) Building(scenery, side * 14.1f, -22 + i * 17, i);
            for (int i = 0; i < 10; i++)
                foreach (float side in new[] { -1f, 1f }) StreetLamp(scenery, side * 7.1f, -15 + i * 20);
            DistantScene.Create(scenery);
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
                            shade = 1;
                        pixels[y * size + x] = style==2 ? ArchitectureScene.MasonryPixel(x,y,size) : new Color(shade, shade, shade);
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
            leaves.GetComponent<MeshRenderer>().sharedMaterial = FoliageScene.Leaves(index%3);
            Material planter = Mat("Planter Stone", new Color(.57f, .54f, .44f));
            // Keep planted beds aligned with the sidewalk while the tree canopy retains its varied yaw.
            Quaternion bedRotation=Quaternion.Inverse(root.localRotation);
            Box("Tree Bed", root, new Vector3(0, .1f, 0), new Vector3(2.2f, .2f, 2.2f), planter).transform.localRotation=bedRotation;
            Box("Soil", root, new Vector3(0, .205f, 0), new Vector3(1.94f, .015f, 1.94f), Mat("Soil", new Color(.23f, .20f, .12f))).transform.localRotation=bedRotation;
        }

        private static void Building(Transform parent, float x, float z, int index)
        {
            int floors = 4 + index % 3; float height = floors * 2.8f + 1.2f;
            Transform root = new GameObject("City Building").transform;
            root.SetParent(parent, false); root.localPosition = new Vector3(x, .16f, z);
            Color[] colors = {new Color(.80f,.79f,.70f),new Color(.75f,.77f,.70f),new Color(.83f,.78f,.69f)};
            Material facade = Mat("Facade " + index % 3, colors[index % 3]);
            Texture(facade, "Masonry", 2, new Vector2(2, floors*.65f));
            Box("Facade", root, new Vector3(0, height / 2, 0), new Vector3(6.5f, height, 14), facade);
            float streetSide = x > 0 ? -3.28f : 3.28f;
            Material stone = Mat("Architectural Trim", new Color(.76f,.75f,.68f));
            Material frame = Mat("Window Frame", new Color(.27f,.31f,.31f), .1f);
            var windows=FrontageScene.BeginWindows(root,index,streetSide,frame);
            for (int floor = 1; floor < floors; floor++)
            {
                Box("Floor Cornice", root, new Vector3(streetSide, floor * 2.8f + .1f, 0), new Vector3(.2f,.12f,14.1f), stone);
                for (int column = 0; column < 5; column++)
                {
                    Vector3 position = new Vector3(streetSide, 1.6f + floor * 2.8f, -5.2f + column * 2.6f);
                    windows.Add(position,floor,column);
                }
            }
            windows.Save();FrontageScene.Shop(root,index,streetSide,frame);
            Color[] awnings = {new Color(.34f,.23f,.15f),new Color(.17f,.40f,.25f),new Color(.25f,.34f,.44f)};
            Box("Shop Awning", root, new Vector3(streetSide + (x > 0 ? -.55f : .55f),2.8f,0), new Vector3(1.2f,.2f,12), Mat("Awning " + index % 3, awnings[index % 3]));
            Box("Roof", root, new Vector3(0,height + .1f,0), new Vector3(6.8f,.2f,14.3f), Mat("Roof",new Color(.39f,.40f,.38f)));
            foreach (float side in new[] {-1f,1f})
                Box("Roof Parapet", root, new Vector3(side * 3.2f,height + .4f,0), new Vector3(.2f,.7f,14), stone);
            Box("Roof Equipment", root, new Vector3(1,height + .65f,1.6f), new Vector3(1.5f,1.1f,2.3f), Mat("Roof Equipment",new Color(.45f,.46f,.42f)));
            if (index % 2 == 0)
                Cylinder("Water Tank",root,new Vector3(-1,height + 1.1f,-3),new Vector3(1.4f,1.0f,1.4f),Mat("Tank",new Color(.46f,.49f,.48f),.35f));
            ArchitectureScene.Details(root,index,height,streetSide,stone,frame,facade);
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

        public static Report Validate(Camera camera, UniversalRenderPipelineAsset pipeline)
        {
            void Need(bool condition,string message) {if(!condition)throw new InvalidOperationException(message);}
            Renderer road = GameObject.Find("Road").GetComponent<Renderer>();
            Renderer ground = GameObject.Find("City Ground").GetComponent<Renderer>();
            Need(road.bounds.min.z < -60 && road.bounds.max.z > 350,"Boulevard still ends inside the city view.");
            int covered = 0,skyRays=0;float vanishingX=0,horizonY=0,crossingY=0,foregroundX=0; float oldAspect = camera.aspect;
            try
            {
                foreach (float aspect in new[] {9f/20,9f/16})
                {
                    camera.aspect = aspect;
                    // Composition oracle: world road direction and an independently fixed crossing.
                    Vector3 vanishing=camera.WorldToViewportPoint(new Vector3(0,0,100000));
                    Vector3 crossingPoint=camera.WorldToViewportPoint(new Vector3(0,0,10));
                    Need(vanishing.x>.60f&&vanishing.x<.73f&&vanishing.y>.60f&&vanishing.y<.70f,"Road vanishing point lacks reference offset/sky room.");
                    Need(crossingPoint.x>.53f&&crossingPoint.x<.68f&&crossingPoint.y>.22f&&crossingPoint.y<.36f,"Crossing leaves the lower-middle reference composition.");
                    Vector3 foreground=camera.WorldToViewportPoint(new Vector3(0,0,-11));
                    Need(foreground.x>.47f&&foreground.x<.59f,"Foreground centre line tilts too far away from reference centre.");foregroundX=foreground.x;
                    vanishingX=vanishing.x;horizonY=vanishing.y;crossingY=crossingPoint.y;
                    foreach(float x in new[]{.02f,.5f,.98f})
                    {
                        Ray sky=camera.ViewportPointToRay(new Vector3(x,.9f,0));
                        Need(sky.direction.y>0&&!new Plane(Vector3.up,Vector3.zero).Raycast(sky,out _),"Upper portrait band still points at the road rather than the distant background/sky.");skyRays++;
                    }
                    foreach(float x in new[]{.02f,.5f,.98f}) foreach(float y in new[]{.02f,.26f,.55f})
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
            var renderers=UnityEngine.Object.FindObjectsByType<Renderer>(FindObjectsSortMode.None);
            Bounds crossing = default; int stripes = 0, markings = 0;
            foreach (var renderer in renderers)
                if (renderer.name == "Zebra Stripe") { if (stripes++ == 0) crossing = renderer.bounds; else crossing.Encapsulate(renderer.bounds); }
            Need(stripes == 12, "Zebra crossing stripes missing.");
            foreach (var renderer in renderers)
                if (renderer.name == "Centre Line" || renderer.name == "Lane Dash")
                {
                    Bounds bounds = renderer.bounds;
                    Need(bounds.max.z <= crossing.min.z + .0001f || bounds.min.z >= crossing.max.z - .0001f, "Road marking overlaps the zebra crossing: " + renderer.name);
                    markings++;
                }
            var materials=new HashSet<Material>();int triangles=0,trees=0,buildings=0,lamps=0,towers=0;
            foreach(var filter in UnityEngine.Object.FindObjectsByType<MeshFilter>(FindObjectsSortMode.None)) triangles+=filter.sharedMesh.triangles.Length/3;
            foreach(var skin in UnityEngine.Object.FindObjectsByType<SkinnedMeshRenderer>(FindObjectsSortMode.None)) triangles+=skin.sharedMesh.triangles.Length/3;
            foreach(var transform in UnityEngine.Object.FindObjectsByType<Transform>(FindObjectsSortMode.None))
            {
                if(transform.name=="City Tree")trees++;if(transform.name=="City Building")buildings++;if(transform.name=="Street Lamp")lamps++;if(transform.name=="Skyline Tower")towers++;
            }
            foreach(var renderer in renderers) {Need(renderer.sharedMaterial!=null&&(renderer.sharedMaterial.shader.name=="Universal Render Pipeline/Lit" || renderer.sharedMaterial.shader.name=="PixelTraffic/Atmosphere" || renderer.sharedMaterial.shader.name=="PixelTraffic/Distant"),"City material is missing or pink.");materials.Add(renderer.sharedMaterial);}
            Need(trees==32&&buildings==24&&lamps==20&&towers==26,"City scenery is incomplete.");
            Need(triangles<120000&&renderers.Length<2400&&materials.Count<48,"City geometry/material budget exceeded.");
            foreach(string name in new[]{"Asphalt","Pavers","Masonry"})
            {
                var texture=AssetDatabase.LoadAssetAtPath<Texture2D>(StarterScene.Generated+"/"+name+".asset");
                Need(texture!=null&&texture.mipmapCount>1&&texture.filterMode==FilterMode.Trilinear,"Surface loses mipmap filtering at distance.");
            }
            return new Report {roadLength=road.bounds.size.z,coveredPortraitRays=covered,skyPortraitRays=skyRays,roadVanishingX=vanishingX,horizonY=horizonY,crossingViewportY=crossingY,foregroundCentreX=foregroundX,trees=trees,buildings=buildings,streetLamps=lamps,skylineTowers=towers,triangles=triangles,renderers=renderers.Length,materials=materials.Count,softShadows=pipeline.supportsSoftShadows,crossingStripes=stripes,roadMarkingsOutsideCrossing=markings};
        }

        [Serializable] public sealed class Report
        {
            public float roadLength,roadVanishingX,horizonY,crossingViewportY,foregroundCentreX;
            public int coveredPortraitRays,skyPortraitRays,trees,buildings,streetLamps,skylineTowers,triangles,renderers,materials;
            public int crossingStripes, roadMarkingsOutsideCrossing;
            public bool softShadows;
        }
    }
}
