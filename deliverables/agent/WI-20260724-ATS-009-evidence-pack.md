# Evidence Pack: WI-20260724-ATS-009

## Summary

- Closed the three bounded WI-007 P2 findings without changing product
  behavior, rate-limit values, API contracts, payment policy, or DB schema.

## Scope / DoD Check

- [x] PDF provenance uses an explicit portable runtime/dependency contract.
- [x] The exact documented replay command passed in a fresh process.
- [x] Generated PDF and manifest were regenerated and verified.
- [x] Six retired availability aliases have zero active runtime references.
- [x] Baseline tests assert all six retired aliases are absent.
- [x] The local DB example and contract use `SPRING_DATASOURCE_PASSWORD`.
- [x] Focused Java, PDF, acceptance-environment, documentation, scan, and
  whitespace gates passed.
- [x] Ignored secret files and external acceptance bundles were not read.
- [x] No commit or push was performed.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and transparency rules |
| 0 | `docs/standards/development-standards.md` | `se` implementation and evidence rules |
| 0 | `docs/standards/documentation-standards.md` | Deliverable and portability documentation rules |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 1 | `docs/policies/security-policy.md` | Canonical datasource secret variable |
| 1 | `docs/policies/quality-gates.md` | Required verification gates |
| 2 | `docs/client/index.md` | Client PDF replay contract |
| Context | `deliverables/user/REQ-20260724-ATS-001.md` | Approved scope |
| Context | `deliverables/user/WI-20260724-ATS-007-summary.md` | Three P2 findings |
| Context | `deliverables/agent/WI-20260724-ATS-007-evidence-pack.md` | Audit evidence and correction targets |

## Evidence Pointers

### PDF replay provenance

- `scripts/docs/replay-client-testing-pdf.ps1:15-113`
  - Requires explicit absolute Python and Poppler paths.
  - Enforces Python 3.10+, creates a temporary venv, installs pinned
    dependencies, regenerates, renders, verifies, and cleans up.
- `scripts/docs/client-testing-pdf-requirements.txt:1-4`
  - Pins `charset-normalizer`, `pillow`, `pypdf`, and `reportlab`.
- `scripts/docs/generate_client_testing_pdf.py:39-52`
  - Defines generator version `1.5.0`, repository replay script, dependency
    lock, and exact portable replay command.
- `scripts/docs/generate_client_testing_pdf.py:452-489`
  - Writes schema v3 runtime versions, normalized script/dependency hashes,
    explicit input contracts, and Poppler version provenance.
- `scripts/docs/verify_client_testing_pdf.py:69-105`
  - Rejects personal path provenance and verifies the schema v3 replay,
    dependency, runtime, and rendering contract.
- `docs/client/index.md:33-54`
  - Documents prerequisites and the exact replay command.
- `output/pdf/atstudio-client-testing-guide.manifest.json`
  - Regenerated schema v3 provenance.
- `output/pdf/atstudio-client-testing-guide.pdf`
  - Regenerated deterministic 12-page artifact.

### Availability aliases

- `src/main/resources/application.yml:153-170`
  - Retains only canonical email, phone, and nickname client-limit variables
    with unchanged defaults of 30 requests and 60 seconds.
- `src/test/java/com/atstudio/atstudio/config/V1BackendBaselineContractTest.java:134-161`
  - Asserts all six retired aliases are absent and all six canonical variables
    are present.

### Local datasource variable

- `application-local.example.yml:14`
  - Uses `${SPRING_DATASOURCE_PASSWORD}`.
- `src/test/java/com/atstudio/atstudio/config/V1BackendBaselineContractTest.java:178-190`
  - Enforces the canonical reference-only datasource password contract.

## Changed Files

| File | WI-009 change |
|---|---|
| `application-local.example.yml` | Canonical datasource password variable |
| `docs/client/index.md` | Portable PDF bootstrap and replay recipe |
| `scripts/docs/client-testing-pdf-requirements.txt` | New pinned PDF dependency contract |
| `scripts/docs/replay-client-testing-pdf.ps1` | New isolated replay wrapper |
| `scripts/docs/generate_client_testing_pdf.py` | Schema v3 truthful provenance |
| `scripts/docs/verify_client_testing_pdf.py` | Schema v3 portability verification |
| `output/pdf/atstudio-client-testing-guide.manifest.json` | Regenerated provenance |
| `output/pdf/atstudio-client-testing-guide.pdf` | Regenerated; deterministic bytes preserved |
| `src/main/resources/application.yml` | Removed six retired aliases |
| `src/test/java/com/atstudio/atstudio/config/V1BackendBaselineContractTest.java` | Alias absence and canonical DB contract assertions |
| `deliverables/user/WI-20260724-ATS-009-summary.md` | User-facing result |
| `deliverables/agent/WI-20260724-ATS-009-evidence-pack.md` | This evidence pack |

## Commands and Results

### Exact documented replay

Prerequisite: the two process-local environment variables contain explicit host
executable paths; their values are intentionally not persisted.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/docs/replay-client-testing-pdf.ps1 -PythonExecutable $env:ATSTUDIO_PDF_PYTHON -RenderTool $env:ATSTUDIO_PDF_RENDER_TOOL
```

Result: exit `0`; pinned dependencies installed in a fresh temporary venv;
`PAGES=12`; `SOURCE_SEGMENTS=295/295 (100.00%)`; `VERIFY=PASS`;
`RENDERED_PAGES=12`; `REPLAY=PASS`. Poppler emitted non-fatal fallback-font
warnings and still rendered all pages successfully.

### Focused Java contract

```powershell
.\gradlew.bat test --tests "com.atstudio.atstudio.config.V1BackendBaselineContractTest"
```

Result: `BUILD SUCCESSFUL`; 6 tests, 0 failures, 0 errors, 0 skipped; 5 seconds.

### Backend environment contract

```powershell
& "scripts/acceptance/test-backend-environment.ps1"
```

Result: exit `0`; 9/9 checks passed.

### Documentation validation

```powershell
& $env:ATSTUDIO_PDF_PYTHON .agents/skills/validate-docs/scripts/validate_docs.py
```

Result: exit `0`; Tier 0 documents, internal links, 455 supported traceability
IDs, and document index passed.

### Semantic scans

```powershell
rg -n "APP_SECURITY_RATE_LIMIT_(EMAIL|PHONE|NICKNAME)_AVAILABILITY_(LIMIT|WINDOW_SECONDS)" src/main
rg -n "APP_SECURITY_RATE_LIMIT_(EMAIL|PHONE|NICKNAME)_AVAILABILITY_(LIMIT|WINDOW_SECONDS)" src/test
rg -n "Path\.stem|portable_command_name" scripts/docs
```

Results:

- Active runtime references: `0`.
- Contract-test references: `6`, all intentional absence assertions.
- Historical deliverable exact-name references before this pack: `0`.
- `Path.stem` or `portable_command_name` references: `0`.
- Corrected PDF tooling/docs/manifest personal absolute paths: `0`.
- Documented replay command equals manifest replay command: `True`.

### Whitespace

```powershell
git diff --check
```

Result: exit `0`; no whitespace errors. Git emitted only existing line-ending
conversion notices.

## Artifact Hashes

| Artifact | SHA-256 |
|---|---|
| `output/pdf/atstudio-client-testing-guide.pdf` | `d7ad6184acba26b9bea2ef5a1c3d2735d7047b06c8390bb32931a46a9e2332f4` |
| `output/pdf/atstudio-client-testing-guide.manifest.json` | `a48e0b02e6c453a8f72ea16215dbd472592a9527c57e5d1d5524b5c229406f53` |
| `scripts/docs/replay-client-testing-pdf.ps1` | `c1343d9f48401277508437d262f80d7758cbfff8e224e03006426f9e4f456094` |
| `scripts/docs/client-testing-pdf-requirements.txt` | `caf9948ede2616491c4fbefb98f73537dee70f196e85bfce4b64fc50702c8d35` |

The manifest stores normalized UTF-8/LF hashes for the replay script and
dependency lock so checkout line-ending policy does not invalidate provenance.

## Risks / Rollback

### Risks

- Fresh replay requires access to the pinned Python packages and a local
  Poppler installation.
- Poppler reports fallback-font warnings on this PDF but produces all 12 pages
  and exits successfully.
- Shared-worktree changes from WI-001 through WI-007 remain present and were
  preserved.

### Rollback

- Reverse only the WI-009 hunks in `application.yml`,
  `application-local.example.yml`, the baseline contract test, PDF scripts, and
  `docs/client/index.md`; preserve earlier WI changes in those files.
- Remove the new replay wrapper, dependency lock, WI-009 summary, and WI-009
  evidence pack.
- Restore the previous manifest/PDF pair together. Do not restore only one
  generated artifact because their hashes and provenance are coupled.

## WI-008 Readiness

**READY.** All three bounded P2 findings are corrected and their focused gates
pass. WI-20260724-ATS-008 should independently re-review these corrections,
artifact provenance, and MA-owned Git state before remote publication.
