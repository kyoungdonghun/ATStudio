[WI HEADER]
WI ID: WI-20260221-ATS-016
REQ: REQ-20260221-ATS-003
Agent: se
Depends On: -
Blocks: WI-20260221-ATS-017

[WI SUMMARY]
Why: WI-013 코드 리뷰에서 발견된 @Transactional 패턴 위반 3건 수정. UserService/LikeService/DownloadQueueService가 클래스 레벨 @Transactional(readOnly=true) 없이 @Transactional만 사용 → 표준 템플릿으로 정규화.
Scope (in/out):
  In:
    - UserService: 클래스 레벨 @Transactional → @Transactional(readOnly = true) 변경; mutating 메서드(register, updateMyProfile, withdraw, completeProfile, updateUserByAdmin)에 @Transactional 추가 [M-04]
    - LikeService: 클래스 레벨 @Transactional → @Transactional(readOnly = true); mutating 메서드(addLike, removeLike)에 @Transactional 추가 [M-05]
    - DownloadQueueService: 클래스 레벨 @Transactional → @Transactional(readOnly = true); mutating 메서드(addToQueue, removeFromQueue)에 @Transactional 추가 [M-05]
  Out:
    - Service 외 파일(Repository, Controller, DTO, Entity) 수정 금지
    - 비즈니스 로직 변경 금지 (트랜잭션 어노테이션만 수정)
    - 테스트 파일 수정 금지
DoD:
  - UserService 클래스: @Transactional(readOnly = true)
  - UserService mutating 5개 메서드: @Transactional (별도 적용)
  - LikeService 클래스: @Transactional(readOnly = true)
  - LikeService mutating 2개 메서드: @Transactional
  - DownloadQueueService 클래스: @Transactional(readOnly = true)
  - DownloadQueueService mutating 2개 메서드: @Transactional
Constraints/Forbidden:
  - 수정 대상 3개 파일 외 변경 금지
  - 비즈니스 로직, 메서드 서명, 반환 타입 변경 금지
  - read 메서드에 @Transactional 추가 금지 (클래스 레벨로 상속됨)

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] UserService class: @Transactional(readOnly = true)
  - [ ] UserService.register(): @Transactional
  - [ ] UserService.updateMyProfile(): @Transactional
  - [ ] UserService.withdraw(): @Transactional
  - [ ] UserService.completeProfile(): @Transactional
  - [ ] UserService.updateUserByAdmin(): @Transactional
  - [ ] LikeService class: @Transactional(readOnly = true)
  - [ ] LikeService.addLike(): @Transactional
  - [ ] LikeService.removeLike(): @Transactional
  - [ ] DownloadQueueService class: @Transactional(readOnly = true)
  - [ ] DownloadQueueService.addToQueue(): @Transactional
  - [ ] DownloadQueueService.removeFromQueue(): @Transactional
Performance:
  - [ ] read 메서드에 readOnly=true 적용되어 DB read-only 최적화
Quality:
  - [ ] 컴파일 오류 없음
  - [ ] 기존 테스트 통과 (WI-017에서 검증)

[INPUT POINTERS]
Tier 0 (Constitution):
  - docs/standards/core-principles.md

Tier 0 (Standards):
  - docs/standards/development-standards.md

REQ/Context Docs:
  - deliverables/user/REQ-20260221-ATS-003.md
  - deliverables/user/WI-20260221-ATS-013-summary.md

Files (primary targets):
  - src/main/java/com/atstudio/atstudio/service/UserService.java
  - src/main/java/com/atstudio/atstudio/service/LikeService.java
  - src/main/java/com/atstudio/atstudio/service/DownloadQueueService.java

Reference (올바른 패턴 예시):
  - src/main/java/com/atstudio/atstudio/service/TagService.java (class: readOnly=true, mutating override 패턴 확인)
  - src/main/java/com/atstudio/atstudio/service/NoticeService.java (동일 패턴)

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260221-ATS-016-summary.md :
  - 수정된 서비스 3개 목록 + @Transactional 변경 내역
Agent-facing -> deliverables/agent/WI-20260221-ATS-016-evidence-pack.md :
  - 수정 전/후 클래스 어노테이션 + mutating 메서드 목록
Handoff Packet -> deliverables/agent/WI-20260221-ATS-016-handoff.md :
  - This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers: 각 파일:라인 번호 필수 (클래스 선언부 + 각 mutating 메서드)
Tests: WI-017 (qa)에서 검증
Rollback: git diff로 추적 가능
