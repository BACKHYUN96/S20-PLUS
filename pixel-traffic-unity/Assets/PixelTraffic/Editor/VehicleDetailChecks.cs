using System;
using System.Collections.Generic;
using UnityEditor;
using UnityEngine;

namespace PixelTraffic.UnityPrototype.Editor
{
    internal static class VehicleDetailChecks
    {
        internal static Report Validate()
        {
            var models=new HashSet<string>();var shared=new Dictionary<string,Mesh>();
            int fleetTriangles=0,checkedVertices=0,curvedSeams=0;
            var cars=UnityEngine.Object.FindObjectsByType<PrototypeDrive>(FindObjectsSortMode.None);
            foreach(var car in cars)
            {
                Need(car.GetComponentsInChildren<MeshRenderer>().Length==14,"Vehicle detail adds renderers.");
                foreach(var filter in car.GetComponentsInChildren<MeshFilter>())
                {
                    Mesh mesh=filter.sharedMesh;fleetTriangles+=mesh.triangles.Length/3;
                    string key=car.Model+"/"+filter.name;
                    if(shared.TryGetValue(key,out var previous)){Need(previous==mesh,"Mesh is duplicated per vehicle.");continue;}
                    shared.Add(key,mesh);Vector3[] vertices=mesh.vertices,normals=mesh.normals;
                    Need(mesh.uv.Length==vertices.Length,"Vehicle UV count mismatch.");
                    Need(normals.Length==vertices.Length,"Vehicle normals missing.");
                    foreach(Vector3 v in vertices)Need(Finite(v.x)&&Finite(v.y)&&Finite(v.z),"Nonfinite vehicle vertex.");
                    foreach(Vector3 n in normals)Need(Finite(n.x)&&Finite(n.y)&&Finite(n.z)&&Mathf.Abs(n.magnitude-1)<.005f,"Invalid vehicle normal.");
                    int[] indices=mesh.triangles;
                    for(int n=0;n<indices.Length;n+=3)
                        Need(Vector3.Cross(vertices[indices[n+1]]-vertices[indices[n]],vertices[indices[n+2]]-vertices[indices[n]]).sqrMagnitude>1e-14f,"Collapsed vehicle face.");
                    checkedVertices+=vertices.Length;
                    if(filter.name=="Sculpted Body" || filter.name=="Tyre")
                    {
                        // Across duplicated vertices on a curved seam, shading normals agree.
                        int smooth=0;
                        for(int i=0;i<vertices.Length;i++)for(int j=i+1;j<vertices.Length;j++)
                            if((vertices[i]-vertices[j]).sqrMagnitude<1e-10f&&Vector3.Dot(normals[i],normals[j])>.999f)smooth++;
                        Need(smooth>12,"Curved surface has unwelded shading seams.");curvedSeams+=smooth;
                    }
                    if(filter.name=="Tyre")
                    {
                        float shoulder=0,tread=0;
                        foreach(Vector3 v in vertices)
                        {float radius=new Vector2(v.y,v.z).magnitude;if(Mathf.Abs(v.x)<.001f)tread=Mathf.Max(tread,radius);else shoulder=Mathf.Max(shoulder,radius);}
                        Need(Mathf.Abs(tread-car.WheelRadius)<.001f&&shoulder<tread*.92f,"Tyre shoulder is flat or road radius changed.");
                    }
                }
                if(models.Add(car.Model))
                {
                    var texture=car.transform.Find("Sloped Windows").GetComponent<Renderer>().sharedMaterial.GetTexture("_BaseMap") as Texture2D;
                    Need(texture!=null&&texture.width==64&&texture.height==64,"Shared glass finish missing.");
                    Need((texture.GetPixel(32,60)-texture.GetPixel(32,3)).grayscale>.1f,"Window finish has no depth gradient.");
                    Mesh body=car.transform.Find("Sculpted Body").GetComponent<MeshFilter>().sharedMesh;
                    float expected=car.Model=="Suv" ? 1.84f : car.Model=="SportCoupe" ? 1.28f : 1.49f;
                    bool crown=false;foreach(Vector3 v in body.vertices)if(Mathf.Abs(v.x)<.001f&&Mathf.Abs(v.y-expected)<.001f)crown=true;
                    Need(crown,"Crowned roof silhouette missing.");
                }
            }
            Need(models.Count==4&&cars.Length==24,"Detail fleet incomplete.");
            // Previous mesh formula: 48,600 fleet triangles. Preserve the city budget,
            // including all 100 open umbrellas (previous city total: 118,914).
            Need(fleetTriangles<49686,"Vehicle detail exhausts the city triangle budget.");
            return new Report {result="PASS: actual shared mesh topology, finite/unit normals, curved shading, crowned roofs, tyre shoulder/contact, window UV/finish and fleet budget; device performance unmeasured",
                models=models.Count,vehicles=cars.Length,renderersPerVehicle=14,fleetTriangles=fleetTriangles,checkedVertices=checkedVertices,curvedSeams=curvedSeams,windowTextureSize=64};
        }
        private static bool Finite(float v)=>!float.IsNaN(v)&&!float.IsInfinity(v);
        private static void Need(bool value,string message){if(!value)throw new InvalidOperationException(message);}
        [Serializable] internal sealed class Report
        {
            public string result;public int models,vehicles,renderersPerVehicle,fleetTriangles,checkedVertices,curvedSeams,windowTextureSize;
        }
    }
}
