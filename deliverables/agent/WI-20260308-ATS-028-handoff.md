[WI HEADER]
WI ID: WI-20260308-ATS-028
REQ: REQ-20260307-ATS-009
Agent: se
Depends On: WI-023~027 (Phase 1 검증 완료)
Blocks: -

---

[WI SUMMARY]
Why: Phase 2 — MAJOR 백엔드 코드 수정 3건 (M-3, M-4, M-5)
Scope (in):
  - M-3: TrackController.getTrack() ResponseDTO message 누락 수정
  - M-4: TrackController createTrack/updateTrack/deleteTrack @PreAuthorize 추가
  - M-5: §1.8 GET /api/tracks/admin — @RequestParam name="is_active" 추가
  - 관련 테스트 수정/추가 필요 시 포함
Scope (out):
  - MINOR/SUGGESTION 항목 수정 금지
  - 다른 도메인 파일 수정 금지
  - 서비스/리포지토리 로직 변경 금지

DoD:
  - getTrack() 응답에 message 포함
  - createTrack/updateTrack/deleteTrack에 @PreAuthorize("hasRole('ADMIN')") 추가
  - GET /api/tracks/admin 에서 ?is_active=true/false 정상 바인딩
  - ./gradlew test 전체 통과

Constraints/Forbidden:
  - MINOR/SUGGESTION 항목 함께 수정 금지
  - 기존 기능 동작 변경 금지

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] TrackController.getTrack() — .message("Track retrieved") 추가
- [ ] TrackController.createTrack() — @PreAuthorize("hasRole('ADMIN')") 추가
- [ ] TrackController.updateTrack() — @PreAuthorize("hasRole('ADMIN')") 추가
- [ ] TrackController.deleteTrack() — @PreAuthorize("hasRole('ADMIN')") 추가
- [ ] TrackController.getTracksForAdmin() — @RequestParam(name = "is_active", required = false) Boolean isActive 로 수정
      (기존 @RequestParam(required = false) Boolean isActive → name 속성 추가)

Quality:
- [ ] ./gradlew test 전체 통과 (failures=0)
- [ ] 기존 TrackControllerTest/TrackServiceTest 영향 없음

---

[INPUT POINTERS]

Tier 0 (Standards):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

REQ:
- deliverables/user/REQ-20260307-ATS-009.md

Phase 1 검증 근거:
- deliverables/agent/WI-20260307-ATS-025-evidence-pack.md

Files (수정 대상):
- src/main/java/com/atstudio/atstudio/controller/TrackController.java

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260308-ATS-028-summary.md
Agent-facing -> deliverables/agent/WI-20260308-ATS-028-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260308-ATS-028-handoff.md

---

[TRACEABILITY REQUIREMENTS]
Evidence: 수정 파일:라인 포인터 포함
Tests: ./gradlew test 실행 결과 (테스트 수, failures=0 확인)
