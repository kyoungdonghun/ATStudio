[WI HEADER]
WI ID: WI-20260303-ATS-004
REQ: REQ-20260303-ATS-002
Agent: cr
Depends On: WI-20260303-ATS-003
Blocks: -

[WI SUMMARY]
Why: WI-20260303-ATS-003에서 구현된 플레이리스트 3개 제한 로직의 정확성, 코드 품질, 비즈니스 규칙 준수를 검증.
Scope (in/out):
  In:
  - BUSINESS_ERROR.java: PLAYLIST_LIMIT_EXCEEDED 에러코드 패턴, 상태코드(409), 메시지 적합성 검토
  - PlaylistRepository.java: countByUserAndIsActiveTrue() 쿼리 메서드 정확성 검토
  - PlaylistService.createPlaylist(): 카운트 체크 위치, 조건(>= 3), 예외 처리 방식 검토
  - PlaylistServiceTest.java: 신규 테스트 케이스의 커버리지, 경계값, Mockito 패턴 검토
  Out:
  - 다른 서비스 파일 변경 없음
  - REQ 범위 외 개선 사항 구현 금지

DoD:
  - CRITICAL 0건, MAJOR 0건 → PASS
  - CRITICAL 또는 MAJOR 발견 시 → FAIL (구현 수정 필요)
  - MINOR/SUGGESTION은 명세만 기록

Constraints/Forbidden:
  - 리뷰 범위: 4개 파일 변경분에 한정
  - 기존 코드 변경 제안 금지 (이번 WI 범위 외)

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] PLAYLIST_LIMIT_EXCEEDED HTTP 상태코드 409 확인
  - [ ] countByUserAndIsActiveTrue — isActive=false 제외 여부 검토
  - [ ] createPlaylist() 제한 체크 위치 검토 (validateSubscriber() 직후, 썸네일 처리 전)
  - [ ] 테스트: 제한 초과 케이스(count=3 → throw), 경계값 케이스(count=2 → 성공) 존재

Quality:
  - [ ] 기존 테스트 546건 전체 통과 확인 (evidence-pack 기준)

[INPUT POINTERS]
Tier 0 (Constitution):
- docs/standards/core-principles.md

Tier 0 (Development Standards):
- docs/standards/development-standards.md

REQ/Context Docs:
- deliverables/user/REQ-20260303-ATS-002.md

WI Evidence:
- deliverables/agent/WI-20260303-ATS-003-evidence-pack.md

Files (리뷰 대상):
- src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java
- src/main/java/com/atstudio/atstudio/repository/PlaylistRepository.java
- src/main/java/com/atstudio/atstudio/service/PlaylistService.java:40-60 (createPlaylist 영역)
- src/test/java/com/atstudio/atstudio/service/PlaylistServiceTest.java (신규 테스트 케이스)

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260303-ATS-004-summary.md :
  - 리뷰 verdict (PASS / FAIL), 발견 이슈 목록, 권장 조치
Agent-facing -> deliverables/agent/WI-20260303-ATS-004-evidence-pack.md :
  - 이슈별 파일:라인 포인터, 심각도, 설명, 권장 수정 방향

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 이슈별 파일명:라인번호 명시 필수
Rollback: N/A (리뷰 전용 WI)
