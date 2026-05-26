# WI-20260526-ATS-003 Summary

## Result

- Settlement import/reconciliation 보안·민감정보 경계를 점검했다.
- 허용 필드는 order ID, provider, provider payment key, provider settlement ID, 정산일, 지급일, 금액, 상태, mismatch reason, source file name, source row number로 제한했다.
- 금지 필드는 raw card data, CVC/expiry, billing key, authKey, customerKey, Toss secret, raw provider payload, bank account secret로 유지했다.
- 현재 구현은 provider API를 호출하지 않고, imported row에서 support-safe 필드만 `sourcePayload`에 축약 저장한다.
- admin endpoint는 기존 `/api/admin/payments` controller의 `hasRole('ADMIN')` 경계를 따른다.

## Changed Files

- 코드 변경 없음. WI-20260526-ATS-002 구현과 WI-20260526-ATS-001 설계를 기준으로 검토했다.

## Verification

- `AdminPaymentSettlementService`가 raw provider payload를 저장하지 않고 allowlist 기반 `sourcePayload`만 구성하는 것을 확인했다.
- `AdminPaymentSettlementResponse`가 raw secret/card/billing key/authKey/customerKey 필드를 반환하지 않는 것을 확인했다.

## Residual Risk

- 운영자가 업로드하는 CSV 자체에 불필요한 민감정보 컬럼을 넣을 수 있으므로, UI와 문서에서 “정해진 템플릿만 사용”을 안내해야 한다.
