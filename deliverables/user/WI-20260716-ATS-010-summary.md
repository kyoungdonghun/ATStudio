# WI-20260716-ATS-010 Summary

## Outcome

Completed the combined frontend reliability and accessibility integration review on `codex/p1-acceptance-hardening`. The affected views now keep the newest request result, distinguish legitimate empty states from failures, preserve safe login return paths, synchronize saved profile state, and expose retryable player/load failures without changing ATStudio product policy.

The integration and final MA cross-review found and fixed four cross-slice defects:

- Available Track filter tags had no stale-response fence even though the main Track request did. They now use cancellation plus a monotonic generation check, with a delayed-response regression test.
- `AddToPlaylistModal` restarted its playlist load when an inline parent callback changed identity. Callback refs now keep the modal lifecycle stable across parent rerenders, with regression coverage.
- SubscriberRoute relied on AbortController without a monotonic generation fence. Signal-ignoring stale success/failure is now fenced in `then`/`catch`/`finally`, including unauthenticated transitions and cleanup.
- Profile fields were hydrated in two React phases, so a fast subscription lookup could expose the edit form before phone/job hydration and overwrite immediate test/user input. Initial and saved profile fields now update in one state batch, and the regression test waits for the complete initial form state.

## Behavior Changes

- Track list, Track available-filter tags, and Admin Payment Operations reads use latest-request-wins. Superseded requests cannot overwrite the current data, pagination, loading, or error state.
- Core load surfaces distinguish initial loading, successful data, legitimate empty/inactive state, classified client/domain failure, infrastructure failure, and superseded/cancelled work. Retry remains guarded and idempotent.
- Subscriber access redirects only for structured `403 + NO_ACTIVE_SUBSCRIPTION`. Network, timeout, server, and unrelated authorization failures stay visible and retryable without granting subscriber access.
- Login preserves a safe internal pathname and query. External, protocol-relative, malformed, and privileged return targets fall back safely.
- Initial profile fields hydrate together, and a successful profile save updates the page fields plus the active/persisted authentication user together.
- `/playlists/new` reuses the existing playlist creation modal rather than introducing a second creation flow.
- Playlist modal close/reopen generations invalidate stale loads and delayed close timers; parent rerenders alone do not restart the query.
- Player failures and stalled playback are visible and retryable. Toast, pagination, header search, modal focus handling, and player controls now have the focused semantic, keyboard, live-region, and focus-return behavior covered by tests.

## Unchanged Product Policy

- Public listeners still receive the full Track stream from `/api/tracks/{id}/stream`; no preview duration, preview gate, or listening restriction was introduced.
- Subscription, daily download quota, license, and payment gates are unchanged.
- ADMIN Payment Operations and USER-only payment callback routing remain separated.
- Company certification apply/status routes remain USER + BUSINESS only.
- Whitelist channel registration remains authenticated and its registration action retains the existing subscription-limit check.
- The frozen client-demo branch and Cloudflare runtime were not modified.

## Documentation Updated

- `docs/ui/screen-flow.md`: safe login return, strict subscriber error classification, Track search/state behavior, public full listening, profile synchronization, and `/playlists/new` flow.
- `docs/ui/atstudio-front-list.md`: Track keyword contract (`title + USAGE`), latest-request-wins, and existing playlist-modal reuse.
- `docs/design/usecase/sound-playlist.md`: direct create-route behavior and stable modal lifecycle.
- `docs/design/remaining-remediation-design-20260716.md`: WI-010 implementation status and unchanged product boundaries.

## Verification

- Focused integration regressions: 2 files, 13 tests passed.
- SubscriberRoute final focused suite: 1 file, 9 tests passed.
- ProfilePage flaky regression: 10 consecutive runs, 5 tests per run, 50/50 tests passed.
- Full Vitest suite after all final corrections: 38 files, 180 tests passed.
- TypeScript typecheck: passed; the pre-existing tracked `frontend/tsconfig.tsbuildinfo` worktree change was not modified by the verification run.
- Affected ESLint: 46 TypeScript/TSX files passed with zero warnings or errors.
- Final targeted ESLint: 2 SubscriberRoute files and 2 ProfilePage files passed with zero warnings or errors.
- Changed-file Prettier: 56 WI-010 frontend files passed after formatting four affected files.
- Final targeted Prettier: the 2 SubscriberRoute files and 2 ProfilePage files passed.
- Vite production build: passed; 262 modules transformed.
- Documentation validation: passed for Tier 0 documents, internal links, traceability IDs, and index integrity.
- `git diff --check`: passed.

## Residual Manual Risks

- A real browser should still exercise Axios cancellation under throttled requests, nested modal focus return, mobile player expansion/focus, seek-control keyboard behavior, and real media `stalled`/`waiting` events.
- Screen-reader announcement quality should be checked with an actual assistive technology; automated DOM assertions verify semantics but not the final spoken experience.
- Social OAuth callback return behavior is not part of the password-login `returnTo` change and remains outside WI-010.
- Repository-wide Prettier cleanup and executable coverage baselines remain separate approved follow-up work; broad browser regression remains owned by WI-014.

## Scope Preservation

No backend, database, schema, secret, runtime, client-demo, or product-policy change was made for WI-010. Existing WI-005 through WI-009 edits and unrelated dirty-worktree files were preserved. Nothing was staged or committed.
