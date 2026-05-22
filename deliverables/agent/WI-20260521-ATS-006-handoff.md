[WI HEADER]
WI ID: WI-20260521-ATS-006
REQ: REQ-20260521-ATS-001
Agent: se
Depends On: WI-20260521-ATS-001, WI-20260521-ATS-002
Blocks: WI-20260521-ATS-009, WI-20260521-ATS-010

[WI SUMMARY]
Why: Provide read-only operator visibility into payment orders, billing agreements, and subscription payments.
Scope (in/out): In scope: sanitized admin APIs and simple admin UI. Out of scope: refund/cancel/settlement mutations.
DoD: Operators can inspect payment state without raw provider secrets or mutation actions.
Constraints/Forbidden: Read-only only; no refund, cancel, or billing-key operations.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Admin can list/search payment orders.
- [ ] Admin can list billing agreements with masked method and failure count.
- [ ] Admin can list subscription payment ledger records.
Quality:
- [ ] Admin-only authorization is enforced.
- [ ] Sensitive fields are sanitized.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md

REQ/Context Docs:
- deliverables/user/REQ-20260521-ATS-001.md
- docs/design/api-spec.md
- docs/design/payment-integration-design.md

Files:
- src/main/java/com/atstudio/atstudio/controller
- src/main/java/com/atstudio/atstudio/service
- src/main/java/com/atstudio/atstudio/repository
- frontend/src/pages/admin
- frontend/src/api/admin.ts

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260521-ATS-006-summary.md
Agent-facing -> deliverables/agent/WI-20260521-ATS-006-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260521-ATS-006-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: Include backend/frontend admin tests where added
Rollback: Document API/UI route changes
