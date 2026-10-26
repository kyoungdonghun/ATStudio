---
version: 1.0
last_updated: 2026-10-24
project: ATS
owner: SE
category: agent
status: active
---
# WI-20261024-ATS-023 Handoff
[WI HEADER]
WI ID: WI-20261024-ATS-023
REQ: REQ-20261024-ATS-001
Agent: se
Depends On: -
Blocks: WI-20261024-ATS-025
[WI SUMMARY]
Why: Prepare exact existing-account role/grant change with reused tools
Scope: Own build/WI20261024-023-stage-two/ and own deliverables only. Inspect existing fixture/apply and stage-one tools. Implement minimal guarded local/native recipe for ID1 USER to ADMIN, preserve ADMIN2 USER3, existing grant1 expiry 2026-11-24 inclusive KST; preserve started_at, MONTHLY/CANCELLED and other data; truthful audit. Reuse SSH/DB lifecycle and evidence, prepare latest preflight, rollback/recovery and unknown-commit reconciliation. Focused local tests. No remote access or private file bodies. Identify exact MA commands and private reference paths without exposing secrets.
DoD: Precise evidence-backed output and reproducible focused checks; distinguish prepared from applied.
Constraints: Latest approved REQ and AGENTS stage2 approval persistence govern. No server/provider mutation, secret output, purchase, charge, new user, deletion, unrelated edits, commit/push.
[ACCEPTANCE CRITERIA]
Functional: Reuse existing actors/artifacts and verify exact intended effects and remaining prerequisites.
Performance: No repeated whole-trial reruns or premature human unlock.
Quality: Focused checks, truthful evidence and safe resumption; no unsupported current-state claims.
[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
Tier 2:
- AGENTS.md
- docs/index.md
- deliverables/user/REQ-20261024-ATS-001.md
- deliverables/user/REQ-20261011-ATS-007.md
- deliverables/agent/WI-20261024-ATS-019-evidence-pack.md
- build/WI20261024-001-fixture-apply/
- build/WI20261024-017-stage-one/
- docs/payment/feature-inventory.md
- src/main/resources/seed.sql
[OUTPUT CONTRACT]
User-facing: deliverables/user/WI-20261024-ATS-023-summary.md
Agent-facing: deliverables/agent/WI-20261024-ATS-023-evidence-pack.md
Use create-wi-evidence-pack skill. Provide concise progress and exact MA continuation commands.
[TRACEABILITY REQUIREMENTS]
Paths, checks, hashes and limitations required. Preserve historical evidence. Rollback limited to owned changed bytes; no blind reversal after unknown mutation.

