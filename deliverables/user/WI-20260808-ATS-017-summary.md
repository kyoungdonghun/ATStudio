# WI-20260808-ATS-017 Completion Summary

## Status

WI-20260808-ATS-017 is **complete** on `codex/v1-release-rehearsal-fixes`. The unverified partial implementation was audited and completed in place. Tag create/update/direct API paths now share one backend-authoritative canonicalization and validation policy, database race failures use the stable tag duplicate contract, and save/delete failures remain local to their open modal without replacing the tag list.

No schema, existing tag data, dependency, stored media, payment path, root configuration, global documentation, Git state, or external system was changed. No commit was created.

## Delivered Behavior

- Canonical order is outer Unicode space-separator trim, internal space-separator collapse to ASCII space, Unicode NFC, then validation.
- Final names allow Hangul, ASCII letters/digits, ASCII space, and only `- & / ' ’ ( )`. Controls, `#`, emoji, and other punctuation are rejected.
- Raw input is capped at 200 Unicode code points in the service policy. Final names must be nonblank and at most 50 Unicode code points.
- The DTO no longer uses `@NotBlank` or `@Size` for the name, so null, blank, raw overflow, final overflow, and character-policy failures all reach the stable HTTP 400 `TAG_NAME_INVALID` business contract.
- No case folding was added. The existing global unique schema and database collation remain unchanged.
- Create and update persist and compare canonical names. A normalized self-edit is allowed; a normalized collision with another tag returns `TAG_NAME_DUPLICATED`.
- Create uses `saveAndFlush`; update uses `flush`. A race is translated only when evidence identifies `uq_tags_name`: exact named evidence for MySQL/Hibernate, or H2 SQLState `23505` plus unique `TAGS(NAME)` evidence. Unrelated integrity failures remain generic.
- Frontend duplicate precheck uses the same canonical form, excludes the edited tag ID, and sends no request for a known duplicate. Backend and database checks remain authoritative for stale lists and direct clients.
- Duplicate and invalid failures are name-field-local. Other save errors stay inside the create/edit modal. Delete errors stay inside the delete modal.
- Failed saves preserve the modal, raw name input, selected type, current list, and active filter. Name changes immediately clear or recompute field feedback.
- `USAGE` names remain raw in edit requests and receive `#` only in table and delete-confirmation display.
- Field errors use `aria-invalid`, `aria-describedby`, and `role="alert"`; filter state exposes `aria-pressed`.

## Changed Files

Backend production:

- `src/main/java/com/atstudio/atstudio/common/validation/ValidationConstants.java`
- `src/main/java/com/atstudio/atstudio/common/validation/TagNamePolicy.java`
- `src/main/java/com/atstudio/atstudio/dto/tag/TagCreateRequest.java`
- `src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java` (`TAG_NAME_INVALID` only for WI-017; WI-014/015/016 entries preserved)
- `src/main/java/com/atstudio/atstudio/service/TagNameConstraintTranslator.java`
- `src/main/java/com/atstudio/atstudio/service/TagService.java`

Frontend production:

- `frontend/src/utils/tagName.ts`
- `frontend/src/pages/admin/TagManagePage.tsx`
- `frontend/src/pages/admin/TagManagePage.module.css`

Tests:

- `src/test/java/com/atstudio/atstudio/common/validation/TagNamePolicyTest.java`
- `src/test/java/com/atstudio/atstudio/service/TagNameConstraintTranslatorTest.java`
- `src/test/java/com/atstudio/atstudio/service/TagServiceTest.java`
- `src/test/java/com/atstudio/atstudio/controller/TagControllerTest.java`
- `frontend/src/utils/tagName.test.ts`
- `frontend/src/pages/admin/TagManagePage.test.tsx`

WI deliverables:

- `deliverables/user/WI-20260808-ATS-017-summary.md`
- `deliverables/agent/WI-20260808-ATS-017-evidence-pack.md`

All unrelated dirty files, including the WI-014/015/016 work, `output/client-demo-screenshots-20260716-140514.zip`, root `application-local.yml`, audio/payment files, and `src/main/resources/schema.sql`, were left untouched by WI-017.

## Verification

| Command | Result |
|---|---|
| `.\gradlew.bat test --tests "com.atstudio.atstudio.common.validation.TagNamePolicyTest" --tests "com.atstudio.atstudio.service.TagNameConstraintTranslatorTest" --tests "com.atstudio.atstudio.service.TagServiceTest" --tests "com.atstudio.atstudio.service.TagServiceBranchCoverageTest" --tests "com.atstudio.atstudio.controller.TagControllerTest"` | PASS, 5 suites / 53 tests / 0 failures / 0 errors / 0 skipped |
| `npm test -- src/utils/tagName.test.ts src/pages/admin/TagManagePage.test.tsx` | PASS, 2 files / 26 tests / 0 failed |
| `npm run typecheck` | PASS, `tsc --noEmit` |
| `npx eslint src/utils/tagName.ts src/utils/tagName.test.ts src/pages/admin/TagManagePage.tsx src/pages/admin/TagManagePage.test.tsx --max-warnings 0` | PASS, 0 errors / 0 warnings |
| `npx prettier --check src/utils/tagName.ts src/utils/tagName.test.ts src/pages/admin/TagManagePage.tsx src/pages/admin/TagManagePage.test.tsx src/pages/admin/TagManagePage.module.css` | PASS, all 5 files matched |
| `git diff --check` | PASS, no whitespace errors; existing shared-worktree line-ending warnings remain |

## Constraint-Specific Mapping Proof

- `TagService.createTag` catches `DataIntegrityViolationException` around `saveAndFlush` only; `updateTag` catches it around `flush` only.
- `TagNameConstraintTranslator` returns `TAG_NAME_DUPLICATED` only for exact `uq_tags_name` named evidence or H2 `23505` evidence identifying the unique `tags(name)` path.
- Translator tests cover MySQL and H2-shaped tag-name evidence and prove unrelated MySQL `uq_users_email` and H2 `tags(type)` violations return the original exception unchanged.
- Service race tests force the MySQL-shaped create exception and H2-shaped update exception through the real `saveAndFlush`/`flush` catch sites.
- Controller tests prove raw input over 200 reaches the service and renders HTTP 400 `TAG_NAME_INVALID`, while a duplicate renders HTTP 409 `TAG_NAME_DUPLICATED`.

## UI State-Preservation Proof

- Separate `loadError`, `nameError`, `formError`, and `deleteError` states prevent mutation failures from entering the page-replacement load-error branch.
- The save catch does not close the modal, clear form state, change `activeType`, replace `tags`, or call `loadTags`.
- Component tests assert retained modal/name/type/list/filter state for client duplicate, server duplicate, server invalid, client invalid, and generic save failure.
- Tests also prove duplicate precheck sends no request, self edit succeeds, field feedback recomputes on name change, `USAGE` displays `#Shorts` while submitting `Shorts`, and delete failure remains in the delete modal.

## Residual Risks

- MySQL behavior is verified with a MySQL-shaped `1062/23000` exception and the actual service catch path, not a live concurrent MySQL request. A live database concurrency rehearsal remains appropriate in the later integration quality WI.
- Frontend duplicate precheck is intentionally snapshot-based. A stale list can still submit; backend lookup and `uq_tags_name` remain the final defenses.
- Existing invalid or noncanonical tags were not audited or migrated. This is required by the approved no-migration constraint.
- Existing MySQL collation determines case sensitivity for lookup/uniqueness. This WI adds no application case folding and does not change that schema behavior.

## Rollback

1. Revert only the WI-017 production and test files listed above while preserving unrelated shared-worktree changes.
2. In shared `BUSINESS_ERROR.java`, remove only `TAG_NAME_INVALID`; retain WI-014/015/016 entries.
3. Restore the prior `TagCreateRequest` annotations only together with the prior service behavior; otherwise raw overflow would stop producing the required domain error.
4. No schema or data rollback is required because no migration or data mutation was performed.

## WI Chain

- WI-018 is unblocked by completion of WI-017.
- WI-021's WI-017 prerequisite is cleared, but WI-021 remains blocked overall until WI-020 completes.
