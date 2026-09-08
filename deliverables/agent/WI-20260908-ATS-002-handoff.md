---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: se
category: reference
status: active
dependencies:
  - path: ../user/REQ-20260908-ATS-001.md
    reason: Approved bounded safety fixes
---

# WI-20260908-ATS-002: Payment confirmation safety

[WI HEADER]
REQ: REQ-20260908-ATS-001 (approved)
Agent: se
Depends On: WI-20260908-ATS-001
Blocks: WI-20260908-ATS-004

[WI SUMMARY]
Why: Actual acceptance exposed confusing failed-card callbacks, annual upgrade charges labelled with future monthly cadence, and an entitlement correction date silently initialized to today.
Scope: Small frontend fixes and focused tests only, preserving backend billing/entitlement policies.
DoD: Clear authoritative vs reported failure distinction; explicit amount/current-period confirmation before paid upgrade; no accidental today default for correction; regressions pass.
Forbidden: Backend/API/DB changes, raw provider messages, new libraries, redesign, name changes, real mutations, server restart, other worktrees, unrelated files.

[ACCEPTANCE CRITERIA]
- [ ] On failed callback, a bounded allowlist code (especially INVALID_CARD_NUMBER) can provide a clearly reported card-registration hint, but URL data never declares final financial state, triggers charge/prepare, or bypasses UNKNOWN safeguards. Unknown, duplicate/empty codes, DONE precedence, safe route recovery tested.
- [ ] Upgrade preview separates current retained cycle/period from next billing cycle. A deliberate confirmation exposes exact immediate amount, retained end date and next-cycle date/amount before calling mutation. Stale preview, repeated-click and unmount guards remain effective; use existing UI controls/confirmation patterns.
- [ ] Refund entitlement-correction target date is explicitly chosen, not silently today; confirmation includes target status, expiry and cancellation effect. Preserve preview/approval/execute and input invalidation guards. No new correction cancel endpoint.
- [ ] Focused Vitest, typecheck, lint, changed-file Prettier pass. No provider interaction is needed.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; docs/standards/development-standards.md; docs/standards/documentation-standards.md; docs/standards/glossary.md.
Tier 1: docs/policies/security-policy.md; docs/policies/quality-gates.md.
Tier 2: docs/payment/acceptance-test-checklist.md; docs/payment/admin-operations-guide.md; docs/design/payment-integration-design.md; frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx; SubscriptionManagePage.tsx; frontend/src/pages/admin/PaymentOperationsPage.tsx; corresponding tests; existing confirmation component.
Skill chain: react-best-practices -> test/typecheck/eslint/prettier -> create-wi-evidence-pack. Rule source .claude/config/context-injection-rules.json, se required core/development and React task context.

[WRITE OWNERSHIP]
- The three named page .tsx files and their corresponding .test.tsx files.
- Their existing CSS modules only if necessary for the small confirmation UI.
- Own evidence pack and user summary.

[OUTPUT CONTRACT]
Use create-wi-evidence-pack for deliverables/agent/WI-20260908-ATS-002-evidence-pack.md and deliverables/user/WI-20260908-ATS-002-summary.md. Record exact changes, test counts, unresolved matters, policy unchanged, no external mutations, file-only rollback, next WI-004.

[EXECUTION SAFETY]
Main worktree C:/Users/jm991/Desktop/project/ATStudio, codex/v1-release-rehearsal-fixes only. Use apply_patch. No nested agents, no commit or push. Public Vite may HMR source edits; do not restart either server. Notify MA if a backend contract change is essential rather than forcing a misleading safe-retry claim.
