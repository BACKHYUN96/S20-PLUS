using System;
using System.Collections.Generic;
using UnityEngine;

namespace PixelTraffic.UnityPrototype
{
    // Reuses the existing fleet and the street clock. The junction is a one-lane
    // roundabout: each merge yields to committed/circulating cars, with no global lock.
    public sealed class RoundaboutTraffic
    {
        public const float CenterZ=235,Radius=14,IslandRadius=10,OuterRadius=19.5f,GateZ=195;
        public enum Stage { Road,Hidden,Approach,Waiting,Circulating,Exit }
        public sealed class State
        {
            public Stage stage;public int origin,destination,lane,visits,waitTicks;public uint random;
            public float progress,lastTravel,lapLength,loopBefore;public Path path,exitPath;public bool admitted,leaving;
        }
        public sealed class Path
        {
            public readonly Vector2[] points;public readonly float[] distance;
            public readonly float entry,exit,clear;public float Length=>distance[distance.Length-1];
            public Path(List<Vector2> vertices,int entryIndex,int exitIndex,int clearIndex)
            {
                points=vertices.ToArray();distance=new float[points.Length];
                for(int i=1;i<points.Length;i++)distance[i]=distance[i-1]+Vector2.Distance(points[i],points[i-1]);
                entry=distance[entryIndex];exit=distance[exitIndex];clear=distance[clearIndex];
            }
            public Vector2 Pose(float s,out float yaw)
            {
                int low=0,high=points.Length-1;while(high-low>1){int mid=(low+high)/2;if(distance[mid]<=s)low=mid;else high=mid;}
                float t=Mathf.InverseLerp(distance[low],distance[high],s);
                Vector2 a=(points[high]-points[low]).normalized;
                Vector2 b=low>0?(points[high]-points[low-1]).normalized:a;
                Vector2 c=high+1<points.Length?(points[high+1]-points[low]).normalized:a;
                Vector2 tangent=Vector2.Lerp(b,c,t).normalized;yaw=Mathf.Atan2(tangent.x,tangent.y)*Mathf.Rad2Deg;
                return Vector2.Lerp(points[low],points[high],t);
            }
        }
        readonly StreetModel model;public readonly State[] States;
        readonly Dictionary<int,Path> paths=new Dictionary<int,Path>();
        uint dispatchRandom=47639;readonly int[] exitBag={0,1,2,3};int exitCursor=4;
        public int Owner {get {for(int i=0;i<States.Length;i++)if(States[i].admitted)return i;return -1;}}
        public int Entries{get;private set;}public int Completed{get;private set;}public int Yields{get;private set;}public int Returns{get;private set;}public int Recycles{get;private set;}
        public readonly int[] Destinations=new int[4];
        public bool MaintainFive {get;set;}
        public const int OccupancyTarget=5;
        public int ExtraLaps{get;private set;}
        public int RingCount {get{int count=0;for(int i=0;i<States.Length;i++){var c=model.Cars[i];var s=States[i];if(c.active&&!c.roundHidden&&c.roundabout&&s.path!=null&&s.progress>=s.path.entry&&s.progress<s.path.exit)count++;}return count;}}
        public RoundaboutTraffic(StreetModel street,bool maintainFive=false)
        {
            model=street;MaintainFive=maintainFive;States=new State[model.Cars.Length];
            for(int i=0;i<States.Length;i++)
            {
                var c=model.Cars[i];States[i]=new State{random=(uint)(17239+i*1297),stage=Stage.Road};
                if(c.z>GateZ){c.roundabout=true;c.roundHidden=true;c.roundPosition=new Vector2(-90,CenterZ);c.z=CenterZ;States[i].stage=Stage.Hidden;States[i].origin=1+i%3;}
            }
            if(MaintainFive)SeedFive();
        }
        void SeedFive()
        {
            var candidates=new List<int>();for(int i=0;i<model.Cars.Length;i++)if(model.Cars[i].active)candidates.Add(i);
            candidates.Sort((a,b)=>{int order=model.Cars[b].z.CompareTo(model.Cars[a].z);return order!=0?order:a.CompareTo(b);});
            for(int n=0;n<Mathf.Min(OccupancyTarget,candidates.Count);n++)
            {
                int i=candidates[n];var c=model.Cars[i];var state=States[i];
                state.origin=0;state.destination=Destination();state.lane=c.bus?0:n%2;
                SetPath(state,Route(0,state.destination,state.destination==0?state.lane:3));
                state.progress=state.path.entry+n*(Mathf.PI*2*Radius/OccupancyTarget);state.admitted=true;state.stage=Stage.Circulating;state.visits++;
                c.lane=3;c.roundabout=true;c.roundHidden=false;c.speed=4.8f;c.roundPosition=state.path.Pose(state.progress,out c.roundYaw);c.z=c.roundPosition.y;state.lastTravel=4.8f*StreetModel.Dt;
            }
        }
        void SetPath(State s,Path path)
        {
            s.path=path;s.exitPath=path;s.leaving=false;s.lapLength=0;s.loopBefore=0;
            if(!MaintainFive)return;
            var list=new List<Vector2>();int first=Array.BinarySearch(path.distance,path.entry),last=Array.BinarySearch(path.distance,path.exit),clear=Array.BinarySearch(path.distance,path.clear);
            for(int n=0;n<=last;n++)list.Add(path.points[n]);
            var p=path.points[last]-new Vector2(0,CenterZ);float angle=Mathf.Atan2(p.y,p.x);
            for(int n=1;n<=180;n++)list.Add(Ring(angle+Mathf.PI*2*n/180));
            for(int n=last+1;n<path.points.Length;n++)list.Add(path.points[n]);
            s.path=new Path(list,first,last+180,clear+180);s.lapLength=s.path.exit-path.exit;
            s.loopBefore=Mathf.Min(12,path.exit-path.entry-1);
        }
        float PlannedProgress(State s,float progress)
        {
            if(MaintainFive&&s.lapLength>0&&!s.leaving)
                while(progress>=s.path.exit-s.loopBefore)progress-=s.lapLength;
            return progress;
        }
        bool MayLeave(int index)
        {
            int circulating=RingCount,leaving=0;
            for(int j=0;j<States.Length;j++)
            {
                var s=States[j];var c=model.Cars[j];if(j==index||!c.active||c.roundHidden||!c.roundabout||s.path==null)continue;
                if(s.leaving&&s.progress<s.path.exit)leaving++;
            }
            return circulating-leaving>OccupancyTarget;
        }
        static int Random(ref uint seed,int range){uint n=seed;n^=n<<13;n^=n>>17;n^=n<<5;seed=n;return (int)(n%(uint)range);}
        static int Random(State s,int range)=>Random(ref s.random,range);
        int Destination()
        {
            if(exitCursor==4){for(int i=3;i>0;i--){int j=Random(ref dispatchRandom,i+1);int value=exitBag[i];exitBag[i]=exitBag[j];exitBag[j]=value;}exitCursor=0;}
            return exitBag[exitCursor++];
        }
        public bool Busy(StreetModel.Car c)=>c.roundabout;
        public static Vector2 Position(StreetModel.Car c)=>new Vector2(c.X,(float)c.z);
        public static float Yaw(StreetModel.Car c)=>c.roundabout?c.roundYaw:(c.Direction<0?180:0)+LaneChanges.Yaw(c);
        static Vector2 Rotate(Vector2 p,int port)
        {switch(port&3){case 1:return new Vector2(-p.y,p.x);case 2:return -p;case 3:return new Vector2(p.y,-p.x);default:return p;}}
        static Vector2 World(Vector2 p,int port)=>Rotate(p,port)+new Vector2(0,CenterZ);
        static Vector2 Ring(float angle)=>new Vector2(Mathf.Cos(angle)*Radius,CenterZ+Mathf.Sin(angle)*Radius);
        static Vector2 Tangent(float angle)=>new Vector2(-Mathf.Sin(angle),Mathf.Cos(angle));
        static void Bezier(List<Vector2> p,Vector2 a,Vector2 b,Vector2 c,Vector2 d,int steps=32)
        {if(p.Count==0)p.Add(a);for(int i=1;i<=steps;i++){float t=i/(float)steps,u=1-t;p.Add(u*u*u*a+3*u*u*t*b+3*u*t*t*c+t*t*t*d);}}
        public Path Route(int origin,int destination,int lane)
        {
            int key=origin*32+destination*4+lane;if(paths.TryGetValue(key,out var result))return result;
            float x=origin==0?(lane-1.5f)*StarterConfig.LaneWidth:4.8f;
            var p=new List<Vector2>();Vector2 gate=World(new Vector2(x,-40),origin);
            if(origin==1||origin==3)
            {
                Vector2 start=World(new Vector2(4.8f,-140),origin);Bezier(p,start,World(new Vector2(4.8f,-100),origin),World(new Vector2(4.8f,-60),origin),gate);
            }
            else if(origin==2)Bezier(p,new Vector2(-140,302),new Vector2(-80,302),new Vector2(-4.8f,295),gate);
            else p.Add(gate);
            float entryAngle=(-45+origin*90)*Mathf.Deg2Rad;Vector2 entry=Ring(entryAngle);
            if(origin==0)
            {
                Vector2 straight=World(new Vector2(x,-22),origin);
                Bezier(p,gate,World(new Vector2(x,-34),origin),World(new Vector2(x,-28),origin),straight,18);
                Bezier(p,straight,World(new Vector2(x,-19),origin),entry-Tangent(entryAngle)*7,entry);
            }
            else Bezier(p,gate,World(new Vector2(x,-30),origin),entry-Tangent(entryAngle)*7,entry);
            int entryIndex=p.Count-1;
            float exitAngle=(-105+destination*90)*Mathf.Deg2Rad;while(exitAngle<entryAngle+.01f)exitAngle+=Mathf.PI*2;
            int arcSteps=Mathf.RoundToInt((exitAngle-entryAngle)*Mathf.Rad2Deg/2);
            for(int i=1;i<=arcSteps;i++)p.Add(Ring(Mathf.Lerp(entryAngle,exitAngle,i/(float)arcSteps)));
            int exitIndex=p.Count-1;Vector2 end;
            float outX=destination==0?(lane-1.5f)*StarterConfig.LaneWidth:-4.8f;
            end=World(new Vector2(outX,-40),destination);
            if(destination==0)
            {
                // Align with the outgoing lane before reaching queued oncoming buses.
                // A diagonal wide truck near the gate can otherwise block both directions.
                Vector2 aligned=World(new Vector2(outX,-22),destination);
                Bezier(p,Ring(exitAngle),Ring(exitAngle)+Tangent(exitAngle)*3,World(new Vector2(outX,-18),destination),aligned);
                Bezier(p,aligned,World(new Vector2(outX,-28),destination),World(new Vector2(outX,-34),destination),end,18);
            }
            else Bezier(p,Ring(exitAngle),Ring(exitAngle)+Tangent(exitAngle)*3,World(new Vector2(outX,-30),destination),end);
            int clearIndex=p.Count-1;
            if(destination==1||destination==3)Bezier(p,end,World(new Vector2(-4.8f,-60),destination),World(new Vector2(-4.8f,-100),destination),World(new Vector2(-4.8f,-140),destination));
            else if(destination==2)Bezier(p,end,new Vector2(4.8f,295),new Vector2(80,302),new Vector2(140,302));
            result=new Path(p,entryIndex,exitIndex,clearIndex);paths.Add(key,result);return result;
        }
        // Expanded oriented rectangles test both longitudinal clearance and body width.
        // Four independent separating axes cover arbitrary heading and long buses.
        public static bool Separated(Vector2 a,float yawA,float lengthA,float widthA,Vector2 b,float yawB,float lengthB,float widthB,float margin=1.8f)
        {
            float ra=yawA*Mathf.Deg2Rad,rb=yawB*Mathf.Deg2Rad;
            Vector2 fa=new Vector2(Mathf.Sin(ra),Mathf.Cos(ra)),sa=new Vector2(fa.y,-fa.x);
            Vector2 fb=new Vector2(Mathf.Sin(rb),Mathf.Cos(rb)),sb=new Vector2(fb.y,-fb.x),d=b-a;
            float la=(lengthA+margin)*.5f,lb=(lengthB+margin)*.5f,wa=(widthA+.08f)*.5f,wb=(widthB+.08f)*.5f;
            return Axis(d,fa,fa,sa,fb,sb,la,wa,lb,wb)||Axis(d,sa,fa,sa,fb,sb,la,wa,lb,wb)||Axis(d,fb,fa,sa,fb,sb,la,wa,lb,wb)||Axis(d,sb,fa,sa,fb,sb,la,wa,lb,wb);
        }
        static bool Axis(Vector2 d,Vector2 axis,Vector2 fa,Vector2 sa,Vector2 fb,Vector2 sb,float la,float wa,float lb,float wb)
        =>Mathf.Abs(Vector2.Dot(d,axis))>=Mathf.Abs(Vector2.Dot(fa,axis))*la+Mathf.Abs(Vector2.Dot(sa,axis))*wa+Mathf.Abs(Vector2.Dot(fb,axis))*lb+Mathf.Abs(Vector2.Dot(sb,axis))*wb-.0001f;
        // Exact rotated bodies already include yaw. Keep a small physical buffer;
        // the larger road SafetyLength remains at ordinary road handoffs.
        int PhysicalBlocker(int index,Vector2 point,float yaw)
        {
            var c=model.Cars[index];for(int j=0;j<model.Cars.Length;j++)
            {var other=model.Cars[j];if(j==index||!other.active||other.roundHidden)continue;if(!Separated(point,yaw,c.length,c.width,Position(other),Yaw(other),other.length,other.width,.15f))return j;}
            return -1;
        }
        bool RoadSpawnFree(int index,Vector2 point,float yaw=0,bool roadOnly=false)
        {
            var car=model.Cars[index];
            for(int j=0;j<model.Cars.Length;j++)
            {
                var other=model.Cars[j];if(j==index||!other.active||other.roundHidden||(roadOnly&&other.roundabout))continue;
                if(!Separated(point,yaw,car.SafetyLength,car.width,Position(other),Yaw(other),other.SafetyLength,other.width,2.2f))return false;
            }
            return true;
        }
        public int Blocker(int index,Vector2 point,float yaw)=>PhysicalBlocker(index,point,yaw);
        // Only the local merge is planned. Occupied remote exit curves must not
        // reserve a whole roundabout; a circulating queue may safely follow inside.
        bool Free(int index,Vector2 point,float yaw)=>PhysicalBlocker(index,point,yaw)<0;
        static float ProjectedTravel(float speed,float pace,float time)
        {
            float ramp=Mathf.Min(time,Mathf.Max(0,(pace-speed)/1.8f));
            return speed*ramp+.9f*ramp*ramp+pace*(time-ramp);
        }
        static float ArrivalTime(float distance,float speed,float pace)
        {
            float ramp=Mathf.Max(0,(pace-speed)/1.8f),rampDistance=(pace*pace-speed*speed)/3.6f;
            return distance<=rampDistance?(Mathf.Sqrt(speed*speed+3.6f*distance)-speed)/1.8f:ramp+(distance-rampDistance)/pace;
        }
        public int AdmissionBlocker(int index,out float blockedAt)
        {
            var state=States[index];var own=model.Cars[index];blockedAt=state.progress;
            float pace=4.8f*model.DrivingPace;
            if(MaintainFive)
            {
                int committed=RingCount;
                for(int j=0;j<States.Length;j++){var s=States[j];var c=model.Cars[j];if(j!=index&&c.active&&!c.roundHidden&&c.roundabout&&s.path!=null&&s.admitted&&s.progress<s.path.entry)committed++;}
                if(committed>=OccupancyTarget+1)return index;
            }
            for(int j=0;j<States.Length;j++)
            {
                var ahead=States[j];var car=model.Cars[j];
                if(j==index||!car.active||car.roundHidden||!car.roundabout||ahead.path==null)continue;
                if(ahead.origin==state.origin&&ahead.progress<ahead.path.entry+car.length+1.2f&&
                    ahead.path.entry-ahead.progress<state.path.entry-state.progress-.01f)return j;
            }
            float end=Mathf.Min(state.path.Length,state.path.entry+own.length*.5f+2);
            for(int j=0;j<States.Length;j++)
            {
                var route=States[j];var other=model.Cars[j];
                if(j==index||!other.active||other.roundHidden||!route.admitted||route.path==null)continue;
                // Protect the immediate exit turn against an approaching nose.
                // A wide truck may slow here; projecting it at cruising speed
                // must not allow the approach to pin its tail against the ring.
                if((!MaintainFive||route.leaving)&&route.destination==0&&route.progress>=route.path.exit-12&&route.progress<route.path.exit+12)
                {
                    for(float a=state.progress;a<=end;a+=.75f)
                    {
                        Vector2 p=state.path.Pose(a,out float yaw);
                        for(float b=route.progress;b<=Mathf.Min(route.path.clear,route.progress+12);b+=1)
                        {
                            Vector2 q=route.path.Pose(b,out float heading);
                            if(!Separated(p,yaw,own.length,own.width,q,heading,other.length,other.width,.8f)){blockedAt=a;return j;}
                        }
                    }
                }
                for(float d=state.progress;d<=end;d+=.75f)
                {
                    float time=ArrivalTime(d-state.progress,Mathf.Min(own.speed,pace),pace);
                    float travel=route.lastTravel>.00001f?ProjectedTravel(Mathf.Min(other.speed,pace),pace,time):0;
                    Vector2 p=state.path.Pose(d,out float yaw),q=route.path.Pose(Mathf.Min(route.path.Length,PlannedProgress(route,route.progress+travel)),out float heading);
                    if(!Separated(p,yaw,own.length,own.width,q,heading,other.length,other.width,1.2f))
                    {blockedAt=d;return j;}
                }
            }
            return -1;
        }
        float FollowingRoom(int index)
        {
            var state=States[index];var own=model.Cars[index];float room=float.MaxValue;
            if(state.progress>=state.path.entry&&state.progress<state.path.exit)
            {
                var point=Position(own)-new Vector2(0,CenterZ);float angle=Mathf.Atan2(point.y,point.x);
                for(int j=0;j<States.Length;j++)
                {
                    var route=States[j];var other=model.Cars[j];
                    if(j==index||!other.active||other.roundHidden||!other.roundabout||route.path==null||route.progress<route.path.entry||route.progress>route.path.exit)continue;
                    var q=Position(other)-new Vector2(0,CenterZ);float delta=Mathf.Atan2(q.y,q.x)-angle;
                    while(delta<0)delta+=Mathf.PI*2;
                    if(delta<Mathf.PI)room=Mathf.Min(room,delta*Radius-(own.length+other.length)*.5f-1.2f);
                }
            }
            float look=Mathf.Max(3,own.speed*own.speed/6.8f+own.speed*.5f+2);
            float heading=Yaw(own)*Mathf.Deg2Rad;var forward=new Vector2(Mathf.Sin(heading),Mathf.Cos(heading));
            for(float d=.75f;d<=look;d+=.75f)
            {
                Vector2 p=state.path.Pose(Mathf.Min(state.path.Length,PlannedProgress(state,state.progress+d)),out float yaw);
                for(int j=0;j<model.Cars.Length;j++)
                {
                    var other=model.Cars[j];if(j==index||!other.active||other.roundHidden)continue;
                    var route=States[j];
                    bool priority=state.progress>=state.path.entry;
                    bool otherApproach=other.roundabout&&route.path!=null&&route.progress<route.path.entry;
                    if(priority&&otherApproach&&(!MaintainFive||!route.admitted||state.leaving))continue; // entering traffic yields; let the ring/exit clear first
                    Vector2 q=Position(other);if(Vector2.Dot(q-Position(own),forward)<-.2f)continue;
                    if(!Separated(p,yaw,own.length,own.width,q,Yaw(other),other.length,other.width,1.2f))return Mathf.Max(0,Mathf.Min(room,d-.75f));
                }
            }
            return Mathf.Max(0,room);
        }
        bool ClearRoute(int index,State state)=>AdmissionBlocker(index,out _) < 0;
        void Start(int i,int origin)
        {
            var s=States[i];var c=model.Cars[i];s.origin=origin;
            if(s.path==null)
            {
                // Only consume an exit choice when the road-to-path handoff can really start.
                if(origin==0){Vector2 gate=Route(0,1,c.lane).Pose(0,out float direction);if(!Free(i,gate,direction))return;}
                s.destination=origin==0?Destination():0;s.lane=s.destination==0?(c.bus?0:Random(s,2)):c.lane;
                SetPath(s,origin==0&&s.destination==0?UTurn(c.lane,s.lane):Route(origin,s.destination,s.destination==0?s.lane:c.lane));
            }
            var path=s.path;
            Vector2 point=path.Pose(0,out float yaw);if(!Free(i,point,yaw))return;
            float progress=origin==0?Mathf.Max(0,(float)c.z-GateZ):0;
            point=path.Pose(progress,out yaw);if(!Free(i,point,yaw))return;
            s.path=path;s.progress=progress;s.stage=Stage.Approach;s.waitTicks=0;s.visits++;s.admitted=false;
            c.roundabout=true;c.roundHidden=false;c.roundPosition=point;c.roundYaw=yaw;c.z=point.y;c.roundSignal=false;c.roundSignalTicks=0;c.speed=Mathf.Min(c.speed,4.8f);c.committed=false;
            model.Maneuvers.Cancel(c,model.ActiveTicks);if(origin==0)Destinations[s.destination]++;
        }
        Path UTurn(int incoming,int outgoing)
        {
            int key=200+incoming*4+outgoing;if(paths.TryGetValue(key,out var p))return p;
            var entry=Route(0,1,incoming);var exit=Route(2,0,outgoing);var list=new List<Vector2>();
            for(int i=0;i<=Array.BinarySearch(entry.distance,entry.entry);i++)list.Add(entry.points[i]);
            int first=list.Count-1;float a=-45*Mathf.Deg2Rad,b=255*Mathf.Deg2Rad;
            for(int i=1;i<=150;i++)list.Add(Ring(Mathf.Lerp(a,b,i/150f)));
            int last=list.Count-1;int n=Array.BinarySearch(exit.distance,exit.exit);
            for(int i=n+1;i<exit.points.Length;i++)list.Add(exit.points[i]);
            p=new Path(list,first,last,list.Count-1);paths.Add(key,p);return p;
        }
        public float RoadRoom(StreetModel.Car c)
        {
            // Leave braking room beyond the handoff; it is not a mandatory stop line.
            // Start preserves the small per-tick overshoot on the identical straight path.
            float room=c.Direction>0?((float)c.z<GateZ?GateZ-(float)c.z+4.8f*4.8f/6.8f:GateZ+.5f-(float)c.z):(float)c.z-StarterConfig.RouteStart;
            foreach(var other in model.Cars)
            {
                if(!other.active||!other.roundabout||other.roundHidden)continue;
                float angle=other.roundYaw*Mathf.Deg2Rad;
                float wide=other.width*Mathf.Abs(Mathf.Cos(angle))+other.SafetyLength*Mathf.Abs(Mathf.Sin(angle));
                float length=other.SafetyLength*Mathf.Abs(Mathf.Cos(angle))+other.width*Mathf.Abs(Mathf.Sin(angle));
                float ahead=c.Direction*((float)other.z-(float)c.z);
                if(ahead>=0&&Mathf.Abs(c.X-other.X)<(c.width+wide)*.5f+.05f)room=Mathf.Min(room,ahead-(c.SafetyLength+length)*.5f-2.2f);
            }
            return Mathf.Max(0,room);
        }
        public void Tick()
        {
            for(int pass=0;pass<States.Length;pass++)
            {
                int i=(pass+model.ActiveTicks/30)%States.Length;var s=States[i];var c=model.Cars[i];if(!c.active)continue;
                if(s.stage==Stage.Road)
                {
                    if(c.Direction>0&&c.z>=GateZ-.001f&&c.maneuver==LaneChanges.Stage.Idle&&!model.Stops.Holds(c))Start(i,0);
                    else if(c.Direction<0&&c.z<=StarterConfig.RouteStart+.001f)
                    {
                        int lane=c.bus?3:2+(i+s.visits)%2;Vector2 p=new Vector2((lane-1.5f)*StarterConfig.LaneWidth,StarterConfig.RouteStart);
                        if(!RoadSpawnFree(i,p))continue;c.lane=lane;c.z=StarterConfig.RouteStart;c.speed=0;c.committed=false;model.Maneuvers.Cancel(c,model.ActiveTicks);model.Stops.Wrapped(c);
                    }
                    continue;
                }
                if(s.stage==Stage.Hidden){Start(i,s.origin);continue;}
                // Approach the yield line; each entry reserves only its merge curve.
                // Stagger the inner queue outside the outer bus's turning tail.
                float hold=s.origin==0&&c.lane==2?0:Mathf.Max(0,s.path.entry-22);
                float limit=s.path.Length;
                if(!s.admitted&&s.progress<s.path.entry)
                {
                    if(s.progress>=hold-(c.speed*c.speed/6.8f+c.speed*.5f+1)&&ClearRoute(i,s)){s.admitted=true;Entries++;s.stage=Stage.Circulating;}
                    else {limit=Mathf.Min(limit,hold);s.stage=Stage.Waiting;s.waitTicks++;Yields++;}
                }
                if(MaintainFive&&s.lapLength>0&&!s.leaving)
                {
                    bool firstPass=s.progress<s.exitPath.exit-.001f;float exit=firstPass?s.exitPath.exit:s.path.exit;
                    if(s.progress>=exit-16&&MayLeave(i))
                    {
                        s.leaving=true;
                        if(firstPass){s.path=s.exitPath;s.lapLength=0;s.loopBefore=0;}
                    }
                }
                if((!MaintainFive||s.leaving)&&s.progress>=s.path.exit-12)
                {if(!c.roundSignal)c.roundSignalTicks=0;c.roundSignal=true;c.roundSignalTicks++;}
                if(c.roundSignal&&c.roundSignalTicks<54)limit=Mathf.Min(limit,s.path.exit);
                // A rolling handoff can already be beyond a hold point; yielding
                // must keep that pose, never clamp progress backwards.
                limit=Mathf.Max(s.progress,limit);
                float pace=(s.progress<s.path.entry-45||s.progress>s.path.clear+8)?Mathf.Max(c.cruise,4.8f):4.8f;
                float room=Mathf.Min(Mathf.Max(0,limit-s.progress),FollowingRoom(i)),target=Mathf.Min(pace*model.DrivingPace,Mathf.Sqrt(6.8f*room));float previous=c.speed;
                c.speed=Mathf.MoveTowards(c.speed,target,(target>c.speed?1.8f:3.4f)*StreetModel.Dt);
                float travel=Mathf.Min(c.speed*StreetModel.Dt,room),next=Mathf.Min(s.progress+travel,limit);
                float planned=PlannedProgress(s,next);if(planned<next){next=planned;ExtraLaps++;}
                Vector2 nextPoint=s.path.Pose(next,out float nextYaw);
                bool blocked=!Free(i,nextPoint,nextYaw);
                if(blocked){travel=0;next=s.progress;nextPoint=s.path.Pose(next,out nextYaw);c.speed=0;s.waitTicks++;}
                s.progress=next;s.lastTravel=travel;c.roundPosition=nextPoint;c.roundYaw=nextYaw;c.z=nextPoint.y;c.distance=travel;c.totalDistance+=travel;c.braking=previous>c.speed+.015f||(c.speed<.1f&&(room<.3f||blocked));
                if(s.progress>=s.path.exit+.001f)s.stage=Stage.Exit;
                if(s.admitted&&s.progress>=s.path.clear-.001f){s.admitted=false;Completed++;}
                if(s.progress<s.path.Length-.001f)continue;
                if(s.destination==0)
                {
                    if(!RoadSpawnFree(i,c.roundPosition,180,true))continue;
                    c.lane=s.lane;c.roundabout=false;c.roundSignal=false;c.roundHidden=false;c.z=GateZ;c.busOffset=0;s.stage=Stage.Road;s.path=null;Returns++;model.Stops.Wrapped(c);
                }
                else
                {
                    // East/west endpoints and the river-road bend are beyond the saved portrait frustum.
                    s.stage=Stage.Hidden;s.path=null;s.origin=s.destination;c.roundHidden=true;c.speed=0;c.roundSignal=false;Recycles++;
                }
            }
        }
    }
}
