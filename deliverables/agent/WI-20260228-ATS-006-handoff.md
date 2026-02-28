[WI HEADER]
WI ID: WI-20260228-ATS-006
REQ: REQ-20260228-ATS-010
Agent: cr
Depends On: WI-20260228-ATS-004
Blocks: -

[WI SUMMARY]
Why: WI-002(Question cascade) + WI-003(Track/Subscription) 수정 내용 코드 리뷰.
     CR-C-001 cascade 삭제 순서, CR-A-001 Track @OneToMany, CR-B-001~004 Subscription 수정이
     올바르게 구현되었는지 독립 검증.
Scope (in):
  - QuestionService.java: deleteQuestion() 삭제 순서 및 null-safe 처리 (CR-C-001)
  - AnswerRepository.java, QuestionAttachmentRepository.java: 신규 메서드 검토
  - Track.java: @OneToMany trackTags LAZY fetch 검토 (CR-A-001)
  - TrackSpecification.java: join("trackTags") 정상 동작 확인
  - UserSubscriptionController.java: DELETE 204 응답 검토 (CR-B-001/002)
  - UserSubscriptionService.java: proratedAmount abs() 제거 검토 (CR-B-003)
  - SubscriptionService.java: UserType.valueOf() try/catch 처리 검토 (CR-B-004)
  - 신규 테스트 품질 검토
Scope (out):
  - Security/Auth/User 범위 (WI-005)
  - 코드 수정 (Read-only 리뷰만)
DoD:
  - 각 파일 PASS/FAIL/SUGGESTION 판정
  - FAIL 항목 파일:라인 포인터 포함
Constraints/Forbidden: 코드 수정 금지. Read-only 리뷰만.

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] QuestionService.deleteQuestion(): Attachment→Answer→Question 순서 또는 안전한 cascade 방식 확인
  - [ ] null-safe: Answer 없는 Question, Attachment 없는 Question 모두 처리 가능
  - [ ] Track.java: @OneToMany(mappedBy="track", fetch=LAZY) 올바름
  - [ ] UserSubscriptionController: DELETE 엔드포인트 204 No Content 반환 확인
  - [ ] UserSubscriptionService: proratedAmount에 .abs() 없음 확인
  - [ ] SubscriptionService: UserType.valueOf() IllegalArgumentException catch → 400 응답 확인
Quality:
  - [ ] 신규 테스트가 cascade 삭제 시나리오 충분히 커버
  - [ ] Track trackTags LAZY fetch → N+1 위험 없음 확인
  - [ ] 에러 처리 일관성 (BUSINESS_ERROR 패턴 준수)

[INPUT POINTERS]
Tier 0 (Constitution):
  - docs/standards/core-principles.md

Tier 0 (Standards — cr):
  - docs/standards/development-standards.md

REQ:
  - deliverables/user/REQ-20260228-ATS-010.md

WI-002/003 수정 결과:
  - deliverables/user/WI-20260228-ATS-002-summary.md
  - deliverables/agent/WI-20260228-ATS-002-evidence-pack.md
  - deliverables/user/WI-20260228-ATS-003-summary.md
  - deliverables/agent/WI-20260228-ATS-003-evidence-pack.md

회귀 검증 결과:
  - deliverables/user/WI-20260228-ATS-004-summary.md

리뷰 대상 파일:
  - src/main/java/com/atstudio/atstudio/service/inquiry/QuestionService.java
  - src/main/java/com/atstudio/atstudio/repository/AnswerRepository.java
  - src/main/java/com/atstudio/atstudio/repository/QuestionAttachmentRepository.java
  - src/main/java/com/atstudio/atstudio/entity/Track.java
  - src/main/java/com/atstudio/atstudio/repository/spec/TrackSpecification.java
  - src/main/java/com/atstudio/atstudio/controller/UserSubscriptionController.java
  - src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java
  - src/main/java/com/atstudio/atstudio/service/SubscriptionService.java
  - src/test/java/com/atstudio/atstudio/service/QuestionServiceTest.java
  - src/test/java/com/atstudio/atstudio/controller/UserSubscriptionControllerTest.java
  - src/test/java/com/atstudio/atstudio/service/UserSubscriptionServiceTest.java
  - src/test/java/com/atstudio/atstudio/service/SubscriptionServiceTest.java

[OUTPUT CONTRACT]
User-facing → deliverables/user/WI-20260228-ATS-006-summary.md :
  - 최종 판정 (PASS / CONDITIONAL PASS / FAIL)
  - 파일별 ✅/⚠️/❌ 판정표
  - 발견 이슈 목록 (있을 경우)

Agent-facing → deliverables/agent/WI-20260228-ATS-006-evidence-pack.md :
  - 파일별 상세 리뷰 (파일:라인 포인터 포함)
  - cascade 삭제 순서 검증 결과
  - N+1 위험 검토 결과

[TRACEABILITY REQUIREMENTS]
Evidence: 이슈 발견 시 파일명·라인번호 필수
Rollback: Read-only → 불필요
