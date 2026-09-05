# WI-20260823-ATS-002 Summary

## Independent Verification Verdict

**FAIL - remediation is required before WI-20260823-ATS-004.**

The review was read-only on `codex/v1-release-rehearsal-fixes`. Product code,
configuration, schema, data, storage, secrets, and the client-acceptance worktree
were not modified or accessed. The pre-existing modifications to
`frontend/src/pages/public/HomePage.tsx` and
`frontend/src/pages/public/HomePage.test.tsx` are explicitly excluded from this WI.

## Requirement Evidence

| Requirement | Status | Evidence |
| --- | --- | --- |
| Multiple public mood selections remain visible and use repeated query values | PASS | Focused catalog tests passed. In the public local browser, selecting `mood01` then `mood02` produced `?mood=mood01&mood=mood02&page=1`; both stayed selected and `mood03` remained visible. |
| Nickname internal spaces and edge trimming are consistent | PASS | Frontend normalization/validation, auth availability calls, backend registration, profile update, profile completion, availability, duplicate lookup, and persistence paths were reviewed. Focused frontend tests and forced `UserServiceTest` passed. |
| BUSINESS uses existing `companyName`; INDIVIDUAL keeps `job` | PASS | Source and focused registration, social completion, profile, and admin tests support the existing-field contract. The broad frontend suite has one stale coverage expectation that must be updated. |
| Playlist Play all starts the first Track and preserves order | PASS | `PlaylistDetailPage` passes ordered Tracks to the existing `playerStore.playAll`, which replaces the queue and plays index zero. Focused playlist tests passed. |
| Direct Likes action is present beside history in desktop and expanded mobile PlayerBar | FAIL | The entry is present in all reviewed layouts, but the changed drawer resets a manually selected Likes tab to Playlists on reopen. The existing async-likes regression test now fails independently. |
| Question FAB clears desktop/mobile PlayerBar | BLOCKED | Component test and CSS show PlayerBar and expanded-mobile clearance. Browser confirmation is unavailable without a protected session, which this WI must not create or use. |
| Safe local billing-key example has keyring shape without a real secret | PASS | The changed example is limited to the existing keyring shape. No ignored local configuration or secret was read, logged, or changed. |
| Required quality gates | FAIL | Typecheck, lint, focused tests, frontend build, backend targeted/full tests, backend build, and diff whitespace check passed. Changed-file Prettier and the broad frontend suite failed. |

## Confirmed Remediation Items

1. **P2 - Likes drawer loses the selected tab on reopen.**
   `PlaylistDrawer.tsx:67-71` applies the default `initialTab` whenever it opens.
   After a user selects Likes, closes the generic drawer, and reopens it, the default
   `playlists` tab replaces the prior Likes state. Reproduce with:
   `npm run test -- src/components/player/playerComponents.test.tsx -t "prevents an earlier likes response from populating a reopened drawer"`.
   The result is one failing test at `playerComponents.test.tsx:607`.

2. **P2 - Broad frontend coverage test still asserts the replaced BUSINESS label.**
   `publicAuthShell.coverage.test.tsx:620-621` expects the previous company-only
   error and label, while `SignupPage.tsx:105-106,374-380` now correctly exposes
   the approved combined descriptor. Update the test expectation as part of the
   implementation remediation; do not revert the approved UI wording.

3. **P2 - Changed-file formatting gate fails.**
   Prettier reports `SignupPage.test.tsx`, `SocialCompleteProfilePage.test.tsx`,
   `TrackListPage.tsx`, and `ProfilePage.test.tsx`. No formatting change was made in
   this WI.

## Scope, Runtime, and Browser Limits

- The broad HomePage failure is excluded from this WI because both related files were
  already modified before WI-001. It was not evaluated as a WI-001 defect.
- The public catalog loaded Track metadata but displayed its existing media-cover
  fallback. No audio was played. This is the known development media/storage mismatch,
  not a source regression in this WI.
- Registration, social completion, profile save, playlist playback, Likes retrieval,
  and FAB visual placement remain user-verification items because they require a
  protected session or a mutation and were intentionally not exercised.

## Documentation Handoff for DocOps

- Update `docs/design/usecase/user-info.md` and `docs/design/api-spec.md` to state
  edge-trimmed/internal-space nickname normalization across registration, completion,
  profile update, and availability; describe BUSINESS `companyName` as the one
  combined `Company name or industry` descriptor, with no new DB/API field.
- Update `docs/design/usecase/sound-playlist.md`, `docs/ui/atstudio-front-list.md`,
  and `docs/ui/screen-flow.md` for Play all queue semantics, direct Likes access next
  to history, and Question FAB clearance.
- Keep the billing-key documentation limited to the safe keyring terminology and
  environment-backed placeholders. Run documentation validation only after WI-003.
