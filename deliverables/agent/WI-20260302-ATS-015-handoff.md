[WI HEADER]
WI ID: WI-20260302-ATS-015
REQ: REQ-20260302-ATS-012
Agent: cr
Depends On: WI-20260302-ATS-013
Blocks: -

[WI SUMMARY]
Why: Phase 3 코드 리뷰 — N+1/Cascade/상태전이 수정 범위 (WI-010~012) 검토
Why: M-3(@EntityGraph), M-4(trackCount batch), M-5/M-10(고아 데이터), M-6(findTop), M-7/M-8(상태기계) 수정의 정확성과 완전성 검토
Scope (in):
  - LicenseRepository.java — @EntityGraph 적용
  - PlaylistTrackRepository.java — countByPlaylistIdIn batch query
  - PlaylistService.java — getMyPlaylists N+1 수정, deletePlaylist cascade
  - TrackService.java — deleteTrack cascade
  - CompanyCertificationRepository.java — findTopByUserOrderByCreatedAtDesc
  - CompanyCertification.java — validateTransition 상태기계
  - Question.java — validateTransition 상태기계
  - 관련 테스트: CompanyCertificationTest, QuestionTest, TrackServiceTest, PlaylistServiceTest
Scope (out): Download/User/Auth/Error 수정 (WI-014 담당)
DoD:
  - CRITICAL/MAJOR 이슈 없음
  - 상태전이 규칙 완전성 검토 (누락 케이스 여부)
  - N+1 해소 방식 적절성 검토
  - 고아 데이터 삭제 순서 정확성 검토
Constraints/Forbidden:
  - 코드 직접 수정 금지
  - 리뷰 결과를 evidence-pack에 기록

[ACCEPTANCE CRITERIA]
Functional:
- [ ] @EntityGraph attributePaths 필드명 정확성 확인 (License.track)
- [ ] countByPlaylistIdIn JPQL 구문 정확성 및 엣지케이스 (빈 리스트 처리)
- [ ] deleteTrack — trackTagRepository.deleteAllByTrack() 호출 순서 (deactivate 전)
- [ ] deletePlaylist — deleteAllByIdPlaylistId() 호출 순서 (deactivate 전)
- [ ] CompanyCertification 상태전이 규칙 완전성 (APPROVED→REVISION_REQUESTED 등 누락 케이스)
- [ ] Question 상태전이 규칙 완전성 (RESOLVED→OPEN 등 누락 케이스)
Quality:
- [ ] CRITICAL 이슈 없음
- [ ] MAJOR 이슈 없음 (또는 문서화됨)

[INPUT POINTERS]
Tier 0 (Constitution):
- docs/standards/core-principles.md

Tier 0 (Standards):
- docs/standards/development-standards.md

REQ:
- deliverables/user/REQ-20260302-ATS-012.md

Evidence Packs (리뷰 대상):
- deliverables/agent/WI-20260302-ATS-010-evidence-pack.md
- deliverables/agent/WI-20260302-ATS-011-evidence-pack.md
- deliverables/agent/WI-20260302-ATS-012-evidence-pack.md

Files (리뷰 대상 소스):
- src/main/java/com/atstudio/atstudio/repository/LicenseRepository.java
- src/main/java/com/atstudio/atstudio/repository/PlaylistTrackRepository.java
- src/main/java/com/atstudio/atstudio/service/PlaylistService.java
- src/main/java/com/atstudio/atstudio/service/TrackService.java
- src/main/java/com/atstudio/atstudio/repository/CompanyCertificationRepository.java
- src/main/java/com/atstudio/atstudio/entity/CompanyCertification.java
- src/main/java/com/atstudio/atstudio/entity/Question.java
- src/test/java/com/atstudio/atstudio/entity/CompanyCertificationTest.java
- src/test/java/com/atstudio/atstudio/entity/QuestionTest.java
- src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java
- src/test/java/com/atstudio/atstudio/service/PlaylistServiceTest.java

[OUTPUT CONTRACT]
User-facing → deliverables/user/WI-20260302-ATS-015-summary.md
Agent-facing → deliverables/agent/WI-20260302-ATS-015-evidence-pack.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 리뷰 소견 파일:라인 명시
Tests: N/A (검증은 WI-013에서 완료)
Rollback: N/A
