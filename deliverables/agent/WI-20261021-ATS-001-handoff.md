---
version: 1.0
last_updated: 2026-10-21
project: ATS
owner: MA
category: handoff
status: active
---
# WI-20261021-ATS-001 Handoff

[WI HEADER]
WI ID: WI-20261021-ATS-001
REQ: REQ-20261021-ATS-001
Agent: se
Depends On: approved request
Blocks: reviewed local commits

[WI SUMMARY]
Classify current Git changes and confirm current source/artifact identity without changing application, tools or runtime. Produce a concise purpose-based commit candidate list and explicit retained exclusions. No staging, commit, push, deletion, rebuild, remote access or broad refactor.

[ACCEPTANCE CRITERIA]
- Inventory tracked modifications and untracked paths without printing secret bodies.
- Compare all643 backend inputs and JAR SHA against WI068, frontend336 recorded inputs and143 outputs (four historical test-only exclusions explicit).
- Group application changes, reusable deployment files, operational historical records, generated/private assets, unrelated changes. Do not treat all untracked files as safe.
- Propose exact candidate paths, preserving all other files. No new generic framework.

[INPUT POINTERS]
Tier0: docs/standards/core-principles.md; docs/standards/development-standards.md; docs/standards/documentation-standards.md; docs/standards/glossary.md.
Tier1: docs/policies/security-policy.md.
Context: deliverables/user/REQ-20261021-ATS-001.md; docs/index.md; build/WI068-release-run-02/artifact.json; build/WI068-release-run-02/source-before.json; build/WI067-pc-delivery-v7/public-manifest.json.

[OUTPUT CONTRACT]
Only create deliverables/agent/WI-20261021-ATS-001-evidence-pack.md, deliverables/user/WI-20261021-ATS-001-summary.md and a path-only candidate list at deliverables/agent/WI-20261021-ATS-001-candidates.json. Use apply_patch. No unrelated file edits.

[TRACEABILITY REQUIREMENTS]
Report measured counts, checks, safe exact paths, retained reasons and WI002 review dependency. Use create-wi-evidence-pack format. No sensitive values or misleading clean-worktree claim.
