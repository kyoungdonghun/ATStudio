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
    -Condition (-not $valid.Output.Contains($validName)) `
    -Message "Valid preflight should not echo the exact target database name."

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

$javaSource = Get-Content -Raw -LiteralPath $javaSourcePath
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
        "fixed-current-sql-inputs",
        "retired-migration-absence",
        "unrelated-database-enumeration-absence"
    )
} | ConvertTo-Json -Depth 5
