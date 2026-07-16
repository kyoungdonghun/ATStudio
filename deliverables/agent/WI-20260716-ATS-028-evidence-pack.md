---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: se
category: evidence
status: complete
related_wi: WI-20260716-ATS-028
---

# WI-20260716-ATS-028 Evidence Pack

## 1. Work Item

| Field | Value |
|---|---|
| WI | `WI-20260716-ATS-028` |
| REQ | `REQ-20260716-ATS-002` |
| Agent | `se` |
| Branch | `codex/p1-acceptance-hardening` |
| Depends on | `WI-20260716-ATS-025`, `WI-20260716-ATS-027` |
| Blocks | `WI-20260716-ATS-030` |
| Scope | F-025-01 through F-025-05 only |
| Verdict | `IMPLEMENTED_FOCUSED_TESTS_PASS` |

The WI-028-owned runbook portion of the WI-029 integration hold F-027-04 is closed. Diagnostic and evidence wording now distinguishes local amount/currency from Provider amount/currency; no WI-029-owned file was modified.

The full-suite follow-up test-slice hold is also closed in the owned integration test. A deterministic test-only `PaymentProperties` bean satisfies the revised `RecurringRenewalService` constructor; production configuration and constructor behavior remain unchanged.

## 2. Inputs and Boundaries

The implementation followed:

- `deliverables/agent/WI-20260716-ATS-028-handoff.md`
- `deliverables/agent/WI-20260716-ATS-025-evidence-pack.md`
- `deliverables/user/WI-20260716-ATS-027-summary.md`
- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`
- `docs/standards/documentation-standards.md`
- `docs/standards/glossary.md`
- Owned payment design, operations, and security documents listed in the handoff

No frontend, CORS, API-spec, glossary, subscription use-case, client worktree/runtime, schema/migration, retained/live DB, Provider, Git index, commit, branch, or remote mutation was performed. Existing unrelated and concurrent changes were preserved.

## 3. Finding Closure

### F-025-01 - Withdrawal and Renewal Cancellation Fence

- `UserService` performs an early password check, then locks billing agreement, subscription, and user in canonical order before revalidation and mutation.
- Withdrawal rejects charge orders in `PROCESSING`, `PROVIDER_SUCCEEDED`, or `PENDING_PROVIDER_CONFIRMATION` for `SUBSCRIBE`, `UPGRADE`, or `RENEWAL`.
- Renewal claim validates active agreement/user/subscription state.
- `authorizeRenewalProviderCall` repeats the state and order check immediately before key decryption and Provider invocation.
- Provider-success recording commits Provider evidence even when the specialized cancellation fence rejects local continuation.
- Renewal finalization repeats the cancellation fence before creating payment evidence or extending access.

### F-025-02 - Read-only ADMIN Reconciliation

- `AdminPaymentReadService.reconcilePayments()` suspends the class-level read transaction with `NOT_SUPPORTED` and calls `diagnoseProviderLedger()`.
- Diagnostics use non-locking read claims and Provider lookups only.
- Diagnostics do not call Incident writes, Provider-success application, payment finalizers, or Incident resolution/reopen paths.
- `reconcileProviderLedger()` remains separately wired to the scheduled mutating recovery entry point.

### F-025-03 - Entitlement-Correction Lock and Stale Fences

- Create and execute lock agreement before subscription.
- Both paths reject Provider-outcome-pending charge orders.
- Execute compares the stored subscription before state and stored billing-agreement status before mutation.
- Correction cancellation uses the already locked agreement.

### F-025-04 - Payment Business-zone Clock

- `PaymentProperties.schedulerZoneId()` is the single zone parser.
- `RecurringRenewalService`, `SubscriptionScheduler`, and `PaymentReconciliationService` receive production clocks from the configured zone and package-visible test clocks.
- Renewal due date, claim timestamp, stale reconciliation boundary, completed-order lookback, payment-order expiration, and subscription expiration use the injected clock.
- The system-default-date compatibility overload was removed from active-agreement reconciliation.

### F-025-05 - Provider Identifier Privacy

- `ProviderSupportReference` is centralized under `service/payment`, outside DTO-only code.
- Structured reconciliation Incident identifiers persist deterministic `REF-[0-9A-F]{12}` values.
- Generated reconciliation audit notes omit Provider transaction IDs.
- ADMIN DTO mapping sanitizes labelled legacy Provider identifiers in audit/Incident free text to deterministic references.
- Sentinel tests reject the complete raw value, selected prefix/suffix fragments, and short raw identifiers in persistence and serialized responses.

### F-027-04 Integration Hold - Reconciliation Currency Wording

- `docs/design/payment-operations-runbook.md` now lists local amount with local currency and Provider amount with Provider currency for on-demand diagnostics.
- The compensation evidence list carries the same explicit local/Provider currency distinction.
- The change is limited to the WI-028-owned runbook; WI-029-owned API specification and frontend files remain untouched.

## 4. Changed Files and Traceability

### Production Source

| File | Finding | Change |
|---|---|---|
| `src/main/java/com/atstudio/atstudio/config/PaymentProperties.java` | F-025-04 | Added configured scheduler `ZoneId` accessor. |
| `src/main/java/com/atstudio/atstudio/repository/BillingAgreementRepository.java` | F-025-01, F-025-03 | Added user/provider pessimistic agreement lock. |
| `src/main/java/com/atstudio/atstudio/repository/PaymentOrderRepository.java` | F-025-01, F-025-03 | Added agreement/purpose/status pending-order existence fence. |
| `src/main/java/com/atstudio/atstudio/service/UserService.java` | F-025-01 | Added canonical withdrawal locks, locked credential recheck, and pending-order fence. |
| `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java` | F-025-01 | Added renewal claim, pre-Provider, success-recording, reconciliation, and finalization cancellation fences. |
| `src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java` | F-025-01, F-025-04 | Added business clock and immediate pre-Provider authorization. |
| `src/main/java/com/atstudio/atstudio/service/SubscriptionScheduler.java` | F-025-04 | Switched expiration decisions to the configured business clock. |
| `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationTransactionService.java` | F-025-02, F-025-04 | Added non-claiming provider observation and explicit business date input. |
| `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java` | F-025-02, F-025-04 | Added side-effect-free provider diagnostics and business-clock date/time boundaries. |
| `src/main/java/com/atstudio/atstudio/service/AdminPaymentReadService.java` | F-025-02 | Routed ADMIN GET to diagnostics under `NOT_SUPPORTED`. |
| `src/main/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionService.java` | F-025-03 | Added agreement-first locks, agreement drift check, and pending-payment fences. |
| `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentService.java` | F-025-05 | Persisted deterministic support references and removed Provider IDs from generated audit notes. |
| `src/main/java/com/atstudio/atstudio/service/payment/ProviderSupportReference.java` | F-025-05 | Centralized deterministic reference generation and legacy free-text sanitization. |
| `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentOperationAuditLogResponse.java` | F-025-05 | Sanitized retained audit notes and used centralized references. |
| `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentReconciliationIncidentResponse.java` | F-025-05 | Sanitized retained Incident free text and used centralized references. |
| `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentReceiptResponse.java` | F-025-05 | Moved support-reference import to the centralized service utility. |
| `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentReconciliationResponse.java` | F-025-05 | Moved support-reference import to the centralized service utility. |
| `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentRefundPreviewResponse.java` | F-025-05 | Moved support-reference import to the centralized service utility. |
| `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentRefundResponse.java` | F-025-05 | Moved support-reference import to the centralized service utility. |
| `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentSettlementResponse.java` | F-025-05 | Moved support-reference import to the centralized service utility. |
| `src/main/java/com/atstudio/atstudio/dto/payment/AdminSubscriptionPaymentResponse.java` | F-025-05 | Moved support-reference import to the centralized service utility. |

### Tests

| File | Finding | Change |
|---|---|---|
| `src/test/java/com/atstudio/atstudio/service/UserServiceTest.java` | F-025-01 | Verified lock order, pre-lock password rejection, and in-flight-order withdrawal rejection. |
| `src/test/java/com/atstudio/atstudio/service/PaymentCommandTransactionFenceTest.java` | F-025-01 | Added cancellation/deletion authorization, Provider-evidence retention, and no-reactivation finalizer tests. |
| `src/test/java/com/atstudio/atstudio/service/RecurringRenewalServiceTest.java` | F-025-01, F-025-04 | Verified pre-Provider authorization order, closed-fence no-call behavior, and zone boundary dates. |
| `src/test/java/com/atstudio/atstudio/service/RecurringRenewalCommandIntegrationTest.java` | F-025-04 full-suite follow-up | Added an imported test-only `PaymentProperties` bean fixed to `Asia/Seoul` for the sliced `DataJpaTest` context. |
| `src/test/java/com/atstudio/atstudio/service/SubscriptionSchedulerTest.java` | F-025-04 | Verified non-default-zone expiration date. |
| `src/test/java/com/atstudio/atstudio/service/AdminPaymentReadServiceTest.java` | F-025-02 | Verified diagnostic routing, no recovery call, and `NOT_SUPPORTED` propagation. |
| `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationServiceTest.java` | F-025-02, F-025-04 | Verified observation-only exact-DONE handling and explicit business dates. |
| `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationTransactionServiceTest.java` | F-025-02, F-025-04 | Kept reconciliation evidence tests on an explicit deterministic date. |
| `src/test/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionServiceTest.java` | F-025-03 | Verified agreement-before-subscription order, agreement drift, and pending-payment rejection. |
| `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentServiceTest.java` | F-025-05 | Verified persisted support reference and absence of raw full/prefix/suffix values and note identifiers. |
| `src/test/java/com/atstudio/atstudio/dto/payment/ProviderSupportReferenceTest.java` | F-025-05 | Verified stable references, idempotence, and free-text sanitization. |
| `src/test/java/com/atstudio/atstudio/dto/payment/AdminProviderIdentifierContractTest.java` | F-025-05 | Added persistence/serialization sentinels for legacy raw identifiers and fragments. |

### Documentation and Deliverables

| File | Finding | Change |
|---|---|---|
| `docs/design/payment-integration-design.md` | F-025-01..05 | Documented cancellation/payment fences, diagnostic/recovery split, business clock, and privacy boundary. |
| `docs/design/payment-operations-runbook.md` | F-025-01..05, F-027-04 integration hold | Updated operator behavior, lock/fence handling, diagnostic safety, explicit local/Provider currencies, scheduler dates, and identifier policy. |
| `docs/policies/security-policy.md` | F-025-05 | Required structured references and no full/partial raw identifiers in free text or serialization. |
| `deliverables/user/WI-20260716-ATS-028-summary.md` | F-025-01..05 | User-facing result and environment boundary. |
| `deliverables/agent/WI-20260716-ATS-028-evidence-pack.md` | F-025-01..05 | Reproducible traceability, verification, and rollback evidence. |

Total candidate files changed by WI-028: **38** (21 production source, 12 tests, 3 owned documents, and 2 WI deliverables).

## 5. Verification Evidence

### 5.1 Final focused command

```powershell
.\gradlew.bat test --tests "com.atstudio.atstudio.service.UserServiceTest" --tests "com.atstudio.atstudio.service.PaymentCommandTransactionFenceTest" --tests "com.atstudio.atstudio.service.RecurringRenewalServiceTest" --tests "com.atstudio.atstudio.service.SubscriptionSchedulerTest" --tests "com.atstudio.atstudio.service.AdminPaymentReadServiceTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationServiceTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationTransactionServiceTest" --tests "com.atstudio.atstudio.service.AdminPaymentEntitlementCorrectionServiceTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationIncidentServiceTest" --tests "com.atstudio.atstudio.service.PaymentOperationAuditLogServiceTest" --tests "com.atstudio.atstudio.service.WithdrawalBillingCleanupCoordinatorTest" --tests "com.atstudio.atstudio.service.BillingAgreementApplicationServiceTest" --tests "com.atstudio.atstudio.service.UserSubscriptionServiceTest" --tests "com.atstudio.atstudio.controller.AdminPaymentControllerTest" --tests "com.atstudio.atstudio.controller.PaymentControllerTest" --tests "com.atstudio.atstudio.dto.payment.ProviderSupportReferenceTest" --tests "com.atstudio.atstudio.dto.payment.AdminProviderIdentifierContractTest"
```

Result:

- `BUILD SUCCESSFUL in 1m 1s`
- 17 selected classes; 26 JUnit XML suites including nested suites
- 156 tests, 0 failures, 0 errors, 0 skipped
- Main and test compilation executed successfully
- Spring controller contexts used configured in-process test data sources and shut them down; no retained/external DB or Provider was used

### 5.2 Included related regression classes

- `PaymentOperationAuditLogServiceTest`
- `WithdrawalBillingCleanupCoordinatorTest`
- `BillingAgreementApplicationServiceTest`
- `UserSubscriptionServiceTest`
- `AdminPaymentControllerTest`
- `PaymentControllerTest`

### 5.3 Text hygiene

`git diff --check` was run against all owned tracked source/test/document paths. Exit code was 0. Output contained only Git LF-to-CRLF working-copy warnings and no whitespace error.

### 5.4 Full-suite Follow-up

Root cause: `RecurringRenewalCommandIntegrationTest` imports `RecurringRenewalService` into a sliced `@DataJpaTest` context, but the slice did not provide the new constructor dependency `PaymentProperties`. The same ApplicationContext failure surfaced as six failed test methods.

Test-only resolution:

- Added nested `PaymentConfiguration` with `@TestConfiguration(proxyBeanMethods = false)`.
- Imported that configuration only into `RecurringRenewalCommandIntegrationTest`.
- Supplied a `PaymentProperties` bean with `schedulerZone=Asia/Seoul`.
- Made no production workaround or fallback.

Class-only reproduction/closure command:

```powershell
.\gradlew.bat test --tests "com.atstudio.atstudio.service.RecurringRenewalCommandIntegrationTest"
```

Result: 6 tests, 0 failures, 0 errors, 0 skipped; `BUILD SUCCESSFUL in 34s`.

Related focused rerun command:

```powershell
.\gradlew.bat test --tests "com.atstudio.atstudio.service.RecurringRenewalCommandIntegrationTest" --tests "com.atstudio.atstudio.service.RecurringRenewalServiceTest" --tests "com.atstudio.atstudio.service.PaymentCommandTransactionFenceTest" --tests "com.atstudio.atstudio.service.SubscriptionSchedulerTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationServiceTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationTransactionServiceTest"
```

Result: 6 suites, 48 tests, 0 failures, 0 errors, 0 skipped; `BUILD SUCCESSFUL in 35s`.

## 6. Environment-conditional Evidence

| Boundary | Current evidence | Required closure |
|---|---|---|
| Retained MySQL lock/deadlock behavior | Deterministic service tests verify lock call order and state fences | Run withdrawal/correction versus renewal/finalizer interleavings on an approved retained MySQL 8 test copy |
| Live Provider timing | Provider boundary and state transitions verified with test doubles | Run approved Toss sandbox claim/cancel/lookup/reconciliation scenarios |
| Full backend acceptance | Original focused 156-test set and follow-up 48-test set pass; the six-test context root cause is closed | Rerun the complete backend gates after this test-slice correction and concurrent WI-029 integration |

These are not silently treated as passed. The WI prohibition on DB/Provider mutation prevented their execution here.

## 7. Rollback

1. Revert the 38 WI-028 candidate files together; the fences, tests, documents, centralized support-reference utility, and sliced-context fixture form one behavior set.
2. Do not roll back by routing ADMIN GET reconciliation to `reconcileProviderLedger()`; that would restore the side-effect defect.
3. If the clock wiring fails configuration parsing, restore the prior constructor wiring and clock tests together while preserving the configured zone value for diagnosis.
4. If support-reference presentation must change, preserve raw-fragment removal and structured `REF-*` persistence; only support-safe display wording may be adjusted.
5. No schema or data rollback is required because WI-028 introduced no migration or entity field.

## 8. Handoff to WI-030

WI-028 remediation is ready for independent acceptance verification. Its owned F-027-04 runbook hold and the `RecurringRenewalCommandIntegrationTest` sliced-context hold are closed. WI-030 should reconcile the currency wording with the independently owned WI-029 API-spec result and rerun the complete backend suite. It should verify current source rather than assume the focused result closes retained-MySQL or live Provider gates, preserve the concurrent WI-029 ownership boundary, and avoid broad staging in the shared dirty worktree.
