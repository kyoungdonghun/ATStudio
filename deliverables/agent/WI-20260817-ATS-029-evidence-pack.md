
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A056: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a056). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

# Evidence Pack: WI-20260817-ATS-029

## Summary

- Read-only targeted integration regression verification completed with no confirmed defect. Development-database Hibernate validate boot is **BLOCKED** because the current startup path can mutate storage-recovery state.

## Scope / DoD Check

- [x] Authentication/session, routing, mutable catalogue, player, and tag risks have separate UI/API/persistence evidence.
- [x] Every result is labelled PASS, BLOCKED, or NOT EXECUTED.
- [ ] Current development database boot with Hibernate `ddl-auto=validate`: BLOCKED; no mutation-safe application startup switch exists in the inspected path.
- [x] No source, tracked configuration, DB, data, provider, SMTP, or client-acceptance runtime was changed or invoked.
- [x] `git diff --check` passed after verification and again after deliverable creation.

## Reference Documents

| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Read-only verification and work-boundary baseline |
| 0 | `docs/standards/development-standards.md` | Backend/frontend contract inspection baseline |
| 0 | `docs/standards/documentation-standards.md` | Deliverable traceability baseline |
| 0 | `docs/standards/glossary.md` | Canonical domain terminology |
| 1 | `docs/policies/security-policy.md` | Secret-safe and non-side-effect evidence boundary |
| 1 | `docs/policies/quality-gates.md` | Verification evidence and quality-gate baseline |
| REQ | `deliverables/user/REQ-20260817-ATS-010.md` | Runtime isolation and external-effect approval gates |
| Context | `deliverables/user/REQ-20260817-ATS-009.md` | Release-rehearsal context |
| Context | `deliverables/user/WI-20260817-ATS-023-summary.md` | Current isolated client-acceptance boundary |
| Context | `deliverables/agent/WI-20260817-ATS-023-evidence-pack.md` | Prior acceptance runtime evidence |
| Context | `scripts/acceptance/README.md` | Client-acceptance port/runtime boundary |
| Handoff | `deliverables/agent/WI-20260817-ATS-029-handoff.md` | Approved scope, forbidden actions, and output contract |

## Risk / Evidence Matrix

| Risk area | Status | UI evidence | API evidence | Persistence/runtime evidence |
|---|---|---|---|---|
| Authentication, session, and return route | PASS | `frontend/src/router/ProtectedRoute.test.tsx:78-150`; `SubscriberRoute.test.tsx:77-228`; `api/client.test.ts:91-559`; `store/authStore.test.ts:73-372` | `SecurityFilterChainTest` (23), `AuthControllerTest` (2), `AuthServiceTest` (17) | H2 test profile only; live development persistence NOT EXECUTED |
| Track/album/playlist cancellation, retry, ownership | PASS | `TrackEditPage.test.tsx:146-208`; `AlbumEditPage.test.tsx:171-653`; `PlaylistEditPage.test.tsx:121-197`; `TagManagePage.test.tsx:194-343` | `TrackControllerTest` (33), `PlaylistControllerTest` (16), `TrackServiceTest` (34), `AlbumServiceTest` (20), `PlaylistServiceTest` (28) | `TrackAudioReplacementTransactionIntegrationTest` (1) proves rollback in H2; live development state NOT EXECUTED |
| Player duration, waveform, and hydration | PASS | `TrackDetailPage.test.tsx:191-211`; `WaveformCanvas.test.tsx:46-62`; `PlayerBar.test.tsx:260-327`; `playerStore.test.ts:257-649` | `TrackControllerTest`; `fetchPlayableTracks` contract at `frontend/src/api/tracks.ts:84-86` | `TrackWaveformSchemaContractTest` (2); `PlayableTrackQueryCountTest` (4), both H2/contract scope |
| Tag keyword and category filters | PASS | `TrackListPage.test.tsx:262-742`; `TagManagePage.test.tsx:80-343` | `TagControllerTest` (16); `TrackSpecificationIntegrationTest` (4); `TagServiceAvailableTagsIntegrationTest` (1) | H2 query/association tests only; live development tag records NOT EXECUTED |
| Development Hibernate validate boot | BLOCKED | N/A | N/A | `src/main/resources/application.yml:15` defaults to `validate`, but `StorageMutationRecoveryService.java:35-48` invokes `claimBatch` at `ApplicationReadyEvent`; real development boot was not started |
| Live local UI/API smoke | NOT EXECUTED | No browser/local UI request | No HTTP request | `5173` (node) and `8080` (java) were listening; `18080` and `18081` were absent. The active pair belongs to the client-acceptance boundary and was not touched. |

## Evidence Pointers

- `frontend/src/router/ProtectedRoute.tsx:55-59` and `frontend/src/router/SubscriberRoute.tsx:51-98` implement return-route navigation, request cancellation, and bounded retry.
- `frontend/src/api/client.ts:44-158` and `frontend/src/store/authStore.ts:102-142` implement protected-request refresh/replay and session-generation fencing.
- `frontend/src/pages/public/TrackListPage.tsx:198-202,367-382,461-467` and `frontend/src/api/tracks.ts:35-61` retain keyword plus four tag families in URL/API state.
- `frontend/src/pages/public/TrackDetailPage.tsx:169-170`, `frontend/src/layouts/PlayerBar.tsx:188-198`, and `frontend/src/store/playerStore.ts:250-264,417-439,471-521` preserve complete playable metadata and refine duration from decoded media.
- `frontend/src/pages/creator/AlbumEditPage.tsx:81-242` and `frontend/src/pages/subscriber/PlaylistEditPage.tsx:44-102` use AbortController ownership fencing for mutable flows.
- `src/main/java/com/atstudio/atstudio/dto/track/PlayableTrackResponse.java:9-30` returns duration, waveform, and tags; `src/test/java/com/atstudio/atstudio/repository/PlayableTrackQueryCountTest.java` holds the query-count contract.
- `src/test/java/com/atstudio/atstudio/repository/spec/TrackSpecificationIntegrationTest.java:58-113` covers keyword and all-tags semantics; `TagServiceAvailableTagsIntegrationTest.java:64-82` covers active-only four-family available-tag semantics.
- `src/main/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapRunner.java:39` conditionally enables data-changing test bootstrap; `src/main/java/com/atstudio/atstudio/service/storage/StorageMutationRecoveryService.java:35-48` has no equivalent safe verification disable switch.

## Commands and Results

```powershell
npm test -- src/router/ProtectedRoute.test.tsx src/router/SubscriberRoute.test.tsx src/router/index.test.tsx src/api/client.test.ts src/api/auth.test.ts src/store/authStore.test.ts src/utils/safeStorage.test.ts src/pages/public/TrackListPage.test.tsx src/pages/public/TrackDetailPage.test.tsx src/components/player/playerComponents.test.tsx src/components/player/WaveformCanvas.test.tsx src/layouts/PlayerBar.test.tsx src/store/playerStore.test.ts src/store/playerPersistence.test.ts src/utils/playableTrack.test.ts src/pages/creator/TrackUploadPage.test.tsx src/pages/creator/TrackEditPage.test.tsx src/pages/creator/AlbumCreatePage.test.tsx src/pages/creator/AlbumEditPage.test.tsx src/pages/subscriber/PlaylistListPage.test.tsx src/pages/subscriber/PlaylistEditPage.test.tsx src/pages/subscriber/PlaylistDetailPage.test.tsx src/pages/admin/TagManagePage.test.tsx
```

- PASS: 23 files, 306 tests; `jsdom`/mocked frontend contract only, with no live HTTP call.

```powershell
.\gradlew.bat test --tests "com.atstudio.atstudio.controller.SecurityFilterChainTest" --tests "com.atstudio.atstudio.controller.AuthControllerTest" --tests "com.atstudio.atstudio.service.auth.AuthServiceTest" --tests "com.atstudio.atstudio.controller.TrackControllerTest" --tests "com.atstudio.atstudio.service.TrackServiceTest" --tests "com.atstudio.atstudio.service.TrackServiceAudioProcessingTest" --tests "com.atstudio.atstudio.service.TrackAudioReplacementTransactionIntegrationTest" --tests "com.atstudio.atstudio.service.AlbumServiceTest" --tests "com.atstudio.atstudio.service.AlbumPlaylistMutationLockContractTest" --tests "com.atstudio.atstudio.controller.PlaylistControllerTest" --tests "com.atstudio.atstudio.service.PlaylistServiceTest" --tests "com.atstudio.atstudio.controller.TagControllerTest" --tests "com.atstudio.atstudio.service.TagServiceAvailableTagsIntegrationTest" --tests "com.atstudio.atstudio.entity.TrackWaveformSchemaContractTest" --tests "com.atstudio.atstudio.repository.PlayableTrackQueryCountTest" --tests "com.atstudio.atstudio.repository.spec.TrackSpecificationIntegrationTest"
```

- PASS: 16 suites, 211 tests, 0 failures, 0 errors, 0 skipped. Test resources use H2 `create-drop`; this is not development-database evidence.

```powershell
npm run typecheck
npm run lint
git diff --check
```

- PASS: TypeScript check and ESLint exited successfully; `git diff --check` exited successfully.

```powershell
Get-NetTCPConnection -State Listen -LocalPort 5173,8080,18080,18081
```

- Boundary check only: 5173/node and 8080/java were present; 18080/18081 were absent. No request was sent.

## Runtime Boundary and Non-Execution

- NOT EXECUTED: `bootRun`, direct development-DB access, browser navigation, and local API calls.
- NOT EXECUTED: client-acceptance runtime/database access; no request was sent to its observed listeners.
- NOT EXECUTED: Toss requests, payment/refund requests, SMTP delivery, scheduler rehearsal, storage recovery, and all provider callbacks.
- BLOCKED rationale: `StorageMutationRecoveryService.recoverOnStartup()` runs on `ApplicationReadyEvent` and calls `recoverBatch()`, which can claim journal rows, transition states, and delete storage objects. Running application startup would breach this WI's no-data/no-storage-mutation constraint.

## Generated Artifacts

- Required test tools refreshed ignored artifacts: `build/jacoco/test.exec`, `build/reports/tests/test/**`, `build/test-results/test/**`, and `frontend/node_modules/.vite/vitest/**`.
- They were left in place as required. `git status --short` showed no new untracked test artifact; the pre-existing untracked deliverables and unrelated `output/` and `scripts/database/patches/` entries were not modified.

## Risks / Rollback

- Risks: Real development database compatibility, live API response shape, browser rendering, and persisted production-like state are unresolved. Passing H2 and mocked UI tests must not be represented as proof of those live conditions.
- Rollback: None. This WI made no source/configuration/DB/data/runtime change. Generated ignored test artifacts were intentionally not removed.

## Follow-up Recommendation

- New approved WI: introduce a dedicated non-mutating verification profile that disables `ApplicationReadyEvent` storage recovery, all scheduled jobs, and test-user bootstrap while retaining JPA initialization with `ddl-auto=validate`. Add an integration test proving those controls, then boot only against an explicitly isolated development target and record a secret-free success/failure result.

## Changed Files

- `deliverables/user/WI-20260817-ATS-029-summary.md`
- `deliverables/agent/WI-20260817-ATS-029-evidence-pack.md`
