[WI HEADER]
WI ID: WI-20260303-ATS-005
REQ: REQ-20260303-ATS-003
Agent: se
Depends On: -
Blocks: WI-20260303-ATS-006

[WI SUMMARY]
Why: 어드민 큐레이팅 앨범(Album) 도메인이 미구현 상태. Playlist(구독자 재생목록)와 분리된 별도 도메인으로 신규 구현 필요.
Scope (in/out):
  In:
  - Entity: Album.java, AlbumTrack.java, AlbumTrackId.java (복합PK)
  - Repository: AlbumRepository.java, AlbumTrackRepository.java
  - DTO: AlbumCreateRequest, AlbumUpdateRequest, AlbumTrackAddRequest, AlbumTrackOrderRequest, AlbumResponse, AlbumDetailResponse, AlbumListItemResponse
  - Service: AlbumService.java
  - Controller: AlbumController.java (8개 API)
  - SecurityConfig.java 업데이트 (Album 권한 규칙 추가)
  - api-spec.md Section 15 추가 (8개 API 명세)
  - db-schema.md 업데이트 (albums + album_tracks 테이블)
  - AlbumServiceTest.java (단위 테스트, Mockito)
  Out:
  - Playlist 도메인 변경 없음
  - 앨범 좋아요/댓글/검색 기능
  - AlbumControllerTest (선택사항 — 시간 여유 있을 때만)

DoD:
  - 8개 API 정상 동작 (ADMIN write, PUBLIC read)
  - is_active=false 앨범은 GET 목록/상세 제외
  - 트랙 중복 등록 가능 (M:N)
  - ./gradlew test 전체 통과 (0 failures)
  - api-spec.md Section 15 추가, db-schema.md 업데이트

Constraints/Forbidden:
  - BaseEntity 상속 필수 (created_at, updated_at)
  - 삭제는 소프트 삭제 (is_active=false) — 기존 Playlist 패턴과 동일
  - Entity를 Controller에서 직접 반환 금지 (DTO 분리 필수)
  - @Transactional 클래스 레벨 readOnly=true, mutating 메서드만 @Transactional override
  - Playlist 도메인 코드 변경 금지

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] POST /api/albums — ADMIN 전용, 201 Created, AlbumResponse 반환
  - [ ] GET /api/albums — PUBLIC, 200 OK, is_active=true 앨범 목록 (AlbumListItemResponse 배열)
  - [ ] GET /api/albums/{id} — PUBLIC, 200 OK, AlbumDetailResponse (트랙 목록 포함)
  - [ ] PUT /api/albums/{id} — ADMIN 전용, 200 OK, 수정된 AlbumResponse 반환
  - [ ] DELETE /api/albums/{id} — ADMIN 전용, 204 No Content, is_active=false 소프트 삭제
  - [ ] POST /api/albums/{id}/tracks — ADMIN 전용, 200 OK, 트랙 추가
  - [ ] DELETE /api/albums/{id}/tracks/{trackId} — ADMIN 전용, 204 No Content, 트랙 제거
  - [ ] PUT /api/albums/{id}/tracks — ADMIN 전용, 200 OK, 트랙 순서 변경
  - [ ] 존재하지 않는 앨범 접근 → RESOURCE_NOT_FOUND (404)
  - [ ] 존재하지 않는 트랙을 앨범에 추가 시도 → RESOURCE_NOT_FOUND (404)

Performance:
  - N/A

Quality:
  - [ ] ./gradlew test 0 failures (기존 546건 + 신규 테스트 포함)
  - [ ] AlbumServiceTest: 주요 케이스(createAlbum, getAlbums, getAlbum, updateAlbum, deleteAlbum, addTrack, removeTrack, reorderTracks) 단위 테스트

[INPUT POINTERS]
Tier 0 (Constitution):
- docs/standards/core-principles.md

Tier 0 (Development Standards):
- docs/standards/development-standards.md

Tier 1 (Quality):
- docs/policies/quality-gates.md

REQ/Context Docs:
- deliverables/user/REQ-20260303-ATS-003.md

Reference (기존 패턴 참고):
- src/main/java/com/atstudio/atstudio/entity/Playlist.java          ← Album 엔티티 패턴 참고
- src/main/java/com/atstudio/atstudio/entity/PlaylistTrack.java     ← AlbumTrack 패턴 참고
- src/main/java/com/atstudio/atstudio/entity/key/PlaylistTrackId.java ← AlbumTrackId 복합PK 패턴 참고
- src/main/java/com/atstudio/atstudio/service/PlaylistService.java   ← AlbumService 구조 참고
- src/main/java/com/atstudio/atstudio/controller/PlaylistController.java ← AlbumController 패턴 참고
- src/main/java/com/atstudio/atstudio/config/SecurityConfig.java     ← 권한 규칙 삽입 위치 확인
- src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java ← 에러 코드 패턴 확인
- src/main/java/com/atstudio/atstudio/entity/BaseEntity.java         ← BaseEntity 상속 패턴
- src/test/java/com/atstudio/atstudio/service/PlaylistServiceTest.java ← 테스트 패턴 참고
- docs/design/api-spec.md   ← 기존 명세 패턴 확인 + Section 15 추가 위치
- docs/design/db-schema.md  ← 기존 스키마 패턴 확인 + albums/album_tracks 추가 위치

[API 명세]
8개 API 구현 상세:

1. POST /api/albums
   - Auth: ADMIN
   - Request: multipart/form-data (title, description, thumbnailFile)
   - Response: 201 Created, AlbumResponse

2. GET /api/albums
   - Auth: 없음 (PUBLIC)
   - Response: 200 OK, { "dataList": [AlbumListItemResponse] }
   - 조건: is_active=true만

3. GET /api/albums/{id}
   - Auth: 없음 (PUBLIC)
   - Response: 200 OK, AlbumDetailResponse (트랙 목록 포함)
   - 조건: is_active=true만 (false면 404)

4. PUT /api/albums/{id}
   - Auth: ADMIN
   - Request: multipart/form-data (title, description, thumbnailFile — 모두 optional)
   - Response: 200 OK, AlbumResponse

5. DELETE /api/albums/{id}
   - Auth: ADMIN
   - Response: 204 No Content
   - 동작: is_active=false 소프트 삭제

6. POST /api/albums/{id}/tracks
   - Auth: ADMIN
   - Request: { "trackId": Long }
   - Response: 200 OK, AlbumDetailResponse

7. DELETE /api/albums/{id}/tracks/{trackId}
   - Auth: ADMIN
   - Response: 204 No Content

8. PUT /api/albums/{id}/tracks
   - Auth: ADMIN
   - Request: { "trackOrders": [{ "trackId": Long, "order": Integer }] }
   - Response: 200 OK, AlbumDetailResponse

[DTO 설계 참고]
- AlbumResponse: id, title, description, thumbnailUrl, trackCount, createdAt
- AlbumListItemResponse: id, title, thumbnailUrl, trackCount
- AlbumDetailResponse: id, title, description, thumbnailUrl, tracks(List<AlbumTrackItemResponse>), createdAt
- AlbumTrackItemResponse: trackId, title, artistName, thumbnailUrl, order
- AlbumCreateRequest: title(필수), description, thumbnailFile
- AlbumUpdateRequest: title, description, thumbnailFile (모두 optional)
- AlbumTrackAddRequest: trackId
- AlbumTrackOrderRequest: trackOrders(List<{trackId, order}>)

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260303-ATS-005-summary.md :
  - 구현 결과 요약, 변경/생성 파일 목록(파일명:라인), 테스트 결과(총 수, failures)
Agent-facing -> deliverables/agent/WI-20260303-ATS-005-evidence-pack.md :
  - 생성/변경된 코드 포인터 (파일:라인), 테스트 명령어 및 출력 결과, 주요 설계 결정
Handoff Packet -> deliverables/agent/WI-20260303-ATS-005-handoff.md :
  - 이 파일 (트레이서빌리티용)

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 생성/변경 파일별 라인 번호 명시
Tests: gradlew.bat test 실행 결과 (총 테스트 수, failures 수) — Windows 환경이므로 gradlew.bat 사용
Rollback: 신규 파일 삭제, SecurityConfig 추가분 제거, api-spec.md/db-schema.md 추가분 제거
