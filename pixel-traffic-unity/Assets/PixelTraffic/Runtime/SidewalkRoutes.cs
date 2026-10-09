using UnityEngine;

namespace PixelTraffic.UnityPrototype
{
    // Feet avoid actual 2.2m tree beds and lamp posts. Canopies are visual cover, not foot obstacles.
    public sealed class SidewalkRoutes
    {
        public const float Radius = .16f;
        public const int Columns = 12;
        private const int Rows = 401;
        private readonly bool[] open = new bool[Columns * Rows];
        private readonly int[] seen = new int[Columns * Rows], previous = new int[Columns * Rows], queue = new int[Columns * Rows];
        private int stamp;
        public int Count => open.Length;
        public SidewalkRoutes()
        {
            for(int i=0;i<open.Length;i++)open[i]=Allowed(Node(i,1));
        }
        public Vector2 Node(int index,int side) => new Vector2(side*(6.80f+.355f*(index%Columns)),-28+.45f*(index/Columns));
        public bool IsOpen(int index) => open[index];
        public static bool Allowed(Vector2 p)
        {
            float x=Mathf.Abs(p.x);
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
            for(int i=0;i<open.Length;i++)if(open[i])
            {
                float d=(Node(i,side)-p).sqrMagnitude;
                if(d<best){best=d;result=i;}
            }
            return result;
        }
        public int Find(Vector2 from,Vector2 to,int side,int[] path)
        {
            int start=Nearest(from,side),end=Nearest(to,side);if(start<0||end<0)return 0;
            stamp++;int read=0,write=0;queue[write++]=start;seen[start]=stamp;previous[start]=-1;
            while(read<write&&seen[end]!=stamp)
            {
                int n=queue[read++],col=n%Columns,row=n/Columns;
                void Add(int v)
                {
                    if(open[v]&&seen[v]!=stamp&&SegmentAllowed(Node(n,side),Node(v,side))) {seen[v]=stamp;previous[v]=n;queue[write++]=v;}
                }
                if(col>0)Add(n-1);if(col<Columns-1)Add(n+1);if(row>0)Add(n-Columns);if(row<Rows-1)Add(n+Columns);
            }
            if(seen[end]!=stamp)return 0;
            int count=0;for(int n=end;n>=0&&count<path.Length;n=previous[n])path[count++]=n;
            for(int n=0;n<count/2;n++){int swap=path[n];path[n]=path[count-1-n];path[count-1-n]=swap;}
            return count;
        }
    }
}
