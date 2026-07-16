[WI HEADER]
WI ID: WI-20260716-ATS-032
REQ: REQ-20260716-ATS-002
Agent: qa-integ
Depends On: WI-20260716-ATS-031
Blocks: final development-branch commit readiness

[WI SUMMARY]
Why: Independently verify the final integrated remediation after WI-031 and detect any remaining source/document regression before commit.
Scope (in):
- Read-only review of WI-031 closure for F-025-03, F-025-05, and F-027-03.
- Reconcile all WI-025/026/027 findings against current code, tests, design/current-state/operations documents, and WI-028/029/031 evidence.
- Inspect current generated backend/frontend/docs gate evidence supplied by MA and classify any residual as CLOSED, REOPENED, or ENVIRONMENT-CONDITIONAL.
- Verify core product invariants: public full-track listening, subscriber-only download, recurring card billing, and single-server topology.
Scope (out):
- Product/doc edits, test/build execution that writes generated output, schema/data/Provider operations, Git index/history, client worktree/runtime changes, or public tunnel operations.
DoD:
- Final disposition matrix covers every original and reopened finding.
- Any actionable P1/P2 or correctness/document mismatch is reported first with exact pointers.
- Commit-readiness is PASS only if no actionable source finding remains and MA gate evidence is current.
Constraints/Forbidden:
- Work only from C:/Users/jm991/Desktop/project/ATStudio on codex/p1-acceptance-hardening.
- You are not alone in the worktree; do not revert or overwrite any change.
- Write only the WI-032 user summary and evidence pack.
- Do not touch C:/Users/jm991/Desktop/project/ATStudio-client-demo-stable, runtime processes, frontend/tsconfig.tsbuildinfo, Git index/history, secrets, database, or Provider.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Same-status billing-agreement replacement is fenced by revision, not status alone.
- [ ] Retained colon/equals Provider identifiers cannot reach ADMIN DTO output raw.
- [ ] Service-enabled subscription docs use expiresAt.
- [ ] All prior findings have a supported final disposition.
Performance:
- [ ] No new query/network/schema behavior is introduced by WI-031.
Quality:
- [ ] Review is pointer-based and reproducible.
- [ ] Generated gate evidence is distinguished from static source review.
- [ ] No file outside the two deliverables is changed.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2:
- docs/design/api-spec.md
- docs/design/payment-integration-design.md
- docs/design/payment-operations-runbook.md
- docs/design/usecase/sound-track.md
- docs/ui/screen-flow.md

REQ/Context Docs:
- deliverables/user/REQ-20260716-ATS-002.md
- deliverables/agent/WI-20260716-ATS-025-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-026-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-027-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-028-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-029-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-030-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-031-evidence-pack.md

Files:
- Current source/test/doc pointers named by WI-025 through WI-031.
- build/test-results/test/TEST-*.xml
- build/reports/jacoco/test/jacocoTestReport.xml
- frontend/coverage/coverage-summary.json
- frontend/dist/index.html

Repro/Logs:
- MA full-gate evidence: backend 154 suites/1125 tests before WI-031; frontend 44 files/257 tests; docs/PDF/format/diff checks pass. Treat as needing a post-WI-031 refresh before final PASS.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-032-summary.md
Agent-facing -> deliverables/agent/WI-20260716-ATS-032-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260716-ATS-032-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required for every finding disposition.
Tests: Inspect and report current artifacts; do not claim commands you did not execute.
Rollback: Deliverables-only rollback because this WI is read-only.
