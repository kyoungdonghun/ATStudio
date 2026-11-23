# Participant music library UI

REQ-20261123-ATS-003 · WI-20261123-ATS-014 · local React implementation and verification.

## Scope and evidence

- Existing catalog track row and detail actions now work for signed-in pilot USER accounts. Pure listening and unauthenticated catalog requests keep their original behavior.
- `frontend/src/router/index.tsx`, `config/appMode.ts`, `utils/loginReturn.ts` and `layouts/Header.tsx` connect the four saved-music routes, their exact authenticated requests, desktop links and mobile menu. Album likes and selling routes stay outside the pilot.
- `pages/subscriber/PlaylistListPage.tsx` uses the narrow capacity endpoint. The empty track-add dialog links to first playlist creation. Creation and editing use title and description in the pilot. Playlist image uploads are available in the selling version.
- `store/authStore.ts`, `store/likeStore.ts` and `components/playlist/AddToPlaylistModal.tsx` discard the previous account's likes and in-flight projections. The delayed unlike rejection regression uses separate local test logins.
- `store/playerStore.ts` keeps a private library playback sequence and its listening history in memory. A session change drops them and restores the public resume point, re-reading its track IDs through the public catalog. Playlist and like records in the backend can be revisited after login. The player and drawer UI are covered by WI-016.
- `frontend/src/router/pilotLibrary.test.tsx` traverses the actual React routes and store with a local Axios adapter. From an empty library, A likes a public track, creates a first playlist, adds the track and plays the saved list after re-login. B sees empty personal screens. The test verifies the original public queue and history across the account change and the shared browser storage contents. This suite uses a simulated media element. Actual HTTP authorization is checked separately by WI-015.

## Local results

- `npm run typecheck` and `npm run lint` passed.
- Pilot production build passed using `VITE_APP_MODE=pilot npm run build -- --outDir ../build/WI-20261123-ATS-014-pilot-frontend`. Artifact is local.
- Pilot Vitest suites covering routing, personal screens, session requests, mobile navigation, library and licenses passed 22/22. Related default-mode component, player, route and subscriber suites passed 97/97; counts overlap.
- Initial full frontend regression during parallel development reported two failures: a mobile player selector and the new multi-screen test exceeded the 5-second individual test timeout while competing with the full suite. The mobile selector was corrected in WI-016. The multi-screen case has a 20-second timeout and passed its focused rerun. The combined final full-suite result is tracked by the integrating reviewer.

## References and rollback

Inputs from the handoff include the four Tier 0 standards, security policy, screen flow, API specification and November 23 feature audits 003/004. Base commit `c81f9d0`. Rollback for the local candidate is reverting its library UI changes together with the matching pilot API and proxy changes. No deployed files or accounts were changed by WI-014.
