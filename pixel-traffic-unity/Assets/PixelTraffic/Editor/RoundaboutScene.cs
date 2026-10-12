using System;
using System.Collections.Generic;
using UnityEditor;
using UnityEngine;
using UnityEngine.Rendering;

namespace PixelTraffic.UnityPrototype.Editor
{
    internal static class RoundaboutScene
    {
        sealed class Shape
        {
            readonly List<Vector3> points=new List<Vector3>();readonly List<Vector2> uv=new List<Vector2>();readonly List<int> indices=new List<int>();
            void Vertex(Vector3 p){points.Add(p);uv.Add(new Vector2(p.x/6,p.z/6));}
            internal void Quad(Vector3 a,Vector3 b,Vector3 c,Vector3 d)
            {int n=points.Count;Vertex(a);Vertex(b);Vertex(c);Vertex(d);indices.AddRange(new[]{n,n+1,n+2,n,n+2,n+3});}
            internal void Triangle(Vector3 a,Vector3 b,Vector3 c)
            {int n=points.Count;Vertex(a);Vertex(b);Vertex(c);indices.AddRange(new[]{n,n+1,n+2});}
            internal void Rect(float x0,float x1,float z0,float z1,float y)=>Quad(new Vector3(x0,y,z0),new Vector3(x0,y,z1),new Vector3(x1,y,z1),new Vector3(x1,y,z0));
            internal void Save(string name,Transform parent,Material material)
            {
                // These materials also serve long box roads; undo their box tiling for world-space meshes.
                Vector2 scale=material.GetTextureScale("_BaseMap");
                for(int i=0;i<uv.Count;i++)
                {
                    Vector3 p=points[i];
                    Vector2 tile=name=="Roundabout Island"?new Vector2((p.x+10)/20,(p.z-RoundaboutTraffic.CenterZ+10)/20):
                        name=="Roundabout Paving"?new Vector2(p.x/2,p.z/4):new Vector2(p.x/6,p.z/6);
                    uv[i]=new Vector2(tile.x/Mathf.Max(.001f,scale.x),tile.y/Mathf.Max(.001f,scale.y));
                }
                var mesh=new Mesh{name=name};mesh.SetVertices(points);mesh.SetUVs(0,uv);mesh.SetTriangles(indices,0);mesh.RecalculateNormals();mesh.RecalculateBounds();
                AssetDatabase.CreateAsset(mesh,StarterScene.Generated+"/"+name.Replace(" ","")+".asset");
                var obj=new GameObject(name,typeof(MeshFilter),typeof(MeshRenderer));obj.transform.SetParent(parent,false);obj.GetComponent<MeshFilter>().sharedMesh=mesh;
                var view=obj.GetComponent<MeshRenderer>();view.sharedMaterial=material;view.shadowCastingMode=ShadowCastingMode.Off;
            }
        }

        static void Gardens(Transform road)
        {
            Transform root=new GameObject("Roundabout Gardens").transform;root.SetParent(road,false);
            var soil=new Shape();var bark=new Shape();var flowers=new Shape();var green=new[]{new Shape(),new Shape(),new Shape()};
            Material Existing(string name)=>AssetDatabase.LoadAssetAtPath<Material>(StarterScene.Generated+"/"+name.Replace(" ","")+".mat");
            void Marker(string name,Vector3 p,Vector3 size)
            {var obj=new GameObject(name);obj.transform.SetParent(root,false);obj.transform.localPosition=p;obj.transform.localScale=size;}
            void Bed(Vector3 p,float x,float z,bool island)
            {
                // A low octagonal soil basin leaves the grass visible around each planted group.
                Marker(island?"Island Planting Bed":"Outer Planting Bed",p,new Vector3(x,.04f,z));
                for(int n=0;n<8;n++)
                {
                    float a=n*Mathf.PI/4,b=(n+1)*Mathf.PI/4;
                    soil.Triangle(p,p+new Vector3(Mathf.Cos(b)*x,0,Mathf.Sin(b)*z),p+new Vector3(Mathf.Cos(a)*x,0,Mathf.Sin(a)*z));
                }
            }
            void Crown(Vector3 p,Vector3 radius,int colour)
            {
                // Six-sided rounded cap: low-cost opaque geometry, no alpha cards or realtime lights.
                const int sides=6;Vector3 top=p+Vector3.up*radius.y;
                for(int n=0;n<sides;n++)
                {
                    float a=n*Mathf.PI*2/sides,b=(n+1)*Mathf.PI*2/sides;
                    Vector3 u=p+new Vector3(Mathf.Cos(a)*radius.x,0,Mathf.Sin(a)*radius.z),v=p+new Vector3(Mathf.Cos(b)*radius.x,0,Mathf.Sin(b)*radius.z);
                    green[colour].Triangle(top,v,u);
                    green[colour].Triangle(p-Vector3.up*radius.y*.5f,u,v);
                }
            }
            float z=RoundaboutTraffic.CenterZ;
            Vector3[] island={new Vector3(-4,.252f,z-2.6f),new Vector3(3.5f,.252f,z-3),new Vector3(0,.252f,z+3)};
            for(int bed=0;bed<island.Length;bed++)
            {
                Vector3 p=island[bed];Bed(p,2.2f,1.5f,true);
                for(int n=0;n<3;n++)
                {
                    Vector3 at=p+new Vector3((n-1)*1.05f,.24f,Mathf.Sin(n*2+bed)*.4f);
                    Vector3 radius=new Vector3(.78f,.36f+n*.075f,.68f);Crown(at,radius,(bed+n)%3);
                    Marker("Island Shrub",at,radius);
                }
                for(int n=0;n<8;n++)
                {
                    float a=(n+.3f*bed)*Mathf.PI/4;Vector3 at=p+new Vector3(Mathf.Cos(a)*1.8f,.025f,Mathf.Sin(a)*1.05f);
                    float size=.15f+(n%3)*.035f;
                    flowers.Quad(at+new Vector3(-size,0,0),at+new Vector3(0,.065f,size),at+new Vector3(size,0,0),at+new Vector3(0,.065f,-size));
                    Marker("Island Flower Cluster",at,new Vector3(size,1,size));
                }
            }
            // The four diagonal land parcels are outside the ring, flared entries, and paved walking bands.
            for(int i=0;i<4;i++)
            {
                float side=i%2==0?-1:1,back=i<2?-1:1;
                Vector3 p=new Vector3(side*27,-.17f,z+back*26);Bed(p,4.4f,3.2f,false);
                for(int n=0;n<2;n++)
                {
                    Vector3 at=p+new Vector3(side*(n*2-1),.43f,back*1.65f);Vector3 radius=new Vector3(1.15f,.57f+n*.18f,.94f);
                    Crown(at,radius,(i+n)%3);Marker("Outer Shrub",at,radius);
                }
                Vector3 tree=p+new Vector3(-side,0,-back*.75f);float height=2.65f+(i%2)*.55f;
                Marker("Garden Tree",tree,new Vector3(1,height,1));
                for(int n=0;n<4;n++)
                {
                    float a=n*Mathf.PI/2,b=(n+1)*Mathf.PI/2;
                    Vector3 u=new Vector3(Mathf.Cos(a)*.14f,0,Mathf.Sin(a)*.14f),v=new Vector3(Mathf.Cos(b)*.14f,0,Mathf.Sin(b)*.14f);
                    bark.Quad(tree+u,tree+u+Vector3.up*height,tree+v+Vector3.up*height,tree+v);
                }
                Crown(tree+new Vector3(-.6f,height,0),new Vector3(1.3f,1.1f,1.1f),i%3);
                Crown(tree+new Vector3(.55f,height+.2f,.3f),new Vector3(1.15f,1.2f,1.2f),(i+1)%3);
            }
            soil.Save("Garden Soil",root,Existing("Soil"));bark.Save("Garden Trunks",root,Existing("Bark"));flowers.Save("Garden Flowers",root,Existing("Road White"));
            for(int i=0;i<green.Length;i++)green[i].Save("Garden Foliage "+i,root,Existing("Leaves "+i));
        }
        static Vector3 Circle(float r,float angle,float y)=>new Vector3(Mathf.Cos(angle)*r,y,RoundaboutTraffic.CenterZ+Mathf.Sin(angle)*r);
        internal static void Create(Transform road)
        {
            Transform root=new GameObject("Roundabout Terrain").transform;root.SetParent(road,false);
            var asphalt=new Shape();var paving=new Shape();var curb=new Shape();var lawn=new Shape();var white=new Shape();
            const int segments=24;float z=RoundaboutTraffic.CenterZ;
            for(int i=0;i<segments;i++)
            {
                float a=i*Mathf.PI*2/segments,b=(i+1)*Mathf.PI*2/segments;
                asphalt.Triangle(new Vector3(0,.008f,z),Circle(19.5f,b,.008f),Circle(19.5f,a,.008f));
                lawn.Triangle(new Vector3(0,.235f,z),Circle(10,b,.235f),Circle(10,a,.235f));
                curb.Quad(Circle(10,a,.25f),Circle(10,b,.25f),Circle(10.28f,b,.25f),Circle(10.28f,a,.25f));
                curb.Quad(Circle(10.28f,a,.25f),Circle(10.28f,b,.25f),Circle(10.28f,b,0),Circle(10.28f,a,0));
            }
            asphalt.Rect(-160,-13,z-6.4f,z+6.4f,.006f);asphalt.Rect(13,160,z-6.4f,z+6.4f,.006f);
            asphalt.Rect(-160,160,295.6f,308.4f,.006f);
            asphalt.Quad(new Vector3(-6.4f,.006f,275),new Vector3(-35,.006f,302),new Vector3(35,.006f,302),new Vector3(6.4f,.006f,275));
            // Support the full swept river-road bend, including the long bus body, beyond the central flare.
            foreach(float side in new[]{-1f,1f})
            {
                Vector3 Bend(float t)
                {float u=1-t;return new Vector3(side*(u*u*u*4.8f+3*u*u*t*4.8f+3*u*t*t*80+t*t*t*140),.006f,u*u*u*275+3*u*u*t*295+3*u*t*t*302+t*t*t*302);}
                Vector3 Edge(float t)
                {float u=1-t;Vector3 tangent=new Vector3(side*(6*u*t*75.2f+3*t*t*60),0,3*u*u*20+6*u*t*7);return new Vector3(-tangent.z,0,tangent.x).normalized*6.4f;}
                for(int n=0;n<16;n++)
                {float a=n/16f,b=(n+1)/16f;Vector3 p=Bend(a),q=Bend(b),s=Edge(a),t=Edge(b);asphalt.Quad(p+s,q+t,q-t,p-s);}
            }
            for(int port=0;port<4;port++)
            {
                Vector3 P(float x,float v,float y){Vector2 p=new Vector2(x,v);for(int k=0;k<port;k++)p=new Vector2(-p.y,p.x);return new Vector3(p.x,y,z+p.y);}
                // Adjacent approach pavements meet at the same diagonal ring corner, without stray stubs.
                float inside=19.5f/Mathf.Sqrt(2),outside=21.5f/Mathf.Sqrt(2),lip=19.75f/Mathf.Sqrt(2);
                asphalt.Quad(P(-6.4f,-40,.006f),P(-inside,-inside,.006f),P(inside,-inside,.006f),P(6.4f,-40,.006f));
                paving.Quad(P(-10.4f,-40,.15f),P(-outside,-outside,.15f),P(-inside,-inside,.15f),P(-6.4f,-40,.15f));
                paving.Quad(P(6.4f,-40,.15f),P(inside,-inside,.15f),P(outside,-outside,.15f),P(10.4f,-40,.15f));
                curb.Quad(P(-6.65f,-40,.17f),P(-lip,-lip,.17f),P(-inside,-inside,.17f),P(-6.4f,-40,.17f));
                curb.Quad(P(6.4f,-40,.17f),P(inside,-inside,.17f),P(lip,-lip,.17f),P(6.65f,-40,.17f));
            }
            foreach(float sign in new[]{-1f,1f})
            {
                float x0=sign<0?-160:40,x1=sign<0?-40:160;
                foreach(float edge in new[]{-1f,1f})
                {
                    float band=z+edge*7.4f;
                    paving.Rect(x0,x1,band-1,band+1,.15f);
                    float at=z+edge*6.52f;curb.Rect(x0,x1,at-.12f,at+.12f,.17f);
                }
                // Continue the main-road pavements around the roundabout and to the river road.

            }
            foreach(float edge in new[]{-1f,1f})
            {
                float p=302+edge*7.4f,at=302+edge*6.52f;
                if(edge<0)
                {paving.Rect(-160,-90,p-1,p+1,.15f);paving.Rect(90,160,p-1,p+1,.15f);curb.Rect(-160,-90,at-.12f,at+.12f,.17f);curb.Rect(90,160,at-.12f,at+.12f,.17f);}
                else{paving.Rect(-160,160,p-1,p+1,.15f);curb.Rect(-160,160,at-.12f,at+.12f,.17f);}
            }
            // Yield teeth at all four entries. The ring's single lane has no confusing centre line.
            for(int port=0;port<4;port++)for(int n=0;n<3;n++)
            {
                float x=1.1f+n*1.35f;Vector3 P(float u,float v){Vector2 p=new Vector2(u,v);for(int k=0;k<port;k++)p=new Vector2(-p.y,p.x);return new Vector3(p.x,.022f,z+p.y);}
                white.Triangle(P(x-.4f,-23),P(x+.4f,-23),P(x,-23.8f));
            }
            // Small clockwise-facing arrows are avoided: these arrows follow the actual CCW tangent.
            for(int port=0;port<4;port++)
            {
                float angle=(port*90+20)*Mathf.Deg2Rad;Vector3 p=Circle(14,angle,.022f);
                Vector3 forward=new Vector3(-Mathf.Sin(angle),0,Mathf.Cos(angle)),side=new Vector3(forward.z,0,-forward.x);
                white.Quad(p-forward*1.1f-side*.12f,p+forward*.5f-side*.12f,p+forward*.5f+side*.12f,p-forward*1.1f+side*.12f);
                white.Triangle(p+forward*1.3f,p+forward*.35f+side*.6f,p+forward*.35f-side*.6f);
            }
            Material Existing(string name)=>AssetDatabase.LoadAssetAtPath<Material>(StarterScene.Generated+"/"+name.Replace(" ","")+".mat");
            asphalt.Save("Roundabout Asphalt",root,Existing("Asphalt"));paving.Save("Roundabout Paving",root,Existing("Paving"));
            curb.Save("Roundabout Curb",root,Existing("Curb"));lawn.Save("Roundabout Island",root,Existing("Leaves 0"));white.Save("Roundabout Markings",root,Existing("Road White"));
            Gardens(road);
            // Reuse the existing lens/metal and pooled glow; retain four realtime lights.
            Transform lighting=new GameObject("Roundabout Lighting").transform;lighting.SetParent(road,false);
            foreach(float side in new[]{-1f,1f})
            {
                Transform lamp=new GameObject("Roundabout Lamp").transform;lamp.SetParent(lighting,false);lamp.localPosition=new Vector3(side*18.5f,.15f,z-15.5f);
                void Box(string name,Vector3 centre,Vector3 size,Material material)
                {
                    var obj=GameObject.CreatePrimitive(PrimitiveType.Cube);obj.name=name;obj.transform.SetParent(lamp,false);obj.transform.localPosition=centre;obj.transform.localScale=size;
                    UnityEngine.Object.DestroyImmediate(obj.GetComponent<Collider>());var renderer=obj.GetComponent<MeshRenderer>();renderer.sharedMaterial=material;renderer.shadowCastingMode=ShadowCastingMode.Off;
                }
                Box("Roundabout Pole",new Vector3(0,2.3f,0),new Vector3(.17f,4.6f,.17f),Existing("Street Metal"));
                Box("Roundabout Arm",new Vector3(-side*.5f,4.6f,0),new Vector3(1.1f,.13f,.2f),Existing("Street Metal"));
                Box("Lamp Lens",new Vector3(-side,4.5f,0),new Vector3(.5f,.05f,.3f),Existing("Lamp Lens"));
            }
        }
    }
}
