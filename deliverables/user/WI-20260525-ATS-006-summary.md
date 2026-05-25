# WI-20260525-ATS-006 Summary

환불 기능의 회귀 테스트를 추가하고 전체 검증을 수행했다.

## 완료 사항

- `AdminPaymentRefundServiceTest`를 추가했다.
- `TossBillingProviderTest`에 cancel API 성공 테스트를 추가했다.
- 테스트가 확인하는 범위:
  - 환불 요청은 provider 호출 전에 로컬 원장에 저장된다.
  - 요청 금액은 남은 환불 가능액을 초과할 수 없다.
  - 승인되지 않은 환불은 provider 실행을 할 수 없다.
  - 실행 시 저장된 idempotency key가 재사용된다.
  - Toss cancel 요청은 Basic auth와 `Idempotency-Key`를 사용한다.
  - Toss cancel 응답의 raw card data는 provider payload에 저장되지 않는다.

## 검증

- `gradlew.bat test` 통과.
- `npm run typecheck`, `npm run lint`, `npm test`, `npm run build` 통과.
