---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: qa-fe
category: audit
status: complete
dependencies:
  - path: WI-20260809-ATS-053-qa-handoff.md
    reason: Independent QA scope, acceptance criteria, and constraints
  - path: WI-20260809-ATS-053-handoff.md
    reason: Approved functional and quality contract
  - path: WI-20260809-ATS-053-pg-r2-result.md
    reason: Closed session and PII review gate
---

# QA Frontend Result: WI-20260809-ATS-053

## Findings

### QA-FE-053-001 [P2] License user search can republish results for a retired keyword or selected user

- `frontend/src/pages/admin/LicenseManagePage.tsx:84-103` advances the search
  generation only when another search is submitted.
- Editing the search input at `:133-136` hides the current dropdown but does not
  abort or retire the pending request. Selecting a user at `:110-114` and a URL
  user-context change also leave that request eligible to commit.
- Therefore, a request for keyword A can resolve after the input has changed to
  keyword B and set A's rows plus `showResults=true`. A pending search can also
  reopen its dropdown after a canonical `userId` selection, presenting users
  unrelated to the visible License context.
- `frontend/src/pages/admin/LicenseManagePage.test.tsx:85-123` races only the
  detail/license pair for User A versus User B; it does not exercise the search
  paths above.
- Impact: stale User rows can appear under a newer keyword or selected-user
  context and can lead an operator to replace the intended License target.
- Required remediation: abort and increment search ownership on input changes,
  selection, route/user retirement, and unmount; bind commit to the normalized
  submitted keyword as well as the generation. Add deferred tests for keyword
  A -> edited keyword B and pending search -> selected `userId`.

### QA-FE-053-002 [P2] The new User detail surface has no mobile entry point

- `frontend/src/pages/admin/UserManagePage.tsx:312-320` exposes User detail only
  through the table's fifth-column `View` button.
- `frontend/src/pages/admin/UserManagePage.module.css:344-353` explicitly hides
  column 5 at viewports up to 767 px. There is no row action, menu, or other
  visible control that opens the detail dialog at that breakpoint.
- The detail dialog itself has mobile styling at `:356-357`, but it cannot be
  reached through the rendered mobile UI.
- Impact: `CR-031-095` remains functionally missing for mobile ADMIN users, so
  the required read-only detail workflow is not consistently operable.
- Required remediation: keep a detail action visible at the mobile breakpoint,
  for example in a compact actions column or row menu, and add a viewport-level
  regression that opens, retries, and closes the dialog from the mobile list.

### QA-FE-053-003 [P2] Subscription mobile selectors now hide the wrong fields

- The changed table order in
  `frontend/src/pages/admin/SubscriptionManagePage.tsx:52-59` is Name,
  Audience, Monthly, Yearly, Download, Channel, Playlist, and Status.
- The unchanged mobile rules at
  `frontend/src/pages/admin/SubscriptionManagePage.module.css:122-127` hide
  columns 3 and 5. With the new order, that removes Monthly price and the daily
  download limit rather than the previously intended fields.
- Impact: at up to 767 px, the table does not expose all contracted limits and
  silently drops a price column. The jsdom content assertion passes because it
  does not apply the media-query visibility rule.
- Required remediation: revise the responsive layout after the column addition.
  Prefer horizontal table scrolling or a compact row layout that preserves all
  contracted fields; add a viewport-level assertion for audience, prices, and
  all three limits, including `maxPlaylists = -1`.

No P0, P1, or P3 finding was identified. Three P2 findings remain open.

## Verdict

**FAIL**

The QA handoff permits PASS only with no open P0-P3 finding. The three open P2
findings above prevent closure.

## Verification Results

| Check            | Result                                                                     |
| ---------------- | -------------------------------------------------------------------------- |
| Focused frontend | PASS: 12 files, 133/133 tests, 11.43 s                                     |
| TypeScript       | PASS: `npm run typecheck`                                                  |
| ESLint           | PASS: `npm run lint`, zero warnings allowed                                |
| Prettier         | PASS: `npm run format`                                                     |
| Backend focused  | PASS: 3 suites, 16/16 tests, 0 failures/errors/skipped; `BUILD SUCCESSFUL` |
| Documentation    | PASS: Tier 0, links, 585 IDs, and index                                    |
| Diff whitespace  | PASS: scoped `git diff --check`                                            |

Focused frontend command:

```text
npm test -- --run src/pages/admin/UserManagePage.integration.test.tsx src/pages/admin/UserManagePage.test.tsx src/pages/admin/LicenseManagePage.test.tsx src/pages/admin/QuestionManagePage.test.tsx src/pages/admin/TrackManagePage.test.tsx src/pages/admin/SiteSettingsPage.test.tsx src/api/client.test.ts src/api/adminContracts.test.ts src/api/domainApis.test.ts src/store/authStore.test.ts src/router/ProtectedRoute.test.tsx src/test/coverage/adminSubscriberPages.coverage.test.tsx
```

Backend focused command:

```text
.\gradlew.bat test --tests "com.atstudio.atstudio.controller.UserControllerTest" --tests "com.atstudio.atstudio.controller.SettingControllerTest" --tests "com.atstudio.atstudio.service.SiteSettingServiceTest" --rerun-tasks
```

Suite counts were User controller 10, Setting controller 3, and Site Setting
service 3. The independent run confirmed the bounded User DTO, ADMIN/public
Setting contract, Setting upsert behavior, stale-403 integration path, list
race tests, canonical Settings read, type safety, and static quality gates.

## Confirmed Evidence

- `PG-053-001` is closed by the independent PG R2 result. Source and the real
  Axios integration test establish one rejected PUT, one current-user GET, no
  PUT replay, server-derived role state, and canonical route-guard redirect.
- User detail request abort/generation fencing, bounded rendering, retry, and
  close behavior are present for reachable viewports.
- License detail/list, Question list, and Track list responses use abort plus
  generation ownership for the tested URL/filter transitions.
- Desktop plan rows distinguish same-name INDIVIDUAL/BUSINESS plans and render
  the Playlist limit with `-1` as unlimited.
- Settings freezes the submitted draft, performs one PUT followed by the public
  canonical GET, displays canonical success, and does not claim success when
  confirmation fails.

## Residual Risk

- No live browser, deployed environment, production data, or external provider
  was used. Responsive findings are source-level CSS/DOM conclusions; the
  focused jsdom suite cannot validate media-query visibility.
- Full frontend coverage and production build claims in the Evidence Pack were
  inspected but not rerun in this bounded QA pass. The current focused suite,
  backend suites, typecheck, lint, format, docs validation, and diff check were
  independently rerun.
- Protected output paths and ignored secret/local environment values were not
  inspected or modified.
