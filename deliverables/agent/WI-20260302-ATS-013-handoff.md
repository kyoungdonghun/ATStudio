[WI HEADER]
WI ID: WI-20260302-ATS-013
REQ: REQ-20260302-ATS-012
Agent: re
Depends On: WI-20260302-ATS-007, WI-20260302-ATS-008, WI-20260302-ATS-009, WI-20260302-ATS-010, WI-20260302-ATS-011, WI-20260302-ATS-012
Blocks: WI-20260302-ATS-014, WI-20260302-ATS-015

[WI SUMMARY]
Why: Phase 1 (WI-007~012) 완료 후 전체 회귀 테스트 — CRITICAL 2건 + MAJOR 11건 수정 사항이 기존 테스트를 깨뜨리지 않으며 신규 테스트가 정상 동작하는지 독립 검증
Scope (in):
  - `gradlew.bat clean test` 전체 테스트 실행 (커밋 e7c6d7a 기준)
  - 테스트 결과 분석: 실패 케이스 원인 파악 및 보고
  - 신규 추가 테스트 검증 (DownloadServiceTest, OAuth2ServiceTest, CompanyCertificationTest, QuestionTest, TrackServiceTest, PlaylistServiceTest, UserServiceTest)
  - BUILD 성공 여부 확인
Scope (out): 코드 수정 금지, 새 테스트 작성 금지 (재현 보고만 수행)
DoD:
  - BUILD SUCCESSFUL, 0 failures
  - 전체 테스트 수 및 신규 테스트 통과 여부 보고
  - 실패 시: 정확한 테스트명, 에러 메시지, 원인 분석 포함
Constraints/Forbidden:
  - 소스 코드/테스트 파일 수정 절대 금지
  - 검증 보고만 수행 (구현은 se 역할)
  - 실패 발견 시 수정 시도 금지 → evidence-pack에 기록만

[ACCEPTANCE CRITERIA]
Functional:
- [ ] 전체 테스트 BUILD SUCCESSFUL
- [ ] 0 failures, 0 errors
- [ ] DownloadServiceTest: unlimitedPlan 관련 신규 테스트 3건 PASS
- [ ] OAuth2ServiceTest: null guard 관련 테스트 PASS
- [ ] CompanyCertificationTest: 상태전이 유효/무효 케이스 PASS
- [ ] QuestionTest: 상태전이 유효/무효 케이스 PASS
- [ ] TrackServiceTest: deleteTrack track_tags 삭제 검증 PASS
- [ ] PlaylistServiceTest: deletePlaylist playlist_tracks 삭제 검증 PASS
Quality:
- [ ] BUILD SUCCESSFUL
- [ ] 테스트 총 건수 보고 (기대: 530건 이상)

[INPUT POINTERS]
Tier 0 (Constitution):
- docs/standards/core-principles.md

Tier 1 (Quality Policy):
- docs/policies/quality-gates.md

REQ:
- deliverables/user/REQ-20260302-ATS-012.md

Phase 1 WI Evidence Packs (검증 대상 변경사항):
- deliverables/agent/WI-20260302-ATS-007-evidence-pack.md
- deliverables/agent/WI-20260302-ATS-008-evidence-pack.md
- deliverables/agent/WI-20260302-ATS-009-evidence-pack.md
- deliverables/agent/WI-20260302-ATS-010-evidence-pack.md
- deliverables/agent/WI-20260302-ATS-011-evidence-pack.md
- deliverables/agent/WI-20260302-ATS-012-evidence-pack.md

[OUTPUT CONTRACT]
User-facing → deliverables/user/WI-20260302-ATS-013-summary.md
Agent-facing → deliverables/agent/WI-20260302-ATS-013-evidence-pack.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines): 테스트 실패 시 정확한 테스트 클래스:메서드명 및 에러 스택 포함
Tests: gradlew.bat clean test
Rollback: N/A (검증 전용 WI)
