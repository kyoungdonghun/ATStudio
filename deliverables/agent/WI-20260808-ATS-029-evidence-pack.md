---
version: 1.0
last_updated: 2026-08-09
project: ATS
owner: cr
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20260808-ATS-029-handoff.md
    reason: Approved final re-review scope and constraints
  - path: WI-20260809-ATS-006-evidence-pack.md
    reason: Confirmed backend findings
  - path: WI-20260809-ATS-007-evidence-pack.md
    reason: Confirmed frontend findings
  - path: WI-20260809-ATS-010-evidence-pack.md
    reason: Backend repair evidence
  - path: WI-20260809-ATS-011-evidence-pack.md
    reason: Frontend repair evidence
  - path: ../../docs/standards/core-principles.md
    reason: Tier 0 constitution
  - path: ../../docs/standards/development-standards.md
    reason: Tier 0 development and review standard
---

# Evidence Pack: WI-20260808-ATS-029

## Confirmed Findings

### MAJOR-001 - Playlist reorder wire order is incompatible with the repaired service contract

**Evidence pointers**

- `frontend/src/pages/subscriber/PlaylistEditPage.tsx:120-124` maps visible rows to `trackOrder: i + 1`.
- `src/main/java/com/atstudio/atstudio/service/PlaylistService.java:334-363` requires unique contiguous orders containing every integer from `0` through `activeCount - 1`.
- `frontend/src/api/playlists.ts:84-90` sends the editor array without an order-base conversion.
- `frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx:953-976` explicitly expects one-based orders from the editor.
- `src/test/java/com/atstudio/atstudio/service/PlaylistServiceTest.java:443-507` independently exercises only zero-based service payloads, including retained inactive rows.

**Reproduction by contract**

1. Load an owned Playlist with at least two visible active Tracks; retained inactive membership may also be present.
2. Move one visible Track in `PlaylistEditPage` and save.
3. The frontend sends orders `1` and `2`.
4. `validatePlaylistReorderRequest` checks for order `0`, does not find it, and throws `INVALID_ARGUMENT` before the inactive-row append logic can complete.

**Impact**

The visible owner workflow remains unreorderable. This directly invalidates the WI-010 claim that the WI-006 Playlist finding is repaired across detail and reorder behavior. Backend final-order uniqueness is internally correct for a valid zero-based request, but the active SPA does not produce that request.

**Required action**

Align the active SPA and backend on one documented order base. The smallest repair under the current zero-based design is `trackOrder: i` in `PlaylistEditPage`, followed by a focused cross-layer regression with active and inactive memberships. Re-run the Playlist service/repository tests and the Playlist editor test before repeating WI-029.

## Review Disposition

- **Result:** PASS
- **Finding count:** 0 BLOCKER, 0 MAJOR, 0 MINOR
- **WI-029:** MAJOR-001 resolved by WI-014 and independently verified by
  WI-015
- **WI-030:** Unblocked from the WI-029 perspective
- **Authority:** This is the final disposition. Earlier finding, repair,
  command, and follow-up sections are retained as the original review record
  and are superseded only as to disposition by
  `deliverables/agent/WI-20260809-ATS-015-evidence-pack.md`.

## Scope And DoD Check

- [x] Re-reviewed the WI-006 backend findings against WI-010 implementation and focused tests.
- [x] Re-reviewed the WI-007 frontend findings against WI-011 implementation and focused tests.
- [x] Verified Track pagination bounds.
- [ ] Verified Playlist count/detail/reorder as one working active-membership workflow; count/detail pass, but reorder fails MAJOR-001.
- [x] Verified Album active counts, active-only `trackCount` sort, and detail projection; the WI-006 Album MINOR is resolved.
- [x] Verified omitted nullable PlayableTrack fields, immediate duration replacement, independent taxonomy failure, fallback chips, and raw Usage values.
- [x] Checked only the requested repair regressions: order uniqueness, query fan-out, synthetic-key collision, stale taxonomy response, and playback policy change.
- [x] Recorded browser and MySQL performance uncertainty as residual risk rather than an evidence-free finding.

## Reference Documents

| Tier | Document                                            | Use                                                                      |
| ---- | --------------------------------------------------- | ------------------------------------------------------------------------ |
| 0    | `docs/standards/core-principles.md`                 | Constitution, language, traceability, and transparency                   |
| 0    | `docs/standards/development-standards.md`           | Evidence-first review and focused test expectations                      |
| 0    | `docs/standards/documentation-standards.md`         | Deliverable metadata and structure                                       |
| 0    | `docs/standards/glossary.md`                        | Canonical Track, Playlist, Tag, Usage Guide Tag, and PlayableTrack terms |
| 2    | `deliverables/user/REQ-20260808-ATS-004.md`         | Approved SR-94 through SR-101 scope and quality gates                    |
| 2    | `deliverables/agent/WI-20260808-ATS-029-handoff.md` | Review scope, constraints, and output contract                           |

## Repair Evidence

### Track page and size bounds - PASS

- Implementation: `src/main/java/com/atstudio/atstudio/service/TrackService.java:102-135,278-283`.
- Tests: `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java:206-255`; `src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java:231-277`.
- Result: invalid pages and sizes fail before repository access; size `100` preserves 1-based public page metadata.

### Playlist active membership - PARTIAL / MAJOR-001

- Active count/detail: `src/main/java/com/atstudio/atstudio/repository/PlaylistTrackRepository.java:23-48`; `src/main/java/com/atstudio/atstudio/service/PlaylistService.java:79-114,212-243`.
- Deterministic backend ordering: all rows load by `trackOrder, trackId`; valid active requests receive `0..n-1`; inactive rows follow in prior deterministic order.
- Query behavior: `src/test/java/com/atstudio/atstudio/repository/PlayableTrackQueryCountTest.java:178-206` keeps one detail query for one or many rows.
- Failure: the active editor emits one-based orders, as documented in MAJOR-001.

### Album active counts and sort - PASS

- Sort query: `src/main/java/com/atstudio/atstudio/repository/AlbumRepository.java:22-28` filters the joined Track before `COUNT(at)` and retains deterministic tie-breakers.
- Count/detail: `src/main/java/com/atstudio/atstudio/repository/AlbumTrackRepository.java:23-60`; `src/main/java/com/atstudio/atstudio/service/AlbumService.java:76-107`.
- Integration evidence: `src/test/java/com/atstudio/atstudio/repository/ActiveMembershipRepositoryTest.java:70-112` distinguishes active public counts from all memberships and verifies active-only ordering/detail.
- Finding disposition: WI-006 MINOR-001 is repaired.

### Omitted nullable PlayableTrack keys - PASS

- Boundary mapper: `frontend/src/utils/playableTrack.ts:17-33` accepts omitted thumbnail/waveform keys, normalizes them to `null`, and retains positive safe-integer identity validation.
- Wire types: `frontend/src/api/albums.ts:6-13`; `frontend/src/api/playlists.ts:6-15`; `frontend/src/types/index.ts:197-208`.
- Tests: `frontend/src/utils/playableTrack.test.ts:46-113`; `frontend/src/store/playerPersistence.test.ts:204-239`; aggregate page tests exercise context, play, queue, persistence, and reload.

### Immediate duration switch - PASS

- Implementation: `frontend/src/store/playerStore.ts:519-545` commits `currentTrack`, `currentTime: 0`, and `duration: track.duration` in one transition.
- Tests: `frontend/src/store/playerStore.test.ts:257-276`; `frontend/src/layouts/PlayerBar.test.tsx:240-270` verify immediate duration, progress, seek scale, and ARIA values before replacement metadata.
- Playback policy: buffering timers, real-error separation, public stream URL, and entitled download paths were not broadened by this repair; focused player regressions remain green.

### Taxonomy isolation, fallback, and raw Usage - PASS

- Independent state and stale fencing: `frontend/src/pages/public/TrackListPage.tsx:262-309` uses per-type generations and in-flight tokens.
- Collision-resistant keys and active fallback merge: `frontend/src/pages/public/TrackListPage.tsx:69-99` namespaces API and URL keys by source and Tag type and includes identity/value material.
- Raw API state: `frontend/src/pages/public/TrackListPage.tsx:326-338`; `frontend/src/api/tracks.ts:53-71`; display-only `#`: `frontend/src/utils/tagName.ts:48-50`.
- Tests: `frontend/src/pages/public/TrackListPage.test.tsx:510-648` covers one failed taxonomy, scoped retry, omitted active values, visible/removable row and modal chips, reset, and raw API arrays.

## Requested Regression Review

| Regression axis         | Result     | Evidence                                                                                                                                     |
| ----------------------- | ---------- | -------------------------------------------------------------------------------------------------------------------------------------------- |
| Ordering uniqueness     | MAJOR-001  | Backend final rows are unique for valid zero-based requests, but the active client emits an incompatible one-based set.                      |
| Query fan-out           | No finding | Active counts use aggregate queries; detail projections fetch Track and creator; query-count tests remain constant for one versus many rows. |
| Synthetic-key collision | No finding | API and URL fallback keys use distinct source/type namespaces plus identity/value material.                                                  |
| Stale taxonomy response | No finding | Per-type generation checks discard superseded completion; retries are in-flight fenced and scoped.                                           |
| Playback policy change  | No finding | Immediate duration replacement does not change stream/download authorization or buffering/error policy.                                      |

## Commands And Outputs

### Static review

- `git status --short`, `git branch --show-current`, and `git rev-parse --short HEAD` inspected the shared dirty worktree on `codex/v1-release-rehearsal-fixes` at `c7f779d`.
- Targeted `rg`, numbered `Get-Content`, and scoped `git diff` inspected only the WI findings, repairs, affected implementations, and focused tests.
- No external request, browser session, secret/environment-value inspection, ZIP access, schema/data action, commit, push, or staging occurred.

### Focused backend tests

```text
.\gradlew.bat test --tests "com.atstudio.atstudio.service.TrackServiceTest" --tests "com.atstudio.atstudio.controller.TrackControllerTest" --tests "com.atstudio.atstudio.service.PlaylistServiceTest" --tests "com.atstudio.atstudio.service.AlbumServiceTest" --tests "com.atstudio.atstudio.repository.ActiveMembershipRepositoryTest" --tests "com.atstudio.atstudio.repository.PlayableTrackQueryCountTest" --tests "com.atstudio.atstudio.service.AlbumPlaylistMutationLockContractTest"
```

- Result: PASS, `BUILD SUCCESSFUL in 53s`.
- Scope: seven selected test classes; no full suite.

### Focused frontend tests

```text
npm test -- src/utils/playableTrack.test.ts src/store/playerStore.test.ts src/store/playerPersistence.test.ts src/layouts/PlayerBar.test.tsx src/pages/public/TrackListPage.test.tsx src/components/catalogComponents.test.tsx src/test/coverage/publicAuthShell.coverage.test.tsx src/test/coverage/adminSubscriberPages.coverage.test.tsx
```

- Result: PASS.
- Vitest: 4.1.4.
- Test files: 8 passed / 8.
- Tests: 144 passed / 144.
- Duration: 16.33 seconds.

The green focused runs do not invalidate MAJOR-001 because the frontend and backend order-base assertions are isolated behind mocked boundaries.

## Residual Risks

1. Hibernate/H2 and query-count evidence does not include a MySQL execution plan or production-cardinality latency/payload measurement. This remains a residual performance risk, not a finding without evidence.
2. jsdom-focused tests do not reproduce native browser media-event ordering, request cancellation timing, or production waveform rendering cost.
3. Per-type taxonomy generations ignore stale responses but do not cancel the underlying transport request.
4. Existing malformed legacy membership orders are normalized only by authorized reorder/remove operations; no backfill or retained-data mutation was approved.
5. The repository has a substantial shared dirty worktree, so all rollback and subsequent review must remain hunk-scoped.

## Rollback

This WI changed no product, test, schema, or data state. To withdraw only the review output, remove these two newly created files:

- `deliverables/user/WI-20260808-ATS-029-summary.md`
- `deliverables/agent/WI-20260808-ATS-029-evidence-pack.md`

Do not replace or revert any shared product file. Test execution created only normal local Gradle/Vitest outputs.

## Follow-up

Repair MAJOR-001, add one cross-layer Playlist reorder regression, repeat the focused Playlist review, and keep `WI-20260808-ATS-030` blocked until WI-029 records 0 BLOCKER and 0 MAJOR.
