---
version: 1.0
last_updated: 2026-10-03
project: ATS
owner: MA
category: handoff
status: complete
related_req: REQ-20261003-ATS-001
---
# WI-20261003-ATS-002 Handoff

[WI HEADER]
WI ID: WI-20261003-ATS-002
REQ: REQ-20261003-ATS-001
Agent: docops
Depends On: -
Blocks: 004

[WI SUMMARY]
Why: Implement the approved pre-purchase preparation, not a production deployment.
Scope (in/out):
- OWN ONLY deploy/single-vps/README.md, deploy/single-vps/runbooks/ and your two deliverables.
- Write practical pre-purchase/target deployment runbook reusing existing runtime-storage/reconstruction evidence. Link CONFIGURATION.md supplied by WI001 and scripts/deployment/validate_bundle.py planned by WI003.
- Include first-server target inventory, exact credential/DB/SSH/remote-action approval boundaries, initial setup, fresh DB strategy (no retained TEST migration, no actual DDL), seeded demo exclusion/admin bootstrap, FFmpeg check, TLS/renewal, reboot/rollback, canonical origins/mail/callbacks.
- Recovery runbook: consistent DB + all public/private storage + exact release/config/key inventory; single application writer and all workers stopped during cold consistent snapshot procedure, not mysqldump-only claimed atomic. Backup encryption/key separation, operator-approved retention/size budget and deletion rules, off-provider copy, daily and prechange proposal; no backup scripts pretending to be implemented.
- Isolated restore egress default-deny, no imported LIVE/mail credentials, pending schedules isolated. DB restoration never against retained/prod until exact approval.
- Monitoring/capacity acceptance runbook: CPU/RAM/disk/inodes/interface egress versus Nginx bytes (provider billing differs), rate limits not budget cap, alert ownership/delivery to test later. 20->50 mixed active users and optional100 as validation scenarios not forecasts; bounded duration/bytes; WAV upload/encode simultaneous play/download; p95/error/memory/queue/resource stop thresholds as proposed gates; immediate shortfall report to user no silent paid upgrade.
- Preserve unresolved provider allowance/midmonth/inbound50Mbps/attackbilling/auto-block terms without new pricing claims or contact. Minimal public readiness checklist with not-run status.
DoD:
- No claims backup automation/restore/alerts/capacity proven. Distinguish ready docs from actual target acceptance.
- State mail Gmail success bounded TEST preserved and selected recipients/newhost external send needs confirmation.
- Frontend built assets are public, source/private docs and secret material not copied into webroot.
- Complete operator handoff list with decisions needed NOW versus on-target; do not turn every historical check into a new blocker.
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
- scripts/reconstruction/README.md
- docs/design/runtime-storage-operations.md
- docs/design/db-schema.md
- deliverables/agent/WI-20260909-ATS-028-evidence-pack.md
- deliverables/user/REQ-20260909-ATS-004.md
- docs/payment/known-limits-and-next-steps.md

[OUTPUT CONTRACT]
- User-facing: deliverables/user/WI-20261003-ATS-002-summary.md
- Agent-facing: deliverables/agent/WI-20261003-ATS-002-evidence-pack.md (use create-wi-evidence-pack)
- Include changed files, reproducible checks, risks, rollback by reverting ONLY owned additions, downstream WI.
- Do not edit other WI/REQ files.
- Return concise findings and paths; report template/file interface decisions promptly.

[TRACEABILITY REQUIREMENTS]
- Injected Tier 0/security content is supplied in the agent prompt in addition to pointers.
- Evidence date/environment/owner/result and unverified boundary must be separate.
