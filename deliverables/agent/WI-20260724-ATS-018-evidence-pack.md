---
version: 1.0
last_updated: 2026-07-24
project: ATS
owner: SE
category: evidence-pack
status: stable
related_wi: WI-20260724-ATS-018
dependencies:
  - path: WI-20260724-ATS-018-handoff.md
    reason: Approved scope and acceptance criteria
  - path: ../../docs/standards/evidence-pack-standard.md
    reason: Evidence Pack output standard
---

# Evidence Pack: WI-20260724-ATS-018

## Change Summary

- Defined deterministic LF checkout behavior without bulk renormalization.
- Centralized UTF-8/LF text hashing and normalized byte counts for every client
  PDF source record and verifier.
- Replaced platform-translating manifest text output with explicit UTF-8/LF
  bytes.
- Added exact Malgun Gothic font input preflight before PDF generation.
- Added focused regression tests for EOL equivalence, current manifest records,
  and font preflight failures.
- Added repository-wide Python cache ignore rules for generated test artifacts.
- Final verdict is **PASS**. An independent replay with the pinned bundled
  Python 3.12.13 runtime reproduced the committed PDF and manifest exactly.

## Scope

### In

- `.gitattributes`
- `scripts/docs/`
- Focused tests under `scripts/docs/`
- WI-018 user summary and Evidence Pack

### Out

- Product behavior
- Client Markdown content
- PDF content or layout
- Bulk tracked-file renormalization
- Dependency or runtime installation outside the existing replay script

## Pointers

| Path | Evidence |
|---|---|
| `.gitignore:70-71` | Repository-wide `__pycache__/` and `*.py[cod]` ignores |
| `.gitattributes:1-22` | LF default, CRLF batch exceptions, explicit binary assets |
| `scripts/docs/pdf_provenance.py:8-13` | Approved Malgun Gothic SHA-256 constants |
| `scripts/docs/pdf_provenance.py:16-31` | Shared UTF-8/LF bytes, hash, and size contract |
| `scripts/docs/pdf_provenance.py:34-37` | Explicit UTF-8/LF text writer |
| `scripts/docs/pdf_provenance.py:40-61` | Actionable missing/unexpected font validation |
| `scripts/docs/generate_client_testing_pdf.py:449-459` | Normalized source records |
| `scripts/docs/generate_client_testing_pdf.py:519-522` | Manifest output through the raw LF writer |
| `scripts/docs/generate_client_testing_pdf.py:544-553` | Font preflight before output generation |
| `scripts/docs/verify_client_testing_pdf.py:112-116` | Normalized source hash and byte verification |
| `scripts/docs/test_pdf_provenance.py:20-104` | Five focused contract tests |

Related inputs:

- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`
- `docs/policies/quality-gates.md`
- `docs/standards/evidence-pack-standard.md`
- `docs/client/index.md`
- `deliverables/user/REQ-20260724-ATS-002.md`
- `deliverables/agent/WI-20260724-ATS-011-evidence-pack.md`
- `deliverables/agent/WI-20260724-ATS-012-evidence-pack.md`

## Verification

### Python Compile

```powershell
python -m py_compile scripts/docs/pdf_provenance.py scripts/docs/generate_client_testing_pdf.py scripts/docs/verify_client_testing_pdf.py scripts/docs/test_pdf_provenance.py
```

Result: PASS, exit `0`.

### Focused Tests

```powershell
python scripts/docs/test_pdf_provenance.py -v
```

Post-fix result: PASS, 5 tests, 0 failures, 0 errors, 0.039 seconds.

Covered contracts:

1. Current manifest records match normalized hash and normalized byte count.
2. LF, CRLF, and lone CR inputs have identical hashes and sizes.
3. Manifest text output contains literal LF bytes and no CRLF or lone CR.
4. Missing and unexpected font inputs fail with actionable messages.
5. An expected font hash passes and returns the resolved path.

### Git Attribute Contract

```powershell
git check-attr text eol -- frontend/package.json docs/client/testing-guide.md gradlew.bat frontend/public/favicon.ico output/pdf/atstudio-client-testing-guide.pdf
```

Result:

- Frontend and Markdown text: `text=auto`, `eol=lf`.
- Windows batch: `text=set`, `eol=crlf`.
- Binary icon and PDF: `text=unset`.

No `git add --renormalize`, formatter write, or equivalent bulk rewrite was
executed.

### Python Cache Hygiene

```powershell
git check-ignore -v scripts/docs/__pycache__/pdf_provenance.cpython-312.pyc scripts/docs/__pycache__/test_pdf_provenance.cpython-314.pyc
git check-ignore -v scripts/docs/ignore-probe.pyc scripts/docs/ignore-probe.pyo scripts/docs/ignore-probe.pyd
```

Result: PASS. Current cache files match `__pycache__/`; `.pyc`, `.pyo`, and
`.pyd` probes match `*.py[cod]`.

### Frontend Prettier

```powershell
cd frontend
npm run format
```

Result: PASS, exit `0`; all matched files use Prettier formatting.

### Bounded PDF Replay

The existing replay script was invoked after the line-ending correction with
the pinned bundled Python 3.12.13 runtime and the explicit Poppler executable.
It completed in 14.701 seconds with exit code `0`.

Inner command:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/docs/replay-client-testing-pdf.ps1 -PythonExecutable C:\Users\jm991\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe -RenderTool C:\Users\jm991\.cache\codex-runtimes\codex-primary-runtime\dependencies\native\poppler\Library\bin\pdftoppm.exe
```

Functional result:

| Check | Result |
|---|---|
| Exit code | 0 |
| Replay | PASS |
| Verifier | PASS |
| Pages | 12 |
| Source segments | 295/295, 100.00% |
| Rendered pages | 12 |
| PDF and manifest diff | PASS, `git diff --exit-code` |

Byte reproducibility result:

| Artifact | Committed | Pinned Python 3.12.13 replay |
|---|---|---|
| PDF SHA-256 | `d7ad6184acba26b9bea2ef5a1c3d2735d7047b06c8390bb32931a46a9e2332f4` | `d7ad6184acba26b9bea2ef5a1c3d2735d7047b06c8390bb32931a46a9e2332f4` |
| Manifest SHA-256 | `f05cace32f363b4cd97ebfce0b86d1c33094bf31103d266b3b1cd5d97cb916fb` | `f05cace32f363b4cd97ebfce0b86d1c33094bf31103d266b3b1cd5d97cb916fb` |

The pinned runtime path reports Python `3.12.13`. The replay completed with
`REPLAY=PASS`, `VERIFY=PASS`, 12 pages, and 295/295 source segments. A final
`git diff --exit-code` for the PDF and manifest passed.

Raw manifest evidence after replay:

| Check | Result |
|---|---|
| Bytes | 3,973 |
| CRLF sequences | 0 |
| Any CR byte | false |
| Ends with LF | true |

Before this correction, `Path.write_text(... + "\n")` could translate the
manifest to CRLF on Windows and temporarily produce raw SHA-256
`a48e...`, while Git normalization hid the worktree difference. The writer now
emits explicit UTF-8/LF bytes, so the replayed raw file matches the committed
LF artifact without relying on Git normalization.

The replay emitted non-fatal Poppler display-font lookup warnings. Rendering,
page count, source coverage, and verification still passed.

A separate non-contract diagnostic with system Python `3.14.3` produced
different PDF bytes. Byte-identical evidence therefore requires the pinned
explicit Python 3.12.13 runtime above; a generic Python 3.10+ interpreter is not
an interchangeable byte-reproduction input.

### Direct Verifier Attempt

```powershell
python scripts/docs/verify_client_testing_pdf.py
```

Result: exit `1`, `ModuleNotFoundError: No module named 'pypdf'`.

This system-Python dependency absence is not hidden. The official replay
script's isolated temporary environment installed its pinned requirements and
the verifier passed there.

### Diff Integrity

```powershell
git diff --check
```

Result: PASS, exit `0`.

## Exact Changed Paths

- `.gitattributes`
- `.gitignore`
- `scripts/docs/pdf_provenance.py`
- `scripts/docs/generate_client_testing_pdf.py`
- `scripts/docs/verify_client_testing_pdf.py`
- `scripts/docs/test_pdf_provenance.py`
- `deliverables/user/WI-20260724-ATS-018-summary.md`
- `deliverables/agent/WI-20260724-ATS-018-evidence-pack.md`

## Acceptance Criteria

| Criterion | Verdict |
|---|---|
| Deterministic LF policy with batch and binary exceptions | PASS |
| Normalized hash for every source record and verifier | PASS |
| Raw manifest writer emits UTF-8/LF only | PASS |
| Explicit Malgun font preflight | PASS |
| CRLF/LF equivalence test | PASS |
| Focused tests | PASS |
| Replay and verifier | PASS |
| Generated PDF SHA unchanged | PASS |
| `git diff --check` | PASS |

## Risk And Rollback

Risks:

- Byte-identical replay evidence depends on the pinned explicit Python 3.12.13
  runtime. Python 3.14 is suitable for diagnostics but not interchangeable for
  artifact hash reproduction.
- Fresh-clone verification of the new Git attributes remains required in
  WI-20260724-ATS-020.

Rollback:

1. Revert `.gitattributes` and the four `scripts/docs/` implementation/test
   files together.
2. Do not renormalize existing tracked files as part of rollback.
3. Remove the two WI-018 deliverables if the entire WI is abandoned.
4. No PDF, client Markdown, dependency lock, product source, database, secret,
   commit, or remote state requires rollback.
