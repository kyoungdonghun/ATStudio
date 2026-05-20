---
wi_id: WI-20260520-ATS-001
req_id: REQ-20260519-ATS-001
agent: qa-integ
status: completed
created_at: 2026-05-20
---

# WI-20260520-ATS-001 Handoff: Payment Review Fixes

[WI HEADER]
WI ID: WI-20260520-ATS-001
REQ: REQ-20260519-ATS-001
Agent: qa-integ
Depends On: WI-20260519-ATS-005
Blocks: -

[WI SUMMARY]
Why: Post-commit review found payment edge cases that passed tests but could fail in real Toss billing calls or leave preview UX under-specified.
Scope (in/out): Fix whole-KRW upgrade charge rounding, zero-amount upgrade handling, subscription preview next-billing fields, docs, tests, and whitespace quality. Exclude live Toss charging and refund/reconciliation automation.
DoD: Tests/build/docs/diff checks pass and a follow-up commit contains only intended files.
Constraints/Forbidden: Do not stage unrelated 20260420 deliverables or runtime pid/log files.

[ACCEPTANCE CRITERIA]
Functional:
- [x] Upgrade prorated charge is rounded to whole KRW before Toss billing charge.
- [x] Zero-amount upgrade still requires an active billing agreement but skips provider charge.
- [x] Preview response includes next billing date and next billing amount.
- [x] Manage page displays next billing date and next billing amount before confirmation.
Quality:
- [x] Backend and frontend tests pass.
- [x] Frontend build/typecheck/lint pass.
- [x] Documentation validation passes.
- [x] Diff whitespace check passes for the final branch diff.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md

REQ/Context Docs:
- deliverables/user/REQ-20260519-ATS-001.md
- deliverables/agent/WI-20260519-ATS-005-evidence-pack.md
- docs/design/payment-integration-design.md
- docs/design/api-spec.md
- docs/design/usecase/user-subscription.md
- docs/design/usecase/util.md
- docs/ui/screen-flow.md
- docs/ui/modal-list.md

Files:
- src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java
- src/main/java/com/atstudio/atstudio/service/UtilService.java
- src/main/java/com/atstudio/atstudio/dto/util/SubscriptionChangePreviewResponse.java
- frontend/src/api/userSubscriptions.ts
- frontend/src/pages/subscriber/SubscriptionManagePage.tsx
- related backend/frontend tests

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260520-ATS-001-summary.md
Agent-facing -> deliverables/agent/WI-20260520-ATS-001-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260520-ATS-001-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: Include focused and broad verification commands
Rollback: Revert the follow-up commit
