[WI HEADER]
WI ID: WI-20260818-ATS-001
REQ: REQ-20260818-ATS-001
Agent: se
Depends On: -
Blocks: -

[WI SUMMARY]
Why: Missing static-resource requests are currently classified by the generic exception fallback and become HTTP 500.
Scope (in/out): Modify only `GlobalExceptionHandler` and its focused regression test to classify Spring MVC `NoResourceFoundException` as `BUSINESS_ERROR.RESOURCE_NOT_FOUND`. Do not alter any runtime, configuration, database, external integration, or unrelated error mapping.
DoD: The framework exception receives the normal JSON 404/`RESOURCE_NOT_FOUND` response, and a regression test passes.
Constraints/Forbidden: Do not start/stop/restart ports 5173/8080; do not access secrets, mutate data, call provider/mail services, or expand the change beyond the two scoped Java files.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] `NoResourceFoundException` maps to HTTP 404 and `RESOURCE_NOT_FOUND`.
- [ ] Existing generic unexpected exception handling remains HTTP 500.
Quality:
- [ ] Focused `GlobalExceptionHandlerTest` passes.
- [ ] Run the relevant backend test task and report the exact result.
- [ ] `git diff --check` passes.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/quality-gates.md

REQ/Context Docs:
- deliverables/user/REQ-20260818-ATS-001.md

Files:
- src/main/java/com/atstudio/atstudio/common/exception/GlobalExceptionHandler.java
- src/test/java/com/atstudio/atstudio/common/exception/GlobalExceptionHandlerTest.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260818-ATS-001-summary.md:
- Korean summary, changed paths, test result, and no-runtime-interference statement.
Agent-facing -> deliverables/agent/WI-20260818-ATS-001-evidence-pack.md:
- Patch notes, exact verification command/results, and rollback guidance.
Handoff Packet -> deliverables/agent/WI-20260818-ATS-001-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required.
Tests: Include focused test and diff check.
Rollback: Revert only this WI's two Java files and its deliverables; no runtime rollback is needed.
