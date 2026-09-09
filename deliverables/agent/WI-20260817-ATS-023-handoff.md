
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A051: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a051). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

[WI HEADER]
WI ID: WI-20260817-ATS-023
REQ: REQ-20260817-ATS-010
Agent: qa-integ
Depends On: User explicit exact-scope approval in this conversation; client snapshot worktree at 18928a7
Blocks: - (This bounded preparation does not satisfy or advance the broader external-effect rehearsal.)

[WI SUMMARY]
Why: Prepare one isolated, disposable, loopback MySQL acceptance database from the frozen client snapshot without touching any existing database or starting the acceptance runtime.
Scope (in): Run the guarded database helper tests and the Preflight, Create, and Validate actions for one fresh permitted database name; create a separate user-only-ACL client backend-environment JSON copy that preserves source keys and changes only the datasource database target on the same loopback host and port; add the local secure copier needed to perform that copy without exposing bundle values; record safe evidence in both deliverable sets.
Scope (out): Any existing development, stage, production, system, or atstudio database or data; live Toss, refund, SMTP, or production deployment; original-bundle mutation; secret, credential, JDBC URL, provider key, database-name, or raw bundle-value disclosure; git staging/commit; branch/worktree/file deletion.
DoD: The client worktree guard test and guarded Preflight, Create, and Validate actions succeed against exactly one new disposable database with the current 43-table schema and seed; the actual acceptance runtime starts with `ddl-auto=validate`; the protected copy has the same source key set, only its datasource target differs, and its filesystem ACL is user-only; user and agent deliverables contain safe statuses/counts only.
Constraints/Forbidden: Use the original repo-external bundle only as a read-only credential source. Generate and retain the exact disposable name only internally. Do not print or write source/copied bundle values to logs or deliverables. Do not start any runtime. Do not drop the disposable database or remove the bundle copy without a new exact-scope approval.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] The client snapshot resolves to the approved commit and `test-bootstrap-guards.ps1` passes.
- [ ] Guarded Preflight, Create, and Validate each report PASS using one internally generated allowed target.
- [ ] The actual acceptance runtime reports ready after Spring/Hibernate `ddl-auto=validate` startup.
- [ ] Safe helper evidence confirms the current 43-table source/manifest condition without an exact target identifier.
- [ ] A separate JSON copy exists under the approved local user directory with an identical key set and a changed datasource database target only.
- [ ] The copied bundle has user-only ACL validation and no bundle values are printed.
Performance:
- [ ] N/A: bounded local preparation; no service is started.
Quality:
- [ ] The secure bundle copier is syntax-checked and does not emit bundle values.
- [ ] `git diff --check` passes for tracked changes.
- [ ] User summary and agent evidence pack contain command statuses, rollback boundary, and no-secret assertion.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
- docs/standards/evidence-pack-standard.md

Tier 1:
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md
- docs/architecture/system-design.md

Tier 2:
- docs/templates/requirements-request-template.md
- docs/templates/wi-subagent-handoff-template.md
- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md

REQ/Context Docs:
- deliverables/user/REQ-20260817-ATS-010.md
- deliverables/agent/WI-20260817-ATS-021-evidence-pack.md
- deliverables/agent/WI-20260817-ATS-022-evidence-pack.md
- scripts/database/README.md
- scripts/acceptance/README.md

Files:
- scripts/database/test-bootstrap-guards.ps1
- scripts/database/bootstrap-disposable-mysql.ps1
- $USERPROFILE\AppData\Local\ATStudio\worktrees\v1-client-acceptance-20260817

Repro/Logs:
- Safe command statuses, aggregate counts, and ACL/key-set checks recorded only in the required WI-023 evidence deliverables.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-023-summary.md:
- Bounded result, safe statuses/counts, changed paths, risks, approval-required rollback, and explicit non-actions.
Agent-facing -> deliverables/agent/WI-20260817-ATS-023-evidence-pack.md:
- Evidence pointers, safe commands/results, key-set and ACL checks, reproducibility, rollback, and no-secret assertion.
Handoff Packet -> deliverables/agent/WI-20260817-ATS-023-handoff.md:
- This packet (for traceability).

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/commands/logs): Required; evidence must use safe status/count fields only.
Tests: Record guard test plus each bootstrap action and `git diff --check` result.
Rollback: Retain the exact disposable database and protected copy until a later user approves dropping/removing the named object; no cleanup action is authorized by this WI.
