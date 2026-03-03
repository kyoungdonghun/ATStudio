[WI HEADER]
WI ID: WI-20260302-ATS-007
REQ: REQ-20260302-ATS-012
Agent: se
Depends On: -
Blocks: WI-20260302-ATS-013

[WI SUMMARY]
Why: CRITICAL C-1 — downloadPerDay=-1 무제한 플랜 구독자 다운로드 완전 차단 수정 + M-1 @Transactional 표준 준수
Scope (in):
  - DownloadService.java:46 — downloadPerDay == -1 체크 (무제한 가드)
  - DownloadService.java:19 — 클래스 레벨 @Transactional(readOnly=true) 추가
  - DownloadServiceTest.java — 무제한 플랜 다운로드 허용 테스트 추가
Scope (out): 다른 서비스 파일 수정 금지
DoD:
  - downloadPerDay = -1인 구독자가 정상적으로 다운로드 가능
  - downloadPerDay = 5인 구독자는 5건 초과 시 DOWNLOAD_LIMIT_EXCEEDED
  - BUILD SUCCESSFUL, 0 failures
Constraints/Forbidden:
  - 코드 수정은 DownloadService.java, DownloadServiceTest.java만 허용
  - 다른 파일 수정 금지

[ACCEPTANCE CRITERIA]
Functional:
- [ ] downloadPerDay == -1 → 다운로드 한도 체크 우회 (무제한 허용)
- [ ] downloadPerDay > 0 → 기존 한도 체크 유지
- [ ] 클래스 레벨 @Transactional(readOnly=true) 적용, mutating 메서드에 @Transactional override
Quality:
- [ ] BUILD SUCCESSFUL
- [ ] 신규 테스트 포함 전체 테스트 0 failures

[INPUT POINTERS]
Tier 0 (Constitution):
- docs/standards/core-principles.md

Tier 0 (Standards):
- docs/standards/development-standards.md

REQ:
- deliverables/user/REQ-20260302-ATS-012.md

Files:
- src/main/java/com/atstudio/atstudio/service/DownloadService.java:40-55
- src/test/java/com/atstudio/atstudio/service/DownloadServiceTest.java

[OUTPUT CONTRACT]
User-facing → deliverables/user/WI-20260302-ATS-007-summary.md
Agent-facing → deliverables/agent/WI-20260302-ATS-007-evidence-pack.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines): 수정된 라인 명시
Tests: gradlew.bat test --tests "*DownloadServiceTest"
Rollback: git revert
