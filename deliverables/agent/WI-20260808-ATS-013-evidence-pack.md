---
version: 1.0
last_updated: 2026-08-08
project: ATS
owner: docops
category: evidence-pack
status: confirmed
related_wi: WI-20260808-ATS-013
dependencies:
  - path: WI-20260808-ATS-013-handoff.md
    reason: Approved correction scope, constraints, and output contract
  - path: ../user/REQ-20260808-ATS-003.md
    reason: Approved parent request
  - path: ../user/WI-20260808-ATS-013-summary.md
    reason: User-facing correction and validation result
---

# Evidence Pack: WI-20260808-ATS-013

## Summary (one-liner)

- Corrected only the two WI-012 MINOR findings by standardizing SR-99's legacy fixed-rate notation to `128 Kibit/s` and making SR-101's acceptance quote exactly match the current PlayerBar constant.

## Scope / DoD Check

- [x] Replaced the mixed `128Ki-bps` and `128kbps` notation for the legacy fixed calculation with `128 Kibit/s` in SR-99.
- [x] Preserved the decimal `kbps` notation for the measured approximately 323.1/320.4-kbps average bitrates and the 320-kbps comparison fixture.
- [x] Replaced only SR-101's quoted current UI message with the exact PlayerBar constant.
- [x] Preserved all Track values, calculations, root causes, recommendations, acceptance meaning, SR status, and index counts.
- [x] Created the required user summary and this Evidence Pack.
- [x] Changed no product code, database row, public data, SR index, or documentation index.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Constitution and evidence transparency |
| 0 | `docs/standards/documentation-standards.md` | Minimal correction, link, index, and validation rules |
| 0 | `docs/standards/glossary.md` | Canonical Track and playback terminology |
| 2 | `docs/SR/SR-99.md` | Target duration SR and mixed fixed-rate notation |
| 2 | `docs/SR/SR-101.md` | Target playback SR and inaccurate current-message quote |
| 2 | `docs/SR/index.md` | Preserved SR numbering and status baseline |
| Registry | `docs/index.md` | Preserved documentation count baseline |
| Context | `deliverables/user/REQ-20260808-ATS-003.md` | Approved parent scope |
| Evidence | `deliverables/agent/WI-20260808-ATS-008-evidence-pack.md` | Exact fixed calculation and duration evidence |
| Evidence | `deliverables/agent/WI-20260808-ATS-010-evidence-pack.md` | PlayerBar event and message context |
| Validation | `deliverables/user/WI-20260808-ATS-012-summary.md` | Two MINOR findings and correction targets |
| Validation | `deliverables/agent/WI-20260808-ATS-012-evidence-pack.md` | Exact source pointers and severity assessment |

**Injection Rules Applied**:

- Rule source: `.claude/config/context-injection-rules.json`
- Assignee: `docops`
- Task type: documentation correction
- Injected tiers: documentation Tier 0, target SRs and indexes, approved REQ, source Evidence Packs, and independent validation findings

## Evidence Pointers

### Files Changed

- `docs/SR/SR-99.md` — unit-only correction for the legacy fixed-rate calculation.
- `docs/SR/SR-101.md` — exact current PlayerBar message quote correction.
- `deliverables/user/WI-20260808-ATS-013-summary.md` — user-facing two-MINOR correction summary.
- `deliverables/agent/WI-20260808-ATS-013-evidence-pack.md` — this evidence and rollback record.

### Exact Before / After

| Pointer | Before | After |
| --- | --- | --- |
| `docs/SR/SR-99.md:22,30,34` | `128Ki-bps` | `128 Kibit/s` |
| `docs/SR/SR-99.md:41,70` | `128kbps` | `128 Kibit/s` |
| `docs/SR/SR-101.md:14` | `재생이 지연되고 있습니다. 잠시 기다리거나 다시 시도해 주세요.` | `재생이 지연되고 있습니다. 연결을 확인한 뒤 다시 시도해 주세요.` |

### Authoritative Pointers

- `src/main/java/com/atstudio/atstudio/service/TrackService.java:300-302` — `128 * 1024 / 8` equals 16,384 bytes/s, or 128 Kibit/s.
- `frontend/src/layouts/PlayerBar.tsx:16` — exact `STALLED_MESSAGE` constant.
- `deliverables/agent/WI-20260808-ATS-012-evidence-pack.md` — independent MINOR findings and preservation constraints.

## Commands & Outputs

| Command / check | Result |
| --- | --- |
| `rg -n -C 2 "128\|320\|재생이 지연" docs/SR/SR-99.md docs/SR/SR-101.md frontend/src/layouts/PlayerBar.tsx` | Located all unit occurrences and confirmed the authoritative UI constant before correction |
| `python .agents/skills/validate-docs/scripts/validate_docs.py` | PASS, exit 0: Tier 0, internal links, 492 supported traceability ID matches, and document index |
| Targeted PowerShell source comparison | old `128Ki-bps`/`128kbps` 0; `128 Kibit/s` 5; preserved Track values true; SR quote equals PlayerBar constant true |
| Targeted PowerShell local-link resolution | 0 broken links |
| Targeted UTF-8 replacement-character and trailing-whitespace scan | 0 findings |
| `git diff --check` | PASS, exit 0; only existing CRLF-to-LF warnings for the two tracked indexes |

## Tests

- No product test suite was run because the WI changes only two historical-document wording/unit issues and its two required evidence files.
- Documentation validation and exact-source comparisons are the relevant tests for this bounded correction.

## Risks / Rollback

### Risks

- `Kibit/s` is intentionally used only for the legacy binary-base calculation. The measured real average bitrates remain decimal `kbps`; replacing those would change the recorded evidence and is outside scope.
- The PlayerBar message is current code text. A future UI-copy change should update implementation tests and active requirements deliberately rather than silently rewriting this historical correction.

### Rollback

- In `docs/SR/SR-99.md`, restore `128Ki-bps` at lines 22, 30, and 34 and `128kbps` at lines 41 and 70.
- In `docs/SR/SR-101.md`, restore `재생이 지연되고 있습니다. 잠시 기다리거나 다시 시도해 주세요.` at line 14.
- Remove `deliverables/user/WI-20260808-ATS-013-summary.md` and this Evidence Pack.
- No code, index, database, storage, or public runtime rollback is required.

## Follow-ups

- None for this wording/unit correction. Product implementation remains subject to separately approved WIs under REQ-20260808-ATS-003.
