# WI-20261121-ATS-003 backend implementation and author verification

[Approved requirement](../user/REQ-20261121-ATS-002.md) · [Doc and source context](WI-20261121-ATS-003-handoff.md).

## Implementation

- [Default-off settings](../../src/main/java/com/atstudio/atstudio/config/ListeningProperties.java) require both listening and pilot mode, plus named user IDs. Existing source/trusted-proxy admission still applies. [HTTP method/path guard](../../src/main/java/com/atstudio/atstudio/security/ListeningAccessFilter.java) and [role/eligibility checks](../../src/main/java/com/atstudio/atstudio/config/ListeningSecurityConfiguration.java) retain separate administrator and public surfaces. Private responses use no-store.
- [Eligibility](../../src/main/java/com/atstudio/atstudio/service/PilotAccess.java) checks verified, nondeleted USER, finite active plan, inclusive KST grant dates, CANCELLED nonrenewing fixture, no pending plan/cycle and no billing agreements, orders or subscription payments. Login and refresh apply eligibility before issuing a trial session. Clearing eligibility denies subsequent protected requests, including existing access-token requests.
- [Download decision](../../src/main/java/com/atstudio/atstudio/service/DownloadService.java) under the user lock checks the locked grant and catalog snapshot before locking the track and applying the existing first/repeat policy. New downloads count into a half-open KST day; repeats retain their existing UUID and ledgers. No second download-catalog flag.
- Personal history/search, license list/detail tie to the authenticated owner. Unpublished/inactive historical items retain issued records; the response replaces current track information with a placeholder. Keyword matches exclude private metadata. Bulk ID and admin lookup endpoints are outside pilot.
- No schema change, scheduled task, external activity or user provisioning. Earlier A behavior remains the default. Application rollback for a future server change requires restoring the known prior deployment; removing the opt-in returns to the listening-only policy after reload.

## Verification

Author-focused tests use the existing Gradle test profile and [pilot unit tests](../../src/test/java/com/atstudio/atstudio/service/PilotAccessTest.java). The separate WI005 owns the realistic two-user workflow. Do not add overlapping test counts across WIs. This is local author-side verification. Independent review is pending.

The final Gradle XML records 80 tests across seven suites with zero failures, errors or skips. The isolated output directory was `build/wi003-pilot-verification/test-results/test`. Reproduce with the project wrapper in offline mode and `test --tests "*PilotAccessTest" --tests "*DownloadServiceTest" --tests "*LicenseServiceTest" --tests "*Listening*Security*" --tests "*UtilServiceTest" --tests "*AuthServiceTest"`. The local init script only relocated build outputs to keep concurrent author/integration results separate. The four new tests cover cohort defaults and enrollment, date/status/limit conditions, exclusion of paid histories and the KST day interval.
