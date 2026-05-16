[WI HEADER]
WI ID: WI-20260516-ATS-005
REQ: REQ-20260516-ATS-001
Agent: re
Depends On: WI-20260516-ATS-004
Blocks: WI-20260516-ATS-008

[WI SUMMARY]
Why: Verify backend payment and subscription regression behavior after integration.
Scope (in): Run and strengthen backend tests for payment prepare/confirm/cancel, idempotency, subscribe, upgrade, downgrade, and controller contract.
Scope (out): Frontend tests, browser E2E, Toss live API tests.
DoD: Backend tests cover the REQ success criteria and pass.
Constraints/Forbidden: Do not weaken existing subscription regression tests to pass.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] SUBSCRIBE confirm success creates subscription once.
- [ ] UPGRADE confirm success applies upgrade once.
- [ ] DOWNGRADE remains payment-free.
- [ ] Failure/cancel does not create or upgrade subscriptions.
- [ ] Idempotent confirm does not create duplicates.
Performance:
- [ ] Tests do not require network or external payment services.
Quality:
- [ ] `./gradlew.bat test` or targeted backend tests pass.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 1 (Policies - Inferred):
- docs/policies/quality-gates.md
- docs/standards/evidence-pack-standard.md

Tier 2 (Design/Context):
- docs/design/payment-integration-design.md

REQ/Context Docs:
- deliverables/user/REQ-20260516-ATS-001.md
- deliverables/agent/WI-20260516-ATS-004-handoff.md

Files:
- src/test/java/com/atstudio/atstudio/service
- src/test/java/com/atstudio/atstudio/controller
- src/main/java/com/atstudio/atstudio/service
- src/main/java/com/atstudio/atstudio/controller

Repro/Logs:
- `./gradlew.bat test --tests "com.atstudio.atstudio.service.UserSubscriptionServiceTest"`
- `./gradlew.bat test --tests "com.atstudio.atstudio.controller.*Payment*"`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260516-ATS-005-summary.md:
- Summary, test results, risks.
Agent-facing -> deliverables/agent/WI-20260516-ATS-005-evidence-pack.md:
- Test matrix, command output summary, patch notes.
Handoff Packet -> deliverables/agent/WI-20260516-ATS-005-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Required.
Rollback: Document any test helper changes.
