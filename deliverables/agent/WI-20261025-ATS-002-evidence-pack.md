---
version: 1.0
last_updated: 2026-10-25
project: ATS
owner: PG
category: evidence-pack
status: accepted
---
# Evidence Pack: WI-20261025-ATS-002

## Summary
Independent local review resolves WI027 F1/F2 for candidate
`480834a7fadc69d542dd9113deb0af877d67a570170aa1819c682feec81efe2c`.
ALLOW the existing approved MA sequence subject to fresh native gates; no native
operation, private-input access or tool modification was performed by PG.

## Scope / DoD Check
- [x] Exact final candidate, minimal diff and frozen reused lineage verified.
- [x] Failure after retained log creation followed by new-attempt recovery tested.
- [x] Date exception limited to fresh read-only original-attempt reconciliation.
- [x] Business delta, four-value recovery and audit functions unchanged.
- [x] Additional claim/authority rejection and payload integration checked.
- [ ] Actual host/schema/MySQL/cleanup and applied outcome remain MA work.

## Reference Documents (Tier 0-2)
[WI002 handoff](WI-20261025-ATS-002-handoff.md) existed before processing.
Tier0 principles from core-principles, development-standards,
documentation-standards and glossary were retained from the preceding WI027
review; relevant security-policy and current AGENTS scope-persistent rules apply.
No fresh whole-document re-read is claimed. Tier2 freshly read: resumed
[REQ](../user/REQ-20261024-ATS-001.md), current docs/index, WI001/002 handoffs,
new candidate diff/README/test harness. Reused: WI027 original source inspection
and reproduction, WI023/025 historical tests and WI005/011 lifecycle/builders.
Assignee PG, review task; `.claude/config/workspace.json` confirms ATS.
Applied the previously read create-wi-evidence-pack skill; no further delegation.

## Evidence Pointers / Findings
Candidate folder: `build/WI20261025-001-stage-two-repair/`.
All seven manifest members match. Original WI023 candidate and all its members,
frozen WI005 and WI011 manifests/members also match.

- **F1 resolved:** native_adapter.py:46-49 selects the separate reconciliation
  attempt for service/log identity. Lines 63-67 retain the original apply claim
  check and save attempt-specific plan evidence under the original apply directory.
  Result/status files are attempt-specific. Reused cold_capture.py:549-566 remains
  unchanged and still uses exclusive log creation. Original claims/logs are not
  deleted or rewritten to permit retry.
- **F2 resolved:** transaction.py:22 exempts only reconcile from the end-date
  cutoff; lines 19-21 retain mode/write/candidate/freshness checks and lines 23-28
  retain target/nonce binding plus a distinct valid reconcile_attempt.
- **Preservation:** AST equality confirmed for eligible, audit_rows, expected
  and transact against WI023. Therefore the six-plan/source-state guards, exact
  four-cell delta, current recovery-before record, two offline audits, before-
  COMMIT rollback and uncertain-COMMIT handling are unchanged. make_payload.py
  and unlock.ps1 are byte-identical; no application/JAR/schema change.
- **No additional blocking finding** in this bounded repair review. Current
  schema/host compatibility is not inferred from the synthetic native boundary.

## Commands & Outputs / Tests
Local branch HEAD: `91a16e72693d7cd6ed04e0d36a5bd160af96f6d8`.
All reviewer-generated synthetic evidence is under the reviewer output folder.

1. `python -B build/WI20261025-002-stage-two-review/check_candidate.py 480834a7fadc69d542dd9113deb0af877d67a570170aa1819c682feec81efe2c`
   -> INDEPENDENT_LOCAL_REVIEW_PASS. [Source](../../build/WI20261025-002-stage-two-review/check_candidate.py),
   [result](../../build/WI20261025-002-stage-two-review/review-result.json).
   Six invalid after-date authorities rejected; actual adapter rejects three
   original-claim mismatches before log/service/DB start without rewriting claim.
   Actual reused mysql_arguments keeps read-only/super-read-only ON for
   inspect/reconcile and OFF only for apply; networking/events/infile and finite
   lifetime remain restricted. Actual builder compiles inspect/apply/reconcile
   payloads with exact candidate/record bytes; no payload executed.
2. Imported exact candidate test_recovery.py, redirected its synthetic output
   root to `build/WI20261025-002-stage-two-review/`, and ran its three unittest
   cases once -> 3 PASS. [Output](../../build/WI20261025-002-stage-two-review/focused-test-output.txt).
   Actual native() and retained exclusive-log helper execute; the first start
   fails after log creation, a new attempt confirms expected-after read-only,
   same-attempt replay holds, both attempted starts clean up, two logs remain,
   original claim bytes remain, and no UPDATE/INSERT/DELETE/COMMIT occurs.
   Linux ownership/signals/proc/socket/systemd/MySQL are synthetic; local O_EXCL
   file creation is real. This is not a live native trial.
3. Old eleven/seven tests were reused as historical evidence, not rerun. SE's
   initial Windows SIGHUP harness incompatibility was corrected at the synthetic
   signal boundary; it is not a server failure. No tool change followed the
   frozen manifest reviewed here.

## Allow / Hold Conditions
[Allowance receipt](../../build/WI20261025-002-stage-two-review/allow.json) binds
this exact candidate and independent results. Use the new builder folder; the
operational WI/scope/evidence namespace intentionally remains WI023.
MA performs required access handoff, strict known-host identity, fresh clock/
boot/machine/source/runtime pins and cold preflight. Inspect must produce a fresh
candidate-bound receipt and all five cleanup PASS checks; apply requires the
same schema/DB identity and complete unchanged before snapshot within 900 seconds.
Apply once; reconcile the ORIGINAL APPLY nonce with a fresh distinct 32-hex
reconcile_attempt, including after an uncertain result. Do not change the
original nonce to bypass consumed evidence. Read-only reconciliation still
requires fresh authority, target identity and original candidate-bound claim.

HOLD on drift, failed containment/cleanup, unknown/partial outcome or missing
evidence. Preserve failures and diagnose the smallest cause; never replay DML
blindly, weaken pins or relabel prior failures. Independent cold verification
and original temporary-access restoration are required after execution.

## Risks / Rollback
Verification date 2026-10-25; local Windows reviewer PG. Prior native observations
belong to MA/humans; this review has no fresh server observation. A local PASS
does not assert native SQL compatibility, DB state, provider validity or overall
stage-two completion. The original candidate remains held and unchanged.
No operational rollback was needed. If a later reversal is necessary, use current
exact-after equality and reviewed restoration of only the four old values with
new truthful audit; preserve original audits and unrelated data. No full restore,
old-password restoration, new backup, real payment or extra cost is authorized.

## Follow-ups
WI002 review is complete. It unblocks MA's approved gated sequence and
WI-20261025-ATS-003 result documentation; MA was notified immediately with the
exact hash. DocOps must use actual later receipts, not this allowance as APPLIED.
Mail/PG readiness and the full REQ remain open. Preserve WI027 and prior history.
