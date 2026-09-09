---
version: 1.3
last_updated: 2026-09-09
project: ATS
owner: CR
category: evidence-pack
status: stable
dependencies:
  - path: WI-20260909-ATS-026-handoff.md
    reason: Approved independent review and targeted correction scope
  - path: ../user/REQ-20260909-ATS-005.md
    reason: Approved original-preserving audio contract
  - path: WI-20260909-ATS-023-evidence-pack.md
    reason: Backend implementation and final follow-up evidence
  - path: WI-20260909-ATS-025-evidence-pack.md
    reason: Final documentation and deployment boundaries
---

# Evidence Pack: WI-20260909-ATS-026

## Summary

Independent storage/queue/access/frontend review; corrected missing safe FAILED
guidance and reproduced/fixed static access to staged and case-aliased final
audio. SE corrections are reviewed and focused results verified. DocOps handed
final current-document/REQ closure to CR. The parent's final full Gradle,
coverage, full frontend suite and build pass and are independently
artifact-verified. **Verdict: PASS for the reviewed source implementation.**
All four confirmed findings are resolved; no blocking source finding remains
within this scope. REQ005 source implementation is complete, not deployed.

## Scope / DoD Check

- [x] Read approved WI026 packet and inspect current REQ005 changed source.
- [x] Review lifecycle locks, generation/token fencing, retained references and journal-before-stage ordering.
- [x] Review public/static denial, ADMIN status/retry, derivative Range selection and unchanged original-download policy.
- [x] Confirm missing FAILED reason, notify parent before editing, add narrow Korean allowlisted guidance and regressions.
- [x] Focused frontend tests, typecheck, scoped ESLint/Prettier and whitespace checks pass.
- [x] Report duration-derived output expansion and unnecessary inherited child environment to parent; SE owns backend corrections.
- [x] Re-read final SE output budget/environment/compatibility corrections and independently inspect its final 50/50 test XML/log.
- [x] Re-read final DocOps contract; parent explicitly handed current operations/REQ closure to CR after WI025 closed.
- [x] Reproduce staged/static case-alias exposure using real temporary files; fix only the two approved prefixes and pass 65 focused security/MVC tests.
- [x] Independently review full-build failure XML and SE's corrected concurrency barrier plus 9/9 focused results; existing assertions retained.
- [x] Independently verify parent final full Gradle XML/log and unchanged coverage gates.
- [x] Independently verify parent final full frontend suite and frontend build logs.
- [x] Final document validator, nine-file scoped diff/whitespace and four-document metadata/dependency checks pass.
- [x] Issue final source-review verdict and close the authorized REQ implementation record; deployment remains separate.

## Reference Documents (Tier 0-2)

| Tier | Document | Applied context |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Injected approved scope, honest completion and preservation |
| 0 | `docs/standards/development-standards.md` | Existing patterns, short locked transactions and focused tests |
| 0 | `docs/standards/documentation-standards.md` | Seven metadata fields, exact evidence and limits |
| 0 | `docs/standards/glossary.md` | Canonical WI/REQ, Public Listening and Official Download |
| 1 | `docs/policies/security-policy.md` | Denied raw audio access and secret-safe diagnostics |
| 1 | `docs/policies/quality-gates.md` | Independent review and bounded verification |
| 2 | `docs/design/runtime-storage-operations.md` | Tuple, worker and restart/journal contract |
| 2 | `docs/design/usecase/sound-track.md` | Original preservation, replacement and manual activation |
| 2 | `docs/design/api-spec.md` | Public/admin projections, processing and Range contracts |
| 2 | `docs/design/db-schema.md` | Nine additive columns, index, historical/current manifest separation |
| 2 | `docs/standards/frontend-standards.md` | Existing component and UI conventions |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | Packet reference; companion SKILL and effect-dependency rule consulted |
| 2 | `.agents/skills/test/SKILL.md` | Focused Vitest command/result |
| 2 | `.agents/skills/typecheck/SKILL.md` | TypeScript no-emit check |
| 2 | `.agents/skills/eslint/SKILL.md` | Narrow changed-file lint |
| 2 | `.agents/skills/prettier/SKILL.md` | Changed-file formatting verification |
| 2 | `.agents/skills/build-check/SKILL.md` | Parent-executed full-build artifact verification; no duplicate build |
| 2 | `.agents/skills/test-coverage/SKILL.md` | Final JaCoCo XML and configured gate inspection |
| 2 | `.agents/skills/validate-docs/SKILL.md` | Documentation consistency validation |
| 2 | `.agents/skills/create-wi-evidence-pack/SKILL.md` | Handoff existence/read gate satisfied; this report follows the skill |

Injection: parent-provided Tier0 excerpts and skill-generated WI026 handoff;
assignee `cr`, task type `review`, agent-required tiers 0 and 1. No agents
spawned. Prior memory only oriented the initial audio search; current source
determines the reviewed contract.

## Findings and Corrections

| ID | Severity | Confirmed issue | Status / ownership |
|---|---|---|---|
| CR026-04 | P1 | PUBLIC staging was not denied; Windows uppercase/mixed-case final audio paths also bypassed the case-sensitive raw-media rule. Real temporary files returned 200/audio bytes to anonymous, USER and ADMIN requests. | Resolved by CR after explicit parent approval: case-insensitive deny of only `.staging` and `tracks/audio` prefixes. Red XML demonstrates actual exposure; final 65/65 security/MVC tests PASS. |
| CR026-01 | P2 | `FfmpegAudioEncoder.outputByteLimit` originally computed only `ceil(decodedFrames / sampleRate * 16000) + 65536`. A valid positive low sample rate could make the nominal bound enormous despite a 100MiB input. | Resolved by SE; absolute 256MiB preflight rejection with `AUDIO_OUTPUT_TOO_LARGE`, exact overflow-safe arithmetic and boundary regressions re-reviewed. Final focused XML PASS independently inspected. |
| CR026-02 | P2 | `new ProcessBuilder(command).start()` inherited unrelated server environment entries into the native encoder. | Resolved by SE; case-insensitive eleven-name child-only allowlist, synthetic-secret omission test and ten real native pipeline cases PASS. No actual credentials read. |
| CR026-03 | P2 | FAILED state rendered its label and retry action but ignored `status.errorCode`; missing encoder, input, timeout and storage failures had no actionable distinction. | Fixed by CR in three frontend files after parent notification; 26 component tests pass. |

CR026-01 reproduction reasoning: 100MiB mono 8-bit PCM at 1Hz can represent
roughly 104.9 million seconds, below the analyzer's integer-duration bound;
the former 128kbps estimate is roughly 1.68TB. The encode timeout is a time
bound, not an absolute byte budget. No giant file or native encode was run by
CR. The proposed 256MiB ceiling retains the approximately 200MiB plus 64KiB
estimate for a 100MiB 8kHz mono 8-bit input; oversized full-length output must
be rejected before process creation, never silently clipped.

CR026-02 is least-privilege hardening of a confirmed inheritance path, not a
claim that a credential was leaked or that a media exploit was demonstrated.

Final correction review: `FfmpegAudioEncoder.java:42` calculates the complete
output budget before constructing ProcessBuilder. At line 120, whole seconds
are bounded before multiplying, and the remainder calculation stays within
long arithmetic for the positive int sample rate. The exact 268435456-byte
estimated boundary is accepted; one byte above is rejected. Accepted output
still uses `-fs`, rejects reaching the bound, and is re-decoded for duration,
128000bps and compatible rate/channels. At line 59, only the new child map is
filtered; the parent environment is not changed. Ten real two-second native
pipeline cases pass with the filter active. Boundary/8-bit/large-duration
tests are modeled metadata, not multi-hour/large-output native tests.

CR026-03 uses a switch over ten known codes, including the coordinated
`AUDIO_OUTPUT_TOO_LARGE`. Unknown/null/path/prototype-like values get only a
fixed generic message. No arbitrary backend code, message, path, stderr or
exception text is rendered. Guidance is shown only for FAILED and does not
change retry/readiness/polling state. The termination-failure enum is handled
defensively; current worker halt is not falsely described as a normally
persisted FAILED/retryable state.

CR026-04 reproduction and scope:

- `LocalStorageService.java:260` puts generated files below PUBLIC `.staging`;
  `WebConfig.java:23` maps the PUBLIC root to `/uploads/**`. The old security
  rule denied only lowercase final `/uploads/tracks/audio/**`, with static
  requests otherwise permitted.
- First red run: 20 tests, 12 failures, each showing HTTP 200 and synthetic
  audio bytes for `.staging`/`.STAGING`, WAV/MP3, anonymous/USER/ADMIN.
- Stage-only fix passed. Parent then explicitly approved checking the same
  Windows case mismatch for final audio. Second red run: 26 tests, six failures
  with actual bytes from uppercase/mixed-case final audio for all three roles.
- Final rule uses a case-insensitive RegexRequestMatcher for exactly staging
  and final audio prefixes. It replaces, and strengthens, the former final
  raw-audio deny. Other-domain rules and public listening endpoints are unchanged.
- Final 26 new cases pass: protected anonymous 401, USER/ADMIN 403; ordinary
  public thumbnail returns exact bytes with 200. Encoded dot/slash return 400
  through the existing security firewall in MockMvc, not an assertion about
  live Tomcat/proxy decoding. The final targeted selection adds the existing
  thumbnail and Track controller suites for 65/65 PASS.

The parent's initial full-build failure is a separate test-harness correction:
all seven `TrackMutationConcurrencyIntegrationTest` writer cases failed at the
old pre-lock latch assertion. The MP3 fixture still used `mutations.replace`;
it was not a missing `store` stub. Analysis now intentionally precedes row-lock
acquisition, so the test barrier had to move to the post-lock storage call.
SE retained every seven-writer Timeout/media/counter assertion, added actual
PESSIMISTIC_WRITE/old-key checks, and a test proving analysis-time writes finish
and survive later replacement. CR verified its 9/9 XML, with no source-lock or
threshold weakening. Original failing logs/XML remain preserved.

## Evidence Pointers

CR changed files:

- `frontend/src/utils/audioProcessing.ts:7`: safe Korean failure-code switch.
- `frontend/src/components/track/AudioProcessingStatus.tsx:201`: FAILED-only guidance using existing styles.
- `frontend/src/components/track/AudioProcessingStatus.test.tsx:95`: ten known codes, four unknown/null cases, poll-to-failure/retry and non-failed stale-reason regressions; sixteen added tests.
- `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:90`: approved two-prefix case-insensitive deny.
- `src/test/java/com/atstudio/atstudio/config/StagedAudioStaticAccessTest.java:75`: actual temporary-file security/MVC, role/case and positive-thumbnail regressions.
- `docs/design/runtime-storage-operations.md`: current protection and review evidence; prior dated evidence retained.
- `deliverables/user/REQ-20260909-ATS-005.md`: parent-authorized current review/implementation closeout, distinct from deployment.
- `deliverables/agent/WI-20260909-ATS-026-evidence-pack.md`: this report.
- `deliverables/user/WI-20260909-ATS-026-summary.md`: compact review handoff.

Read-only lifecycle/security review:

- `entity/Track.java:167` and `service/audio/TrackAudioProcessingTransactions.java:38` under `src/main/java/com/atstudio/atstudio/`: completion requires generation, claim token and PROCESSING; old results are not published. Claim release is token-specific.
- `TrackAudioProcessingTransactions.java:65`: startup recovery is batched at 50; interrupted PROCESSING becomes a manually retryable failure, superseded PENDING generation is preserved.
- `service/storage/StorageMutationCoordinator.java:70,119`: register in-flight and commit journal intent before uploaded/generated staging. Generation runs outside a business transaction and attaches only on accepted finalization.
- `service/storage/StorageMutationJournalService.java:96`: recovery excludes same-runtime in-flight operations. This is not cross-process leasing.
- `service/storage/StorageReferenceChecker.java`: all original/stream/pending/claimed references are counted, including inactive rows, before old-file cleanup.
- `service/TrackService.java:159,175,232`: stream selects derivative when required; replacement preserves old pair until completion; deactivate/delete fences pending work while retaining existing original/license records.
- `config/SecurityConfig.java:90`, `controller/TrackController.java:85`, `dto/track/TrackResponse.java:30`: corrected raw/staged denial, ADMIN-only status/retry, public audio key and processing projection remain null.
- `service/DownloadService.java:40,90`: existing user-then-track locks, entitlement/quota/license policy and original selection are unchanged. This is not a new right to download inactive Tracks.
- `src/test/java/com/atstudio/atstudio/service/storage/TrackAudioPipelineIntegrationTest.java`: existing source covers stale generation, replacement/metadata, deactivate, rollback, partial output, retained original, Range and download; CR artifact-audited SE/parent execution without rerunning the native pipeline.
- `scripts/database/manual-wi023-audio-processing.sql`: source-only nine additive columns and queue index; no UPDATE/DELETE/DROP/backfill. `DisposableMysqlBootstrap.java` leaves historical manifest values intact and current expectation UNRECORDED.

## Commands & Outputs

Frontend working directory: `C:\Users\jm991\Desktop\project\ATStudio\frontend`.

| Command | Result |
|---|---|
| `npm.cmd test -- src/components/track/AudioProcessingStatus.test.tsx --maxWorkers=1` | Exit 0; 1 file, 26 passed, 0 failed, 1.83s; 2026-09-09 12:50:47 KST |
| `npm.cmd run typecheck` | Exit 0; `tsc --noEmit`, entire configured frontend source |
| `npx.cmd eslint src/utils/audioProcessing.ts src/components/track/AudioProcessingStatus.tsx src/components/track/AudioProcessingStatus.test.tsx --max-warnings 0` | Exit 0, no errors/warnings |
| `npx.cmd prettier --check src/utils/audioProcessing.ts src/components/track/AudioProcessingStatus.tsx src/components/track/AudioProcessingStatus.test.tsx` | Final exit 0, all three files formatted |
| `npx.cmd prettier --write src/components/track/AudioProcessingStatus.test.tsx` | Exit 0; only the CR-owned test file formatted after an initial check failed |
| Scoped `git diff --check --` for the same three frontend paths | Exit 0; untracked contents additionally checked directly |
| PowerShell direct trailing-whitespace check of the three frontend files | 3 files, 0 trailing-whitespace errors |
| `python -B .agents/skills/validate-docs/scripts/validate_docs.py` from repository root | Exit 0; Tier0, links, 718 traceability IDs and index checks PASS |
| PowerShell required-field/trailing-whitespace check of both WI026 reports | 2 files, seven fields each, 0 errors; presence check, not a general YAML schema validator |
| Final `python -B .agents/skills/validate-docs/scripts/validate_docs.py` after source-completion closeout | Exit 0; Tier0, links, 718 traceability IDs and index PASS |
| Final `git diff --check --` with the nine paths in CR changed files above | Exit 0; existing SecurityConfig CRLF normalization warning only, no whitespace errors or Git state mutation |
| Final PowerShell direct whitespace scan of those nine files, seven metadata fields and dependency-path checks of the four Markdown files | 0 errors; includes untracked files; required-field presence and dependency existence, not a general YAML schema validator |

CR did not execute a native encoder, full Gradle build, full frontend suite or
frontend build. CR's focused Gradle security tests ran serially after SE's
concurrency run ended; the following full-suite window was returned to the
parent. No concurrent heavy tests or server restarts were performed.

Repository-root commands for direct security reproduction and correction:

```powershell
.\gradlew.bat test --tests '*StagedAudioStaticAccessTest' --console=plain --max-workers=1 *> build/wi026-security-red.log
.\gradlew.bat test --tests '*StagedAudioStaticAccessTest' --tests '*PublicThumbnailStaticResourceTest' --tests '*TrackControllerTest' --console=plain --max-workers=1 *> build/wi026-security-green.log
.\gradlew.bat test --tests '*StagedAudioStaticAccessTest' --console=plain --max-workers=1 *> build/wi026-final-audio-red.log
.\gradlew.bat test --tests '*StagedAudioStaticAccessTest' --tests '*PublicThumbnailStaticResourceTest' --tests '*TrackControllerTest' --console=plain --max-workers=1 *> build/wi026-security-final-green.log
```

Results in order: expected exit 1/12 failures in 12s; stage-only exit 0 in
30s; expected exit 1/six failures in 12s; final exit 0 in 30s, 65 tests passed,
zero failures/errors/skips. Preserved XML:
`build/wi026-security-red.xml`, `build/wi026-security-stage-green.xml`,
`build/wi026-final-audio-red.xml`, and the three suites under
`build/wi026-security-final-green-xml/`. Red assertion messages include only
synthetic fixture URLs/bytes, never real storage objects or credentials.

## Tests and Verification Limits

New frontend evidence is Vitest/jsdom with mocked APIs, not real browser or
uploaded media. The parent's earlier 114-file/1584-test PASS and frontend
build PASS precede this correction; do not rename them as a new full-suite
run. The separate final full frontend suite/build are recorded below.

SE's final output/environment run was reported complete and independently
checked from `build/wi023-evidence/output-env-focused.log` and the three XML
files in `build/wi023-evidence/output-env-focused-xml/`. Log ends BUILD
SUCCESSFUL in 31s; XML totals are encoder 28, pipeline 20, configuration 2,
**50 passed / 0 failed / 0 errors / 0 skipped**. The pipeline XML contains ten
indexed native rate/channel cases, including 96kHz/stereo, 44.1kHz/six-channel,
96kHz/six-channel and 8kHz/mono. These are SE-executed native/H2/temporary-root
tests, independently artifact-audited by CR, not CR reruns or runtime MySQL.

Exact SE command from WI023 evidence, with `ATS_TEST_FFMPEG_PATH` set explicitly
in that test process to the parent-provisioned binary:

```powershell
.\gradlew.bat test --tests '*FfmpegAudioEncoderTest' --tests '*TrackAudioPipelineIntegrationTest' --tests '*AudioProcessingConfigTest' --console=plain --max-workers=1 *> build/wi023-evidence/output-env-focused.log
```

SE concurrency evidence is `build/wi023-evidence/concurrency-focused.log` and
`concurrency-focused-xml/`: 9/9 PASS, zero failures/errors/skips, 27s. Initial
parent full-build failure is preserved under `build/wi023-evidence/parent-full-failed.log`
and `parent-full-failed-xml/`: 1885 total, seven failures, 19 skips. Neither
failure nor narrower focused reruns are a successful full build.

### Parent Final Backend Build

Parent ran `.\gradlew.bat build` with explicit `ATS_TEST_FFMPEG_PATH`; CR
independently read `%LOCALAPPDATA%/ATStudio/validation/wav128-20260909/backend-full-build-final.log`,
all `build/test-results/test/TEST-*.xml`, and
`build/reports/jacoco/test/jacocoTestReport.xml`. The log ends **BUILD SUCCESSFUL
in 2m 16s**, including `jacocoTestReport`, `jacocoTestCoverageVerification`,
`check` and `build`. XML totals: **204 suites, 1912 tests, 1893 passed,
0 failures, 0 errors, 19 skipped**. Skips are 18 explicitly opted-in disposable
MySQL cases not enabled in this run and one Windows symbolic-link case.

| JaCoCo metric | Covered / total | Percent | Unchanged gate |
|---|---:|---:|---:|
| LINE | 10869 / 12264 | 88.625% | 80% |
| METHOD | 1930 / 2228 | 86.625% | 80% |
| BRANCH | 3883 / 5232 | 74.216% | 70% |

All seven configured critical security classes retain LINE/METHOD 100%.
The changed `SecurityConfig`, separately inspected, has LINE 96/96 and
METHOD 11/11 (100%). No coverage thresholds, exclusions or product locks were
weakened. CR did not substitute focused coverage for the full gate. This run
supersedes the earlier failed full run as current source evidence, without
erasing its failure record or establishing actual MySQL migration/deployment.

### Parent Final Frontend Verification

Working directory: `C:\Users\jm991\Desktop\project\ATStudio\frontend`.
Logs are below `%LOCALAPPDATA%/ATStudio/validation/wav128-20260909/`.

| Exact invocation | Final result | Log independently read by CR |
|---|---|---|
| `npm.cmd test -- --maxWorkers=1` | Exit 0; 114 files, 1600 passed, 0 failed, 236.59s; start 2026-09-09 13:11:09 KST | `frontend-full-tests-final.log` |
| `npm.cmd run build` | Exit 0; `tsc -b && vite build`; Vite 6.4.3, 304 modules, Vite phase 2.40s | `frontend-build-final.log` |

The test log contains one jsdom `Not implemented: navigation to another Document`
diagnostic and still reports all 1600 tests passed; it is not browser navigation
evidence. Parent separately confirmed global `npm.cmd run lint`,
`npm.cmd run typecheck` and the 17-changed-file Prettier check passed after the
last frontend source change. Those global static results are parent-reported;
CR's own narrower exact static commands/results are listed above. All heavy
executions were serial, and source remained frozen for the final runs.

### Final Documentation and Preservation

Current WI026 reports, operations rollout table and REQ005 closeout distinguish
source completion from deployment pending. The WI025 receipt text and earlier
failed/partial results remain explicitly historical. Other dated domains and
the eight pre-existing dirty documents were not rewritten by CR. Final
documentation validation and scoped whitespace checks are recorded in Commands
& Outputs. No additional WI is required: WI026's `Blocks` value is `none`.

## Risks / Rollback

- The currently running backend remains the old 30MiB source deployment; Vite
  HMR may expose the 100MiB frontend label. No DB, DDL, real media, actual
  credentials, private settings/backups, existing server or process was changed.
- Single-runtime startup recovery assumes the old encoder/descendants have
  exited. A hard-killed JVM is not OS-native process containment; unresolved
  native termination halts the worker and requires an approved ownership check.
  This intentional operational boundary is not a newly proven generation bug.
- Actual MySQL migration, tuple/backup checks, strict integrity, provisioning,
  proxy total-request limits, browser/full-payload and deployed acceptance are
  outside this review's performed verification.
- CR rollback: after comparing concurrent edits, reverse only this WI's
  frontend/message and security-rule/test hunks and current documentation
  additions. Reverting the deny rule reopens the demonstrated exposure and
  requires an explicit risk decision; never restore the entire shared file.
  Keep all pre-existing WI024 code and unrelated worktree changes. No Git state
  mutation was performed.

## Follow-ups

WI023/024/025 dependencies and WI026 source review are closed. Under the parent's
explicit delegation, CR completed the current REQ005 source-implementation
record. No next WI was generated. Deployment remains a separate approval:
verify the retained DB/root/backup tuple, apply the nine columns plus queue index
to the existing database (not a new database), configure the service's approved
FFmpeg path, validate schema/integrity, coordinate a non-overlapping restart,
and perform proxy whole-request and actual browser acceptance. None of those
runtime operations was performed or approved by this source verdict.
