using System;
using UnityEngine;

namespace PixelTraffic.UnityPrototype
{
    // The street clock supplies the real lane-change pose; Step remains the fixed-lane wrap oracle.
    public sealed class PrototypeDrive : MonoBehaviour
    {
        [SerializeField] private Transform[] wheels = Array.Empty<Transform>();
        [SerializeField] private int lane;
        [SerializeField] private float speed = StarterConfig.SpeedMetresPerSecond;
        [SerializeField] private float wheelRadius = .32f;
        [SerializeField] private string model;
        [SerializeField] private StreetSimulation street;
        private bool paused, focused = true, initialized;
        private double routePosition;
        private VehicleLighting lighting;
        private float wheelRoll;

        public Transform[] Wheels { get => wheels; set => wheels = value; }
        public int Lane => lane;
        public int Direction => lane < 2 ? -1 : 1;
        public float Speed => speed;
        public float WheelRadius => wheelRadius;
        public string Model => model;
        public float LaneX => (lane - 1.5f) * StarterConfig.LaneWidth;
        public VehicleLighting Lighting=>lighting!=null?lighting:(lighting=GetComponent<VehicleLighting>());
        public Bounds BodyBounds()
        {
            Bounds bounds=default;bool found=false;
            foreach(var renderer in GetComponentsInChildren<Renderer>(true))
            {if(renderer.name=="Headlight Road Beams")continue;if(!found){bounds=renderer.bounds;found=true;}else bounds.Encapsulate(renderer.bounds);}
            return bounds;
        }

        public void Configure(int laneIndex, float metresPerSecond, float radius, string modelName)
        {
            if (laneIndex < 0 || laneIndex > 3 || metresPerSecond <= 0 || radius <= 0)
                throw new ArgumentOutOfRangeException(nameof(laneIndex));
            lane = laneIndex; speed = metresPerSecond; wheelRadius = radius; model = modelName;
            ResetPosition(transform.position.z);
        }

        private void Awake()
        {
            Application.targetFrameRate = StarterConfig.TargetFps;
            ResetPosition(transform.position.z);
        }

        private void Update() { if (street == null) Step(Time.deltaTime); }
        public void Bind(StreetSimulation controller) => street = controller;

        public void ApplyTraffic(double position, float distance)
        {
            routePosition = position;
            transform.position = new Vector3(LaneX, 0, (float)position);
            float degrees = distance / wheelRadius * Mathf.Rad2Deg;
            foreach (Transform wheel in wheels) if (wheel != null) wheel.Rotate(Vector3.right, degrees, Space.Self);
        }
        public void ApplyTraffic(StreetModel.Car car)
        {
            routePosition=car.z;float yaw=LaneChanges.Yaw(car);
            transform.position=new Vector3(car.X,0,(float)car.z);transform.rotation=Quaternion.Euler(0,(Direction<0?180:0)+yaw,0);
            wheelRoll=Mathf.Repeat(wheelRoll+car.distance/wheelRadius*Mathf.Rad2Deg,360);
            foreach(var wheel in wheels)if(wheel!=null)wheel.localRotation=Quaternion.Euler(0,wheel.localPosition.z>0?yaw*1.25f:0,0)*Quaternion.AngleAxis(wheelRoll,Vector3.right);
        }

        public void ResetPosition(float z)
        {
            routePosition = z;
            wheelRoll=0;
            initialized = true;
            transform.position = new Vector3(LaneX, 0, z);
            transform.rotation = Quaternion.Euler(0, Direction < 0 ? 180 : 0, 0);
        }

        // Used by Update and scene validation: pause does not accumulate catch-up time.
        public void Step(float elapsed)
        {
            if (paused || (!focused && !Application.isEditor)) return;
            if (!initialized) ResetPosition(transform.position.z);
            routePosition = Advance(routePosition, elapsed, Direction, speed);
            transform.position = new Vector3(LaneX, 0, (float)routePosition);
            float degrees = speed * elapsed / wheelRadius * Mathf.Rad2Deg;
            foreach (Transform wheel in wheels)
                if (wheel != null) wheel.Rotate(Vector3.right, degrees, Space.Self);
        }

        public static float Advance(float position, float elapsed)
            => (float)Advance(position, elapsed, -1, StarterConfig.SpeedMetresPerSecond);

        public static double Advance(double position, double elapsed, int direction, double metresPerSecond)
        {
            if (double.IsNaN(position) || double.IsInfinity(position) || double.IsNaN(elapsed) ||
                double.IsInfinity(elapsed) || elapsed < 0 || metresPerSecond <= 0 ||
                double.IsNaN(metresPerSecond) || double.IsInfinity(metresPerSecond) || Math.Abs(direction) != 1)
                throw new ArgumentOutOfRangeException(nameof(elapsed));
            if (elapsed == 0) return position;
            double span = StarterConfig.RouteEnd - StarterConfig.RouteStart;
            double phase = (position - StarterConfig.RouteStart + direction * metresPerSecond * elapsed) % span;
            if (phase < 0) phase += span;
            return StarterConfig.RouteStart + phase;
        }

        public void SetPaused(bool value) => paused = value;
        private void OnApplicationPause(bool value) => SetPaused(value);
        private void OnApplicationFocus(bool value) => focused = value;
    }
}
