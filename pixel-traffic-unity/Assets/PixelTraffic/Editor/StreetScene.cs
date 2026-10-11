using System;
using System.Collections.Generic;
using UnityEditor;
using UnityEngine;
using UnityEngine.Rendering;

namespace PixelTraffic.UnityPrototype.Editor
{
    internal static class StreetScene
    {
        private static readonly Vector3[] pivots={Vector3.zero,new Vector3(-.14f,1.22f,0),new Vector3(.14f,1.22f,0),new Vector3(-.065f,.70f,0),new Vector3(.065f,.70f,0)};
        internal static void Create()
        {
            var root=new GameObject("Street Life");var controller=root.AddComponent<StreetSimulation>();
            var views=new StreetSimulation.WalkerView[100];var riders=new StreetSimulation.WalkerView[8];Mesh[] meshes=new Mesh[8];
            Mesh umbrellaMesh=UmbrellaMesh();
            Material palette=Palette();var coats=new Mesh[8];for(int i=0;i<meshes.Length;i++){meshes[i]=PersonMesh(i);coats[i]=PersonMesh(i,true);}
            for(int i=0;i<views.Length+riders.Length;i++)
            {
                Transform person=new GameObject("Walker "+i).transform;person.SetParent(root.transform,false);
                var bones=new Transform[5];bones[0]=person;
                for(int b=1;b<5;b++) {bones[b]=new GameObject(b<3?"Arm":"Leg").transform;bones[b].SetParent(person,false);bones[b].localPosition=pivots[b]*(.92f+(i%8)*.022f);}
                var skin=person.gameObject.AddComponent<SkinnedMeshRenderer>();skin.sharedMesh=meshes[i%8];skin.sharedMaterial=palette;skin.bones=bones;skin.rootBone=person;
                skin.quality=SkinQuality.Bone1;skin.localBounds=new Bounds(new Vector3(0,.9f,0),new Vector3(1.2f,2.4f,1.3f));
                skin.lightProbeUsage=LightProbeUsage.Off;skin.reflectionProbeUsage=ReflectionProbeUsage.Off;
                Transform umbrella=new GameObject("Umbrella").transform;umbrella.SetParent(person,false);umbrella.localPosition=new Vector3(.14f,1.0f,.32f);
                umbrella.gameObject.AddComponent<MeshFilter>().sharedMesh=umbrellaMesh;
                var umbrellaRenderer=umbrella.gameObject.AddComponent<MeshRenderer>();umbrellaRenderer.sharedMaterial=palette;
                umbrellaRenderer.lightProbeUsage=LightProbeUsage.Off;umbrellaRenderer.reflectionProbeUsage=ReflectionProbeUsage.Off;
                var tint=new MaterialPropertyBlock();Color[] umbrellaColors={new Color(.45f,.65f,1),new Color(1,.4f,.3f),new Color(1,.83f,.4f),new Color(.5f,.83f,.6f)};
                tint.SetColor("_BaseColor",umbrellaColors[i%4]);umbrellaRenderer.SetPropertyBlock(tint);umbrella.gameObject.SetActive(false);
                var view=new StreetSimulation.WalkerView {root=person,limbs=new[]{bones[1],bones[2],bones[3],bones[4]},umbrella=umbrella,body=skin,dryMesh=meshes[i%8],coatMesh=coats[i%8],wearsCoat=i%4==0};if(i<views.Length)views[i]=view;else {person.name="Bus Passenger "+(i-views.Length);riders[i-views.Length]=view;}
            }
            Material metal=StarterScene.Surface("Vehicle Trim",new Color(.025f,.031f,.038f),.05f,.2f);
            Material lens=StarterScene.Surface("Headlamp",new Color(.97f,.94f,.79f),.1f,.6f);
            var vehicleLights=new List<Renderer>();var walkLights=new List<Renderer>();
            float armReach=StarterConfig.CurbFurnitureX-3.38f;
            foreach(float side in new[]{-1f,1f})
            {
                float z=StarterConfig.CrossingZ+side*3;
                Transform pole=new GameObject("Signal Pole").transform;pole.SetParent(root.transform,false);pole.position=new Vector3(side*StarterConfig.CurbFurnitureX,.16f,z);
                StarterScene.Box("Signal Mast",pole,new Vector3(0,2.9f,0),new Vector3(.18f,5.8f,.18f),metal);
                // One overhead head per travel direction, on the far side of the crossing.
                // One combined vehicle/pedestrian mast on each curb; no extra pedestrian-only mast.
                if(Mathf.Abs(z-(StarterConfig.CrossingZ+side*3))<.001f)
                {
                    Transform head=new GameObject("Vehicle Signal").transform;head.SetParent(pole,false);head.localPosition=new Vector3(-side*armReach,5.45f,0);
                    // Each face looks upstream, towards the drivers who must obey it.
                    head.localRotation=Quaternion.Euler(0,side<0?0:180,0);
                    StarterScene.Box("Signal Arm",pole,new Vector3(-side*armReach/2,5.78f,0),new Vector3(armReach,.15f,.15f),metal);
                    StarterScene.Box("Signal Housing",head,Vector3.zero,new Vector3(1.82f,.50f,.26f),metal);
                    for(int n=0;n<4;n++)
                    {
                        // Viewed from the illuminated face: red left, straight green right.
                        var center=new Vector3(.66f-n*.44f,0,.141f);
                        vehicleLights.Add(SignalPart("Signal Lens",head,CircleLens(),center,lens));
                        SignalPart("Signal Hood",head,Hood(.40f,.38f),new Vector3(center.x,0,.135f),metal);
                    }
                }
                Transform walk=new GameObject("Pedestrian Signal").transform;walk.SetParent(pole,false);walk.localPosition=new Vector3(0,2.45f,0);walk.localRotation=Quaternion.Euler(0,side<0?90:-90,0);
                StarterScene.Box("Walk Housing",walk,Vector3.zero,new Vector3(.37f,1.16f,.25f),metal);
                for(int n=0;n<3;n++)
                {
                    float y=.38f-n*.38f;
                    SignalPart("Walk Hood",walk,Hood(.34f,.34f),new Vector3(0,y,.13f),metal);
                    if(n!=1)walkLights.Add(SignalPart("Walk Lens",walk,PersonIcon(n==2),new Vector3(0,y,.132f),lens));
                }
            }
            Material white=StarterScene.Surface("Road White",new Color(.88f,.88f,.82f));
            foreach(float side in new[]{-1f,1f})StarterScene.Box("Stop Line",root.transform,new Vector3(side*3.2f,.027f,StarterConfig.CrossingZ+side*-4),new Vector3(6.1f,.012f,.22f),white);
            var cars=UnityEngine.Object.FindObjectsByType<PrototypeDrive>(FindObjectsSortMode.None);
            Array.Sort(cars,(a,b)=>string.CompareOrdinal(a.name,b.name));
            controller.Configure(cars,views,vehicleLights.ToArray(),walkLights.ToArray());
            BusStopScene.Create();controller.ConfigureRiders(riders);
        }
        private static Renderer SignalPart(string name,Transform parent,Mesh mesh,Vector3 center,Material material)
        {
            var part=new GameObject(name);part.transform.SetParent(parent,false);part.transform.localPosition=center;
            part.AddComponent<MeshFilter>().sharedMesh=mesh;var renderer=part.AddComponent<MeshRenderer>();renderer.sharedMaterial=material;return renderer;
        }
        private static Mesh SignalMesh(string name,List<Vector3> vertices,List<int> triangles)
        {
            string path=StarterScene.Generated+"/"+name+".asset";var existing=AssetDatabase.LoadAssetAtPath<Mesh>(path);if(existing!=null)return existing;
            var mesh=new Mesh{name=name};mesh.SetVertices(vertices);mesh.SetTriangles(triangles,0);mesh.RecalculateNormals();mesh.RecalculateBounds();AssetDatabase.CreateAsset(mesh,path);return mesh;
        }
        private static Mesh CircleLens()
        {
            var v=new List<Vector3>{Vector3.zero};var t=new List<int>();
            for(int i=0;i<12;i++){float a=i*Mathf.PI/6;v.Add(new Vector3(Mathf.Cos(a)*.155f,Mathf.Sin(a)*.155f,0));t.AddRange(new[]{0,i+1,(i+1)%12+1});}
            return SignalMesh("KoreanRoundLens",v,t);
        }
        private static Mesh Hood(float width,float height)
        {
            var v=new List<Vector3>();var t=new List<int>();float x=width/2,y=height/2;
            void Face(Vector3 a,Vector3 b,Vector3 c,Vector3 d){int k=v.Count;v.AddRange(new[]{a,b,c,d});t.AddRange(new[]{k,k+1,k+2,k,k+2,k+3});}
            Face(new Vector3(-x,y,0),new Vector3(-x,y,.23f),new Vector3(x,y,.23f),new Vector3(x,y,0));
            Face(new Vector3(-x,-y,0),new Vector3(-x,-y,.17f),new Vector3(-x,y,.23f),new Vector3(-x,y,0));
            Face(new Vector3(x,y,0),new Vector3(x,y,.23f),new Vector3(x,-y,.17f),new Vector3(x,-y,0));
            return SignalMesh(width>.35f?"VehicleSignalHood":"PedestrianSignalHood",v,t);
        }
        private static Mesh PersonIcon(bool walking)
        {
            var v=new List<Vector3>();var t=new List<int>();
            void Bar(Vector2 a,Vector2 b,float width)
            {
                Vector2 n=new Vector2(-(b-a).y,(b-a).x).normalized*width/2;int k=v.Count;
                foreach(var p in new[]{a-n,b-n,b+n,a+n})v.Add(new Vector3(p.x,p.y,0));t.AddRange(new[]{k,k+1,k+2,k,k+2,k+3});
            }
            Bar(new Vector2(0,.078f),new Vector2(0,.11f),.054f);
            Bar(new Vector2(0,.058f),new Vector2(0,-.025f),.047f);
            Bar(new Vector2(0,.046f),new Vector2(walking?-.07f:-.052f,walking?.02f:-.025f),.025f);
            Bar(new Vector2(0,.046f),new Vector2(walking?.072f:.052f,walking?.072f:-.025f),.025f);
            Bar(new Vector2(-.012f,-.021f),new Vector2(walking?-.079f:-.026f,-.115f),.030f);
            Bar(new Vector2(.012f,-.021f),new Vector2(walking?.073f:.026f,-.115f),.030f);
            return SignalMesh(walking?"KoreanWalkIcon":"KoreanStopIcon",v,t);
        }
        private static Mesh UmbrellaMesh()
        {
            var vertices=new List<Vector3>();var triangles=new List<int>();var uv=new List<Vector2>();
            void Triangle(Vector3 a,Vector3 b,Vector3 c,int row)
            {
                int k=vertices.Count;vertices.AddRange(new[]{a,b,c});triangles.AddRange(new[]{k,k+1,k+2});
                uv.AddRange(new[]{new Vector2(.31f,(row+.5f)/4),new Vector2(.31f,(row+.5f)/4),new Vector2(.31f,(row+.5f)/4)});
            }
            for(int i=0;i<8;i++)
            {
                float a=i*Mathf.PI/4,b=(i+1)*Mathf.PI/4;
                Vector3 p=new Vector3(Mathf.Sin(a)*.62f,.88f,Mathf.Cos(a)*.62f),q=new Vector3(Mathf.Sin(b)*.62f,.88f,Mathf.Cos(b)*.62f),top=new Vector3(0,1.08f,0);
                Triangle(top,p,q,1);Triangle(top,q,p,1);
            }
            for(int i=0;i<4;i++)
            {
                float a=i*Mathf.PI/2,b=(i+1)*Mathf.PI/2;
                Vector3 p=new Vector3(Mathf.Sin(a)*.014f,0,Mathf.Cos(a)*.014f),q=new Vector3(Mathf.Sin(b)*.014f,0,Mathf.Cos(b)*.014f);
                Triangle(p,q,q+Vector3.up*1.08f,3);Triangle(p,q+Vector3.up*1.08f,p+Vector3.up*1.08f,3);
            }
            var mesh=new Mesh {name="Shared Umbrella"};mesh.SetVertices(vertices);mesh.SetTriangles(triangles,0);mesh.SetUVs(0,uv);mesh.RecalculateNormals();mesh.RecalculateBounds();
            AssetDatabase.CreateAsset(mesh,StarterScene.Generated+"/Umbrella.asset");return mesh;
        }
        private static Material Palette()
        {
            Color[][] colors={
                new[]{new Color(.78f,.53f,.36f),new Color(.94f,.74f,.56f),new Color(.42f,.26f,.17f),new Color(.67f,.44f,.30f)},
                new[]{new Color(.16f,.30f,.53f),new Color(.63f,.12f,.09f),new Color(.81f,.75f,.59f),new Color(.18f,.36f,.22f),new Color(.43f,.20f,.43f),new Color(.81f,.45f,.10f),new Color(.10f,.17f,.22f),new Color(.65f,.65f,.69f)},
                new[]{new Color(.055f,.08f,.12f),new Color(.21f,.25f,.32f),new Color(.36f,.27f,.18f),new Color(.09f,.13f,.19f)},
                new[]{new Color(.055f,.04f,.025f),new Color(.15f,.09f,.045f),new Color(.28f,.21f,.13f),new Color(.05f,.055f,.06f)}};
            var texture=new Texture2D(64,32,TextureFormat.RGB24,true) {name="People Palette",filterMode=FilterMode.Point,wrapMode=TextureWrapMode.Clamp};
            var pixels=new Color[64*32];for(int y=0;y<32;y++)for(int x=0;x<64;x++)pixels[y*64+x]=colors[y/8][(x/8)%colors[y/8].Length];
            texture.SetPixels(pixels);texture.Apply(true,false);AssetDatabase.CreateAsset(texture,StarterScene.Generated+"/PeoplePalette.asset");
            var material=StarterScene.Surface("People Palette",Color.white,0,.2f);material.SetTexture("_BaseMap",texture);EditorUtility.SetDirty(material);return material;
        }
        private static Mesh PersonMesh(int variant,bool coat=false)
        {
            float scale=.92f+variant*.022f;
            var vertices=new List<Vector3>();var triangles=new List<int>();var uv=new List<Vector2>();var weights=new List<BoneWeight>();
            void Box(Vector3 center,Vector3 size,int bone,int row,int color)
            {
                Vector3 p(int x,int y,int z)=>Vector3.Scale(new Vector3(x,y,z)*.5f,size)+center;
                void Face(Vector3 a,Vector3 b,Vector3 c,Vector3 d)
                {
                    int start=vertices.Count;foreach(var point in new[]{a,b,c,d}){vertices.Add(point*scale);uv.Add(new Vector2((color%8+.5f)/8,(row+.5f)/4));weights.Add(new BoneWeight {boneIndex0=bone,weight0=1});}
                    triangles.AddRange(new[]{start,start+1,start+2,start,start+2,start+3});
                }
                Face(p(-1,-1,1),p(1,-1,1),p(1,1,1),p(-1,1,1));Face(p(1,-1,-1),p(-1,-1,-1),p(-1,1,-1),p(1,1,-1));
                Face(p(1,-1,1),p(1,-1,-1),p(1,1,-1),p(1,1,1));Face(p(-1,-1,-1),p(-1,-1,1),p(-1,1,1),p(-1,1,-1));
                Face(p(-1,1,1),p(1,1,1),p(1,1,-1),p(-1,1,-1));Face(p(-1,-1,-1),p(1,-1,-1),p(1,-1,1),p(-1,-1,1));
            }
            int fabric=coat?(variant<4?5:0):variant;
            Box(new Vector3(0,coat?.98f:1.05f,0),new Vector3(coat?.31f:.25f,coat?.72f:.55f,coat?.23f:.18f),0,1,fabric);
            Box(new Vector3(0,1.49f,0),new Vector3(.26f,.30f,.25f),0,0,variant%4);
            Box(new Vector3(0,1.65f,-.015f),new Vector3(coat?.33f:.27f,.07f,coat?.30f:.26f),0,coat?1:3,coat?fabric:variant%4);
            if(coat)
            {
                void HoodPanel(Vector3 a,Vector3 b,Vector3 c,Vector3 d)
                {
                    int k=vertices.Count;foreach(var point in new[]{a,b,c,d}){vertices.Add(point*scale);uv.Add(new Vector2((fabric+.5f)/8,1.5f/4));weights.Add(new BoneWeight{boneIndex0=0,weight0=1});}
                    triangles.AddRange(new[]{k,k+1,k+2,k,k+2,k+3});
                }
                HoodPanel(new Vector3(-.165f,1.34f,-.16f),new Vector3(-.165f,1.34f,.135f),new Vector3(-.165f,1.63f,.135f),new Vector3(-.165f,1.63f,-.16f));
                HoodPanel(new Vector3(.165f,1.34f,.135f),new Vector3(.165f,1.34f,-.16f),new Vector3(.165f,1.63f,-.16f),new Vector3(.165f,1.63f,.135f));
                HoodPanel(new Vector3(.165f,1.34f,-.16f),new Vector3(-.165f,1.34f,-.16f),new Vector3(-.165f,1.63f,-.16f),new Vector3(.165f,1.63f,-.16f));
            }
            foreach(float side in new[]{-1f,1f})
            {
                int arm=side<0?1:2,leg=side<0?3:4;
                Box(new Vector3(side*.14f,1.06f,0),new Vector3(coat?.09f:.065f,.35f,coat?.13f:.10f),arm,1,fabric);
                Box(new Vector3(side*.14f,.84f,0),new Vector3(.07f,.11f,.10f),arm,0,variant%4);
                Box(new Vector3(side*.065f,.43f,0),new Vector3(.10f,.60f,.13f),leg,2,variant%4);
                Box(new Vector3(side*.065f,.06f,.045f),new Vector3(.11f,.12f,.22f),leg,3,3);
            }
            var mesh=new Mesh {name=(coat?"Raincoat ":"Person ")+variant};mesh.SetVertices(vertices);mesh.SetTriangles(triangles,0);mesh.SetUVs(0,uv);mesh.boneWeights=weights.ToArray();
            var bind=new Matrix4x4[5];for(int i=0;i<5;i++)bind[i]=Matrix4x4.TRS(pivots[i]*scale,Quaternion.identity,Vector3.one).inverse;mesh.bindposes=bind;
            mesh.RecalculateNormals();mesh.RecalculateBounds();AssetDatabase.CreateAsset(mesh,StarterScene.Generated+"/"+(coat?"Raincoat":"Person")+variant+".asset");return mesh;
        }
    }
}
