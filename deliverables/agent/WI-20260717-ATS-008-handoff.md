[WI HEADER]
WI ID: WI-20260717-ATS-008
REQ: REQ-20260716-ATS-004
Agent: qa, qa-fe, qa-integ, cr
Depends On: WI-20260717-ATS-007
Blocks: WI-20260717-ATS-009, V1 branch/worktree cleanup, final staging/commit

[WI SUMMARY]
Why: Re-audit the WI-007 remediation from independent backend, frontend, and integrated code-doc-repository perspectives before declaring the V1 consolidation reliable.
Scope (in): Read-only review of the complete working tree after WI-007. Re-run or independently inspect backend/DB/config/payment gates, frontend/UI/API-contract gates, the 56-item disposition ledger, protected KEEP safeguards, active documentation, residual references, secret exposure, runtime evidence, and Git cleanup preconditions. Scope (out): product or document edits, database mutation, Git ref deletion, staging, commit, push, and policy expansion.
DoD: Three reports exist; each WI-006 finding is independently mapped to CLOSED, REOPENED, or DEFERRED; all executable quality gates and coverage thresholds pass; no unresolved P1/P2/P3 remains; the final integration report explicitly authorizes or blocks WI-009 evidence aggregation and repository cleanup.
Constraints/Forbidden: Review agents are read-only except for their assigned report file. Never read or print application-local.yml or secrets. Do not modify product code, active docs, DB, Git refs, index, or another report. Do not trust prior summaries without checking current files and executable evidence. Do not add features or reinterpret approved product policy.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Backend track verifies full test/build/JaCoCo gates, DB/config/provider contracts, financial invariants, and all WI-006 backend findings.
- [ ] Frontend track verifies full test/coverage/typecheck/ESLint/Prettier/build gates and all WI-006 frontend findings.
- [ ] Integration track verifies all 56 dispositions, code-doc-schema-route consistency, active negative searches, secret handling, generated-artifact policy, and branch/worktree cleanup preconditions.
- [ ] Every WI-006 finding is mapped to current evidence and no issue is closed by assertion alone.
Performance:
- [ ] No material build, bundle, startup, or request regression is visible in current evidence.
Quality:
- [ ] No unresolved P1/P2/P3 finding.
- [ ] Backend lines/methods >= 80% and branches >= 70%.
- [ ] Frontend statements/lines/functions >= 80% and branches >= 70%.
- [ ] git diff --check, documentation validation, residual searches, and high-confidence secret scans pass.

[INPUT POINTERS]
Tier 0 (Constitution and standards):
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
- docs/standards/frontend-standards.md

Tier 1 (Policies):
- docs/policies/quality-gates.md
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/versioning-policy.md
- docs/policies/archive-policy.md

Tier 2 (Current source of truth):
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/ui/
- docs/payment/
- docs/client/testing-guide.md
- docs/registry/project-registry.md
- .agents/skills/react-best-practices/AGENTS.md

REQ / decision / evidence sources:
- deliverables/user/REQ-20260716-ATS-004.md
- deliverables/agent/WI-20260717-ATS-001-evidence-pack.md
- deliverables/agent/WI-20260717-ATS-006/backend-qa.md
- deliverables/agent/WI-20260717-ATS-006/frontend-qa.md
- deliverables/agent/WI-20260717-ATS-006/integration-review.md
- deliverables/agent/WI-20260717-ATS-007-handoff.md
- current working tree and executable quality gates

[OUTPUT CONTRACT]
Backend QA -> deliverables/agent/WI-20260717-ATS-008/backend-qa.md
Frontend QA -> deliverables/agent/WI-20260717-ATS-008/frontend-qa.md
Integration review -> deliverables/agent/WI-20260717-ATS-008/integration-review.md
Handoff Packet -> deliverables/agent/WI-20260717-ATS-008-handoff.md

[TRACEABILITY REQUIREMENTS]
Each report must list injected documents, exact commands or inspected paths, current metrics, finding-by-finding closure evidence, residual risks, and an explicit PASS or BLOCK recommendation. Findings must include severity, reproducible evidence, file/line pointers, impact, and the smallest safe remediation. Report-only edits are allowed; all other repository edits are forbidden.
