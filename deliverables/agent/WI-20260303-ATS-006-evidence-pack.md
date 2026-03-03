[EVIDENCE PACK — WI-20260303-ATS-006]

## Review Status
CONDITIONALLY PASS — CRITICAL 0, MAJOR 2, MINOR 3, SUGGESTION 1
Date: 2026-03-04

## MAJOR Issues

### M-1: N+1 Query — @EntityGraph track.user 미포함

**파일**: `AlbumTrackRepository.java:16`, `AlbumTrackItemResponse.java:18-19`

**근거**:
- `AlbumTrackItemResponse.from()`: `albumTrack.getTrack().getUser().getNickname()` 호출
- `Track.java:43`: `@ManyToOne(fetch = FetchType.LAZY) private User user;`
- `AlbumTrackRepository.java:16`: `@EntityGraph(attributePaths = "track")` — user 미포함
- 결과: 트랙 N개인 앨범 조회 시 User SELECT N번 추가 발생

**영향 범위**: `getAlbum()`, `addTrack()` 반환, `reorderTracks()` 반환

**수정**:
```java
// AlbumTrackRepository.java:16
@EntityGraph(attributePaths = {"track", "track.user"})
List<AlbumTrack> findAllByAlbumOrderByTrackOrder(Album album);
```

---

### M-2: N+1 Query — 카운트 목적으로 전체 엔티티 fetch

**파일**: `AlbumService.java:65,99,125`

**근거**:
- L65: `albumTrackRepository.findAllByAlbumOrderByTrackOrder(album).size()` — getAlbums() stream 내부
- L99: 동일 패턴 (updateAlbum)
- L125: 동일 패턴 (addTrack nextOrder 계산)
- M 개 앨범의 경우 M번의 전체 엔티티 fetch 쿼리 발생

**수정**:
```java
// AlbumTrackRepository.java — 추가
long countByAlbum(Album album);

// AlbumService.java:65 대체
long trackCount = albumTrackRepository.countByAlbum(album);
```

## MINOR Issues

### m-1: Request DTO class vs record
- 파일: `AlbumCreateRequest.java`, `AlbumUpdateRequest.java`
- @ModelAttribute multipart 바인딩에서 mutable setter 필요 — class 사용 허용

### m-2: addTrack() nextOrder 계산
- 파일: `AlbumService.java:125`
- M-2 fix(countByAlbum)로 동시 해결됨

### m-3: 비활성 트랙 예외 테스트 누락
- 파일: `AlbumServiceTest.java`
- AlbumService.java:117-119: `.filter(Track::isActive)` 분기가 테스트 미커버

## SUGGESTION

### S-1: 썸네일 초기화 불가
- 파일: `AlbumService.java:93-97`, `Album.java:46`
- `Album.update()`에서 null 필드 skip → 한번 설정된 thumbnail 제거 불가
- 비즈니스 요구사항에 따라 별도 결정 필요

## Architecture Compliance

| 항목 | 결과 | 근거 |
|------|------|------|
| @Transactional(readOnly=true) 클래스 레벨 | PASS | AlbumService.java:25 |
| mutating 메서드만 @Transactional override | PASS | L37,87,105,113,141,154 |
| BaseEntity 상속 | PASS | Album.java:16 |
| Entity/DTO 분리 | PASS | Controller 전체 DTO 반환 |
| @Embeddable + @EmbeddedId | PASS | AlbumTrackId.java:9,14 |
| equals/hashCode + Serializable | PASS | AlbumTrackId.java:13-14 |
| FetchType.LAZY 연관 | PASS | Album.java:31,39 / AlbumTrack.java:18,23 |

## API Spec Alignment (Section 15)

| API | Method | URL | Status | 결과 |
|-----|--------|-----|--------|------|
| 15.1 Create Album | POST | /api/albums | 201 | PASS |
| 15.2 List Albums | GET | /api/albums | 200 | PASS |
| 15.3 Get Album | GET | /api/albums/{id} | 200 | PASS |
| 15.4 Update Album | PUT | /api/albums/{id} | 200 | PASS |
| 15.5 Delete Album | DELETE | /api/albums/{id} | 204 | PASS |
| 15.6 Add Track | POST | /api/albums/{id}/tracks | 200 | PASS |
| 15.7 Remove Track | DELETE | /api/albums/{id}/tracks/{trackId} | 204 | PASS |
| 15.8 Reorder Tracks | PUT | /api/albums/{id}/tracks | 200 | PASS |

## Security Review

| 항목 | 결과 | 근거 |
|------|------|------|
| GET permitAll | PASS | SecurityConfig.java:90 |
| POST/PUT/DELETE ADMIN | PASS | SecurityConfig.java:91-96 |
| Entity 미노출 | PASS | 전 엔드포인트 DTO 반환 |
| 입력 검증 | PASS | @Valid, @NotBlank, @Size, @NotNull 적용 |
