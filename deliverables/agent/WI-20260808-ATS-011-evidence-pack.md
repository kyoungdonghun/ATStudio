---
version: 1.0
last_updated: 2026-08-08
project: ATS
owner: docops
category: evidence-pack
status: confirmed
related_wi: WI-20260808-ATS-011
dependencies:
  - path: WI-20260808-ATS-011-handoff.md
    reason: Approved Work Item scope, input pointers, and output contract
  - path: ../user/REQ-20260808-ATS-003.md
    reason: Approved three-SR request
  - path: ../user/WI-20260808-ATS-011-summary.md
    reason: User-facing outcome and unresolved decisions
---
# Evidence Pack: WI-20260808-ATS-011

## Summary (one-liner)

- Integrated the three completed investigations into OPEN SR-99 through SR-101, synchronized the SR and documentation indexes to 100 SR files, 15 OPEN items, and 202 managed documents, and passed targeted link, count, encoding, whitespace, and diff checks.

## Scope / DoD Check

- [x] Created SR-99 with all three public Track values, the exact 128-Ki-bps cause, approximately 320-kbps real average bitrates, create/replace parity, and read-only dry-run plus approved backfill requirements.
- [x] Created SR-100 with a Usage-first consolidated module, explicit current Usage zero-assignment state, verified Instrument data/backend support and public Track List wiring gap, and the completed SR-04 boundary.
- [x] Created SR-101 with immediate waiting/stalled semantics, the approximately 1.8-second recovery observation, an initial approximately two-second visibility threshold, Album waveform contract loss, cross-screen queue impact, common PlayableTrack/batch hydration, N+1 prohibition, and the SR-90 boundary.
- [x] Registered all three SRs as OPEN without rewriting SR-04, SR-90, or other historical records.
- [x] Synchronized exact counts: 100 numbered SR files, 100 index rows, 15 OPEN items, and 202 managed documents.
- [x] Created the required user-facing summary and this Evidence Pack.
- [x] Changed no product code, database row, existing SR body, or public runtime data.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Constitution, buyer discovery, integrity, simplicity, and traceability |
| 0 | `docs/standards/documentation-standards.md` | Document structure, local links, index synchronization, and historical-record rules |
| 0 | `docs/standards/glossary.md` | Canonical Track, Tag, Usage Guide Tag, License, and playback terms |
| 1 | `docs/policies/archive-policy.md` | Preserve SR and deliverable history without rewriting completed records |
| 2 | `docs/SR/index.md` | SR numbering and status registry |
| 2 | `docs/SR/SR-04.md` | Completed Mood discovery historical boundary |
| 2 | `docs/SR/SR-90.md` | Real analyzed waveform requirement and player-design boundary |
| Registry | `docs/index.md` | Per-category and total managed-document counts |
| Context | `deliverables/user/REQ-20260808-ATS-003.md` | Approved scope and acceptance criteria |
| Evidence | `deliverables/user/WI-20260808-ATS-008-summary.md` and `deliverables/agent/WI-20260808-ATS-008-evidence-pack.md` | Duration runtime, calculation, code path, test, and backfill evidence |
| Evidence | `deliverables/user/WI-20260808-ATS-009-summary.md` and `deliverables/agent/WI-20260808-ATS-009-evidence-pack.md` | Tag inventory, result-bearing assignments, query parity, and information-architecture evidence |
| Evidence | `deliverables/user/WI-20260808-ATS-010-summary.md` and `deliverables/agent/WI-20260808-ATS-010-evidence-pack.md` | Buffering event, waveform DTO, cross-screen impact, performance, and test evidence |

**Injection Rules Applied**:

- Rule source: `.claude/config/context-injection-rules.json`
- Assignee: `docops`
- Task type: documentation and historical SR integration
- Injected tiers: Tier 0, archive policy, named SR/index context, approved REQ, and all three prerequisite WI summaries and Evidence Packs

## Evidence Pointers

### Files Changed

- `docs/SR/SR-99.md` — duration discrepancy evidence, root cause, cross-screen propagation, create/replace contract, backfill, and acceptance criteria.
- `docs/SR/SR-100.md` — four-type Tag evidence, Usage-first module, empty-state/query parity, SR-04 boundary, and acceptance criteria.
- `docs/SR/SR-101.md` — delayed-feedback conditions, waveform transport failure, cross-screen queue impact, shared playback contract, SR-90 boundary, and acceptance criteria.
- `docs/SR/index.md` — added SR-99 through SR-101 and synchronized file/status totals.
- `docs/index.md` — changed SR count to 100, range to SR-92 through SR-101, and total to 202; version advanced to 2.7.
- `deliverables/user/WI-20260808-ATS-011-summary.md` — user-facing decisions, unresolved items, and validation results.
- `deliverables/agent/WI-20260808-ATS-011-evidence-pack.md` — this reproducibility and rollback record.

### Key Locations

- `docs/SR/SR-99.md:18-29` — three-Track runtime table and exact fixed-rate conclusion.
- `docs/SR/SR-99.md:32-41` — persisted-value propagation and browser correction path.
- `docs/SR/SR-99.md:45-66` — exact extraction, create/replace parity, dry-run, and approved backfill.
- `docs/SR/SR-100.md:17-30` — live counts, Usage zero assignment, and Instrument backend/page parity gap.
- `docs/SR/SR-100.md:32-55` — Usage-first integrated module, empty-state, and four-type query contract.
- `docs/SR/SR-100.md:65-69` — SR-04 remains DONE and is not reopened.
- `docs/SR/SR-101.md:20-34` — immediate waiting/stalled behavior, 1.8-second observation, and initial two-second threshold.
- `docs/SR/SR-101.md:36-62` — Album null waveform mapping and impact across collection/queue entry points.
- `docs/SR/SR-101.md:64-85` — common PlayableTrack/batch alternatives, N+1 prohibition, and SR-90 boundary.
- `docs/SR/index.md:3,104-106` — 100-file/15-OPEN contract and three OPEN entries.
- `docs/index.md:2,28,34` — version 2.7, SR count 100, and total 202.

## Commands & Outputs

| Command / check | Output |
| --- | --- |
| PowerShell count of `docs/SR/SR-\d+.md` | `SR_FILE_COUNT=100` |
| Regex count of numbered rows in `docs/SR/index.md` | `SR_INDEX_ROWS=100` |
| Status aggregation from SR index | `DONE=82`, `OPEN=15`, `NOT CONFIRMED=2`, `DROPPED=1` |
| Recursive per-category Markdown count excluding `index.md` | `architecture=1`, `design=29`, `policies=8`, `standards=13`, `templates=18`, `registry=4`, `audit=6`, `client=8`, `payment=7`, `SR=100`, `retrospective=4`, `adr=1`, `ui=3`, `eval=0` |
| Sum of the Document Overview count column | `DOC_INDEX_SUM=202` |
| Resolve every local Markdown link in SR-99 through SR-101 | `BROKEN_LOCAL_LINKS=0` |
| Search changed documentation for trailing whitespace and replacement characters | `TRAILING_WHITESPACE=0`, `REPLACEMENT_CHARS=0` |
| `git diff --check -- docs/SR/index.md docs/index.md` | PASS; only Git line-ending normalization warnings for the two existing tracked indexes |

## Tests

- Product test suites were not run because WI-011 changes only historical SR and registry documents.
- Targeted documentation checks passed for file count, index row coverage, status aggregation, category totals, local-link resolution, UTF-8 replacement-character absence, trailing whitespace, and tracked diff whitespace.
- Independent repository documentation and cross-layer validation remains assigned to blocking follow-up `WI-20260808-ATS-012`.

## Risks / Rollback

### Risks

- The public API values and the approximately 1.8-second playback observation are 2026-08-08 acceptance snapshots and can drift; the corresponding code-contract conclusions are retained separately in the prerequisite Evidence Packs.
- Approximately two seconds is an initial buffering-feedback recommendation, not an immutable product constant; slow-network validation may justify adjustment.
- A direct PlayableTrack projection increases collection response size, while batch hydration adds a contract and caching path. SR-101 intentionally leaves the final choice to a measured design WI.
- Usage-first discovery needs content readiness: the live Usage master Tag currently has no active Track assignment, so implementation without the defined empty state would create a dead-end.
- Existing duration correction and waveform hydration both require separate approved implementation and data-mutation work; these SR files do not authorize database changes.

### Rollback

- Remove only `docs/SR/SR-99.md`, `docs/SR/SR-100.md`, `docs/SR/SR-101.md`, `deliverables/user/WI-20260808-ATS-011-summary.md`, and this Evidence Pack.
- Revert only the three appended rows and count sentence in `docs/SR/index.md`.
- Restore `docs/index.md` from version 2.7 to 2.6, SR count 100 to 97, range ending SR-101 to SR-98, and total 202 to 199.
- No code, database, object storage, or public runtime rollback is required.

## Follow-ups

- `WI-20260808-ATS-012` should independently validate the new SR wording against WI-008 through WI-010 evidence, confirm links/counts/statuses, run the project documentation validator, and check changed-file scope.
- Later implementation requires separate approved WIs for exact audio analysis and backfill, four-type discovery parity, and buffering/playback DTO changes.
