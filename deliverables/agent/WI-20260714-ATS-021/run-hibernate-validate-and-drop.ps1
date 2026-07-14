param(
    [Parameter(Mandatory = $true)]
    [string]$DatabaseName,
    [string]$Workspace = (Resolve-Path ".").Path
)

$ErrorActionPreference = "Stop"

$workspacePath = (Resolve-Path -LiteralPath $Workspace).Path
$artifactDir = Join-Path $workspacePath "deliverables\agent\WI-20260714-ATS-021"
$logPath = Join-Path $artifactDir "hibernate-validate.log"
$dropLogPath = Join-Path $artifactDir "drop-after-hibernate-validate.log"
$applicationLocal = Join-Path $workspacePath "application-local.yml"

if ($DatabaseName -notmatch '^ats_wi021_\d{8}_[a-z0-9]{8}$') {
    throw "Refusing unexpected disposable database name: $DatabaseName"
}

function Read-Scalar([string]$Text, [string]$Key) {
    $match = [regex]::Match($Text, "(?m)^\s*$([regex]::Escape($Key))\s*:\s*(.+?)\s*$")
    if (-not $match.Success) {
        return ""
    }
    return $match.Groups[1].Value.Trim().Trim("'").Trim('"')
}

if (-not (Test-Path -LiteralPath $applicationLocal)) {
    throw "application-local.yml is required for this local disposable rehearsal."
}

$configText = Get-Content -Raw -LiteralPath $applicationLocal
$sourceUrl = Read-Scalar $configText "url"
$sourceUsername = Read-Scalar $configText "username"
$sourcePassword = Read-Scalar $configText "password"

if ([string]::IsNullOrWhiteSpace($sourceUrl) -or
    [string]::IsNullOrWhiteSpace($sourceUsername) -or
    [string]::IsNullOrWhiteSpace($sourcePassword) -or
    $sourceUrl.Contains("REPLACE_WITH") -or
    $sourceUsername.Contains("REPLACE_WITH") -or
    $sourcePassword.Contains("REPLACE_WITH")) {
    throw "Datasource values are missing or placeholders."
}

$urlMatch = [regex]::Match($sourceUrl, "^(jdbc:mysql://[^/]+/)([^?]+)(\?.*)?$")
if (-not $urlMatch.Success) {
    throw "Unsupported JDBC URL shape."
}

$query = $urlMatch.Groups[3].Value
if (-not [string]::IsNullOrWhiteSpace($query)) {
    $parts = $query.TrimStart("?").Split("&") | Where-Object {
        -not $_.ToLowerInvariant().StartsWith("createdatabaseifnotexist=")
    }
    $query = if ($parts.Count -gt 0) { "?" + ($parts -join "&") } else { "" }
}

$env:SPRING_DATASOURCE_URL = $urlMatch.Groups[1].Value + $DatabaseName + $query
$env:SPRING_DATASOURCE_USERNAME = $sourceUsername
$env:SPRING_DATASOURCE_PASSWORD = $sourcePassword
$env:SPRING_JPA_HIBERNATE_DDL_AUTO = "validate"
$env:SPRING_JPA_SHOW_SQL = "false"
$env:SPRING_SQL_INIT_MODE = "never"
$env:APP_BOOTSTRAP_TEST_USERS_ENABLED = "false"
$env:APP_PAYMENT_PROVIDER = "MOCK"
$env:LOG_LEVEL_HIBERNATE_SQL = "warn"
$env:LOG_LEVEL_HIBERNATE_BINDER = "warn"

Write-Output "hibernate.connection=process-env; secret-values-not-printed"
Write-Output "hibernate.database=$DatabaseName"
Write-Output "hibernate.ddlAuto=validate"

try {
    Push-Location $workspacePath
    & .\gradlew.bat bootRun --args="--spring.main.web-application-type=none --spring.sql.init.mode=never" *> $logPath
    $exitCode = $LASTEXITCODE
    Pop-Location

    $logText = Get-Content -Raw -LiteralPath $logPath
    $sanitizedLogText = [regex]::Replace(
        $logText,
        "Database JDBC URL \[jdbc:mysql://[^\]]+\]",
        "Database JDBC URL [REDACTED_JDBC_URL; database=$DatabaseName]"
    )
    Set-Content -LiteralPath $logPath -Value $sanitizedLogText -NoNewline

    Write-Output "hibernate.log=$logPath"
    Write-Output "hibernate.exitCode=$exitCode"
    $validationFailure = Select-String -LiteralPath $logPath -Pattern "SchemaManagementException|Application run failed|Failed to initialize JPA EntityManagerFactory" -Quiet
    Write-Output "hibernate.schemaValidationFailureDetected=$validationFailure"
    if ($exitCode -ne 0 -or $validationFailure) {
        throw "Hibernate validate failed; inspect $logPath"
    }
} finally {
    if ((Get-Location).Path -ne $workspacePath) {
        Set-Location $workspacePath
    }
    Remove-Item Env:\SPRING_DATASOURCE_URL -ErrorAction SilentlyContinue
    powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $artifactDir "run-disposable-mysql-rehearsal.ps1") `
        -DatabaseName $DatabaseName `
        -LogPath $dropLogPath `
        -DropOnly
    Write-Output "cleanup.log=$dropLogPath"
}
