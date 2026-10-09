[CmdletBinding()]
param([string]$LogPath, [string]$OutputPath, [string]$SourceRevision = '')
$ErrorActionPreference = 'Stop'
if (-not (Test-Path -LiteralPath $LogPath)) { return }
$redactions = New-Object 'Collections.Generic.List[string]'
foreach ($name in @('PIXEL_TRAFFIC_KEYSTORE_PASSWORD', 'PIXEL_TRAFFIC_KEY_PASSWORD', 'PIXEL_TRAFFIC_KEYSTORE', 'USERPROFILE', 'LOCALAPPDATA', 'USERNAME', 'GITHUB_WORKSPACE', 'RUNNER_TEMP')) {
    $value = [Environment]::GetEnvironmentVariable($name)
    if ($value) { $redactions.Add($value) }
}
if ($env:LOCALAPPDATA) {
    foreach ($file in @('store-credential.xml', 'key-credential.xml')) {
        $credentialPath = Join-Path (Join-Path $env:LOCALAPPDATA 'PixelTraffic/Signing') $file
        if (Test-Path -LiteralPath $credentialPath) {
            # Fail closed if credentials cannot be decrypted for redaction.
            $credential = Import-Clixml -LiteralPath $credentialPath
            $password = $credential.GetNetworkCredential().Password
            if ($password) { $redactions.Add($password) }
        }
    }
}
$lines = @(Get-Content -LiteralPath $LogPath)
$indexes = New-Object 'Collections.Generic.SortedSet[int]'
for ($i = 0; $i -lt $lines.Count; $i++) {
    if ($lines[$i] -match '(?i)(error:|\berror\b|failed|failure|exception|what went wrong|caused by|could not|SDK.*missing|NDK.*missing)') {
        for ($j = [Math]::Max(0, $i - 1); $j -le [Math]::Min($lines.Count - 1, $i + 4); $j++) { [void]$indexes.Add($j) }
    }
}
$excerpt = foreach ($index in $indexes | Select-Object -Last 180) {
    $line = $lines[$index]
    foreach ($value in $redactions) {
        $line = $line.Replace($value, '[redacted]').Replace($value.Replace('\', '/'), '[redacted]')
    }
    $line = $line -replace '(?i)((?:storePassword|keyPassword|password|token|authorization)[\s:=]+)[^\s,;]+', '$1[redacted]'
    $line = $line -replace '(?i)[A-Z]:[\\/]Users[\\/][^\\/\s]+', '[user]'
    if ($line.Length -gt 1500) { $line = $line.Substring(0, 1500) }
    $line
}
[ordered]@{ sourceRevision = $SourceRevision; capturedUtc = [DateTime]::UtcNow.ToString('O'); errors = @($excerpt) } |
    ConvertTo-Json -Depth 3 | Set-Content -LiteralPath $OutputPath -Encoding UTF8
