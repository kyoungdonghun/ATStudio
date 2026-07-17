param(
    [ValidateSet("Disposable", "RecreateLocal")]
    [string]$Mode = "Disposable",
    [string]$Workspace = (Resolve-Path ".").Path,
    [ValidateRange(30, 600)]
    [int]$CommandTimeoutSeconds = 300
)

$ErrorActionPreference = "Stop"
$workspacePath = (Resolve-Path -LiteralPath $Workspace).Path
$artifactDir = Join-Path $workspacePath "deliverables\agent\WI-20260717-ATS-004"
$generatedRedactedLogNames = @(
    "manager-compile.log",
    "database-preflight.log",
    "disposable-create.log",
    "schema-first-apply.log",
    "schema-second-apply.log",
    "manifest-before-runtime.log",
    "manifest-after-races.log",
    "hibernate-validate.log",
    "mysql-races.log",
    "disposable-drop.log",
    "local-recreate.log",
    "local-manifest-final.log",
    "Disposable-summary.log",
    "RecreateLocal-summary.log"
)
$generatedRedactedLogNames | ForEach-Object {
    $existingLog = Join-Path $artifactDir $_
    if (Test-Path -LiteralPath $existingLog -PathType Leaf) {
        $lockProbe = $null
        try {
            $lockProbe = [IO.File]::Open(
                $existingLog,
                [IO.FileMode]::Open,
                [IO.FileAccess]::ReadWrite,
                [IO.FileShare]::None)
            $lockProbe.Dispose()
            $lockProbe = $null
            Remove-Item -LiteralPath $existingLog -Force
        } catch [IO.IOException] {
            # This run writes to a unique directory, so a locked prior log is left untouched.
        } finally {
            if ($null -ne $lockProbe) {
                $lockProbe.Dispose()
            }
        }
    }
}
$runName = "run-{0}-{1}" -f (
    Get-Date -Format "yyyyMMdd-HHmmss"),
    ([guid]::NewGuid().ToString("N").Substring(0, 8))
$runArtifactDir = Join-Path $artifactDir $runName
$managerSource = Join-Path $artifactDir "V1MysqlProofManager.java"
$applicationLocal = Join-Path $workspacePath "application-local.yml"
$manualSqlDir = Join-Path $workspacePath "src\main\resources\db\manual"
$tempBase = [IO.Path]::GetFullPath([IO.Path]::GetTempPath())
$tempRoot = Join-Path $tempBase ("atstudio-wi004-" + [guid]::NewGuid().ToString("N"))
$classesDir = Join-Path $tempRoot "classes"
$disposableName = "ats_wi004_{0}_{1}" -f (
    Get-Date -Format "yyyyMMdd"),
    ([guid]::NewGuid().ToString("N").Substring(0, 8))
$disposableCreated = $false

New-Item -ItemType Directory -Force -Path $artifactDir | Out-Null
New-Item -ItemType Directory -Path $runArtifactDir | Out-Null
New-Item -ItemType Directory -Force -Path $classesDir | Out-Null

function Read-Scalar([string]$Text, [string]$Key) {
    $match = [regex]::Match($Text, "(?m)^\s*$([regex]::Escape($Key))\s*:\s*(.+?)\s*$")
    if (-not $match.Success) {
        return ""
    }
    return $match.Groups[1].Value.Trim().Trim("'").Trim('"')
}

function Get-SourceDatasource {
    $url = $env:SPRING_DATASOURCE_URL
    $username = $env:SPRING_DATASOURCE_USERNAME
    $password = $env:SPRING_DATASOURCE_PASSWORD
    $source = "process-env"
    if ([string]::IsNullOrWhiteSpace($url) -or
        [string]::IsNullOrWhiteSpace($username) -or
        [string]::IsNullOrWhiteSpace($password)) {
        if (-not (Test-Path -LiteralPath $applicationLocal -PathType Leaf)) {
            throw "Datasource credential source is unavailable."
        }
        $configText = Get-Content -Raw -LiteralPath $applicationLocal
        $url = Read-Scalar $configText "url"
        $username = Read-Scalar $configText "username"
        $password = Read-Scalar $configText "password"
        $source = "application-local-yml"
    }
    if ([string]::IsNullOrWhiteSpace($url) -or
        [string]::IsNullOrWhiteSpace($username) -or
        [string]::IsNullOrWhiteSpace($password) -or
        $url.Contains("REPLACE_WITH") -or
        $username.Contains("REPLACE_WITH") -or
        $password.Contains("REPLACE_WITH")) {
        throw "Datasource credentials are missing or placeholders."
    }
    $match = [regex]::Match($url, "^jdbc:mysql://([^/]+)/([^?]+)(\?.*)?$")
    if (-not $match.Success) {
        throw "Only a jdbc:mysql datasource with an explicit database is supported."
    }
    $hostPort = $match.Groups[1].Value.ToLowerInvariant()
    if ($hostPort -notmatch '^(localhost|127\.0\.0\.1|\[::1\])(?::\d+)?$') {
        throw "Only a loopback MySQL datasource is allowed."
    }
    if ($match.Groups[2].Value -cne "atstudio") {
        throw "Datasource database name must be exactly atstudio."
    }
    return [pscustomobject]@{
        Url = $url
        Username = $username
        Password = $password
        HostPort = $match.Groups[1].Value
        Query = $match.Groups[3].Value
        Source = $source
    }
}

function Get-TargetUrl($Datasource, [string]$Database) {
    $query = $Datasource.Query
    if (-not [string]::IsNullOrWhiteSpace($query)) {
        $parts = $query.TrimStart("?").Split("&") | Where-Object {
            -not $_.ToLowerInvariant().StartsWith("createdatabaseifnotexist=")
        }
        $query = if ($parts.Count -gt 0) { "?" + ($parts -join "&") } else { "" }
    }
    return "jdbc:mysql://$($Datasource.HostPort)/$Database$query"
}

function Get-RedactedText([string]$Text, $Datasource) {
    $redacted = [regex]::Replace(
        $Text,
        'jdbc:mysql://[^\s\]\)''"]+',
        '[REDACTED_JDBC_URL]')
    $redacted = [regex]::Replace(
        $redacted,
        'ats_wi004_\d{8}_[a-z0-9]{8}',
        '[WI004_DISPOSABLE]')
    foreach ($value in @($Datasource.Url, $Datasource.Username, $Datasource.Password)) {
        if (-not [string]::IsNullOrWhiteSpace($value)) {
            $redacted = [regex]::Replace(
                $redacted,
                [regex]::Escape($value),
                '[REDACTED]')
        }
    }
    return $redacted
}

function Invoke-BoundedCommand(
    [string]$Label,
    [string]$FilePath,
    [string[]]$ArgumentList,
    $Datasource
) {
    $stdoutPath = Join-Path $tempRoot ($Label + "-stdout.log")
    $stderrPath = Join-Path $tempRoot ($Label + "-stderr.log")
    $exitCodePath = Join-Path $tempRoot ($Label + "-exit-code.txt")
    $commandPath = Join-Path $tempRoot ($Label + "-command.cmd")
    $logPath = Join-Path $runArtifactDir ($Label + ".log")
    $process = $null
    try {
        $quotedArguments = $ArgumentList | ForEach-Object {
            '"' + $_.Replace('"', '""') + '"'
        }
        $commandLines = @(
            "@echo off",
            ('call "' + $FilePath + '" ' + ($quotedArguments -join ' ')),
            'set "ATSTUDIO_EXIT=%ERRORLEVEL%"',
            ('> "' + $exitCodePath + '" echo %ATSTUDIO_EXIT%'),
            'exit /b %ATSTUDIO_EXIT%'
        )
        Set-Content -LiteralPath $commandPath -Value $commandLines -Encoding Ascii
        $process = Start-Process `
            -FilePath $commandPath `
            -WorkingDirectory $workspacePath `
            -WindowStyle Hidden `
            -PassThru `
            -RedirectStandardOutput $stdoutPath `
            -RedirectStandardError $stderrPath
        $timedOut = -not $process.WaitForExit($CommandTimeoutSeconds * 1000)
        if ($timedOut) {
            & taskkill /PID $process.Id /T /F *> $null
        }
        $process.WaitForExit()
        $raw = ""
        if (Test-Path -LiteralPath $stdoutPath) {
            $raw += Get-Content -Raw -LiteralPath $stdoutPath
        }
        if (Test-Path -LiteralPath $stderrPath) {
            $raw += [Environment]::NewLine + (Get-Content -Raw -LiteralPath $stderrPath)
        }
        $redacted = Get-RedactedText $raw $Datasource
        Set-Content -LiteralPath $logPath -Value $redacted -NoNewline
        if ($timedOut) {
            throw "$Label exceeded the command timeout."
        }
        if (-not (Test-Path -LiteralPath $exitCodePath)) {
            throw "$Label did not report an exit code."
        }
        $exitCode = [int](Get-Content -Raw -LiteralPath $exitCodePath).Trim()
        if ($exitCode -ne 0) {
            throw "$Label failed with exit code $exitCode."
        }
        return $redacted
    } finally {
        Remove-Item -LiteralPath $stdoutPath -Force -ErrorAction SilentlyContinue
        Remove-Item -LiteralPath $stderrPath -Force -ErrorAction SilentlyContinue
        Remove-Item -LiteralPath $exitCodePath -Force -ErrorAction SilentlyContinue
        Remove-Item -LiteralPath $commandPath -Force -ErrorAction SilentlyContinue
    }
}

function Invoke-Manager(
    [string]$Label,
    [string]$ManagerMode,
    [string]$Database,
    $Datasource,
    [string]$ConnectorJar,
    [bool]$ApprovedLocalRecreate = $false
) {
    return Invoke-BoundedCommand `
        -Label $Label `
        -FilePath "java" `
        -ArgumentList @(
            "-cp",
            "$classesDir;$ConnectorJar",
            "V1MysqlProofManager",
            "--workspace",
            $workspacePath,
            "--database",
            $Database,
            "--mode",
            $ManagerMode,
            "--approved-local-recreate",
            $ApprovedLocalRecreate.ToString().ToLowerInvariant()
        ) `
        -Datasource $Datasource
}

function Assert-RuntimeStopped {
    $listeners = Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue |
        Where-Object { $_.LocalPort -in 5173, 8080 }
    $cloudflared = Get-Process -Name "cloudflared" -ErrorAction SilentlyContinue
    Write-Output ("runtime.port5173.listeners={0}" -f (
        ($listeners | Where-Object LocalPort -eq 5173 | Measure-Object).Count))
    Write-Output ("runtime.port8080.listeners={0}" -f (
        ($listeners | Where-Object LocalPort -eq 8080 | Measure-Object).Count))
    Write-Output ("runtime.cloudflared.processes={0}" -f (
        ($cloudflared | Measure-Object).Count))
    if ($listeners -or $cloudflared) {
        throw "Active application runtime blocks MySQL proof."
    }
}

$trackedEnvironment = @(
    "SPRING_DATASOURCE_URL",
    "SPRING_DATASOURCE_USERNAME",
    "SPRING_DATASOURCE_PASSWORD",
    "SPRING_JPA_HIBERNATE_DDL_AUTO",
    "SPRING_JPA_SHOW_SQL",
    "SPRING_SQL_INIT_MODE",
    "APP_BOOTSTRAP_TEST_USERS_ENABLED",
    "PAYMENT_BILLING_KEY_ACTIVE_KEY_ID",
    "PAYMENT_BILLING_KEY_0_ID",
    "PAYMENT_BILLING_KEY_0_SECRET",
    "ATSTUDIO_MYSQL_PROOF_ENABLED",
    "ATSTUDIO_MYSQL_PROOF_TARGET"
)
$originalEnvironment = @{}
foreach ($name in $trackedEnvironment) {
    $originalEnvironment[$name] = [Environment]::GetEnvironmentVariable($name, "Process")
}

$summary = [ordered]@{
    wi = "WI-20260717-ATS-004"
    mode = $Mode
    runtimePreflight = "NOT_RUN"
    datasourcePreflight = "NOT_RUN"
    firstApply = "NOT_RUN"
    secondApply = "NOT_RUN"
    manifest = "NOT_RUN"
    hibernateValidate = "NOT_RUN"
    mysqlRaces = if ($Mode -eq "Disposable") { "NOT_RUN" } else { "NOT_REQUIRED" }
    cleanup = if ($Mode -eq "Disposable") { "NOT_RUN" } else { "NOT_REQUIRED" }
    result = "FAIL"
}

try {
    Assert-RuntimeStopped
    $summary.runtimePreflight = "PASS"
    $datasource = Get-SourceDatasource
    $summary.datasourcePreflight = "PASS"
    Write-Output "database.hostClass=loopback"
    Write-Output "database.name=atstudio"
    Write-Output ("database.credentialSource={0}" -f $datasource.Source)

    $manualSqlCount = (Get-ChildItem -LiteralPath $manualSqlDir -File -Filter "*.sql" |
        Measure-Object).Count
    Write-Output ("manualSql.files={0}" -f $manualSqlCount)
    if ($Mode -eq "Disposable" -and $manualSqlCount -ne 9) {
        throw "Disposable proof requires all nine manual SQL files to remain present."
    }
    if ($Mode -eq "RecreateLocal" -and $manualSqlCount -ne 0) {
        throw "Local recreation requires the manual SQL retirement gate to be complete."
    }

    $connector = Get-ChildItem -Recurse -File `
            -Path "$env:USERPROFILE\.gradle\caches\modules-2\files-2.1\com.mysql\mysql-connector-j" `
            -Filter "*.jar" |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1
    if ($null -eq $connector -or -not (Test-Path -LiteralPath $managerSource)) {
        throw "Proof manager source or MySQL Connector/J is unavailable."
    }

    $env:SPRING_DATASOURCE_URL = $datasource.Url
    $env:SPRING_DATASOURCE_USERNAME = $datasource.Username
    $env:SPRING_DATASOURCE_PASSWORD = $datasource.Password
    Invoke-BoundedCommand `
        -Label "manager-compile" `
        -FilePath "javac" `
        -ArgumentList @("-cp", $connector.FullName, "-d", $classesDir, $managerSource) `
        -Datasource $datasource | Out-Null

    $preflight = Invoke-Manager `
        "database-preflight" "preflight" "atstudio" $datasource $connector.FullName
    if ($preflight -notmatch "database.activeSessions=0" -or
        $preflight -notmatch "disposable.databases=0") {
        throw "Local database session preflight did not pass."
    }

    if ($Mode -eq "Disposable") {
        Invoke-Manager `
            "disposable-create" "create-disposable" $disposableName $datasource $connector.FullName |
            Out-Null
        $disposableCreated = $true
        $first = Invoke-Manager `
            "schema-first-apply" "apply-first" $disposableName $datasource $connector.FullName
        if ($first -notmatch "schema.firstApply=PASS") {
            throw "First schema apply did not pass."
        }
        $summary.firstApply = "PASS"

        $second = Invoke-Manager `
            "schema-second-apply" "apply-second-expect-fail" $disposableName $datasource $connector.FullName
        if ($second -notmatch "schema.secondApply=EXPECTED_FAILURE") {
            throw "Second schema apply did not fail as expected."
        }
        $summary.secondApply = "EXPECTED_FAILURE"

        $manifest = Invoke-Manager `
            "manifest-before-runtime" "manifest" $disposableName $datasource $connector.FullName
        if ($manifest -notmatch "manifest.tables=39" -or
            $manifest -notmatch "manifest.plans=6" -or
            $manifest -notmatch "manifest.providerTossOnly=9") {
            throw "Disposable manifest proof did not pass."
        }
        $summary.manifest = "PASS"
        $targetDatabase = $disposableName
        $env:ATSTUDIO_MYSQL_PROOF_TARGET = "disposable"
    } else {
        Invoke-Manager `
            "local-recreate" "recreate-local" "atstudio" $datasource $connector.FullName $true |
            Out-Null
        $summary.firstApply = "PASS"
        $summary.secondApply = "PROVEN_ON_DISPOSABLE"
        $summary.manifest = "PASS"
        $targetDatabase = "atstudio"
        $env:ATSTUDIO_MYSQL_PROOF_TARGET = "local-atstudio"
    }

    $env:SPRING_DATASOURCE_URL = Get-TargetUrl $datasource $targetDatabase
    $env:SPRING_JPA_HIBERNATE_DDL_AUTO = "validate"
    $env:SPRING_JPA_SHOW_SQL = "false"
    $env:SPRING_SQL_INIT_MODE = "never"
    $env:APP_BOOTSTRAP_TEST_USERS_ENABLED = "false"
    $env:PAYMENT_BILLING_KEY_ACTIVE_KEY_ID = "wi004-proof-v2"
    $env:PAYMENT_BILLING_KEY_0_ID = "wi004-proof-v2"
    $env:PAYMENT_BILLING_KEY_0_SECRET = "wi004-proof-only-key-material-not-runtime"
    $env:ATSTUDIO_MYSQL_PROOF_ENABLED = "true"

    Invoke-BoundedCommand `
        -Label "hibernate-validate" `
        -FilePath (Join-Path $workspacePath "gradlew.bat") `
        -ArgumentList @(
            "test", "--no-daemon", "--rerun-tasks", "--tests",
            "com.atstudio.atstudio.service.PaymentMysqlSchemaValidationTest") `
        -Datasource $datasource | Out-Null
    $summary.hibernateValidate = "PASS"

    if ($Mode -eq "Disposable") {
        Invoke-BoundedCommand `
            -Label "mysql-races" `
            -FilePath (Join-Path $workspacePath "gradlew.bat") `
            -ArgumentList @(
                "test", "--no-daemon", "--rerun-tasks", "--tests",
                "com.atstudio.atstudio.service.PaymentMysqlConcurrencyIntegrationTest") `
            -Datasource $datasource | Out-Null
        $summary.mysqlRaces = "PASS"
    } else {
        $env:SPRING_DATASOURCE_URL = $datasource.Url
        $localManifest = Invoke-Manager `
            "local-manifest-final" "manifest" "atstudio" $datasource $connector.FullName
        if ($localManifest -notmatch "manifest.tables=39") {
            throw "Final local manifest proof did not pass."
        }
    }
    $summary.result = "PASS"
} finally {
    try {
        if ($Mode -eq "Disposable" -and $disposableCreated) {
            $env:SPRING_DATASOURCE_URL = $datasource.Url
            $env:SPRING_DATASOURCE_USERNAME = $datasource.Username
            $env:SPRING_DATASOURCE_PASSWORD = $datasource.Password
            $drop = Invoke-Manager `
                "disposable-drop" "drop-disposable" $disposableName $datasource $connector.FullName
            if ($drop -match "disposable.remaining=0") {
                $summary.cleanup = "PASS"
            } else {
                $summary.cleanup = "FAIL"
                $summary.result = "FAIL"
            }
        }
    } catch {
        $summary.cleanup = "FAIL"
        $summary.result = "FAIL"
        throw
    } finally {
        foreach ($name in $trackedEnvironment) {
            $originalValue = $originalEnvironment[$name]
            if ($null -eq $originalValue) {
                Remove-Item -LiteralPath ("Env:\" + $name) -ErrorAction SilentlyContinue
            } else {
                [Environment]::SetEnvironmentVariable($name, $originalValue, "Process")
            }
        }
        $resolvedTemp = [IO.Path]::GetFullPath($tempRoot)
        if ($resolvedTemp.StartsWith($tempBase, [StringComparison]::OrdinalIgnoreCase) -and
            (Split-Path -Leaf $resolvedTemp).StartsWith("atstudio-wi004-")) {
            Remove-Item -LiteralPath $resolvedTemp -Recurse -Force -ErrorAction SilentlyContinue
        }
        $summary.GetEnumerator() | ForEach-Object { "$($_.Key)=$($_.Value)" } |
            Set-Content -LiteralPath (Join-Path $runArtifactDir ("$Mode-summary.log"))
    }
}

Write-Output ("proof.mode={0}" -f $Mode)
Write-Output ("proof.evidenceDirectory={0}" -f $runName)
Write-Output ("proof.result={0}" -f $summary.result)
Write-Output ("proof.hibernateValidate={0}" -f $summary.hibernateValidate)
Write-Output ("proof.mysqlRaces={0}" -f $summary.mysqlRaces)
Write-Output ("proof.cleanup={0}" -f $summary.cleanup)
if ($summary.result -ne "PASS") {
    throw "WI-004 MySQL proof failed; inspect redacted artifacts only."
}
