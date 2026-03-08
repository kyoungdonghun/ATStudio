[WI HEADER]
WI ID: WI-20260308-ATS-037
REQ: REQ-20260308-ATS-012
Agent: se
Depends On: WI-20260308-ATS-036
Blocks: WI-20260308-ATS-043, WI-20260308-ATS-044, WI-20260308-ATS-045

---

[WI SUMMARY]
Why: 비로그인 사용자가 접근하는 공개 페이지 A 구현 — 홈, 음원 목록, 앨범 목록

Scope (in):
  - pages/public/HomePage.tsx — Hero섹션, 신규앨범 캐러셀, 인기앨범 그리드, 장르태그, Footer
  - pages/public/TrackListPage.tsx — 필터바(장르/분위기/BPM), 트랙 테이블, 정렬, 페이지네이션
  - pages/public/AlbumListPage.tsx — 앨범 카드 그리드, 장르 필터, 페이지네이션
  - 각 페이지의 API 연동 (Axios client 사용)
  - 각 페이지 CSS Module

Scope (out):
  - 앨범 상세, 음원 상세, 구독, 공지 페이지 (WI-038 담당)
  - 로그인 필요 기능 (찜, 재생목록 추가는 버튼 렌더링만, 클릭 시 로그인 유도)
  - 백엔드 코드 수정 금지

DoD:
  - 3개 페이지 렌더링 정상
  - API 연동 (로딩/에러 상태 처리 포함)
  - npm run lint 0 errors, npm run typecheck 0 errors, npm run build 성공

Constraints:
  - 공통 컴포넌트(AlbumCard, TrackRow, Button, Tag, FilterChip) 반드시 재사용
  - 디자인 토큰 CSS 변수만 사용 (하드코딩 금지)
  - CSS Modules 사용

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] HomePage: Hero (badge/title/desc/CTA버튼/앨범스택), 신규앨범 캐러셀, 인기앨범 6열 그리드, 장르태그 필터
- [ ] HomePage: GET /api/albums (최신 7개 캐러셀), GET /api/albums?sort=popular (6개 그리드)
- [ ] TrackListPage: 장르/분위기/BPM 필터 칩, 정렬 드롭다운, TrackRow 테이블, 페이지네이션
- [ ] TrackListPage: GET /api/tracks?page&size&genre&bpm_min&bpm_max 연동
- [ ] AlbumListPage: AlbumCard 6열 그리드, 장르 필터, 페이지네이션
- [ ] AlbumListPage: GET /api/albums?page&size 연동
- [ ] 로딩 중 스켈레톤 또는 로딩 인디케이터
- [ ] API 에러 시 에러 메시지 표시

Quality:
- [ ] npm run lint 0 errors
- [ ] npm run typecheck 0 errors
- [ ] npm run build 성공

---

[INPUT POINTERS]

Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 2:
- .claude/skills/react-best-practices/AGENTS.md

REQ:
- deliverables/user/REQ-20260308-ATS-012.md

참조 (수정 금지):
- docs/check/mockup/main.html               ← HomePage 디자인 상세
- docs/check/mockup/track-list.html         ← TrackListPage 디자인 상세
- docs/design/api-spec.md (§1 Track, §2 Tag, §3 Album)  ← API 엔드포인트/파라미터
- docs/check/atstudio-front-list.md         ← 화면별 URL 경로 확인

기존 파일 (읽기):
- frontend/src/api/client.ts
- frontend/src/types/index.ts
- frontend/src/components/album/AlbumCard.tsx
- frontend/src/components/track/TrackRow.tsx
- frontend/src/components/ui/ (Button, Badge, Tag, FilterChip)
- frontend/src/layouts/MainLayout.tsx

수정 대상 (신규):
- frontend/src/pages/public/HomePage.tsx + HomePage.module.css
- frontend/src/pages/public/TrackListPage.tsx + TrackListPage.module.css
- frontend/src/pages/public/AlbumListPage.tsx + AlbumListPage.module.css
- frontend/src/api/tracks.ts (Track API 모듈)
- frontend/src/api/albums.ts (Album API 모듈)

---

[OUTPUT CONTRACT]
User-facing  -> deliverables/user/WI-20260308-ATS-037-summary.md
Agent-facing -> deliverables/agent/WI-20260308-ATS-037-evidence-pack.md
Handoff      -> deliverables/agent/WI-20260308-ATS-037-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence: 생성 파일 목록 + npm lint/typecheck/build 결과
Rollback: 신규 파일 삭제로 복구 가능
