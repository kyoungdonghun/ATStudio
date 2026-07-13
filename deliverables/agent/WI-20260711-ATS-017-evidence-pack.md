# Evidence Pack: WI-20260711-ATS-017

## Summary

- Independently adjudicated the current backend, transaction, API, database, and operational P0/P1 candidates into 3 confirmed P0 findings, 11 confirmed P1 findings, one conditional P1 migration family, and explicit conditional/down-ranked/transferred claims.

## Scope / DoD Check

- [x] Reassessed every P0/P1 backend, DB, and API candidate carried by WI-002, WI-005, WI-006, WI-007, WI-008, and WI-015.
- [x] Reconciled current source, executable MySQL DDL/manual patches, API/design contracts, and prior test/build evidence.
- [x] Separated fresh-database behavior from retained-database migration risk.
- [x] Separated passing regression/build evidence from paths not proven by those checks.
- [x] Identified material unbounded batch/query risks without elevating all unbounded reads to P1.
- [x] Produced an ordered remediation plan with current pointers and ownership.
- [x] Changed only the two WI-017 output files.

## Baseline and Constraints

| Field | Value |
|---|---|
| Workspace | `C:\Users\jm991\Desktop\project\ATStudio` |
| Branch | `dev/kyoung` |
| HEAD | `27d22446e5d21324dadcfcb322dbe51704dfe914` |
| Review mode | Static inspection only; no Gradle/npm test, DB, HTTP, SMTP, filesystem-provider, or payment-provider execution |
| Shared worktree | Dirty before WI-017; unrelated tracked/untracked files were preserved |
| Relevant source status | No Git status entries for the reviewed backend, schema, configuration, and API-spec paths |
| Allowed writes | `deliverables/user/WI-20260711-ATS-017-summary.md`; `deliverables/agent/WI-20260711-ATS-017-evidence-pack.md` |

## Reference Documents

| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Platform integrity, payment traceability, transparency, and non-destructive review |
| 0 | `docs/standards/development-standards.md` | Transaction, JPA, exception, API, and test evidence standards |
| 0 | `docs/standards/documentation-standards.md` | Deliverable and documentation rules |
| 0 | `docs/standards/glossary.md` | Canonical domain and WI terminology |
| 1 | `docs/policies/quality-gates.md` | Review traceability and regression-evidence limits |
| 2 | `docs/design/api-spec.md` | Current API contract |
| 2 | `src/main/resources/schema.sql` | Fresh MySQL manual schema |
| REQ | `deliverables/user/REQ-20260711-ATS-001.md` | Approved audit scope |
| WI | `deliverables/agent/WI-20260711-ATS-017-handoff.md` | Scope, dependencies, DoD, constraints, and output contract |
| Prior evidence | `deliverables/agent/WI-20260711-ATS-{002,005,006,007,008,009,011,013,015}-evidence-pack.md` | Candidates and independent verification inputs |

## Adjudication Scale

- **Confirmed:** current repository source/DDL establishes the control path and failure or bypass without requiring unknown deployment state.
- **Conditional:** the hazardous path exists, but retained DB age/data or production ingress/topology is required for the stated impact.
- **Down-ranked:** the underlying mismatch exists, but the claimed P0/P1 impact is not established.
- **Rejected/transferred:** current backend evidence contradicts the backend claim or establishes ownership in another layer.

## Confirmed Findings

| ID | Severity | Decision and current pointers | Owner |
|---|---:|---|---|
| ATS017-C01 | P0 | Public detail is allowed (`SecurityConfig.java:66-69`), `TrackResponse` exposes `audioFile` (`TrackResponse.java:10-39`), and `/uploads/**` maps the storage root and falls through to permit-all (`WebConfig.java:20-25`; `SecurityConfig.java:129-132`). This bypasses the entitlement and ledger path (`DownloadService.java:39-86`). | Backend security / Track |
| ATS017-C02 | P0 | Withdrawal deletes selected children and only marks the user deleted (`UserService.java:104-123`; `User.java:81-84`). Due agreement lookup has no deleted-user predicate and renewal can charge an active agreement (`BillingAgreementRepository.java:26-29`; `RecurringRenewalService.java:89-159`). | Account / Billing |
| ATS017-C03 | P0 | Verification/reset URLs contain live tokens (`EmailService.java:46-65,88-108`); password reset consumes the token (`EmailService.java:141-159`); send failure logs recipient, subject, and full HTML body and is swallowed (`EmailService.java:163-181`). Password login defaults enabled (`application.yml:81-84`). | Backend security / Mail |
| ATS017-C04 | P1 | Java emits settlement audit actions/target (`PaymentOperationAuditAction.java:3-19`; `PaymentOperationAuditTargetType.java:3-8`; `AdminPaymentSettlementService.java:78-108,213-253`), while fresh DDL omits all settlement values (`schema.sql:797-815`). MySQL settlement operations can fail at audit flush or persist invalid audit evidence. | DB / Payment |
| ATS017-C05 | P1 | Initial confirm uses default rollback (`BillingAgreementApplicationService.java:161-178`), marks charge failure and throws in the same transaction (`BillingAgreementApplicationService.java:212-226`). The existing focused test is Mockito-only (`BillingAgreementApplicationServiceTest.java:40-45,310-349`). | Payment transaction |
| ATS017-C06 | P1 | Confirm/renewal repositories have no command lock (`PaymentOrderRepository.java:17-33`; `BillingAgreementRepository.java:16-35`), payment aggregates have no `@Version`, upgrade creates a fresh order/idempotency key per request (`UserSubscriptionService.java:339-373`), and `subscription_payments.payment_order_id` is nullable/non-unique (`schema.sql:518-538`). | Payment / DB |
| ATS017-C07 | P1 | Renewal searches READY/IN_PROGRESS/FAILED/DONE and reuses any non-DONE order regardless of period because of `status != DONE || samePeriod` (`RecurringRenewalService.java:48-52,167-197`). Old grace dates are then used before charging (`RecurringRenewalService.java:135-159`). | Recurring billing |
| ATS017-C08 | P1 | Due agreements are an unpaged `List` and the full loop plus provider calls run in one transaction (`BillingAgreementRepository.java:26-29`; `RecurringRenewalService.java:84-105,147-159`). One later runtime/flush failure can separate prior provider success from rolled-back local finalization. | Recurring billing / Operations |
| ATS017-C09 | P1 | Refund creation reads aggregate reserved amount and inserts without locking the source payment (`AdminPaymentRefundService.java:89-111,248-262`). Only later execution locks an individual refund (`PaymentRefundRepository.java:45-57`). | Refund / DB |
| ATS017-C10 | P1 | Track/playlist/album/question/notice services store before DB completion, delete old files before commit, or omit old-file cleanup (`TrackService.java:60-89,148-179`; `PlaylistService.java:39-64,178-193`; `AlbumService.java:42-61,113-126`; `QuestionService.java:49-66,173-188,207-223`; `NoticeService.java:43-59,92-132,159-174`). Storage deletion failure is log-only (`LocalStorageService.java:78-87`). | Storage / Domain services |
| ATS017-C11 | P1 | User-controlled channel fields enter admin CSV (`WhitelistChannelRequest.java:6-10`; `AdminWhitelistChannelService.java:127-150`), while `csv()` only quotes and escapes quotes (`AdminWhitelistChannelService.java:192-198`). | Whitelist / Admin API |
| ATS017-C12 | P1 | Certification validation checks only nonempty/count/extension/size and persists the client content type (`CompanyCertificationService.java:233-276`). Admin download returns that media type as an attachment (`CompanyCertificationController.java:98-112,130-138`). | Certification / File security |
| ATS017-C13 | P1 | Subscriber playlist thumbnails are stored without extension, MIME, signature, or image-decode checks (`PlaylistService.java:39-64,178-193`); storage preserves the sanitized original extension (`LocalStorageService.java:50-70`); returned paths are publicly served (`PlaylistResponse.java:9-25`; `SecurityConfig.java:129-132`). | Playlist / File security |
| ATS017-C14 | P1 | No server logout endpoint exists (`AuthController.java:21-74`); password change/reset update only the password (`UserService.java:148-159`; `EmailService.java:141-159`); a matching stored refresh-token hash remains renewable (`AuthService.java:75-110`). | Authentication |

## Conditional Findings

| ID | Severity | Condition and current evidence | Required verification / Owner |
|---|---:|---|---|
| ATS017-X01 | P1 conditional | Runtime defaults to schema validation (`application.yml:16-20`). The latest payment patch explicitly requires an earlier baseline (`20260615_align_payment_whitelist_schema.sql:18-23`), but repository SQL inventory is only fresh schema, seed, and two latest manual patches. The company-document patch creates only the child table and has no legacy `document_path` backfill (`20260618_company_certification_documents.sql:1-22`). Fresh schema already contains the child table; this is an existing-DB risk, separate from C04's fresh-schema defect. | Inventory copied DB schema/history and legacy rows; define ordered migrations and backfill. DB/release + Payment/Certification |
| ATS017-X02 | P0 escalation only; C13 remains P1 | Same-origin credential impact requires production SPA and uploads to share an authenticated origin and browser-executable delivery. Repository evidence establishes only development proxying, not production ingress. | Inspect production ingress/headers without mutating it. Platform security |
| ATS017-X03 | P2 conditional | Registration and exact availability checks are public (`SecurityConfig.java:55,62-65`; `UtilController.java:36-57`), while the in-app limiter covers only login/forgot/reset/refresh (`AuthRateLimitFilter.java:58-76`). Upstream WAF/rate limits and measured exhaustion were not inspected. | Verify edge controls and abuse telemetry. Platform/Auth |

## Rejected or Down-ranked Claims

| ID | Prior claim | Final adjudication and basis |
|---|---|---|
| ATS017-D01 | Whitelist removal cannot complete, P1 | **Down-ranked to P2 contract ambiguity.** `CANCELLED` is admin-mutable (`AdminWhitelistChannelService.java:40-47,80-101`) and is not counted against the plan (`WhitelistChannel.java:124-130`), so a non-counting terminal path exists. The docs do not define whether `CANCELLED` means external removal completed (`whitelist.md:120-132`; `api-spec.md:3042-3059`). |
| ATS017-D02 | ADMIN member-checkout access, P1 | **Down-ranked to P2 role/domain correctness.** Payment endpoints are authenticated and the prepare service validates user type but not role (`PaymentController.java:72-94`; `BillingAgreementApplicationService.java:108-145`). This can create inappropriate admin-owned billing state, but no cross-user authorization bypass or P1 financial impact was established. |
| ATS017-D03 | Social callback failure is a backend/API P1 | **Rejected as a backend defect; transferred to frontend.** Backend returns fresh access/refresh tokens (`AuthService.java:60-72`). The frontend calls `fetchMe()` before storing or passing the new token (`frontend/src/pages/auth/SocialLoginPage.tsx:42-59`). |
| ATS017-D04 | Public original stream fallback itself is the paid-download bypass | **Rejected.** The confirmed bypass is C01's independently addressable master path. The stream fallback is a separate documented product policy and should not be used as C01's proof. |
| ATS017-D05 | All unbounded queries/lists are P1 | **Rejected.** Renewal's unbounded external-call transaction remains P1 (C08). Reconciliation's fixed latest-100 window plus unpaged agreement scan (`PaymentReconciliationService.java:56-91,107-194`) and whitelist export/list growth (`WhitelistChannelRepository.java:47-48`; `AdminWhitelistChannelService.java:55-76,104-157`) are material P2 production-scale/observability risks until measured. |

## Fresh DB vs Existing DB

| Scenario | Verified repository behavior | Verdict |
|---|---|---|
| Fresh MySQL created from `schema.sql` | Current tables, including `company_certification_documents`, are present, but settlement audit ENUM values are absent (`schema.sql:162-177,797-815`). Header/footer metadata also remains v12/38 while content has 39 tables (`schema.sql:2-4,1014-1017`). | C04 is a confirmed fresh-DB defect; metadata drift is P2. |
| H2 test database | Test profile disables SQL init and uses Hibernate `create-drop` (`src/test/resources/application.yml:1-7`). | Cannot validate MySQL ENUMs or manual migrations. |
| Retained MySQL database | Application validates rather than migrates; latest manual patches assume unknown earlier changes. | ATS017-X01 remains conditional and deployment-blocking until the actual baseline is inventoried. |

## Performance and Batch Risk

| Priority | Risk | Basis | Disposition |
|---:|---|---|---|
| P1 | Renewal loads every due agreement and performs external calls in one transaction | `RecurringRenewalService.java:84-105,147-159`; `BillingAgreementRepository.java:26-29` | Page/claim and isolate each agreement transaction before scale-out. |
| P2 | Reconciliation only examines the latest 100 orders, scans all ACTIVE agreements, and performs per-agreement subscription lookup | `PaymentReconciliationService.java:56-91,107-194` | Add cursor/time windows, paging/batch joins, and a coverage watermark. |
| P2 | Whitelist export loads a full status set, builds rows/items in memory, and looks up subscriptions per row | `AdminWhitelistChannelService.java:104-157`; `WhitelistChannelRepository.java:47-48` | Atomically claim, page/chunk, and make batches re-downloadable. |
| P2 | Admin whitelist page accepts any positive size and does an active-subscription lookup per result | `AdminWhitelistChannelService.java:55-76,160-162` | Clamp page size and batch-fetch entitlement data. |

## Prior Test and Build Evidence

No test or build command was run by WI-017.

| Evidence | Result | What it proves | What it does not prove |
|---|---|---|---|
| WI-009 `\.\gradlew.bat test --rerun-tasks --console=plain` | 745 passed; 0 failed/errors/skipped at the same HEAD | Fresh execution of the configured backend regression suite | MySQL DDL/migrations, provider behavior, transaction persistence after exceptions, row locks, concurrent workers, resource-handler access |
| WI-011 `\.\gradlew.bat compileJava` | PASS, task `UP-TO-DATE` | Gradle accepted current compile inputs/artifacts | Clean recompilation or runtime behavior |
| WI-013 `\.\gradlew.bat build` | PASS, all eight backend tasks `UP-TO-DATE` | Current incremental build state | Clean build, deployment, DB startup, external integration |
| WI-015 coverage inspection | Not measurable | No configured JaCoCo or frontend coverage provider/report | Any numeric coverage percentage; unknown must not be reported as 0% |

Focused tests remain absent for C01-C14's resource-handler, real transaction, MySQL ENUM, concurrency/locking, file rollback, upload-content, CSV formula, and refresh-revocation paths. Existing payment, settlement, refund, whitelist, certification, mail, and auth focused suites are predominantly Mockito tests; their passing results do not establish database commit/rollback or concurrent-worker behavior.

## Ordered Remediation Plan

1. **Release blockers:** close C01-C03; add resource-handler and mail log-capture tests.
2. **Database gate:** fix C04 in fresh DDL and an ordered patch; inventory the retained DB and resolve X01 before deployment.
3. **Payment command integrity:** address C06 with claim/lock/version and unique order/period/finalization invariants.
4. **Renewal correctness:** fix C07 and split/page/claim C08 into per-agreement transactions.
5. **Failure and refund durability:** persist C05 outcomes across API errors and serialize C09 reservations on the source payment.
6. **File lifecycle:** implement one transaction-aware coordinator for C10, then add durable cleanup retry/reconciliation.
7. **Untrusted files/exports:** enforce C12 quarantine/signature policy, C13 image decode/re-encode and isolated origin, and C11 CSV formula neutralization.
8. **Session lifecycle:** implement server logout/revocation and credential-change invalidation for C14.
9. **Verification gate:** add MySQL/Testcontainers schema/migration tests, real Spring transaction tests, two-worker concurrency tests, and security resource-handler tests; then rerun the full suite and clean build.

## Commands and Outputs

- Static repository inspection used `Get-Content`, `Select-String`, `rg`, `Get-ChildItem`, `git status --short`, `git rev-parse`, and `git branch --show-current`.
- `git status --short` showed pre-existing unrelated `docs/client/` changes and untracked WI/log/output artifacts.
- Scoped status for reviewed source/schema/API paths was clean before the two WI-017 outputs were created.
- No destructive command, restore, checkout, DB command, provider call, mail call, or source/schema mutation was performed.

## Files Changed

- `deliverables/user/WI-20260711-ATS-017-summary.md` - user-facing adjudication and remediation order.
- `deliverables/agent/WI-20260711-ATS-017-evidence-pack.md` - current pointers, classification, inherited verification evidence, risks, and rollback.

## Risks / Rollback

- Static analysis establishes code/DDL paths but not live DB contents, SQL mode, production ingress, proxy controls, scheduler replica count, provider behavior, or SMTP/log access policy.
- The 745-test pass is a regression baseline, not evidence that the retained findings are covered.
- Coverage remains unknown because instrumentation is absent.
- The shared worktree can change after this snapshot; source pointers were current at HEAD `27d22446e5d21324dadcfcb322dbe51704dfe914`.
- Rollback, only if explicitly requested: remove the two WI-017 output files. Do not revert any unrelated shared-worktree change.

## Follow-up / WI Chain

- WI-017 blocks WI-020. WI-020 should consume the confirmed/conditional/down-ranked tables and preserve the distinction between passing regressions and unverified production paths.
