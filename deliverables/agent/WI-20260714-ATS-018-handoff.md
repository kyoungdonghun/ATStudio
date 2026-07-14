[WI HEADER]
WI ID: WI-20260714-ATS-018
REQ: REQ-20260714-ATS-001
Agent: re
Depends On: WI-20260714-ATS-004, WI-20260714-ATS-005, WI-20260714-ATS-006, WI-20260714-ATS-007, WI-20260714-ATS-008
Blocks: WI-20260714-ATS-023, WI-20260714-ATS-025, WI-20260714-ATS-028, WI-20260714-ATS-034

[WI SUMMARY]
Why: Independently prove that initial billing, charged upgrade, renewal, and refund commands converge under retries, provider ambiguity, local finalization failure, and concurrent requests.
Scope: Fake-provider integration tests, transaction boundary assertions, repeated/finalize-only calls, stale PROCESSING refusal, per-agreement renewal isolation, refund reservation concurrency, and contract-level unique-key assertions.
Out: Live Toss, real payment, production/local DB schema application, maker-checker expansion, or business-policy changes.
DoD: No tested retry/concurrency path creates duplicate provider intent, payment ledger row, subscription transition, renewal period order, or excess refund reservation.
Constraints: Test the approved implementation as an independent verifier. Fix production code only for a reproducible in-scope defect and document it explicitly.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Provider success plus local failure converges through finalize-only without another provider call.
- [ ] Stale/ambiguous PROCESSING commands never blind replay.
- [ ] Parallel upgrade/refund attempts converge under locks and unique constraints.
- [ ] One renewal agreement failure cannot roll back or stop another agreement.
Quality:
- [ ] Focused suites, compileTestJava, and git diff --check pass.
- [ ] Any environment limitation is separated from application assertion failures.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
Tier 1:
- docs/policies/quality-gates.md
Context:
- deliverables/user/REQ-20260714-ATS-001.md
- docs/design/p1-payment-db-integrity-design.md
- deliverables/agent/WI-20260714-ATS-004-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-005-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-006-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-007-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-008-evidence-pack.md
Files:
- src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java
- src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java
- src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java
- src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java
- src/main/java/com/atstudio/atstudio/service/AdminPaymentRefundService.java
- related repositories/entities/tests

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-018-summary.md (Korean)
Agent-facing -> deliverables/agent/WI-20260714-ATS-018-evidence-pack.md
Handoff -> deliverables/agent/WI-20260714-ATS-018-handoff.md

[TRACEABILITY REQUIREMENTS]
Exact commands, test counts, provider call counts, committed DB outcomes, rollback notes, and remaining MySQL-only risks are required.
