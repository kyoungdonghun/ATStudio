[CmdletBinding()]
param(
    [string] $RuntimeRoot,
    [string] $CloudflaredPath,
    [string] $BackendEnvironmentPath,
    [int] $TunnelTimeoutSeconds = 60,
    [int] $ReadinessTimeoutSeconds = 180,
    [switch] $DryRun
)

$ErrorActionPreference = "Stop"
$modulePath = Join-Path $PSScriptRoot "AcceptanceLifecycle.psm1"
Import-Module $modulePath -Force

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")

if ($DryRun) {
    New-AcceptanceDryRunPlan `
        -RepoRoot $repoRoot.Path `
        -RuntimeRoot $RuntimeRoot `
        -CloudflaredPath $CloudflaredPath |
        ConvertTo-Json -Depth 8
    exit 0
}

if ([string]::IsNullOrWhiteSpace($BackendEnvironmentPath)) {
    throw "BackendEnvironmentPath is required for non-dry-run starts."
}

Start-AcceptanceEnvironment `
    -RepoRoot $repoRoot.Path `
    -RuntimeRoot $RuntimeRoot `
    -CloudflaredPath $CloudflaredPath `
    -BackendEnvironmentPath $BackendEnvironmentPath `
    -TunnelTimeoutSeconds $TunnelTimeoutSeconds `
    -ReadinessTimeoutSeconds $ReadinessTimeoutSeconds |
    ConvertTo-Json -Depth 8
