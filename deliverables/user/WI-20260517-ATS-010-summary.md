# WI-20260517-ATS-010 Summary

빌링키 등록 API 흐름을 구현했습니다.

- `POST /api/payments/billing-agreements/prepare`를 추가했습니다.
  - 구독 플랜/주기를 검증하고 `TOSS_BILLING` 결제 의도 주문을 생성합니다.
  - Toss Billing 인증에 필요한 client-safe checkout metadata만 반환합니다.
- `POST /api/payments/billing-agreements/confirm`을 추가했습니다.
  - 로그인 사용자, 주문 소유권, customerKey, 금액, 만료, 구독 플랜/주기를 검증합니다.
  - Toss billing key 발급 후 즉시 최초 정기 결제를 수행합니다.
  - 최초 결제 성공 시에만 구독을 활성화하고 결제 내역을 저장합니다.
  - 최초 결제 실패 시 구독은 활성화하지 않고, 발급된 billing key는 provider delete를 시도한 뒤 로컬 암호문을 정리합니다.
- `GET /api/payments/billing-agreements/me`를 추가했습니다.
  - 현재 자동결제 등록 상태와 결제수단 마스킹 정보, 다음 결제일을 조회합니다.
- `DELETE /api/payments/billing-agreements/me`를 추가했습니다.
  - provider billing key delete 후 자동결제 등록을 취소합니다.
  - 이미 결제된 구독 접근권은 기존 정책대로 `expiresAt`까지 `CANCELLED` 유예 상태로 보존합니다.
- 응답 DTO에는 raw billing key가 포함되지 않도록 분리했습니다.

검증:

- `./gradlew.bat test --tests "com.atstudio.atstudio.service.BillingAgreementApplicationServiceTest" --tests "com.atstudio.atstudio.controller.PaymentControllerTest"` 통과.
- 결제 관련 회귀 묶음 테스트 통과.
- `./gradlew.bat test` 전체 통과.

다음 흐름:

- WI-20260517-ATS-011에서 갱신 스케줄러, 실패 재시도 3회, 3일 유예 정책을 연결합니다.
