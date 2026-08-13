# Conclusive Independent QA-INTEG Review Result: WI-20260809-ATS-050

## Findings

### F-QA-INTEG-050-008 - P2 - CLOSED - Mandatory executable verification now passes in this runtime

**Contract:** The conclusive handoff requires independent mock/test-safe execution for every mandatory WI-050 schedule and permits PASS only with zero open/new P0-P2.

**Pointers:**

- `deliverables/agent/WI-20260809-ATS-050-qa-conclusive-review-handoff.md:15-26`
- `deliverables/agent/WI-20260809-ATS-050-qa-conclusive-review-handoff.md:28-32`
- `deliverables/agent/WI-20260809-ATS-050-qa-conclusive-review-handoff.md:43-46`
- `frontend/src/pages/admin/NoticeAdminShellIntegration.test.tsx:165-260`
- `frontend/src/pages/admin/NoticeAdminShellIntegration.test.tsx:262-421`
- `frontend/src/pages/admin/NoticeAdminPages.test.tsx:131-453`
- `frontend/src/pages/admin/NoticeAdminPages.test.tsx:537-758`
- `frontend/src/pages/public/NoticeDetailPage.test.tsx:65-204`
- `frontend/src/pages/public/NoticeListPage.test.tsx:38-67`
- `frontend/src/api/notices.test.ts`
- `frontend/src/components/ui/Modal.test.tsx`
- `frontend/src/utils/noticeCreateObservationFence.test.ts`
- `frontend/src/utils/safeStorage.test.ts`
- `frontend/vite.config.ts:141-143`
- `src/test/java/com/atstudio/atstudio/service/NoticeServiceTest.java:360-412`
- `src/test/java/com/atstudio/atstudio/controller/NoticeControllerTest.java:30-36`
- `src/test/java/com/atstudio/atstudio/controller/NoticeControllerTest.java:163-270`

**Composed schedules and exact call counts required by existing tests:**

- Pending real `ProtectedRoute + AdminLayout + NoticeEditPage` save: one application PUT, one wire PUT, one ADMIN GET, zero Logout calls, zero logout POSTs, zero router navigations while pending; after success/4xx/ambiguous settlement, exactly one beforeunload removal and Logout enabled (`NoticeAdminShellIntegration.test.tsx:191-258`).
- Same ADMIN user token A -> 401 -> refresh -> token B -> replay for update/delete: one application PUT or DELETE, two wire mutations, one refresh POST, one ADMIN GET before terminal success, exactly one success navigation, or two ADMIN GETs for response-loss observation with no repeated mutation (`NoticeAdminShellIntegration.test.tsx:262-369`).
- Different authenticated ADMIN user while mutation is pending: one application PUT, one wire PUT, two ADMIN GETs, zero navigations after stale success (`NoticeAdminShellIntegration.test.tsx:372-420`).
- Create observation fence: one POST until successful current list GET; no title/content/file value stored; remove-only session-storage failure keeps the fence and a later successful list observation clears it (`NoticeAdminPages.test.tsx:287-453`).
- Backend ADMIN projection and safe attachment download: ADMIN projection uses `findAdminEditRowsById` and never calls public `findById`/attachment public detail paths; public detail increments once; attachment response keeps octet-stream and safe headers (`NoticeServiceTest.java:360-412`, `NoticeControllerTest.java:163-270`).

**Actual independent execution result in this run:**

- Frontend mandatory Vitest composition executed under configured `jsdom` (`vite.config.ts:141-143`) and passed: 8 test files, 53 tests, 0 failures, duration 7.00s, exit 0.
- Backend focused Gradle command first exited 0 with `:test UP-TO-DATE`; the same focused test set was then rerun with `--rerun-tasks` as narrow adjacent verification to force execution.
- Backend forced rerun passed with MockMvc/H2/test infrastructure: `BUILD SUCCESSFUL in 52s`, `:test` executed, H2 in-memory JDBC URL observed in logs (`jdbc:h2:mem:609b0215-b17e-4e31-998c-8b33e5e83806`), no live DB evidence.
- Backend XML result headers show: `NoticeControllerTest` 18 tests, 0 failures, 0 errors, 0 skipped; `NoticeServiceTest` 19 tests, 0 failures, 0 errors, 0 skipped; `LocalStorageServiceTest` 11 tests, 0 failures, 0 errors, 1 skipped. Total: 48 tests discovered, 47 passed, 1 skipped, 0 failed/errors.

F-QA-INTEG-050-008 is closed by actual execution evidence. It is no longer a QA gate blocker.

### F-QA-INTEG-050-009 - P3 - OPEN - Real AdminLayout create Logout composition is source-verified but not separately composed in tests

**Contract:** The conclusive mandatory case names real `ProtectedRoute + AdminLayout + create/edit`.

**Pointers:**

- `deliverables/agent/WI-20260809-ATS-050-qa-conclusive-review-handoff.md:19`
- `frontend/src/pages/admin/NoticeAdminShellIntegration.test.tsx:10-13`
- `frontend/src/pages/admin/NoticeAdminShellIntegration.test.tsx:90-128`
- `frontend/src/pages/admin/NoticeAdminShellIntegration.test.tsx:165-260`
- `frontend/src/pages/admin/NoticeCreatePage.tsx:99-118`
- `frontend/src/layouts/AdminLayout.tsx:51-70`
- `frontend/src/layouts/AdminLayout.tsx:86-89`
- `frontend/src/layouts/AdminLayout.tsx:149-153`

**Schedule and call counts:**

- Existing real shell test composes `AdminLayout`, `ProtectedRoute`, and `NoticeEditPage` only. It imports `NoticeEditPage`, not `NoticeCreatePage`, and its router children contain `/admin/notices/:noticeId/edit`.
- Source inspection shows create acquires the same boundary synchronously before `createNotice` (`operationRef.current = operation`, `adminMutationBoundary.acquire(operation)`, then API call) and AdminLayout guards forced Logout through the boundary ref before `logout()` and `navigate('/')`.
- Existing isolated create tests prove one POST, blocked history, no AbortSignal, one beforeunload add/remove, and no repeat after ambiguous create until observation, but they do not give a separate real shell create Logout call-count schedule.

This remains an evidence gap only. It does not show a second POST, Logout side effect, or wrong navigation in implementation.

## Prior-Finding Closure Table

| Finding | Priority | Status | Independent result |
| --- | --- | --- | --- |
| `F-QA-INTEG-050-001` | P2 | **CLOSED BY EXECUTION** | AdminLayout owns a synchronous Set-based mutation boundary (`AdminLayout.tsx:51-70`). Forced Logout returns before `logout()` and `navigate()` while any owner exists (`AdminLayout.tsx:86-89`), and the button is natively disabled (`AdminLayout.tsx:149-153`). Notice create/edit acquire before API calls (`NoticeCreatePage.tsx:99-108`, `NoticeEditPage.tsx:282-301`, `NoticeEditPage.tsx:339-351`) and release only the exact owner in `finally` (`NoticeCreatePage.tsx:130-136`, `NoticeEditPage.tsx:317-323`, `NoticeEditPage.tsx:368-374`). Mandatory Vitest command passed 53/53 tests. |
| `F-QA-INTEG-050-006` | P2 | **CLOSED BY EXECUTION** | Notice edit owner key is `userID + role`, not access token (`NoticeEditPage.tsx:42-62`), so same-user token A -> B replay does not retire the projection. Success clears ref/state and navigates exactly once (`NoticeEditPage.tsx:302-307`, `NoticeEditPage.tsx:352-356`); ambiguous replay sets observation-only recovery (`NoticeEditPage.tsx:310-312`, `NoticeEditPage.tsx:360-363`). Client interceptor refreshes once and replays the original request (`client.ts:102-149`). Mandatory Vitest command passed 53/53 tests. |
| `F-QA-INTEG-050-007` | P3 | **CLOSED BY EXECUTION** | `safeSessionStorage.removeItem` returns boolean (`safeStorage.ts:57-63`). `clearNoticeCreateObservation` clears memory only when no stored fence may exist or removal succeeds (`noticeCreateObservationFence.ts:22-32`). Focused source and execution cover remove-only failure then later successful clear (`noticeCreateObservationFence.test.ts:21-38`) and route-level create/list recovery (`NoticeAdminPages.test.tsx:394-453`). |
| `F-QA-INTEG-050-002` | P2 | **CLOSED BY EXECUTION** | Busy Modal disables header close, exposes `aria-busy`, suppresses close through `requestClose`, and retains focus on the dialog when there are zero focusable descendants (`Modal.tsx:36-58`, `Modal.tsx:126-155`). Notice delete test source asserts one DELETE, disabled close/cancel/confirm, Tab and Shift+Tab trapped, Escape/backdrop suppressed, and recovery restores close/focus (`NoticeAdminPages.test.tsx:606-657`). Mandatory Vitest command passed. |
| `F-QA-INTEG-050-003` | P3 | **CLOSED BY EXECUTION** | Public 404/5xx/route/download schedules and destination GET-only retries remain covered in source tests with exact read/mutation counts (`NoticeDetailPage.test.tsx:65-204`, `NoticeAdminPages.test.tsx:455-499`, `NoticeAdminPages.test.tsx:701-758`). Mandatory Vitest and focused Gradle commands passed. |
| `F-QA-INTEG-050-004` | P2 | **CLOSED BY EXECUTION** | `usePendingMutationGuard` installs `beforeunload` only while `pending` is true and removes it on settlement/unmount (`usePendingMutationGuard.ts:12-23`). Existing create/shell tests assert idle zero listener, pending one add, and terminal one remove (`NoticeAdminPages.test.tsx:251-284`, `NoticeAdminShellIntegration.test.tsx:172-258`). Mandatory Vitest command passed. |
| `F-QA-INTEG-050-005` | P2 | **CLOSED BY EXECUTION** | Real all-disabled Notice delete Modal traps focus on the dialog while busy and restores normal focus cycle after failure (`Modal.tsx:43-58`, `NoticeAdminPages.test.tsx:606-657`). Mandatory Vitest command passed. |

## Mandatory Case Matrix

| Mandatory case | Result | Evidence |
| --- | --- | --- |
| Real ProtectedRoute + AdminLayout + create/edit Logout and owner release | **PASS WITH P3 EVIDENCE GAP** | Edit is composed in `NoticeAdminShellIntegration.test.tsx:165-260` and passed in the mandatory Vitest run. Create uses the same boundary by source (`NoticeCreatePage.tsx:99-118`) and isolated create tests passed, but a separate real shell create composition remains open as P3 (`F-QA-INTEG-050-009`). |
| Simultaneous/serial owner safety and unrelated release | **PASS** | Admin boundary releases only owners present in the Set (`AdminLayout.tsx:56-68`). One owner cannot delete a different owner. Mounted guard prevents setState after layout unmount (`AdminLayout.tsx:74-79`). Mandatory Vitest command passed. |
| Same ADMIN token A -> 401 -> refresh -> B -> replay update/delete | **PASS** | One app-level client call and two wire mutations are asserted by test source (`NoticeAdminShellIntegration.test.tsx:310-358`) and passed. Owner key excludes token (`NoticeEditPage.tsx:42-62`); interceptor refresh/replay path remains unchanged (`client.ts:102-149`). |
| Response-loss observation and bounded 4xx retry | **PASS** | 4xx sets action/delete error without recovery fence (`NoticeEditPage.tsx:313-315`, `NoticeEditPage.tsx:364-366`); response loss sets `outcomeUnknown` and requires observation (`NoticeEditPage.tsx:310-312`, `NoticeEditPage.tsx:360-363`). Existing tests assert no repeated PUT/DELETE before observation (`NoticeAdminPages.test.tsx:582-604`, `NoticeAdminPages.test.tsx:680-699`) and passed. |
| Different user, role, or Notice target stale retirement | **PASS** | Owner key includes user and role (`NoticeEditPage.tsx:42-62`); read key includes target (`NoticeEditPage.tsx:61-62`). Test source covers different ADMIN user and target switch with zero stale navigation (`NoticeAdminShellIntegration.test.tsx:372-420`, `NoticeAdminPages.test.tsx:504-535`, `NoticeAdminPages.test.tsx:659-678`) and passed. |
| `beforeunload`, page blocker, Admin boundary ownership | **PASS** | Blocker predicate is `operationRef.current !== null` and reset is explicit (`usePendingMutationGuard.ts:8-28`). Create/edit clear operation ref before resetting blocked navigation on success and in `finally` (`NoticeCreatePage.tsx:114-136`, `NoticeEditPage.tsx:302-323`, `NoticeEditPage.tsx:352-374`). Mandatory Vitest command passed. |
| Observation fence set/get/remove permutations and public list stale behavior | **PASS** | Storage stores only a fixed key/value and never user/Notice/file data (`noticeCreateObservationFence.ts:3-18`; test source `NoticeAdminPages.test.tsx:317-322`). Remove-only failure remains fail-closed until later clear (`noticeCreateObservationFence.ts:22-32`). Public list clears only current responses and retires stale effects by request ID (`NoticeListPage.tsx:38-67`). Mandatory Vitest command passed. |
| Previously closed public 404/5xx/route/download, Modal, ADMIN read, public count, WI-039 safe headers, destination GET recovery | **PASS** | Public/detail/list/Modal tests passed in Vitest. Backend focused rerun passed 48 discovered tests with 0 failures/errors; source keeps ADMIN projection separate from public view count and safe attachment headers (`NoticeService.java:86-117`, `NoticeController.java:97-113`). |
| No schema/dependency/extra endpoint/attachment-policy/live-effect expansion | **PASS BY DIFF INSPECTION** | Dependency/schema diff command returned no files. Endpoint diff shows only the approved `GET /api/notices/{noticeId}/admin` and ADMIN matcher. Attachment-policy grep showed only projection/metadata references, no new count/type/byte policy. |

## Independent Commands and Results

1. `npm test -- --run src/pages/admin/NoticeAdminShellIntegration.test.tsx src/pages/admin/NoticeAdminPages.test.tsx src/pages/public/NoticeDetailPage.test.tsx src/pages/public/NoticeListPage.test.tsx src/api/notices.test.ts src/components/ui/Modal.test.tsx src/utils/noticeCreateObservationFence.test.ts src/utils/safeStorage.test.ts`
   - **PASS:** Vitest exited 0. Output: `Test Files 8 passed (8)`, `Tests 53 passed (53)`, `Duration 7.00s`. Config uses `environment: 'jsdom'` and `setupFiles: './src/test/setup.ts'` (`frontend/vite.config.ts:141-143`).
2. `.\gradlew.bat test --tests "com.atstudio.atstudio.service.NoticeServiceTest" --tests "com.atstudio.atstudio.controller.NoticeControllerTest" --tests "com.atstudio.atstudio.service.storage.LocalStorageServiceTest"`
   - **PASS / CACHED:** Gradle exited 0 with `BUILD SUCCESSFUL in 3s`; `:test UP-TO-DATE`. This proves the exact focused command is runnable in the current runtime, but not fresh execution.
3. `.\gradlew.bat test --rerun-tasks --tests "com.atstudio.atstudio.service.NoticeServiceTest" --tests "com.atstudio.atstudio.controller.NoticeControllerTest" --tests "com.atstudio.atstudio.service.storage.LocalStorageServiceTest"`
   - **PASS:** Gradle exited 0 with `BUILD SUCCESSFUL in 52s`; `5 actionable tasks: 5 executed`; `:test` executed. Logs show H2 in-memory test infrastructure (`jdbc:h2:mem:609b0215-b17e-4e31-998c-8b33e5e83806`) and MockMvc is the controller test harness (`NoticeControllerTest.java:30-36`).
   - **Result headers:** `NoticeControllerTest`: 18 tests, 0 failures, 0 errors, 0 skipped. `NoticeServiceTest`: 19 tests, 0 failures, 0 errors, 0 skipped. `LocalStorageServiceTest`: 11 tests, 0 failures, 0 errors, 1 skipped.
4. `Get-ChildItem -Path "build\test-results\test" -Filter "TEST-com.atstudio.atstudio.*.xml" | ...`
   - **PASS:** Narrow result-header extraction confirmed the three focused Gradle result files above. XML DOM parsing was not used for the two Korean-display-name suites because their report headers contain mojibake; regex extraction from the `<testsuite ...>` header succeeded.
5. `git diff --name-only -- build.gradle settings.gradle frontend/package.json frontend/package-lock.json src/main/resources ':(glob)**/*schema*' ':(glob)**/*migration*'`
   - **PASS:** no output.
6. `git diff -U0 -- src/main/java/com/atstudio/atstudio/controller/NoticeController.java src/main/java/com/atstudio/atstudio/config/SecurityConfig.java | rg "^\+.*(@(Get|Post|Put|Delete|Patch)Mapping|requestMatchers)"`
   - **PASS:** output limited to `requestMatchers(HttpMethod.GET, "/api/notices/*/admin").hasRole("ADMIN")` and `@GetMapping("/{noticeId}/admin")`; Git emitted CRLF-to-LF working-copy warnings only.
7. `git diff -U0 -- frontend/src/api/notices.ts frontend/src/utils/validation.ts src/main/java/com/atstudio/atstudio/service/NoticeService.java src/main/java/com/atstudio/atstudio/controller/NoticeController.java | rg "^\+.*(ATTACHMENT_MAX|MAX_SIZE|max|attachment|attachments|@PostMapping|@PutMapping|@DeleteMapping|@GetMapping)"`
   - **PASS:** output was limited to the approved admin projection/attachment metadata lines and `@GetMapping("/{noticeId}/admin")`; no new attachment type/count/byte policy was found. Git emitted CRLF-to-LF working-copy warnings only.
8. `git diff --check -- . ':(exclude)output/**'`
   - **PASS:** exit 0; only CRLF-to-LF working-copy warnings were emitted.

## Residual Deferrals

- `F-QA-INTEG-050-009` remains a P3 evidence gap for a separate real AdminLayout create Logout composition; source inspection and passing isolated create tests support safety through the shared boundary.
- WI-055 remains responsible for broader binary/filename/download-helper normalization.
- WI-059 remains responsible for public catalog keyboard/headings/fallback work.
- WI-066 remains responsible for canonical Notice/Question attachment type, count, and byte policy.
- WI-070 remains responsible for broader creator/ADMIN page coverage.
- No real browser, live DB/storage/file/download, retained external effect, secret/protected-output content access, Git staging/commit/push/branch action, deploy, schema change, dependency install, or `output/**` content read/write was performed.

## Self-Check

- Intended source/deliverable write by this run: only `deliverables/agent/WI-20260809-ATS-050-qa-conclusive-review-result.md`.
- Test execution refreshed generated Gradle build/test artifacts under `build/`; these are test outputs, not workspace source/deliverable changes.
- Final verification compared `git status --short --untracked-files=all -- . ':(exclude)output/**'` against the pre-run dirty baseline. The listed source/deliverable paths remained the same; the conclusive result file remains the only source/deliverable path intentionally rewritten by this run.

## Verdict

**PASS**

Open/new P0-P2 count is **0**.

Open/new implementation P0-P2 count is **0**. Mandatory frontend Vitest and backend focused Gradle evidence now execute successfully in this unsandboxed mock/test-safe runtime. WI-050 is cleared by QA-INTEG for finalization, full gates, commit, and push, subject to the residual P3 evidence gap and the listed downstream deferrals.
