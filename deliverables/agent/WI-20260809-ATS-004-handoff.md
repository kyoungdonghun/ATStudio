[WI HEADER]
WI ID: WI-20260809-ATS-004
REQ: REQ-20260808-ATS-004
Agent: cr
Depends On: WI-20260808-ATS-023~027
Blocks: WI-20260808-ATS-028

[WI SUMMARY]
Why: Review the privileged backend mutations for SR-96 and SR-97 without mixing frontend concerns into the same context.
Scope (in/out): Last-admin protection, role mutation, subscription-correction state machine, authorization rechecks, concurrency, audit durability, entitlement effects, and provider isolation. Frontend presentation is out of scope.
DoD: Evidence-backed BLOCKER/MAJOR/MINOR findings or an explicit no-findings result, with tight file-line pointers and residual test risks.
Constraints/Forbidden: Read-only review. Do not modify code, tests, schema, data, secrets, the intentional ZIP, or external provider state. Do not commit or push.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Review last-admin and subscription-correction state transitions under concurrent requests.
- [ ] Verify audit, authorization, entitlement, and payment-provider boundaries.
Performance:
- [ ] Identify lock-order or repeated-query risks that affect correctness.
Quality:
- [ ] Every finding has severity, evidence, impact, and a recommended repair/test.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md

Context:
- deliverables/user/REQ-20260808-ATS-004.md
- docs/SR/SR-96.md
- docs/SR/SR-97.md
- deliverables/agent/WI-20260808-ATS-028-handoff.md

Files:
- src/main/java/com/atstudio/atstudio/service/UserService.java
- src/main/java/com/atstudio/atstudio/repository/UserRepository.java
- src/main/java/com/atstudio/atstudio/controller/UserController.java
- src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java
- src/main/java/com/atstudio/atstudio/controller/AdminUserSubscriptionCorrectionController.java
- src/main/java/com/atstudio/atstudio/service/AdminOperationAudit*.java
- src/main/java/com/atstudio/atstudio/entity/AdminSubscriptionCorrection.java
- src/main/java/com/atstudio/atstudio/repository/AdminSubscriptionCorrectionRepository.java
- src/main/resources/schema.sql
- corresponding focused tests only

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-004-summary.md
Agent-facing -> deliverables/agent/WI-20260809-ATS-004-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260809-ATS-004-handoff.md

[TRACEABILITY REQUIREMENTS]
Reviewed symbols, findings, residual risks, rollback implications, and WI-028 block status are required.
