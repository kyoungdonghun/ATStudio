# Independent QA Integration Review: WI-20260809-ATS-050

## Verdict

**FAIL**

No P0 or P1 finding was identified. Two P2 findings block WI-050 completion. One
P3 evidence gap remains. The implementer's passing-test claim was independently
rerun but was not treated as authority.

## Findings

### F-QA-INTEG-050-001 - P2 - Pending and ambiguous Notice mutations can be abandoned and repeated

**Contract:** One owned operation must prevent save, Notice delete, file add or
remove, close/navigation, and duplicate submit from conflicting. A retry must
not repeat a mutation that may already have committed.

**Pointers:**

- `frontend/src/pages/admin/NoticeCreatePage.tsx:39-45,99-118,224-230`
- `frontend/src/pages/admin/NoticeEditPage.tsx:168-175,255-277,305-316,495-515`
- `frontend/src/api/notices.ts:57-95`
- `frontend/src/layouts/AdminLayout.tsx:72-83`
- `frontend/src/pages/admin/NoticeAdminPages.test.tsx:114-126,181-220,244-266`

**Reproducible counterexamples:**

1. Load `/admin/notices/9/edit`, add a file, select **Save**, then navigate with
   an enabled Admin sidebar link or browser history before the response settles.
   The route-switch test at lines 244-266 confirms that navigation is accepted
   and the PUT signal is aborted. Client abort does not prove that the server
   transaction or PRIVATE file operation did not commit. The page then discards
   the only operation owner and provides no outcome recovery.
2. Make `createNotice` or attachment-bearing `updateNotice` reject after a test
   server double records the mutation, modeling a committed response lost in
   transit. The catch path reports failure, clears the operation fence, and
   enables the same submit action. A second click issues a second POST/PUT; no
   idempotency key, read-before-retry, or committed-result lookup exists. Create
   can duplicate a Notice, and update can duplicate a newly added attachment.
3. The existing failure tests assert only one initial call and preserved input.
   They do not click the enabled manual retry after an ambiguous result, so they
   are green while the counterexample remains.

**Expected remediation:** Keep one mutation owner across every in-app departure
path and define bounded unknown-outcome recovery. Do not abort an accepted
mutation merely because the route unmounts unless non-commit is authoritative.
After a no-response/timeout outcome, recover through an observation-only read or
an idempotent attempt identity before enabling another POST/PUT/file mutation.
Add create, update-with-file, and delete tests that model commit followed by
response loss and prove mutation call count remains one.

### F-QA-INTEG-050-002 - P2 - The pending delete dialog exposes an enabled inert close control

**Contract:** Pending state and disabled controls must be programmatically clear,
while modal close, keyboard, and destructive action cannot conflict.

**Pointers:**

- `frontend/src/pages/admin/NoticeEditPage.tsx:519-546`
- `frontend/src/components/ui/Modal.tsx:26-32,109-138`
- `frontend/src/pages/admin/NoticeAdminPages.test.tsx:222-242`

**Reproducible counterexample:** Open the delete confirmation, start deletion,
and focus the header **Close** button. The button has no `disabled` or
`aria-disabled` state and remains keyboard/pointer actionable. Its callback is
silently ignored by `operationRef.current !== null`, so clicking it or pressing
Escape leaves the dialog open with no explanation. The existing test explicitly
clicks this control while deletion is pending and checks only that the dialog
stays open; it does not check that the control is programmatically disabled.

**Expected remediation:** Give the shared modal an explicit non-closable/busy
contract for owned operations. Disable or remove the close control from the
focus order, suppress Escape/backdrop through the same state, expose the busy
state accessibly, and test pointer plus keyboard behavior while pending and
after failure recovery.

### F-QA-INTEG-050-003 - P3 - Mandatory lifecycle proof is incomplete

**Contract:** Public invalid-route/unmount schedules, attachment unmount, and
post-mutation success followed by destination-read failure must be separately
proven with exact read and mutation counts.

**Pointers:**

- `frontend/src/pages/public/NoticeDetailPage.test.tsx:65-161`
- `frontend/src/pages/admin/NoticeAdminPages.test.tsx:80-266`

**Reproducible evidence gap:** The public suite proves 404, one network retry,
A-to-B retirement, load unmount, download duplicate/independence/retry, and
download route replacement. It does not exercise a valid route becoming
invalid, download completion after direct unmount, or 5xx separately. The Admin
suite does not mount the create/edit success destination and make its first read
fail, so it does not prove that recovery retries only GET and never the completed
POST/PUT/DELETE/file mutation.

**Expected remediation:** Add focused schedules for these missing cases and
assert exact Notice read, attachment read, browser-effect, and mutation call
counts. Keep this evidence separate from later WI-055 filename/binary work and
WI-070 broad page coverage.

## Mandatory Attack Matrix

| Attack case | Result | Independent evidence |
|---|---|---|
| Public 404 vs network/5xx, retry, A-to-B late completion, invalid route, unmount | **FAIL** | Source fences stale commits and current tests pass for 404/network/A-to-B/load-unmount, but 5xx, valid-to-invalid, and download-unmount schedules are not separately proven (`F-QA-INTEG-050-003`). |
| Invalid ADMIN IDs make zero Notice/attachment/mutation calls and provide safe navigation | **PASS** | Canonical parser rejects missing/non-decimal/non-positive/non-safe IDs before effect or action; parameterized tests prove `abc`, `0`, `-1`, `01`, and unsafe integer with zero API calls. |
| ADMIN read performs zero view-count write; public read increments once per API call | **PASS** | Dedicated ADMIN projection query does not load/mutate `Notice`; service tests verify no public repository path. Public service test verifies exactly one entity increment. |
| ADMIN-only read does not weaken public attachment authorization | **PASS** | Security matcher and method annotation are explicit; MockMvc proves anonymous 401, USER 403, ADMIN 200. Existing public attachment GET remains anonymous 200 with safe headers. |
| Create/edit mutation coordination and duplicate fence | **FAIL** | Local controls and same-tick duplicate submit are fenced, but global/history navigation retires the mutation owner and ambiguous manual retry can repeat POST/PUT/file effects (`F-QA-INTEG-050-001`). |
| Commit success followed by refresh failure retries read only | **FAIL** | Navigation targets have read-only retry behavior by source, but no cross-page proof exists and transport-level committed-but-lost mutation outcomes remain repeatable (`F-QA-INTEG-050-001`, `F-QA-INTEG-050-003`). |
| Attachment duplicate/independence/target/error/retry/bounds | **PASS** | Per-attachment operation map synchronously fences duplicates, permits another file, clears on target/unmount, ignores retired bytes, and keeps local fixed errors. Filename/binary canonicalization remains WI-055 scope. |
| Accessibility and localization | **FAIL** | Labels, fixed Korean errors, native disabled form controls, and retry buttons pass; the pending modal close control is enabled but inert (`F-QA-INTEG-050-002`). |
| No invented attachment policy; canonical content maximum | **PASS** | Existing 5-file/20 MiB client advisory behavior is unchanged; no new server type/count/byte policy was added. Client and server enforce the existing 1,000-character content maximum. WI-066 remains held. |
| WI-039 PRIVATE storage and safe response regression | **PASS** | Focused backend tests confirm PRIVATE create/delete/load and octet-stream, encoded disposition, no-store/private, no-cache, nosniff, CSP, and CORP headers. |
| Current-state documentation only; public vs ADMIN semantics | **PASS** | Updated docs distinguish the public counting read from ADMIN non-counting projection and retain the no-live/browser-acceptance boundary. Link, index, and traceability validation passed. |

## Independent Commands and Results

1. `npm test -- --run src/pages/public/NoticeDetailPage.test.tsx src/pages/admin/NoticeAdminPages.test.tsx src/api/notices.test.ts`
   - **PASS**: 3 files, 20 tests, 20 passed.
2. `.\gradlew.bat test --tests "com.atstudio.atstudio.service.NoticeServiceTest" --tests "com.atstudio.atstudio.controller.NoticeControllerTest" --tests "com.atstudio.atstudio.service.storage.LocalStorageServiceTest"`
   - **PASS**: 48 tests, 47 passed, 0 failed/errors, 1 environment-conditional symlink test skipped; `BUILD SUCCESSFUL`.
3. `npm test -- --run src/api/domainApis.test.ts src/test/coverage/adminSubscriberPages.coverage.test.tsx src/test/coverage/publicAuthShell.coverage.test.tsx`
   - **PASS**: 3 files, 82 tests, 82 passed.
4. `npm run typecheck`
   - **PASS**: TypeScript no-emit check exited 0.
5. `npx eslint src/api/notices.ts src/pages/public/NoticeDetailPage.tsx src/pages/admin/NoticeCreatePage.tsx src/pages/admin/NoticeEditPage.tsx src/api/notices.test.ts src/pages/public/NoticeDetailPage.test.tsx src/pages/admin/NoticeAdminPages.test.tsx src/api/domainApis.test.ts src/test/coverage/adminSubscriberPages.coverage.test.tsx src/test/coverage/publicAuthShell.coverage.test.tsx`
   - **PASS**: exited 0 with no warnings or errors.
6. `npx prettier --check src/api/notices.ts src/pages/public/NoticeDetailPage.tsx src/pages/public/NoticeDetailPage.module.css src/pages/admin/NoticeCreatePage.tsx src/pages/admin/NoticeCreatePage.module.css src/pages/admin/NoticeEditPage.tsx src/pages/admin/NoticeEditPage.module.css src/api/notices.test.ts src/pages/public/NoticeDetailPage.test.tsx src/pages/admin/NoticeAdminPages.test.tsx src/api/domainApis.test.ts src/test/coverage/adminSubscriberPages.coverage.test.tsx src/test/coverage/publicAuthShell.coverage.test.tsx`
   - **PASS**: all matched files use Prettier style.
7. `python .agents/skills/validate-docs/scripts/validate_docs.py`
   - **PASS**: Tier 0, internal links, 585 supported traceability IDs, and document index all passed.
8. `git diff --check -- . ':(exclude)output/**'`
   - **PASS**: exit 0; only existing CRLF-to-LF working-copy warnings were emitted.

All executions used mocks, MockMvc, the test application context, H2/test
infrastructure, and temporary test storage only. No real attachment download,
live database/storage mutation, retained file mutation, Provider/external effect,
secret inspection, protected output access, Git staging/commit/branch action, or
deployment occurred.

## Residual Risks and Intentional Deferrals

- WI-055 remains responsible for shared binary response, filename, byte, and
  download-helper normalization beyond this Notice-owned operation state.
- WI-059 remains responsible for public catalog keyboard/headings/fallback work.
- WI-066 remains responsible for canonical Notice/Question attachment type,
  count, and byte policy; this review does not invent those limits.
- WI-070 remains responsible for broader dedicated creator/ADMIN page coverage.
- No live ADMIN browser, production runtime, retained database row, or retained
  storage object was inspected, so no live/production acceptance is claimed.
