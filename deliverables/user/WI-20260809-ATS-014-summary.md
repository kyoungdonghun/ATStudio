# WI-20260809-ATS-014 Playlist Reorder Contract Repair Summary

## Outcome

WI-20260809-ATS-014 is implemented and focused verification is complete. The
Playlist editor now submits reordered active Tracks with zero-based contiguous
`trackOrder` values that match the backend validation contract.

## Implemented Behavior

- `PlaylistEditPage` maps the first reordered Track to `trackOrder: 0` and each
  following Track to its zero-based array index.
- A focused page-level test loads a non-empty three-Track Playlist, moves the
  first Track down, submits, and asserts the exact `reorderTracks` payload:

```typescript
[
  { trackId: 102, trackOrder: 0 },
  { trackId: 101, trackOrder: 1 },
  { trackId: 103, trackOrder: 2 },
];
```

- The existing broader Playlist editor page test now expects the same
  zero-based contract.
- The Playlist API current-state document now explicitly states `0..n-1`.
  `sound-playlist.md` already documented orders from 0 through n-1, so it was
  intentionally left unchanged.

## Changed Files

- `frontend/src/pages/subscriber/PlaylistEditPage.tsx`
- `frontend/src/pages/subscriber/PlaylistEditPage.test.tsx`
- `frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx`
- `docs/design/api-spec.md`
- `deliverables/user/WI-20260809-ATS-014-summary.md`
- `deliverables/agent/WI-20260809-ATS-014-evidence-pack.md`

## Focused Verification

- RED focused Vitest confirmed the defect: the new test expected `0, 1, 2`
  while the page submitted `1, 2, 3`.
- Final focused Vitest: 1 file passed; 1 test passed.
- Existing Playlist editor page test: 1 test passed; 32 unrelated tests were
  skipped by the test-name filter.
- `npm run typecheck`: passed with zero TypeScript errors.
- `npm run lint`: passed with zero ESLint errors or warnings.
- Scoped Prettier check for the three affected TS/TSX files: passed.
- Scoped `git diff --check` for the affected tracked files: passed.
- The changed API document retains pre-existing whole-file Prettier differences
  and was not globally reformatted over co-located dirty work.

No full suite, coverage, build, backend test, schema/data action, external call,
secret or ZIP access, commit, or push was performed.

## Risks And Rollback

- Focused tests mock the API boundary and do not replace deployed browser/API
  integration verification.
- Rollback must inverse only the WI-014 hunks in the listed files and remove the
  two WI-014 deliverables. Whole-file replacement is unsafe because the current
  worktree contains pre-existing dirty changes.

## WI-20260808-ATS-029 Status

WI-014 repairs and regression-tests MAJOR-001 from the WI-029 final re-review.
The implementation-side blocker is ready for an independent reviewer rerun;
this summary does not mark WI-029 approved or complete.
