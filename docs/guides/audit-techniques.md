---
version: 1.0
last_updated: 2026-03-13
project: ATS
owner: QA-INTEG
category: guide
status: stable
tier: 2
---

# Audit Techniques Catalog (ATStudio Domain Examples)

> This document records audit techniques with ATStudio-specific examples.
> Agent definitions (`.claude/agents/`) embed the portable checklists.
> This document is a **reference companion**, not a dependency.

---

## 1. Three-Way Verification (spec → code → spec)

### Definition
Validate that implementation matches specification by checking BEFORE and AFTER.

### ATStudio Example: Track API

**Phase 1 — Spec (api-spec.md)**
```
POST /api/tracks
  audioFile: MultipartFile (required)
  thumbnail: MultipartFile (optional)
  tagIds: List<Long> (optional)
Response: { id, title, bpm, audioFile, tags[] }
```

**Phase 2 — Actual Code (TrackController.java)**
```java
@PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<ResponseDTO<TrackResponse>> create(
    @ModelAttribute TrackCreateRequest request,  // includes categoryId — NOT in spec
    @AuthenticationPrincipal CustomUserDetails userDetails)
```

**Phase 3 — Delta**
- `categoryId` field added without spec update → accidental drift → update api-spec.md

### Agents Using This
- `re` (RE-8): Independent 3-way verification
- `qa-integ` (INT-1): Full endpoint coverage
- `cr`: Change impact analysis

---

## 2. Role × Screen Matrix

### Definition
Exhaustive cross-check of Role (GUEST/USER/ADMIN) × Screen (all pages) to catch access control mismatches that grep cannot find.

### ATStudio Findings (Examples)

| Finding | Type |
|---------|------|
| ADMIN sees "구독 시작하기" CTA on ProfilePage | Role UI mismatch |
| GUEST sees like/download buttons → silent 401 | Auth-required button leak |
| DownloadService checks subscription for ADMIN → NO_ACTIVE_SUBSCRIPTION | Backend role bypass missing |
| PlaylistListPage MAX_PLAYLISTS=3 applied to ADMIN | Business rule role bypass missing |

### Frontend Standalone (qa-fe)
- Route guard minRole matches intended access
- Conditional rendering per role is correct
- No auth-required buttons visible to GUEST

### Cross-Validation (qa-integ)
- Frontend route guard ≤ backend @PreAuthorize
- Frontend allows + backend blocks = bad UX (user sees UI, gets 403)
- Frontend blocks + backend allows = security gap (API bypasses UI guard)

---

## 3. Data Flow Tracing

### Definition
Trace a single request end-to-end through all layers and verify data consistency at each boundary.

### ATStudio Example: Download Track

```
Frontend: downloadTrack(trackId)
  → GET /api/tracks/{id}/download
  → DownloadController.download()
  → DownloadService.download()
    → check subscription (ADMIN bypass needed!)
    → check daily limit (downloadPerDay == -1 → unlimited)
    → create License record
    → return file resource
  → Frontend: trigger browser download
```

**Boundaries where bugs were found:**
- Service layer: ADMIN bypass missing → NO_ACTIVE_SUBSCRIPTION
- Service layer: downloadPerDay == -1 guard missing → unlimited plan blocked

---

## 4. Enum / Type Contract Alignment

### ATStudio Examples

| Java Enum | API JSON | TypeScript | Status |
|-----------|----------|------------|--------|
| `UserRole.ADMIN` | `"ADMIN"` | `'ADMIN' \| 'USER'` | OK |
| `QuestionStatus.IN_PROGRESS` | `"IN_PROGRESS"` | `'IN_PROGRESS'` | OK |
| Subscription `name` field | `"STANDARD"` | `PLAN_DISPLAYS.key = 'STANDARD'` | Fixed (L-2: was case-sensitive match) |

---

## 5. Null Propagation Tracking

### ATStudio Example: Thumbnail

```
Entity: thumbnail (nullable)
  → API response: thumbnail: "tracks/thumbs/abc.jpg" or null
  → Frontend: toUploadUrl(track.thumbnail)
  → toUploadUrl(null) must return null (not "/uploads/null")
```

**9 locations required `toUploadUrl()` application** — all found during frontend audit.

---

## 6. Error Code → UI Message Mapping

### ATStudio Pattern

```
Backend: throw new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION)
  → GlobalExceptionHandler → 403 Forbidden
  → Frontend: catch (error) → axios interceptor →
    if (status === 403) show "접근 권한이 없습니다"
```

**Known gap:** Generic 403 message for all forbidden responses — ideally differentiated by error code in response body.

---

## 7. Cross-Cutting Concern Audit

### ATStudio Patterns Found

| Concern | Issue | Fix |
|---------|-------|-----|
| `@Transactional` | AuthService, OAuth2Service missing | Added class-level `@Transactional` |
| Exception handling | `AccessDeniedException` caught by catch-all → 500 | Added explicit handler |
| Cascade delete | Question children (Answer, Attachment) not deleted → FK 500 | Explicit service-layer delete order |
| `isDeleted` filter | `findByEmail` returned soft-deleted users | Added `AndIsDeletedFalse` to queries |

---

## 8. Pagination Consistency

### ATStudio Convention
- Backend: 1-based page numbering (`page - 1` for PageRequest)
- Frontend: sends `page=1` for first page
- PageInfo structure: `{ page, size, total, totalPages, blockSize }`
- Edge case: empty result → `total=0, totalPages=0` → Pagination component hidden

---

## 9. State Machine Verification

### ATStudio Example: Question Status

```
OPEN → IN_PROGRESS → RESOLVED → CLOSED
         ↑                ↓
         └── OPEN ←── (reopen not implemented)
```

**Verified:** `question.updateStatus()` enforces valid transitions; invalid transitions throw `BusinessException`.
