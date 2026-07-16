---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: se
category: audit
status: stable
dependencies:
  - path: REQ-20260716-ATS-002.md
    reason: Approved remediation scope
  - path: ../../docs/design/remaining-remediation-design-20260716.md
    reason: P2-16 and P2-17 remediation boundaries
---

# WI-20260716-ATS-011 Summary

## Outcome

Completed the approved quality instrumentation, production and development dependency remediation, and full frontend formatting baseline on `codex/p1-acceptance-hardening`.

- Backend tests now generate JaCoCo HTML and XML with `gradlew.bat test jacocoTestReport`.
- Frontend tests now generate Vitest V8 text, HTML, and JSON summary coverage with `npm run test:coverage`.
- Coverage is recorded as an observational baseline only. No minimum threshold or coverage verification gate was added.
- The full frontend Prettier command now checks the supported source, test, and configuration tree. The initial 119-file failure inventory was mechanically formatted and the final full check passes.
- Production dependency advisories were reduced from five package-level findings to zero without `--force` or a major framework upgrade.
- Development-toolchain advisories were reduced from eight package-level findings to zero through compatible Vite 6 and transitive patch updates.
- Generated backend/frontend reports, build output, and TypeScript build information are ignored.

## Production Dependency Remediation

| Dependency path | Before | After | Result |
|---|---:|---:|---|
| direct `axios` | 1.13.6 | 1.18.1 | High-severity direct advisory range removed |
| `axios -> follow-redirects` | 1.15.11 | 1.16.0 | Cross-domain custom-auth-header advisory removed |
| `axios -> form-data` | 4.0.5 | 4.0.6 | Multipart CRLF-injection advisory removed |
| direct `react-router-dom` | 6.30.3 | 6.30.4 | Compatible v6 patch applied |
| `react-router-dom -> react-router` | 6.30.3 | 6.30.4 | Protocol-relative redirect advisory removed |

`npm audit --omit=dev` changed from 5 vulnerabilities (2 high, 3 moderate) to 0 vulnerabilities. The production dependency result remains zero after the development-toolchain update.

## Development Dependency Remediation

The unfiltered `npm audit` changed from 8 vulnerabilities (1 low, 3 moderate, 4 high) to 0 vulnerabilities. `npm audit fix --dry-run --json` identified a non-force, non-major path for all eight findings. The compatible update applied these security-relevant versions:

| Dependency path | Before | After |
|---|---:|---:|
| direct `vite` | 6.4.1 | 6.4.3 |
| `@vitejs/plugin-react -> @babel/core` | 7.29.0 | 7.29.7 |
| ESLint/minimatch paths -> `brace-expansion` | 1.1.12 / 5.0.4 | 1.1.16 / 5.0.7 |
| `eslint -> flat-cache -> flatted` | 3.3.4 | 3.4.2 |
| `eslint -> js-yaml` | 4.1.1 | 4.3.0 |
| Vite/Vitest paths -> `picomatch` | 4.0.3 | 4.0.5 |
| `vite -> postcss` | 8.5.8 | 8.5.19 |
| `jsdom -> undici` | 7.25.0 | 7.28.0 |

The declared Vite range is now `^6.4.3`. No override, `--force`, or major upgrade was used. A clean `npm ci` confirmed the lockfile installs and executes Vite 6.4.3. Focused Vite-config, navigation, unsafe-return-target, refresh redirect, Axios error classification, cancellation, and stale-response regressions passed: 9 files / 63 tests.

## Coverage Baseline

No acceptance threshold is attached to these measurements.

| Runtime | Metric | Covered / Total | Baseline |
|---|---|---:|---:|
| Backend JaCoCo | Instructions | 34,615 / 45,457 | 76.15% |
| Backend JaCoCo | Branches | 2,226 / 3,770 | 59.05% |
| Backend JaCoCo | Lines | 7,653 / 9,964 | 76.81% |
| Backend JaCoCo | Methods | 1,451 / 1,895 | 76.57% |
| Frontend V8 | Statements | 2,347 / 6,803 | 34.49% |
| Frontend V8 | Branches | 1,532 / 4,505 | 34.00% |
| Frontend V8 | Functions | 515 / 1,851 | 27.82% |
| Frontend V8 | Lines | 2,168 / 6,119 | 35.43% |

The frontend baseline includes untested production modules through `coverage.include`. Exclusions are limited to declaration files, test files, test setup, and the `src/main.tsx` bootstrap. No product module was excluded to improve the percentages.

## Formatting Baseline

- Initial full-tree command: `npx prettier --list-different . --ignore-unknown`
- Initial result: exit 1, 119 files required formatting.
- Mechanical write: `npx prettier --write . --ignore-unknown`
- Final repository command: `npm run format`
- Final result: PASS, all matched files use Prettier style.

Of the 119 files, `package.json` and `package-lock.json` also contain the approved dependency/script changes. The other 117 files are formatting-only. The exact inventory is in `deliverables/agent/WI-20260716-ATS-011-evidence-pack.md`.

## Verification

- Backend: `gradlew.bat test jacocoTestReport` passed; 1,046 tests recorded across 146 suites, 0 failures/errors, 9 skipped.
- Frontend focused Vite/config/navigation/Axios regression: 9 files / 63 tests passed.
- Frontend full Vitest: 38 files / 180 tests passed.
- Frontend V8 coverage: 38 files / 180 tests passed and generated HTML/JSON reports.
- TypeScript typecheck: passed.
- ESLint: passed with zero warnings/errors.
- Vite 6.4.3 production build: passed; 264 modules transformed.
- Full frontend Prettier: passed.
- Production dependency audit: 0 vulnerabilities.
- Unfiltered dependency audit: 0 vulnerabilities; no unresolved advisory remains.
- Documentation validation: passed with `python .agents/skills/validate-docs/scripts/validate_docs.py`.
- `git diff --check`: passed.

The first JaCoCo-enabled backend run exhausted the default Gradle test-worker heap in the existing oversized-image test. The test JVM now has a 1GB maximum heap; this setting is test-only and the required default command subsequently passed.

## Residual Risks

- Coverage is a baseline, not proof of release readiness. Frontend function/branch coverage and backend branch coverage expose material untested paths for later risk-based test work.
- `npm ci` still reports deprecation notices for the existing ESLint 8-era toolchain and legacy transitive utilities. These are not audit advisories; a future reviewed major toolchain migration can address support lifecycle separately.
- HTML/JSON/XML coverage reports are generated and ignored, so reviewers must rerun the documented commands rather than expect reports in Git.

## Scope Preservation

- Product behavior, public full-track listening, download entitlement, recurring card billing, database behavior, and single-server topology were unchanged.
- The pre-existing `frontend/tsconfig.tsbuildinfo` bytes were restored after build verification. SHA-256 remained `B6A42AD2CD32A5AD04D06C55B8B1B26DD9B1894AFDAF7B508DFF16EF0C60F22A`.
- The client-demo worktree, branch, Cloudflare runtime, and public processes were not inspected or modified.
- Nothing was staged, committed, or pushed.

## Related Documents

- [Evidence Pack](../agent/WI-20260716-ATS-011-evidence-pack.md): Exact file inventory, counters, commands, and rollback.
- [Approved REQ](REQ-20260716-ATS-002.md): Approved remediation scope.
- [Remaining Remediation Design](../../docs/design/remaining-remediation-design-20260716.md): P2-16 and P2-17 ownership.
