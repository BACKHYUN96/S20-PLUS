using System;
using UnityEngine;

namespace PixelTraffic.UnityPrototype
{
    public sealed class StreetSimulation : MonoBehaviour
    {
        [Serializable] public sealed class WalkerView {public Transform root;public Transform[] limbs;public Transform umbrella;}
        [SerializeField] private PrototypeDrive[] vehicles;
        [SerializeField] private WalkerView[] walkers;
        [SerializeField] private Renderer[] vehicleLights,walkLights;
        [SerializeField] private int people=StarterConfig.DefaultPeople;
        [SerializeField] private WetTraffic spray;
        private CityClimate climate;
        private readonly Plane[] viewPlanes=new Plane[6];
        private Camera portraitCamera;
        private StreetModel model;
        public WetTraffic Spray => spray;
        public void ConfigureSpray(WetTraffic value)=>spray=value;
        private double accumulator;
        private bool paused,focused=true;
        private bool wallpaperRuntime,skipNextDelta;
        private float settingsAt;
        private StreetModel.Phase lastPhase=(StreetModel.Phase)(-1);
        private MaterialPropertyBlock lightBlock;
        private static readonly Color[] umbrellaColors={new Color(.45f,.65f,1),new Color(1,.4f,.3f),new Color(1,.83f,.4f),new Color(.5f,.83f,.6f)};
        public StreetModel Model => model;
        public WalkerView[] Walkers => walkers;
        public void Configure(PrototypeDrive[] cars,WalkerView[] views,Renderer[] signals,Renderer[] crossingSignals)
        {
            vehicles=cars;walkers=views;vehicleLights=signals;walkLights=crossingSignals;
            foreach(var car in cars)car.Bind(this);ResetModel();ApplyViews();
        }
        private void Awake()
        {
            wallpaperRuntime=AndroidWallpaperBridge.IsWallpaper;
            Application.runInBackground=wallpaperRuntime;
            Application.targetFrameRate=wallpaperRuntime?AndroidWallpaperBridge.FrameRate:StarterConfig.TargetFps;
            if(wallpaperRuntime)people=AndroidWallpaperBridge.Population(people);
            ResetModel();
        }
        public void ResetModel()
        {
            var cars=new StreetModel.Car[vehicles.Length];
            for(int i=0;i<cars.Length;i++)
            {
                var vehicle=vehicles[i];vehicle.gameObject.SetActive(true);var renderers=vehicle.GetComponentsInChildren<Renderer>(true);Bounds bounds=renderers[0].bounds;
                foreach(var r in renderers)bounds.Encapsulate(r.bounds);
                cars[i]=new StreetModel.Car {lane=vehicle.Lane,z=vehicle.transform.position.z,cruise=vehicle.Speed,speed=vehicle.Speed,length=bounds.size.z};
            }
            model=new StreetModel(people,cars);
            portraitCamera=Camera.main;if(portraitCamera!=null){GeometryUtility.CalculateFrustumPlanes(portraitCamera,viewPlanes);model.Visible=Visible;}
            accumulator=0;lastPhase=(StreetModel.Phase)(-1);
        }
        private void Update()
        {
            if(paused||(!focused&&!Application.isEditor&&!wallpaperRuntime))return;
            if(wallpaperRuntime)
            {
                if(!AndroidWallpaperBridge.IsRendering)return;
                if(Time.unscaledTime>=settingsAt)
                {
                    settingsAt=Time.unscaledTime+1;
                    int count=AndroidWallpaperBridge.Population(people);
                    if(count!=people)SetPeople(count);
                    Application.targetFrameRate=AndroidWallpaperBridge.FrameRate;
                }
            }
            if(skipNextDelta){skipNextDelta=false;ApplyViews();return;}
            Advance(Math.Min(Time.deltaTime,.25));ApplyViews();
        }
        public void Advance(double elapsed)
        {
            if(double.IsNaN(elapsed)||double.IsInfinity(elapsed)||elapsed<0)throw new ArgumentOutOfRangeException(nameof(elapsed));
            if(paused||(!focused&&!Application.isEditor&&!wallpaperRuntime))return;
            if(climate==null)climate=UnityEngine.Object.FindFirstObjectByType<CityClimate>();
            if(climate!=null&&climate.WeatherBlend!=null)model.SetWeather(climate.PeopleFraction,climate.TrafficFraction,climate.DrivingPace,climate.WalkingPace);
            if(portraitCamera!=null)GeometryUtility.CalculateFrustumPlanes(portraitCamera,viewPlanes);
            accumulator+=elapsed;
            while(accumulator+1e-7>=1d/30)
            {
                model.Tick();accumulator-=1d/30;
                for(int i=0;i<vehicles.Length;i++)
                {
                    var car=model.Cars[i];if(vehicles[i].gameObject.activeSelf!=car.active)vehicles[i].gameObject.SetActive(car.active);
                    vehicles[i].ApplyTraffic(car.z,car.distance);
                }
            }
            if(spray!=null)spray.Advance((float)elapsed,model,climate!=null&&climate.WeatherBlend!=null?climate.RainGain:0);
        }
        private bool Visible(Vector2 point)
            =>GeometryUtility.TestPlanesAABB(viewPlanes,new Bounds(new Vector3(point.x,1.2f,point.y),new Vector3(3,3.2f,6)));
        public void ApplyViews()
        {
            if(lightBlock==null)lightBlock=new MaterialPropertyBlock();
            for(int i=0;i<walkers.Length;i++)
            {
                var p=model.People[i];var v=walkers[i];if(v.root.gameObject.activeSelf!=p.active)v.root.gameObject.SetActive(p.active);if(!p.active)continue;
                float x=Mathf.Abs(p.position.x);
                float ground=Mathf.Clamp01((x-6.4f)/.35f)*.16f;
                ground=Mathf.Lerp(ground,.06f,Mathf.Clamp01((x-10.36f)/.12f));
                v.root.position=new Vector3(p.position.x,ground,p.position.y);
                if(p.velocity.sqrMagnitude>.001f)v.root.rotation=Quaternion.LookRotation(new Vector3(p.velocity.x,0,p.velocity.y));
                float rain=climate!=null&&climate.WeatherBlend!=null?climate.RainGain:0;
                float wind=climate!=null&&climate.WeatherBlend!=null?climate.WindGain:0;
                if(v.umbrella!=null)
                {
                    bool open=rain>.015f;if(v.umbrella.gameObject.activeSelf!=open)v.umbrella.gameObject.SetActive(open);
                    float scale=Mathf.SmoothStep(0,1,Mathf.Clamp01(rain/.38f));v.umbrella.localScale=new Vector3(scale,1,scale);
                    v.umbrella.rotation=Quaternion.Euler(-wind*12,0,wind*10);
                    if(open){lightBlock.Clear();lightBlock.SetColor("_BaseColor",umbrellaColors[i%4]);lightBlock.SetFloat("_Smoothness",.6f);v.umbrella.GetComponent<Renderer>().SetPropertyBlock(lightBlock);}
                }
                float angle=Mathf.Sin(p.walkDistance*8)*20*(p.velocity.sqrMagnitude>.01f?1:0);
                for(int n=0;n<4;n++)v.limbs[n].localRotation=Quaternion.Euler(n==1&&rain>.015f?-65:(n%2==0?1:-1)*angle*(n < 2 ? .8f : 1),0,0);
            }
            if(lastPhase==model.Signal)return;lastPhase=model.Signal;
            bool green=model.Signal==StreetModel.Phase.VehicleGreen,yellow=model.Signal==StreetModel.Phase.VehicleYellow;
            for(int i=0;i<vehicleLights.Length;i++)
            {
                int lamp=i%3;bool on=lamp==0?!green&&!yellow:lamp==1?yellow:green;
                Tint(vehicleLights[i],on?(lamp==0?new Color(1,.045f,.018f):lamp==1?new Color(1,.57f,.025f):new Color(.06f,.93f,.22f)):new Color(.028f,.035f,.04f));
            }
            for(int i=0;i<walkLights.Length;i++)Tint(walkLights[i],i%2==1?(model.CanEnter?new Color(.08f,1,.26f):new Color(.03f,.04f,.04f)):(model.CanEnter?new Color(.03f,.04f,.04f):new Color(1,.035f,.02f)));
        }
        private void Tint(Renderer renderer,Color color){lightBlock.SetColor("_BaseColor",color);lightBlock.SetColor("_EmissionColor",color*.65f);renderer.SetPropertyBlock(lightBlock);}
        public void SetPaused(bool value)=>paused=value;
        private void OnApplicationPause(bool value){SetPaused(value);if(!value)skipNextDelta=true;}
        private void OnApplicationFocus(bool value)=>focused=value;
        private void OnGUI()
        {
            if(wallpaperRuntime)return;
            Matrix4x4 previous=GUI.matrix;GUI.matrix=Matrix4x4.Scale(Vector3.one*(Screen.width/540f));
            GUI.Box(new Rect(344,12,184,80),"People: "+model.RequestedPeople);
            if(GUI.Button(new Rect(354,44,74,35),"- 4"))SetPeople(model.RequestedPeople-4);
            if(GUI.Button(new Rect(438,44,74,35),"+ 4"))SetPeople(model.RequestedPeople+4);
            GUI.matrix=previous;
        }
        private void SetPeople(int count){people=Mathf.Clamp(count,4,100);model.SetPopulation(people);ApplyViews();}
    }
}
