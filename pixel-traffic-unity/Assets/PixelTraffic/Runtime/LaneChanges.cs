using System;
using UnityEngine;

namespace PixelTraffic.UnityPrototype
{
    // Fixed-clock decisions, tracked rear cars and a reservation of both lanes while signaling/merging.
    public sealed class LaneChanges
    {
        public enum Stage { Idle,Waiting,Signaling,Merging }
        readonly StreetModel.Car[] cars;
        readonly double[,] rearPassAt;
        public int Completed {get;private set;}
        public int RearWaits {get;private set;}
        public int Canceled {get;private set;}
        public const int BlinkTicks=54,MergeTicks=90;
        public LaneChanges(StreetModel.Car[] cars)
        {
            if(cars.Length>32)throw new ArgumentOutOfRangeException(nameof(cars));this.cars=cars;rearPassAt=new double[cars.Length,cars.Length];
            for(int i=0;i<cars.Length;i++)cars[i].nextChangeTick=(8+i*11%23)*30;
        }
        public static bool SharesLane(StreetModel.Car a,StreetModel.Car b)
        {for(int lane=0;lane<4;lane++)if(a.Occupies(lane)&&b.Occupies(lane))return true;return false;}
        static double SignedAhead(StreetModel.Car a,StreetModel.Car b)
        {
            double span=StarterConfig.RouteEnd-StarterConfig.RouteStart;
            double distance=a.Direction*(b.z-a.z);return distance-span*Math.Floor((distance+span/2)/span);
        }
        bool Context(StreetModel model,StreetModel.Car car)
        {
            return car.active&&model.Signal==StreetModel.Phase.VehicleGreen&&model.PhaseSeconds<12&&model.DrivingPace>.85f&&car.speed>2&&
                Math.Abs(car.z-StarterConfig.CrossingZ)>50&&car.z>StarterConfig.RouteStart+22&&car.z<StarterConfig.RouteEnd-22;
        }
        public bool Request(StreetModel model,int index)
        {
            var car=cars[index];if(car.maneuver!=Stage.Idle||!Context(model,car))return false;
            car.targetLane=car.lane^1;car.maneuver=Stage.Waiting;car.requestTick=model.ActiveTicks;car.rearMask=0;car.blinks=0;
            // Track every target-lane rear car in the 60m observation range, including slow cars.
            for(int j=0;j<cars.Length;j++)if(j!=index&&cars[j].active&&cars[j].Occupies(car.targetLane))
            {double gap=SignedAhead(car,cars[j]);if(gap<0&&gap>=-60){car.rearMask|=1u<<j;rearPassAt[index,j]=gap-cars[j].totalDistance+car.totalDistance;}}
            if(car.rearMask!=0)RearWaits++;return true;
        }
        public bool SafeGap(int index)
        {
            var car=cars[index];
            for(int j=0;j<cars.Length;j++)if(j!=index&&cars[j].active&&cars[j].Occupies(car.targetLane))
            {
                double gap=SignedAhead(car,cars[j]);float half=(car.length+cars[j].length)*.5f;
                float front=half+2.2f+car.speed*1.2f+Mathf.Max(0,car.speed-cars[j].speed)*4.8f;
                float rear=half+2.2f+cars[j].speed*1.5f+Mathf.Max(0,cars[j].speed-car.speed)*4.8f;
                if(gap>=0?gap<front:-gap<rear)return false;
            }
            return true;
        }
        bool Busy(StreetModel.Car car)
        {foreach(var other in cars)if(other!=car&&other.Direction==car.Direction&&(other.maneuver==Stage.Signaling||other.maneuver==Stage.Merging))return true;return false;}
        public void Tick(StreetModel model)
        {
            for(int i=0;i<cars.Length;i++)
            {
                var car=cars[i];if(!car.active){if(car.maneuver!=Stage.Idle)Cancel(car,model.ActiveTicks);continue;}
                if(car.maneuver==Stage.Merging)
                {
                    car.mergeTicks++;
                    if(car.mergeTicks>=MergeTicks)
                    {car.lane=car.targetLane;car.targetLane=-1;car.maneuver=Stage.Idle;car.nextChangeTick=model.ActiveTicks+(28+i*7%33)*30;Completed++;}
                    continue;
                }
                if(car.maneuver==Stage.Idle&&model.AutomaticLaneChanges&&model.ActiveTicks>=car.nextChangeTick)Request(model,i);
                if(car.maneuver==Stage.Waiting)
                {
                    if(model.ActiveTicks-car.requestTick>3600||model.DrivingPace<=.85f){Cancel(car,model.ActiveTicks);continue;}
                    for(int j=0;j<cars.Length;j++)if(j!=i&&cars[j].active&&cars[j].Occupies(car.targetLane)&&(car.rearMask&(1u<<j))==0)
                    {
                        double gap=SignedAhead(car,cars[j]);
                        if(gap<0&&gap>=-60){car.rearMask|=1u<<j;rearPassAt[i,j]=gap-cars[j].totalDistance+car.totalDistance;}
                    }
                    for(int j=0;j<cars.Length;j++)if((car.rearMask&(1u<<j))!=0)
                    {
                        // Odometers distinguish a real rear pass from a wrapped signed-distance jump.
                        double gap=rearPassAt[i,j]+cars[j].totalDistance-car.totalDistance;
                        if(!cars[j].active||!cars[j].Occupies(car.targetLane)||gap>(car.length+cars[j].length)*.5f+2.2f)car.rearMask&=~(1u<<j);
                    }
                    if(car.rearMask==0&&Context(model,car)&&!Busy(car)&&SafeGap(i))
                    {car.maneuver=Stage.Signaling;car.signalTicks=0;car.blinks=1;}
                }
                else if(car.maneuver==Stage.Signaling)
                {
                    bool rearArrived=false;
                    for(int j=0;j<cars.Length;j++)if(j!=i&&cars[j].active&&cars[j].Occupies(car.targetLane))
                    {double gap=SignedAhead(car,cars[j]);if(gap<0&&gap>=-60)rearArrived=true;}
                    if(rearArrived){Cancel(car,model.ActiveTicks);continue;}
                    car.signalTicks++;
                    if(model.Signal!=StreetModel.Phase.VehicleGreen||model.DrivingPace<=.85f){Cancel(car,model.ActiveTicks);continue;}
                    if(car.signalTicks<BlinkTicks&&car.signalTicks%18==0)car.blinks++;
                    if(car.signalTicks>=BlinkTicks)
                    {
                        if(car.blinks==3&&car.rearMask==0&&SafeGap(i)&&Math.Abs(car.z-StarterConfig.CrossingZ)>18)
                        {car.maneuver=Stage.Merging;car.mergeTicks=0;}
                        else Cancel(car,model.ActiveTicks);
                    }
                }
            }
        }
        public void Cancel(StreetModel.Car car,int ticks)
        {car.maneuver=Stage.Idle;car.targetLane=-1;car.signalTicks=0;car.mergeTicks=0;car.rearMask=0;car.nextChangeTick=ticks+900;Canceled++;}
        public static bool IndicatorOn(StreetModel.Car car)=>car.maneuver==Stage.Signaling&&car.signalTicks%18<9;
        public static int IndicatorSide(StreetModel.Car car)=>car.targetLane<0?0:Math.Sign(car.targetLane-car.lane)*car.Direction;
        public static float Yaw(StreetModel.Car car)
        {
            if(car.maneuver!=Stage.Merging)return 0;float t=car.mergeTicks/(float)MergeTicks;
            float lateral=(car.targetLane-car.lane)*StarterConfig.LaneWidth*6*t*(1-t)/3;
            return Mathf.Clamp(Mathf.Atan2(lateral,Mathf.Max(2,car.speed))*Mathf.Rad2Deg*car.Direction,-8,8);
        }
    }
}
