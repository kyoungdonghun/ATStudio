---
version: 1.0
last_updated: 2026-10-21
project: ATS
owner: PG
category: reference
status: active
---
# Evidence Pack: WI-20261021-ATS-007

## Summary
Exact frozen v8 review is HOLD_FIX_REQUIRED for PG-V8-001. No native approval receipt issued.

## Context
WI007 handoff, WI006 evidence, retained Tier0 instructions, security-policy, test and create-wi-evidence-pack skills. Review is local and bounded; no source/candidate edits, remote/private access or purchases.

## Evidence
Machine-readable receipt: `build/WI071-review/pg-dns-v8-review-20261021.json`. All three requested freeze hashes and every manifest member matched independently (01d71c).

## Finding
`build/WI067-auth-trial-v8/binding/native.py:980` calls verify_restore before the new unarmed early return. That function (line892) does not verify quarantine file hashes, unlike restore_quarantine. Service metadata and listener equality do not prove configuration bytes. A PREFLIGHT failure caused by changed quarantine files can therefore become cleanup PASS. Require exact quarantine hashes in the new branch and a regression with unchanged service/listener/env state and altered quarantine bytes. Do not relabel the original trial HOLD.

## Independent Tests
- DNS/cleanup5, binding72, delivery29 and producer12 Python tests PASS (ef2201).
- Java17.0.12 actual SpringApplication/YAML/JSON/CLI session probes: NO_TLS PASS (4e5e1f), UNTRUSTED PASS (af09b9), HOST_MISMATCH PASS (6f06e6), all before AUTH on loopback fake SMTP.
- Existing tests passing do not cover PG-V8-001: the early-return test mocks verify_restore instead of checking quarantine drift.

## Other Review Results
Exact bounded hosts mapping and launch TLS overrides are consistent with the proposed architecture. Synthetic JSON deliberately disables TLS; actual CLI properties restore all three values in Session. Hostname remains smtp.gmail.com. Existing v7 JSON already enforced TLS, so this is not evidence of a prior TLS vulnerability.

Delivery README has stale v7 heading/count/link wording; nonblocking, not the execution guide. WI006 probe README and manifests are authoritative for this review. Avoid cosmetic freeze churn.

## Follow-up / Limits
SE repairs only PG-V8-001 with regression and freezes a new exact candidate. PG delta-review then decides allowance. Actual target Java17 resolution and no-auth STARTTLS with system CA and hostname verification remain mandatory before human readiness. Native transfer/handshake exact invocation is not yet reviewed. Preserve prior freezes and reviews; no automatic native retries.

## R1 Superseding Local Decision
The original HOLD above remains historical evidence. R1 resolves PG-V8-001 with three lines validating the complete quarantine path set and every pinned file before unarmed success. Exact approval receipt is `build/WI071-review/pg-final-dns-v8r1.json`; conditions and all three freeze hashes are in `build/WI071-review/pg-dns-v8-r1-review-20261021.json`.

Independent focused6/binding72/delivery PowerShell17/producer PowerShell8 tests passed. Runtime42-member verifier, delivery12-member verifier and producer7-member verifier passed with reference propagation. WholeCheckPublic verified187 files. Release framing5 passed after PG explicitly corrected the harness parser input in memory to the actual delivery invoke.ps1; the standalone supporting test initially failed because it looked for a nonexistent sibling. No frozen file was changed. This is a nonblocking reproducibility issue, not a runtime parser failure. Unchanged TLS fixtures were not rerun.

Native plan is bounded Java17 name resolution using the pinned compiled class, plus fresh public-IPv4 no-auth STARTTLS with system CA and hostname verification. Exact transfer handling and current cold preservation remain MA responsibilities under their approved scopes. Local approval is not native readiness or mail acceptance. Original candidate and finding receipts remain preserved.

## WI008 Exact Transition Review
Reviewed WI008 handoff and completion addendum, operator/transport delta from profile-retry preservation and exact frozen metadata. Receipt: `build/WI071-review/pg-dns-v8-transition-20261021-final.json`. Freeze `f5e03832a77ceb118f931b012421b46f37bbfb290237f38a47c04801855efbb9` and all eight members independently matched. Fourteen Python and five PowerShell tests independently passed (ec3dbe); no remote/private access.

Allowance is limited to one exact expired PREFLIGHT attempt preservation and v7 tool retirement, after fresh native cold checks. App mask must be absent, grant absent, run/log directories empty. Supervisor is copied durably, retired and checked unloaded before records move; v7 tools move last. Final preserved inventory and unchanged JAR/SPA are verified. Existing failed semantic vectors are not relabeled. Any HOLD/partial transition requires inspection without automatic replay. Native postchecks must precede v8r1 Public. d59e49 target Java/TLS proof remains MA-provided evidence, not PG-executed app-sandbox acceptance.
