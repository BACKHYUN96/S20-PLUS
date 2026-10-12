using System;
using System.Collections.Generic;
using UnityEditor;
using UnityEngine;

namespace PixelTraffic.UnityPrototype.Editor
{
    internal static class RoundaboutChecks
    {
        static void Need(bool value,string message){if(!value)throw new InvalidOperationException("Roundabout: "+message);}
        static Vector2[] Corners(StreetModel.Car c)
        {
            float a=RoundaboutTraffic.Yaw(c)*Mathf.Deg2Rad;var f=new Vector2(Mathf.Sin(a),Mathf.Cos(a));var s=new Vector2(f.y,-f.x);var p=RoundaboutTraffic.Position(c);
            return new[]{p-f*c.length*.5f-s*c.width*.5f,p+f*c.length*.5f-s*c.width*.5f,p+f*c.length*.5f+s*c.width*.5f,p-f*c.length*.5f+s*c.width*.5f};
        }
        static bool Separate(Vector2[] a,Vector2[] b)
        {
            foreach(var polygon in new[]{a,b})for(int edge=0;edge<2;edge++)
            {
                Vector2 line=polygon[edge+1]-polygon[edge],axis=new Vector2(-line.y,line.x).normalized;
                float minA=float.MaxValue,maxA=float.MinValue,minB=float.MaxValue,maxB=float.MinValue;
                foreach(var p in a){float d=Vector2.Dot(p,axis);minA=Mathf.Min(minA,d);maxA=Mathf.Max(maxA,d);}
                foreach(var p in b){float d=Vector2.Dot(p,axis);minB=Mathf.Min(minB,d);maxB=Mathf.Max(maxB,d);}
                if(maxA<minB-.005f||maxB<minA-.005f)return true;
            }
            return false;
        }
        static string Summary(StreetModel model)
        {
            for(int i=0;i<model.Cars.Length;i++)
            {
                var c=model.Cars[i];var state=model.Roundabout.States[i];if(!c.active||c.roundHidden||state.path==null)continue;
                int blocker=model.Roundabout.AdmissionBlocker(i,out float at);Vector2 p=state.path.Pose(at,out float yaw);
                Debug.LogError("Roundabout diagnostic error: car="+i+" origin="+state.origin+" destination="+state.destination+" pose="+RoundaboutTraffic.Position(c)+" width="+c.width+" length="+c.length+" stage="+state.stage+" progress="+state.progress+" admissionBlocker="+blocker+" blockedAt="+p+" distance="+at+" owner="+model.Roundabout.Owner);
            }
            return " owner="+model.Roundabout.Owner;
        }
        // Use the saved fleet's real dimensions and the production clock. Every
        // unobstructed approach must cross the handoff and merge without a stop.
        static int UninterruptedEntries(StreetModel.Car[] templates)
        {
            int cases=0;
            foreach(var source in templates)for(int variant=-1;variant<4;variant++)
            {
                int origin=Mathf.Max(0,variant);
                var car=new StreetModel.Car{lane=variant<0?2:3,z=180,cruise=6,speed=6,
                    length=source.length,width=source.width,height=source.height,bus=source.bus};
                var model=new StreetModel(4,new[]{car});model.EnableRoundabout();
                if(origin>0)
                {
                    var state=model.Roundabout.States[0];state.path=model.Roundabout.Route(origin,0,0);
                    state.origin=origin;state.destination=0;state.lane=0;state.progress=state.path.entry-45;state.stage=RoundaboutTraffic.Stage.Approach;
                    car.roundabout=true;car.roundPosition=state.path.Pose(state.progress,out car.roundYaw);car.z=car.roundPosition.y;car.speed=4.8f;
                }
                for(int tick=0;tick<2400&&model.Roundabout.Completed==0;tick++)
                {model.Tick();Need(car.distance>.00001f,"unobstructed vehicle stops at handoff/merge: origin="+origin+" length="+car.length);}
                Need(model.Roundabout.Completed==1,"unobstructed vehicle never leaves the junction.");cases++;
            }
            return cases;
        }
        internal static Report Validate()
        {
            var controller=UnityEngine.Object.FindFirstObjectByType<StreetSimulation>();controller.ResetModel();Need(controller.Model.Roundabout!=null,"saved runtime controller omits the junction.");
            int uninterruptedCases=UninterruptedEntries(controller.Model.Cars);
            Transform root=GameObject.Find("Roundabout Terrain").transform;var filters=root.GetComponentsInChildren<MeshFilter>();Need(filters.Length==5,"terrain loses material sharing.");
            int triangles=0;foreach(var f in filters)
            {
                Mesh mesh=f.sharedMesh;triangles+=mesh.triangles.Length/3;Need(mesh.vertexCount==mesh.uv.Length&&mesh.normals.Length==mesh.vertexCount,"mesh normals/UV absent.");
                Need(f.GetComponent<Collider>()==null&&f.GetComponent<Renderer>().sharedMaterial!=null,"terrain adds collider or misses shared material.");
                foreach(Vector3 v in mesh.vertices)Need(!float.IsNaN(v.x)&&!float.IsNaN(v.y)&&!float.IsNaN(v.z),"non-finite terrain point.");
                if(f.name=="Roundabout Asphalt")foreach(Vector3 normal in mesh.normals)Need(normal.y>.99f,"asphalt has an invisible back-facing road patch.");
            }
            Need(triangles<=400,"junction exceeds its reclaimed far-marking triangle budget.");
            var lighting=GameObject.Find("Roundabout Lighting");Need(lighting!=null&&lighting.transform.childCount==2&&lighting.GetComponentsInChildren<Light>().Length==0&&lighting.GetComponentsInChildren<Collider>().Length==0,"junction lighting lacks two shared-material fixtures or adds realtime lights/obstacles.");
            foreach(var lamp in lighting.GetComponentsInChildren<MeshRenderer>())Need(lamp.sharedMaterial!=null,"junction fixture misses shared material.");
            var island=root.Find("Roundabout Island").GetComponent<MeshFilter>().sharedMesh;
            Need(Mathf.Abs(island.bounds.center.z-235)<.001f&&Mathf.Abs(island.bounds.size.x-20)<.001f&&island.bounds.max.y<.3f,"central island obscures traffic or leaves the circle.");
            var camera=Camera.main;float aspect=camera.aspect;foreach(float a in new[]{9f/20,9f/16}){camera.aspect=a;var p=camera.WorldToViewportPoint(new Vector3(0,0,235));Need(p.x>0&&p.x<1&&p.y>0&&p.y<1&&p.z>0,"junction outside saved portrait.");}camera.aspect=aspect;
            foreach(float a in new[]{9f/20,9f/16})
            {
                camera.aspect=a;foreach(Vector3 point in new[]{new Vector3(-140,4,302),new Vector3(140,4,302),new Vector3(-140,4,230.2f),new Vector3(140,4,239.8f)})
                {var q=camera.WorldToViewportPoint(point);Need(q.x<-.05f||q.x>1.05f||q.z<0,"recycle endpoint remains visible in saved portrait.");}
            }
            camera.aspect=aspect;
            var cameraChoice=camera.GetComponent<CityCamera>();Need(cameraChoice!=null,"saved camera choice component missing.");
            Vector3 originalPosition=camera.transform.position;Quaternion originalRotation=camera.transform.rotation;float originalFov=camera.fieldOfView;
            var cameraProbe=UnityEngine.Object.FindFirstObjectByType<PrototypeDrive>();Vector3 carBefore=cameraProbe.transform.position;
            try
            {
                cameraChoice.Preview(1);
                foreach(float a in new[]{9f/20,9f/16})
                {
                    camera.aspect=a;
                    for(int sample=0;sample<16;sample++)
                    {
                        float angle=sample*Mathf.PI/8;var q=camera.WorldToViewportPoint(new Vector3(Mathf.Cos(angle)*19.5f,.3f,235+Mathf.Sin(angle)*19.5f));
                        Need(q.x>.05f&&q.x<.95f&&q.y>.05f&&q.y<.95f&&q.z>0,"roundabout option clips the ring.");
                    }
                    foreach(Vector3 point in new[]{new Vector3(-140,4,302),new Vector3(140,4,302),new Vector3(-140,4,230.2f),new Vector3(140,4,239.8f)})
                    {var q=camera.WorldToViewportPoint(point);Need(q.x<-.05f||q.x>1.05f||q.z<0,"roundabout option exposes a recycle endpoint.");}
                }
                cameraChoice.Preview(0);
                Need(Vector3.Distance(camera.transform.position,originalPosition)<.001f&&Quaternion.Angle(camera.transform.rotation,originalRotation)<.01f&&Mathf.Abs(camera.fieldOfView-originalFov)<.001f,"city view is not restored.");
                Need(cameraProbe.transform.position==carBefore,"camera choice resets traffic.");
            }
            finally{cameraChoice.Preview(0);camera.aspect=aspect;}
            var drives=UnityEngine.Object.FindObjectsByType<PrototypeDrive>(FindObjectsInactive.Include,FindObjectsSortMode.None);var poses=new float[drives.Length];for(int i=0;i<drives.Length;i++)poses[i]=drives[i].transform.position.z;
            var climate=UnityEngine.Object.FindFirstObjectByType<CityClimate>();int pairChecks=0,ringSamples=0,signalFrames=0,hiddenResets=0;float maximumStep=0;
            int entriesWithoutStop=0,approachStoppedTicks=0;
            int entries=0,completed=0,returns=0,yields=0,recycles=0,departures=0,peakMoving=0,multipleMovingTicks=0,lastCompletionTick=0,lastCompleted=0,maxCompletionGap=0;int[] destinations=null;
            try
            {
                cameraChoice.Preview(1);climate.Preview(0,0);controller.ResetModel();var model=controller.Model;
                var previous=new Vector2[model.Cars.Length];var oldHidden=new bool[model.Cars.Length];var oldRound=new bool[model.Cars.Length];var oldLane=new int[model.Cars.Length];
                var visit=new int[model.Cars.Length];var stoppedBeforeMerge=new bool[visit.Length];var mergeRecorded=new bool[visit.Length];
                for(int tick=0;tick<18000;tick++)
                {
                    for(int i=0;i<previous.Length;i++){previous[i]=RoundaboutTraffic.Position(model.Cars[i]);oldHidden[i]=model.Cars[i].roundHidden;oldRound[i]=model.Cars[i].roundabout;oldLane[i]=model.Cars[i].lane;}
                    controller.Advance(1d/30);model=controller.Model;var round=model.Roundabout;int moving=0;
                    if(round.Completed!=lastCompleted){lastCompleted=round.Completed;lastCompletionTick=tick;}
                    maxCompletionGap=Mathf.Max(maxCompletionGap,tick-lastCompletionTick);
                    if(tick-lastCompletionTick>=2700)Need(false,"junction has no completed route for90seconds."+Summary(model));
                    Need(!(model.CanEnter||model.Signal==StreetModel.Phase.PedestrianClearance)||!model.RoadOccupied(),"junction breaks crossing clearance.");
                    for(int i=0;i<model.Cars.Length;i++)
                    {
                        var c=model.Cars[i];if(!c.active||c.roundHidden)continue;
                        float step=Vector2.Distance(previous[i],RoundaboutTraffic.Position(c));
                        bool hiddenSpawn=oldHidden[i];bool bottomReset=!oldRound[i]&&!c.roundabout&&c.lane!=oldLane[i]&&c.z<StarterConfig.RouteStart+1;
                        if(hiddenSpawn){Need(Mathf.Abs(c.X)>130,"visible junction source teleport.");hiddenResets++;}
                        else if(!bottomReset){maximumStep=Mathf.Max(maximumStep,step);Need(step<=c.distance+((oldRound[i]||c.roundabout) ? .001f : .055f),"visible route discontinuity: "+i+" step="+step+" travel="+c.distance);}
                        if(c.roundabout)
                        {
                            var p=RoundaboutTraffic.Position(c);var state=round.States[i];
                            if(visit[i]!=state.visits){visit[i]=state.visits;stoppedBeforeMerge[i]=false;mergeRecorded[i]=false;}
                            if(state.progress>=state.path.entry-40&&state.progress<state.path.entry&&c.distance<=.00001f)
                            {approachStoppedTicks++;stoppedBeforeMerge[i]=true;}
                            if(state.progress>=state.path.entry&&!mergeRecorded[i])
                            {mergeRecorded[i]=true;if(!stoppedBeforeMerge[i])entriesWithoutStop++;}
                            if(state.progress>=state.path.entry&&state.progress<=state.path.exit)
                            {
                                Need(Vector2.Distance(p,new Vector2(0,235))>13.9f,"circle cuts through its island.");
                                Vector2 forward=new Vector2(Mathf.Sin(c.roundYaw*Mathf.Deg2Rad),Mathf.Cos(c.roundYaw*Mathf.Deg2Rad));Vector2 radial=p-new Vector2(0,235);
                                Need(radial.x*forward.y-radial.y*forward.x>12,"traffic circulates clockwise.");
                                foreach(var corner in Corners(c))Need(Vector2.Distance(corner,new Vector2(0,235))>10.3f,"long body clips the island.");ringSamples++;if(c.distance>.00001f)moving++;
                            }
                            if(c.roundSignal)signalFrames++;
                        }
                        if(tick%5==0)for(int j=i+1;j<model.Cars.Length;j++)
                        {var other=model.Cars[j];if(!other.active||other.roundHidden)continue;Need(Separate(Corners(c),Corners(other)),"actual rotated bodies overlap: "+i+"/"+j+" tick="+tick);pairChecks++;}
                    }
                    peakMoving=Mathf.Max(peakMoving,moving);if(moving>=2)multipleMovingTicks++;
                }
                var final=model.Roundabout;entries=final.Entries;completed=final.Completed;returns=final.Returns;yields=final.Yields;recycles=final.Recycles;destinations=(int[])final.Destinations.Clone();departures=model.Stops.Departures;
                if(!(entries>=8&&completed>=8&&returns>=8&&recycles>=4&&yields>0&&ringSamples>100&&signalFrames>100))Need(false,"traffic stalls or skips entry/circulation/return/indicator stages: "+entries+"/"+completed+"/"+returns+"/"+recycles+Summary(model));
                Need(completed>=50&&entriesWithoutStop>0,"rolling entry throughput/continuous entry regression.");
                Need(peakMoving>=2&&multipleMovingTicks>=90,"only one ring vehicle moves at a time: peak="+peakMoving+" ticks="+multipleMovingTicks);
                foreach(int count in destinations)Need(count>0,"one requested random destination never occurs: "+string.Join(",",destinations)+" entries="+entries+" completed="+completed);
                Need(model.Stops.Departures>=2&&model.Cycles>=2,"roundabout starves bus service or signals.");
                // The actual fixed clock must retain route, heading, destination and progress at every frame rate.
                Vector2[] baseline=null;float[] headings=null;int[] choices=null;
                foreach(int fps in new[]{15,30,60,120})
                {
                    for(int i=0;i<drives.Length;i++)drives[i].ResetPosition(poses[i]);controller.ResetModel();
                    for(int n=0;n<fps*180;n++)controller.Advance(1d/fps);
                    model=controller.Model;
                    if(baseline==null){baseline=new Vector2[model.Cars.Length];headings=new float[baseline.Length];choices=new int[baseline.Length];for(int i=0;i<baseline.Length;i++){baseline[i]=RoundaboutTraffic.Position(model.Cars[i]);headings[i]=RoundaboutTraffic.Yaw(model.Cars[i]);choices[i]=model.Roundabout.States[i].destination;}}
                    else for(int i=0;i<baseline.Length;i++)Need(Vector2.Distance(baseline[i],RoundaboutTraffic.Position(model.Cars[i]))<.002f&&Mathf.Abs(Mathf.DeltaAngle(headings[i],RoundaboutTraffic.Yaw(model.Cars[i])))<.002f&&choices[i]==model.Roundabout.States[i].destination,"frame-rate dependent route.");
                    int before=model.ActiveTicks;var snapshot=RoundaboutTraffic.Position(model.Cars[0]);controller.SetPaused(true);controller.Advance(20);Need(model.ActiveTicks==before&&RoundaboutTraffic.Position(model.Cars[0])==snapshot,"hidden/pause advances the junction.");controller.SetPaused(false);
                }
                // Actual lamp material: roundabout exit uses the vehicle's right side at any heading.
                var probe=drives[0].Lighting;var stateProbe=new StreetModel.Car{active=true,roundabout=true,roundSignal=true};var block=new MaterialPropertyBlock();
                for(int tick=0;tick<54;tick++){stateProbe.roundSignalTicks=tick;probe.Apply(stateProbe,1,0);probe.Right.GetPropertyBlock(block);Need((block.GetColor("_EmissionColor").r>1)==(tick%18<9),"exit right lamp disagrees with clock.");probe.Left.GetPropertyBlock(block);Need(block.GetColor("_EmissionColor").r<1,"exit also lights left lamp.");}
                // Wet weather still completes the real junction; reducing traffic cannot freeze a reservation.
                for(int i=0;i<drives.Length;i++)drives[i].ResetPosition(poses[i]);climate.Preview(0,5);controller.ResetModel();for(int n=0;n<9000;n++)controller.Advance(1d/30);
                Need(controller.Model.Roundabout.Completed>=2,"storm deadlocks the junction.");
            }
            finally{cameraChoice.Preview(0);controller.SetPaused(false);for(int i=0;i<drives.Length;i++)drives[i].ResetPosition(poses[i]);climate.Preview(0,0);controller.ResetModel();controller.ApplyViews();}
            return new Report{result="PASS: actual production routes, CCW circulation/rotated-body island and pair clearance, four random exits, rolling uninterrupted entry and circulating-priority clearance, right indicators, offscreen return, bus service, storm and fixed-clock/pause; actual GPU and phone separate",cameraOptionsChecked=true,entriesWithoutStop=entriesWithoutStop,approachStoppedSeconds=approachStoppedTicks/30f,uninterruptedEntryCases=uninterruptedCases,uninterruptedEntryStopTicks=0,peakMovingRingCars=peakMoving,multipleMovingTicks=multipleMovingTicks,maxCompletionGapSeconds=maxCompletionGap/30f,terrainMeshes=5,terrainTriangles=triangles,simulatedSeconds=600,entries=entries,completed=completed,returns=returns,yields=yields,recycles=recycles,destinations=destinations,pairChecks=pairChecks,ringSamples=ringSamples,signalFrames=signalFrames,hiddenSpawns=hiddenResets,maximumVisibleStep=maximumStep,busDepartures=departures,frameRates=new[]{15,30,60,120}};
        }
        [Serializable] internal sealed class Report
        {public string result;public bool cameraOptionsChecked;public float approachStoppedSeconds;public int entriesWithoutStop,uninterruptedEntryCases,uninterruptedEntryStopTicks,peakMovingRingCars,multipleMovingTicks;public float maxCompletionGapSeconds;public int terrainMeshes,terrainTriangles,simulatedSeconds,entries,completed,returns,yields,recycles,pairChecks,ringSamples,signalFrames,hiddenSpawns,busDepartures;public int[] destinations,frameRates;public float maximumVisibleStep;}
    }
}
