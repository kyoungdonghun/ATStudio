# WI-20261123-ATS-007 · Pilot catalog search

[Approved requirement](../user/REQ-20261123-ATS-002.md) · [Handoff and inputs](WI-20261123-ATS-007-handoff.md)

The [header](../../frontend/src/layouts/Header.tsx) uses the existing desktop and mobile catalog forms and their CSS, trim/length restriction, encoded query, page reset and destination-focus behavior for the pilot. A mobile search opener expands just the search form; Escape restores focus. Existing scoped pilot links remain visible. The listening-only header retains its prior links. No backend, API access, pricing, data or Vite environment changes.

The [pilot regression tests](../../frontend/src/layouts/Header.pilot.test.tsx) check guest whitespace and special-character submissions, role-scoped navigation, mobile search focus, Escape and destination focus. The first test iteration located the mobile opener by its desktop jsdom accessibility tree, which hides mobile elements under the real CSS. Using the component's accessible label exercises the mobile command independent of jsdom's viewport. Production mobile rendering requires a viewport-capable browser.

Verification on 2026-11-23, local Windows frontend checkout: scoped ESLint passed with no warnings. Vitest passed 40 tests in Header.test.tsx, Header.pilot.test.tsx, pilotPersonalPages.test.tsx, index.listening.test.tsx and TrackListPage.test.tsx (five files). Source and test formatted with repository Prettier. Deployment and live acceptance are owned by the parent release task.

Independent Chrome review observed a 12px trailing nav overflow for an authenticated pilot participant at 390px. The existing pilot-only rule at widths up to 480px now uses 2px of horizontal tab padding instead of 4px. The nav retains native horizontal scrolling for narrower devices. Scoped Prettier and Git whitespace checks passed; final browser measurements belong to WI009.
