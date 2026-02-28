[WI HEADER]
WI ID: WI-20260228-ATS-003
REQ: REQ-20260228-ATS-010
Agent: se
Depends On: -
Blocks: WI-20260228-ATS-004

[WI SUMMARY]
Why: Track 도메인 CRITICAL 1건 + Subscription 도메인 MAJOR 4건 수정.
     CR-A-001 — Track 엔티티에 trackTags 필드 없어 TrackSpecification.join("trackTags")가
                 런타임 IllegalArgumentException. 태그 기반 트랙 검색 전체 불가.
     CR-B-001/002 — 구독 취소(관리자/본인) DELETE → ResponseEntity.ok() 200 반환. 명세: 204 No Content.
     CR-B-003 — 구독 다운그레이드 시 proratedAmount.abs()로 음수 환불액이 양수 청구로 전환되는 계산 버그.
     CR-B-004 — UserType.valueOf(userType) IllegalArgumentException 미처리 → 잘못된 값 시 500.
Scope (in):
  - Track.java: @OneToMany(mappedBy="track", fetch=LAZY) List<TrackTag> trackTags 필드 추가
  - TrackSpecification.java: join("trackTags") 정상 동작 확인 (수정 불필요할 수 있음)
  - UserSubscriptionController.java: adminCancelSubscription(), selfCancelSubscription() → 204 응답
  - UserSubscriptionService.java: changeSubscription() proratedAmount.abs() 제거
  - SubscriptionService.java: UserType.valueOf() → try/catch → INVALID_USER_TYPE 에러 or 기존 BUSINESS_ERROR 활용
  - 관련 단위 테스트 추가/수정
Scope (out):
  - Track 태그 필터 N+1 쿼리 최적화 (CR-A-002): REQ-2 범위
  - 구독 관련 다른 비즈니스 로직 변경
  - DB 스키마 변경
  - 다른 WI 범위 파일 수정
DoD:
  - TrackSpecification.join("trackTags") 실행 시 IllegalArgumentException 없음
  - DELETE /api/user-subscriptions/{id}, DELETE /api/user-subscriptions/me → 204 No Content
  - 다운그레이드 시 proratedAmount 음수 그대로 processPayment 전달
  - 잘못된 userType 문자열 입력 시 500 아닌 400 BAD_REQUEST 응답
  - 기존 테스트 포함 0 failures
Constraints/Forbidden:
  - Track.java에 trackTags 추가 시 반드시 fetch=LAZY 명시 (N+1 방지)
  - DB 스키마 변경 금지
  - proratedAmount 계산 자체 로직(잔여일/총일) 변경 금지 (abs()만 제거)
  - 다른 WI 범위 파일 수정 금지

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] Track.java: @OneToMany(mappedBy="track", fetch=LAZY) List<TrackTag> trackTags 필드 존재
  - [ ] TrackSpecification: 태그 필터 있는 Specification 생성 시 예외 없음
  - [ ] DELETE /api/user-subscriptions/{id} → HTTP 204 No Content
  - [ ] DELETE /api/user-subscriptions/me → HTTP 204 No Content
  - [ ] changeSubscription() 다운그레이드 시: proratedAmount 음수 → processPayment에 음수 전달
  - [ ] SubscriptionService.getAvailablePlans(userType): 잘못된 userType → 400 BAD_REQUEST (500 아님)
Quality:
  - [ ] UserSubscriptionControllerTest: DELETE 204 응답 확인 테스트
  - [ ] UserSubscriptionServiceTest: 다운그레이드 시 음수 amount 검증 테스트
  - [ ] SubscriptionServiceTest: 잘못된 userType → 400 검증 테스트
  - [ ] 기존 테스트 전체 통과 (no regressions)

[INPUT POINTERS]
Tier 0 (Constitution):
  - docs/standards/core-principles.md

Tier 0 (Standards — se):
  - docs/standards/development-standards.md

REQ:
  - deliverables/user/REQ-20260228-ATS-010.md

감사 근거 (이슈 출처):
  - docs/audit/backend-audit-report.md  ← CR-A-001 (CRITICAL), CR-B-001~004 (MAJOR)
  - deliverables/user/WI-20260227-ATS-029-summary.md  ← CR-A-001 상세 (cr-A 발견)
  - deliverables/user/WI-20260227-ATS-030-summary.md  ← CR-B-001~004 상세 (cr-B 발견)

수정 대상 파일:
  - src/main/java/com/atstudio/atstudio/entity/Track.java                         ← CR-A-001 주 수정
  - src/main/java/com/atstudio/atstudio/repository/spec/TrackSpecification.java   ← CR-A-001 확인
  - src/main/java/com/atstudio/atstudio/controller/UserSubscriptionController.java ← CR-B-001/002
  - src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java      ← CR-B-003 (line 176)
  - src/main/java/com/atstudio/atstudio/service/SubscriptionService.java          ← CR-B-004 (line 25)
  - src/test/java/com/atstudio/atstudio/controller/UserSubscriptionControllerTest.java
  - src/test/java/com/atstudio/atstudio/service/UserSubscriptionServiceTest.java
  - src/test/java/com/atstudio/atstudio/service/SubscriptionServiceTest.java

연관 엔티티 참조:
  - src/main/java/com/atstudio/atstudio/entity/TrackTag.java  ← mappedBy 방향 확인
  - docs/design/db-schema.md  ← track_tags 테이블 FK 정의

[OUTPUT CONTRACT]
User-facing → deliverables/user/WI-20260228-ATS-003-summary.md :
  - 수정 완료 확인, CR-A-001·B-001~004 해결 여부, 테스트 결과

Agent-facing → deliverables/agent/WI-20260228-ATS-003-evidence-pack.md :
  - 수정된 파일:라인 목록
  - Track.java trackTags 필드 추가 스니펫
  - DELETE 204 변경 전/후 스니펫
  - proratedAmount.abs() 제거 전/후 스니펫
  - UserType.valueOf() try/catch 처리 스니펫
  - `./gradlew test` 결과 요약

[TRACEABILITY REQUIREMENTS]
Evidence: 수정 파일명·라인번호 필수. 각 이슈별 변경 전후 코드 스니펫 포함.
Tests: 신규 테스트 케이스 메서드명 + 핵심 assert 내용 포함
Rollback: git revert
