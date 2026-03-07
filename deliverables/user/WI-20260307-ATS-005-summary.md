# WI-20260307-ATS-005 리뷰 결과 요약

WI: WI-20260307-ATS-005 | REQ: REQ-20260307-ATS-007 | Agent: cr

## 리뷰 범위
UserSubscription 다운그레이드 예약 구현 (T-3)

## 결과

| Severity | Count |
|----------|-------|
| CRITICAL | 0 |
| MAJOR | 2 |
| MINOR | 3 |
| SUGGESTION | 2 |

**판정: CONDITIONAL PASS → WI-006에서 MAJOR 수정 완료**

## 주요 지적 사항

- MAJOR-1: UserSubscriptionService.java:151 — 동일 가격 UPGRADE 오판정 → WI-006에서 `> 0` 수정 ✅
- MAJOR-2: UserSubscription.java:upgrade() — pending 필드 미클리어 → WI-006에서 null 초기화 추가 ✅
- MINOR-1: ChangeSubscriptionResponse String 타입 (MINOR, 보류)
- MINOR-2: 동일 플랜 다운그레이드 예약 미방지 (MINOR, 보류)
- MINOR-3: db-schema.md "21 Tables" 헤더 불일치 (기존 이슈, 보류)
