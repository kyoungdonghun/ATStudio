[WI HEADER]
WI ID: WI-20260808-ATS-011
REQ: REQ-20260808-ATS-003
Agent: docops
Depends On: WI-20260808-ATS-008, WI-20260808-ATS-009, WI-20260808-ATS-010
Blocks: WI-20260808-ATS-012
[WI SUMMARY]
Why: 세 전문 조사 결과를 SR-99~101로 통합하고 문서 인덱스를 동기화한다.
Scope (in/out): 신규 SR 세 건, 두 인덱스, WI-011 summary/evidence만 생성·수정한다. 코드, DB, 기존 SR 본문은 수정하지 않는다.
DoD: 세 SR이 사실/재현/원인/영향/제안/미확정을 구분해 OPEN으로 등록되고 SR 100개, OPEN 15건, 전체 문서 202개로 일치한다.
Constraints/Forbidden: 선행 WI 세 개가 완료되기 전 시작하지 않는다. 공개 URL을 영구 환경으로 표현하지 않는다. 확인되지 않은 수치를 단정하지 않는다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] SR-99가 세 곡 수치, 128kbps 추정 원인, 생성/수정/백필을 포함한다.
- [ ] SR-100이 Usage 우선과 Instrument 포함, 홈 과밀화 대안을 포함한다.
- [ ] SR-101이 stalled 이벤트 의미와 album waveform null 및 타 화면 영향 범위를 포함한다.
- [ ] SR/index와 docs/index가 실제 파일·상태·문서 수와 일치한다.
Performance:
- [ ] 해당 없음(문서 전용 WI).
Quality:
- [ ] 한국어 SR historical record 관례를 따른다.
- [ ] 런타임 관찰과 영구 정책을 구분한다.
- [ ] 로컬 링크와 Markdown이 유효하다.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1/2 (Task Context):
- docs/policies/archive-policy.md
- docs/SR/index.md
- docs/SR/SR-04.md
- docs/SR/SR-90.md
- docs/SR/SR-99.md
- docs/SR/SR-100.md
- docs/SR/SR-101.md
- docs/index.md

REQ/Context Docs:
- deliverables/user/REQ-20260808-ATS-003.md
- deliverables/user/WI-20260808-ATS-008-summary.md
- deliverables/user/WI-20260808-ATS-009-summary.md
- deliverables/user/WI-20260808-ATS-010-summary.md
- deliverables/agent/WI-20260808-ATS-008-evidence-pack.md
- deliverables/agent/WI-20260808-ATS-009-evidence-pack.md
- deliverables/agent/WI-20260808-ATS-010-evidence-pack.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260808-ATS-011-summary.md : created docs, decisions, unresolved items
Agent-facing -> deliverables/agent/WI-20260808-ATS-011-evidence-pack.md : changed files, evidence pointers, counts, checks, rollback
Handoff Packet -> deliverables/agent/WI-20260808-ATS-011-handoff.md : This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: file/status/count/link/whitespace checks 포함
Rollback: 신규 SR과 해당 인덱스 변경만 되돌리는 방법 기록
