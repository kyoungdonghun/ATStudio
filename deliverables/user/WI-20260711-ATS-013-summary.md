# WI-20260711-ATS-013 Result Summary

## TL;DR

Backend full build and frontend production build both passed. No source fix or deployment was performed. The frontend build changed tracked generated file `frontend/tsconfig.tsbuildinfo` during execution; it was restored byte-for-byte to its pre-build SHA-256 baseline.

| Check | Result | Exit code | Elapsed | Key output |
|---|---|---:|---:|---|
| Backend full build | PASS | 0 | 3,225 ms | `BUILD SUCCESSFUL`; 8 tasks up-to-date |
| Frontend production build | PASS | 0 | 9,945 ms | 259 modules transformed; 131 files in `dist/` |

## Backend Build

- Command from repository root: `.\gradlew.bat build`
- Result: `BUILD SUCCESSFUL in 2s`; measured process elapsed time was 3,225 ms.
- Gradle reported 8 actionable tasks, all `UP-TO-DATE`, including `test` and `build`.
- Artifacts already current and therefore not rewritten:
  - `build/libs/ATStudio-0.0.1-SNAPSHOT.jar`: 69,562,899 bytes (66.34 MiB)
  - `build/libs/ATStudio-0.0.1-SNAPSHOT-plain.jar`: 792,623 bytes (0.76 MiB)
- No compilation or test warning was emitted. Gradle printed an informational suggestion to enable configuration cache.

## Frontend Build

- Command from `frontend/`: `$env:NODE_ENV = 'production'; npm run build`
- Effective script: `tsc -b && vite build`
- Result: Vite production build succeeded in 2.81 s; measured total process elapsed time was 9,945 ms.
- Output: 259 modules transformed; `frontend/dist/` contains 131 files totaling 906,725 bytes (0.86 MiB).
- Largest chunk: `assets/index-D-VZNp2o.js`, 321.00 kB (105.05 kB gzip).
- Vite emitted no chunk-size warning; the largest emitted chunk was below the default 500 kB warning threshold.

## Generated Tracked-File Handling

- `frontend/tsconfig.tsbuildinfo` is tracked and was clean at the pre-build baseline.
- Pre-build SHA-256: `432D8E84A411B0C34B1DA11800B68EDC2F1A8FEDF925D4D6290F544785E40F90`
- Post-build SHA-256 before restoration: `66047ECF66C2A3B54C29C83E7BAFDF46E044E2F529075D556C122437E0EDEBB7`
- Restored SHA-256: `432D8E84A411B0C34B1DA11800B68EDC2F1A8FEDF925D4D6290F544785E40F90`
- Post-check `git diff --exit-code -- frontend/tsconfig.tsbuildinfo` returned 0.
- The pre-existing tracked diff set was identical before and after both builds. No concurrent source or documentation change was reverted.

## Scope Notes

- No source edit, dependency install, clean operation, publish, deployment, or external-system mutation was performed.
- Build outputs remain under ignored `build/` and `frontend/dist/` paths.
