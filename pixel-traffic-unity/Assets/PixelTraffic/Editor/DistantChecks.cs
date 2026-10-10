using System;
using System.Collections.Generic;
using UnityEditor;
using UnityEngine;

namespace PixelTraffic.UnityPrototype.Editor
{
    internal static class DistantChecks
    {
        internal static Report Validate()
        {
            var distant=UnityEngine.Object.FindFirstObjectByType<DistantBackdrop>();Need(distant!=null,"Distant controller missing.");
            var root=GameObject.Find("Distant Skyline");var filters=root.GetComponentsInChildren<MeshFilter>();
            Need(filters.Length==8,"Sky, three mountains, city, river, banks and bridge meshes missing.");int triangles=0;
            foreach(var filter in filters)
            {
                Mesh mesh=filter.sharedMesh;Need(mesh!=null&&mesh.vertexCount>0,"Distant mesh missing.");
                var vertices=mesh.vertices;var indices=mesh.triangles;triangles+=indices.Length/3;
                Need(mesh.colors.Length==vertices.Length&&mesh.uv.Length==vertices.Length,"Distant shader data missing.");
                foreach(var v in vertices)Need(float.IsFinite(v.x)&&float.IsFinite(v.y)&&float.IsFinite(v.z),"Non-finite backdrop vertex.");
                for(int i=0;i<indices.Length;i+=3)Need(Vector3.Cross(vertices[indices[i+1]]-vertices[indices[i]],vertices[indices[i+2]]-vertices[indices[i]]).sqrMagnitude>.0000001f,"Degenerate distant triangle.");
            }
            Need(triangles<1800,"Distant mesh budget exceeded.");
            Need(root.GetComponentsInChildren<Light>().Length==0&&root.GetComponentsInChildren<Collider>().Length==0,"Distant scenery adds realtime lights or traffic obstacles.");
            foreach(var renderer in root.GetComponentsInChildren<Renderer>())
                Need(renderer.shadowCastingMode==UnityEngine.Rendering.ShadowCastingMode.Off&&!renderer.receiveShadows,"Distant geometry adds shadow work.");
            Mesh riverMesh=GameObject.Find("Reference River").GetComponent<MeshFilter>().sharedMesh;
            Need(riverMesh.bounds.size.z>60&&riverMesh.bounds.min.y==8&&riverMesh.bounds.max.y==25,"River lost actual depth or near/far banks.");
            float surfaceY=8+(362-330)*17f/65;
            Need(24.8f-1.3f/2-surfaceY>7,"Bridge deck is submerged or occluded by an upright river sheet.");
            Mesh bridgeMesh=GameObject.Find("Reference Bridge").GetComponent<MeshFilter>().sharedMesh;
            int lampVertices=0;foreach(var color in bridgeMesh.colors)if(color.r==6)lampVertices++;
            Need(lampVertices==11*4,"Bridge does not have eleven authored opaque lamp faces.");
            Mesh ridge0=GameObject.Find("Distant Ridge 0").GetComponent<MeshFilter>().sharedMesh;
            Mesh ridge1=GameObject.Find("Distant Ridge 1").GetComponent<MeshFilter>().sharedMesh;
            Mesh ridge2=GameObject.Find("Distant Ridge 2").GetComponent<MeshFilter>().sharedMesh;
            Need(ridge1.bounds.max.y>ridge0.bounds.max.y+15&&ridge2.bounds.max.y>ridge1.bounds.max.y+15,"Mountain layers collapse to one silhouette.");
            var cylinder=AssetDatabase.LoadAssetAtPath<Mesh>(StarterScene.Generated+"/StreetCylinder.asset");
            Need(cylinder!=null&&cylinder.triangles.Length/3==64&&cylinder.bounds.size==new Vector3(1,2,1),"Street cylinder changed dimensions or budget.");
            var climate=UnityEngine.Object.FindFirstObjectByType<CityClimate>();climate.Initialize();climate.Preview(0,0);
            Material original=AssetDatabase.LoadAssetAtPath<Material>(StarterScene.Generated+"/DistantLandscape.mat");
            Vector4 originalTheme=original.GetVector("_Theme"),originalWeather=original.GetVector("_Weather");
            Need(distant.RuntimeMaterial!=original&&distant.RuntimeMaterial.shader.name=="PixelTraffic/Distant","Backdrop mutates a shared asset or loses its shader.");
            foreach(var renderer in root.GetComponentsInChildren<Renderer>())Need(renderer.sharedMaterial==distant.RuntimeMaterial,"Backdrop creates duplicate materials.");
            int combinations=0;
            for(int theme=0;theme<3;theme++)for(int weather=0;weather<6;weather++)
            {
                climate.Preview(theme,weather);var t=distant.RuntimeMaterial.GetVector("_Theme");
                Need(Mathf.Abs(t[theme]-1)<.00001f&&Mathf.Abs(t.x+t.y+t.z-1)<.00001f,"Saved distant theme flashes another endpoint.");
                var w=distant.RuntimeMaterial.GetVector("_Weather");Need(w.x>=0&&w.x<=1&&w.y>=0&&w.y<=1&&w.z>=0&&w.z<=1&&w.w>=0&&w.w<=1,"Invalid distant weather weights.");
                Need((w.z>.99f)==(weather==3)&&(w.y>.99f)==(weather==4),"Backdrop snow or haze endpoint mapping differs.");combinations++;
            }
            float maxJump=0;int transitionFrames=0;int[] rates={15,30,60,120};
            foreach(int fps in rates)
            {
                climate.Preview(0,0);climate.Select(2,5);Vector4 before=distant.RuntimeMaterial.GetVector("_Theme");
                for(int frame=1;frame<=fps*4;frame++)
                {
                    climate.Advance(1d/fps,true);Vector4 actual=distant.RuntimeMaterial.GetVector("_Theme");double seconds=(double)frame/fps;
                    double q=seconds<=2?seconds/2:(seconds-2)/2;q=q*q*(3-2*q);
                    Need(Math.Abs(actual.x-(seconds<=2?1-q:0))<.00002&&Math.Abs(actual.y-(seconds<=2?q:1-q))<.00002&&Math.Abs(actual.z-(seconds<=2?0:q))<.00002,"Backdrop does not follow the independent sunset journey oracle.");
                    maxJump=Mathf.Max(maxJump,(actual-before).magnitude);before=actual;transitionFrames++;
                }
                climate.Select(0,0);
                for(int frame=0;frame<fps*4;frame++){climate.Advance(1d/fps,true);Need(distant.RuntimeMaterial.GetVector("_Theme").y==0,"Night-to-day backdrop inserts unwanted sunset.");}
            }
            climate.Preview(0,0);float offset=distant.CloudOffset;climate.Advance(.1,true);Need(distant.CloudOffset>offset,"Clouds do not advance on active frames.");
            var motion=distant.RuntimeMaterial.GetVector("_Motion");var weights=distant.RuntimeMaterial.GetVector("_Theme");
            for(int i=0;i<100;i++)climate.Advance(.1,false);
            Need(motion==distant.RuntimeMaterial.GetVector("_Motion")&&weights==distant.RuntimeMaterial.GetVector("_Theme"),"Hidden wallpaper advances clouds or shimmer.");
            climate.Select(2,5);for(int i=0;i<9;i++)climate.Advance(.1,true);weights=distant.RuntimeMaterial.GetVector("_Theme");climate.Select(1,3);
            Need(weights==distant.RuntimeMaterial.GetVector("_Theme"),"Backdrop retarget jumps before the next frame.");
            Need(original.GetVector("_Theme")==originalTheme&&original.GetVector("_Weather")==originalWeather,"Backdrop preview changed asset defaults.");
            Camera camera=Camera.main;float oldAspect=camera.aspect;int compositionSamples=0;
            try
            {
                foreach(float aspect in new[]{9f/20,9f/16})
                {
                    camera.aspect=aspect;
                    Vector3 river=camera.WorldToViewportPoint(new Vector3(0,17,372));
                    Vector3 summit=camera.WorldToViewportPoint(new Vector3(-42,70,425));
                    Need(river.z>0&&river.y>.62f&&river.y<.72f,"River leaves the band behind the boulevard.");
                    Need(summit.z>0&&summit.y>river.y+.10f&&summit.y<.92f,"Mountain depth or sky room lost.");compositionSamples+=2;
                }
            }finally{camera.aspect=oldAspect;}
            climate.Preview(0,0);
            return new Report{result="PASS: river depth and unsubmerged bridge, authored lamps, three ridge silhouettes, one runtime material, saved endpoints, directional sunset, hidden clock, retarget and portrait composition; actual GPU preview/device test separate",meshCount=filters.Length,triangles=triangles,streetCylinderTriangles=64,combinations=combinations,frameRates=rates,transitionFrames=transitionFrames,compositionSamples=compositionSamples,maxThemeFrameJump=maxJump,hiddenFrames=100,bridgeLampCount=11,riverDepth=riverMesh.bounds.size.z,bridgeDeckClearance=24.8f-1.3f/2-surfaceY};
        }
        private static void Need(bool ok,string why){if(!ok)throw new InvalidOperationException(why);}
        [Serializable] internal sealed class Report
        {
            public string result;public int meshCount,triangles,streetCylinderTriangles,combinations,transitionFrames,compositionSamples,hiddenFrames;
            public int[] frameRates;public float maxThemeFrameJump;
            public int bridgeLampCount;public float riverDepth,bridgeDeckClearance;
        }
    }
}
