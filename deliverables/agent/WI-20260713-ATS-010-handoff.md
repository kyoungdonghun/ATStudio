[WI HEADER]
WI ID: WI-20260713-ATS-010
REQ: REQ-20260713-ATS-001
Agent: cr
Depends On: WI-20260713-ATS-008
Blocks: WI-20260713-ATS-012

[WI SUMMARY]
Why: Review withdrawal-to-Provider cleanup transaction boundaries and compensation semantics before release.
Scope (in/out): Review event ordering, AFTER_COMMIT/REQUIRES_NEW behavior, retry query, idempotency, incident dedupe/resolve, provider error classification, scheduler behavior, and zero-charge guards. Correct only confirmed P0 billing defects.
DoD: Local cancellation cannot be rolled back by Provider failure, retries converge, deleted users cannot charge, and no secret is logged or placed in events.
Constraints/Forbidden: No schema, refund, live Provider, multi-server lock, or unrelated payment feature work.

[ACCEPTANCE CRITERIA]
- [ ] Local transaction and external side effect ordering is safe.
- [ ] Provider success/local failure and repeated deletion converge.
- [ ] Retry is targeted and incident lifecycle is accurate.
- [ ] Focused billing tests pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/design/p0-release-blocker-remediation-design.md
- docs/design/payment-operations-runbook.md
REQ/Context Docs:
- deliverables/agent/WI-20260713-ATS-008-evidence-pack.md
Files:
- WI-005 and WI-008 owned paths

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260713-ATS-010-summary.md
Agent-facing -> deliverables/agent/WI-20260713-ATS-010-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260713-ATS-010-handoff.md

[TRACEABILITY REQUIREMENTS]
Severity-ranked findings, transaction analysis, resolved diffs, commands, and residual risk: Required
