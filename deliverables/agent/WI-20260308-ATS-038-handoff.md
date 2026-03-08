[WI HEADER]
WI ID: WI-20260308-ATS-038
REQ: REQ-20260308-ATS-012
Agent: se
Depends On: WI-20260308-ATS-036
Blocks: WI-20260308-ATS-043, WI-20260308-ATS-044, WI-20260308-ATS-045

---

[WI SUMMARY]
Why: 공개 페이지 B 구현 — 앨범 상세, 음원 상세, 구독 플랜, 공지 목록/상세

Scope (in):
  - pages/public/AlbumDetailPage.tsx — 바이닐+커버, 앨범정보, 수록곡 TrackRow 테이블
  - pages/public/TrackDetailPage.tsx — 음원 상세, 라이선스 안내, 구매/다운로드 버튼
  - pages/public/SubscriptionPage.tsx — 3플랜 비교 카드 (Starter/Pro/Business), 기능 비교표, FAQ
  - pages/public/NoticeListPage.tsx — 공지 목록 테이블
  - pages/public/NoticeDetailPage.tsx — 공지 단건 상세
  - 각 페이지 API 연동 + CSS Module

Scope (out):
  - 홈, 음원목록, 앨범목록 (WI-037 담당)
  - 인증 필요 기능 (구독 신청은 버튼 렌더링만, 클릭 시 로그인 유도)
  - 백엔드 코드 수정 금지

DoD:
  - 5개 페이지 렌더링 정상
  - API 연동 (로딩/에러 상태 처리 포함)
  - npm run lint 0 errors, npm run typecheck 0 errors, npm run build 성공

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] AlbumDetailPage: 바이닐 CSS 애니메이션, 앨범커버, 앨범정보(제목/아티스트/메타/태그/설명), 전체재생/좋아요 버튼
- [ ] AlbumDetailPage: GET /api/albums/{albumId} + GET /api/albums/{albumId}/tracks 연동
- [ ] TrackDetailPage: 음원 정보, 라이선스 종류(개인/상업) 안내, 구매 버튼
- [ ] TrackDetailPage: GET /api/tracks/{trackId} 연동
- [ ] SubscriptionPage: Starter/Pro(popular)/Business 3열 카드, 월간/연간 토글, 기능 비교표, FAQ 아코디언
- [ ] SubscriptionPage: GET /api/subscriptions/plans 연동
- [ ] NoticeListPage: 공지 목록 테이블 + 페이지네이션
- [ ] NoticeDetailPage: 공지 단건 상세 뷰
- [ ] GET /api/notices 연동

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
- docs/check/mockup/album-detail.html       ← AlbumDetailPage 디자인 (바이닐 CSS)
- docs/check/mockup/subscription.html       ← SubscriptionPage 디자인 (3플랜 카드, FAQ)
- docs/design/api-spec.md (§1 Track, §3 Album, §6 Notice, §9 Subscription)
- docs/check/atstudio-front-list.md         ← URL 경로

기존 파일 (읽기):
- frontend/src/api/client.ts
- frontend/src/types/index.ts
- frontend/src/components/track/TrackRow.tsx
- frontend/src/components/album/AlbumCard.tsx
- frontend/src/components/ui/ (전체)
- frontend/src/layouts/MainLayout.tsx

수정 대상 (신규):
- frontend/src/pages/public/AlbumDetailPage.tsx + AlbumDetailPage.module.css
- frontend/src/pages/public/TrackDetailPage.tsx + TrackDetailPage.module.css
- frontend/src/pages/public/SubscriptionPage.tsx + SubscriptionPage.module.css
- frontend/src/pages/public/NoticeListPage.tsx + NoticeListPage.module.css
- frontend/src/pages/public/NoticeDetailPage.tsx + NoticeDetailPage.module.css
- frontend/src/api/notices.ts
- frontend/src/api/subscriptions.ts (플랜 조회)

---

[OUTPUT CONTRACT]
User-facing  -> deliverables/user/WI-20260308-ATS-038-summary.md
Agent-facing -> deliverables/agent/WI-20260308-ATS-038-evidence-pack.md
Handoff      -> deliverables/agent/WI-20260308-ATS-038-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence: 생성 파일 목록 + npm lint/typecheck/build 결과
Rollback: 신규 파일 삭제로 복구 가능
