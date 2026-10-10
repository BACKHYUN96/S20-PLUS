using System;
using UnityEditor;
using UnityEngine;

namespace PixelTraffic.UnityPrototype.Editor
{
    internal static class FrontageChecks
    {
        internal static Report Validate()
        {
            var climate=UnityEngine.Object.FindFirstObjectByType<CityClimate>();climate.Initialize();
            Material window=null,shop=null;int buildings=0,windows=0,lit=0,dark=0,warm=0,cool=0;int[] styles=new int[3];
            foreach(var root in UnityEngine.Object.FindObjectsByType<Transform>(FindObjectsSortMode.None))
            {
                if(root.name!="City Building")continue;buildings++;
                Transform glass=root.Find("Window Glass"),display=root.Find("Shop Interior");
                Need(glass!=null&&display!=null&&root.Find("Window Frame")!=null&&root.Find("Shop Frames")!=null,"Merged facade parts missing.");
                var wm=glass.GetComponent<MeshRenderer>().sharedMaterial;var sm=display.GetComponent<MeshRenderer>().sharedMaterial;
                if(window==null){window=wm;shop=sm;}Need(wm==window&&sm==shop,"Facade duplicates a material per building.");
                var mesh=glass.GetComponent<MeshFilter>().sharedMesh;var store=display.GetComponent<MeshFilter>().sharedMesh;
                Geometry(mesh);Geometry(store);Geometry(root.Find("Window Frame").GetComponent<MeshFilter>().sharedMesh);Geometry(root.Find("Shop Frames").GetComponent<MeshFilter>().sharedMesh);
                Need(store.vertexCount==20&&store.triangles.Length/3==10,"Shop surface budget changed.");
                Need(root.Find("Shop Frames").GetComponent<MeshFilter>().sharedMesh.triangles.Length/3==132,"Shop frame budget changed.");
                var atlas=(Texture2D)wm.GetTexture("_EmissionMap");int localLit=0,localDark=0;
                Vector2[] uv=mesh.uv;
                for(int i=0;i<uv.Length;i+=4)
                {
                    Vector2 centre=(uv[i]+uv[i+2])*.5f;Color colour=atlas.GetPixelBilinear(centre.x,centre.y);windows++;
                    if(colour.maxColorComponent<.001f){dark++;localDark++;}else{lit++;localLit++;if(colour.r>colour.b*1.5f)warm++;if(colour.b>colour.r*1.5f)cool++;}
                }
                Need(localLit>0&&localDark>0,"Building has no mixed occupied/dark rooms.");
                Vector2 sign=(store.uv[12]+store.uv[14])*.5f;int style=Mathf.FloorToInt(sign.x*4);Need(style>=0&&style<3,"Shop style UV invalid.");styles[style]++;
                // Text reads from either sidewalk: UV increases toward the viewer's right.
                float outward=Mathf.Sign(glass.localPosition.x+mesh.bounds.center.x);
                // Viewer right is cross(camera up, forward toward the building).
                Vector3 viewerRight=Vector3.Cross(Vector3.up,-Vector3.right*outward);
                Vector3 signEdge=store.vertices[13]-store.vertices[12];
                Need((store.uv[13].x-store.uv[12].x)*Vector3.Dot(signEdge,viewerRight)>0,"Shop sign is mirrored on one side of the road.");
                foreach(Vector3 vertex in store.vertices)
                {
                    Vector3 world=root.TransformPoint(vertex);Need(world.y>2.5f||Mathf.Abs(world.x)>10.6f,"Low shop geometry blocks the pedestrian route.");
                }
                Need(root.GetComponentsInChildren<Collider>().Length==0,"Building frontage introduces primitive colliders.");
            }
            Need(buildings==24&&windows==480&&styles[0]==8&&styles[1]==8&&styles[2]==8&&lit>0&&dark>0&&warm>0&&cool>0,"Facade counts or room variety differ.");
            Map(window,256,128);Map(shop,512,512);
            var originalWindow=AssetDatabase.LoadAssetAtPath<Material>(StarterScene.Generated+"/WindowGlass.mat");
            var originalShop=AssetDatabase.LoadAssetAtPath<Material>(StarterScene.Generated+"/ShopInterior.mat");
            Color originalW=originalWindow.GetColor("_EmissionColor"),originalS=originalShop.GetColor("_EmissionColor");
            int[] rates={15,30,60,120};int cases=0;
            foreach(int fps in rates)
            {
                climate.Preview(0,0);climate.Select(2,0);
                for(int frame=1;frame<=4*fps;frame++)
                {
                    climate.Advance(1d/fps,true);double seconds=(double)frame/fps;
                    double t=seconds<=2?seconds/2:(seconds-2)/2;t=t*t*(3-2*t);
                    double dusk=seconds<=2?t:1-t,night=seconds<=2?0:t;
                    Gain(climate,window,shop,(float)(dusk*.52+night),(float)(dusk*.86+night));
                    climate.Select(2,0);
                }
                climate.Select(0,0);
                for(int frame=1;frame<=4*fps;frame++)
                {climate.Advance(1d/fps,true);double t=frame/(4d*fps);t=t*t*(3-2*t);Gain(climate,window,shop,(float)(1-t),(float)(1-t));}
                foreach(double at in new[]{.75,2.5})
                {
                    climate.Preview(0,0);climate.Select(2,0);for(int frame=0;frame<at*fps;frame++)climate.Advance(1d/fps,true);
                    float before=climate.BuildingGain,storeBefore=climate.ShopGain,clock=climate.ActiveSeconds;
                    climate.Select(0,0);Gain(climate,window,shop,before,storeBefore);
                    for(int frame=0;frame<fps;frame++)climate.Advance(1d/fps,false);
                    Need(climate.ActiveSeconds==clock,"Hidden frontage keeps advancing.");Gain(climate,window,shop,before,storeBefore);
                    for(int frame=0;frame<4*fps;frame++)climate.Advance(1d/fps,true);Gain(climate,window,shop,0,0);cases++;
                }
                cases+=2;
            }
            climate.Preview(0,5);Need(climate.ShopGain>0&&climate.BuildingGain>0,"Dark-weather storefront exposure missing.");
            Need(GameObject.Find("Window Glass").GetComponent<MeshRenderer>().sharedMaterial==window&&GameObject.Find("Shop Interior").GetComponent<MeshRenderer>().sharedMaterial==shop,"Time selection allocates new facade materials.");
            Need(originalW==originalWindow.GetColor("_EmissionColor")&&originalS==originalShop.GetColor("_EmissionColor"),"Preview edits shared material assets.");
            Need(climate.AdditionalLights==4,"Store adds realtime lights beyond the mobile limit.");
            climate.Preview(0,0);
            return new Report{result="PASS: baked storefront geometry, shared room/shop atlases and actual material fades; device test pending",buildings=buildings,windows=windows,litWindows=lit,darkWindows=dark,warmWindows=warm,coolWindows=cool,shopCounts=styles,frameRates=rates,transitionCases=cases,windowAtlasWidth=256,windowAtlasHeight=128,shopAtlasSize=512,realtimeStreetLights=4};
        }
        static void Gain(CityClimate climate,Material window,Material shop,float expectedW,float expectedS)
        {
            Need(Mathf.Abs(climate.BuildingGain-expectedW)<.0001f&&Mathf.Abs(climate.ShopGain-expectedS)<.0001f,"Actual scene gain differs from the directional transition oracle.");
            Need(Mathf.Abs(window.GetColor("_EmissionColor").r-expectedW*1.15f)<.00015f&&Mathf.Abs(shop.GetColor("_EmissionColor").r-expectedS*1.1f)<.00015f,"Shader exposure differs from the transition.");
        }
        static void Map(Material material,int width,int height)
        {
            Need(material.IsKeywordEnabled("_EMISSION"),"Emission shader variant missing.");
            foreach(string property in new[]{"_BaseMap","_EmissionMap"})
            {var map=material.GetTexture(property) as Texture2D;Need(map!=null&&map.width==width&&map.height==height&&map.mipmapCount>1&&map.wrapMode==TextureWrapMode.Clamp,"Shared atlas dimensions or filtering invalid.");}
        }
        static void Geometry(Mesh mesh)
        {
            Vector3[] p=mesh.vertices,n=mesh.normals;Vector2[] uv=mesh.uv;int[] triangles=mesh.triangles;
            Need(p.Length==n.Length&&p.Length==uv.Length,"Facade mesh attributes incomplete.");
            for(int i=0;i<p.Length;i++)Need(Finite(p[i].x)&&Finite(p[i].y)&&Finite(p[i].z)&&Mathf.Abs(n[i].magnitude-1)<.001f&&uv[i].x>=0&&uv[i].x<=1&&uv[i].y>=0&&uv[i].y<=1,"Facade attributes invalid.");
            for(int i=0;i<triangles.Length;i+=3)Need(Vector3.Cross(p[triangles[i+1]]-p[triangles[i]],p[triangles[i+2]]-p[triangles[i]]).sqrMagnitude>1e-10f,"Facade has degenerate triangles.");
        }
        static bool Finite(float value)=>!float.IsNaN(value)&&!float.IsInfinity(value);
        static void Need(bool condition,string message){if(!condition)throw new InvalidOperationException(message);}
        [Serializable]internal sealed class Report
        {
            public string result;public int buildings,windows,litWindows,darkWindows,warmWindows,coolWindows,transitionCases,windowAtlasWidth,windowAtlasHeight,shopAtlasSize,realtimeStreetLights;
            public int[] shopCounts,frameRates;
        }
    }
}
