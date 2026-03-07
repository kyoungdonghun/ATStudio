[WI HEADER]
WI ID: WI-20260307-ATS-001
REQ: REQ-20260307-ATS-007
Agent: se
Depends On: -
Blocks: WI-20260307-ATS-003

---

[WI SUMMARY]
Why: 프론트엔드 착수를 위한 백엔드 보완 — UtilService 기능 2건 추가
Scope (in):
  - T-1: DownloadCountResponse에 nextResetAt 필드 추가 (내일 00:00 KST)
  - T-2: GET /api/utils/subscription-change-preview 신규 엔드포인트
    - 파라미터: subscriptionId (Long, required), billingCycle (String: MONTHLY|YEARLY, required)
    - 응답: changeType (UPGRADE|DOWNGRADE), proratedAmount (BigDecimal), effectiveDate (LocalDate), newPlanName (String), newBillingCycle (String)
    - UPGRADE 판정: newPlan.priceMonthly >= currentPlan.priceMonthly
    - UPGRADE 시 proratedAmount = newPrice - (currentPrice × remainingDays / totalDays), effectiveDate = today
    - DOWNGRADE 시 proratedAmount = 0, effectiveDate = current expiresAt
    - 활성 구독 없는 경우: BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION) → 400
  - 각 기능 단위 테스트 작성 포함
Scope (out):
  - T-3 (UserSubscription 다운그레이드 예약) — WI-002에서 처리
  - PaymentService 변경 금지
  - SecurityConfig 변경 없음 (기존 /api/utils/** USER 접근 규칙 활용)

DoD:
  - T-1: getDownloadCount() 응답에 nextResetAt (LocalDateTime) 포함
  - T-2: previewSubscriptionChange() 메서드 + GET /api/utils/subscription-change-preview 엔드포인트
  - 신규 테스트 케이스 3건 이상 (nextResetAt, preview-upgrade, preview-downgrade, no-subscription)
  - 기존 UtilServiceTest 전체 통과

Constraints/Forbidden:
  - T-2 로직은 UtilService에 구현 (UserSubscriptionService 수정 금지 — WI-002와 충돌 방지)
  - UserSubscription.upgrade() / cancel() 호출 금지 (조회 + 계산만)
  - PaymentService 호출 금지

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] GET /api/utils/download-count 응답에 nextResetAt (내일 00:00) 포함
- [ ] GET /api/utils/subscription-change-preview?subscriptionId=1&billingCycle=MONTHLY 정상 응답
- [ ] UPGRADE 시: changeType="UPGRADE", proratedAmount > 0 (비싼 플랜으로 변경 시), effectiveDate = 오늘
- [ ] DOWNGRADE 시: changeType="DOWNGRADE", proratedAmount = 0, effectiveDate = expiresAt
- [ ] 활성 구독 없을 시 400 응답
- [ ] BillingCycle 잘못된 값 입력 시 400 응답 (enum valueOf try-catch)

Quality:
- [ ] 기존 UtilServiceTest (6건) 전체 통과
- [ ] 신규 테스트 케이스 추가 (nextResetAt 포함 검증, preview-upgrade, preview-downgrade, no-subscription 4케이스 이상)
- [ ] @Transactional(readOnly = true) 클래스 레벨 유지

---

[INPUT POINTERS]

Tier 0 (Constitution):
- docs/standards/core-principles.md

Tier 0 (Development Standards):
- docs/standards/development-standards.md

Tier 1 (Quality):
- docs/policies/quality-gates.md

REQ:
- deliverables/user/REQ-20260307-ATS-007.md

Files (수정 대상):
- src/main/java/com/atstudio/atstudio/dto/util/DownloadCountResponse.java
- src/main/java/com/atstudio/atstudio/service/UtilService.java
- src/main/java/com/atstudio/atstudio/controller/UtilController.java
- src/test/java/com/atstudio/atstudio/service/UtilServiceTest.java

Files (참조용 — 수정 금지):
- src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java:150-167  ← prorated 계산 로직 참조
- src/main/java/com/atstudio/atstudio/entity/UserSubscription.java
- src/main/java/com/atstudio/atstudio/entity/Subscription.java
- src/main/java/com/atstudio/atstudio/repository/SubscriptionRepository.java
- src/main/java/com/atstudio/atstudio/repository/UserSubscriptionRepository.java

Files (신규 생성):
- src/main/java/com/atstudio/atstudio/dto/util/SubscriptionChangePreviewResponse.java

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260307-ATS-001-summary.md:
- 변경 파일 목록, 신규 API 스펙 요약, 테스트 결과

Agent-facing -> deliverables/agent/WI-20260307-ATS-001-evidence-pack.md:
- 변경 파일 목록 + 핵심 코드 스니펫
- 신규 테스트 케이스 목록 및 실행 결과
- 롤백 방법 (git revert 대상 파일 목록)

Handoff Packet -> deliverables/agent/WI-20260307-ATS-001-handoff.md:
- 이 파일 (추적용)

---

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 변경된 각 파일의 핵심 라인 범위 명시
Tests: gradlew.bat test --tests "*.UtilServiceTest" 실행 결과 포함
Rollback: DownloadCountResponse, UtilService, UtilController, SubscriptionChangePreviewResponse 파일 목록
