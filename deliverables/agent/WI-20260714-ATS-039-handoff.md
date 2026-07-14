[WI HEADER]
WI ID: WI-20260714-ATS-039
REQ: REQ-20260714-ATS-001
Agent: qa
Depends On: WI-20260714-ATS-037, WI-20260714-ATS-038
Blocks: WI-20260714-ATS-040

[WI SUMMARY]
Why: The client-facing acceptance worktree must be frozen only after the current security and fresh-schema subscriber fixes pass a bounded preview-safe quality gate.
Scope (in):
- Independently verify backend compilation.
- Run focused backend tests for acceptance profile/startup guard, plan/user bootstrap, Question private attachment authorization/download headers/static-path denial, and current security configuration.
- Run frontend typecheck, focused tests for auth/logout/social/proxy behavior, and frontend production build.
- Run scoped/full working-tree `git diff --check` and report unrelated runtime artifacts without modifying them.
- Produce a Korean user summary and English Evidence Pack.
Scope (out):
- No full backend test suite (reserved for WI-028), DB creation/drop/apply, tunnel, branch/worktree, staging, commit, payment/provider mutation, email, runtime process, or product-code edit.
- Do not restore or modify `frontend/tsconfig.tsbuildinfo`; report it for MA cleanup.
DoD:
- All bounded preview-safe checks pass or exact blockers are reported with file/test evidence.
- No application, DB, provider, or external state changes.
Constraints/Forbidden:
- Shared dirty worktree: do not revert or edit any product/document/runtime file.
- Do not expose secret values, JDBC URLs, tokens, account credentials, or public URLs in output/evidence.
- Do not run tests in parallel against the shared Gradle test-results directory.

[ACCEPTANCE CRITERIA]
Quality:
- [ ] `gradlew.bat compileJava` succeeds.
- [ ] Focused backend suites pass serially.
- [ ] Frontend `npm run typecheck` succeeds.
- [ ] Focused frontend tests pass.
- [ ] Frontend `npm run build` succeeds.
- [ ] `git diff --check` reports no whitespace errors; line-ending warnings are classified separately.
- [ ] Runtime logs and generated `tsconfig.tsbuildinfo` are identified as excluded artifacts.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/quality-gates.md
- docs/policies/security-policy.md
Context:
- deliverables/user/REQ-20260714-ATS-001.md
- deliverables/agent/WI-20260714-ATS-037-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-038-evidence-pack.md
- src/main/resources/application-acceptance.yml
- scripts/acceptance/AcceptanceLifecycle.psm1
Backend tests:
- src/test/java/com/atstudio/atstudio/bootstrap/AcceptanceSubscriptionPlanBootstrapRunnerTest.java
- src/test/java/com/atstudio/atstudio/bootstrap/AcceptanceSubscriptionPlanBootstrapConfigurationTest.java
- src/test/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapRunnerTest.java
- src/test/java/com/atstudio/atstudio/config/AcceptanceProfileConfigurationTest.java
- src/test/java/com/atstudio/atstudio/config/AcceptanceStartupGuardTest.java
- src/test/java/com/atstudio/atstudio/controller/QuestionControllerTest.java
- src/test/java/com/atstudio/atstudio/service/QuestionServiceTest.java
Frontend tests:
- frontend/src/api/auth.test.ts
- frontend/src/api/client.test.ts
- frontend/src/store/authStore.test.ts
- frontend/src/pages/auth/SocialLoginPage.test.tsx
- frontend/vite.config.test.ts

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-039-summary.md
Agent-facing -> deliverables/agent/WI-20260714-ATS-039-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260714-ATS-039-handoff.md

[TRACEABILITY REQUIREMENTS]
- Record exact commands and pass/fail counts without copying secret-bearing environment output.
- Distinguish product failures, environment failures, warnings, and intentionally deferred full-suite coverage.
- List changed artifacts caused by verification, especially `frontend/tsconfig.tsbuildinfo`, but do not clean them.
- State that no DB, provider, email, tunnel, Git staging, or commit was touched.
