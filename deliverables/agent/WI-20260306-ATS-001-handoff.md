[WI HEADER]
WI ID: WI-20260306-ATS-001
REQ: REQ-20260306-ATS-004
Agent: docops
Depends On: -
Blocks: WI-20260306-ATS-003

---

[WI SUMMARY]

Why:
- Playlist 도메인의 한국어 UI 레이블이 과거에 "앨범"으로 오기입되어 6개 문서에 잔존함
- 신규 Album 도메인(어드민 큐레이팅) 추가로 "앨범"이 두 개념을 지칭하는 명칭 충돌 발생
- Playlist 관련 "앨범" 오기입을 "재생목록"으로 정정하여 충돌 해소

Scope:
- In:
  - `docs/ui/atstudio-front-list.md` — 💿 플레이리스트 섹션 내 화면명 "앨범 목록/생성/수정" → "재생목록 목록/생성/수정", 섹션 설명 내 "앨범" 오기입
  - `docs/design/api-spec.md` — Section 3 (Playlist) 내 "앨범" 오기입 표기
  - `docs/design/usecase/sound-playlist.md` — 문서 내 "앨범" 오기입 표기 전체
  - `docs/design/usecase/index.md` — Playlist 관련 행 설명 내 "앨범" 오기입
  - `docs/design/db-schema.md` — playlists 테이블 설명 내 "앨범" 오기입
  - `docs/standards/glossary.md` — Playlist 용어 설명 내 "앨범" 오기입
- Out:
  - Album 도메인 관련 "앨범" 표기 (Section 15, 어드민 큐레이팅) — **절대 변경 금지**
  - API URL (`/api/playlists`) — 변경 금지
  - 영문 기술 식별자 (`Playlist`, `playlist`, `PLAYLIST` 등) — 변경 금지
  - 백엔드 Java 코드 — WI-20260306-ATS-002에서 별도 처리

DoD:
1. 6개 대상 문서에서 Playlist를 지칭하는 "앨범" 표기가 "재생목록"으로 정정됨
2. Album 도메인 "앨범" 표기는 변경되지 않음 (의미 구별 검증 완료)
3. API URL, 영문 식별자 변경 없음
4. evidence-pack에 변경 전/후 diff 포인터 기록됨

Constraints/Forbidden:
- **⚠️ CRITICAL**: "앨범" 표기 변경 전 반드시 문맥 판별
  - Playlist 맥락 (구독자, `/api/playlists`, 개인 재생목록, 최대 3개) → "재생목록"으로 변경
  - Album 맥락 (어드민, `/api/albums`, 큐레이팅, Section 15) → "앨범" 유지
- 영문 식별자(`Playlist`, `playlist`) 변경 금지
- API URL 변경 금지

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] `docs/ui/atstudio-front-list.md`: 💿 섹션 화면명 "재생목록 목록(이미지)", "재생목록 목록(리스트)", "재생목록 생성", "재생목록 수정"으로 정정
- [ ] `docs/design/api-spec.md`: Section 3 제목·설명에서 Playlist 지칭 "앨범" 표기 정정
- [ ] `docs/design/usecase/sound-playlist.md`: 문서 전체 Playlist 지칭 "앨범" 표기 정정
- [ ] `docs/design/usecase/index.md`: Playlist 행 설명 정정
- [ ] `docs/design/db-schema.md`: playlists 테이블 설명 정정
- [ ] `docs/standards/glossary.md`: Playlist 용어 정정 (존재하는 경우)
- [ ] Album 도메인(Section 15, `/api/albums`) 관련 "앨범" 표기 보존 확인

Quality:
- [ ] 변경 후 Grep으로 잔존 오기입 없음 확인: Playlist 맥락에서 "앨범" 0건
- [ ] evidence-pack에 파일별 변경 내역(before/after) 기록

---

[INPUT POINTERS]

Tier 0 (Constitution):
- docs/standards/core-principles.md

Tier 0 (Documentation Standards):
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

REQ/Context Docs:
- deliverables/user/REQ-20260306-ATS-004.md

Files (대상):
- docs/ui/atstudio-front-list.md
- docs/design/api-spec.md (Section 3 집중)
- docs/design/usecase/sound-playlist.md
- docs/design/usecase/index.md
- docs/design/db-schema.md
- docs/standards/glossary.md

참고 (변경 금지 기준):
- docs/design/api-spec.md (Section 15 — Album 도메인, 변경 금지 기준 확인용)
- docs/design/usecase/sound-album.md (Album 도메인, 변경 금지 기준 확인용)

---

[OUTPUT CONTRACT]

User-facing → deliverables/user/WI-20260306-ATS-001-summary.md:
- 정정된 파일 목록 및 변경 건수 요약
- 의미 구별 판별 결과 (Playlist vs Album 각 건수)

Agent-facing → deliverables/agent/WI-20260306-ATS-001-evidence-pack.md:
- 파일별 변경 전/후 표기 목록 (before → after)
- Grep 결과: 잔존 "앨범" (Playlist 맥락) 0건 확인 커맨드 및 결과
- Album 도메인 "앨범" 보존 확인 목록

Handoff Packet → deliverables/agent/WI-20260306-ATS-001-handoff.md:
- 이 파일

---

[TRACEABILITY REQUIREMENTS]

Evidence pointers:
- 파일별 수정 라인 번호 및 before/after 기록
- Grep 명령어: `grep -n "앨범" docs/ui/atstudio-front-list.md docs/design/api-spec.md docs/design/usecase/sound-playlist.md docs/design/usecase/index.md docs/design/db-schema.md docs/standards/glossary.md` 결과 (변경 전/후 각 1회)

Tests:
- N/A (문서 변경 작업)

Rollback:
- git diff HEAD로 변경 추적 가능. 롤백 시: `git checkout -- <file>` per file
