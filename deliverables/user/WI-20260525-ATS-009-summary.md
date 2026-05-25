# WI-20260525-ATS-009 Summary

환불 후 권한 보정의 설계 경계를 확정했다.

## 완료 사항

- 환불과 권한 보정은 자동 연결하지 않고 별도 승인 작업으로 분리했다.
- 이전 플랜을 결제 이력에서 추정해 롤백하지 않고, 관리자가 목표 플랜/주기/상태/만료일을 명시하도록 정했다.
- local billing agreement 취소는 provider billing key 삭제와 별개인 로컬 상태 변경으로 정의했다.
- 권한 보정 원장, preview/request/approve/execute/read API, audit log 기록이 필요한 범위로 확정됐다.

## 결정

- 권한 보정은 `SUCCEEDED` 환불 건 이후에만 생성한다.
- 실행 전에는 원장에 before/target snapshot을 저장한다.
- 실행은 승인된 보정 건에 대해서만 가능하다.
- 관리자 UI 탭은 이번 범위에서 제외하고 backend/API-only로 먼저 닫는다.
