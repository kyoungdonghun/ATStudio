[WI HEADER]
WI ID: WI-20260220-ATS-001
REQ: REQ-20260220-ATS-001
Agent: sa (Software Architect)
Depends On: -
Blocks: WI-20260220-ATS-002 (pg), WI-20260220-ATS-003 (se)

[WI SUMMARY]
Why: Auth 시스템 구현 전 아키텍처를 확정한다. Spring Security 6 필터 체인 구조,
     JWT TTL, Refresh Token DB 저장 방식, 소셜 로그인 OAuth2 플로우를 설계하여
     후속 WI(pg 보안 검토, se 구현)가 일관된 기반 위에서 작업할 수 있도록 한다.

Scope:
  In:
    - Spring Security 6 SecurityFilterChain 빈 구조 설계 (CORS, CSRF, SessionManagement, 엔드포인트 접근 규칙)
    - JWT 구조 설계 (Header/Payload 필드, Signature 알고리즘, Access Token TTL, Refresh Token TTL)
    - Refresh Token DB 저장 전략 (users.refresh_token 컬럼 스펙, null 처리 규칙)
    - OAuth2 소셜 로그인 플로우 설계 (Authorization Code → 백엔드 토큰 교환 → 사용자 생성/조회 → JWT 발급)
    - Auth 관련 패키지 구조 정의 (config/, service/auth/, dto/auth/ 등)
    - JwtFilter 처리 흐름 설계 (OncePerRequestFilter 기반)
    - CustomUserDetailsService 설계
    - GlobalExceptionHandler Auth 에러 코드 목록 (401, 403 케이스)
  Out:
    - 실제 Java 코드 구현 (se WI-003/004/005/006 담당)
    - 보안 검토 (pg WI-002 담당)
    - 테스트 작성 (re WI-008 담당)

DoD:
  - 아키텍처 설계 문서 (ADR 또는 Architecture Decision) 작성 완료
  - SecurityFilterChain 빈 구조 확정 (엔드포인트별 접근 규칙 포함)
  - JWT 페이로드 필드 목록 및 TTL 값 확정
  - Refresh Token 컬럼 스펙 확정 (타입, nullable, 인덱스 여부)
  - 소셜 로그인 3개 provider 플로우 다이어그램 (텍스트 형식 가능)
  - 패키지 구조 트리 정의
  - 후속 WI(pg, se)가 즉시 착수할 수 있는 수준의 명확성 확보

Constraints/Forbidden:
  - 실제 Java 파일 생성/수정 금지 (설계 문서만 작성)
  - Spring Security 5.x 패턴 사용 금지 (WebSecurityConfigurerAdapter 상속 방식 금지)
    → Spring Security 6은 SecurityFilterChain @Bean 방식 사용
  - `HttpSession` 사용 금지 → `SessionCreationPolicy.STATELESS`
  - JWT Secret Key를 설계 문서에 실제 값으로 작성 금지 (${JWT_SECRET} 환경변수 형식 사용)
  - 불필요한 의존성 추가 제안 금지 (build.gradle에 이미 있는 JJWT 0.12.5 사용)

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] SecurityFilterChain 빈 구조가 api-spec.md의 모든 [PUBLIC] / [auth required] / [ADMIN] 엔드포인트 접근 규칙을 커버
  - [ ] JWT 페이로드: userId, email, role 필드 포함 여부 확정
  - [ ] Access Token TTL, Refresh Token TTL 값 명시
  - [ ] users.refresh_token 컬럼 스펙 (VARCHAR 길이, nullable 여부) 명시
  - [ ] Google/Kakao/Naver 소셜 로그인 각각의 Authorization Code Exchange URL 및 User Info URL 포함
  - [ ] isProfileComplete 판단 로직 명시 (첫 소셜 가입 감지 방법)
  - [ ] 로그아웃/탈퇴 시 refresh_token null 처리 흐름 명시

Performance:
  - [ ] JwtFilter는 매 요청마다 실행 → DB 조회 최소화 방안 명시 (토큰 자체에 claims 포함 전략)

Quality:
  - [ ] 설계 문서가 후속 se WI-003에서 구현 가능한 수준의 명확성 보유
  - [ ] 보안 리스크 항목이 pg WI-002에서 검토 가능하도록 명시

[INPUT POINTERS]
Tier 0 (Constitution - Required):
  - docs/standards/core-principles.md

Tier 0 (Standards - sa required):
  - docs/standards/development-standards.md

Tier 1 (Policies - Security/Architecture 키워드 매칭):
  - docs/policies/security-policy.md
  - docs/policies/access-control-policy.md
  - docs/architecture/system-design.md

Tier 2 (REQ / Context Docs):
  - deliverables/user/REQ-20260220-ATS-001.md          ← 승인된 REQ (범위/결정사항)
  - docs/design/api-spec.md                            ← 전체 API 명세 (5.1~5.10, 14.1~14.3)
  - docs/design/db-schema.md                           ← 현재 DB 스키마 (users 테이블 확인)
  - docs/standards/dto-standards.md                    ← DTO 패턴 표준
  - docs/standards/exception-handling.md               ← 에러코드/예외처리 표준

Files (현재 코드베이스 참조):
  - src/main/java/com/atstudio/atstudio/entity/User.java         ← User 엔티티 현황
  - src/main/java/com/atstudio/atstudio/entity/SocialAccount.java ← 소셜 계정 엔티티
  - src/main/resources/application.yml                           ← 현재 설정
  - src/main/resources/schema.sql                               ← 현재 DDL
  - build.gradle                                                 ← 의존성 현황 (JJWT 버전 확인)

[OUTPUT CONTRACT]
User-facing → deliverables/user/WI-20260220-ATS-001-summary.md:
  - 아키텍처 결정 요약 (JWT TTL, Security 구조, 소셜 로그인 플로우)
  - 후속 작업에 미치는 영향 (DB 스키마 변경 필요 항목)
  - 리스크 및 주의사항

Agent-facing → deliverables/agent/WI-20260220-ATS-001-evidence-pack.md:
  - 상세 아키텍처 설계 문서 (설계 결정 + 근거)
  - SecurityFilterChain 구조 (코드 스니펫 형식)
  - JWT 설계 명세 (페이로드, TTL, 알고리즘)
  - users.refresh_token 컬럼 DDL 스니펫
  - 소셜 로그인 플로우 (provider별 설명)
  - 패키지 구조 트리
  - 후속 WI(pg-002, se-003)를 위한 인수인계 포인트

Handoff Packet → deliverables/agent/WI-20260220-ATS-001-handoff.md:
  - 이 파일 (추적성용)

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 모든 설계 결정에 "결정 근거" 명시 필수
Tests: N/A (설계 단계)
Rollback: N/A (파일 생성만, DB 변경 없음)
Follow-up WIs: WI-20260220-ATS-002 (pg), WI-20260220-ATS-003 (se) 즉시 착수 가능 조건 명시
