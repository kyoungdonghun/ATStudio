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
            $javaRequestedAction = if ($RequestedAction -eq "HibernateValidate") {
                "hibernate-validate"
            } else {
                $RequestedAction.ToLowerInvariant()
            }
            [void] $arguments.Add($javaRequestedAction)
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
    -Condition ($valid.Output.Contains("source.schema.createTableStatements=43")) `
    -Message "Valid preflight should derive the current 43-table source count."
Assert-True `
    -Condition ($valid.Output.Contains("source.schema.createTableStatementsCheck=PASS")) `
    -Message "Valid preflight should report the source table-count check."
Assert-True `
    -Condition ($valid.Output.Contains("mysql.manifest.expectation=RECORDED")) `
    -Message "Preflight should report the recorded current MySQL manifest."
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
    -Condition ($sourceCreateTableCount -eq 43) `
    -Message "Current schema.sql should contain exactly 43 CREATE TABLE statements."

$syntheticWorkspace = Join-Path `
    ([System.IO.Path]::GetTempPath()) `
    ("atstudio-db-source-preflight-" + [guid]::NewGuid().ToString("N"))
try {
    $syntheticResources = Join-Path $syntheticWorkspace "src\main\resources"
    [System.IO.Directory]::CreateDirectory($syntheticResources) | Out-Null
    $syntheticSchema = 1..42 | ForEach-Object {
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
        -DatabaseName $validName `
        -RequestedAction "Inventory"
    Assert-True `
        -Condition ($staleSource.ExitCode -ne 0) `
        -Message "A 42-table source should fail closed during non-DB preflight."
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

Assert-Refusal `
    -Action "Inventory" `
    -DatabaseName "ats_disposable_20260724_short" `
    -HostName "127.0.0.1" `
    -ExpectedReason "INVALID_DISPOSABLE_DATABASE_NAME"

Assert-Refusal `
    -Action "Inventory" `
    -DatabaseName $validName `
    -HostName "db.example.com" `
    -ExpectedReason "NON_LOOPBACK_HOST"

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

foreach ($allowedAction in @("Create", "Validate", "HibernateValidate")) {
    $allowedPreflight = Invoke-JavaPreflight `
        -Workspace (Resolve-Path (Join-Path $PSScriptRoot "..\..")) `
        -DatabaseName $validName `
        -RequestedAction $allowedAction
    Assert-True `
        -Condition ($allowedPreflight.ExitCode -eq 0) `
        -Message "$allowedAction should pass recorded-manifest preflight without MySQL."
    Assert-True `
        -Condition ($allowedPreflight.Output.Contains("mysql.manifest.expectation=RECORDED")) `
        -Message "$allowedAction preflight should report the recorded manifest state."
    Assert-True `
        -Condition (-not $allowedPreflight.Output.Contains("CREDENTIALS_UNAVAILABLE")) `
        -Message "$allowedAction preflight should not read credentials or connect."
}

$observationPreflight = Invoke-JavaPreflight `
    -Workspace (Resolve-Path (Join-Path $PSScriptRoot "..\\..")) `
    -DatabaseName $validName `
    -RequestedAction "Observe"
Assert-True `
    -Condition ($observationPreflight.ExitCode -ne 0) `
    -Message "Observe preflight should refuse after the MySQL manifest is recorded."
Assert-True `
    -Condition ($observationPreflight.Output.Contains("mysql.manifest.expectation=RECORDED")) `
    -Message "Observe preflight should report the recorded manifest state."
Assert-True `
    -Condition ($observationPreflight.Output.Contains(
        "reason=MYSQL_MANIFEST_OBSERVATION_NOT_REQUIRED")) `
    -Message "Observe preflight should use the recorded-manifest refusal reason."

$inventoryPreflight = Invoke-JavaPreflight `
    -Workspace (Resolve-Path (Join-Path $PSScriptRoot "..\\..")) `
    -DatabaseName $validName `
    -RequestedAction "Inventory"
Assert-True `
    -Condition ($inventoryPreflight.ExitCode -eq 0) `
    -Message "Inventory preflight should remain available after the MySQL manifest is recorded."
Assert-True `
    -Condition ($inventoryPreflight.Output.Contains("mysql.manifest.expectation=RECORDED")) `
    -Message "Inventory preflight should report the recorded manifest state."
Assert-True `
    -Condition (-not $inventoryPreflight.Output.Contains("CREDENTIALS_UNAVAILABLE")) `
    -Message "Inventory preflight should not read credentials or connect."

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
        "EXPECTED_SOURCE_CREATE_TABLE_STATEMENTS = 43L")) `
    -Message "The helper should pin only the approved source-level table count."
Assert-True `
    -Condition ([regex]::IsMatch(
        $javaSource,
        "CURRENT_MYSQL_MANIFEST_EXPECTATION\s*=\s*new\s+RecordedMysqlManifestExpectation")) `
    -Message "The active MySQL manifest expectation should be explicitly recorded."
$recordedManifestPattern = (
    'new\s+RecordedMysqlManifestExpectation\(\s*' +
    '(?<tables>\d+)L\s*,\s*' +
    '(?<columns>\d+)L\s*,\s*' +
    '(?<indexes>\d+)L\s*,\s*' +
    '(?<foreignKeys>\d+)L\s*,\s*' +
    '(?<plans>\d+)L\s*,\s*' +
    '(?<planKeys>\d+)L\s*,\s*' +
    '(?<forbiddenTables>\d+)L\s*,\s*' +
    '(?<forbiddenColumns>\d+)L\s*,\s*' +
    '"(?<sha256>[0-9a-f]{64})"\s*\)'
)
$recordedManifest = [regex]::Match($javaSource, $recordedManifestPattern)
Assert-True `
    -Condition $recordedManifest.Success `
    -Message "The recorded MySQL manifest should retain every bounded field."
$expectedRecordedManifest = [ordered] @{
    tables = "43"
    columns = "511"
    indexes = "175"
    foreignKeys = "91"
    plans = "6"
    planKeys = "6"
    forbiddenTables = "0"
    forbiddenColumns = "0"
    sha256 = "b177b34780fabc75ea8b4608a0d210167a81d414d2778cc1d1dc5c0e39c8fea4"
}
foreach ($entry in $expectedRecordedManifest.GetEnumerator()) {
    Assert-True `
        -Condition ($recordedManifest.Groups[$entry.Key].Value -eq $entry.Value) `
        -Message "Recorded manifest $($entry.Key) should match the approved observation."
}
Assert-True `
    -Condition ($javaSource.Contains("manifest.planKeys() == planKeys")) `
    -Message "Recorded manifest matching should retain the observed plan-key count."
Assert-True `
    -Condition ($javaSource.Contains("manifest.forbiddenTables() == forbiddenTables")) `
    -Message "Recorded manifest matching should retain the observed forbidden-table count."
Assert-True `
    -Condition ($javaSource.Contains("manifest.forbiddenColumns() == forbiddenColumns")) `
    -Message "Recorded manifest matching should retain the observed forbidden-column count."
Assert-True `
    -Condition ($javaSource.Contains('OBSERVE("observe")')) `
    -Message "The helper should retain a distinct manifest observation action."
Assert-True `
    -Condition ($javaSource.Contains('INVENTORY("inventory")')) `
    -Message "The helper should add the Inventory action without removing existing actions."
Assert-True `
    -Condition ($wrapperSource.Contains(
        '[ValidateSet("Preflight", "Observe", "Create", "Validate", "Drop", "Inventory", "HibernateValidate")]')) `
    -Message "The supported wrapper should allow guarded Hibernate validation alongside existing actions."
Assert-True `
    -Condition ($javaSource.Contains('HIBERNATE_VALIDATE("hibernate-validate")')) `
    -Message "The helper should retain the wrapper-managed Hibernate validation action."
Assert-True `
    -Condition ($javaSource.Contains('case HIBERNATE_VALIDATE -> bootstrap.hibernateValidate();')) `
    -Message "The helper should emit the Hibernate validation proof status only after wrapper execution."
Assert-True `
    -Condition ($javaSource.Contains("case CREATE, OBSERVE -> bootstrap.create();")) `
    -Message "Observe should use the same guarded create/apply/validate/cleanup path."
Assert-True `
    -Condition ($javaSource.Contains("case INVENTORY -> bootstrap.inventory();")) `
    -Message "Inventory should use its dedicated read-only bootstrap path."
Assert-True `
    -Condition (-not $javaSource.Contains(
        "UnrecordedMysqlManifestExpectation.INSTANCE")) `
    -Message "The unrecorded MySQL manifest expectation must not remain active."
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

$mysqlValidationTestPath = Join-Path `
    $PSScriptRoot `
    "..\\..\\src\\test\\java\\com\\atstudio\\atstudio\\service\\PaymentMysqlSchemaValidationTest.java"
$mysqlValidationSource = Get-Content -Raw -Encoding UTF8 -LiteralPath $mysqlValidationTestPath
Assert-True `
    -Condition ($mysqlValidationSource.Contains('@EnabledIf("isExplicitGuardedDisposableProof")')) `
    -Message "Hibernate validation should require an explicit guarded proof condition before Spring starts."
Assert-True `
    -Condition ($mysqlValidationSource.Contains('ATSTUDIO_MYSQL_PROOF_ENABLED')) `
    -Message "Hibernate validation should require an explicit opt-in environment flag."
Assert-True `
    -Condition ($mysqlValidationSource.Contains('ATSTUDIO_MYSQL_PROOF_DATABASE')) `
    -Message "Hibernate validation should bind the selected datasource to the generated disposable name."
Assert-True `
    -Condition ($mysqlValidationSource.Contains('^ats_disposable_\\d{8}_[a-z0-9]{8}$')) `
    -Message "Hibernate validation should allow only the guarded disposable naming contract."
Assert-True `
    -Condition ($mysqlValidationSource.Contains('^jdbc:mysql://(?:localhost|127\\.0\\.0\\.1|\\[::1])(?::3306)?/')) `
    -Message "Hibernate validation should allow only loopback MySQL JDBC targets."
Assert-True `
    -Condition (-not $mysqlValidationSource.Contains('"local-atstudio"')) `
    -Message "Hibernate validation must not retain the local retained-database override."
Assert-True `
    -Condition (-not $mysqlValidationSource.Contains('"atstudio"')) `
    -Message "Hibernate validation must not admit the retained application database."
Assert-True `
    -Condition (-not [regex]::IsMatch($mysqlValidationSource, '(?i)\bSELECT\b')) `
    -Message "Hibernate validation proof must not use a manual SQL query."
Assert-True `
    -Condition ($wrapperSource.Contains('Invoke-HibernateValidation')) `
    -Message "The supported wrapper should own the targeted Hibernate invocation."
Assert-True `
    -Condition ($wrapperSource.Contains('ATSTUDIO_MYSQL_PROOF_DATABASE')) `
    -Message "The supported wrapper should bind the targeted Hibernate proof to its guarded name."
Assert-True `
    -Condition ($wrapperSource.Contains('jdbc:mysql://$HostName`:$Port/$DatabaseName')) `
    -Message "The supported wrapper should construct the Hibernate JDBC target from guarded inputs only."

$inventorySqlMatches = [regex]::Matches(
    $javaSource,
    '(?s)private static final String INVENTORY_COUNT_SQL\s*=\s*"(?<first>[^"]*)"\s*\+\s*"(?<second>[^"]*)";'
)
Assert-True `
    -Condition ($inventorySqlMatches.Count -eq 1) `
    -Message "Inventory should define exactly one fixed aggregate query."
if ($inventorySqlMatches.Count -eq 1) {
    $inventorySql = $inventorySqlMatches[0].Groups["first"].Value +
        $inventorySqlMatches[0].Groups["second"].Value
    Assert-True `
        -Condition ($inventorySql -eq
            "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name REGEXP '^ats_disposable_[0-9]{8}_[a-z0-9]{8}$'") `
        -Message "Inventory query must be the fixed disposable-schema COUNT predicate."
}

$inventoryMethodMatch = [regex]::Match(
    $javaSource,
    '(?s)void inventory\(\) throws SQLException \{(?<body>.*?)\r?\n        \}\r?\n\r?\n        private Connection openAdminConnection'
)
Assert-True `
    -Condition ($inventoryMethodMatch.Success) `
    -Message "Inventory should have a bounded dedicated method."
if ($inventoryMethodMatch.Success) {
    $inventoryMethod = $inventoryMethodMatch.Groups["body"].Value
    $inventoryOutputKeys = @(
        [regex]::Matches($inventoryMethod, 'safe\(\s*"(?<key>[^"]+)"') |
            ForEach-Object { $_.Groups["key"].Value }
    )
    Assert-True `
        -Condition (($inventoryMethod -split "openAdminConnection\(\)").Count -eq 2) `
        -Message "Inventory should open exactly one admin connection."
    Assert-True `
        -Condition (-not $inventoryMethod.Contains("openDatabaseConnection()")) `
        -Message "Inventory must not open a selected-database connection."
    Assert-True `
        -Condition (-not $inventoryMethod.Contains("requireSelectedDatabase")) `
        -Message "Inventory must not select or verify a target database."
    Assert-True `
        -Condition (-not $inventoryMethod.Contains("databaseCount(")) `
        -Message "Inventory must not query its companion disposable name."
    Assert-True `
        -Condition (-not [regex]::IsMatch(
            $inventoryMethod,
            '(?i)\b(CREATE|DROP|ALTER|UPDATE|INSERT|DELETE)\b')) `
        -Message "Inventory must not perform a mutating SQL operation."
    Assert-True `
        -Condition (-not [regex]::IsMatch($inventoryMethod, '(?i)manifest')) `
        -Message "Inventory must not record, compare, or emit a manifest."
    Assert-True `
        -Condition ($inventoryOutputKeys.Count -eq 2 -and
            $inventoryOutputKeys -contains "inventory.count" -and
            $inventoryOutputKeys -contains "inventory.state") `
        -Message "Inventory must emit only its bounded count and state fields."
    Assert-True `
        -Condition ([regex]::IsMatch(
            $inventoryMethod,
            '(?s)count\s*==\s*0L\s*\?\s*"NO_POSSIBLE_ORPHAN"\s*:\s*"POSSIBLE_ORPHAN_EXISTS"')) `
        -Message "Inventory output state must map zero and positive counts exactly."
}

$inventoryCountMethodMatch = [regex]::Match(
    $javaSource,
    '(?s)private long inventoryCount\(Connection connection\) throws SQLException \{(?<body>.*?)\r?\n        \}\r?\n\r?\n        private void requireSelectedDatabase'
)
Assert-True `
    -Condition ($inventoryCountMethodMatch.Success) `
    -Message "Inventory should keep its aggregate query in one bounded helper."
if ($inventoryCountMethodMatch.Success) {
    $inventoryCountMethod = $inventoryCountMethodMatch.Groups["body"].Value
    Assert-True `
        -Condition ($inventoryCountMethod.Contains(
            "connection.prepareStatement(INVENTORY_COUNT_SQL)")) `
        -Message "Inventory should execute only the fixed aggregate query."
    Assert-True `
        -Condition (($inventoryCountMethod -split "executeQuery\(").Count -eq 2) `
        -Message "Inventory should execute exactly one query."
    Assert-True `
        -Condition (-not $inventoryCountMethod.Contains("getString(")) `
        -Message "Inventory must not select or read schema names."
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
        "powershell-parser",
        "valid-preflight-without-mysql",
        "protected-name-refusal-before-connector",
        "malformed-name-refusal-before-connector",
        "non-loopback-refusal-before-connector",
        "target-name-redaction",
        "current-source-create-table-count-43",
        "stale-source-count-refusal-without-mysql",
        "recorded-manifest-create-validate-hibernate-preflight-without-mysql",
        "recorded-manifest-observe-refusal-without-mysql",
        "recorded-manifest-inventory-preflight-without-mysql",
        "inventory-wrapper-allowlist-and-preconnection-name-refusal",
        "inventory-preconnection-loopback-refusal",
        "inventory-fixed-count-query-and-output-contract",
        "inventory-admin-only-single-query-no-target-selection",
        "recorded-manifest-active-expectation",
        "recorded-manifest-observed-values",
        "guarded-hibernate-disposable-only-proof",
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
