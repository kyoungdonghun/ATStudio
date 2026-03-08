[WI HEADER]
WI ID: WI-20260308-ATS-044
REQ: REQ-20260308-ATS-012
Agent: qa
Depends On: WI-20260308-ATS-040, WI-20260308-ATS-041, WI-20260308-ATS-042
Blocks: -

---

[WI SUMMARY]
Why: ESLint 검사 — Phase 3~4 전체 페이지 구현 후 lint 오류 0 확인

Scope (in):
  - frontend/ 디렉토리 전체 대상으로 npm run lint 실행
  - 오류 발견 시 수정 후 재실행
  - 결과 보고 (0 errors 확인)

Scope (out):
  - TypeScript 타입 검사 (WI-043 담당)
  - 빌드 검사 (WI-045 담당)
  - 백엔드 코드 수정 금지

DoD:
  - npm run lint 0 errors
  - 오류 발견 시 수정 완료

---

[ACCEPTANCE CRITERIA]

Quality:
- [ ] npm run lint (eslint src/) 0 errors
- [ ] 수정한 파일 목록 보고 (수정 없으면 "no changes")

---

[INPUT POINTERS]

Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 2:
- .claude/skills/react-best-practices/AGENTS.md

기존 파일 (읽기):
- frontend/.eslintrc.cjs
- frontend/src/ (전체 — lint 대상)

---

[OUTPUT CONTRACT]
User-facing  -> deliverables/user/WI-20260308-ATS-044-summary.md
Agent-facing -> deliverables/agent/WI-20260308-ATS-044-evidence-pack.md
Handoff      -> deliverables/agent/WI-20260308-ATS-044-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence: npm run lint 실행 결과 (0 errors)
Rollback: 수정 파일 있으면 git diff 기록
