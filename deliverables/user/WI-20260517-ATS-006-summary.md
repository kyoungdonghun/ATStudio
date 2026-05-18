# WI-20260517-ATS-006 Summary

빌링키 보안 설계를 정리했습니다.

- 빌링키는 절대 평문 저장하지 않고 `billing_key_ciphertext`에 암호화해서 저장합니다.
- 암호화 키 환경변수 이름은 승인된 대로 `PAYMENT_BILLING_KEY_ENCRYPTION_SECRET`를 사용합니다.
- 디버깅/조회용으로는 원문 hash가 아니라 HMAC 기반 `billing_key_fingerprint`만 저장합니다.
- `customerKey`는 `ats_user_{id}`처럼 예측 가능한 값이 아니라 UUID/ULID 기반 랜덤 값으로 발급해 `provider_customer_key`에 저장해야 합니다.
- 프론트엔드에는 billing key, secret key, 암호화 키, 원본 provider payload를 반환하지 않습니다.
- 자동 갱신 취소는 기본적으로 다음 결제 호출을 중지하는 방식이며, 필요 시 provider-side `DELETE /v1/billing/{billingKey}`를 별도 취소/폐기 동작으로 사용합니다.

구현 시에는 AES-GCM 같은 인증 암호화와 versioned ciphertext 포맷을 권장합니다. 로컬/테스트 환경에서는 live key 사용을 명시적으로 막는 가드도 같이 두는 편이 안전합니다.
