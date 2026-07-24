# Evidence Pack: WI-20260724-ATS-006

## Summary (one-liner)

- Aligned current acceptance and client PDF maintenance documentation with the
  three residual fixes, regenerated and verified the portable PDF artifacts,
  and completed documentation and semantic residual checks.

## Scope / DoD Check

- [x] Current acceptance and PDF instructions match the implementation.
- [x] Non-PATH Python invocation and `--render-tool` input contracts are
      unambiguous.
- [x] Active current-state docs contain no obsolete alias used as active
      configuration.
- [x] Active current-state docs and generated manifest contain no personal
      machine path.
- [x] Historical audit records were not rewritten.
- [x] Product code was not modified.
- [x] Client PDF and manifest were regenerated after the final documentation
      edit.
- [x] PDF verifier, visual sample review, documentation validation, semantic
      scans, and `git diff --check` passed.
- [x] No commit was created.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier    | Document                                            | Reason                                      |
| ------- | --------------------------------------------------- | ------------------------------------------- |
| 0       | `docs/standards/core-principles.md`                 | Constitution and approved execution bounds  |
| 0       | `docs/standards/documentation-standards.md`         | Current-state documentation rules           |
| 0       | `docs/standards/glossary.md`                        | Project terminology                         |
| 1       | `docs/policies/archive-policy.md`                   | Preserve historical records as history      |
| REQ     | `deliverables/user/REQ-20260724-ATS-001.md`         | Approved residual-fix and final-audit scope |
| WI      | `deliverables/agent/WI-20260724-ATS-006-handoff.md` | Mandatory scope, DoD, and output contract   |
| Context | `docs/index.md`                                     | Documentation entry point                   |
| Context | `docs/SR/SR-93.md`                                  | Production-readiness current state          |
| Context | `docs/payment/acceptance-test-checklist.md`         | Payment acceptance contract                 |

**Injection Rules Applied:**

- Assignee: `docops`
- Task type: current-state documentation alignment and verification
- Forbidden scope observed: no historical audit rewrite, product policy change,
  product code change, secret access, or commit

## Evidence Pointers

### Files changed or confirmed by WI-006

- `docs/client/index.md:33`
  - Added PDF maintenance instructions.
- `docs/client/index.md:42`
  - Clarified that a non-PATH Python runtime must itself be invoked by explicit
    executable path.
- `docs/client/index.md:43`
  - Clarified that `--render-tool` accepts either a PATH command or an explicit
    executable path.
- `docs/payment/acceptance-test-checklist.md:30`
  - Current bundle rejection check for obsolete payment aliases.
- `docs/SR/SR-93.md:116`
  - Current-state statement that obsolete payment aliases are absent and
    rejected.
- `output/pdf/atstudio-client-testing-guide.pdf`
  - Regenerated deterministic client testing PDF.
- `output/pdf/atstudio-client-testing-guide.manifest.json`
  - Regenerated portable provenance manifest.
- `deliverables/user/WI-20260724-ATS-006-summary.md`
- `deliverables/agent/WI-20260724-ATS-006-evidence-pack.md`

### PDF source-scope observation

- `scripts/docs/generate_client_testing_pdf.py:43`
  - The current generator enumerates seven `docs/client` Markdown sources.
- `docs/payment/acceptance-test-checklist.md` is not in that source list.
- WI-006 did not alter the approved client PDF source composition. It
  regenerated the artifacts after final documentation edits and recorded this
  verified distinction.

## Commands & Outputs

### PDF generation

- Runtime:
  - `C:\Users\jm991\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe`
- Render tool:
  - `C:\Users\jm991\.cache\codex-runtimes\codex-primary-runtime\dependencies\native\poppler\Library\bin\pdftoppm.exe`
- Command:
  - `python scripts/docs/generate_client_testing_pdf.py --render-tool <explicit-pdftoppm-path>`
- Result:
  - PDF SHA-256:
    `d7ad6184acba26b9bea2ef5a1c3d2735d7047b06c8390bb32931a46a9e2332f4`
  - Manifest SHA-256:
    `644659f08baf747ca3fbf3d112f9b15a7fa0563d6bd6076668678954411daafc`

The manifest stores portable command identities rather than either explicit
machine path.

### PDF verification

- Command:
  - `python scripts/docs/verify_client_testing_pdf.py`
- Result:
  - `PAGES=12`
  - `SOURCE_SEGMENTS=295/295 (100.00%)`
  - `VERIFY=PASS`
- Visual sampling:
  - Rendered all 12 pages at 96 DPI.
  - Inspected page 1, page 6, and page 12.
  - No clipping, overlap, unreadable Korean text, or broken page boundary was
    observed.
  - Poppler emitted missing fallback display-font warnings while rendering;
    the embedded Korean content rendered correctly and the deterministic PDF
    hash remained unchanged.

### Documentation validation

- Command:
  - `python .agents/skills/validate-docs/scripts/validate_docs.py`
- Result:
  - Tier 0 documents: PASS
  - Internal links: PASS, 0 broken links
  - Traceability IDs: PASS, 454 matches
  - Document index: PASS, 0 orphaned documents

### Semantic scans

- Obsolete aliases:
  - `APP_PAYMENT_PROVIDER`
  - `TOSS_CONFIRM_URL`
  - `PAYMENT_BILLING_KEY_ENCRYPTION_SECRET`
- Result:
  - Occurrences are limited to rejection tests and explicit current-state
    statements that the aliases are obsolete.
  - Active configuration and fallback usage: 0.
- Personal path patterns:
  - `C:\Users\jm991`, `/Users/<name>`, `/home/<name>`
- Result:
  - Active docs and generated manifest: 0.
  - The only active source hit is the verifier's `/home/` rejection assertion.
- Retired runtime identifiers:
  - `acceptance-preview-64db91c`
  - `acceptance-v1-final-20260717`
  - historical TryCloudflare hostnames checked by the WI
- Result:
  - Active docs and scripts: 0.

### Diff integrity

- Command:
  - `git diff --check`
- Result:
  - PASS.
  - Git reported only expected LF-to-CRLF working-copy notices.

### Optional Markdown formatting check

- A non-gating Prettier check over the full changed documentation slice also
  reported pre-existing style differences in the three current-state documents
  and the generated manifest.
- Those existing files were not mechanically reformatted because that would
  expand WI-006 beyond the approved semantic documentation changes.
- The two new WI-006 deliverables were formatted with Prettier and pass their
  focused formatting check.

## Tests

- PDF generator: PASS
- PDF verifier: PASS
- PDF visual sample review: PASS
- Documentation validator: PASS
- Semantic residual scans: PASS
- `git diff --check`: PASS
- New WI-006 deliverable formatting: PASS

## Risks / Rollback

- Risks:
  - The payment acceptance checklist is not currently one of the seven client
    PDF source documents. Changing that composition would alter the client
    artifact and requires an explicit documentation-scope decision.
  - Regeneration relies on ReportLab, PyPDF, and Poppler being available. The
    maintenance instructions now describe both PATH-based and explicit-path
    invocation.
- Rollback:
  - Revert `docs/client/index.md`,
    `docs/payment/acceptance-test-checklist.md`, and `docs/SR/SR-93.md`
    together with the generated PDF and manifest.
  - Remove the WI-006 summary and Evidence Pack.
  - Do not revert the generated files separately from the generator/verifier
    contract delivered by WI-003.

## Follow-ups

- `WI-20260724-ATS-007`: V1 final audit after WI-004 through WI-006.
- `WI-20260724-ATS-008`: independent re-review after the final audit.
- Client acceptance, branch push, previous remote-branch deletion decision,
  and production readiness remain in the approved sequence after the final
  audit and re-review.
