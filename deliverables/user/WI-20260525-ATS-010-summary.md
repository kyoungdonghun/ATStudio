# WI-20260525-ATS-010 Summary

환불 후 권한 보정 원장과 관리자 API를 구현했다.

## 완료 사항

- `payment_entitlement_corrections` 엔티티, enum, Repository, schema를 추가했다.
- 권한 보정 관리자 API 6종을 추가했다.
  - `POST /api/admin/payments/entitlement-correction-preview`
  - `GET /api/admin/payments/entitlement-corrections`
  - `GET /api/admin/payments/entitlement-corrections/{correctionId}`
  - `POST /api/admin/payments/entitlement-corrections`
  - `POST /api/admin/payments/entitlement-corrections/{correctionId}/approve`
  - `POST /api/admin/payments/entitlement-corrections/{correctionId}/execute`
- 보정 실행 시 `user_subscriptions`를 명시된 목표 상태로 변경하도록 했다.
- 선택 시 local `billing_agreements`만 취소 처리하며, provider billing key 삭제 API는 호출하지 않는다.
- `payment_operation_audit_logs`에 권한 보정 request/approve/process/success 이벤트를 남긴다.

## 남은 범위

- first-class admin refund/entitlement correction UI.
- settlement import/reconciliation.
- tax invoice request/admin workflow.
