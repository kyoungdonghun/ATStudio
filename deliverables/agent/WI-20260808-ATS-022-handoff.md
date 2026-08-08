[WI HEADER]
WI ID: WI-20260808-ATS-022
REQ: REQ-20260808-ATS-004
Agent: docops
Depends On: WI-20260808-ATS-014~021
Blocks: WI-20260808-ATS-023~027
[WI SUMMARY]
Why: SR-94~101 구현 결과에 맞춰 설계·API·DB·UI·운영 문서를 현행화한다.
Scope (in/out): SR 상태와 완료 근거, API/DB/화면 계약, dry-run·backfill 승인 경계, 인덱스 동기화. 역사적 증거 왜곡 금지.
DoD: 문서와 코드 3-way 일치; 인덱스 정확; validate-docs와 diff check 통과.
Constraints/Forbidden: 현재 브랜치에서만 작업한다. 무관 파일·기존 ZIP·비밀값을 수정하지 않는다. 파일 삭제, 스키마 변경, 실제 데이터 변경, 외부 결제 호출은 승인 없이 금지한다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] 문서와 코드 3-way 일치; 인덱스 정확; validate-docs와 diff check 통과.
Performance:
- [ ] 컬렉션·동시성·미디어 처리 경로에서 기존 대비 비정상적인 N+1, deadlock, 반복 디코딩을 만들지 않는다.
Quality:
- [ ] 대상 테스트와 관련 정적 검사가 통과한다.
- [ ] 변경 파일과 재현·롤백 근거를 남긴다.

[INPUT POINTERS]
Tier 0/1/2 and context:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
- docs/SR/SR-94.md~SR-101.md
- docs/design 및 docs/ui 관련 문서
- deliverables/user/REQ-20260808-ATS-004.md
Files:
- docs/SR/*
- docs/design/*
- docs/ui/*
- docs/index.md 및 관련 인덱스

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260808-ATS-022-summary.md
Agent-facing -> deliverables/agent/WI-20260808-ATS-022-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260808-ATS-022-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers, executed commands, test results, risks, rollback, and next blocked WI status are required.
