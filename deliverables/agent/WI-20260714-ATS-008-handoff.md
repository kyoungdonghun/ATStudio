[WI HEADER]
WI ID: WI-20260714-ATS-008
REQ: REQ-20260714-ATS-001
Agent: se
Depends On: WI-20260714-ATS-001, WI-20260714-ATS-002
Blocks: WI-20260714-ATS-018, WI-20260714-ATS-023, WI-20260714-ATS-025

[WI SUMMARY]
Why: Prevent concurrent refund requests from reserving more than the finalized source payment.
Scope (in/out):
- In: Source `SubscriptionPayment` pessimistic lock, locked revalidation, aggregate reservation, idempotent request creation, and concurrency-focused tests.
- Out: Provider execution transaction split, maker-checker policy, UI, schema application, and live refunds.
DoD:
- `createRefund` locks the source payment before reading reserved totals and inserting the request.
- Concurrent requests cannot reserve above the source amount.
- Preview remains explicitly advisory and unlocked.
Constraints/Forbidden:
- No live Provider call or real refund.
- Do not edit PaymentOrder/schema files owned by WI-004.
- Do not change refund business policy beyond P1-10.
- You are not alone in the codebase; never revert concurrent edits.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Repository exposes a graph-complete `PESSIMISTIC_WRITE` source-payment lookup.
- [ ] Status/provider/key/amount are revalidated after locking.
- [ ] Reserved statuses match the approved design.
- [ ] Boundary and retry/idempotency behavior remain compatible.
Quality:
- [ ] Focused service/repository tests pass.
- [ ] A concurrency test or bounded executable substitute proves lock ordering without claiming H2 proves InnoDB semantics.
- [ ] `gradlew.bat compileJava` and `git diff --check` pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/quality-gates.md
Context:
- deliverables/user/REQ-20260714-ATS-001.md
- docs/audit/p1-remediation-trace-matrix-20260714.md
- docs/design/p1-payment-db-integrity-design.md
- src/main/java/com/atstudio/atstudio/service/AdminPaymentRefundService.java
- src/main/java/com/atstudio/atstudio/repository/SubscriptionPaymentRepository.java
- src/main/java/com/atstudio/atstudio/repository/PaymentRefundRepository.java
- src/test/java/com/atstudio/atstudio/service/AdminPaymentRefundServiceTest.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-008-summary.md (Korean)
Agent-facing -> deliverables/agent/WI-20260714-ATS-008-evidence-pack.md
Handoff -> deliverables/agent/WI-20260714-ATS-008-handoff.md
Implementation ownership -> refund service, source-payment repository, and focused refund tests only.

[TRACEABILITY REQUIREMENTS]
Evidence pointers and exact commands: Required
Tests: focused Gradle refund tests
Rollback: revert owned code/tests; no data mutation in this WI
