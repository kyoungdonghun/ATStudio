[WI HEADER]
WI ID: WI-20260525-ATS-006
REQ: REQ-20260525-ATS-004
Agent: re/qa-integ
Depends On: WI-20260525-ATS-004
Blocks: WI-20260525-ATS-008

[WI SUMMARY]
Why: Refund correctness depends on money amount, provider state, auditability, and non-mutation of entitlements.
Scope (in/out): Define and implement focused backend test coverage for refund workflow. Exclude frontend tests unless frontend code changes.
DoD: Tests cover provider request details, approval gate, partial refund cap, sanitized payload, and no entitlement auto-correction.
Constraints/Forbidden: Do not require live Toss keys or real money movement.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Provider cancel request test uses local HTTP server.
- [ ] Service tests cover request/approve/execute paths.
- [ ] Controller/admin API contract tests cover endpoint wiring if practical.
Quality:
- [ ] `gradlew.bat test` passes.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/policies/quality-gates.md

Tier 2:
- deliverables/user/REQ-20260525-ATS-004.md
- docs/design/payment-refund-receipt-settlement-policy.md
- src/test/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProviderTest.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260525-ATS-006-summary.md
Agent-facing -> deliverables/agent/WI-20260525-ATS-006-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260525-ATS-006-handoff.md

[TRACEABILITY REQUIREMENTS]
Test commands, pass/fail output, and uncovered residual risks are required.
