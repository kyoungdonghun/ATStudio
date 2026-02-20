# WI-20260220-ATS-001 Summary: Auth System Architecture Design

> **WI**: WI-20260220-ATS-001
> **Role**: SA (Software Architect)
> **Date**: 2026-02-20
> **Status**: Design Complete - Awaiting Review

---

## 1. Architecture Decisions Summary

### 1.1 JWT Design

| Item | Decision |
|------|----------|
| Algorithm | HS256 (HMAC-SHA256) |
| Access Token TTL | 1 hour (3,600,000 ms) |
| Refresh Token TTL | 14 days (1,209,600,000 ms) |
| Payload fields | `sub` (userId), `email`, `role`, `iat`, `exp` |
| Secret key source | `${JWT_SECRET}` environment variable (min 256-bit) |

### 1.2 SecurityFilterChain Structure

- **CSRF**: Disabled (JWT-based stateless API)
- **Session**: `SessionCreationPolicy.STATELESS`
- **CORS**: Explicit bean configuration (origin whitelist per environment)
- **JwtFilter**: `OncePerRequestFilter` inserted before `UsernamePasswordAuthenticationFilter`
- Endpoint access rules fully mapped from api-spec.md (PUBLIC / auth required / ADMIN)

### 1.3 Refresh Token Strategy

- **Decision**: Option B (DB storage in `users` table)
- Column: `users.refresh_token VARCHAR(512) NULL`
- On logout/withdrawal: set to `NULL`
- On refresh: validate DB value matches request, then rotate both tokens

### 1.4 OAuth2 Social Login Flow

- Supported providers: GOOGLE, KAKAO, NAVER
- Flow: Frontend obtains Authorization Code -> Backend exchanges for access token -> Fetches user info -> Creates/finds user + social_account -> Issues JWT pair
- New social users: `isProfileComplete = false` -> Frontend redirects to profile completion (PUT /api/users/me/complete-profile)

### 1.5 Package Structure

New packages/classes introduced:
- `config/SecurityConfig.java` (existing, to be redesigned)
- `config/CorsConfig.java` (new)
- `config/JwtConfig.java` (new - properties holder)
- `security/JwtTokenProvider.java` (new)
- `security/JwtAuthenticationFilter.java` (new)
- `security/CustomUserDetailsService.java` (new)
- `security/CustomUserDetails.java` (new)
- `service/auth/AuthService.java` (new)
- `service/auth/OAuth2Service.java` (new)
- `dto/auth/*` (new - login/social/refresh request/response DTOs)

---

## 2. DB Schema Change Required

**Single change**: Add `refresh_token` column to `users` table.

```sql
ALTER TABLE users
    ADD COLUMN refresh_token VARCHAR(512) NULL COMMENT 'Hashed refresh token. NULL when logged out.'
    AFTER is_deleted;
```

- No new tables required
- No existing column modifications
- `social_accounts` table already exists with correct schema

---

## 3. Risks and Considerations

| Risk | Severity | Mitigation |
|------|----------|------------|
| JWT secret key compromise | HIGH | 256-bit minimum, environment variable only, rotation plan via key versioning |
| Refresh token theft | MEDIUM | DB-stored hash comparison, rotate on every refresh, nullify on logout |
| Social provider API changes | LOW | Isolate provider-specific logic in OAuth2Service with provider strategy pattern |
| `isProfileComplete` stale state | LOW | Always derive from DB fields (nickname NULL check), not a stored flag |
| Single-device limitation | LOW | Current design (one refresh_token per user) supports single-session only. Multi-device requires separate token table (future WI if needed) |

---

## 4. Handoff Points

| Next WI | Agent | What to review/implement |
|---------|-------|--------------------------|
| pg-002 (Security Review) | PG | JWT TTL values, CORS whitelist, refresh token hashing algorithm, social provider secret management |
| se-003 (Auth Implementation) | SE | Full implementation per evidence-pack pseudo-code, all DTOs, filter chain, services |
| re-004 (Auth Testing) | RE | Security filter integration tests, JWT token generation/validation unit tests, social login mock tests |
