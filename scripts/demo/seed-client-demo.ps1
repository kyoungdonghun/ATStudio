[CmdletBinding()]
param(
    [ValidateSet('Seed', 'Verify', 'Cleanup')]
    [string]$Mode = 'Seed',
    [string]$ApiBase = 'http://127.0.0.1:8080',
    [string]$RuntimeCredentialsPath = '',
    [string]$WorkDirectory = '',
    [switch]$DryRun
)

$ErrorActionPreference = 'Stop'
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$scriptPath = Join-Path $PSScriptRoot 'seed-client-demo.mjs'

if ([string]::IsNullOrWhiteSpace($WorkDirectory)) {
    $WorkDirectory = Join-Path $repoRoot 'output\demo-seed'
}

if (-not $DryRun -and [string]::IsNullOrWhiteSpace($RuntimeCredentialsPath)) {
    throw 'RuntimeCredentialsPath is required for non-dry-run demo seed operations.'
}

$arguments = @(
    $scriptPath,
    '--mode', $Mode.ToLowerInvariant(),
    '--api-base', $ApiBase,
    '--work-dir', $WorkDirectory
)
if (-not [string]::IsNullOrWhiteSpace($RuntimeCredentialsPath)) {
    $arguments += @('--credentials', $RuntimeCredentialsPath)
}
if ($DryRun) {
    $arguments += '--dry-run'
}

& node @arguments
if ($LASTEXITCODE -ne 0) {
    throw "Demo seed command failed with exit code $LASTEXITCODE"
}
