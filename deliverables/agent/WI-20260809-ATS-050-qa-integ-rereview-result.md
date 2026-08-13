# Independent QA Integration Rereview: WI-20260809-ATS-050

## Findings

### F-QA-INTEG-050-001 - P2 - OPEN - Ambiguous create ownership is lost across a non-observation route

**Contract:** After an ambiguous create/update/delete result, the identical
mutation must remain impossible until an observation-only path is taken. The
observation may issue only GET and may not claim mutation success or failure.

**Pointers:**

- `frontend/src/pages/admin/NoticeCreatePage.tsx:34-42,85-100,112-127,232-250`
- `frontend/src/layouts/AdminLayout.tsx:12-27,66-88`
- `frontend/src/pages/admin/NoticeAdminPages.test.tsx:190-207`
- `docs/ui/atstudio-front-list.md:100-109`
- `docs/ui/screen-flow.md:119-128`

**Reproducible schedule and exact calls:**

1. Open `/admin/notices/new`, submit Notice A, and make the POST reject with no
   response. The first `createNotice` call count is one.
2. The catch path sets `recoveryRequiredRef.current = true` and
   `outcomeUnknown = true`, but `finally` clears `operationRef`. The local form
   blocks another submit only while this component instance remains mounted.
3. Use any enabled Admin sidebar link other than the supplied `/notices`
   observation link, then use the sidebar `공지사항` link, which returns directly
   to `/admin/notices/new`.
4. The new create-page instance initializes `recoveryRequiredRef` and
   `outcomeUnknown` to false and performs zero Notice GETs. Submitting the same
   values issues a second POST. Expected count before a Notice observation is
   still one; source behavior permits two.

The new test checks only that submit is disabled in the original mount and that
an observation link exists. It neither follows an observation GET nor exercises
sidebar departure, remount, and repeat. Pending navigation itself is now blocked,
and edit/delete can unlock through a fresh ADMIN GET, but the create
counterexample keeps the original finding open.

**Required remediation:** Preserve the ambiguous create attempt across route
retirement or constrain recovery so only a completed Notice-list GET can release
the duplicate fence. Add a route-remount test proving one POST and at least one
observation GET before a later deliberate create can become available.

### F-QA-INTEG-050-004 - P2 - The browser-unload listener is installed while no operation exists

**Contract:** `beforeunload` must be installed only while current mutation
ownership exists and released on authoritative success, authoritative failure,
ambiguous settlement, and unmount.

**Pointers:**

- `frontend/src/hooks/usePendingMutationGuard.ts:4-18`
- `frontend/src/pages/admin/NoticeAdminPages.test.tsx:100-140`
- `frontend/node_modules/react-router-dom/dist/react-router-dom.development.js:1415-1427`

**Reproducible schedule:** Mount an idle create or edit page without submitting.
`usePendingMutationGuard` invokes `useBeforeUnload` unconditionally. The locally
installed React Router 6.30.4 implementation immediately calls
`window.addEventListener("beforeunload", callback)` and removes it only when the
hook callback changes or the component unmounts. The callback is a no-op while
`operationRef.current` is null, but the listener remains installed for the full
page lifetime.

The current test dispatches an event during and after a mutation and checks only
`defaultPrevented`; it does not assert listener add/remove ownership. This fails
the explicit lifecycle contract even though idle unload is not cancelled.

**Required remediation:** Drive listener registration from reactive pending
ownership, and spy on `addEventListener`/`removeEventListener` for idle, pending,
success, 4xx, ambiguous, and unmount schedules.

### F-QA-INTEG-050-005 - P2 - Busy Notice delete can release keyboard focus outside the modal

**Contract:** The shared busy Modal must keep an accessible modal boundary while
the header close action, Escape, and backdrop close are unavailable.

**Pointers:**

- `frontend/src/components/ui/Modal.tsx:43-68,126-151`
- `frontend/src/pages/admin/NoticeEditPage.tsx:578-610`
- `frontend/src/components/ui/Modal.test.tsx:43-57,123-142`
- `frontend/src/pages/admin/NoticeAdminPages.test.tsx:331-365`

**Reproducible schedule:** Open Notice delete, start a pending DELETE, and press
Tab. Busy state disables the header close button and the Notice modal disables
both content buttons, leaving zero focusable descendants. `handleKeyDown`
returns when `focusable.length === 0` without preventing Tab or refocusing the
dialog, so normal browser focus traversal can leave the modal.

The shared busy test keeps an artificial enabled `내부 작업` button, while the
real Notice test checks close, Escape, and backdrop but not Tab. The original
enabled-inert close defect is fixed, but this is a new busy-state accessibility
regression.

**Required remediation:** When busy content has no actionable descendants,
retain focus on the dialog and prevent Tab from escaping. Cover the actual
all-disabled Notice modal, then prove failure recovery restores the normal close
and focus cycle.

### F-QA-INTEG-050-003 - P3 - OPEN - Mandatory transition lifecycle proof remains partial

The public 5xx, valid-to-invalid, download-unmount, and update destination-GET
cases were added with exact call assertions. The ADMIN suite still does not
block a transition before an authoritative 4xx or ambiguous rejection, so it
does not execute the required reset schedule for those terminal outcomes. It
also mounts a failing destination only after update success; create/delete
success still navigate to a placeholder instead of proving that destination
recovery performs GET only and never repeats POST/DELETE.

**Pointers:**

- `frontend/src/pages/admin/NoticeAdminPages.test.tsx:100-207,262-329,331-437`
- `frontend/src/pages/public/NoticeDetailPage.test.tsx:92-135,174-205`

This remains an evidence gap, not an independently observed second mutation.

## Original Finding Closure

| Original finding          | Status     | Independent result                                                                                                                                                                                                                                                    |
| ------------------------- | ---------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `F-QA-INTEG-050-001` (P2) | **OPEN**   | Pending link/history/target transitions are blocked and authoritative success resets the blocker before programmatic navigation. Update/delete have fresh-GET recovery. Ambiguous create can still be repeated after a non-observation route and create-page remount. |
| `F-QA-INTEG-050-002` (P2) | **CLOSED** | Header close is natively disabled and excluded from the focusable query; Escape/backdrop use the same busy gate; `aria-busy` is exposed; authoritative failure restores close behavior. `F-QA-INTEG-050-005` records a separate zero-focusable regression.            |
| `F-QA-INTEG-050-003` (P3) | **OPEN**   | All four named public schedules and one update destination-read failure are now covered, but the required blocked-transition failure matrix and create/delete destination GET schedules are absent.                                                                   |

No P0 or P1 finding was identified.

## Mandatory Counterexample Matrix

| Case                                                           | Result         | Exact independent evidence                                                                                                                                                                                                                                    |
| -------------------------------------------------------------- | -------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Pending link/history/target change, then authoritative success | **PASS**       | Router remains on create/edit; mutation count stays one; after resolve, programmatic destination navigation succeeds.                                                                                                                                         |
| Pending transition, then authoritative 4xx                     | **NOT PROVEN** | 400/409 retry tests exist, but no transition is attempted before rejection. Mutation counts are one before deliberate retry and two after it.                                                                                                                 |
| Pending transition, then ambiguous network result              | **FAIL**       | Same-mount submit remains one, but create can remount through a non-observation route and issue POST two.                                                                                                                                                     |
| Direct unmount during accepted create                          | **PASS**       | `createNotice` receives one argument, so no request AbortSignal is supplied; unmount followed by resolution leaves call count at one.                                                                                                                         |
| `beforeunload` only during ownership                           | **FAIL**       | Guard behavior is conditional, listener registration is not (`F-QA-INTEG-050-004`).                                                                                                                                                                           |
| Ambiguous update/delete observation                            | **PASS**       | Each recovery action adds exactly one `fetchAdminNotice` GET and leaves PUT/DELETE at one. A successful edit GET unlocks deliberate editing without claiming the prior result.                                                                                |
| Ambiguous create observation                                   | **FAIL**       | The supplied list link is observation-shaped, but unrelated navigation/remount clears the local fence with zero GET (`F-QA-INTEG-050-001`).                                                                                                                   |
| Direct detail/download route retirement                        | **PASS**       | Retired detail and attachment signals abort; late bytes cause zero `triggerBlobDownload` calls.                                                                                                                                                               |
| Busy Modal close/Escape/backdrop/recovery                      | **PASS**       | Original enabled-inert close defect is closed.                                                                                                                                                                                                                |
| Busy Modal keyboard containment with all actions disabled      | **FAIL**       | Real Notice delete has zero focusable descendants and Tab is not trapped (`F-QA-INTEG-050-005`).                                                                                                                                                              |
| Public 404, 5xx, valid-to-invalid, unmount                     | **PASS**       | 404 has no retry, 503 has retry and one GET, invalid transition adds zero GET, unmount aborts current read.                                                                                                                                                   |
| Destination GET failure                                        | **PARTIAL**    | Update success issues one PUT; failed detail GET plus retry produces two GETs and no repeated mutation. Create/delete destination schedules are absent.                                                                                                       |
| ADMIN non-counting read and public counting read               | **PASS**       | ADMIN projection query avoids entity mutation; public service test verifies one `incrementViewCount`; controller tests prove anonymous 401, USER 403, ADMIN 200.                                                                                              |
| Schema/dependency/endpoint/attachment/storage boundary         | **PASS**       | No schema, migration, Gradle, package manifest, lockfile, or attachment-limit file changed. The overall WI diff contains only the approved ADMIN projection endpoint, not an additional remediation endpoint. PRIVATE storage/security regression tests pass. |

## Independent Commands and Results

1. `npm test -- --run src/pages/public/NoticeDetailPage.test.tsx src/pages/admin/NoticeAdminPages.test.tsx src/api/notices.test.ts src/components/ui/Modal.test.tsx`
   - **PASS:** 4 files, 32 tests, 32 passed.
2. `npm test -- --run src/api/domainApis.test.ts src/test/coverage/adminSubscriberPages.coverage.test.tsx src/test/coverage/publicAuthShell.coverage.test.tsx`
   - **PASS:** 3 files, 82 tests, 82 passed.
3. `npm test -- --run src/api/loadError.test.ts`
   - **PASS:** 1 file, 9 tests, 9 passed.
4. `.\gradlew.bat test --rerun-tasks --tests "com.atstudio.atstudio.service.NoticeServiceTest" --tests "com.atstudio.atstudio.controller.NoticeControllerTest" --tests "com.atstudio.atstudio.service.storage.LocalStorageServiceTest"`
   - **PASS:** `BUILD SUCCESSFUL`; 48 tests, 47 passed, 0 failures/errors,
     1 environment-conditional symlink test skipped. All five Gradle tasks executed.
5. `npm run typecheck`
   - **PASS:** TypeScript no-emit check exited 0.
6. `npm run lint`
   - **PASS:** full frontend ESLint exited 0 with zero warnings/errors.
7. `npx prettier --check <18 changed Notice/Modal/hook/API/test files>`
   - **PASS:** all matched files use Prettier style.
8. `python .agents/skills/validate-docs/scripts/validate_docs.py`
   - **PASS:** Tier 0, links, 585 traceability IDs, and document index passed.
9. `git diff --check -- . ':(exclude)output/**'`
   - **PASS:** exit 0; only existing CRLF-to-LF working-copy warnings.
10. `git diff --name-only -- build.gradle settings.gradle frontend/package.json frontend/package-lock.json src/main/resources ':(glob)**/*schema*' ':(glob)**/*migration*'`
    - **PASS:** no output.

Source inspection also used exact numbered reads with `rg -n "^"` for the
handoffs, Notice pages/tests/API, Modal/tests, guard hook, backend Notice paths,
and relevant docs. The installed `useBeforeUnload` implementation was confirmed
with `rg -uuu -n "function useBeforeUnload|useBeforeUnload" frontend/node_modules/react-router-dom/dist/react-router-dom.development.js`.

## Verdict

**FAIL**

`F-QA-INTEG-050-001` remains open at P2, and two new P2 findings were identified.
The handoff permits PASS only when every P0-P2 finding is closed. Passing tests
therefore do not authorize WI-050 finalization.

## Residual Deferrals and Execution Boundary

- WI-055 remains responsible for broader binary/filename/download-helper work.
- WI-059 remains responsible for public catalog keyboard/headings/fallback work.
- WI-066 remains responsible for canonical attachment type/count/byte policy.
- WI-070 remains responsible for broad creator/ADMIN page coverage.
- No actual browser mutation, live DB/storage/file/download, retained external
  effect, secret/protected-output access, staging, commit, push, branch action,
  deployment, schema change, or dependency installation was performed.
