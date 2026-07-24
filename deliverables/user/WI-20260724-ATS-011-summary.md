---
version: 1.0
last_updated: 2026-07-24
project: ATS
owner: qa-fe
category: wi-summary
status: failed
dependencies:
  - path: REQ-20260724-ATS-002.md
    reason: Approved release rehearsal scope
  - path: ../agent/WI-20260724-ATS-011-handoff.md
    reason: WI execution and output contract
---

# WI-20260724-ATS-011 Frontend Clean Verification Summary

## Result

**FAIL**: the lockfile-based clean frontend install, tests, coverage, typecheck,
ESLint, and production build passed, but the required Prettier gate failed on
255 files. WI-20260724-ATS-014 remains blocked by this result.

## Clone And Install

| Item | Result |
|---|---|
| Clone | `C:\Users\jm991\AppData\Local\ATStudio\release-rehearsal-3147873-20260724` |
| Expected / initial / final HEAD | `3147873c42bfd7883fdaa92922c0485e5fc72621` |
| Branch / remote-tracking ref | `codex/p1-acceptance-hardening`; same commit |
| Frontend pre-install state | `node_modules`, `coverage`, `dist`, and `.qa-npm-cache` absent |
| Lockfile | version 3; 371 package entries; SHA-256 `A021CF4AF29C7B423A4C5C8158961754C9BC251594BD61AB511E4BE30427F504` |
| Toolchain | Node `v24.14.0`; npm `11.9.0`; Git `2.53.0.windows.1` |
| Install | `npm ci --cache .qa-npm-cache`; exit 0; 321 added / 322 audited; 7.357s |

Install warnings:

- Six deprecation notices: `rimraf@3.0.2`, `inflight@1.0.6`, `glob@7.2.3`,
  `@humanwhocodes/config-array@0.13.0`,
  `@humanwhocodes/object-schema@2.0.3`, and `eslint@8.57.1`.
- npm reported 2 moderate-severity vulnerabilities and 73 packages seeking funding.
- npm reported that version `11.18.0` is available. No update was performed.

## Gate Results

| Gate | Exact result | Status |
|---|---|---|
| Tests | 63 files, 468 passed, 0 failed, 0 skipped; 26.063s command duration | PASS |
| Coverage | Statements 86.73%, branches 76.98%, functions 85.41%, lines 88.75%; 63 files / 468 tests passed | PASS |
| Typecheck | `tsc --noEmit`; exit 0; 7.369s | PASS |
| ESLint | `eslint src --ext .ts,.tsx --max-warnings 0`; exit 0; no warnings; 6.181s | PASS |
| Prettier | `prettier --check . --ignore-unknown`; 255 files reported; exit 1; 8.320s | **FAIL** |
| Build | `tsc -b && vite build`; 266 modules; 133 files / 949,725 bytes; exit 0; 8.728s | PASS |

Configured aggregate coverage thresholds all passed: statements 80%, branches
70%, functions 80%, and lines 80%.

## Failure Detail

The formatting failure is reproducible from the fresh Windows checkout:

- Git config is `core.autocrlf=true`.
- Representative tracked files report `i/lf w/crlf`.
- The repository `.gitattributes` sets EOL only for `gradlew` and `*.bat`; it
  does not pin frontend text files to LF.
- Prettier reformats the representative CRLF files to LF.

No formatting write was performed because the WI forbids source changes.

## Build And Clone Status

- Vite emitted no build warning. The largest bundle was
  `dist/assets/index-rKfqIvyT.js` at 343.19 kB (111.74 kB gzip).
- Final frontend tracked diff: 0; staged diff: 0.
- Generated frontend paths: ignored `node_modules/`, `coverage/`, `dist/`, and
  `tsconfig.tsbuildinfo`; untracked `frontend/.qa-npm-cache/`.
- A concurrent, out-of-scope tracked change appeared after the initial clean
  check in `output/pdf/atstudio-client-testing-guide.manifest.json`
  (14 insertions / 14 deletions). This WI did not modify or revert it.
- Source changes, commits, pushes, secret access, and original-workspace
  `node_modules` access: none.

## Detailed Evidence

- `deliverables/agent/WI-20260724-ATS-011-evidence-pack.md`
