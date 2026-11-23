# Public track libraries, two-user integration and edge acceptance

[Handoff and reference documents](WI-20261123-ATS-015-handoff.md) · [requirement](../user/REQ-20261123-ATS-003.md)

The test runs a real Spring application with MockMvc, H2, JWT login, password hashing, individual nonrenewing grants, catalog publication and synthetic audio files. It verifies two invited users can see only their own collections and can log in again to replay a saved track. Likes, creation limit, rename, add, reorder, remove and delete all run against persisted data. An unpublished track disappears from visible counts, likes and list details and the stream is denied. An uninvited grant holder, an expired invitee, the operator and anonymous visitors cannot use the participant library. The neighboring album, subscription details and bulk-add routes remain closed. The pre-existing original-download/license scenario also runs with its own clean application context.

| Check | Result |
|---|---|
| `gradlew.bat --offline test --tests "*ListeningPilotWorkflowIntegrationTest" --tests "*ListeningSecurityTest" --tests "*PlaylistServiceTest" --tests "*LikeServiceTest"` | 66 successes, zero failures, errors or skips. Integration 2, listening access 29, playlist service 29, like service 6. |
| `python -B deploy/single-vps/nginx/test_pilot_locations.py` | Two contract tests passed, covering the supported paths/methods and closed adjacent routes. Only playlist multipart creation/metadata updates use a 32 KiB request limit. |
| Retained deployed edge baseline | From the retained November 21 site and its later frontend-root substitution, reproduced the November 23 WI012 deployed SHA-256 `8c06a270a05b47271551c18c28e03f2c1740748672d36658325c4e87527947f0`. Replaced the single personal-locations block while retaining the published HTML cache/security headers, existing media limits and fallback; local edge candidate digest `1d2781912aee58189588bfbaa3c99a08823d19b78fc045214c4ad467f3f3b394`. |

Changed code is in `src/test/java/com/atstudio/atstudio/security/ListeningPilotWorkflowIntegrationTest.java`, `deploy/single-vps/nginx/listening-pilot-locations.conf.template` and its adjacent contract test. Local trial logs, the isolated Gradle output, and the edge-only rendering script and candidate are in `build/WI-20261123-ATS-015/`. The first test compilation found a changed Spring multipart builder type; the shared base request type fixed the test. A concurrent DTO change temporarily prevented compilation of a pre-existing repository test. WI013 restored compatibility before the successful run.

The edge contract check parses the template. The local machine has no configured native nginx runtime; this record does not attest to a native nginx configuration check, a running reverse proxy, production deployment, or a real phone. A deployment must pair the reviewed Spring, frontend and effective edge configuration, recheck current live identity, test native configuration and verify the public service. The rendered edge candidate preserves the previous release root for comparison and is not a complete deployable release.

To roll back, restore the previously prepared complete release and its matching edge configuration. The changes introduce no database schema migration; preserve participant records.
