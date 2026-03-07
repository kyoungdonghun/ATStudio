# WI-20260307-ATS-022 Summary

## What Changed
BD-2 CRITICAL 해결: `GET /api/tracks/admin` 관리자 전용 엔드포인트 신규 구현.

- 관리자가 비활성 트랙을 포함한 전체 트랙 목록을 조회 가능
- `isActive` optional 필터 지원 (null=전체, true=활성만, false=비활성만)
- 응답에 `isActive` 필드 포함하는 `AdminTrackListItemResponse` DTO 신규 생성
- SecurityConfig에 `/api/tracks/admin` ADMIN 전용 규칙 추가 (와일드카드 규칙보다 먼저 배치)

## Files Changed

| File | Change |
|------|--------|
| `TrackController.java:45-52` | `GET /admin` 엔드포인트 추가 |
| `TrackService.java:171-193` | `getTracksForAdmin()` 메서드 추가 |
| `TrackSpecification.java:15-18` | `hasIsActive(Boolean)` 스펙 추가 |
| `AdminTrackListItemResponse.java` | 신규 DTO (isActive 필드 포함) |
| `SecurityConfig.java:61` | `/api/tracks/admin` hasRole("ADMIN") 규칙 추가 |
| `TrackServiceTest.java` | 4건 테스트 추가 |

## Risk
- LOW: 기존 `GET /api/tracks` (public) 로직 변경 없음
- SecurityConfig 규칭 순서: `/api/tracks/admin` (ADMIN) 이 `/api/tracks/*` (permitAll) 앞에 배치되어 정상 작동

## Verification
- `gradlew compileJava`: BUILD SUCCESSFUL
- `gradlew compileTestJava`: 기존 22 errors (pre-existing, 본 WI 변경과 무관)
- 신규 테스트 4건: 컴파일 정상 (pre-existing 에러로 전체 테스트 실행 불가)
