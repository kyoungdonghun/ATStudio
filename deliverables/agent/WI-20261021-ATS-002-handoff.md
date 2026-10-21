---
version: 1.0
last_updated: 2026-10-21
project: ATS
owner: MA
category: handoff
status: active
---
# WI-20261021-ATS-002 Handoff

[WI HEADER]
WI ID: WI-20261021-ATS-002
REQ: REQ-20261021-ATS-001
Agent: pg
Depends On: approved request; final WI001 candidates
Blocks: local commits

[WI SUMMARY]
Review uncommitted application/deployment/document candidate files for secrets, PII and operational identities before local commits. Inspect candidates locally without disclosing matched values. No remote access, app execution, network submission, staging, commit, push, secret hashing, historical rewrites or security weakening.

[ACCEPTANCE CRITERIA]
- Give an exact allowed/blocked path list and concise reasons with path/line only for sensitive findings.
- Start with18 source/test changes; independently classify deployment assets and historical operational docs conservatively. Exclude private/generated evidence instead of rewriting registered history.
- Review new WI001/003 reports when available; harmless variable names and synthetic fixtures are not secrets, but actual operational endpoints/private user paths require deliberate exclusion.
- Validate source identity independently and clearly separate security review from functionality acceptance.

[INPUT POINTERS]
Tier0: docs/standards/core-principles.md; docs/standards/development-standards.md; docs/standards/documentation-standards.md; docs/standards/glossary.md.
Tier1: docs/policies/security-policy.md.
Context: deliverables/user/REQ-20261021-ATS-001.md; WI001 candidate list; current Git diff; build/WI068-release-run-02/source-before.json.

[OUTPUT CONTRACT]
Only create deliverables/agent/WI-20261021-ATS-002-evidence-pack.md and deliverables/user/WI-20261021-ATS-002-summary.md. Use apply_patch. Provide exact commit allowance through these reports, never matched credential values.

[TRACEABILITY REQUIREMENTS]
Use create-wi-evidence-pack. No blanket promise of no secrets; state scan coverage/limits. Preserve all existing files.
