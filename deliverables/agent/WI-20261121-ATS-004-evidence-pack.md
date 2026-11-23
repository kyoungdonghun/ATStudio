# WI-20261121-ATS-004 frontend evidence

Implemented and verified locally on 2026-11-21 by the frontend implementation agent for [REQ-20261121-ATS-002](../user/REQ-20261121-ATS-002.md). See the [handoff](WI-20261121-ATS-004-handoff.md) for the approved boundaries.

## Changes

- An explicit Vite pilot mode uses the restricted listening route tree and adds a signed-in USER's download history, license list and license detail. Existing admin routes retain their role guards. [Routes](../../frontend/src/router/index.tsx)
- Public track browsing remains anonymous. Individual downloads and personal API requests send the current account's credentials. Protected reads reject responses from an obsolete session. The frontend obtains the user role from the server after login. [Request policy](../../frontend/src/config/appMode.ts) and [client](../../frontend/src/api/client.ts)
- Pilot participants can download individual tracks, retry from personal history and open license details. Pilot pages omit bulk selection and links to sales. Authorization, release visibility, identity and entitlements are enforced by the backend.

## Verification

Run in the local `frontend` directory with installed dependencies.

| Check | Result |
| --- | --- |
| `npm.cmd run typecheck` | Passed |
| `npm.cmd run lint` | Passed |
| `$env:VITE_APP_MODE='pilot'` followed by `npm.cmd run build -- --outDir ../build/WI-20261121-ATS-004/final` in PowerShell | Passed, 311 modules |
| Focused Vitest command below | 10 files, 142 tests passed |

```powershell
npm.cmd test -- src/router/pilotPersonalPages.test.tsx src/router/pilot.test.tsx src/router/index.listening.test.tsx src/api/client.test.ts src/pages/subscriber/DownloadHistoryPage.test.tsx src/pages/subscriber/LicenseListPage.test.tsx src/pages/subscriber/LicenseDetailPage.test.tsx src/pages/public/TrackDetailPage.test.tsx src/pages/public/TrackListPage.test.tsx src/pages/auth/LoginPage.test.tsx
```

[Pilot route tests](../../frontend/src/router/pilot.test.tsx) cover roles, destinations and request classification. [Rendered pilot flows](../../frontend/src/router/pilotPersonalPages.test.tsx) use a local Axios adapter for a public detail page and file download, and two synthetic accounts for personal history and license detail. During test development the detail fixture lacked numeric counters and a text query matched both the heading and breadcrumb. Completing the fixture and selecting the heading resolved the failures.

## Verification boundaries

Frontend results cover rendered behavior and requests with controlled local data. Real backend account isolation is covered separately by the local integration work item. Independent review and server verification remain pending. Build output is local. Existing listening and selling configurations do not select the pilot mode.
