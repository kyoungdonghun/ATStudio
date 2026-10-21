---
version: 1.0.0
last_updated: 2026-10-21
project: ATS
owner: DocOps
category: guide
status: draft
dependencies:
  - path: ../../deliverables/user/REQ-20261021-ATS-001.md
    reason: Local preparation scope only
  - path: ../../deliverables/agent/WI-20261021-ATS-003-handoff.md
    reason: Resume guide scope and privacy boundaries
---

# Evening Authentication Trial Resume

> Purpose: Resume the bounded Gmail trial after human availability, without replaying a consumed operation.

**TL;DR:** Server work is paused. Local Git cleanup does not authorize a restart.
The previous v7 trial reached OPEN and the signup UI; signup and mail receipt remain unverified.

## Preserved State

- MA's October21 native evidence: original trial POLICY/BPF/LEDGER PASS, app JAVA_RELEASED; MAIL FAILED because the human deferred before submission. The collation fix passed the earlier ledger blocker.
- Server safety cleanup verified: app and DB stopped/masked, no trial-owned process, quarantine restored, trial firewall table absent. Overall cleanup aggregate remains FAILED; later post-stop checks after scope removal do not rewrite the original BPF PASS.
- Owned SSH key unloaded, agent has no identities, recovery console logged out, signup/console tabs closed. Agent remains Running/Manual; the pending elevation caller was terminated. Administrator stop/disable is deferred until home, not an urgent blocker or a completed restoration claim.
- Existing bounded backup/restore acceptance is not full disaster recovery or production acceptance. REQ-20261011-ATS-007 remains active; this separate cleanup REQ does not close it.

## Protected Artifact Identity

The October11 WI068 JAR is still the reviewed candidate. MA post-commit receipts: `cf3648`643 backend inputs changed0 and JAR unchanged; `43d9b9`336 frontend inputs changed0; latest MA SPA check143 outputs changed0. These are bounded recorded sets; retain historical test-only exclusions and do not infer all-worktree equality. DocOps did not rebuild or independently compare those input sets.

| Recorded object | Public identity |
| --- | --- |
| JAR SHA-256 | `f3b748f8b3fb4fbd4027b797ebf0bc79db62bc9671e5d7e45a8397732d2aef81` |
| SPA output-set SHA-256 (recorded manifest method) | `43a0384c5f48d100e4af6b20d989a2186b34e6900cf4e69c94b010e9671c8647` |
| Historical build HEAD | `58b1011dad796017e47dbcc82a4225ca449bcdf6` |

The historical build HEAD stays unchanged after local commits. Match deployed content by reviewed file manifests/hashes, not by assuming the new Git HEAD was built or deployed. Keep the existing JAR/SPA and registered evidence intact.

## Evening Sequence

1. Human confirms availability at the home PC, with mailbox access and enough time for the bounded window. Address deferred administrator agent restoration explicitly; do not assume it happened. No automatic login or access is needed during this preparation task.
2. MA rechecks current local and remote artifact identity, effective configuration provenance, management IP/access rules, cold state and review validity without exposing values. These are future operational gates, not actions performed by this guide. Direct mobile access is not a reason to widen access rules.
3. Use one continuous authorized SSH work session, not repeated per-step unlocks. Obtain a fresh exact reviewed one-use Release/preflight/producer/Start allowance with a valid window and human readiness. Never run an old consumed command or bypass a HOLD.
4. After actual OPEN, human enters the password, acknowledges consent and submits signup. Do not infer consent from prefilled fields. Use existing Gmail for real SMTP sending to the designated Naver recipient; keep both addresses private. Paid Naver Cloud mail has not been built or integrated.
5. Confirm signup-mail receipt, complete the authorized account verification, and test login/logout. Request and confirm reset-email receipt only: do not open/use the reset link or change the password in this trial. Never copy token-bearing links or credentials into reports.
6. MA performs bounded Finish, verifies server safety and records the original result separately from later post-stop checks. At session end unload only the owned key, log out of the console, and verify agent service restoration with administrator access. Report safety, aggregate cleanup, access and mail outcomes separately.

Stop for unexpected cost, changed security scope or a failed gate. No additional paid mail product, public release, destructive cleanup, automatic replay or push is authorized here.

## Local Git Handoff

MA completed local commit `86f744a` for18 source/test files only. WI002 PG allowed their raw hashes; MA reports staged content matched Git-filtered working content, diff check PASS and no working-file byte changes. DocOps confirmed the commit's18-file stat, not a new functionality test. Historical build HEAD and artifacts remain the provenance above, not this new commit.

Remaining generic deployment files may be deferred for efficiency; no allowance or commit is inferred. New cleanup documents remain pending final PG allowance and any later MA commit. The separate WI001 classification report is pending, while MA post-commit identity is verified as recorded above. Keep historical operational deliverables, generated/private evidence and unrelated documentation changes outside the allowed set, unchanged and unstaged. Do not infer a clean worktree. This guide's eventual commit need not refer to its own hash; local commits are not remote backup, and no push is authorized.

## Related Documents

- [Approved local preparation REQ](../../deliverables/user/REQ-20261021-ATS-001.md)
- [WI003 Evidence Pack](../../deliverables/agent/WI-20261021-ATS-003-evidence-pack.md)
- [WI002 privacy allowance and exclusions](../../deliverables/agent/WI-20261021-ATS-002-evidence-pack.md)
- [Retained operational evidence](../../deliverables/agent/WI-20261011-ATS-062-evidence-pack.md): Local reference only; not permission to commit its operational contents.
