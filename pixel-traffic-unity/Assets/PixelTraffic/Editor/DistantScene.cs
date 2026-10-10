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
                for(int i=0;i<48;i++)
                {
                    float x=-240+i*10,x2=x+10;
                    float Height(float value)=>38+layer*9+24*Mathf.Exp(-Mathf.Pow((value+42)/74,2))+Mathf.Sin(value*.032f+layer)*5+Mathf.Sin(value*.11f)*1.6f;
                    mountain.Quad(new Vector3(x,14,z),new Vector3(x,Height(x),z),new Vector3(x2,Height(x2),z),new Vector3(x2,14,z),new Color(1,layer*.43f,layer*.7f,1));
                }
                views.Add(mountain.Save("Distant Ridge "+layer,root,material));
            }
            var city=new Geometry();
            for(int i=0;i<26;i++)
            {
                float x=-132+i*10.5f,z=402+i%3*6,height=13+i*17%27;
                if(i==21)height=66;
                new GameObject("Skyline Tower").transform.SetParent(root,false);
                city.Box(new Vector3(x,20+height*.5f,z),new Vector3(6+i%4,height,8+i%3*2),new Color(2,height/2.4f,(i%7)/7f,1));
                city.Box(new Vector3(x,20+height+.6f,z),new Vector3(3,1.2f,4),new Color(4,0,0,1));
            }
            // A small summit landmark, using the same opaque material and no realtime light.
            city.Box(new Vector3(-42,70,422),new Vector3(.75f,14,.75f),new Color(4,0,0,1));
            city.Box(new Vector3(-42,74,422),new Vector3(4.1f,1.3f,3.2f),new Color(4,0,0,1));
            city.Box(new Vector3(-42,82,422),new Vector3(.22f,9,.22f),new Color(4,0,0,1));
            views.Add(city.Save("Opposite River City",root,material));
            var water=new Geometry();
            water.Quad(new Vector3(-220,8,372),new Vector3(-220,25,372),new Vector3(220,25,372),new Vector3(220,8,372),new Color(3,0,0,1));
            views.Add(water.Save("Reference River",root,material));
            var bridge=new Geometry();
            bridge.Box(new Vector3(0,25.5f,384),new Vector3(250,1.1f,6),new Color(4,0,0,1));
            bridge.Box(new Vector3(0,27.4f,384),new Vector3(250,.22f,6.2f),new Color(4,0,0,1));
            for(int i=0;i<11;i++)bridge.Box(new Vector3(-115+i*23,17,384),new Vector3(1.7f,17,3.2f),new Color(4,0,0,1));
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
