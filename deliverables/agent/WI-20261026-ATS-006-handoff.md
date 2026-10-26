---
version: 1.1
last_updated: 2026-10-26
project: ATS
owner: MA
category: reference
status: active
dependencies:
  - path: ../user/REQ-20261026-ATS-001.md
    reason: Explicit local preservation and commit authorization
---
# WI-20261026-ATS-006
[WI HEADER]
REQ: REQ-20261026-ATS-001
Agent: qa
Depends On: -
Blocks: 007

[WI SUMMARY]
Inspect five existing changed Java/config/test files and two new NCP mail files. Reuse WI20261025-008/006 source pins and focused test evidence when exact identity matches. Determine which earlier tests changed EmailService compatibility and token tests actually require focused verification. Read relevant test/build skills before executing. Use offline local Gradle with isolated output if necessary and existing dependencies; no installations/network/download, real email/server/DB/provider starts. Tests may use in-memory test fixtures only. Do not run whole suite absent reason or edit product behavior. Report precise allowlist of source files, diff assessment, tested identities/evidence boundaries and minimal required correction if any. Do not stage/commit. Own reports only; if harmless test/document correction needed discuss MA scope first, no new feature implementation.

[ACCEPTANCE CRITERIA]
Exact selected paths and source identities; no sensitive bodies in output; preserve historical evidence; explicit limits; focused checks and two deliverable sets. User-approved scope supersedes earlier no-local-commit gates only. No need to ask again for safe in-scope recovery. No runtime, provider, new expense, destructive cleanup or push.

[INPUT POINTERS]
Tier0 MA injection: core-principles evidence-led sustainable/scoped work; latest AGENTS approve-once; development-standards minimal reproducible changes; documentation-standards metadata/dependencies/Korean user-facing and English internal; glossary canonical REQ/WI. Paths: docs/standards/core-principles.md; development-standards.md; documentation-standards.md; glossary.md.
Tier1: AGENTS.md; docs/policies/security-policy.md (no plaintext secrets/PII in repository, inspect before commit); quality-gates.md; versioning-policy.md.
Tier2: approved REQ; docs/index.md current checkpoint; scripts/reconstruction/README.md; WI20261025-006/008 evidence; WI20261026-001/002/003 results.
Baseline: main 91a16e72693d7cd6ed04e0d36a5bd160af96f6d8, 12 tracked modifications, 674 untracked files excluding ignored. No staged files. Local origin/main comparison ahead3, remote not queried.
Memory is navigation only; current files/evidence govern.

[OUTPUT CONTRACT]
build/WI-20261026-ATS-006 validation receipts; deliverables/user/WI-20261026-ATS-006-summary.md; deliverables/agent/WI-20261026-ATS-006-evidence-pack.md
Apply .agents/skills/create-wi-evidence-pack/SKILL.md to completed work. Handoff was created with create-wi-handoff-packet skill, approved REQ exists.
[TRACEABILITY]
Changed file allowlist, validation commands/results, original preservation/hash evidence, risks, rollback by new scoped correction not destructive reset, dependent WI trigger.


[COMPLETION CHECKPOINT — 2026-10-26]
Completed within approved local scope. See [final integration evidence](WI-20261026-ATS-007-evidence-pack.md) for actual commits, exact gates and retained limits. Dependency WI007 was triggered and its preservation commits completed.
