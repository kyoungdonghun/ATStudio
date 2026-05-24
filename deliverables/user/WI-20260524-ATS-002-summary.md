# WI-20260524-ATS-002 Summary

결제 대사에서 발견한 불일치를 운영자가 놓치지 않도록 `payment_reconciliation_incidents` 기반의 저장/조회/처리 흐름을 추가했다.

## 완료 사항

- 스케줄 대사 실행 시 로컬/Provider 불일치를 dedupe key 기준으로 incident에 생성 또는 갱신하도록 했다.
- 같은 문제가 반복되면 새 행을 만들지 않고 `occurrenceCount`, `lastDetectedAt`을 갱신한다.
- `RESOLVED` 상태의 문제가 다시 발견되면 `OPEN`으로 재오픈하고, `IGNORED`는 무시 상태를 유지하면서 발생 횟수만 갱신한다.
- 관리자 API로 incident 목록 조회와 `OPEN`, `ACKNOWLEDGED`, `RESOLVED`, `IGNORED` 상태 변경을 지원한다.
- 운영자 이메일 알림은 기본 비활성화이며, `PAYMENT_RECONCILIATION_NOTIFICATION_ENABLED=true`와 `PAYMENT_OPERATIONS_OPERATOR_EMAIL`이 설정된 경우에만 발송된다.
- SR-93, payment runbook, payment integration design, API spec, DB schema, docs index, project registry를 121 API / 31 DB table 기준으로 현행화했다.

## 검증

- `.\gradlew.bat test --tests "com.atstudio.atstudio.service.PaymentReconciliationServiceTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationIncidentServiceTest"` 통과.
- `.\gradlew.bat test` 통과.
- `python .agents\skills\validate-docs\scripts\validate_docs.py` 통과.
- `git diff --check` 확인: whitespace error 없음.

## 남은 범위

- 이번 WI는 incident 저장과 운영자 가시성 API까지다.
- 환불/취소 자동화, 관리자 결제 mutation, Slack/SMS/in-app 알림, 관리자 incident frontend UI, 멀티서버 scheduler lock, webhook, 멀티 PG는 별도 REQ/SR 범위로 남겨두었다.
