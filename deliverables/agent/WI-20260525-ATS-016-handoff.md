[WI HEADER]
WI ID: WI-20260525-ATS-016
REQ: REQ-20260525-ATS-006
Agent: qa-fe/qa/docops
Depends On: WI-20260525-ATS-015
Blocks: -

[WI SUMMARY]
Why: 결제 운영 UI는 금전/권한 mutation을 노출하므로 type/build/test/docs 검증과 산출물 정리가 필요하다.
Scope (in/out): In은 frontend typecheck/lint/build, backend 결제 운영 targeted tests, docs validation, diff check, summary/evidence 작성이다. Out은 추가 기능 구현이다.
DoD: 검증 명령이 통과하고 변경 요약/후속 범위가 산출물에 기록된다.
Constraints/Forbidden: 실패한 검증을 통과한 것처럼 기록하지 않는다. 미실행 검증은 명시한다.

[ACCEPTANCE CRITERIA]
Quality:
- [ ] `npm run typecheck` 통과.
- [ ] `npm run lint` 통과.
- [ ] `npm run build` 통과.
- [ ] backend 결제 운영 targeted tests 통과.
- [ ] `validate-docs` 통과.
- [ ] `git diff --check` 통과.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1 (Policies - Inferred):
- docs/policies/quality-gates.md

REQ/Context Docs:
- deliverables/user/REQ-20260525-ATS-006.md
- deliverables/agent/WI-20260525-ATS-014-handoff.md
- deliverables/agent/WI-20260525-ATS-015-handoff.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260525-ATS-016-summary.md :
- 최종 검증 결과, 변경 요약, 후속 권장 작업
Agent-facing -> deliverables/agent/WI-20260525-ATS-016-evidence-pack.md :
- 검증 명령과 결과, diff summary, rollback
Handoff Packet -> deliverables/agent/WI-20260525-ATS-016-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers: command outputs, changed files, docs validation.
Tests: frontend checks, backend targeted tests, docs validation.
Rollback: revert the REQ-20260525-ATS-006 related patch/commit.
