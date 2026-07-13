[WI HEADER]
WI ID: WI-20260711-ATS-007
REQ: REQ-20260711-ATS-001
Agent: sa + pg
Depends On: WI-20260711-ATS-001, WI-20260711-ATS-002, WI-20260711-ATS-003, WI-20260711-ATS-004, WI-20260711-ATS-005
Blocks: WI-20260711-ATS-009, WI-20260711-ATS-017

[WI SUMMARY]
Why: Reconcile whitelist-channel and company-certification design, code, DB, UI, security policy, and client docs.
Scope (in/out): Cover states, plan limits, primary channel, delete/removal, CSV export, admin processing, certification apply/resubmit/reject/approve, document storage/download, role gates, migration, and client acceptance wording. Read-only except WI outputs.
DoD: Produce two complete 3-way matrices, confirmed findings, severity, policy questions, and focused tests.
Constraints/Forbidden: Do not export real data, upload/download documents, apply SQL, mutate statuses, or expose PII/storage paths in outputs.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Verify every whitelist and certification state transition against entity/service/API/UI/docs.
- [ ] Verify plan-limit, primary-channel, export claim, deletion/removal, and concurrency semantics.
- [ ] Verify BUSINESS/INDIVIDUAL/admin access, document validation, storage exposure, and resubmission rules.
- [ ] Reconcile schema/manual patches with JPA and current docs.
Performance:
- [ ] Assess unbounded export/list and storage growth risks with evidence.
Quality:
- [ ] Separate exploitable security defect, functional defect, policy ambiguity, and deferred automation.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md

Tier 2:
- docs/design/usecase/whitelist.md
- docs/design/usecase/company-certification.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/client/1-quick-checklist.md
- docs/client/2-full-feature-checklist.md
- docs/client/3-admin-checklist.md

REQ/Context Docs:
- deliverables/user/REQ-20260615-ATS-001.md
- deliverables/user/REQ-20260618-ATS-001.md
- deliverables/user/REQ-20260711-ATS-001.md
- deliverables/agent/WI-20260711-ATS-001-evidence-pack.md
- deliverables/agent/WI-20260711-ATS-002-evidence-pack.md
- deliverables/agent/WI-20260711-ATS-003-evidence-pack.md
- deliverables/agent/WI-20260711-ATS-004-evidence-pack.md
- deliverables/agent/WI-20260711-ATS-005-evidence-pack.md

Files:
- src/main/java/com/atstudio/atstudio/controller/WhitelistChannelController.java
- src/main/java/com/atstudio/atstudio/controller/AdminWhitelistChannelController.java
- src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java
- src/main/java/com/atstudio/atstudio/service/WhitelistChannelService.java
- src/main/java/com/atstudio/atstudio/service/AdminWhitelistChannelService.java
- src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java
- src/main/java/com/atstudio/atstudio/entity/WhitelistChannel.java
- src/main/java/com/atstudio/atstudio/entity/CompanyCertification.java
- src/main/resources/schema.sql
- src/main/resources/db/manual/
- frontend/src/pages/subscriber/WhitelistChannelPage.tsx
- frontend/src/pages/subscriber/CompanyCertApplyPage.tsx
- frontend/src/pages/subscriber/CompanyCertStatusPage.tsx
- frontend/src/pages/admin/WhitelistChannelManagePage.tsx
- frontend/src/pages/admin/CompanyCertManagePage.tsx

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260711-ATS-007-summary.md : Korean whitelist/certification verdict
Agent-facing -> deliverables/agent/WI-20260711-ATS-007-evidence-pack.md : matrices, findings, evidence, tests, follow-ups
Handoff Packet -> deliverables/agent/WI-20260711-ATS-007-handoff.md : this packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: Static/focused non-destructive inputs only
Rollback: Remove only this WI's two owned outputs if explicitly requested
