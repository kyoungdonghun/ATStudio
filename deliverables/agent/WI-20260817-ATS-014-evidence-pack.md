---
version: 1.0
last_updated: 2026-08-17
project: ATS
owner: se
category: evidence-pack
status: complete
related_wi: WI-20260817-ATS-014
dependencies:
  - path: WI-20260817-ATS-014-handoff.md
    reason: Approved dependency upgrade scope, compatibility constraints, and DoD
  - path: ../user/REQ-20260817-ATS-009.md
    reason: Approved production dependency-audit remediation request
  - path: WI-20260817-ATS-013-evidence-pack.md
    reason: Predecessor test-only repair baseline that must be preserved
  - path: ../../docs/SR/SR-93.md
    reason: Current security-acceptance context
---

# Evidence Pack: WI-20260817-ATS-014

## Summary

- Upgraded the Vite SPA dependency from React Router DOM 6.30.4 to pinned
  React Router DOM 7.18.2, retaining the existing data-router SPA model.
- Removed V6-only future props from production bootstrap and affected test
  harnesses. V7 now supplies that behavior without the removed prop.
- Replaced the removed `react-router-dom/server` test import with the V7
  `react-router-dom` re-export for `StaticRouter`.

## Scope / DoD Check

- [x] `react-router` and `react-router-dom` resolve to 7.18.2.
- [x] Registry requirements were verified before version selection: Node
  `>=20.0.0`, React `>=18`, and ReactDOM `>=18`; host Node 24.14.0 and
  installed React/ReactDOM 18.3.1 satisfy them.
- [x] Browser data-router initialization remains `createBrowserRouter(routes)`
  with `RouterProvider`; no framework-mode conversion was made.
- [x] Route guards, redirects, query navigation, lazy routes, and `useBlocker`
  coverage passed in the complete frontend suite.
- [x] No V6 `future` prop or `react-router-dom/server` subpath import remains
  under `frontend/src`.
- [x] `npm audit --omit=dev` reports no remaining production vulnerabilities.
- [x] All required frontend quality gates and `git diff --check` passed.

## Reference Documents

| Tier | Document | Use |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Approved scope and traceability control |
| 0 | `docs/standards/development-standards.md` | Dependency-change verification discipline |
| 1 | `docs/policies/security-policy.md` | Production dependency-audit boundary |
| 1 | `docs/policies/quality-gates.md` | Required frontend quality gates |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | React compatibility context |
| 2 | `docs/design/p1-security-acceptance-hardening-design.md` | Existing SPA security-acceptance context |
| Context | `deliverables/user/REQ-20260817-ATS-009.md` | Approved request |
| Context | `deliverables/agent/WI-20260817-ATS-013-evidence-pack.md` | Preserved test-repair baseline |
| Context | `docs/SR/SR-93.md` | Current security-acceptance context |

## Dependency Evidence

- Before change, `npm ls react react-dom react-router react-router-dom --depth=0`
  resolved React 18.3.1, ReactDOM 18.3.1, and React Router DOM 6.30.4.
- Before change, `npm audit --omit=dev --json` reported 2 moderate production
  vulnerability records for `react-router` and `react-router-dom`; the audit
  fix available was `react-router-dom@7.18.2`. The report included
  `GHSA-wrjc-x8rr-h8h6`, `GHSA-337j-9hxr-rhxg`, and `GHSA-jjmj-jmhj-qwj2`.
- `npm view react-router-dom@7.18.2 peerDependencies --json` returned React
  and ReactDOM `>=18`; `npm view react-router-dom@7.18.2 engines --json`
  returned Node `>=20.0.0`. The equivalent `react-router@7.18.2` queries
  returned the same requirements.
- `frontend/package.json:21` pins `react-router-dom` to `7.18.2`.
- `frontend/package-lock.json:4342-4378` resolves React Router and React
  Router DOM to 7.18.2, replaces V6 `@remix-run/router`, and records the V7
  Node/peer constraints.

## Source Compatibility Changes

- `frontend/src/App.tsx:1-6` keeps `RouterProvider` with the existing router
  and removes the V6-only `future` prop.
- `frontend/src/router/index.tsx:1-220` was inspected and remains the existing
  `createBrowserRouter(routes)` data-router configuration; no route contract
  was changed.
- Deleted `frontend/src/router/routerFuture.ts`, which only contained the
  obsolete V6 future flags after all consumers were migrated.
- `frontend/src/router/LazyRoute.test.tsx:1-34`,
  `frontend/src/router/ProtectedRoute.test.tsx`, and
  `frontend/src/router/SubscriberRoute.test.tsx:1-113` remove V6 future props;
  the latter imports `StaticRouter` from `react-router-dom` rather than the
  removed server subpath.
- Removed only obsolete future props from affected router harnesses in:
  `frontend/src/components/catalogComponents.test.tsx`;
  `frontend/src/layouts/{AdminLayout,Header,MainLayout,PlayerBar,navigationFocus.crossLayout}.test.tsx`;
  `frontend/src/pages/admin/{LicenseManagePage,QuestionManagePage,TrackManagePage,UserManagePage}.test.tsx` and `UserManagePage.integration.test.tsx`;
  `frontend/src/pages/auth/{EmailVerifyPage,LoginPage,PasswordResetPage,SignupPage,SocialCompleteProfilePage,SocialLoginPage}.test.tsx`;
  `frontend/src/pages/creator/{AlbumCreatePage,AlbumEditPage,AlbumManagePage,TrackEditPage,TrackUploadPage}.test.tsx`;
  `frontend/src/pages/public/{AlbumDetailPage,AlbumListPages,HomePage,NoticeListPage,SubscriptionPlanPage,TrackDetailPage,TrackListPage}.test.tsx`;
  `frontend/src/pages/subscriber/{CompanyCertApplyPage,CompanyCertStatusPage,DownloadHistoryPage,LicenseDetailPage,LikeListPage,PlaylistDetailPage,PlaylistEditPage,PlaylistListPage,ProfilePage,QuestionDetailPage,QuestionListPage,SubscriptionManagePage,SubscriptionPaymentPage,SubscriptionPaymentReplay}.test.tsx`; and
  `frontend/src/test/coverage/{adminSubscriberGaps,adminSubscriberPages,publicAuthShell,shellCatalogRouterGaps}.coverage.test.tsx`.
- Existing WI-013 behavioral assertions in the dirty coverage files were
  preserved; this WI removed only their incompatible router props.

## Reproduction And Results

| Command | Result |
| --- | --- |
| `npm ls react react-dom react-router react-router-dom --depth=0` before | React/ReactDOM 18.3.1; React Router DOM 6.30.4. |
| `npm audit --omit=dev --json` before | 2 moderate production React Router vulnerability records; fix available: 7.18.2. |
| `npm view react-router-dom@7.18.2 peerDependencies engines --json` | React/ReactDOM `>=18`, Node `>=20.0.0`; host satisfies both. |
| `npm install` | PASS; intentional `frontend/package-lock.json` refresh, 2 packages added, 1 removed, 2 changed. |
| `npm run typecheck` | PASS. |
| `npm run lint` | PASS with `--max-warnings 0`. |
| `npm run format` | PASS: all matched files use Prettier code style. |
| `npm test -- --run` | PASS: 111 test files, 1,440 tests. No React Router V6 deprecation warning emitted. |
| `npm run build` | PASS: Vite production build completed; 301 modules transformed. |
| `npm audit --omit=dev --json` after | PASS: 0 vulnerabilities (0 info, 0 low, 0 moderate, 0 high, 0 critical). |
| `rg -n -g '*.ts' -g '*.tsx' 'future=|future:\\s*\\{|v7_[A-Za-z]+|react-router-dom/server' src` | No matches; exit 1 is the expected `rg` no-match status. |
| `git diff --check` | PASS: no whitespace errors. Existing unrelated CRLF advisory messages were emitted without failure. |

## Risks / Rollback

- Risk: The worktree already contained unrelated frontend, backend, and
  documentation modifications. This WI did not revert or modify their logic;
  the full-suite result therefore reflects the current combined worktree.
- Rollback: Revert `frontend/package.json`, `frontend/package-lock.json`,
  `frontend/src/App.tsx`, the listed router/test harness hunks, and restore
  `frontend/src/router/routerFuture.ts` in one atomic commit. No backend, DB,
  configuration, secret, output, or data rollback is required.
