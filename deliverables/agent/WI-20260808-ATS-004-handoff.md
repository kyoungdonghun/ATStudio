[WI HEADER]
WI ID: WI-20260808-ATS-004
REQ: REQ-20260808-ATS-002
Agent: qa-integ
Depends On: -
Blocks: WI-20260808-ATS-006
[WI SUMMARY]
Why: 관리자 구독 편집 화면의 플랜 선택 부재와 상태·만료일 조합 문제를 UI/API/도메인/결제 운영 관점에서 확인한다.
Scope (in/out): 프론트엔드, DTO, controller, service, entity, 결제·예약 변경 연계, 테스트와 설계 문서의 읽기 전용 조사만 포함한다. SR/코드/DB/외부 결제 상태는 수정하지 않는다.
DoD: 현재 플랜 변경 가능 여부, 필드별 저장 동작, 상태-날짜 허용 행렬, 플랜 변경 시 부작용과 두 대안이 근거 포인터와 함께 정리된다.
Constraints/Forbidden: `CANCELLED`+미래 만료일을 무조건 오류로 취급하지 않는다. 내부 DB 변경만으로 결제 플랜 변경이 완결된다고 가정하지 않는다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] 플랜 목록 조회와 실제 편집 요청 필드를 구분하여 현재 플랜 변경 가능 여부를 판정한다.
- [ ] `ACTIVE`, `CANCELLED`, `EXPIRED`와 `expiresAt`의 허용·거부 조합을 제안한다.
- [ ] 플랜 변경을 추가할 경우 사용자 유형, 활성 플랜, 결제 주기, 외부 결제, 예약 변경, 감사 요구를 분석한다.
- [ ] 플랜 변경 지원과 긴급 상태 교정 전용 유지의 대안을 비교하고 권고한다.
- [ ] 구현 시 필요한 API·service·UI·회귀 테스트를 제시한다.
Performance:
- [ ] 해당 없음(읽기 전용 조사).
Quality:
- [ ] UI 표시, API 요청, 저장 상태, 결제/예약 부작용을 분리한다.
- [ ] 코드와 기존 설계/SR의 불일치를 명시한다.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred from Task):
- docs/policies/quality-gates.md
- docs/policies/security-policy.md

Tier 2 (Tech Stack and Task Context):
- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/AGENTS.md
- docs/design/api-spec.md
- docs/design/usecase/user-subscription.md
- docs/SR/SR-14.md

REQ/Context Docs:
- deliverables/user/REQ-20260808-ATS-002.md

Files:
- frontend/src/pages/admin/UserSubscriptionManagePage.tsx
- frontend/src/api/userSubscriptions.ts
- src/main/java/com/atstudio/atstudio/dto/subscription/AdminUpdateSubscriptionRequest.java
- src/main/java/com/atstudio/atstudio/controller/UserSubscriptionController.java
- src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java
- src/main/java/com/atstudio/atstudio/entity/UserSubscription.java
- src/test/java/com/atstudio/atstudio/service/UserSubscriptionServiceTest.java
- src/main/java/com/atstudio/atstudio/entity/Payment.java
- src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java

Repro/Logs:
- `rg -n "AdminUpdateSubscriptionRequest|adminUpdate|fetchAdminSubscriptionPlans|pendingSubscription|expiresAt|EXPIRED|CANCELLED" frontend/src src/main/java src/test/java docs/design/usecase/user-subscription.md docs/SR/SR-14.md`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260808-ATS-004-summary.md :
- 현재 판정, 상태 행렬, 플랜 변경 대안, 위험
Agent-facing -> deliverables/agent/WI-20260808-ATS-004-evidence-pack.md :
- Evidence pointers, 교차 레이어 조사, 테스트 제안, SR-97 필수 요구
Handoff Packet -> deliverables/agent/WI-20260808-ATS-004-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: 실행하지 않더라도 필요한 API·도메인·UI 회귀 테스트를 명시
Rollback (if needed): 읽기 전용 조사 산출물 제거 방법 기록
