[WI HEADER]
WI ID: WI-20260809-ATS-005
REQ: REQ-20260808-ATS-004
Agent: cr
Depends On: WI-20260809-ATS-004
Blocks: WI-20260808-ATS-028

[WI SUMMARY]
Why: Review the admin role and subscription-correction frontend/API contract separately from backend state-machine analysis.
Scope (in/out): Admin UI authorization assumptions, preview/request/approve/execute contract, dangerous confirmations, stale-state handling, error/PII display, and API typing. Backend implementation internals are covered by WI-004.
DoD: Evidence-backed BLOCKER/MAJOR/MINOR findings or an explicit no-findings result, with tight file-line pointers and residual UX/test risks.
Constraints/Forbidden: Read-only review. Do not modify code, tests, data, secrets, the intentional ZIP, or external services. Do not commit or push.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Verify frontend/API state and action availability against SR-96/SR-97.
- [ ] Verify confirmation, stale response, and failure behavior for privileged actions.
Performance:
- [ ] Note repeated fetches or race-prone UI state only when they affect correctness.
Quality:
- [ ] Every finding has severity, evidence, impact, and a recommended repair/test.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md

Context:
- deliverables/user/REQ-20260808-ATS-004.md
- docs/SR/SR-96.md
- docs/SR/SR-97.md
- deliverables/user/WI-20260809-ATS-004-summary.md
- deliverables/agent/WI-20260809-ATS-004-evidence-pack.md

Files:
- frontend/src/api/admin.ts
- frontend/src/pages/admin/UserManagePage.tsx
- frontend/src/pages/admin/UserManagePage.test.tsx
- frontend/src/pages/admin/UserSubscriptionManagePage.tsx
- frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx
- frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx
- related response/request types and backend controller DTO contracts

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-005-summary.md
Agent-facing -> deliverables/agent/WI-20260809-ATS-005-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260809-ATS-005-handoff.md

[TRACEABILITY REQUIREMENTS]
Reviewed components/contracts, findings, residual risks, rollback implications, and WI-028 block status are required.
