[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"
$script:Failures = New-Object System.Collections.ArrayList
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
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

function Assert-SequenceEqual {
    param(
        [object[]] $Actual,
        [object[]] $Expected,
        [Parameter(Mandatory = $true)]
        [string] $Message
    )

    if ((@($Actual) -join "|") -ne (@($Expected) -join "|")) {
        [void] $script:Failures.Add($Message)
    }
}

function Invoke-InAcceptanceModule {
    param(
        [Parameter(Mandatory = $true)]
        [scriptblock] $ScriptBlock,
        [object[]] $ArgumentList = @()
    )

    $module = Get-Module AcceptanceLifecycle
    return & $module $ScriptBlock @ArgumentList
}

function Write-JsonFixture {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Path,
        [Parameter(Mandatory = $true)]
        [object] $Value
    )

    [System.IO.File]::WriteAllText($Path, ($Value | ConvertTo-Json -Depth 5))
}

function Invoke-BundleRead {
    param([string] $Path)

    return Invoke-InAcceptanceModule `
        -ArgumentList @($repoRoot, $Path) `
        -ScriptBlock {
            param([string] $RepoRoot, [string] $Path)
            Read-AcceptanceBackendEnvironmentBundle `
                -BackendEnvironmentPath $Path `
                -RepoRoot $RepoRoot
        }
}

function Assert-BundleFailure {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Path,
        [Parameter(Mandatory = $true)]
        [string] $Case,
        [Parameter(Mandatory = $true)]
        [string[]] $ForbiddenText
    )

    try {
        Invoke-BundleRead -Path $Path | Out-Null
        [void] $script:Failures.Add("Bundle validation should reject case: $Case")
    } catch {
        $message = $_.Exception.Message
        foreach ($forbidden in $ForbiddenText) {
            if (-not [string]::IsNullOrEmpty($forbidden) -and $message.Contains($forbidden)) {
                [void] $script:Failures.Add("Bundle validation error exposed protected text for case: $Case")
            }
        }
    }
}

$testRoot = Join-Path `
    ([System.IO.Path]::GetTempPath()) `
    ("atstudio-wi040-" + [guid]::NewGuid().ToString("N"))
$marker = "WI040_" + [guid]::NewGuid().ToString("N")

try {
    New-Item -ItemType Directory -Path $testRoot | Out-Null
    $missingPathRejected = $false
    try {
        & (Join-Path $PSScriptRoot "start.ps1") `
            -RuntimeRoot (Join-Path $testRoot "missing-path") | Out-Null
    } catch {
        $missingPathRejected = $true
        Assert-True `
            -Condition (-not $_.Exception.Message.Contains($testRoot)) `
            -Message "Missing bundle path refusal should not expose a runtime path."
    }
    Assert-True `
        -Condition $missingPathRejected `
        -Message "Non-dry-run start should reject a missing backend environment path before spawn."

    $required = [ordered]@{
        SPRING_DATASOURCE_URL = "jdbc:synthetic:$marker"
        SPRING_DATASOURCE_USERNAME = "user_$marker"
        SPRING_DATASOURCE_PASSWORD = "db_$marker"
        JWT_SECRET = "jwt_$marker"
        APP_BOOTSTRAP_TEST_USERS_ENABLED = "true"
        APP_BOOTSTRAP_TEST_USERS_DEFAULT_PASSWORD = "bootstrap_$marker"
    }
    $currentOptionalNames = @(
        "PAYMENT_BILLING_KEY_ACTIVE_KEY_ID",
        "PAYMENT_BILLING_KEY_0_ID",
        "PAYMENT_BILLING_KEY_0_SECRET",
        "APP_PAYMENT_SCHEDULER_ZONE"
    )
    $currentOptional = [ordered]@{
        PAYMENT_BILLING_KEY_ACTIVE_KEY_ID = "active_$marker"
        PAYMENT_BILLING_KEY_0_ID = "key_$marker"
        PAYMENT_BILLING_KEY_0_SECRET = "key_secret_$marker"
        APP_PAYMENT_SCHEDULER_ZONE = "Asia/Seoul"
    }
    $obsoleteNames = @(
        "PAYMENT_BILLING_KEY_ENCRYPTION_SECRET",
        "APP_PAYMENT_PROVIDER",
        "TOSS_CONFIRM_URL"
    )
    $allowlistContract = Invoke-InAcceptanceModule -ScriptBlock {
        return [pscustomobject]@{
            optional = @($script:OptionalBackendEnvironmentVariableNames)
            allowed = @($script:BackendEnvironmentVariableNames)
        }
    }
    foreach ($name in $currentOptionalNames) {
        Assert-True `
            -Condition ($allowlistContract.optional -ccontains $name) `
            -Message "Current backend environment name should be optional and allowlisted: $name"
    }
    foreach ($name in $obsoleteNames) {
        Assert-True `
            -Condition (-not ($allowlistContract.allowed -ccontains $name)) `
            -Message "Obsolete backend environment name should not be allowlisted: $name"
    }

    $validPath = Join-Path $testRoot "valid.json"
    $valid = [ordered]@{} + $required
    foreach ($name in $currentOptionalNames) {
        $valid[$name] = $currentOptional[$name]
    }
    $valid.MAIL_PASSWORD = "mail_$marker"
    Write-JsonFixture -Path $validPath -Value $valid
    $loaded = Invoke-BundleRead -Path $validPath
    Assert-True `
        -Condition ($loaded.Count -eq ($required.Count + $currentOptional.Count + 1)) `
        -Message "Valid bundle should return required and optional allowlisted names."
    foreach ($name in $currentOptionalNames) {
        Assert-True `
            -Condition ($loaded.ContainsKey($name)) `
            -Message "Bundle validation should accept current backend environment name: $name"
    }

    $forbidden = @($testRoot, $marker, $required.SPRING_DATASOURCE_URL)
    Assert-BundleFailure `
        -Path (Join-Path $testRoot "missing.json") `
        -Case "missing-file" `
        -ForbiddenText $forbidden
    Assert-BundleFailure `
        -Path (Join-Path $repoRoot "scripts\acceptance\start.ps1") `
        -Case "in-repo" `
        -ForbiddenText $forbidden
    Assert-BundleFailure `
        -Path $testRoot `
        -Case "non-file" `
        -ForbiddenText $forbidden

    $malformedPath = Join-Path $testRoot "malformed.json"
    [System.IO.File]::WriteAllText($malformedPath, '{"invalid":')
    Assert-BundleFailure -Path $malformedPath -Case "malformed" -ForbiddenText $forbidden

    $nestedPath = Join-Path $testRoot "nested.json"
    $nested = [ordered]@{} + $required
    $nested.MAIL_PASSWORD = @{ value = "nested_$marker" }
    Write-JsonFixture -Path $nestedPath -Value $nested
    Assert-BundleFailure -Path $nestedPath -Case "nested" -ForbiddenText $forbidden

    $unknownPath = Join-Path $testRoot "unknown.json"
    $unknown = [ordered]@{} + $required
    $unknown.UNKNOWN_BACKEND_VALUE = "unknown_$marker"
    Write-JsonFixture -Path $unknownPath -Value $unknown
    Assert-BundleFailure -Path $unknownPath -Case "unknown-key" -ForbiddenText $forbidden

    foreach ($name in $obsoleteNames) {
        $obsoletePath = Join-Path $testRoot "obsolete-$($name.ToLowerInvariant()).json"
        $obsolete = [ordered]@{} + $required
        $obsolete[$name] = "obsolete_$marker"
        Write-JsonFixture -Path $obsoletePath -Value $obsolete
        Assert-BundleFailure `
            -Path $obsoletePath `
            -Case "obsolete-$($name.ToLowerInvariant())" `
            -ForbiddenText $forbidden
    }

    $blankPath = Join-Path $testRoot "blank.json"
    $blank = [ordered]@{} + $required
    $blank.SPRING_DATASOURCE_PASSWORD = "   "
    Write-JsonFixture -Path $blankPath -Value $blank
    Assert-BundleFailure -Path $blankPath -Case "blank-value" -ForbiddenText $forbidden

    $missingRequiredPath = Join-Path $testRoot "missing-required.json"
    $missingRequired = [ordered]@{} + $required
    $missingRequired.Remove("JWT_SECRET")
    Write-JsonFixture -Path $missingRequiredPath -Value $missingRequired
    Assert-BundleFailure `
        -Path $missingRequiredPath `
        -Case "missing-required" `
        -ForbiddenText $forbidden

    $disabledBootstrapPath = Join-Path $testRoot "disabled-bootstrap.json"
    $disabledBootstrap = [ordered]@{} + $required
    $disabledBootstrap.APP_BOOTSTRAP_TEST_USERS_ENABLED = "false"
    Write-JsonFixture -Path $disabledBootstrapPath -Value $disabledBootstrap
    Assert-BundleFailure `
        -Path $disabledBootstrapPath `
        -Case "disabled-bootstrap" `
        -ForbiddenText $forbidden

    $propagatedBundle = [ordered]@{} + $required
    foreach ($name in $currentOptionalNames) {
        $propagatedBundle[$name] = $currentOptional[$name]
    }
    $isolation = Invoke-InAcceptanceModule `
        -ArgumentList @($propagatedBundle, $marker, $testRoot) `
        -ScriptBlock {
            param([hashtable] $BackendBundle, [string] $Marker, [string] $TestRoot)

            $script:SpawnChecks = New-Object System.Collections.ArrayList
            $probeScriptPath = Join-Path $TestRoot "observe-child-environment.ps1"
            $probeNamesPath = Join-Path $TestRoot "observe-child-environment-names.json"
            [System.IO.File]::WriteAllText(
                $probeNamesPath,
                (@($script:BackendEnvironmentVariableNames) | ConvertTo-Json -Compress)
            )
            [System.IO.File]::WriteAllText($probeScriptPath, @'
param(
    [Parameter(Mandatory = $true)]
    [string] $OutputPath,
    [Parameter(Mandatory = $true)]
    [string] $NamesPath
)

$Names = Get-Content -Raw -LiteralPath $NamesPath | ConvertFrom-Json
$observed = [ordered]@{}
foreach ($name in $Names) {
    $observed[$name] = [System.Environment]::GetEnvironmentVariable($name, "Process")
}
$temporaryOutputPath = "$OutputPath.$PID.tmp"
[System.IO.File]::WriteAllText($temporaryOutputPath, ($observed | ConvertTo-Json -Compress))
[System.IO.File]::Move($temporaryOutputPath, $OutputPath)
[Console]::Out.WriteLine("synthetic-child-stdout")
[Console]::Error.WriteLine("synthetic-child-stderr")
Start-Sleep -Seconds 2
'@)
            $currentOptionalNames = @(
                "PAYMENT_BILLING_KEY_ACTIVE_KEY_ID",
                "PAYMENT_BILLING_KEY_0_ID",
                "PAYMENT_BILLING_KEY_0_SECRET",
                "APP_PAYMENT_SCHEDULER_ZONE"
            )
            $parentNames = @($script:BackendEnvironmentVariableNames)
            $parentBefore = @{}
            foreach ($name in $parentNames) {
                $parentBefore[$name] = [System.Environment]::GetEnvironmentVariable($name, "Process")
            }

            try {
                $common = New-AcceptanceChildEnvironment `
                    -PublicBaseUrl "https://abc-123.trycloudflare.com"
                $backend = New-AcceptanceBackendEnvironment `
                    -ChildEnvironment $common `
                    -BackendEnvironmentBundle $BackendBundle
                $powerShell = Get-AcceptancePowerShellPath
                foreach ($role in @("tunnel", "backend", "frontend")) {
                    $environment = if ($role -eq "backend") { $backend } else { $common }
                    if ($role -eq "tunnel") {
                        $environment = @{}
                    }
                    $observationPath = Join-Path $TestRoot "$role-environment.json"
                    $service = Start-AcceptanceOwnedProcess `
                        -Role $role `
                        -FilePath $powerShell `
                        -ArgumentList (@(
                            "-NoProfile",
                            "-ExecutionPolicy",
                            "Bypass",
                            "-File",
                            $probeScriptPath,
                            "-OutputPath",
                            $observationPath,
                            "-NamesPath",
                            $probeNamesPath
                        )) `
                        -WorkingDirectory $TestRoot `
                        -StdOutPath (Join-Path $TestRoot "$role.out") `
                        -StdErrPath (Join-Path $TestRoot "$role.err") `
                        -Environment $environment `
                        -ExcludedEnvironmentVariableNames $script:BackendEnvironmentVariableNames
                    $deadline = (Get-Date).AddSeconds(5)
                    $observed = $null
                    while ($null -eq $observed) {
                        if ((Get-Date) -gt $deadline) {
                            throw "Synthetic child environment observation did not publish a complete payload."
                        }
                        if (Test-Path -LiteralPath $observationPath) {
                            $candidate = Get-Content -Raw -LiteralPath $observationPath | ConvertFrom-Json
                            $hasAllBackendProperties =
                                ($candidate -is [pscustomobject]) -and
                                (@($script:BackendEnvironmentVariableNames | Where-Object {
                                    $null -eq $candidate.PSObject.Properties[$_]
                                }).Count -eq 0)
                            if ($hasAllBackendProperties) {
                                $observed = $candidate
                                continue
                            }
                        }
                        Start-Sleep -Milliseconds 50
                    }
                    $stdout = Get-Content -Raw -LiteralPath (Join-Path $TestRoot "$role.out")
                    $stderr = Get-Content -Raw -LiteralPath (Join-Path $TestRoot "$role.err")
                    $logsCapturedWhileRunning =
                        (Get-Process -Id $service.pid -ErrorAction SilentlyContinue) -and
                        $stdout.Contains("synthetic-child-stdout") -and
                        $stderr.Contains("synthetic-child-stderr")
                    if ($role -eq "frontend") {
                        Stop-Process -Id $service.pid -Force
                    }
                    Wait-Process -Id $service.pid -Timeout 5 -ErrorAction SilentlyContinue
                    $logHandlesReleased = $false
                    $releaseDeadline = (Get-Date).AddSeconds(5)
                    while (-not $logHandlesReleased -and (Get-Date) -le $releaseDeadline) {
                        $handles = @()
                        try {
                            foreach ($path in @(
                                (Join-Path $TestRoot "$role.out"),
                                (Join-Path $TestRoot "$role.err")
                            )) {
                                $handles += [System.IO.File]::Open(
                                    $path,
                                    [System.IO.FileMode]::Open,
                                    [System.IO.FileAccess]::ReadWrite,
                                    [System.IO.FileShare]::None
                                )
                            }
                            $logHandlesReleased = $true
                        } catch {
                            Start-Sleep -Milliseconds 50
                        } finally {
                            foreach ($handle in $handles) {
                                $handle.Dispose()
                            }
                        }
                    }
                    $present = @(
                        $script:BackendEnvironmentVariableNames |
                            Where-Object {
                                $null -ne $observed.PSObject.Properties[$_].Value
                            }
                    )
                    $matchesExpected = @(
                        $BackendBundle.Keys |
                            Where-Object {
                                $observed.PSObject.Properties[$_].Value -cne $BackendBundle[$_]
                            }
                    ).Count -eq 0
                    [void] $script:SpawnChecks.Add([pscustomobject]@{
                        role = $role
                        backendNameCount = $present.Count
                        backendNames = @($present)
                        matchesExpected = $matchesExpected
                        logsCapturedWhileRunning = $logsCapturedWhileRunning
                        logHandlesReleased = $logHandlesReleased
                    })
                }

                return [pscustomobject]@{
                    checks = @($script:SpawnChecks)
                    parentRestored = @($parentNames | Where-Object {
                        [System.Environment]::GetEnvironmentVariable($_, "Process") -ceq $parentBefore[$_]
                    }).Count -eq $parentNames.Count
                }
            } finally {
                Remove-Variable -Name SpawnChecks -Scope Script -ErrorAction SilentlyContinue
            }
        }

    Assert-SequenceEqual `
        -Actual @($isolation.checks | ForEach-Object { $_.role }) `
        -Expected @("tunnel", "backend", "frontend") `
        -Message "Isolation test should inspect tunnel, backend, then frontend spawns."
    Assert-True `
        -Condition ($isolation.checks[0].backendNameCount -eq 0) `
        -Message "Tunnel spawn should receive no backend-only variable names."
    Assert-True `
        -Condition ($isolation.checks[1].backendNameCount -eq $propagatedBundle.Count) `
        -Message "Backend spawn should receive every supplied backend variable name."
    Assert-True `
        -Condition $isolation.checks[1].matchesExpected `
        -Message "Backend spawn should receive the supplied synthetic values."
    Assert-True `
        -Condition ($isolation.checks[2].backendNameCount -eq 0) `
        -Message "Frontend spawn should receive no backend-only variable names."
    Assert-True `
        -Condition $isolation.parentRestored `
        -Message "Launcher environment should remain unchanged before frontend spawn."
    Assert-True `
        -Condition (@($isolation.checks | Where-Object { -not $_.logsCapturedWhileRunning }).Count -eq 0) `
        -Message "Every synthetic child should continuously redirect stdout and stderr to its log files."
    Assert-True `
        -Condition (@($isolation.checks | Where-Object { -not $_.logHandlesReleased }).Count -eq 0) `
        -Message "Child log handles should close after natural and forced process exit."
    foreach ($name in $currentOptionalNames) {
        Assert-True `
            -Condition ($isolation.checks[1].backendNames -ccontains $name) `
            -Message "Backend spawn should receive current backend environment name: $name"
        Assert-True `
            -Condition (-not ($isolation.checks[0].backendNames -ccontains $name)) `
            -Message "Tunnel spawn should not receive current backend environment name: $name"
        Assert-True `
            -Condition (-not ($isolation.checks[2].backendNames -ccontains $name)) `
            -Message "Frontend spawn should not receive current backend environment name: $name"
    }
    $order = Invoke-InAcceptanceModule `
        -ArgumentList @($repoRoot, (Join-Path $testRoot "order"), $validPath, $required) `
        -ScriptBlock {
            param(
                [string] $RepoRoot,
                [string] $RuntimeRoot,
                [string] $BundlePath,
                [hashtable] $Required
            )

            $script:Events = New-Object System.Collections.ArrayList
            $script:Spawns = New-Object System.Collections.ArrayList
            $script:BundleLoaded = $false
            function Get-AcceptanceStatus { return [pscustomobject]@{ state = "not-started" } }
            function Find-AcceptanceCloudflared { return "mock-cloudflared" }
            function Get-AcceptancePowerShellPath { return "mock-powershell" }
            function Wait-AcceptanceTunnelUrl { return "https://abc-123.trycloudflare.com" }
            function Save-AcceptanceManifest { param([object] $Manifest, [string] $ManifestPath) }
            function Wait-AcceptanceCondition { return $true }
            function Read-AcceptanceBackendEnvironmentBundle {
                param([string] $BackendEnvironmentPath, [string] $RepoRoot)
                [void] $script:Events.Add("load-bundle")
                $script:BundleLoaded = $true
                return $Required
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
                [void] $script:Events.Add("spawn-$Role")
                $backendNames = @(
                    $Environment.Keys |
                        Where-Object { $script:BackendEnvironmentVariableNames -contains $_ }
                )
                $backendValues = @(
                    $Environment.Values |
                        Where-Object {
                            @(
                                $Required.GetEnumerator() |
                                    Where-Object { $_.Key -ne "APP_BOOTSTRAP_TEST_USERS_ENABLED" } |
                                    ForEach-Object { $_.Value }
                            ) -ccontains [string] $_
                        }
                )
                $record = [pscustomobject]@{
                    role = $Role
                    pid = 100
                    backendNameCount = $backendNames.Count
                    backendValueCount = $backendValues.Count
                    bundleLoaded = $script:BundleLoaded
                    exposesBundlePath = (($ArgumentList -join " ").Contains($BundlePath)) -or
                        (@($Environment.Values) -contains $BundlePath)
                    excludesAllBackendNames = @(
                        $script:BackendEnvironmentVariableNames |
                            Where-Object { $ExcludedEnvironmentVariableNames -notcontains $_ }
                    ).Count -eq 0
                }
                [void] $script:Spawns.Add($record)
                return $record
            }

            try {
                Start-AcceptanceEnvironment `
                    -RepoRoot $RepoRoot `
                    -RuntimeRoot $RuntimeRoot `
                    -CloudflaredPath "mock-cloudflared" `
                    -BackendEnvironmentPath $BundlePath `
                    -TunnelTimeoutSeconds 1 `
                    -ReadinessTimeoutSeconds 1 | Out-Null

                return [pscustomobject]@{
                    events = @($script:Events)
                    spawns = @($script:Spawns)
                }
            } finally {
                foreach ($name in @(
                    "Get-AcceptanceStatus",
                    "Find-AcceptanceCloudflared",
                    "Get-AcceptancePowerShellPath",
                    "Wait-AcceptanceTunnelUrl",
                    "Save-AcceptanceManifest",
                    "Wait-AcceptanceCondition",
                    "Read-AcceptanceBackendEnvironmentBundle",
                    "Start-AcceptanceOwnedProcess"
                )) {
                    Remove-Item -Path "Function:\$name" -Force -ErrorAction SilentlyContinue
                }
                Remove-Variable -Name Events -Scope Script -ErrorAction SilentlyContinue
                Remove-Variable -Name Spawns -Scope Script -ErrorAction SilentlyContinue
                Remove-Variable -Name BundleLoaded -Scope Script -ErrorAction SilentlyContinue
            }
        }
    Assert-SequenceEqual `
        -Actual $order.events `
        -Expected @("spawn-tunnel", "load-bundle", "spawn-backend", "spawn-frontend") `
        -Message "Bundle loading should occur after tunnel spawn and before backend/frontend spawns."
    Assert-True `
        -Condition (-not $order.spawns[0].bundleLoaded) `
        -Message "Tunnel should spawn before the backend bundle is loaded."
    Assert-True `
        -Condition ($order.spawns[0].backendNameCount -eq 0) `
        -Message "Tunnel launch record should contain no backend bundle names."
    Assert-True `
        -Condition ($order.spawns[0].backendValueCount -eq 0) `
        -Message "Tunnel launch record should contain no backend bundle values."
    Assert-True `
        -Condition ($order.spawns[1].backendNameCount -eq $required.Count) `
        -Message "Backend launch record should contain the required backend bundle names."
    Assert-True `
        -Condition ($order.spawns[2].backendNameCount -eq 0) `
        -Message "Frontend launch record should contain no backend bundle names."
    Assert-True `
        -Condition ($order.spawns[2].backendValueCount -eq 0) `
        -Message "Frontend launch record should contain no backend bundle values."
    Assert-True `
        -Condition (@($order.spawns | Where-Object { $_.exposesBundlePath }).Count -eq 0) `
        -Message "Child launch records should not receive the backend bundle path."
    Assert-True `
        -Condition (@($order.spawns | Where-Object { -not $_.excludesAllBackendNames }).Count -eq 0) `
        -Message "Every child spawn should explicitly suppress inherited backend names."
} finally {
    Remove-Item -LiteralPath $testRoot -Recurse -Force -ErrorAction SilentlyContinue
}

if ($script:Failures.Count -gt 0) {
    [pscustomobject]@{
        status = "failed"
        failures = @($script:Failures)
    } | ConvertTo-Json -Depth 5
    exit 1
}

[pscustomobject]@{
    status = "passed"
    checks = @(
        "external-bundle-validation",
        "required-and-allowlisted-names",
        "current-v2-and-scheduler-name-acceptance",
        "obsolete-payment-name-rejection",
        "safe-validation-errors",
        "child-process-environment-isolation",
        "backend-environment-restoration",
        "continuous-child-log-drainage",
        "tunnel-before-bundle-load-order",
        "temporary-fixture-cleanup"
    )
} | ConvertTo-Json -Depth 5
