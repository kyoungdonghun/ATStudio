---
version: 1.2
last_updated: 2026-09-09
project: ATS
owner: se
category: reference
status: active
---

# Evidence Pack: WI-20260909-ATS-005

> Implementation and lockfile are ready for MA verification. No Java or frontend test/build runner was executed by this assignee; this is not release approval.

Reopened narrowly after MA verification: the first build stopped at the synthetic InputBuffer lambda compilation error; the second compiled and ran tests, exposing nine identical Content-Type initialization failures in the fixture. Only the owned test and these two reports were corrected. The latest fixture is stable for MA focused/full retry; no runner was executed by this assignee.

## Summary

DEP-01: Spring Boot 4.0.2 -> 4.0.8 using its BOM, without component overrides. DEP-02: compatible development lockfile corrections. OPS-01: finite 128-part connector boundary and ten synthetic parser/response-contract cases, awaiting MA execution of the corrected fixture.

## Scope / DoD Check

- [x] Approved REQ and assigned handoff read before implementation; no subdelegation.
- [x] Product changes restricted to build.gradle, frontend/package-lock.json, AppConfig.java, and its new focused test.
- [x] Actual frontend form builders inspected; repeated tag fields and notice replacement retained.
- [x] No direct dependency range, business validation, handler, schema, policy, installed package version, or production configuration change.
- [x] Lock-only audit and static diff/semver checks executed; two-set reports supplied.
- [x] MA second run compiled/executed the class: one configuration case passed, nine fixture cases failed before parsing.
- [ ] Corrected fixture/parser regressions pass; original-code red run performed.
- [ ] Patched full backend build/coverage and isolated frontend quality gates completed by MA.
- [ ] Independent WI012 review and downstream WI006/007/008/009/014 closure.

Unexpected npm metadata side effect: the authorized lock-only command rewrote live `frontend/node_modules/.package-lock.json`, although the 15 affected installed package manifests still report their old versions. Do not interpret the checked item above as a byte-for-byte preservation claim for the entire node_modules tree. No corrective live install or hidden-lock repair was attempted.

## Reference Documents (Tier 0-2)

| Tier | Document | Applied scope |
|---|---|---|
| 0 | docs/standards/core-principles.md | Approved bounded execution, transparency, domain preservation |
| 0 | docs/standards/development-standards.md | Java 17, focused tests, no policy or transaction changes |
| 0 | docs/standards/documentation-standards.md | English, metadata, traceable two-set reports |
| 0 | docs/standards/glossary.md | Canonical domain and WI terms |
| 1 | docs/policies/security-policy.md | No secrets, PII, real provider, or runtime operations |
| 1 | docs/policies/quality-gates.md | Preserve existing gates and distinguish unexecuted checks |
| 2 | deliverables/user/REQ-20260909-ATS-001.md | Approved DEP-01/02 and OPS-01 scope |
| 2 | deliverables/agent/WI-20260909-ATS-005-handoff.md | Owned paths, version authorization, runner serialization |
| 2 | deliverables/agent/WI-20260908-ATS-018-findings.md | Dependency/resource findings; historical evidence preserved |
| 2 | docs/design/api-spec.md; docs/design/usecase/sound-tag.md | Existing form/tag contract |
| Skill | .agents/skills/create-wi-evidence-pack/SKILL.md | This evidence-pack structure; handoff precondition satisfied |
| Skill | .agents/skills/react-best-practices/SKILL.md | Frontend scope context only; no React implementation edits |

Injection rule source: `.claude/config/context-injection-rules.json`; assignee `se`, task types implementation/security, agent_required_tiers `[0]`, handoff additionally includes security/quality policies. Only relevant portions of longer linked documents were reviewed. Historical WI015/016/017 packets were not reopened for unrelated auth/payment/storage investigation.

## Evidence Pointers

| Finding | Changed path | Location / purpose |
|---|---|---|
| DEP-01 | build.gradle | Line 4: only Boot plugin version changed |
| DEP-02 | frontend/package-lock.json | 15 existing dev package records changed; 52 added / 52 removed lines |
| OPS-01 | src/main/java/com/atstudio/atstudio/config/AppConfig.java | Lines 13, 21-31: 128 total parts, rationale, real Connector setter |
| OPS-01 | src/test/java/com/atstudio/atstudio/config/AppConfigMultipartBoundaryTest.java | Lines 54-171: limit, valid forms, exact boundary, overflow, real resolver/handler and unlimited control; request helper implements the real InputBuffer contract |
| Report | deliverables/user/WI-20260909-ATS-005-summary.md | User-facing implementation/verification boundary |

### Official Dependency Evidence

Verified on 2026-09-09, consistent with MA's handoff:

- [Spring Boot documentation](https://docs.spring.io/spring-boot/index.html): 4.0.8 is stable; 4.0.9-SNAPSHOT is separate. No 4.1 migration.
- [Published Boot 4.0.8 BOM](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/4.0.8/spring-boot-dependencies-4.0.8.pom): parsed with PowerShell XML; Tomcat 11.0.24, Framework 7.0.9, Security 7.0.7. This is published metadata, not a claim about resolved Gradle artifacts or the pinned running JAR.

### Multipart Accounting

| Current builder | Maximum fixed/repeated shape inspected | Boundary consequence |
|---|---|---|
| TrackUploadPage.tsx:262-274 | Four scalar fields + audio + thumbnail + N separately appended tagIds; selected tracks upload as separate sequential requests | 6 + N; 122 tag parts fit |
| TrackEditPage.tsx:198-213 | Six scalar fields including isActive/replaceTags + two optional files + N separate tagIds | 8 + N; 120 tag parts fit |
| frontend/src/api/notices.ts:78-86 | title/content/isPinned + five deleteAttachmentIds + five newAttachments | 13 parts; regression included |
| frontend/src/api/questions.ts:90-96 | Four scalar fields + five attachments | 9 parts; regression included |
| frontend/src/api/companyCerts.ts:31-46 | Ten separately appended documents | 10 parts; regression included |
| Albums/playlists/settlement import builders | At most three/three/two fixed parts | Below 128; source-reviewed, no separate parser case |

The 128-part setting is an explicit transport budget with tag headroom, not a discovered business maximum. DTOs and tag selection currently have no explicit tag-count product limit. The actual live catalog was not queried. Forms beyond 128 parts are now rejected; no promise of unlimited tag selection is made. Existing request/file bytes, parameter count, part-header size, service attachment checks, and tag policies are unchanged.

### Actual Rejection Path

[Tomcat 11.0.24 Request source](https://github.com/apache/tomcat/blob/11.0.24/java/org/apache/catalina/connector/Request.java) connects `maxPartCount` to the real multipart parser. Overflow throws `InvalidParameterException` with status 413 and `FileCountLimitExceededException` as cause; it does not return a silently truncated successful form.

[Framework 7.0.9 multipart adapter](https://github.com/spring-projects/spring-framework/blob/v7.0.9/spring-web/src/main/java/org/springframework/web/multipart/support/StandardMultipartHttpServletRequest.java) recognizes a count-limit cause and throws `MaxUploadSizeExceededException`. Current `GlobalExceptionHandler.java:96-97,119-121,145-153` maps that to existing `BUSINESS_ERROR.IO_LARGE`, defined at `BUSINESS_ERROR.java:52-55`.

Expected existing application response fields: `status=413`, `error=HttpStatus.CONTENT_TOO_LARGE.getReasonPhrase()`, `errorCode=IO_LARGE`, and `message=BUSINESS_ERROR.IO_LARGE.getClientMessage()` (the existing Korean file-size-limit message). The test calls the real Spring resolver and production handler; it introduces no synthetic error handler or special success/exception mapping. This is a composed in-process contract test, not an executed HTTP/DispatcherServlet/proxy test. No handler change was necessary. General non-size `MultipartException` still follows the pre-existing generic fallback; no broader exception-policy change was made.

## Commands & Outputs

Executed by this assignee:

1. `npm.cmd audit fix --package-lock-only --ignore-scripts` in live frontend: exit 0, audited 323 packages, 0 vulnerabilities. No `--force`, npm install/ci, lifecycle scripts, or test runner.
2. Read-only JSON diff against `git show HEAD:frontend/package-lock.json`: exactly 15 changed existing package records, all dev; package-entry set and root dependency declarations unchanged. This HEAD comparison was valid because owned files had no initial diff.
3. Read-only Node semver inspection: all 16 changed dependency edges satisfy their parent ranges; changed engine ranges match local Node v24.14.0. In particular brace-expansion 5.0.9 no longer supports Node 18; current tooling already uses a later Node. This is not execution/compatibility proof.
4. Read-only installed package inspection: all 15 affected manifests retain old versions. `package.json` SHA-256 unchanged. Full node_modules byte hashes were not captured.
5. Scoped `git diff --check`: exit 0 for tracked owned changes. New test/report whitespace was checked separately as text, not by executing Java.
6. Read-only official-source requests, BOM XML parsing, code/doc searches, and diff/hash inspection.

| Package group | Old -> new lock version |
|---|---|
| brace-expansion, four nested records | 1.1.16 -> 1.1.18 |
| brace-expansion, root | 5.0.7 -> 5.0.9 |
| browserslist | 4.28.6 -> 4.28.9 |
| js-yaml | 4.3.0 -> 4.3.2 |
| nanoid | 3.3.16 -> 3.3.18 |
| postcss | 8.5.19 -> 8.5.28 |
| undici | 7.28.0 -> 7.29.1 |
| Required Browserslist data/tools | baseline-browser-mapping 2.10.43 -> 2.11.21; caniuse-lite 1.0.30001805 -> 1.0.30001810; electron-to-chromium 1.5.392 -> 1.5.424; node-releases 2.0.51 -> 2.0.54; update-browserslist-db 1.2.3 -> 1.3.2 |

The retained audit baseline is `output/security-readiness-20260908/npm-audit.json`: six groups, five high / one moderate. The new audit result describes the lockfile, not patched live Vite dependencies.

### Snapshot Identity And npm Side Effect

| File | SHA-256 after implementation |
|---|---|
| build.gradle | 2B0CBDB0FC2413065E2CEC101501337A8FDE3A475EA02D1C452B8A432EF0CF95 |
| AppConfig.java | 22A5A845759309F706D4C000367CD340E921A94A82CD437917D8CE505641F546 |
| AppConfigMultipartBoundaryTest.java, after MIME-header fixture correction | 2C7C6ED3D70F1CCD74D40E925E4EC20D5A504D68704E73CF4DC0052935C7E610 |
| frontend/package-lock.json | 0BD31892B1D7A2A84A1BF7AFC0FC69772ED6685E46AC513B2E8F27E2A52FDA92 |
| frontend/package.json, unchanged | 81B927632F8CD435F2A3BE1C19FA9614EE6AA300889989BFDB1AA8EF1B20529C |

Original root lock hash: `9116AB77E40BAF4E5CC162BE5694508EC21F964C0C7272D2492119B6B48C0FE2`.

Live hidden lock hash changed from `82BB46CD22F8D694FA546082529C91B613E8CE2AA440A60DC4A526979C23DEE6` to `B79D369D67D16867E42D1FAEA81EF74ED57A053F48A60460FFC54F8AD4867DF7`. Both root and hidden lock timestamps became 2026-09-09 04:06:43 local, while inspected installed package timestamps/versions remained old. Hidden metadata now lists new lock versions, so it must not be used as proof of installed versions. MA was notified before snapshot. No hidden-lock deletion/restoration was performed without ownership authorization.

## Tests And MA Requested Commands

All ten Java cases are **NOT RUN by this assignee**. They use real Connector/Coyote/Request parsing, raw synthetic multipart bytes, mocked container metadata, and JUnit-owned temp storage. There is no application startup, socket listener, DB, credential, provider, mail, or real media access. The unlimited control is designed to accept the same 129-part fixture with `maxPartCount=-1`; a full original-source red run remains unexecuted.

### MA First Patched Build And Owned Test Correction

- Observed MA log: `output/release-remediation-20260909/backend-full-first.log`. Tasks compileJava, bootJar, jar, and assemble completed; compileTestJava failed after 50 seconds with `InputBuffer is not a functional interface` at the original test line 196. No test or JaCoCo result was produced in that run. Product artifact creation is not a passed full build.
- Root cause: the test incorrectly treated Tomcat 11.0.24 `org.apache.coyote.InputBuffer` as a single-method interface. The assignee's lambda was invalid; production AppConfig was not the cause.
- Inspected the actual resolved JAR using `C:\Program Files\Java\jdk-17\bin\javap.exe`: `C:\Users\jm991\.gradle\caches\modules-2\files-2.1\org.apache.tomcat.embed\tomcat-embed-core\11.0.24\172bb449cba1a8ffcffa249727907602a4cc899\tomcat-embed-core-11.0.24.jar`. The initial unqualified javap command was unavailable on PATH; the absolute JDK command succeeded with exit 0.
- `javap -classpath <the exact JAR above> org.apache.coyote.InputBuffer org.apache.tomcat.util.net.ApplicationBufferHandler` reports two InputBuffer methods: `int doRead(ApplicationBufferHandler) throws IOException` and `int available()`. There is no additional read overload in this resolved interface. The [official tagged source](https://github.com/apache/tomcat/blob/11.0.24/java/org/apache/coyote/InputBuffer.java) agrees.
- Correction: replace the lambda with an explicit InputBuffer implementation. `doRead` shares the synthetic ByteBuffer with the consumer, returns its readable byte count, and returns -1 after exhaustion. `available` reports remaining buffered bytes, including zero at exhaustion. The in-memory implementation need not declare IOException. Real Tomcat parsing, Spring resolution, production error handling, and all ten scenarios remain unchanged.
- Scope on reopen: only `AppConfigMultipartBoundaryTest.java` and this WI's two reports. No build.gradle, AppConfig, lock, handler, runtime, or installed dependency writes; no Gradle, compiler, or test runner invoked by this assignee.
- Superseded original test hash: `4DE592FC331CDB3B11F79BBC23AA318960E357890A913686D1BBFC06871AC660`. The first compile correction produced `2F4B6F161A22B117891C41AA09185D6A26EEAC7DFA80CB15EB00D6D1ED9DBF3F`; MA's next run compiled that version but exposed the separate fixture error below.

### MA Second Build And MIME-header Fixture Correction

- Completed log: `output/release-remediation-20260909/backend-full-second.log`, BUILD FAILED in 2m 38s. Total 1,825: 1,797 passed, nine failed, 19 skipped. This is not a passed full build or coverage gate.
- Inspected structured XML: `build/test-results/test/TEST-com.atstudio.atstudio.config.AppConfigMultipartBoundaryTest.xml`, ten cases, nine failures, zero errors/skips. The customizer case passed. All nine failures have the same root: `Request.setContentType(Request.java:709)` throws because `contentTypeMB` is null, from fixture line 195. They fail during request setup, before the actual multipart parser runs.
- Read-only `javap -c -p org.apache.coyote.Request` against the exact resolved JAR above confirmed: `setContentType(String)` dereferences the existing contentTypeMB; `contentType()` lazily loads the MessageBytes from `headers.getValue("content-type")`. `setResponse(Response)` also calls `Response.setRequest(this)`, so the existing request/response wiring is already reciprocal.
- Minimal correction: populate `coyoteRequest.getMimeHeaders().setValue("content-type").setString(...)` with the real multipart boundary header instead of calling the deprecated String setter on an uninitialized cache. Content length, explicit InputBuffer, TempDir, real Connector/Request parsing, production resolver/handler assertions, and 128/129 limits are unchanged. No invented protocol response, parser substitute, or production behavior was added.
- The log showed the full run had finished before this source edit. Current stable test hash is `2C7C6ED3D70F1CCD74D40E925E4EC20D5A504D68704E73CF4DC0052935C7E610`. MA was notified immediately. Only this test and the two WI reports changed on this follow-up; no runner was started here. Request MA's focused class retry before repeating full build/test/JaCoCo.

MA may run the focused class first, then the existing full gates serially from the repository root:

```powershell
.\gradlew.bat test --tests "com.atstudio.atstudio.config.AppConfigMultipartBoundaryTest"
.\gradlew.bat build
.\gradlew.bat dependencyInsight --dependency tomcat-embed-core --configuration runtimeClasspath
.\gradlew.bat dependencyInsight --dependency spring-web --configuration runtimeClasspath
.\gradlew.bat dependencyInsight --dependency spring-security-core --configuration runtimeClasspath
```

The full build includes existing tests and JaCoCo gates; do not lower thresholds. MA was informed that all owned Java/lock inputs are stable and that product/test edits are paused during the patched build. MA's earlier provisional auth/storage run is not evidence for this patch and is not counted here.

Frontend commands are for MA's isolated copy under `%LOCALAPPDATA%/ATStudio/validation/<unique>` only, excluding env, live node_modules, and dist. Resolve the actual copy path before running; keep logs in `output/release-remediation-20260909`. These commands were **NOT RUN here**:

```powershell
npm.cmd ci --ignore-scripts
npm.cmd test
npm.cmd run typecheck
npm.cmd run lint
npm.cmd run format
npm.cmd run build
npm.cmd audit --json
npm.cmd audit --omit=dev --json
```

No isolated installation or validation result is inferred from the live lock-only audit. Result disposition: **not-installed-in-live-Vite**.

## Risks / Rollback

- MA runs confirm product compilation/artifact creation and compilation of the first test correction. The latest MIME-header fixture retry, actual parser assertions, full regressions, and coverage remain MA gates. No running pinned JAR or server was changed.
- The transport cap does not constitute complete body/time/concurrency/proxy resource protection. Selected-target review remains necessary; no load or DoS experiment was performed.
- Limits above the inspected normal forms are intentionally finite. A future larger tag/form shape must account for all repeated parts before changing the transport budget.
- Existing `IO_LARGE` user text describes size even for a count rejection. This reuses the current response contract; no new product error taxonomy was introduced.
- Roll back only this WI's named hunks through reviewed inverse patches and remove only its owned new test/report files with explicit approval. Never reset the shared worktree or restore unrelated changes. Returning to vulnerable dependencies or unlimited parsing is not a recommended release configuration.
- Hidden npm metadata discrepancy is disclosed, not silently repaired. Any future live install/repair requires MA coordination and is outside this implementation delivery.

## Follow-ups

Return control to MA for WI012 independent review, WI006 backend gates, WI007/008/009 isolated frontend gates, then WI014 integration. No subdelegation and no REQ closure by this assignee. Implementation delivery is complete; verification acceptance is pending.

## Related Documents

- [Assigned handoff](WI-20260909-ATS-005-handoff.md)
- [Approved REQ](../user/REQ-20260909-ATS-001.md)
- [User summary](../user/WI-20260909-ATS-005-summary.md)
