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
            float outward=Mathf.Sign(streetSide);
            var trim=new Solids();var rails=new Solids();var plaster=new Solids();
            foreach(float z in new[]{-6.78f,6.78f})trim.Box(new Vector3(streetSide+outward*.04f,height*.5f,z),new Vector3(.23f,height,.22f));
            trim.Box(new Vector3(streetSide+outward*.05f,height-.5f,0),new Vector3(.28f,.22f,14.1f));
            foreach(float z in new[]{-6.9f,6.9f})trim.Box(new Vector3(0,height+.4f,z),new Vector3(6.4f,.7f,.2f));
            for(int balcony=0;balcony<2;balcony++)
            {
                float z=balcony==0?-2.6f:2.6f,y=1.6f+(balcony+1)*2.8f-.9f;
                trim.Box(new Vector3(streetSide+outward*.32f,y,z),new Vector3(.78f,.13f,2.12f));
                float front=streetSide+outward*.69f;
                rails.Box(new Vector3(front,y+.75f,z),new Vector3(.06f,.055f,2.12f));
                foreach(float offset in new[]{-1f,-.34f,.34f,1f})rails.Box(new Vector3(front,y+.39f,z+offset),new Vector3(.045f,.68f,.045f));
                foreach(float side in new[]{-1f,1f})rails.Box(new Vector3(streetSide+outward*.34f,y+.75f,z+side),new Vector3(.7f,.055f,.055f));
            }
            // Slats are baked into the same metal mesh, without a renderer per slat.
            float ventX=outward>0?1.76f:.24f;
            for(int i=0;i<5;i++)rails.Box(new Vector3(ventX,height+.25f+i*.17f,1.6f),new Vector3(.025f,.065f,1.9f));
            plaster.Box(new Vector3(1.5f,height+1.0f,-3.0f),new Vector3(1.65f,1.75f,2.0f));
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
            internal void Save(Transform parent,string name,string asset,Material material)
            {
                var mesh=new Mesh{name=asset};mesh.SetVertices(vertices);mesh.SetUVs(0,uv);mesh.SetTriangles(indices,0);mesh.RecalculateNormals();mesh.RecalculateBounds();AssetDatabase.CreateAsset(mesh,StarterScene.Generated+"/"+asset+".asset");
                var obj=new GameObject(name,typeof(MeshFilter),typeof(MeshRenderer));obj.transform.SetParent(parent,false);obj.GetComponent<MeshFilter>().sharedMesh=mesh;
                var renderer=obj.GetComponent<MeshRenderer>();renderer.sharedMaterial=material;renderer.shadowCastingMode=ShadowCastingMode.On;renderer.receiveShadows=true;renderer.lightProbeUsage=LightProbeUsage.Off;renderer.reflectionProbeUsage=ReflectionProbeUsage.Off;
            }
        }
    }
}
