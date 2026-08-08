---
version: 1.0
last_updated: 2026-08-08
project: ATS
owner: qa-integ
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20260808-ATS-002-handoff.md
    reason: Approved Work Item scope and output contract
  - path: WI-20260808-ATS-001-evidence-pack.md
    reason: Upstream authoring evidence under independent review
  - path: ../user/REQ-20260808-ATS-001.md
    reason: Approved request and acceptance criteria
  - path: ../user/WI-20260808-ATS-002-summary.md
    reason: User-facing verification summary
---

# Evidence Pack: WI-20260808-ATS-002

## Summary (one-liner)

- Independently validated SR-94/SR-95 and both indexes across React, Spring, schema, design, and official external references; corrected one concurrency-contract overstatement in SR-94 and found no other defects.

## Scope / DoD Check

- [x] Verified SR-94 observed and target flows against frontend save handling, backend duplicate detection, structured error serialization, and DB uniqueness.
- [x] Corrected SR-94 to distinguish service-level `TAG_NAME_DUPLICATED` from DB-race `DATA_INTEGRITY_VIOLATION` fallback behavior.
- [x] Verified SR-95 current input constraints and separated recommendations from unresolved policy decisions.
- [x] Re-opened all three official Splice/Epidemic Sound references and confirmed every representative naming claim.
- [x] Verified 94 SR files, 94 index rows, and status totals of 82 `DONE`, 9 `OPEN`, 2 `NOT CONFIRMED`, and 1 `DROPPED`.
- [x] Verified the SR category count of 94 and overall document count of 196.
- [x] Passed repository documentation validation, focused local-link checks, and `git diff --check`.
- [x] Made no changes to code, DB, policies, historical SRs, WI-001 orchestration artifacts, or the untracked ZIP.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Constitution and execution boundary |
| 0 | `docs/standards/development-standards.md` | Cross-layer implementation and evidence standards |
| 1 | `docs/standards/exception-handling.md` | Structured business-error response contract |
| 1 | `docs/policies/quality-gates.md` | Documentation verification expectations |
| 1 | `docs/policies/archive-policy.md` | Historical-document preservation boundary |
| 2 | `docs/design/api-spec.md` | Current Tag API inventory |
| 2 | `docs/design/usecase/sound-tag.md` | Create/update duplicate-name 409 use cases and usage tags |
| 2 | `docs/SR/index.md` | SR count and status source of truth |
| 2 | `docs/index.md` | Repository-wide document counts |
| Context | `deliverables/user/REQ-20260808-ATS-001.md` | Approved scope and acceptance criteria |
| Context | `deliverables/agent/WI-20260808-ATS-001-evidence-pack.md` | Upstream claims and reproduction pointers |

**Injection Rules Applied**:

- Rule source: `.claude/config/context-injection-rules.json`
- Assignee: `qa-integ`
- Task type: integration/documentation review
- Injected tiers: Tier 0, relevant Tier 1, and task-specific Tier 2/context pointers from the WI handoff packet

## Evidence Pointers (required)

### Corrected Defect: Concurrent Duplicate Contract

- `docs/SR/SR-94.md:3`
  - Now separates backend domain validation from DB uniqueness as the concurrency defense.
- `docs/SR/SR-94.md:13-18`
  - Records both the normal `TAG_NAME_DUPLICATED` path and the concurrent DB-collision fallback.
- `docs/SR/SR-94.md:22-28`
  - Distinguishes frontend precheck, service response, DB-race response, and page state.
- `docs/SR/SR-94.md:40-46`
  - Requires constraint-specific translation of `uq_tags_name` collisions to the stable tag domain error.
- `docs/SR/SR-94.md:55-63`
  - Adds acceptance coverage for concurrent duplicate creation.
- `src/main/java/com/atstudio/atstudio/service/TagService.java:29-39`
  - Create flow performs `existsByName` before persistence and throws `TAG_NAME_DUPLICATED` when already visible.
- `src/main/java/com/atstudio/atstudio/service/TagService.java:49-59`
  - Update flow excludes an unchanged name and rejects another visible duplicate.
- `src/main/resources/schema.sql:74-82`
  - Declares `uq_tags_name` as the final DB uniqueness constraint.
- `src/main/java/com/atstudio/atstudio/common/exception/GlobalExceptionHandler.java:97-100`
  - Maps generic Spring `DataIntegrityViolationException` to `DATA_INTEGRITY_VIOLATION`.
- `src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java:62-65,245-248`
  - Defines the distinct generic integrity and tag-name duplicate 409 contracts.

### Confirmed SR-94 Frontend Contract

- `frontend/src/pages/admin/TagManagePage.tsx:67-83`
  - Trims the name, submits create/update, and converts every rejection to `Failed to save tag`.
- `frontend/src/pages/admin/TagManagePage.tsx:108-114`
  - Replaces the full management UI whenever shared `error` is set.
- `frontend/src/pages/admin/TagManagePage.tsx:186-226`
  - The create/edit modal holds name/type inputs but has no field-level error state.
- `frontend/src/api/loadError.ts:24-27`
  - Existing frontend helper can extract JSON `errorCode` for domain-specific handling.
- `src/main/java/com/atstudio/atstudio/common/exception/GlobalExceptionHandler.java:35-39,137-145`
  - Serializes a thrown business error as status, reason, `errorCode`, and safe client message.

### Confirmed SR-95 Input Contract

- `docs/SR/SR-95.md:11-18`
  - Current frontend, backend, persistence, and global-unique behavior matches code and schema.
- `frontend/src/pages/admin/TagManagePage.tsx:68-75,195-201`
  - Uses submit-time `trim()` and `maxLength={TAG_NAME_MAX}` only.
- `frontend/src/utils/validation.ts:27`
  - Defines a 50-character maximum without a tag-name pattern.
- `src/main/java/com/atstudio/atstudio/dto/tag/TagCreateRequest.java:18-23`
  - Applies only `@NotBlank`, maximum length, and non-null type validation.
- `src/main/java/com/atstudio/atstudio/controller/TagController.java:24-28,60-65`
  - Applies the same validated request DTO to both create and update endpoints.
- `src/main/java/com/atstudio/atstudio/service/TagService.java:30-39,54-59`
  - Uses the supplied name for duplicate lookup and persistence/update without canonicalization.
- `src/main/java/com/atstudio/atstudio/entity/Tag.java:20-25`
  - Declares globally unique `name` independently of `type`.
- `docs/SR/SR-95.md:27-69`
  - Keeps whitespace/punctuation policy as recommendations and preserves case, Unicode, `#`, uniqueness scope, and migration as decisions.

### External Official References

- `https://support.splice.com/en/articles/8652594-finding-sounds`
  - Confirmed keyword and genre/instrument-tag search guidance and the `hip-hop` example; accessed 2026-08-08.
- `https://splice.com/sounds/genres`
  - Confirmed `Hip Hop / R&B`, `Lo-Fi Hip Hop`, `Afropop & Afrobeats`, `Synth-Pop`, and `K-Pop`; accessed 2026-08-08.
- `https://www.epidemicsound.com/music/genres/`
  - Confirmed `R&B`, `Brass & Marching Band`, `2-Step`, `Children’s Music`, `Electro-Funk`, `Electronic Dance Music (EDM)`, and `Euro-Trance`; accessed 2026-08-08.

### Index Synchronization

- `docs/SR/index.md:3,99-100`
  - Declares 94 numbered SRs and includes SR-94/SR-95 as `OPEN`.
- `docs/index.md:28,34`
  - Declares 94 SR documents and an overall total of 196.

## Files Changed

- `docs/SR/SR-94.md`
  - Minimal correction for concurrent DB-constraint behavior and target error mapping.
- `deliverables/user/WI-20260808-ATS-002-summary.md`
  - User-facing independent verification result.
- `deliverables/agent/WI-20260808-ATS-002-evidence-pack.md`
  - This reproducibility and traceability record.

No WI-002 correction was required in `docs/SR/SR-95.md`, `docs/SR/index.md`, or `docs/index.md`.

## Commands & Outputs

| Command | Result |
| --- | --- |
| `rg -n "Failed to save tag|TAG_NAME_DUPLICATED|TAG_NAME_MAX|existsByName" frontend/src src/main/java docs` plus focused full-file reads | PASS; SR facts mapped to exact React/Spring locations |
| Open official Splice help, Splice genres, and Epidemic Sound genres pages | PASS; all cited examples present on official pages on 2026-08-08 |
| `python .agents/skills/validate-docs/scripts/validate_docs.py` | PASS; Tier 0 exists, no broken links, 479 supported traceability IDs, all documents indexed |
| Count `docs/SR/SR-*.md` files and parse numbered index rows | PASS; 94 files and 94 rows |
| Group parsed SR statuses | PASS; 82 `DONE`, 9 `OPEN`, 2 `NOT CONFIRMED`, 1 `DROPPED` |
| Sum category counts and parse declared total in `docs/index.md` | PASS; 196 equals 196 |
| Resolve local Markdown links in SR-94/SR-95 | PASS; zero missing targets |
| `git diff --check` | PASS; exit code 0 and no whitespace errors |
| Scan SR-94, SR-95, and both WI-002 outputs for trailing spaces/tabs | PASS; zero findings, including untracked files not covered by `git diff --check` |

## Tests

- Documentation-only integration review; no application runtime or product build was required by the WI.
- Repository documentation validation and focused contract/count/link/diff checks all passed after the correction.

## Existing Baseline Warnings vs New Defects

### Existing Baseline Warnings

- `validate_docs.py`: none.
- `git diff --check` emitted only informational CRLF-to-LF conversion notices for already modified `docs/SR/index.md` and `docs/index.md`; exit code remained 0. No line-ending normalization was performed by WI-002.

### New Defect Found and Corrected

- SR-94 treated `TAG_NAME_DUPLICATED` as the final concurrency defense, but simultaneous requests can reach `uq_tags_name` and currently use the generic `DATA_INTEGRITY_VIOLATION` fallback.
- Corrected only SR-94 to preserve the service contract, identify the DB constraint as the final integrity defense, and require constraint-specific conversion to the stable tag-name error.

## Risks / Rollback

### Risks

- Constraint-name inspection is database/provider-specific; implementation must avoid converting unrelated integrity violations into tag-name duplicates.
- Frontend list checks can become stale and must remain advisory.
- SR-95 normalization and allowlist choices can collide with existing values and remain intentionally unapproved.

### Rollback

- Revert only the WI-002 hunk in `docs/SR/SR-94.md` that adds DB-race behavior and acceptance coverage.
- Remove `deliverables/user/WI-20260808-ATS-002-summary.md` and this Evidence Pack if the independent review record is abandoned.
- Preserve SR-95, both indexes, all WI-001 artifacts, code, DB, policies, historical SRs, and `output/client-demo-screenshots-20260716-140514.zip`.

## Follow-ups

- Resolve the six SR-95 policy decisions before implementation.
- During implementation, add create/update tests for frontend precheck, structured 409 handling, stale-list/server rejection, and concurrent `uq_tags_name` collision mapping.
