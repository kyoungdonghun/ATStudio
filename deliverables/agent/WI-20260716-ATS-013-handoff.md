[WI HEADER]
WI ID: WI-20260716-ATS-013
REQ: REQ-20260716-ATS-002
Agent: re
Depends On: WI-20260716-ATS-005, WI-20260716-ATS-006, WI-20260716-ATS-007, WI-20260716-ATS-008, WI-20260716-ATS-009, WI-20260716-ATS-010, WI-20260716-ATS-011, WI-20260716-ATS-012
Blocks: WI-20260716-ATS-015, WI-20260716-ATS-016

[WI SUMMARY]
Why: Independently verify that the complete backend remediation set compiles, passes its full regression suite, preserves approved product policy, and produces honest JaCoCo evidence before cross-layer review.

Scope (in):
- Review all backend production, test, configuration, schema, and manual-migration changes accumulated by WI-005 through WI-012.
- Run clean backend compilation/build and the complete JUnit suite, not only focused tests.
- Reproduce JaCoCo instruction, branch, line, method, and class coverage from the final working tree and identify risk-heavy low-coverage packages/classes without inventing a release threshold.
- Verify schema/entity parity and the new schema-contract/concurrency/security/payment tests, including rate limiting, payment reconciliation, billing-key crypto, whitelist, company certification, OAuth, download atomicity, playlist/catalog locking, scheduler behavior, and withdrawal billing cleanup.
- Check the approved invariants: public full-track listening, gated downloads, recurring billing-key card payment, and single-server topology.
- Review test reports and runtime logs for hidden failures, OOMs, flaky ordering, skipped-test meaning, and swallowed exceptions.
- Create the required user summary and evidence pack. Do not repair production code in this WI; report precise findings for WI-017.

Scope (out):
- Frontend quality verification, browser testing, live provider calls, retained-DB migration, secrets, client-demo worktree changes, or product-policy invention.
- Social-only withdrawal implementation while the policy remains unapproved.
- Stage, commit, push, or destructive data/schema operations.

DoD:
- Full backend build/test and JaCoCo reporting complete from the current development worktree.
- Exact test counts, failures/errors/skips, coverage metrics, notable warnings, and any residual findings are recorded with reproducible commands and report paths.
- Every finding is severity-ranked and points to concrete files/tests; a clean result explicitly states residual environment and coverage risks.
- User summary and Evidence Pack are created.

Constraints/Forbidden:
- Work only in C:/Users/jm991/Desktop/project/ATStudio on codex/p1-acceptance-hardening.
- You are not alone in the codebase. Do not revert, reformat, or overwrite existing changes; do not edit product/test code.
- Do not modify or restart the frozen client-demo worktree/runtime.
- No DB mutation, real payment/OAuth/provider call, secret access, stage, commit, or push.
- Preserve frontend/tsconfig.tsbuildinfo exactly; this WI must not run frontend commands.
- Coverage is observational. Do not fabricate thresholds or call low coverage a pass/fail gate unless the approved REQ says so.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Full Gradle build/test completes from the current working tree.
- [ ] Security, payment, whitelist, company-certification, OAuth, download, playlist/catalog, scheduler, and withdrawal regression areas are represented in the executed suite.
- [ ] Schema/entity/manual-patch contracts are reviewed without applying DDL.
- [ ] Approved product invariants show no backend regression.

Performance:
- [ ] Test execution is bounded and any memory adjustment or unusually slow suite is recorded.

Quality:
- [ ] Exact JUnit totals and skipped-test reasons are recorded from reports.
- [ ] JaCoCo HTML/XML reports are generated and summarized by metric and risk area.
- [ ] Hidden test failures, OOMs, flaky behavior, and swallowed exceptions are explicitly checked.
- [ ] git diff --check remains clean apart from non-failing line-ending warnings.

[INPUT POINTERS]
Tier 0 (Constitution and development standards):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies):
- docs/policies/quality-gates.md
- docs/policies/security-policy.md

REQ/Context Docs:
- deliverables/user/REQ-20260716-ATS-002.md
- docs/design/remaining-remediation-design-20260716.md
- deliverables/agent/WI-20260716-ATS-005-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-006-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-007-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-008-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-009-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-010-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-011-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-012-evidence-pack.md

Implementation Evidence:
- build.gradle
- src/main/java/com/atstudio/atstudio/
- src/main/resources/application.yml
- src/main/resources/schema.sql
- src/main/resources/db/manual/
- src/test/java/com/atstudio/atstudio/
- build/test-results/
- build/reports/tests/
- build/reports/jacoco/

Repro/Logs:
- gradlew.bat clean test jacocoTestReport
- gradlew.bat build
- XML/HTML report inspection and git diff --check

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-013-summary.md:
- Plain-language backend regression result, exact counts/coverage, residual risks, and whether cross-layer review may proceed.

Agent-facing -> deliverables/agent/WI-20260716-ATS-013-evidence-pack.md:
- Commands, report paths, exact metrics, test-area matrix, findings with severity/evidence, environment limits, and rollback/no-change statement.

Handoff Packet -> deliverables/agent/WI-20260716-ATS-013-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
- Distinguish rerun evidence from prior WI evidence.
- Cite exact report files/commands for every count and coverage metric.
- Record skipped tests and explain why they do or do not weaken the conclusion.
- Do not claim retained-MySQL, real-provider, secret/proxy, or production-runtime proof from local unit tests.
- Rollback must state that this WI is verification-only except its summary/evidence deliverables.
