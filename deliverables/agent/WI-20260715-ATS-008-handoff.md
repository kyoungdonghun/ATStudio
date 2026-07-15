[WI HEADER]
WI ID: WI-20260715-ATS-008
REQ: REQ-20260714-ATS-001
Agent: se
Depends On: WI-20260715-ATS-007 (partial MySQL evidence; races 4 and 7 exposed the defect)
Blocks: WI-20260715-ATS-007 closure, payment/DB independent review

[WI SUMMARY]
Why: Disposable MySQL races proved that renewal completion is not idempotent after the first finalizer advances mutable billing state, and reconciliation can therefore leave an otherwise converged Incident open.
Scope (in): Correct DONE-order validation/finalization ordering for renewal; make reconciliation and the normal renewal finalizer converge after either side wins; inspect the analogous upgrade DONE path and change it only if the same mutable-state ordering defect is present; add focused H2 regression coverage.
Scope (out): Package G MySQL test/runner/evidence files, provider calls, schema changes, retained or preview databases, UI, unrelated payment refactors, and live Toss behavior.
DoD: A repeated renewal finalizer validates immutable ownership/payment evidence and returns without reapplying entitlement; reconciliation can resolve an Incident when a normal finalizer has already completed the same order; focused tests pass; no duplicate payment or entitlement transition is possible.
Constraints/Forbidden: Do not edit any untracked WI-007 MySQL proof file. Do not mutate any database or run the disposable MySQL runner in this WI. Do not weaken ownership, provider transaction, amount, currency, payment-state, or relationship validation. Preserve canonical lock order. The running acceptance-preview worktree/server must not be changed. You are not alone in the codebase; do not revert others' edits and accommodate the current branch state.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] A second `finalizeRenewal` call after the first advances `agreement.nextBillingAt` succeeds as an idempotent no-op.
- [ ] DONE renewal no-op requires one matching DONE `SubscriptionPayment`; missing or mismatched payment evidence fails closed.
- [ ] A reconciliation attempt racing after normal completion can converge and permit the matching Incident to become RESOLVED without another provider charge or local entitlement effect.
- [ ] PROVIDER_SUCCEEDED renewal finalization still validates the current billing period before applying state.
- [ ] The analogous upgrade path is explicitly reviewed; any change is limited to the same proven idempotency ordering contract and covered by a regression test.
Performance:
- [ ] No new locks, unbounded waits, or external calls are added.
Quality:
- [ ] Focused payment command and reconciliation integration tests pass.
- [ ] Existing Package B/F focused regression suites pass.
- [ ] `git diff --check` passes for owned files.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/quality-gates.md
- docs/policies/security-policy.md
Tier 2:
- docs/design/p1-payment-integrity-remediation-design.md
REQ/Context:
- deliverables/user/REQ-20260714-ATS-001.md
- deliverables/agent/WI-20260715-ATS-002-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-003-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-006-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-007-handoff.md
- deliverables/agent/WI-20260715-ATS-007/mysql-races.log
Files (owned):
- src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java
- src/test/java/com/atstudio/atstudio/service/PaymentCommandIndependentVerificationIntegrationTest.java
- src/test/java/com/atstudio/atstudio/service/PaymentReconciliationRecoveryIntegrationTest.java
- existing tracked focused payment tests only when a narrowly scoped assertion is required
Files (read-only):
- src/test/java/com/atstudio/atstudio/service/PaymentMysqlConcurrencyIntegrationTest.java
- src/test/java/com/atstudio/atstudio/service/MysqlRaceTestSupport.java
- deliverables/agent/WI-20260715-ATS-007/**

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260715-ATS-008-summary.md :
- Explain the exposed defect, correction, test result, and residual MySQL proof dependency.
Agent-facing -> deliverables/agent/WI-20260715-ATS-008-evidence-pack.md :
- Record exact code/test pointers, commands, outcomes, invariant proof, rollback, and follow-up WI.
Handoff Packet -> deliverables/agent/WI-20260715-ATS-008-handoff.md :
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Include exact focused commands and result counts.
Rollback: State the owned production/test files to revert; no database rollback is permitted or required.
Follow-up: WI-007 must rerun its unchanged disposable MySQL proof after this WI passes focused H2 verification.
