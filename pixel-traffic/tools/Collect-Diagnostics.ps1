param(
    [string]$AdbPath = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe",
    [string]$OutputDir = (Join-Path $PSScriptRoot ("device-report-" + (Get-Date -Format 'yyyyMMdd-HHmmss')))
)
$ErrorActionPreference = 'Stop'
if (-not (Test-Path -LiteralPath $AdbPath -PathType Leaf)) { throw "ADB not found: $AdbPath" }
function Invoke-Device {
    param([string[]]$Arguments)
    $result = & $AdbPath @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) { throw ($result -join "`n") }
    return $result
}
$state = Invoke-Device -Arguments @('get-state')
if (($state -join '').Trim() -ne 'device') { throw 'Connect and authorize exactly one Android device.' }
New-Item -ItemType Directory -Path $OutputDir -ErrorAction Stop | Out-Null
Invoke-Device -Arguments @('shell', 'getprop', 'ro.product.model') | Set-Content -LiteralPath (Join-Path $OutputDir 'model.txt') -Encoding UTF8
Invoke-Device -Arguments @('shell', 'getprop', 'ro.build.version.sdk') | Set-Content -LiteralPath (Join-Path $OutputDir 'android-api.txt') -Encoding UTF8
Invoke-Device -Arguments @('shell', 'dumpsys', 'package', 'com.s20plus.pixeltraffic') | Select-String 'versionCode=|versionName=' | Set-Content -LiteralPath (Join-Path $OutputDir 'version.txt') -Encoding UTF8
Invoke-Device -Arguments @('shell', 'dumpsys', 'meminfo', 'com.s20plus.pixeltraffic') | Set-Content -LiteralPath (Join-Path $OutputDir 'memory.txt') -Encoding UTF8
Invoke-Device -Arguments @('logcat', '-d', '-v', 'time', '-s', 'PixelTraffic:I', '*:S') | Set-Content -LiteralPath (Join-Path $OutputDir 'wallpaper-log.txt') -Encoding UTF8
'Collection only. No installation, root commands, settings changes or log clearing performed.' | Set-Content -LiteralPath (Join-Path $OutputDir 'README.txt') -Encoding UTF8
Write-Output "Saved diagnostics to $OutputDir"
