[WI HEADER]
WI ID: WI-20260809-ATS-045-QA-REMEDIATION
REQ: REQ-20260809-ATS-001
Agent: se
Depends On: WI-20260809-ATS-045-QA-FE-REVIEW
Blocks: WI-20260809-ATS-045 completion

[WI SUMMARY]
Why: Independent QA found three P2 gaps in the first WI-045 patch: Playlist creation remains actionable across a user/capacity owner change, Download History does not retire same-role user data, and numeric coercion accepts non-decimal route-ID spellings.
Scope (in/out):
- In: On PlaylistList authenticated owner change, close/reset create UI; hide or disable it whenever list or capacity is not current/known; guard `handleCreate` immediately before mutation.
- In: Add deferred regression proof that user replacement with pending/failed capacity cannot submit a previously opened create form.
- In: Add authenticated-user/session ownership to DownloadHistory load; retire/abort old requests and clear items, page, count, and selection before the new owner loads; test same-role user replacement and stale completion suppression.
- In: Introduce or reuse a small strict positive decimal route-ID parser and apply it to Playlist detail/edit, License detail, and Question detail. Reject exponent, hexadecimal, plus sign, whitespace, fractional, zero, negative, missing, overflow, and nonnumeric raw values before API invocation.
- In: Add focused tests for bypass spellings across all four consumers and align docs with strict decimal behavior.
- Out: All WI-046/047/059 mutation and semantic scope, backend/schema/data, real side effects, and unrelated route normalization.
DoD: All three QA findings are fixed with meaningful regression tests; no creation occurs under unknown capacity; Download History is user-owner safe; only canonical positive decimal IDs reach APIs; focused and full WI-045 gates pass.
Constraints/Forbidden:
- Preserve backend as the final Playlist limit authority and retain existing mutation semantics.
- Keep the route-ID parser narrowly scoped and dependency-free; do not accept JavaScript coercion spellings.
- Do not edit, stage, commit, or push outside this remediation; do not create evidence/summary files.
- Do not touch, inspect, stage, or delete protected output or ignored configuration; invoke no live effects.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] An open Playlist create modal closes and resets when authenticated ownership changes; pending/failed capacity makes submission impossible at UI and handler boundaries.
- [ ] Download History starts a new load for a same-role user change, clears prior-user projection, and ignores the retired user's success/failure/finally.
- [ ] `1e3`, `0x10`, `+7`, whitespace-padded values, fractions, zero, negative, missing, overflow, and nonnumeric IDs issue no detail request.
- [ ] Canonical ASCII decimal positive safe integers continue to load.
Quality:
- [ ] Focused tests include deferred promises and direct API-call absence assertions.
- [ ] Typecheck, ESLint, Prettier, focused tests, and `git diff --check` pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:
- docs/policies/quality-gates.md
- docs/standards/evidence-pack-standard.md

Tier 2:
- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/AGENTS.md
- docs/design/usecase/sound-playlist.md
- docs/design/usecase/user-license.md
- docs/design/usecase/user-question.md
- docs/design/usecase/download-queue.md

REQ/Context Docs:
- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/agent/WI-20260809-ATS-045-handoff.md
- deliverables/agent/WI-20260809-ATS-045-qa-fe-review-handoff.md

Files:
- frontend/src/pages/subscriber/PlaylistListPage.tsx
- frontend/src/pages/subscriber/PlaylistListPage.test.tsx
- frontend/src/pages/subscriber/DownloadHistoryPage.tsx
- frontend/src/pages/subscriber/DownloadHistoryPage.test.tsx
- frontend/src/pages/subscriber/PlaylistDetailPage.tsx
- frontend/src/pages/subscriber/PlaylistEditPage.tsx
- frontend/src/pages/subscriber/LicenseDetailPage.tsx
- frontend/src/pages/subscriber/QuestionDetailPage.tsx
- corresponding focused tests
- a narrowly scoped frontend utility/test if shared parsing removes real duplication
- current WI-045 documentation changed by the first patch

[OUTPUT CONTRACT]
- Final response only: changed files, evidence for all three findings, commands/results, and blockers. No new deliverable files.

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required for all three QA findings.
Tests: Record focused RED/GREEN and all non-mutating checks run.
Rollback: Revert only this remediation layer on top of WI-045; no data rollback.
