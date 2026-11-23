# WI-20261121-ATS-007 frontend review evidence

The independent frontend reviewer inspected the WI004 candidate on 2026-11-21, corrected the findings below and verified the resulting local pilot build. [Approved request](../user/REQ-20261121-ATS-003.md) · [Handoff and Tier 0–2 input pointers](WI-20261121-ATS-007-handoff.md) · [WI004 baseline](WI-20261121-ATS-004-evidence-pack.md)

## Findings and corrections

| Finding | Correction and evidence |
| --- | --- |
| A tab could retain A's UI while Axios read B's bearer from shared local storage. A delayed logout or refresh could also alter B's login. | The pilot selects one login record and binds its user, access token and refresh token together. Each login has a separate ID. Refresh and logout only modify their own record. Storage events and private request/response checks reconcile the displayed owner, including before delayed events arrive. A password login's explicit identity check cannot replay using an older account. [Session persistence](../../frontend/src/store/pilotSession.ts), [store](../../frontend/src/store/authStore.ts), [HTTP client](../../frontend/src/api/client.ts), [login bootstrap](../../frontend/src/api/auth.ts) |
| Catalog downloads could finish an asynchronous action after the initiating account had changed. Error decoding and quota feedback also had unguarded continuations. | Pilot list/detail download actions verify the owning login before saving the file or showing follow-up feedback. Personal history and license pages recheck ownership after decoding asynchronous errors. [Catalog list](../../frontend/src/pages/public/TrackListPage.tsx), [detail](../../frontend/src/pages/public/TrackDetailPage.tsx), [history](../../frontend/src/pages/subscriber/DownloadHistoryPage.tsx), [license list](../../frontend/src/pages/subscriber/LicenseListPage.tsx), [owner check](../../frontend/src/utils/ownerProjection.ts) |
| Download history transferred its private track list into the shared player, whose queue persists across logins. A previously downloaded track can subsequently be withdrawn. | Pilot history no longer seeds the shared player or provides direct playback from history metadata. Its title link leads to the public track detail where publication is checked. Single-track re-download remains available under the backend policy. |
| Review of the initial persistence fix identified a possible stuck login button when browser storage rejects a new session. | The store advances the login generation after the new record is successfully selected. The real login component displays an error and re-enables submission on storage failure. [Rendered regression tests](../../frontend/src/router/pilotPersonalPages.test.tsx) |

## Verification

Run the following from `frontend` using the installed dependencies. Vitest selects pilot scenarios via the test mocks. Set the build environment variable only for the build.

```powershell
npm.cmd test -- src/api/pilotSession.test.ts src/router/pilotPersonalPages.test.tsx src/router/pilot.test.tsx src/router/index.listening.test.tsx src/api/client.test.ts src/pages/subscriber/DownloadHistoryPage.test.tsx src/pages/subscriber/LicenseListPage.test.tsx src/pages/subscriber/LicenseDetailPage.test.tsx src/pages/public/TrackDetailPage.test.tsx src/pages/public/TrackListPage.test.tsx src/pages/auth/LoginPage.test.tsx src/store/authStore.test.ts src/api/auth.test.ts
npm.cmd run typecheck
npm.cmd run lint
$env:VITE_APP_MODE='pilot'
npm.cmd run build -- --outDir ../build/WI-20261121-ATS-007/pilot
```

- All 181 tests in 13 selected files passed. The [six HTTP/session regressions](../../frontend/src/api/pilotSession.test.ts) include pre-event dispatch, stale refresh and logout, same-user renewal, explicit bootstrap and remote revocation. The [five rendered scenarios](../../frontend/src/router/pilotPersonalPages.test.tsx) include switching account while a license is displayed, successful and unsuccessful late file transfers and the login storage failure.
- Type checking, repository frontend ESLint, and Prettier checks on the eleven WI007 frontend source/test files passed. The pilot build compiled 312 modules. The SHA-256 of the generated index was `1a5bba3f8d86b78480e509650891cf618e47666e2646293bedc22cd87414f123` and the entry script was `index-CM8gnXmj.js`.
- The initial rendered storage-failure test queried the form synchronously during capability loading. The one focused retry after waiting for the form passed. The final 13-file run passed without a skip.
- WI008 separately exercised two native Chrome tabs with this same bundle and a local HTTP fixture. After B signed in, both tabs rendered only B's history without a reload. The individual HTTP regression tests simulate delayed completions that are difficult to time by hand.

## Full-suite follow-up

The orchestrator's first complete frontend run passed 1,707 of 1,712 tests. All five failures were investigated. The staged-login request contract now asserts that an explicit bootstrap credential disables replay. Two admin coverage scenarios used an incomplete mock of the existing catalog hook's authentication dependency. Two upload coverage scenarios still expected raw provider text or obsolete error text. The committed upload handler already instructs the operator to verify whether an ambiguous request was saved before retrying. The updated tests verify this guidance, absence of the raw error, and retry of the failed entry only. The first expectation correction used the validation fallback and was rejected by the two upload tests because their fixtures specifically represent ambiguous network outcomes. The focused correction for that case passed. All 98 tests in the four affected files passed together. Only tests changed after the pilot bundle was built.

```powershell
npm.cmd test -- src/api/authContracts.test.ts src/test/coverage/adminSubscriberPages.coverage.test.tsx src/test/coverage/publicAuthShell.coverage.test.tsx src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx
```

## Operation and rollback

Pilot uses its own session storage keys within browser localStorage. A participant or administrator signs in once when first entering this build. Subsequent pilot tabs and reloads share that login. Existing listening and selling builds continue using their original storage format. Historical sessions, live accounts and external services were outside this local review.

To roll back this review locally, revert the listed WI007 frontend changes against the WI004 baseline as a group and discard its isolated build output. Preserve the pre-existing uncommitted WI004 changes. Server verification, deployment and mobile device acceptance remain pending.
