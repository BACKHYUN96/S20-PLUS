using System;
using UnityEngine;

namespace PixelTraffic.UnityPrototype
{
    // One straight-lane visual probe, not a port of the production traffic simulation.
    public sealed class PrototypeDrive : MonoBehaviour
    {
        [SerializeField] private Transform[] wheels = Array.Empty<Transform>();
        private bool paused;
        private bool focused = true;
        private float laneX;
        private const float WheelRadius = .32f;

        public Transform[] Wheels { get => wheels; set => wheels = value; }

        private void Awake()
        {
            laneX = transform.position.x;
            Application.targetFrameRate = StarterConfig.TargetFps;
        }

        private void Update()
        {
            if (paused || (!focused && !Application.isEditor)) return;
            float elapsed = Time.deltaTime;
            Vector3 position = transform.position;
            position.z = Advance(position.z, elapsed);
            position.x = laneX;
            transform.position = position;
            float degrees = StarterConfig.SpeedMetresPerSecond * elapsed / WheelRadius * Mathf.Rad2Deg;
            foreach (Transform wheel in wheels)
                if (wheel != null) wheel.Rotate(Vector3.right, degrees, Space.Self);
        }

        public static float Advance(float position, float elapsed)
        {
            if (float.IsNaN(position) || float.IsInfinity(position))
                throw new ArgumentOutOfRangeException(nameof(position));
            if (float.IsNaN(elapsed) || float.IsInfinity(elapsed) || elapsed < 0)
                throw new ArgumentOutOfRangeException(nameof(elapsed));
            if (elapsed == 0) return position;
            float span = StarterConfig.RouteEnd - StarterConfig.RouteStart;
            return StarterConfig.RouteStart + Mathf.Repeat(
                position - StarterConfig.RouteStart - StarterConfig.SpeedMetresPerSecond * elapsed, span);
        }

        private void OnApplicationPause(bool value) => paused = value;
        private void OnApplicationFocus(bool value) => focused = value;
    }
}
