[WI HEADER]
WI ID: WI-20260307-ATS-006
REQ: REQ-20260307-ATS-007
Agent: se
Depends On: WI-20260307-ATS-004, WI-20260307-ATS-005
Blocks: -

[WI SUMMARY]
Why: cr 리뷰(WI-004/005) MAJOR 4건 수정
Scope (in):
  - M-1: UserSubscriptionService.java changeSubscription() 판정 조건 수정 (>= 0 → > 0)
  - M-2: UserSubscription.java upgrade() 메서드에 pending 클리어 추가
  - M-3: UtilService.java previewSubscriptionChange() 판정 조건 수정 (>= 0 → > 0)
  - M-4: UtilServiceTest.java invalidBillingCycle 테스트에 verify 추가
Scope (out): 기타 MINOR/SUGGESTION 수정 금지

DoD:
  - 4건 수정 완료
  - gradlew.bat test 0 failures

Constraints/Forbidden:
  - MINOR/SUGGESTION 수정 금지
  - 기존 로직 구조 변경 금지 (지정 라인만 수정)

[ACCEPTANCE CRITERIA]
- [ ] UserSubscriptionService.java: isUpgrade 판정 조건 > 0 (strictly greater)
- [ ] UserSubscription.java: upgrade() 내 this.pendingSubscription = null; this.pendingBillingCycle = null; 추가
- [ ] UtilService.java: isUpgrade 판정 조건 > 0 (strictly greater)
- [ ] UtilServiceTest.java: previewSubscriptionChange_invalidBillingCycle에 verify(userRepository, never()).findById(any()) 추가
- [ ] gradlew.bat test 0 failures

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md, docs/standards/development-standards.md
REQ: deliverables/user/REQ-20260307-ATS-007.md
cr 리뷰: deliverables/agent/WI-20260307-ATS-004-evidence-pack.md, deliverables/agent/WI-20260307-ATS-005-evidence-pack.md

Files (수정 대상):
- src/main/java/com/atstudio/atstudio/entity/UserSubscription.java (upgrade() 메서드)
- src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java (isUpgrade 판정)
- src/main/java/com/atstudio/atstudio/service/UtilService.java (isUpgrade 판정)
- src/test/java/com/atstudio/atstudio/service/UtilServiceTest.java (invalidBillingCycle 테스트)

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260307-ATS-006-summary.md
Agent-facing -> deliverables/agent/WI-20260307-ATS-006-evidence-pack.md

[TRACEABILITY REQUIREMENTS]
Evidence: 수정 전후 코드 스니펫 포함
Tests: gradlew.bat test 실행 결과
