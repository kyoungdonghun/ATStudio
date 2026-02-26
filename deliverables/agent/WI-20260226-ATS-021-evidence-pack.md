# WI-20260226-ATS-021 Evidence Pack

## Created/Modified Files

| File Path | Change Type |
|-----------|-------------|
| `src/main/java/.../entity/Question.java` | MODIFIED - added `updateStatus()` |
| `src/main/java/.../repository/QuestionRepository.java` | MODIFIED - added JpaSpecificationExecutor |
| `src/main/java/.../repository/AnswerRepository.java` | MODIFIED - added query methods |
| `src/main/java/.../repository/QuestionAttachmentRepository.java` | MODIFIED - added query methods |
| `src/main/java/.../repository/spec/QuestionSpecification.java` | NEW |
| `src/main/java/.../dto/question/QuestionCreateRequest.java` | NEW |
| `src/main/java/.../dto/question/AnswerCreateRequest.java` | NEW |
| `src/main/java/.../dto/question/QuestionStatusUpdateRequest.java` | NEW |
| `src/main/java/.../dto/question/QuestionResponse.java` | NEW |
| `src/main/java/.../dto/question/QuestionListItemResponse.java` | NEW |
| `src/main/java/.../dto/question/AnswerResponse.java` | NEW |
| `src/main/java/.../dto/question/AttachmentResponse.java` | NEW |
| `src/main/java/.../service/QuestionService.java` | NEW |
| `src/main/java/.../controller/QuestionController.java` | NEW |
| `src/test/java/.../service/QuestionServiceTest.java` | NEW |
| `src/test/java/.../controller/QuestionControllerTest.java` | NEW |

## Test Results

- Command: `gradlew.bat test`
- Result: **362 tests, 0 failures** (323 existing + 39 new)
- New test breakdown:
  - QuestionServiceTest: 24 cases (createQuestion:2, createAnswer:4, getQuestions:3, getQuestion:5, downloadAttachment:3, updateQuestionStatus:2, deleteQuestion:5)
  - QuestionControllerTest: 15 cases (auth/role verification for all 7 endpoints)

## Regression Risk
- No existing tests broken (323 -> 323 still green)
- SecurityConfig unchanged (ADMIN rule for `/api/questions/*/status` already existed)

## Bugs Fixed During Implementation
1. `Specification.where(null)` -> IllegalArgumentException in Spring Data 4.x -> Fixed with `cb.conjunction()` base
2. Access check order in `createAnswer()` -> moved before `userRepository.findById()` to ensure correct 403 vs 404
3. Java 17 compatibility: replaced `List.getFirst()` (Java 21+) with `List.get(0)`

## Follow-up WI
- None. REQ-20260221-ATS-005 Inquiry scope complete.
