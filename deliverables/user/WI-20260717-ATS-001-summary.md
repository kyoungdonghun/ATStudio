# WI-20260717-ATS-001 Summary

## Status

Completed as a bounded documentation-formatting task. No product code, database, runtime, Git ref, worktree, secret, or unrelated artifact was changed. The 56-row source table remains authoritative in `deliverables/agent/WI-20260716-ATS-038-evidence-pack.md`.

## Approved Action Ledger

The complete compact mapping is:

| Set | IDs and action |
|---|---|
| KEEP | `INT-K01` `INT-K02` `INT-K03` `INT-K04` `INT-K05` `INT-K06` `INT-K07` `INT-K08` `INT-K09` `INT-K10` `INT-K11` `INT-K12` `INT-K13` |
| REMOVE | `INT-R01` `INT-R02` `INT-R03` `INT-R04` `INT-R05` `INT-R06` `INT-R07` `INT-R08` `INT-R09` `INT-R10` `INT-R11` `INT-R12` `INT-R13` `INT-R14` `INT-R15` `INT-R16` |
| REPLACE | `INT-P01` `INT-P02` `INT-P03` `INT-P04` `INT-P05` `INT-P06` `INT-P07` `INT-P08` `INT-P09` `INT-P10` `INT-P11` `INT-P12` |
| ARCHIVE | `INT-A01` `INT-A02` `INT-A03` |
| Resolved former REVIEW | `INT-V01` `INT-V02` `INT-V03` `INT-V04` `INT-V05` = REMOVE; `INT-V06` = KEEP, guarded disabled-by-default and production-forbidden QA bootstrap; `INT-V07` = REPLACE with persisted provider `TOSS` and recurring-only V1 while retaining multi-PG interfaces; `INT-V08` = REPLACE by removing automatic local import and requiring explicit local load, without reading or tracking the secret file; `INT-V09` = KEEP `0.1.0`; `INT-V10` = ARCHIVE branch tips by tags, then remove branches only during final cleanup; `INT-V11` = KEEP until runtime transition, then remove/ignore as generated; `INT-V12` = KEEP as a documented emergency admin operation. |

## Protected Safeguards

- Preserve payment idempotency, provider-attempt claims, locks, leases/fences, state machines, reconciliation, audit logs, refund and storage-recovery invariants (`INT-K01`).
- Preserve acceptance host/CORS/secret guards and keep QA bootstrap disabled by default and forbidden in production (`INT-K02`, `INT-V06`).
- Preserve OAuth state/PKCE and route safety, browser error/cancellation fallbacks, authorization boundaries, recurring checkout, whitelist/document integrity, local play history, base DB configuration, historical records, the client PDF, rollback tags, and explicit demo ownership (`INT-K03`..`INT-K13`).
- Stop the runtime before any disposable DB is recreated. Client review is complete; DB data preservation is not required.

## Ordered Next WIs

1. `WI-20260717-ATS-002`: backend cleanup, including legacy API removal and backend portions of cross-layer removals; excludes payment-provider normalization, application configuration/bootstrap, and schema work.
2. `WI-20260717-ATS-003`: frontend cleanup and UI/API client replacements; may run in parallel with WI-002 after WI-001.
3. `WI-20260717-ATS-004`: payment-provider normalization, application configuration/bootstrap, fresh V1 schema, and disposable-MySQL proof; depends on WI-002.
4. `WI-20260717-ATS-005`: active-document updates, archives, generated-artifact cleanup, and demo-script cleanup; depends on WIs 002-004.
5. `WI-20260717-ATS-006`: full backend/frontend/docs/runtime/API/UI verification plus an independent residual-reference audit; depends on WIs 002-005.
6. `WI-20260717-ATS-007`: final local tag, branch, worktree, and runtime-log cleanup; depends on WI-006.

WI-002 and WI-003 may run in parallel after WI-001. All other dependencies are mandatory. Ownership remains disjoint, work stays on the development branch, and no push is authorized.

## Preflight, Reset Rules, and Rollback

Before destructive work: capture branch/HEAD/status, tags and unique commits; verify clean ownership and process paths; stop runtime; confirm disposable DB identity and emptiness; snapshot exact paths/hashes; scan staged/generated output for secrets; then run proof-before searches. After each destructive batch, rerun status, exact negative searches, docs checks, tests, and DB manifest checks. Recreate/reset only a verified disposable DB, never a shared or active DB.

Rollback is by restoring the tagged/reachable pre-cleanup commits and reverting the specific batch. Do not delete rollback tags. For this WI itself, remove only the two WI-001 deliverables if this ledger is found incorrect.
