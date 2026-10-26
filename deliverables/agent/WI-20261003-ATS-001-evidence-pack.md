---
version: 1.1
last_updated: 2026-10-03
project: ATS
owner: SE
category: evidence-pack
status: stable
dependencies:
  - path: WI-20261003-ATS-001-handoff.md
    reason: Scoped ownership and required output contract
  - path: ../user/REQ-20261003-ATS-001.md
    reason: Approved pre-purchase scope
  - path: ../../deploy/single-vps/CONFIGURATION.md
    reason: Delivered configuration interface and target gates
---

# Evidence Pack: WI-20261003-ATS-001

## Summary

Delivered minimal single-host Linux candidates, source/env mappings and a
machine-readable contract; no target application or production verification.

## Scope / DoD Check

- [x] Owned only the assigned deployment directories, CONFIGURATION and two deliverables.
- [x] HTTP bootstrap / TLS / reusable proxy / deny-all ACL, one foreground JVM,
  placeholder env, loopback MySQL and bounded journal candidates created.
- [x] Routes preserve URI and Range; no direct storage alias. Header allowlist
  prevents forwarded-identity propagation and preserves Idempotency-Key.
- [x] Strict roots/integrity, production guard, no test bootstrap, Swagger off,
  explicit origins and mandatory local billing keyring mapped to source.
- [x] Contract supplied for WI003; assumptions and later target checks explicit.
- [x] Static validation, two-set outputs and rollback boundary recorded.
- [ ] Independent security/QA are downstream, not self-certified here.

## Verification Identity

| Field | Evidence |
|---|---|
| Verification date | 2026-10-03 |
| Environment | Existing Windows/PowerShell shared checkout, no Linux execution |
| Evidence owner | SE, this WI |
| Source branch / HEAD | `main` / `58b1011dad796017e47dbcc82a4225ca449bcdf6` |
| Working tree | Existing dirty AGENTS/CLAUDE/README/docs/reconstruction and earlier deliverables preserved; concurrent WI002/MA work is outside ownership |
| Document edit date | 2026-10-03; does not advance historical runtime evidence dates |
| Result | Bounded local static PASS; target runtime NOT RUN; production HOLD |

## Reference Documents (Tier 0-2)

Tier 0 and security content were injected by MA in the prompt before this
implementation. The supplied approved REQ/handoff authorizes direct SE execution;
no agents were spawned. The on-disk handoff was read and its existence verified.

| Tier | Input pointer | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` (STD-001) | Approved scope, communication and side-effect gates |
| 0 | `docs/standards/documentation-standards.md` (STD-004) | English documents, metadata, relative links |
| 0 | `docs/standards/development-standards.md` (STD-002) | Scoped edits, two-set evidence, traceability |
| 0 | `docs/standards/glossary.md` (STD-005) | Public Listening, Official Download, storage terminology |
| 1 | `docs/policies/security-policy.md` | Injected proxy, secrets, storage and logging rules |
| 1 | `docs/policies/quality-gates.md` | Read; independent review remains WI004/WI005 |
| 1 | `.claude/agents/se.md` | Read assignee context; current injected standards supersede stale phase labels |
| 2 | `docs/index.md#current-decision-entry` | Read current evidence table and source/target distinction |
| 2 | `docs/design/runtime-storage-operations.md` | Read strict root/tuple and single-worker/audio sections |
| 2 | `docs/SR/SR-93.md#remaining-production-gates` | Read current target HOLD boundaries |
| 2 | `.agents/skills/create-wi-evidence-pack/SKILL.md` | Read and used to create this file |
| 2 | `.agents/skills/validate-docs/SKILL.md` | Read; bounded owned-file checks, not repository-wide validation |

Context ordering followed the supplied Tier 0 -> security/task context -> source
snapshot. No generated injection bundle or automatic context-loader execution
is claimed. `.claude/config/workspace.json` confirms project tag `ATS`.
Memory was navigation only; current entry/source drove configuration decisions.

## Evidence Pointers

All files below are **new owned additions**, not changes to product source:

| File | Purpose |
|---|---|
| `deploy/single-vps/nginx/nginx.conf.template` | Complete candidate, minimal query-free syslog format, resource zones and error-log boundary |
| `deploy/single-vps/nginx/http-bootstrap.conf.template` | Token-only ACME bootstrap without application/cert dependency |
| `deploy/single-vps/nginx/https.conf.template` | Fixed host, reject defaults, inherited ACL, media/private-path boundaries, SPA |
| `deploy/single-vps/nginx/proxy.conf.template` | Full URI proxy, explicit request header allowlist, unbuffered no-retry/no-cache transfer |
| `deploy/single-vps/nginx/restricted-access.conf` | Empty approval list means deny all |
| `deploy/single-vps/systemd/atstudio.service` | Single JVM/control group, read-only release, writable data/temp, bounded resources |
| `deploy/single-vps/env/atstudio.env.example` | 73 explicit keys, placeholders only for required target values, Providers blank |
| `deploy/single-vps/env/contract.json` | Schema version 1, 11 required paths, required keys/literals/placeholder obligations/roots |
| `deploy/single-vps/mysql/atstudio.cnf` | Loopback SQL, X Plugin off, no grants/DDL/secrets |
| `deploy/single-vps/journald/atstudio.conf` | Host-wide size/age limits and forwarding boundary |
| `deploy/single-vps/CONFIGURATION.md` | Exact install mapping, source contract, deployment order, holds and official references |
| `deliverables/user/WI-20261003-ATS-001-summary.md` | User-facing result and limitations |
| `deliverables/agent/WI-20261003-ATS-001-evidence-pack.md` | This bounded evidence |

Key source evidence, read only:

- `src/main/resources/application.yml`: storage, mail, auth, audio, rate limit,
  payment and 100MB/120MB multipart mappings.
- `config/AcceptanceStartupGuard.java:68`: unconditional billing keyring
  validation, before non-acceptance early return.
- `service/payment/billing/BillingKeyCrypto.java:72`: required active ID and
  matching configured keyring. This is independent of Toss activation.
- `config/JwtConfig.java:24`: Base64 decoded key must be at least 32 bytes.
- `config/StorageIntegrityStartupGuard.java:31`: prod requires strict audit.
- `config/WebConfig.java:21`, `config/SecurityConfig.java:90`: static public-root
  mapping and existing private-path denies; nginx never bypasses Java.
- `security/TrustedClientIdentityResolver.java:28`: loopback peer trust and
  single validated internal IP header.
- `controller/TrackController.java:121`: Public Listening Range contract.
- `service/NoticeService.java:176`: public attachment controller reads PRIVATE;
  `/uploads/notices` denial does not block `/api/notices/.../attachments/...`.
- `service/SubscriptionScheduler.java:59`: recurring scheduled work is not
  disabled by the reconciliation notification flag.
- `frontend/src/api/client.ts:15`: same-origin `/api`; Vite is not deployed as a
  server. No existing `application-prod.yml` is assumed or added.

Abbreviated Java paths above begin at `src/main/java/com/atstudio/atstudio/`.

## Commands & Outputs

Read-only source context: `git status --short --branch`, `git rev-parse HEAD`,
targeted `rg -n`, `Get-Content` of the handoff, skills, public config and source.
Two initial rg calls used nonexistent/glob-as-literal paths and returned exit 1;
subsequent exact-path searches resolved them. No setup or runtime was attempted.

Edits used only `apply_patch`. The following local PowerShell checks completed:

| Check | Observed result |
|---|---|
| Contract JSON, path/key completeness, duplicate keys, exact safe values, blank Provider keys, token coverage and matching billing IDs | PASS: 11 paths, 73 env keys, 36 literals, 12 empty keys, 11 tokens |
| Proxy/ACL/unit text assertions | PASS: 18 |
| Protected vs intended public path regex fixtures | PASS: 6 denied + 4 unaffected |
| ASCII/trailing-whitespace/final-newline check | PASS: 11 contract paths |
| CONFIGURATION local Markdown target existence | PASS: 15 local links; no anchor assertion |
| Final owned-file text / document metadata / local-link check | PASS: 13 owned files, 3 documents with seven required metadata fields, 24 local links; anchors not asserted |
| `git diff --check -- deploy/single-vps deliverables/user/WI-20261003-ATS-001-summary.md deliverables/agent/WI-20261003-ATS-001-evidence-pack.md` | Exit 0; untracked file coverage comes from the separate text check, not this Git command |

Official nginx proxy/connection/access/log/core/SSL and MySQL X Plugin pages
were consulted; exact relevant links are in CONFIGURATION. Several freedesktop
systemd pages returned HTTP 403 and one MySQL page timed out. Those failures are
not successful documentation or native-tool validation. Selected distro man
pages and actual validators must resolve platform compatibility on target.

## Test Evidence And Reproduction

No project build/test suite or target process was launched: this WI changes
configuration candidates only. WI003 owns the reusable offline validator and
positive/negative tests; WI004 owns independent security review. The local
checks read **only checked-in candidates**, never a rendered/private env file.

Compact reproduction from repository root (PowerShell; throws on failure):

```powershell
$ErrorActionPreference = 'Stop'
$c = Get-Content -Raw deploy/single-vps/env/contract.json | ConvertFrom-Json
$e = @{}
foreach ($line in Get-Content deploy/single-vps/env/atstudio.env.example) {
    if ($line -match '^([A-Z][A-Z0-9_]*)=(.*)$') {
        if ($e.ContainsKey($Matches[1])) { throw 'Duplicate key' }
        $e[$Matches[1]] = $Matches[2].Trim('"')
    }
}
foreach ($p in $c.required_template_paths) {
    if (-not (Test-Path -LiteralPath $p)) { throw 'Missing path' }
    $text = Get-Content -Raw -LiteralPath $p
    if ($text -match '(?m)[ \t]+\r?$|[^\x00-\x7F]' -or -not $text.EndsWith("`n")) {
        throw 'Text hygiene failure'
    }
}
foreach ($k in $c.required_env) { if (-not $e.ContainsKey($k)) { throw 'Missing key' } }
foreach ($k in $e.Keys) { if ($k -notin $c.required_env) { throw 'Extra key' } }
foreach ($p in $c.safe_literals.PSObject.Properties) {
    if ($e[$p.Name] -cne $p.Value) { throw 'Literal mismatch' }
}
foreach ($k in $c.must_be_empty) { if ($e[$k] -ne '') { throw 'Expected empty key' } }
$all = ($c.required_template_paths | Where-Object { $_ -notmatch 'contract.json|CONFIGURATION.md' } |
    ForEach-Object { Get-Content -Raw -LiteralPath $_ }) -join "`n"
foreach ($p in $c.required_placeholders.PSObject.Properties) {
    if (-not $all.Contains($p.Name)) { throw 'Missing placeholder' }
    foreach ($k in $p.Value.env) { if (-not $e[$k].Contains($p.Name)) { throw 'Wrong placeholder' } }
}
$tokens = [regex]::Matches($all, '__[A-Z0-9_]+__') | ForEach-Object Value | Sort-Object -Unique
foreach ($t in $tokens) {
    if ($t -notin $c.required_placeholders.PSObject.Properties.Name) { throw 'Extra placeholder' }
}
if ($e['PAYMENT_BILLING_KEY_ACTIVE_KEY_ID'] -cne $e['PAYMENT_BILLING_KEY_0_ID']) {
    throw 'Keyring mismatch'
}
'PASS: candidate contract only'
```

Path-fixture reproduction (this is **.NET regex**, not nginx/PCRE execution or
normalization/authorization proof):

```powershell
$s = Get-Content -Raw deploy/single-vps/nginx/https.conf.template
$p = [regex]::Match($s, 'location ~\* (?<pattern>\^/uploads/[^\r\n]+) \{').Groups['pattern'].Value
if (-not $p) { throw 'Missing protected-path pattern' }
$denied = @('/uploads/.staging/item', '/uploads/tracks/audio/item.wav',
    '/UPLOADS/TRACKS/AUDIO/item.mp3', '/uploads/company-docs/item',
    '/uploads/questions/attachments/item', '/uploads/notices/attachments/item')
$allowed = @('/api/notices/1/attachments/2', '/uploads/albums/image.jpg',
    '/uploads/playlists/image.jpg', '/uploads/tracks/thumbnails/image.jpg')
foreach ($v in $denied) { if (-not [regex]::IsMatch($v, $p, 'IgnoreCase')) { throw 'Deny failed' } }
foreach ($v in $allowed) { if ([regex]::IsMatch($v, $p, 'IgnoreCase')) { throw 'Public path hidden' } }
'PASS: 10 literal route fixtures only'
```

No coverage metric applies to native behavior from these text assertions.
Native `nginx -t`, `systemd-analyze verify`, MySQL config/startup, FFmpeg/encoder,
actual HTTP/TLS/range/upload tests, restart/restore/reboot and load: **NOT RUN**.
MA reported unavailable nginx/bash/docker and no runnable WSL Linux environment;
none was installed, downloaded or started here.

## Risks / Rollback

- All numerical settings are provisional. A capacity run must record effective
  config/version/artifact identity and measure memory, disk/temp, latency, errors,
  queues and transfer; no supported-user or cost-cap claim is made.
- `MAIL_PORT` and other target placeholders intentionally prevent a usable
  deployment as supplied. WI003 template PASS is not deployment readiness.
- Empty ACL gates application content but not token-only HTTP-01, redirects or
  error returns. Certificates/ACME, firewall/SSH and IP trust need target proof.
- Notification-off is not scheduler-off; isolate external network before an
  approved first start/restore. The unit is single-instance by operational rule,
  not a distributed lock preventing another manually launched process.
- nginx error logs are discarded, not redacted. App/driver/OS/external log
  capture remains unverified; journal bounds may drop logs. Diagnostics and
  alerting need the target gate and WI002 runbook.
- MySQL service name/include paths, nginx worker user, certificates, permissions,
  Java 17 binary and FFmpeg/libmp3lame remain target duties.
- Before target application, withdraw only the owned additions listed above.
  Preserve all concurrent work. After approved application, restore only prior
  reviewed config/release with validated schema/media compatibility and no
  overlapping JVM; never delete DB/media/keys as rollback. No commit/push made.

## Follow-ups

WI003 is unblocked by this contract. WI004 may review templates once WI002 is
available. MA owns downstream dispatch and later WI005/WI006; this SE task does
not close the REQ, invent a Linux PASS, spawn agents or change other handoffs.

## WI004 Correction Addendum (2026-10-03)

**Delivered for independent re-review; PG004 security closure is not claimed.**
Authority: reopened WI001 handoff follow-up and
[WI004 findings](WI-20261003-ATS-004-evidence-pack.md). Owner/environment:
SE, 2026-10-03, same Windows/PowerShell checkout. Initial findings/checks above
remain historical evidence. No product, private config, runtime or DB change.

- **PG004-F01:** replace numeric-only matching with the quoted pattern
  `^/api(?:;[^/]*)?/tracks(?:;[^/]*)?/[^/]+/(stream|download)(?:;[^/]*)?/?$`.
  It covers complete ID segments and optional matrix suffixes on fixed segments;
  literal semicolons are inside nginx quotes. Tighter matching is an edge
  resource policy, not permission for the backend to accept matrix/invalid IDs.
  Source search found no custom HttpFirewall or path-matching override. Actual
  servlet/firewall normalization and matrix acceptance remain NOT RUN.
- **PG004-F02:** add `X-Frame-Options DENY always` inside the SPA `location /`
  beside existing headers and fallback. No broad CSP or child-frame restriction
  is added. [Official header inheritance](https://nginx.org/en/docs/http/ngx_http_headers_module.html#add_header)
  was checked; the header is intentionally location-local.
- **Interface:** additive `nginx.media_location_pattern` and
  `nginx.spa_response_headers` in contract schema version 1. Existing env keys,
  paths, API/media limits, proxy include, upstream and no-retry are unchanged.

Changed in this follow-up: `deploy/single-vps/nginx/https.conf.template`,
`deploy/single-vps/env/contract.json`, `deploy/single-vps/CONFIGURATION.md`,
this Evidence Pack and `deliverables/user/WI-20261003-ATS-001-summary.md` only.

| Bounded check | Result |
|---|---|
| Before edit | Canonical media matched; `+1`, `0x1`, action matrix suffix and frame-deny checks were false |
| After edit, media fixtures | PASS: 4 ID forms (`1`, `+1`, `0x1`, decoded `#1`) x 2 actions x 6 path variants = 48 |
| Unrelated route fixtures | PASS: 6 non-media paths do not match |
| Template/contract | PASS: quoted regex equals contract pattern |
| Preserved controls | PASS: five media directives, shared API limiter and no-retry unchanged |
| SPA | PASS: fallback retained; all three contract headers are local and use `always` |
| Follow-up files / docs | PASS: five text files, three metadata-complete documents, 25 local link targets; scoped `git diff --check` exit 0 (untracked content checked separately) |
| Native nginx/HTTP/browser | NOT RUN; static .NET regex/text checks are not platform or framing proof |

Compact fixture reproduction (repository-root PowerShell):

```powershell
$h = Get-Content -Raw deploy/single-vps/nginx/https.conf.template
$c = Get-Content -Raw deploy/single-vps/env/contract.json | ConvertFrom-Json
$p = [regex]::Match($h, 'location ~ "(?<p>\^/api[^"\r\n]+)" \{').Groups['p'].Value
if (-not $p -or $p -cne $c.nginx.media_location_pattern) { throw 'Quoted pattern mismatch' }
foreach ($id in @('1','+1','0x1','#1')) {
    foreach ($a in @('stream','download')) {
        foreach ($v in @("/api/tracks/$id/$a", "/api/tracks/$id/$a/",
            "/api/tracks/${id};v=1/$a", "/api/tracks/$id/${a};v=1",
            "/api;v=1/tracks;v=2/$id/$a", "/api;v=1/tracks;v=2/${id};v=3/${a};v=4/")) {
            if (-not [regex]::IsMatch($v,$p)) { throw 'Media fixture missed' }
        }
    }
}
foreach ($v in @('/api/tracks/1','/api/tracks//stream','/api/tracks/1/stream/more',
    '/api/tracks/1/downloadable','/api/tracks/1/streams','/other/api/tracks/1/download')) {
    if ([regex]::IsMatch($v,$p)) { throw 'Non-media overmatch' }
}
$spa = [regex]::Match($h,'location / \{(?<body>[^{}]*try_files[^{}]*)\}').Groups['body'].Value
foreach ($x in $c.nginx.spa_response_headers.PSObject.Properties) {
    if (-not $spa.Contains("add_header $($x.Name) $($x.Value) always;")) { throw 'SPA header missing' }
}
'PASS: 54 route fixtures and location-local headers, static only'
```

Corrected candidate SHA-256 (`Get-FileHash -Algorithm SHA256 -LiteralPath @(...)`):

- HTTPS: `f69cad94887742e4f220d8e8fd198a9e89a5ea32637121dc1f1a4d8fd74dc2ca`
- Contract: `ecd1e4936403ef3b2ea71a7fecc4585bfcc1b5afcdb97624d4d3a92948686ed7`

The first hash invocation used two positional paths and failed; the explicit
`-LiteralPath` array call above succeeded. No file writes came from these checks.
Rollback remains scoped to these edits; returning to the earlier candidate
reopens F01/F02 and is not a security-approved alternative. WI003 consumes the
additive interface and WI004 owns re-review; no agents were spawned here.

## Related Documents

- [Handoff](WI-20261003-ATS-001-handoff.md): Approved execution scope.
- [REQ](../user/REQ-20261003-ATS-001.md): Staged goal and remaining approvals.
- [Configuration](../../deploy/single-vps/CONFIGURATION.md): Target interface and official references.
- [User summary](../user/WI-20261003-ATS-001-summary.md): Concise result.
