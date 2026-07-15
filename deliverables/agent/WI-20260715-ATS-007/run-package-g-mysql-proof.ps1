param(
    [string]$Workspace = (Resolve-Path ".").Path,
    [string]$DatabaseName = "",
    [ValidateRange(1, 300)]
    [int]$CommandTimeoutSeconds = 300
)

$ErrorActionPreference = "Stop"
$disposablePattern = '^ats_wi007_\d{8}_[a-z0-9]{8}$'
$protectedDatabaseNames = @(
    "atstudio",
    "mysql",
    "information_schema",
    "performance_schema",
    "sys",
    "preview",
    "stage",
    "staging",
    "prod",
    "production"
)

$workspacePath = (Resolve-Path -LiteralPath $Workspace).Path
$artifactDir = Join-Path $workspacePath "deliverables\agent\WI-20260715-ATS-007"
$managerSource = Join-Path $artifactDir "DisposableMysqlDatabaseManager.java"
$applicationLocal = Join-Path $workspacePath "application-local.yml"
$tempBase = [IO.Path]::GetFullPath([IO.Path]::GetTempPath())
$tempRoot = Join-Path $tempBase ("atstudio-wi007-" + [guid]::NewGuid().ToString("N"))
$classesDir = Join-Path $tempRoot "classes"

New-Item -ItemType Directory -Force -Path $artifactDir | Out-Null
New-Item -ItemType Directory -Force -Path $classesDir | Out-Null

if ([string]::IsNullOrWhiteSpace($DatabaseName)) {
    $alphabet = (48..57) + (97..122)
    $suffix = -join ($alphabet | Get-Random -Count 8 | ForEach-Object { [char]$_ })
    $DatabaseName = "ats_wi007_{0}_{1}" -f (Get-Date -Format "yyyyMMdd"), $suffix
}

if ($DatabaseName -notmatch $disposablePattern) {
    throw "Database name failed the WI007 disposable pattern."
}
if ($protectedDatabaseNames -contains $DatabaseName.ToLowerInvariant()) {
    throw "Refusing a protected database target."
}

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
    if (-not [string]::IsNullOrWhiteSpace($url) -and
        -not [string]::IsNullOrWhiteSpace($username) -and
        -not [string]::IsNullOrWhiteSpace($password)) {
        return [pscustomobject]@{
            Url = $url
            Username = $username
            Password = $password
            Source = "process-env"
        }
    }

    if (-not (Test-Path -LiteralPath $applicationLocal)) {
        throw "Datasource credential source is unavailable."
    }
    $configText = Get-Content -Raw -LiteralPath $applicationLocal
    $url = Read-Scalar $configText "url"
    $username = Read-Scalar $configText "username"
    $password = Read-Scalar $configText "password"
    if ([string]::IsNullOrWhiteSpace($url) -or
        [string]::IsNullOrWhiteSpace($username) -or
        [string]::IsNullOrWhiteSpace($password) -or
        $url.Contains("REPLACE_WITH") -or
        $username.Contains("REPLACE_WITH") -or
        $password.Contains("REPLACE_WITH")) {
        throw "Datasource credential source is missing or contains placeholders."
    }
    return [pscustomobject]@{
        Url = $url
        Username = $username
        Password = $password
        Source = "application-local-yml"
    }
}

function Get-TargetJdbcUrl([string]$SourceUrl, [string]$TargetDatabase) {
    $match = [regex]::Match($SourceUrl, "^(jdbc:mysql://([^/]+)/)([^?]+)(\?.*)?$")
    if (-not $match.Success) {
        throw "Only a jdbc:mysql credential source with a database path is supported."
    }
    $hostPort = $match.Groups[2].Value.ToLowerInvariant()
    if ($hostPort -notmatch '^(localhost|127\.0\.0\.1|\[::1\])(?::\d+)?$') {
        throw "Package G accepts only a loopback MySQL credential source."
    }
    $sourceDatabase = $match.Groups[3].Value
    if ($TargetDatabase.Equals($sourceDatabase, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to target the credential-source database."
    }

    $query = $match.Groups[4].Value
    if (-not [string]::IsNullOrWhiteSpace($query)) {
        $parts = $query.TrimStart("?").Split("&") | Where-Object {
            -not $_.ToLowerInvariant().StartsWith("createdatabaseifnotexist=")
        }
        $query = if ($parts.Count -gt 0) { "?" + ($parts -join "&") } else { "" }
    }
    return $match.Groups[1].Value + $TargetDatabase + $query
}

function Set-DatasourceEnvironment(
    [string]$Url,
    [string]$Username,
    [string]$Password
) {
    $env:SPRING_DATASOURCE_URL = $Url
    $env:SPRING_DATASOURCE_USERNAME = $Username
    $env:SPRING_DATASOURCE_PASSWORD = $Password
}

function Get-RedactedText([string]$Text, $Datasource, [string]$TargetDatabase) {
    $redacted = [regex]::Replace(
        $Text,
        'jdbc:mysql://[^\s\]\)''"]+',
        '[REDACTED_JDBC_URL]')
    $redacted = [regex]::Replace(
        $redacted,
        'ats_wi007_\d{8}_[a-z0-9]{8}',
        '[WI007_DISPOSABLE]')
    foreach ($value in @($Datasource.Url, $Datasource.Username, $Datasource.Password, $TargetDatabase)) {
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
    [string]$LogPath,
    $Datasource
) {
    $stdoutPath = Join-Path $tempRoot ($Label + "-stdout.log")
    $stderrPath = Join-Path $tempRoot ($Label + "-stderr.log")
    $exitCodePath = Join-Path $tempRoot ($Label + "-exit-code.txt")
    $commandPath = Join-Path $tempRoot ($Label + "-command.cmd")
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
            -NoNewWindow `
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
        Set-Content -LiteralPath $LogPath -Value (
            Get-RedactedText $raw $Datasource $DatabaseName) -NoNewline
        if ($timedOut) {
            throw "$Label exceeded the strict five minute command limit."
        }
        if (-not (Test-Path -LiteralPath $exitCodePath)) {
            throw "$Label did not report a process exit code."
        }
        $exitCode = [int](Get-Content -Raw -LiteralPath $exitCodePath).Trim()
        if ($exitCode -ne 0) {
            throw "$Label failed with exit code $exitCode."
        }
    } finally {
        Remove-Item -LiteralPath $stdoutPath -Force -ErrorAction SilentlyContinue
        Remove-Item -LiteralPath $stderrPath -Force -ErrorAction SilentlyContinue
        Remove-Item -LiteralPath $exitCodePath -Force -ErrorAction SilentlyContinue
        Remove-Item -LiteralPath $commandPath -Force -ErrorAction SilentlyContinue
    }
}

function Invoke-Manager(
    [string]$Mode,
    [string]$LogPath,
    $Datasource,
    [string]$ConnectorJar
) {
    Invoke-BoundedCommand `
        -Label ("manager-" + $Mode) `
        -FilePath "java" `
        -ArgumentList @(
            "-cp",
            "$classesDir;$ConnectorJar",
            "DisposableMysqlDatabaseManager",
            "--workspace",
            $workspacePath,
            "--database",
            $DatabaseName,
            "--mode",
            $Mode,
            "--log",
            $LogPath
        ) `
        -LogPath (Join-Path $artifactDir ("manager-" + $Mode + "-process.log")) `
        -Datasource $Datasource
}

$sourceDatasource = Get-SourceDatasource
$targetJdbcUrl = Get-TargetJdbcUrl $sourceDatasource.Url $DatabaseName
$connectorJar = Get-ChildItem -Recurse -File `
        -Path "$env:USERPROFILE\.gradle\caches\modules-2\files-2.1\com.mysql\mysql-connector-j" `
        -Filter "*.jar" |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1
if ($null -eq $connectorJar) {
    throw "MySQL Connector/J is unavailable in the Gradle cache."
}
if (-not (Test-Path -LiteralPath $managerSource)) {
    throw "Disposable database manager source is missing."
}

$trackedEnvironment = @(
    "SPRING_DATASOURCE_URL",
    "SPRING_DATASOURCE_USERNAME",
    "SPRING_DATASOURCE_PASSWORD",
    "SPRING_JPA_HIBERNATE_DDL_AUTO",
    "SPRING_JPA_SHOW_SQL",
    "SPRING_SQL_INIT_MODE",
    "APP_BOOTSTRAP_TEST_USERS_ENABLED",
    "APP_PAYMENT_PROVIDER",
    "LOG_LEVEL_HIBERNATE_SQL",
    "LOG_LEVEL_HIBERNATE_BINDER",
    "ATSTUDIO_MYSQL_PROOF_ENABLED"
)
$originalEnvironment = @{}
foreach ($name in $trackedEnvironment) {
    $originalEnvironment[$name] = [Environment]::GetEnvironmentVariable($name, "Process")
}

$summary = [ordered]@{
    wi = "WI-20260715-ATS-007"
    databaseAlias = "WI007_DISPOSABLE"
    databaseName = "not-printed"
    credentialSource = $sourceDatasource.Source
    commandTimeoutSeconds = $CommandTimeoutSeconds
    schemaCreate = "NOT_RUN"
    hibernateValidate = "NOT_RUN"
    mysqlRaces = "NOT_RUN"
    diagnostics = "NOT_REQUIRED"
    drop = "NOT_RUN"
    cleanupDatabaseExists = "UNKNOWN"
    result = "FAIL"
}
$primaryFailure = $null
$cleanupFailure = $null

try {
    Set-DatasourceEnvironment `
        $sourceDatasource.Url `
        $sourceDatasource.Username `
        $sourceDatasource.Password
    Invoke-BoundedCommand `
        -Label "manager-compile" `
        -FilePath "javac" `
        -ArgumentList @(
            "-cp",
            $connectorJar.FullName,
            "-d",
            $classesDir,
            $managerSource
        ) `
        -LogPath (Join-Path $artifactDir "manager-compile.log") `
        -Datasource $sourceDatasource
    Invoke-Manager `
        -Mode "create" `
        -LogPath (Join-Path $artifactDir "database-create.log") `
        -Datasource $sourceDatasource `
        -ConnectorJar $connectorJar.FullName
    $summary.schemaCreate = "PASS"

    Set-DatasourceEnvironment `
        $targetJdbcUrl `
        $sourceDatasource.Username `
        $sourceDatasource.Password
    $env:SPRING_JPA_HIBERNATE_DDL_AUTO = "validate"
    $env:SPRING_JPA_SHOW_SQL = "false"
    $env:SPRING_SQL_INIT_MODE = "never"
    $env:APP_BOOTSTRAP_TEST_USERS_ENABLED = "false"
    $env:APP_PAYMENT_PROVIDER = "MOCK"
    $env:LOG_LEVEL_HIBERNATE_SQL = "warn"
    $env:LOG_LEVEL_HIBERNATE_BINDER = "warn"
    $env:ATSTUDIO_MYSQL_PROOF_ENABLED = "true"

    Invoke-BoundedCommand `
        -Label "hibernate-validate" `
        -FilePath (Join-Path $workspacePath "gradlew.bat") `
        -ArgumentList @(
            "test",
            "--no-daemon",
            "--rerun-tasks",
            "--tests",
            "com.atstudio.atstudio.service.PaymentMysqlSchemaValidationTest"
        ) `
        -LogPath (Join-Path $artifactDir "hibernate-validate.log") `
        -Datasource $sourceDatasource
    $summary.hibernateValidate = "PASS"

    Invoke-BoundedCommand `
        -Label "mysql-races" `
        -FilePath (Join-Path $workspacePath "gradlew.bat") `
        -ArgumentList @(
            "test",
            "--no-daemon",
            "--rerun-tasks",
            "--tests",
            "com.atstudio.atstudio.service.PaymentMysqlConcurrencyIntegrationTest"
        ) `
        -LogPath (Join-Path $artifactDir "mysql-races.log") `
        -Datasource $sourceDatasource
    $summary.mysqlRaces = "PASS"
    $summary.result = "PASS"
} catch {
    $primaryFailure = $_
    if ($summary.schemaCreate -eq "NOT_RUN") {
        $summary.schemaCreate = "FAIL"
    } elseif ($summary.hibernateValidate -eq "NOT_RUN") {
        $summary.hibernateValidate = "FAIL"
    } elseif ($summary.mysqlRaces -eq "NOT_RUN") {
        $summary.mysqlRaces = "FAIL"
    }
} finally {
    try {
        Set-DatasourceEnvironment `
            $sourceDatasource.Url `
            $sourceDatasource.Username `
            $sourceDatasource.Password
        if ($null -ne $primaryFailure -and $summary.schemaCreate -eq "PASS") {
            try {
                Invoke-Manager `
                    -Mode "diagnostics" `
                    -LogPath (Join-Path $artifactDir "failure-diagnostics.log") `
                    -Datasource $sourceDatasource `
                    -ConnectorJar $connectorJar.FullName
                $summary.diagnostics = "CAPTURED"
            } catch {
                $summary.diagnostics = "CAPTURE_FAILED"
            }
        }

        Invoke-Manager `
            -Mode "drop" `
            -LogPath (Join-Path $artifactDir "database-drop.log") `
            -Datasource $sourceDatasource `
            -ConnectorJar $connectorJar.FullName
        $summary.drop = "PASS"
        Invoke-Manager `
            -Mode "verify-absent" `
            -LogPath (Join-Path $artifactDir "database-absent.log") `
            -Datasource $sourceDatasource `
            -ConnectorJar $connectorJar.FullName
        $summary.cleanupDatabaseExists = "0"
    } catch {
        $cleanupFailure = $_
        $summary.drop = "FAIL"
        $summary.cleanupDatabaseExists = "UNKNOWN"
        $summary.result = "FAIL"
    } finally {
        foreach ($name in $trackedEnvironment) {
            $originalValue = $originalEnvironment[$name]
            if ($null -eq $originalValue) {
                Remove-Item -LiteralPath ("Env:\" + $name) -ErrorAction SilentlyContinue
            } else {
                [Environment]::SetEnvironmentVariable($name, $originalValue, "Process")
            }
        }
        $resolvedTempRoot = [IO.Path]::GetFullPath($tempRoot)
        if ($resolvedTempRoot.StartsWith($tempBase, [StringComparison]::OrdinalIgnoreCase) -and
            (Split-Path -Leaf $resolvedTempRoot).StartsWith("atstudio-wi007-")) {
            Remove-Item -LiteralPath $resolvedTempRoot -Recurse -Force -ErrorAction SilentlyContinue
        }
    }
}

$summaryLines = $summary.GetEnumerator() | ForEach-Object { "$($_.Key)=$($_.Value)" }
Set-Content -LiteralPath (Join-Path $artifactDir "run-summary.log") -Value $summaryLines

Write-Output "packageG.result=$($summary.result)"
Write-Output "packageG.hibernateValidate=$($summary.hibernateValidate)"
Write-Output "packageG.mysqlRaces=$($summary.mysqlRaces)"
Write-Output "packageG.cleanupDatabaseExists=$($summary.cleanupDatabaseExists)"
Write-Output "packageG.artifacts=$artifactDir"

if ($null -ne $cleanupFailure) {
    throw "Package G cleanup failed; inspect the redacted cleanup artifacts."
}
if ($null -ne $primaryFailure) {
    throw "Package G proof failed; inspect the redacted artifacts."
}
