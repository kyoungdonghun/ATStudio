[WI HEADER]
WI ID: WI-20260809-ATS-023
REQ: REQ-20260809-ATS-001
Agent: qa-fe
Depends On: WI-20260809-ATS-022
Blocks: WI-20260809-ATS-024

[WI SUMMARY]
Why: Audit the frozen public catalog and shared playback experience across browser, source, and API boundaries, with one canonical Track identity and full-duration playback policy across every owned entry point.
Scope (in/out): Execute `PUB-01` through `PUB-06`, `SH-02`, and `SH-06` from the master matrix. Cover Home, Track list/detail, Album image/list/detail, PlayerBar, and TagFilterModal using seeded data and read-only playback only. Verify title plus Usage search; four Tag taxonomies with AND semantics; URL, reload, back, and pagination behavior; canonical images and fallbacks; ordered Track projections; waveform, full duration, progress, seek, queue, transport, shuffle, repeat, volume, and playback recovery; and the same Track across all entry points. Include loading, empty, error, and missing-ID states; 1440x900, 1024x768, 390x844, and 360x800; keyboard and accessibility checks. Recheck the prior waveform/full-Track policy. Authenticated like/add/download paths are `BLOCKED` without an approved login fixture; even with a fixture, this WI may inspect only their rendered guard and source/API contract, not invoke a mutation or download.
DoD: Every owned row is `PASS`, `FAIL`, `BLOCKED`, or `N/A`; `INV-PLAY`, `INV-TRACK`, `INV-SEARCH`, and `INV-IMAGE` are checked across the owned entry points; waveform/time evidence proves full-length rather than preview playback; ordered Album projections agree with queue and previous/next behavior; responsive, keyboard, focus, and non-occlusion results are recorded; each finding distinguishes visible UI, sanitized API evidence, canonical state, and automation/environment limits; no product fix or side effect is applied.
Constraints/Forbidden: Keep the approved frozen baseline and preserve the intentional demo ZIP unchanged. No product or test edit; no like, add, download, upload, or other application mutation; no DB/schema/data/file/provider/mail/payment/account/secret/storage action; no branch, commit, or other Git action. Do not expose credentials, tokens, storage keys, private paths, or secrets. Stop and classify any unavailable fixture, unsafe effect, or environment limitation instead of manufacturing evidence.

[ACCEPTANCE CRITERIA]
Functional:

- [ ] Verify current local/public runtime identity and seeded-data boundary before reusing browser evidence.
- [ ] Check Home discovery and Track-list title plus Usage search, all four Tag taxonomies, repeated-value AND semantics, visible Usage `#` versus raw URL/API values, apply/clear/remove, close-without-apply, reopen state, invalid query/page, pagination, reload, and back/forward behavior.
- [ ] Check Track detail for valid, nullable-media, inactive, and missing IDs; breadcrumb/back behavior; title, duration, waveform, image, license presentation, and PlayerBar agreement.
- [ ] Check Album image/list views use the same canonical Album identity, counts, pagination, image fallback, view switching, and detail navigation.
- [ ] Check Album detail preserves canonical Track order and shared `PlayableTrack` fields through selection, queue, previous/next, and nullable image/waveform states.
- [ ] Check the same seeded Track from Home, Track list/detail, Album detail, and Player queue yields the same ID, title, duration, stream source, image/waveform fallback, and full-duration behavior; selecting a new source resets stale progress and errors.
- [ ] Check PlayerBar no-Track/current-Track, play/pause, waveform and time progress, pointer and keyboard seek, queue, previous/next, shuffle, repeat, volume/mute, buffering, error/retry, source switch, and desktop/mobile expanded states without content occlusion.
- [ ] Check loading, empty, error, retry, stale-response, malformed URL, and missing-ID states without creating data or mutating canonical state.
- [ ] Check 1440x900, 1024x768, 390x844, and 360x800, including keyboard order, visible focus, accessible names, dialog focus containment/restoration, announcements, contrast, overflow, and Header/PlayerBar occlusion.
- [ ] Classify authenticated like/add/download affordances and entitlement boundaries without invoking them; record `BLOCKED` when no approved login fixture exists.

Performance:

- [ ] Use bounded seeded requests and one controlled browser session; avoid unbounded polling, repeated stream starts, and unnecessary media transfer.
- [ ] Treat late search, Tag, pagination, route, and stream responses as explicit latest-request/source-switch checks.

Quality:

- [ ] Separate UI observation, request/response evidence, canonical API/server state, and browser-local player state in every applicable result.
- [ ] Separate product defects, documentation drift, fixture gaps, automation limitations, environment failures, and policy boundaries.
- [ ] Use source and existing focused tests only as corroboration; do not infer browser reachability or full-duration playback from imports, mocks, or unit assertions alone.
- [ ] Apply no product, test, fixture, documentation, or demo ZIP change during the audit.

[INPUT POINTERS]
Tier 0:

- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:

- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md

Tier 2:

- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md
- docs/ui/atstudio-front-list.md
- docs/ui/screen-flow.md
- docs/design/api-spec.md
- docs/design/usecase/sound-track.md
- docs/design/usecase/sound-album.md
- docs/design/usecase/sound-tag.md
- docs/design/usecase/sound-playhistory.md
- scripts/acceptance/

REQ/Context:

- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md
- deliverables/agent/WI-20260809-ATS-022-findings.md
- deliverables/agent/WI-20260809-ATS-022-evidence-pack.md
- AGENTS.md

Primary code entry points:

- frontend/src/router/index.tsx
- frontend/src/pages/public/HomePage.tsx
- frontend/src/pages/public/TrackListPage.tsx
- frontend/src/pages/public/TrackDetailPage.tsx
- frontend/src/pages/public/AlbumListImagePage.tsx
- frontend/src/pages/public/AlbumListPage.tsx
- frontend/src/pages/public/AlbumDetailPage.tsx
- frontend/src/layouts/PlayerBar.tsx
- frontend/src/components/filter/TagFilterModal.tsx
- frontend/src/utils/playableTrack.ts
- frontend/src/api/tracks.ts
- frontend/src/api/albums.ts
- frontend/src/api/tags.ts
- frontend/src/store/playerStore.ts
- src/main/java/com/atstudio/atstudio/controller/TrackController.java
- src/main/java/com/atstudio/atstudio/controller/AlbumController.java
- src/main/java/com/atstudio/atstudio/controller/TagController.java

Existing test pointers:

- frontend/src/
- src/test/java/com/atstudio/atstudio/

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-023-summary.md:

- Runtime and seeded-fixture boundary, owned rows, pass/fail/blocked totals, material catalog/playback findings, policy and environment limits, unchanged-state confirmation, and WI-024 readiness.

Agent-facing -> deliverables/agent/WI-20260809-ATS-023-evidence-pack.md:

- Sanitized preflight, scenario matrix, viewport evidence, source/API pointers, UI/request/canonical-state separation, waveform/full-duration timing, Track identity and Album order comparisons, automation/environment limits, reproduction steps, unchanged-state proof, and WI-024 trigger.

Findings -> deliverables/agent/WI-20260809-ATS-023-findings.md:

- One row per defect, drift, blocker, or policy question with expected source, visible UI result, sanitized API result, canonical state, severity, classification, automation/environment caveat, adjacent entry points, and bounded follow-up.

Handoff Packet -> deliverables/agent/WI-20260809-ATS-023-handoff.md:

- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required for every matrix result. Record fixture alias without credentials, route, viewport, Track/Album ID, expected source, UI/DOM/console result, sanitized request status and shape, independent canonical API/page read, and browser-local queue/time state where relevant. A screenshot or click alone is not canonical-state evidence.
Tests: Browser execution is primary. Source and focused existing tests may corroborate contracts, but record any unavailable automation, media timing, audio-output, pointer-seek, keyboard, or environment capability separately and never convert it into a product failure or pass.
Rollback: No application mutation or download is allowed. Restore reversible browser-local player, URL, filter, queue, shuffle/repeat, and volume state after each scenario; confirm no seeded data, product file, test, or intentional demo ZIP changed.
