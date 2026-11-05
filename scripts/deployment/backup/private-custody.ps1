# Windows-only local output boundary. Never reads file contents.
[CmdletBinding()]
param([ValidateSet('Create','Verify')][string]$Action='Verify',[Parameter(Mandatory=$true)][string]$Path)
$ErrorActionPreference='Stop'
try {
    Import-Module (Join-Path $PSHOME 'Modules\Microsoft.PowerShell.Security\Microsoft.PowerShell.Security.psd1') -ErrorAction Stop
    $full=[IO.Path]::GetFullPath($Path)
    $sid=[Security.Principal.WindowsIdentity]::GetCurrent().User
    $cursor=$full
    while ($cursor) {
        if ((Test-Path -LiteralPath $cursor) -and ((Get-Item -LiteralPath $cursor -Force).Attributes -band [IO.FileAttributes]::ReparsePoint)) {throw 'REPARSE'}
        $cursor=[IO.Path]::GetDirectoryName($cursor)
    }
    if ($Action -eq 'Create') {
        if (Test-Path -LiteralPath $full) {throw 'EXISTS'}
        if (-not [IO.Directory]::Exists([IO.Path]::GetDirectoryName($full))) {throw 'PARENT'}
        $security=[Security.AccessControl.DirectorySecurity]::new()
        $security.SetOwner($sid)
        $security.SetAccessRuleProtection($true,$false)
        $security.AddAccessRule([Security.AccessControl.FileSystemAccessRule]::new($sid,'FullControl','ContainerInherit, ObjectInherit','None','Allow'))
        [void][IO.Directory]::CreateDirectory($full,$security)
    }
    $item=Get-Item -LiteralPath $full -Force
    $acl=Get-Acl -LiteralPath $full
    $rules=@($acl.GetAccessRules($true,$true,[Security.Principal.SecurityIdentifier]))
    if ($acl.GetOwner([Security.Principal.SecurityIdentifier]) -ne $sid -or $rules.Count -ne 1 -or
        $rules[0].IdentityReference -ne $sid -or $rules[0].AccessControlType -ne 'Allow' -or
        $rules[0].FileSystemRights -ne [Security.AccessControl.FileSystemRights]::FullControl) {throw 'ACL'}
    if ($item.PSIsContainer -and -not $acl.AreAccessRulesProtected) {throw 'INHERITANCE'}
    'PRIVATE_CUSTODY_OK'
} catch { Write-Output 'PRIVATE_CUSTODY_HOLD'; exit 2 }
