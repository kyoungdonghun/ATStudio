# WI-20260525-ATS-002 Summary

환불·영수증·정산·세금계산서 기능을 바로 구현하기 전에, 운영 정책과 다음 구현 경계를 먼저 설계했다.

## 완료 사항

- [Payment Refund, Receipt, Settlement, and Tax Invoice Policy](../../docs/design/payment-refund-receipt-settlement-policy.md)를 추가했다.
- 환불 정책을 “구독 취소와 별개인 예외적 admin-only 운영”으로 정의했다.
- 전액/부분 환불, 멱등키, provider refund, entitlement correction 분리 원칙을 정리했다.
- 영수증/현금영수증 정책을 정리했다. 현재 정기결제는 카드 billing auth 중심이므로 현금영수증 자동화는 1차 구현 범위가 아니다.
- 정산 정책을 “PG-to-ATStudio merchant settlement”로 한정했다. 창작자 지급/셀러 payout은 별도 도메인으로 분리했다.
- 세금계산서는 우선 HomeTax/ASP/manual 운영 추적 정책으로 잡고, 자동화 전 세무 검토가 필요하다고 명시했다.
- 다음 구현 후보로 `payment_refunds`, `payment_receipts`, `payment_settlements`, `tax_invoice_requests`, admin action audit 흐름을 정리했다.
- SR-93, payment operations runbook, payment integration design, docs index를 현행화했다.

## 핵심 결정

- 사용자 구독 취소는 환불이 아니다. 기본 정책은 다음 갱신 중지와 현재 기간 사용 유지다.
- provider 환불과 구독 권한 보정은 분리된 감사 작업이어야 한다.
- 환불 구현은 반드시 idempotency key와 append-only refund ledger를 먼저 가져야 한다.
- 영수증/현금영수증/세금계산서는 서로 다른 증빙이며 한 상태값으로 합치지 않는다.
- 정산은 먼저 ATStudio 자체 구독 매출의 PG 정산 대사로 다룬다.

## 검증

- `python .agents\skills\validate-docs\scripts\validate_docs.py` 통과.
- `git diff --check` 확인: whitespace error 없음.

## 남은 범위

- 실제 Toss refund/cancel API 구현.
- receipt URL 저장 및 사용자 영수증 화면.
- 현금영수증 발급/취소 API.
- Toss settlement import/reconciliation.
- tax invoice request/admin workflow.
- admin payment mutation과 entitlement correction UI/API.
