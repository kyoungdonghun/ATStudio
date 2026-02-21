[WI HEADER]
WI ID: WI-20260221-ATS-008
REQ: REQ-20260221-ATS-002
Agent: se
Depends On: -
Blocks: WI-20260221-ATS-010, WI-20260221-ATS-011, WI-20260221-ATS-013

[WI SUMMARY]
Why: 좋아요(Likes)와 다운로드 큐(Download Queue) — 복합 PK 기반 단순 toggle/CRUD 구현
Scope (in/out):
  In:
    - LikeService (addLike, getMyLikes, removeLike)
    - LikeController (POST /api/likes/{trackId}, GET /api/likes, DELETE /api/likes/{trackId})
    - LikeResponse DTO
    - DownloadQueueService (addToQueue, getMyQueue, removeFromQueue)
    - DownloadQueueController (POST /api/download-queue/{trackId}, GET, DELETE /{trackId})
    - DownloadQueueResponse DTO
  Out:
    - 테스트 코드 (WI-010, WI-011 담당)
    - 다른 서비스 수정

DoD:
  - 6개 엔드포인트가 api-spec.md v5 Section 10, 11 명세와 일치
  - 중복 추가 시 DataIntegrityViolationException → GlobalExceptionHandler가 409로 처리 (별도 처리 불필요)
  - 삭제 시 본인 소유 검증 (user_id 매칭)
  - ./gradlew build -x test 성공

Constraints/Forbidden:
  - Entity 수정 금지 (Like, DownloadQueue, Track)
  - 복합 PK 엔티티의 save() = merge() 동작 인지할 것 (중복 INSERT 예외 → 409)
  - 새 DTO는 record 타입, @JsonInclude(NON_NULL)
  - Like 목록 응답: trackId, title, bpm, tonality, thumbnail, createdAt 포함 (api-spec 10.2)
  - DownloadQueue 목록 응답: trackId, title, bpm, tonality, thumbnail, createdAt 포함 (api-spec 11.2)

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] POST /api/likes/{trackId}: 좋아요 추가 → 201, 중복 → 409
  - [ ] GET /api/likes: 내 좋아요 목록 반환 → 200 (트랙 요약 포함)
  - [ ] DELETE /api/likes/{trackId}: 좋아요 해제 → 204, 없으면 404
  - [ ] POST /api/download-queue/{trackId}: 큐 추가 → 201, 중복 → 409
  - [ ] GET /api/download-queue: 내 큐 목록 반환 → 200
  - [ ] DELETE /api/download-queue/{trackId}: 큐 제거 → 204
  - [ ] 모든 엔드포인트: 비인증 → 401
Quality:
  - [ ] ./gradlew build -x test 성공
  - [ ] 기존 209개 테스트 영향 없음

[INPUT POINTERS]
Tier 0 (Constitution):
  - docs/standards/core-principles.md
Tier 0 (Standards):
  - docs/standards/development-standards.md
  - docs/standards/dto-standards.md
  - docs/standards/exception-handling.md

REQ/Context Docs:
  - deliverables/user/REQ-20260221-ATS-002.md
  - docs/design/api-spec.md (Section 10.1~10.3, 11.1~11.3)
  - docs/design/db-schema.md (Section 7.1 likes, 8.1 download_queue)

Files (기존 코드 참조):
  - src/main/java/com/atstudio/atstudio/entity/Like.java
  - src/main/java/com/atstudio/atstudio/entity/DownloadQueue.java
  - src/main/java/com/atstudio/atstudio/entity/key/LikeId.java
  - src/main/java/com/atstudio/atstudio/entity/key/DownloadQueueId.java
  - src/main/java/com/atstudio/atstudio/repository/LikeRepository.java
  - src/main/java/com/atstudio/atstudio/repository/DownloadQueueRepository.java
  - src/main/java/com/atstudio/atstudio/repository/TrackRepository.java
  - src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java
  - src/main/java/com/atstudio/atstudio/service/DownloadService.java (패턴 참조 — user 조회 방식)
  - src/test/java/com/atstudio/atstudio/repository/LikeRepositoryTest.java (기존 테스트 참조)

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260221-ATS-008-summary.md :
  - 구현된 엔드포인트, 중복 처리 방식
Agent-facing -> deliverables/agent/WI-20260221-ATS-008-evidence-pack.md :
  - 생성/수정 파일 목록, 빌드 결과
Handoff Packet -> deliverables/agent/WI-20260221-ATS-008-handoff.md :
  - 이 파일

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 생성/수정한 모든 파일 경로 명시
Tests: ./gradlew build -x test 결과
