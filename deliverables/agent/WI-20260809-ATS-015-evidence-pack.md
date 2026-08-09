# Evidence Pack: WI-20260809-ATS-015

## Summary

- Independently verified the repaired Playlist reorder path as a zero-based
  end-to-end contract and closed WI-029 MAJOR-001 with no remaining finding.

## Scope / DoD Check

- [x] Traced a rendered non-empty Playlist reorder through the frontend API,
      controller/DTO boundary, service validation, and managed entity updates.
- [x] Confirmed exact visible active Track membership and contiguous `0..n-1`
      order semantics.
- [x] Confirmed the focused page regression fails the former one-based
      producer contract.
- [x] Confirmed backend acceptance, retained inactive-row ordering, and the
      one-based rejection condition.
- [x] Confirmed no new request, remote-call loop, or query behavior.
- [x] Updated only WI-015 deliverables and the final WI-029 disposition.

## Reference Documents

| Tier    | Document                                                  | Use                                                    |
| ------- | --------------------------------------------------------- | ------------------------------------------------------ |
| 0       | `docs/standards/core-principles.md`                       | Traceability, transparency, and review boundary        |
| 0       | `docs/standards/development-standards.md`                 | Evidence-first review and focused test expectations    |
| 1       | `docs/policies/quality-gates.md`                          | Regression, impact, rollback, and review gates         |
| Context | `deliverables/user/REQ-20260808-ATS-004.md`               | Approved scope and quality baseline                    |
| Context | `deliverables/agent/WI-20260808-ATS-029-handoff.md`       | Original review scope                                  |
| Context | `deliverables/user/WI-20260808-ATS-029-summary.md`        | Original MAJOR-001 and final disposition target        |
| Context | `deliverables/agent/WI-20260808-ATS-029-evidence-pack.md` | Original detailed finding and final disposition target |
| Context | `deliverables/user/WI-20260809-ATS-014-summary.md`        | Repair claim under independent review                  |
| Context | `deliverables/agent/WI-20260809-ATS-014-evidence-pack.md` | Repair pointers and prior focused verification         |

## Evidence Pointers

### Frontend producer and transport

- `frontend/src/pages/subscriber/PlaylistEditPage.tsx:89-95,268-295`
  - Rendered controls swap the Track array without changing membership.
- `frontend/src/pages/subscriber/PlaylistEditPage.tsx:115-125`
  - A changed non-empty order maps every visible Track to its zero-based array
    index and calls `reorderTracks` once.
- `frontend/src/api/playlists.ts:84-90`
  - Sends `{ tracks }` unchanged in one PUT request.
- `frontend/src/pages/subscriber/PlaylistEditPage.test.tsx:83-99`
  - Moves Track 101 below Track 102 and asserts exact values
    `102 -> 0`, `101 -> 1`, `103 -> 2`.
- `frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx:953-978`
  - Existing metadata-plus-reorder workflow asserts `12 -> 0`, `11 -> 1`.

### Backend boundary, validation, and persistence

- `src/main/java/com/atstudio/atstudio/controller/PlaylistController.java:109-115`
  - Binds the same PUT body and delegates the validated request.
- `src/main/java/com/atstudio/atstudio/dto/playlist/PlaylistReorderRequest.java:8-10`
  - Requires a non-empty validated Track list.
- `src/main/java/com/atstudio/atstudio/dto/playlist/PlaylistTrackOrderItem.java:6-9`
  - Requires non-null Track IDs and non-negative orders.
- `src/main/java/com/atstudio/atstudio/service/PlaylistService.java:218-242`
  - Runs transactionally, locks ownership, loads memberships once, validates,
    applies requested active orders, and appends inactive rows.
- `src/main/java/com/atstudio/atstudio/service/PlaylistService.java:334-363`
  - Requires exact active Track ID membership, unique orders, and every integer
    from zero through `activeCount - 1`. A former `1..n` request lacks zero and
    throws `INVALID_ARGUMENT`.
- `src/main/java/com/atstudio/atstudio/repository/PlaylistTrackRepository.java:14-21`
  - Loads all memberships in deterministic `trackOrder, trackId` order with
    Track state available in the same query.
- `src/main/java/com/atstudio/atstudio/entity/PlaylistTrack.java:28-33`
  - Stores `trackOrder` in a non-null entity column; transactional managed
    entity updates are persisted by JPA dirty checking.
- `src/test/java/com/atstudio/atstudio/service/PlaylistServiceTest.java:443-507`
  - Verifies accepted zero-based values, inactive-row retention, final
    contiguous orders, and deterministic reactivation position.
- `src/test/java/com/atstudio/atstudio/service/PlaylistServiceTest.java:509-558`
  - Verifies active membership and order uniqueness rejection boundaries.
- `src/test/java/com/atstudio/atstudio/controller/PlaylistControllerTest.java:154-164`
  - Verifies the controller accepts a zero-based request body.

### Current-state documentation

- `docs/design/api-spec.md:278-281`
  - Specifies every visible active Track exactly once with orders `0..n-1`.
- `docs/design/usecase/sound-playlist.md:133-145`
  - Specifies the same validation, inactive-row retention, and DB postcondition.

## Performance Review

- The WI-014 implementation delta is `trackOrder: i + 1` to `trackOrder: i`.
- The UI still sends one PUT only when order changed and the Playlist is
  non-empty.
- The service still performs one ownership lock, one membership query, and
  local O(n) validation/update passes. No remote call, additional query, or
  request fan-out was introduced.

## Commands And Outputs

1. `git status --short`, `git branch --show-current`,
   `git rev-parse --short HEAD`
   - Shared dirty worktree preserved on
     `codex/v1-release-rehearsal-fixes` at `c7f779d`.
2. Targeted `rg`, numbered `Get-Content`, and scoped `git diff`
   - Traced only Playlist reorder inputs, implementation, tests, and docs.
3. `npm test -- src/pages/subscriber/PlaylistEditPage.test.tsx`
   - PASS: 1 file, 1 test; Vitest 4.1.4; 5.61 seconds.
4. `npx vitest run src/test/coverage/adminSubscriberPages.coverage.test.tsx -t "saves playlist metadata and reordered tracks after meaningful edits"`
   - PASS: 1 file, 1 selected test; 32 skipped; 6.87 seconds.
5. `.\gradlew.bat test --tests "com.atstudio.atstudio.service.PlaylistServiceTest" --tests "com.atstudio.atstudio.controller.PlaylistControllerTest"`
   - PASS: 44 tests, 0 failures/errors/skips; `BUILD SUCCESSFUL in 41s`.

## Findings And Final Disposition

- BLOCKER: 0
- MAJOR: 0
- MINOR: 0
- **Result: PASS**
- WI-029 MAJOR-001: resolved by WI-014 and independently verified here.
- WI-030: unblocked from the WI-029 Playlist reorder review perspective.

## Residual Risks / Rollback

- Focused jsdom and unit/controller tests do not replace deployed browser/API
  or MySQL verification.
- No service test is named specifically for a complete one-based `1..n`
  request. This does not leave the former defect unprotected: the exact page
  producer regression fails that implementation, and the service complete-set
  check deterministically rejects a missing zero.
- No product, test, schema, data, external-provider, secret, ZIP, Git staging,
  commit, or push action was performed by WI-015.
- Rollback: remove the two WI-015 deliverables and restore only the WI-029
  disposition hunks. Preserve every unrelated dirty or untracked file.
