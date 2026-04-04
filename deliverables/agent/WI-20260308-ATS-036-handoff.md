[WI HEADER]
WI ID: WI-20260308-ATS-036
REQ: REQ-20260308-ATS-012
Agent: se
Depends On: WI-20260308-ATS-035
Blocks: WI-20260308-ATS-037, WI-20260308-ATS-038, WI-20260308-ATS-039, WI-20260308-ATS-040, WI-20260308-ATS-041, WI-20260308-ATS-042

---

[WI SUMMARY]
Why: 모든 페이지가 공유하는 공통 컴포넌트 구현. WI-037~042(페이지 WI)가 이 컴포넌트들을 재사용하므로 먼저 완료되어야 한다.

Scope (in):
  Layout 컴포넌트:
    - layouts/Header.tsx — 로고, 검색바, 탭 네비게이션(홈/음원/앨범/구독/공지), 로그인/구독 버튼 (authStore 연동)
    - layouts/PlayerBar.tsx — 고정 하단 바: 썸네일+곡정보+좋아요 / 재생 컨트롤+프로그레스바 / 대기열+구매 (playerStore 연동)
    - layouts/MainLayout.tsx — Header + <Outlet> + PlayerBar 래퍼 (padding-bottom: 72px)

  UI Atoms (components/ui/):
    - Button.tsx — variant: primary(gold) | ghost | outline | danger, size: sm|md|lg
    - Badge.tsx — variant: new | hot | accent, 인라인 텍스트 배지
    - Tag.tsx — 장르/분위기 필터 칩 (on/off 상태)
    - FilterChip.tsx — 필터 바 내 선택 칩 (on/off 상태)
    - Modal.tsx — 베이스 모달 래퍼 (backdrop + 포커스 트랩 + ESC 닫기)

  Track 컴포넌트:
    - components/track/TrackRow.tsx — 트랙 테이블 행 (번호, 썸네일, 제목/아티스트, 장르칩, BPM, 조성, 길이, 액션버튼)
      - hover 시 번호→재생버튼 교체
      - playing 상태: 제목 gold 강조
      - 액션: 좋아요(♥/♡), 재생목록 추가(+), 다운로드(↓), 구매 버튼

  Album 컴포넌트:
    - components/album/AlbumCard.tsx — 앨범 카드 (썸네일+hover play overlay, 제목, 장르·곡수 메타)

Scope (out):
  - 페이지 컴포넌트 구현 금지 (WI-037~042 담당)
  - API 실제 호출 금지 (playerStore/authStore 상태 읽기만)
  - 새로운 디자인 토큰 추가 금지 (tokens.css 기존 변수만 사용)
  - 백엔드 코드 수정 금지

DoD:
  - 위 컴포넌트 전체 구현 완료
  - npm run lint 0 errors
  - npm run typecheck 0 errors
  - npm run build 성공

Constraints/Forbidden:
  - 디자인 토큰: src/styles/tokens.css CSS 변수만 사용 (하드코딩 금지)
  - CSS Modules 사용 (파일명: ComponentName.module.css)
  - inline style 금지 (tokens.css 변수 var(--accent) 등으로 처리)
  - playerStore, authStore import 허용 (상태 읽기/쓰기)

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] layouts/Header.tsx — 탭 활성 상태 현재 경로 기준 자동 처리 (useLocation)
- [ ] layouts/Header.tsx — authStore.isAuthenticated() 기준 로그인/구독 vs 내계정 버튼 전환
- [ ] layouts/PlayerBar.tsx — playerStore currentTrack null 시 숨김 처리
- [ ] layouts/PlayerBar.tsx — play/pause/next/prev/shuffle/repeat 버튼 playerStore action 연결
- [ ] layouts/PlayerBar.tsx — 프로그레스바 렌더링 (실제 audio는 WI-037+ 담당, UI만)
- [ ] layouts/MainLayout.tsx — Header 58px fixed + PlayerBar 72px fixed + 본문 적절한 padding
- [ ] Button.tsx — 4가지 variant, 3가지 size, disabled/loading 상태
- [ ] Modal.tsx — ESC 키 닫기, backdrop 클릭 닫기, children 렌더링
- [ ] TrackRow.tsx — hover 시 재생 버튼 등장, playing prop 시 gold 강조
- [ ] AlbumCard.tsx — hover 시 play overlay 등장

Quality:
- [ ] npm run lint — 0 errors
- [ ] npm run typecheck — 0 errors
- [ ] npm run build — 성공

---

[INPUT POINTERS]

Tier 0 (Required — se):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 2 (React — enabled: true로 전환됨):
- .claude/skills/react-best-practices/AGENTS.md

REQ:
- deliverables/user/REQ-20260308-ATS-012.md

참조 (수정 금지):
- docs/ui/mockup/main.html          ← Header/PlayerBar/TrackRow/AlbumCard 디자인 상세
- docs/ui/mockup/track-list.html    ← TrackRow, FilterChip, Tag 패턴
- docs/ui/mockup/album-detail.html  ← AlbumCard, TrackRow in album context
- docs/ui/mockup/playlist.html      ← AlbumCard grid 패턴

기존 파일 (읽기 전용):
- frontend/src/styles/tokens.css       ← CSS 변수 (반드시 이것만 사용)
- frontend/src/store/playerStore.ts    ← PlayerBar 연동용
- frontend/src/store/authStore.ts      ← Header 연동용
- frontend/src/types/index.ts          ← Track, Album 타입

수정 대상 (새 파일 생성):
- frontend/src/layouts/Header.tsx + Header.module.css
- frontend/src/layouts/PlayerBar.tsx + PlayerBar.module.css
- frontend/src/layouts/MainLayout.tsx + MainLayout.module.css
- frontend/src/components/ui/Button.tsx + Button.module.css
- frontend/src/components/ui/Badge.tsx + Badge.module.css
- frontend/src/components/ui/Tag.tsx + Tag.module.css
- frontend/src/components/ui/FilterChip.tsx + FilterChip.module.css
- frontend/src/components/ui/Modal.tsx + Modal.module.css
- frontend/src/components/track/TrackRow.tsx + TrackRow.module.css
- frontend/src/components/album/AlbumCard.tsx + AlbumCard.module.css

---

[OUTPUT CONTRACT]

User-facing  -> deliverables/user/WI-20260308-ATS-036-summary.md
Agent-facing -> deliverables/agent/WI-20260308-ATS-036-evidence-pack.md
Handoff      -> deliverables/agent/WI-20260308-ATS-036-handoff.md

---

[TRACEABILITY REQUIREMENTS]
Evidence: 생성된 컴포넌트 파일 목록 + npm run lint/typecheck/build 실행 결과
Tests: npm run lint (0 errors), npm run typecheck (0 errors), npm run build (성공)
Rollback: 생성된 파일 삭제로 복구 가능 (기존 파일 미수정)
