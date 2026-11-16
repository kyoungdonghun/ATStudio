---
version: 1.0
last_updated: 2026-11-17
project: ATS
owner: MA-DOCOPS
category: registry
status: confirmed
dependencies:
  - path: git-checkpoint-20261117.md
    reason: Previous phase-one source checkpoint
---

# Taxonomy display restoration — 2026-11-17

This bounded checkpoint preserves the current phase-one display of Genre,
Instrument and Mood. The historical tag-layout branch was a read-only reference;
it was not merged. Traceability: REQ-20261117-ATS-002, WI-20261117-ATS-006 through
WI-20261117-ATS-009; source-preservation review: REQ-20261117-ATS-003 / WI010.

## Source changes

- The public track list has separate Genre, Instrument and Mood columns.
- Compact layouts retain these categories as labeled groups below the title.
- Usage hashtags remain visible, and the detail page now includes Instrument.
- Empty, multiple and long taxonomy values have focused regression coverage.
- Existing search, playback controls, listening-mode restrictions and API contracts
  are preserved. There are no new requests, dependencies or backend changes.

The source selection is the six TSX/CSS implementation files for TrackRow,
TrackListPage and TrackDetailPage, plus their three test files. The nine-file
source freeze was verified unchanged during this Git preparation.

## Dated verification and deployment evidence

The following results belong to the completed November 17 implementation and
deployment work, not a new server operation during this commit preparation.

- Focused local suite: 39 tests passed. Listening-mode selected cases: 8 passed;
  27 cases were intentionally excluded by the name filter, not failed.
- Type check, scoped ESLint/Prettier and full/listening builds passed. Independent
  review repeated the relevant tests and checked the exact candidate.
- MA observed desktop and 390-pixel responsive-browser list/detail displays,
  Instrument filtering, empty taxonomy cells and an older open tab's delayed
  detail navigation. Responsive-browser evidence does not replace physical-phone
  testing, human audio checks or client acceptance.
- The frontend-only deployment matched all 99 served frontend file hashes.
  The corrected bounded public verifier passed 114 checks, independently reconciled.
- Its first attempt incorrectly expected HTTP200 for the existing root302
  redirect. The failed evidence was retained; only the checker was corrected.
  No repeat deployment was needed.
- The backend JAR, application/DB processes and TLS configuration were preserved.
  Nginx was gracefully reloaded to select the new frontend directory. No client
  track/tag data was modified and no playback was triggered by these checks.
- Temporary SSH access and the owned local key/agent state were restored; public
  web access and the running application/DB were retained.

## Git selection and limits

Only the selected source/tests, this sanitized checkpoint and a bounded index
entry belong to the local commit. The local documentation chronology is preserved
separately from the HEAD-derived staged index. Raw operational receipts, private
configuration, credentials, customer audio, generated builds and unrelated dirty
work are excluded. This task does not push or contact the server.

Prior reconstruction instructions remain in the
[phase-one checkpoint](git-checkpoint-20261117.md) and
[frontend cache runbook](../../deploy/single-vps/runbooks/frontend-cache.md).
If a later rollback is approved, revert this source change narrowly and follow
the existing deployment procedure; a Git revert alone does not change the server.
