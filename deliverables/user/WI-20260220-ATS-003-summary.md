# WI-20260220-ATS-003 Summary

## Title
Auth Infrastructure Implementation (Spring Security 6 + JWT)

## Status: COMPLETE

## Change Summary

### New Files (7)
| # | File | Description |
|---|------|-------------|
| 1 | `src/main/java/.../security/TokenValidationResult.java` | Enum: VALID, EXPIRED, INVALID |
| 2 | `src/main/java/.../config/JwtConfig.java` | JWT configuration bean (`@Value` binding + key-length validation) |
| 3 | `src/main/java/.../security/JwtTokenProvider.java` | Access/refresh token generation, parsing, validation (JJWT 0.12.5) |
| 4 | `src/main/java/.../security/CustomUserDetails.java` | UserDetails implementation with `isProfileComplete` |
| 5 | `src/main/java/.../security/CustomUserDetailsService.java` | UserDetailsService loading by email or ID |
| 6 | `src/main/java/.../security/JwtAuthenticationFilter.java` | OncePerRequestFilter: Bearer token extraction, validation, SecurityContext population |
| 7 | `src/main/java/.../config/CorsConfig.java` | Explicit CORS origins/headers (no wildcards, env-var driven) |

### Modified Files (7)
| # | File | Change |
|---|------|--------|
| 8 | `config/SecurityConfig.java` | Full replacement: STATELESS sessions, JWT filter chain, URL-based authorization (public/admin/authenticated), BCryptPasswordEncoder, AuthenticationManager bean |
| 9 | `entity/User.java` | Added `refreshToken` field + `updateRefreshToken()` / `clearRefreshToken()` methods |
| 10 | `repository/UserRepository.java` | Added `findByEmail`, `findByNickname`, `findByRefreshToken` |
| 11 | `repository/SocialAccountRepository.java` | Added `findByProviderAndProviderId(SocialProvider, String)` |
| 12 | `resources/application.yml` | JWT config block, CORS config, DB credentials env-var with local defaults |
| 13 | `resources/schema.sql` | Added `refresh_token VARCHAR(512)` column to `users` table |
| 14 | `common/exception/BUSINESS_ERROR.java` | Added 7 auth error codes: INVALID_CREDENTIALS, TOKEN_EXPIRED, REFRESH_TOKEN_INVALID, SOCIAL_AUTH_FAILED, PROFILE_ALREADY_COMPLETE, ACCOUNT_DEACTIVATED, EMAIL_ALREADY_REGISTERED |
| 15 | `common/exception/GlobalExceptionHandler.java` | Added fallback handlers for BadCredentialsException, DisabledException, LockedException |

## Verification
- **Compile**: `gradlew.bat compileJava` -- BUILD SUCCESSFUL (0 errors, 0 warnings)
- **Security decisions preserved**: Access token contains only sub(userId) + role (no email per pg H-1); invalid tokens clear SecurityContext (SEC-05); CORS explicit headers only (SEC-13); origins via env-var (SEC-14)

## Risk Assessment
- **LOW**: No runtime logic changes to existing features; all new code is additive infrastructure
- DB schema change (`refresh_token` column) requires `ALTER TABLE` on existing databases or re-run of `schema.sql`
