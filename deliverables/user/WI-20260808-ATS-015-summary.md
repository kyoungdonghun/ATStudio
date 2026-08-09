# WI-20260808-ATS-015 Completion Summary

## Status

WI-20260808-ATS-015 is **complete**. The approved administrator subscription-correction workflow is implemented across backend, frontend, fresh-schema baseline, audit behavior, and targeted MySQL concurrency verification.

WI-20260808-ATS-016 is **unblocked**. Its approved handoff already exists at `deliverables/agent/WI-20260808-ATS-016-handoff.md`.

Completion of WI-015 does not pre-close the REQ-level full-suite, coverage, build, security, or cross-layer gates listed under Remaining Verification.

## Delivered Behavior

- General administrator corrections are stored in the separate `admin_subscription_corrections` workflow. The refund-bound `payment_entitlement_corrections` workflow remains unchanged.
- Direct ADMIN `PUT` and `DELETE /api/user-subscriptions/{id}` backend mappings and frontend call paths are retired. The verified active controller/frontend reference count for those direct ID mutation paths is `0`.
- Self-service subscription paths remain active, including `GET`, `PUT`, and `DELETE /api/user-subscriptions/me` and `POST /api/user-subscriptions/me/reactivate`.
- The explicit workflow is `preview -> request -> approve -> execute`. One administrator may perform all stages; this is a single-operator workflow, not two-person approval.
- Closing, reopening, or refreshing the workflow resumes an open `REQUESTED`, `APPROVED`, or `PROCESSING` correction through the open lookup. Aborted or superseded lookups and previews cannot overwrite the current row's state.
- `reason_note` is required. `approval_note` and `execution_note` are optional, and all three values persist when supplied.
- Toss payment, refund, provider billing-key deletion, and email calls from this workflow are all `0`.

## API

All routes require `ADMIN`.

| Method | Path | Result |
|---|---|---|
| `POST` | `/api/admin/user-subscription-corrections/preview` | Validate and preview a local correction |
| `GET` | `/api/admin/user-subscription-corrections` | List correction records |
| `GET` | `/api/admin/user-subscription-corrections/{correctionId}` | Read correction detail |
| `GET` | `/api/admin/user-subscription-corrections/open?userSubscriptionId={id}` | Return the open correction with `200`, or `204` when none exists |
| `POST` | `/api/admin/user-subscription-corrections` | Create a `REQUESTED` correction |
| `POST` | `/api/admin/user-subscription-corrections/{correctionId}/approve` | Explicitly approve the request |
| `POST` | `/api/admin/user-subscription-corrections/{correctionId}/execute` | Revalidate and execute the local correction |

## Transaction, Audit, And Locking

- A successful mutation and its generic administrator audit participate in the same outer transaction.
- A rejected execution writes its audit with `REQUIRES_NEW`. If that audit write fails, the audit exception is suppressed and the original `BusinessException` remains the caller-visible error.
- Before/after audit state includes the local subscription state and billing-agreement status. It contains no PII, provider identifiers, tokens, billing-key material, or other secrets.
- Request and execute use the shared lock order `BillingAgreement -> UserSubscription -> target Subscription -> correction`.
- Request finishes with a pessimistic non-terminal current read, preventing duplicate open corrections under MySQL `REPEATABLE READ`.
- Execute first reads only lock-target IDs through a non-locking projection, acquires the ordered locks, then revalidates current IDs, status, and snapshots from the locked correction row.

## Database Evidence

### Current development database

- MySQL version: `8.0.45`.
- Application was additive: `40 -> 41` tables.
- `admin_subscription_corrections`: `32` columns, `11` physical indexes including InnoDB foreign-key indexes, `9` foreign keys, and `0` rows at verification.
- Existing 40-table row-count digest before and after:
  `4995fa10e08421b24ffa302822b8611b37f1516fdfbf68e1f3095ed7cb4811bd`
- Existing-data comparison: `unchanged=true`.

`src/main/resources/schema.sql` is the sole V1 fresh baseline. WI-015 added no manual migration file.

### Disposable proof database

- Database: `ats_disposable_20260808_f1eb1b33`.
- Fresh `schema.sql` plus `seed.sql`: `41` tables, `6` plans, InnoDB, and `0` correction rows before the race proof.
- `AdminSubscriptionCorrectionMysqlConcurrencyIntegrationTest`: `2` tests, `2` passed, `0` skipped, `0` failures, `0` errors, `17.857s`.
- The proof covers duplicate request creation and the execute-versus-request race.
- The disposable database was removed, and its absence was verified.

## Verification Boundary

| Evidence stage | Result | Boundary |
|---|---|---|
| Earlier backend full suite | `1,272` discovered; `1,260` passed; `12` gated skipped; `0` failures/errors | Ran before the final lock/open refinements; it is not post-refinement full-suite evidence |
| Post-refinement lock cohort | `37` discovered; `35` passed; `2` MySQL-gated skipped; `0` failures/errors | Targeted lock and transaction verification |
| Open-endpoint cohort | `42/42` passed | Backend controller, repository-contract, and service coverage for `200/204` and non-terminal lookup |
| Live MySQL gated cohort | `2/2` passed | MySQL 8/InnoDB concurrency proof on the disposable database |
| Frontend final targeted cohort | `3` files, `52/52` passed | Page/modal `14`, domain API `14`, coverage cohort `24` |

Frontend `typecheck`, ESLint, and Prettier check passed. The frontend build remains assigned to WI-027.

The final post-refinement full backend suite and coverage run belongs to WI-023. The earlier `1,272`-test run must not be represented as that final rerun.

The final whole-worktree `git diff --check` evidence passed with CRLF conversion warnings only. The final documentation-only check is recorded in the Evidence Pack.

## Rollback

- **Code rollback:** revert the correction backend/frontend implementation and its fresh-baseline source changes as one compatible code change. Restoring the retired direct ADMIN mutation routes requires a separate approved product decision.
- **Database rollback:** code rollback does not require dropping the additive table. Dropping `admin_subscription_corrections` is destructive and is safe only after confirming that no correction rows or related audit evidence must be retained, followed by explicit destructive approval.
- No provider, refund, billing-key, or email rollback is required because the workflow made no such external calls.

## Remaining Verification

- WI-023: final full backend tests and coverage.
- WI-024: final full frontend tests and coverage.
- WI-025: final frontend typecheck gate.
- WI-026: final frontend lint and format gates.
- WI-027: backend and frontend builds.
- WI-028: administrator and payment security review.
- WI-030: final cross-layer audit.

## Scope Integrity

This DocOps pass updates only this summary and `deliverables/agent/WI-20260808-ATS-015-evidence-pack.md`. It does not edit code, schema, other documentation, Git state, `application-local.yml`, database state, or output artifacts. The intentional untracked file `output/client-demo-screenshots-20260716-140514.zip` remains untouched.
