# WI-20260809-ATS-011 Summary

Completed on 2026-08-09 with focused frontend verification.

This work item repairs the three frontend MAJOR findings reported by
`WI-20260809-ATS-007`: omitted nullable aggregate fields, stale player duration
on Track switches, and all-or-nothing Track-list taxonomy loading. The existing
dirty worktree was preserved. No backend, schema, retained-data, dependency,
external-service, secret/ZIP, commit, or push operation was performed.

## Delivered Behavior

### PlayableTrack wire normalization

- Aggregate source interfaces now allow omitted `thumbnail`, `thumbnailUrl`,
  and `waveformData` keys where the API may omit null members.
- `toPlayableTrack` continues to require a positive safe integer ID.
- Omitted and explicit-null thumbnail or waveform fields normalize to explicit
  `null` in the shared PlayableTrack model.
- API-shaped Album, Playlist, and Like fixtures cover mapper, component play and
  context publication, queue persistence, and persisted-state reload paths.

### Immediate duration replacement

- `playerStore.play(track)` now sets `currentTrack`, `currentTime = 0`, and the
  selected Track's `duration` in one Zustand state transition.
- Current-source browser metadata may still replace the declared duration.
- Store and PlayerBar tests prove an immediate Track switch exposes no previous
  duration, progress ratio, seek target, range maximum, range value, or range
  ARIA text.

### Independent taxonomy state

- Genre, Mood, Instrument, and Usage load independently with per-type loading,
  ready, and error state. One rejection does not clear or suppress the other
  successful taxonomies.
- A failed type renders its own accessible error and explicit manual retry.
  Initial loading occurs once, retries are user-triggered, and duplicate retry
  clicks are ignored while that type already has a request in flight.
- Active repeated URL values are merged into the visible option model even when
  the taxonomy request fails or omits those values. The fallback keys include
  source, type, value length, and value; API-backed keys are kept in a separate
  namespace.
- Fallback chips remain visible and removable in the filter rows and modal.
  Individual removal and reset update the URL, and subsequent Track requests no
  longer contain the removed arrays.
- Usage values remain raw in URL and Track API parameters. `#` is added only by
  the rendering formatter.
- Per-type request generations prevent stale taxonomy responses from replacing
  newer state. No per-Track request fan-out or automatic retry loop was added.

## Changed Files

Production:

- `frontend/src/utils/playableTrack.ts`
- `frontend/src/api/albums.ts`
- `frontend/src/api/playlists.ts`
- `frontend/src/types/index.ts`
- `frontend/src/store/playerStore.ts`
- `frontend/src/components/filter/TagFilterModal.tsx`
- `frontend/src/pages/public/TrackListPage.tsx`
- `frontend/src/pages/public/TrackListPage.module.css`

Tests:

- `frontend/src/utils/playableTrack.test.ts`
- `frontend/src/store/playerStore.test.ts`
- `frontend/src/store/playerPersistence.test.ts`
- `frontend/src/layouts/PlayerBar.test.tsx`
- `frontend/src/components/catalogComponents.test.tsx`
- `frontend/src/pages/public/TrackListPage.test.tsx`
- `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx`
- `frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx`

Current-state documentation:

- `docs/SR/SR-100.md`
- `docs/SR/SR-101.md`
- `docs/ui/atstudio-front-list.md`
- `docs/ui/screen-flow.md`

Deliverables:

- `deliverables/user/WI-20260809-ATS-011-summary.md`
- `deliverables/agent/WI-20260809-ATS-011-evidence-pack.md`

Some listed files already contained unrelated or predecessor-WI changes in the
dirty worktree. This list identifies WI-011 touch points and does not attribute
every existing diff hunk to this work item.

## Verification

Final combined focused Vitest command from `frontend`:

```text
npm test -- src/utils/playableTrack.test.ts src/store/playerStore.test.ts src/store/playerPersistence.test.ts src/layouts/PlayerBar.test.tsx src/pages/public/TrackListPage.test.tsx src/components/catalogComponents.test.tsx src/test/coverage/publicAuthShell.coverage.test.tsx src/test/coverage/adminSubscriberPages.coverage.test.tsx
```

Result: PASS, Vitest 4.1.4, 8 files, 144 tests, 0 failures, 11.87 seconds.

Additional final results:

- `npm run typecheck`: PASS.
- `npm run lint`: PASS with 0 warnings.
- Prettier check for the 16 changed frontend code/test/style files: PASS; all
  files matched Prettier formatting.

Corrections made during verification:

- The first new PlayerBar switch test failed because a global waveform query
  matched both desktop and mobile controls. The test was scoped to the desktop
  range control; the focused rerun passed 3 files and 63 tests.
- The first typecheck reported five `TS2550` errors because the new test helper
  used `Array.prototype.at`, which is outside the configured library target.
  Index access replaced it; the final typecheck passed.
- The first lint run reported two React hook dependency warnings for cleanup
  reads of mutable refs. The request maps are now captured in the effect; the
  final lint run passed with zero warnings.
- The first Prettier check identified only `TrackListPage.tsx`. It was formatted,
  and the final changed-code Prettier check passed.

Not run after the user's immediate close instruction:

- A second verification cycle after the documentation and WI deliverables were
  written.
- Prettier check covering the four changed documentation files and these two new
  deliverables.
- Full coverage, full test suite, production build, or browser acceptance. The
  first three were outside the requested scope; browser acceptance remains a
  reviewer/acceptance activity.

## Risks And Rollback

- Vitest verifies the immediate UI/store contract, but real-browser native
  media ordering and throttled network behavior were not exercised.
- Taxonomy requests use per-type generation fencing rather than transport-level
  cancellation. Superseded results are ignored, but the underlying request may
  still finish.
- A deleted or stale URL Tag intentionally stays visible and active until the
  user removes or resets it; the Track API remains authoritative for results.
- Rollback must inverse only the WI-011 hunks in the listed files and remove the
  two WI-011 deliverables, while preserving all unrelated dirty changes. No
  database, schema, dependency, or external-state rollback is required.

## WI-029 Status

WI-011 implements and regression-tests all three frontend findings that blocked
`WI-20260808-ATS-029`. Together with the WI-010 backend repairs, WI-029 is
unblocked for its required independent reviewer rerun. WI-029 is not completed
by this evidence and remains pending reviewer disposition.
