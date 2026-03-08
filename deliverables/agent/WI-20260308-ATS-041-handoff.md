[WI HEADER]
WI ID: WI-20260308-ATS-041
REQ: REQ-20260308-ATS-012
Agent: se
Depends On: WI-20260308-ATS-037, WI-20260308-ATS-038, WI-20260308-ATS-039
Blocks: WI-20260308-ATS-043, WI-20260308-ATS-044, WI-20260308-ATS-045

---

[WI SUMMARY]
Why: 크리에이터(CREATOR 역할) 전용 페이지 구현 — 음원 업로드/관리, 앨범 관리, 수익 통계

Scope (in):
  - pages/creator/TrackUploadPage.tsx — 음원 파일 + 썸네일 업로드, 제목/장르/BPM/조성/설명 입력
  - pages/creator/TrackManagePage.tsx — 내 음원 목록 (테이블), 수정/삭제/활성화 토글
  - pages/creator/TrackEditPage.tsx — 음원 수정 폼 (TrackUploadPage와 유사)
  - pages/creator/AlbumManagePage.tsx — 내 앨범 목록 + 앨범 생성/수정/삭제
  - 각 페이지 API 연동 + CSS Module

Scope (out):
  - 구독자/관리자 페이지 (WI-040, WI-042 담당)
  - 백엔드 코드 수정 금지

DoD:
  - 4개 페이지 렌더링 정상 (Protected Route: CREATOR 이상)
  - API 연동 (로딩/에러 처리)
  - npm run lint 0 errors, npm run typecheck 0 errors, npm run build 성공

Constraints:
  - 파일 업로드: multipart/form-data, @RequestPart 패턴 (audioFile + thumbnail 별도)
  - CSS Modules + tokens.css 변수만 사용

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] TrackUploadPage: 오디오 파일 + 썸네일 파일 선택, 메타 입력, POST /api/tracks (multipart)
- [ ] TrackManagePage: 내 음원 테이블, GET /api/tracks?creatorId=me, 수정/삭제 링크
- [ ] TrackEditPage: 기존 데이터 로드, PUT /api/tracks/{trackId} (multipart)
- [ ] AlbumManagePage: 내 앨범 카드 목록, 앨범 생성(POST /api/albums), 수정/삭제
- [ ] 삭제 시 ConfirmModal 사용 (Modal 컴포넌트 활용)

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
- docs/design/api-spec.md (§1 Track CRUD, §3 Album CRUD)
- docs/check/atstudio-front-list.md

기존 파일 (읽기):
- frontend/src/api/client.ts
- frontend/src/api/tracks.ts
- frontend/src/api/albums.ts
- frontend/src/store/authStore.ts
- frontend/src/types/index.ts
- frontend/src/components/ (전체)

수정 대상 (신규):
- frontend/src/pages/creator/TrackUploadPage.tsx + .module.css
- frontend/src/pages/creator/TrackManagePage.tsx + .module.css
- frontend/src/pages/creator/TrackEditPage.tsx + .module.css
- frontend/src/pages/creator/AlbumManagePage.tsx + .module.css

---

[OUTPUT CONTRACT]
User-facing  -> deliverables/user/WI-20260308-ATS-041-summary.md
Agent-facing -> deliverables/agent/WI-20260308-ATS-041-evidence-pack.md
Handoff      -> deliverables/agent/WI-20260308-ATS-041-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence: 생성 파일 목록 + npm lint/typecheck/build 결과
Rollback: 신규 파일 삭제로 복구 가능
