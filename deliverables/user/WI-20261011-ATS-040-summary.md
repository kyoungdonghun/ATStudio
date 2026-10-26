---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: work-summary
status: confirmed
dependencies:
  - path: REQ-20261011-ATS-006.md
    reason: Approved repair and fresh one-attempt scope
  - path: ../agent/WI-20261011-ATS-040-evidence-pack.md
    reason: Exact identity, tests and execution conditions
---

# WI040 Independent Review

> Purpose: Report the bounded WI039 safety gate before MA's single management-only trial.

**TL;DR: CONDITIONAL ALLOW for the exact frozen WI039 candidate.** No new blocking defect found. This completes local review, not native acceptance or public release.

## Verified

- 26 frozen files,11 recipe bodies,42 preserved historical pins and external QA helper match.
- 95/95 owner Python regressions,9/9 independent PG tests and8/8 PowerShell synthetic assertions PASS. Four PowerShell bodies parse; no recipe/native/network execution.
- Missing Exec properties are handled only in the appropriate stage. Required scalars, malformed/duplicate/unknown properties and unsafe states remain rejected.
- Mocked unmask timeout, loaded-gate failure and MySQL start timeout reach separate cleanup. Single/multiple cleanup failures continue through all36 checks, remain HOLD and retain startup evidence.

## Exact Gate

Runtime bundle: `fb643ff04e5c0f1e90f1b110eafa312c311a3d560a822769ff43f5ed432d379b`.

Review set: `1a19015eb8e890b02d6973331dfb2ef7fd7458821694e929ba425cd1ab3c1572`.

Freeze: `e29624f41ccfd51f43005721d05e1e619bf38cac459f5281707c9dd9530f772e`.

MA must use the pinned README/eleven blocks and verified System32 SSH with current strict host pin/options, redacted transport errors and the pinned QA identity helper. Recheck current artifacts, host/boot/peer, management firewall, logged-in recovery, masks, fresh paths and all eleven approval attestations. MA reported renewed root recovery and unchanged provider rules at16:07KST; PG did not observe the server.

Only one fresh WI039 approval/claim/start is permitted. Installed vendor values were measured; loaded systemctl rendering remains unmeasured and must fail closed before MySQL start if different. No predicate relaxation or automatic retry.

## Remaining Limits

The retained P2 deactivating-status false failure needs no code change: preserve intermediate receipts and use only existing bounded S5 observation until the authoritative final result, followed by S7. A true final failure or missing completion stays HOLD; later clean state cannot erase it.

Browser context is retained, not proven pristine. Fixed HTTP checks and visible browser observations are separate evidence; full network absence, populated media/login, capacity and release readiness are not proven. Original failed attempts remain unchanged.

**Next:** MA's fresh gated one-attempt trial, then WI041 actual-evidence closure. WI039 native remains NOT_RUN at this review.

## Related Documents

- [WI040 evidence](../agent/WI-20261011-ATS-040-evidence-pack.md): exact file/block hashes, commands and mandatory conditions.
- [Test receipt](../../build/WI040-restricted-web-review/test-receipt.json): independent local outcomes.
- [WI039 README](../../build/WI039-restricted-web-check/README.md): only reviewed execution recipe.
