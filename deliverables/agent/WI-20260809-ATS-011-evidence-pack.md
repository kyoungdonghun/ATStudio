# WI-20260809-ATS-011 Evidence Pack

## Identity

| Field | Value |
|---|---|
| Work item | `WI-20260809-ATS-011` |
| REQ | `REQ-20260808-ATS-004` |
| Role | `qa-fe` |
| Date | 2026-08-09 |
| Status | Completed with focused verification |
| Authoritative input | `deliverables/agent/WI-20260809-ATS-011-handoff.md` |
| Reviewer evidence | `deliverables/user/WI-20260809-ATS-007-summary.md` |
| Depends on | `WI-20260809-ATS-007`, `WI-20260809-ATS-010` |
| Blocks | `WI-20260808-ATS-029` independent rerun |
| Branch policy | Current branch only; no commit or push |

## Outcome

All three required frontend repairs are implemented. The final combined focused
Vitest run passed 144 tests across 8 files, typecheck passed, lint passed with
zero warnings, and changed frontend code passed Prettier. Documentation and WI
deliverables were then written under an immediate close instruction; they did
not receive a subsequent Prettier or verification rerun.

## Requirement Evidence

| Requirement | Implementation evidence | Regression evidence |
|---|---|---|
| Accept omitted nullable aggregate fields | `playableTrack.ts` makes nullable wire fields optional, preserves positive safe-integer ID validation, and normalizes thumbnail/waveform to `null`; Album, Playlist, and Like source interfaces match the omitted-key wire shape | `playableTrack.test.ts`, aggregate page coverage, and `playerPersistence.test.ts` use API-shaped omitted keys through mapping, context, play, queue, persistence, and reload |
| Replace stale duration on Track switch | `playerStore.play` writes selected duration with `currentTrack` and `currentTime = 0`; existing metadata handling can refine the current source | `playerStore.test.ts` and `PlayerBar.test.tsx` assert immediate duration, zero progress, seek targets, and desktop range ARIA scale before replacement metadata |
| Load taxonomies independently | `TrackListPage.tsx` stores status and tags per Tag type and launches four independent requests without one aggregate promise | `TrackListPage.test.tsx` rejects Mood while Genre, Instrument, and Usage recover independently and remain usable |
| Preserve absent active values | Active URL values merge with fetched options; type/source-scoped synthetic keys prevent API/fallback collisions | Tests omit selected values from all four responses and assert visible/removable row and modal fallback chips |
| Preserve raw Usage values | URL and API state use the unmodified string; only the display formatter adds `#` | Tests assert raw Usage in Track API parameters while the rendered chip uses `#` |
| Provide bounded recovery | Only the failed type displays manual retry; an in-flight token suppresses duplicate retry clicks and generations reject stale results | Recovery test double-clicks Mood retry, observes only one new Mood request, no new request for other types, and successful error clearance |

## Taxonomy Failure Behavior

| State | Visible behavior | Request behavior |
|---|---|---|
| Loading | Per-type status is announced; active URL fallback chips remain visible | One initial request per taxonomy |
| Ready, selected value returned | API-backed option renders active and removable | Raw repeated URL values continue to drive Track requests |
| Ready, selected value omitted | Stable URL fallback option renders active and removable | No synthetic value transformation reaches the API |
| Error | Only that row exposes an alert and retry; other successful options remain visible; active values remain visible | No automatic retry |
| Manual retry | Failed type returns to loading while its active fallback remains | Duplicate clicks are ignored while in flight; other taxonomies are not re-requested |
| Remove/reset | Chip disappears after URL state changes | Removed arrays disappear from subsequent Track API parameters |

API-backed option keys use `tag:<type>:<id>:<length>:<value>`. URL fallback
keys use `url:<type>:<length>:<value>`. The source and type namespaces prevent
cross-type and API/fallback collisions while keeping keys deterministic.

## Changed Files

| Area | Paths |
|---|---|
| PlayableTrack implementation | `frontend/src/utils/playableTrack.ts`; `frontend/src/api/albums.ts`; `frontend/src/api/playlists.ts`; `frontend/src/types/index.ts` |
| Player implementation | `frontend/src/store/playerStore.ts` |
| Taxonomy implementation | `frontend/src/components/filter/TagFilterModal.tsx`; `frontend/src/pages/public/TrackListPage.tsx`; `frontend/src/pages/public/TrackListPage.module.css` |
| Mapper/store/player tests | `frontend/src/utils/playableTrack.test.ts`; `frontend/src/store/playerStore.test.ts`; `frontend/src/store/playerPersistence.test.ts`; `frontend/src/layouts/PlayerBar.test.tsx` |
| Taxonomy/component tests | `frontend/src/components/catalogComponents.test.tsx`; `frontend/src/pages/public/TrackListPage.test.tsx` |
| Aggregate-page tests | `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx`; `frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx` |
| Current-state docs | `docs/SR/SR-100.md`; `docs/SR/SR-101.md`; `docs/ui/atstudio-front-list.md`; `docs/ui/screen-flow.md` |
| WI outputs | `deliverables/user/WI-20260809-ATS-011-summary.md`; `deliverables/agent/WI-20260809-ATS-011-evidence-pack.md` |

The repository began with a substantial dirty worktree, including pre-existing
changes in some touched files. No unrelated changes were reverted, reformatted,
staged, committed, or pushed.

## Verification Ledger

### Mapper and aggregate regressions

An interim focused mapper/aggregate run passed 3 files and 65 tests.

### Duration and PlayerBar regressions

The first run had one test failure: an unscoped test query matched both desktop
and mobile waveform controls. This was a test-selector ambiguity, not a product
assertion failure. Scoping to the desktop range resolved it; the rerun passed 3
files and 63 tests.

### Taxonomy regressions

The focused TrackList and catalog-component run passed 2 files and 16 tests.

### Final combined focused Vitest

```text
npm test -- src/utils/playableTrack.test.ts src/store/playerStore.test.ts src/store/playerPersistence.test.ts src/layouts/PlayerBar.test.tsx src/pages/public/TrackListPage.test.tsx src/components/catalogComponents.test.tsx src/test/coverage/publicAuthShell.coverage.test.tsx src/test/coverage/adminSubscriberPages.coverage.test.tsx
```

- Result: PASS.
- Runner: Vitest 4.1.4.
- Results: 8 files, 144 tests, 0 failures.
- Duration: 11.87 seconds.

### Typecheck

```text
npm run typecheck
```

- Initial result: failed with five `TS2550` errors because a new test helper used
  `Array.prototype.at`, which is unavailable in the configured TypeScript
  library target.
- Correction: use indexed access for the final mock call.
- Final result: PASS.

### Lint

```text
npm run lint
```

- Initial result: two React hook dependency warnings caused by cleanup reads of
  mutable request refs.
- Correction: capture the request maps in the effect before constructing its
  cleanup.
- Final result: PASS, 0 warnings.

### Prettier

```text
npx prettier --check src/utils/playableTrack.ts src/utils/playableTrack.test.ts src/api/albums.ts src/api/playlists.ts src/types/index.ts src/store/playerStore.ts src/store/playerStore.test.ts src/store/playerPersistence.test.ts src/layouts/PlayerBar.test.tsx src/components/filter/TagFilterModal.tsx src/components/catalogComponents.test.tsx src/pages/public/TrackListPage.tsx src/pages/public/TrackListPage.test.tsx src/pages/public/TrackListPage.module.css src/test/coverage/publicAuthShell.coverage.test.tsx src/test/coverage/adminSubscriberPages.coverage.test.tsx
```

- Initial result: `TrackListPage.tsx` did not match formatting.
- Correction: format that file with the repository Prettier configuration.
- Final changed-code result: PASS, all 16 files matched.

## Not Run Or Incomplete

- No second test/typecheck/lint cycle was run after current-state docs and the WI
  deliverables were written. These files do not affect frontend compilation or
  runtime behavior.
- The four changed documentation files and two new WI deliverables did not
  receive a final Prettier check because the user instructed an immediate close
  based on the last completed verification.
- No full test suite, full coverage, or production build was run, as explicitly
  excluded from the work item.
- No browser acceptance, native media-event throttling, or live network failure
  exercise was run.

## Risks

1. Real-browser media event ordering may expose behavior not represented by the
   jsdom-focused PlayerBar tests.
2. Per-type taxonomy generations ignore stale results but do not cancel the
   underlying transport request.
3. Stale or deleted URL Tag values intentionally remain active and visible until
   removed or reset; the backend Track result remains authoritative.
4. Co-located predecessor changes require hunk-scoped rollback rather than
   replacing whole files.

## Rollback

Apply an inverse patch only to WI-011 hunks in the listed implementation, test,
and current-state documentation files. Remove these two WI-011 deliverables if
the work item is withdrawn. Preserve all unrelated dirty-worktree content. No
backend, database, schema, dependency, or external-state rollback is required.

## WI-029 Status

WI-011 resolves and regression-tests the three frontend MAJOR findings from
WI-007. Combined with WI-010's backend repairs, this clears the implementation
block for the independent `WI-20260808-ATS-029` reviewer rerun. WI-029 remains
pending and must not be marked complete until that independent review records
its disposition.
