[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$script:Failures = New-Object System.Collections.ArrayList
$nodeScript = Join-Path $PSScriptRoot 'seed-client-demo.mjs'
$wrapperScript = Join-Path $PSScriptRoot 'seed-client-demo.ps1'

function Assert-True {
    param(
        [Parameter(Mandatory = $true)]
        [bool] $Condition,
        [Parameter(Mandatory = $true)]
        [string] $Message
    )

    if (-not $Condition) {
        [void] $script:Failures.Add($Message)
    }
}

function Invoke-CapturedProcess {
    param(
        [Parameter(Mandatory = $true)]
        [string] $FilePath,
        [Parameter(Mandatory = $true)]
        [string[]] $ArgumentList
    )

    $startInfo = New-Object System.Diagnostics.ProcessStartInfo
    $startInfo.FileName = $FilePath
    $startInfo.Arguments = @(
        $ArgumentList | ForEach-Object {
            '"' + ([string] $_).Replace('"', '\"') + '"'
        }
    ) -join ' '
    $startInfo.UseShellExecute = $false
    $startInfo.CreateNoWindow = $true
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true

    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $startInfo
    try {
        if (-not $process.Start()) {
            throw "Failed to start process: $FilePath"
        }
        $standardOutput = $process.StandardOutput.ReadToEnd()
        $standardError = $process.StandardError.ReadToEnd()
        $process.WaitForExit()
        return [pscustomobject]@{
            exitCode = $process.ExitCode
            output = $standardOutput + $standardError
        }
    } finally {
        $process.Dispose()
    }
}

function Assert-ProcessResult {
    param(
        [Parameter(Mandatory = $true)]
        [object] $Result,
        [Parameter(Mandatory = $true)]
        [int] $ExpectedExitCode,
        [Parameter(Mandatory = $true)]
        [string] $ExpectedText,
        [Parameter(Mandatory = $true)]
        [string] $Case
    )

    Assert-True `
        -Condition ($Result.exitCode -eq $ExpectedExitCode) `
        -Message "$Case exit code mismatch. Expected=$ExpectedExitCode Actual=$($Result.exitCode)"
    Assert-True `
        -Condition $Result.output.Contains($ExpectedText) `
        -Message "$Case output did not contain the expected contract text."
    Assert-True `
        -Condition (-not ($Result.output -match '(?i)password|secret|acceptance-preview')) `
        -Message "$Case output exposed a secret-bearing label or retired runtime path."
}

$nodeCommand = Get-Command node -ErrorAction Stop
$powerShellCommand = (Get-Process -Id $PID).Path
$testRoot = Join-Path `
    ([System.IO.Path]::GetTempPath()) `
    ('atstudio-demo-seed-contract-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $testRoot | Out-Null

try {
    $nodeSource = Get-Content -Raw -LiteralPath $nodeScript
    $wrapperSource = Get-Content -Raw -LiteralPath $wrapperScript
    foreach ($source in @($nodeSource, $wrapperSource)) {
        Assert-True `
            -Condition (-not ($source -match '(?i)acceptance-preview|C:[/\\]Users[/\\]')) `
            -Message 'Demo seed source must not contain a retired runtime or user-specific path.'
    }

    $syntax = Invoke-CapturedProcess `
        -FilePath $nodeCommand.Source `
        -ArgumentList @('--check', $nodeScript)
    Assert-True `
        -Condition ($syntax.exitCode -eq 0) `
        -Message "Node syntax check failed: $($syntax.output)"

    $seedDryRun = Invoke-CapturedProcess `
        -FilePath $nodeCommand.Source `
        -ArgumentList @(
            $nodeScript,
            '--mode', 'seed',
            '--dry-run',
            '--work-dir', (Join-Path $testRoot 'seed-direct')
        )
    Assert-ProcessResult `
        -Result $seedDryRun `
        -ExpectedExitCode 0 `
        -ExpectedText '"mode": "seed-dry-run"' `
        -Case 'direct seed dry-run'

    $cleanupDryRun = Invoke-CapturedProcess `
        -FilePath $nodeCommand.Source `
        -ArgumentList @(
            $nodeScript,
            '--mode', 'cleanup',
            '--dry-run',
            '--work-dir', (Join-Path $testRoot 'cleanup-direct')
        )
    Assert-ProcessResult `
        -Result $cleanupDryRun `
        -ExpectedExitCode 0 `
        -ExpectedText '"mode": "cleanup-dry-run"' `
        -Case 'direct cleanup dry-run'

    foreach ($mode in @('seed', 'verify', 'cleanup')) {
        $missingCredentials = Invoke-CapturedProcess `
            -FilePath $nodeCommand.Source `
            -ArgumentList @(
                $nodeScript,
                '--mode', $mode,
                '--work-dir', (Join-Path $testRoot "missing-$mode")
            )
        Assert-ProcessResult `
            -Result $missingCredentials `
            -ExpectedExitCode 1 `
            -ExpectedText '--credentials is required for non-dry-run demo seed operations' `
            -Case "direct $mode without credentials"
    }

    $missingRuntimeInput = Join-Path $testRoot 'external-runtime-input.json'
    $directExplicitInput = Invoke-CapturedProcess `
        -FilePath $nodeCommand.Source `
        -ArgumentList @(
            $nodeScript,
            '--mode', 'verify',
            '--credentials', $missingRuntimeInput,
            '--work-dir', (Join-Path $testRoot 'explicit-direct')
        )
    Assert-ProcessResult `
        -Result $directExplicitInput `
        -ExpectedExitCode 1 `
        -ExpectedText 'ENOENT' `
        -Case 'direct explicit missing runtime input'

    $verifyDryRun = Invoke-CapturedProcess `
        -FilePath $nodeCommand.Source `
        -ArgumentList @(
            $nodeScript,
            '--mode', 'verify',
            '--dry-run',
            '--work-dir', (Join-Path $testRoot 'verify-dry-run')
        )
    Assert-ProcessResult `
        -Result $verifyDryRun `
        -ExpectedExitCode 1 `
        -ExpectedText '--dry-run is supported only for seed and cleanup modes' `
        -Case 'direct verify dry-run'

    $wrapperCommon = @(
        '-NoProfile',
        '-ExecutionPolicy', 'Bypass',
        '-File', $wrapperScript
    )
    $wrapperSeedDryRun = Invoke-CapturedProcess `
        -FilePath $powerShellCommand `
        -ArgumentList ($wrapperCommon + @(
            '-Mode', 'Seed',
            '-DryRun',
            '-WorkDirectory', (Join-Path $testRoot 'seed-wrapper')
        ))
    Assert-ProcessResult `
        -Result $wrapperSeedDryRun `
        -ExpectedExitCode 0 `
        -ExpectedText '"mode": "seed-dry-run"' `
        -Case 'wrapper seed dry-run'

    $wrapperCleanupDryRun = Invoke-CapturedProcess `
        -FilePath $powerShellCommand `
        -ArgumentList ($wrapperCommon + @(
            '-Mode', 'Cleanup',
            '-DryRun',
            '-WorkDirectory', (Join-Path $testRoot 'cleanup-wrapper')
        ))
    Assert-ProcessResult `
        -Result $wrapperCleanupDryRun `
        -ExpectedExitCode 0 `
        -ExpectedText '"mode": "cleanup-dry-run"' `
        -Case 'wrapper cleanup dry-run'

    $wrapperMissingCredentials = Invoke-CapturedProcess `
        -FilePath $powerShellCommand `
        -ArgumentList ($wrapperCommon + @(
            '-Mode', 'Seed',
            '-WorkDirectory', (Join-Path $testRoot 'missing-wrapper')
        ))
    Assert-ProcessResult `
        -Result $wrapperMissingCredentials `
        -ExpectedExitCode 1 `
        -ExpectedText 'RuntimeCredentialsPath is required for non-dry-run demo seed operations.' `
        -Case 'wrapper seed without credentials'

    $wrapperExplicitInput = Invoke-CapturedProcess `
        -FilePath $powerShellCommand `
        -ArgumentList ($wrapperCommon + @(
            '-Mode', 'Verify',
            '-RuntimeCredentialsPath', $missingRuntimeInput,
            '-WorkDirectory', (Join-Path $testRoot 'explicit-wrapper')
        ))
    Assert-ProcessResult `
        -Result $wrapperExplicitInput `
        -ExpectedExitCode 1 `
        -ExpectedText 'ENOENT' `
        -Case 'wrapper explicit missing runtime input'

    $wrapperVerifyDryRun = Invoke-CapturedProcess `
        -FilePath $powerShellCommand `
        -ArgumentList ($wrapperCommon + @(
            '-Mode', 'Verify',
            '-DryRun',
            '-WorkDirectory', (Join-Path $testRoot 'verify-wrapper')
        ))
    Assert-ProcessResult `
        -Result $wrapperVerifyDryRun `
        -ExpectedExitCode 1 `
        -ExpectedText 'DryRun is supported only for Seed and Cleanup modes.' `
        -Case 'wrapper verify dry-run'
} finally {
    Remove-Item -LiteralPath $testRoot -Recurse -Force -ErrorAction SilentlyContinue
}

if ($script:Failures.Count -gt 0) {
    [pscustomobject]@{
        status = 'failed'
        failures = @($script:Failures)
    } | ConvertTo-Json -Depth 5
    exit 1
}

[pscustomobject]@{
    status = 'passed'
    checks = @(
        'no-personal-default-path',
        'node-syntax',
        'direct-seed-dry-run',
        'direct-cleanup-dry-run',
        'direct-non-dry-run-fail-closed',
        'direct-explicit-runtime-input-forwarding',
        'direct-verify-dry-run-rejected',
        'wrapper-seed-dry-run',
        'wrapper-cleanup-dry-run',
        'wrapper-non-dry-run-fail-closed',
        'wrapper-explicit-runtime-input-forwarding',
        'wrapper-verify-dry-run-rejected',
        'secret-safe-output',
        'temporary-fixture-cleanup'
    )
} | ConvertTo-Json -Depth 5
