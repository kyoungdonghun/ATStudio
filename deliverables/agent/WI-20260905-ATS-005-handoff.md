# WI-20260905-ATS-005 Handoff

[WI HEADER]
WI ID: WI-20260905-ATS-005
REQ: REQ-20260905-ATS-001
Agent: se
Depends On: WI-20260905-ATS-002 reproduction
Blocks: WI-20260905-ATS-002 browser closeout; WI-20260905-ATS-003 final evidence

[WI SUMMARY]
Why: Real browser play/pause/seek/reload still renders an empty PlayerBar after the prior WI-010 fix.
Scope: Diagnose current frontend/src/store/playerStore.ts persistence/hydration with real browser evidence supplied by MA, add regression and minimal fix only for confirmed cause. Temporary safe diagnostic logging is allowed, restricted to player version/counts/track ID/time and hydration status; remove it before final tests/commit. No auth tokens, storage full dump or private data.
DoD: Root cause demonstrated, focused regression, actual browser retest by MA; or exact environmental blocker without speculative patch.
Constraints: Only playerStore.ts and corresponding playerPersistence/playerStore tests and own evidence files. No PlayerBar or PlaylistDrawer edits (peer owns these). No DB/backend/client worktree, no new APIs/dependencies. Do not operate browser or run full test suites. Notify MA before touching observed source so it can reload deliberately.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Explain why play history survives but paused player does not.
- [ ] Confirm only current public IDs hydrate; no stale auth/queue regression.
Performance:
- [ ] No unrelated playback redesign.
Quality:
- [ ] All temporary instrumentation removed and focused tests pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
Tier 2:
- .agents/skills/react-best-practices/SKILL.md
- deliverables/user/REQ-20260905-ATS-001.md
- deliverables/user/WI-20260823-ATS-010-summary.md
- frontend/src/store/playerStore.ts
- frontend/src/store/playerPersistence.test.ts
- frontend/src/layouts/PlayerBar.tsx
- frontend/src/api/tracks.ts
- frontend/src/api/client.ts
- frontend/src/utils/safeStorage.ts

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260905-ATS-005-summary.md
Agent-facing -> deliverables/agent/WI-20260905-ATS-005-evidence-pack.md
Use create-wi-evidence-pack skill. Root cause, exact modified paths and test commands.

[TRACEABILITY REQUIREMENTS]
Reproduction: /tracks/4 public demo 7s -> play -> pause -> slider Home ArrowRight (5s) -> reload -> empty PlayerBar. API POST /api/tracks/batch ids=[4] returns dataList with track4. Browser history modal after new play contains current and September2 entries, so browser storage is not globally gone. Browser read-only evaluator localStorage is undefined in its sandbox: NOT proof that page localStorage is unavailable. Only Vite websocket console warning. MA owns real browser tests.
Rollback: Only this WI edits, preserve existing approved behavior.
