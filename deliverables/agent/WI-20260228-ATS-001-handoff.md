[WI HEADER]
WI ID: WI-20260228-ATS-001
REQ: REQ-20260228-ATS-010
Agent: se
Depends On: -
Blocks: WI-20260228-ATS-004

[WI SUMMARY]
Why: Security/Auth/User 레이어의 CRITICAL 2건 + MAJOR 1건 수정.
     CR-P-001 — SecurityConfig `/api/users/*` ADMIN 와일드카드가 `/api/users/me`를 먼저 매칭하여
     일반 USER의 프로필 조회·수정·삭제 전체 차단. 프론트 연동 시 즉시 재현.
     CR-C-002 — AuthService/OAuth2Service 클래스 레벨 @Transactional(readOnly=true) 누락.
     CR-C-003 — UserService.updatePassword() 현재 비밀번호 검증 없이 바로 변경 허용.
Scope (in):
  - SecurityConfig.java: /api/users/me 명시 규칙을 ADMIN 와일드카드 앞에 추가
  - AuthService.java: 클래스 레벨 @Transactional(readOnly=true) 추가
  - OAuth2Service.java: 클래스 레벨 @Transactional(readOnly=true) 추가
  - UserService.java: updatePassword() 현재 비밀번호 검증 로직 추가
  - 각 수정 파일에 대한 단위 테스트 추가/수정
Scope (out):
  - CR-P-004 (JWT 시크릿): 이번 수정 제외
  - CR-P-005 (RefreshToken 만료): REQ-2 범위
  - 다른 도메인 코드 수정
DoD:
  - GET/PUT/DELETE /api/users/me — 일반 USER(hasRole("USER"))로 200/204 응답 (403 아님)
  - AuthService, OAuth2Service 클래스 선언부에 @Transactional(readOnly=true) 존재
  - updatePassword() — 현재 비밀번호 불일치 시 예외, 일치 시 정상 변경
  - 관련 단위 테스트 추가 (기존 테스트 포함 0 failures)
Constraints/Forbidden:
  - DB 스키마 변경 금지
  - SecurityConfig 기존 ADMIN 규칙 로직 변경 금지 (순서만 조정)
  - 다른 WI 범위 파일 수정 금지

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] SecurityConfig에 GET/PUT/DELETE /api/users/me → authenticated() 규칙이 /api/users/* ADMIN 와일드카드 앞에 위치
  - [ ] PUT /api/users/me/complete-profile → authenticated() 규칙도 와일드카드 앞에 위치
  - [ ] AuthService 클래스 선언: @Transactional(readOnly=true) 존재
  - [ ] OAuth2Service 클래스 선언: @Transactional(readOnly=true) 존재
  - [ ] UserService.updatePassword(): 현재 비밀번호 BCrypt 검증 → 불일치 시 BUSINESS_ERROR 던짐
  - [ ] UserService.updatePassword(): 검증 통과 시 새 비밀번호 encode 후 저장
Quality:
  - [ ] SecurityConfig 관련 테스트: USER 역할로 /api/users/me GET/PUT → 200 확인
  - [ ] updatePassword() 테스트: 현재 비밀번호 불일치 케이스 추가
  - [ ] 기존 테스트 전체 통과 (no failures, no regressions)

[INPUT POINTERS]
Tier 0 (Constitution):
  - docs/standards/core-principles.md

Tier 0 (Standards — se):
  - docs/standards/development-standards.md

Tier 1 (Policies — 보안/권한 수정):
  - docs/policies/security-policy.md
  - docs/policies/access-control-policy.md

REQ:
  - deliverables/user/REQ-20260228-ATS-010.md

감사 근거 (이슈 출처):
  - docs/audit/backend-audit-report.md  ← CR-P-001 (CRITICAL), CR-C-002 (CRITICAL), CR-C-003 (MAJOR)
  - deliverables/user/WI-20260227-ATS-032-summary.md  ← CR-P-001 상세 (pg 발견)
  - deliverables/user/WI-20260227-ATS-031-summary.md  ← CR-C-002, CR-C-003 상세 (cr-C 발견)

수정 대상 파일:
  - src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:71-73  ← CR-P-001
  - src/main/java/com/atstudio/atstudio/service/auth/AuthService.java      ← CR-C-002
  - src/main/java/com/atstudio/atstudio/service/auth/OAuth2Service.java    ← CR-C-002
  - src/main/java/com/atstudio/atstudio/service/user/UserService.java      ← CR-C-003
  - src/test/java/com/atstudio/atstudio/service/ (관련 테스트 파일들)

API 명세 (권한 기준):
  - docs/design/api-spec.md  ← 5.4 GET /api/users/me (authenticated), 5.7 PUT /api/users/me

[OUTPUT CONTRACT]
User-facing → deliverables/user/WI-20260228-ATS-001-summary.md :
  - 수정 완료 확인, 테스트 통과 여부, CR-P-001·C-002·C-003 해결 여부

Agent-facing → deliverables/agent/WI-20260228-ATS-001-evidence-pack.md :
  - 수정된 파일:라인 목록
  - SecurityConfig 변경 전/후 규칙 순서
  - 추가된 테스트 케이스 목록
  - `./gradlew test` 결과 요약

[TRACEABILITY REQUIREMENTS]
Evidence: 수정 파일명·라인번호 필수. SecurityConfig 변경 전후 스니펫 포함.
Tests: 신규 테스트 메서드명 + 결과 포함
Rollback: git revert 또는 SecurityConfig 규칙 순서 원복
