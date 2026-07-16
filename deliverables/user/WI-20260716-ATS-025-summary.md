---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: cr
category: audit
status: findings-recorded
dependencies:
  - path: REQ-20260716-ATS-002.md
    reason: Approved acceptance-hardening scope
  - path: ../agent/WI-20260716-ATS-025-handoff.md
    reason: Read-only backend and security audit contract
  - path: WI-20260716-ATS-022-summary.md
    reason: Supplied integration verification baseline
---

# WI-20260716-ATS-025 Summary

## Findings First

### F-025-01 - P1 - Withdrawal can race an already-claimed renewal and charge a deleted user

The renewal claim checks `isDeleted` while holding the agreement and subscription locks, but then returns an encrypted billing-key claim and releases those locks before the Provider call. Withdrawal locks only the user row, reads the billing agreement and subscription without their pessimistic locks, cancels both, and commits deletion. The renewal worker has no second persisted-state check before decrypting and charging. Provider-success recording and renewal finalization also do not reject a deleted user or cancelled agreement/subscription; finalization calls `startNewSubscription()` and `recordSuccessfulCharge()`, which can reactivate the local entitlement and agreement.

- Evidence: `src/main/java/com/atstudio/atstudio/service/UserService.java:124-162`; `PaymentCommandTransactionService.java:234-258,327-336,650-701`; `RecurringRenewalService.java:102-109,147-158,181-190`.
- Contract conflict: `docs/design/api-spec.md:3168-3171` promises deleted-user exclusion before charge and local-first billing stop.
- Impact: a renewal claimed immediately before withdrawal can still charge after the account is deleted; the resulting local finalization can restore ACTIVE subscription/agreement state on the deleted user.
- Required fix: define a single agreement-first lock/fence contract for withdrawal and renewal, and make an in-flight renewal explicitly block, defer, or converge with withdrawal before any Provider charge. Revalidate the cancellation/deletion fence when applying Provider success and finalization.
- Required tests: retained-MySQL interleavings for claim-vs-withdraw, withdraw-vs-Provider-success, and cleanup-vs-finalize; assert no charge is issued after the withdrawal fence and no deleted user is reactivated.

### F-025-02 - P1 - The ADMIN reconciliation GET mutates payment and entitlement state

`GET /api/admin/payments/reconciliation` calls the same recovery path used to repair Provider-success cases. Exact Provider evidence creates an Incident, records Provider success, dispatches the purpose-specific finalizer, and resolves Incidents. This is not a read-only diagnostic operation.

- Evidence: `src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java:253-256`; `AdminPaymentReadService.java:73-80`; `PaymentReconciliationService.java:339-370,499-514`.
- Test proof of mutation: `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationServiceTest.java:146-180` expects Incident writes and subscription finalizers from `reconcileProviderLedger()`.
- Contract conflict: `docs/design/api-spec.md:1592-1621,2198-2204` says the GET must not mutate payment, agreement, subscription, or Incident state.
- Impact: merely opening or refreshing an ADMIN diagnostic view can persist financial workflow rows and activate subscription state.
- Required fix: split read-only diagnostics from scheduled/operator recovery. The GET path may perform Provider lookups and return observations, but only an explicit mutating command or scheduled recovery path may persist or finalize.
- Required tests: controller/service integration tests asserting the GET never invokes Incident writers, Provider-success application, or purpose finalizers; retain separate tests for the mutating recovery path.

### F-025-03 - P1 - Entitlement correction reverses the payment lock order and leaves billing agreement state unfenced

Correction creation locks the subscription and then snapshots the billing agreement through an unlocked query. Execution again locks the subscription first, validates only the subscription before-state, and later mutates the agreement through the same unlocked query. Payment renewal/reconciliation paths lock the agreement before the subscription. `BillingAgreement` has neither an optimistic version nor a correction-time agreement before-state check.

- Evidence: `src/main/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionService.java:105-145,184-235`; `BillingAgreementRepository.java:44-45,67-75`; `PaymentCommandTransactionService.java:238-252,650-660`; `PaymentReconciliationTransactionService.java:144-150`; `BillingAgreement.java:30-64`.
- Test gap: `AdminPaymentEntitlementCorrectionServiceTest.java:195-260` covers subscription stale state but stubs the unlocked agreement lookup and does not cover agreement drift, renewal competition, or MySQL lock order.
- Impact: correction can deadlock against a payment finalizer, overwrite a concurrently advanced agreement, or cancel after a renewal claim has already escaped to a Provider charge.
- Required fix: use the canonical agreement-before-subscription lock order, compare the current agreement against the recorded before-state, and reject/defer correction while a non-terminal payment attempt can still produce a charge.
- Required tests: retained-MySQL correction-vs-renewal/finalizer interleavings, agreement-state drift, and idempotent correction retry after a rejected stale execution.

### F-025-04 - P2 - Configurable payment cron zone does not control the business date

The cron annotations resolve `app.payment.scheduler-zone`, but renewal and subscription-expiry decisions use no-argument `LocalDate.now()`, which uses the JVM default zone. With a UTC JVM and the default `Asia/Seoul` scheduler, the midnight job receives the previous UTC date and can delay renewal and expiry by one day.

- Evidence: `src/main/java/com/atstudio/atstudio/config/PaymentProperties.java:14-22`; `SubscriptionScheduler.java:32-34,58-62`; `RecurringRenewalService.java:58-64`; `src/main/resources/application.yml:112-114`.
- Test gap: `SubscriptionSchedulerTest.java:43-70` checks only the annotation string; expiry tests also build expectations with the JVM-default `LocalDate.now()`.
- Required fix: resolve the configured `ZoneId` once and inject a `Clock`, or pass the zone-derived business date into all date-based jobs.
- Required tests: run with JVM default UTC and scheduler zone `Asia/Seoul`, including both sides of midnight and a non-default configured zone.

### F-025-05 - P2 - Reconciliation audit notes disclose raw Provider identifier fragments

The structured ADMIN DTO now exposes a deterministic `REF-*` value, but reconciliation persists `first4...last4` fragments in the Incident and operation-audit note. The ADMIN audit response returns `note` verbatim, creating the free-text fallback channel prohibited by the payment privacy policy.

- Evidence: `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentService.java:210-229,297-340`; `AdminPaymentOperationAuditLogResponse.java:23-28,47-51`.
- Policy conflict: `docs/policies/security-policy.md:231-239` requires deterministic `REF-*` support references and forbids raw Provider identifiers in audit/Incident free text.
- Test weakness: `PaymentReconciliationIncidentServiceTest.java:99-143` asserts only that the complete raw value is absent, so disclosure of its first and last four characters passes.
- Required fix: use the same deterministic support-reference function in persisted structured evidence and omit Provider identifiers from free-text notes.
- Required tests: assert that neither the full identifier nor any selected raw prefix/suffix appears in persisted Incident/audit fields or serialized ADMIN responses.

## Disposition

| Severity | Count | IDs |
|---|---:|---|
| P0 | 0 | None |
| P1 | 3 | F-025-01, F-025-02, F-025-03 |
| P2 | 2 | F-025-04, F-025-05 |

**Judgment: CHANGES_REQUIRED.** The cumulative backend/security diff is not ready for acceptance promotion until the three P1 financial/state issues are fixed and independently verified. The two P2 issues should be closed in the same hardening cycle because they affect payment timing and the explicit Provider-identifier privacy contract.

## Reviewed Without Additional Findings

- Authentication/authorization, USER-only payment and business-certification self-service boundaries, process-local rate limits, and typed OAuth payload handling.
- Billing-key AES-GCM key-ID envelopes, startup validation, receipt URL policy, and Provider exception logging.
- Whitelist URL validation, user/channel lock order, bounded immutable export replay, and CSV formula neutralization.
- Company-certification private storage, format/canonicalization checks, path omission, and audit events.
- Download entitlement/quota/license locking, atomic track count, album/playlist mutation locks, and storage compensation/recovery.
- Fresh-schema/manual-patch shapes for payment indexes, certification audit/version, whitelist export/version, and unique user-track licenses.

## Residual Risks

- Retained-MySQL DDL, lock ordering, deadlock behavior, index plans, and duplicate-data prechecks remain environment-conditional.
- Live Toss charge/refund/lookup/callback behavior, key rotation, trusted proxy/CORS, and external receipt hosts were not exercised.
- The scheduler-zone defect was established by source reasoning; no JVM-zone-changing runtime test was executed in this read-only WI.
- `application-local.example.yml:62-89` does not show the active key ID/key-ring values required when switching from MOCK to `TOSS_BILLING`; startup validation fails closed, but the operator example should be aligned when the payment fixes are made.
- WI-022 supplied a 1,106-test, zero-failure result, but WI-025 did not rerun Gradle because build/test execution would create files outside the two paths permitted by this read-only contract.

## Scope Preservation

WI-025 created only this summary and `deliverables/agent/WI-20260716-ATS-025-evidence-pack.md`. It did not modify product code, configuration, schema, tests, runtime, DB/data, Provider state, client worktree, Git index, commits, branches, or remotes.
