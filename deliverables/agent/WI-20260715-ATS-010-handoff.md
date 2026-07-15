[WI HEADER]
WI ID: WI-20260715-ATS-010
REQ: REQ-20260714-ATS-001
Agent: qa-integ
Depends On: WI-20260715-ATS-001 through WI-20260715-ATS-008
Blocks: payment remediation documentation and final quality gate

[WI SUMMARY]
Why: Independently verify that payment design, schema, repositories, service contracts, tests, evidence, and acceptance boundaries describe the same current behavior.
Scope (in): Three-way design/code/test traceability for F-01 through F-05; API/service state transitions affected by renewal, upgrade, refund, cancellation cleanup, and reconciliation; schema/manual-patch parity; evidence consistency; acceptance-preview isolation.
Scope (out): Production or document correction, database execution, live provider calls, frontend redesign, and unrelated P1 domains.
DoD: Produce a contract matrix and severity-ordered mismatches; explicitly decide PASS only when no P0/P1 cross-layer inconsistency remains; list documentation updates required after review.
Constraints/Forbidden: Read-only review. Create only WI-010 summary/evidence. Do not edit code, tests, schema, design docs, existing evidence, runtime logs, or preview. Treat WI-007 final PASS logs as authoritative and its historical diagnostics as prior-run evidence only.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Each F-01 through F-05 requirement maps to schema/code/test/evidence.
- [ ] Persisted enum/column/index/constraint expectations match entity and repository usage.
- [ ] Service outcomes and reconciliation/refund states match documented contracts.
- [ ] Preview branch isolation and no-live-provider boundary remain intact.
Quality:
- [ ] Mismatches are severity-ranked with exact pointers.
- [ ] Documentation-only drift is separated from executable defects.
- [ ] PASS/FAIL and required follow-up list are explicit.

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
- docs/design/api-spec.md
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
Implementation:
- payment entities, repositories, services, controllers/DTOs, schema/manual patches, and focused tests
- acceptance worktree `C:/Users/jm991/Desktop/project/ATStudio-acceptance-preview` at read-only commit `b217234`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260715-ATS-010-summary.md
Agent-facing -> deliverables/agent/WI-20260715-ATS-010-evidence-pack.md
Handoff -> deliverables/agent/WI-20260715-ATS-010-handoff.md

[TRACEABILITY REQUIREMENTS]
Provide a design-schema-code-test-evidence matrix, exact mismatch pointers, explicit acceptance-preview boundary verification, residual risks, and next documentation/fix WI recommendation. Do not infer behavior without source evidence.
