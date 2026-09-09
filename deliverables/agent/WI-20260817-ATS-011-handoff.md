---
version: 1.0
last_updated: 2026-08-17
project: ATS
owner: qa-integ
category: wi-handoff
status: approved-execution
related_req: REQ-20260817-ATS-007
dependencies:
  - path: ../user/REQ-20260817-ATS-007.md
    reason: Approved one-time read-only Inventory scope
  - path: WI-20260817-ATS-010-evidence-pack.md
    reason: Required implementation and non-DB guard evidence
---

> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A032: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a032). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

[WI HEADER]
WI ID: WI-20260817-ATS-011
REQ: REQ-20260817-ATS-007
Agent: qa-integ
Depends On: WI-20260817-ATS-010 Evidence Pack and guard-test PASS
Blocks: -

[WI SUMMARY]
Why: Produce the one approved, read-only runtime Inventory result through the supported wrapper without exposing environment or connection information.
Scope (in/out): Invoke only `scripts/database/bootstrap-disposable-mysql.ps1` once with `Action Inventory`, one fresh guard-valid companion name, and the opaque private environment bundle. Create this WI's summary and evidence pack; update the README only if runtime evidence proves its current claim false. Do not perform any cleanup or other database operation.
DoD: The one wrapper stream is held only in process memory, reduced to known safe fields, and accepted only when all required Inventory gates pass. The final deliverables retain only the sanitized count/state result, action counts, and safe terminal status.
Constraints/Forbidden: Do not inspect, enumerate, print, persist, or identify the opaque bundle, its location, credentials, connection data, schema names, raw output, raw SQL, rows, or diagnostics. Do not invoke Preflight, Observe, Create, Validate, or Drop separately; do not run another DB query, application/browser/external action, Git action, source/config change, schema/seed/patch, manifest update, cleanup, deletion, retry, or investigation. The companion name is guard-only, is not an Inventory target, and must not be printed or persisted.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Exactly one fresh guard-valid companion name is generated only in process memory.
- [ ] The supported wrapper is invoked exactly once with `Inventory` and only the opaque private bundle input.
- [ ] Stream validation confirms action `inventory`, loopback class, disposable-name guard, source create-table count `43` with PASS, manifest expectation `UNRECORDED`, exactly one nonnegative integer inventory count, its matching zero/positive state, and final PASS status.
- [ ] No raw output or prohibited connection, bundle, schema-name, target, credential, host, port, URL, environment-value, SQL, row, or diagnostic data is retained.
- [ ] A zero count records `NO_POSSIBLE_ORPHAN`; a positive count records only the sanitized count and `POSSIBLE_ORPHAN_EXISTS`, then stops without cleanup.
Performance:
- [ ] One runtime wrapper invocation only; no retry or secondary query.
Quality:
- [ ] The WI summary and evidence pack state runtime action count `1` and every prohibited action count `0`.
- [ ] Documentation validation passes after the two deliverables are created.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
- docs/standards/evidence-pack-standard.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md
- docs/architecture/system-design.md

Tier 2 (Tech Stack - Conditional on project tech_stack from workspace.json):
- .agents/skills/react-best-practices/AGENTS.md (workspace-level injection only; no frontend work is in scope)
- docs/standards/frontend-standards.md (workspace-level injection only; no frontend work is in scope)
- docs/templates/eval-report-template.md
- docs/templates/requirements-request-template.md
- docs/templates/wi-subagent-handoff-template.md

REQ/Context Docs:
- deliverables/user/REQ-20260817-ATS-007.md
- deliverables/agent/WI-20260817-ATS-010-handoff.md
- deliverables/user/WI-20260817-ATS-010-summary.md
- deliverables/agent/WI-20260817-ATS-010-evidence-pack.md
- .agents/skills/create-wi-handoff-packet/SKILL.md
- .agents/skills/create-wi-evidence-pack/SKILL.md
- .claude/config/workspace.json
- .claude/config/context-injection-rules.json

Files:
- scripts/database/bootstrap-disposable-mysql.ps1
- scripts/database/DisposableMysqlBootstrap.java
- scripts/database/test-bootstrap-guards.ps1
- scripts/database/README.md

Repro/Logs:
- One supported-wrapper Inventory invocation with output held only in memory and reduced before reporting
- python .agents/skills/validate-docs/scripts/validate_docs.py

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-011-summary.md :
- Sanitized result, exact action counts, no-cleanup position, and separate-approval boundary for a positive count
Agent-facing -> deliverables/agent/WI-20260817-ATS-011-evidence-pack.md :
- Safe gate evidence, injected-context pointers, command class, action counts, documentation-validation result, and no-op rollback
Handoff Packet -> deliverables/agent/WI-20260817-ATS-011-handoff.md :
- This packet (for traceability)

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Record only safe paths and a sanitized invocation class; never record the private bundle identifier/location or raw stream.
Tests: Validate the final WI documents with the project documentation validator only after the one runtime invocation.
Rollback (if needed): No runtime mutation occurred. Revert only the WI-011 deliverables through approved follow-up work; no database rollback or cleanup is authorized.
