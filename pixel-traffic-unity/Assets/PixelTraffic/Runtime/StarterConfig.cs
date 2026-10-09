namespace PixelTraffic.UnityPrototype
{
    public static class StarterConfig
    {
        public const string EditorVersion = "6000.3.26f1";
        public const string ScenePath = "Assets/PixelTraffic/Scenes/FirstRoad.unity";
        public const string ExperimentAppId = "com.s20plus.pixeltraffic.unityprototype";
        public const string ReleaseAppId = "com.s20plus.pixeltraffic";
        public const float LaneWidth = 3.2f;
        public const float RoadWidth = LaneWidth * 4;
        public const float LaneCenter = -LaneWidth * 1.5f;
        public const float RouteStart = -12;
        public const float RouteEnd = 90;
        public const float SpeedMetresPerSecond = 6;
        public const int TargetFps = 30;
    }
}
