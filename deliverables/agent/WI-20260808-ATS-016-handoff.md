[WI HEADER]
WI ID: WI-20260808-ATS-016
REQ: REQ-20260808-ATS-004
Agent: se
Depends On: WI-20260808-ATS-015
Blocks: WI-20260808-ATS-017, WI-20260808-ATS-019
[WI SUMMARY]
Why: SR-99 실제 미디어 분석 기반 duration·waveform 단일 계약과 읽기 전용 dry-run을 구현한다.
Scope (in/out): MP3/WAV 단일 분석 결과, 생성·교체 원자성, 손상 파일 거절, 기존 Track dry-run 보고. 실제 DB backfill은 금지.
DoD: CBR/VBR/WAV fixture 정확도; 교체 시 duration+waveform 동시 갱신; 실패 시 기존값 유지; dry-run은 DB 무변경.
Constraints/Forbidden: 현재 브랜치에서만 작업한다. 무관 파일·기존 ZIP·비밀값을 수정하지 않는다. 파일 삭제, 스키마 변경, 실제 데이터 변경, 외부 결제 호출은 승인 없이 금지한다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] CBR/VBR/WAV fixture 정확도; 교체 시 duration+waveform 동시 갱신; 실패 시 기존값 유지; dry-run은 DB 무변경.
Performance:
- [ ] 컬렉션·동시성·미디어 처리 경로에서 기존 대비 비정상적인 N+1, deadlock, 반복 디코딩을 만들지 않는다.
Quality:
- [ ] 대상 테스트와 관련 정적 검사가 통과한다.
- [ ] 변경 파일과 재현·롤백 근거를 남긴다.

[INPUT POINTERS]
Tier 0/1/2 and context:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/design/usecase/sound-track.md
- docs/SR/SR-99.md
- deliverables/user/REQ-20260808-ATS-004.md
Files:
- src/main/java/com/atstudio/atstudio/service/TrackService.java
- 미디어 분석 컴포넌트와 dry-run 도구
- 관련 fixture 및 테스트 파일

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260808-ATS-016-summary.md
Agent-facing -> deliverables/agent/WI-20260808-ATS-016-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260808-ATS-016-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers, executed commands, test results, risks, rollback, and next blocked WI status are required.
