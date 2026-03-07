# WI-20260307-ATS-013 — API Spec v5 → v6 변경 요약

> 대상 파일: `docs/design/api-spec.md`
> WI: WI-20260307-ATS-013 | REQ: REQ-20260307-ATS-008

---

## 변경 항목 요약 (6건)

### 1. §14.5 GET /api/utils/download-count — nextResetAt 필드 추가

| | 내용 |
|---|---|
| Before | `{ todayDownloads, dailyLimit, remaining }` |
| After | `{ todayDownloads, dailyLimit, remaining, nextResetAt }` |
| 이유 | 프론트엔드에서 "언제 초기화되는지" 표시 가능하도록 필드 추가 |

---

### 2. §14.8 신규 — GET /api/utils/subscription-change-preview

| 항목 | 값 |
|---|---|
| URL | `GET /api/utils/subscription-change-preview` |
| Auth | 구독자 전용 (JWT) |
| Query | `subscriptionId` (Long), `billingCycle` (MONTHLY|YEARLY) |
| 응답 | `changeType`, `proratedAmount`, `effectiveDate`, `newPlanName`, `newBillingCycle` |
| 에러 | 401, 400 (잘못된 파라미터), 404 (구독 미보유) |

구독 변경 확정 전에 금액과 적용일을 미리 확인하는 API.

---

### 3. §6.7 PUT /api/user-subscriptions/me — UPGRADE/DOWNGRADE 분기 명시

| | 내용 |
|---|---|
| Before | "Applied immediately, prorated amount charged" (분기 없음) |
| After | UPGRADE: 즉시 적용 + proratedAmount 결제. DOWNGRADE: pending 저장 후 기간 만료 후 적용. |
| 추가 필드 | 응답에 `changeType: "UPGRADE" | "DOWNGRADE"` 포함 |

---

### 4. §6.10 DELETE /api/user-subscriptions/me — 유예 기간 명시

| | 내용 |
|---|---|
| Before | "Immediate cancellation (status=CANCELLED)" |
| After | "status가 CANCELLED로 변경되나, expiresAt까지 서비스 이용 가능. expiresAt 이후 자동 만료." |
| 이유 | 취소 후 즉시 혜택 중단이 아닌 유예 기간 정책을 정확히 반영 |

---

### 5. §1.8 신규 — GET /api/tracks/admin (관리자 전체 트랙 목록)

| 항목 | 값 |
|---|---|
| URL | `GET /api/tracks/admin` |
| Auth | `[ADMIN]` |
| Query | `page`, `size`, `is_active` (optional, 미전달 시 전체) |
| 응답 | §1.2와 동일 구조 + `isActive` 필드 포함 |
| 에러 | 401, 403 |

비활성 트랙 포함 전체 목록을 조회할 수 있는 관리자 전용 API.

---

### 6. §3.1 POST /api/playlists — PLAYLIST_LIMIT_EXCEEDED 에러 추가

| | 내용 |
|---|---|
| Before | Error Cases 없음 |
| After | `409 Conflict: PLAYLIST_LIMIT_EXCEEDED — "활성 재생목록은 최대 3개까지 생성할 수 있습니다."` |
| 이유 | REQ-20260303-ATS-002에서 구현된 3개 제한 로직이 API spec에 미반영 상태였음 |

---

## 버전 변경

| 항목 | 이전 | 이후 |
|---|---|---|
| 버전 | v5 | v6 |
| 날짜 | 2026-02-20 | 2026-03-07 |
| Full API Summary | 87개 (Track 7, Util 7) | 89개 (Track 8, Util 8) |
