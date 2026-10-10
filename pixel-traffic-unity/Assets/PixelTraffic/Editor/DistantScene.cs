using System;
using System.Collections.Generic;
using UnityEditor;
using UnityEngine;
using UnityEngine.Rendering;

namespace PixelTraffic.UnityPrototype.Editor
{
    internal static class DistantScene
    {
        private sealed class Geometry
        {
            internal readonly List<Vector3> vertices=new List<Vector3>();
            private readonly List<int> indices=new List<int>();
            private readonly List<Vector2> uv=new List<Vector2>();
            private readonly List<Color> colors=new List<Color>();
            internal void Quad(Vector3 a,Vector3 b,Vector3 c,Vector3 d,Color data)
            {
                int n=vertices.Count;vertices.AddRange(new[]{a,b,c,d});
                uv.AddRange(new[]{new Vector2(0,0),new Vector2(0,1),new Vector2(1,1),new Vector2(1,0)});
                for(int i=0;i<4;i++)colors.Add(data);
                indices.AddRange(new[]{n,n+1,n+2,n,n+2,n+3});
            }
            internal void Box(Vector3 centre,Vector3 size,Color data)
            {
                Vector3 a=centre-size*.5f,b=centre+size*.5f;
                Quad(new Vector3(a.x,a.y,a.z),new Vector3(a.x,b.y,a.z),new Vector3(b.x,b.y,a.z),new Vector3(b.x,a.y,a.z),data);
                Quad(new Vector3(b.x,a.y,b.z),new Vector3(b.x,b.y,b.z),new Vector3(a.x,b.y,b.z),new Vector3(a.x,a.y,b.z),data);
                Quad(new Vector3(a.x,a.y,b.z),new Vector3(a.x,b.y,b.z),new Vector3(a.x,b.y,a.z),new Vector3(a.x,a.y,a.z),data);
                Quad(new Vector3(b.x,a.y,a.z),new Vector3(b.x,b.y,a.z),new Vector3(b.x,b.y,b.z),new Vector3(b.x,a.y,b.z),data);
                Quad(new Vector3(a.x,b.y,a.z),new Vector3(a.x,b.y,b.z),new Vector3(b.x,b.y,b.z),new Vector3(b.x,b.y,a.z),data);
                Quad(new Vector3(a.x,a.y,b.z),new Vector3(a.x,a.y,a.z),new Vector3(b.x,a.y,a.z),new Vector3(b.x,a.y,b.z),data);
            }
            internal Renderer Save(string name,Transform parent,Material material)
            {
                var mesh=new Mesh{name=name};mesh.SetVertices(vertices);mesh.SetTriangles(indices,0);mesh.SetUVs(0,uv);mesh.SetColors(colors);mesh.RecalculateNormals();mesh.RecalculateBounds();
                AssetDatabase.CreateAsset(mesh,StarterScene.Generated+"/"+name.Replace(" ","")+".asset");
                var obj=new GameObject(name,typeof(MeshFilter),typeof(MeshRenderer));obj.transform.SetParent(parent,false);
                obj.GetComponent<MeshFilter>().sharedMesh=mesh;var renderer=obj.GetComponent<MeshRenderer>();renderer.sharedMaterial=material;renderer.shadowCastingMode=ShadowCastingMode.Off;renderer.receiveShadows=false;return renderer;
            }
        }
        // Shared authored layout also locates reflection ribbons; the shader never invents
        // unrelated night lights on a separate screen-space grid.
        internal const int TowerCount=34;
        internal const float BridgeZ=362;
        internal const float BridgeDeckY=24.8f;
        internal static float TowerX(int i)=>-165+i*10;
        internal static float TowerHeight(int i)=>i==20?66:12+(i*19%31);
        internal static float RidgeHeight(float x,int layer)
        {
            float centre=layer==0?-42:layer==1?42:-83;
            float peak=layer==0?27:layer==1?47:62;
            return 31+layer*5+peak*Mathf.Exp(-Mathf.Pow((x-centre)/(layer==0?62:88),2))
                +Mathf.Sin(x*.048f+layer*2)*5+Mathf.Sin(x*.13f+layer)*2.2f;
        }
        internal static void Create(Transform scenery)
        {
            var shader=Shader.Find("PixelTraffic/Distant");if(shader==null)throw new InvalidOperationException("Distant shader missing.");
            var material=new Material(shader){name="Distant Landscape"};AssetDatabase.CreateAsset(material,StarterScene.Generated+"/DistantLandscape.mat");
            Transform root=new GameObject("Distant Skyline").transform;root.SetParent(scenery,false);
            var views=new List<Renderer>();var sky=new Geometry();
            sky.Quad(new Vector3(-330,-35,468),new Vector3(-330,420,468),new Vector3(330,420,468),new Vector3(330,-35,468),new Color(0,0,0,1));
            views.Add(sky.Save("Reference Sky",root,material));
            for(int layer=0;layer<3;layer++)
            {
                var mountain=new Geometry();float z=425+layer*18;
                for(int i=0;i<64;i++)
                {
                    float x=-240+i*7.5f,x2=x+7.5f;
                    mountain.Quad(new Vector3(x,14,z),new Vector3(x,RidgeHeight(x,layer),z),new Vector3(x2,RidgeHeight(x2,layer),z),new Vector3(x2,14,z),new Color(1,layer*.43f,layer*.7f,1));
                }
                views.Add(mountain.Save("Distant Ridge "+layer,root,material));
            }
            var city=new Geometry();
            for(int i=0;i<TowerCount;i++)
            {
                float x=TowerX(i),z=402+i%3*6,height=TowerHeight(i),seed=i/(float)TowerCount;
                var marker=new GameObject("Skyline Tower").transform;marker.SetParent(root,false);marker.localPosition=new Vector3(x,20,z);
                city.Box(new Vector3(x,20+height*.5f,z),new Vector3(5+i%4,height,7+i%3*2),new Color(2,height/2.4f,seed,1));
                city.Box(new Vector3(x,20+height+.6f,z),new Vector3(3,1.2f,4),new Color(4,0,0,1));
                if(i==20)
                {
                    city.Box(new Vector3(x,20+height+4,z),new Vector3(2.3f,7,3),new Color(2,3,seed,1));
                    city.Box(new Vector3(x,20+height+10,z),new Vector3(.35f,8,.35f),new Color(4,0,0,1));
                }
            }
            // A small summit landmark, using the same opaque material and no realtime light.
            float summit=RidgeHeight(-42,0);
            city.Box(new Vector3(-42,summit+7,422),new Vector3(.75f,14,.75f),new Color(4,0,0,1));
            city.Box(new Vector3(-42,summit+11,422),new Vector3(4.1f,1.3f,3.2f),new Color(4,0,0,1));
            city.Box(new Vector3(-42,summit+19,422),new Vector3(.22f,9,.22f),new Color(4,0,0,1));
            views.Add(city.Save("Opposite River City",root,material));
            var water=new Geometry();
            // A depth-spanning, gently raised stylized river, rather than an upright
            // sheet in front of the bridge. Its near/far banks retain portrait framing.
            water.Quad(new Vector3(-220,8,330),new Vector3(-220,25,395),new Vector3(220,25,395),new Vector3(220,8,330),new Color(3,0,0,1));
            views.Add(water.Save("Reference River",root,material));
            var bank=new Geometry();
            bank.Box(new Vector3(0,7.3f,325),new Vector3(360,1.4f,4),new Color(5,0,0,1));
            bank.Box(new Vector3(0,25.6f,397),new Vector3(360,1.2f,3),new Color(5,0,0,1));
            for(int i=0;i<12;i++)
            {
                float x=-100+i*18;
                bank.Box(new Vector3(x,5,314+i%3),new Vector3(10,5+i%4,6),new Color(2,3,i/12f,1));
                bank.Quad(new Vector3(x-6,7.8f,319),new Vector3(x-6,9.3f,319),new Vector3(x+6,9.3f,319),new Vector3(x+6,7.8f,319),new Color(5,1,0,1));
            }
            views.Add(bank.Save("River Banks",root,material));
            var bridge=new Geometry();
            bridge.Box(new Vector3(0,BridgeDeckY,BridgeZ),new Vector3(250,1.3f,6),new Color(4,0,0,1));
            foreach(float z in new[]{BridgeZ-3.1f,BridgeZ+3.1f})
                bridge.Box(new Vector3(0,BridgeDeckY+1.2f,z),new Vector3(250,.28f,.22f),new Color(4,0,0,1));
            for(int i=0;i<11;i++)
            {
                float x=-115+i*23;
                bridge.Box(new Vector3(x,18,BridgeZ),new Vector3(1.9f,12,3.2f),new Color(4,0,0,1));
                bridge.Box(new Vector3(x,23.1f,BridgeZ),new Vector3(6,1.3f,4),new Color(4,0,0,1));
                bridge.Quad(new Vector3(x-.325f,BridgeDeckY+1.425f,BridgeZ-3.2f),new Vector3(x-.325f,BridgeDeckY+1.775f,BridgeZ-3.2f),new Vector3(x+.325f,BridgeDeckY+1.775f,BridgeZ-3.2f),new Vector3(x+.325f,BridgeDeckY+1.425f,BridgeZ-3.2f),new Color(6,0,0,1));
            }
            views.Add(bridge.Save("Reference Bridge",root,material));
            root.gameObject.AddComponent<DistantBackdrop>().Configure(material,views.ToArray());
        }
        // Native Unity cylinders are needlessly detailed for thin tree branches/poles.
        internal static Mesh StreetCylinder()
        {
            string path=StarterScene.Generated+"/StreetCylinder.asset";var existing=AssetDatabase.LoadAssetAtPath<Mesh>(path);if(existing!=null)return existing;
            const int sides=16;var v=new List<Vector3>();var normals=new List<Vector3>();var t=new List<int>();
            for(int i=0;i<=sides;i++)
            {
                float angle=i*Mathf.PI*2/sides;var normal=new Vector3(Mathf.Cos(angle),0,Mathf.Sin(angle));
                v.Add(normal*.5f-Vector3.up);v.Add(normal*.5f+Vector3.up);normals.Add(normal);normals.Add(normal);
                if(i<sides){int a=i*2,b=a+2;t.AddRange(new[]{a,a+1,b+1,a,b+1,b});}
            }
            foreach(float y in new[]{-1f,1f})
            {
                int centre=v.Count;v.Add(new Vector3(0,y,0));normals.Add(Vector3.up*y);
                for(int i=0;i<sides;i++){float angle=i*Mathf.PI*2/sides;v.Add(new Vector3(Mathf.Cos(angle)*.5f,y,Mathf.Sin(angle)*.5f));normals.Add(Vector3.up*y);}
                for(int i=0;i<sides;i++){int a=centre+1+i,b=centre+1+(i+1)%sides;t.AddRange(y>0?new[]{centre,b,a}:new[]{centre,a,b});}
            }
            var mesh=new Mesh{name="Street Cylinder 16 sides"};mesh.SetVertices(v);mesh.SetNormals(normals);mesh.SetTriangles(t,0);mesh.RecalculateBounds();
            AssetDatabase.CreateAsset(mesh,path);return mesh;
        }
    }
}
