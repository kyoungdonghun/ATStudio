---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SE
category: guide
status: draft
dependencies:
  - path: ../../../deliverables/user/REQ-20261010-ATS-010.md
    reason: Approved apex-only quarantine scope and separate CA consent
  - path: ../../../deliverables/agent/WI-20261011-ATS-001-handoff.md
    reason: Local preparation and independent PG gate
  - path: first-host-setup.md
    reason: Strict host pin, native operator secret entry and exact-key cleanup
---

# Restricted Domain and TLS

> Purpose: Give MA a bounded DNS/HTTP-01/TLS sequence without exposing or starting ATStudio.

## 1. Boundary and Fresh Gates

TL;DR: candidate only. WI002 independent PG PASS for these exact bytes precedes
application. Run one stage at a time in MA's existing strict-pinned native root
session. Never execute this document as one script. SE made no target connection.
No app proxy, private config read, MySQL start, app start, reboot, new paid service,
www/wildcard/AAAA, host-firewall replacement, commit or push. Any drift is HOLD.

MA observations, 2026-10-11 00:04-00:05 KST and subsequent same-window update:
Ubuntu24.04/Nginx1.24.0; current distro `nginx -t` PASS; strict pin, atadmin sudo0
and SSH source matching provider allowlist; MySQL/Nginx masked and inactive;
app unit absent, Java/FFmpeg0, only SSH22 plus loopback DNS listeners. UFW inactive,
nft tables absent. `/etc/letsencrypt` and `/etc/atstudio/{nginx,tls}` absent;
existing `/var/lib/atstudio/acme` root:root0755 is preserved and unused here.
NTP yes; dpkg audit/locks clear; sshd syntax PASS; sudo listing found no capture
flags, which is not a full capture audit. Root-signed Ubuntu sources observed;
package provenance/install still pending. DNS zone empty, authoritative SOA refused
and recursive SERVFAIL; console draft is not DNS publication. These are attributed
MA facts, not SE verification or permission to skip fresh gates.
MA additionally confirmed authenticated root noVNC recovery at00:07 KST.

Before changes, MA checks same host/boot/current source, retained authenticated
recovery console and fresh administrator login. Confirm no session recording,
debug tracing, private-input capture or unexpected collection before user contact
entry. Keep app absent, MySQL exact `/dev/null` mask and inactive, no Java/FFmpeg,
no queued starts or 8080/3306/33060/33062 listeners throughout. No SQL is needed.
Preserve SSH source and all existing egress rules. Verify IPv6 separately: no AAAA,
no IPv6 80/443 provider allow rule, and later no IPv6 Nginx socket. No host rules
are introduced. Independent non-allowlisted HTTPS denial is a required receipt.

Create a fresh root0700 `/root/wi20261011-001` after checking absence INCLUDING
dangling links. Verify real root-owned ancestry, no group/other write, unexpected
ACL/default ACL, bind-mount alias, symlink or hardlinked file at every new target.
Stage the seven implementation files listed in the Evidence Pack with repository
relative paths; match SHA-256 after transfer. No secrets or application artifacts.
Back up only relevant public Nginx main/unit/drop-in configurations to NEW files
in that stage; preserve modes, hashes and timer/mask/firewall/DNS baseline privately.
No overwrite, recursive chown, default-site deletion or old snapshot replacement.

## 2. Packages and Service Selection

Recheck package locks, signed official Noble origins, architecture, exact versions,
available disk and simulation. The observed plan is seven NEW packages, zero
upgrades/removals: certbot, python3-acme, python3-certbot, python3-configargparse,
python3-josepy, python3-parsedatetime, python3-rfc3339. Observed certbot/acme version
is2.9.0-1; resolve/pin every dependency version from the current signed index.

```bash
apt-get --simulate --no-install-recommends install certbot=2.9.0-1
```

MA reviews the resolved seven-package version/origin/hash set and downloaded
maintainer scripts, triggers, service/timer/cron files before the matching install.
Use official apt only, no PPA/snap/pip or web installer. A plan/origin change needs
review, not an automatic retry/upgrade. Preserve existing apt state; no index
refresh implied. Download-only is not install approval or installed provenance.
After verifying new certbot units/masks are absent, prevent package auto-start:

```bash
systemctl mask --runtime certbot.service certbot.timer
```

Require both exact runtime masks; existing MySQL/Nginx masks remain. Then MA runs
the approved exact-version `apt-get --no-install-recommends install` for that
reviewed set, with native confirmation, not a blind `-y`. Check dpkg status and
new timer/cron schedule without running renewal. Package scripts must not start
other services; any unexpected start is HOLD and the masked-state recovery below.

```bash
/usr/bin/certbot --version
/usr/bin/certbot --help all
/usr/sbin/nginx -V
```

Require target2.9 help for `register`, `certonly`, `--dry-run`, `--run-deploy-hooks`,
`--cert-name`, `--webroot`, `--webroot-path`, `--preferred-challenges`, `--server`,
`--force-interactive`, `--no-eff-email` and standard hook directories. Current
online5.8 docs do not prove target2.9 flags. Confirm SSL/rewrite/access/static
modules, OpenSSL TLS1.2/1.3 and Linux `disable_symlinks` support. No dynamic module
includes are used. Unknown required module or warning is HOLD, not a reinstall.

The installed native unit ordinarily selects `/etc/nginx/nginx.conf`. This WI
leaves that file and all distro sites intact and adds only the supplied
`nginx-service.conf` as `/etc/systemd/system/nginx.service.d/90-atstudio-quarantine.conf`.
All four start/test/reload commands explicitly select the quarantine config.
Before unmasking, inspect the vendor unit and ALL effective drop-ins: root master,
www-data worker, `/run/nginx.pid`, compatible Stop/Type/PIDFile, no alternate
environment/ExecStartPost/ExecReload/namespace/root directory or later override.
No package default is accepted merely because a drop-in exists.

## 3. Bootstrap and Synthetic Challenge

In the verified public staging tree, render with target Python3 stdlib. Read the
approved public client privately; never record its value in tracked evidence.
The renderer has no network/install/write mode. Its stdout contains that client
and belongs only in protected MA staging, not a shared transcript.

```bash
cd /root/wi20261011-001
umask 077
set -C
read -r -p 'Approved current IPv4/32: ' CLIENT
python3 -I -B scripts/deployment/tls-quarantine.py --client "$CLIENT" --phase bootstrap > bootstrap.conf
python3 -I -B scripts/deployment/tls-quarantine.py --client "$CLIENT" --phase tls > tls.conf
unset CLIENT
```

Require both exit0/nonempty files; noclobber stays enabled. A partial failed render
is retained, never installed/reused; choose a separately reviewed new stage name.
The renderer requires the exact apex and canonical public IPv4/32, not proof of
approval/ownership. MA must compare the value to the actual SSH source/provider
rule, not a forwarded header or a guessed address.

After absence/ancestry/ACL checks, create only these dedicated root-owned paths:

```bash
mkdir -m 0755 /etc/nginx/tls-quarantine
mkdir -m 0755 /var/lib/atstudio-tls-quarantine
mkdir -m 0755 /var/lib/atstudio-tls-quarantine/acme
mkdir -m 0755 /var/lib/atstudio-tls-quarantine/acme/.well-known
mkdir -m 0755 /var/lib/atstudio-tls-quarantine/acme/.well-known/acme-challenge
mkdir -m 0755 /var/lib/atstudio-tls-quarantine/marker
printf '%s\n' 'ATStudio TLS quarantine only' > /var/lib/atstudio-tls-quarantine/marker/ready.txt
chmod 0444 /var/lib/atstudio-tls-quarantine/marker/ready.txt
install -o root -g root -m 0644 bootstrap.conf /etc/nginx/tls-quarantine/nginx.conf
```

Every destination must still be absent immediately before `install`; it can
otherwise overwrite. Marker and challenge files must be regular/single-link;
no copied app files, symlinks, hardlinks or worker-writable directories. Linux
`disable_symlinks on` adds per-request checks but cannot identify a hardlink.
Certbot alone writes new challenge tokens as root; preserve its normal cleanup.

Create the service drop-in directory only if absent; if present, inventory it
without altering its owner/mode. Refuse an existing target drop-in including a
dangling link. Install the reviewed source root:root0644, then:

```bash
systemctl daemon-reload
/usr/sbin/nginx -t -q -c /etc/nginx/tls-quarantine/nginx.conf
systemctl unmask nginx.service
systemctl show nginx.service -p FragmentPath -p DropInPaths -p ExecStartPre -p ExecStart -p ExecReload
```

Require exit0 and exact effective quarantine selection. Re-mask on any failure;
never start the distro default. No nginx enable/preset here. With recovery ready,
MA may `timeout 60s systemctl start nginx.service`, then require active/root-master
and IPv4 port80 only, no IPv6/app/DB listeners. Timeout leaves possible jobs:
submit bounded stop and verify quiescence, not an automatic retry.

MA publishes the verified empty zone and ONE apex A to the approved target,
retaining NS/registrar settings; add no www, CNAME, AAAA or unreviewed CAA/DS change.
If authority remains REFUSED/SERVFAIL, HOLD issuance; do not change DNSSEC/NS by
guessing. Require authoritative SOA/NS/A and independent recursive A agreement,
AAAA absence and no CAA constraint preventing issuance. Inspect target privately.
Provider firewall: preserve exact SSH22 source and existing egress; add TCP80
public IPv4 and TCP443 only that same approved IPv4/32. Recheck actual rules and
fresh SSH access after each change. No broad443, IPv6 rule or host-firewall reset.

Create one public synthetic token (noclobber still enabled):

```bash
printf '%s\n' 'WI001 synthetic challenge' > /var/lib/atstudio-tls-quarantine/acme/.well-known/acme-challenge/WI001-probe
chmod 0644 /var/lib/atstudio-tls-quarantine/acme/.well-known/acme-challenge/WI001-probe
```

Bounded external native curl, no browser: exact HTTP token returns200 with that
body; missing token404; `/`, `/api/tracks`, `/uploads/tracks/audio/x`, dot paths,
query-bearing token, percent-encoded/traversal token return404; unknown Host is
closed (444 internally); POST token403. Use `--path-as-is`, `--max-time 10` and
no redirect following/cookies/auth. Limit the combined acceptance set to24
synthetic requests, stop on unexpected success. Retain only outcomes. This
synthetic challenge test creates no ACME account and is NOT CA staging success.

## 4. CA Consent, Staging, Production

**STOP for explicit action-time user CA agreement/contact consent before ANY
registration, including staging. No such approval exists at this handoff.** State
that public certificate transparency discloses the apex. One approval may name
staging, production and renewal together; do not ask the user to approve the same
covered operation twice. Reconfirm only an uncovered scope or changed agreement.

Default contact path: the user chooses/enters the actual contact in an unrecorded
native prompt, after capture-surface checks. Do not invent a contact, pass email
in argv, pre-accept terms or agree to third-party marketing.

Bounded alternative: the user may explicitly approve the exact current LE
Subscriber Agreement v1.8, staging/production/renewal, and issuance without a
contact address or contact sharing. MA must verify the agreement version at the
action time. Only AFTER that consent may MA review exact noninteractive no-email
arguments, including any required ToS flag, against installed Certbot2.9 help.
They must preserve the same domain, endpoints, webroot, hook and exposure gates.
This document supplies no pre-accepted noninteractive command and grants no
consent. The commands below illustrate only the default interactive contact path;
do not run them as a substitute for the separately reviewed no-email invocation.

Check default Certbot CLI configuration locations and hooks are absent/known,
with no injected server/authenticator/pre/post/deploy hooks. Do not read account
keys/private config bodies. New `/etc/letsencrypt` and standard work/log paths
must have protected ancestry; keep account/log directories root-only and private
keys root0600. Retain Certbot logs locally under restricted access; they are NOT
sanitized evidence and may contain contact/ACME material. Never upload their bodies.

First register a staging account interactively with explicit consent. This is
deliberate: Certbot2.9 dry-run can internally enable ToS/no-email registration
when other accounts exist. Never rely on dry-run to create an account implicitly.

```bash
/usr/bin/certbot register --force-interactive --no-eff-email --server https://acme-staging-v02.api.letsencrypt.org/directory
/usr/bin/certbot certonly --dry-run --webroot --webroot-path /var/lib/atstudio-tls-quarantine/acme --preferred-challenges http --cert-name atmusic.co.kr -d atmusic.co.kr --server https://acme-staging-v02.api.letsencrypt.org/directory
```

Before the dry-run, MA requires successful staging registration and protected
account-directory metadata for that endpoint; no raw account ID in evidence.
Keep the timer/service runtime-masked. No deploy hook is installed yet. Require
exit0 and staging challenge PASS; no certificate is installed for this test.
Failure means HOLD, no production retries. Renewal dry-run later must reuse this
staging account, not silently register another account or accept changed terms.

Then check that the action-time approval covers production agreement/contact
scope. Ask only if it was not already included, before:

```bash
/usr/bin/certbot certonly --force-interactive --no-eff-email --webroot --webroot-path /var/lib/atstudio-tls-quarantine/acme --preferred-challenges http --cert-name atmusic.co.kr -d atmusic.co.kr --server https://acme-v02.api.letsencrypt.org/directory
```

Require the exact standard lineage, not `-0001`; exact single apex SAN, trusted
chain, dates and same-generation fullchain/privkey links. No staging certificate
is served. Public certificate inspection is allowed; key body/output/copy is not.
The Nginx root master reads the protected lineage directly. Do not grant www-data
key access or relax live/archive0700 and private-key0600 to make a test pass.

## 5. Restricted TLS and Renewal Proof

Check `tls.conf` with `/usr/sbin/nginx -t -q -c /root/wi20261011-001/tls.conf`.
This tests the actual certificate/key pair without printing it. Hash the active
bootstrap file and require equality with the reviewed render. Preserve it under
a NEW stage name before installing TLS; these are only this WI's new files:

```bash
test ! -e /root/wi20261011-001/bootstrap.installed && test ! -L /root/wi20261011-001/bootstrap.installed
mv -T /etc/nginx/tls-quarantine/nginx.conf /root/wi20261011-001/bootstrap.installed
install -o root -g root -m 0644 /root/wi20261011-001/tls.conf /etc/nginx/tls-quarantine/nginx.conf
/usr/sbin/nginx -t -q -c /etc/nginx/tls-quarantine/nginx.conf
timeout 60s systemctl reload nginx.service
```

Stop on ANY failed command; do not continue the block. Observe new worker/config
selection and IPv4-only80/443. Reload exit0 alone is not served TLS proof.
From the approved line, trusted native curl to `https://atmusic.co.kr/ready.txt`
must return200 with the synthetic marker and expected safe headers. Never use
`-k`. Root/app/media/storage paths remain404 even for that line; no redirect.
Check wrong Host, missing Host, unknown/no SNI (TLS rejection), query/encoded/
traversal marker and POST. Loopback curl with correct SNI/Host must return403 for
the marker EVEN with forged X-Forwarded-For/X-Real-IP claiming the approved source.
Example: `curl --resolve atmusic.co.kr:443:127.0.0.1 --max-time 10 -o /dev/null -w '%{http_code}\n' https://atmusic.co.kr/ready.txt`.
Compare independent non-allowlisted network denial with provider rules; a loopback
403 is not external firewall proof. Record IPv6 no-listener/no-rule/no-AAAA separately.

Verify Certbot-created hook ancestry/ACLs, and no other pre/post/deploy hooks or
saved renewal hook commands; require this new deploy filename absent. Install:

```bash
install -o root -g root -m 0750 deploy/single-vps/tls-quarantine/reload-hook.py /etc/letsencrypt/renewal-hooks/deploy/40-atstudio-quarantine
env RENEWED_DOMAINS=atmusic.co.kr RENEWED_LINEAGE=/etc/letsencrypt/live/atmusic.co.kr /etc/letsencrypt/renewal-hooks/deploy/40-atstudio-quarantine
/usr/bin/certbot renew --cert-name atmusic.co.kr --dry-run --run-deploy-hooks
```

Require fixed hook success, native reload/active, dry-run exit0, and a second
independent trusted TLS/marker read with the expected served certificate
fingerprint/dates. Target2.9 `--run-deploy-hooks` uses the active production
certificate, not the temporary test one. A Certbot exit0 can coexist with a hook
failure: independently require the hook receipt and served result. No forced
production renewal merely to manufacture a different serial. This proves a
staging renewal plus reload of the current certificate, not future unattended
renewal, a changed production certificate, expiry alerts or application readiness.

Before scheduling, inspect installed certbot.service/timer AND package cron;
require exactly one effective scheduler, no extra lineages, trusted command/hooks,
known next trigger and no saved staging endpoint for the production lineage.
Review only safe selected settings, not full private renewal/account files.
After acceptance, remove only this WI's two runtime masks and enable/start the
reviewed timer (`systemctl unmask --runtime certbot.service certbot.timer`, then
`systemctl enable --now certbot.timer`). Nginx ongoing quarantine/boot enablement
must be recorded explicitly by MA; if continuity is not accepted, keep scheduling
masked, stop/remask Nginx and report renewal NOT_OPERATIONAL. No reboot test here.
HTTP80 token path, DNS and Nginx availability are renewal prerequisites.

Check actual timer next/last trigger, certbot.service Result/ExecMainStatus, hook
result and certificate expiry; do not assume a healthy timer means successful
renewal. Public-certificate local expiry threshold check:

```bash
/usr/bin/openssl x509 -in /etc/letsencrypt/live/atmusic.co.kr/fullchain.pem -noout -enddate -checkend 2592000
```

Nonzero/under30days or missed renewal requires MA attention, not blind retries.
Let's Encrypt ended expiration-email service in2025; a contact address is NOT
expiry monitoring. Independent alert owner/recipient/transport and synthetic
delivery remain separately approved and unimplemented. Record this operational
gap explicitly even if TLS/renewal checks pass. Nginx request logs are disabled;
hook logs contain fixed status only. Loss of detailed request diagnostics is
intentional. OS/Certbot/provider capture still needs separate inspection.

## 6. HOLD and Closure

Before activation: leave Nginx/MySQL masked; preserve all candidates. After any
unexpected disclosure/failed gate, close ONLY the newly added provider80/443 rules
while preserving SSH and egress. Stop this window's certbot timer if started and
restore its prior disabled/masked state; do not interrupt an unknown ACME writer.
For Nginx use `systemctl stop --no-block nginx.service`, then bounded observation
(up to60s) requiring inactive, no pending start/reload and no80/443 listeners.
Only then `systemctl mask nginx.service` and confirm the exact `/dev/null` mask.
If quiescence cannot be established, retain recovery access and escalate; never
claim cleanup or automatically kill processes. MySQL stays masked/inactive.

Retain generated certs/accounts/keys, tokens, configs, packages, DNS and evidence;
no automatic delete, revocation, downgrade or DNS rollback. If withdrawing the
new drop-in is needed, move only it to a verified-absent root-stage name while
Nginx is stopped/masked, then daemon-reload and recheck. Never start a restored
distro default. Restoring generated bootstrap/TLS files requires exact old/new
hash comparison and a fresh no-overwrite destination; preserve failures.

MA records dated owner/environment, source/render/install hashes, consent scope,
DNS/rule checks, native parsing, allowed/denied probes, certificate validity,
renewal/hook receipts and remaining alerts/boot limitations. Read no app env or
DB credentials. End by removing ONLY this window's SSH agent key, verify empty
agent before Stopped/Disabled restoration, and close all sessions. WI002 is the
immediate next dependency; after its PASS and MA execution/cleanup, WI003 DocOps
integrates receipts. Local PASS is not production approval or REQ completion.

## Related Documents

- [WI001 Evidence Pack](../../../deliverables/agent/WI-20261011-ATS-001-evidence-pack.md): Exact candidate identities and local limits.
- [Current entry](../../../docs/index.md#current-decision-entry): Latest completed host evidence.
- [Production readiness](production-readiness.md): Separate app/DB/SMTP/backup/alerts/load gates.
- [Nginx SSL](https://nginx.org/en/docs/http/ngx_http_ssl_module.html#ssl_reject_handshake): Default SNI rejection and certificate loading.
- [Nginx symlink control](https://nginx.org/en/docs/http/ngx_http_core_module.html#disable_symlinks): Per-request path checks.
- [Certbot2.9 CLI](https://github.com/certbot/certbot/blob/v2.9.0/certbot/certbot/_internal/cli/__init__.py): Target-version flag semantics, subject to native help.
- [Certbot2.9 dry-run handling](https://github.com/certbot/certbot/blob/v2.9.0/certbot/certbot/_internal/cli/cli_utils.py): Internal staging-registration behavior requiring the prior account gate.
- [LE expiration emails](https://letsencrypt.org/docs/expiration-emails/): Ended service; never an alert guarantee.
