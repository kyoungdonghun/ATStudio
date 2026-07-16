[WI HEADER]
WI ID: WI-20260716-ATS-031
REQ: REQ-20260716-ATS-002
Agent: se
Depends On: WI-20260716-ATS-030
Blocks: WI-20260716-ATS-032

[WI SUMMARY]
Why: Close the three findings reopened by the independent integrated review before release-readiness verification.
Scope (in):
- Add a no-schema stale-revision fence so an approved entitlement correction cannot cancel a billing agreement that was re-registered after correction creation, even when its status returns to ACTIVE.
- Sanitize retained Provider identifiers using colon-labelled and equivalent supported label separators before ADMIN DTO serialization.
- Replace the non-existent currentPeriodEnd documentation term with the implemented expiresAt field.
- Add focused regression tests for each executable change and update paired WI deliverables.
Scope (out):
- Schema or migration changes, Provider calls, retained/live DB mutation, payment policy changes, public/client runtime changes, client branch/worktree changes, and Git staging/commit/push.
DoD:
- F-025-03, F-025-05, and F-027-03 are closed with source, tests, and documentation evidence.
- Same-status ACTIVE -> completed payment-method re-registration -> ACTIVE drift is rejected before correction mutation.
- Colon-labelled Provider IDs cannot be serialized raw in support-reference or ADMIN audit/Incident DTO paths.
- Focused backend tests and documentation validation pass.
Constraints/Forbidden:
- Work only in C:/Users/jm991/Desktop/project/ATStudio on branch codex/p1-acceptance-hardening.
- You are not alone in the worktree. Do not revert, restore, reformat, or overwrite unrelated edits.
- Do not touch C:/Users/jm991/Desktop/project/ATStudio-client-demo-stable or the public Cloudflare/client runtime.
- Do not change schema.sql, migrations, DB data, Provider state, secrets, frontend/tsconfig.tsbuildinfo, or Git index/history.
- Prefer an existing persisted revision signal such as agreement updatedAt compared with correction createdAt; handle null/equal timestamp behavior explicitly and conservatively.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Executing a correction after a completed same-status billing-agreement replacement fails as stale without cancelling the replacement agreement.
- [ ] Existing valid corrections still execute when the agreement has not changed.
- [ ] transactionId/paymentKey/orderId labels using '=' or ':' plus reasonable whitespace are sanitized; partial raw identifiers are not retained in ADMIN serialization.
- [ ] Documentation uses expiresAt consistently with the wire model.
Performance:
- [ ] No new network call, table scan, or schema dependency is introduced.
Quality:
- [ ] Focused JUnit tests pass.
- [ ] Documentation validation and git diff --check pass for the owned slice.
- [ ] No unrelated file is changed.

[INPUT POINTERS]
Tier 0 (Constitution - Required):
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1 (Policies):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2 (Domain design):
- docs/design/payment-integration-design.md
- docs/design/usecase/sound-track.md

REQ/Context Docs:
- deliverables/user/REQ-20260716-ATS-002.md
- deliverables/user/WI-20260716-ATS-030-summary.md
- deliverables/agent/WI-20260716-ATS-030-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-028-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-029-evidence-pack.md

Files:
- src/main/java/com/atstudio/atstudio/entity/PaymentEntitlementCorrection.java
- src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java
- src/main/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionService.java
- src/main/java/com/atstudio/atstudio/service/payment/ProviderSupportReference.java
- src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentOperationAuditLogResponse.java
- src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentReconciliationIncidentResponse.java
- src/test/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionServiceTest.java
- src/test/java/com/atstudio/atstudio/dto/payment/ProviderSupportReferenceTest.java
- src/test/java/com/atstudio/atstudio/dto/payment/AdminProviderIdentifierContractTest.java
- docs/standards/glossary.md
- docs/design/usecase/sound-track.md

Repro/Logs:
- F-025-03, F-025-05, and F-027-03 reproduction and pointers in WI-20260716-ATS-030 evidence pack.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-031-summary.md:
- Findings closed, implementation summary, residual risks, and verification results.
Agent-facing -> deliverables/agent/WI-20260716-ATS-031-evidence-pack.md:
- Exact evidence pointers, changed-file inventory, tests, rollback, and follow-up WI.
Handoff Packet -> deliverables/agent/WI-20260716-ATS-031-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required for each reopened finding and each changed file.
Tests: Record exact commands and pass/fail counts.
Rollback: Describe file-scoped rollback without touching unrelated worktree edits.
