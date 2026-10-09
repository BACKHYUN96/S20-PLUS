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
            var views=new StreetSimulation.WalkerView[100];Mesh[] meshes=new Mesh[8];
            Material palette=Palette();for(int i=0;i<meshes.Length;i++)meshes[i]=PersonMesh(i);
            for(int i=0;i<views.Length;i++)
            {
                Transform person=new GameObject("Walker "+i).transform;person.SetParent(root.transform,false);
                var bones=new Transform[5];bones[0]=person;
                for(int b=1;b<5;b++) {bones[b]=new GameObject(b<3?"Arm":"Leg").transform;bones[b].SetParent(person,false);bones[b].localPosition=pivots[b]*(.92f+(i%8)*.022f);}
                var skin=person.gameObject.AddComponent<SkinnedMeshRenderer>();skin.sharedMesh=meshes[i%8];skin.sharedMaterial=palette;skin.bones=bones;skin.rootBone=person;
                skin.quality=SkinQuality.Bone1;skin.localBounds=new Bounds(new Vector3(0,.9f,0),new Vector3(1.2f,2.4f,1.3f));
                skin.lightProbeUsage=LightProbeUsage.Off;skin.reflectionProbeUsage=ReflectionProbeUsage.Off;
                views[i]=new StreetSimulation.WalkerView {root=person,limbs=new[]{bones[1],bones[2],bones[3],bones[4]}};
            }
            Material metal=StarterScene.Surface("Vehicle Trim",new Color(.025f,.031f,.038f),.05f,.2f);
            Material lens=StarterScene.Surface("Headlamp",new Color(.97f,.94f,.79f),.1f,.6f);
            var vehicleLights=new List<Renderer>();var walkLights=new List<Renderer>();
            foreach(float side in new[]{-1f,1f})foreach(float z in new[]{7f,13f})
            {
                Transform pole=new GameObject("Signal Pole").transform;pole.SetParent(root.transform,false);pole.position=new Vector3(side*10.08f,.16f,z);
                StarterScene.Box("Signal Mast",pole,new Vector3(0,2.35f,0),new Vector3(.13f,4.7f,.13f),metal);
                Transform head=new GameObject("Vehicle Signal").transform;head.SetParent(pole,false);head.localPosition=new Vector3(-side*2.15f,4.1f,0);
                head.localRotation=Quaternion.Euler(0,side<0?180:0,0);
                StarterScene.Box("Signal Arm",pole,new Vector3(-side*1.12f,4.35f,0),new Vector3(2.25f,.11f,.11f),metal);
                StarterScene.Box("Signal Housing",head,Vector3.zero,new Vector3(.38f,1.08f,.23f),metal);
                for(int n=0;n<3;n++)vehicleLights.Add(StarterScene.Box("Signal Lens",head,new Vector3(0,.32f-n*.32f,.13f),new Vector3(.23f,.23f,.025f),lens).GetComponent<Renderer>());
                Transform walk=new GameObject("Pedestrian Signal").transform;walk.SetParent(pole,false);walk.localPosition=new Vector3(0,2.5f,0);walk.localRotation=Quaternion.Euler(0,side<0?90:-90,0);
                StarterScene.Box("Walk Housing",walk,Vector3.zero,new Vector3(.34f,.66f,.22f),metal);
                for(int n=0;n<2;n++)walkLights.Add(StarterScene.Box("Walk Lens",walk,new Vector3(0,.16f-n*.32f,.12f),new Vector3(.21f,.21f,.026f),lens).GetComponent<Renderer>());
            }
            Material white=StarterScene.Surface("Road White",new Color(.88f,.88f,.82f));
            foreach(float side in new[]{-1f,1f})StarterScene.Box("Stop Line",root.transform,new Vector3(side*3.2f,.027f,StarterConfig.CrossingZ+side*-4),new Vector3(6.1f,.012f,.22f),white);
            var cars=UnityEngine.Object.FindObjectsByType<PrototypeDrive>(FindObjectsSortMode.None);
            Array.Sort(cars,(a,b)=>string.CompareOrdinal(a.name,b.name));
            controller.Configure(cars,views,vehicleLights.ToArray(),walkLights.ToArray());
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
        private static Mesh PersonMesh(int variant)
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
            Box(new Vector3(0,1.05f,0),new Vector3(.25f,.55f,.18f),0,1,variant);
            Box(new Vector3(0,1.49f,0),new Vector3(.26f,.30f,.25f),0,0,variant%4);
            Box(new Vector3(0,1.65f,-.015f),new Vector3(.27f,.07f,.26f),0,3,variant%4);
            foreach(float side in new[]{-1f,1f})
            {
                int arm=side<0?1:2,leg=side<0?3:4;
                Box(new Vector3(side*.14f,1.06f,0),new Vector3(.065f,.35f,.10f),arm,1,variant);
                Box(new Vector3(side*.14f,.84f,0),new Vector3(.07f,.11f,.10f),arm,0,variant%4);
                Box(new Vector3(side*.065f,.43f,0),new Vector3(.10f,.60f,.13f),leg,2,variant%4);
                Box(new Vector3(side*.065f,.06f,.045f),new Vector3(.11f,.12f,.22f),leg,3,3);
            }
            var mesh=new Mesh {name="Person "+variant};mesh.SetVertices(vertices);mesh.SetTriangles(triangles,0);mesh.SetUVs(0,uv);mesh.boneWeights=weights.ToArray();
            var bind=new Matrix4x4[5];for(int i=0;i<5;i++)bind[i]=Matrix4x4.TRS(pivots[i]*scale,Quaternion.identity,Vector3.one).inverse;mesh.bindposes=bind;
            mesh.RecalculateNormals();mesh.RecalculateBounds();AssetDatabase.CreateAsset(mesh,StarterScene.Generated+"/Person"+variant+".asset");return mesh;
        }
    }
}
