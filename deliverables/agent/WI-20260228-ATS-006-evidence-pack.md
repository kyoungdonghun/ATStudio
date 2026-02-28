# WI-20260228-ATS-006 Evidence Pack — Question/Track/Subscription 코드 리뷰

## CR-C-001: QuestionService cascade 삭제

**판정: ✅ PASS**

`QuestionService.java:185-188`:
```java
// Delete children first to avoid FK constraint violation (CR-C-001)
attachmentRepository.deleteAllByQuestion(question);
answerRepository.deleteAllByQuestion(question);
questionRepository.delete(question);
```
- 삭제 순서: Attachment → Answer → Question (FK 방향 올바름)
- null-safe: deleteAllByQuestion()은 매칭 레코드 없으면 0건 삭제 (NPE 없음)
- @Transactional: L173 메서드 레벨 (클래스 readOnly override)
- `QuestionServiceTest.java` inOrder() 검증: 삭제 순서 강제 확인

**SUGGESTION**: StorageService 물리 파일 미정리 (`QuestionService.java:185-188`) — 첨부파일 삭제 시 저장소 실제 파일도 삭제 필요 (비차단)

## CR-A-001: Track @OneToMany trackTags

**판정: ✅ PASS**

`Track.java:55-57`:
```java
@OneToMany(mappedBy = "track", fetch = FetchType.LAZY)
@Builder.Default
private List<TrackTag> trackTags = new ArrayList<>();
```
- `TrackTag.java`: `@ManyToOne track` 필드명과 mappedBy 일치 ✅
- LAZY fetch → N+1 위험 없음 ✅
- `TrackSpecification.java:40` `root.join("trackTags")` 정상 해석 ✅

## CR-B-001/002: DELETE 204

**판정: ✅ PASS**

`UserSubscriptionController.java:98-112` — 두 DELETE 메서드 `ResponseEntity.noContent().build()` 반환
**MINOR**: `@ResponseStatus(HttpStatus.NO_CONTENT)` 중복 어노테이션 존재 (무해)

## CR-B-003: proratedAmount.abs() 제거

**판정: ✅ PASS**

`UserSubscriptionService.java:167`:
```java
BigDecimal proratedAmount = newPrice.subtract(refundAmount);  // abs() 없음
```
`UserSubscriptionServiceTest.java` — `argThat(amount -> ((BigDecimal) amount).signum() < 0)` 음수 검증

## CR-B-004: UserType.valueOf() try/catch

**판정: ✅ PASS**

`SubscriptionService.java:24-29`:
```java
try {
    type = UserType.valueOf(userType);
} catch (IllegalArgumentException e) {
    throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
}
```
INVALID_ARGUMENT → HTTP 400 (GlobalExceptionHandler 매핑 확인)
