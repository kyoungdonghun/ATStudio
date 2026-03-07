[WI SUMMARY — User-Facing]
WI ID: WI-20260307-ATS-016
REQ: REQ-20260307-ATS-008 Phase 4
Domain: Subscription / Utils / DownloadQueue
Date: 2026-03-07
Author: cr (MA 직접 수행)

---

## 발견 건수 요약

| 심각도 | 건수 |
|--------|------|
| CRITICAL | 1 |
| MAJOR | 0 |
| MINOR | 0 |
| **합계** | **1** |

---

## CRITICAL-1: 구독 취소 유예 기간 미구현 (BD-1)

- **위치**: `UserSubscription.java:68-70`, `UserSubscriptionRepository.java:18-23`, `UserSubscriptionService.java:232-241`
- **문서 기준**: api-spec v6 §6.10 — "status=CANCELLED이나 expiresAt까지 서비스 이용 가능"
- **실제 코드**:
  - `UserSubscription.cancel()` → `this.status = SubscriptionStatus.CANCELLED` (expiresAt 변경 없음)
  - `findActiveByUser` 쿼리: `WHERE us.status = :status AND us.expiresAt >= :today` → status=ACTIVE 파라미터로만 호출됨
  - `selfCancel()` 후 모든 서비스에서 `findActiveByUser(ACTIVE)` 실패 → `NO_ACTIVE_SUBSCRIPTION` 예외
  - **결과**: 취소 즉시 다운로드, 재생목록 등 모든 구독 혜택 차단됨
- **수정 방안**: `findActiveByUser` 쿼리에서 `status IN (ACTIVE, CANCELLED)` 또는 별도 쿼리 추가하여 expiresAt 내에 있으면 혜택 유지

---

## 이상 없음 항목

| 항목 | 결과 |
|------|------|
| `changeSubscription()` UPGRADE/DOWNGRADE 분기 | ✅ 완전 구현 (UserSubscriptionService:154-204) |
| changeType 응답 필드 | ✅ "UPGRADE"/"DOWNGRADE" 포함 (line 186, 199) |
| DOWNGRADE pendingSubscription/pendingBillingCycle | ✅ 구현됨 (UserSubscription:47-57) |
| `UtilService.getDownloadCount()` nextResetAt | ✅ 반환 (UtilService:71, 74, 86) |
| `GET /api/utils/subscription-change-preview` | ✅ UtilController:76, UtilService:97 완전 구현 |
| `DownloadQueueController` DELETE `/{trackId}` path param | ✅ `{trackId}` 사용 (DownloadQueueController:42) |
