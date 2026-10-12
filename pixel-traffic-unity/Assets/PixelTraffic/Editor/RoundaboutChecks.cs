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
        // A named density fixture uses the saved fleet's dimensions and unchanged
        // production routes. It is distinct from the naturally arriving fleet.
        internal static int[] DenseIndices(StreetModel.Car[] cars)
        {
            var buses=new List<int>();var trucks=new List<int>();var passenger=new List<int>();
            for(int i=0;i<cars.Length;i++){if(cars[i].bus)buses.Add(i);else if(cars[i].length>7)trucks.Add(i);else passenger.Add(i);}
            Need(buses.Count>=2&&trucks.Count>=1&&passenger.Count>=3,"density fixture lacks saved vehicle types.");
            return new[]{buses[0],passenger[0],trucks[0],passenger[1],buses[1],passenger[2]};
        }
        internal static void ConfigureDense(StreetModel model,int[] indices)
        {
            foreach(var car in model.Cars){car.active=false;car.rank=100;}
            for(int n=0;n<indices.Length;n++)
            {
                int i=indices[n];var c=model.Cars[i];var state=model.Roundabout.States[i];
                c.active=true;c.rank=0;c.lane=3;c.speed=4.8f;c.cruise=6;c.roundabout=true;c.roundHidden=false;c.roundSignal=false;c.roundSignalTicks=0;
                state.origin=0;state.destination=0;state.lane=0;state.path=model.Roundabout.Route(0,0,0);
                state.progress=state.path.entry+11*n;state.stage=RoundaboutTraffic.Stage.Circulating;state.admitted=true;state.lastTravel=.16f;
                c.roundPosition=state.path.Pose(state.progress,out c.roundYaw);c.z=c.roundPosition.y;
            }
        }
        static int[] DenseQueue(StreetModel.Car[] templates)
        {
            var selected=DenseIndices(templates);var cars=new StreetModel.Car[7];
            for(int i=0;i<6;i++){var c=templates[selected[i]];cars[i]=new StreetModel.Car{lane=3,z=180,length=c.length,width=c.width,height=c.height,bus=c.bus,cruise=6,speed=4.8f};}
            cars[6]=new StreetModel.Car{lane=0,z=180,length=5,width=2.2f,active=false};
            var model=new StreetModel(4,cars);model.AutomaticLaneChanges=false;model.EnableRoundabout();ConfigureDense(model,new[]{0,1,2,3,4,5});
            int sixTicks=0,queued=0;
            for(int tick=0;tick<1590;tick++)
            {
                if(tick==90){cars[6].active=true;cars[6].rank=0;cars[6].lane=0;cars[6].roundabout=false;cars[6].roundHidden=false;cars[6].z=218;cars[6].speed=0;cars[6].cruise=0;model.Roundabout.States[6].stage=RoundaboutTraffic.Stage.Road;}
                if(tick==690){cars[6].active=false;cars[6].rank=100;}
                model.Tick();int moving=0,stopped=0;
                for(int i=0;i<6;i++)
                {
                    var car=cars[i];var state=model.Roundabout.States[i];
                    if(car.roundabout&&state.progress>=state.path.entry&&state.progress<state.path.exit){if(car.distance>.00001f)moving++;else stopped++;}
                    for(int j=i+1;j<cars.Length;j++)if(car.active&&cars[j].active&&!car.roundHidden&&!cars[j].roundHidden)Need(Separate(Corners(car),Corners(cars[j])),"density fixture rotated bodies overlap: "+i+"/"+j+" tick="+tick);
                }
                if(tick<90&&moving==6)sixTicks++;if(tick>=90&&tick<690)queued=Mathf.Max(queued,stopped);
            }
            Need(sixTicks>=60&&queued>=2&&model.Roundabout.Completed>=6,"six-car circulation/internal queue does not recover: "+sixTicks+"/"+queued+"/"+model.Roundabout.Completed);
            return new[]{sixTicks,queued,model.Roundabout.Completed};
        }
        static int StaleLaneRequests(StreetModel.Car[] templates)
        {
            int cases=0;
            foreach(var source in templates)foreach(var pending in new[]{LaneChanges.Stage.Waiting,LaneChanges.Stage.Signaling})
            {
                var c=new StreetModel.Car{lane=3,z=195,cruise=6,speed=4.8f,length=source.length,width=source.width,height=source.height,bus=source.bus,targetLane=2,maneuver=pending,rearMask=1,requestTick=1};
                var model=new StreetModel(4,new[]{c});model.EnableRoundabout();model.Tick();
                Need(c.roundabout&&c.maneuver==LaneChanges.Stage.Idle&&c.rearMask==0,"stale lane-change request strands a car at an empty gate.");cases++;
            }
            var merging=new StreetModel.Car{lane=3,z=175,cruise=6,speed=4.8f,length=5,width=2.2f,targetLane=2,maneuver=LaneChanges.Stage.Merging,mergeTicks=30};
            var probe=new StreetModel(4,new[]{merging});probe.EnableRoundabout();probe.Tick();
            Need(merging.maneuver==LaneChanges.Stage.Merging&&!merging.roundabout,"an already swept lane merge is canceled/teleported.");
            return cases;
        }
        // Independent polar-angle accumulation: the first assigned exit must be
        // reached in less than one full turn on all ports and lane variants.
        static float[] FirstExits(StreetModel model)
        {
            int cases=0;float maximum=0;
            for(int origin=0;origin<4;origin++)for(int destination=0;destination<4;destination++)for(int lane=0;lane<2;lane++)
            {
                var path=model.Roundabout.Route(origin,destination,lane);float arc=0;
                for(int i=1;i<path.points.Length;i++)if(path.distance[i]>path.entry+.001f&&path.distance[i]<=path.exit+.001f)
                {
                    var a=path.points[i-1]-new Vector2(0,235);var b=path.points[i]-new Vector2(0,235);
                    float delta=Mathf.DeltaAngle(Mathf.Atan2(a.y,a.x)*Mathf.Rad2Deg,Mathf.Atan2(b.y,b.x)*Mathf.Rad2Deg);
                    Need(delta>0&&delta<2.01f,"first-exit curve reverses or skips its polar samples.");arc+=delta;
                }
                float expected=Mathf.Repeat(-125+destination*90-(-45+origin*90),360);
                Need(arc<360&&Mathf.Abs(arc-expected)<.01f,"route adds a lap or misses its first assigned exit: "+origin+"/"+destination);
                maximum=Mathf.Max(maximum,arc);cases++;
            }
            return new[]{(float)cases,maximum};
        }

        static int Gardens()
        {
            Transform gardens=GameObject.Find("Roundabout Gardens").transform;
            Need(gardens.GetComponentsInChildren<Collider>().Length==0&&gardens.GetComponentsInChildren<Light>().Length==0,"planting adds traffic obstacles or realtime lights.");
            int islandBeds=0,outerBeds=0,shrubs=0,outerShrubs=0,flowers=0,trees=0,triangles=0;
            var names=new HashSet<string>();var allowed=new HashSet<Material>();
            foreach(string name in new[]{"Soil","Bark","RoadWhite","Leaves0","Leaves1","Leaves2"})allowed.Add(AssetDatabase.LoadAssetAtPath<Material>(StarterScene.Generated+"/"+name+".mat"));
            foreach(Transform marker in gardens)
            {
                Vector3 p=marker.localPosition,s=marker.localScale;
                if(marker.name=="Island Planting Bed"){islandBeds++;Need(new Vector2(p.x,p.z-235).magnitude+Mathf.Max(s.x,s.z)<9.5f&&p.y>.235f&&p.y<.27f,"island bed crosses its curb or floats.");}
                if(marker.name=="Island Shrub"){shrubs++;Need(p.y+s.y<1.1f&&new Vector2(p.x,p.z-235).magnitude+Mathf.Max(s.x,s.z)<9.5f,"central planting obscures traffic.");}
                if(marker.name=="Island Flower Cluster")flowers++;
                if(marker.name=="Outer Planting Bed"){outerBeds++;Need(Mathf.Abs(p.y+.17f)<.001f&&Mathf.Abs(p.x)-s.x>21.5f&&Mathf.Abs(p.z-235)-s.z>21.5f,"outer bed enters the circle, flared approach or sidewalk.");}
                if(marker.name=="Outer Shrub")outerShrubs++;
                if(marker.name=="Garden Tree")trees++;
                var filter=marker.GetComponent<MeshFilter>();if(filter==null)continue;
                Need(names.Add(marker.name)&&allowed.Contains(marker.GetComponent<MeshRenderer>().sharedMaterial),"garden duplicates a mesh or adds a new material.");
                Mesh mesh=filter.sharedMesh;var points=mesh.vertices;var normals=mesh.normals;var uv=mesh.uv;var indices=mesh.triangles;triangles+=indices.Length/3;
                Need(points.Length==normals.Length&&points.Length==uv.Length,"garden normal/UV data missing.");
                for(int i=0;i<points.Length;i++)
                {
                    var p0=points[i];Need(!float.IsNaN(p0.x)&&!float.IsNaN(p0.y)&&!float.IsNaN(p0.z)&&Mathf.Abs(normals[i].magnitude-1)<.001f,"garden geometry invalid.");
                    float r=new Vector2(p0.x,p0.z-235).magnitude;
                    bool central=r<9.5f&&p0.y>=.235f&&p0.y<1.1f;
                    bool outside=Mathf.Abs(p0.x)>21.5f&&Mathf.Abs(p0.z-235)>21.5f&&p0.y>=-.175f&&p0.y<4.7f;
                    Need(central||outside,"actual planting enters a vehicle lane, walking band, or central sightline.");
                }
                for(int i=0;i<indices.Length;i+=3)
                {int a=indices[i],b=indices[i+1],c=indices[i+2];var normal=Vector3.Cross(points[b]-points[a],points[c]-points[a]);Need(normal.sqrMagnitude>1e-10f&&Vector3.Dot(normal,normals[a]+normals[b]+normals[c])>0,"garden face degenerate or reversed.");}
            }
            Need(islandBeds==3&&outerBeds==4&&shrubs==9&&outerShrubs==8&&flowers==24&&trees==4&&names.Count==6&&triangles==436,"landscape groups or shared mesh budget changed.");
            return triangles;
        }
        internal static Report Validate()
        {
            var controller=UnityEngine.Object.FindFirstObjectByType<StreetSimulation>();controller.ResetModel();Need(controller.Model.Roundabout!=null,"saved runtime controller omits the junction.");
            int staleCases=StaleLaneRequests(controller.Model.Cars);var firstExits=FirstExits(controller.Model);int uninterruptedCases=UninterruptedEntries(controller.Model.Cars);int[] density=DenseQueue(controller.Model.Cars);
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
            int gardenTriangles=Gardens();
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
            int entriesWithoutStop=0,approachStoppedTicks=0,lowerQueueTicks=0,peakLowerQueue=0,maxLowerWaitTicks=0,progressRewinds=0;
            int entries=0,completed=0,returns=0,yields=0,recycles=0,departures=0,peakMoving=0,multipleMovingTicks=0,lastCompletionTick=0,lastCompleted=0,maxCompletionGap=0;int[] destinations=null;
            try
            {
                cameraChoice.Preview(1);climate.Preview(0,0);controller.ResetModel();var model=controller.Model;
                var previous=new Vector2[model.Cars.Length];var oldHidden=new bool[model.Cars.Length];var oldRound=new bool[model.Cars.Length];var oldLane=new int[model.Cars.Length];
                var previousProgress=new float[model.Cars.Length];var previousVisits=new int[model.Cars.Length];var lowerWait=new int[model.Cars.Length];var visit=new int[model.Cars.Length];var stoppedBeforeMerge=new bool[visit.Length];var mergeRecorded=new bool[visit.Length];
                for(int i=0;i<visit.Length;i++){var state=model.Roundabout.States[i];visit[i]=state.visits;mergeRecorded[i]=model.Cars[i].roundabout&&state.path!=null&&state.progress>=state.path.entry;}
                for(int tick=0;tick<18000;tick++)
                {
                    for(int i=0;i<previous.Length;i++){previous[i]=RoundaboutTraffic.Position(model.Cars[i]);oldHidden[i]=model.Cars[i].roundHidden;oldRound[i]=model.Cars[i].roundabout;oldLane[i]=model.Cars[i].lane;previousProgress[i]=model.Roundabout.States[i].progress;previousVisits[i]=model.Roundabout.States[i].visits;}
                    controller.Advance(1d/30);model=controller.Model;var round=model.Roundabout;int moving=0;
                    int lowerQueue=0;
                    if(round.Completed!=lastCompleted){lastCompleted=round.Completed;lastCompletionTick=tick;}
                    maxCompletionGap=Mathf.Max(maxCompletionGap,tick-lastCompletionTick);
                    if(tick-lastCompletionTick>=2700)Need(false,"junction has no completed route for90seconds."+Summary(model));
                    Need(!(model.CanEnter||model.Signal==StreetModel.Phase.PedestrianClearance)||!model.RoadOccupied(),"junction breaks crossing clearance.");
                    for(int i=0;i<model.Cars.Length;i++)
                    {
                        var c=model.Cars[i];if(!c.active||c.roundHidden)continue;
                        var route=round.States[i];
                        bool lowerApproach=(!c.roundabout&&c.Direction>0&&c.z>=160&&c.z<=195.6)||(c.roundabout&&route.origin==0&&route.path!=null&&route.progress<route.path.entry);
                        if(lowerApproach&&c.distance<=.00001f){lowerQueue++;lowerQueueTicks++;lowerWait[i]++;maxLowerWaitTicks=Mathf.Max(maxLowerWaitTicks,lowerWait[i]);}else lowerWait[i]=0;
                        if(oldRound[i]&&c.roundabout&&previousVisits[i]==route.visits&&route.progress<previousProgress[i]-.001f)progressRewinds++;
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
                    peakLowerQueue=Mathf.Max(peakLowerQueue,lowerQueue);peakMoving=Mathf.Max(peakMoving,moving);if(moving>=2)multipleMovingTicks++;
                }
                Need(progressRewinds==0,"runtime repeats a lap by rewinding route progress.");var final=model.Roundabout;entries=final.Entries;completed=final.Completed;returns=final.Returns;yields=final.Yields;recycles=final.Recycles;destinations=(int[])final.Destinations.Clone();departures=model.Stops.Departures;
                if(!(entries>=8&&completed>=8&&returns>=8&&recycles>=4&&yields>0&&ringSamples>100&&signalFrames>100))Need(false,"traffic stalls or skips entry/circulation/return/indicator stages: "+entries+"/"+completed+"/"+returns+"/"+recycles+Summary(model));
                Need(completed>=50&&entriesWithoutStop>0,"rolling entry throughput/continuous entry regression.");
                Need(peakMoving>=3&&multipleMovingTicks>=90,"only one ring vehicle moves at a time: peak="+peakMoving+" ticks="+multipleMovingTicks);
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
            return new Report{gardenTriangles=gardenTriangles,gardenMeshes=6,islandShrubs=9,islandFlowerClusters=24,outerShrubs=8,gardenTrees=4,result="PASS: actual production routes, CCW circulation/rotated-body island and pair clearance, four random exits, rolling uninterrupted entry and local time-aligned merges, first assigned exits without occupancy retention or extra laps, measured lower entrance queue and stale rear-pass cancellation at gate, six-car density/internal-queue recovery and committed-merge clearance, right indicators, offscreen return, bus service, storm and fixed-clock/pause; actual GPU and phone separate",cameraOptionsChecked=true,staleLaneRequestCases=staleCases,firstExitRoutes=(int)firstExits[0],maximumArcDegrees=firstExits[1],progressRewinds=progressRewinds,extraLaps=0,lowerQueueStopSeconds=lowerQueueTicks/30f,peakLowerQueue=peakLowerQueue,maximumLowerWaitSeconds=maxLowerWaitTicks/30f,denseVehicles=6,denseMovingTicks=density[0],denseInsideQueueCount=density[1],denseRecovered=density[2],entriesWithoutStop=entriesWithoutStop,approachStoppedSeconds=approachStoppedTicks/30f,uninterruptedEntryCases=uninterruptedCases,uninterruptedEntryStopTicks=0,peakMovingRingCars=peakMoving,multipleMovingTicks=multipleMovingTicks,maxCompletionGapSeconds=maxCompletionGap/30f,terrainMeshes=5,terrainTriangles=triangles,simulatedSeconds=600,entries=entries,completed=completed,returns=returns,yields=yields,recycles=recycles,destinations=destinations,pairChecks=pairChecks,ringSamples=ringSamples,signalFrames=signalFrames,hiddenSpawns=hiddenResets,maximumVisibleStep=maximumStep,busDepartures=departures,frameRates=new[]{15,30,60,120}};
        }
        [Serializable] internal sealed class Report
        {public int gardenTriangles,gardenMeshes,islandShrubs,islandFlowerClusters,outerShrubs,gardenTrees;public string result;public bool cameraOptionsChecked;public float approachStoppedSeconds;public int staleLaneRequestCases,firstExitRoutes,progressRewinds,extraLaps,peakLowerQueue;public float maximumArcDegrees,lowerQueueStopSeconds,maximumLowerWaitSeconds;public int denseVehicles,denseMovingTicks,denseInsideQueueCount,denseRecovered;public int entriesWithoutStop,uninterruptedEntryCases,uninterruptedEntryStopTicks,peakMovingRingCars,multipleMovingTicks;public float maxCompletionGapSeconds;public int terrainMeshes,terrainTriangles,simulatedSeconds,entries,completed,returns,yields,recycles,pairChecks,ringSamples,signalFrames,hiddenSpawns,busDepartures;public int[] destinations,frameRates;public float maximumVisibleStep;}
    }
}
