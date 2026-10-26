---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: docops
category: handoff
status: draft
---

# WI-20261011-ATS-003: Domain TLS Execution Receipts

[WI HEADER]
WI ID: WI-20261011-ATS-003
REQ: REQ-20261010-ATS-010 (approved / in progress)
Agent: docops
Depends On: WI-20261011-ATS-002 PASS and MA native execution/cleanup receipts
Blocks: -

[WI SUMMARY]
Why: Integrate fresh observations without confusing reviewed candidates with deployed services.
Scope (in): Current decision entry, REQ010 progress, and this WI's summary/evidence. Prepare initial receipt scaffold from MA facts, then reconcile final MA receipt before finishing.
Scope (out): No implementation, historical evidence rewrite, remote/browser/private files, services, cloud operations, new purchases, commits/pushes or descendants.
DoD: Dated owner/environment and exact local/native PASS/HOLD/NOT RUN boundaries; cleanup measured; no unsupported completion claims.
Constraints: Use apply_patch. Preserve unrelated dirty changes and old dated evidence. No IPs, personal contacts, credentials, account IDs, console token URLs or private configuration in tracked files.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Record WI001 local35/35 and WI002 independent35/35 PASS, not native success.
- [ ] Record domain purchased/registered as observed, separating user payment report from receipt audit.
- [ ] Append actual MA DNS/package/Nginx/firewall/certificate/renewal receipts when supplied.
- [ ] Explicit current state for app/DB, monitoring, boot behavior and SSH cleanup.
Performance:
- [ ] Only focused documentation edits; no repository-wide churn.
Quality:
- [ ] Validate changed document metadata, links, traceability and whitespace.
- [ ] Do not close REQ while issuance, native acceptance or cleanup remains pending.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md
Tier 1:
- .claude/agents/docops.md
- docs/policies/security-policy.md
Tier 2:
- .agents/skills/validate-docs/SKILL.md
- .agents/skills/create-wi-evidence-pack/SKILL.md
- docs/index.md
- deploy/single-vps/runbooks/domain-tls.md
Context:
- deliverables/user/REQ-20261010-ATS-010.md
- deliverables/agent/WI-20261011-ATS-001-evidence-pack.md
- deliverables/agent/WI-20261011-ATS-002-evidence-pack.md
- deliverables/agent/WI-20261010-ATS-032-evidence-pack.md
- MA safe operational receipts supplied in messages; no raw private logs.

[OUTPUT CONTRACT]
Allowed writes only:
- docs/index.md: concise new current decision checkpoint preserving historical entries.
- deliverables/user/REQ-20261010-ATS-010.md: measured progress and pending approvals.
- deliverables/user/WI-20261011-ATS-003-summary.md.
- deliverables/agent/WI-20261011-ATS-003-evidence-pack.md.
Handoff: this skill-generated file.

[TRACEABILITY REQUIREMENTS]
Evidence: exact measured date/environment/owner and source identities; distinguish local tests, simulation, installed package, effective service and external acceptance.
Tests: scoped docs validator and diff/whitespace checks.
Rollback: documentation only; preserve original historical evidence and no runtime authority.
Completion gate: MA final receipt REQUIRED. Before that, drafts may honestly show in progress; do not fabricate cleanup or close the REQ.
