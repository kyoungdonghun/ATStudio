---
version: 1.1
last_updated: 2026-10-26
project: ATS
owner: MA
category: reference
status: stable
dependencies:
  - path: ../user/REQ-20261026-ATS-001.md
    reason: Explicit local preservation and commit authorization
---
# WI-20261026-ATS-007
[WI HEADER]
REQ: REQ-20261026-ATS-001
Agent: eo
Depends On: 004,005,006
Blocks: -

[WI SUMMARY]
After dependencies complete: independently crosscheck PG scan and reconstructed source archive, Java evidence, exact stage allowlist and hooks. Integrate docs/index and REQ checkpoints minimally preserving history; scan own new docs. Prepare coherent local commits for validated source, reusable operating tooling, safe documentation/evidence and final retention report. Exact path staging only with per-commit cached diff review/secret scan and whitespace validation; preserve staged baseline (currently empty), never add ignored originals or blanket add. Inspect hooks before commit; no bypass automatic review, no push/network/server actions, no amend/reset/clean. May delegate actual Git execution back to MA per policy; don't concurrent mutate index. Final report commit hashes/paths/counts, intentionally untracked/private leftovers and local origin/main stale observation, no misleading full backup or clean-worktree claim. Every frozen original remains unchanged. Maintain safe retained archive rather than needlessly asking user for each ordinary exclusion. Finish all authorized safe commits even if some items must remain private.

[ACCEPTANCE CRITERIA]
Exact selected paths and source identities; no sensitive bodies in output; preserve historical evidence; explicit limits; focused checks and two deliverable sets. User-approved scope supersedes earlier no-local-commit gates only. No need to ask again for safe in-scope recovery. No runtime, provider, new expense, destructive cleanup or push.

[INPUT POINTERS]
Tier0 MA injection: core-principles evidence-led sustainable/scoped work; latest AGENTS approve-once; development-standards minimal reproducible changes; documentation-standards metadata/dependencies/Korean user-facing and English internal; glossary canonical REQ/WI. Paths: docs/standards/core-principles.md; development-standards.md; documentation-standards.md; glossary.md.
Tier1: AGENTS.md; docs/policies/security-policy.md (no plaintext secrets/PII in repository, inspect before commit); quality-gates.md; versioning-policy.md.
Tier2: approved REQ; docs/index.md current checkpoint; scripts/reconstruction/README.md; WI20261025-006/008 evidence; WI20261026-001/002/003 results.
Baseline: main 91a16e72693d7cd6ed04e0d36a5bd160af96f6d8, 12 tracked modifications, 674 untracked files excluding ignored. No staged files. Local origin/main comparison ahead3, remote not queried.
Memory is navigation only; current files/evidence govern.

[OUTPUT CONTRACT]
safe integrated retention report, final EP/summary/current entry; exact committed allowlists; local Git commits; no further WI
Apply .agents/skills/create-wi-evidence-pack/SKILL.md to completed work. Handoff was created with create-wi-handoff-packet skill, approved REQ exists.
[TRACEABILITY]
Changed file allowlist, validation commands/results, original preservation/hash evidence, risks, rollback by new scoped correction not destructive reset, dependent WI trigger.


[COMPLETION CHECKPOINT — 2026-10-26]
Completed within approved local scope. See [final integration evidence](WI-20261026-ATS-007-evidence-pack.md) for actual commits, exact gates and retained limits. No dependent WI remains in this REQ; final checkpoint documents accompany the closure commit. Stage two remains open.
