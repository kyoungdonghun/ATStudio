# WI-20260221-ATS-014 Evidence Pack

## Metadata
- WI: WI-20260221-ATS-014
- REQ: REQ-20260221-ATS-003
- Agent: se
- Date: 2026-02-21
- Files Modified: 4

---

## Fix 1 -- [C-01] TagController.createTag() @PreAuthorize

**File:** `src/main/java/com/atstudio/atstudio/controller/TagController.java`
**Line:** 25

### Before
```java
    @PostMapping
    public ResponseEntity<ResponseDTO<TagResponse>> createTag(
```

### After
```java
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<TagResponse>> createTag(
```

**Rationale:** `createTag()` is a write operation that was missing authorization, while `updateTag()` (line 43) and `deleteTag()` (line 55) already had `@PreAuthorize`. No new import needed -- `PreAuthorize` already imported at line 12.

---

## Fix 2 -- [M-06] NoticeController ResponseDTO Wrapper

**File:** `src/main/java/com/atstudio/atstudio/controller/NoticeController.java`
**Lines:** 27-36, 46-52, 56-64

### Before (createNotice, line 27)
```java
    public ResponseEntity<NoticeResponse> createNotice(
            @Valid @RequestBody NoticeCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(noticeService.createNotice(request, userDetails));
    }
```

### After (createNotice, line 27)
```java
    public ResponseEntity<ResponseDTO<NoticeResponse>> createNotice(
            @Valid @RequestBody NoticeCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        NoticeResponse response = noticeService.createNotice(request, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.<NoticeResponse>withSingleData()
                        .message("Notice created")
                        .data(response)
                        .build());
    }
```

### Before (getNotice, line 42)
```java
    public ResponseEntity<NoticeResponse> getNotice(@PathVariable Long noticeId) {
        return ResponseEntity.ok(noticeService.getNotice(noticeId));
    }
```

### After (getNotice, line 46)
```java
    public ResponseEntity<ResponseDTO<NoticeResponse>> getNotice(@PathVariable Long noticeId) {
        NoticeResponse response = noticeService.getNotice(noticeId);
        return ResponseEntity.ok(ResponseDTO.<NoticeResponse>withSingleData()
                .message("Notice retrieved")
                .data(response)
                .build());
    }
```

### Before (updateNotice, line 48)
```java
    public ResponseEntity<NoticeResponse> updateNotice(
            @PathVariable Long noticeId,
            @Valid @RequestBody NoticeUpdateRequest request) {
        return ResponseEntity.ok(noticeService.updateNotice(noticeId, request));
    }
```

### After (updateNotice, line 56)
```java
    public ResponseEntity<ResponseDTO<NoticeResponse>> updateNotice(
            @PathVariable Long noticeId,
            @Valid @RequestBody NoticeUpdateRequest request) {
        NoticeResponse response = noticeService.updateNotice(noticeId, request);
        return ResponseEntity.ok(ResponseDTO.<NoticeResponse>withSingleData()
                .message("Notice updated")
                .data(response)
                .build());
    }
```

**Rationale:** Aligns with project DTO standard (docs/standards/dto-standards.md). `ResponseDTO` was already imported at line 3. Pattern matches `TagController` and `LikeController` usage. `getNotices()` already used `ResponseDTO` -- left unchanged.

---

## Fix 3 -- [m-07] PlayHistoryService size Validation

**File:** `src/main/java/com/atstudio/atstudio/service/PlayHistoryService.java`
**Line:** 51

### Before
```java
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);
```

### After
```java
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, size));
```

**Rationale:** `PageRequest.of()` throws `IllegalArgumentException` if `size < 1`. This guard prevents 400/500 errors from malformed client requests.

---

## Fix 4 -- [m-08] NoticeService size Validation

**File:** `src/main/java/com/atstudio/atstudio/service/NoticeService.java`
**Line:** 49

### Before
```java
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);
```

### After
```java
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, size));
```

**Rationale:** Same as Fix 3 -- defensive guard against `size <= 0`.

---

## Acceptance Criteria Checklist

- [x] TagController.createTag() has `@PreAuthorize("hasRole('ADMIN')")` (line 25)
- [x] NoticeController.createNotice() returns `ResponseEntity<ResponseDTO<NoticeResponse>>` (line 27)
- [x] NoticeController.getNotice() returns `ResponseEntity<ResponseDTO<NoticeResponse>>` (line 46)
- [x] NoticeController.updateNotice() returns `ResponseEntity<ResponseDTO<NoticeResponse>>` (line 56)
- [x] PlayHistoryService line 51: `PageRequest.of(Math.max(0, page - 1), Math.max(1, size))`
- [x] NoticeService line 49: `PageRequest.of(Math.max(0, page - 1), Math.max(1, size))`
- [x] No new imports added (all already present)
- [x] No test files modified
- [x] No files outside scope modified

## Follow-up
- WI-20260221-ATS-017 (qa): Build compilation and test regression verification
