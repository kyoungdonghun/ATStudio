[WI HEADER]
WI ID: WI-20260714-ATS-038
REQ: REQ-20260714-ATS-001
Agent: se
Depends On: WI-20260714-ATS-015, WI-20260714-ATS-017, WI-20260714-ATS-022, WI-20260714-ATS-037
Blocks: WI-20260714-ATS-039

[WI SUMMARY]
Why: A fresh disposable acceptance database contains no subscription plan reference rows, so the existing QA bootstrap cannot create ACTIVE/CANCELLED subscriber fixtures. A client preview must exercise subscriber-only routes without reusing retained development or production data.
Scope (in):
- Add an acceptance-only subscription-plan bootstrap that runs before `TestUserBootstrapRunner`.
- Seed exactly the six canonical INDIVIDUAL/BUSINESS STANDARD, DELUXE, and PREMIUM plans from `docs/design/db-schema.md` only when missing.
- Keep the bootstrap idempotent, deterministic, and fail closed on an existing inactive/conflicting canonical plan.
- Add focused tests for fresh seed, idempotent restart, conflict refusal, and activation/property/profile gating.
- Produce user summary and agent Evidence Pack.
Scope (out):
- No production or retained local DB mutation.
- No schema change, data deletion, payment/provider call, Toss live key, email delivery, public tunnel start, branch/worktree/commit operation, or full test suite.
- Do not modify payment integrity remediation files.
DoD:
- On an empty acceptance schema, canonical plans exist before QA users and the subscriber/grace/business fixtures receive their intended subscriptions.
- Restarting the bootstrap creates no duplicates and does not rewrite existing active plan values.
- The runner cannot activate outside explicit `acceptance` profile with both acceptance and QA bootstrap flags enabled.
- Focused tests and scoped diff checks pass.
Constraints/Forbidden:
- The repository is shared. Do not revert or overwrite edits made by other workers.
- Never log credentials, URLs, JDBC values, account emails, or raw external configuration.
- Do not run against any real/retained database.
- Documentation is English except the user-facing summary, which is Korean.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Six canonical plan rows are inserted into a fresh acceptance database.
- [ ] Existing matching active rows are preserved without mutation or duplication.
- [ ] Existing inactive/conflicting canonical rows cause startup refusal with a non-sensitive reason.
- [ ] Plan bootstrap order precedes `TestUserBootstrapRunner`.
- [ ] `qa_subscriber`, `qa_grace`, and `qa_business` can resolve DELUXE/INDIVIDUAL, STANDARD/INDIVIDUAL, and PREMIUM/BUSINESS respectively after bootstrap.
Quality:
- [ ] Focused unit/configuration tests pass.
- [ ] `git diff --check` passes for owned files.
- [ ] No schema, retained DB, payment, or secret mutation occurs.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2 / Context:
- deliverables/user/REQ-20260714-ATS-001.md
- deliverables/agent/WI-20260714-ATS-015-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-017-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-022-evidence-pack.md
- docs/design/db-schema.md
Files:
- src/main/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapRunner.java
- src/main/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapProperties.java
- src/main/java/com/atstudio/atstudio/entity/Subscription.java
- src/main/java/com/atstudio/atstudio/repository/SubscriptionRepository.java
- src/main/resources/application-acceptance.yml
- src/test/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapRunnerTest.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-038-summary.md
- Korean summary of behavior, tests, limitations, and next WI.
Agent-facing -> deliverables/agent/WI-20260714-ATS-038-evidence-pack.md
- Exact file pointers, behavior evidence, focused test command/result, diff check, rollback, residual risk.
Handoff Packet -> deliverables/agent/WI-20260714-ATS-038-handoff.md
- This packet.

[TRACEABILITY REQUIREMENTS]
- List every changed file and focused test result.
- Explain ordering and all profile/property guards.
- Record that no retained DB, schema, provider, email, or public tunnel was touched.
- Rollback must be application-code removal only; no data rollback is performed by this WI.
