[WI HEADER]
WI ID: WI-20260228-ATS-005
REQ: REQ-20260228-ATS-010
Agent: cr
Depends On: WI-20260228-ATS-004
Blocks: -

[WI SUMMARY]
Why: WI-001 수정 내용(Security/Auth/User 레이어) 코드 리뷰.
     CR-P-001 SecurityConfig 규칙 순서, CR-C-002 @Transactional, CR-C-003 비밀번호 검증이
     올바르게 구현되었는지 독립 검증. 보안 관련 수정이므로 security-policy 준수 여부 중점 검토.
Scope (in):
  - SecurityConfig.java: /api/users/me 명시 규칙 순서 검토 (CR-P-001)
  - AuthService.java, OAuth2Service.java: @Transactional(readOnly=true) 적용 검토 (CR-C-002)
  - UserService.java: updatePassword() BCrypt 검증 로직 검토 (CR-C-003)
  - User.java: updatePassword() 도메인 메서드 검토
  - UpdatePasswordRequest.java: DTO 검토
  - UserController.java: PUT /me/password 엔드포인트 검토
  - SecurityFilterChainTest.java, UserServiceTest.java: 신규 테스트 품질 검토
Scope (out):
  - 다른 WI 범위 파일 (Question, Track, Subscription)
  - 코드 수정 (Read-only 리뷰만)
DoD:
  - 각 파일 PASS/FAIL/SUGGESTION 판정
  - FAIL 항목 파일:라인 포인터 포함
  - 보안 관련 항목 특히 상세 검토
Constraints/Forbidden: 코드 수정 금지. Read-only 리뷰만.

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] SecurityConfig: /api/users/me authenticated() 규칙이 /api/users/* ADMIN 앞에 존재 확인
  - [ ] AuthService/OAuth2Service: 클래스 레벨 @Transactional(readOnly=true) + mutating 메서드 override 확인
  - [ ] updatePassword(): BCrypt.matches() 현재 비밀번호 검증 로직 올바름 확인
  - [ ] UpdatePasswordRequest DTO: currentPassword, newPassword 필드 + 검증 어노테이션
  - [ ] PUT /api/users/me/password: 204 No Content 응답 확인
Quality:
  - [ ] 신규 테스트가 실제 시나리오를 충분히 커버하는지 확인
  - [ ] 보안 취약점 없음 (비밀번호 평문 노출, 에러 메시지 정보 노출 등)

[INPUT POINTERS]
Tier 0 (Constitution):
  - docs/standards/core-principles.md

Tier 0 (Standards — cr):
  - docs/standards/development-standards.md

Tier 1 (Policies — 보안 수정 리뷰):
  - docs/policies/security-policy.md
  - docs/policies/access-control-policy.md

REQ:
  - deliverables/user/REQ-20260228-ATS-010.md

WI-001 수정 결과:
  - deliverables/user/WI-20260228-ATS-001-summary.md
  - deliverables/agent/WI-20260228-ATS-001-evidence-pack.md

회귀 검증 결과:
  - deliverables/user/WI-20260228-ATS-004-summary.md

리뷰 대상 파일:
  - src/main/java/com/atstudio/atstudio/config/SecurityConfig.java
  - src/main/java/com/atstudio/atstudio/service/auth/AuthService.java
  - src/main/java/com/atstudio/atstudio/service/auth/OAuth2Service.java
  - src/main/java/com/atstudio/atstudio/service/user/UserService.java
  - src/main/java/com/atstudio/atstudio/entity/User.java
  - src/main/java/com/atstudio/atstudio/dto/user/UpdatePasswordRequest.java
  - src/main/java/com/atstudio/atstudio/controller/UserController.java
  - src/test/java/com/atstudio/atstudio/controller/SecurityFilterChainTest.java
  - src/test/java/com/atstudio/atstudio/service/UserServiceTest.java

[OUTPUT CONTRACT]
User-facing → deliverables/user/WI-20260228-ATS-005-summary.md :
  - 최종 판정 (PASS / CONDITIONAL PASS / FAIL)
  - 파일별 ✅/⚠️/❌ 판정표
  - 발견 이슈 목록 (있을 경우)

Agent-facing → deliverables/agent/WI-20260228-ATS-005-evidence-pack.md :
  - 파일별 상세 리뷰 (파일:라인 포인터 포함)
  - 보안 체크리스트 결과

[TRACEABILITY REQUIREMENTS]
Evidence: 이슈 발견 시 파일명·라인번호 필수
Rollback: Read-only → 불필요
