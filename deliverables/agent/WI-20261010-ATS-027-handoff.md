---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: MA
category: guide
status: active
dependencies:
  - path: ../user/REQ-20261010-ATS-008.md
    reason: Approved scope and sensitive-operation gates
---
# Production Bootstrap Independent Security Review
[WI HEADER]
WI ID: WI-20261010-ATS-027
REQ: REQ-20261010-ATS-008
Agent: pg
Depends On: WI025 and WI026 (final gate; preliminary review overlaps)
Blocks: WI028
Session: current

[WI SUMMARY]
Why: Prepare the approved existing target without exposing secrets or conflating local checks with operation.
Scope: Review WI025 current candidate + final Evidence Pack, WI026 artifacts and exact approved MA native execution sequence. Draft review may overlap ongoing tests/build; final disposition MUST await both completed inputs and exact final file hashes. Audit fresh-only collisions, all native MySQL8.0.46 SQL/options/privilege views, secret/log/binlog exposure, root-only env, safe environment serialization, no partial retry, least privilege, target/machine/UUID pin, stopped services and stop/remask cleanup. Review proposed transfer/staging/approval-receipt mechanics before MA apply. Do not execute remote/SQL or generate secrets. Report blocking defects immediately to MA; no implementation edits. Re-run bounded offline helper tests as useful; local mocks are not MySQL proof. Clearly state whether preflight/apply may proceed under exact user approvals, separate runtime account authentication and Hibernate validation limits.
Write set: deliverables/user/WI-20261010-ATS-027-summary.md and deliverables/agent/WI-20261010-ATS-027-evidence-pack.md only
Constraints: No remote commands, provider actions, credentials/secret reads or generation, SQL execution, app startup, local retained DB mutation, package installation, arbitrary deletion, new dependencies, commit/push. Preserve unrelated dirty files.
Implementation must use apply_patch. Do not delegate descendants.

[ACCEPTANCE CRITERIA]
- [ ] Named approved scope only, fresh-only fail-closed behavior with bounded evidence.
- [ ] No secret-bearing arguments/stdout/stderr/logs, raw endpoints/tokens or private configuration in reports.
- [ ] Reproducible checks; report failures accurately; no claim of production capacity or server execution.
- [ ] Two deliverables and explicit next WI.
Performance: No capacity claim; preserve 2vCPU/8GB/100GB baseline and bounded resource assumptions.

[INPUT POINTERS]
Tier 0 (injected in full):
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md
Tier 1:
- .claude/agents/pg.md
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md
Tier 2:
- .agents/skills/create-wi-evidence-pack/SKILL.md
- .agents/skills/test/SKILL.md
- .agents/skills/build-check/SKILL.md
- .agents/skills/react-best-practices/AGENTS.md (conditional tech-stack pointer; no frontend code change here)
- docs/standards/frontend-standards.md (same bound)
- docs/standards/evidence-pack-standard.md
- docs/templates/eval-report-template.md
- docs/index.md#current-decision-entry
- deploy/single-vps/CONFIGURATION.md
- deploy/single-vps/runbooks/deployment.md
- scripts/reconstruction/README.md
- docs/design/db-schema.md
Context:
- deliverables/user/REQ-20261010-ATS-008.md
- deliverables/agent/WI-20261010-ATS-024-evidence-pack.md
- deliverables/agent/WI-20261010-ATS-014-evidence-pack.md
Source:
- src/main/resources/schema.sql
- src/main/resources/seed.sql
- src/main/resources/application.yml
- deploy/single-vps/env/atstudio.env.example
- deploy/single-vps/env/contract.json
- deploy/single-vps/systemd/atstudio.service
- scripts/deployment/validate_bundle.py
- build.gradle
- scripts/deployment/production-bootstrap.py
- scripts/deployment/test_production_bootstrap.py
- deploy/single-vps/runbooks/production-bootstrap.md
- deliverables/agent/WI-20261010-ATS-025-evidence-pack.md (completion pending)
- deliverables/agent/WI-20261010-ATS-026-evidence-pack.md (completion pending)

[OUTPUT CONTRACT]
User-facing: deliverables/user/WI-20261010-ATS-027-summary.md
Agent-facing: deliverables/agent/WI-20261010-ATS-027-evidence-pack.md
Use create-wi-evidence-pack skill. Record pointers, tool versions, hashes, exact check commands/results, risks, non-destructive rollback and next WI. English docs; concise user summary.

[TRACEABILITY REQUIREMENTS]
Second native compatibility review (2026-10-10, skill-generated reopened WI027): user explicitly approved the exact two built-in cache-cleaner classification correction. Review WI025's final candidate after its completion, requiring exact names plus SQL NULL library, denial of every other active AUDIT plugin and active mysqlx/group_replication, preserved independent guards, positive/negative/error/ambiguous regressions and new bundle identity. Preserve prior results; update only this WI's evidence/summary. Review the same bounded MA archive/enrollment/preflight/single-apply/cleanup procedure using fresh real `/root/wi025-v3` and matching new archive/upload names instead of v2; no old-stage overwrite or server setting changes. Approval record is created only after full native guards and exact local/native plan identity match. Native application remains MA-only, after your final PASS. WI028 immediately consumes the review and actual native/cleanup receipts.
Generated by create-wi-handoff-packet after approved REQ and maximum WI026 checked.
ATS tag from workspace.json; tier ordering and role/security/testing requirements from context-injection-rules.json. Meta-only architecture inference excluded from product bootstrap scope. Do not modify handoff.
