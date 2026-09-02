[WI HEADER]
WI ID: WI-20260902-ATS-003
REQ: REQ-20260902-ATS-001
Agent: docops
Depends On: WI-20260902-ATS-001
Blocks: WI-20260902-ATS-004

[WI SUMMARY]
Why: Make runtime ownership, storage integrity, backup/restore, and legacy-asset remediation discoverable to an operator without copying secrets or historical runtime values.
Scope (in/out): Update active operational/config/API design documentation and create a runtime-storage operations guide. Preserve historical REQ/WI/SR text; do not make data, source, or secret changes.
DoD: Documentation defines development/acceptance/production runtime ownership, start readiness, integrity checks, backup/restore bundle requirements, current legacy asset decision boundary, and explicit limitations.
Constraints/Forbidden: Do not reveal runtime bundle paths that identify user accounts, secrets, JDBC URLs, file keys, private document paths, or historical credentials. Do not claim production deployment or a completed data migration.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Current runtime storage contract and strict/non-strict behavior are documented.
- [ ] DB+public/private storage backup and restore are documented as one operation.
- [ ] Historical missing-assets are described only as inventory counts and operator choices.
Quality:
- [ ] validate-docs passes.
- [ ] No historical record is rewritten as current state.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1:
- docs/policies/security-policy.md
- docs/policies/future-policy-stubs.md

Tier 2:
- deliverables/user/REQ-20260902-ATS-001.md
- deliverables/agent/WI-20260902-ATS-001-evidence-pack.md
- scripts/acceptance/README.md
- docs/design/db-schema.md
- docs/design/api-spec.md
- docs/design/p1-security-acceptance-hardening-design.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260902-ATS-003-summary.md.
Agent-facing -> deliverables/agent/WI-20260902-ATS-003-evidence-pack.md.
Handoff Packet -> deliverables/agent/WI-20260902-ATS-003-handoff.md.

[TRACEABILITY REQUIREMENTS]
Document pointers, validate-docs result, current/archival-state separation, and rollback guidance are required.
