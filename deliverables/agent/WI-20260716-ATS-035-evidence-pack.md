# Evidence Pack: WI-20260716-ATS-035

## Summary

- Completed a read-only frontend residual-code audit and classified 22 evidence-backed inventory units for V1 consolidation.

## Scope / DoD Check

- [x] Searched routes, API clients, Zustand state, components, tests, feature/config branches, response fallbacks, debug/demo paths, generated artifacts, and backup-like files.
- [x] Recorded import/reference evidence, runtime reachability, backend-contract dependencies, user-visible impact, confidence, false-positive risk, and verification methods.
- [x] Checked public listening, authentication, subscription/payment, whitelist, and company-certification compatibility boundaries.
- [x] Preserved public/client runtime, Git index/history, branches/worktrees, DB state, product files, dependencies, generated files, and `frontend/tsconfig.tsbuildinfo`.
- [x] Created only the required WI summary and Evidence Pack.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and approval boundaries |
| 0 | `docs/standards/development-standards.md` | Source-review and verification baseline |
| 0 | `docs/standards/documentation-standards.md` | Deliverable structure and traceability |
| 0 | `docs/standards/glossary.md` | Current AT.M/ATStudio and domain terminology |
| 1 | `docs/policies/quality-gates.md` | Review evidence and regression expectations |
| 1 | `docs/policies/versioning-policy.md` | REMOVE/REPLACE/ARCHIVE distinction |
| 1 | `docs/standards/frontend-standards.md` | Active React patterns and UI rules |
| 2 | `docs/design/api-spec.md` | Frontend/backend contract comparison |
| 2 | `docs/ui/screen-flow.md` | Current user-flow source |
| 2 | `docs/ui/atstudio-front-list.md` | Current route/screen inventory |
| WI | `deliverables/user/REQ-20260716-ATS-004.md` | Approved V1 consolidation scope |
| WI | `deliverables/agent/WI-20260716-ATS-035-handoff.md` | Scope, constraints, and output contract |

## Baseline Snapshot

| Item | Value |
|---|---|
| Branch | `codex/p1-acceptance-hardening` |
| HEAD | `a96d2e0c5d249723bbf449b6834299a04cf2ad30` |
| `frontend/tsconfig.tsbuildinfo` SHA-256 | `B6A42AD2CD32A5AD04D06C55B8B1B26DD9B1894AFDAF7B508DFF16EF0C60F22A` |
| Pre-existing tracked frontend delta | `M frontend/tsconfig.tsbuildinfo` |
| Pre-existing frontend runtime logs | `frontend/vite.err.log`, `frontend/vite.out.log` |
| Product/runtime mutation | None |

Repository-resident inspection covered 252 frontend files after excluding `node_modules`, `dist`, and `coverage`; this included 236 files under `frontend/src`, 43 test files, all frontend root configuration/package files, and the sole `frontend/public/.gitkeep` entry.

## Audit Coverage

| Surface | Evidence | Result |
|---|---|---|
| Runtime import graph | Entry `frontend/src/main.tsx`; static and lazy imports resolved through `@/` and relative paths | 189 non-test TS/TSX/JS/JSX/CSS files, 184 reachable; unreachable product artifacts were `api/downloadQueue.ts`, `api/playHistory.ts`, `DataTable.tsx`, and `DataTable.module.css`; `src/test/setup.ts` is Vitest-only |
| Routes | `frontend/src/router/index.tsx:141-257`; 62 `path` declarations | Current routes reachable; legacy/adapter routes isolated below |
| Route producers | Literal route search in `frontend/src`, excluding tests | No runtime producer for five payment aliases or `/playlists/new`; `/download-queue` has active Header/Profile producers |
| API wrappers | Export inventory plus exact-symbol negative searches | Six unused exported functions/types confirmed; same-file internal types were not misclassified |
| Zustand | Imports/usages of auth, player, like, album-like, theme, and toast stores | All active stores reachable and used |
| Authentication | `LoginPage`, `SocialLoginPage`, `SocialCompleteProfilePage`, `ProtectedRoute`, `oauthAttempt.ts` | Current state/PKCE/return-path controls are active KEEP paths |
| Subscription/payment | `SubscriptionPlanPage`, `SubscriptionManagePage`, `SubscriptionPaymentPage`, payment APIs | Current checkout active; five compatibility aliases isolated |
| Whitelist/company certification | Subscriber/admin pages and API modules | State transitions and plan/certification gates are active; defensive fallbacks represent real failures |
| Response compatibility | `data.data`, `dataList`, nullish/shape searches | No proven old/new dual-response adapter found; remaining fallbacks are defensive defaults |
| Debug/mock/demo | Runtime `mock`, `dummy`, `console`, `debugger`, TODO/FIXME/HACK searches | No runtime console/debugger or dummy path; one unused provider type and stale `mock*` CSS naming found |
| Backup-like files | Tracked and filesystem filename search | No true `.bak`, `.backup`, `.old`, copy, or legacy backup file found; `oauthAttempt` was a substring false positive |
| Generated files | Git tracking and ignore checks | `dist/`, `coverage/`, `.env.local` ignored; `tsconfig.tsbuildinfo` remains tracked despite ignore; Vite logs remain unignored |

## Disposition Count

| Disposition | Inventory units |
|---|---:|
| KEEP | 8 |
| REMOVE | 7 |
| REPLACE | 6 |
| ARCHIVE | 0 |
| REVIEW | 1 |
| **Total** | **22** |

Counts are decision rows. A row may intentionally group one contract and all files/symbols that must change together.

## REMOVE Inventory

### F035-R01 - Legacy download-queue client surface

- **Disposition / confidence:** REMOVE / HIGH
- **Targets:**
  - `frontend/src/api/downloadQueue.ts:1-31`
  - `frontend/src/types/index.ts:192-202` (`DownloadQueueItem`)
- **Evidence:** Exact searches find no import or symbol reference outside the definitions. The active history page imports `fetchDownloadHistory` and `fetchDownloadHistoryTrackIds` from `frontend/src/api/downloads.ts` at `frontend/src/pages/subscriber/DownloadQueuePage.tsx:4-12`.
- **Backend dependency:** `src/main/java/com/atstudio/atstudio/controller/DownloadQueueController.java:16` and `docs/design/api-spec.md:2910-2954` remain compatibility surfaces for WI-034/038 coordination.
- **User impact:** None from removing the unused frontend wrapper/type. Backend removal is a separate coordinated decision.
- **Verification:** Negative symbol/import search, frontend typecheck, download-history page tests, subscriber download smoke.
- **False-positive risk:** LOW; both import graph and exact-symbol searches agree.

### F035-R02 - Unused server play-history client surface

- **Disposition / confidence:** REMOVE / HIGH
- **Targets:**
  - `frontend/src/api/playHistory.ts:1-24`
  - `frontend/src/types/index.ts:184-190` (`PlayHistory`)
- **Evidence:** The module is unreachable and has no caller. Current playback writes local history through `frontend/src/store/playerStore.ts:52-77,258-259`; current screen contract is browser-local at `docs/ui/atstudio-front-list.md:52-54`.
- **Backend dependency:** Server endpoints are explicitly compatibility-only at `docs/design/api-spec.md:892-944` and implemented by `PlayHistoryController.java:17`.
- **User impact:** None if only the unused frontend module/type is removed. Do not remove browser-local history.
- **Verification:** Play history page, PlayerBar history modal, player-store tests, negative `/play-histories` frontend search.
- **False-positive risk:** LOW; current SPA SoT is explicitly documented.

### F035-R03 - Dead shared DataTable component

- **Disposition / confidence:** REMOVE / HIGH
- **Targets:**
  - `frontend/src/components/ui/DataTable.tsx:1-96`
  - `frontend/src/components/ui/DataTable.module.css`
  - stale shared-component example at `docs/standards/frontend-standards.md:60`
- **Evidence:** No runtime or test import; import graph marks both files unreachable.
- **User impact:** None.
- **Verification:** Negative `DataTable` search, typecheck, ESLint, Prettier, full Vitest/build.
- **False-positive risk:** LOW.

### F035-R04 - Unused exported frontend API symbols

- **Disposition / confidence:** REMOVE / HIGH
- **Targets:**
  - `frontend/src/api/admin.ts:44-47` - `fetchUser`
  - `frontend/src/api/subscriptions.ts:29-37` - `fetchSubscriptionPlanDetail`
  - `frontend/src/api/userSubscriptions.ts:102-106` - `fetchAdminUserSubscriptionDetail`
  - `frontend/src/api/playlists.ts:88-97` - `addTracksToPlaylistBatch`
  - `frontend/src/api/payments.ts:97-102` - `cancelMyBillingAgreement`
  - `frontend/src/api/payments.ts:5` - unused `PaymentProvider`, including stale `MOCK`, `TOSS`, and `KAKAOPAY` union members
- **Evidence:** Exact-symbol search across all frontend source/tests returns definitions only. Current neighboring list/update/get functions have concrete page callers.
- **Backend dependency:** Corresponding endpoints still exist. Wrapper removal does not itself authorize endpoint removal; WI-034/038 owns backend disposition.
- **User impact:** None.
- **Verification:** Negative exact-symbol search plus full frontend gates.
- **False-positive risk:** LOW for current V1 runtime; MEDIUM for intentionally deferred future UI, but future code should add its client at use time.

### F035-R05 - Unproduced playlist-create route adapter

- **Disposition / confidence:** REMOVE / HIGH
- **Targets:**
  - `frontend/src/pages/subscriber/PlaylistCreatePage.tsx:1-10`
  - lazy import and `/playlists/new` route in `frontend/src/router/index.tsx:59,167`
  - adapter test at `frontend/src/router/index.test.tsx:87-105`
  - adapter descriptions in `docs/ui/screen-flow.md:42`, `docs/ui/modal-list.md:45`, and related use-case text
- **Evidence:** Runtime literal search finds no producer for `/playlists/new`. Normal creation is handled directly by the list-page modal.
- **User impact:** Old direct bookmarks stop resolving. REQ-004 explicitly removes old compatibility branches, so this is approval-sensitive but consistent with scope.
- **Verification:** Playlist list/modal tests and route inventory after removal.
- **False-positive risk:** MEDIUM because external bookmarks cannot be observed statically.

### F035-R06 - Placeholder keep-files

- **Disposition / confidence:** REMOVE / HIGH
- **Targets:** `frontend/src/features/.gitkeep`, `frontend/src/hooks/.gitkeep`, `frontend/public/.gitkeep`
- **Evidence:** `features/` and `public/` contain no runtime asset; `hooks/` already contains `usePublicCapabilities.ts`, so its keep-file has no effect. `docs/standards/frontend-standards.md:61-62,91` still describes `features` and `hooks` as placeholders and must be updated by docops.
- **User impact:** None.
- **Verification:** File inventory and docs validation.
- **False-positive risk:** LOW.

### F035-R07 - Generated Vite logs

- **Disposition / confidence:** REMOVE / HIGH
- **Targets:** untracked `frontend/vite.err.log`, `frontend/vite.out.log`; add a narrow ignore rule during approved cleanup.
- **Evidence:** Files are runtime outputs, appear in Git status, and are not matched by current ignore rules.
- **User impact:** None; active process output must be redirected to an operational log location before deletion if still needed.
- **Verification:** `git check-ignore -v`, Git status, runtime ownership check before destructive deletion.
- **False-positive risk:** MEDIUM until process ownership is checked immediately before deletion.

## REPLACE Inventory

### F035-P01 - Five stale subscription payment aliases

- **Disposition / confidence:** REPLACE / HIGH
- **Targets:**
  - routes at `frontend/src/router/index.tsx:184-199`
  - stale-redirect branch at `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx:31-61`
  - route/payment tests and `docs/ui/atstudio-front-list.md:58`, `docs/ui/screen-flow.md:53`
  - backend acceptance callback references at `src/main/resources/application-acceptance.yml:42-47` and `AcceptancePublicUrls.java:55-67`
- **Evidence:** Current producers create only `/subscriptions/checkout` URLs (`SubscriptionPlanPage.tsx:182`, `SubscriptionManagePage.tsx:352`). API spec calls one-time `/subscriptions/payment/success` stale at `docs/design/api-spec.md:1404`.
- **Replacement:** Retain only checkout plus checkout success/fail after backend one-time callback and endpoint disposition is complete.
- **User impact:** Removing frontend routes alone can turn an existing backend callback into 404. This must be one coordinated change.
- **Verification:** Router tests, billing-auth success/fail tests, backend callback configuration tests, Toss test callback smoke.
- **False-positive risk:** HIGH if changed in isolation; LOW as an integrated backend/frontend cleanup.

### F035-P02 - Download history URL and file naming

- **Disposition / confidence:** REPLACE / MEDIUM-HIGH
- **Targets:** `/download-queue`, `DownloadQueuePage.tsx/.module.css/.test.tsx`, lazy import name, Header and Profile route literals.
- **Evidence:** The page component is already named `DownloadHistoryPage` and calls only `/downloads/history` APIs (`DownloadQueuePage.tsx:1,38,71-98`). Active producers remain at `Header.tsx:26` and `ProfilePage.tsx:290`.
- **Replacement:** Prefer `/downloads` and `DownloadHistoryPage.*`; update all producers/tests/docs atomically. Do not leave a compatibility redirect under the approved no-legacy V1 policy unless the user changes that policy.
- **User impact:** Bookmark/path change; requires explicit destructive-change approval.
- **Verification:** Header/Profile navigation, query pagination/back-forward, bulk download, route inventory, no `/download-queue` frontend references.
- **False-positive risk:** MEDIUM because the old URL is functionally valid despite stale naming.

### F035-P03 - Misnamed admin payment operations page

- **Disposition / confidence:** REPLACE / HIGH
- **Targets:** `PaymentReadOnlyPage.tsx/.module.css/.test.tsx`, router lazy symbol at `frontend/src/router/index.tsx:99,251`.
- **Evidence:** The page is labeled "결제 운영" (`PaymentReadOnlyPage.tsx:678`) and includes settlement import, refund request/approve/execute, and entitlement-correction mutations (`PaymentReadOnlyPage.tsx:394-613`).
- **Replacement:** Rename to `PaymentOperationsPage` (or the approved canonical equivalent) without changing `/admin/payments`.
- **User impact:** None; source/test identity only.
- **Verification:** Typecheck, page tests, admin payment smoke.
- **False-positive risk:** LOW.

### F035-P04 - Native confirm dialogs

- **Disposition / confidence:** REPLACE / HIGH
- **Targets:** 10 `window.confirm` calls: seven in `PaymentReadOnlyPage.tsx:399-607`, two in `WhitelistChannelManagePage.tsx:119-154`, one in `WhitelistChannelPage.tsx:247-250`.
- **Evidence:** `docs/standards/frontend-standards.md:147` prohibits production `window.confirm`; reusable `ConfirmDialog` is active in `DownloadQueuePage.tsx:19,473`.
- **Replacement:** Use controlled `ConfirmDialog` state with explicit action, pending, cancellation, and focus behavior.
- **User impact:** More consistent and testable confirmation UX; preserve exact warnings and double-submit protection.
- **Verification:** Focus/escape/cancel/confirm tests, mutation call-count tests, admin/user smoke.
- **False-positive risk:** LOW.

### F035-P05 - Stale Mock payment presentation names

- **Disposition / confidence:** REPLACE / HIGH
- **Targets:** `SubscriptionPaymentPage.module.css:147-204` (`mockPanel`, `mockHeader`, `mockTitle`, `mockStatus`, `mockMeta`, `mockError`) and uses at `SubscriptionPaymentPage.tsx:321-334`.
- **Evidence:** Visible title is Toss recurring payment, while CSS comment/class names still describe Mock payment.
- **Replacement:** Rename to provider-neutral checkout/status names; no visual behavior change.
- **User impact:** None.
- **Verification:** Typecheck, payment-page tests, visual snapshot/smoke.
- **False-positive risk:** LOW.

### F035-P06 - Tracked TypeScript build cache

- **Disposition / confidence:** REPLACE / HIGH
- **Target:** tracked `frontend/tsconfig.tsbuildinfo`.
- **Evidence:** `frontend/.gitignore:5` already ignores `*.tsbuildinfo`, but `git ls-files --stage` confirms this file remains tracked. Repeated prior evidence records build-induced churn. Current required hash was preserved.
- **Replacement:** During approved destructive cleanup, remove it from Git tracking while leaving local generation ignored. Do not restore/delete it in this audit.
- **User impact:** None; cleaner verification and staging behavior.
- **Verification:** `git ls-files`, `git check-ignore`, two consecutive build/status checks.
- **False-positive risk:** LOW.

## REVIEW Inventory

### F035-V01 - Frontend package V1 version

- **Disposition / confidence:** REVIEW / HIGH factual confidence, policy decision required
- **Target:** `frontend/package.json:4` (`version: 0.1.0`) and lockfile root package version.
- **Question:** Set to `1.0.0` when the official V1 baseline is finalized, or retain `0.1.0` because the package is private and not independently released?
- **User impact:** None at runtime; release identity only.
- **Verification:** Package/lock parity and release documentation.

## KEEP Inventory

| ID | Target | Evidence and rationale | Confidence |
|---|---|---|---|
| F035-K01 | `frontend/vite.config.ts:8-139` public/acceptance ingress | Exact host allowlist, forwarding-header removal, and validated internal client IP form a security boundary; `vite.config.test.ts` covers it. It is not an auth bypass. | HIGH |
| F035-K02 | `frontend/src/utils/oauthAttempt.ts:3-163` | Active Login/SocialLogin/SocialCompleteProfile callers; validates OAuth state/PKCE lifetime and safe internal return paths. Filename search matching `temp` inside `Attempt` is an explicit false positive. | HIGH |
| F035-K03 | `frontend/src/utils/safeStorage.ts:1-64` | Active auth/player/OAuth callers; catches real browser storage access/quota failures. | HIGH |
| F035-K04 | `ProtectedRoute`, `SubscriberRoute`, `usePublicCapabilities` | Active authentication, ADMIN/USER, BUSINESS, subscription, OAuth/email capability boundaries. | HIGH |
| F035-K05 | `loadError.ts`, request AbortController/generation fences, company guide fallback | These handle cancellation, out-of-order completion, network/server failure, and unavailable settings. The represented failures remain possible. | HIGH |
| F035-K06 | `/subscriptions/checkout` plus success/fail and billing-agreement APIs | Current recurring subscription and payment-method re-registration flow; concrete producers and tests exist. | HIGH |
| F035-K07 | Whitelist/company-certification status and plan-limit handling | Current `PENDING`, `EXPORTED`, `REGISTERED`, `REVISION_REQUESTED`, removal, and certification states have UI/API dependencies. | HIGH |
| F035-K08 | Player store, browser-local play history, Waveform flat-line fallback, all Zustand stores | Current public full playback and local history behavior; waveform absence and autoplay/network failures remain valid cases. | HIGH |

## Explicit False-Positive Guards

1. `frontend/src/test/setup.ts` is unreachable from `main.tsx` by design but is configured at `frontend/vite.config.ts:145` as Vitest `setupFiles`; KEEP.
2. `frontend/src/pages/error/ErrorPage.module.css` has no same-basename TSX but is imported by both `NotFoundPage.tsx:2` and `ServerErrorPage.tsx:2`; KEEP.
3. `PaymentCheckout` and `PaymentOrderStatus` have no cross-file references but are used inside `api/payments.ts`; they are not dead exports.
4. `dataList ?? []`, error-message fallback, flat-waveform fallback, and cancellation checks do not prove legacy response support. They represent valid missing/failed inputs and remain KEEP unless a stronger contract proves impossibility.
5. `/albums` and `/albums/list` are two reachable, user-selectable views, not accidental duplicate pages.
6. `APP_PUBLIC_BASE_URL`/acceptance naming is not enough to classify the Vite code as temporary; current security and external-test consumers are concrete.

## Commands and Reproduction

Representative read-only commands:

```powershell
git branch --show-current
git status --short
git rev-parse HEAD
Get-FileHash frontend/tsconfig.tsbuildinfo -Algorithm SHA256
rg --files frontend/src
rg -n -i --glob '!tsconfig.tsbuildinfo' --glob '!*.log' '(legacy|compat|fallback|deprecated|mock|demo|acceptance|backup|TODO|FIXME|HACK)' frontend
rg -n '\b(fetchUser|addTracksToPlaylistBatch|cancelMyBillingAgreement|fetchSubscriptionPlanDetail|fetchAdminUserSubscriptionDetail|recordPlay|fetchPlayHistory|deletePlayHistory|DownloadQueueItem)\b' frontend
rg -n 'window\.confirm' frontend/src --glob '!*.test.ts' --glob '!*.test.tsx'
rg -n '(console\.(log|debug|warn|error)|debugger;)' frontend/src frontend/vite.config.ts --glob '!*.test.ts' --glob '!*.test.tsx'
git ls-files --stage -- frontend/tsconfig.tsbuildinfo frontend/src/features/.gitkeep frontend/src/hooks/.gitkeep
git check-ignore -v frontend/.env.local frontend/dist frontend/coverage frontend/vite.err.log frontend/vite.out.log
```

The import-graph check used an in-memory Node script only. It recursively read runtime TS/TSX/JS/JSX/CSS files, resolved static exports/imports and lazy `import()` references from `main.tsx`, and wrote no file.

## Tests

- No build, formatter, test runner, browser, server, or network command was run. This WI is a static read-only audit.
- Verification consisted of import graph analysis, exact reference searches, route/API contract comparison, and pre/post Git/HEAD/hash checks.
- Required implementation gates after approval: typecheck, ESLint, Prettier, Vitest, Vite build, route tests, payment callback tests, and role-specific browser smoke tests.

## Inspection Limits

- No requested repository-resident frontend surface is known to have been omitted: source, tests, root config/package files, public assets, matching API/UI documents, and directly coupled backend callback/endpoint references were enumerated statically.
- Static inspection cannot exhaustively observe external bookmarks, third-party OAuth/Toss callback traffic, browser-extension behavior, or runtime-only payload variants. Those are explicitly represented as false-positive/integration risks and must be covered by the post-remediation browser/provider smoke gates rather than inferred as absent.
- Dependency implementation internals under `frontend/node_modules` and generated `dist`/`coverage` output were intentionally excluded; package manifests, lock metadata, imports, and configured scripts were inspected.

## Risks / Rollback

- **Highest integration risk:** Removing stale payment routes before backend callback/config removal can break redirects. Treat F035-P01 as an atomic cross-layer work item.
- **User path risk:** Replacing `/download-queue` and removing `/playlists/new` breaks old bookmarks by design. Obtain destructive-change approval and include these paths in acceptance notes.
- **Over-cleaning risk:** Do not delete Vite ingress security, OAuth attempt state, storage/error fallbacks, or current status fences based on naming alone.
- **Rollback:** This audit changes no product/runtime state. Delete only the two WI deliverables to roll back the audit record.

## Files Created

- `deliverables/user/WI-20260716-ATS-035-summary.md`
- `deliverables/agent/WI-20260716-ATS-035-evidence-pack.md`

## Follow-up

- WI-038 should merge this inventory with WI-034 backend, WI-036 DB/config, and WI-037 docs/worktree findings, deduplicate cross-layer contracts, and present the exact destructive removal set for user approval.
