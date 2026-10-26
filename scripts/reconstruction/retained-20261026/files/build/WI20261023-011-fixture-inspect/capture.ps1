#Requires -Version 5.1
[CmdletBinding()]
param(
    [ValidateSet('Prepare','Verify','Capture','SelfTest')][string]$Action='Verify',
    [string]$PrivateDirectory,
    [string]$PythonPath='python.exe'
)
Set-StrictMode -Version Latest
$ErrorActionPreference='Stop'
$script:Sid=[Security.Principal.WindowsIdentity]::GetCurrent().User
$script:Repo=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..'))

function Assert-NoReparse([string]$Path) {
    $cursor=[IO.Path]::GetFullPath($Path)
    while ($cursor) {
        if (Test-Path -LiteralPath $cursor) {
            if ([IO.File]::GetAttributes($cursor) -band [IO.FileAttributes]::ReparsePoint) {throw 'PATH'}
        }
        $cursor=[IO.Path]::GetDirectoryName($cursor)
    }
}

function Assert-OutsideRepo([string]$Path) {
    $full=[IO.Path]::GetFullPath($Path)
    if ($full -notmatch '^[A-Za-z]:\\' -or $full.StartsWith($script:Repo,[StringComparison]::OrdinalIgnoreCase)) {throw 'PATH'}
    Assert-NoReparse $full
}

function Assert-Private([string]$Path,[bool]$Directory=$false) {
    Assert-NoReparse $Path
    if (-not $Directory -and (Get-Item -LiteralPath $Path).LinkType -eq 'HardLink') {throw 'HARDLINK'}
    $acl=Get-Acl -LiteralPath $Path
    $rules=@($acl.GetAccessRules($true,$true,[Security.Principal.SecurityIdentifier]))
    if ($acl.GetOwner([Security.Principal.SecurityIdentifier]) -ne $script:Sid -or $rules.Count -ne 1) {throw 'ACL'}
    if ($rules[0].IdentityReference -ne $script:Sid -or $rules[0].AccessControlType -ne 'Allow' -or
        $rules[0].FileSystemRights -ne [Security.AccessControl.FileSystemRights]::FullControl) {throw 'ACL'}
    if ($Directory -and -not $acl.AreAccessRulesProtected) {throw 'ACL'}
}

function New-PrivateDirectory([string]$Path) {
    Assert-OutsideRepo $Path
    if (Test-Path -LiteralPath $Path) {throw 'COLLISION'}
    if (-not [IO.Directory]::Exists([IO.Path]::GetDirectoryName($Path))) {throw 'PARENT'}
    $security=[Security.AccessControl.DirectorySecurity]::new()
    $security.SetOwner($script:Sid)
    $security.SetAccessRuleProtection($true,$false)
    $security.AddAccessRule([Security.AccessControl.FileSystemAccessRule]::new(
        $script:Sid,'FullControl','ContainerInherit, ObjectInherit','None','Allow'))
    if (-not ('ATStudio.InspectDirectory' -as [type])) {
        Add-Type -TypeDefinition @'
using System;
using System.Runtime.InteropServices;
namespace ATStudio {
 public static class InspectDirectory {
  [StructLayout(LayoutKind.Sequential)] private struct Attributes {
   public int Length; public IntPtr Descriptor; public int Inherit;
  }
  [DllImport("kernel32.dll", CharSet=CharSet.Unicode, SetLastError=true)]
  private static extern bool CreateDirectoryW(string path, ref Attributes attributes);
  public static bool CreateNew(string path, byte[] descriptor) {
   var pin=GCHandle.Alloc(descriptor,GCHandleType.Pinned);
   try {var a=new Attributes {Length=Marshal.SizeOf(typeof(Attributes)),Descriptor=pin.AddrOfPinnedObject()};
    return CreateDirectoryW(path,ref a);
   } finally {pin.Free();}
  }
 }
}
'@
    }
    if (-not [ATStudio.InspectDirectory]::CreateNew($Path,$security.GetSecurityDescriptorBinaryForm())) {throw 'CREATE'}
    Assert-Private $Path $true
}

function Write-Private([string]$Path,[string]$Text) {
    Assert-Private ([IO.Path]::GetDirectoryName($Path)) $true
    Assert-NoReparse $Path
    $bytes=[Text.UTF8Encoding]::new($false).GetBytes($Text)
    $stream=[IO.FileStream]::new($Path,[IO.FileMode]::CreateNew,[IO.FileAccess]::Write,[IO.FileShare]::None)
    try {$stream.Write($bytes,0,$bytes.Length);$stream.Flush($true)} finally {$stream.Dispose()}
    # Inherited rights were already current-user-only while writing. Protect
    # this file explicitly before reporting capture success.
    $acl=Get-Acl -LiteralPath $Path
    $acl.SetAccessRuleProtection($true,$true)
    Set-Acl -LiteralPath $Path -AclObject $acl
    Assert-Private $Path
}

function Invoke-Validator([string]$Mode,[string]$Json='') {
    $python=@(& $PythonPath -I -S -B -c 'import sys; print(sys.executable)' 2>$null)
    if ($LASTEXITCODE -ne 0 -or $python.Count -ne 1 -or -not [IO.File]::Exists($python[0])) {throw 'PYTHON'}
    $start=[Diagnostics.ProcessStartInfo]::new()
    $start.FileName=$python[0]
    $scriptFile=Join-Path $PSScriptRoot 'inspect_tools.py'
    if ($scriptFile.Contains('"') -or $PrivateDirectory.Contains('"')) {throw 'PATH'}
    $start.Arguments='-I -S -B "'+$scriptFile+'" '+$Mode
    if ($Mode -eq 'ready') {$start.Arguments+=' --private-directory "'+$PrivateDirectory+'"'}
    $start.UseShellExecute=$false;$start.CreateNoWindow=$true
    $start.RedirectStandardInput=$true;$start.RedirectStandardOutput=$true;$start.RedirectStandardError=$true
    $process=[Diagnostics.Process]::new();$process.StartInfo=$start
    if (-not $process.Start()) {throw 'VALIDATOR'}
    try {
        if ($Json) {$process.StandardInput.Write($Json)}
        $process.StandardInput.Close()
        if (-not $process.WaitForExit(30000)) {$process.Kill();throw 'VALIDATOR'}
        $output=$process.StandardOutput.ReadToEnd().Trim()
        $errorText=$process.StandardError.ReadToEnd()
        if ($process.ExitCode -ne 0 -or $errorText -or $output -cne $(if ($Mode -eq 'ready') {'CAPTURE_READY'} else {'INPUT_VALID'})) {throw 'INPUT'}
    } finally {$process.Dispose()}
}

function Show-Capture {
    Invoke-Validator 'ready'
    if ([IO.File]::Exists((Join-Path $PrivateDirectory 'inputs.json'))) {throw 'COLLISION'}
    Add-Type -AssemblyName System.Windows.Forms
    Add-Type -AssemblyName System.Drawing
    $form=[Windows.Forms.Form]::new();$form.Text='ATStudio inspect-only private input'
    $form.Width=600;$form.Height=550;$form.StartPosition='CenterScreen';$form.TopMost=$true
    $form.FormBorderStyle='FixedDialog';$form.MaximizeBox=$false
    $note=[Windows.Forms.Label]::new();$note.Left=20;$note.Top=15;$note.Width=550;$note.Height=48
    $note.Text='Private inspect input only. No account creation or login. Password: 12-72 printable ASCII characters, no spaces. Nickname: ats_test_ + 1-11 letters/digits/underscores.'
    $form.Controls.Add($note)
    $boxes=@{};$y=80
    foreach ($actor in @('admin','buyer')) {
        foreach ($field in @('email','nickname','password','confirmation')) {
            $label=[Windows.Forms.Label]::new();$label.Left=20;$label.Top=$y+4;$label.Width=170;$label.Text=$actor+' '+$field
            $box=[Windows.Forms.TextBox]::new();$box.Left=200;$box.Top=$y;$box.Width=350
            $box.MaxLength=4096
            if ($field -in @('password','confirmation')) {$box.UseSystemPasswordChar=$true}
            $boxes[$actor+'_'+$field]=$box;$form.Controls.Add($label);$form.Controls.Add($box);$y+=42
        }
    }
    $status=[Windows.Forms.Label]::new();$status.Left=20;$status.Top=420;$status.Width=540;$status.Height=35;$form.Controls.Add($status)
    $button=[Windows.Forms.Button]::new();$button.Text='Save privately';$button.Left=360;$button.Top=460;$button.Width=190;$form.Controls.Add($button)
    $script:Captured=$false
    $button.Add_Click({
        $stage='VALIDATION'
        try {
            $value=@{}
            foreach ($actor in @('admin','buyer')) {
                if ($boxes[$actor+'_password'].Text -cne $boxes[$actor+'_confirmation'].Text) {throw 'INPUT'}
                $value[$actor]=@{email=$boxes[$actor+'_email'].Text;nickname=$boxes[$actor+'_nickname'].Text;password=$boxes[$actor+'_password'].Text}
            }
            $json=$value|ConvertTo-Json -Depth 4 -Compress
            Invoke-Validator 'validate' $json
            # Capture was authorized after staged readiness. Preserve valid local
            # input even if that native approval expires while the user types.
            $stage='STORAGE'
            Write-Private (Join-Path $PrivateDirectory 'inputs.json') $json
            $script:Captured=$true
            foreach ($box in $boxes.Values) {$box.Clear()}
            $form.Close()
        } catch {
            $status.Text=if ($stage -eq 'STORAGE') {'Storage state uncertain. Files preserved; stop for MA. Do not re-enter.'}
                else {'Check format, unique identities and password confirmation. Nothing was submitted.'}
        }
    })
    [void]$form.ShowDialog();$form.Dispose()
    if (-not $script:Captured) {throw 'CANCELLED'}
    'PRIVATE_INPUT_CAPTURED'
}

try {
    Import-Module (Join-Path $PSHOME 'Modules\Microsoft.PowerShell.Security\Microsoft.PowerShell.Security.psd1') -ErrorAction Stop
    if ($Action -eq 'SelfTest') {
        $PrivateDirectory=Join-Path ([Environment]::GetFolderPath('LocalApplicationData')) ('ATStudio-WI011-synthetic-'+[Guid]::NewGuid().ToString('N'))
        New-PrivateDirectory $PrivateDirectory
        $fake='{"admin":{"email":"admin@example.invalid","nickname":"ats_test_admin","password":"SYNTHETIC-admin-2026!"},"buyer":{"email":"buyer@example.invalid","nickname":"ats_test_buyer","password":"SYNTHETIC-buyer-2026!"}}'
        Invoke-Validator 'validate' $fake
        Write-Private (Join-Path $PrivateDirectory 'synthetic-inputs.json') $fake
        $collision=$false
        try {Write-Private (Join-Path $PrivateDirectory 'synthetic-inputs.json') 'changed'} catch {$collision=$true}
        if (-not $collision) {throw 'TEST'}
        $invalid=$false
        try {Invoke-Validator 'validate' '{}'} catch {$invalid=$true}
        if (-not $invalid) {throw 'TEST'}
        Assert-Private $PrivateDirectory $true
        Assert-Private (Join-Path $PrivateDirectory 'synthetic-inputs.json')
        'SYNTHETIC_ACL_VALIDATION_COLLISION_PASS';return
    }
    if (-not $PrivateDirectory) {throw 'DIRECTORY'}
    Assert-OutsideRepo $PrivateDirectory
    if ($Action -eq 'Prepare') {New-PrivateDirectory $PrivateDirectory;'PRIVATE_DIRECTORY_READY';return}
    Assert-Private $PrivateDirectory $true
    foreach ($file in Get-ChildItem -LiteralPath $PrivateDirectory -Force) {
        if ($file.PSIsContainer) {throw 'UNEXPECTED_DIRECTORY'}
        Assert-Private $file.FullName
    }
    if ($Action -eq 'Verify') {'PRIVATE_ACL_OK';return}
    Show-Capture
} catch {
    if ($Action -eq 'SelfTest') {Write-Output ('SYNTHETIC_FAILURE_LINE_'+$_.InvocationInfo.ScriptLineNumber+'_'+$_.Exception.GetType().Name)}
    Write-Output 'HOLD_PRIVATE_INPUT_BOUNDARY'
    exit 2
}
