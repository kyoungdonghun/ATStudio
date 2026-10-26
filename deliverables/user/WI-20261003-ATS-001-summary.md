---
version: 1.1
last_updated: 2026-10-03
project: ATS
owner: SE
category: work-summary
status: stable
dependencies:
  - path: REQ-20261003-ATS-001.md
    reason: Approved pre-purchase requirement
  - path: ../agent/WI-20261003-ATS-001-evidence-pack.md
    reason: Bounded checks and reproducible evidence
---

# WI-20261003-ATS-001 Summary

> Purpose: Deliver single-VPS configuration candidates for independent validation.

## Result

**Template delivery complete; production HOLD.** No purchase, installation,
runtime restart, DB operation, secret inspection, email or payment call occurred.

- Added nginx HTTP-only bootstrap, restricted TLS host, common proxy and default
  deny-all ACL. Java remains the only `/api` and `/uploads` gateway.
- Added one-JVM systemd, external placeholder-only environment, loopback MySQL
  and bounded host-journal candidates. Exact paths remain the approved interface.
- Added [configuration guidance](../../deploy/single-vps/CONFIGURATION.md) and
  [machine-readable contract](../../deploy/single-vps/env/contract.json) for WI003.
- Mandatory local billing encryption keyring is separate from blank Toss Provider
  credentials. No Provider subscription or activation is required by that keyring.
- Existing Gmail, original/128kbps MP3 and phone TEST evidence is preserved.

## Verification And Limits

SE verification, 2026-10-03, Windows/PowerShell, source `main` / `58b1011`:

| Check | Result |
|---|---|
| JSON/env contract | PASS: 11 paths, 73 keys, 36 safe literals, 12 empty values, 11 placeholder tokens |
| Proxy/unit/ACL text assertions | PASS: 18 assertions |
| Protected/public path fixtures | PASS: 6 denied + 4 unaffected path fixtures, .NET regex only |
| Configuration text hygiene / local links | PASS: 11 text files, 15 local links before deliverable creation |
| Final owned-file / document check | PASS: 13 files, 3 metadata-complete documents, 24 local links |
| Linux nginx/systemd/MySQL/FFmpeg and actual HTTP | NOT RUN; tools unavailable, none installed |
| Runtime, DB, SMTP, TLS, firewall, restore, capacity | NOT RUN; require exact-target approval |

Resource limits are provisional, not measured support capacity. Disabled
notifications do not disable schedulers. Initial egress isolation and a selected
auth/mail configuration remain target gates. Query-free access logs are not
full redaction: nginx request error logs are discarded, and external/OS/app
logging still needs target verification.

## Next Work And Rollback

WI003 can consume the completed contract. WI004 can review this delivery and
WI002's runbook; independent QA and Phase A closeout remain downstream.
No agents were spawned by this scoped implementation.

Before installation, rollback means withdrawing only these owned additions;
preserve other agents' files. After a separately approved deployment, use the
documented validated config/release rollback, never DB/media/key deletion.

## WI004 Correction Addendum (2026-10-03)

PG004-F01/F02 corrections delivered for independent re-review: quoted media
regex now covers complete ID segments and optional matrix suffixes; SPA fallback
adds local `X-Frame-Options DENY always`. Existing API/media limits, upstream and
no-retry are unchanged. Contract adds `nginx.media_location_pattern` and
`nginx.spa_response_headers` without changing schema version 1 or env keys.
SE static regression: PASS, 48 media + 6 non-media fixtures, quoted-pattern
contract agreement and three location-local `always` headers. Native HTTP,
framework matrix acceptance and browser framing remain NOT RUN. Only HTTPS
template, contract, CONFIGURATION and these two deliverables changed in follow-up.
WI003 regression and WI004 security disposition remain independently owned.

## Related Documents

- [REQ](REQ-20261003-ATS-001.md): Scope and approval.
- [Evidence Pack](../agent/WI-20261003-ATS-001-evidence-pack.md): All changed files, commands, risks and boundaries.
- [Configuration](../../deploy/single-vps/CONFIGURATION.md): File destinations and later target sequence.
