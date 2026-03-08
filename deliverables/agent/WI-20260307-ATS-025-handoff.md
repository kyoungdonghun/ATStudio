[WI HEADER]
WI ID: WI-20260307-ATS-025
REQ: REQ-20260307-ATS-009
Agent: cr
Depends On: -
Blocks: -

---

[WI SUMMARY]
Why: Track 2-A — api-spec §1~4, §10~11 ↔ 백엔드 코드 정합성 검증 (read-only)
Scope (in):
  - api-spec §1(Track), §2(Tag), §3(Playlist), §4(PlayHistory), §10(Likes), §11(DownloadQueue)
  - 대상 Controller: TrackController, TagController, PlaylistController,
    PlayHistoryController, LikeController, DownloadQueueController
  - 대상 Service: TrackService, TagService, PlaylistService, PlayHistoryService, LikeService, DownloadService
  - 검증 항목:
    1. URL, HTTP Method, 경로 파라미터 일치
    2. 응답 HTTP 상태코드 (ResponseEntity.ok, created, noContent 등)
    3. 권한 설정 (@PreAuthorize, SecurityConfig 규칙)
    4. 요청 DTO 필드명 (api-spec 요청 body ↔ @RequestBody DTO)
    5. 응답 DTO 필드명 (api-spec 응답 fields ↔ record/class 필드)
Scope (out):
  - 파일 수정 금지 (발견·보고만)
  - §5 이후 도메인 (WI-026 담당)
  - 문서↔문서 검증 (WI-023 담당)

DoD:
  - 6개 Controller 각각 불일치 항목 목록 산출
  - CRITICAL/MAJOR/MINOR/SUGGESTION 분류 명시
  - 발견 없으면 "PASS" 명시

Constraints/Forbidden:
  - 절대 파일 수정 금지
  - 판단 근거(api-spec 섹션# + 파일:라인) 증거로 명시

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] TrackController + TrackService ↔ api-spec §1 (1.1~1.8) 검증
      포함: GET /admin (1.8), isActive 파라미터, AdminTrackListItemResponse 필드
- [ ] TagController ↔ api-spec §2 (2.1~2.4) 검증
- [ ] PlaylistController + PlaylistService ↔ api-spec §3 (3.1~3.8) 검증
- [ ] PlayHistoryController ↔ api-spec §4 (4.1~4.3) 검증
- [ ] LikeController ↔ api-spec §10 (10.1~10.3) 검증
- [ ] DownloadQueueController ↔ api-spec §11 (11.1~11.3) 검증

Quality:
- [ ] 이슈별 api-spec 섹션# + Controller/Service 파일:라인 포인터 포함
- [ ] CRITICAL: 구현 없는 API, 상태코드 오류, 권한 누락
- [ ] MAJOR: 필드명/타입 불일치, 경로 파라미터 불일치
- [ ] MINOR: 응답 메시지 불일치, 주석 오류

---

[INPUT POINTERS]

Tier 0 (Standards):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

REQ:
- deliverables/user/REQ-20260307-ATS-009.md

API Spec (검증 기준):
- docs/design/api-spec.md  ← §1, §2, §3, §4, §10, §11

Backend Files (검증 대상):
- src/main/java/com/atstudio/atstudio/controller/TrackController.java
- src/main/java/com/atstudio/atstudio/controller/TagController.java
- src/main/java/com/atstudio/atstudio/controller/PlaylistController.java
- src/main/java/com/atstudio/atstudio/controller/PlayHistoryController.java
- src/main/java/com/atstudio/atstudio/controller/LikeController.java
- src/main/java/com/atstudio/atstudio/controller/DownloadQueueController.java
- src/main/java/com/atstudio/atstudio/service/TrackService.java
- src/main/java/com/atstudio/atstudio/service/PlaylistService.java
- src/main/java/com/atstudio/atstudio/service/DownloadService.java
- src/main/java/com/atstudio/atstudio/dto/track/
- src/main/java/com/atstudio/atstudio/dto/playlist/
- src/main/java/com/atstudio/atstudio/config/SecurityConfig.java

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260307-ATS-025-summary.md :
- Controller별 발견 이슈 요약
Agent-facing -> deliverables/agent/WI-20260307-ATS-025-evidence-pack.md :
- 이슈별 상세 근거 포인터
Handoff Packet -> deliverables/agent/WI-20260307-ATS-025-handoff.md

---

[TRACEABILITY REQUIREMENTS]
Evidence: 이슈별 api-spec 섹션# + 파일:라인 포인터 필수
Tests: 해당 없음 (read-only 검증)
