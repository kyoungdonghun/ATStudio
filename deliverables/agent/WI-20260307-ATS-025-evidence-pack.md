[EVIDENCE PACK]
WI ID: WI-20260307-ATS-025
REQ: REQ-20260307-ATS-009
Agent: cr
Completed: 2026-03-08

---

## MAJOR-001: TrackController.getTrack() message 누락
- `src/main/java/com/atstudio/atstudio/controller/TrackController.java:57-59`
  ```java
  return ResponseEntity.ok(ResponseDTO.<TrackResponse>withSingleData()
          .data(trackService.getTrack(trackId))
          .build());  // .message() 없음
  ```
- 비교: createTrack(line 34) `.message("Track created")`, updateTrack(line 88) `.message("Track updated")`
- 수정: `.message("Track retrieved")` 추가

## MAJOR-002: TrackController @PreAuthorize 혼용
- `src/main/java/com/atstudio/atstudio/controller/TrackController.java`
  - line 27-37: `createTrack` — @PreAuthorize 없음
  - line 83-91: `updateTrack` — @PreAuthorize 없음
  - line 93-98: `deleteTrack` — @PreAuthorize 없음
  - line 46-51: `getTracksForAdmin` — `@PreAuthorize("hasRole('ADMIN')")` 있음
- `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:81`
  `.requestMatchers(HttpMethod.POST, "/api/tracks").hasRole("ADMIN")` — URL 레벨 보호 존재
- 수정: createTrack/updateTrack/deleteTrack에 `@PreAuthorize("hasRole('ADMIN')")` 추가

## MINOR-001: §1.8 isActive 파라미터명 불일치
- api-spec: `docs/design/api-spec.md` §1.8 Query Params: `is_active: Boolean (optional)`
- 코드: `src/main/java/com/atstudio/atstudio/controller/TrackController.java:48`
  `@RequestParam(required = false) Boolean isActive`
- 실제 영향: `?is_active=true` 호출 시 null 바인딩 → 필터 미작동
- 수정: `@RequestParam(name = "is_active", required = false) Boolean isActive`

## MINOR-002: TagController raw array
- `src/main/java/com/atstudio/atstudio/controller/TagController.java:37-39`
  `ResponseEntity<List<TagResponse>>`
- api-spec §2.2: raw array — 스펙과 코드 일치
- 프로젝트 표준(ResponseDTO 래핑)과 불일치

## MINOR-003: PlaylistController thumbnail 처리 방식
- `src/main/java/com/atstudio/atstudio/controller/PlaylistController.java:29-31`
  `@RequestPart(required = false) MultipartFile thumbnail` 별도
- 비교: TrackController — TrackCreateRequest DTO 내 포함

## MINOR-004: LikeController.getMyLikes() message 누락
- `src/main/java/com/atstudio/atstudio/controller/LikeController.java:34-39`
  `.message()` 호출 없음

## SUGGESTION-001: TrackResponse updatedAt 과잉
- `src/main/java/com/atstudio/atstudio/dto/track/TrackResponse.java:22` — updatedAt 포함
- api-spec §1.1 POST 응답에 미명시 (§1.3 GET에는 명시)

## SUGGESTION-002: DownloadQueue addToQueue body 과잉
- `src/main/java/com/atstudio/atstudio/controller/DownloadQueueController.java:23-30`
  message body 반환
- api-spec §11.1: body 미명시
