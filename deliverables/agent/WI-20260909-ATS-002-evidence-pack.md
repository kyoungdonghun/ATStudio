---
version: 1.1
last_updated: 2026-09-09
project: ATS
owner: se
category: evidence-pack
status: verification-pending
---

# Evidence Pack: WI-20260909-ATS-002

## Summary

Implemented PAY-01 returning-purchase source binding and PAY-02 competing upgrade fences using existing persisted fields, plus the WI011 F1 local-mutation correction. Product/test source is stable; follow-up execution and WI011 re-review remain pending. This is not a test PASS or deployment approval.

## Scope / DoD Check

- [x] Approved REQ-20260909-ATS-001 and WI-20260909-ATS-002-handoff.md followed.
- [x] Owned product/test files edited directly with apply_patch; two-set reports produced.
- [x] Historical rows, current-period pricing, next-cycle policy and Provider attempt identity retained.
- [x] No new fields, schema changes, Git commands, runtime actions, real DB or Provider actions.
- [x] Deterministic fake-Provider/H2 regression scenarios authored.
- [x] WI011 F1: local writers lock before reading; unresolved monetary commands fence local mutations in the opposite interleaving.
- [ ] MA executes focused and compatibility tests; original-defect red evidence not executed here.
- [ ] WI011 independent payment review and aggregate quality gates.

## Reference Documents (Tier 0-2)

| Tier | Input | Use |
| --- | --- | --- |
| 0 | docs/standards/core-principles.md | Injected summary and live source; scoped autonomy, financial traceability, preserved data |
| 0 | docs/standards/documentation-standards.md | Metadata, exact evidence, no historical rewrite |
| 0 | docs/standards/development-standards.md | Java 17, service transactions, focused independent regression |
| 0 | docs/standards/glossary.md | Existing entitlement definitions remain unchanged |
| 1 | docs/policies/security-policy.md; docs/policies/quality-gates.md | No secret inspection, no quality weakening |
| 2 | deliverables/user/REQ-20260909-ATS-001.md | Approved scope and downstream chain |
| 2 | deliverables/agent/WI-20260909-ATS-002-handoff.md | Ownership, no runners/subdelegation/runtime actions |
| 2 | deliverables/agent/WI-20260908-ATS-016-evidence-pack.md | F1/F2 source traces, prior test blind spots |
| 2 | deliverables/agent/WI-20260908-ATS-018-findings.md | PAY-01/02 closure requirements |
| 2 | deliverables/agent/WI-20260909-ATS-011-evidence-pack.md:48 | Independent F1 counterexample and initial MA test evidence; historical review, not follow-up acceptance |
| Skill | .agents/skills/create-wi-evidence-pack/SKILL.md | Handoff prerequisite checked; this report generated using required structure |

Assignee: se; task type: implementation. User supplied Tier 0 injection; the listed live documents and relevant code were inspected. No subagent was invoked. The previous evidence's API-contract references were context, not a new external/API validation.

## Exact Changed Files

All paths below are relative to C:/Users/jm991/Desktop/project/ATStudio.

| File | Change / Finding |
| --- | --- |
| src/main/java/com/atstudio/atstudio/service/PaymentCommandKeyFactory.java | Immutable source suffix and comparison, PAY-01/02 |
| src/main/java/com/atstudio/atstudio/service/BillingAgreementPrepareTransactionService.java | Lock/link retained source and bind returning purchase; descriptor/replay source validation, PAY-01 |
| src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java | Claim/finalize/reconciliation source guards; unresolved competing upgrade fence; eliminate pre-lock subscription preload, PAY-01/02 |
| src/main/java/com/atstudio/atstudio/repository/PaymentOrderRepository.java | Locking unresolved upgrade/local-mutation monetary queries; escaped bounded command-family lookup and ambiguity rejection, PAY-01/02/F1 |
| src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java | Agreement-before-subscription locks and current-state validation for local mutations; unresolved monetary busy guard, WI011 F1 |
| src/test/java/com/atstudio/atstudio/service/PaymentReturningSubscriberIntegrationTest.java | New real prepare/confirm/finalize H2 scenarios, PAY-01 and command-family ambiguity |
| src/test/java/com/atstudio/atstudio/service/SubscriptionUpgradeCommandIntegrationTest.java | Distinct-cycle/target concurrency, UNKNOWN/durable recovery, source changes, legacy/DONE replay, PAY-02 |
| src/test/java/com/atstudio/atstudio/service/PaymentCommandKeyFactoryTest.java | Source price-scale normalization and 191-character storage bound |
| src/test/java/com/atstudio/atstudio/service/BillingAgreementCommandIntegrationTestSupport.java | Pure fake prepare and per-order fake charge responder; thread-safe call collection |
| src/test/java/com/atstudio/atstudio/service/PaymentReconciliationRecoveryIntegrationTest.java | Synthetic upgrade fixture now carries actual source-bound identity instead of an arbitrary key |
| src/test/java/com/atstudio/atstudio/service/SubscriptionMutationFenceIntegrationTest.java | New independent H2 interleavings, legacy stale-write control, held fake Provider/UNKNOWN busy fences and terminal behavior, WI011 F1 |
| src/test/java/com/atstudio/atstudio/service/UserSubscriptionServiceTest.java | Mutation fixtures use locking repository methods; ordered-lock/no-unlocked-read assertions, WI011 F1 |
| deliverables/agent/WI-20260909-ATS-002-evidence-pack.md | This report |
| deliverables/user/WI-20260909-ATS-002-summary.md | User-facing summary |

Total: five product files, seven test files and these two reports. BillingAgreementApplicationService.java, PaymentOrder.java, other product sources, other agents' reports and runtime configuration were not edited.

## Implementation Evidence

- PAY-01 root cause: preparation excluded expired service access, whereas finalization rejected every retained subscription row, making legitimate full-price returning purchase unrecoverable after Provider success.
- BillingAgreementPrepareTransactionService.java:104,160,201 now locks the retained row, derives service-enabled intent, links that row, and checks the same source during prepare replay/descriptor finalization. Fresh users and zero-charge registration retain their previous command formats.
- PaymentCommandTransactionService.java:173,533,1339 validates the locked returning source before claim and again before finalization. Reconciliation uses the same source guard; a fresh order also detects a subscription added after its claim.
- PaymentCommandKeyFactory.java:53 binds the existing request identity to a SHA-256 source suffix. Input includes subscription/user/plan IDs, current cycle/status, period dates, and normalized source-plan monthly/yearly prices. It contains no raw request key, credentials, Provider payload or PII.
- Price scale is canonicalized with stripTrailingZeros/toPlainString, so 9900 and persisted 9900.00 match. Actual source tariff, plan, cycle, period or status changes fail closed. Pending future choices are not pricing-source inputs; current-period/future-cycle policy is unchanged.
- PAY-02 root cause: distinct target/cycle keys claimed separately while the first Provider call was outside the local transaction. Finalization did not check the original priced source.
- PaymentCommandTransactionService.java:71 locks agreement first, then the current subscription, then unresolved orders. It rejects every other unresolved upgrade on that subscription, including older unresolved periods. READY, IN_PROGRESS, PROCESSING, PENDING_PROVIDER_CONFIRMATION and PROVIDER_SUCCEEDED remain fences.
- The same PROVIDER_SUCCEEDED command enters finalize-only before recalculating today's proration. Finalization compares its complete source-bound key and the charged cycle before changing entitlement. Existing Provider-attempt keys and definitive-failure retry numbering remain unchanged.
- PaymentCommandTransactionService.java:1021 preserves the DONE replay path without checking a later source generation or reapplying an upgrade; existing order/payment validation remains in place.
- PaymentOrderRepository.java:49,82 uses exact-key OR escaped literal SOURCE-prefix matching, preserving request-level recovery. Candidate lists are capped at two and explicitly reject ambiguity with a bounded DataIntegrityViolationException. Neither query requests a JPA single result, selects a random candidate, nor creates another command after ambiguity.
- Agreement locks serialize normal same-request creation. The exact persisted-key DB uniqueness constraint is unchanged; it alone does not enforce family uniqueness across manually introduced different suffixes. The explicit ambiguity guard covers that boundary.

## WI011 F1 Follow-up: 2026-09-09

- The initial WI011 review identified cancellation-read -> paid-upgrade-commit -> cancellation-commit: a stale managed entity could restore Basic despite the Premium DONE ledger. Earlier source-change-before-finalize tests did not exercise this ordering; their historical success does not close F1.
- UserSubscriptionService.java:125,201,217 now locks the agreement before first loading the active subscription with a write lock. Cancel, reactivate, clear-pending, scheduled change and zero-charge upgrade reread/revalidate current state under this protocol; expiry and no-refund semantics are unchanged.
- Locks alone would allow cancellation during an out-of-transaction Provider call to alter the source and strand later paid finalization. UserSubscriptionService.java:239 therefore checks PaymentOrderRepository.java:107 using a current locking query after agreement/subscription locks and before any local mutation.
- The guard covers SUBSCRIBE/UPGRADE/RENEWAL in READY, IN_PROGRESS, PROCESSING, PENDING_PROVIDER_CONFIRMATION and PROVIDER_SUCCEEDED. It returns existing PAYMENT_ORDER_INVALID_STATE, does not alter the order, and excludes registration-only BILLING_AGREEMENT. Terminal FAILED/CANCELLED/EXPIRED/DONE do not block.
- The charged-upgrade planning branch remains mutation-free and delegates to the existing command fence, preserving exact PROVIDER_SUCCEEDED finalize-only retries. A different charged target/cycle remains rejected there. Ordinary cancellation/reactivation after DONE still works without a Provider cancellation/refund.
- The new integration suite has ten cases. Its legacy control intentionally demonstrates the old full-row overwrite; four actual-service mutations delay before source loading, let a separate finalizer commit, then assert paid-tier/period preservation and current-state decisions. Transaction resource identities, commit order and subscription UPDATE columns are asserted, not assumed.
- Opposite-order coverage pauses cancellation after locked source loading, then starts a waiting finalizer: cancellation must reject busy and release its locks so finalization completes. Held fake-Provider success/UNKNOWN cases reject local mutations before success and after UNKNOWN; authoritative synthetic recovery then finalizes once. Exact finalize-only retry and definitive-failure cancellation are separate cases.
- These are authored deterministic regressions, not an executed SQL trace or original-product red run. WI011 must re-review the latest correction after MA execution; F1 is not declared closed here.

## Commands And Outputs

- Executed: focused rg searches and Get-Content reads of named guidance, source and tests; apply_patch edits to the exact files above.
- Static observations: source-bound key length explicitly capped at 191; source price normalization visible in code; old exact and new prefixed lookup paths retained; local lock ordering inspected.
- NOT executed by this assignee: Gradle, npm, JUnit, H2, MySQL, Provider operations, original-code red run, browser/account actions, Git commands, deployment or restart. No new test logs are claimed.
- Historical initial execution reported by WI011: output/release-remediation-20260909/payment-initial-results.json and payment-initial.log, seven suites/75 cases, 72 passed and three guarded MySQL skips. This is attributed review evidence, not independently rerun here and not evidence for the subsequently added F1 correction.
- MA received focused-test readiness before report authoring. Source/test paths are stabilized for downstream execution.
- MA subsequently announced the full patched BE build start; Java product/tests are frozen for that run. No outcome was available at this report checkpoint. Static report checks: 157/35 lines, required metadata present, zero trailing whitespace and ASCII-only content.

## Tests: Authored; Follow-up Execution Pending

| Class / Pointer | Scenarios |
| --- | --- |
| PaymentReturningSubscriberIntegrationTest.java:59 | Expired ACTIVE/CANCELLED/EXPIRED history, full-price purchase, one reused subscription row, retained previous ledger, prepare/DONE replay |
| PaymentReturningSubscriberIntegrationTest.java:82,104 | Durable Provider success after local rollback/checkout expiry; UNKNOWN reconciliation without recharge |
| PaymentReturningSubscriberIntegrationTest.java:120,135,152 | Active or also-expired replacement before/after charge; cancelled agreement still blocked |
| PaymentReturningSubscriberIntegrationTest.java:166,180 | No unsafe adoption of legacy history; two snapshots for one request reject explicitly without extra order/Provider call |
| SubscriptionUpgradeCommandIntegrationTest.java:289 | Independent transaction contexts while first Provider call waits; another cycle or target rejected; fake transaction ID derives from each distinct order |
| SubscriptionUpgradeCommandIntegrationTest.java:337,362 | UNKNOWN and durable success fence competitors; finalize-only recovery retains YEARLY current period and MONTHLY pending cycle |
| SubscriptionUpgradeCommandIntegrationTest.java:398 | Changed source plan, period, cycle, price or status cannot finalize paid success against the replacement |
| SubscriptionUpgradeCommandIntegrationTest.java:426,443 | Legacy DONE replay after later expiry; unbound READY/IN_PROGRESS/PROCESSING/PROVIDER_SUCCEEDED/UNKNOWN remain unchanged and uncharged |
| PaymentCommandKeyFactoryTest.java:39 | Price scale normalization, changed price mismatch, maximum ID key length |
| PaymentReconciliationRecoveryIntegrationTest.java:475 | Existing recovery fixture uses the strengthened source contract |
| SubscriptionMutationFenceIntegrationTest.java:148,169,326,478 | Legacy stale overwrite control; actual cancel/reactivate/pending/clear interleavings, separate transaction resources, commit order and UPDATE capture |
| SubscriptionMutationFenceIntegrationTest.java:198,228 | Local locks acquired before finalizer; held fake Provider and UNKNOWN reject mutations until DONE |
| SubscriptionMutationFenceIntegrationTest.java:286,298 | Exact durable-success finalize-only retry; definitive failure permits ordinary cancellation without refund |
| UserSubscriptionServiceTest.java:617,665,713,808 | Updated unit fixtures plus ordered agreement/subscription lock assertions; no ordinary subscription read in mutation paths |

Proposed first follow-up command, MA only, from repository root (full BE build may include these):

```powershell
./gradlew.bat test --tests '*SubscriptionMutationFenceIntegrationTest' --tests '*UserSubscriptionServiceTest'
```

Core compatibility rerun, MA only:

```powershell
./gradlew.bat test --tests '*PaymentReturningSubscriberIntegrationTest' --tests '*SubscriptionUpgradeCommandIntegrationTest' --tests '*PaymentCommandKeyFactoryTest' --tests '*PaymentReconciliationRecoveryIntegrationTest'
```

Proposed compatibility command, MA only, separate serialized run:

```powershell
./gradlew.bat test --tests '*BillingAgreementPrepareIdempotencyIntegrationTest' --tests '*BillingAgreementPrepareToConfirmIntegrationTest' --tests '*PaymentProviderSuccessRecoveryIntegrationTest' --tests '*PaymentCommandIndependentVerificationIntegrationTest' --tests '*PaymentRecoveryReadIntegrationTest' --tests '*RecurringRenewalCommandIntegrationTest' --tests '*BillingAgreementCancellationTransactionIntegrationTest' --tests '*BillingAgreementChargeTimestampIntegrationTest'
```

Do not include BillingAgreementPrepareMysqlConcurrencyIntegrationTest or opt into any real-database profile under this WI. H2 execution, when performed by MA, will not establish MySQL interleaving or production correctness.

## Rollout Boundary And Unresolved Disposition

- New source-bound commands need no DDL. Do not rewrite existing command keys or invent historical source snapshots.
- Old unbound unfinished UPGRADE orders, including READY/IN_PROGRESS, PROCESSING, UNKNOWN and PROVIDER_SUCCEEDED, fail closed for claim/finalization under the strengthened guard. They also fence competing upgrade attempts. Definitively FAILED unbound retries similarly cannot acquire an invented source.
- Old unbound SUBSCRIBE orders can continue only when the original no-subscription invariant still holds. They cannot adopt a retained expired row. A previously charged returning order lacking source evidence remains a reviewed remediation boundary, not an automatic repair.
- Zero-charge BILLING_AGREEMENT behavior and legacy DONE replay remain compatible. DONE replay does not bind a new source, mutate history or charge again.
- Before deployment, arrange a separately approved admission pause/inventory/drain/reconciliation of old-format monetary commands. READY without an attempted charge may use established expiry/cancellation controls; PROCESSING/UNKNOWN/paid success require authoritative evidence and existing reviewed recovery/remediation. PAY-01 legacy paid returns may require explicit remediation rather than the old defective finalizer. No such operation was performed here.
- Do not deploy mixed old/new command writers: old exact-key lookup cannot understand new bound keys, and old writers do not enforce the competing-intent fence. The same admission/drain boundary applies before a rollback to the old implementation.
- Out-of-band/admin changes to a bound source's status or tariff while unresolved still require review rather than repricing or attaching a replacement lifecycle. Normal local cancellation/reactivation/pending mutations now reject busy before changing it. No automatic historical snapshot retrofit is authorized.
- Multiple source snapshots for one identity are an explicit integrity error, not a normal replay or a not-found invitation to charge. The response is fail-closed, not a new customer-facing recovery UX.
- No unresolved DDL or policy choice is needed for prospective implementation. Legacy-order rollout disposition and MA test/review acceptance remain open.

## Risks / Rollback / Handoff

- Source guards use existing logical state, not a new monotonic generation column; an out-of-band actor restoring exactly identical fields is outside the evidence supplied here. No data-mutation workaround was introduced.
- Rollback: isolate admissions and resolve newly bound in-flight commands before reverting only these scoped source/test changes through MA's approved change process. Never strip suffixes, purge orders/ledgers, or blindly revert unrelated dirty files. No rollback command was executed.
- WI002 blocks WI011, WI006, WI013 and WI014 per the current handoff. MA should run the stabilized tests/full build, return this correction to WI011 for re-review, then propagate verified evidence to the remaining gates. This assignee does not subdelegate or close the REQ.
