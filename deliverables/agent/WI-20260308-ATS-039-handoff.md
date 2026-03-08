[WI HEADER]
WI ID: WI-20260308-ATS-039
REQ: REQ-20260308-ATS-012
Agent: se
Depends On: WI-20260308-ATS-036
Blocks: WI-20260308-ATS-043, WI-20260308-ATS-044, WI-20260308-ATS-045

---

[WI SUMMARY]
Why: 인증 페이지 구현 — 로그인, 회원가입, 이메일 인증, 비밀번호 재설정

Scope (in):
  - pages/auth/LoginPage.tsx — 이메일/비밀번호 입력, 로그인 버튼, authStore 연동
  - pages/auth/SignupPage.tsx — 역할 선택(USER/CREATOR), 이메일/비밀번호/이름 입력
  - pages/auth/EmailVerifyPage.tsx — 인증 코드 입력 또는 링크 안내
  - pages/auth/PasswordResetPage.tsx — 이메일 입력 → 재설정 링크 발송
  - 각 페이지 API 연동 + CSS Module
  - 로그인 성공 시 authStore.login() 호출 → 이전 페이지 또는 홈으로 리다이렉트

Scope (out):
  - 소셜 로그인 (api-spec에 없음)
  - 백엔드 코드 수정 금지

DoD:
  - 4개 페이지 렌더링 정상
  - 로그인 성공 시 authStore 업데이트 + 리다이렉트
  - npm run lint 0 errors, npm run typecheck 0 errors, npm run build 성공

---

[ACCEPTANCE CRITERIA]

Functional:
- [ ] LoginPage: 이메일/비밀번호 폼, 유효성 검사, POST /api/auth/login 연동
- [ ] LoginPage: 성공 시 authStore.login(token, user) 호출, 홈으로 이동
- [ ] LoginPage: 실패 시 에러 메시지 표시 (401: 이메일/비밀번호 불일치)
- [ ] SignupPage: 역할 선택(일반/크리에이터), 입력 폼, POST /api/auth/register 연동
- [ ] SignupPage: 성공 시 이메일 인증 안내 페이지 이동
- [ ] EmailVerifyPage: POST /api/auth/verify-email 연동
- [ ] PasswordResetPage: POST /api/auth/password-reset-request 연동
- [ ] 모든 폼에 HTML5 + 커스텀 유효성 검사 (빈 값, 이메일 형식, 비밀번호 8자 이상)

Quality:
- [ ] npm run lint 0 errors
- [ ] npm run typecheck 0 errors
- [ ] npm run build 성공

---

[INPUT POINTERS]

Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (인증 관련):
- docs/policies/security-policy.md

Tier 2:
- .claude/skills/react-best-practices/AGENTS.md

REQ:
- deliverables/user/REQ-20260308-ATS-012.md

참조 (수정 금지):
- docs/design/api-spec.md (§4 Auth)      ← 인증 API 엔드포인트/요청·응답 구조
- docs/check/atstudio-front-list.md       ← 인증 화면 URL 경로

기존 파일 (읽기):
- frontend/src/api/client.ts
- frontend/src/store/authStore.ts
- frontend/src/types/index.ts
- frontend/src/components/ui/Button.tsx
- frontend/src/layouts/MainLayout.tsx
- frontend/src/router/index.tsx            ← 인증 라우트 경로 확인

수정 대상 (신규):
- frontend/src/pages/auth/LoginPage.tsx + LoginPage.module.css
- frontend/src/pages/auth/SignupPage.tsx + SignupPage.module.css
- frontend/src/pages/auth/EmailVerifyPage.tsx + EmailVerifyPage.module.css
- frontend/src/pages/auth/PasswordResetPage.tsx + PasswordResetPage.module.css
- frontend/src/api/auth.ts (인증 API 모듈)

---

[OUTPUT CONTRACT]
User-facing  -> deliverables/user/WI-20260308-ATS-039-summary.md
Agent-facing -> deliverables/agent/WI-20260308-ATS-039-evidence-pack.md
Handoff      -> deliverables/agent/WI-20260308-ATS-039-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence: 생성 파일 목록 + npm lint/typecheck/build 결과
Rollback: 신규 파일 삭제로 복구 가능
