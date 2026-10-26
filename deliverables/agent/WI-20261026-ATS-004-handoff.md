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
# WI-20261026-ATS-004
[WI HEADER]
REQ: REQ-20261026-ATS-001
Agent: pg
Depends On: -
Blocks: 007

[WI SUMMARY]
Classify and scan all accumulated visible Git changes without printing sensitive matches. Inspect current tracked diffs/untracked contents by bounded local scanner (including binary document metadata/text where needed); preserve original evidence. Produce exact repository-relative allowlist, exclusions with reason category (not sensitive values), counts/hash pins and suggested coherent commit groups. Files with personal identifiers, internal access endpoints, raw tokens, private evidence or uncertain binary contents must remain unstaged or have a safe derivative preserving originals. No blanket add, no stage/commit, no changes to historical frozen artifacts. Can add own public scanning/report scripts and safe reports in deliverables/agent or scripts/reconstruction only in uniquely named WI004 files; raw reports if needed under ignored build/WI-20261026-ATS-004 and never echo values. Coordinate WI005 new candidate scanning with it; initial corpus snapshot manifest must make later drift detectable. Identify changed docs safe to sanitize vs immutable records; ask MA only material decision, not routine exclusions.

[ACCEPTANCE CRITERIA]
Exact selected paths and source identities; no sensitive bodies in output; preserve historical evidence; explicit limits; focused checks and two deliverable sets. User-approved scope supersedes earlier no-local-commit gates only. No need to ask again for safe in-scope recovery. No runtime, provider, new expense, destructive cleanup or push.

[INPUT POINTERS]
Tier0 MA injection: core-principles evidence-led sustainable/scoped work; latest AGENTS approve-once; development-standards minimal reproducible changes; documentation-standards metadata/dependencies/Korean user-facing and English internal; glossary canonical REQ/WI. Paths: docs/standards/core-principles.md; development-standards.md; documentation-standards.md; glossary.md.
Tier1: AGENTS.md; docs/policies/security-policy.md (no plaintext secrets/PII in repository, inspect before commit); quality-gates.md; versioning-policy.md.
Tier2: approved REQ; docs/index.md current checkpoint; scripts/reconstruction/README.md; WI20261025-006/008 evidence; WI20261026-001/002/003 results.
Baseline: main 91a16e72693d7cd6ed04e0d36a5bd160af96f6d8, 12 tracked modifications, 674 untracked files excluding ignored. No staged files. Local origin/main comparison ahead3, remote not queried.
Memory is navigation only; current files/evidence govern.

[OUTPUT CONTRACT]
build/WI-20261026-ATS-004/ initial inventory/scans; deliverables/user/WI-20261026-ATS-004-summary.md; deliverables/agent/WI-20261026-ATS-004-evidence-pack.md plus public exact allow/exclude manifests where safe
Apply .agents/skills/create-wi-evidence-pack/SKILL.md to completed work. Handoff was created with create-wi-handoff-packet skill, approved REQ exists.
[TRACEABILITY]
Changed file allowlist, validation commands/results, original preservation/hash evidence, risks, rollback by new scoped correction not destructive reset, dependent WI trigger.


[COMPLETION CHECKPOINT — 2026-10-26]
Completed within approved local scope. See [final integration evidence](WI-20261026-ATS-007-evidence-pack.md) for actual commits, exact gates and retained limits. Dependency WI007 was triggered and its preservation commits completed.
