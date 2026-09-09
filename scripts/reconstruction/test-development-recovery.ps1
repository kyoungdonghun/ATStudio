#requires -Version 7.2
[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'Test-DevelopmentRecovery.ps1')
$preflight = Join-Path $PSScriptRoot 'Test-DevelopmentRecovery.ps1'
$testRoot = Join-Path ([IO.Path]::GetTempPath()) ('ats-recovery-test-' + [guid]::NewGuid().ToString('N'))
$testRoot = [IO.Path]::GetFullPath($testRoot)
$failures = [System.Collections.Generic.List[string]]::new()
$total = 0

function Assert-Recovery {
    param([bool] $Condition, [string] $Label)
    $script:total++
    if (-not $Condition) { $script:failures.Add($Label) }
}

function Write-Fixture {
    param([string] $Relative, [string] $Text)
    $path = Join-Path $testRoot $Relative
    [void][IO.Directory]::CreateDirectory([IO.Path]::GetDirectoryName($path))
    [IO.File]::WriteAllText($path, $Text)
}

function Reset-Manifests {
    $package = @{
        name = 'atstudio-frontend'; version = '0.1.0'; private = $true
        scripts = @{ build = 'synthetic'; test = 'synthetic'; lint = 'synthetic'; typecheck = 'synthetic' }
        dependencies = @{ react = '^18.3.1' }; devDependencies = @{ vite = '^6.4.3' }
    }
    $lock = @{
        name = $package.name; version = $package.version; lockfileVersion = 3
        packages = @{
            '' = @{ name = $package.name; version = $package.version
                dependencies = $package.dependencies; devDependencies = $package.devDependencies }
            'node_modules/react' = @{ version = '18.3.1' }
            'node_modules/vite' = @{ version = '6.4.3' }
        }
    }
    Write-Fixture 'frontend/package.json' ($package | ConvertTo-Json -Depth 8)
    Write-Fixture 'frontend/package-lock.json' ($lock | ConvertTo-Json -Depth 8)
}

function Get-FixtureHashes {
    return @(Get-ChildItem -LiteralPath $testRoot -File -Recurse -Force | Sort-Object FullName |
        ForEach-Object { (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash }) -join ':'
}

function Invoke-PreflightCli {
    param([string] $Root)
    $info = [Diagnostics.ProcessStartInfo]::new()
    $info.FileName = (Get-Process -Id $PID).Path
    $info.UseShellExecute = $false
    $info.CreateNoWindow = $true
    $info.RedirectStandardOutput = $true
    $info.RedirectStandardError = $true
    foreach ($arg in @('-NoProfile', '-File', $preflight, '-RepositoryRoot', $Root, '-AsJson')) {
        $info.ArgumentList.Add($arg)
    }
    $process = [Diagnostics.Process]::Start($info)
    try {
        $output = $process.StandardOutput.ReadToEndAsync()
        $errorOutput = $process.StandardError.ReadToEndAsync()
        if (-not $process.WaitForExit(15000)) {
            $process.Kill($true)
            $process.WaitForExit()
            throw 'SYNTHETIC_CLI_TIMEOUT'
        }
        return @{ code = $process.ExitCode; output = $output.Result; errors = $errorOutput.Result }
    } finally { $process.Dispose() }
}

try {
    foreach ($relative in $script:RecoverySourceFiles) { Write-Fixture $relative 'synthetic source' }
    Reset-Manifests
    # Private fixtures are synthetic and locked against reads; no actual settings are accessed.
    foreach ($relative in @('application-local.yml', 'frontend/.env', 'acceptance-backend-environment.json')) {
        Write-Fixture $relative 'PRIVATE_SENTINEL_NOT_A_REAL_SECRET'
    }
    $before = Get-FixtureHashes
    $handles = @()
    try {
        foreach ($relative in @('application-local.yml', 'frontend/.env', 'acceptance-backend-environment.json')) {
            $handles += [IO.File]::Open((Join-Path $testRoot $relative), 'Open', 'ReadWrite', 'None')
        }
        function Get-RecoveryToolState { throw 'TOOLS_MUST_NOT_RUN_IN_DEFAULT_MODE' }
        $report = Get-DevelopmentRecoveryReport -Root $testRoot
        Assert-Recovery $report.ok 'valid-source-with-locked-private-fixtures'
        Assert-Recovery ($report.privateSettings -eq 'NOT_READ' -and $report.database -eq 'NOT_CONNECTED' -and
            $report.restore -eq 'NOT_RUN' -and $report.runtime -eq 'NOT_STARTED_OR_VERIFIED') 'no-runtime-claim'
    } finally { foreach ($handle in $handles) { $handle.Dispose() } }
    Assert-Recovery ((Get-FixtureHashes) -eq $before) 'source-and-private-fixtures-unchanged'

    foreach ($root in @('', (Join-Path $testRoot 'absent'), (Join-Path $testRoot 'README.md'), '\\invalid-host\source')) {
        Assert-Recovery (-not (Get-DevelopmentRecoveryReport -Root $root).ok) 'invalid-root-rejected'
    }
    Write-Fixture 'application-local.example.yml' ''
    Assert-Recovery (-not (Get-DevelopmentRecoveryReport -Root $testRoot).ok) 'empty-template-rejected'
    Write-Fixture 'application-local.example.yml' 'synthetic source'
    [IO.File]::Delete((Join-Path $testRoot 'gradlew.bat'))
    Assert-Recovery (-not (Get-DevelopmentRecoveryReport -Root $testRoot).ok) 'missing-wrapper-rejected'
    Write-Fixture 'gradlew.bat' 'synthetic source'
    [IO.File]::Delete((Join-Path $testRoot '.agents/skills/create-wi-handoff-packet/SKILL.md'))
    Assert-Recovery (-not (Get-DevelopmentRecoveryReport -Root $testRoot).ok) 'missing-workflow-skill-rejected'
    Write-Fixture '.agents/skills/create-wi-handoff-packet/SKILL.md' 'synthetic source'
    [IO.File]::Delete((Join-Path $testRoot 'docs/registry/development-history-recovery-20260909.md'))
    Assert-Recovery (-not (Get-DevelopmentRecoveryReport -Root $testRoot).ok) 'missing-history-registry-rejected'
    Write-Fixture 'docs/registry/development-history-recovery-20260909.md' 'synthetic source'

    foreach ($invalid in @('{ PRIVATE_SENTINEL_NOT_A_REAL_SECRET', '[]', 'null', '{"packages":[]}')) {
        Write-Fixture 'frontend/package-lock.json' $invalid
        $report = Get-DevelopmentRecoveryReport -Root $testRoot
        Assert-Recovery (-not $report.ok) 'invalid-lock-rejected'
        $json = $report | ConvertTo-Json -Depth 6
        Assert-Recovery (-not $json.Contains('PRIVATE_SENTINEL') -and -not $json.Contains($testRoot)) 'diagnostics-redacted'
    }
    Write-Fixture 'frontend/package-lock.json' ('x' * (4MB + 1))
    Assert-Recovery (-not (Get-DevelopmentRecoveryReport -Root $testRoot).ok) 'oversized-json-rejected'
    Reset-Manifests
    foreach ($change in @('dependency', 'root', 'resolved', 'version')) {
        $lock = Get-Content -Raw -LiteralPath (Join-Path $testRoot 'frontend/package-lock.json') |
            ConvertFrom-Json -AsHashtable
        switch ($change) {
            'dependency' { $lock.packages[''].dependencies.react = '^19.0.0' }
            'root' { [void]$lock.packages.Remove('') }
            'resolved' { [void]$lock.packages.Remove('node_modules/react') }
            'version' { $lock.lockfileVersion = 2 }
        }
        Write-Fixture 'frontend/package-lock.json' ($lock | ConvertTo-Json -Depth 8)
        Assert-Recovery (-not (Get-DevelopmentRecoveryReport -Root $testRoot).ok) 'mismatched-lock-rejected'
        Reset-Manifests
    }
    Write-Fixture 'frontend/package.json' '[]'
    Assert-Recovery (-not (Get-DevelopmentRecoveryReport -Root $testRoot).ok) 'nonobject-package-rejected'
    Reset-Manifests

    function Get-RecoveryToolState { param($Name) return 'PRESENT_VERSION_UNVERIFIED' }
    $report = Get-DevelopmentRecoveryReport -Root $testRoot -CheckTools
    Assert-Recovery ($report.ok -and $report.toolVersions -eq 'NOT_CHECKED') 'tool-presence-not-version-proof'
    function Get-RecoveryToolState {
        param($Name)
        if ($Name -eq 'javac') { return 'MISSING' }
        return 'PRESENT_VERSION_UNVERIFIED'
    }
    Assert-Recovery (-not (Get-DevelopmentRecoveryReport -Root $testRoot -CheckTools).ok) 'missing-required-tool'
    function Get-RecoveryToolState {
        param($Name)
        if ($Name -in @('mysql', 'cloudflared')) { return 'MISSING' }
        return 'PRESENT_VERSION_UNVERIFIED'
    }
    $report = Get-DevelopmentRecoveryReport -Root $testRoot -CheckTools
    Assert-Recovery ($report.ok -and @($report.checks | Where-Object status -eq 'WARN').Count -eq 2) 'optional-tools-advisory'

    if ($IsWindows) {
        $link = Join-Path $testRoot 'linked-root'
        $target = Join-Path $testRoot 'scripts'
        [void](New-Item -ItemType Junction -Path $link -Target $target)
        try {
            Assert-Recovery (-not (Get-DevelopmentRecoveryReport -Root $link).ok) 'reparse-root-rejected'
            Assert-Recovery (-not (Get-DevelopmentRecoveryReport -Root (Join-Path $link 'acceptance')).ok) 'reparse-ancestor-rejected'
            [IO.File]::Delete((Join-Path $testRoot 'frontend/package-lock.json'))
            $lockLink = Join-Path $testRoot 'frontend/package-lock.json'
            [void](New-Item -ItemType Junction -Path $lockLink -Target $target)
            try {
                Assert-Recovery (-not (Get-DevelopmentRecoveryReport -Root $testRoot).ok) 'reparse-manifest-rejected'
            } finally { [IO.Directory]::Delete($lockLink) }
        } finally { [IO.Directory]::Delete($link) }
        Reset-Manifests
    }

    $cli = Invoke-PreflightCli -Root $testRoot
    Assert-Recovery ($cli.code -eq 0 -and ($cli.output | ConvertFrom-Json).ok -and $cli.errors -eq '') 'cli-success-json-exit-zero'
    Write-Fixture 'frontend/package.json' '{ PRIVATE_SENTINEL_NOT_A_REAL_SECRET'
    $cli = Invoke-PreflightCli -Root $testRoot
    Assert-Recovery ($cli.code -eq 1 -and -not ($cli.output | ConvertFrom-Json).ok -and
        $cli.errors -eq '' -and -not $cli.output.Contains('PRIVATE_SENTINEL')) 'cli-failure-redacted-exit-one'
} catch {
    $failures.Add('UNEXPECTED_TEST_FAILURE')
} finally {
    $tempBase = [IO.Path]::GetFullPath([IO.Path]::GetTempPath()).TrimEnd('\', '/') + [IO.Path]::DirectorySeparatorChar
    if (-not $testRoot.StartsWith($tempBase, [StringComparison]::OrdinalIgnoreCase) -or
        [IO.Path]::GetFileName($testRoot) -notmatch '^ats-recovery-test-[a-f0-9]{32}$') {
        throw 'UNSAFE_TEST_CLEANUP_REFUSED'
    }
    if (Test-Path -LiteralPath $testRoot) { Remove-Item -LiteralPath $testRoot -Recurse -Force }
}

[pscustomobject]@{ total = $total; passed = $total - $failures.Count; failed = $failures.Count; failures = $failures } |
    ConvertTo-Json -Depth 3
if ($failures.Count -gt 0) { exit 1 }
exit 0
