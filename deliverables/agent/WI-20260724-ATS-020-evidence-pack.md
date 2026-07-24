---
version: 1.0
last_updated: 2026-07-24
project: ATS
owner: qa
category: evidence-pack
status: confirmed
related_wi: WI-20260724-ATS-020
dependencies:
  - path: WI-20260724-ATS-020-handoff.md
    reason: Independent QA execution contract
  - path: ../user/REQ-20260724-ATS-002.md
    reason: Approved release rehearsal scope
  - path: ../../docs/standards/core-principles.md
    reason: Tier 0 constitution
  - path: ../../docs/standards/development-standards.md
    reason: Tier 0 development and verification rules
  - path: ../../docs/policies/security-policy.md
    reason: Secret and database safety rules
  - path: ../../docs/policies/quality-gates.md
    reason: Release gate requirements
---

# Evidence Pack: WI-20260724-ATS-020

## Independent Verdict

**PASS.** The corrective commit resolves both original fresh-clone failures,
the supported database helper fails closed, and the final fresh clone is
completely clean.

## Scope and Constraints

- Review source: remote
  `origin/codex/v1-release-rehearsal-fixes`.
- Expected and verified commit:
  `df35f9fe45146ffdeb64a3fac2730c5c24c6b644`.
- New clone:
  `C:\Users\jm991\AppData\Local\ATStudio\release-rehearsal-df35f9f-20260724`.
- Earlier clone
  `C:\Users\jm991\AppData\Local\ATStudio\release-rehearsal-3147873-20260724`
  was not reused or modified.
- Generated caches were kept in the fresh clone only when ignored, or under
  `C:\Users\jm991\AppData\Local\ATStudio\release-rehearsal-runtime-df35f9f-20260724`.
- No commit, push, runtime API/UI smoke, provider mutation, or production
  database operation was performed.

All readable input pointers in the handoff were reviewed before execution. The
initial `scripts/db/` typo was resolved against the WI-019 implementation at
`scripts/database/` and corrected in the handoff after independent review.

## Clone and Commit Evidence

Command:

```powershell
git clone --branch codex/v1-release-rehearsal-fixes --single-branch `
  https://github.com/kyoungdonghun/ATStudio.git `
  $env:LOCALAPPDATA\ATStudio\release-rehearsal-df35f9f-20260724
git rev-parse HEAD
git rev-parse origin/codex/v1-release-rehearsal-fixes
```

Result:

| Check | Value |
|---|---|
| Clone HEAD | `df35f9fe45146ffdeb64a3fac2730c5c24c6b644` |
| Remote-tracking HEAD | `df35f9fe45146ffdeb64a3fac2730c5c24c6b644` |
| Expected commit | `df35f9fe45146ffdeb64a3fac2730c5c24c6b644` |
| Initial status | Clean |
| Git `core.autocrlf` | `true` |

## Corrective Diff Review

Compared:

```powershell
git diff --name-only `
  3147873c42bfd7883fdaa92922c0485e5fc72621..`
  df35f9fe45146ffdeb64a3fac2730c5c24c6b644
```

| Classification | Result |
|---|---|
| Total corrective paths | 44 |
| Application backend paths under `src/main` or `src/test` | 0 |
| Frontend product paths under `frontend/src` | 0 |
| Frontend dependency manifests | 0 |
| Build configuration paths | 0 |
| Changed top-level areas | `.gitattributes`, `.gitignore`, `deliverables`, `docs`, `scripts` |

No product feature behavior changed in the corrective diff.

## Windows Checkout Attribute Proof

Commands:

```powershell
git check-attr text eol -- `
  frontend/package.json `
  frontend/src/App.tsx `
  frontend/src/styles/tokens.css `
  docs/client/index.md `
  scripts/docs/generate_client_testing_pdf.py `
  gradlew.bat
git ls-files --eol
```

Results:

- Frontend, Markdown, and Python samples: `i/lf w/lf`,
  `attr/text=auto eol=lf`.
- `gradlew.bat`: `i/lf w/crlf`, `attr/text eol=crlf`.
- Tracked EOL records inspected: 2,421.
- Unexpected worktree CRLF records: 0.

This independently closes the WI-011 Windows checkout defect.

## Toolchain

| Tool | Version |
|---|---|
| Java | Oracle JDK 17.0.12 LTS |
| Gradle | 9.3.0 |
| Node.js | 24.14.0 |
| npm | 11.9.0 |
| Vitest | 4.1.4 |
| Prettier | 3.8.1 |
| Python | exact bundled 3.12.13 |
| Poppler `pdftoppm` | 26.05.0 |

## Frontend Verification

Commands:

```powershell
npm ci --cache <repo-external-cache>
npm test
npm run test:coverage
npm run typecheck
npm run lint
npm run format
npm run build
```

| Gate | Result | Duration |
|---|---|---|
| `npm ci` | PASS, 321 packages installed / 322 audited | 7.738 s |
| Tests | PASS, 63 files / 468 tests | 24.151 s |
| Coverage | PASS, 63 files / 468 tests | 26.407 s |
| Typecheck | PASS | 6.138 s |
| ESLint | PASS, zero warnings | 4.816 s |
| Prettier | PASS, all matched files | 3.663 s |
| Production build | PASS, 266 modules / 133 files / 948,769 bytes | 8.600 s |

Coverage:

| Metric | Result | Threshold |
|---|---|---|
| Statements | 86.73% | 80% |
| Branches | 76.98% | 70% |
| Functions | 85.41% | 80% |
| Lines | 88.75% | 80% |

`dist/index.html` SHA-256:
`0e0e792df8a7dbfb8f8f2e74aab91ab9a0851367728130ede96e0bab77af7c01`.

The original WI-011 Prettier failure is fixed. Test, coverage, typecheck,
ESLint, and build durations did not materially regress from WI-011.

## Backend Regression Gate

Command:

```powershell
$env:GRADLE_USER_HOME = <repo-external-isolated-gradle-home>
.\gradlew.bat clean check --no-daemon
```

Result:

| Check | Result |
|---|---|
| Gradle gate | PASS |
| Duration | 149.758 s |
| Test suites | 158 |
| Tests | 1,208 |
| Passed | 1,199 |
| Failures / errors | 0 / 0 |
| Skipped | 9 |
| JaCoCo line | 85.7258% |
| JaCoCo method | 82.9308% |
| JaCoCo branch | 71.6819% |
| JaCoCo instruction | 85.6726% |
| Coverage verification | PASS |

The nine skips match the existing environment-conditional set: eight MySQL
tests and one Windows symlink test. No product code changed in the corrective
commit.

## PDF and Documentation Verification

### Focused Provenance Tests

Commands:

```powershell
<Python-3.12.13> -m py_compile `
  scripts/docs/pdf_provenance.py `
  scripts/docs/generate_client_testing_pdf.py `
  scripts/docs/verify_client_testing_pdf.py `
  scripts/docs/test_pdf_provenance.py
<Python-3.12.13> scripts/docs/test_pdf_provenance.py -v
```

Result: PASS, 5 tests, 0 failures, 0 errors.

The tests covered normalized LF/CRLF/CR equivalence, committed manifest source
records, raw LF writing, missing/unexpected font refusal, and approved font
hash acceptance.

### Exact Replay

Command:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass `
  -File scripts/docs/replay-client-testing-pdf.ps1 `
  -PythonExecutable <bundled-python-3.12.13> `
  -RenderTool <bundled-poppler-pdftoppm>
```

| Evidence | Before | After |
|---|---|---|
| PDF SHA-256 | `d7ad6184acba26b9bea2ef5a1c3d2735d7047b06c8390bb32931a46a9e2332f4` | same |
| Manifest SHA-256 | `f05cace32f363b4cd97ebfce0b86d1c33094bf31103d266b3b1cd5d97cb916fb` | same |

Additional evidence:

- Runtime: Python 3.12.13.
- Replay duration: 14.947 s versus WI-018 14.701 s; no material regression.
- `VERIFY=PASS`, `REPLAY=PASS`.
- PDF pages: 12.
- Source coverage: 295/295.
- Raw manifest bytes: 3,973.
- CRLF sequences: 0.
- Lone CR bytes: 0.
- Ends with LF: true.
- PDF and manifest tracked diff: 0.

Poppler emitted the same non-fatal display-font lookup warnings recorded by
WI-018. Rendering, page count, source coverage, hashes, and verification all
passed.

### Documentation Validation

Command:

```powershell
<Python-3.12.13> .agents/skills/validate-docs/scripts/validate_docs.py
```

Result:

| Check | Result |
|---|---|
| Tier 0 documents | PASS, 4/4 |
| Internal links | PASS, 0 broken |
| Traceability IDs | PASS, 469 matched |
| Document index | PASS, 0 orphaned |

## Database Helper Safety

### Restricted Bundle

The existing bundle was inspected without printing values.

| Check | Result |
|---|---|
| Repo-external regular file | PASS |
| SHA-256 | `489eaffd01b95af9404f68981b7f9f9c226b446aebe52386c20d6fa51cf19751` |
| Required datasource keys | PASS, 3/3 |
| ACL inheritance | Disabled |
| ACL rules | One current-account identity |
| Secret values emitted | 0 |

### Refusal Suite

Command:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass `
  -File scripts/database/test-bootstrap-guards.ps1
```

Result: PASS in 43.391 seconds.

Passed groups:

1. PowerShell parser.
2. Valid preflight without MySQL.
3. Protected-name refusal before connector access.
4. Malformed-name refusal before connector access.
5. Non-loopback refusal before connector access.
6. Target-name redaction.
7. Fixed current SQL inputs.
8. Retired migration absence.
9. Unrelated-database enumeration absence.

### Real Disposable MySQL Proof

One generated target matching
`^ats_disposable_\d{8}_[a-z0-9]{8}$` was used. Its exact value was never
printed.

Commands:

```powershell
.\scripts\database\bootstrap-disposable-mysql.ps1 `
  -Action Create -DatabaseName <guarded-disposable> `
  -BackendEnvironmentPath <restricted-bundle> `
  -ConnectorJarPath <isolated-connector-j>
.\scripts\database\bootstrap-disposable-mysql.ps1 `
  -Action Validate -DatabaseName <guarded-disposable> `
  -BackendEnvironmentPath <restricted-bundle> `
  -ConnectorJarPath <isolated-connector-j>
.\scripts\database\bootstrap-disposable-mysql.ps1 `
  -Action Drop -DatabaseName <guarded-disposable> `
  -BackendEnvironmentPath <restricted-bundle> `
  -ConnectorJarPath <isolated-connector-j>
```

| Check | Result |
|---|---|
| Preflight | PASS |
| `schema.sql` apply | PASS |
| `seed.sql` apply | PASS |
| Create | PASS |
| Independent validate | PASS |
| Drop and absence verification | PASS |
| Total proof duration | 8.729 s |
| Tables | 39 |
| Columns | 449 |
| Indexes | 153 |
| Foreign keys | 80 |
| Plans | 6 |
| Manifest SHA-256 | `c48d3c75378aaf2364d89ed06833ba68e27a5a334dbc4670d1443bd938c6c506` |

The helper connected only to loopback, operated only on the guarded disposable
target, and did not select, mutate, or enumerate the protected `atstudio`
database.

## Final Clone Integrity

Commands:

```powershell
git diff --check
git diff --exit-code
git diff --cached --exit-code
git status --short --untracked-files=all
```

| State | Count |
|---|---|
| Tracked diff | 0 |
| Staged diff | 0 |
| Untracked files | 0 |
| Whitespace errors | 0 |

The fresh clone remained completely clean after every check.

## Acceptance Criteria

| Criterion | Result |
|---|---|
| Fresh clone equals pushed corrective HEAD | PASS |
| Full frontend gates | PASS |
| Exact PDF replay and clean artifacts | PASS |
| Protected/malformed/remote DB refusals | PASS |
| Disposable create/schema/seed/validate/drop | PASS |
| No product feature changes | PASS |
| Backend regression and coverage gates | PASS |
| Docs validation and diff check | PASS |
| Final fresh-clone tracked/staged diff zero | PASS |
| No material build/replay regression | PASS |

## Residual Risks and Findings

1. React Router remains at `6.30.4` with the already-reviewed moderate
   advisories. Current routes were found non-reachable for the reviewed
   exploits, but production requires a controlled `7.18.1` migration.
2. The handoff path was corrected after independent review from `scripts/db/`
   to the active `scripts/database/` path.
3. This WI intentionally excludes runtime API/UI smoke and external payment
   mutations. Those remain owned by WI-014 and later WIs.

## Cleanup and Rollback

- The proof database was dropped and absence was verified by the guarded helper.
- The protected database and earlier rehearsal clone were untouched.
- Verification generated no repository change requiring rollback.
- Corrective rollback pointer:
  `3147873c42bfd7883fdaa92922c0485e5fc72621`.
- The new clone and repo-external caches remain as reproducibility evidence and
  may be removed later only by exact-path cleanup.
