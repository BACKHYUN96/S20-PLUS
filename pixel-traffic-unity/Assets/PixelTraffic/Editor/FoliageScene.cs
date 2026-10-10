using System;
using System.Collections.Generic;
using UnityEditor;
using UnityEngine;

namespace PixelTraffic.UnityPrototype.Editor
{
    // Opaque, shared foliage: silhouette and baked colour detail without alpha cards.
    internal static class FoliageScene
    {
        internal static Mesh Canopy(int species)
        {
            string path=StarterScene.Generated+"/CanopySpecies"+species+".asset";
            Mesh existing=AssetDatabase.LoadAssetAtPath<Mesh>(path);if(existing!=null)return existing;
            Vector3[] centres;
            Vector3 radius;
            if(species==1)
            {
                centres=new[]{new Vector3(0,5.9f,0),new Vector3(-.95f,4.8f,.2f),new Vector3(.95f,4.9f,-.2f),new Vector3(.1f,4.7f,1),new Vector3(-.1f,4.7f,-1)};
                radius=new Vector3(1.3f,1.55f,1.28f);
            }
            else if(species==2)
            {
                centres=new[]{new Vector3(0,5.4f,0),new Vector3(-1.25f,4.65f,.2f),new Vector3(1.3f,4.6f,-.2f),new Vector3(.15f,4.65f,1.05f),new Vector3(-.2f,4.65f,-1.1f)};
                radius=new Vector3(1.5f,1.15f,1.4f);
            }
            else
            {
                centres=new[]{new Vector3(0,5.65f,0),new Vector3(-1.05f,4.7f,.15f),new Vector3(1.1f,4.75f,-.2f),new Vector3(.1f,4.7f,1.05f),new Vector3(-.1f,4.65f,-1)};
                radius=new Vector3(1.45f,1.35f,1.4f);
            }
            Mesh mesh=Clusters("Canopy Species "+species,centres,radius,true,species);
            AssetDatabase.CreateAsset(mesh,path);return mesh;
        }

        internal static Mesh RoofShrub()
        {
            string path=StarterScene.Generated+"/RoofShrub.asset";
            Mesh existing=AssetDatabase.LoadAssetAtPath<Mesh>(path);if(existing!=null)return existing;
            Mesh mesh=Clusters("Roof Shrub",new[]{new Vector3(-.20f,.42f,0),new Vector3(.20f,.40f,.05f),new Vector3(0,.57f,.1f)},new Vector3(.38f,.52f,.6f),false,7);
            AssetDatabase.CreateAsset(mesh,path);return mesh;
        }

        internal static Material Leaves(int species)
        {
            Color[] greens={new Color(.33f,.49f,.13f),new Color(.40f,.55f,.17f),new Color(.25f,.43f,.105f)};
            Material material=StarterScene.Surface("Leaves "+species,greens[species],0,.20f);
            if(material.GetTexture("_BaseMap")!=null)return material;
            string path=StarterScene.Generated+"/FoliageColour.asset";
            Texture2D texture=AssetDatabase.LoadAssetAtPath<Texture2D>(path);
            if(texture==null)
            {
                const int size=256;texture=new Texture2D(size,size,TextureFormat.RGB24,true){name="Foliage Colour",filterMode=FilterMode.Trilinear,wrapMode=TextureWrapMode.Repeat,anisoLevel=2};
                var pixels=new Color[size*size];
                for(int y=0;y<size;y++)for(int x=0;x<size;x++)
                {
                    float u=x/(float)size,v=y/(float)size;
                    float broad=TileNoise(u,v,7,31,11),leaf=TileNoise(u,v,43,7,29);
                    float shade=.55f+broad*.27f+leaf*.18f;
                    // Small oblique leaf groups stay readable after mip filtering.
                    float vein=Mathf.Abs(Mathf.Sin((u*71+v*43)*Mathf.PI));
                    shade*=.94f+vein*.06f;pixels[y*size+x]=new Color(shade*.96f,shade,shade*.87f);
                }
                texture.SetPixels(pixels);texture.Apply(true,false);AssetDatabase.CreateAsset(texture,path);
            }
            material.SetTexture("_BaseMap",texture);EditorUtility.SetDirty(material);return material;
        }

        private static float TileNoise(float u,float v,float frequency,float x,float y)
        {
            float a=Mathf.PerlinNoise(u*frequency+x,v*frequency+y),b=Mathf.PerlinNoise((u-1)*frequency+x,v*frequency+y);
            float c=Mathf.PerlinNoise(u*frequency+x,(v-1)*frequency+y),d=Mathf.PerlinNoise((u-1)*frequency+x,(v-1)*frequency+y);
            return Mathf.Lerp(Mathf.Lerp(a,b,u),Mathf.Lerp(c,d,u),v);
        }

        private static Mesh Clusters(string name,Vector3[] centres,Vector3 radius,bool refine,int seed)
        {
            float t=(1+Mathf.Sqrt(5))/2;
            var unit=new List<Vector3>{new Vector3(-1,t,0),new Vector3(1,t,0),new Vector3(-1,-t,0),new Vector3(1,-t,0),new Vector3(0,-1,t),new Vector3(0,1,t),new Vector3(0,-1,-t),new Vector3(0,1,-t),new Vector3(t,0,-1),new Vector3(t,0,1),new Vector3(-t,0,-1),new Vector3(-t,0,1)};
            for(int i=0;i<unit.Count;i++)unit[i]=unit[i].normalized;
            int[] faces={0,11,5,0,5,1,0,1,7,0,7,10,0,10,11,1,5,9,5,11,4,11,10,2,10,7,6,7,1,8,3,9,4,3,4,2,3,2,6,3,6,8,3,8,9,4,9,5,2,4,11,6,2,10,8,6,7,9,8,1};
            if(refine)
            {
                var mid=new Dictionary<long,int>();
                int Mid(int a,int b)
                {
                    long key=((long)Math.Min(a,b)<<32)|(uint)Math.Max(a,b);
                    if(!mid.TryGetValue(key,out int index)){index=unit.Count;unit.Add((unit[a]+unit[b]).normalized);mid.Add(key,index);}return index;
                }
                var next=new List<int>();
                for(int i=0;i<faces.Length;i+=3)
                {int a=faces[i],b=faces[i+1],c=faces[i+2],ab=Mid(a,b),bc=Mid(b,c),ca=Mid(c,a);next.AddRange(new[]{a,ab,ca,b,bc,ab,c,ca,bc,ab,bc,ca});}
                faces=next.ToArray();
            }
            var positions=new List<Vector3>();var indices=new List<int>();var uv=new List<Vector2>();
            for(int clump=0;clump<centres.Length;clump++)
            {
                int offset=positions.Count;
                foreach(Vector3 n in unit)
                {
                    float growth=.92f+Mathf.PerlinNoise(n.x*3+seed*13+clump*7,n.z*3+n.y*2+19)*.16f;
                    positions.Add(centres[clump]+Vector3.Scale(n,radius)*growth);
                    uv.Add(new Vector2(n.x*.55f+clump*.17f,n.y*.55f+seed*.21f));
                }
                foreach(int index in faces)indices.Add(offset+index);
            }
            var mesh=new Mesh{name=name};mesh.SetVertices(positions);mesh.SetUVs(0,uv);mesh.SetTriangles(indices,0);mesh.RecalculateNormals();mesh.RecalculateBounds();return mesh;
        }
    }
}
