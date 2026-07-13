# Evidence Pack: WI-20260711-ATS-011

## Summary (one-liner)

- Independently verified Java production compilation and frontend TypeScript contracts; both commands passed, and the tracked `tsconfig.tsbuildinfo` baseline remained unchanged.

## Scope / DoD Check

- [x] Ran the Java compile check.
- [x] Ran the repository-configured frontend TypeScript no-emit check.
- [x] Recorded exact commands, exit codes, outputs, warnings, and elapsed times.
- [x] Proved the tracked `tsconfig.tsbuildinfo` baseline was clean before execution.
- [x] Compared its post-run Git state and SHA-256 with the baseline.
- [x] Recorded that no generated-file rollback was necessary.
- [x] Created only the two WI-011 output files.

## Baseline and Constraints

| Item | Result |
|---|---|
| Workspace | `C:\Users\jm991\Desktop\project\ATStudio` |
| Branch | `dev/kyoung` |
| HEAD | `27d22446e5d21324dadcfcb322dbe51704dfe914` |
| Initial owned outputs | Both WI-011 output paths were absent. |
| Working tree | Dirty before execution; all concurrent and user changes were treated as immutable. |
| Allowed mutation | Only the two WI-011 output files; a newly produced tracked `tsbuildinfo` delta could be restored only after proving a clean baseline. |
| Forbidden mutation | Source fixes and unrelated worktree changes. |

## Reference Documents (Tier 0-2)

| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approval, transparency, platform integrity, and traceability rules |
| 0 | `docs/standards/development-standards.md` | Java and TypeScript verification standards |
| 0 | `docs/standards/documentation-standards.md` | Deliverable structure and documentation language |
| 0 | `docs/standards/glossary.md` | Canonical WI and QA terminology |
| 1 | `docs/policies/quality-gates.md` | Regression verification and Evidence Pack requirements |
| 2 | `.agents/skills/typecheck/SKILL.md` | Java and TypeScript typecheck commands |
| Context | `deliverables/user/REQ-20260711-ATS-001.md` | Approved audit and quality-verification scope |
| Context | `deliverables/agent/WI-20260711-ATS-011-handoff.md` | Command scope, DoD, generated-state rule, and output contract |

Injection order applied: Tier 0 -> Tier 1 -> Tier 2 -> current repository snapshot.

## Evidence Pointers

- `deliverables/agent/WI-20260711-ATS-011-handoff.md:10-21`
  - Requires Java compile/type checking, frontend TypeScript no-emit checking, elapsed times, and explicit generated-state handling.
- `deliverables/agent/WI-20260711-ATS-011-handoff.md:38-46`
  - Defines the two owned outputs, exact-command evidence, and rollback boundary.
- `build.gradle:13`
  - Configures the Java 17 Gradle project used by `compileJava`.
- `frontend/package.json:13`
  - Maps `typecheck` to `tsc --noEmit`.
- `frontend/tsconfig.json:14`
  - Disables compiler output.
- `frontend/tsconfig.json:18`
  - Enables strict TypeScript checking.
- Process output was captured directly in the executing PowerShell sessions; no standalone log was created because this WI permits only two output files.

## Commands and Outputs

### Java compile check

```powershell
Set-Location C:\Users\jm991\Desktop\project\ATStudio
.\gradlew.bat compileJava
```

Result excerpt:

```text
> Task :compileJava UP-TO-DATE

BUILD SUCCESSFUL in 2s
1 actionable task: 1 up-to-date
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.3.0/userguide/configuration_cache_enabling.html
```

| Metric | Value |
|---|---|
| Process exit code | `0` |
| Wall-clock elapsed | `2.761` seconds (`00:00:02.7614377`) |
| Timeout limit | 300 seconds |
| Timed out | No |
| Compilation warnings | None |
| Informational notices | Gradle configuration-cache recommendation |

### TypeScript typecheck

```powershell
Set-Location C:\Users\jm991\Desktop\project\ATStudio\frontend
npm run typecheck
```

Resolved script and output:

```text
> atstudio-frontend@0.1.0 typecheck
> tsc --noEmit
```

| Metric | Value |
|---|---|
| Process exit code | `0` |
| Wall-clock elapsed | `7.067` seconds (`00:00:07.0674001`) |
| Timeout limit | 300 seconds |
| Timed out | No |
| TypeScript diagnostics | None |

## Generated-State Verification

Baseline inspection:

```powershell
git ls-files '*tsbuildinfo*'
git status --short -- frontend/tsconfig.tsbuildinfo
Get-FileHash -Algorithm SHA256 frontend/tsconfig.tsbuildinfo
```

Post-run inspection:

```powershell
git status --short -- frontend/tsconfig.tsbuildinfo
Get-FileHash -Algorithm SHA256 frontend/tsconfig.tsbuildinfo
git diff --numstat -- frontend/tsconfig.tsbuildinfo
git diff --cached --numstat -- frontend/tsconfig.tsbuildinfo
```

| Observation | Before | After |
|---|---|---|
| Tracked file | `frontend/tsconfig.tsbuildinfo` | `frontend/tsconfig.tsbuildinfo` |
| Git status | Clean | Clean |
| SHA-256 | `432D8E84A411B0C34B1DA11800B68EDC2F1A8FEDF925D4D6290F544785E40F90` | Same |
| Unstaged/staged diff | None | None |

- Conclusion: `npm run typecheck` did not alter the tracked build-info file.
- No restore command was run because there was no newly generated delta to restore.

## Repository Integrity Verification

- `git status --short -- src frontend build.gradle` showed only the two pre-existing untracked files `frontend/vite.err.log` and `frontend/vite.out.log`.
- No tracked Java source, TypeScript source, compiler configuration, or build configuration changed during this WI.
- All unrelated pre-existing workspace changes were preserved.

## Risks / Limitations / Rollback

- `compileJava` was up-to-date, so Gradle verified task inputs and reused its existing output rather than recompiling every source file from scratch.
- This WI does not verify test compilation, tests, lint, formatting, production bundles, runtime behavior, or coverage; those belong to separate WIs.
- The workspace is shared and dirty, so repository state may change after this evidence snapshot.
- Rollback, only if explicitly requested: remove only these two files:
  - `deliverables/user/WI-20260711-ATS-011-summary.md`
  - `deliverables/agent/WI-20260711-ATS-011-evidence-pack.md`

## Follow-ups

- `WI-20260711-ATS-017` may consume this passing compile-time contract baseline.
