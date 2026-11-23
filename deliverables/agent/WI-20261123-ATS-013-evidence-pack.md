# Friends pilot track library authorization

WI-20261123-ATS-013 implements the Spring portion of [REQ-20261123-ATS-003](../user/REQ-20261123-ATS-003.md). Use the [handoff](WI-20261123-ATS-013-handoff.md) for Tier 0 standards, security policy and UI/API references.

## Behavior

An invited participant with a valid non-billing grant can like a published track, read their likes, and manage their own playlists. Playlist capacity comes from the participant's actual plan. The capacity endpoint returns only `maxPlaylists`. Reads pin the current publication selection before opening the personal metadata snapshot. Writes lock the participant and grant, pin the catalog, and recheck eligibility in the same transaction. Pilot playlist lookups constrain the ID and owner in the database.

When a track is unpublished, fresh personal projections exclude it from likes, playlist membership, counts and displayed order. Reordering uses the visible subset and retains hidden memberships. The normal stream continues to check the current catalog. Pilot playlist metadata accepts a title and description; the server rejects custom images and omits historical playlist cover paths. Track images continue through the public track channel. The single-track membership endpoint is sufficient for this UI. Album likes, private playlist batch addition and the billing API stay outside the pilot method allowlist.

## Code and checks

- Spring boundary: `security/ListeningAccessFilter.java`, `service/PilotAccess.java`, `controller/PlaylistController.java`, `service/LikeService.java`, `service/PlaylistService.java` under `src/main/java/com/atstudio/atstudio`.
- Owner-scoped lookup and aggregate counts: `repository/PlaylistRepository.java`, `repository/PlaylistTrackRepository.java`. The playlist DTOs use the appropriate thumbnail and visible track order.
- The final offline Gradle invocation below passed **140 tests in 11 suites** with zero failures, errors or skips and built the executable JAR. The local two-user HTTP test is shared with WI015 and is counted once in these totals. It uses actual Spring/JWT/JPA responses, local H2 data and synthetic audio. The original selling controllers/services, anonymous/operator listening, query count, storage and concurrent writes also passed.

```powershell
.\gradlew.bat --offline --console=plain test --tests '*PlaylistServiceTest' --tests '*LikeServiceTest' --tests '*PilotAccessTest' --tests '*ListeningPilotWorkflowIntegrationTest' --tests '*TrackMutationConcurrencyIntegrationTest' --tests '*PlayableTrackQueryCountTest' --tests '*RetainedHistoryStorageIntegrationTest' --tests '*LikeControllerTest' --tests '*PlaylistControllerTest' --tests '*ListeningSecurityTest' --tests '*ListeningAuthenticatedAdminTest' bootJar
```

JUnit XML: `build/test-results/test`. JAR: `build/libs/ATStudio-0.0.1-SNAPSHOT.jar`, SHA-256 `199465b5b8f6cdb9a61e43916a305ff7a21941dcc9dfd74a04f2ec6370a83033`. An earlier invocation placed `--tests` after `bootJar`; Gradle rejected those arguments before execution. The corrected invocation above passed.

## Native database verification

The two unchanged workflow method bodies also passed on a disposable loopback MySQL 8.0.45 process. The local runner generated a test class from the current integration source, replacing the class name and H2 datasource only. JUnit reported two passes, zero failures/errors/skips, Connector/J and MySQLDialect. The recorded XML is `build/WI-20261123-ATS-013/native-kny1xp91/gradle-build/test-results/test/TEST-com.atstudio.atstudio.security.NativeLibraryWorkflowTest.xml`. This supplements the H2 tests with native database behavior.

An ordinary cleanup attempt encountered a file handle from MySQL's Windows child process. The instance-specific MySQL shutdown event stopped the identified child. The child and its local listener were confirmed absent, and the temporary initialization SQL was already removed. The execution tool blocked recursive removal of the remaining closed synthetic data directory inside the ignored build directory.

## Cross-layer review and rollback

The pilot edge and frontend authentication matcher use the same library method and path pairs as Spring. Nginx serves only HTML at the personal page addresses. Public catalog and stream requests continue without credentials. The review found that text-only multipart metadata endpoints inherited the general 120 MiB body size. WI015 set a 32 KiB body limit on those two locations and checked that track reorder retains its own limit. The participant UI uses the narrow capacity response. Account changes invalidate owner projections and clear personal playback state.

Local release candidate only. Rollback consists of using the previous matched application/SPA/proxy release. No schema migration or production record cleanup is required for this change.
