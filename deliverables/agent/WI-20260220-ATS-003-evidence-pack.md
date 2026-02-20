# WI-20260220-ATS-003 Evidence Pack

## Work Item
- **ID**: WI-20260220-ATS-003
- **Title**: Auth Infrastructure Implementation
- **Agent**: se (Software Engineer)
- **Parent REQ**: REQ-20260220-ATS-001

## File-Level Change Details

### New Files

#### 1. `src/main/java/com/atstudio/atstudio/security/TokenValidationResult.java`
- Simple enum with three states: VALID, EXPIRED, INVALID
- Used by JwtTokenProvider.validateToken() and JwtAuthenticationFilter

#### 2. `src/main/java/com/atstudio/atstudio/config/JwtConfig.java`
- `@Configuration` bean binding `jwt.secret`, `jwt.expiration`, `jwt.refresh-expiration`
- `@PostConstruct` validates Base64-decoded key >= 32 bytes (256 bits)

#### 3. `src/main/java/com/atstudio/atstudio/security/JwtTokenProvider.java`
- JJWT 0.12.5 API: `Jwts.builder()`, `Jwts.parser().verifyWith(key).build().parseSignedClaims()`
- Access token payload: `sub` (userId as String), `role` (UserRole.name()), `iat`, `exp`
- Refresh token payload: `sub` (userId), `iat`, `exp` only
- `validateToken()` returns TokenValidationResult (catches ExpiredJwtException separately)

#### 4. `src/main/java/com/atstudio/atstudio/security/CustomUserDetails.java`
- Implements `UserDetails` with fields: id, email, password, role, isDeleted, isProfileComplete
- `from(User)` static factory; `determineProfileComplete` checks phonePersonal + job non-null
- `getAuthorities()` returns `ROLE_` + role.name()

#### 5. `src/main/java/com/atstudio/atstudio/security/CustomUserDetailsService.java`
- `loadUserByUsername(email)`: standard UserDetailsService contract
- `loadUserById(Long)`: used by JwtAuthenticationFilter for token-based auth
- Both check `isDeleted` and throw UsernameNotFoundException

#### 6. `src/main/java/com/atstudio/atstudio/security/JwtAuthenticationFilter.java`
- Extracts Bearer token from Authorization header
- VALID: loads user, sets SecurityContext
- EXPIRED: clears SecurityContext, adds `X-Token-Expired: true` header
- INVALID: clears SecurityContext (SEC-05 compliance)

#### 7. `src/main/java/com/atstudio/atstudio/config/CorsConfig.java`
- Origins from `${CORS_ALLOWED_ORIGINS}` env-var, split by comma
- Explicit allowed methods and headers (no wildcards per SEC-13)
- Applied to `/api/**` only

### Modified Files

#### 8. `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java`
- Full replacement from permit-all stub to production security config
- STATELESS session management
- Custom authenticationEntryPoint (401 JSON) and accessDeniedHandler (403 JSON)
- URL authorization: PUBLIC endpoints, ADMIN endpoints, authenticated catch-all
- JwtAuthenticationFilter registered before UsernamePasswordAuthenticationFilter
- BCryptPasswordEncoder(10) and AuthenticationManager beans

#### 9. `src/main/java/com/atstudio/atstudio/entity/User.java`
- Added: `@Column(name = "refresh_token", length = 512) private String refreshToken`
- Added: `updateRefreshToken(String)` and `clearRefreshToken()` methods

#### 10. `src/main/java/com/atstudio/atstudio/repository/UserRepository.java`
- Added: `findByEmail(String)`, `findByNickname(String)`, `findByRefreshToken(String)`

#### 11. `src/main/java/com/atstudio/atstudio/repository/SocialAccountRepository.java`
- Added: `findByProviderAndProviderId(SocialProvider provider, String providerId)`
- Confirmed field names from SocialAccount entity: `provider` (SocialProvider enum), `providerId` (String)

#### 12. `src/main/resources/application.yml`
- DB credentials: `${SPRING_DATASOURCE_USERNAME:root}`, `${SPRING_DATASOURCE_PASSWORD:1234}`
- Added `jwt:` block with secret, expiration, refresh-expiration (all env-var with dev defaults)
- Added `cors:` block with allowed-origins env-var
- Removed verbose inline comments for cleanliness

#### 13. `src/main/resources/schema.sql`
- Added `refresh_token VARCHAR(512) NULL` column after `is_deleted`, before `created_at`
- Comment: 'BCrypt-hashed refresh token. NULL when logged out.'

#### 14. `src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java`
- Changed `SUBSCRIPTION_NOT_FOUND` trailing `;` to `,`
- Added 7 new enum constants: INVALID_CREDENTIALS, TOKEN_EXPIRED, REFRESH_TOKEN_INVALID, SOCIAL_AUTH_FAILED, PROFILE_ALREADY_COMPLETE, ACCOUNT_DEACTIVATED, EMAIL_ALREADY_REGISTERED

#### 15. `src/main/java/com/atstudio/atstudio/common/exception/GlobalExceptionHandler.java`
- Added imports: BadCredentialsException, DisabledException, LockedException
- Added two fallback branches at top of if-chain in `handleAllExceptions`:
  - `BadCredentialsException` -> INVALID_CREDENTIALS
  - `DisabledException | LockedException` -> ACCOUNT_DEACTIVATED

## Compilation Evidence

```
> Task :compileJava
BUILD SUCCESSFUL in 5s
1 actionable task: 1 executed
```

## Handoff Points for WI-004 / WI-005

### For WI-004 (Auth Business Logic)
- **JwtTokenProvider**: ready to use `generateAccessToken()`, `generateRefreshToken()`, `validateToken()`
- **CustomUserDetailsService**: ready for AuthService to delegate authentication
- **PasswordEncoder**: BCrypt(10) bean available via DI
- **AuthenticationManager**: bean registered for programmatic authentication
- **BUSINESS_ERROR**: auth error codes ready (INVALID_CREDENTIALS, TOKEN_EXPIRED, REFRESH_TOKEN_INVALID, etc.)
- **User.updateRefreshToken()**: call with BCrypt-hashed refresh token
- **User.clearRefreshToken()**: call on logout
- **UserRepository.findByEmail()**: for login lookup
- **UserRepository.findByRefreshToken()**: for refresh token rotation lookup

### For WI-005 (OAuth2 / Social Login)
- **SocialAccountRepository.findByProviderAndProviderId()**: ready for social auth flow
- **BUSINESS_ERROR.SOCIAL_AUTH_FAILED**: ready for provider callback errors
- **BUSINESS_ERROR.PROFILE_ALREADY_COMPLETE**: ready for complete-profile guard
- **SecurityConfig**: `/api/auth/social/**` endpoints are permitAll
- **CustomUserDetails.isProfileComplete**: available for response DTO mapping

### Important Design Decisions
1. Refresh token stored as BCrypt hash in DB (not raw JWT) -- AuthService must hash before storing
2. Access token contains userId (sub) + role only; no email in payload (pg H-1)
3. X-Token-Expired header signals frontend to attempt refresh silently
4. CORS origins are comma-separated env-var; frontend URL must be included in production
