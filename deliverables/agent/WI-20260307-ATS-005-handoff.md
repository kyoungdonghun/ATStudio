[WI HEADER]
WI ID: WI-20260307-ATS-005
REQ: REQ-20260307-ATS-007
Agent: cr
Depends On: WI-20260307-ATS-003
Blocks: -

---

[WI SUMMARY]
Why: WI-002 구현 범위(UserSubscription 다운그레이드 예약) 코드 리뷰
Scope (in):
  - UserSubscription.java — pending 필드, schedulePendingChange() 메서드
  - UserSubscriptionService.java — changeSubscription() UPGRADE/DOWNGRADE 분기
  - ChangeSubscriptionResponse.java — changeType 필드
  - UserSubscriptionResponse.java — pendingSubscriptionId, pendingBillingCycle 필드
  - UserSubscriptionServiceTest.java — ChangeSubscription 테스트 케이스
  - UserSubscriptionControllerTest.java — 업데이트된 생성자
  - docs/design/db-schema.md — pending 컬럼 추가 정확성
Scope (out):
  - WI-001 범위 (UtilService, UtilController) — WI-004에서 검토

DoD:
  - 각 파일 코드 품질, 예외 처리, 보안, 표준 준수 검토
  - UPGRADE/DOWNGRADE 판정 로직 정확성 검토
  - CRITICAL/MAJOR 발견 시 명시

Constraints/Forbidden:
  - 코드 직접 수정 금지 — 지적 사항 보고만

---

[ACCEPTANCE CRITERIA]

Quality:
- [ ] CRITICAL 0건
- [ ] MAJOR 건수 명시
- [ ] UPGRADE/DOWNGRADE 분기 로직 검토 완료
- [ ] null-safety (pendingSubscription nullable) 검토 완료

---

[INPUT POINTERS]

Tier 0 (Constitution):
- docs/standards/core-principles.md

Tier 0 (Development Standards):
- docs/standards/development-standards.md

REQ:
- deliverables/user/REQ-20260307-ATS-007.md

Files (리뷰 대상):
- src/main/java/com/atstudio/atstudio/entity/UserSubscription.java
- src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java:135-210
- src/main/java/com/atstudio/atstudio/dto/subscription/ChangeSubscriptionResponse.java
- src/main/java/com/atstudio/atstudio/dto/subscription/UserSubscriptionResponse.java
- src/test/java/com/atstudio/atstudio/service/UserSubscriptionServiceTest.java
- src/test/java/com/atstudio/atstudio/controller/UserSubscriptionControllerTest.java
- docs/design/db-schema.md

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260307-ATS-005-summary.md:
- 리뷰 결과 요약 (CRITICAL/MAJOR/MINOR 건수), 주요 지적 사항

Agent-facing -> deliverables/agent/WI-20260307-ATS-005-evidence-pack.md:
- 파일별 상세 리뷰 코멘트 (severity, 파일경로:라인, 설명, 권고)

Handoff Packet -> deliverables/agent/WI-20260307-ATS-005-handoff.md:
- 이 파일 (추적용)

---

[TRACEABILITY REQUIREMENTS]
Evidence: 각 지적 사항에 파일:라인 포인터 포함
