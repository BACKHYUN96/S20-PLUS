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
            public int lane;public double z;public float cruise,speed,length,distance;public float width=2.2f,height=1.9f;public bool committed,active=true;public int rank;
            public LaneChanges.Stage maneuver;public int targetLane=-1,signalTicks,mergeTicks,blinks,requestTick;
            public double nextChangeTick,totalDistance;public uint rearMask;public bool braking;
            // A conservative longitudinal envelope covers any yaw up to eight degrees.
            // Keep it constant through reservations so the queue gap cannot shrink at merge start.
            public float SafetyLength => length+width*Mathf.Sin(8*Mathf.Deg2Rad);
            public int MergeDuration => length>6?180:LaneChanges.MergeTicks;
            public int Direction => lane<2?-1:1;
            public float X => (lane-1.5f)*StarterConfig.LaneWidth+(maneuver==LaneChanges.Stage.Merging?(targetLane-lane)*StarterConfig.LaneWidth*Mathf.SmoothStep(0,1,mergeTicks/(float)MergeDuration):0);
            public bool Occupies(int value)=>lane==value||((maneuver==LaneChanges.Stage.Signaling||maneuver==LaneChanges.Stage.Merging)&&targetLane==value);
        }
        public sealed class Person
        {
            public int side,slot=-1,crossings,pathCount,pathCursor;public bool active,retiring,weatherHidden;
            public Vector2 position,goal,velocity,detour;public Activity activity;public float detourSeconds;
            public float speed,cooldown,retrySeconds,routeRepairSeconds,walkDistance,waitingTime;
            public uint random;public readonly int[] path=new int[600];
        }
        public const float Dt=1f/30, Separation=.40f;
        public readonly Car[] Cars;
        public readonly Person[] People=new Person[100];
        public readonly SidewalkRoutes Routes=new SidewalkRoutes();
        private readonly double[] oldZ;
        public readonly LaneChanges Maneuvers;
        public bool AutomaticLaneChanges=true;
        public Func<Vector2,Vector3,bool> VisibleVehicle;
        public bool VehicleVisible(Car car,double z)
        {
            var point=new Vector2(car.X,(float)z);
            return VisibleVehicle!=null?VisibleVehicle(point,new Vector3(car.width+car.length*Mathf.Sin(8*Mathf.Deg2Rad),car.height,car.SafetyLength)):Visible(point);
        }
        public int ActiveTicks=>tick;
        private readonly int[,] slots=new int[2,6];
        private int tick;
        private float phaseTime,spawnWait;
        private float peopleFraction=1,trafficFraction=1,drivingPace=1,walkingPace=1;
        public Func<Vector2,bool> Visible = point => point.y > -25 && point.y < 160;
        public int WeatherPeopleTarget => Mathf.Max(4,Mathf.RoundToInt(RequestedPeople*peopleFraction));
        public int WeatherCarsPerLane => Mathf.Clamp(Mathf.RoundToInt(StarterConfig.VehiclesPerLane*trafficFraction),2,StarterConfig.VehiclesPerLane);
        public float WalkingPace => walkingPace;
        public float DrivingPace => drivingPace;
        public int WeatherDepartures { get; private set; }
        public int WeatherArrivals { get; private set; }
        public Phase Signal {get;private set;}
        public int RequestedPeople {get;private set;}
        public int Cycles {get;private set;}
        public bool VehicleAllowed => Signal==Phase.VehicleGreen||Signal==Phase.VehicleYellow;
        public bool CanEnter => Signal==Phase.Walk;
        public float PhaseSeconds => phaseTime;
        public StreetModel(int population,Car[] cars)
        {
            Cars=cars;oldZ=new double[cars.Length];
            int[] ranks=new int[4];foreach(var car in Cars)car.rank=ranks[car.lane]++;
            Maneuvers=new LaneChanges(Cars);
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
                if(i<WeatherPeopleTarget&&i<RequestedPeople&&!p.active&&!p.weatherHidden)
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
            Person p=People[i];Release(i);p.active=false;p.retiring=false;p.activity=Activity.Roam;Roam(p);
        }
        public void SetWeather(float peopleGain,float trafficGain,float driving,float walking)
        {
            peopleFraction=Mathf.Clamp01(peopleGain);trafficFraction=Mathf.Clamp01(trafficGain);
            drivingPace=Mathf.Clamp(driving,.5f,1);walkingPace=Mathf.Clamp(walking,1,1.25f);
        }
        private void WeatherPopulation()
        {
            int target=WeatherPeopleTarget;
            for(int i=0;i<People.Length;i++)
            {
                var p=People[i];
                if(p.retiring&&i<target){p.retiring=false;p.detourSeconds=0;Roam(p);}
                if(p.active&&i>=target&&i<RequestedPeople&&!p.retiring&&p.activity!=Activity.Cross)
                { Release(i);p.retiring=true;p.detourSeconds=0;p.activity=Activity.Roam;Path(p,new Vector2(p.side*8.22f,-27.55f)); }
            }
            spawnWait-=Dt;
            if(spawnWait<=0)
            {
                for(int i=0;i<target;i++)
                {
                    var p=People[i];if(p.active||!p.weatherHidden)continue;
                    bool placed=false;
                    for(int column=0;column<SidewalkRoutes.Columns;column++)
                    {
                        Vector2 point=Routes.Node(column,p.side);if(!Routes.IsOpen(column)||Visible(point))continue;
                        bool free=true;foreach(var other in People)if(other.active&&(other.position-point).sqrMagnitude<Separation*Separation)free=false;
                        if(!free)continue;
                        p.position=point;p.active=true;p.weatherHidden=false;p.retiring=false;p.velocity=Vector2.zero;p.cooldown=8+Random(p)*16;p.detourSeconds=0;Roam(p);placed=true;break;
                    }
                    if(placed){spawnWait=1.5f;WeatherArrivals++;break;}
                }
            }
            foreach(var car in Cars)if(!car.active&&car.rank<WeatherCarsPerLane)
            {
                double entry=StarterConfig.RouteStart+.05;bool free=true;
                if(VehicleVisible(car,entry))continue;
                double span=StarterConfig.RouteEnd-StarterConfig.RouteStart;
                foreach(var other in Cars)if(other.active&&other.Occupies(car.lane))
                {
                    double distance=Math.Abs(entry-other.z);distance=Math.Min(distance,span-distance);
                    if(distance<(car.SafetyLength+other.SafetyLength)/2+2.2)free=false;
                }
                if(free){car.active=true;car.z=entry;car.speed=0;car.distance=0;car.committed=false;}
            }
        }
        private void Path(Person p,Vector2 goal)
        {
            p.goal=goal;p.pathCount=Routes.Find(p.position,goal,p.side,p.path);p.pathCursor=0;
            if(p.pathCount>1&&SidewalkRoutes.SegmentAllowed(p.position,Routes.Node(p.path[1],p.side)))p.pathCursor=1;
        }
        private void Roam(Person p)
        {
            int row=Mathf.Clamp(Mathf.RoundToInt((p.position.y+28)/.45f)+(Random(p)<.5f?-1:1)*(25+(int)(Random(p)*55)),1,398);
            if(p.cooldown>0&&p.crossings>0&&Mathf.Abs(p.position.y-StarterConfig.CrossingZ)<24)
                row=Mathf.Clamp(Mathf.RoundToInt((p.position.y+28)/.45f)+(p.position.y<StarterConfig.CrossingZ?-1:1)*(55+(int)(Random(p)*30)),1,398);
            if(p.cooldown<=0)row=Mathf.Clamp(Mathf.RoundToInt((StarterConfig.CrossingZ-24+Random(p)*48+28)/.45f),1,398);
            int columns=SidewalkRoutes.Columns;int col=(int)(Random(p)*columns),node=row*columns+col;
            for(int trial=0;trial<columns&&!Routes.IsOpen(node);trial++)node=row*columns+(col+trial+1)%columns;
            if(!Routes.IsOpen(node))node=row*columns;
            Path(p,Routes.Node(node,p.side));
        }
        private Vector2 SlotPoint(Person p) => new Vector2(p.side*(7.02f+.45f*(p.slot/2)),StarterConfig.CrossingZ+(p.side<0?-1.2f:.4f)+.8f*(p.slot%2));
        private void Release(int i)
        {
            Person p=People[i];if(p.slot<0)return;slots[p.side<0?0:1,p.slot]=-1;p.slot=-1;
        }
        private bool Reserve(int i)
        {
            Person p=People[i];if(Mathf.Abs(p.position.y-StarterConfig.CrossingZ)>18)return false;
            // Someone who has not yet crossed takes a free slot before repeated visitors.
            if(p.crossings>0)foreach(var other in People)if(other.active&&other.side==p.side&&other.crossings==0&&other.cooldown<=0&&Mathf.Abs(other.position.y-StarterConfig.CrossingZ)<=18)return false;
            int side=p.side<0?0:1;
            for(int n=0;n<6;n++)if(slots[side,n]<0)
            {
                slots[side,n]=i;p.slot=n;p.activity=Activity.Approach;p.waitingTime=0;Path(p,SlotPoint(p));return true;
            }
            return false;
        }
        public void Tick()
        {
            tick++;phaseTime+=Dt;WeatherPopulation();UpdateSignal();Maneuvers.Tick(this);MoveCars();
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
                    if(!car.active){car.committed=false;continue;}
                    float stop=StopCenter(car);float room=(float)(car.Direction*(stop-car.z));
                    car.committed=(room>=0&&room<car.speed*car.speed/6.8f+1)||(room<0&&car.Direction*(car.z-StarterConfig.CrossingZ)<5+car.SafetyLength/2);
                }
                Change(Phase.VehicleYellow);
            }
            else if(Signal==Phase.VehicleYellow&&phaseTime>=3)Change(Phase.VehicleClearance);
            else if(Signal==Phase.VehicleClearance&&phaseTime>=1&&!RoadOccupied()&&!CommittedApproach())Change(Phase.Walk);
            else if(Signal==Phase.Walk&&phaseTime>=7)Change(Phase.PedestrianClearance);
            else if(Signal==Phase.PedestrianClearance&&phaseTime>=1&&!CrossingActive())Change(Phase.Restart);
            else if(Signal==Phase.Restart&&phaseTime>=2){Cycles++;Change(Phase.VehicleGreen);}
        }
        private float StopCenter(Car car) => StarterConfig.CrossingZ-car.Direction*(4+car.SafetyLength/2);
        private bool CommittedApproach()
        {
            foreach(var car in Cars)if(car.active&&car.committed&&car.Direction*(car.z-StarterConfig.CrossingZ)<4+car.SafetyLength/2)return true;
            return false;
        }
        public bool RoadOccupied()
        {
            foreach(var car in Cars)if(car.active&&Math.Abs(car.z-StarterConfig.CrossingZ)<2.5+car.SafetyLength/2)return true;
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
                Car car=Cars[i];car.distance=0;if(!car.active)continue;
                if(car.rank>=WeatherCarsPerLane&&car.maneuver!=LaneChanges.Stage.Merging&&car.maneuver!=LaneChanges.Stage.Signaling&&!VehicleVisible(car,car.z))
                {car.active=false;car.committed=false;car.speed=0;car.braking=false;Maneuvers.Cancel(car,tick);continue;}
                float room=float.MaxValue;
                for(int j=0;j<Cars.Length;j++)if(j!=i&&Cars[j].active&&LaneChanges.SharesLane(car,Cars[j]))
                {
                    double gap=car.Direction*(oldZ[j]-oldZ[i]);gap=(gap%span+span)%span;
                    room=Mathf.Min(room,(float)gap-(car.SafetyLength+Cars[j].SafetyLength)/2-1.8f);
                }
                if(Signal!=Phase.VehicleGreen&&!car.committed)
                {
                    double distance=car.Direction*(StopCenter(car)-oldZ[i]);
                    if(distance<0&&distance>-.001)distance=0; // Round-off at a stop line is not a completed route loop.
                    distance=(distance%span+span)%span;
                    room=Mathf.Min(room,(float)distance);
                }
                float yield=car.maneuver==LaneChanges.Stage.Waiting&&car.rearMask!=0?.72f:1;
                float target=Mathf.Min(car.cruise*drivingPace*yield,Mathf.Sqrt(Mathf.Max(0,6.8f*room)));
                float previousSpeed=car.speed;
                car.speed=Mathf.MoveTowards(car.speed,target,(target>car.speed?1.8f:3.4f)*Dt);
                float travel=Mathf.Min(car.speed*Dt,Mathf.Max(0,room));if(travel<.0005f){travel=0;car.speed=0;}
                double next=oldZ[i]+car.Direction*travel;
                if(Signal!=Phase.VehicleGreen&&!car.committed&&Math.Abs(next-StopCenter(car))<.001)
                { next=StopCenter(car);travel=Mathf.Max(0,(float)(car.Direction*(next-oldZ[i])));car.speed=0; }
                next=StarterConfig.RouteStart+((next-StarterConfig.RouteStart)%span+span)%span;
                car.distance=travel;car.totalDistance+=travel;car.z=next;
                car.braking=previousSpeed-car.speed>.015f||(car.speed<.1f&&room<.3f);
                if(car.active&&car.committed&&car.Direction*(car.z-StarterConfig.CrossingZ)>5+car.SafetyLength/2)car.committed=false;
            }
        }
        private void MovePerson(int i)
        {
            Person p=People[i];if(!p.active)return;p.velocity=Vector2.zero;
            bool seekingStarts=p.cooldown>0&&p.cooldown<=Dt;p.cooldown-=Dt;p.retrySeconds-=Dt;p.routeRepairSeconds-=Dt;
            if(p.retiring)
            {
                if(!Visible(p.position)){Retire(i);p.weatherHidden=true;WeatherDepartures++;return;}
            }
            if(seekingStarts&&!p.retiring&&p.activity==Activity.Roam)Roam(p);
            if(!p.retiring&&p.activity==Activity.Roam&&p.cooldown<=0&&p.retrySeconds<=0&&Mathf.Abs(p.position.y-StarterConfig.CrossingZ)<=18&&!Reserve(i))
            {
                p.retrySeconds=8+Random(p)*12;
                if(p.crossings>0)p.cooldown=45+Random(p)*60;
                Roam(p);
            }
            if(p.activity==Activity.Approach||p.activity==Activity.Wait)p.waitingTime+=Dt;
            if(p.activity==Activity.Wait)
            {
                if(!CanEnter)return;
                Vector2 start=new Vector2(p.side*7.02f,SlotPoint(p).y);
                // Back rows cannot enter through waiting front rows. Move to the curb using avoidance first.
                if(Mathf.Abs(p.position.x)>7.10f||Mathf.Abs(p.position.y-start.y)>.06f){Move(i,start,false);return;}
                bool free=true;
                foreach(var other in People)if(other.active&&other.activity==Activity.Cross&&Mathf.Abs(other.position.y-p.position.y)<.3f&&Vector2.Distance(other.position,p.position)<.70f)free=false;
                Vector2 entry=new Vector2(p.side*6.30f,p.position.y);
                for(int j=0;j<People.Length;j++)if(j!=i&&People[j].active&&SegmentDistanceSquared(p.position,entry,People[j].position)<Separation*Separation)free=false;
                if(!free)return;
                Release(i);p.detourSeconds=0;p.activity=Activity.Cross;p.goal=new Vector2(-p.side*7.06f,p.position.y);
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
            for(int look=0;look<3&&p.pathCursor+1<p.pathCount;look++)
            { Vector2 ahead=Routes.Node(p.path[p.pathCursor+1],p.side);if((ahead-p.position).sqrMagnitude>4||!SidewalkRoutes.SegmentAllowed(p.position,ahead))break;p.pathCursor++; }
            if(p.pathCursor<p.pathCount)target=Routes.Node(p.path[p.pathCursor],p.side);
            if(p.routeRepairSeconds<=0&&!SidewalkRoutes.SegmentAllowed(p.position,target))
            {
                Path(p,p.goal);p.routeRepairSeconds=1;
                if(p.pathCursor<p.pathCount)target=Routes.Node(p.path[p.pathCursor],p.side);else target=p.goal;
            }
            // The tight outer tree-bed corridor fits two 0.40m foot streams.
            // Keep right in world travel direction so opposing pedestrians do not share its centre.
            if(Mathf.Abs(p.position.x)>9.86f&&Mathf.Abs(target.x)>9.86f&&Mathf.Abs(target.y-p.position.y)>.5f)
            {
                float stream=p.side*(target.y-p.position.y)>0?10.55f:10.05f;
                Vector2 passing=new Vector2(p.side*stream,target.y);
                if(SidewalkRoutes.SegmentAllowed(p.position,passing))target=passing;
            }
            Move(i,target,false);
            if(Vector2.Distance(p.position,p.goal)<.14f)
            {
                if(p.retiring)return;
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
            Person p=People[i];
            if(!crossing&&p.detourSeconds>0)
            {
                p.detourSeconds-=Dt;
                if(Vector2.Distance(p.position,p.detour)<.10f)p.detourSeconds=0;
                else goal=p.detour;
            }
            Vector2 difference=goal-p.position;float distance=difference.magnitude;if(distance<.001f)return;
            Vector2 forward=difference/distance;float step=Mathf.Min(distance,p.speed*walkingPace*Dt);
            // Prefer each walker's right side. Opposing walkers choose opposite world sides.
            Vector2 right=new Vector2(forward.y,-forward.x);
            if(!crossing&&p.detourSeconds<=0&&!Free(i,p.position+forward*step,false))
            {
                bool personAhead=false;
                for(int j=0;j<People.Length;j++)if(j!=i&&People[j].active&&(People[j].position-p.position).sqrMagnitude<.85f*.85f&&Vector2.Dot(People[j].position-p.position,forward)>0)personAhead=true;
                if(personAhead)for(int side=0;side<2;side++)
                {
                    Vector2 waypoint=p.position+right*(side==0?.65f:-.65f)-forward*.15f;
                    if(!SidewalkRoutes.SegmentAllowed(p.position,waypoint))continue;
                    p.detour=waypoint;p.detourSeconds=3;
                    difference=waypoint-p.position;distance=difference.magnitude;forward=difference/distance;right=new Vector2(forward.y,-forward.x);step=Mathf.Min(distance,p.speed*walkingPace*Dt);break;
                }
            }
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
