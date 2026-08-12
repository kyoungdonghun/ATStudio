[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"
$script:Failures = New-Object System.Collections.ArrayList
$bootstrapPath = Join-Path $PSScriptRoot "bootstrap-disposable-mysql.ps1"
$javaSourcePath = Join-Path $PSScriptRoot "DisposableMysqlBootstrap.java"

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

function Get-PowerShellCommand {
    $current = Get-Process -Id $PID
    if ($current.Path) {
        return $current.Path
    }
    $command = Get-Command powershell.exe -ErrorAction SilentlyContinue
    if (-not $command) {
        $command = Get-Command pwsh.exe -ErrorAction SilentlyContinue
    }
    if (-not $command) {
        throw "PowerShell executable was not found."
    }
    return $command.Source
}

function Get-JavaCommand {
    $command = Get-Command java.exe -ErrorAction SilentlyContinue
    if (-not $command) {
        $command = Get-Command java -ErrorAction SilentlyContinue
    }
    if (-not $command) {
        throw "Java 17 or later is required."
    }
    return $command.Source
}

function Invoke-JavaPreflight {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Workspace,
        [Parameter(Mandatory = $true)]
        [string] $DatabaseName,
        [string] $RequestedAction
    )

    $outputPath = Join-Path `
        ([System.IO.Path]::GetTempPath()) `
        ("atstudio-db-java-preflight-" + [guid]::NewGuid().ToString("N") + ".log")
    try {
        $arguments = [System.Collections.ArrayList] @(
            $javaSourcePath,
            "--action", "preflight",
            "--workspace", $Workspace,
            "--host", "127.0.0.1",
            "--port", "3306",
            "--database", $DatabaseName
        )
        if (-not [string]::IsNullOrWhiteSpace($RequestedAction)) {
            [void] $arguments.Add("--requested-action")
            [void] $arguments.Add($RequestedAction.ToLowerInvariant())
        }
        $process = Start-Process `
            -FilePath (Get-JavaCommand) `
            -ArgumentList $arguments `
            -RedirectStandardOutput $outputPath `
            -RedirectStandardError ($outputPath + ".err") `
            -WindowStyle Hidden `
            -PassThru `
            -Wait
        $text = ""
        if (Test-Path -LiteralPath $outputPath -PathType Leaf) {
            $text += [System.IO.File]::ReadAllText($outputPath)
        }
        if (Test-Path -LiteralPath ($outputPath + ".err") -PathType Leaf) {
            $text += [System.IO.File]::ReadAllText($outputPath + ".err")
        }
        return [pscustomobject]@{
            ExitCode = $process.ExitCode
            Output = $text
        }
    } finally {
        Remove-Item -LiteralPath $outputPath -Force -ErrorAction SilentlyContinue
        Remove-Item -LiteralPath ($outputPath + ".err") -Force -ErrorAction SilentlyContinue
    }
}

function Invoke-BootstrapProcess {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Action,
        [Parameter(Mandatory = $true)]
        [string] $DatabaseName,
        [Parameter(Mandatory = $true)]
        [string] $HostName
    )

    $outputPath = Join-Path `
        ([System.IO.Path]::GetTempPath()) `
        ("atstudio-db-bootstrap-test-" + [guid]::NewGuid().ToString("N") + ".log")
    try {
        $arguments = @(
            "-NoProfile",
            "-ExecutionPolicy", "Bypass",
            "-File", $bootstrapPath,
            "-Action", $Action,
            "-DatabaseName", $DatabaseName,
            "-HostName", $HostName
        )
        $process = Start-Process `
            -FilePath (Get-PowerShellCommand) `
            -ArgumentList $arguments `
            -RedirectStandardOutput $outputPath `
            -RedirectStandardError ($outputPath + ".err") `
            -WindowStyle Hidden `
            -PassThru `
            -Wait
        $text = ""
        if (Test-Path -LiteralPath $outputPath -PathType Leaf) {
            $text += [System.IO.File]::ReadAllText($outputPath)
        }
        if (Test-Path -LiteralPath ($outputPath + ".err") -PathType Leaf) {
            $text += [System.IO.File]::ReadAllText($outputPath + ".err")
        }
        return [pscustomobject]@{
            ExitCode = $process.ExitCode
            Output = $text
        }
    } finally {
        Remove-Item -LiteralPath $outputPath -Force -ErrorAction SilentlyContinue
        Remove-Item -LiteralPath ($outputPath + ".err") -Force -ErrorAction SilentlyContinue
    }
}

function Assert-Refusal {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Action,
        [Parameter(Mandatory = $true)]
        [string] $DatabaseName,
        [Parameter(Mandatory = $true)]
        [string] $HostName,
        [Parameter(Mandatory = $true)]
        [string] $ExpectedReason,
        [bool] $CheckTargetRedaction = $true
    )

    $result = Invoke-BootstrapProcess `
        -Action $Action `
        -DatabaseName $DatabaseName `
        -HostName $HostName
    Assert-True `
        -Condition ($result.ExitCode -ne 0) `
        -Message "Unsafe bootstrap case should fail: $ExpectedReason"
    Assert-True `
        -Condition ($result.Output.Contains("reason=$ExpectedReason")) `
        -Message "Unsafe bootstrap case should report the fixed reason: $ExpectedReason"
    if ($CheckTargetRedaction) {
        Assert-True `
            -Condition (-not $result.Output.Contains($DatabaseName)) `
            -Message "Bootstrap output should not echo the exact target database name."
    }
    Assert-True `
        -Condition (-not $result.Output.Contains("CREDENTIALS_UNAVAILABLE")) `
        -Message "Unsafe bootstrap case should fail before reading credentials."
    Assert-True `
        -Condition (-not $result.Output.Contains("Datasource credentials are unavailable")) `
        -Message "Unsafe bootstrap case should fail before wrapper credential loading."
    Assert-True `
        -Condition (-not $result.Output.Contains("MySQL Connector/J was not found")) `
        -Message "Unsafe bootstrap case should fail before connector discovery."
}

$tokens = $null
$errors = $null
[System.Management.Automation.Language.Parser]::ParseFile(
    $bootstrapPath,
    [ref] $tokens,
    [ref] $errors
) | Out-Null
Assert-True `
    -Condition ($errors.Count -eq 0) `
    -Message "The bootstrap PowerShell entry point should parse without errors."

$validName = "ats_disposable_20260724_a1b2c3d4"
$valid = Invoke-BootstrapProcess `
    -Action "Preflight" `
    -DatabaseName $validName `
    -HostName "127.0.0.1"
Assert-True `
    -Condition ($valid.ExitCode -eq 0) `
    -Message "A valid disposable target should pass preflight without MySQL."
Assert-True `
    -Condition ($valid.Output.Contains("safety.databasePattern=PASS")) `
    -Message "Valid preflight should report the database-name guard."
Assert-True `
    -Condition ($valid.Output.Contains("safety.hostClass=loopback")) `
    -Message "Valid preflight should report the loopback guard."
Assert-True `
    -Condition ($valid.Output.Contains("sql.order=schema.sql->seed.sql")) `
    -Message "Valid preflight should report the fixed SQL order."
Assert-True `
    -Condition ($valid.Output.Contains("source.schema.createTableStatements=42")) `
    -Message "Valid preflight should derive the current 42-table source count."
Assert-True `
    -Condition ($valid.Output.Contains("source.schema.createTableStatementsCheck=PASS")) `
    -Message "Valid preflight should report the source table-count check."
Assert-True `
    -Condition ($valid.Output.Contains("mysql.manifest.expectation=RECORDED")) `
    -Message "Preflight should report the current MySQL manifest as recorded."
Assert-True `
    -Condition (-not $valid.Output.Contains($validName)) `
    -Message "Valid preflight should not echo the exact target database name."

$schemaSourcePath = Join-Path $PSScriptRoot "..\..\src\main\resources\schema.sql"
$schemaSource = Get-Content -Raw -Encoding UTF8 -LiteralPath $schemaSourcePath
$sourceCreateTableCount = [regex]::Matches(
    $schemaSource,
    "(?im)^\s*CREATE\s+TABLE\b"
).Count
Assert-True `
    -Condition ($sourceCreateTableCount -eq 42) `
    -Message "Current schema.sql should contain exactly 42 CREATE TABLE statements."

$syntheticWorkspace = Join-Path `
    ([System.IO.Path]::GetTempPath()) `
    ("atstudio-db-source-preflight-" + [guid]::NewGuid().ToString("N"))
try {
    $syntheticResources = Join-Path $syntheticWorkspace "src\main\resources"
    [System.IO.Directory]::CreateDirectory($syntheticResources) | Out-Null
    $syntheticSchema = 1..41 | ForEach-Object {
        "CREATE TABLE synthetic_$($_) (id BIGINT);"
    }
    [System.IO.File]::WriteAllText(
        (Join-Path $syntheticResources "schema.sql"),
        ($syntheticSchema -join [Environment]::NewLine),
        [System.Text.UTF8Encoding]::new($false)
    )
    [System.IO.File]::WriteAllText(
        (Join-Path $syntheticResources "seed.sql"),
        "-- synthetic non-DB preflight input",
        [System.Text.UTF8Encoding]::new($false)
    )
    $staleSource = Invoke-JavaPreflight `
        -Workspace $syntheticWorkspace `
        -DatabaseName $validName
    Assert-True `
        -Condition ($staleSource.ExitCode -ne 0) `
        -Message "A 41-table source should fail closed during non-DB preflight."
    Assert-True `
        -Condition ($staleSource.Output.Contains(
            "reason=CURRENT_SCHEMA_CREATE_TABLE_COUNT_MISMATCH")) `
        -Message "A stale source count should report the fixed mismatch reason."
    Assert-True `
        -Condition (-not $staleSource.Output.Contains("CREDENTIALS_UNAVAILABLE")) `
        -Message "Source-count refusal should not read credentials."
} finally {
    if (Test-Path -LiteralPath $syntheticWorkspace -PathType Container) {
        [System.IO.Directory]::Delete($syntheticWorkspace, $true)
    }
}

foreach ($protected in @(
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
)) {
    Assert-Refusal `
        -Action "Create" `
        -DatabaseName $protected `
        -HostName "127.0.0.1" `
        -ExpectedReason "PROTECTED_DATABASE_NAME" `
        -CheckTargetRedaction $false
}

foreach ($malformed in @(
    "ats_disposable_20260724_short",
    "ats_disposable_20260724_ABCDEF12",
    "ats_disposable_20260724_abcdefgh_extra",
    "ats_wi007_20260724_a1b2c3d4",
    "ats-disposable-20260724-a1b2c3d4"
)) {
    Assert-Refusal `
        -Action "Validate" `
        -DatabaseName $malformed `
        -HostName "127.0.0.1" `
        -ExpectedReason "INVALID_DISPOSABLE_DATABASE_NAME"
}

foreach ($remoteHost in @(
    "db.example.com",
    "0.0.0.0",
    "127.0.0.2",
    "localhost:3306",
    "jdbc:mysql://127.0.0.1"
)) {
    Assert-Refusal `
        -Action "Drop" `
        -DatabaseName $validName `
        -HostName $remoteHost `
        -ExpectedReason "NON_LOOPBACK_HOST"
}

foreach ($allowedAction in @("Create", "Validate")) {
    $allowedPreflight = Invoke-JavaPreflight `
        -Workspace (Resolve-Path (Join-Path $PSScriptRoot "..\..")) `
        -DatabaseName $validName `
        -RequestedAction $allowedAction
    Assert-True `
        -Condition ($allowedPreflight.ExitCode -eq 0) `
        -Message "$allowedAction should pass the recorded-manifest preflight without MySQL."
    Assert-True `
        -Condition ($allowedPreflight.Output.Contains("mysql.manifest.expectation=RECORDED")) `
        -Message "$allowedAction preflight should retain the recorded manifest state."
    Assert-True `
        -Condition (-not $allowedPreflight.Output.Contains("CREDENTIALS_UNAVAILABLE")) `
        -Message "$allowedAction preflight should not read credentials or connect."
}

Assert-Refusal `
    -Action "Observe" `
    -DatabaseName $validName `
    -HostName "127.0.0.1" `
    -ExpectedReason "MYSQL_MANIFEST_OBSERVATION_NOT_REQUIRED"

$javaSource = Get-Content -Raw -Encoding UTF8 -LiteralPath $javaSourcePath
$wrapperSource = Get-Content -Raw -Encoding UTF8 -LiteralPath $bootstrapPath
$manifestOutputKeys = @(
    'safe("manifest.tables"',
    'safe("manifest.columns"',
    'safe("manifest.indexes"',
    'safe("manifest.foreignKeys"',
    'safe("manifest.plans"',
    'safe("manifest.planKeys"',
    'safe("manifest.forbiddenTables"',
    'safe("manifest.forbiddenColumns"',
    'safe("manifest.sha256"'
)
$matchesExpectedIndex = $javaSource.IndexOf(
    'if (!CURRENT_MYSQL_MANIFEST_EXPECTATION.matches(manifest))'
)
$manifestOutputCallIndex = $javaSource.IndexOf("outputManifest(manifest);")
Assert-True `
    -Condition ($manifestOutputCallIndex -ge 0 -and
        $manifestOutputCallIndex -lt $matchesExpectedIndex) `
    -Message "Manifest output must precede the fail-closed comparison."
foreach ($manifestOutputKey in $manifestOutputKeys) {
    $manifestOutputIndex = $javaSource.IndexOf($manifestOutputKey)
    Assert-True `
        -Condition ($manifestOutputIndex -ge 0) `
        -Message "Manifest output $manifestOutputKey must remain bounded and explicit."
}
Assert-True `
    -Condition ($javaSource.Contains(
        "EXPECTED_SOURCE_CREATE_TABLE_STATEMENTS = 42L")) `
    -Message "The helper should pin only the approved source-level table count."
Assert-True `
    -Condition ($javaSource.Contains(
        "new RecordedMysqlManifestExpectation(")) `
    -Message "The active MySQL manifest expectation should remain explicitly recorded."
$recordedManifestPattern = (
    'new\s+RecordedMysqlManifestExpectation\(\s*' +
    '(?<tables>\d+)L\s*,\s*' +
    '(?<columns>\d+)L\s*,\s*' +
    '(?<indexes>\d+)L\s*,\s*' +
    '(?<foreignKeys>\d+)L\s*,\s*' +
    '(?<plans>\d+)L\s*,\s*' +
    '"(?<sha256>[0-9a-f]{64})"\s*\)'
)
$recordedManifest = [regex]::Match($javaSource, $recordedManifestPattern)
Assert-True `
    -Condition $recordedManifest.Success `
    -Message "The recorded MySQL manifest should retain its bounded constructor signature."
$expectedRecordedManifest = [ordered] @{
    tables = "42"
    columns = "506"
    indexes = "173"
    foreignKeys = "90"
    plans = "6"
    sha256 = "acf28c935bf6107a8f2af431c971ebe0cd3539dba1aa1a941d966dde4a2a7a65"
}
foreach ($entry in $expectedRecordedManifest.GetEnumerator()) {
    Assert-True `
        -Condition ($recordedManifest.Groups[$entry.Key].Value -eq $entry.Value) `
        -Message "Recorded manifest $($entry.Key) should match the observed value."
}
Assert-True `
    -Condition ($javaSource.Contains("manifest.planKeys() == plans")) `
    -Message "Recorded manifest planKeys should remain equal to the observed plan count."
Assert-True `
    -Condition ($javaSource.Contains("manifest.forbiddenTables() == 0L")) `
    -Message "Recorded manifest forbidden-table guard should remain zero."
Assert-True `
    -Condition ($javaSource.Contains("manifest.forbiddenColumns() == 0L")) `
    -Message "Recorded manifest forbidden-column guard should remain zero."
Assert-True `
    -Condition ($javaSource.Contains('OBSERVE("observe")')) `
    -Message "The helper should retain a distinct manifest observation action."
Assert-True `
    -Condition ($javaSource.Contains("case CREATE, OBSERVE -> bootstrap.create();")) `
    -Message "Observe should use the same guarded create/apply/validate/cleanup path."
Assert-True `
    -Condition (-not $javaSource.Contains(
        "c581bef61cfba143744882b0674daf8d8fe742d82adbbf66d6b61699f5b86333")) `
    -Message "The predecessor manifest hash must not remain an active expectation."
Assert-True `
    -Condition (-not [regex]::IsMatch(
        $javaSource,
        "EXPECTED_(TABLES|COLUMNS|INDEXES|FOREIGN_KEYS|PLANS)\s*=")) `
    -Message "Predecessor MySQL manifest constants must be absent."
Assert-True `
    -Condition ([regex]::IsMatch(
        $javaSource,
        "if\s*\(created\)\s*\{\s*cleanupCreatedDatabase\(\);")) `
    -Message "A failed create or observation should retain exact-target cleanup."
Assert-True `
    -Condition ($javaSource.Contains(
        'execute(admin, "DROP DATABASE IF EXISTS `" + config.databaseName() + "`");')) `
    -Message "Cleanup should remain scoped to the guarded disposable database name."
Assert-True `
    -Condition (-not [regex]::IsMatch(
        $wrapperSource,
        'arguments\.Add\(\$credentials\.(Username|Password)\)')) `
    -Message "Credentials must not be passed to Java as command-line arguments."
Assert-True `
    -Condition (-not [regex]::IsMatch(
        $javaSource,
        'safe(Error)?\("(credential|username|password|jdbc)')) `
    -Message "Bootstrap output must not add credential or JDBC values."
Assert-True `
    -Condition ($javaSource.Contains('"src/main/resources/schema.sql"')) `
    -Message "The active helper should name current schema.sql."
Assert-True `
    -Condition ($javaSource.Contains('"src/main/resources/seed.sql"')) `
    -Message "The active helper should name current seed.sql."
Assert-True `
    -Condition (-not $javaSource.Contains("src/main/resources/db/manual")) `
    -Message "The active helper must not reference retired manual migrations."
Assert-True `
    -Condition (-not $javaSource.Contains("SHOW DATABASES")) `
    -Message "The active helper must not enumerate unrelated databases."
Assert-True `
    -Condition (-not $javaSource.Contains('safe("manifest.databaseName"')) `
    -Message "Manifest observation must not emit the database name."

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
        "powershell-parser",
        "valid-preflight-without-mysql",
        "protected-name-refusal-before-connector",
        "malformed-name-refusal-before-connector",
        "non-loopback-refusal-before-connector",
        "target-name-redaction",
        "current-source-create-table-count-42",
        "stale-source-count-refusal-without-mysql",
        "recorded-manifest-create-validate-preflight-without-mysql",
        "recorded-manifest-observe-refusal-before-credentials",
        "recorded-manifest-observed-values",
        "predecessor-manifest-expectation-absence",
        "observe-action-path",
        "exact-target-cleanup-preservation",
        "credential-command-line-and-output-absence",
        "manifest-observation-before-fail-closed-comparison",
        "manifest-database-name-redaction",
        "fixed-current-sql-inputs",
        "retired-migration-absence",
        "unrelated-database-enumeration-absence"
    )
} | ConvertTo-Json -Depth 5
