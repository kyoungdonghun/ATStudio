[WI HEADER]
WI ID: WI-20260302-ATS-014
REQ: REQ-20260302-ATS-012
Agent: cr
Depends On: WI-20260302-ATS-013
Blocks: -

[WI SUMMARY]
Why: Phase 3 코드 리뷰 — Download/User/Auth/Error 수정 범위 (WI-007~009) 검토
Why: C-1(무제한 플랜 가드), C-2(PII 탈퇴 계정), M-1(@Transactional), M-2(HTTP 409), M-9(OAuth2 null guard), M-11(valueOf try-catch) 수정의 정확성과 보안성 검토
Scope (in):
  - DownloadService.java — downloadPerDay=-1 가드 로직, @Transactional
  - UserRepository.java — JPQL isDeleted=false 조건
  - OAuth2Service.java — null guard 구현 방식
  - BUSINESS_ERROR.java — RESOURCE_DUPLICATE 409, INVALID_STATE_TRANSITION 추가
  - CompanyCertificationService.java — valueOf try-catch
  - 관련 테스트 파일: DownloadServiceTest, OAuth2ServiceTest, UserServiceTest
Scope (out): N+1/Cascade/상태전이 수정 (WI-015 담당)
DoD:
  - CRITICAL/MAJOR 이슈 없음
  - 수정 로직 정확성 확인
  - 보안 관점 검토 (PII, OAuth2)
  - 테스트 충분성 검토
Constraints/Forbidden:
  - 코드 직접 수정 금지
  - 리뷰 결과를 evidence-pack에 기록

[ACCEPTANCE CRITERIA]
Functional:
- [ ] downloadPerDay=-1 가드 로직 정확성 (경계값 포함)
- [ ] JPQL isDeleted=false 조건 — 탈퇴 계정 완전 배제 확인
- [ ] OAuth2 null guard — 모든 provider(Google/Kakao/Naver) 커버리지 확인
- [ ] RESOURCE_DUPLICATE 409 — api-spec 정합성 확인
- [ ] valueOf try-catch — IllegalArgumentException → INVALID_ARGUMENT(400) 정확성
Quality:
- [ ] CRITICAL 이슈 없음
- [ ] MAJOR 이슈 없음 (또는 문서화됨)

[INPUT POINTERS]
Tier 0 (Constitution):
- docs/standards/core-principles.md

Tier 0 (Standards):
- docs/standards/development-standards.md

Tier 1 (Security — PII/Auth 관련):
- docs/policies/security-policy.md

REQ:
- deliverables/user/REQ-20260302-ATS-012.md

Evidence Packs (리뷰 대상):
- deliverables/agent/WI-20260302-ATS-007-evidence-pack.md
- deliverables/agent/WI-20260302-ATS-008-evidence-pack.md
- deliverables/agent/WI-20260302-ATS-009-evidence-pack.md

Files (리뷰 대상 소스):
- src/main/java/com/atstudio/atstudio/service/DownloadService.java
- src/main/java/com/atstudio/atstudio/repository/UserRepository.java
- src/main/java/com/atstudio/atstudio/service/auth/OAuth2Service.java
- src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java
- src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java
- src/test/java/com/atstudio/atstudio/service/DownloadServiceTest.java
- src/test/java/com/atstudio/atstudio/service/auth/OAuth2ServiceTest.java
- src/test/java/com/atstudio/atstudio/service/UserServiceTest.java

[OUTPUT CONTRACT]
User-facing → deliverables/user/WI-20260302-ATS-014-summary.md
Agent-facing → deliverables/agent/WI-20260302-ATS-014-evidence-pack.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 리뷰 소견 파일:라인 명시
Tests: N/A (검증은 WI-013에서 완료)
Rollback: N/A
