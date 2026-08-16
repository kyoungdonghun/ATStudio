---
version: 1.0
last_updated: 2026-08-16
project: ATS
owner: se
category: evidence-pack
status: confirmed
wi_id: WI-20260809-ATS-059
req_id: REQ-20260809-ATS-001
dependencies:
  - path: WI-20260809-ATS-059-handoff.md
    reason: Approved test-only remediation boundary
  - path: WI-20260809-ATS-059-qa-fe-r4-review.md
    reason: Semantic control baseline under regression test
---

# WI-059 Coverage Regression Remediation Result

## Root Cause

- `frontend/src/components/basicComponents.test.tsx:44` selected an unnamed
  generic button after `AlbumCard` exposed both the card action and like action
  as native labelled buttons. Its following title-text click also no longer
  invoked the card action because the semantic card button is a sibling.
- `frontend/src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx:922-945`
  began the next mocked download immediately after a toast assertion. The
  `TrackListPage` handler emits that toast before its `finally` releases the
  pending-download disabled state, so coverage instrumentation could make the
  next click land on a still-disabled button and time out.

## Exact Correction

- Selected the like action with the accessible name `좋아요 해제` and the card
  action with `Focus Mix 앨범 보기`; both assertions retain their original
  action and non-propagation intent.
- Stored the existing `download-11` action button, retained every success and
  failure toast assertion, and waited for that action to be re-enabled after
  each attempted download before starting the next outcome.
- No application source/API/router/player/policy/dependency file was changed.

## Evidence Pointers

- `frontend/src/components/basicComponents.test.tsx:44-48`
- `frontend/src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx:922-945`

## Test Results

| Command                                                                                                                                | Result                                                                                                                                 |
| -------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| `npm exec vitest -- run src/components/basicComponents.test.tsx src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx`            | PASS: 2 files, 30 tests                                                                                                                |
| `npm exec vitest -- run --coverage src/components/basicComponents.test.tsx src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx` | Test PASS: 2 files, 30 tests. The partial run reports the existing global coverage-threshold failure because it is not the full suite. |

## External Effects and Rollback

- No browser, network/API, authentication, payment, mail, download, database,
  Git stage/commit/push, or other external effect was executed. Download calls
  in the test remained mocked.
- Rollback: revert only `frontend/src/components/basicComponents.test.tsx`,
  `frontend/src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx`, and
  this result document through source control.
