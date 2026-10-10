using UnityEngine;

namespace PixelTraffic.UnityPrototype
{
    // Driven only by CityClimate's active clock: hidden wallpaper never advances clouds.
    public sealed class DistantBackdrop : MonoBehaviour
    {
        [SerializeField] private Material source;
        [SerializeField] private Renderer[] views;
        private Material runtime;
        public Material RuntimeMaterial => runtime;
        public float CloudOffset { get; private set; }
        public void Configure(Material material, Renderer[] renderers) { source=material; views=renderers; }
        public void Apply(float day,float dusk,float night,float rain,float snow,float fog,float cloud,float seconds)
        {
            if(runtime==null)
            {
                runtime=new Material(source){name="Distant Landscape Runtime"};
                foreach(var view in views)view.sharedMaterial=runtime;
            }
            CloudOffset=seconds*.012f;
            runtime.SetVector("_Theme",new Vector4(day,dusk,night,0));
            runtime.SetVector("_Weather",new Vector4(cloud,fog,snow,rain));
            runtime.SetVector("_Motion",new Vector4(seconds,CloudOffset,0,0));
            runtime.SetColor("_HazeColor",RenderSettings.fogColor);
        }
        private void OnDestroy()
        {
            if(runtime==null)return;
            if(Application.isPlaying)Destroy(runtime);else DestroyImmediate(runtime);
        }
    }
}
