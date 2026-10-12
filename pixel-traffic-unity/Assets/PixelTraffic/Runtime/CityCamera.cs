using UnityEngine;

namespace PixelTraffic.UnityPrototype
{
    // Move the existing camera only. Traffic and weather keep their current clocks.
    [RequireComponent(typeof(Camera))]
    public sealed class CityCamera : MonoBehaviour
    {
        private Camera view;
        private bool wallpaper, paused;
        private float poll;
        public int Selected { get; private set; }
        private void Awake()
        {
            wallpaper = AndroidWallpaperBridge.IsWallpaper;
            Preview(wallpaper ? AndroidWallpaperBridge.CameraView : 0);
        }
        private void Update()
        {
            if (!wallpaper || paused || !AndroidWallpaperBridge.IsRendering || Time.unscaledTime < poll) return;
            poll = Time.unscaledTime + .25f;
            int selected = AndroidWallpaperBridge.CameraView;
            if (selected != Selected) Preview(selected);
        }
        public void Preview(int selected)
        {
            Selected = Mathf.Clamp(selected, 0, 1);
            if (view == null) view = GetComponent<Camera>();
            Vector3 position = Selected == 0 ? new Vector3(1.4f, 14, -37) : new Vector3(0, 46, 161);
            Vector3 target = Selected == 0 ? new Vector3(-4.9f, 1, 68) : new Vector3(0, 0, 241);
            transform.SetPositionAndRotation(position, Quaternion.LookRotation(target - position));
            view.fieldOfView = Selected == 0 ? 44 : 60;
        }
        private void OnApplicationPause(bool value) { paused = value; if (!value) poll = 0; }
    }
}
