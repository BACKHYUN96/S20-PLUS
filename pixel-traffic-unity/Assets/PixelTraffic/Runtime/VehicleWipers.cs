using System;
using UnityEngine;

namespace PixelTraffic.UnityPrototype
{
    // Only StreetSimulation's gated clock advances this pair. No independent Update/coroutine.
    public sealed class VehicleWipers : MonoBehaviour
    {
        [SerializeField] private Transform[] pivots;
        [SerializeField] private Vector3[] screen;
        [SerializeField] private float startDelay;
        private bool requested,moving,finishing,first;
        private double sweepTime,waitTime,duration;
        private float lastRain;
        public const float ParkAngle=8,TopAngle=95;
        public Transform[] Pivots=>pivots;
        public Vector3[] Screen=>screen;
        public float StartDelay=>startDelay;
        public bool Moving=>moving;
        public float Angle {get;private set;}
        public int CompletedSweeps {get;private set;}
        public void Configure(Transform[] value,Vector3[] windshield,float seed)
        {pivots=value;screen=windshield;startDelay=Mathf.Clamp01(seed)*.7f;ResetState();}
        public void ResetState()
        {requested=moving=finishing=false;first=true;sweepTime=waitTime=0;lastRain=0;CompletedSweeps=0;Pose(ParkAngle);}
        private void Pose(float angle)
        {Angle=angle;foreach(var pivot in pivots)pivot.localRotation=Quaternion.AngleAxis(angle,Vector3.forward);}
        private void StartSweep(float rain)
        {moving=true;sweepTime=0;duration=1.55-.75*Mathf.Clamp01(rain);}
        public void Advance(double elapsed,float rain)
        {
            if(double.IsNaN(elapsed)||double.IsInfinity(elapsed)||elapsed<0||float.IsNaN(rain)||float.IsInfinity(rain))throw new ArgumentOutOfRangeException(nameof(elapsed));
            if(elapsed==0)return;
            rain=Mathf.Clamp01(rain);bool wet=rain>.02f;
            if(wet&&!requested){first=true;waitTime=0;}
            if(requested&&!wet)
            {
                // Finish the current stroke; if resting between wipes, do one last complete wipe.
                finishing=true;if(!moving)StartSweep(lastRain);
            }
            if(wet){lastRain=rain;finishing=false;}
            requested=wet;
            while(elapsed>1e-10)
            {
                if(moving)
                {
                    double step=Math.Min(elapsed,duration-sweepTime);sweepTime+=step;elapsed-=step;
                    double wave=.5-.5*Math.Cos(sweepTime/duration*Math.PI*2);
                    Pose(Mathf.Lerp(ParkAngle,TopAngle,(float)wave));
                    if(sweepTime+1e-9<duration)break;
                    moving=false;CompletedSweeps++;waitTime=0;first=false;Pose(ParkAngle);
                    if(finishing){finishing=false;break;}
                }
                else
                {
                    if(!wet)break;
                    double gap=first?startDelay:6*(1-Mathf.InverseLerp(.38f,.78f,rain));
                    double step=Math.Min(elapsed,Math.Max(0,gap-waitTime));waitTime+=step;elapsed-=step;
                    if(waitTime+1e-9<gap)break;
                    StartSweep(rain);
                }
            }
        }
    }
}
