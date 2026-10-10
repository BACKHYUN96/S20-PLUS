using UnityEngine;

namespace PixelTraffic.UnityPrototype
{
    // Feet avoid actual 2.2m tree beds and lamp posts. Canopies are visual cover, not foot obstacles.
    public sealed class SidewalkRoutes
    {
        public const float Radius = .16f;
        public const int Columns = 12;
        private const int Rows = 401;
        private readonly bool[] open = new bool[Columns * Rows*2];
        private readonly int[] seen = new int[Columns * Rows], previous = new int[Columns * Rows], queue = new int[Columns * Rows];
        private readonly byte[] links=new byte[Columns*Rows*2];
        private int stamp;
        public int Count => Columns*Rows;
        public SidewalkRoutes()
        {
            // Stops differ on the two sidewalks; never reuse a mirrored obstacle graph.
            foreach(int side in new[]{-1,1})
            {
                int offset=side<0?0:Count;
                for(int i=0;i<Count;i++)open[offset+i]=Allowed(Node(i,side));
                for(int i=0;i<Count;i++)if(open[offset+i])
                {
                    if(i%Columns<Columns-1&&open[offset+i+1]&&SegmentAllowed(Node(i,side),Node(i+1,side))){links[offset+i]|=2;links[offset+i+1]|=1;}
                    if(i+Columns<Count&&open[offset+i+Columns]&&SegmentAllowed(Node(i,side),Node(i+Columns,side))){links[offset+i]|=8;links[offset+i+Columns]|=4;}
                }
            }
        }
        public Vector2 Node(int index,int side) => new Vector2(side*(6.80f+.355f*(index%Columns)),-28+.45f*(index/Columns));
        public bool IsOpen(int index,int side=1) => open[(side<0?0:Count)+index];
        public static bool Allowed(Vector2 p)
        {
            float x=Mathf.Abs(p.x);
            if(!BusStops.SidewalkOpen(p))return false;
            if(x<6.79f||x>10.75f||p.y< -28.1f||p.y>152.1f)return false;
            for(int i=0;i<16;i++)if(Mathf.Abs(x-8.6f)<1.1f+Radius&&Mathf.Abs(p.y-(-24+i*13))<1.1f+Radius)return false;
            for(int i=0;i<10;i++)if(new Vector2(x-7.1f,p.y-(-15+i*20)).sqrMagnitude<.34f*.34f)return false;
            // Added signal poles stand on the outer sidewalk edge.
            if(new Vector2(x-10.08f,p.y-7.0f).sqrMagnitude<.29f*.29f||new Vector2(x-10.08f,p.y-13.0f).sqrMagnitude<.29f*.29f)return false;
            return true;
        }
        public static bool SegmentAllowed(Vector2 a,Vector2 b)
        {
            int steps=Mathf.Max(1,Mathf.CeilToInt(Vector2.Distance(a,b)/.045f));
            for(int n=1;n<=steps;n++)if(!Allowed(Vector2.Lerp(a,b,(float)n/steps)))return false;
            return true;
        }
        private int Nearest(Vector2 p,int side)
        {
            float best=float.MaxValue;int result=-1;
            for(int i=0;i<Count;i++)if(IsOpen(i,side))
            {
                float d=(Node(i,side)-p).sqrMagnitude;
                if(d<best){best=d;result=i;}
            }
            if(result>=0&&SegmentAllowed(p,Node(result,side)))return result;
            best=float.MaxValue;result=-1;
            for(int i=0;i<Count;i++)if(IsOpen(i,side))
            {
                float d=(Node(i,side)-p).sqrMagnitude;
                if(d<best&&SegmentAllowed(p,Node(i,side))){best=d;result=i;}
            }
            return result;
        }
        public int Find(Vector2 from,Vector2 to,int side,int[] path)
        {
            int start=Nearest(from,side),end=Nearest(to,side);if(start<0||end<0)return 0;
            int offset=side<0?0:Count;stamp++;int read=0,write=0;queue[write++]=start;seen[start]=stamp;previous[start]=-1;
            while(read<write&&seen[end]!=stamp)
            {
                int n=queue[read++];
                void Add(int v)
                {
                    if(seen[v]!=stamp) {seen[v]=stamp;previous[v]=n;queue[write++]=v;}
                }
                if((links[offset+n]&1)!=0)Add(n-1);if((links[offset+n]&2)!=0)Add(n+1);if((links[offset+n]&4)!=0)Add(n-Columns);if((links[offset+n]&8)!=0)Add(n+Columns);
            }
            if(seen[end]!=stamp)return 0;
            int count=0;for(int n=end;n>=0&&count<path.Length;n=previous[n])path[count++]=n;
            for(int n=0;n<count/2;n++){int swap=path[n];path[n]=path[count-1-n];path[count-1-n]=swap;}
            return count;
        }
    }
}
