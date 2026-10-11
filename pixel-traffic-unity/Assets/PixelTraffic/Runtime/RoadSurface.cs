using UnityEngine;

namespace PixelTraffic.UnityPrototype
{
    // One reusable world-space mesh for the whole fleet. No per-car renderer,
    // material, light or projection camera; UVs and indices never change.
    public sealed class RoadSurface : MonoBehaviour
    {
        [SerializeField] private MeshFilter fleet;
        [SerializeField] private Renderer[] views;
        [SerializeField] private Material source;
        [SerializeField] private PrototypeDrive[] cars;
        private Mesh mesh;
        private Material material;
        private Vector3[] vertices;
        private Color[] colours;
        private CityClimate climate;
        private bool paused, focused=true;
        public Mesh RuntimeMesh => mesh;
        public Material RuntimeMaterial => material;
        public int VehicleCount => cars.Length;
        public void Configure(MeshFilter filter,Renderer[] renderers,Material shared,PrototypeDrive[] vehicles)
        {fleet=filter;views=renderers;source=shared;cars=vehicles;}
        public void Sync(bool active)
        {
            if(!active)return;
            if(mesh==null)
            {
                mesh=Instantiate(fleet.sharedMesh);mesh.name="Fleet Ground Runtime";mesh.MarkDynamic();
                fleet.sharedMesh=mesh;vertices=mesh.vertices;colours=mesh.colors;
                material=new Material(source){name="Ground Overlay Runtime"};
                foreach(var view in views)view.sharedMaterial=material;
                climate=FindFirstObjectByType<CityClimate>();climate.Initialize();
            }
            for(int i=0;i<cars.Length;i++)
            {
                var car=cars[i];int start=i*28;
                if(!car.gameObject.activeInHierarchy)
                {for(int v=0;v<28;v++)vertices[start+v]=Vector3.zero;continue;}
                Bounds body=car.transform.Find("Sculpted Body").GetComponent<MeshFilter>().sharedMesh.bounds;
                Place(start,car.transform,Vector3.zero,body.size.x*.93f,body.size.z*.92f);
                for(int wheel=0;wheel<4;wheel++)
                {
                    Vector3 centre=car.Wheels[wheel].localPosition;
                    Place(start+4+wheel*4,car.transform,centre,.48f,.62f);
                }
                Place(start+20,car.transform,new Vector3(0,0,body.max.z+2.8f),body.size.x*.86f,5.2f);
                Place(start+24,car.transform,new Vector3(0,0,body.min.z-1.8f),body.size.x*.83f,3.2f);
                float brake=car.Lighting==null?.1f:Mathf.Clamp01(car.Lighting.BrakeGain*.35f);
                for(int v=24;v<28;v++)colours[start+v]=new Color(.56f+brake*.34f,.035f,.016f,1);
            }
            mesh.SetVertices(vertices);mesh.SetColors(colours);
            mesh.bounds=new Bounds(new Vector3(0,0,100),new Vector3(24,2,450));
            material.SetVector("_Ground",new Vector4(climate.Wetness,climate.LampGain,(float)climate.WeatherBlend.Weights[3],climate.ActiveSeconds));
        }
        private void Place(int first,Transform car,Vector3 centre,float width,float length)
        {
            for(int corner=0;corner<4;corner++)
            {
                Vector3 p=centre+new Vector3((corner==1||corner==2?1:-1)*width*.5f,0,(corner>=2?1:-1)*length*.5f);
                p=car.TransformPoint(p);p.y=.042f;vertices[first+corner]=p;
            }
        }
        private void LateUpdate()
        {
            bool wallpaper=AndroidWallpaperBridge.IsWallpaper;
            Sync(!paused&&(focused||wallpaper)&&(!wallpaper||AndroidWallpaperBridge.IsRendering));
        }
        private void OnApplicationPause(bool value){paused=value;}
        private void OnApplicationFocus(bool value){focused=value;}
        private void OnDestroy()
        {
            if(Application.isPlaying){if(mesh!=null)Destroy(mesh);if(material!=null)Destroy(material);}
            else{if(mesh!=null)DestroyImmediate(mesh);if(material!=null)DestroyImmediate(material);}
        }
    }
}
