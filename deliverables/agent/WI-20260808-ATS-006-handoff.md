[WI HEADER]
WI ID: WI-20260808-ATS-006
REQ: REQ-20260808-ATS-002
Agent: docops
Depends On: WI-20260808-ATS-003, WI-20260808-ATS-004, WI-20260808-ATS-005
Blocks: WI-20260808-ATS-007
[WI SUMMARY]
Why: 세 전문 조사 결과를 기존 SR 관례에 맞는 독립 문서로 통합하고 문서 인덱스를 동기화한다.
Scope (in/out): `docs/SR/SR-96.md`, `SR-97.md`, `SR-98.md`, `docs/SR/index.md`, `docs/index.md`와 이 WI 산출물만 작성·수정한다. 코드·DB·활성 정책은 수정하지 않는다.
DoD: 세 SR이 사실/요구/제안/미확정을 구분하고 OPEN으로 인덱싱되며 SR 수 97개, 전체 문서 수 199개로 일치한다.
Constraints/Forbidden: 선행 WI 세 개가 모두 완료되기 전 시작하지 않는다. 확인되지 않은 런타임·DB 관찰을 단정하지 않는다. SR-68과 SR-14를 재작성하지 않는다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] SR-96에 현재 허용 판정과 마지막 관리자·동시성·세션·감사·복구 요구가 포함된다.
- [ ] SR-97에 플랜 변경 불가 현황, 두 대안, 상태-만료일 행렬과 결제 부작용이 포함된다.
- [ ] SR-98에 비율 보존/잘림 구분, 1:1 안내·실제 미리보기 중심 권고와 공식 근거가 포함된다.
- [ ] `docs/SR/index.md`와 `docs/index.md`가 실제 파일 수와 동기화된다.
Performance:
- [ ] 해당 없음(문서 전용 WI).
Quality:
- [ ] 기존 SR historical record의 한국어 작성 관례와 구조를 따른다.
- [ ] Markdown 표와 로컬·외부 링크가 유효하다.
- [ ] `git diff --check`에 걸리는 공백 오류가 없다.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1/2 (Policies and Task Context):
- docs/policies/archive-policy.md
- docs/SR/index.md
- docs/SR/SR-14.md
- docs/SR/SR-68.md
- docs/SR/SR-94.md
- docs/SR/SR-95.md
- docs/index.md

REQ/Context Docs:
- deliverables/user/REQ-20260808-ATS-002.md
- deliverables/user/WI-20260808-ATS-003-summary.md
- deliverables/user/WI-20260808-ATS-004-summary.md
- deliverables/user/WI-20260808-ATS-005-summary.md
- deliverables/agent/WI-20260808-ATS-003-evidence-pack.md
- deliverables/agent/WI-20260808-ATS-004-evidence-pack.md
- deliverables/agent/WI-20260808-ATS-005-evidence-pack.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260808-ATS-006-summary.md :
- 생성 문서, 핵심 판단, 남은 결정 사항
Agent-facing -> deliverables/agent/WI-20260808-ATS-006-evidence-pack.md :
- Evidence pointers, 변경 파일, 검증 결과, rollback
Handoff Packet -> deliverables/agent/WI-20260808-ATS-006-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: 문서 파일 수·상태 집계, 링크 검사, `git diff --check` 결과 포함
Rollback (if needed): 이 WI가 추가한 SR 세 건과 인덱스 변경만 되돌리는 방법 기록
