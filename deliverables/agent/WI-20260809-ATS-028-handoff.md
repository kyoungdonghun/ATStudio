[WI HEADER]
WI ID: WI-20260809-ATS-028
REQ: REQ-20260809-ATS-001
Agent: qa-integ
Depends On: WI-20260809-ATS-020; WI-20260809-ATS-025..027 for shared admin and payment context only
Blocks: WI-20260809-ATS-030
Baseline: codex/v1-release-rehearsal-fixes @ e343c2085fbc82c66b44fb8e5edde35bf920980f

[WI SUMMARY]
Why: Perform the source, automated-test, and read-only integration audit assigned to WI-028 for every active ADMIN operational surface. Correlate UI state, frontend request invocation, backend authorization/business response, audit/state-machine behavior, and durable-state expectations without executing live administrative, financial, file, mail, or database mutations.

Scope (in/out):

In:

- Audit matrix rows `ADM-01` through `ADM-11`, `ADM-14`, and shared confirmation row `SH-07` from `WI-20260809-ATS-020-acceptance-matrix.md`.
- Cover `/admin/dashboard`, `/admin/users`, `/admin/subscriptions`, `/admin/licenses`, `/admin/questions`, `/admin/company-certifications`, `/admin/tags`, `/admin/track-manage`, `/admin/user-subscriptions`, `/admin/payments`, `/admin/whitelist-channels`, and `/admin/settings`.
- Verify exact ADMIN route guards, anonymous safe return behavior, USER denial targets, navigation/menu discoverability, loading/empty/populated/error/retry/race states, selection/filter/page preservation, stale-response handling, confirmation semantics, pending/duplicate submission handling, and canonical reload behavior.
- Trace every active frontend ADMIN API wrapper to controller mapping, request/response DTO, service transition, repository/entity/audit boundary, and existing automated tests. Classify API-only support endpoints explicitly; do not infer a missing UI is automatically a defect.
- Audit role changes including reason capture, self-demotion, last-admin protection, stale authority, session refresh, denial handling, and durable audit expectations.
- Audit dashboard totals/recent users, read-only subscription-plan projection, selected-user license projection, Question list/detail/status mutation, Tag create/edit/delete conflicts, Track list/delete dependency behavior, Company Certification review controls, Whitelist review controls, and public/admin settings agreement.
- Audit ADMIN local subscription correction preview/request/approve/execute/recovery. Confirm copy distinguishes local entitlement correction from Toss charge/refund, stale state is revalidated, duplicate mutation is blocked, and operation/audit state is resumable.
- Audit all nine Payment Operations tabs and their UI/API/state contracts: orders, billing agreements, payments, reconciliation incidents, receipts, operation audits, settlements, refunds, and entitlement corrections. Include reconciliation trigger/API-only classification, provider/local comparison rules, refund and correction staged operations, settlement status controls, receipt URL/card/provider-data sanitization, and persistent audit expectations.
- Audit the payment reconciliation scheduled boundary and withdrawn-user billing cleanup coordinator from source and existing automated tests: candidate selection, bounded window, idempotency, incident creation/update, no automatic money mutation, eligible withdrawn-user filtering, failure observability/retry, and active-user exclusion.
- Treat UI copy/control, frontend request invocation, server/test-provider response, and durable audit/domain state as four independent evidence lanes. Use isolated automated tests as test-managed persistence evidence only.
- Use WI-025 through WI-027 findings/evidence only for shared route, modal, Company Certification, Whitelist, subscription, payment, and evidence-boundary context. Re-check every conclusion against current source and the frozen baseline.

Out:

- Real ADMIN mutation through the live browser/API; role, question, tag, track, subscription, payment, refund, correction, settlement, incident, whitelist, certification, or setting mutation against the local/live DB.
- Real Toss prepare, charge, refund, cancellation, reconciliation lookup, provider mutation, external mail, file download/upload, CSV import/export, private-document access, storage mutation, or production-provider execution.
- Binary/CSV/file-content verification assigned to WI-029. WI-028 may audit controls, API contracts, validation, copy, state transitions, and source/tests but must not click or execute downloads/imports/exports.
- DB/schema/data cleanup, migration, seed/reset, direct durable-row inspection, secret/credential inspection, or fixture mutation.
- Product code, tests, configuration, fixtures, schema, runtime implementation, branch, unrelated/current product documentation, or unrelated deliverables. Documentation changes are limited to this handoff plus `WI-20260809-ATS-028-findings.md`, `WI-20260809-ATS-028-evidence-pack.md`, and `deliverables/user/WI-20260809-ATS-028-summary.md`.
- Opening, reading, hashing, metadata-probing, moving, replacing, deleting, staging, committing, or using `output/client-demo-screenshots-20260716-140514.zip` as a fixture.

DoD:

- Every assigned matrix row is classified `PASS`, `FAIL`, `BLOCKED`, or `NOT RUN`, with exact route, role/state, source/test/screenshot/log pointer, and no whole-row overstatement.
- Every operation records separate UI, frontend invocation, server/test-provider, and durable-state statements. Unavailable lanes are `BLOCKED` or `NOT INSPECTED`; source/mock/UI copy never substitutes for an observed runtime/provider/DB result.
- Findings are split by independent cause/contract and include severity, impact, exact evidence, and a bounded follow-up. Cross-page shared causes are one finding with all affected rows, not duplicated findings.
- API-only support endpoints and scheduled methods are explicitly classified as intentional, unreachable, ambiguous, or missing-product-surface, with documentary and call-site evidence.
- Targeted frontend/backend tests, typecheck, ESLint, output-document Prettier, docs validation, and `git diff --check` are recorded with exact commands/results only when executed.
- Any browser use is anonymous/read-only, records exact guard results, and is restored to `http://127.0.0.1:5173/` with no dialog/form/download/player activity.
- Product/runtime/DB/configuration remain frozen; the intentional ZIP remains path/status-only and uninspected.

Constraints/Forbidden:

- Treat `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md` as the executable design. Verify branch/HEAD and worktree boundaries before relying on evidence.
- Do not inspect browser storage/cookies/session state or local ignored secret files. Do not print tokens, credentials, PII, private document paths, raw provider payloads, full card numbers, receipt secrets, or sensitive bodies.
- Do not sign in, provision an account, alter DB state, or use remembered credentials. Anonymous guard navigation is permitted; authenticated ADMIN behavior must come from source/tests unless a separately approved safe fixture is provided.
- Automated tests may use their configured isolated test database and test-provider/mocks. Record that boundary and never report it as live/provider/production proof.
- No real charge/refund/provider cancellation/reconciliation lookup, no external mail, no file/CSV action, no live scheduler invocation, and no direct DB/storage mutation.
- Product code is frozen during this audit. Do not edit source, tests, config, schema, fixtures, current docs, branch, or git index.

[ACCEPTANCE CRITERIA]
Functional:

- [ ] `ADM-01`: dashboard loading/error/empty/populated states and totals/recent-user projection agree with API/service contracts; dense cards remain readable.
- [ ] `ADM-02`: User search/page/detail/role change, reason, self-demotion, last-admin protection, stale authority, duplicate submission, session refresh, failure preservation, audit, and reload contracts are explicit.
- [ ] `ADM-03`: ADMIN Subscription Plans are read-only, complete, correctly priced/cycled/limited/active, and expose no misleading mutation affordance.
- [ ] `ADM-04`: Users then selected-user Licenses handle Enter/search/page/selection replacement, stale responses, empty/error/retry, ownership, and list/detail support API consistency.
- [ ] `ADM-05`: Question filters/page/detail/status update preserve identity, answer ownership, one mutation, readable conflicts/errors, audit expectations, and canonical list/detail agreement.
- [ ] `ADM-06`: Company Certification list/detail/review controls distinguish states and dialogs, bound reasons, fence stale responses, avoid storage-path leakage, and defer actual private download to WI-029.
- [ ] `ADM-07`: Tag create/update/delete catches client-decidable duplicate locally, presents server conflicts in-context, preserves form/filter state, confirms dependencies/destruction, and avoids error-page escape.
- [ ] `ADM-08`: Track search/filter/page/edit/delete state preserves context, confirms target, handles dependency/not-found/stale errors, and audits the API-only audio-analysis dry-run without executing it live.
- [ ] `ADM-09`: local subscription correction preview/request/approve/execute/recovery is resumable, revalidates stale state, uses typed confirmation, persists audit expectations, and never implies Toss charge/refund.
- [ ] `ADM-10`: all nine Payment Operations tabs and every exposed stage have exact target/amount/state copy, role denial, duplicate/pending/unknown handling, refresh consistency, sanitized identifiers, and persistent audit expectations. Test-provider/source evidence remains separate from live Provider and DB evidence.
- [ ] `ADM-11`: Whitelist list/filter/status controls and export-scope copy are explicit; status note bounds and failed-reload behavior are checked; immutable CSV content itself remains WI-029.
- [ ] `ADM-14`: missing/present `COMPANY_CERT_GUIDE` is editable, save is single-submit and recoverable, and public Company Certification guidance has a canonical reload contract.
- [ ] `SH-07`: every ADMIN confirmation/typed-confirmation occurrence names target/action/consequence, traps/restores focus, defines Escape/cancel/pending/error behavior, and prevents duplicate requests.
- [ ] Support APIs: classify reconciliation trigger, audio-analysis dry-run, refund/correction/license/user detail, and payment history/API-only endpoints with role, contract, call-site, and product-surface evidence.
- [ ] Scheduled boundaries: verify reconciliation and withdrawn-user cleanup selection, idempotency, observability/retry, sanitization, active-user exclusion, and absence of automatic financial mutation from source and existing tests only.

Performance:

- [ ] Record layout evidence at available viewports and explicitly mark unavailable `1440x900`, `1024x768`, `390x844`, and `360x800` live checks `BLOCKED/NOT RUN`; static CSS is not a substitute for live responsive proof.
- [ ] Record request ordering and stale-response behavior for search/filter/page/detail switches, tab changes, mutation+reload, and late network responses. Do not invent a response-time SLO.

Quality:

- [ ] Findings cite exact WI-020 rows plus frontend page/component/API, backend controller/DTO/service/entity/repository/audit/test, and design/policy contracts.
- [ ] Authorization, PII, private-file, provider, receipt URL/card-data, and audit-log boundaries are reviewed without exposing sensitive values.
- [ ] Existing tests are read for assertion strength; test names/counts are not treated as behavior proof without checking relevant assertions and production source.
- [ ] Targeted Vitest and Gradle tests, `npm run typecheck`, targeted ESLint, output-document Prettier/check, docs validation, and `git diff --check` are recorded accurately when run.
- [ ] Pre/post branch, HEAD, `git status --short`, frozen-product result, ZIP preservation, and browser restoration are recorded.

[INPUT POINTERS]
Tier 0 (Constitution - required):

- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - required/inferred):

- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md
- docs/architecture/system-design.md

Tier 2 (Frontend and domain standards):

- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/AGENTS.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/ui/screen-flow.md
- docs/ui/atstudio-front-list.md
- docs/ui/modal-list.md
- docs/design/usecase/company-certification.md
- docs/design/usecase/whitelist.md
- docs/design/usecase/user-subscription.md
- docs/design/payment-operations-runbook.md
- docs/payment/system-overview.md
- docs/payment/feature-inventory.md
- docs/payment/operator-guide.md
- docs/payment/known-limits-and-next-steps.md

REQ/Context Docs:

- AGENTS.md
- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md:195-215,279-340,391-399,503-569
- deliverables/agent/WI-20260809-ATS-020-evidence-pack.md
- deliverables/agent/WI-20260809-ATS-025-findings.md and WI-20260809-ATS-025-evidence-pack.md only for shared admin Tag/Track boundaries
- deliverables/agent/WI-20260809-ATS-026-findings.md and WI-20260809-ATS-026-evidence-pack.md only for shared Company Certification/Whitelist boundaries
- deliverables/agent/WI-20260809-ATS-027-findings.md and WI-20260809-ATS-027-evidence-pack.md only for shared Subscription/Payment boundaries
- .claude/config/workspace.json
- .claude/config/context-injection-rules.json

Files - frontend:

- frontend/src/router/index.tsx
- frontend/src/router/AdminRoute.tsx
- frontend/src/router/index.test.tsx
- frontend/src/router/AdminRoute.test.tsx
- frontend/src/layouts/AdminLayout.tsx and related menu/layout styles/tests found during audit
- frontend/src/pages/admin/DashboardPage.tsx and DashboardPage.test.tsx
- frontend/src/pages/admin/UserManagePage.tsx and UserManagePage.test.tsx
- frontend/src/pages/admin/SubscriptionManagePage.tsx
- frontend/src/pages/admin/LicenseManagePage.tsx
- frontend/src/pages/admin/QuestionManagePage.tsx
- frontend/src/pages/admin/CompanyCertManagePage.tsx and CompanyCertManagePage.test.tsx
- frontend/src/pages/admin/TagManagePage.tsx and TagManagePage.test.tsx
- frontend/src/pages/admin/TrackManagePage.tsx
- frontend/src/pages/admin/UserSubscriptionManagePage.tsx and UserSubscriptionManagePage.test.tsx
- frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx
- frontend/src/pages/admin/PaymentOperationsPage.tsx and PaymentOperationsPage.test.tsx
- frontend/src/pages/admin/WhitelistChannelManagePage.tsx and related tests
- frontend/src/pages/admin/SiteSettingsPage.tsx
- frontend/src/components/ui/ConfirmDialog.tsx and ConfirmDialog.test.tsx
- frontend/src/api/admin.ts
- frontend/src/api/adminContracts.test.ts
- frontend/src/api/adminWhitelistChannels.test.ts
- frontend/src/api/client.ts
- frontend/src/api/loadError.ts

Files - backend and persistence boundary:

- src/main/java/com/atstudio/atstudio/controller/AdminStatsController.java
- src/main/java/com/atstudio/atstudio/controller/AdminSettingController.java
- src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java
- src/main/java/com/atstudio/atstudio/controller/AdminUserSubscriptionCorrectionController.java
- src/main/java/com/atstudio/atstudio/controller/AdminWhitelistChannelController.java
- src/main/java/com/atstudio/atstudio/controller/AdminTrackAudioAnalysisController.java
- all currently mapped User, Subscription, License, Question, Tag, Track, Company Certification ADMIN controller methods discovered from `frontend/src/api/admin.ts`
- src/main/java/com/atstudio/atstudio/service/AdminStatsService.java
- src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java
- src/main/java/com/atstudio/atstudio/service/AdminPaymentReadService.java
- src/main/java/com/atstudio/atstudio/service/AdminPaymentIncidentService.java
- src/main/java/com/atstudio/atstudio/service/AdminPaymentRefundService.java
- src/main/java/com/atstudio/atstudio/service/AdminPaymentSettlementService.java
- src/main/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionService.java
- src/main/java/com/atstudio/atstudio/service/AdminWhitelistChannelService.java
- src/main/java/com/atstudio/atstudio/service/AdminTrackAudioAnalysisService.java
- src/main/java/com/atstudio/atstudio/service/AdminOperationAuditService.java
- src/main/java/com/atstudio/atstudio/service/AdminOperationRejectionAuditService.java
- src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java
- src/main/java/com/atstudio/atstudio/service/PaymentReconciliationTransactionService.java
- src/main/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentService.java
- src/main/java/com/atstudio/atstudio/service/PaymentOperationAuditLogService.java
- entities, DTOs, repositories, enums, schema tables, unique constraints, and scheduler/coordinator classes reached by the named services; list exact files in evidence

Files - automated tests:

- frontend/src/pages/admin/_.test.ts and frontend/src/pages/admin/_.test.tsx relevant to assigned pages
- frontend/src/components/ui/ConfirmDialog.test.tsx
- frontend/src/api/adminContracts.test.ts
- frontend/src/api/adminWhitelistChannels.test.ts
- src/test/java/com/atstudio/atstudio/controller/Admin\*ControllerTest.java relevant to assigned endpoints
- src/test/java/com/atstudio/atstudio/service/Admin\*ServiceTest.java relevant to assigned services
- src/test/java/com/atstudio/atstudio/service/PaymentReconciliation\*Test.java
- src/test/java/com/atstudio/atstudio/service/PaymentRefundResilienceIntegrationTest.java
- src/test/java/com/atstudio/atstudio/service/AdminOperationAuditTransactionIntegrationTest.java
- src/test/java/com/atstudio/atstudio/repository/AdminSubscriptionCorrectionRepositoryContractTest.java
- related access-control, entity state-machine, concurrency, schema-contract, and provider-sanitization tests discovered during audit

Repro/Logs:

- Record branch, HEAD, and `git status --short` before/after. Confirm frozen product code, no stage/commit, and ZIP preservation by path/status only.
- Record exact read-only anonymous ADMIN guard routes and observed redirects if the browser is used. Do not sign in or inspect session storage.
- Run bounded targeted frontend/admin tests, typecheck, and targeted ESLint where useful. Record exact command, exit code, counts, duration, warnings, and skipped/not-run areas.
- Run bounded targeted backend ADMIN/payment/reconciliation/cleanup tests with `--rerun-tasks` where cache ambiguity exists. Automated-test DB/provider effects are isolated evidence only.
- Run output-document Prettier/check, docs validation, and `git diff --check` for WI outputs. Never report unexecuted checks as passed.
- Restore browser to `http://127.0.0.1:5173/` and record dialog/form/download/player state.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-028-summary.md:

- Row outcomes, confirmed findings, blocked/not-run coverage, four-lane summary, targeted quality results, screenshot inventory, browser restoration, risks/approval points, and frozen-worktree/ZIP status.

Agent-facing -> deliverables/agent/WI-20260809-ATS-028-evidence-pack.md:

- Evidence index, row/scenario matrix, UI/request/server-test/durable lanes, route/API/backend/audit/persistence crosswalk, support-API and scheduler classifications, test commands/results, screenshots/logs, limitations, and rollback/no-mutation statement.

Findings -> deliverables/agent/WI-20260809-ATS-028-findings.md:

- One finding per independent cause/contract with severity, affected row/route, exact source/test/runtime evidence, four-lane classification, impact, and bounded follow-up.

Screenshot root -> output/ui-ux-audit/20260809/WI-028/:

- Use only for new anonymous/read-only audit screenshots with stable row/scenario/viewport/outcome names. Do not overwrite prior evidence or inspect the preserved demo ZIP.

Handoff Packet -> deliverables/agent/WI-20260809-ATS-028-handoff.md:

- This packet only.

[TRACEABILITY REQUIREMENTS]

Evidence pointers:

- Link every result to exact WI-020 row, route, state/role, frontend control/API invocation, backend mapping/DTO/service/repository/entity/audit/test, and screenshot/log where available.
- For each mutation or scheduled operation, record four separate statements: UI/control copy; frontend invocation; server/test-provider response; durable domain/audit/reload state.
- Mark unavailable authenticated, Provider, DB, storage, private-file, CSV, download, or durable-state lanes `BLOCKED` or `NOT INSPECTED`.
- Distinguish local subscription correction from payment/refund. Distinguish reconciliation list/status controls from the reconciliation trigger. Distinguish Payment Operations UI tabs from API-only detail/history endpoints.
- Verify security-sensitive display and logging are minimized without reading secrets: role reason, PII, provider identifiers, masked cards, receipt URLs, audit notes, physical storage paths, and private-document metadata.

Tests:

- Include exact commands, exit codes, counts/durations, warnings, skipped areas, and environment boundaries. Read relevant assertions before citing a test as proof. Never translate test-provider/mock or isolated DB proof into live Provider/production DB proof.

Rollback:

- This WI is read-only and changes only the declared audit documents/screenshots. No product rollback is expected. If any product/runtime/DB/config/index mutation occurs, stop and report it; do not revert or delete without explicit approval.
