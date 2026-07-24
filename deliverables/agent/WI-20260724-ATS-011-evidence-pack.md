---
version: 1.0
last_updated: 2026-07-24
project: ATS
owner: qa-fe
category: evidence-pack
status: failed
dependencies:
  - path: WI-20260724-ATS-011-handoff.md
    reason: WI execution contract
  - path: ../user/REQ-20260724-ATS-002.md
    reason: Approved release rehearsal scope
  - path: ../../docs/standards/core-principles.md
    reason: Tier 0 constitution
  - path: ../../docs/standards/development-standards.md
    reason: Tier 0 development and quality rules
  - path: ../../docs/policies/quality-gates.md
    reason: Tier 1 operational quality checklist
  - path: ../../docs/standards/frontend-standards.md
    reason: Frontend architecture and gate commands
---

# Evidence Pack: WI-20260724-ATS-011

## Summary

- FAIL: the fresh-clone frontend passed clean install, tests, aggregate coverage,
  typecheck, ESLint, and production build, but failed the required Prettier
  check on 255 files.

## Scope / DoD Check

- [x] Ran lockfile-based clean installation from an absent `node_modules`.
- [x] Ran the complete Vitest suite separately and with coverage.
- [x] Passed configured aggregate coverage thresholds.
- [x] Passed TypeScript typecheck.
- [x] Passed ESLint with zero allowed warnings.
- [ ] Passed Prettier check.
- [x] Passed production build and recorded output and warnings.
- [x] Preserved zero frontend tracked and staged diffs.
- [x] Avoided source writes, commits, pushes, secrets, and the original
  workspace `node_modules`.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and execution boundaries |
| 0 | `docs/standards/development-standards.md` | Test, coverage, and evidence requirements |
| 1 | `docs/policies/quality-gates.md` | Operational quality checklist |
| 2 | `docs/standards/frontend-standards.md` | Active React/TypeScript architecture |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | React maintenance guidance |
| Context | `deliverables/user/REQ-20260724-ATS-002.md` | Approved release rehearsal scope |
| Dependency | `deliverables/agent/WI-20260724-ATS-010-evidence-pack.md` | Fresh clone and backend PASS |
| Contract | `deliverables/agent/WI-20260724-ATS-011-handoff.md` | Commands, constraints, and outputs |
| Build | `frontend/package.json`, `frontend/package-lock.json` | Scripts and locked dependency graph |

## Clone And Preconditions

| Evidence | Value |
|---|---|
| Clone path | `C:\Users\jm991\AppData\Local\ATStudio\release-rehearsal-3147873-20260724` |
| Branch | `codex/p1-acceptance-hardening` |
| Expected commit | `3147873c42bfd7883fdaa92922c0485e5fc72621` |
| Initial HEAD | `3147873c42bfd7883fdaa92922c0485e5fc72621` |
| Initial remote-tracking ref | `3147873c42bfd7883fdaa92922c0485e5fc72621` |
| Final HEAD | `3147873c42bfd7883fdaa92922c0485e5fc72621` |
| Final remote-tracking ref | `3147873c42bfd7883fdaa92922c0485e5fc72621` |
| Initial frontend generated paths | `node_modules=False`, `coverage=False`, `dist=False`, `.qa-npm-cache=False` |
| Initial tracked / staged diff | 0 / 0 |
| Pre-existing clone state | Untracked `.qa-gradle-user-home/`; ignored `.gradle/` and `build/` from WI-010 |

Hashes before and after the gates were identical:

| File | SHA-256 |
|---|---|
| `frontend/package.json` | `ECF637D7A4C33986D0AA7F987F2A351D75F1E04E22431EC1F0DEDBAFCD7B216E` |
| `frontend/package-lock.json` | `A021CF4AF29C7B423A4C5C8158961754C9BC251594BD61AB511E4BE30427F504` |

`package-lock.json` uses lockfile version 3 and contains 371 package entries.

## Toolchain

| Tool | Exact version |
|---|---|
| Node | `v24.14.0` |
| npm | `11.9.0` |
| Git | `git version 2.53.0.windows.1` |
| Vitest | `4.1.4` |
| TypeScript | `5.6.3` |
| ESLint | `8.57.1` |
| Prettier | `3.8.1` |
| Vite | `6.4.3` |

## Dependency Installation

Command:

```powershell
npm ci --cache .qa-npm-cache
```

Result:

- Start: `2026-07-24T13:10:12.0600639+09:00`
- End: `2026-07-24T13:10:19.4170265+09:00`
- Duration: 7.357 seconds
- Exit code: 0
- Added: 321 packages
- Audited: 322 packages
- Funding notice: 73 packages
- Audit result: 2 moderate-severity vulnerabilities

The cache path was created inside the fresh clone. The original repository
`node_modules` and its dependency tree were not accessed.

Exact direct dependency resolution from `npm ls --depth=0`:

| Package | Resolved version |
|---|---|
| `@testing-library/jest-dom` | 6.9.1 |
| `@testing-library/react` | 16.3.2 |
| `@types/react-dom` | 18.3.7 |
| `@types/react` | 18.3.28 |
| `@typescript-eslint/eslint-plugin` | 8.56.1 |
| `@typescript-eslint/parser` | 8.56.1 |
| `@vitejs/plugin-react` | 4.7.0 |
| `@vitest/coverage-v8` | 4.1.4 |
| `axios` | 1.18.1 |
| `eslint-config-prettier` | 9.1.2 |
| `eslint-plugin-react-hooks` | 5.2.0 |
| `eslint-plugin-react-refresh` | 0.4.26 |
| `eslint` | 8.57.1 |
| `jsdom` | 29.0.2 |
| `prettier` | 3.8.1 |
| `react-dom` | 18.3.1 |
| `react-router-dom` | 6.30.4 |
| `react` | 18.3.1 |
| `typescript` | 5.6.3 |
| `vite` | 6.4.3 |
| `vitest` | 4.1.4 |
| `zustand` | 5.0.11 |

Install warnings, exactly classified:

1. Deprecated `rimraf@3.0.2`.
2. Deprecated and unsupported `inflight@1.0.6`, with a memory-leak warning.
3. Deprecated `glob@7.2.3`, with an old-version security warning.
4. Deprecated `@humanwhocodes/config-array@0.13.0`.
5. Deprecated `@humanwhocodes/object-schema@2.0.3`.
6. Unsupported ESLint version `8.57.1`.
7. npm audit reported 2 moderate-severity vulnerabilities.
8. npm update notice: `11.9.0 -> 11.18.0`.

No dependency or npm version was changed.

## Tests

Command:

```powershell
npm test
```

Result:

| Metric | Exact result |
|---|---:|
| Test files | 63 passed / 63 total |
| Tests | 468 passed / 468 total |
| Failed | 0 |
| Skipped | 0 |
| Vitest duration | 24.46 seconds |
| Command duration | 26.063 seconds |
| Exit code | 0 |

Timing:

- Start: `2026-07-24T13:10:34.4438172+09:00`
- End: `2026-07-24T13:11:00.5066427+09:00`

## Coverage

Command:

```powershell
npm run test:coverage
```

Result:

- 63 test files passed.
- 468 tests passed.
- Vitest duration: 25.42 seconds.
- Command duration: 27.047 seconds.
- Exit code: 0.
- Report pointers:
  - Clone `frontend/coverage/index.html`
  - Clone `frontend/coverage/coverage-summary.json`

| Metric | Covered | Total | Coverage | Threshold | Result |
|---|---:|---:|---:|---:|---|
| Statements | 6,095 | 7,027 | 86.73% | 80% | PASS |
| Branches | 3,626 | 4,710 | 76.98% | 70% | PASS |
| Functions | 1,616 | 1,892 | 85.41% | 80% | PASS |
| Lines | 5,597 | 6,306 | 88.75% | 80% | PASS |

Timing:

- Start: `2026-07-24T13:11:10.6161657+09:00`
- End: `2026-07-24T13:11:37.6626923+09:00`

## Static Gates

| Gate | Command | Duration | Exit | Result |
|---|---|---:|---:|---|
| Typecheck | `npm run typecheck` | 7.369s | 0 | PASS |
| ESLint | `npm run lint` | 6.181s | 0 | PASS, zero warnings |
| Prettier | `npm run format` | 8.320s | 1 | FAIL, 255 files |

Commands resolved to:

```text
typecheck: tsc --noEmit
lint: eslint src --ext .ts,.tsx --max-warnings 0
format: prettier --check . --ignore-unknown
```

The three checks ran from approximately `13:11:49+09:00` through
`13:11:58+09:00`.

### Prettier Failure Evidence

Prettier ended with:

```text
Code style issues found in 255 files. Run Prettier with --write to fix.
```

The listed set spans frontend configuration, package files, TypeScript/TSX,
CSS modules, tests, and Vite configuration. No `--write` command was run.

Read-only diagnosis:

| Evidence | Result |
|---|---|
| Git config | `core.autocrlf=true` |
| Repository `.gitattributes` | Only `/gradlew text eol=lf`, `*.bat text eol=crlf`, and `*.jar binary` |
| `frontend/package.json` | `i/lf w/crlf`; 43 CRLF sequences; Prettier output 43 LF sequences |
| `frontend/src/App.tsx` | `i/lf w/crlf`; 7 CRLF sequences; Prettier output 7 LF sequences |
| `frontend/src/styles/tokens.css` | `i/lf w/crlf`; 152 CRLF sequences; Prettier output 152 LF sequences |

This proves a Windows checkout EOL mismatch for representative failed files.
The repository does not currently pin general frontend text files to LF.

## Production Build

Command:

```powershell
npm run build
```

Resolved command:

```text
tsc -b && vite build
```

Result:

| Evidence | Value |
|---|---|
| Start | `2026-07-24T13:12:05.8004541+09:00` |
| End | `2026-07-24T13:12:14.5280227+09:00` |
| Command duration | 8.728 seconds |
| Vite duration | 2.06 seconds |
| Exit code | 0 |
| Modules transformed | 266 |
| Output files | 133 |
| Total output bytes | 949,725 |
| Build warnings | None |
| `dist/index.html` SHA-256 | `DAD21A668CA4BD0CD518CC6675A5FAE34351C5107AE281309CBD998B81744056` |

Largest outputs:

| File | Raw size | Gzip size when reported |
|---|---:|---:|
| `dist/assets/index-rKfqIvyT.js` | 343.19 kB | 111.74 kB |
| `dist/assets/PaymentOperationsPage-CaZBLmzM.js` | 40.76 kB | 9.70 kB |
| `dist/assets/index-DY0aaWGq.css` | 33.73 kB | 6.68 kB |

## Final Clone Status

Final identity:

```text
HEAD=3147873c42bfd7883fdaa92922c0485e5fc72621
REMOTE_TRACKING=3147873c42bfd7883fdaa92922c0485e5fc72621
BRANCH=codex/p1-acceptance-hardening
```

Frontend status:

- Tracked diff count: 0.
- Staged diff count: 0.
- Ignored generated paths: `frontend/node_modules/`, `frontend/coverage/`,
  `frontend/dist/`, and `frontend/tsconfig.tsbuildinfo`.
- Untracked generated path: `frontend/.qa-npm-cache/`.

Whole-clone status:

```text
 M output/pdf/atstudio-client-testing-guide.manifest.json
?? .qa-gradle-user-home/
?? frontend/.qa-npm-cache/
```

The manifest was clean during the initial precheck and showed a last-write time
of `2026-07-24 13:11:04+09:00`, while this WI's test command was running. Its
final diff is 14 insertions and 14 deletions. It is outside the frontend scope
and was neither created, modified, nor reverted by this WI. The Gradle user
home predated this WI and came from WI-010.

A broad ignored-file status traversal also emitted four Windows
`Filename too long` warnings under the pre-existing backend test HTML report.
These warnings did not affect Git object state or any frontend gate.

## Files Written

Official workspace only:

- `deliverables/user/WI-20260724-ATS-011-summary.md`
- `deliverables/agent/WI-20260724-ATS-011-evidence-pack.md`

Runtime source changes: none.

## Risks / Rollback

- Risk: Windows clean clones with `core.autocrlf=true` fail the required
  Prettier gate even though tests, coverage, typecheck, ESLint, and build pass.
- Risk: npm reported 2 moderate vulnerabilities and six deprecated packages;
  remediation was not in this WI.
- Risk: the shared clone has an out-of-scope concurrent PDF manifest change, so
  whole-clone purity must be rechecked by the owning WI before final review.
- Rollback: no source, dependency manifest, commit, remote, database, or secret
  state was changed. Generated frontend outputs can be removed only by the
  final cleanup owner under the approved exact-path cleanup scope.

## Follow-ups

- WI-20260724-ATS-014 must remain blocked because the WI-011 Prettier acceptance
  criterion did not pass.
- A separately approved corrective WI should define repository-wide EOL
  normalization for frontend text files and rerun the same fresh-clone gates.
