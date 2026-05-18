# WI-20260517-ATS-007 Summary

Toss Payments 자동결제 공식 문서를 확인해 Phase C 구현 기준을 정리했습니다.

- 카드 등록은 프론트에서 Toss SDK의 billing auth 흐름을 열고, 성공 redirect에서 `customerKey`와 `authKey`를 받는 구조입니다.
- 서버는 `POST /v1/billing/authorizations/issue`로 `authKey`, `customerKey`를 보내 빌링키를 발급받습니다.
- 발급된 빌링키는 다시 조회할 수 없으므로 서버가 `customerKey`와 매핑해 안전하게 저장해야 합니다.
- 자동결제 승인은 서버가 `POST /v1/billing/{billingKey}`를 호출하며, body에는 `amount`, `customerKey`, `orderId`, `orderName` 등이 들어갑니다.
- Toss는 구독 스케줄링을 제공하지 않으므로 갱신 job은 ATStudio가 직접 구현해야 합니다.
- 자동결제 API는 추가 계약 대상이며, 승인 API timeout은 최소 60초로 잡아야 합니다.
- 구매자가 구독을 취소한 경우 다음 결제일에 해당 빌링키와 `customerKey`로 승인 API를 호출하지 않으면 됩니다.

따라서 Phase C 구현은 Toss의 billing flow를 `/payments/confirm` 단건 결제와 섞지 않고, 별도 billing agreement API와 recurring charge provider로 분리하는 것이 맞습니다.
