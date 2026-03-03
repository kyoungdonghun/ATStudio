[WI HEADER]
WI ID: WI-20260303-ATS-003
REQ: REQ-20260303-ATS-002
Agent: se
Depends On: -
Blocks: WI-20260303-ATS-004

[WI SUMMARY]
Why: 구독자 플레이리스트 생성 시 최대 3개 제한 비즈니스 규칙이 현재 미구현 상태 — 무제한 생성 가능. 제한 초과 시 409 PLAYLIST_LIMIT_EXCEEDED 반환 필요.
Scope (in/out):
  In:
  - BUSINESS_ERROR.java: PLAYLIST_LIMIT_EXCEEDED 에러코드 추가 (409 Conflict)
  - PlaylistRepository.java: countByUserAndIsActiveTrue(User user) 메서드 추가
  - PlaylistService.createPlaylist(): validateSubscriber() 직후 활성 플레이리스트 수 >= 3 체크 추가
  - PlaylistServiceTest.java: 제한 초과/경계/정상 케이스 테스트 3개 추가
  Out:
  - 기존 플레이리스트 CRUD 8개 API 로직 변경 없음
  - is_active=false 데이터 카운트 제외 (소프트 삭제 현행 유지)
  - 기존 3개 초과 데이터 처리 없음 (신규 생성만 차단)

DoD:
  - 활성 플레이리스트 3개 보유 → 추가 생성 시 409 PLAYLIST_LIMIT_EXCEEDED
  - 활성 플레이리스트 0~2개 보유 → 생성 정상 동작
  - is_active=false 플레이리스트는 카운트 제외
  - ./gradlew test 전체 통과 (0 failures)

Constraints/Forbidden:
  - 기존 플레이리스트 API 8개 동작 변경 금지
  - HTTP 상태코드: 409 Conflict (RESOURCE_DUPLICATE와 동일 패턴)
  - 에러코드명: PLAYLIST_LIMIT_EXCEEDED (변경 금지)
  - 제한 수: 3개 (하드코딩 허용)

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] createPlaylist() 호출 시 countByUserAndIsActiveTrue() >= 3이면 BusinessException(PLAYLIST_LIMIT_EXCEEDED) throw
  - [ ] countByUserAndIsActiveTrue()는 is_active=true인 플레이리스트만 카운트
  - [ ] 플레이리스트 2개 보유 시 생성 성공 (경계값 정상)
  - [ ] 플레이리스트 3개 보유 시 생성 실패 (경계값 차단)

Performance:
  - N/A (단순 count 쿼리 추가)

Quality:
  - [ ] ./gradlew test 0 failures (기존 542건 포함 전체 통과)
  - [ ] PlaylistServiceTest에 최소 2개 신규 테스트 케이스 (초과 케이스, 정상 케이스)

[INPUT POINTERS]
Tier 0 (Constitution):
- docs/standards/core-principles.md

Tier 0 (Development Standards):
- docs/standards/development-standards.md

REQ/Context Docs:
- deliverables/user/REQ-20260303-ATS-002.md

Files (수정 대상):
- src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java
  → SUBSCRIPTION_ALREADY_EXISTS 아래 PLAYLIST_LIMIT_EXCEEDED 추가 (L126 인근)
  → 패턴: HttpStatus.CONFLICT, "플레이리스트는 최대 3개까지 생성할 수 있습니다.", "활성 플레이리스트 3개 초과 시도."
- src/main/java/com/atstudio/atstudio/repository/PlaylistRepository.java
  → countByUserAndIsActiveTrue(User user) 메서드 추가 (Spring Data 쿼리 메서드)
- src/main/java/com/atstudio/atstudio/service/PlaylistService.java:40-58
  → createPlaylist() 메서드: validateSubscriber() 호출 직후, thumbnailUrl 처리 전에 카운트 체크 삽입
- src/test/java/com/atstudio/atstudio/service/PlaylistServiceTest.java:49~
  → createPlaylist 테스트 섹션에 케이스 추가

Files (참고용):
- src/main/java/com/atstudio/atstudio/entity/Playlist.java (is_active 필드 확인)
- src/main/java/com/atstudio/atstudio/entity/User.java (User 엔티티 확인)

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260303-ATS-003-summary.md :
  - 구현 결과 요약, 변경 파일 목록, 테스트 결과
Agent-facing -> deliverables/agent/WI-20260303-ATS-003-evidence-pack.md :
  - 변경된 코드 라인 포인터, 테스트 명령어 및 결과, 추가 WI 필요 여부
Handoff Packet -> deliverables/agent/WI-20260303-ATS-003-handoff.md :
  - 이 파일 (트레이서빌리티용)

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 변경 파일별 라인 번호 명시
Tests: ./gradlew test 실행 결과 (총 테스트 수, failures 수)
Rollback: BUSINESS_ERROR enum 항목 제거, Repository 메서드 제거, Service 카운트 체크 제거
