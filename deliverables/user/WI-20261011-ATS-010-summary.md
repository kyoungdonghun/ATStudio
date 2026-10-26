---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: work-summary
status: confirmed
dependencies:
  - path: REQ-20261011-ATS-001.md
    reason: Approved correction and independent re-review
  - path: ../agent/WI-20261011-ATS-010-evidence-pack.md
    reason: Exact identities, findings, tests and native conditions
---

# WI010 Corrected Probe Review

> Purpose: State the bounded independent verdict for the corrected probe and operator.

## Verdict

**TL;DR: scoped PASS for exact v4 plus operator revision 3. Native NOT_RUN.**

The original real-validator type failure is corrected. Independent focused tests passed 21/21, including the actual validator against existing compiled v4, missing-table/column rejection and original v3 failure reproduction. Offline mapping smoke also passed. No candidate rebuild or real connection was used.

The original procedure's cleanup exit-code defect and two failure-reporting defects are corrected in revision 3. Eight cleanup mocks and three reporting mocks passed. No remaining material blocker was found within this review.

## Exact Approved Pair

- Candidate: `build/WI009-schema-v4`, bundle `f2e59839caa096fff83b427ad47563be8f7b96c58bdd2825bda9216e109eaeec`.
- Operator: [ma-native-operator-revision3.py](../../build/WI010-pg-20261011-0210/ma-native-operator-revision3.py).
- Operator SHA-256: `85384f2c118119e96e2166f8456448dcf16ad5a915798b0be211db4f246b5189`.

MA must copy and hash-check those exact bytes, repeat fresh target/capture/identity/permission gates, verify the final plan and Linux offline smoke, and then use the separately approved read-only lifecycle. Exact pinned old-helper `load_bundle` and read-only guard/grant/observation reuse is acceptable; historical approvals supply identity only.

MySQL cleanup, process-group clearance and unchanged restricted TLS must be verified even on failure. Recovery-console fallback remains required for uncatchable termination or connection loss. Any failure is HOLD; no automatic retry or DB change is approved. The actual Linux/DB outcome is MA-owned and is not proven by these local mocks.

## Limits and Handoff

Old candidate/procedure history remains intact. No implementation, private config, DB, SSH, network, browser, dependency, commit or push operation was performed. This is not app-startup, canonical schema, capacity or public-deployment approval. MA proceeds to WI008 with the actual result or HOLD and cleanup evidence.

## Related Documents

- [Evidence Pack](../agent/WI-20261011-ATS-010-evidence-pack.md): Findings, source/procedure pins, reproduction and conditions.
- [Handoff](../agent/WI-20261011-ATS-010-handoff.md): Approved scope.
