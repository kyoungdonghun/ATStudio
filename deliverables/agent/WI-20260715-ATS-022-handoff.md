[WI HEADER]
WI ID: WI-20260715-ATS-022
REQ: REQ-20260715-ATS-001
Agent: qa-integ
Depends On: WI-20260715-ATS-018, WI-20260715-ATS-019, WI-20260715-ATS-021
Blocks: WI-20260715-ATS-023

[WI SUMMARY]
Why: Independently verify the complete-listening restoration across backend, frontend, security/download boundaries, and active documentation before freezing a client demo checkpoint.
Scope (in/out): In: read-only diff review, full backend/frontend quality gates, docs validation, changed-file formatting, traceability and cross-layer contract checks. Out: product fixes, branch/commit/server/tunnel operations, live Provider calls, DB/schema/data mutation.
DoD: All required gates pass or exact blockers are recorded; no bounded-preview behavior remains in current code/docs; static original denial and protected download regressions remain demonstrably intact; only required QA deliverables are written.
Constraints/Forbidden: Do not modify product code/current-state docs, do not stage/commit, do not stop/start services, do not call live payment/email/OAuth, and do not touch runtime logs. You are not alone in the codebase; preserve concurrent work.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Backend full-resource/no-Range/Range behavior agrees with API and SOUND-010.
- [ ] Public DTO key redaction and `/uploads/tracks/audio/**` denial remain.
- [ ] Official download first-use subscription/quota/ledger/License and licensed re-download behavior remain.
- [ ] Player state follows play Promise/media error outcomes and normal transient buffering is not made fatal.
- [ ] Active docs contain no stale bounded-preview current claim.
Quality:
- [ ] `gradlew.bat test` passes.
- [ ] Frontend typecheck, ESLint, test, and build pass.
- [ ] Changed frontend files pass Prettier check.
- [ ] Documentation validation and `git diff --check` pass.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1 (Policies):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2 (Context):
- deliverables/user/REQ-20260715-ATS-001.md
- deliverables/agent/WI-20260715-ATS-018-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-019-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-021-evidence-pack.md
- docs/design/api-spec.md
- docs/design/usecase/sound-track.md
- docs/audit/p0-release-blocker-closure-20260713.md
Files:
- src/main/java/com/atstudio/atstudio/service/TrackService.java
- src/main/java/com/atstudio/atstudio/controller/TrackController.java
- src/main/java/com/atstudio/atstudio/config/SecurityConfig.java
- src/test/java/com/atstudio/atstudio/
- frontend/src/store/playerStore.ts
- frontend/src/layouts/PlayerBar.tsx
- frontend/src/store/playerStore.test.ts
- frontend/src/layouts/PlayerBar.test.tsx

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260715-ATS-022-summary.md
Agent-facing -> deliverables/agent/WI-20260715-ATS-022-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260715-ATS-022-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required for each cross-layer decision and command.
Tests: Record exact command, exit, counts, and known baseline exclusions.
Rollback: QA deliverables only; no product rollback is permitted in this WI.
