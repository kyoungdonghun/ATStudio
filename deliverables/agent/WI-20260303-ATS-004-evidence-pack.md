[EVIDENCE PACK — WI-20260303-ATS-004]

## Review Status
PASS — CRITICAL 0, MAJOR 0, MINOR 0, SUGGESTION 0
Date: 2026-03-03

## Checkpoint Results

| # | Checkpoint | Status | Evidence |
|---|-----------|--------|----------|
| 1 | PLAYLIST_LIMIT_EXCEEDED HttpStatus = CONFLICT(409) | PASS | `BUSINESS_ERROR.java:129` |
| 2 | clientMessage 사용자 친화적 | PASS | `BUSINESS_ERROR.java:130` |
| 3 | Enum 패턴 일관성 (3-arg constructor) | PASS | ATStudio Domain section 배치 |
| 4 | countByUserAndIsActiveTrue is_active=false 제외 | PASS | `PlaylistRepository.java:13` Spring Data JPA 파생 |
| 5 | 반환 타입 적합성 (int, max 3) | PASS | `PlaylistRepository.java:13` |
| 6 | 카운트 체크 위치 (validateSubscriber 직후, storageService.store 전) | PASS | `PlaylistService.java:44,46,50` |
| 7 | 조건 >= 3 | PASS | `PlaylistService.java:46` |
| 8 | BusinessException 생성 패턴 | PASS | `PlaylistService.java:47` |
| 9 | 제한 초과 테스트 (count=3 → throw) | PASS | `PlaylistServiceTest.java:69-84` |
| 10 | 경계값 테스트 (count=2 → 성공) | PASS | `PlaylistServiceTest.java:87-105` |
| 11 | BDDMockito given-when-then 패턴 | PASS | 전체 신규 테스트 |
| 12 | @DisplayName 명확성 | PASS | 메서드명+시나리오+결과 형식 |

## Code Review Details

### BUSINESS_ERROR.java (L128-131)
```java
PLAYLIST_LIMIT_EXCEEDED(
        HttpStatus.CONFLICT,
        "플레이리스트는 최대 3개까지 생성할 수 있습니다.",
        "활성 플레이리스트 3개 초과 시도."),
```
SUBSCRIPTION_ALREADY_EXISTS 다음, Auth 섹션 앞에 배치. 논리적 위치.

### PlaylistRepository.java (L13)
```java
int countByUserAndIsActiveTrue(User user);
```
Spring Data JPA: `SELECT COUNT(*) FROM playlist WHERE user_id = ?1 AND is_active = true`
반환 타입 int — 최대값 3이므로 overflow 없음.

### PlaylistService.java (L44-48)
```java
User user = validateSubscriber(userDetails);           // L44
if (playlistRepository.countByUserAndIsActiveTrue(user) >= 3) {  // L46
    throw new BusinessException(BUSINESS_ERROR.PLAYLIST_LIMIT_EXCEEDED);  // L47
}                                                      // L48
```
- 실행 순서: 구독 검증 → 개수 체크 → 썸네일 I/O → 저장
- 제한 초과 시 불필요한 I/O 방지

### PlaylistServiceTest.java (L69-105)
- `createPlaylist_limitExceeded_throws`: count=3 mock → PLAYLIST_LIMIT_EXCEEDED 예외 검증
- `createPlaylist_atLimit_succeeds`: count=2 mock → 정상 생성 검증
- 기존 `createPlaylist_success` 테스트와 공존 (int 기본값 0 → `0 >= 3` false → 별도 mock 불필요)

## Issues
없음

## Verdict Rationale
4개 파일 변경분 모두 비즈니스 요구사항, 코드 패턴, 테스트 품질 기준 충족.
CRITICAL 0, MAJOR 0 → PASS 기준 달성.
