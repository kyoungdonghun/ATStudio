# Independent Final QA Integration Review: WI-20260809-ATS-050

## Findings

### F-QA-INTEG-050-001 - P2 - OPEN - Pending logout bypasses the Notice mutation owner

**Contract:** One owned Notice mutation must prevent every in-app departure from
discarding its owner before the result is authoritative. A blocked transition
must be reset rather than resumed, and no surrounding control may perform its
departure side effect before the blocker decides.

**Pointers:**

- `frontend/src/layouts/AdminLayout.tsx:44-58,109-119`
- `frontend/src/store/authStore.ts:141-159`
- `frontend/src/router/ProtectedRoute.tsx:29-41,54-60`
- `frontend/src/pages/admin/NoticeEditPage.tsx:70-87,251-308,311-357`
- `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx:1228-1249`
- `docs/ui/screen-flow.md:119-123`
- `docs/design/usecase/user-notice.md:159-161`

**Reproducible schedule and exact calls:**

1. Mount the real protected Admin layout at `/admin/notices/9/edit`; let the
   ADMIN projection GET complete once.
2. Start Save or Notice deletion and leave its promise pending. The Notice
   mutation invocation count is one, and the route blocker owns the attempted
   navigation.
3. Select the enabled Admin-layout Logout button. `handleLogout` invokes
   `logout()` once before it invokes `navigate('/', { replace: true })` once.
   `useBlocker` can block the navigation, but it cannot undo the already-started
   logout operation.
4. When logout settles, `clearSession()` changes the role to `GUEST`.
   `ProtectedRoute` stops rendering `AdminLayout` and the Notice page, so the
   accepted mutation owner is unmounted even though the route transition was
   supposed to remain blocked.
5. The accepted PUT/DELETE is deliberately not aborted and may settle after the
   owner is gone. No terminal Notice result can be presented in that component.

The shell test independently confirms that Logout and navigation are both
invoked unconditionally, but the Notice tests mount the page without
`AdminLayout` or `ProtectedRoute`; no test combines pending Notice ownership
with Logout. R2 closes ordinary sidebar, history, target-change, and direct
remount schedules, but it does not close the original every-departure contract.

**Required remediation:** Propagate the active mutation boundary to the Admin
shell or otherwise delay/disable Logout until the Notice owner reaches a
terminal result. Add a protected-router integration test that proves Logout has
zero auth/session/navigation effects while pending and becomes available after
success, authoritative rejection, or ambiguous settlement.

### F-QA-INTEG-050-006 - P2 - NEW - Access-token rotation invalidates edit mutation settlement

**Contract:** A same-session token refresh must not deadlock authoritative
success, discard ambiguous recovery, or leave `beforeunload` registration out
of sync with actual mutation ownership. Success must navigate, and an unknown
outcome must remain fenced behind an observation-only GET.

**Pointers:**

- `frontend/src/pages/admin/NoticeEditPage.tsx:45-50,89-97,251-308,311-357`
- `frontend/src/api/notices.ts:72-95`
- `frontend/src/api/client.ts:102-118,133-150`
- `frontend/src/hooks/usePendingMutationGuard.ts:8-27`
- `frontend/src/api/client.test.ts:294-315`
- `frontend/src/pages/admin/NoticeAdminPages.test.tsx:117-123,475-542,597-696`

**Reproducible schedule and exact calls:**

1. Complete one `fetchAdminNotice` GET under access token A, then invoke one
   attachment-bearing `updateNotice` PUT or one `deleteNotice` DELETE.
2. Return HTTP 401 for that wire request. Notice mutations do not set
   `skipAuthReplay`, so the interceptor sends one `/api/auth/refresh` POST,
   stores token B, updates Zustand once, and replays the original mutation once.
   The application-level mutation call count is one; the wire-level PUT/DELETE
   count is two (401 plus replay).
3. Token B changes `ownerKey` and `readKey`. The edit page retires the token-A
   projection and starts a second ADMIN projection GET while the token-A
   `MutationOperation` is still pending.
4. If the replay succeeds, `isCurrentProjection(operationKey)` is false. The
   success branch issues zero destination navigations. `finally` clears
   `operationRef` but does not clear React `operation`, leaving the page in
   permanent `saving`/`deleting` busy UI.
5. If the replay commits and its response is lost, the same false owner check
   also issues zero `setOutcomeUnknown` transitions and exposes no observation
   control. `operationRef` is null while React `operation` stays non-null, so
   the `beforeunload` listener remains registered but its predicate no longer
   protects the page.

`client.test.ts` proves that refresh stores the new access token and replays the
protected request. The Notice page tests hold `authState.accessToken` constant,
so all green Notice schedules bypass this cross-layer transition.

**Required remediation:** Keep an accepted mutation owned across a same-user
token rotation, or make the Notice mutation's auth-replay policy explicitly
authoritative. Terminal handling must clear ref and React pending state
together, navigate on authoritative success, and enter observation-only
recovery on an ambiguous replay. Add integrated token-A -> 401 -> token-B
success and response-loss tests with exact HTTP, API, GET, navigation, and
listener counts for both update and delete.

### F-QA-INTEG-050-007 - P3 - NEW - Remove-only storage failure retains a successfully observed create fence

**Contract:** A successful current Notice-list GET clears the create observation
fence, while storage exceptions remain non-crashing and conservative.

**Pointers:**

- `frontend/src/utils/noticeCreateObservationFence.ts:6-19`
- `frontend/src/utils/safeStorage.ts:41-63`
- `frontend/src/pages/public/NoticeListPage.tsx:38-68`
- `frontend/src/pages/admin/NoticeAdminPages.test.tsx:286-390`

**Reproducible schedule and exact calls:**

1. Let `setItem` succeed for one ambiguous create, producing one POST and a
   stored value of `1`.
2. Let one current Notice-list GET succeed, but make only
   `sessionStorage.removeItem` throw. The wrapper swallows the exception.
3. `clearNoticeCreateObservation` sets the memory boolean to false, but the
   stored `1` remains. On create-page remount, `getItem` succeeds and restores
   the fence, so the successful observation unlocks zero later POSTs.

This is fail-closed and does not permit a duplicate or expose data, so it is P3.
The storage test makes `getItem`, `setItem`, and `removeItem` all throw together;
that causes the post-clear `getItem` to return null and does not exercise the
remove-only schedule.

## Prior Finding Closure

| Finding | Priority | Final status | Independent result |
| --- | --- | --- | --- |
| `F-QA-INTEG-050-001` | P2 | **OPEN** | Ambiguous create now survives unrelated routes/remounts and unlocks only after a successful current list GET; ordinary pending links/history are blocked. Admin Logout still performs its auth side effect before the blocked navigation and unmounts the owner. |
| `F-QA-INTEG-050-002` | P2 | **CLOSED** | Busy Modal uses one contract: native disabled close, `aria-busy`, and suppressed Escape/backdrop. Authoritative failure restores normal close behavior. |
| `F-QA-INTEG-050-003` | P3 | **CLOSED** | Public 5xx, valid-to-invalid, download unmount, blocked-transition-before-4xx/ambiguity, and create/update/delete destination GET failure/retry schedules now have exact call assertions. |
| `F-QA-INTEG-050-004` | P2 | **CLOSED** | Under stable ownership, idle registers zero listeners; pending registers one; success, 4xx, ambiguity, and unmount remove it. `F-QA-INTEG-050-006` records a separate token-rotation state/ref divergence. |
| `F-QA-INTEG-050-005` | P2 | **CLOSED** | The real all-disabled Notice delete dialog prevents Tab and Shift+Tab and retains focus on the dialog; failure restores the ordinary focus cycle. |

No P0 or P1 finding was identified. One prior P2 remains open, one new P2 was
identified, and one new P3 availability finding remains.

## Mandatory Attack Matrix

| Attack case | Result | Independent evidence |
| --- | --- | --- |
| Ambiguous create -> unrelated route -> remount -> submit | **PASS** | One POST, zero unlock through unrelated route, failed GET, and cancelled GET; a current successful list GET unlocks one later deliberate POST. No title/content/file/user value is stored. |
| Session-scoped fence and storage exceptions | **PARTIAL** | Session storage plus in-memory fallback survives get/set failure without a crash or safety weakening. Remove-only failure retains the fence after successful observation (`F-QA-INTEG-050-007`, P3). |
| Public list without a fence; absent clear; stale response | **PASS** | Public loading remains unchanged; absent removal is guarded. Request IDs and effect cleanup prevent a retired list response from invoking clear. |
| Pending owner across idle, success, 4xx, ambiguity, unmount | **FAIL** | Stable-owner listener counts pass, but token rotation leaves React pending true after `operationRef` is cleared (`F-QA-INTEG-050-006`). |
| Same-tick route blocking; success navigation; blocked reset | **FAIL** | Ref-based route blocking and reset pass for tested links/history. Token-rotation success issues zero destination navigation, and Logout performs its side effect before the blocker (`F-QA-INTEG-050-001`, `-006`). |
| Busy Modal with zero enabled descendants | **PASS** | Tab and Shift+Tab remain on the dialog; close, Escape, and backdrop are suppressed; authoritative failure restores non-busy behavior. Existing non-busy focus tests pass. |
| Create/update/delete destination GET failure and retry | **PASS** | Successful POST/PUT/DELETE counts stay one; destination failure plus retry issues exactly two GETs and no repeated mutation in all three focused schedules. |
| Public detail/download ownership | **PASS** | 404 and 5xx remain distinct; route switch/unmount retire detail and download work; duplicate same-file invocation is fenced and another attachment stays independent. No browser download effect runs for retired bytes. |
| ADMIN projection and public view count | **PASS** | ADMIN-only projection uses one scalar projection query and zero public entity/view-count paths; public detail increments once per API invocation. Anonymous 401, USER 403, and ADMIN 200 pass. |
| WI-039 PRIVATE/safe download boundary | **PASS** | PRIVATE store/load/delete and parent-child lookup remain intact; safe octet-stream and fixed response-header tests pass. |
| Schema/dependency/endpoint/policy boundary | **PASS** | No schema, migration, dependency manifest, lockfile, or resource config changed. The only new endpoint is the approved ADMIN projection and its explicit security matcher. No attachment-policy expansion was introduced. |

## Independent Commands and Results

1. `npm test -- --run src/pages/public/NoticeDetailPage.test.tsx src/pages/admin/NoticeAdminPages.test.tsx src/api/notices.test.ts src/components/ui/Modal.test.tsx`
   - **PASS:** 4 files, 37 tests, 37 passed.
2. `npm test -- --run src/api/domainApis.test.ts src/api/loadError.test.ts src/test/coverage/adminSubscriberPages.coverage.test.tsx src/test/coverage/publicAuthShell.coverage.test.tsx`
   - **PASS:** 4 files, 91 tests, 91 passed.
3. `npm test -- --run src/api/client.test.ts src/store/authStore.test.ts src/router/ProtectedRoute.test.tsx`
   - **PASS:** 3 files, 44 tests, 44 passed. These tests confirm token rotation/replay, session clearing, and protected redirects independently, but do not compose them with a pending Notice mutation.
4. `.\gradlew.bat test --rerun-tasks --tests "com.atstudio.atstudio.service.NoticeServiceTest" --tests "com.atstudio.atstudio.controller.NoticeControllerTest" --tests "com.atstudio.atstudio.service.storage.LocalStorageServiceTest"`
   - **PASS:** `BUILD SUCCESSFUL`; 48 tests, 47 passed, 0 failures/errors, 1 environment-conditional symlink test skipped; all 5 Gradle tasks executed.
5. `npm run typecheck`
   - **PASS:** `tsc --noEmit` exited 0.
6. `npm run lint`
   - **PASS:** full frontend ESLint exited 0 with `--max-warnings 0`.
7. `npx prettier --check src/api/notices.ts src/api/loadError.ts src/api/notices.test.ts src/api/domainApis.test.ts src/components/ui/Modal.tsx src/components/ui/Modal.module.css src/components/ui/Modal.test.tsx src/hooks/usePendingMutationGuard.ts src/utils/noticeCreateObservationFence.ts src/pages/public/NoticeListPage.tsx src/pages/public/NoticeDetailPage.tsx src/pages/public/NoticeDetailPage.module.css src/pages/public/NoticeDetailPage.test.tsx src/pages/admin/NoticeCreatePage.tsx src/pages/admin/NoticeCreatePage.module.css src/pages/admin/NoticeEditPage.tsx src/pages/admin/NoticeEditPage.module.css src/pages/admin/NoticeAdminPages.test.tsx src/test/coverage/adminSubscriberPages.coverage.test.tsx src/test/coverage/publicAuthShell.coverage.test.tsx`
   - **PASS:** all 20 scoped files use Prettier style.
8. `npm run build`
   - **PASS:** `tsc -b && vite build`; 291 modules transformed; production bundle built successfully.
9. `python .agents/skills/validate-docs/scripts/validate_docs.py`
   - **PASS:** Tier 0, internal links, 585 supported traceability IDs, and document index all passed.
10. `git diff --check -- . ':(exclude)output/**'`
    - **PASS:** exit 0; only CRLF-to-LF working-copy warnings were emitted.
11. `git diff --name-only -- build.gradle settings.gradle frontend/package.json frontend/package-lock.json src/main/resources ':(glob)**/*schema*' ':(glob)**/*migration*'`
    - **PASS:** no output.
12. `git diff -U0 -- src/main/java/com/atstudio/atstudio/controller/NoticeController.java src/main/java/com/atstudio/atstudio/config/SecurityConfig.java | rg "^\+.*(@(Get|Post|Put|Delete|Patch)Mapping|requestMatchers)"`
    - **PASS:** only `GET /api/notices/{noticeId}/admin` and its ADMIN matcher were added.
13. `rg -n -C 3 "function handleLogout|logout\(\)|navigate\('/', \{ replace: true \}\)|accessToken =|createOwnerKey|operationKey = readKey|const isCurrent =|setOperation\(null\)|setOutcomeUnknown|newAccessToken|return client\(originalRequest\)|skipAuthReplay" <scoped frontend files>`
    - **FAIL (attack review):** confirmed the two P2 cross-layer schedules above; no existing composed test closes them.

All test execution used Vitest/jsdom mocks, mocked API clients, MockMvc, the test
application context, H2/test infrastructure, and temporary test storage only.
No actual browser mutation, live database/storage/file/download, retained
external effect, secret/protected-output access, Git mutation, branch action,
deployment, schema change, or dependency installation was performed.

## Verdict

**FAIL**

PASS is allowed only with zero open or new P0-P2 findings.
`F-QA-INTEG-050-001` remains open at P2 and
`F-QA-INTEG-050-006` is a new P2. Passing automated gates therefore do not
authorize WI-050 finalization, full gates, commit, or push.

## Residual Risks and Intentional Deferrals

- `F-QA-INTEG-050-007` is a P3 fail-closed availability issue for a remove-only
  session-storage failure.
- WI-055 remains responsible for broader binary/filename/download-helper work.
- WI-059 remains responsible for public catalog keyboard/headings/fallback work.
- WI-066 remains responsible for canonical Notice/Question attachment type,
  count, and byte policy.
- WI-070 remains responsible for broader creator/ADMIN page coverage.
- No live ADMIN browser, production runtime, retained database row, retained
  storage object, or real download was inspected, so no live or production
  acceptance is claimed.
