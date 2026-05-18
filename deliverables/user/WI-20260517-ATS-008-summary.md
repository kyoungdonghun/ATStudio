# WI-20260517-ATS-008 Summary

빌링키 저장 기반을 구현했습니다.

- `BillingAgreement` 엔티티와 `BillingAgreementStatus`를 추가했습니다.
- `billing_agreements` 수동 스키마와 `payment_orders`/`subscription_payments`의 `billing_agreement_id` 추적 컬럼을 추가했습니다.
- `BillingAgreementRepository`에 사용자/provider 조회, provider customerKey 조회, 갱신 대상 조회를 추가했습니다.
- `BillingKeyCrypto`는 `PAYMENT_BILLING_KEY_ENCRYPTION_SECRET` 기반 AES-GCM 암호화와 HMAC-SHA256 fingerprint를 제공합니다.
- `BillingCustomerKeyGenerator`는 Toss 규격에 맞는 랜덤 `providerCustomerKey`를 생성합니다.
- `application.yml`과 `application-local.example.yml`에 빌링키 암호화 키 설정 자리를 추가했습니다.

검증:

- `./gradlew.bat test --tests "com.atstudio.atstudio.service.payment.billing.BillingKeyCryptoTest" --tests "com.atstudio.atstudio.service.payment.billing.BillingCustomerKeyGeneratorTest" --tests "com.atstudio.atstudio.entity.BillingAgreementTest" --tests "com.atstudio.atstudio.repository.BillingAgreementRepositoryTest" --tests "com.atstudio.atstudio.entity.EntityDefaultValueTest"` 통과.
- `./gradlew.bat test --tests "com.atstudio.atstudio.service.PaymentApplicationServiceTest" --tests "com.atstudio.atstudio.service.payment.provider.TossPaymentProviderTest"` 통과.
- `./gradlew.bat test` 전체 통과.
