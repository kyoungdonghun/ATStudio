[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"
$script:Failures = New-Object System.Collections.ArrayList
$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$modulePath = Join-Path $PSScriptRoot "AcceptanceLifecycle.psm1"
Import-Module $modulePath -Force

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

function Assert-Equal {
    param(
        [object] $Actual,
        [object] $Expected,
        [Parameter(Mandatory = $true)]
        [string] $Message
    )

    if ($Actual -ne $Expected) {
        [void] $script:Failures.Add("$Message Expected=[$Expected] Actual=[$Actual]")
    }
}

function Assert-SequenceEqual {
    param(
        [object[]] $Actual,
        [object[]] $Expected,
        [Parameter(Mandatory = $true)]
        [string] $Message
    )

    $actualJoined = @($Actual) -join "|"
    $expectedJoined = @($Expected) -join "|"
    if ($actualJoined -ne $expectedJoined) {
        [void] $script:Failures.Add("$Message Expected=[$expectedJoined] Actual=[$actualJoined]")
    }
}

function Invoke-InAcceptanceModule {
    param(
        [Parameter(Mandatory = $true)]
        [scriptblock] $ScriptBlock,
        [object[]] $ArgumentList = @()
    )

    $module = Get-Module AcceptanceLifecycle
    if (-not $module) {
        throw "AcceptanceLifecycle module was not loaded."
    }

    return & $module $ScriptBlock @ArgumentList
}

function Invoke-ParserCheck {
    $files = Get-ChildItem -Path $PSScriptRoot -Recurse -Include *.ps1, *.psm1
    foreach ($file in $files) {
        $tokens = $null
        $errors = $null
        [System.Management.Automation.Language.Parser]::ParseFile(
            $file.FullName,
            [ref] $tokens,
            [ref] $errors
        ) | Out-Null
        Assert-Equal `
            -Actual $errors.Count `
            -Expected 0 `
            -Message "Parser errors in $($file.FullName)."
    }
}

function Invoke-AnalyzerIfAvailable {
    if (-not (Get-Command Invoke-ScriptAnalyzer -ErrorAction SilentlyContinue)) {
        return "not-installed"
    }

    $findings = Invoke-ScriptAnalyzer -Path $PSScriptRoot -Recurse
    Assert-Equal `
        -Actual @($findings).Count `
        -Expected 0 `
        -Message "PSScriptAnalyzer findings were returned."
    return "passed"
}

Invoke-ParserCheck
$analyzerStatus = Invoke-AnalyzerIfAvailable

$moduleSource = Get-Content -Raw -LiteralPath $modulePath
Assert-True `
    -Condition ($moduleSource -match '\$completedSuccessfully\s*=\s*\$false') `
    -Message "AcceptanceLifecycle should initialize a completedSuccessfully flag."
Assert-True `
    -Condition ($moduleSource -match 'finally\s*\{[\s\S]*if\s*\(-not\s+\$completedSuccessfully\)') `
    -Message "AcceptanceLifecycle should guard cleanup from a finally block."
Assert-True `
    -Condition ($moduleSource -match 'Invoke-AcceptanceServiceCleanup\s+-ServicesByName\s+\$servicesByName') `
    -Message "AcceptanceLifecycle finally cleanup should route through Invoke-AcceptanceServiceCleanup."

$validText = @"
INF Tunnel established
https://abc-123.trycloudflare.com
"@
$parsedUrl = Get-AcceptancePublicUrlFromText -Text $validText
Assert-Equal `
    -Actual $parsedUrl `
    -Expected "https://abc-123.trycloudflare.com" `
    -Message "Quick-tunnel URL parser should return exactly one normalized URL."

Assert-True `
    -Condition (Test-AcceptancePublicBaseUrl -PublicBaseUrl "https://abc-123.trycloudflare.com") `
    -Message "Valid quick-tunnel base URL should pass."
Assert-True `
    -Condition (-not (Test-AcceptancePublicBaseUrl -PublicBaseUrl "http://abc-123.trycloudflare.com")) `
    -Message "Non-HTTPS public base URL should fail."
Assert-True `
    -Condition (-not (Test-AcceptancePublicBaseUrl -PublicBaseUrl "https://abc-123.trycloudflare.com/")) `
    -Message "Trailing slash public base URL should fail."
Assert-True `
    -Condition (-not (Test-AcceptancePublicBaseUrl -PublicBaseUrl "https://example.com")) `
    -Message "Non-quick-tunnel public base URL should fail."

$testRoot = Join-Path `
    ([System.IO.Path]::GetTempPath()) `
    ("atstudio-acceptance-dry-run-tests-" + [guid]::NewGuid().ToString("N"))
New-Item -ItemType Directory -Path $testRoot | Out-Null
$runtimeRoot = Join-Path $testRoot "plan"
$plan = New-AcceptanceDryRunPlan -RepoRoot $repoRoot.Path -RuntimeRoot $runtimeRoot
Assert-True `
    -Condition ($plan.runtimeRoot -notlike "$($repoRoot.Path)*") `
    -Message "Dry-run runtime root must be outside the repository."
Assert-True `
    -Condition ($plan.environmentVariableNames -contains "APP_PUBLIC_BASE_URL") `
    -Message "Dry-run should list APP_PUBLIC_BASE_URL as an injected name."
Assert-True `
    -Condition ($plan.environmentVariableNames -contains "SPRING_PROFILES_ACTIVE") `
    -Message "Dry-run should list SPRING_PROFILES_ACTIVE as an injected name."
Assert-True `
    -Condition (-not (($plan | ConvertTo-Json -Depth 8) -match "SECRET|PASSWORD|CLIENT_KEY")) `
    -Message "Dry-run plan should not contain secret-bearing variable names or values."
Assert-Equal `
    -Actual $plan.backendEnvironmentBundle.requiredVariableCount `
    -Expected 6 `
    -Message "Dry-run should report the required backend variable count without listing names."
Assert-True `
    -Condition (-not ($plan.PSObject.Properties.Name -contains "backendEnvironmentPath")) `
    -Message "Dry-run should not include a backend environment bundle path."

$dryRunRoot = Join-Path $testRoot "start"
$dryRunBundlePath = Join-Path $testRoot "bundle-path-must-not-appear.json"
$startDryRun = & (Join-Path $PSScriptRoot "start.ps1") `
    -RuntimeRoot $dryRunRoot `
    -BackendEnvironmentPath $dryRunBundlePath `
    -DryRun |
    ConvertFrom-Json
Assert-True `
    -Condition ($startDryRun.dryRun -eq $true) `
    -Message "start.ps1 -DryRun should not start processes."
Assert-True `
    -Condition ($startDryRun.runtimeRoot -notlike "$($repoRoot.Path)*") `
    -Message "start.ps1 -DryRun should keep runtime state outside the repository."
Assert-True `
    -Condition (-not (Test-Path -LiteralPath $startDryRun.manifestPath)) `
    -Message "start.ps1 -DryRun should not create a runtime manifest."
Assert-True `
    -Condition (-not (($startDryRun | ConvertTo-Json -Depth 8).Contains($dryRunBundlePath))) `
    -Message "start.ps1 -DryRun should not expose the backend environment bundle path."

$emptyRoot = Join-Path $testRoot "empty"
$emptyManifestPath = Join-Path $emptyRoot "runtime-manifest.json"
if (Test-Path -LiteralPath $emptyManifestPath) {
    Remove-Item -LiteralPath $emptyManifestPath -Force
}
$statusNoManifest = & (Join-Path $PSScriptRoot "status.ps1") -RuntimeRoot $emptyRoot | ConvertFrom-Json
Assert-Equal `
    -Actual $statusNoManifest.state `
    -Expected "not-started" `
    -Message "status.ps1 should return not-started when no manifest exists."
Assert-True `
    -Condition (-not (Test-Path -LiteralPath $emptyManifestPath)) `
    -Message "status.ps1 should not create a manifest when none exists."

$stopNoManifest = & (Join-Path $PSScriptRoot "stop.ps1") -RuntimeRoot $emptyRoot | ConvertFrom-Json
Assert-Equal `
    -Actual $stopNoManifest.state `
    -Expected "not-started" `
    -Message "stop.ps1 should return not-started when no manifest exists."
Assert-True `
    -Condition (-not (Test-Path -LiteralPath $emptyManifestPath)) `
    -Message "stop.ps1 should not create a manifest when none exists."

$readinessContract = Invoke-InAcceptanceModule -ScriptBlock {
    $script:MockStatusCode = 200
    $script:MockThrow = $false
    function Invoke-WebRequest {
        param(
            [string] $Uri,
            [switch] $UseBasicParsing,
            [int] $TimeoutSec
        )

        if ($script:MockThrow) {
            throw "mock request failure"
        }

        return [pscustomobject]@{
            StatusCode = $script:MockStatusCode
        }
    }

    try {
        $script:MockStatusCode = 200
        $ok200 = Test-AcceptanceUrlReady -Url "https://example.test" -TimeoutSeconds 1
        $script:MockStatusCode = 399
        $ok399 = Test-AcceptanceUrlReady -Url "https://example.test" -TimeoutSeconds 1
        $script:MockStatusCode = 403
        $fail403 = Test-AcceptanceUrlReady -Url "https://example.test" -TimeoutSeconds 1
        $script:MockStatusCode = 404
        $fail404 = Test-AcceptanceUrlReady -Url "https://example.test" -TimeoutSeconds 1
        $script:MockThrow = $true
        $failThrown = Test-AcceptanceUrlReady -Url "https://example.test" -TimeoutSeconds 1

        return [pscustomobject]@{
            ok200 = $ok200
            ok399 = $ok399
            fail403 = $fail403
            fail404 = $fail404
            failThrown = $failThrown
        }
    } finally {
        Remove-Item -Path Function:\Invoke-WebRequest -Force -ErrorAction SilentlyContinue
        Remove-Variable -Name MockStatusCode -Scope Script -ErrorAction SilentlyContinue
        Remove-Variable -Name MockThrow -Scope Script -ErrorAction SilentlyContinue
    }
}
Assert-True `
    -Condition $readinessContract.ok200 `
    -Message "HTTP 200 readiness should be treated as success."
Assert-True `
    -Condition $readinessContract.ok399 `
    -Message "HTTP 399 readiness should be treated as success."
Assert-True `
    -Condition (-not $readinessContract.fail403) `
    -Message "HTTP 403 readiness should be treated as failure."
Assert-True `
    -Condition (-not $readinessContract.fail404) `
    -Message "HTTP 404 readiness should be treated as failure."
Assert-True `
    -Condition (-not $readinessContract.failThrown) `
    -Message "Request exceptions should be treated as failure."

$cleanupContract = Invoke-InAcceptanceModule `
    -ArgumentList @($repoRoot.Path, (Join-Path $testRoot "cleanup")) `
    -ScriptBlock {
        param(
            [string] $RepoRoot,
            [string] $RuntimeRoot
        )

        $script:StopRoles = New-Object System.Collections.ArrayList
        $script:SavedStates = New-Object System.Collections.ArrayList
        $script:OwnedRoles = @("tunnel", "frontend", "backend")

        function Get-AcceptanceStatus {
            param([string] $RuntimeRoot)
            return [pscustomobject]@{
                state = "not-started"
            }
        }

        function Find-AcceptanceCloudflared {
            param([string] $CloudflaredPath)
            return "C:\mock\cloudflared.exe"
        }

        function Get-AcceptancePowerShellPath {
            return "C:\Windows\System32\WindowsPowerShell\v1.0\powershell.exe"
        }

        function Start-AcceptanceOwnedProcess {
            param(
                [string] $Role,
                [string] $FilePath,
                [string[]] $ArgumentList,
                [string] $WorkingDirectory,
                [string] $StdOutPath,
                [string] $StdErrPath,
                [hashtable] $Environment = @{},
                [string[]] $ExcludedEnvironmentVariableNames = @()
            )

            return [pscustomobject]@{
                role = $Role
                pid = @{ tunnel = 101; frontend = 202; backend = 303 }[$Role]
                startTimeUtc = "2026-07-15T00:00:00.0000000Z"
                executablePath = "C:\mock\$Role.exe"
                commandLine = "$Role command"
                commandFingerprint = "$Role fingerprint"
                workingDirectory = $WorkingDirectory
                stdout = $StdOutPath
                stderr = $StdErrPath
            }
        }

        function Wait-AcceptanceTunnelUrl {
            param(
                [object] $TunnelService,
                [int] $TimeoutSeconds = 60
            )

            return "https://abc-123.trycloudflare.com"
        }

        function Read-AcceptanceBackendEnvironmentBundle {
            param(
                [string] $BackendEnvironmentPath,
                [string] $RepoRoot
            )

            return @{
                SPRING_DATASOURCE_URL = "synthetic"
                SPRING_DATASOURCE_USERNAME = "synthetic"
                SPRING_DATASOURCE_PASSWORD = "synthetic"
                JWT_SECRET = "synthetic"
                APP_BOOTSTRAP_TEST_USERS_ENABLED = "true"
                APP_BOOTSTRAP_TEST_USERS_DEFAULT_PASSWORD = "synthetic"
            }
        }

        function Save-AcceptanceManifest {
            param(
                [object] $Manifest,
                [string] $ManifestPath
            )

            [void] $script:SavedStates.Add($Manifest.state)
        }

        function Stop-AcceptanceOwnedService {
            param([object] $Service)

            [void] $script:StopRoles.Add($Service.role)
            if ($script:OwnedRoles -contains $Service.role) {
                $script:OwnedRoles = @($script:OwnedRoles | Where-Object { $_ -ne $Service.role })
                return [pscustomobject]@{
                    role = $Service.role
                    stopped = $true
                    reason = "owned-process-tree-stopped"
                }
            }

            return [pscustomobject]@{
                role = $Service.role
                stopped = $false
                reason = "not-running-or-not-owned"
            }
        }

        function Wait-AcceptanceCondition {
            param(
                [scriptblock] $Condition,
                [int] $TimeoutSeconds,
                [int] $IntervalMilliseconds = 1000,
                [string] $FailureMessage = "Timed out waiting for acceptance condition."
            )

            throw [System.InvalidOperationException]::new("simulated readiness failure")
        }

        $caughtFailure = $false
        try {
            Start-AcceptanceEnvironment `
                -RepoRoot $RepoRoot `
                -RuntimeRoot $RuntimeRoot `
                -CloudflaredPath "C:\mock\cloudflared.exe" `
                -BackendEnvironmentPath "synthetic-external-bundle.json" `
                -TunnelTimeoutSeconds 1 `
                -ReadinessTimeoutSeconds 1 | Out-Null
        } catch [System.InvalidOperationException] {
            $caughtFailure = $true
        }

        $firstCleanupOrder = @($script:StopRoles)
        $secondCleanup = Invoke-AcceptanceServiceCleanup -ServicesByName ([ordered]@{
            tunnel = [pscustomobject]@{ role = "tunnel"; pid = 101 }
            frontend = [pscustomobject]@{ role = "frontend"; pid = 202 }
            backend = [pscustomobject]@{ role = "backend"; pid = 303 }
        })

        return [pscustomobject]@{
            caughtFailure = $caughtFailure
            firstCleanupOrder = $firstCleanupOrder
            secondCleanupReasons = @($secondCleanup | ForEach-Object { $_.reason })
            savedStates = @($script:SavedStates)
        }
    }
Assert-True `
    -Condition $cleanupContract.caughtFailure `
    -Message "Start-AcceptanceEnvironment should surface a startup failure while still cleaning up."
Assert-SequenceEqual `
    -Actual $cleanupContract.firstCleanupOrder `
    -Expected @("tunnel", "frontend", "backend") `
    -Message "Abnormal-start cleanup order should be tunnel -> frontend -> backend."
Assert-SequenceEqual `
    -Actual $cleanupContract.secondCleanupReasons `
    -Expected @("not-running-or-not-owned", "not-running-or-not-owned", "not-running-or-not-owned") `
    -Message "Cleanup should be idempotent for already-stopped owned services."
Assert-True `
    -Condition (@($cleanupContract.savedStates) -contains "failed") `
    -Message "Abnormal-start cleanup should persist a failed manifest state before teardown."

$statusLifecycleContract = Invoke-InAcceptanceModule -ScriptBlock {
    function New-StatusService {
        param(
            [string] $Role,
            [int] $ProcessId
        )

        return [pscustomobject]@{
            role = $Role
            pid = $ProcessId
            startTimeUtc = "2026-08-17T00:00:00.0000000Z"
            executablePath = "C:\\acceptance\\$Role.exe"
            commandFingerprint = "$Role-fingerprint"
        }
    }

    $script:StatusManifest = [pscustomobject]@{
        state = "ready"
        publicBaseUrl = "https://abc-123.trycloudflare.com"
        localFrontendUrl = "http://127.0.0.1:5173"
        localApiUrl = "http://127.0.0.1:8080/api/tracks"
        publicApiUrl = "https://abc-123.trycloudflare.com/api/tracks"
        services = [pscustomobject]@{
            tunnel = New-StatusService -Role "tunnel" -ProcessId 101
            frontend = New-StatusService -Role "frontend" -ProcessId 102
            backend = New-StatusService -Role "backend" -ProcessId 103
        }
    }
    $script:StatusReadinessCalls = New-Object System.Collections.ArrayList

    function Get-AcceptanceManifest {
        param([string] $RuntimeRoot)

        return $script:StatusManifest
    }

    function Get-AcceptanceProcessSnapshot {
        param([int] $ProcessId)

        return $script:StatusSnapshots[$ProcessId]
    }

    function Test-AcceptanceUrlReady {
        param(
            [string] $Url,
            [int] $TimeoutSeconds = 5
        )

        [void] $script:StatusReadinessCalls.Add($Url)
        return [bool] $script:StatusReadiness[$Url]
    }

    function Set-StatusSnapshots {
        $script:StatusSnapshots = @{}
        foreach ($service in @(
            $script:StatusManifest.services.tunnel,
            $script:StatusManifest.services.frontend,
            $script:StatusManifest.services.backend
        )) {
            $script:StatusSnapshots[$service.pid] = [pscustomobject]@{
                pid = $service.pid
                startTimeUtc = $service.startTimeUtc
                executablePath = $service.executablePath
                commandFingerprint = $service.commandFingerprint
            }
        }
    }

    $script:StatusSnapshots = @{}
    $script:StatusReadiness = @{}
    $stale = Get-AcceptanceStatus -RuntimeRoot "C:\\synthetic\\stale"
    $staleReadinessCallCount = $script:StatusReadinessCalls.Count

    Set-StatusSnapshots
    $script:StatusSnapshots[103] = [pscustomobject]@{
        pid = 103
        startTimeUtc = "2026-08-17T00:00:00.0000000Z"
        executablePath = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe"
        commandFingerprint = "unrelated-chrome-fingerprint"
    }
    [void] $script:StatusReadinessCalls.Clear()
    $pidReused = Get-AcceptanceStatus -RuntimeRoot "C:\\synthetic\\pid-reused"
    $pidReuseReadinessCallCount = $script:StatusReadinessCalls.Count
    $pidReuseBackend = @($pidReused.services | Where-Object { $_.role -eq "backend" })[0]

    Set-StatusSnapshots
    $script:StatusReadiness = @{}
    $script:StatusReadiness[$script:StatusManifest.localFrontendUrl] = $true
    $script:StatusReadiness[$script:StatusManifest.localApiUrl] = $true
    $script:StatusReadiness[$script:StatusManifest.publicBaseUrl] = $false
    $script:StatusReadiness[$script:StatusManifest.publicApiUrl] = $false
    [void] $script:StatusReadinessCalls.Clear()
    $publicUnavailable = Get-AcceptanceStatus -RuntimeRoot "C:\\synthetic\\public-unavailable"

    $script:StatusReadiness[$script:StatusManifest.publicBaseUrl] = $true
    $script:StatusReadiness[$script:StatusManifest.publicApiUrl] = $true
    [void] $script:StatusReadinessCalls.Clear()
    $fullyReady = Get-AcceptanceStatus -RuntimeRoot "C:\\synthetic\\ready"

    return [pscustomobject]@{
        staleState = $stale.state
        staleCoreServicesOwned = $stale.ownership.coreServicesOwned
        staleReadinessCallCount = $staleReadinessCallCount
        pidReuseState = $pidReused.state
        pidReuseBackendOwned = $pidReuseBackend.owned
        pidReuseReadinessCallCount = $pidReuseReadinessCallCount
        publicUnavailableState = $publicUnavailable.state
        publicUnavailableCoreServicesOwned = $publicUnavailable.ownership.coreServicesOwned
        publicUnavailableLocalReady = $publicUnavailable.health.local.ready
        publicUnavailablePublicChecked = $publicUnavailable.health.public.checked
        publicUnavailablePublicReady = $publicUnavailable.health.public.ready
        fullyReadyState = $fullyReady.state
    }
}
Assert-Equal `
    -Actual $statusLifecycleContract.staleState `
    -Expected "stale" `
    -Message "A ready manifest without owned frontend and backend services should be stale."
Assert-True `
    -Condition (-not $statusLifecycleContract.staleCoreServicesOwned) `
    -Message "A stale ready manifest should report missing core service ownership."
Assert-Equal `
    -Actual $statusLifecycleContract.staleReadinessCallCount `
    -Expected 0 `
    -Message "Status should not probe readiness when no owned core runtime exists."
Assert-Equal `
    -Actual $statusLifecycleContract.pidReuseState `
    -Expected "stale" `
    -Message "A reused PID belonging to an unrelated executable should not preserve ready status."
Assert-True `
    -Condition (-not $statusLifecycleContract.pidReuseBackendOwned) `
    -Message "PID reuse should fail the backend ownership check."
Assert-Equal `
    -Actual $statusLifecycleContract.pidReuseReadinessCallCount `
    -Expected 0 `
    -Message "Status should not probe readiness when PID reuse invalidates a core service."
Assert-Equal `
    -Actual $statusLifecycleContract.publicUnavailableState `
    -Expected "degraded" `
    -Message "An owned local runtime with an unavailable public tunnel should be degraded, not stale."
Assert-True `
    -Condition $statusLifecycleContract.publicUnavailableCoreServicesOwned `
    -Message "An owned local runtime should retain its core ownership evidence."
Assert-True `
    -Condition $statusLifecycleContract.publicUnavailableLocalReady `
    -Message "Local readiness should remain visible when public readiness fails."
Assert-True `
    -Condition $statusLifecycleContract.publicUnavailablePublicChecked `
    -Message "Public readiness should be recorded separately for an owned tunnel."
Assert-True `
    -Condition (-not $statusLifecycleContract.publicUnavailablePublicReady) `
    -Message "Public readiness should report the unavailable tunnel honestly."
Assert-Equal `
    -Actual $statusLifecycleContract.fullyReadyState `
    -Expected "ready" `
    -Message "A fully owned runtime with local and public readiness should remain ready."

if ($script:Failures.Count -gt 0) {
    $result = [pscustomobject]@{
        status = "failed"
        analyzer = $analyzerStatus
        failures = @($script:Failures)
    }
    $exitCode = 1
} else {
    $result = [pscustomobject]@{
        status = "passed"
        analyzer = $analyzerStatus
        checks = @(
            "parser",
            "quick-tunnel-url-parser",
            "public-base-url-validation",
            "dry-run-contract",
            "status-no-manifest",
            "acceptance-status-lifecycle-classification",
            "stop-no-manifest",
            "readiness-http-status-contract",
            "abnormal-start-cleanup-contract",
            "start-finally-structure",
            "secret-free-dry-run-output"
        )
    }
    $exitCode = 0
}

Remove-Item -LiteralPath $testRoot -Recurse -Force -ErrorAction SilentlyContinue
$result | ConvertTo-Json -Depth 5
exit $exitCode
