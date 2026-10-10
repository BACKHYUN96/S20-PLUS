using System;
using System.Collections.Generic;
using UnityEngine;

namespace PixelTraffic.UnityPrototype.Editor
{
    internal static class HeavyVehicleChecks
    {
        internal static Report Validate()
        {
            var drives=UnityEngine.Object.FindObjectsByType<PrototypeDrive>(FindObjectsSortMode.None);
            int buses=0,trucks=0,vertices=0,doorLocks=0,doorHinges=0;var checkedModels=new HashSet<string>();
            foreach(var drive in drives)
            {
                bool bus=drive.Model=="CityBus",truck=drive.Model=="BoxTruck";if(!bus&&!truck)continue;
                if(bus)buses++;else trucks++;
                Need(drive.Wheels.Length==4&&drive.GetComponentsInChildren<Renderer>().Length==17,"Heavy wheel/renderer contract changed.");
                Need(drive.GetComponentsInChildren<Collider>().Length==0&&drive.GetComponentsInChildren<Light>().Length==0,"Heavy fleet adds physics or real-time lights.");
                float frontAxle=bus?3.64f:2.35f,rearAxle=bus?-3.10f:-2.22f;
                int frontWheels=0,rearWheels=0;
                foreach(var wheel in drive.Wheels)
                {
                    Need(Mathf.Abs(wheel.localPosition.y-drive.WheelRadius)<.001f,"Heavy wheel contact is compressed.");
                    if(Mathf.Abs(wheel.localPosition.z-frontAxle)<.001f)frontWheels++;
                    if(Mathf.Abs(wheel.localPosition.z-rearAxle)<.001f)rearWheels++;
                }
                Need(frontWheels==2&&rearWheels==2,"Heavy axles do not align left/right.");
                if(!checkedModels.Add(drive.Model))continue;
                Mesh Mesh(string part)=>drive.transform.Find(part).GetComponent<MeshFilter>().sharedMesh;
                var body=Mesh("Sculpted Body");var points=new HashSet<Vector3Int>();
                foreach(var p in body.vertices)points.Add(Key(p));
                foreach(var p in body.vertices)Need(points.Contains(Key(new Vector3(-p.x,p.y,p.z))),"Heavy body has baked sideways perspective.");
                Need(Mathf.Abs(body.bounds.center.x)<.001f&&Mathf.Abs(body.bounds.size.z-(bus?10.6f:7.4f))<.001f,"Heavy body dimensions are not centred/metre scale.");
                float f=bus?5.3f:3.7f,r=-f,lampX=bus?1.04f:1.02f;
                float fy=bus?.88f:.83f,ry=bus?.82f:.75f;
                var heads=Boxes(Mesh("Front Lamps"));var tails=Boxes(Mesh("Rear Lamps"));
                foreach(float side in new[]{-1f,1f})
                {
                    Need(Has(heads,new Vector3(side*lampX,fy,f+.03f)),"Heavy front lamp is misplaced.");
                    Need(Has(tails,new Vector3(side*lampX,ry,r-.03f)),"Heavy brake lamp is misplaced.");
                    var signal=Mesh(side<0?"Left Turn Signals":"Right Turn Signals");
                    foreach(var p in signal.vertices)Need(side*p.x>.9f&&Mathf.Abs(Mathf.Abs(p.z)-Mathf.Abs(f))<.1f,"Heavy signal sits inside vehicle or on wrong end.");
                }
                if(truck)
                {
                    var metal=Boxes(Mesh("Chrome and Plates"));var trim=Boxes(Mesh("Grille and Trim"));
                    Need(Has(trim,new Vector3(0,2.12f,r-.012f)),"Cargo rear doors have no central seam.");
                    foreach(float side in new[]{-1f,1f})
                    {
                        Need(Has(metal,new Vector3(side*.28f,2.10f,r-.030f)),"Cargo locking bar missing.");doorLocks++;
                        for(int n=0;n<3;n++){Need(Has(metal,new Vector3(side*1.12f,1.36f+n*.70f,r-.030f)),"Cargo hinge missing.");doorHinges++;}
                    }
                    Color paint=drive.transform.Find("Sculpted Body").GetComponent<Renderer>().sharedMaterial.GetColor("_BaseColor");
                    Need(paint.r>.9f&&paint.g>.9f&&paint.b>.9f,"Box truck is not white.");
                }
                else
                {
                    Need(Boxes(Mesh("Sloped Windows")).Count>=18,"Bus lacks side windows or paired entry panes.");
                    Need(Mesh("Chrome and Plates").bounds.max.y>3.37f,"Bus roof air conditioning missing.");
                }
                foreach(var filter in drive.GetComponentsInChildren<MeshFilter>())
                {
                    var mesh=filter.sharedMesh;var v=mesh.vertices;var normals=mesh.normals;var uv=mesh.uv;var indices=mesh.triangles;
                    Need(normals.Length==v.Length&&uv.Length==v.Length,"Heavy mesh normal/UV arrays missing.");
                    for(int i=0;i<v.Length;i++)
                    {
                        Need(Finite(v[i].x)&&Finite(v[i].y)&&Finite(v[i].z)&&Finite(uv[i].x)&&Finite(uv[i].y),"Nonfinite heavy geometry.");
                        Need(Finite(normals[i].x)&&Mathf.Abs(normals[i].magnitude-1)<.005f,"Heavy shading normal invalid.");
                    }
                    for(int i=0;i<indices.Length;i+=3)
                    {
                        int a=indices[i],b=indices[i+1],c=indices[i+2];var cross=Vector3.Cross(v[b]-v[a],v[c]-v[a]);
                        Need(cross.sqrMagnitude>1e-14f&&Vector3.Dot(cross,normals[a]+normals[b]+normals[c])>0,"Heavy winding or face collapses.");
                    }
                    vertices+=v.Length;
                }
            }
            Need(buses==4&&trucks==4&&checkedModels.Count==2,"Mixed fleet lost bus/truck ratio.");
            return new Report{result="PASS: actual symmetric straight metre bodies, four axle pivots, front/rear lamps, paired cargo seam/locks/hinges, bus windows/AC, shared opaque mesh topology; phone test pending",buses=buses,trucks=trucks,checkedVertices=vertices,cargoLockBars=doorLocks,cargoHinges=doorHinges};
        }
        static bool Finite(float x)=>!float.IsNaN(x)&&!float.IsInfinity(x);
        static Vector3Int Key(Vector3 p)=>new Vector3Int(Mathf.RoundToInt(p.x*10000),Mathf.RoundToInt(p.y*10000),Mathf.RoundToInt(p.z*10000));
        static List<Bounds> Boxes(Mesh mesh)
        {
            var result=new List<Bounds>();var p=mesh.vertices;Need(p.Length%24==0,"Expected merged box-face topology.");
            for(int i=0;i<p.Length;i+=24){var b=new Bounds(p[i],Vector3.zero);for(int j=1;j<24;j++)b.Encapsulate(p[i+j]);result.Add(b);}return result;
        }
        static bool Has(List<Bounds> boxes,Vector3 center){foreach(var b in boxes)if(Vector3.Distance(b.center,center)<.001f)return true;return false;}
        static void Need(bool value,string message){if(!value)throw new InvalidOperationException(message);}
        [Serializable]internal sealed class Report{public string result;public int buses,trucks,checkedVertices,cargoLockBars,cargoHinges;}
    }
}
