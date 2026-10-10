using System;
using UnityEngine;

namespace PixelTraffic.UnityPrototype.Editor
{
    internal static class BusStopChecks
    {
        static void Need(bool value,string message){if(!value)throw new InvalidOperationException(message);}
        static StreetModel.Car Bus(int lane,double z,float speed)=>new StreetModel.Car{lane=lane,z=z,cruise=speed,speed=speed,length=10.73f,width=2.84f,height=3.4f,bus=true};
        internal static Report Validate()
        {
            var controller=UnityEngine.Object.FindFirstObjectByType<StreetSimulation>();
            Need(controller.Riders.Length==8&&controller.Model.Stops.Riders.Length==8,"Bus passenger pool missing.");
            var probe=UnityEngine.Object.FindFirstObjectByType<BusDoors>();Need(probe!=null,"Actual sliding bus doors missing.");
            var lighting=probe.GetComponent<VehicleLighting>();var block=new MaterialPropertyBlock();var state=Bus(0,BusStops.CenterZ(0),0);
            foreach(var phase in new[]{BusStops.Stage.Boarding,BusStops.Stage.Closing})for(int n=0;n<36;n++)
            {
                state.busStage=phase;state.hazardTicks=n;lighting.Apply(state,1,1);
                lighting.Left.GetPropertyBlock(block);bool left=block.GetColor("_EmissionColor").r>1;
                lighting.Right.GetPropertyBlock(block);bool right=block.GetColor("_EmissionColor").r>1;
                Need(left==right&&left==(n%18<9),"Stop hazard lamps do not flash together on both sides.");
            }
            state.busStage=BusStops.Stage.Ready;state.maneuver=LaneChanges.Stage.Signaling;state.targetLane=1;state.signalTicks=0;lighting.Apply(state,1,0);
            lighting.Left.GetPropertyBlock(block);bool departureLeft=block.GetColor("_EmissionColor").r>1;lighting.Right.GetPropertyBlock(block);
            Need(departureLeft&&block.GetColor("_EmissionColor").r==0,"Departure has hazards instead of a directional signal.");
            probe.Apply(1);Need(probe.Front.localPosition.z>4.9f&&probe.Rear.localPosition.z<3.2f,"Doorway never opens.");probe.Apply(0);
            Need(Mathf.Abs(probe.Front.localPosition.z-4.38f)<.001f&&Mathf.Abs(probe.Rear.localPosition.z-3.77f)<.001f,"Sliding doors do not close.");
            // Skin triangles cannot cover the actual opening; moving only a painted pane is insufficient.
            Mesh skin=probe.transform.Find("Sculpted Body").GetComponent<MeshFilter>().sharedMesh;var v=skin.vertices;var indices=skin.triangles;
            for(int n=0;n<indices.Length;n+=3)
            {
                var a=v[indices[n]];var b=v[indices[n+1]];var c=v[indices[n+2]];
                if(a.x>1.274f&&b.x>1.274f&&c.x>1.274f)
                {var middle=(a+b+c)/3;Need(!(middle.z>3.48f&&middle.z<4.67f&&middle.y>.59f&&middle.y<2.69f),"Opaque bus skin blocks the doorway.");}
            }
            var routes=new SidewalkRoutes();var path=new int[600];
            for(int stop=0;stop<2;stop++)
            {
                int side=BusStops.Side(stop);float stopZ=BusStops.DoorZ(stop);
                int count=routes.Find(new Vector2(side*9.995f,stopZ-3),new Vector2(side*9.995f,stopZ+3),side,path);
                Need(count>1,"Shelter blocks the sidewalk route.");
                for(int i=1;i<count;i++)Need(SidewalkRoutes.SegmentAllowed(routes.Node(path[i-1],side),routes.Node(path[i],side)),"Sidewalk graph uses obstacles from the opposite side.");
            }
            int boards=0,alights=0,departures=0,hazardFrames=0;float minimum=float.MaxValue;
            foreach(int population in new[]{4,32,100})
            {
                var cars=new[]{Bus(0,BusStops.CenterZ(0)+18,6),Bus(3,BusStops.CenterZ(1)-18,5.8f),
                    new StreetModel.Car{lane=1,z=BusStops.CenterZ(0)+30,cruise=9,speed=9,length=4.8f},
                    new StreetModel.Car{lane=2,z=BusStops.CenterZ(1)-30,cruise=9,speed=9,length=4.8f}};
                var m=new StreetModel(population,cars);m.AutomaticLaneChanges=false;
                var positions=new Vector2[8];var visible=new bool[8];var lastStage=new LaneChanges.Stage[4];var wasOn=new bool[4];var pulses=new int[4];
                for(int tick=0;tick<18000;tick++)
                {
                    for(int i=0;i<8;i++){positions[i]=m.Stops.Riders[i].position;visible[i]=m.Stops.Riders[i].Visible;}
                    for(int i=0;i<cars.Length;i++)lastStage[i]=cars[i].maneuver;
                    m.Tick();
                    for(int i=0;i<cars.Length;i++)
                    {
                        var car=cars[i];if(m.Stops.Holds(car))Need(car.speed==0&&car.distance==0,"Bus moves while doors or riders are in service.");
                        if(car.busDoor>0)Need(car.speed==0&&car.busStage!=BusStops.Stage.Leaving,"Bus departs with open doors.");
                        if(car.busStage==BusStops.Stage.Boarding||car.busStage==BusStops.Stage.Closing)hazardFrames++;
                        if(car.bus)
                        {
                            bool on=LaneChanges.IndicatorOn(car);if(on&&!wasOn[i])pulses[i]++;wasOn[i]=on;
                            if(car.maneuver==LaneChanges.Stage.Signaling&&lastStage[i]!=car.maneuver){pulses[i]=1;}
                            if(car.maneuver==LaneChanges.Stage.Merging&&lastStage[i]!=car.maneuver)
                                Need(car.busDoor==0&&car.blinks==3&&pulses[i]==3&&car.rearMask==0,"Departure skips closed doors/three signals/all rear passes.");
                            float a=Mathf.Abs(LaneChanges.Yaw(car))*Mathf.Deg2Rad,extent=(car.width*Mathf.Cos(a)+car.length*Mathf.Sin(a))/2;
                            Need(car.Direction<0?car.X-extent>-6.4f&&car.X+extent<0:car.X-extent>0&&car.X+extent<6.4f,"Bus stop sweep crosses curb or centre line.");
                        }
                    }
                    if(m.CanEnter||m.Signal==StreetModel.Phase.PedestrianClearance)Need(!m.RoadOccupied(),"Stopped/departing bus obstructs pedestrian green.");
                    for(int i=0;i<8;i++)
                    {
                        var rider=m.Stops.Riders[i];if(!rider.Visible)continue;
                        if(visible[i])Need(Vector2.Distance(rider.position,positions[i])<1.22f*StreetModel.Dt+.0001f,"Visible bus passenger teleports.");
                        if(Mathf.Abs(rider.position.x)<6.79f)
                        {var car=cars[rider.owner];Need(car.busDoor==1&&car.busStage==BusStops.Stage.Boarding&&car.speed==0,"Passenger crosses curb without a stopped, open bus.");}
                        else Need(SidewalkRoutes.Allowed(rider.position),"Bus passenger goes through shelter/tree/bench.");
                        foreach(var person in m.People)if(person.active)
                        {float gap=Vector2.Distance(person.position,rider.position);minimum=Mathf.Min(minimum,gap);Need(gap>=StreetModel.Separation-.001f,"Bus rider and pedestrian overlap.");}
                        for(int j=i+1;j<8;j++)if(m.Stops.Riders[j].Visible)Need(Vector2.Distance(rider.position,m.Stops.Riders[j].position)>=StreetModel.Separation-.001f,"Bus riders overlap.");
                    }
                    for(int i=0;i<cars.Length;i++)for(int j=i+1;j<cars.Length;j++)if(LaneChanges.SharesLane(cars[i],cars[j]))
                    {
                        double d=Math.Abs(cars[i].z-cars[j].z);d=Math.Min(d,310-d);
                        Need(d-(cars[i].SafetyLength+cars[j].SafetyLength)/2>=1.79,"Bus queues or departure reservations overlap.");
                    }
                }
                Need(m.Stops.Arrivals>=2&&m.Stops.Boardings>=4&&m.Stops.Alightings>=2&&m.Stops.Departures>=2,"Bus service does not complete: population="+population+" arrivals="+m.Stops.Arrivals+" boarded="+m.Stops.Boardings+" alighted="+m.Stops.Alightings+" departed="+m.Stops.Departures+" states="+cars[0].busStage+"/"+cars[1].busStage);
                boards+=m.Stops.Boardings;alights+=m.Stops.Alightings;departures+=m.Stops.Departures;
            }
            // Exercise the complete serialized fleet, including queues on both lanes during service.
            controller.ResetModel();var productionStops=controller.Model;float productionGap=float.MaxValue;
            for(int productionTick=0;productionTick<18000;productionTick++)
            {
                productionStops.Tick();
                Need(!(productionStops.CanEnter||productionStops.Signal==StreetModel.Phase.PedestrianClearance)||!productionStops.RoadOccupied(),"Full bus fleet blocks pedestrian green.");
                for(int ci=0;ci<productionStops.Cars.Length;ci++)
                {
                    var ca=productionStops.Cars[ci];if(!ca.active)continue;
                    if(ca.busDoor>0)Need(ca.speed==0,"Full-fleet bus moves with an open door.");
                    for(int cj=ci+1;cj<productionStops.Cars.Length;cj++)
                    {
                        var cb=productionStops.Cars[cj];if(!cb.active||!LaneChanges.SharesLane(ca,cb))continue;
                        double cd=Math.Abs(ca.z-cb.z);cd=Math.Min(cd,310-cd);float clearance=(float)cd-(ca.SafetyLength+cb.SafetyLength)/2;
                        productionGap=Mathf.Min(productionGap,clearance);Need(clearance>=1.79f,"Full-fleet stop reservation overlaps another vehicle.");
                    }
                }
            }
            Need(productionStops.Stops.Arrivals>=2&&productionStops.Stops.Boardings>=4&&productionStops.Stops.Departures>=2,"Full fleet deadlocks serviced buses: arrivals="+productionStops.Stops.Arrivals+" boarded="+productionStops.Stops.Boardings+" departed="+productionStops.Stops.Departures);
            // Storm traffic may be slower, but a serviced bus must eventually depart safely.
            var weatherBus=Bus(0,BusStops.CenterZ(0),6);weatherBus.speed=0;
            var weatherModel=new StreetModel(4,new[]{weatherBus}){AutomaticLaneChanges=false};weatherModel.SetWeather(.3f,.45f,.6f,1.18f);
            for(int weatherTick=0;weatherTick<5400;weatherTick++)weatherModel.Tick();
            Need(weatherModel.Stops.Boardings>=2&&weatherModel.Stops.Departures>=1,"Storm locks a serviced bus at the stop.");
            // The actual serialized controller clock must preserve stops, doors and riders across frame rates and pause.
            var drives=UnityEngine.Object.FindObjectsByType<PrototypeDrive>(FindObjectsSortMode.None);float[] z=new float[drives.Length];for(int i=0;i<z.Length;i++)z[i]=drives[i].transform.position.z;
            string baseline=null;
            try
            {
                foreach(int fps in new[]{15,30,60,120})
                {
                    for(int i=0;i<z.Length;i++)drives[i].ResetPosition(z[i]);controller.ResetModel();
                    for(int n=0;n<fps*120;n++)controller.Advance(1d/fps);
                    var model=controller.Model;string snapshot=model.Stops.Arrivals+"/"+model.Stops.Boardings+"/"+model.Stops.Alightings+"/"+model.Stops.Departures;
                    foreach(var car in model.Cars)snapshot+="/"+car.z.ToString("F3",System.Globalization.CultureInfo.InvariantCulture)+":"+car.busStage+":"+car.busTicks;
                    foreach(var rider in model.Stops.Riders)snapshot+="/"+rider.stage+":"+rider.position.ToString("F3");
                    if(baseline==null)baseline=snapshot;else Need(snapshot==baseline,"Bus service depends on rendering frame rate.");
                    int before=model.ActiveTicks;controller.SetPaused(true);controller.Advance(30);Need(model.ActiveTicks==before,"Paused bus service continues.");controller.SetPaused(false);
                }
            }
            finally{controller.SetPaused(false);for(int i=0;i<z.Length;i++)drives[i].ResetPosition(z[i]);controller.ResetModel();controller.ApplyViews();}
            var stopClimate=UnityEngine.Object.FindFirstObjectByType<CityClimate>();
            stopClimate.Preview(0,2);controller.ResetModel();controller.Model.SetPopulation(100);
            // Stress all eight passenger rigs/umbrellas as well as the 100 ordinary walkers.
            foreach(var stressRider in controller.Model.Stops.Riders)stressRider.stage=BusStops.RiderStage.Queue;
            controller.ApplyViews();
            foreach(var passengerView in controller.Riders)Need(passengerView.root.gameObject.activeSelf&&passengerView.umbrella.gameObject.activeSelf,"Waiting rain passenger lacks umbrella.");
            var stopPipeline=(UnityEngine.Rendering.Universal.UniversalRenderPipelineAsset)UnityEngine.Rendering.GraphicsSettings.defaultRenderPipeline;
            var peak=CityEnvironment.Validate(Camera.main,stopPipeline);
            stopClimate.Preview(0,0);controller.ResetModel();controller.ApplyViews();
            return new Report{result="PASS: two shelters, real open doorways, pooled walk-on/off riders, both-side hazard lamps, closed-door three-pulse safe departures, queues/curb/crosswalk/foot safety, 600sec 4/32/100 people, fixed clock and pause; phone test pending",stops=2,riders=8,productionDepartures=productionStops.Stops.Departures,productionArrivals=productionStops.Stops.Arrivals,productionMinimumGap=productionGap,peakRiderBudget=peak,stormDepartures=weatherModel.Stops.Departures,boardings=boards,alightings=alights,departures=departures,hazardFrames=hazardFrames,minimumFootDistance=minimum,dwellSeconds=8,doorSeconds=.8f,departureBlinkCount=3,departureBlinkSeconds=1.8f,departureMergeSeconds=6,frameRates=new[]{15,30,60,120}};
        }
        [Serializable]internal sealed class Report
        {public string result;public CityEnvironment.Report peakRiderBudget;public int stormDepartures,productionDepartures,productionArrivals;public float productionMinimumGap;public int stops,riders,boardings,alightings,departures,hazardFrames,departureBlinkCount;public float minimumFootDistance,dwellSeconds,doorSeconds,departureBlinkSeconds,departureMergeSeconds;public int[] frameRates;}
    }
}
