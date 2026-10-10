using System;
using UnityEngine;
using UnityEngine.Rendering.Universal;

namespace PixelTraffic.UnityPrototype.Editor
{
    internal static class WeatherLifeChecks
    {
        internal static Report Validate(CityClimate climate)
        {
            var street=UnityEngine.Object.FindFirstObjectByType<StreetSimulation>();
            var drives=UnityEngine.Object.FindObjectsByType<PrototypeDrive>(FindObjectsSortMode.None);Array.Sort(drives,(a,b)=>string.CompareOrdinal(a.name,b.name));
            var planes=GeometryUtility.CalculateFrustumPlanes(Camera.main);
            bool Visible(Vector2 p)=>GeometryUtility.TestPlanesAABB(planes,new Bounds(new Vector3(p.x,1.2f,p.y),new Vector3(3,3.2f,6)));
            float minimumFeet=float.MaxValue,minimumGap=float.MaxValue;int departures=0,arrivals=0;
            foreach(int count in new[]{4,32,100})
            {
                var cars=new StreetModel.Car[drives.Length];
                for(int i=0;i<cars.Length;i++)
                {
                    var renderers=drives[i].GetComponentsInChildren<Renderer>(true);Bounds b=renderers[0].bounds;foreach(var r in renderers)b.Encapsulate(r.bounds);
                    cars[i]=new StreetModel.Car {lane=drives[i].Lane,z=drives[i].transform.position.z,cruise=drives[i].Speed,speed=drives[i].Speed,length=b.size.z};
                }
                var model=new StreetModel(count,cars){Visible=Visible};
                var oldActive=new bool[100];var oldPos=new Vector2[100];var oldActivity=new StreetModel.Activity[100];
                var oldCars=new bool[cars.Length];var oldZ=new double[cars.Length];
                for(int tick=0;tick<30*810;tick++)
                {
                    // Begin after the first pedestrian green, so the change includes road occupants.
                    if(tick==30*25)model.SetWeather(.3f,.45f,.6f,1.18f);
                    if(tick==30*625)
                    {
                        int active=0,activeCars=0;foreach(var p in model.People)if(p.active)active++;foreach(var c in cars)if(c.active)activeCars++;
                        Need(active==Mathf.Max(4,Mathf.RoundToInt(count*.3f)),"Storm pedestrians did not drain through offscreen exits: "+count+"/"+active);
                        Need(activeCars==12,"Storm traffic did not drain to three per lane.");
                        model.SetWeather(1,1,1,1);
                    }
                    for(int i=0;i<100;i++){oldActive[i]=model.People[i].active;oldPos[i]=model.People[i].position;oldActivity[i]=model.People[i].activity;}
                    for(int i=0;i<cars.Length;i++){oldCars[i]=cars[i].active;oldZ[i]=cars[i].z;}
                    model.Tick();
                    if(tick>30*28&&tick<30*625)foreach(var car in cars)if(car.active)Need(car.speed<=car.cruise*.6f+.001f,"Storm does not reduce cruising speed.");
                    Need(!(model.CanEnter||model.Signal==StreetModel.Phase.PedestrianClearance)||!model.RoadOccupied(),"Weather breaks pedestrian clearance.");
                    for(int i=0;i<100;i++)
                    {
                        var p=model.People[i];
                        if(oldActive[i]&&!p.active)Need(!Visible(oldPos[i])&&oldActivity[i]!=StreetModel.Activity.Cross,"A visible or crossing walker disappears.");
                        if(!oldActive[i]&&p.active)Need(!Visible(p.position),"Walker respawns in the viewport.");
                        if(!p.active)continue;
                        if(oldActive[i])Need(Vector2.Distance(oldPos[i],p.position)<=p.speed*model.WalkingPace*StreetModel.Dt+.0001f,"Weather teleports a visible walker.");
                        if(p.activity!=StreetModel.Activity.Cross)Need(SidewalkRoutes.Allowed(p.position),"Weather walker enters a tree bed or road.");
                        if(tick%10==0)for(int j=i+1;j<100;j++)if(model.People[j].active)
                        {float d=Vector2.Distance(p.position,model.People[j].position);minimumFeet=Mathf.Min(minimumFeet,d);Need(d>=.399f,"Weather pedestrians overlap.");}
                    }
                    for(int i=0;i<cars.Length;i++)
                    {
                        if(oldCars[i]&&!cars[i].active)Need(!Visible(new Vector2((cars[i].lane-1.5f)*3.2f,(float)oldZ[i])),"Car disappears inside viewport.");
                        if(!oldCars[i]&&cars[i].active)Need(!Visible(new Vector2((cars[i].lane-1.5f)*3.2f,(float)cars[i].z)),"Car reappears inside viewport.");
                        if(tick%10!=0||!cars[i].active)continue;
                        for(int j=i+1;j<cars.Length;j++)if(cars[j].active&&cars[j].lane==cars[i].lane)
                        {
                            float d=(float)Math.Abs(cars[i].z-cars[j].z);d=Mathf.Min(d,310-d);float gap=d-(cars[i].length+cars[j].length)/2;
                            minimumGap=Mathf.Min(minimumGap,gap);Need(gap>=1.79f,"Weather traffic overlaps at queue or respawn.");
                        }
                    }
                }
                int recovered=0,recoveredCars=0;foreach(var p in model.People)if(p.active)recovered++;foreach(var c in cars)if(c.active)recoveredCars++;
                Need(recovered==count&&recoveredCars==24,"Clear-weather population fails to return.");Need(model.Cycles>4,"Weather locks the signals.");
                departures+=model.WeatherDepartures;arrivals+=model.WeatherArrivals;
            }
            // Actual rigs, renderer state and pool, including the worst-case 100 open umbrellas.
            climate.Preview(0,2);street.ResetModel();street.Advance(.1);street.ApplyViews();
            Need(street.Spray.Capacity==96&&street.Spray.EmittingVehicles>0,"Moving rain vehicles produce no bounded spray.");
            foreach(var car in street.Model.Cars)car.speed=0;
            street.Spray.Advance(.1f,street.Model,1);Need(street.Spray.EmittingVehicles==0&&!GameObject.Find("Tyre Spray Pool").GetComponent<Renderer>().enabled,"Stopped cars still spray.");
            street.ResetModel();street.Model.SetWeather(1,1,1,1);street.Model.SetPopulation(100);street.ApplyViews();int umbrellas=0;
            foreach(var walker in street.Walkers){Need(walker.umbrella!=null&&walker.umbrella.gameObject.activeInHierarchy,"Rain walker lacks umbrella.");umbrellas++;}
            var pipeline=(UniversalRenderPipelineAsset)UnityEngine.Rendering.GraphicsSettings.defaultRenderPipeline;
            CityEnvironment.Report budget=CityEnvironment.Validate(Camera.main,pipeline);
            climate.Preview(0,0);street.Advance(.1);street.ApplyViews();
            foreach(var walker in street.Walkers)Need(!walker.umbrella.gameObject.activeSelf,"Clear weather keeps umbrella open.");
            Need(street.Spray.EmittingVehicles==0,"Dry road produces spray.");
            street.ResetModel();street.ApplyViews();
            return new Report {result="PASS: 4/32/100 weather drain/recovery, offscreen-only changes, collision/clearance, actual umbrella and spray renderer, 100-umbrella budget",simulatedSeconds=810,populations=new[]{4,32,100},departures=departures,arrivals=arrivals,minimumFootDistance=minimumFeet,minimumBumperGap=minimumGap,umbrellaCapacity=umbrellas,sprayCapacity=96,maxUmbrellaBudget=budget};
        }
        private static void Need(bool value,string message){if(!value)throw new InvalidOperationException(message);}
        [Serializable] internal sealed class Report
        {
            public string result;public int simulatedSeconds,departures,arrivals,umbrellaCapacity,sprayCapacity;public int[] populations;
            public float minimumFootDistance,minimumBumperGap;public CityEnvironment.Report maxUmbrellaBudget;
        }
    }
}
