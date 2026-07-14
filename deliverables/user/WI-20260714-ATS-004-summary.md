# WI-20260714-ATS-004 결제 DB 무결성 구현 요약

## 결과

결제 명령과 최종 결제 기록이 Java 매핑과 신규 MySQL 스키마에서 같은 계약을 사용하도록 정렬했습니다.

- `PaymentOrderStatus`에 `PROCESSING`, `PROVIDER_SUCCEEDED`, `PENDING_PROVIDER_CONFIRMATION`을 추가했습니다.
- `PaymentOrder`에 명령 키, 결제 주기 시작일, Provider 시도 번호/멱등 키, 처리 시작 시각을 매핑했습니다.
- 결제 명령 키, Provider 시도 키, 갱신 주기 조합에 고유 제약을 추가했습니다.
- `SubscriptionPayment`에 주문당 하나의 결제 기록과 Provider 거래당 하나의 로컬 결제 기록을 보장하는 고유 제약을 추가하고 거래 ID 길이를 200자로 맞췄습니다.
- 신규 `schema.sql`의 결제 감사 action/target ENUM에 현재 Java ENUM 값을 모두 반영했습니다.
- 기존 DB용 `20260714_payment_db_integrity.sql`을 추가했습니다. 이 패치는 비종결 주문, 중복 최종 결제, 3일 유예 가정 불일치, 중복 갱신 주기를 출력하고 중단하며, 애매한 원장 데이터를 삭제하거나 자동 정리하지 않습니다.

## 검증

- 집중 계약 테스트: 4개 통과
- `gradlew.bat compileJava`: 통과
- 소유한 tracked 파일 `git diff --check`: 통과
- 신규 파일 `git diff --no-index --check`: 공백 오류 없음

DB 연결, DDL 적용, 테스트 데이터 생성 또는 삭제는 수행하지 않았습니다.

## 위험 및 승인

- 수동 패치의 MySQL 8 실행과 Hibernate `validate` 증명은 `WI-20260714-ATS-021`로 이관됩니다.
- 기존 DB에 비종결 주문, 중복 결제, 갱신 주기 중복 또는 3일 유예 가정 불일치가 있으면 패치는 `SQLSTATE 45000`으로 중단됩니다. 해당 행의 처리에는 별도 승인이 필요합니다.
- 실제 DB 적용, disposable DB 생성/삭제, 신규 라이브러리 도입 승인은 사용하지 않았습니다.

## WI 체인

`WI-20260714-ATS-004` 완료 직후 `WI-20260714-ATS-005`, `WI-20260714-ATS-006`, `WI-20260714-ATS-007`의 핸드오프를 `/create-wi-handoff-packet`으로 생성하고 담당 Subagent에게 즉시 위임해야 합니다. `WI-20260714-ATS-018`과 `WI-20260714-ATS-021`은 나머지 선행 WI가 모두 완료된 시점에 트리거합니다.
