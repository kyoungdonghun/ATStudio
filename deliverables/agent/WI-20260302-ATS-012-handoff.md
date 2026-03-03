[WI HEADER]
WI ID: WI-20260302-ATS-012
REQ: REQ-20260302-ATS-012
Agent: se
Depends On: -
Blocks: WI-20260302-ATS-013

[WI SUMMARY]
Why: M-6 CompanyCertificationRepository findByUser 비결정적 + M-7 CompanyCertification 상태전이 무검증 + M-8 Question 상태전이 무검증
Scope (in):
  - CompanyCertificationRepository.java:14 — findByUser → findTopByUserOrderByCreatedAtDesc 변경
  - CompanyCertification.java:42-48 — process() 메서드에 유효 상태전이 검증 추가
    (APPROVED→PENDING 등 역방향 전이 차단, 유효 전이만 허용)
  - Question.java:44-46 — updateStatus() 메서드에 유효 상태전이 검증 추가
    (api-spec 기준 흐름: OPEN→IN_PROGRESS→RESOLVED→CLOSED, CLOSED→OPEN 등 역전이 차단)
  - CompanyCertificationService.java — findByUser 호출부 시그니처 확인 (Optional<> 유지)
  - 관련 테스트 추가
Scope (out): 다른 엔티티/서비스 수정 금지
DoD:
  - CompanyCertification 재신청 시 최신 레코드 반환 (findTopByUserOrderByCreatedAtDesc)
  - 유효하지 않은 CompanyCertification 상태전이 시 BusinessException 발생
  - 유효하지 않은 Question 상태전이 시 BusinessException 발생
  - BUILD SUCCESSFUL, 0 failures
Constraints/Forbidden:
  - CompanyCertificationRepository, CompanyCertification.java, Question.java만 수정
  - 유효 상태전이 규칙:
    - CompanyCertification: PENDING→APPROVED/REVISION_REQUESTED/REJECTED, REVISION_REQUESTED→PENDING
    - Question: OPEN→IN_PROGRESS, IN_PROGRESS→RESOLVED/CLOSED, OPEN→CLOSED, RESOLVED→CLOSED

[ACCEPTANCE CRITERIA]
Functional:
- [ ] findTopByUserOrderByCreatedAtDesc — 복수 레코드 시 최신 1건 반환
- [ ] CompanyCertification.process() — 유효하지 않은 전이(예: APPROVED→PENDING) 시 INVALID_STATE_TRANSITION 예외
- [ ] Question.updateStatus() — 유효하지 않은 전이(예: CLOSED→OPEN) 시 INVALID_STATE_TRANSITION 예외
- [ ] BUSINESS_ERROR에 INVALID_STATE_TRANSITION 없으면 추가 (적절한 HTTP 상태코드)
Quality:
- [ ] BUILD SUCCESSFUL
- [ ] 신규 테스트 포함 전체 테스트 0 failures

[INPUT POINTERS]
Tier 0 (Constitution):
- docs/standards/core-principles.md

Tier 0 (Standards):
- docs/standards/development-standards.md

REQ:
- deliverables/user/REQ-20260302-ATS-012.md

Files:
- src/main/java/com/atstudio/atstudio/repository/CompanyCertificationRepository.java
- src/main/java/com/atstudio/atstudio/entity/CompanyCertification.java:40-55
- src/main/java/com/atstudio/atstudio/entity/Question.java:40-50
- src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java
- src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java
- docs/design/api-spec.md (8.6 Status Flow, 13.5 Review)

[OUTPUT CONTRACT]
User-facing → deliverables/user/WI-20260302-ATS-012-summary.md
Agent-facing → deliverables/agent/WI-20260302-ATS-012-evidence-pack.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines): 수정된 라인 명시
Tests: gradlew.bat test --tests "*CompanyCertification*" --tests "*Question*"
Rollback: git revert
