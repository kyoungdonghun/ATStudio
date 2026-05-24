# WI-20260525-ATS-003 Summary

결제 운영 P2-A 단계로, 환불이나 정산처럼 실제 돈을 움직이는 기능 전에 필요한 영수증 근거 저장과 운영 감사 로그 기반을 구현했다.

## 완료 사항

- `payment_receipts` 테이블/엔티티/Repository를 추가했다.
- `payment_operation_audit_logs` 테이블/엔티티/Repository를 추가했다.
- Toss 정기결제 성공 응답에서 안전한 receipt/cashReceipt 필드만 sanitizer에 포함하도록 확장했다.
- 최초 정기 구독 결제, 업그레이드 차액 결제, 자동 갱신 결제 성공 후 receipt evidence 저장 이벤트를 발행한다.
- receipt evidence 저장은 결제 트랜잭션 commit 이후 처리되며, provider가 영수증 정보를 주지 않으면 조용히 skip한다.
- `GET /api/admin/payments/receipts` read-only API를 추가했다.
- `GET /api/admin/payments/operation-audit-logs` read-only API를 추가했다.
- reconciliation incident 상태 변경 시 actor, before/after status, note, 대상 incident/order 정보를 감사 로그로 남긴다.
- API spec, DB schema, SR-93, payment runbook, payment policy, 최종 인수테스트 체크리스트를 현행화했다.

## 보안 경계

- raw billing key, authKey, customerKey, Toss secret key, raw card data, raw provider payload는 저장/노출하지 않는다.
- receipt evidence payload는 payment key, order ID, status, method, amount, approvedAt, receipt URL/key 같은 최소 sanitized metadata만 저장한다.
- 이번 작업은 provider refund/cancel, cash receipt issue/cancel, settlement import, tax invoice workflow를 구현하지 않는다.

## 검증

- `gradlew.bat test` 통과.
- `python .agents\skills\validate-docs\scripts\validate_docs.py` 통과.
- `git diff --check` 통과. Windows CRLF 안내 warning만 있었고 whitespace error는 없었다.

## 남은 범위

- Toss refund/cancel API와 환불 ledger.
- cash receipt 발급/취소 mutation.
- settlement import/reconciliation.
- tax invoice request/admin workflow.
- admin payment mutation UI/API.
