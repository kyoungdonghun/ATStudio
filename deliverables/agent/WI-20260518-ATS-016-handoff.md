[WI HEADER]
WI ID: WI-20260518-ATS-016
REQ: REQ-20260518-ATS-001
Agent: docops
Depends On: WI-20260518-ATS-013
Blocks: WI-20260518-ATS-018

[WI SUMMARY]
Why: Reflect the approved payment UX state flow into UI documentation.
Scope (in/out): In scope: subscription payment screen flow, billing success/fail callbacks, subscription management auto-renewal state display, SR-92 relation. Out of scope: frontend code implementation.
DoD: UI docs describe the production target UX while preserving the current debug-friendly page-fixed implementation note.
Constraints/Forbidden: Do not remove SR-92 context; convert it into follow-up UX work instead of treating current behavior as a bug.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] `docs/ui/screen-flow.md` includes checkout separation target and fallback paths.
- [ ] `docs/ui/modal-list.md` or equivalent UI docs identify needed checkout modal/dedicated step candidates.
- [ ] User-facing states align with recurring billing agreement states.
Performance:
- [ ] No runtime performance requirement; this is documentation-only.
Quality:
- [ ] `validate-docs` passes.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 2 / Context:
- docs/ui/screen-flow.md
- docs/ui/modal-list.md
- docs/SR/SR-92.md
- docs/design/payment-integration-design.md
- deliverables/user/REQ-20260518-ATS-001.md

Files:
- frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx
- frontend/src/pages/subscriber/SubscriptionManagePage.tsx

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260518-ATS-016-summary.md :
- UI documentation changes and UX decision summary.
Agent-facing -> deliverables/agent/WI-20260518-ATS-016-evidence-pack.md :
- Changed docs, rationale, and validation results.
Handoff Packet -> deliverables/agent/WI-20260518-ATS-016-handoff.md :
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Run `python .agents/skills/validate-docs/scripts/validate_docs.py`.
Rollback (if needed): Revert UI docs tied to this WI.
