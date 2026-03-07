# WI-20260307-ATS-004 리뷰 결과 요약

WI: WI-20260307-ATS-004 | REQ: REQ-20260307-ATS-007 | Agent: cr

## 리뷰 범위
UtilService 추가 구현 (T-1 nextResetAt + T-2 subscription-change-preview)

## 결과

| Severity | Count |
|----------|-------|
| CRITICAL | 0 |
| MAJOR | 2 |
| MINOR | 3 |
| SUGGESTION | 2 |

**판정: CONDITIONAL PASS → WI-006에서 MAJOR 수정 완료**

## 주요 지적 사항

- M-1: UtilService.java:119 — 동일 가격 플랜 시 `>= 0` 오판정 → WI-006에서 `> 0`으로 수정 ✅
- M-2: UtilServiceTest.java — invalidBillingCycle 테스트 verify 누락 → WI-006에서 추가 ✅
- m-1: changeType 문자열 리터럴 사용 (MINOR, 보류)
- m-2: DownloadCountResponse @JsonInclude 불필요 (MINOR, 보류)
- m-3: YEARLY billingCycle 테스트 미검증 (MINOR, 보류)
