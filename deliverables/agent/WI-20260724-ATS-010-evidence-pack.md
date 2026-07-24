---
version: 1.0
last_updated: 2026-07-24
project: ATS
owner: qa
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20260724-ATS-010-handoff.md
    reason: WI execution contract
  - path: ../user/REQ-20260724-ATS-002.md
    reason: Approved release rehearsal scope
  - path: ../../docs/standards/core-principles.md
    reason: Tier 0 constitution
  - path: ../../docs/standards/development-standards.md
    reason: Tier 0 development and quality rules
  - path: ../../docs/policies/quality-gates.md
    reason: Tier 1 operational quality checklist
---

# Evidence Pack: WI-20260724-ATS-010

## Summary

- PASS: reproduced and verified the backend baseline from the remote official branch in an independent fresh clone.

## Scope / DoD Check

- [x] Cloned only remote branch `codex/p1-acceptance-hardening` with `--single-branch --no-tags`.
- [x] Verified remote and clone HEAD as `3147873c42bfd7883fdaa92922c0485e5fc72621` before testing.
- [x] Verified Java 17 and Gradle 9.3.0 project contracts.
- [x] Ran `clean check bootJar` with a fresh clone-local Gradle user home.
- [x] Recorded exact JUnit totals, failures, errors, skips, and suite time.
- [x] Verified bundle and critical-class JaCoCo thresholds.
- [x] Verified executable Spring Boot JAR structure and hash.
- [x] Rechecked remote/clone identity and bounded clone status after verification.
- [x] Avoided DB mutation, external Provider calls, runtime source edits, commits, and pushes.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and execution boundaries |
| 0 | `docs/standards/development-standards.md` | Java 17, Gradle, test, coverage, and evidence rules |
| 1 | `docs/policies/quality-gates.md` | Operational quality checklist |
| Context | `deliverables/user/REQ-20260724-ATS-002.md` | Approved release rehearsal scope |
| Context | `docs/SR/SR-93.md` | Production-readiness boundary |
| Contract | `deliverables/agent/WI-20260724-ATS-010-handoff.md` | Clone path, commit, command, and output contract |
| Build | `build.gradle`, `gradlew.bat`, `settings.gradle` | Toolchain and task definitions |

## Clone Evidence

| Evidence | Value |
|---|---|
| Repository | `https://github.com/kyoungdonghun/ATStudio.git` |
| Clone path | `C:\Users\jm991\AppData\Local\ATStudio\release-rehearsal-3147873-20260724` |
| Remote branch | `codex/p1-acceptance-hardening` |
| Initial remote HEAD | `3147873c42bfd7883fdaa92922c0485e5fc72621` |
| Initial clone HEAD | `3147873c42bfd7883fdaa92922c0485e5fc72621` |
| Final remote HEAD | `3147873c42bfd7883fdaa92922c0485e5fc72621` |
| Final clone HEAD | `3147873c42bfd7883fdaa92922c0485e5fc72621` |
| Fetch refspec | `+refs/heads/codex/p1-acceptance-hardening:refs/remotes/origin/codex/p1-acceptance-hardening` |
| Local tag count | 0 |

Clone command:

```powershell
git clone --single-branch --branch codex/p1-acceptance-hardening --no-tags https://github.com/kyoungdonghun/ATStudio.git C:\Users\jm991\AppData\Local\ATStudio\release-rehearsal-3147873-20260724
```

Pre-gate state:

- `build/` absent.
- Project `.gradle/` absent.
- `.qa-gradle-user-home/` absent.
- Target path did not exist before clone.
- No files, configuration, build output, ignored assets, or secrets were copied from the official workspace.

## Toolchain

| Tool | Exact result |
|---|---|
| Git | `git version 2.53.0.windows.1` |
| Java | `java version "17.0.12" 2024-07-16 LTS` |
| Java runtime | `Java(TM) SE Runtime Environment (build 17.0.12+8-LTS-286)` |
| JVM | `Java HotSpot(TM) 64-Bit Server VM (build 17.0.12+8-LTS-286)` |
| Gradle | 9.3.0, revision `701205ed2f78811508466c8e1952304c2ea869f5` |
| Gradle launcher JVM | Oracle 17.0.12 |
| OS | Windows 11 10.0 amd64 |

Isolation:

```powershell
$env:GRADLE_USER_HOME = 'C:\Users\jm991\AppData\Local\ATStudio\release-rehearsal-3147873-20260724\.qa-gradle-user-home'
```

The Gradle 9.3.0 distribution and dependencies were obtained into this previously absent directory. The existing global Gradle dependency cache was not used.

## Commands And Results

Primary gate:

```powershell
.\gradlew.bat clean check bootJar --no-daemon --console=plain
```

Result:

- Start: `2026-07-24T13:00:27.2574969+09:00`
- End: `2026-07-24T13:02:50.2167909+09:00`
- Measured duration: 142.959 seconds
- Exit code: 0
- Gradle: `BUILD SUCCESSFUL in 2m 22s`
- Tasks: 10 actionable, 9 executed, 1 up-to-date
- `jacocoTestReport`, `jacocoTestCoverageVerification`, `check`, and `bootJar` passed.
- Compilation emitted an unchecked/unsafe operations note.
- The test JVM emitted a class-data-sharing limitation warning.
- No build failure or coverage violation was reported.

## Tests

JUnit XML source: clone `build/test-results/test/TEST-*.xml`.

| Metric | Exact count |
|---|---:|
| XML suite files | 158 |
| Total | 1,208 |
| Passed | 1,199 |
| Failed | 0 |
| Errors | 0 |
| Skipped | 9 |
| Aggregate suite time | 77.030 seconds |

Skip classification:

| Count | Classification | Evidence |
|---:|---|---|
| 7 | Environment-conditional MySQL concurrency proof | `PaymentMysqlConcurrencyIntegrationTest` is enabled only when `ATSTUDIO_MYSQL_PROOF_ENABLED=true`; races 1-7 skipped |
| 1 | Environment-conditional MySQL schema proof | `PaymentMysqlSchemaValidationTest` uses the same condition; Hibernate disposable-schema validation skipped |
| 1 | Windows symbolic-link capability unavailable | `LocalStorageServiceTest.rejectsDirectoriesAndSymbolicLinks()` aborted with `Symbolic links are unavailable in this environment` |

`ATSTUDIO_MYSQL_PROOF_ENABLED`, target, URL, username, and password variables were absent. The eight MySQL tests are assigned to WI-20260724-ATS-013 and are not counted as MySQL validation in this WI.

## Coverage

JaCoCo XML source: clone `build/reports/jacoco/test/jacocoTestReport.xml`.

| Metric | Covered | Missed | Total | Coverage | Threshold | Result |
|---|---:|---:|---:|---:|---:|---|
| Instruction | 38,778 | 6,485 | 45,263 | 85.6726% | 80% standard equivalent | PASS |
| Branch | 2,830 | 1,118 | 3,948 | 71.6819% | 70% | PASS |
| Line | 8,528 | 1,420 | 9,948 | 85.7258% | 80% | PASS |
| Method | 1,511 | 311 | 1,822 | 82.9308% | 80% | PASS |
| Complexity | 2,498 | 1,316 | 3,814 | 65.4955% | Informational | N/A |
| Class | 345 | 24 | 369 | 93.4959% | Informational | N/A |

Critical security class coverage:

| Class | Line | Method |
|---|---:|---:|
| `JwtConfig` | 100% (10/10) | 100% (2/2) |
| `AuthRateLimitFilter` | 100% (95/95) | 100% (17/17) |
| `CustomUserDetailsService` | 100% (10/10) | 100% (4/4) |
| `JwtAuthenticationFilter` | 100% (22/22) | 100% (2/2) |
| `JwtTokenProvider` | 100% (36/36) | 100% (9/9) |
| `AuthService` | 100% (44/44) | 100% (6/6) |
| `BillingKeyCrypto` | 100% (97/97) | 100% (17/17) |

## Executable JAR Evidence

| Item | Result |
|---|---|
| Path | Clone `build/libs/ATStudio-0.0.1-SNAPSHOT.jar` |
| Size | 69,737,281 bytes |
| SHA-256 | `20978ae645577c13b999b3ff4096f567f17da7f8b0e2a180f858104593ea4da5` |
| ZIP entries | 787 |
| Spring Boot loader entries | 112 |
| Manifest `Main-Class` | `org.springframework.boot.loader.launch.JarLauncher` |
| Manifest `Start-Class` | `com.atstudio.atstudio.AtStudioApplication` |
| Application class | `BOOT-INF/classes/com/atstudio/atstudio/AtStudioApplication.class` present |

The archive and manifest were parsed with .NET ZIP APIs. The JAR was not launched because database/runtime smoke belongs to later WIs and this WI forbids DB interaction.

## Final Clone Status

Bounded read-only checks:

```text
git status --short --untracked-files=normal
?? .qa-gradle-user-home/
```

- Tracked worktree diff names: 0.
- Staged diff names: 0.
- Non-ignored untracked files: 2,069.
- Their source is the newly generated dedicated Gradle user home.
- `build/` and project `.gradle/` are ignored generated outputs.
- `.qa-gradle-user-home/` is not ignored.
- HEAD and branch remained unchanged.
- A bounded `git ls-files` count emitted one Windows filename-length warning while traversing the Gradle metadata cache; it did not affect build or Git object state.

This state is disclosed rather than hidden or cleaned because the clone and its generated evidence are inputs to dependent rehearsal WIs.

## Files Written

- `deliverables/user/WI-20260724-ATS-010-summary.md`
- `deliverables/agent/WI-20260724-ATS-010-evidence-pack.md`

Runtime source changes: none.

## Risks / Rollback / Cleanup

- The eight MySQL proof skips remain explicitly pending for WI-20260724-ATS-013.
- Symbolic-link rejection remains unexecuted on this host due to local capability.
- The clone and generated Gradle/build outputs are retained for WI-011, WI-012, and WI-013.
- Final cleanup ownership: MA/final rehearsal cleanup WI. Remove only the exact clone path after dependent WIs finish and destructive cleanup is authorized.
- No database, external Provider, source branch, commit, or remote state requires rollback.

## Follow-ups

- WI-20260724-ATS-011, WI-20260724-ATS-012, and WI-20260724-ATS-013 are unblocked by this PASS result.
