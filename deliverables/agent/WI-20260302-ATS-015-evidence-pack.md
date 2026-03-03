[EVIDENCE PACK — WI-20260302-ATS-015]

## Review Status
PASS WITH MINOR — CRITICAL 0, MAJOR 0, MINOR 2, SUGGESTION 1
Date: 2026-03-03

## Files Reviewed

| # | File | Verdict |
|---|------|---------|
| 1 | LicenseRepository.java | PASS |
| 2 | PlaylistTrackRepository.java | PASS |
| 3 | PlaylistService.java (getMyPlaylists + deletePlaylist) | PASS (SUGGESTION) |
| 4 | TrackService.java | PASS |
| 5 | CompanyCertificationRepository.java | PASS |
| 6 | CompanyCertification.java | PASS |
| 7 | Question.java | PASS |
| 8 | CompanyCertificationTest.java | PASS WITH MINOR |
| 9 | QuestionTest.java | PASS |

## Detailed Findings

### LicenseRepository.java (M-3)
- L19: `@EntityGraph(attributePaths = "track")` → findAllByUser ✓
- L22: `@EntityGraph(attributePaths = "track")` → findAllByUser_Id ✓
- License 엔티티 `track` 필드명 확인 (License.java L29-31: `@ManyToOne ... private Track track`) ✓

### PlaylistTrackRepository.java (M-4)
- L19-20: JPQL `SELECT pt.id.playlistId, COUNT(pt) FROM PlaylistTrack pt WHERE pt.id.playlistId IN :playlistIds GROUP BY pt.id.playlistId` — 구문 정확
- Embedded ID path expression `pt.id.playlistId` — PlaylistTrackId 구조와 일치

### PlaylistService.java (M-4 + M-10)
- L69-82 getMyPlaylists():
  - L71-72: `playlistIds.isEmpty() ? Collections.emptyMap()` — JPA IN () 빈 리스트 문제 예방 ✓
  - L73-78: `Object[]` → `Map<Long, Long>` 캐스팅 안전 (JPQL COUNT → Long, playlistId → Long) ✓
  - L81: `countMap.getOrDefault(p.getId(), 0L).intValue()` — 실용적으로 안전
  - **CR-S-001 (SUGGESTION)**: Long.intValue() 다운캐스트 — 의도 주석 권고
- L193-197 deletePlaylist():
  - L196: `playlistTrackRepository.deleteAllByIdPlaylistId(playlistId)` — deactivate() 전 호출 ✓
  - L197: `playlist.deactivate()` — FK cleanup 후 소프트삭제 ✓

### TrackService.java (M-5)
- L167: `trackTagRepository.deleteAllByTrack(track)` — deactivate() 전 ✓
- L168: `track.deactivate()` ✓
- L42: `private final TrackTagRepository trackTagRepository` (@RequiredArgsConstructor) ✓

### CompanyCertificationRepository.java (M-6)
- L14: `findTopByUserOrderByCreatedAtDesc(User user)` — Spring Data JPA 명명 규칙 정확 ✓
- CompanyCertificationService.java L75: 호출부 업데이트 ✓
- findByUser 잔재 없음 (전체 소스 grep 확인) ✓

### CompanyCertification.java (M-7)
- L53-64 validateTransition() 전이 매트릭스:

| From → To | PENDING | APPROVED | REVISION_REQUESTED | REJECTED |
|-----------|---------|----------|--------------------|----------|
| PENDING | ✗ | ✓ | ✓ | ✓ |
| APPROVED | ✗ | ✗ | ✗ | ✗ (default) |
| REVISION_REQUESTED | ✓ | ✗ | ✗ | ✗ |
| REJECTED | ✗ | ✗ | ✗ | ✗ (default) |

- `default -> false` — APPROVED/REJECTED 및 신규 enum 값 안전하게 차단 ✓
- 자기 전이(PENDING→PENDING 등): 모두 false ✓

### Question.java (M-8)
- L51-61 validateTransition() 전이 매트릭스:

| From → To | OPEN | IN_PROGRESS | RESOLVED | CLOSED |
|-----------|------|-------------|----------|--------|
| OPEN | ✗ | ✓ | ✗ | ✓ |
| IN_PROGRESS | ✗ | ✗ | ✓ | ✓ |
| RESOLVED | ✗ | ✗ | ✗ | ✓ |
| CLOSED | ✗ | ✗ | ✗ | ✗ |

- Fully exhaustive switch (default 없음, 컴파일러 보장) ✓
- OPEN→RESOLVED: false (api-spec 미허용 전이 차단) ✓
- CLOSED 터미널: `case CLOSED -> false` ✓

### Test Coverage

**CompanyCertificationTest.java**

Valid transitions: PENDING→APPROVED/REVISION_REQUESTED/REJECTED, REVISION_REQUESTED→PENDING (4건) ✓

Invalid transitions tested:
- APPROVED→PENDING ✓, REJECTED→PENDING ✓
- REVISION_REQUESTED→APPROVED/REVISION_REQUESTED/REJECTED (parameterized 3건) ✓

**CR-M-001 (MINOR)**: APPROVED 상태에서 APPROVED/REVISION_REQUESTED/REJECTED 전이 미테스트
**CR-M-002 (MINOR)**: REJECTED 상태에서 APPROVED/REVISION_REQUESTED/REJECTED 전이 미테스트
(실제 로직은 `default -> false`로 올바르게 차단됨)

**QuestionTest.java**

Valid: OPEN→IN_PROGRESS/CLOSED, IN_PROGRESS→RESOLVED/CLOSED, RESOLVED→CLOSED (5건) ✓
Invalid: CLOSED→OPEN/IN_PROGRESS, RESOLVED→OPEN, **OPEN→RESOLVED**, IN_PROGRESS→OPEN (5건) ✓
특히 OPEN→RESOLVED 명시적 테스트 (L111) — 우수한 커버리지 ✓

## Review Checklist
| # | Checkpoint | Result |
|---|------------|--------|
| 1 | @EntityGraph attributePaths="track" 필드명 정확 | PASS |
| 2 | @EntityGraph 두 메서드 모두 적용 | PASS |
| 3 | countByPlaylistIdIn JPQL + 빈 리스트 guard | PASS |
| 4 | Object[] 캐스팅 안전성 | PASS |
| 5 | deleteTrack: deleteAllByTrack before deactivate | PASS |
| 6 | trackTagRepository @RequiredArgsConstructor 주입 | PASS |
| 7 | deletePlaylist: deleteAllByIdPlaylistId before deactivate | PASS |
| 8 | findTopByUserOrderByCreatedAtDesc 명명 정확 | PASS |
| 9 | findByUser 잔재 없음 | PASS |
| 10 | CompanyCertification 전이 매트릭스 완전성 | PASS |
| 11 | Question 전이 매트릭스 완전성 (exhaustive switch) | PASS |
| 12 | OPEN→RESOLVED 차단 확인 | PASS |
| 13 | CompanyCertificationTest 커버리지 | MINOR |
| 14 | QuestionTest OPEN→RESOLVED 무효 테스트 | PASS |

## Conclusion
차단 이슈 없음. WI-010~012 수정 정확. MINOR 2건은 CompanyCertificationTest 추가 테스트 케이스 권고. PASS.
