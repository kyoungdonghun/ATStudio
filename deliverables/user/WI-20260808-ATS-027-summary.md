# WI-20260808-ATS-027 Production Build Verification Summary

## Result

- **Status:** PASS
- **REQ:** REQ-20260808-ATS-004
- **Agent:** qa
- **Branch:** `codex/v1-release-rehearsal-fixes`
- **Execution date:** 2026-08-09 (Asia/Seoul)
- **Scope:** One backend Gradle production build followed by one frontend npm production build. No repair, server startup, or external integration was performed.

## Commands and Results

| Sequence | Working directory | Command | Exit code | Result |
| ---: | --- | --- | ---: | --- |
| 1 | Repository root | `.\gradlew.bat build` | 0 | PASS: `BUILD SUCCESSFUL in 3m 9s`; 10 actionable tasks, 8 executed and 2 up-to-date. |
| 2 | `frontend` | `npm run build` | 0 | PASS: `tsc -b && vite build`; 272 modules transformed; Vite built in 3.39s. |

The Gradle build executed `test`, `jacocoTestReport`, `jacocoTestCoverageVerification`, `check`, and `build`. Generated test XML contained 1,357 tests: 0 failures, 0 errors, and 13 skipped.

## Warnings

- Backend: the JVM reported `Sharing is only supported for boot loader classes because bootstrap classpath has been appended`.
- Backend: Gradle suggested enabling the configuration cache. This was advisory and did not affect the successful exit.
- Backend test logs included Hibernate test-context cleanup SQL. QA did not issue an independent DB, schema, or data command.
- Frontend: no Vite warning or chunk-size warning was reported.

## Build Artifacts

| Artifact | Size |
| --- | ---: |
| `build/libs/ATStudio-0.0.1-SNAPSHOT.jar` | 69,820,945 bytes |
| `build/libs/ATStudio-0.0.1-SNAPSHOT-plain.jar` | 1,044,919 bytes |
| `frontend/dist/` | 133 files, 993,638 bytes total |
| `frontend/dist/index.html` | 613 bytes |
| `frontend/dist/assets/index-BLmJUbOU.js` | 348,310 bytes; Vite gzip 113.09 kB |
| `frontend/dist/assets/index-cyTSEMGo.css` | 33,645 bytes; Vite gzip 6.64 kB |

## Generated and Tracked Changes

- `build/` and `frontend/dist/` were generated or refreshed and are ignored by the repository. Scoped `git status --short -- build frontend/dist` returned no entries.
- `git status --short -- frontend/tsconfig.tsbuildinfo` returned no entries both immediately before and immediately after the frontend build. QA did not restore or edit this file.
- Created as WI outputs: `deliverables/user/WI-20260808-ATS-027-summary.md` and `deliverables/agent/WI-20260808-ATS-027-evidence-pack.md`.
- No product or test code was edited by QA.

## Risk

This gate verifies compilation, packaged artifacts, the tests and coverage verification included in Gradle `build`, and the frontend production bundle. It does not verify a running application, production data, external providers, deployment, or the unrelated pre-existing dirty worktree. Thirteen backend tests were reported as skipped; the build and coverage gates nevertheless passed.

## Rollback

No product or test code rollback is required. No cleanup was performed. The ignored build artifacts may be replaced by a later authorized clean build, and withdrawal of this evidence would require reverting only the two WI-027 documents under a separately authorized cleanup action.

## Blocking Status

WI-027 is complete and no longer blocks WI-028 through WI-030. WI-028 and WI-029 may proceed now. WI-030 is cleared with respect to WI-027 but remains dependency-blocked until WI-028 and WI-029 complete.
