[WI HEADER]
WI ID: WI-20260303-ATS-006
REQ: REQ-20260303-ATS-003
Agent: cr
Depends On: WI-20260303-ATS-005
Blocks: -

[WI SUMMARY]
Why: WI-20260303-ATS-005에서 구현된 Album 신규 도메인(17개 신규 + 3개 수정)의 코드 품질, 아키텍처, 보안, 테스트 커버리지 검증.
Scope (in/out):
  In:
  - Entity: Album.java, AlbumTrack.java, AlbumTrackId.java
  - Repository: AlbumRepository.java, AlbumTrackRepository.java
  - DTO: dto/album/ 하위 9개 파일
  - Service: AlbumService.java
  - Controller: AlbumController.java
  - SecurityConfig.java (추가분만)
  - AlbumServiceTest.java
  - api-spec.md Section 15, db-schema.md 추가분
  Out:
  - 기존 Playlist/PlayHistory 등 다른 도메인 코드 리뷰 범위 외
  - 기존 SecurityConfig 규칙(앨범 외) 변경 금지

DoD:
  - PASS: CRITICAL 0, MAJOR 0
  - CONDITIONALLY PASS: CRITICAL 0, MAJOR >= 1
  - FAIL: CRITICAL >= 1

Constraints/Forbidden:
  - 범위 밖 기존 코드 변경 제안 금지

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] ADMIN write / PUBLIC read 권한 분리 정확성
  - [ ] softDelete 방식 (is_active=false) 정확성
  - [ ] 복합PK(AlbumTrackId) 구현 정확성
  - [ ] 중복 트랙 추가 방지 (RESOURCE_DUPLICATE) 구현 정확성
  - [ ] AlbumServiceTest 테스트 커버리지 (주요 케이스 포함 여부)

Quality:
  - [ ] @Transactional(readOnly=true) 클래스 레벨, mutating 메서드만 override 확인
  - [ ] Entity → DTO 변환 (Entity 직접 반환 없음) 확인
  - [ ] api-spec.md Section 15 명세 ↔ Controller 구현 일치

[INPUT POINTERS]
Tier 0 (Constitution):
- docs/standards/core-principles.md

Tier 0 (Development Standards):
- docs/standards/development-standards.md

REQ/Context Docs:
- deliverables/user/REQ-20260303-ATS-003.md

WI Evidence (se 구현 결과):
- deliverables/agent/WI-20260303-ATS-005-evidence-pack.md

Files (리뷰 대상):
- src/main/java/com/atstudio/atstudio/entity/key/AlbumTrackId.java
- src/main/java/com/atstudio/atstudio/entity/AlbumTrack.java
- src/main/java/com/atstudio/atstudio/entity/Album.java
- src/main/java/com/atstudio/atstudio/repository/AlbumRepository.java
- src/main/java/com/atstudio/atstudio/repository/AlbumTrackRepository.java
- src/main/java/com/atstudio/atstudio/dto/album/ (전체)
- src/main/java/com/atstudio/atstudio/service/AlbumService.java
- src/main/java/com/atstudio/atstudio/controller/AlbumController.java
- src/main/java/com/atstudio/atstudio/config/SecurityConfig.java (Album 추가분)
- src/test/java/com/atstudio/atstudio/service/AlbumServiceTest.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260303-ATS-006-summary.md
Agent-facing -> deliverables/agent/WI-20260303-ATS-006-evidence-pack.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 이슈별 파일명:라인번호 필수
Rollback: N/A (리뷰 전용 WI)
