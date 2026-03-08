[WI SUMMARY]
WI ID: WI-20260307-ATS-025
REQ: REQ-20260307-ATS-009
Track: 2-A (api-spec §1~4, §10~11 ↔ 백엔드 코드)
Status: Completed ✅

---

## Overall Assessment

6개 Controller URL/Method/경로 파라미터 전체 일치. CRITICAL 없음. MAJOR 2건, MINOR 4건, SUGGESTION 2건 발견.

## Issue Count by Controller

| Controller | CRITICAL | MAJOR | MINOR | SUGGESTION | Verdict |
|---|---|---|---|---|---|
| TrackController | 0 | 2 | 1 | 1 | Issues found |
| TagController | 0 | 0 | 1 | 0 | Issues found |
| PlaylistController | 0 | 0 | 1 | 0 | Issues found |
| PlayHistoryController | 0 | 0 | 0 | 0 | PASS ✅ |
| LikeController | 0 | 0 | 1 | 0 | Issues found |
| DownloadQueueController | 0 | 0 | 0 | 1 | Issues found |
| **Total** | **0** | **2** | **4** | **2** | |

## Issue Summary

### MAJOR-001 — TrackController.getTrack() message 누락
- `TrackController.java:57-59` — `ResponseDTO` 빌더에 `.message()` 호출 없음
- createTrack(`.message("Track created")`), updateTrack(`.message("Track updated")`), getTracks(서비스 내 포함)는 모두 있으나 getTrack만 누락
- **권장**: `.message("Track retrieved")` 추가

### MAJOR-002 — TrackController createTrack/updateTrack/deleteTrack @PreAuthorize 누락
- SecurityConfig에서 URL 레벨 ADMIN 보호는 있으나, `@GetMapping("/admin")`은 `@PreAuthorize` 명시됨 — 혼용 패턴 불일치
- 런타임 보안 문제는 없으나 SecurityConfig 변경 시 권한 누락 위험
- **권장**: `createTrack`, `updateTrack`, `deleteTrack` 에 `@PreAuthorize("hasRole('ADMIN')")` 추가 (이중 보호)

### MINOR-001 — §1.8 isActive 파라미터명 불일치 ⚠️ (실제 동작 영향 있음)
- api-spec: `is_active` (snake_case), 코드: `isActive` (camelCase)
- 클라이언트가 `?is_active=true`로 호출하면 null 바인딩 → 필터 미적용
- **권장**: `@RequestParam(name = "is_active", required = false)` 추가 OR api-spec을 `isActive`로 통일

### MINOR-002 — TagController raw array 반환 (프로젝트 표준 불일치)
- api-spec §2.2: raw array `[{...}]` 명시, 코드도 동일 — 스펙 일치
- 그러나 프로젝트 전체 공통 응답 형식 `{message, data/dataList}` 와 불일치
- **권장**: api-spec 및 코드 통일 여부 결정 필요

### MINOR-003 — PlaylistController thumbnail 처리 방식 불일치
- Track: `TrackCreateRequest` DTO 내 `MultipartFile thumbnail` (@ModelAttribute 일괄)
- Playlist: `@RequestPart(required = false) MultipartFile thumbnail` 별도 파라미터
- 기능상 문제 없음, 팀 내 일관성 관점
- **권장**: Track 방식으로 통일 또는 현 상태 유지 결정

### MINOR-004 — LikeController.getMyLikes() message 누락
- `LikeController.java:34-39` — `ResponseDTO` 빌더에 `.message()` 없음
- **권장**: `.message("Like list retrieved")` 추가

### SUGGESTION-001 — TrackResponse updatedAt 과잉 반환
- api-spec §1.1 POST /api/tracks 응답에 updatedAt 미명시, 코드는 항상 포함
- **권장**: api-spec §1.1에 updatedAt 추가 (문서 보완)

### SUGGESTION-002 — DownloadQueueController addToQueue body 과잉
- api-spec §11.1: 201 Created (body 미명시), 코드: message body 포함 반환
- **권장**: api-spec §11.1에 응답 body 명시 (문서 보완)
