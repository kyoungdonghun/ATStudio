[WI HEADER]
WI ID: WI-20260715-ATS-011
REQ: REQ-20260714-ATS-001
Agent: se
Depends On: WI-20260715-ATS-009, WI-20260715-ATS-010
Blocks: independent review rerun, payment documentation, final quality gate

[WI SUMMARY]
Why: Close the two P1 and two P2 gaps independently confirmed after the Package A-G implementation and MySQL proof.
Scope (in): (1) strict `Propagation.NEVER` at refund execution/provider boundary; (2) cancelled or cleanup-claimed initial SUBSCRIBE remains detect-only during reconciliation and cannot finalize; (3) consume `renewalRetryAt` when an eligible failed renewal retry is claimed; (4) remove/mask raw Toss `paymentKey` from provider payload and Incident/audit note evidence while retaining the exact structured transaction owner field; focused regression tests and two-set evidence.
Scope (out): Schema/index/lock-order changes, new libraries, live Toss, retained/disposable MySQL execution, UI, preview mutation, unrelated payment refactors, and changes to historical review evidence.
DoD: All four findings have deterministic failing-before/passing-after tests; refund outer-transaction invocation fails before provider call; contradictory SUBSCRIBE evidence creates/remains Incident-only without financial mutation; day-two ambiguous retry has null retry gate; raw payment key is absent from payload/note; impacted payment suites pass.
Constraints/Forbidden: No DB mutation, provider network call, schema edit, runtime log edit, or preview change. Keep exact transaction ID only in protected structured order/payment ownership fields required for reconciliation/refund. Do not weaken amount/currency/provider/order evidence matching. Preserve canonical lock order. You are not alone; do not revert other changes.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] `executeRefund` and `executeRefundAt` reject an active caller transaction with `IllegalTransactionStateException` and zero provider calls.
- [ ] Normal non-transactional refund execution and lease/replay behavior remain passing.
- [ ] SUBSCRIBE reconciliation mutation eligibility requires agreement `READY`, cleanup `NONE`, no cancellation marker, no subscription, and retained key evidence.
- [ ] The same expected state is revalidated under lock before provider-success persistence and again before initial finalization.
- [ ] Cancellation/cleanup that wins after lookup leaves order/agreement/subscription/payment unchanged except Incident/audit detect-only evidence; no provider charge is issued.
- [ ] Claiming an eligible `FAILED` renewal retry clears/consumes `renewalRetryAt`; deterministic failure may schedule a new date, while ambiguity leaves it null.
- [ ] Toss lookup `paymentKey` remains available only as the structured transaction ID; serialized provider payload and Incident/audit notes do not contain the raw value.
Quality:
- [ ] Focused refund, renewal, reconciliation, provider, Incident, and transaction-boundary tests pass.
- [ ] Impacted Package B/E/F regression tests pass.
- [ ] `git diff --check` passes.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/design/p1-payment-integrity-remediation-design.md
Context:
- deliverables/user/REQ-20260714-ATS-001.md
- deliverables/agent/WI-20260715-ATS-009-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-010-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-002-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-003-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-006-evidence-pack.md
Owned production files:
- src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java
- src/main/java/com/atstudio/atstudio/service/AdminPaymentRefundService.java
- src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java
- src/main/java/com/atstudio/atstudio/service/PaymentReconciliationTransactionService.java
- src/main/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentService.java
- src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java
Owned tests:
- focused refund, renewal command, reconciliation recovery/Incident, and Toss billing provider tests under src/test/java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260715-ATS-011-summary.md
Agent-facing -> deliverables/agent/WI-20260715-ATS-011-evidence-pack.md
Handoff -> deliverables/agent/WI-20260715-ATS-011-handoff.md

[TRACEABILITY REQUIREMENTS]
Map every change/test to WI-009 P1-01/P1-02/P2-01 and WI-010 P2-EXEC-02/P2-SEC-03. Record exact transaction behavior, state invariants, raw-key negative assertions, commands/results, rollback, and whether any MySQL rerun is technically required. Do not claim closure before independent review.
