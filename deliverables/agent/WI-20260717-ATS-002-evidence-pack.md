# Evidence Pack: WI-20260717-ATS-002

## Summary (one-liner)

- Removed the approved backend residual contracts and Java consumers while preserving recurring-payment, authorization, download, whitelist, storage, and emergency-admin invariants.

## Scope / DoD Check

- [x] Removed backend server Play History contract while preserving browser-local history and full Public Listening.
- [x] Removed backend Download Queue contract while preserving Official Download authorization, accounting, licensing, history, and count behavior.
- [x] Removed legacy one-time payment and direct subscription-creation surfaces while preserving recurring checkout and financial integrity controls.
- [x] Removed dormant subscription-status, user-type, and ADMIN subscription-detail endpoints.
- [x] Removed preview and whitelist legacy snapshot Java consumers without editing schema ownership.
- [x] Preserved direct ADMIN subscription update/cancel emergency operations.
- [x] Exact Java symbol/route negative searches passed.
- [x] Java main/test compilation and focused backend tests passed.
- [x] No forbidden frontend, SQL/schema, application/provider config, active docs, Git ref, runtime, secret, or generated frontend cache edit was made.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution, approval, traceability, financial integrity |
| 0 | `docs/standards/development-standards.md` | SE implementation, Java, testing, and evidence rules |
| 1 | `docs/policies/security-policy.md` | Payment, media, whitelist, secret, and route safeguards |
| 1 | `docs/policies/quality-gates.md` | Regression, rollback, and evidence gates |
| 2 | `docs/design/api-spec.md` | Current and approved-to-remove API contracts |
| 2 | `docs/design/payment-integration-design.md` | Recurring-first payment architecture and KEEP paths |
| 2 | `docs/design/db-schema.md` | Persistence boundary and WI-004 schema ownership |
| REQ | `deliverables/user/REQ-20260716-ATS-004.md` | Approved V1 cleanup goal and constraints |
| WI | `deliverables/agent/WI-20260717-ATS-001-evidence-pack.md` | Approved disposition ledger and WI ownership |
| WI | `deliverables/agent/WI-20260716-ATS-038-evidence-pack.md` | Integrated INT manifests, safeguards, and proof gates |
| WI | `deliverables/agent/WI-20260716-ATS-034-evidence-pack.md` | Backend target inventory and exact source consumers |

**Injection Rules Applied**:

- Rule source: `AGENTS.md` and `deliverables/agent/WI-20260717-ATS-002-handoff.md`
- Assignee: `se`
- Task type: implementation
- Required tiers: Tier 0 plus handoff-injected Tier 1/2 and decision sources
- Evidence skill: `.agents/skills/create-wi-evidence-pack/SKILL.md`

## INT Traceability

| INT | Backend result | KEEP boundary verified |
|---|---|---|
| `INT-R01` | Deleted Play History controller/service/repository/entity/DTO/tests and User/Track cleanup consumers | Browser-local history is frontend-owned; Track Public Listening remains in `TrackController`/`TrackService` |
| `INT-R02` | Deleted Download Queue controller/service/repository/entity/key/DTO/tests and cleanup consumers | `DownloadController`/`DownloadService`, `track_downloads`, License, quota/accounting, and atomic count paths remain |
| `INT-R03` | Deleted only deprecated four-argument `finalizeUpgrade` overload | Three-argument claim/fence finalizer remains at `PaymentCommandTransactionService.java:604` |
| `INT-R04` | Removed `Track.previewFile`, storage-reference query branch, and obsolete test consumer | `Track.audioFile` remains at `Track.java:38`; full-resource test remains at `TrackServiceTest.java:287` |
| `INT-R05` | Removed whitelist user ID/nickname snapshot fields and five legacy getter assertions | Email/channel/plan/order immutable snapshots and byte-stable replay remain; replay test at `AdminWhitelistChannelServiceTest.java:407` |
| `INT-R07` | Removed stale `PUT /api/settings/*` matcher | Public GET remains at `SecurityConfig.java:85`; `/api/admin/**` remains ADMIN-only; tests at `SecurityFilterChainTest.java:95,106,116` |
| `INT-V01` | Deleted one-time `PaymentApplicationService`, provider package surface, DTOs, controller methods, and tests | Billing agreement methods remain at `PaymentController.java:30,43,55,65`; recurring provider package is untouched |
| `INT-V02` | Deleted direct POST endpoint, blocked service method, DTO, error code, and tests | Recurring checkout and subscription manage/reactivate paths remain |
| `INT-V03` | Deleted `/api/utils/subscription-status`, service method, DTO, and tests | `/api/user-subscriptions/me` remains the canonical subscription read |
| `INT-V04` | Deleted `/api/utils/user-type`, service method, DTO, and tests | User profile and authorization roles remain unchanged |
| `INT-V05` | Deleted ADMIN GET detail endpoint/service/test and stale GET wildcard matcher | ADMIN list plus direct update/cancel remain at `UserSubscriptionController.java:34,58,72` |

## Evidence Pointers

### Modified Backend Files (15)

- `src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java` - removed direct-checkout error code (`INT-V02`)
- `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java` - removed stale settings and ADMIN detail GET matchers (`INT-R07`, `INT-V05`)
- `src/main/java/com/atstudio/atstudio/controller/DownloadController.java` - removed queue compatibility wording (`INT-R02`)
- `src/main/java/com/atstudio/atstudio/controller/PaymentController.java` - retained recurring methods only (`INT-V01`)
- `src/main/java/com/atstudio/atstudio/controller/UserSubscriptionController.java` - removed direct POST and ADMIN detail GET (`INT-V02`, `INT-V05`)
- `src/main/java/com/atstudio/atstudio/controller/UtilController.java` - removed dormant utility routes (`INT-V03`, `INT-V04`)
- `src/main/java/com/atstudio/atstudio/entity/Track.java` - removed preview field (`INT-R04`)
- `src/main/java/com/atstudio/atstudio/entity/WhitelistExportItem.java` - removed legacy snapshot fields (`INT-R05`)
- `src/main/java/com/atstudio/atstudio/service/DownloadService.java` - removed queue compatibility wording (`INT-R02`)
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java` - removed deprecated overload (`INT-R03`)
- `src/main/java/com/atstudio/atstudio/service/TrackService.java` - removed queue/history cleanup dependencies (`INT-R01`, `INT-R02`)
- `src/main/java/com/atstudio/atstudio/service/UserService.java` - removed queue/history cleanup dependencies (`INT-R01`, `INT-R02`)
- `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java` - removed blocked creation and detail read (`INT-V02`, `INT-V05`)
- `src/main/java/com/atstudio/atstudio/service/UtilService.java` - removed dormant utility methods (`INT-V03`, `INT-V04`)
- `src/main/java/com/atstudio/atstudio/service/storage/StorageReferenceChecker.java` - removed preview reference (`INT-R04`)

### Modified Test Files (10)

- `src/test/java/com/atstudio/atstudio/controller/PaymentControllerTest.java`
- `src/test/java/com/atstudio/atstudio/controller/SecurityFilterChainTest.java`
- `src/test/java/com/atstudio/atstudio/controller/UserContractIntegrationTest.java`
- `src/test/java/com/atstudio/atstudio/controller/UserSubscriptionControllerTest.java`
- `src/test/java/com/atstudio/atstudio/entity/key/CompositeKeyEqualityTest.java`
- `src/test/java/com/atstudio/atstudio/service/AdminWhitelistChannelServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/UserServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/UserSubscriptionServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/UtilServiceTest.java`

### Deleted Backend Files (29)

- `src/main/java/com/atstudio/atstudio/controller/DownloadQueueController.java`
- `src/main/java/com/atstudio/atstudio/controller/PlayHistoryController.java`
- `src/main/java/com/atstudio/atstudio/dto/downloadqueue/DownloadQueueResponse.java`
- `src/main/java/com/atstudio/atstudio/dto/payment/PaymentCancelRequest.java`
- `src/main/java/com/atstudio/atstudio/dto/payment/PaymentCheckoutResponse.java`
- `src/main/java/com/atstudio/atstudio/dto/payment/PaymentConfirmRequest.java`
- `src/main/java/com/atstudio/atstudio/dto/payment/PaymentConfirmResponse.java`
- `src/main/java/com/atstudio/atstudio/dto/payment/PaymentOrderResponse.java`
- `src/main/java/com/atstudio/atstudio/dto/payment/PaymentPrepareRequest.java`
- `src/main/java/com/atstudio/atstudio/dto/payment/PaymentPrepareResponse.java`
- `src/main/java/com/atstudio/atstudio/dto/playhistory/PlayHistoryDeleteRequest.java`
- `src/main/java/com/atstudio/atstudio/dto/playhistory/PlayHistoryListItemResponse.java`
- `src/main/java/com/atstudio/atstudio/dto/playhistory/PlayHistorySaveRequest.java`
- `src/main/java/com/atstudio/atstudio/dto/subscription/UserSubscriptionRequest.java`
- `src/main/java/com/atstudio/atstudio/dto/util/SubscriptionStatusResponse.java`
- `src/main/java/com/atstudio/atstudio/dto/util/UserTypeResponse.java`
- `src/main/java/com/atstudio/atstudio/entity/DownloadQueue.java`
- `src/main/java/com/atstudio/atstudio/entity/PlayHistory.java`
- `src/main/java/com/atstudio/atstudio/entity/key/DownloadQueueId.java`
- `src/main/java/com/atstudio/atstudio/repository/DownloadQueueRepository.java`
- `src/main/java/com/atstudio/atstudio/repository/PlayHistoryRepository.java`
- `src/main/java/com/atstudio/atstudio/service/DownloadQueueService.java`
- `src/main/java/com/atstudio/atstudio/service/PaymentApplicationService.java`
- `src/main/java/com/atstudio/atstudio/service/PlayHistoryService.java`
- `src/main/java/com/atstudio/atstudio/service/payment/provider/MockPaymentProvider.java`
- `src/main/java/com/atstudio/atstudio/service/payment/provider/PaymentProvider.java`
- `src/main/java/com/atstudio/atstudio/service/payment/provider/PaymentProviderConfirmResult.java`
- `src/main/java/com/atstudio/atstudio/service/payment/provider/PaymentProviderPrepareResult.java`
- `src/main/java/com/atstudio/atstudio/service/payment/provider/TossPaymentProvider.java`

### Deleted Test Files (6)

- `src/test/java/com/atstudio/atstudio/controller/DownloadQueueControllerTest.java`
- `src/test/java/com/atstudio/atstudio/controller/PlayHistoryControllerTest.java`
- `src/test/java/com/atstudio/atstudio/service/DownloadQueueServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/PaymentApplicationServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/PlayHistoryServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/payment/provider/TossPaymentProviderTest.java`

## Commands & Outputs

- Preflight: `git branch --show-current`; `git rev-parse HEAD`; `git status --short`
  - Branch: `codex/p1-acceptance-hardening`
  - HEAD: `a96d2e0c5d249723bbf449b6834299a04cf2ad30`
  - Existing unrelated frontend cache/log/output/deliverable changes were left untouched.
- Exact negative searches used named `rg -n --glob '*.java'` checks over `src/main/java` and `src/test/java`.
  - PASS: `INT-R01 server play history`
  - PASS: `INT-R02 download queue`
  - PASS: `INT-V01 one-time payment symbols`
  - PASS: `INT-V01 one-time payment routes`
  - PASS: `INT-V02 direct subscription symbols`
  - PASS: `INT-V02 and INT-V05 removed mappings`
  - PASS: `INT-V03 and INT-V04 utility routes`
  - PASS: `INT-R03 deprecated upgrade overload`
  - PASS: `INT-R04 preview consumers`
  - PASS: `INT-R05 whitelist legacy snapshots`
  - PASS: `INT-R07 stale settings matcher`
- `git diff --check -- src/main/java src/test/java` -> PASS.
- Diff summary: 60 product/test files, 36 insertions, 2667 deletions; 25 modified, 35 deleted.

## Tests

- First `gradlew.bat compileJava compileTestJava --console=plain`:
  - `compileJava` passed.
  - `compileTestJava` found five `AdminWhitelistChannelServiceTest` assertions against the approved-to-remove legacy snapshot getters.
  - Removed only those five assertions; current immutable export fields and byte-stable replay assertions remain.
- Final `gradlew.bat compileJava compileTestJava --console=plain` -> `BUILD SUCCESSFUL` in 8s.
- Focused command:

```powershell
.\gradlew.bat test --console=plain `
  --tests "com.atstudio.atstudio.controller.PaymentControllerTest" `
  --tests "com.atstudio.atstudio.service.PaymentCommandTransactionFenceTest" `
  --tests "com.atstudio.atstudio.service.PaymentProviderSuccessRecoveryIntegrationTest" `
  --tests "com.atstudio.atstudio.controller.UserSubscriptionControllerTest" `
  --tests "com.atstudio.atstudio.service.UserSubscriptionServiceTest" `
  --tests "com.atstudio.atstudio.controller.TrackControllerTest" `
  --tests "com.atstudio.atstudio.service.TrackServiceTest" `
  --tests "com.atstudio.atstudio.service.DownloadServiceTest" `
  --tests "com.atstudio.atstudio.service.DownloadConcurrencyContractTest" `
  --tests "com.atstudio.atstudio.service.AdminWhitelistChannelServiceTest" `
  --tests "com.atstudio.atstudio.service.WhitelistConcurrencyContractTest" `
  --tests "com.atstudio.atstudio.controller.SecurityFilterChainTest" `
  --tests "com.atstudio.atstudio.service.StorageCleanupServiceTest" `
  --tests "com.atstudio.atstudio.service.StorageMutationRecoveryVerificationTest"
```

- Result: `BUILD SUCCESSFUL` in 47s.
- XML result aggregate: 19 suites, 146 tests, 0 failures, 0 errors, 0 skipped.
- Report pointer: `build/reports/tests/test/index.html`.

## Protected Safeguards Reviewed

- `INT-K01`: payment order claims, idempotency, provider-success persistence, finalize fences, reconciliation, incidents, refunds, audits, leases, locks, and state machines remain. Focused fence/recovery tests passed.
- `INT-K05`: USER payment role boundary and ADMIN catch-all remain; billing route and settings role tests passed.
- `INT-K06`: recurring billing agreement prepare/confirm/read/cancel and subscription upgrade/renewal paths remain.
- `INT-K07`: whitelist export scope, immutable replay, current snapshots, optimistic lock, and CSV safety remain.
- `INT-K08`: full Track streaming remains; server Play History removal does not touch frontend local history.
- `INT-V12`: direct ADMIN subscription update/cancel remains unchanged.
- Download KEEP boundary: first-download user lock, License issuance, quota/history, atomic count, and licensed re-download code remain; download contract/concurrency tests passed.
- Storage KEEP boundary: mutation coordinator, journal, cleanup, and recovery code remains; focused storage tests passed.

## Risks / Rollback

### Risks

- Approved external-client/telemetry waivers mean removed public routes now stop resolving; no compatibility failure endpoint remains.
- Schema still contains `play_histories`, `download_queue`, `tracks.preview_file`, and legacy whitelist snapshot columns until WI-004. This WI intentionally removed Java consumers only.
- `PaymentProviderType`, `PaymentProperties`, `application*.yml`, provider selection, and persisted provider normalization remain WI-004 ownership. Do not treat their residual one-time names as a WI-002 miss.
- Frontend and active-document references remain WI-003 and WI-005 ownership.
- Validation was focused and H2/local-test based. Full backend, disposable MySQL, runtime, API, and cross-layer verification remains WI-004/WI-006.

### Rollback

- Revert the WI-002 code/test diff only, or restore the listed 60 files from preflight HEAD `a96d2e0c5d249723bbf449b6834299a04cf2ad30` after reconciling concurrent edits.
- Remove only `deliverables/user/WI-20260717-ATS-002-summary.md` and this Evidence Pack to roll back the deliverables.
- Do not restore or alter unrelated frontend cache, logs, output, prior WI deliverables, Git refs, runtime, secrets, or generated artifacts.

## Follow-ups

- `WI-20260717-ATS-004`: remove matching schema tables/columns and normalize `PaymentProviderType` / `PaymentProperties` / application provider configuration. Precise pointers: `src/main/resources/schema.sql`, `src/main/java/com/atstudio/atstudio/entity/enums/PaymentProviderType.java`, `src/main/java/com/atstudio/atstudio/config/PaymentProperties.java`, and `src/main/resources/application*.yml`.
- `WI-20260717-ATS-003`: remove frontend Play History/Download Queue/legacy payment clients and route aliases.
- `WI-20260717-ATS-005`: update active API, DB, payment, UI, and glossary documents without rewriting historical evidence.
- `WI-20260717-ATS-006`: run full backend/frontend/docs/MySQL/runtime/API/UI verification and independent residual-reference audit.
