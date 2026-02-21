[WI HEADER]
WI ID: WI-20260221-ATS-018
REQ: REQ-20260221-ATS-004
Agent: se
Depends On: -
Blocks: WI-20260221-ATS-019

[WI SUMMARY]
Why: Playlist 도메인 8개 API 전체 구현. 구독자 전용 재생목록 CRUD + 트랙 관리 + 순서 변경.
Scope (in/out):
  In:
    - Playlist 엔티티 update() 메서드 추가
    - PlaylistRepository, PlaylistTrackRepository 신규 작성
    - PlaylistService 신규 작성 (8개 API 비즈니스 로직)
    - PlaylistController 신규 작성
    - 관련 DTO 신규 작성 (Request/Response)
  Out:
    - 테스트 코드 (WI-019에서 작성)
    - 기존 파일 수정 금지 (Playlist/PlaylistTrack/PlaylistTrackId 엔티티는 update() 메서드 추가만 허용)
    - 결제/구독 생성 로직
DoD:
  - 8개 엔드포인트 구현 완료
  - 구독자 체크 + 소유자 체크 정확히 구현
  - 컴파일 오류 없음
Constraints/Forbidden:
  - Entity 직접 반환 금지 (DTO 변환 필수)
  - PlaylistService는 @Transactional(readOnly = true) 클래스 레벨, mutating 메서드 @Transactional override
  - 썸네일 업로드는 반드시 StorageService.store() 사용
  - 구독자 체크는 UserSubscriptionRepository.findActiveByUser() 패턴 사용

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] POST /api/playlists → 201, PlaylistResponse 반환
  - [ ] GET /api/playlists → 200, List<PlaylistListItemResponse> (trackCount 포함)
  - [ ] GET /api/playlists/{id} → 200, PlaylistDetailResponse (tracks + trackOrder)
  - [ ] POST /api/playlists/{id}/tracks → 201 성공, 409 중복
  - [ ] PUT /api/playlists/{id} → 200, 수정된 PlaylistResponse
  - [ ] PUT /api/playlists/{id}/tracks → 200, 순서 변경 반영
  - [ ] DELETE /api/playlists/{id}/tracks/{trackId} → 204
  - [ ] DELETE /api/playlists/{id} → 204, isActive=false
  - [ ] 비구독자 → 모든 API 403
  - [ ] 타인 플레이리스트 접근(3.3~3.8) → 403
Performance:
  - [ ] N+1 주의: PlaylistTrack 조회 시 @EntityGraph 또는 JOIN FETCH 사용
Quality:
  - [ ] @Transactional(readOnly = true) 클래스 레벨 표준 준수
  - [ ] 컴파일 오류 없음

[INPUT POINTERS]
Tier 0 (Constitution):
  - docs/standards/core-principles.md

Tier 0 (Standards):
  - docs/standards/development-standards.md
  - docs/standards/dto-standards.md
  - docs/standards/exception-handling.md

REQ/Context:
  - deliverables/user/REQ-20260221-ATS-004.md
  - docs/design/api-spec.md (Section 3: lines 354-492)

Existing Entities (read-only reference):
  - src/main/java/com/atstudio/atstudio/entity/Playlist.java
  - src/main/java/com/atstudio/atstudio/entity/PlaylistTrack.java
  - src/main/java/com/atstudio/atstudio/entity/key/PlaylistTrackId.java
  - src/main/java/com/atstudio/atstudio/entity/Track.java
  - src/main/java/com/atstudio/atstudio/entity/User.java
  - src/main/java/com/atstudio/atstudio/entity/UserSubscription.java

Pattern References (similar implementations to reuse patterns from):
  - src/main/java/com/atstudio/atstudio/service/LikeService.java (복합 PK 패턴)
  - src/main/java/com/atstudio/atstudio/service/DownloadQueueService.java (복합 PK 패턴)
  - src/main/java/com/atstudio/atstudio/service/NoticeService.java (@Transactional 패턴)
  - src/main/java/com/atstudio/atstudio/service/UtilService.java (구독 체크 패턴)
  - src/main/java/com/atstudio/atstudio/service/TrackService.java (StorageService 사용 패턴)
  - src/main/java/com/atstudio/atstudio/repository/LikeRepository.java (@EntityGraph 패턴)
  - src/main/java/com/atstudio/atstudio/common/dto/ResponseDTO.java

Infrastructure:
  - src/main/java/com/atstudio/atstudio/service/storage/StorageService.java
  - src/main/java/com/atstudio/atstudio/repository/UserSubscriptionRepository.java
  - src/main/java/com/atstudio/atstudio/repository/UserRepository.java
  - src/main/java/com/atstudio/atstudio/repository/TrackRepository.java
  - src/main/java/com/atstudio/atstudio/exception/ErrorCode.java
  - src/main/java/com/atstudio/atstudio/config/SecurityConfig.java

[IMPLEMENTATION GUIDE]

### 구독자 체크 패턴 (모든 public 메서드 첫 줄)
```java
private void validateSubscriber(CustomUserDetails userDetails) {
    User user = userRepository.findById(userDetails.getId())
        .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    LocalDate today = LocalDate.now();
    userSubscriptionRepository.findActiveByUser(user, SubscriptionStatus.ACTIVE, today)
        .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.FORBIDDEN));
}
```

### 소유자 체크 패턴 (3.3~3.8)
```java
private Playlist getOwnedPlaylist(Long playlistId, Long userId) {
    Playlist playlist = playlistRepository.findById(playlistId)
        .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    if (!playlist.getUser().getId().equals(userId)) {
        throw new BusinessException(BUSINESS_ERROR.FORBIDDEN);
    }
    if (!playlist.isActive()) {
        throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND);
    }
    return playlist;
}
```

### Reorder (3.6) 구현
```java
// 기존 트랙 전체 삭제 후 새 순서로 재삽입 (동일 트랜잭션 내)
@Transactional
public void reorderTracks(Long playlistId, PlaylistReorderRequest request, CustomUserDetails userDetails) {
    validateSubscriber(userDetails);
    Playlist playlist = getOwnedPlaylist(playlistId, userDetails.getId());
    playlistTrackRepository.deleteAllByIdPlaylistId(playlistId);
    List<PlaylistTrack> reordered = request.getTracks().stream()
        .map(item -> PlaylistTrack.builder()
            .id(new PlaylistTrackId(playlistId, item.getTrackId()))
            .playlist(playlist)
            .track(trackRepository.getReferenceById(item.getTrackId()))
            .trackOrder(item.getTrackOrder())
            .build())
        .toList();
    playlistTrackRepository.saveAll(reordered);
}
```

### 중복 추가 체크 (3.4)
```java
PlaylistTrackId id = new PlaylistTrackId(playlistId, request.getTrackId());
if (playlistTrackRepository.existsById(id)) {
    throw new BusinessException(BUSINESS_ERROR.DATA_INTEGRITY_VIOLATION); // 409
}
```

### trackCount 계산
```java
// PlaylistTrackRepository에 추가
long countByIdPlaylistId(Long playlistId);
```

### 썸네일 업로드
```java
// thumbnail 파일이 있을 때만 업로드 (null/empty이면 기존값 유지)
String thumbnailUrl = (thumbnailFile != null && !thumbnailFile.isEmpty())
    ? storageService.store(thumbnailFile, "playlists/thumbnails")
    : existingThumbnail;
```

### SecurityConfig 추가 필요
```java
// PlaylistController는 .requestMatchers("/api/**").authenticated() catch-all로 처리됨
// 별도 규칙 불필요 — 구독자 체크는 Service 레이어에서 수행
```

[DTO SPEC]

PlaylistResponse (3.1, 3.5 응답):
```java
record PlaylistResponse(Long id, String title, String description, String thumbnail,
                        int trackCount, LocalDateTime createdAt) {}
```

PlaylistListItemResponse (3.2 응답):
```java
record PlaylistListItemResponse(Long id, String title, String thumbnail,
                                int trackCount, LocalDateTime createdAt) {}
```

PlaylistDetailResponse (3.3 응답):
```java
record PlaylistDetailResponse(Long id, String title, String description, String thumbnail,
                               List<PlaylistTrackItemResponse> tracks,
                               LocalDateTime createdAt, LocalDateTime updatedAt) {}
```

PlaylistTrackItemResponse (트랙 항목):
```java
record PlaylistTrackItemResponse(int trackOrder, Long trackId, String title,
                                 Integer bpm, String tonality) {}
```

PlaylistCreateRequest (3.1):
```java
// @ModelAttribute (multipart)
record PlaylistCreateRequest(String title, String description) {}
// thumbnail은 @RequestPart MultipartFile로 별도 받기
```

PlaylistUpdateRequest (3.5):
```java
// @ModelAttribute (multipart)
record PlaylistUpdateRequest(String title, String description) {}
```

PlaylistAddTrackRequest (3.4):
```java
record PlaylistAddTrackRequest(Long trackId) {}
```

PlaylistReorderRequest (3.6):
```java
record PlaylistReorderRequest(List<PlaylistTrackOrderItem> tracks) {}
record PlaylistTrackOrderItem(Long trackId, int trackOrder) {}
```

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260221-ATS-018-summary.md :
  - 구현 완료 API 목록
  - 생성된 파일 목록
Agent-facing -> deliverables/agent/WI-20260221-ATS-018-evidence-pack.md :
  - 파일별 구현 내용
  - 특이 사항 (N+1 방지, 트랜잭션 처리 등)
Handoff Packet -> deliverables/agent/WI-20260221-ATS-018-handoff.md :
  - This packet

[TRACEABILITY REQUIREMENTS]
Evidence: 생성/수정된 모든 파일 목록 + 주요 메서드 라인 번호
Tests: WI-019 (re)에서 작성
Rollback: git diff로 추적 가능
