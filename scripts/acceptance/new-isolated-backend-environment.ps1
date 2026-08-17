[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string] $SourcePath,

    [Parameter(Mandatory = $true)]
    [string] $TargetPath,

    [Parameter(Mandatory = $true)]
    [ValidatePattern("^ats_disposable_\d{8}_[a-z0-9]{8}$")]
    [string] $DatabaseName
)

$ErrorActionPreference = "Stop"

function Test-PathWithin {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Path,
        [Parameter(Mandatory = $true)]
        [string] $Root
    )

    $normalizedPath = [System.IO.Path]::GetFullPath($Path).TrimEnd([char[]]@("\", "/"))
    $normalizedRoot = [System.IO.Path]::GetFullPath($Root).TrimEnd([char[]]@("\", "/"))
    $comparison = [System.StringComparison]::OrdinalIgnoreCase
    return $normalizedPath.StartsWith("$normalizedRoot\", $comparison)
}

function Assert-UserOnlyProtectedAcl {
    param(
        [Parameter(Mandatory = $true)]
        [System.Security.AccessControl.FileSecurity] $Acl,
        [Parameter(Mandatory = $true)]
        [string] $Label
    )

    if (-not $Acl.AreAccessRulesProtected) {
        throw "$Label ACL must be protected."
    }

    $currentUserSid = [System.Security.Principal.WindowsIdentity]::GetCurrent().User.Value
    $rules = @($Acl.Access)
    if ($rules.Count -ne 1) {
        throw "$Label ACL must contain exactly one access rule."
    }

    $rule = $rules[0]
    try {
        $ruleSid = $rule.IdentityReference.Translate(
            [System.Security.Principal.SecurityIdentifier]).Value
    } catch {
        throw "$Label ACL identity could not be resolved."
    }
    if ($rule.AccessControlType -ne [System.Security.AccessControl.AccessControlType]::Allow -or
        $ruleSid -ne $currentUserSid) {
        throw "$Label ACL must grant access only to the current user."
    }
}

$acceptanceRoot = Join-Path $env:LOCALAPPDATA "ATStudio"
$sourceFullPath = [System.IO.Path]::GetFullPath($SourcePath)
$targetFullPath = [System.IO.Path]::GetFullPath($TargetPath)

if (-not (Test-PathWithin -Path $sourceFullPath -Root $acceptanceRoot) -or
    -not (Test-PathWithin -Path $targetFullPath -Root $acceptanceRoot)) {
    throw "Acceptance environment bundles must remain under the local ATStudio runtime directory."
}

$sourceFile = Get-Item -LiteralPath $sourceFullPath -Force -ErrorAction Stop
if (-not ($sourceFile -is [System.IO.FileInfo]) -or
    (($sourceFile.Attributes -band [System.IO.FileAttributes]::ReparsePoint) -ne 0)) {
    throw "Source acceptance environment bundle must be a regular file."
}
if (Test-Path -LiteralPath $targetFullPath) {
    throw "Target acceptance environment bundle already exists."
}

$sourceAcl = Get-Acl -LiteralPath $sourceFullPath
Assert-UserOnlyProtectedAcl -Acl $sourceAcl -Label "Source acceptance environment bundle"

try {
    $bundle = [System.IO.File]::ReadAllText($sourceFullPath) | ConvertFrom-Json -ErrorAction Stop
} catch {
    throw "Source acceptance environment bundle must contain valid JSON."
}
if (-not ($bundle -is [pscustomobject])) {
    throw "Source acceptance environment bundle must be a flat JSON object."
}
foreach ($property in $bundle.PSObject.Properties) {
    if (-not ($property.Value -is [string])) {
        throw "Source acceptance environment bundle values must be strings."
    }
}

$requiredNames = @(
    "SPRING_DATASOURCE_URL",
    "SPRING_DATASOURCE_USERNAME",
    "SPRING_DATASOURCE_PASSWORD"
)
foreach ($name in $requiredNames) {
    $property = $bundle.PSObject.Properties[$name]
    if (-not $property -or [string]::IsNullOrWhiteSpace([string] $property.Value)) {
        throw "Source acceptance environment bundle is missing required datasource values."
    }
}

$url = [string] $bundle.SPRING_DATASOURCE_URL
$jdbcPattern = "^(jdbc:mysql://(?<host>\[[^\]]+\]|[^/:?#]+)(?::(?<port>\d+))?/)(?<database>[^?]+)(?<query>\?.*)?$"
if ($url -notmatch $jdbcPattern) {
    throw "Source acceptance environment bundle must contain a single-host MySQL JDBC URL."
}
$jdbcHost = $Matches.host.ToLowerInvariant()
if ($jdbcHost -notin @("localhost", "127.0.0.1", "::1", "[::1]")) {
    throw "Source acceptance datasource must use a loopback MySQL host."
}
$bundle.SPRING_DATASOURCE_URL = $Matches[1] + $DatabaseName + $Matches.query

$targetDirectory = Split-Path -Parent $targetFullPath
if (-not (Test-Path -LiteralPath $targetDirectory -PathType Container)) {
    throw "Target acceptance environment directory is unavailable."
}
$temporaryPath = Join-Path $targetDirectory ("." + [System.IO.Path]::GetFileName($targetFullPath) + "." + [guid]::NewGuid().ToString("N") + ".tmp")
$targetCreated = $false

try {
    $json = $bundle | ConvertTo-Json -Depth 8
    [System.IO.File]::WriteAllText($temporaryPath, $json, (New-Object System.Text.UTF8Encoding($false)))
    Set-Acl -LiteralPath $temporaryPath -AclObject $sourceAcl
    Move-Item -LiteralPath $temporaryPath -Destination $targetFullPath -ErrorAction Stop
    $targetCreated = $true

    $targetAcl = Get-Acl -LiteralPath $targetFullPath
    Assert-UserOnlyProtectedAcl -Acl $targetAcl -Label "Target acceptance environment bundle"
    if ($targetAcl.Sddl -ne $sourceAcl.Sddl) {
        throw "Target acceptance environment bundle ACL does not match the source ACL."
    }

    [pscustomobject]@{
        status = "PASS"
        bundle = "created"
        acl = "protected-and-matched"
        datasource = "loopback-disposable-target"
        databaseIdentity = "REDACTED"
    } | ConvertTo-Json
} catch {
    if ($targetCreated -and (Test-Path -LiteralPath $targetFullPath)) {
        Remove-Item -LiteralPath $targetFullPath -Force
    }
    throw
} finally {
    if (Test-Path -LiteralPath $temporaryPath) {
        Remove-Item -LiteralPath $temporaryPath -Force
    }
}
