using System;
using UnityEngine;

namespace PixelTraffic.UnityPrototype
{
    // All doors, riders and departure decisions share the street's fixed 30 Hz clock.
    public sealed class BusStops
    {
        public enum Stage { Cruising, Approach, Boarding, Closing, Ready, Leaving }
        public enum RiderStage { Queue, Boarding, Onboard, Alighting, Returning }
        public sealed class Rider
        {
            public int stop,slot,owner=-1;public RiderStage stage;public Vector2 position,velocity;
            public float distance;public bool Visible=>stage!=RiderStage.Onboard;
        }
        public const int DwellTicks=240,DoorTicks=24;
        public const float DoorLocalZ=4.075f;
        public readonly Rider[] Riders;
        readonly StreetModel model;
        readonly int[] owners={-1,-1};
        readonly bool[] alighted=new bool[2];
        readonly int[] boarded=new int[2];
        public int Arrivals {get;private set;}public int Boardings {get;private set;}public int Alightings {get;private set;}public int Departures {get;private set;}
        public static int Side(int stop)=>stop==0?-1:1;
        public static float DoorZ(int stop)=>stop==0?-4.5f:34.5f;
        public static bool SidewalkOpen(Vector2 p)
        {
            float x=Mathf.Abs(p.x);for(int stop=0;stop<2;stop++)
            {
                if(Mathf.Sign(p.x)!=Side(stop))continue;float z=p.y-DoorZ(stop);
                if(x>9.14f&&x<10.28f&&Mathf.Abs(z)<1.42f)return false;
                if(Mathf.Abs(x-10.29f)<.18f&&Mathf.Abs(z)<1.85f)return false;
                if(x>9.45f&&x<10.47f&&Mathf.Abs(Mathf.Abs(z)-1.65f)<.19f)return false;
                if((Mathf.Abs(x-8.95f)<.20f||Mathf.Abs(x-10.3f)<.20f)&&Mathf.Abs(Mathf.Abs(z)-1.72f)<.20f)return false;
                if(new Vector2(x-7.04f,z+2.15f).sqrMagnitude<.21f*.21f)return false;
            }return true;
        }
        public static float CenterZ(int stop)=>DoorZ(stop)-Side(stop)*DoorLocalZ;
        public BusStops(StreetModel street)
        {
            model=street;bool enabled=false;foreach(var c in model.Cars)enabled|=c.bus;
            Riders=new Rider[enabled?8:0];
            for(int i=0;i<Riders.Length;i++)
            {
                var r=new Rider{stop=i/4,slot=i%4,stage=i%4==3?RiderStage.Onboard:RiderStage.Queue};Riders[i]=r;
                r.position=QueuePoint(r);
                // Keep all initial foot discs clear of the existing pedestrian population.
                for(int trial=0;r.Visible&&!Free(r,r.position,false)&&trial<100;trial++)
                    r.position=new Vector2(Side(r.stop)*(7.05f+(trial%5)*.28f),DoorZ(r.stop)-2.1f+(trial/5)*.22f);
            }
        }
        static Vector2 QueuePoint(Rider r)=>new Vector2(Side(r.stop)*9.0f,DoorZ(r.stop)-1.2f+r.slot*.8f);
        static Vector2 DoorPoint(StreetModel.Car car)=>new Vector2(car.X+car.Direction*1.29f,(float)car.z+car.Direction*DoorLocalZ);
        public bool NearUnservedStop(StreetModel.Car c)
        {
            if(!c.bus||c.busServed)return false;int stop=c.Direction<0?0:1;
            double ahead=c.Direction*(CenterZ(stop)-c.z);return ahead>=-8&&ahead<70;
        }
        public bool Holds(StreetModel.Car c)=>c.busStage==Stage.Boarding||c.busStage==Stage.Closing||c.busStage==Stage.Ready;
        public float Room(StreetModel.Car c)
        {
            if(Holds(c))return 0;if(c.busStage!=Stage.Approach)return float.MaxValue;
            return Mathf.Max(0,(float)(c.Direction*(CenterZ(c.Direction<0?0:1)-c.z)));
        }
        public void Wrapped(StreetModel.Car c)
        {c.busServed=false;c.busStage=Stage.Cruising;c.busTicks=0;c.busDoor=0;c.busOffset=0;}
        public bool Blocks(Vector2 a,Vector2 b)
        {
            foreach(var r in Riders)if(r!=null&&r.Visible&&StreetModel.SegmentDistanceSquared(a,b,r.position)<StreetModel.Separation*StreetModel.Separation-1e-6f)return true;
            return false;
        }
        bool Free(Rider self,Vector2 next,bool boarding)
        {
            if(!boarding&&!SidewalkRoutes.SegmentAllowed(self.position,next))return false;
            if(boarding)
            {
                if(self.owner<0)return false;var c=model.Cars[self.owner];var door=DoorPoint(c);
                if(c.speed>.001f||c.busStage!=Stage.Boarding||c.busDoor<.999f||Mathf.Abs(next.y-door.y)>.06f||Mathf.Abs(next.x)<5.7f)return false;
            }
            foreach(var p in model.People)if(p.active&&StreetModel.SegmentDistanceSquared(self.position,next,p.position)<StreetModel.Separation*StreetModel.Separation-1e-6f)return false;
            foreach(var r in Riders)if(r!=null&&r!=self&&r.Visible&&StreetModel.SegmentDistanceSquared(self.position,next,r.position)<StreetModel.Separation*StreetModel.Separation-1e-6f)return false;
            return true;
        }
        bool Move(Rider r,Vector2 goal,bool doorway)
        {
            Vector2 delta=goal-r.position;float length=delta.magnitude;if(length<.035f)return true;
            Vector2 forward=delta/length;float step=Mathf.Min(length,1.22f*StreetModel.Dt);var right=new Vector2(forward.y,-forward.x);
            for(int n=0;n<(doorway?1:5);n++)
            {
                var direction=n==0?forward:n==1?(forward+right).normalized:n==2?(forward-right).normalized:n==3?right:-right;
                var next=r.position+direction*step;if(!Free(r,next,doorway))continue;
                r.velocity=(next-r.position)/StreetModel.Dt;r.distance+=step;r.position=next;break;
            }
            return Vector2.Distance(r.position,goal)<.035f;
        }
        bool TransferActive(int stop)
        {foreach(var r in Riders)if(r.stop==stop&&(r.stage==RiderStage.Boarding||r.stage==RiderStage.Alighting))return true;return false;}
        public void Tick()
        {
            foreach(var r in Riders)r.velocity=Vector2.zero;
            for(int i=0;i<model.Cars.Length;i++)
            {
                var c=model.Cars[i];if(!c.bus||!c.active)continue;int stop=c.Direction<0?0:1;
                float ahead=(float)(c.Direction*(CenterZ(stop)-c.z));
                if(c.busStage==Stage.Cruising&&!c.busServed&&(c.lane==0||c.lane==3)&&ahead>=0&&ahead<35&&c.maneuver!=LaneChanges.Stage.Merging)
                {
                    if(c.maneuver!=LaneChanges.Stage.Idle)model.Maneuvers.Cancel(c,model.ActiveTicks);
                    c.busStage=Stage.Approach;
                }
                if(c.busStage==Stage.Approach)
                {
                    c.busOffset=c.Direction*.12f*Mathf.SmoothStep(0,1,Mathf.Clamp01((8-ahead)/8));
                    if(ahead<.003f&&c.speed<.001f&&owners[stop]<0)
                    {owners[stop]=i;c.busStage=Stage.Boarding;c.busTicks=0;c.busServed=true;alighted[stop]=false;boarded[stop]=0;c.hazardTicks=0;Arrivals++;}
                }
                else if(c.busStage==Stage.Boarding)
                {
                    c.busTicks++;c.hazardTicks++;c.busDoor=Mathf.Min(1,c.busTicks/(float)DoorTicks);
                    if(c.busDoor>=1)Service(stop,i);
                    if(c.busTicks>=DwellTicks&&!TransferActive(stop)&&(boarded[stop]>=2||c.busTicks>=DwellTicks+360))
                    {c.busStage=Stage.Closing;c.busTicks=0;}
                }
                else if(c.busStage==Stage.Closing)
                {
                    c.busTicks++;c.hazardTicks++;c.busDoor=Mathf.Max(0,1-c.busTicks/(float)DoorTicks);
                    if(c.busDoor<=0){owners[stop]=-1;c.busStage=Stage.Ready;c.busTicks=0;}
                }
                else if(c.busStage==Stage.Ready)
                {
                    if(c.maneuver==LaneChanges.Stage.Merging){c.busStage=Stage.Leaving;Departures++;}
                    else if(c.maneuver==LaneChanges.Stage.Idle&&model.ActiveTicks%30==0)model.Maneuvers.Request(model,i);
                }
                else if(c.busStage==Stage.Leaving)
                {
                    c.busOffset=c.Direction*.12f*(1-Mathf.SmoothStep(0,1,c.mergeTicks/(float)c.MergeDuration));
                    if(c.maneuver==LaneChanges.Stage.Idle){c.busStage=Stage.Cruising;c.busOffset=0;}
                }
            }
            foreach(var r in Riders)if(r.stage==RiderStage.Returning&&r.velocity.sqrMagnitude<1e-8f)
            {
                if(Move(r,QueuePoint(r),false))r.stage=RiderStage.Queue;
            }
        }
        void Service(int stop,int owner)
        {
            var c=model.Cars[owner];var door=DoorPoint(c);
            if(!TransferActive(stop))
            {
                if(!alighted[stop])
                {
                    alighted[stop]=true;
                    foreach(var r in Riders)if(r.stop==stop&&r.stage==RiderStage.Onboard&&(r.owner<0||r.owner==owner))
                    {r.owner=owner;r.position=new Vector2(c.Direction*5.82f,door.y);r.stage=RiderStage.Alighting;break;}
                }
                else if(boarded[stop]<2&&c.busTicks<DwellTicks+360)
                    foreach(var r in Riders)if(r.stop==stop&&r.stage==RiderStage.Queue){r.owner=owner;r.stage=RiderStage.Boarding;break;}
            }
            foreach(var r in Riders)if(r.stop==stop)
            {
                if(r.stage==RiderStage.Alighting)
                {
                    if(Move(r,new Vector2(c.Direction*7.35f,door.y),true)){r.stage=RiderStage.Returning;Alightings++;}
                }
                else if(r.stage==RiderStage.Boarding)
                {
                    // Reach the open doorway on the sidewalk first; only then cross the curb.
                    bool aligned=Mathf.Abs(r.position.y-door.y)<.035f;
                    var goal=aligned?new Vector2(c.Direction*5.82f,door.y):new Vector2(c.Direction*7.55f,door.y);
                    if(Move(r,goal,aligned)&&aligned){r.stage=RiderStage.Onboard;boarded[stop]++;Boardings++;}
                }
            }
        }
    }
}
