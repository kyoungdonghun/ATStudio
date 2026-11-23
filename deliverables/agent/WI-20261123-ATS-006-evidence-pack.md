# WI-20261123-ATS-006 · Reference taxonomy columns

The [approved scope](../user/REQ-20261123-ATS-002.md) selects the three-category local table at `f521a6e`. The [handoff](WI-20261123-ATS-006-handoff.md) keeps the approved mobile presentation from `199f86d`.

Restored the desktop taxonomy cell and chip rules in [TrackRow.module.css](../../frontend/src/components/track/TrackRow.module.css) and the header widths in [TrackListPage.module.css](../../frontend/src/pages/public/TrackListPage.module.css). In [TrackRow.tsx](../../frontend/src/components/track/TrackRow.tsx), an empty category produces an empty aligned cell. The historical desktop display clips tags horizontally on a single line. The mobile layout retains the current labeled groups and multiline tags under the title. Usage hashtags, filter controls, playback and user roles were outside the change.

Local verification on November 23, 2026: TrackRow and TrackListPage Vitest suites passed 26 tests in two files. Compared the effective CSS declarations for the row tag cell, tag chip and table header at widths 768, 1023, 1024, 1200, 1201 and 1440 against `git show f521a6e:<path>` using PostCSS. All declarations matched. At 390px, chip and mobile group declarations matched `199f86d`. The empty-cell test now checks three retained unlabelled desktop cells. Deployment evidence belongs to the release owner.
