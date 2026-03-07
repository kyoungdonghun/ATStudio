# WI-20260307-ATS-010 Cross-Validation Summary

> **WI**: WI-20260307-ATS-010
> **Date**: 2026-03-07
> **Scope**: api-spec §6/§11/§14 ↔ usecase ↔ front-list ↔ modal-list ↔ screen-flow
> **Validator**: docops

---

## Result Overview

| Severity | Count |
|----------|-------|
| CRITICAL | 2 |
| MAJOR | 3 |
| MINOR | 2 |
| SUGGESTION | 1 |
| **Total** | **8** |

---

## CRITICAL Issues (2건)

### C-1: `nextResetAt` — api-spec §14.5 및 usecase UTIL-006 미반영

`modal-list.md`와 `screen-flow.md`는 `nextResetAt` 필드를 이미 구현 완료(T-1)로 표기하고 있으나, `api-spec.md §14.5` Download Count Check 응답과 `usecase/util.md UTIL-006`의 응답 필드 목록에 `nextResetAt`가 없습니다.

- 영향: 프론트엔드가 api-spec을 기준으로 구현하면 `nextResetAt` 없이 개발됨. 토스트 메시지("오늘 다운로드 한도 초과. {nextResetAt} 초기화")와 장바구니 화면 표시 항목이 동작하지 않음.

### C-2: `GET /api/utils/subscription-change-preview` — api-spec §14 및 usecase 미정의

`modal-list.md` Backend Supplement에서 T-2 완료로 표기되어 있고 `screen-flow.md §7`이 해당 API를 참조하지만, `api-spec.md §14` Util 섹션에 이 엔드포인트가 정의되어 있지 않습니다. `usecase/util.md`에도 대응 UC가 없습니다.

- 영향: 프론트엔드 개발자가 api-spec만 보면 업그레이드 예상 금액 계산 API 존재 자체를 알 수 없음. 구독 변경 플로우 구현 불가.

---

## MAJOR Issues (3건)

### MA-1: DOWNGRADE pending 예약 — api-spec §6.7 및 usecase PAYMENT-007 미반영

`screen-flow.md §7`과 `modal-list.md`는 다운그레이드 예약(`pendingSubscriptionId`, `pending` 구독 표시) 흐름을 정의하고 있으나, `api-spec §6.7` 설명("Applied immediately")과 `usecase PAYMENT-007` 본문("immediately updates")은 즉시 적용만 기술합니다. DOWNGRADE 분기 및 pending 상태 응답 필드가 없습니다.

- 영향: api-spec/usecase를 기준으로 구현하면 다운그레이드 예약 기능이 누락됨.

### MA-2: `api-spec §6.10` 즉시 취소 vs `screen-flow/modal-list` 유예 기간 충돌

`api-spec §6.10` 및 `usecase PAYMENT-010`은 취소 시 `status=CANCELLED` 즉시 적용을 명시합니다. 그러나 `screen-flow.md §7`은 "취소 후 {expiresAt}까지 이용 가능" 안내를, `modal-list.md M-10`은 "취소 후 유예 안내"를 정의합니다.

- 영향: 취소 후 서비스 유지 여부 비즈니스 정책이 문서 간에 충돌함. 프론트엔드와 백엔드 구현 방향이 달라질 수 있음.

### MA-3: `11.3 DELETE /api/download-queue/{trackId}` vs `{id}` 불일치

`api-spec §11.3`은 경로 파라미터를 `{trackId}`로 정의합니다. `modal-list.md M-22`는 동일 엔드포인트를 `DELETE /api/download-queue/{id}`로 표기합니다.

- 영향: 프론트엔드 구현 시 경로 파라미터명 혼동 가능. 의미 명확성 문제 (`{id}` = download_queue record id? `{trackId}` = track id?).

---

## MINOR Issues (2건)

### MI-1: `screen-flow.md §7` DOWNGRADE 경로에 "TODO T-3" 잔존

`screen-flow.md §7` 다운그레이드 경로에 `(pendingSubscriptionId TODO T-3)` 표기가 남아 있습니다. `modal-list.md`는 T-3을 완료로 표기하므로 TODO 라벨이 제거되지 않은 상태입니다.

### MI-2: `api-spec §6.4` My Subscription 응답 상세 미기술

`screen-flow.md §7`은 `GET /api/user-subscriptions/me` 응답에서 `pending 구독` 정보와 `expiresAt` 필드를 사용하나, `api-spec §6.4`는 응답 본문을 "My current subscription status"로만 기술하고 구체적 필드 예시가 없습니다.

---

## SUGGESTION (1건)

### S-1: `usecase/util.md UTIL-006` 응답 필드 목록 보강 검토

`nextResetAt` 필드 추가 이후, UTIL-006 `Postconditions` 항목과 Main Flow 3단계 반환 필드 목록을 함께 갱신하면 usecase 단독 주입 시 정보 완결성이 높아집니다.

---

## Required Follow-up (승인 필요)

| 항목 | 결정 필요 사항 |
|------|--------------|
| C-1 | api-spec §14.5 및 UTIL-006에 `nextResetAt` 추가 승인 |
| C-2 | api-spec §14에 `subscription-change-preview` 신규 항목 추가 승인 |
| MA-1 | api-spec §6.7 DOWNGRADE pending 분기 반영 및 PAYMENT-007 UC 보강 승인 |
| MA-2 | 취소 후 유예 기간 정책 확정 (즉시 취소 vs 기간 만료까지 유지) |
| MA-3 | `{trackId}` vs `{id}` 경로 파라미터 표기 통일 승인 |
