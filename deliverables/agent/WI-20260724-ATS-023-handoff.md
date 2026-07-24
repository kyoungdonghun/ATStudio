[WI HEADER]
WI ID: WI-20260724-ATS-023
REQ: REQ-20260724-ATS-002
Agent: se
Depends On: WI-20260724-ATS-014
Blocks: WI-20260724-ATS-024

[WI SUMMARY]
Why: Runtime API verification found that `GET /api/company-certifications/me` reaches certification persistence for a PERSONAL user and returns 404 instead of failing authorization before the repository query. A changed-account scenario could expose historical BUSINESS certification data.
Scope (in/out): Add the existing BUSINESS-user eligibility guard to the self-status service path and add focused service/controller/security regression tests proving PERSONAL and ADMIN callers cannot read self certification while BUSINESS behavior remains unchanged. Do not redesign certification workflow or alter response DTOs.
DoD: Non-BUSINESS access fails before certification lookup with the established forbidden contract; BUSINESS no-record remains 404; BUSINESS record remains 200; focused and related suites pass.
Constraints/Forbidden: Modify only company-certification service and directly related tests plus WI-023 deliverables. Reuse existing error semantics and security conventions. No schema, API shape, frontend, DB data, Provider, or mail changes.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] PERSONAL user receives the established forbidden response for `GET /api/company-certifications/me`.
- [ ] Repository lookup is not invoked for PERSONAL or ADMIN callers.
- [ ] BUSINESS user with no record still receives not found.
- [ ] BUSINESS user with a record receives the same response as before.
- [ ] Changed-account/historical-record regression is covered.
Performance:
- [ ] No additional persistence query for rejected callers.
Quality:
- [ ] Focused service/controller/security tests pass.
- [ ] Relevant backend test slice and compile pass.
- [ ] `git diff --check` passes.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/design/usecase/company-certification.md
- docs/design/api-spec.md
- docs/standards/evidence-pack-standard.md
REQ/Context:
- deliverables/user/REQ-20260724-ATS-002.md
- deliverables/agent/WI-20260724-ATS-014-evidence-pack.md
Files:
- src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java
- src/test/java/com/atstudio/atstudio/service/CompanyCertificationServiceTest.java
- src/test/java/com/atstudio/atstudio/service/CompanyCertificationSecurityVerificationTest.java
- src/test/java/com/atstudio/atstudio/controller/CompanyCertificationControllerTest.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260724-ATS-023-summary.md
Agent-facing -> deliverables/agent/WI-20260724-ATS-023-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260724-ATS-023-handoff.md

[TRACEABILITY REQUIREMENTS]
Record exact guard location, test names/counts, repository non-invocation proof, commands, and rollback.
