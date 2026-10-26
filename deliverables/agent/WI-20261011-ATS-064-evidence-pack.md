---
version: 1.1
last_updated: 2026-10-11
project: ATS
owner: SE
category: evidence-pack
status: draft
dependencies:
  - path: WI-20261011-ATS-064-handoff.md
    reason: Skill-generated bounded assignment and source-identity gate
  - path: WI-20261011-ATS-059-evidence-pack.md
    reason: Audio-only suppression design and global-queue prerequisites
  - path: ../user/REQ-20261011-ATS-007.md
    reason: Approved REQ with separate MA native authority
---

# Evidence Pack: WI-20261011-ATS-064

## Current Result: Applied And Locally Verified

**SOURCE_APPLIED_FOCUSED_TESTS_PASS_REVIEW_PENDING.** On 2026-10-11 MA explicitly
released the source identity gate and authorized `apply_patch`, isolated focused
tests and an in-bounds eight-journal proof. MA reported WI060native bounded PASS,
18 state checks, 36 cleanup checks, S7 post PASS, external quarantine PASS and
app/DB stopped/masked. These are MA-attributed release evidence, not observations
made or repeated by this SE. No native operation was performed here.

The pre-application check at **21:26:50.6784191 KST** reconfirmed all635 source
hashes, both new paths absent, original patch/manifest SHA-256 and read-only
`git apply --check` PASS. Applied the eight-file candidate through `apply_patch`,
then added the eight-journal proof only to the already-owned worker test file.
All five production files match the prepared candidate; worker and transaction
algorithms, guard source, frontend and deployment tools are unchanged.

- [x] MA source gate released and six dirty preimages preserved in the bounded patch.
- [x] Source application completed: six modified files, two new files, no other source changes.
- [x] Offline isolated compilation and focused tests: **127 PASS / 0 FAIL / 0 ERROR / 0 SKIP**, eleven suites.
- [x] Two-upload success proof: four CREATE + four DELETE-intent drafts; all originals retained.
- [x] All660 captured source/build-input/retained-artifact hashes unchanged during testing.
- [x] Original629 source files outside WI064 ownership still match the preparation inventory.
- [ ] Independent exact-byte review; fresh JAR build and native use remain unauthorized here.

### Test Execution And Isolation

Environment: Windows/PowerShell, JDK17.0.12+8-LTS-286, Gradle9.3.0,
Spring Boot4.0.8. Evidence owner SE. Execution **21:31:36.6759174 through
21:32:39.0393654 KST**; exit0, Gradle BUILD SUCCESSFUL in1m1s,
five executed tasks; summed XML suite time7.661s.

```powershell
& ./build/WI064-audio-only/run-tests.ps1 -RunName focused-01
```

This completed invocation must not be rerun over its existing output. The launcher
refuses reused run names. Any approved independent rerun uses a new name and verifies
the source/helper hashes first. The exact Java/Gradle arguments, filters and explicit
nonsecret environment are retained in `build/WI064-focused-01/invocation.json`.
It invokes the installed Gradle9.3 CLI with `--offline --no-daemon`, no build/configuration
cache, two maximum workers, a new Gradle user-home containing only copied dependency
cache material, and a separate project cache. No user Gradle properties/init scripts
were loaded; project Gradle properties were absent and distribution init scripts were
checked. The child environment was cleared then populated from a literal allowlist;
the test JVM receives only five OS/temp/path environment keys. Temporary files,
classes/resources, XML/HTML and JaCoCo execution data are confined to this WI run.

The init script restricts the task graph to test compilation/resources/classes/test;
no package, clean, JAR or application task is permitted. There was no dependency
installation or external network operation. Gradle's local process coordination is
not a network-isolation proof. No DB, SpringBootTest/full app, credentials, SMTP or
native FFmpeg was used. The single-use Gradle daemon PID14300 was absent after completion.

| Executed suite | Tests | Result |
| --- | --- | --- |
| BackgroundTasksConfigurationTest | 61 | PASS |
| AudioProcessingConfigTest | 2 | PASS |
| AcceptanceStartupGuardTest | 14 | PASS |
| StorageIntegrityStartupGuardTest | 4 | PASS |
| StorageMutationRecoveryServiceTest | 8 | PASS |
| StorageMutationRecoveryVerificationTest | 1 | PASS |
| StorageMutationCoordinatorTest | 11 | PASS |
| TrackAudioProcessingWorkerIsolationTest | 3 | PASS |
| SubscriptionSchedulerTest | 6 | PASS |
| PaymentReconciliationServiceTest | 15 | PASS |
| WithdrawalBillingCleanupCoordinatorTest | 2 | PASS |

The configuration suite includes all27 master/audio-only/worker true/false/missing
combinations, six malformed audio-only inputs exercised across three master states,
strict master checks, mixed-case/whitespace boundaries, accidental scheduler imports
and custom post-processors, default-off YAML mapping, manual compatibility and guard
preservation. The malformed-mode sentinel is never created even with master=false
and worker=false. Enabled legacy mode retains exactly six scheduling registrations;
audio-only retains the audio worker but has zero scheduling post-processors and zero
storage-ready calls. Mock TaskScheduler does not execute the registered jobs.

Source pointers: `BackgroundTasksConfigurationTest.java:64,136,260,276,342,361`;
`StorageMutationRecoveryServiceTest.java:43`; worker test `:53,70,79,168` under
their corresponding `src/test/java/com/atstudio/atstudio/` packages.
The historical independent-verification-named storage suite was rerun by SE;
its name does not make this an independent review.

### Eight-Journal Proof And Limits

`TrackAudioProcessingWorkerIsolationTest.twoFreshWavSuccessPathsCreateEightJournalsWithoutDeletingOriginals`
at line79 passed. It executes real TrackService, Track entity, audio transaction,
StorageMutationCoordinator and StorageCleanupService code for two synthetic first
uploads with no thumbnail/old stream. Repositories, journal persistence, storage and
audio analysis are mocks; transaction synchronization callbacks are invoked manually.
The media payload is a one-byte placeholder handled only by mocked analysis/storage;
it is deliberately not a WAV decoding or transcoding test.

It captures exactly eight journal drafts: four CREATE and four DELETE intents, with
each original referenced by two delete intents. All CREATE IDs reach `CREATE_COMMITTED`;
all DELETE IDs reach `SHARED_REFERENCE_RETAINED`. Both Tracks are READY but inactive,
retain their original keys, have stream keys and released claims. There are zero physical
delete/deleteStaged, recovery claimBatch or encoder calls. This closes the source-level
success prediction gap; it does not prove actual DB inserts, Spring-managed commit/
rollback, file bytes, FFmpeg128kbps output, failure/retry budgets or target delivery.

The other worker tests still demonstrate global interrupted-batch recovery and claim
polling. Audio-only is not row-scoped. Native enablement still requires privately verified
zero prior pending/processing/claimed work and unresolved recovery debt, one JVM and
fenced writers. No target queue/debt check was performed or replaced by mocks.

### Review Artifacts And Preservation

- Current tested diff: `build/WI064-audio-only/audio-only-applied.patch`.
- Current eight raw source hashes, helper pins and application receipt: `build/WI064-audio-only/application-verification.json`.
- Frozen tested source copies: `build/WI064-audio-only/applied/`.
- Exact invocation and before/after660-file snapshots: `build/WI064-focused-01/invocation.json`, `before.json`, `after.json`.
- Results/logs: `build/WI064-focused-01/result.json`, `stdout.log`, `stderr.log`; XML/HTML under `output/test-results/test/` and `output/reports/tests/test/`.
- Helpers: `build/WI064-audio-only/run-tests.ps1`, `tests.init.gradle`.
- Initial reports preserved byte-for-byte in `build/WI064-audio-only/pre-application-reports/`; original patch/candidate/preimages/verification/README remain preparation history.

Tested patch SHA-256: `eb7efd4f25415310e1a9c89755589f3bef530af42e4a828a73e762ef412937c9`.
Test result SHA-256: `85c3f7456053e483fbf9f46fd4dc21fac21d9c0d5fac351e2422293398f05b49`.
`git apply --reverse --check --whitespace=error-all` on the tested patch passed without
reversal. Scoped `git diff --check` passed. Existing preimages are the patch baseline,
not HEAD; no reset, staging, commit, push or deletion was performed.

Both retained JARs remain byte-identical, including boot JAR SHA-256
`517b745479792a7eae6ce75357c7d1299ab644b50d5bc5b331fea384ccb10e0a`.
The660-file test snapshot also includes the previously retained WI016/WI019 XMLs.
It is not an exhaustive hash of every old build/native artifact. Output routing and
fresh-directory guards prevent writes to their locations.

Existing deprecated/unchecked compilation notes, JVM class-sharing warning and Gradle10
deprecation notice remain. The isolated daemon debug log recorded a registry-cleanup
shutdown-hook warning after normal completion; the CLI exited0, all tests passed and
the daemon process was absent. No product defect or new dependency change was required.
Full suite, coverage report/threshold evaluation and performance measurements were not
run; JaCoCo execution data alone is not a measured coverage claim.

**Chain check:** implementation/local verification is ready for MA's independent review
successor. No subdelegation by SE. REQ007 is not closed. Do not reuse old runtime pins
for this changed source; no JAR build, native/DB action or frontend change is authorized
by this report. Keep master=false as containment before removing audio-only on any
future runtime. Preserve data and concurrent dirty work during any reviewed rollback.

## Initial Patch-Only Checkpoint (Historical)

The following sections retain the pre-release state; the current result above supersedes
their source-not-applied and tests-NOT_RUN statements for local WI064 work only.

**PATCH_READY_ONLY; awaiting explicit MA source-identity release.** Prepared an
applyable eight-file audio-only patch and focused test additions under
`build/WI064-audio-only`. No repository `src` file was written. No Gradle test,
JAR build, native operation, network, database or private-input access occurred.
This is an initial handoff checkpoint, not full WI/REQ completion or native readiness.

## Initial Scope / DoD Check

- [x] Read the existing skill-generated handoff, approved REQ007 and WI059 SE-A design.
- [x] Preserve pre-existing dirty/untracked source; prepare only the allowed eight-path proposal.
- [x] Record six raw source preimages and two absent-new-file requirements.
- [x] Prepare default-off mapping, strict validation, master stop and scheduling/ready suppression.
- [x] Prepare truth-table, malformed-config, guard, storage and controlled worker tests.
- [x] Pass non-mutating patch applicability/whitespace check and 635-file source recheck.
- [x] Document global queue/debt gate and precise remaining eight-journal test evidence.
- [ ] Receive explicit MA source release; apply the reviewed patch.
- [ ] Run isolated focused tests and report actual counts/coverage limits.
- [ ] Complete eight-journal local proof before a feature operation contract.
- [ ] Receive independent review before fresh JAR build; native release remains separate.

## Reference Documents (Tier 0-2)

| Tier | Document | Use |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` (STD-001) | Full injected constitution; approval and transparency. |
| 0 | `docs/standards/documentation-standards.md` (STD-004) | Full injected metadata, language and evidence rules. |
| 0 | `docs/standards/development-standards.md` (STD-002) | Full injected Java/testing/ownership rules. |
| 0 | `docs/standards/glossary.md` (STD-005) | Full injected canonical terms. |
| 1 | `.claude/agents/se.md` | Current SE assignment; no subdelegation. |
| 1 | `docs/policies/security-policy.md`, `docs/policies/quality-gates.md` | Public-source-only inputs and separate quality gates. |
| 1 | `docs/standards/evidence-pack-standard.md` | Required evidence sections, per handoff classification. |
| 2 | `.agents/skills/create-wi-handoff-packet/SKILL.md` | Consumed existing packet, verified approved REQ; no new WI issued. |
| 2 | `.agents/skills/create-wi-evidence-pack/SKILL.md` | Handoff existence check and this two-set output. |
| 2 | `.agents/skills/test/SKILL.md` | Detected Java/JUnit5 via build.gradle; test execution explicitly deferred. |
| 2 | `docs/templates/wi-subagent-handoff-template.md` | Scope, acceptance and traceability contract. |

Rule source `.claude/config/context-injection-rules.json`; tag ATS verified in
`.claude/config/workspace.json`; SE required tiers `[0]`. Full Tier0 arrived first
in the task. Current `AGENTS.md`, `docs/index.md#current-decision-entry`, REQ007,
WI059 and WI064 source pointers were read. No frontend, provider research or native
inspection was performed. Dated native statements in those documents are not new observations.

## Evidence Pointers

Preparation/check owner: SE, local Windows/PowerShell, 2026-10-11.
`verification.json` records **2026-10-11T21:18:38.3372966+09:00**.
Branch `main`; HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`.
Working tree was already dirty; the patch is relative to current file bytes, not HEAD.

| Artifact | Evidence |
| --- | --- |
| `build/WI064-audio-only/audio-only.patch` | Six existing + two new paths; 268 insertions, 18 deletions, including tests. |
| `build/WI064-audio-only/source-preimages.json` | Six target preimages, two required-absent new paths, 635 public/nonignored source hashes and lengths. |
| `build/WI064-audio-only/verification.json` | Read-only apply check, unchanged inventory, eight candidate hashes, explicit NOT_RUN fields. |
| `build/WI064-audio-only/preimage/`, `candidate/` | Review copies only; not a replacement runtime or source tree. |
| `build/WI064-audio-only/README.md` | Recheck/apply gate, focused test plan, global queue and eight-journal evidence gap. |
| `deliverables/user/WI-20261011-ATS-064-summary.md` | User-facing checkpoint and next gate. |

Patch SHA-256: `3e39f2b4607df8466220f3de31c38ac6a63968fc1790c3786f26b70d052ccab8`.
Preimage manifest SHA-256: `63bf02afd90fb92f9da4b6f7c4a8612c1e283224ecf1d527eb030fec66b68af8`.

Proposed source paths use `src/main/java/com/atstudio/atstudio/` unless stated:

| Path | Proposed change |
| --- | --- |
| `config/BackgroundTasksEnabledCondition.java` | Strictly validate master and audio-only even when master=false; retain master worker condition. |
| `config/BackgroundTasksConfiguration.java` | Use scheduled condition for registration and accidental-post-processor guard. |
| `config/ScheduledBackgroundTasksEnabledCondition.java` (new) | `master && !audioOnly` using the validated flags. |
| `service/storage/StorageMutationRecoveryService.java` | Suppress only ready-event recovery when audio-only=true; retain manual methods. |
| `src/main/resources/application.yml` | Add `${APP_BACKGROUND_TASKS_AUDIO_ONLY:false}` mapping only. |
| `src/test/java/.../config/BackgroundTasksConfigurationTest.java` | 27 combinations, malformed inputs, guard paths and YAML checks. |
| `src/test/java/.../service/storage/StorageMutationRecoveryServiceTest.java` | Startup suppression/manual compatibility and default behavior. |
| `src/test/java/.../service/audio/TrackAudioProcessingWorkerIsolationTest.java` (new) | Controlled startup batches and existing global queue poller. |

Worker, transaction algorithms, startup security guards, build.gradle, frontend and
deployment tools remain unchanged. No new dependency, scheduler framework or executor.

## Commands & Outputs

- Local reads: `Get-Content`, targeted `rg`, Git branch/HEAD/status and SHA-256 hashing.
- Byte-preserving copies of six owned preimages into this WI's build area.
- Manual edits used `apply_patch` only on build-area candidates and owned reports.
- `git diff --no-index --binary -- build/WI064-audio-only/preimage build/WI064-audio-only/candidate`: exit1 denotes expected differences, not a test failure. Root-prefix rewriting generated the patch.
- `git apply --check --whitespace=error-all -- build/WI064-audio-only/audio-only.patch`: exit0, no application.
- Recomputed all 635 `rg --files src` hashes: zero changed/missing files and zero additions; both proposed new paths still absent.
- Git warned about existing mixed/CRLF YAML normalization for diff generation. Raw live-source hashes remained unchanged. No live-source newline rewrite was done.

This inventory is not the backend641/frontend336/SPA143 native identity recheck.
No earlier output, artifact, historical deliverable or native evidence was overwritten.

## Tests

**Product tests NOT RUN; compiled test count, failures, duration and coverage N/A.**
The source-identity gate explicitly defers application and Gradle execution.
Patch syntax/applicability is not compilation or behavioral validation.
The README provides exact planned suites, environment/output isolation prerequisites
and remaining mock-based eight-journal proof. No test launcher was executed.

The existing worker still recovers/claims global work. Its mocks cannot establish
row-scoped isolation, a clean target queue or actual conversion success. Source predicts
eight journal rows for two fresh success-only WAV paths, but this remains untested.
Unexpected pre-existing queue/debt, writers, journal counts or states require HOLD.

## Risks / Rollback

Current rollback is withholding the patch; no source rollback is needed.
Application requires rechecking the recorded raw preimages and absent-new paths,
reviewing exact patch bytes, and receiving MA's explicit release after the native web
identity window. Drift must not trigger force-apply or restoration of old files.
After future application, any inverse patch must preserve other agents' changes.
Do not remove audio-only while master=true on a live runtime; stop via master=false first.
Manual billing/recovery APIs are not disabled; future operation contracts must exclude them.
Native/private/mail/database/payment actions remain outside this WI's execution authority.

## Follow-ups

**Chain check:** WI064 is not complete beyond PATCH_READY_ONLY. Return this candidate
to MA, then wait for explicit application/test authorization. Its downstream independent
feature review and fresh JAR build remain blocked on that signal and local verification.
No successor was created or delegated. REQ007 remains active; this report changes no MA
trial status, runtime pin, source identity baseline or deployment decision.

## Related Documents

- [WI064 summary](../user/WI-20261011-ATS-064-summary.md)
- [WI064 handoff](WI-20261011-ATS-064-handoff.md)
- [Candidate and test plan](../../build/WI064-audio-only/README.md)
- [WI059 design](WI-20261011-ATS-059-evidence-pack.md)
- [Approved REQ007](../user/REQ-20261011-ATS-007.md)
