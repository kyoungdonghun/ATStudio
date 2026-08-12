[WI HEADER]
WI ID: WI-20260809-ATS-026
REQ: REQ-20260809-ATS-001
Agent: qa-integ
Depends On: WI-20260809-ATS-020
Blocks: WI-20260809-ATS-030

[WI SUMMARY]
Why: Audit the USER/ADMIN Whitelist and BUSINESS/ADMIN Company Certification workflows against WI-020, correlating route guards, UI states, requests, server responses, and durable database/storage/export contracts without performing an unapproved side effect.

Scope (in/out):

In:

- Execute and/or source-audit matrix rows `G-BUS`, `MEM-12`, `MEM-13`, `MEM-14`, `ADM-06`, and `ADM-11`.
- Execute the `G-ADMIN` sublanes only for `/admin/company-certifications` and `/admin/whitelist-channels`.
- Audit USER Whitelist list, register, update, delete, set-primary, and request-registration behavior; Subscription plan and 100-profile limits; draft versus registration-relevant statuses; submitted-value immutability; deletion/removal rules; and stale review state.
- Audit BUSINESS Company Certification apply, status, and resubmit behavior; the exact BUSINESS route guard; redirects for existing statuses; PDF/JPG/JPEG/PNG client and server validation; private/quarantine storage metadata; reason, status, and timestamp projection; replacement rules; and audit evidence.
- Audit ADMIN Company Certification list, detail, private-document download boundary, and review statuses/reason; latest-request or stale-response fencing; durable audit; and absence of physical storage-path leakage.
- Audit ADMIN Whitelist filtering, status transitions, immutable CSV export batches, and agreement between exported snapshots and later user edits or deletes.
- Review the exact React pages, route guards, API wrappers, backend controllers/services/entities/repositories/DTOs/tests, and `docs/design/usecase/whitelist.md` plus `docs/design/usecase/company-certification.md`.

Out:

- All matrix rows not named above, including subscription/payment execution and the remaining ADMIN routes.
- Product code, tests, configuration, schema, fixtures, current-state design baselines, and runtime implementation changes.
- Authenticated USER, BUSINESS, or ADMIN execution without an explicitly authorized session and safe fixture.
- Registration, resubmission, review, delete/removal, primary, status, CSV export/download, private-document read/download, database/storage inspection, PII/body inspection, or any external side effect without separate approval.

DoD:

- Every in-scope row and both `G-ADMIN` sublanes are classified `PASS`, `FAIL`, `BLOCKED`, or `NOT RUN`, with an evidence pointer or an explicit missing approval/session/fixture reason.
- Loading, empty, populated, invalid, denied, error, retry, duplicate, race/stale-response, keyboard/focus, and responsive states are evidenced at `1440x900`, `1024x768`, `390x844`, and `360x800`, or carry an explicit `BLOCKED` reason.
- Every result separates UI observation, frontend request invocation, server response, and durable database/storage/export state. No lane is inferred from another lane.
- Findings, evidence, user summary, screenshots, commands, limitations, and browser restoration use the WI-026 output contract below.
- Product/runtime remains frozen, targeted quality checks are recorded, the preserved demo ZIP is untouched, and no stage or commit operation is performed.

Constraints/Forbidden:

- Treat `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md` as the executable test design and verify current runtime identity before relying on browser evidence.
- Anonymous read-only browser guard checks may run later. Do not claim authenticated USER, BUSINESS, or ADMIN behavior without an authorized session.
- Do not invoke registration, request-registration, set-primary, update, delete/removal, certification apply/resubmit/review, status transition, private-document access/download, CSV export/download, or any other mutation without separate approval and a safe fixture.
- Do not inspect a live database, storage contents, private document bytes, CSV bytes, request/response bodies containing PII, browser/session secrets, ignored configuration, or environment secrets.
- Preserve `output/client-demo-screenshots-20260716-140514.zip` byte-for-byte. Do not inspect, move, replace, delete, or use it as a fixture.
- Sanitize evidence. Never expose tokens, credentials, PII, private filenames, physical storage paths, or secret/internal persistence fields.
- Use WI-021 through WI-025 only when an exact shared route, guard, component, or evidence boundary is relevant; do not copy unrelated findings or conclusions.
- Restore any browser used for read-only checks to a neutral public route with dialogs closed, no active form/download, no player overlay, and the viewport reset. Record the result.
- Do not stage, commit, branch, reset, restart services, or modify unrelated untracked work.

[ACCEPTANCE CRITERIA]
Functional:

- [ ] `G-BUS`: verify `/company-certification/apply` and `/company-certification/status` use the exact authenticated USER plus `userType=BUSINESS` guard. Record anonymous behavior read-only; record individual USER and ADMIN behavior only with authorized sessions, otherwise `BLOCKED`.
- [ ] Verify existing `PENDING`, `APPROVED`, `REJECTED`, and `REVISION_REQUESTED` certification states select the documented apply/status target without content flash, redirect loops, or a request from the wrong page.
- [ ] `MEM-12`: verify Whitelist list/register/update/delete/set-primary/request-registration contracts, plan limits, the 100-profile safety cap, draft versus registration-relevant status counting, immutable submitted values, immediate-delete versus removal-request rules, replacement primary behavior, stale review state, duplicate submission, and reload agreement.
- [ ] `MEM-13`: verify Company Certification apply/current-state/guide requests, client validation and server verification for PDF/JPG/JPEG/PNG, count/per-file/aggregate/filename boundaries, corrupt or mismatched content rejection, one submission, private storage metadata, and no false application on failure.
- [ ] `MEM-14`: verify status/reason/timestamps and resubmit replacement are available only in the documented state; rejected/revision information remains readable; replacement preserves the application identity where required; old/new document metadata, storage mutation or quarantine state, audit, and reload result agree.
- [ ] `ADM-06`: verify ADMIN list filter/page, detail selection, private-document download control, review statuses, conditional bounded reason, duplicate/retry behavior, stale list/detail/review response fencing, durable status/audit agreement, and no storage-path leak in UI, API, logs, metadata, or download filename.
- [ ] `ADM-11`: verify ADMIN list filter/page, allowed status transitions and reason, stale user/admin state handling, immutable CSV export batch/items, formula-safe and bounded snapshot fields, batch/download agreement, and snapshot stability after later user edits or deletes.
- [ ] For `/admin/company-certifications?from=audit`, verify anonymous navigation resolves to `/login?returnTo=%2Fadmin%2Fcompany-certifications%3Ffrom%3Daudit` without invoking an ADMIN API.
- [ ] For `/admin/whitelist-channels?from=audit`, verify anonymous navigation resolves to `/login?returnTo=%2Fadmin%2Fwhitelist-channels%3Ffrom%3Daudit` without invoking an ADMIN API.
- [ ] Cover loading, legitimate empty, populated, invalid/missing ID or filter, denied/not-found, validation, server error, retry/recovery, duplicate/repeat, stale/race, keyboard/focus, and responsive states for every applicable row. Use explicit `BLOCKED` reasons where authorization or safe fixtures are unavailable.
- [ ] Record route, role/fixture, viewport, state, expected result, actual result, and evidence path for each browser or source/test observation. Do not collapse `PASS`, `FAIL`, `BLOCKED`, and `NOT RUN`.

Performance:

- [ ] At `1440x900`, `1024x768`, `390x844`, and `360x800`, check stable loading/table/form/dialog/file/status layouts, horizontal overflow, clipping, occlusion, layout shift, focus visibility, and responsive controls.
- [ ] For filters, pagination, detail selection, retries, submit fences, review state, and export state, record request ordering and whether a late response can overwrite the current UI. Do not invent a response-time SLO.

Quality:

- [ ] Findings cite the exact matrix row and relevant frontend page/guard, API function and method/path, backend controller/service/entity/repository/DTO, test, and design contract.
- [ ] Evidence uses four independent lanes: UI; request invocation; server response; and durable DB/storage/export state. Mark unavailable lanes `BLOCKED` or `NOT INSPECTED`.
- [ ] No authenticated role, successful mutation, private-document access, CSV output, database row, storage object, or audit record is claimed from source code, a toast, or another lane alone.
- [ ] Targeted frontend and backend tests, typecheck/lint where run, output-document Prettier, documentation validation, and `git diff --check` record exact commands, exit codes, counts, and limitations.
- [ ] Browser restoration is recorded, product/runtime remains unchanged, the demo ZIP is preserved, and no stage or commit is performed.

[INPUT POINTERS]
Tier 0 (Constitution and standards - required):

- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - required for this audit):

- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md

Tier 2 (Frontend, API, UI, and domain references):

- docs/standards/frontend-standards.md
- docs/design/api-spec.md:80-103,134-141,233-243
- docs/design/db-schema.md
- docs/design/usecase/whitelist.md
- docs/design/usecase/company-certification.md
- docs/ui/screen-flow.md
- docs/ui/atstudio-front-list.md
- docs/ui/modal-list.md
- .agents/skills/react-best-practices/AGENTS.md

REQ/Context Docs:

- AGENTS.md
- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md:124-125,171-173,204,209,304-332,457-459,474,479,515-516
- deliverables/agent/WI-20260809-ATS-020-evidence-pack.md
- deliverables/agent/WI-20260809-ATS-021-findings.md and `WI-20260809-ATS-021-evidence-pack.md` only for exact shared guard/browser evidence
- deliverables/agent/WI-20260809-ATS-025-findings.md and `WI-20260809-ATS-025-evidence-pack.md` only for exact shared `G-ADMIN` guard evidence
- WI-022 through WI-024 findings/evidence only when a cited result has an exact shared entry point, guard, component, or evidence boundary
- .claude/config/workspace.json
- .claude/config/context-injection-rules.json

Files - frontend route, contracts, and shared utilities:

- frontend/src/router/index.tsx:118-135,183-185,209-233
- frontend/src/router/ProtectedRoute.tsx:34-60
- frontend/src/router/index.test.tsx
- frontend/src/router/ProtectedRoute.test.tsx
- frontend/src/layouts/AdminLayout.tsx:21-24
- frontend/src/types/index.ts:239-304
- frontend/src/utils/validation.ts:40-56,94-124
- frontend/src/api/client.ts
- frontend/src/api/loadError.ts
- frontend/src/api/settings.ts
- frontend/src/api/userSubscriptions.ts
- frontend/src/components/ui/Button.tsx
- frontend/src/components/ui/ConfirmDialog.tsx
- frontend/src/components/ui/Modal.tsx
- frontend/src/components/ui/Pagination.tsx

Files - USER/BUSINESS pages and APIs:

- frontend/src/pages/subscriber/WhitelistChannelPage.tsx
- frontend/src/pages/subscriber/WhitelistChannelPage.module.css
- frontend/src/pages/subscriber/WhitelistChannelPage.test.tsx
- frontend/src/pages/subscriber/whitelistChannelPolicy.ts
- frontend/src/pages/subscriber/whitelistChannelPolicy.test.ts
- frontend/src/pages/subscriber/CompanyCertApplyPage.tsx
- frontend/src/pages/subscriber/CompanyCertApplyPage.module.css
- frontend/src/pages/subscriber/CompanyCertApplyPage.test.tsx
- frontend/src/pages/subscriber/CompanyCertStatusPage.tsx
- frontend/src/pages/subscriber/CompanyCertStatusPage.module.css
- frontend/src/pages/subscriber/CompanyCertStatusPage.test.tsx
- frontend/src/api/whitelistChannels.ts:6-58
- frontend/src/api/companyCerts.ts:4-62

Files - ADMIN pages and APIs:

- frontend/src/pages/admin/CompanyCertManagePage.tsx
- frontend/src/pages/admin/CompanyCertManagePage.module.css
- frontend/src/pages/admin/CompanyCertManagePage.test.tsx
- frontend/src/pages/admin/WhitelistChannelManagePage.tsx
- frontend/src/pages/admin/WhitelistChannelManagePage.module.css
- frontend/src/pages/admin/WhitelistChannelManagePage.test.ts
- frontend/src/pages/admin/WhitelistChannelManagePage.render.test.tsx
- frontend/src/pages/admin/whitelistStatusTransitions.ts
- frontend/src/api/admin.ts:88-140,150-226
- frontend/src/api/adminContracts.test.ts:131-149
- frontend/src/api/adminWhitelistChannels.test.ts
- frontend/src/test/coverage/adminSubscriberGaps.coverage.test.tsx

Files - backend controllers, services, model, and persistence:

- src/main/java/com/atstudio/atstudio/controller/WhitelistChannelController.java:18-95
- src/main/java/com/atstudio/atstudio/controller/AdminWhitelistChannelController.java:31-85
- src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java:28-136
- src/main/java/com/atstudio/atstudio/service/WhitelistChannelService.java:32-268
- src/main/java/com/atstudio/atstudio/service/AdminWhitelistChannelService.java:44-383
- src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java:53-557
- src/main/java/com/atstudio/atstudio/entity/WhitelistChannel.java
- src/main/java/com/atstudio/atstudio/entity/WhitelistExportBatch.java
- src/main/java/com/atstudio/atstudio/entity/WhitelistExportItem.java
- src/main/java/com/atstudio/atstudio/entity/CompanyCertification.java
- src/main/java/com/atstudio/atstudio/entity/CompanyCertificationDocument.java
- src/main/java/com/atstudio/atstudio/entity/CompanyCertificationAuditLog.java
- src/main/java/com/atstudio/atstudio/entity/StorageMutation.java
- src/main/java/com/atstudio/atstudio/entity/UserSubscription.java
- src/main/java/com/atstudio/atstudio/entity/enums/WhitelistChannelStatus.java
- src/main/java/com/atstudio/atstudio/entity/enums/CompanyCertificationStatus.java
- src/main/java/com/atstudio/atstudio/entity/enums/CompanyCertificationAuditAction.java
- src/main/java/com/atstudio/atstudio/repository/WhitelistChannelRepository.java
- src/main/java/com/atstudio/atstudio/repository/WhitelistExportBatchRepository.java
- src/main/java/com/atstudio/atstudio/repository/WhitelistExportItemRepository.java
- src/main/java/com/atstudio/atstudio/repository/CompanyCertificationRepository.java
- src/main/java/com/atstudio/atstudio/repository/CompanyCertificationDocumentRepository.java
- src/main/java/com/atstudio/atstudio/repository/CompanyCertificationAuditLogRepository.java
- src/main/java/com/atstudio/atstudio/repository/StorageMutationRepository.java
- src/main/java/com/atstudio/atstudio/repository/UserSubscriptionRepository.java
- src/main/java/com/atstudio/atstudio/dto/whitelist/
- src/main/java/com/atstudio/atstudio/dto/certification/
- src/main/java/com/atstudio/atstudio/config/WhitelistChannelProperties.java
- src/main/java/com/atstudio/atstudio/config/WhitelistExportProperties.java
- src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java
- src/main/java/com/atstudio/atstudio/service/storage/StorageService.java
- src/main/java/com/atstudio/atstudio/service/storage/StorageMutationCoordinator.java
- src/main/java/com/atstudio/atstudio/service/storage/StorageMutationJournalService.java
- src/main/java/com/atstudio/atstudio/service/storage/StorageRoot.java
- src/main/java/com/atstudio/atstudio/service/storage/StorageDomain.java
- src/main/resources/schema.sql

Files - backend tests:

- src/test/java/com/atstudio/atstudio/controller/WhitelistChannelControllerTest.java
- src/test/java/com/atstudio/atstudio/controller/AdminWhitelistChannelControllerTest.java
- src/test/java/com/atstudio/atstudio/controller/CompanyCertificationControllerTest.java
- src/test/java/com/atstudio/atstudio/service/WhitelistChannelServiceTest.java
- src/test/java/com/atstudio/atstudio/service/WhitelistConcurrencyContractTest.java
- src/test/java/com/atstudio/atstudio/service/AdminWhitelistChannelServiceTest.java
- src/test/java/com/atstudio/atstudio/service/CompanyCertificationServiceTest.java
- src/test/java/com/atstudio/atstudio/service/CompanyCertificationSecurityVerificationTest.java
- src/test/java/com/atstudio/atstudio/entity/CompanyCertificationTest.java
- src/test/java/com/atstudio/atstudio/entity/CompanyCertificationSchemaContractTest.java
- src/test/java/com/atstudio/atstudio/entity/StorageMutationContractTest.java
- src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java
- src/test/java/com/atstudio/atstudio/service/storage/StorageMutationCoordinatorTest.java
- src/test/java/com/atstudio/atstudio/service/storage/StorageMutationJournalServiceTest.java

Repro/Logs:

- Record `git status --short` before and after the audit; confirm product/runtime files remain frozen, the demo ZIP remains untouched, and no stage/commit occurred.
- Record read-only browser route, role/session boundary, viewport, screenshot/DOM/accessibility observation, console, sanitized request metadata, and exact redirect for each executed scenario.
- Run targeted Vitest files listed above and targeted Gradle tests for the listed controller/service/entity/storage classes when applicable; record exact command, exit code, duration, counts, warnings, and skipped or blocked reason.
- Record `npm run typecheck`, targeted ESLint, output-document Prettier, documentation validation, and `git diff --check` only when run. Do not report an unexecuted command as passed.
- Record browser restoration to a neutral public route, with dialogs/forms/downloads closed and viewport reset.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-026-summary.md:

- Scope and row outcomes, confirmed findings, blocked coverage, risks, approval points, targeted quality results, and browser end-state restoration.

Agent-facing -> deliverables/agent/WI-20260809-ATS-026-evidence-pack.md:

- Evidence index; row and scenario outcomes; UI/request/server/durable-state lanes; screenshots; sanitized commands/results; source/API/DB/storage/export cross-checks; limitations; cross-entry references used; and rollback.

Findings -> deliverables/agent/WI-20260809-ATS-026-findings.md:

- One finding per independent cause or contract, with severity, affected row, reproduction/source evidence, UI/request/server/durable-state classification, impact, and bounded follow-up. Preserve `BLOCKED`, `NOT RUN`, and `NOT INSPECTED` distinctions.

Screenshot root -> output/ui-ux-audit/20260809/WI-026/:

- Use stable names containing row/scenario, viewport, and outcome. Do not overwrite prior WI evidence or the preserved demo ZIP.

Handoff Packet -> deliverables/agent/WI-20260809-ATS-026-handoff.md:

- This packet only.

[TRACEABILITY REQUIREMENTS]
Evidence pointers:

- Link every result to its WI-020 row, route/scenario, exact source/API/backend/test/design pointer, and screenshot or log path when available.
- For each operation, record four independent statements: what the UI displayed or enabled; what request was or was not invoked; what server response was observed; and what durable database/storage/export state was observed or remained blocked.
- For certification files, record client extension/size/count validation separately from server signature/MIME/canonicalization validation, private storage metadata, storage mutation or quarantine evidence, document replacement, and audit. Never expose bytes, PII, or a physical path.
- For Whitelist exports, correlate filter/request, batch ID, ordered immutable item snapshots, generated CSV semantics, status changes, and later user edits/deletes. Without separate approval and a safe fixture, keep every mutation/download/durable-state lane `BLOCKED`.
- Cite WI-021 through WI-025 only for directly shared evidence. Name the shared route, guard, component, or contract and do not inherit another WI's conclusion by reference alone.
- A UI message, mocked test, source path, or API success cannot prove canonical persistence, private storage, audit, or export content by itself.

Tests and commands:

- Record exact working directory, command, exit code, duration, test count, warning, and relevant sanitized excerpt for every check.
- Anonymous guard checks may be performed read-only. Authenticated and side-effecting scenarios require separate approval plus authorized sessions and fixtures; otherwise classify them explicitly.
- Restore the browser to the required neutral state and record route, viewport, closed overlays/forms, and absence of active downloads or exposed sensitive data.

Rollback:

- This handoff and its audit outputs are documentation/evidence only; no product/runtime rollback is expected.
- If separately approved, remove only untracked WI-026 findings/evidence/summary and `output/ui-ux-audit/20260809/WI-026/` artifacts created by this WI.
- Do not alter the preserved demo ZIP, WI-019 through WI-025 artifacts, product/test/config/runtime files, database/storage/export state, secrets, or environment configuration.
