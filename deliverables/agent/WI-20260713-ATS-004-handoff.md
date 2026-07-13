[WI HEADER]
WI ID: WI-20260713-ATS-004
REQ: REQ-20260713-ATS-001
Agent: se
Depends On: WI-20260713-ATS-002
Blocks: WI-20260713-ATS-007

[WI SUMMARY]
Why: Remove verification/reset secrets and PII from SMTP success and failure logs without changing external account-recovery behavior.
Scope (in/out): Replace payload logging with a random delivery ID and non-sensitive outcome, add captured-log tests, and scan the mail flow for equivalent token logging. Do not change API responses, token lifetimes, mail templates, or SMTP configuration.
DoD: Captured failure logs contain a correlation ID and outcome but not recipient, subject, body, URL, token, raw exception message, or stack trace.
Constraints/Forbidden: Follow `docs/design/p0-release-blocker-remediation-design.md`. Do not edit track, withdrawal, billing, or reconciliation files. Do not call SMTP.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] SMTP success/failure behavior remains externally compatible.
- [ ] Failure logging is useful through a non-sensitive delivery ID.
Performance:
- [ ] One local random identifier is generated per delivery attempt.
Quality:
- [ ] Log-capture tests prove forbidden values are absent.
- [ ] Existing password-login-policy tests pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/design/p0-release-blocker-remediation-design.md
REQ/Context Docs:
- deliverables/user/REQ-20260713-ATS-001.md
- deliverables/agent/WI-20260713-ATS-002-evidence-pack.md
Files (owned):
- src/main/java/com/atstudio/atstudio/service/EmailService.java
- src/test/java/com/atstudio/atstudio/service/EmailServiceTest.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260713-ATS-004-summary.md
Agent-facing -> deliverables/agent/WI-20260713-ATS-004-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260713-ATS-004-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers, captured-log assertions, and exact test commands: Required
Rollback: Revert only EmailService and its focused test plus this WI's outputs.
