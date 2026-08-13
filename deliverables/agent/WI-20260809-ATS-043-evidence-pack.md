---
version: 1.0
last_updated: 2026-08-13
project: ATS
owner: se
category: evidence-pack
status: draft
related_wi: WI-20260809-ATS-043
---

# Evidence Pack - WI-20260809-ATS-043

## Change Summary

- Centralized safe, internal login return-target construction and post-authentication access revalidation across route guards, player actions, password login, and social login completion.
- Moved `SubscriberRoute` toast mutation into an effect, adopted supported React Router future behavior in production and test routers, and added bounded application-owned lazy import recovery.
- Preserved pathname plus query and excluded hashes, preserved the `/error` server-error route and public wildcard 404 semantics, and did not change WI-057 shell keyboard/focus semantics.
- Addressed independent PG findings by using one decoded/lowercase canonical pathname for access classification and binding one-time OAuth profile continuation to the authenticated user ID.

## Scope / DoD Check

- [x] `CR-031-002`: anonymous `SubscriberRoute` redirects retain a validated pathname and query; warning mutation is outside render and deduplicated.
- [x] `CR-031-005`: production and test routers opt into the supported transition future behavior without a dependency change.
- [x] `CR-031-047`: desktop and mobile Player actions retain a validated internal origin.
- [x] `CR-031-129`: lazy import rejection has localized recovery, one fresh retry, retained URL, safe Home/Back actions, and no raw error disclosure or retry/reload loop.
- [x] Unsafe, malformed, login-loop, non-route, ADMIN, USER-payment, and BUSINESS-only targets are rejected at the appropriate validation phase.
- [x] Existing `/error`, public wildcard 404, protected route, subscriber route, and ADMIN route semantics remain covered.
- [x] PG-remediation focused and adjacent tests pass, and MA final frontend, backend, documentation, and diff quality gates pass.
- [x] Independent PG re-review PASS: the prior P2 canonical-path authorization issue and P3 OAuth continuation issue are closed, with no new open redirect, encoding bypass, cross-account replay, token/secret storage, or fail-open finding.

## Reference Documents

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | `AGENTS.md` | Repository and role rules |
| 0 | `docs/standards/core-principles.md` | Constitution and traceability |
| 0 | `docs/standards/development-standards.md` | Frontend implementation and test expectations |
| 1 | `docs/policies/security-policy.md` | Untrusted navigation and disclosure controls |
| 1 | `docs/policies/access-control-policy.md` | Role, user-type, and subscription boundaries |
| 1 | `docs/policies/quality-gates.md` | Required verification gates |
| 1 | `docs/standards/evidence-pack-standard.md` | Evidence structure and reproducibility |
| 1 | `docs/standards/frontend-standards.md` | Current route and recovery contract |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | React implementation guidance |
| 2 | `deliverables/user/REQ-20260809-ATS-001.md` | Approved requirement |
| 2 | `deliverables/agent/WI-20260809-ATS-043-handoff.md` | WI scope, findings, and output contract |

## Evidence Pointers

### Return Targets and Access Revalidation

- `frontend/src/utils/loginReturn.ts:getSafeLoginReturnTarget` - bounded structural validation; same-origin pathname/query normalization; hash, backslash, control, API/upload, and authentication-loop rejection.
- `frontend/src/utils/loginReturn.ts:getAccessibleLoginReturnTarget` - post-identity ADMIN, USER checkout, and BUSINESS certification checks using the same percent-decoded, lowercase canonical pathname as structural classification while preserving the exact validated target on authorization.
- `frontend/src/utils/loginReturn.ts:createLoginPath` - shared encoded `/login?returnTo=...` construction.
- `frontend/src/router/ProtectedRoute.tsx` and `frontend/src/router/SubscriberRoute.tsx` - shared guest return handling.
- `frontend/src/layouts/PlayerBar.tsx` - shared desktop/mobile guest action return handling.
- `frontend/src/pages/auth/LoginPage.tsx`, `SocialLoginPage.tsx`, and `SocialCompleteProfilePage.tsx` - access-aware revalidation after identity is known.
- `frontend/src/utils/oauthAttempt.ts` - shared structural validation for OAuth records; profile continuation clears any old value before storage, binds to an exact user ID, and deletes before one-time identity validation.
- `frontend/src/pages/auth/SocialLoginPage.tsx` - incomplete-profile storage failure explicitly proceeds to `/complete-profile` without a continuation.
- `frontend/src/pages/auth/SocialCompleteProfilePage.tsx` - consumes continuation only for the refreshed user ID, then applies role/user-type access validation.

### Router and Lazy Recovery

- `frontend/src/router/routerFuture.ts` and `frontend/src/App.tsx` - centralized production future configuration; package manifests and lockfiles are unchanged.
- `frontend/src/router/LazyRoute.tsx` - fresh lazy component per bounded retry and handled loader rejection.
- `frontend/src/router/LazyRouteRecovery.tsx` and `LazyRoute.module.css` - localized recovery UI, retained SPA location, safe Back fallback, and Home action.
- `frontend/src/router/index.tsx` - all existing lazy page imports use the recovery factory; route objects and wildcard placement are unchanged.
- `frontend/src/router/index.test.tsx` - `/error`, public wildcard 404, and ADMIN wildcard-absence assertions.

### Subscriber Side Effects and Transition Race

- `frontend/src/router/SubscriberRoute.tsx:redirectReason` effect - toast store mutation occurs after render and each redirect reason is warned once per mounted guard.
- `frontend/src/pages/subscriber/PlaylistListPage.tsx:handledCreateRequestKeyRef` is a production correctness change required by the adopted `v7_startTransition` behavior, not a test-only accommodation.
- Runtime sequence reproduced before the guard: `handleCreate()` closed the modal and scheduled `navigate(..., { replace: true })`; the immediate playlist refresh completed while that navigation was still transitional; the route-state effect observed the old `openCreate: true` location again and reopened the modal. The failure DOM already showed `/playlists:null` by assertion time while the reopened dialog remained mounted.
- `handledCreateRequestKeyRef` records the location key before opening, so the same one-shot request cannot be consumed twice; a later navigation with a new key remains eligible.
- `frontend/src/pages/subscriber/PlaylistListPage.test.tsx` assertion `consumes the create route request once when refresh races the transition` verifies that both initial and post-create fetches complete (`2` calls), route state is cleared, and the modal remains closed.

### Documentation

- `docs/standards/frontend-standards.md` - shared return-target, future behavior, and lazy recovery policy.
- `docs/ui/screen-flow.md`, `docs/ui/atstudio-front-list.md` - current route and failure presentation.
- `docs/design/usecase/user-info.md` - password/social post-login access revalidation.
- `docs/index.md`, `docs/client/_internal-feature-map.md` - current lazy route implementation pointers.

### Tests Changed

- Behavioral tests: `frontend/src/utils/loginReturn.test.ts`, `oauthAttempt.test.ts`, `frontend/src/router/ProtectedRoute.test.tsx`, `SubscriberRoute.test.tsx`, `LazyRoute.test.tsx`, `index.test.tsx`, `frontend/src/layouts/PlayerBar.test.tsx`, `frontend/src/pages/auth/LoginPage.test.tsx`, `SocialLoginPage.test.tsx`, `SocialCompleteProfilePage.test.tsx`, `frontend/src/pages/subscriber/PlaylistListPage.test.tsx`, `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx`, and `shellCatalogRouterGaps.coverage.test.tsx`.
- Mechanical future-flag fixtures only: `frontend/src/components/catalogComponents.test.tsx`, `frontend/src/layouts/Header.test.tsx`, `frontend/src/pages/admin/UserManagePage.test.tsx`, auth `EmailVerifyPage`, `PasswordResetPage`, and `SignupPage` tests, creator `AlbumEditPage`, `TrackEditPage`, and `TrackUploadPage` tests, public `HomePage`, `NoticeListPage`, `SubscriptionPlanPage`, and `TrackDetailPage` tests, subscriber `CompanyCertApplyPage`, `CompanyCertStatusPage`, `DownloadHistoryPage`, `PlaylistEditPage`, `ProfilePage`, `SubscriptionManagePage`, `SubscriptionPaymentPage`, and `SubscriptionPaymentReplay` tests, plus `adminSubscriberGaps` and `adminSubscriberPages` coverage tests.
- A scoped zero-context diff inspection of the mechanical fixture list contained only `MemoryRouter`/`RouterProvider` future props and Prettier line wrapping; no assertions, mocks, routes, or product behavior changed.

## Focused TDD Evidence

### RED

```powershell
cd frontend
npm test -- src/utils/loginReturn.test.ts src/router/SubscriberRoute.test.tsx src/layouts/PlayerBar.test.tsx src/pages/auth/LoginPage.test.tsx src/router/LazyRoute.test.tsx src/router/index.test.tsx
```

- Expected failures: missing shared return/lazy modules and assertions exposing lost origins, unsafe or access-inappropriate returns, render-time toast mutation, and absent import-rejection recovery.

```powershell
npm test -- src/pages/subscriber/PlaylistListPage.test.tsx src/test/coverage/publicAuthShell.coverage.test.tsx
```

- Before the transition-race guard/fixture correction: `2` files failed, `2` tests failed, `30` passed.
- Playlist failure: the route state had become `null`, but the create dialog had reopened after the refresh raced the transition.
- Auth-shell failure: the fixture claimed authentication while supplying no user identity; fail-closed navigation correctly selected Home.

### GREEN

```powershell
npm test -- src/utils/loginReturn.test.ts src/router/SubscriberRoute.test.tsx src/layouts/PlayerBar.test.tsx src/pages/auth/LoginPage.test.tsx src/router/LazyRoute.test.tsx src/router/index.test.tsx
```

- `6` files passed, `85` tests passed.

```powershell
npm test -- src/utils/loginReturn.test.ts src/utils/oauthAttempt.test.ts src/router/ProtectedRoute.test.tsx src/router/SubscriberRoute.test.tsx src/router/LazyRoute.test.tsx src/router/index.test.tsx src/pages/auth/LoginPage.test.tsx src/pages/auth/SocialLoginPage.test.tsx src/pages/auth/SocialCompleteProfilePage.test.tsx src/layouts/PlayerBar.test.tsx src/layouts/Header.test.tsx src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx src/test/coverage/publicAuthShell.coverage.test.tsx
```

- `13` files passed, `175` tests passed.

```powershell
npm test -- src/router/LazyRoute.test.tsx src/pages/subscriber/PlaylistListPage.test.tsx
```

- Final focused rerun after Fast Refresh module separation: `2` files passed, `9` tests passed.

### Independent PG Remediation RED

```powershell
npm test -- src/utils/loginReturn.test.ts src/utils/oauthAttempt.test.ts src/pages/auth/SocialLoginPage.test.tsx src/pages/auth/SocialCompleteProfilePage.test.tsx
```

- Before remediation: `4` files failed, `9` tests failed, `60` passed.
- P2 reproduced with `/%61dmin/dashboard`, mixed-case `/AdMiN%2Fdashboard`, encoded checkout slash, and encoded BUSINESS boundary inputs.
- P3 reproduced account mismatch reuse, stale replacement retention, storage-failure retention, and cross-account profile navigation.

### Independent PG Remediation GREEN

```powershell
npm test -- src/utils/loginReturn.test.ts src/utils/oauthAttempt.test.ts src/pages/auth/SocialLoginPage.test.tsx src/pages/auth/SocialCompleteProfilePage.test.tsx
```

- PASS - `4` files, `69` tests.

```powershell
npm test -- src/utils/loginReturn.test.ts src/utils/oauthAttempt.test.ts src/router/ProtectedRoute.test.tsx src/router/SubscriberRoute.test.tsx src/router/LazyRoute.test.tsx src/router/index.test.tsx src/pages/auth/LoginPage.test.tsx src/pages/auth/SocialLoginPage.test.tsx src/pages/auth/SocialCompleteProfilePage.test.tsx src/layouts/PlayerBar.test.tsx src/layouts/Header.test.tsx src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx src/test/coverage/publicAuthShell.coverage.test.tsx
```

- PASS - `13` adjacent files, `190` tests.

## Verification Results

MA ran the final repository gates after PG remediation. All reported gates pass.

| Command | Result |
| --- | --- |
| `npm run test:coverage` | PASS - `80` files, `930` tests, `0` failures; statements 88.8% (`7929/8929`), branches 80.33% (`5012/6239`), functions 88.43% (`1981/2240`), lines 90.98% (`7291/8013`) |
| `npm run typecheck` | PASS - TypeScript no-emit check |
| `npm run lint` | PASS - full `frontend/src`, zero warnings |
| `npm run format` | PASS - all matched frontend files |
| `npm run build` | PASS - Vite 6.4.3 production build; `280` modules transformed |
| `.\gradlew.bat test jacocoTestReport jacocoTestCoverageVerification assemble --rerun-tasks --no-daemon --max-workers=1 --console=plain` | PASS - `1568` tests, `0` failures/errors, `19` skipped, `184` suites; instruction 86.957%, branch 72.251%, line 87.228%, method 84.730%, class 94.824%; assemble PASS |
| `python .agents/skills/validate-docs/scripts/validate_docs.py` | PASS - `579` supported traceability IDs; no broken links or orphans |
| `git diff --check -- deliverables/agent/WI-20260809-ATS-043-evidence-pack.md deliverables/user/WI-20260809-ATS-043-summary.md` | PASS - rerun after this final evidence update |

No OAuth/provider/payment/mail/download/export action was executed. No database or ignored secret configuration was accessed or mutated. The protected output archive and audit directory were excluded from workspace status/diff checks and were not touched, inspected, staged, or deleted.

## Independent PG Findings and SE Disposition

- P2 fixed: raw `parsed.pathname` can no longer bypass ADMIN, checkout, or BUSINESS classification through percent encoding or case. Focused adversarial tests verify denial and exact-target preservation for authorized identities.
- P3 fixed: continuation records include only attempt metadata, return target, timestamp, and user ID; no token or secret is stored. Old records are cleared before every store attempt, storage failure leaves no continuation, and consume deletes before current-user matching.
- No new error disclosure or retry-loop behavior was introduced.
- Independent PG re-review: PASS. The prior P2/P3 findings are closed. Static re-review found no new open redirect, percent-encoding or case bypass, cross-account continuation replay, token/secret storage, or fail-open behavior.
- Residual assumptions: authenticated user IDs are stable and not reused; `attemptId` is correlation metadata, not an access token, refresh token, authorization code, or PKCE verifier/secret.
- Review boundary: the independent re-review was static. Focused and adjacent tests plus MA final frontend, backend, documentation, and diff gates are recorded above and pass.

## Risks / Rollback

- Risk: access-aware target classification is an explicit list matching the current route policy. New restricted route families must extend the shared helper and adversarial tests.
- Risk: one retry is bounded per mounted lazy recovery. A deliberate route departure and later revisit creates a new mount and therefore a new bounded attempt.
- Risk: `handledCreateRequestKeyRef` assumes React Router location keys identify distinct navigation requests; this is the router's supported location identity contract and is covered by the transition-race test.
- Rollback: revert the shared helper/consumers, route effect handling, PlayerBar navigation, future configuration, lazy recovery files and route imports, transition-race guard, related tests, current docs, and WI deliverables as one patch. No data rollback is required.

## Follow-ups

- This WI blocks `WI-20260809-ATS-057`, `WI-20260809-ATS-060`, and `WI-20260809-ATS-072`; chain handling belongs to the MA after evidence acceptance.
