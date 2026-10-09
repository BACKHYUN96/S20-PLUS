[CmdletBinding()]
param(
    [ValidateSet('Validate', 'BuildApk')]
    [string]$Operation = 'Validate',
    [string]$UnityExe
)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$reports = Join-Path $projectRoot 'Reports'
New-Item -ItemType Directory -Path $reports -Force | Out-Null
$summary = [ordered]@{
    schemaVersion = 1; operation = $Operation; result = 'FAILED'
    startedUtc = [DateTime]::UtcNow.ToString('O'); finishedUtc = $null
    wallpaper = $false
}
$temporaryKey = $null
$previousKey = $env:PIXEL_TRAFFIC_KEYSTORE
$previousJava = $env:JAVA_HOME
$previousKeyPassword = $env:PIXEL_TRAFFIC_KEY_PASSWORD
$previousStorePassword = $env:PIXEL_TRAFFIC_KEYSTORE_PASSWORD
$previousAlias = $env:PIXEL_TRAFFIC_KEY_ALIAS
$previousGradleHome = $env:GRADLE_USER_HOME
$previousTemp = $env:TEMP
$previousTmp = $env:TMP
function Invoke-AndroidTool {
    param([string]$Tool, [string[]]$ToolArguments, [string]$OutputPath)
    $previousPreference = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        $toolOutput = & $Tool @ToolArguments 2>&1 | Out-String
        $toolExit = $LASTEXITCODE
    } finally { $ErrorActionPreference = $previousPreference }
    if ($OutputPath) {
        $toolOutput | Set-Content -LiteralPath $OutputPath -Encoding UTF8
        if ($toolExit -ne 0) {
            & (Join-Path $PSScriptRoot 'collect-build-failure.ps1') -LogPath $OutputPath `
                -OutputPath (Join-Path $reports 'verification-failure.json') -SourceRevision $env:GITHUB_SHA
        }
    }
    if ($toolExit -ne 0) { throw "Android verification tool failed: $(Split-Path -Leaf $Tool)" }
    return $toolOutput
}
try {
    foreach ($reportName in @('scene-validation.json', 'apk-verification.json', 'android-build-result.txt', 'android-lint-result.json', 'verification-failure.json', 'android-lint.log')) {
        $oldReport = Join-Path $reports $reportName
        if (Test-Path -LiteralPath $oldReport) { Remove-Item -LiteralPath $oldReport }
    }
    . (Join-Path $PSScriptRoot 'unity-editor.ps1')
    $UnityExe = Get-PixelTrafficUnityEditor -ExplicitPath $UnityExe
    if ($Operation -eq 'BuildApk') {
        if ([Environment]::OSVersion.Platform -eq [PlatformID]::Win32NT) {
            # Android Prefab's Windows batch file cannot reliably use a Unicode
            # Gradle profile/temp path. Keep these build-only paths in ASCII.
            $cacheRoot = $env:PIXEL_TRAFFIC_BUILD_CACHE
            if (-not $cacheRoot) {
                if ($env:RUNNER_TEMP) {
                    $cacheRoot = Join-Path ([IO.Directory]::GetParent($env:RUNNER_TEMP).Parent.FullName) 'PixelTrafficBuildCache'
                } else { $cacheRoot = Join-Path ([IO.Path]::GetPathRoot($projectRoot)) 'Unity/PixelTrafficBuildCache' }
            }
            $cacheRoot = [IO.Path]::GetFullPath($cacheRoot)
            if ($cacheRoot -match '[^\x20-\x7e]') { throw 'PIXEL_TRAFFIC_BUILD_CACHE must use an ASCII Windows path.' }
            $gradleHome = Join-Path $cacheRoot 'gradle'
            $temporaryDirectory = Join-Path $cacheRoot 'temp'
            New-Item -ItemType Directory -Path $gradleHome, $temporaryDirectory -Force | Out-Null
            # Reuse downloaded dependencies without moving/deleting the user's cache.
            $oldHome = $previousGradleHome
            if (-not $oldHome) { $oldHome = Join-Path $env:USERPROFILE '.gradle' }
            $oldModules = Join-Path $oldHome 'caches/modules-2'
            $newModules = Join-Path $gradleHome 'caches/modules-2'
            if ((Test-Path -LiteralPath $oldModules) -and -not (Test-Path -LiteralPath $newModules)) {
                $preference = $ErrorActionPreference
                try {
                    $ErrorActionPreference = 'Continue'
                    & robocopy.exe $oldModules $newModules /E /XF '*.lock' /NFL /NDL /NJH /NJS /R:1 /W:1 | Out-Null
                    $copyExit = $LASTEXITCODE
                } finally { $ErrorActionPreference = $preference }
                if ($copyExit -ge 8) { throw 'Could not reuse the existing Gradle dependency cache.' }
            }
            $env:GRADLE_USER_HOME = $gradleHome
            $env:TEMP = $temporaryDirectory
            $env:TMP = $temporaryDirectory
        }
        if ([string]::IsNullOrEmpty($env:PIXEL_TRAFFIC_KEY_PASSWORD)) { $env:PIXEL_TRAFFIC_KEY_PASSWORD = $null }
        # Optional cloud secret; a PC can instead keep its original key outside the checkout.
        if ($env:PIXEL_TRAFFIC_KEYSTORE_BASE64) {
            $temporaryKey = Join-Path ([IO.Path]::GetTempPath()) ("pixel-traffic-signing-" + [Guid]::NewGuid().ToString('N') + '.keystore')
            [IO.File]::WriteAllBytes($temporaryKey, [Convert]::FromBase64String($env:PIXEL_TRAFFIC_KEYSTORE_BASE64))
            $env:PIXEL_TRAFFIC_KEYSTORE = $temporaryKey
        } elseif (-not $env:PIXEL_TRAFFIC_KEYSTORE -or -not $env:PIXEL_TRAFFIC_KEYSTORE_PASSWORD) {
            # Actions' empty secret env entries hide inherited runner passwords.
            # Read the one-time DPAPI setup instead; no secrets enter the checkout.
            if ([Environment]::OSVersion.Platform -ne [PlatformID]::Win32NT) {
                throw 'Configure the original signing key and password in this runner environment.'
            }
            $signingDirectory = Join-Path $env:LOCALAPPDATA 'PixelTraffic/Signing'
            $localKey = Join-Path $signingDirectory 'debug.keystore'
            $storeCredential = Import-Clixml -LiteralPath (Join-Path $signingDirectory 'store-credential.xml')
            $keyCredential = Import-Clixml -LiteralPath (Join-Path $signingDirectory 'key-credential.xml')
            if ($storeCredential -isnot [Management.Automation.PSCredential] -or
                $keyCredential -isnot [Management.Automation.PSCredential] -or
                (Get-FileHash -LiteralPath $localKey -Algorithm SHA256).Hash.ToLowerInvariant() -ne
                    '6e3050b987c1baa866c2c98cab3853bc4eee2fe05c6d1f778d3ad6eeece2b66c') {
                throw 'Local encrypted signing setup is invalid; original key required.'
            }
            if (-not $env:PIXEL_TRAFFIC_KEYSTORE) { $env:PIXEL_TRAFFIC_KEYSTORE = $localKey }
            if (-not $env:PIXEL_TRAFFIC_KEYSTORE_PASSWORD) {
                $env:PIXEL_TRAFFIC_KEYSTORE_PASSWORD = $storeCredential.GetNetworkCredential().Password
            }
            if (-not $env:PIXEL_TRAFFIC_KEY_ALIAS) { $env:PIXEL_TRAFFIC_KEY_ALIAS = $storeCredential.UserName }
            if (-not $env:PIXEL_TRAFFIC_KEY_PASSWORD) {
                $env:PIXEL_TRAFFIC_KEY_PASSWORD = $keyCredential.GetNetworkCredential().Password
            }
        }
        $apk = Join-Path $projectRoot 'Builds/pixel-traffic-unity-prototype-0.1.0.apk'
        # Prevent a previous successful APK from masquerading as this run's output.
        if (Test-Path -LiteralPath $apk) { Remove-Item -LiteralPath $apk }
    }
    & (Join-Path $PSScriptRoot 'run-unity.ps1') -Mode $Operation -UnityExe $UnityExe
    if ($Operation -eq 'BuildApk') {
        $editorDirectory = Split-Path -Parent $UnityExe
        $androidPlayer = Join-Path $editorDirectory 'Data/PlaybackEngines/AndroidPlayer'
        $sdk = $env:UNITY_ANDROID_SDK_PATH
        if (-not $sdk) { $sdk = Join-Path $androidPlayer 'SDK' }
        $env:JAVA_HOME = Join-Path $androidPlayer 'OpenJDK'
        $gradleProject = Join-Path $projectRoot 'Library/Bee/Android/Prj/IL2CPP/Gradle'
        if (-not (Test-Path -LiteralPath (Join-Path $gradleProject 'launcher/build.gradle'))) {
            throw 'Generated Unity Android Gradle project missing; Lint cannot run.'
        }
        $onWindows = [Environment]::OSVersion.Platform -eq [PlatformID]::Win32NT
        $gradle = Join-Path $androidPlayer $(if ($onWindows) { 'Tools/gradle/bin/gradle.bat' } else { 'Tools/gradle/bin/gradle' })
        $gradleArguments = @('--no-daemon', '-p', $gradleProject, ':launcher:lintDebug')
        if (-not (Test-Path -LiteralPath $gradle)) {
            # Unity may ship Gradle libraries without the distribution's bin script.
            # Run that same bundled Gradle with Unity's JDK, as the Editor does.
            $gradleLibraries = Join-Path $androidPlayer 'Tools/gradle/lib'
            if (-not (Test-Path -LiteralPath $gradleLibraries)) {
                throw 'Unity bundled Gradle launcher missing; Lint cannot run.'
            }
            $launchers = @(Get-ChildItem -LiteralPath $gradleLibraries -Filter 'gradle-launcher-*.jar' -File)
            if ($launchers.Count -ne 1) { throw 'Expected exactly one Unity bundled Gradle launcher.' }
            $gradle = Join-Path $env:JAVA_HOME $(if ($onWindows) { 'bin/java.exe' } else { 'bin/java' })
            $gradleArguments = @('-classpath', $launchers[0].FullName, 'org.gradle.launcher.GradleMain') + $gradleArguments
        }
        $lintOutput = Invoke-AndroidTool $gradle $gradleArguments -OutputPath (Join-Path $reports 'android-lint.log')
        $lintXml = Join-Path $gradleProject 'launcher/build/reports/lint-results-debug.xml'
        [xml]$lintReport = Get-Content -LiteralPath $lintXml -Raw
        $lintErrors = @($lintReport.issues.issue | Where-Object { $_.severity -in @('Fatal', 'Error') }).Count
        $lintWarnings = @($lintReport.issues.issue | Where-Object { $_.severity -eq 'Warning' }).Count
        [ordered]@{ result = $(if ($lintErrors -eq 0) { 'PASS' } else { 'FAILED' }); errors = $lintErrors; warnings = $lintWarnings; task = ':launcher:lintDebug' } |
            ConvertTo-Json | Set-Content -LiteralPath (Join-Path $reports 'android-lint-result.json') -Encoding UTF8
        if ($lintErrors -ne 0) {
            & (Join-Path $PSScriptRoot 'collect-build-failure.ps1') -LogPath $lintXml `
                -OutputPath (Join-Path $reports 'verification-failure.json') -SourceRevision $env:GITHUB_SHA
            throw 'Android Lint reported errors; APK will not be published.'
        }
        $buildTools = Get-ChildItem -LiteralPath (Join-Path $sdk 'build-tools') -Directory |
            Where-Object { $_.Name -match '^\d+\.\d+\.\d+$' } |
            Sort-Object { [version]$_.Name } -Descending | Select-Object -First 1
        if (-not $buildTools) { throw 'Android SDK build-tools not found.' }
        $signer = Join-Path $buildTools.FullName $(if ($onWindows) { 'apksigner.bat' } else { 'apksigner' })
        $aapt = Join-Path $buildTools.FullName $(if ($onWindows) { 'aapt.exe' } else { 'aapt' })
        $signature = Invoke-AndroidTool $signer @('verify', '--verbose', '--print-certs', $apk)
        $expectedCertificate = 'a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6'
        if ($signature -notmatch 'Verified using v2 scheme[^\r\n]*true' -or
            $signature -notmatch "Signer #1 certificate SHA-256 digest: $expectedCertificate") {
            throw 'APK v2/original signing certificate verification failed.'
        }
        $badging = Invoke-AndroidTool $aapt @('dump', 'badging', $apk)
        if ($badging -notmatch "package: name='com\.s20plus\.pixeltraffic\.unityprototype' versionCode='1' versionName='0\.1\.0'" -or
            $badging -notmatch "(?m)^sdkVersion:'29'\s*$" -or
            $badging -notmatch "(?m)^native-code: 'arm64-v8a'\s*$") {
            throw 'APK app ID/version/min SDK/ARM64 verification failed.'
        }
        if ($badging -notmatch "targetSdkVersion:'(\d+)'" -or [int]$Matches[1] -lt 29) {
            throw 'APK target SDK verification failed.'
        }
        $verification = [ordered]@{
            result = 'PASS'; version = '0.1.0'; versionCode = 1; minSdk = 29
            targetSdk = [int]$Matches[1]; architecture = 'arm64-v8a'
            certificateSha256 = $expectedCertificate; v2 = $true
            apkSha256 = (Get-FileHash -LiteralPath $apk -Algorithm SHA256).Hash.ToLowerInvariant()
            bytes = (Get-Item -LiteralPath $apk).Length; wallpaper = $false
        }
        $verification | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $reports 'apk-verification.json') -Encoding UTF8
    }
    $summary.result = 'PASS'
} finally {
    $summary.finishedUtc = [DateTime]::UtcNow.ToString('O')
    $summary | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $reports 'pipeline-result.json') -Encoding UTF8
    if ($temporaryKey -and (Test-Path -LiteralPath $temporaryKey)) { Remove-Item -LiteralPath $temporaryKey -Force }
    $env:PIXEL_TRAFFIC_KEYSTORE = $previousKey
    $env:JAVA_HOME = $previousJava
    $env:PIXEL_TRAFFIC_KEY_PASSWORD = $previousKeyPassword
    $env:PIXEL_TRAFFIC_KEYSTORE_PASSWORD = $previousStorePassword
    $env:PIXEL_TRAFFIC_KEY_ALIAS = $previousAlias
    $env:GRADLE_USER_HOME = $previousGradleHome
    $env:TEMP = $previousTemp
    $env:TMP = $previousTmp
}
