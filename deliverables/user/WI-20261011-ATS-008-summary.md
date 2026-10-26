---
version: 1.1
last_updated: 2026-10-11
project: ATS
owner: DocOps
category: work-summary
status: confirmed
dependencies:
  - path: ../agent/WI-20261011-ATS-008-evidence-pack.md
    reason: Exact v5 local PASS, preserved native HOLD and cleanup evidence
---

# WI-20261011-ATS-008: Release Verification Integration

> Purpose: Integrate exact v5 local acceptance while preserving the stopped native attempt and future gates.

## Current Result

TL;DR: exact v5 local correction/review PASS; native NOT_RUN; production HOLD.
WI008 documentation integration is complete. REQ001 remains approved/active
pending separate target authority, exact operator review and native validation.

Candidate `build/WI011-schema-v5`, bundle
`463c95667ee51e5fe2f01303a43ccebe0c0547375a374730c511c924212c7e54`.
SE011 verified55/55 local tests through02:30 KST; PG012 independently verified
26/26 on exact prepared v5 at02:33 KST, Windows/PowerShell, Java17.0.12.
The full83-key public renderer fixture accepts the exact classpath:/ line in
v5 and reproduces v4 ENV_KEYS; external/import/other overrides remain rejected.
PG's previous29/29 JDBC evidence is reused, not part of its new26-test count.

## Preserved Native Attempt

MA's 2026-10-11 02:21:59 KST receipt records temporary loopback MySQL activation
and GateError at APPROVED_ENV_VALIDATION, before schema approval or child launch.
The exit0 operator result is HOLD, not schema PASS. Before-baseline equality is
inferred from reaching that phase; no after-observation ran.

The public bootstrap requires/preserves `SPRING_CONFIG_LOCATION="classpath:/"`,
while the pinned v4 schema helper rejected all `SPRING_CONFIG_` settings. MA/PG's 02:22
follow-up withdraws v4 execution-readiness PASS to HOLD. The DB-off diagnostic
at02:23:04 confirms only ENV_KEYS; the public incompatibility is the likely
trigger, not an observed private offending line. No private body/value/hash
was imported. WI010's original review remains unchanged historical evidence.

## Verified Boundaries

- SE009's 50/50 and PG010's 21/21 plus cleanup/reporting mocks remain bounded
  local successes. Original v3 Integer failure and subsequent fixes are preserved.
- Backend638/frontend336/SPA143 recorded comparisons matched; four exact
  historical frontend coverage exclusions remain, not full source equivalence.
  MA's root stage has178 public files/113653320 bytes and Linux mapping smoke
  passed before the attempt. Exact v4/operator pins are in the Evidence Pack.
- Operator stop0/mask0, owned process group clear and TLS unchanged were
  independently rechecked by MA at02:22:46. Root tty1=0 at02:23:26; exact key
  removed, empty agent checked, then fresh Windows Stopped/Disabled around02:24.
  No DocOps native/browser/secret access or repeated test occurred.
- Restricted Nginx/renewal timer remain active/enabled; app absent and MySQL
  inactive/masked. No schema/data/grant/password changes, purchase/install or
  application publication. NS/alerts/unattended renewal/reboot remain unresolved
  or unverified; canonical fresh manifest UNRECORDED and app_runnable=false.

## Remaining Gates

The user's latest direct approval covered only exact `classpath:/` allowlisting,
continued external/import/other-config rejection, a full public-bootstrap
synthetic fixture regression, a fresh candidate and independent review.
WI-20261011-ATS-011(SE) -> WI-20261011-ATS-012(PG) -> WI008 integration is handled.
No new remote/DB/SSH-agent operation followed the existing cleanup. No v5
operator or retry is approved; old rev3 and approvals are invalid for v5.
Next: fresh access/secret-use approval, independently reviewed exact v5 operator
and fresh target/capture/identity/TLS gates, then read-only native schema check.
App startup remains excluded; never change private env to bypass the guard.

## Documentation Checks

Four-file Tier0-presence/internal-file-link validation passed,60 supported
traceability IDs,0 errors/0 warnings. Direct metadata/dependency/newline/
whitespace checks include untracked deliverables; scoped git diff --check passed.
The known whole-validator false positive remains separate, not a global PASS.
Final02:38 KST rerun passed the same four-file scope and60 IDs,0 errors/warnings.
All23 public/history pins and the earlier native receipt text were preserved.

## Related Documents

- [WI011 evidence](../agent/WI-20261011-ATS-011-evidence-pack.md): Exact v5 correction and SE tests.
- [WI012 review](../agent/WI-20261011-ATS-012-evidence-pack.md): Local scoped PASS, not operator approval.
- [Evidence Pack](../agent/WI-20261011-ATS-008-evidence-pack.md): Receipts, findings, pins and documentation validation.
- [Approved REQ](REQ-20261011-ATS-001.md): Current DoD and correction chain.
- [Current entry](../../docs/index.md#current-decision-entry): Latest checkpoint with preserved history.
