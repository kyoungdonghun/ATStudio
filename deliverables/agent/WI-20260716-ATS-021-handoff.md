[WI HEADER]
WI ID: WI-20260716-ATS-021
REQ: REQ-20260716-ATS-002
Agent: se
Depends On: WI-20260716-ATS-020
Blocks: Final development-branch release-readiness re-verification

[WI SUMMARY]
Why: Remediate F-020-01, where the ADMIN on-demand reconciliation response returns the service-internal raw provider transaction identifier.
Scope (in):
- Replace direct exposure of `PaymentReconciliationService.ProviderReconciliationIssue` in `AdminPaymentReconciliationResponse` with an ADMIN-safe response record.
- Map `providerTransactionId` to deterministic `providerReference` via the existing `ProviderSupportReference` helper.
- Keep service-internal reconciliation evidence unchanged for incident persistence and provider operations.
- Add DTO/serialization/controller contract tests proving the response has `providerReference`, contains no raw provider identifier field/value, and matches the documented API shape.
- Update current docs only if implementation details require a correction; the documented `providerReference` contract should remain authoritative.
- Create WI-021 summary and Evidence Pack.
Scope (out):
- Provider reconciliation algorithms, incident persistence, logs already fixed by WI-018, frontend UI, DB schema, provider calls, client propagation.
DoD:
- `/api/admin/payments/reconciliation` can no longer serialize `providerTransactionId` or the raw identifier value.
- Safe provider issue fields and aggregate/truncation metadata remain available.
- Existing provider reconciliation tests remain green.
- Focused contract tests and affected quality checks pass.
Constraints/Forbidden:
- Work only in the development worktree/branch.
- Do not modify service-internal issue records solely to satisfy the API boundary.
- Do not weaken or remove reconciliation evidence needed internally.
- Do not stage, commit, push, delete, mutate DB/provider/client/runtime, or alter `frontend/tsconfig.tsbuildinfo`.
- Preserve unrelated cumulative WI changes.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] ADMIN reconciliation provider issues expose `providerReference` and never `providerTransactionId`.
- [ ] The reference is deterministic `REF-*` and does not contain the raw provider identifier.
- [ ] Local/provider aggregate counts and issue truncation fields are unchanged.
- [ ] Internal incident recording continues to receive exact provider evidence where required.
Performance:
- [ ] Mapping is bounded by the existing issue detail limit and adds no provider/DB calls.
Quality:
- [ ] DTO record-contract test includes `AdminPaymentReconciliationResponse` nested provider issue type.
- [ ] Serialized response test rejects raw field names and raw identifier values.
- [ ] Focused backend tests and `git diff --check` pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2:
- docs/design/api-spec.md
- docs/design/payment-operations-runbook.md

REQ/Context Docs:
- deliverables/user/REQ-20260716-ATS-002.md
- deliverables/agent/WI-20260716-ATS-020-evidence-pack.md

Files:
- src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentReconciliationResponse.java
- src/main/java/com/atstudio/atstudio/dto/payment/ProviderSupportReference.java
- src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java
- src/main/java/com/atstudio/atstudio/service/AdminPaymentReadService.java
- src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java
- src/test/java/com/atstudio/atstudio/dto/payment/AdminProviderIdentifierContractTest.java
- src/test/java/com/atstudio/atstudio/controller/PaymentControllerTest.java
- src/test/java/com/atstudio/atstudio/service/PaymentReconciliationServiceTest.java

Repro:
- Construct a `ProviderReconciliationResult` issue with a sentinel raw transaction ID and serialize `AdminPaymentReconciliationResponse.from(...)`.
- Confirm JSON contains `providerReference` and does not contain `providerTransactionId` or the sentinel.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-021-summary.md:
- Fix summary, preserved internal evidence, tests, and readiness boundary.
Agent-facing -> deliverables/agent/WI-20260716-ATS-021-evidence-pack.md:
- Exact mappings, serialized contract evidence, commands/results, rollback/follow-up.
Handoff Packet -> deliverables/agent/WI-20260716-ATS-021-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required.
Tests: Exact focused commands/results required.
Rollback: Revert the ADMIN DTO mapping and tests together; do not alter internal reconciliation records.
