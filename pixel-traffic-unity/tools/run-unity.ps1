[CmdletBinding()]
param(
    [ValidateSet('Open', 'Prepare', 'Validate', 'ExportAndroid', 'BuildApk')]
    [string]$Mode = 'Open',
    [string]$UnityExe
)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
. (Join-Path $PSScriptRoot 'unity-editor.ps1')
$UnityExe = Get-PixelTrafficUnityEditor -ExplicitPath $UnityExe
New-Item -ItemType Directory -Path (Join-Path $projectRoot 'Reports') -Force | Out-Null
$logPath = Join-Path (Join-Path $projectRoot 'Reports') "unity-$Mode.log"
$unityArguments = @('-projectPath', $projectRoot, '-logFile', $logPath)
if ($Mode -ne 'Open') {
    $methods = @{
        Prepare = 'PixelTraffic.UnityPrototype.Editor.StarterScene.PrepareBatch'
        # Fresh checkouts do not contain generated scenes. PrepareBatch also validates.
        Validate = 'PixelTraffic.UnityPrototype.Editor.StarterScene.PrepareBatch'
        ExportAndroid = 'PixelTraffic.UnityPrototype.Editor.PrototypeBuild.ExportAndroidProject'
        BuildApk = 'PixelTraffic.UnityPrototype.Editor.PrototypeBuild.BuildActivityApk'
    }
    $unityArguments += @('-batchmode', '-nographics', '-quit', '-executeMethod', $methods[$Mode])
    if ($Mode -eq 'ExportAndroid' -or $Mode -eq 'BuildApk') {
        $unityArguments += @('-buildTarget', 'Android')
    }
    # Windows GUI executables may leave LASTEXITCODE unset. Wait on this exact
    # process and read its exit code; do not clear signing state while Unity runs.
    $startInfo = New-Object System.Diagnostics.ProcessStartInfo
    $startInfo.FileName = $UnityExe
    $startInfo.WorkingDirectory = $projectRoot
    $startInfo.UseShellExecute = $false
    if ($startInfo.PSObject.Properties['ArgumentList']) {
        foreach ($argument in $unityArguments) { $startInfo.ArgumentList.Add($argument) }
    } else {
        # Windows PowerShell 5.1 uses a single command-line string. Preserve
        # spaces, Unicode, quotes, and trailing backslashes using CRT quoting.
        $quotedArguments = foreach ($argument in $unityArguments) {
            '"' + ([regex]::Replace($argument, '(\\*)"', '$1$1\"') -replace '(\\+)$', '$1$1') + '"'
        }
        $startInfo.Arguments = $quotedArguments -join ' '
    }
    $unityProcess = [System.Diagnostics.Process]::Start($startInfo)
    if ($null -eq $unityProcess) { throw 'Could not start Unity Editor.' }
    try {
        Write-Host "Unity $Mode running (PID $($unityProcess.Id)). Log: $logPath"
        $unityProcess.WaitForExit()
        $unityExitCode = $unityProcess.ExitCode
    } finally { $unityProcess.Dispose() }
    if ($unityExitCode -ne 0) { throw "Unity $Mode failed (exit $unityExitCode). See $logPath" }
    Write-Host "Unity $Mode completed. Log: $logPath"
} else {
    & $UnityExe @unityArguments
}
