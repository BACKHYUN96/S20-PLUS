function Get-PixelTrafficUnityEditor {
    param([string]$ExplicitPath)
    $versionFile = Join-Path (Split-Path -Parent $PSScriptRoot) 'ProjectSettings/ProjectVersion.txt'
    $versionText = Get-Content -LiteralPath $versionFile -Raw
    if ($versionText -notmatch 'm_EditorVersion:\s*(\S+)') { throw 'Unity project version missing.' }
    $version = $Matches[1]
    if (-not $ExplicitPath) { $ExplicitPath = $env:UNITY_EDITOR_PATH }
    if (-not $ExplicitPath) {
        $candidates = @(
            "C:\Program Files\Unity\Hub\Editor\$version\Editor\Unity.exe",
            "D:\Unity\Hub\Editor\$version\Editor\Unity.exe",
            "D:\Unity\Editor\$version\Editor\Unity.exe",
            "D:\Unity\Editors\$version\Editor\Unity.exe",
            "D:\Unity\Unity Hub\Editor\$version\Editor\Unity.exe",
            '/opt/unity/Editor/Unity'
        )
        $ExplicitPath = $candidates | Where-Object { Test-Path -LiteralPath $_ -PathType Leaf } | Select-Object -First 1
    }
    if (-not $ExplicitPath -or -not (Test-Path -LiteralPath $ExplicitPath -PathType Leaf)) {
        throw "Unity $version Editor not found. Set UNITY_EDITOR_PATH or pass -UnityExe (not Unity Hub.exe)."
    }
    return (Resolve-Path -LiteralPath $ExplicitPath).Path
}
