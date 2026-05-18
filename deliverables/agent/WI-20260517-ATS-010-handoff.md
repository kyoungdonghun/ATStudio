---
wi_id: WI-20260517-ATS-010
req_id: REQ-20260517-ATS-002
agent: se
status: ready
created_at: 2026-05-17
---

# WI-20260517-ATS-010 Handoff: Billing Agreement API Flow

[WI HEADER]
WI ID: WI-20260517-ATS-010
REQ: REQ-20260517-ATS-002
Agent: se
Depends On: WI-20260517-ATS-005, WI-20260517-ATS-006, WI-20260517-ATS-007, WI-20260517-ATS-008, WI-20260517-ATS-009
Blocks: WI-20260517-ATS-011, WI-20260517-ATS-012

[WI SUMMARY]
Why: Users need an API flow to start recurring billing, confirm Toss billing-key auth, perform the initial charge, inspect agreement state, and cancel automatic renewal.
Scope (in/out): Add billing agreement controller/application service/DTOs for prepare, confirm, current status, and cancel. Initial recurring subscription activation must happen only after immediate first charge succeeds. Exclude frontend UI work and scheduler.
DoD: Authenticated users can register a Toss billing agreement in test mode, and initial recurring subscription is activated only after billing key issue and first charge success.
Constraints/Forbidden: Do not use legacy `POST /api/user-subscriptions` for recurring activation. Do not return raw billing keys.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Add `POST /api/payments/billing-agreements/prepare`.
- [ ] Add `POST /api/payments/billing-agreements/confirm`.
- [ ] Add `GET /api/payments/billing-agreements/me`.
- [ ] Add `DELETE /api/payments/billing-agreements/me`.
- [ ] Confirm validates authenticated user, agreement status, `customerKey`, plan, billing cycle, and expiry.
- [ ] Confirm issues billing key, stores encrypted key, creates initial `SUBSCRIBE` payment order, charges billing key, saves payment, and activates subscription only on charge success.
- [ ] Initial charge failure leaves no active subscription and returns retryable failure state.
- [ ] Cancel stops future renewal and preserves paid access until `expiresAt`.
Quality:
- [ ] Controller/service tests cover success, failure, owner mismatch, duplicate active agreement, and no raw key response.
- [ ] Existing one-time payment tests still pass.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred):
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md

REQ/Context Docs:
- deliverables/user/REQ-20260517-ATS-002.md
- deliverables/agent/WI-20260517-ATS-005-evidence-pack.md
- deliverables/agent/WI-20260517-ATS-006-evidence-pack.md
- deliverables/agent/WI-20260517-ATS-007-evidence-pack.md
- deliverables/agent/WI-20260517-ATS-008-evidence-pack.md
- deliverables/agent/WI-20260517-ATS-009-evidence-pack.md
- docs/design/payment-integration-design.md
- docs/design/api-spec.md

Files:
- src/main/java/com/atstudio/atstudio/controller/PaymentController.java
- src/main/java/com/atstudio/atstudio/service/
- src/main/java/com/atstudio/atstudio/dto/payment/
- src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java
- src/test/java/com/atstudio/atstudio/

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260517-ATS-010-summary.md :
- Summary, API behavior, risks, approval points
Agent-facing -> deliverables/agent/WI-20260517-ATS-010-evidence-pack.md :
- Evidence pointers, tests, rollback, downstream notes
Handoff Packet -> deliverables/agent/WI-20260517-ATS-010-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: Include focused service/controller tests and command results
Rollback: Document API/service/DTO files to revert
