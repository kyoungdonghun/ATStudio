# WI-20260525-ATS-015 Summary

## Result

- `/admin/payments`에 `영수증`, `감사로그`, `환불`, `권한 보정` 탭을 추가했다.
- 환불 탭은 결제내역 ID 기반 preview, refund request 생성, approve, provider execute를 지원한다.
- 권한 보정 탭은 succeeded refund 기반 preview, correction request 생성, approve, execute를 지원한다.
- 결제내역 탭에서 `DONE` 결제에 대해 환불 preview로 바로 이동할 수 있게 했다.
- 환불과 권한 보정은 자동 결합하지 않고, 기존 정책대로 별도 admin-confirmed workflow로 유지했다.
- provider 환불 실행과 local 권한 보정 실행은 각각 `환불 실행`, `권한 보정 실행` 문구 입력을 요구하도록 보강했다.
- `사용자 구독 관리`는 일반 구독 상태 편집, `결제 운영`은 결제 근거가 있는 환불/권한 보정으로 경계를 분리했다.

## Changed Files

- `frontend/src/api/admin.ts`
- `frontend/src/pages/admin/PaymentReadOnlyPage.tsx`
- `frontend/src/pages/admin/PaymentReadOnlyPage.module.css`

## Verification

- `npm run typecheck` passed.
- `npm run lint` passed.
- `npm run build` passed.

## Manual Acceptance Points

- 관리자 계정으로 `/admin/payments`에 접속해 새 탭 4개가 보이는지 확인한다.
- 환불/권한 보정 mutation은 테스트 데이터에서만 실행한다.
- 환불 실행/권한 보정 실행은 정확한 확인 문구 입력 없이는 진행되지 않는지 확인한다.
- 화면에 raw billing key, authKey, customerKey, Toss secret, raw card data, raw provider payload가 보이지 않는지 확인한다.
