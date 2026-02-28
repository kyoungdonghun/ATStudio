# WI-20260228-ATS-005 Summary — Security/Auth/User 코드 리뷰

**검토 범위:** WI-001 수정 내용 — CR-P-001 (SecurityConfig), CR-C-002 (@Transactional), CR-C-003 (비밀번호 검증)
**최종 판정:** ✅ PASS — 모든 이슈 올바르게 수정됨. SUGGESTION 2건 (비차단)

---

## 파일별 판정

| 파일 | 판정 | 비고 |
|------|------|------|
| `SecurityConfig.java` | ✅ PASS | /api/users/me 5개 엔드포인트 ADMIN 와일드카드 앞에 정확히 배치 |
| `AuthService.java` | ✅ PASS | 클래스 @Transactional(readOnly=true) + mutating 메서드 override |
| `OAuth2Service.java` | ✅ PASS | 동일 패턴 정확히 적용 |
| `UserService.java` | ✅ PASS | BCrypt 검증, null 비밀번호 처리, 인코딩 저장 모두 정확 |
| `User.java` | ✅ PASS | updatePassword() 도메인 메서드 올바름 |
| `UpdatePasswordRequest.java` | ✅ PASS | @NotBlank 검증 (SUGGESTION: @Size(min=8) 미적용) |
| `UserController.java` | ✅ PASS | PUT /api/users/me/password 204 No Content 반환 |
| `SecurityFilterChainTest.java` | ✅ PASS | 7건 테스트, /api/users/me USER 접근 검증 |
| `UserServiceTest.java` | ✅ PASS | 비밀번호 검증 4가지 시나리오 커버 |

---

## SUGGESTION (비차단)

| # | 위치 | 내용 |
|---|------|------|
| S-001 | `UpdatePasswordRequest.java:15-16` | `newPassword`에 `@Size(min=8)` 없음 — 1자 비밀번호도 통과 가능 |
| S-002 | `SecurityFilterChainTest.java:148-158` | `completeProfile` 테스트가 `assertNotForbidden()`만 검증 (200 아님) |
