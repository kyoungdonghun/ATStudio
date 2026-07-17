[WI HEADER]
WI ID: WI-20260717-ATS-018
REQ: REQ-20260716-ATS-004
Agent: qa-integ
Depends On: WI-20260717-ATS-017
Blocks: Production-readiness scope closeout

[WI SUMMARY]
Why: Prove that the official V1 baseline branch starts through the operator-controlled acceptance lifecycle and that representative public, authenticated, subscriber, business, and admin flows remain reachable after code, schema, and documentation consolidation.
Scope (in/out): In scope are acceptance lifecycle contract tests, secret-safe external bundle preflight, a newly started local frontend/backend plus Cloudflare quick tunnel, local/public readiness checks, role-based API and UI smoke tests, startup-log review, and cleanup/status evidence. The approved external bundle at the operator-controlled runtime location may be used only after confirming local MySQL, Toss test-key prefixes, and QA bootstrap enablement without printing values. Out of scope are real payment, live Toss, production deployment, product-policy changes, source/document edits, schema changes, retained-data migration, remote push, branch switching, and unrelated generated artifacts.
DoD: The acceptance lifecycle tests pass; the official branch starts with `ddl-auto=validate`; local frontend, local API, public frontend, and public proxied API respond successfully; five QA fixture roles can authenticate or their expected access boundaries are verified; representative subscription, whitelist, company certification, payment-operations, track/player, and admin surfaces are smoke-tested; no secret appears in command output or evidence; runtime is either intentionally left running with an exact current URL and manifest state or stopped cleanly; findings are classified and recorded.
Constraints/Forbidden: Use Toss test configuration only and never initiate provider mutation, billing-key registration, charge, refund execution, or real-money flow. Do not display or copy secret values, passwords, raw keys, or the external bundle content. Do not read `application-local.yml`. Do not alter source, tracked docs, schema, branches, Git refs/index, or the unrelated screenshot ZIP. Do not infer production readiness from this acceptance run. You are not alone in the codebase; do not revert other changes.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] `scripts/acceptance/test-dry-run.ps1` and `test-backend-environment.ps1` pass.
- [ ] External bundle preflight confirms a regular repo-external file, local MySQL, Toss test-key prefixes, and enabled QA bootstrap without exposing values.
- [ ] Acceptance startup reaches `ready` from `codex/p1-acceptance-hardening` with Hibernate `ddl-auto=validate` and no startup exception.
- [ ] Local frontend, local `/api/tracks`, public frontend, and public `/api/tracks` return successful responses through the supported single-origin proxy topology.
- [ ] Public/authenticated/subscriber/business/admin representative routes and API authorization boundaries are smoke-tested with the five QA fixtures.
- [ ] Payment tests remain read-only and do not call provider mutation or admin mutation endpoints.
- [ ] Runtime ownership, URL, and cleanup/leave-running decision are recorded from the current manifest, never from historical URLs.
Performance:
- [ ] Readiness completes within the lifecycle timeout without repeated crash/restart loops.
Quality:
- [ ] No newly discovered P0/P1 regression remains unexplained.
- [ ] Logs contain no secret values in captured evidence.
- [ ] `git diff --check` passes and runtime testing does not create tracked-source changes.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1 (Policies - Testing, security, and operations):
- docs/policies/quality-gates.md
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/execution-policy.md

Tier 2 (Current contracts and guides):
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/client/testing-guide.md
- docs/payment/acceptance-test-checklist.md
- docs/SR/SR-42.md
- docs/SR/SR-93.md

REQ/Context Docs:
- deliverables/user/REQ-20260716-ATS-004.md
- deliverables/agent/WI-20260717-ATS-015-evidence-pack.md
- deliverables/agent/WI-20260717-ATS-016-evidence-pack.md
- deliverables/agent/WI-20260717-ATS-017-evidence-pack.md

Files:
- scripts/acceptance/AcceptanceLifecycle.psm1
- scripts/acceptance/start.ps1
- scripts/acceptance/status.ps1
- scripts/acceptance/stop.ps1
- scripts/acceptance/test-dry-run.ps1
- scripts/acceptance/test-backend-environment.ps1
- src/main/resources/application-acceptance.yml
- src/main/java/com/atstudio/atstudio/config/AcceptanceStartupGuard.java
- src/main/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapRunner.java
- frontend/src/router/index.tsx

Repro/Logs:
- `powershell -ExecutionPolicy Bypass -File scripts/acceptance/test-dry-run.ps1`
- `powershell -ExecutionPolicy Bypass -File scripts/acceptance/test-backend-environment.ps1`
- Use the repo-external acceptance runtime manifest/log directory; record paths, status, and sanitized results only.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260717-ATS-018-summary.md :
- Korean summary of tested flows, current URL/state if left running, findings, limits, and next approval point.
Agent-facing -> deliverables/agent/WI-20260717-ATS-018-evidence-pack.md :
- Evidence pointers, sanitized commands/results, role/API/UI matrix, log review, runtime ownership, cleanup status, risks, and rollback.
Handoff Packet -> deliverables/agent/WI-20260717-ATS-018-handoff.md :
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required. Never include secret values.
Tests: Record lifecycle tests, startup status, exact current readiness endpoints/statuses, representative role-flow outcomes, and any skipped destructive/provider mutation flow.
Rollback: Stop only processes owned by the current acceptance manifest. Do not kill unrelated processes or delete external credentials/data.
