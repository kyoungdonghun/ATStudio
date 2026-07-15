[WI HEADER]
WI ID: WI-20260716-ATS-003
REQ: REQ-20260716-ATS-001
Agent: qa-integ
Depends On: WI-20260716-ATS-002
Blocks: stable demo checkpoint refresh

[WI SUMMARY]
Why: 표시 브랜드 변경이 빠진 사용자 접점 없이 적용되었고 내부 호환성 식별자는 보존되었는지 독립 검증해야 한다.
Scope (in/out): In: WI-002 diff 리뷰, 사용자 노출 문자열 재검색, 프론트 typecheck/lint/test/build, 백엔드 관련 테스트 또는 컴파일, 공개 화면 검증 준비, evidence. Out: 새로운 기능, 디자인 변경, DB 데이터 수정, URL 또는 내부 식별자 리네이밍.
DoD: 잔존 문자열이 사용자 노출인지 내부 식별자인지 분류되고, 필수 품질 명령이 통과하며, 발견된 범위 내 결함은 명확히 보고하거나 작은 회귀 수정으로 제한한다.
Constraints/Forbidden: 기존 DB를 수정하지 않는다. 대규모 일괄 치환을 하지 않는다. 다른 변경이나 런타임 로그를 되돌리거나 stage/commit하지 않는다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] 활성 화면과 사용자 메시지의 브랜드가 `AT.M`으로 통일되었다.
- [ ] URL·도메인·package·내부 header·암호화 associated data가 보존되었다.
- [ ] seed 원본과 기존 DB 데이터의 적용 차이가 evidence에 기록되었다.
Quality:
- [ ] frontend typecheck가 통과한다.
- [ ] frontend ESLint가 통과한다.
- [ ] frontend Vitest가 통과한다.
- [ ] frontend build가 통과한다.
- [ ] backend 관련 테스트 또는 compileJava가 통과한다.
- [ ] `git diff --check`가 통과한다.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1 (Policies):
- docs/policies/quality-gates.md
Tier 2 (Tech Stack / Context):
- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md
- docs/design/api-spec.md
REQ/Context Docs:
- deliverables/user/REQ-20260716-ATS-001.md
- deliverables/agent/WI-20260716-ATS-002-handoff.md
- deliverables/agent/WI-20260716-ATS-002-evidence-pack.md
Files:
- WI-002 changed files
- frontend/src
- frontend/index.html
- src/main/java/com/atstudio/atstudio/service
- src/main/resources/seed.sql
Repro/Logs:
- `rg` searches for exact `ATStudio` and case-insensitive display variants within active runtime source.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-003-summary.md
Agent-facing -> deliverables/agent/WI-20260716-ATS-003-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260716-ATS-003-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required for every residual `ATStudio` classification and quality command.
Tests: Record exact commands, exit status, and any warning.
Rollback: Code-only revert; no data rollback should be needed.
