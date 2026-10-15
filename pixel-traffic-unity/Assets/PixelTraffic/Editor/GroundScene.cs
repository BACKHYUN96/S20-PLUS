using System;
using System.Collections.Generic;
using UnityEditor;
using UnityEngine;
using UnityEngine.Rendering;
using UnityEngine.Rendering.Universal;

namespace PixelTraffic.UnityPrototype.Editor
{
    internal static class GroundScene
    {
        internal static void SurfaceTexture(Material material,string name,int style,Vector2 scale)
        {
            int size=style==0?512:style==1?512:256;
            var pixels=new Color[size*size];var normal=style==0?new Color[size*size]:null;
            for(int y=0;y<size;y++)for(int x=0;x<size;x++)
            {
                int px=x%(size-1),py=y%(size-1);
                float grain=((px*73+py*179+px*py*13)%101)/100f;
                float macro=Periodic(x,y,size,5),shade=.80f+macro*.12f;
                Color tint=Color.white;
                if(style==0)
                {
                    // Fine exposed aggregate with sparse light stones, rather than uniform image noise.
                    float aggregate=Periodic(x,y,size,64);
                    shade+=(aggregate-.5f)*.11f+(grain-.5f)*.035f;
                    if(grain>.965f&&aggregate>.51f)shade+=.11f;
                    if(grain<.035f)shade-=.07f;
                    tint=new Color(1.0f,.992f,.977f);
                }
                if(style==1)
                {
                    // Four by eight staggered blocks per 2.4m tile: 60cm x 30cm paving.
                    int row=py/64,cx=(px+(row%2)*64)%128,cy=py%64;
                    int column=(px+(row%2)*64)/128;
                    float edge=Mathf.Min(Mathf.Min(cx,127-cx),Mathf.Min(cy,63-cy));
                    float stone=.84f+((column+row*3)%5)*.021f;
                    shade=edge<2?.56f:edge<5?stone-.09f:stone+(grain-.5f)*.04f+(macro-.5f)*.035f;
                    tint=((column+row)%7==0)?new Color(.96f,.97f,1):new Color(1,.995f,.975f);
                }
                else if(style==3)
                {
                    float joint=Mathf.Min(py,size-1-py);
                    float edge=Mathf.Min(px,size-1-px);
                    shade=joint<3?.49f:joint<6?.76f:.91f+(grain-.5f)*.055f;
                    // A narrow worn rim and dark contact edge stay on the existing curb mesh.
                    if(edge<14)shade*=Mathf.Lerp(.76f,1,edge/14f);
                    tint=new Color(1,.996f,.98f);
                }
                pixels[y*size+x]=new Color(shade*tint.r,shade*tint.g,shade*tint.b);
                if(normal!=null)
                {
                    float nx=(Periodic(x-1,y,size,64)-Periodic(x+1,y,size,64))*.32f;
                    float ny=(Periodic(x,y-1,size,64)-Periodic(x,y+1,size,64))*.32f;
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
            Material curb=AssetDatabase.LoadAssetAtPath<Material>(StarterScene.Generated+"/Curb.mat");SurfaceTexture(curb,"CurbFinish",3,new Vector2(1,(RoundaboutTraffic.GateZ+65)/1.2f));
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
                Vector3[] before=ground.RuntimeMesh.vertices;Vector4 parameters=ground.RuntimeMaterial.GetVector("_Ground");float heldRain=ground.RuntimeMaterial.GetFloat("_Rain");
                cars[0].transform.position+=Vector3.forward;UnityEngine.Object.FindFirstObjectByType<CityClimate>().Advance(.1,true);ground.Sync(false);
                Vector3[] after=ground.RuntimeMesh.vertices;for(int i=0;i<before.Length;i++)Need(before[i]==after[i],"Hidden ground mesh advances.");
                Need(parameters==ground.RuntimeMaterial.GetVector("_Ground"),"Hidden wet reflection clock advances.");
                Need(heldRain==ground.RuntimeMaterial.GetFloat("_Rain"),"Hidden reflection rain gain advances.");
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
            var asphaltTexture=AssetDatabase.LoadAssetAtPath<Texture2D>(StarterScene.Generated+"/Asphalt.asset");
            var pavingTexture=AssetDatabase.LoadAssetAtPath<Texture2D>(StarterScene.Generated+"/Pavers.asset");
            var curbTexture=AssetDatabase.LoadAssetAtPath<Texture2D>(StarterScene.Generated+"/CurbFinish.asset");
            Need(asphaltTexture.width==512&&pavingTexture.width==512&&curbTexture.width==256,"Street finish texture resolution differs.");
            // Inspect the baked assets and actual renderer scale, not just the pixel generator.
            Need(pavingTexture.GetPixel(64,32).grayscale>pavingTexture.GetPixel(0,0).grayscale+.20f,"Paving joints are not distinct from the block face.");
            Need(curbTexture.GetPixel(128,128).grayscale>curbTexture.GetPixel(128,0).grayscale+.25f,"Curb joints are not distinct from the worn stone.");
            int pavementScales=0;
            foreach(var renderer in UnityEngine.Object.FindObjectsByType<MeshRenderer>(FindObjectsSortMode.None))
                if(renderer.name=="Sidewalk")
                {
                    Vector2 repeat=renderer.sharedMaterial.GetTextureScale("_BaseMap");
                    Need(Mathf.Abs(renderer.transform.localScale.x/repeat.x-2.4f)<.001f&&Mathf.Abs(renderer.transform.localScale.z/repeat.y-2.4f)<.001f,"Paving block scale stretches along the sidewalk.");
                    pavementScales++;
                }
            Need(pavementScales==2,"Expected two physically scaled sidewalks.");
            Mesh drains=GameObject.Find("Curb Drain Covers").GetComponent<MeshFilter>().sharedMesh;Need(drains.triangles.Length/3==48,"Drain cover budget differs.");
            foreach(var p in drains.vertices)Need(Mathf.Abs(p.x)>5.8f&&Mathf.Abs(p.x)<6.4f,"Drain leaves road edge or blocks sidewalk.");
            var climate=UnityEngine.Object.FindFirstObjectByType<CityClimate>();climate.Preview(2,2);ground.Sync(true);var wet=ground.RuntimeMaterial.GetVector("_Ground");
            Need(wet.x>.99f&&wet.y>.99f&&wet.z==0,"Wet nighttime reflection mapping missing.");
            climate.Preview(0,0);ground.Sync(true);Need(ground.RuntimeMaterial.GetVector("_Ground").x==0,"Dry road retains wet reflections.");
            int reflectionStates=0,reflectionTransitions=0;
            foreach(int theme in new[]{0,1,2})foreach(int weather in new[]{0,1,2,3,4,5})
            {
                climate.Preview(theme,weather);ground.Sync(true);var p=ground.RuntimeMaterial.GetVector("_Ground");
                Need(Mathf.Abs(p.x-climate.Wetness)<.00001f&&Mathf.Abs(p.y-climate.LampGain)<.00001f&&Mathf.Abs(ground.RuntimeMaterial.GetFloat("_Rain")-climate.RainGain)<.00001f,"Reflection misses the real weather/light/rain state.");reflectionStates++;
            }
            foreach(int destination in new[]{0,2,3,4})
            {
                climate.Preview(2,destination==2?0:2);ground.Sync(true);climate.Select(2,destination);
                for(int frame=0;frame<40;frame++)
                {
                    float previousWet=ground.RuntimeMaterial.GetVector("_Ground").x,previousRain=ground.RuntimeMaterial.GetFloat("_Rain");climate.Advance(.1,true);ground.Sync(true);
                    float actualWet=ground.RuntimeMaterial.GetVector("_Ground").x,actualRain=ground.RuntimeMaterial.GetFloat("_Rain");
                    Need(float.IsFinite(actualWet)&&actualWet>=0&&actualWet<=1&&Mathf.Abs(actualWet-previousWet)<.08f&&Mathf.Abs(actualRain-previousRain)<.08f,"Reflection wetness/ripple strength jumps during weather blending.");reflectionTransitions++;
                }
            }
            climate.Preview(0,0);ground.Sync(true);Need(asset.GetFloat("_Rain")==0,"Rain preview mutates the shared ground asset.");
            return new Report{result="PASS: seamless aggregate asphalt, physically scaled 60x30cm staggered paving, worn curb joints, four ground renderers, wheel contacts, hidden/disabled prevention, 18 actual wet/light/rain states and 160 continuous weather transition samples; actual GPU shader checks are in preview-result",asphaltPixels=asphaltTexture.width,pavingPixels=pavingTexture.width,curbPixels=curbTexture.width,physicallyScaledSidewalks=pavementScales,vehicles=cars.Length,contactSamples=contactSamples,textures=textures,renderers=4,fleetTriangles=336,drains=24,reflectionPools=20,reflectionStates=reflectionStates,reflectionTransitions=reflectionTransitions};
        }
        private static void Need(bool condition,string why){if(!condition)throw new InvalidOperationException(why);}
        [Serializable] internal sealed class Report
        {public string result;public int asphaltPixels,pavingPixels,curbPixels,physicallyScaledSidewalks;public int vehicles,contactSamples,textures,renderers,fleetTriangles,drains,reflectionPools,reflectionStates,reflectionTransitions;}
        [Serializable]internal sealed class ReflectionGpuReport
        {public string result;public int cases,pixels;public float peak,idleDelta,rainMeanDelta;}
        internal static ReflectionGpuReport CheckReflectionGpu()
        {
            var root=new GameObject("Temporary reflection GPU probe");root.layer=30;
            var mesh=new Mesh{vertices=new[]{new Vector3(-.625f,0,-4),new Vector3(.625f,0,-4),new Vector3(.625f,0,4),new Vector3(-.625f,0,4)},uv=new[]{Vector2.zero,Vector2.right,Vector2.one,Vector2.up},triangles=new[]{0,2,1,0,3,2}};
            mesh.RecalculateBounds();root.AddComponent<MeshFilter>().sharedMesh=mesh;
            var material=new Material(Shader.Find("PixelTraffic/RoadOverlay"));root.AddComponent<MeshRenderer>().sharedMaterial=material;
            var cameraObject=new GameObject("Temporary reflection probe camera");var camera=cameraObject.AddComponent<Camera>();camera.enabled=false;camera.cullingMask=1<<30;
            camera.transform.SetPositionAndRotation(new Vector3(0,10,0),Quaternion.Euler(90,0,0));camera.orthographic=true;camera.orthographicSize=4.5f;camera.aspect=1;camera.nearClipPlane=.1f;camera.farClipPlane=20;
            camera.clearFlags=CameraClearFlags.SolidColor;camera.backgroundColor=Color.clear;camera.allowHDR=false;camera.allowMSAA=false;camera.GetUniversalAdditionalCameraData().renderPostProcessing=false;
            var target=new RenderTexture(256,256,24,RenderTextureFormat.ARGB32);target.Create();var image=new Texture2D(256,256,TextureFormat.RGBA32,false);
            var previous=RenderTexture.active;bool fog=RenderSettings.fog;RenderSettings.fog=false;var report=new ReflectionGpuReport();
            try
            {
                float[] Probe(float wet,float lamps,float snow,float rain,float seconds)
                {
                    material.SetVector("_Ground",new Vector4(wet,lamps,snow,seconds));material.SetFloat("_Rain",rain);
                    RenderPipeline.SubmitRenderRequest(camera,new UniversalRenderPipeline.SingleCameraRequest{destination=target});RenderTexture.active=target;
                    image.ReadPixels(new Rect(0,0,256,256),0,0);image.Apply();Color[] colours=image.GetPixels();var values=new float[colours.Length];
                    for(int i=0;i<values.Length;i++){Color c=target.sRGB?colours[i].linear:colours[i];values[i]=Mathf.Max(c.r,c.g,c.b);Need(float.IsFinite(values[i])&&values[i]>=0&&values[i]<.35f,"Actual reflection shader is nonfinite or washes out the road.");report.peak=Mathf.Max(report.peak,values[i]);}
                    report.cases++;report.pixels+=values.Length;return values;
                }
                float Mean(float[] values){float sum=0;foreach(float value in values)sum+=value;return sum/values.Length;}
                foreach(float kind in new[]{1f,3f})
                {
                    var colour=new Color(1,1,1,kind);mesh.colors=new[]{colour,colour,colour,colour};
                    Need(Mean(Probe(0,1,0,1,0))<.00001f,"Dry road still reflects lamps.");
                    Need(Mean(Probe(1,0,0,1,0))<.00001f,"Unlit road still reflects lamps.");
                    Need(Mean(Probe(1,1,1,1,0))<.00001f,"Snow-covered road retains wet reflection glare.");
                    float[] full=Probe(1,1,0,0,0),idle=Probe(1,1,0,0,10),damp=Probe(.35f,1,0,0,0),rain=Probe(1,1,0,1,0),later=Probe(1,1,0,1,2);
                    Need(Mean(full)>.0001f&&Mean(full)>Mean(damp)*2,"Actual reflection does not respond to road wetness.");
                    float idleDelta=0,rainDelta=0;
                    for(int i=0;i<full.Length;i++){idleDelta=Mathf.Max(idleDelta,Mathf.Abs(full[i]-idle[i]));rainDelta+=Mathf.Abs(rain[i]-later[i]);}
                    rainDelta/=full.Length;Need(idleDelta<.00001f,"Wet road shimmers without rain.");Need(rainDelta>.0000001f&&rainDelta<.035f,"Actual rain reflection is frozen or flickers too strongly.");
                    report.idleDelta=Mathf.Max(report.idleDelta,idleDelta);report.rainMeanDelta=Mathf.Max(report.rainMeanDelta,rainDelta);
                }
                report.result="PASS: actual URP RoadOverlay pixels, dry/unlit/snow removal, bounded wetness gain, rain-only subtle motion and no idle shimmer";return report;
            }
            finally
            {
                RenderSettings.fog=fog;RenderTexture.active=previous;target.Release();UnityEngine.Object.DestroyImmediate(target);UnityEngine.Object.DestroyImmediate(image);
                UnityEngine.Object.DestroyImmediate(cameraObject);UnityEngine.Object.DestroyImmediate(root);UnityEngine.Object.DestroyImmediate(material);UnityEngine.Object.DestroyImmediate(mesh);
            }
        }
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
