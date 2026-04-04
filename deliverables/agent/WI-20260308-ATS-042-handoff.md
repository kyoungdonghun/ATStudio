[WI HEADER]
WI ID: WI-20260308-ATS-042
REQ: REQ-20260308-ATS-012
Agent: se
Depends On: WI-20260308-ATS-037, WI-20260308-ATS-038, WI-20260308-ATS-039
Blocks: WI-20260308-ATS-043, WI-20260308-ATS-044, WI-20260308-ATS-045

---

[WI SUMMARY]
Why: 관리자(ADMIN 역할) 전용 페이지 구현 — 유저 관리, 기업 인증 심사, 태그 관리, 공지 관리

Scope (in):
  - pages/admin/UserManagePage.tsx — 전체 유저 목록, 검색, 역할 변경, 계정 비활성화
  - pages/admin/CompanyCertPage.tsx — 기업 인증 신청 목록, 심사 처리(APPROVED/REJECTED)
  - pages/admin/TagManagePage.tsx — 태그 목록, 생성/수정/삭제
  - pages/admin/AdminNoticePage.tsx — 공지 생성/수정/삭제 (관리자용)
  - pages/admin/AdminDashboardPage.tsx — 기본 통계 대시보드 (유저수/음원수/구독자수)
  - 각 페이지 API 연동 + CSS Module

Scope (out):
  - 구독자/크리에이터 페이지 (WI-040, WI-041 담당)
  - 백엔드 코드 수정 금지

DoD:
  - 5개 페이지 렌더링 정상 (Protected Route: ADMIN 전용)
  - API 연동 (로딩/에러 처리)
  - npm run lint 0 errors, npm run typecheck 0 errors, npm run build 성공

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] AdminDashboardPage: 통계 카드 (총 유저/음원/구독자), 최근 가입 유저 리스트
- [ ] UserManagePage: 유저 목록 테이블, 검색(이메일/닉네임), GET /api/users (admin), PATCH 역할 변경
- [ ] CompanyCertPage: 기업 인증 신청 목록, 심사 상태 필터, PATCH /api/company-certs/{id}/status 승인/거절
- [ ] TagManagePage: 태그 목록 테이블, POST /api/tags (생성), PUT /api/tags/{tagId} (수정), DELETE /api/tags/{tagId} (삭제)
- [ ] AdminNoticePage: 공지 목록 + 생성 폼, POST /api/notices, PUT /api/notices/{id}, DELETE /api/notices/{id}
- [ ] 삭제/역할변경 시 ConfirmModal 사용

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
- docs/design/api-spec.md (§2 Tag, §6 Notice, §10 User admin, §12 CompanyCert, §15 Stats)
- docs/ui/atstudio-front-list.md

기존 파일 (읽기):
- frontend/src/api/client.ts
- frontend/src/store/authStore.ts
- frontend/src/types/index.ts
- frontend/src/components/ (전체)
- frontend/src/router/index.tsx

수정 대상 (신규):
- frontend/src/pages/admin/AdminDashboardPage.tsx + .module.css
- frontend/src/pages/admin/UserManagePage.tsx + .module.css
- frontend/src/pages/admin/CompanyCertPage.tsx + .module.css
- frontend/src/pages/admin/TagManagePage.tsx + .module.css
- frontend/src/pages/admin/AdminNoticePage.tsx + .module.css
- frontend/src/api/admin.ts
- frontend/src/api/tags.ts (이미 존재할 수 있음 — 덮어쓰지 말고 함수 추가)

---

[OUTPUT CONTRACT]
User-facing  -> deliverables/user/WI-20260308-ATS-042-summary.md
Agent-facing -> deliverables/agent/WI-20260308-ATS-042-evidence-pack.md
Handoff      -> deliverables/agent/WI-20260308-ATS-042-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence: 생성 파일 목록 + npm lint/typecheck/build 결과
Rollback: 신규 파일 삭제로 복구 가능
