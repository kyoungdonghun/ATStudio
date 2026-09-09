[WI HEADER]
WI ID: WI-20260817-ATS-017
REQ: REQ-20260817-ATS-009
Agent: cr
Depends On: WI-20260817-ATS-015, WI-20260817-ATS-016
Blocks: -

[WI SUMMARY]
Why: Independently determine whether the WI-013 through WI-016 repository state is a release-candidate-ready baseline, document the remaining operational gates, and identify a commit candidate without changing the candidate's contents.
Scope (in/out): Review the current source, frontend dependency state, schema and guarded disposable-MySQL proof, current-state documentation, prior WI evidence, and working tree. Run the REQ quality gates and create the release checklist and two required deliverables. Do not run a new database lifecycle, provider payment/refund, email delivery, or external-environment action.
DoD: The WI-016 completion and accepted proof facts are independently reconciled from its evidence and source guards; all specified quality gates have current, exact results; review findings and residual risks lead both deliverables; the operational checklist separates repository readiness, acceptance-environment checks, and external production gates; and a named, unstaged commit candidate list is recorded.
Constraints/Forbidden: Keep all artifacts secret-free. Do not inspect output/client-demo-screenshots-20260716-140514.zip or output/ui-ux-audit/. Do not use git add, commit, push, reset, checkout, branch deletion, file deletion, external-data modification, or secret/bundle inspection. Do not patch an implementation defect; only correct a factual current-state document when strictly necessary and after validation.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Independently reconcile WI-013 through WI-016 evidence against current source, dependencies, schema/bootstrap guards, and current-state documentation.
- [ ] Verify the accepted WI-016 no-orphan and proof facts only through evidence and source guards, without a new database lifecycle.
- [ ] Produce a precise release checklist and a named, unstaged commit candidate list.
Performance:
- [ ] No production, provider, SMTP, or database lifecycle is initiated.
- [ ] No prohibited output paths or secret/bundle paths are inspected.
Quality:
- [ ] Backend full test/check, frontend typecheck, ESLint, Prettier, full Vitest, production build, npm audit --omit=dev, docs validation, and git diff --check have current results recorded.
- [ ] User-facing summary and agent-facing evidence pack are English, traceable, and secret-free.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
- docs/policies/versioning-policy.md
- docs/policies/access-control-policy.md

Tier 2 (React and workflow context):
- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md
- docs/templates/wi-subagent-handoff-template.md

REQ/Context Docs:
- deliverables/user/REQ-20260817-ATS-009.md
- deliverables/agent/WI-20260817-ATS-013-handoff.md
- deliverables/agent/WI-20260817-ATS-013-evidence-pack.md
- deliverables/agent/WI-20260817-ATS-014-handoff.md
- deliverables/agent/WI-20260817-ATS-014-evidence-pack.md
- deliverables/agent/WI-20260817-ATS-015-handoff.md
- deliverables/agent/WI-20260817-ATS-015-evidence-pack.md
- deliverables/agent/WI-20260817-ATS-016-handoff.md
- deliverables/agent/WI-20260817-ATS-016-evidence-pack.md
- deliverables/user/WI-20260817-ATS-013-summary.md
- deliverables/user/WI-20260817-ATS-014-summary.md
- deliverables/user/WI-20260817-ATS-015-summary.md
- deliverables/user/WI-20260817-ATS-016-summary.md

Files:
- frontend/package.json
- frontend/package-lock.json
- scripts/database/bootstrap-disposable-mysql.ps1
- scripts/database/test-bootstrap-guards.ps1
- scripts/database/DisposableMysqlBootstrap.java
- src/main/resources/schema.sql
- docs/design/db-schema.md
- docs/design/api-spec.md
- docs/payment/known-limits-and-next-steps.md

Repro/Logs:
- gradlew.bat check
- npm run typecheck, npm run lint, npm run format:check, npm run test:run, npm run build, npm audit --omit=dev (from frontend/)
- python .claude/scripts/validate_docs.py
- git diff --check

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-017-summary.md :
- Findings first, exact verification outcomes, residual risks, operational release checklist, and recommended next actions.
Agent-facing -> deliverables/agent/WI-20260817-ATS-017-evidence-pack.md :
- Evidence pointers, commands and current results, current-state reconciliation, unstaged commit candidate list, rollback note, and follow-up gates.
Handoff Packet -> deliverables/agent/WI-20260817-ATS-017-handoff.md :
- This packet (for traceability).

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Record each current command and exact outcome; distinguish a passed gate from an unexecuted external gate.
Rollback (if needed): Documentation-only artifacts can be reverted by removing the three WI-017 deliverables; no source, dependency, or external-data change is authorized by this WI.
