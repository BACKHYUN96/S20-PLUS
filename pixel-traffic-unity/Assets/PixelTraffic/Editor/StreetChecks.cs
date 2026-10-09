using System;
using System.Collections.Generic;
using UnityEditor;
using UnityEngine;
using UnityEngine.Rendering.Universal;

namespace PixelTraffic.UnityPrototype.Editor
{
    internal static class StreetChecks
    {
        internal static Report Validate()
        {
            var controller=UnityEngine.Object.FindFirstObjectByType<StreetSimulation>();Need(controller!=null,"Street controller missing.");
            var drives=UnityEngine.Object.FindObjectsByType<PrototypeDrive>(FindObjectsSortMode.None);Array.Sort(drives,(a,b)=>string.CompareOrdinal(a.name,b.name));
            var definitions=new StreetModel.Car[drives.Length];var poses=new Vector3[drives.Length];var wheels=new Quaternion[drives.Length][];
            for(int i=0;i<drives.Length;i++)
            {
                poses[i]=drives[i].transform.position;wheels[i]=new Quaternion[4];for(int n=0;n<4;n++)wheels[i][n]=drives[i].Wheels[n].localRotation;
                var renderers=drives[i].GetComponentsInChildren<Renderer>();Bounds bounds=renderers[0].bounds;foreach(var r in renderers)bounds.Encapsulate(r.bounds);
                definitions[i]=new StreetModel.Car {lane=drives[i].Lane,z=poses[i].z,cruise=drives[i].Speed,speed=drives[i].Speed,length=bounds.size.z};
            }
            StreetModel Create(int count)
            {
                var cars=new StreetModel.Car[definitions.Length];for(int i=0;i<cars.Length;i++){var c=definitions[i];cars[i]=new StreetModel.Car {lane=c.lane,z=c.z,cruise=c.cruise,speed=c.speed,length=c.length};}return new StreetModel(count,cars);
            }
            float minimumPeople=float.MaxValue,minimumCars=float.MaxValue;int completed=0,cycles=0;var populations=new List<int>();
            foreach(int count in new[]{4,32,100})
            {
                var model=Create(count);int ticks=count==4?6000:18000;
                int[] crossingCounts=new int[100];float[] firstDistances=new float[100];Vector2[] previous=new Vector2[100];StreetModel.Activity[] activities=new StreetModel.Activity[100];
                for(int step=0;step<ticks;step++)
                {
                    for(int i=0;i<count;i++){previous[i]=model.People[i].position;activities[i]=model.People[i].activity;}
                    model.Tick();
                    if(model.CanEnter||model.Signal==StreetModel.Phase.PedestrianClearance)Need(!model.RoadOccupied(),"Vehicle enters pedestrian phase: count "+count+" tick "+step+" phase "+model.Signal+" "+CarSummary(model));
                    for(int i=0;i<count;i++)
                    {
                        var p=model.People[i];Need(p.active,"Population unexpectedly removed.");
                        Need(Vector2.Distance(previous[i],p.position)<=p.speed*StreetModel.Dt+.0001f,"Walker teleports.");
                        if(p.activity==StreetModel.Activity.Cross)
                        {
                            Need(p.position.y>=8.2f&&p.position.y<=11.8f,"Walker leaves zebra stripes: count="+count+" tick="+step+" id="+i+" pos="+p.position);
                            if(activities[i]!=StreetModel.Activity.Cross)Need(model.CanEnter,"New pedestrian enters after green admission.");
                        }
                        else Need(SidewalkRoutes.Allowed(p.position),"Walker walks through curb/tree bed/lamp/building.");
                        if(step%10==0)for(int j=i+1;j<count;j++)
                        {
                            float d=Vector2.Distance(p.position,model.People[j].position);minimumPeople=Mathf.Min(minimumPeople,d);Need(d>=StreetModel.Separation-.001f,"People overlap feet.");
                        }
                    }
                    if(step%10==0)
                    {
                        int waiting=0;foreach(var p in model.People)if(p.active&&(p.activity==StreetModel.Activity.Wait||p.activity==StreetModel.Activity.Approach))waiting++;
                        Need(waiting<=12,"Crosswalk queue attracts the whole population.");
                        for(int i=0;i<model.Cars.Length;i++)for(int j=i+1;j<model.Cars.Length;j++)if(model.Cars[i].lane==model.Cars[j].lane)
                        {
                            float distance=(float)Math.Abs(model.Cars[i].z-model.Cars[j].z);distance=Mathf.Min(distance,StarterConfig.RouteEnd-StarterConfig.RouteStart-distance);
                            float gap=distance-(model.Cars[i].length+model.Cars[j].length)/2;minimumCars=Mathf.Min(minimumCars,gap);Need(gap>=1.79f,"Queue/wrap vehicles overlap.");
                        }
                    }
                    if(step==ticks/2)for(int i=0;i<count;i++)firstDistances[i]=model.People[i].walkDistance;
                }
                for(int i=0;i<count;i++)
                {
                    var p=model.People[i];Need(p.walkDistance-firstDistances[i]>1,"Pedestrian stuck: count="+count+" id="+i+" phase="+model.Signal+" time="+model.PhaseSeconds+" cycles="+model.Cycles+" activity="+p.activity+" pos="+p.position+" goal="+p.goal+" path="+p.pathCursor+"/"+p.pathCount+" wait="+p.waitingTime+" "+PeopleSummary(model,i)+" "+CarSummary(model));
                    Need(p.crossings>0,"A person never gets through a green crossing: "+count+"/"+i+" "+p.activity+" "+p.position+" goal="+p.goal+" wait="+p.waitingTime+" detour="+p.detour+"/"+p.detourSeconds+" next="+(p.pathCursor<p.pathCount?model.Routes.Node(p.path[p.pathCursor],p.side):p.goal)+" path "+p.pathCursor+"/"+p.pathCount+" "+PeopleSummary(model,i)+" "+CrossingSummary(model));
                    crossingCounts[i]=p.crossings;completed+=p.crossings;
                }
                Need(model.Cycles>=2,"Signal never returns to vehicle green.");cycles+=model.Cycles;populations.Add(count);
                // A reduction preserves road occupants until they reach a sidewalk.
                model.SetPopulation(4);for(int n=0;n<1800;n++)model.Tick();
                int active=0;foreach(var p in model.People)if(p.active)active++;
                Need(active==4,"Population reduction never drains safely.");
            }
            try
            {
                // Exercise the actual runtime controller clock/pause/wheels and compare 15/30/60/120Hz.
                double[] baseline=null;
                foreach(int fps in new[]{15,30,60,120})
                {
                    for(int i=0;i<drives.Length;i++)drives[i].ResetPosition(poses[i].z);
                    controller.ResetModel();
                    for(int n=0;n<fps*90;n++)controller.Advance(1d/fps);
                    var positions=new double[drives.Length];for(int i=0;i<positions.Length;i++)positions[i]=controller.Model.Cars[i].z;
                    if(baseline==null)baseline=positions;else for(int i=0;i<positions.Length;i++)Need(Math.Abs(positions[i]-baseline[i])<.001,"Signal traffic depends on frame rate.");
                    controller.SetPaused(true);double before=controller.Model.Cars[0].z;controller.Advance(30);Need(controller.Model.Cars[0].z==before,"Paused signal time advances.");controller.SetPaused(false);
                }
                // All 100 actual rigs/materials/geometry fit the same mobile budget, not just the default32.
                for(int i=0;i<drives.Length;i++)drives[i].ResetPosition(poses[i].z);
                controller.ResetModel();controller.Model.SetPopulation(100);controller.ApplyViews();
                var pipeline=(UniversalRenderPipelineAsset)UnityEngine.Rendering.GraphicsSettings.defaultRenderPipeline;
                var budget=CityEnvironment.Validate(Camera.main,pipeline);
                var treeBeds=new List<Bounds>();foreach(var r in UnityEngine.Object.FindObjectsByType<Renderer>(FindObjectsSortMode.None))if(r.name=="Tree Bed")treeBeds.Add(r.bounds);
                foreach(var view in controller.Walkers)
                {
                    Need(view.root.GetComponent<SkinnedMeshRenderer>().sharedMesh!=null&&view.limbs.Length==4,"Human rig incomplete.");
                    Vector3 foot=view.root.position;foreach(var bed in treeBeds)Need(foot.x<bed.min.x-.15f||foot.x>bed.max.x+.15f||foot.z<bed.min.z-.15f||foot.z>bed.max.z+.15f,"Actual tree bed differs from foot navigation.");
                }
                return new Report {result="PASS: actual runtime signal/following/crossing/avoidance,4/32/100 populations,600sec max,clock/pause and actual100-rig budget; device test pending",populations=populations.ToArray(),simulatedSeconds=new[]{200,600,600},minimumFootDistance=minimumPeople,minimumBumperGap=minimumCars,crossings=completed,signalCycles=cycles,maxPopulationBudget=budget};
            }
            finally
            {
                controller.SetPaused(false);for(int i=0;i<drives.Length;i++){drives[i].ResetPosition(poses[i].z);for(int n=0;n<4;n++)drives[i].Wheels[n].localRotation=wheels[i][n];}
                controller.ResetModel();controller.ApplyViews();
            }
        }
        private static string CrossingSummary(StreetModel model)
        {
            string result="uncrossed: ";for(int i=0;i<model.RequestedPeople;i++){var p=model.People[i];if(p.crossings==0)result+=i+":"+p.activity+"@"+p.position+"; ";}return result;
        }
        private static string PeopleSummary(StreetModel model,int index)
        {
            string result="nearby: ";foreach(var p in model.People)if(p.active&&Vector2.Distance(p.position,model.People[index].position)<3)result+="id="+Array.IndexOf(model.People,p)+" side="+p.side+" slot="+p.slot+" activity="+p.activity+" pos="+p.position+" goal="+p.goal+" crossings="+p.crossings+"; ";return result;
        }
        private static string CarSummary(StreetModel model)
        {
            string result="";foreach(var c in model.Cars)if(Math.Abs(c.z-StarterConfig.CrossingZ)<6)result+="lane="+c.lane+" z="+c.z.ToString("F7")+" speed="+c.speed+" committed="+c.committed+"; ";return result;
        }
        private static void Need(bool value,string message){if(!value)throw new InvalidOperationException(message);}
        [Serializable] internal sealed class Report
        {
            public string result;public int[] populations,simulatedSeconds;public float minimumFootDistance,minimumBumperGap;public int crossings,signalCycles;
            public CityEnvironment.Report maxPopulationBudget;
        }
    }
}
