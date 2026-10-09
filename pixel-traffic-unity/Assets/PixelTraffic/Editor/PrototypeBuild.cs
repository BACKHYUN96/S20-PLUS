using System;
using System.Diagnostics;
using System.IO;
using System.Security.Cryptography;
using UnityEditor;
using UnityEditor.Android;
using UnityEditor.Build.Reporting;
using UnityEngine;
using Debug = UnityEngine.Debug;

namespace PixelTraffic.UnityPrototype.Editor
{
    public static class PrototypeBuild
    {
        private const string CertificateSha256 = "a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6";

        [MenuItem("Pixel Traffic/3. Export Android Project")]
        public static void ExportAndroidProject()
        {
            StarterScene.PrepareBatch();
            bool previous = EditorUserBuildSettings.exportAsGoogleAndroidProject;
            try
            {
                EditorUserBuildSettings.exportAsGoogleAndroidProject = true;
                Build("Builds/AndroidExport", BuildOptions.Development);
                Debug.Log("Android Gradle project exported. WallpaperService integration is the next step; this export is an Activity prototype.");
            }
            finally { EditorUserBuildSettings.exportAsGoogleAndroidProject = previous; }
        }

        [MenuItem("Pixel Traffic/4. Build Signed Activity APK")]
        public static void BuildActivityApk()
        {
            string keystore = RequiredEnvironment("PIXEL_TRAFFIC_KEYSTORE");
            string password = RequiredEnvironment("PIXEL_TRAFFIC_KEYSTORE_PASSWORD");
            string alias = Environment.GetEnvironmentVariable("PIXEL_TRAFFIC_KEY_ALIAS") ?? "androiddebugkey";
            string keyPassword = Environment.GetEnvironmentVariable("PIXEL_TRAFFIC_KEY_PASSWORD") ?? password;
            if (!File.Exists(keystore)) throw new FileNotFoundException("Original signing key not found.", keystore);
            VerifyOriginalCertificate(keystore, alias);
            StarterScene.PrepareBatch();
            bool previousExport = EditorUserBuildSettings.exportAsGoogleAndroidProject;
            bool previousBundle = EditorUserBuildSettings.buildAppBundle;
            bool previousCustom = PlayerSettings.Android.useCustomKeystore;
            string previousKeystore = PlayerSettings.Android.keystoreName;
            string previousAlias = PlayerSettings.Android.keyaliasName;
            string previousStorePass = PlayerSettings.Android.keystorePass;
            string previousKeyPass = PlayerSettings.Android.keyaliasPass;
            string previousGradleHome = AndroidExternalToolsSettings.Gradle.userHomePath;
            try
            {
                string gradleHome = Environment.GetEnvironmentVariable("GRADLE_USER_HOME");
                if (!string.IsNullOrWhiteSpace(gradleHome))
                    AndroidExternalToolsSettings.Gradle.userHomePath = Path.GetFullPath(gradleHome);
                EditorUserBuildSettings.exportAsGoogleAndroidProject = false;
                EditorUserBuildSettings.buildAppBundle = false;
                PlayerSettings.Android.useCustomKeystore = true;
                PlayerSettings.Android.keystoreName = Path.GetFullPath(keystore);
                PlayerSettings.Android.keystorePass = password;
                PlayerSettings.Android.keyaliasName = alias;
                PlayerSettings.Android.keyaliasPass = keyPassword;
                const string output = "Builds/pixel-traffic-unity-prototype-0.1.0.apk";
                Build(output, BuildOptions.Development);
                Debug.Log("Activity prototype APK built with original certificate. This is a separate test app, not a wallpaper or a 0.48 update: " + output);
            }
            finally
            {
                EditorUserBuildSettings.exportAsGoogleAndroidProject = previousExport;
                EditorUserBuildSettings.buildAppBundle = previousBundle;
                PlayerSettings.Android.useCustomKeystore = previousCustom;
                PlayerSettings.Android.keystoreName = previousKeystore;
                PlayerSettings.Android.keyaliasName = previousAlias;
                PlayerSettings.Android.keystorePass = previousStorePass;
                PlayerSettings.Android.keyaliasPass = previousKeyPass;
                AndroidExternalToolsSettings.Gradle.userHomePath = previousGradleHome;
            }
        }

        private static void Build(string output, BuildOptions options)
        {
            StarterScene.ConfigurePlayer();
            Directory.CreateDirectory("Builds");
            BuildReport report = BuildPipeline.BuildPlayer(new BuildPlayerOptions {
                scenes = new[] { StarterConfig.ScenePath }, locationPathName = output,
                target = BuildTarget.Android, options = options
            });
            Directory.CreateDirectory("Reports");
            File.WriteAllText("Reports/android-build-result.txt", "Editor: " + Application.unityVersion +
                "\nApp ID: " + StarterConfig.ExperimentAppId + "\nResult: " + report.summary.result +
                "\nErrors: " + report.summary.totalErrors + "\nWarnings: " + report.summary.totalWarnings +
                "\nOutput: " + output + "\nActivity prototype; no WallpaperService yet.\n");
            if (report.summary.result != BuildResult.Succeeded)
                throw new InvalidOperationException("Android build failed; inspect Reports/android-build-result.txt and Editor log.");
        }

        private static string RequiredEnvironment(string name)
        {
            string value = Environment.GetEnvironmentVariable(name);
            if (string.IsNullOrWhiteSpace(value))
                throw new InvalidOperationException("Set " + name + " before starting Unity; signing credentials are not bundled in the project.");
            return value;
        }

        private static void VerifyOriginalCertificate(string keystore, string alias)
        {
            string executable = Path.Combine(AndroidExternalToolsSettings.jdkRootPath, "bin", Application.platform == RuntimePlatform.WindowsEditor ? "keytool.exe" : "keytool");
            if (!File.Exists(executable)) throw new FileNotFoundException("Unity OpenJDK keytool missing; check Android modules.");
            string temporary = Path.Combine(Path.GetTempPath(), "pixel-traffic-" + Guid.NewGuid().ToString("N") + ".der");
            try
            {
                var start = new ProcessStartInfo {
                    FileName = executable,
                    Arguments = "-exportcert -keystore " + Quote(Path.GetFullPath(keystore)) +
                        " -alias " + Quote(alias) + " -storepass:env PIXEL_TRAFFIC_KEYSTORE_PASSWORD -file " + Quote(temporary),
                    UseShellExecute = false, CreateNoWindow = true,
                    RedirectStandardOutput = true, RedirectStandardError = true
                };
                using (Process process = Process.Start(start))
                {
                    if (process == null) throw new InvalidOperationException("Could not start Unity keytool.");
                    // Drain both pipes concurrently; certificate/tool failure logs contain no password argument.
                    var stdout = process.StandardOutput.ReadToEndAsync();
                    var stderr = process.StandardError.ReadToEndAsync();
                    if (!process.WaitForExit(30000))
                    {
                        process.Kill();
                        throw new TimeoutException("Certificate verification timed out.");
                    }
                    if (process.ExitCode != 0)
                        throw new InvalidOperationException("Signing certificate export failed; check key/alias/password locally.");
                    System.Threading.Tasks.Task.WaitAll(stdout, stderr);
                }
                using (SHA256 sha = SHA256.Create())
                {
                    string actual = BitConverter.ToString(sha.ComputeHash(File.ReadAllBytes(temporary))).Replace("-", "").ToLowerInvariant();
                    if (actual != CertificateSha256) throw new InvalidOperationException("Signing certificate differs from the installed Pixel Traffic key; build stopped.");
                }
            }
            finally { if (File.Exists(temporary)) File.Delete(temporary); }
        }

        private static string Quote(string value)
        {
            if (value.Contains("\"") || value.Contains("\r") || value.Contains("\n") || value.EndsWith("\\"))
                throw new ArgumentException("Unsupported quote/newline/trailing slash in keytool argument.");
            return "\"" + value + "\"";
        }
    }
}
