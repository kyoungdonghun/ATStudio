# Domain Design Lessons

> Covers domain modeling, API design, and structural decisions from ATStudio.
> These are framework-agnostic and apply to any backend project.

---

## Domain Vocabulary: Lock Early

### The Playlist / Album Problem

ATStudio had two distinct domains that shared similar surface concepts:

| Domain | Owner | Purpose | Table |
|--------|-------|---------|-------|
| **Playlist** | Subscriber | Personal track queue, max 3 | `playlists` |
| **Album** | Admin | Curated public collection | `albums` |

These were confused in UI copy for multiple sessions because the distinction wasn't locked in vocabulary from the start. The fix required a dedicated REQ (REQ-20260306-ATS-004).

**Rule for next project**: Before writing any code, define every domain concept in a glossary with:
- Who creates it (actor)
- Who can read/modify/delete it (access model)
- Its lifecycle (states)
- How it differs from any similar-sounding concept

### Vocabulary First, Code Second

Terminology that's inconsistent in documentation will be inconsistent in variable names, API paths, UI labels, and error messages. Standardize the glossary before implementing any feature that touches multiple teams or surfaces.

---

## Soft Delete vs Hard Delete

### Pattern Used (ATStudio)

```sql
-- Users, Tracks: soft delete (preserve for audit/license history)
is_deleted TINYINT(1) DEFAULT 0
is_active  TINYINT(1) DEFAULT 1
```

| Entity | Delete Strategy | Reason |
|--------|----------------|--------|
| User | Soft (`is_deleted=1`) | License history, audit trail |
| Track | Soft (`is_active=0`) | Preserve download history, licenses |
| Playlist | Hard | User-owned, no external references |
| Album | Soft (`is_active=false`) | `album_tracks` preserved for recovery |

### Decision Framework

Use **soft delete** when:
- Other records reference this entity (FK constraints)
- Audit trail is required by business rules
- Recovery ("undelete") is a plausible user request

Use **hard delete** when:
- No external references (or all are cascade-deleted)
- Data privacy requirements demand actual removal (GDPR)
- The entity is truly ephemeral (session data, queues)

### Critical: Filter Deleted Records Everywhere

When using soft delete, every query that returns user-facing data must filter:
```java
findByEmailAndIsDeletedFalse(String email)  // ✅
findByEmail(String email)                   // ❌ — returns deleted accounts
```

Failing to filter leaks PII in admin list APIs and can allow re-authentication with deleted credentials.

---

## Status Machines

### Pattern

Define status transitions on the entity itself, not in the service layer:

```java
@Entity
public class CompanyCertification {

    public void approve(String certCode) {
        if (this.status != PENDING) {
            throw new BusinessException(INVALID_STATUS_TRANSITION);
        }
        this.status = APPROVED;
        this.certificationCode = certCode;
        this.approvedAt = LocalDateTime.now();
    }

    public void requestRevision(String adminNote) {
        if (this.status != PENDING) {
            throw new BusinessException(INVALID_STATUS_TRANSITION);
        }
        this.status = REVISION_REQUESTED;
        this.adminNote = adminNote;
    }
}
```

**Benefits**:
- Business rules live next to the data they protect
- Service layer just calls `entity.approve()` — no transition logic scattered in services
- Impossible states are prevented at the model level

### Test All Invalid Transitions

For a status machine with N states, test every invalid transition explicitly:
```
APPROVED → approve()     // should throw
REJECTED → reject()      // should throw
APPROVED → reject()      // should throw
```

This was missing in ATStudio initially and added later (REQ-20260303-ATS-001).

---

## API Design Principles

### Response Envelope Consistency

Choose one pattern at project start and enforce it:

```json
// ✅ Consistent — all list responses use this wrapper
{ "dataList": [...], "pageInfo": { "page": 0, "size": 10, "total": 100 } }

// ❌ Inconsistent — some endpoints return raw arrays, others use wrapper
[...]          // some endpoints
{ "data": [] } // other endpoints
```

ATStudio retrofitted 4+ endpoints from raw array to `dataList` mid-project (MINOR-001~004). This required API spec updates, frontend adjustments, and test changes.

### HTTP Status Code Standards

Establish these before implementation:

| Situation | Status |
|-----------|--------|
| Successful creation | 201 Created |
| Successful update | 200 OK |
| Successful delete | 204 No Content |
| Validation failure | 400 Bad Request |
| Authentication required | 401 Unauthorized |
| Insufficient permission | 403 Forbidden |
| Resource not found | 404 Not Found |
| Duplicate resource | 409 Conflict |
| Business rule violation | 409 Conflict (or 422 Unprocessable Entity) |

ATStudio initially returned 400 for duplicate resources — fixed to 409 during audit (M-2). Define upfront.

### Business Limits as API Rules

When a business rule limits a resource count (e.g., plan-based playlist limits), enforce it at the API level and expose a meaningful error:

```java
// ✅ Named error code with HTTP 409
PLAYLIST_LIMIT_EXCEEDED(409, "PLAYLIST_LIMIT_EXCEEDED", "구독 플랜의 재생목록 한도를 초과했습니다.")
```

The frontend should pre-check the limit (e.g., hide the "create" button) but the API must enforce it independently.

---

## Download / License Pattern

ATStudio's download flow solved a common e-commerce problem cleanly:

```
User clicks Download
  → Check subscription access state (service-enabled subscription required)
  → Count today's downloads (< plan.downloadPerDay)
  → Retrieve audio_file from storage
  → Insert track_downloads record
  → Upsert licenses record (idempotent — issue once per user+track)
  → Return file as attachment
```

**Key insight**: License issuance is idempotent (`INSERT IGNORE` or `existsById` check). A user downloading the same track twice shouldn't get two licenses. This pattern (check → insert-if-absent) applies to any resource that should be granted once.

### Unlimited Plans

When `downloadPerDay = -1` represents unlimited, the limit check must handle this explicitly:

```java
if (subscription.getDownloadPerDay() != -1
    && todayCount >= subscription.getDownloadPerDay()) {
    throw new BusinessException(DOWNLOAD_LIMIT_EXCEEDED);
}
```

The `!= -1` guard is easy to miss and creates a hard-to-debug security hole.

---

## Many-to-Many with Order

When a many-to-many relationship requires ordering (e.g., tracks in a playlist/album), store the order in the join table:

```sql
CREATE TABLE playlist_tracks (
    playlist_id BIGINT NOT NULL,
    track_id    BIGINT NOT NULL,
    track_order INT NOT NULL,
    PRIMARY KEY (playlist_id, track_id)
);
```

Provide a dedicated "reorder" API that batch-updates `track_order`:
```
PUT /api/playlists/{id}/tracks/order
Body: [{ "trackId": 1, "order": 1 }, { "trackId": 2, "order": 2 }]
```

Avoid sorting by `created_at` — it breaks when tracks are removed and re-added.

---

## Subscription Model Design

The upgrade/downgrade/cancel decision matrix used in ATStudio:

| Action | Timing | Billing |
|--------|--------|---------|
| Upgrade | Immediate | Prorated: `(newDailyRate - oldDailyRate) × remainingDays` |
| Downgrade | Next billing date | No immediate charge |
| Cancel | Immediate status change | Service continues until `expires_at` |

**Why this works**: Users are never charged retroactively. Upgrades justify immediate payment (immediate benefit). Downgrades are scheduled (user already paid for current period). Cancels preserve trust (service doesn't disappear immediately).

**Schema implication for downgrade scheduling**: The `user_subscriptions` table needs `pending_subscription_id` and `pending_billing_cycle` columns to store the scheduled change. A scheduled job or login-time check applies the change when `expires_at` is reached.
