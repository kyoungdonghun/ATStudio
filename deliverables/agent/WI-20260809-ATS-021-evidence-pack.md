# WI-20260809-ATS-021 Evidence Pack

## Status

- Result: `11 PASS / 3 FAIL / 1 BLOCKED` scenario groups.
- Product code changed: No.
- Existing current-state documentation changed: No.
- Runtime or DB changed: No.
- External side effects: None; only `S0` read/navigation behavior was executed.
- Baseline: `e343c20` on `codex/v1-release-rehearsal-fixes`.

One delegated `qa-fe` documentation worker remained in `running` state through
bounded waits and was stopped to avoid leaving another stale worker. It wrote
no files and returned no result. The browser evidence and this pack were
completed from the reproducible WI-021 session observations.

## Deliverables

- `deliverables/agent/WI-20260809-ATS-021-handoff.md`
- `deliverables/agent/WI-20260809-ATS-021-findings.md`
- `deliverables/agent/WI-20260809-ATS-021-evidence-pack.md`
- `deliverables/user/WI-20260809-ATS-021-summary.md`
- `output/ui-ux-audit/20260809/WI-021/`

## Environment Preflight

| Check                  | Result | Evidence                                                                                                                         |
| ---------------------- | ------ | -------------------------------------------------------------------------------------------------------------------------------- |
| Local frontend         | PASS   | `http://127.0.0.1:5173/` returned the AT.M SPA.                                                                                  |
| Local backend          | PASS   | `http://127.0.0.1:8080/api/utils/public-capabilities` returned `200`.                                                            |
| Current Cloudflare URL | PASS   | The active tunnel metrics identified `https://newest-gary-justify-longer.trycloudflare.com`.                                     |
| Runtime identity       | PASS   | Local and public `/` bodies were exactly equal at 770 bytes; local and public capability bodies were exactly equal at 285 bytes. |
| Branch baseline        | PASS   | `codex/v1-release-rehearsal-fixes` at `e343c20`; no tracked product diff existed when the WI started.                            |

## Scenario Results

| Scenario group                          | Status  | Role / viewport                     | Result and evidence                                                                                                                                                                         |
| --------------------------------------- | ------- | ----------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `ENV-01` runtime identity               | PASS    | Anonymous / desktop                 | Local and public root and capability bodies matched exactly.                                                                                                                                |
| `G-PUBLIC` public shell                 | PASS    | Anonymous / desktop, tablet, mobile | AT.M brand, public navigation, Login/Subscribe actions, content, footer, and empty PlayerBar rendered. Home screenshots: `G-PUBLIC_PUB-01_SH-01_VD_home.png`, `G-PUBLIC_SH-01_VM_home.png`. |
| `G-AUTH` protected guard read           | PASS    | Anonymous / desktop                 | `/profile` redirected to `/login?returnTo=%2Fprofile` and persisted after reload.                                                                                                           |
| `G-SUB` subscriber guard read           | FAIL    | Anonymous / desktop                 | `/playlists` redirected to `/login` and lost the safe return target. `F-UI-021-002`.                                                                                                        |
| `G-PAY` Checkout guard read             | PASS    | Anonymous / desktop                 | The representative Checkout deep link redirected to Login with an encoded safe `returnTo`; no Checkout mutation ran.                                                                        |
| `G-BUS` BUSINESS guard read             | PASS    | Anonymous / desktop                 | The representative business-status deep link redirected to Login with a safe `returnTo`.                                                                                                    |
| `G-ADMIN` ADMIN guard read              | PASS    | Anonymous / desktop                 | Dashboard and contextual track-edit deep links redirected to Login with safe return targets.                                                                                                |
| `PUB-08` Notice list                    | PASS    | Anonymous / 1440, 390, 360          | One valid Notice, sort controls, and the responsive title-only mobile table rendered. No horizontal overflow. Screenshots: `PUB-08_VD_notice-list.png`, `PUB-08_VM_notice-list.png`.        |
| `PUB-09` Notice detail                  | FAIL    | Anonymous / desktop                 | Valid Notice detail, metadata, body, and attachment boundary rendered. Missing Notice handling failed its localized recovery expectation. `F-UI-021-001`.                                   |
| `ERR-01` explicit server error          | PASS    | Anonymous / desktop, 360            | Korean 500 page, Home recovery link, and no stack or secret output.                                                                                                                         |
| `ERR-02` wildcard not found             | PASS    | Anonymous / desktop, 360            | Korean 404 page and Home recovery link.                                                                                                                                                     |
| `SH-01` Header behavior                 | FAIL    | Anonymous / desktop, 390            | Theme state and mobile button/overlay/route close paths worked. Escape close and accessible-name localization failed. `F-UI-021-003`, `F-UI-021-004`.                                       |
| `RESP-01` representative responsiveness | PASS    | 1024x768, 360x800                   | `/`, `/notices`, `/tracks/3`, `/subscriptions`, `/login`, and wildcard 404 had no horizontal overflow.                                                                                      |
| `KEY-01` native keyboard behavior       | BLOCKED | Anonymous / desktop, mobile         | The in-app browser did not dispatch implicit Submit or Tab traversal reliably. Unit evidence passed, but real-browser keyboard acceptance remains open. `B-UI-021-001`.                     |
| `CONSOLE-01` console health             | PASS    | Anonymous / sampled routes          | No uncaught runtime error was observed. The React Router v7 future warning remains `D-UI-021-001`.                                                                                          |

## Responsive Measurements

The representative 1024px and 360px sweep compared
`document.documentElement.scrollWidth` with `window.innerWidth` after each
route loaded. All twelve route/viewport combinations reported no horizontal
overflow. At 390px, the mobile menu exposed correct open/close labels and
`aria-expanded`; link navigation closed the menu.

## Browser and Source Cross-Checks

1. `/notices/999999` returned localized `404 RESOURCE_NOT_FOUND`, while the
   page reduced every rejected request to `Failed to load notice`.
2. `ProtectedRoute` builds a safe path-plus-query `returnTo`; `SubscriberRoute`
   sends anonymous users directly to `/login`.
3. `Header` has overlay and route-change close behavior, but no Escape listener.
4. Theme toggling changed the rendered state and was restored to the original
   light mode after the assertion.
5. The Header-focused Vitest command passed 27 tests in two files. This proves
   component event logic but does not close `B-UI-021-001`.

Focused test command:

```powershell
Set-Location frontend
npm run test -- --run `
  src/layouts/Header.test.tsx `
  src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx
```

Result: two files and 27 tests passed.

## Screenshot Index

| File                                        | Purpose                                |
| ------------------------------------------- | -------------------------------------- |
| `G-PUBLIC_PUB-01_SH-01_VD_home.png`         | Desktop public shell and Home baseline |
| `G-PUBLIC_SH-01_VM_home.png`                | Mobile public shell baseline           |
| `PUB-08_VD_notice-list.png`                 | Desktop Notice list                    |
| `PUB-08_VM_notice-list.png`                 | Mobile Notice list                     |
| `F-UI-021-001_PUB-09_VD_missing-notice.png` | Missing Notice error finding           |

All files are under `output/ui-ux-audit/20260809/WI-021/`.

## Safety and Cleanup

- No Login, Signup, provider, refund, mail, attachment download, file upload,
  data mutation, or schema operation was executed.
- The intentional
  `output/client-demo-screenshots-20260716-140514.zip` remains untouched and
  untracked.
- The theme was restored to its original light state.
- Only one controlled browser tab was used; no extra tab was left open.

## Limits

- Browser screenshots are bounded visual evidence; DOM measurements are the
  authority for viewport dimensions and overflow.
- Header implicit Submit and Tab traversal require another input surface.
- One public track timing sample is an observation, not a confirmed latency
  defect.
- Authenticated, stateful, file, mail, provider, and payment checks remain for
  their owned WIs.

## Rollback

Delete the three new WI-021 result documents and the WI-021 screenshot folder
to remove this audit output. Product code, runtime, and DB require no rollback.

## Next WI

WI-20260809-ATS-022 audits Login, Signup, email verification, password reset,
safe Login return behavior, and Profile/account states. It must preserve the
approved mail boundary and avoid uncontrolled external delivery.
