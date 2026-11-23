# WI-20261123-ATS-011 · Combined catalog tags

The [handoff](WI-20261123-ATS-011-handoff.md) records the user's amendment to [REQ-20261123-ATS-002](../user/REQ-20261123-ATS-002.md) and the injected Tier 0–2 references.

## Implementation

- `frontend/src/pages/public/TrackListPage.tsx` and `TrackListPage.module.css` restore the single desktop tag header and reference widths from `f0c73d4`.
- `frontend/src/components/track/TrackRow.tsx` renders genre, instrument and mood chips into one cell. The associated CSS restores desktop and tablet cell sizing. The title keeps its usage hashtags and the mobile layout keeps its labeled taxonomy groups.
- The matching TrackListPage and TrackRow tests cover header order, row alignment with empty tags, grouped chip order for mixed tags, and an instrument-only track. The current title link opens the detail page, which already displays all attached tags.

## Local verification

2026-11-23 KST. Frontend engineer ran the following from `frontend/`.

- `npm test -- src/components/track/TrackRow.test.tsx src/pages/public/TrackListPage.test.tsx` passed 26 tests.
- In pilot mode, `npm test -- src/components/track/TrackRow.test.tsx src/router/pilot.test.tsx src/layouts/Header.pilot.test.tsx` passed all 10 tests. A separate name-filtered pilot run also passed the combined column heading case.
- `npm run lint`, Prettier check for the six changed frontend files, and `git diff --check -- frontend` passed.
- With `VITE_APP_MODE=pilot`, `npm run build -- --outDir ../build/WI-20261123-ATS-011/pilot` completed TypeScript compilation and the Vite production build.
- The independent release owner verified the built pilot in local Chrome at 1440, 1100, 900 and 390 CSS pixels using synthetic tracks. The desktop/tablet checks found one tag header, the expected chip order, the reference cell sizing and a 4px chip radius. The phone showed three labeled groups. The usage hashtag stayed with the title, there was no page overflow, and the linked detail showed all 11 fixture tags at each width. The owner's normal-mode catalog component and track detail specs also passed (13 tests).

## Boundaries and rollback

Desktop chip overflow uses the reference's single clipped line. The detail is available through the title. Local tests and browser checks cover the patched build. To revert this UI decision, restore the two components, their CSS and their tests to their `a8feb14` versions, preserving any subsequent edits. No backend or data changes are involved.
