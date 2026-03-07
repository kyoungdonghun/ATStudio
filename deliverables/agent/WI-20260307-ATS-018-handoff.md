[WI HEADER]
WI ID: WI-20260307-ATS-018
REQ: REQ-20260307-ATS-008
Agent: cr
Depends On: WI-013/014/015 (Phase 3 완료)
Blocks: -

---

[WI SUMMARY]
Why: Phase 4 — Album/Playlist 도메인 코드 정합성 체크 (Phase 3 문서 기준)
Scope (in):
  - AlbumController.java + AlbumService.java + AlbumRepository.java 코드 체크
  - PlaylistService.java + PlaylistController.java 코드 체크
  - 이 5개 파일(+관련 DTO/Entity) 만
Scope (out):
  - 코드 수정 금지
  - 다른 도메인 파일 탐색 금지

Constraints/Forbidden:
  - 발견 보고만. 코드 수정 절대 금지.

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] AlbumController/AlbumService: PUT /api/albums/{id}/tracks 요청 body 형식 확인
       api-spec §15.8: Request Body = { "trackOrders": [{ "trackId": Long, "order": Integer }] }
       usecase ALBUM-008 (WI-009 MAJOR-002): 래퍼 객체 trackOrders 포함 여부
       코드와 스펙 불일치 시 [CONFLICT] MAJOR 보고
- [ ] PlaylistService: createPlaylist() — PLAYLIST_LIMIT_EXCEEDED 체크 존재 여부
       이미 구현된 것으로 알려져 있음 — 확인 후 [OK] 또는 [CONFLICT] 보고
       PlaylistRepository: countByUserAndIsActiveTrue(User) 쿼리 존재 여부
- [ ] PlaylistController: POST /api/playlists/{id}/tracks 엔드포인트 존재 여부 (SOUND-019)
       존재하지 않으면 [GAP] MAJOR 보고
- [ ] AlbumController: POST /api/albums/{id}/tracks — request body 형식 (단건 trackId vs 배열)
- [ ] Album CRUD endpoint 8개 모두 존재 여부 간략 확인:
       POST /api/albums, GET /api/albums, GET /api/albums/{id}, PUT /api/albums/{id},
       DELETE /api/albums/{id}, POST /api/albums/{id}/tracks,
       DELETE /api/albums/{id}/tracks/{trackId}, PUT /api/albums/{id}/tracks

Quality:
- [ ] 발견 항목별 파일:라인 포인터 포함
- [ ] CONFLICT/GAP/OMISSION/SUGGESTION 형식 준수

---

[INPUT POINTERS]

Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (cr 필수):
- docs/policies/security-policy.md

REQ:
- deliverables/user/REQ-20260307-ATS-008.md

참조 문서 (코드와 대조 기준):
- docs/design/api-spec.md  ← §3 (Playlists), §3.1 (PLAYLIST_LIMIT_EXCEEDED), §15 (Albums), §15.8 (PUT /api/albums/{id}/tracks)
- docs/design/usecase/sound-playlist.md  ← SOUND-002 (재생목록 생성 + 3개 제한)
- docs/design/usecase/sound-track.md  ← SOUND-019 (재생목록에 트랙 추가)

Files (검사 대상):
- src/main/java/com/atstudio/atstudio/controller/AlbumController.java
- src/main/java/com/atstudio/atstudio/service/AlbumService.java
- src/main/java/com/atstudio/atstudio/repository/AlbumRepository.java
- src/main/java/com/atstudio/atstudio/controller/PlaylistController.java
- src/main/java/com/atstudio/atstudio/service/PlaylistService.java
- src/main/java/com/atstudio/atstudio/dto/  (AlbumRequest DTO, AlbumTrackOrderRequest 등)

---

[OUTPUT CONTRACT]

User-facing -> deliverables/user/WI-20260307-ATS-018-summary.md
Agent-facing -> deliverables/agent/WI-20260307-ATS-018-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260307-ATS-018-handoff.md

---

[TRACEABILITY REQUIREMENTS]
Evidence: 발견 항목별 파일:라인 포인터 포함
Format:
  [CONFLICT] 상충: (코드 파일:라인) vs (문서:섹션) — 설명
  [GAP]      누락: 문서에는 있으나 코드에 없음 — 설명
  [OMISSION] 미흡: 부분 구현 — 설명
  [SUGGESTION] 제안: 개선 가능 — 설명
심각도: CRITICAL / MAJOR / MINOR / SUGGESTION
