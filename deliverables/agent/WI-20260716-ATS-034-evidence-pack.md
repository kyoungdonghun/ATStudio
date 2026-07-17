# WI-20260716-ATS-034 Evidence Pack

## 1. Metadata

| Field | Value |
|---|---|
| WI | `WI-20260716-ATS-034` |
| REQ | `REQ-20260716-ATS-004` (approved) |
| Agent role | ATStudio Code Reviewer (`cr`) |
| Audit mode | Static and read-only |
| Branch observed | `codex/p1-acceptance-hardening` |
| HEAD observed | `a96d2e0c5d249723bbf449b6834299a04cf2ad30` |
| Product/runtime mutation | None |
| Deliverables permitted | This Evidence Pack and the paired user summary only |

## 2. Scope and Method

The audit covered:

- `src/main/java`
- `src/main/resources`
- `src/test/java`
- `build.gradle`
- static API consumers under `frontend/src`
- the approved REQ, WI handoff, Tier 0 standards, security/versioning/quality policies, API/DB/remediation designs, and payment system overview

The method combined endpoint enumeration, symbol-reference searches, file-name searches, profile/property reachability checks, test-reference inspection, frontend import/path searches, and design-policy comparison. A controller's low Java reference count was not treated as proof of dead code because Spring invokes controllers reflectively.

No server, test suite, database, branch, worktree, formatter, build, or migration command was run.

## 3. Classification Rules

| Classification | Rule used in this audit |
|---|---|
| KEEP | Active product behavior, security boundary, or operational integrity control |
| REMOVE | Replacement path and in-repository call absence are evidenced; no safety role found |
| REPLACE | Current surface has active or migration value and requires a safer successor before removal |
| ARCHIVE | Historical evidence should leave active paths but remain in an approved archive |
| REVIEW | External caller, retained-data, profile, or product-policy evidence is insufficient for a safe decision |

Severity expresses the consequence of an incorrect disposition, not merely code complexity. Confidence expresses confidence in the static classification.

## 4. Inventory Summary

| Classification | Count |
|---|---:|
| KEEP | 2 |
| REMOVE | 7 |
| REPLACE | 2 |
| REVIEW | 7 |
| ARCHIVE | 0 |
| Total | 18 |

## 5. Evidence-Backed Inventory

### ATS-BE-001 - Legacy one-time payment surface

- **Classification:** REVIEW, with an atomic removal recommendation after the API-removal gate is satisfied
- **Severity / confidence:** Medium / High
- **Paths and symbols:**
  - `src/main/java/com/atstudio/atstudio/controller/PaymentController.java:38-69`
  - `src/main/java/com/atstudio/atstudio/service/PaymentApplicationService.java:91-337`
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/PaymentProvider.java`
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/MockPaymentProvider.java`
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/TossPaymentProvider.java`
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/PaymentProviderPrepareResult.java`
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/PaymentProviderConfirmResult.java`
  - legacy DTOs `PaymentCancelRequest`, `PaymentCheckoutResponse`, `PaymentConfirmRequest`, `PaymentConfirmResponse`, `PaymentOrderResponse`, `PaymentPrepareRequest`, and `PaymentPrepareResponse`
- **Reachability evidence:** The controller still exposes `/api/payments/subscriptions/prepare`, `/api/payments/confirm`, and `/api/payments/cancel`. `prepareSubscriptionPayment` always rejects, and `confirmPayment` rejects subscription/upgrade purposes. The active SPA payment client uses only `/payments/billing-agreements/*` (`frontend/src/api/payments.ts:74-99`). Searches found no SPA caller for the legacy paths.
- **Documentation evidence:** `docs/design/api-spec.md:1325,1354,1382,1404-1406,1588-1590` identifies the subscription routes as blocked/legacy and requires supported-caller absence plus telemetry/observation or agreed evidence before contract removal.
- **Safety role:** None for current recurring subscription checkout. This package is distinct from `service/payment/provider/recurring/**`, which is active.
- **Deletion impact:** Atomic removal must include controller methods, service, provider interface/implementations/results, DTOs, tests, configuration branches, and docs. Partial removal risks bean wiring or contract drift.
- **False-positive risk:** A stale external client may still call the public legacy endpoints. Static repository search cannot disprove this.
- **Verification method:** API traffic/telemetry or an explicit approved telemetry waiver; contract tests proving recurring-only checkout; negative frontend and external-client inventory.

### ATS-BE-002 - Blocked direct subscription creation endpoint

- **Classification:** REVIEW
- **Severity / confidence:** Medium / High
- **Evidence:** `UserSubscriptionController.java:24-34` exposes `POST /api/user-subscriptions`; `UserSubscriptionService.java:81-87` always throws `SUBSCRIPTION_CHECKOUT_REQUIRED`. `UserSubscriptionRequest` is referenced only by that path and its service test. The active frontend has no POST wrapper for this route (`frontend/src/api/userSubscriptions.ts`).
- **Documentation evidence:** `docs/design/api-spec.md:1325` and the lifecycle gate near `:1590` describe the direct endpoint as blocked and subject to the same removal conditions.
- **Safety role:** The rejection protects the recurring checkout requirement while the endpoint remains published.
- **Deletion impact:** Remove the endpoint, blocked service method, DTO, test, and docs together only after the external-caller gate.
- **False-positive risk:** Unknown external callers may rely on the current explicit error contract.
- **Verification:** Traffic evidence or explicit waiver, OpenAPI diff review, and negative external-client inventory.

### ATS-BE-003 - Server play-history compatibility stack

- **Classification:** REMOVE
- **Severity / confidence:** Low / High
- **Paths:** `PlayHistoryController`, `PlayHistoryService`, `PlayHistoryRepository`, `PlayHistory`, `dto/playhistory/**`, related tests, and cleanup references in `UserService`/`TrackService`.
- **Reachability evidence:** `docs/design/api-spec.md:894` states the active SPA persists play history in localStorage and does not synchronize with the server. `frontend/src/stores/playerStore.ts:61-100,259` implements that local persistence. `frontend/src/api/playHistory.ts` has no import/caller outside its own file.
- **Safety role:** None found for current user playback or authorization.
- **Deletion impact:** Coordinate backend code/test removal with the `play_histories` table decision in the DB WI.
- **False-positive risk:** Product may intend future cross-device history despite current documentation.
- **Verification:** Product-policy confirmation, frontend import/path negative search, backend test update, schema validation after the approved DB change.

### ATS-BE-004 - Legacy download-queue backend

- **Classification:** REMOVE
- **Severity / confidence:** Low / High
- **Paths:** `DownloadQueueController`, `DownloadQueueService`, `DownloadQueueRepository`, `DownloadQueue`, `DownloadQueueId`, `dto/downloadqueue/DownloadQueueResponse`, related tests, and cleanup references in `UserService`/`TrackService`.
- **Reachability evidence:** `frontend/src/api/downloadQueue.ts:3-30` marks the client as deprecated and retained for backend compatibility; it has no importer. The live `/download-queue` route renders download history, not a queue. `DownloadController.java:17-23` distinguishes current download behavior from the legacy queue.
- **Safety role:** None found for current download authorization, limit accounting, or history.
- **Deletion impact:** Do not remove the live frontend `/download-queue` page. Remove only the obsolete queue model/API bundle and coordinate the table change with the DB WI.
- **False-positive risk:** A repository-external client may call the queue API.
- **Verification:** External-client inventory/traffic review, controller contract removal tests, and DB schema validation.

### ATS-BE-005 - Deprecated four-argument upgrade finalizer

- **Classification:** REMOVE
- **Severity / confidence:** Low / High
- **Evidence:** `PaymentCommandTransactionService.java:611-619` marks the four-argument `finalizeUpgrade(..., BillingCycle ignoredCallerTargetBillingCycle)` overload deprecated. Current main callers in `UserSubscriptionService.java:371-374,429-432` and `PaymentReconciliationService.java:756-759`, plus tests, use the three-argument finalizer.
- **Safety role:** None. It delegates to the active finalizer and ignores its additional argument.
- **Deletion impact:** Remove only the deprecated overload after a full symbol search. Keep the three-argument finalizer and all claim/fence/state checks.
- **False-positive risk:** Reflection or an out-of-repository binary consumer is possible but unlikely for an internal service method.
- **Verification:** Compile/test after symbol removal and repeat the symbol search.

### ATS-BE-006 - Billing-key V1 decryption compatibility

- **Classification:** REPLACE
- **Severity / confidence:** High / High
- **Evidence:** `BillingKeyCrypto.java:25,67-80,90-99` retains a V1 envelope and legacy encryption-secret requirement. `BillingKeyCrypto.java:26,35-56,70-72,101-112,172-205` implements the current V2 key-ID/key-ring envelope. `application.yml:125` and `PaymentProperties.Billing.encryptionSecret` retain the legacy property.
- **Safety role:** V1 decryption prevents existing billing keys from becoming unreadable. V2 key-ring selection, active key ID, authenticated encryption, and fingerprint checks are current integrity controls.
- **Replacement:** Prove that the official V1 database contains no retained `v1:` ciphertext, then remove only the V1 branch/property and keep V2 key-ring behavior.
- **Deletion impact:** Premature removal makes any retained V1 billing agreement unusable.
- **False-positive risk:** A local, acceptance, or manually copied DB may still hold V1 ciphertext even if production preservation is not required.
- **Verification:** Read-only data inventory before reset or proof of a newly initialized V1 DB; crypto tests covering V2-only startup and decrypt.

### ATS-BE-007 - `Track.previewFile`

- **Classification:** REMOVE
- **Severity / confidence:** Low / High
- **Evidence:** `Track.java:41` defines the field; `schema.sql:217` defines the legacy column; `StorageReferenceChecker.java:23` is the only active source reference beyond the entity. `TrackServiceTest.java:288-291` asserts that preview storage is ignored and the original track is used. `docs/design/db-schema.md:217,444` marks the preview path as superseded.
- **Safety role:** None under the current full-track listening policy.
- **Deletion impact:** Entity, schema, storage-reference check, test fixtures, and DB documentation must change together.
- **False-positive risk:** Retained rows may contain preview file paths requiring a storage-cleanup decision before column removal.
- **Verification:** DB/storage inventory in the DB WI, full-play tests, storage reference tests, and schema validation.

### ATS-BE-008 - Legacy whitelist export snapshots

- **Classification:** REMOVE
- **Severity / confidence:** Low / High
- **Evidence:** `WhitelistExportItem.java:41-48` retains `userIdSnapshot` and `userNicknameSnapshot`; the corresponding `schema.sql:308,310` columns have no other active code references. `docs/design/db-schema.md:914,916` identifies them as legacy and says new rows leave them null.
- **Safety role:** None found in current export traceability, which uses current export item/user data.
- **Deletion impact:** Coordinate entity and schema removal; confirm no required historical export evidence depends on these columns.
- **False-positive risk:** Existing audit exports may rely on snapshot semantics outside current code.
- **Verification:** Retained-data decision, export regression tests, and schema validation.

### ATS-BE-009 - Inert Thymeleaf configuration

- **Classification:** REMOVE
- **Severity / confidence:** Low / High
- **Evidence:** `application.yml:26-30` contains Thymeleaf settings. `build.gradle` has no Thymeleaf starter, no template resources were found, and no Thymeleaf controller/view code was found. `SpaForwardController.java:6-12` forwards SPA routes to `/index.html` and is not Thymeleaf SSR.
- **Safety role:** None. The SPA forward controller is active and must remain.
- **Deletion impact:** Configuration-only cleanup; do not remove SPA forwarding.
- **False-positive risk:** A runtime-added dependency is outside repository control.
- **Verification:** Dependency report, application startup, and SPA route smoke tests after approved removal.

### ATS-BE-010 - Stale security matcher for `PUT /api/settings/*`

- **Classification:** REMOVE
- **Severity / confidence:** Low / High
- **Evidence:** `SecurityConfig.java:86` protects `PUT /api/settings/*`, but the actual admin endpoint is `/api/admin/settings/{key}` and the frontend uses `/admin/settings`. `SecurityConfig.java:139` already protects `/api/admin/**`. No backend PUT endpoint under `/api/settings/*` was found.
- **Safety role:** None for the real admin settings path. The `/api/admin/**` matcher remains essential.
- **Deletion impact:** Remove only the stale matcher. Keep public GET settings behavior and the admin catch-all.
- **False-positive risk:** An external filter/router could rewrite the old path, but no in-repository evidence exists.
- **Verification:** Security matcher tests for anonymous/user/admin requests to the real settings endpoints.

### ATS-BE-011 - Dormant subscription-status utility endpoint

- **Classification:** REVIEW
- **Severity / confidence:** Low / Medium
- **Evidence:** `UtilController.java:60-65` and `UtilService.java:107-126` implement `GET /api/utils/subscription-status`. No static SPA consumer was found.
- **Safety role:** Read-only convenience response; no integrity role found.
- **Deletion impact:** Unknown external callers could break.
- **False-positive risk:** Public or manually operated consumers may call it without a checked-in client.
- **Verification:** Traffic inventory and API owner confirmation before removal.

### ATS-BE-012 - Dormant user-type utility endpoint

- **Classification:** REVIEW
- **Severity / confidence:** Low / Medium
- **Evidence:** `UtilController.java:76-81` and `UtilService.java:155-160` implement `GET /api/utils/user-type`. No API consumer was found in the SPA.
- **Safety role:** Read-only convenience response; no integrity role found.
- **Deletion impact / false-positive risk:** Same external-consumer uncertainty as ATS-BE-011.
- **Verification:** Traffic inventory and API owner confirmation.

### ATS-BE-013 - Unused admin subscription-detail wrapper/endpoint

- **Classification:** REVIEW
- **Severity / confidence:** Low / Medium
- **Evidence:** `UserSubscriptionController.java:60-69` exposes `GET /api/user-subscriptions/{id}`. `frontend/src/api/userSubscriptions.ts:102-105` defines `fetchAdminUserSubscriptionDetail`, but no caller of the wrapper was found.
- **Safety role:** Read-only admin detail.
- **Deletion impact:** May affect a future/admin deep-link or external admin client.
- **False-positive risk:** Dynamic calls outside checked-in frontend.
- **Verification:** Admin workflow inventory and traffic evidence.

### ATS-BE-014 - Direct admin subscription mutation

- **Classification:** REPLACE
- **Severity / confidence:** Medium / High
- **Evidence:** `UserSubscriptionController.java:87-107` and `UserSubscriptionService.java:212-227` directly update/cancel subscriptions. `UserSubscriptionManagePage.tsx:117,133` actively calls them. The payment operations domain separately provides audited entitlement-correction endpoints in `AdminPaymentController.java:199-249`.
- **Safety role:** Current admin recovery/manual-control capability. It is not dead code.
- **Concern:** Direct mutation can diverge entitlement state from billing/payment evidence if used outside a defined procedure.
- **Replacement:** Either define it explicitly as an audited manual control or replace it with a general audited subscription-adjustment workflow. Do not delete before the operator workflow is available.
- **False-positive risk:** The current operations process may intentionally rely on this emergency control.
- **Verification:** Product/operations decision, authorization tests, audit-event assertions, and end-to-end billing-entitlement state tests.

### ATS-BE-015 - Acceptance environment security boundary

- **Classification:** KEEP
- **Severity / confidence:** High / High
- **Evidence:** `AcceptanceStartupGuard.java:49-91` refuses production-like startup and validates required secrets; `AcceptancePublicUrls`, `AcceptanceHostFilter`, `CorsConfig.java:38-40`, `SecurityConfig.java:146`, and `application-acceptance.yml` restrict public origin/host behavior. `scripts/acceptance/**` consumes this contract.
- **Safety role:** Prevents acceptance fixtures and permissive public URLs from leaking into production-like environments. It is a guardrail, not an acceptance bypass.
- **Deletion impact:** Removing it can allow unsafe profile/secret/origin combinations or break reproducible client acceptance.
- **False-positive risk:** None material; individual fields may evolve, but the boundary must remain.
- **Verification:** Guard unit tests and acceptance/prod-profile negative startup tests in a later execution WI.

### ATS-BE-016 - Acceptance fixture bootstrap runners

- **Classification:** REVIEW
- **Severity / confidence:** Medium / High
- **Evidence:** `TestUserBootstrapRunner` is property-gated and protected by the startup guard; `AcceptanceSubscriptionPlanBootstrapRunner` is acceptance-profile/property gated. Both intentionally mutate only the acceptance database to create fixtures and are covered by tests. `scripts/demo/seed-client-demo.mjs` depends on the QA account lifecycle.
- **Safety role:** Reproducible client-acceptance fixture provisioning.
- **Concern:** DB-mutating fixture provisioning remains inside the application artifact.
- **Disposition rationale:** Keep until an external provisioning replacement exists; then consider removing the runners while preserving startup/host/origin/secret guards.
- **False-positive risk:** Immediate removal would break the current acceptance workflow.
- **Verification:** Acceptance provisioning replacement, clean-DB setup test, and explicit production refusal test.

### ATS-BE-017 - Payment, subscription, and storage integrity controls

- **Classification:** KEEP
- **Severity / confidence:** High / High
- **Evidence:**
  - Idempotency and immutable attempt identity: `PaymentOrder.java:46-50,74-75,121,168-184`
  - Claim/finalize/provider-success transactions: `PaymentCommandTransactionService.java:72-161,198-337`
  - Scheduled reconciliation: `PaymentReconciliationService.java:81-85` and incident persistence in `PaymentReconciliationIncidentService`
  - Operation audit persistence: `PaymentOperationAuditLogService.java:23-143`
  - Pessimistic locks: `PaymentOrderRepository.java:48,52,56`, `PaymentRefundRepository.java:47`, `PaymentEntitlementCorrectionRepository.java:50`, `BillingAgreementRepository.java:47,74,79`, `UserSubscriptionRepository.java:32,52,56,60`
  - Refund and billing-cleanup leases/fences: `PaymentRefundTransactionService.java:34,51-105,296-298`; `BillingAgreementCleanupTransactionService.java:32` and its claim/fence checks
  - State machines: `PaymentOrder.java:160+`, `BillingAgreement.java:118+`, `PaymentRefund`, and `UserSubscription`
  - Optimistic locks: `CompanyCertification.java:26-28`, `WhitelistChannel.java:22-24`
  - Storage recovery: `StorageMutationRecoveryService.java:40`
- **Safety role:** Prevents duplicate provider execution, stale worker commits, lost updates, unrecorded operator changes, local/provider drift, and orphaned storage mutation.
- **Deletion impact:** Financial inconsistency, duplicate actions, or concurrency corruption.
- **False-positive risk:** None material. These controls may be refactored only with equivalent invariants and independent concurrency/failure testing.
- **Verification:** Idempotency, concurrency, lease-expiry, reconciliation, audit, state-transition, optimistic-lock, and recovery tests. These tests were not run in this read-only WI.

### ATS-BE-018 - Provider enum values without a single current ownership model

- **Classification:** REVIEW
- **Severity / confidence:** Low / Medium
- **Evidence:** `PaymentProviderType` retains values associated with the legacy one-time provider path (`MOCK`, `TOSS`) and a not-yet-implemented future provider (`KAKAOPAY`), while the active recurring/refund/lookup adapters are provider-neutral interfaces with Toss implementations.
- **Safety role:** Enum values may preserve persisted provider identity or a deliberate multi-PG roadmap.
- **Deletion impact:** Removing persisted enum values can break schema/data deserialization; keeping speculative values can falsely imply supported providers.
- **False-positive risk:** The DB or roadmap may intentionally reserve these values.
- **Verification:** DB data inventory, DB-WI decision, supported-provider matrix, and architecture approval.

## 6. Operational Integrity Protection Matrix

| Control | Disposition | Protection requirement |
|---|---|---|
| Payment idempotency / unique command identity | KEEP | Do not weaken unique keys or immutable attempt claims |
| Provider-success local finalization | KEEP | Preserve claim/fence and replay-safe state transition |
| Reconciliation and incidents | KEEP | Preserve scheduled comparison, durable incidents, and operator visibility |
| Payment operation audit | KEEP | Preserve append-only evidence for sensitive operations |
| Refund and billing cleanup leases | KEEP | Preserve ownership token, staleness, and finalize fencing |
| Pessimistic repository locks | KEEP | Preserve serialized financial/subscription mutations |
| Entity state machines | KEEP | Preserve allowed-transition and terminal-state checks |
| Optimistic locks | KEEP | Preserve concurrent company-certification/whitelist update detection |
| Storage mutation recovery | KEEP | Preserve retry/recovery of partially applied file mutations |
| Acceptance startup/host/origin/secret guards | KEEP | Preserve production refusal and exact public-boundary validation |
| Billing-key V2 key ring | KEEP | Remove only V1 compatibility after retained-data proof |

## 7. Negative Searches and Reproducibility

Representative commands used:

```powershell
rg -n "/payments/(subscriptions/prepare|confirm|cancel)" frontend/src src/main/java src/test/java docs
rg -n "billing-agreements" frontend/src src/main/java src/test/java
rg -n "playHistory|play-history|play_histories" frontend/src src/main/java src/test/java docs
rg -n "downloadQueue|download-queue|download_queues" frontend/src src/main/java src/test/java docs
rg -n "finalizeUpgrade\(" src/main/java src/test/java
rg -n "LEGACY_VERSION|encryptionSecret|v1:" src/main/java src/main/resources src/test/java docs
rg -n "previewFile|preview_file" src/main/java src/main/resources src/test/java docs
rg -n "userIdSnapshot|userNicknameSnapshot|user_id_snapshot|user_nickname_snapshot" src/main/java src/main/resources src/test/java docs
rg -n "thymeleaf|ModelAndView|@Controller|templates/" build.gradle src/main src/test
rg -n "subscription-status|user-type|fetchAdminUserSubscriptionDetail" frontend/src src/main/java src/test/java docs
rg -n "@Deprecated|@Profile|@ConditionalOnProperty|matchIfMissing|fallback|recovery|lease|PESSIMISTIC_WRITE|@Version" src/main/java src/test/java
Get-ChildItem -Path src/main/java,src/test/java,src/main/resources -Recurse -File |
  Where-Object { $_.Name -match '(?i)(\.bak$|\.old$|\.orig$|~$|backup|copy)' }
```

Negative results relevant to disposition:

- No checked-in SPA calls to the legacy one-time payment endpoints.
- No import/caller of `frontend/src/api/playHistory.ts` outside the module.
- No import/caller of `frontend/src/api/downloadQueue.ts` outside the module.
- No current caller of the deprecated four-argument `finalizeUpgrade` overload.
- No Thymeleaf starter, template resource, or SSR view controller; the SPA forward controller is distinct and active.
- No backend backup-like files matching `.bak`, `.old`, `.orig`, `~`, `backup`, or `copy` under the audited source/resource trees.
- No `@JsonAlias` compatibility aliases were found. Existing `@JsonProperty` usage belongs to external OAuth contracts and is not a removal candidate.

## 8. Coverage Limits

The repository surfaces requested by the WI were searched statically, but the following could not be exhaustively inspected under the read-only/no-runtime constraint:

1. External API clients, actual traffic, and telemetry.
2. Production, acceptance, local, or copied database contents, including V1 billing ciphertext and legacy-table rows.
3. Every environment-variable combination and actual Spring conditional/profile activation at runtime.
4. Reflection, runtime-generated URLs, third-party scripts, or consumers outside this repository.
5. Runtime transaction, concurrency, provider, scheduler, and schema behavior.

These limitations are why ATS-BE-001, 002, 006, 011-013, 016, and 018 are not unconditional REMOVE decisions.

## 9. Validation and Git-State Evidence

### Before audit deliverable creation

- Branch: `codex/p1-acceptance-hardening`
- HEAD: `a96d2e0c5d249723bbf449b6834299a04cf2ad30`
- Pre-existing tracked modification: `frontend/tsconfig.tsbuildinfo`
- Pre-existing untracked artifacts included handoffs, other-agent deliverables, logs, screenshots, output, and `tmp/`.

### Static-only validation

- Evidence commands completed without mutating product/runtime state.
- No build, test, formatter, migration, server, DB, branch, or worktree operation was executed.
- The post-write `git diff --name-only -- src/main/java src/main/resources src/test/java build.gradle` result was empty.
- The post-write Git status added only the two permitted WI-034 deliverables relative to the captured baseline. The pre-existing `frontend/tsconfig.tsbuildinfo` modification and unrelated untracked artifacts remained present and untouched.

## 10. False-Positive Controls

- Spring endpoint reachability was not inferred from Java call counts.
- Recovery, reconciliation, cleanup, fallback exception handlers, locks, leases, and audit code were inspected for safety roles before classification.
- Public API removal was not approved solely from frontend negative searches.
- DB-backed compatibility was not approved for deletion without retained-data evidence.
- Active admin UI consumers overrode any superficial dead-code appearance.
- Acceptance code was separated into security boundary controls (KEEP) and fixture provisioning (REVIEW).

## 11. Rollback and Handoff

No product mutation exists to roll back. If these audit artifacts must be withdrawn, remove only:

- `deliverables/user/WI-20260716-ATS-034-summary.md`
- `deliverables/agent/WI-20260716-ATS-034-evidence-pack.md`

WI-038 should merge this inventory with the frontend, DB/config, and documentation/storage audits. No remediation should begin from this pack without the consolidated deletion/replacement approval required by REQ-004.
