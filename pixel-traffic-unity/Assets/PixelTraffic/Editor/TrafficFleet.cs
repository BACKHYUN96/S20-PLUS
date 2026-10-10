using System;
using System.Collections.Generic;
using UnityEditor;
using UnityEngine;
using UnityEngine.Rendering;

namespace PixelTraffic.UnityPrototype.Editor
{
    internal static class TrafficFleet
    {
        private static readonly float[] speeds = {6.0f,7.0f,6.5f,5.8f};
        private static readonly float[] offsets = {17,34,9,26};

        // Bus/truck traffic favours the outer lanes; both directions still retain all four cars.
        internal static VehicleGeometry.Kind KindFor(int lane,int index)
        {
            if(lane<0||lane>3||index<0||index>=StarterConfig.VehiclesPerLane)
                throw new ArgumentOutOfRangeException(nameof(lane));
            if(lane==0&&(index==1||index==4)||lane==3&&index==3||lane==2&&index==2)
                return VehicleGeometry.Kind.CityBus;
            if(lane==3&&(index==1||index==4)||lane==0&&index==3||lane==1&&index==2)
                return VehicleGeometry.Kind.BoxTruck;
            return (VehicleGeometry.Kind)((lane+index+2)%4);
        }

        internal static void Create()
        {
            var fleet = new GameObject("Two Way Traffic").transform;
            var shapes = new VehicleGeometry.Shape[6];
            for(int i=0;i<shapes.Length;i++) shapes[i]=VehicleGeometry.Build((VehicleGeometry.Kind)i);
            Material blue = StarterScene.Surface("Vehicle Blue",new Color(.07f,.24f,.60f),.55f,.68f);
            Material red = StarterScene.Surface("Vehicle Red",new Color(.58f,.035f,.025f),.45f,.65f);
            Material silver = StarterScene.Surface("Vehicle Silver",new Color(.58f,.64f,.69f),.60f,.63f);
            Material pearl = StarterScene.Surface("Vehicle Pearl",new Color(.82f,.81f,.76f),.38f,.62f);
            Material yellow = StarterScene.Surface("Vehicle Yellow",new Color(.96f,.55f,.045f),.38f,.62f);
            Material glass = StarterScene.Surface("Vehicle Glass",Color.white,.35f,.88f);
            GlassFinish(glass);
            Material trim = StarterScene.Surface("Vehicle Trim",new Color(.025f,.031f,.038f),.05f,.2f);
            Material metal = StarterScene.Surface("Vehicle Metal",new Color(.63f,.67f,.70f),.78f,.73f);
            Material head = StarterScene.Surface("Headlamp",new Color(.97f,.94f,.79f),.1f,.6f);
            Material tail = StarterScene.Surface("Tail Lamp",new Color(.66f,.04f,.025f),.1f,.6f);
            Material truckWhite=StarterScene.Surface("Truck White",new Color(.93f,.94f,.92f),.20f,.55f);
            Material[] paints={blue,red,silver,pearl};
            for(int lane=0;lane<4;lane++) for(int index=0;index<StarterConfig.VehiclesPerLane;index++)
            {
                int kind=(int)KindFor(lane,index);
                VehicleGeometry.Shape shape=shapes[kind];
                Transform root=new GameObject(((VehicleGeometry.Kind)kind)+" Lane "+lane+" #"+index).transform;
                root.SetParent(fleet,false);
                float span=StarterConfig.RouteEnd-StarterConfig.RouteStart;
                float z=StarterConfig.RouteStart+offsets[lane]+index*span/StarterConfig.VehiclesPerLane;
                root.position=new Vector3((lane-1.5f)*StarterConfig.LaneWidth,0,z);
                Material paint=kind==(int)VehicleGeometry.Kind.Taxi?yellow:
                    kind==(int)VehicleGeometry.Kind.CityBus?blue:
                    kind==(int)VehicleGeometry.Kind.BoxTruck?truckWhite:paints[(lane+index)%paints.Length];
                MeshPart("Sculpted Body",root,shape.paint,paint);
                MeshPart("Sloped Windows",root,shape.glass,glass);
                MeshPart("Grille and Trim",root,shape.trim,trim);
                MeshPart("Chrome and Plates",root,shape.metal,metal);
                MeshPart("Front Lamps",root,shape.head,head);
                MeshPart("Rear Lamps",root,shape.tail,tail);
                var wheels=new List<Transform>();
                foreach(float side in new[]{-1f,1f}) foreach(float wheelZ in new[]{shape.rearAxle,shape.frontAxle})
                {
                    Transform wheel=new GameObject("Wheel").transform;wheel.SetParent(root,false);
                    wheel.localPosition=new Vector3(side*shape.wheelX,shape.radius,wheelZ);
                    MeshPart("Tyre",wheel,shape.tyre,trim);MeshPart("Five Spoke Rim",wheel,shape.rim,metal);wheels.Add(wheel);
                }
                var drive=root.gameObject.AddComponent<PrototypeDrive>();
                drive.Wheels=wheels.ToArray();drive.Configure(lane,speeds[lane],shape.radius,((VehicleGeometry.Kind)kind).ToString());
                DrivingScene.Attach(root,shape,head,((VehicleGeometry.Kind)kind).ToString());
            }
        }

        private static void GlassFinish(Material material)
        {
            // Small authored highlight/sky tint, lit by the existing URP shader.
            // No realtime reflection probe or per-car texture/material allocation.
            var texture=new Texture2D(64,64,TextureFormat.RGB24,true) {
                name="Vehicle Window Finish",filterMode=FilterMode.Bilinear,wrapMode=TextureWrapMode.Clamp };
            for(int y=0;y<64;y++)for(int x=0;x<64;x++)
            {
                float u=x/63f,v=y/63f;
                Color color=Color.Lerp(new Color(.025f,.045f,.065f),new Color(.19f,.32f,.43f),Mathf.SmoothStep(0,1,v));
                float band=Mathf.Exp(-Mathf.Pow((v-(.65f-u*.18f))/.045f,2));
                color=Color.Lerp(color,new Color(.38f,.48f,.55f),band*.55f);
                texture.SetPixel(x,y,color);
            }
            texture.Apply();AssetDatabase.CreateAsset(texture,StarterScene.Generated+"/VehicleWindowFinish.asset");
            material.SetTexture("_BaseMap",texture);
        }

        private static void MeshPart(string name,Transform root,Mesh mesh,Material material)
        {
            var part=new GameObject(name);part.transform.SetParent(root,false);
            part.AddComponent<MeshFilter>().sharedMesh=mesh;
            var renderer=part.AddComponent<MeshRenderer>();renderer.sharedMaterial=material;
            renderer.shadowCastingMode=ShadowCastingMode.On;renderer.receiveShadows=true;
            renderer.lightProbeUsage=LightProbeUsage.Off;renderer.reflectionProbeUsage=ReflectionProbeUsage.Off;
        }

        internal static Report Validate()
        {
            var cars=UnityEngine.Object.FindObjectsByType<PrototypeDrive>(FindObjectsSortMode.None);
            Need(cars.Length==4*StarterConfig.VehiclesPerLane,"Traffic population incomplete.");
            int[] counts=new int[4];int visible=0;var planes=GeometryUtility.CalculateFrustumPlanes(Camera.main);var models=new Dictionary<string,ModelReport>();
            float[] initial=new float[cars.Length];Quaternion[][] wheelRotations=new Quaternion[cars.Length][];
            for(int i=0;i<cars.Length;i++)
            {
                var car=cars[i];counts[car.Lane]++;initial[i]=car.transform.position.z;
                wheelRotations[i]=new Quaternion[4];
                Need(car.Wheels.Length==4,"Vehicle needs four independent wheel pivots.");
                Need(car.transform.localScale==Vector3.one&&car.transform.parent.localScale==Vector3.one,"Vehicle scaled to fit a lane.");
                Need(Mathf.Abs(car.transform.position.x-car.LaneX)<.001f,"Vehicle not centered in its lane.");
                Need(Vector3.Dot(car.transform.forward,Vector3.forward*car.Direction)>.999f,"Vehicle front/rear points away from travel direction.");
                var renderers=car.GetComponentsInChildren<Renderer>();Bounds bounds=car.BodyBounds();
                foreach(var r in renderers)
                {
                    Need(r.sharedMaterial!=null&&r.sharedMaterial.shader.name==(r.name=="Headlight Road Beams"?"PixelTraffic/Atmosphere":"Universal Render Pipeline/Lit"),"Car has missing material.");
                    Need(GameObjectUtility.GetStaticEditorFlags(r.gameObject)==0,"Moving car is statically batched.");
                }
                Need(bounds.min.x>car.LaneX-StarterConfig.LaneWidth/2&&bounds.max.x<car.LaneX+StarterConfig.LaneWidth/2,"Vehicle exceeds its own lane.");
                Need(Mathf.Abs(bounds.min.y)<.001f,"Tyre contact floats or penetrates the road.");
                if(car.Model=="CityBus")
                    Need(bounds.size.x>2.6f&&bounds.size.x<2.9f&&bounds.size.z>10.5f&&bounds.size.z<10.8f&&bounds.size.y>3.2f&&bounds.size.y<3.5f,"Bus dimensions compressed or lane-exceeding.");
                else if(car.Model=="BoxTruck")
                    Need(bounds.size.x>2.5f&&bounds.size.x<2.9f&&bounds.size.z>7.3f&&bounds.size.z<7.6f&&bounds.size.y>3.3f&&bounds.size.y<3.5f,"Truck dimensions compressed or lane-exceeding.");
                else
                {Need(bounds.size.x>2&&bounds.size.x<2.4f&&bounds.size.z>4.4f&&bounds.size.z<5.1f,"Vehicle metres changed or compressed.");
                 Need(bounds.size.y>1.20f&&bounds.size.y<2,"Vehicle height compressed.");}
                Need(car.GetComponentsInChildren<Collider>().Length==0,"Visual fleet unexpectedly adds physics cost.");
                for(int n=0;n<4;n++)
                {
                    wheelRotations[i][n]=car.Wheels[n].localRotation;
                    Need(Mathf.Abs(car.Wheels[n].localPosition.y-car.WheelRadius)<.001f,"Wheel contact radius mismatch.");
                }
                if(GeometryUtility.TestPlanesAABB(planes,bounds))visible++;
                string model=car.Model;
                var report=new ModelReport {model=model,width=bounds.size.x,height=bounds.size.y,length=bounds.size.z};
                if(models.TryGetValue(model,out var previous))
                    Need(Mathf.Abs(previous.width-report.width)<.001f&&Mathf.Abs(previous.height-report.height)<.001f&&Mathf.Abs(previous.length-report.length)<.001f,"Car dimensions vary by lane or distance.");
                else models.Add(model,report);
            }
            for(int lane=0;lane<4;lane++)Need(counts[lane]==StarterConfig.VehiclesPerLane,"Missing lane traffic.");
            Need(visible>=6,"Camera does not show sufficient two-way traffic.");
            Need(models.Count==6&&models["Suv"].height>models["Sedan"].height+.30f&&models["SportCoupe"].height<models["Sedan"].height-.15f,"Model height differences lost.");
            float minimumGap=float.MaxValue;
            try
            {
                // Independent closed-form oracle, also exercises actual Update's Step on every car.
                foreach(int fps in new[]{15,30,60,120})
                {
                    for(int i=0;i<cars.Length;i++)cars[i].ResetPosition(initial[i]);
                    for(int n=0;n<fps*60;n++)foreach(var car in cars)car.Step(1f/fps);
                    for(int i=0;i<cars.Length;i++)
                    {
                        double expected=initial[i]+cars[i].Direction*cars[i].Speed*60;
                        double span=StarterConfig.RouteEnd-StarterConfig.RouteStart;
                        expected-=span*Math.Floor((expected-StarterConfig.RouteStart)/span);
                        Need(Math.Abs(cars[i].transform.position.z-expected)<.005,"Traffic changes with frame rate.");
                    }
                }
                for(int i=0;i<cars.Length;i++)cars[i].ResetPosition(initial[i]);
                for(int n=0;n<1200;n++)
                {
                    foreach(var car in cars)car.Step(.5f);
                    for(int i=0;i<cars.Length;i++)
                    {
                        var car=cars[i];Need(Mathf.Abs(car.transform.position.x-car.LaneX)<.001f,"Vehicle drifts sideways.");
                        Need(car.transform.position.z>=StarterConfig.RouteStart&&car.transform.position.z<StarterConfig.RouteEnd,"Wrap leaves route bounds.");
                        for(int j=i+1;j<cars.Length;j++)if(car.Lane==cars[j].Lane)
                        {
                            float distance=Mathf.Abs(car.transform.position.z-cars[j].transform.position.z);
                            distance=Mathf.Min(distance,StarterConfig.RouteEnd-StarterConfig.RouteStart-distance);
                            float a=models[car.Model].length,b=models[cars[j].Model].length;
                            float gap=distance-(a+b)/2;minimumGap=Mathf.Min(minimumGap,gap);
                            Need(gap>20,"Cars overlap or bunch during wrap.");
                        }
                    }
                }
                var probe=cars[0];probe.ResetPosition(24);
                foreach(var wheel in probe.Wheels)wheel.localRotation=Quaternion.identity;
                probe.Step(.1f);
                float expectedAngle=probe.Speed*.1f/probe.WheelRadius*Mathf.Rad2Deg;
                foreach(var wheel in probe.Wheels)Need(Quaternion.Angle(wheel.localRotation,Quaternion.AngleAxis(expectedAngle,Vector3.right))<.02f,"Wheel spin is inconsistent with travelled distance.");
                Vector3 before=probe.transform.position;Quaternion rotation=probe.Wheels[0].localRotation;
                probe.SetPaused(true);probe.Step(5);Need(probe.transform.position==before&&probe.Wheels[0].localRotation==rotation,"Suspended app moves traffic.");
                probe.SetPaused(false);probe.Step(.1f);
                Need(Mathf.Abs(probe.transform.position.z-before.z-probe.Direction*probe.Speed*.1f)<.001f,"Resume catches up hidden time.");
                foreach(int direction in new[]{-1,1})
                {
                    double expected=direction<0?StarterConfig.RouteEnd-1:StarterConfig.RouteStart+1;
                    double start=direction<0?StarterConfig.RouteStart:StarterConfig.RouteEnd;
                    Need(Math.Abs(PrototypeDrive.Advance(start,1,direction,1)-expected)<.0001,"Wrap loses overshoot.");
                    Need(PrototypeDrive.Advance(24,0,direction,6)==24,"Zero elapsed moves vehicle.");
                    Need(Math.Abs(PrototypeDrive.Advance(24,155,direction,6)-24)<.0001,"Multi-loop wrap loses phase.");
                }
                foreach(double invalid in new[]{double.NaN,double.PositiveInfinity,-1d})
                {
                    bool rejected=false;try{PrototypeDrive.Advance(24,invalid,1,6);}catch(ArgumentOutOfRangeException){rejected=true;}
                    Need(rejected,"Invalid time accepted.");
                }
            }
            finally
            {
                for(int i=0;i<cars.Length;i++)
                {
                    cars[i].SetPaused(false);cars[i].ResetPosition(initial[i]);
                    for(int n=0;n<4;n++)cars[i].Wheels[n].localRotation=wheelRotations[i][n];
                }
            }
            var list=new List<ModelReport>(models.Values);list.Sort((a,b)=>string.CompareOrdinal(a.model,b.model));
            return new Report {vehicles=cars.Length,visibleVehicles=visible,perLane=counts,models=list.ToArray(),minimumBumperGap=minimumGap,
                simulatedSeconds=600,frameRates=new[]{15,30,60,120},result="PASS: actual fleet geometry/Step, dimensions, direction, 10-minute separation, wrap, wheels and pause/resume; device performance unmeasured"};
        }
        private static void Need(bool value,string message){if(!value)throw new InvalidOperationException(message);}
        [Serializable] internal sealed class ModelReport {public string model;public float width,height,length;}
        [Serializable] internal sealed class Report
        {
            public string result;public int vehicles,visibleVehicles,simulatedSeconds;public int[] perLane,frameRates;
            public float minimumBumperGap;public ModelReport[] models;
        }
    }
}
