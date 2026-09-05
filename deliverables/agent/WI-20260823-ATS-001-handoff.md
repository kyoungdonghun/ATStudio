# WI Handoff Packet: WI-20260823-ATS-001

[WI HEADER]
WI ID: WI-20260823-ATS-001
REQ: REQ-20260823-ATS-001
Agent: se
Depends On: -
Blocks: WI-20260823-ATS-002, WI-20260823-ATS-003

[WI SUMMARY]
Why: Implement the approved client-feedback remediation without changing product policies or the client-acceptance environment.

Scope (in):
- Fix multi-mood selection visibility in the public Track catalog and add regression coverage.
- Promote Question list `New question` into a dedicated responsive FAB.
- Reuse the existing BUSINESS `companyName` value as a required `Company name or industry` input throughout registration/profile/admin presentation; do not add a DB field or repurpose `UserJob`.
- Permit internal nickname spaces; trim edge whitespace consistently before frontend validation/availability/register/profile calls and all backend validation/uniqueness/persistence paths.
- Add playlist `Play all` using the existing player `playAll` capability; retain non-starting queue add.
- Add a direct Likes action adjacent to playback history on desktop and mobile expanded PlayerBar, opening the existing likes experience.
- Align safe local billing-key example/documentation with the current keyring configuration only. Never write a secret or change ignored `application-local.yml`.
- Add/adjust focused tests.

Scope (out):
- No DB schema/data/storage mutation, media reseed, deletion, client-worktree modification, payment/refund/mail/provider call, plan/default-playlist/repeat-policy change, or arbitrary mobile-volume redesign.

DoD:
- All approved behaviors are implemented with focused tests.
- No unrelated dirty file is modified: especially `frontend/src/pages/public/HomePage.tsx`, `frontend/src/pages/public/HomePage.test.tsx`, untracked deliverables/output/scripts, or client worktree files.
- Run targeted tests and report any broad-suite blocker separately.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Multiple mood values remain both visually selectable and encoded as repeated `mood` URL/API values.
- [ ] BUSINESS has one `Company name or industry` input backed by `companyName`; INDIVIDUAL keeps `job`.
- [ ] Internal nickname spaces survive; leading/trailing spaces cannot affect duplicate checks or stored values.
- [ ] Playlist Play all starts first Track and queue traversal follows playlist order.
- [ ] Likes is directly reachable next to playback history on desktop and mobile expanded player.
- [ ] Question FAB is visually larger without overlapping PlayerBar/mobile content.
- [ ] Local config example/docs use `active-key-id` and keyring terminology, with no secret.
Quality:
- [ ] Relevant frontend and backend tests pass.
- [ ] `npm run typecheck`, `npm run lint`, changed-file Prettier check, and `git diff --check` pass or any pre-existing blocker is evidenced.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2:
- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md
- docs/design/usecase/user-info.md
- docs/design/usecase/sound-playlist.md
- docs/ui/atstudio-front-list.md
- docs/ui/screen-flow.md

REQ/Context Docs:
- deliverables/user/REQ-20260823-ATS-001.md
- docs/SR/SR-55.md
- docs/SR/SR-72.md

Files (starting points):
- frontend/src/pages/public/TrackListPage.tsx
- frontend/src/pages/subscriber/QuestionListPage.tsx
- frontend/src/pages/subscriber/QuestionListPage.module.css
- frontend/src/pages/subscriber/PlaylistDetailPage.tsx
- frontend/src/layouts/PlayerBar.tsx
- frontend/src/store/playerStore.ts
- frontend/src/pages/auth/SignupPage.tsx
- frontend/src/utils/validation.ts
- src/main/java/com/atstudio/atstudio/common/validation/ValidationConstants.java
- src/main/java/com/atstudio/atstudio/service/UserService.java
- application-local.example.yml

Repro/Logs:
- Local runtime confirmed `mood` URL repeats but result-scoped facet filtering hides unselected moods.
- Standard local config boot requires the current billing keyring shape; no secret may be printed or committed.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260823-ATS-001-summary.md:
- Summary, changed behavior, risks, and user-verification notes.
Agent-facing -> deliverables/agent/WI-20260823-ATS-001-evidence-pack.md:
- Changed paths, commands/results, line-level pointers, rollback, and follow-up WIs.
Handoff Packet -> deliverables/agent/WI-20260823-ATS-001-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
- Evidence pointers: required for each behavior.
- Tests: include exact commands and outcome.
- Rollback: identify one commit/diff boundary; do not perform Git reset/restore.
