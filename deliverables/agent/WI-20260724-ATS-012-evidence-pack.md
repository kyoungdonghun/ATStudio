# Evidence Pack: WI-20260724-ATS-012

## Summary

- Documentation validation passed and the exact documented PDF replay executed,
  but fresh-clone CRLF conversion changed the tracked manifest while leaving the
  PDF byte-identical. Final WI verdict: **FAIL**.

## Scope / DoD Check

- [x] Read the approved REQ, handoff, Tier 0, Tier 1, client, DB, and SR pointers.
- [x] Run Tier 0, internal-link, traceability, and index validation.
- [x] Run the exact documented PDF replay with explicit process-only runtime
  inputs.
- [x] Compare pre/post PDF and manifest hashes and inspect the clone diff.
- [x] Inspect install, fresh-DB, and acceptance prerequisites.
- [x] Treat historical WI-007 MySQL/manual-SQL material as historical evidence.
- [ ] Produce matching hashes for both committed PDF replay artifacts.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | System constitution |
| 0 | `docs/standards/documentation-standards.md` | Documentation rules |
| 0 | `docs/standards/development-standards.md` | Required project baseline |
| 0 | `docs/standards/glossary.md` | Canonical terms |
| 1 | `docs/policies/quality-gates.md` | Validation gate |
| 1 | `docs/policies/security-policy.md` | Secret and DB configuration boundary |
| Context | `deliverables/user/REQ-20260724-ATS-002.md` | Approved scope |
| Context | `deliverables/agent/WI-20260724-ATS-012-handoff.md` | WI contract |
| Context | `docs/client/index.md` | PDF replay contract |
| Context | `docs/SR/SR-93.md` | V1 DB and acceptance state |
| Context | `src/main/resources/schema.sql` | Fresh schema input |
| Context | `src/main/resources/seed.sql` | Fresh baseline-data input |

## Validation Evidence

Command:

```powershell
<explicit Python 3.10+ process input> .agents/skills/validate-docs/scripts/validate_docs.py
```

| Validator | Count | Result |
|---|---:|---|
| Required Tier 0 documents | 4/4 | PASS |
| Broken internal links | 0 | PASS |
| Supported unique traceability IDs | 455 | PASS |
| Orphaned documents | 0 | PASS |
| Final errors | 0 | PASS |
| Final warnings | 0 | PASS |

Exit code: `0`.

## PDF Replay Evidence

The executable values were supplied only to the replay process. The portable
command recorded in the documentation and manifest was executed unchanged:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/docs/replay-client-testing-pdf.ps1 -PythonExecutable $env:ATSTUDIO_PDF_PYTHON -RenderTool $env:ATSTUDIO_PDF_RENDER_TOOL
```

| Check | Result |
|---|---|
| Replay exit code | `0` |
| `VERIFY` / `REPLAY` | `PASS` / `PASS` |
| Rendered pages | 12 |
| Source segments | 295/295 (100.00%) |
| Replay temp directories after exit | 0 |
| Persisted supplied-runtime path hits | 0 |
| PDF bytes | 164,547 before and after |
| PDF SHA-256 before | `d7ad6184acba26b9bea2ef5a1c3d2735d7047b06c8390bb32931a46a9e2332f4` |
| PDF SHA-256 after | `d7ad6184acba26b9bea2ef5a1c3d2735d7047b06c8390bb32931a46a9e2332f4` |
| Manifest SHA-256 before | `a48e0b02e6c453a8f72ea16215dbd472592a9527c57e5d1d5524b5c229406f53` |
| Manifest SHA-256 after | `80ba87fb4ded8a548a4660458c946e274fef1509089246f91505bede003588a4` |
| Normalized replay-script SHA-256 | `c1343d9f48401277508437d262f80d7758cbfff8e224e03006426f9e4f456094` |
| Normalized dependency-lock SHA-256 | `caf9948ede2616491c4fbefb98f73537dee70f196e85bfce4b64fc50702c8d35` |

Poppler emitted non-fatal display-font lookup warnings. Rendering, source
coverage, metadata checks, and both replay scripts still passed.

## Finding 1: Manifest Is Not Fresh-Clone Deterministic

**Classification:** Blocking WI acceptance failure; documentation/provenance
defect.

Evidence:

- System Git config: `core.autocrlf=true`.
- `git ls-files --eol` reported `i/lf w/crlf` for all seven PDF Markdown
  sources.
- `.gitattributes` has no Markdown or PDF-source EOL rule.
- `scripts/docs/generate_client_testing_pdf.py:448-450` records raw source
  bytes and raw SHA-256.
- `scripts/docs/verify_client_testing_pdf.py:117-120` compares the manifest only
  with the current checkout.
- The PDF builder reads source as text at
  `scripts/docs/generate_client_testing_pdf.py:427-433`.

The committed source hashes equal the LF Git blobs. The replayed source hashes
equal the CRLF worktree files:

| Source | Committed LF SHA-256 | Replayed CRLF SHA-256 |
|---|---|---|
| `testing-guide.md` | `14372d35...18c6` | `39b8b083...8c6` |
| `1-quick-checklist.md` | `62a38e8f...ca6b` | `2d25db60...e75` |
| `2-full-feature-checklist.md` | `59182f99...4698` | `95f9191d...bb06` |
| `3-admin-checklist.md` | `c393e337...8460` | `125f4b06...b2bc` |
| `4-sr-format.md` | `fdf33be9...d34a` | `f72642a9...612e` |
| `5-ai-prompt.md` | `ac1ec26b...be6b` | `50f5c737...7c9a` |
| `0-site-policy.md` | `ca8abf8d...9f4a1` | `8f560ed4...e48` |

Required correction candidate: define and verify one EOL-independent source
hash contract, or enforce LF checkout for the seven sources. Verification must
compare both generated tracked artifacts with `HEAD`, not only validate the
newly generated manifest against the same checkout.

## Finding 2: Hidden PDF Font Prerequisite

**Classification:** Genuine portability/documentation gap.

- `docs/client/index.md:33-54` documents only Python 3.10+ and Poppler.
- `scripts/docs/generate_client_testing_pdf.py:518-519` defaults to two Windows
  Malgun Gothic font files.
- `scripts/docs/replay-client-testing-pdf.ps1` exposes Python and render-tool
  parameters but no font parameters.

The supplied Python and Poppler inputs are therefore insufficient on a fresh
machine that lacks those Windows fonts.

## Finding 3: Fresh-DB Operator Bootstrap Gap

**Classification:** Genuine install/operator documentation gap.

Current contract is consistent:

- `docs/design/db-schema.md:31-43` requires verified-empty MySQL 8,
  `schema.sql`, `seed.sql`, then Hibernate `ddl-auto=validate`.
- `docs/SR/SR-93.md:164-171` repeats the fresh-only contract and rejects deleted
  manual SQL.
- `src/main/resources/seed.sql:1-3` is the sole baseline-data input.

Missing active operator path:

- No DB bootstrap tool exists under `scripts/`.
- `src/main/resources/schema.sql:9-14` shows only a schema import into
  `atstudio`; it does not create/verify an empty database, apply `seed.sql`, or
  verify the 39-table manifest.
- MySQL managers under historical WI deliverables are proof artifacts, not
  active V1 operator tooling. The historical WI-007 manager was not used.

External prerequisites remain MySQL 8 server/client, credentials, database
creation privileges, and a safely selected empty database. These are not tied
together by a current executable operator runbook.

## Finding 4: Acceptance Operator Runbook Gap

**Classification:** Genuine acceptance documentation gap.

- `scripts/acceptance/start.ps1:26-35` requires an external backend environment
  bundle for non-dry-run use.
- `scripts/acceptance/AcceptanceLifecycle.psm1:9-16` requires six exact
  variables and QA bootstrap enabled.
- `scripts/acceptance/AcceptanceLifecycle.psm1:203-224` requires Cloudflared by
  explicit path or `PATH`.
- `docs/client/testing-guide.md:19-32` correctly tells clients to use only the
  current operator URL, but does not document operator startup.
- `docs/SR/SR-42.md:3-9` labels its ngrok path historical, while its remaining
  procedure still describes manual IntelliJ/npm/ngrok startup.
- Current docs do not link an active start/status/stop runbook or safe
  non-secret bundle schema.

## Clone Diff And No-Change Verdict

Initial facts:

- HEAD: `3147873c42bfd7883fdaa92922c0485e5fc72621`.
- The shared clone already contained `.qa-gradle-user-home/` plus ignored
  Gradle/build output from a predecessor WI.

Post-replay tracked diff:

```text
M output/pdf/atstudio-client-testing-guide.manifest.json
1 file changed, 14 insertions(+), 14 deletions(-)
```

The only tracked diff is the seven manifest source hash/byte pairs. The PDF has
no diff. Frontend cache, coverage, and dependency directories appeared during
parallel rehearsal work and were not produced, removed, or modified by this
doc/PDF command sequence.

Official workspace verdict:

- No source, documentation, schema, PDF, or runtime artifact changed.
- Created only:
  - `deliverables/user/WI-20260724-ATS-012-summary.md`
  - `deliverables/agent/WI-20260724-ATS-012-evidence-pack.md`
- No commit or push.

## Risks / Rollback

- The manifest replay can appear to pass while leaving a tracked diff on a
  normal Windows clone.
- A non-Windows or stripped Windows environment can fail before PDF generation
  because font inputs are implicit.
- A new operator can follow the high-level DB/acceptance contract but still lack
  the external tooling, bundle shape, and exact safe bootstrap sequence.
- Rollback for this WI is removal of the two permitted deliverables only.

## Follow-up

- Open a corrective WI for EOL-independent PDF source provenance and a
  committed-artifact diff assertion.
- Include explicit font inputs or a documented/packaged font dependency in the
  replay wrapper.
- Add one current operator runbook connecting fresh MySQL bootstrap,
  acceptance environment-bundle preparation, and start/status/stop commands
  without persisting secrets.
