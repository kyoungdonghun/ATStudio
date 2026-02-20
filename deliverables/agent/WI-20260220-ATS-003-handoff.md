[WI HEADER]
WI ID: WI-20260220-ATS-003
REQ: REQ-20260220-ATS-001
Agent: se (Software Engineer)
Depends On: WI-20260220-ATS-001 (sa), WI-20260220-ATS-002 (pg)
Blocks: WI-20260220-ATS-004 (se), WI-20260220-ATS-005 (se)

[WI SUMMARY]
Why: Auth 시스템의 뼈대(인프라)를 구현한다. SecurityConfig, JwtTokenProvider, JwtFilter,
     CustomUserDetailsService 등을 먼저 완성해야 후속 WI(로그인, 소셜 로그인)가
     이 인프라 위에서 기능을 구현할 수 있다.

Scope:
  In:
    1. SecurityConfig.java — Spring Security 6 SecurityFilterChain 빈
    2. JwtTokenProvider.java — JWT 생성/검증/파싱 (JJWT 0.12.5)
    3. JwtAuthenticationFilter.java — OncePerRequestFilter 기반 JWT 필터
    4. CustomUserDetailsService.java — UserDetailsService 구현체
    5. CustomUserDetails.java — UserDetails 구현체
    6. TokenDto.java (또는 record) — Access/Refresh Token 응답 DTO
    7. application.yml에 JWT 설정 추가 (jwt.secret, jwt.access-token-expiration, jwt.refresh-token-expiration)
    8. User 엔티티에 refresh_token 필드 추가 + users 테이블 DDL 업데이트
    9. UserRepository에 findByRefreshToken 메서드 추가 (Refresh Token DB 조회용)
    10. GlobalExceptionHandler에 Auth 에러 처리 추가 (AuthenticationException, AccessDeniedException)
    11. 관련 ErrorCode enum 항목 추가 (없으면 생성)
    12. AuthController 기본 뼈대 (빈 메서드, 나중에 WI-004/005에서 채움)
  Out:
    - 회원가입/로그인 로직 구현 (WI-004 담당)
    - 소셜 로그인 로직 구현 (WI-005 담당)
    - 회원 정보 관리 API 구현 (WI-006 담당)
    - 테스트 작성 (WI-008 담당)

DoD:
  - `gradlew.bat compileJava` 성공 (컴파일 오류 없음)
  - JwtTokenProvider가 토큰 생성/파싱/검증을 수행
  - JwtAuthenticationFilter가 SecurityFilterChain에 등록됨
  - CustomUserDetailsService가 UserRepository로 사용자 조회
  - User 엔티티에 refresh_token 필드 추가됨
  - schema.sql users 테이블에 refresh_token 컬럼 추가됨
  - ErrorCode enum에 Auth 에러 코드 추가됨

Constraints/Forbidden:
  - Spring Security 5.x WebSecurityConfigurerAdapter 상속 금지
  - HttpSession 사용 금지 → SessionCreationPolicy.STATELESS
  - JWT Secret Key 하드코딩 금지 → application.yml의 ${JWT_SECRET} 환경변수 또는 yml 값 사용
  - Entity를 Controller에서 직접 반환 금지 (DTO 사용)
  - @Transactional은 Service 레이어에만 적용
  - JJWT 0.12.5 API 사용 (0.11.x 이하 deprecated API 금지)
    → 0.12.x: Jwts.builder().signWith(key, algorithm) 방식
    → Keys.hmacShaKeyFor(secret.getBytes()) 사용

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] SecurityConfig: PUBLIC/auth/ADMIN 엔드포인트 접근 규칙 적용
  - [ ] JwtTokenProvider: generateAccessToken(), generateRefreshToken(), validateToken(), getClaims() 구현
  - [ ] JwtAuthenticationFilter: Authorization 헤더에서 Bearer 토큰 추출 → 검증 → SecurityContext 세팅
  - [ ] CustomUserDetailsService: loadUserByUsername(email) 구현
  - [ ] User 엔티티: refreshToken 필드 추가 (String, nullable)
  - [ ] schema.sql: users 테이블에 refresh_token VARCHAR(512) NULL 컬럼 추가
  - [ ] ErrorCode: INVALID_TOKEN, EXPIRED_TOKEN, UNAUTHORIZED, FORBIDDEN, TOKEN_NOT_FOUND 등 추가

Quality:
  - [ ] `gradlew.bat compileJava` 성공
  - [ ] Lombok, Java 17 문법 활용 (record for DTO 등)
  - [ ] 코드에 Javadoc 불필요 (로직이 자명한 경우)

[INPUT POINTERS]
Tier 0 (Constitution - Required):
  - docs/standards/core-principles.md
  - docs/standards/development-standards.md

Tier 1 (Policies - security task):
  - docs/policies/security-policy.md

Tier 2 (Architecture Design - 반드시 숙지):
  - deliverables/agent/WI-20260220-ATS-001-evidence-pack.md  ← sa 설계 문서 (구현 기준)
  - deliverables/agent/WI-20260220-ATS-002-evidence-pack.md  ← pg 보안 검토 결과 (반영 필수)
  - deliverables/user/REQ-20260220-ATS-001.md
  - docs/standards/dto-standards.md
  - docs/standards/exception-handling.md

Files (수정/참조 대상):
  - src/main/java/com/atstudio/atstudio/entity/User.java             ← refresh_token 필드 추가
  - src/main/java/com/atstudio/atstudio/repository/UserRepository.java ← 메서드 추가
  - src/main/resources/application.yml                               ← JWT 설정 추가
  - src/main/resources/schema.sql                                    ← DDL 수정
  - build.gradle                                                      ← JJWT 버전 확인

[OUTPUT CONTRACT]
User-facing → deliverables/user/WI-20260220-ATS-003-summary.md:
  - 생성/수정된 파일 목록
  - 컴파일 성공 확인

Agent-facing → deliverables/agent/WI-20260220-ATS-003-evidence-pack.md:
  - 파일별 변경 내역 및 경로
  - 컴파일 명령 및 결과
  - 후속 WI(004, 005)를 위한 인수인계 포인트

Handoff Packet → deliverables/agent/WI-20260220-ATS-003-handoff.md:
  - 이 파일 (추적성용)

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 생성/수정 파일 전체 목록 + 경로
Tests: WI-008에서 처리 (현재는 컴파일 확인만)
Rollback: git diff로 변경사항 확인 가능
