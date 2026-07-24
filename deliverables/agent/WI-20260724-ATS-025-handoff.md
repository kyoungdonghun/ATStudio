[WI HEADER]
WI ID: WI-20260724-ATS-025
REQ: REQ-20260724-ATS-002
Agent: se
Depends On: WI-20260724-ATS-016
Blocks: WI-20260724-ATS-017

[WI SUMMARY]
Why: WI-016 proved real loopback SMTP transport but found that account-mail nicknames and subscription-payment-failure dynamic text are inserted into HTML without output encoding.
Scope (in/out): Apply context-appropriate HTML escaping at every affected EmailService template boundary, add adversarial regression tests, and rerun the four-message loopback SMTP transport contract. No mail workflow redesign, external SMTP delivery, provider call, schema change, or unrelated template rewrite.
DoD: Verification, password-reset, payment-failure, and reconciliation messages all preserve AT.M branding and valid links while every untrusted dynamic field is escaped; success/failure logs remain secret-safe; focused and related tests pass.
Constraints/Forbidden: Reuse the existing escaping helper or an equivalent established local pattern. Do not double-escape already encoded values, log message content, retain raw MIME, contact a real inbox, or change the shared payment runtime/database.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Verification and password-reset nicknames are HTML escaped.
- [ ] Payment-failure nickname, failure summary, and retry guidance are HTML escaped.
- [ ] Verification/reset callback links remain valid HTTPS links with tokens only in URL parameters.
- [ ] Reconciliation escaping remains unchanged and passing.
- [ ] Four representative messages reach a loopback SMTP sink.
Performance:
- [ ] Mail generation adds no external dependency or unbounded processing.
Quality:
- [ ] Adversarial unit regressions pass.
- [ ] Existing EmailService tests pass.
- [ ] Actual JavaMailSenderImpl loopback transport contract passes 17/17.
- [ ] Logs and retained evidence contain no recipient, body, token, secret, or raw MIME.
- [ ] Backend focused/related tests, documentation validation, and git diff checks pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/standards/evidence-pack-standard.md
REQ/Context Docs:
- deliverables/user/REQ-20260724-ATS-002.md
- deliverables/agent/WI-20260724-ATS-016-handoff.md
- deliverables/agent/WI-20260724-ATS-016-evidence-pack.md
Files:
- src/main/java/com/atstudio/atstudio/service/EmailService.java
- src/test/java/com/atstudio/atstudio/service/EmailServiceTest.java
Repro/Logs:
- C:/Users/jm991/AppData/Local/ATStudio/wi016-mail-evidence-20260724T231700/sanitized

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260724-ATS-025-summary.md
Agent-facing -> deliverables/agent/WI-20260724-ATS-025-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260724-ATS-025-handoff.md

[TRACEABILITY REQUIREMENTS]
Record exact template fields, adversarial cases, transport result matrix, log and raw-MIME cleanup proof, commands, test counts, residual external-delivery gate, and rollback.
