[WI HEADER]
WI ID: WI-20260713-ATS-005
REQ: REQ-20260713-ATS-001
Agent: se
Depends On: WI-20260713-ATS-002
Blocks: WI-20260713-ATS-008

[WI SUMMARY]
Why: Guarantee that account withdrawal stops local recurring billing before any fallible Toss cleanup and leaves a durable retry path.
Scope (in/out): Cancel local agreement/subscription in withdrawal, publish ID-only after-commit cleanup, retry failed provider deletion for deleted users, record/resolve a deduplicated existing reconciliation incident, exclude deleted users from renewal query, add a service guard, and add focused tests. No auto-refund, schema change, live Toss call, or payment policy expansion.
DoD: Withdrawal commits local non-chargeable state independent of Provider result; failed cleanup leaves key material for retry and an open incident; successful retry clears key and resolves incident; deleted users produce zero charge calls.
Constraints/Forbidden: Follow `docs/design/p0-release-blocker-remediation-design.md`. Reuse `LOCAL_DONE_PROVIDER_NOT_DONE`; do not add a DB enum. Do not edit track or mail files. Do not touch runtime logs.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Withdrawal marks user, ACTIVE subscription, and non-terminal agreement locally cancelled.
- [ ] Provider cleanup runs only after commit and receives no raw key in the event.
- [ ] Provider failure cannot reactivate or roll back local cancellation.
- [ ] Daily retry is limited to deleted users with CANCELLED agreements and retained key material.
- [ ] Due-renewal query and service guard exclude deleted users.
Performance:
- [ ] Due filtering occurs in the repository query; retry scans only the targeted state.
Quality:
- [ ] Withdrawal, cleanup success/failure/retry, incident dedupe/resolve, and zero-charge tests pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/design/p0-release-blocker-remediation-design.md
- docs/design/payment-integration-design.md
- docs/design/payment-operations-runbook.md
REQ/Context Docs:
- deliverables/user/REQ-20260713-ATS-001.md
- deliverables/agent/WI-20260713-ATS-002-evidence-pack.md
Files (owned):
- src/main/java/com/atstudio/atstudio/service/UserService.java
- src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java
- src/main/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentService.java
- src/main/java/com/atstudio/atstudio/repository/BillingAgreementRepository.java
- new withdrawal billing-cleanup event/service files
- focused user/renewal/cleanup/incident tests

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260713-ATS-005-summary.md
Agent-facing -> deliverables/agent/WI-20260713-ATS-005-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260713-ATS-005-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers, transaction-order assertions, provider invocation counts, and exact test commands: Required
Rollback: Revert product/test files; no schema or data rollback is needed.
