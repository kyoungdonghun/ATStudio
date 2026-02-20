# WI-20260220-ATS-004 Evidence Pack

## Work Item
- **ID**: WI-20260220-ATS-004
- **Title**: Auth 기능 구현 (로그인 + 소셜 로그인 + 토큰 재발급)
- **Agent**: SE (Software Engineer)
- **Status**: IMPLEMENTED (컴파일 검증 미완료 -- Bash 권한 부재)

## Change Pointers

### New Files

| File | Lines | Purpose |
|------|-------|---------|
| `src/main/java/com/atstudio/atstudio/dto/auth/LoginRequest.java` | 20 | @Getter @Setter @NoArgsConstructor, @NotBlank @Email validation |
| `src/main/java/com/atstudio/atstudio/dto/auth/AuthResponse.java` | 8 | Java record: accessToken, refreshToken, tokenType, expiresIn |
| `src/main/java/com/atstudio/atstudio/dto/auth/RefreshRequest.java` | 14 | @NotBlank refreshToken field |
| `src/main/java/com/atstudio/atstudio/dto/auth/SocialLoginRequest.java` | 14 | @NotBlank authorizationCode field |
| `src/main/java/com/atstudio/atstudio/dto/auth/SocialAuthResponse.java` | 9 | Java record: extends AuthResponse fields + isProfileComplete |
| `src/main/java/com/atstudio/atstudio/service/auth/SocialUserInfo.java` | 3 | Java record: providerId, email, name |
| `src/main/java/com/atstudio/atstudio/service/auth/AuthService.java` | ~95 | login(), socialLogin(), refresh() with SEC-07/08 enforcement |
| `src/main/java/com/atstudio/atstudio/service/auth/OAuth2Service.java` | ~170 | processSocialLogin() with SEC-09, token exchange for 3 providers |
| `src/main/java/com/atstudio/atstudio/controller/AuthController.java` | ~42 | 3 POST endpoints, thin controller pattern with ResponseDTO wrapper |
| `src/main/java/com/atstudio/atstudio/config/AppConfig.java` | 13 | RestClient @Bean for OAuth2Service |

### Modified Files

| File | Change | Rationale |
|------|--------|-----------|
| `src/main/resources/application.yml` | Added `oauth2:` section (Google/Kakao/Naver) with env var defaults | OAuth2Service @Value injection |

## Dependency Chain

### Existing classes consumed (no modifications):
- `JwtTokenProvider.generateAccessToken()`, `.generateRefreshToken()`, `.validateToken()`, `.getUserID()`, `.getAccessTokenExpiration()`
- `CustomUserDetails.getId()`, `.getRole()`
- `TokenValidationResult.VALID`, `.EXPIRED`, `.INVALID`
- `User.updateRefreshToken()`, `.clearRefreshToken()`, `.isDeleted()`, `.getPhonePersonal()`, `.getJob()`
- `SocialAccount` entity with builder
- `BUSINESS_ERROR.REFRESH_TOKEN_INVALID`, `.RESOURCE_NOT_FOUND`, `.ACCOUNT_DEACTIVATED`, `.EMAIL_ALREADY_REGISTERED`
- `BusinessException`
- `ResponseDTO.withSingleData()`
- `UserRepository.findById()`, `.findByEmail()`
- `SocialAccountRepository.findByProviderAndProviderId()`
- `SecurityConfig` already has permitAll for `/api/auth/**` endpoints
- `AuthenticationManager`, `PasswordEncoder` beans from SecurityConfig

## Design Decisions

1. **RestClient over WebClient**: RestClient is synchronous and simpler for server-to-server OAuth2 token exchange. WebClient would add unnecessary reactive complexity.
2. **AppConfig separate from SecurityConfig**: RestClient bean is a general HTTP client concern, not security-specific. Separation of concerns.
3. **@SuppressWarnings("unchecked") on Map casts**: RestClient returns raw Map from JSON deserialization. Type-safe DTOs for provider responses would be over-engineering at this stage.
4. **ExpiredJwtException catch in refresh()**: JJWT throws ExpiredJwtException before returning claims. We catch it to extract the subject (userID) from expired tokens, which is valid behavior for refresh token rotation.

## Reproduction / Verification

```bash
# Step 1: Compile check
gradlew.bat compileJava

# Step 2: Full build (if MySQL is available)
gradlew.bat build

# Step 3: Manual API test (after bootRun)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"password"}'
```

## Follow-up WI

- **WI-005**: User 기능 구현 (/api/users/*) -- AuthService를 의존
- **WI-008** (계획): Auth 단위 테스트 (AuthServiceTest, OAuth2ServiceTest, AuthControllerTest)
- RestClient mock 전략 필요 (OAuth2Service 테스트)
