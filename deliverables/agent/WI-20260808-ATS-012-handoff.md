[WI HEADER]
WI ID: WI-20260808-ATS-012
REQ: REQ-20260808-ATS-003
Agent: qa-integ
Depends On: WI-20260808-ATS-011
Blocks: -

[WI SUMMARY]
Why: SR-99~101의 런타임·코드·문서 근거와 인덱스 정합성을 독립 검증한다.
Scope (in/out): 신규 SR·인덱스·선행 Evidence Packs와 관련 코드/API를 읽고 검증 산출물만 생성한다. SR, 인덱스, 코드, DB는 수정하지 않는다.
DoD: 핵심 주장, 수치, 영향 범위, 제안 경계, 파일·상태·링크·Markdown 검증이 PASS하거나 문제 등급과 포인터로 보고된다.
Constraints/Forbidden: 문제 발견 시 직접 수정하지 않는다. BLOCKER/MAJOR/MINOR로 분류한다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] SR-99의 세 곡 수치·원인·영향 범위가 API/브라우저/코드와 일치한다.
- [ ] SR-100의 태그 개수·기능 지원·우선순위가 API/코드/용어 계약과 일치한다.
- [ ] SR-101의 stalled 조건·해제·순간 재현과 waveform null 범위가 코드와 일치한다.
- [ ] SR 파일 100개, 인덱스 100행, 상태와 전체 문서 202개가 일치한다.
Performance:
- [ ] 해당 없음(문서 검증 WI).
Quality:
- [ ] validate-docs와 `git diff --check` 결과를 기록한다.
- [ ] 새 문서 링크·후행공백·코드펜스·EOF를 검사한다.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1/2 (Policies and Context):
- docs/policies/quality-gates.md
- docs/standards/evidence-pack-standard.md
- docs/standards/frontend-standards.md
- docs/design/api-spec.md
- docs/design/usecase/sound-track.md
- docs/design/usecase/sound-tag.md
- docs/SR/SR-99.md
- docs/SR/SR-100.md
- docs/SR/SR-101.md
- docs/SR/index.md
- docs/index.md

REQ/Context Docs:
- deliverables/user/REQ-20260808-ATS-003.md
- deliverables/agent/WI-20260808-ATS-008-evidence-pack.md
- deliverables/agent/WI-20260808-ATS-009-evidence-pack.md
- deliverables/agent/WI-20260808-ATS-010-evidence-pack.md
- deliverables/agent/WI-20260808-ATS-011-evidence-pack.md

Repro/Logs:
- `python .agents/skills/validate-docs/scripts/validate_docs.py`
- `git diff --check`
- focused API/Range/browser/code/count commands in evidence packs

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260808-ATS-012-summary.md : PASS/FAIL, finding counts, final state
Agent-facing -> deliverables/agent/WI-20260808-ATS-012-evidence-pack.md : commands, outputs, evidence pointers, follow-ups
Handoff Packet -> deliverables/agent/WI-20260808-ATS-012-handoff.md : This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: command, exit code, key result 포함
Rollback: validation-only outputs removal method 기록
