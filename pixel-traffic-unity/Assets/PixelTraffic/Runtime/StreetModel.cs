using System;
using UnityEngine;

namespace PixelTraffic.UnityPrototype
{
    // One deterministic 30Hz clock owns both traffic and pedestrians; rendering cannot advance it.
    public sealed class StreetModel
    {
        public enum Phase { VehicleGreen, VehicleYellow, VehicleClearance, Walk, PedestrianClearance, Restart }
        public enum Activity { Roam, Approach, Wait, Cross, Exit }
        public sealed class Car
        {
            public int lane;public double z;public float cruise,speed,length,distance;public bool committed;
            public int Direction => lane<2?-1:1;
        }
        public sealed class Person
        {
            public int side,slot=-1,crossings,pathCount,pathCursor;public bool active;
            public Vector2 position,goal,velocity;public Activity activity;
            public float speed,cooldown,walkDistance,waitingTime;
            public uint random;public readonly int[] path=new int[600];
        }
        public const float Dt=1f/30, Separation=.40f;
        public readonly Car[] Cars;
        public readonly Person[] People=new Person[100];
        public readonly SidewalkRoutes Routes=new SidewalkRoutes();
        private readonly double[] oldZ;
        private readonly int[,] slots=new int[2,6];
        private int tick;
        private float phaseTime;
        public Phase Signal {get;private set;}
        public int RequestedPeople {get;private set;}
        public int Cycles {get;private set;}
        public bool VehicleAllowed => Signal==Phase.VehicleGreen||Signal==Phase.VehicleYellow;
        public bool CanEnter => Signal==Phase.Walk;
        public float PhaseSeconds => phaseTime;
        public StreetModel(int population,Car[] cars)
        {
            Cars=cars;oldZ=new double[cars.Length];
            for(int side=0;side<2;side++)for(int slot=0;slot<6;slot++)slots[side,slot]=-1;
            for(int i=0;i<People.Length;i++)
            {
                var p=new Person {side=i%2==0?-1:1,random=(uint)(7847+i*971),speed=1.05f+(i%7)*.04f,activity=Activity.Roam,cooldown=8+i%32};People[i]=p;
                Place(i);Roam(p);
            }
            SetPopulation(population);
        }
        private static float Random(Person p)
        {
            uint s=p.random;s^=s<<13;s^=s>>17;s^=s<<5;p.random=s;return(s&0xffffff)/16777216f;
        }
        private void Place(int index)
        {
            Person p=People[index];
            for(int trial=0;trial<Routes.Count;trial++)
            {
                int node=(index*137+trial*29)%Routes.Count;if(!Routes.IsOpen(node))continue;
                Vector2 pos=Routes.Node(node,p.side);bool free=true;
                for(int j=0;j<index;j++)if((People[j].position-pos).sqrMagnitude<Separation*Separation)free=false;
                if(free){p.position=pos;return;}
            }
            throw new InvalidOperationException("No free sidewalk spawn.");
        }
        public void SetPopulation(int count)
        {
            RequestedPeople=Mathf.Clamp(count,4,100);
            for(int i=0;i<People.Length;i++)
            {
                Person p=People[i];
                if(i<RequestedPeople&&!p.active)
                {
                    // Reactivating waits for a free stored foot position; no pop through another person.
                    bool free=true;for(int j=0;j<People.Length;j++)if(People[j].active&&(People[j].position-p.position).sqrMagnitude<Separation*Separation)free=false;
                    if(free)p.active=true;
                }
                if(i>=RequestedPeople&&p.active&&p.activity!=Activity.Cross)Retire(i);
            }
        }
        private void Retire(int i)
        {
            Person p=People[i];Release(i);p.active=false;p.activity=Activity.Roam;Roam(p);
        }
        private void Path(Person p,Vector2 goal)
        {
            p.goal=goal;p.pathCount=Routes.Find(p.position,goal,p.side,p.path);p.pathCursor=0;
        }
        private void Roam(Person p)
        {
            int row=Mathf.Clamp(Mathf.RoundToInt((p.position.y+28)/.45f)+(Random(p)<.5f?-1:1)*(25+(int)(Random(p)*55)),1,398);
            if(p.cooldown<=0)row=Mathf.Clamp(Mathf.RoundToInt((StarterConfig.CrossingZ-28+Random(p)*64+28)/.45f),1,398);
            int col=(int)(Random(p)*10),node=row*10+col;
            for(int trial=0;trial<10&&!Routes.IsOpen(node);trial++)node=row*10+(col+trial+1)%10;
            if(!Routes.IsOpen(node))node=row*10;
            Path(p,Routes.Node(node,p.side));
        }
        private Vector2 SlotPoint(Person p) => new Vector2(p.side*(7.02f+.45f*(p.slot/2)),StarterConfig.CrossingZ+(p.side<0?-1.2f:.4f)+.8f*(p.slot%2));
        private void Release(int i)
        {
            Person p=People[i];if(p.slot<0)return;slots[p.side<0?0:1,p.slot]=-1;p.slot=-1;
        }
        private bool Reserve(int i)
        {
            Person p=People[i];if(Mathf.Abs(p.position.y-StarterConfig.CrossingZ)>45)return false;
            int side=p.side<0?0:1;
            for(int n=0;n<6;n++)if(slots[side,n]<0)
            {
                slots[side,n]=i;p.slot=n;p.activity=Activity.Approach;p.waitingTime=0;Path(p,SlotPoint(p));return true;
            }
            return false;
        }
        public void Tick()
        {
            tick++;phaseTime+=Dt;UpdateSignal();MoveCars();
            // Rotating update order removes a permanent priority bias; pairs never share a foot disc.
            for(int n=0;n<People.Length;n++)MovePerson((tick+n)%People.Length);
            if(tick%30==0)SetPopulation(RequestedPeople);
        }
        private void Change(Phase phase){Signal=phase;phaseTime=0;}
        private void UpdateSignal()
        {
            if(Signal==Phase.VehicleGreen&&phaseTime>=18)
            {
                foreach(var car in Cars)
                {
                    float stop=StopCenter(car);float room=(float)(car.Direction*(stop-car.z));
                    car.committed=(room>=0&&room<car.speed*car.speed/6.8f+1)||(room<0&&car.Direction*(car.z-StarterConfig.CrossingZ)<5+car.length/2);
                }
                Change(Phase.VehicleYellow);
            }
            else if(Signal==Phase.VehicleYellow&&phaseTime>=3)Change(Phase.VehicleClearance);
            else if(Signal==Phase.VehicleClearance&&phaseTime>=1&&!RoadOccupied()&&!CommittedApproach())Change(Phase.Walk);
            else if(Signal==Phase.Walk&&phaseTime>=7)Change(Phase.PedestrianClearance);
            else if(Signal==Phase.PedestrianClearance&&phaseTime>=1&&!CrossingActive())Change(Phase.Restart);
            else if(Signal==Phase.Restart&&phaseTime>=2){Cycles++;Change(Phase.VehicleGreen);}
        }
        private float StopCenter(Car car) => StarterConfig.CrossingZ-car.Direction*(4+car.length/2);
        private bool CommittedApproach()
        {
            foreach(var car in Cars)if(car.committed&&car.Direction*(car.z-StarterConfig.CrossingZ)<4+car.length/2)return true;
            return false;
        }
        public bool RoadOccupied()
        {
            foreach(var car in Cars)if(Math.Abs(car.z-StarterConfig.CrossingZ)<2.5+car.length/2)return true;
            return false;
        }
        public bool CrossingActive()
        {
            foreach(var p in People)if(p.active&&p.activity==Activity.Cross)return true;return false;
        }
        private void MoveCars()
        {
            double span=StarterConfig.RouteEnd-StarterConfig.RouteStart;
            for(int i=0;i<Cars.Length;i++)oldZ[i]=Cars[i].z;
            for(int i=0;i<Cars.Length;i++)
            {
                Car car=Cars[i];float room=float.MaxValue;
                for(int j=0;j<Cars.Length;j++)if(j!=i&&Cars[j].lane==car.lane)
                {
                    double gap=car.Direction*(oldZ[j]-oldZ[i]);gap=(gap%span+span)%span;
                    room=Mathf.Min(room,(float)gap-(car.length+Cars[j].length)/2-1.8f);
                }
                if(Signal!=Phase.VehicleGreen&&!car.committed)
                {
                    double distance=car.Direction*(StopCenter(car)-oldZ[i]);
                    if(distance<0&&distance>-.001)distance=0; // Round-off at a stop line is not a completed route loop.
                    distance=(distance%span+span)%span;
                    room=Mathf.Min(room,(float)distance);
                }
                float target=Mathf.Min(car.cruise,Mathf.Sqrt(Mathf.Max(0,6.8f*room)));
                car.speed=Mathf.MoveTowards(car.speed,target,(target>car.speed?1.8f:3.4f)*Dt);
                float travel=Mathf.Min(car.speed*Dt,Mathf.Max(0,room));if(travel<.0005f){travel=0;car.speed=0;}
                double next=oldZ[i]+car.Direction*travel;
                if(Signal!=Phase.VehicleGreen&&!car.committed&&Math.Abs(next-StopCenter(car))<.001)
                { next=StopCenter(car);travel=Mathf.Max(0,(float)(car.Direction*(next-oldZ[i])));car.speed=0; }
                next=StarterConfig.RouteStart+((next-StarterConfig.RouteStart)%span+span)%span;
                car.distance=travel;car.z=next;
                if(car.committed&&car.Direction*(car.z-StarterConfig.CrossingZ)>5+car.length/2)car.committed=false;
            }
        }
        private void MovePerson(int i)
        {
            Person p=People[i];if(!p.active)return;p.velocity=Vector2.zero;p.cooldown-=Dt;
            if(p.activity==Activity.Roam&&p.cooldown<=0)Reserve(i);
            if(p.activity==Activity.Approach||p.activity==Activity.Wait)p.waitingTime+=Dt;
            if(p.activity==Activity.Wait)
            {
                if(!CanEnter)return;
                Vector2 start=new Vector2(p.side*7.02f,p.position.y);
                // Back rows cannot enter through waiting front rows. Move to the curb using avoidance first.
                if(Mathf.Abs(p.position.x)>7.10f){Move(i,start,false);return;}
                bool free=true;
                foreach(var other in People)if(other.active&&other.activity==Activity.Cross&&Mathf.Abs(other.position.y-p.position.y)<.3f&&Vector2.Distance(other.position,p.position)<.70f)free=false;
                if(!free)return;
                Release(i);p.activity=Activity.Cross;p.goal=new Vector2(-p.side*7.06f,p.position.y);
            }
            if(p.activity==Activity.Cross)
            {
                Move(i,p.goal,true);
                if(Mathf.Abs(p.position.x-p.goal.x)<.08f)
                {
                    p.side=-p.side;p.crossings++;p.activity=Activity.Exit;p.cooldown=45+Random(p)*60;
                    if(i>=RequestedPeople){Retire(i);return;}
                    Path(p,new Vector2(p.side*9.99f,p.position.y));
                }
                return;
            }
            Vector2 target=p.goal;
            while(p.pathCursor<p.pathCount&&Vector2.Distance(p.position,Routes.Node(p.path[p.pathCursor],p.side))<.12f)p.pathCursor++;
            if(p.pathCursor<p.pathCount)target=Routes.Node(p.path[p.pathCursor],p.side);
            Move(i,target,false);
            if(Vector2.Distance(p.position,p.goal)<.14f)
            {
                if(p.activity==Activity.Approach)p.activity=Activity.Wait;
                else {if(i>=RequestedPeople){Retire(i);return;}p.activity=Activity.Roam;Roam(p);}
            }
        }
        private static float SegmentDistanceSquared(Vector2 a,Vector2 b,Vector2 p)
        {
            Vector2 d=b-a;float t=d.sqrMagnitude<1e-9f?0:Mathf.Clamp01(Vector2.Dot(p-a,d)/d.sqrMagnitude);return (a+t*d-p).sqrMagnitude;
        }
        private bool Free(int i,Vector2 next,bool crossing)
        {
            Person p=People[i];
            if(crossing){if(next.y<8.2f||next.y>11.8f||Mathf.Abs(next.x)>7.25f)return false;}
            else if(!SidewalkRoutes.SegmentAllowed(p.position,next))return false;
            for(int j=0;j<People.Length;j++)if(j!=i&&People[j].active&&SegmentDistanceSquared(p.position,next,People[j].position)<Separation*Separation-1e-6f)return false;
            return true;
        }
        private void Move(int i,Vector2 goal,bool crossing)
        {
            Person p=People[i];Vector2 difference=goal-p.position;float distance=difference.magnitude;if(distance<.001f)return;
            Vector2 forward=difference/distance;float step=Mathf.Min(distance,p.speed*Dt);
            // Prefer each walker's right side. Opposing walkers choose opposite world sides.
            Vector2 right=new Vector2(forward.y,-forward.x);
            for(int attempt=0;attempt<7;attempt++)
            {
                Vector2 direction=forward;
                if(attempt==1)direction=(forward+right).normalized;
                if(attempt==2)direction=(forward-right).normalized;
                if(attempt==3)direction=right;
                if(attempt==4)direction=-right;
                if(attempt==5)direction=(-forward+right).normalized;
                if(attempt==6)direction=-forward;
                if(crossing&&attempt>0)break; // Opposite streams have separate zebra bands; no diagonal lane change in traffic.
                Vector2 next=p.position+direction*step;
                if(!Free(i,next,crossing))continue;
                p.velocity=(next-p.position)/Dt;p.walkDistance+=step;p.position=next;return;
            }
        }
    }
}
