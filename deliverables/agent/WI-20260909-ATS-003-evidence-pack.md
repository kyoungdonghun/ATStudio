---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: se
category: evidence-pack
status: stable
---

# Evidence Pack: WI-20260909-ATS-003

## Summary
- Implemented DATA-01/02/03 and STORAGE-04 in the approved shared workspace. Two MA-run focused checkpoints passed, including final active-operation and AlbumLike additions. Ready for downstream independent review, not production GO.
- Root for every exact relative path below: `C:/Users/jm991/Desktop/project/ATStudio`.
- Parent: `019e30c8-16ba-7d71-9adb-82894b7cb77d`; WI handoff: `deliverables/agent/WI-20260909-ATS-003-handoff.md`; approved REQ: `deliverables/user/REQ-20260909-ATS-001.md`.

## Scope / DoD Check
- [x] Implement retained licenses/download events and quota; preserve Track media on soft deletion.
- [x] Align Album/Playlist thumbnail deletion, retained references, cleanup and unchanged strict audit.
- [x] Fence all identified material Track writers; add/remove AlbumLike fencing approved by MA within the same stale-reference regression boundary.
- [x] Close streams, clean partial writes and journal every target before staging; preserve failed cleanup ownership for recovery.
- [x] Register active operations before durable prepare; exclude them before claim/attempt increment until completion or preparation failure.
- [x] Add independent-context H2 and synthetic temporary-storage regressions; no schema, provider, retained-data or runtime actions.
- [x] MA executed the final added tests and requested companion regressions; artifact counts and successful log read back by se.
- [ ] MA aggregate build and downstream independent review close final acceptance.
- [x] Two-set outputs generated with `create-wi-evidence-pack` after checking the required handoff exists.

## Reference Documents
| Tier | Injected/read evidence | Purpose |
|---|---|---|
| 0 | `docs/standards/core-principles.md`; `docs/standards/development-standards.md`; `docs/standards/documentation-standards.md`; `docs/standards/glossary.md` | Approved scoped execution; retained evidence; transactions; English technical reports; exact pointers |
| 1 | `docs/policies/security-policy.md`; `docs/policies/quality-gates.md` | No sensitive payload logging, no quality-gate weakening, isolated regression |
| 2 | Approved REQ and WI handoff above; `deliverables/agent/WI-20260908-ATS-018-findings.md:33`; `deliverables/agent/WI-20260908-ATS-017-evidence-pack.md:37` | DATA/STORAGE root causes and historical evidence, not current execution results |
| 2 | `docs/design/usecase/sound-track.md:306`; `docs/design/usecase/user-license.md:28`; `docs/design/runtime-storage-operations.md` | Soft deletion, licenses, DB/media tuple integrity |
| Skill | `.agents/skills/create-wi-evidence-pack/SKILL.md`; `.claude/config/workspace.json`; `.claude/config/context-injection-rules.json` | ATS naming and evidence contract; se required tiers `[0]` |
- MA explicitly extended owned files to `StorageMutationJournalService.java` and `AlbumLikeService.java`, with optional `AlbumRepository.java` only if necessary. No repository change was needed for Album.

## Exact Product Changes
| Finding | Changed path | Key evidence |
|---|---|---|
| DATA-01/03 | `src/main/java/com/atstudio/atstudio/service/TrackService.java` | `:170`, `:218`, `:220`, `:276`: lock before managed Track mutation; remove only the two license/history delete calls |
| DATA-03 | `src/main/java/com/atstudio/atstudio/repository/TrackRepository.java` | `:21`: narrow `PESSIMISTIC_WRITE` lookup, no fetch-join user lock |
| DATA-03 | `src/main/java/com/atstudio/atstudio/service/LikeService.java` | `:33`, `:61`, `:69`: user then Track, before reading/mutating Like; no stale association pre-load |
| DATA-03 | `src/main/java/com/atstudio/atstudio/service/DownloadService.java` | `:46`, `:50`: preserve user quota lock, then Track lock; retain atomic count update |
| DATA-02 | `src/main/java/com/atstudio/atstudio/service/PlaylistService.java` | `:268`: capture old key before deactivation; journal old-key cleanup |
| DATA-02 | `src/main/java/com/atstudio/atstudio/service/AlbumService.java` | `:141`: same old-key handoff for Album |
| DATA-02 | `src/main/java/com/atstudio/atstudio/entity/Playlist.java` | `:42`: deactivate and null thumbnail together; no schema annotations changed |
| DATA-02 | `src/main/java/com/atstudio/atstudio/entity/Album.java` | `:53`: soft-delete and null thumbnail together; no schema annotations changed |
| DATA-02 | `src/main/java/com/atstudio/atstudio/service/AlbumLikeService.java` | `:36`, `:67`, `:76`: all AlbumLike writers use user then existing Album row lock |
| DATA-02 | `src/main/java/com/atstudio/atstudio/service/storage/StorageReferenceChecker.java` | `:15`, `:33`: all retained Album/Playlist keys remain references, including inactive and cross-catalog shared keys |
| STORAGE-04 | `src/main/java/com/atstudio/atstudio/service/storage/LocalStorageService.java` | `:93`: close input/output, CREATE_NEW ownership, cleanup newly created partial file without removing pre-existing stage |
| STORAGE-04 | `src/main/java/com/atstudio/atstudio/service/storage/StorageMutationCoordinator.java` | `:68`, `:129`, `:182`: durable preparation before bytes, failure cleanup for all drafts, active-owner lifecycle for writes and deletes |
| STORAGE-04 | `src/main/java/com/atstudio/atstudio/service/storage/StorageMutationJournalService.java` | `:28`, `:96`: same-process active operation registry and filter before claims/attempt increments |

## Exact Test Changes
- `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java`: use write lookup; assert no license/history repository deletion interaction.
- `src/test/java/com/atstudio/atstudio/service/LikeServiceTest.java`: write stubs, user-before-Track-before-Like ordering and counter assertions.
- `src/test/java/com/atstudio/atstudio/service/DownloadServiceTest.java`: write lookup and preserved decision ordering.
- `src/test/java/com/atstudio/atstudio/service/DownloadConcurrencyContractTest.java`: pessimistic Track annotation and user-before-Track source contract.
- `src/test/java/com/atstudio/atstudio/service/TrackMutationConcurrencyIntegrationTest.java` (new): `:77` unlocked persistence control reproduces stale keys; `:113` metadata/like-add/like-remove/download/delete/bulk-play/bulk-download interleavings preserve media/counters.
- `src/test/java/com/atstudio/atstudio/service/storage/LocalStorageServiceTest.java`: successful input close; duplicate staging preserves original bytes.
- `src/test/java/com/atstudio/atstudio/service/storage/StorageMutationCoordinatorTest.java`: both failed-stage targets owned; preparation failure writes no bytes; cleanup-transition failure retains journal; every completion status releases ownership.
- `src/test/java/com/atstudio/atstudio/service/storage/StorageMutationJournalServiceTest.java`: active entries consume no claim/attempt; release/recreated journal service makes stale work eligible.
- `src/test/java/com/atstudio/atstudio/service/storage/RetainedHistoryStorageIntegrationTest.java` (new): `:129` identity/timestamp/quota retention; `:156` cleanup-to-production-strict-guard matrix; `:208`, `:312` AlbumLike races; `:249` aged active stage and promoted/uncommitted owner; `:366` first/later partial stage and failed-cleanup restart.
- Output files: this Evidence Pack and `deliverables/user/WI-20260909-ATS-003-summary.md`. No other files were changed by this WI.

## Semantics And Lock Order
- Track deactivation no longer erases licenses or download events. Existing history list filtering for inactive Tracks is unchanged; retained rows still count toward the daily limit. Reactivation preserves license ID/code/issued timestamp and re-download identity without a new quota event.
- Existing Track tag/playlist/album/like association cleanup behavior remains otherwise unchanged. Track audio/thumbnail remain retained. No actual retained rows/files were inspected, repaired or deleted.
- New Album/Playlist deletion clears only its current thumbnail reference; the old key remains in the durable DELETE journal. Shared active, cross-catalog and legacy inactive references prevent physical cleanup. No-thumbnail is a no-op for storage.
- `StorageIntegrityService` and `StorageIntegrityStartupGuard` are unchanged. A legacy inactive row with a missing retained key still fails strict startup; no repair or exclusion was added.
- Track metadata/media/deactivation takes Track X before entity read. Like/Download takes User X then Track X before child reads/writes/FK insertion. AlbumLike takes User X then Album X; Album/Playlist writers retain their existing parent locks. No new Track-to-User or Album-to-User lock was introduced.
- Repository bulk play/download updates take their database UPDATE row lock, change only their counter column, and conflict with Track X. Both bulk paths are in the H2 interleaving matrix. Download keeps its guarded atomic update. `AdminTrackAudioAnalysisService` is read-only; no additional Track material writer was found in the source scan.
- The reference checker remains domain-based, as before. TRACK/ALBUM/PLAYLIST protect public catalog keys; private document domains retain their existing queries and root behavior. This is not a new root-aware schema or private/public migration.

## Recovery Bounds
- Single-runtime policy only: the registry is per `StorageMutationJournalService` instance. Register occurs before PREPARED becomes visible; normal callbacks retain ownership through afterCompletion, including committed/rolled-back/unknown status. Preparation/staging failure releases ownership in finally.
- Candidate selection still uses a database pessimistic claim. In-flight candidates are filtered before attempt increment and lease assignment; a long-running request cannot exhaust retry attempts or have its stage/promoted object deleted by that runtime's recovery.
- Existing defaults are unchanged: PREPARED stale threshold 300 seconds since `updatedAt`, claim 120 seconds, batch 50, maximum 8 attempts, scheduled polling 60 seconds. Active ownership is not limited to 300 seconds.
- After a real process restart, abandoned in-memory ownership disappears and PREPARED grace still applies. Tests recreate journal/storage/recovery objects and use H2; they do not kill a process or prove multi-runtime coordination.
- A batch whose oldest candidates are all active can delay later recovery until a subsequent batch. No new upload timeout, heartbeat lease, distributed worker or schema was introduced.

## Commands And Execution Evidence
- Child executed source/document/test reads using `rg`/`Get-Content`, manual edits using `apply_patch`, and report/source static checks only. No child Gradle/npm, Git operation, DB client, runtime restart, provider/account action or subdelegation.
- MA reported initial `compileJava compileTestJava` PASS (29 seconds), then focused Gradle PASS (1 minute 7 seconds) at the earlier checkpoint.
- Read-back artifact: `output/release-remediation-20260909/focused-initial-results.json`; log: `output/release-remediation-20260909/focused-initial.log` (MA-owned).
- WI003 initial subset: 100 total, 99 pass, 1 platform-dependent LocalStorage skip, 0 failures/errors. This proves the initial stale-Track control/interleavings, retention/quota, thumbnail deletion/strict guard and partial-stage tests, not the subsequently added active-owner/AlbumLike tests.
- Second MA checkpoint: `output/release-remediation-20260909/storage-auth-second.log` ends `BUILD SUCCESSFUL in 57s`; `output/release-remediation-20260909/storage-auth-second-results.json` contains 19 suites / 158 passed / 0 failed / 0 errors / 0 skipped, including companion storage and separate auth/security suites.
- Final WI003 core in that second checkpoint: TrackMutationConcurrencyIntegrationTest 8, RetainedHistoryStorageIntegrationTest 22, StorageMutationCoordinatorTest 11, StorageMutationJournalServiceTest 6; all 47 passed. Active staging/promoted-owner exclusion and AlbumLike replacement/deletion races are now executed, not proposed.
- Initial and second checkpoints overlap and must not be added as unique-test counts. Final aggregate build/independent review remain MA-owned and pending; these are provisional scoped checkpoints, not production/MySQL proof.

## Reproducible MA Gates
Use only MA's isolated H2 configuration, synthetic temp roots, disabled MySQL opt-ins/providers/bootstrap, serialized Gradle slot. Child has not executed these commands.
```powershell
.\gradlew.bat test --tests '*TrackMutationConcurrencyIntegrationTest' --tests '*RetainedHistoryStorageIntegrationTest' --tests '*StorageMutationJournalServiceTest' --tests '*StorageMutationCoordinatorTest' --tests '*LocalStorageServiceTest' --tests '*TrackServiceTest' --tests '*LikeServiceTest' --tests '*DownloadServiceTest' --tests '*DownloadConcurrencyContractTest'
.\gradlew.bat test --tests '*AlbumServiceTest' --tests '*PlaylistServiceTest' --tests '*AlbumPlaylistMutationLockContractTest' --tests '*TrackAudioReplacementTransactionIntegrationTest' --tests '*TrackServiceAudioProcessingTest' --tests '*StorageCleanupServiceTest' --tests '*StorageReferenceCheckerBranchCoverageTest' --tests '*StorageMutationRecoveryServiceTest' --tests '*StorageMutationRecoveryVerificationTest' --tests '*StorageIntegrityServiceTest' --tests '*StorageIntegrityStartupGuardTest'
```

## Risks / Rollback / Follow-ups
- No unresolved policy or schema choice. Operational boundary: one live writer/recovery runtime for a DB/storage tuple; rolling overlap across separate JVMs is not protected by this in-memory registry.
- Track/Album writes now hold existing-style row locks during analysis/storage; this can increase same-row contention. H2 regression is not MySQL lock-scheduler, production latency or client-byte-delivery proof.
- Unchanged strict audit can still reject already-broken retained references. Those require separately scoped diagnosis/repair, never automatic cleanup to manufacture PASS.
- Rollback must be a coordinated code-only reversal of this WI's exact paths, preserving unrelated work and retained data. Do not reverse database history or remove media/journal evidence; no schema rollback exists because schema is unchanged.
- MA owns downstream WI-013 (independent verification), WI-012 and WI-014 chain progression per handoff. This child does not close REQ-20260909-ATS-001 or claim production readiness.
