# WI-20260228-ATS-002 Summary

## Change Summary

**What changed:** Fixed Question deletion to properly cascade-delete child records (Answer, QuestionAttachment) before deleting the Question itself.

**Why:** CR-C-001 (CRITICAL) -- `QuestionService.deleteQuestion()` only deleted the `questions` row, leaving orphaned `answers` and `question_attachments` rows. When FK constraints are enforced, this causes `DataIntegrityViolationException` (HTTP 500).

**Approach chosen:** Explicit service-layer deletion (Option 1 from the handoff). Added `deleteAllByQuestion(Question)` methods to `AnswerRepository` and `QuestionAttachmentRepository`, then called them in `deleteQuestion()` before `questionRepository.delete()`.

## Risk Assessment

- **Risk level:** LOW
- The change only affects the delete path; create/read/update paths are untouched.
- Deletion order (attachments -> answers -> question) follows FK dependency direction.
- No DB schema changes; no migration files created.

## Verification Results

| Check | Result |
|-------|--------|
| Build | SUCCESSFUL |
| Total tests | 467 |
| Failures | 0 |
| New tests added | 2 (cascade delete with answers, cascade delete with attachments) |
| Existing tests updated | 2 (owner+OPEN delete, ADMIN delete now verify cascade calls) |
| Regressions | None |

## Files Modified

| File | Change |
|------|--------|
| `QuestionService.java` | `deleteQuestion()` -- added child deletion before parent |
| `AnswerRepository.java` | Added `deleteAllByQuestion(Question)` |
| `QuestionAttachmentRepository.java` | Added `deleteAllByQuestion(Question)` + Question import |
| `QuestionServiceTest.java` | 2 new tests + 2 existing tests updated to verify cascade |
