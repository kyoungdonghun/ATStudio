---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: qa-fe
category: evidence-pack
status: stable
related_wi: WI-20260716-ATS-014
dependencies:
  - path: ../user/REQ-20260716-ATS-002.md
    reason: Approved development-branch remediation scope
  - path: WI-20260716-ATS-014-handoff.md
    reason: Frontend QA execution contract
  - path: WI-20260716-ATS-010-evidence-pack.md
    reason: Frontend state and accessibility implementation evidence
  - path: WI-20260716-ATS-011-evidence-pack.md
    reason: Dependency, formatting, and coverage configuration evidence
  - path: WI-20260716-ATS-012-evidence-pack.md
    reason: Current documentation and UI contract evidence
---

# Evidence Pack: WI-20260716-ATS-014

## Summary

Independently reproduced every required frontend audit, type, lint, test, coverage, build, and format check on `codex/p1-acceptance-hardening`; reviewed routes, asynchronous state, accessibility, and playback/download policy; and recorded residual findings for WI-017. Verification changed no application or environment state except regenerable ignored reports and the two required WI-014 deliverables.

## Scope / DoD Check

- [x] Production-only and unfiltered dependency audits reproduced.
- [x] Declared/resolved dependency versions and lack of override/force workaround confirmed.
- [x] Typecheck, ESLint, full Vitest, V8 coverage, production build, and full-tree Prettier completed.
- [x] Exact test totals, coverage values, build size, and report paths recorded.
- [x] Route guards, safe return paths, stale-request defenses, and major accessibility contracts reviewed.
- [x] Public full-track playback and server-authoritative download policy reviewed.
- [x] Findings severity-ranked with code/test evidence and WI-017 disposition.
- [x] `tsconfig.tsbuildinfo` restored to its exact pre-command SHA-256.
- [ ] Real-browser, live provider, retained DB, production proxy, and client-demo proof: outside WI scope.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and product invariants |
| 0 | `docs/standards/development-standards.md` | Frontend quality and test guidance |
| 1 | `docs/policies/quality-gates.md` | Quality evidence contract |
| 1 | `docs/policies/security-policy.md` | Route, stream, download, and secret boundaries |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | React request/render/performance review guidance |
| 2 | `docs/ui/` | Current route, screen, modal, and flow contracts |
| 2 | `docs/design/api-spec.md` | API, download, and ADMIN bypass contract |
| 2 | `docs/client/` | Client-visible behavior and acceptance checks |

REQ/context and implementation pointers loaded from the handoff: `deliverables/user/REQ-20260716-ATS-002.md`, `docs/design/remaining-remediation-design-20260716.md`, WI-010/WI-011/WI-012 Evidence Packs, `frontend/package*.json`, Vite/TypeScript/ESLint/Prettier configuration, `frontend/src/`, generated `coverage/`, and generated `dist/`.

## Environment and Branch Proof

| Item | Result |
|---|---|
| Worktree | `C:/Users/jm991/Desktop/project/ATStudio` |
| Branch | `codex/p1-acceptance-hardening` |
| Frozen client-demo | Not inspected, modified, or restarted |
| Package override/resolution | `overrides=null`, `resolutions=null` |
| Package mutation in WI-014 | None; no install, update, audit fix, or lockfile write |

## Commands and Exact Results

| Command | Exit | Result / evidence |
|---|---:|---|
| `Get-FileHash frontend/tsconfig.tsbuildinfo -Algorithm SHA256` | 0 | Baseline `B6A42AD2CD32A5AD04D06C55B8B1B26DD9B1894AFDAF7B508DFF16EF0C60F22A` |
| `npm audit --omit=dev --json` | 0 | 0 total; 0 info/low/moderate/high/critical; temporary raw JSON `%TEMP%/WI-20260716-ATS-014-audit-production.json` |
| `npm audit --json` | 0 | 0 total; 0 info/low/moderate/high/critical; temporary raw JSON `%TEMP%/WI-20260716-ATS-014-audit-all.json` |
| `npm ls ... --all --depth=20` | 0 | Dependency tree valid; versions recorded below |
| `npm run typecheck` | 0 | `tsc --noEmit` PASS |
| `npm run lint` | 0 | ESLint PASS with `--max-warnings 0` |
| `npm test -- --run` | 0 | Vitest 4.1.4; 38 files / 180 tests; duration 17.22 s |
| `npm run test:coverage` | 0 | 38 files / 180 tests; V8 summary below; duration 21.76 s |
| `npm run build` | 0 | Vite 6.4.3; 264 modules; built in 3.26 s |
| `npx prettier --check . --ignore-unknown` | 0 | All matched files use Prettier style |
| route declaration count script | 0 | 62 paths, 1 index redirect, 54 lazy pages, 8 payment-page route references |
| player preview/stream/download grep | 0 | Preview matches 0; stream source at `playerStore.ts:241`; download API at `downloads.ts:15` |
| `git diff --check` | PASS (exit 0) | No whitespace errors; existing working-tree LF-to-CRLF warnings were emitted |

## Dependency Evidence

| Dependency path | Declared | Resolved |
|---|---:|---:|
| `axios` | `^1.18.1` | 1.18.1 |
| `axios -> follow-redirects` | transitive | 1.16.0 |
| `axios -> form-data` | transitive | 4.0.6 |
| `react-router-dom` | `^6.30.4` | 6.30.4 |
| `react-router` | transitive | 6.30.4 |
| `vite` | `^6.4.3` | 6.4.3 |
| `vitest` | `^4.1.4` | 4.1.4 |
| `@vitest/coverage-v8` | `^4.1.4` | 4.1.4 |
| `@vitejs/plugin-react -> @babel/core` | transitive | 7.29.7 |
| minimatch paths -> `brace-expansion` | transitive | 1.1.16 / 5.0.7 |
| ESLint flat-cache -> `flatted` | transitive | 3.4.2 |
| ESLint -> `js-yaml` | transitive | 4.3.0 |
| Vite/Vitest paths -> `picomatch` | transitive | 4.0.5 |
| Vite -> `postcss` | transitive | 8.5.19 |
| jsdom -> `undici` | transitive | 7.28.0 |

No `--force`, package override, resolution, major-upgrade workaround, or package mutation was performed by WI-014.

## V8 Coverage Evidence

Report pointers: `frontend/coverage/coverage-summary.json`, `frontend/coverage/index.html`.

| Metric | Covered / Total | Percentage |
|---|---:|---:|
| Statements | 2,347 / 6,803 | 34.49% |
| Branches | 1,532 / 4,505 | 34.00% |
| Functions | 515 / 1,851 | 27.82% |
| Lines | 2,168 / 6,119 | 35.43% |

Coverage is observational. No threshold is declared or inferred. Risk-relevant examples:

- `PaymentReadOnlyPage.tsx`: 34.64% lines, 24.17% branches, 20.89% functions; four tests do not exercise financial mutation workflows.
- `PlayerBar.tsx`: 43.57% lines; tests exercise feedback, keyboard seek, and mobile focus, but not subscription-state classification.
- `TrackDetailPage.tsx`: 56.94% lines; three tests exercise Track/player mapping, not stale detail load or download failure state.
- `UserManagePage.tsx` and `UserSubscriptionManagePage.tsx`: 0% and no co-located tests.
- Many creator/admin pages and API wrappers remain at 0%; this is visible because coverage includes production source files rather than only imported modules.

## Build Evidence

Report pointer: `frontend/dist/index.html` and `frontend/dist/assets/` (ignored, regenerable).

- Vite 6.4.3 transformed 264 modules and completed in 3.26 s.
- No Vite oversized-chunk warning occurred.
- Largest initial JS chunk: `assets/index-C4BpPLWe.js`, 341.04 kB, 110.97 kB gzip.
- Largest reviewed feature chunk: `PaymentReadOnlyPage-DaFSoUMd.js`, 39.29 kB, 9.20 kB gzip.

## Route / State / Accessibility / Policy Review Matrix

| Area | Evidence | Result |
|---|---|---|
| USER payment routes | `router/index.tsx:123-129,175-200`; `router/index.test.tsx:37-68` | PASS: eight routes use USER min/max guard and ADMIN redirect `/admin/payments` |
| BUSINESS certification | `router/index.tsx:131-137,203-204`; `router/index.test.tsx:48-84` | PASS: USER plus BUSINESS-only |
| Protected password return | `ProtectedRoute.tsx:53-56`; `LoginPage.tsx:61-121,137,215`; `LoginPage.test.tsx:227-268` | PASS for validated internal pathname/query; privileged/external/protocol-relative targets rejected |
| Subscriber domain classification | `SubscriberRoute.tsx:25-76`; `SubscriberRoute.test.tsx:64-198` | PASS: strict structured no-active outcome, cancellation, generation fence, retry; unauthenticated return path gap recorded below |
| Track list stale response | `TrackListPage.tsx:178-258`; `TrackListPage.test.tsx:195-349` | PASS: latest success/failure/filter tags win and retry is fenced |
| Admin payment read state | `PaymentReadOnlyPage.tsx:188-338`; `PaymentReadOnlyPage.test.tsx:195-291` | PASS for tab/filter stale-read and active failure; mutation coverage gap recorded below |
| Modal focus | `Modal.tsx:26-105`; `Modal.test.tsx:44-108` | PASS in jsdom: topmost Escape/Tab trap, nested close order, unmount and opener focus restoration |
| Toast / pagination / header | `ToastContainer.tsx:18-40`; `Pagination.tsx:27-73`; focused tests | PASS for alert/status live regions, native dismiss, navigation naming/current page, desktop/mobile search labels |
| Player state and keyboard | `playerStore.ts:147-219,241-260`; `PlayerBar.tsx:546-675,783-800,927-949`; focused tests | PASS for play success/failure, stale play resolution, metadata/time progression, stalled recovery, semantic seek sliders |
| Full-track policy | `playerStore.ts:241`; preview-term scan = 0; `security-policy.md:180-185` | PASS: controller stream path, no frontend cutoff/timer/preview gate |
| Download policy | `downloads.ts:13-18`; `PlayerBar.tsx:121-146,760-779,993-1010`; `api-spec.md:34-40` | PASS: dedicated download API and server authority retained; existing documented ADMIN bypass unchanged |
| Route count | `router/index.tsx:35-139`; source-derived count command | PASS declarations/docs, LOW stale inline comment finding remains |

The focused accessibility review is code/jsdom evidence only. No claim is made about screen-reader output, real responsive layout, media events, nested overlays in a browser, or axe/jsx-a11y coverage.

## Severity-Ranked Findings

### F-014-01 - P2 Medium - Player subscription failure is misclassified as inactive

- Evidence: `frontend/src/layouts/PlayerBar.tsx:86-94` maps every rejection from `fetchMySubscription()` to `hasSubscription=false`; `:277-350`, `:760-779`, and `:993-1010` render a non-subscriber experience from that boolean.
- Impact: a timeout, 5xx, offline condition, or stale response can hide valid subscriber controls and tell a subscribed user to subscribe again. An older request can also overwrite newer auth/subscription state.
- Test gap: `frontend/src/layouts/PlayerBar.test.tsx:112-183` has no active/inactive/error/stale subscription cases.
- WI-017 disposition: use explicit loading/active/inactive/error state; classify only `403 + NO_ACTIVE_SUBSCRIPTION` as inactive; add AbortController plus generation fence and retry/error UX; add race/error tests.

### F-014-02 - P2 Medium - Latest-request-wins coverage is incomplete

- Evidence:
  - `frontend/src/pages/public/TrackDetailPage.tsx:36-45` commits any detail promise result for the current component without abort/generation control.
  - `frontend/src/pages/admin/UserManagePage.tsx:29-38` can commit an older page/search response after a newer one.
  - `frontend/src/pages/admin/UserSubscriptionManagePage.tsx:50-59` can commit an older page response after a newer one.
  - `frontend/src/pages/subscriber/DownloadQueuePage.tsx:69-90` can commit an older page/keyword/sort result after a newer one.
- Impact: route/filter/page UI can display stale records or stale errors; admin operators may act on a view that no longer matches the selected filter/page.
- Documentation conflict: `docs/ui/screen-flow.md` says list screens use latest-request-wins, which is broader than current code.
- WI-017 disposition: prioritize admin user/subscription and track detail; add abort/generation fences, cancellation classification, retry state, and deferred out-of-order tests; either complete all claimed list surfaces or narrow the documentation.

### F-014-03 - P2 Medium - Financial mutation UI has insufficient regression proof

- Evidence: `frontend/src/pages/admin/PaymentReadOnlyPage.tsx:403-432,481-641,2014-2016` implements settlement import/reconciliation and refund/entitlement preview-request-approve-execute with typed execution confirmation.
- Test evidence: `frontend/src/pages/admin/PaymentReadOnlyPage.test.tsx:195-291` contains four tests: stale tab, stale filter failure, active read failure, and incident reload. No mutation workflow is exercised.
- Impact: parameter/ID/note/status wiring, duplicate-submit protection, typed confirmation, and post-mutation refresh could regress while the current suite remains green.
- WI-017 disposition: add focused tests for cancellation, rejected/accepted confirmations, each mutation call payload, disabled/busy state, failure feedback, and single refresh after success.

### F-014-04 - P3 Low - Social login loses validated return target

- Evidence: `frontend/src/pages/auth/LoginPage.tsx:137,228-262` validates and uses `returnTo` only for password navigation; `frontend/src/pages/auth/SocialLoginPage.tsx:72-77` navigates completed social login to `/`.
- Impact: users entering a protected deep link and choosing social login do not return to the intended safe page.
- WI-017 disposition: persist the already validated target in session state for the OAuth attempt, consume it once after profile completion, and add safe/unsafe/stale-state tests.

### F-014-05 - P3 Low - Router source comment is stale

- Evidence: `frontend/src/router/index.tsx:139` says `49 screens + 2 error pages`; source count is 62 paths, 1 index redirect, 54 lazy pages, 53 distinct visual UIs after the modal adapter rule.
- Impact: no runtime failure; future maintainers can copy an obsolete count.
- WI-017 disposition: replace the fixed numeric comment with the documented count contract or a pointer to `docs/ui/atstudio-front-list.md`.

## tsbuildinfo Integrity Proof

| Point | SHA-256 |
|---|---|
| Before any command | `B6A42AD2CD32A5AD04D06C55B8B1B26DD9B1894AFDAF7B508DFF16EF0C60F22A` |
| After typecheck/test/coverage/build | `0C55D2074C97ED14B747BCCFCB1ED9EAAD20E490C8214D6BF6F51BDB6EEF0714` |
| Backup | `B6A42AD2CD32A5AD04D06C55B8B1B26DD9B1894AFDAF7B508DFF16EF0C60F22A` |
| After exact restore | `B6A42AD2CD32A5AD04D06C55B8B1B26DD9B1894AFDAF7B508DFF16EF0C60F22A` |

The build changed the generated file as expected. It was restored from `%TEMP%/WI-20260716-ATS-014-tsconfig.tsbuildinfo`, and the final hash exactly equals the pre-command baseline.

## Risks / Limits

- No P0/P1 frontend defect was found in this verification pass.
- Low overall coverage remains a material risk signal; the percentage is not converted into an invented gate.
- Passing jsdom tests and Vite build do not prove real-browser focus, responsive layout, media/network timing, provider callbacks, CORS, public runtime, or production deployment.
- Audits prove the current installed/locked development dependency graph at execution time, not the frozen client-demo graph.
- Temporary audit JSON and the tsbuildinfo backup are outside the repository and are not deliverables.

## Rollback / No-Change Statement

WI-014 is verification-only except:

- `deliverables/user/WI-20260716-ATS-014-summary.md`
- `deliverables/agent/WI-20260716-ATS-014-evidence-pack.md`

Rollback removes only those two files. Generated ignored `frontend/coverage/` and `frontend/dist/` may be deleted/regenerated independently. Do not revert existing WI-004 through WI-012 changes, `frontend/tsconfig.tsbuildinfo`, client-demo state, DB/data, runtime logs, or unrelated worktree changes.

## Follow-up Chain

- WI-015 may proceed with cross-layer/API/document 3-way review.
- WI-016 may proceed with security/concurrency/code review.
- WI-017 must disposition F-014-01 through F-014-05 and rerun the complete frontend gate before release readiness is reconsidered.

## Related Documents

- [User Summary](../user/WI-20260716-ATS-014-summary.md)
- [Handoff](WI-20260716-ATS-014-handoff.md)
- [Approved REQ](../user/REQ-20260716-ATS-002.md)
- [Current Remediation Design](../../docs/design/remaining-remediation-design-20260716.md)
