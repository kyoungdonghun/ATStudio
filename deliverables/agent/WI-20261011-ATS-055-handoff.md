---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: guide
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-007.md
    reason: Approved autonomous bounded deployment scope
  - path: WI-20261011-ATS-054-evidence-pack.md
    reason: Reviewed diagnostics baseline
---

# WI-20261011-ATS-055 Handoff

[WI HEADER]
WI ID: WI-20261011-ATS-055
REQ: REQ-20261011-ATS-007 (approved)
Agent: se
Depends On: WI054
Blocks: WI056 independent PG, WI058 MA native trial

[WI SUMMARY]
Why: Remove known wrapper/cleanup defects and package reviewed diagnostics into a fresh operational trial without weakening checks.
Scope: New build/WI055-restricted-web/ candidate derived from WI042 and WI052/v2. Fix HTTP checker explicit success exit and wrapper process contract; durable cross-filesystem overlay preservation/detachment and independent cleanup attempts; integrate diagnostic rows and enable fresh guarded operational entrypoints. Keep management-only, bounded read-only, max30min and existing secrets internal. No server action by agent.
DoD: Focused semantic diff, preservation pins, tested candidate and executable MA recipe, fresh claim/control/service names, exact manifest. Tests cover real function paths, not only string assertions.
Forbidden: Any SSH/UI/cloud/network/DB access, private files, purchases, app builds, old artifacts changed, public opening, bypassing state comparisons, deleting history, commits/push. No speculative secret values.

[ACCEPTANCE CRITERIA]
- [ ] Preserve WI042/WI048/WI052 bytes and original failure history. Use current WI052/v2 runtime/session diagnostic functions, not v1.
- [ ] Fix null/stale LASTEXITCODE and verify a child-process success/failure contract with PowerShell synthetic fixtures. Inner success must not become false HOLD, failed/absent/invalid receipts cannot PASS.
- [ ] Replace cross-filesystem rename-only cleanup using audited exact-byte durable preservation and same-directory detachment. Retain root ownership/mode/no-symlink/no-overwrite checks, original bytes and source identity before/after. Fail closed on partial fsync/copy/detach. Attempt safe service stop/mask and remaining independent cleanup even if an earlier cleanup step fails. Do not falsely claim restored HTTPS when unverified.
- [ ] Preserve security comparisons (including BPF IDs), original read order, short circuit, primary failure and bounded diagnostics. Changes outside that scope must be a specific finding, not silently normalized.
- [ ] Refresh only execution identity/packaging for a NEW one-shot claim, service, control folder and helper imports. Never reuse consumed claims or run LOCAL_ONLY main. Prevent accidental old-module import/pin drift.
- [ ] Fresh native preflight before writes, original quarantine HTTPS recovery, stop app/DB after bounded trial and watchdog. Recipe must include exact upload/hash and explicit result/exit handling, cleanup/status checks and limits. Do not duplicate fragile wrapper mechanics.
- [ ] Regression: historical106, diagnostic100 (overlap reported), targeted new failure/cleanup tests, PowerShell parse/exit tests. Linux-only durability must remain unproven until actual host testing.
- [ ] Provide frozen hashes/recipe and evidence; PG must review before MA executes.

[INPUT POINTERS]
Tier0 (MA injected CE projection): docs/standards/core-principles.md; docs/standards/documentation-standards.md; docs/standards/development-standards.md; docs/standards/glossary.md.
Tier1: docs/policies/security-policy.md; docs/policies/quality-gates.md; .claude/agents/se.md.
Tier2: .agents/skills/react-best-practices/AGENTS.md and docs/standards/frontend-standards.md are conditional pointers only (no frontend source edits). REQ007; WI045 native receipt/evidence; WI048/049 completed HTTPS-only repair; WI050 diagnosis; WI052/v2 and WI053 final review.
Files: build/WI042-restricted-web-check/; build/WI052-state-diagnostics/v2/; build/WI046-quarantine-recovery/; build/WI048-quarantine-continuation/.
Skills: test; create-wi-evidence-pack. Never gradlew clean.

[OUTPUT CONTRACT]
Write scope only build/WI055-restricted-web/**, deliverables/agent/WI-20261011-ATS-055-evidence-pack.md, deliverables/user/WI-20261011-ATS-055-summary.md. apply_patch manual edits; exact-path mechanical copies allowed. Report when candidate code ready for PG, then finish two-set evidence. No subdelegation or shared docs edits.

[TRACEABILITY REQUIREMENTS]
Record before/after old pins, all tests with OS and commands, source diff and rollback. Operational authority belongs to MA and only after independent review/current preflight. If exact state changes recur, capture safe item-level diagnosis, do not make them PASS by suppression.
