# WI-20260220-ATS-002 Evidence Pack: Auth Architecture Security Review

> **WI**: WI-20260220-ATS-002
> **Role**: PG (Privacy Guardian)
> **Date**: 2026-02-20
> **Tier 0 Loaded**: core-principles.md (STD-001)
> **Tier 1 Loaded**: security-policy.md, access-control-policy.md
> **Review Target**: WI-20260220-ATS-001-evidence-pack.md (SA Auth Design)

---

## Table of Contents

1. [Detailed Finding Analysis](#1-detailed-finding-analysis)
2. [SE Implementation Security Checklist](#2-se-implementation-security-checklist)
3. [Detection Rationale and Evidence Pointers](#3-detection-rationale-and-evidence-pointers)
4. [Redaction and Block Rules](#4-redaction-and-block-rules)
5. [Deferred Items Register](#5-deferred-items-register)

---

## 1. Detailed Finding Analysis

### C-1. Hardcoded DB Credentials in application.yml [CRITICAL]

| Attribute | Value |
|-----------|-------|
| Severity | CRITICAL |
| Policy Reference | security-policy.md Section 6.2 |
| Location | `C:\Users\jm991\Desktop\project\ATStudio\src\main\resources\application.yml` lines 16-17 |
| Finding | `username: root` and `password: 1234` hardcoded in committed file |
| Risk | Credential exposure in version control. Even for development, establishes a dangerous pattern that can propagate to production. |
| Status | BLOCK -- Must be remediated before Auth implementation proceeds |

**Remediation:**
```yaml
# application.yml (committed)
spring:
  datasource:
    username: ${SPRING_DATASOURCE_USERNAME:root}
    password: ${SPRING_DATASOURCE_PASSWORD}
```
Create `application-local.yml` (add to `.gitignore`) for local dev defaults.

---

### H-1. Email PII in JWT Access Token Payload [HIGH]

| Attribute | Value |
|-----------|-------|
| Severity | HIGH |
| Policy Reference | security-policy.md Section 1.2 (PII), Section 3 (Masking Rules) |
| Location | WI-001 evidence-pack Section 2.1, line 199: `"email": "user@example.com"` in payload |
| Finding | JWT payload is Base64-encoded (not encrypted). Any interceptor can decode and read the email. |
| Risk | PII leakage via token interception (network sniffing, XSS, logging). Email is a direct identifier per security-policy.md Section 1.2. |

**Remediation:**
- Remove `email` claim from Access Token payload.
- Keep only `sub` (userId) and `role` claims.
- If email is needed in the frontend, fetch via `GET /api/users/me` using the access token.
- Update `JwtTokenProvider.generateAccessToken()` signature to remove email parameter.
- Update `JwtAuthenticationFilter` -- do not rely on email from token; load from DB via `CustomUserDetailsService.loadUserById()` (already does this).

**Impact on SA design:**
- Section 2.1: Remove `email` from payload table
- Section 2.4: Remove `getEmail()` method from JwtTokenProvider (or derive from DB)
- Section 8.5 (AuthService): `generateAccessToken()` call signature changes

---

### H-2. Automatic Email-Based Account Linking -- Account Takeover Risk [HIGH]

| Attribute | Value |
|-----------|-------|
| Severity | HIGH |
| Policy Reference | core-principles.md Section 9 (Security & Backup), access-control-policy.md Section 1 (Least Privilege) |
| Location | WI-001 evidence-pack Section 6.3, OAuth2Service lines 722-727 |
| Finding | When a social login provides an email matching an existing local account, the social account is automatically linked without any verification of account ownership. |
| Risk | **Account Takeover Attack Vector**: Attacker creates a social account with victim's email -> social login -> automatic link to victim's local account -> attacker now has full access to victim's account via social login, bypassing password. |

**Attack Scenario:**
1. Victim has local account with email `victim@example.com`
2. Attacker creates a Google account with same email (or uses a provider that does not verify email)
3. Attacker performs social login with `victim@example.com`
4. System finds existing user, auto-links -> Attacker has access

**Remediation Options (choose one):**
- **Option A (Recommended)**: When email matches existing local account, require the user to authenticate with their existing account password before linking. Return a special response indicating "account exists, verification required."
- **Option B**: Only auto-link if the social provider guarantees email verification (Google does; Kakao/Naver may not always). Check `email_verified` claim from provider.
- **Option C**: Never auto-link. Treat as a new, separate account. Offer manual linking in account settings after authenticating both accounts.

---

### H-3. Missing OAuth2 State Parameter for CSRF Protection [HIGH]

| Attribute | Value |
|-----------|-------|
| Severity | HIGH |
| Policy Reference | security-policy.md Section 6.3 (Spring Security Configuration) |
| Location | WI-001 evidence-pack Section 6.1 flow diagram -- no `state` parameter mentioned |
| Finding | The OAuth2 Authorization Code flow does not include a `state` parameter for CSRF protection. |
| Risk | CSRF attack on OAuth2 callback: attacker can trick a victim into completing an OAuth2 flow that links the attacker's social account to the victim's session. |

**Remediation:**
- Frontend generates a cryptographically random `state` value before redirecting to the provider's authorization URL.
- Frontend stores `state` in sessionStorage or a short-lived cookie.
- When the provider redirects back with `authorization_code` + `state`, frontend validates that the returned `state` matches the stored value before sending the code to the backend.
- This is a frontend-side responsibility, but the backend should document this requirement for the frontend team.

---

### H-4. CORS AllowedHeaders Wildcard with Credentials [HIGH]

| Attribute | Value |
|-----------|-------|
| Severity | HIGH |
| Policy Reference | security-policy.md Section 6.3 (CORS: Explicitly configured per environment) |
| Location | WI-001 evidence-pack Section 1.3, CorsConfig line 168: `config.setAllowedHeaders(List.of("*"))` |
| Finding | Wildcard `*` for allowed headers combined with `allowCredentials(true)` is overly permissive. Modern browsers may block this combination, and it exposes the API to potential header injection attacks. |
| Risk | Unnecessary attack surface expansion. Headers like `X-Forwarded-For`, `X-HTTP-Method-Override` could be injected by malicious clients. |

**Remediation:**
```java
config.setAllowedHeaders(List.of(
    "Authorization",
    "Content-Type",
    "Accept",
    "Origin",
    "X-Requested-With"
));
```

---

### M-1. BCrypt for Refresh Token Hashing -- Performance Consideration [MEDIUM]

| Attribute | Value |
|-----------|-------|
| Severity | MEDIUM (informational) |
| Location | WI-001 evidence-pack Section 3.1, 3.3 |
| Finding | BCrypt (strength 10) is used to hash refresh tokens before DB storage. Each encode/matches call takes ~100ms. |
| Assessment | **Approved for current phase.** BCrypt provides excellent security. Performance impact is negligible at current scale (each user refreshes at most once per hour). |
| Future Note | If token refresh endpoint becomes a bottleneck at scale (>10K concurrent users), consider switching to HMAC-SHA256 with a server-side secret for refresh token hashing. This is faster (~microseconds) while still preventing plaintext storage. |

---

### M-2. validateToken() Does Not Distinguish Expired vs Tampered [MEDIUM]

| Attribute | Value |
|-----------|-------|
| Severity | MEDIUM |
| Location | WI-001 evidence-pack Section 2.4, JwtTokenProvider lines 337-344 |
| Finding | `validateToken()` catches all `JwtException` and returns `false`. No distinction between `ExpiredJwtException` and `SignatureException`/`MalformedJwtException`. |
| Risk | Frontend cannot differentiate "token expired, refresh needed" from "token tampered, logout immediately". Both result in 401. |

**Remediation:**
```java
public TokenValidationResult validateToken(String token) {
    try {
        parseToken(token);
        return TokenValidationResult.VALID;
    } catch (ExpiredJwtException e) {
        return TokenValidationResult.EXPIRED;
    } catch (JwtException | IllegalArgumentException e) {
        return TokenValidationResult.INVALID;
    }
}

public enum TokenValidationResult {
    VALID, EXPIRED, INVALID
}
```

The JwtAuthenticationFilter can then:
- VALID -> set authentication
- EXPIRED -> set response header `X-Token-Expired: true` (frontend knows to call /refresh)
- INVALID -> pass through (Spring Security returns 401)

---

### M-3. Production CORS Origins Not Parameterized [MEDIUM]

| Attribute | Value |
|-----------|-------|
| Severity | MEDIUM |
| Location | WI-001 evidence-pack Section 1.3, CorsConfig lines 163-165 |
| Finding | Hardcoded `localhost` origins only. The note says "Production origins must be configured via environment variable" but no design is provided. |

**Remediation:**
```java
@Value("${cors.allowed-origins:http://localhost:3000,http://localhost:8080}")
private String allowedOriginsRaw;

// In corsConfigurationSource():
config.setAllowedOrigins(List.of(allowedOriginsRaw.split(",")));
```

Add to `application.yml`:
```yaml
cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:8080}
```

---

### M-4. Swagger Endpoints Unconditionally Public [MEDIUM]

| Attribute | Value |
|-----------|-------|
| Severity | MEDIUM |
| Location | WI-001 evidence-pack Section 1.2, SecurityConfig line 101 |
| Finding | `/swagger-ui/**` and `/v3/api-docs/**` are `permitAll()` regardless of environment. |
| Risk | In production, API documentation exposes all endpoint contracts, request/response formats, and auth requirements to potential attackers. |

**Remediation:**
Use Spring profile-conditional configuration:
```java
@Profile("!prod")
@Bean
public SecurityFilterChain swaggerFilterChain(HttpSecurity http) throws Exception {
    // permit swagger endpoints only in non-prod profiles
}
```
Or simpler: add Swagger endpoints to `permitAll()` only when a "dev" property is set.

---

### L-1. isProfileComplete Runtime Derivation -- Bypass Consideration [LOW]

| Attribute | Value |
|-----------|-------|
| Severity | LOW |
| Location | WI-001 evidence-pack Section 5.1, CustomUserDetails lines 553-557 |
| Finding | `isProfileComplete` is derived at runtime from `nickname`, `phonePersonal`, and `job` fields. |
| Assessment | **Approved.** Runtime derivation is actually more secure than a stored flag (no stale state). The only "bypass" would be if the user supplies minimal valid data (e.g., fake phone number), but that is a data validation concern, not an auth bypass. |
| Recommendation | Add proper validation on `CompleteProfileRequest` fields (phone format regex, nickname uniqueness check). This is already implied by the DTO design. |

---

### L-2. anyRequest().permitAll() Catch-All for Static Resources [LOW]

| Attribute | Value |
|-----------|-------|
| Severity | LOW |
| Location | WI-001 evidence-pack Section 1.2, SecurityConfig line 131 |
| Finding | `.anyRequest().permitAll()` is used as a catch-all for Thymeleaf static resources. |
| Risk | Any endpoint not explicitly matched by `/api/**` rules is publicly accessible. If a new API endpoint is accidentally created outside `/api/` prefix, it bypasses all auth. |
| Recommendation | In Phase 2 (React SPA), change to `.anyRequest().authenticated()` and explicitly permit only known static resource paths. For now, acceptable with the understanding that all API endpoints use `/api/` prefix. |

---

### L-3. Sensitive Data in API Responses [LOW]

| Attribute | Value |
|-----------|-------|
| Severity | LOW |
| Location | General design concern |
| Assessment | The design correctly separates DTOs from entities. `AuthResponse` and `SocialAuthResponse` contain only tokens and metadata. User data responses should follow dto-standards.md to exclude sensitive fields. No specific issue found in the auth design. |
| Recommendation | Ensure `GET /api/users/me` response DTO masks phone numbers per security-policy.md Section 3 masking rules, or returns full data only to the authenticated owner. |

---

## 2. SE Implementation Security Checklist

The following checklist MUST be followed during WI-003 (Auth Implementation). Each item includes the rationale and specific implementation guidance.

### JWT Implementation

- [ ] **SEC-01**: `BCryptPasswordEncoder` MUST be registered as a `@Bean` in SecurityConfig and injected via constructor (not `new BCryptPasswordEncoder()` inline). Ensures consistent strength value and singleton reuse.
- [ ] **SEC-02**: `JwtTokenProvider.generateAccessToken()` MUST NOT include `email` in the payload. Only `sub` (userId) and `role` claims. (H-1 remediation)
- [ ] **SEC-03**: JWT Secret Key (`JWT_SECRET`) MUST be at least 256 bits (32 bytes) when Base64-decoded. Add a `@PostConstruct` validation in `JwtConfig` that checks `Decoders.BASE64.decode(secret).length >= 32` and throws `IllegalStateException` if too short.
- [ ] **SEC-04**: `JwtTokenProvider.validateToken()` MUST distinguish `ExpiredJwtException` from other `JwtException` types. Return an enum (`VALID`/`EXPIRED`/`INVALID`) instead of boolean. (M-2 remediation)
- [ ] **SEC-05**: `JwtAuthenticationFilter` MUST clear `SecurityContextHolder` on invalid token (call `SecurityContextHolder.clearContext()`). Prevents stale authentication from a previous request in the same thread (thread pool reuse).

### Refresh Token Handling

- [ ] **SEC-06**: Refresh token rotation MUST invalidate the old token atomically. The `user.updateRefreshToken(newHash)` call and the response generation must be in the same `@Transactional` boundary. (Already designed correctly in AuthService.refresh())
- [ ] **SEC-07**: On refresh token mismatch (DB hash does not match presented token), MUST clear the stored refresh token (`user.clearRefreshToken()`) as a defensive measure against token theft. This forces re-login on all sessions.
- [ ] **SEC-08**: `POST /api/auth/refresh` MUST validate that the user is not soft-deleted (`isDeleted = false`) before issuing new tokens.

### OAuth2 Social Login

- [ ] **SEC-09**: DO NOT auto-link social accounts to existing local accounts by email alone. Implement one of: (a) require password verification for existing local accounts, (b) check provider's `email_verified` flag, or (c) disallow auto-linking entirely. (H-2 remediation)
- [ ] **SEC-10**: Document the `state` parameter requirement for frontend OAuth2 flow. The backend social login endpoint (`POST /api/auth/social/{provider}`) should log a warning if called without a prior state validation step. (H-3 remediation)
- [ ] **SEC-11**: OAuth2 client secrets (`GOOGLE_CLIENT_SECRET`, `KAKAO_CLIENT_SECRET`, `NAVER_CLIENT_SECRET`) MUST be loaded from environment variables. Never hardcode or commit to repository.
- [ ] **SEC-12**: Social provider access tokens (received during code exchange) MUST NOT be stored in DB or logged. Use only transiently within `OAuth2Service.processSocialLogin()` method scope.

### CORS and Security Headers

- [ ] **SEC-13**: `CorsConfig.setAllowedHeaders()` MUST use explicit header list, not `"*"`. Required headers: `Authorization`, `Content-Type`, `Accept`, `Origin`, `X-Requested-With`. (H-4 remediation)
- [ ] **SEC-14**: CORS `allowedOrigins` MUST be parameterized via `${CORS_ALLOWED_ORIGINS}` environment variable with localhost defaults. (M-3 remediation)
- [ ] **SEC-15**: Swagger/OpenAPI endpoints (`/swagger-ui/**`, `/v3/api-docs/**`) MUST be restricted to non-production profiles. (M-4 remediation)

### Password and Credential Handling

- [ ] **SEC-16**: `application.yml` MUST NOT contain plaintext DB credentials. Use `${SPRING_DATASOURCE_USERNAME}` and `${SPRING_DATASOURCE_PASSWORD}` placeholders. (C-1 remediation)
- [ ] **SEC-17**: Login failure responses MUST use the same generic message for both "email not found" and "wrong password" scenarios to prevent user enumeration. Error code `INVALID_CREDENTIALS` covers both cases.
- [ ] **SEC-18**: `loadUserByUsername()` and `loadUserById()` exception messages MUST NOT include the email or userId in production logs at INFO level. Use DEBUG level only.

### General Security

- [ ] **SEC-19**: All auth-related error responses MUST use standardized error codes from Section 9 of the SA design. No raw exception messages in API responses.
- [ ] **SEC-20**: The `SecurityFilterChain` MUST be verified with integration tests covering: (a) public endpoint without token = 200, (b) protected endpoint without token = 401, (c) ADMIN endpoint with USER role = 403, (d) ADMIN endpoint with ADMIN role = 200.

---

## 3. Detection Rationale and Evidence Pointers

### C-1 Detection

- **Method**: Grep scan for `password|secret|key|credential` in `*.yml` files under `src/main/resources/`
- **Match**: `C:\Users\jm991\Desktop\project\ATStudio\src\main\resources\application.yml` line 17: `password: 1234`
- **Policy**: security-policy.md Section 6.2: "Production DB credentials must never appear in committed files."
- **Git status**: File is tracked and committed (not in .gitignore)

### H-1 Detection

- **Method**: Review of JWT payload design in WI-001 Section 2.1
- **Pattern**: `"email": "user@example.com"` in Access Token payload specification
- **Policy**: security-policy.md Section 1.2 classifies email as PII (Direct identifier)
- **Risk Model**: JWT payloads are Base64-encoded (not encrypted) -- anyone with the token can read claims

### H-2 Detection

- **Method**: Code flow analysis of OAuth2Service.processSocialLogin() in WI-001 Section 6.3
- **Pattern**: `Optional<User> existingUser = userRepository.findByEmail(userInfo.email())` followed by unconditional linking at line 727
- **Risk Model**: Email spoofing via social providers that do not verify email ownership

### H-3 Detection

- **Method**: Review of OAuth2 flow diagram in WI-001 Section 6.1
- **Pattern**: Absence of `state` parameter in authorization request/callback flow
- **Standard**: OAuth 2.0 RFC 6749 Section 10.12 recommends state parameter for CSRF mitigation

### H-4 Detection

- **Method**: Review of CorsConfig in WI-001 Section 1.3
- **Pattern**: `setAllowedHeaders(List.of("*"))` combined with `setAllowCredentials(true)`
- **Policy**: security-policy.md Section 6.3: "CORS: Explicitly configured per environment"

---

## 4. Redaction and Block Rules

### Pre-Commit Block Rules

| Rule ID | Pattern | Action | Severity |
|---------|---------|--------|----------|
| BLK-001 | `password:\s*[^$\{]` in `*.yml` (non-placeholder values) | BLOCK commit | CRITICAL |
| BLK-002 | `secret:\s*[^$\{]` in `*.yml` | BLOCK commit | CRITICAL |
| BLK-003 | `client-secret:\s*[^$\{]` in `*.yml` | BLOCK commit | CRITICAL |
| BLK-004 | Base64-encoded JWT tokens in source files | WARN | HIGH |
| BLK-005 | Hardcoded IP addresses or internal hostnames in committed config | WARN | MEDIUM |

### Redaction Rules for Logs/Responses

| Rule ID | Data Type | Redaction Method |
|---------|-----------|-----------------|
| RDT-001 | Email in logs | `a***@domain.com` format |
| RDT-002 | JWT tokens in logs | First 10 chars + `...` |
| RDT-003 | Refresh tokens | Never log, even partially |
| RDT-004 | Social provider access tokens | Never log or store |
| RDT-005 | Phone numbers in API responses | `010-****-1234` format (per security-policy.md Section 3) |

---

## 5. Deferred Items Register

Items reviewed and explicitly deferred to future phases. These are NOT security gaps in the current design -- they are enhancements appropriate for later stages.

| ID | Item | Defer To | Rationale |
|----|------|----------|-----------|
| DEF-001 | API Rate Limiting (login, refresh, registration) | Phase 2 | Low traffic in Phase 1. Implement with Bucket4j or Spring Cloud Gateway when scaling. |
| DEF-002 | RS256 Algorithm Migration | Microservice transition | HS256 is correct for single-server. RS256 needed only when multiple services verify tokens independently. |
| DEF-003 | Multi-device Session Support | Future WI | Requires separate `refresh_tokens` table. Single-session is acceptable for MVP. |
| DEF-004 | Login IP Logging and Anomaly Detection | Phase 2 | Useful for account security but not critical for MVP launch. |
| DEF-005 | Refresh Token Absolute Expiry (sliding window cap) | Phase 2 | Current 14-day rolling expiry is acceptable. A 90-day absolute cap prevents indefinite session extension. |
| DEF-006 | Account Lockout After Failed Login Attempts | Phase 2 | Prevents brute force. Currently deferred due to low risk at launch scale. |

---

## Referenced Documents

| Document | Path | Section Used |
|----------|------|--------------|
| SA Auth Design | `C:\Users\jm991\Desktop\project\ATStudio\deliverables\agent\WI-20260220-ATS-001-evidence-pack.md` | All sections |
| Security Policy | `C:\Users\jm991\Desktop\project\ATStudio\docs\policies\security-policy.md` | Sections 1.2, 3, 6.1, 6.2, 6.3 |
| Access Control Policy | `C:\Users\jm991\Desktop\project\ATStudio\docs\policies\access-control-policy.md` | Sections 1, 2 |
| Core Principles | `C:\Users\jm991\Desktop\project\ATStudio\docs\standards\core-principles.md` | Sections 9, 10 |
| application.yml | `C:\Users\jm991\Desktop\project\ATStudio\src\main\resources\application.yml` | Lines 16-17 |
