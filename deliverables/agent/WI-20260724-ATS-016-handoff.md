[WI HEADER]
WI ID: WI-20260724-ATS-016
REQ: REQ-20260724-ATS-002
Agent: qa-integ
Depends On: WI-20260724-ATS-024
Blocks: WI-20260724-ATS-017

[WI SUMMARY]
Why: Prove mail generation and transport without contacting real members.
Scope (in/out): Run a controlled local SMTP sink and verify registration, verification, password reset, payment-failure, and reconciliation-alert message delivery, HTML/subject/base URL, token secrecy, and log safety. If an approved external SMTP and designated test inbox already exist, perform one bounded external delivery and receipt check; otherwise classify it as the remaining human/operations gate.
DoD: Local SMTP transport and content contract pass; no real member receives mail; external delivery has an explicit PASS or conditionally blocked result.
Constraints/Forbidden: No bulk email, no arbitrary address, no secret/token logging, no new SMTP account provisioning, and no silent claim of external deliverability from local-sink evidence.

[ACCEPTANCE CRITERIA]
- [ ] SMTP sink receives all representative message types.
- [ ] Subjects, HTML, AT.M branding, and callback base URL are correct.
- [ ] Tokens appear only in message links and never logs/evidence.
- [ ] Failure logging is bounded and secret-safe.
- [ ] External delivery is performed only to the designated test inbox.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
REQ/Context:
- deliverables/user/REQ-20260724-ATS-002.md
- deliverables/agent/WI-20260724-ATS-014-evidence-pack.md
Files:
- src/main/java/com/atstudio/atstudio/service/EmailService.java
- src/test/java/com/atstudio/atstudio/service/EmailServiceTest.java

[OUTPUT CONTRACT]
User-facing -> `deliverables/user/WI-20260724-ATS-016-summary.md`
Agent-facing -> `deliverables/agent/WI-20260724-ATS-016-evidence-pack.md`

[TRACEABILITY REQUIREMENTS]
Record sink type/version, trigger matrix, support-safe delivery IDs, content assertions, external-delivery condition, log scan, and cleanup.
