# WI-20260228-ATS-006 Summary — Question/Track/Subscription 코드 리뷰

**검토 범위:** WI-002/003 수정 내용 — CR-C-001, CR-A-001, CR-B-001~004
**최종 판정:** ✅ PASS — 모든 이슈 올바르게 수정됨. MINOR 1건, SUGGESTION 1건 (비차단)

---

## 파일별 판정

| 파일 | 판정 | 비고 |
|------|------|------|
| `QuestionService.java` | ✅ PASS | Attachment→Answer→Question 순서 올바름, @Transactional |
| `AnswerRepository.java` | ✅ PASS | deleteAllByQuestion() 추가 |
| `QuestionAttachmentRepository.java` | ✅ PASS | deleteAllByQuestion() 추가 |
| `Track.java` | ✅ PASS | @OneToMany(mappedBy="track", fetch=LAZY) 올바름 |
| `TrackSpecification.java` | ✅ PASS | join("trackTags") 정상 해석 가능 |
| `UserSubscriptionController.java` | ✅ PASS | DELETE 204 No Content (MINOR: @ResponseStatus 중복) |
| `UserSubscriptionService.java` | ✅ PASS | proratedAmount.abs() 제거됨, 음수 그대로 전달 |
| `SubscriptionService.java` | ✅ PASS | try/catch INVALID_ARGUMENT(400) 처리 |
| `QuestionServiceTest.java` | ✅ PASS | inOrder()로 삭제 순서 검증 |
| `UserSubscriptionControllerTest.java` | ✅ PASS | 204 isNoContent() 검증 |
| `UserSubscriptionServiceTest.java` | ✅ PASS | argThat(signum<0)로 음수 금액 검증 |
| `SubscriptionServiceTest.java` | ✅ PASS | INVALID_ARGUMENT 에러코드 검증 |

---

## MINOR/SUGGESTION (비차단)

| 심각도 | 위치 | 내용 |
|--------|------|------|
| MINOR | `UserSubscriptionController.java:98,107` | `@ResponseStatus(HttpStatus.NO_CONTENT)` + `ResponseEntity.noContent()` 중복 — 무해하나 불필요 |
| SUGGESTION | `QuestionService.java:185-188` | 첨부파일 DB 삭제 시 물리 파일(StorageService) 미정리 — 고아 파일 남을 수 있음 |
