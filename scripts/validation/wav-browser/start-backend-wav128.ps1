# WI-20260909-ATS-027: prepared test-runtime launcher, not production approval.
# Run only after MA records tuple, unfinished-command and automatic-job disposition.
#Requires -Version 7.4
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateNotNullOrEmpty()]
    [string]$JarPath,
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[A-Fa-f0-9]{64}$')]
    [string]$JarSha256,
    [Parameter(Mandatory = $true)]
    [uri]$PublicOrigin,
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[A-Fa-f0-9]{64}$')]
    [string]$LocalConfigSha256,
    [switch]$CheckOnly,
    [switch]$RuntimeSafetyReviewed
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$repo = 'C:\Users\jm991\Desktop\project\ATStudio'
$runtime = 'C:\Users\jm991\AppData\Local\ATStudio\remote-development-20260908'
$java = 'C:\Program Files\Java\jdk-17\bin\java.exe'
$localConfig = Join-Path $repo 'application-local.yml'
$mailFile = 'C:\Users\jm991\AppData\Local\ATStudio\acceptance-backend-environment.json'
$expectedJar = Join-Path $runtime 'ATStudio-wav128-20260909.jar'
$helperRoot = Join-Path $repo 'scripts\validation\wav-browser'
$ffmpeg = 'C:\Users\jm991\AppData\Local\ATStudio\tools\ffmpeg-128k-20260909\unpacked\ffmpeg-9.0.1-essentials_build\bin\ffmpeg.exe'

if (-not $IsWindows -or -not (Get-Command Start-Process).Parameters.ContainsKey('Environment')) {
    throw 'Windows PowerShell 7.4 or newer is required.'
}
if (-not $PublicOrigin.IsAbsoluteUri -or $PublicOrigin.Scheme -ne 'https' -or
    $PublicOrigin.AbsolutePath -ne '/' -or $PublicOrigin.Query -or
    $PublicOrigin.Fragment -or $PublicOrigin.UserInfo -or $PublicOrigin.IsLoopback -or
    $PublicOrigin.HostNameType -ne [System.UriHostNameType]::Dns) {
    throw 'PublicOrigin must be the expected public HTTPS DNS origin without a path, query or credentials.'
}
if ([IO.Path]::GetFullPath($PSScriptRoot) -ne $helperRoot) {
    throw 'This launcher must remain in the WI027-owned helper directory.'
}
if ($JarPath -notmatch '^[A-Za-z]:[\\/]' -or -not [IO.Path]::IsPathFullyQualified($JarPath)) {
    throw 'JarPath must be an explicit absolute local file path.'
}
$jar = [IO.Path]::GetFullPath($JarPath)
$expectedHash = $JarSha256.ToUpperInvariant()
if ($jar -ne $expectedJar) {
    throw 'Only the exact separately named ATStudio-wav128-20260909.jar is allowed.'
}
foreach ($path in @($java, $jar, $localConfig, $mailFile, $ffmpeg, $PSCommandPath)) {
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw 'A required runtime file is missing.'
    }
}
foreach ($path in @($repo, $runtime, "$repo\uploads", "$repo\private-uploads")) {
    if (-not (Test-Path -LiteralPath $path -PathType Container)) {
        throw 'A required runtime directory is missing.'
    }
}
# Reject redirected paths, including parent junctions, before trusting the fixed tuple.
foreach ($path in @($java, $jar, $localConfig, $mailFile, $ffmpeg,
        $PSCommandPath, "$repo\uploads", "$repo\private-uploads")) {
    $item = Get-Item -LiteralPath $path -Force
    while ($null -ne $item) {
        if (($item.Attributes -band [IO.FileAttributes]::ReparsePoint) -ne 0) {
            throw 'A runtime path contains a reparse point; ownership/tuple review is required.'
        }
        if ($item -is [IO.FileInfo]) { $item = $item.Directory }
        else { $item = $item.Parent }
    }
}
$currentSid = [Security.Principal.WindowsIdentity]::GetCurrent().User.Value
foreach ($path in @($runtime, $jar, $PSCommandPath)) {
    $ownerSid = (Get-Acl -LiteralPath $path).GetOwner([Security.Principal.SecurityIdentifier]).Value
    if ($ownerSid -ne $currentSid) {
        throw 'Runtime, new JAR and launcher must be owned by the launching Windows identity.'
    }
}
if ((Get-FileHash -LiteralPath $localConfig -Algorithm SHA256).Hash -ne $LocalConfigSha256.ToUpperInvariant()) {
    throw 'The reviewed root local configuration hash changed.'
}
# Accept only one literal URL in the reviewed file; never output its contents.
$configText = Get-Content -LiteralPath $localConfig -Raw -Encoding UTF8
$urlMatches = [regex]::Matches($configText, '(?m)^\s+url:\s*[''"]?(jdbc:mysql://localhost:3306/atstudio(?:\?[^\s''"#]+)?)[''"]?\s*$')
$allUrlMatches = [regex]::Matches($configText, '(?m)^\s+url:')
if ($urlMatches.Count -ne 1 -or $allUrlMatches.Count -ne 1) {
    throw 'Expected one literal localhost:3306/atstudio datasource URL in the reviewed local configuration.'
}
$databaseUrl = $urlMatches[0].Groups[1].Value
$urlParts = $databaseUrl.Split('?', 2)
$query = [System.Web.HttpUtility]::ParseQueryString($(if ($urlParts.Count -eq 2) { $urlParts[1] } else { '' }))
if (@($query.AllKeys | Where-Object { $_ -in @('user', 'password') }).Count -gt 0) {
    throw 'Datasource URL contains an unapproved credential parameter.'
}
# Strip the historical creation option only from the child CLI URL.
$query.Remove('createDatabaseIfNotExist')
$databaseUrl = $urlParts[0]
if ($query.Count -gt 0) { $databaseUrl += '?' + $query.ToString() }
$configText = $null
if ((Get-FileHash -LiteralPath $jar -Algorithm SHA256).Hash -ne $expectedHash) {
    throw 'JAR SHA256 differs from the explicitly supplied verified artifact hash.'
}
# Fail closed on inherited configuration/JVM injection; report names, never values.
$overrideNames = @(Get-ChildItem Env: | Where-Object {
    $_.Name -match '^(SPRING_|APP_|TOSS_|JWT_|MAIL_|CORS_|SERVER_|MANAGEMENT_|PAYMENT_|STORAGE_|LOADER_)' -or
    $_.Name -in @('JAVA_TOOL_OPTIONS', 'JDK_JAVA_OPTIONS', '_JAVA_OPTIONS', 'JAVA_OPTS', 'CLASSPATH')
} | Select-Object -ExpandProperty Name)
if ($overrideNames.Count -gt 0) {
    throw ('Inherited environment requires explicit MA review in a clean launcher session. Names: ' +
        (($overrideNames | Sort-Object) -join ', '))
}
function Assert-BackendPortFree {
    $listeners = @(Get-NetTCPConnection -State Listen -ErrorAction Stop |
        Where-Object LocalPort -EQ 8080)
    if ($listeners.Count -gt 0) {
        $owners = @($listeners | Select-Object -ExpandProperty OwningProcess -Unique)
        throw ('Port 8080 is occupied. MA must verify/stop only the intended backend. PIDs: ' +
            ($owners -join ', '))
    }
}
Assert-BackendPortFree
Write-Warning 'Schedulers remain enabled in source; storage recovery also runs on ApplicationReadyEvent. This launcher does not suppress jobs, SMTP startup connection checks or HTTP writes.'
if (-not $CheckOnly -and -not $RuntimeSafetyReviewed) {
    throw 'MA must record single-writer, admission, command and automatic-job safety evidence before using RuntimeSafetyReviewed. It is an acknowledgement, not a safety proof.'
}
$origin = $PublicOrigin.GetLeftPart([System.UriPartial]::Authority)
$mail = $null
$childEnvironment = @{}
try {
    # Reuse only existing SMTP keys in memory; never import its DB/provider settings.
    try { $mail = Get-Content -LiteralPath $mailFile -Raw | ConvertFrom-Json -AsHashtable -ErrorAction Stop }
    catch { throw 'Cannot parse the existing external mail configuration.' }
    if ($mail -isnot [System.Collections.IDictionary]) {
        throw 'Expected an existing mail configuration object.'
    }
    if ($mail['MAIL_HOST'] -ne 'smtp.gmail.com' -or [string]$mail['MAIL_PORT'] -ne '587') {
        throw 'Expected Gmail STARTTLS configuration was not found.'
    }
    foreach ($key in @('MAIL_USERNAME', 'MAIL_PASSWORD', 'MAIL_FROM')) {
        if ([string]::IsNullOrWhiteSpace([string]$mail[$key])) {
            throw "Missing required mail key: $key"
        }
    }
    $childEnvironment = @{
        SPRING_MAIL_HOST = [string]$mail['MAIL_HOST']
        SPRING_MAIL_PORT = [string]$mail['MAIL_PORT']
        SPRING_MAIL_USERNAME = [string]$mail['MAIL_USERNAME']
        SPRING_MAIL_PASSWORD = [string]$mail['MAIL_PASSWORD']
        APP_MAIL_FROM = [string]$mail['MAIL_FROM']
    }
    $arguments = @(
        '-jar', ('"' + $jar + '"'),
        '--spring.profiles.active=local',
        ('--spring.config.additional-location=file:' + $localConfig.Replace('\', '/')),
        '--server.address=127.0.0.1', '--server.port=8080',
        ('--spring.datasource.url=' + $databaseUrl),
        '--spring.jpa.hibernate.ddl-auto=validate', '--app.bootstrap.test-users.enabled=false',
        ('--app.audio.ffmpeg-path=' + $ffmpeg), '--app.audio.worker-enabled=true',
        ('--app.storage.public-path=' + $repo + '\uploads'),
        ('--app.storage.private-path=' + $repo + '\private-uploads'),
        '--app.storage.require-explicit-roots=true', '--app.storage.integrity.audit-on-startup=true',
        '--app.storage.integrity.strict-on-startup=false',
        ('--cors.allowed-origins=http://localhost:3000,http://localhost:5173,http://127.0.0.1:5173,http://localhost:5174,http://localhost:8080,' + $origin),
        ('--app.mail.base-url=' + $origin),
        ('--app.payment.billing.auth-success-url=' + $origin + '/subscriptions/checkout/success'),
        ('--app.payment.billing.auth-fail-url=' + $origin + '/subscriptions/checkout/fail'),
        '--spring.mail.test-connection=true',
        '--spring.mail.properties.mail.smtp.auth=true',
        '--spring.mail.properties.mail.smtp.starttls.enable=true',
        '--spring.mail.properties.mail.smtp.starttls.required=true',
        '--spring.mail.properties.mail.smtp.ssl.checkserveridentity=true',
        '--spring.mail.properties.mail.smtp.connectiontimeout=10000',
        '--spring.mail.properties.mail.smtp.timeout=10000',
        '--spring.mail.properties.mail.smtp.writetimeout=10000'
    )
    if ($CheckOnly) {
        [pscustomobject]@{
            Status = 'CHECKS_ONLY_NOT_APPLIED'; Jar = $jar; JarHash = $expectedHash
            Origin = $origin; Port8080 = 'FREE'; ExternalSecrets = 'present, not displayed'
            Database = 'localhost:3306/atstudio'; LocalConfigHash = $LocalConfigSha256.ToUpperInvariant()
            PublicRoot = "$repo\uploads"; PrivateRoot = "$repo\private-uploads"
            FfmpegPath = $ffmpeg; AudioWorkerEnabled = $true
            AutomaticJobs = 'ENABLED_IN_SOURCE_MA_DISPOSITION_REQUIRED'
            RuntimeSafetyAcknowledged = [bool]$RuntimeSafetyReviewed
        }
        return
    }
    Assert-BackendPortFree
    if ((Get-FileHash -LiteralPath $jar -Algorithm SHA256).Hash -ne $expectedHash) {
        throw 'JAR changed during preflight.'
    }
    if ((Get-FileHash -LiteralPath $localConfig -Algorithm SHA256).Hash -ne $LocalConfigSha256.ToUpperInvariant()) {
        throw 'Local configuration changed during preflight.'
    }
    $stamp = (Get-Date -Format 'yyyyMMdd-HHmmss-fff') + '-' + [Guid]::NewGuid().ToString('N')
    $stdout = Join-Path $runtime "backend-wav128-$stamp.out.log"
    $stderr = Join-Path $runtime "backend-wav128-$stamp.err.log"
    if ((Test-Path -LiteralPath $stdout) -or (Test-Path -LiteralPath $stderr)) {
        throw 'New log names must not overwrite existing runtime logs.'
    }
    $process = Start-Process -FilePath $java -ArgumentList $arguments -WorkingDirectory $repo -Environment $childEnvironment -WindowStyle Hidden -RedirectStandardOutput $stdout -RedirectStandardError $stderr -PassThru
    [pscustomobject]@{
        ProcessId = $process.Id; StartedAt = (Get-Date).ToString('o'); PublicOrigin = $origin
        Jar = $jar; JarHash = $expectedHash; WorkingDirectory = $repo
        Stdout = $stdout; Stderr = $stderr; MailHost = 'smtp.gmail.com'; MailPort = 587
        Status = 'STARTED_NOT_YET_HEALTH_CHECKED'
    }
}
finally {
    $childEnvironment.Clear()
    if ($mail -is [System.Collections.IDictionary]) { $mail.Clear() }
    $mail = $null
}
