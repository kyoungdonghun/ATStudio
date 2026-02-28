# WI-20260228-ATS-005 Evidence Pack — Security/Auth/User 코드 리뷰

## CR-P-001: SecurityConfig /api/users/me 규칙 순서

**판정: ✅ PASS**

`SecurityConfig.java:70-79` — /api/users/me 5개 규칙이 /api/users/* ADMIN 와일드카드 앞에 배치됨:
```java
.requestMatchers(HttpMethod.GET,    "/api/users/me").authenticated()       // L71
.requestMatchers(HttpMethod.PUT,    "/api/users/me").authenticated()       // L72
.requestMatchers(HttpMethod.DELETE, "/api/users/me").authenticated()       // L73
.requestMatchers(HttpMethod.PUT,    "/api/users/me/complete-profile").authenticated() // L74
.requestMatchers(HttpMethod.PUT,    "/api/users/me/password").authenticated()         // L75
// ADMIN 와일드카드 (L77-79)
.requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
.requestMatchers(HttpMethod.GET, "/api/users/*").hasRole("ADMIN")
.requestMatchers(HttpMethod.PUT, "/api/users/*").hasRole("ADMIN")
```

## CR-C-002: @Transactional(readOnly=true)

**판정: ✅ PASS**

- `AuthService.java:24` — `@Transactional(readOnly = true)` 클래스 레벨
  - `login()`, `socialLogin()`, `refresh()` — `@Transactional` override (쓰기)
- `OAuth2Service.java:22` — `@Transactional(readOnly = true)` 클래스 레벨
  - `processSocialLogin()` — `@Transactional` override (쓰기)

## CR-C-003: updatePassword() 비밀번호 검증

**판정: ✅ PASS**

`UserService.java:105-116`:
```java
@Transactional
public void updatePassword(Long userID, UpdatePasswordRequest request) {
    User user = userRepository.findById(userID)
            .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    if (user.getPassword() == null
            || !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
        throw new BusinessException(BUSINESS_ERROR.INVALID_CREDENTIALS);
    }
    user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
}
```
- BCrypt.matches() 사용 ✅
- null 비밀번호(소셜 회원) 처리 ✅
- 새 비밀번호 encode 저장 ✅
- 에러 응답에 평문 미포함 ✅

## SUGGESTION 상세

**S-001** — `UpdatePasswordRequest.java:15-16`: `@NotBlank`만 있어 1자 비밀번호 통과 가능. `@Size(min=8, max=100)` 권장.

**S-002** — `SecurityFilterChainTest.java:148-158`: completeProfile 테스트 약한 검증 (`assertNotForbidden()` only). service mock 추가 시 `isOk()` 검증 가능.
