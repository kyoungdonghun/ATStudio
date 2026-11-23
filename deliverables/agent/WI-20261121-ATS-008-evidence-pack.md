# WI-20261121-ATS-008 · Independent B1 integration review

## Result

On 2026-11-21 the independent integration reviewer ran the two-participant Spring workflow against the final WI006 backend. **One integrated JUnit test passed with zero failures, errors or skips.** The run used Spring MVC, password login, JWTs, H2, JPA and original file bytes in a temporary directory. A separate authenticated administrator changed the catalog through its normal API.

| Operation | Observed outcome |
|---|---|
| Download before publication, then publish via the administrator interface | The private file returned 404 with no history or license. The administrator published both tracks at revision 1. |
| Two simultaneous first clicks by A, then an independent first download by B | A received the correct bytes twice with one history row and one license. B initially saw empty lists. B then received its own distinct bytes and record. |
| View personal records and repeat a download at the quota limit | Each participant saw only their own history. Each received 404 for the other's license detail. A received the previous file, kept its UUID and zero remaining quota, and was denied a new file. The private detail and file responses included `Cache-Control: no-store`. |
| Unpublish at revision 2, update a draft title, then republish at revision 3 | A's original download was blocked while unpublished. History, license list and detail did not reveal the draft title, including a keyword search. Upon republication A could retrieve the same bytes and UUID, see the newly published title, and retained the same quota. |
| Expire B's grant and withdraw A while A still has its previous JWT | B lost access to its file and personal records. The final candidate returned 401 on all three of A's subsequent requests for history, license detail and original file. |

The [workflow](../../src/test/java/com/atstudio/atstudio/security/ListeningPilotWorkflowIntegrationTest.java) now exercises public-catalog revisions through authenticated HTTP, personal response cache headers, republication and withdrawal. The [WI006 evidence](WI-20261121-ATS-006-evidence-pack.md) describes the two backend findings and their corrections. The initial local workflow passed before the withdrawal assertions were added. The reviewer reran the final candidate including those assertions.

MA independently ran the same final application workflow on disposable local MySQL 8.0.45, isolated to loopback with synthetic credentials. The local runner substituted the test class name and database connection for H2. It verified Connector/J, the MySQL dialect/version and the three withdrawal assertions in the executed class. The JUnit result was **one test, zero failures, errors or skips**, and the temporary database directory was removed after shutdown. This run tested the application requests on native MySQL. WI006 records the additional two-transaction ordering experiment separately.

## Two-tab browser check

The independent reviewer loaded WI007's pilot bundle (`index-CM8gnXmj.js`) in a disposable local Chrome 157.0.8103.79 profile. A submitted the login form in the first tab and saw A's distinct history entry. B submitted the login form in a second tab on the same origin. The first tab automatically replaced A's history with B's. Both tabs displayed B's entry and neither retained A's entry. Navigation, forms, storage events and rendering ran in Chrome. Playwright fulfilled loopback-origin requests with synthetic per-user responses and blocked other origins. The H2 and MySQL runs above cover server-side authorization independently. The bounded local harness and result are `build/WI-20261121-ATS-008/browser-check.cjs` and `browser-check.log` in that directory.

MA separately ran the full general-mode frontend suite. The initial 1,707/1,712 result prompted four test-file updates covering contract expectations, missing mock data and a formerly expected upload error message. All 98 tests in those files passed on rerun. Final result: 1,712/1,712 across 127 files with zero failures and skips, recorded in `build/WI-20261121-ATS-007/all-frontend-final.json`. The app bundle stayed identical to the Chrome-tested bundle. The focused WI007 result of 181 tests is reported separately.

## Proxy contract

The independent structural check matched eight pilot paths and excluded twelve adjacent paths. It inspected the opt-in [locations](../../deploy/single-vps/nginx/listening-pilot-locations.conf.template), the [listening server](../../deploy/single-vps/nginx/listening-authenticated-admin-https.conf.template), the [proxy include](../../deploy/single-vps/nginx/proxy.conf.template) and the [global zones](../../deploy/single-vps/nginx/nginx.conf.template). The three optional locations restrict methods to the GET class at the edge, including nginx's implicit HEAD behavior. Personal API calls forward authorization, caching and storage are disabled at the upstream proxy, and all eight original-download connection limits reference existing zones. The baseline listening server requires explicit inclusion of the pilot locations.

This was a template inspection. The Windows workstation had no available nginx executable, Docker CLI or installed WSL distribution. Native nginx configuration loading, TLS, proxy forwarding, device downloads and rate limits under traffic remain deployment checks.

## Reproduction

- Run `gradlew.bat --offline test --tests "*ListeningPilotWorkflowIntegrationTest"` from the repository root. For this independent run, `--project-cache-dir build/WI-20261121-ATS-008/project-cache -I build/WI-20261121-ATS-008/isolated.gradle` directed outputs to a separate local directory.
- The 39-second final Gradle invocation recompiled the changed backend and tests. The individual JUnit test took 24.958 seconds. Local outputs are `build/WI-20261121-ATS-008/final.log` and `build/WI-20261121-ATS-008/gradle-build/test-results/test/TEST-com.atstudio.atstudio.security.ListeningPilotWorkflowIntegrationTest.xml`. The earlier run is in `integration.log` in the same WI directory.
- The native application run's JUnit test took 40.521 seconds. Its local Gradle log is `build/WI-20261121-ATS-008/native-mysql-check/result.log`. Raw reports stay under the ignored build directory.
- The [handoff](WI-20261121-ATS-008-handoff.md) identifies the Tier 0 standards, security and quality policies, current roadmap and the WI003–005 author evidence. The approved [review scope](../user/REQ-20261121-ATS-003.md) governs this result.
- A bounded check resolved all 20 new or revised evidence, requirement, roadmap and decision-entry links, including their anchors. The repository-wide documentation validator reported 5,748 link errors and four index warnings both before and after the decision-entry update. These include pre-existing reconstructed documents, archived build outputs and shell snippets parsed as links. Logs are `docs-prior.log` and `docs-final.log` under the ignored WI008 build directory.

## Rollback and limits

WI008 extended one local test and recorded its evidence. Reversing only the review-specific assertions restores the earlier test coverage. The application fixes and their rollback boundaries belong to WI006 and WI007. This test used local synthetic accounts and temporary media. Live end-to-end and participant-device checks require a separately approved deployment.
