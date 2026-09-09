---
version: 1.0
last_updated: 2026-08-17
project: ATS
owner: qa-integ
category: wi-handoff
status: approved-execution
related_req: REQ-20260817-ATS-006
---

> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A028: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a028). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

[WI HEADER]
WI ID: WI-20260817-ATS-009
REQ: REQ-20260817-ATS-006
Agent: qa-integ
Depends On: WI-20260817-ATS-008 (historical blocked observation record), approved REQ-20260817-ATS-006
Blocks: -

[WI SUMMARY]
Why: Determine, exactly once and read-only, whether guarded disposable MySQL schemas may remain after WI-008 while preserving secret-safe evidence and correcting one inaccurate historical Follow-ups sentence.

Scope (in/out):
- In: Create this packet before database access. Correct only the inaccurate WI-008 Follow-ups line without changing its historical one-observation restriction, results, or blocked status. Run one purpose-built temporary wrapper outside the repository using the supplied private bundle solely as opaque input.
- In: The wrapper must validate the JDBC URL shape and loopback host before loading the JDBC driver or connecting; derive a root-only MySQL endpoint; execute exactly one parameterless aggregate inventory whose database-side predicate enforces the exact disposable-name guard; retain only a sanitized count and terminal status.
- Out: All cleanup, deletion, retry, schema/data mutation, application or browser testing, external interaction, Git action, code/configuration change, manifest recording, and follow-up WI creation.

DoD:
- The WI ID is the next valid ATS identifier and this handoff exists before database access.
- WI-008 has exactly its inaccurate Follow-ups claim corrected; its historical restriction and results remain intact.
- One inventory attempt either records the sanitized count, read-only gate values, and `NO_POSSIBLE_ORPHAN` or `POSSIBLE_ORPHAN_EXISTS`, or records a sanitized blocked status before any database query.
- The temporary wrapper reveals no bundle location/content/value, connection detail, raw SQL, schema name, row, or raw command output; it clears its process environment and removes itself in a finally block.
- User-facing and agent-facing deliverables are written, then documentation validation passes.

Constraints/Forbidden:
- Treat the private input bundle as opaque. Do not inspect, persist, display, or cite its location, content, credential, environment value, JDBC URL, host, port, or derived connection information.
- Connect only to the loopback server root endpoint and never select, connect to, enumerate, or return any application or named schema.
- The inventory must use one parameterless count query; its WHERE predicate must itself apply the exact bootstrap disposable-name regex. It must not issue any other database statement or inspect schema names.
- On any failed or unverified safety gate, do not load the driver, connect, query, retry, or broaden scope. On any positive count, stop after documentation; destructive cleanup requires separate explicit approval.
- Do not retain raw SQL, raw output, temporary-wrapper contents, or temporary-wrapper path in repository deliverables.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] The approved REQ and highest existing WI identifier are verified before packet creation.
- [ ] The sole WI-008 Follow-ups inaccuracy is corrected without modifying historical results or restrictions.
- [ ] Exactly one read-only inventory is attempted only after positive opaque-input, JDBC-format, loopback, root-endpoint, driver, and count-query safety gates.
- [ ] Success evidence contains only sanitized count, terminal status, bounded safety gates, and possible-orphan status; a blocked result contains only a sanitized reason/status.
- [ ] No schema name, raw row, raw SQL, bundle detail, connection detail, source/configuration change, cleanup, retry, test, browser, external, or Git action occurs.
Performance:
- [ ] Not applicable: one bounded aggregate inventory with no response-time target.
Quality:
- [ ] G1 through G4 of REQ-20260817-ATS-006 are evidenced without prohibited retention.
- [ ] Documentation validation succeeds after the two-set deliverables are written.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Required by assignee and approved REQ):
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
- docs/standards/evidence-pack-standard.md

Tier 1 (Policies - Inferred from the approved DB inventory):
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md
- docs/architecture/system-design.md

Tier 2 (Workflow / technical context):
- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md
- docs/templates/eval-report-template.md
- docs/templates/requirements-request-template.md
- docs/templates/wi-subagent-handoff-template.md

REQ/Context Docs:
- deliverables/user/REQ-20260817-ATS-006.md
- deliverables/user/WI-20260817-ATS-008-summary.md
- deliverables/agent/WI-20260817-ATS-008-handoff.md
- deliverables/agent/WI-20260817-ATS-008-evidence-pack.md
- scripts/database/README.md

Files:
- scripts/database/bootstrap-disposable-mysql.ps1
- scripts/database/DisposableMysqlBootstrap.java

Repro/Logs:
- One temporary outside-repository inventory wrapper; it emits only sanitized terminal fields and is removed in its finally block. No raw log is retained.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-009-summary.md :
- Sanitized result, documentation correction, whether destructive cleanup approval is required, and scope boundary.
Agent-facing -> deliverables/agent/WI-20260817-ATS-009-evidence-pack.md :
- Pointer-based safety evidence, sanitized count/status, documentation validation result, risks, and rollback position.
Handoff Packet -> deliverables/agent/WI-20260817-ATS-009-handoff.md :
- This packet, created before the sole database inventory attempt.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/actions): Required; record only the safe action sequence and terminal fields.
Tests: Run only the documentation validator after deliverables are written; no application or database test is authorized.
Rollback (if needed): No product, configuration, schema, or data change is allowed. Documentation changes can be manually reverted only under a separately approved documentation correction; no database rollback applies.
