[WI HEADER]
WI ID: WI-20260302-ATS-008
REQ: REQ-20260302-ATS-012
Agent: se
Depends On: -
Blocks: WI-20260302-ATS-013

[WI SUMMARY]
Why: CRITICAL C-2 — Admin 사용자 목록에 탈퇴 계정(isDeleted=true) 노출 (PII 유출) + M-9 OAuth2 토큰교환 NPE 위험
Scope (in):
  - UserRepository.java:21-23 — JPQL에 AND u.isDeleted = false 조건 추가
  - OAuth2Service.java:129,144,159 — 토큰 교환 응답 null guard + 에러 처리
  - OAuth2Service.java:178,191,205 — userInfo 응답 null guard + 에러 처리
  - UserRepositoryTest.java 또는 UserServiceTest.java — 탈퇴 계정 미노출 테스트 추가
  - OAuth2ServiceTest.java 또는 AuthServiceTest.java — null 응답 처리 테스트 추가
Scope (out): SecurityConfig, UserController 수정 금지
DoD:
  - Admin 사용자 목록 조회 시 isDeleted=true 계정 미포함
  - OAuth2 토큰교환 실패 시 500 대신 적절한 비즈니스 예외 반환
  - BUILD SUCCESSFUL, 0 failures
Constraints/Forbidden:
  - UserRepository, OAuth2Service, 관련 테스트만 수정
  - 다른 파일 수정 금지

[ACCEPTANCE CRITERIA]
Functional:
- [ ] isDeleted=true 사용자가 Admin 목록 API 응답에 포함되지 않음
- [ ] OAuth2 token exchange 응답이 null이면 BusinessException 발생 (500 아님)
- [ ] OAuth2 userInfo 응답이 null이면 BusinessException 발생
- [ ] 정상적인 소셜 로그인 플로우 미영향
Quality:
- [ ] BUILD SUCCESSFUL
- [ ] 신규 테스트 포함 전체 테스트 0 failures

[INPUT POINTERS]
Tier 0 (Constitution):
- docs/standards/core-principles.md

Tier 0 (Standards):
- docs/standards/development-standards.md

Tier 1 (Security — PII/Auth 관련):
- docs/policies/security-policy.md

REQ:
- deliverables/user/REQ-20260302-ATS-012.md

Files:
- src/main/java/com/atstudio/atstudio/repository/UserRepository.java:21-23
- src/main/java/com/atstudio/atstudio/service/auth/OAuth2Service.java:120-215
- src/test/java/com/atstudio/atstudio/service/auth/AuthServiceTest.java

[OUTPUT CONTRACT]
User-facing → deliverables/user/WI-20260302-ATS-008-summary.md
Agent-facing → deliverables/agent/WI-20260302-ATS-008-evidence-pack.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines): 수정된 라인 명시
Tests: gradlew.bat test --tests "*UserRepository*" --tests "*OAuth2*" --tests "*AuthService*"
Rollback: git revert
