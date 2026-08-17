Set-StrictMode -Version Latest

$script:SchemaVersion = 1
$script:FrontendPort = 5173
$script:BackendPort = 8080
$script:LocalFrontendUrl = "http://127.0.0.1:$script:FrontendPort"
$script:LocalApiUrl = "http://127.0.0.1:$script:BackendPort/api/tracks"
$script:TunnelOriginUrl = "http://127.0.0.1:$script:FrontendPort"
$script:RequiredBackendEnvironmentVariableNames = @(
    "SPRING_DATASOURCE_URL",
    "SPRING_DATASOURCE_USERNAME",
    "SPRING_DATASOURCE_PASSWORD",
    "JWT_SECRET",
    "APP_BOOTSTRAP_TEST_USERS_ENABLED",
    "APP_BOOTSTRAP_TEST_USERS_DEFAULT_PASSWORD"
)
$script:OptionalBackendEnvironmentVariableNames = @(
    "TOSS_CLIENT_KEY",
    "TOSS_SECRET_KEY",
    "TOSS_CANCEL_URL",
    "TOSS_CONNECT_TIMEOUT_MILLIS",
    "TOSS_READ_TIMEOUT_MILLIS",
    "PAYMENT_BILLING_KEY_ACTIVE_KEY_ID",
    "PAYMENT_BILLING_KEY_0_ID",
    "PAYMENT_BILLING_KEY_0_SECRET",
    "APP_PAYMENT_SCHEDULER_ZONE",
    "TOSS_BILLING_ISSUE_URL",
    "TOSS_BILLING_CHARGE_URL",
    "TOSS_BILLING_DELETE_URL",
    "TOSS_PAYMENT_LOOKUP_BY_ORDER_ID_URL",
    "TOSS_BILLING_CONNECT_TIMEOUT_MILLIS",
    "TOSS_BILLING_READ_TIMEOUT_MILLIS",
    "PAYMENT_RECONCILIATION_NOTIFICATION_ENABLED",
    "PAYMENT_OPERATIONS_OPERATOR_EMAIL",
    "GOOGLE_CLIENT_ID",
    "GOOGLE_CLIENT_SECRET",
    "KAKAO_CLIENT_ID",
    "KAKAO_CLIENT_SECRET",
    "NAVER_CLIENT_ID",
    "NAVER_CLIENT_SECRET",
    "MAIL_HOST",
    "MAIL_PORT",
    "MAIL_USERNAME",
    "MAIL_PASSWORD",
    "MAIL_SMTP_AUTH",
    "MAIL_SMTP_STARTTLS",
    "MAIL_FROM",
    "APP_STORAGE_PUBLIC_PATH",
    "APP_STORAGE_PRIVATE_PATH",
    "APP_STORAGE_RECOVERY_BATCH_SIZE",
    "APP_STORAGE_RECOVERY_MAX_ATTEMPTS",
    "APP_STORAGE_RECOVERY_STALE_SECONDS",
    "APP_STORAGE_RECOVERY_CLAIM_SECONDS",
    "APP_STORAGE_RECOVERY_INTERVAL_MS"
)
$script:BackendEnvironmentVariableNames = @(
    $script:RequiredBackendEnvironmentVariableNames
    $script:OptionalBackendEnvironmentVariableNames
)

function Initialize-AcceptanceProcessLogPumpType {
    if ($null -ne ("AcceptanceProcessLogPump" -as [type])) {
        return
    }

    Add-Type -TypeDefinition @'
using System;
using System.Collections.Concurrent;
using System.Diagnostics;
using System.IO;
using System.Text;

public sealed class AcceptanceProcessLogPump : IDisposable
{
    private static readonly ConcurrentDictionary<int, AcceptanceProcessLogPump> ActivePumps =
        new ConcurrentDictionary<int, AcceptanceProcessLogPump>();

    private readonly Process process;
    private readonly StreamWriter stdoutWriter;
    private readonly StreamWriter stderrWriter;
    private readonly object writeLock = new object();
    private readonly DataReceivedEventHandler stdoutHandler;
    private readonly DataReceivedEventHandler stderrHandler;
    private readonly EventHandler exitedHandler;
    private int processId = -1;
    private bool disposed;

    private AcceptanceProcessLogPump(Process process, string stdoutPath, string stderrPath)
    {
        this.process = process;
        stdoutWriter = new StreamWriter(stdoutPath, false, new UTF8Encoding(false)) { AutoFlush = true };
        stderrWriter = new StreamWriter(stderrPath, false, new UTF8Encoding(false)) { AutoFlush = true };
        stdoutHandler = WriteStdout;
        stderrHandler = WriteStderr;
        exitedHandler = OnExited;
    }

    public static Process Start(ProcessStartInfo startInfo, string stdoutPath, string stderrPath)
    {
        var process = new Process { StartInfo = startInfo };
        AcceptanceProcessLogPump pump = null;
        var started = false;
        try
        {
            pump = new AcceptanceProcessLogPump(process, stdoutPath, stderrPath);
            if (!process.Start())
            {
                throw new InvalidOperationException("The child process did not start.");
            }

            started = true;
            pump.processId = process.Id;
            ActivePumps[pump.processId] = pump;
            pump.Attach();
            return process;
        }
        catch
        {
            if (started)
            {
                StopStartedProcess(process);
            }
            if (pump != null)
            {
                pump.Dispose();
            }
            process.Dispose();
            throw;
        }
    }

    private static void StopStartedProcess(Process process)
    {
        try
        {
            if (!process.HasExited)
            {
                var killTree = typeof(Process).GetMethod("Kill", new[] { typeof(bool) });
                if (killTree != null)
                {
                    killTree.Invoke(process, new object[] { true });
                }
                else
                {
                    process.Kill();
                }
            }
        }
        catch
        {
            // Preserve the original stream-attachment failure.
        }

        try
        {
            process.WaitForExit(5000);
        }
        catch
        {
            // Preserve the original stream-attachment failure.
        }
    }

    private void Attach()
    {
        process.OutputDataReceived += stdoutHandler;
        process.ErrorDataReceived += stderrHandler;
        process.Exited += exitedHandler;
        process.BeginOutputReadLine();
        process.BeginErrorReadLine();
        process.EnableRaisingEvents = true;
        if (process.HasExited)
        {
            Complete();
        }
    }

    private void WriteStdout(object sender, DataReceivedEventArgs eventArgs)
    {
        WriteLine(stdoutWriter, eventArgs.Data);
    }

    private void WriteStderr(object sender, DataReceivedEventArgs eventArgs)
    {
        WriteLine(stderrWriter, eventArgs.Data);
    }

    private void WriteLine(StreamWriter writer, string line)
    {
        if (line == null)
        {
            return;
        }

        lock (writeLock)
        {
            if (!disposed)
            {
                writer.WriteLine(line);
            }
        }
    }

    private void OnExited(object sender, EventArgs eventArgs)
    {
        Complete();
    }

    private void Complete()
    {
        process.WaitForExit();
        Dispose();
    }

    public void Dispose()
    {
        lock (writeLock)
        {
            if (disposed)
            {
                return;
            }

            disposed = true;
            stdoutWriter.Dispose();
            stderrWriter.Dispose();
        }

        process.OutputDataReceived -= stdoutHandler;
        process.ErrorDataReceived -= stderrHandler;
        process.Exited -= exitedHandler;
        if (processId > 0)
        {
            AcceptanceProcessLogPump ignored;
            ActivePumps.TryRemove(processId, out ignored);
        }
    }
}
'@
}

function Get-AcceptanceDefaultRuntimeRoot {
    $base = $env:LOCALAPPDATA
    if ([string]::IsNullOrWhiteSpace($base)) {
        $base = [System.IO.Path]::GetTempPath()
    }

    return [System.IO.Path]::Combine($base, "ATStudio", "acceptance")
}

function Resolve-AcceptanceFullPath {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Path
    )

    return [System.IO.Path]::GetFullPath($Path)
}

function Resolve-AcceptanceRuntimeRoot {
    param(
        [string] $RuntimeRoot
    )

    if ([string]::IsNullOrWhiteSpace($RuntimeRoot)) {
        $RuntimeRoot = Get-AcceptanceDefaultRuntimeRoot
    }

    return Resolve-AcceptanceFullPath -Path $RuntimeRoot
}

function Assert-AcceptanceRuntimeOutsideRepo {
    param(
        [Parameter(Mandatory = $true)]
        [string] $RuntimeRoot,
        [Parameter(Mandatory = $true)]
        [string] $RepoRoot
    )

    $runtime = (Resolve-AcceptanceFullPath -Path $RuntimeRoot).TrimEnd('\', '/')
    $repo = (Resolve-AcceptanceFullPath -Path $RepoRoot).TrimEnd('\', '/')
    $comparison = [System.StringComparison]::OrdinalIgnoreCase

    if ($runtime.Equals($repo, $comparison) -or $runtime.StartsWith("$repo\", $comparison)) {
        throw "RuntimeRoot must be outside the repository: $runtime"
    }
}

function Read-AcceptanceBackendEnvironmentBundle {
    param(
        [Parameter(Mandatory = $true)]
        [string] $BackendEnvironmentPath,
        [Parameter(Mandatory = $true)]
        [string] $RepoRoot
    )

    if ([string]::IsNullOrWhiteSpace($BackendEnvironmentPath)) {
        throw "Backend environment bundle is required."
    }

    try {
        $resolvedPath = Resolve-AcceptanceFullPath -Path $BackendEnvironmentPath
        $resolvedRepo = (Resolve-AcceptanceFullPath -Path $RepoRoot).TrimEnd('\', '/')
    } catch {
        throw "Backend environment bundle must be a regular file outside the repository."
    }

    $comparison = [System.StringComparison]::OrdinalIgnoreCase
    $normalizedPath = $resolvedPath.TrimEnd('\', '/')
    if ($normalizedPath.Equals($resolvedRepo, $comparison) -or
        $normalizedPath.StartsWith("$resolvedRepo\", $comparison)) {
        throw "Backend environment bundle must be a regular file outside the repository."
    }

    try {
        $file = Get-Item -LiteralPath $resolvedPath -Force -ErrorAction Stop
    } catch {
        throw "Backend environment bundle must be a regular file outside the repository."
    }
    if (-not ($file -is [System.IO.FileInfo]) -or
        (($file.Attributes -band [System.IO.FileAttributes]::ReparsePoint) -ne 0)) {
        throw "Backend environment bundle must be a regular file outside the repository."
    }

    try {
        $json = [System.IO.File]::ReadAllText($file.FullName)
        $parsed = $json | ConvertFrom-Json -ErrorAction Stop
    } catch {
        throw "Backend environment bundle must contain valid JSON."
    }
    if (-not ($parsed -is [pscustomobject])) {
        throw "Backend environment bundle must be a flat JSON object."
    }

    $bundle = @{}
    foreach ($property in @($parsed.PSObject.Properties)) {
        if (-not ($script:BackendEnvironmentVariableNames -ccontains $property.Name)) {
            throw "Backend environment bundle contains a variable name that is not allowlisted."
        }
        if (-not ($property.Value -is [string]) -or
            [string]::IsNullOrWhiteSpace([string] $property.Value)) {
            throw "Backend environment bundle values must be nonblank strings."
        }
        $bundle[$property.Name] = [string] $property.Value
    }

    $missing = @(
        $script:RequiredBackendEnvironmentVariableNames |
            Where-Object { -not $bundle.ContainsKey($_) }
    )
    if ($missing.Count -gt 0) {
        throw "Backend environment bundle is missing required variable names: $($missing -join ', ')."
    }
    if ($bundle["APP_BOOTSTRAP_TEST_USERS_ENABLED"] -cne "true") {
        throw "Backend environment bundle must enable the QA bootstrap."
    }

    return $bundle
}

function Get-AcceptanceManifestPath {
    param(
        [string] $RuntimeRoot
    )

    $root = Resolve-AcceptanceRuntimeRoot -RuntimeRoot $RuntimeRoot
    return [System.IO.Path]::Combine($root, "runtime-manifest.json")
}

function Get-AcceptancePowerShellPath {
    $currentProcess = Get-Process -Id $PID
    if ($currentProcess.Path) {
        return $currentProcess.Path
    }

    $pwsh = Get-Command pwsh.exe -ErrorAction SilentlyContinue
    if ($pwsh) {
        return $pwsh.Source
    }

    return "powershell.exe"
}

function Find-AcceptanceCloudflared {
    param(
        [string] $CloudflaredPath
    )

    if (-not [string]::IsNullOrWhiteSpace($CloudflaredPath)) {
        $resolved = Resolve-AcceptanceFullPath -Path $CloudflaredPath
        if (-not (Test-Path -LiteralPath $resolved -PathType Leaf)) {
            throw "cloudflared was not found at the supplied path."
        }
        return $resolved
    }

    $command = Get-Command cloudflared.exe -ErrorAction SilentlyContinue
    if (-not $command) {
        $command = Get-Command cloudflared -ErrorAction SilentlyContinue
    }
    if (-not $command) {
        throw "cloudflared is required but was not found on PATH. Install/configure it outside this script."
    }

    return $command.Source
}

function Test-AcceptancePublicBaseUrl {
    param(
        [Parameter(Mandatory = $true)]
        [string] $PublicBaseUrl
    )

    if ($PublicBaseUrl -ne $PublicBaseUrl.Trim()) {
        return $false
    }
    if ($PublicBaseUrl.EndsWith("/")) {
        return $false
    }

    try {
        $uri = [System.Uri]::new($PublicBaseUrl)
    } catch {
        return $false
    }

    if ($uri.Scheme -ne "https") {
        return $false
    }
    if (-not [string]::IsNullOrWhiteSpace($uri.UserInfo)) {
        return $false
    }
    if ($uri.AbsolutePath -ne "/") {
        return $false
    }
    if (-not [string]::IsNullOrWhiteSpace($uri.Query)) {
        return $false
    }
    if (-not [string]::IsNullOrWhiteSpace($uri.Fragment)) {
        return $false
    }
    if (-not $uri.Host.EndsWith(".trycloudflare.com", [System.StringComparison]::OrdinalIgnoreCase)) {
        return $false
    }

    return $true
}

function Get-AcceptancePublicUrlFromText {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Text
    )

    $matches = [regex]::Matches($Text, "https://[A-Za-z0-9-]+\.trycloudflare\.com")
    $urls = @(
        $matches |
            ForEach-Object { $_.Value.ToLowerInvariant() } |
            Sort-Object -Unique
    )

    if ($urls.Count -eq 0) {
        return $null
    }
    if ($urls.Count -ne 1) {
        throw "Expected exactly one quick-tunnel URL, found $($urls.Count)."
    }
    if (-not (Test-AcceptancePublicBaseUrl -PublicBaseUrl $urls[0])) {
        throw "Quick-tunnel URL failed validation."
    }

    return $urls[0]
}

function New-AcceptanceCommandFingerprint {
    param(
        [string] $ExecutablePath,
        [string] $CommandLine
    )

    $material = "$ExecutablePath`n$CommandLine"
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($material)
    $hash = [System.Security.Cryptography.SHA256]::Create()
    try {
        return [System.BitConverter]::ToString($hash.ComputeHash($bytes)).Replace("-", "").ToLowerInvariant()
    } finally {
        $hash.Dispose()
    }
}

function Get-AcceptanceProcessSnapshot {
    param(
        [Parameter(Mandatory = $true)]
        [int] $ProcessId
    )

    $process = Get-Process -Id $ProcessId -ErrorAction SilentlyContinue
    if (-not $process) {
        return $null
    }

    $cim = Get-CimInstance Win32_Process -Filter "ProcessId = $ProcessId" -ErrorAction SilentlyContinue
    $executablePath = $process.Path
    $commandLine = ""
    if ($cim) {
        if ($cim.ExecutablePath) {
            $executablePath = $cim.ExecutablePath
        }
        if ($cim.CommandLine) {
            $commandLine = $cim.CommandLine
        }
    }
    if (-not $commandLine) {
        $commandLine = $executablePath
    }

    return [pscustomobject]@{
        pid = $ProcessId
        startTimeUtc = $process.StartTime.ToUniversalTime().ToString("o")
        executablePath = $executablePath
        commandLine = $commandLine
        commandFingerprint = New-AcceptanceCommandFingerprint `
            -ExecutablePath $executablePath `
            -CommandLine $commandLine
    }
}

function ConvertTo-AcceptanceServiceRecord {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Role,
        [Parameter(Mandatory = $true)]
        [System.Diagnostics.Process] $Process,
        [Parameter(Mandatory = $true)]
        [string] $WorkingDirectory,
        [Parameter(Mandatory = $true)]
        [string] $StdOutPath,
        [Parameter(Mandatory = $true)]
        [string] $StdErrPath
    )

    Start-Sleep -Milliseconds 250
    $snapshot = Get-AcceptanceProcessSnapshot -ProcessId $Process.Id
    if (-not $snapshot) {
        throw "Process for $Role exited before it could be recorded."
    }

    return [pscustomobject]@{
        role = $Role
        pid = $snapshot.pid
        startTimeUtc = $snapshot.startTimeUtc
        executablePath = $snapshot.executablePath
        commandLine = $snapshot.commandLine
        commandFingerprint = $snapshot.commandFingerprint
        workingDirectory = (Resolve-AcceptanceFullPath -Path $WorkingDirectory)
        stdout = (Resolve-AcceptanceFullPath -Path $StdOutPath)
        stderr = (Resolve-AcceptanceFullPath -Path $StdErrPath)
    }
}

function Start-AcceptanceOwnedProcess {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Role,
        [Parameter(Mandatory = $true)]
        [string] $FilePath,
        [Parameter(Mandatory = $true)]
        [string[]] $ArgumentList,
        [Parameter(Mandatory = $true)]
        [string] $WorkingDirectory,
        [Parameter(Mandatory = $true)]
        [string] $StdOutPath,
        [Parameter(Mandatory = $true)]
        [string] $StdErrPath,
        [hashtable] $Environment = @{},
        [string[]] $ExcludedEnvironmentVariableNames = @()
    )

    $childEnvironment = @{}
    foreach ($entry in [System.Environment]::GetEnvironmentVariables("Process").GetEnumerator()) {
        $childEnvironment[[string] $entry.Key] = [string] $entry.Value
    }
    foreach ($name in $ExcludedEnvironmentVariableNames) {
        [void] $childEnvironment.Remove($name)
    }
    foreach ($name in $Environment.Keys) {
        $childEnvironment[$name] = [string] $Environment[$name]
    }

    $startInfo = [System.Diagnostics.ProcessStartInfo]::new()
    $startInfo.FileName = $FilePath
    if ($null -ne $startInfo.PSObject.Properties["ArgumentList"]) {
        foreach ($argument in $ArgumentList) {
            [void] $startInfo.ArgumentList.Add($argument)
        }
    } else {
        $startInfo.Arguments = (@($ArgumentList | ForEach-Object {
            '"' + ($_ -replace '(\\*)"', '$1$1\\"' -replace '(\\+)$', '$1$1') + '"'
        }) -join " ")
    }
    $startInfo.WorkingDirectory = $WorkingDirectory
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $startInfo.UseShellExecute = $false
    $startInfo.CreateNoWindow = $true
    $startInfo.WindowStyle = [System.Diagnostics.ProcessWindowStyle]::Hidden
    $startInfo.EnvironmentVariables.Clear()
    foreach ($entry in $childEnvironment.GetEnumerator()) {
        $startInfo.EnvironmentVariables[$entry.Key] = $entry.Value
    }

    Initialize-AcceptanceProcessLogPumpType
    $process = [AcceptanceProcessLogPump]::Start($startInfo, $StdOutPath, $StdErrPath)

    return ConvertTo-AcceptanceServiceRecord `
        -Role $Role `
        -Process $process `
        -WorkingDirectory $WorkingDirectory `
        -StdOutPath $StdOutPath `
        -StdErrPath $StdErrPath
}

function New-AcceptanceRuntimeManifest {
    param(
        [Parameter(Mandatory = $true)]
        [string] $RepoRoot,
        [Parameter(Mandatory = $true)]
        [string] $RuntimeRoot,
        [Parameter(Mandatory = $true)]
        [string] $RunDirectory,
        [Parameter(Mandatory = $true)]
        [string] $PublicBaseUrl
    )

    $publicUri = [System.Uri]::new($PublicBaseUrl)
    return [pscustomobject]@{
        schemaVersion = $script:SchemaVersion
        state = "starting"
        repoRoot = (Resolve-AcceptanceFullPath -Path $RepoRoot)
        runtimeRoot = (Resolve-AcceptanceFullPath -Path $RuntimeRoot)
        runDirectory = (Resolve-AcceptanceFullPath -Path $RunDirectory)
        createdAtUtc = (Get-Date).ToUniversalTime().ToString("o")
        updatedAtUtc = (Get-Date).ToUniversalTime().ToString("o")
        publicBaseUrl = $PublicBaseUrl
        publicHost = $publicUri.Host.ToLowerInvariant()
        localFrontendUrl = $script:LocalFrontendUrl
        localApiUrl = $script:LocalApiUrl
        publicApiUrl = "$PublicBaseUrl/api/tracks"
        environmentVariableNames = @(
            "SPRING_PROFILES_ACTIVE",
            "APP_ACCEPTANCE_ENABLED",
            "APP_PUBLIC_BASE_URL",
            "APP_SECURITY_TRUSTED_CLIENT_IDENTITY_ENABLED",
            "APP_SECURITY_TRUSTED_PROXY_ADDRESSES"
        )
        services = [ordered]@{}
    }
}

function Save-AcceptanceManifest {
    param(
        [Parameter(Mandatory = $true)]
        [object] $Manifest,
        [Parameter(Mandatory = $true)]
        [string] $ManifestPath
    )

    $Manifest.updatedAtUtc = (Get-Date).ToUniversalTime().ToString("o")
    $directory = Split-Path -Parent $ManifestPath
    New-Item -ItemType Directory -Force -Path $directory | Out-Null
    $Manifest | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $ManifestPath -Encoding UTF8
}

function Get-AcceptanceManifest {
    param(
        [string] $RuntimeRoot
    )

    $path = Get-AcceptanceManifestPath -RuntimeRoot $RuntimeRoot
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        return $null
    }

    return Get-Content -Raw -LiteralPath $path | ConvertFrom-Json
}

function Test-AcceptanceProcessOwnership {
    param(
        [Parameter(Mandatory = $true)]
        [object] $Service
    )

    if (-not $Service.pid) {
        return $false
    }

    $snapshot = Get-AcceptanceProcessSnapshot -ProcessId ([int] $Service.pid)
    if (-not $snapshot) {
        return $false
    }

    $expectedStart = [datetime]::Parse($Service.startTimeUtc).ToUniversalTime()
    $actualStart = [datetime]::Parse($snapshot.startTimeUtc).ToUniversalTime()
    $startDelta = [Math]::Abs(($actualStart - $expectedStart).TotalSeconds)
    $pathMatches = $snapshot.executablePath -eq $Service.executablePath
    $fingerprintMatches = $snapshot.commandFingerprint -eq $Service.commandFingerprint

    return ($startDelta -le 2 -and $pathMatches -and $fingerprintMatches)
}

function Get-AcceptanceDescendantProcessIds {
    param(
        [Parameter(Mandatory = $true)]
        [int] $RootProcessId
    )

    $all = Get-CimInstance Win32_Process -ErrorAction SilentlyContinue
    $childrenByParent = @{}
    foreach ($process in $all) {
        $parent = [int] $process.ParentProcessId
        if (-not $childrenByParent.ContainsKey($parent)) {
            $childrenByParent[$parent] = New-Object System.Collections.ArrayList
        }
        [void] $childrenByParent[$parent].Add([int] $process.ProcessId)
    }

    $result = New-Object System.Collections.ArrayList
    $queue = New-Object System.Collections.Queue
    $queue.Enqueue($RootProcessId)
    while ($queue.Count -gt 0) {
        $current = [int] $queue.Dequeue()
        if (-not $childrenByParent.ContainsKey($current)) {
            continue
        }
        foreach ($child in $childrenByParent[$current]) {
            [void] $result.Add($child)
            $queue.Enqueue($child)
        }
    }

    return @($result | Sort-Object -Descending)
}

function Stop-AcceptanceOwnedService {
    param(
        [Parameter(Mandatory = $true)]
        [object] $Service
    )

    if (-not (Test-AcceptanceProcessOwnership -Service $Service)) {
        return [pscustomobject]@{
            role = $Service.role
            stopped = $false
            reason = "not-running-or-not-owned"
        }
    }

    $descendants = Get-AcceptanceDescendantProcessIds -RootProcessId ([int] $Service.pid)
    foreach ($childPid in $descendants) {
        Stop-Process -Id $childPid -Force -ErrorAction SilentlyContinue
    }
    Stop-Process -Id ([int] $Service.pid) -Force -ErrorAction SilentlyContinue

    return [pscustomobject]@{
        role = $Service.role
        stopped = $true
        reason = "owned-process-tree-stopped"
    }
}

function Get-AcceptanceServicesByName {
    param(
        [object] $Manifest,
        [System.Collections.IDictionary] $StartedServices = @{}
    )

    $servicesByName = [ordered]@{}
    foreach ($name in @("tunnel", "frontend", "backend")) {
        $service = $null
        if ($Manifest -and $Manifest.services) {
            $service = $Manifest.services.$name
        }
        if ((-not $service) -and $StartedServices.Contains($name)) {
            $service = $StartedServices[$name]
        }
        if ($service) {
            $servicesByName[$name] = $service
        }
    }

    return $servicesByName
}

function Invoke-AcceptanceServiceCleanup {
    param(
        [System.Collections.IDictionary] $ServicesByName = @{}
    )

    $stopped = New-Object System.Collections.ArrayList
    foreach ($name in @("tunnel", "frontend", "backend")) {
        if ($ServicesByName.Contains($name) -and $ServicesByName[$name]) {
            [void] $stopped.Add((Stop-AcceptanceOwnedService -Service $ServicesByName[$name]))
        }
    }

    return @($stopped)
}

function Test-AcceptanceTcpPortClosed {
    param(
        [Parameter(Mandatory = $true)]
        [int] $Port
    )

    $connections = Get-NetTCPConnection `
        -LocalPort $Port `
        -State Listen `
        -ErrorAction SilentlyContinue
    return (-not $connections)
}

function Test-AcceptanceUrlReady {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Url,
        [int] $TimeoutSeconds = 5
    )

    try {
        $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec $TimeoutSeconds
        return ($response.StatusCode -ge 200 -and $response.StatusCode -lt 400)
    } catch {
        return $false
    }
}

function Wait-AcceptanceCondition {
    param(
        [Parameter(Mandatory = $true)]
        [scriptblock] $Condition,
        [Parameter(Mandatory = $true)]
        [int] $TimeoutSeconds,
        [int] $IntervalMilliseconds = 1000,
        [string] $FailureMessage = "Timed out waiting for acceptance condition."
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    do {
        if (& $Condition) {
            return $true
        }
        Start-Sleep -Milliseconds $IntervalMilliseconds
    } while ((Get-Date) -lt $deadline)

    throw $FailureMessage
}

function Get-AcceptanceTunnelLogText {
    param(
        [Parameter(Mandatory = $true)]
        [object] $TunnelService
    )

    $parts = New-Object System.Collections.ArrayList
    foreach ($path in @($TunnelService.stdout, $TunnelService.stderr)) {
        if (Test-Path -LiteralPath $path -PathType Leaf) {
            [void] $parts.Add((Get-Content -Raw -LiteralPath $path))
        }
    }

    return ($parts -join "`n")
}

function Wait-AcceptanceTunnelUrl {
    param(
        [Parameter(Mandatory = $true)]
        [object] $TunnelService,
        [int] $TimeoutSeconds = 60
    )

    $url = $null
    Wait-AcceptanceCondition `
        -TimeoutSeconds $TimeoutSeconds `
        -FailureMessage "Timed out waiting for exactly one Cloudflare quick-tunnel URL." `
        -Condition {
            if (-not (Test-AcceptanceProcessOwnership -Service $TunnelService)) {
                throw "cloudflared exited or no longer matches the run manifest."
            }
            $text = Get-AcceptanceTunnelLogText -TunnelService $TunnelService
            $script:DiscoveredAcceptanceUrl = Get-AcceptancePublicUrlFromText -Text $text
            return ($null -ne $script:DiscoveredAcceptanceUrl)
        } | Out-Null

    $url = $script:DiscoveredAcceptanceUrl
    Remove-Variable -Name DiscoveredAcceptanceUrl -Scope Script -ErrorAction SilentlyContinue
    return $url
}

function New-AcceptanceChildEnvironment {
    param(
        [Parameter(Mandatory = $true)]
        [string] $PublicBaseUrl
    )

    return @{
        SPRING_PROFILES_ACTIVE = "acceptance"
        APP_ACCEPTANCE_ENABLED = "true"
        APP_PUBLIC_BASE_URL = $PublicBaseUrl
        APP_SECURITY_TRUSTED_CLIENT_IDENTITY_ENABLED = "true"
        APP_SECURITY_TRUSTED_PROXY_ADDRESSES = "127.0.0.1,::1"
    }
}

function New-AcceptanceBackendEnvironment {
    param(
        [Parameter(Mandatory = $true)]
        [hashtable] $ChildEnvironment,
        [Parameter(Mandatory = $true)]
        [hashtable] $BackendEnvironmentBundle
    )

    $environment = @{}
    foreach ($name in $ChildEnvironment.Keys) {
        $environment[$name] = $ChildEnvironment[$name]
    }
    foreach ($name in $BackendEnvironmentBundle.Keys) {
        $environment[$name] = $BackendEnvironmentBundle[$name]
    }
    return $environment
}

function New-AcceptanceDryRunPlan {
    param(
        [Parameter(Mandatory = $true)]
        [string] $RepoRoot,
        [string] $RuntimeRoot,
        [string] $CloudflaredPath
    )

    $resolvedRuntime = Resolve-AcceptanceRuntimeRoot -RuntimeRoot $RuntimeRoot
    Assert-AcceptanceRuntimeOutsideRepo -RuntimeRoot $resolvedRuntime -RepoRoot $RepoRoot
    $cloudflared = if ($CloudflaredPath) {
        Resolve-AcceptanceFullPath -Path $CloudflaredPath
    } else {
        "cloudflared(.exe) from PATH"
    }
    $powerShell = Get-AcceptancePowerShellPath

    return [pscustomobject]@{
        dryRun = $true
        repoRoot = (Resolve-AcceptanceFullPath -Path $RepoRoot)
        runtimeRoot = $resolvedRuntime
        manifestPath = (Get-AcceptanceManifestPath -RuntimeRoot $resolvedRuntime)
        runtimeContract = "repo-external manifest and logs only"
        backendEnvironmentBundle = @{
            required = $true
            source = "repo-external flat JSON file"
            requiredVariableCount = $script:RequiredBackendEnvironmentVariableNames.Count
            allowedVariableCount = $script:BackendEnvironmentVariableNames.Count
        }
        cloudflared = @{
            file = $cloudflared
            args = @("tunnel", "--url", $script:TunnelOriginUrl, "--no-autoupdate")
        }
        backend = @{
            file = $powerShell
            workingDirectory = (Resolve-AcceptanceFullPath -Path $RepoRoot)
            command = ".\gradlew.bat bootRun"
        }
        frontend = @{
            file = $powerShell
            workingDirectory = (Resolve-AcceptanceFullPath -Path (Join-Path $RepoRoot "frontend"))
            command = "npm.cmd run dev -- --host 127.0.0.1 --port 5173"
        }
        readiness = @(
            $script:LocalFrontendUrl,
            $script:LocalApiUrl,
            "PUBLIC_BASE_URL",
            "PUBLIC_BASE_URL/api/tracks"
        )
        environmentVariableNames = (New-AcceptanceChildEnvironment -PublicBaseUrl "https://example.trycloudflare.com").Keys |
            Sort-Object
    }
}

function Get-AcceptanceStatus {
    param(
        [string] $RuntimeRoot
    )

    $manifestPath = Get-AcceptanceManifestPath -RuntimeRoot $RuntimeRoot
    $manifest = Get-AcceptanceManifest -RuntimeRoot $RuntimeRoot
    if (-not $manifest) {
        return [pscustomobject]@{
            state = "not-started"
            manifestPath = $manifestPath
            services = @()
        }
    }

    $serviceStatuses = New-Object System.Collections.ArrayList
    foreach ($name in @("tunnel", "frontend", "backend")) {
        $service = $manifest.services.$name
        if (-not $service) {
            continue
        }
        [void] $serviceStatuses.Add([pscustomobject]@{
            role = $service.role
            pid = $service.pid
            owned = (Test-AcceptanceProcessOwnership -Service $service)
            startTimeUtc = $service.startTimeUtc
        })
    }

    $running = @($serviceStatuses | Where-Object { $_.owned }).Count
    $state = if ($running -gt 0) { "running" } else { $manifest.state }

    return [pscustomobject]@{
        state = $state
        manifestPath = $manifestPath
        publicBaseUrl = $manifest.publicBaseUrl
        localFrontendUrl = $manifest.localFrontendUrl
        localApiUrl = $manifest.localApiUrl
        publicApiUrl = $manifest.publicApiUrl
        services = @($serviceStatuses)
    }
}

function Stop-AcceptanceEnvironment {
    param(
        [string] $RuntimeRoot,
        [int] $TimeoutSeconds = 30
    )

    $manifestPath = Get-AcceptanceManifestPath -RuntimeRoot $RuntimeRoot
    $manifest = Get-AcceptanceManifest -RuntimeRoot $RuntimeRoot
    if (-not $manifest) {
        return [pscustomobject]@{
            state = "not-started"
            manifestPath = $manifestPath
            stopped = @()
        }
    }

    $servicesByName = Get-AcceptanceServicesByName -Manifest $manifest
    $stopped = Invoke-AcceptanceServiceCleanup -ServicesByName $servicesByName

    Wait-AcceptanceCondition `
        -TimeoutSeconds $TimeoutSeconds `
        -FailureMessage "Timed out waiting for frontend/backend ports to close." `
        -Condition {
            (Test-AcceptanceTcpPortClosed -Port $script:FrontendPort) -and
                (Test-AcceptanceTcpPortClosed -Port $script:BackendPort)
        } | Out-Null

    $publicClosed = $true
    if ($manifest.publicBaseUrl) {
        $publicClosed = -not (Test-AcceptanceUrlReady -Url $manifest.publicBaseUrl -TimeoutSeconds 5)
    }

    $manifest.state = "stopped"
    Save-AcceptanceManifest -Manifest $manifest -ManifestPath $manifestPath

    return [pscustomobject]@{
        state = "stopped"
        manifestPath = $manifestPath
        stopped = @($stopped)
        portsClosed = $true
        publicUrlUnreachable = $publicClosed
    }
}

function Start-AcceptanceEnvironment {
    param(
        [Parameter(Mandatory = $true)]
        [string] $RepoRoot,
        [string] $RuntimeRoot,
        [string] $CloudflaredPath,
        [Parameter(Mandatory = $true)]
        [string] $BackendEnvironmentPath,
        [int] $TunnelTimeoutSeconds = 60,
        [int] $ReadinessTimeoutSeconds = 180
    )

    $resolvedRepo = Resolve-AcceptanceFullPath -Path $RepoRoot
    $resolvedRuntime = Resolve-AcceptanceRuntimeRoot -RuntimeRoot $RuntimeRoot
    Assert-AcceptanceRuntimeOutsideRepo -RuntimeRoot $resolvedRuntime -RepoRoot $resolvedRepo

    $existing = Get-AcceptanceStatus -RuntimeRoot $resolvedRuntime
    if ($existing.state -eq "running") {
        return $existing
    }

    $cloudflared = Find-AcceptanceCloudflared -CloudflaredPath $CloudflaredPath
    $runId = (Get-Date).ToUniversalTime().ToString("yyyyMMddTHHmmssZ")
    $runDirectory = Join-Path $resolvedRuntime $runId
    New-Item -ItemType Directory -Force -Path $runDirectory | Out-Null
    $manifestPath = Get-AcceptanceManifestPath -RuntimeRoot $resolvedRuntime
    $manifest = $null
    $startedServices = [ordered]@{}
    $completedSuccessfully = $false

    try {
        $tunnel = Start-AcceptanceOwnedProcess `
            -Role "tunnel" `
            -FilePath $cloudflared `
            -ArgumentList @("tunnel", "--url", $script:TunnelOriginUrl, "--no-autoupdate") `
            -WorkingDirectory $resolvedRepo `
            -StdOutPath (Join-Path $runDirectory "cloudflared.out.log") `
            -StdErrPath (Join-Path $runDirectory "cloudflared.err.log") `
            -ExcludedEnvironmentVariableNames $script:BackendEnvironmentVariableNames
        $startedServices.tunnel = $tunnel

        $publicBaseUrl = Wait-AcceptanceTunnelUrl `
            -TunnelService $tunnel `
            -TimeoutSeconds $TunnelTimeoutSeconds
        $manifest = New-AcceptanceRuntimeManifest `
            -RepoRoot $resolvedRepo `
            -RuntimeRoot $resolvedRuntime `
            -RunDirectory $runDirectory `
            -PublicBaseUrl $publicBaseUrl
        $manifest.services.tunnel = $tunnel
        Save-AcceptanceManifest -Manifest $manifest -ManifestPath $manifestPath

        $backendEnvironmentBundle = Read-AcceptanceBackendEnvironmentBundle `
            -BackendEnvironmentPath $BackendEnvironmentPath `
            -RepoRoot $resolvedRepo
        $childEnvironment = New-AcceptanceChildEnvironment -PublicBaseUrl $publicBaseUrl
        $backendEnvironment = New-AcceptanceBackendEnvironment `
            -ChildEnvironment $childEnvironment `
            -BackendEnvironmentBundle $backendEnvironmentBundle
        $powerShell = Get-AcceptancePowerShellPath

        $backend = Start-AcceptanceOwnedProcess `
            -Role "backend" `
            -FilePath $powerShell `
            -ArgumentList @("-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", "& { .\gradlew.bat bootRun }") `
            -WorkingDirectory $resolvedRepo `
            -StdOutPath (Join-Path $runDirectory "backend.out.log") `
            -StdErrPath (Join-Path $runDirectory "backend.err.log") `
            -Environment $backendEnvironment `
            -ExcludedEnvironmentVariableNames $script:BackendEnvironmentVariableNames
        $startedServices.backend = $backend
        $manifest.services.backend = $backend
        Save-AcceptanceManifest -Manifest $manifest -ManifestPath $manifestPath

        $frontend = Start-AcceptanceOwnedProcess `
            -Role "frontend" `
            -FilePath $powerShell `
            -ArgumentList @("-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", "& { npm.cmd run dev -- --host 127.0.0.1 --port 5173 }") `
            -WorkingDirectory (Join-Path $resolvedRepo "frontend") `
            -StdOutPath (Join-Path $runDirectory "frontend.out.log") `
            -StdErrPath (Join-Path $runDirectory "frontend.err.log") `
            -Environment $childEnvironment `
            -ExcludedEnvironmentVariableNames $script:BackendEnvironmentVariableNames
        $startedServices.frontend = $frontend
        $manifest.services.frontend = $frontend
        Save-AcceptanceManifest -Manifest $manifest -ManifestPath $manifestPath

        foreach ($url in @(
            $script:LocalFrontendUrl,
            $script:LocalApiUrl,
            $publicBaseUrl,
            "$publicBaseUrl/api/tracks"
        )) {
            Wait-AcceptanceCondition `
                -TimeoutSeconds $ReadinessTimeoutSeconds `
                -FailureMessage "Readiness check failed: $url" `
                -Condition { Test-AcceptanceUrlReady -Url $url -TimeoutSeconds 5 } | Out-Null
        }

        $manifest.state = "ready"
        Save-AcceptanceManifest -Manifest $manifest -ManifestPath $manifestPath
        $completedSuccessfully = $true
        return Get-AcceptanceStatus -RuntimeRoot $resolvedRuntime
    } catch {
        if ($manifest) {
            $manifest.state = "failed"
            Save-AcceptanceManifest -Manifest $manifest -ManifestPath $manifestPath
        }
        throw
    } finally {
        if (-not $completedSuccessfully) {
            $servicesByName = Get-AcceptanceServicesByName `
                -Manifest $manifest `
                -StartedServices $startedServices
            try {
                Invoke-AcceptanceServiceCleanup -ServicesByName $servicesByName | Out-Null
            } catch {
                # Preserve the original startup failure while still making cleanup best-effort.
            }
        }
    }
}

Export-ModuleMember -Function `
    Get-AcceptanceDefaultRuntimeRoot, `
    Get-AcceptanceManifestPath, `
    Get-AcceptanceManifest, `
    Get-AcceptanceStatus, `
    Get-AcceptancePublicUrlFromText, `
    New-AcceptanceDryRunPlan, `
    Start-AcceptanceEnvironment, `
    Stop-AcceptanceEnvironment, `
    Test-AcceptanceProcessOwnership, `
    Test-AcceptancePublicBaseUrl
