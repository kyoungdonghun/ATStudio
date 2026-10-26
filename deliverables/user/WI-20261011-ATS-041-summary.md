---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: docops
category: work-summary
status: confirmed
dependencies:
  - path: ../agent/WI-20261011-ATS-041-evidence-pack.md
    reason: Actual result, diagnosis and bounded validation
---

# WI-20261011-ATS-041 Summary

> Purpose: Record WI039's failed native attempt and confirmed access cleanup.

## Outcome

**TL;DR: REQ006 remains active/HOLD. MA's single WI039 attempt returned
PREPARE / DUPLICATE_JSON / HOLD at16:15:40 KST on2026-10-11. App/DB never
started. Cleanup:6 PASS,9 NOT_APPLICABLE; not a full36-step PASS.**

PG confirmed a P2 parser defect: valid multiple Nginx ExecReload commands were
rejected by r.unique, yielding a misleading JSON error code. Public contract
duplicate keys were0. Local tests missed this valid command shape. The earlier
WI040 conditional review remains historical; the native HOLD governs readiness.

App was not-found/PID0, MySQL masked/inactive/PID0 and supervisor failed/PID0.
At16:18:43 normal TLS/no-cookie/proxy/redirect HTTPS returned200 and the expected
quarantine body. No OPEN, PC HTTP assertions, application-browser navigation or
actual web restoration cycle was exercised. Old preflight22/23 remains HOLD
because the historical approval file is retained.

## Cleanup and Next Scope

MA confirmed owned key removal/no identities16:18:03, agent Stopped/Disabled
16:18:23, and user-confirmed console logout plus login-prompt screenshot16:18:30.
The recovery tab closed; provider portal retained. No required execution session
remains. Frozen candidates and previous failure/success records were preserved.

The new approval covers local-only parser repair, tests, independent review and
preflight preparation: WI042 -> WI043 -> WI044. It grants no remote/start/secret
use or another native trial; those local results are outside WI041.
No login/media/mail/payment, pristine-browser/full-traffic, load/capacity or
public-release acceptance. No new build/product purchase; existing billing continues.

DocOps validation passed:232 scoped documents,zero errors/warnings; changed
metadata,links,exact native JSON and whitespace checks passed, including untracked
outputs. Earlier index history and nine protected public hashes stayed unchanged.

## Related Documents

- [REQ006](REQ-20261011-ATS-006.md): active/HOLD and local-only amendment.
- [Evidence](../agent/WI-20261011-ATS-041-evidence-pack.md): owners, pins and document checks.
- [Receipt](../agent/WI-20261011-ATS-041-native-receipt.json): exact native result and reduced observations.
