---
version: 1.0
last_updated: 2026-08-13
project: ATS
owner: qa-integ
category: audit
status: confirmed
dependencies:
  - path: WI-20260809-ATS-049-qa-integ-rereview-handoff.md
    reason: Independent re-review scope and output contract
  - path: WI-20260809-ATS-049-qa-integ-review-result.md
    reason: Immutable original FAIL findings
  - path: WI-20260809-ATS-049-remediation-handoff.md
    reason: Required remediation and counterexample contract
---

# Independent QA-INTEG Re-review: WI-20260809-ATS-049

## Findings

No new P0 or P1 finding was identified. One new P2 finding blocks acceptance.

### QA-049-R2-001 - A retired Album mutation can repopulate the current route with another Album's tracks

- Severity: `P2`
- Contract: Async handlers and membership reads must remain owned by the Album
  route that initiated them. A retired target must not commit UI state or start
  a follow-up read for the current route.
- Evidence: The route effect retires existing membership work when `albumId`
  changes at `frontend/src/pages/creator/AlbumEditPage.tsx:87-100`, but a
  mutation that has not resolved yet is not abortable or represented by a page
  owner. After their mutation awaits, `handleAddTrack`, `handleRemoveTrack`, and
  `handleMoveTrack` continue into `refetchTracks` at lines `303-374` without
  checking that the initiating Album is still current. `refetchTracks` captures
  that retired render's `albumId` and validates only its newly allocated
  membership-request generation at lines `171-193`; it does not validate the
  current page generation or Album ID before `setTracks`, fence reconciliation,
  or refresh-status commits. The router uses one parameterized
  `albums/:albumId/edit` element at `frontend/src/router/index.tsx:207`, so a
  parameter change can preserve the component instance.
- Exact counterexample: load Album 11 and leave `addTrackToAlbum(11, 42)`
  pending. Navigate the same edit route to Album 12 and let Album 12 detail
  finish. Then resolve the Album 11 add. The retired continuation adds the local
  fence and starts `fetchAlbumDetail(11)` after the Album 12 route effect has
  already retired prior membership work. If that read resolves last, its
  generation is current and it writes Album 11 tracks into the Album 12 screen.
  A subsequent remove action is rendered by the current Album 12 handlers and
  can call `removeTrackFromAlbum(12, <Album-11-track-id>)`; a Track shared by the
  two Albums can therefore be removed from the wrong Album. An unmount variant
  still permits the retired follow-up read and global toast after departure.
- Test gap: `AlbumEditPage.test.tsx` covers stale search ownership, invalid
  routes, and membership retry generations, but contains no route-target switch
  while add/remove/reorder is pending. The green 27-test and 91-test runs do not
  exercise this schedule.
- Remediation expectation: snapshot an immutable page owner when each
  membership mutation begins, revalidate it after every await, and let
  `refetchTracks` commit only when both its request generation and current Album
  owner match. Add a route-switch test that proves zero retired follow-up reads,
  zero retired feedback/state commits, and no Album 11 projection on Album 12.

## Closure Matrix

| Finding | Status | Independent closure evidence |
|---|---|---|
| `QA-049-001` | `CLOSED` | `MembershipRefreshProvenance` is retained through `refetchTracks` and the retry passes the existing provenance at `AlbumEditPage.tsx:22`, `:71-76`, `:171-197`, and `:599-614`. Tests `:524-547` prove rejection -> recovery failure -> retry failure remains unconfirmed with one reorder call; `:549-573` proves a successful retry performs a read only and clears the alert. The prior hard-coded committed retry would fail these assertions in principle. |
| `QA-049-002` | `CLOSED` | `AlbumThumbnailField.tsx:28-39` contains no filename check and accepts blank, generic binary, JPEG, or PNG supplied MIME before browser decode; `:80-151` retains size/decode/bounds rejection. Tests `AlbumThumbnailField.test.tsx:69-112` prove extensionless JPEG/PNG reaches decode, incompatible supplied MIME stops before decode, and corrupt compatible data fails at decode. The prior extension gate would fail the object-URL/decode assertions in principle. |
| `QA-049-003` | `CLOSED` | `AlbumEditPage.tsx:247-299` implements Home/End, active-option ownership, Enter, Escape, Tab, and focus-out; `:505-578` links combobox, listbox, option IDs, and mouse selection while preserving input focus. Tests `:241-317` assert `aria-activedescendant`, first/last option selection, dismissal, and one pointer-selected add. The prior missing Home/End and blur branches would fail these assertions in principle. |
| `QA-049-004` | `CLOSED` | Authoritative members and the locally committed fence are filtered at `AlbumEditPage.tsx:44-69`; add installs the fence before refresh at `:303-319`; only a successful authoritative read reconciles it at `:180-187`. Tests `:319-362` prove initial-member absence and committed-add absence after refresh failure with exactly one add mutation. The prior tracks-only availability rule would fail both counterexamples in principle. |

## Additional Required Checks

| Check | Result |
|---|---|
| WI-038 zero-based reorder | `PASS`: `AlbumEditPage.tsx:354-374` still emits every member once with contiguous zero-based orders. Tests `:433-573` remain green, including boundary no-op, authoritative recovery, rejection, and read-only retry call counts. |
| Historical QA evidence | `PASS`: `WI-20260809-ATS-049-qa-integ-review-result.md` still records the original four-P2 `FAIL`. The evidence pack and user summary describe later remediation without replacing that historical verdict. |
| Current remediation docs | `PASS` for QA-049-001..004: `docs/design/usecase/sound-album.md` describes retained provenance, advisory filename handling, combobox behavior, and member filtering. The new route-owner P2 above is not covered. |

## Verdict

`FAIL`

`QA-049-001` through `QA-049-004` are closed, but `QA-049-R2-001` is a new
P2 target-ownership regression. WI-049 should not proceed to final full gates or
commit until the retired mutation continuation is fenced and independently
retested.

## Independent Test Results

| Command | Result |
|---|---|
| `npm test -- --run src/pages/creator/AlbumEditPage.test.tsx src/pages/creator/AlbumThumbnailField.test.tsx` | `PASS`: Vitest 4.1.4; 2 files, 27 tests, 0 failed; Vitest duration 4.24 s (test phase 2.38 s). |
| `npm test -- --run src/pages/creator/AlbumCreatePage.test.tsx src/pages/creator/AlbumEditPage.test.tsx src/pages/creator/AlbumManagePage.test.tsx src/pages/creator/AlbumThumbnailField.test.tsx src/pages/public/AlbumDetailPage.test.tsx src/pages/public/AlbumListPages.test.tsx src/api/domainApis.test.ts src/test/coverage/publicAuthShell.coverage.test.tsx` | `PASS`: Vitest 4.1.4; 8 files, 91 tests, 0 failed; Vitest duration 9.53 s (test phase 10.76 s across workers). |
| `.\gradlew.bat test --tests "com.atstudio.atstudio.service.AlbumServiceTest" --rerun-tasks` | `PASS`: Gradle build successful in 26 s; 5 tasks executed. Generated XML reports 20 tests, 0 failures, 0 errors, 0 skipped, suite time 4.729 s. |

The Gradle run emitted existing unchecked/unsafe-operation and JVM class-data-
sharing notes; neither was a test failure. No typecheck, lint, Prettier, frontend
build, documentation validator, full repository suite, or coverage command was
independently rerun in this checkpoint.

## Residual Risks

- The thumbnail tests simulate browser decode events in JSDOM. They prove the
  client gate and ownership logic, not byte signature, APNG rejection,
  canonical JPEG output, storage, or durable state; the backend remains
  authoritative.
- The committed-add fence is component-local and is reconciled by a successful
  Album read. Server duplicate membership enforcement remains the durable
  authority.
- The focused suites use mocked APIs and do not prove real ADMIN browser timing,
  route transitions, media upload, live DB, storage, provider, mail, payment,
  download, or deployment behavior.
- WI-059 public Album semantics and WI-070 broad screen inventory remain outside
  this review.
- Protected output paths and ignored secrets were not accessed. No live or
  external effect was invoked, and no production, test, current-state document,
  staging, commit, push, or branch state was changed by this review.
