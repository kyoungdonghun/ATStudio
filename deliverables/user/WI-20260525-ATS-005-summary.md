# WI-20260525-ATS-005 Summary

관리자 환불 원장과 Toss cancel API 연동을 구현했다.

## 완료 사항

- `payment_refunds` 엔티티, Repository, schema를 추가했다.
- 환불 상태와 사유 enum을 추가했다.
- 관리자 API를 추가했다.
  - `GET /api/admin/payments/refund-preview/{subscriptionPaymentId}`
  - `GET /api/admin/payments/refunds`
  - `GET /api/admin/payments/refunds/{refundId}`
  - `POST /api/admin/payments/refunds`
  - `POST /api/admin/payments/refunds/{refundId}/approve`
  - `POST /api/admin/payments/refunds/{refundId}/execute`
- Toss billing provider에 `POST /v1/payments/{paymentKey}/cancel` 호출을 추가했다.
- provider cancel은 Basic auth와 저장된 `Idempotency-Key`를 사용한다.
- provider 응답 payload는 allowlist 기반으로만 저장한다.
- 환불 workflow 전환은 `payment_operation_audit_logs`에 남긴다.

## 제외

- 관리자 환불 UI 탭.
- 환불 후 구독 권한 자동 보정.
- 정산, 세금계산서, cash receipt 발급/취소.
