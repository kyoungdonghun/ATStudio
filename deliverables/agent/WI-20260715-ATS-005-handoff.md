[WI HEADER]
WI ID: WI-20260715-ATS-005
REQ: REQ-20260714-ATS-001
Agent: se
Depends On: WI-20260715-ATS-002
Blocks: WI-20260715-ATS-007

[WI SUMMARY]
Why: Close the charged-upgrade portion of F-02 and ensure immediate upgrades finalize only from the persisted order target.
Scope (in): Package D only: remove broad charged-upgrade transaction lifetime from `UserSubscriptionService`, use B claim/provider/result/finalize phases, enforce `Propagation.NEVER` in the provider executor, migrate the caller to the persisted-target finalizer, and focused upgrade tests.
Scope (out): B-owned payment command/repositories, cancellation, refund, reconciliation, MySQL proof, UI, and live provider calls.
DoD: Provider call has no active/suspended local transaction; provider success plus local failure remains finalize-only recoverable; persisted upgrade cycle controls finalization; focused tests and two-set deliverables pass.
Constraints/Forbidden: Do not edit B-owned files. Do not call live Toss or mutate retained DB. Preserve concurrent work and preview runtime.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Upgrade planning/claim and finalization are short local transactions separated from provider charge.
- [ ] Provider success is durably recorded before local entitlement finalization.
- [ ] Retry/finalize-only does not charge again and uses `PaymentOrder.upgradeTargetBillingCycle`.
- [ ] Removed billing-key and deterministic/unknown failures preserve existing user-facing behavior and recovery evidence.
Quality:
- [ ] Transaction-observing fake provider and focused upgrade regressions pass.
- [ ] Java compile and `git diff --check` pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Context:
- deliverables/user/REQ-20260714-ATS-001.md
- docs/design/p1-payment-integrity-remediation-design.md
- deliverables/agent/WI-20260714-ATS-036-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-002-evidence-pack.md
Files:
- src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java
- src/main/java/com/atstudio/atstudio/service/SubscriptionUpgradePaymentExecutor.java
- src/test/java/com/atstudio/atstudio/service/UserSubscriptionServiceTest.java
- src/test/java/com/atstudio/atstudio/service/SubscriptionUpgradeCommandIntegrationTest.java
- related executor tests

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260715-ATS-005-summary.md
Agent-facing -> deliverables/agent/WI-20260715-ATS-005-evidence-pack.md
Handoff -> deliverables/agent/WI-20260715-ATS-005-handoff.md

[TRACEABILITY REQUIREMENTS]
Record transaction boundaries, provider-success/local-failure recovery, persisted-target evidence, tests, rollback, and compatibility-overload residual risk.
