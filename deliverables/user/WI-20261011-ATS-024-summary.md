---
version: 1.1
last_updated: 2026-10-11
project: ATS
owner: SA
category: work-summary
status: stable
dependencies:
  - path: REQ-20261011-ATS-003.md
    reason: Approved bounded first-start direction
  - path: ../agent/WI-20261011-ATS-024-evidence-pack.md
    reason: Exact pins and local verification
---

# WI024 First-Start Preparation Summary

> Purpose: Report local completion and the remaining independent/native gates.

**Current revision: v2, Section 4 below. Sections 1-3 retain the v1 completion record.**

## 1. Outcome

**TL;DR: Local preparation complete. Native NOT_RUN. PG025 final review pending.**

The separate IPv4-only unit, one runtime, public configuration shape, inert tests
and exact four-file root staging/approval/invocation recipe are frozen. Original
candidates, application source, JAR and existing private configuration were not
modified. SA performed no SSH, target operation, private read or rebuild.

Bundle SHA-256:
`d9493b397847e07015e66d751e24d37815c1a528eeb569dceb934dfdb308b976`.
README SHA-256:
`3d7b2152c2d191fde60f374ea5f5df887e230902aa08564351604fa3afbf8ab7`.

## 2. Verification

At 2026-10-11 12:42:26 KST, SA on Windows/Python 3.14.3 verified:

- Owner inert tests: **41/41 PASS**.
- Unchanged PG preliminary regressions rerun by SA: **8/8 PASS**, not final independent signoff.
- Mandatory HOLD when bpftool is missing, overall five-second HTTP deadline, SIGHUP cleanup and actual tags response-object handling covered.
- Fixed diagnostic codes, one-attempt claim, no-overwrite paths and cleanup-failure HOLD retained.
- Frozen pins and original public templates checked; recipe snippets syntax-checked only.

## 3. Remaining Gate and Risks

Recommendation: PG025 reviews these exact bytes and recipe snippets, then MA alone
may perform the approved fresh target preflight and one bounded run. Any failed
gate means HOLD, not weakened guards or an automatic retry. No new architectural
decision is needed under the approved pinned-JAR threat model.

The future proof is on-host IPv4 TCP/UDP filtering, not Internet or external
IPv4+IPv6 delivery denial. Native compatibility/enforcement/startup remain
unmeasured. SIGKILL or host loss may prevent Python cleanup; MA's recovery console
must restore/verify exact app and MySQL stop/mask state when a receipt is missing.
Keep all evidence and partials. No helper changes after freeze unless PG identifies
a concrete defect requiring a recorded replacement freeze.

## 4. v2 F03 Correction

PG025 found that the final HTTP budget check touched a socket already closed by
normal body completion. The narrow fix checks elapsed/expired state without
accessing that socket; the five-second timer and pre-I/O limits remain intact.
All ten v1 files were byte-preserved in `build/WI024-private-start/history/v1/`
before editing. The README now carries exact v2 staging and approval hashes.

SA's inert rerun: **42/42 owner, 8/8 PG preliminary and 3/3 PG HTTP-lifetime PASS**.
The new regression reproduced two errors against v1 first, using real stdlib
HTTP parsing and in-memory bytes, without network activity.

Current bundle: `ae5f468e51655bedb71f997dddd18cb7ba3a50b7afcee33fdcdc5980eb960423`.
Current README: `74571c45524a93d7b888809c82e5a3dd0bbaa6995c84e894e57eded568f8881e`.
**PG025 final exact-v2 review pending; native NOT_RUN.**

## Related Documents

- [Operator recipe](../../build/WI024-private-start/README.md): Exact minimal staging, approval, invocation and recovery.
- [Evidence Pack](../agent/WI-20261011-ATS-024-evidence-pack.md): Pins, tests, limitations and next WI025 gate.
- [REQ](REQ-20261011-ATS-003.md): Approved scope.
