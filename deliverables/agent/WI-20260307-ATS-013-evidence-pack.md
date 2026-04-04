---
wi: WI-20260307-ATS-013
req: REQ-20260307-ATS-008
agent: docops
status: DONE
date: 2026-03-07
---

# Evidence Pack — WI-20260307-ATS-013

## 1. Scope

File modified: `docs/design/api-spec.md` (only)
Version: v5 → v6

---

## 2. Change Evidence (File:Line Pointers)

### W1 — §14.5 nextResetAt field added

- File: `docs/design/api-spec.md`
- Location: §14.5 Download Count Check response block
- Line range (approximate): 1627–1642
- Before:
  ```json
  { "todayDownloads": 3, "dailyLimit": 20, "remaining": 17 }
  ```
- After:
  ```json
  { "todayDownloads": 3, "dailyLimit": 20, "remaining": 17, "nextResetAt": "2026-03-08T00:00:00" }
  ```
- Added note: `nextResetAt`: LocalDateTime (ISO-8601) — timestamp when the daily download counter resets.

---

### W2 — §14.8 GET /api/utils/subscription-change-preview added

- File: `docs/design/api-spec.md`
- Location: inserted after §14.7 Nickname Duplicate Check, before "Removed Items" section
- Line range (approximate): 1674–1709
- New section heading: `## 14.8 Subscription Change Preview`
- Auth: auth required (subscribers only)
- Query Params: `subscriptionId` (Long, required), `billingCycle` (String: MONTHLY|YEARLY, required)
- Response 200 fields: `changeType`, `proratedAmount`, `effectiveDate`, `newPlanName`, `newBillingCycle`
- Error cases: 401, 400 INVALID_ARGUMENT, 404 SUBSCRIPTION_NOT_FOUND

---

### W3 — §6.7 UPGRADE/DOWNGRADE branch semantics added

- File: `docs/design/api-spec.md`
- Location: §6.7 Change My Subscription (Upgrade/Downgrade)
- Line range (approximate): 937–972
- Description field: Updated from "Applied immediately, prorated amount charged" to full UPGRADE/DOWNGRADE branch explanation
- Response: Added `changeType: "UPGRADE"` field in JSON example
- Added note block explaining UPGRADE (immediate) vs DOWNGRADE (pending) behavior

---

### W4 — §6.10 grace period semantics updated

- File: `docs/design/api-spec.md`
- Location: §6.10 Cancel My Subscription
- Line range (approximate): 990–1002
- Before (Description): "Member cancels their own active subscription. Immediate cancellation (status=CANCELLED)."
- After (Description): "Member cancels their own active subscription. status가 CANCELLED로 변경되나, expiresAt까지 서비스 이용 가능. expiresAt 이후 자동 만료."

---

### W5 — §1.8 GET /api/tracks/admin added

- File: `docs/design/api-spec.md`
- Location: inserted after §1.7 Delete Track, before `# 2. Sound — Tag`
- Line range (approximate): 291–331
- New section heading: `## 1.8 List All Tracks (Admin)`
- Auth: `[ADMIN]`
- Query Params: `page`, `size`, `is_active` (optional)
- Response: same structure as §1.2 + `isActive` field in each item
- Error cases: 401, 403

---

### W6 — §3.1 PLAYLIST_LIMIT_EXCEEDED error case added

- File: `docs/design/api-spec.md`
- Location: §3.1 Create Playlist, after Response 201 block
- Line range (approximate): 434–437
- Added:
  ```
  **Error Cases**
  { "status": 409, "error": "Conflict", "errorCode": "PLAYLIST_LIMIT_EXCEEDED", "message": "활성 재생목록은 최대 3개까지 생성할 수 있습니다." }
  ```
- Traceability: Corresponds to `PlaylistService.java:46-48` and `BUSINESS_ERROR.PLAYLIST_LIMIT_EXCEEDED` implemented in REQ-20260303-ATS-002.

---

### W7 — Version header and Full API Summary updated

- File: `docs/design/api-spec.md`
- Line 1: `# ATStudio API Specification v6 (Confirmed)`
- Line 3: Status description updated
- Line 5: Date changed to `2026-03-07`
- Line 9: New `## v5 → v6 Change History` table (W1–W6)
- Full API Summary: Track 7→8, Util 7→8, Total 87→89

---

## 3. DoD Checklist

- [x] §14.5 nextResetAt (LocalDateTime, ISO-8601) added to response
- [x] §14.8 GET /api/utils/subscription-change-preview added (after §14.7)
- [x] §6.7 UPGRADE/DOWNGRADE branch semantics documented, changeType field in response
- [x] §6.10 grace period semantics updated (expiresAt까지 서비스 이용 가능)
- [x] §1.8 GET /api/tracks/admin added (after §1.7)
- [x] §3.1 PLAYLIST_LIMIT_EXCEEDED 409 error case added
- [x] Version v5 → v6, date 2026-03-07
- [x] Full API Summary count updated (87 → 89)
- [x] Section numbering conflict-free
- [x] Existing section structure/format consistency maintained

---

## 4. Files Modified

| File | Change Type |
|------|-------------|
| `docs/design/api-spec.md` | Modified (v5 → v6, 6 content changes + 1 version update) |

---

## 5. Files NOT Modified (Scope Out)

Per WI constraints, the following were not touched:

- `docs/design/usecase/` — WI-014/015 scope
- `docs/ui/atstudio-front-list.md` — WI-014/015 scope
- Any backend Java source files

---

## 6. Traceability

| WI Change | Upstream Source |
|-----------|----------------|
| W1 (nextResetAt) | REQ-20260307-ATS-008 |
| W2 (subscription-change-preview) | REQ-20260307-ATS-008 |
| W3 (§6.7 changeType) | REQ-20260307-ATS-008 |
| W4 (§6.10 grace period) | REQ-20260307-ATS-008 |
| W5 (§1.8 admin track list) | REQ-20260307-ATS-008 |
| W6 (PLAYLIST_LIMIT_EXCEEDED) | REQ-20260303-ATS-002 (implemented), REQ-20260307-ATS-008 (spec sync) |
