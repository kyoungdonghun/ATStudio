# WI-20260227-ATS-032 Summary — pg 크로스컷 보안 검토: SecurityConfig·JWT·ResponseDTO

**검토 범위:** SecurityConfig 79개 API 전수, JWT 보안 흐름, 전 Controller 응답 일관성
**최종 판정:** CONDITIONAL PASS — CRITICAL 2건, MAJOR 2건 수정 후 승인

---

## 판정 통계

| 판정 | 건수 |
|------|------|
| CRITICAL | 2 |
| MAJOR | 2 |
| MINOR | 4 |
| 📋 정보성 | 1 |

---

## CRITICAL (즉시 수정 필수)

### CR-P-001: SecurityConfig `/api/users/*` 와일드카드 — 일반 사용자 프로필 접근 차단
- `SecurityConfig.java:71-73` — `/api/users/*` hasRole("ADMIN") 규칙이 `/api/users/me`에도 매칭
- Spring Security 첫 번째 매칭 규칙 적용 → 일반 USER 역할로 `GET /api/users/me`(5.4), `PUT /api/users/me`(5.7) 호출 시 403 Forbidden
- **WI-029~031 cr 검토 전체에서 발견하지 못한 신규 CRITICAL 이슈**
- **수정**: `/api/users/me` 명시적 `authenticated()` 규칙을 `/api/users/*` 와일드카드 앞에 추가

```java
// 수정 방향 (와일드카드 규칙 앞에 /me 명시)
.requestMatchers(HttpMethod.GET,    "/api/users/me").authenticated()
.requestMatchers(HttpMethod.PUT,    "/api/users/me").authenticated()
.requestMatchers(HttpMethod.DELETE, "/api/users/me").authenticated()
.requestMatchers(HttpMethod.PUT,    "/api/users/me/complete-profile").authenticated()
// 이후 ADMIN 와일드카드
.requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
.requestMatchers(HttpMethod.GET, "/api/users/*").hasRole("ADMIN")
.requestMatchers(HttpMethod.PUT, "/api/users/*").hasRole("ADMIN")
```

### CR-P-004: JWT 시크릿 예측 가능한 기본값 하드코딩
- `application.yml:36` — fallback 값이 `"atstudio-secret-key-for-development-only-2026"` (Base64 디코딩 시)
- `JWT_SECRET` 환경변수 미설정 시 공개 저장소에서 키 탈취 → 임의 JWT 위조 가능 → 완전 권한 탈취
- security-policy.md 6.1절 위반
- **수정**: fallback 값 완전 제거 → `secret: ${JWT_SECRET}` (미설정 시 기동 실패가 올바른 동작)

---

## MAJOR (프론트 전 반드시 수정)

| # | 이슈 | 파일:라인 |
|---|------|----------|
| CR-P-003 | `TestController` — `/test`, `/health` 인증 없이 운영 노출 (CR-C-008 재확인) | `TestController.java:1-18` |
| CR-P-007 | 6.9/6.10 DELETE → `ResponseEntity.ok()` 200 반환 (명세: 204 No Content, CR-B-001/002 재확인) | `UserSubscriptionController.java:97-114` |

---

## MINOR (권장 수정)

| # | 이슈 | 파일 |
|---|------|------|
| CR-P-005 | 만료된 RefreshToken 갱신 허용 → RT JWT 만료시간 사실상 무의미 | `AuthService.java:70-73` |
| CR-P-006 | `TagController.getAllTags()` ResponseDTO 미래핑, raw `List<>` 반환 (CR-A-007 재확인) | `TagController.java:37-40` |
| CR-P-008 | `AccessDeniedException` catch-all 체인 내 처리 (CR-C-014 재확인) | `GlobalExceptionHandler.java:116-118` |
| CR-P-009 | `/api/company-certifications/me` BUSINESS 체크는 서비스 레이어만 — 정보성 | `SecurityConfig.java:83` |

---

## SecurityConfig 전수 검사 결과 (79 APIs)

| 결과 | 건수 |
|------|------|
| ✅ PASS | 75 |
| ❌ FAIL (CR-P-001) | 2 (GET·PUT /api/users/me) |
| ❌ 보안 미조치 (CR-P-003) | 2 (TestController) |

**주요 발견**: user-subscriptions·company-certifications `/me` 패턴은 SecurityConfig에서 와일드카드 앞에 명시적 선언되어 있어 정상. `/api/users/me`만 누락.

---

## 전반적 평가

JWT 라이프사이클(발급/갱신/무효화/BCrypt 해싱), CORS, stateless 세션, 401/403 JSON 응답, 에러 응답 민감정보 미노출 등 보안 아키텍처 기반은 견고함.

가장 심각한 신규 발견: **CR-P-001** — 일반 사용자가 자신의 프로필을 조회/수정할 수 없는 SecurityConfig 버그. 프론트엔드 연동 시 즉시 재현 가능하며, 테스트로 잡히지 않은 이유는 `@WithMockUser`가 MockMvc에서 SecurityConfig를 우회하기 때문.
