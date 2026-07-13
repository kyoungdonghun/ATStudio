[WI HEADER]
WI ID: WI-20260713-ATS-007
REQ: REQ-20260713-ATS-001
Agent: re
Depends On: WI-20260713-ATS-004
Blocks: WI-20260713-ATS-009, WI-20260713-ATS-013

[WI SUMMARY]
Why: Independently prove that verification/reset secrets and PII cannot re-enter logs through success, failure, or exception rendering.
Scope (in/out): Review WI-004, run captured-output tests, scan related mail logs, and correct only mail-log defects/tests. No API, template, token-policy, or SMTP configuration changes.
DoD: Recipient, nickname, subject, body, URL, token, provider message, and stack trace are absent while a delivery ID and outcome remain.
Constraints/Forbidden: No SMTP call. Preserve media and billing WIs.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Generic external behavior remains unchanged.
Quality:
- [ ] Success and failure captured-output tests pass and source scan finds no fallback payload logging.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/design/p0-release-blocker-remediation-design.md
REQ/Context Docs:
- deliverables/agent/WI-20260713-ATS-004-evidence-pack.md
Files:
- src/main/java/com/atstudio/atstudio/service/EmailService.java
- src/test/java/com/atstudio/atstudio/service/EmailServiceTest.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260713-ATS-007-summary.md
Agent-facing -> deliverables/agent/WI-20260713-ATS-007-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260713-ATS-007-handoff.md

[TRACEABILITY REQUIREMENTS]
Independent findings, captured values, commands/results, and corrective diff: Required
Rollback: Revert only WI-007 corrective edits and outputs.
