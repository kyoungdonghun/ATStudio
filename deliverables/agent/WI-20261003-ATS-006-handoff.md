---
version: 1.0
last_updated: 2026-10-03
project: ATS
owner: MA
category: handoff
status: complete
related_req: REQ-20261003-ATS-001
---
# WI-20261003-ATS-006 Handoff

[WI HEADER]
WI ID: WI-20261003-ATS-006
REQ: REQ-20261003-ATS-001
Agent: docops
Depends On: WI-20261003-ATS-005
Blocks: -

[WI SUMMARY]
Why: Implement the approved pre-purchase preparation, not a production deployment.
Scope (in/out):
- OWN ONLY docs/index.md, deliverables/user/REQ-20261003-ATS-001.md, and your evidence/summary.
- Add small current decision entry pointing to approved deployment REQ, deploy/single-vps/README.md and final QA/security evidence; preserve the existing 2026-09-29 dirty refresh verbatim except additive/current metadata.
- Update REQ A-stage WI progress with actual outcomes and exact remaining B gates. NEVER mark whole REQ complete or production GO after local preparation.
- State actual target/Linux nginx/systemd/MySQL/restore/alerts/load NOT RUN and new external approval inputs needed. Preserve Gmail/WAV/historical hashes.
- Validate your new links and metadata using validate-docs skill; no product/private/runtime modifications.
DoD:
- A-stage complete only if WI005 evidence supports it; otherwise unresolved findings visible.
- Minimum user next step list: actual plan/OS/domain/account target and purchase approval separately, unresolved billing terms, safe SSH/secret/DB action checks. No fabricated current pricing.
Constraints/Forbidden:
- Use apply_patch for edits. Preserve all other work and dirty files.
- No purchase, remote host operation, DB access/DDL/data changes, app/server restart, secret inspection, network email/payment calls, public tunnel load, package installation, commit or push.
- No src/ or frontend/ product changes under this WI.
- English operational docs and evidence; Korean user-facing brief allowed only as conversation, REQ Korean.
- Static/offline checks never prove actual Linux/service/capacity readiness.
- Existing Gmail and WAV/128kbps MP3/phone TEST evidence remains valid in its original bounds.

[ACCEPTANCE CRITERIA]
Functional:
- [x] Deliver scoped files with precise installation/validation and HOLD gates.
Performance:
- [x] No target capacity claim. Identify values as starting settings, not measured guarantees.
Quality:
- [x] Bounded checks run, paths recorded, no secrets or unrelated changes.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/index.md#current-decision-entry
- docs/design/runtime-storage-operations.md
- docs/SR/SR-93.md#remaining-production-gates
- .agents/skills/create-wi-evidence-pack/SKILL.md
- .agents/skills/validate-docs/SKILL.md
REQ:
- deliverables/user/REQ-20261003-ATS-001.md
Files:
- docs/index.md#current-decision-entry
- deliverables/user/REQ-20261003-ATS-001.md
- deploy/single-vps/README.md
- deliverables/agent/WI-20261003-ATS-005-evidence-pack.md
- .agents/skills/validate-docs/SKILL.md

[OUTPUT CONTRACT]
- User-facing: deliverables/user/WI-20261003-ATS-006-summary.md
- Agent-facing: deliverables/agent/WI-20261003-ATS-006-evidence-pack.md (use create-wi-evidence-pack)
- Include changed files, reproducible checks, risks, rollback by reverting ONLY owned additions, downstream WI.
- Do not edit other WI/REQ files.
- Return concise findings and paths; report template/file interface decisions promptly.

[TRACEABILITY REQUIREMENTS]
- Injected Tier 0/security content is supplied in the agent prompt in addition to pointers.
- Evidence date/environment/owner/result and unverified boundary must be separate.
