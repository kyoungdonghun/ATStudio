---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: se
category: reference
status: active
dependencies:
  - path: ../user/REQ-20260908-ATS-001.md
    reason: Approved billing timestamp fix
---

# WI-20260908-ATS-003: Preserve monetary charge timestamps

[WI HEADER]
REQ: REQ-20260908-ATS-001 (approved)
Agent: se
Depends On: WI-20260908-ATS-001
Blocks: WI-20260908-ATS-004

[WI SUMMARY]
Why: Zero-amount billing-method re-registration clears and rewrites lastChargedAt despite no monetary payment.
Scope: Minimal BillingAgreement/PaymentCommandTransactionService changes and corresponding entity/service/integration tests; correct the stale next_billing_at SQL comment only (not DDL).
DoD: Registration preserves prior actual charge timestamp; first-ever zero-charge registration does not invent one; actual subscribe/upgrade/renewal charge still updates it. No period/status/key cleanup behavior change.
Forbidden: DB/real account mutations, new schema/API/dependencies, provider/mail calls, server restarts, frontend/peer files, cleanup, branch changes or commits.

[ACCEPTANCE CRITERIA]
- [ ] Read actual call paths and preserve actual monetary timestamp through re-registration prepare/finalize and relevant key cleanup without changing key/status semantics.
- [ ] Cover previous timestamp and null case, failed preparation/retry, real monetary finalization and replay safety.
- [ ] Focused JUnit passes and source-only schema comment correction matches expiresAt contract.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; docs/standards/development-standards.md; docs/standards/documentation-standards.md; docs/standards/glossary.md.
Tier 1: docs/policies/security-policy.md; docs/policies/quality-gates.md.
Tier 2: docs/payment/acceptance-test-checklist.md; docs/design/payment-integration-design.md; src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java; src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java; related tests; src/main/resources/schema.sql.
Skill chain: test -> create-wi-evidence-pack. Rule source .claude/config/context-injection-rules.json; se core/development plus billing/security contract.

[WRITE OWNERSHIP]
- BillingAgreement.java, PaymentCommandTransactionService.java and directly corresponding tests (do not edit WI-001 test files without coordination).
- schema.sql next_billing_at comment only.
- Own evidence and summary files.

[OUTPUT CONTRACT]
Create deliverables/agent/WI-20260908-ATS-003-evidence-pack.md and deliverables/user/WI-20260908-ATS-003-summary.md using create-wi-evidence-pack. Include exact tests, preserved semantics, runtime not redeployed, rollback and next WI-004 trigger.

[EXECUTION SAFETY]
Main worktree C:/Users/jm991/Desktop/project/ATStudio, branch codex/v1-release-rehearsal-fixes. JAVA_HOME C:/Program Files/Java/jdk-17. Use H2/stubs, no ignored local credentials. Use apply_patch. No nested agents, no commit/push. Coordinate Gradle with MA; focused Gradle may run before MA full build.
