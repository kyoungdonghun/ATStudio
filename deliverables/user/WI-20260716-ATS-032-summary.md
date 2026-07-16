---
id: WI-20260716-ATS-032
req: REQ-20260716-ATS-002
agent: qa-integ
date: 2026-07-16
decision: COMMIT_READY_ENVIRONMENT_CONDITIONAL
---

# WI-20260716-ATS-032 Closure Review Summary

## Findings First

**Actionable new findings: none.**

The three findings reopened by WI-030 are closed at the current source/document level, and this
review found no regression in the existing WI-030 disposition matrix.

| Finding | Source disposition | Evidence |
|---|---|---|
| `F-025-03` | `CLOSED` | Agreement execution requires matching status plus non-null `agreement.updatedAt < correction.createdAt`; the same-status re-registration regression test rejects the stale correction and verifies the replacement remains active (`AdminPaymentEntitlementCorrectionService.java:347-363`, `AdminPaymentEntitlementCorrectionServiceTest.java:305-341`). |
| `F-025-05` | `CLOSED` | Retained Provider labels accept `=` and `:` separators, including whitespace, and replace the value before both ADMIN audit and Incident serialization (`ProviderSupportReference.java:13-16,39-53`, `AdminPaymentOperationAuditLogResponse.java:49-53`, `AdminPaymentReconciliationIncidentResponse.java:62-70`). |
| `F-027-03` | `CLOSED` | The route contract, API type, glossary, and SOUND-019 precondition use `expiresAt`; no stale field remains in the two owned documents (`SubscriberRoute.tsx:12-15`, `userSubscriptions.ts:10-20`, `glossary.md:93`, `sound-track.md:208-216`). |

## WI-030 Regression Review

WI-030's 13-finding matrix remains supported. After WI-031, the final source-level count is 11
`CLOSED` and 2 `ENVIRONMENT-CONDITIONAL`: `F-025-01` retained-MySQL interleavings and `F-027-05`
staging/index proof for `frontend/tsconfig.tsbuildinfo` remain conditional. `F-027-01` remains
`CLOSED` at source level; its deployed separate-origin smoke is a residual environment gate, not an
additional conditional finding. No product policy regression was found for public full-track
listening, subscriber-only download, recurring card billing, or single-server topology.

## Final Gate Decision

**`COMMIT_READY_ENVIRONMENT_CONDITIONAL`.** The MA supplied a current post-WI-031 full gate rerun:

- Backend clean test/JaCoCo/build: 154 suites, 1,129 tests, 0 failures, 0 errors, 9 skipped; line
  77.90%, branch 59.31%, method 78.04%, class 90.10%, instruction 77.22%.
- Frontend production/full audit: 0 vulnerabilities; typecheck, ESLint, Prettier, build, and 257/257
  Vitest tests passed. Coverage is lines 40.80%, statements 39.52%, functions 32.99%, branches
  39.57%. `frontend/tsconfig.tsbuildinfo` remained SHA-256
  `B6A42AD2CD32A5AD04D06C55B8B1B26DD9B1894AFDAF7B508DFF16EF0C60F22A`.
- Documentation validation passed with 419 traceability IDs; the 12-page PDF matched 278/278 source
  segments with SHA-256 `afba32cce2460d5d38b80f4a88278e31d1f7344a2258e240bfd61df74f4c6095`;
  diff check passed.
- A dev JAR on port 18080 with `ddl-auto=none` returned `/api/tracks` 200 and the expected CORS
  origin/header exposure contract.

The shared local DB environment gate did **not** pass: `ddl-auto=validate` correctly refused startup
because `billing_agreements.billing_key_cleanup_started_at` is absent. The idempotent manual migration
is defined in `src/main/resources/db/manual/20260714_payment_db_integrity.sql`, but WI-032 did not
apply DDL. The final disposition therefore remains 11 `CLOSED` and 2 `ENVIRONMENT-CONDITIONAL`.
These results are MA-supplied gate evidence; WI-032 did not rerun the commands.

## Scope And Restrictions

Read-only review completed from the ATStudio worktree. Only this summary and the paired evidence pack
were written. Product source, product documentation, generated frontend metadata, Git index/history,
client worktree/runtime, database, Provider, secrets, and runtime processes were not touched.

## Rollback

Deliverables-only rollback: remove the WI-032 summary and evidence pack. No product or environment
rollback is applicable.
