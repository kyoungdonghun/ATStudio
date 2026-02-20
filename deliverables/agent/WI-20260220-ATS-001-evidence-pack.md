# WI-20260220-ATS-001 Evidence Pack: Auth System Architecture Design

> **WI**: WI-20260220-ATS-001
> **Role**: SA (Software Architect)
> **Date**: 2026-02-20
> **Tier 0 Loaded**: core-principles.md (STD-001), development-standards.md (STD-002)
> **Tier 1 Loaded**: security-policy.md, dto-standards.md, exception-handling.md

---

## Table of Contents

1. [SecurityFilterChain Design](#1-securityfilterchain-design)
2. [JWT Design Specification](#2-jwt-design-specification)
3. [Refresh Token DB Strategy](#3-refresh-token-db-strategy)
4. [JwtFilter Processing Flow](#4-jwtfilter-processing-flow)
5. [CustomUserDetailsService Design](#5-customuserdetailsservice-design)
6. [OAuth2 Social Login Flow](#6-oauth2-social-login-flow)
7. [Package Structure](#7-package-structure)
8. [Auth DTOs](#8-auth-dtos)
9. [GlobalExceptionHandler Auth Error Codes](#9-globalexceptionhandler-auth-error-codes)
10. [Endpoint Access Rules (Complete)](#10-endpoint-access-rules-complete)
11. [Checklist](#11-checklist)
12. [Handoff Points](#12-handoff-points)

---

## 1. SecurityFilterChain Design

### 1.1 Design Intent

Replace the current permissive `SecurityConfig.java` (line 22-24: `anyRequest().permitAll()`) with a production-ready JWT-based stateless configuration.

**Reference**: `C:\Users\jm991\Desktop\project\ATStudio\src\main\java\com\atstudio\atstudio\config\SecurityConfig.java`

### 1.2 Pseudo-code

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // for @PreAuthorize if needed later
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. CSRF disabled — JWT-based stateless API (security-policy.md Section 6.3)
            .csrf(csrf -> csrf.disable())

            // 2. CORS — delegate to CorsConfig bean
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 3. Session — STATELESS (no HttpSession)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 4. Exception handling for auth errors
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    // 401 Unauthorized — unauthenticated access
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("""
                        {"status":401,"error":"Unauthorized","message":"인증이 필요합니다. 다시 로그인해주세요."}
                        """);
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    // 403 Forbidden — insufficient role
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("""
                        {"status":403,"error":"Forbidden","message":"해당 정보를 열람할 수 없습니다."}
                        """);
                })
            )

            // 5. Endpoint authorization rules
            .authorizeHttpRequests(auth -> auth
                // --- PUBLIC endpoints ---
                .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/social/{provider}").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/utils/check-email").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/utils/check-phone").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/utils/check-nickname").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/tracks").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/tracks/{trackId}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/tracks/{trackId}/stream").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/tags").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/subscriptions").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/subscriptions/{subscriptionId}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/notices").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/notices/{noticeId}").permitAll()
                // Swagger/OpenAPI
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // --- ADMIN endpoints ---
                .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/users/{userId}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/users/{userId}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/tracks").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/tracks/{trackId}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/tracks/{trackId}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/tags").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/tags/{tagId}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/tags/{tagId}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/user-subscriptions").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/user-subscriptions/{userSubscriptionId}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/user-subscriptions/{userSubscriptionId}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/user-subscriptions/{userSubscriptionId}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/users/{userId}/licenses").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/users/{userId}/licenses/{licenseId}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/notices").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/notices/{noticeId}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/notices/{noticeId}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/business-licenses").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/business-licenses/{requestId}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/business-licenses/{requestId}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/questions/{questionId}/status").hasRole("ADMIN")

                // --- All other /api/** require authentication ---
                .requestMatchers("/api/**").authenticated()

                // --- Static resources (Thymeleaf phase) ---
                .anyRequest().permitAll()
            )

            // 6. JWT filter — before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);  // strength 10 per security-policy.md
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
```

### 1.3 CorsConfig (Separate Bean)

```java
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://localhost:3000",    // React dev server (Phase 2)
            "http://localhost:8080"     // Thymeleaf dev (Phase 1)
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
```

**Note**: Production origins must be configured via environment variable. This is a dev-only default.

---

## 2. JWT Design Specification

### 2.1 Token Structure

**Header:**
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**Payload (Access Token):**
```json
{
  "sub": "1",
  "email": "user@example.com",
  "role": "USER",
  "iat": 1740000000,
  "exp": 1740003600
}
```

| Claim | Type | Description |
|-------|------|-------------|
| `sub` | String | User ID (Long.toString()) |
| `email` | String | User email |
| `role` | String | `USER` or `ADMIN` |
| `iat` | Long | Issued at (epoch seconds) |
| `exp` | Long | Expiration (epoch seconds) |

**Payload (Refresh Token):**
```json
{
  "sub": "1",
  "iat": 1740000000,
  "exp": 1741209600
}
```

Refresh token carries minimal claims (no email/role) -- it is only used for token rotation, not authorization.

### 2.2 TTL Configuration

| Token | TTL | Milliseconds | Environment Variable |
|-------|-----|-------------|---------------------|
| Access Token | 1 hour | 3,600,000 | `${JWT_EXPIRATION}` |
| Refresh Token | 14 days | 1,209,600,000 | `${JWT_REFRESH_EXPIRATION}` |

**Rationale:**
- Access Token 1h: balances UX (not too short) and security (limits window of compromised token)
- Refresh Token 14 days: aligns with typical "remember me" periods, requires re-login every 2 weeks at most

### 2.3 JwtConfig (Properties Holder)

```java
@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:3600000}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-expiration:1209600000}")
    private long refreshTokenExpiration;

    // getters
}
```

**application.yml addition:**
```yaml
jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION:3600000}
  refresh-expiration: ${JWT_REFRESH_EXPIRATION:1209600000}
```

### 2.4 JwtTokenProvider

```java
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;
    private SecretKey key;

    @PostConstruct
    public void init() {
        // JJWT 0.12.5 API: Keys.hmacShaKeyFor(byte[])
        this.key = Keys.hmacShaKeyFor(
            Decoders.BASE64.decode(jwtConfig.getSecret())
        );
    }

    /**
     * Generate access token with full claims.
     */
    public String generateAccessToken(Long userID, String email, UserRole role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtConfig.getAccessTokenExpiration());

        return Jwts.builder()
            .subject(String.valueOf(userID))
            .claim("email", email)
            .claim("role", role.name())
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact();
    }

    /**
     * Generate refresh token with minimal claims.
     */
    public String generateRefreshToken(Long userID) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtConfig.getRefreshTokenExpiration());

        return Jwts.builder()
            .subject(String.valueOf(userID))
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact();
    }

    /**
     * Parse and validate JWT. Returns Claims on success.
     * Throws JwtException subtypes on failure.
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public Long getUserID(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    public String getEmail(String token) {
        return parseToken(token).get("email", String.class);
    }

    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getAccessTokenExpiration() {
        return jwtConfig.getAccessTokenExpiration();
    }
}
```

**Note on naming**: Per development-standards.md Section 2A.3, abbreviation-preserving camelCase is used: `userID` not `userId`.

---

## 3. Refresh Token DB Strategy

### 3.1 Decision: Option B (DB Storage)

Store a **hashed** refresh token in the `users` table.

### 3.2 DDL

```sql
ALTER TABLE users
    ADD COLUMN refresh_token VARCHAR(512) NULL
    COMMENT 'BCrypt-hashed refresh token. NULL when logged out or withdrawn.'
    AFTER is_deleted;
```

**Why VARCHAR(512)?** BCrypt hash output is 60 chars, but using 512 allows future algorithm changes without schema migration.

### 3.3 Lifecycle Rules

| Event | Action |
|-------|--------|
| Login (local or social) | Generate refresh token, BCrypt-hash it, store in `users.refresh_token` |
| Token refresh (`POST /api/auth/refresh`) | (1) Parse refresh token from request, (2) Load user from `sub` claim, (3) BCrypt-verify against stored hash, (4) Generate new token pair, (5) Update stored hash |
| Logout (future API) | Set `users.refresh_token = NULL` |
| Withdrawal (`DELETE /api/users/me`) | Set `users.refresh_token = NULL` (before soft delete) |
| Token expired | Client must re-login; stale DB value is harmless |

### 3.4 User Entity Addition (Design Only)

The `User` entity will need:
```java
@Column(name = "refresh_token", length = 512)
private String refreshToken;
```

Plus a domain method:
```java
public void updateRefreshToken(String hashedRefreshToken) {
    this.refreshToken = hashedRefreshToken;
}

public void clearRefreshToken() {
    this.refreshToken = null;
}
```

### 3.5 Single-Session Limitation

This design supports **one active session per user**. If the user logs in from a second device, the first device's refresh token becomes invalid. This is acceptable for ATStudio's current phase. Multi-device support (separate `refresh_tokens` table) can be added as a future WI if needed.

---

## 4. JwtFilter Processing Flow

### 4.1 JwtAuthenticationFilter (OncePerRequestFilter)

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Step 1: Extract token from Authorization header
        String token = resolveToken(request);

        // Step 2: If no token, pass through (public endpoints will succeed,
        //         protected endpoints will be caught by Spring Security)
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3: Validate token
        if (!jwtTokenProvider.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 4: Extract user info and build Authentication
        Long userID = jwtTokenProvider.getUserID(token);
        UserDetails userDetails = userDetailsService.loadUserById(userID);

        // Step 5: Set SecurityContext
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(
            new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Step 6: Continue filter chain
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
```

### 4.2 Flow Diagram (Step-by-step)

```
Request arrives
  |
  v
[1] Extract "Authorization: Bearer {token}" header
  |
  +-- No header? --> Pass through to Spring Security
  |                   (public: OK, protected: 401)
  v
[2] Validate JWT signature + expiration
  |
  +-- Invalid/Expired? --> Pass through (Spring Security handles 401)
  |
  v
[3] Extract userId from token subject
  |
  v
[4] Load UserDetails from DB via CustomUserDetailsService.loadUserById()
  |
  +-- User not found / isDeleted? --> Pass through (no auth set)
  |
  v
[5] Create UsernamePasswordAuthenticationToken with authorities
  |
  v
[6] Set SecurityContextHolder.getContext().setAuthentication(...)
  |
  v
[7] Continue filter chain --> Controller executes
```

---

## 5. CustomUserDetailsService Design

### 5.1 CustomUserDetails (UserDetails Implementation)

```java
@Getter
@Builder
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final UserRole role;
    private final boolean isDeleted;
    private final boolean isProfileComplete;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() { return email; }

    @Override
    public String getPassword() { return password; }

    @Override
    public boolean isAccountNonLocked() { return !isDeleted; }

    // isEnabled, isAccountNonExpired, isCredentialsNonExpired: default true

    public static CustomUserDetails from(User user) {
        return CustomUserDetails.builder()
            .id(user.getId())
            .email(user.getEmail())
            .password(user.getPassword())
            .role(user.getRole())
            .isDeleted(user.isDeleted())
            .isProfileComplete(determineProfileComplete(user))
            .build();
    }

    /**
     * Profile is complete when all required fields are filled.
     * Social-login users initially have NULL nickname (auto-generated placeholder
     * is not considered complete) or NULL job.
     */
    private static boolean determineProfileComplete(User user) {
        return user.getNickname() != null
            && user.getPhonePersonal() != null
            && user.getJob() != null;
    }
}
```

**Note on `isProfileComplete`**: This is **derived at runtime** from the user's actual field state, not stored as a DB column. This eliminates stale-flag risk.

### 5.2 CustomUserDetailsService

```java
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Spring Security standard entry point (login by email).
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (user.isDeleted()) {
            throw new UsernameNotFoundException("User account is deactivated: " + email);
        }

        return CustomUserDetails.from(user);
    }

    /**
     * JWT filter entry point (load by userId from token).
     */
    public UserDetails loadUserById(Long userID) {
        User user = userRepository.findById(userID)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userID));

        if (user.isDeleted()) {
            throw new UsernameNotFoundException("User account is deactivated: " + userID);
        }

        return CustomUserDetails.from(user);
    }
}
```

---

## 6. OAuth2 Social Login Flow

### 6.1 Overall Flow

```
[Frontend]                      [Backend]                       [Social Provider]
    |                               |                                  |
    |  (1) Redirect user to         |                                  |
    |      provider's auth page ----|--------------------------------->|
    |                               |                                  |
    |  (2) User grants permission   |                                  |
    |<------ authorization_code ----|----------------------------------|
    |                               |                                  |
    |  (3) POST /api/auth/social/{provider}                            |
    |      { authorizationCode }    |                                  |
    |------------------------------>|                                  |
    |                               |  (4) Exchange code for           |
    |                               |      access_token -------------->|
    |                               |<---- access_token + user info ---|
    |                               |                                  |
    |                               |  (5) Find or create User +       |
    |                               |      SocialAccount in DB         |
    |                               |                                  |
    |                               |  (6) Generate JWT pair           |
    |                               |      (access + refresh)          |
    |                               |                                  |
    |  (7) Return tokens +          |                                  |
    |      isProfileComplete        |                                  |
    |<------------------------------|                                  |
    |                               |                                  |
    |  (8) If isProfileComplete=false                                  |
    |      -> Navigate to profile   |                                  |
    |         completion screen     |                                  |
```

### 6.2 Provider-Specific Configuration

#### Google

| Item | Value |
|------|-------|
| Authorization URL | `https://accounts.google.com/o/oauth2/v2/auth` |
| Token Exchange URL | `https://oauth2.googleapis.com/token` |
| User Info URL | `https://www.googleapis.com/oauth2/v2/userinfo` |
| Scopes | `email`, `profile` |
| Returned fields | `id` (providerId), `email`, `name` |

#### Kakao

| Item | Value |
|------|-------|
| Authorization URL | `https://kauth.kakao.com/oauth/authorize` |
| Token Exchange URL | `https://kauth.kakao.com/oauth/token` |
| User Info URL | `https://kapi.kakao.com/v2/user/me` |
| Scopes | `profile_nickname`, `account_email` |
| Returned fields | `id` (providerId), `kakao_account.email`, `properties.nickname` |

#### Naver

| Item | Value |
|------|-------|
| Authorization URL | `https://nid.naver.com/oauth2.0/authorize` |
| Token Exchange URL | `https://nid.naver.com/oauth2.0/token` |
| User Info URL | `https://openapi.naver.com/v1/nid/me` |
| Scopes | (default) |
| Returned fields | `response.id` (providerId), `response.email`, `response.name` |

**Environment Variables for OAuth2:**
```yaml
oauth2:
  google:
    client-id: ${GOOGLE_CLIENT_ID}
    client-secret: ${GOOGLE_CLIENT_SECRET}
    redirect-uri: ${GOOGLE_REDIRECT_URI}
  kakao:
    client-id: ${KAKAO_CLIENT_ID}
    client-secret: ${KAKAO_CLIENT_SECRET}
    redirect-uri: ${KAKAO_REDIRECT_URI}
  naver:
    client-id: ${NAVER_CLIENT_ID}
    client-secret: ${NAVER_CLIENT_SECRET}
    redirect-uri: ${NAVER_REDIRECT_URI}
```

### 6.3 OAuth2Service Design

```java
@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final RestClient restClient;  // Spring Boot 4.x preferred HTTP client

    /**
     * Core social login method.
     * Returns the User entity (new or existing).
     */
    @Transactional
    public User processSocialLogin(SocialProvider provider, String authorizationCode) {
        // Step 1: Exchange authorization code for access token
        String socialAccessToken = exchangeCodeForToken(provider, authorizationCode);

        // Step 2: Fetch user info from provider
        SocialUserInfo userInfo = fetchUserInfo(provider, socialAccessToken);

        // Step 3: Check if social account already exists
        Optional<SocialAccount> existingSocial = socialAccountRepository
            .findByProviderAndProviderId(provider, userInfo.providerId());

        if (existingSocial.isPresent()) {
            // Existing user — return
            return existingSocial.get().getUser();
        }

        // Step 4: Check if email already registered (local account linking)
        Optional<User> existingUser = userRepository.findByEmail(userInfo.email());

        User user;
        if (existingUser.isPresent()) {
            // Link social account to existing local user
            user = existingUser.get();
        } else {
            // Step 5: Create new user with minimal info
            //   - nickname: temporary placeholder (e.g., provider + providerId hash)
            //   - password: NULL (social-only account)
            //   - job: NULL (to be filled in profile completion)
            //   - phonePersonal: NULL (to be filled in profile completion)
            user = User.builder()
                .email(userInfo.email())
                .nickname(generateTempNickname(provider, userInfo.providerId()))
                .password(null)
                .role(UserRole.USER)
                .userType(UserType.INDIVIDUAL)
                .build();
            user = userRepository.save(user);
        }

        // Step 6: Create SocialAccount entry
        SocialAccount socialAccount = SocialAccount.builder()
            .user(user)
            .provider(provider)
            .providerId(userInfo.providerId())
            .build();
        socialAccountRepository.save(socialAccount);

        return user;
    }

    private String generateTempNickname(SocialProvider provider, String providerId) {
        // e.g., "GOOGLE_a1b2c3" (first 6 chars of hash)
        String hash = Integer.toHexString(providerId.hashCode());
        return provider.name() + "_" + hash.substring(0, Math.min(6, hash.length()));
    }

    // ... exchangeCodeForToken(), fetchUserInfo() implementations
    //     use RestClient to call provider APIs per Section 6.2 URLs
}

record SocialUserInfo(String providerId, String email, String name) {}
```

### 6.4 isProfileComplete Determination

**Decision**: `isProfileComplete` is NOT a stored column. It is derived at runtime.

**Logic** (in `CustomUserDetails.determineProfileComplete()`):
```
isProfileComplete = (nickname != temp-generated)
                    AND (phonePersonal != null)
                    AND (job != null)
```

In practice, we check:
- `user.getPhonePersonal() != null` -- social users start with NULL phone
- `user.getJob() != null` -- social users start with NULL job

The temporary nickname check is handled by: if `phonePersonal` and `job` are non-null, the user has completed the profile form (which also sets a real nickname).

### 6.5 AuthService (Login/Refresh Orchestration)

```java
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OAuth2Service oAuth2Service;

    /**
     * Local email/password login.
     */
    public AuthResponse login(LoginRequest request) {
        // Step 1: Authenticate via Spring Security
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Step 2: Generate token pair
        String accessToken = jwtTokenProvider.generateAccessToken(
            userDetails.getId(), userDetails.getEmail(), userDetails.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails.getId());

        // Step 3: Store hashed refresh token in DB
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        user.updateRefreshToken(passwordEncoder.encode(refreshToken));

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtTokenProvider.getAccessTokenExpiration() / 1000)
            .build();
    }

    /**
     * Social login.
     */
    public SocialAuthResponse socialLogin(SocialProvider provider, String authorizationCode) {
        // Step 1: Process social login (creates/finds user)
        User user = oAuth2Service.processSocialLogin(provider, authorizationCode);

        // Step 2: Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
            user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        // Step 3: Store refresh token
        user.updateRefreshToken(passwordEncoder.encode(refreshToken));

        // Step 4: Determine profile completion
        boolean isProfileComplete = user.getPhonePersonal() != null
            && user.getJob() != null;

        return SocialAuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtTokenProvider.getAccessTokenExpiration() / 1000)
            .isProfileComplete(isProfileComplete)
            .build();
    }

    /**
     * Refresh token rotation.
     */
    public AuthResponse refresh(RefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        // Step 1: Validate token
        if (!jwtTokenProvider.validateToken(requestRefreshToken)) {
            throw new BusinessException(BUSINESS_ERROR.UNAUTHORIZED_ACTION);
        }

        // Step 2: Extract userId
        Long userID = jwtTokenProvider.getUserID(requestRefreshToken);

        // Step 3: Load user and verify stored hash matches
        User user = userRepository.findById(userID)
            .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        if (user.getRefreshToken() == null
                || !passwordEncoder.matches(requestRefreshToken, user.getRefreshToken())) {
            throw new BusinessException(BUSINESS_ERROR.UNAUTHORIZED_ACTION);
        }

        // Step 4: Generate new pair (rotation)
        String newAccessToken = jwtTokenProvider.generateAccessToken(
            user.getId(), user.getEmail(), user.getRole());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        // Step 5: Update stored hash
        user.updateRefreshToken(passwordEncoder.encode(newRefreshToken));

        return AuthResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtTokenProvider.getAccessTokenExpiration() / 1000)
            .build();
    }
}
```

---

## 7. Package Structure

```
com.atstudio.atstudio
├── common/
│   └── entity/
│       └── BaseEntity.java                 (existing)
├── config/
│   ├── SecurityConfig.java                 (existing → redesign)
│   ├── JpaConfig.java                      (existing)
│   ├── CorsConfig.java                     (NEW)
│   └── JwtConfig.java                      (NEW — @Value properties holder)
├── controller/
│   └── AuthController.java                 (NEW)
├── dto/
│   └── auth/
│       ├── LoginRequest.java               (NEW)
│       ├── AuthResponse.java               (NEW)
│       ├── SocialLoginRequest.java         (NEW)
│       ├── SocialAuthResponse.java         (NEW)
│       ├── RefreshRequest.java             (NEW)
│       └── CompleteProfileRequest.java     (NEW)
├── entity/
│   ├── User.java                           (existing → add refreshToken field)
│   ├── SocialAccount.java                  (existing)
│   └── enums/
│       ├── UserRole.java                   (existing)
│       └── SocialProvider.java             (existing)
├── repository/
│   ├── UserRepository.java                 (existing → add findByEmail)
│   └── SocialAccountRepository.java        (existing → add findByProviderAndProviderId)
├── security/
│   ├── JwtTokenProvider.java               (NEW — token generation/validation)
│   ├── JwtAuthenticationFilter.java        (NEW — OncePerRequestFilter)
│   ├── CustomUserDetails.java              (NEW — UserDetails impl)
│   └── CustomUserDetailsService.java       (NEW — UserDetailsService impl)
└── service/
    └── auth/
        ├── AuthService.java                (NEW — login/refresh orchestration)
        └── OAuth2Service.java              (NEW — social provider integration)
```

### File Count Summary

| Category | New | Modified |
|----------|-----|----------|
| Config | 2 | 1 (SecurityConfig) |
| Controller | 1 | 0 |
| DTO | 6 | 0 |
| Entity | 0 | 1 (User) |
| Repository | 0 | 2 (add query methods) |
| Security | 4 | 0 |
| Service | 2 | 0 |
| **Total** | **15** | **4** |

---

## 8. Auth DTOs

Per dto-standards.md: Request DTOs use `@Getter @Setter @NoArgsConstructor`. Response DTOs use `record` or `@Getter @Builder`.

### 8.1 LoginRequest

```java
@Getter @Setter
@NoArgsConstructor
public class LoginRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}
```

### 8.2 AuthResponse

```java
public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn
) {
    @Builder
    public AuthResponse {}
}
```

### 8.3 SocialLoginRequest

```java
@Getter @Setter
@NoArgsConstructor
public class SocialLoginRequest {
    @NotBlank
    private String authorizationCode;
}
```

### 8.4 SocialAuthResponse

```java
public record SocialAuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    boolean isProfileComplete
) {
    @Builder
    public SocialAuthResponse {}
}
```

### 8.5 RefreshRequest

```java
@Getter @Setter
@NoArgsConstructor
public class RefreshRequest {
    @NotBlank
    private String refreshToken;
}
```

### 8.6 CompleteProfileRequest

```java
@Getter @Setter
@NoArgsConstructor
public class CompleteProfileRequest {
    @NotBlank
    @Size(max = 20)
    private String nickname;

    @NotBlank
    @Size(max = 20)
    private String phonePersonal;

    @Size(max = 20)
    private String phoneCompany;

    @NotNull
    private UserJob job;

    @NotNull
    private UserType userType;
}
```

### 8.7 AuthController (Thin)

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ResponseDTO.<AuthResponse>withSingleData()
            .message("Login successful")
            .data(authService.login(request))
            .build());
    }

    @PostMapping("/social/{provider}")
    public ResponseEntity<ResponseDTO<SocialAuthResponse>> socialLogin(
            @PathVariable SocialProvider provider,
            @Valid @RequestBody SocialLoginRequest request) {
        return ResponseEntity.ok(ResponseDTO.<SocialAuthResponse>withSingleData()
            .message("Social login successful")
            .data(authService.socialLogin(provider, request.getAuthorizationCode()))
            .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseDTO<AuthResponse>> refresh(
            @Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(ResponseDTO.<AuthResponse>withSingleData()
            .message("Token refreshed")
            .data(authService.refresh(request))
            .build());
    }
}
```

---

## 9. GlobalExceptionHandler Auth Error Codes

### 9.1 New Auth-Specific Error Codes to Add

Per exception-handling.md Section 2.3 (ATStudio Domain Extensions), the following auth-related codes are needed:

| Code | HTTP Status | Client Message | Developer Message | Category |
|------|------------|----------------|-------------------|----------|
| `UNAUTHORIZED_ACTION` | 401 UNAUTHORIZED | 인증이 필요합니다. 다시 로그인해주세요. | JWT 토큰 만료 또는 무효. | BusinessException |
| `INVALID_CREDENTIALS` | 401 UNAUTHORIZED | 이메일 또는 비밀번호가 올바르지 않습니다. | 로그인 인증 실패. | BusinessException |
| `TOKEN_EXPIRED` | 401 UNAUTHORIZED | 인증이 만료되었습니다. 다시 로그인해주세요. | JWT Access Token 만료. | BusinessException |
| `REFRESH_TOKEN_INVALID` | 401 UNAUTHORIZED | 세션이 만료되었습니다. 다시 로그인해주세요. | Refresh Token이 유효하지 않거나 DB 불일치. | BusinessException |
| `SOCIAL_AUTH_FAILED` | 401 UNAUTHORIZED | 소셜 로그인에 실패했습니다. 다시 시도해주세요. | 소셜 프로바이더 인증 코드 교환 실패. | BusinessException |
| `PROFILE_ALREADY_COMPLETE` | 400 BAD_REQUEST | 이미 프로필이 완성된 계정입니다. | isProfileComplete=true인 사용자가 complete-profile 호출. | BusinessException |
| `ACCOUNT_DEACTIVATED` | 401 UNAUTHORIZED | 탈퇴한 계정입니다. | isDeleted=true인 사용자 로그인 시도. | BusinessException |
| `EMAIL_ALREADY_REGISTERED` | 409 CONFLICT | 이미 가입된 이메일입니다. | 회원가입 시 이메일 중복. | BusinessException |

### 9.2 Fallback Mapping Additions

Add to `GlobalExceptionHandler` fallback handler:

| Exception Class | Maps To |
|----------------|---------|
| `BadCredentialsException` | `INVALID_CREDENTIALS` |
| `DisabledException` | `ACCOUNT_DEACTIVATED` |
| `LockedException` | `ACCOUNT_DEACTIVATED` |
| `AccessDeniedException` | `RESOURCE_NOT_ACCESS` (existing) |

---

## 10. Endpoint Access Rules (Complete)

### 10.1 PUBLIC (No Auth Required)

| Method | Endpoint |
|--------|----------|
| POST | `/api/users` |
| POST | `/api/auth/login` |
| POST | `/api/auth/social/{provider}` |
| POST | `/api/auth/refresh` |
| GET | `/api/utils/check-email` |
| GET | `/api/utils/check-phone` |
| GET | `/api/utils/check-nickname` |
| GET | `/api/tracks` |
| GET | `/api/tracks/{trackId}` |
| GET | `/api/tracks/{trackId}/stream` |
| GET | `/api/tags` |
| GET | `/api/subscriptions` |
| GET | `/api/subscriptions/{subscriptionId}` |
| GET | `/api/notices` |
| GET | `/api/notices/{noticeId}` |
| GET | `/swagger-ui/**` |
| GET | `/v3/api-docs/**` |

### 10.2 AUTH REQUIRED (Any Authenticated User)

All `/api/**` endpoints not listed in PUBLIC or ADMIN sections.

Key examples:
- `PUT /api/users/me/complete-profile`
- `GET /api/users/me`
- `PUT /api/users/me`
- `DELETE /api/users/me`
- `GET/POST/DELETE /api/likes/**`
- `GET/POST/DELETE /api/download-queue/**`
- `GET/POST/DELETE /api/play-histories/**`
- `GET/POST/PUT/DELETE /api/playlists/**` (subscriber check at service layer)
- `GET/POST /api/user-subscriptions/me`
- `DELETE /api/user-subscriptions/me`
- `PUT /api/user-subscriptions/me`
- `GET /api/licenses/**`
- `GET/POST/DELETE /api/questions/**`
- `GET/POST/PUT/DELETE /api/whitelist-channels/**`
- `POST /api/business-licenses`
- `GET /api/business-licenses/me`
- `GET /api/utils/subscription-status`
- `GET /api/utils/download-count`
- `GET /api/utils/user-type`
- `GET /api/tracks/{trackId}/download` (subscriber check at service layer)

### 10.3 ADMIN ONLY

| Method | Endpoint |
|--------|----------|
| GET | `/api/users` |
| GET | `/api/users/{userId}` |
| PUT | `/api/users/{userId}` |
| POST | `/api/tracks` |
| PUT | `/api/tracks/{trackId}` |
| DELETE | `/api/tracks/{trackId}` |
| POST | `/api/tags` |
| PUT | `/api/tags/{tagId}` |
| DELETE | `/api/tags/{tagId}` |
| GET | `/api/user-subscriptions` (list all) |
| GET | `/api/user-subscriptions/{userSubscriptionId}` |
| PUT | `/api/user-subscriptions/{userSubscriptionId}` |
| DELETE | `/api/user-subscriptions/{userSubscriptionId}` |
| GET | `/api/users/{userId}/licenses` |
| GET | `/api/users/{userId}/licenses/{licenseId}` |
| POST | `/api/notices` |
| PUT | `/api/notices/{noticeId}` |
| DELETE | `/api/notices/{noticeId}` |
| GET | `/api/business-licenses` (list all) |
| GET | `/api/business-licenses/{requestId}` |
| PUT | `/api/business-licenses/{requestId}` |
| PUT | `/api/questions/{questionId}/status` |

**Note**: "subscribers only" and "owner only" checks are enforced at the **service layer**, not at the SecurityFilterChain level. The filter chain only checks authentication and ADMIN role.

---

## 11. Checklist

- [x] api-spec.md의 모든 [PUBLIC]/[auth]/[ADMIN] 엔드포인트가 SecurityFilterChain에 포함 (Section 10)
- [x] JWT 페이로드에 userId, email, role 포함 (Section 2.1 - `sub`, `email`, `role` claims)
- [x] Access Token TTL (1h = 3,600,000ms) 및 Refresh Token TTL (14d = 1,209,600,000ms) 구체적 값 명시 (Section 2.2)
- [x] users.refresh_token 컬럼 DDL 포함 (Section 3.2)
- [x] 소셜 3개 provider (Google/Kakao/Naver) 각각의 OAuth2 엔드포인트 포함 (Section 6.2)
- [x] isProfileComplete=false 판단 및 처리 흐름 명시 (Section 6.4)
- [x] 로그아웃/탈퇴 시 refresh_token null 처리 명시 (Section 3.3)
- [x] 패키지 구조 트리 포함 (Section 7)

---

## 12. Handoff Points

### 12.1 For PG-002 (Security Review)

Items requiring PG review before implementation:

1. **JWT TTL values**: Are 1h access / 14d refresh appropriate for ATStudio's threat model?
2. **Refresh token hashing**: BCrypt used for hashing. Is the strength (10) sufficient? Should we use SHA-256 instead for faster validation?
3. **CORS whitelist**: Currently hardcoded localhost origins. Need production domain plan.
4. **OAuth2 client secrets**: 6 new environment variables needed (client-id + client-secret per provider). Confirm secret management approach.
5. **Single-session limitation**: Acceptable risk? Or should multi-device be supported from the start?
6. **Token in response body**: Tokens returned in JSON body (not cookies). Frontend stores in memory + httpOnly cookie for refresh token (per frontend-standards.md). Verify this matches frontend plan.

### 12.2 For SE-003 (Auth Implementation)

Implementation order recommendation:

1. **Phase 1**: JwtConfig + JwtTokenProvider + unit tests
2. **Phase 2**: CustomUserDetails + CustomUserDetailsService + unit tests
3. **Phase 3**: JwtAuthenticationFilter + SecurityConfig redesign + integration tests
4. **Phase 4**: AuthService (login + refresh) + AuthController + integration tests
5. **Phase 5**: OAuth2Service (start with Google, then Kakao/Naver) + integration tests
6. **Phase 6**: User entity modification (refreshToken field) + migration DDL

Key implementation notes:
- Use JJWT 0.12.5 API (already in build.gradle lines 55-57)
- `Jwts.builder()` and `Jwts.parser()` are the 0.12.x API (not deprecated `Jwts.parserBuilder()`)
- Spring Boot 4.x uses `spring-boot-starter-security` which includes Spring Security 6
- `RestClient` (Spring Boot 4.x) preferred over `RestTemplate` for OAuth2 provider HTTP calls
- `hasRole("ADMIN")` automatically prepends `ROLE_` prefix, so `CustomUserDetails.getAuthorities()` must return `ROLE_ADMIN`

### 12.3 For RE-004 (Auth Testing)

Critical test scenarios:

1. **JwtTokenProvider**: generate, parse, validate, expired token, tampered token
2. **JwtAuthenticationFilter**: valid token, no token, expired token, malformed header
3. **SecurityFilterChain**: PUBLIC endpoints accessible without token, protected endpoints return 401, ADMIN endpoints return 403 for USER role
4. **AuthService.login()**: valid credentials, wrong password, deleted user, non-existent email
5. **AuthService.refresh()**: valid refresh, expired refresh, DB mismatch (stolen token scenario)
6. **OAuth2Service**: mock provider responses, new user creation, existing user linking

### 12.4 Referenced Documents

| Document | Path | What was used |
|----------|------|---------------|
| core-principles.md | `docs/standards/core-principles.md` | Constitutional baseline, security principles |
| development-standards.md | `docs/standards/development-standards.md` | Layer architecture, naming (abbreviation-preserving camelCase), code templates |
| security-policy.md | `docs/policies/security-policy.md` | JWT env vars, BCrypt strength, CSRF/CORS rules |
| dto-standards.md | `docs/standards/dto-standards.md` | Request/Response DTO patterns, ResponseDTO wrapper |
| exception-handling.md | `docs/standards/exception-handling.md` | Error code taxonomy, dual-message pattern |
| api-spec.md | `docs/design/api-spec.md` | Endpoint list, auth levels, request/response formats |
| schema.sql | `src/main/resources/schema.sql` | users table DDL, social_accounts DDL |
| User.java | `src/main/java/.../entity/User.java` | Current entity fields |
| SocialAccount.java | `src/main/java/.../entity/SocialAccount.java` | Provider + providerId structure |
| SecurityConfig.java | `src/main/java/.../config/SecurityConfig.java` | Current permissive config to replace |
| build.gradle | `build.gradle` | JJWT 0.12.5 dependency confirmation (lines 55-57) |
