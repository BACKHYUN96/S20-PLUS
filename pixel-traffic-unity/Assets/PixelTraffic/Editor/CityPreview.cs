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
                var street = UnityEngine.Object.FindFirstObjectByType<StreetSimulation>();
                if (street != null) { street.ResetModel(); street.Advance(30); street.ApplyViews(); }
                UnityEngine.Object.FindFirstObjectByType<CityClimate>().Initialize();
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
                var originalCars=UnityEngine.Object.FindObjectsByType<PrototypeDrive>(FindObjectsSortMode.None);var originalZ=new float[originalCars.Length];for(int i=0;i<originalCars.Length;i++)originalZ[i]=originalCars[i].transform.position.z;
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
                camera.transform.SetPositionAndRotation(roadCameraPosition,roadCameraRotation);camera.fieldOfView=roadFov;landscapeClimate.Preview(0,0);
                var sceneryPreviews=new System.Collections.Generic.List<string>();
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
                for(int i=0;i<originalCars.Length;i++)originalCars[i].ResetPosition(originalZ[i]);
                detailStreet.ResetModel();climate.Preview(0,0);camera.transform.SetPositionAndRotation(roadCameraPosition,roadCameraRotation);camera.fieldOfView=roadFov;
                int tracked=-1,captured=0;
                for(int tick=0;tick<18000&&captured<4;tick++)
                {
                    detailStreet.Advance(StreetModel.Dt);detailStreet.ApplyViews();
                    var cars=detailStreet.Model.Cars;
                    if(tracked<0)for(int i=0;i<cars.Length;i++)if(cars[i].maneuver==LaneChanges.Stage.Signaling&&cars[i].signalTicks==0)
                    {Vector3 screen=camera.WorldToViewportPoint(new Vector3(cars[i].X,.8f,(float)cars[i].z));if(screen.z>0&&screen.x>.1f&&screen.x<.9f&&screen.y>.2f&&screen.y<.8f){tracked=i;captured=1;drivingPreviews.Add(CaptureState(camera,request,target,image,"lane-signal-start"));break;}}
                    if(tracked>=0)
                    {
                        var car=cars[tracked];
                        if(captured==1&&car.maneuver==LaneChanges.Stage.Merging){captured=2;drivingPreviews.Add(CaptureState(camera,request,target,image,"lane-change-start"));}
                        else if(captured==2&&car.maneuver==LaneChanges.Stage.Merging&&car.mergeTicks==car.MergeDuration/2){captured=3;drivingPreviews.Add(CaptureState(camera,request,target,image,"lane-change-mid"));}
                        else if(captured==3&&car.maneuver==LaneChanges.Stage.Idle){captured=4;drivingPreviews.Add(CaptureState(camera,request,target,image,"lane-change-complete"));}
                        else if(captured==1&&car.maneuver==LaneChanges.Stage.Idle){tracked=-1;captured=0;drivingPreviews.RemoveAt(drivingPreviews.Count-1);}
                    }
                }
                if(captured!=4)throw new InvalidOperationException("No visible real lane maneuver captured.");
                // Capture real stop transfers and both phases of the same hazard clock.
                for(int i=0;i<originalCars.Length;i++)originalCars[i].ResetPosition(originalZ[i]);
                detailStreet.ResetModel();var stopPreviews=new System.Collections.Generic.List<string>();
                var stopTarget=new RenderTexture(720,960,24,RenderTextureFormat.ARGB32){antiAliasing=2};stopTarget.Create();
                var stopImage=new Texture2D(720,960,TextureFormat.RGB24,false);var seenStop=new bool[2,4];int stopCaptured=0;
                try
                {
                    camera.aspect=.75f;camera.fieldOfView=58;var stopRequest=new UniversalRenderPipeline.SingleCameraRequest{destination=stopTarget};
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
                                camera.transform.position=new Vector3(side*10.55f,5.5f,z-side*6);camera.transform.LookAt(new Vector3(side*7.45f,1.25f,z));
                                climate.Preview(stage<2?0:2,0);detailStreet.ApplyViews();
                                string[] labels={"alighting-day","boarding-day","hazards-on-night","hazards-off-night"};
                                stopPreviews.Add(CaptureState(camera,stopRequest,stopTarget,stopImage,"bus-stop-"+(stop==0?"west-":"east-")+labels[stage]));seenStop[stop,stage]=true;stopCaptured++;
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
                File.WriteAllText("Reports/preview-result.json", JsonUtility.ToJson(new Report {
                    result = "PASS", version = StarterConfig.VersionName, editor = Application.unityVersion,
                    graphicsApi = SystemInfo.graphicsDeviceType.ToString(), width = width, height = height,
                    image = output, source = "Unity Editor URP camera; not a phone screenshot or FPS test"
                    , materialColors = colors, climateImages = previews.ToArray(), vehicleImages = vehiclePreviews.ToArray(),frontageImages=frontagePreviews.ToArray(),drivingImages=drivingPreviews.ToArray(),cameraImages=cameraPreviews.ToArray(),landscapeImages=landscapePreviews.ToArray(),sceneryImages=sceneryPreviews.ToArray(),heavyImages=heavyPreviews.ToArray(),busStopImages=stopPreviews.ToArray()
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
            climate.Effects.Advance(0,climate.RainGain,(float)climate.WeatherBlend.Weights[3],climate.WindGain,climate.WeatherBlend.Target==5);
            if(climate.RainGain==0&&climate.WeatherBlend.Weights[3]==0)
                if(GameObject.Find("Rain Pool").GetComponent<Renderer>().enabled||GameObject.Find("Snow Pool").GetComponent<Renderer>().enabled)
                    throw new InvalidOperationException("Clear-weather capture still contains precipitation: "+label);
            RenderPipeline.SubmitRenderRequest(camera, request); RenderTexture.active = target;
            image.ReadPixels(new Rect(0, 0, image.width, image.height), 0, 0); image.Apply();
            int visible = 0, pink = 0; foreach (Color32 pixel in image.GetPixels32())
            { if (pixel.r + pixel.g + pixel.b > 15) visible++; if (pixel.r > 220 && pixel.b > 220 && pixel.g < 50) pink++; }
            if (visible < image.width * image.height / 3 || pink > image.width * image.height / 100) throw new InvalidOperationException("Climate preview is black or pink: " + label);
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
            public string result, version, editor, graphicsApi, image, source;
            public int width, height;
            public string[] materialColors, climateImages, vehicleImages,frontageImages,drivingImages,cameraImages,landscapeImages,sceneryImages,heavyImages,busStopImages;
        }
    }
}
