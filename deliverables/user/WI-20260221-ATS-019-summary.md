[WI SUMMARY - User Facing]
WI ID: WI-20260221-ATS-019
REQ: REQ-20260221-ATS-004
Status: ✅ Completed

[WHAT WAS DONE]
Playlist 서비스 단위 테스트 + 컨트롤러 권한 테스트 작성 완료.

[CREATED FILES]
- src/test/java/com/atstudio/atstudio/service/PlaylistServiceTest.java (12 tests)
- src/test/java/com/atstudio/atstudio/controller/PlaylistControllerTest.java (16 tests)

[TEST RESULTS]
PlaylistServiceTest: 12/12 passed
  - createPlaylist(): 성공, 비구독자(NO_ACTIVE_SUBSCRIPTION) 예외
  - getMyPlaylists(): 성공
  - getPlaylistDetail(): 성공, 소유자 아님(RESOURCE_NOT_ACCESS) 예외, 없음(RESOURCE_NOT_FOUND) 예외
  - addTrack(): 성공, 중복(DATA_INTEGRITY_VIOLATION) 예외
  - updatePlaylist(): 성공
  - reorderTracks(): 성공
  - removeTrack(): 성공
  - deletePlaylist(): 성공 (isActive=false 검증)

PlaylistControllerTest: 16/16 passed
  - POST /api/playlists: 비인증→401, 인증→201
  - GET /api/playlists: 비인증→401, 인증→200
  - GET /api/playlists/{id}: 비인증→401, 인증→200
  - POST /api/playlists/{id}/tracks: 비인증→401, 인증→201
  - PUT /api/playlists/{id}: 비인증→401, 인증→200
  - PUT /api/playlists/{id}/tracks: 비인증→401, 인증→200
  - DELETE /api/playlists/{id}/tracks/{trackId}: 비인증→401, 인증→204
  - DELETE /api/playlists/{id}: 비인증→401, 인증→204

[OVERALL REGRESSION]
Total: 323/323 passed (이전 295 → 신규 +28)
BUILD SUCCESSFUL
