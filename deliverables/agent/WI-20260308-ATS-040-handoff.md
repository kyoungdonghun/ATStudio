[WI HEADER]
WI ID: WI-20260308-ATS-040
REQ: REQ-20260308-ATS-012
Agent: se
Depends On: WI-20260308-ATS-037, WI-20260308-ATS-038, WI-20260308-ATS-039
Blocks: WI-20260308-ATS-043, WI-20260308-ATS-044, WI-20260308-ATS-045

---

[WI SUMMARY]
Why: 로그인한 구독자(USER 역할)가 사용하는 페이지 구현

Scope (in):
  - pages/subscriber/PlaylistListPage.tsx — 내 재생목록 카드 그리드 (최대 3개 제한 표현, 새 재생목록 버튼)
  - pages/subscriber/PlaylistDetailPage.tsx — 재생목록 상세 + 수록곡 TrackRow 테이블
  - pages/subscriber/PurchaseHistoryPage.tsx — 구매 내역 목록 (날짜/곡명/금액)
  - pages/subscriber/LicensePage.tsx — 보유 라이선스 목록
  - pages/subscriber/MyAccountPage.tsx — 내 정보 조회/수정 (닉네임, 비밀번호 변경)
  - pages/subscriber/SubscriptionManagePage.tsx — 현재 구독 상태, 업그레이드/다운그레이드/취소
  - 각 페이지 API 연동 + CSS Module

Scope (out):
  - 크리에이터/관리자 전용 페이지 (WI-041, WI-042 담당)
  - 백엔드 코드 수정 금지

DoD:
  - 6개 페이지 렌더링 정상 (Protected Route: USER 이상)
  - API 연동 (로딩/에러 처리 포함)
  - npm run lint 0 errors, npm run typecheck 0 errors, npm run build 성공

Constraints:
  - 재생목록 3개 제한: 3개 이상이면 "새 재생목록" 버튼 비노출
  - 공통 컴포넌트(AlbumCard, TrackRow, Button, Modal 등) 재사용
  - CSS Modules + tokens.css 변수만 사용

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] PlaylistListPage: 내 재생목록 카드 그리드, 재생목록 수 < 3이면 "새 재생목록" 버튼 노출, ≥ 3이면 비노출
- [ ] PlaylistListPage: GET /api/playlists (내 목록), POST /api/playlists (새 재생목록)
- [ ] PlaylistDetailPage: 재생목록명/수록곡수, TrackRow 테이블, 곡 삭제 기능
- [ ] PlaylistDetailPage: GET /api/playlists/{playlistId}/tracks, DELETE /api/playlists/{playlistId}/tracks/{trackId}
- [ ] PurchaseHistoryPage: 구매 내역 테이블 (곡명/날짜/금액/라이선스종류)
- [ ] PurchaseHistoryPage: GET /api/licenses (or /api/purchases — api-spec 확인)
- [ ] LicensePage: 보유 라이선스 목록 (곡명/라이선스종류/취득일)
- [ ] MyAccountPage: 닉네임 수정 (PUT /api/users/{userId}), 비밀번호 변경
- [ ] SubscriptionManagePage: 현재 플랜 표시, 업그레이드/다운그레이드/취소 버튼
- [ ] SubscriptionManagePage: GET /api/user-subscriptions/me, 변경 예약/취소 API 연동

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
- docs/ui/mockup/playlist.html              ← PlaylistListPage 디자인 (카드 그리드, 3개 제한)
- docs/design/api-spec.md (§5 Playlist, §7 License, §8 UserSubscription, §10 User)
- docs/ui/atstudio-front-list.md

기존 파일 (읽기):
- frontend/src/api/client.ts
- frontend/src/store/authStore.ts
- frontend/src/types/index.ts
- frontend/src/components/ (전체)
- frontend/src/router/index.tsx

수정 대상 (신규):
- frontend/src/pages/subscriber/PlaylistListPage.tsx + .module.css
- frontend/src/pages/subscriber/PlaylistDetailPage.tsx + .module.css
- frontend/src/pages/subscriber/PurchaseHistoryPage.tsx + .module.css
- frontend/src/pages/subscriber/LicensePage.tsx + .module.css
- frontend/src/pages/subscriber/MyAccountPage.tsx + .module.css
- frontend/src/pages/subscriber/SubscriptionManagePage.tsx + .module.css
- frontend/src/api/playlists.ts
- frontend/src/api/licenses.ts
- frontend/src/api/userSubscriptions.ts

---

[OUTPUT CONTRACT]
User-facing  -> deliverables/user/WI-20260308-ATS-040-summary.md
Agent-facing -> deliverables/agent/WI-20260308-ATS-040-evidence-pack.md
Handoff      -> deliverables/agent/WI-20260308-ATS-040-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence: 생성 파일 목록 + npm lint/typecheck/build 결과
Rollback: 신규 파일 삭제로 복구 가능
