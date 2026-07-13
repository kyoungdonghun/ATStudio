# Evidence Pack: WI-20260711-ATS-009

## Summary (one-liner)

- Established a fresh backend regression baseline: all 745 JUnit tests passed with zero failures, errors, skips, or timeouts.

## Scope / DoD Check

- [x] Executed `gradlew.bat test` from the repository root.
- [x] Forced one fresh full-suite execution after the required command left the prior incremental report unchanged.
- [x] Recorded commands, exit codes, durations, timeout status, and exact pass/fail/error/skipped counts.
- [x] Recorded failing tests and reproducible error excerpts: none existed.
- [x] Did not modify production code, tests, configuration, secrets, persistent application data, or external provider state.
- [x] Created only the two output files owned by this WI.

## Baseline and Constraints

| Field | Value |
|---|---|
| Branch | `dev/kyoung` |
| HEAD | `27d22446e5d21324dadcfcb322dbe51704dfe914` |
| Working directory | `C:\Users\jm991\Desktop\project\ATStudio` |
| Test mode | Java 17 / Gradle 9.3.0 / JUnit Platform |
| Inspection posture | Read-only for source, tests, configuration, data, and provider state |
| Existing worktree | Pre-existing dirty/untracked work retained and not reverted |

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution, traceability, and non-destructive execution principles |
| 1 | `docs/policies/quality-gates.md` | Regression evidence and quality-gate requirements |
| 2 | `.agents/skills/test/SKILL.md` | Gradle/JUnit execution and result format |
| 2 | `.agents/skills/create-wi-evidence-pack/SKILL.md` | Evidence Pack output contract |
| REQ | `deliverables/user/REQ-20260711-ATS-001.md` | Approved audit and regression scope |
| WI | `deliverables/agent/WI-20260711-ATS-009-handoff.md` | Command, DoD, constraints, and owned outputs |
| Build | `build.gradle:66-68` | JUnit Platform test task configuration |

Context was loaded in the handoff order: Tier 0, Tier 1, Tier 2, REQ/WI context, then the current repository snapshot.

## Commands and Outputs

### Required command

- Command: `.\gradlew.bat test`
- Working directory: repository root
- Exit code: `0`
- Wall time: `17.1s`
- Timeout limit: `120s`; not reached
- Observation: the HTML/XML report timestamps remained `2026-06-18`; therefore this successful incremental invocation was not used as the fresh result-count baseline.

### Fresh full-suite execution

- Command: `.\gradlew.bat test --rerun-tasks --console=plain`
- Working directory: repository root
- Exit code: `0`
- Measured elapsed time: `103.942s`
- Gradle elapsed time: `1m 43s`
- Timeout limit: `180s`; not reached
- Gradle result: `BUILD SUCCESSFUL`
- Task evidence: `5 actionable tasks: 5 executed`
- Executed tasks: `compileJava`, `processResources`, `compileTestJava`, `processTestResources`, `test`

Relevant output excerpts:

```text
> Task :test
BUILD SUCCESSFUL in 1m 43s
5 actionable tasks: 5 executed
MEASURED_ELAPSED_SECONDS=103.942
```

Non-failing warnings:

```text
Note: Some input files use unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
Java HotSpot(TM) 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
```

### Result aggregation

- Gradle HTML report: `build/reports/tests/test/index.html`
- HTML report timestamp: `2026-07-12 15:15:57.800 +09:00`
- JUnit result directory: `build/test-results/test/`
- JUnit result files: `100`
- JUnit result timestamp range: `2026-07-12 15:15:57.987 +09:00` to `2026-07-12 15:15:58.033 +09:00`
- HTML duration: `1m17.50s`
- Sum of suite times: `74.052s`
- The HTML counters and all 100 JUnit suite headers agreed on tests, failures, errors, and skipped counts.

## Tests

| Metric | Count |
|---|---:|
| Total | 745 |
| Passed | 745 |
| Failed | 0 |
| Errors | 0 |
| Skipped | 0 |

- Failing tests: none.
- Error excerpts: not applicable; no `<failure>` or `<error>` elements were present.
- Skipped tests: none; no `<skipped>` elements were present.
- Final verdict: `PASS` for the current backend full-suite regression baseline.

## Evidence Pointers

- Files created:
  - `deliverables/user/WI-20260711-ATS-009-summary.md` - user-facing result and verdict.
  - `deliverables/agent/WI-20260711-ATS-009-evidence-pack.md` - commands, counts, durations, warnings, and reproducibility evidence.
- Generated test evidence:
  - `build/reports/tests/test/index.html` - Gradle aggregate report.
  - `build/test-results/test/TEST-*.xml` - 100 JUnit result files.
- Test configuration:
  - `build.gradle:66-68` - `test` uses JUnit Platform.

## Reproduction

From the repository root:

```powershell
.\gradlew.bat test
.\gradlew.bat test --rerun-tasks --console=plain
```

Use the second command when a fresh execution is required and Gradle would otherwise reuse the previous test task result. Confirm the generated HTML and JUnit result timestamps belong to the current run before reporting counts.

## Risks / Limitations / Rollback

- Passing tests establish the current regression baseline but do not prove completeness of test coverage or production behavior.
- Integration tests exercised their isolated H2 schema lifecycle; no persistent/local application database or external provider was targeted.
- The forced execution refreshed ignored `build/` artifacts only; no tracked source, test, or configuration path changed.
- Existing unrelated worktree changes were not modified or reverted.
- Rollback: remove only this WI's two created outputs, and only if explicitly requested.

## Follow-ups / WI Chain

- The handoff declares that this WI blocks `WI-20260711-ATS-016`.
- The MA should perform the required chain check and trigger the downstream WI after accepting this evidence.
