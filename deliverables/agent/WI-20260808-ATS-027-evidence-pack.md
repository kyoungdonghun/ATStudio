# Evidence Pack: WI-20260808-ATS-027

## Identity

| Field | Value |
| --- | --- |
| WI | WI-20260808-ATS-027 |
| REQ | REQ-20260808-ATS-004 |
| Agent | qa |
| Branch | `codex/v1-release-rehearsal-fixes` |
| Handoff | `deliverables/agent/WI-20260808-ATS-027-handoff.md` |
| Execution date | 2026-08-09 (Asia/Seoul) |
| Final status | PASS |

## Summary

The backend Gradle build and frontend Vite production build both completed successfully without product or test code edits.

## Scope and DoD Check

- [x] Run `.\gradlew.bat build` once from the repository root.
- [x] Run `npm run build` once from `frontend` after the backend command completed.
- [x] Continue independently without repair if either build failed; no repair was needed.
- [x] Record exit codes, build results, warnings, artifacts, and bundle sizes.
- [x] Check only `frontend/tsconfig.tsbuildinfo` immediately before and after the frontend build; both checks were clean.
- [x] Confirm generated build output did not add scoped tracked-file changes.
- [x] Avoid application servers, external integrations, ZIP handling, secrets, DB/schema/data commands, commit, and push.

## Reference Documents

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Constitution and traceability requirements. |
| 0 | `docs/standards/documentation-standards.md` | English documentation and deliverable structure. |
| 0 | `docs/standards/development-standards.md` | QA build, test, and evidence requirements. |
| 0 | `docs/standards/glossary.md` | Canonical WI and REQ terminology. |
| 1 | `docs/policies/quality-gates.md` | Operational quality gate criteria. |
| 2 | `deliverables/user/REQ-20260808-ATS-004.md` | Approved scope, constraints, and downstream plan. |
| 2 | `deliverables/agent/WI-20260808-ATS-027-handoff.md` | Authoritative WI scope and output contract. |

## Authoritative Execution Evidence

| Sequence | Working directory | Command | Exit code | Exact observed result |
| ---: | --- | --- | ---: | --- |
| 1 | Repository root | `.\gradlew.bat build` | 0 | `BUILD SUCCESSFUL in 3m 9s`; 10 actionable tasks: 8 executed, 2 up-to-date. Command wall time was 190.7 seconds. |
| 2 | `frontend` | `npm run build` | 0 | Script ran `tsc -b && vite build`; Vite 6.4.3 transformed 272 modules and reported `built in 3.39s`. Command wall time was 12.5 seconds. |

The backend command executed these terminal Gradle tasks: `test`, `jacocoTestReport`, `jacocoTestCoverageVerification`, `check`, and `build`. The generated XML results at `build/test-results/test/` contained 170 suite files and aggregated to 1,357 tests, 0 failures, 0 errors, 13 skipped, and 161.802 seconds of suite time.

## Frontend Tracked-File Sentinel

| Point | Command | Exit code | Output |
| --- | --- | ---: | --- |
| Immediately before frontend build | `git status --short -- frontend/tsconfig.tsbuildinfo` | 0 | No output. |
| Immediately after frontend build | `git status --short -- frontend/tsconfig.tsbuildinfo` | 0 | No output. |

QA did not restore, edit, or otherwise alter `frontend/tsconfig.tsbuildinfo`.

## Warnings and Advisories

- Backend JVM warning: `Sharing is only supported for boot loader classes because bootstrap classpath has been appended`.
- Backend Gradle advisory: `Consider enabling configuration cache to speed up this build`.
- Backend test output included Hibernate `drop table if exists ...` cleanup statements emitted by test contexts. QA ran no standalone DB, schema, or data command.
- Frontend warnings: none reported. Vite emitted no chunk-size warning.

## Artifact Evidence

| Path | Exact size | Build output detail |
| --- | ---: | --- |
| `build/libs/ATStudio-0.0.1-SNAPSHOT.jar` | 69,820,945 bytes | Spring Boot executable JAR. |
| `build/libs/ATStudio-0.0.1-SNAPSHOT-plain.jar` | 1,044,919 bytes | Plain JAR. |
| `frontend/dist/` | 993,638 bytes | 133 generated files. |
| `frontend/dist/index.html` | 613 bytes | Vite reported 0.61 kB, gzip 0.36 kB. |
| `frontend/dist/assets/index-BLmJUbOU.js` | 348,310 bytes | Largest output; Vite reported 348.31 kB, gzip 113.09 kB. |
| `frontend/dist/assets/index-cyTSEMGo.css` | 33,645 bytes | Vite reported 33.65 kB, gzip 6.64 kB. |

The hashed main bundle names observed in this run were `index-BLmJUbOU.js` and `index-cyTSEMGo.css`. No baseline bundle diff was requested or generated; this pack records the current production output exactly.

## Generated and Tracked Changes

- `git status --short -- build frontend/dist` exited 0 with no output.
- `git check-ignore -v` confirmed backend JARs are ignored by `.gitignore:3` (`build/`) and frontend output is ignored by `frontend/.gitignore:2` (`dist/`).
- Generated or refreshed ignored output: `build/` and `frontend/dist/`.
- Created tracked-candidate documentation outputs:
  - `deliverables/user/WI-20260808-ATS-027-summary.md`
  - `deliverables/agent/WI-20260808-ATS-027-evidence-pack.md`
- Product/test code modifications by qa: none.
- Existing unrelated dirty worktree content was approved, not broadly inspected, and not reverted.

## Acceptance Decision

- Backend production build: PASS.
- Backend tests and JaCoCo verification included in the build: PASS.
- Frontend TypeScript project build: PASS.
- Frontend Vite production bundle: PASS.
- Scoped tracked-file cleanliness: PASS.
- WI-027 DoD: satisfied.

## Risk

- This verification does not cover runtime startup, deployed behavior, production databases, external providers, or deployment packaging beyond the generated JAR and Vite bundle.
- Thirteen backend tests were skipped. No failure or coverage-verification error resulted, but skipped coverage remains a residual limitation of this run.
- The approved pre-existing dirty worktree was outside the scoped status checks, except for the required `frontend/tsconfig.tsbuildinfo` sentinel and generated artifact paths.

## Rollback

- No product or test code rollback is required.
- No destructive cleanup was performed.
- Generated ignored artifacts can be superseded by a later authorized clean build.
- If this evidence is withdrawn, revert only the two WI-027 documentation files through a separately authorized cleanup action.

## Downstream Status

WI-027 no longer blocks WI-028 through WI-030. WI-028 and WI-029 are executable after this PASS. WI-030 is unblocked by WI-027 specifically but remains gated by completion of WI-028 and WI-029 under the approved REQ dependency plan.
