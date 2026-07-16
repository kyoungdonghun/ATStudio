---
id: WI-20260716-ATS-030
req: REQ-20260716-ATS-002
agent: qa-integ
date: 2026-07-16
decision: CHANGES_REQUIRED
---

# WI-20260716-ATS-030 Integration Verification Summary

## Findings First

### [P1] F-025-03 - Same-status billing-agreement replacement bypasses the correction stale fence

The correction records only `beforeBillingAgreementStatus` and execution compares only that enum
(`PaymentEntitlementCorrection.java:129-135`,
`AdminPaymentEntitlementCorrectionService.java:346-350`). A payment-method re-registration can
replace the encrypted key, fingerprint, method, and related agreement state and then return the same
agreement to `ACTIVE` (`BillingAgreement.java:118-176`). Because the correction pending-order fence
excludes `BILLING_AGREEMENT` orders (`AdminPaymentEntitlementCorrectionService.java:56-63`), an older
approved correction can then cancel the newly registered agreement at
`AdminPaymentEntitlementCorrectionService.java:237-244`. The added drift test changes `ACTIVE` to
`CANCELLED`; it does not cover `ACTIVE -> re-registration -> ACTIVE`
(`AdminPaymentEntitlementCorrectionServiceTest.java:276-298`).

**Disposition:** `REOPENED`.

### [P2] F-025-05 - Colon-labelled retained Provider identifiers still serialize raw

`ProviderSupportReference` recognizes labelled identifiers only when the separator is `=`
(`ProviderSupportReference.java:14-16`). ADMIN DTOs pass retained audit and Incident free text
through that sanitizer (`AdminPaymentOperationAuditLogResponse.java:49-53`,
`AdminPaymentReconciliationIncidentResponse.java:62-70`). Consequently, retained text such as
`transactionId: pay_0123456789_abcdef` is returned unchanged, contrary to the labelled-identifier
serialization rule in `docs/policies/security-policy.md:231-244`. Existing tests cover only
equals-labelled examples (`ProviderSupportReferenceTest.java:37-47`,
`AdminProviderIdentifierContractTest.java:168-203`).

**Disposition:** `REOPENED`.

### [P3] F-027-03 - Correct access semantics use a non-existent period field name in docs

The route implementation now correctly says `CANCELLED before expiry`
(`frontend/src/router/SubscriberRoute.tsx:12-15`), but the glossary and track use case name the field
`currentPeriodEnd` (`docs/standards/glossary.md:93`,
`docs/design/usecase/sound-track.md:208-216`). The implemented API model uses `expiresAt`
(`frontend/src/api/userSubscriptions.ts:10-20`).

**Disposition:** `REOPENED`.

## Decision

**CHANGES_REQUIRED.** WI-028/WI-029 close most source findings, but F-025-03 and F-025-05 are
material P1/P2 gaps. WI-031 must not treat the integrated remediation as accepted until a focused
follow-up closes them and the post-fix full gates pass.

## Disposition Matrix

| Finding | Disposition | Integration result |
|---|---|---|
| F-025-01 | `ENVIRONMENT-CONDITIONAL` | Source fences cover withdrawal, renewal authorization, Provider-success recording, and finalization. Retained MySQL withdrawal/correction interleavings were not run; the available MySQL proof remains opt-in and covers seven older races. |
| F-025-02 | `CLOSED` | ADMIN GET uses `NOT_SUPPORTED` and the observation-only Provider path; scheduled reconciliation remains the mutating recovery entry point. |
| F-025-03 | `REOPENED` | Agreement drift is status-only and misses a completed same-status billing-key replacement. |
| F-025-04 | `CLOSED` | Renewal, expiration, and reconciliation derive business dates from the configured payment clock; focused zone tests passed. |
| F-025-05 | `REOPENED` | Colon-labelled retained identifiers bypass response sanitization. |
| F-026-01 | `CLOSED` | Both ADMIN lists fence success, failure, and loading ownership with request generations. |
| F-026-02 | `CLOSED` | Subscriber whitelist refresh requests coalesce and drain to a final server refresh. |
| F-026-03 | `CLOSED` | Certification detail has explicit open state and close invalidates late completion. |
| F-027-01 | `CLOSED` | CORS exposes both export headers and the adapter validates initial/replay batch identity. A deployed separate-origin smoke remains an environment gate. |
| F-027-02 | `CLOSED` | Certification examples match response envelopes and the generic binary media contract. |
| F-027-03 | `REOPENED` | Service-enabled semantics are correct, but two docs use `currentPeriodEnd` instead of `expiresAt`. |
| F-027-04 | `CLOSED` | API and operations guidance include local and Provider currency context and distinguish diagnostics from persisted Incidents. |
| F-027-05 | `ENVIRONMENT-CONDITIONAL` | `frontend/tsconfig.tsbuildinfo` is still a tracked worktree modification (`1/1` diff; worktree blob `6be7018...`, HEAD `3c8b761...`). Closure depends on excluding it from the eventual staging allowlist and verifying the index. |

## Gate Status

- WI-028 reports 156 focused backend tests passing, followed by the repaired renewal slice at 6/6
  and the related slice at 48/48 (`WI-20260716-ATS-028-evidence-pack.md:148-202`).
- The supplied full-backend report observed at review start was the pre-fix run: 1,125 tests with
  six repeated `RecurringRenewalCommandIntegrationTest` context failures. Current source contains
  the test-only `PaymentProperties` bean (`RecurringRenewalCommandIntegrationTest.java:42-64`), but
  no post-fix full-backend report was available. Full-backend acceptance therefore remains open.
- WI-029 reports 25 focused frontend tests, typecheck, targeted lint/format, and focused CORS tests
  passing (`WI-20260716-ATS-029-evidence-pack.md:152-216`). The supplied Vitest cache showed all 44
  test files passing. No post-integration frontend production build evidence was available.
- WI-030 did not execute builds or tests because those commands write generated files outside the
  two-file write allowlist.

## Scope And Invariants

- Public full-track streaming, subscriber-only download entitlement, recurring billing, and the
  approved single-server topology remain unchanged in the integrated slice.
- WI-028/WI-029 ownership inventories contain no schema or migration change. The cumulative shared
  worktree is dirty, so this is a slice-attribution statement rather than a clean-tree claim.
- WI-030 did not inspect or mutate the client worktree/runtime. WI-028 and WI-029 record the same
  isolation boundary; independent client inspection was prohibited by the handoff.

## Smallest Follow-up WI

1. Add a no-schema billing-agreement revision fence that rejects any agreement mutation after a
   correction snapshot, and test `ACTIVE -> completed re-registration -> ACTIVE` before execute.
2. Sanitize colon-labelled and equivalent retained Provider identifiers, with DTO serialization
   sentinels for full and partial values.
3. Replace `currentPeriodEnd` with the implemented `expiresAt` terminology in the two affected docs.
4. Run the complete backend and frontend gates, then verify the eventual Git index excludes
   `frontend/tsconfig.tsbuildinfo`.
