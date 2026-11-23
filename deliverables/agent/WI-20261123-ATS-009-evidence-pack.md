# Pilot UI independent review

2026-11-23 KST. [Requirement](../user/REQ-20261123-ATS-002.md) · [Review assignment](WI-20261123-ATS-009-handoff.md).

The reviewer compared the effective cell, header and chip CSS declarations against Git object `f521a6e` using PostCSS at widths 768, 1023, 1024, 1200, 1201 and 1440. All matched. Desktop empty groups render no placeholder. The later mobile labeled groups and wrapping remain.

The reviewer ran local Chromium on the independent pilot build at 320, 390 and 1440 pixels with guest and synthetic USER profiles. Both roles submitted a whitespace-padded Korean keyword containing an ampersand and arrived at the correctly encoded, trimmed results URL. Mobile search opened and Escape returned focus. The exposed navigation links matched each role; no sales/profile/registration links appeared. A local participant opened a synthetic owner license and its copy button passed the entire displayed UUID to an isolated clipboard spy. No real credential, clipboard mutation or server account was used. External CDN requests were blocked in this local check.

The first 390px signed-in layout clipped 12 pixels of its horizontally scrollable nav. The implementer reduced pilot tab inline padding on narrow screens. Final signed-in navigation measured 245px client / 245px content at 390px, 185px / 245px at 320px, and 352px / 352px at 1440px. At 320px the nav itself scrolls. The document did not overflow in the tested list or license detail views.

Seven focused Vitest files passed all 50 cases: TrackRow, TrackListPage, both Header suites, LicenseDetailPage.pilot, router/pilot and pilotPersonalPages. Coverage includes the existing selling header, pilot route restrictions, stale clipboard ownership, access failure and delayed completion. This count describes focused tests only.

All 40 independently built pilot files matched the final local packaging candidate by relative path and SHA-256. The index hash is `70f3b2367534e9443e06d0e47c871d3be69bd3cc70f35aa17a198ac2ceb7118b`. Independent local review passed. Native rollout and live results belong to the release owner.
