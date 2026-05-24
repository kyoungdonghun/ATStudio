# WI-20260525-ATS-001 Summary

관리자가 결제 대사 불일치 incident를 API 직접 호출 없이 `/admin/payments` 화면에서 확인하고 처리할 수 있도록 결제 운영 화면을 확장했다.

## 완료 사항

- 기존 관리자 결제 화면명을 `결제 운영`으로 정리하고, `대사 Incident` 탭을 추가했다.
- incident 목록을 `전체`, `OPEN`, `ACKNOWLEDGED`, `RESOLVED`, `IGNORED` 상태로 필터링할 수 있게 했다.
- 각 incident row에서 처리 상태와 메모를 수정해 `/api/admin/payments/reconciliation-incidents/{incidentId}/status`로 저장할 수 있게 했다.
- 화면에는 issue type, severity, orderId, user, provider, local/provider status, amount, occurrence count, timestamp, note처럼 운영에 필요한 support-safe 필드만 노출한다.
- raw billing key, authKey, customerKey, Toss secret, raw card data, raw provider payload는 노출하지 않는다.
- SR-93, payment integration design, payment operations runbook, UI screen/list 문서를 현재 구현 기준으로 현행화했다.
- 멀티서버 scheduler lock은 현재 단일 서버 운영 전제라 활성 범위에서 제외하고, 2대 이상 app instance 운영 전 검토 항목으로 문서화했다.

## 검증

- `npm run typecheck` 통과.
- `npm run lint` 통과.
- `npm test` 통과: 14 files / 51 tests.
- `npm run build` 통과.
- `python .agents\skills\validate-docs\scripts\validate_docs.py` 통과.
- `git diff --check` 확인: whitespace error 없음.
- Vite dev server 기동 후 `http://127.0.0.1:5173/admin/payments` HTTP 200 응답 확인.

## 참고

- `npm run format`은 프로젝트 전체 기존 포맷 베이스라인 문제로 151개 파일에서 warning이 발생했다. 이번 WI의 필수 품질 게이트는 아니며, 대량 포맷 변경을 피하기 위해 별도 수정하지 않았다.
- 이번 WI는 incident workflow 상태만 변경한다. 결제 취소, 환불, 구독 권한 보정, billing agreement 변경 같은 금전/권한 mutation은 여전히 별도 REQ/SR 범위다.
