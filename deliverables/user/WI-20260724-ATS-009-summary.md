# WI-20260724-ATS-009 P2 Correction Summary

> Date: 2026-07-24
> Role: `se` (Software Engineer)
> Approved requirement: `REQ-20260724-ATS-001`
> Status: **PASS**

## Corrections

1. PDF replay provenance now uses a repository-owned PowerShell wrapper with
   explicit Python and Poppler executable inputs. The wrapper creates a
   temporary virtual environment from pinned dependencies, regenerates the PDF
   and manifest, renders the PDF with Poppler, runs the verifier, and removes
   the temporary environment.
2. The six retired email, phone, and nickname availability rate-limit aliases
   were removed. Runtime configuration now accepts only the canonical
   `*_CLIENT_LIMIT` and `*_CLIENT_WINDOW_SECONDS` variables.
3. The local datasource example and its contract test now use the canonical
   `SPRING_DATASOURCE_PASSWORD` variable.

## Behavior Boundary

- Rate-limit values remain `30` requests per `60` seconds.
- Product behavior, payment policy, API contracts, database schema, and
  frontend code were not changed.
- No ignored local secret file or external acceptance bundle was read.
- No personal Python or Poppler path is stored in tracked artifacts.
- No commit or push was performed.

## Verification

- Exact documented PDF replay command: **PASS** in a fresh PowerShell process.
  It installed the four pinned dependencies, rendered 12 pages, matched
  295/295 source segments, and returned `VERIFY=PASS` and `REPLAY=PASS`.
- PDF: 12 pages, SHA-256
  `d7ad6184acba26b9bea2ef5a1c3d2735d7047b06c8390bb32931a46a9e2332f4`.
- Manifest SHA-256:
  `a48e0b02e6c453a8f72ea16215dbd472592a9527c57e5d1d5524b5c229406f53`.
- `V1BackendBaselineContractTest`: 6 tests, 6 passed, 0 failed, 0 skipped.
- Backend environment contract: 9 checks passed.
- Documentation validation: Tier 0, links, 455 traceability IDs, and document
  index passed.
- Active runtime scan: 0 references to the six retired aliases.
- Portability scan: 0 personal absolute paths and 0 `Path.stem` command
  derivations in the corrected PDF tooling contract.
- `git diff --check`: passed.

## Remaining Gates

The three bounded P2 findings are closed. WI-20260724-ATS-008 can proceed with
independent re-review and MA-owned Git-state verification. Client acceptance,
remote publication, commit, and push remain outside this WI.
