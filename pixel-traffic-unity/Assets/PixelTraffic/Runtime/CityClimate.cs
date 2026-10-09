using UnityEngine;

namespace PixelTraffic.UnityPrototype
{
    public sealed class CityClimate : MonoBehaviour
    {
        [SerializeField] private Material[] surfaces;
        [SerializeField] private Light[] streetLights;
        [SerializeField] private Transform[] canopies;
        [SerializeField] private ClimateEffects effects;
        private Material[] runtimeSurfaces;
        private Quaternion[] treeRotations;
        private SceneBlend time, weather;
        private bool initialized, paused, focused = true, skipDelta, wallpaper;
        private float poll, clock;
        public SceneBlend TimeBlend => time;
        public SceneBlend WeatherBlend => weather;
        public float ActiveSeconds => clock;
        public ClimateEffects Effects => effects;
        public int AdditionalLights => streetLights.Length;
        public float LampGain { get; private set; }
        public float Wetness { get; private set; }
        public void Configure(Material[] materials, Light[] lights, Transform[] trees, ClimateEffects particles)
        { surfaces = materials; streetLights = lights; canopies = trees; effects = particles; }
        public void Initialize(int theme = 0, int selectedWeather = 0)
        {
            if (initialized) return; initialized = true;
            time = new SceneBlend(3, theme); weather = new SceneBlend(6, selectedWeather);
            runtimeSurfaces = new Material[surfaces.Length];
            for (int i = 0; i < surfaces.Length; i++) runtimeSurfaces[i] = new Material(surfaces[i]) { name = surfaces[i].name + " Runtime" };
            foreach (var renderer in Object.FindObjectsByType<Renderer>(FindObjectsSortMode.None))
                for (int i = 0; i < surfaces.Length; i++) if (renderer.sharedMaterial == surfaces[i]) { renderer.sharedMaterial = runtimeSurfaces[i]; break; }
            treeRotations = new Quaternion[canopies.Length];
            for (int i = 0; i < canopies.Length; i++) treeRotations[i] = canopies[i].localRotation;
            effects.Initialize(); Apply();
        }
        private void Awake()
        {
            wallpaper = AndroidWallpaperBridge.IsWallpaper;
            Initialize(wallpaper ? AndroidWallpaperBridge.Theme : 0, wallpaper ? AndroidWallpaperBridge.Weather : 0);
        }
        public void Select(int theme, int selectedWeather) { time.Select(theme); weather.Select(selectedWeather); }
        public void Preview(int theme, int selectedWeather) { Initialize(); time.Snap(theme); weather.Snap(selectedWeather); Apply(); }
        private void Update()
        {
            bool active = !paused && (focused || wallpaper || Application.isEditor) && (!wallpaper || AndroidWallpaperBridge.IsRendering);
            if (!active) return;
            if (wallpaper && Time.unscaledTime >= poll)
            { poll = Time.unscaledTime + .25f; Select(AndroidWallpaperBridge.Theme, AndroidWallpaperBridge.Weather); }
            if (skipDelta) { skipDelta = false; Apply(); return; }
            Advance(Time.unscaledDeltaTime, true);
        }
        public void Advance(double seconds, bool active)
        {
            if (!active || double.IsNaN(seconds) || double.IsInfinity(seconds) || seconds <= 0) return;
            float dt = (float)System.Math.Min(.1, seconds);
            time.Advance(dt); weather.Advance(dt); clock += dt; Apply();
            effects.Advance(dt, (float)(weather.Weights[1] * .38 + weather.Weights[2] + weather.Weights[5]), (float)weather.Weights[3], (float)weather.Weights[5], weather.Target == 5);
        }
        private void Apply()
        {
            float day = (float)time.Weights[0], dusk = (float)time.Weights[1], night = (float)time.Weights[2];
            float rain = (float)(weather.Weights[1] * .38 + weather.Weights[2] + weather.Weights[5]);
            float snow = (float)weather.Weights[3], fog = Mathf.Clamp01((float)(weather.Weights[4] + weather.Weights[5] * .78));
            float cloud = Mathf.Clamp01(rain * .68f + snow * .45f + fog * .3f);
            Light sun = RenderSettings.sun;
            sun.color = new Color(1, .88f, .70f) * day + new Color(1, .47f, .23f) * dusk + new Color(.52f, .65f, 1) * night;
            sun.intensity = (1.35f * day + .72f * dusk + .30f * night) * (1 - cloud * .72f);
            sun.transform.rotation = Quaternion.Euler(43 * day + 17 * dusk + 38 * night, -38 * day - 70 * dusk - 100 * night, 0);
            sun.shadowStrength = .82f * day + .68f * dusk + .4f * night;
            RenderSettings.ambientSkyColor = (new Color(.68f, .75f, .83f) * day + new Color(.47f, .32f, .39f) * dusk + new Color(.12f, .16f, .29f) * night) * (1 - cloud * .28f);
            RenderSettings.ambientEquatorColor = new Color(.43f, .46f, .49f) * day + new Color(.34f, .24f, .25f) * dusk + new Color(.09f, .115f, .20f) * night;
            RenderSettings.ambientGroundColor = new Color(.25f, .24f, .22f) * day + new Color(.18f, .13f, .13f) * dusk + new Color(.045f, .055f, .095f) * night;
            Color sky = new Color(.72f, .78f, .81f) * day + new Color(.70f, .42f, .32f) * dusk + new Color(.055f, .075f, .145f) * night;
            RenderSettings.fogColor = Color.Lerp(sky, new Color(.57f, .62f, .66f) * (day + dusk * .6f + night * .22f), cloud * .65f);
            RenderSettings.fogStartDistance = Mathf.Lerp(130, 10, fog);
            RenderSettings.fogEndDistance = Mathf.Lerp(360 * day + 310 * dusk + 270 * night, 105, fog);
            Camera.main.backgroundColor = RenderSettings.fogColor;
            LampGain = Mathf.Clamp01(dusk * .72f + night + cloud * .4f);
            Wetness = Mathf.Clamp01((float)(weather.Weights[1] * .68 + weather.Weights[2] + weather.Weights[4] * .35 + weather.Weights[5]));
            runtimeSurfaces[0].SetColor("_BaseColor", Color.Lerp(surfaces[0].GetColor("_BaseColor") * (1 - Wetness * .3f), new Color(.58f, .61f, .64f), snow * .45f));
            runtimeSurfaces[0].SetFloat("_Smoothness", Mathf.Lerp(.12f, .78f, Wetness));
            runtimeSurfaces[1].SetColor("_BaseColor", Color.Lerp(surfaces[1].GetColor("_BaseColor") * (1 - Wetness * .16f), new Color(.84f, .85f, .83f), snow * .7f));
            runtimeSurfaces[2].SetColor("_BaseColor", Color.Lerp(surfaces[2].GetColor("_BaseColor"), new Color(.72f, .40f, .12f), LampGain * .7f));
            runtimeSurfaces[2].SetColor("_EmissionColor", new Color(1, .49f, .12f) * LampGain * .65f);
            runtimeSurfaces[3].SetColor("_EmissionColor", new Color(1, .76f, .35f) * LampGain * 1.6f);
            runtimeSurfaces[4].SetColor("_EmissionColor", new Color(1, .92f, .68f) * LampGain * 1.5f);
            runtimeSurfaces[5].SetColor("_EmissionColor", new Color(1, .035f, .01f) * LampGain * 1.2f);
            foreach (var light in streetLights) light.intensity = 2.3f * LampGain;
            effects.SetLighting(LampGain);
            float wind = (float)weather.Weights[5];
            for (int i = 0; i < canopies.Length; i++)
            {
                float gust = Mathf.Sin(clock * 2.2f + i * .7f) * .7f + Mathf.Sin(clock * 4.3f + i) * .3f;
                canopies[i].localRotation = treeRotations[i] * Quaternion.Euler(wind * gust * 3, 0, wind * (2.5f + gust * 3.5f));
            }
        }
        private void OnApplicationPause(bool value) { paused = value; if (!value) skipDelta = true; }
        private void OnApplicationFocus(bool value) { focused = value; }
        private void OnDestroy() { if (runtimeSurfaces != null) foreach (var mat in runtimeSurfaces) if (mat != null) { if (Application.isPlaying) Destroy(mat); else DestroyImmediate(mat); } }
    }
}
