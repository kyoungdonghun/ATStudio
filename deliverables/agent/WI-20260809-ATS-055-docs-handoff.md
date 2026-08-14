---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: ma
category: wi-documentation-handoff
status: active
dependencies:
  - path: WI-20260809-ATS-055-pg-r2-result.md
    reason: Final independent security PASS
  - path: WI-20260809-ATS-055-qa-integ-r2-result.md
    reason: Final independent cross-layer PASS
---

# Current Documentation Handoff: WI-20260809-ATS-055

## Assignment

- **Agent:** `docops`
- **Purpose:** synchronize only current binary/download documentation with the
  final verified WI-055 implementation.

## Required Updates

- Document one frontend binary result boundary: a non-empty Blob, sanitized
  server filename with deterministic stable-ID fallback, and normalized content
  type. State that malformed, Unicode-control, traversal/dot, non-Blob, and
  zero-byte responses do not trigger a browser download.
- State RFC 5987/basic disposition parsing and actual Axios header shape are
  covered; server response filename/content type take precedence over caller
  guesses.
- Document Track download error normalization through Blob-aware canonical API
  error codes and the same-identity synchronous pending fence across entry
  points.
- Document Download History's shared single/bulk registry: a currently claimed
  Track ID is not requested twice, distinct IDs continue, owner-safe cleanup
  permits retry, and skipped existing work is not counted as a new result.
- Document Question and Company Certification private files as authorized
  `StreamingResponseBody` responses with encoded disposition, private/no-store,
  pragma, nosniff, sandbox CSP, and disabled ranges. State the service Resource
  is streamed without a controller-sized intermediate byte array and input is
  closed after transfer.
- Keep authorization and audit wording exact: certification
  `DOCUMENT_ACCESS_GRANTED` records authorized resource access, not completed
  client byte delivery.
- Explicitly preserve held policies: durable grant versus completed-byte
  success, a bulk ceiling, and route-outliving cancellation/ownership remain
  undecided/outside WI-055. Do not claim client download completion as a durable
  server fact.

## Write Scope

- `docs/design/api-spec.md`
- `docs/design/usecase/user-license.md`
- `docs/design/usecase/user-question.md`
- `docs/design/usecase/company-certification.md`
- `docs/design/usecase/download-queue.md`

Touch fewer files if a document has no relevant current statement. Do not edit
indexes/counts unless these in-place updates actually change inventory (they
should not).

## Evidence

- Canonical WI-055 handoff and all implementation handoffs.
- Current implementation/tests.
- Initial PG/QA-INTEG FAIL records and final R2 PASS records.
- Latest verified focused/full test facts in the R2 records.

## Verification

- Run docs validation, Prettier check on changed docs if supported, stale phrase
  search, and `git diff --check`.

## Constraints

- Write only the five scoped current docs.
- Do not edit implementation, tests, WI/review records, protected outputs,
  ignored secrets, or private files. No external effects, policy invention,
  commit, or push.
