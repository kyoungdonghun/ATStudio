# WI-20260724-ATS-012 Documentation and PDF Replay Summary

> Date: 2026-07-24
> Role: `docops`
> Requirement: `REQ-20260724-ATS-002`
> Fresh-clone commit: `3147873c42bfd7883fdaa92922c0485e5fc72621`
> Verdict: **FAIL**

## Result

Documentation validation passed, and the documented PDF replay completed with
12 rendered pages, 295/295 source segments, and replay/verification exit code
0. The PDF itself reproduced byte-for-byte, but the tracked manifest did not.
That hash mismatch fails the WI requirement that both replay artifacts match.

| Check | Result |
|---|---|
| Tier 0 documents | PASS, 4/4 present |
| Internal links | PASS, 0 broken |
| Traceability IDs | PASS, 455 supported unique IDs |
| Document index | PASS, 0 orphaned documents |
| Validator summary | PASS, 0 errors / 0 warnings |
| PDF pages and source coverage | PASS, 12 pages / 295 of 295 segments |
| PDF SHA-256 | MATCH, `d7ad6184acba26b9bea2ef5a1c3d2735d7047b06c8390bb32931a46a9e2332f4` |
| Manifest SHA-256 | MISMATCH, `a48e0b02...406f53` before and `80ba87fb...8a4` after |

## Genuine Gaps

1. **Fresh-clone line endings change the manifest.** The Windows Git
   installation checks the seven Markdown inputs out as CRLF. PDF generation
   reads them as text, so the PDF remains identical, while manifest provenance
   hashes the raw files. All seven source hash/byte records therefore change.
   The verifier accepts the newly generated local manifest and does not compare
   it with the committed artifact.
2. **The replay has an undocumented Windows-font prerequisite.**
   `docs/client/index.md` names Python 3.10+ and Poppler, but the wrapper also
   relies on `C:/Windows/Fonts/malgun.ttf` and `malgunbd.ttf`; it exposes no font
   override parameters.
3. **The active fresh-DB bootstrap is described but not packaged as operator
   tooling.** Current documentation correctly requires an empty MySQL 8
   database, then `schema.sql`, `seed.sql`, and Hibernate validation. The
   repository has no active operator bootstrap under `scripts/` that creates
   and verifies the empty database, applies both files, and verifies the
   manifest. The only inline SQL command applies `schema.sql` to `atstudio` and
   omits database creation, emptiness verification, `seed.sql`, and manifest
   verification.
4. **The active acceptance launcher lacks a current operator runbook.** The
   launcher requires Cloudflared plus a repository-external flat JSON bundle
   with six mandatory variables and an enabled QA bootstrap. Current client/SR
   documents do not link or document that invocation; `SR-42` retains an
   explicitly historical ngrok/manual-start procedure.

Historical WI-007 MySQL manager and manual-SQL references were treated only as
historical proof. They are not the active V1 bootstrap. The active V1 contract
remains the fresh-only `schema.sql` then `seed.sql` path.

## Clone And Change Verdict

- Clone HEAD matched the requested commit.
- Replay changed only the tracked PDF manifest: 14 insertions and 14 deletions
  across seven source records. The PDF had no tracked diff.
- Replay temporary directories were removed, and no supplied runtime executable
  path was persisted in the generated artifacts.
- Existing or concurrent QA caches/build outputs in the shared rehearsal clone
  were not modified or removed by this WI.
- No official source, documentation, PDF, schema, or runtime artifact was
  changed. Only this summary and the corresponding Evidence Pack were created.
- No commit or push was performed.
