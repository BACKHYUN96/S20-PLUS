using UnityEngine;

namespace PixelTraffic.UnityPrototype
{
    public sealed class VehicleLighting : MonoBehaviour
    {
        [SerializeField]Renderer head,tail,left,right,beams;
        MaterialPropertyBlock block;
        public Renderer Head=>head;public Renderer Tail=>tail;public Renderer Left=>left;public Renderer Right=>right;public Renderer Beams=>beams;
        public float BrakeGain {get;private set;}
        public void Configure(Renderer h,Renderer t,Renderer l,Renderer r,Renderer b){head=h;tail=t;left=l;right=r;beams=b;}
        void Tint(Renderer renderer,Color colour,Color emission)
        {block.Clear();block.SetColor("_BaseColor",colour);block.SetColor("_EmissionColor",emission);renderer.SetPropertyBlock(block);}
        public void Apply(StreetModel.Car car,float night,float wet)
        {
            if(block==null)block=new MaterialPropertyBlock();
            Tint(head,new Color(.93f,.94f,.86f),new Color(1,.95f,.76f)*(.16f+night*1.65f));
            BrakeGain=car.braking?2.6f:.06f+night*.42f;
            Tint(tail,car.braking?new Color(1,.07f,.025f):new Color(.46f,.025f,.012f),new Color(1,.035f,.009f)*BrakeGain);
            int side=LaneChanges.IndicatorSide(car);bool on=car.active&&LaneChanges.IndicatorOn(car);
            bool hazard=car.active&&(car.busStage==BusStops.Stage.Boarding||car.busStage==BusStops.Stage.Closing)&&car.hazardTicks%18<9;
            Indicator(left,hazard||(on&&side<0));Indicator(right,hazard||(on&&side>0));
            bool beamOn=car.active&&night>.015f;if(beams.enabled!=beamOn)beams.enabled=beamOn;
            if(beamOn)Tint(beams,new Color(.94f,.9f,.65f,night*(.14f+wet*.08f)),new Color(.045f,.041f,.024f)*night);
        }
        void Indicator(Renderer renderer,bool lit)=>Tint(renderer,lit?new Color(1,.48f,.015f):new Color(.28f,.13f,.025f),lit?new Color(1,.35f,.005f)*2.3f:Color.black);
    }
}
