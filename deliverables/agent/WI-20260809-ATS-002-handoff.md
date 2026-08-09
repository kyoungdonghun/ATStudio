[WI HEADER]
WI ID: WI-20260809-ATS-002
REQ: REQ-20260808-ATS-004
Agent: se
Depends On: WI-20260808-ATS-023 failed clean verification
Blocks: WI-20260808-ATS-023~030

[WI SUMMARY]
Why: Repair the stale Track audio replacement integration-test slice exposed by the WI-023 clean backend run.
Scope (in/out): Add the missing `CanonicalImageService` test mock required by the current `TrackService` constructor, rerun the focused integration test, then hand back to WI-023 for a clean full-suite and JaCoCo rerun. No product behavior or production source change.
DoD: The focused test context loads and its rollback assertion passes; the change is limited to test wiring; focused verification and diff checks pass; WI-023 evidence records the repair dependency.
Constraints/Forbidden: Preserve the shared dirty worktree and intentional ZIP. Do not change product code, schema, seed, DB state, dependencies, external systems, secrets, or unrelated tests. No file deletion, stage, commit, or branch operation.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] `TrackAudioReplacementTransactionIntegrationTest` supplies `CanonicalImageService` through the same Spring test-mock mechanism used by its other `TrackService` collaborators.
- [ ] The rollback test still exercises audio replacement and database constraint rollback rather than bypassing `TrackService`.
Quality:
- [ ] Focused test reruns from execution, not only from Gradle cache, and passes.
- [ ] Java compilation and `git diff --check` pass.
- [ ] User summary and Evidence Pack record the exact failure, minimal patch, result, and rollback.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:
- docs/policies/quality-gates.md

Context:
- deliverables/user/REQ-20260808-ATS-004.md
- deliverables/agent/WI-20260808-ATS-023-handoff.md
- deliverables/user/WI-20260808-ATS-023-summary.md
- deliverables/agent/WI-20260808-ATS-023-evidence-pack.md

Files:
- src/test/java/com/atstudio/atstudio/service/TrackAudioReplacementTransactionIntegrationTest.java
- src/main/java/com/atstudio/atstudio/service/TrackService.java
- src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-002-summary.md
Agent-facing -> deliverables/agent/WI-20260809-ATS-002-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260809-ATS-002-handoff.md

[TRACEABILITY REQUIREMENTS]
Record the failed bean dependency, exact changed test lines, focused rerun command/result, scope preservation, rollback, and WI-023 rerun requirement.
