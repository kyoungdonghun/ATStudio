# Security Review Summary -- WI-20260220-ATS-002

> **Reviewer**: PG (Privacy Guardian)
> **Date**: 2026-02-20
> **Target**: WI-20260220-ATS-001 (Auth System Architecture Design by SA)
> **Policy Baseline**: security-policy.md, access-control-policy.md, core-principles.md

---

## Overall Assessment

**CONDITIONAL APPROVAL** -- 설계 전반적으로 건전하며, security-policy.md 요구사항 대부분 충족. 단, Critical 1건 및 High 4건을 구현 단계에서 반드시 반영해야 함.

---

## Critical Items (즉시 수정 필요)

### C-1. application.yml에 DB 비밀번호 평문 하드코딩 [CRITICAL]

**현재 상태**: `src/main/resources/application.yml` 라인 16-17에 `username: root`, `password: 1234`가 평문으로 커밋되어 있음.

**정책 위반**: security-policy.md Section 6.2 -- "Production DB credentials must never appear in committed files."

**조치 필요**:
- `application.yml`에서 DB 자격증명을 `${SPRING_DATASOURCE_USERNAME}`, `${SPRING_DATASOURCE_PASSWORD}` 환경변수 참조로 교체
- `application-local.yml` (gitignored)을 로컬 개발 전용으로 생성
- 현재 커밋된 비밀번호는 개발용이라 하더라도 보안 습관 형성 차원에서 즉시 수정

---

## High Priority Items (구현 시 반드시 반영)

### H-1. JWT Access Token 페이로드에 email 포함 [HIGH]

**문제**: Access Token 페이로드에 `email` claim이 포함됨 (Section 2.1). JWT는 Base64 디코딩만으로 페이로드를 읽을 수 있으므로, 이메일 주소가 PII로서 노출됨.

**권고**: Access Token에서 `email` claim 제거. `sub` (userId)와 `role`만 유지. 이메일이 필요하면 서버에서 userId로 DB 조회.

### H-2. OAuth2 소셜 로그인 시 email 기반 자동 계정 연동 -- 계정 탈취 위험 [HIGH]

**문제**: OAuth2Service Section 6.3 Step 4에서, 소셜 로그인 시 동일 email을 가진 로컬 계정이 있으면 자동으로 소셜 계정을 연동함. 공격자가 피해자의 email로 소셜 계정을 생성하면, 피해자의 로컬 계정에 자동 연동되어 계정 탈취가 가능.

**권고**: 자동 연동 대신, 기존 계정이 있으면 해당 계정의 비밀번호 확인(로컬) 또는 기존 소셜 계정으로 재인증 후 연동하는 "명시적 연동 확인" 단계 추가.

### H-3. OAuth2 State 파라미터 CSRF 보호 미설계 [HIGH]

**문제**: OAuth2 플로우 (Section 6.1)에서 `state` 파라미터를 사용한 CSRF 방지가 설계에 포함되어 있지 않음. Authorization Code Grant Flow에서 state 파라미터 없이는 CSRF 공격에 취약.

**권고**: 프론트엔드가 Authorization URL 생성 시 랜덤 `state` 값을 생성하고 세션/메모리에 저장. 콜백 시 state 값 검증 추가.

### H-4. CORS allowedHeaders 와일드카드 [HIGH]

**문제**: CorsConfig에서 `config.setAllowedHeaders(List.of("*"))` 로 모든 헤더를 허용. `allowCredentials(true)`와 와일드카드 헤더 조합은 보안 위험.

**권고**: 필요한 헤더만 명시적으로 나열 -- `Authorization`, `Content-Type`, `Accept`, `Origin`, `X-Requested-With`.

---

## Medium Priority Items (구현 권고)

### M-1. Refresh Token에 BCrypt 사용 -- 성능 고려

**상태**: 설계에서 Refresh Token을 BCrypt로 해싱하여 DB 저장. BCrypt는 의도적으로 느린 알고리즘(strength 10 ~ 100ms).

**평가**: 보안 관점에서는 우수. 그러나 매 토큰 갱신마다 BCrypt encode + matches 호출 발생. 현재 단계에서는 트래픽이 낮으므로 수용 가능.

**참고**: 대규모 트래픽 시 SHA-256(HMAC) 해시로 전환 검토 가능. 현재는 유지.

### M-2. validateToken()에서 ExpiredJwtException 분리 처리 미설계

**문제**: JwtTokenProvider.validateToken()이 모든 JwtException을 동일하게 false로 처리. 만료 토큰과 위변조 토큰을 구분하지 못함.

**권고**: ExpiredJwtException을 별도로 catch하여, 프론트엔드가 "만료 -> 갱신 필요"와 "위변조 -> 즉시 로그아웃" 상황을 구분할 수 있도록 처리.

### M-3. Production CORS Origin 환경변수 미설계

**상태**: Note로 "Production origins must be configured via environment variable"라고 언급되어 있으나 구체적 설계 없음.

**권고**: `${CORS_ALLOWED_ORIGINS}` 환경변수 기반 동적 설정 구현. 구현 시 반드시 반영.

### M-4. Swagger/OpenAPI 엔드포인트 Production 노출

**문제**: `/swagger-ui/**`, `/v3/api-docs/**`가 무조건 permitAll. Production 환경에서 API 문서가 공개됨.

**권고**: Profile 기반 조건부 노출 (`spring.profiles.active` = dev일 때만 허용) 또는 ADMIN 인증 필요.

---

## Approved Items (보안 적합)

| 항목 | 평가 |
|------|------|
| HS256 알고리즘 선택 | 적합. 단일 서버 환경에서 HS256은 성능/단순성 측면에서 최적. RS256은 마이크로서비스 전환 시 재검토. |
| Access Token TTL 1시간 | 적합. 표준 범위(15분~2시간) 내. |
| Refresh Token TTL 14일 | 적합. "Remember Me" 기간으로 합리적. |
| JWT Secret 환경변수 처리 | 적합. `${JWT_SECRET}` 플레이스홀더 사용, security-policy.md 6.1 준수. |
| BCrypt strength 10 | 적합. security-policy.md 6.3 기준(10+) 충족. |
| CSRF 비활성화 (REST API) | 적합. JWT Stateless API에서 CSRF 토큰 불필요. security-policy.md 6.3 준수. |
| SessionCreationPolicy.STATELESS | 적합. JWT 기반 인증과 일치. |
| Refresh Token 로테이션 | 적합. 갱신 시 이전 토큰 무효화 + 새 토큰 발급. 토큰 탈취 피해 최소화. |
| 소프트 삭제 사용자 인증 차단 | 적합. `isDeleted` 체크 후 UsernameNotFoundException 발생. |
| Refresh Token에 최소 claim만 포함 | 적합. sub, iat, exp만 포함. email/role 미포함. |
| 단일 세션 제한 | 적합. 현재 단계에서 수용 가능. 다중 기기 지원은 Phase 2로 연기. |
| VARCHAR(512) for BCrypt hash | 적합. BCrypt 출력 60자, 향후 알고리즘 변경 여유 충분. |

---

## Deferred to Phase 2 (현재 구현 불필요)

| 항목 | 사유 |
|------|------|
| API Rate Limiting | 초기 트래픽 낮음. Spring Boot + Bucket4j 등으로 Phase 2 도입 |
| RS256 전환 | 마이크로서비스 전환 시 재검토 |
| Multi-device 세션 지원 | 별도 refresh_tokens 테이블 필요. 현재 단일 세션으로 충분 |
| IP 기반 이상 탐지 | 로그인 시 IP 기록 및 이상 패턴 탐지는 운영 안정화 후 |
