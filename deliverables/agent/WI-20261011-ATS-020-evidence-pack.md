---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: QA
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-020-handoff.md
    reason: Approved isolated packaging scope
  - path: ../user/REQ-20261011-ATS-002.md
    reason: Local-only preparation approval and successor chain
  - path: WI-20261011-ATS-018-evidence-pack.md
    reason: Superseding independent local PASS
  - path: WI-20261011-ATS-019-evidence-pack.md
    reason: Exact-source independent 87-test regression
---

# Evidence Pack: WI-20261011-ATS-020

> Purpose: Identify the new background-switch executable and reusable SPA without changing or deploying historical artifacts.

## Summary

**LOCAL ARTIFACT PASS; target application/runtime checks NOT RUN.** Offline
`compileJava` and `bootJar` succeeded. Use the new path below for future
separately approved staging, not the retained WI026 backend path.

| Artifact | Exact identity |
|---|---|
| New executable | `build/WI020-release-v3/build/libs/ATStudio-0.0.1-SNAPSHOT.jar` |
| Bytes | 70,306,351 |
| SHA-256 | `01d5623972e17d0b6e1d3e532096239cd87e9995a08771c59292c94bf3699dcb` |
| New public manifest | `deliverables/agent/WI-20261011-ATS-020-artifact.json` |
| Manifest SHA-256 | `8a74e29f15708dad5a8278dffc3e8add1eb7b8dbcd6ddb18045ec85c32c63fe6` |
| Reusable SPA directory | `build/WI-20261010-ATS-026-frontend-dist` (143 files; separately inventoried) |

Verification owner QA; Windows 11 / PowerShell 7.6.5, Gradle 9.3.0,
Java 17.0.12+8-LTS-286, Spring Boot 4.0.8. Actual build window:
**2026-10-11 11:25:45.728 through 11:26:15.309 KST**. Gradle reported 29s,
exit0, four actionable tasks executed. Branch `main`, HEAD
`58b1011dad796017e47dbcc82a4225ca449bcdf6`; existing dirty worktree retained.

## Scope / DoD Check

- [x] Approved REQ, actual WI020 handoff and current prerequisite completions read.
- [x] WI018 latest PASS recognized as superseding its preserved historical HOLD.
- [x] WI018 19 exact public pins and all 654 WI019 snapshot entries rehashed.
- [x] WI019 result/snapshot pins and ten XML receipts checked: 87/87, no failures/errors/skips. Not rerun.
- [x] Fresh isolated Gradle outputs and project cache; no clean, full build, test or app-launch task.
- [x] Backend input paths, sizes and SHA-256 captured before/after: 641 unchanged.
- [x] Executable launcher, Java17 classes, background-switch classes and all four public resources verified.
- [x] Archive duplicate/private-name checks and old/new dependency/model comparisons passed.
- [x] Historical JAR/manifest, previous class/XML/JAR evidence and SPA bytes preserved.
- [x] One new public manifest and both report sets produced; WI021 is next.

## Packaging and Preservation Evidence

| Check | Result |
|---|---|
| Packaged application classes | 615; all major version61; exact set/hash match to fresh compiled output |
| Background switch | Both new configuration classes plus nested `EnabledScheduling` present; updated worker/recovery classes present |
| Executable metadata | `JarLauncher`, `Start-Class: com.atstudio.atstudio.AtStudioApplication`, Boot4.0.8 |
| Public resources | Exact set and bytes match current `src/main/resources`: application.yml, application-acceptance.yml, schema.sql, seed.sql |
| Packaged YAML SHA-256 | `c3bd0e1571ffbe9e35ec5b77e6197fb91ca9baa602fb952609de4c07efd4bf55` |
| Duplicate/private-named archive entries | 0 / 0; bounded filename check, not a generic secret-detection claim |
| Libraries vs WI026 | 101/101 identical paths, sizes and hashes; no added/removed/changed dependencies |
| Class delta vs WI026 | 3 added, 6 changed, 0 removed; exact names in manifest |
| Schema/model comparison | 136 entity/related model classes identical; schema.sql and seed.sql identical |
| Frontend input identity | Exact historical 336-file set/hash match; no frontend build or private env read |
| SPA identity | Exact 143-file set/hash match at separate reusable directory |
| Preservation inventory | 7,912 public files unchanged, including old build classes/XMLs/JARs, prior SPA, saved JARs and prereq receipts |

The six changed compiled entries are AtStudioApplication, TrackAudioProcessingWorker,
StorageMutationRecoveryService and its three nested/synthetic companion classes.
The added entries are BackgroundTasksConfiguration, its EnabledScheduling class,
and BackgroundTasksEnabledCondition. No unrelated old class changed.

Preserved historical pins:

| Path | SHA-256 |
|---|---|
| `build/libs/ATStudio-0.0.1-SNAPSHOT.jar` | `517b745479792a7eae6ce75357c7d1299ab644b50d5bc5b331fea384ccb10e0a` |
| `deliverables/agent/WI-20261010-ATS-026-artifact.json` | `fbeb74425ade0fe3882a9dddc4c97f7b67b75dda711ffa37acbd755a02fa4d9c` |

Historical frontend coverage exclusions remain exactly these four paths under
`frontend/src/test/coverage/`: `adminSubscriberGaps.coverage.test.tsx`,
`adminSubscriberPages.coverage.test.tsx`, `publicAuthShell.coverage.test.tsx`,
`shellCatalogRouterGaps.coverage.test.tsx`. Their current bytes were also preserved;
they are not retroactively added to the historical 336-file build-input claim.

## Commands and Receipts

Executed wrapper: `./build/WI020-release-audit.ps1`. Its final run uses a
cleared child environment and directly invokes the batch wrapper through
`cmd.exe /d /s /c`; no shell AutoRun or PowerShell profile is loaded.

```powershell
.\gradlew.bat --offline --no-daemon --console=plain --no-build-cache --no-configuration-cache --project-cache-dir build/WI020-release-v3/gradle-cache -Dorg.gradle.java.installations.auto-download=false -Dorg.gradle.java.installations.auto-detect=false "-Dorg.gradle.java.installations.paths=C:/Program Files/Java/jdk-17" -I build/WI020-release.init.gradle compileJava bootJar
```

Gradle's single-use daemon was requested only to honor its JVM settings; the
build receipt states it stops at completion. No reusable daemon/application
launch was requested. The task graph allowlist confines execution to compileJava,
processResources, classes, resolveMainClassName and bootJar; outputs must be
under the fresh WI020 directory. Existing cache sufficed; no dependency download,
installation or upgrade. Cached wrapper distribution was checked before launch.

Child environment names only: `SystemRoot`, `WINDIR`, `TEMP`, `TMP`, `PATH`,
`JAVA_HOME`, `GRADLE_USER_HOME`, `USERPROFILE`. No inherited integration values,
JVM injection variables, private config body or private hash entered the manifest.
Project/user Gradle properties and executable auto-init scripts were absent by
existence checks. Cached build tooling is trusted locally, not provenance-attested.

- [Full stdout](../../build/WI020-release-v3/receipts/gradle-stdout.log) and [stderr](../../build/WI020-release-v3/receipts/gradle-stderr.log).
- [Exact command](../../build/WI020-release-v3/receipts/command.txt), [build result](../../build/WI020-release-v3/receipts/build-result.json), [tool versions](../../build/WI020-release-v3/receipts/tool-versions.log).
- [Before source inventory](../../build/WI020-release-v3/receipts/source-before.json) and [after source inventory](../../build/WI020-release-v3/receipts/source-after.json).
- [Archive inventory](../../build/WI020-release-v3/receipts/archive-inventory.json): each entry, byte count and SHA-256, with application class major versions.
- [Preservation inventory](../../build/WI020-release-v3/receipts/preserved-before.json) and [preservation result](../../build/WI020-release-v3/receipts/preservation-result.json).
- [Init script](../../build/WI020-release.init.gradle) and [bounded build/audit wrapper](../../build/WI020-release-audit.ps1).

Two preceding wrapper attempts stopped **before Gradle** because PowerShell added
PATHEXT/PSModulePath despite the supplied environment allowlist. No app/source
defect or missing cache was involved. Their receipts remain in
`build/WI020-release/receipts/` and `build/WI020-release-v2/receipts/`; neither
contains a JAR. Removing the PowerShell child stage resolved the local tooling
guard; the final build uses fresh v3 paths. No receipt or old output was overwritten.

Reproduction requires a newly approved, fresh WI-local output path: the wrapper
refuses existing output/manifest paths. Do not rerun it over this evidence or
remove old outputs. Read-only verification can hash the manifest's public input,
artifact and receipt paths directly. `gradlew.bat --offline --no-daemon --version`
was separately run in the same cleared environment; its parsed version receipt
was added to the generated public manifest without rebuilding.

## Test Evidence and Warnings

WI020 executed packaging checks, not tests. Prerequisite evidence is attributed:
WI018 owner19/19 and independent redteam27/27; WI019 independent87/87. These
counts are separate and not presented as new QA tests. Full-suite, coverage,
SAST/current-CVE scans, frontend type/lint/build and runtime tests were NOT RUN
under this packaging-only handoff. No measured capacity or performance claim.

Final build stderr is empty; no compilation warning was emitted in this build.
Historical JUnit4.13.2 and Hamcrest runtime libraries remain byte-identical
transitive dependencies, not new test-dependency additions. No exclusion or
library change was made and no vulnerability verdict is implied.

Final scoped verification at11:29:41 KST passed9,048 hash assertions (overlapping
public record groups, not9,048 unique files), including all7,912 preservation
records. Both reports passed required metadata, whitespace/EOF and24 file-link
target checks; link anchors were not checked. Helper PowerShell syntax and scoped
`git diff --check` passed. Direct report checks cover the untracked files that
Git diff alone omits. Receipt: `build/WI020-release-v3/receipts/final-checks.json`.

## Native Boundary and Rollback

WI015 records MA's **2026-10-11 10:34:46 KST** approved Linux read-only Hibernate
PASS for the historical exact probe/model/library identity. That historical
success remains valid. Matching 136 model classes, schema/seed and all101 libraries
establishes no drift in the compared packaged mapping inputs; it does **not**
establish native validation or safe startup of the new complete executable.

Before separately approved use: bind staging/probe/config to the new artifact
identity, reconcile target configuration and fresh-schema provenance, and prove
native first-start OFF behavior, effective systemd/BPF/IPv4/IPv6/relay constraints,
mandatory guards and cleanup. Canonical fresh manifest remains a separate gate.
No full app, DB, remote, SMTP, OAuth, Provider or Linux operation occurred here.
Production remains HOLD; the false prestart gate and unresolved new-JAR candidate
placeholder were not changed. No private env, secret use, deletion, commit or push.

The pinned old `release-readiness.py` was read only to retain exact historical
coverage/model boundaries; it was neither invoked nor modified. Its old manifest
must not be patched or used as a new-release validator. WI020 intentionally
supersedes only the backend artifact selection for future approved staging.

Local rollback requires no runtime or source action. Keep all historical bytes
and the new evidence; withdrawing this candidate means withholding it from future
staging, not deploying the older JAR as an automatically safe fallback. Deletion
or remote rollback requires separate authority.

## Reference Documents and Chain

Tier0 STD-001, STD-004, STD-002 and STD-005 were fully injected in order, followed
by QA persona/security, handoff and snapshot. Quality-gates, actual REQ002,
WI020 handoff, WI018 latest repair addendum, WI019 receipts, WI015, current index
entry and relevant CONFIGURATION/production-readiness sections were read.
The older readiness matrix is historical, not a fresh target verdict. Both
build-check and create-wi-evidence-pack skills were read and applied to the
narrower explicit handoff. `.claude/config/workspace.json` confirmed ATS;
injection rule pointer: `.claude/config/context-injection-rules.json`.
Memory lookup was navigation only; current sources/receipts govern every result.

**WI020 complete; Blocks WI021. MA must immediately prepare/delegate WI021 for
current-entry integration.** No sub-delegation or cross-chat dispatch by QA;
REQ002 remains open until that integration. QA did not edit index, REQ, deployment
candidates, source, build.gradle or historical manifests.

## Related Documents

- [Public artifact manifest](WI-20261011-ATS-020-artifact.json): Exact deploy-candidate identity and scoped provenance.
- [User summary](../user/WI-20261011-ATS-020-summary.md): Compact result and limits.
- [WI020 handoff](WI-20261011-ATS-020-handoff.md): Ownership and acceptance.
- [REQ002](../user/REQ-20261011-ATS-002.md): Approval and WI chain.
- [WI018](WI-20261011-ATS-018-evidence-pack.md): Latest security clearance.
- [WI019](WI-20261011-ATS-019-evidence-pack.md): Independent regression.
- [WI015](WI-20261011-ATS-015-evidence-pack.md): Attributed historical native evidence.
