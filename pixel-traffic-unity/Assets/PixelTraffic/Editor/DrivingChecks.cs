using System;
using UnityEngine;

namespace PixelTraffic.UnityPrototype.Editor
{
    internal static class DrivingChecks
    {
        static StreetModel.Car Car(int lane,double z,float speed)=>new StreetModel.Car{lane=lane,z=z,cruise=speed,speed=speed,length=4.8f};
        static StreetModel Scenario(params StreetModel.Car[] cars)=>new StreetModel(4,cars){AutomaticLaneChanges=false};
        internal static Report Validate()
        {
            // Two real rear vehicles must both pass; lateral movement starts only after three pulses.
            var m=Scenario(Car(0,140,6),Car(1,160,10),Car(1,185,10));Need(m.Maneuvers.Request(m,0),"Rear-pass request rejected.");
            Need(m.Cars[0].rearMask==6,"Rear tracker misses a rear vehicle.");int pulses=0;bool wasOn=false;float gap=float.MaxValue;int mergeAt=-1;
            for(int n=0;n<1050;n++)
            {
                m.Tick();bool on=LaneChanges.IndicatorOn(m.Cars[0]);if(on&&!wasOn)pulses++;wasOn=on;
                if(m.Cars[0].maneuver==LaneChanges.Stage.Waiting)Need(Mathf.Abs(m.Cars[0].X+4.8f)<.0001f&&!on,"Waiting car signals or moves sideways before a rear pass.");
                if(m.Cars[0].maneuver==LaneChanges.Stage.Merging&&mergeAt<0)
                {
                    mergeAt=n;Need(pulses==3&&m.Cars[0].blinks==3&&m.Cars[0].rearMask==0,"Merge does not follow exactly three pulses.");
                    Need(-20+m.Cars[1].totalDistance-m.Cars[0].totalDistance>7&&-45+m.Cars[2].totalDistance-m.Cars[0].totalDistance>7,"Not all original rear cars have passed.");
                }
                Gap(m,ref gap);
                if(m.Maneuvers.Completed>0)break;
            }
            Need(m.Maneuvers.Completed==1&&m.Cars[0].lane==1&&mergeAt>=54,"Rear-pass maneuver fails to finish.");
            var blocked=Scenario(Car(0,140,6),Car(1,130,4));Need(blocked.Maneuvers.Request(blocked,0),"Front-gap request rejected.");
            for(int n=0;n<60;n++){blocked.Tick();Need(blocked.Cars[0].maneuver==LaneChanges.Stage.Waiting,"Insufficient front gap starts a maneuver.");}
            var late=Scenario(Car(0,140,6),Car(1,90,6));Need(late.Maneuvers.Request(late,0),"Late-gap request rejected.");
            for(int n=0;n<10;n++)late.Tick();Need(late.Cars[0].maneuver==LaneChanges.Stage.Signaling,"Safe request does not signal.");
            late.Cars[1].z=late.Cars[0].z+16;late.Cars[1].speed=20;
            for(int n=0;n<60;n++){late.Tick();Need(late.Cars[0].maneuver!=LaneChanges.Stage.Merging,"New approaching rear vehicle is cut off after signaling.");Gap(late,ref gap);}
            Need(late.Maneuvers.Canceled>0,"Unsafe final gap does not cancel.");
            var cross=Scenario(Car(0,20,6));Need(!cross.Maneuvers.Request(cross,0),"Car changes lane near crossing.");
            var storm=Scenario(Car(2,140,6));storm.SetWeather(.3f,.45f,.6f,1.18f);Need(!storm.Maneuvers.Request(storm,0),"Storm permits a new lane change.");

            // Ten minutes of production traffic, including reservations and unwrapped rear-pass accounting.
            var definitions=new StreetModel.Car[24];float[] speeds={6,7,6.5f,5.8f};float[] offsets={17,34,9,26};
            for(int lane=0;lane<4;lane++)for(int i=0;i<6;i++)definitions[lane*6+i]=Car(lane,-55+offsets[lane]+i*310d/6,speeds[lane]);
            var longRun=new StreetModel(32,definitions);int verifiedStarts=0;
            var oldStages=new LaneChanges.Stage[24];var previousX=new float[24];
            for(int n=0;n<18000;n++)
            {
                for(int i=0;i<24;i++){oldStages[i]=definitions[i].maneuver;previousX[i]=definitions[i].X;}
                longRun.Tick();Gap(longRun,ref gap);
                for(int i=0;i<24;i++)
                {
                    var car=definitions[i];Need(car.X>=-4.801f&&car.X<=4.801f,"Lane change crosses a curb.");
                    Need(Mathf.Abs(car.X-previousX[i])<.055f,"Lane change teleports sideways.");
                    Need(car.targetLane<0||(car.lane<2)==(car.targetLane<2),"Car crosses the centre line.");
                    if(car.maneuver==LaneChanges.Stage.Merging&&oldStages[i]!=LaneChanges.Stage.Merging){Need(car.blinks==3&&car.rearMask==0,"Production merge skips signal/rear-pass gate.");verifiedStarts++;}
                    Need(!(longRun.CanEnter||longRun.Signal==StreetModel.Phase.PedestrianClearance)||!longRun.RoadOccupied(),"Changing vehicle enters pedestrian green.");
                }
            }
            Need(longRun.Maneuvers.Completed>=4&&verifiedStarts>=longRun.Maneuvers.Completed&&longRun.Maneuvers.RearWaits>0,"Production traffic never changes lanes or waits for rear traffic.");
            var drives=UnityEngine.Object.FindObjectsByType<PrototypeDrive>(FindObjectsSortMode.None);var climate=UnityEngine.Object.FindFirstObjectByType<CityClimate>();
            var probe=drives[0].Lighting;Need(probe!=null,"Actual vehicle lighting component missing.");
            var state=Car(0,140,6);var block=new MaterialPropertyBlock();climate.Preview(0,0);probe.Apply(state,climate.LampGain,0);probe.Head.GetPropertyBlock(block);float dayHead=block.GetColor("_EmissionColor").r;
            climate.Preview(2,0);probe.Apply(state,climate.LampGain,0);probe.Head.GetPropertyBlock(block);Need(block.GetColor("_EmissionColor").r>dayHead+1,"Headlights do not respond to night.");
            probe.Tail.GetPropertyBlock(block);float runningTail=block.GetColor("_EmissionColor").r;state.braking=true;probe.Apply(state,0,0);probe.Tail.GetPropertyBlock(block);Need(block.GetColor("_EmissionColor").r>runningTail*3,"Actual braking material fails to brighten even in daylight.");
            state.maneuver=LaneChanges.Stage.Signaling;state.targetLane=1;state.braking=false;
            for(int tick=0;tick<54;tick++)
            {
                state.signalTicks=tick;probe.Apply(state,1,0);probe.Left.GetPropertyBlock(block);bool left=block.GetColor("_EmissionColor").r>1;probe.Right.GetPropertyBlock(block);bool right=block.GetColor("_EmissionColor").r>1;
                Need(left==(tick%18<9)&&!right,"Actual front/rear signal side or pulse timing differs for approaching traffic.");
            }
            state.lane=2;state.targetLane=3;state.signalTicks=0;probe.Apply(state,1,1);probe.Right.GetPropertyBlock(block);Need(block.GetColor("_EmissionColor").r>1,"Away-going signal side is reversed.");
            Need(probe.Beams.enabled&&probe.Beams.sharedMaterial.shader.name=="PixelTraffic/Atmosphere","Road beam missing or using an extra light.");
            int lights=0;foreach(var light in UnityEngine.Object.FindObjectsByType<Light>(FindObjectsSortMode.None))if(light.type!=LightType.Directional)lights++;Need(lights==4,"Fleet adds per-car realtime lights.");
            var meshes=new System.Collections.Generic.Dictionary<string,Mesh>();
            foreach(var drive in drives)
            {
                Need(drive.Lighting!=null&&drive.GetComponentsInChildren<Renderer>().Length==17,"Fleet light parts incomplete.");
                foreach(string part in new[]{"Left Turn Signals","Right Turn Signals","Headlight Road Beams"})
                {Mesh mesh=drive.transform.Find(part).GetComponent<MeshFilter>().sharedMesh;string key=drive.Model+part;if(meshes.TryGetValue(key,out var old))Need(old==mesh,"Vehicle lighting mesh is copied per vehicle.");else meshes.Add(key,mesh);}
            }
            Need(Camera.main.transform.position==new Vector3(-1.2f,20.5f,-27)&&Camera.main.fieldOfView==44,"Driving camera framing missing.");
            climate.Preview(0,0);UnityEngine.Object.FindFirstObjectByType<StreetSimulation>().ApplyViews();
            return new Report{result="PASS: rear passes, three pulses, final gap cancellation, 600-second shared-lane safety, actual light properties and camera; phone test pending",completedChanges=longRun.Maneuvers.Completed,rearWaits=longRun.Maneuvers.RearWaits,verifiedMergeStarts=verifiedStarts,simulatedSeconds=600,minimumBumperGap=gap,blinkCount=3,blinkSeconds=1.8f,mergeSeconds=3,renderersPerVehicle=17,fleetLightTriangles=288,realtimeStreetLights=lights};
        }
        static void Gap(StreetModel model,ref float minimum)
        {
            for(int i=0;i<model.Cars.Length;i++)for(int j=i+1;j<model.Cars.Length;j++)
            {
                var a=model.Cars[i];var b=model.Cars[j];if(!a.active||!b.active||!LaneChanges.SharesLane(a,b))continue;
                double distance=Math.Abs(a.z-b.z);distance=Math.Min(distance,310-distance);float gap=(float)distance-(a.length+b.length)*.5f;
                minimum=Mathf.Min(minimum,gap);Need(gap>=1.79f,"Reserved-lane cars overlap: "+i+"/"+j+" gap="+gap+" stages="+a.maneuver+"/"+b.maneuver);
            }
        }
        static void Need(bool value,string message){if(!value)throw new InvalidOperationException(message);}
        [Serializable]internal sealed class Report
        {public string result;public int completedChanges,rearWaits,verifiedMergeStarts,simulatedSeconds,blinkCount,renderersPerVehicle,fleetLightTriangles,realtimeStreetLights;public float minimumBumperGap,blinkSeconds,mergeSeconds;}
    }
}
