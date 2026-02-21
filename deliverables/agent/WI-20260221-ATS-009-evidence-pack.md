# WI-20260221-ATS-009 Evidence Pack

## WI Summary
Notice CRUD API implementation -- ADMIN create/update/delete, PUBLIC list/detail with pinned-first sorting.

## Files Created

| File | Lines | Description |
|------|-------|-------------|
| `src/main/java/com/atstudio/atstudio/dto/notice/NoticeCreateRequest.java` | 11 | Request record: title, content, isPinned with validation |
| `src/main/java/com/atstudio/atstudio/dto/notice/NoticeUpdateRequest.java` | 9 | Partial update record: all fields nullable |
| `src/main/java/com/atstudio/atstudio/dto/notice/NoticeListItemResponse.java` | 22 | List item response record with `from(Notice)` factory |
| `src/main/java/com/atstudio/atstudio/dto/notice/NoticeResponse.java` | 25 | Detail response record with `from(Notice)` factory |
| `src/main/java/com/atstudio/atstudio/service/NoticeService.java` | 83 | Service: 5 methods (create, getNotices, getNotice, update, delete) |
| `src/main/java/com/atstudio/atstudio/controller/NoticeController.java` | 60 | Controller: 5 endpoints matching api-spec v5 Section 9 |

## Files Modified

| File | Change | Lines |
|------|--------|-------|
| `src/main/java/com/atstudio/atstudio/entity/Notice.java` | Added `update(String, String, Boolean)` domain method | 33-37 |
| `src/main/java/com/atstudio/atstudio/repository/NoticeRepository.java` | Added `findAllByOrderByIsPinnedDescCreatedAtDesc(Pageable)` query method + Page/Pageable imports | 4-5, 10 |

## Files NOT Modified (Already Correct)

| File | Reason |
|------|--------|
| `SecurityConfig.java` | Already has GET `/api/notices` and `/api/notices/*` as permitAll (lines 66-67), POST/PUT/DELETE as hasRole("ADMIN") (lines 80-82) |
| `BUSINESS_ERROR.java` | `RESOURCE_NOT_FOUND` already exists and is reused for all not-found cases |

## Design Decisions

| Decision | Rationale |
|----------|-----------|
| Reuse `RESOURCE_NOT_FOUND` | No notice-specific not-found needed; generic error code sufficient per exception-handling.md |
| `@Transactional(readOnly = true)` on class, `@Transactional` on writes | Follows TagService/LicenseService pattern |
| Pagination follows LicenseService pattern | `ResponseDTO.builder().dataList().pageInfo(PageInfo.of()).build()` |
| `NoticeCreateRequest` uses `@NotNull Boolean isPinned` | Explicit required field; entity default `false` only applies when builder not given value |
| `NoticeUpdateRequest` all fields nullable | Partial update support via `notice.update()` null-check pattern |
| Notice entity `update()` method added | Required for JPA dirty checking partial update pattern |
| `@JsonInclude(NON_NULL)` on response records | Per dto-standards.md Section 4.2 |
| No `updatedAt` in create response | api-spec 9.1 response does not include updatedAt; however NoticeResponse includes it for reuse in getNotice/updateNotice -- NON_NULL will exclude it when null |

## API Endpoint Summary

| Method | Path | Auth | Status | Description |
|--------|------|------|--------|-------------|
| POST | `/api/notices` | ADMIN | 201 | Create notice |
| GET | `/api/notices` | PUBLIC | 200 | List notices (isPinned DESC, createdAt DESC) |
| GET | `/api/notices/{noticeId}` | PUBLIC | 200 | Get notice detail |
| PUT | `/api/notices/{noticeId}` | ADMIN | 200 | Update notice (partial) |
| DELETE | `/api/notices/{noticeId}` | ADMIN | 204 | Delete notice (physical) |

## Build Verification

- **Command**: `gradlew.bat build -x test`
- **Status**: Pending -- Bash tool access denied during execution. Manual verification required.

## Reproduction Steps

1. Run `gradlew.bat build -x test` from project root
2. Fix any compilation errors if present
3. Verify endpoints with Swagger UI at `/swagger-ui/index.html`

## Rollback Notes

- SecurityConfig: No changes made (rules already existed)
- To rollback: delete files in `dto/notice/`, `service/NoticeService.java`, `controller/NoticeController.java`
- Revert `Notice.java` (remove lines 33-37: `update()` method)
- Revert `NoticeRepository.java` (remove `findAllByOrderByIsPinnedDescCreatedAtDesc` and Page/Pageable imports)
