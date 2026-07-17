[WI HEADER]
WI ID: WI-20260717-ATS-010
REQ: REQ-20260716-ATS-004
Agent: qa-integ, cr
Depends On: WI-20260717-ATS-009
Blocks: WI-20260717-ATS-011 evidence aggregation, V1 cleanup, final staging/commit

[WI SUMMARY]
Why: Independently verify that WI-009 closes every WI-008 blocker without introducing a regression and that the complete V1 working tree is ready for destructive repository cleanup.
Scope (in): Two read-only tracks. Track A reviews WI-009 frontend/backend code, tests, coverage rules/reports, and finding closure. Track B verifies current docs/code/schema/route consistency, 56-item ledger, residual searches, high-confidence secret scan, generated artifacts, runtime evidence, and exact Git/worktree/tag cleanup preconditions. Scope (out): product/doc edits, DB mutation, branch/worktree/tag deletion, staging, commit, push, and application-local.yml inspection.
DoD: Both reports issue PASS; all WI-008 P2/P3 findings are CLOSED; no new P1/P2/P3 exists; current quality metrics and smoke evidence are internally consistent; repository cleanup targets and safety tags are exact; final aggregation and cleanup are explicitly authorized.
Constraints/Forbidden: Reports only. Never read or print application-local.yml or secret values. Do not rerun the already fresh heavy full suites unless evidence is inconsistent; inspect their generated results and run focused non-mutating checks. Do not broaden policy or product scope.

[ACCEPTANCE CRITERIA]
- [ ] WI-009 code/tests genuinely implement the claimed behavior and no test is assertion-only or masks production behavior.
- [ ] Frontend 468-test and backend 1,206-test evidence, coverage counters, and fail-closed thresholds are reproducible from current generated reports/configuration.
- [ ] Seven governed backend security classes are exactly 100% line/method and global thresholds remain 80/80/70.
- [ ] All four frontend/integration findings are closed and backend/frontend contracts match.
- [ ] Current docs validation, route/API/table counts, residual searches, diff checks, and value-suppressing secret scan pass.
- [ ] Runtime/API/UI smoke evidence remains applicable to the same working-tree behavior or is explicitly rerun where WI-009 affects it.
- [ ] Tags, branches, and worktrees satisfy exact deletion preconditions.
- [ ] No unresolved P1/P2/P3 remains.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
- docs/standards/frontend-standards.md

Tier 1:
- docs/policies/quality-gates.md
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/versioning-policy.md
- docs/policies/archive-policy.md

Tier 2:
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/ui/
- docs/payment/
- docs/client/testing-guide.md
- docs/registry/project-registry.md

Evidence:
- deliverables/agent/WI-20260717-ATS-001-evidence-pack.md
- deliverables/agent/WI-20260717-ATS-008/backend-qa.md
- deliverables/agent/WI-20260717-ATS-008/frontend-qa.md
- deliverables/agent/WI-20260717-ATS-008/integration-review.md
- deliverables/agent/WI-20260717-ATS-009/backend-remediation.md
- deliverables/agent/WI-20260717-ATS-009/frontend-remediation.md
- current working tree and generated test/coverage reports

[OUTPUT CONTRACT]
Remediation verification -> deliverables/agent/WI-20260717-ATS-010/remediation-review.md
Repository readiness -> deliverables/agent/WI-20260717-ATS-010/repository-readiness.md
Handoff Packet -> deliverables/agent/WI-20260717-ATS-010-handoff.md

[TRACEABILITY REQUIREMENTS]
Each report must list exact file/line and command/report evidence, all finding mappings, severity for any new issue, and explicit PASS/BLOCK. The repository-readiness report must list exact ref/worktree targets and prove every destructive operation is bounded. Secret scan must report candidate counts and safe paths only, never values.
