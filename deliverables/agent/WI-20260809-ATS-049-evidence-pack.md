---
version: 1.1
last_updated: 2026-08-13
project: ATS
owner: docops
category: evidence-pack
status: complete
dependencies:
  - path: WI-20260809-ATS-049-handoff.md
    reason: Approved work item and original output contract
  - path: WI-20260809-ATS-049-continuation-handoff.md
    reason: Interrupted implementation checkpoint and final gate contract
  - path: WI-20260809-ATS-049-qa-integ-review-result.md
    reason: Immutable independent FAIL verdict and four remediation findings
  - path: WI-20260809-ATS-049-remediation-handoff.md
    reason: Approved remediation scope and counterexample test contract
  - path: WI-20260809-ATS-049-qa-integ-rereview-result.md
    reason: Immutable independent R2 FAIL verdict and route-owner finding
  - path: WI-20260809-ATS-049-remediation-r2-handoff.md
    reason: Approved R2 route-switch and unmount remediation contract
  - path: WI-20260809-ATS-049-qa-final-review-result.md
    reason: Final independent PASS verdict and closure of all five QA findings
  - path: WI-20260809-ATS-049-finalization-handoff.md
    reason: Authoritative final full-gate metrics and documentation boundary
---

# Evidence Pack: WI-20260809-ATS-049

## Summary

Final independent QA is `PASS`: `QA-049-001` through `QA-049-004` and
`QA-049-R2-001` are `CLOSED`, with no new P0-P2. The focused Album suite passes
93 tests across 8 files, and the final frontend, backend, coverage, build,
formatting, documentation, and whitespace gates all pass. The two earlier QA
`FAIL` results remain unchanged as historical evidence.

Album ADMIN create/edit/manage now has explicit description clearing, target-
owned modal and list reads, complete bounded pagination, one shared thumbnail
selection lifecycle, title-plus-Usage Track search with keyboard operation,
refresh-only partial-success recovery, and fail-closed edit route IDs. WI-038
zero-based reorder behavior remains unchanged. Refresh provenance survives
retries, filename extensions are advisory, the combobox owns Home/End and
focus-out, and authoritative plus locally committed Album members are excluded
from search. Membership mutations and reads share an immutable canonical Album
page owner, so route replacement or unmount retires stale local continuations
before follow-up reads, feedback, fences, or state commits.

## Scope and DoD

- [x] Blank edit description is present in multipart data as an explicit clear.
- [x] Modal open, close, target switch, failed detail, and retry are generation-
  owned and non-submittable until the active detail is ready.
- [x] Management pages expose all active Albums, normalize malformed and beyond-
  last pages, own the latest request, and retry only on command.
- [x] Create, route edit, and modal edit use the same advisory JPEG/PNG picker,
  compatible optional MIME, 10 MiB, decodability, 4096-dimension, and
  16,777,216-pixel thumbnail component without filename-extension authority.
- [x] Locally created thumbnail URLs are revoked on replacement, rejection,
  clear, and unmount; same-file retry is proven.
- [x] Track search copy matches title plus `USAGE`, stale requests lose
  ownership, current/committed members are excluded, and combobox/listbox
  Arrow/Home/End/Enter/Escape/focus-out/pointer states are proven.
- [x] Add/remove/reorder partial success exposes refresh-only recovery without a
  duplicate mutation; rejected/unconfirmed provenance cannot become committed
  because a retry read fails.
- [x] Missing, malformed, non-positive, and unsafe Album route IDs issue zero
  Album or membership requests.
- [x] WI-038 exact zero-based reorder payload, boundary no-op, canonical refetch,
  and rejection recovery remain green.
- [x] Pending add/remove/reorder continuations are owned by the initiating
  canonical Album route and component lifetime; route replacement or unmount
  prevents stale follow-up reads, feedback, fences, and state commits.
- [x] Final independent QA, focused Album tests, full frontend coverage,
  frontend static/build gates, forced backend build/tests/JaCoCo, documentation
  validation, and whitespace gates pass.

## Reference Documents

| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approval, language, traceability, and safety baseline |
| 0 | `docs/standards/development-standards.md` | React/Java implementation and test evidence standards |
| 0 | `docs/standards/documentation-standards.md` | Current-state document structure and language |
| 0 | `docs/standards/glossary.md` | Album, Track, Usage Guide Tag, and WI terminology |
| 1 | `docs/policies/quality-gates.md` | Regression, rollback, and validation gates |
| 1 | `docs/standards/frontend-standards.md` | Request ownership, object URL, state, and CSS patterns |
| 2 | `docs/design/api-spec.md` | Pagination, multipart, keyword, and response contracts |
| 2 | `docs/design/usecase/sound-album.md` | Album create/update/membership/reorder contract |
| 2 | `docs/design/usecase/sound-track.md` | Title-plus-`USAGE` keyword contract |
| 2 | `docs/ui/screen-flow.md`, `docs/ui/atstudio-front-list.md`, `docs/ui/modal-list.md` | Current UI and modal behavior |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | Effect ownership, state updates, and explicit rendering |
| 3 | `deliverables/user/REQ-20260809-ATS-001.md` | Approved audit correction authority |
| 3 | `deliverables/agent/WI-20260809-ATS-025-findings.md` | `F-UI-025-005` through `F-UI-025-008` evidence |
| 3 | `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md` | `CR-031-058` through `CR-031-061` routing |
| 3 | `deliverables/agent/WI-20260809-ATS-038-evidence-pack.md` | Protected zero-based reorder dependency |

## Production Evidence

| Behavior | Pointer |
|---|---|
| Shared thumbnail selection, optional MIME check, and submit fence | `frontend/src/pages/creator/albumThumbnail.ts:1`; `frontend/src/pages/creator/AlbumThumbnailField.tsx:11-39`; `frontend/src/pages/creator/AlbumCreatePage.tsx:17`; `frontend/src/pages/creator/AlbumEditPage.tsx:532`; `frontend/src/pages/creator/AlbumManagePage.tsx:396` |
| Object URL generation and cleanup | `frontend/src/pages/creator/AlbumThumbnailField.tsx:63-82` |
| Management pagination, list ownership, normalization, and retry | `frontend/src/pages/creator/AlbumManagePage.tsx:67-128`; `frontend/src/pages/creator/AlbumManagePage.tsx:337` |
| Modal target ownership, retry, and close retirement | `frontend/src/pages/creator/AlbumManagePage.tsx:146-201` |
| Explicit description clear request | `frontend/src/pages/creator/AlbumManagePage.tsx:208-217`; `frontend/src/pages/creator/AlbumEditPage.tsx:431-450` |
| Canonical route ID and bounded load recovery | `frontend/src/pages/creator/AlbumEditPage.tsx:36`; `frontend/src/pages/creator/AlbumEditPage.tsx:119-182`; `frontend/src/pages/creator/AlbumEditPage.tsx:461-509` |
| Search latest-owner, member exclusion, and keyboard/focus combobox | `frontend/src/pages/creator/AlbumEditPage.tsx:96-104`; `frontend/src/pages/creator/AlbumEditPage.tsx:237-337`; `frontend/src/pages/creator/AlbumEditPage.tsx:555-644` |
| Retry provenance and committed-membership fence | `frontend/src/pages/creator/AlbumEditPage.tsx:31-82`; `frontend/src/pages/creator/AlbumEditPage.tsx:203-234`; `frontend/src/pages/creator/AlbumEditPage.tsx:340-374`; `frontend/src/pages/creator/AlbumEditPage.tsx:649-664` |
| Canonical Album page owner and retired mutation/read continuations | `frontend/src/pages/creator/AlbumEditPage.tsx:31-62`; `frontend/src/pages/creator/AlbumEditPage.tsx:203-230`; `frontend/src/pages/creator/AlbumEditPage.tsx:340-426`; `frontend/src/pages/creator/AlbumEditPage.tsx:656-662` |
| Preserved zero-based reorder | `frontend/src/pages/creator/AlbumEditPage.tsx:400-427` |
| Existing server clear behavior | `src/main/java/com/atstudio/atstudio/service/AlbumService.java:115-133`; `src/main/java/com/atstudio/atstudio/entity/Album.java:47-50` |
| Existing authoritative image bounds | `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:31-38`; `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:175-183` |

## Test Mapping

| Criterion | Test pointer |
|---|---|
| Explicit clear at UI request and backend entity response | `frontend/src/pages/creator/AlbumManagePage.test.tsx:229`; `frontend/src/pages/creator/AlbumEditPage.test.tsx:516`; `src/test/java/com/atstudio/atstudio/service/AlbumServiceTest.java:302` |
| Modal close/switch ownership and failed-load retry | `frontend/src/pages/creator/AlbumManagePage.test.tsx:177`; `frontend/src/pages/creator/AlbumManagePage.test.tsx:209` |
| Page normalization, complete navigation, bounded retry | `frontend/src/pages/creator/AlbumManagePage.test.tsx:120`; `frontend/src/pages/creator/AlbumManagePage.test.tsx:144`; `frontend/src/pages/creator/AlbumManagePage.test.tsx:161` |
| Thumbnail contract, pending, stale generation, cleanup, same-file retry | `frontend/src/pages/creator/AlbumThumbnailField.test.tsx:59-208`; `frontend/src/pages/creator/AlbumCreatePage.test.tsx:42`; `frontend/src/pages/creator/AlbumManagePage.test.tsx:245`; `frontend/src/pages/creator/AlbumEditPage.test.tsx:494` |
| Search latest-wins, full keyboard/focus/pointer, empty/error/retry | `frontend/src/pages/creator/AlbumEditPage.test.tsx:283`; `frontend/src/pages/creator/AlbumEditPage.test.tsx:348`; `frontend/src/pages/creator/AlbumEditPage.test.tsx:389`; `frontend/src/pages/creator/AlbumEditPage.test.tsx:471` |
| Member exclusion and committed-add fence | `frontend/src/pages/creator/AlbumEditPage.test.tsx:426`; `frontend/src/pages/creator/AlbumEditPage.test.tsx:442` |
| Route-switch and unmount mutation ownership | `frontend/src/pages/creator/AlbumEditPage.test.tsx:202`; `frontend/src/pages/creator/AlbumEditPage.test.tsx:254` |
| Add/remove/reorder refresh-only partial success and retry provenance | `frontend/src/pages/creator/AlbumEditPage.test.tsx:631`; `frontend/src/pages/creator/AlbumEditPage.test.tsx:656`; `frontend/src/pages/creator/AlbumEditPage.test.tsx:682` |
| Invalid route IDs and zero protected requests | `frontend/src/pages/creator/AlbumEditPage.test.tsx:174` |
| WI-038 reorder payload and recovery | `frontend/src/pages/creator/AlbumEditPage.test.tsx:540-681`; `frontend/src/api/domainApis.test.ts` |
| Adjacent current production copy and behavior | `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx:921-1098`; `frontend/src/pages/public/AlbumDetailPage.test.tsx`; `frontend/src/pages/public/AlbumListPages.test.tsx` |

## QA Remediation Closure

| Finding | Production correction | Exact counterexample tests |
|---|---|---|
| `QA-049-001` | `MembershipRefreshProvenance` is stored as `committed` or `unconfirmed`; the retry passes that same state and a successful authoritative read clears it. | `AlbumEditPage.test.tsx:631` proves rejection -> recovery read failure -> retry read failure remains neutral with one mutation; `:656` proves a successful read-only retry clears the neutral state. |
| `QA-049-002` | `AlbumThumbnailField.tsx:11-39` removes filename-extension authority, accepts blank/generic or JPEG/PNG MIME for decode, and rejects only an incompatible supplied MIME before existing size/decode/dimension checks. | `AlbumThumbnailField.test.tsx:69-113` proves extensionless JPEG and PNG selection reaches decode, incompatible MIME is rejected, and corrupt compatible data is rejected by decode. |
| `QA-049-003` | `AlbumEditPage.tsx:295-337` adds Home/End while preserving active-option ownership; `:555` adds focus-aware dismissal while option `mousedown` retains pointer selection. | `AlbumEditPage.test.tsx:348` proves active option, `aria-activedescendant`, Home, End, and Enter; `:389` proves Escape, outside focus, and pointer selection. |
| `QA-049-004` | `AlbumEditPage.tsx:96-104` filters authoritative and locally fenced member IDs; `:340-374` installs the fence after commit and `:203-230` clears it only after an authoritative read. | `AlbumEditPage.test.tsx:426` proves initial-member exclusion; `:442` proves committed-add/refresh-failure exclusion with one add call. |
| `QA-049-R2-001` | `AlbumEditPage.tsx:31-62` creates and retires the canonical Album page owner; `:203-230` combines page and membership-request ownership; `:340-426` revalidates add/remove/reorder after every await before follow-up work. | `AlbumEditPage.test.tsx:202` proves Album 11 pending add -> Album 12 route replacement has only detail IDs `[11, 12]`, no retired toast/fence/state, an authoritative Album 12 projection, and only remove `(12, 121)`; `:254` proves unmount permits no follow-up read or feedback. |

## Final Authoritative Results

The results below are the final authority supplied by
`WI-20260809-ATS-049-finalization-handoff.md`. This documentation finalization
did not rerun any command.

| Gate | Authority or exact command | Final result |
|---|---|---|
| Final independent QA | `WI-20260809-ATS-049-qa-final-review-result.md` | `PASS`: `QA-049-001` through `QA-049-004` and `QA-049-R2-001` are `CLOSED`; no new P0-P2 |
| R2 focused route ownership | `npm test -- --run src/pages/creator/AlbumEditPage.test.tsx` | `PASS`: 1 file, 20 tests, 0 failed |
| Focused and adjacent Album tests | `npm test -- --run src/pages/creator/AlbumCreatePage.test.tsx src/pages/creator/AlbumEditPage.test.tsx src/pages/creator/AlbumManagePage.test.tsx src/pages/creator/AlbumThumbnailField.test.tsx src/pages/public/AlbumDetailPage.test.tsx src/pages/public/AlbumListPages.test.tsx src/api/domainApis.test.ts src/test/coverage/publicAuthShell.coverage.test.tsx` | `PASS`: 8 files, 93 tests, 0 failed |
| Frontend full coverage run | Final full-gate record; command is not restated in the finalization handoff | `PASS`: 95 files, 1,142 tests |
| Frontend coverage | Final full-gate record | Statements 89.2% (9499/10648); branches 81.41% (6187/7599); functions 89.91% (2201/2448); lines 91.73% (8754/9543) |
| Frontend static, format, and build gates | Final full-gate record | Typecheck `PASS`; ESLint `PASS` with zero warnings; full Prettier `PASS`; production build `PASS`; Vite transformed 289 modules |
| Forced backend final build | `test jacocoTestReport jacocoTestCoverageVerification assemble --rerun-tasks --no-daemon --max-workers=1 --console=plain` | `BUILD SUCCESSFUL` in 3m19s |
| Backend tests | Forced backend final build report | 184 suites; 1,587 tests; 0 failures; 0 errors; 19 skipped |
| JaCoCo | Forced backend final build report | Instruction 87.027%; branch 72.293%; line 87.294%; method 84.862%; verification `PASS` |
| Documentation validation | Final full-gate record | `PASS`: 585 traceability IDs |
| Whitespace/diff | Final full-gate record | `PASS`; only existing CRLF-to-LF notices for `sound-album.md` and `AlbumServiceTest.java` |

## Historical Verification Evidence

- Initial focused red from the continuation handoff: 3 Album page files, 18
  tests, 4 passed and 14 failed.
- Intermediate pre-final green: 4 Album files, 23 tests passed; typecheck and
  lint passed. A later adjacent run had 8 files and 79 tests, with 73 passed and
  6 stale `publicAuthShell.coverage` assertions failed.
- A corrected intermediate run reached 8 files and 81 passing tests. Independent
  QA also passed those tests but returned `FAIL` because four mandatory
  counterexamples were absent. The unchanged historical result is
  `WI-20260809-ATS-049-qa-integ-review-result.md`.
- Remediation red ran 27 tests across 2 files: 19 passed and 8 failed, covering
  extensionless/decode behavior, Home/End/focus-out, member exclusion/fencing,
  and retry provenance. The remediated targeted suite then passed all 27.
- Independent R2 QA passed 8 files and 91 tests but returned `FAIL` for
  `QA-049-R2-001`. The unchanged historical result is
  `WI-20260809-ATS-049-qa-integ-rereview-result.md`.
- R2 red ran 20 `AlbumEditPage` tests: 18 passed and 2 route-owner tests failed.
  Route replacement produced detail IDs `[11, 12, 11]`, and unmount produced a
  second detail read. After remediation, all 20 passed.
- Final independent QA returned `PASS` in
  `WI-20260809-ATS-049-qa-final-review-result.md`; all five findings are closed
  and no new P0-P2 was identified.

## Remediation-Changed Files

### R2

- `frontend/src/pages/creator/AlbumEditPage.tsx`,
  `frontend/src/pages/creator/AlbumEditPage.test.tsx`,
  `docs/design/usecase/sound-album.md`,
  `deliverables/agent/WI-20260809-ATS-049-evidence-pack.md`, and
  `deliverables/user/WI-20260809-ATS-049-summary.md`.

### R1

- `frontend/src/pages/creator/AlbumEditPage.tsx`
- `frontend/src/pages/creator/AlbumEditPage.test.tsx`
- `frontend/src/pages/creator/AlbumThumbnailField.tsx`
- `frontend/src/pages/creator/AlbumThumbnailField.test.tsx`
- `docs/design/usecase/sound-album.md`
- `deliverables/agent/WI-20260809-ATS-049-evidence-pack.md`
- `deliverables/user/WI-20260809-ATS-049-summary.md`

## Changed Files

### Production and styles

- `frontend/src/pages/creator/AlbumCreatePage.tsx`
- `frontend/src/pages/creator/AlbumEditPage.tsx`
- `frontend/src/pages/creator/AlbumEditPage.module.css`
- `frontend/src/pages/creator/AlbumManagePage.tsx`
- `frontend/src/pages/creator/AlbumManagePage.module.css`
- `frontend/src/pages/creator/AlbumThumbnailField.tsx`
- `frontend/src/pages/creator/AlbumThumbnailField.module.css`
- `frontend/src/pages/creator/albumThumbnail.ts`
- `frontend/src/utils/validation.ts`

### Tests

- `frontend/src/pages/creator/AlbumCreatePage.test.tsx`
- `frontend/src/pages/creator/AlbumEditPage.test.tsx`
- `frontend/src/pages/creator/AlbumManagePage.test.tsx`
- `frontend/src/pages/creator/AlbumThumbnailField.test.tsx`
- `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx`
- `src/test/java/com/atstudio/atstudio/service/AlbumServiceTest.java`

### Current-state documents and deliverables

- `docs/design/api-spec.md`
- `docs/design/usecase/sound-album.md`
- `docs/ui/atstudio-front-list.md`
- `docs/ui/modal-list.md`
- `docs/ui/screen-flow.md`
- `deliverables/agent/WI-20260809-ATS-049-evidence-pack.md`
- `deliverables/user/WI-20260809-ATS-049-summary.md`

## Contract Lanes and Effects

- UI state: verified in deterministic React tests for loading, error, empty,
  pending, stale, keyboard, partial-success, and recovery transitions.
- API invocation: verified through mocks for exact Album IDs, explicit blank
  `description`, page/size, keyword, one mutation, refresh-only retry, and zero
  invalid-route requests.
- Server contract: inspected in Album controller/service/entity/DTO and
  `CanonicalImageService`; `AlbumServiceTest` verifies blank clear behavior.
- Durable state and external effects: not executed. No live DB, storage,
  provider, mail, payment, download, deployment, or browser upload occurred.

## Risks and Rollback

- Browser image checks are advisory. They do not prove byte signature, APNG
  rejection, canonical JPEG output, or storage behavior; the existing backend
  remains authoritative for those checks.
- The committed-add fence is component-local and bounded by unique successful
  additions since the last authoritative Album read. It is not durable state;
  the server remains the duplicate-membership authority.
- Tests use JSDOM, mocked object URLs, and mocked APIs. No manual ADMIN browser
  acceptance or real media fixture run was performed. Live browser, storage,
  and durable-state acceptance remains unexecuted.
- Route-switch and unmount ownership is proven with React Router and mocked API
  timing. A provider/server mutation already submitted before route departure
  may still commit server-side; the UI retires only the stale local
  continuation and does not cancel or roll back that request.
- Full frontend coverage and the forced backend build passed, but automated
  gates do not establish live browser, storage, media, or durable-state
  behavior.
- Final independent QA passed after both remediation rounds. The earlier QA
  `FAIL` result files remain historical evidence and are not current verdicts.
- WI-059 public Album semantics and WI-070 broader page inventory remain out of
  scope.
- Rollback is a scoped revert of the listed frontend source/tests/styles,
  AlbumService test, current-state docs, and WI deliverables. No data or
  external rollback is required.

## Safety and Escalation

- No architecture, dependency, schema, security, destructive-data, product-
  policy, or external-effect decision was required.
- No live ADMIN mutation, DB/storage/media/external effect, protected-output
  access, secret inspection, branch action, staging, commit, push, or deployment
  occurred during WI-049 implementation, verification, or this finalization.
- Escalation: none. Final independent QA and all recorded full repository gates
  are complete; commit and push remain separate, unperformed actions.
