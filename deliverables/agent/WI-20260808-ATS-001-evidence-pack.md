---
version: 1.0
last_updated: 2026-08-08
project: ATS
owner: docops
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20260808-ATS-001-handoff.md
    reason: Approved Work Item scope and output contract
  - path: ../user/REQ-20260808-ATS-001.md
    reason: Approved request and acceptance criteria
  - path: ../user/WI-20260808-ATS-001-summary.md
    reason: User-facing result summary
---

# Evidence Pack: WI-20260808-ATS-001

## Summary

- Added two independent `OPEN` SR documents for tag duplicate-error handling and tag-name input-rule design, synchronized both documentation indexes, and preserved all code, DB, policy, and unrelated workspace state.

## Scope / DoD Check

- [x] `SR-94` separates the backend `409 TAG_NAME_DUPLICATED` contract from the frontend page-level `Failed to save tag` failure state.
- [x] `SR-94` covers create and update flows, pre-submit duplicate feedback, server 409 handling, and modal/list state preservation.
- [x] `SR-95` records frontend submit-time trim, backend `NotBlank`/50-character validation without normalization or a pattern, and DB-global name uniqueness.
- [x] `SR-95` separates recommendations from unresolved policy decisions.
- [x] `SR-95` recommends leading/trailing trim, internal spaces, repeated-space normalization, and a deliberate punctuation allowlist.
- [x] Official Splice and Epidemic Sound pages and representative naming examples are linked.
- [x] `docs/SR/index.md` and `docs/index.md` are synchronized to 94 SR documents and 196 total documents.
- [x] Local Markdown links added by this WI resolve to existing files.
- [x] Focused counts, status checks, and `git diff --check` passed.
- [x] No code, DB, existing tag data, or active design/policy document was changed.

## Reference Documents

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Constitution and approved execution boundary |
| 0 | `docs/standards/documentation-standards.md` | Document structure, link, and language rules |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 1 | `docs/policies/archive-policy.md` | Historical-record preservation boundary |
| 1 | `docs/standards/exception-handling.md` | Business error and response contract |
| 2 | `docs/SR/index.md` | SR numbering, status, and count source |
| 2 | `docs/SR/SR-70.md` | Existing SR structure reference |
| 2 | `docs/SR/SR-78.md` | Existing investigation-and-change SR reference |
| 2 | `docs/SR/SR-84.md` | Existing concise observed-state SR reference |
| 2 | `docs/SR/SR-90.md` | Existing proposal and unresolved-item SR reference |
| 2 | `docs/design/usecase/sound-tag.md` | Create/update duplicate-name 409 use cases |
| 2 | `docs/index.md` | Repository-wide document counts |
| Context | `deliverables/user/REQ-20260808-ATS-001.md` | Approved scope, DoD, and quality gates |
| Code | `frontend/src/pages/admin/TagManagePage.tsx` | Current trim, catch, and page replacement behavior |
| Code | `frontend/src/api/tags.ts` | Create/update request boundary |
| Code | `frontend/src/utils/validation.ts` | Frontend 50-character constant |
| Code | `src/main/java/com/atstudio/atstudio/dto/tag/TagCreateRequest.java` | Backend blank and length validation |
| Code | `src/main/java/com/atstudio/atstudio/service/TagService.java` | Exact-name duplicate checks and persistence |
| Code | `src/main/java/com/atstudio/atstudio/entity/Tag.java` | DB-global unique mapping |
| Code | `src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java` | Duplicate tag domain error |
| Code | `src/main/java/com/atstudio/atstudio/common/exception/GlobalExceptionHandler.java` | Error-code response serialization |

**Injection Rules Applied**:

- Rule source: `.claude/config/context-injection-rules.json`
- Assignee: `docops`
- Task type: `documentation`
- Injected tiers: Tier 0, relevant Tier 1, and task-specific Tier 2/context pointers from the WI handoff packet

## Evidence Pointers

### SR-94: Duplicate Save Flow

- `docs/SR/SR-94.md:13-18`
  - Separates the acceptance observation, backend 409 response, frontend generic catch, and page-level replacement.
- `docs/SR/SR-94.md:20-27`
  - Records the current frontend, API, backend, and UI responsibilities independently.
- `docs/SR/SR-94.md:29-57`
  - Specifies pre-submit feedback, authoritative server-response handling, state preservation, and acceptance checks for create/update.
- `frontend/src/pages/admin/TagManagePage.tsx:68-79`
  - Trims only at submit and maps every save rejection to `Failed to save tag`.
- `frontend/src/pages/admin/TagManagePage.tsx:108-114`
  - Replaces the management page whenever the shared error state is set.
- `src/main/java/com/atstudio/atstudio/service/TagService.java:29-39`
  - Create flow checks exact name and throws `TAG_NAME_DUPLICATED` before saving.
- `src/main/java/com/atstudio/atstudio/service/TagService.java:50-58`
  - Update flow excludes the unchanged current name and rejects another existing name.
- `src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java:245-248`
  - Defines HTTP 409 and the safe client message.
- `src/main/java/com/atstudio/atstudio/common/exception/GlobalExceptionHandler.java:35-39`
  - Serializes the domain error code into the API response.

### SR-95: Input-Rule Recommendation

- `docs/SR/SR-95.md:13-19`
  - Records current trim, length, normalization, pattern, persistence, and global-unique behavior.
- `docs/SR/SR-95.md:21-27`
  - Links official Splice and Epidemic Sound naming examples and constrains the inference drawn from them.
- `docs/SR/SR-95.md:29-57`
  - Separates whitespace normalization, punctuation allowlist candidates, and frontend/backend application principles.
- `docs/SR/SR-95.md:54-61`
  - Preserves case, Unicode, `#`, uniqueness scope, and migration as unresolved design decisions.
- `frontend/src/pages/admin/TagManagePage.tsx:195-200`
  - Input uses only a 50-character maximum at edit time.
- `frontend/src/utils/validation.ts:27`
  - Defines `TAG_NAME_MAX = 50` without a tag-name pattern.
- `src/main/java/com/atstudio/atstudio/dto/tag/TagCreateRequest.java:18-20`
  - Applies only `NotBlank` and the 50-character maximum to the name.
- `src/main/java/com/atstudio/atstudio/entity/Tag.java:20-21`
  - Maps the name as a globally unique 50-character column.

### External Official References

- `https://support.splice.com/en/articles/8652594-finding-sounds`
  - Official Splice search/filter guidance; accessed 2026-08-08.
- `https://splice.com/sounds/genres`
  - Official Splice genre inventory with spaces and music-specific punctuation; accessed 2026-08-08.
- `https://www.epidemicsound.com/music/genres/`
  - Official Epidemic Sound genre inventory with spaces, punctuation, apostrophes, parentheses, and digits; accessed 2026-08-08.

### Index Synchronization

- `docs/SR/index.md:3`
  - Contract updated to 94 files: 82 `DONE`, 9 `OPEN`, 2 `NOT CONFIRMED`, 1 `DROPPED`.
- `docs/SR/index.md:99-100`
  - Adds `SR-94` and `SR-95` as `OPEN`.
- `docs/index.md:2-3`
  - Registry version and update date advanced.
- `docs/index.md:28`
  - SR category count and range updated to 94 and `SR-92~95`.
- `docs/index.md:34`
  - Repository total updated from 194 to 196.

## Commands and Results

| Command | Result |
| --- | --- |
| `Get-ChildItem docs/SR -Filter 'SR-*.md'` with count | PASS; 94 numbered SR files |
| Parse `docs/SR/index.md` numbered table rows | PASS; 94 rows |
| Parse SR index statuses | PASS; 82 `DONE`, 9 `OPEN`, 2 `NOT CONFIRMED`, 1 `DROPPED` |
| Resolve relative Markdown links in all six WI output files | PASS; 10/10 local targets exist |
| `rg -n "Current index contract|SR-94|SR-95|\\| SR \\||Total Document Count" docs/SR/index.md docs/index.md docs/SR/SR-94.md docs/SR/SR-95.md` | PASS; new IDs and counts found at expected locations |
| `git diff --check -- docs/SR/index.md docs/index.md` | PASS; no whitespace errors |
| Scan all six WI files for trailing spaces or tabs | PASS; zero trailing-whitespace findings |
| Sum the category counts in `docs/index.md` | PASS; total is 196 |
| `git status --short` | PASS; scoped edits identified and pre-existing REQ/handoffs/ZIP preserved |

## Tests

- Documentation-only WI: no product build or runtime test was required.
- Focused file-count, index-status, local-link, scope, and whitespace checks passed.

## Risks / Rollback

### Risks

- The SR-95 allowlist is intentionally a proposal, not an approved contract.
- Database collation may already affect case or accent comparison; this was not inferred from the JPA mapping and remains a follow-up check.
- Canonicalization can create collisions among existing tag values; data inspection must precede implementation.
- A frontend list-based duplicate check can become stale and must not replace the backend 409 or DB unique constraint.

### Rollback

- Remove only `docs/SR/SR-94.md` and `docs/SR/SR-95.md`.
- Remove only the two corresponding rows and restore the prior contract counts in `docs/SR/index.md`.
- Restore `docs/index.md` version/date, SR count/range, and total count to their previous values.
- Remove this WI summary and Evidence Pack if the approved SR documentation work is abandoned.
- Preserve the pre-existing REQ, both WI handoff packets, and `output/client-demo-screenshots-20260716-140514.zip`.

## Follow-up

- `WI-20260808-ATS-002` is unblocked for independent cross-layer and documentation validation.

## Related Documents

- [WI-001 Handoff](WI-20260808-ATS-001-handoff.md)
- [WI-001 User Summary](../user/WI-20260808-ATS-001-summary.md)
- [Approved REQ](../user/REQ-20260808-ATS-001.md)
- [SR-94](../../docs/SR/SR-94.md)
- [SR-95](../../docs/SR/SR-95.md)
