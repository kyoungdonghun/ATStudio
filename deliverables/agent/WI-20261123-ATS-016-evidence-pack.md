# Participant controls in the audio player

[Handoff and reference documents](WI-20261123-ATS-016-handoff.md) · [approved scope](../user/REQ-20261123-ATS-003.md)

The player reuses the existing heart, add dialog, likes/playlist drawer and download control for invited listeners. Its pilot subscription check reads only the playlist capacity endpoint. The original public player and existing sales layouts retain their own control conditions. In the expanded phone player a listener can save the current track directly. Both player sessions and pending binary-download results are scoped to the signed-in session. Playing a private drawer selection uses the personal playback transition in WI014.

| Location | Evidence |
|---|---|
| `frontend/src/layouts/PlayerBar.tsx` | Visibility, session remount, delayed download and desktop/phone actions. |
| `frontend/src/components/player/PlaylistDrawer.tsx` | Participant capacity, first playlist and owner-scoped playback. |
| `frontend/src/layouts/PlayerBar.test.tsx` | Pilot controls, guest/admin exclusions, mobile and account transition. |
| `frontend/src/layouts/PlayerBar.drawer.test.tsx` | Real drawer tab navigation and first collection creation with and without a current track. |

From `frontend`, run `npx vitest run src/components/player/playerComponents.test.tsx src/layouts/PlayerBar.test.tsx src/layouts/PlayerBar.drawer.test.tsx`. All 69 tests passed. In a separate process with `VITE_APP_MODE=pilot`, all six real-drawer cases passed. These executions overlap. Targeted ESLint and Prettier checks on the four modified TypeScript files passed. The initial mobile test used a desktop JSDOM accessible-role query against CSS-hidden mobile controls. The final test exercises the expanded mobile tree directly. These are component and API-mock checks, not real-device playback.

Rollback for a local candidate restores the four player files together with the matching WI014 session and API changes before rebuilding.

On November 24, 2026 KST, pre-commit typechecking found TS2769 in the drawer test. Removing the unsupported `exact` option from `findByRole` preserved its exact string match for the button's accessible name. Local verification from `frontend` passed `npm run typecheck` and all six tests in `src/layouts/PlayerBar.drawer.test.tsx` in each of the default and pilot modes.
