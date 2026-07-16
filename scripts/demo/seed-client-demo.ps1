[CmdletBinding()]
param(
    [ValidateSet('Seed', 'Verify', 'Cleanup')]
    [string]$Mode = 'Seed',
    [string]$ApiBase = 'http://127.0.0.1:8080',
    [string]$RuntimeCredentialsPath = 'C:\Users\jm991\AppData\Local\ATStudio\acceptance-preview-64db91c\backend-environment-credentials.json',
    [string]$WorkDirectory = '',
    [switch]$DryRun
)

$ErrorActionPreference = 'Stop'
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$scriptPath = Join-Path $PSScriptRoot 'seed-client-demo.mjs'

if ([string]::IsNullOrWhiteSpace($WorkDirectory)) {
    $WorkDirectory = Join-Path $repoRoot 'output\demo-seed'
}

$arguments = @(
    $scriptPath,
    '--mode', $Mode.ToLowerInvariant(),
    '--api-base', $ApiBase,
    '--credentials', $RuntimeCredentialsPath,
    '--work-dir', $WorkDirectory
)
if ($DryRun) {
    $arguments += '--dry-run'
}

& node @arguments
if ($LASTEXITCODE -ne 0) {
    throw "Demo seed command failed with exit code $LASTEXITCODE"
}
