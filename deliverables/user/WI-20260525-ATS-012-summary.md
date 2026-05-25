# WI-20260525-ATS-012 Summary

권한 보정 backend 구현에 맞춰 설계 문서와 운영 문서를 현행화했다.

## 완료 사항

- API spec을 v14, 135 endpoints로 갱신했다.
- DB schema를 v9, 35 tables로 갱신했다.
- `payment_entitlement_corrections` 테이블과 권한 보정 admin API를 문서화했다.
- SR-93에 P2-C 완료 항목을 추가했다.
- payment operations runbook에 권한 보정 preview -> request -> approve -> execute 운영 절차를 추가했다.
- payment policy에서 entitlement correction backend는 구현 완료, first-class admin UI는 후속 범위로 분리했다.
- 최종 인수테스트 체크리스트에 entitlement correction API-only 검증 항목을 추가했다.

## 남은 문서상 후속 범위

- first-class admin refund/entitlement correction UI.
- settlement import/reconciliation.
- tax invoice request/admin workflow.
- webhook/multi-PG.
