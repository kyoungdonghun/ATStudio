---
version: 1.0
last_updated: 2026-08-08
project: ATS
owner: docops
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20260808-ATS-006-handoff.md
    reason: Approved Work Item scope and output contract
  - path: ../user/REQ-20260808-ATS-002.md
    reason: Approved request and acceptance criteria
  - path: WI-20260808-ATS-003-evidence-pack.md
    reason: Administrator role-change security evidence
  - path: WI-20260808-ATS-004-evidence-pack.md
    reason: Subscription cross-layer consistency evidence
  - path: WI-20260808-ATS-005-evidence-pack.md
    reason: Thumbnail processing and external-practice evidence
---
# Evidence Pack: WI-20260808-ATS-006

## Summary (one-liner)

- Integrated the three approved investigations into OPEN historical SR-96 through SR-98 and synchronized the SR and overall document indexes to the verified file counts.

## Scope / DoD Check

- [x] `SR-96` separates current permission behavior, DB-current-role backend authorization, stale SPA role state, and proposed last-admin controls.
- [x] `SR-96` covers self-demotion, another-admin demotion, concurrent cross-demotion, session convergence, audit, recovery, and tests.
- [x] `SR-97` states that the current UI/API cannot replace a plan and can independently persist status, billing cycle, and expiration.
- [x] `SR-97` preserves future-dated `CANCELLED` as a valid grace period and rejects future-dated `EXPIRED` in the proposed matrix.
- [x] `SR-97` compares quick-edit plan selection with the recommended guarded general entitlement-correction flow and keeps payment/provider boundaries explicit.
- [x] `SR-98` records the acceptance-time `track 1` 564×1404px evidence without claiming pixel distortion.
- [x] `SR-98` distinguishes aspect-preserving resize, `cover` crop, `contain` whitespace, padding, and `fill` distortion.
- [x] `SR-98` recommends an explicit 1:1 contract and same-as-production preview before an interactive crop editor, with official references.
- [x] SR and overall documentation indexes match actual file counts and OPEN status totals.
- [x] Only WI-006-authorized documents were created or modified.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Constitution, transparency, simplicity, and ATStudio domain principles |
| 0 | `docs/standards/documentation-standards.md` | Documentation structure, metadata, and indexing standards |
| 0 | `docs/standards/glossary.md` | Canonical ATStudio and workflow terminology |
| 1 | `docs/policies/archive-policy.md` | Preserve SRs and deliverables as historical records |
| 2 | `docs/SR/index.md` | Current numbering, title, and status contract |
| 2 | `docs/SR/SR-14.md` | Historical ADMIN subscription product/status requirement |
| 2 | `docs/SR/SR-68.md` | Existing album image display and responsive-layout boundary |
| 2 | `docs/SR/SR-94.md` | Recent Korean SR structure and fact/proposal separation pattern |
| 2 | `docs/SR/SR-95.md` | Recent Korean SR recommendation and open-decision pattern |
| 2 | `docs/index.md` | Overall documentation category count contract |
| Context | `deliverables/user/REQ-20260808-ATS-002.md` | Approved three-SR scope and quality gates |
| Context | `deliverables/user/WI-20260808-ATS-003-summary.md` | Administrator-role security conclusions |
| Context | `deliverables/user/WI-20260808-ATS-004-summary.md` | Subscription consistency conclusions |
| Context | `deliverables/user/WI-20260808-ATS-005-summary.md` | Thumbnail recommendation summary |
| Context | `deliverables/agent/WI-20260808-ATS-003-evidence-pack.md` | Role-change code, session, audit, recovery, and test evidence |
| Context | `deliverables/agent/WI-20260808-ATS-004-evidence-pack.md` | Subscription UI/API/storage/payment and reusable-flow evidence |
| Context | `deliverables/agent/WI-20260808-ATS-005-evidence-pack.md` | Track upload/display/runtime and official external evidence |

**Injection Rules Applied**:

- Rule source: `.claude/config/context-injection-rules.json`
- Assignee: `docops`
- Task type: documentation, review
- Injected tiers: Tier 0 plus WI-specific Tier 1/2 and completed-WI context

## Evidence Pointers (required)

### SR-96

- `docs/SR/SR-96.md:17-27`
  - Current frontend/API/backend/concurrency/audit behavior and the confirmed self/last-admin demotion verdict.
- `docs/SR/SR-96.md:29-34`
  - Distinction between backend current-DB-role authorization and stale React role state.
- `docs/SR/SR-96.md:38-67`
  - Server invariant, shared serialization, UI/session convergence, audit, and recovery requirements.
- `docs/SR/SR-96.md:69-84`
  - Acceptance matrix and unresolved implementation/security decisions.

### SR-97

- `docs/SR/SR-97.md:17-28`
  - Current plan-list usage, absent plan-change contract, independent persistence, and SR-14 gap.
- `docs/SR/SR-97.md:30-44`
  - Proposed state/date matrix including valid future-dated `CANCELLED` grace and invalid future-dated `EXPIRED`.
- `docs/SR/SR-97.md:46-65`
  - Quick-editor alternative and recommended general guarded entitlement-correction flow.
- `docs/SR/SR-97.md:67-87`
  - Target-state validation, payment/provider separation, lock, confirmation, and audit requirements.

### SR-98

- `docs/SR/SR-98.md:11-24`
  - Acceptance-time `track 1` evidence and the square-`cover` clipping cause.
- `docs/SR/SR-98.md:26-38`
  - Resize/crop/contain/padding/distortion comparison.
- `docs/SR/SR-98.md:40-65`
  - Recommended 1:1 contract, production-equivalent preview, canonical resize boundary, and deferred crop-tool option.
- `docs/SR/SR-98.md:67-88`
  - Existing-asset decision, acceptance criteria, and official MDN/Shopify/Cloudinary links.

### Indexes

- `docs/SR/index.md:3`
  - Contract updated to 97 files and `82 DONE / 12 OPEN / 2 NOT CONFIRMED / 1 DROPPED`.
- `docs/SR/index.md:101-103`
  - SR-96 through SR-98 registered as OPEN.
- `docs/index.md:28,34`
  - SR category updated to 97 and non-index Markdown total updated to 199.

## Files Changed

- `docs/SR/SR-96.md`
- `docs/SR/SR-97.md`
- `docs/SR/SR-98.md`
- `docs/SR/index.md`
- `docs/index.md`
- `deliverables/user/WI-20260808-ATS-006-summary.md`
- `deliverables/agent/WI-20260808-ATS-006-evidence-pack.md`

No application code, database state, existing SR body, runtime, billing/provider state, or unrelated user artifact was modified.

## Commands & Outputs

| Command | Result |
| --- | --- |
| `Get-ChildItem docs/SR -File -Filter 'SR-*.md'` | 97 numbered SR files |
| `Select-String docs/SR/index.md -Pattern '^\\| SR-\\d+'` | 97 index rows |
| Status aggregation over SR index rows | `DONE=82`, `OPEN=12`, `NOT CONFIRMED=2`, `DROPPED=1` |
| `Get-ChildItem docs -Recurse -File -Filter '*.md'` excluding files named `index.md` | 199 documents, matching the documented count contract |
| Regex-based local Markdown link resolution for SR-96 through SR-98 | PASS; no missing local targets |
| Unicode replacement-character scan for SR-96 through SR-98 | PASS; none found |
| `git diff --check` | PASS; no whitespace errors. Existing tracked index files emitted CRLF-to-LF normalization warnings only |
| `git status --short` | Reviewed; pre-existing REQ/WI/SR-94/95 and user ZIP remained untouched outside this WI's authorized files |

## Tests

- Document file and index-row counts: PASS (`97 == 97`).
- SR status total: PASS (`82 + 12 + 2 + 1 == 97`).
- Overall non-index documentation count: PASS (`199`).
- New SR local-link resolution: PASS.
- Unicode replacement-character check: PASS.
- `git diff --check`: PASS with line-ending warnings only.
- Product build/test suites were not run because WI-006 changes documentation only; independent cross-layer and document validation is assigned to WI-007.

## Risks / Rollback

### Risks

- SR-96 intentionally specifies the invariant rather than choosing the lock implementation; architecture review remains required before code change.
- SR-97 recommends generalizing a refund-linked correction pattern, but the non-refund evidence reference and approval model are still policy decisions.
- SR-98 defines 1:1 as the proposed required ratio while leaving 2048×2048px recommended; implementation must not silently hard-code the latter as mandatory.
- Existing non-square thumbnails remain unaffected until a migration or exception-display policy is approved.

### Rollback

- Remove `docs/SR/SR-96.md`, `docs/SR/SR-97.md`, and `docs/SR/SR-98.md`.
- Restore only the WI-006 lines in `docs/SR/index.md` and `docs/index.md` to the prior 94-SR, 9-OPEN, 196-document values.
- Remove `deliverables/user/WI-20260808-ATS-006-summary.md` and this Evidence Pack.
- Do not alter earlier REQ/WI deliverables, SR-94/SR-95, product code, DB, or the unrelated ZIP artifact.

## Follow-ups

- `WI-20260808-ATS-007` should independently verify the SR claims against current code, official references, file/status counts, document links, and validation tooling.
