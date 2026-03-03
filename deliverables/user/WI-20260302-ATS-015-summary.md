[WI-015 CR SUMMARY]
WI ID: WI-20260302-ATS-015
REQ: REQ-20260302-ATS-012
Reviewer: cr
Date: 2026-03-03
Scope: WI-010~012 (N+1/Cascade/상태전이)

## Verdict

Status: PASS WITH MINOR
CRITICAL: 0
MAJOR: 0
MINOR: 2
SUGGESTION: 1

## Confirmed Fixes (All Correct)

- M-3 LicenseRepository @EntityGraph(attributePaths="track") — License.track 필드명 정확. 두 메서드 모두 적용
- M-4 PlaylistService getMyPlaylists() batch count — 빈 리스트 guard (Collections.emptyMap()) 정확. JPQL GROUP BY 구문 올바름. Object[] 캐스팅 안전
- M-5 TrackService.deleteTrack() — deleteAllByTrack()이 deactivate() 앞에 위치. @RequiredArgsConstructor 주입 정확
- M-10 PlaylistService.deletePlaylist() — deleteAllByIdPlaylistId()가 deactivate() 앞에 위치. removeTrack()과 혼동 없음
- M-6 findTopByUserOrderByCreatedAtDesc — Spring Data JPA 명명 규칙 정확. findByUser 잔재 없음 (전체 소스 grep 확인)
- M-7 CompanyCertification 상태기계 — 전이 매트릭스 완전. default -> false로 APPROVED/REJECTED 모든 전이 차단
- M-8 Question 상태기계 — 완전 exhaustive switch(default 없음). CLOSED 터미널, OPEN→RESOLVED 차단 확인

## MINOR Issues

| ID | File | Description |
|----|------|-------------|
| CR-M-001 | CompanyCertificationTest.java | APPROVED 상태에서 APPROVED/REVISION_REQUESTED/REJECTED 전이 무효 테스트 누락 (APPROVED→PENDING만 테스트됨) |
| CR-M-002 | CompanyCertificationTest.java | REJECTED 상태에서 APPROVED/REVISION_REQUESTED/REJECTED 전이 무효 테스트 누락 (REJECTED→PENDING만 테스트됨) |

## SUGGESTION

| ID | File | Line | Description |
|----|------|------|-------------|
| CR-S-001 | PlaylistService.java | 81 | `Long.intValue()` 다운캐스트 — 트랙 수가 Integer.MAX_VALUE 초과 시 오버플로. 현실적 가능성 극히 낮으나 주석으로 의도 명시 권고 |

## QuestionTest 특이사항 ✅

OPEN→RESOLVED가 INVALID_STATE_TRANSITION으로 명시적 테스트됨 (QuestionTest.java:111). 우수한 커버리지.

## Approval

차단 이슈 없음. WI-010~012 모든 수정 정확히 구현됨. 머지 승인.
