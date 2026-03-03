[WI HEADER]
WI ID: WI-20260302-ATS-011
REQ: REQ-20260302-ATS-012
Agent: se
Depends On: -
Blocks: WI-20260302-ATS-013

[WI SUMMARY]
Why: M-5 Track 소프트삭제 시 track_tags 고아 데이터 + M-10 Playlist 소프트삭제 시 playlist_tracks 고아 데이터
Scope (in):
  - TrackService.java:164-168 — deleteTrack()에 trackTagRepository.deleteAllByTrack(track) 선행 호출 추가
  - PlaylistService.java:181-186 — deletePlaylist()에 playlistTrackRepository.deleteAllByPlaylist(playlist) 선행 호출 추가 (또는 deleteAllByIdPlaylistId)
  - 관련 테스트 추가
Scope (out): Track/Playlist 엔티티 변경 금지, 다른 메서드 수정 금지
DoD:
  - Track 소프트삭제 후 track_tags 테이블에 해당 track 관련 레코드 없음
  - Playlist 소프트삭제 후 playlist_tracks 테이블에 해당 playlist 관련 레코드 없음
  - BUILD SUCCESSFUL, 0 failures
Constraints/Forbidden:
  - TrackService.deleteTrack(), PlaylistService.deletePlaylist() 외 메서드 수정 금지
  - CascadeType.ALL 추가 금지 (서비스 레이어 명시적 삭제 패턴 유지)

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Track 삭제(소프트) → trackTagRepository.deleteAllByTrack() 호출 확인
- [ ] Playlist 삭제(소프트) → playlist_tracks 삭제 호출 확인
- [ ] 기존 Track/Playlist 소프트삭제 기능 미영향
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
- src/main/java/com/atstudio/atstudio/service/TrackService.java:160-170
- src/main/java/com/atstudio/atstudio/service/PlaylistService.java:178-190
- src/main/java/com/atstudio/atstudio/repository/TrackTagRepository.java
- src/main/java/com/atstudio/atstudio/repository/PlaylistTrackRepository.java
- src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java
- src/test/java/com/atstudio/atstudio/service/PlaylistServiceTest.java

[OUTPUT CONTRACT]
User-facing → deliverables/user/WI-20260302-ATS-011-summary.md
Agent-facing → deliverables/agent/WI-20260302-ATS-011-evidence-pack.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines): 수정된 라인 명시
Tests: gradlew.bat test --tests "*TrackService*" --tests "*PlaylistService*"
Rollback: git revert
