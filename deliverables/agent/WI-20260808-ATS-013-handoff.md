[WI HEADER]
WI ID: WI-20260808-ATS-013
REQ: REQ-20260808-ATS-003
Agent: docops
Depends On: WI-20260808-ATS-012
Blocks: -

[WI SUMMARY]
Why: 독립 검증에서 발견된 경미한 정확성 문제 두 건을 교정하여 SR-99와 SR-101을 근거와 완전히 일치시킨다.
Scope (in/out): SR-99의 bitrate 단위와 SR-101의 실제 지연 안내 인용 문구만 교정하고 검증 산출물을 생성한다. 다른 SR 내용, 인덱스 수치, 제품 코드, DB, 공개 데이터는 수정하지 않는다.
DoD: 두 MINOR가 정확히 교정되고 문서 링크·UTF-8·후행 공백·validate-docs·git diff 검증이 통과한다.
Constraints/Forbidden: 의미·요구 범위·권고안·인덱스 상태를 바꾸지 않는다. 근거 없는 추가 문구를 넣지 않는다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] `docs/SR/SR-101.md`의 인수 테스트 지연 안내 인용이 실제 PlayerBar 상수인 `재생이 지연되고 있습니다. 연결을 확인한 뒤 다시 시도해 주세요.`와 일치한다.
- [ ] `docs/SR/SR-99.md`의 고정 비트레이트 단위가 실제 계산인 128 Kibit/s를 의미하도록 일관되게 표기된다.
- [ ] 세 곡의 수치, 계산식, 제안, 상태, 인덱스 수치는 변경하지 않는다.
Performance:
- [ ] 해당 없음(문서 교정 WI).
Quality:
- [ ] validate-docs와 `git diff --check`가 통과한다.
- [ ] 교정 파일에 대체문자·후행 공백·깨진 로컬 링크가 없다.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1/2 (Policies and Context):
- docs/SR/SR-99.md
- docs/SR/SR-101.md
- docs/SR/index.md
- docs/index.md

REQ/Context Docs:
- deliverables/user/REQ-20260808-ATS-003.md
- deliverables/agent/WI-20260808-ATS-008-evidence-pack.md
- deliverables/agent/WI-20260808-ATS-010-evidence-pack.md
- deliverables/user/WI-20260808-ATS-012-summary.md
- deliverables/agent/WI-20260808-ATS-012-evidence-pack.md

Repro/Logs:
- `python .agents/skills/validate-docs/scripts/validate_docs.py`
- `git diff --check`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260808-ATS-013-summary.md : two MINOR corrections and final validation result
Agent-facing -> deliverables/agent/WI-20260808-ATS-013-evidence-pack.md : exact before/after pointers, commands, outputs, rollback
Handoff Packet -> deliverables/agent/WI-20260808-ATS-013-handoff.md : This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: command, exit code, key result 포함
Rollback: 두 문구만 이전 표현으로 되돌리는 방법 기록
