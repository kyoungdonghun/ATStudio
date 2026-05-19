---
wi_id: WI-20260519-ATS-004
req_id: REQ-20260519-ATS-001
agent: docops
status: ready
created_at: 2026-05-19
---

# WI-20260519-ATS-004 Handoff: Payment Documentation Alignment

[WI HEADER]
WI ID: WI-20260519-ATS-004
REQ: REQ-20260519-ATS-001
Agent: docops
Depends On: WI-20260519-ATS-001
Blocks: WI-20260519-ATS-005

[WI SUMMARY]
Why: Payment docs still describe one-time and recurring subscription payment as parallel user-facing flows.
Scope (in/out): Retire SR-92, update design/API/DB/UI/use case docs, and record production-readiness checkpoints. Exclude admin payment screen implementation.
DoD: Docs describe recurring-first subscription payment and current code behavior.
Constraints/Forbidden: Do not document raw secret values or test keys.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] SR-92 status is closed as retired/deprecated.
- [ ] Payment integration design states recurring-first subscription policy.
- [ ] API spec describes upgrade through subscription change endpoint with billing-key charge.
- [ ] UI flow no longer points upgrade to one-time checkout.
Quality:
- [ ] Docs validation passes or any residual doc validation issue is reported.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

REQ/Context Docs:
- deliverables/user/REQ-20260519-ATS-001.md
- docs/design/payment-integration-design.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/design/usecase/user-subscription.md
- docs/ui/screen-flow.md
- docs/ui/modal-list.md
- docs/ui/atstudio-front-list.md
- docs/SR/index.md
- docs/SR/SR-92.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260519-ATS-004-summary.md
Agent-facing -> deliverables/agent/WI-20260519-ATS-004-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260519-ATS-004-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: Include docs validation result
Rollback: Revert changed docs
