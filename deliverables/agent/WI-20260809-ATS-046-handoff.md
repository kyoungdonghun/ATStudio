[WI HEADER]
WI ID: WI-20260809-ATS-046
REQ: REQ-20260809-ATS-001
Agent: se
Depends On: WI-20260809-ATS-037, WI-20260809-ATS-045
Blocks: WI-20260809-ATS-059

[WI SUMMARY]
Why: Playlist mutations currently contain unconfirmed destructive Drawer actions, silent failures, incomplete add-to-playlist recovery, and unreleased thumbnail preview URLs.
Scope (in): `CR-031-041`, `CR-031-046`, and `CR-031-052`; PlaylistDrawer destructive confirmation/pending/error/retry; AddToPlaylistModal visible loading/retry/subscription-required outcome; Playlist list/edit blob-preview lifecycle; focused tests and current-state Playlist docs.
Scope (out): Drawer dialog/focus/keyboard semantics owned by WI-057, card/keyboard/image fallback semantics owned by WI-059, reorder payload owned by WI-037, member read ownership owned by WI-045, backend/schema/data/product-policy changes.
DoD: Destructive Drawer calls cannot start before explicit confirmation; failures remain visible and retryable; duplicate submits are fenced; AddToPlaylistModal never renders a blank open state and can retry transient list failure; missing optional expiry callback still produces explicit feedback; every created preview object URL is revoked exactly once at replacement/removal/close/route-owner/unmount boundaries without revoking backend URLs; focused and full gates pass; docs match code.
Constraints/Forbidden: Preserve WI-037 zero-based reorder and WI-045 owner/read projection behavior. Do not invent new product policy or accessibility wording reserved for later WIs. No backend/schema/data changes and no real authenticated mutations/downloads/provider/mail effects. Do not inspect ignored secrets or touch protected output artifacts.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Drawer playlist delete and track remove require explicit, target-specific confirmation before the API call.
- [ ] Confirmed mutation has one pending owner; duplicate clicks are ignored and controls cannot act on stale owner/detail state.
- [ ] Failure uses fixed Korean copy, exposes a bounded retry for the same current target, and success refreshes authoritative list/detail state.
- [ ] AddToPlaylistModal displays loading while its list is pending, a fixed failure with retry, and an explicit subscription-required result even when no callback is supplied.
- [ ] Close/reopen and retired responses/timers cannot affect the current AddToPlaylistModal lifecycle.
- [ ] Playlist list/edit revoke only locally created preview object URLs on every lifecycle boundary.
Quality:
- [ ] Focused tests prove confirmation, zero calls before confirm, pending duplicate fencing, failure/retry, stale owner/lifecycle suppression, loading/retry/expiry, and object-URL cleanup.
- [ ] Existing WI-037 reorder and WI-045 ownership tests continue to pass.
- [ ] Frontend full tests, typecheck, lint, format, build, backend regression, docs validation, and diff check pass before closure.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred):
- docs/policies/quality-gates.md

Tier 2 (Frontend and UX):
- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md
- docs/design/usecase/sound-playlist.md
- docs/ui/screen-flow.md
- docs/ui/atstudio-front-list.md

REQ/Context Docs:
- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md
- deliverables/agent/WI-20260809-ATS-024-findings.md
- deliverables/agent/WI-20260809-ATS-037-evidence-pack.md
- deliverables/agent/WI-20260809-ATS-045-evidence-pack.md

Files:
- frontend/src/components/player/PlaylistDrawer.tsx
- frontend/src/components/player/PlaylistDrawer.module.css
- frontend/src/components/player/playerComponents.test.tsx
- frontend/src/components/playlist/AddToPlaylistModal.tsx
- frontend/src/components/playlist/AddToPlaylistModal.module.css
- existing AddToPlaylistModal tests and coverage callers
- frontend/src/pages/subscriber/PlaylistListPage.tsx
- frontend/src/pages/subscriber/PlaylistListPage.test.tsx
- frontend/src/pages/subscriber/PlaylistEditPage.tsx
- frontend/src/pages/subscriber/PlaylistEditPage.test.tsx
- frontend/src/components/ui/ConfirmDialog.tsx (usage only unless a proven shared defect requires escalation)

Repro/Logs:
- `frontend/src/components/player/PlaylistDrawer.tsx`: delete/remove APIs currently start directly and swallow failures.
- `frontend/src/components/playlist/AddToPlaylistModal.tsx`: `open && !ready` returns null; load failure has no retry; missing callback can close silently.
- Playlist list/edit use `URL.createObjectURL` without complete revocation.

[OUTPUT CONTRACT]
User-facing -> `deliverables/user/WI-20260809-ATS-046-summary.md`:
- Korean summary, user-visible recovery, tests, residual risks.
Agent-facing -> `deliverables/agent/WI-20260809-ATS-046-evidence-pack.md`:
- Root closure, exact pointers, RED/GREEN evidence, full gates, rollback, next WI.
Handoff Packet -> `deliverables/agent/WI-20260809-ATS-046-handoff.md`:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required for confirmation, retry, lifecycle, and URL cleanup.
Tests: Include exact commands, counts, and independent review findings.
Rollback: Revert Drawer/AddToPlaylistModal/list/edit/test/doc patch as one unit; no data rollback.
