[WI HEADER]
WI ID: WI-20260714-ATS-024
REQ: REQ-20260714-ATS-001
Agent: pg
Depends On: WI-20260714-ATS-019, WI-20260714-ATS-020, WI-20260714-ATS-022
Blocks: WI-20260714-ATS-025, WI-20260714-ATS-026, WI-20260714-ATS-027, WI-20260714-ATS-034

[WI SUMMARY]
Why: Independently review upload, session, proxy, acceptance, and public-exposure defenses after focused and public smoke evidence.
Scope: Image/document authenticity, private/public roots, response headers, storage journal data exposure, refresh revocation, CSV neutralization, social callback, Host/CORS/proxy identity, bootstrap/secrets, and lifecycle scripts.
Out: New security products, malware scanning, key rotation, production infrastructure, or unrelated UI changes.
DoD: Findings lead with severity and exact evidence; no confirmed critical/high exposure or authentication defect remains unresolved.
Constraints: Never inspect real document bodies, secrets, tokens, or live credentials. Use generated fixtures and redacted evidence only.

[ACCEPTANCE CRITERIA]
- [ ] New untrusted content cannot become active public content.
- [ ] Certification bytes and legacy paths fail closed outside admin attachment flow.
- [ ] Session termination and social callback sequencing leave no partial/stale session.
- [ ] Forwarding headers, Host, CORS, rate-limit identity, and acceptance bootstrap are fail-closed.
- [ ] Lifecycle readiness/cleanup claims match script and smoke evidence.

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
- deliverables/agent/WI-20260714-ATS-014-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-015-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-016-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-017-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-019-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-020-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-022-evidence-pack.md
Files:
- upload/storage/auth/security/acceptance/frontend files and tests

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-024-summary.md (Korean)
Agent-facing -> deliverables/agent/WI-20260714-ATS-024-evidence-pack.md
Handoff -> deliverables/agent/WI-20260714-ATS-024-handoff.md

[TRACEABILITY REQUIREMENTS]
Severity-ordered findings, OWASP-relevant mapping where useful, exact pointers, test/smoke evidence, rollback, and residual risk are required.
