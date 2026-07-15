[WI HEADER]
WI ID: WI-20260715-ATS-002
REQ: REQ-20260714-ATS-001
Agent: se
Depends On: WI-20260715-ATS-001
Blocks: WI-20260715-ATS-004, WI-20260715-ATS-005, WI-20260715-ATS-006, WI-20260715-ATS-007

[WI SUMMARY]
Why: Close F-01 and establish canonical payment-command locks/finalizers for later cleanup, upgrade, and reconciliation packages.
Scope (in): Design Package B only: renewal command identity and retry scheduling, exact due/candidate repository contracts, canonical agreement/order/subscription/payment locks, persisted upgrade target cycle at order creation, reconciliation-safe provider-success/finalize APIs, and B-owned cleanup/stale projections consumed by Package C.
Scope (out): Cancellation/withdrawal orchestration, charged-upgrade outer transaction split, refund recovery, reconciliation provider lookup/orchestration, MySQL execution, and live provider calls.
DoD: Same logical billing period keeps one order and command across different retry dates; retries increment attempt identity without moving `nextBillingAt`; pending/processing ambiguity does not trigger a new charge; all B-owned locks follow the approved order; focused tests and two-set deliverables pass.
Constraints/Forbidden: Do not edit Package C/D/E/F production services. Do not call live Toss or mutate any retained DB. Preserve Package A contracts and unrelated changes. You are not alone; do not revert other work.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Due selection uses immutable `nextBillingAt` as period start and `renewalRetryAt` only as a retry gate.
- [ ] A retry on a later date reuses the exact renewal order/command and advances only provider attempt identity.
- [ ] Exact purpose/period/target-cycle data is persisted and validated before provider claim/finalization.
- [ ] Repository projections provide Package C cleanup/stale candidates without Package C editing repository files.
- [ ] Canonical lock order is used by every B-owned multi-row phase.
Performance:
- [ ] Candidate queries match Package A indexes and are bounded/batched as before.
Quality:
- [ ] Focused renewal, command, recovery, repository, and static contract tests pass.
- [ ] Java compile and `git diff --check` pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
REQ/Context:
- deliverables/user/REQ-20260714-ATS-001.md
- docs/design/p1-payment-integrity-remediation-design.md
- deliverables/agent/WI-20260714-ATS-036-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-023-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-001-evidence-pack.md
Files:
- src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java
- src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java
- src/main/java/com/atstudio/atstudio/repository/BillingAgreementRepository.java
- src/main/java/com/atstudio/atstudio/repository/PaymentOrderRepository.java
- src/main/java/com/atstudio/atstudio/repository/UserSubscriptionRepository.java
- src/main/java/com/atstudio/atstudio/repository/SubscriptionPaymentRepository.java
- src/test/java/com/atstudio/atstudio/service/RecurringRenewalCommandIntegrationTest.java
- src/test/java/com/atstudio/atstudio/service/RecurringRenewalServiceTest.java
- src/test/java/com/atstudio/atstudio/service/PaymentCommandIndependentVerificationIntegrationTest.java
- src/test/java/com/atstudio/atstudio/service/PaymentProviderSuccessRecoveryIntegrationTest.java
- src/test/java/com/atstudio/atstudio/repository/BillingAgreementRepositoryTest.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260715-ATS-002-summary.md
Agent-facing -> deliverables/agent/WI-20260715-ATS-002-evidence-pack.md
Handoff -> deliverables/agent/WI-20260715-ATS-002-handoff.md

[TRACEABILITY REQUIREMENTS]
Exact file/line/command evidence, focused test results, retry identity examples for at least two dates, rollback, and residual risks are required. Tests must reject arbitrary exceptions as expected race losers.
