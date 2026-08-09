[WI HEADER]
WI ID: WI-20260809-ATS-001
REQ: REQ-20260808-ATS-004
Agent: se
Depends On: WI-20260808-ATS-022 documentation audit
Blocks: WI-20260808-ATS-023~030

[WI SUMMARY]
Why: Repair the two code/schema-verification mismatches found while reconciling WI-022 so the approved quality chain can rely on the current V1 baseline.
Scope (in/out): Remove retired direct ADMIN subscription-route matchers; add safe manifest observation before fail-closed comparison; after separate destructive approval only, derive and replace the exact current disposable-MySQL manifest constants/hash and prove create/validate/drop cleanup. Update the WI-022 current-state documents and evidence only where the repair changes a previously recorded blocker. Do not change application schema, seed data, retained data, payment behavior, or public API behavior.
DoD: Retired matchers are absent; no direct ADMIN mutation route exists; guard tests pass; observed manifest values are emitted without target names or secrets; after approval, two independently named loopback disposable runs establish the exact manifest and leave zero disposable databases; WI-022 three-way consistency is revalidated.
Constraints/Forbidden: Work only on `codex/v1-release-rehearsal-fixes`. Preserve the shared dirty worktree and do not touch `output/client-demo-screenshots-20260716-140514.zip` or ignored root `application-local.yml`. No retained/production database access, schema or seed edits, data backfill, external provider/storage/email action, dependency change, file deletion, or branch operation. Do not run Create/Validate/Drop until the user explicitly approves the isolated destructive database step.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] `SecurityConfig` no longer grants special policy to retired `PUT`/`DELETE /api/user-subscriptions/*` paths; current `/me` and ADMIN list policies remain unchanged.
- [ ] Controller mapping evidence still proves direct ADMIN `{id}` PUT/DELETE routes are absent.
- [ ] Manifest metrics/hash are printed before fail-closed comparison without database name, credentials, paths, or row data.
- [ ] Bootstrap guard tests cover fixed current SQL inputs, loopback/name protection, target redaction, and the safe manifest-observation contract.
- [ ] After separate approval, a first isolated probe obtains current metrics/hash and auto-cleans on expected mismatch; constants are then updated; a second isolated proof passes create/validate and exact drop; protected `atstudio` is untouched and residual disposable count is zero.
Performance:
- [ ] No application query/runtime path or frontend bundle is changed.
Quality:
- [ ] Focused backend/security and bootstrap guard tests pass.
- [ ] `validate-docs` and `git diff --check` pass after blocker documentation is updated.
- [ ] Changed files, commands, redacted evidence, risks, and rollback are recorded in both outputs.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md

Tier 2/context:
- deliverables/user/REQ-20260808-ATS-004.md
- deliverables/agent/WI-20260808-ATS-022-handoff.md
- deliverables/user/WI-20260808-ATS-022-summary.md
- deliverables/agent/WI-20260808-ATS-022-evidence-pack.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- scripts/database/README.md

Files:
- src/main/java/com/atstudio/atstudio/config/SecurityConfig.java
- src/test/java/com/atstudio/atstudio/controller/UserSubscriptionControllerTest.java
- scripts/database/DisposableMysqlBootstrap.java
- scripts/database/bootstrap-disposable-mysql.ps1
- scripts/database/test-bootstrap-guards.ps1

Repro/Logs:
- `rg -n -C 5 "user-subscriptions" src/main/java/com/atstudio/atstudio/config/SecurityConfig.java src/test/java/com/atstudio/atstudio/controller/UserSubscriptionControllerTest.java`
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/database/test-bootstrap-guards.ps1`
- Disposable Create/Validate/Drop commands from `scripts/database/README.md`, only after separate user approval and with output redacted to non-secret manifest/guard fields.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-001-summary.md
Agent-facing -> deliverables/agent/WI-20260809-ATS-001-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260809-ATS-001-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers, exact changed files, executed commands/results, isolated database names represented only as redacted/generated classes, before/after manifest values, residual-database count, risks, rollback, and WI-023 unblock status are required. Never include credentials or the contents of the external environment bundle.
