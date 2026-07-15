[WI HEADER]
WI ID: WI-20260715-ATS-003
REQ: REQ-20260714-ATS-001
Agent: se
Depends On: WI-20260715-ATS-001
Blocks: WI-20260715-ATS-007

[WI SUMMARY]
Why: Close F-03 by making refund `PROCESSING` claims recoverable without duplicate refund mutation or stale local result writes.
Scope (in): Design Package E only: refund repository stale/locked projections, 15-minute lease-aware claim/reclaim, persisted lease in claim snapshots, exact same-row/key replay for current Toss refunds within the 24-hour ceiling, lookup-only/pending behavior when replay is forbidden, and lease-fenced success/failure/pending result writers.
Scope (out): Provider adapter HTTP semantics, payment-command renewal, cancellation/withdrawal, upgrade, general payment reconciliation, MySQL proof, and live Toss.
DoD: All six Section 6.4 crash/race/replay cases are implemented with focused tests; no replacement refund/key is created; old lease results cannot win; provider call remains outside local transactions; two-set deliverables are complete.
Constraints/Forbidden: Do not change Toss adapter or provider contract unless an unavoidable compile-only signature issue is first reported. Do not mutate retained DB or call live provider. Do not edit Package B/C/D/F files. You are not alone; preserve others' changes.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Fresh `PROCESSING` rejects a competing execution; stale `PROCESSING` can be atomically reclaimed once.
- [ ] Claim snapshot includes immutable refund/provider/payment/order/amount/currency/reason/idempotency/lease evidence.
- [ ] Every result writer verifies the exact persisted lease and rejects delayed old results.
- [ ] Stale replay uses the exact same idempotency key and immutable command inside the permitted ceiling.
- [ ] When replay is forbidden and exact lookup is unavailable, no provider mutation occurs and the refund remains pending with auditable evidence.
- [ ] Provider invocation occurs with no active transaction.
Performance:
- [ ] Stale scans/claims use Package A's `(status, processing_started_at, id)` index and bounded selection.
Quality:
- [ ] Focused refund resilience/unit/integration tests pass with exact loser assertions.
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
- src/main/java/com/atstudio/atstudio/service/AdminPaymentRefundService.java
- src/main/java/com/atstudio/atstudio/service/PaymentRefundTransactionService.java
- src/main/java/com/atstudio/atstudio/repository/PaymentRefundRepository.java
- src/test/java/com/atstudio/atstudio/service/PaymentRefundResilienceIntegrationTest.java
- related focused refund service tests

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260715-ATS-003-summary.md
Agent-facing -> deliverables/agent/WI-20260715-ATS-003-evidence-pack.md
Handoff -> deliverables/agent/WI-20260715-ATS-003-handoff.md

[TRACEABILITY REQUIREMENTS]
Exact file/line/command evidence, all six crash/replay scenarios, transaction-observation evidence, rollback, and residual provider-retention assumptions are required. Deadlock, timeout, connection failure, or arbitrary exception cannot satisfy a race test.
