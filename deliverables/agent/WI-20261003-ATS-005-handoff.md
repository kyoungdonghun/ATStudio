---
version: 1.0
last_updated: 2026-10-03
project: ATS
owner: MA
category: handoff
status: complete
related_req: REQ-20261003-ATS-001
---
# WI-20261003-ATS-005 Handoff

[WI HEADER]
WI ID: WI-20261003-ATS-005
REQ: REQ-20261003-ATS-001
Agent: qa
Depends On: WI-20261003-ATS-003, WI-20261003-ATS-004
Blocks: 006

[WI SUMMARY]
Why: Implement the approved pre-purchase preparation, not a production deployment.
Scope (in/out):
- OWN ONLY your evidence and summary. Independent verification of deploy/single-vps and scripts/deployment.
- Rerun offline validation CLI and focused unit tests independently; inspect redaction/path guards/no network or subprocess/env-sourcing behavior. Use existing Python, no installs.
- Parse JSON, compile-check Python without executing deployment, check diff whitespace and focused documentation links/metadata. Existing global validator may have historical warnings; classify rather than edit unrelated files.
- Confirm no src/frontend runtime modifications, existing dirty docs preserved, and actual Linux/server checks remain NOT RUN.
- Review security findings/fixes from WI004, fail completion for remaining actionable blockers; do not change implementation.
DoD:
- Publish exact commands/exits/test counts and template-only qualification.
- Bundle executable integrity and source-reference checks, no DB/live contact. Regression fixture output contains no raw unsafe supplied secret text.
Constraints/Forbidden:
- Use apply_patch for edits. Preserve all other work and dirty files.
- No purchase, remote host operation, DB access/DDL/data changes, app/server restart, secret inspection, network email/payment calls, public tunnel load, package installation, commit or push.
- No src/ or frontend/ product changes under this WI.
- English operational docs and evidence; Korean user-facing brief allowed only as conversation, REQ Korean.
- Static/offline checks never prove actual Linux/service/capacity readiness.
- Existing Gmail and WAV/128kbps MP3/phone TEST evidence remains valid in its original bounds.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Deliver scoped files with precise installation/validation and HOLD gates.
Performance:
- [ ] No target capacity claim. Identify values as starting settings, not measured guarantees.
Quality:
- [ ] Bounded checks run, paths recorded, no secrets or unrelated changes.

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
- scripts/deployment/
- deploy/single-vps/
- deliverables/agent/WI-20261003-ATS-003-evidence-pack.md
- deliverables/agent/WI-20261003-ATS-004-evidence-pack.md
- .agents/skills/test/SKILL.md
- .agents/skills/lint/SKILL.md
- .agents/skills/validate-docs/SKILL.md

[OUTPUT CONTRACT]
- User-facing: deliverables/user/WI-20261003-ATS-005-summary.md
- Agent-facing: deliverables/agent/WI-20261003-ATS-005-evidence-pack.md (use create-wi-evidence-pack)
- Include changed files, reproducible checks, risks, rollback by reverting ONLY owned additions, downstream WI.
- Do not edit other WI/REQ files.
- Return concise findings and paths; report template/file interface decisions promptly.

[TRACEABILITY REQUIREMENTS]
- Injected Tier 0/security content is supplied in the agent prompt in addition to pointers.
- Evidence date/environment/owner/result and unverified boundary must be separate.
