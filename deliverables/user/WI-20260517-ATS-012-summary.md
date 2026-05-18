# WI-20260517-ATS-012 Summary

프론트엔드 정기결제 등록/관리 흐름을 연결했습니다.

- `frontend/src/api/payments.ts`에 billing agreement prepare/confirm/current/cancel API를 추가했습니다.
- Toss SDK helper에 `payment({ customerKey }).requestBillingAuth(...)` 타입을 추가했습니다.
- 구독 플랜 CTA는 신규 구독일 때 `/subscriptions/payment?...&mode=recurring`로 이동합니다.
- `SubscriptionPaymentPage`는 `mode=recurring`에서 Toss Billing 카드 등록창을 열고, 성공 리다이렉트의 `authKey/customerKey/orderId/amount`를 backend confirm API로 전달합니다.
- `/subscriptions/billing/success`, `/subscriptions/billing/fail` 라우트를 추가했습니다.
- `SubscriptionManagePage`에서 자동 갱신 상태, 결제수단 마스킹 정보, 다음 결제일을 표시하고 자동 갱신 해지를 실행할 수 있게 했습니다.
- 관련 프론트 테스트 케이스를 추가했습니다.

검증:

- `npm run typecheck` 통과.
- `npm run lint` 통과.
- `npm test -- SubscriptionPaymentPage.test.tsx SubscriptionManagePage.test.tsx`는 Vite/esbuild 하위 프로세스 spawn이 sandbox에서 `EPERM`으로 막혀 실행되지 못했습니다.
- `npm run build`도 같은 `spawn EPERM` 제한으로 Vite 단계에서 막혔습니다.

주의:

- SR-92의 결제창 모달/전용 checkout 분리는 이번 WI에서 처리하지 않았습니다.
- Toss Billing은 공식 문서 기준 `requestBillingAuth()` 성공 후 서버에서 billing key 발급과 최초 결제를 처리하는 흐름입니다.
