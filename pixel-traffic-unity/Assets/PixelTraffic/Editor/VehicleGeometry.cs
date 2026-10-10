using System;
using System.Collections.Generic;
using UnityEditor;
using UnityEngine;

namespace PixelTraffic.UnityPrototype.Editor
{
    // Authored metre-scale meshes. Perspective is supplied by the camera, never by lane scaling.
    internal static class VehicleGeometry
    {
        internal enum Kind { Sedan, SportCoupe, Suv, Taxi }
        internal sealed class Shape
        {
            public Mesh paint, glass, trim, metal, head, tail, tyre, rim;
            public float radius, axle;
        }

        internal static Shape Build(Kind kind)
        {
            bool suv = kind == Kind.Suv, sport = kind == Kind.SportCoupe;
            float half = suv ? .96f : sport ? .95f : .92f;
            float length = suv ? 4.90f : sport ? 4.48f : 4.58f;
            float belt = suv ? 1.08f : sport ? .77f : .91f;
            float roof = suv ? 1.84f : sport ? 1.28f : 1.49f;
            float radius = suv ? .36f : .32f;
            float axle = suv ? 1.48f : 1.37f;
            var paint = new Builder(); var glass = new Builder(); var trim = new Builder();
            var metal = new Builder(); var head = new Builder(); var tail = new Builder();
            float rear = -length / 2, front = length / 2;
            float[] z = { rear, rear + .18f, rear + .65f, -.75f, .65f, front - .52f, front - .12f, front };
            float[] width = { half * .82f, half * .97f, half, half, half, half * .98f, half * .91f, half * .82f };
            float[] heights = { belt * .87f, belt * .98f, belt, belt, belt, belt * .97f, belt * .85f, belt * .81f };
            var rings = new List<Vector3[]>();
            for (int i = 0; i < z.Length; i++)
            {
                float w = width[i], h = heights[i];
                rings.Add(new[] {
                    new Vector3(-w*.88f,.41f,z[i]), new Vector3(-w,.52f,z[i]),
                    new Vector3(-w,h-.10f,z[i]), new Vector3(-w*.85f,h,z[i]), new Vector3(0,h+.024f,z[i]),
                    new Vector3(w*.85f,h,z[i]), new Vector3(w,h-.10f,z[i]),
                    new Vector3(w,.52f,z[i]), new Vector3(w*.88f,.41f,z[i]) });
            }
            paint.Loft(rings);
            float cabinBack = suv ? -1.77f : sport ? -1.24f : -1.37f;
            float cabinFront = suv ? .92f : sport ? .62f : .80f;
            float roofBack = suv ? -1.46f : sport ? -.83f : -.99f;
            float roofFront = suv ? .46f : sport ? -.09f : .17f;
            float baseWidth = half * .83f, roofWidth = half * .69f;
            Vector3 bl = new Vector3(-baseWidth,belt+.008f,cabinBack), br = new Vector3(baseWidth,belt+.008f,cabinBack);
            Vector3 fl = new Vector3(-baseWidth,belt+.008f,cabinFront), fr = new Vector3(baseWidth,belt+.008f,cabinFront);
            Vector3 rbl = new Vector3(-roofWidth,roof-.07f,roofBack), rbr = new Vector3(roofWidth,roof-.07f,roofBack);
            Vector3 rfl = new Vector3(-roofWidth,roof-.07f,roofFront), rfr = new Vector3(roofWidth,roof-.07f,roofFront);
            glass.Quad(fl,fr,rfr,rfl,Vector3.forward);
            glass.Quad(br,bl,rbl,rbr,Vector3.back);
            glass.Quad(bl,fl,rfl,rbl,Vector3.left);
            glass.Quad(fr,br,rbr,rfr,Vector3.right);
            // Crown and tapered shoulders replace the flat box roof without changing its height.
            var roofRings = new List<Vector3[]>();
            for(int i=0;i<3;i++)
            {
                float roofZ=Mathf.Lerp(roofBack-.045f,roofFront+.045f,i*.5f), w=roofWidth+.03f;
                float crown=i==1 ? roof : roof-.016f;
                roofRings.Add(new[]{new Vector3(-w,roof-.08f,roofZ),new Vector3(-w,roof-.05f,roofZ),
                    new Vector3(-w*.78f,crown-.012f,roofZ),new Vector3(0,crown,roofZ),
                    new Vector3(w*.78f,crown-.012f,roofZ),new Vector3(w,roof-.05f,roofZ),
                    new Vector3(w,roof-.08f,roofZ),new Vector3(0,roof-.08f,roofZ)});
            }
            paint.Loft(roofRings);
            foreach (float side in new[]{-1f,1f})
            {
                Vector3 rb = side < 0 ? rbl : rbr, rf = side < 0 ? rfl : rfr;
                Vector3 cb = side < 0 ? bl : br, cf = side < 0 ? fl : fr;
                paint.Beam(cb,rb,.075f,.07f); paint.Beam(cf,rf,.075f,.07f);
                trim.Beam(Vector3.Lerp(cb,cf,.46f),Vector3.Lerp(rb,rf,.46f),.055f,.07f);
                if (suv) paint.Beam(Vector3.Lerp(cb,cf,.20f),Vector3.Lerp(rb,rf,.20f),.07f,.08f);
                metal.Beam(cb+Vector3.up*.015f,cf+Vector3.up*.015f,.035f,.026f);
                trim.Box(new Vector3(side*(half+.015f),.45f,-.05f),new Vector3(.045f,.12f,2.40f));
                float mirrorZ = cabinFront-.20f;
                trim.Box(new Vector3(side*(half+.065f),belt+.12f,mirrorZ),new Vector3(.16f,.055f,.06f));
                paint.Box(new Vector3(side*(half+.10f),belt+.15f,mirrorZ),new Vector3(.18f,.13f,.26f));
                metal.Box(new Vector3(side*(half+.10f),belt+.15f,mirrorZ-.135f),new Vector3(.14f,.06f,.012f));
                foreach (float doorZ in new[]{-.80f,.26f})
                {
                    metal.Box(new Vector3(side*(half+.003f),belt-.17f,doorZ),new Vector3(.018f,.027f,.20f));
                    trim.Box(new Vector3(side*(half+.005f),(.51f+belt)/2,doorZ-.18f),new Vector3(.010f,belt-.52f,.012f));
                }
                foreach(float wheelZ in new[]{-axle,axle})
                {
                    // Raised fender lip keeps the visible tyre edge separate from the body.
                    for(int n=0;n<12;n++)
                    {
                        float a=n*Mathf.PI/12,b=(n+1)*Mathf.PI/12;
                        Vector3 Point(float angle,float r) => new Vector3(side*(half+.112f),radius+Mathf.Sin(angle)*r,wheelZ+Mathf.Cos(angle)*r);
                        trim.Quad(Point(a,radius+.035f),Point(b,radius+.035f),Point(b,radius+.070f),Point(a,radius+.070f),Vector3.right*side);
                    }
                }
                float lampWidth = suv ? .44f : .48f;
                head.Lens(new Vector3(side*.61f,belt*.77f,front+.015f),new Vector3(lampWidth,sport ? .085f : .11f,.025f),true);
                trim.Box(new Vector3(side*.61f,belt*.64f,front+.012f),new Vector3(lampWidth+.05f,.045f,.029f));
                tail.Lens(new Vector3(side*.63f,belt*.78f,rear-.015f),new Vector3(.42f,sport ? .09f : .14f,.025f),false);
                metal.Box(new Vector3(side*.61f,.36f,rear-.04f),new Vector3(.14f,.08f,.13f));
            }
            trim.Box(new Vector3(0,.52f,front+.016f),new Vector3(1.22f,.21f,.03f));
            trim.Box(new Vector3(0,belt*.78f,front+.028f),new Vector3(suv ? .88f : .72f,.17f,.025f));
            for(int i=-3;i<=3;i++) metal.Box(new Vector3(i*.10f,belt*.78f,front+.043f),new Vector3(.023f,.12f,.014f));
            trim.Box(new Vector3(0,.48f,rear-.015f),new Vector3(1.68f,.17f,.028f));
            metal.Box(new Vector3(0,.49f,front+.045f),new Vector3(.37f,.095f,.02f));
            metal.Box(new Vector3(0,.62f,rear-.043f),new Vector3(.37f,.12f,.02f));
            metal.Box(new Vector3(0,belt*.78f,front+.060f),new Vector3(.075f,.05f,.014f));
            // Bonnet crease strips and the roof's dark inset give light a readable upper surface.
            foreach(float side in new[]{-1f,1f})
                paint.Beam(new Vector3(side*.55f,belt+.012f,cabinFront+.04f),new Vector3(side*.47f,belt*.91f,front-.34f),.025f,.018f);
            if(suv)
            {
                foreach(float side in new[]{-1f,1f}) trim.Box(new Vector3(side*.57f,roof+.035f,-.47f),new Vector3(.055f,.07f,1.60f));
                glass.Box(new Vector3(0,roof+.008f,-.20f),new Vector3(.87f,.01f,.65f));
            }
            if(sport)
            {
                foreach(float side in new[]{-1f,1f}) trim.Box(new Vector3(side*.53f,belt+.17f,rear+.40f),new Vector3(.055f,.20f,.09f));
                paint.Box(new Vector3(0,belt+.28f,rear+.40f),new Vector3(1.66f,.055f,.24f));
            }
            if(kind==Kind.Taxi)
            {
                trim.Box(new Vector3(0,roof+.05f,-.35f),new Vector3(.47f,.055f,.23f));
                head.Box(new Vector3(0,roof+.145f,-.35f),new Vector3(.39f,.16f,.17f));
                trim.Box(new Vector3(0,roof+.15f,-.255f),new Vector3(.24f,.035f,.008f));
            }
            var tyre = new Builder(); var rim = new Builder();
            tyre.RoundedTyre(radius,.21f,16);
            rim.Cylinder(radius*.20f,.224f,12);
            foreach(float side in new[]{-1f,1f})
            {
                for(int n=0;n<5;n++)
                {
                    // Thin tapered spokes have visible depth; the hidden inner box faces are omitted.
                    float a=n*72*Mathf.Deg2Rad;
                    Vector3 Point(float angle,float r,float x)=>new Vector3(side*x,Mathf.Cos(angle)*r,Mathf.Sin(angle)*r);
                    Vector3 p=Point(a-.12f,radius*.20f,.125f),q=Point(a+.12f,radius*.20f,.125f);
                    Vector3 v=Point(a+.065f,radius*.70f,.115f),u=Point(a-.065f,radius*.70f,.115f);
                    rim.Quad(p,q,v,u,Vector3.right*side);
                    Vector3 offset=Vector3.right*(-side*.018f);
                    rim.Quad(p,u,u+offset,p+offset,new Vector3(0,-Mathf.Sin(a),Mathf.Cos(a)));
                    rim.Quad(q,q+offset,v+offset,v,new Vector3(0,Mathf.Sin(a),-Mathf.Cos(a)));
                }
                for(int n=0;n<16;n++)
                {
                    float a=n*2*Mathf.PI/16,b=(n+1)*2*Mathf.PI/16;
                    Vector3 Point(float angle,float r)=>new Vector3(side*.12f,Mathf.Cos(angle)*r,Mathf.Sin(angle)*r);
                    rim.Quad(Point(a,radius*.69f),Point(b,radius*.69f),Point(b,radius*.78f),Point(a,radius*.78f),Vector3.right*side);
                }
            }
            string prefix = kind.ToString();
            return new Shape { paint=paint.Save(prefix+"Paint",55),glass=glass.Save(prefix+"Glass"),trim=trim.Save(prefix+"Trim"),
                metal=metal.Save(prefix+"Metal"),head=head.Save(prefix+"Head"),tail=tail.Save(prefix+"Tail"),
                tyre=tyre.Save(prefix+"Tyre",65),rim=rim.Save(prefix+"Rim"),radius=radius,axle=axle };
        }

        private sealed class Builder
        {
            private readonly List<Vector3> vertices = new List<Vector3>();
            private readonly List<int> indices = new List<int>();
            private readonly List<Vector2> uv = new List<Vector2>();
            public void Quad(Vector3 a, Vector3 b, Vector3 c, Vector3 d, Vector3 outward)
            {
                if(Vector3.Dot(Vector3.Cross(b-a,c-a),outward)<0) {Vector3 swap=b;b=d;d=swap;}
                int n=vertices.Count;vertices.Add(a);vertices.Add(b);vertices.Add(c);vertices.Add(d);
                uv.AddRange(new[]{Vector2.zero,Vector2.right,Vector2.one,Vector2.up});
                indices.AddRange(new[]{n,n+1,n+2,n,n+2,n+3});
            }
            public void Triangle(Vector3 a,Vector3 b,Vector3 c,Vector3 outward)
            {
                if(Vector3.Dot(Vector3.Cross(b-a,c-a),outward)<0) {Vector3 swap=b;b=c;c=swap;}
                int n=vertices.Count;vertices.Add(a);vertices.Add(b);vertices.Add(c);indices.AddRange(new[]{n,n+1,n+2});
                uv.AddRange(new[]{new Vector2(.5f,.5f),Vector2.zero,Vector2.one});
            }
            public void Lens(Vector3 center,Vector3 size,bool front)
            {
                var points=new[]{new Vector2(-.40f,-.5f),new Vector2(.40f,-.5f),new Vector2(.5f,-.25f),new Vector2(.5f,.25f),
                    new Vector2(.40f,.5f),new Vector2(-.40f,.5f),new Vector2(-.5f,.25f),new Vector2(-.5f,-.25f)};
                float sign=front ? 1 : -1;
                Vector3 p(int n,float z)=>center+new Vector3(points[n].x*size.x,points[n].y*size.y,z*sign);
                for(int i=0;i<8;i++)
                {
                    int next=(i+1)%8;
                    Triangle(center+Vector3.forward*(sign*size.z/2),p(i,size.z/2),p(next,size.z/2),Vector3.forward*sign);
                    Triangle(center-Vector3.forward*(sign*size.z/2),p(next,-size.z/2),p(i,-size.z/2),Vector3.back*sign);
                    Quad(p(i,-size.z/2),p(next,-size.z/2),p(next,size.z/2),p(i,size.z/2),new Vector3(points[i].x,points[i].y,0));
                }
            }
            public void RoundedTyre(float radius,float width,int count)
            {
                for(int n=0;n<count;n++)
                {
                    float a=n*2*Mathf.PI/count,b=(n+1)*2*Mathf.PI/count;
                    Vector3 p(float angle,float x,float r)=>new Vector3(x,Mathf.Cos(angle)*r,Mathf.Sin(angle)*r);
                    foreach(float side in new[]{-1f,1f})
                    {
                        Quad(p(a,0,radius),p(b,0,radius),p(b,side*width/2,radius*.90f),p(a,side*width/2,radius*.90f),
                            new Vector3(side*.4f,Mathf.Cos((a+b)/2),Mathf.Sin((a+b)/2)));
                        Triangle(new Vector3(side*width/2,0,0),p(a,side*width/2,radius*.90f),p(b,side*width/2,radius*.90f),Vector3.right*side);
                    }
                }
            }
            public void Box(Vector3 center, Vector3 size) => Box(center,size,Quaternion.identity);
            public void Box(Vector3 center,Vector3 size,Quaternion rotation)
            {
                Vector3 p(int x,int y,int z) => center+rotation*Vector3.Scale(new Vector3(x,y,z)*.5f,size);
                Quad(p(-1,-1,1),p(1,-1,1),p(1,1,1),p(-1,1,1),rotation*Vector3.forward);
                Quad(p(1,-1,-1),p(-1,-1,-1),p(-1,1,-1),p(1,1,-1),rotation*Vector3.back);
                Quad(p(1,-1,1),p(1,-1,-1),p(1,1,-1),p(1,1,1),rotation*Vector3.right);
                Quad(p(-1,-1,-1),p(-1,-1,1),p(-1,1,1),p(-1,1,-1),rotation*Vector3.left);
                Quad(p(-1,1,1),p(1,1,1),p(1,1,-1),p(-1,1,-1),rotation*Vector3.up);
                Quad(p(-1,-1,-1),p(1,-1,-1),p(1,-1,1),p(-1,-1,1),rotation*Vector3.down);
            }
            public void Beam(Vector3 a,Vector3 b,float width,float depth)
                => Box((a+b)/2,new Vector3(width,Vector3.Distance(a,b),depth),Quaternion.FromToRotation(Vector3.up,b-a));
            public void Loft(List<Vector3[]> rings)
            {
                for(int j=0;j<rings.Count-1;j++) for(int k=0;k<rings[j].Length;k++)
                {
                    int next=(k+1)%rings[j].Length;
                    Vector3 a=rings[j][k],b=rings[j][next],c=rings[j+1][next],d=rings[j+1][k];
                    Vector3 center=Vector3.zero;foreach(Vector3 v in rings[j])center+=v;
                    foreach(Vector3 v in rings[j+1])center+=v;center/=rings[j].Length*2;
                    Vector3 mid=(a+b+c+d)/4;Quad(a,b,c,d,new Vector3(mid.x-center.x,mid.y-center.y,0));
                }
                foreach(int j in new[]{0,rings.Count-1})
                {
                    Vector3 center=Vector3.zero;foreach(Vector3 v in rings[j])center+=v;center/=rings[j].Length;
                    for(int k=0;k<rings[j].Length;k++) Triangle(center,rings[j][k],rings[j][(k+1)%rings[j].Length],j==0?Vector3.back:Vector3.forward);
                }
            }
            public void Cylinder(float radius,float width,int count)
            {
                for(int n=0;n<count;n++)
                {
                    float a=n*2*Mathf.PI/count,b=(n+1)*2*Mathf.PI/count;
                    Vector3 p(float angle,float x)=>new Vector3(x,Mathf.Cos(angle)*radius,Mathf.Sin(angle)*radius);
                    Quad(p(a,-width/2),p(b,-width/2),p(b,width/2),p(a,width/2),new Vector3(0,Mathf.Cos((a+b)/2),Mathf.Sin((a+b)/2)));
                    foreach(float side in new[]{-1f,1f})
                        Triangle(new Vector3(side*width/2,0,0),p(a,side*width/2),p(b,side*width/2),Vector3.right*side);
                }
            }
            public Mesh Save(string name,float smoothAngle=0)
            {
                var mesh=new Mesh {name=name};mesh.SetVertices(vertices);mesh.SetUVs(0,uv);mesh.SetTriangles(indices,0);mesh.RecalculateNormals();mesh.RecalculateBounds();
                if(smoothAngle>0)
                {
                    // Weld only shading, never positions. Keep panel edges sharper than the threshold.
                    Vector3[] normals=mesh.normals, result=(Vector3[])normals.Clone();
                    var groups=new Dictionary<Vector3Int,List<int>>();
                    for(int i=0;i<vertices.Count;i++)
                    {
                        Vector3 v=vertices[i];var key=new Vector3Int(Mathf.RoundToInt(v.x*10000),Mathf.RoundToInt(v.y*10000),Mathf.RoundToInt(v.z*10000));
                        if(!groups.TryGetValue(key,out var group)){group=new List<int>();groups.Add(key,group);}group.Add(i);
                    }
                    float threshold=Mathf.Cos(smoothAngle*Mathf.Deg2Rad);
                    foreach(var group in groups.Values)foreach(int i in group)
                    {
                        Vector3 sum=Vector3.zero;foreach(int j in group)if(Vector3.Dot(normals[i],normals[j])>threshold)sum+=normals[j];
                        result[i]=sum.normalized;
                    }
                    mesh.normals=result;
                }
                AssetDatabase.CreateAsset(mesh,StarterScene.Generated+"/"+name+".asset");return mesh;
            }
        }
    }
}
