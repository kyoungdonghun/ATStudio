# WI-20260227-ATS-032 Evidence Pack — pg: SecurityConfig·JWT·ResponseDTO 크로스컷 보안 검토

## 1. SecurityConfig 권한 매핑 전수 테이블 (79 APIs)

### 1.x Track

| API | URL | Spec 권한 | SC 설정 | 판정 | 비고 |
|-----|-----|----------|--------|------|------|
| 1.1 | POST /api/tracks | ADMIN | `hasRole("ADMIN")` (line 74) | ✅ | |
| 1.2 | GET /api/tracks | PUBLIC | `permitAll()` (line 60) | ✅ | |
| 1.3 | GET /api/tracks/{id} | PUBLIC | `permitAll()` (line 61) | ✅ | |
| 1.4 | GET /api/tracks/{id}/stream | PUBLIC | `permitAll()` (line 62) | ✅ | |
| 1.5 | GET /api/tracks/{id}/download | Subscriber | `authenticated()` catch-all (line 97) | ✅ | 구독 체크는 DownloadService SL |
| 1.6 | PUT /api/tracks/{id} | ADMIN | `hasRole("ADMIN")` (line 75) | ✅ | |
| 1.7 | DELETE /api/tracks/{id} | ADMIN | `hasRole("ADMIN")` (line 76) | ✅ | |

### 2.x Tag

| API | URL | Spec 권한 | SC 설정 | 판정 | 비고 |
|-----|-----|----------|--------|------|------|
| 2.1 | POST /api/tags | ADMIN | `hasRole("ADMIN")` (line 77) + @PreAuthorize | ✅ | 이중 게이트 |
| 2.2 | GET /api/tags | PUBLIC | `permitAll()` (line 63) | ✅ | |
| 2.3 | PUT /api/tags/{id} | ADMIN | `hasRole("ADMIN")` (line 78) + @PreAuthorize | ✅ | |
| 2.4 | DELETE /api/tags/{id} | ADMIN | `hasRole("ADMIN")` (line 79) + @PreAuthorize | ✅ | |

### 3.x Playlist

| API | URL | Spec 권한 | SC 설정 | 판정 | 비고 |
|-----|-----|----------|--------|------|------|
| 3.1 | POST /api/playlists | Subscriber | `authenticated()` catch-all | ✅ | SL 구독 체크 |
| 3.2 | GET /api/playlists | Subscriber | `authenticated()` catch-all | ✅ | SL 구독 체크 |
| 3.3 | GET /api/playlists/{id} | Subscriber+Owner | `authenticated()` catch-all | ✅ | SL owner 체크 |
| 3.4 | POST /api/playlists/{id}/tracks | Subscriber+Owner | `authenticated()` catch-all | ✅ | |
| 3.5 | PUT /api/playlists/{id} | Subscriber+Owner | `authenticated()` catch-all | ✅ | |
| 3.6 | PUT /api/playlists/{id}/tracks | Subscriber+Owner | `authenticated()` catch-all | ✅ | |
| 3.7 | DELETE /api/playlists/{id}/tracks/{trackId} | Subscriber+Owner | `authenticated()` catch-all | ✅ | |
| 3.8 | DELETE /api/playlists/{id} | Subscriber+Owner | `authenticated()` catch-all | ✅ | |

### 4.x Play History

| API | URL | Spec 권한 | SC 설정 | 판정 |
|-----|-----|----------|--------|------|
| 4.1 | POST /api/play-histories | Auth | `authenticated()` catch-all | ✅ |
| 4.2 | GET /api/play-histories | Auth | `authenticated()` catch-all | ✅ |
| 4.3 | DELETE /api/play-histories | Auth | `authenticated()` catch-all | ✅ |

### 5.x User + Auth

| API | URL | Spec 권한 | SC 설정 | 판정 | 비고 |
|-----|-----|----------|--------|------|------|
| 5.1 | POST /api/users | PUBLIC | `permitAll()` (line 53) | ✅ | |
| 5.2 | POST /api/auth/login | PUBLIC | `permitAll()` (line 54) | ✅ | |
| 5.3 | POST /api/auth/social/{provider} | PUBLIC | `permitAll()` (line 55) | ✅ | |
| 5.4 | GET /api/users/me | Auth (USER) | `/api/users/*` `hasRole("ADMIN")` (line 72) | ❌ **CR-P-001** | 와일드카드 충돌 |
| 5.5 | GET /api/users | ADMIN | `hasRole("ADMIN")` (line 71) + @PreAuthorize | ✅ | |
| 5.6 | GET /api/users/{userId} | ADMIN | `hasRole("ADMIN")` (line 72) + @PreAuthorize | ✅ | |
| 5.7 | PUT /api/users/me | Auth (USER) | `/api/users/*` `hasRole("ADMIN")` (line 73) | ❌ **CR-P-001** | 와일드카드 충돌 |
| 5.8 | PUT /api/users/{userId} | ADMIN | `hasRole("ADMIN")` (line 73) + @PreAuthorize | ✅ | |
| 5.9 | DELETE /api/users/me | Auth | `authenticated()` catch-all | ✅ | DELETE /api/users/* 규칙 없음 |
| 5.10 | PUT /api/users/me/complete-profile | Auth | `authenticated()` catch-all | ✅ | 4 세그먼트, /* 미매칭 |
| 14.1 | POST /api/auth/refresh | PUBLIC (RT) | `permitAll()` (line 56) | ✅ | SL RT 검증 |

#### CR-P-001 근거 상세

```
SecurityConfig.java:71-73:
  .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")      // line 71
  .requestMatchers(HttpMethod.GET, "/api/users/*").hasRole("ADMIN")   // line 72
  .requestMatchers(HttpMethod.PUT, "/api/users/*").hasRole("ADMIN")   // line 73

Spring Security 6 = 선언 순서 기준 첫 번째 매칭 규칙 적용.
"/api/users/*" (단일 세그먼트 와일드카드) → /api/users/me 매칭 ✅

/api/users/me 에 대한 명시적 authenticated() 규칙 없음.
→ GET /api/users/me: line 72 hasRole("ADMIN") 선적용 → USER 역할 → 403 Forbidden
→ PUT /api/users/me: line 73 hasRole("ADMIN") 선적용 → USER 역할 → 403 Forbidden

비교: /api/user-subscriptions/me 는 line 89-91에 명시적 선언 → 정상
      /api/company-certifications/me 는 line 83에 명시적 선언 → 정상
      /api/users/me 만 명시적 선언 누락 → 버그
```

### 6.x Subscription

| API | URL | Spec 권한 | SC 설정 | 판정 | 비고 |
|-----|-----|----------|--------|------|------|
| 6.1 | GET /api/subscriptions | PUBLIC | `permitAll()` (line 64) | ✅ | |
| 6.2 | GET /api/subscriptions/{id} | PUBLIC | `permitAll()` (line 65) | ✅ | |
| 6.3 | POST /api/user-subscriptions | Auth | `authenticated()` catch-all | ✅ | SL BUSINESS 체크 |
| 6.4 | GET /api/user-subscriptions/me | Auth | `authenticated()` (line 89) | ✅ | /me 명시 선언 |
| 6.5 | GET /api/user-subscriptions | ADMIN | `hasRole("ADMIN")` (line 92) | ✅ | |
| 6.6 | GET /api/user-subscriptions/{id} | ADMIN | `hasRole("ADMIN")` (line 93) | ✅ | |
| 6.7 | PUT /api/user-subscriptions/me | Auth | `authenticated()` (line 90) | ✅ | /me 명시 선언 |
| 6.8 | PUT /api/user-subscriptions/{id} | ADMIN | `hasRole("ADMIN")` (line 94) | ✅ | |
| 6.9 | DELETE /api/user-subscriptions/{id} | ADMIN | `hasRole("ADMIN")` (line 95) | ✅ (권한) / ❌ (상태코드) | CR-P-007 |
| 6.10 | DELETE /api/user-subscriptions/me | Auth | `authenticated()` (line 91) | ✅ (권한) / ❌ (상태코드) | CR-P-007 |

### 7.x License

| API | URL | Spec 권한 | SC 설정 | 판정 | 비고 |
|-----|-----|----------|--------|------|------|
| 7.1 | GET /api/licenses/me | Auth | `authenticated()` catch-all | ✅ | |
| 7.2 | GET /api/users/{userId}/licenses | ADMIN | `authenticated()` catch-all + @PreAuthorize | ✅ | /api/users/123/licenses = 4세그먼트, SC wildcard 미매칭, @PreAuthorize 담당 |
| 7.3 | GET /api/licenses/{licenseId} | Auth+Owner | `authenticated()` catch-all | ✅ | SL owner 체크 |
| 7.4 | GET /api/users/{userId}/licenses/{licenseId} | ADMIN | `authenticated()` catch-all + @PreAuthorize | ✅ | |

### 8.x Inquiry

| API | URL | Spec 권한 | SC 설정 | 판정 | 비고 |
|-----|-----|----------|--------|------|------|
| 8.1 | POST /api/questions | Auth | `authenticated()` catch-all | ✅ | |
| 8.2 | POST /api/questions/{id}/answers | Auth | `authenticated()` catch-all | ✅ | SL 권한 체크 |
| 8.3 | GET /api/questions | Auth | `authenticated()` catch-all | ✅ | SL 가시성 |
| 8.4 | GET /api/questions/{id} | Auth | `authenticated()` catch-all | ✅ | SL 접근 체크 |
| 8.5 | GET /api/questions/{id}/attachments/{attachId} | Auth | `authenticated()` catch-all | ✅ | |
| 8.6 | PUT /api/questions/{id}/status | ADMIN | `hasRole("ADMIN")` (line 87) + @PreAuthorize | ✅ | |
| 8.7 | DELETE /api/questions/{id} | Auth (dual) | `authenticated()` catch-all | ✅ | SL 이중 권한 |

### 9.x Notice

| API | URL | Spec 권한 | SC 설정 | 판정 |
|-----|-----|----------|--------|------|
| 9.1 | POST /api/notices | ADMIN | `hasRole("ADMIN")` (line 80) + @PreAuthorize | ✅ |
| 9.2 | GET /api/notices | PUBLIC | `permitAll()` (line 66) | ✅ |
| 9.3 | GET /api/notices/{id} | PUBLIC | `permitAll()` (line 67) | ✅ |
| 9.4 | PUT /api/notices/{id} | ADMIN | `hasRole("ADMIN")` (line 81) + @PreAuthorize | ✅ |
| 9.5 | DELETE /api/notices/{id} | ADMIN | `hasRole("ADMIN")` (line 82) + @PreAuthorize | ✅ |

### 10.x Likes / 11.x DownloadQueue / 12.x Whitelist

| API | URL | Spec 권한 | SC 설정 | 판정 |
|-----|-----|----------|--------|------|
| 10.1~10.3 | /api/likes/** | Auth | `authenticated()` catch-all | ✅ |
| 11.1~11.3 | /api/download-queue/** | Auth | `authenticated()` catch-all | ✅ |
| 12.1~12.4 | /api/whitelist-channels/** | Auth/Subscriber | `authenticated()` catch-all | ✅ |

### 13.x Company Certification

| API | URL | Spec 권한 | SC 설정 | 판정 | 비고 |
|-----|-----|----------|--------|------|------|
| 13.1 | POST /api/company-certifications | Auth (BUSINESS) | `authenticated()` catch-all | ✅ | SL BUSINESS 체크 |
| 13.2 | GET /api/company-certifications/me | Auth (BUSINESS) | `authenticated()` (line 83) | ✅ | /me 명시 선언 |
| 13.3 | GET /api/company-certifications | ADMIN | `hasRole("ADMIN")` (line 84) | ✅ | |
| 13.4 | GET /api/company-certifications/{id} | ADMIN | `hasRole("ADMIN")` (line 85) | ✅ | |
| 13.5 | PUT /api/company-certifications/{id} | ADMIN | `hasRole("ADMIN")` (line 86) | ✅ | |

### 14.x Utility

| API | URL | Spec 권한 | SC 설정 | 판정 |
|-----|-----|----------|--------|------|
| 14.2 | GET /api/utils/check-email | PUBLIC | `permitAll()` (line 57) | ✅ |
| 14.3 | GET /api/utils/check-phone | PUBLIC | `permitAll()` (line 58) | ✅ |
| 14.4 | GET /api/utils/subscription-status | Auth | `authenticated()` catch-all | ✅ |
| 14.5 | GET /api/utils/download-count | Auth | `authenticated()` catch-all | ✅ |
| 14.6 | GET /api/utils/user-type | Auth | `authenticated()` catch-all | ✅ |
| 14.7 | GET /api/utils/check-nickname | PUBLIC | `permitAll()` (line 59) | ✅ |

#### TestController (명세 외)

| 엔드포인트 | SC 설정 | 판정 |
|----------|--------|------|
| GET /test | 미정의 → anyRequest().permitAll() | ❌ CR-P-003 |
| GET /health | 미정의 → anyRequest().permitAll() | ❌ CR-P-003 |

---

## 2. JWT 보안 흐름 검토

### 2.1 토큰 생성

| 파일:라인 | 항목 | 판정 |
|---------|------|------|
| `JwtTokenProvider.java:27-37` | AT: sub=userId, claim=role, HMAC-SHA | ✅ |
| `JwtTokenProvider.java:39-48` | RT: sub=userId만, role claim 없음 | ✅ |
| `JwtConfig.java:22-29` | 키 길이 검증: >=32 bytes (256 bits) 기동 시 체크 | ✅ |

### 2.2 토큰 검증 (JwtAuthenticationFilter)

| 파일:라인 | 케이스 | 처리 | 판정 |
|---------|------|------|------|
| `JwtAuthenticationFilter.java:36-43` | VALID | UserDetails 로드 + authentication 세팅 | ✅ |
| `JwtAuthenticationFilter.java:45-47` | EXPIRED | SecurityContext clear + X-Token-Expired 헤더 | ✅ |
| `JwtAuthenticationFilter.java:49-51` | INVALID | SecurityContext clear | ✅ |

### 2.3 RefreshToken 갱신 흐름

| 파일:라인 | 항목 | 판정 |
|---------|------|------|
| `AuthService.java:70-73` | VALID 또는 EXPIRED RT 허용 | ⚠️ CR-P-005 |
| `AuthService.java:82-86` | BCrypt 불일치 → RT 삭제 (SEC-07) | ✅ |
| `AuthService.java:89-91` | 탈퇴 계정 차단 (SEC-08) | ✅ |
| `AuthService.java:93-96` | 완전 로테이션: 신규 AT+RT 발급, 해시 저장 | ✅ |
| `AuthService.java:46-47` | 로그인: BCrypt 해시 후 DB 저장 | ✅ |

#### CR-P-005 상세: 만료된 RT 허용

```java
// AuthService.java:70-73
if (result != TokenValidationResult.VALID && result != TokenValidationResult.EXPIRED) {
    throw new BusinessException(BUSINESS_ERROR.REFRESH_TOKEN_INVALID);
}
```

만료된 RT도 DB BCrypt 매칭만 통과하면 갱신 허용.
→ refresh-expiration 설정값이 사실상 무의미 (DB 삭제로만 무효화 가능).
→ 보안 위협 수준: MINOR (BCrypt 검증이 2차 방어선 역할).
→ 권장: EXPIRED 거부 또는 refresh-expiration 설정 제거(오해 방지).

### 2.4 JWT 시크릿 관리

| 파일:라인 | 항목 | 판정 |
|---------|------|------|
| `application.yml:36` | `${JWT_SECRET:YXRzdHVkaW8tc2Vj...}` fallback 존재 | ❌ CR-P-004 |

#### CR-P-004 상세

```
Base64 디코딩: "atstudio-secret-key-for-development-only-2026"
→ 공개 저장소에서 탈취 가능
→ JWT_SECRET 미설정 시 공격자가 임의 토큰(ADMIN 포함) 위조 가능
→ 완전한 시스템 권한 탈취
security-policy.md 6.1절: "Never hardcode in application.yml. Use ${JWT_SECRET} placeholder."
"placeholder-with-fallback" ≠ "placeholder" → 정책 위반
```

---

## 3. ResponseDTO 래핑 일관성 스캔

| Controller | 래핑 상태 | 이슈 |
|-----------|---------|------|
| AuthController | ✅ 전 엔드포인트 | |
| TrackController | ✅ (stream/download는 Resource 반환 — 정상) | |
| TagController | ⚠️ `getAllTags()` raw `List<>` 반환 | CR-P-006 |
| PlaylistController | ✅ | |
| PlayHistoryController | ✅ | |
| LicenseController | ✅ | |
| UserController | ✅ | |
| UtilController | ✅ | |
| NoticeController | ✅ | |
| QuestionController | ✅ (attachment는 Resource — 정상) | |
| WhitelistChannelController | ✅ | |
| CompanyCertificationController | ✅ | |
| SubscriptionController | ✅ | |
| UserSubscriptionController | ⚠️ DELETE 2개 HTTP 200 반환 | CR-P-007 |
| LikeController | ✅ | |
| DownloadQueueController | ✅ | |
| TestController | ❌ raw String 반환 + 보안 미조치 | CR-P-003 |

---

## 4. GlobalExceptionHandler 보안 검토

| 파일:라인 | 항목 | 판정 |
|---------|------|------|
| `GlobalExceptionHandler.java:35-39` | BusinessException — clientMessage만 반환 | ✅ |
| `GlobalExceptionHandler.java:41-45` | TechnicException — clientMessage만 반환 | ✅ |
| `GlobalExceptionHandler.java:54-57` | BadCredentialsException → INVALID_CREDENTIALS (401) | ✅ |
| `GlobalExceptionHandler.java:64-79` | 검증 예외 — 필드 상세 미노출 | ✅ |
| `GlobalExceptionHandler.java:91-94` | DataIntegrityViolationException — 쿼리 내용 미노출 | ✅ |
| `GlobalExceptionHandler.java:116-118` | AccessDeniedException — catch-all 내부 처리 | ⚠️ CR-P-008 |
| 전체 | 스택 트레이스 응답 노출 | ✅ 없음 |

---

## 5. 기존 CR 이슈 보안 관련 재검토

| 기존 이슈 | pg 재평가 | CR-P 매핑 |
|---------|---------|---------|
| CR-C-008: TestController | 보안 위험 확인 | CR-P-003 |
| CR-C-009: JWT 기본 시크릿 | CRITICAL 보안 위험 확인 | CR-P-004 |
| CR-B-001/002: DELETE 200 vs 204 | API 계약 위반 확인 | CR-P-007 |
| CR-A-007: Tag ResponseDTO 미래핑 | 일관성 위반 확인 | CR-P-006 |
| CR-A-009: TrackResponse audioFile 경로 노출 | 보안 관련, 기존 CR-A-009로 충분 | — |
| CR-B-005: Whitelist URL 검증 우회 | 보안 관련, 기존 CR-B-005로 충분 | — |
| CR-C-013: OAuth2 null 체크 | NPE → 스택 트레이스 잠재 위험, 기존으로 충분 | — |

---

## 6. 발견 이슈 종합 (CR-P-XXX)

| # | 심각도 | 파일:라인 | 이슈 | 권장 조치 |
|---|--------|---------|------|---------:|
| CR-P-001 | ❌ CRITICAL | `SecurityConfig.java:71-73` | `/api/users/*` ADMIN 와일드카드가 `/api/users/me` 차단 — 일반 사용자 프로필 접근 불가 | `/api/users/me` 명시적 `authenticated()` 규칙을 와일드카드 앞에 추가 |
| CR-P-003 | ❌ MAJOR | `TestController.java:1-18` | `/test`, `/health` 인증 없이 운영 노출 | 파일 삭제 또는 `@Profile("dev")` 격리 |
| CR-P-004 | ❌ CRITICAL | `application.yml:36` | JWT 시크릿 예측 가능한 Base64 기본값 하드코딩 | fallback 완전 제거: `secret: ${JWT_SECRET}` |
| CR-P-005 | ⚠️ MINOR | `AuthService.java:70-73` | 만료된 RT 갱신 허용 → JWT RT 만료시간 무의미 | EXPIRED 거부 또는 refresh-expiration 설정 제거 |
| CR-P-006 | ⚠️ MINOR | `TagController.java:37-40` | `getAllTags()` raw `List<>` 반환 (ResponseDTO 미래핑) | `ResponseDTO.dataList()` 래핑 |
| CR-P-007 | ❌ MAJOR | `UserSubscriptionController.java:97-103,107-114` | DELETE 6.9/6.10 → HTTP 200 (명세: 204 No Content) | `ResponseEntity.noContent().build()` 변경 |
| CR-P-008 | ⚠️ MINOR | `GlobalExceptionHandler.java:116-118` | AccessDeniedException catch-all 체인 내 처리 | 전용 `@ExceptionHandler(AccessDeniedException.class)` 분리 |
| CR-P-009 | 📋 정보성 | `SecurityConfig.java:83` | `/api/company-certifications/me` BUSINESS 체크는 SL만 — 서비스 레이어 방어 허용 패턴 | 조치 불필요 |
