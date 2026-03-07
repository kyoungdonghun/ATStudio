[EVIDENCE PACK]
WI ID: WI-20260307-ATS-004 | Agent: cr | Status: CONDITIONAL PASS

MAJOR (2):
- M-1: UtilService.java:119 — `>= 0` → `> 0` (동일 가격 UPGRADE 오판정) → WI-006 수정
- M-2: UtilServiceTest.java — previewSubscriptionChange_invalidBillingCycle verify 누락 → WI-006 수정

MINOR (3):
- m-1: UtilService.java:141,150 — "UPGRADE"/"DOWNGRADE" 문자열 리터럴
- m-2: DownloadCountResponse.java:7 — @JsonInclude(NON_NULL) primitive에 무의미
- m-3: UtilServiceTest.java — YEARLY billingCycle 테스트 없음

SUGGESTION (2):
- S-1: prorated 로직 UserSubscriptionService와 중복 (DRY)
- S-2: effectiveDate LocalDate 타입 — 현재 적절, 향후 검토

Security: 이슈 없음 (@AuthenticationPrincipal, BillingCycle try-catch OK)
