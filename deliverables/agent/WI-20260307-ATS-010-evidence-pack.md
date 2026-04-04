# WI-20260307-ATS-010 Evidence Pack

> **WI**: WI-20260307-ATS-010
> **Date**: 2026-03-07
> **Type**: Cross-Validation (Read-only)
> **Validator**: docops
> **Files Modified**: None (read-only task)

---

## Scope

| Document | Sections Validated |
|----------|--------------------|
| `docs/design/api-spec.md` | §6 Subscription/UserSubscription, §11 DownloadQueue, §14 Util |
| `docs/design/usecase/user-subscription.md` | PAYMENT-001 ~ PAYMENT-010 |
| `docs/design/usecase/download-queue.md` | DLQ-001 ~ DLQ-003 |
| `docs/design/usecase/util.md` | UTIL-002 ~ UTIL-007, UTIL-012 |
| `docs/ui/atstudio-front-list.md` | Screen 11, 16-1, 16-2, 16-3 |
| `docs/ui/modal-list.md` | M-09, M-10, M-22, M-24, M-26, M-27, Backend Supplement |
| `docs/ui/screen-flow.md` | §6 장바구니/다운로드 흐름, §7 구독 흐름 |

---

## Validation Targets

| Target | Description |
|--------|-------------|
| T-1 | `nextResetAt` field — consistent across api-spec, usecase, screen-flow, modal-list |
| T-2 | `GET /api/utils/subscription-change-preview` — defined in api-spec, usecase, screen-flow |
| T-3 | DOWNGRADE pending reservation — reflected in api-spec, usecase, screen-flow |

---

## Detailed Findings

---

### [CONFLICT] CRITICAL — C-1: `nextResetAt` field absent from api-spec and usecase

**Finding type**: CONFLICT
**Severity**: CRITICAL

**Evidence — Documents that INCLUDE `nextResetAt`:**
- `docs/ui/screen-flow.md §6` line 152–153:
  ```
  상단: 오늘 잔여 다운로드 횟수 + nextResetAt 표시
  ```
- `docs/ui/screen-flow.md §6` line 159:
  ```
  한도 초과 → 토스트: "오늘 다운로드 한도 초과. {nextResetAt} 초기화"
  ```
- `docs/ui/screen-flow.md §13` (토스트 기준) line 353:
  ```
  한도 초과: "다운로드 한도 초과. {nextResetAt} 초기화"
  ```
- `docs/ui/modal-list.md` Backend Supplement table:
  ```
  T-1 | nextResetAt 필드 (GET /api/utils/download-count) | ✅ 완료
  ```

**Evidence — Documents that DO NOT INCLUDE `nextResetAt`:**
- `docs/design/api-spec.md §14.5` Download Count Check response (line 1571–1577):
  ```json
  {
    "todayDownloads": 3,
    "dailyLimit": 20,
    "remaining": 17
  }
  ```
  `nextResetAt` field is absent.
- `docs/design/usecase/util.md UTIL-006` Main Flow step 3 (line 130):
  ```
  Backend returns the result (todayDownloads, dailyLimit, remaining).
  ```
  `nextResetAt` is not listed. Postconditions also do not mention the field.

**Impact**: Frontend developer injecting api-spec.md will implement the download count screen without `nextResetAt`. The toast message "오늘 다운로드 한도 초과. {nextResetAt} 초기화" and the cart screen header display will fail at runtime.

**Fix pointer**: Update `api-spec.md §14.5` response body to add `nextResetAt: "2026-03-08T00:00:00"`. Update `usecase/util.md UTIL-006` Main Flow step 3 and Postconditions.

---

### [GAP] CRITICAL — C-2: `GET /api/utils/subscription-change-preview` not defined in api-spec or usecase

**Finding type**: GAP
**Severity**: CRITICAL

**Evidence — Documents that REFERENCE the endpoint:**
- `docs/ui/screen-flow.md §7` line 185:
  ```
  GET /api/utils/subscription-change-preview → proratedAmount 표시
  ```
- `docs/ui/modal-list.md` Flow 2 (PlanCompareModal):
  ```
  GET /api/utils/subscription-change-preview (TODO T-2)
    → proratedAmount = (newDailyRate - oldDailyRate) x 남은 일수
  ```
  Note: despite "TODO T-2" label in the flow diagram, Backend Supplement marks T-2 as complete:
  ```
  T-2 | GET /api/utils/subscription-change-preview | ✅ 완료
  ```

**Evidence — Documents where the endpoint is ABSENT:**
- `docs/design/api-spec.md §14` Util section (lines 1484–1618): Contains 14.1–14.7 only. No `subscription-change-preview` entry.
- `docs/design/usecase/util.md`: Contains UTIL-002, UTIL-003, UTIL-004, UTIL-005, UTIL-006, UTIL-007, UTIL-012. No UC for subscription-change-preview.

**Impact**: If a frontend developer or subagent is injected api-spec.md as the sole source of truth, the upgrade preview API does not exist. The upgrade flow in M-09 PlanCompareModal (proratedAmount display before payment) cannot be implemented.

**Fix pointer**: Add `14.8 GET /api/utils/subscription-change-preview` to `api-spec.md §14`. Add corresponding UC to `usecase/util.md`. Required request parameters: `subscriptionId`, `billingCycle`. Required response fields: `proratedAmount`, `changeType` (UPGRADE/DOWNGRADE).

---

### [CONFLICT] MAJOR — MA-1: DOWNGRADE pending path absent from api-spec §6.7 and usecase PAYMENT-007

**Finding type**: CONFLICT
**Severity**: MAJOR

**Evidence — Documents that DEFINE DOWNGRADE pending:**
- `docs/ui/screen-flow.md §7` lines 187–189:
  ```
  다운그레이드:
    "다음 결제일({expiresAt})부터 적용 · 추가 결제 없음" 안내
    → [변경 예약] → PUT 6.7 → 화면 갱신 (pending 표시)
  ```
- `docs/ui/screen-flow.md §7` line 181:
  ```
  pending 구독 있을 시: "예약된 변경: {플랜명} ({expiresAt}부터)" 표시
  ```
- `docs/ui/modal-list.md` Backend Supplement:
  ```
  T-3 | user_subscriptions pending 컬럼 + DOWNGRADE 예약 | ✅ 완료 (스케줄러 적용은 별도 REQ)
  ```

**Evidence — Documents that LACK DOWNGRADE pending:**
- `docs/design/api-spec.md §6.7` (lines 885–910): Description states "Applied immediately, prorated amount charged". Request body: `subscriptionId`, `billingCycle`. Response body has no `pendingSubscriptionId`, no `changeType`, no `scheduledAt` or equivalent. No DOWNGRADE-specific path or note.
- `docs/design/usecase/user-subscription.md PAYMENT-007` Main Flow step 5: "Backend immediately updates user_subscriptions". No alternative branch for DOWNGRADE reservation. Postconditions: "Changed plan services applied immediately."

**Impact**: api-spec §6.7 and usecase PAYMENT-007 describe only the UPGRADE (immediate) path. A subagent implementing `PUT /api/user-subscriptions/me` from these sources alone will not implement DOWNGRADE reservation. The pending display on Screen 16-3 will also have no data to render.

**Fix pointer**: Add DOWNGRADE branch to `api-spec §6.7` — request should accept optional `changeType: "DOWNGRADE"`, response should return `pendingStatus` or `scheduledAt`. Add Alternative Flow to `usecase PAYMENT-007` for DOWNGRADE reservation.

---

### [CONFLICT] MAJOR — MA-2: Immediate cancellation (api-spec/usecase) vs grace period (screen-flow/modal-list)

**Finding type**: CONFLICT
**Severity**: MAJOR

**Evidence — "Immediate cancellation" position:**
- `docs/design/api-spec.md §6.10` (line 933): "Member cancels their own active subscription. Immediate cancellation (status=CANCELLED)."
- `docs/design/usecase/user-subscription.md PAYMENT-010` Main Flow step 6: "Backend updates user_subscriptions.status to CANCELLED and returns 204 No Content."
- `docs/design/usecase/user-subscription.md PAYMENT-010` Postconditions: "user_subscriptions.status=CANCELLED updated. Subscription benefits (downloads, channel registration, playlists) no longer available."

**Evidence — "Grace period until expiry" position:**
- `docs/ui/screen-flow.md §7` line 192:
  ```
  "취소 후 {expiresAt}까지 이용 가능" 안내
  ```
- `docs/ui/modal-list.md M-10`: Content column states "취소 후 유예 안내 + 확인" (component: StatusModal).

**Impact**: The cancellation business policy is undefined at the authoritative layer (api-spec/usecase). If implemented per api-spec, benefits are lost immediately upon cancellation. If implemented per screen-flow, benefits continue until `expiresAt`. These are materially different UX and business outcomes. A business decision is required before implementation.

**Fix pointer**: Confirm cancellation policy (immediate vs. until-expiry grace period). Update `api-spec §6.10` description and `usecase PAYMENT-010` Postconditions to reflect the confirmed policy. Update `modal-list M-10` and `screen-flow §7` accordingly if they deviate from the confirmed policy.

---

### [CONFLICT] MAJOR — MA-3: Download Queue remove endpoint path parameter `{trackId}` vs `{id}`

**Finding type**: CONFLICT
**Severity**: MAJOR

**Evidence:**
- `docs/design/api-spec.md §11.1` (line 1283): `POST /api/download-queue/{trackId}`
- `docs/design/api-spec.md §11.3` (line 1313): `DELETE /api/download-queue/{trackId}`
- `docs/ui/modal-list.md M-22` (line 91): `11.3 DELETE /api/download-queue/{id}`

**Impact**: `{trackId}` and `{id}` carry different semantic meaning. `{trackId}` is the track's PK; `{id}` could be interpreted as the download_queue record's own PK (which exists as a composite key). Frontend developer reading modal-list.md may implement with wrong parameter. Additionally, `docs/design/usecase/download-queue.md DLQ-003` (line 74) states "Frontend sends a delete request with trackId" — consistent with api-spec, inconsistent with modal-list.

**Fix pointer**: Standardize to `{trackId}` in `modal-list.md M-22` to match api-spec §11.1, §11.3 and usecase DLQ-003.

---

### [OMISSION] MINOR — MI-1: "TODO T-3" label not removed from screen-flow.md §7

**Finding type**: OMISSION
**Severity**: MINOR

**Evidence:**
- `docs/ui/screen-flow.md §7` line 156 (downgrade path):
  ```
  → PUT 6.7 /api/user-subscriptions/me (pendingSubscriptionId TODO T-3)
  ```
- `docs/ui/modal-list.md` Backend Supplement:
  ```
  T-3 | user_subscriptions pending 컬럼 + DOWNGRADE 예약 | ✅ 완료
  ```

**Impact**: "TODO T-3" is stale. An agent injecting screen-flow.md may treat the downgrade path as incomplete and defer implementation.

**Fix pointer**: After MA-1 is resolved, remove "TODO T-3" label from `screen-flow.md §7` downgrade path. Replace with concrete field reference (e.g., `pendingSubscriptionId`).

---

### [GAP] MINOR — MI-2: api-spec §6.4 response body not specified

**Finding type**: GAP
**Severity**: MINOR

**Evidence:**
- `docs/design/api-spec.md §6.4` (line 861–867):
  ```
  ## 6.4 My Subscription
  | URL | GET /api/user-subscriptions/me |
  | Auth | auth required |
  Response 200 OK — My current subscription status
  ```
  No response body JSON example is provided.
- `docs/ui/screen-flow.md §7` line 181: Uses `pending 구독` info from this endpoint.
- `docs/ui/screen-flow.md §7` line 188: Uses `expiresAt` from this endpoint.
- `docs/ui/screen-flow.md §7` line 192: Uses `expiresAt` again in cancellation flow.

**Impact**: Frontend developers cannot confirm whether `expiresAt`, `pendingSubscriptionId`, or `pending` fields are included in the response. This is particularly critical if MA-1 is resolved and DOWNGRADE pending is added.

**Fix pointer**: Add a complete response body example to `api-spec §6.4` including at minimum: `id`, `subscription`, `billingCycle`, `status`, `startedAt`, `expiresAt`, and (post MA-1 resolution) `pendingSubscription` or equivalent.

---

### [SUGGESTION] — S-1: usecase/util.md UTIL-006 response field list should be kept in sync with api-spec §14.5

**Finding type**: SUGGESTION

**Context**: UTIL-006 Main Flow step 3 lists return fields inline: `(todayDownloads, dailyLimit, remaining)`. These are currently consistent with api-spec §14.5, but both are missing `nextResetAt`. When C-1 is fixed, both must be updated simultaneously to avoid re-divergence.

**Recommendation**: Establish a single-update rule: when api-spec §14.5 response body changes, `usecase/util.md UTIL-006` Main Flow step 3 field list and Postconditions must be updated in the same commit.

---

## Cross-Matrix Summary

| Finding | api-spec §6 | api-spec §11 | api-spec §14 | usecase/user-sub | usecase/dlq | usecase/util | front-list | modal-list | screen-flow |
|---------|:-----------:|:------------:|:------------:|:----------------:|:-----------:|:------------:|:----------:|:----------:|:-----------:|
| C-1 nextResetAt | — | — | MISSING | — | — | MISSING | — | OK (T-1 ✅) | OK (uses it) |
| C-2 change-preview | — | — | MISSING | — | — | MISSING | — | OK (T-2 ✅) | OK (uses it) |
| MA-1 DOWNGRADE pending | MISSING | — | — | MISSING | — | — | — | OK (T-3 ✅) | OK (uses it) |
| MA-2 cancel grace | CONFLICT | — | — | CONFLICT | — | — | — | CONFLICT | CONFLICT |
| MA-3 {trackId} vs {id} | — | CONFLICT | — | — | OK | — | — | CONFLICT | — |
| MI-1 TODO T-3 stale | — | — | — | — | — | — | — | OK (cleared) | STALE |
| MI-2 §6.4 body missing | MISSING | — | — | — | — | — | — | — | uses fields |

Legend: OK = consistent, MISSING = field/endpoint absent, CONFLICT = contradicts another source, STALE = outdated label

---

## Files Read (Read-only, no modifications)

| File | Lines Read |
|------|-----------|
| `docs/design/api-spec.md` | Full (§6 lines 791–941, §11 lines 1275–1318, §14 lines 1484–1618) |
| `docs/design/usecase/user-subscription.md` | Full (242 lines) |
| `docs/design/usecase/download-queue.md` | Full (83 lines) |
| `docs/design/usecase/util.md` | Full (178 lines) |
| `docs/ui/atstudio-front-list.md` | Full (152 lines) — Screen 11, 16-1, 16-2, 16-3 |
| `docs/ui/modal-list.md` | Full (274 lines) — M-09, M-10, M-22, M-24, M-26, M-27, Backend Supplement |
| `docs/ui/screen-flow.md` | Full (359 lines) — §6, §7 |

---

## Recommended Fix Priority

| Priority | Finding | Blocking? |
|----------|---------|-----------|
| 1 | C-1 nextResetAt in api-spec + usecase | Blocks frontend Screen 11 download count display |
| 2 | C-2 subscription-change-preview in api-spec + usecase | Blocks frontend M-09 upgrade flow |
| 3 | MA-2 Cancellation policy decision | Blocks M-10 and Screen 16-3 UX finalization |
| 4 | MA-1 DOWNGRADE pending in api-spec + usecase | Blocks Screen 16-3 pending display and PUT 6.7 downgrade implementation |
| 5 | MA-3 {trackId} vs {id} in modal-list M-22 | Minor inconsistency, fix in modal-list |
| 6 | MI-1 Remove TODO T-3 from screen-flow | Stale label, low-effort cleanup |
| 7 | MI-2 Add response body to api-spec §6.4 | Documentation completeness |
| 8 | S-1 Sync UTIL-006 with api-spec §14.5 update | Do as part of C-1 fix |
