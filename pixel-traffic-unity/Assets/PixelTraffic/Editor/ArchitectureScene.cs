using System.Collections.Generic;
using UnityEditor;
using UnityEngine;
using UnityEngine.Rendering;

namespace PixelTraffic.UnityPrototype.Editor
{
    internal static class ArchitectureScene
    {
        internal static void Details(Transform root,int index,float height,float streetSide,Material stone,Material metal,Material facade)
        {
            float outward=Mathf.Sign(streetSide);int profile=index+(outward<0?3:0);bool near=index<3;int balconyCount=near?3:2;
            var trim=new Solids();var rails=new Solids();var plaster=new Solids();
            foreach(float z in new[]{-6.78f,6.78f})trim.Box(new Vector3(streetSide+outward*.04f,height*.5f,z),new Vector3(.23f,height,.22f));
            trim.Box(new Vector3(streetSide+outward*.05f,height-.5f,0),new Vector3(.28f,.22f,14.1f));
            foreach(float z in new[]{-6.9f,6.9f})trim.Box(new Vector3(0,height+.4f,z),new Vector3(6.4f,.7f,.2f));
            for(int balcony=0;balcony<balconyCount;balcony++)
            {
                float z=balcony==2?(profile%2==0?-4f:4f):balcony==0?-2.6f:2.6f;
                float y=balcony==2?height-3.3f:1.6f+(balcony+1)*2.8f-.9f;
                float width=near?(balcony==2?4f:2.12f+(profile%3)*.32f):2.12f;
                float depth=near?(balcony==2?1.05f:.78f+(profile%3)*.08f):.78f;
                trim.Box(new Vector3(streetSide+outward*(depth*.5f-.07f),y,z),new Vector3(depth,.13f,width));
                float front=streetSide+outward*(depth-.09f);
                rails.Box(new Vector3(front,y+.75f,z),new Vector3(.06f,.055f,width));
                int posts=balcony==2?6:4;
                for(int post=0;post<posts;post++)
                    rails.Box(new Vector3(front,y+.39f,z+Mathf.Lerp(-width*.5f+.06f,width*.5f-.06f,post/(float)(posts-1))),new Vector3(.045f,.68f,.045f));
                foreach(float side in new[]{-1f,1f})rails.Box(new Vector3(streetSide+outward*(depth*.5f-.05f),y+.75f,z+side*(width*.5f-.06f)),new Vector3(depth-.08f,.055f,.055f));
            }
            // Slats are baked into the same metal mesh, without a renderer per slat.
            float ventX=outward>0?1.76f:.24f,ventZ=near?-.5f:1.6f;
            for(int i=0;i<5;i++)rails.Box(new Vector3(ventX,height+.25f+i*.17f,ventZ),new Vector3(.025f,.065f,1.9f));
            plaster.Box(new Vector3(near?.9f:1.5f,height+1.0f,-3.0f),new Vector3(near?1.65f+profile*.12f:1.65f,1.75f,2.0f));
            if(near)
            {
                float capHeight=1f+profile*.22f;
                plaster.Box(new Vector3(profile%2==0?-.35f:.35f,height+.2f+capHeight*.5f,4f),new Vector3(2.8f+profile*.08f,capHeight,4f));
            }
            if(index%3==0)
            {
                rails.Box(new Vector3(-1.8f,height+1.15f,3.8f),new Vector3(.07f,1.8f,.07f));
                foreach(float y in new[]{height+1.6f,height+1.9f})rails.Box(new Vector3(-1.8f,y,3.8f),new Vector3(1.0f,.045f,.045f));
            }
            if(index%2==0)
            {
                foreach(float z in new[]{-4.5f,4.5f})trim.Box(new Vector3(streetSide-outward*.6f,height+.42f,z),new Vector3(.95f,.6f,1.8f));
                var go=new GameObject("Roof Garden",typeof(MeshFilter),typeof(MeshRenderer));go.transform.SetParent(root,false);
                go.transform.localPosition=new Vector3(streetSide-outward*.6f,height+.65f,-4.5f);go.GetComponent<MeshFilter>().sharedMesh=FoliageScene.RoofShrub();go.GetComponent<MeshRenderer>().sharedMaterial=FoliageScene.Leaves(index%3);
            }
            if(index<3)
            {
                var plants=new Solids();
                for(int balcony=0;balcony<balconyCount;balcony++)
                {
                    float z=balcony==2?(profile%2==0?-4f:4f):balcony==0?-2.6f:2.6f;
                    float y=balcony==2?height-3.3f:1.6f+(balcony+1)*2.8f-.9f;
                    float plantX=streetSide+outward*(balcony==2?.45f:.36f+profile%3*.03f);
                    trim.Box(new Vector3(plantX,y+.25f,z),new Vector3(.47f,.38f,1.18f));
                    rails.Box(new Vector3(plantX,y+.45f,z),new Vector3(.49f,.045f,1.22f));
                    plants.Append(FoliageScene.RoofShrub(),new Vector3(plantX,y+.44f,z),new Vector3(.35f,.48f+profile%3*.07f,.72f));
                    if(balcony==2)continue;
                    // Units sit beside, rather than in front of, the balcony openings.
                    float unitY=y+.87f,unitZ=balcony==0?-3.9f:3.9f;
                    plaster.Box(new Vector3(streetSide+outward*.27f,unitY,unitZ),new Vector3(.48f,.52f,.77f));
                    rails.Box(new Vector3(streetSide+outward*.52f,unitY,unitZ),new Vector3(.035f,.39f,.62f));
                    for(int slat=0;slat<4;slat++)
                        rails.Plane(streetSide+outward*.54f,unitY-.15f+slat*.09f,unitY-.135f+slat*.09f,unitZ-.29f,unitZ+.29f,outward);
                }
                // Ground-floor stone fascia and raised entrance lip stay behind the walking corridor.
                trim.Box(new Vector3(streetSide+outward*.15f,3.22f,0),new Vector3(.25f,.13f,11.8f));
                trim.Box(new Vector3(streetSide+outward*.11f,.095f,(index%3-1)*3.6f),new Vector3(.18f,.09f,1.75f));
                plants.Save(root,"Balcony Plants",(outward>0?"Left":"Right")+index.ToString("00")+"BalconyPlants",FoliageScene.Leaves(index%3));
            }
            string key=(outward>0?"Left":"Right")+index.ToString("00");
            trim.Save(root,"Facade Stonework",key+"Stonework",stone);
            rails.Save(root,"Balcony Metalwork",key+"Metalwork",metal);
            plaster.Save(root,"Roof Access",key+"RoofAccess",facade);
        }

        internal static Color MasonryPixel(int x,int y,int size)
        {
            float u=x/(float)size,v=y/(float)size;
            // Large concrete panels and subtle runoff replace the small repeated brick grid.
            float cloud=Mathf.PerlinNoise(u*8+53,v*8+17),grain=((x*31+y*73+x*y*11)%97)/96f;
            bool seam=y%(size/4)<2||(x+(y/(size/4)%2)*(size/8))%(size/4)<1;
            float stain=Mathf.PerlinNoise(u*29+41,v*3+7)*.055f;
            float shade=seam?.72f:.89f+cloud*.065f+grain*.025f-stain;
            return new Color(shade,shade*.99f,shade*.97f);
        }

        private sealed class Solids
        {
            readonly List<Vector3> vertices=new List<Vector3>();readonly List<Vector2> uv=new List<Vector2>();readonly List<int> indices=new List<int>();
            readonly List<Vector3> planeVertices=new List<Vector3>();
            internal void Box(Vector3 centre,Vector3 size)
            {
                Vector3 h=size*.5f;
                Vector3[] p={new Vector3(-h.x,-h.y,-h.z),new Vector3(h.x,-h.y,-h.z),new Vector3(h.x,h.y,-h.z),new Vector3(-h.x,h.y,-h.z),new Vector3(-h.x,-h.y,h.z),new Vector3(h.x,-h.y,h.z),new Vector3(h.x,h.y,h.z),new Vector3(-h.x,h.y,h.z)};
                int[] faces={0,3,2,1,4,5,6,7,0,4,7,3,1,2,6,5,0,1,5,4,3,7,6,2};
                for(int face=0;face<6;face++)
                {
                    int n=vertices.Count;
                    for(int i=0;i<4;i++)vertices.Add(centre+p[faces[face*4+i]]);
                    uv.AddRange(new[]{Vector2.zero,Vector2.right,Vector2.one,Vector2.up});
                    indices.AddRange(new[]{n,n+1,n+2,n,n+2,n+3});
                }
            }
            internal void Plane(float x,float bottom,float top,float start,float end,float outward)
            {
                // Keep box vertices contiguous for the independent slab/railing geometry oracle.
                if(outward>0)planeVertices.AddRange(new[]{new Vector3(x,bottom,start),new Vector3(x,top,start),new Vector3(x,top,end),new Vector3(x,bottom,end)});
                else planeVertices.AddRange(new[]{new Vector3(x,bottom,start),new Vector3(x,bottom,end),new Vector3(x,top,end),new Vector3(x,top,start)});
            }
            internal void Append(Mesh mesh,Vector3 position,Vector3 scale)
            {
                int n=vertices.Count;
                foreach(Vector3 v in mesh.vertices)vertices.Add(position+Vector3.Scale(v,scale));
                uv.AddRange(mesh.uv);foreach(int i in mesh.triangles)indices.Add(n+i);
            }
            internal void Save(Transform parent,string name,string asset,Material material)
            {
                for(int i=0;i<planeVertices.Count;i+=4)
                {
                    int n=vertices.Count;vertices.AddRange(planeVertices.GetRange(i,4));uv.AddRange(new[]{Vector2.zero,Vector2.right,Vector2.one,Vector2.up});indices.AddRange(new[]{n,n+1,n+2,n,n+2,n+3});
                }
                var mesh=new Mesh{name=asset};mesh.SetVertices(vertices);mesh.SetUVs(0,uv);mesh.SetTriangles(indices,0);mesh.RecalculateNormals();mesh.RecalculateBounds();AssetDatabase.CreateAsset(mesh,StarterScene.Generated+"/"+asset+".asset");
                var obj=new GameObject(name,typeof(MeshFilter),typeof(MeshRenderer));obj.transform.SetParent(parent,false);obj.GetComponent<MeshFilter>().sharedMesh=mesh;
                var renderer=obj.GetComponent<MeshRenderer>();renderer.sharedMaterial=material;renderer.shadowCastingMode=ShadowCastingMode.On;renderer.receiveShadows=true;renderer.lightProbeUsage=LightProbeUsage.Off;renderer.reflectionProbeUsage=ReflectionProbeUsage.Off;
            }
        }
    }
}
