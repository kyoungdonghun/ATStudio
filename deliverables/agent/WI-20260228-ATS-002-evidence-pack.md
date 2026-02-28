# WI-20260228-ATS-002 Evidence Pack

## Patch Rationale

CR-C-001 identified that `QuestionService.deleteQuestion()` deletes only the `questions` row without removing child `answers` and `question_attachments` rows first. Since both child tables have `question_id` FK constraints pointing to `questions.id`, the DB rejects the parent deletion with `DataIntegrityViolationException`.

**Decision:** Explicit service-layer deletion (Option 1) was chosen over JPA cascade/orphanRemoval (Option 2) because:
1. No entity structure change required (Question entity stays without `@OneToMany` collections)
2. More predictable -- no risk of unexpected lazy loading or cascade side effects in other code paths
3. Deletion order is explicit and auditable in the service method

## File/Line Change Pointers

### 1. QuestionService.java (main fix)

**Path:** `src/main/java/com/atstudio/atstudio/service/QuestionService.java`
**Lines:** 173-189

**Before:**
```java
@Transactional
public void deleteQuestion(Long questionId, CustomUserDetails userDetails) {
    Question question = findQuestionById(questionId);
    boolean isOwner = question.getUser().getId().equals(userDetails.getId());
    boolean isAdmin = userDetails.getRole() == UserRole.ADMIN;
    if (isAdmin) {
        questionRepository.delete(question);
        return;
    }
    if (isOwner && question.getStatus() == QuestionStatus.OPEN) {
        questionRepository.delete(question);
        return;
    }
    throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
}
```

**After:**
```java
@Transactional
public void deleteQuestion(Long questionId, CustomUserDetails userDetails) {
    Question question = findQuestionById(questionId);
    boolean isOwner = question.getUser().getId().equals(userDetails.getId());
    boolean isAdmin = userDetails.getRole() == UserRole.ADMIN;
    if (!isAdmin && !(isOwner && question.getStatus() == QuestionStatus.OPEN)) {
        throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
    }
    // Delete children first to avoid FK constraint violation (CR-C-001)
    attachmentRepository.deleteAllByQuestion(question);
    answerRepository.deleteAllByQuestion(question);
    questionRepository.delete(question);
}
```

**Secondary improvement:** Consolidated duplicated `questionRepository.delete()` calls into a single path after the guard clause.

### 2. AnswerRepository.java

**Path:** `src/main/java/com/atstudio/atstudio/repository/AnswerRepository.java`
**Line added:** 18

```java
void deleteAllByQuestion(Question question);
```

### 3. QuestionAttachmentRepository.java

**Path:** `src/main/java/com/atstudio/atstudio/repository/QuestionAttachmentRepository.java`
**Line added:** 4 (import), 16 (method)

```java
import com.atstudio.atstudio.entity.Question;
// ...
void deleteAllByQuestion(Question question);
```

### 4. QuestionServiceTest.java

**Path:** `src/test/java/com/atstudio/atstudio/service/QuestionServiceTest.java`

**Updated tests:**
- `success_ownerOpenStatus()` (line ~441): Added `verify(attachmentRepository).deleteAllByQuestion()` and `verify(answerRepository).deleteAllByQuestion()`
- `success_admin()` (line ~455): Same cascade verification added

**New tests:**
- `success_cascadeDeleteWithAnswers()` (line ~469): Verifies deletion order via `inOrder()` -- attachments before answers before question
- `success_cascadeDeleteWithAttachments()` (line ~484): Verifies all three delete calls are made for attachment-bearing questions

## Test Results

```
BUILD SUCCESSFUL
467 tests, 0 failures, 0 errors
Duration: 30.383s
```

## Acceptance Criteria Checklist

- [x] QuestionService.deleteQuestion() -- children deleted before question
- [x] FK constraint violation eliminated (attachments + answers deleted first)
- [x] Answer-less Question deletes normally (null-safe: `deleteAllByQuestion` is a no-op on empty set)
- [x] Attachment-less Question deletes normally (same reason)
- [x] QuestionServiceTest: Answer cascade test added
- [x] QuestionServiceTest: Attachment cascade test added
- [x] All 467 tests pass, 0 regressions

## Follow-Up

- **Blocks:** WI-20260228-ATS-004 (unblocked by this WI completion)
- No further action needed for this WI
