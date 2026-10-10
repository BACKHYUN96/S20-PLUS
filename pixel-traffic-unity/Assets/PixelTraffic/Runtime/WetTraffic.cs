using UnityEngine;

namespace PixelTraffic.UnityPrototype
{
    // Four reusable billboards per vehicle; stopped cars cannot produce tyre spray.
    public sealed class WetTraffic : MonoBehaviour
    {
        [SerializeField] private MeshFilter pool;
        [SerializeField] private PrototypeDrive[] vehicles;
        private Mesh mesh;
        private Vector3[] vertices;
        private MaterialPropertyBlock block;
        private float clock;
        public int Capacity => 96;
        public int EmittingVehicles { get; private set; }
        public void Configure(MeshFilter filter,PrototypeDrive[] cars){pool=filter;vehicles=cars;}
        public void Advance(float dt,StreetModel model,float rain)
        {
            if(mesh==null){mesh=Instantiate(pool.sharedMesh);pool.sharedMesh=mesh;mesh.MarkDynamic();mesh.bounds=new Bounds(new Vector3(0,1,100),new Vector3(26,6,320));vertices=new Vector3[Capacity*4];block=new MaterialPropertyBlock();}
            clock+=dt;EmittingVehicles=0;
            Vector3 right=Camera.main.transform.right,up=Camera.main.transform.up;
            for(int i=0;i<vehicles.Length;i++)
            {
                var car=model.Cars[i];float gain=car.active&&car.speed>.25f?rain*Mathf.Clamp01(car.speed/6):0;
                if(gain>.01f)EmittingVehicles++;
                for(int q=0;q<4;q++)
                {
                    float t=Mathf.Repeat(clock*2.5f+i*.173f+q*.31f,1);
                    Transform wheel=vehicles[i].Wheels[(q%2)*2];
                    Vector3 center=wheel.position-vehicles[i].transform.forward*(.18f+t*.85f);
                    center.y=.12f+Mathf.Sin(t*Mathf.PI)*.34f;
                    float size=gain*(.07f+.22f*t);Vector3 a=right*size,b=up*size*.65f;
                    int k=(i*4+q)*4;vertices[k]=center-a-b;vertices[k+1]=center+a-b;vertices[k+2]=center+a+b;vertices[k+3]=center-a+b;
                }
            }
            Renderer renderer=pool.GetComponent<Renderer>();renderer.enabled=EmittingVehicles>0;
            if(!renderer.enabled)return;mesh.vertices=vertices;
            block.Clear();block.SetColor("_BaseColor",new Color(.8f,.88f,.94f,.42f*rain));block.SetColor("_EmissionColor",new Color(.07f,.085f,.1f));renderer.SetPropertyBlock(block);
        }
        private void OnDestroy(){if(mesh!=null){if(Application.isPlaying)Destroy(mesh);else DestroyImmediate(mesh);}}
    }
}
