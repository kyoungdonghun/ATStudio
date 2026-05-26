# WI-20260526-ATS-004 Summary

## 작업 요약

관리자 결제 운영 화면(`/admin/payments`)에 정산 탭을 추가했다.

- `frontend/src/api/admin.ts`에 settlement list/import/reconcile/ignore API client와 타입을 추가했다.
- `PaymentReadOnlyPage`에 `정산` 탭, status/source/date 필터, CSV import 패널, import 결과 요약, 누락 후보 확인, settlement row table, IGNORE 메모 액션을 추가했다.
- 정산 작업은 운영자 확인용 accounting evidence 흐름으로만 노출했고, 구독/결제/환불/provider 상태를 변경하지 않는다는 경계를 UI 문구와 작업 흐름에 반영했다.
- CSV import만 지원한다. Excel 원본은 CSV로 export한 뒤 import하는 정책으로 정리했다.

## 검증

- `npm run typecheck` passed.
- `npm run lint` passed.
- `npx prettier --check src\api\admin.ts src\pages\admin\PaymentReadOnlyPage.tsx src\pages\admin\PaymentReadOnlyPage.module.css` passed.
- `npm run build` passed.

## 인수 확인 포인트

- `/admin/payments`에서 `정산` 탭이 보인다.
- settlement CSV import 후 batch, row count, duplicate count, failed count, status count가 보인다.
- settlement row 목록에서 상태, 주문번호, provider payment key, 사용자, source, 금액, 정산일, local mapping, mismatch reason을 확인할 수 있다.
- 누락 후보 확인은 provider evidence가 없는 local DONE 결제 후보만 생성하는 backend API로 연결된다.
- IGNORE 처리는 메모를 요구하고 row 삭제가 아니라 `IGNORED` workflow 상태로 남긴다.

## 남은 일

- WI-20260526-ATS-005에서 API/DB/UI/SR 문서 현행화와 전체 검증을 닫는다.
