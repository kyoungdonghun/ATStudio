[WI HEADER]
WI ID: WI-20260221-ATS-001
REQ: REQ-20260220-ATS-001
Agent: re
Depends On: WI-20260220-ATS-010 (cr API review — completed)
Blocks: -

[WI SUMMARY]
Why: Auth 시스템 구현(WI-003~005)에 대한 테스트 케이스가 없어 비즈니스 로직 동작이 실제로 검증되지 않은 상태.
      특히 JWT 토큰 검증, Refresh Token rotation, 탈퇴/만료 계정 차단 등 보안 핵심 로직에 대한 unit test가 필요.

Scope (in):
  - JwtTokenProvider 단위 테스트 (순수 Java, Spring 컨텍스트 불필요)
  - AuthService 단위 테스트 (Mockito 모킹)
  - UserService 단위 테스트 (Mockito 모킹)
  - SecurityFilterChain 슬라이스 테스트 (@WebMvcTest)

Scope (out):
  - OAuth2Service 외부 HTTP 호출 테스트 (MockServer 셋업 비용 과다)
  - E2E 통합 테스트
  - 기존 131개 테스트 수정

DoD:
  - 새 테스트 케이스 최소 20개 추가
  - `gradlew.bat test` BUILD SUCCESSFUL (기존 131 + 신규 전부 통과)
  - 각 테스트 클래스가 AAA(Arrange-Act-Assert) 패턴 준수

Constraints/Forbidden:
  - 기존 테스트 코드 수정 금지
  - 프로덕션 코드 수정 금지 (테스트 작성 전용 WI)
  - H2 in-memory DB 활용 (@DataJpaTest 필요 시)
  - 외부 네트워크 호출 금지 (MockServer/WireMock 없음)

[ACCEPTANCE CRITERIA]
Functional:
- [ ] JwtTokenProvider: generateAccessToken → sub/role claim 검증
- [ ] JwtTokenProvider: validateToken → VALID 토큰 반환 VALID
- [ ] JwtTokenProvider: validateToken → 만료 토큰 반환 EXPIRED (예외 아님)
- [ ] JwtTokenProvider: validateToken → 변조 토큰 반환 INVALID
- [ ] JwtTokenProvider: getUserIDAllowExpired → 만료 토큰에서 userID 정상 추출
- [ ] AuthService.login(): 올바른 자격증명 → AuthResponse 반환, DB에 hashed refreshToken 저장
- [ ] AuthService.login(): 잘못된 비밀번호 → BadCredentialsException 발생
- [ ] AuthService.refresh(): VALID 토큰 + DB 해시 일치 → 토큰 rotation 성공
- [ ] AuthService.refresh(): EXPIRED 토큰 + DB 해시 일치 → 토큰 rotation 성공 (getUserIDAllowExpired 경로)
- [ ] AuthService.refresh(): DB 해시 불일치 → clearRefreshToken() 호출 + REFRESH_TOKEN_INVALID 예외
- [ ] AuthService.refresh(): 탈퇴 계정(isDeleted=true) → ACCOUNT_DEACTIVATED 예외
- [ ] UserService.register(): 이메일 중복 → EMAIL_ALREADY_REGISTERED 예외
- [ ] UserService.register(): 닉네임 중복 → NICKNAME_DUPLICATED 예외
- [ ] UserService.completeProfile(): 이미 완성된 프로필(isProfileComplete=true) → PROFILE_ALREADY_COMPLETE 예외
- [ ] SecurityFilterChain: /api/users/me 토큰 없이 → 401
- [ ] SecurityFilterChain: /api/auth/login 토큰 없이 → 200 (PUBLIC 엔드포인트)
- [ ] SecurityFilterChain: 만료 토큰 → 401 + X-Token-Expired: true 헤더

Quality:
- [ ] `gradlew.bat test` BUILD SUCCESSFUL
- [ ] 기존 131개 테스트 non-regression
- [ ] 테스트 클래스명: `XxxTest.java` (ProductionClass 기준)
- [ ] 패키지: `src/test/java/com/atstudio/atstudio/` 하위 (security/, service/auth/, service/, controller/ 등)

[INPUT POINTERS]
Tier 0 (Constitution):
- docs/standards/core-principles.md

Tier 1 (Policies - testing + security 키워드 매칭):
- docs/policies/quality-gates.md
- docs/policies/security-policy.md

REQ/Context Docs:
- deliverables/user/REQ-20260220-ATS-001.md
- deliverables/agent/WI-20260220-ATS-002-evidence-pack.md  ← pg SEC-01~SEC-20 체크리스트
- deliverables/user/WI-20260220-ATS-009-summary.md         ← cr 보안 리뷰 결과
- deliverables/user/WI-20260220-ATS-010-summary.md         ← cr API 로직 리뷰 결과

Target Files (테스트 대상):
- src/main/java/com/atstudio/atstudio/security/JwtTokenProvider.java
- src/main/java/com/atstudio/atstudio/security/JwtAuthenticationFilter.java
- src/main/java/com/atstudio/atstudio/security/CustomUserDetails.java
- src/main/java/com/atstudio/atstudio/security/TokenValidationResult.java
- src/main/java/com/atstudio/atstudio/service/auth/AuthService.java
- src/main/java/com/atstudio/atstudio/service/UserService.java
- src/main/java/com/atstudio/atstudio/config/SecurityConfig.java
- src/main/java/com/atstudio/atstudio/config/JwtConfig.java
- src/main/java/com/atstudio/atstudio/entity/User.java

Existing Test Examples (패턴 참고):
- src/test/java/com/atstudio/atstudio/repository/UserRepositoryTest.java
- src/test/java/com/atstudio/atstudio/common/exception/GlobalExceptionHandlerTest.java
- src/test/resources/application.yml  ← jwt/cors/oauth2 test 설정 있음

Repro:
- gradlew.bat test
- gradlew.bat test --tests "com.atstudio.atstudio.security.*"

[OUTPUT CONTRACT]
User-facing  -> deliverables/user/WI-20260221-ATS-001-summary.md
  - 추가된 테스트 케이스 목록, 통과 결과, 발견된 버그
Agent-facing -> deliverables/agent/WI-20260221-ATS-001-evidence-pack.md
  - 생성된 테스트 파일 경로, `gradlew.bat test` 전체 출력, non-regression 확인
Handoff Packet -> deliverables/agent/WI-20260221-ATS-001-handoff.md (이 파일)

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 생성된 각 테스트 파일 경로 + 케이스 수 명시
Tests: `gradlew.bat test` 전체 출력 첨부 (총 N/N PASS)
Rollback: 프로덕션 코드 미수정이므로 롤백 불필요
