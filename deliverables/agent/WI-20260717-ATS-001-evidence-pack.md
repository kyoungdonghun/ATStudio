# Evidence Pack: WI-20260717-ATS-001

## Summary

Produced the executable documentation ledger for the approved 56-row V1 disposition. This WI changed only its user summary and this evidence pack.

## Scope / DoD Check

- [x] All 56 IDs are mapped: 13 KEEP, 16 REMOVE, 12 REPLACE, 3 ARCHIVE, and 12 resolved former REVIEW items.
- [x] V01-V12 decisions are recorded exactly as supplied by the user.
- [x] Ordered, disjoint next-WI boundaries are defined.
- [x] KEEP safeguards, preflight, reset rules, tests, stop conditions, and rollback are explicit.
- [x] No product/runtime/DB/Git mutation was performed.

## Reference Documents

Only the user-authorized inputs were read:

| Document | Use |
|---|---|
| `AGENTS.md` | Repository language, scope, deliverable, and non-destructive constraints |
| `deliverables/agent/WI-20260717-ATS-001-handoff.md` | WI scope, DoD, output contract, dependencies, and traceability requirements |
| `deliverables/agent/WI-20260716-ATS-038-evidence-pack.md` | Authoritative 56-row source manifests, bundles, safeguards, proof commands, and limits |
| `deliverables/user/REQ-20260716-ATS-004.md` | Approved V1 goal, scope, constraints, quality gates, and execution order |

No other repository document or product file was read. The source table is referenced rather than duplicated; its primary manifests are `## KEEP Manifest`, `## REMOVE Manifest`, `## REPLACE Manifest`, `## ARCHIVE Manifest`, and `## REVIEW Manifest`.

## Complete Action Mapping

| IDs | Action |
|---|---|
| `INT-K01`..`INT-K13` | KEEP |
| `INT-R01`..`INT-R16` | REMOVE |
| `INT-P01`..`INT-P12` | REPLACE |
| `INT-A01`..`INT-A03` | ARCHIVE |
| `INT-V01`..`INT-V05` | REMOVE |
| `INT-V06` | KEEP: guarded, disabled-by-default, production-forbidden QA bootstrap |
| `INT-V07` | REPLACE: persisted provider `TOSS`, recurring-only V1; retain multi-PG interfaces |
| `INT-V08` | REPLACE: explicit local load only; remove automatic import; leave secret file untracked and unread |
| `INT-V09` | KEEP: `0.1.0` |
| `INT-V10` | ARCHIVE tips by tags; remove branches only at final cleanup |
| `INT-V11` | KEEP until runtime transition; then remove/ignore as generated |
| `INT-V12` | KEEP as documented emergency admin operation |

This is 13 + 16 + 12 + 3 + 12 = 56 IDs. The exact target and rationale for every range remain at the corresponding row in the WI-038 source manifests.

## Execution Ledger

| WI | Ownership | Dependency |
|---|---|---|
| `WI-20260717-ATS-002` | Backend cleanup: legacy API removal and backend portions of cross-layer removals; explicitly excludes provider, config/bootstrap, and schema ownership | WI-001; may run in parallel with WI-003 |
| `WI-20260717-ATS-003` | Frontend cleanup and UI/API client replacements | WI-001; may run in parallel with WI-002 |
| `WI-20260717-ATS-004` | Payment-provider normalization, application config/bootstrap, fresh V1 schema, and disposable-MySQL proof | WI-002; runtime stopped before DB recreate |
| `WI-20260717-ATS-005` | Active docs, archives, generated artifacts, and demo-script cleanup | WIs 002, 003, and 004 |
| `WI-20260717-ATS-006` | Full backend/frontend/docs/runtime/API/UI verification and independent residual-reference audit | WIs 002 through 005 |
| `WI-20260717-ATS-007` | Final local tags, branches, worktrees, and runtime-log cleanup | WI-006 |

No WI may delete or replace a target owned by another WI. Branch, worktree, tag, and runtime-log cleanup is reserved for WI-007, local only, on the development branch, with no push.

## Protected Safeguards

The hard boundary is `INT-K01`: idempotency, claims, locks, leases/fences, state transitions, reconciliation, audit, refund, and storage recovery must retain equivalent invariants. Also protect `INT-K02` production refusal and host/CORS/secret controls, `INT-K03` OAuth state/PKCE, `INT-K04` failure/cancellation fallbacks, `INT-K05` authorization routes, `INT-K06` recurring checkout, `INT-K07` certification/document integrity, `INT-K08` browser-local play history, `INT-K09` base/test DB stack, `INT-K10` historical evidence, `INT-K11` client PDF, `INT-K12` rollback tags/official branch, and `INT-K13` explicit demo ownership. `INT-V06` remains non-production and off by default.

## Preflight and Reset Rules

Before any later destructive WI: record branch, HEAD, status, tags, unique commits, worktree/process ownership, target hashes/counts, and secret-scan baseline; stop runtime before DB recreation; verify a unique empty disposable DB; preserve tracked PDF and `frontend/tsconfig.tsbuildinfo` until its owning WI completes. After each batch, reset verification against the new HEAD and rerun exact negative searches, `git status`, docs checks, and relevant test/build gates. Never read or print local secret values.

## Commands and Tests

Documentation-only commands used: four `Get-Content -Raw` reads for the authorized inputs and targeted `Select-String` extraction from the two source inputs. No product tests, build, runtime, DB, Git mutation, or secret inspection was run. Later proof commands are plans from WI-038: exact `rg` searches; `gradlew.bat clean test jacocoTestReport build --console=plain`; frontend typecheck/lint/test/build/Prettier; docs validation; disposable-MySQL manifest and second-apply failure; runtime local/public `/` and `/api/tracks` checks; Git reachability/worktree checks; and staged-diff secret scanning.

## Risks / Rollback

Risks include unknown external API consumers, accidental weakening of financial invariants, applying fresh schema to a non-empty DB, premature runtime/log/worktree cleanup, and rewriting historical evidence. Stop and escalate on any mismatch, non-empty/shared DB, unresolved external-consumer evidence, failed safeguard test, secret exposure, or runtime ownership ambiguity.

Rollback this WI by removing only `deliverables/user/WI-20260717-ATS-001-summary.md` and this evidence pack. Later implementation rollback uses the preserved tags/reachable commits and batch-level reversion; DB rollback applies only to disposable databases.

## Follow-ups

- Parallel start after WI-001: `WI-20260717-ATS-002` and `WI-20260717-ATS-003`.
- Ordered dependencies: WI-002 -> WI-004; WIs 002-004 -> WI-005; WIs 002-005 -> WI-006; WI-006 -> WI-007.
- Completion remains stopped before destructive action until the next WI's preflight passes.
