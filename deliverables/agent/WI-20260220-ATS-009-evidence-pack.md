# WI-20260220-ATS-009 Evidence Pack: Auth Security Code Review

**WI**: WI-20260220-ATS-009
**Agent**: cr
**Date**: 2026-02-21
**Scope**: Auth security infrastructure (JwtTokenProvider, JwtAuthenticationFilter, CustomUserDetails, CustomUserDetailsService, TokenValidationResult, SecurityConfig, JwtConfig, CorsConfig) + AuthService + OAuth2Service + application.yml
**Standards**: core-principles.md, development-standards.md, security-policy.md
**PG Checklist Reference**: WI-20260220-ATS-002-evidence-pack.md (SEC-01~SEC-20)

---

## SEC Checklist Verification

| SEC | Requirement | File:Line | Result |
|-----|-------------|-----------|--------|
| SEC-01 | BCryptPasswordEncoder(10) @Bean | SecurityConfig.java:97-100 | PASS |
| SEC-02 | No email in JWT access token | JwtTokenProvider.java:27 | PASS |
| SEC-03 | JWT key >= 32 bytes @PostConstruct | JwtConfig.java:22-29 | PASS |
| SEC-04 | TokenValidationResult enum | JwtTokenProvider.java:66-75 | PASS |
| SEC-05 | clearContext() on EXPIRED/INVALID | JwtAuthenticationFilter.java:46,50 | PASS |
| SEC-06 | Refresh rotation @Transactional | AuthService.java (class-level) | PASS |
| SEC-07 | DB mismatch → clearRefreshToken() | AuthService.java:88-92 | PASS |
| SEC-08 | Deleted account blocked on refresh | AuthService.java:94-97 | PASS |
| SEC-09 | No auto-link by email (social) | OAuth2Service.java:72-75 | PASS |
| SEC-11 | OAuth2 secrets via env vars | application.yml:46-57 | PASS |
| SEC-12 | Social token transient only | OAuth2Service.java:59 | PASS |
| SEC-13 | CORS explicit headers | CorsConfig.java:23-24 | PASS |
| SEC-14 | CORS origins via env var | CorsConfig.java:15-16 | PASS |
| SEC-15 | Swagger prod restriction | application.yml (springdoc section) | PASS (after fix) |
| SEC-16 | No plaintext DB password default | application.yml:10 | PASS (after fix) |
| SEC-17 | Generic login failure message | BUSINESS_ERROR.java + GlobalExceptionHandler | PASS |
| SEC-18 | No PII in exception messages | CustomUserDetailsService.java:20,22 | PASS |
| SEC-10 | OAuth2 state param | N/A (frontend responsibility) | DEFERRED |
| SEC-20 | SecurityFilterChain tests | N/A (delegate to re) | DEFERRED |

---

## Issues Found & Fixed

### MAJ-1: Plaintext DB password default removed
- **Before**: `password: ${SPRING_DATASOURCE_PASSWORD:1234}`
- **After**: `password: ${SPRING_DATASOURCE_PASSWORD}`
- **File**: `src/main/resources/application.yml:10`

### MAJ-2: Swagger conditional property added
- **Added to** `src/main/resources/application.yml`:
  ```yaml
  springdoc:
    api-docs:
      enabled: ${SWAGGER_ENABLED:true}
    swagger-ui:
      enabled: ${SWAGGER_ENABLED:true}
  ```
- Production deployment: set `SWAGGER_ENABLED=false`

---

## Build & Test Evidence

```
gradlew.bat build -x test → BUILD SUCCESSFUL in 3s
gradlew.bat test          → BUILD SUCCESSFUL in 14s (131/131 PASS)
```
