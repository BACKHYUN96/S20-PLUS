using System;
using System.Collections.Generic;
using UnityEditor;
using UnityEngine;

namespace PixelTraffic.UnityPrototype.Editor
{
    internal static class SceneryChecks
    {
        internal static Report Validate()
        {
            var meshes=new HashSet<Mesh>();var materials=new HashSet<Material>();
            var gardenMaterials=new List<Material>();
            int trees=0,buildings=0,gardens=0,checkedVertices=0,architectureTriangles=0;
            Texture2D leafTexture=null;
            foreach(var root in UnityEngine.Object.FindObjectsByType<Transform>(FindObjectsSortMode.None))
            {
                if(root.name=="City Tree")
                {
                    trees++;var canopy=root.Find("Layered Canopy");Need(canopy!=null,"Tree canopy missing.");
                    var mesh=canopy.GetComponent<MeshFilter>().sharedMesh;meshes.Add(mesh);
                    Geometry(mesh,ref checkedVertices);
                    Need(mesh.triangles.Length/3<=480&&mesh.bounds.min.y>3&&mesh.bounds.max.y<8,"Foliage blocks walking height or exceeds silhouette budget.");
                    var material=canopy.GetComponent<MeshRenderer>().sharedMaterial;materials.Add(material);
                    var texture=material.GetTexture("_BaseMap") as Texture2D;
                    Need(texture!=null&&texture.width==256&&texture.height==256&&texture.mipmapCount>1&&texture.filterMode==FilterMode.Trilinear,"Leaf colour detail lacks shared distance-filtered texture.");
                    if(leafTexture==null)leafTexture=texture;Need(texture==leafTexture,"Species duplicate the leaf texture.");
                    Need(material.shader.name=="Universal Render Pipeline/Lit"&&material.GetFloat("_Surface")==0,"Foliage adds transparent leaf layers.");
                    Need(Mathf.Abs(root.position.x)>8&&Mathf.Abs(root.position.x)<9,"Trees moved into the road or walking route.");
                }
                if(root.name!="City Building")continue;
                buildings++;
                foreach(string name in new[]{"Facade Stonework","Balcony Metalwork","Roof Access"})
                {
                    Transform detail=root.Find(name);Need(detail!=null,"Merged building detail missing: "+name);
                    var mesh=detail.GetComponent<MeshFilter>().sharedMesh;Geometry(mesh,ref checkedVertices);architectureTriangles+=mesh.triangles.Length/3;
                    foreach(Vector3 vertex in mesh.vertices)
                    {
                        Vector3 world=detail.TransformPoint(vertex);
                        Need(world.y>3.1f||Mathf.Abs(world.x)>10.6f,"New architecture blocks low pedestrian clearance.");
                        Need(Mathf.Abs(vertex.z)<7.2f,"Facade extends into the next building gap.");
                    }
                    if(name=="Balcony Metalwork")Need(mesh.bounds.min.y>3.3f,"Balcony railings enter pedestrian head height.");
                    Need(detail.GetComponent<MeshRenderer>().sharedMaterial!=null,"Building detail lacks material.");
                }
                Transform garden=root.Find("Roof Garden");
                if(garden!=null)
                {
                    gardens++;var mesh=garden.GetComponent<MeshFilter>().sharedMesh;Geometry(mesh,ref checkedVertices);
                    Need(mesh.triangles.Length/3<=80,"Small rooftop foliage exceeds its budget.");
                    foreach(Vector3 v in mesh.vertices)
                    {
                        Vector3 p=garden.localPosition+v;
                        Need(Mathf.Abs(p.x)<3.4f&&Mathf.Abs(p.z)<7.1f,"Roof garden hangs outside its roof footprint.");
                    }
                    gardenMaterials.Add(garden.GetComponent<MeshRenderer>().sharedMaterial);
                }
                Need(root.GetComponentsInChildren<Collider>().Length==0,"Details add primitive colliders.");
            }
            Need(trees==32&&meshes.Count==3&&materials.Count==3&&buildings==24&&gardens==12,"Scenery loses tree/building variety or counts.");
            foreach(Material material in gardenMaterials)Need(materials.Contains(material),"Roof garden adds an independent material.");
            // Assert the three actual assets are distinct shapes, not just different names.
            var shapes=new List<Mesh>(meshes);
            for(int a=0;a<shapes.Count;a++)for(int b=a+1;b<shapes.Count;b++)
                Need(Vector3.Distance(shapes[a].bounds.size,shapes[b].bounds.size)>.15f,"Canopy species have identical silhouettes.");
            float minimum=1,maximum=0;
            foreach(Color c in leafTexture.GetPixels()) {minimum=Mathf.Min(minimum,c.g);maximum=Mathf.Max(maximum,c.g);}
            Need(maximum-minimum>.12f,"Leaf texture is flat colour.");
            Material original=AssetDatabase.LoadAssetAtPath<Material>(StarterScene.Generated+"/Leaves0.mat");
            Need(original!=null&&original.GetTexture("_BaseMap")==leafTexture,"Runtime/editor changes replace shared leaf asset.");
            var pipeline=UnityEngine.Rendering.GraphicsSettings.defaultRenderPipeline as UnityEngine.Rendering.Universal.UniversalRenderPipelineAsset;
            CityEnvironment.Report budget=CityEnvironment.Validate(Camera.main,pipeline);
            Need(budget.triangles<110000,"Base scenery consumed the reserved full-population budget.");
            Need(UnityEngine.Object.FindFirstObjectByType<CityClimate>().AdditionalLights==4,"Architectural details add realtime lights.");
            return new Report{result="PASS: actual distinct opaque foliage, shared mip texture, merged facade/roof meshes, normals/UV/winding and pedestrian/roof clearance; weather/wind checked by ClimateChecks; device performance unmeasured",trees=trees,species=meshes.Count,buildings=buildings,balconies=buildings*2,roofGardens=gardens,canopyTrianglesPerTree=shapes[0].triangles.Length/3,architectureTriangles=architectureTriangles,checkedVertices=checkedVertices,leafTextureSize=leafTexture.width,leafTextureContrast=maximum-minimum};
        }

        static void Geometry(Mesh mesh,ref int count)
        {
            Need(mesh!=null&&mesh.vertexCount>0,"Missing scenery mesh.");
            Vector3[] vertices=mesh.vertices,normals=mesh.normals;Vector2[] uv=mesh.uv;int[] indices=mesh.triangles;
            Need(normals.Length==vertices.Length&&uv.Length==vertices.Length,"Scenery normals/UV incomplete.");
            for(int i=0;i<vertices.Length;i++)
            {
                Vector3 p=vertices[i];Need(Finite(p.x)&&Finite(p.y)&&Finite(p.z)&&Finite(uv[i].x)&&Finite(uv[i].y),"Scenery vertex or UV non-finite.");
                Need(Mathf.Abs(normals[i].magnitude-1)<.003f,"Scenery shading normal not unit length.");count++;
            }
            for(int i=0;i<indices.Length;i+=3)
            {
                int a=indices[i],b=indices[i+1],c=indices[i+2];Vector3 area=Vector3.Cross(vertices[b]-vertices[a],vertices[c]-vertices[a]);
                Need(area.sqrMagnitude>1e-12f&&Vector3.Dot(area,normals[a]+normals[b]+normals[c])>0,"Scenery triangle degenerate or reversed.");
            }
        }
        static bool Finite(float value)=>!float.IsNaN(value)&&!float.IsInfinity(value);
        static void Need(bool condition,string message){if(!condition)throw new InvalidOperationException(message);}
        [Serializable] internal sealed class Report
        {
            public string result;
            public int trees,species,buildings,balconies,roofGardens,canopyTrianglesPerTree,architectureTriangles,checkedVertices,leafTextureSize;
            public float leafTextureContrast;
        }
    }
}
