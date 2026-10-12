using System;
using System.IO;
using UnityEditor;
using UnityEditor.SceneManagement;
using UnityEngine;
using UnityEngine.Rendering;
using UnityEngine.Rendering.Universal;

namespace PixelTraffic.UnityPrototype.Editor
{
    public static class CityPreview
    {
        private static double started;
        private static int warmupFrames;
        private static readonly System.Collections.Generic.Dictionary<PrototypeDrive,float> initialPositions=new System.Collections.Generic.Dictionary<PrototypeDrive,float>();

        // A real URP camera render on the runner's GPU, not an AI-generated mockup.
        // This is an Editor preview; it does not assert Android device performance.
        public static void CaptureBatch()
        {
            try
            {
                StarterScene.RequireEditor();
                if (SystemInfo.graphicsDeviceType == GraphicsDeviceType.Null)
                    throw new InvalidOperationException("Preview needs a graphics device; do not use -nographics.");
                EditorSceneManager.OpenScene(StarterConfig.ScenePath);
                initialPositions.Clear();foreach(var vehicle in UnityEngine.Object.FindObjectsByType<PrototypeDrive>(FindObjectsSortMode.None))initialPositions.Add(vehicle,vehicle.transform.position.z);
                var street = UnityEngine.Object.FindFirstObjectByType<StreetSimulation>();
                if (street != null) { street.ResetModel(); street.Advance(30); street.ApplyViews(); }
                UnityEngine.Object.FindFirstObjectByType<CityClimate>().Initialize();
                UnityEngine.Object.FindFirstObjectByType<RoadSurface>().Sync(true);
                started = EditorApplication.timeSinceStartup;
                warmupFrames = 0;
                EditorApplication.update += CaptureWhenReady;
                EditorApplication.QueuePlayerLoopUpdate();
            }
            catch (Exception exception) { Fail(exception); }
        }

        private static void CaptureWhenReady()
        {
            double elapsed = EditorApplication.timeSinceStartup - started;
            if (elapsed > 90) { Fail(new TimeoutException("Preview shader warmup timed out.")); return; }
            if (elapsed < 3 || ShaderUtil.anythingCompiling)
            {
                EditorApplication.QueuePlayerLoopUpdate();
                return;
            }
            if (warmupFrames < 3)
            {
                try
                {
                    Camera warmCamera = Camera.main;
                    var warmTarget = RenderTexture.GetTemporary(540, 1200, 24, RenderTextureFormat.ARGB32);
                    try
                    {
                        warmCamera.aspect = .45f;
                        RenderPipeline.SubmitRenderRequest(warmCamera, new UniversalRenderPipeline.SingleCameraRequest { destination = warmTarget });
                    }
                    finally { RenderTexture.ReleaseTemporary(warmTarget); }
                    warmupFrames++;
                    EditorApplication.QueuePlayerLoopUpdate();
                    return;
                }
                catch (Exception exception) { Fail(exception); return; }
            }
            EditorApplication.update -= CaptureWhenReady;
            RenderTexture target = null;
            Texture2D image = null;
            RenderTexture previous = RenderTexture.active;
            try
            {
                const int width = 540, height = 1200;
                Camera camera = Camera.main;
                if (camera == null) throw new InvalidOperationException("City camera missing.");
                camera.aspect = (float)width / height;
                Vector3 roadCameraPosition=camera.transform.position;Quaternion roadCameraRotation=camera.transform.rotation;float roadFov=camera.fieldOfView;
                var originalCars=UnityEngine.Object.FindObjectsByType<PrototypeDrive>(FindObjectsSortMode.None);var originalZ=new float[originalCars.Length];for(int i=0;i<originalCars.Length;i++)originalZ[i]=initialPositions[originalCars[i]];
                target = new RenderTexture(width, height, 24, RenderTextureFormat.ARGB32) { antiAliasing = 2 };
                target.Create();
                var request = new UniversalRenderPipeline.SingleCameraRequest { destination = target };
                RenderPipeline.SubmitRenderRequest(camera, request);
                RenderTexture.active = target;
                image = new Texture2D(width, height, TextureFormat.RGB24, false);
                image.ReadPixels(new Rect(0, 0, width, height), 0, 0);
                image.Apply();
                Color32[] pixels = image.GetPixels32();
                int nonBlack = 0, pink = 0;
                foreach (Color32 pixel in pixels)
                {
                    if (pixel.r + pixel.g + pixel.b > 30) nonBlack++;
                    if (pixel.r > 220 && pixel.b > 220 && pixel.g < 50) pink++;
                }
                if (nonBlack < pixels.Length / 2 || pink > pixels.Length / 100)
                    throw new InvalidOperationException("Preview is black or has missing-material pink pixels.");
                Directory.CreateDirectory("Reports");
                string output = "Reports/city-preview-" + StarterConfig.VersionName + ".png";
                File.WriteAllBytes(output, image.EncodeToPNG());
                string[] names = { "Asphalt", "Road White", "Road Yellow", "Vehicle Blue", "Leaves 0", "Leaves 1", "Leaves 2" };
                var colors = new string[names.Length];
                for (int i = 0; i < names.Length; i++)
                {
                    Material material = AssetDatabase.LoadAssetAtPath<Material>(StarterScene.Generated + "/" + names[i].Replace(" ", "") + ".mat");
                    colors[i] = names[i] + ": " + material.GetColor("_BaseColor").ToString("F3");
                }
                var pipeline = (UniversalRenderPipelineAsset)GraphicsSettings.currentRenderPipeline;
                bool oldBatcher = pipeline.useSRPBatcher;
                bool oldGraphicsBatcher = GraphicsSettings.useScriptableRenderPipelineBatching;
                try
                {
                    pipeline.useSRPBatcher = false;
                    GraphicsSettings.useScriptableRenderPipelineBatching = false;
                    RenderPipeline.SubmitRenderRequest(camera, request);
                    RenderTexture.active = target;
                    image.ReadPixels(new Rect(0, 0, width, height), 0, 0); image.Apply();
                    File.WriteAllBytes("Reports/city-preview-" + StarterConfig.VersionName + "-batcher-diagnostic.png", image.EncodeToPNG());
                }
                finally
                {
                    pipeline.useSRPBatcher = oldBatcher;
                    GraphicsSettings.useScriptableRenderPipelineBatching = oldGraphicsBatcher;
                }
                var cameraPreviews=new System.Collections.Generic.List<string>();
                // Identical traffic state, lighting and resolution: only camera pose changes.
                camera.transform.position=new Vector3(-1.2f,20.5f,-27);camera.transform.LookAt(new Vector3(0,1.6f,23));camera.fieldOfView=44;
                cameraPreviews.Add(CaptureState(camera,request,target,image,"camera-previous"));
                camera.transform.SetPositionAndRotation(roadCameraPosition,roadCameraRotation);camera.fieldOfView=roadFov;
                cameraPreviews.Add(CaptureState(camera,request,target,image,"camera-reference"));
                var referenceTarget=new RenderTexture(720,1280,24,RenderTextureFormat.ARGB32){antiAliasing=2};referenceTarget.Create();
                var referenceImage=new Texture2D(720,1280,TextureFormat.RGB24,false);
                try
                {
                    camera.aspect=9f/16;
                    cameraPreviews.Add(CaptureState(camera,new UniversalRenderPipeline.SingleCameraRequest{destination=referenceTarget},referenceTarget,referenceImage,"camera-reference-9x16"));
                }
                finally{camera.aspect=.45f;referenceTarget.Release();UnityEngine.Object.DestroyImmediate(referenceTarget);UnityEngine.Object.DestroyImmediate(referenceImage);}
                var landscapePreviews=new System.Collections.Generic.List<string>();
                var landscapeClimate=UnityEngine.Object.FindFirstObjectByType<CityClimate>();
                camera.transform.position=new Vector3(2,20,295);camera.transform.LookAt(new Vector3(-13,48,425));camera.fieldOfView=44;
                foreach(int theme in new[]{0,1,2})
                {
                    landscapeClimate.Preview(theme,0);landscapePreviews.Add(CaptureState(camera,request,target,image,"landscape-"+new[]{"day","sunset","night"}[theme]));
                }
                landscapeClimate.Preview(0,5);landscapePreviews.Add(CaptureState(camera,request,target,image,"landscape-storm"));
                landscapeClimate.Preview(0,4);landscapePreviews.Add(CaptureState(camera,request,target,image,"landscape-fog"));
                camera.transform.position=new Vector3(-20,32,288);camera.transform.LookAt(new Vector3(-20,20,364));camera.fieldOfView=60;
                landscapeClimate.Preview(0,0);landscapePreviews.Add(CaptureState(camera,request,target,image,"river-day"));
                landscapeClimate.Preview(2,0);landscapePreviews.Add(CaptureState(camera,request,target,image,"river-night"));
                for(int i=0;i<60;i++)landscapeClimate.Advance(.1,true);
                landscapePreviews.Add(CaptureState(camera,request,target,image,"river-motion"));
                camera.transform.SetPositionAndRotation(roadCameraPosition,roadCameraRotation);camera.fieldOfView=roadFov;landscapeClimate.Preview(0,0);
                var riverfrontPreviews=new System.Collections.Generic.List<string>();
                var riverfrontCamera=camera.GetComponent<CityCamera>();
                try
                {
                    foreach(int option in new[]{0,1})foreach(int theme in new[]{0,2})
                    {
                        riverfrontCamera.Preview(option);landscapeClimate.Preview(theme,0);
                        riverfrontPreviews.Add(CaptureState(camera,request,target,image,"riverfront-"+(option==0?"city-":"roundabout-")+(theme==0?"day":"night")));
                    }
                }
                finally{riverfrontCamera.Preview(0);camera.transform.SetPositionAndRotation(roadCameraPosition,roadCameraRotation);camera.fieldOfView=roadFov;landscapeClimate.Preview(0,0);}
                var skyPreviews=new System.Collections.Generic.List<string>();
                var skyTarget=new RenderTexture(960,720,24,RenderTextureFormat.ARGB32){antiAliasing=2};skyTarget.Create();
                var skyImage=new Texture2D(960,720,TextureFormat.RGB24,false);
                try
                {
                    camera.aspect=4f/3;camera.transform.position=new Vector3(0,60,295);
                    camera.transform.LookAt(new Vector3(0,115,468));camera.fieldOfView=44;
                    var skyRequest=new UniversalRenderPipeline.SingleCameraRequest{destination=skyTarget};
                    foreach(int theme in new[]{0,1,2})
                    {
                        landscapeClimate.Preview(theme,0);
                        skyPreviews.Add(CaptureState(camera,skyRequest,skyTarget,skyImage,"sky-"+new[]{"day","sunset","night"}[theme]));
                    }
                    landscapeClimate.Preview(0,5);
                    skyPreviews.Add(CaptureState(camera,skyRequest,skyTarget,skyImage,"sky-storm"));
                    landscapeClimate.Preview(0,0);for(int frame=0;frame<60;frame++)landscapeClimate.Advance(.1,true);
                    skyPreviews.Add(CaptureState(camera,skyRequest,skyTarget,skyImage,"sky-motion"));
                }
                finally
                {
                    skyTarget.Release();UnityEngine.Object.DestroyImmediate(skyTarget);UnityEngine.Object.DestroyImmediate(skyImage);
                    camera.aspect=.45f;camera.transform.SetPositionAndRotation(roadCameraPosition,roadCameraRotation);camera.fieldOfView=roadFov;landscapeClimate.Preview(0,0);
                }
                var sceneryPreviews=new System.Collections.Generic.List<string>();
                var groundPreviews=new System.Collections.Generic.List<string>();
                var reflectionPreviews=new System.Collections.Generic.List<string>();
                var groundTarget=new RenderTexture(960,720,24,RenderTextureFormat.ARGB32){antiAliasing=2};groundTarget.Create();
                var groundImage=new Texture2D(960,720,TextureFormat.RGB24,false);
                var groundStreet=UnityEngine.Object.FindFirstObjectByType<StreetSimulation>();
                try
                {
                    camera.aspect=4f/3;camera.fieldOfView=55;camera.transform.position=new Vector3(0,8,-23);camera.transform.LookAt(new Vector3(3,0,6));
                    var groundRequest=new UniversalRenderPipeline.SingleCameraRequest{destination=groundTarget};
                    landscapeClimate.Preview(0,0);groundStreet.ApplyViews();groundPreviews.Add(CaptureState(camera,groundRequest,groundTarget,groundImage,"ground-day"));
                    landscapeClimate.Preview(2,2);groundStreet.ApplyViews();groundPreviews.Add(CaptureState(camera,groundRequest,groundTarget,groundImage,"ground-rain-night"));
                    for(int frame=0;frame<20;frame++)landscapeClimate.Advance(.1,true);
                    groundPreviews.Add(CaptureState(camera,groundRequest,groundTarget,groundImage,"ground-ripple-motion"));
                    landscapeClimate.Preview(0,0);groundStreet.ApplyViews();camera.transform.position=new Vector3(4.4f,4,-28);camera.transform.LookAt(new Vector3(7.7f,.13f,-22));
                    groundPreviews.Add(CaptureState(camera,groundRequest,groundTarget,groundImage,"ground-paving"));
                    camera.transform.position=new Vector3(0,8,-23);camera.transform.LookAt(new Vector3(3,0,6));
                    foreach(int reflectionWeather in new[]{0,1,2,4,5})
                    {
                        landscapeClimate.Preview(2,reflectionWeather);groundStreet.ApplyViews();
                        reflectionPreviews.Add(CaptureState(camera,groundRequest,groundTarget,groundImage,"wet-reflections-weather-"+reflectionWeather));
                    }
                    landscapeClimate.Preview(0,2);groundStreet.ApplyViews();reflectionPreviews.Add(CaptureState(camera,groundRequest,groundTarget,groundImage,"wet-reflections-rain-day"));
                    landscapeClimate.Preview(2,2);landscapeClimate.Select(2,0);
                    for(int frame=0;frame<20;frame++)landscapeClimate.Advance(.1,true);
                    groundStreet.ApplyViews();reflectionPreviews.Add(CaptureState(camera,groundRequest,groundTarget,groundImage,"wet-reflections-drying-2s"));
                    for(int frame=0;frame<40;frame++)landscapeClimate.Advance(.1,true);
                    groundStreet.ApplyViews();reflectionPreviews.Add(CaptureState(camera,groundRequest,groundTarget,groundImage,"wet-reflections-drying-6s"));
                }
                finally
                {
                    groundTarget.Release();UnityEngine.Object.DestroyImmediate(groundTarget);UnityEngine.Object.DestroyImmediate(groundImage);
                    camera.aspect=.45f;camera.transform.SetPositionAndRotation(roadCameraPosition,roadCameraRotation);camera.fieldOfView=roadFov;landscapeClimate.Preview(0,0);
                    groundStreet.ApplyViews();
                }
                var sceneryTarget=new RenderTexture(960,720,24,RenderTextureFormat.ARGB32){antiAliasing=2};sceneryTarget.Create();
                var sceneryImage=new Texture2D(960,720,TextureFormat.RGB24,false);
                try
                {
                    camera.aspect=4f/3;camera.fieldOfView=52;
                    var sceneryRequest=new UniversalRenderPipeline.SingleCameraRequest{destination=sceneryTarget};
                    string[] species={"rounded","upright","spreading"};
                    for(int kind=0;kind<3;kind++)
                    {
                        float z=-24+kind*13;landscapeClimate.Preview(0,0);
                        camera.transform.position=new Vector3(-3.7f,4.7f,z+9);camera.transform.LookAt(new Vector3(-8.6f,4.6f,z));
                        sceneryPreviews.Add(CaptureState(camera,sceneryRequest,sceneryTarget,sceneryImage,"tree-"+species[kind]+"-day"));
                    }
                    camera.transform.position=new Vector3(-3.7f,4.7f,-15);camera.transform.LookAt(new Vector3(-8.6f,4.6f,-24));
                    landscapeClimate.Preview(0,5);for(int i=0;i<200;i++)landscapeClimate.Advance(.05,true);
                    sceneryPreviews.Add(CaptureState(camera,sceneryRequest,sceneryTarget,sceneryImage,"tree-rounded-storm"));
                    landscapeClimate.Preview(2,0);sceneryPreviews.Add(CaptureState(camera,sceneryRequest,sceneryTarget,sceneryImage,"tree-rounded-night"));
                    camera.fieldOfView=55;camera.transform.position=new Vector3(-4.8f,9,-11);camera.transform.LookAt(new Vector3(-11.8f,8,-22));
                    foreach(int theme in new[]{0,2})
                    {
                        landscapeClimate.Preview(theme,0);sceneryPreviews.Add(CaptureState(camera,sceneryRequest,sceneryTarget,sceneryImage,"architecture-"+(theme==0?"day":"night")));
                    }
                    camera.transform.position=new Vector3(-4.8f,18,-13);camera.transform.LookAt(new Vector3(-13,12.8f,-22));landscapeClimate.Preview(0,0);
                    sceneryPreviews.Add(CaptureState(camera,sceneryRequest,sceneryTarget,sceneryImage,"architecture-roof-day"));
                    foreach(float side in new[]{-1f,1f})
                    {
                        camera.transform.position=new Vector3(side*8.4f,10,-36);camera.transform.LookAt(new Vector3(side*14.1f,9.5f,-29));camera.fieldOfView=50;
                        foreach(int theme in new[]{0,2})
                        {
                            landscapeClimate.Preview(theme,0);sceneryPreviews.Add(CaptureState(camera,sceneryRequest,sceneryTarget,sceneryImage,"building-corner-"+(side<0?"left":"right")+(theme==0?"-day":"-night")));
                        }
                    }
                    camera.transform.position=new Vector3(-4.8f,24,-13);camera.transform.LookAt(new Vector3(-13,17.5f,-22));camera.fieldOfView=55;
                    landscapeClimate.Preview(0,0);sceneryPreviews.Add(CaptureState(camera,sceneryRequest,sceneryTarget,sceneryImage,"building-terrace-day"));
                    camera.transform.position=new Vector3(3.8f,10,-10);camera.transform.LookAt(new Vector3(10.85f,7.5f,-5));camera.fieldOfView=48;
                    foreach(int theme in new[]{0,2})
                    {
                        landscapeClimate.Preview(theme,0);sceneryPreviews.Add(CaptureState(camera,sceneryRequest,sceneryTarget,sceneryImage,"architecture-right-"+(theme==0?"day":"night")));
                    }
                    camera.transform.position=new Vector3(-7.3f,10.1f,2);camera.transform.LookAt(new Vector3(-10.5f,7.5f,-5));camera.fieldOfView=44;
                    foreach(int theme in new[]{0,1,2})
                    {
                        landscapeClimate.Preview(theme,0);sceneryPreviews.Add(CaptureState(camera,sceneryRequest,sceneryTarget,sceneryImage,"architecture-window-"+new[]{"day","sunset","night"}[theme]));
                    }
                }
                finally{sceneryTarget.Release();UnityEngine.Object.DestroyImmediate(sceneryTarget);UnityEngine.Object.DestroyImmediate(sceneryImage);}
                camera.aspect=.45f;camera.transform.SetPositionAndRotation(roadCameraPosition,roadCameraRotation);camera.fieldOfView=roadFov;landscapeClimate.Preview(0,0);
                var climate = UnityEngine.Object.FindFirstObjectByType<CityClimate>();
                var previews = new System.Collections.Generic.List<string>();
                int[] themes = { 1, 2, 0, 0, 0, 2 }; int[] weather = { 0, 0, 2, 3, 4, 5 };
                string[] labels = { "sunset", "night", "rain", "snow", "fog", "storm-night" };
                for (int i = 0; i < labels.Length; i++)
                {
                    climate.Preview(themes[i], weather[i]);
                    var street=UnityEngine.Object.FindFirstObjectByType<StreetSimulation>();
                    for (int n = 0; n < 50; n++) {climate.Advance(.1, true);street.Advance(.1);}street.ApplyViews();
                    previews.Add(CaptureState(camera, request, target, image, labels[i]));
                }
                climate.Preview(0, 0); climate.Advance(.01, true);
                var transitionStreet=UnityEngine.Object.FindFirstObjectByType<StreetSimulation>();transitionStreet.Advance(0);transitionStreet.ApplyViews();
                climate.Select(2, 0);
                previews.Add(CaptureState(camera, request, target, image, "transition-0s"));
                for(int second=1;second<=4;second++)
                {
                    for (int n = 0; n < 10; n++) climate.Advance(.1, true);
                    transitionStreet.ApplyViews();
                    previews.Add(CaptureState(camera, request, target, image, "transition-"+second+"s"));
                }
                climate.Preview(0,5);
                var settledStreet=UnityEngine.Object.FindFirstObjectByType<StreetSimulation>();
                for(int n=0;n<2000;n++){climate.Advance(.1,true);settledStreet.Advance(.1);}settledStreet.ApplyViews();
                previews.Add(CaptureState(camera, request, target, image, "storm-settled"));
                climate.Preview(0, 0);
                var vehiclePreviews=new System.Collections.Generic.List<string>();
                // Opening another scene here would unload the temporary readback texture.
                // Reset the existing unsaved scene instead; it is never used to build the APK.
                var detailStreet=UnityEngine.Object.FindFirstObjectByType<StreetSimulation>();
                detailStreet.ResetModel();detailStreet.Advance(0);detailStreet.ApplyViews();
                // Temporary camera/poses in an unsaved Editor scene; phone camera is unchanged.
                foreach(string model in new[]{"Sedan","SportCoupe","Suv","Taxi"})
                {
                    PrototypeDrive selected=null;
                    foreach(var car in UnityEngine.Object.FindObjectsByType<PrototypeDrive>(FindObjectsSortMode.None))
                        if(car.Model==model&&selected==null)selected=car;
                    selected.transform.position=new Vector3(-4.8f,0,0);selected.transform.rotation=Quaternion.identity;
                    foreach(bool rear in new[]{false,true})
                    {
                        // Both views stay over the road; a sidewalk camera can sit inside a canopy.
                        camera.transform.position=new Vector3(-1.0f,4.8f,rear ? -11.8f : 11.8f);
                        camera.transform.LookAt(selected.transform.position+Vector3.up*.85f);camera.fieldOfView=48;
                        vehiclePreviews.Add(CaptureState(camera,request,target,image,"vehicle-"+model+(rear ? "-rear" : "-front")));
                    }
                    selected.transform.position=new Vector3(selected.LaneX,0,200);
                }
                var heavyPreviews=new System.Collections.Generic.List<string>();
                // Straight front/rear elevations with full-length roof silhouettes. These poses are
                // temporary; the stored scene and phone camera are never changed by capture.
                var heavyTarget=new RenderTexture(720,960,24,RenderTextureFormat.ARGB32){antiAliasing=2};heavyTarget.Create();
                var heavyImage=new Texture2D(720,960,TextureFormat.RGB24,false);
                var savedActive=new bool[originalCars.Length];for(int i=0;i<originalCars.Length;i++)savedActive[i]=originalCars[i].gameObject.activeSelf;
                try
                {
                    camera.aspect=.75f;camera.fieldOfView=45;
                    var heavyRequest=new UniversalRenderPipeline.SingleCameraRequest{destination=heavyTarget};
                    foreach(string model in new[]{"CityBus","BoxTruck"})
                    {
                        PrototypeDrive selected=null;foreach(var car in originalCars)if(car.Model==model&&selected==null)selected=car;
                        if(selected==null)throw new InvalidOperationException("Missing heavy capture model "+model);
                        foreach(var car in originalCars)car.gameObject.SetActive(car==selected);
                        selected.transform.SetPositionAndRotation(new Vector3(0,0,90),Quaternion.identity);
                        foreach(int theme in new[]{0,2})foreach(bool rear in new[]{false,true})
                        {
                            climate.Preview(theme,0);detailStreet.ApplyViews();
                            selected.Lighting.Apply(new StreetModel.Car{lane=2,active=true,braking=rear&&theme==2},theme==2?1:0,0);
                            float distance=model=="CityBus"?17:14;
                            camera.transform.position=new Vector3(0,model=="CityBus"?7.4f:6.6f,90+(rear?-distance:distance));
                            camera.transform.LookAt(new Vector3(0,1.55f,90));
                            heavyPreviews.Add(CaptureState(camera,heavyRequest,heavyTarget,heavyImage,"heavy-"+model+(rear?"-rear":"-front")+(theme==0?"-day":"-night")));
                        }
                        selected.ResetPosition(originalZ[Array.IndexOf(originalCars,selected)]);
                    }
                }
                finally
                {
                    for(int i=0;i<originalCars.Length;i++)originalCars[i].gameObject.SetActive(savedActive[i]);
                    heavyTarget.Release();UnityEngine.Object.DestroyImmediate(heavyTarget);UnityEngine.Object.DestroyImmediate(heavyImage);
                    camera.aspect=.45f;camera.fieldOfView=roadFov;climate.Preview(0,0);detailStreet.ApplyViews();
                }
                var frontagePreviews=new System.Collections.Generic.List<string>();
                var frontageTarget=new RenderTexture(960,540,24,RenderTextureFormat.ARGB32){antiAliasing=2};frontageTarget.Create();
                var frontageImage=new Texture2D(960,540,TextureFormat.RGB24,false);
                try
                {
                    camera.aspect=960f/540;camera.fieldOfView=55;
                    var frontageRequest=new UniversalRenderPipeline.SingleCameraRequest{destination=frontageTarget};
                    string[] shops={"Cafe","Market","Store"};
                    for(int style=0;style<3;style++)
                    {
                        float z=-22+style*17;
                        camera.transform.position=new Vector3(-4,2.2f,z+3.8f);
                        camera.transform.LookAt(new Vector3(-10.75f,1.65f,z));
                        foreach(int theme in new[]{0,2})
                        {
                            climate.Preview(theme,0);detailStreet.ApplyViews();
                            frontagePreviews.Add(CaptureState(camera,frontageRequest,frontageTarget,frontageImage,"shop-"+shops[style]+(theme==0?"-day":"-night")));
                        }
                    }
                }
                finally{frontageTarget.Release();UnityEngine.Object.DestroyImmediate(frontageTarget);UnityEngine.Object.DestroyImmediate(frontageImage);}
                var drivingPreviews=new System.Collections.Generic.List<string>();
                var colourPreviews=new System.Collections.Generic.List<string>();
                camera.aspect=.45f;camera.fieldOfView=48;
                PrototypeDrive lightCar=null;foreach(var car in originalCars)if(car.Model=="Suv"&&lightCar==null)lightCar=car;
                lightCar.transform.position=new Vector3(-4.8f,0,0);lightCar.transform.rotation=Quaternion.identity;climate.Preview(2,0);
                var lightState=new StreetModel.Car{lane=2,active=true};
                foreach(string label in new[]{"lights-front-night","lights-brake-rear-night","lights-indicator-on","lights-indicator-off"})
                {
                    bool front=label=="lights-front-night";camera.transform.position=new Vector3(-1,4.8f,front?11.8f:-11.8f);camera.transform.LookAt(lightCar.transform.position+Vector3.up*.85f);
                    lightState.braking=label=="lights-brake-rear-night";lightState.targetLane=3;lightState.maneuver=label.Contains("indicator")?LaneChanges.Stage.Signaling:LaneChanges.Stage.Idle;lightState.signalTicks=label.EndsWith("off")?9:0;
                    lightCar.Lighting.Apply(lightState,1,0);drivingPreviews.Add(CaptureState(camera,request,target,image,label));
                }
                // Temporary isolated top views show the fan footprint and turning wet sheen.
                var fanActive=new bool[originalCars.Length];for(int i=0;i<originalCars.Length;i++){fanActive[i]=originalCars[i].gameObject.activeSelf;originalCars[i].gameObject.SetActive(originalCars[i]==lightCar);}
                try
                {
                    lightCar.transform.position=new Vector3(-3.2f,0,0);camera.transform.position=new Vector3(-3.2f,13,5);camera.transform.LookAt(new Vector3(-3.2f,0,5));camera.fieldOfView=52;
                    foreach(int fanWeather in new[]{0,2})
                    {
                        climate.Preview(2,fanWeather);lightCar.transform.rotation=Quaternion.Euler(0,fanWeather==0?0:8,0);
                        lightCar.Lighting.Apply(new StreetModel.Car{lane=2,active=true},1,fanWeather==0?0:1);
                        drivingPreviews.Add(CaptureState(camera,request,target,image,fanWeather==0?"headlight-fans-dry":"headlight-fans-wet-turn"));
                    }
                }
                finally{for(int i=0;i<originalCars.Length;i++)originalCars[i].gameObject.SetActive(fanActive[i]);}
                // Same road, camera and brightness for each model's dry/wet colour profile.
                try
                {
                    foreach(string colourModel in new[]{"Sedan","Suv","Taxi","SportCoupe","CityBus","BoxTruck"})
                    {
                        PrototypeDrive colourCar=null;foreach(var candidate in originalCars)if(candidate.Model==colourModel&&colourCar==null)colourCar=candidate;
                        if(colourCar==null)throw new InvalidOperationException("Missing headlight colour capture model "+colourModel);
                        foreach(var candidate in originalCars)candidate.gameObject.SetActive(candidate==colourCar);
                        colourCar.transform.SetPositionAndRotation(new Vector3(-3.2f,0,-colourCar.transform.Find("Sculpted Body").GetComponent<MeshFilter>().sharedMesh.bounds.max.z),Quaternion.identity);
                        camera.transform.position=new Vector3(-3.2f,15,3);camera.transform.LookAt(new Vector3(-3.2f,0,3));camera.fieldOfView=55;
                        foreach(int colourWeather in new[]{0,2})
                        {
                            climate.Preview(2,colourWeather);colourCar.Lighting.Apply(new StreetModel.Car{active=true},1,colourWeather==0?0:1);
                            colourPreviews.Add(CaptureState(camera,request,target,image,"headlight-colour-"+colourModel+(colourWeather==0?"-dry":"-wet")));
                        }
                    }
                }
                finally{for(int i=0;i<originalCars.Length;i++){originalCars[i].gameObject.SetActive(fanActive[i]);originalCars[i].ResetPosition(originalZ[i]);}}

                for(int i=0;i<originalCars.Length;i++)originalCars[i].ResetPosition(originalZ[i]);
                detailStreet.ResetModel();climate.Preview(0,0);camera.transform.SetPositionAndRotation(roadCameraPosition,roadCameraRotation);camera.fieldOfView=roadFov;
                int tracked=-1,captured=0,laneSignals=0,laneMerges=0;
                for(int tick=0;tick<18000&&captured<4;tick++)
                {
                    detailStreet.Advance(StreetModel.Dt);detailStreet.ApplyViews();
                    var cars=detailStreet.Model.Cars;foreach(var candidate in cars){if(candidate.maneuver==LaneChanges.Stage.Signaling)laneSignals++;if(candidate.maneuver==LaneChanges.Stage.Merging)laneMerges++;}
                    if(tracked<0)for(int i=0;i<cars.Length;i++)if(cars[i].maneuver==LaneChanges.Stage.Signaling&&cars[i].signalTicks==0)
                    {tracked=i;captured=1;camera.transform.position=new Vector3(cars[i].X+5,7,(float)cars[i].z-14);camera.transform.LookAt(new Vector3(cars[i].X,.8f,(float)cars[i].z));drivingPreviews.Add(CaptureState(camera,request,target,image,"lane-signal-start"));break;}
                    if(tracked>=0)
                    {
                        var car=cars[tracked];
                        camera.transform.position=new Vector3(car.X+5,7,(float)car.z-14);camera.transform.LookAt(new Vector3(car.X,.8f,(float)car.z));
                        if(captured==1&&car.maneuver==LaneChanges.Stage.Merging){captured=2;drivingPreviews.Add(CaptureState(camera,request,target,image,"lane-change-start"));}
                        else if(captured==2&&car.maneuver==LaneChanges.Stage.Merging&&car.mergeTicks==car.MergeDuration/2){captured=3;drivingPreviews.Add(CaptureState(camera,request,target,image,"lane-change-mid"));}
                        else if(captured==3&&car.maneuver==LaneChanges.Stage.Idle){captured=4;drivingPreviews.Add(CaptureState(camera,request,target,image,"lane-change-complete"));}
                        else if(captured==1&&car.maneuver==LaneChanges.Stage.Idle){tracked=-1;captured=0;drivingPreviews.RemoveAt(drivingPreviews.Count-1);}
                    }
                }
                if(captured!=4)throw new InvalidOperationException("Real lane maneuver capture incomplete: frames="+captured+" signalingTicks="+laneSignals+" mergingTicks="+laneMerges+" cycles="+detailStreet.Model.Cycles+" signal="+detailStreet.Model.Signal+" arrivals="+detailStreet.Model.Stops.Arrivals+" departures="+detailStreet.Model.Stops.Departures);
                // Capture real stop transfers and both phases of the same hazard clock.
                for(int i=0;i<originalCars.Length;i++)originalCars[i].ResetPosition(originalZ[i]);
                detailStreet.ResetModel();var stopPreviews=new System.Collections.Generic.List<string>();
                var stopTarget=new RenderTexture(720,960,24,RenderTextureFormat.ARGB32){antiAliasing=2};stopTarget.Create();
                var stopImage=new Texture2D(720,960,TextureFormat.RGB24,false);var seenStop=new bool[2,4];int stopCaptured=0;
                try
                {
                    camera.aspect=.75f;camera.fieldOfView=68;var stopRequest=new UniversalRenderPipeline.SingleCameraRequest{destination=stopTarget};
                    for(int tick=0;tick<18000&&stopCaptured<8;tick++)
                    {
                        detailStreet.Advance(StreetModel.Dt);var model=detailStreet.Model;
                        for(int stop=0;stop<2;stop++)
                        {
                            StreetModel.Car bus=null;foreach(var c in model.Cars)if(c.bus&&c.busStage==BusStops.Stage.Boarding&&c.busDoor==1&&c.Direction==BusStops.Side(stop)){bus=c;break;}
                            if(bus==null)continue;bool board=false,alight=false;
                            foreach(var rider in model.Stops.Riders)if(rider.stop==stop){board|=rider.stage==BusStops.RiderStage.Boarding;alight|=rider.stage==BusStops.RiderStage.Alighting;}
                            bool[] ready={alight,board,board&&bus.hazardTicks%18<9,board&&bus.hazardTicks%18>=9};
                            for(int stage=0;stage<4;stage++)if(ready[stage]&&!seenStop[stop,stage])
                            {
                                int side=BusStops.Side(stop);float z=BusStops.DoorZ(stop);
                                if(stage<2){camera.transform.position=new Vector3(side*7.5f,2.25f,z-side*5);camera.transform.LookAt(new Vector3(side*7.7f,1.2f,z));}
                                else{camera.transform.position=new Vector3(side*8.5f,2.25f,z+side*8);camera.transform.LookAt(new Vector3(side*5.6f,1.2f,BusStops.CenterZ(stop)));}
                                climate.Preview(stage<2?0:2,0);detailStreet.ApplyViews();
                                string[] stopLabels={"alighting-day","boarding-day","hazards-on-night","hazards-off-night"};
                                stopPreviews.Add(CaptureState(camera,stopRequest,stopTarget,stopImage,"bus-stop-"+(stop==0?"west-":"east-")+stopLabels[stage]));seenStop[stop,stage]=true;stopCaptured++;
                            }
                        }
                    }
                    if(stopCaptured!=8)throw new InvalidOperationException("Actual bus transfer/hazard capture incomplete: "+stopCaptured+"/8");
                }
                finally
                {
                    stopTarget.Release();UnityEngine.Object.DestroyImmediate(stopTarget);UnityEngine.Object.DestroyImmediate(stopImage);
                    for(int i=0;i<originalCars.Length;i++)originalCars[i].ResetPosition(originalZ[i]);detailStreet.ResetModel();climate.Preview(0,0);detailStreet.ApplyViews();camera.aspect=.45f;camera.fieldOfView=roadFov;camera.transform.SetPositionAndRotation(roadCameraPosition,roadCameraRotation);
                }
                var koreanPreviews=new System.Collections.Generic.List<string>();
                int koreanMask=camera.cullingMask;var koreanLayers=new System.Collections.Generic.Dictionary<GameObject,int>();
                var outfitProbes=new System.Collections.Generic.List<GameObject>();var outfitMeshes=new System.Collections.Generic.List<Mesh>();var outfitSkins=new System.Collections.Generic.List<SkinnedMeshRenderer>();
                void PreviewLayer(Transform root,int layer){foreach(var child in root.GetComponentsInChildren<Transform>(true)){if(!koreanLayers.ContainsKey(child.gameObject))koreanLayers.Add(child.gameObject,child.gameObject.layer);child.gameObject.layer=layer;}}
                try
                {
                    climate.Preview(0,0);detailStreet.ResetModel();
                    camera.cullingMask=koreanMask;camera.transform.position=new Vector3(1.0f,6.7f,-4.0f);camera.transform.LookAt(new Vector3(StarterConfig.CurbFurnitureX,2.9f,7));camera.fieldOfView=55;
                    koreanPreviews.Add(CaptureState(camera,request,target,image,"curb-signal-placement"));
                    climate.Preview(2,2);detailStreet.ApplyViews();camera.transform.SetPositionAndRotation(roadCameraPosition,roadCameraRotation);camera.fieldOfView=roadFov;
                    koreanPreviews.Add(CaptureState(camera,request,target,image,"unique-signals-night-rain"));
                    climate.Preview(0,0);detailStreet.ApplyViews();
                    foreach(var pole in UnityEngine.Object.FindObjectsByType<Transform>(FindObjectsSortMode.None))if(pole.name=="Signal Pole"&&pole.position.x<0&&Mathf.Abs(pole.position.z-7)<.01f)PreviewLayer(pole,30);
                    var koreanCaptured=new System.Collections.Generic.HashSet<string>();
                    for(int frame=0;frame<1200&&koreanCaptured.Count<5;frame++)
                    {
                        detailStreet.Advance(.1);detailStreet.ApplyViews();var phase=detailStreet.Model.Signal;
                        string vehicle=phase==StreetModel.Phase.VehicleGreen?"green":phase==StreetModel.Phase.VehicleYellow?"amber":"red";
                        if(koreanCaptured.Add("vehicle-"+vehicle))
                        {
                            camera.cullingMask=koreanMask;camera.transform.position=new Vector3(3.38f,6.0f,0);camera.transform.LookAt(new Vector3(3.38f,5.61f,StarterConfig.CrossingZ+3));camera.fieldOfView=35;
                            koreanPreviews.Add(CaptureState(camera,request,target,image,"korean-vehicle-"+vehicle));
                        }
                        string pedestrian=detailStreet.Model.CanEnter?"walk":"stop";
                        if(koreanCaptured.Add("pedestrian-"+pedestrian))
                        {
                            camera.cullingMask=1<<30;camera.transform.position=new Vector3(-4.02f,2.75f,7);camera.transform.LookAt(new Vector3(-StarterConfig.CurbFurnitureX,2.61f,7));camera.fieldOfView=42;
                            koreanPreviews.Add(CaptureState(camera,request,target,image,"korean-pedestrian-"+pedestrian));
                        }
                    }
                    if(koreanCaptured.Count!=5)throw new InvalidOperationException("Korean signal capture cycle incomplete.");
                    climate.Preview(0,2);detailStreet.ResetModel();detailStreet.Model.SetWeather(1,1,1,1);detailStreet.Model.SetPopulation(100);detailStreet.ApplyViews();
                    var coatPerson=detailStreet.Walkers[0];var umbrellaPerson=detailStreet.Walkers[1];
                    if(!coatPerson.wearsCoat||coatPerson.body.sharedMesh!=coatPerson.coatMesh||coatPerson.umbrella.gameObject.activeSelf)throw new InvalidOperationException("Capture coat rig differs from tested state.");
                    PreviewLayer(coatPerson.root,31);PreviewLayer(umbrellaPerson.root,31);camera.cullingMask=1<<31;
                    coatPerson.root.SetPositionAndRotation(new Vector3(-10.0f,.16f,10),Quaternion.identity);
                    umbrellaPerson.root.SetPositionAndRotation(new Vector3(-9.0f,.16f,10),Quaternion.identity);
                    camera.transform.position=new Vector3(-9.5f,2.4f,14);camera.transform.LookAt(new Vector3(-9.5f,1.1f,10));camera.fieldOfView=60;
                    // Bake the tested rig's current skin pose for a same-frame Editor closeup.
                    // A newly swapped offscreen skin can retain its previous culling bounds until the next frame.
                    foreach(var view in new[]{coatPerson,umbrellaPerson})
                    {
                        var baked=new Mesh{name="Outfit detail pose"};view.body.BakeMesh(baked);outfitMeshes.Add(baked);
                        var probe=new GameObject("Actual outfit detail");probe.layer=31;probe.transform.SetPositionAndRotation(view.root.position,view.root.rotation);
                        probe.AddComponent<MeshFilter>().sharedMesh=baked;probe.AddComponent<MeshRenderer>().sharedMaterial=view.body.sharedMaterial;outfitProbes.Add(probe);
                        view.body.enabled=false;outfitSkins.Add(view.body);
                    }
                    koreanPreviews.Add(CaptureState(camera,request,target,image,"raincoat-and-umbrella"));
                }
                finally
                {
                    camera.cullingMask=koreanMask;foreach(var pair in koreanLayers)pair.Key.layer=pair.Value;
                    foreach(var skin in outfitSkins)skin.enabled=true;foreach(var probe in outfitProbes)UnityEngine.Object.DestroyImmediate(probe);foreach(var mesh in outfitMeshes)UnityEngine.Object.DestroyImmediate(mesh);
                    for(int i=0;i<originalCars.Length;i++)originalCars[i].ResetPosition(originalZ[i]);detailStreet.ResetModel();climate.Preview(0,0);detailStreet.ApplyViews();
                    camera.aspect=.45f;camera.fieldOfView=roadFov;camera.transform.SetPositionAndRotation(roadCameraPosition,roadCameraRotation);
                }
                var wiperPreviews=new System.Collections.Generic.List<string>();var wiperPixelChanges=new System.Collections.Generic.List<int>();
                var wiperModelsSeen=new System.Collections.Generic.HashSet<string>();var wiperLayers=new System.Collections.Generic.Dictionary<GameObject,int>();int wiperMask=camera.cullingMask;
                try
                {
                    detailStreet.ResetModel();detailStreet.ApplyViews();camera.cullingMask=1<<30;camera.fieldOfView=60;
                    foreach(var vehicle in originalCars)if(wiperModelsSeen.Add(vehicle.Model))
                    {
                        foreach(var child in vehicle.GetComponentsInChildren<Transform>(true)){wiperLayers.Add(child.gameObject,child.gameObject.layer);child.gameObject.layer=30;}
                        var wiperPair=vehicle.GetComponent<VehicleWipers>();var wiperScreen=wiperPair.Screen;
                        Vector3 wiperBottom=(wiperScreen[0]+wiperScreen[1])*.5f,wiperTop=(wiperScreen[2]+wiperScreen[3])*.5f;
                        Vector3 wiperUp=(wiperTop-wiperBottom).normalized,wiperNormal=Vector3.Cross(Vector3.right,wiperUp).normalized,wiperCentre=(wiperBottom+wiperTop)*.5f;
                        float wiperWidth=Vector3.Distance(wiperScreen[0],wiperScreen[1]);
                        camera.transform.position=vehicle.transform.TransformPoint(wiperCentre+wiperNormal*wiperWidth*2.1f+wiperUp*wiperWidth*.7f+Vector3.right*wiperWidth*.4f);
                        camera.transform.LookAt(vehicle.transform.TransformPoint(wiperCentre));
                        climate.Preview(0,0);detailStreet.ApplyViews();wiperPair.ResetState();wiperPair.Advance(12,0);
                        wiperPreviews.Add(CaptureState(camera,request,target,image,"wiper-"+vehicle.Model+"-dry-parked"));
                        climate.Preview(0,2);detailStreet.ApplyViews();wiperPair.Advance(wiperPair.StartDelay+.2,1);
                        wiperPreviews.Add(CaptureState(camera,request,target,image,"wiper-"+vehicle.Model+"-mid-sweep"));var wiperMidPixels=image.GetPixels32();
                        wiperPair.ResetState();wiperPair.Advance(wiperPair.StartDelay+.4,1);
                        wiperPreviews.Add(CaptureState(camera,request,target,image,"wiper-"+vehicle.Model+"-upper-sweep"));var wiperUpperPixels=image.GetPixels32();int changedWiperPixels=0;
                        for(int n=0;n<wiperMidPixels.Length;n++)if(Math.Abs(wiperMidPixels[n].r-wiperUpperPixels[n].r)+Math.Abs(wiperMidPixels[n].g-wiperUpperPixels[n].g)+Math.Abs(wiperMidPixels[n].b-wiperUpperPixels[n].b)>20)changedWiperPixels++;
                        if(changedWiperPixels<=16)throw new InvalidOperationException("Actual GPU wiper motion is not visible: "+vehicle.Model+" pixels="+changedWiperPixels);
                        wiperPixelChanges.Add(changedWiperPixels);wiperPair.Advance(2,0);climate.Preview(0,0);detailStreet.ApplyViews();
                        if(wiperPair.Moving||Mathf.Abs(wiperPair.Angle-VehicleWipers.ParkAngle)>.001f)throw new InvalidOperationException("Captured wiper fails to park after rain.");
                        wiperPreviews.Add(CaptureState(camera,request,target,image,"wiper-"+vehicle.Model+"-after-rain"));
                        climate.Preview(2,2);detailStreet.ApplyViews();wiperPair.ResetState();wiperPair.Advance(wiperPair.StartDelay+.4,1);
                        wiperPreviews.Add(CaptureState(camera,request,target,image,"wiper-"+vehicle.Model+"-night-grey"));
                        foreach(var pair in wiperLayers)pair.Key.layer=pair.Value;wiperLayers.Clear();
                    }
                    if(wiperPreviews.Count!=30)throw new InvalidOperationException("Wiper model captures incomplete.");
                }
                finally
                {
                    foreach(var pair in wiperLayers)pair.Key.layer=pair.Value;camera.cullingMask=wiperMask;
                    for(int i=0;i<originalCars.Length;i++)originalCars[i].ResetPosition(originalZ[i]);climate.Preview(0,0);detailStreet.ResetModel();detailStreet.ApplyViews();
                    camera.aspect=.45f;camera.fieldOfView=roadFov;camera.transform.SetPositionAndRotation(roadCameraPosition,roadCameraRotation);
                }
                var roundaboutPreviews=new System.Collections.Generic.List<string>();
                var roundaboutPixelChanges=new System.Collections.Generic.List<int>();int roundaboutMovingVehicles=0;
                var roundaboutMotionPositions=new System.Collections.Generic.List<Vector3>();
                var entranceImages=new System.Collections.Generic.List<string>();var entranceMovingCounts=new System.Collections.Generic.List<int>();var entrancePixelChanges=new System.Collections.Generic.List<int>();
                var denseImages=new System.Collections.Generic.List<string>();var densePixelChanges=new System.Collections.Generic.List<int>();
                var junctionVisibilityPreviews=new System.Collections.Generic.List<string>();int savedNightChangedPixels=0;float savedNightPixelDelta=0,savedNightFogFraction=0;
                try
                {
                    for(int i=0;i<originalCars.Length;i++)originalCars[i].ResetPosition(originalZ[i]);climate.Preview(0,0);detailStreet.ResetModel();
                    camera.aspect=.45f;camera.transform.position=new Vector3(0,46,161);camera.transform.LookAt(new Vector3(0,0,241));camera.fieldOfView=60;
                    detailStreet.Advance(60);detailStreet.ApplyViews();
                    foreach(int theme in new[]{0,2})
                    {climate.Preview(theme,0);detailStreet.ApplyViews();roundaboutPreviews.Add(CaptureState(camera,request,target,image,"roundabout-"+(theme==0?"day":"night")));}
                    climate.Preview(0,5);detailStreet.ApplyViews();roundaboutPreviews.Add(CaptureState(camera,request,target,image,"roundabout-storm"));
                    climate.Preview(0,0);detailStreet.ApplyViews();
                    camera.transform.position=new Vector3(28,38,177);camera.transform.LookAt(new Vector3(0,0,235));
                    roundaboutPreviews.Add(CaptureState(camera,request,target,image,"roundabout-oblique"));
                    camera.transform.position=new Vector3(0,46,161);camera.transform.LookAt(new Vector3(0,0,241));
                    var junctionTracked=new System.Collections.Generic.List<int>();
                    for(int wait=0;wait<18000;wait++)
                    {
                        junctionTracked.Clear();var round=detailStreet.Model.Roundabout;
                        for(int i=0;i<round.States.Length;i++)
                        {var car=detailStreet.Model.Cars[i];var state=round.States[i];if(car.active&&!car.roundHidden&&state.path!=null&&state.progress>=state.path.entry&&state.progress<state.path.exit-15&&car.distance>.00001f)junctionTracked.Add(i);}
                        if(junctionTracked.Count>=2)break;detailStreet.Advance(1d/30);
                    }
                    if(junctionTracked.Count<2)throw new InvalidOperationException("No simultaneous actual circulating vehicles for junction captures.");
                    var multiStart=new Vector2[junctionTracked.Count];for(int i=0;i<multiStart.Length;i++)multiStart[i]=RoundaboutTraffic.Position(detailStreet.Model.Cars[junctionTracked[i]]);
                    Color32[] previousPixels=null;
                    for(int frame=0;frame<4;frame++)
                    {
                        if(frame>0)detailStreet.Advance(1);detailStreet.ApplyViews();
                        var car=detailStreet.Model.Cars[junctionTracked[0]];roundaboutMotionPositions.Add(new Vector3(car.X,car.roundabout?car.roundYaw:(car.Direction<0?180:0),(float)car.z));
                        roundaboutPreviews.Add(CaptureState(camera,request,target,image,"roundabout-motion-"+frame));
                        var currentPixels=image.GetPixels32();if(previousPixels!=null)
                        {
                            int changed=0;for(int n=0;n<currentPixels.Length;n++)if(Math.Abs(currentPixels[n].r-previousPixels[n].r)+Math.Abs(currentPixels[n].g-previousPixels[n].g)+Math.Abs(currentPixels[n].b-previousPixels[n].b)>20)changed++;
                            if(changed<=16)throw new InvalidOperationException("Real junction motion is not visible in GPU captures.");roundaboutPixelChanges.Add(changed);
                        }
                        previousPixels=currentPixels;
                    }
                    for(int i=0;i<multiStart.Length;i++)
                    {if(Vector2.Distance(multiStart[i],RoundaboutTraffic.Position(detailStreet.Model.Cars[junctionTracked[i]]))<.5f)throw new InvalidOperationException("A tracked circulating vehicle stays frozen across actual GPU frames.");}
                    roundaboutMovingVehicles=junctionTracked.Count;
                    // Natural lower entrance flow, without seeded density or
                    // occupancy retention. Wait for a real approaching vehicle.
                    for(int i=0;i<originalCars.Length;i++)originalCars[i].ResetPosition(originalZ[i]);climate.Preview(2,0);detailStreet.ResetModel();
                    int trackedEntrance=-1;
                    for(int wait=0;wait<18000&&trackedEntrance<0;wait++)
                    {
                        detailStreet.Advance(1d/30);
                        for(int i=0;i<detailStreet.Model.Cars.Length;i++)
                        {var c=detailStreet.Model.Cars[i];var st=detailStreet.Model.Roundabout.States[i];if(c.active&&!c.roundHidden&&c.roundabout&&st.origin==0&&st.path!=null&&st.progress<st.path.entry-12&&c.distance>.00001f){trackedEntrance=i;break;}}
                    }
                    if(trackedEntrance<0)throw new InvalidOperationException("No naturally moving lower approach for GPU frames.");
                    var entranceStart=RoundaboutTraffic.Position(detailStreet.Model.Cars[trackedEntrance]);previousPixels=null;
                    for(int frame=0;frame<4;frame++)
                    {
                        if(frame>0)detailStreet.Advance(1);detailStreet.ApplyViews();int count=0;
                        foreach(var c in detailStreet.Model.Cars)if(c.active&&!c.roundHidden&&c.Direction>0&&c.z>=160&&c.z<228&&c.distance>.00001f)count++;
                        entranceMovingCounts.Add(count);entranceImages.Add(CaptureState(camera,request,target,image,"roundabout-entrance-flow-"+frame));
                        var currentPixels=image.GetPixels32();if(previousPixels!=null)
                        {int changed=0;for(int n=0;n<currentPixels.Length;n++)if(Math.Abs(currentPixels[n].r-previousPixels[n].r)+Math.Abs(currentPixels[n].g-previousPixels[n].g)+Math.Abs(currentPixels[n].b-previousPixels[n].b)>20)changed++;if(changed<=16)throw new InvalidOperationException("Actual entrance-flow GPU frames contain no motion.");entrancePixelChanges.Add(changed);}
                        previousPixels=currentPixels;
                    }
                    if(Vector2.Distance(entranceStart,RoundaboutTraffic.Position(detailStreet.Model.Cars[trackedEntrance]))<.5f)throw new InvalidOperationException("Tracked lower entrance car remains frozen in real GPU frames.");
                    // Explicit six-car density fixture: not a claim that all six
                    // arrive together in the unmodified saved traffic sequence.
                    for(int i=0;i<originalCars.Length;i++)originalCars[i].ResetPosition(originalZ[i]);climate.Preview(0,0);detailStreet.ResetModel();
                    var dense=RoundaboutChecks.DenseIndices(detailStreet.Model.Cars);RoundaboutChecks.ConfigureDense(detailStreet.Model,dense);
                    var denseStart=new Vector2[6];for(int i=0;i<6;i++)denseStart[i]=RoundaboutTraffic.Position(detailStreet.Model.Cars[dense[i]]);
                    previousPixels=null;
                    for(int frame=0;frame<4;frame++)
                    {
                        detailStreet.Advance(frame>0?1:1d/30);detailStreet.ApplyViews();
                        foreach(int i in dense){var c=detailStreet.Model.Cars[i];if(!c.active||!c.roundabout||(frame>0&&c.distance<=.00001f))throw new InvalidOperationException("Six-car density GPU fixture contains a frozen car.");}
                        denseImages.Add(CaptureState(camera,request,target,image,"roundabout-six-fixture-"+frame));
                        var currentPixels=image.GetPixels32();if(previousPixels!=null)
                        {int changed=0;for(int n=0;n<currentPixels.Length;n++)if(Math.Abs(currentPixels[n].r-previousPixels[n].r)+Math.Abs(currentPixels[n].g-previousPixels[n].g)+Math.Abs(currentPixels[n].b-previousPixels[n].b)>20)changed++;if(changed<=16)throw new InvalidOperationException("Six-car density motion is absent in GPU pixels.");densePixelChanges.Add(changed);}
                        previousPixels=currentPixels;
                    }
                    for(int i=0;i<6;i++)if(Vector2.Distance(denseStart[i],RoundaboutTraffic.Position(detailStreet.Model.Cars[dense[i]]))<.5f)throw new InvalidOperationException("Six-car density GPU fixture does not move each vehicle.");

                }
                finally
                {
                    for(int i=0;i<originalCars.Length;i++)originalCars[i].ResetPosition(originalZ[i]);climate.Preview(0,0);detailStreet.ResetModel();detailStreet.ApplyViews();
                    camera.aspect=.45f;camera.fieldOfView=roadFov;camera.transform.SetPositionAndRotation(roadCameraPosition,roadCameraRotation);
                }
                // Reproduce the user's saved-camera night problem, rather than checking only an inspection camera.
                try
                {
                    camera.aspect=.45f;camera.fieldOfView=roadFov;camera.transform.SetPositionAndRotation(roadCameraPosition,roadCameraRotation);
                    climate.Preview(0,0);detailStreet.ApplyViews();junctionVisibilityPreviews.Add(CaptureState(camera,request,target,image,"roundabout-saved-day"));
                    climate.Preview(2,0);detailStreet.ApplyViews();
                    float clearStart=RenderSettings.fogStartDistance,clearEnd=RenderSettings.fogEndDistance;
                    float depth=Vector3.Dot(new Vector3(0,.25f,RoundaboutTraffic.CenterZ)-camera.transform.position,camera.transform.forward);
                    savedNightFogFraction=Mathf.Clamp01((depth-clearStart)/(clearEnd-clearStart));
                    RenderSettings.fogStartDistance=130;RenderSettings.fogEndDistance=270;
                    junctionVisibilityPreviews.Add(CaptureState(camera,request,target,image,"roundabout-saved-night-oldfog"));var hiddenPixels=image.GetPixels32();
                    RenderSettings.fogStartDistance=clearStart;RenderSettings.fogEndDistance=clearEnd;
                    junctionVisibilityPreviews.Add(CaptureState(camera,request,target,image,"roundabout-saved-night"));var clearPixels=image.GetPixels32();
                    int x0=image.width,y0=image.height,x1=0,y1=0;
                    foreach(float x in new[]{-22f,22f})foreach(float z in new[]{213f,257f})
                    {var q=camera.WorldToViewportPoint(new Vector3(x,.25f,z));x0=Mathf.Min(x0,Mathf.FloorToInt(q.x*image.width));x1=Mathf.Max(x1,Mathf.CeilToInt(q.x*image.width));y0=Mathf.Min(y0,Mathf.FloorToInt(q.y*image.height));y1=Mathf.Max(y1,Mathf.CeilToInt(q.y*image.height));}
                    x0=Mathf.Clamp(x0,0,image.width-1);x1=Mathf.Clamp(x1,0,image.width-1);y0=Mathf.Clamp(y0,0,image.height-1);y1=Mathf.Clamp(y1,0,image.height-1);int samples=0;
                    for(int y=y0;y<=y1;y++)for(int x=x0;x<=x1;x++)
                    {int n=y*image.width+x;int delta=Math.Abs(clearPixels[n].r-hiddenPixels[n].r)+Math.Abs(clearPixels[n].g-hiddenPixels[n].g)+Math.Abs(clearPixels[n].b-hiddenPixels[n].b);savedNightPixelDelta+=delta/(255f*3);if(delta>20)savedNightChangedPixels++;samples++;}
                    savedNightPixelDelta/=Mathf.Max(1,samples);
                    if(savedNightFogFraction>=.15f||savedNightChangedPixels<32||savedNightPixelDelta<.002f)throw new InvalidOperationException("Saved night-camera junction remains lost in fog: obscured="+savedNightFogFraction+" changed="+savedNightChangedPixels+" delta="+savedNightPixelDelta);
                    climate.Preview(2,4);detailStreet.ApplyViews();junctionVisibilityPreviews.Add(CaptureState(camera,request,target,image,"roundabout-saved-fog"));
                }
                finally{climate.Preview(0,0);detailStreet.ApplyViews();}
                var reflectionGpu=GroundScene.CheckReflectionGpu();
                var cameraOptionImages=new System.Collections.Generic.List<string>();var cameraChoice=camera.GetComponent<CityCamera>();
                if(cameraChoice==null)throw new InvalidOperationException("Camera option missing from saved scene.");
                try
                {
                    // ResetModel restores serialized objects; a real production tick applies
                    // hidden junction sources and circulating poses before taking the new view.
                    for(int i=0;i<originalCars.Length;i++)originalCars[i].ResetPosition(originalZ[i]);
                    climate.Preview(0,0);detailStreet.ResetModel();detailStreet.Advance(60);detailStreet.ApplyViews();
                    foreach(var car in originalCars)if(car.gameObject.activeSelf)
                    {
                        Vector3 p=car.transform.position;
                        if(Vector2.Distance(new Vector2(p.x,p.z),new Vector2(0,RoundaboutTraffic.CenterZ))<13.9f)
                            throw new InvalidOperationException("Camera option capture contains an unsynchronized vehicle inside the roundabout island.");
                    }
                    camera.aspect=.45f;cameraChoice.Preview(1);
                    foreach(int theme in new[]{0,1,2})
                    {climate.Preview(theme,0);detailStreet.ApplyViews();cameraOptionImages.Add(CaptureState(camera,request,target,image,"camera-option-roundabout-"+(theme==0?"day":theme==1?"dusk":"night")));}
                    climate.Preview(0,2);detailStreet.ApplyViews();cameraOptionImages.Add(CaptureState(camera,request,target,image,"camera-option-roundabout-rain"));
                }
                finally{cameraChoice.Preview(0);climate.Preview(0,0);detailStreet.ApplyViews();}
                File.WriteAllText("Reports/preview-result.json", JsonUtility.ToJson(new Report {
                    riverfrontImages=riverfrontPreviews.ToArray(),cameraOptionImages=cameraOptionImages.ToArray(),cameraOptionTrafficSimulatedSeconds=60,
                    entranceImages=entranceImages.ToArray(),entranceMovingCounts=entranceMovingCounts.ToArray(),entrancePixelChanges=entrancePixelChanges.ToArray(),denseMovingVehicles=6,denseImages=denseImages.ToArray(),densePixelChanges=densePixelChanges.ToArray(),roundaboutMovingVehicles=roundaboutMovingVehicles,roundaboutImages=roundaboutPreviews.ToArray(),roundaboutPixelChanges=roundaboutPixelChanges.ToArray(),roundaboutMotionPositions=roundaboutMotionPositions.ToArray(),
                    junctionVisibilityImages=junctionVisibilityPreviews.ToArray(),savedNightChangedPixels=savedNightChangedPixels,savedNightPixelDelta=savedNightPixelDelta,savedNightFogFraction=savedNightFogFraction,
                    result = "PASS", version = StarterConfig.VersionName, editor = Application.unityVersion,
                    graphicsApi = SystemInfo.graphicsDeviceType.ToString(), width = width, height = height,
                    image = output, source = "Unity Editor URP camera; not a phone screenshot or FPS test"
                    , wiperImages=wiperPreviews.ToArray(),wiperPixelChanges=wiperPixelChanges.ToArray(),koreanSignalImages=koreanPreviews.ToArray(),reflectionGpu=reflectionGpu,wetReflectionImages=reflectionPreviews.ToArray(),materialColors = colors, climateImages = previews.ToArray(), vehicleImages = vehiclePreviews.ToArray(),frontageImages=frontagePreviews.ToArray(),drivingImages=drivingPreviews.ToArray(),headlightColourImages=colourPreviews.ToArray(),cameraImages=cameraPreviews.ToArray(),landscapeImages=landscapePreviews.ToArray(),skyImages=skyPreviews.ToArray(),groundImages=groundPreviews.ToArray(),sceneryImages=sceneryPreviews.ToArray(),heavyImages=heavyPreviews.ToArray(),busStopImages=stopPreviews.ToArray()
                }, true));
                Debug.Log("PASS: real city camera preview saved: " + output);
            }
            catch (Exception exception) { Fail(exception); return; }
            finally
            {
                RenderTexture.active = previous;
                if (target != null) { target.Release(); UnityEngine.Object.DestroyImmediate(target); }
                if (image != null) UnityEngine.Object.DestroyImmediate(image);
            }
            EditorApplication.Exit(0);
        }

        private static string CaptureState(Camera camera, UniversalRenderPipeline.SingleCameraRequest request, RenderTexture target, Texture2D image, string label)
        {
            // Snap/temporary camera changes need a zero-delta effect refresh before readback.
            // Keep the exact blend/active clock while clearing old weather and aligning billboards.
            var climate=UnityEngine.Object.FindFirstObjectByType<CityClimate>();
            UnityEngine.Object.FindFirstObjectByType<RoadSurface>().Sync(true);
            climate.Effects.Advance(0,climate.RainGain,(float)climate.WeatherBlend.Weights[3],climate.WindGain,climate.WeatherBlend.Target==5);
            if(climate.RainGain==0&&climate.WeatherBlend.Weights[3]==0)
                if(GameObject.Find("Rain Pool").GetComponent<Renderer>().enabled||GameObject.Find("Snow Pool").GetComponent<Renderer>().enabled)
                    throw new InvalidOperationException("Clear-weather capture still contains precipitation: "+label);
            RenderPipeline.SubmitRenderRequest(camera, request); RenderTexture.active = target;
            image.ReadPixels(new Rect(0, 0, image.width, image.height), 0, 0); image.Apply();
            int visible = 0, pink = 0; foreach (Color32 pixel in image.GetPixels32())
            { if (pixel.r + pixel.g + pixel.b > 15) visible++; if (pixel.r > 220 && pixel.b > 220 && pixel.g < 50) pink++; }
            if (visible < image.width * image.height / 3 || pink > image.width * image.height / 100) throw new InvalidOperationException("Climate preview is black or pink: " + label);
            if(label=="roundabout-night")
            {
                int clipped=0;foreach(var pixel in image.GetPixels32())if(pixel.r>=250&&pixel.g>=250)clipped++;
                if(clipped>image.width*image.height*.08f)throw new InvalidOperationException("Junction night lighting clips grass/road detail: "+clipped+" pixels.");
            }
            if(label=="korean-pedestrian-stop"||label=="korean-pedestrian-walk")
            {
                int lit=0;bool walk=label.EndsWith("walk");
                foreach(var p in image.GetPixels32())if(walk?p.g>100&&p.g>p.r*2&&p.g>p.b*2:p.r>100&&p.r>p.g*2&&p.r>p.b*2)lit++;
                if(lit<100)throw new InvalidOperationException("Pedestrian icon is obscured in actual GPU pixels: "+label);
            }
            string path = "Reports/city-preview-" + StarterConfig.VersionName + "-" + label + ".png"; File.WriteAllBytes(path, image.EncodeToPNG()); return path;
        }

        private static void Fail(Exception exception)
        {
            EditorApplication.update -= CaptureWhenReady;
            Directory.CreateDirectory("Reports");
            File.WriteAllText("Reports/preview-result.json", JsonUtility.ToJson(new Report {
                result = "FAILED", version = StarterConfig.VersionName, editor = Application.unityVersion,
                source = "Editor preview failed; APK verification is separate"
            }, true));
            Debug.LogException(exception);
            EditorApplication.Exit(1);
        }

        [Serializable] private sealed class Report
        {
            public string[] riverfrontImages;public string result, version, editor, graphicsApi, image, source;
            public int width, height;
            public string[] entranceImages;public int[] entranceMovingCounts,entrancePixelChanges;public int denseMovingVehicles;public string[] denseImages;public int[] densePixelChanges;public int roundaboutMovingVehicles;public string[] roundaboutImages;public int[] roundaboutPixelChanges;public Vector3[] roundaboutMotionPositions;
            public string[] junctionVisibilityImages;public int savedNightChangedPixels;public float savedNightPixelDelta,savedNightFogFraction;
            public string[] cameraOptionImages;
            public int cameraOptionTrafficSimulatedSeconds;
            public string[] headlightColourImages;
            public string[] wetReflectionImages,koreanSignalImages;
            public string[] wiperImages;public int[] wiperPixelChanges;
            public GroundScene.ReflectionGpuReport reflectionGpu;
            public string[] materialColors, climateImages, vehicleImages,frontageImages,drivingImages,cameraImages,landscapeImages,skyImages,groundImages,sceneryImages,heavyImages,busStopImages;
        }
    }
}
