---
version: 1.0
last_updated: 2026-08-17
project: ATS
owner: se
category: wi-handoff
status: approved-execution
related_req: REQ-20260817-ATS-007
dependencies:
  - path: ../user/REQ-20260817-ATS-007.md
    reason: Approved Inventory implementation scope
---

> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A030: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a030). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

[WI HEADER]
WI ID: WI-20260817-ATS-010
REQ: REQ-20260817-ATS-007
Agent: se
Depends On: REQ-20260817-ATS-007 approval
Blocks: WI-20260817-ATS-011

[WI SUMMARY]
Why: Add the approved, read-only Inventory action so a later separately assigned WI can perform one bounded disposable-schema orphan count.
Scope (in/out): Extend only the supported PowerShell wrapper, Java bootstrap, focused non-DB guards, and README where behavior changes; create this WI's user summary and evidence pack. Do not perform Inventory, connect to MySQL, use an external bundle, or change any application source.
DoD: Inventory preserves existing preconnection gates, performs only the fixed single information_schema count through an admin connection, emits only the bounded inventory result, and passes the requested no-connection verification.
Constraints/Forbidden: No real database connection; no application, browser, external-service, Git, commit, or Inventory invocation; no DDL/DML, schema/seed/patch application, manifest recording/change, selected-database connection, schema-name selection, or diagnostic/connection output. Existing Preflight, Observe, Create, Validate, and Drop semantics remain unchanged.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] The wrapper allowlist accepts Inventory and retains all existing actions.
- [ ] Inventory repeats action, loopback, disposable companion-name, and current 43-table source preflight checks before credentials, Connector/J, or a connection.
- [ ] Inventory uses only one admin/root JDBC connection and one fixed COUNT query scoped by `^ats_disposable_[0-9]{8}_[a-z0-9]{8}$`.
- [ ] Inventory outputs numeric `inventory.count` and the exact zero/positive state without schema names, connection values, or diagnostics.
- [ ] Inventory is not refused only because the current manifest expectation is unrecorded; Create and Validate remain refused in that state.
Performance:
- [ ] No new repeated query or connection path is introduced.
Quality:
- [ ] Focused guard suite passes without a database connection.
- [ ] Java source compiles and no-connection preflight checks pass.
- [ ] Documentation validation and scoped diff check pass.

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
- .agents/skills/react-best-practices/AGENTS.md (workspace-level injection only; no frontend implementation is in scope)
- docs/standards/frontend-standards.md (workspace-level injection only; no frontend implementation is in scope)
- docs/templates/eval-report-template.md
- docs/templates/requirements-request-template.md
- docs/templates/wi-subagent-handoff-template.md

REQ/Context Docs:
- deliverables/user/REQ-20260817-ATS-007.md
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
- powershell -ExecutionPolicy Bypass -File scripts/database/test-bootstrap-guards.ps1
- javac -d <temporary-output> scripts/database/DisposableMysqlBootstrap.java
- python .claude/scripts/validate_docs.py
- git diff --check -- <allowed WI files>

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-010-summary.md :
- Summary, verification, no-live-DB confirmation, risks, and approval points
Agent-facing -> deliverables/agent/WI-20260817-ATS-010-evidence-pack.md :
- Evidence pointers, patch notes, reproducible test results, rollback, and follow-up WI
Handoff Packet -> deliverables/agent/WI-20260817-ATS-010-handoff.md :
- This packet (for traceability)

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Include guard, Java compilation/no-connection, documentation validation, and scoped diff results
Rollback (if needed): Revert the four implementation/document files and the WI-010 deliverables; no database rollback is needed because this WI performs no live database action.
