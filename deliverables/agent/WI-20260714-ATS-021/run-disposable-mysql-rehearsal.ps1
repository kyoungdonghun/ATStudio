param(
    [string]$Workspace = (Resolve-Path ".").Path,
    [string]$DatabaseName = "",
    [string]$LogPath = "",
    [switch]$KeepDatabase,
    [switch]$DropOnly
)

$ErrorActionPreference = "Stop"

$workspacePath = (Resolve-Path -LiteralPath $Workspace).Path
$artifactDir = Join-Path $workspacePath "deliverables\agent\WI-20260714-ATS-021"
New-Item -ItemType Directory -Force -Path $artifactDir | Out-Null

if ([string]::IsNullOrWhiteSpace($DatabaseName)) {
    $suffix = -join ((48..57 + 97..122) | Get-Random -Count 8 | ForEach-Object {[char]$_})
    $DatabaseName = "ats_wi021_{0}_{1}" -f (Get-Date -Format "yyyyMMdd"), $suffix
}

if ($DatabaseName -notmatch '^ats_wi021_\d{8}_[a-z0-9]{8}$') {
    throw "Refusing unexpected disposable database name: $DatabaseName"
}

if ([string]::IsNullOrWhiteSpace($LogPath)) {
    $LogPath = Join-Path $artifactDir "rehearsal-jdbc.log"
}

$connectorJar = Get-ChildItem -Recurse -File -Path "$env:USERPROFILE\.gradle\caches\modules-2\files-2.1\com.mysql\mysql-connector-j" -Filter "*.jar" |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if ($null -eq $connectorJar) {
    throw "MySQL Connector/J is unavailable in the Gradle cache."
}

$javaSource = Join-Path $artifactDir "DisposableMysqlRehearsal.java"
javac -cp $connectorJar.FullName -d $artifactDir $javaSource
if ($LASTEXITCODE -ne 0) {
    throw "javac failed with exit code $LASTEXITCODE"
}

Write-Output "preflight.mysqlCliOnPath=$([bool](Get-Command mysql -ErrorAction SilentlyContinue))"
Write-Output "preflight.mysql80ServiceRunning=$([bool](Get-Service MySQL80 -ErrorAction SilentlyContinue | Where-Object Status -eq 'Running'))"
Write-Output "preflight.connectorJPresent=$true"
Write-Output "preflight.credentialSource=process-env-or-application-local-yml; secret-values-not-printed"
Write-Output "preflight.disposableDatabase=$DatabaseName"

if ($DropOnly) {
    java -cp "$artifactDir;$($connectorJar.FullName)" DisposableMysqlRehearsal `
        --workspace $workspacePath `
        --database $DatabaseName `
        --log $LogPath `
        --mode drop-only
} else {
    $dropValue = if ($KeepDatabase) { "false" } else { "true" }
    java -cp "$artifactDir;$($connectorJar.FullName)" DisposableMysqlRehearsal `
        --workspace $workspacePath `
        --database $DatabaseName `
        --log $LogPath `
        --drop $dropValue
}
if ($LASTEXITCODE -ne 0) {
    throw "Java rehearsal failed with exit code $LASTEXITCODE"
}

Write-Output "rehearsal.log=$LogPath"
