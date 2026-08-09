# WI-20260809-ATS-015 Playlist Reorder Independent Review Summary

## Outcome

**PASS - 0 BLOCKER, 0 MAJOR, 0 MINOR.**

The WI-029 Playlist reorder finding is repaired end to end. The active editor
submits every visible Track exactly once with zero-based contiguous
`trackOrder` values, the backend accepts that exact contract and applies it to
managed Playlist membership entities, and focused frontend and backend tests
pass.

`WI-20260808-ATS-030` is unblocked from the WI-029 Playlist reorder review
perspective.

## Contract Trace

- The rendered Move up/Move down controls reorder the page's Track array.
- Save maps the resulting array to `{ trackId, trackOrder: i }`, producing
  `0..n-1` for every non-empty reordered Playlist.
- `reorderTracks` sends that array unchanged in one
  `PUT /api/playlists/{playlistId}/tracks` request.
- The controller validates the request DTO. The service then locks the owned
  Playlist, loads memberships once, restricts the request contract to visible
  active Tracks, and requires exact Track ID membership plus the complete
  order set `0..n-1`.
- In the same transaction, active memberships receive the requested values and
  retained inactive memberships are assigned contiguous values after them.
  JPA dirty checking persists the managed entity updates.

The former one-based producer would fail the focused page test because it
asserts the exact payload `0, 1, 2`. Independently, a one-based backend payload
`1..n` is rejected because order `0` is missing.

## Focused Verification

- `npm test -- src/pages/subscriber/PlaylistEditPage.test.tsx`: PASS, 1/1.
- Existing Playlist editor workflow Vitest by test name: PASS, 1/1 with 32
  unrelated tests skipped.
- Playlist service and controller JUnit classes: PASS, 44/44; Gradle
  `BUILD SUCCESSFUL in 41s`.
- Static review confirmed the API and use-case documents both specify
  zero-based contiguous `0..n-1` ordering.
- The repair changes only an index value, test expectations, a focused test,
  and current-state documentation. It adds no request, remote-call loop, or
  query behavior.

No full suite, browser/API deployment test, MySQL persistence test, schema/data
action, external call, secret inspection, ZIP access, commit, push, or staging
was performed.

## Changed Files

- `deliverables/user/WI-20260809-ATS-015-summary.md`
- `deliverables/agent/WI-20260809-ATS-015-evidence-pack.md`
- `deliverables/user/WI-20260808-ATS-029-summary.md` - final disposition only
- `deliverables/agent/WI-20260808-ATS-029-evidence-pack.md` - final disposition
  only

## Residual Risks And Rollback

- The focused frontend tests mock the HTTP boundary, and the backend tests do
  not replace deployed browser/API or MySQL verification. This is accepted
  residual verification scope, not an evidenced finding.
- The service suite does not contain a test named specifically for a complete
  one-based `1..n` payload. The former producer is directly protected by the
  exact page payload regression, while the service's complete-set validation
  deterministically rejects the missing zero.
- Rollback is documentation-only: remove the two WI-015 deliverables and
  restore only the WI-029 disposition hunks. Do not replace whole shared files
  or alter implementation changes.
