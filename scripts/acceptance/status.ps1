[CmdletBinding()]
param(
    [string] $RuntimeRoot
)

$ErrorActionPreference = "Stop"
$modulePath = Join-Path $PSScriptRoot "AcceptanceLifecycle.psm1"
Import-Module $modulePath -Force

Get-AcceptanceStatus -RuntimeRoot $RuntimeRoot | ConvertTo-Json -Depth 8
