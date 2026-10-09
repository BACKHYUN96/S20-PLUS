using System;
using System.IO;
using System.Linq;
using System.Xml.Linq;
using UnityEditor.Android;
using UnityEngine;

namespace PixelTraffic.UnityPrototype.Editor
{
    public sealed class AndroidWallpaperBuild : IPostGenerateGradleAndroidProject
    {
        private const string Package="com.s20plus.pixeltraffic.unitywallpaper.";
        private static readonly XNamespace Android="http://schemas.android.com/apk/res/android";
        public int callbackOrder => 100;
        private static string Templates => Path.GetFullPath(Path.Combine(Application.dataPath,"../NativeAndroid"));
        public void OnPostGenerateGradleAndroidProject(string path)
        {
            Copy(Path.Combine(Templates,"src"),Path.Combine(path,"src/main/java"));
            Copy(Path.Combine(Templates,"res"),Path.Combine(path,"src/main/res"));
            Patch(Path.Combine(path,"src/main/AndroidManifest.xml"),true);
            string launcher=Path.Combine(Path.GetDirectoryName(path),"launcher/src/main/AndroidManifest.xml");
            if(File.Exists(launcher))Patch(launcher,false);
            ValidateGenerated(path);
            Directory.CreateDirectory("Reports");
            File.WriteAllText("Reports/wallpaper-build.json",JsonUtility.ToJson(new Report {
                result="PASS: generated service/provider/launcher permissions, metadata and copied native sources; device surface test pending",
                version=StarterConfig.VersionName,service=Package+"PixelTrafficWallpaperService",
                launcher=Package+"WallpaperSettingsActivity",process=":wallpaper",authority=StarterConfig.ExperimentAppId+".wallpaper.settings"
            },true));
        }
        private static void Copy(string source,string destination)
        {
            foreach(string file in Directory.GetFiles(source,"*",SearchOption.AllDirectories))
            {
                string target=Path.Combine(destination,file.Substring(source.Length+1));
                Directory.CreateDirectory(Path.GetDirectoryName(target));File.Copy(file,target,true);
            }
        }
        internal static void Patch(string path,bool library)
        {
            var document=XDocument.Load(path);XElement root=document.Root,app=root.Element("application");
            if(app==null)throw new InvalidOperationException("Android application manifest missing.");
            foreach(var activity in app.Elements("activity"))
            {
                string name=(string)activity.Attribute(Android+"name")??"";
                if(name.StartsWith("com.unity3d.player.UnityPlayer"))
                {
                    activity.SetAttributeValue(Android+"exported","false");activity.SetAttributeValue(Android+"enabled","false");
                    foreach(var filter in activity.Elements("intent-filter").ToArray())filter.Remove();
                }
            }
            foreach(var component in app.Elements().Where(e=>((string)e.Attribute(Android+"name")??"").StartsWith(Package)).ToArray())component.Remove();
            if(library)
            {
                if(!root.Elements("uses-feature").Any(e=>(string)e.Attribute(Android+"name")=="android.software.live_wallpaper"))
                    root.AddFirst(new XElement("uses-feature",A("name","android.software.live_wallpaper"),A("required","false")));
                app.Add(new XElement("activity",A("name",Package+"WallpaperSettingsActivity"),A("exported","true"),
                    A("label","@string/pixel_traffic_wallpaper_name"),A("theme","@android:style/Theme.Material.Light.NoActionBar"),
                    new XElement("intent-filter",new XElement("action",A("name","android.intent.action.MAIN")),new XElement("category",A("name","android.intent.category.LAUNCHER")))));
                app.Add(new XElement("provider",A("name",Package+"WallpaperSettingsProvider"),A("exported","false"),
                    A("authorities",StarterConfig.ExperimentAppId+".wallpaper.settings")));
                app.Add(new XElement("service",A("name",Package+"PixelTrafficWallpaperService"),A("exported","true"),
                    A("permission","android.permission.BIND_WALLPAPER"),A("process",":wallpaper"),A("label","@string/pixel_traffic_wallpaper_name"),
                    new XElement("intent-filter",new XElement("action",A("name","android.service.wallpaper.WallpaperService"))),
                    new XElement("meta-data",A("name","android.service.wallpaper"),A("resource","@xml/pixel_traffic_wallpaper"))));
            }
            document.Save(path);
        }
        private static XAttribute A(string name,string value)=>new XAttribute(Android+name,value);
        private static void Need(bool value,string message){if(!value)throw new InvalidOperationException(message);}
        private static void ValidateGenerated(string path)
        {
            var app=XDocument.Load(Path.Combine(path,"src/main/AndroidManifest.xml")).Root.Element("application");
            var service=app.Elements("service").Single(e=>(string)e.Attribute(Android+"name")==Package+"PixelTrafficWallpaperService");
            Need((string)service.Attribute(Android+"permission")=="android.permission.BIND_WALLPAPER"&&
                (string)service.Attribute(Android+"process")==":wallpaper","Wallpaper service permission/process changed.");
            Need(app.Elements("provider").Single(e=>(string)e.Attribute(Android+"name")==Package+"WallpaperSettingsProvider").Attribute(Android+"exported").Value=="false","Settings provider exported.");
            Need(app.Elements("activity").Single(e=>(string)e.Attribute(Android+"name")==Package+"WallpaperSettingsActivity").Elements("intent-filter").Any(),"Settings launcher missing.");
            Need(!app.Elements("activity").Any(e=>((string)e.Attribute(Android+"name")??"").StartsWith("com.unity3d.player.UnityPlayer")&&(string)e.Attribute(Android+"enabled")!="false"),"Second Unity Activity renderer enabled.");
            string[] names={"SurfaceArbiter","WallpaperPreferences","WallpaperSettingsProvider","WallpaperRuntimeHost","PixelTrafficWallpaperService","WallpaperSettingsActivity"};
            foreach(string name in names)Need(File.Exists(Path.Combine(path,"src/main/java/com/s20plus/pixeltraffic/unitywallpaper/"+name+".java")),"Native source missing: "+name);
        }
        internal static void ValidateTemplates()
        {
            string directory=Path.Combine(Path.GetTempPath(),"pixel-wallpaper-manifest-"+Guid.NewGuid().ToString("N"));Directory.CreateDirectory(directory);
            try
            {
                string path=Path.Combine(directory,"AndroidManifest.xml");
                File.WriteAllText(path,"<manifest xmlns:android='http://schemas.android.com/apk/res/android'><application><activity android:name='com.unity3d.player.UnityPlayerActivity' android:exported='true'><intent-filter><category android:name='android.intent.category.LAUNCHER'/></intent-filter></activity></application></manifest>");
                Copy(Path.Combine(Templates,"src"),Path.Combine(directory,"src/main/java"));
                string generated=Path.Combine(directory,"src/main/AndroidManifest.xml");Directory.CreateDirectory(Path.GetDirectoryName(generated));File.Copy(path,generated);
                Patch(generated,true);ValidateGenerated(directory);string first=File.ReadAllText(generated);Patch(generated,true);Need(File.ReadAllText(generated)==first,"Manifest patch is not idempotent.");
                XElement metadata=XDocument.Load(Path.Combine(Templates,"res/xml/pixel_traffic_wallpaper.xml")).Root;
                Need((string)metadata.Attribute(Android+"settingsActivity")==Package+"WallpaperSettingsActivity","Wallpaper settings metadata missing.");
            }
            finally{Directory.Delete(directory,true);}
        }
        [Serializable] private sealed class Report{public string result,version,service,launcher,process,authority;}
    }
}
