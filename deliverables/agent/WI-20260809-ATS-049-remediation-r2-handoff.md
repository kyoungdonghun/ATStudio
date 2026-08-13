# Remediation R2 Handoff: WI-20260809-ATS-049

[WI HEADER]

- WI ID: `WI-20260809-ATS-049-REM-R2`
- REQ: `REQ-20260809-ATS-001` (`approved`)
- Agent: `se`
- Depends On: `WI-20260809-ATS-049-QA-INTEG-R2`
- Blocks: final WI-049 QA closure and full gates

[WI SUMMARY]

## Why

Close the single new P2 `QA-049-R2-001`: a membership mutation started on Album A must not launch or commit follow-up reads, feedback, fences, or state after the component route owner changes to Album B or unmounts.

## Required Work

- Introduce a bounded page/route owner generation tied to the canonical Album ID and component lifetime.
- Snapshot immutable owner at add/remove/reorder start and revalidate after every awaited mutation/read before any local fence, feedback, refresh, or state commit.
- Make `refetchTracks` require both request-generation ownership and current page/Album ownership.
- On route switch or unmount, retire prior mutations/read continuations without attempting a stale follow-up read or emitting retired feedback.
- Add exact tests for pending add on Album 11 -> route switch Album 12 -> add resolves: zero Album-11 follow-up read, zero retired feedback/state/fence commit, Album 12 projection remains authoritative, and no wrong-Album remove path appears. Cover unmount if practical.
- Preserve all prior QA closures and WI-038 reorder behavior.
- Update current-state docs/evidence/summary only as needed; preserve both historical QA FAIL files.

## Constraints

- No API/schema/dependency/product-policy changes and no unrelated refactor.
- No live/external/data/secret/protected-output/Git actions.
- Run focused 8-file suite, typecheck, lint, changed-file Prettier, build, docs validation, diff check.

[INPUT POINTERS]

- `deliverables/agent/WI-20260809-ATS-049-qa-integ-rereview-result.md`
- `deliverables/agent/WI-20260809-ATS-049-remediation-handoff.md`
- `deliverables/agent/WI-20260809-ATS-049-evidence-pack.md`
- `deliverables/user/WI-20260809-ATS-049-summary.md`
- `frontend/src/pages/creator/AlbumEditPage.tsx`
- `frontend/src/pages/creator/AlbumEditPage.test.tsx`
- `docs/design/usecase/sound-album.md`
- Tier 0 and quality documents from the original handoff

[OUTPUT CONTRACT]

- Scoped code/test/doc/evidence/summary corrections.
- Exact red/green counterexample and final gates in the report.
- No commit/push.
