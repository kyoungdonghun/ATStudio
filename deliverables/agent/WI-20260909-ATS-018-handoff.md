---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: docops
category: reference
status: active
dependencies:
  - path: ../user/REQ-20260909-ATS-004.md
    reason: Approved portability preservation scope
---

# WI-20260909-ATS-018 Handoff

[WI HEADER]
REQ: REQ-20260909-ATS-004
Agent: docops
Depends On: none
Blocks: WI-20260909-ATS-020

[WI SUMMARY]
Why: Mechanical link closure retained only 13 of 96 historical documents; semantic decisions must survive machine loss too.
Scope: Review all 96 archived development documents (83 currently local-only), preserve useful safe historical records, and map decisions to current SoT. Account for all 205 originals; keep raw 109 outputs/media/SQL private, do not execute them.
DoD: All documents content-reviewed, private values excluded, preserved records discoverable from Git; original and sanitized derivative identities clearly separated. Fix stale register handoff-ready status. Historical facts must remain dated, not become current claims.
Forbidden: Product/config/runtime/DB/media changes, deleting originals, uploading raw evidence or secrets, commits/pushes. Do not make production-readiness claims.

[ACCEPTANCE CRITERIA]
- [ ] Original archive hashes and per-file preservation decisions match; no unseen document is called reviewed.
- [ ] Unique decisions/work rules are preserved or explicitly mapped to current docs.
- [ ] Sensitive original content remains private; sanitized copies have clear provenance.
- [ ] Markdown references and scope are checked; raw local-only pointers are honestly labeled.
- Performance: bounded output; no heavy concurrent builds.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md, docs/standards/development-standards.md, docs/standards/documentation-standards.md, docs/standards/glossary.md.
Tier 1: docs/policies/archive-policy.md, docs/policies/security-policy.md.
Tier 2: docs/index.md, docs/registry/v1-artifact-retention-20260909.md, docs/design/runtime-storage-operations.md.
Context: deliverables/user/REQ-20260909-ATS-004.md; prior WI016/WI017 evidence.
Private input: %LOCALAPPDATA%/ATStudio/archives/v1-artifacts-20260909-084405/manifest.json, workspace-artifacts.zip, restore-check/, disposition.json. Original set 205; document subset 96.

[OUTPUT CONTRACT]
Owned edits: missing original historical Markdown paths under deliverables/ after privacy review; docs/registry/v1-artifact-retention-20260909.md; new docs/registry/development-history-recovery-20260909.md; registry/index.md; archive-policy.md only if required; own WI018 evidence and user summary.
Other agent owns README/reconstruction scripts and docs/index.md; coordinate links without editing those files.
Use create-wi-evidence-pack skill. Report each restored/sanitized set and original hash mapping; actual review scope, tests, limitations, rollback (Git preserves changes; originals remain untouched), and next WI020.
