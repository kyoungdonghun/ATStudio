[WI HEADER]
WI ID: WI-20260302-ATS-010
REQ: REQ-20260302-ATS-012
Agent: se
Depends On: -
Blocks: WI-20260302-ATS-013

[WI SUMMARY]
Why: M-3 LicenseRepository N+1 (@EntityGraph 누락) + M-4 PlaylistService trackCount N+1 (M+1 쿼리)
Scope (in):
  - LicenseRepository.java:18,20 — @EntityGraph(attributePaths = "track") 추가
  - PlaylistService.java:66-71 — trackCount N+1 → @Query batch count로 개선
  - PlaylistTrackRepository — countByPlaylistIds 배치 쿼리 메서드 추가 (또는 IN 쿼리)
  - 관련 테스트 추가
Scope (out): License/Playlist 비즈니스 로직 변경 금지
DoD:
  - License 목록 조회 시 track LAZY 로딩으로 인한 N+1 해소
  - Playlist 목록 조회 시 trackCount N+1 해소 (단일 batch 쿼리)
  - BUILD SUCCESSFUL, 0 failures
Constraints/Forbidden:
  - LicenseRepository, PlaylistService, PlaylistTrackRepository만 수정
  - 응답 형식 변경 금지

[ACCEPTANCE CRITERIA]
Functional:
- [ ] LicenseRepository.findAllByUser() — @EntityGraph로 track 즉시 로딩
- [ ] LicenseRepository.findAllByUser_Id() — 동일 적용
- [ ] PlaylistService.getMyPlaylists() — M개 플레이리스트에 대해 단일 count 쿼리 (또는 최소 쿼리)
- [ ] 기존 License/Playlist 기능 미영향
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
- src/main/java/com/atstudio/atstudio/repository/LicenseRepository.java
- src/main/java/com/atstudio/atstudio/service/PlaylistService.java:60-75
- src/main/java/com/atstudio/atstudio/repository/PlaylistTrackRepository.java
- src/test/java/com/atstudio/atstudio/service/PlaylistServiceTest.java

[OUTPUT CONTRACT]
User-facing → deliverables/user/WI-20260302-ATS-010-summary.md
Agent-facing → deliverables/agent/WI-20260302-ATS-010-evidence-pack.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines): 수정된 라인 명시
Tests: gradlew.bat test --tests "*LicenseRepository*" --tests "*PlaylistService*"
Rollback: git revert
