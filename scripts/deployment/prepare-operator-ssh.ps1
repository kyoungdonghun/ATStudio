#Requires -Version 7.0
<#!
.SYNOPSIS
Prepare a new operator-owned Ed25519 key after independent MA review.
.DESCRIPTION
Run locally in an owner-operated console, never an agent terminal or transcript.
Enter a nonempty passphrase only at the native ssh-keygen hidden prompts.
CheckOnly performs metadata preflight only; it never reads or generates keys.
Existing destinations are refused, including incomplete previous attempts.
WI-20261010-ATS-003. No cloud registration, networking, or payment operations.
#>
[CmdletBinding()]
param([switch]$CheckOnly)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Assert-NoReparsePoint([string]$Path) {
    $cursor = [IO.Path]::GetFullPath($Path)
    while ($cursor) {
        try {
            $attributes = [IO.File]::GetAttributes($cursor)
            if ($attributes -band [IO.FileAttributes]::ReparsePoint) {
                throw 'Reparse-point paths are not allowed.'
            }
        }
        catch [IO.FileNotFoundException] { }
        catch [IO.DirectoryNotFoundException] { }
        $cursor = [IO.Path]::GetDirectoryName($cursor)
    }
}

function Assert-UserOnlyAcl([string]$Path, [Security.Principal.SecurityIdentifier]$Sid) {
    Assert-NoReparsePoint $Path
    $acl = Get-Acl -LiteralPath $Path
    $rules = @($acl.GetAccessRules($true, $true, [Security.Principal.SecurityIdentifier]))
    if ($acl.GetOwner([Security.Principal.SecurityIdentifier]) -ne $Sid -or
        $rules.Count -eq 0) { throw 'Owner-only ACL verification failed.' }
    foreach ($rule in $rules) {
        if ($rule.IdentityReference -ne $Sid -or
            $rule.AccessControlType -ne [Security.AccessControl.AccessControlType]::Allow) {
            throw 'Owner-only ACL verification failed.'
        }
    }
}

function Read-HeaderString([byte[]]$Bytes, [ref]$Offset) {
    $position = [int]$Offset.Value
    if ($position + 4 -gt $Bytes.Length) { throw 'Invalid key header.' }
    [uint32]$length = ([uint32]$Bytes[$position] * 16777216) +
        ([uint32]$Bytes[$position + 1] * 65536) +
        ([uint32]$Bytes[$position + 2] * 256) + $Bytes[$position + 3]
    $position += 4
    if ($length -gt 32 -or $position + $length -gt $Bytes.Length) {
        throw 'Invalid key header.'
    }
    $Offset.Value = $position + [int]$length
    return [Text.Encoding]::ASCII.GetString($Bytes, $position, [int]$length)
}

function Assert-EncryptedHeader([string]$Path) {
    # Only a fixed prefix is decoded: never load, decrypt, or output key material.
    $reader = [IO.StreamReader]::new($Path, [Text.Encoding]::ASCII, $false, 128)
    try {
        $characters = [char[]]::new(192)
        $count = $reader.ReadBlock($characters, 0, $characters.Length)
        $prefix = [string]::new($characters, 0, $count)
    }
    finally { $reader.Dispose() }
    $marker = "-----BEGIN OPENSSH PRIVATE KEY-----`n"
    $prefix = $prefix.Replace("`r`n", "`n")
    if (-not $prefix.StartsWith($marker, [StringComparison]::Ordinal)) {
        throw 'Invalid OpenSSH key header.'
    }
    $encoded = $prefix.Substring($marker.Length).Replace("`n", '')
    if ($encoded.Length -lt 96 -or $encoded.Substring(0, 96) -cnotmatch '^[A-Za-z0-9+/]{96}$') {
        throw 'Invalid OpenSSH key header.'
    }
    $header = [Convert]::FromBase64String($encoded.Substring(0, 96))
    if ([Text.Encoding]::ASCII.GetString($header, 0, 15) -cne "openssh-key-v1`0") {
        throw 'Invalid OpenSSH key header.'
    }
    $offset = 15
    $cipher = Read-HeaderString $header ([ref]$offset)
    $kdf = Read-HeaderString $header ([ref]$offset)
    if ($cipher -cne 'aes256-ctr' -or $kdf -cne 'bcrypt') {
        throw 'NOT READY: encrypted key required. Files preserved; do not register.'
    }
}

try {
    if (-not $IsWindows) { throw 'Windows PowerShell 7 is required.' }
    $sshKeygen = 'C:\Windows\System32\OpenSSH\ssh-keygen.exe'
    $profilePath = [Environment]::GetFolderPath([Environment+SpecialFolder]::UserProfile)
    if ($profilePath -notmatch '^[A-Za-z]:\\') { throw 'A local Windows profile is required.' }
    $sshDirectory = Join-Path $profilePath '.ssh'
    $destination = Join-Path $sshDirectory 'atstudio-iwinv'
    $privatePath = Join-Path $destination 'id_ed25519'
    $publicPath = "$privatePath.pub"
    Assert-NoReparsePoint $destination
    Assert-NoReparsePoint $sshKeygen
    if (-not [IO.File]::Exists($sshKeygen)) { throw 'Windows OpenSSH is unavailable.' }
    if (-not [IO.Directory]::Exists($sshDirectory)) {
        throw 'Existing .ssh parent required. Ask MA to review its creation separately.'
    }
    if (Test-Path -LiteralPath $destination) {
        throw 'Destination already exists. Nothing will be reused, read, overwritten, or deleted.'
    }
    if (Test-Path Env:SSH_ASKPASS) { throw 'Remove SSH_ASKPASS from this console before proceeding.' }
    if (Test-Path Env:SSH_ASKPASS_REQUIRE) { throw 'Remove SSH_ASKPASS_REQUIRE from this console before proceeding.' }
    if ($CheckOnly) {
        Write-Output 'PREFLIGHT PASS: no writes or key reads. No key is ready or registered.'
        return
    }
    if ([Console]::IsInputRedirected -or [Console]::IsOutputRedirected -or
        [Console]::IsErrorRedirected -or $Host.Name -ne 'ConsoleHost') {
        throw 'Use a separate owner-operated interactive console after MA review.'
    }

    $sid = [Security.Principal.WindowsIdentity]::GetCurrent().User
    $security = [Security.AccessControl.DirectorySecurity]::new()
    $security.SetOwner($sid)
    $security.SetAccessRuleProtection($true, $false)
    $security.AddAccessRule([Security.AccessControl.FileSystemAccessRule]::new(
        $sid, 'FullControl', 'ContainerInherit, ObjectInherit', 'None', 'Allow'))

    # CreateDirectoryW applies the protected ACL at creation and fails if it exists.
    if (-not ('ATStudio.OperatorKeyDirectory' -as [type])) {
        Add-Type -TypeDefinition @'
using System;
using System.Runtime.InteropServices;
namespace ATStudio {
    public static class OperatorKeyDirectory {
        [StructLayout(LayoutKind.Sequential)]
        private struct Attributes {
            public int Length;
            public IntPtr Descriptor;
            public int Inherit;
        }
        [DllImport("kernel32.dll", CharSet = CharSet.Unicode, SetLastError = true)]
        private static extern bool CreateDirectoryW(string path, ref Attributes attributes);
        public static bool CreateNew(string path, byte[] descriptor) {
            var pin = GCHandle.Alloc(descriptor, GCHandleType.Pinned);
            try {
                var attributes = new Attributes {
                    Length = Marshal.SizeOf<Attributes>(), Descriptor = pin.AddrOfPinnedObject()
                };
                return CreateDirectoryW(path, ref attributes);
            } finally { pin.Free(); }
        }
    }
}
'@
    }
    Assert-NoReparsePoint $destination
    if (-not [ATStudio.OperatorKeyDirectory]::CreateNew(
        $destination, $security.GetSecurityDescriptorBinaryForm())) {
        throw 'Dedicated directory creation refused. Nothing was overwritten.'
    }
    Assert-UserOnlyAcl $destination $sid
    if (-not (Get-Acl -LiteralPath $destination).AreAccessRulesProtected) {
        throw 'Directory ACL inheritance was not disabled.'
    }
    if ((Test-Path -LiteralPath $privatePath) -or (Test-Path -LiteralPath $publicPath)) {
        throw 'Unexpected destination contents. Refusing key generation.'
    }
    Write-Host 'Enter a NONEMPTY passphrase twice at the native hidden prompts.'
    Write-Host 'Do not share it with MA. Blank input will fail readiness; files will be preserved.'
    & $sshKeygen -q -t ed25519 -a 100 -f $privatePath -C 'atstudio-iwinv'
    if ($LASTEXITCODE -ne 0) { throw 'Native key generation did not finish successfully.' }
    Assert-UserOnlyAcl $privatePath $sid
    Assert-UserOnlyAcl $publicPath $sid
    Assert-EncryptedHeader $privatePath
    $fingerprint = & $sshKeygen -l -E sha256 -f $publicPath 2>$null
    if ($LASTEXITCODE -ne 0 -or @($fingerprint).Count -ne 1 -or
        $fingerprint -cnotmatch '^256 (SHA256:[A-Za-z0-9+/]{43}) atstudio-iwinv \(ED25519\)$') {
        throw 'Public Ed25519 fingerprint verification failed.'
    }
    Write-Output 'LOCAL KEY READY: ACL and encrypted header checked; registration and login not tested.'
    Write-Output "Public fingerprint: $($Matches[1])"
    Write-Output "Public key path: $publicPath"
}
catch {
    Write-Error 'NOT READY. Stop for MA review; any created files are preserved. No registration or purchase was performed.' -ErrorAction Continue
    exit 1
}
