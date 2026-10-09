[CmdletBinding()]
param(
    [string]$BackupPath,
    [Security.SecureString]$StorePassword,
    [Security.SecureString]$KeyPassword,
    [string]$Alias = 'androiddebugkey',
    [string]$UnityExe
)
$ErrorActionPreference = 'Stop'
if ([Environment]::OSVersion.Platform -ne [PlatformID]::Win32NT) {
    throw 'This one-time encrypted signing setup requires the Windows user who runs Unity.'
}
if (-not $BackupPath) {
    Add-Type -AssemblyName System.Windows.Forms
    $picker = New-Object System.Windows.Forms.OpenFileDialog
    try {
        $picker.Title = 'Select the original S20-PLUS signing backup'
        $picker.Filter = 'Signing backup (*.zip;*.keystore)|*.zip;*.keystore'
        if ($picker.ShowDialog() -ne 'OK') { throw 'Signing setup cancelled; no key created.' }
        $BackupPath = $picker.FileName
    } finally { $picker.Dispose() }
}
$expectedKey = '6e3050b987c1baa866c2c98cab3853bc4eee2fe05c6d1f778d3ad6eeece2b66c'
if ([IO.Path]::GetExtension($BackupPath) -ieq '.zip') {
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $archive = [IO.Compression.ZipFile]::OpenRead($BackupPath)
    try {
        $entries = @($archive.Entries | Where-Object { $_.FullName -ceq 'debug.keystore' })
        if ($entries.Count -ne 1 -or $entries[0].Length -gt 32768) { throw 'Invalid original signing backup.' }
        $inputStream = $entries[0].Open()
        $memory = New-Object IO.MemoryStream
        try { $inputStream.CopyTo($memory); $keyBytes = $memory.ToArray() }
        finally { $inputStream.Dispose(); $memory.Dispose() }
    } finally { $archive.Dispose() }
} else {
    if ((Get-Item -LiteralPath $BackupPath).Length -gt 32768) { throw 'Invalid signing key size.' }
    $keyBytes = [IO.File]::ReadAllBytes((Resolve-Path -LiteralPath $BackupPath).Path)
}
$sha = [Security.Cryptography.SHA256]::Create()
try { $actualKey = [BitConverter]::ToString($sha.ComputeHash($keyBytes)).Replace('-', '').ToLowerInvariant() }
finally { $sha.Dispose() }
if ($actualKey -ne $expectedKey) { throw 'Backup differs from the verified original key. Setup stopped.' }
if (-not $StorePassword) { $StorePassword = Read-Host 'Original keystore password (input hidden)' -AsSecureString }
if (-not $KeyPassword) { $KeyPassword = $StorePassword }
. (Join-Path $PSScriptRoot 'unity-editor.ps1')
$UnityExe = Get-PixelTrafficUnityEditor -ExplicitPath $UnityExe
$signingDirectory = Join-Path $env:LOCALAPPDATA 'PixelTraffic/Signing'
New-Item -ItemType Directory -Path $signingDirectory -Force | Out-Null
# Limit the directory to the current licensed Windows user and SYSTEM.
$sid = [Security.Principal.WindowsIdentity]::GetCurrent().User.Value
& icacls.exe $signingDirectory /inheritance:r /grant:r "*$($sid):(OI)(CI)F" '*S-1-5-18:(OI)(CI)F' | Out-Null
if ($LASTEXITCODE -ne 0) { throw 'Could not restrict the local signing directory.' }
$keyPath = Join-Path $signingDirectory 'debug.keystore'
if (Test-Path -LiteralPath $keyPath) {
    if ((Get-FileHash -LiteralPath $keyPath -Algorithm SHA256).Hash.ToLowerInvariant() -ne $expectedKey) {
        throw 'Another signing key already exists; it will not be overwritten.'
    }
} else { [IO.File]::WriteAllBytes($keyPath, $keyBytes) }
$storeCredential = New-Object Management.Automation.PSCredential($Alias, $StorePassword)
$keyCredential = New-Object Management.Automation.PSCredential($Alias, $KeyPassword)
$previousPassword = $env:PIXEL_TRAFFIC_KEYSTORE_PASSWORD
$certificate = Join-Path ([IO.Path]::GetTempPath()) ('pixel-traffic-' + [Guid]::NewGuid().ToString('N') + '.der')
try {
    $env:PIXEL_TRAFFIC_KEYSTORE_PASSWORD = $storeCredential.GetNetworkCredential().Password
    $keytool = Join-Path (Split-Path -Parent $UnityExe) 'Data/PlaybackEngines/AndroidPlayer/OpenJDK/bin/keytool.exe'
    if (-not (Test-Path -LiteralPath $keytool)) { throw 'Unity Android OpenJDK module is missing.' }
    # Windows PowerShell treats native stderr (including keytool success text)
    # as ErrorRecord; determine success from the tool's exit code instead.
    $previousPreference = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        & $keytool -exportcert -keystore $keyPath -alias $Alias -storepass:env PIXEL_TRAFFIC_KEYSTORE_PASSWORD -file $certificate 2>&1 | Out-Null
        $certificateExit = $LASTEXITCODE
    } finally { $ErrorActionPreference = $previousPreference }
    if ($certificateExit -ne 0) { throw 'Original key password or alias verification failed.' }
    $expectedCertificate = 'a6e489adbb1502c8cd77689dde4efefab3a29c5953180e5e1ca61acf58a3aba6'
    if ((Get-FileHash -LiteralPath $certificate -Algorithm SHA256).Hash.ToLowerInvariant() -ne $expectedCertificate) {
        throw 'Original signing certificate verification failed.'
    }
    # Export-Clixml encrypts SecureString using this Windows user's DPAPI.
    $storeCredential | Export-Clixml -LiteralPath (Join-Path $signingDirectory 'store-credential.xml')
    $keyCredential | Export-Clixml -LiteralPath (Join-Path $signingDirectory 'key-credential.xml')
    Write-Host 'PASS: original key/certificate verified; signing credentials saved for this Windows user.'
} finally {
    $env:PIXEL_TRAFFIC_KEYSTORE_PASSWORD = $previousPassword
    if (Test-Path -LiteralPath $certificate) { Remove-Item -LiteralPath $certificate }
}
