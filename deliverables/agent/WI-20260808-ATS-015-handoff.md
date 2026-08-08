[WI HEADER]
WI ID: WI-20260808-ATS-015
REQ: REQ-20260808-ATS-004
Agent: se
Depends On: WI-20260808-ATS-014
Blocks: WI-20260808-ATS-016
[WI SUMMARY]
Why: SR-97 관리자 구독 편집을 검증된 로컬 권한 보정 workflow로 전환한다.
Scope (in/out): 상태·만료일 행렬, 플랜 선택, pending/billing agreement 처리 선택, preview·사유·확인·감사·잠금. Toss 결제·환불·빌링키 삭제 호출은 금지.
DoD: 모순 상태 거절; CANCELLED 유예 허용; 진행 중 주문 차단; 외부 결제 비실행 표시; 전후 상태 감사와 회귀 테스트.
Constraints/Forbidden: 현재 브랜치에서만 작업한다. 무관 파일·기존 ZIP·비밀값을 수정하지 않는다. 파일 삭제, 스키마 변경, 실제 데이터 변경, 외부 결제 호출은 승인 없이 금지한다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] 모순 상태 거절; CANCELLED 유예 허용; 진행 중 주문 차단; 외부 결제 비실행 표시; 전후 상태 감사와 회귀 테스트.
Performance:
- [ ] 컬렉션·동시성·미디어 처리 경로에서 기존 대비 비정상적인 N+1, deadlock, 반복 디코딩을 만들지 않는다.
Quality:
- [ ] 대상 테스트와 관련 정적 검사가 통과한다.
- [ ] 변경 파일과 재현·롤백 근거를 남긴다.

[INPUT POINTERS]
Tier 0/1/2 and context:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/design/usecase/user-subscription.md
- docs/design/payment-operations-runbook.md
- docs/SR/SR-97.md
- deliverables/user/REQ-20260808-ATS-004.md
Files:
- src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java
- src/main/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionService.java
- frontend/src/pages/admin/UserSubscriptionManagePage.tsx
- frontend/src/pages/admin/PaymentOperationsPage.tsx
- 관련 API, DTO, repository, audit, 테스트 파일

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260808-ATS-015-summary.md
Agent-facing -> deliverables/agent/WI-20260808-ATS-015-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260808-ATS-015-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers, executed commands, test results, risks, rollback, and next blocked WI status are required.
