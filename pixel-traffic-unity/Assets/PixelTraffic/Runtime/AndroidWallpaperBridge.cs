using UnityEngine;

namespace PixelTraffic.UnityPrototype
{
    internal static class AndroidWallpaperBridge
    {
#if UNITY_ANDROID && !UNITY_EDITOR
        private static AndroidJavaClass preferences;
        private static bool warned;
        private static T Read<T>(string method,T fallback)
        {
            try
            {
                if(preferences==null)preferences=new AndroidJavaClass("com.s20plus.pixeltraffic.unitywallpaper.WallpaperPreferences");
                return preferences.CallStatic<T>(method);
            }
            catch(System.Exception)
            {
                if(!warned){Debug.LogWarning("Wallpaper preferences unavailable; keeping default settings.");warned=true;}
                return fallback;
            }
        }
        internal static bool IsWallpaper => Read("isWallpaperRuntime",false);
        internal static bool IsRendering => Read("isRendering",false);
        internal static int Population(int fallback) => Mathf.Clamp(Read("getPopulation",fallback),4,100);
        internal static int FrameRate => Read("getFrameRate",30)==15?15:30;
#else
        internal static bool IsWallpaper => false;
        internal static bool IsRendering => true;
        internal static int Population(int fallback) => fallback;
        internal static int FrameRate => StarterConfig.TargetFps;
#endif
    }
}
