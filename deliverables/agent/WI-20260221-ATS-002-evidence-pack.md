# WI-20260221-ATS-002 Evidence Pack: Track/Tag Domain Design

> **Agent:** SA (Software Architect)
> **Date:** 2026-02-21
> **Status:** Complete
> **REQ:** REQ-20260221-ATS-001
> **Constraint:** Design/specification only. No code written. No entity modifications.

---

## 1. StorageService Interface Signature

### 1.1 Decision: Interface Abstraction for File Storage

**Rationale:** The current `build.gradle` has AWS S3 dependency commented out (`//implementation 'io.awspring.cloud:spring-cloud-aws-starter-s3:3.1.0'`). The handoff explicitly requires local filesystem only, with S3-swappable interface abstraction.

**Interface: `StorageService`**

| Method | Signature | Description |
|--------|-----------|-------------|
| `store` | `String store(MultipartFile file, String directory)` | Stores file in the specified subdirectory. Returns the relative path (e.g., `tracks/audio/uuid-filename.mp3`). |
| `getUrl` | `String getUrl(String relativePath)` | Returns a URL/path that can be served to the client. For local: `/uploads/{relativePath}`. |
| `delete` | `void delete(String relativePath)` | Deletes the file at the given path. No-op if file does not exist. |

**Implementation: `LocalStorageService`**

- Annotated with `@Service` and `@Profile("local")` (or default profile).
- Base path configured via `application.yml`: `app.storage.base-path=uploads`
- File naming: UUID prefix + original filename to prevent collisions (e.g., `a1b2c3d4_summer-vibes.mp3`)
- Creates directories on demand if they do not exist.

### 1.2 File Storage Path Structure

```
uploads/
  tracks/
    audio/          <- Original audio files (for download)
    thumbnail/      <- Track thumbnail images
    preview/        <- Low-quality preview files (async-generated, future)
  questions/
    attachments/    <- Inquiry attachments (Section 8 scope, not this WI)
  business-docs/
    {userId}/       <- Business license documents (Section 13 scope)
```

**Evidence:**
- `api-spec.md` Section 1.1 Response shows paths like `/tracks/audio/summer-vibes.mp3` and `/tracks/thumbnail/summer-vibes.jpg`.
- `db-schema.md` Section 4.1: `audio_file VARCHAR(255)` and `thumbnail VARCHAR(255)` store relative paths.
- `db-schema.md` Section 3.1: Business docs use `/uploads/business-docs/{user_id}/` pattern.

### 1.3 WebConfig Static Resource Mapping

A `WebMvcConfigurer` resource handler must map `/uploads/**` to the local filesystem base path so files are servable via HTTP. This is a SE implementation detail, not an architectural decision.

---

## 2. DTO Field Specification

### 2.1 TrackCreateRequest (API Spec 1.1)

Based on `api-spec.md` Section 1.1 Request (multipart/form-data).

| Field | Type | Required | Validation | Notes |
|-------|------|----------|------------|-------|
| `title` | `String` | Yes | `@NotBlank`, `@Size(max=100)` | |
| `bpm` | `Integer` | Yes | `@NotNull`, `@Min(1)` | |
| `tonality` | `String` | Yes | `@NotBlank`, `@Size(max=10)` | e.g., C, Am, F#m |
| `description` | `String` | No | | |
| `audioFile` | `MultipartFile` | Yes | | Validated in service (size, type) |
| `thumbnail` | `MultipartFile` | No | | |
| `tagIds` | `List<Long>` | No | | |

**Design note:** This DTO uses `@Getter @Setter @NoArgsConstructor` (mutable request DTO pattern per `dto-standards.md` Section 1.3). It does NOT extend `RequestDTO` because there is no pagination context for creation.

### 2.2 TrackUpdateRequest (API Spec 1.6)

Based on `api-spec.md` Section 1.6 Request (multipart/form-data). All fields optional (partial update).

| Field | Type | Required | Validation | Notes |
|-------|------|----------|------------|-------|
| `title` | `String` | No | `@Size(max=100)` | |
| `bpm` | `Integer` | No | `@Min(1)` | |
| `tonality` | `String` | No | `@Size(max=10)` | |
| `description` | `String` | No | | |
| `audioFile` | `MultipartFile` | No | | Replaces existing file |
| `thumbnail` | `MultipartFile` | No | | Replaces existing file |
| `tagIds` | `List<Long>` | No | | Full replacement strategy |
| `isActive` | `Boolean` | No | | Activate/deactivate toggle |

### 2.3 TrackResponse (API Spec 1.3 -- Detail)

Based on `api-spec.md` Section 1.3 Response. Used for single track detail and create/update responses.

| Field | Type | Notes |
|-------|------|-------|
| `id` | `Long` | |
| `title` | `String` | |
| `bpm` | `int` | |
| `tonality` | `String` | |
| `description` | `String` | Nullable |
| `audioFile` | `String` | Relative path from StorageService |
| `thumbnail` | `String` | Nullable |
| `isActive` | `boolean` | |
| `playCount` | `long` | |
| `tags` | `List<TagResponse>` | Nested tag list |
| `createdAt` | `LocalDateTime` | |
| `updatedAt` | `LocalDateTime` | |

**Implementation:** Use Java `record` with a static `from(Track track, List<Tag> tags)` factory method (per `dto-standards.md` Section 1.3 and `development-standards.md` Section 2A.4).

### 2.4 TrackListItemResponse (API Spec 1.2 -- List Item)

Based on `api-spec.md` Section 1.2 `dataList` item. Lighter than TrackResponse (no `description`, no `audioFile`, no `updatedAt`).

| Field | Type | Notes |
|-------|------|-------|
| `id` | `Long` | |
| `title` | `String` | |
| `bpm` | `int` | |
| `tonality` | `String` | |
| `thumbnail` | `String` | Nullable |
| `playCount` | `long` | |
| `tags` | `List<TagResponse>` | Nested tag list |
| `createdAt` | `LocalDateTime` | |

### 2.5 TagCreateRequest (API Spec 2.1)

Based on `api-spec.md` Section 2.1 Request (JSON).

| Field | Type | Required | Validation | Notes |
|-------|------|----------|------------|-------|
| `name` | `String` | Yes | `@NotBlank`, `@Size(max=50)` | |
| `type` | `TagType` | Yes | `@NotNull` | MOOD / GENRE / INSTRUMENT |

**Reused for Tag update (2.3):** Same fields. PUT endpoint uses the same DTO.

### 2.6 TagResponse (API Spec 2.1/2.2)

| Field | Type | Notes |
|-------|------|-------|
| `id` | `Long` | |
| `name` | `String` | |
| `type` | `TagType` | Enum serialized as string (MOOD/GENRE/INSTRUMENT) |
| `createdAt` | `LocalDateTime` | Included in 2.1 create response; optional for 2.2 list |

**Note on 2.2 List Tags response:** The API spec returns a plain array `[{id, name, type}]` without `createdAt`. Two options:

- **Option A (Recommended):** Use the same `TagResponse` record with `createdAt`. Since `@JsonInclude(NON_NULL)` is on `ResponseDTO`, not on individual records, `createdAt` will always be present. This is acceptable -- the API spec shows a simplified example, and including `createdAt` adds negligible payload.
- **Option B:** Create a separate `TagListItemResponse` without `createdAt`. Adds unnecessary DTO proliferation for a 3-field object.

**Decision:** Option A. Single `TagResponse` record for all tag responses.

### 2.7 TrackSearchRequest (List Query Parameters)

Extends `RequestDTO` per `dto-standards.md` Section 3.1.

| Field | Type | Default | Notes |
|-------|------|---------|-------|
| `page` | `int` | 1 | Inherited from `RequestDTO` |
| `size` | `int` | 20 | Override default (RequestDTO default is 10, API spec default is 20) |
| `keyword` | `String` | null | Inherited `keyword` field from `RequestDTO` -- maps to title search |
| `genre` | `String` | null | Genre tag name filter |
| `mood` | `String` | null | Mood tag name filter |
| `instrument` | `String` | null | Instrument tag name filter |
| `bpmMin` | `Integer` | null | |
| `bpmMax` | `Integer` | null | |
| `tonality` | `String` | null | |
| `sort` | `String` | "latest" | "latest" or "popular" |

**Note:** The API spec uses tag name strings (e.g., `genre=Pop`), not tag IDs. The handoff mentioned `tagId` but the API spec Section 1.2 uses `genre: String`, `mood: String`, `instrument: String` as query parameters with tag name values. Following the API spec.

---

## 3. Track-Tag Association Management

### 3.1 Existing Infrastructure

The following entities already exist and are well-structured:

- `TrackTag` entity with `@EmbeddedId` (`TrackTagId`) -- `C:\Users\jm991\Desktop\project\ATStudio\src\main\java\com\atstudio\atstudio\entity\TrackTag.java`
- `TrackTagId` composite key (`trackId`, `tagId`) -- `C:\Users\jm991\Desktop\project\ATStudio\src\main\java\com\atstudio\atstudio\entity\key\TrackTagId.java`
- `TrackTagRepository` -- `C:\Users\jm991\Desktop\project\ATStudio\src\main\java\com\atstudio\atstudio\repository\TrackTagRepository.java`

### 3.2 Full Replacement Strategy

When creating or updating a track with `tagIds`, the service should use a **full replacement** (delete-all-then-insert) strategy:

1. **On Create (1.1):** For each `tagId` in the request, create a `TrackTag` entity and save.
2. **On Update (1.6):** If `tagIds` is provided (not null):
   - Delete all existing `TrackTag` records for the track: `TrackTagRepository.deleteAllByTrackId(trackId)`
   - Insert new associations for each provided `tagId`.
   - If `tagIds` is an empty list, all tags are removed.
   - If `tagIds` is null (not provided in the request), tags are left unchanged.

### 3.3 Required Repository Methods

`TrackTagRepository` needs the following additional methods:

| Method | Signature | Purpose |
|--------|-----------|---------|
| `deleteAllByTrack` | `void deleteAllByTrack(Track track)` | Bulk delete all tags for a track (update scenario) |
| `findAllByTrack` | `List<TrackTag> findAllByTrack(Track track)` | Fetch all tags for a track (response building) |

**N+1 Prevention:** When building `TrackResponse` with tags, use a `JOIN FETCH` query or `@EntityGraph` on the TrackTag -> Tag relationship. Recommended approach:

```
// TrackTagRepository
@Query("SELECT tt FROM TrackTag tt JOIN FETCH tt.tag WHERE tt.track = :track")
List<TrackTag> findAllWithTagByTrack(@Param("track") Track track);
```

### 3.4 Validation

- Each `tagId` in the request must be validated against the `tags` table. If any `tagId` does not exist, throw `BusinessException` with an appropriate error code.
- This validation happens in the Service layer before saving.

---

## 4. List Search/Filter Query Strategy

### 4.1 Option Analysis

| Option | Pros | Cons | Dependency |
|--------|------|------|------------|
| **A: JPQL (Named/Custom Query)** | No extra dependency. Simple for known queries. | Becomes verbose with many optional filters. Hard to compose dynamically. | None |
| **B: JPA Specification** | Built into Spring Data JPA. Dynamic composition of predicates. Type-safe with metamodel. | Slightly more boilerplate per filter. Learning curve for Criteria API. | `spring-boot-starter-data-jpa` (already present) |
| **C: QueryDSL** | Most fluent API. Excellent type safety. | Requires additional dependency + APT code generation plugin. Build complexity increases. | `querydsl-jpa`, `querydsl-apt` (NOT in `build.gradle`) |

### 4.2 Decision: Option B -- JPA Specification

**Rationale:**

1. **No additional dependencies required.** `build.gradle` already includes `spring-boot-starter-data-jpa` which provides `JpaSpecificationExecutor`. QueryDSL (Option C) would require adding `querydsl-jpa`, `querydsl-apt`, and configuring annotation processing in Gradle -- unnecessary complexity at this project stage.

2. **Dynamic filter composition fits the use case.** The track list API has 7+ optional filter parameters (keyword, genre, mood, instrument, bpmMin, bpmMax, tonality) plus sorting. JPQL (Option A) would require `WHERE 1=1 AND (:keyword IS NULL OR ...)` pattern or string concatenation, both fragile.

3. **Tag join filtering is manageable.** For genre/mood/instrument filters, the Specification can join `track_tags` + `tags` tables. While QueryDSL would be more readable, Specification is sufficient.

4. **Future migration path.** If query complexity grows significantly, migrating from Specification to QueryDSL is straightforward -- they use the same Criteria API underneath.

### 4.3 Implementation Approach

**TrackRepository** must extend `JpaSpecificationExecutor<Track>`:

```
public interface TrackRepository extends JpaRepository<Track, Long>, JpaSpecificationExecutor<Track> { }
```

**TrackSpecification** (new class in `repository` or `repository/spec` package):

A utility class with static methods that return `Specification<Track>`:

| Method | Filter Logic |
|--------|-------------|
| `isActive()` | `WHERE is_active = true` (always applied for public listing) |
| `titleContains(keyword)` | `WHERE title LIKE %keyword%` |
| `hasBpmBetween(min, max)` | `WHERE bpm BETWEEN min AND max` (handles null min/max independently) |
| `hasTonality(tonality)` | `WHERE tonality = tonality` |
| `hasTagWithNameAndType(name, type)` | JOIN `track_tags` JOIN `tags` WHERE `tags.name = name AND tags.type = type` |

**Sorting:**

| Sort Value | Order |
|-----------|-------|
| `latest` (default) | `ORDER BY created_at DESC` |
| `popular` | `ORDER BY play_count DESC` |

Sorting is applied via `Pageable` with `Sort` parameter, not inside the Specification.

### 4.4 Tag Name vs Tag ID Filtering

The API spec (Section 1.2) uses tag **names** as query parameters (`genre=Pop`, `mood=Happy`), not tag IDs. The Specification joins through `track_tags` to `tags` and filters by `tags.name` and `tags.type`.

**Design note:** If the same tag name exists across different types (unlikely but possible due to UNIQUE constraint on `tags.name`), the type-qualified filter (`hasTagWithNameAndType`) prevents ambiguity. However, since `tags.name` has a UNIQUE constraint in the DB schema, a single name can only belong to one type. The type parameter in the Specification serves as documentation and safety.

---

## 5. Daily Download Limit Query

### 5.1 DB Schema Reference

From `db-schema.md` Section 6.1 (`track_downloads`):
- INDEX: `(user_id, downloaded_at)` -- `idx_track_downloads_user_date`
- Already defined in entity: `C:\Users\jm991\Desktop\project\ATStudio\src\main\java\com\atstudio\atstudio\entity\TrackDownload.java` (line 12-13)

From `db-schema.md` Section 1.1 (daily download limit handling):
- `COUNT(*) WHERE user_id = ? AND DATE(downloaded_at) = CURDATE()`

### 5.2 TrackDownloadRepository Methods

| Method | Signature | Purpose |
|--------|-----------|---------|
| `countTodayDownloads` | `@Query("SELECT COUNT(td) FROM TrackDownload td WHERE td.user = :user AND td.downloadedAt >= :startOfDay") long countByUserAndDownloadedAtAfter(@Param("user") User user, @Param("startOfDay") LocalDateTime startOfDay)` | Count today's downloads for limit check |

**Implementation note:** Using `>= startOfDay` (midnight of current day) rather than MySQL `DATE()` function for JPA portability. The service calculates `LocalDate.now().atStartOfDay()` and passes it as parameter.

**Alternative considered:** `countByUserAndDownloadedAtBetween(User user, LocalDateTime start, LocalDateTime end)` -- slightly more explicit but functionally equivalent. The `>= startOfDay` approach is simpler since we only need a lower bound (downloads today cannot be in the future).

### 5.3 Download Flow (Service Level)

1. Verify user has active subscription (check `user_subscriptions` status = ACTIVE).
2. Get subscription plan's `download_per_day` limit.
3. If limit is `-1` (unlimited), skip count check.
4. Call `countTodayDownloads(user, today)`.
5. If count >= limit, throw `BusinessException(DOWNLOAD_LIMIT_EXCEEDED)`.
6. Save `TrackDownload` record.
7. Trigger license auto-issuance (Section 6).
8. Return file stream.

---

## 6. License Auto-Issuance Logic

### 6.1 DB Schema Reference

From `db-schema.md` Section 11.1 (`licenses`):
- UNIQUE constraint: `(user_id, track_id)` -- one license per user per track.
- `license_code VARCHAR(50) UNIQUE` -- UUID-based.
- Entity: `C:\Users\jm991\Desktop\project\ATStudio\src\main\java\com\atstudio\atstudio\entity\License.java`

From `api-spec.md` Section 1.5:
- "Saves download record + auto-issues license (does not re-issue if existing license exists)"

### 6.2 LicenseRepository Methods

| Method | Signature | Purpose |
|--------|-----------|---------|
| `findByUserAndTrack` | `Optional<License> findByUserAndTrack(User user, Track track)` | Check if license already exists for this user+track pair |

### 6.3 Auto-Issuance Flow

Called as part of the download flow (Section 5.3, step 7):

1. `LicenseRepository.findByUserAndTrack(user, track)`
2. **If present:** No action (idempotent -- existing license retained).
3. **If absent:** Create new `License` with:
   - `user`: current user
   - `track`: downloaded track
   - `licenseCode`: `UUID.randomUUID().toString()` (standard Java UUID, fits VARCHAR(50))
4. Save and return.

**Design note:** This is NOT a separate service method. It is an internal step within `TrackDownloadService.download()` (or `TrackService.download()`). The license issuance is a side effect of download, not an independent operation. The API spec explicitly removed the manual license issuance API.

### 6.4 Concurrency Consideration

The UNIQUE constraint `(user_id, track_id)` on the `licenses` table prevents duplicate licenses at the DB level. If two concurrent download requests for the same user+track occur:
- First request: `findByUserAndTrack` returns empty -> creates license.
- Second request: `findByUserAndTrack` might return empty (race condition) -> attempts to create license -> DB throws `DataIntegrityViolationException`.
- **Handling:** Catch `DataIntegrityViolationException` and treat as success (license already exists). This is the simplest approach without pessimistic locking.

---

## 7. ADMIN Permission Check

### 7.1 Existing Security Configuration

From `C:\Users\jm991\Desktop\project\ATStudio\src\main\java\com\atstudio\atstudio\config\SecurityConfig.java`:

- `@EnableMethodSecurity` is enabled (line 23) -- supports `@PreAuthorize`, `@Secured`, etc.
- URL-based authorization is already configured for Track and Tag endpoints:
  - `POST /api/tracks` -> `.hasRole("ADMIN")` (line 74)
  - `PUT /api/tracks/*` -> `.hasRole("ADMIN")` (line 75)
  - `DELETE /api/tracks/*` -> `.hasRole("ADMIN")` (line 76)
  - `POST /api/tags` -> `.hasRole("ADMIN")` (line 77)
  - `PUT /api/tags/*` -> `.hasRole("ADMIN")` (line 78)
  - `DELETE /api/tags/*` -> `.hasRole("ADMIN")` (line 79)

### 7.2 Decision: URL-Based Authorization (Already Sufficient)

**Two approaches available:**

| Approach | Implementation | Status |
|----------|---------------|--------|
| **URL-based** (SecurityFilterChain) | Already configured in `SecurityConfig` | Already done |
| **Method-level** (`@PreAuthorize("hasRole('ADMIN')")`) | Annotation on Controller methods | Optional redundancy |

**Decision:** The URL-based authorization in `SecurityConfig` is **sufficient and already complete** for Track and Tag endpoints. Adding `@PreAuthorize` on controller methods would be redundant but is acceptable as defense-in-depth.

**Recommendation for SE:** Do NOT add `@PreAuthorize` annotations on Track/Tag controller methods. The SecurityConfig already handles ADMIN checks. Adding annotations would create dual maintenance burden without meaningful security benefit. If future endpoints need finer-grained control (e.g., owner-only checks), method-level security can be introduced then.

### 7.3 Download Endpoint (Subscriber-Only)

The download endpoint (`GET /api/tracks/{trackId}/download`) requires:
1. **Authentication:** Handled by `.requestMatchers("/api/**").authenticated()` catch-all (line 88).
2. **Active subscription:** NOT handled by Spring Security. This is a **business rule** checked in the Service layer (query `user_subscriptions` for status = ACTIVE).

This is intentional -- subscription status is dynamic business logic, not a static role.

---

## 8. Architecture Decision Record (ADR)

### ADR-001: JPA Specification over QueryDSL for Track List Filtering

**Context:** Track list API requires dynamic filtering across 7+ optional parameters including cross-table tag joins.

**Decision:** Use JPA Specification (`JpaSpecificationExecutor`) for track list query composition.

**Alternatives Considered:**
1. JPQL with conditional parameters -- rejected due to poor maintainability with many optional filters.
2. QueryDSL -- rejected because it requires additional Gradle dependencies (`querydsl-jpa`, `querydsl-apt`) and annotation processing configuration not currently present in `build.gradle`.

**Consequences:**
- (+) Zero additional dependencies.
- (+) Type-safe predicate composition with metamodel.
- (-) Slightly more verbose than QueryDSL fluent API.
- (-) Complex joins (multiple tag type filters simultaneously) require careful predicate construction.

**Rollback:** If Specification becomes unwieldy, add QueryDSL dependency and migrate. The migration is incremental -- both can coexist.

### ADR-002: Local Filesystem Storage with Interface Abstraction

**Context:** File storage needed for track audio, thumbnails, and previews. S3 dependency is commented out in `build.gradle`.

**Decision:** Implement `StorageService` interface with `LocalStorageService` as the sole implementation. No S3 implementation at this phase.

**Alternatives Considered:**
1. Direct filesystem calls in service -- rejected because it couples business logic to storage mechanism.
2. Full S3 + local dual implementation -- rejected because S3 is not yet needed (dependency commented out).

**Consequences:**
- (+) Clean separation of concerns.
- (+) S3 migration requires only implementing `S3StorageService` and swapping the profile.
- (-) Local storage has no built-in redundancy.

**Rollback:** N/A -- interface pattern has no downside.

---

## 9. Constraints and Warnings for SE

### 9.1 Entity Modification Prohibition

Per WI constraints, existing entities (`Track`, `Tag`, `TrackTag`, `TrackTagId`, `TrackDownload`, `License`) must NOT be modified. All designs in this evidence pack work with the existing entity structure.

### 9.2 Multipart File Size Configuration

The `application.yml` must configure maximum file upload sizes:
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 50MB    # Per file
      max-request-size: 100MB # Total request
```
This is a configuration task, not an entity/code change.

### 9.3 TrackTag N+1 Risk

When building `TrackResponse` or `TrackListItemResponse` for list queries, loading tags for each track individually causes N+1. Two mitigation strategies:

1. **For single track detail (1.3):** `findAllWithTagByTrack` JOIN FETCH query (Section 3.3).
2. **For list (1.2):** After fetching the `Page<Track>`, batch-load all tags for the page's track IDs in a single query:
   ```
   @Query("SELECT tt FROM TrackTag tt JOIN FETCH tt.tag WHERE tt.track.id IN :trackIds")
   List<TrackTag> findAllWithTagByTrackIdIn(@Param("trackIds") List<Long> trackIds);
   ```
   Then group by track ID in the service layer.

### 9.4 Sort Parameter Mapping

The `sort` query parameter accepts string values ("latest", "popular") but Spring Data's `Pageable` uses `Sort` objects. The mapping should happen in the Controller or a custom `ArgumentResolver`:
- `"latest"` -> `Sort.by(Sort.Direction.DESC, "createdAt")`
- `"popular"` -> `Sort.by(Sort.Direction.DESC, "playCount")`

### 9.5 Tag List Response Format

API spec Section 2.2 returns a plain JSON array `[{...}]`, NOT wrapped in `ResponseDTO`. This is an exception to the standard `ResponseDTO` wrapper pattern. SE should confirm with the API spec whether to wrap or return raw array. The API spec shows a raw array, so the controller should return `ResponseEntity<List<TagResponse>>` directly.

---

## 10. File References

| File | Path | Relevance |
|------|------|-----------|
| Track entity | `src/main/java/com/atstudio/atstudio/entity/Track.java` | Existing entity -- no modification |
| Tag entity | `src/main/java/com/atstudio/atstudio/entity/Tag.java` | Existing entity -- no modification |
| TrackTag entity | `src/main/java/com/atstudio/atstudio/entity/TrackTag.java` | Existing entity -- no modification |
| TrackTagId | `src/main/java/com/atstudio/atstudio/entity/key/TrackTagId.java` | Composite PK -- no modification |
| TrackDownload entity | `src/main/java/com/atstudio/atstudio/entity/TrackDownload.java` | Download history -- no modification |
| License entity | `src/main/java/com/atstudio/atstudio/entity/License.java` | License -- no modification |
| TrackRepository | `src/main/java/com/atstudio/atstudio/repository/TrackRepository.java` | Needs `JpaSpecificationExecutor` |
| TrackTagRepository | `src/main/java/com/atstudio/atstudio/repository/TrackTagRepository.java` | Needs new query methods |
| TrackDownloadRepository | `src/main/java/com/atstudio/atstudio/repository/TrackDownloadRepository.java` | Needs count query |
| LicenseRepository | `src/main/java/com/atstudio/atstudio/repository/LicenseRepository.java` | Needs findByUserAndTrack |
| SecurityConfig | `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java` | ADMIN rules already configured |
| RequestDTO | `src/main/java/com/atstudio/atstudio/common/dto/RequestDTO.java` | Base for TrackSearchRequest |
| TagType enum | `src/main/java/com/atstudio/atstudio/entity/enums/TagType.java` | MOOD/GENRE/INSTRUMENT |
| build.gradle | `build.gradle` | No QueryDSL dependency present |
| API spec | `docs/design/api-spec.md` | Sections 1 and 2 |
| DB schema | `docs/design/db-schema.md` | Sections 4, 6, 11 |
| DTO standards | `docs/standards/dto-standards.md` | DTO design patterns |

---

## 11. Follow-Up WI Suggestions

| WI | Scope | Agent |
|----|-------|-------|
| WI-003 | Tag CRUD implementation (Controller + Service + DTOs) | SE |
| WI-004 | Track CRUD implementation (Controller + Service + DTOs + StorageService) | SE |
| WI-005 | Track list search/filter (Specification + TrackSearchRequest) | SE |
| WI-006 | Track download + license auto-issuance flow | SE |
| WI-007 | Unit tests for Track/Tag services | RE |
