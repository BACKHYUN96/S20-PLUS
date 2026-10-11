using System;
using System.Collections.Generic;
using UnityEngine;

namespace PixelTraffic.UnityPrototype
{
    // Reuses the existing fleet and the street clock. The junction is a one-lane
    // roundabout: circulating traffic holds the reservation until its tail clears.
    public sealed class RoundaboutTraffic
    {
        public const float CenterZ=235,Radius=14,IslandRadius=10,OuterRadius=19.5f,GateZ=195;
        public enum Stage { Road,Hidden,Approach,Waiting,Circulating,Exit }
        public sealed class State
        {
            public Stage stage;public int origin,destination,lane,visits,waitTicks;public uint random;
            public float progress;public Path path;
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
        int owner=-1;public int Owner=>owner;
        public int Entries{get;private set;}public int Completed{get;private set;}public int Yields{get;private set;}public int Returns{get;private set;}public int Recycles{get;private set;}
        public readonly int[] Destinations=new int[4];
        public RoundaboutTraffic(StreetModel street)
        {
            model=street;States=new State[model.Cars.Length];
            for(int i=0;i<States.Length;i++)
            {
                var c=model.Cars[i];States[i]=new State{random=(uint)(17239+i*1297),stage=Stage.Road};
                if(c.z>GateZ){c.roundabout=true;c.roundHidden=true;c.roundPosition=new Vector2(-90,CenterZ);c.z=CenterZ;States[i].stage=Stage.Hidden;States[i].origin=1+i%3;}
            }
        }
        static int Random(State s,int range){uint n=s.random;n^=n<<13;n^=n>>17;n^=n<<5;s.random=n;return (int)(n%(uint)range);}
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
            Bezier(p,gate,World(new Vector2(x,-30),origin),entry-Tangent(entryAngle)*7,entry);
            int entryIndex=p.Count-1;
            float exitAngle=(-105+destination*90)*Mathf.Deg2Rad;while(exitAngle<entryAngle+.01f)exitAngle+=Mathf.PI*2;
            int arcSteps=Mathf.CeilToInt((exitAngle-entryAngle)*Mathf.Rad2Deg/2);
            for(int i=1;i<=arcSteps;i++)p.Add(Ring(Mathf.Lerp(entryAngle,exitAngle,i/(float)arcSteps)));
            int exitIndex=p.Count-1;Vector2 end;
            float outX=destination==0?(lane-1.5f)*StarterConfig.LaneWidth:-4.8f;
            end=World(new Vector2(outX,-40),destination);
            Bezier(p,Ring(exitAngle),Ring(exitAngle)+Tangent(exitAngle)*3,World(new Vector2(outX,-30),destination),end);
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
        public int Blocker(int index,Vector2 point,float yaw)
        {
            var c=model.Cars[index];for(int j=0;j<model.Cars.Length;j++)
            {var other=model.Cars[j];if(j==index||!other.active||other.roundHidden)continue;if(!Separated(point,yaw,c.SafetyLength,c.width,Position(other),Yaw(other),other.SafetyLength,other.width))return j;}
            return ReservationBlocker(index,point,yaw);
        }
        int ReservationBlocker(int index,Vector2 point,float yaw)
        {
            if(owner<0||owner==index)return -1;var own=model.Cars[owner];var route=States[owner];var c=model.Cars[index];
            for(float d=route.progress;d<=route.path.clear;d+=.75f)
            {Vector2 p=route.path.Pose(d,out float heading);if(!Separated(point,yaw,c.SafetyLength,c.width,p,heading,own.SafetyLength,own.width))return owner;}
            Vector2 end=route.path.Pose(route.path.clear,out float angle);return Separated(point,yaw,c.SafetyLength,c.width,end,angle,own.SafetyLength,own.width)?-1:owner;
        }
        bool Free(int index,Vector2 point,float yaw)=>Blocker(index,point,yaw)<0;
        public int AdmissionBlocker(int index,out float blockedAt)
        {
            var state=States[index];blockedAt=state.progress;
            for(float d=state.progress;d<state.path.clear;d+=.75f)
            {Vector2 p=state.path.Pose(d,out float yaw);int blocker=Blocker(index,p,yaw);if(blocker>=0){blockedAt=d;return blocker;}}
            Vector2 end=state.path.Pose(state.path.clear,out float heading);blockedAt=state.path.clear;return Blocker(index,end,heading);
        }
        bool ClearRoute(int index,State state)=>AdmissionBlocker(index,out _) < 0;
        void Start(int i,int origin)
        {
            var s=States[i];var c=model.Cars[i];s.origin=origin;
            if(s.path==null)
            {
                s.destination=origin==0?Random(s,4):0;s.lane=s.destination==0?(c.bus?0:Random(s,2)):c.lane;
                s.path=origin==0&&s.destination==0?UTurn(c.lane,s.lane):Route(origin,s.destination,s.destination==0?s.lane:c.lane);
            }
            var path=s.path;
            Vector2 point=path.Pose(0,out float yaw);if(!Free(i,point,yaw))return;
            s.path=path;s.progress=0;s.stage=Stage.Approach;s.waitTicks=0;s.visits++;
            c.roundabout=true;c.roundHidden=false;c.roundPosition=point;c.roundYaw=yaw;c.z=point.y;c.roundSignal=false;c.roundSignalTicks=0;c.speed=Mathf.Min(c.speed,3.8f);c.committed=false;
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
            float room=c.Direction>0?GateZ-(float)c.z:(float)c.z-StarterConfig.RouteStart;
            foreach(var other in model.Cars)
            {
                if(!other.active||!other.roundabout||other.roundHidden)continue;
                float angle=other.roundYaw*Mathf.Deg2Rad;
                float wide=other.width*Mathf.Abs(Mathf.Cos(angle))+other.length*Mathf.Abs(Mathf.Sin(angle));
                float length=other.length*Mathf.Abs(Mathf.Cos(angle))+other.width*Mathf.Abs(Mathf.Sin(angle));
                float ahead=c.Direction*((float)other.z-(float)c.z);
                if(ahead>=0&&Mathf.Abs(c.X-other.X)<(c.width+wide)*.5f+.05f)room=Mathf.Min(room,ahead-(c.SafetyLength+length)*.5f-1.8f);
            }
            if(owner>=0)
            {int index=Array.IndexOf(model.Cars,c);Vector2 next=new Vector2(c.X,(float)c.z+c.Direction*Mathf.Min(Mathf.Max(0,room),c.cruise*StreetModel.Dt));if(ReservationBlocker(index,next,Yaw(c))>=0)room=0;}
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
                        if(!Free(i,p,0))continue;c.lane=lane;c.z=StarterConfig.RouteStart;c.speed=0;c.committed=false;model.Maneuvers.Cancel(c,model.ActiveTicks);model.Stops.Wrapped(c);
                    }
                    continue;
                }
                if(s.stage==Stage.Hidden){Start(i,s.origin);continue;}
                // Reserve before approach lanes converge; keep the entire route clear.
                float hold=Mathf.Max(0,s.path.entry-32);
                float limit=s.path.Length;
                if(s.progress<s.path.entry&&owner!=i)
                {
                    if(owner>=0&&owner!=i){limit=hold;s.stage=Stage.Waiting;s.waitTicks++;Yields++;}
                    else if(s.progress>=hold-.001f&&ClearRoute(i,s)){owner=i;Entries++;s.stage=Stage.Circulating;}
                    else limit=Mathf.Min(limit,hold);
                }
                if(s.progress>=s.path.exit-12)
                {if(!c.roundSignal)c.roundSignalTicks=0;c.roundSignal=true;c.roundSignalTicks++;}
                if(c.roundSignal&&c.roundSignalTicks<54)limit=Mathf.Min(limit,s.path.exit);
                float room=Mathf.Max(0,limit-s.progress),target=Mathf.Min(3.8f*model.DrivingPace,Mathf.Sqrt(6.8f*room));float previous=c.speed;
                c.speed=Mathf.MoveTowards(c.speed,target,(target>c.speed?1.8f:3.4f)*StreetModel.Dt);
                float travel=Mathf.Min(c.speed*StreetModel.Dt,room),next=Mathf.Min(s.progress+travel,limit);
                Vector2 nextPoint=s.path.Pose(next,out float nextYaw);
                if(!Free(i,nextPoint,nextYaw)){travel=0;next=s.progress;nextPoint=s.path.Pose(next,out nextYaw);c.speed=0;s.waitTicks++;}
                s.progress=next;c.roundPosition=nextPoint;c.roundYaw=nextYaw;c.z=nextPoint.y;c.distance=travel;c.totalDistance+=travel;c.braking=previous>c.speed+.015f||(c.speed<.1f&&room<.3f);
                if(s.progress>=s.path.exit+.001f)s.stage=Stage.Exit;
                if(owner==i&&s.progress>=s.path.clear-.001f){owner=-1;Completed++;}
                if(s.progress<s.path.Length-.001f)continue;
                if(s.destination==0)
                {
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
