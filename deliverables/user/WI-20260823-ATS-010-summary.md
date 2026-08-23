# WI-20260823-ATS-010 Summary

## Result

Implementation and automated verification are complete. Browser acceptance is
not closed: the final code was not re-run through the real-browser refresh
scenario before this WI was concluded.

## Diagnosis And Change

`authStore.clearSession()` cleared authentication and user-specific state, but
also called `playerStore.clearQueue()`. `clearQueue()` intentionally persists
an empty player state, so a session expiry or logout could erase independent
public playback before the PlayerBar hydration request ran.

The fix removes that cross-store call. Auth cleanup still removes tokens and
resets track and album like state; public playback remains intact. Explicit
`clearQueue()` behavior is unchanged.

An additional direct defect was found in the same refresh path: `pause()` did
not persist the media's current position. It now clamps and persists the
current media time when a track is paused, preserving the requested seek
position for hydration.

## Verification

- Focused Vitest: 4 files, 100 tests passed.
- Typecheck, ESLint, Prettier, and production build passed.
- `POST /api/tracks/batch` for public track ID 4 returned HTTP 200 with one
  playable record; no backend change was made.
- `git diff --check` passed for the four changed source/test files.

## Browser Follow-up

The in-app browser confirmed that public track 4 enters the populated
PlayerBar state before refresh. An earlier refresh attempt still rendered the
empty PlayerBar, but the WI constraint prohibited inspecting storage values
and the final pause-persistence change was not re-run in that browser before
conclusion. Re-run `/tracks/4` -> play -> pause near 0.7 seconds -> reload in
the target acceptance browser before closing the browser DoD.

## Risk And Rollback

Risk is limited to public playback now surviving auth session cleanup, which
matches the approved public-listening policy. Roll back by reverting this WI's
four source/test changes; explicit queue clearing continues to erase persisted
playback state as before.
