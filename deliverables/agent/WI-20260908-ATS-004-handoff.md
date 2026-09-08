---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: cr
category: reference
status: active
dependencies:
  - path: ../user/REQ-20260908-ATS-001.md
    reason: Approved independent regression review
---

# WI-20260908-ATS-004: Independent bounded review

[WI HEADER]
REQ: REQ-20260908-ATS-001 (approved)
Agent: cr
Depends On: WI-20260908-ATS-002, WI-20260908-ATS-003
Blocks: WI-20260908-ATS-005

[WI SUMMARY]
Scope: Review only the current WI001-003 code/test diff. Check lost existing safety guarantees, misleading financial outcome copy, stale upgrade confirmation, duplicate requests, explicit correction dates and timestamp preservation. Review terminal grace tests for false-positive evidence. Do not expand to unrelated old issues or product policy changes.
DoD: Severity-ordered actionable findings with exact source pointers, or explicit no findings; classify actual defects versus boundaries. Main agent owns aggregate build and browser checks; do not duplicate long suites.
Forbidden: Product/test/config/DB changes, real external calls, public process changes, client worktree edits, new agents or commits.

[ACCEPTANCE CRITERIA]
- [ ] Callback query never acts as verified charge outcome or authorizes mutation.
- [ ] Upgrade confirmation cannot submit a different preview/plan/cycle or duplicate charge; existing recovery paths work.
- [ ] Default date and confirmations reduce accidental entitlement changes without inventing server authority.
- [ ] Real positive monetary paths still update lastChargedAt; registration-only paths do not.
- [ ] Tests prove claimed boundaries, not Toss/LIVE/scheduler wall-clock proof.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; docs/standards/development-standards.md; docs/standards/documentation-standards.md; docs/standards/glossary.md.
Tier 1: docs/policies/security-policy.md; docs/policies/quality-gates.md; .claude/agents/cr.md.
Tier 2: WI001-003 handoffs/evidence, approved REQ; current git diff; corresponding unmodified service/DTO/UI contracts; docs/payment/acceptance-test-checklist.md.
Skill chain: react-best-practices, create-wi-evidence-pack. Injection source .claude/config/context-injection-rules.json; cr requires core/development and security for this work.

[WRITE OWNERSHIP / OUTPUT CONTRACT]
Only deliverables/agent/WI-20260908-ATS-004-evidence-pack.md and deliverables/user/WI-20260908-ATS-004-summary.md via create-wi-evidence-pack. Include examined files, findings, test gaps, limits, next WI005 trigger. Report product fixes to MA/owners, do not edit them.

[EXECUTION SAFETY]
Main worktree C:/Users/jm991/Desktop/project/ATStudio, codex/v1-release-rehearsal-fixes. No commit/push. Public runtime and user data preserved. Use apply_patch for the two review documents only.
