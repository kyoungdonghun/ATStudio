[WI HEADER]
WI ID: WI-20260716-ATS-028
REQ: REQ-20260716-ATS-002
Agent: se
Depends On: WI-20260716-ATS-025, WI-20260716-ATS-027
Blocks: WI-20260716-ATS-030

[WI SUMMARY]
Why: Close the financial/state/security findings from the final backend audit before the development-only commit.
Scope (in): F-025-01 through F-025-05; withdrawal/renewal fencing, read-only ADMIN reconciliation, entitlement-correction lock order and stale fences, scheduler business-zone clock, Provider identifier privacy, focused tests, and directly owned payment/security operations documentation.
Scope (out): Frontend pages, CORS/export adapter, company-certification API examples, subscription glossary/comments, client branch/runtime, live Provider/DB operations, new product behavior.
DoD: All five findings are closed with deterministic unit/integration tests; canonical agreement-before-subscription locking and non-terminal payment fencing are preserved; GET diagnostics are side-effect free; configured business zone drives scheduled dates; no raw Provider fragments reach persisted or serialized notes.
Constraints/Forbidden: Work only on the development branch. Do not stage, commit, push, restart, mutate a DB/provider, touch the client worktree, or alter the four product invariants. Avoid schema changes unless absolutely required; if a schema change becomes necessary, stop and report the approval point instead of applying it. Do not revert concurrent edits.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Withdrawal and renewal claim/finalization cannot charge or reactivate a deleted/cancelled user; an in-flight non-terminal order is fenced deterministically.
- [ ] `GET /api/admin/payments/reconciliation` performs observations only and never writes Incidents/payment/agreement/subscription state.
- [ ] Entitlement correction follows agreement-before-subscription lock order, fences non-terminal payment, and detects agreement/subscription drift.
- [ ] Scheduler and date-based payment jobs use the configured business zone through an injectable Clock or equivalent deterministic date source.
- [ ] Incident/audit free text contains no full or partial raw Provider identifier; deterministic `REF-*` support references remain available.
Quality:
- [ ] Focused concurrency/state/privacy/controller tests pass.
- [ ] Existing related payment/withdrawal/scheduler tests pass.
- [ ] Payment/security operations documents match the implementation.
- [ ] Both deliverables are produced.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/design/remaining-remediation-design-20260716.md
- docs/design/payment-integration-design.md
- docs/design/payment-operations-runbook.md
- docs/design/api-spec.md
REQ/Findings:
- deliverables/user/REQ-20260716-ATS-002.md
- deliverables/user/WI-20260716-ATS-025-summary.md
- deliverables/agent/WI-20260716-ATS-025-evidence-pack.md
- deliverables/user/WI-20260716-ATS-027-summary.md
Owned files:
- src/main/java/com/atstudio/atstudio/service/UserService.java
- src/main/java/com/atstudio/atstudio/service/payment/**
- src/main/java/com/atstudio/atstudio/service/PaymentReconciliation*.java
- src/main/java/com/atstudio/atstudio/service/AdminPayment*.java
- src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java
- src/main/java/com/atstudio/atstudio/service/SubscriptionScheduler.java
- src/main/java/com/atstudio/atstudio/repository/BillingAgreementRepository.java
- src/main/java/com/atstudio/atstudio/repository/PaymentOrderRepository.java
- src/main/java/com/atstudio/atstudio/dto/payment/**
- src/main/java/com/atstudio/atstudio/config/PaymentProperties.java
- corresponding backend tests
- docs/design/payment-integration-design.md
- docs/design/payment-operations-runbook.md
- docs/policies/security-policy.md
Do not edit:
- frontend/**
- src/main/java/com/atstudio/atstudio/config/CorsConfig.java
- docs/design/api-spec.md
- docs/standards/glossary.md
- docs/design/usecase/sound-track.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-028-summary.md
Agent-facing -> deliverables/agent/WI-20260716-ATS-028-evidence-pack.md
Handoff -> deliverables/agent/WI-20260716-ATS-028-handoff.md

[TRACEABILITY REQUIREMENTS]
- List every changed file and map it to F-025-01..05.
- Record exact tests, results, lock/fence reasoning, rollback, and remaining environment evidence.
- Preserve unrelated and concurrent worktree edits.
