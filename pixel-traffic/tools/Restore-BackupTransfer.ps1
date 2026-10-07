param(
    [Parameter(Mandatory = $true)][object]$Packet,
    [string]$Destination = (Join-Path $env:USERPROFILE 'Desktop\블랙\AI\새 폴더')
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Security
$expectedHash = 'bd5dd8d7effd0e58490e08420aada989c56050a8d020c1bbc80e7e2acd67821d'
if ($Packet.format -ne 'rsa-oaep-sha1-blocks-v1' -or
    $Packet.plaintext_bytes -ne 2989 -or
    $Packet.plaintext_sha256 -ne $expectedHash) {
    throw 'Unexpected signing-backup packet.'
}

$outputPath = Join-Path $Destination 'S20-PLUS-0.29.0-signing-backup.zip'
if (Test-Path -LiteralPath $outputPath) {
    if ((Get-FileHash -LiteralPath $outputPath -Algorithm SHA256).Hash -ne $expectedHash) {
        throw 'A different backup already exists; it was not overwritten.'
    }
    Write-Host "Backup already verified: $outputPath"
    return
}

$privatePath = Join-Path $env:LOCALAPPDATA 'PixelTrafficTransfer\transport-private.dpapi'
if (-not (Test-Path -LiteralPath $privatePath)) {
    throw 'Run on the same Windows account and PC that created the transfer public key.'
}
$rsa = New-Object System.Security.Cryptography.RSACryptoServiceProvider(2048)
$rsa.PersistKeyInCsp = $false
$memory = $null
$sha = [System.Security.Cryptography.SHA256]::Create()
try {
    [byte[]]$privateBlob = [System.Security.Cryptography.ProtectedData]::Unprotect(
        [IO.File]::ReadAllBytes($privatePath), $null,
        [System.Security.Cryptography.DataProtectionScope]::CurrentUser)
    $rsa.ImportCspBlob($privateBlob)
    if ($Packet.recipient_modulus_sha256) {
        $publicHash = [BitConverter]::ToString($sha.ComputeHash($rsa.ExportParameters($false).Modulus)).Replace('-', '').ToLowerInvariant()
        if ($publicHash -ne $Packet.recipient_modulus_sha256) {
            throw 'Saved transfer key differs from the public key used for this packet.'
        }
    }
    [int]$blockBytes = $rsa.KeySize / 8
    [byte[]]$encrypted = [Convert]::FromBase64String($Packet.ciphertext_base64)
    if ($Packet.block_bytes -ne $blockBytes -or $encrypted.Length -eq 0 -or
        ($encrypted.Length % $blockBytes) -ne 0) {
        throw 'Encrypted packet is incomplete.'
    }
    $memory = New-Object IO.MemoryStream
    for ($offset = 0; $offset -lt $encrypted.Length; $offset += $blockBytes) {
        [byte[]]$block = New-Object byte[] $blockBytes
        [Array]::Copy($encrypted, $offset, $block, 0, $blockBytes)
        [byte[]]$clear = $rsa.Decrypt($block, $true)
        $memory.Write($clear, 0, $clear.Length)
    }
    [byte[]]$zipBytes = $memory.ToArray()
    $actualHash = [BitConverter]::ToString($sha.ComputeHash($zipBytes)).Replace('-', '').ToLowerInvariant()
    if ($zipBytes.Length -ne 2989 -or $actualHash -ne $expectedHash) {
        throw 'Backup checksum does not match; no file was saved.'
    }
    New-Item -ItemType Directory -Force -Path $Destination | Out-Null
    $file = [IO.File]::Open($outputPath, [IO.FileMode]::CreateNew, [IO.FileAccess]::Write, [IO.FileShare]::None)
    try { $file.Write($zipBytes, 0, $zipBytes.Length) } finally { $file.Dispose() }
    Write-Host "Backup saved and SHA256 verified: $outputPath"
} finally {
    $rsa.Dispose()
    $sha.Dispose()
    if ($memory) { $memory.Dispose() }
}
