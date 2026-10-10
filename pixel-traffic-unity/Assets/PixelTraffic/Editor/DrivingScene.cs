using System.Collections.Generic;
using UnityEditor;
using UnityEngine;
using UnityEngine.Rendering;

namespace PixelTraffic.UnityPrototype.Editor
{
    internal static class DrivingScene
    {
        internal static void Attach(Transform root,VehicleGeometry.Shape shape,Material lamp,string model)
        {
            float front=shape.head.bounds.max.z+.0005f,rear=shape.tail.bounds.min.z-.0005f;
            float frontY=shape.tail.bounds.center.y;
            Renderer l=Part(root,"Left Turn Signals",Signal(model+"LeftSignals",-1,front,rear,frontY),lamp);
            Renderer r=Part(root,"Right Turn Signals",Signal(model+"RightSignals",1,front,rear,frontY),lamp);
            Renderer b=Part(root,"Headlight Road Beams",Beam(model+"RoadBeams",front),lamp);b.enabled=false;
            root.gameObject.AddComponent<VehicleLighting>().Configure(root.Find("Front Lamps").GetComponent<Renderer>(),root.Find("Rear Lamps").GetComponent<Renderer>(),l,r,b);
        }
        static Mesh Signal(string name,float side,float front,float rear,float y)
        {
            var p=new List<Vector3>();var uv=new List<Vector2>();var t=new List<int>();
            void Face(float z,Vector3 normal)
            {float x=side*.78f;Quad(p,uv,t,new Vector3(x-.055f,y-.025f,z),new Vector3(x+.055f,y-.025f,z),new Vector3(x+.055f,y+.025f,z),new Vector3(x-.055f,y+.025f,z),normal);}
            Face(front,Vector3.forward);Face(rear,Vector3.back);return Save(name,p,uv,t);
        }
        static Mesh Beam(string name,float front)
        {
            var p=new List<Vector3>();var uv=new List<Vector2>();var t=new List<int>();
            foreach(float side in new[]{-1f,1f})
            {float x=side*.62f;Quad(p,uv,t,new Vector3(x-.85f,.034f,front+.2f),new Vector3(x+.85f,.034f,front+.2f),new Vector3(x+.85f,.034f,front+5.4f),new Vector3(x-.85f,.034f,front+5.4f),Vector3.up);}
            return Save(name,p,uv,t);
        }
        static void Quad(List<Vector3> p,List<Vector2> uv,List<int> t,Vector3 a,Vector3 b,Vector3 c,Vector3 d,Vector3 normal)
        {
            int n=p.Count;p.AddRange(new[]{a,b,c,d});uv.AddRange(new[]{Vector2.zero,Vector2.right,Vector2.one,Vector2.up});
            bool flip=Vector3.Dot(Vector3.Cross(b-a,c-a),normal)<0;t.AddRange(flip?new[]{n,n+2,n+1,n,n+3,n+2}:new[]{n,n+1,n+2,n,n+2,n+3});
        }
        static Mesh Save(string name,List<Vector3> p,List<Vector2> uv,List<int> t)
        {
            string path=StarterScene.Generated+"/"+name+".asset";var old=AssetDatabase.LoadAssetAtPath<Mesh>(path);if(old!=null)return old;
            var mesh=new Mesh{name=name};mesh.SetVertices(p);mesh.SetUVs(0,uv);mesh.SetTriangles(t,0);mesh.RecalculateNormals();mesh.RecalculateBounds();AssetDatabase.CreateAsset(mesh,path);return mesh;
        }
        static Renderer Part(Transform root,string name,Mesh mesh,Material mat)
        {
            var go=new GameObject(name,typeof(MeshFilter),typeof(MeshRenderer));go.transform.SetParent(root,false);go.GetComponent<MeshFilter>().sharedMesh=mesh;
            var renderer=go.GetComponent<MeshRenderer>();renderer.sharedMaterial=mat;renderer.shadowCastingMode=ShadowCastingMode.Off;renderer.receiveShadows=false;renderer.lightProbeUsage=LightProbeUsage.Off;renderer.reflectionProbeUsage=ReflectionProbeUsage.Off;return renderer;
        }
        internal static void ConfigureBeams(Material glow)
        {foreach(var light in Object.FindObjectsByType<VehicleLighting>(FindObjectsSortMode.None))light.Beams.sharedMaterial=glow;}
    }
}
