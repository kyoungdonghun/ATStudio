---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: docops
category: reference
status: active
dependencies:
  - path: ../user/REQ-20260908-ATS-002.md
    reason: Approved small presentation work
---

# WI-20260908-ATS-008: Small copy follow-up closure

[WI HEADER]
REQ: REQ-20260908-ATS-002 (approved)
Agent: docops
Depends On: WI-20260908-ATS-006, WI-20260908-ATS-007
Blocks: -

[WI SUMMARY / DoD]
After WI006/007 join, update only related CURRENT terminology, typed-confirm phrase, mail-language disposition and receipt display facts in current payment docs; preserve dated old evidence describing English emails and previous test counts. Replace maintenance status for these small items as completed, but deliverability, actual refund aggregation/new API and server deployment remain unproven. Small targeted patches, not another long acceptance table. Close REQ002 after gates, not SR93 production approval.
Validate docs/diff; reference WI006/007 evidence and MA supplied results rather than duplicating complete prior reports. Preserve old REQ001 and its WI001-005 as dated snapshots.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; documentation-standards.md; development-standards.md; glossary.md (all docs/standards).
Tier 1: docs/policies/security-policy.md; .claude/agents/docops.md.
Tier 2: approved REQ; docs/payment/known-limits-and-next-steps.md; admin-operations-guide.md; payment/user-flows.md; corresponding current code/test files.
Skills: validate-docs; create-wi-evidence-pack. Handoff generated using create-wi-handoff-packet; injection rules .claude/config/context-injection-rules.json, ATS workspace config.

[WRITE OWNERSHIP]
docs/payment/admin-operations-guide.md, known-limits-and-next-steps.md, acceptance-test-checklist.md, index.md; docs/standards/glossary.md only display synonym mapping if relevant existing entry; docs/SR/SR-93.md maintenance disposition only; deliverables/user/REQ-20260908-ATS-002.md progress. Also update the exact current typed-confirmation phrase in docs/ui/modal-list.md, docs/ui/screen-flow.md and docs/design/usecase/user-subscription.md; no unrelated edits to those three files.
Own deliverables/agent/WI-20260908-ATS-008-evidence-pack.md and deliverables/user/WI-20260908-ATS-008-summary.md.
No other worktree, product policy, API/DB, secrets, real provider/mail actions, server changes, commits or nested agents. Preserve all preceding uncommitted work; apply_patch only for manual edits.

[OUTPUT CONTRACT]
Use create-wi-evidence-pack; concise evidence with actual commands/results/diff boundaries and next Blocks trigger. No duplicate exhaustive release audit.
Main worktree C:/Users/jm991/Desktop/project/ATStudio, branch codex/v1-release-rehearsal-fixes. MA performs aggregate verification; do not run full build overwriting active JAR.
