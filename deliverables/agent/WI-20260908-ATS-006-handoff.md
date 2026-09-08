---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: se
category: reference
status: active
dependencies:
  - path: ../user/REQ-20260908-ATS-002.md
    reason: Approved small presentation work
---

# WI-20260908-ATS-006: Korean payment email copy

[WI HEADER]
REQ: REQ-20260908-ATS-002 (approved)
Agent: se
Depends On: -
Blocks: WI-20260908-ATS-008

[WI SUMMARY / DoD]
Translate customer renewal failure subject/heading/greeting/defaults and both retry/final suspension branches, plus operator reconciliation email framing/summary labels. Preserve exact grace dates/retry semantics, identifiers/status codes, escaping and secret-safe logs. Verification/reset emails already Korean; do not churn them. Remove unnecessary customer-facing authKey/billingKey/provider-secret boilerplate or replace with short plain Korean safety text, without security behavior changes.
Focused unit/service tests must assert generated MIME Korean UTF-8 text, escaped adversarial dynamic data, intermediate vs final failure guidance and retained retry date/order identity. Existing private provider response sanitization unchanged. No SMTP send.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; documentation-standards.md; development-standards.md; glossary.md (all docs/standards).
Tier 1: docs/policies/security-policy.md; .claude/agents/se.md.
Tier 2: approved REQ; docs/payment/known-limits-and-next-steps.md; admin-operations-guide.md; payment/user-flows.md; corresponding current code/test files.
Skills: test, react-best-practices for frontend; create-wi-evidence-pack. Handoff generated using create-wi-handoff-packet; injection rules .claude/config/context-injection-rules.json, ATS workspace config.

[WRITE OWNERSHIP]
Only EmailService.java, RecurringRenewalService.java, PaymentReconciliationIncidentService.java under src/main/java/com/atstudio/atstudio/service and their corresponding tests (including RecurringRenewalCommandIntegrationTest if needed); no other ownership.
Own deliverables/agent/WI-20260908-ATS-006-evidence-pack.md and deliverables/user/WI-20260908-ATS-006-summary.md.
No other worktree, product policy, API/DB, secrets, real provider/mail actions, server changes, commits or nested agents. Preserve all preceding uncommitted work; apply_patch only for manual edits.

[OUTPUT CONTRACT]
Use create-wi-evidence-pack; concise evidence with actual commands/results/diff boundaries and next Blocks trigger. No duplicate exhaustive release audit.
Main worktree C:/Users/jm991/Desktop/project/ATStudio, branch codex/v1-release-rehearsal-fixes. MA performs aggregate verification; do not run full build overwriting active JAR.
