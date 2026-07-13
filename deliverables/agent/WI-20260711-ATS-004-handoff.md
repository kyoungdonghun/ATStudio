[WI HEADER]
WI ID: WI-20260711-ATS-004
REQ: REQ-20260711-ATS-001
Agent: pg
Depends On: -
Blocks: WI-20260711-ATS-006, WI-20260711-ATS-007, WI-20260711-ATS-008

[WI SUMMARY]
Why: Independently audit security, privacy, authorization, secret handling, sensitive logging, upload safety, and payment-data protection.
Scope (in/out): Inspect security configuration, JWT/auth flows, role gates, object ownership, file uploads/downloads, CORS/callbacks, payment/provider payload handling, admin APIs, and frontend exposure. Do not perform penetration attempts against external or production systems.
DoD: Produce an evidence-backed threat-oriented findings list with severity, exploit preconditions, affected assets, and remediation direction.
Constraints/Forbidden: Read-only except WI outputs. Never print secret values, full tokens, billing keys, card numbers, or personal documents.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Verify endpoint authorization and service-level ownership checks.
- [ ] Inspect token/session lifecycle, CORS, callback validation, upload/download controls, and admin mutations.
- [ ] Check logs, entities, DTOs, provider payloads, and UI for sensitive-data persistence or exposure.
- [ ] Distinguish exploitable issues from hardening opportunities.
Performance:
- [ ] Flag security controls that create denial-of-service or unbounded resource risks where evidenced.
Quality:
- [ ] Map high-risk findings to concrete attack paths and exact code pointers.
- [ ] Include missing security-test coverage.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 1 (Policies - Required/Inferred):
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md

Tier 2 (Security Context):
- docs/design/
- docs/payment/
- docs/SR/

REQ/Context Docs:
- deliverables/user/REQ-20260711-ATS-001.md

Files:
- src/main/java/com/atstudio/atstudio/config/
- src/main/java/com/atstudio/atstudio/controller/
- src/main/java/com/atstudio/atstudio/service/
- src/main/resources/application.yml
- frontend/src/

Repro/Logs:
- rg -n "permitAll|hasRole|hasAuthority|@PreAuthorize|secret|token|billing|provider_payload|MultipartFile" src frontend/src

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260711-ATS-004-summary.md : concise Korean findings and risks
Agent-facing -> deliverables/agent/WI-20260711-ATS-004-evidence-pack.md : threat findings, evidence, severity, test gaps, follow-up inputs
Handoff Packet -> deliverables/agent/WI-20260711-ATS-004-handoff.md : this packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required, with narrow file/line references
Tests: Static inspection now; no destructive/security attack execution
Rollback: Only remove this WI's newly created summary/evidence files if explicitly requested
