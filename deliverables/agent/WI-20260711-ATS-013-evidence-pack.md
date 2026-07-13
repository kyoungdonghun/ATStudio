# Evidence Pack: WI-20260711-ATS-013

## Summary

- Verified production buildability: backend Gradle full build and frontend Vite production build both passed, with generated tracked-file effects restored to baseline.

## Scope / DoD Check

- [x] Ran the complete backend Gradle build, including the configured test lifecycle.
- [x] Ran the frontend production build using the package-defined build script.
- [x] Recorded exact commands, exit codes, elapsed times, warnings, and build artifacts.
- [x] Inspected generated tracked-file effects against the pre-build Git and SHA-256 baseline.
- [x] Restored only the proven build-generated `frontend/tsconfig.tsbuildinfo` delta.
- [x] Performed no source fix, publish, deployment, or external-system mutation.
- [x] Limited authored files to this WI's user summary and agent evidence pack.

## Reference Documents

### Injected Context from Handoff

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | System constitution and traceability rules |
| 0 | `docs/standards/development-standards.md` | QA development and build evidence standards |
| 1 | `docs/policies/quality-gates.md` | Operational quality-gate requirements |
| 2 | `.agents/skills/build-check/SKILL.md` | Backend and frontend build-check workflow |

### Requirement and Input Pointers

- `deliverables/user/REQ-20260711-ATS-001.md`: approved audit scope and Phase 3 `WI-013`.
- `deliverables/agent/WI-20260711-ATS-013-handoff.md`: command scope, DoD, output contract, and rollback boundary.
- `build.gradle`: backend Gradle build and test configuration.
- `frontend/package.json`: `build` script definition, `tsc -b && vite build`.

## Baseline

- Branch: `dev/kyoung`
- HEAD: `27d22446e5d21324dadcfcb322dbe51704dfe914`
- Staged diff: none.
- Pre-existing tracked diff set before build:
  - Modified: `docs/client/0-site-policy.md`, `docs/client/4-sr-format.md`, `docs/client/5-ai-prompt.md`, `docs/client/index.md`, `docs/client/testing-guide.md`, `docs/index.md`
  - Deleted: `docs/client/1-scenarios.md`, `docs/client/2-test-cases.md`, `docs/client/3-test-methodology.md`, `docs/client/testing-guide-friendly.html`
- Numerous pre-existing untracked WI outputs, client documents, logs, and PDF output were present in the shared worktree. They were not modified or reverted by this WI.
- `frontend/tsconfig.tsbuildinfo` was tracked and clean before the frontend build.
- `build/` and `frontend/dist/` are ignored by repository Git rules.

## Evidence Pointers

- Files created:
  - `deliverables/user/WI-20260711-ATS-013-summary.md`: user-facing build verdict and generated-file handling.
  - `deliverables/agent/WI-20260711-ATS-013-evidence-pack.md`: reproducible commands, results, baseline, and rollback evidence.
- Backend artifacts:
  - `build/libs/ATStudio-0.0.1-SNAPSHOT.jar`
  - `build/libs/ATStudio-0.0.1-SNAPSHOT-plain.jar`
- Frontend artifacts: `frontend/dist/`
- Tracked generated-file check: `frontend/tsconfig.tsbuildinfo`

## Commands and Outputs

| Working directory | Command | Exit code | Measured elapsed | Result |
|---|---|---:|---:|---|
| Repository root | `.\gradlew.bat build` | 0 | 3,225 ms | PASS: `BUILD SUCCESSFUL`; 8 actionable tasks up-to-date |
| `frontend/` | `$env:NODE_ENV = 'production'; npm run build` | 0 | 9,945 ms | PASS: `tsc -b && vite build`; 259 modules transformed |
| Repository root | `git diff --exit-code -- frontend/tsconfig.tsbuildinfo` | 0 | Not timed | Restored file matches tracked baseline |

Runtime/tool versions:

- Java launcher: Oracle JDK `17.0.12`
- Gradle: `9.3.0`
- Node.js: `v24.14.0`
- npm: `11.9.0`
- Vite used by build: `6.4.1`

### Backend Output

- Gradle lifecycle tasks included `compileJava`, `processResources`, `bootJar`, `jar`, `compileTestJava`, `test`, `check`, and `build`.
- All 8 tasks were `UP-TO-DATE`; the build did not rewrite the existing JARs.
- `build/libs/ATStudio-0.0.1-SNAPSHOT.jar`: 69,562,899 bytes (66.34 MiB).
- `build/libs/ATStudio-0.0.1-SNAPSHOT-plain.jar`: 792,623 bytes (0.76 MiB).
- Warning classification: no compilation/test warning; one informational Gradle configuration-cache recommendation.

### Frontend Output

- Vite transformed 259 modules and completed its production bundle phase in 2.81 s.
- `frontend/dist/`: 131 files, 906,725 bytes total (0.86 MiB).
- Largest emitted chunk: `assets/index-D-VZNp2o.js`, 321.00 kB and 105.05 kB gzip.
- Output-size warning count: 0. The largest chunk was below Vite's default 500 kB warning threshold.

## Generated Tracked-File Evidence

The frontend build's `tsc -b` step rewrote tracked incremental metadata. The file was snapshotted in memory before the command and restored immediately after measurement.

| State | SHA-256 |
|---|---|
| Pre-build baseline | `432D8E84A411B0C34B1DA11800B68EDC2F1A8FEDF925D4D6290F544785E40F90` |
| Immediately after build | `66047ECF66C2A3B54C29C83E7BAFDF46E044E2F529075D556C122437E0EDEBB7` |
| After byte-for-byte restoration | `432D8E84A411B0C34B1DA11800B68EDC2F1A8FEDF925D4D6290F544785E40F90` |

- Post-build scoped status for `build/`, `frontend/dist/`, and `frontend/tsconfig.tsbuildinfo`: no Git status entries.
- The full tracked `git diff --name-status` set was identical before and after both builds.
- No pre-existing shared-worktree change was reverted.

## Tests

- Backend: PASS through `.\gradlew.bat build`; Gradle reported the `test` task `UP-TO-DATE`.
- Frontend: PASS through production build; TypeScript project build and Vite bundling both completed.
- No clean build, standalone test rerun, source fix, browser test, deployment, or external-system check was in scope.

## Risks / Rollback

- Limitation: Gradle considered all backend tasks up-to-date, so this verifies the current incremental build state rather than forcing a clean rebuild.
- Limitation: a successful build does not establish runtime integration, browser behavior, or deployment readiness.
- Shared-worktree risk was controlled by pre/post Git comparison and byte-for-byte restoration of the only tracked generated delta.
- Rollback: remove only `deliverables/user/WI-20260711-ATS-013-summary.md` and `deliverables/agent/WI-20260711-ATS-013-evidence-pack.md` when explicitly requested. Ignored build outputs may be removed separately only with explicit approval.

## Follow-up / WI Chain

- This WI blocks `WI-20260711-ATS-017` according to the handoff.
- No remediation WI is required from these build results; any clean-build or runtime validation expansion requires separate scope.
