# WI-20260517-ATS-005 Summary

정기결제 Phase C의 백엔드 아키텍처 경계를 정리했습니다.

- `BillingAgreement`는 빌링키 등록/상태/실패 횟수/다음 결제일을 소유합니다.
- `PaymentOrder`는 `BILLING_AGREEMENT`, `SUBSCRIBE`, `RENEWAL` 시도를 모두 감사 가능한 주문 단위로 기록합니다.
- `SubscriptionPayment`는 실제로 승인된 결제만 최종 원장으로 남깁니다.
- `UserSubscription`은 결제 성공 이후에만 생성, 연장, 갱신됩니다.
- 초기 정기 구독은 빌링키 등록 후 즉시 1회 결제에 성공해야 활성화됩니다.
- 정기 갱신은 `nextBillingAt <= today`인 `ACTIVE` billing agreement만 대상으로 합니다.
- 실패 정책은 승인된 기준대로 `3일 유예 + 최대 3회 재시도 + 이후 SUSPENDED/EXPIRED 전환`을 사용합니다.

구현 단계에서는 `BillingAgreementApplicationService`, `RecurringPaymentProvider`, `RecurringBillingScheduler`를 별도 구성으로 추가하는 것이 적절합니다. 기존 `PaymentApplicationService`의 단건 결제 흐름은 유지하고, 정기결제는 공유 원장(`PaymentOrder`, `SubscriptionPayment`)만 재사용하는 쪽이 가장 안전합니다.
