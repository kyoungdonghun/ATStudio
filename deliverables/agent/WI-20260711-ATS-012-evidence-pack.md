# Evidence Pack: WI-20260711-ATS-012

## Summary

- Executed frontend ESLint and Prettier checks without auto-fix: ESLint passed; Prettier reported drift in 143 files.

## Scope / DoD Check

- [x] Ran ESLint independently without `--fix`.
- [x] Ran Prettier check independently without `--write`.
- [x] Recorded exact commands, effective commands, exit codes, counts, elapsed times, and representative paths.
- [x] Distinguished ESLint errors, ESLint warnings, and Prettier formatting drift.
- [x] Limited file creation to this WI's user summary and agent evidence pack.

## Reference Documents

### Injected Context from Handoff

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | System constitution and traceability rules |
| 0 | `docs/standards/development-standards.md` | QA-FE development and evidence standards |
| 1 | `docs/policies/quality-gates.md` | Operational quality checks |
| 2 | `.agents/skills/eslint/SKILL.md` | ESLint read-only check workflow |
| 2 | `.agents/skills/prettier/SKILL.md` | Prettier read-only check workflow |

### Requirement and Input Pointers

- `deliverables/user/REQ-20260711-ATS-001.md`: Phase 3 `WI-012` and quality-gate context.
- `frontend/package.json`: source of the `lint` and `format` scripts.
- `frontend/.eslintrc.cjs`: ESLint configuration actually present and used.
- `frontend/.prettierrc`: Prettier configuration actually present and used.
- Handoff mismatch: `frontend/eslint.config.js` is referenced but absent in the current checkout.
- Context rule source: `.claude/config/context-injection-rules.json`; assignee `qa-fe`, required tier `[0]`, with the handoff explicitly adding the listed quality policy and skills.

## Baseline

- Branch: `dev/kyoung`
- HEAD: `27d22446e5d21324dadcfcb322dbe51704dfe914`
- Scoped pre-check status for `frontend/src`, frontend lint/format inputs, and this WI's two outputs: clean/no entries.
- Other concurrent worktree changes existed outside this WI and were not modified or reverted.

## Evidence Pointers

- Files created:
  - `deliverables/user/WI-20260711-ATS-012-summary.md`: user-facing verdict and counts.
  - `deliverables/agent/WI-20260711-ATS-012-evidence-pack.md`: reproducible command and result evidence.
- Command definitions: `frontend/package.json` (`scripts.lint`, `scripts.format`).
- ESLint rules: `frontend/.eslintrc.cjs`.
- Prettier rules: `frontend/.prettierrc`.

## Commands and Outputs

All commands were run from `frontend/` and were read-only checks.

| Command | Effective command / purpose | Exit code | Elapsed | Result |
|---|---|---:|---:|---|
| `npm run lint` | `eslint src --ext .ts,.tsx --max-warnings 0` | 0 | 5,368 ms | PASS: 0 errors, 0 warnings |
| `npm run format` | `prettier --check "src/**/*.{ts,tsx,css}"` | 1 | 7,362 ms | FAIL: formatting drift in 143 files |
| `npx prettier --list-different "src/**/*.{ts,tsx,css}"` | Read-only drift classification | 1 | 4,977 ms | Confirmed the same 143 drift paths |

Runtime/tool versions:

- Node.js: `v24.14.0`
- npm: `11.9.0`
- ESLint: `v8.57.1`
- Prettier: `3.8.1`

## Counts

| Metric | Count |
|---|---:|
| ESLint-eligible `.ts` files | 35 |
| ESLint-eligible `.tsx` files | 91 |
| ESLint-eligible total | 126 |
| ESLint errors | 0 |
| ESLint warnings | 0 |
| Prettier-targeted `.ts` files | 35 |
| Prettier-targeted `.tsx` files | 91 |
| Prettier-targeted `.css` files | 71 |
| Prettier-targeted total | 197 |
| Prettier drift `.ts` files | 24 |
| Prettier drift `.tsx` files | 75 |
| Prettier drift `.css` files | 44 |
| Prettier drift total | 143 |
| Prettier files without reported drift | 54 |

Prettier's `[warn]` prefix denotes formatting drift output. It is not counted as an ESLint warning.

## Representative Formatting Drift

- `frontend/src/api/albums.ts`
- `frontend/src/App.tsx`
- `frontend/src/components/album/AlbumCard.module.css`
- `frontend/src/components/playlist/AddToPlaylistModal.test.tsx`
- `frontend/src/pages/admin/DashboardPage.tsx`
- `frontend/src/pages/auth/LoginPage.test.tsx`
- `frontend/src/pages/public/HomePage.tsx`
- `frontend/src/router/index.tsx`
- `frontend/src/store/authStore.ts`
- `frontend/src/styles/tokens.css`

## Tests

- `npm run lint`: PASS, exit code 0.
- `npm run format`: FAIL, exit code 1 due only to reported formatting drift.
- No auto-fix, formatting write, build, typecheck, unit test, browser test, or source edit was performed.

## Risks / Rollback

- Risk: broad formatting drift can obscure future functional diffs and prevents the configured formatting gate from passing.
- Limitation: results are a snapshot of a shared worktree at the recorded baseline; concurrent changes after measurement are not represented.
- Rollback: remove only `deliverables/user/WI-20260711-ATS-012-summary.md` and `deliverables/agent/WI-20260711-ATS-012-evidence-pack.md`, and only when explicitly requested.

## Follow-up / WI Chain

- This WI blocks `WI-20260711-ATS-018` according to the handoff.
- Any formatting remediation must be authorized and tracked separately; it was intentionally not performed here.
