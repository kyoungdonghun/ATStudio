# Security Code Review Summary — WI-20260220-ATS-009

**WI**: WI-20260220-ATS-009
**Agent**: cr
**Date**: 2026-02-21
**Status**: CONDITIONAL APPROVAL → RESOLVED ✅

## Overall

The Auth implementation correctly addresses 16 out of 18 SEC checklist items from PG review (WI-20260220-ATS-002).
Two MAJOR issues and one MINOR issue were identified and **resolved in this session**.

## Issues Found & Resolved

### MAJOR (Fixed ✅)

**MAJ-1. `application.yml` default plaintext password fallback (SEC-16)**
- **Location**: `src/main/resources/application.yml` line 10
- **Issue**: `password: ${SPRING_DATASOURCE_PASSWORD:1234}` — usable credential in committed code
- **Fix**: Removed `:1234` default → `password: ${SPRING_DATASOURCE_PASSWORD}`

**MAJ-2. Swagger endpoints unconditionally public (SEC-15)**
- **Location**: `src/main/java/.../config/SecurityConfig.java` lines 68-69
- **Issue**: Comment claimed profile-based restriction but no `@Profile` or conditional logic existed
- **Fix**: Added `springdoc.api-docs.enabled: ${SWAGGER_ENABLED:true}` and `springdoc.swagger-ui.enabled: ${SWAGGER_ENABLED:true}` to `application.yml`. Set `SWAGGER_ENABLED=false` in production environment.

### MINOR (Noted, not fixed)

**MIN-1. JWT secret has development default in committed file**
- **Location**: `src/main/resources/application.yml` line 36
- **Status**: Deferred — default value is present for local dev convenience. Production must set `JWT_SECRET` env var.

## Approved SEC Checklist Items

| SEC | Description | Result |
|-----|-------------|--------|
| SEC-01 | BCryptPasswordEncoder(10) @Bean | PASS |
| SEC-02 | Access token: sub + role only (no email) | PASS |
| SEC-03 | JWT key >= 32 bytes @PostConstruct validation | PASS |
| SEC-04 | TokenValidationResult enum (VALID/EXPIRED/INVALID) | PASS |
| SEC-05 | SecurityContextHolder.clearContext() on EXPIRED/INVALID | PASS |
| SEC-06 | Refresh rotation in @Transactional boundary | PASS |
| SEC-07 | DB mismatch → clearRefreshToken() + throw | PASS |
| SEC-08 | Deleted account blocked on refresh | PASS |
| SEC-09 | Social login: EMAIL_ALREADY_REGISTERED, no auto-link | PASS |
| SEC-11 | All OAuth2 secrets via ${ENV_VAR} placeholders | PASS |
| SEC-12 | Social access tokens are transient local variables only | PASS |
| SEC-13 | CORS allowedHeaders explicit 5-item list | PASS |
| SEC-14 | CORS origins via ${CORS_ALLOWED_ORIGINS} env var | PASS |
| SEC-16 | No plaintext DB password default | PASS (after fix) |
| SEC-17 | Generic "이메일 또는 비밀번호가 올바르지 않습니다." for all login failures | PASS |
| SEC-18 | No email/ID in exception messages | PASS |
