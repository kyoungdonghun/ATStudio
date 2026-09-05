# WI Handoff Packet: WI-20260823-ATS-006

[WI HEADER]
WI ID: WI-20260823-ATS-006
REQ: REQ-20260823-ATS-001
Agent: se
Depends On: WI-20260823-ATS-005
Blocks: WI-20260823-ATS-007

[WI SUMMARY]
Why: Enforce the approved invariant that `job` is INDIVIDUAL-only at every server mutation boundary after the final review showed that a direct BUSINESS request can persist it.

Scope (in):
- Update the established registration, complete-profile, and update-profile validation/service boundary so a BUSINESS request containing a non-null `job` is rejected before persistence.
- Keep BUSINESS `companyName` as the required existing descriptor and INDIVIDUAL `job` behavior unchanged.
- Add focused backend tests for register, complete-profile, and update-profile direct payloads with BUSINESS plus job.
- Update current documentation only if the implementation needs a contract clarification beyond its existing wording.

Scope (out):
- No schema/data migration, no cleanup of historical records, no new industry field, no client worktree change, no external call, and no profile API contract expansion.

DoD:
- A direct BUSINESS payload with `job` cannot reach User persistence in every relevant write path.
- Valid BUSINESS payloads with `job=null` and valid INDIVIDUAL job flows remain accepted.
- Focused and full backend test results are recorded, plus diff check.

[ACCEPTANCE CRITERIA]
- [ ] Register rejects BUSINESS + non-null job.
- [ ] Complete profile rejects BUSINESS + non-null job.
- [ ] Update profile rejects BUSINESS + non-null job.
- [ ] No permitted path silently stores `UserJob` for BUSINESS.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2:
- docs/design/api-spec.md
- docs/design/usecase/user-info.md

REQ/Context Docs:
- deliverables/user/REQ-20260823-ATS-001.md
- deliverables/agent/WI-20260823-ATS-005-evidence-pack.md

Files:
- src/main/java/com/atstudio/atstudio/common/validation/RegisterProfileValidator.java
- src/main/java/com/atstudio/atstudio/common/validation/CompleteProfileValidator.java
- src/main/java/com/atstudio/atstudio/service/UserService.java
- src/test/java/com/atstudio/atstudio/service/UserServiceTest.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260823-ATS-006-summary.md
Agent-facing -> deliverables/agent/WI-20260823-ATS-006-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260823-ATS-006-handoff.md

[TRACEABILITY REQUIREMENTS]
- Use apply_patch.
- Prefer existing validation/error handling patterns; do not introduce a new schema/API field.
- Record test evidence for all three direct payload paths.
