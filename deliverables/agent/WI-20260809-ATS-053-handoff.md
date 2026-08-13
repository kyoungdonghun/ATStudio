---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: ma
category: wi-handoff
status: active
dependencies:
  - path: ../user/REQ-20260809-ATS-001.md
    reason: Approved correction authority and autonomous execution gate
  - path: WI-20260809-ATS-031-consolidated-findings.md
    reason: Canonical CR ownership, sequencing, and severity
  - path: WI-20260809-ATS-028-findings.md
    reason: Detailed reproduction evidence for the five ADMIN findings
---

# WI Handoff: WI-20260809-ATS-053

## WI Header

- **WI ID:** `WI-20260809-ATS-053`
- **REQ:** `REQ-20260809-ATS-001`
- **Agent:** `se`
- **Depends On:** `WI-20260809-ATS-036`, `WI-20260809-ATS-048`
- **Blocks:** `WI-20260809-ATS-054`, `WI-20260809-ATS-057`
- **Canonical findings:** `CR-031-094`, `CR-031-095`, `CR-031-096`,
  `CR-031-098`, `CR-031-101`

## WI Summary

### Why

Close five ADMIN product-surface defects without changing server authority or
business policy. Stale authority rejection must refresh the canonical session,
the contracted User detail endpoint must have a minimized read-only UI, list
responses must belong to the latest request, plan policy fields must remain
visible, and settings success must display the value actually persisted.

### Scope In

- `CR-031-094`: refresh the identity snapshot exactly once after typed stale
  ADMIN/403 rejection, then apply the canonical route guard without retrying the
  rejected mutation.
- `CR-031-095`: add an ADMIN-only User detail GET wrapper and accessible,
  read-only detail surface using only the existing bounded response contract.
- `CR-031-096`: add abort/generation ownership to License, Question, and Track
  collections; derive License selected-user identity from canonical URL/detail
  state; cover old/new response races.
- `CR-031-098`: expose audience and Playlist-limit fields in ADMIN subscription
  rows, including distinguishable same-name cross-audience fixtures.
- `CR-031-101`: prevent or version edits during settings save and reconcile the
  visible value with the canonical saved response/read before claiming success.
- Focused frontend/backend tests and current ADMIN operations/settings
  documentation updates required by these corrections.

### Scope Out

- Modal pending-mutation ownership (`CR-031-099`) and typed local correction
  execution (`CR-031-100`), which belong to WI-054.
- Question transition policy, tag deletion dependency copy, dashboard totals,
  reconciliation semantics, or any other unowned portfolio finding.
- New User detail fields, expanded PII, server role policy, schema/data,
  dependencies, provider/payment/refund/mail/export effects, or deployment.
- Broad Track management refactors beyond latest-request list ownership.

### Definition of Done

- Each finding has exact implementation and regression-test evidence.
- Typed stale ADMIN rejection refreshes identity once, uses canonical guard
  behavior, and never retries the rejected mutation.
- User detail is read-only, accessible, latest-request-owned, and limited to the
  existing minimized DTO; unauthorized roles remain rejected by the server.
- License, Question, and Track lists cannot be overwritten by stale requests,
  and deep-linked License selection has a canonical visible identity.
- ADMIN plan rows show audience and Playlist limits.
- Settings success cannot leave an unsent draft looking published; the public
  certification guide and ADMIN state agree with the canonical saved value.
- Focused tests, relevant backend contract tests, frontend full gates, document
  validation, diff check, independent PG review, independent QA, and Evidence
  Pack all pass.

## Constraints / Forbidden

- Do not inspect, open, hash, modify, stage, or delete
  `output/client-demo-screenshots-20260716-140514.zip` or `output/ui-ux-audit/`.
- Do not inspect ignored secrets or local environment values.
- Do not execute real Toss, payment, refund, mail, export/download, provider, or
  other external side effects.
- Do not change product policy, role authority, schema/data, dependencies,
  branches, deployment, or protected outputs.
- Do not expose credential material or add User fields beyond the existing
  server response contract.
- Preserve the existing server-side ADMIN authorization boundary.

## Acceptance Criteria

### Functional

- [ ] A typed stale ADMIN/403 rejection invokes one identity refresh, invokes no
      mutation retry, and leaves routing/navigation consistent with the refreshed role.
- [ ] User detail opens from the ADMIN list, renders only the existing bounded
      response fields, handles loading/error/close, and ignores stale detail responses.
- [ ] License User A responses cannot overwrite User B; old Question filters/pages
      and Track filters/pages cannot overwrite newer requests.
- [ ] A License `userId` deep link resolves a visible canonical User identity.
- [ ] Same-name INDIVIDUAL and BUSINESS plans render distinguishably and show
      Playlist limits, including unlimited semantics where contracted.
- [ ] Settings edits during save are either frozen or safely versioned, and a
      success state displays the canonical persisted text also consumed publicly.

### Security and Privacy

- [ ] Server-side ADMIN guards remain authoritative for list/detail/mutation calls.
- [ ] User detail exposes no password, token, credential, or field absent from
      the bounded response DTO.
- [ ] Independent `pg` review reports no open P0-P3 issue in session/PII scope.

### Quality

- [ ] Focused frontend tests and relevant backend User/Setting contract tests pass.
- [ ] Frontend coverage, typecheck, ESLint, Prettier, and production build pass.
- [ ] Independent QA reports no open P0-P3 finding in WI scope.
- [ ] Documentation validation and `git diff --check` pass.

## Input Pointers

### Tier 0

- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`

### Tier 1

- `docs/policies/security-policy.md`
- `docs/policies/access-control-policy.md`
- `docs/policies/quality-gates.md`

### Tier 2

- `docs/standards/frontend-standards.md`
- `.agents/skills/react-best-practices/AGENTS.md`
- `docs/design/api-spec.md`
- `docs/design/usecase/user-info.md`
- `docs/design/usecase/user-license.md`
- `docs/design/usecase/user-question.md`
- `docs/design/usecase/user-subscription.md`
- `docs/design/usecase/company-certification.md`
- `docs/payment/admin-operations-guide.md`

### REQ and Evidence

- `deliverables/user/REQ-20260809-ATS-001.md`
- `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:658-665`
- `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:987`
- `deliverables/agent/WI-20260809-ATS-028-findings.md:84-116`
- `deliverables/agent/WI-20260809-ATS-028-findings.md:131-138`
- `deliverables/agent/WI-20260809-ATS-028-findings.md:176-184`
- `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md`

### Primary Files

- `frontend/src/api/admin.ts`
- `frontend/src/store/authStore.ts`
- `frontend/src/pages/admin/UserManagePage.tsx`
- `frontend/src/pages/admin/UserManagePage.test.tsx`
- `frontend/src/pages/admin/LicenseManagePage.tsx`
- `frontend/src/pages/admin/QuestionManagePage.tsx`
- `frontend/src/pages/admin/TrackManagePage.tsx`
- `frontend/src/pages/admin/TrackManagePage.test.tsx`
- `frontend/src/pages/admin/SubscriptionManagePage.tsx`
- `frontend/src/pages/admin/SiteSettingsPage.tsx`

## Output Contract

- User-facing: `deliverables/user/WI-20260809-ATS-053-summary.md`
- Agent-facing: `deliverables/agent/WI-20260809-ATS-053-evidence-pack.md`
- Handoff: this file
- PG review: `deliverables/agent/WI-20260809-ATS-053-pg-result.md`
- Independent QA result: `deliverables/agent/WI-20260809-ATS-053-qa-result.md`

## Traceability Requirements

- Separate UI, request invocation, server authorization/response, and durable
  state evidence where applicable.
- Record request-race fixtures and exact invocation counts.
- Record commands and exact pass/fail counts, including any initial flaky run.
- Document rollback as file/commit-level reversion; no live provider or data
  rollback should be required.
