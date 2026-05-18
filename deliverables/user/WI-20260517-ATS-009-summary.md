# WI-20260517-ATS-009 Summary

반복 결제 provider 계층과 Toss Billing 어댑터를 구현했습니다.

- `RecurringPaymentProvider`와 prepare/confirm/charge/cancel command/result DTO를 추가했습니다.
- `TossBillingProvider`에서 빌링키 발급, 빌링키 결제, 빌링키 삭제 API 호출을 분리했습니다.
- 서버 secret key는 Basic Auth로 서버 내부에서만 사용하고, charge 요청에는 idempotency key를 전달할 수 있게 했습니다.
- Toss 자동결제 응답이 최대 60초까지 걸릴 수 있다는 기준을 반영해 billing read timeout 기본값을 60000ms로 잡았습니다.
- billing auth success/fail URL, issue/charge/delete URL, timeout 설정을 `application.yml`과 `application-local.example.yml`에 추가했습니다.
- raw billing key는 result로만 전달하고, provider payload metadata에는 포함하지 않도록 sanitizing했습니다.

검증:

- `./gradlew.bat test --tests "com.atstudio.atstudio.service.payment.provider.recurring.TossBillingProviderTest"` 통과.
- `./gradlew.bat test --tests "com.atstudio.atstudio.service.payment.provider.recurring.TossBillingProviderTest" --tests "com.atstudio.atstudio.service.payment.billing.BillingKeyCryptoTest" --tests "com.atstudio.atstudio.service.payment.billing.BillingCustomerKeyGeneratorTest" --tests "com.atstudio.atstudio.entity.BillingAgreementTest" --tests "com.atstudio.atstudio.repository.BillingAgreementRepositoryTest" --tests "com.atstudio.atstudio.service.payment.provider.TossPaymentProviderTest"` 통과.
- `./gradlew.bat test` 전체 통과.
- `.agents\skills\validate-docs\scripts\validate_docs.py` 통과.
- `git diff --check` 통과. CRLF 변환 경고만 있었습니다.

남은 흐름:

- WI-20260517-ATS-010에서 이 provider를 사용하는 billing agreement API를 연결합니다.
- WI-20260517-ATS-011에서 실제 갱신 스케줄러와 실패 정책을 연결합니다.
