[WI HEADER]
WI ID: WI-20260808-ATS-007
REQ: REQ-20260808-ATS-002
Agent: qa-integ
Depends On: WI-20260808-ATS-006
Blocks: -

[WI SUMMARY]
Why: 작성된 세 SR이 실제 코드·문서·외부 근거와 일치하고 문서 집계·링크가 정상인지 독립 검증한다.
Scope (in/out): SR-96~98, 두 인덱스, 선행 Evidence Packs와 관련 코드를 읽고 검증 보고만 작성한다. SR·코드·DB는 수정하지 않는다.
DoD: 각 SR의 핵심 주장과 수용 기준, 상태·개수·링크·Markdown·diff가 검증되고 불일치가 없거나 명확히 보고된다.
Constraints/Forbidden: 문제를 발견해도 직접 수정하지 않는다. BLOCKER/MAJOR/MINOR로 분류하고 정확한 파일·라인을 제시한다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] SR-96의 현재 권한 변경 판정과 보호 요구가 코드와 일치한다.
- [ ] SR-97의 플랜·상태·만료일 판정과 행렬이 코드·도메인 의미와 일치한다.
- [ ] SR-98의 업로드 리사이즈·표시 크롭 판정과 외부 근거가 정확하다.
- [ ] SR 파일 수, 인덱스 행, 상태 집계, 전체 문서 수가 일치한다.
Performance:
- [ ] 해당 없음(문서 검증 WI).
Quality:
- [ ] validate-docs와 `git diff --check` 결과를 기록한다.
- [ ] 링크와 외부 출처가 주장 근처에 배치되었는지 확인한다.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred from Task):
- docs/policies/quality-gates.md
- docs/standards/evidence-pack-standard.md
- docs/policies/security-policy.md

Tier 2 (Tech Stack and Task Context):
- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/AGENTS.md
- docs/design/api-spec.md
- docs/design/usecase/user-subscription.md
- docs/SR/SR-14.md
- docs/SR/SR-68.md
- docs/SR/SR-96.md
- docs/SR/SR-97.md
- docs/SR/SR-98.md
- docs/SR/index.md
- docs/index.md

REQ/Context Docs:
- deliverables/user/REQ-20260808-ATS-002.md
- deliverables/agent/WI-20260808-ATS-003-evidence-pack.md
- deliverables/agent/WI-20260808-ATS-004-evidence-pack.md
- deliverables/agent/WI-20260808-ATS-005-evidence-pack.md
- deliverables/agent/WI-20260808-ATS-006-evidence-pack.md

Repro/Logs:
- `python .agents/skills/validate-docs/scripts/validate_docs.py`
- `git diff --check`
- focused `rg` and PowerShell count checks documented in the Evidence Pack

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260808-ATS-007-summary.md :
- PASS/FAIL 판정, 문제 목록, 최종 문서 상태
Agent-facing -> deliverables/agent/WI-20260808-ATS-007-evidence-pack.md :
- Evidence pointers, 명령·결과, 발견 사항, 후속 WI 여부
Handoff Packet -> deliverables/agent/WI-20260808-ATS-007-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: 명령, 종료 코드, 핵심 결과 포함
Rollback (if needed): 검증 산출물만 제거하는 방법 기록
