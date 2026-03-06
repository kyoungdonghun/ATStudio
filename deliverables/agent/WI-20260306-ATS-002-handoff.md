[WI HEADER]
WI ID: WI-20260306-ATS-002
REQ: REQ-20260306-ATS-004
Agent: se
Depends On: -
Blocks: WI-20260306-ATS-003

---

[WI SUMMARY]

Why:
- 백엔드 Java 소스코드 내 한국어 주석에 Playlist를 "앨범"으로 표기한 부분이 잔존할 수 있음
- 문서 정정(WI-001)과 일관성 확보를 위해 코드 주석도 점검·정정 필요

Scope:
- In:
  - `src/main/java/` 전체 스캔 — 한국어 주석에서 Playlist를 "앨범"으로 표기한 부분 정정
  - `src/test/java/` 전체 스캔 — 동일 기준 적용
- Out:
  - 영문 클래스명/메서드명/변수명 (`Playlist`, `PlaylistService`, `playlistRepository` 등) — **변경 금지**
  - API URL (`/api/playlists`) — 변경 금지
  - DB 컬럼/테이블명 (`playlists`, `playlist_id` 등) — 변경 금지
  - Album 도메인 관련 주석 "앨범" 표기 — 변경 금지
  - 문서 파일 (`.md`) — WI-20260306-ATS-001에서 처리

DoD:
1. Java 소스 전체 스캔 완료 (주석 내 한국어 "앨범" Playlist 맥락 탐색)
2. 발견된 경우: "재생목록"으로 정정, evidence-pack에 기록
3. 발견 없는 경우: 스캔 결과 "0건 확인"을 evidence-pack에 기록 (skippable 확인)
4. 빌드 영향 없음 확인 (`gradlew.bat build -x test`)

Constraints/Forbidden:
- **⚠️ CRITICAL**: 영문 식별자 변경 절대 금지 (`Playlist`, `playlist`, `PLAYLIST_LIMIT_EXCEEDED` 등)
- 주석(comment)만 변경 대상. 코드 로직/식별자 변경 금지
- Album 도메인 주석 "앨범" (어드민, `/api/albums`) → 변경 금지

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] `src/` 전체에서 한국어 주석 내 Playlist 지칭 "앨범" Grep 스캔 완료
- [ ] 발견 시: 해당 주석 "재생목록"으로 정정
- [ ] 발견 없을 시: "0건" evidence 기록 (WI skip 처리)
- [ ] 영문 식별자 미변경 확인

Quality:
- [ ] `gradlew.bat build -x test` PASS (주석 변경이 빌드에 영향 없음 확인)
- [ ] evidence-pack에 스캔 커맨드 + 결과 기록

---

[INPUT POINTERS]

Tier 0 (Constitution):
- docs/standards/core-principles.md

Tier 0 (Development Standards):
- docs/standards/development-standards.md

REQ/Context Docs:
- deliverables/user/REQ-20260306-ATS-004.md

Files (스캔 대상):
- src/main/java/com/atstudio/atstudio/service/PlaylistService.java
- src/main/java/com/atstudio/atstudio/controller/PlaylistController.java
- src/main/java/com/atstudio/atstudio/entity/Playlist.java
- src/main/java/com/atstudio/atstudio/repository/PlaylistRepository.java
- src/main/java/com/atstudio/atstudio/ (전체 — 한국어 주석 Grep)
- src/test/java/ (전체 — 한국어 주석 Grep)

참고 (변경 금지 기준):
- src/main/java/com/atstudio/atstudio/service/AlbumService.java (Album 도메인, 변경 금지 기준)
- src/main/java/com/atstudio/atstudio/controller/AlbumController.java

---

[OUTPUT CONTRACT]

User-facing → deliverables/user/WI-20260306-ATS-002-summary.md:
- 스캔 결과 요약 (발견 건수, 정정 건수 또는 "0건 확인")
- 변경된 파일 목록 (없으면 "변경 없음")

Agent-facing → deliverables/agent/WI-20260306-ATS-002-evidence-pack.md:
- Grep 스캔 커맨드 및 결과 (변경 전)
- 파일별 수정 라인 및 before/after (발견 시)
- 빌드 결과 로그 (`gradlew.bat build -x test` PASS)

Handoff Packet → deliverables/agent/WI-20260306-ATS-002-handoff.md:
- 이 파일

---

[TRACEABILITY REQUIREMENTS]

Evidence pointers:
- Grep 커맨드: `grep -rn "앨범" src/ --include="*.java"` 결과 전문
- 수정 파일 존재 시: 파일 경로 + 라인 번호 + before/after

Tests:
- `gradlew.bat build -x test` 결과 (BUILD SUCCESSFUL)

Rollback:
- `git checkout -- src/` (변경 사항 없으면 불필요)
