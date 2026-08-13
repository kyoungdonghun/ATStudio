---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: ma
category: wi-remediation-handoff
status: active
dependencies:
  - path: WI-20260809-ATS-053-qa-result.md
    reason: Independent QA P2 findings requiring closure
  - path: WI-20260809-ATS-053-handoff.md
    reason: Original latest-request, detail, and plan-field contracts
---

# Remediation R2 Handoff: WI-20260809-ATS-053

## Assignment

- **Agent:** `se`
- **Findings:** `QA-FE-053-001`, `QA-FE-053-002`, `QA-FE-053-003`
- **Purpose:** close three functional/responsive gaps found by independent QA.

## Required Changes

### License Search Ownership

- Retire and abort pending User search on input edits, canonical User selection,
  URL/user context changes, and unmount.
- Bind result publication to both request generation and the normalized
  submitted keyword/current input context.
- Add deferred tests for keyword A resolving after edit to B and pending search
  resolving after canonical `userId` navigation/selection.

### Mobile User Detail

- Keep a visible, operable User detail action at <= 767px. Preserve the current
  read-only modal and do not fold this into role mutation controls.
- Add a responsive-contract regression proving the detail action is not hidden
  by the mobile column rules and the mobile entry can open/retry/close.

### Mobile Subscription Plan Table

- Remove stale positional hiding rules. Preserve audience, monthly/yearly
  prices, download/channel/Playlist limits, and status on mobile.
- Prefer the existing scroll container with a stable table min-width rather
  than silently hiding contracted fields.
- Add a responsive-contract regression that protects all eight columns and
  unlimited Playlist semantics from positional hiding regressions.

## Acceptance Criteria

- [ ] Retired License search responses publish no rows/dropdown/loading/error.
- [ ] Mobile ADMIN can open, retry, and close User detail.
- [ ] Mobile plan table retains all contracted fields through horizontal scroll.
- [ ] Existing desktop behavior, authority/PII PG PASS, request races, and
      settings behavior remain green.
- [ ] Focused tests, typecheck, ESLint, Prettier, relevant docs, and diff check pass.
- [ ] No schema/data/dependency/policy/external-effect change.

## Input Pointers

### Tier 0

- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`

### Tier 1/2

- `docs/policies/quality-gates.md`
- `docs/standards/frontend-standards.md`
- `.agents/skills/react-best-practices/AGENTS.md`

### Evidence and Files

- `deliverables/agent/WI-20260809-ATS-053-qa-result.md`
- `deliverables/agent/WI-20260809-ATS-053-evidence-pack.md`
- `frontend/src/pages/admin/LicenseManagePage.tsx`
- `frontend/src/pages/admin/LicenseManagePage.test.tsx`
- `frontend/src/pages/admin/UserManagePage.tsx`
- `frontend/src/pages/admin/UserManagePage.module.css`
- `frontend/src/pages/admin/UserManagePage.test.tsx`
- `frontend/src/pages/admin/SubscriptionManagePage.tsx`
- `frontend/src/pages/admin/SubscriptionManagePage.module.css`
- `frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx`

## Output Contract

- Edit implementation/tests/current docs/evidence in place; do not commit/push.
- Preserve the initial QA FAIL and append remediation evidence transparently.
- Do not create QA R2 result; it remains reviewer-owned.

## Constraints

- Do not inspect, open, hash, modify, stage, or delete protected output paths.
- Do not inspect ignored secrets/local environment values.
- No external effects, schema/data, dependencies, policy, branches, or deployment.
