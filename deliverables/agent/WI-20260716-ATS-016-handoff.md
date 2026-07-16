[WI HEADER]
WI ID: WI-20260716-ATS-016
REQ: REQ-20260716-ATS-002
Agent: cr
Depends On: WI-20260716-ATS-013, WI-20260716-ATS-014
Blocks: WI-20260716-ATS-017

[WI SUMMARY]
Why: Conduct the final adversarial code review focused on security, authorization, concurrency, compensation, financial mutations, privacy, and regression risk after the complete automated suites pass.

Scope (in):
- Review the entire WI-005 through WI-014 diff and current high-risk backend/frontend paths.
- Prioritize auth/rate limit/trusted identity, ADMIN/USER/BUSINESS boundaries, redirects/OAuth state, file/path/PII handling, payment keys and reconciliation, settlement/refund/entitlement mutations, billing-key crypto, scheduler/withdrawal cleanup, whitelist/company-certification state machines, downloads/licenses, catalog/playlist races, and frontend stale-response/duplicate-submit behavior.
- Inspect transaction boundaries, locks, idempotency, provider-success/local-failure compensation, audit evidence, error classification, secret/card-data sanitization, and information disclosure.
- Validate WI-013/WI-014 residuals and identify missing tests or unsafe assumptions. Review docs only where needed to determine intended behavior.
- Produce findings-first review with exact file/line pointers and WI-017 remediation/test guidance. Verification-only except deliverables.

Scope (out):
- Implementing fixes, changing approved policy, live provider/DB/secret/runtime actions, client branch changes, stage/commit/push.

DoD:
- P0-P3 findings are evidence-backed, deduplicated, and ordered by exploitability/user/financial impact.
- Concurrency and transaction review states which guarantees are locally proven and which remain MySQL/environment conditional.
- Every actionable finding has a minimal safe fix and regression-test expectation for WI-017.
- Required summary and Evidence Pack are created.

Constraints/Forbidden:
- Work only in C:/Users/jm991/Desktop/project/ATStudio on codex/p1-acceptance-hardening.
- You are not alone. Preserve all current changes; verification-only except WI-016 deliverables.
- Do not modify/restart the frozen client-demo branch/runtime.
- Do not invent a social-only withdrawal policy or multi-server topology.
- No destructive operation, DB/provider/secret access, stage, commit, or push.
- Findings must be concrete; do not inflate severity or report generic best-practice advice without a reachable code path.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] All high-risk domain paths and WI-013/WI-014 findings receive code-level review.
- [ ] Authorization, state transition, idempotency, transaction/locking, compensation, privacy, and error-classification contracts are explicitly assessed.
- [ ] Product invariants remain unchanged and no approved behavior is treated as a vulnerability merely because an alternative design exists.

Performance:
- [ ] Review remains read-only and does not start runtime/provider/DB processes.

Quality:
- [ ] Findings lead, ordered by severity, with tight file/line references and reproducible reasoning.
- [ ] Missing tests are tied to specific reachable risk.
- [ ] False positives and environment-only boundaries are clearly separated from code defects.
- [ ] git diff --check and tsbuildinfo baseline remain unchanged.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

REQ/Context Docs:
- deliverables/user/REQ-20260716-ATS-002.md
- docs/design/remaining-remediation-design-20260716.md
- docs/design/api-spec.md
- docs/design/payment-integration-design.md
- docs/design/payment-operations-runbook.md
- deliverables/agent/WI-20260716-ATS-005-evidence-pack.md through WI-20260716-ATS-014-evidence-pack.md
- deliverables/user/WI-20260716-ATS-013-summary.md
- deliverables/user/WI-20260716-ATS-014-summary.md

Implementation Evidence:
- src/main/java/com/atstudio/atstudio/config/
- src/main/java/com/atstudio/atstudio/security/
- src/main/java/com/atstudio/atstudio/controller/
- src/main/java/com/atstudio/atstudio/service/
- src/main/java/com/atstudio/atstudio/repository/
- src/main/java/com/atstudio/atstudio/entity/
- src/main/resources/application.yml
- src/main/resources/schema.sql
- src/test/java/com/atstudio/atstudio/
- frontend/src/api/
- frontend/src/layouts/
- frontend/src/pages/
- frontend/src/router/
- frontend/src/store/
- build/reports/jacoco/test/
- frontend/coverage/

Repro/Logs:
- git diff against the approved branch baseline and current working-tree review
- focused source/test searches for roles, transactions, locks, idempotency, redirects, file paths, payment keys, and error handling
- git diff --check and tsbuildinfo SHA-256

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-016-summary.md:
- Findings-first security/concurrency review, verified protections, environment limits, and WI-017 actions.

Agent-facing -> deliverables/agent/WI-20260716-ATS-016-evidence-pack.md:
- Diff scope, threat/race matrix, findings with exact pointers, test evidence/gaps, proposed minimal fixes, and no-change rollback statement.

Handoff Packet -> deliverables/agent/WI-20260716-ATS-016-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
- Every finding must cite a reachable code path and explain the violated contract and impact.
- Distinguish exploitable bugs, reliability races, defense-in-depth opportunities, policy-pending items, and environment-only evidence gaps.
- Carry forward or reject WI-013/WI-014 findings explicitly.
- Map each accepted code finding to a focused WI-017 fix/test; do not broaden scope into unrelated refactoring.
