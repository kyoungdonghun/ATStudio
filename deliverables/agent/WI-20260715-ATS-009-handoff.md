[WI HEADER]
WI ID: WI-20260715-ATS-009
REQ: REQ-20260714-ATS-001
Agent: cr
Depends On: WI-20260715-ATS-001 through WI-20260715-ATS-008
Blocks: payment remediation documentation and final quality gate

[WI SUMMARY]
Why: Independently review the completed payment-integrity remediation after MySQL/InnoDB proof, without relying on implementer conclusions.
Scope (in): Review Packages A-F, WI-008 correction, Package G proof, transaction propagation, lock order, uniqueness/idempotency, provider-success recovery, refund lease fencing, Incident convergence, failure-closed behavior, and test adequacy.
Scope (out): Code edits, database execution, provider calls, UI, preview mutation, unrelated P1 domains, and speculative redesign.
DoD: Produce severity-ordered findings with exact code/evidence pointers; explicitly state PASS only if no P0/P1 defect remains; identify residual risks and missing tests separately from defects.
Constraints/Forbidden: Read-only review. Do not edit production, tests, schema, runner, logs, preview, or existing WI artifacts. You may create only WI-009 summary/evidence. Do not accept H2 evidence as a substitute for MySQL where WI-007 is authoritative. You are not alone in the codebase; preserve every existing change.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Every design finding F-01 through F-05 maps to implementation and proof.
- [ ] Provider side effects remain outside broad local transactions.
- [ ] DONE/finalize-only paths cannot duplicate payment, entitlement, refund, or Incident effects.
- [ ] Lock order and unique constraints are consistent across competing flows.
Quality:
- [ ] Findings are ordered P0/P1/P2/P3 with precise file/line evidence.
- [ ] No finding is based only on style preference.
- [ ] PASS/FAIL recommendation and residual risks are explicit.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/design/p1-payment-integrity-remediation-design.md
- docs/design/payment-integration-design.md
Context:
- deliverables/user/REQ-20260714-ATS-001.md
- deliverables/agent/WI-20260715-ATS-001-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-002-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-003-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-004-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-005-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-006-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-007-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-008-evidence-pack.md
Code:
- src/main/java/com/atstudio/atstudio/entity/payment/**
- src/main/java/com/atstudio/atstudio/repository/payment/**
- src/main/java/com/atstudio/atstudio/service/*Payment*.java
- src/main/java/com/atstudio/atstudio/service/*Refund*.java
- src/main/java/com/atstudio/atstudio/service/*Reconciliation*.java
- src/main/resources/schema.sql
- src/main/resources/db/manual/20260714_payment_db_integrity.sql
- focused payment tests and WI-007 MySQL proof tests

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260715-ATS-009-summary.md
Agent-facing -> deliverables/agent/WI-20260715-ATS-009-evidence-pack.md
Handoff -> deliverables/agent/WI-20260715-ATS-009-handoff.md

[TRACEABILITY REQUIREMENTS]
Use exact paths/lines and distinguish confirmed defect, test gap, design tradeoff, and out-of-scope future work. Include reviewed commit range `103fdf4..830c8dd`, MySQL proof result, rollback implications, and the next WI recommendation.
