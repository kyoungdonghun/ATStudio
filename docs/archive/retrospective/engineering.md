# Engineering Lessons

> Framework: Java 17 + Spring Boot 4.x + Spring Data JPA + JUnit5
> Discovered during ATStudio backend implementation and audit phases.

---

## Spring Security

### Rule Ordering (CRITICAL)

```java
// ❌ WRONG — wildcard catches /api/users/me before the specific rule
.requestMatchers("/api/users/*").hasRole("ADMIN")
.requestMatchers("/api/users/me").authenticated()

// ✅ CORRECT — specific first, wildcard after
.requestMatchers("/api/users/me").authenticated()
.requestMatchers("/api/users/*").hasRole("ADMIN")
```

**Lesson**: Spring Security rules match top-to-bottom. Always place specific paths before wildcards. A single ordering mistake silently blocks legitimate users — and `@WithMockUser` in MockMvc tests bypasses SecurityConfig entirely, so integration tests won't catch it.

### Testing Security Config

- `@WithMockUser` in `@WebMvcTest` bypasses `SecurityFilterChain` → cannot detect rule ordering bugs
- Requires `@SpringBootTest + @AutoConfigureMockMvc` with real security loaded
- Consider a dedicated `SecurityFilterChainTest` class that verifies each endpoint's auth requirement

---

## JPA / Hibernate

### @Transactional Standard

```java
// ✅ Class-level default: readOnly=true (most methods are reads)
@Service
@Transactional(readOnly = true)
public class SomeService {

    // Override only for mutations
    @Transactional
    public void create(...) { ... }

    @Transactional
    public void update(...) { ... }

    // No annotation needed for reads — inherits class-level readOnly
    public SomeDto findById(Long id) { ... }
}
```

**Lesson**: Applying `@Transactional` without `readOnly=true` on read methods disables query optimization. Default to `readOnly=true` at class level and override only for writes.

### N+1 Prevention

```java
// ❌ Causes N+1 on List/Page queries with LAZY associations
List<Track> findAll(Specification<Track> spec, Pageable pageable);

// ✅ Force eager join via @EntityGraph
@EntityGraph(attributePaths = {"trackTags", "trackTags.tag"})
List<Track> findAll(Specification<Track> spec, Pageable pageable);
```

**Caveat**: `@EntityGraph + Pageable` triggers Hibernate warning `HHH90003004` (in-memory pagination) when the eager association produces multiple rows per root entity. Above ~10K records, switch to a two-query pattern: fetch IDs first (paginated), then fetch full objects by ID list.

### Composite PK (Many-to-Many)

```java
// ✅ Pattern for join-table entities (e.g., AlbumTrack, PlaylistTrack)
@Embeddable
public class AlbumTrackId implements Serializable {
    private Long albumId;
    private Long trackId;
}

@Entity
public class AlbumTrack {
    @EmbeddedId
    private AlbumTrackId id;
    // ...
}
```

**Critical**: `save()` on a composite-PK entity calls `merge()` internally. If the record already exists, it silently updates instead of throwing. Always call `existsById()` explicitly before inserting if duplicate prevention is required.

### Cascade Delete Order

When deleting a parent entity with child records across multiple tables, the order matters:

```
Attachment → Answer → Question (correct cascade order)
```

Define this explicitly in `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)` or handle in service layer. Do not rely on DB-level cascade unless explicitly defined in schema.

### JPA Criteria API Pitfall

```java
// ❌ Runtime IllegalArgumentException if "fieldName" doesn't exist on entity
root.join("fieldName")

// ✅ Verify field name matches @Entity field (not column name)
root.join("trackTags")
```

Mockito's `any(Specification.class)` does not execute the lambda — the bug is invisible in unit tests. Requires `@DataJpaTest` to catch.

---

## Exception Handling

### AccessDeniedException in GlobalExceptionHandler

```java
// ❌ AccessDeniedException is a RuntimeException — caught by catch-all
@ExceptionHandler(Exception.class)
public ResponseEntity<?> handleAll(Exception e) {
    // AccessDeniedException gets here and returns 500 instead of 403
}

// ✅ Explicit branch in catch-all
@ExceptionHandler(Exception.class)
public ResponseEntity<?> handleAll(Exception e) {
    if (e instanceof AccessDeniedException) {
        return ResponseEntity.status(403).build();
    }
    // ...
}
```

Or better: add a dedicated `@ExceptionHandler(AccessDeniedException.class)` that runs before the catch-all.

---

## Testing

### @WithMockUser + CustomUserDetails

```java
// ❌ @WithMockUser creates a UsernamePasswordAuthenticationToken,
//    not a CustomUserDetails — @AuthenticationPrincipal CustomUserDetails = null
@Test
@WithMockUser(roles = "USER")
void test() {
    // service.someMethod(userDetails) → NullPointerException
}

// ✅ Mock the service method to not depend on userDetails,
//    or use @WithUserDetails with a UserDetailsService bean
```

### Spring Boot 4.x Package Change

```java
// Spring Boot 4.x (not 3.x)
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
// NOT: org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
```

### enum valueOf() Safety

```java
// ❌ Throws IllegalArgumentException → 500 on invalid input
UserType type = UserType.valueOf(requestString);

// ✅ Catch and return 400
try {
    UserType type = UserType.valueOf(requestString);
} catch (IllegalArgumentException e) {
    throw new BusinessException(INVALID_USER_TYPE); // → 400
}
```

---

## URL Validation

```java
// ✅ Use URI parsing — handles both http and https, rejects malformed URLs
try {
    URI uri = new URI(url);
    if (!List.of("http", "https").contains(uri.getScheme())) {
        throw new BusinessException(INVALID_URL);
    }
} catch (URISyntaxException e) {
    throw new BusinessException(INVALID_URL);
}
```

---

## Password Update

Always verify the current password (BCrypt) before allowing a change:

```java
public void updatePassword(Long userId, String currentPassword, String newPassword) {
    User user = findById(userId);
    if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
        throw new BusinessException(INVALID_PASSWORD); // 401
    }
    user.updatePassword(passwordEncoder.encode(newPassword));
}
```

---

## Soft Delete + Auth Query

When using `is_deleted` soft delete, ensure all `findByEmail`, `findByPhone`, etc. queries exclude deleted accounts:

```java
// ✅ Explicit filter
Optional<User> findByEmailAndIsDeletedFalse(String email);
```

Failing to do this leaks deleted user PII in admin list APIs and allows re-login with deleted credentials.
