[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("Preflight", "Observe", "Create", "Validate", "Drop", "Inventory", "HibernateValidate")]
    [string] $Action,

    [Parameter(Mandatory = $true)]
    [string] $DatabaseName,

    [string] $HostName = "127.0.0.1",

    [ValidateRange(1, 65535)]
    [int] $Port = 3306,

    [string] $BackendEnvironmentPath,

    [string] $ConnectorJarPath
)

$ErrorActionPreference = "Stop"
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$javaSource = Join-Path $PSScriptRoot "DisposableMysqlBootstrap.java"

function Get-JavaCommand {
    $java = Get-Command java.exe -ErrorAction SilentlyContinue
    if (-not $java) {
        $java = Get-Command java -ErrorAction SilentlyContinue
    }
    if (-not $java) {
        throw "Java 17 or later is required."
    }
    return $java.Source
}

function Get-MysqlConnectorJar {
    param([string] $ExplicitPath)

    if (-not [string]::IsNullOrWhiteSpace($ExplicitPath)) {
        $resolved = [System.IO.Path]::GetFullPath($ExplicitPath)
        if (-not (Test-Path -LiteralPath $resolved -PathType Leaf)) {
            throw "The supplied MySQL Connector/J path is not a regular file."
        }
        return $resolved
    }

    $cacheRoot = Join-Path `
        $HOME `
        ".gradle\caches\modules-2\files-2.1\com.mysql\mysql-connector-j"
    if (-not (Test-Path -LiteralPath $cacheRoot -PathType Container)) {
        throw "MySQL Connector/J was not found. Run a backend Gradle build or supply ConnectorJarPath."
    }

    $candidates = @(
        Get-ChildItem `
            -LiteralPath $cacheRoot `
            -Recurse `
            -File `
            -Filter "mysql-connector-j-*.jar" |
            Sort-Object FullName -Descending
    )
    if ($candidates.Count -eq 0) {
        throw "MySQL Connector/J was not found. Run a backend Gradle build or supply ConnectorJarPath."
    }
    return $candidates[0].FullName
}

function Assert-ExternalRegularFile {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Path
    )

    try {
        $resolved = [System.IO.Path]::GetFullPath($Path)
        $file = Get-Item -LiteralPath $resolved -Force -ErrorAction Stop
    } catch {
        throw "Backend environment bundle must be a regular file outside the repository."
    }

    $normalizedRepo = $repoRoot.TrimEnd('\', '/')
    $normalizedPath = $resolved.TrimEnd('\', '/')
    $comparison = [System.StringComparison]::OrdinalIgnoreCase
    if ($normalizedPath.Equals($normalizedRepo, $comparison) -or
        $normalizedPath.StartsWith("$normalizedRepo\", $comparison) -or
        -not ($file -is [System.IO.FileInfo]) -or
        (($file.Attributes -band [System.IO.FileAttributes]::ReparsePoint) -ne 0)) {
        throw "Backend environment bundle must be a regular file outside the repository."
    }
    return $file.FullName
}

function Read-DatabaseCredentials {
    param([string] $BundlePath)

    if ([string]::IsNullOrWhiteSpace($BundlePath)) {
        if ([string]::IsNullOrWhiteSpace($env:SPRING_DATASOURCE_USERNAME) -or
            [string]::IsNullOrWhiteSpace($env:SPRING_DATASOURCE_PASSWORD)) {
            throw "Datasource credentials are unavailable."
        }
        return [pscustomobject]@{
            Username = $env:SPRING_DATASOURCE_USERNAME
            Password = $env:SPRING_DATASOURCE_PASSWORD
        }
    }

    $resolved = Assert-ExternalRegularFile -Path $BundlePath
    try {
        $bundle = [System.IO.File]::ReadAllText($resolved) |
            ConvertFrom-Json -ErrorAction Stop
    } catch {
        throw "Backend environment bundle must contain valid JSON."
    }
    if (-not ($bundle -is [pscustomobject])) {
        throw "Backend environment bundle must be a flat JSON object."
    }

    $required = @(
        "SPRING_DATASOURCE_URL",
        "SPRING_DATASOURCE_USERNAME",
        "SPRING_DATASOURCE_PASSWORD"
    )
    foreach ($name in $required) {
        $property = $bundle.PSObject.Properties[$name]
        if (-not $property -or
            -not ($property.Value -is [string]) -or
            [string]::IsNullOrWhiteSpace([string] $property.Value)) {
            throw "Backend environment bundle is missing required datasource values."
        }
    }

    $jdbcPattern = "^jdbc:mysql://(?<host>\[[^\]]+\]|[^/:?#]+)(?::(?<port>\d+))?/[^?]+(?:\?.*)?$"
    $jdbcUrl = [string] $bundle.SPRING_DATASOURCE_URL
    if ($jdbcUrl -notmatch $jdbcPattern) {
        throw "Backend environment bundle must contain a single-host MySQL JDBC URL."
    }
    $bundleHost = $Matches.host.ToLowerInvariant()
    $bundlePort = if ([string]::IsNullOrWhiteSpace($Matches.port)) {
        3306
    } else {
        [int] $Matches.port
    }
    if ($bundleHost -notin @("localhost", "127.0.0.1", "::1", "[::1]") -or
        $bundlePort -ne $Port) {
        throw "Backend environment bundle must resolve to the selected loopback MySQL port."
    }

    return [pscustomobject]@{
        Username = [string] $bundle.SPRING_DATASOURCE_USERNAME
        Password = [string] $bundle.SPRING_DATASOURCE_PASSWORD
    }
}

function Convert-ToBootstrapAction {
    param([Parameter(Mandatory = $true)][string] $ActionName)

    if ($ActionName -eq "HibernateValidate") {
        return "hibernate-validate"
    }
    return $ActionName.ToLowerInvariant()
}

function Invoke-BootstrapJava {
    param(
        [Parameter(Mandatory = $true)]
        [string] $JavaPath,
        [Parameter(Mandatory = $true)]
        [string] $RequestedAction,
        [string] $PreflightForAction,
        [string] $ConnectorPath
    )

    $arguments = New-Object System.Collections.ArrayList
    if (-not [string]::IsNullOrWhiteSpace($ConnectorPath)) {
        [void] $arguments.Add("--class-path")
        [void] $arguments.Add($ConnectorPath)
    }
    [void] $arguments.Add($javaSource)
    [void] $arguments.Add("--action")
    [void] $arguments.Add((Convert-ToBootstrapAction -ActionName $RequestedAction))
    if (-not [string]::IsNullOrWhiteSpace($PreflightForAction)) {
        [void] $arguments.Add("--requested-action")
        [void] $arguments.Add((Convert-ToBootstrapAction -ActionName $PreflightForAction))
    }
    [void] $arguments.Add("--workspace")
    [void] $arguments.Add($repoRoot)
    [void] $arguments.Add("--host")
    [void] $arguments.Add($HostName)
    [void] $arguments.Add("--port")
    [void] $arguments.Add($Port.ToString())
    [void] $arguments.Add("--database")
    [void] $arguments.Add($DatabaseName)

    & $JavaPath @arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Disposable MySQL bootstrap did not complete."
    }
}

function Invoke-HibernateValidation {
    $gradle = Join-Path $repoRoot "gradlew.bat"
    if (-not (Test-Path -LiteralPath $gradle -PathType Leaf)) {
        throw "Targeted Hibernate validation runner is unavailable."
    }

    $nativeOutput = @(
        & $gradle --no-daemon test `
            --tests "com.atstudio.atstudio.service.PaymentMysqlSchemaValidationTest" 2>&1
    )
    $exitCode = $LASTEXITCODE
    $nativeOutput = $null
    if ($exitCode -ne 0) {
        throw "Targeted Hibernate validation did not complete."
    }
}

$javaPath = Get-JavaCommand

# The preflight is always first and needs neither a connector nor credentials.
# Unsafe names and hosts therefore fail before secret loading or any connection.
Invoke-BootstrapJava `
    -JavaPath $javaPath `
    -RequestedAction "Preflight" `
    -PreflightForAction $Action

if ($Action -eq "Preflight") {
    return
}

if ($Action -eq "HibernateValidate" -and
    [string]::IsNullOrWhiteSpace($BackendEnvironmentPath)) {
    throw "Hibernate validation requires the approved repo-external environment bundle."
}

$credentials = Read-DatabaseCredentials -BundlePath $BackendEnvironmentPath
$connector = Get-MysqlConnectorJar -ExplicitPath $ConnectorJarPath
$savedUsername = [System.Environment]::GetEnvironmentVariable(
    "SPRING_DATASOURCE_USERNAME",
    "Process")
$savedPassword = [System.Environment]::GetEnvironmentVariable(
    "SPRING_DATASOURCE_PASSWORD",
    "Process")
$savedDatasourceUrl = [System.Environment]::GetEnvironmentVariable(
    "SPRING_DATASOURCE_URL",
    "Process")
$savedHibernateProofEnabled = [System.Environment]::GetEnvironmentVariable(
    "ATSTUDIO_MYSQL_PROOF_ENABLED",
    "Process")
$savedHibernateProofDatabase = [System.Environment]::GetEnvironmentVariable(
    "ATSTUDIO_MYSQL_PROOF_DATABASE",
    "Process")

try {
    [System.Environment]::SetEnvironmentVariable(
        "SPRING_DATASOURCE_USERNAME",
        $credentials.Username,
        "Process")
    [System.Environment]::SetEnvironmentVariable(
        "SPRING_DATASOURCE_PASSWORD",
        $credentials.Password,
        "Process")
    if ($Action -eq "HibernateValidate") {
        [System.Environment]::SetEnvironmentVariable(
            "SPRING_DATASOURCE_URL",
            "jdbc:mysql://$HostName`:$Port/$DatabaseName",
            "Process")
        [System.Environment]::SetEnvironmentVariable(
            "ATSTUDIO_MYSQL_PROOF_ENABLED",
            "true",
            "Process")
        [System.Environment]::SetEnvironmentVariable(
            "ATSTUDIO_MYSQL_PROOF_DATABASE",
            $DatabaseName,
            "Process")
        Invoke-HibernateValidation
    }
    Invoke-BootstrapJava `
        -JavaPath $javaPath `
        -RequestedAction $Action `
        -ConnectorPath $connector
} finally {
    [System.Environment]::SetEnvironmentVariable(
        "SPRING_DATASOURCE_USERNAME",
        $savedUsername,
        "Process")
    [System.Environment]::SetEnvironmentVariable(
        "SPRING_DATASOURCE_PASSWORD",
        $savedPassword,
        "Process")
    [System.Environment]::SetEnvironmentVariable(
        "SPRING_DATASOURCE_URL",
        $savedDatasourceUrl,
        "Process")
    [System.Environment]::SetEnvironmentVariable(
        "ATSTUDIO_MYSQL_PROOF_ENABLED",
        $savedHibernateProofEnabled,
        "Process")
    [System.Environment]::SetEnvironmentVariable(
        "ATSTUDIO_MYSQL_PROOF_DATABASE",
        $savedHibernateProofDatabase,
        "Process")
    $credentials = $null
}
