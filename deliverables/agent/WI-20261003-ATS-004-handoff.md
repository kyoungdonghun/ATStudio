---
version: 1.0
last_updated: 2026-10-03
project: ATS
owner: MA
category: handoff
status: complete
related_req: REQ-20261003-ATS-001
---
# WI-20261003-ATS-004 Handoff

[WI HEADER]
WI ID: WI-20261003-ATS-004
REQ: REQ-20261003-ATS-001
Agent: pg
Depends On: WI-20261003-ATS-001, WI-20261003-ATS-002
Blocks: 005

[WI SUMMARY]
Why: Implement the approved pre-purchase preparation, not a production deployment.
Scope (in/out):
- OWN ONLY your summary/evidence files. Independent review; do not edit implementation files.
- Review WI001 deployment templates and WI002 runbooks against actual source, focusing on protect-original/private storage, proxy trust and forwarding, fail-closed restricted access, redirect/location/header inheritance, log sensitive query leakage, security headers, external secret handling, service privilege/write boundaries, template deploy blockers, billing/mail isolation and safe restoration.
- Check default values cannot silently enable provider requests or test users, stale bootstrap assumptions, wrong env names, scheduler claims, and scripts posing as implemented automation.
- Report actionable findings with severity/exact file+line and reproduction, separate target-only validation limitations. Send discovered P1/P2 blockers promptly to MA. No provider test/network/credential inspection or product changes.
DoD:
- Review based on actual current files; no broad theoretical vulnerability dump. State template-only PASS or findings, never production approval.
- Re-review fixes on same WI before finalizing; record initial and resolved issues.
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
- deploy/single-vps/
- src/main/java/com/atstudio/atstudio/config/SecurityConfig.java
- src/main/java/com/atstudio/atstudio/security/TrustedClientIdentityResolver.java
- src/main/java/com/atstudio/atstudio/config/AcceptanceStartupGuard.java
- src/main/resources/application.yml
- docs/design/runtime-storage-operations.md

[OUTPUT CONTRACT]
- User-facing: deliverables/user/WI-20261003-ATS-004-summary.md
- Agent-facing: deliverables/agent/WI-20261003-ATS-004-evidence-pack.md (use create-wi-evidence-pack)
- Include changed files, reproducible checks, risks, rollback by reverting ONLY owned additions, downstream WI.
- Do not edit other WI/REQ files.
- Return concise findings and paths; report template/file interface decisions promptly.

[TRACEABILITY REQUIREMENTS]
- Injected Tier 0/security content is supplied in the agent prompt in addition to pointers.
- Evidence date/environment/owner/result and unverified boundary must be separate.
