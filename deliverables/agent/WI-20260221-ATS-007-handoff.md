[WI HEADER]
WI ID: WI-20260221-ATS-007
REQ: REQ-20260221-ATS-002
Agent: se
Depends On: -
Blocks: WI-20260221-ATS-010, WI-20260221-ATS-011, WI-20260221-ATS-013

[WI SUMMARY]
Why: 재생 기록 저장·조회·삭제 + play_count 동시성 안전 증가 구현
Scope (in/out):
  In:
    - PlayHistoryService (save, getMyHistory, deleteHistory)
    - PlayHistoryController (POST /api/play-histories, GET, DELETE)
    - PlayHistoryListItemResponse, PlayHistorySaveRequest, PlayHistoryDeleteRequest DTO
    - TrackRepository에 play_count +1 @Modifying 쿼리 추가
    - SecurityConfig: POST /api/play-histories → auth required (permitAll 아님)
  Out:
    - 테스트 코드 (WI-010, WI-011 담당)
    - 다른 서비스 수정

DoD:
  - 3개 엔드포인트가 api-spec.md v5 Section 4 명세와 일치
  - play_count: @Modifying JPQL UPDATE로 원자적 +1 (동시성 안전)
  - DELETE: historyIds 비어있으면 전체 삭제, historyIds 있으면 선택 삭제 (본인 소유만)
  - ./gradlew build -x test 성공

Constraints/Forbidden:
  - Entity 수정 금지 (PlayHistory, Track)
  - play_count는 반드시 JPQL UPDATE 사용 (entity.setPlayCount() 방식 금지 — 동시성 위험)
  - 삭제 시 타인 기록 삭제 불가 (user_id 검증)
  - 새 DTO는 record 타입, @JsonInclude(NON_NULL)

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] POST /api/play-histories {trackId}: 재생기록 저장 + Track.playCount +1 → 201
  - [ ] POST: 비인증 → 401, 존재하지 않는 트랙 → 404
  - [ ] GET /api/play-histories?page=1&size=50: 내 재생 목록 최신순 반환 → 200
  - [ ] DELETE /api/play-histories {historyIds:[100,101]}: 선택 삭제 → 204
  - [ ] DELETE /api/play-histories {historyIds:[]}: 전체 삭제 → 204
  - [ ] play_count: 동시 요청에도 누락 없이 +1 (JPQL UPDATE 방식)
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
  - docs/design/api-spec.md (Section 4.1, 4.2, 4.3)
  - docs/design/db-schema.md (Section 6.2 play_histories, 4.1 tracks.play_count)

Files (기존 코드 참조):
  - src/main/java/com/atstudio/atstudio/entity/PlayHistory.java
  - src/main/java/com/atstudio/atstudio/entity/Track.java
  - src/main/java/com/atstudio/atstudio/repository/PlayHistoryRepository.java (빈 인터페이스)
  - src/main/java/com/atstudio/atstudio/repository/TrackRepository.java
  - src/main/java/com/atstudio/atstudio/config/SecurityConfig.java (permitAll 목록 확인)
  - src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java
  - src/main/java/com/atstudio/atstudio/common/dto/ResponseDTO.java
  - src/main/java/com/atstudio/atstudio/common/dto/PageInfo.java
  - src/main/java/com/atstudio/atstudio/service/TrackService.java (패턴 참조)

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260221-ATS-007-summary.md :
  - 구현된 엔드포인트, play_count 처리 방식 결정
Agent-facing -> deliverables/agent/WI-20260221-ATS-007-evidence-pack.md :
  - 생성/수정 파일 목록, 빌드 결과
Handoff Packet -> deliverables/agent/WI-20260221-ATS-007-handoff.md :
  - 이 파일

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 생성/수정한 모든 파일 경로 명시
Tests: ./gradlew build -x test 결과
Rollback: TrackRepository에 추가한 @Modifying 쿼리 명시
