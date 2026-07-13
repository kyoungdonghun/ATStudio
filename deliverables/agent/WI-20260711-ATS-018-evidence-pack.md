# Evidence Pack: WI-20260711-ATS-018

## Summary (one-liner)

- Completed a read-only qa-fe adjudication of frontend-linked P0/P1 findings and core SPA risks, producing decision-ready remediation ownership without source fixes or mutating actions.

## Scope / DoD Check

- [x] Read `deliverables/agent/WI-20260711-ATS-018-handoff.md` completely before analysis.
- [x] Confirmed `deliverables/user/REQ-20260711-ATS-001.md` is approved.
- [x] Loaded required Tier 0-2 documents and every listed predecessor Evidence Pack.
- [x] Read `docs/standards/core-principles.md`, `docs/standards/development-standards.md`, `docs/policies/quality-gates.md`, `docs/standards/frontend-standards.md`, every file under `docs/ui/`, `.agents/skills/react-best-practices/AGENTS.md`, and `.agents/skills/create-wi-evidence-pack/SKILL.md`.
- [x] Traced React route guards, page/API paths, role behavior, async/loading/error paths, request-race risks, accessibility gaps, responsive/core journey implications, and test coverage gaps.
- [x] Reassessed every P0/P1 frontend-linked predecessor finding and separated frontend ownership from backend defects surfaced in UI.
- [x] Accounted for passing tests/typecheck/lint/build and the failing Prettier gate plus missing focused cases.
- [x] Preserved uncertainty where runtime/deployment evidence is absent.
- [x] Wrote only the two WI-018 output files and performed no source fix, browser mutation, form submit, upload, payment, cancellation, refund, export, staging, commit, delete, cleanup, or delegation.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | System constitution, traceability, active React baseline, platform integrity |
| 0 | `docs/standards/development-standards.md` | Evidence-first QA, frontend-active project structure, test/build standards |
| 0 | `docs/standards/documentation-standards.md` | Documentation language and deliverable structure |
| 0 | `docs/standards/glossary.md` | Canonical role, subscription, track, playlist, and WI terms |
| 1 | `docs/policies/quality-gates.md` | Regression and evidence-gate expectations |
| 2 | `docs/standards/frontend-standards.md` | React/Zustand/Axios/routing conventions and no native confirm rule |
| 2 | `docs/ui/atstudio-front-list.md` | Screen/API/auth inventory |
| 2 | `docs/ui/index.md` | UI document entry point |
| 2 | `docs/ui/modal-list.md` | Modal/confirm interaction inventory |
| 2 | `docs/ui/screen-flow.md` | Role and screen-flow expectations |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | Client request, loading, rendering, and performance guidance |
| 2 | `.agents/skills/create-wi-evidence-pack/SKILL.md` | Evidence Pack format |
| Context | `deliverables/user/REQ-20260711-ATS-001.md` | Approved audit scope |
| Context | `deliverables/agent/WI-20260711-ATS-018-handoff.md` | WI scope, DoD, constraints, output contract |
| Predecessor | `deliverables/agent/WI-20260711-ATS-003-evidence-pack.md` | Frontend route/role/API/state findings |
| Predecessor | `deliverables/agent/WI-20260711-ATS-006-evidence-pack.md` | Payment 3-way findings |
| Predecessor | `deliverables/agent/WI-20260711-ATS-007-evidence-pack.md` | Whitelist/company certification findings |
| Predecessor | `deliverables/agent/WI-20260711-ATS-008-evidence-pack.md` | Cross-domain P0/P1 adjudication |
| Predecessor | `deliverables/agent/WI-20260711-ATS-010-evidence-pack.md` | Frontend Vitest baseline |
| Predecessor | `deliverables/agent/WI-20260711-ATS-011-evidence-pack.md` | Java/frontend typecheck baseline |
| Predecessor | `deliverables/agent/WI-20260711-ATS-012-evidence-pack.md` | ESLint/Prettier baseline |
| Predecessor | `deliverables/agent/WI-20260711-ATS-013-evidence-pack.md` | Build baseline |
| Predecessor | `deliverables/agent/WI-20260711-ATS-015-evidence-pack.md` | Coverage/test-gap baseline |

**Injection Rules Applied**:

- Assignee: `qa-fe`
- Task type: frontend review / implementation-task deliverable only
- Handoff dependencies: WI-003, WI-006, WI-007, WI-008, WI-010, WI-011, WI-012, WI-013, WI-015
- Execution boundary: read-only except this WI's two deliverables; no delegation.

## Evidence Pointers

### Files changed

- `deliverables/user/WI-20260711-ATS-018-summary.md` - user-facing verdict, adjudicated blockers, remediation, risks.
- `deliverables/agent/WI-20260711-ATS-018-evidence-pack.md` - reproducible evidence, commands, ownership, tests, rollback, follow-up routing.

### Primary frontend source traced

- Route and guards:
  - `frontend/src/router/index.tsx:105-115` guard helpers.
  - `frontend/src/router/index.tsx:153-164` subscription checkout, manage, whitelist, and company-certification routes.
  - `frontend/src/router/index.tsx:183-209` admin route subtree.
  - `frontend/src/router/ProtectedRoute.tsx:7-24` role hierarchy where ADMIN satisfies USER-level gates.
  - `frontend/src/router/ProtectedRoute.tsx:38-42` unauthenticated and insufficient-role redirects.
  - `frontend/src/router/SubscriberRoute.tsx:25-57` subscription check and catch-all subscription redirect.
- Auth:
  - `frontend/src/pages/auth/SocialLoginPage.tsx:44-45` calls `socialLogin` then `fetchMe()` without returned token.
  - `frontend/src/api/auth.ts:99-103` only sends Authorization when `accessToken` is supplied.
  - `frontend/src/pages/auth/LoginPage.tsx:125-144` password login uses `fetchMe(tokens.accessToken)` before committing auth.
  - `frontend/src/store/authStore.ts:36-59` token/user persistence and client-only logout.
- Subscription/payment:
  - `frontend/src/pages/public/SubscriptionPlanPage.tsx:162-178` catalog subscribe CTA checks auth/user type but not ADMIN role.
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx:127-163` direct checkout prepares billing agreement.
  - `frontend/src/api/payments.ts:70-87` prepare/confirm billing agreement calls.
  - `frontend/src/pages/admin/PaymentReadOnlyPage.tsx:179-234` admin payment tab/page request flow with shared loading/pageInfo updates.
  - `frontend/src/pages/admin/PaymentReadOnlyPage.tsx:326-568` mutating admin operations guarded by native confirm prompts; not executed.
- Whitelist/company certification:
  - `frontend/src/pages/subscriber/WhitelistChannelPage.tsx:74-98` whitelist load and subscription failure collapsed to `null`.
  - `frontend/src/pages/subscriber/WhitelistChannelPage.tsx:204-219` removal/delete copy depends only on EXPORTED/REGISTERED.
  - `frontend/src/pages/admin/WhitelistChannelManagePage.tsx:66-83` admin whitelist load.
  - `frontend/src/pages/admin/WhitelistChannelManagePage.tsx:99-133` status/export native confirm flows; not executed.
  - `frontend/src/pages/subscriber/CompanyCertApplyPage.tsx:54-87` no frontend BUSINESS gate before form eligibility.
  - `frontend/src/pages/subscriber/CompanyCertApplyPage.tsx:90-142` client file checks and apply call.
  - `frontend/src/api/companyCerts.ts:7-31` multipart apply/resubmit calls.
  - `frontend/src/pages/admin/CompanyCertManagePage.tsx:133-150` review/download paths; download not executed.
  - `frontend/src/api/admin.ts:104-121` admin document download helper.
  - `frontend/src/types/index.ts:253-267` company certification detail includes `documentPath`.
  - `frontend/src/utils/validation.ts:40-44` frontend certification file extension/count/size constants.
- Request/race/a11y:
  - `frontend/src/pages/public/TrackListPage.tsx:151-185` no latest-request guard for track loads.
  - `frontend/src/pages/public/TrackListPage.tsx:188-208` available-tags fetch has no cancellation/ignore guard.
  - `frontend/src/components/playlist/AddToPlaylistModal.tsx:28-61` fetch/timer lifecycle issue.
  - `frontend/src/components/ui/ToastContainer.tsx:18-27`, `frontend/src/layouts/Header.tsx:123-132,233-242`, `frontend/src/layouts/PlayerBar.tsx:498-516` are predecessor a11y pointers from WI-003.

### Predecessor evidence anchors

- `WI-003-evidence-pack.md:5-16` frontend audit scope and quality checks.
- `WI-003-evidence-pack.md:77-100` social auth, checkout, company certification, admin route map, guard observations.
- `WI-003-evidence-pack.md:247-263` accessibility gaps.
- `WI-003-evidence-pack.md:384-404` focused frontend test gaps.
- `WI-006-evidence-pack.md:64-81` payment matrix, including frontend payment paths.
- `WI-006-evidence-pack.md:89-102` P0/P1/P2 payment findings.
- `WI-006-evidence-pack.md:153-159` payment frontend tests to add.
- `WI-007-evidence-pack.md:73-80` frontend-linked whitelist/certification re-verification.
- `WI-007-evidence-pack.md:166-203` certification document, route, validation, and migration findings.
- `WI-007-evidence-pack.md:250-291` target frontend/test gaps.
- `WI-008-evidence-pack.md:89-131` P0 original-audio, token logging, active-content upload, session/social findings.
- `WI-010-evidence-pack.md` frontend tests: 14/14 files, 51/51 tests passed.
- `WI-011-evidence-pack.md` Java compile and frontend typecheck passed.
- `WI-012-evidence-pack.md` ESLint passed, Prettier failed with 143 drift files.
- `WI-013-evidence-pack.md` backend and frontend production builds passed.
- `WI-015-evidence-pack.md` Java/frontend coverage not measurable; high-risk focused test gaps.

## Adjudicated Findings

| ID | Status | Priority | Ownership | Reproduction/static trace | Remediation |
|---|---|---:|---|---|---|
| FE-001 social callback token ordering | Confirmed | P1 | Frontend-owned | Fresh callback path: `socialLogin(provider, code, codeVerifier)` returns token, then `fetchMe()` omits it. `fetchMe` only sends Authorization when a token arg is passed. Password login shows the correct order. | Change social callback to `fetchMe(res.accessToken)` and commit matching token/user atomically. Add empty-storage callback test. |
| PAY-006-08 ADMIN member checkout | Confirmed | P1 | Cross-layer contract | USER-level checkout route admits ADMIN because `ROLE_LEVEL.ADMIN > USER`; catalog CTA checks user type but not role; payment page prepares directly. Predecessor confirms backend prepare lacks exclusive member guard. | Add backend ADMIN rejection with no order/agreement persistence. Add frontend ADMIN redirect/message and direct URL/catalog CTA tests. |
| ATS008-01 original-audio static bypass | Confirmed | P0 | Backend defect surfaced in UI | Predecessor proves public detail/DTO/static master path bypasses download service. Frontend public discovery is the visible route, but the missing control is backend/static delivery. | Remove master path from public DTOs; move masters outside public static root; serve only previews publicly. Frontend should consume preview-safe fields only. |
| ATS008-02 SMTP token/PII logging | Confirmed | P0 | Backend defect surfaced in UI | Predecessor proves email reset/verify token URLs can be logged on SMTP failure. No frontend route can mitigate log disclosure. | Redact token/body/recipient from logs and persist explicit mail delivery state. |
| PAY-006-01 withdrawal leaves billing renewal | Confirmed | P0 | Backend defect surfaced in UI | User withdrawal is initiated from user-facing account flow, but predecessor proves renewal lookup still can charge active agreements after withdrawal. | Stop renewal during withdrawal orchestration and define billing-key retention/deletion policy. |
| ATS008-03 active playlist thumbnail upload | Conditional | P1 | Backend defect surfaced in UI | Subscriber UI can upload thumbnails; predecessor proves weak server validation/public storage. P0 browser-token escalation depends on production origin/headers not available here. | Server-side image decode/re-encode, reject active content, fixed response type, separate cookieless upload origin. |
| ATS007-F06 malicious certification document path | Confirmed | P1 | Backend defect surfaced in UI | Client/admin paths allow upload and admin download; predecessor proves server validation is extension/size/count only and persisted MIME is client-supplied. | Server MIME/signature/parser checks, quarantine/malware policy, safe download response, admin warning/testing. |
| ATS007-F10 certification migration/backfill | Conditional | P1 | Documentation/test coverage | UI handles legacy no-document rows, but predecessor cannot verify existing DB migration/backfill from repo state. | Copied-DB migration/backfill verification and explicit legacy disposition. |
| FE-005 stale request commits | Confirmed | P2 | Frontend-owned | Track loads and admin payment tab loads set shared state after awaited requests without abort/latest-request guards. | Add AbortController/Axios signal or monotonic request IDs; deferred-promise tests. |
| ATS007-F07 company BUSINESS gate UX | Confirmed | P2 | Frontend-owned | Routes are only `authRequired(USER+)`; apply page redirects only on existing cert and otherwise renders form. Backend rejects non-BUSINESS. | Add user-type gate before form/file selection. Test BUSINESS/INDIVIDUAL/ADMIN direct URLs. |
| FE-003 subscription guard error collapse | Confirmed | P2 | Frontend-owned | `SubscriberRoute` catches any `fetchMySubscription` rejection and redirects to `/subscriptions`. | Distinguish inactive/no subscription from 401, timeout, and 5xx; provide retry/error states. |
| FE-008 shared accessibility gaps | Confirmed | P2 | Frontend-owned | Predecessor points to toast, header search, player, pagination/modal test gaps. | Add live regions/labels/keyboard semantics/focus return and axe/keyboard tests. |
| FE-009 add-to-playlist lifecycle | Confirmed | P2 | Frontend-owned | Modal load effect has no cancellation and success close timer is not cleared. | Ignore/abort stale loads and clear timers on close/unmount; fake-timer tests. |
| FORMAT-012 Prettier drift | Confirmed | P2 | Documentation/test coverage | `npm run format` failed with 143 files in WI-012. | Separate formatting-baseline WI; do not mix with functional fixes. |
| COV-015 coverage unavailable | Confirmed | P2 | Documentation/test coverage | No JaCoCo/Vitest coverage provider/report exists per WI-015. | Add coverage instrumentation in separate approved WI and define thresholds after baseline. |

## Commands & Outputs

All commands were read-only/static except writing this WI's two deliverables.

- `Get-Content -Raw deliverables/agent/WI-20260711-ATS-018-handoff.md`
  - Result: handoff read completely; scope, DoD, inputs, forbidden actions, and output paths confirmed.
- `Select-String deliverables/user/REQ-20260711-ATS-001.md -Pattern 'Approved|Status|승인|APPROVED'`
  - Result: `Status: approved`.
- `Get-Content -Raw` over required Tier 0-2 documents, all `docs/ui/*`, `.agents/skills/react-best-practices/AGENTS.md`, and `.agents/skills/create-wi-evidence-pack/SKILL.md`
  - Result: loaded required reference context and evidence-pack format.
- `Get-Content -Raw` over listed predecessor Evidence Packs WI-003, WI-006, WI-007, WI-008, WI-010, WI-011, WI-012, WI-013, WI-015
  - Result: P0/P1/P2 findings and quality baselines reconciled.
- Targeted numbered `Get-Content`/`Select-String`/`rg` over `frontend/src/router`, auth pages/store/API, subscription/payment pages/API, company certification pages/API/types/validation, whitelist pages/API, track list, admin payment page, and selected tests.
  - Result: source pointers and adjudication table above.
- `rg --files frontend/src -g "*.test.ts" -g "*.test.tsx"`
  - Result: 14 frontend test files found; no `SocialLoginPage`, `CompanyCertApplyPage`, `CompanyCertStatusPage`, `WhitelistChannelPage`, or `PaymentReadOnlyPage` focused test file found.
- `Select-String` over subscription payment/plan and route tests
  - Result: subscription payment tests cover recurring billing, billing auth, upgrade-route block, success redirect, and legacy one-time path block; no ADMIN checkout direct-URL/catalog CTA rejection case found.

## Tests

### Consumed predecessor results

- `WI-010`: `npm test` passed, 14/14 files and 51/51 tests, exit code 0.
- `WI-011`: `gradlew.bat compileJava` and `npm run typecheck` passed, exit code 0.
- `WI-012`: `npm run lint` passed with 0 errors/0 warnings; `npm run format` failed with 143 Prettier drift files.
- `WI-013`: `gradlew.bat build` and frontend production build passed.
- `WI-015`: no Java/frontend coverage metric exists because coverage tooling is not configured.

### Not rerun in WI-018

- No Gradle/npm/browser/dev-server checks were rerun in this WI. The handoff permits only two output writes, and predecessor packs already provide current quality-command evidence.
- No browser smoke, API call, upload, download, export, payment, refund, cancellation, or admin mutation was executed.

### Missing focused cases

- Social callback with empty storage must call `/users/me` using returned access token.
- ADMIN direct checkout/catalog CTA must not prepare billing and backend must persist no payment state.
- Company-certification route matrix for BUSINESS, INDIVIDUAL, ADMIN before file selection.
- Whitelist/company page focused tests; predecessor found target frontend test files absent.
- Admin payment deferred-promise tests for tab/page/filter races.
- Track filter deferred-response tests.
- Accessibility tests for Toast, Pagination, Header search, Modal focus return, and PlayerBar controls.
- Upload safety tests for active playlist thumbnails and certification documents.
- Coverage instrumentation and threshold gate.

## Risks / Rollback

- Risks:
  - Production topology, storage origin, copied DB migration state, provider state, mail/log access, and malware scanner presence were not available.
  - Static analysis proves reachable frontend paths and absent client-side controls, but does not quantify production frequency.
  - File/line pointers may drift if other agents edit the dirty shared worktree after this pack.
  - Passing configured tests do not cover the missing focused cases listed above.
- Rollback:
  - If explicitly requested, remove only:
    - `deliverables/user/WI-20260711-ATS-018-summary.md`
    - `deliverables/agent/WI-20260711-ATS-018-evidence-pack.md`
  - No application rollback is required because no source/config/test file was modified.

## Follow-up Routing

- This WI blocks `WI-20260711-ATS-020` per the handoff.
- Route frontend-owned remediations to `qa-fe`/frontend implementation WIs:
  - FE-001 social login token ordering and test.
  - FE-005/PAY-006-13 latest-request-wins behavior.
  - ATS007-F07 company route UX gate.
  - FE-003 subscription guard error taxonomy.
  - FE-008/FE-009 a11y and modal lifecycle.
- Route backend/security remediations to backend/security/integration WIs:
  - ATS008-01 original-audio/static resource bypass.
  - ATS008-02 SMTP token/PII logging.
  - PAY-006-01 withdrawal/renewal billing.
  - ATS008-03 upload-origin/active-content controls.
  - ATS007-F06 certification document safety.
  - PAY-006-08 server-side ADMIN member-checkout rejection.
- Route documentation/test-coverage items to docops/QA WIs:
  - ATS007-F10 migration/backfill verification.
  - FORMAT-012 formatting baseline.
  - COV-015 coverage instrumentation.
