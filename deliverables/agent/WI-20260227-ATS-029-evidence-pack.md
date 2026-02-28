# WI-20260227-ATS-029 Evidence Pack — cr-A: Track·License·Tag·Playlist·PlayHistory

## cr-A 검토 결과

### 1.x Track

| API | 판정 | 발견 이슈 | 파일:라인 |
|-----|------|----------|---------|
| 1.1 POST /api/tracks | ✅ | - | - |
| 1.2 GET /api/tracks | ❌ | CR-A-001: `TrackSpecification.root.join("trackTags")` — Track 엔티티에 매핑 없음 → 태그 필터 시 런타임 크래시 | `TrackSpecification.java:40`, `Track.java` |
| 1.3 GET /api/tracks/{trackId} | ✅ | JOIN FETCH로 tags 정상 로딩 | `TrackTagRepository.java:19-20` |
| 1.4 GET /api/tracks/{trackId}/stream | ⚠️ | CR-A-010: fallback 리소스 존재 미검증 | `TrackService.java:116-131` |
| 1.5 GET /api/tracks/{trackId}/download | ❌ | CR-A-003: `downloadPerDay=-1`(무제한) → `todayCount >= -1` 항상 true → 무제한 플랜 차단 | `DownloadService.java:46` |
| 1.6 PUT /api/tracks/{trackId} | ✅ | 부분 수정, 태그 교체, 권한 체크 정상 | `TrackService.java:133-161` |
| 1.7 DELETE /api/tracks/{trackId} | ❌ | CR-A-004: 소프트삭제 시 track_tags 물리 삭제 누락 (RULE-TRACK-003) | `TrackService.java:163-167` |

### 2.x Tag

| API | 판정 | 발견 이슈 | 파일:라인 |
|-----|------|----------|---------|
| 2.1 POST /api/tags | ✅ | ADMIN 권한, 중복 체크, enum 검증 | `TagService.java:25-37` |
| 2.2 GET /api/tags | ⚠️ | CR-A-007: `ResponseDTO` 미사용, 원시 `List<TagResponse>` 반환 | `TagController.java:37-40` |
| 2.3 PUT /api/tags/{tagId} | ✅ | 이름 변경 중복 체크 | `TagService.java:46-57` |
| 2.4 DELETE /api/tags/{tagId} | ✅ | `trackTagRepository.deleteAllByTag()` 선행 호출 (RULE-TAG-002) | `TagService.java:59-65` |

### 3.x Playlist

| API | 판정 | 발견 이슈 | 파일:라인 |
|-----|------|----------|---------|
| 3.1 POST /api/playlists | ✅ | 구독 체크, is_active=true | `PlaylistService.java:37-56` |
| 3.2 GET /api/playlists | ⚠️ | CR-A-006: trackCount 루프 내 N+1 쿼리 | `PlaylistService.java:66-71` |
| 3.3 GET /api/playlists/{id} | ✅ | Owner 체크, @EntityGraph로 tracks 로딩 | `PlaylistTrackRepository.java:12-13` |
| 3.4 POST /api/playlists/{id}/tracks | ✅ | 중복 체크 existsById(), trackOrder = count | `PlaylistService.java:92-118` |
| 3.5 PUT /api/playlists/{id} | ✅ | Owner 체크, 부분 수정 | `PlaylistService.java:122-138` |
| 3.6 PUT /api/playlists/{id}/tracks | ✅ | Delete-all + re-insert (CR-A-012 제안) | `PlaylistService.java:142-162` |
| 3.7 DELETE /api/playlists/{id}/tracks/{trackId} | ✅ | Owner 체크, 404 처리 | `PlaylistService.java:166-177` |
| 3.8 DELETE /api/playlists/{id} | ⚠️ | CR-A-008: 소프트삭제만, playlist_tracks 고아 잔존 | `PlaylistService.java:181-186` |

### 4.x PlayHistory

| API | 판정 | 발견 이슈 | 파일:라인 |
|-----|------|----------|---------|
| 4.1 POST /api/play-histories | ✅ | @Modifying으로 play_count 원자적 증가 | `TrackRepository.java:12-14` |
| 4.2 GET /api/play-histories | ✅ | @EntityGraph, 페이지네이션, DESC 정렬 | `PlayHistoryRepository.java:14-15` |
| 4.3 DELETE /api/play-histories | ✅ | 빈 list = 전체 삭제, 소유자 필터 | `PlayHistoryService.java:67-77` |

### 7.x License

| API | 판정 | 발견 이슈 | 파일:라인 |
|-----|------|----------|---------|
| 7.1 GET /api/licenses/me | ❌ | CR-A-005: @EntityGraph 없음 → N+1 (track LAZY) | `LicenseRepository.java:18` |
| 7.2 GET /api/users/{userId}/licenses | ❌ | CR-A-005: 동일 N+1 | `LicenseRepository.java:20` |
| 7.3 GET /api/licenses/{licenseId} | ⚠️ | @EntityGraph 없음, track+user LAZY 로딩 | `LicenseRepository.java:16` |
| 7.4 GET /api/users/{userId}/licenses/{licenseId} | ⚠️ | 동일 | `LicenseService.java:48-52` |

---

## 레이어별 코딩 표준 준수

| 파일 | @Transactional(readOnly) 클래스 | DTO 분리 | @RequiredArgsConstructor |
|------|--------------------------------|---------|------------------------|
| TrackService.java | ✅ | ✅ | ✅ |
| DownloadService.java | ❌ CR-A-002 | ✅ | ✅ |
| LicenseService.java | ✅ | ✅ | ✅ |
| TagService.java | ✅ | ✅ | ✅ |
| PlaylistService.java | ✅ | ✅ | ✅ |
| PlayHistoryService.java | ✅ | ✅ | ✅ |
| 모든 Controller | N/A | ✅ | ✅ |
| 모든 Entity | N/A | N/A | @Setter 없음 ✅ |

---

## 발견 이슈 종합 목록

| # | 심각도 | 파일:라인 | 이슈 | 권장 조치 |
|---|--------|---------|------|---------|
| CR-A-001 | ❌ CRITICAL | `TrackSpecification.java:40` | `root.join("trackTags")` — Track 엔티티에 매핑 없음, 태그 필터 시 런타임 크래시 | Track 엔티티에 `@OneToMany(mappedBy="track") List<TrackTag> trackTags` 추가 |
| CR-A-002 | ❌ MAJOR | `DownloadService.java:19-21` | 클래스 레벨 `@Transactional(readOnly=true)` 누락 | 어노테이션 추가 |
| CR-A-003 | ❌ MAJOR | `DownloadService.java:46` | `downloadPerDay=-1`(무제한) → 항상 한도 초과 판정 | `if (limit != -1 && todayCount >= limit)` 가드 추가 |
| CR-A-004 | ❌ MAJOR | `TrackService.java:163-167` | 소프트삭제 시 track_tags 물리 삭제 누락 | `trackTagRepository.deleteAllByTrack(track)` 선행 호출 |
| CR-A-005 | ❌ MAJOR | `LicenseRepository.java:18,20` | 라이선스 Page 쿼리 @EntityGraph 없음 → N+1 | `@EntityGraph(attributePaths="track")` 추가 |
| CR-A-006 | ❌ MAJOR | `PlaylistService.java:66-71` | trackCount 루프 내 N+1 | 배치 count 쿼리로 교체 |
| CR-A-007 | ⚠️ MINOR | `TagController.java:37-40` | 응답 ResponseDTO 미사용 | `ResponseDTO.dataList`로 래핑 |
| CR-A-008 | ⚠️ MINOR | `PlaylistService.java:181-186` | 소프트삭제 시 playlist_tracks 고아 잔존 | 삭제 또는 의도 문서화 |
| CR-A-009 | ⚠️ MINOR | `TrackResponse.java:16` | 내부 audioFile 경로 노출 | 제거 또는 URL로 교체 |
| CR-A-010 | ⚠️ MINOR | `TrackService.java:116-131` | fallback 리소스 존재 미검증 | `!resource.exists()` 체크 추가 |
| CR-A-011 | 📋 제안 | `TagCreateRequest.java` 등 | JSON body DTO Lombok class → record 전환 가능 | 스타일 통일 |
| CR-A-012 | 📋 제안 | `PlaylistService.java:149-161` | reorder delete+re-insert → UPDATE 기반 가능 | 추후 최적화 |
| CR-A-013 | 📋 제안 | `TrackService.java:186-189` | 태그 개별 조회 → `findAllById(tagIds)` 배치 | 추후 최적화 |
