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
            var roofProfiles=new HashSet<string>();var balconyWidths=new HashSet<int>();int upperTerraces=0;
            int trees=0,buildings=0,gardens=0,balconies=0,railPosts=0,checkedVertices=0,architectureTriangles=0,detailedBuildings=0,balconyPlants=0,airConditioners=0;
            Texture2D leafTexture=null;int reclaimedFoliageTriangles=0;var checkedCanopies=new HashSet<Mesh>();
            foreach(var root in UnityEngine.Object.FindObjectsByType<Transform>(FindObjectsSortMode.None))
            {
                if(root.name=="City Tree")
                {
                    trees++;var canopy=root.Find("Layered Canopy");Need(canopy!=null,"Tree canopy missing.");
                    var mesh=canopy.GetComponent<MeshFilter>().sharedMesh;meshes.Add(mesh);
                    Geometry(mesh,ref checkedVertices);
                    bool treeNear=root.position.z<16;
                    Need(mesh.vertexCount==(treeNear?630:84),"Small canopy clusters are missing.");
                    if(treeNear)
                    {
                        var points=mesh.vertices;var leafUv=mesh.uv;int fineGroups=0;
                        for(int first=0;first<points.Length;first+=42)
                        {
                            Bounds groupBounds=new Bounds(points[first],Vector3.zero);
                            for(int j=0;j<42;j++){groupBounds.Encapsulate(points[first+j]);Need(Mathf.Abs(leafUv[first+j].y-Mathf.Clamp((points[first+j].y-3)/5,.01f,.99f))<.0001f,"Canopy shade no longer follows height.");}
                            Need(groupBounds.size.x<2.1f&&groupBounds.size.y<2.1f&&groupBounds.size.z<2.1f,"Large foliage blobs remain in detailed crown.");fineGroups++;
                        }
                        Need(fineGroups==15,"Near crown is not split into small leaf groups.");
                    }
                    int originalTriangles=treeNear?1200:140;
                    Need(mesh.triangles.Length/3<=originalTriangles&&mesh.triangles.Length/3>originalTriangles/2,"Distance foliage silhouette budget changed.");
                    reclaimedFoliageTriangles+=originalTriangles-mesh.triangles.Length/3;
                    if(checkedCanopies.Add(mesh))HiddenFaces(mesh,treeNear);
                    Need(canopy.localScale.x>=.72f&&canopy.localScale.x<1&&canopy.localScale.y>=.89f&&canopy.localScale.y<1.04f,"Tree crown variety or crossing clearance lost.");
                    Need(root.Find("Trunk")!=null&&root.Find("Trunk").GetComponent<Renderer>().bounds.max.y>4,"Tree trunk disconnected.");
                    Need(mesh.triangles.Length/3<=1200&&mesh.bounds.min.y>3&&mesh.bounds.max.y<8,"Foliage blocks walking height or exceeds silhouette budget.");
                    var material=canopy.GetComponent<MeshRenderer>().sharedMaterial;materials.Add(material);
                    var texture=material.GetTexture("_BaseMap") as Texture2D;
                    Need(texture!=null&&texture.width==256&&texture.height==256&&texture.mipmapCount>1&&texture.filterMode==FilterMode.Trilinear,"Leaf colour detail lacks shared distance-filtered texture.");
                    if(leafTexture==null)leafTexture=texture;Need(texture==leafTexture,"Species duplicate the leaf texture.");
                    Need(material.shader.name=="Universal Render Pipeline/Lit"&&material.GetFloat("_Surface")==0,"Foliage adds transparent leaf layers.");
                    Need(Mathf.Abs(root.position.x)>8&&Mathf.Abs(root.position.x)<9,"Trees moved into the road or walking route.");
                }
                if(root.name!="City Building")continue;
                buildings++;bool near=root.position.z<=12.01f;if(near)detailedBuildings++;
                float height=root.Find("Facade").GetComponent<Renderer>().bounds.size.y;
                if(near)
                {
                    int slot=Mathf.RoundToInt((root.position.z+22)/17);
                    int expectedFloors=(root.position.x<0?new[]{6,5,4}:new[]{4,6,5})[slot];
                    Need(Mathf.Abs(height-(expectedFloors*2.8f+1.2f))<.001f,"Near building height differs from its independent profile.");
                    int caps=0;
                    foreach(Bounds box in Boxes(root.Find("Roof Access").GetComponent<MeshFilter>().sharedMesh))
                        if(Mathf.Abs(box.size.z-4)<.001f&&box.min.y>height)
                        {Need(box.max.z<6.01f&&box.max.x<3.3f&&box.min.x>-3.3f,"Setback rooftop volume leaves the roof footprint.");roofProfiles.Add(box.size.x.ToString("F2",System.Globalization.CultureInfo.InvariantCulture)+"/"+box.size.y.ToString("F2",System.Globalization.CultureInfo.InvariantCulture));caps++;}
                    Need(caps==1,"Near rooftop setback volume missing.");
                }
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
                // Count actual slabs and their standing posts in the merged meshes.
                // A nonempty metal mesh alone could otherwise pass with only roof slats.
                var slabs=Boxes(root.Find("Facade Stonework").GetComponent<MeshFilter>().sharedMesh);
                var metalParts=Boxes(root.Find("Balcony Metalwork").GetComponent<MeshFilter>().sharedMesh,near?32:0);
                int localBalconies=0;
                foreach(Bounds slab in slabs)
                {
                    if(slab.size.y>.18f||slab.size.x<.65f||slab.size.z<1.8f||slab.center.y<3||slab.center.y>height-.5f)continue;
                    int posts=0;
                    foreach(Bounds post in metalParts)
                        if(post.size.y>.6f&&post.size.x<.08f&&post.size.z<.08f&&post.center.z>slab.min.z&&post.center.z<slab.max.z&&Mathf.Abs(post.min.y-slab.max.y)<.06f&&Mathf.Abs(post.center.x-slab.center.x)<slab.extents.x+.03f)posts++;
                    Need(posts>=4,"Actual balcony has no complete standing railing.");
                    localBalconies++;railPosts+=posts;balconyWidths.Add(Mathf.RoundToInt(slab.size.z*100));
                    if(slab.size.z>3.8f)
                    {Need(near&&posts==6&&Mathf.Abs(slab.center.y-(height-3.3f))<.001f,"Wide upper terrace placement/railing incomplete.");upperTerraces++;}
                }
                Need(localBalconies==(near?3:2),"Actual near/far balcony count changed.");balconies+=localBalconies;
                Transform plants=root.Find("Balcony Plants");
                Need((plants!=null)==near,"Near/far balcony plant detail boundary changed.");
                if(near)
                {
                    Mesh plantMesh=plants.GetComponent<MeshFilter>().sharedMesh;Geometry(plantMesh,ref checkedVertices);
                    architectureTriangles+=plantMesh.triangles.Length/3;Need(plantMesh.triangles.Length/3==180,"Three small balcony shrubs exceed their geometry budget.");
                    balconyPlants+=3;gardenMaterials.Add(plants.GetComponent<MeshRenderer>().sharedMaterial);
                    foreach(Vector3 v in plantMesh.vertices)
                    {
                        Vector3 world=plants.TransformPoint(v);Need(world.y>3.8f&&Mathf.Abs(v.z)<6.2f,"Balcony plants enter pedestrian head space or leave the slab.");
                        Need(Mathf.Abs(v.x)>3.35f&&Mathf.Abs(v.x)<3.95f,"Shrubs are not on the street-facing balcony.");
                    }
                    int units=0;
                    foreach(Bounds box in Boxes(root.Find("Roof Access").GetComponent<MeshFilter>().sharedMesh))
                        if(Mathf.Abs(box.size.x-.48f)<.001f&&Mathf.Abs(box.size.y-.52f)<.001f&&Mathf.Abs(box.size.z-.77f)<.001f)
                        {Need(box.min.y>4,"Air conditioner enters walking clearance.");units++;}
                    Need(units==2,"Two actual wall-mounted air conditioners missing.");airConditioners+=units;
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
            Need(upperTerraces==6&&roofProfiles.Count==6&&balconyWidths.Count>=4,"Building roof/terrace variety incomplete.");
            Need(detailedBuildings==6&&balconyPlants==18&&airConditioners==12,"Near building equipment/plant counts changed.");
            Need(trees==32&&meshes.Count==6&&materials.Count==3&&buildings==24&&gardens==12,"Scenery loses tree/building variety or counts.");
            foreach(Material material in gardenMaterials)Need(materials.Contains(material),"Roof garden adds an independent material.");
            // Assert the three actual assets are distinct shapes, not just different names.
            var shapes=new List<Mesh>(meshes);
            for(int a=0;a<shapes.Count;a++)for(int b=a+1;b<shapes.Count;b++)
                Need(shapes[a].triangles.Length!=shapes[b].triangles.Length||Vector3.Distance(shapes[a].bounds.size,shapes[b].bounds.size)>.15f,"Canopy species have identical silhouettes.");
            float minimum=1,maximum=0;
            foreach(Color c in leafTexture.GetPixels()) {minimum=Mathf.Min(minimum,c.g);maximum=Mathf.Max(maximum,c.g);}
            Need(maximum-minimum>.12f,"Leaf texture is flat colour.");
            float lowerShade=0,upperShade=0;
            for(int x=0;x<leafTexture.width;x++)for(int y=0;y<32;y++)
            {lowerShade+=leafTexture.GetPixel(x,y).g;upperShade+=leafTexture.GetPixel(x,leafTexture.height-1-y).g;}
            Need((upperShade-lowerShade)/(leafTexture.width*32)>.15f,"Sunlit upper foliage lacks its baked shade gradient.");
            Material original=AssetDatabase.LoadAssetAtPath<Material>(StarterScene.Generated+"/Leaves0.mat");
            Need(original!=null&&original.GetTexture("_BaseMap")==leafTexture,"Runtime/editor changes replace shared leaf asset.");
            var pipeline=UnityEngine.Rendering.GraphicsSettings.defaultRenderPipeline as UnityEngine.Rendering.Universal.UniversalRenderPipelineAsset;
            CityEnvironment.Report budget=CityEnvironment.Validate(Camera.main,pipeline);
            Need(budget.triangles<110000,"Base scenery consumed the reserved full-population budget.");
            Need(UnityEngine.Object.FindFirstObjectByType<CityClimate>().AdditionalLights==4,"Architectural details add realtime lights.");
            Need(reclaimedFoliageTriangles>0,"Hidden crown faces were not reclaimed for the gardens.");
            return new Report{reclaimedFoliageTriangles=reclaimedFoliageTriangles,result="PASS: actual distinct opaque foliage, shared mip texture, merged facade/roof meshes, actual slabs/rail posts, normals/UV/winding and pedestrian/roof clearance; weather/wind checked by ClimateChecks; device performance unmeasured",roofProfiles=roofProfiles.Count,upperTerraces=upperTerraces,balconyWidthVariants=balconyWidths.Count,detailedBuildings=detailedBuildings,balconyPlants=balconyPlants,airConditioners=airConditioners,trees=trees,species=meshes.Count,buildings=buildings,balconies=balconies,balconyRailPosts=railPosts,roofGardens=gardens,canopyTrianglesPerTree=shapes[0].triangles.Length/3,architectureTriangles=architectureTriangles,checkedVertices=checkedVertices,leafTextureSize=leafTexture.width,leafTextureContrast=maximum-minimum};
        }


        static void HiddenFaces(Mesh mesh,bool near)
        {
            // Reconstruct every original closed clump independently of the production pruning.
            int[] baseFaces={0,11,5,0,5,1,0,1,7,0,7,10,0,10,11,1,5,9,5,11,4,11,10,2,10,7,6,7,1,8,3,9,4,3,4,2,3,2,6,3,6,8,3,8,9,4,9,5,2,4,11,6,2,10,8,6,7,9,8,1};
            int size=near?42:12;var faces=new List<int>(baseFaces);
            if(near)
            {
                var edges=new Dictionary<long,int>();int next=12;
                int Mid(int a,int b){long key=((long)Math.Min(a,b)<<32)|(uint)Math.Max(a,b);if(!edges.TryGetValue(key,out int v)){v=next++;edges[key]=v;}return v;}
                faces.Clear();
                for(int i=0;i<baseFaces.Length;i+=3)
                {int a=baseFaces[i],b=baseFaces[i+1],c=baseFaces[i+2],ab=Mid(a,b),bc=Mid(b,c),ca=Mid(c,a);faces.AddRange(new[]{a,ab,ca,b,bc,ab,c,ca,bc,ab,bc,ca});}
                Need(next==42,"Independent crown subdivision incomplete.");
            }
            var vertices=mesh.vertices;var kept=new HashSet<string>();
            string Key(int a,int b,int c)=>a+"/"+b+"/"+c;
            int[] indices=mesh.triangles;
            for(int i=0;i<indices.Length;i+=3)Need(kept.Add(Key(indices[i],indices[i+1],indices[i+2])),"Pruned canopy repeats a face.");
            var original=new List<int>();int matched=0;
            for(int group=0;group<vertices.Length/size;group++)for(int face=0;face<faces.Count;face+=3)
            {
                int a=group*size+faces[face],b=group*size+faces[face+1],c=group*size+faces[face+2];original.AddRange(new[]{a,b,c});
                if(kept.Contains(Key(a,b,c))){matched++;continue;}
                bool covered=false;
                for(int other=0;other<vertices.Length/size&&!covered;other++)
                {
                    if(other==group)continue;bool inside=true;
                    for(int plane=0;plane<faces.Count&&inside;plane+=3)
                    {
                        Vector3 p=vertices[other*size+faces[plane]],q=vertices[other*size+faces[plane+1]],v=vertices[other*size+faces[plane+2]];
                        Vector3 normal=Vector3.Cross(q-p,v-p).normalized;
                        foreach(int corner in new[]{a,b,c})if(Vector3.Dot(normal,vertices[corner]-p)>=-.001f){inside=false;break;}
                    }
                    covered=inside;
                }
                Need(covered,"Crown pruning removed a visible surface or changed its silhouette.");
            }
            Need(matched==indices.Length/3,"Pruned canopy added a foreign triangle.");
            var full=new Mesh();full.vertices=vertices;full.triangles=original.ToArray();full.RecalculateNormals();Vector3[] oldNormals=full.normals,newNormals=mesh.normals;
            for(int i=0;i<vertices.Length;i++)Need(Vector3.Distance(oldNormals[i],newNormals[i])<.00001f,"Removing hidden foliage altered visible shading normals.");
            UnityEngine.Object.DestroyImmediate(full);
        }
        static List<Bounds> Boxes(Mesh mesh,int trailingQuadVertices=0)
        {
            Vector3[] v=mesh.vertices;Need((v.Length-trailingQuadVertices)%24==0,"Merged solid mesh has incomplete faces.");
            var result=new List<Bounds>();
            for(int start=0;start<v.Length-trailingQuadVertices;start+=24)
            {
                var bounds=new Bounds(v[start],Vector3.zero);
                for(int i=1;i<24;i++)bounds.Encapsulate(v[start+i]);result.Add(bounds);
            }
            return result;
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
            public int reclaimedFoliageTriangles,roofProfiles,upperTerraces,balconyWidthVariants,detailedBuildings,balconyPlants,airConditioners,trees,species,buildings,balconies,balconyRailPosts,roofGardens,canopyTrianglesPerTree,architectureTriangles,checkedVertices,leafTextureSize;
            public float leafTextureContrast;
        }
    }
}
