# WI-20260823-ATS-001 Summary

## Summary

Implemented the approved release-rehearsal feedback fixes on
`codex/v1-release-rehearsal-fixes` without changing the client-acceptance worktree,
database, external providers, payment/refund behavior, mail, storage, or policy-owned
playlist/repeat/default-plan behavior.

## Changed Behavior

- Public catalog mood tags stay selectable after another mood narrows the result set.
  Multiple selections remain repeated `mood` URL and API values.
- The question list now exposes a dedicated responsive `New question` floating action
  button. It reserves space above the PlayerBar, including the expanded mobile player.
- BUSINESS registration and profile flows use one required `Company name or industry`
  field backed by the existing `companyName`. INDIVIDUAL users retain `job`.
- Nicknames accept internal ASCII spaces, preserve those spaces, and trim leading and
  trailing whitespace before client validation, availability checks, duplicate checks,
  and persistence.
- Playlist detail adds `Play all` through the existing player `playAll` behavior while
  retaining the separate `Add all to queue` action.
- Desktop and expanded mobile PlayerBar place a direct Likes action next to playback
  history. It opens the existing playlist drawer on its Likes tab.
- The local billing-key example describes the existing `active-key-id` keyring shape
  with an environment-backed placeholder only. No billing key or other secret was added.

## Validation

- Focused frontend tests: 9 files and 131 tests passed.
- Focused backend `UserServiceTest`: passed.
- `git diff --check`: passed.

## User Verification Notes

- In a browser, select two moods in sequence and confirm both selected tags remain
  visible and the URL contains repeated `mood` parameters.
- Check the question FAB at desktop width, normal mobile PlayerBar, and expanded mobile
  PlayerBar for clearance and reachability.
- Check BUSINESS and INDIVIDUAL registration/profile presentations, then verify a
  nickname such as `Creator Team` is retained while `  Creator Team  ` is stored as
  `Creator Team`.
- Check playlist play-all ordering and the Likes action in both desktop and expanded
  mobile player layouts.

## Residual Verification

The user requested an immediate focused-test wrap-up. Full frontend typecheck, lint,
Prettier, browser visual checks, and broad integration suites were intentionally left
for independent verification; they are not known failures.

## Risks

- Responsive FAB clearance is covered by CSS and component tests, but needs the listed
  browser visual check against the locally running PlayerBar.
- Existing unrelated dirty `HomePage` changes and pre-existing untracked deliverables,
  output, and scripts remain untouched and are not part of this WI.
