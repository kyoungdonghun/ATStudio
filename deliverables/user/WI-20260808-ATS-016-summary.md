# WI-20260808-ATS-016 Completion Summary

## Status

WI-20260808-ATS-016 is **complete**. Track creation and audio replacement now use one decoded-PCM analysis result for duration and waveform. Invalid audio fails with the stable `AUDIO_ANALYSIS_FAILED` 400 business contract, and the administrator dry-run is read-only, deterministic, bounded, and storage-key-free.

No schema, migration, production dependency, frontend, live database data, or stored media was changed. No backfill or external network call was executed.

## Delivered Behavior

- MP3 CBR, MP3 VBR with Xing and ID3v2, and WAV are decoded through the existing Java Sound + mp3spi/tritonus stack.
- Duration and 200-point waveform are produced from the same PCM pass. The analyzer retains frame bytes split across 4096-byte reads and keeps waveform aggregation memory bounded.
- Duration is the decoded frame count divided by sample rate, rounded to the nearest whole second. Non-empty audio is clamped to at least one second; tests allow at most one second of format/decoder variance.
- Unsupported, malformed, empty, or unreadable audio is rejected before any storage mutation or Track save. No `duration=0` or `waveform=null` fallback remains in the upload path.
- Audio replacement analyzes first and then applies storage key, duration, and waveform through one `Track.updateAudioAnalysis(...)` domain operation inside the existing transaction and `StorageMutationCoordinator` contract.
- Metadata-only update does not invoke the analyzer or storage coordinator and preserves duration/waveform.
- `GET /api/admin/tracks/audio-analysis/dry-run?page=1&size=20` reports active and inactive Tracks in `id ASC` order. Size is restricted to `1..100`.
- Dry-run rows expose Track id/title/active state, readability, stored/analyzed duration, delta, stored waveform presence, format, status, recommendation, decoded frame count, sample rate, and channels. They do not expose `audioFile`, a storage key, or a path.

## Changed Files

Production:

- `src/main/java/com/atstudio/atstudio/service/audio/AudioAnalysisService.java`
- `src/main/java/com/atstudio/atstudio/service/audio/AudioAnalysisResult.java`
- `src/main/java/com/atstudio/atstudio/service/audio/AudioAnalysisFormat.java`
- `src/main/java/com/atstudio/atstudio/service/audio/AudioAnalysisException.java`
- `src/main/java/com/atstudio/atstudio/service/TrackService.java`
- `src/main/java/com/atstudio/atstudio/entity/Track.java`
- `src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java`
- `src/main/java/com/atstudio/atstudio/service/AdminTrackAudioAnalysisService.java`
- `src/main/java/com/atstudio/atstudio/controller/AdminTrackAudioAnalysisController.java`
- `src/main/java/com/atstudio/atstudio/dto/track/AdminTrackAudioAnalysisDryRunItemResponse.java`

Tests and synthetic fixtures:

- `src/test/java/com/atstudio/atstudio/testfixture/SyntheticAudioFixtures.java`
- `src/test/java/com/atstudio/atstudio/service/audio/AudioAnalysisServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/TrackServiceAudioProcessingTest.java`
- `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/TrackAudioReplacementTransactionIntegrationTest.java`
- `src/test/java/com/atstudio/atstudio/service/AdminTrackAudioAnalysisServiceTest.java`
- `src/test/java/com/atstudio/atstudio/controller/AdminTrackAudioAnalysisControllerTest.java`
- `src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java`

WI deliverables:

- `deliverables/user/WI-20260808-ATS-016-summary.md`
- `deliverables/agent/WI-20260808-ATS-016-evidence-pack.md`

Existing WI-014/WI-015 files and changes, `output/client-demo-screenshots-20260716-140514.zip`, root `application-local.yml`, schema files, secrets, and Git state were not modified by WI-016.

## Verification

| Command | Result |
|---|---|
| `.\gradlew.bat compileJava` | PASS, production compilation completed |
| `.\gradlew.bat test --tests "com.atstudio.atstudio.service.audio.AudioAnalysisServiceTest" --tests "com.atstudio.atstudio.service.TrackServiceAudioProcessingTest" --tests "com.atstudio.atstudio.service.TrackServiceTest" --tests "com.atstudio.atstudio.service.AdminTrackAudioAnalysisServiceTest" --tests "com.atstudio.atstudio.controller.AdminTrackAudioAnalysisControllerTest" --tests "com.atstudio.atstudio.service.TrackAudioReplacementTransactionIntegrationTest" --tests "com.atstudio.atstudio.controller.TrackControllerTest" --tests "com.atstudio.atstudio.service.storage.StorageMutationCoordinatorTest"` | PASS, 8 suites / 72 tests / 0 failed / 0 skipped |
| `.\gradlew.bat test --tests "*Track*" --tests "com.atstudio.atstudio.common.exception.GlobalExceptionHandlerTest" --tests "com.atstudio.atstudio.service.storage.StorageMutationCoordinatorTest"` | PASS, 19 suites / 113 tests / 0 failed / 0 skipped |
| `.\gradlew.bat build -x test` | FAIL after successful assemble: partial targeted-test JaCoCo data was checked against the repository-wide 80/80/70 thresholds |
| `.\gradlew.bat build -x test -x jacocoTestReport -x jacocoTestCoverageVerification` | PASS, compile/package/build completed; tests were already run separately above |

The only fixture test failure during implementation was an 8-bit WAV peak assertion expecting `0.500`; Java Sound's decoded 16-bit value was `0.504`. The deterministic assertion was corrected, and both final test runs passed. The synthetic MP3 fixtures required no encoder, binary media, or dependency change.

## Safety Proof

- Analysis failure tests verify no `StorageMutationCoordinator` interaction and no `trackRepository.save(...)` call.
- The replacement transaction integration test forces a DB length-constraint failure after replacement preparation and confirms the persisted title, storage key, duration, and waveform all remain unchanged.
- Existing `StorageMutationCoordinatorTest` verifies rollback removes the new file and never deletes the old file before commit.
- Dry-run service tests verify the only Track repository interaction is `findAll(Pageable)`; `verifyNoMoreInteractions(trackRepository)` excludes save/update/delete/backfill calls.
- Dry-run controller tests assert `audioFile` and `storageKey` are absent from JSON. The response DTO has no raw-key field.

## Residual Risks

- Synthetic fixtures cover PCM WAV, 128/320-kbps CBR MP3, and bitrate-varying MP3 with Xing + ID3v2. Unusual encoder/container variants should be assessed with the read-only dry-run before any separately approved backfill.
- Full repository coverage verification was not run in this WI. WI-023 and WI-027 own repository-wide test/coverage and build gates; the partial-test JaCoCo failure above is retained as evidence rather than treated as a product failure.
- The endpoint was not executed against live storage or a live database because WI-016 forbids data mutation/backfill and does not require operational media access. Its read-only behavior is covered by unit, controller, and H2 transaction tests.

## WI Chain

- WI-017 is unblocked.
- WI-019's WI-016 prerequisite is cleared. Under the approved REQ plan, WI-019 still also depends on WI-018.
