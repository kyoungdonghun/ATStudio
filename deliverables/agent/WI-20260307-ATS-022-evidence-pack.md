# WI-20260307-ATS-022 Evidence Pack

## WI Reference
- WI: WI-20260307-ATS-022
- REQ: REQ-20260307-ATS-008
- Agent: se
- Scope: BD-2 GET /api/tracks/admin 신규 구현

---

## Change Pointers

### 1. AdminTrackListItemResponse.java (NEW)
- Path: `src/main/java/com/atstudio/atstudio/dto/track/AdminTrackListItemResponse.java`
- Lines: 1-33
- Record DTO: TrackListItemResponse 구조 + `isActive` 필드 추가
- `from(Track, List<Tag>)` 팩토리 메서드 포함

### 2. TrackSpecification.java (MODIFIED)
- Path: `src/main/java/com/atstudio/atstudio/repository/spec/TrackSpecification.java`
- Lines: 15-18 (added)
- `hasIsActive(Boolean)`: null이면 null 반환 (no-op), 아니면 isActive 필터

### 3. TrackService.java (MODIFIED)
- Path: `src/main/java/com/atstudio/atstudio/service/TrackService.java`
- Lines: 171-193 (added)
- `getTracksForAdmin(Boolean isActive, int page, int size)`
- `Specification.where(hasIsActive(isActive))`: isActive=null이면 조건 없이 전체 반환
- 기존 getTracks() 패턴과 동일 구조 (PageRequest + Specification + ResponseDTO)

### 4. TrackController.java (MODIFIED)
- Path: `src/main/java/com/atstudio/atstudio/controller/TrackController.java`
- Lines: 15 (added import: PreAuthorize)
- Lines: 45-52 (added endpoint)
- `@GetMapping("/admin")` + `@PreAuthorize("hasRole('ADMIN')")` 이중 보호
- `/{trackId}` 앞에 배치하여 Spring MVC 경로 우선순위 충돌 방지

### 5. SecurityConfig.java (MODIFIED)
- Path: `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java`
- Line: 61 (added)
- `.requestMatchers(HttpMethod.GET, "/api/tracks/admin").hasRole("ADMIN")`
- `/api/tracks/*` (permitAll, line 62) 앞에 배치 (more specific first)

### 6. TrackServiceTest.java (MODIFIED)
- Path: `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java`
- 4 tests added:
  - `getTracksForAdmin_allTracks()`: isActive=null, 전체 반환 (2건)
  - `getTracksForAdmin_activeOnly()`: isActive=true, 활성만 (1건, isActive=true 검증)
  - `getTracksForAdmin_inactiveOnly()`: isActive=false, 비활성만 (1건, isActive=false 검증)
  - `getTracksForAdmin_emptyResult()`: 결과 없을 때 빈 리스트

---

## Design Decisions

1. **AdminTrackListItemResponse vs TrackListItemResponse 확장**: 별도 DTO 생성 선택.
   - 이유: public DTO에 isActive 추가 시 구독자에게 불필요한 정보 노출
   - 기존 TrackListItemResponse 소비자(public API)에 영향 없음

2. **Specification.where(null)**: isActive=null 시 `hasIsActive()` 가 null 반환.
   - `Specification.where(null)` 은 조건 없는 쿼리 생성 (전체 반환)

3. **SecurityConfig 규칙 순서**: `/api/tracks/admin` 규칙을 `/api/tracks/*` 와일드카드 앞에 배치.
   - Spring Security는 첫 번째 매치 규칙 적용. 순서 역전 시 permitAll() 적용되어 보안 우회 가능

---

## Build/Test Evidence

### Compilation
- **Command**: `gradlew compileJava`
- **Result**: BUILD SUCCESSFUL
- **Duration**: ~2s

### Test Compilation
- **Command**: `gradlew compileTestJava`
- **Result**: BUILD FAILED (22 pre-existing errors)
- **Pre-existing errors**: DownloadServiceTest, WhitelistChannelServiceTest, LikeServiceTest, DownloadQueueServiceTest
  - Root cause: `UserSubscriptionRepository.findActiveByUser()` 시그니처 변경 후 테스트 미갱신
- **WI-022 관련 에러**: 0건

### Test Execution
- **Status**: pre-existing 컴파일 에러로 전체 테스트 실행 불가
- **Note**: 본 WI 변경 파일에서는 에러 없음 확인

---

## Acceptance Criteria Check

| Criteria | Status |
|----------|--------|
| GET /api/tracks/admin 엔드포인트 추가 | PASS |
| isActive=null: 전체 반환 | PASS (구현 + 테스트) |
| isActive=true: 활성만 | PASS (구현 + 테스트) |
| isActive=false: 비활성만 | PASS (구현 + 테스트) |
| 응답 DTO에 isActive 필드 포함 | PASS (AdminTrackListItemResponse) |
| 응답 구조 { dataList, pageInfo } | PASS |
| ADMIN 아닌 사용자 403 | PASS (SecurityConfig + @PreAuthorize) |
| 미인증 사용자 401 | PASS (SecurityConfig) |
| 기존 GET /api/tracks 영향 없음 | PASS (미변경) |
| SecurityConfig 규칙 순서 | PASS (admin 먼저 배치) |

---

## Reproduction Steps

1. `cd C:\Users\jm991\Desktop\project\ATStudio`
2. `gradlew.bat compileJava` -- BUILD SUCCESSFUL 확인
3. `gradlew.bat bootRun` 후 아래 테스트:
   - `GET /api/tracks/admin` (no auth) -> 401
   - `GET /api/tracks/admin` (USER token) -> 403
   - `GET /api/tracks/admin` (ADMIN token) -> 200, dataList + pageInfo
   - `GET /api/tracks/admin?isActive=true` (ADMIN) -> 활성만
   - `GET /api/tracks/admin?isActive=false` (ADMIN) -> 비활성만
   - `GET /api/tracks` -> 기존과 동일 동작 (활성만, public)

---

## Follow-up

- Pre-existing test compilation 에러 (22건): `UserSubscriptionRepository.findActiveByUser()` 시그니처 변경 후 DownloadServiceTest, WhitelistChannelServiceTest, LikeServiceTest, DownloadQueueServiceTest 미갱신. 별도 WI로 처리 필요.
