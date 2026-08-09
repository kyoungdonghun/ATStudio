[WI HEADER]
WI ID: WI-20260809-ATS-010
REQ: REQ-20260808-ATS-004
Agent: se
Depends On: WI-20260809-ATS-006
Blocks: WI-20260808-ATS-029

[WI SUMMARY]
Why: Repair confirmed public pagination and inactive-membership consistency gaps from WI-006.
Scope (in/out): Bound public track search pagination, align public playlist detail/count/reorder with active memberships while preserving hidden rows, align public album counts/sort with active tracks, and add focused tests/docs. Destructive membership deletion is out of scope.
DoD: Invalid/unbounded public pages return stable domain errors; playlist operations share one active-membership contract; album counts/sort match playable detail; focused tests pass.
Constraints/Forbidden: No schema/data mutation, destructive cleanup, external calls, secrets/ZIP, unrelated refactor, commit, or push.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Track page and size accept only page >= 1 and 1 <= size <= 100.
- [ ] Playlist list count, detail, and reorder expose the same active set; hidden memberships remain preserved and deterministically ordered.
- [ ] Public album count and track-count sort include active tracks only.
Performance:
- [ ] Aggregate fixes remain query-based and do not introduce per-row counting.
Quality:
- [ ] Focused service/repository/controller tests cover boundary and mixed active/inactive cases.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Context:
- deliverables/user/WI-20260809-ATS-006-summary.md
- deliverables/agent/WI-20260809-ATS-006-evidence-pack.md
- docs/SR/SR-100.md
- docs/SR/SR-101.md
Files:
- TrackController/TrackService/RequestDTO/TrackSearchRequest and focused tests
- PlaylistService/PlaylistTrackRepository and focused tests
- AlbumService/AlbumRepository/AlbumTrackRepository and focused tests
- affected API/use-case current-state docs

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-010-summary.md
Agent-facing -> deliverables/agent/WI-20260809-ATS-010-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260809-ATS-010-handoff.md

[TRACEABILITY REQUIREMENTS]
Patch, query/ordering behavior, tests, risks, rollback, and WI-029 unblock status are required.
