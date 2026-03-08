[WI HEADER]
WI ID: WI-20260308-ATS-030
REQ: REQ-20260308-ATS-010
Agent: se
Depends On: -
Blocks: -

---

[WI SUMMARY]
Why: MINOR/SUGGESTION — 백엔드 코드 3건 수정 (응답 일관성 + 보안 defense-in-depth)
Scope (in):
  - LikeController.getMyLikes() `.message("Likes retrieved")` 추가
  - UserSubscriptionController admin 엔드포인트 4개 @PreAuthorize("hasRole('ADMIN')") 추가
  - CompanyCertificationController.processReview() @PreAuthorize("hasRole('ADMIN')") 추가
Scope (out):
  - 서비스/리포지토리 로직 변경 금지
  - 다른 도메인 파일 수정 금지
  - DTO 필드 변경 금지

DoD:
  - LikeController.getMyLikes()에 .message("Likes retrieved") 추가
  - UserSubscriptionController listAll/getDetail/adminUpdate/adminCancel @PreAuthorize 추가
  - CompanyCertificationController.processReview() @PreAuthorize 추가
  - ./gradlew test 전체 통과

Constraints/Forbidden:
  - SUGGESTION이므로 SecurityConfig URL 레벨 보호 제거 금지 (이중 보호가 목적)
  - 기존 기능 동작 변경 금지

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] LikeController.getMyLikes() — .message("Likes retrieved") 추가
- [ ] UserSubscriptionController.listAll() — @PreAuthorize("hasRole('ADMIN')") 추가
- [ ] UserSubscriptionController.getDetail() — @PreAuthorize("hasRole('ADMIN')") 추가
- [ ] UserSubscriptionController.adminUpdate() — @PreAuthorize("hasRole('ADMIN')") 추가
- [ ] UserSubscriptionController.adminCancel() — @PreAuthorize("hasRole('ADMIN')") 추가
- [ ] CompanyCertificationController.processReview() — @PreAuthorize("hasRole('ADMIN')") 추가

Quality:
- [ ] ./gradlew test 전체 통과 (failures=0)
- [ ] 기존 테스트 영향 없음

---

[INPUT POINTERS]

Tier 0 (Standards):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

REQ:
- deliverables/user/REQ-20260308-ATS-010.md

Phase 1 검증 근거:
- deliverables/agent/WI-20260307-ATS-025-evidence-pack.md  ← MINOR-004 (LikeController message 누락)
- deliverables/agent/WI-20260307-ATS-026-evidence-pack.md  ← SUGGESTION-001/002 (@PreAuthorize 누락)

Files (수정 대상):
- src/main/java/com/atstudio/atstudio/controller/LikeController.java
- src/main/java/com/atstudio/atstudio/controller/UserSubscriptionController.java
- src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260308-ATS-030-summary.md
Agent-facing -> deliverables/agent/WI-20260308-ATS-030-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260308-ATS-030-handoff.md

---

[TRACEABILITY REQUIREMENTS]
Evidence: 수정 파일:라인 포인터 포함
Tests: ./gradlew test 실행 결과 (테스트 수, failures=0 확인)
