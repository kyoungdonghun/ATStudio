[WI HEADER]
WI ID: WI-20260303-ATS-001
REQ: REQ-20260303-ATS-001
Agent: se
Depends On: -
Blocks: -

[WI SUMMARY]
Why: cr 리뷰(WI-014/WI-015)에서 발견된 MINOR 테스트 갭 4건 추가 + OAuth2 POST body URL 인코딩 개선
Scope (in):
  - DownloadServiceTest.java — downloadPerDay=0 경계값 테스트 추가 (CR-M-3)
  - OAuth2ServiceTest.java — Kakao profile=null 케이스 테스트 추가 (CR-M-4)
  - CompanyCertificationTest.java — APPROVED 상태 무효전이 3건(APPROVED→APPROVED/REVISION_REQUESTED/REJECTED) 추가 (CR-M-001)
  - CompanyCertificationTest.java — REJECTED 상태 무효전이 3건(REJECTED→APPROVED/REVISION_REQUESTED/REJECTED) 추가 (CR-M-002)
  - OAuth2Service.java — POST body raw string → UriComponentsBuilder 또는 MultiValueMap+RestTemplate 방식으로 개선 (CR-S-1)
Scope (out):
  - CR-M-1(findByEmail isDeleted 필터) — 비즈니스 결정 보류
  - 새로운 기능 추가
  - 기존 비즈니스 로직 변경
DoD:
  - 4개 테스트 케이스 추가 완료
  - OAuth2Service POST body URL 인코딩 개선 완료
  - ./gradlew test 전체 통과 (0 failures)
Constraints/Forbidden:
  - 기존 테스트 삭제/수정 금지 (추가만)
  - OAuth2 외부 API 동작 변경 금지 (인코딩 방식만 변경)
  - 기존 패턴 준수 (JUnit5 + Mockito, @ExtendWith(MockitoExtension.class))

[ACCEPTANCE CRITERIA]
Functional:
- [ ] DownloadServiceTest: downloadPerDay=0일 때 다운로드 차단됨 (조건: 0 != -1 && 0 >= 0 → true → DOWNLOAD_LIMIT_EXCEEDED 예외)
- [ ] OAuth2ServiceTest: Kakao profile=null 시 SOCIAL_AUTH_FAILED 예외 발생
- [ ] CompanyCertificationTest: APPROVED→APPROVED 무효전이 → InvalidStatusTransitionException
- [ ] CompanyCertificationTest: APPROVED→REVISION_REQUESTED 무효전이 → InvalidStatusTransitionException
- [ ] CompanyCertificationTest: APPROVED→REJECTED 무효전이 → InvalidStatusTransitionException
- [ ] CompanyCertificationTest: REJECTED→APPROVED 무효전이 → InvalidStatusTransitionException
- [ ] CompanyCertificationTest: REJECTED→REVISION_REQUESTED 무효전이 → InvalidStatusTransitionException
- [ ] CompanyCertificationTest: REJECTED→REJECTED 무효전이 → InvalidStatusTransitionException
- [ ] OAuth2Service: Google/Kakao/Naver POST body가 URL 인코딩 방식으로 구성됨
Quality:
- [ ] ./gradlew test 전체 통과 (0 failures)
- [ ] 빌드 오류 없음

[INPUT POINTERS]
Tier 0 (Constitution):
- docs/standards/core-principles.md

Tier 0 (Standards):
- docs/standards/development-standards.md

Tier 1 (Policies):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

REQ/Context Docs:
- deliverables/user/REQ-20260303-ATS-001.md

CR Evidence (MINOR 발견 출처):
- deliverables/agent/WI-20260302-ATS-014-evidence-pack.md (CR-M-3: DownloadServiceTest downloadPerDay=0, CR-M-4: Kakao profile=null, CR-S-1: OAuth2 POST body)
- deliverables/agent/WI-20260302-ATS-015-evidence-pack.md (CR-M-001: APPROVED 무효전이, CR-M-002: REJECTED 무효전이)

Files (수정 대상):
- src/test/java/com/atstudio/atstudio/service/DownloadServiceTest.java
- src/test/java/com/atstudio/atstudio/service/auth/OAuth2ServiceTest.java
- src/test/java/com/atstudio/atstudio/entity/CompanyCertificationTest.java
- src/main/java/com/atstudio/atstudio/service/auth/OAuth2Service.java:122-126,140-144,158-162

Files (참조 — 수정 금지):
- src/main/java/com/atstudio/atstudio/service/DownloadService.java (L48 조건 확인)
- src/main/java/com/atstudio/atstudio/entity/CompanyCertification.java (L53-64 validateTransition 로직)
- src/test/java/com/atstudio/atstudio/entity/QuestionTest.java (기존 패턴 참조)

Repro/Logs:
- ./gradlew test --tests "com.atstudio.atstudio.service.DownloadServiceTest"
- ./gradlew test --tests "com.atstudio.atstudio.service.auth.OAuth2ServiceTest"
- ./gradlew test --tests "com.atstudio.atstudio.entity.CompanyCertificationTest"
- ./gradlew test

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260303-ATS-001-summary.md :
- 변경 요약, 테스트 결과, 불일치 항목
Agent-facing -> deliverables/agent/WI-20260303-ATS-001-evidence-pack.md :
- 변경된 파일:라인, 추가된 테스트 메서드명, 테스트 실행 결과 (통과/전체)
Handoff Packet -> deliverables/agent/WI-20260303-ATS-001-handoff.md :
- 이 파일 (추적용)

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 변경된 파일명과 라인 번호 명시 필수
Tests: ./gradlew test 실행 결과 (passed/failed 수) 포함
Rollback: git revert 또는 테스트 메서드 삭제, OAuth2Service 원본 복원
