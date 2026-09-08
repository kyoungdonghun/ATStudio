---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: re
category: reference
status: active
dependencies:
  - path: ../user/REQ-20260908-ATS-001.md
    reason: Approved closeout scope
---

# WI-20260908-ATS-001: Terminal renewal failure verification

[WI HEADER]
REQ: REQ-20260908-ATS-001 (approved)
Agent: re
Depends On: -
Blocks: WI-20260908-ATS-002

[WI SUMMARY]
Why: One provider TEST failure followed by recovery was exercised; terminal consecutive failure and expired entitlement remain unproven in this acceptance run.
Scope: Add focused H2 integration/service tests using existing fixtures. Exercise repeated failure, retry gate consumption, final suspension, grace boundary, no new charges after suspension, and refusal of first downloads after expiry while licensed re-download remains allowed.
Out: Live DB/provider/mail, account state mutations, product policy changes, application source changes unless reported and separately assigned, server restart, client worktree.
DoD: Reproducible focused tests pass with no external side effects; evidence distinguishes fake provider/H2 from actual Toss/MySQL/timer proof.

[ACCEPTANCE CRITERIA]
- [ ] Consecutive failures share one logical renewal order; bounded attempts and same-day duplicate suppression are checked.
- [ ] Final failure suspends billing but preserves contractual grace access; expiry excludes first-download entitlement.
- [ ] New first download denied after expiry, licensed repeat behavior preserved.
- [ ] Counts and test command are recorded without exposing real account/secret data.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; docs/standards/development-standards.md; docs/standards/documentation-standards.md; docs/standards/glossary.md.
Tier 1: docs/policies/security-policy.md; docs/policies/quality-gates.md.
Tier 2: docs/payment/acceptance-test-checklist.md sections 6-7; docs/SR/SR-93.md; src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java; PaymentCommandTransactionService.java; DownloadService.java; src/test/java/com/atstudio/atstudio/service/RecurringRenewalCommandIntegrationTest.java; DownloadServiceTest.java; BillingAgreementCommandIntegrationTestSupport.java.
Skill chain: .agents/skills/test/SKILL.md -> .agents/skills/create-wi-evidence-pack/SKILL.md.
Injection rules: .claude/config/context-injection-rules.json; re required core-principles; service/integration context additionally includes development and security standards.

[WRITE OWNERSHIP]
- src/test/java/com/atstudio/atstudio/service/RecurringRenewalCommandIntegrationTest.java
- src/test/java/com/atstudio/atstudio/service/DownloadServiceTest.java
- Focused test support only if essential; no build/dependency changes.
- This WI evidence and summary files.

[OUTPUT CONTRACT]
Two-set deliverables: deliverables/agent/WI-20260908-ATS-001-evidence-pack.md and deliverables/user/WI-20260908-ATS-001-summary.md, using create-wi-evidence-pack skill. Include source pointers, executed commands/counts, unverified limits, rollback (test-only diff), discovered defects, and explicit next WI-002 trigger.

[EXECUTION SAFETY]
Workdir C:/Users/jm991/Desktop/project/ATStudio, branch codex/v1-release-rehearsal-fixes. JAVA_HOME=C:/Program Files/Java/jdk-17. Do not load ignored local YAML or runtime secrets. Tests must use H2 and stub provider/SMTP. No commit, push, cleanup or process shutdown.
