# WI-20260809-ATS-016 Current-State Documentation Audit Summary

## Outcome

WI-016 completed the documentation-only consistency audit after WI-008 through
WI-015. Four evidenced current-state contradictions were corrected with narrow
hunks. Historical SR/REQ/WI evidence, implementation, schema, data, secrets,
ZIP files, branches, commits, and remotes were not changed.

## Corrected Statements

| Document                                | Correction                                                                                                                                                                                                                    | Current evidence                                                                              |
| --------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------- |
| `docs/SR/SR-97.md`                      | Replaced the stale claim that every rejected mutation performs reconciliation. HTTP 4xx is definite; network/no-response/timeout/5xx is ambiguous; request 204 remains fenced and read-only retry never replays the mutation. | `UserSubscriptionCorrectionModal.tsx`; focused tests in `UserSubscriptionManagePage.test.tsx` |
| `docs/ui/atstudio-front-list.md`        | Replaced the implication of browser Seoul-date comparison with the actual required/calendar-valid local check and server-owned Seoul business-date validation.                                                                | `UserSubscriptionCorrectionModal.tsx`; `AdminSubscriptionCorrectionService.java`              |
| `docs/design/usecase/sound-album.md`    | Corrected the aggregate playback contract to allow API omission of nullable thumbnail/waveform members and frontend normalization to explicit `null`, while retaining real duration.                                          | `AlbumTrackItemResponse.java`; `playableTrack.ts`                                             |
| `docs/design/usecase/sound-playlist.md` | Applied the same nullable media contract to active Playlist detail rows.                                                                                                                                                      | `PlaylistTrackItemResponse.java`; `playableTrack.ts`                                          |

The Playlist reorder contract required no further repair: the API specification
and use case already state active membership plus zero-based contiguous
`0..n-1`, matching `PlaylistEditPage.tsx` and `PlaylistService.java`.

## Audit Confirmations

- `SR-93` through `SR-101` remain `OPEN` for their documented production,
  browser, operational, backfill, or recovery boundaries. Completed focused
  acceptance items remain checked; no SR status was promoted.
- The SR index remains 100 items: 82 DONE, 15 OPEN, 2 NOT CONFIRMED, and 1
  DROPPED.
- Documentation category counts remain 201 under the documented counting rule.
- Current source counts match the indexes: 25 controllers / 144 mappings, 41
  tables / 41 entities, 53 page UIs, and 22 Modal occurrences in 17 files.
- WI-028 and WI-029 final dispositions remain PASS. Their accepted residuals
  were not reopened.

## Verification

- `python .agents/skills/validate-docs/scripts/validate_docs.py`: PASS.
- `git diff --check`: PASS; line-ending normalization warnings only.
- Scoped Prettier for the two new WI-016 deliverables: PASS.

No runtime test or build was needed for this documentation-only audit. Existing
whole-file Markdown Prettier debt in shared dirty documents was not reformatted
and was not expanded into unrelated churn.

## Rollback And WI-030

Rollback is limited to the four WI-016 documentation hunks and removal of the
two WI-016 deliverables. All predecessor and unrelated dirty work must remain.

`WI-20260808-ATS-030` is unblocked from the WI-016 documentation-consistency
perspective. This statement does not claim production readiness or perform any
release/Git action.
