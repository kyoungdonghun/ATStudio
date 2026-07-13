[WI HEADER]
WI ID: WI-20260713-ATS-008
REQ: REQ-20260713-ATS-001
Agent: re
Depends On: WI-20260713-ATS-005
Blocks: WI-20260713-ATS-010, WI-20260713-ATS-013

[WI SUMMARY]
Why: Independently verify local-first withdrawal, after-commit cleanup compensation, durable retry, and zero-charge behavior.
Scope (in/out): Review WI-005 source/tests and correct billing-cleanup defects. Explicitly verify Provider success followed by local commit failure: a retry response such as `ALREADY_REMOVED_BILLING_KEY` must be treated as completed cleanup, clear local key material, and resolve the incident. No schema change, auto-refund, or live Toss call.
DoD: Transaction ordering, query guard, service guard, failure incident, dedupe, retry, already-removed compensation, and incident resolution are tested.
Constraints/Forbidden: Reuse existing issue type and schema. Preserve media/mail WIs. Do not touch runtime logs.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Withdrawal success is independent of Provider result.
- [ ] Deleted users produce zero renewal charge calls.
- [ ] Provider failure retains a retryable local key and incident.
- [ ] Already-removed Provider response converges to local success.
Quality:
- [ ] Focused repository/service/coordinator tests pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/design/p0-release-blocker-remediation-design.md
- docs/design/payment-operations-runbook.md
REQ/Context Docs:
- deliverables/agent/WI-20260713-ATS-005-evidence-pack.md
Files:
- WI-005 owned source and tests

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260713-ATS-008-summary.md
Agent-facing -> deliverables/agent/WI-20260713-ATS-008-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260713-ATS-008-handoff.md

[TRACEABILITY REQUIREMENTS]
Independent findings, compensation test, test commands/results, and corrective diff: Required
Rollback: Revert only WI-008 corrective edits and outputs.
