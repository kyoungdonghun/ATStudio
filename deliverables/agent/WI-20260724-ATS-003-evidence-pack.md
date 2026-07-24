# Evidence Pack: WI-20260724-ATS-003

## Summary (one-liner)

- Replaced machine-specific PDF tool provenance with portable command
  identities and runtime-queried versions, then regenerated and visually
  verified the deterministic client testing PDF.

## Scope / DoD Check

- [x] Generator source contains no `C:/Users/jm991` or bundled-runtime path.
- [x] Manifest stores portable Python and Poppler command identities.
- [x] `--render-tool` accepts both a PATH command and an explicit executable
      path.
- [x] The exact recorded PATH-based generator command was replayed
      successfully.
- [x] Poppler version is queried from the supplied executable rather than
      hardcoded.
- [x] Omitted Poppler input records an explicit unknown version instead of a
      guess.
- [x] Verifier rejects user-home paths and validates schema version 2.
- [x] Current source hashes, generated PDF hash, and manifest agree.
- [x] Deterministic repeated generation produced the same PDF hash.
- [x] All 12 rendered pages passed visual inspection.
- [x] No client source document was edited.
- [x] Owned-file whitespace validation passed.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document                                            | Reason                                     |
| ---- | --------------------------------------------------- | ------------------------------------------ |
| 0    | `docs/standards/core-principles.md`                 | Constitution and approved execution bounds |
| 0    | `docs/standards/development-standards.md`           | `se` implementation and test standards     |
| 0    | `docs/standards/documentation-standards.md`         | Generated-document traceability contract   |
| REQ  | `deliverables/user/REQ-20260724-ATS-001.md`         | Approved scope and quality gates           |
| WI   | `deliverables/agent/WI-20260724-ATS-003-handoff.md` | Mandatory task and output contract         |

**Injection Rules Applied:**

- Assignee: `se`
- Task type: implementation and generated-document verification
- Owned slice: PDF generator, verifier, generated PDF, manifest, and WI outputs
- External secrets and WI-001/WI-002 owned files were not read or changed.

## Evidence Pointers

### Files changed

- `scripts/docs/generate_client_testing_pdf.py`
  - `GENERATOR_VERSION`: `1.4.1`.
  - `portable_command_name`: removes machine paths and executable suffixes.
  - `resolve_executable`: resolves an explicit file first, then a command
    through `shutil.which`.
  - `executable_version`: queries the supplied renderer with `-v`.
  - `write_manifest`: emits schema version 2 portable provenance.
  - `--render-tool`: accepts either a PATH command or explicit executable path
    and records the accepted input contract.
- `scripts/docs/verify_client_testing_pdf.py`
  - `assert_portable_command`: rejects path-bearing command identities.
  - Manifest checks reject `/Users/` and `/home/` paths.
  - Schema, canonical generator command, renderer version, and provenance source
    are validated.
- `output/pdf/atstudio-client-testing-guide.pdf`
  - Regenerated from the seven current tracked client source documents.
- `output/pdf/atstudio-client-testing-guide.manifest.json`
  - Schema version 2, current source hashes, portable tool provenance, and
    regenerated artifact hash.
- `deliverables/user/WI-20260724-ATS-003-summary.md`
- `deliverables/agent/WI-20260724-ATS-003-evidence-pack.md`

### Final SHA-256 hashes

| Path                                                     | SHA-256                                                            |
| -------------------------------------------------------- | ------------------------------------------------------------------ |
| `scripts/docs/generate_client_testing_pdf.py`            | `66d292d991bc24b68fb46e4593f93a3eda32b56a585217aae3bfa997330e120a` |
| `scripts/docs/verify_client_testing_pdf.py`              | `6b6cb05bc0b8e5e14f5c71d204b6f28ef1adfbf80441c4b520e24879b4c144c5` |
| `output/pdf/atstudio-client-testing-guide.pdf`           | `d7ad6184acba26b9bea2ef5a1c3d2735d7047b06c8390bb32931a46a9e2332f4` |
| `output/pdf/atstudio-client-testing-guide.manifest.json` | `644659f08baf747ca3fbf3d112f9b15a7fa0563d6bd6076668678954411daafc` |

## Commands & Outputs

Bundled tools used:

- Python:
  `C:\Users\jm991\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe`
- Poppler:
  `C:\Users\jm991\.cache\codex-runtimes\codex-primary-runtime\dependencies\native\poppler\Library\bin\pdftoppm.exe`

Executed commands:

- `python -m py_compile scripts/docs/generate_client_testing_pdf.py scripts/docs/verify_client_testing_pdf.py`
  - PASS.
- `python scripts/docs/generate_client_testing_pdf.py --render-tool <bundled-pdftoppm>`
  - PASS.
  - PDF: 12 pages, 164,547 bytes.
  - PDF SHA-256:
    `d7ad6184acba26b9bea2ef5a1c3d2735d7047b06c8390bb32931a46a9e2332f4`.
- PATH was temporarily prefixed with the bundled Python and Poppler
  directories, then the exact recorded command was executed:
  `python scripts/docs/generate_client_testing_pdf.py --render-tool pdftoppm`
  - PASS.
  - The parser resolved `pdftoppm` through `shutil.which`.
  - The regenerated manifest retained only `python` and `pdftoppm`, not their
    absolute locations.
- The generator was also run with the explicit bundled Poppler path and
  temporary output/manifest targets.
  - PASS.
  - The explicit path was used to query version `26.05.0` but did not appear in
    the temporary manifest.
- `python scripts/docs/verify_client_testing_pdf.py`
  - PASS.
  - Source coverage: 295/295 segments, 100%.
- `python scripts/docs/generate_client_testing_pdf.py --output <temporary-pdf> --manifest <temporary-manifest>`
  - PASS without `--render-tool`.
  - Records `render_tool_version: null` and `version_source: not captured`.
- `python scripts/docs/verify_client_testing_pdf.py --pdf <temporary-pdf> --manifest <temporary-manifest>`
  - PASS.
- `pdftoppm -png -r 96 output/pdf/atstudio-client-testing-guide.pdf <temporary-prefix>`
  - PASS: 12 PNG pages generated.
- Source/path scan over the generator, verifier, and final manifest.
  - PASS: zero user-specific or bundled-runtime paths.
- `git diff --check` over the four owned implementation/generated paths.
  - PASS.

## Visual Verification

- Inspected two six-page contact sheets plus pages 11 and 12 at original render
  resolution.
- Confirmed:
  - all Korean glyphs are readable;
  - headings, bullets, checkboxes, and code-like terms render correctly;
  - no clipped, overlapping, blank, or textless page exists;
  - footer and page numbering remain aligned;
  - page 11 reporting template and page 12 policy summary fit their bounds.
- Poppler emitted missing display-font warnings for unrelated fallback font
  names. The generated pages remained visually correct and the verifier
  reported 100% source-body coverage.
- Temporary render and alternate-generation files were removed after review.

## Source Synchronization Observation

- Before regeneration, the tracked manifest contained historical hashes for:
  - `docs/client/testing-guide.md`;
  - `docs/client/1-quick-checklist.md`;
  - `docs/client/2-full-feature-checklist.md`.
- Those source files were already current and unmodified before WI-003.
- Regeneration intentionally synchronized the PDF and manifest to the current
  tracked source state. WI-003 did not edit client guide content.
- Two independent generations produced the identical final PDF SHA-256 shown
  above.

## Risks / Rollback

- Risks:
  - Manifest consumers that hardcode schema version 1 must be updated. Repository
    search identified only the paired verifier as an active consumer.
  - Regeneration exposes current client-source content that was newer than the
    prior PDF; this is a synchronization correction, not a WI content edit.
  - A generation run without `--render-tool` intentionally cannot claim a
    Poppler version.
  - Replaying the portable command requires `python` and `pdftoppm` on PATH.
    The same parser accepts explicit executable paths where PATH provisioning
    is unavailable.
- Rollback:
  - Revert the generator, verifier, generated PDF, and manifest as one unit.
  - Reverting only one artifact breaks the source/output hash or schema
    contract.
  - Do not restore user-home or bundled-runtime paths.

## Follow-ups

- `WI-20260724-ATS-006`: documentation consistency and portability validation.
- `WI-20260724-ATS-004`: backend and acceptance lifecycle verification.
