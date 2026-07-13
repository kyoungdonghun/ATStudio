# User Summary: WI-20260711-ATS-018

## Verdict

WI-018 is complete as a read-only frontend adjudication. No source fixes, browser state changes, form submissions, uploads, payments, cancellations, refunds, exports, staging, commits, or cleanup actions were performed.

Decision-ready result: the React SPA is buildable and the configured frontend regression suite passes, but the current client experience is not release-clean for core journeys. The blocking items are not all frontend-owned. The highest-risk release blockers are backend/content-delivery and backend/payment defects surfaced through UI journeys, plus one confirmed frontend-owned social-login break.

## Baseline

- Approved REQ confirmed: `deliverables/user/REQ-20260711-ATS-001.md` has `Status: approved`.
- Handoff read first and used as the execution contract: `deliverables/agent/WI-20260711-ATS-018-handoff.md`.
- Shared dirty worktree was preserved. Pre-existing tracked/untracked changes were treated as user/other-agent work.
- Only WI-018 outputs were authored:
  - `deliverables/user/WI-20260711-ATS-018-summary.md`
  - `deliverables/agent/WI-20260711-ATS-018-evidence-pack.md`
- Quality baseline consumed from predecessor packs:
  - Frontend tests: 14/14 files, 51/51 tests passed (`WI-010`).
  - Java compile and frontend typecheck passed (`WI-011`).
  - ESLint passed, but Prettier check failed with 143 drift files (`WI-012`).
  - Backend and frontend production builds passed (`WI-013`).
  - Coverage is not measurable with current tooling; this is an instrumentation gap, not 0% coverage (`WI-015`).

## Adjudicated Blockers and Findings

### Fix-now blockers

| ID | Status | Priority | Ownership | Finding |
|---|---|---:|---|---|
| FE-001 | Confirmed | P1 | Frontend-owned | Fresh social login can fail because `SocialLoginPage` calls `fetchMe()` without the returned access token, unlike password login which calls `fetchMe(tokens.accessToken)`. Evidence: `frontend/src/pages/auth/SocialLoginPage.tsx:44-45`, `frontend/src/api/auth.ts:99-103`, `frontend/src/pages/auth/LoginPage.tsx:125-144`. |
| PAY-006-08 | Confirmed | P1 | Cross-layer contract | ADMIN can enter member checkout from frontend route/CTA paths, and predecessor evidence says backend prepare also lacks exclusive member rejection. Frontend must add role UX guards, but server-side ADMIN rejection is the required control. Evidence: `frontend/src/router/index.tsx:153-160`, `frontend/src/router/ProtectedRoute.tsx:7-24`, `frontend/src/pages/public/SubscriptionPlanPage.tsx:162-178`, `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx:127-163`, `WI-006-evidence-pack.md:89-97`. |
| ATS008-01 | Confirmed | P0 | Backend defect surfaced in UI | Anonymous original-audio static retrieval bypasses subscription/quota/license controls. This is not frontend-owned merely because public track UI exposes the journey; frontend should stop depending on master paths, but backend/static delivery must close the bypass. Evidence: `WI-008-evidence-pack.md:89-105`. |
| ATS008-02 | Confirmed | P0 | Backend defect surfaced in UI | Live verification/reset capability and PII can be logged on SMTP failure. Frontend has no adequate mitigation for leaked backend mail tokens. Evidence: `WI-008-evidence-pack.md:106-117`. |
| PAY-006-01 | Confirmed | P0 | Backend defect surfaced in UI | Account withdrawal can leave active billing eligible for renewal. The user-facing profile flow exposes the business risk, but renewal/withdrawal orchestration is backend-owned. Evidence: `WI-006-evidence-pack.md:89`. |

### Decision-needed P1 items

| ID | Status | Priority | Ownership | Finding |
|---|---|---:|---|---|
| ATS008-03 | Conditional | P1 | Backend defect surfaced in UI | Subscriber playlist thumbnail upload can store active/non-image content in public upload storage. P0 escalation depends on production same-origin delivery. Frontend validation can reduce bad inputs, but server decode/re-encode and upload-origin isolation are the required controls. Evidence: `WI-008-evidence-pack.md:118-131`. |
| ATS007-F06 | Confirmed | P1 | Backend defect surfaced in UI | Company-certification documents are extension/size/count checked, client MIME is persisted, and admin UI downloads the submitted file. Frontend currently enables the admin-open path; safe validation/quarantine/download policy belongs server-side. Evidence: `WI-007-evidence-pack.md:166-171`, `frontend/src/pages/admin/CompanyCertManagePage.tsx:150`, `frontend/src/api/admin.ts:104-121`. |
| ATS007-F10 | Conditional | P1 | Documentation/test coverage | Existing DB migration/backfill for company certification is not release-verifiable from repo state. UI handles legacy no-document rows, but this is a migration decision and copied-DB verification issue. Evidence: `WI-007-evidence-pack.md:197-203`. |

### P2 frontend-owned remediation

| ID | Status | Priority | Ownership | Finding |
|---|---|---:|---|---|
| FE-005 / PAY-006-13 | Confirmed | P2 | Frontend-owned | Track filtering and admin payment tabs/pages can commit stale responses because requests have no abort/latest-request guard. Evidence: `frontend/src/pages/public/TrackListPage.tsx:151-185`, `frontend/src/pages/admin/PaymentReadOnlyPage.tsx:179-234`, `WI-006-evidence-pack.md:101`. |
| ATS007-F07 | Confirmed | P2 | Frontend-owned | Company certification routes are only USER-gated, so INDIVIDUAL users can see the BUSINESS-only apply form before backend rejection. Evidence: `frontend/src/router/index.tsx:163-164`, `frontend/src/pages/subscriber/CompanyCertApplyPage.tsx:54-87`, `WI-007-evidence-pack.md:173-179`. |
| FE-003 | Confirmed | P2 | Frontend-owned | `SubscriberRoute` treats every subscription-check rejection as no subscription, so 401/timeout/500 and inactive subscription collapse into the same redirect. Evidence: `frontend/src/router/SubscriberRoute.tsx:25-57`, `WI-003-evidence-pack.md:98-100`. |
| FE-008 | Confirmed | P2 | Frontend-owned | Toast/header/player/pagination accessibility gaps remain, with no focused a11y tests. Evidence: `WI-003-evidence-pack.md:247-263`. |
| FE-009 | Confirmed | P2 | Frontend-owned | Add-to-playlist modal fetches and success timer can outlive close/reopen lifecycle. Evidence: `frontend/src/components/playlist/AddToPlaylistModal.tsx:28-61`. |
| FORMAT-012 | Confirmed | P2 | Documentation/test coverage | Prettier gate fails on 143 files. This is not a functional blocker by itself, but it will make future fixes noisy unless handled separately. Evidence: `WI-012-evidence-pack.md`. |

## Ranked Remediation

1. Fix backend P0s first: protected media/static delivery, mail-token logging, and withdrawal/renewal billing state.
2. Fix the frontend-owned social login token ordering and add an empty-storage social callback test.
3. Add server-side ADMIN rejection for member checkout and frontend ADMIN-safe redirects/messages for catalog and direct checkout URLs.
4. Decide and implement upload/document safety policy: playlist thumbnail isolation and certification document quarantine/safe download.
5. Add company-certification BUSINESS route/page gate and route-matrix tests for BUSINESS, INDIVIDUAL, ADMIN.
6. Add latest-request-wins behavior to track search and admin payment tabs, then cover with deferred-promise tests.
7. Add focused tests before broad refactors: social callback, role checkout, company route matrix, upload safety, stale request ordering, accessibility primitives.
8. Run a separate formatting-baseline WI for the 143 Prettier drift files; do not mix it into feature fixes.
9. Add coverage instrumentation under a separate approved WI; current pass/fail tests do not provide line/branch coverage.

## Risks and Decisions

- Production topology, real DB migration state, storage origin, mail logs, malware scanning, and provider state were not accessed. Conditional findings remain conditional.
- The configured tests passing does not close missing focused cases for social login, ADMIN checkout, company certification, whitelist, admin payment request ordering, accessibility, or upload-origin safety.
- No source fixes were made. A separate approved remediation REQ/WI is required before changing code, schema, tests, or policies.
