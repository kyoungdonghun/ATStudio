# Evidence Pack: WI-20260711-ATS-006

## Summary (one-liner)

- Reconciled payment design, API/code, JPA/MySQL schema, React UI, and operations/client documents into a read-only 3-way verdict with 14 de-duplicated findings: P0 1, P1 8, and P2 5.

## Scope / DoD Check

- [x] Mapped checkout, billing agreement, first charge, re-registration, upgrade, downgrade, cycle change, cancellation, reactivation, renewal, reconciliation, receipt/audit, refund, entitlement correction, settlement, withdrawal, admin UI, and deferred payment capabilities.
- [x] Reconciled overlapping backend, frontend, security, and integration claims into one final disposition per payment issue.
- [x] Re-verified account withdrawal, provider/local failure boundaries, concurrency/idempotency, and schema ENUM claims from current source instead of copying Phase 1 conclusions.
- [x] Assessed renewal/reconciliation bounds and index assumptions from repository queries and DDL without claiming unexecuted SQL plans.
- [x] Listed exact regression tests and MySQL/provider-safe verification inputs.
- [x] Used only the required disposition vocabulary in the contract matrix: `aligned`, `defect`, `policy ambiguity`, `deferred by design`, `external verification required`.
- [x] Created only this WI's two owned outputs.

## Baseline and Constraints

| Item | Result |
|---|---|
| Workspace | `C:\Users\jm991\Desktop\project\ATStudio` |
| Branch | `dev/kyoung` |
| HEAD | `27d22446e5d21324dadcfcb322dbe51704dfe914` |
| Working tree | Dirty before WI-006. Concurrent/user changes and WI-001 through WI-005 outputs were treated as immutable inputs. |
| Initial owned outputs | Both WI-006 output paths were absent. |
| Execution mode | Static, read-only inspection plus non-mutating PowerShell/`rg` comparisons. |
| Forbidden actions | No SQL, provider request, refund/admin mutation, secret readout, application state mutation, source edit, or existing document edit. |

Client snapshot hashes used for this verdict:

| File | SHA-256 |
|---|---|
| `docs/client/testing-guide.md` | `50496A957EF1BC21BE67A7E8862D2307C2A1FC3D1CF8FC1F6CADA78E01B3A5E0` |
| `docs/client/2-full-feature-checklist.md` | `BC9773B6C721260D8F4B33A52042A37337EBCB4584E39543A7E9919E8320B43D` |
| `docs/client/3-admin-checklist.md` | `7CE24DCE52C452ED2F7B36CD7BD4D9697AFF4DFC9A725EBDC7E83909E954CDE1` |

## Reference Documents (Tier 0-2)

| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Platform integrity, financial traceability, approval, and evidence rules |
| 0 | `docs/standards/development-standards.md` | Transactions, JPA/index, testing, and evidence standards |
| 0 | `docs/standards/documentation-standards.md` | Pointer-first and duplicate-resolution rules |
| 0 | `docs/standards/glossary.md` | Canonical subscription/payment terminology |
| 1 | `docs/policies/security-policy.md` | Secret/PII and environment boundary |
| 1 | `docs/policies/quality-gates.md` | Risk, regression, and Evidence Pack requirements |
| 2 | `docs/design/payment-integration-design.md` | Payment state, first charge, change, renewal, and idempotency intent |
| 2 | `docs/design/payment-operations-runbook.md` | Reconciliation and provider-success/local-failure procedure |
| 2 | `docs/design/payment-refund-receipt-settlement-policy.md` | Refund, correction, receipt, audit, and settlement policy |
| 2 | `docs/design/payment-settlement-import-design.md` | Settlement adapter and state-mutation boundary |
| 2 | `docs/design/usecase/user-subscription.md` | User subscription behavior |
| 2 | `docs/design/usecase/user-info.md` | Account withdrawal behavior |
| 2 | `docs/payment/` | Current user, admin, acceptance, and deferred-scope documentation |
| 2 | `docs/SR/SR-93.md` | Payment production-readiness claims |
| Context | `deliverables/user/REQ-20260711-ATS-001.md` | Approved audit scope |
| Context | `deliverables/agent/WI-20260711-ATS-001-evidence-pack.md` through `WI-20260711-ATS-005-evidence-pack.md` | Phase 1 claims, treated as hypotheses and re-checked |

Injection order applied: Tier 0 -> Tier 1 -> Tier 2 -> current source snapshot.

## Payment Contract Matrix

| Capability | Design intent | API / code | DB | Frontend | Operations / client docs | Final disposition |
|---|---|---|---|---|---|---|
| New checkout and prepare | `payment-integration-design.md:437-462`; `user-flows.md:23-48` | `PaymentController.java:72-83`; `BillingAgreementApplicationService.java:108-158` | `schema.sql:459-516` | `SubscriptionPlanPage.tsx:162-178`; `SubscriptionPaymentPage.tsx:122-198` | `2-full-feature-checklist.md:127-136` | `defect` - member flow works sequentially, but ADMIN is not excluded and the API-spec example is stale (PAY-006-08, PAY-006-10). |
| Billing auth confirm and first charge | `payment-integration-design.md:410-415,437-462,534-545` | `BillingAgreementApplicationService.java:161-245` | `schema.sql:485-538` | `SubscriptionPaymentPage.tsx:47-120` | `acceptance-test-checklist.md:33-41`; `SR-93.md:126-133` | `defect` - expected success path aligns, but failure persistence, concurrency, and provider/local recovery do not (PAY-006-03, PAY-006-04, PAY-006-12). |
| Payment-method re-registration | `payment-integration-design.md:499-509` | `BillingAgreementApplicationService.java:115-128,193-209` | Same agreement/order tables | `SubscriptionManagePage.tsx:338-352`; `SubscriptionPaymentPage.tsx:23-30,91-98` | `user-flows.md:70-91`; `acceptance-test-checklist.md:43-50` | `aligned` |
| Upgrade and immediate difference charge | `payment-integration-design.md:464-478` | `UserSubscriptionService.java:118-215,320-373` | New order/payment rows; `schema.sql:485-538` | `SubscriptionManagePage.tsx:355-416,695-800` | `2-full-feature-checklist.md:138-149` | `defect` - sequential behavior aligns, but concurrent requests can create distinct charge orders and provider idempotency keys (PAY-006-04). |
| Downgrade, cycle-only change, pending clear | `payment-integration-design.md:480-488` | `UserSubscriptionService.java:134-146,217-227` | Pending fields in `user_subscriptions` | `SubscriptionManagePage.tsx:305-336,390-405,643-800` | `user-flows.md:116-170`; `2-full-feature-checklist.md:145-147` | `aligned` |
| User cancellation and reactivation | `payment-integration-design.md:416-422,545-546`; `user-subscription.md:280-334` | `UserSubscriptionService.java:248-309` | Subscription/agreement statuses | `SubscriptionManagePage.tsx:420-445,807-858` | `user-flows.md:172-207`; `2-full-feature-checklist.md:150-157` | `aligned` |
| Provider-level agreement cancellation | `payment-integration-design.md:416-422` | `BillingAgreementApplicationService.java:255-281` | Agreement/subscription local state | API exists in `payments.ts:97-101`; no primary UX action by design | `api-spec.md:1405-1423` | `defect` - functional intent aligns, but provider deletion precedes local commit without durable recovery (PAY-006-12). |
| Account withdrawal with active billing | `user-info.md:259-284` omits billing disposition | `UserService.java:104-122`; `RecurringRenewalService.java:89-159` | Active subscription/agreement remain | No current React withdrawal consumer found | No payment acceptance row | `defect` - future charging is not stopped (PAY-006-01). |
| Renewal, retries, grace, pending application | `payment-integration-design.md:511-545`; `user-flows.md:209-238` | `RecurringRenewalService.java:84-288`; `SubscriptionScheduler.java:32-36` | Orders, payments, agreement failure count | Manage page exposes billing status and next date: `SubscriptionManagePage.tsx:566-614` | `acceptance-test-checklist.md:80-87` | `defect` - stale-period reuse, concurrency, and batch isolation are unsafe (PAY-006-04 through PAY-006-06). |
| Reconciliation and incidents | `payment-operations-runbook.md:35-128` | `PaymentReconciliationService.java:48-195`; incident APIs `AdminPaymentController.java:253-277` | Incident ledger exists | Incident list/status UI: `PaymentReadOnlyPage.tsx:195-203,387-405`; no on-demand reconciliation UI consumer | `admin-operations-guide.md:97-118` | `defect` - detection exists but scan completeness and query shape are not production-bounded (PAY-006-11). The on-demand endpoint is API-only, which current docs allow. |
| Receipt evidence and safe billing data | `payment-refund-receipt-settlement-policy.md:243-330` | `PaymentReceiptEvidenceService.java:42-157`; safe DTOs `BillingAgreementResponse.java:10-18`, `AdminBillingAgreementResponse.java:10-40` | Receipt/audit ledgers | Receipt/audit tabs exist | `acceptance-test-checklist.md:89-101,115-122` | `aligned` for receipt creation and secret minimization; settlement audit ENUM failure is classified in the settlement row. |
| Refund request, approval, execution | `payment-refund-receipt-settlement-policy.md:77-198` | `AdminPaymentRefundService.java:73-203` | `payment_refunds` has per-request idempotency key | `PaymentReadOnlyPage.tsx:408-492` | `admin-operations-guide.md:189-218` | `defect` - aggregate reservation is unlocked (PAY-006-07); approval separation remains a policy ambiguity (POL-006-02). |
| Entitlement correction | `payment-refund-receipt-settlement-policy.md:199-241` | `AdminPaymentEntitlementCorrectionService.java:77-228` | Correction row and subscription row are locked at execution | `PaymentReadOnlyPage.tsx:494-573` | `admin-operations-guide.md:219-238` | `aligned` - provider money movement and local access mutation remain separate. |
| Settlement import, scan, ignore | `payment-settlement-import-design.md:16-227` | `AdminPaymentSettlementService.java:78-309` | `payment_settlements` exists; audit ENUM delta is missing | `PaymentReadOnlyPage.tsx:321-385` | `3-admin-checklist.md:62-76`; `admin-operations-guide.md:152-188` | `defect` - operation logic aligns but MySQL audit persistence can fail (PAY-006-02). |
| Admin payment UI coverage | `system-overview.md:40-51,88-111` | Admin APIs are role-gated: `AdminPaymentController.java:55-277` | All ledgers have read paths | Nine tabs at `PaymentReadOnlyPage.tsx:607-635` | `3-admin-checklist.md:62-76` | `defect` - feature coverage aligns, but request ordering can commit stale state (PAY-006-13). |
| Billing API response contract | `api-spec.md:1301-1423` | Actual records: `BillingAgreementPrepareResponse.java:11-22`, `BillingAgreementConfirmResponse.java:10-17`, `BillingAgreementResponse.java:10-18` | N/A | `payments.ts:25-68` matches Java records | Current payment guides avoid field-level response copies | `defect` - Phase 1's aligned-contract claim is overturned (PAY-006-10). |
| Schema, migration, and indexes | `db-schema.md:11-19,680-705,1061-1105` | Hibernate defaults to validate: `application.yml:16-20` | 39 JPA tables match 39 fresh-schema tables, but migration/ENUM/index gaps remain | N/A | `system-overview.md:53-72`; `SR-93.md:112-120` | `defect` - PAY-006-02, PAY-006-09, and PAY-006-11. |
| Blocked legacy one-time subscription paths | `payment-integration-design.md:424-433` | `PaymentApplicationService.java:91-120`; `UserSubscriptionService.java:77-82` | No intended new subscription mutation | Legacy callbacks blocked: `SubscriptionPaymentPage.tsx:55-61,140-143` | `known-limits-and-next-steps.md:49-57` | `aligned` |
| Webhook, Toss settlement API, multi-PG, cash receipt mutation, tax invoice, creator payout | `known-limits-and-next-steps.md:40-57`; `feature-inventory.md:126-135` | Not implemented in current scope | No required current tables beyond extension points | No current user workflow | Explicitly listed as planned/on hold/out of scope | `deferred by design` |
| Live DB/provider/topology configuration | `payment-operations-runbook.md:227-249` | Repository cannot prove runtime values | Production baseline, SQL mode, row counts, indexes, and migration history are unknown | N/A | Single-server assumption is documented | `external verification required` - PAY-006-14 and the environment-dependent part of PAY-006-09. |

## De-duplicated Findings

| ID | Severity | Class | Consolidated Phase 1 source | Evidence-backed decision and required action |
|---|---:|---|---|---|
| PAY-006-01 | P0 | Confirmed defect | `BE-002` | Withdrawal only deletes selected child rows and calls `user.withdraw()` (`UserService.java:104-122`); due agreement lookup has no deleted-user predicate (`BillingAgreementRepository.java:26-29`); renewal still resolves an active subscription and charges (`RecurringRenewalService.java:89-159`). Stop renewal in the withdrawal orchestration and add a defensive deleted-user exclusion. Provider-key retention/deletion remains POL-006-01. |
| PAY-006-02 | P1 | Confirmed MySQL schema defect | `INT-005-01` | Settlement service emits three actions (`AdminPaymentSettlementService.java:93-108,213-220,237-253`) and target `PAYMENT_SETTLEMENT` (`PaymentOperationAuditLogService.java:134-158`), while executable DDL stops before those values (`schema.sql:797-815`) and both manual patches have no matching ALTER. Update fresh DDL and add an ordered existing-DB patch; verify by MySQL flush. |
| PAY-006-03 | P1 | Confirmed transaction defect | `BE-003` | `BusinessException` is a runtime exception (`BusinessException.java:7`); confirm uses default rollback (`BillingAgreementApplicationService.java:161-162`), then marks failure and throws (`:222-226`). The Mockito test only observes mutated objects (`BillingAgreementApplicationServiceTest.java:310-349`). Persist the failure outcome in a committed transaction before returning the business error. |
| PAY-006-04 | P1 | Confirmed concurrency/idempotency defect | `INT-005-02`, concurrency portion of `BE-004` | No order/agreement version or command lock is used. Upgrade creates a random order and a distinct `subscription-upgrade-{orderId}` key per request (`UserSubscriptionService.java:339-373`), so duplicate concurrent intent is not provider-deduped. Initial confirm and renewal can also duplicate local evidence, and `subscription_payments.payment_order_id` is non-unique (`schema.sql:518-538`). Introduce command-level idempotency, row claim/lock, and unique finalization constraints. |
| PAY-006-05 | P1 | Confirmed renewal state defect, newly found | New in WI-006 | Renewal searches READY/IN_PROGRESS/FAILED/DONE (`RecurringRenewalService.java:48-52`) and reuses every non-DONE order regardless of period because `status != DONE || samePeriod` (`:167-197`). An expired/suspended agreement is reset for a fresh subscription (`BillingAgreementApplicationService.java:285-305`), while the old FAILED order remains. On the next due run its old grace date can expire the new subscription without a charge (`RecurringRenewalService.java:140-144,276-287`). Scope renewal identity explicitly by billing period and test re-subscription after failure. |
| PAY-006-06 | P1 | Confirmed transaction/isolation defect | `BE-004`, batch-isolation portion of `INT-005-04` | Scheduler and renewal service join one transaction (`SubscriptionScheduler.java:32-36`; `RecurringRenewalService.java:84-105`), load an unpaged list, and call the provider inside the loop (`:147-159`). One decrypt/runtime/flush/commit failure can abort the batch and roll back prior local finalization after external success. Page/claim work and process each agreement in an isolated transaction. |
| PAY-006-07 | P1 | Confirmed refund race | `BE-005` | Create reads aggregate reserved amount and then inserts with no source-payment lock (`AdminPaymentRefundService.java:89-111,248-262`; `PaymentRefundRepository.java:49-57`). Per-refund execution lock does not protect aggregate reservation. Lock the source payment or use an atomic reserved/refunded invariant. |
| PAY-006-08 | P1 | Confirmed role-boundary defect | `FE-002` | ADMIN satisfies `authRequired(USER)` (`ProtectedRoute.tsx:7-24`), checkout routes use it (`router/index.tsx:153-161`), plan selection ignores role (`SubscriptionPlanPage.tsx:162-178`), and backend preparation checks user type but not role (`BillingAgreementApplicationService.java:108-137`). The non-production admin fixture is `INDIVIDUAL` (`TestUserBootstrapRunner.java:54-68`), so it can match a plan. Add an exclusive member guard and server-side ADMIN rejection. |
| PAY-006-09 | P1 | Conditional deployment defect | `INT-005-03`, `INT-005-07`, `INT-005-10` | Runtime uses `ddl-auto=validate` (`application.yml:16-20`). The current patch requires earlier payment migrations (`20260615_align_payment_whitelist_schema.sql:18-23`), but only two manual patches exist and no Flyway/Liquibase dependency was found. Fresh schema metadata says v12/38 although static count is 39 (`schema.sql:2-15,1014-1017`). H2 tests skip MySQL DDL (`src/test/resources/application.yml:1-7`). Restore an ordered chain or approve a migration baseline/framework; correct metadata in the same approved implementation WI. |
| PAY-006-10 | P2 | Confirmed documentation contract defect | Overturns WI-005 high-risk contract matrix | Prepare spec omits actual fields and shows `purpose=BILLING_AGREEMENT` with non-zero amount despite its own zero-amount rule (`api-spec.md:1316-1336`). Confirm spec is nested (`:1355-1374`) while Java/frontend are flat (`BillingAgreementConfirmResponse.java:10-17`; `payments.ts:50-57`). My/cancel examples include `id` and `failureCount`, omit `subscription`, and do not match `BillingAgreementResponse` (`api-spec.md:1390-1423`; record `:10-18`). Update the API spec from current DTO/controller contract or deliberately change/version the API. |
| PAY-006-11 | P2 | Confirmed completeness/performance defect | `IMP-003`, `INT-005-05`, `INT-005-06` | Reconciliation reads only page 0 size 100 (`PaymentReconciliationService.java:56-60,107-114`), scans all ACTIVE agreements and looks up subscription per agreement (`:75-91`), and scheduled reconciliation wraps provider calls in one transaction (`:48-53`). DDL lacks query-aligned order/payment indexes (`schema.sql:485-538`). Add cursor/time backfill, scan watermark, paged agreement joins, and only indexes justified by copied-data `EXPLAIN`. |
| PAY-006-12 | P2 | Confirmed recovery defect, newly found | Extends provider/local boundary review | Provider key issue happens before encryption and local finalization (`BillingAgreementApplicationService.java:174-243`); provider key deletion happens before local clear/cancel commit (`:255-281`). Encryption or commit failure can leave an untracked issued key or provider-deleted/local-active agreement. Provider interfaces expose payment lookup only, not billing-agreement lookup (`RecurringPaymentProvider.java:5-15`; `PaymentStatusLookupProvider.java:5-13`), and the runbook addresses charge success/local failure only (`payment-operations-runbook.md:130-163`). Add a durable billing-agreement operation/recovery model and idempotent delete handling. |
| PAY-006-13 | P2 | Confirmed frontend race | Payment subset of `FE-005` | `PaymentReadOnlyPage` starts tab/filter/page requests with no abort or request-generation guard and lets every completion update shared `pageInfo`, `loading`, and error state (`PaymentReadOnlyPage.tsx:179-248`). No `PaymentReadOnlyPage.test.tsx` exists. Implement latest-request-wins behavior and deferred-response tests. |
| PAY-006-14 | P2 | External verification required | `POL-002`, deployment portion of `INT-005-04`, `INT-005-09` | Scheduling is enabled on every instance (`AtStudioApplication.java:5-8`); cron expressions have no zone (`SubscriptionScheduler.java:32-60`; `PaymentReconciliationService.java:48-53`); docs assume one server (`system-overview.md:142-151`). Billing crypto validates lazily and supports only one `v1` key (`BillingKeyCrypto.java:22-110`), while one decrypt exception can abort the batch. Confirm replica count, JVM/business zone, secret presence, and rotation state without printing values; enforce them before horizontal scaling or rotation. |

## Phase 1 Claim Reconciliation

| Phase 1 claim(s) | Final disposition |
|---|---|
| `BE-002` | Confirmed as PAY-006-01. |
| `BE-003` | Confirmed as PAY-006-03. |
| `BE-004` + `INT-005-02` + `INT-005-04` | Split by cause into PAY-006-04 command serialization, PAY-006-06 batch isolation, and PAY-006-14 deployment topology. |
| `BE-005` | Confirmed as PAY-006-07. |
| `IMP-003` + `INT-005-05` + `INT-005-06` | Merged into PAY-006-11. |
| `FE-002` | Confirmed as PAY-006-08. |
| Payment subset of `FE-005` | Confirmed as PAY-006-13. |
| `INT-005-01` | Confirmed as PAY-006-02. |
| `INT-005-03` + `INT-005-07` + `INT-005-10` | Merged into PAY-006-09. |
| `INT-005-09` | Merged into PAY-006-14. |
| WI-005 statement that billing prepare/confirm response fields align | **Overturned** by PAY-006-10 after direct DTO/frontend/spec comparison. URL and request fields align; response examples do not. |
| PG payment positive controls | Confirmed: user/admin agreement DTOs omit raw billing material and provider evidence is sanitized (`BillingAgreementResponse.java:10-18`; `AdminBillingAgreementResponse.java:10-40`; `TossBillingProvider.java:448-598`). |
| No Phase 1 finding for stale FAILED renewal reuse | Added as PAY-006-05. |
| No Phase 1 finding for billing-agreement key issue/delete recovery | Added as PAY-006-12. |

Scope boundary: the original-audio access findings `BE-001` / `PG-004-01` are security/content-delivery findings, not one of the payment capabilities enumerated by this handoff. They are not duplicated here and remain an input to WI-016.

## Policy Ambiguities

| ID | Evidence | Required decision |
|---|---|---|
| POL-006-01 | Withdrawal contract only specifies soft deletion (`user-info.md:259-284`); payment docs define cancellation/reactivation but not account closure. | Future charging must stop. Decide whether withdrawal also deletes the provider billing key, how encrypted key/payment evidence is retained for disputes and legal obligations, and which audit event records the action. |
| POL-006-02 | Policy leaves two-person approval above a threshold unresolved (`payment-refund-receipt-settlement-policy.md:600-607`); controller/service require only ADMIN and do not compare request/approve/execute actors. | Decide maker-checker separation and amount thresholds before production refunds. This does not reduce PAY-006-07, which is an independent concurrency defect. |

## Focused Regression Test Inputs

### Backend tests to add

| Priority | Exact test input | Expected assertion | Finding |
|---|---|---|---|
| P0 | `UserWithdrawalBillingIntegrationTest.withdrawActiveSubscriberStopsRenewal` | After password withdrawal, subscription/agreement are non-renewable and a due renewal run makes zero provider calls. | PAY-006-01 |
| P1 | `BillingAgreementFailurePersistenceIntegrationTest.declinedInitialChargePersistsFailureAfterException` | Commit prepare first; mock a declined charge; reload in a new transaction; order is FAILED and the approved agreement cleanup/failure outcome is durable. | PAY-006-03 |
| P1 | `PaymentCommandConcurrencyMySqlTest.concurrentConfirmFinalizesOnce` | Two workers confirm one order; one provider intent and one finalized payment/order result. | PAY-006-04 |
| P1 | `PaymentCommandConcurrencyMySqlTest.concurrentUpgradeChargesOnce` | Two workers submit the same upgrade; at most one charge intent, one upgrade payment, and one plan transition. | PAY-006-04 |
| P1 | `RecurringRenewalConcurrencyMySqlTest.concurrentWorkersFinalizeOnePeriodOnce` | Existing renewal attempt processed by two workers yields one provider intent key and one local payment per agreement/period. | PAY-006-04 |
| P1 | `RecurringRenewalServiceTest.freshSubscriptionDoesNotReuseOldFailedRenewalOrder` | Old FAILED renewal plus a newly started subscription creates a new period order and does not expire the new subscription from the old grace date. | PAY-006-05 |
| P1 | `RecurringRenewalBatchIntegrationTest.failureOfSecondAgreementDoesNotRollbackFirstProviderSuccess` | First agreement local finalization remains committed when second decrypt/provider/local step throws; later items follow the approved continuation policy. | PAY-006-06 |
| P1 | `AdminPaymentRefundConcurrencyMySqlTest.concurrentReservationsCannotExceedOriginalAmount` | Concurrent refund requests cannot reserve more than the source payment. | PAY-006-07 |
| P1 | `PaymentControllerRoleIntegrationTest.adminCannotPrepareOrConfirmMemberBilling` | ADMIN direct request returns 403 and no order/agreement/payment row is created. | PAY-006-08 |
| P1 | `PaymentSettlementSchemaMySqlTest.allAuditEnumsFlushAndSettlementTransitionsCommit` | Fresh schema and upgrade patch both persist every action/target; import/reconcile/ignore commit with audit rows. | PAY-006-02, PAY-006-09 |
| P2 | `PaymentReconciliationServiceTest.scansBeyondFirstHundredWithWatermark` | An issue older than the latest 100 remains discoverable and scan completeness/watermark is reported. | PAY-006-11 |
| P2 | `BillingAgreementRecoveryIntegrationTest.issueSucceededEncryptionFailedIsRecoverable` | Failure after provider issue leaves a durable recovery task and no silent untracked key. | PAY-006-12 |
| P2 | `BillingAgreementRecoveryIntegrationTest.providerDeleteSucceededLocalCommitFailedIsRetryable` | Retry converges local/provider agreement state without treating already-deleted provider state as an unrecoverable error. | PAY-006-12 |
| P2 | `BillingKeyCryptoStartupTest.missingOrUnknownKeyVersionFailsAtStartup` | Missing secret and unsupported key ID fail before scheduler execution; rotation supports approved dual-read behavior. | PAY-006-14 |

### Frontend tests to add

| Priority | Exact test input | Expected assertion | Finding |
|---|---|---|---|
| P1 | `SubscriptionPlanPage.test.tsx` and `SubscriptionPaymentPage.test.tsx`: ADMIN direct URL/catalog CTA | ADMIN cannot enter checkout or trigger prepare; safe redirect/message is shown. | PAY-006-08 |
| P2 | `PaymentReadOnlyPage.test.tsx`: deferred promises for tab, page, and filters | Only the latest request updates visible rows, `pageInfo`, error, and loading state. | PAY-006-13 |
| P2 | Billing contract fixture test generated from current API schema/DTO | Prepare/confirm/my/cancel response fixtures match Java records and `payments.ts`. | PAY-006-10 |

### Existing focused suites to rerun after fixes

```powershell
gradlew.bat test --tests "com.atstudio.atstudio.service.BillingAgreementApplicationServiceTest" --tests "com.atstudio.atstudio.service.UserSubscriptionServiceTest" --tests "com.atstudio.atstudio.service.RecurringRenewalServiceTest" --tests "com.atstudio.atstudio.service.AdminPaymentRefundServiceTest" --tests "com.atstudio.atstudio.service.AdminPaymentEntitlementCorrectionServiceTest" --tests "com.atstudio.atstudio.service.AdminPaymentSettlementServiceTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationServiceTest" --tests "com.atstudio.atstudio.controller.PaymentControllerTest" --tests "com.atstudio.atstudio.controller.UserSubscriptionControllerTest"
```

```powershell
npm --prefix frontend run test -- src/pages/subscriber/SubscriptionPaymentPage.test.tsx src/pages/subscriber/SubscriptionManagePage.test.tsx src/pages/admin/PaymentReadOnlyPage.test.tsx
```

## MySQL and Provider-safe Verification

1. Use disposable MySQL 8 Testcontainers, never the live/local shared DB, for fresh `schema.sql`, ENUM flush, constraints, transaction reload, and concurrency tests.
2. Restore an anonymized/copy-only older schema baseline into a disposable database, apply every ordered migration, then run Hibernate `validate`. Do not apply patches to production from this WI.
3. On a copied staging dataset, run read-only `EXPLAIN` for:
   - due agreements by `(status,next_billing_at)`;
   - stale orders by `(status,expires_at)`;
   - latest reconciliation orders by `created_at`;
   - payments by `(payment_status,created_at)` and `pg_transaction_id`;
   - settlement fallback by order/payment key.
4. Use an in-process fake provider or MockWebServer/WireMock for all automated tests. Assert request count, order ID, amount, and idempotency key without storing raw billing keys in logs or fixtures.
5. Only in a separately approved Toss test environment, use test keys/test payment data to rehearse initial charge, retry, refund, already-deleted billing key, and lookup behavior. Do not use live keys, real card data, or real users.
6. Verify deployment configuration by presence/state only: exactly one scheduler instance, explicit JVM/business time zone, billing-key secret available, key-version/rotation plan, provider lookup configured, strict SQL mode known. Never print secret values.

## Commands and Outputs

Read-only commands executed from the workspace root:

- `git branch --show-current`, `git rev-parse HEAD`, `git status --short`
  - Result: baseline above; 34 pre-existing status entries before WI-006 output creation.
- `rg` / numbered `Get-Content` inspection across all handoff design, code, DDL, React, test, and operations pointers.
  - Result: contract matrix and evidence pointers above.
- Static entity/table comparator.
  - Result: `SCHEMA_COUNT=39`, `ENTITY_TABLE_COUNT=39`, no table-name delta.
- Static Java enum vs `schema.sql` comparator.
  - Result: missing actions `PAYMENT_SETTLEMENT_IMPORTED`, `PAYMENT_SETTLEMENT_RECONCILED`, `PAYMENT_SETTLEMENT_IGNORED`; missing target `PAYMENT_SETTLEMENT`.
- Static concurrency guard search over order/agreement/payment entities, repositories, and DDL.
  - Result: order ID unique exists, but no `@Version`, confirm/renewal row lock, or unique finalized payment per order/period was found.
- Manual migration inventory and Flyway/Liquibase search.
  - Result: two manual patches; no migration framework match.
- Frontend reconciliation consumer search.
  - Result: incident list/status consumers exist; no `/admin/payments/reconciliation` consumer exists, consistent with API-only on-demand operation.
- Test inventory search.
  - Result: relevant payment service tests are Mockito unit tests; no MySQL/Testcontainers or concurrency coverage in the inspected payment suites; no admin payment page test file.

No secret values were read into or written by this WI.

## Tests

- Executed: none.
- Reason: the handoff permits writes only to the two WI outputs. Gradle/Vitest would create or update build/cache artifacts, and DB/provider execution was explicitly forbidden.
- Inspected: existing JUnit and Vitest source listed under Focused Regression Test Inputs.
- Static checks completed: table parity, audit ENUM delta, migration inventory, concurrency-control search, route/API/DTO mapping, and test-gap inventory.

## Risks / Limitations / Rollback

- Runtime SQL mode, production schema baseline, data volume, replica count, JVM zone, secret state, ingress, and live Toss behavior were not available and are not claimed as verified.
- Static inspection proves reachable control flow and missing controls, but only MySQL concurrency/transaction tests can quantify lock and flush behavior.
- Current client docs were concurrent/untracked inputs. Their hashes are recorded above and must be rechecked if they change before WI-020 integration.
- Rollback: remove only these two newly created files, and only if explicitly requested:
  - `deliverables/user/WI-20260711-ATS-006-summary.md`
  - `deliverables/agent/WI-20260711-ATS-006-evidence-pack.md`

## Follow-ups

- WI-009 input: implement and run the backend/MySQL regression tests above, beginning with PAY-006-01 through PAY-006-09.
- WI-016 input: review payment/security remediation, provider-local recovery architecture, and the out-of-scope original-audio access findings without duplicating them into this payment matrix.
- Architecture decision required before implementation: command idempotency/claim model, renewal period identity, withdrawal billing-key lifecycle, migration baseline, and refund maker-checker policy.
