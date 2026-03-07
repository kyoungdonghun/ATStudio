[WI HEADER]
WI ID: WI-20260307-ATS-002
REQ: REQ-20260307-ATS-007
Agent: se
Depends On: -
Blocks: WI-20260307-ATS-003

---

[WI SUMMARY]
Why: 구독 다운그레이드 즉시 적용 버그 수정 — pending 컬럼 추가 + 서비스 로직 UPGRADE/DOWNGRADE 분기
Scope (in):
  - UserSubscription 엔티티에 pending 필드 2개 추가:
      * pendingSubscription: @ManyToOne(fetch=LAZY), nullable, FK → subscriptions.id
      * pendingBillingCycle: @Enumerated(EnumType.STRING), nullable, VARCHAR 10
  - UserSubscription에 schedulePendingChange(Subscription, BillingCycle) 메서드 추가
  - UserSubscriptionService.changeSubscription() 로직 분기:
      * 판정 기준: newPlan.priceMonthly >= currentPlan.priceMonthly → UPGRADE, 아니면 DOWNGRADE
      * UPGRADE: 기존 current.upgrade() + paymentService.processPayment() 유지
      * DOWNGRADE: current.schedulePendingChange(newPlan, billingCycle) 호출, payment 없음
  - UserSubscriptionResponse에 pending 필드 추가:
      * pendingSubscriptionId (Long, nullable)
      * pendingBillingCycle (String, nullable)
  - docs/design/db-schema.md에 user_subscriptions 테이블 컬럼 2건 추가 반영
  - 신규 테스트 케이스 작성 (UPGRADE/DOWNGRADE 분기 검증)
Scope (out):
  - 다운그레이드 예약 적용 스케줄러 (scheduled job) — 별도 REQ
  - UtilService/UtilController 수정 — WI-001에서 처리
  - T-1, T-2 범위 일체 수정 금지

DoD:
  - UPGRADE 시 기존 즉시 적용 동작 유지
  - DOWNGRADE 시 pending 필드 저장, 현재 구독 유지, payment 미호출
  - UserSubscriptionResponse에 pendingSubscriptionId, pendingBillingCycle 포함
  - db-schema.md user_subscriptions 섹션 업데이트
  - 기존 UserSubscriptionServiceTest 전체 통과 + 신규 케이스 추가

Constraints/Forbidden:
  - PaymentService mock 변경 금지
  - UtilService 수정 금지 (WI-001과 충돌 방지)
  - 기존 upgrade() / cancel() 메서드 시그니처 변경 금지 (다른 호출부 보호)
  - DB migration 파일 별도 생성 불필요 — 엔티티 JPA 어노테이션으로만 처리 (ddl-auto)

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] UserSubscription 엔티티에 pendingSubscription, pendingBillingCycle 필드 존재
- [ ] changeSubscription() — 비싼 플랜으로 변경 시 UPGRADE 경로 실행 (기존 동작)
- [ ] changeSubscription() — 저렴한 플랜으로 변경 시 DOWNGRADE 경로 실행:
      현재 구독 유지, pending 필드 저장, paymentService.processPayment() 미호출
- [ ] GET /api/user-subscriptions/me 응답에 pendingSubscriptionId, pendingBillingCycle 포함 (null 허용)
- [ ] ChangeSubscriptionResponse에 changeType (UPGRADE|DOWNGRADE) 필드 추가

Quality:
- [ ] 기존 UserSubscriptionServiceTest 전체 통과
- [ ] 신규 테스트: changeSubscription_upgrade_immediate (UPGRADE 즉시 적용 + payment 호출 verify)
- [ ] 신규 테스트: changeSubscription_downgrade_pending (DOWNGRADE pending 저장 + payment 미호출 verify)
- [ ] db-schema.md 업데이트 완료

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
- src/main/java/com/atstudio/atstudio/entity/UserSubscription.java
- src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java:135-186  ← changeSubscription() 전체
- src/main/java/com/atstudio/atstudio/dto/subscription/UserSubscriptionResponse.java
- src/test/java/com/atstudio/atstudio/service/UserSubscriptionServiceTest.java
- docs/design/db-schema.md  ← user_subscriptions 테이블 컬럼 추가

Files (참조용 — 수정 금지):
- src/main/java/com/atstudio/atstudio/entity/Subscription.java  ← priceMonthly 필드 확인
- src/main/java/com/atstudio/atstudio/entity/enums/BillingCycle.java
- src/main/java/com/atstudio/atstudio/repository/SubscriptionRepository.java
- src/main/java/com/atstudio/atstudio/dto/subscription/ChangeSubscriptionResponse.java

Files (수정 가능 — ChangeSubscriptionResponse에 changeType 추가 시):
- src/main/java/com/atstudio/atstudio/dto/subscription/ChangeSubscriptionResponse.java

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260307-ATS-002-summary.md:
- 변경 파일 목록, UPGRADE/DOWNGRADE 분기 로직 요약, 테스트 결과, DB 스키마 변경 내역

Agent-facing -> deliverables/agent/WI-20260307-ATS-002-evidence-pack.md:
- 변경 파일 목록 + 핵심 코드 스니펫 (schedulePendingChange, changeSubscription 분기)
- 신규 테스트 케이스 목록 및 실행 결과
- db-schema.md 변경 전후 diff
- 롤백 방법 (git revert 대상 파일 목록)

Handoff Packet -> deliverables/agent/WI-20260307-ATS-002-handoff.md:
- 이 파일 (추적용)

---

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 변경된 각 파일의 핵심 라인 범위 명시
Tests: gradlew.bat test --tests "*.UserSubscriptionServiceTest" 실행 결과 포함
Rollback: UserSubscription.java, UserSubscriptionService.java, UserSubscriptionResponse.java, ChangeSubscriptionResponse.java, db-schema.md 파일 목록
