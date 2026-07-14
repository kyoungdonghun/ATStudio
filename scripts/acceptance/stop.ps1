[CmdletBinding()]
param(
    [string] $RuntimeRoot,
    [int] $TimeoutSeconds = 30
)

$ErrorActionPreference = "Stop"
$modulePath = Join-Path $PSScriptRoot "AcceptanceLifecycle.psm1"
Import-Module $modulePath -Force

Stop-AcceptanceEnvironment `
    -RuntimeRoot $RuntimeRoot `
    -TimeoutSeconds $TimeoutSeconds |
    ConvertTo-Json -Depth 8
