---
id: WI-20260716-ATS-032
req: REQ-20260716-ATS-002
agent: qa-integ
date: 2026-07-16
decision: COMMIT_READY_ENVIRONMENT_CONDITIONAL
mode: bounded-independent-read-only-closure-review
---

# Evidence Pack: WI-20260716-ATS-032

## 1. Findings First

**Actionable new findings: none.** No new P1/P2 issue, correctness mismatch, or source regression was
identified within the WI-032 scope.

## 2. WI-031 Reopened Finding Verification

### F-025-03 - Same-status billing-agreement replacement

**Disposition: `CLOSED` at source level.**

- `AdminPaymentEntitlementCorrectionService.java:210-217` locks the agreement and subscription,
  then checks both before-state predicates before processing mutation.
- `AdminPaymentEntitlementCorrectionService.java:347-363` requires the expected status, non-null
  timestamps, and strict `agreement.updatedAt.isBefore(correction.createdAt)`.
- `AdminPaymentEntitlementCorrectionServiceTest.java:305-341` covers
  `ACTIVE -> re-registration -> ACTIVE`, rejects execution, preserves the replacement agreement,
  and leaves the correction approved.
- Equal and missing timestamps fail closed at
  `AdminPaymentEntitlementCorrectionServiceTest.java:343-389`.

This closes the WI-030 interleaving because a completed replacement changes the audited agreement
revision even when status returns to `ACTIVE`. It introduces no schema field or new query/network
behavior.

### F-025-05 - Retained labelled Provider identifiers

**Disposition: `CLOSED` at source level.**

- `ProviderSupportReference.java:13-16` recognizes the supported labels, including `orderId`, with
  either `:` or `=` and surrounding whitespace.
- `ProviderSupportReference.java:39-53` replaces the complete retained value with a deterministic
  `REF-*` reference while preserving the label and separator.
- ADMIN response boundaries still call the centralized sanitizer at
  `AdminPaymentOperationAuditLogResponse.java:49-53` and
  `AdminPaymentReconciliationIncidentResponse.java:62-70`.
- Focused regression coverage is recorded by WI-031 at
  `ProviderSupportReferenceTest.java:37-73` and
  `AdminProviderIdentifierContractTest.java:167-211`.

The colon-labelled raw-value bypass identified by WI-030 is no longer present in the reviewed source.

### F-027-03 - Subscription expiry documentation

**Disposition: `CLOSED` at source/document level.**

- `frontend/src/api/userSubscriptions.ts:10-20` defines the wire field as `expiresAt`.
- `frontend/src/router/SubscriberRoute.tsx:12-15` describes the service-enabled rule using expiry.
- `docs/standards/glossary.md:93` and `docs/design/usecase/sound-track.md:208-216` use `expiresAt`.
- WI-031's terminology check reported no `currentPeriodEnd` match in the two owned documents
  (`WI-20260716-ATS-031-evidence-pack.md:130-134`).

## 3. WI-030 Disposition Matrix Reconciliation

WI-030 had 8 `CLOSED`, 3 `REOPENED`, and 2 `ENVIRONMENT-CONDITIONAL` findings (13 total). After
WI-031, the final source-level count is 11 `CLOSED` and 2 `ENVIRONMENT-CONDITIONAL`.

| Finding | WI-030 disposition | WI-032 result | Basis |
|---|---|---|---|
| F-025-01 | ENVIRONMENT-CONDITIONAL | Preserved | Retained MySQL proof remains an environment gate; no source regression identified. |
| F-025-02 | CLOSED | Preserved | Diagnostic ADMIN read-only contract and `Propagation.NEVER` evidence remain as recorded in `WI-20260716-ATS-030-evidence-pack.md:80-83`. |
| F-025-03 | REOPENED | CLOSED | Revision fence and same-status regression test above. |
| F-025-04 | CLOSED | Preserved | Configured payment clock evidence remains as recorded in `WI-20260716-ATS-030-evidence-pack.md:88-89`. |
| F-025-05 | REOPENED | CLOSED | Colon/equal labelled Provider sanitization above. |
| F-026-01 | CLOSED | Preserved | Request-generation ownership evidence remains as recorded in `WI-20260716-ATS-030-evidence-pack.md:94-95`. |
| F-026-02 | CLOSED | Preserved | Queued final-refresh loop evidence remains as recorded in `WI-20260716-ATS-030-evidence-pack.md:96-97`. |
| F-026-03 | CLOSED | Preserved | Explicit detail-open and close invalidation evidence remains as recorded in `WI-20260716-ATS-030-evidence-pack.md:98-99`. |
| F-027-01 | CLOSED | Preserved | CORS/export adapter source contract remains `CLOSED`; deployed separate-origin smoke is a residual environment gate and does not change the finding disposition. |
| F-027-02 | CLOSED | Preserved | Certification envelope and binary media documentation remains aligned. |
| F-027-03 | REOPENED | CLOSED | `expiresAt` alignment above. |
| F-027-04 | CLOSED | Preserved | Currency fields and diagnostic/Incident distinction remain documented. |
| F-027-05 | ENVIRONMENT-CONDITIONAL | Preserved | MA evidence preserves the file SHA-256 and reports diff check PASS, but final Git-index allowlist proof remains outside WI-032 and conditional. |

## 4. Product Invariants

The WI-030 invariant review remains valid after WI-031: public full-track listening remains controller
mediated, downloads remain subscriber-entitled, recurring billing remains the billing-key flow, and
the single-server scheduler topology remains unchanged. Supporting pointers are
`WI-20260716-ATS-030-evidence-pack.md:125-137` and
`WI-20260716-ATS-031-evidence-pack.md:80-88`.

## 5. Final MA Gate Evidence And Decision

No build, test, lint, typecheck, format, documentation-validation, runtime, or database command was
run by WI-032. The following results are the MA's current post-WI-031 gate evidence supplied for this
closure update.

### Backend

| Gate | Result |
|---|---|
| Clean test/JaCoCo/build | PASS |
| Suites / tests | 154 suites / 1,129 tests |
| Failures / errors / skipped | 0 / 0 / 9 |
| Line coverage | 77.90% |
| Branch coverage | 59.31% |
| Method coverage | 78.04% |
| Class coverage | 90.10% |
| Instruction coverage | 77.22% |

### Frontend

| Gate | Result |
|---|---|
| Production and full npm audit | PASS, 0 vulnerabilities |
| Typecheck / ESLint / Prettier / build | PASS |
| Vitest | PASS, 257/257 tests |
| Coverage | Lines 40.80%; statements 39.52%; functions 32.99%; branches 39.57% |
| `frontend/tsconfig.tsbuildinfo` | SHA-256 preserved: `B6A42AD2CD32A5AD04D06C55B8B1B26DD9B1894AFDAF7B508DFF16EF0C60F22A` |

### Documentation

| Gate | Result |
|---|---|
| Documentation validation | PASS, 419 traceability IDs |
| Client PDF | PASS, 12 pages and 278/278 source segments |
| PDF SHA-256 | `afba32cce2460d5d38b80f4a88278e31d1f7344a2258e240bfd61df74f4c6095` |
| Diff check | PASS |

### Runtime And Database Boundary

- Dev JAR on port 18080 with `ddl-auto=none`: `/api/tracks` returned HTTP 200.
- CORS smoke: origin `http://127.0.0.1:15173` was accepted and exposed
  `Content-Disposition` plus `X-Whitelist-Export-Batch-Id`. This supports the source-closed
  `F-027-01`; it is not a third conditional finding.
- `ddl-auto=validate`: correctly refused startup because the shared local DB lacks
  `billing_agreements.billing_key_cleanup_started_at`.
- The idempotent manual addition is already defined at
  `src/main/resources/db/manual/20260714_payment_db_integrity.sql:451-453`.
- No DDL was applied. This DB environment gate is **not passed** and must not be represented as
  schema-validated runtime evidence.

### Final Decision

**`COMMIT_READY_ENVIRONMENT_CONDITIONAL`.** Current source and the MA's post-WI-031 backend,
frontend, documentation, diff, and bounded runtime gates support commit readiness. The final finding
count remains 11 `CLOSED` and 2 `ENVIRONMENT-CONDITIONAL`: retained/shared-DB proof for `F-025-01`
and eventual Git-index allowlist proof for `F-027-05`. No actionable source finding remains.

## 6. Scope, Reproducibility, And Rollback

Review inputs were the WI-032 handoff, Tier 0 standards, WI-030 matrix/evidence, WI-031 summary/evidence,
current source/test/document pointers listed above, and existing generated artifacts. Inspection used
read-only PowerShell file reads, scoped searches, artifact metadata reads, and `git status --short`;
no build/test command was executed.

Only these two files are permitted outputs and were written:

- `deliverables/user/WI-20260716-ATS-032-summary.md`
- `deliverables/agent/WI-20260716-ATS-032-evidence-pack.md`

Rollback is limited to removing or reverting those two deliverables. No product, document, generated
metadata, Git index/history, client, runtime, database, Provider, or secret rollback is required.
