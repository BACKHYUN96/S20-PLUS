using System;
using System.Collections.Generic;
using UnityEditor;
using UnityEngine;
using UnityEngine.Rendering;

namespace PixelTraffic.UnityPrototype.Editor
{
    internal static class GroundScene
    {
        internal static void SurfaceTexture(Material material,string name,int style,Vector2 scale)
        {
            int size=style==0?512:style==1?256:128;
            var pixels=new Color[size*size];var normal=style==0?new Color[size*size]:null;
            for(int y=0;y<size;y++)for(int x=0;x<size;x++)
            {
                int px=x%(size-1),py=y%(size-1);
                float grain=((px*73+py*179+px*py*13)%101)/100f;
                float macro=Periodic(x,y,size,5),shade=.69f+macro*.22f+grain*.09f;
                if(style==1)
                {
                    int row=py/32,cx=(px+(row%2)*32)%64;
                    bool joint=cx<2||py%32<2;
                    float stone=.87f+((px/64+row*3)%5)*.022f;
                    shade=joint?.52f:stone+grain*.025f;
                }
                else if(style==3)shade=py<2?.55f:.89f+grain*.065f;
                pixels[y*size+x]=new Color(shade,shade,shade);
                if(normal!=null)
                {
                    float nx=(Periodic(x-1,y,size,28)-Periodic(x+1,y,size,28))*.4f;
                    float ny=(Periodic(x,y-1,size,28)-Periodic(x,y+1,size,28))*.4f;
                    Vector3 n=new Vector3(nx,ny,1).normalized;
                    normal[y*size+x]=new Color(n.x*.5f+.5f,n.y*.5f+.5f,n.z*.5f+.5f,1);
                }
            }
            material.SetTexture("_BaseMap",SaveTexture(name,pixels,size,false));material.SetTextureScale("_BaseMap",scale);
            if(normal!=null)
            {
                material.SetTexture("_BumpMap",SaveTexture("AsphaltNormal",normal,size,true));
                material.SetFloat("_BumpScale",.35f);material.EnableKeyword("_NORMALMAP");
            }
            EditorUtility.SetDirty(material);
        }
        private static float Periodic(int x,int y,int size,float frequency)
        {
            float u=Mathf.Repeat(x/(float)(size-1),1),v=Mathf.Repeat(y/(float)(size-1),1);
            float a=Mathf.PerlinNoise(12+u*frequency,29+v*frequency),b=Mathf.PerlinNoise(12+(u-1)*frequency,29+v*frequency);
            float c=Mathf.PerlinNoise(12+u*frequency,29+(v-1)*frequency),d=Mathf.PerlinNoise(12+(u-1)*frequency,29+(v-1)*frequency);
            return Mathf.Lerp(Mathf.Lerp(a,b,u),Mathf.Lerp(c,d,u),v);
        }
        private static Texture2D SaveTexture(string name,Color[] pixels,int size,bool linear)
        {
            string path=StarterScene.Generated+"/"+name+".asset";
            var previous=AssetDatabase.LoadAssetAtPath<Texture2D>(path);if(previous!=null)return previous;
            var texture=new Texture2D(size,size,TextureFormat.RGBA32,true,linear){name=name,filterMode=FilterMode.Trilinear,wrapMode=TextureWrapMode.Repeat,anisoLevel=4};
            texture.SetPixels(pixels);texture.Apply(true,false);AssetDatabase.CreateAsset(texture,path);return texture;
        }
        internal static void Manhole(Transform parent,Vector3 position,Material material)
        {
            string path=StarterScene.Generated+"/ManholeDisc.asset";var mesh=AssetDatabase.LoadAssetAtPath<Mesh>(path);
            if(mesh==null)
            {
                var p=new Vector3[33];var uv=new Vector2[33];uv[0]=new Vector2(.5f,.5f);var triangles=new int[96];
                for(int i=0;i<32;i++)
                {
                    float angle=i*Mathf.PI*2/32;p[i+1]=new Vector3(Mathf.Cos(angle)*.5f,0,Mathf.Sin(angle)*.5f);uv[i+1]=new Vector2(p[i+1].x+.5f,p[i+1].z+.5f);
                    triangles[i*3]=0;triangles[i*3+1]=(i+1)%32+1;triangles[i*3+2]=i+1;
                }
                mesh=new Mesh{name="Manhole Disc",vertices=p,uv=uv,triangles=triangles};mesh.RecalculateNormals();mesh.RecalculateBounds();AssetDatabase.CreateAsset(mesh,path);
            }
            var pixels=new Color[128*128];
            for(int y=0;y<128;y++)for(int x=0;x<128;x++)
            {
                float radius=new Vector2(x/127f-.5f,y/127f-.5f).magnitude;
                bool ring=radius>.44f&&radius<.48f||radius>.36f&&radius<.39f;
                float h=ring?.42f:x%12<2||y%12<2?.53f:.92f;
                pixels[y*128+x]=new Color(h,h,h);
            }
            material.SetTexture("_BaseMap",SaveTexture("ManholeFinish",pixels,128,false));EditorUtility.SetDirty(material);
            var r=Part("Manhole",parent,mesh,material);r.transform.position=position;r.transform.localScale=new Vector3(.7f,1,.7f);
        }
        internal static void Create()
        {
            var root=new GameObject("Ground Finish").transform;
            Material curb=AssetDatabase.LoadAssetAtPath<Material>(StarterScene.Generated+"/Curb.mat");SurfaceTexture(curb,"CurbFinish",3,new Vector2(1,210));
            Shader shader=Shader.Find("PixelTraffic/RoadOverlay");if(shader==null)throw new InvalidOperationException("Road overlay shader missing.");
            var mat=new Material(shader){name="Ground Overlay"};AssetDatabase.CreateAsset(mat,StarterScene.Generated+"/GroundOverlay.mat");
            var cars=UnityEngine.Object.FindObjectsByType<PrototypeDrive>(FindObjectsSortMode.None);Array.Sort(cars,(a,b)=>string.CompareOrdinal(a.name,b.name));
            var contact=new Geometry();
            foreach(var car in cars)
            {
                Bounds b=car.transform.Find("Sculpted Body").GetComponent<MeshFilter>().sharedMesh.bounds;
                contact.CarQuad(car.transform,Vector3.zero,b.size.x*.93f,b.size.z*.92f,new Color(.30f,0,0,0));
                foreach(var wheel in car.Wheels)contact.CarQuad(car.transform,wheel.localPosition,.48f,.62f,new Color(.62f,0,0,0));
                contact.CarQuad(car.transform,new Vector3(0,0,b.max.z+2.8f),b.size.x*.86f,5.2f,new Color(.95f,.91f,.68f,1));
                contact.CarQuad(car.transform,new Vector3(0,0,b.min.z-1.8f),b.size.x*.83f,3.2f,new Color(.62f,.035f,.016f,1));
            }
            Renderer fleet=Part("Fleet Ground Contacts",root,contact.Save("FleetGround"),mat);
            var pools=new Geometry();
            foreach(float side in new[]{-1f,1f})for(int i=0;i<10;i++)
                pools.Quad(new Vector3(side*5.25f,.040f,-19+i*20),1.25f,8,new Color(1,.64f,.28f,1));
            Renderer reflected=Part("Wet Lamp Reflections",root,pools.Save("WetLampReflections"),mat);
            var tracks=new Geometry();
            for(int lane=0;lane<4;lane++)foreach(float side in new[]{-1f,1f})
                tracks.Quad(new Vector3((lane-1.5f)*3.2f+side*.76f,.038f,145),.25f,420,new Color(0,0,0,2));
            Renderer worn=Part("Faint Tyre Tracks",root,tracks.Save("TyreTracks"),mat);
            var grate=StarterScene.Surface("Drain Iron",new Color(.20f,.22f,.23f),.18f,.25f);var pixels=new Color[128*128];
            for(int y=0;y<128;y++)for(int x=0;x<128;x++){float shade=x<6||x>121||y<6||y>121?.65f:x%16<4?.21f:.92f;pixels[y*128+x]=new Color(shade,shade,shade);}
            grate.SetTexture("_BaseMap",SaveTexture("DrainFinish",pixels,128,false));EditorUtility.SetDirty(grate);
            var drains=new Geometry();foreach(float side in new[]{-1f,1f})for(int i=0;i<12;i++)drains.Quad(new Vector3(side*6.08f,.015f,-22+i*30),.42f,1.05f,Color.white);
            Part("Curb Drain Covers",root,drains.Save("CurbDrainCovers"),grate);
            root.gameObject.AddComponent<RoadSurface>().Configure(fleet.GetComponent<MeshFilter>(),new[]{fleet,reflected,worn},mat,cars);
        }
        private static Renderer Part(string name,Transform parent,Mesh mesh,Material material)
        {
            var go=new GameObject(name,typeof(MeshFilter),typeof(MeshRenderer));go.transform.SetParent(parent,false);go.GetComponent<MeshFilter>().sharedMesh=mesh;
            var renderer=go.GetComponent<MeshRenderer>();renderer.sharedMaterial=material;renderer.shadowCastingMode=ShadowCastingMode.Off;renderer.receiveShadows=false;
            renderer.lightProbeUsage=LightProbeUsage.Off;renderer.reflectionProbeUsage=ReflectionProbeUsage.Off;return renderer;
        }
        internal static Report Validate()
        {
            var ground=UnityEngine.Object.FindFirstObjectByType<RoadSurface>();Need(ground!=null,"Road finish controller missing.");
            var root=GameObject.Find("Ground Finish");Need(root.GetComponentsInChildren<Renderer>().Length==4,"Ground creates unbounded renderers.");
            Need(root.GetComponentsInChildren<Light>().Length==0&&root.GetComponentsInChildren<Collider>().Length==0,"Ground finish adds realtime lights or obstacles.");
            Material asset=AssetDatabase.LoadAssetAtPath<Material>(StarterScene.Generated+"/GroundOverlay.mat");Vector4 defaults=asset.GetVector("_Ground");
            var cars=UnityEngine.Object.FindObjectsByType<PrototypeDrive>(FindObjectsSortMode.None);Array.Sort(cars,(a,b)=>string.CompareOrdinal(a.name,b.name));
            Need(cars.Length==24&&ground.VehicleCount==24,"Ground fleet mapping incomplete.");ground.Sync(true);
            Need(ground.RuntimeMaterial!=asset&&ground.RuntimeMesh!=AssetDatabase.LoadAssetAtPath<Mesh>(StarterScene.Generated+"/FleetGround.asset"),"Ground mutates shared material/mesh asset.");
            Need(ground.RuntimeMesh.vertexCount==24*28&&ground.RuntimeMesh.triangles.Length/3==336,"Ground fleet quad budget changed.");
            Vector3[] positions=new Vector3[cars.Length];Quaternion[] rotations=new Quaternion[cars.Length];
            for(int i=0;i<cars.Length;i++){positions[i]=cars[i].transform.position;rotations[i]=cars[i].transform.rotation;}
            int contactSamples=0;
            try
            {
                foreach(float yaw in new[]{-8f,0,8f})
                {
                    for(int i=0;i<cars.Length;i++){cars[i].transform.position=positions[i]+new Vector3(.25f,0,.6f);cars[i].transform.rotation=rotations[i]*Quaternion.Euler(0,yaw,0);}
                    ground.Sync(true);Vector3[] vertices=ground.RuntimeMesh.vertices;
                    for(int i=0;i<cars.Length;i++)for(int wheel=0;wheel<4;wheel++)
                    {
                        int first=i*28+4+wheel*4;Vector3 centre=Vector3.zero;
                        for(int corner=0;corner<4;corner++){Vector3 p=vertices[first+corner];Need(float.IsFinite(p.x)&&float.IsFinite(p.z)&&Mathf.Abs(p.y-.042f)<.00001f,"Ground contacts float or become nonfinite.");centre+=p*.25f;}
                        Vector3 actual=cars[i].Wheels[wheel].position;actual.y=.042f;
                        Need(Vector3.Distance(centre,actual)<.0001f,"Contact does not follow actual wheel/turned vehicle.");contactSamples++;
                    }
                }
                Vector3[] before=ground.RuntimeMesh.vertices;Vector4 parameters=ground.RuntimeMaterial.GetVector("_Ground");
                cars[0].transform.position+=Vector3.forward;UnityEngine.Object.FindFirstObjectByType<CityClimate>().Advance(.1,true);ground.Sync(false);
                Vector3[] after=ground.RuntimeMesh.vertices;for(int i=0;i<before.Length;i++)Need(before[i]==after[i],"Hidden ground mesh advances.");
                Need(parameters==ground.RuntimeMaterial.GetVector("_Ground"),"Hidden wet reflection clock advances.");
                cars[0].gameObject.SetActive(false);ground.Sync(true);after=ground.RuntimeMesh.vertices;
                for(int v=0;v<28;v++)Need(after[v]==Vector3.zero,"Hidden vehicle leaves a shadow/reflection ghost.");cars[0].gameObject.SetActive(true);
            }
            finally
            {
                for(int i=0;i<cars.Length;i++){cars[i].gameObject.SetActive(true);cars[i].transform.SetPositionAndRotation(positions[i],rotations[i]);}
                UnityEngine.Object.FindFirstObjectByType<CityClimate>().Preview(0,0);ground.Sync(true);
            }
            Need(asset.GetVector("_Ground")==defaults,"Ground preview changes material defaults.");
            int textures=0;foreach(string name in new[]{"Asphalt","AsphaltNormal","Pavers","CurbFinish","ManholeFinish","DrainFinish"})
            {
                var texture=AssetDatabase.LoadAssetAtPath<Texture2D>(StarterScene.Generated+"/"+name+".asset");
                Need(texture!=null&&texture.mipmapCount>1&&texture.filterMode==FilterMode.Trilinear&&texture.anisoLevel==4,"Ground texture loses mip/anisotropic filtering.");
                if(name=="Asphalt"||name=="AsphaltNormal")for(int t=0;t<texture.width;t+=16)
                {
                    Need(((Vector4)(texture.GetPixel(0,t)-texture.GetPixel(texture.width-1,t))).sqrMagnitude<.0001f,"Asphalt has a repeating seam.");
                    Need(((Vector4)(texture.GetPixel(t,0)-texture.GetPixel(t,texture.height-1))).sqrMagnitude<.0001f,"Asphalt has a repeating seam.");
                }
                textures++;
            }
            Mesh drains=GameObject.Find("Curb Drain Covers").GetComponent<MeshFilter>().sharedMesh;Need(drains.triangles.Length/3==48,"Drain cover budget differs.");
            foreach(var p in drains.vertices)Need(Mathf.Abs(p.x)>5.8f&&Mathf.Abs(p.x)<6.4f,"Drain leaves road edge or blocks sidewalk.");
            var climate=UnityEngine.Object.FindFirstObjectByType<CityClimate>();climate.Preview(2,2);ground.Sync(true);var wet=ground.RuntimeMaterial.GetVector("_Ground");
            Need(wet.x>.99f&&wet.y>.99f&&wet.z==0,"Wet nighttime reflection mapping missing.");
            climate.Preview(0,0);ground.Sync(true);Need(ground.RuntimeMaterial.GetVector("_Ground").x==0,"Dry road retains wet reflections.");
            return new Report{result="PASS: six mipmapped textures, seamless asphalt, four merged ground renderers, actual wheel-following contacts at three yaw poses, hidden/disabled ghost prevention, one runtime material and dry/wet mapping; GPU appearance/device test separate",vehicles=cars.Length,contactSamples=contactSamples,textures=textures,renderers=4,fleetTriangles=336,drains=24,reflectionPools=20};
        }
        private static void Need(bool condition,string why){if(!condition)throw new InvalidOperationException(why);}
        [Serializable] internal sealed class Report
        {public string result;public int vehicles,contactSamples,textures,renderers,fleetTriangles,drains,reflectionPools;}
        private sealed class Geometry
        {
            private readonly List<Vector3> p=new List<Vector3>();private readonly List<Vector2> uv=new List<Vector2>();private readonly List<Color> colours=new List<Color>();private readonly List<int> t=new List<int>();
            internal void Quad(Vector3 centre,float width,float length,Color color)=>CarQuad(null,centre,width,length,color);
            internal void CarQuad(Transform car,Vector3 centre,float width,float length,Color color)
            {
                int first=p.Count;for(int corner=0;corner<4;corner++)
                {
                    Vector3 v=centre+new Vector3((corner==1||corner==2?1:-1)*width*.5f,0,(corner>=2?1:-1)*length*.5f);
                    if(car!=null){v=car.TransformPoint(v);v.y=.042f;}p.Add(v);uv.Add(new Vector2(corner==1||corner==2?1:0,corner>=2?1:0));colours.Add(color);
                }
                t.AddRange(new[]{first,first+2,first+1,first,first+3,first+2});
            }
            internal Mesh Save(string name)
            {
                var mesh=new Mesh{name=name};mesh.SetVertices(p);mesh.SetUVs(0,uv);mesh.SetColors(colours);mesh.SetTriangles(t,0);mesh.RecalculateNormals();mesh.RecalculateBounds();AssetDatabase.CreateAsset(mesh,StarterScene.Generated+"/"+name+".asset");return mesh;
            }
        }
    }
}
