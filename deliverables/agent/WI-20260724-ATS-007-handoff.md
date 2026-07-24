[WI HEADER]
WI ID: WI-20260724-ATS-007
REQ: REQ-20260724-ATS-001
Agent: cr
Depends On: WI-20260724-ATS-004, WI-20260724-ATS-005, WI-20260724-ATS-006
Blocks: WI-20260724-ATS-008

[WI SUMMARY]
Why: Perform the final bounded V1 portability, reproducibility, and current-state audit.
Scope (in/out): Review runtime code/config/scripts/docs/DB contract/Git state. Classify findings as defect, historical evidence, environment gate, deferred policy, or non-blocking backlog. No fixes.
DoD: Report findings by severity with exact pointers; confirm P0/P1 zero or fail the gate.
Constraints/Forbidden: Do not re-open archived findings without current evidence. Do not modify source.

[ACCEPTANCE CRITERIA]
- [ ] No unclassified user path, retired runtime path, obsolete payment alias, dead compatibility path, or secret-bearing output remains.
- [ ] API/DB/UI/docs and acceptance environment contracts are consistent.
- [ ] Git/remote/artifact state is accurately classified.
- [ ] P0/P1 verdict is explicit.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
- docs/policies/archive-policy.md
REQ/Context:
- deliverables/user/REQ-20260724-ATS-001.md
- docs/audit/full-system-audit-20260713.md
- docs/design/remaining-remediation-design-20260716.md
- docs/SR/SR-93.md
- deliverables/agent/WI-20260724-ATS-004-evidence-pack.md
- deliverables/agent/WI-20260724-ATS-005-evidence-pack.md
- deliverables/agent/WI-20260724-ATS-006-evidence-pack.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260724-ATS-007-summary.md
Agent-facing -> deliverables/agent/WI-20260724-ATS-007-evidence-pack.md

[TRACEABILITY REQUIREMENTS]
Findings first, exact file/line evidence, residual-risk boundaries, and PASS/FAIL.
