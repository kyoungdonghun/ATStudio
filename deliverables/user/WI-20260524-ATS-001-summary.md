# WI-20260524-ATS-001 Summary

## 완료 요약

SR-93 P1 운영 안정화 묶음을 처리했다.

- Toss provider API 조회 기반 reconciliation을 추가했다.
- `GET /api/admin/payments/reconciliation` read-only 진단 endpoint를 추가했다.
- `PROVIDER_DONE_LOCAL_NOT_FINALIZED`, `LOCAL_DONE_PROVIDER_NOT_DONE`, `AMOUNT_MISMATCH`, `PROVIDER_LOOKUP_FAILED` 등 운영자가 볼 수 있는 issue type을 분리했다.
- provider 결제 성공 + 로컬 저장 실패 대응을 위한 Payment Operations Runbook을 작성했다.
- SR-93, payment integration design, API spec, docs index, project registry를 119 API 기준으로 현행화했다.

## 중요한 판단

- Webhook은 이번 구현의 중심으로 두지 않았다.
- Toss billing-key 정기결제는 ATStudio가 직접 charge API를 호출하는 구조이므로, provider API 조회와 로컬 원장 대사를 중심으로 잡았다.
- 실제 환불 자동화, admin 결제 mutation, 정산/세금계산서, 멀티서버 lock, 멀티 PG는 본 WI 범위에서 제외했다.
- DB 스키마 변경은 하지 않았다.

## 검증

- `.\gradlew.bat test --tests "com.atstudio.atstudio.service.PaymentReconciliationServiceTest" --tests "com.atstudio.atstudio.service.payment.provider.recurring.TossBillingProviderTest"` → passed
- `.\gradlew.bat test` → passed
- `python .agents\skills\validate-docs\scripts\validate_docs.py` → passed

## 남은 후속 범위

- 실제 환불/취소 자동화.
- admin 결제 mutation API/UI.
- 영수증, 정산, 세금계산서.
- 멀티서버 scheduler lock.
- legacy endpoint 완전 제거.
- KakaoPay/NaverPay 등 멀티 PG.

