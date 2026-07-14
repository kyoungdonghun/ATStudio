# WI-20260714-ATS-008 환불 예약 동시성 보정 요약

## 결과

환불 요청 생성 시 원본 `SubscriptionPayment`를 `PESSIMISTIC_WRITE`로 잠근 뒤 환불 가능 상태와 예약 금액을 다시 검증하도록 보정했습니다.

- 잠금 조회는 사용자, 사용자 구독, 구독 상품, 결제 주문, 빌링 약정 연관 그래프를 함께 로드합니다.
- `createRefund`는 잠금 획득 후 `DONE` 상태, Toss 빌링 Provider, Provider 결제 키, 요청 금액과 원금 경계를 검증합니다.
- 예약 합계는 승인 설계의 `REQUESTED`, `APPROVED`, `PROCESSING`, `SUCCEEDED`, `PENDING_PROVIDER_CONFIRMATION` 상태만 포함합니다.
- 예약 합계와 신규 요청 금액이 원 결제 금액을 초과하면 요청을 생성하지 않습니다. 정확히 같은 경계 금액은 허용합니다.
- `previewRefund`는 기존과 같이 잠금 없는 참고 정보로 유지했습니다.
- 실제 Provider 환불 실행, 스키마, `PaymentOrder` 파일, maker-checker 정책은 변경하지 않았습니다.

## 검증

- 환불 서비스 및 리포지토리 잠금 계약 집중 테스트: 11건 통과
- `gradlew.bat compileJava`: 통과
- 소유 파일 whitespace 검사: 통과

## 잔여 한계

현재 테스트는 잠금 선언과 애플리케이션 호출 순서를 검증합니다. H2 또는 Mockito 결과는 MySQL InnoDB의 실제 잠금 대기와 직렬화 의미를 증명하지 않습니다. 승인된 disposable MySQL 8 환경의 `AdminPaymentRefundConcurrencyMySqlTest` 증거는 `WI-20260714-ATS-018`에서 완료해야 합니다.

## WI 체인

`WI-20260714-ATS-008` 완료는 `WI-20260714-ATS-018`의 환불 동시성 통합 검증을 트리거합니다. 핸드오프에 명시된 후속 리뷰 `WI-20260714-ATS-023`, `WI-20260714-ATS-025`에도 본 Evidence Pack을 입력으로 전달해야 합니다.
