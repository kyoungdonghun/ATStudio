---
version: 1.0
last_updated: 2026-08-17
project: ATS
owner: qa-integ
category: wi-handoff
status: approved-execution
related_req: REQ-20260817-ATS-008
dependencies:
  - path: ../user/REQ-20260817-ATS-008.md
    reason: Approved corrected one-time read-only Inventory scope
  - path: ../user/WI-20260817-ATS-011-summary.md
    reason: Historical one-group gate result to preserve without modification
  - path: WI-20260817-ATS-011-evidence-pack.md
    reason: Historical runtime evidence and no-retry boundary
---

> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A034: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a034). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

[WI HEADER]
WI ID: WI-20260817-ATS-012
REQ: REQ-20260817-ATS-008
Agent: qa-integ
Depends On: Approved REQ-20260817-ATS-008 and historical WI-011 evidence review
Blocks: -

[WI SUMMARY]
Why: Correct the in-memory safe-output gate to accept the supported wrapper's ordered internal Preflight group followed by its one Inventory group, then collect exactly one sanitized read-only Inventory result.
Scope (in/out): Generate one fresh guard-valid companion name only in process memory; invoke the supported wrapper exactly once with Inventory; parse the child stream only in memory; create this WI's summary and Evidence Pack; validate those deliverables. Do not modify code, scripts, README, policies, historical WI-011 deliverables, or any other documentation.
DoD: Accept only exactly two complete ordered safe groups: internal preflight first and Inventory second. Retain only the sanitized count/state, one Inventory wrapper action, one internal preflight, and zero counts for prohibited actions. On any contract deviation, retain only INVENTORY_EVIDENCE_GATE_BLOCKED and stop without retry.
Constraints/Forbidden: Do not inspect, enumerate, print, persist, or identify the opaque bundle, its location, credentials, connection data, schema names, companion name, source hashes, raw output, raw SQL, rows, diagnostics, or errors. Do not run separate Preflight, Observe, Create, Validate, Drop, a retry, another query, cleanup, deletion, DDL, DML, schema, seed, patch, manifest, application, browser, external-service, or Git action. The companion name is a guard-only input and never an Inventory target.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Exactly one fresh guard-valid companion name is generated only in process memory and is discarded without output or persistence.
- [ ] The supported wrapper is invoked exactly once with Inventory; its internal preflight is accepted only as ordered group 1, never as a separate action.
- [ ] Group 1 contains only action=preflight; database-pattern PASS; loopback host class; schema.sql->seed.sql order; source count 43 and PASS check; manifest expectation UNRECORDED; format-valid normalized source hashes; and terminal PASS status.
- [ ] Group 2 contains the same common safe fields in the same order with action=inventory, then exactly one nonnegative inventory.count, exactly one matching inventory.state, and terminal PASS status.
- [ ] No unknown, duplicate, malformed, out-of-order, sensitive, or extra field is accepted. The downstream group 2 action proves the preflight requested Inventory without retaining a separate requested-action field.
- [ ] A zero count retains only count 0 and NO_POSSIBLE_ORPHAN; a positive count retains only its sanitized count and POSSIBLE_ORPHAN_EXISTS. Both paths stop immediately without cleanup.
Performance:
- [ ] One runtime wrapper Inventory invocation only; no retry, secondary query, or post-result DB action.
Quality:
- [ ] User summary and Evidence Pack retain no hashes, names, endpoints, output, rows, errors, bundle details, or environment data.
- [ ] User summary and Evidence Pack record wrapper Inventory action count 1, internal preflight count 1, and all prohibited action counts 0.
- [ ] Documentation validation passes after all three WI-012 deliverables are created.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee and task type):
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
- docs/standards/evidence-pack-standard.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md
- docs/architecture/system-design.md

Tier 2 (Tech Stack and workflow injection):
- .agents/skills/react-best-practices/AGENTS.md (workspace-level injection only; no frontend work is in scope)
- docs/standards/frontend-standards.md (workspace-level injection only; no frontend work is in scope)
- docs/templates/eval-report-template.md
- docs/templates/requirements-request-template.md
- docs/templates/wi-subagent-handoff-template.md

REQ/Context Docs:
- deliverables/user/REQ-20260817-ATS-008.md
- deliverables/user/REQ-20260817-ATS-007.md
- deliverables/user/WI-20260817-ATS-011-summary.md
- deliverables/agent/WI-20260817-ATS-011-evidence-pack.md
- deliverables/agent/WI-20260817-ATS-011-handoff.md
- .agents/skills/create-wi-handoff-packet/SKILL.md
- .agents/skills/create-wi-evidence-pack/SKILL.md
- .claude/config/workspace.json
- .claude/config/context-injection-rules.json

Files:
- scripts/database/bootstrap-disposable-mysql.ps1
- scripts/database/DisposableMysqlBootstrap.java
- scripts/database/README.md

Repro/Logs:
- One supported-wrapper Inventory invocation with the complete child stream held only in process memory and reduced before reporting
- python .agents/skills/validate-docs/scripts/validate_docs.py

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-012-summary.md :
- Sanitized terminal result, exact action counts, no-cleanup boundary, and separate-approval boundary for a positive count
Agent-facing -> deliverables/agent/WI-20260817-ATS-012-evidence-pack.md :
- Safe-gate evidence, injected-context pointers, sanitized command class, action counts, documentation-validation result, and no-op rollback
Handoff Packet -> deliverables/agent/WI-20260817-ATS-012-handoff.md :
- This packet for traceability

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Record only safe repository paths and the sanitized wrapper-invocation class; never record the private bundle identifier/location or raw stream.
Tests: Validate the final WI documents with the project documentation validator only after the one runtime invocation.
Rollback (if needed): No runtime mutation is authorized. Revert only WI-012 deliverables through approved follow-up work; no database rollback or cleanup is authorized.
