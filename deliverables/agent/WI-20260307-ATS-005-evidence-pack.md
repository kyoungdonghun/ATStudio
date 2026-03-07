[EVIDENCE PACK]
WI ID: WI-20260307-ATS-005 | Agent: cr | Status: CONDITIONAL PASS

MAJOR (2):
- MAJOR-1: UserSubscriptionService.java:151-152 — `>= 0` → `> 0` → WI-006 수정
- MAJOR-2: UserSubscription.java:upgrade() — pending 필드 미클리어 → WI-006 수정

MINOR (3):
- MINOR-1: ChangeSubscriptionResponse.java:8-9 — billingCycle/status String 타입
- MINOR-2: UserSubscriptionService.java — 동일 플랜 다운그레이드 예약 미방지
- MINOR-3: db-schema.md:518 — "21 Tables" 헤더 불일치 (기존 이슈)

SUGGESTION (2):
- SUGGESTION-1: 다운그레이드 예약 취소 API 부재 (후속 REQ)
- SUGGESTION-2: 엣지 케이스 테스트 누락 (동일 플랜, pending+upgrade)

Verified OK:
- UserSubscriptionResponse.from() null-safety PASS
- upgrade()/cancel() 시그니처 유지 PASS
- pending 컬럼 DB 스키마 정확성 PASS
