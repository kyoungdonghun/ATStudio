[WI HEADER]
WI ID: WI-20260809-ATS-044
REQ: REQ-20260809-ATS-001
Agent: se
Depends On: WI-20260809-ATS-043
Blocks: WI-20260809-ATS-059, WI-20260809-ATS-073, WI-20260809-ATS-078

[WI SUMMARY]
Why: `CR-031-024`, `CR-031-025`, `CR-031-026`, `CR-031-028`, `CR-031-031`, `CR-031-032`, and `CR-031-036` identify one bounded catalog/player state group: public detail and collection failures lack deliberate recovery, Album view switching changes the projection, Album requests can commit stale data, page-owned playback context survives unmount, display order leaks zero-based data, and restored progress is not proven to stay within the current medium.
Scope (in/out):
- In: Give missing/error Track and Album detail states localized retry plus safe Back/Home recovery without exposing raw transport errors.
- In: Normalize malformed, negative, zero, and out-of-range Track/Album page query values into one bounded request/navigation behavior. Avoid repeated bad requests and provide deliberate empty/error recovery.
- In: Preserve compatible sort/page query state when switching Album grid/list views and use one page-size contract so a view switch does not silently change the result projection.
- In: Render Album Track positions as user-facing one-based positions while preserving canonical ordering and IDs.
- In: Give Album list/detail requests cancellation or generation ownership so only the latest mounted route/query may commit data, errors, or loading completion.
- In: Scope page-provided `trackListContext` to its mounted owner and clear only that owned context on departure without stopping the active Track or corrupting queue/shuffle/repeat state.
- In: Clamp restored and seeked playback progress to finite non-negative media bounds as duration becomes known, without changing the approved full-track playback policy.
- In: Add focused and adjacent regression tests for malformed/out-of-range pages, rapid page/view/route changes, unmount ownership, ordering display, persisted progress bounds, queue/shuffle/repeat, browser history, and current public playback.
- In: Update current catalog/player behavior documentation and WI evidence when implementation changes the documented contract.
- Out: Album/Track keyboard semantics and image fallback owned by WI-059; Track-detail metadata documentation owned by WI-073; authenticated live-fixture proof owned by WI-078; Home play/Album download product policy `CR-031-038`; stored duration/Usage data repair `CR-031-034/035`; backend API/schema/data mutation; preview-only playback; download/like/playlist/provider/mail effects.
DoD: All seven canonical roots are corrected within existing catalog and full-playback policy; request ownership prevents stale commits; page context cannot survive its owner; restored/seek progress remains finite and bounded; focused, adjacent, and full quality gates pass; current docs and evidence match implementation.
Constraints/Forbidden:
- Preserve the existing policy that public playback may play the full Track; do not reintroduce preview duration or entitlement gating for playback.
- Do not invent a new page size, ordering, missing-resource, download, or Home-play product policy where canonical sources are silent; reuse the current dominant catalog contract.
- Do not clear the active Track or durable queue merely because a page-owned visible-list context unmounts.
- Do not allow an aborted or stale request to commit data, errors, empty state, or loading completion.
- Do not add dependencies, change backend/API/schema/data, perform authenticated mutation/download/export/provider/mail/payment operations, or inspect ignored local configuration.
- Do not touch, inspect, stage, or delete `output/client-demo-screenshots-20260716-140514.zip` or `output/ui-ux-audit/`.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Missing Track/Album detail and recoverable request failure render localized product-owned recovery with bounded retry and safe Back/Home actions.
- [ ] Invalid Track/Album `page` values neither repeat invalid requests nor expose raw Axios/server text; out-of-range pages have a deliberate recovery path.
- [ ] Album grid/list switching preserves compatible query state and returns the same page projection under one page-size contract.
- [ ] Album Track display positions are one-based while canonical order and Track identity remain unchanged.
- [ ] Rapid Album query/view/detail changes and route departure cannot let an older request overwrite current data, error, or loading state.
- [ ] Album page playback context is removed when its owner unmounts; current Track, queue, shuffle, repeat, next, and previous behavior remain coherent.
- [ ] Persisted/current seek values are finite, non-negative, and clamped when media duration is known; waveform/time never renders progress beyond the Track.
Performance:
- [ ] Request ownership uses cancellation or constant-time generation checks without polling or duplicate fetch loops.
- [ ] Context cleanup and progress clamping add no global listeners or render loops.
Quality:
- [ ] Focused Track/Album/player store and PlayerBar tests prove RED/GREEN behavior for each owned root.
- [ ] Adjacent router/history, queue/shuffle/repeat, playable projection, public catalog, and existing full-playback suites pass.
- [ ] Frontend full tests, coverage, typecheck, ESLint, Prettier, and build pass.
- [ ] Backend full tests/build remain green because shared contracts must not regress.
- [ ] Current catalog/player documentation matches implementation; docs validation and `git diff --check` pass.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred from playback/quality scope):
- docs/policies/quality-gates.md
- docs/standards/evidence-pack-standard.md

Tier 2 (React and current contracts):
- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/AGENTS.md
- docs/ui/screen-flow.md
- docs/ui/atstudio-front-list.md
- docs/design/usecase/sound-track.md
- docs/design/usecase/sound-album.md
- docs/design/api-spec.md

REQ/Context Docs:
- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/agent/WI-20260809-ATS-023-findings.md
- deliverables/agent/WI-20260809-ATS-023-evidence-pack.md
- deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md
- deliverables/agent/WI-20260809-ATS-043-evidence-pack.md

Files:
- frontend/src/pages/public/TrackListPage.tsx
- frontend/src/pages/public/TrackDetailPage.tsx
- frontend/src/pages/public/AlbumListPage.tsx
- frontend/src/pages/public/AlbumListImagePage.tsx
- frontend/src/pages/public/AlbumDetailPage.tsx
- frontend/src/components/album/AlbumCard.tsx
- frontend/src/components/track/TrackRow.tsx
- frontend/src/store/playerStore.ts
- frontend/src/layouts/PlayerBar.tsx
- frontend/src/api/tracks.ts
- frontend/src/api/albums.ts
- corresponding focused and adjacent test files

Repro/Logs:
- Use Vitest deferred promises, MemoryRouter/createMemoryRouter, mocked catalog APIs, synthetic finite media durations, and isolated Zustand state only.
- Exercise `page=abc`, negative, zero, and beyond-last-page inputs; rapid query/view/detail changes; unmount before resolution; restored negative/NaN/Infinity/beyond-duration progress; queue/shuffle/repeat transitions.
- Do not inspect browser storage directly outside sanitized test state or invoke real media/download/authenticated mutation effects.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-044-summary.md:
- Korean summary of catalog recovery, stable view projection, latest-request ownership, page context cleanup, progress bounds, unchanged full-playback policy, verification, and residual boundaries.
Agent-facing -> deliverables/agent/WI-20260809-ATS-044-evidence-pack.md:
- Root-to-code/test evidence, RED/GREEN proof, commands, rollback, and follow-up chain.
Handoff Packet -> deliverables/agent/WI-20260809-ATS-044-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required for `CR-031-024`, `CR-031-025`, `CR-031-026`, `CR-031-028`, `CR-031-031`, `CR-031-032`, and `CR-031-036`.
Tests: Record focused RED/GREEN cases, adjacent catalog/player/history suites, full quality gates, and explicit proof that full-track playback remains unchanged.
Rollback: Revert catalog recovery/page normalization, Album projection/request ownership, display numbering, page-context lifecycle, progress bounds, tests, docs, and WI deliverables as one patch. No data rollback is permitted or expected.
