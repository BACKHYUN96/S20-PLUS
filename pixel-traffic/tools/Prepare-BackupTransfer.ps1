Add-Type -AssemblyName System.Security
$transferDir = Join-Path $env:LOCALAPPDATA 'PixelTrafficTransfer'
New-Item -ItemType Directory -Force -Path $transferDir | Out-Null
$privatePath = Join-Path $transferDir 'transport-private.dpapi'
$rsa = New-Object System.Security.Cryptography.RSACryptoServiceProvider(2048)
$rsa.PersistKeyInCsp = $false
try {
    if (Test-Path -LiteralPath $privatePath) {
        $privateBlob = [System.Security.Cryptography.ProtectedData]::Unprotect([IO.File]::ReadAllBytes($privatePath), $null, [System.Security.Cryptography.DataProtectionScope]::CurrentUser)
        $rsa.ImportCspBlob($privateBlob)
    } else {
        $protectedBlob = [System.Security.Cryptography.ProtectedData]::Protect($rsa.ExportCspBlob($true), $null, [System.Security.Cryptography.DataProtectionScope]::CurrentUser)
        [IO.File]::WriteAllBytes($privatePath, $protectedBlob)
    }
    $rsa.ToXmlString($false)
} finally {
    $rsa.Dispose()
}
