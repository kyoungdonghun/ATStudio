---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SE
category: work-summary
status: confirmed
dependencies:
  - path: ../agent/WI-20261011-ATS-048-evidence-pack.md
    reason: Exact scope, tests, corrections and immutable pins
---

# WI048 HTTPS Continuation Preparation

> Purpose: Report the minimal local continuation and its remaining independent review gate.

**TL;DR: Local candidate complete. Use README-v2.md only. Native NOT_RUN; PG049
must review exact v2 bytes before MA performs the approved HTTPS-only continuation.**

## Outcome

- Only Nginx After dependency-token order is normalized. Added, removed or
  duplicated tokens, other properties and reordered Exec commands still fail.
- The already detached files are verified and left untouched. A new exclusive
  record permits at most one original Nginx start; app/MySQL remain stopped/masked.
- Uncertain verification or receipt durability triggers one stop and all safety
  checks. No retry, old receipt edits, secret reads or native worker actions.
- 21 focused tests and 23 inherited regressions pass. Recipe paths resolve to the
  candidate's exact hash; both executable blocks pass syntax checks.

## Revision and Identity

MA found v1's incorrect local staging path before native use. v2 corrects that
path; Python and L2 remain unchanged. v1 files and pins are preserved as history.

Candidate: `2d617dc5a09b67a3024f628b90f6465bcac29bd8cce4ac852a0360d75484765a`.
v2 bundle: `b3befcd2a2310ef8c7461800ce35f75f2aad918eb479de12f480433c55fcacfe`.

Local mocks do not prove actual recovery. The earlier POST_STATE_CHANGED cause
remains undetermined; the separate After-order false positive was observed by MA.
No DB data-equality, browser or new app-start claim is made. Host loss/SIGKILL may
prevent cleanup. Next: PG049 final review, MA-only recovery, WI045 integration.

## Related Documents

- [Active Recipe](../../build/WI048-quarantine-continuation/README-v2.md): Two exact MA blocks.
- [Active Freeze](../../build/WI048-quarantine-continuation/freeze-v2.json): All exact identities.
- [Evidence Pack](../agent/WI-20261011-ATS-048-evidence-pack.md): Tests and limitations.
