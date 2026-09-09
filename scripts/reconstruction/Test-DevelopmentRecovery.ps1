#requires -Version 7.2
[CmdletBinding()]
param(
    [string] $RepositoryRoot = (Join-Path $PSScriptRoot '../..'),
    [switch] $CheckTools,
    [switch] $AsJson
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$script:RecoverySourceFiles = @(
    'README.md', 'AGENTS.md', 'CLAUDE.md', '.gitignore', 'docs/index.md',
    '.claude/config/workspace.json', '.claude/config/context-injection-rules.json',
    '.agents/skills/create-req/SKILL.md', '.agents/skills/create-wi-handoff-packet/SKILL.md',
    '.agents/skills/create-wi-evidence-pack/SKILL.md',
    '.agents/skills/validate-docs/scripts/validate_docs.py',
    'docs/standards/core-principles.md', 'docs/standards/development-standards.md',
    'docs/standards/documentation-standards.md', 'docs/standards/glossary.md',
    'docs/registry/v1-artifact-retention-20260909.md',
    'docs/registry/development-history-recovery-20260909.md',
    'docs/design/runtime-storage-operations.md', 'docs/SR/SR-93.md',
    'build.gradle', 'settings.gradle', 'gradlew.bat', 'gradle/wrapper/gradle-wrapper.jar',
    'gradle/wrapper/gradle-wrapper.properties', 'frontend/package.json',
    'frontend/package-lock.json', 'frontend/.env.example', 'application-local.example.yml',
    'src/main/resources/application.yml', 'src/main/resources/schema.sql',
    'src/main/resources/seed.sql', 'scripts/acceptance/README.md',
    'scripts/acceptance/start.ps1', 'scripts/database/README.md',
    'scripts/database/bootstrap-disposable-mysql.ps1', 'scripts/reconstruction/README.md'
)

function Get-RecoveryToolState {
    param([string] $Name)
    $command = Get-Command -Name $Name -CommandType Application -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if ($null -eq $command) { return 'MISSING' }
    return 'PRESENT_VERSION_UNVERIFIED'
}

function Get-RecoveryFile {
    param([string] $Root, [string] $RelativePath)
    $current = $Root
    foreach ($part in $RelativePath.Split('/')) {
        $current = Join-Path $current $part
        $item = Get-Item -LiteralPath $current -Force
        if ($item.Attributes -band [IO.FileAttributes]::ReparsePoint) { throw 'UNSAFE_SOURCE' }
    }
    if ($item.PSIsContainer -or $item.Length -eq 0) { throw 'INVALID_SOURCE' }
    return $item
}

function Read-RecoveryJson {
    param([IO.FileInfo] $File)
    if ($File.Length -gt 4MB) { throw 'MANIFEST_TOO_LARGE' }
    $data = [IO.File]::ReadAllText($File.FullName) | ConvertFrom-Json -AsHashtable -Depth 64
    if ($data -isnot [System.Collections.IDictionary]) { throw 'INVALID_MANIFEST' }
    return $data
}

function Test-RecoveryPackageContract {
    param([System.Collections.IDictionary] $Package, [System.Collections.IDictionary] $Lock)
    if ($Package['name'] -ne 'atstudio-frontend' -or $Package['private'] -ne $true -or
        $Lock['lockfileVersion'] -ne 3 -or $Lock['name'] -ne $Package['name'] -or
        $Lock['version'] -ne $Package['version']) { return $false }
    if ($Lock['packages'] -isnot [System.Collections.IDictionary]) { return $false }
    $rootPackage = $Lock['packages']['']
    if ($rootPackage -isnot [System.Collections.IDictionary] -or
        $rootPackage['name'] -ne $Package['name'] -or
        $rootPackage['version'] -ne $Package['version']) { return $false }
    if ($Package['scripts'] -isnot [System.Collections.IDictionary]) { return $false }
    foreach ($name in @('build', 'test', 'lint', 'typecheck')) {
        if ($Package['scripts'][$name] -isnot [string] -or
            [string]::IsNullOrWhiteSpace($Package['scripts'][$name])) { return $false }
    }
    foreach ($group in @('dependencies', 'devDependencies')) {
        $expected = $Package[$group]
        $actual = $rootPackage[$group]
        if ($expected -isnot [System.Collections.IDictionary] -or
            $actual -isnot [System.Collections.IDictionary] -or
            $expected.Count -eq 0 -or $expected.Count -ne $actual.Count) { return $false }
        foreach ($name in $expected.Keys) {
            if ($expected[$name] -isnot [string] -or
                [string]::IsNullOrWhiteSpace($expected[$name]) -or
                $expected[$name] -cne $actual[$name]) { return $false }
            $resolved = $Lock['packages']["node_modules/$name"]
            if ($resolved -isnot [System.Collections.IDictionary] -or
                $resolved['version'] -isnot [string] -or
                [string]::IsNullOrWhiteSpace($resolved['version'])) { return $false }
        }
    }
    return $true
}

function Get-DevelopmentRecoveryReport {
    param([string] $Root, [switch] $CheckTools)
    $checks = [System.Collections.Generic.List[object]]::new()
    $validRoot = $false
    try {
        if ([string]::IsNullOrWhiteSpace($Root)) { throw 'INVALID_ROOT' }
        $fullRoot = [IO.Path]::GetFullPath($Root)
        if ($fullRoot.StartsWith('\\') -or $fullRoot.StartsWith('//')) { throw 'REMOTE_ROOT' }
        if ([IO.DriveInfo]::new([IO.Path]::GetPathRoot($fullRoot)).DriveType -eq 'Network') {
            throw 'REMOTE_ROOT'
        }
        $directory = [IO.DirectoryInfo]::new($fullRoot)
        while ($null -ne $directory) {
            if (-not $directory.Exists -or
                ($directory.Attributes -band [IO.FileAttributes]::ReparsePoint)) {
                throw 'INVALID_ROOT'
            }
            $directory = $directory.Parent
        }
        $validRoot = $true
        $checks.Add([pscustomobject]@{ id = 'source.root'; status = 'PASS'; detail = 'LOCAL_DIRECTORY' })
    } catch {
        $checks.Add([pscustomobject]@{ id = 'source.root'; status = 'FAIL'; detail = 'INVALID_OR_UNSAFE_ROOT' })
    }

    if ($validRoot) {
        $manifests = @{}
        foreach ($relative in $script:RecoverySourceFiles) {
            try {
                $file = Get-RecoveryFile -Root $fullRoot -RelativePath $relative
                if ($relative -in @('frontend/package.json', 'frontend/package-lock.json')) {
                    $manifests[$relative] = Read-RecoveryJson -File $file
                }
                $checks.Add([pscustomobject]@{ id = "source.$relative"; status = 'PASS'; detail = 'PRESENT' })
            } catch {
                # Never serialize file contents, input paths or parser/OS exception messages.
                $checks.Add([pscustomobject]@{
                    id = "source.$relative"; status = 'FAIL'; detail = 'MISSING_INVALID_OR_UNSAFE_SOURCE'
                })
            }
        }
        $matches = $false
        if ($manifests.Count -eq 2) {
            try {
                $matches = Test-RecoveryPackageContract -Package $manifests['frontend/package.json'] `
                    -Lock $manifests['frontend/package-lock.json']
            } catch { $matches = $false }
        }
        $checks.Add([pscustomobject]@{
            id = 'source.package-contract'
            status = $(if ($matches) { 'PASS' } else { 'FAIL' })
            detail = $(if ($matches) { 'DIRECT_DEPENDENCIES_PAIRED' } else { 'INVALID_OR_MISMATCHED_MANIFESTS' })
        })
    }

    if ($CheckTools) {
        foreach ($tool in @('git', 'java', 'javac', 'node', 'npm', 'python', 'mysql', 'cloudflared')) {
            $state = Get-RecoveryToolState -Name $tool
            $required = $tool -in @('git', 'java', 'javac', 'node', 'npm', 'python')
            $checks.Add([pscustomobject]@{
                id = "tool.$tool"
                status = $(if ($state -ne 'MISSING') { 'PASS' } elseif ($required) { 'FAIL' } else { 'WARN' })
                detail = $state
            })
        }
    }
    return [pscustomobject]@{
        scope = $(if ($CheckTools) { 'SOURCE_AND_TOOL_DISCOVERY' } else { 'SOURCE_ONLY' })
        ok = (@($checks | Where-Object status -eq 'FAIL').Count -eq 0)
        toolVersions = 'NOT_CHECKED'
        privateSettings = 'NOT_READ'
        database = 'NOT_CONNECTED'
        restore = 'NOT_RUN'
        runtime = 'NOT_STARTED_OR_VERIFIED'
        checks = $checks.ToArray()
    }
}

if ($MyInvocation.InvocationName -eq '.') { return }
$report = Get-DevelopmentRecoveryReport -Root $RepositoryRoot -CheckTools:$CheckTools
if ($AsJson) {
    $report | ConvertTo-Json -Depth 6
} else {
    $report.checks | Format-Table -AutoSize
    $report | Select-Object scope, ok, toolVersions, privateSettings, database, restore, runtime | Format-List
}
if (-not $report.ok) { exit 1 }
exit 0
