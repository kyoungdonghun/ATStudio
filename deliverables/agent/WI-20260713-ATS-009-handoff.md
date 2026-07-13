[WI HEADER]
WI ID: WI-20260713-ATS-009
REQ: REQ-20260713-ATS-001
Agent: pg
Depends On: WI-20260713-ATS-006, WI-20260713-ATS-007
Blocks: WI-20260713-ATS-012

[WI SUMMARY]
Why: Perform a final security review of protected media and secret-free mail changes.
Scope (in/out): Review access-control matcher ordering, encoded/path traversal variants, Range boundary behavior, public/admin DTO leakage, mail values and exception rendering, and focused tests. Correct only confirmed P0 defects in media/mail paths.
DoD: No public full-original route or secret-bearing mail log remains; findings are severity-ranked and resolved or explicitly blocked.
Constraints/Forbidden: No file/data migration, external calls, broad P1 refactor, or billing edits.

[ACCEPTANCE CRITERIA]
- [ ] Anonymous/USER/ADMIN path behavior is intentional and fail-closed.
- [ ] Public DTO and stream cannot disclose the complete original.
- [ ] Mail logs contain no PII, token capability, body, or provider detail.
- [ ] Focused security tests pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
Tier 1:
- docs/policies/security-policy.md
Tier 2:
- docs/design/p0-release-blocker-remediation-design.md
REQ/Context Docs:
- deliverables/agent/WI-20260713-ATS-006-evidence-pack.md
- deliverables/agent/WI-20260713-ATS-007-evidence-pack.md
Files:
- WI-003, WI-004, WI-006, and WI-007 owned paths

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260713-ATS-009-summary.md
Agent-facing -> deliverables/agent/WI-20260713-ATS-009-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260713-ATS-009-handoff.md

[TRACEABILITY REQUIREMENTS]
Severity-ranked findings, resolved diffs, commands, and residual risk: Required
