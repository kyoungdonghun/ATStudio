# WI-20260526-ATS-002 Summary

## Result

- `payment_settlements` backend 원장을 추가했다.
- Settlement source/status enum을 추가했다.
- 관리자 settlement API를 추가했다.
  - `GET /api/admin/payments/settlements`
  - `POST /api/admin/payments/settlements/import`
  - `POST /api/admin/payments/settlements/reconcile`
  - `PUT /api/admin/payments/settlements/{settlementId}/ignore`
- CSV 수동 import를 구현했다.
- 내부 `payment_orders`, `subscription_payments`, succeeded `payment_refunds`와 대조해 `MATCHED`, `MISMATCHED`, `LOCAL_PAYMENT_NOT_FOUND`, `PROVIDER_SETTLEMENT_NOT_FOUND`, `IGNORED` 상태를 관리한다.
- 누락 후보 생성은 이미 import된 provider settlement evidence가 있는 주문을 다시 `PROVIDER_SETTLEMENT_NOT_FOUND`로 만들지 않도록 보강했다.
- provider API 호출이나 사용자 구독/결제/환불 상태 자동 변경은 추가하지 않았다.

## Changed Files

- `src/main/java/com/atstudio/atstudio/entity/PaymentSettlement.java`
- `src/main/java/com/atstudio/atstudio/entity/enums/PaymentSettlementSource.java`
- `src/main/java/com/atstudio/atstudio/entity/enums/PaymentSettlementStatus.java`
- `src/main/java/com/atstudio/atstudio/repository/PaymentSettlementRepository.java`
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentSettlementService.java`
- `src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java`
- `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentSettlement*.java`
- `src/main/java/com/atstudio/atstudio/service/PaymentOperationAuditLogService.java`
- `src/main/java/com/atstudio/atstudio/repository/SubscriptionPaymentRepository.java`
- `src/test/java/com/atstudio/atstudio/service/AdminPaymentSettlementServiceTest.java`

## Verification

- `gradlew.bat compileJava` passed.
- `gradlew.bat test --tests "com.atstudio.atstudio.service.AdminPaymentSettlementServiceTest" --tests "com.atstudio.atstudio.service.AdminPaymentRefundServiceTest" --tests "com.atstudio.atstudio.service.AdminPaymentEntitlementCorrectionServiceTest" --tests "com.atstudio.atstudio.service.PaymentOperationAuditLogServiceTest"` passed.

## Next

- WI-20260526-ATS-003: Security/privacy boundary review.
- WI-20260526-ATS-004: Admin settlement UI 구현.
