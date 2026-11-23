# WI-20261121-ATS-005 · Pilot workflow and edge admission

REQ-20261121-ATS-002, author-side verification. The [handoff](WI-20261121-ATS-005-handoff.md) identifies the standards and approved scope.

## Test fixture

The [Spring integration scenario](../../src/test/java/com/atstudio/atstudio/security/ListeningPilotWorkflowIntegrationTest.java) uses MockMvc with real password login, JWT parsing, Spring Security, repositories, H2 and two distinct synthetic MP3 files in JUnit temporary directories. A synthetic administrator owns both tracks. Two separate verified USER accounts each receive a finite nonbilling daily-one grant. Assertions exercise distinct file bytes, first and repeat download, daily quota, separate history and licenses, stable UUID, other-owner denial, and disabling file access on unpublish or expiry. The test also checks that a previously issued personal record does not disclose a later private title and that the expired grant's existing session cannot read personal pilot data.

## Proxy contract

The [optional pilot locations](../../deploy/single-vps/nginx/listening-pilot-locations.conf.template) are an addition to the authenticated listening server configuration. At future deployment, a reviewed server-context include must load them alongside the actual deployed cache/header policy. The current ordinary listening template does not include them. They permit only GET-class SPA and backend routes, forward private requests through the existing proxy, and use the established large-file rate and connection limits. Spring enforces authentication, publication and grant rules.

For the local static check, collect each anchored location regex and match the paths for downloads, license list/detail, history, quota, and a single-track file (eight positive paths). Thirteen off-scope paths including bulk history IDs, uploads, payments, user administration, invalid IDs and trailing path components matched none. All three blocks include a method restriction. The API blocks reference the existing proxy, the download limits match the selling template, and the existing listening template remains opt-in independent. No native nginx executable was found in the local tool path.

## Reproduction and boundaries

The final isolated Gradle run passed the complete workflow (1 case, 0 failures/errors/skips). Two concurrently released first-download requests both returned the correct original bytes; only one download and one license existed before the second account's first request. The final workflow also checked quota 0 remaining, owner-scoped license UUID and redaction of the subsequent private edit. The [JUnit report](../../build/WI-20261121-ATS-005/gradle-build/test-results/test/TEST-com.atstudio.atstudio.security.ListeningPilotWorkflowIntegrationTest.xml) belongs to this candidate. An earlier shared-output run failed test discovery with missing class files during parallel builds. It did not run the workflow. The isolated build and project cache avoid the conflicting outputs.

Run `gradlew.bat --offline test --tests "*ListeningPilotWorkflowIntegrationTest"` against a dedicated Gradle build directory when another build is using the workspace. This test runs the real MVC request chain in-process without listening on a network port. It excludes live nginx, public HTTPS, browser/phone, production MySQL, external mail/PG and real accounts. Leave the optional nginx include disabled for rollback to the previous edge surface.
