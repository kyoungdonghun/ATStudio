[WI HEADER]
WI ID: WI-20260714-ATS-019
REQ: REQ-20260714-ATS-001
Agent: re
Depends On: WI-20260714-ATS-009, WI-20260714-ATS-010, WI-20260714-ATS-011, WI-20260714-ATS-012, WI-20260714-ATS-013
Blocks: WI-20260714-ATS-024, WI-20260714-ATS-025, WI-20260714-ATS-028, WI-20260714-ATS-034

[WI SUMMARY]
Why: Independently verify untrusted file, private-document, refresh-session, storage-recovery, and CSV defenses across service and HTTP boundaries.
Scope: Canonical image/adversarial bytes, certification signature/quarantine/download/public denial, storage rollback/commit/recovery, refresh revocation races, and spreadsheet formula neutralization.
Out: Malware scanning, legacy file migration, live DB data changes, new file formats, or frontend redesign.
DoD: Active or forged content cannot be published as a new image/document, private certification bytes are admin attachment-only, revoked refresh tokens stay revoked, storage failures remain recoverable, and exported cells cannot execute formulas.
Constraints: Windows symlink privilege limits must be reported, not hidden. No uploaded document body inspection outside generated test fixtures.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] JPEG/PNG canonicalization rejects malformed/oversized/animated inputs and discards trailing payload.
- [ ] Certification PDF/image validation, private-root download, and `/uploads/company-docs/**` denial are proven.
- [ ] Rollback/after-commit/cleanup retry and shared-reference behavior are proven.
- [ ] Logout/password termination prevents refresh reuse, including stale concurrent attempts.
- [ ] All dangerous CSV prefixes are neutralized after whitespace/control-prefix handling.
Quality:
- [ ] Focused backend tests, compileTestJava, and diff check pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md
Context:
- deliverables/user/REQ-20260714-ATS-001.md
- docs/design/p1-security-acceptance-hardening-design.md
- deliverables/agent/WI-20260714-ATS-009-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-010-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-011-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-012-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-013-evidence-pack.md
Files:
- src/main/java/com/atstudio/atstudio/service/image/
- src/main/java/com/atstudio/atstudio/service/storage/
- src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java
- src/main/java/com/atstudio/atstudio/service/auth/AuthService.java
- src/main/java/com/atstudio/atstudio/service/AdminWhitelistChannelService.java
- related controllers/config/tests

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-019-summary.md (Korean)
Agent-facing -> deliverables/agent/WI-20260714-ATS-019-evidence-pack.md
Handoff -> deliverables/agent/WI-20260714-ATS-019-handoff.md

[TRACEABILITY REQUIREMENTS]
Byte/signature fixtures, HTTP headers/status, transaction/recovery states, session outcomes, commands, and rollback are required.
