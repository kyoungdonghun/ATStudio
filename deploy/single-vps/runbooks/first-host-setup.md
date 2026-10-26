---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: SE
category: guide
status: draft
dependencies:
  - path: ../../../deliverables/user/REQ-20261010-ATS-007.md
    reason: Bounded first-host authorization and exclusions
  - path: ../../../deliverables/agent/WI-20261010-ATS-016-handoff.md
    reason: Local preparation only; remote execution remains with MA
  - path: ../CONFIGURATION.md
    reason: Reviewed candidate paths and security contracts
  - path: resource-baseline.md
    reason: Resource observation and escalation thresholds
---

# First-Host Access and Setup

> Purpose: Verify the first host using native Windows OpenSSH, then prepare a
> separately reviewed Ubuntu package change without losing access or exposing services.

## 1. Stop Point and Evidence Boundary

This is a local operator runbook, not an installer or a remote execution receipt.
**STOP before login until the operator authenticates the provider recovery console
and independently obtains the public Ed25519 host key and SHA-256 fingerprint.**
Missing/mismatched identity means HOLD. A reachable port or matching SSH banner
does not authenticate a host. MA owns target confirmation and subsequent approval;
WI017 must independently review this plan before any remote application.

MA-reported on 2026-10-10, not independently repeated by SE: provider reports
Ubuntu 24.04; inbound TCP22 is limited to one approved source; TCP22 was reachable.
TCP80/443/3306/8080 did not connect within 1500ms from the current Windows line.
Those probes are not independent-network, IPv6, host-firewall or application proof.
Provider outbound allows TCP80/443/53 and UDP53/123 to all IPv4 destinations, not
all ports. In particular outbound HTTPS can reach payment/mail APIs: this is
**not application egress isolation**. Do not start an application or test a Provider.
Actual recovery authentication, verified host pin, SSH login and package setup
remain pending at this handoff. Do not record raw host/account/IP/fingerprint here.
The user's latest request is a fresh access notice to their changed email, not
forwarding the previous recipient's notice. MA observed a final rebuild confirmation
for Ubuntu 24.04 on the same existing instance, displayed total0KRW, irreversible
OS/all-data deletion and delivery of rebuild/password information to the current
member email linked to service alarms. The user then explicitly confirmed no data
and approved the irreversible Ubuntu 24.04 rebuild of the existing instance at
displayed0KRW. **MA reports one confirmation click at 19:42 KST on 2026-10-10,
provider request confirmation and status rebuilding**; REQ007 contains the approval
addendum. This is attributed evidence, not SE execution or notice-delivery proof.
No recreation/new purchase occurred. Old VNC/host-fingerprint observations are now
invalidated: after rebuild completion, repeat recovery, provider address/firewall,
independent host-key and SSH checks. Do not reuse or silently replace an old pin.
SSH login and installation are NOT RUN; pending remote work is not a failed WI016.

Preserve provider SSH configuration, recovery access and the current firewall.
No broad allow rule, unapproved local service change, key regeneration, private-key export,
new secret, SQL, app start, reboot, payment, mail, backup operation or purchase is
authorized by this document. Credentials belong only in native operator prompts,
never chat, command arguments, environment variables, transcripts or screenshots.

## 2. Public Host Identity, Then Native Login

### 2.1 Obtain an Independent Public Key

In the **user-authenticated provider VNC console for the exact approved server**,
the operator runs the following read-only commands after MA confirms this scope.
The `.pub` suffix is mandatory. Never read the corresponding private host key.

```bash
ssh-keygen -l -E sha256 -f /etc/ssh/ssh_host_ed25519_key.pub
cat /etc/ssh/ssh_host_ed25519_key.pub
```

If permissions prevent reading the public file, stop for a scoped operator sudo
step. If Ed25519 is absent, stop; do not regenerate keys or change algorithms.
Keep the independently obtained fingerprint separate from any network observation.
The server host key is different from the client's existing login public key.

The installed Windows client is `OpenSSH_for_Windows_9.5p1`. MA observed
`ssh-keyscan` fail after the Ubuntu SSH banner with
`choose_kex: unsupported KEX method sntrup761x25519-sha512@openssh.com`.
This matches the [upstream keyscan issue](https://github.com/PowerShell/Win32-OpenSSH/issues/2140),
whose report distinguishes `ssh.exe` from keyscan. It is not a TCP failure.
MA's native SSH diagnostic completed protocol/key exchange and intentionally
exited 255 on unknown-host rejection with authentication disabled and no trust-file
write. Its observed fingerprint remains untrusted until the independent comparison.
No new installation, keyscan workaround, legacy KEX or trust bypass is needed.

### 2.2 Prepare a Dedicated Public Trust File

Use a separate owner-operated PowerShell 7 console without recording. Reuse the
existing encrypted operator key; do not rerun `prepare-operator-ssh.ps1` to replace it.
The previously prepared directory is `$HOME/.ssh/atstudio-iwinv`. Confirm its
owner-only ACL and absence of reparse points using metadata only. The operator
creates a **new** `known_hosts.first-host` text file there using an editor, containing
exactly one line made from the approved address and VNC public key. Do not include
the public key's trailing comment. Do not overwrite an existing file: a previous
pin requires investigation and a scoped follow-up, not removal with `ssh-keygen -R`.

```text
<exact-IP-literal> ssh-ed25519 <VNC-public-key-base64>
```

For a nondefault port use `[<exact-IP-literal>]:<port>` in the first field.
The approved current port is 22. Keep addresses and this file outside the repository.
Use a local, owner-protected file, not a shared/cloud-writable or linked location.
Copying the key is an explicit operator trust action based on the independent
console, **not** acceptance of unauthenticated `ssh-keyscan` output.

### 2.3 Validate Inputs and Prepare Arguments (No Connection)

Supply only non-secret target parameters at these prompts. This block checks the
public pin and prepares native SSH arguments. It reads **only** the public trust
file; the private key is checked for existence, never read by PowerShell. A blank
fingerprint, malformed target, wrong pin or failed fingerprint calculation stops
before connection. Keep the operator console private; do not print the argument array.

```powershell
$FirstHostSshArguments = $null
$FirstHostSshArguments = & {
    param([string]$TargetAddress, [int]$SshPort, [string]$SshUser,
          [string]$ExpectedHostFingerprint)
    Set-StrictMode -Version Latest
    $ErrorActionPreference = 'Stop'
    if ($ExpectedHostFingerprint -cnotmatch '\ASHA256:[A-Za-z0-9+/]{43}\z') {
        throw 'HOLD: independently verified SHA-256 fingerprint required.'
    }
    $parsedAddress = $null
    if (-not [Net.IPAddress]::TryParse($TargetAddress, [ref]$parsedAddress) -or
        $TargetAddress -cne $parsedAddress.ToString() -or $TargetAddress.Contains('%')) {
        throw 'HOLD: exact canonical IP literal required.'
    }
    if ($SshPort -lt 1 -or $SshPort -gt 65535 -or
        $SshUser -cnotmatch '\A[a-z_][a-z0-9_-]{0,31}\z') {
        throw 'HOLD: approved port and login account required.'
    }
    if ((Test-Path Env:SSH_ASKPASS) -or (Test-Path Env:SSH_ASKPASS_REQUIRE)) {
        throw 'HOLD: use a reviewed native-prompt console without ASKPASS.'
    }
    $keyDirectory = Join-Path $HOME '.ssh/atstudio-iwinv'
    $privateKey = Join-Path $keyDirectory 'id_ed25519'
    $knownHosts = Join-Path $keyDirectory 'known_hosts.first-host'
    if ($keyDirectory -match '["%$\r\n]' -or
        -not (Test-Path -LiteralPath $privateKey -PathType Leaf) -or
        -not (Test-Path -LiteralPath $knownHosts -PathType Leaf)) {
        throw 'HOLD: existing protected key and dedicated public pin required.'
    }
    if ((Get-Item -LiteralPath $knownHosts).Length -gt 4096) {
        throw 'HOLD: unexpected public pin size.'
    }
    $lines = @(Get-Content -LiteralPath $knownHosts)
    $hostField = if ($SshPort -eq 22) { $TargetAddress } else { "[${TargetAddress}]:$SshPort" }
    if ($lines.Count -ne 1 -or $lines[0] -cnotmatch
        ('\A' + [regex]::Escape($hostField) + ' ssh-ed25519 [A-Za-z0-9+/]+={0,2}\z')) {
        throw 'HOLD: expected exactly one target-bound Ed25519 public pin.'
    }
    $fingerprint = @(& C:\Windows\System32\OpenSSH\ssh-keygen.exe -l -E sha256 -f $knownHosts 2>$null)
    if ($LASTEXITCODE -ne 0 -or $fingerprint.Count -ne 1 -or
        $fingerprint[0] -cnotmatch '\A256 (SHA256:[A-Za-z0-9+/]{43}) .* \(ED25519\)\z' -or
        $Matches[1] -cne $ExpectedHostFingerprint) {
        throw 'HOLD: public pin fingerprint mismatch or unreadable key.'
    }
    @('-F', 'NUL', '-o', 'StrictHostKeyChecking=yes',
      '-o', ('UserKnownHostsFile="' + $knownHosts.Replace('\', '/') + '"'),
      '-o', 'GlobalKnownHostsFile=NUL', '-o', 'HostKeyAlgorithms=ssh-ed25519',
      '-o', 'VerifyHostKeyDNS=no', '-o', 'UpdateHostKeys=no',
      '-o', 'IdentityAgent=none', '-o', 'IdentitiesOnly=yes', '-o', 'AddKeysToAgent=no',
      '-o', 'ForwardAgent=no', '-o', 'ClearAllForwardings=yes',
      '-o', 'PasswordAuthentication=no', '-o', 'KbdInteractiveAuthentication=no',
      '-o', 'PreferredAuthentications=publickey', '-o', 'BatchMode=no',
      '-o', 'ConnectTimeout=10', '-o', 'ConnectionAttempts=1',
      '-i', $privateKey, '-p', "$SshPort", '-l', $SshUser, $TargetAddress)
} -TargetAddress (Read-Host 'Approved canonical server IP') `
  -SshPort ([int](Read-Host 'Approved SSH port')) `
  -SshUser (Read-Host 'Approved initial login account') `
  -ExpectedHostFingerprint (Read-Host 'Independent VNC SHA256 fingerprint')
```

### 2.4 Explicit Login and Secret Entry

Only after the preceding block succeeds and MA confirms the exact login scope,
the operator runs this in the same unrecorded, interactive console:

```powershell
if (-not $FirstHostSshArguments) { throw 'HOLD: validated arguments required.' }
& C:\Windows\System32\OpenSSH\ssh.exe @FirstHostSshArguments
```

Enter the encrypted **client key passphrase** only at the native hidden prompt.
That is not the server account password. Do not pipe or script either password.
The default path requires no ssh-agent: it reads the existing encrypted key in
native OpenSSH and uses `IdentityAgent=none`. A host verification error must stop;
do not switch to `ask`, `accept-new`, `no`, or an empty trust file for login.
No remote command is supplied: after login, remain within the authorized survey.

### 2.5 Optional Temporary Agent: Approved, Not Executed

The user subsequently approved temporary Windows ssh-agent Manual/start, native
user-only loading of the existing key and restoration to original Disabled/Stopped.
MA defers activation until independent host identity is available. The direct-key
path above requires no agent and leaves the service unchanged. No helper is added.
Provider notification-recipient checks did not find an initial-credential resend
control in the inspected pages. The latest notice request and potential
reinstall/recreation investigation remain with MA under section 1's approval gate.
Do not reset/recreate the server or reinterpret notification delivery as host identity.

After WI017 review and host verification, MA rechecks the original service state
and records only State/StartMode. If it is no longer Stopped/Disabled, stop and
reconcile ownership. The following are **planned administrator commands**, not
commands executed by WI016:

```powershell
Get-Service ssh-agent | Select-Object Status, StartType
Set-Service -Name ssh-agent -StartupType Manual
Start-Service -Name ssh-agent
Get-Service ssh-agent | Select-Object Status, StartType
```

Require Running/Manual. In a separate unrecorded console running as the key's
owning user, inspect `ssh-add -l -E sha256` and the existing `.pub` fingerprint
privately before loading. Exit1 with no identities is different from exit2 (agent
unavailable). If the intended fingerprint was already present, stop: this window
does not own it and must not remove it. Preserve all unrelated identities.

```powershell
& C:\Windows\System32\OpenSSH\ssh-keygen.exe -l -E sha256 -f "$HOME/.ssh/atstudio-iwinv/id_ed25519.pub"
& C:\Windows\System32\OpenSSH\ssh-add.exe -l -E sha256
```

Only after that absence check, the operator loads the existing key, entering its
passphrase solely in the native hidden prompt. No piping, PowerShell secret
variable, key conversion, `-D`, or key regeneration:

```powershell
& C:\Windows\System32\OpenSSH\ssh-add.exe "$HOME/.ssh/atstudio-iwinv/id_ed25519"
& C:\Windows\System32\OpenSSH\ssh-add.exe -l -E sha256
```

Record whether this window actually added the expected fingerprint, without
publishing the listing. A failed/cancelled add still requires checking whether
the owned key appeared. Service start is not login proof. For agent-assisted
SSH, MA must retain the entire validated pin/target argument set and change only
`IdentityAgent=none` to `IdentityAgent=//./pipe/openssh-ssh-agent` and `BatchMode=no`
to `BatchMode=yes`; this avoids an unattended passphrase fallback. No forwarded
agent, new trust source or host-checking change is permitted.

At completion/cancellation, the owning user removes **only** the key added in
this window, referring to its `.pub` file. Compare the remaining fingerprint
set with the pre-load set; no unrelated key may disappear:

```powershell
& C:\Windows\System32\OpenSSH\ssh-add.exe -d "$HOME/.ssh/atstudio-iwinv/id_ed25519.pub"
& C:\Windows\System32\OpenSSH\ssh-add.exe -l -E sha256
```

Require successful removal and absence of the owned fingerprint before MA's
administrator restoration below. Do not assume stopping the Windows agent erases
stored identities. If removal/verification fails, cleanup is incomplete: notify
MA immediately and resolve exact-key removal, never use `ssh-add -D`. If the
operator console closes unexpectedly, MA owns this same explicit cleanup.

```powershell
Stop-Service -Name ssh-agent
Set-Service -Name ssh-agent -StartupType Disabled
Get-Service ssh-agent | Select-Object Status, StartType
```

Require Stopped/Disabled and record both key-removal and service-restoration
outcomes. The temporary authorization ends with that receipt; no background
loader, persistent startup, new installation or remote operation is implied.

## 3. Fresh-Host Survey Before Changes

Keep recovery console access and the initial SSH session available. Obtain only
the following bounded observations in the authorized session; report sanitized
values/status, not full environment, network/account listings or logs.

```bash
cat /etc/os-release
uname -r
dpkg --print-architecture
nproc
free -b
df -B1 /
df -i /
timedatectl show -p Timezone -p NTPSynchronized
systemctl is-active ssh.service ssh.socket nginx.service mysql.service
systemctl is-enabled ssh.service ssh.socket nginx.service mysql.service
sudo ss -lnt
dpkg-query -W openjdk-17-jre-headless mysql-server nginx ffmpeg
```

Absent packages/units may return nonzero; distinguish absent from failed.
`ss` output includes addresses: inspect privately and report only port/bind class.
Do not dump `systemctl ... Environment`, cloud-init user-data, credential files,
private keys, MySQL client option files, SSH private configuration or raw journals.
Provider image/user-data may contain secrets. Inspect public configuration only
with the relevant scope; preserve original files/metadata externally before any
approved change. Do not overwrite SSH, MySQL, journald, APT or provider defaults.

Before installation, MA must also establish package-manager/cloud-init completion,
existing service masks/drop-ins, provider restart/reboot behavior, existing data
directory presence (metadata only), configuration include paths and disk headroom.
Any existing DB/data/service changes the fresh-host assumption: HOLD, no purge,
SQL, directory cleanup or guessed configuration overwrite. Review RAM below 7GiB,
free-space/reserve shortfall or unexpected services against the resource baseline.

## 4. Bounded Package Plan (Not Executed)

After identity and survey, resolve versions/provenance from the target's existing
official Ubuntu 24.04 Noble repositories. Do not add repositories, PPAs, installers,
Git tools or SSH libraries. Missing candidates require review, not automatic enablement.
Minimal packages: `openjdk-17-jre-headless` (prebuilt app runtime, no server build),
`nginx`, `ffmpeg`; `mysql-server` is a **separate held step** below.

Read-only planning commands against the current package cache:

```bash
apt-cache policy openjdk-17-jre-headless nginx ffmpeg mysql-server
apt-get --simulate --no-install-recommends install openjdk-17-jre-headless nginx ffmpeg
apt-get --simulate --no-install-recommends install mysql-server
```

MA reviews actual origin, versions, dependency changes, disk/network cost and
maintainer-script effects. `apt-get update` is a later approved network/cache write,
not part of this survey; rerun policy/simulation after it. No blanket upgrade,
`-y`, conffile overwrite, SSH/systemd replacement or auto-reboot is included.

Before a later approved Nginx install, verify it is absent/inactive, that the
prospective mask path is absent including symlinks, and preserve any existing
provider policy. Review package scripts and establish a persistent service-start
inhibit first. A candidate sequence for the independently reviewed change is:

```bash
sudo systemctl mask nginx.service
systemctl is-enabled nginx.service
sudo env NEEDRESTART_MODE=l apt-get install --no-install-recommends openjdk-17-jre-headless nginx ffmpeg
systemctl is-enabled nginx.service
systemctl is-active nginx.service
sudo ss -lnt
```

Execute one command at a time only after approval; require `masked` before apt.
No `--force` or existing-unit replacement. `NEEDRESTART_MODE=l` requests list-only
restart handling, not suppression of every maintainer-script effect. If package
behavior cannot be bounded, HOLD. Keep the mask after installation; do not unmask,
start or enable Nginx, and do not serve the package's default site. The provider's
closed web ingress is defense in depth, not proof that nothing started locally.
An unexpected listener/start is a failed change requiring MA response, not PASS.

**MySQL gate:** Ubuntu documents automatic server start during installation.
Distribution installation can also initialize its data directory/system tables
and local administrative account. This is distinct from an ATStudio schema but
is not silently covered by the current DB/schema/user exclusion. Hold installation
until MA/user explicitly resolves that effect. A systemd mask alone does not prove
maintainer scripts will avoid direct initialization or temporary server execution.
No `mysqld --initialize`, `mysql_secure_installation`, SQL, user/grant creation,
dump/import, data deletion or app database creation is supplied here.

## 5. Separate Hardening and Activation Gates

After installation evidence and separate exact-change review:

| Area | Guarded next step |
|---|---|
| SSH / firewall | Preserve provider config, approved source and initial session. Prove recovery and a second authorized sudo/key session before proposed root/password restrictions. Validate effective SSH configuration, including socket activation, before any approved reload. No blanket UFW enable/reset or firewall flush. Recheck both sessions and recovery afterward. |
| MySQL | Resolve initialization gate first. Verify actual include order and existing data/config; stage and compare the existing `mysql/atstudio.cnf` candidate without overwriting provider files. Review loopback3306, X Plugin OFF, 512MiB buffer pool and max30. Starting/validation and any SQL require their own exact scope. Check IPv4/IPv6 listeners and independent external reachability. |
| Journald / clock | Review host-wide impact and existing retention/forwarders before `journald/atstudio.conf`; its limits may age out existing logs. No vacuum/deletion. Stage a distinct reviewed drop-in only after retention approval. Observe NTP first; separately approve Asia/Seoul/time changes and their scheduler effects. |
| Identity / directories | Use `CONFIGURATION.md`'s nonlogin `atstudio` service identity and disjoint private/public/temp paths. Verify absence/ownership first; approve exact new paths/account and modes. No recursive chown/chmod, existing identity reuse or provider-file overwrite. Do not create secrets or install/start the app unit. |
| Verification | Check `java -version`, `ffmpeg -version`, and `ffmpeg -hide_banner -encoders` for `libmp3lame`; inspect package versions and service/port state. These are existence/capability observations, not encode/load tests. Do not install JDK21 as a silent substitute for Java17. |
| Domain / TLS | Track the requested domain purchase with MA before DNS/HTTPS. Purchase, DNS writes, ACME issuance and opening80/443 need separate authorization; do not deploy HTTP bootstrap/default web content while waiting. |

Applying the application environment or Hikari10 requires a newly built/hashed
reviewed artifact with active admission, not the unchanged old JAR. No app startup,
real SMTP/payment, restic repository or backup hook operation is part of this plan.

## 6. Receipt and Recovery

Record stage, date/time, operator/evidence owner, exact approved target reference
(private), reviewed source/artifact hashes, sanitized outputs/exit status and
remaining gate. Keep MA-attributed network evidence, SE local checks and actual
target execution separate. Do not claim production readiness from a syntax check.
Preserve baseline and failed-attempt evidence. Before remote adoption, rollback
means withholding this plan. After any later approved change, retain recovery and
use only its approved inverse steps; never purge packages/data, erase host pins,
unmask/start services or restore over newer configuration automatically.

## Related Documents

- [WI016 evidence](../../../deliverables/agent/WI-20261010-ATS-016-evidence-pack.md): Local tests and limits.
- [Configuration](../CONFIGURATION.md): Ownership, templates and source contracts.
- [Deployment](deployment.md): DB, secrets, runtime and release gates.
- [Resource baseline](resource-baseline.md): Measured-versus-planned capacity.
- [Security policy](../../../docs/policies/security-policy.md): Sensitive-data boundaries.
- [OpenSSH client options](https://man.openbsd.org/ssh_config): Strict checking, dedicated host file and native identity options.
- [Ubuntu MySQL installation](https://ubuntu.com/server/docs/how-to/databases/install-mysql/): Package auto-start and initial administrative account.
- [Windows native key authentication](https://learn.microsoft.com/en-us/windows-server/administration/openssh/openssh_keymanagement): Passphrase and optional agent handling.
