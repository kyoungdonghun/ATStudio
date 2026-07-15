[WI HEADER]
WI ID: WI-20260715-ATS-012
REQ: REQ-20260714-ATS-001
Agent: cr
Depends On: WI-20260715-ATS-011
Blocks: payment documentation and final quality gate

[WI SUMMARY]
Why: Independently decide whether commit `46edd88` closes WI-009/WI-010 P1 and P2 findings without introducing a regression.
Scope (in): Review only the WI-011 commit and its focused tests against refund `NEVER`, SUBSCRIBE reconciliation/finalizer state gates, renewal retry-date consumption, and payment-key minimization.
Scope (out): Broad payment re-audit, unrelated code, edits, DB/provider execution, preview mutation, and new feature suggestions.
DoD: Produce exact finding-by-finding closure decisions and PASS only if no P0/P1 remains; record any residual P2/P3 separately and provide documentation-update pointers.
Constraints/Forbidden: Read-only. Create only WI-012 summary/evidence. Do not edit implementation, tests, schema, existing WI evidence, logs, or preview. Do not rerun MySQL; WI-011 changed no schema/query/lock primitive.

[ACCEPTANCE CRITERIA]
- [ ] Active-transaction refund is rejected before provider call and normal refund paths remain covered.
- [ ] SUBSCRIBE requires READY/cleanup NONE/not cancelled/no subscription/retained key at candidate, locked result, and finalizer boundaries.
- [ ] Day-two failed retry consumes the gate and ambiguous outcome leaves it null without another charge.
- [ ] Raw payment key remains exact only in protected structured ownership and is absent from lookup payload, Incident/audit note, and cancel unknown log.
- [ ] Focused/impacted evidence is reproducible and no P0/P1 remains.
- [ ] `git diff --check` passes for review outputs.

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
- deliverables/agent/WI-20260715-ATS-009-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-010-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-011-evidence-pack.md
Review target:
- commit `46edd88`
- production/tests changed by `46edd88`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260715-ATS-012-summary.md
Agent-facing -> deliverables/agent/WI-20260715-ATS-012-evidence-pack.md
Handoff -> deliverables/agent/WI-20260715-ATS-012-handoff.md

[TRACEABILITY REQUIREMENTS]
Map each WI-009/WI-010 finding to exact corrected code and test pointers, state PASS/FAIL, list residual risk, explain why MySQL rerun is or is not needed, and enumerate only verified documentation updates.
