---
version: 1.0
last_updated: 2026-10-24
project: ATS
owner: PG
category: evidence-pack
status: completed
---
# Evidence Pack: WI-20261024-ATS-025

## Summary
Independent local review allows the exact WI023 candidate to enter the approved native sequence, subject to its fresh host/DB gates. No server access or business mutation occurred in this review.

## Scope / DoD Check
- [x] Existing actors and grant exact delta, preserved rows, truthful audit, recovery evidence, replay/unknown outcome, lifecycle and date semantics reviewed.
- [x] Author checks independently repeated; additional failure injection and payload compilation passed.
- [x] Prepared versus applied boundary recorded. Native success is not claimed.

## Reference Documents (Tier 0-2)
Injected pointers: [handoff](WI-20261024-ATS-025-handoff.md), [REQ](../user/REQ-20261024-ATS-001.md), AGENTS.md, docs/index.md, docs/standards/core-principles.md, development-standards.md, documentation-standards.md, glossary.md, docs/policies/security-policy.md, original WI001 fixture and WI017 stage-one evidence. Source checks additionally used AdminOperationAuditLog, AdminOperationAuditService, AdminOperationAuditState, source schema.sql and seed.sql. Latest AGENTS stage-two approval persistence governs; no new approval gate was invented.

## Evidence Pointers
- Candidate: build/WI20261024-023-stage-two/candidate.json
- Exact SHA256: `3fbbec606c60ac73c752d4f082965692f589311a413746a8bb938733a0070b46`
- Six manifest-bound files: transaction.py, native_adapter.py, make_payload.py, test_stage_two.py, unlock.ps1, README.md.
- Independent checks: build/WI20261024-025-stage-two-review/test_independent.py.
- Frozen reused fixture: build/WI20261023-005-account-fixture/candidate.json, SHA256 `3a0f4fd30afc012a8e1a0c46d70b87ed1d2ef5492c2f8143121dc741da35654c` (22 files).
- Runtime freeze remains `3c1479f9cf0d55e854ae7c7f26d8b7d7c15825eedff15333c06092059f10b66d`; JAR remains `f3b748f8b3fb4fbd4027b797ebf0bc79db62bc9671e5d7e45a8397732d2aef81`.

## Findings Resolved Before Freeze
1. Apply now requires the inspection candidate/nonce claim and all five cleanup PASS results, not merely a snapshot file.
2. Four actual prior column values (user1 role/updated_at and grant1 expiry/updated_at) are retained privately before DML and verified against current per-cell hashes. Historical pre-audio backup is not called a current complete recovery point.
3. Audit actor is NULL with explicit OFFLINE_OPERATOR/REQ/nonce; no logged-in admin or email verification/consent is fabricated.

## Commands & Tests
- `python -B build/WI20261024-023-stage-two/test_stage_two.py`: 11 PASS.
- `python -B build/WI20261024-025-stage-two-review/test_independent.py`: 7 PASS.
- Independent actual make_payload.build with synthetic inspect record: 545137 bytes, Python compilation PASS; no execution/network.
- Reused mysql_arguments: inspect/reconcile retain read-only and super-read-only ON; apply changes both OFF. Network/event restrictions retained.
- Manifest and every component hash verified; Python files compiled without writing bytecode.
- Additional tests cover recovery persistence/hash failure before DML, expiry guard rollback before COMMIT, intent persistence failure, acknowledged COMMIT with lost evidence requiring reconciliation, and minimal actual recovery data.

## Allow / Hold Conditions
ALLOW only this manifest hash, approved scope, fresh strict SSH host identity, current boot/machine pins, matching pinned runtime/source/JAR, cold app/DB, fresh <=900-second inspection and exact full snapshot match. Writable DB must be socket-only, events disabled and infile disabled, exact MySQL containment. Inspect/reconcile are read-only. No HTTP or TCP443 access required.

The expected transaction changes only user1 role/updated_at and grant1 expiry/updated_at plus two audit inserts. All other cells across the known 43 tables, including ID2/3 credentials/session state and six plans, must match. The target date is fixed 2026-11-24, inclusive under existing expiresAt >= today business semantics; status remains CANCELLED, with no billing activation.

HOLD on drift, failed containment/cleanup, uncertain COMMIT, absent prior evidence or mismatched hashes. Reconcile the ORIGINAL apply nonce first; never blindly repeat an uncertain write. APPLIED_CONFIRMED requires complete expected-after equality. A new nonce alone does not authorize replay. Actual server state, current schema and MySQL syntax remain native checks, not inferred from synthetic tests.

## Risks / Rollback
Full DB restoration is neither required nor authorized by this review. The bounded four-value recovery record supports only the approved changed columns. If reversal becomes necessary, first require exact current expected-after state, then perform a reviewed correction restoring only these values with a new truthful audit entry. Never delete original audits, restore old credentials, replace the DB or reset unrelated data. Infrastructure auto-increment allocation may advance on rolled-back inserts; row/cell checks do not assert sequence counters are unchanged.

After native work, MA must perform read-only reconciliation, independently verify cold app/DB, restore temporary TCP22 access, remove only the task key and return SSH agent to Stopped/Disabled. Keep all failed attempts and public/private evidence. No cost, payment, provider mutation, new account or secret body was accessed by PG.

## Follow-ups
No dependent WI remains for PG. MA may perform the approved native sequence; this local review does not itself establish APPLIED or stage-two completion.

Final README-only refresh points to the fresh-expiry WI014 probe builder and explicit server-clock admission. Manifest/7 independent checks and payload compilation were repeated on the final candidate above; historical c0c47401 candidate is superseded.
