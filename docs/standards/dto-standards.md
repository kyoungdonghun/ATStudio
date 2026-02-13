---
version: 1.0
last_updated: 2026-02-13
project: ATS
owner: SA
category: standard
status: stable
dependencies:
  - path: development-standards.md
    reason: Layer architecture and coding standards
  - path: exception-handling.md
    reason: ExceptionResponseDTO definition
tier: 1
target_agents:
  - sa
  - se
  - cr
  - qa
task_types:
  - implementation
  - review
---

# DTO Standards

> **Purpose:** Define the data transfer object design rules for ATStudio, ensuring consistent data flow between layers and between client/server.

---

## 1. Entity / DTO Separation

### 1.1 Core Rule

Entities and DTOs serve fundamentally different purposes and must never be interchanged.

| Aspect | Entity | DTO |
|--------|--------|-----|
| Purpose | JPA persistence, DB mapping | Data transfer between layers |
| Annotations | `@Entity`, `@Column`, `@Id` | `@Getter`, `@Builder` (request: + `@Setter`) |
| Setter | **Never provided** | Request DTO: yes, Response DTO: no |
| Fields | `private` | `private` (accessed via getter/setter) |
| Business logic | None (data holder) | None (data carrier) |
| Exposure | Never returned from Controller | Used for API input/output |

### 1.2 Entity Rules

```java
@Entity
@Table(name = "music")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Music extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;
}
```

- No `@Setter` — state changes through domain methods or builder at creation time.
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` — JPA requires no-arg constructor, but prevent public instantiation.
- Extend `BaseEntity` for `createdAt` / `updatedAt` audit fields.

### 1.3 DTO Rules

- All fields `private` — use Lombok annotations for access.
- **Request DTO:** `@Getter @Setter @NoArgsConstructor` (mutable for binding).
- **Response DTO:** `@Getter @Builder @AllArgsConstructor` (immutable) or Java 17 `record`.

```java
// Request DTO — mutable (Spring binding needs setter)
@Getter @Setter
@NoArgsConstructor
public class MusicCreateRequest {
    private String title;
    private String genre;
}

// Response DTO — immutable (prefer record for simple cases)
public record MusicResponse(
    Long id,
    String title,
    String creatorName,
    LocalDateTime createdAt
) {
    public static MusicResponse from(Music music) {
        return new MusicResponse(
            music.getId(),
            music.getTitle(),
            music.getCreator().getName(),
            music.getCreatedAt()
        );
    }
}
```

### 1.4 Mapping Rule: Service Layer Responsibility

Entity-DTO conversion happens **only in the Service layer**. DTOs must not contain `toEntity()` methods.

```java
// ✅ Correct — Service handles mapping
@Service
public class MusicService {
    public MusicResponse createMusic(MusicCreateRequest request) {
        Music music = Music.builder()
            .title(request.getTitle())
            .genre(request.getGenre())
            .build();
        return MusicResponse.from(musicRepository.save(music));
    }
}

// ❌ Wrong — DTO should not know about Entity
public class MusicCreateRequest {
    public Music toEntity() { ... } // Forbidden
}
```

**Rationale:** Keeps dependency direction clean (Service knows both, DTO knows neither Entity nor Repository).

---

## 2. BaseEntity / BaseDTO

### 2.1 BaseEntity

Provides common audit fields for all JPA entities via JPA Auditing.

```java
@MappedSuperclass
@Getter
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

### 2.2 BaseDTO

Provides common timestamp fields for DTOs that need to carry audit information.

```java
@Getter @Setter
public abstract class BaseDTO {
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

## 3. RequestDTO (Pagination + Search Standard)

A common base for paginated/searchable API requests.

```java
@Getter @Setter
public class RequestDTO {
    private int page = 1;
    private int size = 10;
    private String keyword;
    private String type;

    public Pageable getPageable() {
        return PageRequest.of(
            Math.max(0, this.page - 1),
            Math.max(1, this.size)
        );
    }
}
```

### 3.1 Domain-Specific Extension (Inheritance)

Additional search conditions are added via inheritance, not `Map<String, String>`.

```java
@Getter @Setter
public class MusicSearchRequest extends RequestDTO {
    private String genre;
    private Integer minBpm;
    private Integer maxBpm;
}
```

**Rationale:** Type-safe at compile time. Invalid keys are caught before runtime.

---

## 4. ResponseDTO

A generic wrapper for all API responses. Void-returning endpoints are the only exception.

### 4.1 Structure

```java
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDTO<E> {

    private E data;
    private String message;
    private List<E> dataList;
    private PageInfo pageInfo;

    @Builder(builderMethodName = "withMessage")
    public ResponseDTO(String message) {
        this.message = message;
    }

    @Builder(builderMethodName = "withSingleData")
    public ResponseDTO(String message, E data) {
        this.message = message;
        this.data = data;
    }

    @Builder(builderMethodName = "withAll")
    public ResponseDTO(List<E> dataList, PageInfo pageInfo) {
        this.dataList = dataList;
        this.pageInfo = pageInfo;
    }
}
```

### 4.2 Key Decisions

| Decision | Rule | Rationale |
|----------|------|-----------|
| `@JsonInclude` | `NON_NULL` (not `NON_DEFAULT`) | `NON_DEFAULT` hides meaningful `0` and `false` values |
| Pagination | Separate `PageInfo` object | ResponseDTO stays focused on data; page calculation is a separate concern |
| Field naming | `data` / `dataList` | Clear singular vs plural distinction |

### 4.3 PageInfo (Separated from ResponseDTO)

```java
@Getter
@Builder
@AllArgsConstructor
public class PageInfo {
    private int page;
    private int size;
    private int total;
    private int start;
    private int end;
    private boolean prev;
    private boolean next;

    public static PageInfo of(RequestDTO request, int total) {
        return of(request.getPage(), request.getSize(), total, 10);
    }

    public static PageInfo of(int page, int size, int total, int blockSize) {
        int end = (int) (Math.ceil(page / (double) blockSize)) * blockSize;
        int start = end - blockSize + 1;
        int last = (int) (Math.ceil(total / (double) size));
        end = Math.min(end, last);

        return PageInfo.builder()
            .page(page)
            .size(size)
            .total(total)
            .start(start)
            .end(end)
            .prev(start > 1)
            .next(total > end * size)
            .build();
    }
}
```

**Rationale:** Block size is a parameter (default 10), not hardcoded. UI can request different block sizes without modifying the DTO.

### 4.4 Usage Examples

```java
// Message only
return ResponseDTO.withMessage().message("User created").build();

// Single data
return ResponseDTO.<UserDTO>withSingleData()
    .message("User fetched")
    .data(userDTO)
    .build();

// List with pagination
return ResponseDTO.<MusicDTO>withAll()
    .dataList(musicDTOs)
    .pageInfo(PageInfo.of(requestDTO, totalCount))
    .build();
```

---

## 5. ExceptionResponseDTO

Standardized error response format. See [exception-handling.md](exception-handling.md) for full strategy.

```java
@Getter
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExceptionResponseDTO {
    private int status;
    private String error;
    private String message;
}
```

---

## 6. Composite DTO Pattern

When a DTO needs to aggregate data from multiple domains, use **composition** (not inheritance).

```java
// Composite DTO — aggregates related DTOs
@Getter
@Builder
public class UserDetailsDTO implements UserDetails {
    private Long id;
    private String email;
    private String password;
    private List<GrantedAuthority> authorities;
}
```

### 6.1 Rules

- Use composition: a DTO contains other DTOs as fields.
- Each component DTO remains independently usable.
- **Security DTOs (e.g., UserDetailsDTO):** Include only authentication-minimum fields. Business data (subscriptions, preferences) is loaded separately when needed.
- Term: Use **"Composite DTO"**, not "Nested DTO" (which implies Java inner classes).

---

## 7. Summary Table

| DTO Type | Mutability | Annotations | Use Case |
|----------|-----------|-------------|----------|
| Request DTO | Mutable | `@Getter @Setter @NoArgsConstructor` | API request binding |
| Response DTO | Immutable | `@Getter @Builder` or `record` | API response |
| ResponseDTO\<E\> | Immutable | Generic wrapper | Standardized API envelope |
| ExceptionResponseDTO | Immutable | `@Getter @Builder` | Error responses |
| BaseDTO | Mutable | `@Getter @Setter` | Abstract parent for timestamps |
| Composite DTO | Varies | Composition of other DTOs | Aggregated domain data |