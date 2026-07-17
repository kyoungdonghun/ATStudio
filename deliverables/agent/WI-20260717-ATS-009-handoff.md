[WI HEADER]
WI ID: WI-20260717-ATS-009
REQ: REQ-20260716-ATS-004
Agent: se, qa-fe, re
Depends On: WI-20260717-ATS-008
Blocks: WI-20260717-ATS-010 final re-audit, V1 cleanup, final staging/commit

[WI SUMMARY]
Why: Close every finding reopened by the independent WI-008 re-audit before final V1 cleanup.
Scope (in): Frontend refresh-token persistence failure handling, download-history accessible names, complete payment-status typing, route-count comment, focused regression tests, and full frontend gates. Backend behavior-focused coverage for security-sensitive authentication/authorization/sanitization/crypto paths and fail-closed JaCoCo enforcement of the active critical-path standard. Scope (out): new product features, policy changes, blanket coverage exclusions, assertion-only tests, DB mutation, Git ref/index changes, branch cleanup, push, or reading application-local.yml.
DoD: WI-008 P2/P3 findings are fixed with focused tests; frontend full quality gates pass with thresholds; backend full clean build/test/JaCoCo verification passes; security-sensitive classes governed by the critical-path rule reach and enforce 100% line coverage with no uncovered executable method; no production code is weakened solely for coverage.
Constraints/Forbidden: Agents share one working tree and must not revert other work. Frontend and backend write sets are disjoint. Do not edit active policy to lower requirements. Do not fake coverage with trivial assertions, generated tests, exclusions, or dead-code execution. If a reported class is not actually a security-sensitive boundary, document the evidence rather than silently excluding it. Do not modify Git staging/refs, DB, secrets, or application-local.yml.

[ACCEPTANCE CRITERIA]
Frontend:
- [ ] Refresh cannot retry or authenticate with tokens that failed durable persistence; the session fails coherently and tests cover both access- and refresh-token write failures.
- [ ] Download-history search, sort, and row re-download controls have stable, track-specific accessible names and tests.
- [ ] Frontend payment status union covers every backend PaymentOrderStatus value with a contract test.
- [ ] Admin route comment matches the actual 14-route group.
- [ ] typecheck, ESLint, Prettier, coverage Vitest, and production build pass.
Backend:
- [ ] Critical-path class list is justified against the active standard.
- [ ] Behavior-focused tests cover every executable line and method in the governed security-sensitive classes.
- [ ] JaCoCo verification fails closed below 100% line/method coverage for the governed classes while retaining global 80/80/70 thresholds.
- [ ] clean build, 1,181+ tests, JaCoCo report, and both global/critical verification pass.
Quality:
- [ ] No WI-008 P2/P3 finding remains.
- [ ] No product policy or behavior regression.
- [ ] git diff --check and focused secret-safe review pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/frontend-standards.md

Tier 1:
- docs/policies/quality-gates.md
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md

Tier 2:
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/ui/atstudio-front-list.md
- .agents/skills/react-best-practices/AGENTS.md

Findings and context:
- deliverables/agent/WI-20260717-ATS-008/backend-qa.md
- deliverables/agent/WI-20260717-ATS-008/frontend-qa.md
- deliverables/agent/WI-20260717-ATS-008/integration-review.md
- deliverables/agent/WI-20260717-ATS-007-handoff.md

[TRACK OWNERSHIP]
Frontend qa-fe owns only:
- frontend/src/**
- frontend tests
Backend se/re owns only:
- src/test/java/**
- build.gradle
- production backend code only when a genuinely unreachable or untestable branch requires a behavior-preserving seam, documented explicitly

[OUTPUT CONTRACT]
Frontend implementation report -> deliverables/agent/WI-20260717-ATS-009/frontend-remediation.md
Backend implementation report -> deliverables/agent/WI-20260717-ATS-009/backend-remediation.md
Handoff Packet -> deliverables/agent/WI-20260717-ATS-009-handoff.md

[TRACEABILITY REQUIREMENTS]
Map every WI-008 finding to FIXED, DISPROVED, or BLOCKED. List exact files/lines, tests, commands, metrics, and rollback. Backend report must list the governed critical-path classes, why each is in scope, and the exact JaCoCo rules. Frontend report must list every backend status value and accessibility role/name assertion. No closure by compilation alone.
