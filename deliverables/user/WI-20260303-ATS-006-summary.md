[WI-006 CR SUMMARY]
WI ID: WI-20260303-ATS-006
REQ: REQ-20260303-ATS-003
Reviewer: cr
Date: 2026-03-04

## Verdict
Status: CONDITIONALLY PASS
CRITICAL: 0
MAJOR: 2
MINOR: 3
SUGGESTION: 1

## Issues

### MAJOR

| ID | Description | File | Line |
|----|-------------|------|------|
| M-1 | N+1 Query: @EntityGraph이 track만 fetch, track.user 미포함 → getAlbum/reorderTracks 호출마다 User SELECT N번 발생 | AlbumTrackRepository.java | 16 |
| M-2 | N+1 Query: getAlbums()에서 카운트를 위해 전체 AlbumTrack 엔티티 fetch → countByAlbum() 사용 필요 (AlbumService:65,99,125 3곳) | AlbumService.java | 65,99,125 |

### MINOR

| ID | Description | File |
|----|-------------|------|
| m-1 | AlbumCreateRequest/UpdateRequest class 사용 (record 권장이나 @ModelAttribute multipart에서는 허용) | AlbumCreateRequest.java, AlbumUpdateRequest.java |
| m-2 | addTrack() 다음 순서 계산 시 전체 엔티티 fetch (M-2 fix로 동시 해결) | AlbumService.java:125 |
| m-3 | addTrack() 비활성 트랙 RESOURCE_NOT_FOUND 테스트 케이스 누락 | AlbumServiceTest.java |

### SUGGESTION

| ID | Description | File |
|----|-------------|------|
| S-1 | updateAlbum() 썸네일 초기화 불가 (null skip 방식 한계) | AlbumService.java:93-97 |

## Positive Observations
- SecurityConfig 권한 분리 정확 (GET permitAll, write ADMIN)
- @Transactional 패턴 완벽 준수
- Entity/DTO 분리 철저
- 복합PK(AlbumTrackId) 구현 정확
- 소프트 삭제 로직 정상
- API spec Section 15 ↔ Controller 8개 100% 일치

## Approval
MAJOR 2건(M-1, M-2) 수정 후 머지 승인. 모두 N+1 쿼리 성능 이슈이며 수정이 명확함.
