[WI HEADER]
WI ID: WI-20260809-ATS-029
REQ: REQ-20260809-ATS-001
Agent: qa-integ
Depends On: WI-20260809-ATS-020; WI-20260809-ATS-021..028 for owning-flow context
Blocks: WI-20260809-ATS-030
Baseline: codex/v1-release-rehearsal-fixes @ e343c2085fbc82c66b44fb8e5edde35bf920980f

[WI SUMMARY]
Why: Audit every active CSV, attachment, private-document, import/export, and binary-download contract assigned to WI-029. Establish evidence beyond click behavior by correlating frontend invocation, HTTP headers/status, sanitized filename, bytes/parser result, domain snapshot, storage/audit behavior, and reload expectations using source and isolated automated tests only.

Scope (in/out):

In:

- Audit the binary/file sublanes of `PUB-09`, `MEM-07`, `MEM-09`, `MEM-16`, `MEM-17`, `ADM-06`, `ADM-10`, `ADM-11`, `ADM-12`, and `ADM-13` from `WI-20260809-ATS-020-acceptance-matrix.md`.
- Reconcile shared Track download entry points owned by `PUB-02`, `PUB-03`, `PUB-06`, `MEM-02`, `MEM-05`, `MEM-07`, `MEM-09`, and `SH-02` only for the common Blob, filename, entitlement, counter/history, and License result contract. Do not repeat playback/UI audits already closed in WI-023/024.
- Cover public Notice attachment download; User License/Track download; Download History re-download; Question attachment create/read/download/delete; ADMIN Notice create/edit attachment preserve/replace/remove; Company Certification private document download; Whitelist immutable export batch creation/download; and Payment Settlement CSV import/validate/reconcile/ignore behavior.
- For every generated/downloaded file, trace status, content type, Content-Disposition/sanitized filename, byte length/non-empty policy, parser result, row count, headers, representative escaped values, and comparison with the request/API/domain snapshot where existing tests make that proof possible.
- For every import, trace local file validation, filename/type/size/encoding/headers, duplicate file/row idempotency, malformed or partial-row errors, spreadsheet-formula neutralization, transaction outcome, audit/ledger result, and canonical reload.
- Audit private-file ownership and ADMIN authorization, path traversal defense, Range behavior where supported, no physical storage-path leak, and safe user-facing errors.
- Audit frontend Blob handling, object-URL creation/revocation, derived filename handling, pending/duplicate/error behavior, and no false-success navigation/toast.
- Audit storage mutation journal/recovery boundaries for Notice and Question attachments and Company Certification documents from source/tests; classify isolated temporary-directory evidence separately from live storage.
- Use existing automated tests and repository fixtures. Isolated tests may use H2, mocks/test Provider, and test temporary directories as configured by the suite. Do not create or mutate live product data or storage.
- Treat UI/control, frontend invocation, HTTP/server response, binary/parser result, and durable storage/domain/audit state as separate evidence lanes.

Out:

- Browser clicks that trigger actual upload/download/import/export, authenticated file access, live ADMIN/User mutation, direct live HTTP binary retrieval, or local/live DB/storage mutation.
- Real Provider, payment, refund, reconciliation lookup, external mail, user-data file, ignored secret, or production/private document access.
- Opening untrusted spreadsheets in Excel or any environment with formula execution enabled.
- Product code, tests, configuration, schema, fixtures, runtime, current product documentation, branch, git index, or unrelated deliverables.
- Direct inspection of `output/client-demo-screenshots-20260716-140514.zip`. It is path/status-only: do not open, read, hash, metadata-probe, move, replace, delete, stage, commit, or use it as a fixture.
- Writing outside this handoff, `deliverables/agent/WI-20260809-ATS-029-findings.md`, `deliverables/agent/WI-20260809-ATS-029-evidence-pack.md`, and `deliverables/user/WI-20260809-ATS-029-summary.md`. Do not retain generated test binaries outside existing test/build temporary outputs.

DoD:

- Every assigned file/binary sublane is `PASS`, `FAIL`, `BLOCKED`, or `NOT RUN`, with owner row, role/state, source/test pointer, and exact evidence boundary.
- Every file operation records UI/control, frontend call, HTTP status/headers, binary/parser facts, and durable storage/domain/audit/reload facts independently. Missing lanes are explicit; no click or toast is binary proof.
- All active import/export/download/upload wrappers and backend mappings are inventoried; API-only support is classified rather than assumed defective.
- Findings are independent causes/contracts with severity, impact, exact pointers, and bounded follow-up. Do not duplicate owning-flow findings from WI-021 through WI-028 unless the binary boundary adds a new cause.
- Existing test assertions are inspected. Test-created temporary files/H2 rows are reported as isolated evidence only.
- Targeted frontend/backend tests, typecheck, ESLint, output-document Prettier, docs validation, and `git diff --check` are recorded exactly when run.
- Product/runtime/live DB/storage/provider/mail/browser state remains frozen; ZIP remains uninspected.

Constraints/Forbidden:

- No live upload/download/import/export, no actual user/private file, no browser file chooser, no external spreadsheet application, no live DB/storage mutation, and no direct cleanup/deletion.
- Automated tests may operate only in their existing isolated H2/mock/temp-directory boundaries. Do not add a fixture, alter test configuration, or preserve generated test files as product evidence.
- Inspect generated CSV/text only as inert bytes/text through existing tests or safe read-only parser evidence. Never evaluate spreadsheet formulas or macros.
- Do not inspect browser cookies/storage/session or ignored local secret/config files. Do not print PII, physical storage paths, tokens, full card data, provider payloads, or private document contents.
- Product code is frozen. No stage/commit/push/branch/reset.

[ACCEPTANCE CRITERIA]
Functional:

- [ ] `PUB-09`: Notice attachment absence/presence, safe visible name, ownership/public access contract, status/type/disposition/bytes, missing ID, and error/back behavior are reconciled.
- [ ] `MEM-07` and shared Track download: License ownership, active entitlement, content disposition, full original audio bytes, count/history/license effects, duplicate/retry behavior, and frontend Blob lifecycle are traced.
- [ ] `MEM-09`: Download History and count/Track IDs agree with successful download state; failed/denied download creates no false history/count/License success.
- [ ] `MEM-16/17`: Question attachment local validation, multipart shape, preservation on error, private owner/ADMIN authorization, safe filename/content type, delete cleanup, and no physical path exposure are traced.
- [ ] `ADM-06`: Company Certification private document metadata and bytes are ADMIN-only, path-safe, sanitized, non-public, and consistent with the reviewed application; no live document is accessed.
- [ ] `ADM-11`: Whitelist export scope, immutable batch rows, `userEmail` and required channel headers, deterministic replay, safe filename/type/bytes, spreadsheet-formula neutralization, later edit/delete independence, and audit/status semantics are traced.
- [ ] `ADM-10`: Settlement CSV import checks encoding/header/row/amount/date/provider identifiers, duplicate/idempotent behavior, malformed/partial failures, formula risks, transaction/audit result, reconciliation/ignore, and reload; no live import occurs.
- [ ] `ADM-12/13`: Notice attachment create/edit preserve/replace/remove semantics, one-submit ownership, storage journal/recovery, public detail/download projection, and failure rollback are traced.
- [ ] Frontend binary helpers revoke object URLs, preserve readable failure state, prevent duplicate action, and derive filenames only from sanitized response data or bounded fallback.
- [ ] Storage journal/recovery assertions distinguish staged/promoted/deleted/orphaned files and do not claim live filesystem cleanup.

Performance:

- [ ] Record size/count bounds, pagination/batch caps, streaming/Range behavior where present, and any full-buffer memory behavior as source/test facts; do not invent SLOs.
- [ ] Responsive authenticated file UI remains `BLOCKED/NOT RUN` unless safe existing evidence is available; static CSS is not substituted for live proof.

Quality:

- [ ] Cite exact matrix row, route/owner, frontend page/API/helper, controller/DTO/service/storage/repository/entity/test, and relevant design/security contract.
- [ ] Report status, type, disposition/filename, bytes, parser/row/header/value comparison, transaction/audit, and reload evidence independently.
- [ ] Record exact test commands, case counts, duration, warnings, and isolated H2/temp/mock boundary.
- [ ] Record pre/post branch/HEAD/status, frozen product result, ZIP preservation, and no live/browser file action.

[INPUT POINTERS]
Tier 0 (Constitution - required):

- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - required/inferred):

- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md
- docs/architecture/system-design.md

Tier 2 (Frontend/domain contracts):

- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/AGENTS.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/ui/screen-flow.md
- docs/design/usecase/user-question.md
- docs/design/usecase/whitelist.md
- docs/design/usecase/company-certification.md
- docs/design/usecase/user-license.md
- docs/design/usecase/user-notice.md
- docs/design/payment-operations-runbook.md
- docs/design/payment-refund-receipt-settlement-policy.md
- docs/payment/operator-guide.md

REQ/Context Docs:

- AGENTS.md
- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md:48-110,134-176,195-215,279-340,357-365,391-399,432-519
- deliverables/agent/WI-20260809-ATS-020-evidence-pack.md
- WI-021/023/024/025 findings and evidence only for owning Notice/Track/License/Download/Question/Notice-admin flows
- WI-026 findings and evidence only for Company Certification/Whitelist owning flows
- WI-028 findings and evidence only for Payment Operations/ADMIN owning flows
- .claude/config/workspace.json
- .claude/config/context-injection-rules.json

Files - frontend:

- frontend/src/pages/public/NoticeDetailPage.tsx
- frontend/src/pages/subscriber/LicenseListPage.tsx
- frontend/src/pages/subscriber/LicenseDetailPage.tsx
- frontend/src/pages/subscriber/DownloadHistoryPage.tsx and test
- frontend/src/pages/subscriber/QuestionCreatePage.tsx
- frontend/src/pages/subscriber/QuestionDetailPage.tsx
- frontend/src/pages/admin/CompanyCertManagePage.tsx and test
- frontend/src/pages/admin/WhitelistChannelManagePage.tsx and tests
- frontend/src/pages/admin/PaymentOperationsPage.tsx and test
- frontend/src/pages/admin/NoticeCreatePage.tsx
- frontend/src/pages/admin/NoticeEditPage.tsx
- shared Track/download controls in Track/Album/Playlist/Likes/Player pages only as needed for Blob/entitlement contract
- frontend/src/api/notices.ts
- frontend/src/api/licenses.ts
- frontend/src/api/downloads.ts
- frontend/src/api/questions.ts
- frontend/src/api/admin.ts
- frontend/src/api/adminWhitelistChannels.test.ts
- frontend/src/api/client.ts and any shared binary download helper discovered by call-site tracing

Files - backend/storage:

- src/main/java/com/atstudio/atstudio/controller/NoticeController.java
- src/main/java/com/atstudio/atstudio/controller/LicenseController.java
- src/main/java/com/atstudio/atstudio/controller/DownloadController.java
- src/main/java/com/atstudio/atstudio/controller/QuestionController.java
- src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java
- src/main/java/com/atstudio/atstudio/controller/AdminWhitelistChannelController.java
- src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java
- src/main/java/com/atstudio/atstudio/service/NoticeService.java
- src/main/java/com/atstudio/atstudio/service/LicenseService.java
- src/main/java/com/atstudio/atstudio/service/DownloadService.java
- src/main/java/com/atstudio/atstudio/service/QuestionService.java
- src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java
- src/main/java/com/atstudio/atstudio/service/AdminWhitelistChannelService.java
- src/main/java/com/atstudio/atstudio/service/AdminPaymentSettlementService.java
- src/main/java/com/atstudio/atstudio/service/storage/\*
- related attachment/document/export/settlement DTOs, entities, repositories, storage mutation records, schema columns/constraints, and validation/config classes reached by these paths

Files - automated tests:

- relevant frontend page/API/coverage tests for the named pages and binary helpers
- `NoticeControllerTest`, `LicenseControllerTest`, `DownloadServiceTest`, `DownloadConcurrencyContractTest`, `QuestionControllerTest`, `QuestionServiceTest`
- Company Certification controller/service/security/storage tests
- `AdminWhitelistChannelControllerTest`, `AdminWhitelistChannelServiceTest`, `WhitelistConcurrencyContractTest`, `adminWhitelistChannels.test.ts`
- `AdminPaymentSettlementServiceTest`, `AdminPaymentControllerTest`, `PaymentMysql*` tests if they establish import transaction/schema behavior
- `LocalStorageServiceTest`, `StorageCleanupServiceTest`, `StorageMutationJournalServiceTest`, `StorageMutationCoordinatorTest`, `StorageMutationRecoveryServiceTest`, `StorageMutationRecoveryVerificationTest`, `StorageReferenceCheckerBranchCoverageTest`, `StorageMutationContractTest`
- `NoticeServiceTest`, `NoticeServiceBranchCoverageTest`, `LicenseServiceTest`, and additional exact tests discovered by call-site tracing

Repro/Logs:

- Record branch, HEAD, and `git status --short` before/after without opening the intentional ZIP.
- Run bounded targeted frontend/backend tests with existing isolated fixtures. Use `--rerun-tasks` for Gradle where cache ambiguity matters and aggregate XML results read-only.
- Inspect inert generated test bytes only through safe existing assertions/parsers. Do not open spreadsheets externally or preserve test outputs.
- Run typecheck, targeted ESLint, output-document Prettier/check, docs validation, and `git diff --check` when applicable; record only executed results.
- Browser file actions are forbidden for WI-029; record browser as unchanged/restored from the prior neutral public state.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-029-summary.md:

- Sublane outcomes, confirmed findings, blocked live/browser/binary areas, parsed-file evidence, quality results, risks/approval points, and frozen-state record.

Agent-facing -> deliverables/agent/WI-20260809-ATS-029-evidence-pack.md:

- Binary evidence matrix, source/API/storage crosswalk, status/header/filename/byte/parser/domain/audit lanes, test commands/results, limitations, and rollback/no-live-mutation statement.

Findings -> deliverables/agent/WI-20260809-ATS-029-findings.md:

- Independent file/binary causes/contracts with severity, affected row/entry point, exact evidence, lane classification, impact, and bounded follow-up.

Handoff Packet -> deliverables/agent/WI-20260809-ATS-029-handoff.md:

- This packet only.

[TRACEABILITY REQUIREMENTS]

- A click/toast is never file evidence. For each operation, record UI/control, frontend request, server status/headers, filename/type/bytes/parser, durable domain/storage/audit state, and reload separately.
- Identify exact ownership/authorization and physical-path-leak boundaries without revealing private paths or contents.
- For CSV, record inert parser/headers/row count/escaped representative values and formula neutralization; never execute formulas.
- Distinguish test temporary-directory/H2 evidence from live storage/DB and mark unavailable lanes blocked.
- No product rollback is expected. If any live/product/config/index mutation occurs, stop and report it rather than reverting without approval.
