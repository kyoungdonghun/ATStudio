---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SE
category: work-summary
status: confirmed
dependencies:
  - path: REQ-20261011-ATS-005.md
    reason: Fresh one-attempt approval
  - path: ../agent/WI-20261011-ATS-032-evidence-pack.md
    reason: Exact identities, local tests and verification limits
---

# WI032 Private Diagnostic Start Summary

> Purpose: Report the frozen candidate ready for independent review and MA-controlled execution.

## Result

TL;DR: Implementation complete locally. 88/88 mock tests passed; all eight Python files compiled in memory. MA has started PG033 independent review. Worker native execution remains NOT_RUN.

The separate [WI032 candidate and three-block recipe](../../build/WI032-private-diagnostic-start/README.md) retain WI029's 23 diagnostic steps and WI026's containment. Only fresh names, REQ005/PG033 authority and restoration of the native CLI in this new candidate changed. Old WI026/WI029 files remain unchanged; 51 historical file hashes matched.

- Bundle SHA-256: `11db69c6d82da14083864ea7e14fa239f0823e2665b5d1c3048c70edf9271dc4`
- Runtime SHA-256: `189342bef8ac90496b59efe754efbc4f846a582efd409f56e19dc8adaa16857f`
- README SHA-256: `1a70aea2249fd9a4b3783efd68b0165977d307be0b98aa8e4ddd1ebf65c0e284`

## Remaining Gates

PG033 must approve the exact bundle and three command blocks before MA uses the fresh one-attempt approval and claim. The old masked/failed WI026 unit and failed attempt remain preserved. No worker SSH, native startup, transfer, secret access, application rebuild or service operation occurred.

MA's 14:01:16 KST observation was exactly 22/23 preflight predicates true, with only the retained historical approval absence false; new WI032 absence remained a separate pending check. This observation is not native acceptance. MA owns current host gates, the approved single attempt and temporary SSH access cleanup.

The earlier app_start VERIFIED / cleanup FAILED / HOLD result remains valid and its cause remains undetermined. New diagnostics cannot recover the missing historical outcomes. A future successful run would not establish that cause. SIGKILL and host loss can prevent cleanup; any reported failure retains HOLD.

Rollback is to withhold the new candidate, not delete old evidence. After PG033, native execution belongs solely to MA; WI034 records its actual outcome.

## Related Documents

- [Evidence Pack](../agent/WI-20261011-ATS-032-evidence-pack.md): delta, reproducible tests, pins and limits.
- [Freeze](../../build/WI032-private-diagnostic-start/freeze.json): exact candidate identities.
- [Recipe pins](../../build/WI032-private-diagnostic-start/recipe-pins.json): three command block identities.
- [REQ005](REQ-20261011-ATS-005.md): approved scope and responsibilities.
