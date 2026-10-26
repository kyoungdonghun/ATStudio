---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: SE
category: work-summary
status: active
dependencies:
  - path: REQ-20261010-ATS-006.md
    reason: Approved local implementation boundary
  - path: ../agent/WI-20261010-ATS-012-evidence-pack.md
    reason: Reproducible verification and changed-file hashes
---

# WI-20261010-ATS-012: Deployment Candidate Implementation

## Outcome

Local implementation is complete and ready for WI014 independent review.
Production remains HOLD; target installation and performance are NOT RUN.

- JVM: Xms256m, Xmx1024m, metaspace256m; application cgroup MemoryHigh2G/MemoryMax3G.
- MySQL: 512M buffer pool, unchanged 30 connections. Hikari: maximum10, minimum2, 5000ms acquisition timeout.
- Environment contract: admission enabled, global2/IP1/Retry-After2s; multipart spool threshold0B; SMTP5000/10000/10000ms.
- Nginx: general8/IP32global, shared media4/IP16global, original download1/IP2global, stream256k/download2m. The media child explicitly composes all parent and class-specific connection zones.
- Multipart admission uses Content-Type maps for all paths, methods and authentication states, including malformed-boundary requests. It is a proxy defense in depth, not a replacement for WI011 application admission.
- Independent structural checks supplement pinned template identities. Security defaults, protected routes, trusted-header overwrite, controller delivery, no-cache/no-buffering behavior and invitation deny-all remain preserved.

## Verification

Owner: SE; 2026-10-10; Windows local workspace; Python3.14.3; synthetic fixtures only.

- Focused unittest suite: 39 tests PASS, including original regressions and negative fixtures with deliberately reapproved hashes.
- Template-audit CLI: PASS; deployment_ready=false; production_status=HOLD; seven unresolved target gates retained.
- Eleven explicit source property bindings are checked at their actual YAML mapping paths, including concurrent WI011 changes. This is a static source check, not running Spring configuration binding.
- Original proxy, bootstrap and invitation-ACL files retain identical SHA-256 values.
- Coverage percentages: NOT MEASURED; the coverage module is unavailable and no installation was attempted.

No services, DB, real backup, remote host, secrets, purchases, external sends or Git commands were used. Test-owned temporary fixtures are the only test filesystem mutations; shared product files were not edited by the tests.

## Remaining Gates

WI014 must independently review WI011, WI012 and WI013 together. Native Nginx syntax and phase ordering, real multipart/auth-failure pressure, effective Linux cgroups/MySQL settings, media throughput and supported user counts remain unverified. Hikari10 alone is not a proof against starvation or nested-transaction deadlock. WI015 owns current-entry documentation integration; historical evidence remains immutable.

## Related Documents

- [Evidence Pack](../agent/WI-20261010-ATS-012-evidence-pack.md)
- [Approved Handoff](../agent/WI-20261010-ATS-012-handoff.md)
- [Resource Baseline](../../deploy/single-vps/runbooks/resource-baseline.md)
- [REQ006](REQ-20261010-ATS-006.md)
