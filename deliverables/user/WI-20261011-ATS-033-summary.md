---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: work-summary
status: confirmed
dependencies:
  - path: REQ-20261011-ATS-005.md
    reason: Fresh one-attempt authorization
  - path: ../agent/WI-20261011-ATS-033-evidence-pack.md
    reason: Exact pins, independent review and execution conditions
---

# WI033 Exact Native Candidate Gate

> Purpose: Report a conditional one-attempt gate without claiming native success.

## Verdict

**TL;DR: CONDITIONAL ALLOW for MA's one REQ005 private attempt using the exact
WI032 files and three README blocks. No confirmed code blocker. PG native NOT_RUN.**

Bundle SHA-256:
`11db69c6d82da14083864ea7e14fa239f0823e2665b5d1c3048c70edf9271dc4`

Runtime SHA-256:
`189342bef8ac90496b59efe754efbc4f846a582efd409f56e19dc8adaa16857f`

PG independently ran **88/88** frozen regressions and **7/7** new exact-delta,
recipe and mocked-entry tests: PASS. All11 freeze entries, four-file bundle,
README/three blocks,51 historical files and WI028 receipt matched. The reviewed
diagnostics and containment settings are unchanged; new namespace and native
entry match REQ005/PG033.

## Conditions and Limits

MA must refresh current target/boot/recovery, all seven attestations, exact pins,
fresh owned destinations, old WI026 masked/no-process state and private baseline
before the prescribed staging/approval/one invocation. Use exact pinned buffers
over the existing strictly pinned SSH channel. No retry, overwrite, old-unit
operation, evidence deletion or additional recovery action is authorized here.

MA's14:02:48 target observations are attributed evidence, not PG observation.
The22/23 preflight is not blanket PASS: historical approval absence remains false.
The owner completion report was not yet readable at review time; direct frozen
source, manifests and independently executed tests provide this code/recipe gate.

Any failure or missing receipt means HOLD and read-only postcheck first. Historical
WI026 stays startup VERIFIED / cleanup FAILED / HOLD; cause NOT_DETERMINED. Local
tests do not prove native restoration or Internet isolation. No remote/secret
operation was performed by PG. Next: MA's conditional attempt, then WI034.

## Related Documents

- [Evidence Pack](../agent/WI-20261011-ATS-033-evidence-pack.md): Exact block pins and MA prerequisites.
- [Test result](../../build/WI033-review/test-results.json): Independent tests and preserved identities.
- [Exact README](../../build/WI032-private-diagnostic-start/README.md): Authorized recipe bytes only.
