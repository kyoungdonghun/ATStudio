# WI-20261024-ATS-022 Handoff

## WI Header
- REQ: REQ-20261011-ATS-007; direct user-approved governance amendment on 2026-10-24.
- Agent: DocOps
- Depends On: none
- Blocks: none

## Approved Scope
The user explicitly requests updating AGENTS.md: after scope/execution approval, perform in-scope error recovery, tool corrections, review and safe retries without repeated permission requests. Ask only for new costs, out-of-scope changes or tool-mandated confirmation. Apply from stage two onward. This authorizes this documentation edit, not stage-two runtime operations.

## Acceptance Criteria
- Replace conflicting blanket always-ask exceptions in Approval Before Execution with scope-aware rules.
- Include in-scope unexpected blockers that can be resolved without expanding impact; progress reports are not approval stops.
- Preserve user handoff and tool-required confirmations; do not bypass safeguards.
- Unknown-outcome writes/payments require read-only reconciliation before retry, never blind duplicate execution. Repeated same-cause failures require diagnosis and focused verification, not whole-trial replay.
- Do not authorize new expenditure, scope expansion, unapproved destructive work or automatic stage-two execution.
- Preserve unrelated dirty edits. Change only the relevant AGENTS.md section and this WI's evidence/summary records. No server, secrets, build, tests, commit or push.

## Input Pointers
- Tier 0: docs/standards/core-principles.md; docs/standards/documentation-standards.md; docs/standards/glossary.md; docs/standards/development-standards.md.
- Context: current conversation's explicit approval; AGENTS.md; docs/index.md current entry; deliverables/user/REQ-20261011-ATS-007.md.
- Skills: .agents/skills/create-wi-handoff-packet/SKILL.md; .agents/skills/create-wi-evidence-pack/SKILL.md.

## Output Contract
- AGENTS.md targeted change.
- deliverables/agent/WI-20261024-ATS-022-evidence-pack.md and deliverables/user/WI-20261024-ATS-022-summary.md, concise, with direct approval attribution, diff check and no next blocked WI.
- Use English policy prose consistent with documentation standards; explain precise change in Korean to MA.
