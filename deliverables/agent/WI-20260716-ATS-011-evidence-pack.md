---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: se
category: evidence-pack
status: stable
related_wi: WI-20260716-ATS-011
dependencies:
  - path: ../user/REQ-20260716-ATS-002.md
    reason: Approved request and quality gate
  - path: WI-20260716-ATS-011-handoff.md
    reason: Execution contract
  - path: ../../docs/design/remaining-remediation-design-20260716.md
    reason: P2-16 and P2-17 design boundaries
---

# Evidence Pack: WI-20260716-ATS-011

## Change Summary

- Added report-only JaCoCo HTML/XML generation and a 1GB Gradle test-worker heap required by the existing oversized-image test under instrumentation.
- Added Vitest V8 text/HTML/JSON coverage, a non-watch npm script, and explicit generated-artifact ignores.
- Removed all production dependency audit findings with compatible Axios and React Router v6 patch updates; no `--force` or major upgrade was used.
- Removed all eight development-toolchain audit findings with Vite 6.4.3 and compatible transitive updates; no override, `--force`, or major upgrade was used.
- Replaced the source-only Prettier check with a full frontend tree check, inventoried 119 initial failures, and mechanically formatted them to PASS.
- Recorded actual coverage baselines without adding thresholds or percentage-driven exclusions.

## Scope and DoD

- [x] Production dependency audit before/after evidence recorded.
- [x] Compatible direct/transitive production advisories remediated; no production residual remains.
- [x] Unfiltered audit before/dry-run/after evidence recorded; all compatible development advisories remediated and no audit residual remains.
- [x] Navigation/redirect and Axios cancellation/error focused regressions passed.
- [x] `gradlew.bat test jacocoTestReport` generates backend HTML/XML.
- [x] `npm run test:coverage` generates frontend text/HTML/JSON summary reports.
- [x] Backend/frontend baseline counters recorded without thresholds.
- [x] Initial 119-file Prettier inventory recorded; full repository command passes.
- [x] Typecheck, ESLint, Vitest, build, docs validation, and diff integrity pass.
- [x] Coverage/build output remains ignored; `tsconfig.tsbuildinfo` is preserved byte-for-byte.
- [x] No product behavior, client-demo runtime, Git stage/commit/push, DB, or secret change.

## Evidence Pointers

### Configuration and Dependency Changes

- `build.gradle:3`: applies Gradle's JaCoCo plugin.
- `build.gradle:67-70`: retains JUnit Platform and sets the instrumented test JVM maximum heap to 1GB. The first two coverage runs proved the default heap was insufficient for `CanonicalImageServiceTest.canonicalizeThumbnail_oversizedInput_rejectedWithIoLarge`.
- `build.gradle:72-84`: pins JaCoCo 0.8.14 and enables XML/HTML reports; no verification rule or threshold exists.
- `frontend/package.json:12`: adds non-watch `test:coverage` as `vitest run --coverage`.
- `frontend/package.json:15`: defines the full-tree check as `prettier --check . --ignore-unknown`.
- `frontend/package.json:18,21,32,40`: records patched Axios/React Router ranges, `@vitest/coverage-v8` 4.1.4, and Vite `^6.4.3`.
- `frontend/package-lock.json:2381-2388`: resolves `axios@1.18.1`.
- `frontend/package-lock.json:3218-3246`: resolves `follow-redirects@1.16.0` and `form-data@4.0.6`.
- `frontend/package-lock.json:4339-4378`: resolves `react-router@6.30.4` and `react-router-dom@6.30.4`.
- `frontend/package-lock.json:2107-2138`: resolves `@vitest/coverage-v8@4.1.4`.
- `frontend/package-lock.json:120-166,2426-2443,3211-3228,3673-3695`: resolves patched Babel, brace-expansion, flatted, and js-yaml development paths.
- `frontend/package-lock.json:4158-4198,4800-4822,4851-4910`: resolves patched picomatch, PostCSS, undici, and Vite development paths.
- `frontend/vite.config.ts:145-150`: configures V8, `coverage/`, text/HTML/JSON summary reporters, full production source inclusion, and conventional exclusions only.
- `frontend/.gitignore:4-5`: ignores `coverage/` and `*.tsbuildinfo`; root `.gitignore:3` already ignores backend `build/`.
- `deliverables/user/WI-20260716-ATS-011-summary.md`: user-facing result.
- `deliverables/agent/WI-20260716-ATS-011-evidence-pack.md`: this reproducibility record.

### Coverage Inclusion and Exclusions

`frontend/vite.config.ts` includes every `src/**/*.{ts,tsx}` production candidate, including untested files. The only exclusions are:

| Pattern | Reason |
|---|---|
| `src/**/*.d.ts` | TypeScript declaration artifacts have no runtime statements |
| `src/**/*.test.{ts,tsx}` | Test code is not application runtime code |
| `src/test/**` | Test setup infrastructure is not application runtime code |
| `src/main.tsx` | Conventional DOM/bootstrap entry point |

There are no coverage thresholds, verification tasks, directory-level product exclusions, or exclusions chosen from observed percentages.

## Production Dependency Audit

### Before

The owned `frontend/package.json` and `frontend/package-lock.json` were clean against HEAD at WI start. The human-readable command was run before dependency edits, and the machine-readable result was reproduced from those exact HEAD files in `%TEMP%/WI-20260716-ATS-011-audit-before` without changing the worktree.

```powershell
cd frontend
npm audit --omit=dev
npm audit --omit=dev --json
```

- Human result: exit 1; 5 package-level vulnerabilities (2 high, 3 moderate).
- JSON metadata: `high=2`, `moderate=3`, `total=5`; `prod=36`.

| Dependency path | Installed before | Audit affected range | Runtime relevance | Compatible remediation |
|---|---:|---|---|---|
| root -> `axios` | 1.13.6 | `1.0.0 - 1.15.2` | Direct production HTTP client; audit grouped 22 Axios advisories | 1.18.1 |
| root -> `axios` -> `follow-redirects` | 1.15.11 | `<=1.15.11` | Axios Node HTTP redirect adapter; custom auth headers could cross domains | 1.16.0 |
| root -> `axios` -> `form-data` | 4.0.5 | `4.0.0 - 4.0.5` | Axios Node multipart adapter; unescaped field/file names could inject CRLF | 4.0.6 |
| root -> `react-router-dom` -> `react-router` | 6.30.3 | `6.7.0 - 6.30.3` | Data-router redirects could reinterpret a `//` path as protocol-relative | 6.30.4 |
| root -> `react-router-dom` | 6.30.3 | dependent on vulnerable `react-router` | Direct production routing package | 6.30.4 |

Commands used for the compatible update:

```powershell
npm install axios@^1.18.1 react-router-dom@^6.30.4
npm update form-data
npm install --save-dev @vitest/coverage-v8@4.1.4
```

No `npm audit fix --force`, major framework upgrade, or arbitrary override was used.

### After

```powershell
npm ls --omit=dev axios follow-redirects form-data react-router react-router-dom
npm audit --omit=dev
npm audit --omit=dev --json
```

- Resolved tree: `axios@1.18.1`, `follow-redirects@1.16.0`, `form-data@4.0.6`, `react-router-dom@6.30.4`, `react-router@6.30.4`.
- Human result: exit 0, `found 0 vulnerabilities`.
- JSON metadata: every severity 0, `total=0`; `prod=40`.
- Production residual advisories: none.

## Development Dependency Audit

### Before

The unfiltered audit after production remediation but before development dependency updates was:

```powershell
npm audit
npm audit --json
```

- Result: exit 1; 8 package-level vulnerabilities (`low=1`, `moderate=3`, `high=4`, `total=8`).
- Dependency counts: `prod=40`, `dev=331`, `optional=52`, `peer=7`, `total=370`.

| Dependency path | Installed before | Affected range | Tool/runtime relevance |
|---|---:|---|---|
| root -> `@vitejs/plugin-react` -> `@babel/core` | 7.29.0 | `<=7.29.0` | Transform source-map file-read path |
| root -> ESLint/minimatch paths -> `brace-expansion` | 1.1.12 / 5.0.4 | `<=1.1.12` / `4.0.0 - 5.0.5` | Crafted lint glob/range resource exhaustion |
| root -> `eslint` -> `flat-cache` -> `flatted` | 3.3.4 | `<=3.4.1` | Lint cache parse recursion/prototype paths |
| root -> `eslint` -> `js-yaml` | 4.1.1 | `4.0.0 - 4.1.1` | Crafted YAML alias CPU exhaustion |
| root -> Vite/Vitest paths -> `picomatch` | 4.0.3 | `4.0.0 - 4.0.3` | Build/test glob injection and ReDoS |
| root -> `vite` -> `postcss` | 8.5.8 | `<8.5.10` | Build-time CSS stringify output |
| root -> `jsdom` -> `undici` | 7.25.0 | `7.0.0 - 7.27.2` | Test HTTP/TLS/WebSocket client paths |
| root -> direct `vite` | 6.4.1 | `<=6.4.2` | Exposed dev-server file-read, path, and Windows editor-launch paths |

### Read-Only Compatibility Analysis

```powershell
npm view vite@6.4.3 version
npm audit fix --dry-run --json
```

- Registry result: Vite `6.4.3` exists.
- Dry-run result: exit 1 because the pre-update audit remained nonzero; proposed 29 changes, 0 additions, and 0 removals.
- Proposed security targets: Vite 6.4.3, `@babel/core` 7.29.7, `brace-expansion` 1.1.16/5.0.7, `flatted` 3.4.2, `js-yaml` 4.3.0, `picomatch` 4.0.5, `postcss` 8.5.19, and `undici` 7.28.0.
- Every target was available inside the existing direct dependency ranges and transitive semver contracts. No force, override, or framework/tool major was required.

The dry-run unexpectedly refreshed npm's hidden `node_modules/.package-lock.json` while leaving the repository manifest/lockfile and installed Vite files unchanged. This produced a local metadata/file mismatch only; the final `npm ci` below rebuilt `node_modules` from the committed lockfile candidate and verified the executable version.

### Compatible Update

```powershell
npm install --save-dev vite@^6.4.3
npm audit fix
npm ci
```

- The direct declaration moved from `vite@^6.0.5` to `vite@^6.4.3`, keeping Vite major 6.
- `npm install` updated the compatible lock tree; the intermediate audit dropped to 7 findings (`low=1`, `moderate=3`, `high=3`).
- Non-force `npm audit fix` updated the remaining compatible transitive packages and reported 0 vulnerabilities.
- Clean `npm ci` installed 321 packages, audited 322, and reported 0 vulnerabilities.
- `node -p "require('vite/package.json').version"` and `npx vite --version` both reported 6.4.3 after the clean install.

### After

```powershell
npm ls @babel/core brace-expansion flatted js-yaml picomatch postcss undici vite --all
npm audit --json
npm audit --omit=dev
```

| Dependency path | Resolved after |
|---|---:|
| direct `vite` | 6.4.3 |
| `@vitejs/plugin-react -> @babel/core` | 7.29.7 |
| ESLint/minimatch paths -> `brace-expansion` | 1.1.16 / 5.0.7 |
| `eslint -> flat-cache -> flatted` | 3.4.2 |
| `eslint -> js-yaml` | 4.3.0 |
| Vite/Vitest paths -> `picomatch` | 4.0.5 |
| `vite -> postcss` | 8.5.19 |
| `jsdom -> undici` | 7.28.0 |

- Unfiltered JSON result: every severity 0, `total=0`; dependency counts remained `prod=40`, `dev=331`, `optional=52`, `peer=7`, `total=370`.
- Production-only result: exit 0, `found 0 vulnerabilities`.
- Residual advisory paths: none. No advisory requires a deferred major upgrade or compatibility exception.
- `npm ci` still emits deprecation notices for the existing ESLint 8-era toolchain and legacy transitive utilities. Those notices are support-lifecycle signals, not audit advisories.

## Dependency Focused Regression

```powershell
npx vitest run src/api/client.test.ts src/api/loadError.test.ts src/router/ProtectedRoute.test.tsx src/router/SubscriberRoute.test.tsx src/router/index.test.tsx src/pages/auth/LoginPage.test.tsx src/layouts/Header.test.tsx src/pages/public/TrackListPage.test.tsx src/pages/admin/PaymentReadOnlyPage.test.tsx
```

- Result after clean Vite 6.4.3 installation: exit 0; 9 test files / 63 tests passed.
- Contracts covered: refresh failure login redirect, load-error cancellation classification, protected/subscriber redirects, safe internal `returnTo`, unsafe/protocol-relative target rejection, route wiring, AbortSignal cancellation, and latest-request-wins fences.

## Coverage Baselines

Coverage values are observations, not acceptance thresholds.

### Backend JaCoCo

```powershell
gradlew.bat test jacocoTestReport
```

- Final result: exit 0; `BUILD SUCCESSFUL in 1m 31s`.
- Test result headers: 146 suites, 1,046 tests, 0 failures, 0 errors, 9 skipped.
- XML: `build/reports/jacoco/test/jacocoTestReport.xml` (1,398,094 bytes at measurement).
- HTML: `build/reports/jacoco/test/html/index.html`.

| Counter | Covered | Missed | Total | Baseline |
|---|---:|---:|---:|---:|
| Instruction | 34,615 | 10,842 | 45,457 | 76.15% |
| Branch | 2,226 | 1,544 | 3,770 | 59.05% |
| Line | 7,653 | 2,311 | 9,964 | 76.81% |
| Complexity | 2,060 | 1,738 | 3,798 | 54.24% |
| Method | 1,451 | 444 | 1,895 | 76.57% |
| Class | 341 | 48 | 389 | 87.66% |

Diagnostic history:

1. Initial exact command: exit 1 after 1m 20s, `Could not complete execution for Gradle Test Executor 21`.
2. Diagnostic command `gradlew.bat test jacocoTestReport --no-daemon --max-workers=1 --stacktrace`: exit 1 after 1m 56s; root cause `java.lang.OutOfMemoryError: Java heap space` at `CanonicalImageServiceTest.java:121` while allocating the approved oversized-image test input.
3. Added `maxHeapSize = '1g'` to the Gradle `test` task only.
4. Re-ran the required default command: exit 0 and both reports generated.

### Frontend Vitest V8

```powershell
npm run test:coverage
```

- Result: exit 0; 38 test files / 180 tests passed.
- JSON: `frontend/coverage/coverage-summary.json`.
- HTML: `frontend/coverage/index.html`.

| Counter | Covered | Total | Baseline |
|---|---:|---:|---:|
| Statements | 2,347 | 6,803 | 34.49% |
| Branches | 1,532 | 4,505 | 34.00% |
| Functions | 515 | 1,851 | 27.82% |
| Lines | 2,168 | 6,119 | 35.43% |

## Prettier Baseline

Initial command:

```powershell
npx prettier --list-different . --ignore-unknown
```

- Result: exit 1; 119 files required formatting.
- The complete 119-file inventory is listed below.
- `package.json` and `package-lock.json` combine approved dependency/script/lock changes with formatting; the other 117 paths are formatting-only.

```text
.eslintrc.cjs
.prettierrc
index.html
package-lock.json
package.json
src/api/albums.ts
src/api/auth.ts
src/api/client.ts
src/api/downloadQueue.ts
src/api/downloads.ts
src/api/licenses.ts
src/api/likes.ts
src/api/notices.ts
src/api/playHistory.ts
src/api/playlists.ts
src/api/questions.ts
src/api/settings.ts
src/api/subscriptions.ts
src/api/whitelistChannels.ts
src/App.tsx
src/components/album/AlbumCard.module.css
src/components/album/AlbumCard.tsx
src/components/filter/TagFilterModal.module.css
src/components/filter/TagFilterModal.tsx
src/components/player/HistoryModal.tsx
src/components/player/PlaylistDrawer.module.css
src/components/player/PlaylistDrawer.tsx
src/components/track/TrackRow.module.css
src/components/track/TrackRow.tsx
src/components/ui/Badge.tsx
src/components/ui/Button.module.css
src/components/ui/Button.tsx
src/components/ui/DataTable.tsx
src/components/ui/FilterChip.module.css
src/components/ui/FilterChip.tsx
src/components/ui/Tag.module.css
src/components/ui/Tag.tsx
src/hooks/usePublicCapabilities.ts
src/layouts/AdminLayout.module.css
src/layouts/AdminLayout.tsx
src/layouts/MainLayout.tsx
src/main.tsx
src/pages/admin/LicenseManagePage.tsx
src/pages/admin/NoticeCreatePage.module.css
src/pages/admin/NoticeCreatePage.tsx
src/pages/admin/NoticeEditPage.module.css
src/pages/admin/NoticeEditPage.tsx
src/pages/admin/QuestionManagePage.tsx
src/pages/admin/SiteSettingsPage.module.css
src/pages/admin/SubscriptionManagePage.tsx
src/pages/admin/TagManagePage.module.css
src/pages/admin/TagManagePage.tsx
src/pages/admin/TrackManagePage.module.css
src/pages/admin/TrackManagePage.tsx
src/pages/admin/UserManagePage.module.css
src/pages/admin/UserManagePage.tsx
src/pages/admin/UserSubscriptionManagePage.tsx
src/pages/auth/EmailVerifyPage.tsx
src/pages/auth/LoginPage.module.css
src/pages/auth/PasswordResetPage.tsx
src/pages/auth/SignupPage.module.css
src/pages/auth/SignupPage.test.tsx
src/pages/auth/SignupPage.tsx
src/pages/auth/SocialCompleteProfilePage.test.tsx
src/pages/auth/SocialCompleteProfilePage.tsx
src/pages/auth/SocialLoginPage.test.tsx
src/pages/auth/SocialLoginPage.tsx
src/pages/creator/AlbumCreatePage.module.css
src/pages/creator/AlbumCreatePage.tsx
src/pages/creator/AlbumEditPage.module.css
src/pages/creator/AlbumEditPage.tsx
src/pages/creator/AlbumManagePage.module.css
src/pages/creator/AlbumManagePage.tsx
src/pages/creator/TrackEditPage.module.css
src/pages/creator/TrackEditPage.tsx
src/pages/creator/TrackUploadPage.module.css
src/pages/creator/TrackUploadPage.tsx
src/pages/error/ErrorPage.module.css
src/pages/error/NotFoundPage.tsx
src/pages/error/ServerErrorPage.tsx
src/pages/public/AlbumDetailPage.module.css
src/pages/public/AlbumDetailPage.tsx
src/pages/public/AlbumListImagePage.module.css
src/pages/public/AlbumListImagePage.tsx
src/pages/public/AlbumListPage.module.css
src/pages/public/AlbumListPage.tsx
src/pages/public/HomePage.module.css
src/pages/public/HomePage.tsx
src/pages/public/NoticeDetailPage.tsx
src/pages/public/SubscriptionPlanPage.module.css
src/pages/public/SubscriptionPlanPage.test.tsx
src/pages/public/SubscriptionPlanPage.tsx
src/pages/public/TrackDetailPage.module.css
src/pages/subscriber/DownloadQueuePage.test.tsx
src/pages/subscriber/DownloadQueuePage.tsx
src/pages/subscriber/LicenseDetailPage.tsx
src/pages/subscriber/LicenseListPage.tsx
src/pages/subscriber/LikeListPage.module.css
src/pages/subscriber/LikeListPage.tsx
src/pages/subscriber/PlayHistoryPage.module.css
src/pages/subscriber/PlayHistoryPage.tsx
src/pages/subscriber/PlaylistDetailPage.module.css
src/pages/subscriber/PlaylistDetailPage.tsx
src/pages/subscriber/PlaylistEditPage.module.css
src/pages/subscriber/PlaylistEditPage.tsx
src/pages/subscriber/PlaylistListPage.module.css
src/pages/subscriber/ProfilePage.module.css
src/pages/subscriber/QuestionCreatePage.tsx
src/pages/subscriber/QuestionDetailPage.tsx
src/pages/subscriber/QuestionListPage.module.css
src/pages/subscriber/QuestionListPage.tsx
src/pages/subscriber/SubscriptionPaymentPage.module.css
src/store/likeStore.ts
src/store/themeStore.ts
src/styles/tokens.css
src/utils/format.ts
src/utils/tossPayments.ts
tsconfig.json
vite-env.d.ts
```

Mechanical write and final check:

```powershell
npx prettier --write . --ignore-unknown
npm run format
```

- Write result: exit 0; formatter-only changes on the listed paths.
- Final result: exit 0; `All matched files use Prettier code style!`
- `frontend/tsconfig.tsbuildinfo` was ignored by the formatter and preserved.

## Full Verification Results

| Command | Exit | Result |
|---|---:|---|
| `npm audit --omit=dev` | 0 | 0 production vulnerabilities |
| unfiltered `npm audit --json` | 0 | 0 vulnerabilities across production and development dependencies |
| focused 9-file Vitest command | 0 | 9 files / 63 tests passed after clean Vite 6.4.3 install |
| `gradlew.bat test jacocoTestReport` | 0 | 1,046 tests recorded; JaCoCo HTML/XML generated |
| `npm run typecheck` | 0 | TypeScript passed |
| `npm run lint` | 0 | ESLint passed with zero warnings/errors |
| `npm test` | 0 | Existing non-watch script; 38 files / 180 tests passed |
| `npm run test:coverage` | 0 | 38 files / 180 tests passed; V8 HTML/JSON generated |
| `npm run build` | 0 | Vite 6.4.3; 264 modules transformed |
| `npm run format` | 0 | Full frontend tree PASS |
| `python .agents/skills/validate-docs/scripts/validate_docs.py` | 0 | Documentation validation PASS |
| `git diff --check` | 0 | PASS; only non-failing line-ending warnings |

The handoff's `npm test -- --run` example was not used because the existing `test` script is already `vitest run`; `npm test` is the equivalent non-watch command and avoids a duplicate run argument.

## Generated Artifact and State Integrity

`git check-ignore -v --no-index` confirmed:

- Backend XML/HTML: ignored by root `.gitignore:3` (`build/`).
- Frontend JSON/HTML coverage: ignored by `frontend/.gitignore:4` (`coverage/`).
- Frontend production build: ignored by `frontend/.gitignore:2` (`dist/`).
- TypeScript build information: ignored by `frontend/.gitignore:5` (`*.tsbuildinfo`).

`frontend/tsconfig.tsbuildinfo` was dirty before WI-011. Baseline and final SHA-256 are both:

```text
B6A42AD2CD32A5AD04D06C55B8B1B26DD9B1894AFDAF7B508DFF16EF0C60F22A
```

`npm run build` temporarily changed the generated file as expected; the exact pre-WI bytes were restored from the isolated backup and re-hashed. Coverage/build reports do not appear in normal `git status`.

## Risk and Follow-up

- Coverage baselines identify risk; they do not establish a release threshold or release readiness.
- Frontend function coverage (27.82%) and branch coverage (34.00%), plus backend branch coverage (59.05%), should guide risk-based tests in later WIs rather than percentage-only tests.
- Production and unfiltered npm audits both report zero vulnerabilities. No advisory path is deferred.
- The clean install still warns that ESLint 8 and several legacy transitive utilities are deprecated. Updating those support-lifecycle dependencies may require a separately reviewed toolchain major even though they have no current audit finding.
- JaCoCo instrumentation requires a 1GB test heap because an existing negative-path test allocates an input above the image limit. This increases test-only memory demand.

## Rollback

Rollback only WI-011-owned changes; preserve all earlier WI/user edits.

1. Remove `id 'jacoco'`, the `jacoco`/`jacocoTestReport` blocks, and the test-only `maxHeapSize` from `build.gradle`.
2. Remove `test:coverage`, restore the prior source-only `format` script, restore prior Axios/React Router ranges and Vite `^6.0.5`, and remove `@vitest/coverage-v8` from `frontend/package.json`.
3. Regenerate `frontend/package-lock.json` from the restored manifest without `--force`; do not restore unrelated files.
4. Remove the `coverage` block from `frontend/vite.config.ts` and the two WI-011 ignore lines from `frontend/.gitignore`.
5. From the complete 119-file inventory above, revert only the 117 non-package formatting-only paths plus the formatting portion of the two package files. Do not revert files from WI-005 through WI-010.
6. Remove only these WI-011 summary/evidence documents if the WI itself is rolled back.

Generated `build/`, `frontend/coverage/`, and `frontend/dist/` content is ignored and can be regenerated; no DB, secret, runtime, or data rollback is needed.

## Branch and Runtime Integrity

- Development branch verified: `codex/p1-acceptance-hardening`.
- Client-demo worktree/branch, Cloudflare runtime, public server processes, and client data were not inspected, switched, restarted, or modified.
- No file was staged, committed, or pushed.

## Related Documents

- [User Summary](../user/WI-20260716-ATS-011-summary.md): Plain-language outcome.
- [Handoff](WI-20260716-ATS-011-handoff.md): Approved execution contract.
- [Approved REQ](../user/REQ-20260716-ATS-002.md): Approved request.
- [Remaining Remediation Design](../../docs/design/remaining-remediation-design-20260716.md): P2-16 and P2-17 boundaries.
