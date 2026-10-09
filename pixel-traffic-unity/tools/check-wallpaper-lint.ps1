[CmdletBinding()]
param([string]$UnityExe)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$gradleProject = Join-Path $projectRoot 'Library/Bee/Android/Prj/IL2CPP/Gradle'
$reports = Join-Path $projectRoot 'Reports'
New-Item -ItemType Directory -Path $reports -Force | Out-Null
$reportPath = Join-Path $reports 'wallpaper-lint-result.json'
if (Test-Path -LiteralPath $reportPath) { Remove-Item -LiteralPath $reportPath }
# Reuse only generated Java/resources matching the checked-out sources.
$matched = 0
foreach ($pair in @(@('src', 'java'), @('res', 'res'))) {
    $root = Join-Path $projectRoot ('NativeAndroid/' + $pair[0])
    foreach ($file in Get-ChildItem -LiteralPath $root -File -Recurse) {
        $relative = $file.FullName.Substring($root.Length + 1)
        $generated = Join-Path (Join-Path $gradleProject ('unityLibrary/src/main/' + $pair[1])) $relative
        if (-not (Test-Path -LiteralPath $generated) -or
            (Get-FileHash -LiteralPath $generated).Hash -ne (Get-FileHash -LiteralPath $file.FullName).Hash) {
            throw "Generated wallpaper source differs: $relative. BuildApk first."
        }
        $matched++
    }
}
. (Join-Path $PSScriptRoot 'unity-editor.ps1')
$UnityExe = Get-PixelTrafficUnityEditor -ExplicitPath $UnityExe
$androidPlayer = Join-Path (Split-Path -Parent $UnityExe) 'Data/PlaybackEngines/AndroidPlayer'
$previousJava = $env:JAVA_HOME
$previousGradle = $env:GRADLE_USER_HOME
$previousTemp = $env:TEMP
$previousTmp = $env:TMP
try {
    $env:JAVA_HOME = Join-Path $androidPlayer 'OpenJDK'
    if ([Environment]::OSVersion.Platform -eq [PlatformID]::Win32NT) {
        $cache = $env:PIXEL_TRAFFIC_BUILD_CACHE
        if (-not $cache) {
            if ($env:RUNNER_TEMP) { $cache = Join-Path ([IO.Directory]::GetParent($env:RUNNER_TEMP).Parent.FullName) 'PixelTrafficBuildCache' }
            else { $cache = Join-Path ([IO.Path]::GetPathRoot($projectRoot)) 'Unity/PixelTrafficBuildCache' }
        }
        if ($cache -match '[^\x20-\x7e]') { throw 'Use the existing ASCII build cache path.' }
        $env:GRADLE_USER_HOME = Join-Path $cache 'gradle'
        $env:TEMP = Join-Path $cache 'temp'; $env:TMP = $env:TEMP
        New-Item -ItemType Directory -Path $env:GRADLE_USER_HOME, $env:TEMP -Force | Out-Null
    }
    $onWindows = [Environment]::OSVersion.Platform -eq [PlatformID]::Win32NT
    $launcher = @(Get-ChildItem -LiteralPath (Join-Path $androidPlayer 'Tools/gradle/lib') -Filter 'gradle-launcher-*.jar' -File)
    if ($launcher.Count -ne 1) { throw 'Expected one bundled Gradle launcher.' }
    $java = Join-Path $env:JAVA_HOME $(if ($onWindows) { 'bin/java.exe' } else { 'bin/java' })
    $arguments = @('-classpath', $launcher[0].FullName, 'org.gradle.launcher.GradleMain', '--no-daemon', '-p', $gradleProject, ':unityLibrary:lintDebug')
    $preference = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        $output = & $java @arguments 2>&1 | Out-String
        $exitCode = $LASTEXITCODE
    } finally { $ErrorActionPreference = $preference }
    $output | Set-Content -LiteralPath (Join-Path $reports 'wallpaper-lint.log') -Encoding UTF8
    $xml = Join-Path $gradleProject 'unityLibrary/build/reports/lint-results-debug.xml'
    [xml]$lint = Get-Content -LiteralPath $xml -Raw
    $errors = @($lint.issues.issue | Where-Object { $_.severity -in @('Fatal', 'Error') }).Count
    $warnings = @($lint.issues.issue | Where-Object { $_.severity -eq 'Warning' }).Count
    $issues = @($lint.issues.issue | ForEach-Object { [ordered]@{ id = $_.id; severity = $_.severity; message = $_.message } })
    [ordered]@{ result = $(if ($exitCode -eq 0 -and $errors -eq 0) { 'PASS' } else { 'FAILED' }); errors = $errors; warnings = $warnings; task = ':unityLibrary:lintDebug'; nativeFilesMatched = $matched; issues = $issues; deviceTested = $false } |
        ConvertTo-Json -Depth 5 | Set-Content -LiteralPath $reportPath -Encoding UTF8
    if ($exitCode -ne 0 -or $errors -ne 0) { throw 'Wallpaper library Lint failed. See wallpaper-lint-result.json.' }
} finally {
    $env:JAVA_HOME = $previousJava; $env:GRADLE_USER_HOME = $previousGradle
    $env:TEMP = $previousTemp; $env:TMP = $previousTmp
}
