---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: qa-integ
category: evidence-pack
status: confirmed
---

# Evidence Pack: WI-20260908-ATS-016

## Summary

Completed the bounded REQ-20260908-ATS-004 integration review of main `8161f0a00b088001a37962da719c3ace8fea44ad`: **two P1 release blockers confirmed by static code tracing; no P0 found**. Neither defect was executed against a database or Provider. Review completion is not implementation, test PASS, or production GO.

## Evidence Pointers

### F1 - P1: Returning subscribers can be charged but never finalized

- Trigger: an authenticated USER retains an expired `UserSubscription` row and a CANCELLED/EXPIRED/SUSPENDED agreement, then purchases again through normal SUBSCRIBE checkout and the Provider charge succeeds.
- Entry/contract: `docs/design/api-spec.md:698-706` authorizes SUBSCRIBE when no service-enabled subscription exists. `frontend/src/pages/public/SubscriptionPlanPage.tsx:162-171,220-225` treats authoritative absence as a new checkout. `src/main/java/com/atstudio/atstudio/service/BillingAgreementPrepareTransactionService.java:111-151,266-285` excludes expired access, resets a non-active agreement, and creates the full-price order.
- Cause: `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:527,552,1319-1324` loads ANY retained subscription and rejects every non-null row. `src/main/java/com/atstudio/atstudio/repository/UserSubscriptionRepository.java:56-58` has no status/expiry filter. The intended row-reuse branch at `PaymentCommandTransactionService.java:576-581` is consequently unreachable for this purchase.
- Impact: `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java:287-346` already charged and durably recorded success before that rejection. No new payment ledger/access is finalized. `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx:205-220` correctly stays UNKNOWN instead of granting success.
- Defenses checked: ownership/customer/amount checks and active-subscription exclusion at `PaymentCommandTransactionService.java:180-212,1384-1420` do not reject an expired row before charging. Durable success prevents recharge, but confirm replay hits the same finalizer; reconciliation also uses the same rejection at `1298-1302`. Unique user ownership of the retained row is intentional (`entity/UserSubscription.java:23-25`), not corrupt data.
- Existing test blind spot: `src/test/java/com/atstudio/atstudio/service/PaymentProviderSuccessRecoveryIntegrationTest.java:55-100` proves rollback/retry for a fresh user only; its fixture at `BillingAgreementCommandIntegrationTestSupport.java:93-130` creates no prior subscription. `BillingAgreementChargeTimestampIntegrationTest.java:133-159` reuses that fresh fixture even when simulating a prior charge timestamp.
- Bounded correction direction, not implemented: distinguish a legitimate expired source aggregate from an active/replaced/cancelled-in-flight aggregate; preserve historical rows and validate the same source lifecycle before charge and finalize-only recovery. Do not merely remove all finalization guards.

### F2 - P1: Different next-cycle selections bypass an unresolved upgrade

- Trigger: for one active paid period, request upgrade to the same higher plan with MONTHLY, pause its Provider call, then submit the same plan with YEARLY from another tab/API before the first finalizes. An unresolved/unknown first response plus another context can reach the same defect.
- Cause: `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:100-150,823-849` fences only the exact target-plan/next-cycle key; it does not reject another unresolved command on that aggregate. Both requests see the old plan because claim commits before Provider execution. `src/main/java/com/atstudio/atstudio/service/PaymentCommandKeyFactory.java:50-62,81-84` creates different command and Provider-attempt identities for those cycles/orders.
- Impact: `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java:329-398` sends two charges for the same current-period price difference. Each can return its own valid Provider transaction. Finalization at `PaymentCommandTransactionService.java:635-663,1015-1028` checks linkage but not the originally priced plan/period generation, so both can become DONE; the last pending cycle wins and the user pays the difference twice.
- Frontend/defenses checked: `frontend/src/pages/subscriber/SubscriptionManagePage.tsx:377-381,580-582,734-790` has explicit preview confirmation, same-view duplicate prevention, and UNKNOWN locks. Those refs do not span tabs/reloads/API requests. Ordered database locks serialize each short claim, not the whole Provider lifecycle. Same-command and same-Provider-transaction uniqueness cannot collapse two different orders with distinct legitimate transactions.
- Existing test blind spot: `src/test/java/com/atstudio/atstudio/service/SubscriptionUpgradeCommandIntegrationTest.java:244-282` submits MONTHLY twice and verifies one charge; `216-239` also retries only the identical tuple. Neither exercises different cycles/targets while another upgrade is unresolved.
- Bounded correction direction, not implemented: fence competing monetary intents on the same subscription while an outcome is unresolved and validate the priced source state on completion. Preserve the approved distinction between current YEARLY entitlement and next MONTHLY cadence; this does not require a new price/quote policy or distributed-server design.

## Scope / DoD Check

- [x] Read current policy, primary application/transaction/provider code, frontend call sites, and test assertions; challenged findings against existing defenses.
- [x] Covered price/owner/provider authority, command identity, partial failures, callbacks, renewal/cancel/reactivate, refunds/access, and current-period/next-cycle semantics.
- [x] Kept only concrete blockers; test gaps are attached to their defect instead of duplicated as findings. No separate maintenance or optional cleanup item is required here.
- [x] Wrote only this evidence pack and `deliverables/user/WI-20260908-ATS-016-summary.md` using apply_patch; no source/test/config edits or subdelegation.
- `PaymentService.java` is absent at this baseline. `controller/PaymentController.java:38-40,61-66` delegates to `BillingAgreementApplicationService`, `PaymentCommandTransactionService`, and `PaymentRecoveryReadService`; those current paths were reviewed instead of assuming an obsolete service exists.

## Reference Documents (Tier 0-2)

| Tier | Injected document / relevant content | Use |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md:64-80,155-203` | Approved bounded execution, transparency, financial traceability |
| 0 | `docs/standards/documentation-standards.md:45-61,288-362` | Metadata, concise English artifacts, relative document links |
| 0 | `docs/standards/development-standards.md:112-155,278-303,788-833` | Layer boundaries, security and honest test evidence |
| 0 | `docs/standards/glossary.md:24-96` | Canonical WI, REQ and domain vocabulary |
| 1 | `docs/policies/security-policy.md:203-214,268-298`; `docs/policies/quality-gates.md:27-56` | Listening/download policy, Provider evidence privacy, criticality |
| 2 | `docs/index.md:76-81`; `docs/SR/SR-93.md:57-75,175-208` | Current source baseline and OPEN production gates |
| 2 | `docs/design/api-spec.md:628-709`; `docs/design/payment-integration-design.md:23-37,104-117,185-242,244-408` | Authoritative checkout, lifecycle and recovery contracts |
| 2 | `docs/design/payment-refund-receipt-settlement-policy.md:65-75,125-161,197-240`; `docs/design/p1-payment-integrity-remediation-design.md:70-112` | Refund/access separation, command and finalization invariants |
| 2 | [Approved REQ004](../user/REQ-20260908-ATS-004.md); [WI016 handoff](WI-20260908-ATS-016-handoff.md) | Assignee, bounded read-only scope, outputs and WI018 dependency |

Injection applied: ordered pointers in the existing handoff; role `qa-integ`; task types review/security/testing; Tier 0 followed by task-relevant Tier 1/2. Skills used: `.agents/skills/create-wi-evidence-pack/SKILL.md` and `vercel-react-best-practices` (`async-parallel`, `rerender-dependencies`). Existing parallel reads and request-generation guards were considered; no performance redesign was introduced.

## Three-Way Doc / Code / Test Map

Path prefixes in this table: `S=src/main/java/com/atstudio/atstudio/service/`, `E=src/main/java/com/atstudio/atstudio/entity/`, `T=src/test/java/com/atstudio/atstudio/service/`, `F=frontend/src/pages/subscriber/`. All test observations below are **source inspection, not execution**.

| Requirement / document | Implementation evidence | Test substance / boundary |
| --- | --- | --- |
| Server price, purpose, audience and owner: api-spec 698-709 | `S/BillingAgreementPrepareTransactionService.java:111-151,239-264`; `S/PaymentCommandTransactionService.java:180-212,1367-1420`; `F/SubscriptionPaymentPage.tsx:351-395,785-810` | `T/BillingAgreementPrepareToConfirmIntegrationTest.java:204-249` owner separation; `SubscriptionPaymentPage.test.tsx:575,746` exact plan/zero-amount gates. F1 is the missing returning-user completion case. |
| Durable success/finalize-only: payment-integration-design 388-408 | `S/BillingAgreementApplicationService.java:336-346`; `S/PaymentCommandTransactionService.java:377-403,521-605` | `T/PaymentProviderSuccessRecoveryIntegrationTest.java:55-100` checks persisted success, rollback of ledger/access, then one-charge recovery. F1 still fails for retained expired rows. |
| Upgrade charge before access: payment-integration-design 217-232 | `S/UserSubscriptionService.java:324-398`; `S/SubscriptionUpgradePaymentExecutor.java:26-36` | `T/SubscriptionUpgradeCommandIntegrationTest.java:121-175,216-282` durable recovery and exact-tuple contention; F2 is the cross-tuple gap. |
| Annual period retained, MONTHLY reserved: payment-integration-design 227-232 | `S/UtilService.java:173-200`; `E/UserSubscription.java:73-81`; `S/PaymentCommandTransactionService.java:660-663,786-801`; `F/SubscriptionManagePage.tsx:1418-1447` | `T/UtilServiceTest.java:232-268` verifies annual difference and next monthly amount/date; `F/SubscriptionManagePage.test.tsx:1027-1079` verifies retained period, explicit consent and one click action. Static policy agreement, excluding F2. |
| Failed callback is untrusted: payment-integration-design 261-292 | `F/SubscriptionPaymentPage.tsx:78-101,134-143,197-307`; `S/PaymentRecoveryReadService.java:34-62` | `F/SubscriptionPaymentPage.test.tsx:917-995,1035-1098` verifies bounded hint, zero mutations and DONE precedence; replay test 92 verifies early URL scrubbing. No false failure inferred from URL. |
| Registration is not a charge: payment-integration-design 199-215 | `E/BillingAgreement.java:142-187`; `S/PaymentCommandTransactionService.java:748-765,1142-1148` | `T/BillingAgreementChargeTimestampIntegrationTest.java:86-159` tests prior/null timestamps, prepare failure, cleanup, positive charge and replay. Earlier timestamp concern is fixed in current code. |
| Renewal command/retry/grace: SR-93 187-195 | `S/RecurringRenewalService.java:94-115,162-235`; `S/PaymentCommandTransactionService.java:237-355,472-518,672-724`; `S/SubscriptionScheduler.java:58-98` | `T/RecurringRenewalCommandIntegrationTest.java:265-346,351-403,406-452` checks three attempts, one order, no same-day replay, grace/download boundary and unknown hold. Scheduled wall-clock execution is not proved by these direct invocations. |
| Cancel preserves paid expiry; explicit reactivation: payment-integration-design 234-242,316-335 | `S/UserSubscriptionService.java:196-217,241-257`; `E/BillingAgreement.java:247-250`; `F/SubscriptionManagePage.tsx:1483-1517` | `T/UserSubscriptionServiceTest.java:691-739,748-763` stored-key requirement and unchanged expiry; `F/SubscriptionManagePage.test.tsx:983-1024` price availability gate. These do not establish cross-request lifecycle serialization. |
| Refund amount reservation, stable key, access unchanged: refund policy 125-161,197 | `S/AdminPaymentRefundService.java:92-123,149-207,257-272`; `S/PaymentRefundTransactionService.java:64-105,196-214,264-314` | `T/PaymentRefundResilienceIntegrationTest.java:188-274,321-370,458-520` exact-command replay, stale-result rejection, elapsed replay ceiling and reservation contention. Provider is a fake, not financial proof. |
| Explicit refund-linked entitlement correction: refund policy 199-240 | `S/AdminPaymentEntitlementCorrectionService.java:119-159,196-258,289-378` | `T/AdminPaymentEntitlementCorrectionServiceTest.java:162-174,213-281,396-438` rejects pending refund, stale state and unresolved payments; applies explicit local target and idempotent replay. Repository locking is mocked here. |
| Card-only Provider and exact recovery evidence: payment-integration-design 23-53,401-409 | `S/payment/provider/recurring/TossBillingProvider.java:38-39,273-291,360-391,449-483`; `S/PaymentReconciliationTransactionService.java:231-259` | `T/payment/provider/recurring/TossBillingProviderTest.java:164-255,364-418,488-537` captures amount/key and rejects mismatched/missing evidence via a local HTTP stub. No Toss availability, live response or credential validation. |

Free full-song streaming, paid/quota downloads, card-only recurring and single-server operation remain approved policies, not findings. File/download authorization and deployment/dependencies belong to WI017/MA, respectively.

## Commands & Outputs

- Read-only commands: targeted `rg`, numbered `Get-Content`, `Test-Path`, structured package-script read, `git rev-parse HEAD`, `git status --short --branch --untracked-files=no`. HEAD matches the full SHA above; tracked baseline was clean. Historical untracked deliverables were neither enumerated wholesale nor altered.
- Two report files were absent before creation. The only write mechanism used was apply_patch for those files. Report validation covers line limits, referenced path/line bounds, relative links, whitespace and unchanged tracked files.
- No Gradle/npm, test process, application HTTP, Provider/SMTP, credential file, Git write, runtime restart or database operation was performed. Early P1 warning was posted in task commentary; `send_input` was not available in this toolset. No live containment mutation was attempted.

## Tests / MA Candidates

MA-reported fresh safe H2 suite, corrected from its fresh XML: **1,708 total / 1,689 passed / 19 skipped / 0 failed**; skips are **18 environment-gated MySQL tests + 1 LocalStorage platform test**, not 19 MySQL tests. MA also reports frontend **112 files / 1,493 tests PASS in 120.35s** and independently confirms the F1/F2 claim/finalizer source chains. This reviewer neither ran these suites nor independently inspected their results. MA reports no payment/mail/real-database actions. These current local results are distinct from historical Toss TEST acceptance and do not close the missing F1/F2 cases identified in the inspected tests. WI017 findings remain with MA and are not duplicated here.

Commands below are handback candidates only. Run serially from the root except the frontend command. These are local unit/JPA slices with test doubles; keep embedded test-database replacement enabled. Do not add `*Mysql*` classes, external profiles, live keys or production database overrides.

```powershell
.\gradlew.bat test --tests "com.atstudio.atstudio.service.PaymentProviderSuccessRecoveryIntegrationTest" --tests "com.atstudio.atstudio.service.SubscriptionUpgradeCommandIntegrationTest" --tests "com.atstudio.atstudio.service.BillingAgreementPrepareToConfirmIntegrationTest" --tests "com.atstudio.atstudio.service.BillingAgreementChargeTimestampIntegrationTest"
.\gradlew.bat test --tests "com.atstudio.atstudio.service.RecurringRenewalCommandIntegrationTest" --tests "com.atstudio.atstudio.service.PaymentRefundResilienceIntegrationTest" --tests "com.atstudio.atstudio.service.AdminPaymentEntitlementCorrectionServiceTest" --tests "com.atstudio.atstudio.service.UserSubscriptionServiceTest" --tests "com.atstudio.atstudio.service.UtilServiceTest" --tests "com.atstudio.atstudio.service.SubscriptionSchedulerTest" --tests "com.atstudio.atstudio.service.payment.provider.recurring.TossBillingProviderTest"
```

From `frontend/`: `npm test -- src/pages/subscriber/SubscriptionPaymentPage.test.tsx src/pages/subscriber/SubscriptionPaymentReplay.test.tsx src/pages/subscriber/SubscriptionManagePage.test.tsx src/pages/public/SubscriptionPlanPage.test.tsx`.

- F1 new regression candidate, not written: persist a normal expired subscription and cancelled agreement; real prepare -> confirm -> fake successful charge -> finalize -> replay. Assert one retained/restarted aggregate, one new ledger, correct period, one charge; include successful-Provider/local-failure recovery with history present and rejection when the source became active meanwhile.
- F2 new regression candidate, not written: block the first fake charge, submit the same target with another next cycle, then release. Give each potential Provider call a DISTINCT transaction ID so uniqueness cannot mask a second charge. Assert one accepted monetary intent and no double difference; also hold the first result UNKNOWN before changing tuple.
- Dated test counts, prior MySQL proofs and historical Toss TEST acceptance are preserved as historical evidence, not new execution or production validation. Mock clocks do not prove midnight scheduling; H2 locks do not prove MySQL interleavings. No coverage target or new live rehearsal is requested by this WI.

## Risks / Rollback

- F1/F2 block a clean payment-readiness recommendation pending separately scoped correction and regression verification. This report contains static reproducible paths, not observed customer incidents, exploit results or deployed-production findings.
- No runtime rollback is needed because no product state changed. Reversal, if later requested, affects only these two new reports; preserve all other files and payment history.

## Follow-ups

WI016 is complete and hands its two findings/test candidates to MA's existing WI-20260908-ATS-018. Do not close REQ004, modify the handoff, create another WI or subdelegate from this reviewer. Operational/dependency review and central test execution remain with MA; SR-93 target-dependent gates remain OPEN.

## Related Documents

- [WI016 Summary](../user/WI-20260908-ATS-016-summary.md)
- [Approved Review Scope](../user/REQ-20260908-ATS-004.md)
