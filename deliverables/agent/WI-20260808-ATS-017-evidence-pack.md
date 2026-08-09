# Evidence Pack: WI-20260808-ATS-017

## Summary (one-liner)

- Completed one backend-authoritative tag-name policy, constraint-specific duplicate race translation, and modal-local admin error UX with preserved working state.

## Scope / DoD Check

- [x] Canonical order is trim outer Unicode space separators, collapse internal space separators to ASCII space, apply NFC, then validate.
- [x] Final allowlist is Hangul, ASCII letters/digits/space, and exactly `- & / ' ’ ( )`.
- [x] `#`, emoji, controls, and unapproved punctuation are rejected; `#` is display-only for `USAGE`.
- [x] Raw input is limited to 200 Unicode code points in the service policy; final input is nonblank and at most 50 code points.
- [x] Invalid names, including raw overflow, produce stable HTTP 400 `TAG_NAME_INVALID` instead of bean-validation fallback.
- [x] Create/update canonical persistence, normalized duplicate handling, and normalized self-edit are covered.
- [x] `saveAndFlush`/`flush` make create/update race translation observable inside the service transaction.
- [x] MySQL and H2-shaped `uq_tags_name` evidence maps to HTTP 409 `TAG_NAME_DUPLICATED`; unrelated integrity failures pass through.
- [x] Frontend known-duplicate precheck sends no request and excludes the edited tag ID.
- [x] Duplicate/invalid errors are field-local, generic save errors are form-modal-local, and delete errors are delete-modal-local.
- [x] Failed saves preserve modal, name, type, list, and active filter; changing name recomputes feedback.
- [x] `USAGE` table/delete display uses `#`; edit input and request body remain raw.
- [x] Focused backend/frontend tests, typecheck, ESLint, Prettier, and diff checks pass.
- [x] No schema, data, dependency, file deletion, Git mutation, external call, or existing-tag migration occurred.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution, approved execution, transparency, and REST-first rules |
| 0 | `docs/standards/development-standards.md` | Java/React layering, exception, testing, and evidence standards |
| 0 | `docs/standards/documentation-standards.md` | English deliverable and pointer-first documentation rules |
| 0 | `docs/standards/glossary.md` | Canonical Tag and Usage Guide Tag terminology |
| 2 | `docs/design/usecase/sound-tag.md` | Existing create/update/global uniqueness/USAGE behavior |
| 2 | `docs/SR/SR-94.md` | Duplicate precheck, race translation, and state-preservation requirements |
| 2 | `docs/SR/SR-95.md` | Whitespace, character policy, and validation problem statement |
| REQ | `deliverables/user/REQ-20260808-ATS-004.md` | Approved policy baseline, scope, dependencies, and quality gates |
| WI | `deliverables/agent/WI-20260808-ATS-017-handoff.md` | Assignee, DoD, forbidden actions, output contract, and blockers |

**Assignee:** `se`

**Task type:** backend/frontend implementation and focused tests

**Write scope applied:** WI-017 production/test files and its two required deliverables. Shared `BUSINESS_ERROR.java` retained unrelated WI entries.

## Design Rationale

1. `TagNamePolicy` owns pure canonicalization and code-point validation. The service checks the raw bound before canonicalization so a compressible 201-character input cannot bypass the explicit resource bound.
2. Name bean-validation annotations were removed while type `@NotNull` remains. This lets every name-policy failure converge on `BusinessException(TAG_NAME_INVALID)` without weakening type validation.
3. Canonical names are used for repository lookup and entity mutation. The existing name is compared after canonicalization, allowing a self-edit that changes only spacing/NFC representation.
4. `TagNameConstraintTranslator` is package-visible and focused so tests can exercise real provider-shaped evidence without reflection. MySQL/Hibernate uses exact constraint naming; H2 requires SQLState `23505` plus `TAGS(NAME)` evidence because H2 may expose generated index wording.
5. Create uses `saveAndFlush` and update calls `flush` inside their catch sites. Deferred constraint failures therefore cannot escape to the generic global integrity handler after service return.
6. Frontend utility code mirrors canonicalization for UX only. Backend and database checks remain authoritative.
7. Page load failures and mutation failures use separate state. A failed mutation leaves the mounted list and modal state intact; only successful mutations close and reload.

## Evidence Pointers

Backend production:

- `src/main/java/com/atstudio/atstudio/common/validation/ValidationConstants.java:34` - explicit raw cap of 200.
- `src/main/java/com/atstudio/atstudio/common/validation/TagNamePolicy.java:17` - trim/collapse/NFC canonicalization.
- `src/main/java/com/atstudio/atstudio/common/validation/TagNamePolicy.java:27` - raw Unicode code-point bound.
- `src/main/java/com/atstudio/atstudio/common/validation/TagNamePolicy.java:32` - final code-point and allowlist validation.
- `src/main/java/com/atstudio/atstudio/dto/tag/TagCreateRequest.java:14` - name reaches service policy without `@NotBlank/@Size`; type remains `@NotNull`.
- `src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java:270` - stable 400 `TAG_NAME_INVALID`.
- `src/main/java/com/atstudio/atstudio/service/TagService.java:32` - create canonical lookup/persistence.
- `src/main/java/com/atstudio/atstudio/service/TagService.java:43` - create `saveAndFlush` race boundary.
- `src/main/java/com/atstudio/atstudio/service/TagService.java:60` - update canonical/self comparison.
- `src/main/java/com/atstudio/atstudio/service/TagService.java:68` - update `flush` race boundary.
- `src/main/java/com/atstudio/atstudio/service/TagService.java:75` - raw and final policy convergence on `TAG_NAME_INVALID`.
- `src/main/java/com/atstudio/atstudio/service/TagNameConstraintTranslator.java:26` - translate only matched evidence and preserve original exception otherwise.
- `src/main/java/com/atstudio/atstudio/service/TagNameConstraintTranslator.java:33` - bounded cause-chain inspection.
- `src/main/java/com/atstudio/atstudio/service/TagNameConstraintTranslator.java:52` - exact named-constraint normalization.
- `src/main/java/com/atstudio/atstudio/service/TagNameConstraintTranslator.java:63` - H2 SQLState/table/column evidence gate.

Frontend production:

- `frontend/src/utils/tagName.ts:20` - mirrored trim/collapse/NFC utility.
- `frontend/src/utils/tagName.ts:27` - raw/final/allowlist precheck.
- `frontend/src/utils/tagName.ts:37` - canonical duplicate check with self-ID exclusion and no case folding.
- `frontend/src/utils/tagName.ts:48` - display-only `USAGE` hash.
- `frontend/src/pages/admin/TagManagePage.tsx:92` - name-change validation and duplicate recomputation.
- `frontend/src/pages/admin/TagManagePage.tsx:107` - no-request precheck and canonical request body.
- `frontend/src/pages/admin/TagManagePage.tsx:130` - server duplicate/invalid field mapping and modal-local generic error.
- `frontend/src/pages/admin/TagManagePage.tsx:153` - delete failure remains in delete modal.
- `frontend/src/pages/admin/TagManagePage.tsx:205` - accessible active-filter state.
- `frontend/src/pages/admin/TagManagePage.tsx:237` - table display-only hash.
- `frontend/src/pages/admin/TagManagePage.tsx:275` - accessible field-error semantics.
- `frontend/src/pages/admin/TagManagePage.tsx:326` - delete-confirmation display-only hash.
- `frontend/src/pages/admin/TagManagePage.module.css:164` - invalid field and modal-local error presentation.

Tests:

- `src/test/java/com/atstudio/atstudio/common/validation/TagNamePolicyTest.java:12` - canonical order; punctuation/rejection/boundaries continue through line 63.
- `src/test/java/com/atstudio/atstudio/service/TagNameConstraintTranslatorTest.java:16` - MySQL/H2 translation and unrelated pass-through.
- `src/test/java/com/atstudio/atstudio/service/TagServiceTest.java:48` - canonical create persistence.
- `src/test/java/com/atstudio/atstudio/service/TagServiceTest.java:103` - create race at `saveAndFlush`.
- `src/test/java/com/atstudio/atstudio/service/TagServiceTest.java:150` - canonical update persistence.
- `src/test/java/com/atstudio/atstudio/service/TagServiceTest.java:200` - normalized self edit.
- `src/test/java/com/atstudio/atstudio/service/TagServiceTest.java:217` - update race at `flush`.
- `src/test/java/com/atstudio/atstudio/controller/TagControllerTest.java:75` - raw overflow stable 400 and service invocation.
- `src/test/java/com/atstudio/atstudio/controller/TagControllerTest.java:93` - stable duplicate 409.
- `frontend/src/utils/tagName.test.ts:10` - canonicalization, allowlist, boundaries, duplicate/no-folding, and display tests.
- `frontend/src/pages/admin/TagManagePage.test.tsx:64` - no-request duplicate and recomputation.
- `frontend/src/pages/admin/TagManagePage.test.tsx:83` - self edit and raw `USAGE` submit.
- `frontend/src/pages/admin/TagManagePage.test.tsx:104` - server duplicate and preserved state.
- `frontend/src/pages/admin/TagManagePage.test.tsx:123` - server invalid and preserved state.
- `frontend/src/pages/admin/TagManagePage.test.tsx:140` - accessible client-invalid state.
- `frontend/src/pages/admin/TagManagePage.test.tsx:162` - generic save failure preservation.
- `frontend/src/pages/admin/TagManagePage.test.tsx:178` - delete-modal failure and display hash.

Complete changed-file inventory is in `deliverables/user/WI-20260808-ATS-017-summary.md`.

## Commands & Outputs

1. Final backend focused regression:

```powershell
.\gradlew.bat test --tests "com.atstudio.atstudio.common.validation.TagNamePolicyTest" --tests "com.atstudio.atstudio.service.TagNameConstraintTranslatorTest" --tests "com.atstudio.atstudio.service.TagServiceTest" --tests "com.atstudio.atstudio.service.TagServiceBranchCoverageTest" --tests "com.atstudio.atstudio.controller.TagControllerTest"
```

- PASS: `BUILD SUCCESSFUL in 38s`.
- XML totals: 5 suites, 53 tests, 0 failures, 0 errors, 0 skipped.
- Per suite: policy 21, translator 4, service 13, existing service branch coverage 3, controller 12.

2. Final frontend focused regression:

```powershell
npm test -- src/utils/tagName.test.ts src/pages/admin/TagManagePage.test.tsx
```

- PASS: 2 test files, 26 tests, 0 failed; Vitest duration 4.10s.

3. Final TypeScript typecheck:

```powershell
npm run typecheck
```

- PASS: `tsc --noEmit`, exit code 0.

4. Final changed-file ESLint:

```powershell
npx eslint src/utils/tagName.ts src/utils/tagName.test.ts src/pages/admin/TagManagePage.tsx src/pages/admin/TagManagePage.test.tsx --max-warnings 0
```

- PASS: exit code 0, 0 errors, 0 warnings.
- First check found one `no-useless-escape` on a trailing character-class hyphen. The literal was corrected and the same command passed.

5. Final changed-frontend Prettier check:

```powershell
npx prettier --check src/utils/tagName.ts src/utils/tagName.test.ts src/pages/admin/TagManagePage.tsx src/pages/admin/TagManagePage.test.tsx src/pages/admin/TagManagePage.module.css
```

- PASS: all 5 matched files use Prettier style.
- First check identified `src/utils/tagName.ts`; `npx prettier --write src/utils/tagName.ts` formatted that file, then the exact check passed.

6. Whitespace verification:

```powershell
git diff --check
```

- PASS: no whitespace errors. Git reported existing CRLF-to-LF warnings across the shared dirty worktree; no line-ending rewrite command was run.

## Constraint-Specific Mapping Proof

- MySQL test evidence wraps SQLState `23000`, vendor code `1062`, and `Duplicate entry ... for key 'tags.uq_tags_name'` in `DataIntegrityViolationException`.
- H2 test evidence wraps SQLState `23505` and `Unique index ... ON PUBLIC.TAGS(NAME ...)`.
- Unrelated MySQL `users.uq_users_email` and H2 `TAGS(TYPE)` evidence return the same original `DataIntegrityViolationException` instance.
- Service tests throw the shaped exceptions from mocked `saveAndFlush` and `flush`; both emerge as `BusinessException(TAG_NAME_DUPLICATED)` with the original exception retained as cause.
- Global fallback remains unchanged, so all unmatched integrity violations continue to produce generic `DATA_INTEGRITY_VIOLATION` behavior.

## UI State-Preservation Proof

- Mutation errors never write `loadError`; therefore they cannot enter the early page-replacement return.
- Client duplicate and invalid paths return before `setFormLoading` or API invocation.
- Server duplicate/invalid and generic catch branches do not call `closeFormModal` or `loadTags`.
- Component tests assert dialog presence, raw name, selected type, visible filtered list row, and `aria-pressed=true` after each save failure class.
- Name changes run validation first, then canonical duplicate recomputation; tests cover clear-to-valid and recompute-to-another-existing-name.
- Delete catch writes only `deleteError`; its test keeps the `Delete Tag` dialog, `#Shorts`, list heading, and active `USAGE` filter visible.

## Risks / Rollback

Risks:

- Provider translation is tested with realistic MySQL/H2 exception shapes rather than a live concurrent MySQL run. Later integration quality work should retain a real-MySQL race rehearsal if that environment is available.
- Snapshot duplicate precheck cannot prevent stale-list races. The service lookup and database unique key intentionally remain authoritative.
- Existing noncanonical tags remain unchanged under the approved no-migration rule.
- Case sensitivity remains governed by the existing MySQL collation; no code-level case folding was introduced.

Rollback:

1. Revert only the WI-017 production/test files listed in the user summary while preserving all unrelated dirty changes.
2. Remove only `TAG_NAME_INVALID` from shared `BUSINESS_ERROR.java`; keep WI-014/015/016 constants.
3. Revert `TagCreateRequest`, `TagNamePolicy`, and the service policy as one behavioral unit so invalid-name error routing remains internally consistent.
4. No schema/data rollback or migration is required.

## Follow-ups

- WI-018 is unblocked.
- WI-021 has its WI-017 prerequisite cleared but remains blocked on WI-020.
- WI-022 owns global documentation updates; WI-017 changed only its required summary/evidence documents.
- WI-023/WI-024/WI-025/WI-026/WI-027 retain repository-wide backend/frontend quality gates beyond this focused WI.
