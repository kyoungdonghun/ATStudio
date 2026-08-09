# Evidence Pack: WI-20260809-ATS-003

## Summary (one-liner)

- Aligned all stale Usage display assertions in the focused admin/subscriber coverage test with the approved hash-prefixed UI contract while preserving the unprefixed create payload.

## Scope / DoD Check

- DoD items:
  - [x] Usage display expectations assert `#Shorts` and `#Tutorial`.
  - [x] The create payload remains `{ name: 'Tutorial', type: 'USAGE' }`.
  - [x] The focused Vitest file passes with zero failures.
  - [x] No product runtime path was changed.
  - [x] Unrelated dirty-worktree changes were preserved.

## Reference Documents (Tier 0-2)

Handoff-provided context pointers:

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Required governance pointer |
| 0 | `docs/standards/development-standards.md` | Engineering standards pointer |
| 0 | `docs/standards/frontend-standards.md` | Frontend QA standards pointer |
| 1 | `docs/policies/quality-gates.md` | Focused verification policy pointer |

Additional traceability pointers:

- `deliverables/user/REQ-20260808-ATS-004.md`
- `deliverables/agent/WI-20260808-ATS-024-handoff.md`
- `deliverables/agent/WI-20260809-ATS-003-handoff.md`

Execution used the approved WI handoff and the user-injected constraints. Broad documentation reconnaissance was not performed because the WI explicitly prohibited additional exploration.

## Evidence Pointers

- Contract implementation:
  - `frontend/src/pages/admin/TagManagePage.tsx:237` renders tag names through `formatTagNameForDisplay`.
  - `frontend/src/utils/tagName.ts:48` prefixes `USAGE` display names with `#` without changing stored names.
- Test contract:
  - `frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx:394` expects `#Shorts`.
  - `frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx:406` preserves `createTag({ name: 'Tutorial', type: 'USAGE' })`.
  - `frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx:408` expects `#Tutorial` after reload.
- Files changed by this WI:
  - `frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx` (two display-only assertions in an already-dirty file)
  - `deliverables/user/WI-20260809-ATS-003-summary.md`
  - `deliverables/agent/WI-20260809-ATS-003-evidence-pack.md`

## Commands & Outputs

Command executed twice from `frontend/`:

```text
npx vitest run src/test/coverage/adminSubscriberPages.coverage.test.tsx
```

First focused result after the initial `#Shorts` correction:

```text
Exit code: 1
Test Files: 1 failed (1)
Tests: 32 passed, 1 failed (33)
Duration: 9.28s
Failure: Unable to find text `Tutorial`; rendered DOM contained `#Tutorial`.
Location: src/test/coverage/adminSubscriberPages.coverage.test.tsx:408
```

Final focused result after the clarified scope authorized the second stale Usage assertion:

```text
Exit code: 0
Test Files: 1 passed (1)
Tests: 33 passed (33)
Duration: 8.64s
Vitest: v4.1.4
```

Input baseline supplied by the handoff, not rerun by this WI:

```text
159 suites, 579 tests, 578 passed, 1 failed
Original failure: getByText('Shorts') around line 394
```

## Tests

- `npx vitest run src/test/coverage/adminSubscriberPages.coverage.test.tsx` -> PASS, 1/1 file and 33/33 tests.
- Full-suite execution was intentionally excluded from WI-20260809-ATS-003.

## Risks / Rollback

- Risks:
  - Runtime risk is negligible because this WI changed test expectations only.
  - Residual full-suite risk remains until WI-20260808-ATS-024 resumes broader verification.
  - The target test file contained pre-existing changes; whole-file restoration would overwrite unrelated work.
- Rollback:
  - Revert only `#Shorts` to `Shorts` at the focused display assertion.
  - Revert only `#Tutorial` to `Tutorial` at the post-create display assertion.
  - Do not use whole-file checkout or reset on the dirty worktree.

## Follow-ups

- WI-20260808-ATS-024 unblock status: **UNBLOCKED**. Its focused blocker is resolved, and broader verification may resume.
- No commit, push, product edit, database/schema/data operation, secret access, external call, or intentional ZIP modification was performed.
