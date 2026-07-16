# WI-20260716-ATS-007 Summary

## Behavior Changes

- Whitelist status changes now follow one explicit transition matrix. `REMOVAL_REQUESTED -> CANCELLED` records completed external removal, `CANCELLED` is terminal, and repeated current-status/removal operations are idempotent.
- Every cooperating user-scoped whitelist mutation locks the owning `users` row before channel/count writes. Channel rows also carry an optimistic `version` fence.
- Primary selection is serialized and idempotent. `REMOVAL_REQUESTED` and `CANCELLED` cannot be primary; removing or invalidating a primary promotes the newest eligible channel, while zero primary remains valid when no eligible channel exists.
- Account withdrawal no longer deletes external-removal evidence. `EXPORTED` and `REGISTERED` rows become `REMOVAL_REQUESTED`, existing removal requests remain, primary flags are cleared, and only local-only/terminal rows are deleted.
- Member edits are rejected for both `REMOVAL_REQUESTED` and `CANCELLED`, so channel metadata used as the external removal target cannot drift.
- Draft storage has a separate configurable technical safety cap, `APP_WHITELIST_MAX_SAVED_CHANNELS`, default 100. Creation acquires the existing user row lock, counts all saved whitelist rows, and rejects at the cap before save; this does not change subscription registration-slot policy.
- `GET /api/whitelist-channels` uses that same cap with primary-first/newest-first ordering. Retained users above the cap receive only the deterministic leading window in the unchanged response shape; older history is not claimed as returned.
- Admin export now requires an explicit status and/or keyword, reads a deterministic bounded candidate set, locks accepted users and channels in stable order, rejects oversized scopes before partial mutation, and records immutable batch/item snapshots.
- Export re-download uses only stored ordered snapshots and reproduces the original CSV bytes. New exports retain `userEmail` and operational channel/subscription fields while omitting user ID and nickname snapshots.

## User and Operator Effects

- Subscriber and admin screens label `CANCELLED` as completed removal and do not offer it as a registration-request state.
- Subscriber UI hides metadata editing for `REMOVAL_REQUESTED` and `CANCELLED` rows.
- Admin status controls expose only valid next transitions.
- The admin export action uses the currently applied status/keyword scope, returns a batch ID, and supports batch-ID re-download.
- `APP_WHITELIST_EXPORT_MAX_ITEMS` controls both new export selection and batch re-download; the committed default is 500.
- `APP_WHITELIST_MAX_SAVED_CHANNELS` controls only saved-row technical safety; the committed default is 100.

## Schema and Documentation

- Fresh schema adds whitelist optimistic versioning, recorded export filters, ordered/channel-ID snapshots, and deterministic export indexing.
- `src/main/resources/db/manual/20260716_whitelist_integrity_and_exports.sql` is the source-only additive patch for retained MySQL databases. It was not executed.
- API, database, use-case, security, and screen-flow canonical documents now describe the transition, withdrawal, lock, primary, export, PII, and re-download contracts.

## Verification

| Check | Result |
|---|---|
| Focused whitelist backend services/controllers/contracts | PASS, 12 suites / 86 tests / 0 failures / 0 errors / 0 skipped; BUILD SUCCESSFUL |
| Focused frontend Vitest | PASS, 3 files / 7 tests |
| Frontend typecheck, ESLint, build | PASS |
| Changed-file Prettier | PASS |
| Documentation validator | PASS, Tier 0, links, 401 traceability IDs, and index coverage |
| `git diff --check` | PASS; only repository line-ending conversion warnings were emitted |
| Generic repo lint helper | TOOLING-CONDITIONAL; `markdownlint`, `jq`, and `ruff` are not installed. Required docs validator and frontend ESLint passed separately. |

No DDL was executed and no live/test data was mutated by this WI.

## Risks and Environment Follow-up

- Real MySQL DDL rehearsal, lock behavior, index inspection, retained-data duplicate-primary inventory, and Hibernate validation are `ENVIRONMENT-CONDITIONAL`.
- MySQL has no partial unique index for the eligible primary rule. The approved implementation uses the user-row lock plus `@Version` and contract tests instead of an intrusive generated-column architecture; a database-only unique-primary proof remains residual environment evidence.
- A retained legacy export batch above the configured maximum is deliberately rejected before item loading. Operators must review the bound rather than bypass it with an unbounded query.
- Retained users above the saved-row cap see only the primary-first/newest-first leading window because the response shape has no pagination metadata. Operators should inventory and clean up those exceptional rows rather than lowering the cap into normal plan range.
- No product ambiguity or implementation blocker remains. WI-007 unblocks WI-010, WI-012, WI-013, WI-015, and WI-016 subject to their other dependencies.

WI-005/WI-006 changes in shared files, unrelated dirty-worktree edits, client-demo files, Cloudflare runtime, and runtime logs were preserved.
