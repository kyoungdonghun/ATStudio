[WI HEADER]
WI ID: WI-20260307-ATS-004
REQ: REQ-20260307-ATS-007
Agent: cr
Depends On: WI-20260307-ATS-003
Blocks: -

---

[WI SUMMARY]
Why: WI-001 구현 범위(UtilService 추가) 코드 리뷰
Scope (in):
  - DownloadCountResponse.java — nextResetAt 필드
  - SubscriptionChangePreviewResponse.java — 신규 DTO
  - UtilService.java — previewSubscriptionChange() 메서드, nextResetAt 계산
  - UtilController.java — subscription-change-preview 엔드포인트
  - UtilServiceTest.java — 신규 테스트 케이스 품질
Scope (out):
  - WI-002 범위 (UserSubscription, UserSubscriptionService) — WI-005에서 검토

DoD:
  - 각 파일 코드 품질, 예외 처리, 보안, 표준 준수 검토
  - CRITICAL/MAJOR 발견 시 명시

Constraints/Forbidden:
  - 코드 직접 수정 금지 — 지적 사항 보고만

---

[ACCEPTANCE CRITERIA]

Quality:
- [ ] CRITICAL 0건
- [ ] MAJOR 건수 명시
- [ ] 각 파일별 리뷰 코멘트

---

[INPUT POINTERS]

Tier 0 (Constitution):
- docs/standards/core-principles.md

Tier 0 (Development Standards):
- docs/standards/development-standards.md

REQ:
- deliverables/user/REQ-20260307-ATS-007.md

Files (리뷰 대상):
- src/main/java/com/atstudio/atstudio/dto/util/DownloadCountResponse.java
- src/main/java/com/atstudio/atstudio/dto/util/SubscriptionChangePreviewResponse.java
- src/main/java/com/atstudio/atstudio/service/UtilService.java
- src/main/java/com/atstudio/atstudio/controller/UtilController.java
- src/test/java/com/atstudio/atstudio/service/UtilServiceTest.java

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260307-ATS-004-summary.md:
- 리뷰 결과 요약 (CRITICAL/MAJOR/MINOR 건수), 주요 지적 사항

Agent-facing -> deliverables/agent/WI-20260307-ATS-004-evidence-pack.md:
- 파일별 상세 리뷰 코멘트 (severity, 파일경로:라인, 설명, 권고)

Handoff Packet -> deliverables/agent/WI-20260307-ATS-004-handoff.md:
- 이 파일 (추적용)

---

[TRACEABILITY REQUIREMENTS]
Evidence: 각 지적 사항에 파일:라인 포인터 포함
