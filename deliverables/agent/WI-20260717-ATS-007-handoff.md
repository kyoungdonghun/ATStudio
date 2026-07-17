[WI HEADER]
WI ID: WI-20260717-ATS-007
REQ: REQ-20260716-ATS-004
Agent: se, qa-fe, docops
Depends On: WI-20260717-ATS-006
Blocks: WI-20260717-ATS-006 closure, final runtime smoke, V1 branch/worktree cleanup

[WI SUMMARY]
Why: Independent WI-006 verification found no P1 issues but found functional, contract, documentation, accessibility, persistence-recovery, and coverage defects that block the approved V1 consolidation gate.
Scope (in):
- Frontend: repair selected re-download track targeting, refund preview/request target binding, login storage-failure handling, player rehydration, recurring callback replay and amount validation, Toss SDK retry, persisted-state shape validation, download-history accessibility, and provider contract normalization.
- Backend/config: correct explicit local-config startup guidance, add strict provider-mismatch reconciliation regression coverage, raise JaCoCo coverage to the documented thresholds, and enforce those thresholds in Gradle after they pass.
- Documentation/config prose: remove current `/playlists/new` promises, removed manual-patch instructions, deleted frontend-symbol examples, stale placeholder language, and obsolete Thymeleaf configuration commentary. Make the historical preservation count reproducible or stop presenting an unreproducible exact count as a gate.
- Tests: add focused regression tests for every repaired defect, expand meaningful behavior coverage to meet current documented minimums, and preserve all existing passing tests.
Scope (out):
- New product features, policy changes, payment-provider additions, production deployment, remote push, branch/worktree deletion, database mutation, and weakening/excluding production code solely to inflate coverage.
DoD:
- All WI-006 P2 and P3 findings are fixed or disproved with reproducible evidence.
- Frontend statements/lines/functions >= 80% and branches >= 70%; backend lines/methods >= 80% and branches >= 70%.
- Frontend and backend coverage verification is executable and fails closed below the documented thresholds.
- Full frontend/backend/docs gates pass and exact legacy/residual searches are clean.
Constraints/Forbidden:
- Do not alter the approved product policies: public full-track playback, subscriber/quota-gated downloads, Toss card recurring V1, future multi-PG interfaces, emergency admin subscription operations, or guarded QA bootstrap.
- Do not weaken payment idempotency, claim/fence/lease/lock/state-transition, audit, reconciliation, refund, storage recovery, authorization, or secret-handling controls.
- Do not read or print application-local.yml or secrets.
- Do not mutate DB, Git staging, branches, tags, worktrees, or remotes.
- Do not use blanket coverage exclusions, trivial assertion-only tests, implementation-copy tests, or threshold reduction.
- Agents share the working tree. Do not revert other agents' edits; stay within the assigned write set.

[TRACK OWNERSHIP]
Track A - qa-fe owns:
- frontend/src/**
- frontend/vite.config.ts and frontend/package*.json only if needed for coverage enforcement
- frontend tests and test-only helpers
Track B - se owns:
- src/main/java/com/atstudio/atstudio/config/JwtConfig.java
- src/main/resources/application.yml
- src/test/java/** for backend regression/coverage tests
- build.gradle only for JaCoCo verification
Track C - docops owns:
- docs/**
- WI-007 user summary/evidence only after implementation results are available
No agent may edit another track's files without returning the task for re-routing.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Selected history re-download always calls the selected item's track ID, with a request-level regression test.
- [ ] Refund request creation is impossible when the current inputs differ from the latest successful preview; input edits and preview failure invalidate the preview.
- [ ] Password login fails coherently when durable token persistence fails; no false authenticated navigation occurs.
- [ ] Restored player state either rehydrates audio source/time before resume or clears non-resumable state, with reload/resume tests.
- [ ] Terminal recurring callbacks replace browser history and cannot replay on Back; missing/empty/negative/non-integer amount fails closed.
- [ ] Toss SDK load can recover from a transient first failure.
- [ ] Persisted player/history data is schema-validated and malformed valid JSON falls back safely.
- [ ] Download-history playback and selection controls are keyboard/screen-reader accessible.
- [ ] Frontend recurring-payment provider values match backend/API `TOSS`; active `TOSS_BILLING` and `MOCK` meanings are absent.
- [ ] JWT error guidance names explicit additional-location loading.
- [ ] PROVIDER_MISMATCH reconciliation behavior has direct regression coverage.
- [ ] Current docs contain no removed `/playlists/new`, manual-patch, deleted DataTable/playHistory API, placeholder-directory, or obsolete automatic-local-import semantics.
Quality:
- [ ] Frontend typecheck, ESLint, Prettier, full Vitest, coverage verification, and production build pass.
- [ ] Backend full test, JaCoCo verification/report, and clean build pass.
- [ ] Documentation validation and git diff checks pass.
- [ ] Added-line and untracked-text secret scans return zero high-confidence findings.
- [ ] No unresolved P1/P2/P3 remains in an independent rerun.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on assignee):
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
- docs/standards/frontend-standards.md

Tier 1 (Policies):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
- docs/policies/access-control-policy.md
- docs/policies/versioning-policy.md

Tier 2 (Current source of truth):
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/ui/screen-flow.md
- docs/client/1-quick-checklist.md
- docs/client/2-full-feature-checklist.md

REQ/Decision/Evidence:
- deliverables/user/REQ-20260716-ATS-004.md
- deliverables/agent/WI-20260717-ATS-001-evidence-pack.md
- deliverables/agent/WI-20260717-ATS-006/frontend-qa.md
- deliverables/agent/WI-20260717-ATS-006/backend-qa.md
- deliverables/agent/WI-20260717-ATS-006/integration-review.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260717-ATS-007-summary.md:
- Korean summary of fixes, behavioral impact, validation, residual risks, and next blocked/unblocked WI.
Agent-facing -> deliverables/agent/WI-20260717-ATS-007-evidence-pack.md:
- Finding-by-finding closure matrix, exact file/line pointers, commands/results, coverage metrics, negative searches, rollback, and follow-up WI.
Handoff Packet -> deliverables/agent/WI-20260717-ATS-007-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
- Map every WI-006 finding identifier to FIXED, DISPROVED, or BLOCKED with evidence.
- Record before/after coverage counters and the executable enforcement command.
- Record exact regression tests for each functional/financial/authentication defect.
- Record all active-document negative searches and validate-docs output.
- Do not claim closure from compilation alone.
- Rollback must identify the track-owned files and explain how to restore behavior without touching unrelated shared-worktree edits.
