[WI HEADER]
WI ID: WI-20260715-ATS-004
REQ: REQ-20260714-ATS-001
Agent: se
Depends On: WI-20260715-ATS-001, WI-20260715-ATS-002
Blocks: WI-20260715-ATS-007

[WI SUMMARY]
Why: Close the cancellation and withdrawal portion of F-02 by separating durable cleanup claims/results from billing-key provider deletion.
Scope (in): Package C only: non-transactional user cancellation and withdrawal cleanup orchestrators, new short `BillingAgreementCleanupTransactionService` claim/result phases, consumption of B-owned unresolved/stale cleanup projections, idempotent already-removed handling, Incident/audit transitions, and focused tests.
Scope (out): Renewal, upgrade, refund, reconciliation, repository edits owned by B, provider adapter changes, MySQL proof, and live provider calls.
DoD: Provider deletion is observed under `Propagation.NEVER`; claim/result states survive injected failures; stale cleanup follows the approved lease; ambiguous provider outcome remains pending/Incident-backed; focused tests and two-set deliverables pass.
Constraints/Forbidden: Do not edit B-owned repositories or payment command service. Do not call live Toss or mutate retained DB. Preserve concurrent work and preview runtime.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Cancellation and withdrawal persist a cleanup claim before provider deletion and a fenced result afterward.
- [ ] Provider success and already-removed clear local key material once; deterministic failure and unknown outcome retain recoverable evidence.
- [ ] Fresh cleanup lease rejects competition and bounded stale candidates are reclaimable.
- [ ] Subscription cancellation semantics remain unchanged for users.
Quality:
- [ ] Provider fake proves no active transaction.
- [ ] Focused cancellation/withdrawal/cleanup tests and compile pass.
- [ ] `git diff --check` passes.

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
- deliverables/agent/WI-20260715-ATS-001-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-002-evidence-pack.md
Files:
- src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java
- src/main/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupService.java
- src/main/java/com/atstudio/atstudio/service/BillingAgreementCleanupTransactionService.java
- src/test/java/com/atstudio/atstudio/service/BillingAgreementApplicationServiceTest.java
- src/test/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupServiceTest.java
- new focused cleanup transaction/integration tests if needed

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260715-ATS-004-summary.md
Agent-facing -> deliverables/agent/WI-20260715-ATS-004-evidence-pack.md
Handoff -> deliverables/agent/WI-20260715-ATS-004-handoff.md

[TRACEABILITY REQUIREMENTS]
Record claim/provider/result boundaries, exact stale loser, provider fake transaction observation, tests, rollback, and unresolved provider-outcome behavior.
