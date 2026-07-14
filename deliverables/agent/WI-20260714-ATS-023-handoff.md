[WI HEADER]
WI ID: WI-20260714-ATS-023
REQ: REQ-20260714-ATS-001
Agent: cr
Depends On: WI-20260714-ATS-018, WI-20260714-ATS-021, WI-20260714-ATS-035
Blocks: WI-20260714-ATS-025, WI-20260714-ATS-026, WI-20260714-ATS-034

[WI SUMMARY]
Why: Independently review payment transaction boundaries, command identity, locking, ledger durability, and database contracts after implementation and concurrency proof.
Scope: Initial billing, upgrade, renewal, refund, reconciliation/cleanup paths, JPA lock order, state transitions, unique constraints, failure persistence, and test adequacy.
Out: New payment features, live Toss, policy redesign, maker-checker expansion, or unrelated refactoring.
DoD: Findings lead, ordered by severity with exact file/line evidence; no critical/high payment integrity defect remains unaddressed or explicitly deferred with rationale.
Constraints: Read and review first. Make code changes only for confirmed in-scope defects and keep them narrowly scoped with tests.

[ACCEPTANCE CRITERIA]
- [ ] Provider calls do not run inside broad local transactions.
- [ ] Retry/finalize-only/stale ambiguity states cannot duplicate charge or ledger effects.
- [ ] Lock order and unique keys converge for upgrade, renewal, and refund.
- [ ] MySQL proof matches JPA/entity/enum contracts.
- [ ] Review includes residual risks and missing-test assessment.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Context:
- deliverables/user/REQ-20260714-ATS-001.md
- docs/design/p1-payment-db-integrity-design.md
- deliverables/agent/WI-20260714-ATS-004-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-005-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-006-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-007-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-008-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-018-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-021-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-035-evidence-pack.md
Files:
- payment entities/repositories/services/tests and schema/manual patches

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-023-summary.md (Korean)
Agent-facing -> deliverables/agent/WI-20260714-ATS-023-evidence-pack.md
Handoff -> deliverables/agent/WI-20260714-ATS-023-handoff.md

[TRACEABILITY REQUIREMENTS]
Severity-ordered findings with file/line pointers, test evidence, assumptions, rollback, and residual risk are required.
