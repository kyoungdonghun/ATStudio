---
version: 1.0
last_updated: 2026-07-24
project: ATS
owner: PG
category: evidence-pack
status: stable
related_wi: WI-20260724-ATS-021
related_req: REQ-20260724-ATS-002
---

# Evidence Pack: WI-20260724-ATS-021

## Change Summary

- Performed a review-only assessment of the React Router advisories reported
  against the locked ATStudio frontend dependency graph.
- Inventoried production router call sites and traced user-, query-, hash-, and
  server-controlled navigation values.
- Assessed open-redirect/XSS and SSR hydration constructor injection
  independently.
- No product source, dependency manifest, or lockfile was changed.

## Scope And DoD

- [x] Recorded exact installed versions and advisory identifiers/ranges.
- [x] Inventoried production `Link`, `NavLink`, `navigate`,
  `router.navigate`, `Navigate`, `redirect`, and hydration/SSR surfaces.
- [x] Traced every call-site group that can receive a non-literal value.
- [x] Assessed open-redirect/XSS reachability separately.
- [x] Assessed constructor-injection reachability separately.
- [x] Compared major upgrade, application mitigation, and residual-risk paths.
- [x] Identified the approval point.
- [x] Preserved dependency and product-code state.

## Inputs Read

All handoff input pointers were reviewed:

- `docs/standards/core-principles.md`
- `docs/policies/security-policy.md`
- `docs/policies/access-control-policy.md`
- `docs/policies/quality-gates.md`
- `docs/standards/evidence-pack-standard.md`
- `docs/standards/frontend-standards.md`
- `deliverables/user/REQ-20260724-ATS-002.md`
- `deliverables/agent/WI-20260724-ATS-011-evidence-pack.md`
- `frontend/package.json`
- `frontend/package-lock.json`
- Production and test sources under `frontend/src/`

## Dependency Evidence

### Locked graph

Local commands:

```powershell
cd frontend
npm ls react-router react-router-dom --all --json
npm audit --json
```

Resolved graph:

```text
react-router-dom@6.30.4
└── react-router@6.30.4
```

Pointers:

- `frontend/package.json:14` declares `react-router-dom` as a direct
  dependency.
- `frontend/package-lock.json:4339-4345` locks `react-router@6.30.4`.
- `frontend/package-lock.json:4354-4361` locks
  `react-router-dom@6.30.4` and its exact `react-router@6.30.4` dependency.
- `frontend/node_modules/react-router/package.json` and
  `frontend/node_modules/react-router-dom/package.json` independently report
  version `6.30.4`.

The audit result contained two moderate vulnerable package records and three
advisory records. This distinction matters: npm groups the two React Router
advisories under the `react-router` package record while also reporting the
direct `react-router-dom` record.

| npm source | Advisory | Package and affected range | Upstream patch |
|---:|---|---|---|
| 1124268 | `GHSA-wrjc-x8rr-h8h6`, `CVE-2026-53669` | `react-router >=6.0.0 <7.18.0` | `7.18.0` |
| 1124270 | `GHSA-jjmj-jmhj-qwj2`, `CVE-2026-53668` | `react-router-dom >=6.30.2 <=6.30.4` | No patched 6.x version; npm recommends a major upgrade |
| 1124272 | `GHSA-337j-9hxr-rhxg`, `CVE-2026-53666` | `react-router >=6.4.0 <7.18.0` | `7.18.0` |

`npm audit` reported the available aggregate remediation as
`react-router-dom@7.18.1`, marked `isSemVerMajor=true`.

Primary sources:

- [GHSA-wrjc-x8rr-h8h6](https://github.com/remix-run/react-router/security/advisories/GHSA-wrjc-x8rr-h8h6)
- [GHSA-jjmj-jmhj-qwj2](https://github.com/remix-run/react-router/security/advisories/GHSA-jjmj-jmhj-qwj2)
- [GHSA-337j-9hxr-rhxg](https://github.com/remix-run/react-router/security/advisories/GHSA-337j-9hxr-rhxg)
- [React Router URL normalization patch](https://github.com/remix-run/react-router/pull/15176)
- [React Router SSR hydration patch](https://github.com/remix-run/react-router/pull/15175)

The first two links describe attacker-controlled navigation targets causing an
unexpected external navigation or XSS when an application permits an open
redirect. The third explicitly limits the constructor-injection issue to
Framework Mode or Data Mode applications performing manual SSR/hydration.

## Router Surface Inventory

### Production surface counts

Read-only `rg` inventory under `frontend/src`, excluding tests:

| Surface | Count | Notes |
|---|---:|---|
| `navigate(...)`, including one `router.navigate(...)` | 82 | Most targets are literals or `navigate(-1)`. |
| `router.navigate(...)` | 1 | Static `/login` target. |
| `<Link>` | 73 | No whole-string untrusted target found. |
| `<NavLink>` | 0 | Not used. |
| `<Navigate>` | 5 | Three static; two described below. |
| `redirect(...)` | 1 | Static `/admin/questions` target. |

### Whole-string controlled targets

These are the highest-sensitivity call sites because an externally controlled
value can become the complete navigation target.

| Pointer | Input provenance | Existing control | Reachability result |
|---|---|---|---|
| `frontend/src/router/ProtectedRoute.tsx:54-56` | Current pathname and query | Encodes the value as a `returnTo` query parameter on a fixed `/login` path | Cannot navigate externally at this step. |
| `frontend/src/pages/auth/LoginPage.tsx:75,83,153` | `returnTo` query parameter | `getSafeLoginReturnTarget` before both navigation calls | No bypass found in reviewed payload classes. |
| `frontend/src/utils/oauthAttempt.ts:38-71` | Login query value | Requires one leading slash, rejects `//`, raw/decoded backslashes, fragments, control characters, cross-origin parsing, and non-canonical round trips | Blocks the advisory's direct path classes. |
| `frontend/src/utils/oauthAttempt.ts:85-117` | Session-stored OAuth attempt | Validated before storage and revalidated after retrieval | Storage tampering does not bypass the guard. |
| `frontend/src/utils/oauthAttempt.ts:120-144` | Session-stored profile return target | Validated before storage and revalidated after retrieval | Storage tampering does not bypass the guard. |
| `frontend/src/pages/auth/SocialLoginPage.tsx:67,72` | Validated OAuth attempt record | Consumed only through the revalidating helper | No raw callback query becomes a route target. |
| `frontend/src/pages/auth/SocialCompleteProfilePage.tsx:141` | Validated profile return record | Consumed through the revalidating helper; fallback `/` | No raw storage value becomes a route target. |
| `frontend/src/router/ProtectedRoute.tsx:60` | `deniedRedirect` prop | All production callers in `frontend/src/router/index.tsx` pass fixed literals (`/`, `/admin/payments`) | Not externally controlled in the current route graph. |

No production router target is derived from `location.hash`, and
`getSafeLoginReturnTarget` rejects fragments.

### Fixed-prefix query targets

These values may contain user or server data, but they remain query values
under a fixed same-origin route.

| Pointer | Input | Construction | Result |
|---|---|---|---|
| `frontend/src/layouts/Header.tsx:117` | User-entered search text | `encodeURIComponent` under `/tracks?...` | Same-origin path |
| `frontend/src/pages/public/HomePage.tsx:108-124` | Server tag names selected by the user | `URLSearchParams` under `/tracks?...` | Same-origin path |
| `frontend/src/pages/public/SubscriptionPlanPage.tsx:178-182` | Server plan name and local cycle enum | Fixed `/subscriptions/...?...` prefix | Same-origin path; query construction could be normalized later for consistency |
| `frontend/src/pages/subscriber/SubscriptionManagePage.tsx:340-352` | Selected plan/cycle and computed amount | `URLSearchParams` under `/subscriptions/checkout` | Same-origin path |
| `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx:20-28,84,399-402` | Callback/query return plan and validated cycle enum | `URLSearchParams` under `/subscriptions/manage` | Same-origin path |

### Fixed-prefix path-segment targets

These call sites receive route parameters or server identifiers, but the
complete target always starts with a fixed ATStudio path. None can supply a
scheme or protocol-relative prefix.

`Link` call sites:

- `frontend/src/components/track/TrackRow.tsx:67`
- `frontend/src/pages/subscriber/DownloadHistoryPage.tsx:442`
- `frontend/src/pages/subscriber/LikeListPage.tsx:244`
- `frontend/src/pages/subscriber/PlayHistoryPage.tsx:157`
- `frontend/src/pages/subscriber/PlaylistDetailPage.tsx:268`
- `frontend/src/pages/subscriber/PlaylistEditPage.tsx:180`
- `frontend/src/pages/public/AlbumDetailPage.tsx:257`
- `frontend/src/pages/public/HomePage.tsx:307`
- `frontend/src/pages/public/NoticeListPage.tsx:132`
- `frontend/src/pages/admin/TrackManagePage.tsx:203`

`navigate` call sites:

- `frontend/src/layouts/PlayerBar.tsx:625,639,847,861`
- `frontend/src/pages/creator/AlbumManagePage.tsx:183`
- `frontend/src/pages/subscriber/LikeListPage.tsx:323`
- `frontend/src/pages/subscriber/PlaylistDetailPage.tsx:226`
- `frontend/src/pages/subscriber/PlaylistEditPage.tsx:127,188`
- `frontend/src/pages/subscriber/PlaylistListPage.tsx:159`
- `frontend/src/pages/admin/NoticeEditPage.tsx:110`
- `frontend/src/pages/admin/QuestionManagePage.tsx:176`
- `frontend/src/pages/public/AlbumListImagePage.tsx:73`
- `frontend/src/pages/public/AlbumListPage.tsx:124`
- `frontend/src/pages/public/HomePage.tsx:132`
- `frontend/src/pages/subscriber/QuestionListPage.tsx:174`

### Non-controlled variable targets

- `frontend/src/layouts/Header.tsx:153,162,268,277` uses `item.path`, but every
  value comes from fixed module constants at lines 14-33.
- `frontend/src/layouts/AdminLayout.tsx:76` uses `item.path`, but every value
  comes from the fixed `MENU_ITEMS` array at lines 12-28.
- `frontend/src/layouts/PlayerBar.tsx:120-123` limits `guestTarget` to the
  literal union `'/login' | '/subscriptions'`.
- `frontend/src/pages/subscriber/ProfilePage.tsx:288-294,336` resolves from a
  fixed `ACTIVITY_ROUTES` map.
- `frontend/src/api/client.ts:118` calls `router.navigate('/login')`.
- `frontend/src/router/index.tsx:193` calls
  `redirect('/admin/questions')`.
- Remaining `Link`, `Navigate`, and `navigate` targets are string literals or
  numeric history deltas.

## Advisory 1: Open Redirect And XSS

### Upstream condition

The upstream advisories require attacker-supplied paths to reach React Router
navigation mechanisms as a complete destination. The 7.18.0 patch broadens
absolute/protocol-relative URL recognition to cover slash and backslash
combinations.

### ATStudio assessment

**No exploitable current call site was found.**

The only user-controlled complete target is the login/OAuth return target.
`getSafeLoginReturnTarget` rejects:

- absolute schemes such as `https:` and `javascript:`,
- protocol-relative `//`,
- raw and once-decoded backslashes,
- encoded protocol-relative paths,
- URL fragments and control characters,
- cross-origin parsing,
- paths changed by normalization.

All other non-literal targets either use a fixed same-origin path prefix or
encode values with `URLSearchParams`.

### Read-only behavior probe

The current module was loaded through Vite without modifying source. The guard
accepted `/profile?tab=edit` and rejected representative absolute, protocol-
relative, mixed-slash, backslash, encoded-backslash, encoded-double-slash,
`javascript:`, control-character, and fragment targets. A double-encoded
backslash remained an encoded same-origin pathname in a supplementary
`createBrowserRouter`/JSDOM probe.

This probe supports the static analysis but is not a substitute for upstream
patching or real-browser regression tests.

### Residual risk

- The vulnerable package code remains installed, so `npm audit` correctly
  remains non-zero.
- A future call site that sends an unvalidated whole string to `Link` or
  `navigate` could make the advisory reachable.
- Existing tests cover absolute/protocol-relative and encoded slash targets,
  but do not name all newly disclosed mixed/backslash payloads.

Disposition: **low current application exploitability, moderate residual
dependency risk**.

## Advisory 2: SSR Hydration Constructor Injection

### Upstream condition

`GHSA-337j-9hxr-rhxg` requires Framework Mode or Data Mode with manual
SSR/hydration and attacker influence over serialized errors passed into client
hydration.

### ATStudio assessment

**Not reachable in the current runtime architecture.**

Production pointers:

- `frontend/src/main.tsx:2,5` uses `createRoot`, not `hydrateRoot`.
- `frontend/src/App.tsx:1,6` renders `RouterProvider`.
- `frontend/src/router/index.tsx:1,241` uses `createBrowserRouter`.
- No production reference exists to `hydrateRoot`, `HydratedRouter`,
  `StaticRouterProvider`, `createStaticRouter`, `deserializeErrors`,
  `hydrationData`, or `window.__staticRouterHydrationData`.

The installed package contains optional server modules, but ATStudio does not
import or execute them.

Disposition: **not applicable to the current client-only architecture**.
Reassess immediately if SSR, framework hydration, or manual hydration data is
introduced.

## Verification Commands And Results

Commands:

```powershell
cd frontend
npm ls react-router react-router-dom --all --json
npm audit --json
npm test -- src/utils/oauthAttempt.test.ts src/pages/auth/LoginPage.test.tsx src/pages/auth/SocialLoginPage.test.tsx src/pages/auth/SocialCompleteProfilePage.test.tsx
rg -n --glob '*.ts' --glob '*.tsx' --glob '!**/*.test.*' --glob '!**/test/**' '\bnavigate\s*\(' src
rg -n --glob '*.tsx' --glob '!**/*.test.*' --glob '!**/test/**' '<(Link|NavLink|Navigate)\b' src
rg -n -i --glob '*.{ts,tsx,js,jsx}' --glob '!**/*.test.*' --glob '!**/test/**' 'redirect\s*\(|hydrateRoot|HydratedRouter|StaticRouterProvider|createStaticRouter|deserializeErrors|createBrowserRouter|RouterProvider' src
```

Results:

- Dependency resolution: PASS, exact `6.30.4` pair confirmed.
- Audit reproduction: PASS, two moderate package records / three advisory
  records reproduced.
- Focused return-target/OAuth tests: PASS, 4 files and 34 tests.
- Production call-site inventory: PASS.
- SSR/hydration negative inventory: PASS.
- Dependency or lockfile mutation: none.

Integrity hashes after review:

```text
frontend/package.json
1DB20FE719A32D78B6846C021781E371A5C69535E49625B2BA94063DD96EF6A0

frontend/package-lock.json
02006B02FF31D3D89124349A09584752105571313302AF12602B862C0F1B74BF
```

## Remediation Options

| Option | Security result | Cost / limitation | Recommendation |
|---|---|---|---|
| Controlled upgrade to `react-router-dom@7.18.1` | Removes the reported vulnerable dependency ranges | SemVer-major migration; requires a separate approved WI and full router regression suite | **Recommended before production** |
| Application mitigation only | Current call sites remain non-reachable if the route-target invariant is preserved | Audit remains non-zero; future call-site drift can reintroduce reachability | Accept only as a time-bounded non-production exception |
| Suppress or ignore audit | No technical remediation | Hides known residual risk | Reject |
| `npm audit fix --force` | May install the major version automatically | Unreviewed migration and lockfile churn | Reject |

## Approval Point

User approval is required to choose:

1. A separate controlled React Router 7.18.1 migration WI before production
   (**recommended**), or
2. A documented, time-bounded residual-risk exception for the non-production
   rehearsal plus a corrective WI for advisory-specific route-target tests.

WI-20260724-ATS-020 should remain unable to claim production readiness until
the selected disposition is recorded.

## Risk And Rollback

- Review-only: no product-code rollback is required.
- The two WI-021 files can be removed if this review is superseded.
- No dependency, lockfile, runtime, database, or secret state was changed.
